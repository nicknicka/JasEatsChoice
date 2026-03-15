# Function Calling 流式接口测试指南

> **更新时间**：2026-03-14
> **版本**：v1.0.0

---

## ✅ 已完成的修改

### 1. 后端修改

**文件**：`AIStreamController.java`

#### 新增功能
- ✅ 添加工具函数定义注入（`buildToolDefinitions()`）
- ✅ 添加工具调用检测（`extractToolCalls()`）
- ✅ 添加工具函数执行（`executeToolCalls()`）
- ✅ 修改流式响应处理逻辑，支持工具调用的二次请求

#### 关键代码变更

**1. 注入工具函数定义**
```java
@Resource
private AiFunctionDefinitionsOptimized functionDefinitions;

@Resource
private AiFunctionExecutorOptimized functionExecutor;

private List<Map<String, Object>> buildToolDefinitions() {
    // 转换ToolFunction为HTTP API格式
    // 使用反射访问私有字段（因为Lombok生成的方法IDE识别有问题）
}
```

**2. 修改请求构建**
```java
private Map<String, Object> buildChatRequest(
    String userMessage,
    String userContext,
    List<Map<String, Object>> conversationHistory) {

    // 添加工具函数定义
    List<Map<String, Object>> tools = buildToolDefinitions();
    if (!tools.isEmpty()) {
        request.put("tools", tools);
    }
}
```

**3. 流式响应处理（支持Function Calling）**
```java
private void processStreamResponse(
    SseEmitter emitter,
    Map<String, Object> requestBody,
    List<Map<String, Object>> conversationHistory) {

    // 第一轮：读取流式响应
    // 检测tool_calls
    List<Map<String, Object>> toolCalls = extractToolCalls(data);

    if (hasToolCalls) {
        // 执行工具函数
        List<Map<String, Object>> updatedHistory = executeToolCalls(...);

        // 第二轮：使用工具结果再次请求AI
        processStreamResponse(emitter, followUpRequest, updatedHistory);
    }
}
```

### 2. 前端配置（无需修改）

前端已经在使用正确的接口：
- **配置**：`API_CONFIG.ai.chat = '/v1/ai/stream/chat'`
- **组件**：`AIChatFull.vue` 的 `streamResponse()` 方法
- **快速提问**：已设计好与Function Calling对齐的问题分类

---

## 🧪 测试步骤

### 步骤1：启动后端服务

```bash
cd /Users/nickxiao/JasEatsChoice/JasEatsChoiceJava
./mvnw spring-boot:run
```

确认服务启动成功，看到日志：
```
Started JasEatsChoiceJavaApplication in X.XXX seconds
```

### 步骤2：启动前端服务

```bash
cd /Users/nickxiao/JasEatsChoice/JasEatsChoiceFront
npm run dev
```

### 步骤3：测试快速提问功能

#### 测试场景1：菜品搜索
1. 打开AI聊天页面
2. 点击"快速提问"面板中的"🍽️ 菜品探索"分类
3. 点击问题："**帮我搜索一些主食菜品**"
4. **预期结果**：
   - AI流式输出："正在为您搜索主食菜品..."
   - 后端调用 `search_dishes` 工具函数
   - AI输出搜索结果（菜品列表）
   - 所有内容流式传输到前端

#### 测试场景2：营养分析
1. 点击"📊 营养分析"分类
2. 点击问题："**分析西红柿炒鸡蛋的营养成分**"
3. **预期结果**：
   - AI调用 `analyze_nutrition` 工具函数
   - 流式输出营养分析结果

#### 测试场景3：订单管理
1. 点击"🛒 订单管理"分类
2. 点击问题："**我要下单宫保鸡丁和红烧肉**"
3. **预期结果**：
   - AI首先调用 `get_dish_details` 获取菜品信息
   - 然后调用 `create_order` 创建订单
   - 流式输出订单创建结果

### 步骤4：查看后端日志

在IDE的Console中查看日志输出：

```
检测到工具函数调用，数量: 1
执行工具函数: search_dishes, 参数: {keyword=主食, category=主食}
工具函数执行结果: 找到以下菜品：...
```

---

## 🔍 调试技巧

### 1. 确认工具函数已注入

查看后端启动日志：
```
已添加6个工具函数定义
```

### 2. 查看实际发送到智谱AI的请求

在 `AIStreamController.java` 的 `buildChatRequest` 方法中添加日志：

```java
System.out.println("发送到AI的请求: " + objectMapper.writeValueAsString(request));
```

### 3. 查看流式响应中的tool_calls

在 `extractToolCalls` 方法中添加日志：

```java
System.out.println("检测到tool_calls: " + toolCalls);
```

### 4. 浏览器开发者工具

打开F12开发者工具 → Network标签 → 筛选 `/v1/ai/stream/chat`

查看：
- **Request Headers**: 确认使用SSE（`Accept: text/event-stream`）
- **Response**: 查看流式数据格式

---

## 📊 预期的数据流

