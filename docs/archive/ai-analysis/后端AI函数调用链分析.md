# 后端AI部分函数调用链分析

## 📋 概述

佳食宜选后端AI系统基于 **LangChain4j** 框架实现，采用 **L3智能调度Agent** 架构，提供流式响应和工具调用能力。

---

## 🏗️ 架构层次

```
┌─────────────────────────────────────────────────────┐
│           前端 (Electron / UniApp)                  │
└─────────────────────────────────────────────────────┘
                         ↓ SSE/HTTP
┌─────────────────────────────────────────────────────┐
│         Controller层 (AIStreamController)           │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│      Agent层 (StreamingIntelligentAssistantAgent)   │
│              L3智能调度Agent                         │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│           工具层 (Tools) - 业务逻辑                  │
├─────────────────────────────────────────────────────┤
│ • RecommendationQueryTools   (菜品推荐)             │
│ • NutritionAnalysisTools     (营养分析)             │
│ • OrderQueryTools            (订单查询)             │
│ • OrderCreateTools           (订单创建)             │
│ • UserProfileTools           (用户资料)             │
│ • MerchantQueryTools         (商家查询)             │
│ • TimeTools                  (时间服务)             │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│         Service层 (业务服务层)                       │
├─────────────────────────────────────────────────────┤
│ • DishService      • OrderService                  │
│ • UserService      • MerchantService               │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│         Mapper层 (MyBatis-Plus)                     │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│         数据库 (MySQL)                               │
└─────────────────────────────────────────────────────┘
```

---

## 🔄 完整调用链

### 1️⃣ 用户发起对话请求

**前端** → **后端**

```
前端发送SSE请求:
POST /v1/ai/stream/chat
{
  "message": "用户消息",
  "userId": "用户ID"
}
```

**文件**: `AIStreamController.java:54`

---

### 2️⃣ 控制器接收请求

**AIStreamController** 处理SSE流式请求

```java
@PostMapping("/chat")
public SseEmitter streamChat(@RequestBody Map<String, Object> params)
```

**关键步骤**:
1. 创建 `SseEmitter` (5分钟超时)
2. 提取 `message` 和 `userId` 参数
3. 调用 `StreamingIntelligentAssistantAgent.chat()`
4. 通过SSE流式返回响应

**文件**: `AIStreamController.java:33-186`

---

### 3️⃣ Agent层处理

**StreamingIntelligentAssistantAgent** (L3智能调度Agent)

```java
TokenStream chat(
    @UserMessage String userMessage,
    @V("userId") String userId
)
```

**System Prompt配置**:
- 角色: "佳食宜选"智能助手
- 职责: 理解用户需求，智能调度专业工具
- 可用工具: 10个工具类（推荐、营养、订单、用户等）

**文件**: `StreamingIntelligentAssistantAgent.java:17-93`

---

### 4️⃣ LangChain4j流式处理

**流程**:

```
AIStreamController.chat()
    ↓
streamingIntelligentAssistantAgent.chat(message, userId)
    ↓
TokenStream.onPartialResponse(token)  // 每个token流式回调
    ↓
SseEmitter.send("message", {char: token})
    ↓
TokenStream.onCompleteResponse(response)  // 完成时回调
    ↓
检查工具执行 → 生成卡片数据 → 发送完成事件
```

**关键代码**: `AIStreamController.java:81-178`

---

### 5️⃣ 工具调用机制

#### 5.1 工具类注册

在 `LangChain4jStreamingConfig` 中配置:

```java
@Bean
public StreamingIntelligentAssistantAgent streamingIntelligentAssistantAgent(
    StreamingChatModel streamingChatLanguageModel,
    ChatMemory streamingChatMemory
) {
    return AiServices.builder(StreamingIntelligentAssistantAgent.class)
        .streamingChatModel(streamingChatLanguageModel)
        .chatMemory(streamingChatMemory)
        .tools(
            userProfileTools,
            recommendationQueryTools,
            recommendationFilterTools,
            recommendationRankTools,
            nutritionAnalysisTools,
            calorieCalculatorTools,
            orderQueryTools,
            orderCreateTools,
            merchantQueryTools,
            merchantStatsTools,
            timeTools
        )
        .build();
}
```

**文件**: `LangChain4jStreamingConfig.java:135-158`

---

#### 5.2 工具方法定义

