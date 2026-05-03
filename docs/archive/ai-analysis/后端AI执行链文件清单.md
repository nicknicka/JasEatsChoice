# 后端AI执行链文件清单

## 📚 文档说明

本文档详细列出佳食宜选后端AI系统的所有关键文件，按照调用链的层次结构组织，说明每个文件的作用和职责。

---

## 🏗️ 架构层次概览

```
┌─────────────────────────────────────────────────────┐
│  Layer 1: Controller层 (控制器)                       │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│  Layer 2: Agent层 (智能调度)                          │
├─────────────────────────────────────────────────────┤
│  • Agent接口定义                                      │
│  • Agent配置工厂                                      │
│  • 监听器                                            │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│  Layer 3: 工具层 (业务逻辑封装)                        │
├─────────────────────────────────────────────────────┤
│  • 推荐工具 (recommendation/)                         │
│  • 订单工具 (order/)                                 │
│  • 用户工具 (user/)                                  │
│  • 营养工具 (nutrition/)                             │
│  • 商家工具 (merchant/)                              │
│  • 系统工具 (system/)                                │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│  Layer 4: 切面层 (AOP拦截)                            │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│  Layer 5: 上下文层 (ThreadLocal存储)                  │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│  Layer 6: Service层 (数据服务)                        │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│  Layer 7: 数据访问层 (Mapper)                         │
└─────────────────────────────────────────────────────┘
```

---

## 📁 Layer 1: Controller层 (控制器)

### 1.1 AIStreamController.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/controller/AIStreamController.java`

**职责**: SSE流式响应控制器，处理AI聊天请求

**关键方法**:
- `streamChat()` - 处理SSE聊天请求
- `buildCardData()` - 根据卡片类型构建卡片数据
- `buildOrderListCardData()` - 构建订单列表卡片
- `buildUserInfoCardData()` - 构建用户信息卡片
- `buildOrderGuideCardData()` - 构建下单引导卡片
- `health()` - 健康检查接口

**调用链位置**:
```
前端请求 → AIStreamController.streamChat()
    ↓
StreamingIntelligentAssistantAgent.chat()
    ↓
流式响应 + 卡片数据生成
```

**代码量**: 608行

---

## 📁 Layer 2: Agent层 (智能调度)

### 2.1 Agent接口定义

#### StreamingIntelligentAssistantAgent.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/agents/stream/StreamingIntelligentAssistantAgent.java`

**职责**: L3智能调度Agent接口（流式版本）

**配置**:
- SystemMessage: 定义角色、职责、可用工具
- 支持流式输出 (TokenStream)
- 工具自动调用 (通过@Tool注解)

**关键方法**:
```java
TokenStream chat(
    @UserMessage String userMessage,
    @V("userId") String userId
)
```

**System Prompt要点**:
- 角色: "佳食宜选"的智能助手
- 职责: 理解用户需求，智能调度专业工具
- 可用工具: 10个工具类

**代码量**: 94行

---

#### StreamingMerchantAssistantAgent.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/agents/stream/StreamingMerchantAssistantAgent.java`

**职责**: 商家端流式经营助手Agent

**使用场景**: 商家数据分析、经营优化建议

---

### 2.2 Agent配置工厂

#### LangChain4jStreamingConfig.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/config/LangChain4jStreamingConfig.java`

**职责**: LangChain4j流式Agent配置工厂

**关键Bean**:
1. `streamingChatLanguageModel` - 智谱AI流式模型
2. `streamingChatMemory` - 对话记忆 (20条消息)
3. `streamingIntelligentAssistantAgent` - 用户端Agent
4. `streamingMerchantAssistantAgent` - 商家端Agent

**工具注册**:
```java
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
```

**代码量**: 204行

---

#### SupervisorAgentFactory.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/service/SupervisorAgentFactory.java`

**职责**: SupervisorAgent工厂类（L3 → L1 架构）

**功能**:
- 动态创建带监听器的SupervisorAgent实例
- 支持每个用户独立的ChatMemory（Redis + MySQL混合存储）
- 移除L2层，L3 SupervisorAgent直接对接L1专家Agent

**关键方法**:
- `createWithListener()` - 创建带监听器的SupervisorAgent
- `createSupervisorContext()` - 创建详细的SupervisorContext
- `renderCards()` - 渲染卡片格式

