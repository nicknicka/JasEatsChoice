# 智谱AI Function Calling HTTP 400错误修复总结

## 问题概述

在实现智谱AI Function Calling功能时，第二轮请求（将工具函数执行结果发送回AI）持续返回HTTP 400错误，导致AI无法基于工具函数结果生成最终回复。

## 错误表现

```
处理流式响应失败: Server returned HTTP response code: 400
for URL: https://open.bigmodel.cn/api/paas/v4/chat/completions
```

### 预期流程
1. 用户发送消息 → AI返回tool_calls → 执行工具函数 → AI基于结果生成回复 ✅
2. 实际流程：用户发送消息 → AI返回tool_calls → 执行工具函数 → **HTTP 400错误** ❌

## 根本原因分析

通过添加详细调试日志，发现了**4个关键问题**导致智谱AI API拒绝请求：

### 问题1：assistant消息包含空content字段

**错误代码：**
```java
Map<String, Object> assistantMessage = new HashMap<>();
assistantMessage.put("role", "assistant");
assistantMessage.put("content", "");  // ❌ 空字符串
assistantMessage.put("tool_calls", toolCalls);
```

**问题分析：**
根据智谱AI Function Calling规范，当assistant消息只包含tool_calls时，**不应该有content字段**（或者content应为null）。空字符串会被API拒绝。

**影响：** 导致API无法正确解析assistant消息，返回400错误。

---

### 问题2：arguments被错误地解析为Map对象

**错误代码：**
```java
// 从AI响应中提取tool_calls时
String argsStr = functionNode.get("arguments").asText();
try {
    Map<String, Object> args = objectMapper.readValue(argsStr, Map.class);
    function.put("arguments", args);  // ❌ 存储为Map对象
} catch (Exception e) {
    function.put("arguments", argsStr);
}
```

**问题分析：**
智谱AI API规范要求，**arguments必须是字符串形式的JSON**，例如：
```json
"arguments": "{\"category\":\"甜点\",\"keyword\":\"\"}"
```

如果发送Map对象，JSON序列化后会变成：
```json
"arguments": {"category":"甜点","keyword":""}
```

这不符合API规范，导致400错误。

**影响：** 第二轮请求的tool_calls格式不符合规范。

---

### 问题3：arguments字符串无法转换为Map（执行函数时）

**错误代码：**
```java
// 执行工具函数时
Map<String, Object> arguments = (Map<String, Object>) function.get("arguments");  // ❌ 类型转换异常
String result = functionExecutor.executeFunction(functionName, arguments, userId);
```

**问题分析：**
修复问题2后，arguments变成字符串。但执行工具函数时需要Map参数，直接强制转换会抛出`ClassCastException`：

```
class java.lang.String cannot be cast to class java.util.Map
```

**影响：** 工具函数无法执行。

---

### 问题4：tool_call对象缺少type字段

**错误代码：**
```java
// 提取tool_call时
Map<String, Object> toolCall = new HashMap<>();
if (toolCallNode.has("id")) {
    toolCall.put("id", toolCallNode.get("id").asText());
}
// ❌ 缺少type字段
if (toolCallNode.has("function")) {
    toolCall.put("function", function);
}
```

**问题分析：**
根据智谱AI Function Calling规范（兼容OpenAI标准），每个tool_call对象必须包含：
- `id`: string - 工具调用的唯一标识
- `type`: "function" - **必需字段，标识类型**
- `function`: object - 函数详情

缺少type字段会导致API拒绝请求。

**影响：** tool_calls结构不完整。

---

## 解决方案

### 修复1：移除assistant消息的空content字段

```java
// ✅ 修复后
Map<String, Object> assistantMessage = new HashMap<>();
assistantMessage.put("role", "assistant");
assistantMessage.put("tool_calls", toolCalls);
// 不添加content字段，让其为null（符合智谱AI规范）
updatedHistory.add(assistantMessage);
```

**文件：** `AIStreamController.java` 第416-420行

---

### 修复2：arguments保持字符串格式

```java
// ✅ 修复后
if (functionNode.has("arguments")) {
    // arguments必须保持为字符串形式的JSON（符合智谱AI规范）
    String argsStr = functionNode.get("arguments").asText();
    function.put("arguments", argsStr);  // 保持字符串形式
}
```

**文件：** `AIStreamController.java` 第504-508行

---

### 修复3：智能解析arguments字符串

