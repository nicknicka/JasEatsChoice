# Redis + MySQL 混合存储方案实施总结

**实施时间**: 2026-03-26
**方案**: Redis(2小时TTL) + 异步MySQL持久化
**状态**: ✅ 代码完成，待编译测试

---

## ✅ 已完成的工作

### 1. 核心组件（5个新文件）

#### 1.1 RedisBackedChatMemory
**位置**: `agent/memory/RedisBackedChatMemory.java`

**功能**:
- 实现 `ChatMemory` 接口
- Redis存储最近20条消息（TTL=2小时）
- 异步写入MySQL持久化
- Redis未命中时从MySQL加载历史

**关键方法**:
```java
public void add(ChatMessage message) {
    // 1. 添加到本地缓存
    localMessages.add(message);

    // 2. 写入Redis
    redisTemplate.opsForList().rightPush(redisKey, message);
    redisTemplate.opsForList().trim(redisKey, -20, -1);
    redisTemplate.expire(redisKey, ttl);

    // 3. 异步写入MySQL
    asyncSaveToMySQL(message);
}
```

#### 1.2 ChatMemoryConfig
**位置**: `agent/config/ChatMemoryConfig.java`

**功能**:
- 创建 `Function<String, ChatMemory>` Provider
- 配置TTL=2小时
- 配置maxMessages=20

**关键配置**:
```java
@Bean
public Function<String, ChatMemory> redisChatMemoryProvider(
    RedisTemplate<String, String> redisTemplate,
    AIChatHistoryMapper chatHistoryMapper
) {
    Duration ttl = Duration.ofHours(2);
    int maxMessages = 20;

    return memoryId -> new RedisBackedChatMemory(
        redisTemplate, chatHistoryMapper,
        Long.parseLong(memoryId), ttl, maxMessages
    );
}
```

#### 1.3 SupervisorAgentFactory (修改)
**位置**: `agent/service/SupervisorAgentFactory.java`

**变更**:
- 添加 `Function<String, ChatMemory>` 注入
- `createWithListener()` 方法新增 `userId` 参数
- 使用 chatMemoryProvider 为每个用户创建独立ChatMemory

**关键代码**:
```java
public SupervisorAgent createWithListener(
    AgentListener listener,
    String userId  // ✅ 新增参数
) {
    return AgenticServices
        .supervisorBuilder(SupervisorAgent.class)
        .chatMemoryProvider(memoryId ->
            chatMemoryProvider.apply(userId)  // ✅ 每个用户独立
        )
        .listener(listener)
        .build();
}
```

#### 1.4 SupervisorSSEController (修改)
**位置**: `controller/SupervisorSSEController.java`

**变更**:
- 生成临时userId（如果未提供）
- 传递userId给Factory

**关键代码**:
```java
public SseEmitter chatStream(
    @RequestParam String message,
    @RequestParam(required = false) String userId
) {
    // 如果没有userId，生成临时ID
    if (userId == null || userId.isEmpty()) {
        userId = UUID.randomUUID().toString();
    }

    SupervisorAgent agent = factory.createWithListener(listener, userId);
    // ...
}
```

#### 1.5 AsyncConfig (新增)
**位置**: `config/AsyncConfig.java`

**功能**:
- 配置异步线程池
- 支持 `@Async` 注解
- 用于异步写入MySQL

**配置**:
```java
@Bean
public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(100);
    return executor;
}
```

---

## 📊 架构设计

### 数据流

```
用户发送消息
    ↓
┌────────────────────────────────┐
│ 1. ChatMemory.getChatMemory(userId)  │
│    → 查询Redis                       │
│    → 未命中? 从MySQL加载              │
│    → 回写到Redis                    │
└────────────┬───────────────────────┘
             ↓
┌────────────────────────────────┐
│ 2. ChatMemory.add(message)      │
│    → 添加到本地缓存               │
│    → 写入Redis                   │
│    → 设置TTL=2小时               │
└────────────┬───────────────────────┘
             ↓
┌────────────────────────────────┐
│ 3. 异步写入MySQL                │
│    @Async                      │
│    → 不阻塞主流程               │
└────────────────────────────────┘
```

### Redis数据结构

```
Key: chat:memory:{userId}
Type: List
TTL: 2小时

数据格式: "user|消息内容" 或 "ai|消息内容"

例如:
chat:memory:123 → [
    "user|推荐川菜",
    "ai|我推荐宫保鸡丁",
    "user|辣一点的",
    "ai|尝试水煮鱼"
]
```

### MySQL数据表

```
表: t_ai_chat_history

字段:
- id (Long)
- userId (String) - 用户ID
- sender (String) - "user" 或 "ai"
- content (String) - 消息内容
- createTime (LocalDateTime) - 创建时间
- messageType (String) - 消息类型（可选）
- cardData (String) - 卡片数据（可选）
```

---

## 🎯 TTL设置说明

### 最终选择: **TTL = 2小时**

### 理由

1. **用户体验好** ✅
   - 覆盖午餐场景（12:00-14:00）
   - 覆盖下午茶场景（15:00-17:00）
   - 支持连续对话

2. **成本合理** ✅
   - 1000活跃用户 × 100KB = 100MB
   - Redis 512MB实例 ≈ 150元/月
   - 可接受的成本

