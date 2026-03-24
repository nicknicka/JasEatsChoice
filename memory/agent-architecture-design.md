# 佳食宜选 Supervisor 子Agent设计方案

> 基于业务场景的完整Agent架构设计
>
> 文档日期：2026-03-24
> 项目：佳食宜选 - 校园餐饮推荐平台

---

## 📐 业务场景分析

### 用户端（C端）核心需求

1. **个性化饮食推荐**
   - 基于口味偏好、历史订单、营养需求
   - 考虑时间、地点、天气、季节

2. **卡路里精准管理**
   - 食物营养分析
   - 每日热量追踪
   - 健康饮食建议

3. **智能订餐**
   - 自然语言下单
   - 智能填写订单信息
   - 订单状态跟踪

4. **收藏与食谱**
   - 菜品收藏
   - 个人食谱管理
   - 食谱分享

5. **社交互动**
   - 评价菜品
   - 分享到社交圈
   - 查看好友动态

### 商家端（B端）核心需求

1. **订单管理**
   - 订单查询、统计
   - 订单状态管理

2. **菜品管理**
   - 菜品上架下架
   - 价格调整
   - 库存管理

3. **经营分析**
   - 销售数据分析
   - 客户评价分析
   - 营业统计

---

## 🎯 架构设计：三层Agent体系

### L1: 基础Agent（原子操作）

**特点：** 单一职责，完成一个明确的任务

| Agent名称 | 能力描述 | 输入 | 输出 |
|----------|---------|------|------|
| **NutritionAnalysisAgent** | 分析食物营养成分 | 食物名称 | 营养信息 |
| **CalorieCalculatorAgent** | 计算每日热量需求 | 身高、体重、年龄、性别、活动量 | 热量目标 |
| **DietTrackerAgent** | 追踪每日饮食摄入 | 食物列表 | 总热量、营养汇总 |
| **BasicRecommendationAgent** | 基础菜品推荐 | 用户偏好、时间、地点 | 推荐列表 |
| **UserPreferenceAgent** | 获取/更新用户偏好 | 用户ID | 偏好信息 |
| **MenuQueryAgent** | 查询商家菜单 | 商家ID、筛选条件 | 菜品列表 |
| **OrderQueryAgent** | 查询订单信息 | 订单ID | 订单详情 |
| **OrderCreateAgent** | 创建订单 | 菜品、地址、电话 | 订单号 |
| **OrderCancelAgent** | 取消订单 | 订单ID | 取消结果 |
| **FavoriteManageAgent** | 收藏管理（增删查） | 用户ID、菜品ID | 操作结果 |
| **ReviewAgent** | 评价菜品 | 订单ID、评分、内容 | 评价结果 |
| **RecipeAgent** | 食谱管理 | 用户ID、食谱内容 | 食谱ID |
| **MerchantStatsAgent** | 商家经营统计 | 商家ID、时间范围 | 统计数据 |
| **MenuManageAgent** | 菜品上下架 | 商家ID、菜品ID、操作 | 操作结果 |
| **PriceAnalysisAgent** | 价格分析 | 菜品ID、市场数据 | 价格建议 |
| **HealthAdviceAgent** | 健康饮食建议 | 营养数据、用户目标 | 建议内容 |

---

### L2: 复合Agent（Workflow）

**特点：** 组合多个L1 Agent，完成复杂任务

#### 1. **SmartRecommendationAgent**（智能推荐）

**内部：Loop Workflow**