**示例**: `RecommendationQueryTools.queryRecommendations()`

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
    """)
public String queryRecommendations(
    @P("用户ID") String userId,
    @P("分类（可选）") String category
) {
    // 1. 查询用户信息
    User user = userService.getById(userId);

    // 2. 构建查询条件
    LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<Dish>()
        .eq(Dish::getIsOnline, true);

    if (category != null && !category.isEmpty()) {
        queryWrapper.eq(Dish::getCategory, category);
    }

    // 3. 查询菜品列表
    List<Dish> dishes = dishService.list(queryWrapper);

    // 4. 格式化返回结果
    return formatDishList(dishes);
}
```

**文件**: `RecommendationQueryTools.java:43-100`

---

### 6️⃣ AOP切面拦截 (卡片生成)

#### 6.1 @CardType 注解

标注需要生成卡片的工具方法:

```java
@CardType("food_recommendation_card")
public String queryNearbyRecommendations(...)
```

#### 6.2 CardTypeAspect 拦截

```java
@Around("@annotation(com.xx.jaseatschoicejava.agent.annotation.CardType)")
public Object aroundCardTypeMethod(ProceedingJoinPoint joinPoint) {
    // 1. 获取注解信息
    CardType cardType = ...;

    // 2. 记录工具执行开始
    ToolExecutionContext.startExecution(toolName, cardTypeValue, parameters);

    // 3. 执行工具方法
    Object result = joinPoint.proceed();

    // 4. 记录工具执行结束（保存结果）
    ToolExecutionContext.endExecution(result);

    return result;
}
```

**文件**: `CardTypeAspect.java:23-76`

---

### 7️⃣ 卡片数据生成

**流程**:

```
TokenStream.onCompleteResponse(response)
    ↓
ToolExecutionContext.getCardExecutions()  // 获取工具执行信息
    ↓
buildCardData(cardType, userId, executionInfo)
    ↓
根据cardType构建不同卡片:
    - order_list_card      → 订单列表卡片
    - user_info_card       → 用户信息卡片
    - order_guide_card     → 下单引导卡片
    ↓
SseEmitter.send("message", {card_data: {...}})
```

**关键代码**: `AIStreamController.java:122-148`

---

## 📊 关键数据流

### 用户消息流

```
用户输入: "推荐一些低卡路里的菜品"
    ↓
AIStreamController接收请求
    ↓
StreamingIntelligentAssistantAgent分析意图
    ↓
LLM决定调用: RecommendationQueryTools.queryRecommendations()
    ↓
工具执行: 查询数据库 → 返回菜品列表
    ↓
LLM生成回复: "为您推荐以下低卡菜品..."
    ↓
流式输出每个token
    ↓
前端实时显示
```

### 卡片数据流

```
工具执行完成
    ↓
CardTypeAspect拦截 → 保存到ToolExecutionContext
    ↓
TokenStream完成 → 触发onCompleteResponse
    ↓
从ToolExecutionContext获取工具执行信息
    ↓
buildCardData() → 构建前端需要的卡片数据结构
    ↓
SSE发送: {card_data: {messageType, data}}
    ↓
前端解析 → 渲染卡片组件
```

---

## 🔧 核心组件说明

### 1. AIStreamController

**职责**: SSE流式响应控制器

**关键方法**:
- `streamChat()`: 处理SSE聊天请求
- `buildCardData()`: 根据卡片类型构建卡片数据
- `buildOrderListCardData()`: 构建订单列表卡片
- `buildUserInfoCardData()`: 构建用户信息卡片
- `buildOrderGuideCardData()`: 构建下单引导卡片

**文件**: `AIStreamController.java`

---

### 2. StreamingIntelligentAssistantAgent

**职责**: L3智能调度Agent接口

**配置**:
- SystemMessage: 定义角色、职责、可用工具
- 支持流式输出 (TokenStream)
- 工具自动调用 (通过@Tool注解)

**文件**: `StreamingIntelligentAssistantAgent.java`

---

### 3. LangChain4jStreamingConfig

**职责**: Agent配置工厂

**关键Bean**:
- `streamingChatLanguageModel`: 智谱AI流式模型
- `streamingChatMemory`: 对话记忆 (20条消息)
- `streamingIntelligentAssistantAgent`: Agent实例

**文件**: `LangChain4jStreamingConfig.java`

---

### 4. 工具类 (Tools)

**职责**: 业务逻辑封装

**主要工具类**:

| 工具类 | 职责 | 关键方法 |
|--------|------|----------|
| **RecommendationQueryTools** | 菜品推荐 | queryRecommendations, getPersonalizedRecommendations |
| **NutritionAnalysisTools** | 营养分析 | analyzeNutrition, calculateCalories |
| **OrderQueryTools** | 订单查询 | getOrderDetail, getUserOrders |
| **OrderCreateTools** | 订单创建 | createOrder, validateOrder |
| **UserProfileTools** | 用户资料 | getUserProfile, updateUserProfile |
| **MerchantQueryTools** | 商家查询 | getMerchantInfo, searchMerchants |
| **TimeTools** | 时间服务 | getCurrentTime, isOpenNow |

---

### 5. CardTypeAspect

**职责**: AOP切面，拦截带@CardType注解的方法

**功能**:
1. 拦截工具方法执行
2. 记录方法名、参数、结果
3. 保存到 `ToolExecutionContext` (ThreadLocal)

**文件**: `CardTypeAspect.java`

---

### 6. ToolExecutionContext

**职责**: ThreadLocal上下文，存储工具执行信息

**数据结构**:
```java
class ToolExecutionInfo {
    String toolName;       // 工具名称
    String cardType;       // 卡片类型
    Map<String, Object> parameters;  // 方法参数
    Object result;         // 执行结果
}
```

**文件**: `ToolExecutionContext.java`

---

## 🎯 典型场景调用链

### 场景1: 用户查询订单

```
用户: "我的订单"
    ↓
AIStreamController.streamChat()
    ↓
StreamingIntelligentAssistantAgent.chat()
    ↓
LLM分析意图 → 调用 OrderQueryTools.getUserOrders(userId)
    ↓
OrderQueryTools执行 → 查询数据库 → 返回订单列表
    ↓
LLM生成回复 → 流式输出: "您有以下订单..."
    ↓
同时检测到@CardType注解 → 生成order_list_card
    ↓
SSE发送卡片数据 → 前端渲染订单卡片
```

---

### 场景2: 用户要求推荐菜品

```
用户: "推荐一些低卡路里的菜品"
    ↓
AIStreamController.streamChat()
    ↓
StreamingIntelligentAssistantAgent.chat()
    ↓
LLM分析意图 → 调用 RecommendationQueryTools.queryRecommendations(userId, "低卡")
    ↓
RecommendationQueryTools执行:
    1. 查询用户信息 (UserService)
    2. 查询菜品列表 (DishService)
    3. 过滤低卡菜品
    4. 返回推荐列表
    ↓
LLM生成回复 → 流式输出: "为您推荐以下低卡菜品..."
    ↓
同时检测到@CardType注解 → 生成food_recommendation_card
    ↓
SSE发送卡片数据 → 前端渲染推荐卡片
```

---

### 场景3: 用户创建订单

```
用户: "我要一份宫保鸡丁和一份麻婆豆腐"
    ↓
AIStreamController.streamChat()
    ↓
StreamingIntelligentAssistantAgent.chat()
    ↓
LLM分析意图 → 调用 OrderCreateTools.createOrder(...)
    ↓
OrderCreateTools执行:
    1. 解析菜品名称和数量
    2. 查询菜品信息 (DishService)
    3. 计算总价
    4. 创建订单 (OrderService)
    ↓
LLM生成回复 → 流式输出: "订单创建成功！"
    ↓
同时检测到@CardType注解 → 生成order_guide_card
    ↓
SSE发送卡片数据 → 前端渲染下单引导卡片
```

---

## 🔑 关键技术点

### 1. 流式响应 (SSE)

使用 `SseEmitter` 实现服务端推送:

```java
SseEmitter emitter = new SseEmitter(300000L);  // 5分钟超时

streamingAgent.chat(message)
    .onPartialResponse(token -> {
        emitter.send(SseEmitter.event().name("message").data(token));
    })
    .onCompleteResponse(response -> {
        emitter.send(SseEmitter.event().name("end").data(Map.of("done", true)));
        emitter.complete();
    })
    .start();
```

---

### 2. 工具调用 (@Tool注解)

LangChain4j自动识别工具:

```java
@Tool("工具描述")
public String methodName(@P("参数描述") String param) {
    // 工具逻辑
}
```

---

### 3. AOP卡片生成

通过AOP拦截工具执行，记录信息用于卡片生成:

```java
@CardType("order_list_card")
public String getUserOrders(String userId) {
    // 工具执行后，CardTypeAspect会保存执行信息
}
```

---

### 4. ThreadLocal上下文

使用ThreadLocal存储工具执行信息:

```java
// 开始执行
ToolExecutionContext.startExecution(toolName, cardType, parameters);

// 执行工具
Object result = joinPoint.proceed();

// 结束执行
ToolExecutionContext.endExecution(result);

// 获取信息
Map<String, ToolExecutionInfo> executions = ToolExecutionContext.getCardExecutions();
```

---

## 📁 核心文件清单

### Controller层
- `AIStreamController.java` - SSE流式响应控制器

### Agent层
- `StreamingIntelligentAssistantAgent.java` - L3智能调度Agent接口
- `LangChain4jStreamingConfig.java` - Agent配置

### 工具层
- `RecommendationQueryTools.java` - 菜品推荐工具
- `NutritionAnalysisTools.java` - 营养分析工具
- `OrderQueryTools.java` - 订单查询工具
- `OrderCreateTools.java` - 订单创建工具
- `UserProfileTools.java` - 用户资料工具
- `MerchantQueryTools.java` - 商家查询工具
- `TimeTools.java` - 时间服务工具

### 切面层
- `CardTypeAspect.java` - 卡片类型切面
- `ToolExecutionContext.java` - ThreadLocal上下文

### Service层
- `DishService.java` - 菜品服务
- `OrderService.java` - 订单服务
- `UserService.java` - 用户服务
- `MerchantService.java` - 商家服务

---

## 🎓 总结

佳食宜选后端AI系统采用 **LangChain4j** 框架，实现了：

1. **流式响应**: 通过SSE实时推送AI生成内容
2. **智能调度**: L3 Agent根据意图自动调用合适工具
3. **工具封装**: 业务逻辑通过工具类封装，清晰易维护
4. **卡片生成**: 通过AOP切面自动生成前端卡片数据
5. **分层架构**: Controller → Agent → Tools → Service → Mapper，职责清晰

**核心优势**:
- ✅ 实时流式输出，用户体验好
- ✅ 工具自动调用，开发效率高
- ✅ AOP卡片生成，代码解耦
- ✅ 分层清晰，易于扩展

**文件位置**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/`