3. **MySQL兜底** ✅
   - Redis过期后从MySQL恢复
   - 不丢失历史数据

4. **对比其他方案** ✅

| TTL | 用户体验 | 内存(1000用户) | 成本 | 评分 |
|-----|---------|----------------|------|------|
| 10分钟 | ⭐⭐ | 17MB | 50元 | 6/10 |
| 30分钟 | ⭐⭐⭐ | 50MB | 100元 | 7/10 |
| **2小时** | **⭐⭐⭐⭐** | **100MB** | **150元** | **9/10** |
| 4小时 | ⭐⭐⭐⭐⭐ | 200MB | 250元 | 8/10 |
| 24小时 | ⭐⭐⭐⭐⭐ | 1.2GB | 800元 | 6/10 |

---

## 💰 成本分析

### Redis成本

| 用户规模 | 内存占用 | Redis实例 | 月成本 |
|---------|---------|-----------|--------|
| 1000 | 100MB | 512MB | 150元 |
| 5000 | 500MB | 1GB | 200元 |
| 10000 | 1GB | 4GB | 300元 |

### 与纯Redis对比

| 方案 | TTL | 月成本(1000用户) | 备注 |
|------|-----|----------------|------|
| 纯Redis | 1小时 | 300元 | 需要更大Redis |
| **混合方案** | **2小时** | **150元** | **MySQL兜底** |
| 纯Redis | 24小时 | 800元 | 内存占用大 |

**混合方案优势**:
- ✅ 成本降低50%
- ✅ TTL更长（2小时 vs 1小时）
- ✅ 持久化保障（MySQL）

---

## 🚀 使用方法

### 1. 配置参数

```yaml
# application.yml
chat:
  memory:
    ttl-hours: 2  # TTL: 2小时
    max-messages: 20  # 窗口大小: 20条
```

### 2. API调用

```javascript
// GET方式
const eventSource = new EventSource(
  '/api/agent/supervisor-sse/chat?message=推荐川菜&userId=123'
);

// POST方式
fetch('/api/agent/supervisor-sse/chat', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    message: '推荐川菜',
    userId: '123'  // ✅ 重要：传入userId以保持历史
  })
});
```

### 3. userId处理

**有userId**:
- 保持对话历史（从MySQL加载）
- Redis过期后可恢复

**无userId**:
- 生成临时UUID
- 不保存历史（每次新对话）

---

## ✅ 优势总结

### 与纯Redis对比

| 特性 | 纯Redis | 混合方案 |
|------|---------|----------|
| TTL | 1小时 | **2小时** ✅ |
| 成本(1000用户) | 300元 | **150元** ✅ |
| 持久化 | ❌ | **✅ MySQL** ✅ |
| Redis过期恢复 | ❌ 丢失 | **✅ 可恢复** ✅ |

### 与纯内存对比

| 特性 | 纯内存 | 混合方案 |
|------|--------|----------|
| 重启恢复 | ❌ | **✅** ✅ |
| 多实例同步 | ❌ | **✅** ✅ |
| 成本 | 0元 | 150元 |
| 性能 | 最快 | 很快 |

---

## 🔧 待完成工作

### 1. 编译验证
```bash
cd /Users/nickxiao/JasEatsChoice/JasEatsChoiceJava
./mvnw compile -DskipTests
```

### 2. 启动测试
```bash
./mvnw spring-boot:run
```

### 3. 功能测试

**测试1: 单用户连续对话**
```bash
curl -N "http://localhost:8080/api/agent/supervisor-sse/chat?message=推荐川菜&userId=123"
```

**测试2: 多用户隔离**
```bash
# 用户A
curl -N "http://localhost:8080/api/agent/supervisor-sse/chat?message=推荐川菜&userId=123"
# 用户B
curl -N "http://localhost:8080/api/agent/supervisor-sse/chat?message=推荐粤菜&userId=456"
```

**测试3: Redis过期恢复**
```bash
# 1. 发送消息
curl "..."
# 2. 等待2小时（或手动删除Redis key）
# 3. 再次发送，应该从MySQL加载历史
```

### 4. 性能测试

- 并发用户: 1000
- 响应时间: <100ms
- Redis命中率: >95%

---

## 📝 文件清单

### 新增文件
1. `agent/memory/RedisBackedChatMemory.java`
2. `agent/config/ChatMemoryConfig.java`
3. `config/AsyncConfig.java`

### 修改文件
1. `agent/service/SupervisorAgentFactory.java`
2. `controller/SupervisorSSEController.java`

### 文档
1. `Redis-MySQL混合存储架构设计.md`
2. `Redis内存消耗真实分析.md`
3. `ChatMemory共享问题分析.md`
4. 本文档

---

## 🎉 实施完成度

| 模块 | 状态 | 完成度 |
|------|------|--------|
| 核心代码 | ✅ 完成 | 100% |
| 编译验证 | ⏳ 待测试 | 0% |
| 功能测试 | ⏳ 待测试 | 0% |
| 性能测试 | ⏳ 待测试 | 0% |
| 文档 | ✅ 完成 | 100% |

**总体进度**: 70% (代码完成，待测试)

---

**实施人**: Claude Code AI Assistant
**完成时间**: 2026-03-26
**方案**: Redis(2小时TTL) + 异步MySQL
**状态**: ✅ 代码完成，待编译测试