**代码量**: 391行

---

### 2.3 监听器

#### SSEAgentListener.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/listener/SSEAgentListener.java`

**职责**: SSE Agent监听器，监听Agent执行事件

**功能**:
- 监听Agent执行开始/结束
- 监听工具调用
- 实时推送执行状态到前端

---

#### ExecutionEvent.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/listener/ExecutionEvent.java`

**职责**: Agent执行事件实体

---

#### ExecutionEventType.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/listener/ExecutionEventType.java`

**职责**: Agent执行事件类型枚举

---

### 2.4 记忆管理

#### ChatMemoryFactory.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/config/ChatMemoryFactory.java`

**职责**: ChatMemory工厂，支持Redis + MySQL混合存储

**功能**:
- 为每个用户创建独立的ChatMemory
- 支持对话历史的持久化

---

#### RedisBackedChatMemory.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/memory/RedisBackedChatMemory.java`

**职责**: Redis支持的ChatMemory实现

**功能**:
- 将对话历史存储到Redis
- 支持快速检索和更新

---

## 📁 Layer 3: 工具层 (业务逻辑封装)

### 3.1 推荐工具 (recommendation/)

#### RecommendationQueryTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/recommendation/RecommendationQueryTools.java`

**职责**: 菜品推荐查询工具

**关键方法**:
- `queryRecommendations()` - 根据用户偏好查询推荐菜品
- `searchDishes()` - 搜索菜品
- `getPersonalizedRecommendations()` - 获取个性化推荐

**使用场景**:
- 用户要求推荐
- 菜品搜索
- 个性化建议

**依赖服务**:
- DishService
- UserService

---

#### RecommendationFilterTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/recommendation/RecommendationFilterTools.java`

**职责**: 菜品推荐过滤工具

**功能**:
- 根据过敏信息过滤
- 根据饮食目标过滤
- 根据热量限制过滤

---

#### RecommendationRankTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/recommendation/RecommendationRankTools.java`

**职责**: 菜品推荐排序工具

**功能**:
- 按评分排序
- 按推荐度排序
- 按个性化得分排序

---

### 3.2 订单工具 (order/)

#### OrderQueryTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/order/OrderQueryTools.java`

**职责**: 订单查询工具

**关键方法**:
- `getOrderDetail()` - 查询订单详情
- `getUserOrders()` - 查询用户所有订单
- `getOrderStatus()` - 查询订单状态

**使用场景**:
- 用户查询订单
- 订单状态跟踪
- 订单详情查看

**依赖服务**:
- OrderService

---

#### OrderCreateTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/order/OrderCreateTools.java`

**职责**: 订单创建工具

**关键方法**:
- `createOrder()` - 创建订单
- `validateOrder()` - 验证订单
- `calculateTotal()` - 计算订单总价

**使用场景**:
- 用户下单
- 订单验证
- 价格计算

---

#### OrderGuideTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/order/OrderGuideTools.java`

**职责**: 订单引导工具（带@CardType注解）

**关键方法**:
- `guideOrderCreation()` - 引导用户创建订单

**注解**: `@CardType("merchant_order_card")`

---

### 3.3 用户工具 (user/)

#### UserProfileTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/user/UserProfileTools.java`

**职责**: 用户资料工具

**关键方法**:
- `getUserProfile()` - 获取用户资料
- `updateUserProfile()` - 更新用户资料
- `getBodyData()` - 获取身体数据

**使用场景**:
- 用户查看个人信息
- 更新资料
- 健康数据管理

**依赖服务**:
- UserService

---

#### UserPreferenceTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/user/UserPreferenceTools.java`

**职责**: 用户偏好工具

**关键方法**:
- `getUserPreferences()` - 获取用户偏好
- `updatePreferences()` - 更新用户偏好

---

#### UserQueryTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/user/UserQueryTools.java`

**职责**: 用户查询工具

**关键方法**:
- `queryUserById()` - 根据ID查询用户
- `searchUsers()` - 搜索用户

---

#### UserHealthGoalTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/user/UserHealthGoalTools.java`

**职责**: 用户健康目标工具

**关键方法**:
- `getHealthGoal()` - 获取健康目标
- `updateHealthGoal()` - 更新健康目标
- `trackProgress()` - 跟踪目标进度

