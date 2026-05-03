# AI调度传参机制详解

## 📋 概述

佳食宜选AI系统使用 **LangChain4j** 框架，实现了三层传参机制：
1. **Controller → Agent**: 用户消息和userId传递
2. **Agent → Tools**: LLM自动解析并调用工具，传递工具参数
3. **Tools Methods**: 工具方法参数定义和接收

---

## 🔄 第一层：Controller → Agent 传参

### 1.1 请求入口

**文件**: `AIStreamController.java`

```java
@PostMapping("/chat")
public SseEmitter streamChat(@RequestBody Map<String, Object> params) {
    // 1. 提取参数
    String message = (String) params.get("message");
    String userId = (String) params.getOrDefault("userId", "anonymous");

    // 2. 调用Agent，传递两个参数
    streamingIntelligentAssistantAgent.chat(message, userId)
        .onPartialResponse(token -> { /* ... */ })
        .onCompleteResponse(response -> { /* ... */ })
        .start();
}
```

**传递参数**:
- `message`: 用户消息文本
- `userId`: 用户ID（用于识别用户）

---

### 1.2 Agent接口定义

**文件**: `StreamingIntelligentAssistantAgent.java`

```java
public interface StreamingIntelligentAssistantAgent {

    @SystemMessage("""
        你是"佳食宜选"的智能助手，是L3级别的智能调度Agent。

        # 重要：用户ID识别
        当前对话的用户ID是：{{userId}}

        ⚠️ 严格要求：
        - 在查询用户信息、订单、偏好时，必须且只能使用上述用户ID：{{userId}}
        - 绝对不要编造、修改或使用示例用户ID（如12345、111等）
        """)
    TokenStream chat(
        @UserMessage String userMessage,  // 用户消息
        @V("userId") String userId         // 用户ID（注入到System Prompt）
    );
}
```

**关键注解**:
- `@UserMessage`: 标注用户消息参数
- `@V("userId")`: 标注变量参数，会注入到System Prompt的 `{{userId}}` 占位符

**传参流程**:
```
Controller传入: message="推荐一些菜品", userId="12345"
    ↓
Agent接口接收: userMessage="推荐一些菜品", userId="12345"
    ↓
System Prompt替换: {{userId}} → "12345"
    ↓
LLM收到完整的System Prompt，知道当前用户ID是"12345"
```

---

## 🔄 第二层：Agent → Tools 传参

### 2.1 工具方法定义

**文件**: `RecommendationQueryTools.java`

```java
@Tool("""
    根据用户偏好查询推荐菜品

    **推荐因素：**
    - 用户饮食目标
    - 口味偏好
    - 过敏信息
    - 菜品评分

    **何时使用：**
    - 用户要求推荐
    - 菜品搜索
    - 个性化建议

    **参数：**
    - userId - 用户ID（用于个性化）
    - category - 分类（可选，如：主食、汤羹、小吃）

    **返回：** 推荐菜品列表（文本格式）
    """)
public String queryRecommendations(
    @P("用户ID") String userId,
    @P("分类（可选）") String category
) {
    // 工具实现
}
```

**关键注解**:
- `@Tool(...)`: 工具描述，告诉LLM这个工具的作用、使用场景、参数说明
- `@P("...")`: 参数描述，告诉LLM每个参数的含义

---

### 2.2 LLM自动调用工具

**工作流程**:

```
步骤1: 用户发送消息
    "推荐一些低卡路里的菜品"
    ↓
步骤2: Agent接收消息
    userMessage = "推荐一些低卡路里的菜品"
    userId = "12345"
    ↓
步骤3: LLM分析意图
    分析 @Tool 注解，决定调用 RecommendationQueryTools.queryRecommendations()
    ↓
步骤4: LLM生成工具参数
    从对话中提取参数值：
    - userId = "12345" (从 @V("userId") 获取)
    - category = "低卡路里" (从用户消息中推断)
    ↓
步骤5: LangChain4j自动调用工具
    RecommendationQueryTools.queryRecommendations("12345", "低卡路里")
    ↓
步骤6: 工具执行并返回结果
    "🌟 为您推荐的菜品\n\n1. **蔬菜沙拉**\n   💰 15.00元..."
    ↓
步骤7: LLM基于工具结果生成回复
    "根据您的要求，我为您推荐以下低卡路里菜品：..."
```