```java
/**
 * 智能推荐Agent（会迭代优化）
 *
 * 场景：
 * - "推荐一些健康的菜"
 * - "再推荐一些类似的"
 * - "我不喜欢这些，换一批"
 */
public interface SmartRecommendationAgent {

    @Agent(
        value = """
            智能推荐专家，能够：
            1. 个性化菜品推荐（考虑口味、历史、营养）
            2. 自动迭代优化直到用户满意
            3. 多样化推荐（避免重复）

            **何时调用：**
            - 用户要求推荐
            - 用户表示不满意，需要优化
            - 用户想要发现新菜品
        """,
        outputKey = "optimizedRecommendation"
    )
    String recommend(@V("userRequest") String request);
}

/**
 * 内部实现：Loop Workflow
 */
@Bean
public SmartRecommendationAgent smartRecommendationAgent(
    ChatLanguageModel model,
    BasicRecommendationAgent basicRecAgent,
    RecommendationScorerAgent scorerAgent,
    RecommendationOptimizerAgent optimizerAgent
) {
    // Loop: 推荐评分 → 优化 → 再评分 → 再优化...
    var loop = AgenticServices.loopBuilder()
        .subAgents(scorerAgent, optimizerAgent)
        .maxIterations(3)
        .exitCondition(scope -> scope.readState("score", 0.0) >= 0.8)
        .build();

    // 先推荐，再Loop优化
    return AgenticServices
        .sequenceBuilder(SmartRecommendationAgent.class)
        .subAgents(basicRecAgent, loop)
        .build();
}
```

**流程：**
```
用户请求 → BasicRecommendationAgent（初步推荐）
    ↓
Loop开始 → ScorerAgent（评分）
    ↓
Score < 0.8?
    ↓ Yes
OptimizerAgent（优化推荐）
    ↓
回到评分
    ↓ No
返回优化后的推荐
```

---

#### 2. **MultiDimensionalRecommendationAgent**（多维度推荐）

**内部：Parallel Workflow**

```java
/**
 * 多维度推荐Agent（并行分析）
 *
 * 场景：
 * - "推荐营养又便宜的菜"
 * - "综合考虑口味、营养、价格推荐"
 * - "推荐高评分且健康的菜"
 */
public interface MultiDimensionalRecommendationAgent {

    @Agent(
        value = """
            多维度推荐专家，能够并行分析：
            1. 营养维度（健康度）
            2. 价格维度（实惠度）
            3. 评分维度（受欢迎度）
            4. 口味维度（匹配度）

            综合多个维度给出推荐。

            **何时调用：**
            - 用户有多个维度要求
            - 用户说"又"、"并且"、"同时考虑"
            - 用户要求综合分析
        """,
        outputKey = "comprehensiveRecommendation"
    )
    String recommend(@V("userRequest") String request);
}

/**
 * 内部实现：Parallel Workflow
 */
@Bean
public MultiDimensionalRecommendationAgent multiDimRecAgent(
    ChatLanguageModel model
) {
    var nutritionRec = AgenticServices.agentBuilder(NutritionRecAgent.class)
        .chatModel(model).build();

    var priceRec = AgenticServices.agentBuilder(PriceRecAgent.class)
        .chatModel(model).build();

    var ratingRec = AgenticServices.agentBuilder(RatingRecAgent.class)
        .chatModel(model).build();

    var tasteRec = AgenticServices.agentBuilder(TasteRecAgent.class)
        .chatModel(model).build();

    // 并行执行4个维度的分析
    return AgenticServices
        .parallelBuilder(MultiDimensionalRecommendationAgent.class)
        .subAgents(nutritionRec, priceRec, ratingRec, tasteRec)
        .executor(Executors.newFixedThreadPool(4))
        .output(agenticScope -> {
            // 合并4个维度的结果
            return mergeResults(
                agenticScope.readState("nutritionRec", ""),
                agenticScope.readState("priceRec", ""),
                agenticScope.readState("ratingRec", ""),
                agenticScope.readState("tasteRec", "")
            );
        })
        .build();
}
```

**流程：**
```
用户请求 → 并行执行：
    ├── NutritionRecAgent → 营养维度推荐
    ├── PriceRecAgent → 价格维度推荐
    ├── RatingRecAgent → 评分维度推荐
    └── TasteRecAgent → 口味维度推荐
    ↓
合并结果 → 综合推荐
```

---

#### 3. **BatchNutritionAnalyzerAgent**（批量营养分析）