---

#### UserDietRecordTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/user/UserDietRecordTools.java`

**职责**: 用户饮食记录工具

**关键方法**:
- `getDietRecord()` - 获取饮食记录
- `addDietRecord()` - 添加饮食记录

---

#### HealthGoalTrackerTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/user/HealthGoalTrackerTools.java`

**职责**: 健康目标跟踪工具

**功能**:
- 跟踪健康目标进度
- 分析饮食数据
- 生成健康报告

---

### 3.4 营养工具 (nutrition/)

#### NutritionAnalysisTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/nutrition/NutritionAnalysisTools.java`

**职责**: 营养分析工具

**关键方法**:
- `analyzeNutrition()` - 分析营养成分
- `getNutritionInfo()` - 获取营养信息
- `compareNutrition()` - 比较营养含量

**使用场景**:
- 营养成分查询
- 菜品营养分析
- 营养对比

---

#### CalorieCalculatorTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/nutrition/CalorieCalculatorTools.java`

**职责**: 卡路里计算工具

**关键方法**:
- `calculateCalories()` - 计算卡路里
- `calculateDailyIntake()` - 计算每日摄入
- `calculateBMI()` - 计算BMI

---

#### NutritionQueryTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/nutrition/NutritionQueryTools.java`

**职责**: 营养查询工具

**关键方法**:
- `queryNutrition()` - 查询营养信息
- `queryDishNutrition()` - 查询菜品营养

---

#### DietRecordAnalysisTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/nutrition/DietRecordAnalysisTools.java`

**职责**: 饮食记录分析工具

**功能**:
- 分析饮食习惯
- 生成营养报告
- 提供饮食建议

---

### 3.5 商家工具 (merchant/)

#### MerchantQueryTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/merchant/MerchantQueryTools.java`

**职责**: 商家查询工具

**关键方法**:
- `getMerchantInfo()` - 获取商家信息
- `searchMerchants()` - 搜索商家
- `getNearbyMerchants()` - 获取附近商家

**使用场景**:
- 商家信息查询
- 商家搜索
- 附近商家推荐

**依赖服务**:
- MerchantService

---

#### MerchantStatsTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/merchant/MerchantStatsTools.java`

**职责**: 商家统计工具

**关键方法**:
- `getMerchantStats()` - 获取商家统计数据
- `getSalesReport()` - 获取销售报告
- `getPopularDishes()` - 获取热门菜品

**使用场景**:
- 商家数据分析
- 销售报告生成
- 经营优化建议

---

### 3.6 系统工具 (system/)

#### TimeTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/system/TimeTools.java`

**职责**: 时间服务工具

**关键方法**:
- `getCurrentTime()` - 获取当前时间
- `isOpenNow()` - 判断商家是否营业
- `getBusinessHours()` - 获取营业时间

**使用场景**:
- 时间查询
- 营业状态判断
- 时段推荐

---

#### LocationTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/system/LocationTools.java`

**职责**: 位置服务工具（带@CardType注解）

**关键方法**:
- `getCurrentLocation()` - 获取当前位置
- `getNearbyMerchants()` - 获取附近商家
- `calculateDistance()` - 计算距离

**注解**: `@CardType("food_recommendation_card")`

---

#### TimeRecommendationTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/system/TimeRecommendationTools.java`

**职责**: 时段推荐工具

**功能**:
- 根据时间推荐菜品
- 早餐/午餐/晚餐推荐

---

#### LocationRecommendationTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/system/LocationRecommendationTools.java`

**职责**: 位置推荐工具

**功能**:
- 根据位置推荐商家
- 附近美食推荐

---

### 3.7 其他工具

#### MenuQueryTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/menu/MenuQueryTools.java`

**职责**: 菜单查询工具（带@CardType注解）

**关键方法**:
- `getMenu()` - 获取菜单

**注解**: `@CardType("menu_card")`

---

#### CollectionTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/CollectionTools.java`

**职责**: 收藏工具

**功能**:
- 收藏菜品
- 查看收藏列表

---

#### RecipeTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/RecipeTools.java`

**职责**: 食谱工具

**功能**:
- 获取食谱
- 食谱推荐

---

#### NutritionRecordTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/NutritionRecordTools.java`