```java
// ✅ 修复后：智能判断类型
Object argumentsObj = function.get("arguments");
Map<String, Object> arguments;

if (argumentsObj instanceof String) {
    // 字符串形式的JSON，需要解析
    try {
        arguments = objectMapper.readValue((String) argumentsObj, Map.class);
    } catch (Exception e) {
        System.err.println("解析arguments失败: " + argumentsObj);
        arguments = new HashMap<>();
    }
} else if (argumentsObj instanceof Map) {
    // 已经是Map对象（兼容性处理）
    arguments = (Map<String, Object>) argumentsObj;
} else {
    arguments = new HashMap<>();
}
```

**文件：** `AIStreamController.java` 第431-448行

---

### 修复4：添加type字段

```java
// ✅ 修复后
Map<String, Object> toolCall = new HashMap<>();

// 提取id
if (toolCallNode.has("id")) {
    toolCall.put("id", toolCallNode.get("id").asText());
}

// 提取type（重要：智谱AI规范要求）
if (toolCallNode.has("type")) {
    toolCall.put("type", toolCallNode.get("type").asText());
} else {
    // 如果没有type字段，默认设置为"function"
    toolCall.put("type", "function");
}

// 提取function信息
if (toolCallNode.has("function")) {
    toolCall.put("function", function);
}
```

**文件：** `AIStreamController.java` 第482-511行

---

## 验证结果

### 修复前的日志（失败）
```
=== 发送给智谱AI的消息列表 ===
消息数量: 4
[2] role: assistant
    tool_calls: [{function={name=search_dishes, arguments={category=菜肴, keyword=鸡肉}}, id=call_xxx}]
    ❌ arguments是Map对象，缺少type字段

处理流式响应失败: Server returned HTTP response code: 400
```

### 修复后的日志（成功）
```
=== 发送给智谱AI的消息列表 ===
消息数量: 4
[2] role: assistant
    tool_calls: [{function={name=search_dishes, arguments={"category": "菜肴", "keyword": "鸡肉"}}, id=call_xxx, type=function}]
    ✅ arguments是字符串，包含type字段

执行工具函数: search_dishes, 参数: {category=菜肴, keyword=鸡肉}, 用户ID: xxx
工具函数执行结果: 抱歉，没有找到相关的菜品...

发送完成标记  ✅
```

---

## 技术要点总结

### 1. 智谱AI Function Calling规范

**消息序列：**
```
1. system消息（系统提示词）
2. user消息（用户输入）
3. assistant消息（包含tool_calls，无content字段）
4. tool消息（包含tool_call_id和执行结果）
5. assistant消息（AI的最终回复）
```

**tool_calls结构：**
```json
{
  "role": "assistant",
  "tool_calls": [
    {
      "id": "call_xxx",
      "type": "function",
      "function": {
        "name": "search_dishes",
        "arguments": "{\"category\":\"菜肴\",\"keyword\":\"鸡肉\"}"
      }
    }
  ]
}
```

**tool消息结构：**
```json
{
  "role": "tool",
  "tool_call_id": "call_xxx",
  "content": "执行结果字符串"
}
```

### 2. 调试技巧

添加详细的调试日志可以快速定位问题：

```java
// 打印完整的消息列表
System.out.println("=== 发送给智谱AI的消息列表 ===");
for (int i = 0; i < messages.size(); i++) {
    System.out.println("[" + i + "] role: " + role);
    System.out.println("    content: " + content);
    System.out.println("    tool_calls: " + toolCalls);
    System.out.println("    tool_call_id: " + toolCallId);
}
```

### 3. 类型安全处理

使用`instanceof`进行类型检查，避免类型转换异常：

```java
if (obj instanceof String) {
    // 处理字符串
} else if (obj instanceof Map) {
    // 处理Map
} else {
    // 默认处理
}
```

---

## 相关文件

- **主要修改文件：**
  - `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/controller/AIStreamController.java`

- **相关依赖：**
  - `AiFunctionDefinitionsOptimized.java` - 工具函数定义
  - `AiFunctionExecutorOptimized.java` - 工具函数执行器

---

## 参考资料

- [智谱AI开放平台API文档](https://open.bigmodel.cn/dev/api)
- [OpenAI Function Calling规范](https://platform.openai.com/docs/guides/function-calling)

---

## 修复时间线

1. **问题发现** - 2026-03-15 14:08
2. **分析阶段** - 添加调试日志，分析消息结构
3. **修复阶段** - 逐个修复4个关键问题
4. **验证阶段** - 2026-03-15 14:24 验证成功

---

## 经验教训

1. **严格遵循API规范** - 字段类型、结构必须完全符合文档要求
2. **详细日志至关重要** - Function Calling的调试需要完整的请求/响应日志
3. **类型安全** - JSON序列化/反序列化时要特别注意类型转换
4. **完整性检查** - 确保所有必需字段都已正确设置

---

**修复完成日期：** 2026-03-15
**修复状态：** ✅ 完全解决
**测试状态：** ✅ 通过