---

### 2.3 参数类型和格式

#### 基本类型参数

```java
@Tool("查询用户订单")
public String getUserOrders(
    @P("用户ID") String userId,
    @P("返回数量") Integer limit  // 基本类型
) {
    // userId: "12345"
    // limit: 10
}
```

**LLM传递**:
```json
{
    "userId": "12345",
    "limit": 10
}
```

---

#### JSON字符串参数

```java
@Tool("创建订单")
public String createOrder(
    @P("用户ID") String userId,
    @P("商家ID") String merchantId,
    @P("菜品列表（JSON数组字符串）") String dishItemsJson,
    @P("就餐方式") String diningMode
) {
    // dishItemsJson: [{"dishId":"D001","quantity":2,"price":15.5}]
}
```

**LLM传递**:
```json
{
    "userId": "12345",
    "merchantId": "M001",
    "dishItemsJson": "[{\"dishId\":\"D001\",\"quantity\":2,\"price\":15.5}]",
    "diningMode": "dine_in"
}
```

**工具内部解析**:
```java
List<Map<String, Object>> dishItems = objectMapper.readValue(
    dishItemsJson,
    new TypeReference<List<Map<String, Object>>>() {}
);
```

---

#### 可选参数

```java
@Tool("创建订单")
public String createOrder(
    @P("用户ID") String userId,
    @P("商家ID") String merchantId,
    @P("菜品列表") String dishItemsJson,
    @P("就餐方式") String diningMode,
    @P(value = "座号（可选）", required = false) String tableNumber,  // 可选
    @P(value = "备注（可选）", required = false) String note             // 可选
) {
    // tableNumber 和 note 可以为null
    if (tableNumber != null) {
        // 处理座号
    }
}
```

**LLM传递**:
```json
{
    "userId": "12345",
    "merchantId": "M001",
    "dishItemsJson": "[...]",
    "diningMode": "dine_in",
    "tableNumber": "A12",  // 可选，LLM可能传递
    "note": null           // 可选，LLM可能不传递
}
```

---

#### 数组/List参数

```java
@Tool("分析多个食物的营养")
public String analyzeMultipleFoods(
    @P("食物名称列表") List<String> foodNames
) {
    // foodNames: ["番茄炒蛋", "宫保鸡丁", "麻婆豆腐"]
}
```

**LLM传递**:
```json
{
    "foodNames": ["番茄炒蛋", "宫保鸡丁", "麻婆豆腐"]
}
```

---

## 🎯 第三层：System Prompt 变量注入

### 3.1 变量定义语法

**StreamingIntelligentAssistantAgent.java**:

```java
@SystemMessage("""
    你是"佳食宜选"的智能助手。

    # 重要：用户ID识别
    当前对话的用户ID是：{{userId}}

    ⚠️ 严格要求：
    - 在查询用户信息时，必须且只能使用上述用户ID：{{userId}}
    """)
TokenStream chat(
    @UserMessage String userMessage,
    @V("userId") String userId  // 注入到 {{userId}}
);
```

---

### 3.2 变量替换流程

```
原始System Prompt:
    """
    当前对话的用户ID是：{{userId}}
    - 在查询用户信息时，必须且只能使用上述用户ID：{{userId}}
    """

调用时传入: userId = "12345"

替换后的System Prompt:
    """
    当前对话的用户ID是：12345
    - 在查询用户信息时，必须且只能使用上述用户ID：12345
    """

LLM收到的最终Prompt:
    System Prompt: "当前对话的用户ID是：12345..."
    User Message: "我的订单有哪些？"
```

---

### 3.3 多变量注入示例

```java
@SystemMessage("""
    你是智能助手。

    当前用户ID：{{userId}}
    当前时间：{{currentTime}}
    用户位置：{{location}}
    """)
TokenStream chat(
    @UserMessage String userMessage,
    @V("userId") String userId,
    @V("currentTime") String currentTime,
    @V("location") String location
);
```

**调用时**:
```java
agent.chat(
    "推荐附近的餐厅",
    "12345",           // userId
    "2026-04-02 12:00", // currentTime
    "北京市朝阳区"      // location
);
```

**LLM收到**:
```
System Prompt:
    你是智能助手。

    当前用户ID：12345
    当前时间：2026-04-02 12:00
    用户位置：北京市朝阳区

User Message:
    推荐附近的餐厅
```