**职责**: 营养记录工具

**功能**:
- 记录营养摄入
- 查询营养记录

---

#### OrderTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/OrderTools.java`

**职责**: 订单工具（通用）

**功能**:
- 订单管理
- 订单查询

---

#### UserTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/UserTools.java`

**职责**: 用户工具（通用）

**功能**:
- 用户管理
- 用户查询

---

#### RecommendationTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/RecommendationTools.java`

**职责**: 推荐工具（通用）

**功能**:
- 菜品推荐
- 个性化推荐

---

#### NutritionTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/NutritionTools.java`

**职责**: 营养工具（通用）

**功能**:
- 营养分析
- 营养查询

---

#### MerchantTools.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/MerchantTools.java`

**职责**: 商家工具（通用）

**功能**:
- 商家管理
- 商家查询

---

## 📁 Layer 4: 切面层 (AOP拦截)

### 4.1 CardTypeAspect.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/aspect/CardTypeAspect.java`

**职责**: 卡片类型切面，拦截带@CardType注解的工具方法

**功能**:
1. 拦截工具方法执行
2. 记录方法名、参数、结果
3. 保存到ToolExecutionContext (ThreadLocal)

**关键代码**:
```java
@Around("@annotation(com.xx.jaseatschoicejava.agent.annotation.CardType)")
public Object aroundCardTypeMethod(ProceedingJoinPoint joinPoint) {
    // 1. 获取注解信息
    CardType cardType = ...;

    // 2. 记录工具执行开始
    ToolExecutionContext.startExecution(toolName, cardTypeValue, parameters);

    // 3. 执行工具方法
    Object result = joinPoint.proceed();

    // 4. 记录工具执行结束
    ToolExecutionContext.endExecution(result);

    return result;
}
```

**代码量**: 77行

---

### 4.2 CardType.java (注解)

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/annotation/CardType.java`

**职责**: @CardType注解定义

**功能**: 标注需要生成卡片的工具方法

**使用示例**:
```java
@CardType("order_list_card")
public String getUserOrders(String userId) {
    // ...
}
```

---

## 📁 Layer 5: 上下文层 (ThreadLocal存储)

### 5.1 ToolExecutionContext.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/context/ToolExecutionContext.java`

**职责**: ThreadLocal上下文，存储工具执行信息

**数据结构**:
```java
class ToolExecutionInfo {
    String toolName;                    // 工具名称
    String cardType;                    // 卡片类型
    Map<String, Object> parameters;     // 方法参数
    Object result;                      // 执行结果
}
```

**关键方法**:
- `startExecution()` - 开始工具执行
- `endExecution()` - 结束工具执行
- `getCardExecutions()` - 获取所有卡片执行信息
- `clear()` - 清理ThreadLocal

**使用流程**:
```
CardTypeAspect拦截
    ↓
ToolExecutionContext.startExecution()
    ↓
执行工具方法
    ↓
ToolExecutionContext.endExecution()
    ↓
AIStreamController获取信息 → buildCardData()
    ↓
ToolExecutionContext.clear()
```

---

## 📁 Layer 6: Service层 (数据服务)

### 6.1 DishService.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/service/DishService.java`

**职责**: 菜品数据服务

**关键方法**:
- `list()` - 查询菜品列表
- `getById()` - 根据ID查询菜品
- `updateById()` - 更新菜品信息

**使用场景**: 所有需要查询菜品数据的工具类

---

### 6.2 OrderService.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/service/OrderService.java`

**职责**: 订单数据服务

**关键方法**:
- `list()` - 查询订单列表
- `getById()` - 根据ID查询订单
- `save()` - 保存订单
- `updateById()` - 更新订单

**使用场景**: 订单相关工具类

---

### 6.3 UserService.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/service/UserService.java`

**职责**: 用户数据服务

**关键方法**:
- `getById()` - 根据ID查询用户
- `updateById()` - 更新用户信息
- `save()` - 保存用户

**使用场景**: 用户相关工具类

---

### 6.4 MerchantService.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/service/MerchantService.java`

**职责**: 商家数据服务

**关键方法**:
- `list()` - 查询商家列表
- `getById()` - 根据ID查询商家
- `updateById()` - 更新商家信息

**使用场景**: 商家相关工具类