**内部：Parallel Mapper Workflow**

```java
/**
 * 批量营养分析Agent
 *
 * 场景：
 * - "我今天吃了苹果、香蕉、鸡蛋，总共多少热量？"
 * - "分析这些食物的营养：[列表]"
 * - "帮我记录今天的饮食"
 */
public interface BatchNutritionAnalyzerAgent {

    @Agent(
        value = """
            批量营养分析专家，能够：
            1. 并行分析多个食物的营养成分
            2. 汇总总热量和营养成分
            3. 生成营养报告

            **何时调用：**
            - 用户一次提到多个食物
            - 用户要求记录饮食
            - 用户需要营养汇总
        """,
        outputKey = "nutritionReport"
    )
    String analyze(@V("foodList") List<String> foods);
}

/**
 * 内部实现：Parallel Mapper
 */
@Bean
public BatchNutritionAnalyzerAgent batchNutritionAnalyzerAgent(
    ChatLanguageModel model
) {
    var singleAnalyzer = AgenticServices.agentBuilder(SingleFoodNutritionAgent.class)
        .chatModel(model)
        .build();

    // 并行分析每个食物
    var mapper = AgenticServices
        .parallelMapperBuilder()
        .subAgents(singleAnalyzer)
        .itemsProvider("foodList")
        .executor(Executors.newFixedThreadPool(10))
        .build();

    // 加上汇总
    var summarizer = AgenticServices.agentBuilder(NutritionSummarizerAgent.class)
        .chatModel(model)
        .build();

    return AgenticServices
        .sequenceBuilder(BatchNutritionAnalyzerAgent.class)
        .subAgents(mapper, summarizer)
        .build();
}
```

---

#### 4. **SmartMealPlannerAgent**（智能饮食规划）

**内部：Goal-Oriented Planner**

```java
/**
 * 智能饮食规划Agent
 *
 * 场景：
 * - "帮我规划一周的减肥食谱"
 * - "制定一个月的健康饮食计划"
 * - "我要增肌，规划一下饮食"
 */
public interface SmartMealPlannerAgent {

    @Agent(
        value = """
            智能饮食规划专家，能够：
            1. 分析用户目标和身体状况
            2. 计算每日热量需求
            3. 制定长期饮食计划
            4. 考虑营养均衡和多样性

            **何时调用：**
            - 用户要求规划饮食
            - 用户提到"一周"、"一个月"等时间范围
            - 用户有长期目标（减肥、增肌）
        """,
        outputKey = "mealPlan"
    )
    String plan(@V("userGoal") String goal);
}

/**
 * 内部实现：Goal-Oriented Planner
 */
@Bean
public SmartMealPlannerAgent smartMealPlannerAgent(
    ChatLanguageModel model
) {
    var profileAgent = AgenticServices.agentBuilder(UserProfileAgent.class)
        .chatModel(model).build();

    var calorieAgent = AgenticServices.agentBuilder(CalorieTargetAgent.class)
        .chatModel(model).build();

    var nutritionAgent = AgenticServices.agentBuilder(NutritionRatioAgent.class)
        .chatModel(model).build();

    var mealAgent = AgenticServices.agentBuilder(MealPlanAgent.class)
        .chatModel(model).build();

    // Goal-Oriented: 自动规划最优路径
    return AgenticServices
        .plannerBuilder(SmartMealPlannerAgent.class)
        .subAgents(profileAgent, calorieAgent, nutritionAgent, mealAgent)
        .outputKey("mealPlan")
        .planner(() -> new GoalOrientedPlanner())
        .build();
}
```

---

#### 5. **IntelligentOrderAgent**（智能订餐流程）

**内部：Sequential Workflow**