---

## 📊 完整传链示例

### 场景：用户查询订单

#### 步骤1: 前端发送请求

```javascript
fetch('/v1/ai/stream/chat', {
    method: 'POST',
    body: JSON.stringify({
        message: "我的订单有哪些？",
        userId: "12345"
    })
});
```

---

#### 步骤2: Controller接收并传参

**AIStreamController.java**:

```java
@PostMapping("/chat")
public SseEmitter streamChat(@RequestBody Map<String, Object> params) {
    String message = (String) params.get("message");  // "我的订单有哪些？"
    String userId = (String) params.get("userId");    // "12345"

    // 传递给Agent
    streamingIntelligentAssistantAgent.chat(message, userId)
        .onPartialResponse(token -> { /* ... */ })
        .start();
}
```

---

#### 步骤3: Agent接收并构建Prompt

**StreamingIntelligentAssistantAgent.java**:

```java
@SystemMessage("""
    你是"佳食宜选"的智能助手。

    当前对话的用户ID是：{{userId}}

    ⚠️ 严格要求：
    - 在查询用户信息、订单、偏好时，必须且只能使用上述用户ID：{{userId}}
    """)
TokenStream chat(
    @UserMessage String userMessage,  // "我的订单有哪些？"
    @V("userId") String userId         // "12345"
);
```

**LLM实际收到**:
```
System Prompt:
    你是"佳食宜选"的智能助手。

    当前对话的用户ID是：12345

    ⚠️ 严格要求：
    - 在查询用户信息、订单、偏好时，必须且只能使用上述用户ID：12345

User Message:
    我的订单有哪些？
```

---

#### 步骤4: LLM决定调用工具

**LLM分析**:
- 用户意图：查询订单
- 应该调用工具：`OrderQueryTools.getUserOrders()`
- 需要参数：`userId = "12345"`

---

#### 步骤5: LangChain4j自动调用工具

**OrderQueryTools.java**:

```java
@Tool("""
    查询用户的所有订单

    **何时使用：**
    - 用户查询订单
    - 订单状态跟踪

    **参数：**
    - userId - 用户ID

    **返回：** 订单列表（文本格式）
    """)
public String getUserOrders(
    @P("用户ID") String userId
) {
    // userId = "12345"
    log.info("🔍 [Tool] 查询用户订单，userId: {}", userId);

    // 查询订单
    List<Order> orders = orderService.list(
        new QueryWrapper<Order>().eq("user_id", userId)
    );

    // 返回结果
    return formatOrders(orders);
}
```

**LangChain4j自动传递参数**:
```java
OrderQueryTools.getUserOrders("12345");
```

---

#### 步骤6: 工具执行并返回

```java
return """
    📋 您的订单列表

    1. **订单号：O2026040212345**
       状态：已完成
       金额：￥35.50
       时间：2026-04-01 12:30

    2. **订单号：O2026040198765**
       状态：配送中
       金额：￥28.00
       时间：2026-04-02 11:15
    """;
```

---

#### 步骤7: LLM基于工具结果生成回复

```
LLM收到工具结果后，生成最终回复：
    "您目前有2个订单：

     1. 订单号 O2026040212345 - 已完成，金额￥35.50
     2. 订单号 O2026040198765 - 配送中，金额￥28.00

     需要查看某个订单的详情吗？"
```

---

## 🔧 高级传参技巧

### 1. 参数验证

```java
@Tool("创建订单")
public String createOrder(
    @P("用户ID") String userId,
    @P("商家ID") String merchantId,
    @P("菜品列表") String dishItemsJson
) {
    // 验证必需参数
    if (userId == null || userId.isEmpty()) {
        return "❌ 缺少用户ID（userId）";
    }
    if (merchantId == null || merchantId.isEmpty()) {
        return "❌ 缺少商家ID（merchantId）";
    }
    if (dishItemsJson == null || dishItemsJson.isEmpty()) {
        return "❌ 菜品列表不能为空";
    }

    // 参数格式验证
    try {
        List<Map<String, Object>> dishItems = objectMapper.readValue(
            dishItemsJson,
            new TypeReference<List<Map<String, Object>>>() {}
        );
    } catch (Exception e) {
        return "❌ 菜品列表格式错误，正确格式：[{\"dishId\":\"xxx\",\"quantity\":1}]";
    }

    // 继续处理...
}
```

