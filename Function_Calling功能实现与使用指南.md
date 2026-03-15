# Function Calling 功能实现与使用指南

> **佳食宜选 AI助手 - 智能工具函数调用系统**

本文档详细介绍佳食宜选AI助手的Function Calling（工具函数调用）功能，包括后端实现原理、前端适配方式、使用场景、联动操作以及扩展指南。

---

## 📋 目录

1. [系统概述](#系统概述)
2. [后端实现架构](#后端实现架构)
3. [前端适配方案](#前端适配方案)
4. [使用场景详解](#使用场景详解)
5. [联动操作流程](#联动操作流程)
6. [功能扩展指南](#功能扩展指南)
7. [最佳实践](#最佳实践)

---

## 系统概述

### 什么是 Function Calling？

Function Calling（函数调用）是AI大模型的一项核心能力，允许AI根据用户意图自动调用后端的工具函数，获取实时数据并生成精准回复。

### 系统特点

- ✅ **智能决策**：AI自动判断何时需要调用工具函数
- ✅ **参数解析**：AI理解用户自然语言并提取结构化参数
- ✅ **多函数协作**：支持单次请求调用多个函数
- ✅ **类型安全**：使用枚举替代字符串，避免硬编码错误
- ✅ **真实数据**：集成数据库服务，返回实时业务数据
- ✅ **可扩展性**：模块化设计，轻松添加新功能

### 技术栈

**后端**：
- 智谱AI SDK（官方）
- Spring Boot 4.0.2
- MyBatis-Plus
- MySQL + Redis

**前端**：
- Vue 3 Composition API
- Element Plus
- SSE（Server-Sent Events）

---

## 后端实现架构

### 1. 架构层次图

```
┌─────────────────────────────────────────────────┐
│           前端 AI 聊天界面                       │
│  (AIChatFull.vue + 快速提问面板)                 │
└──────────────────┬──────────────────────────────┘
                   │ HTTP/SSE
                   ▼
┌─────────────────────────────────────────────────┐
│           AIController.java                     │
│  (/v1/ai/chat)                                  │
└──────────────────┬──────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────┐
│        ZhipuAIServiceImpl.java                  │
│  1. 构建消息列表（系统提示词+历史对话+用户消息）  │
│  2. 添加工具函数定义                             │
│  3. 调用智谱AI SDK                               │
│  4. 解析工具函数调用请求                         │
└──────────────────┬──────────────────────────────┘
                   │
         ┌─────────┴─────────┐
         ▼                   ▼
┌──────────────────┐  ┌──────────────────────────┐
│ AiFunctionType  │  │ AiFunctionDefinitions    │
│   (枚举定义)     │  │    (函数定义构建)         │
└──────────────────┘  └──────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────┐
│        AiFunctionExecutorOptimized              │
│  1. 验证函数类型                                 │
│  2. 执行具体业务逻辑（调用Service层）            │
│  3. 格式化返回结果                               │
└──────────────────┬──────────────────────────────┘
                   │
         ┌─────────┴─────────┐
         ▼                   ▼
┌──────────────────┐  ┌──────────────────────────┐
│  业务服务层       │  │   数据访问层             │
│  - DishService   │  │   - MyBatis-Plus         │
│  - OrderService  │  │   - MySQL                │
│  - UserService   │  │   - Redis Cache          │
│  - NutritionSvc  │  │                          │
└──────────────────┘  └──────────────────────────┘
```

### 2. 核心组件详解

#### 2.1 AiFunctionType（枚举定义）

**位置**：`com.xx.jaseatschoicejava.enums.AiFunctionType`

**作用**：定义所有可用的AI工具函数类型，使用枚举避免字符串硬编码错误。

**代码结构**：
```java
@Getter
@AllArgsConstructor
public enum AiFunctionType {
    SEARCH_DISHES("search_dishes", "根据关键词或分类搜索菜品", 5000),
    GET_DISH_DETAILS("get_dish_details", "获取指定菜品的详细信息", 3000),
    CREATE_ORDER("create_order", "创建一个新的订单", 10000),
    GET_ORDER_STATUS("get_order_status", "查询订单的当前状态", 3000),
    GET_USER_PREFERENCES("get_user_preferences", "获取用户的饮食偏好和历史记录", 3000),
    ANALYZE_NUTRITION("analyze_nutrition", "分析食物的营养成分和热量", 5000);

    private final String functionName;      // 函数名称（用于API调用）
    private final String description;       // 函数描述（供AI理解）
    private final int timeout;              // 超时时间（毫秒）
}
```

**关键方法**：
```java
// 根据函数名称获取枚举
public static AiFunctionType fromFunctionName(String functionName)

// 验证函数名称是否有效
public static boolean isValidFunction(String functionName)

// 获取所有已启用的函数名称列表
public static String[] getAllFunctionNames()
```

**设计优势**：
- ✅ 类型安全：编译期检查，避免拼写错误
- ✅ 易于维护：新增函数只需添加枚举值
- ✅ 自文档化：枚举名称即为功能说明
- ✅ 统一管理：超时时间、描述等元数据集中定义

---

#### 2.2 AiFunctionDefinitionsOptimized（函数定义构建器）

**位置**：`com.xx.jaseatschoicejava.ai.function.AiFunctionDefinitionsOptimized`

**作用**：构建符合OpenAI Function Calling标准的工具函数定义Schema，供AI模型理解和使用。

**核心方法**：

1. **搜索菜品函数**
```java
private ToolFunction createSearchDishesFunction() {
    AiFunctionType type = AiFunctionType.SEARCH_DISHES;

    Map<String, Object> properties = new HashMap<>();
    properties.put("keyword", createStringProperty("搜索关键词"));
    properties.put("category", createStringPropertyWithEnum("菜品分类", dishCategories));

    return ToolFunction.builder()
            .name(type.getFunctionName())
            .description(type.getDescription())
            .parameters(createParameterSchema(properties, Collections.singletonList("keyword")))
            .build();
}
```

2. **创建订单函数**
```java
private ToolFunction createCreateOrderFunction() {
    Map<String, Object> dishItemProperties = new HashMap<>();
    dishItemProperties.put("dish_id", createStringProperty("菜品ID"));
    dishItemProperties.put("quantity", createIntegerProperty("数量"));

    Map<String, Object> itemsSchema = new HashMap<>();
    itemsSchema.put("type", "object");
    itemsSchema.put("properties", dishItemProperties);

    Map<String, Object> properties = new HashMap<>();
    properties.put("dish_items", createArrayProperty("菜品列表", itemsSchema));
    properties.put("address", createStringProperty("配送地址"));

    return ToolFunction.builder()
            .name("create_order")
            .description("创建一个新的订单")
            .parameters(createParameterSchema(properties, Arrays.asList("dish_items", "address")))
            .build();
}
```

**生成的Schema示例**：
```json
{
  "name": "search_dishes",
  "description": "根据关键词或分类搜索菜品",
  "parameters": {
    "type": "object",
    "properties": {
      "keyword": {
        "type": "string",
        "description": "搜索关键词"
      },
      "category": {
        "type": "string",
        "description": "菜品分类",
        "enum": ["主食", "菜肴", "汤品", "饮品", "小吃", "甜点"]
      }
    },
    "required": ["keyword"]
  }
}
```

**系统提示词配置**：
```java
systemPrompts.put("primary",
    "你是佳食宜选的智能饮食助手，可以帮助用户搜索菜品、查看订单、获取营养分析等。" +
    "你的职责：\n" +
    "1. 根据用户需求推荐合适的菜品\n" +
    "2. 提供准确的营养信息和健康建议\n" +
    "3. 协助用户完成下单和订单查询\n" +
    "4. 保持友好、专业的服务态度\n" +
    "请用简洁、自然的语言与用户交流。"
);
```

---

#### 2.3 ZhipuAIServiceImpl（AI服务实现）

**位置**：`com.xx.jaseatschoicejava.service.impl.ZhipuAIServiceImpl`

**核心流程**：

```java
@Override
public String chat(String message, List<Map<String, String>> conversationHistory) {
    // 1. 构建消息列表（系统提示词+历史对话+用户消息）
    List<ChatMessage> messages = buildMessages(message, conversationHistory);

    // 2. 构建请求（包含工具函数定义）
    ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
            .model(zhipuAIConfig.getModel())
            .messages(messages)
            .tools(convertToolDefinitionsToSDK())  // 注入工具函数
            .temperature(0.7f)
            .build();

    // 3. 调用智谱AI SDK
    var response = zhipuClient.chat().createChatCompletion(request);

    // 4. 判断是否需要调用工具函数
    List<ToolCalls> toolCalls = response.getData().getChoices()
            .get(0).getMessage().getToolCalls();

    if (!CollectionUtils.isEmpty(toolCalls)) {
        // 执行工具函数并生成最终回复
        return handleToolCalls(messages, toolCalls);
    }

    // 直接返回AI回复
    return response.getData().getChoices().get(0).getMessage().getContent().toString();
}
```

**工具函数调用处理**：

```java
private String handleToolCalls(List<ChatMessage> messages, List<ToolCalls> toolCalls) {
    // 1. 添加AI的工具调用请求消息
    ChatMessage assistantMessage = ChatMessage.builder()
            .role("assistant")
            .content("")
            .toolCalls(toolCalls)
            .build();
    messages.add(assistantMessage);

    // 2. 执行所有工具函数
    for (ToolCalls toolCall : toolCalls) {
        String functionName = toolCall.getFunction().getName();
        JsonNode argumentsJson = toolCall.getFunction().getArguments();

        // 解析参数并执行函数
        Map<String, Object> arguments = parseArguments(argumentsJson.toString());
        String result = functionExecutor.executeFunction(functionName, arguments);

        // 添加函数结果到对话
        messages.add(ChatMessage.builder()
                .role("tool")
                .content(result)
                .toolCallId(toolCall.getId())
                .build());
    }

    // 3. 再次调用AI，生成基于工具函数结果的最终回复
    ChatCompletionCreateParams followUpRequest = ChatCompletionCreateParams.builder()
            .model(zhipuAIConfig.getModel())
            .messages(messages)
            .tools(convertToolDefinitionsToSDK())
            .build();

    var followUpResponse = zhipuClient.chat().createChatCompletion(followUpRequest);
    return followUpResponse.getData().getChoices().get(0).getMessage().getContent().toString();
}
```

---

#### 2.4 AiFunctionExecutorOptimized（函数执行器）

**位置**：`com.xx.jaseatschoicejava.ai.function.AiFunctionExecutorOptimized`

**作用**：根据AI模型选择的函数类型，执行具体业务逻辑并返回格式化结果。

**执行流程**：

```java
public String executeFunction(String functionName, Map<String, Object> arguments) {
    // 1. 验证函数名称
    AiFunctionType functionType = AiFunctionType.fromFunctionName(functionName);
    if (functionType == null) {
        return buildErrorResponse("未知的工具函数: " + functionName);
    }

    // 2. 根据枚举类型执行相应函数（Java 17 Switch表达式）
    return switch (functionType) {
        case SEARCH_DISHES -> searchDishes(arguments);
        case GET_DISH_DETAILS -> getDishDetails(arguments);
        case CREATE_ORDER -> createOrder(arguments);
        case GET_ORDER_STATUS -> getOrderStatus(arguments);
        case GET_USER_PREFERENCES -> getUserPreferences(arguments);
        case ANALYZE_NUTRITION -> analyzeNutrition(arguments);
    };
}
```

**搜索菜品实现示例**：

```java
private String searchDishes(Map<String, Object> arguments) {
    String keyword = getStringArgument(arguments, "keyword");
    String category = getStringArgument(arguments, "category");

    // 构建查询条件
    QueryWrapper<Dish> queryWrapper = new QueryWrapper<>();
    if (keyword != null && !keyword.isEmpty()) {
        queryWrapper.like("name", keyword);
    }
    if (category != null && !category.isEmpty()) {
        queryWrapper.eq("category", category);
    }
    queryWrapper.eq("is_online", true)
            .orderByDesc("avg_rating")
            .last("LIMIT 10");

    List<Dish> dishes = dishService.list(queryWrapper);

    // 格式化返回结果（AI可理解的自然语言）
    StringBuilder result = new StringBuilder("找到以下菜品：\n\n");
    for (int i = 0; i < dishes.size(); i++) {
        Dish dish = dishes.get(i);
        result.append(String.format("%d. %s\n", i + 1, dish.getName()));
        result.append(String.format("   价格：￥%.2f", dish.getPrice()));
        if (dish.getCalorie() != null) {
            result.append(String.format(" | 热量：%d kcal", dish.getCalorie()));
        }
        result.append("\n\n");
    }
    result.append(String.format("共找到%d道菜品，需要查看详情或下单吗？", dishes.size()));
    return result.toString();
}
```

**创建订单实现示例**：

```java
private String createOrder(Map<String, Object> arguments) {
    // 1. 解析参数
    List<Map<String, Object>> dishItems = getArrayArgument(arguments, "dish_items");
    String address = getStringArgument(arguments, "address");

    // 2. 参数验证
    if (dishItems == null || dishItems.isEmpty()) {
        return buildErrorResponse("请至少选择一道菜品");
    }

    // 3. 查询菜品信息并计算总金额
    List<OrderDish> orderDishes = new ArrayList<>();
    BigDecimal totalAmount = BigDecimal.ZERO;

    for (Map<String, Object> item : dishItems) {
        String dishId = getStringArgument(item, "dish_id");
        Integer quantity = getIntegerArgument(item, "quantity");

        Dish dish = dishService.getById(dishId);
        totalAmount = totalAmount.add(dish.getPrice().multiply(new BigDecimal(quantity)));
    }

    // 4. 创建订单对象
    Order order = new Order();
    order.setUserId(userId);
    order.setTotalAmount(totalAmount);
    order.setAddress(address);
    order.setStatus(0); // 待支付

    // 5. 调用订单服务创建订单
    boolean success = orderService.createOrderWithDishes(order, orderDishes);

    // 6. 返回成功结果
    return "订单创建成功！🎉\n\n" +
           "📋 订单详情：\n" +
           "- 订单号：" + order.getId() + "\n" +
           "- 菜品数量：" + orderDishes.size() + "道\n" +
           "- 订单总金额：￥" + totalAmount + "\n" +
           "- 配送地址：" + address + "\n\n" +
           "预计30分钟内送达，谢谢您的订购！🍴";
}
```

---

### 3. 数据流转示例

#### 示例1：用户搜索菜品

**用户输入**：`"帮我搜索一些辣味的川菜"`

**数据流转**：
```
1. 用户消息 → AIController
   POST /v1/ai/chat
   { "message": "帮我搜索一些辣味的川菜" }

2. AIController → ZhipuAIServiceImpl.chat()
   构建消息列表：
   [
     { role: "system", content: "你是佳食宜选的智能饮食助手..." },
     { role: "user", content: "帮我搜索一些辣味的川菜" }
   ]

3. ZhipuAI SDK 分析意图
   返回工具调用请求：
   {
     "tool_calls": [
       {
         "function": {
           "name": "search_dishes",
           "arguments": { "keyword": "川菜", "category": "菜肴" }
         }
       }
     ]
   }

4. AiFunctionExecutorOptimized.executeFunction()
   执行 searchDishes({keyword: "川菜", category: "菜肴"})
   查询数据库：SELECT * FROM dish WHERE name LIKE '%川菜%' AND category='菜肴'
   返回结果：
   "找到以下菜品：

   1. 宫保鸡丁
      价格：￥28.00 | 热量：350 kcal

   2. 麻婆豆腐
      价格：￥18.00 | 热量：280 kcal

   共找到2道菜品，需要查看详情或下单吗？"

5. 再次调用AI生成最终回复
   输入：工具函数执行结果
   输出：
   "为您找到了2道川菜：
   🍽️ 宫保鸡丁 - ￥28.00，经典川菜代表
   🍽️ 麻婆豆腐 - ￥18.00，麻辣鲜香

   需要查看详细介绍或直接下单吗？"
```

#### 示例2：用户创建订单

**用户输入**：`"我要下单宫保鸡丁2份，送到学生宿舍5号楼203"`

**数据流转**：
```
1. 用户消息 → AIController

2. AI识别需要调用多个函数：
   第一步：get_dish_details({dish_id: "xxx"})  // 获取菜品详情
   第二步：create_order({
       dish_items: [{dish_id: "xxx", quantity: 2}],
       address: "学生宿舍5号楼203"
     })

3. AiFunctionExecutorOptimized 执行函数
   - 查询菜品信息：宫保鸡丁 ￥28.00
   - 计算总金额：28.00 × 2 = 56.00
   - 创建订单记录：INSERT INTO order ...

4. 返回结果：
   "订单创建成功！🎉

   📋 订单详情：
   - 订单号：20260314123456
   - 菜品数量：1道
   - 订单总金额：￥56.00
   - 配送地址：学生宿舍5号楼203
   - 订单状态：待支付

   预计30分钟内送达，谢谢您的订购！🍴"
```

---

## 前端适配方案

### 1. 整体架构

```
┌─────────────────────────────────────────────────┐
│           AIChatFull.vue (主聊天界面)            │
│  ┌────────────────────────────────────────────┐ │
│  │   聊天消息列表（支持流式渲染）               │ │
│  └────────────────────────────────────────────┘ │
│  ┌───────────────────┬────────────────────────┐│
│  │  快速提问面板      │   输入框 + 发送按钮      ││
│  │  (分类折叠面板)    │                        ││
│  └───────────────────┴────────────────────────┘│
└─────────────────────────────────────────────────┘
```

### 2. 快速提问分类设计

**设计理念**：将后端的6个Function Calling能力映射到4个前端分类，方便用户快速发起AI对话。

**分类映射**：

```javascript
const quickQuestionCategories = ref([
  {
    id: 'dish-exploration',
    title: '🍽️ 菜品探索',
    expanded: true,
    questions: [
      "帮我搜索一些主食菜品",           // → search_dishes
      "有什么推荐的甜点吗",             // → search_dishes
      "搜索包含鸡肉的菜肴",             // → search_dishes
      "查看汤品分类的菜品"              // → search_dishes
    ]
  },
  {
    id: 'nutrition-analysis',
    title: '📊 营养分析',
    expanded: false,
    questions: [
      "分析西红柿炒鸡蛋的营养成分",     // → analyze_nutrition
      "宫保鸡丁的热量是多少",           // → analyze_nutrition + get_dish_details
      "这份菜的蛋白质含量高吗",         // → analyze_nutrition
      "分析这碗米饭的营养价值"          // → analyze_nutrition
    ]
  },
  {
    id: 'order-management',
    title: '🛒 订单管理',
    expanded: false,
    questions: [
      "我要下单宫保鸡丁和红烧肉",       // → get_dish_details + create_order
      "查询我的订单状态",               // → get_order_status
      "创建一个新订单",                 // → create_order
      "我的订单配送到了吗"              // → get_order_status
    ]
  },
  {
    id: 'personalized-recommendation',
    title: '👤 个性化推荐',
    expanded: false,
    questions: [
      "根据我的喜好推荐菜品",           // → get_user_preferences + search_dishes
      "查看我的饮食偏好",               // → get_user_preferences
      "我最近都点了什么菜",             // → get_user_preferences
      "有什么适合我的健康菜品推荐"      // → get_user_preferences + search_dishes
    ]
  }
])
```

**关键特性**：
- ✅ **手风琴折叠**：每个分类可独立展开/收起，节省空间
- ✅ **emoji图标**：视觉化区分功能类别
- ✅ **自然语言问题**：用户无需了解技术细节，直接点击提问
- ✅ **AI智能路由**：同一个问题可能触发多个后端函数

### 3. UI组件实现

**分类面板组件**（AIChatFull.vue:66-104）：

```vue
<div class="quick-questions-categories">
  <div v-for="category in quickQuestionCategories" :key="category.id" class="question-category">
    <!-- 分类标题（可点击展开/收起） -->
    <div class="category-header" @click="toggleCategory(category.id)">
      <span class="category-title">{{ category.title }}</span>
      <el-icon class="category-arrow" :class="{ 'is-expanded': category.expanded }">
        <ArrowRight />
      </el-icon>
    </div>

    <!-- 问题列表（带动画过渡） -->
    <transition name="category-slide">
      <div v-show="category.expanded" class="category-questions">
        <div v-for="question in category.questions"
             :key="question"
             @click.stop="handleQuickQuestion(question)"
             class="question-item">
          {{ question }}
        </div>
      </div>
    </transition>
  </div>
</div>
```

**点击事件处理**：

```javascript
// 切换分类展开状态
const toggleCategory = (categoryId) => {
  const category = quickQuestionCategories.value.find(c => c.id === categoryId)
  if (category) {
    category.expanded = !category.expanded
  }
}

// 处理快速提问
const handleQuickQuestion = (question) => {
  // 添加用户消息到聊天记录
  messages.value.push({
    role: 'user',
    content: question
  })

  // 触发AI对话
  streamResponse(question)
}
```

**CSS动画**（AIChatFull.vue:1373-1507）：

```less
.quick-questions-panel {
  position: absolute;
  bottom: 100%;
  right: 0;
  max-width: 320px;
  max-height: 400px;
  overflow-y: auto;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

  .question-category {
    border: 1px solid #e8ecef;
    border-radius: 6px;
    margin-bottom: 8px;

    .category-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 10px 12px;
      cursor: pointer;
      background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
      transition: all 0.2s;

      &:hover {
        background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);
      }

      .category-arrow {
        transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);

        &.is-expanded {
          transform: rotate(90deg);
        }
      }
    }

    .category-questions {
      padding: 8px;

      .question-item {
        padding: 8px 12px;
        margin: 4px 0;
        border-radius: 4px;
        cursor: pointer;
        transition: all 0.2s;

        &:hover {
          background: #f0f0f0;
        }
      }
    }
  }
}

// 分类展开/收起动画
.category-slide-enter-active,
.category-slide-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.category-slide-enter-from,
.category-slide-leave-to {
  max-height: 0;
  opacity: 0;
}

.category-slide-enter-to,
.category-slide-leave-from {
  max-height: 200px;
  opacity: 1;
}
```

### 4. 流式响应处理

**核心逻辑**（AIChatFull.vue:678-789）：

```javascript
const streamResponse = async (userMessage) => {
  // 1. 添加用户消息
  messages.value.push({
    role: 'user',
    content: userMessage
  })

  // 2. 创建AI占位消息
  const aiMessage = {
    role: 'assistant',
    content: '',
    isStreaming: true
  }
  messages.value.push(aiMessage)

  // 3. 滚动到底部（强制）
  scrollToBottom(true)

  // 4. 创建AbortController用于取消请求
  const controller = new AbortController()

  try {
    const response = await fetch('http://localhost:8080/v1/ai/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        message: userMessage,
        history: messages.value.slice(0, -1).map(msg => ({
          role: msg.role,
          content: msg.content
        }))
      }),
      signal: controller.signal
    })

    const reader = response.body.getReader()
    const decoder = new TextDecoder()

    // 5. 读取流式数据
    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const chunk = decoder.decode(value)
      const lines = chunk.split('\n')

      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const data = JSON.parse(line.slice(6))
          if (data.content) {
            aiMessage.content += data.content
            // 仅在用户未手动滚动时自动滚动
            scrollToBottom(false)
          }
        }
      }
    }

  } catch (error) {
    if (error.name === 'AbortError') {
      aiMessage.content = '（用户已取消生成）'
    } else {
      aiMessage.content = '抱歉，AI服务出现错误：' + error.message
    }
  } finally {
    aiMessage.isStreaming = false
    isLoading.value = false
  }
}
```

**智能滚动逻辑**：

```javascript
// 用户滚动状态追踪
const userHasScrolled = ref(false)
let isAutoScrolling = false

// 滚动事件处理
const handleScroll = () => {
  if (isAutoScrolling) return

  const container = chatContainerRef.value
  if (!container) return

  // 判断是否接近底部（100px阈值）
  const isNearBottom =
    container.scrollHeight - container.scrollTop - container.clientHeight < 100

  if (!isNearBottom) {
    userHasScrolled.value = true
  } else {
    userHasScrolled.value = false
  }
}

// 滚动到底部
const scrollToBottom = (force = false) => {
  if (force || !userHasScrolled.value) {
    isAutoScrolling = true
    nextTick(() => {
      if (chatContainerRef.value) {
        chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
      }
      setTimeout(() => {
        isAutoScrolling = false
      }, 100)
    })
  }
}
```

**关键特性**：
- ✅ **尊重用户意图**：用户手动滚动后停止自动滚动
- ✅ **智能阈值**：距离底部100px内视为"接近底部"
- ✅ **强制滚动**：用户发送消息时强制滚动到底部
- ✅ **防抖动**：使用 `isAutoScrolling` 标志防止滚动事件循环

### 5. Markdown渲染与XSS防护

**自定义解析器**（AIChatFull.vue:791-873）：

```javascript
const parseMarkdown = (text) => {
  if (!text) return ''

  // XSS防护：移除危险标签
  let html = text
    .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
    .replace(/<iframe\b[^<]*(?:(?!<\/iframe>)<[^<]*)*<\/iframe>/gi, '')
    .replace(/javascript:/gi, '')
    .replace(/on\w+\s*=/gi, '')

  // 解析Markdown元素
  html = html
    // 代码块
    .replace(/```(\w+)?\n([\s\S]*?)```/g, '<pre><code class="language-$1">$2</code></pre>')
    // 行内代码
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    // 加粗
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    // 斜体
    .replace(/\*([^*]+)\*/g, '<em>$1</em>')
    // 链接
    .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>')
    // 换行
    .replace(/\n/g, '<br>')

  return html
}
```

---

## 使用场景详解

### 场景1：菜品搜索与推荐

**用户意图**：找到符合口味、预算、营养需求的菜品

**触发函数**：`search_dishes`

**用户输入示例**：
- "帮我找一些辣味的川菜"
- "有什么便宜的素食推荐吗"
- "搜索热量低于300卡的主食"
- "给我看看评分最高的菜品"

**后端处理流程**：
```
1. AI分析用户意图
   - 提取关键词：辣味、川菜
   - 识别分类：菜肴

2. 调用 search_dishes({keyword: "川菜", category: "菜肴"})

3. 数据库查询
   SELECT * FROM dish
   WHERE name LIKE '%川菜%'
     AND category = '菜肴'
     AND is_online = true
   ORDER BY avg_rating DESC
   LIMIT 10

4. 返回结果给AI
   "找到以下菜品：
   1. 宫保鸡丁 - ￥28.00 - ⭐4.8
   2. 麻婆豆腐 - ￥18.00 - ⭐4.7
   ..."

5. AI生成自然语言回复
   "为您找到了5道川菜，包括宫保鸡丁、麻婆豆腐等经典菜品。
    都是辣味十足的选择！需要查看详细介绍或直接下单吗？"
```

**前端展示**：
- AI回复以Markdown格式渲染
- 菜品列表结构化展示（序号、名称、价格、评分）
- 引导用户进行下一步操作（查看详情、下单）

---

### 场景2：营养分析与健康建议

**用户意图**：了解菜品的营养成分，做出健康选择

**触发函数**：`analyze_nutrition` + `get_dish_details`

**用户输入示例**：
- "宫保鸡丁的营养成分怎么样"
- "这道菜的蛋白质含量高吗"
- "分析一下西红柿炒鸡蛋的热量"
- "这道菜适合减肥期间吃吗"

**后端处理流程**：
```
1. AI判断需要调用营养分析
   analyze_nutrition({food_name: "宫保鸡丁"})

2. 查询营养数据库
   SELECT * FROM nutrition_info
   WHERE food_name = '宫保鸡丁'

3. 返回详细营养数据
   {
     "food_name": "宫保鸡丁",
     "calories_per_100g": 185,
     "protein": 15.2,
     "fat": 8.5,
     "carbs": 12.3,
     "fiber": 1.2,
     "sodium": 450
   }

4. AI生成健康建议
   "宫保鸡丁每100克含185千卡热量，蛋白质含量丰富（15.2g），
    适合补充蛋白质。不过钠含量较高（450mg），建议高血压
    人群适量食用。整体而言，这是一道营养均衡的菜品。"
```

**数据来源**：
- **中国食物成分表**（标准数据库）
- 菜品营养信息表（`j_food_nutrition`）
- AI根据食材配比估算（自定义菜品）

---

### 场景3：订单创建全流程

**用户意图**：快速下单，无需复杂操作

**触发函数**：`get_dish_details` + `create_order`

**用户输入示例**：
- "我要下单宫保鸡丁2份，送到学生宿舍5号楼"
- "帮我点一份麻婆豆腐和一份米饭，地址是图书馆3楼"
- "我要订餐，要宫保鸡丁、红烧肉各一份，送到实验楼302"

**后端处理流程**：
```
1. AI解析用户意图，提取菜品和数量
   dish_items: [
     {dish_name: "宫保鸡丁", quantity: 2}
   ]
   address: "学生宿舍5号楼"

2. 调用 get_dish_details 获取菜品ID
   根据菜品名称查询：
   SELECT * FROM dish WHERE name = '宫保鸡丁'
   返回：{id: "1001", price: 28.00, ...}

3. 调用 create_order 创建订单
   {
     "dish_items": [
       {"dish_id": "1001", "quantity": 2}
     ],
     "address": "学生宿舍5号楼",
     "user_id": "current_user_id"
   }

4. 后端订单处理
   - 计算总金额：28.00 × 2 = 56.00
   - 创建订单记录
   - 扣减库存（如果启用）
   - 触发商家通知

5. 返回成功结果
   "订单创建成功！🎉

   📋 订单详情：
   - 订单号：20260314123456
   - 菜品：宫保鸡丁 × 2
   - 订单总金额：￥56.00
   - 配送地址：学生宿舍5号楼
   - 预计送达时间：30分钟内"
```

**前端联动**：
- 订单创建成功后，显示"查看订单详情"按钮
- 跳转到订单列表页面
- 显示订单状态实时更新（WebSocket推送）

---

### 场景4：订单状态查询

**用户意图**：了解订单进度，规划时间

**触发函数**：`get_order_status`

**用户输入示例**：
- "我的订单配送到了吗"
- "查询订单20260314123456的状态"
- "我刚才下的单现在什么情况了"
- "订单还有多久送到"

**后端处理流程**：
```
1. AI识别查询意图
   如果用户未提供订单号，从上下文推断最近订单

2. 调用 get_order_status({order_id: "20260314123456"})

3. 查询订单信息
   SELECT * FROM `order` WHERE id = '20260314123456'

4. 返回订单状态
   {
     "order_id": "20260314123456",
     "status": 3,  // 烹饪中
     "total_amount": 56.00,
     "create_time": "2026-03-14 12:30:00",
     "estimated_delivery": "2026-03-14 13:00:00"
   }

5. AI生成友好回复
   "您的订单正在烹饪中，大约10分钟后可以出餐。
    预计13:00送达您手中，请耐心等待哦～"
```

**订单状态映射**：
```java
0 → 待支付
1 → 待接单
2 → 备菜中
3 → 烹饪中
4 → 待上菜
5 → 已送达
6 → 已取消
7 → 待评价
8 → 已评价
```

---

### 场景5：个性化推荐

**用户意图**：发现符合个人口味的菜品

**触发函数**：`get_user_preferences` + `search_dishes`

**用户输入示例**：
- "根据我的喜好推荐一些菜品"
- "我最近都点了什么菜"
- "有什么适合我的健康菜品推荐"
- "查看我的饮食偏好"

**后端处理流程**：
```
1. 调用 get_user_preferences({user_id: "current_user"})

2. 查询用户历史订单
   SELECT d.*, COUNT(*) as order_count
   FROM `order` o
   JOIN order_dish od ON o.id = od.order_id
   JOIN dish d ON od.dish_id = d.id
   WHERE o.user_id = 'current_user'
   GROUP BY d.id
   ORDER BY order_count DESC
   LIMIT 5

3. 分析用户偏好
   - 常点菜系：川菜（60%）、粤菜（30%）
   - 口味偏好：辣（80%）、清淡（20%）
   - 价格区间：20-40元
   - 营养关注：高蛋白（70%）

4. 调用 search_dishes 推荐相似菜品
   {
     "keyword": "辣",
     "category": "菜肴",
     "price_range": [20, 40]
   }

5. AI生成个性化推荐
   "根据您的历史订单，我发现您偏爱辣味川菜，
    为您推荐以下菜品：
   1. 口水鸡 - ￥32.00 - 酸辣开胃
   2. 辣子鸡 - ￥35.00 - 麻辣鲜香
   3. 水煮鱼 - ￥38.00 - 四川经典

    这些菜品都符合您的口味偏好，试试看吧！"
```

**推荐算法要素**：
- 历史订单分析（协同过滤）
- 菜品标签匹配（内容推荐）
- 时间因素（午餐/晚餐/夜宵）
- 天气因素（雨天推荐热汤，晴天推荐凉菜）
- 健康目标（减脂、增肌、控糖）

---

## 联动操作流程

### 流程1：搜索 → 详情 → 下单

**用户旅程**：
```
1. 用户："帮我找一些川菜"
   ↓
   AI：调用 search_dishes({keyword: "川菜"})
   ↓
   返回："找到5道川菜：宫保鸡丁、麻婆豆腐、口水鸡..."

2. 用户："宫保鸡丁的详细介绍"
   ↓
   AI：调用 get_dish_details({dish_id: "1001"})
   ↓
   返回："宫保鸡丁详情：￥28.00，热量350kcal，评分4.8..."

3. 用户："我要下单宫保鸡丁2份"
   ↓
   AI：调用 create_order({
         dish_items: [{dish_id: "1001", quantity: 2}],
         address: "用户默认地址"
       })
   ↓
   返回："订单创建成功！订单号：20260314123456..."
```

**关键点**：
- AI维护对话上下文，记住之前搜索的菜品
- 用户无需重复输入菜品ID，自然语言即可
- 流程连贯，无需跳转页面

---

### 流程2：营养分析 → 推荐 → 下单

**用户旅程**：
```
1. 用户："这道菜适合减肥期间吃吗"
   ↓
   AI：推断用户指的是最近搜索的菜品
   ↓
   调用 analyze_nutrition({food_name: "宫保鸡丁"})
   ↓
   返回："宫保鸡丁热量185kcal/100g，蛋白质15.2g，
         适合减肥期间食用，建议搭配蔬菜。"

2. 用户："有什么适合减肥的蔬菜推荐"
   ↓
   AI：调用 search_dishes({
         keyword: "蔬菜",
         max_calories: 200
       })
   ↓
   返回："为您推荐以下低热量蔬菜菜品：
         1. 凉拌黄瓜 - ￥12.00 - 热量80kcal
         2. 清炒时蔬 - ￥18.00 - 热量120kcal..."

3. 用户："我要一份凉拌黄瓜"
   ↓
   AI：调用 create_order({
         dish_items: [{dish_id: "2005", quantity: 1}]
       })
   ↓
   返回："订单创建成功！健康之选～"
```

**关键点**：
- AI提供专业营养建议
- 根据健康目标智能推荐
- 闭环完成下单

---

### 流程3：订单查询 → 催单 → 取消

**用户旅程**：
```
1. 用户："我的订单怎么还没送到"
   ↓
   AI：调用 get_order_status({order_id: "最新订单"})
   ↓
   返回："您的订单状态是【烹饪中】，
         预计10分钟后出餐。"

2. 用户："能不能帮我催一下"
   ↓
   AI：调用 notify_merchant({
         order_id: "xxx",
         type: "urge"
       })
   ↓
   返回："已经通知商家为您加快处理，
         请耐心等待片刻～"

3. 用户："太慢了，取消订单"
   ↓
   AI：调用 cancel_order({order_id: "xxx"})
   ↓
   返回："订单已取消，如有疑问请联系客服。"
```

**关键点**：
- AI理解用户情绪（催单、投诉）
- 自动调用对应的客服函数
- 提供人性化的交互体验

---

### 流程4：多轮对话式点餐

**用户旅程**：
```
1. 用户："我要点餐"
   ↓
   AI："好的，请问您想点什么菜品呢？"

2. 用户："有什么推荐的"
   ↓
   AI：调用 search_dishes({sort: "rating", limit: 5})
   ↓
   返回："为您推荐以下热门菜品：
         1. 宫保鸡丁 - ⭐4.8
         2. 红烧肉 - ⭐4.7
         3. 西湖醋鱼 - ⭐4.6..."

3. 用户："来一份宫保鸡丁"
   ↓
   AI：内部添加到临时购物车
   ↓
   "好的，宫保鸡丁已添加。还需要其他菜品吗？"

4. user："再加一份米饭"
   ↓
   AI：内部添加到临时购物车
   ↓
   "好的，米饭已添加。当前订单：
      - 宫保鸡丁 × 1
      - 米饭 × 1
      总计：￥30.00
      请提供配送地址。"

5. user："送到图书馆3楼"
   ↓
   AI：调用 create_order({
         dish_items: [...],
         address: "图书馆3楼"
       })
   ↓
   返回："订单创建成功！🎉"
```

**关键点**：
- AI维护临时状态（购物车）
- 多轮交互式点餐
- 实时计算总价
- 自然对话体验

---

## 功能扩展指南

### 扩展1：购物车管理

**需求场景**：用户需要先选择多个菜品，统一下单

**后端实现**：

1. **新增枚举**（AiFunctionType.java）：
```java
CART_ADD("cart_add", "添加菜品到购物车", 3000),
CART_REMOVE("cart_remove", "从购物车移除菜品", 3000),
CART_LIST("cart_list", "查看购物车内容", 3000),
CART_CLEAR("cart_clear", "清空购物车", 3000);
```

2. **函数定义**（AiFunctionDefinitionsOptimized.java）：
```java
private ToolFunction createCartAddFunction() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("dish_id", createStringProperty("菜品ID"));
    properties.put("quantity", createIntegerProperty("数量"));

    return ToolFunction.builder()
            .name("cart_add")
            .description("添加菜品到购物车")
            .parameters(createParameterSchema(properties, Arrays.asList("dish_id")))
            .build();
}
```

3. **函数执行**（AiFunctionExecutorOptimized.java）：
```java
private String cartAdd(Map<String, Object> arguments) {
    String dishId = getStringArgument(arguments, "dish_id");
    Integer quantity = getIntegerArgument(arguments, "quantity");

    // 调用购物车服务
    cartService.addItem(userId, dishId, quantity != null ? quantity : 1);

    // 返回当前购物车状态
    return cartService.getCartSummary(userId);
}

private String cartList(Map<String, Object> arguments) {
    List<CartItem> items = cartService.getCartItems(userId);

    if (items.isEmpty()) {
        return "您的购物车是空的，快去添加喜欢的菜品吧～";
    }

    StringBuilder result = new StringBuilder("🛒 您的购物车：\n\n");
    BigDecimal total = BigDecimal.ZERO;

    for (CartItem item : items) {
        result.append(String.format("- %s × %d  ￥%.2f\n",
            item.getDishName(), item.getQuantity(), item.getSubtotal()));
        total = total.add(item.getSubtotal());
    }

    result.append(String.format("\n总计：￥%.2f", total));
    result.append("\n\n需要结算吗？说"结算"即可完成下单。");
    return result.toString();
}
```

4. **AI交互示例**：
```
用户："我要宫保鸡丁和麻婆豆腐"
AI：[cart_add 宫保鸡丁] → [cart_add 麻婆豆腐]
    "已添加到购物车：
     - 宫保鸡丁 × 1
     - 麻婆豆腐 × 1
     总计：￥46.00
     需要结算吗？"

用户："再加一份米饭"
AI：[cart_add 米饭]
    "已添加，当前总计：￥48.00"

用户："结算"
AI：[cart_list] → [create_order]
    "订单创建成功！订单号：xxx"
```

---

### 扩展2：收藏与历史

**需求场景**：用户收藏喜欢的菜品，查看浏览历史

**后端实现**：

1. **新增枚举**：
```java
FAVORITE_ADD("favorite_add", "收藏菜品", 3000),
FAVORITE_REMOVE("favorite_remove", "取消收藏", 3000),
FAVORITE_LIST("favorite_list", "查看收藏列表", 3000),
HISTORY_GET("history_get", "获取浏览历史", 3000);
```

2. **函数定义**：
```java
private ToolFunction createFavoriteAddFunction() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("dish_id", createStringProperty("菜品ID"));

    return ToolFunction.builder()
            .name("favorite_add")
            .description("收藏菜品到收藏夹")
            .parameters(createParameterSchema(properties, Collections.singletonList("dish_id")))
            .build();
}
```

3. **函数执行**：
```java
private String favoriteAdd(Map<String, Object> arguments) {
    String dishId = getStringArgument(arguments, "dish_id");

    // 检查是否已收藏
    if (favoriteService.isFavorited(userId, dishId)) {
        return "这道菜已经在您的收藏夹里了～";
    }

    // 添加收藏
    favoriteService.addFavorite(userId, dishId);

    // 获取菜品信息
    Dish dish = dishService.getById(dishId);

    return String.format("已收藏《%s》！可以在"我的收藏"中查看。", dish.getName());
}

private String favoriteList(Map<String, Object> arguments) {
    List<Dish> favorites = favoriteService.getUserFavorites(userId);

    if (favorites.isEmpty()) {
        return "您还没有收藏任何菜品，快去发现美食吧～";
    }

    StringBuilder result = new StringBuilder("⭐ 您的收藏夹（共" + favorites.size() + "道）：\n\n");
    for (int i = 0; i < favorites.size(); i++) {
        Dish dish = favorites.get(i);
        result.append(String.format("%d. %s - ￥%.2f\n", i + 1, dish.getName(), dish.getPrice()));
    }

    result.append("\n需要下单吗？告诉我就行～");
    return result.toString();
}
```

---

### 扩展3：智能筛选与排序

**需求场景**：用户按照价格、热量、评分等条件筛选菜品

**后端实现**：

1. **新增枚举**：
```java
FILTER_DISHES("filter_dishes", "按照条件筛选菜品", 5000);
```

2. **函数定义**：
```java
private ToolFunction createFilterDishesFunction() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("min_price", createNumberProperty("最低价格"));
    properties.put("max_price", createNumberProperty("最高价格"));
    properties.put("max_calories", createIntegerProperty("最大热量"));
    properties.put("min_rating", createNumberProperty("最低评分"));
    properties.put("sort_by", createStringPropertyWithEnum("排序方式",
        Arrays.asList("price_asc", "price_desc", "rating_desc", "calories_asc")));

    return ToolFunction.builder()
            .name("filter_dishes")
            .description("按照价格、热量、评分等条件筛选菜品")
            .parameters(createParameterSchema(properties, new ArrayList<>()))  // 所有参数可选
            .build();
}
```

3. **函数执行**：
```java
private String filterDishes(Map<String, Object> arguments) {
    BigDecimal minPrice = getDecimalArgument(arguments, "min_price");
    BigDecimal maxPrice = getDecimalArgument(arguments, "max_price");
    Integer maxCalories = getIntegerArgument(arguments, "max_calories");
    Double minRating = getDoubleArgument(arguments, "min_rating");
    String sortBy = getStringArgument(arguments, "sort_by");

    // 构建动态查询条件
    QueryWrapper<Dish> queryWrapper = new QueryWrapper<>();

    if (minPrice != null) {
        queryWrapper.ge("price", minPrice);
    }
    if (maxPrice != null) {
        queryWrapper.le("price", maxPrice);
    }
    if (maxCalories != null) {
        queryWrapper.le("calorie", maxCalories);
    }
    if (minRating != null) {
        queryWrapper.ge("avg_rating", minRating);
    }

    // 动态排序
    if (sortBy != null) {
        switch (sortBy) {
            case "price_asc" -> queryWrapper.orderByAsc("price");
            case "price_desc" -> queryWrapper.orderByDesc("price");
            case "rating_desc" -> queryWrapper.orderByDesc("avg_rating");
            case "calories_asc" -> queryWrapper.orderByAsc("calorie");
        }
    } else {
        queryWrapper.orderByDesc("avg_rating");  // 默认排序
    }

    List<Dish> dishes = dishService.list(queryWrapper);

    // 返回结果...
}
```

4. **AI交互示例**：
```
用户："给我推荐一些便宜又好吃的菜"
AI：[filter_dishes {max_price: 25, min_rating: 4.5, sort_by: "rating_desc"}]
    "为您找到了8道高性价比菜品：
     1. 麻婆豆腐 - ￥18.00 - ⭐4.7
     2. 西红柿炒鸡蛋 - ￥15.00 - ⭐4.6
     ..."

用户："有没有热量低于200的"
AI：[filter_dishes {max_calories: 200, sort_by: "calories_asc"}]
    "找到了5道低热量菜品：
     1. 凉拌黄瓜 - ￥12.00 - 80kcal
     2. 清炒时蔬 - ￥18.00 - 120kcal
     ..."
```

---

### 扩展4：订单修改与售后

**需求场景**：用户修改订单地址、数量，申请退款

**后端实现**：

1. **新增枚举**：
```java
ORDER_MODIFY("order_modify", "修改订单信息", 5000),
ORDER_CANCEL("order_cancel", "取消订单", 3000),
ORDER_REFUND("order_refund", "申请退款", 5000);
```

2. **函数定义**：
```java
private ToolFunction createOrderModifyFunction() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("order_id", createStringProperty("订单ID"));
    properties.put("address", createStringProperty("新配送地址"));
    properties.put("dish_items", createArrayProperty("修改菜品列表", itemsSchema));

    return ToolFunction.builder()
            .name("order_modify")
            .description("修改订单的配送地址或菜品数量")
            .parameters(createParameterSchema(properties, Collections.singletonList("order_id")))
            .build();
}
```

3. **函数执行**：
```java
private String orderModify(Map<String, Object> arguments) {
    String orderId = getStringArgument(arguments, "order_id");
    String newAddress = getStringArgument(arguments, "address");
    List<Map<String, Object>> newDishItems = getArrayArgument(arguments, "dish_items");

    Order order = orderService.getById(orderId);

    // 检查订单状态（只有待接单才能修改）
    if (order.getStatus() != 0 && order.getStatus() != 1) {
        return "抱歉，订单已进入制作流程，无法修改。如需帮助请联系客服。";
    }

    // 修改地址
    if (newAddress != null) {
        order.setAddress(newAddress);
        orderService.updateById(order);
    }

    // 修改菜品
    if (newDishItems != null && !newDishItems.isEmpty()) {
        orderService.updateOrderDishes(orderId, newDishItems);
        // 重新计算总价
        order = orderService.getById(orderId);
    }

    return String.format("订单修改成功！\n\n" +
            "📋 更新后的订单：\n" +
            "- 订单号：%s\n" +
            "- 总金额：￥%.2f\n" +
            "- 配送地址：%s\n\n" +
            "请确认信息无误。",
            orderId, order.getTotalAmount(), order.getAddress());
}
```

---

### 扩展5：客服与评价

**需求场景**：用户联系客服、评价订单

**后端实现**：

1. **新增枚举**：
```java
SUPPORT_CONTACT("support_contact", "联系客服", 3000),
EVALUATION_SUBMIT("evaluation_submit", "提交订单评价", 3000);
```

2. **函数执行**：
```java
private String supportContact(Map<String, Object> arguments) {
    String orderId = getStringArgument(arguments, "order_id");
    String issue = getStringArgument(arguments, "issue");

    // 创建客服工单
    SupportTicket ticket = new SupportTicket();
    ticket.setUserId(userId);
    ticket.setOrderId(orderId);
    ticket.setIssue(issue);
    ticket.setStatus("pending");
    ticket.setCreateTime(LocalDateTime.now());
    supportTicketService.save(ticket);

    // 发送通知给客服
    notificationService.notifySupport(ticket);

    return "已为您创建客服工单（编号：" + ticket.getId() + "），\n" +
           "客服人员会在10分钟内联系您，请保持电话畅通。\n\n" +
           "问题描述：" + issue;
}

private String evaluationSubmit(Map<String, Object> arguments) {
    String orderId = getStringArgument(arguments, "order_id");
    Integer rating = getIntegerArgument(arguments, "rating");  // 1-5星
    String content = getStringArgument(arguments, "content");
    List<String> tags = (List<String>) arguments.get("tags");  // ["味道好", "配送快"]

    // 检查订单状态（只有已送达才能评价）
    Order order = orderService.getById(orderId);
    if (order.getStatus() != 5) {
        return "订单尚未送达，暂时无法评价。请在收货后再评价哦～";
    }

    // 检查是否已评价
    if (evaluationService.hasEvaluated(orderId)) {
        return "您已经评价过该订单了，感谢您的反馈！";
    }

    // 创建评价
    Evaluation evaluation = new Evaluation();
    evaluation.setOrderId(orderId);
    evaluation.setUserId(userId);
    evaluation.setRating(rating);
    evaluation.setContent(content);
    evaluation.setTags(String.join(",", tags));
    evaluationService.save(evaluation);

    // 更新菜品评分
    dishService.updateRating(orderId, rating);

    return "评价提交成功！感谢您的宝贵意见～⭐\n\n" +
           "您的评价：" + rating + "星\n" +
           (content != null ? "评价内容：" + content : "");
}
```

---

## 最佳实践

### 1. 提示词工程

**系统提示词设计原则**：
- ✅ 明确AI的职责边界
- ✅ 指定何时调用工具函数
- ✅ 定义回复风格（友好、专业、简洁）
- ✅ 提供上下文示例

**示例**：
```java
private static final String DIET_ASSISTANT_PROMPT = """
    你是"佳食宜选"的专业AI饮食助手。

    **你的职责**：
    1. 帮助用户搜索和推荐菜品
    2. 提供营养分析和健康建议
    3. 协助用户完成订单操作
    4. 解答饮食相关疑问

    **何时调用工具函数**：
    - 用户需要搜索菜品时 → search_dishes
    - 用户询问菜品详情时 → get_dish_details
    - 用户要下单时 → create_order
    - 用户查询订单时 → get_order_status
    - 用户询问营养成分时 → analyze_nutrition
    - 用户想要个性化推荐时 → get_user_preferences + search_dishes

    **回复风格**：
    - 友好亲切，使用emoji增加亲和力
    - 专业准确，营养信息要科学
    - 简洁明了，避免长篇大论
    - 主动引导，告诉用户可以做什么

    **注意事项**：
    - 不要编造菜品信息，严格使用工具函数返回的数据
    - 如果用户的问题不清楚，主动询问澄清
    - 保护用户隐私，不要透露个人敏感信息
    """;
```

---

### 2. 错误处理

**分级处理策略**：

```java
public String executeFunction(String functionName, Map<String, Object> arguments) {
    try {
        // 1. 函数名称验证
        AiFunctionType functionType = AiFunctionType.fromFunctionName(functionName);
        if (functionType == null) {
            log.warn("未知的工具函数: {}", functionName);
            return "抱歉，我不明白您想做什么。可以换个说法试试吗？";
        }

        // 2. 参数验证
        validateArguments(functionType, arguments);

        // 3. 执行函数
        return switch (functionType) {
            case SEARCH_DISHES -> searchDishes(arguments);
            // ...
        };

    } catch (ValidationException e) {
        // 参数验证错误
        log.warn("参数验证失败: {}", e.getMessage());
        return "抱歉，" + e.getMessage() + "。请提供完整信息。";

    } catch (NotFoundException e) {
        // 资源未找到
        log.warn("资源未找到: {}", e.getMessage());
        return "抱歉，" + e.getMessage() + "。请检查信息是否正确。";

    } catch (BusinessException e) {
        // 业务逻辑错误
        log.error("业务错误: {}", e.getMessage());
        return "抱歉，" + e.getMessage() + "。如需帮助请联系客服。";

    } catch (Exception e) {
        // 系统错误
        log.error("系统错误", e);
        return "抱歉，系统出现了一些问题，请稍后重试。";
    }
}
```

**用户友好的错误消息**：
- ❌ "数据库连接失败"
- ✅ "抱歉，系统暂时无法连接，请稍后重试"

- ❌ "参数 dish_id 不能为空"
- ✅ "请告诉我您要点哪道菜，我帮您查询"

---

### 3. 性能优化

**优化策略**：

1. **数据库查询优化**：
```java
// ❌ N+1查询
for (OrderDish od : orderDishes) {
    Dish dish = dishService.getById(od.getDishId());  // 循环查询
}

// ✅ 批量查询
List<String> dishIds = orderDishes.stream()
    .map(OrderDish::getDishId)
    .collect(Collectors.toList());
Map<String, Dish> dishMap = dishService.listByIds(dishIds).stream()
    .collect(Collectors.toMap(Dish::getId, d -> d));
```

2. **缓存热点数据**：
```java
// 缓存热门菜品列表（5分钟过期）
@Cacheable(value = "hot_dishes", key = "'top10'", unless = "#result.isEmpty()")
public List<Dish> getHotDishes() {
    return dishService.list(new QueryWrapper<Dish>()
        .eq("is_online", true)
        .orderByDesc("avg_rating")
        .last("LIMIT 10"));
}
```

3. **异步处理耗时操作**：
```java
@Async
public void sendOrderNotification(String orderId) {
    // 异步发送通知，不阻塞主流程
    emailService.sendOrderConfirmation(orderId);
    pushService.sendOrderUpdate(orderId);
}
```

---

### 4. 安全防护

**XSS防护**：
```javascript
// 前端Markdown解析器移除危险标签
const parseMarkdown = (text) => {
  let html = text
    .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
    .replace(/javascript:/gi, '')
    .replace(/on\w+\s*=/gi, '');
  return html;
}
```

**SQL注入防护**：
```java
// ✅ 使用参数化查询（MyBatis-Plus）
QueryWrapper<Dish> queryWrapper = new QueryWrapper<>();
queryWrapper.like("name", keyword);  // 自动转义

// ❌ 字符串拼接（危险）
String sql = "SELECT * FROM dish WHERE name LIKE '%" + keyword + "%'";
```

**权限验证**：
```java
private String getOrderStatus(Map<String, Object> arguments) {
    String orderId = getStringArgument(arguments, "order_id");

    Order order = orderService.getById(orderId);

    // 验证订单归属
    if (!order.getUserId().equals(getCurrentUserId())) {
        log.warn("用户{}尝试访问他人订单{}", getCurrentUserId(), orderId);
        return "抱歉，您无权查看该订单。";
    }

    // 返回订单详情...
}
```

---

### 5. 日志记录

**结构化日志**：
```java
log.info("执行AI工具函数 | function={} | userId={} | args={}",
    functionName, getCurrentUserId(), arguments);

log.warn("工具函数执行失败 | function={} | error={} | userId={}",
    functionName, e.getMessage(), getCurrentUserId());

log.error("系统异常 | function={} | userId={} | stackTrace={}",
    functionName, getCurrentUserId(), ExceptionUtils.getStackTrace(e));
```

**关键操作审计**：
```java
@AuditLog(operation = "AI_CREATE_ORDER", description = "AI助手创建订单")
private String createOrder(Map<String, Object> arguments) {
    // ...
}
```

---

## 总结

### Function Calling 核心价值

1. **提升用户体验**：
   - 自然语言交互，无需学习复杂操作
   - 智能推荐，发现个性化美食
   - 一站式服务，从搜索到下单全流程

2. **提高业务效率**：
   - 减少用户操作步骤
   - 降低客服咨询量
   - 提升订单转化率

3. **技术可扩展性**：
   - 模块化设计，新增功能只需添加枚举和执行逻辑
   - 类型安全，编译期检查避免错误
   - 真实数据集成，告别硬编码

### 开发路线图

**第一阶段**（当前已完成）：
- ✅ 基础6个函数实现
- ✅ 前端快速提问面板
- ✅ 智能滚动行为

**第二阶段**（建议优先实现）：
- 🔲 购物车管理（P0）
- 🔲 订单修改功能（P0）
- 🔲 收藏与历史（P1）
- 🔲 智能筛选排序（P1）

**第三阶段**（增强体验）：
- 🔲 多轮对话点餐（P1）
- 🔲 营养目标跟踪（P2）
- 🔲 社交分享功能（P2）
- 🔲 语音交互支持（P3）

---

**文档版本**：v1.0.0
**最后更新**：2026-03-14
**维护者**：Claude AI Assistant
**反馈渠道**：项目负责人