```java
/**
 * 智能订餐Agent
 *
 * 场景：
 * - "我要宫保鸡丁和米饭，送到学生宿舍3栋"
 * - "帮我点一份营养均衡的午餐"
 * - "我要下单，推荐一些好的"
 */
public interface IntelligentOrderAgent {

    @Agent(
        value = """
            智能订餐专家，能够：
            1. 理解用户下单需求
            2. 智能推荐菜品（如用户未指定）
            3. 自动填写订单信息（地址、电话）
            4. 查询并应用最优优惠
            5. 创建订单并跟踪

            **何时调用：**
            - 用户明确说"下单"、"点餐"、"我要..."
            - 用户询问配送、优惠等信息
            - 用户要求推荐并下单
        """,
        outputKey = "orderResult"
    )
    String order(@V("userRequest") String request);
}

/**
 * 内部实现：Sequential Workflow
 */
@Bean
public IntelligentOrderAgent intelligentOrderAgent(
    ChatLanguageModel model
) {
    // 1. 理解需求Agent
    var requirementAgent = AgenticServices.agentBuilder(OrderRequirementAgent.class)
        .chatModel(model).build();

    // 2. 推荐菜品Agent（如果需要）
    var recommendAgent = AgenticServices.agentBuilder(OrderRecommendAgent.class)
        .chatModel(model).build();

    // 3. 填写信息Agent
    var fillInfoAgent = AgenticServices.agentBuilder(OrderFillInfoAgent.class)
        .chatModel(model).build();

    // 4. 查询优惠Agent
    var couponAgent = AgenticServices.agentBuilder(CouponQueryAgent.class)
        .chatModel(model).build();

    // 5. 创建订单Agent
    var createAgent = AgenticServices.agentBuilder(OrderCreateAgent.class)
        .chatModel(model).build();

    // 顺序执行
    return AgenticServices
        .sequenceBuilder(IntelligentOrderAgent.class)
        .subAgents(
            requirementAgent,
            recommendAgent,  // 条件执行
            fillInfoAgent,
            couponAgent,
            createAgent
        )
        .outputKey("orderResult")
        .build();
}
```

---

#### 6. **HealthGoalTrackerAgent**（健康目标追踪）

**内部：Sequential + Loop Workflow**

```java
/**
 * 健康目标追踪Agent
 *
 * 场景：
 * - "记录今天的饮食，看看是否达标"
 * - "我这周吃得怎么样？"
 * - "帮我追踪减肥进度"
 */
public interface HealthGoalTrackerAgent {

    @Agent(
        value = """
            健康目标追踪专家，能够：
            1. 记录每日饮食
            2. 对比目标与实际摄入
            3. 分析营养状况
            4. 给出调整建议

            **何时调用：**
            - 用户要求记录饮食
            - 用户询问"吃得怎么样"
            - 用户追踪健康目标
        """,
        outputKey = "healthReport"
    )
    String track(@V("userRequest") String request);
}

/**
 * 内部实现：Sequential + Loop
 */
@Bean
public HealthGoalTrackerAgent healthGoalTrackerAgent(
    ChatLanguageModel model
) {
    // 1. 记录饮食
    var recordAgent = AgenticServices.agentBuilder(DietRecordAgent.class)
        .chatModel(model).build();

    // 2. 分析达标情况
    var analyzerAgent = AgenticServices.agentBuilder(GapAnalyzerAgent.class)
        .chatModel(model).build();

    // 3. 给出建议（可能需要迭代优化）
    var advisorAgent = AgenticServices.agentBuilder(HealthAdvisorAgent.class)
        .chatModel(model).build();

    // Loop优化建议
    var loop = AgenticServices.loopBuilder()
        .subAgents(analyzerAgent, advisorAgent)
        .maxIterations(2)
        .exitCondition(scope -> scope.readState("satisfaction", 0.0) >= 0.8)
        .build();

    return AgenticServices
        .sequenceBuilder(HealthGoalTrackerAgent.class)
        .subAgents(recordAgent, loop)
        .build();
}
```

---

#### 7. **MerchantAnalyticsAgent**（商家经营分析）

**内部：Parallel Workflow**