---

### 2. 参数默认值

```java
@Tool("查询热门菜品")
public String getHotDishes(
    @P("返回数量（默认10）") Integer limit,
    @P("分类（可选）") String category
) {
    // 设置默认值
    int actualLimit = limit != null && limit > 0 ? limit : 10;

    // 使用默认值查询
    List<Dish> dishes = dishService.list(
        new QueryWrapper<Dish>()
            .eq(Dish::getIsOnline, true)
            .last("LIMIT " + actualLimit)
    );

    return formatDishes(dishes);
}
```

---

### 3. 参数类型转换

```java
@Tool("计算BMI")
public String calculateBMI(
    @P("体重（公斤）") Double weight,
    @P("身高（厘米）") Double height
) {
    // 参数类型转换
    double weightKg = weight != null ? weight : 0.0;
    double heightM = (height != null ? height : 0.0) / 100.0;

    // 计算BMI
    double bmi = weightKg / (heightM * heightM);

    return String.format("您的BMI指数为：%.1f", bmi);
}
```

---

### 4. 复杂对象参数

```java
@Tool("更新用户资料")
public String updateUserProfile(
    @P("用户ID") String userId,
    @P("用户资料JSON") String profileJson
) {
    try {
        // 解析复杂对象
        Map<String, Object> profile = objectMapper.readValue(
            profileJson,
            new TypeReference<Map<String, Object>>() {}
        );

        // 提取字段
        String nickname = (String) profile.get("nickname");
        Integer height = (Integer) profile.get("height");
        Integer weight = (Integer) profile.get("weight");

        // 更新用户资料
        User user = userService.getById(userId);
        user.setNickname(nickname);
        user.setHeight(height);
        user.setWeight(weight);
        userService.updateById(user);

        return "✅ 用户资料更新成功";

    } catch (Exception e) {
        return "❌ 用户资料格式错误";
    }
}
```

**LLM传递**:
```json
{
    "userId": "12345",
    "profileJson": "{\"nickname\":\"张三\",\"height\":175,\"weight\":70}"
}
```

---

## 📝 参数定义最佳实践

### 1. 清晰的参数描述

```java
// ✅ 好的做法
@P("用户ID（必填，用于识别用户）") String userId

// ❌ 不好的做法
@P("id") String userId
```

---

### 2. 明确参数格式

```java
// ✅ 好的做法
@P("菜品列表（JSON数组字符串格式，示例：[{\"dishId\":\"D001\",\"quantity\":2}]）")
String dishItemsJson

// ❌ 不好的做法
@P("菜品列表") String dishItemsJson
```

---

### 3. 标注可选参数

```java
// ✅ 好的做法
@P(value = "座号（可选，堂食时建议填写）", required = false) String tableNumber

// ❌ 不好的做法
@P("座号") String tableNumber
```

---

### 4. 提供示例值

```java
// ✅ 好的做法
@Tool("""
    创建订单

    **参数示例：**
    - userId: "12345"
    - merchantId: "M001"
    - dishItemsJson: [{"dishId":"D001","quantity":2,"price":15.5}]
    - diningMode: "dine_in" 或 "takeout"
    """)
```

---

## 🎓 总结

佳食宜选AI系统的传参机制：

```
┌─────────────────────────────────────────────────┐
│  第一层：Controller → Agent                      │
│  - @UserMessage: 用户消息                        │
│  - @V("var"): 变量注入到System Prompt            │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│  第二层：Agent → Tools                          │
│  - LLM自动分析意图                               │
│  - LLM自动选择工具                               │
│  - LLM自动提取参数值                             │
│  - LangChain4j自动调用工具                       │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│  第三层：Tools Methods                          │
│  - @Tool: 工具描述                              │
│  - @P: 参数描述                                  │
│  - required: 是否必需                            │
└─────────────────────────────────────────────────┘
```

**核心优势**:
- ✅ 自动化：LLM自动理解意图、选择工具、提取参数
- ✅ 类型安全：工具方法使用强类型参数
- ✅ 灵活性：支持基本类型、JSON、数组等多种参数类型
- ✅ 可扩展：添加新工具只需定义@Tool方法

**文件位置**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/`