---

## 📁 Layer 7: 数据访问层 (Mapper)

### 7.1 AIChatHistoryMapper.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/mapper/AIChatHistoryMapper.java`

**职责**: AI聊天历史数据访问

**功能**:
- 保存聊天历史
- 查询聊天历史
- 删除聊天历史

---

## 📁 Layer 8: 实体层 (Entity)

### 8.1 AIChatHistory.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/entity/AIChatHistory.java`

**职责**: AI聊天历史实体

**字段**:
- id - 主键
- userId - 用户ID
- message - 消息内容
- messageType - 消息类型（用户/助手）
- createTime - 创建时间

---

## 📁 Layer 9: DTO层 (数据传输对象)

### 9.1 用户相关DTO

#### UserProfile.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/dto/UserProfile.java`

**职责**: 用户资料DTO

---

#### UserBasicInfo.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/dto/UserBasicInfo.java`

**职责**: 用户基本信息DTO

---

#### UserStatistics.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/dto/UserStatistics.java`

**职责**: 用户统计信息DTO

---

#### UserHealthGoal.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/dto/UserHealthGoal.java`

**职责**: 用户健康目标DTO

---

#### UserDietPreference.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/dto/UserDietPreference.java`

**职责**: 用户饮食偏好DTO

---

### 9.2 订单相关DTO

#### OrderDetail.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/dto/OrderDetail.java`

**职责**: 订单详情DTO

---

#### OrderItemDTO.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/dto/OrderItemDTO.java`

**职责**: 订单项DTO

---

#### MerchantOrderCardDTO.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/dto/MerchantOrderCardDTO.java`

**职责**: 商家订单卡片DTO

---

### 9.3 营养相关DTO

#### NutritionInfo.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/dto/NutritionInfo.java`

**职责**: 营养信息DTO

---

## 📁 Layer 10: 监控层 (Monitoring)

### 10.1 AgentCallTracer.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/monitoring/AgentCallTracer.java`

**职责**: Agent调用追踪器

**功能**:
- 追踪Agent调用链
- 记录调用时间
- 分析性能瓶颈

---

### 10.2 AgentPerformanceMonitor.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/monitoring/AgentPerformanceMonitor.java`

**职责**: Agent性能监控器

**功能**:
- 监控Agent性能
- 统计调用次数
- 计算响应时间

---

### 10.3 CallChainTraceService.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/monitoring/CallChainTraceService.java`

**职责**: 调用链追踪服务

**功能**:
- 追踪完整调用链
- 生成调用链报告
- 支持调用链分析

---

### 10.4 AgentMonitoringConfig.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/monitoring/AgentMonitoringConfig.java`

**职责**: Agent监控配置

**功能**:
- 配置监控参数
- 设置监控阈值
- 配置告警规则

---

## 📁 Layer 11: L1专家Agent (可选架构)

### 11.1 专家Agent列表

#### DishRecommendationAgent.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/agents/DishRecommendationAgent.java`

**职责**: 菜品推荐专家Agent

**功能**:
- 个性化推荐
- 智能搜索
- 菜品对比
- 时段推荐

---

#### UserPreferenceAgent.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/agents/UserPreferenceAgent.java`

**职责**: 用户偏好专家Agent

**功能**:
- 用户资料管理
- 饮食偏好分析
- 健康目标跟踪

---

#### NutritionGuideAgent.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/agents/NutritionGuideAgent.java`

**职责**: 营养指导专家Agent

**功能**:
- 营养成分分析
- 热量计算
- 饮食记录分析

---

#### OrderHelperAgent.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/agents/OrderHelperAgent.java`

**职责**: 订单辅助专家Agent

**功能**:
- 订单创建
- 查询追踪
- 订单管理

---

#### MerchantInfoAgent.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/agents/MerchantInfoAgent.java`

**职责**: 商家信息专家Agent

**功能**:
- 商家查询
- 搜索筛选
- 对比分析

---

#### TimeAwareAgent.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/agents/TimeAwareAgent.java`

**职责**: 时段推荐专家Agent

**功能**:
- 时段判断
- 时段推荐
- 营业时间查询

---

#### LocationServiceAgent.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/agents/LocationServiceAgent.java`

**职责**: 位置服务专家Agent