### 示例：用户问"帮我搜索一些辣味的川菜"

#### 1. 前端发送请求
```javascript
POST /api/v1/ai/stream/chat
{
  "message": "帮我搜索一些辣味的川菜"
}
```

#### 2. 后端构建请求（包含tools）
```json
{
  "model": "glm-4-flash",
  "messages": [
    {"role": "system", "content": "你是佳食宜选的智能饮食助手..."},
    {"role": "user", "content": "帮我搜索一些辣味的川菜"}
  ],
  "tools": [
    {
      "type": "function",
      "function": {
        "name": "search_dishes",
        "description": "根据关键词或分类搜索菜品",
        "parameters": {
          "type": "object",
          "properties": {
            "keyword": {"type": "string", "description": "搜索关键词"},
            "category": {"type": "string", "description": "菜品分类"}
          },
          "required": ["keyword"]
        }
      }
    }
  ],
  "stream": true
}
```

#### 3. 智谱AI返回流式响应（包含tool_calls）
```
data: {"choices":[{"delta":{"tool_calls":[{"index":0,"id":"call_123","function":{"name":"search_dishes","arguments":"{\"keyword\":\"川菜\",\"category\":\"菜肴\"}"}}]}}]}

data: {"choices":[{"delta":{"tool_calls":[{"index":0,"function":{"arguments":""}}],"finish_reason":"tool_calls"}]}}

data: [DONE]
```

#### 4. 后端执行工具函数
```java
searchDishes({keyword: "川菜", category: "菜肴"})
// 返回： "找到以下菜品：1. 宫保鸡丁 - ￥28.00..."
```

#### 5. 后端再次请求AI（包含工具结果）
```json
{
  "messages": [
    {"role": "system", "content": "..."},
    {"role": "user", "content": "帮我搜索一些辣味的川菜"},
    {"role": "assistant", "content": "", "tool_calls": [...]},
    {"role": "tool", "content": "找到以下菜品：...", "tool_call_id": "call_123"}
  ]
}
```

#### 6. AI流式输出最终回复
```
data: {"choices":[{"delta":{"content":"为您"}}]}

data: {"choices":[{"delta":{"content":"找到了"}}]}

data: {"choices":[{"delta":{"content":"5道"}}]}

data: {"choices":[{"delta":{"content":"川菜..."}}]}

data: [DONE]
```

#### 7. 前端显示
```
🔧 正在执行工具函数...

为您找到了5道川菜：
🍽️ 宫保鸡丁 - ￥28.00 - ⭐4.8
🍽️ 麻婆豆腐 - ￥18.00 - ⭐4.7
...
```

---

## ⚠️ 已知问题与解决方案

### 问题1：流式传输时tool_calls可能被截断

**现象**：工具调用参数过长时，流式响应可能分多个chunk传输

**解决方案**：
- 当前实现已经累积所有chunk后再解析
- `extractToolCalls` 方法会处理完整的tool_calls对象

### 问题2：并发请求导致状态混乱

**现象**：用户快速发送多个消息时，conversationHistory可能混乱

**解决方案**：
- 前端已有防抖机制（`isLoading.value`）
- 后端每个请求都是独立的，不共享状态

### 问题3：工具函数执行超时

**现象**：工具函数执行时间过长，导致SSE超时

**解决方案**：
- 当前SSE超时设置为60秒
- 可以在`AiFunctionType`枚举中为每个函数设置超时时间

---

## ✅ 验证清单

测试完成后，请确认以下功能：

- [ ] 快速提问的4个分类都能正常展开/收起
- [ ] 点击每个问题都能触发AI对话
- [ ] 菜品搜索能够返回真实数据（来自数据库）
- [ ] 营养分析能够返回真实数据（来自营养数据库）
- [ ] 订单创建能够成功（需要先有菜品数据）
- [ ] 流式传输正常（内容逐字显示）
- [ ] 工具调用时有"🔧 正在执行工具函数..."提示
- [ ] 后端日志显示工具函数执行记录
- [ ] 智能滚动功能正常工作

---

## 🎯 下一步优化

### 1. 性能优化
- [ ] 添加工具函数结果缓存
- [ ] 优化流式传输缓冲区大小
- [ ] 添加请求并发限制

### 2. 功能增强
- [ ] 支持多轮工具调用（一个任务需要调用多个工具）
- [ ] 添加工具调用进度显示
- [ ] 支持工具调用失败后的自动重试

### 3. 用户体验
- [ ] 添加工具调用可视化（显示正在调用的工具名称）
- [ ] 添加工具执行时间统计
- [ ] 支持取消工具调用

---

## 📝 相关文档

- [Function Calling功能实现与使用指南.md](./Function_Calling功能实现与使用指南.md)
- [AiFunctionType.java](../JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/enums/AiFunctionType.java)
- [AiFunctionExecutorOptimized.java](../JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/ai/function/AiFunctionExecutorOptimized.java)

---

**文档维护者**：Claude AI Assistant
**最后更新**：2026-03-14
