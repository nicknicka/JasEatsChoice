# ChatMemory 共享问题分析

## 🔍 当前实现

### 1. ChatMemory Bean 配置（单例）
```java
@Bean
public ChatMemory chatMemory() {
    return MessageWindowChatMemory.withMaxMessages(20);
}
```

**问题**: 这是一个 **Spring单例Bean**，整个应用只有一个实例。

### 2. 使用方式
```java
// SupervisorAgentFactory 中
.chatMemoryProvider(memoryId -> chatMemory)  // 总是返回同一个实例

// SupervisorAgent Bean 中
.chatMemoryProvider(memoryId -> chatMemory)  // 总是返回同一个实例
```

**问题**: 虽然使用了 `chatMemoryProvider`，但lambda表达式总是返回同一个 `chatMemory` 实例。

---

## ⚠️ 潜在问题

### 问题1：多用户会话混淆

**场景**:
```
时间线:
10:00:00 - 用户A发送消息: "推荐川菜"
10:00:05 - 用户B发送消息: "推荐粤菜"
10:00:10 - 用户A发送消息: "再来点" (期望继续川菜推荐)

实际结果:
❌ 用户A可能看到粤菜推荐，因为ChatMemory中最后一条消息是用户B的
```

**原因**:
```
用户A的对话历史:
[User: 推荐川菜] [AI: 川菜有...]

用户B的对话历史:
[User: 推荐川菜] [AI: 川菜有...] [User: 推荐粤菜] [AI: 粤菜有...]
                                    ↑ 用户B的消息被添加进来了

当用户A问"再来点"时，Agent看到的是用户B的"推荐粤菜"，
所以会继续推荐粤菜！
```

### 问题2：并发安全问题

**场景**: 多个请求同时写入同一个ChatMemory
```java
// 请求1
chatMemory.add(userMessage1);  // 线程1
chatMemory.add(aiResponse1);   // 线程1

// 请求2 同时进行
chatMemory.add(userMessage2);  // 线程2
chatMemory.add(aiResponse2);   // 线程2
```

**可能结果**:
- 消息顺序混乱
- 消息丢失
- 内存溢出（消息窗口不断增长）

### 问题3：内存泄漏风险

**场景**:
```
每个用户的消息都累积在同一个ChatMemory中
- 窗口大小: 20条消息
- 但如果有100个用户同时使用，实际会有 2000+ 条消息
- MessageWindowChatMemory的"20条"是指每个conversationId的20条，
  但如果使用默认memoryId，所有请求共享同一个conversationId
```

---

## ✅ 正确的解决方案

### 方案1: 使用 MemoryId 隔离（推荐）

```java
// 修改 SupervisorAgentFactory
public SupervisorAgent createWithListener(
    AgentListener listener,
    String sessionId  // 每个请求传入唯一的sessionId
) {
    return AgenticServices
        .supervisorBuilder(SupervisorAgent.class)
        .chatModel(supervisorModel)
        .chatMemoryProvider(memoryId -> {
            // ✅ 为每个 sessionId 创建独立的 ChatMemory
            return MessageWindowChatMemory.withMaxMessages(20);
        })
        .build();
}
```

**Controller中调用**:
```java
@GetMapping("/chat")
public SseEmitter chatStream(@RequestParam String message) {
    String sessionId = UUID.randomUUID().toString();  // 每个请求唯一ID
    SSEAgentListener listener = new SSEAgentListener(emitter);
    SupervisorAgent agent = factory.createWithListener(listener, sessionId);
    // ...
}
```

### 方案2: 使用 Provider 模式

```java
// 修改 Bean 配置
@Bean
public Supplier<ChatMemory> chatMemoryProvider() {
    return () -> MessageWindowChatMemory.withMaxMessages(20);
}

// Factory 中使用
private final Supplier<ChatMemory> chatMemoryProvider;

public SupervisorAgent createWithListener(AgentListener listener) {
    return AgenticServices
        .supervisorBuilder(SupervisorAgent.class)
        .chatMemoryProvider(memoryId -> chatMemoryProvider.get())  // 每次创建新实例
        .build();
}
```