**功能**:
- 位置查询
- 附近商家推荐
- 距离估算

---

#### CardRendererAgent.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/agents/CardRendererAgent.java`

**职责**: 卡片渲染Agent

**功能**:
- 渲染卡片格式
- 格式化数据
- 生成前端需要的卡片数据

---

#### CustomerServiceAgent.java

**路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/agents/CustomerServiceAgent.java`

**职责**: 客服专家Agent

**功能**:
- 回答常见问题
- 处理投诉建议
- 提供客服支持

---

## 📊 文件统计

### 按层次统计

| 层次 | 文件数量 | 代码量估算 |
|------|---------|-----------|
| Controller层 | 1 | ~600行 |
| Agent层 | 10+ | ~2000行 |
| 工具层 | 30+ | ~3000行 |
| 切面层 | 2 | ~100行 |
| 上下文层 | 1 | ~100行 |
| 监控层 | 4 | ~500行 |
| L1专家Agent | 9 | ~1500行 |
| **总计** | **57+** | **~7800行** |

### 按功能模块统计

| 模块 | 文件数量 |
|------|---------|
| 推荐模块 | 3 |
| 订单模块 | 3 |
| 用户模块 | 6 |
| 营养模块 | 4 |
| 商家模块 | 2 |
| 系统模块 | 4 |
| 监控模块 | 4 |

---

## 🎯 典型调用文件链

### 场景1: 用户查询订单

```
AIStreamController.java (Controller层)
    ↓
StreamingIntelligentAssistantAgent.java (Agent层)
    ↓
OrderQueryTools.java (工具层)
    ↓
OrderService.java (Service层)
    ↓
OrderMapper.java (数据访问层)
    ↓
数据库
```

### 场景2: 用户要求推荐菜品

```
AIStreamController.java (Controller层)
    ↓
StreamingIntelligentAssistantAgent.java (Agent层)
    ↓
RecommendationQueryTools.java (工具层)
    ↓
DishService.java (Service层)
    ↓
DishMapper.java (数据访问层)
    ↓
数据库
```

### 场景3: 卡片生成流程

```
OrderQueryTools.java (@CardType注解)
    ↓
CardTypeAspect.java (AOP拦截)
    ↓
ToolExecutionContext.java (ThreadLocal存储)
    ↓
AIStreamController.java (读取执行信息)
    ↓
buildOrderListCardData() (构建卡片数据)
    ↓
SSE推送到前端
```

---

## 📚 关键设计模式

### 1. 工厂模式

**SupervisorAgentFactory** - 创建Agent实例

### 2. 策略模式

**不同工具类** - 根据用户意图选择不同工具

### 3. AOP切面编程

**CardTypeAspect** - 拦截工具方法，记录执行信息

### 4. ThreadLocal模式

**ToolExecutionContext** - 线程隔离的上下文存储

### 5. 观察者模式

**SSEAgentListener** - 监听Agent执行事件

### 6. 构建器模式

**AiServices.builder()** - 构建Agent实例

---

## 🔧 扩展指南

### 添加新工具类

1. 创建工具类文件（如：`NewFeatureTools.java`）
2. 添加`@Tool`注解的方法
3. 在`LangChain4jStreamingConfig`中注册工具
4. （可选）添加`@CardType`注解以支持卡片生成

### 添加新卡片类型

1. 在`@CardType`注解中定义新类型
2. 在`AIStreamController.buildCardData()`中添加处理逻辑
3. 创建对应的前端卡片组件

### 添加新监控

1. 在`AgentMonitoringConfig`中配置监控参数
2. 在`AgentCallTracer`中添加追踪逻辑
3. 在`AgentPerformanceMonitor`中添加性能指标

---

## 📝 总结

佳食宜选后端AI系统采用**分层架构**设计，共11层57+个文件，约7800行代码。

**核心优势**:
- ✅ **职责清晰**: 每层有明确的职责
- ✅ **易于扩展**: 添加新功能只需添加工具类
- ✅ **流式响应**: SSE实时推送，用户体验好
- ✅ **智能调度**: L3 Agent自动选择合适工具
- ✅ **AOP解耦**: 卡片生成通过切面实现，代码解耦
- ✅ **监控完善**: 完整的调用链追踪和性能监控

**文件位置**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/`