```java
/**
 * 商家经营分析Agent
 *
 * 场景：
 * - "分析我店铺上周的经营情况"
 * - "哪些菜品卖得好？"
 * - "客户对我的评价怎么样？"
 */
public interface MerchantAnalyticsAgent {

    @Agent(
        value = """
            商家经营分析专家，能够：
            1. 分析销售数据（营业额、订单数、客单价）
            2. 分析菜品表现（畅销、滞销）
            3. 分析客户评价（情感分析、关键词）
            4. 给出优化建议

            **何时调用：**
            - 商家询问经营情况
            - 商家要求数据分析
            - 商家需要改进建议
        """,
        outputKey = "analyticsReport"
    )
    String analyze(@V("merchantRequest") String request);
}

/**
 * 内部实现：Parallel Workflow
 */
@Bean
public MerchantAnalyticsAgent merchantAnalyticsAgent(
    ChatLanguageModel model
) {
    var salesAnalyzer = AgenticServices.agentBuilder(SalesAnalyzerAgent.class)
        .chatModel(model).build();

    var menuAnalyzer = AgenticServices.agentBuilder(MenuAnalyzerAgent.class)
        .chatModel(model).build();

    var reviewAnalyzer = AgenticServices.agentBuilder(ReviewAnalyzerAgent.class)
        .chatModel(model).build();

    // 并行分析三个维度
    return AgenticServices
        .parallelBuilder(MerchantAnalyticsAgent.class)
        .subAgents(salesAnalyzer, menuAnalyzer, reviewAnalyzer)
        .executor(Executors.newFixedThreadPool(3))
        .output(agenticScope -> {
            // 综合三个维度的分析结果
            return mergeAnalytics(
                agenticScope.readState("salesAnalysis", ""),
                agenticScope.readState("menuAnalysis", ""),
                agenticScope.readState("reviewAnalysis", "")
            );
        })
        .build();
}
```

---

### L3: 领域Supervisor（协调L2 Agent）

#### 1. **UserDiningSupervisor**（用户餐饮Supervisor）

```java
/**
 * 用户餐饮领域的Supervisor
 *
 * 协调：推荐、营养、订餐等相关Agent
 */
@Bean
public UserDiningSupervisor userDiningSupervisor(
    ChatLanguageModel supervisorModel,
    ChatLanguageModel agentModel
) {
    // 准备L2 Agent
    var smartRec = smartRecommendationAgent(agentModel);
    var multiDimRec = multiDimensionalRecAgent(agentModel);
    var batchAnalyzer = batchNutritionAnalyzerAgent(agentModel);
    var smartPlanner = smartMealPlannerAgent(agentModel);
    var intelligentOrder = intelligentOrderAgent(agentModel);
    var healthTracker = healthGoalTrackerAgent(agentModel);

    // 准备L1 Agent
    var nutritionAgent = nutritionAnalysisAgent(agentModel);
    var favoriteAgent = favoriteManageAgent(agentModel);
    var recipeAgent = recipeAgent(agentModel);

    return AgenticServices
        .supervisorBuilder()
        .chatModel(supervisorModel)
        .subAgents(
            // L2 复杂Agent
            smartRec,
            multiDimRec,
            batchAnalyzer,
            smartPlanner,
            intelligentOrder,
            healthTracker,

            // L1 基础Agent
            nutritionAgent,
            favoriteAgent,
            recipeAgent
        )
        .supervisorContext("""
            你是"佳食宜选"的用户餐饮专家Supervisor。

            # 你的高级工具（L2 Agent）
            - SmartRecommendation: 智能推荐（会自动优化）
            - MultiDimensionalRec: 多维度推荐（并行分析营养、价格、评分）
            - BatchNutritionAnalyzer: 批量营养分析（并行处理多个食物）
            - SmartMealPlanner: 智能饮食规划（自动规划最优路径）
            - IntelligentOrder: 智能订餐（完整流程）
            - HealthGoalTracker: 健康目标追踪（记录+分析+建议）

            # 你的基础工具（L1 Agent）
            - NutritionAgent: 营养分析
            - FavoriteAgent: 收藏管理
            - RecipeAgent: 食谱管理

            # 决策策略
            1. 简单推荐 → SmartRecommendation
            2. 多维度要求 → MultiDimensionalRec
            3. 批量食物 → BatchNutritionAnalyzer
            4. 长期规划 → SmartMealPlanner
            5. 下单 → IntelligentOrder
            6. 追踪目标 → HealthGoalTracker
            7. 简单查询 → 直接调用L1 Agent
        """)
        .build();
}
```