### 方案3: 使用 Redis 分布式 ChatMemory

```java
@Bean
public ChatMemory chatMemory(RedisTemplate<String, Object> redisTemplate) {
    return RedisChatMemory.builder()
        .redisTemplate(redisTemplate)
        .maxMessages(20)
        .ttl(Duration.ofHours(1))  // 1小时后自动清理
        .build();
}
```

**优势**:
- 天然支持多用户隔离
- 持久化存储
- 自动过期清理

---

## 📊 影响评估

### 当前实现的风险

| 风险类型 | 严重程度 | 发生概率 | 影响 |
|---------|---------|---------|------|
| 多用户会话混淆 | 🔴 高 | 🟡 中 | 用户看到别人的对话内容 |
| 并发安全问题 | 🟡 中 | 🟢 低 | 消息顺序混乱 |
| 内存泄漏 | 🟡 中 | 🟡 中 | 内存持续增长 |

### 实际测试场景

**场景A: 单用户测试**
```
✅ 没问题，因为只有一个用户
```

**场景B: 多用户顺序访问**
```
⚠️ 有问题，后一个用户会看到前一个用户的对话历史
例如:
  用户A: 推荐川菜
  用户B: 今天天气 (AI可能回复"关于川菜的推荐...")
```

**场景C: 多用户并发访问**
```
❌ 严重问题，对话历史完全混乱
```

---

## 🎯 建议优先级

### 立即修复（如果支持多用户）
**优先级**: 🔴 P0 - 严重

**修改方案**: 使用方案1（MemoryId隔离）
**工作量**: 15分钟
**影响**: 需要修改Factory和Controller

### 可以延迟（如果只支持单用户）
**优先级**: 🟡 P2 - 优化

**原因**: 如果系统只支持单用户使用，当前实现是安全的
**建议**: 在文档中明确标注"当前版本仅支持单用户会话"

### 长期优化
**优先级**: 🟢 P3 - 增强

**方案**: 使用Redis ChatMemory
**优势**: 支持分布式部署，会话持久化

---

## 🔧 快速修复代码

### 修改1: SupervisorAgentFactory

```java
public SupervisorAgent createWithListener(AgentListener listener, String sessionId) {
    log.debug("创建带监听器的SupervisorAgent，sessionId={}", sessionId);

    return AgenticServices
        .supervisorBuilder(SupervisorAgent.class)
        .chatModel(supervisorModel)
        // ✅ 使用 sessionId 作为 memoryId
        .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
        .listener(listener)
        .build();
}
```

### 修改2: SupervisorSSEController

```java
@GetMapping("/chat")
public SseEmitter chatStream(@RequestParam String message) {
    String sessionId = UUID.randomUUID().toString();  // ✅ 生成唯一ID
    SSEAgentListener listener = new SSEAgentListener(emitter);
    SupervisorAgent agent = factory.createWithListener(listener, sessionId);  // ✅ 传入sessionId

    // ... 其他代码
}
```

### 修改3: 支持用户ID作为sessionId

```java
@GetMapping("/chat")
public SseEmitter chatStream(
    @RequestParam String message,
    @RequestParam(required = false) String userId  // 可选
) {
    // ✅ 如果有userId，使用userId作为sessionId（同一用户连续对话）
    // ✅ 如果没有userId，生成新的sessionId（每次新对话）
    String sessionId = userId != null ? userId : UUID.randomUUID().toString();

    SSEAgentListener listener = new SSEAgentListener(emitter);
    SupervisorAgent agent = factory.createWithListener(listener, sessionId);

    // ... 其他代码
}
```

---

## 📝 总结

**当前实现**: ⚠️ 仅适用于单用户场景
**生产环境**: ❌ 需要修改为多用户隔离
**建议修复**: 使用sessionId隔离ChatMemory

**修复后优势**:
- ✅ 每个用户独立的对话历史
- ✅ 支持多用户并发
- ✅ 内存安全，不会泄漏
- ✅ 支持同一用户的连续对话

---

**分析时间**: 2026-03-26
**建议**: 如果项目需要支持多用户，建议立即修复此问题