---

#### 2. **MerchantManagementSupervisor**（商家管理Supervisor）

```java
/**
 * 商家管理领域的Supervisor
 *
 * 协调：订单、菜品、分析等相关Agent
 */
@Bean
public MerchantManagementSupervisor merchantManagementSupervisor(
    ChatLanguageModel supervisorModel,
    ChatLanguageModel agentModel
) {
    // 准备L2 Agent
    var merchantAnalytics = merchantAnalyticsAgent(agentModel);

    // 准备L1 Agent
    var orderQuery = orderQueryAgent(agentModel);
    var menuManage = menuManageAgent(agentModel);
    var priceAnalysis = priceAnalysisAgent(agentModel);

    return AgenticServices
        .supervisorBuilder()
        .chatModel(supervisorModel)
        .subAgents(
            merchantAnalytics,
            orderQuery,
            menuManage,
            priceAnalysis
        )
        .supervisorContext("""
            你是"佳食宜选"的商家管理专家Supervisor。

            # 你的工具
            - MerchantAnalytics: 经营分析（销售、菜品、评价并行分析）
            - OrderQuery: 订单查询
            - MenuManage: 菜品管理
            - PriceAnalysis: 价格分析

            # 决策策略
            1. 经营分析 → MerchantAnalytics
            2. 订单相关 → OrderQuery
            3. 菜品相关 → MenuManage
            4. 价格相关 → PriceAnalysis
        """)
        .build();
}
```

---

### L4: 主Supervisor（总协调）

#### **MainSupervisor**（智能助手总协调）

```java
/**
 * 主Supervisor：协调所有领域
 */
@Bean
public SupervisorAgent mainSupervisor(
    ChatLanguageModel mainSupervisorModel,
    UserDiningSupervisor userDiningSupervisor,
    MerchantManagementSupervisor merchantManagementSupervisor
) {
    return AgenticServices
        .supervisorBuilder()
        .chatModel(mainSupervisorModel)
        .subAgents(
            userDiningSupervisor,      // L3: 用户餐饮领域
            merchantManagementSupervisor // L3: 商家管理领域
        )
        .supervisorContext("""
            你是"佳食宜选"的智能助手总协调者。

            # 你的领域Supervisor

            ## 1. UserDiningSupervisor（用户餐饮）
            处理用户端的所有餐饮相关需求：
            - 智能推荐（会自动优化）
            - 营养分析（支持批量）
            - 饮食规划（长期计划）
            - 智能订餐（完整流程）
            - 健康追踪（目标管理）

            ## 2. MerchantManagementSupervisor（商家管理）
            处理商家端的所有管理需求：
            - 经营分析（多维度分析）
            - 订单管理
            - 菜品管理
            - 价格分析

            # 决策策略
            根据用户身份和需求，分发给对应的领域Supervisor。
        """)
        .build();
}
```

---

## 📊 完整的Agent层次结构

```
MainSupervisor (总协调)
    │
    ├── UserDiningSupervisor (用户餐饮领域)
    │   │
    │   ├── L2: SmartRecommendationAgent (Loop)
    │   │   └── BasicRec → Scorer → Optimizer → Loop
    │   │
    │   ├── L2: MultiDimensionalRecAgent (Parallel)
    │   │   ├── NutritionRec
    │   │   ├── PriceRec
    │   │   ├── RatingRec
    │   │   └── TasteRec
    │   │
    │   ├── L2: BatchNutritionAnalyzerAgent (Mapper)
    │   │   └── 并行分析多个食物 → 汇总
    │   │
    │   ├── L2: SmartMealPlannerAgent (Goal-Oriented)
    │   │   └── 自动规划最优路径
    │   │
    │   ├── L2: IntelligentOrderAgent (Sequential)
    │   │   └── 需求→推荐→填单→优惠→下单
    │   │
    │   ├── L2: HealthGoalTrackerAgent (Sequential+Loop)
    │   │   └── 记录→分析→建议→Loop优化
    │   │
    │   ├── L1: NutritionAnalysisAgent
    │   ├── L1: FavoriteManageAgent
    │   └── L1: RecipeAgent
    │
    └── MerchantManagementSupervisor (商家管理领域)
        │
        ├── L2: MerchantAnalyticsAgent (Parallel)
        │   ├── SalesAnalyzer
        │   ├── MenuAnalyzer
        │   └── ReviewAnalyzer
        │
        ├── L1: OrderQueryAgent
        ├── L1: MenuManageAgent
        └── L1: PriceAnalysisAgent
```

---

## 🎯 实施建议

### 阶段1：L1基础Agent（1周）

**优先级最高：**
- [ ] NutritionAnalysisAgent
- [ ] BasicRecommendationAgent
- [ ] OrderQueryAgent
- [ ] OrderCreateAgent
- [ ] UserPreferenceAgent

**理由：** 这些是最常用的功能，必须先实现

---

### 阶段2：L2复合Agent（2周）

**按优先级：**
1. [ ] SmartRecommendationAgent（Loop）
   - 立即提升推荐质量

2. [ ] IntelligentOrderAgent（Sequential）
   - 完整的下单体验

3. [ ] MultiDimensionalRecAgent（Parallel）
   - 多维度推荐能力

4. [ ] BatchNutritionAnalyzerAgent（Mapper）
   - 批量营养分析

**理由：** 这些Agent能显著提升用户体验

---

### 阶段3：L3领域Supervisor（1周）

- [ ] UserDiningSupervisor
- [ ] MerchantManagementSupervisor

**理由：** 统一协调各领域Agent

---

### 阶段4：L4主Supervisor（1周）

- [ ] MainSupervisor

**理由：** 统一入口，智能路由

---

## 📋 Agent清单

### 需要实现的Agent总数：**30+**

#### L1: 基础Agent（16个）
1. NutritionAnalysisAgent
2. CalorieCalculatorAgent
3. DietTrackerAgent
4. BasicRecommendationAgent
5. UserPreferenceAgent
6. MenuQueryAgent
7. OrderQueryAgent
8. OrderCreateAgent
9. OrderCancelAgent
10. FavoriteManageAgent
11. ReviewAgent
12. RecipeAgent
13. MerchantStatsAgent
14. MenuManageAgent
15. PriceAnalysisAgent
16. HealthAdviceAgent

#### L2: 复合Agent（7个）
1. SmartRecommendationAgent (Loop)
2. MultiDimensionalRecAgent (Parallel)
3. BatchNutritionAnalyzerAgent (Mapper)
4. SmartMealPlannerAgent (Goal-Oriented)
5. IntelligentOrderAgent (Sequential)
6. HealthGoalTrackerAgent (Sequential+Loop)
7. MerchantAnalyticsAgent (Parallel)

#### L3: 领域Supervisor（2个）
1. UserDiningSupervisor
2. MerchantManagementSupervisor

#### L4: 主Supervisor（1个）
1. MainSupervisor

---

## 🚀 下一步行动

1. **评审Agent设计**：确认是否覆盖所有业务场景
2. **优先级排序**：根据业务价值决定实施顺序
3. **开始实施**：从L1基础Agent开始
4. **逐步组合**：构建L2、L3、L4

**预计总工时：** 5-6周
**建议团队规模：** 2-3人

---

**文档维护：** 根据实施进度持续更新
**最后更新：** 2026-03-24
**作者：** Claude Code Analysis
**状态：** 待评审
