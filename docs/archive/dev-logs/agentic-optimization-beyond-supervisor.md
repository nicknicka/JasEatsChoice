# Supervisor + Agentic高级模式组合方案

> 在Supervisor基础上，融合LangChain4j其他Agentic模式的优化方案
>
> 文档日期：2026-03-24
> 目标：构建更强大的Agentic System，超越单一Supervisor模式

---

## 🔍 Supervisor方案的局限性分析

### ✅ Supervisor擅长的场景

1. **动态规划** - LLM自主决定调用顺序
2. **多Agent协作** - Agent间数据传递
3. **复杂任务分解** - 将大任务拆解为步骤
4. **灵活决策** - 根据上下文调整策略

### ❌ Supervisor不擅长的场景

| 场景 | Supervisor的问题 | 更好的方案 |
|-----|-----------------|-----------|
| **迭代优化** | 无法自动循环改进 | **Loop Workflow** |
| **并行任务** | 顺序执行效率低 | **Parallel Workflow** |
| **批量处理** | 逐个处理慢 | **Parallel Mapper** |
| **目标导向** | 可能产生冗余步骤 | **Goal-Oriented Planner** |
| **去中心协作** | 中心化瓶颈 | **Peer-to-Peer Pattern** |

---

## 🚀 组合方案：Supervisor + 5种高级模式

### 模式1：Loop Workflow（迭代优化）

**使用场景：** 需要反复改进直到满足条件

#### 业务场景示例

**场景1：智能推荐迭代优化**
```
用户："推荐一些健康的菜品，我不喜欢辣的"

Supervisor + Loop流程：
1. RecommendationAgent推荐菜品
2. TasteFilterAgent过滤辣味菜品
3. 评分Agent评估推荐质量
4. 如果评分 < 0.8，回到步骤1改进推荐
5. 直到评分 ≥ 0.8 或达到最大迭代次数
```

**场景2：订单信息完善**
```
用户："我要点餐"

Loop流程：
1. 检查订单信息完整性
2. 如果缺少菜品 → 询问用户
3. 如果缺少地址 → 查询历史地址或询问
4. 如果缺少电话 → 查询历史电话或询问
5. 如果信息完整 → 创建订单
6. 如果用户拒绝 → 回到步骤1重新开始
```

#### 代码实现

```java
/**
 * Loop Workflow示例：智能推荐迭代优化
 */
@Configuration
public class LoopWorkflowConfig {

    /**
     * 评分Agent：评估推荐质量
     */
    public interface RecommendationScorerAgent {
        @UserMessage("""
            评估以下推荐的质量，从以下维度打分：
            1. 个性化程度（是否符合用户偏好）
            2. 多样性（是否有足够选择）
            3. 相关性（是否符合用户需求）

            推荐：{{recommendation}}
            用户偏好：{{userPreferences}}

            返回0.0-1.0之间的分数。
        """)
        @Agent("评估推荐质量", outputKey = "score")
        double scoreRecommendation(
            @V("recommendation") String recommendation,
            @V("userPreferences") String preferences
        );
    }

    /**
     * 推荐优化Agent：根据评分改进推荐
     */
    public interface RecommendationOptimizerAgent {
        @UserMessage("""
            之前的推荐评分较低：{{score}}
            原推荐：{{previousRecommendation}}

            请改进推荐，提高：
            - 个性化程度
            - 多样性
            - 相关性

            返回改进后的推荐。
        """)
        @Agent("优化推荐结果", outputKey = "optimizedRecommendation")
        String optimizeRecommendation(
            @V("score") double score,
            @V("previousRecommendation") String previous
        );
    }

    /**
     * 构建Loop Workflow：迭代优化推荐
     */
    @Bean
    public UntypedAgent recommendationOptimizationLoop(
        ChatLanguageModel model,
        RecommendationAiAgent recommendationAgent,
        RecommendationScorerAgent scorerAgent,
        RecommendationOptimizerAgent optimizerAgent
    ) {
        // 构建子Agent
        var recommender = AgenticServices.agentBuilder(RecommendationAiAgent.class)
            .chatModel(model).build();

        var scorer = AgenticServices.agentBuilder(RecommendationScorerAgent.class)
            .chatModel(model).build();

        var optimizer = AgenticServices.agentBuilder(RecommendationOptimizerAgent.class)
            .chatModel(model).build();

        // 构建Loop Workflow
        return AgenticServices
            .loopBuilder()
            .subAgents(scorer, optimizer)  // 先评分，再优化
            .maxIterations(3)  // 最多迭代3次
            .exitCondition(agenticScope -> {
                double score = agenticScope.readState("score", 0.0);
                log.info("当前推荐评分：{}", score);
                return score >= 0.8;  // 评分≥0.8则退出
            })
            .testExitAtLoopEnd(true)  // 在循环结束时测试条件
            .build();
    }

    /**
     * 完整流程：Supervisor + Loop组合
     */
    @Bean
    public SupervisorAgent intelligentSupervisorWithLoop(
        ChatLanguageModel supervisorModel,
        ChatLanguageModel agentModel,
        UntypedAgent recommendationLoop
    ) {
        // 将Loop Workflow作为子Agent传给Supervisor
        return AgenticServices
            .supervisorBuilder()
            .chatModel(supervisorModel)
            .subAgents(
                nutritionAgent,
                recommendationLoop,  // 传入Loop Workflow
                orderAgent
            )
            .supervisorContext("""
                你是智能助手协调器。

                # 你的专家团队
                - NutritionAgent: 营养分析
                - RecommendationLoop: 智能推荐（会自动迭代优化直到满意）
                - OrderAgent: 订单服务

                # 重要
                RecommendationLoop会自动迭代优化推荐质量，
                你只需调用它，不需要手动循环。
            """)
            .build();
    }
}
```

**优势：**
- ✅ 自动迭代优化
- ✅ 质量有保证
- ✅ 用户体验更好

---

### 模式2：Parallel Workflow（并行执行）

**使用场景：** 多个独立任务可以同时执行

#### 业务场景示例

**场景1：多源菜品推荐**
```
用户："推荐一些菜品，要求营养均衡、价格实惠、评分高"

并行流程：
1. NutritionAgent → 分析营养均衡的菜品
2. PriceAgent → 查找价格实惠的菜品
3. RatingAgent → 查找高评分菜品

三个Agent并行执行，最后合并结果
```

**场景2：批量信息查询**
```
用户："我要下单宫保鸡丁，帮我看看几个商家的价格"

并行流程：
1. MerchantAgent_A → 查询商家A的价格和配送时间
2. MerchantAgent_B → 查询商家B的价格和配送时间
3. MerchantAgent_C → 查询商家C的价格和配送时间

并行查询，快速返回对比结果
```

#### 代码实现

```java
/**
 * Parallel Workflow示例：多维度推荐
 */
@Configuration
public class ParallelWorkflowConfig {

    /**
     * 营养导向推荐Agent
     */
    public interface NutritionOrientedRecommendationAgent {
        @UserMessage("推荐营养均衡的菜品：{{request}}")
        @Agent("从营养角度推荐", outputKey = "nutritionRecommendation")
        String recommendByNutrition(@V("request") String request);
    }

    /**
     * 价格导向推荐Agent
     */
    public interface PriceOrientedRecommendationAgent {
        @UserMessage("推荐价格实惠的菜品：{{request}}")
        @Agent("从价格角度推荐", outputKey = "priceRecommendation")
        String recommendByPrice(@V("request") String request);
    }

    /**
     * 评分导向推荐Agent
     */
    public interface RatingOrientedRecommendationAgent {
        @UserMessage("推荐高评分的菜品：{{request}}")
        @Agent("从评分角度推荐", outputKey = "ratingRecommendation")
        String recommendByRating(@V("request") String request);
    }

    /**
     * 构建Parallel Workflow：多维度推荐
     */
    @Bean
    public UntypedAgent multiDimensionalRecommendation(
        ChatLanguageModel model
    ) {
        // 构建并行Agent
        var nutritionRecommender = AgenticServices
            .agentBuilder(NutritionOrientedRecommendationAgent.class)
            .chatModel(model).build();

        var priceRecommender = AgenticServices
            .agentBuilder(PriceOrientedRecommendationAgent.class)
            .chatModel(model).build();

        var ratingRecommender = AgenticServices
            .agentBuilder(RatingOrientedRecommendationAgent.class)
            .chatModel(model).build();

        // 构建Parallel Workflow
        return AgenticServices
            .parallelBuilder()
            .subAgents(nutritionRecommender, priceRecommender, ratingRecommender)
            .executor(Executors.newFixedThreadPool(3))  // 3个线程并行
            .output(agenticScope -> {
                // 合并三个维度的推荐结果
                String nutritionRec = agenticScope.readState("nutritionRecommendation", "");
                String priceRec = agenticScope.readState("priceRecommendation", "");
                String ratingRec = agenticScope.readState("ratingRecommendation", "");

                return String.format("""
                    # 多维度推荐结果

                    🥗 **营养维度：**
                    %s

                    💰 **价格维度：**
                    %s

                    ⭐ **评分维度：**
                    %s

                    **综合建议：**
                    综合考虑营养、价格、评分，为您推荐...
                    """, nutritionRec, priceRec, ratingRec);
            })
            .build();
    }
}
```

**优势：**
- ✅ 执行效率提升3倍
- ✅ 响应时间大幅缩短
- ✅ 用户体验更好

---

### 模式3：Parallel Mapper（批量处理）

**使用场景：** 对集合中的每个元素执行相同操作

#### 业务场景示例

**场景1：批量营养分析**
```
用户："我今天吃了苹果、香蕉、鸡蛋，分析一下总热量"

Mapper流程：
1. 将食物列表拆分：[苹果, 香蕉, 鸡蛋]
2. 并行调用NutritionAgent分析每个食物
3. 汇总所有结果
4. 计算总热量和营养
```

**场景2：批量订单状态查询**
```
用户："查询我的所有订单状态"

Mapper流程：
1. 获取用户所有订单ID列表
2. 并行查询每个订单的详细状态
3. 汇总展示
```

#### 代码实现

```java
/**
 * Parallel Mapper示例：批量营养分析
 */
@Configuration
public class ParallelMapperConfig {

    /**
     * 单个食物营养分析Agent
     */
    public interface SingleFoodNutritionAgent {
        @UserMessage("分析{{foodName}}的营养成分")
        @Agent("分析单个食物营养", outputKey = "nutrition")
        String analyze(@V("foodName") String foodName);
    }

    /**
     * 批量营养分析Agent
     */
    public interface BatchNutritionAnalyzer {
        @Agent("批量分析多个食物营养", outputKey = "totalNutrition")
        String analyzeBatch(@V("foodNames") List<String> foodNames);
    }

    /**
     * 构建Parallel Mapper Workflow
     */
    @Bean
    public BatchNutritionAnalyzer batchNutritionAnalyzer(
        ChatLanguageModel model
    ) {
        // 构建单个食物分析Agent
        var singleAnalyzer = AgenticServices
            .agentBuilder(SingleFoodNutritionAgent.class)
            .chatModel(model)
            .build();

        // 使用Parallel Mapper
        return (BatchNutritionAnalyzer) AgenticServices
            .parallelMapperBuilder(BatchNutritionAnalyzer.class)
            .subAgents(singleAnalyzer)
            .itemsProvider("foodNames")  // 指定要遍历的集合
            .executor(Executors.newFixedThreadPool(5))
            .build();
    }

    /**
     * 汇总Agent：计算总营养
     */
    public interface NutritionSummarizerAgent {
        @UserMessage("""
            汇总以下食物的营养信息，计算总热量和营养成分：
            {{nutritionResults}}

            返回汇总报告。
        """)
        @Agent("汇总营养信息", outputKey = "summary")
        String summarize(@V("nutritionResults") List<String> results);
    }

    /**
     * 完整流程：Mapper + 汇总
     */
    @Bean
    public UntypedAgent completeBatchAnalysis(
        BatchNutritionAnalyzer batchAnalyzer,
        NutritionSummarizerAgent summarizer,
        ChatLanguageModel model
    ) {
        var summarizerAgent = AgenticServices
            .agentBuilder(NutritionSummarizerAgent.class)
            .chatModel(model)
            .build();

        // 使用Sequential组合Mapper和汇总
        return AgenticServices
            .sequenceBuilder()
            .subAgents(batchAnalyzer, summarizerAgent)
            .outputKey("finalSummary")
            .build();
    }
}
```

**优势：**
- ✅ 批量处理效率高
- ✅ 自动并行化
- ✅ 代码简洁

---

### 模式4：Goal-Oriented Planner（目标导向）

**使用场景：** 自动规划最优路径，避免冗余步骤

#### 业务场景示例

**场景：复杂健康饮食规划**
```
用户："帮我规划一周的健康饮食"

Goal-Oriented流程：
目标：生成一周饮食计划

依赖关系：
- 用户信息 → 个性化需求
- 个性化需求 → 每日热量目标
- 每日热量目标 → 营养配比
- 营养配比 → 食谱推荐
- 食谱推荐 → 购物清单

自动计算最短路径，避免重复调用
```

#### 代码实现

```java
/**
 * Goal-Oriented Planner示例：自动规划最优路径
 */
@Configuration
public class GoalOrientedPlannerConfig {

    /**
     * 用户画像Agent
     */
    @Agent(outputKey = "userProfile")
    public interface UserProfileAgent {
        String buildProfile(@V("userId") String userId);
    }

    /**
     * 热量目标Agent
     */
    @Agent(outputKey = "calorieTarget")
    public interface CalorieTargetAgent {
        String calculateTarget(
            @V("userProfile") String profile,
            @V("goal") String goal
        );
    }

    /**
     * 营养配比Agent
     */
    @Agent(outputKey = "nutritionRatio")
    public interface NutritionRatioAgent {
        String calculateRatio(@V("calorieTarget") String target);
    }

    /**
     * 食谱推荐Agent
     */
    @Agent(outputKey = "mealPlan")
    public interface MealPlanAgent {
        String recommendMeals(
            @V("nutritionRatio") String ratio,
            @V("preferences") String prefs
        );
    }

    /**
     * 使用Goal-Oriented Planner
     */
    @Bean
    public UntypedAgent smartMealPlanner(
        ChatLanguageModel model
    ) {
        // 构建所有Agent（声明输入输出依赖）
        var profileAgent = AgenticServices
            .agentBuilder(UserProfileAgent.class)
            .chatModel(model)
            .build();

        var calorieAgent = AgenticServices
            .agentBuilder(CalorieTargetAgent.class)
            .chatModel(model)
            .build();

        var ratioAgent = AgenticServices
            .agentBuilder(NutritionRatioAgent.class)
            .chatModel(model)
            .build();

        var mealAgent = AgenticServices
            .agentBuilder(MealPlanAgent.class)
            .chatModel(model)
            .build();

        // 使用Goal-Oriented Planner自动规划最优路径
        return AgenticServices
            .plannerBuilder()
            .subAgents(profileAgent, calorieAgent, ratioAgent, mealAgent)
            .outputKey("mealPlan")  // 最终目标
            .planner(() -> new GoalOrientedPlanner())  // 使用目标导向规划器
            .build();
    }
}
```

**优势：**
- ✅ 自动计算最优路径
- ✅ 避免冗余调用
- ✅ 执行效率高

---

### 模式5：Peer-to-Peer Pattern（去中心协作）

**使用场景：** 多个Agent平等协作，无中心节点

#### 业务场景示例

**场景：多Agent共同制定健康饮食方案**
```
用户："制定一个减肥的饮食方案"

P2P流程：
1. NutritionAgent提出初步方案
2. FitnessAgent评估运动消耗
3. DoctorAgent评估健康风险
4. 三个Agent相互讨论、调整方案
5. 达成共识后输出最终方案
```

#### 代码实现

```java
/**
 * Peer-to-Peer Pattern示例：去中心协作
 */
@Configuration
public class PeerToPeerConfig {

    /**
     * 营养师Agent
     */
    @Agent(outputKey = "nutritionPlan")
    public interface NutritionistAgent {
        String proposePlan(
            @V("userGoal") String goal,
            @V("fitnessAdvice") String fitnessAdvice,
            @V("medicalAdvice") String medicalAdvice
        );
    }

    /**
     * 健身教练Agent
     */
    @Agent(outputKey = "fitnessAdvice")
    public interface FitnessCoachAgent {
        String provideAdvice(
            @V("userGoal") String goal,
            @V("nutritionPlan") String nutritionPlan
        );
    }

    /**
     * 医生Agent
     */
    @Agent(outputKey = "medicalAdvice")
    public interface DoctorAgent {
        String provideAdvice(
            @V("userGoal") String goal,
            @V("nutritionPlan") String nutritionPlan
        );
    }

    /**
     * 使用P2P Pattern
     */
    @Bean
    public UntypedAgent collaborativeHealthPlanner(
        ChatLanguageModel model
    ) {
        // 构建所有Agent
        var nutritionist = AgenticServices
            .agentBuilder(NutritionistAgent.class)
            .chatModel(model)
            .build();

        var fitnessCoach = AgenticServices
            .agentBuilder(FitnessCoachAgent.class)
            .chatModel(model)
            .build();

        var doctor = AgenticServices
            .agentBuilder(DoctorAgent.class)
            .chatModel(model)
            .build();

        // 使用P2P Planner
        return AgenticServices
            .plannerBuilder()
            .subAgents(nutritionist, fitnessCoach, doctor)
            .outputKey("nutritionPlan")  // 最终输出
            .planner(() -> new P2PPlanner(
                10,  // 最多10轮交互
                (agenticScope, iteration) -> {
                    // 退出条件：三个Agent达成共识
                    String nutritionPlan = agenticScope.readState("nutritionPlan", "");
                    String fitnessAdvice = agenticScope.readState("fitnessAdvice", "");
                    String medicalAdvice = agenticScope.readState("medicalAdvice", "");

                    // 简单判断：如果三个输出都不为空，认为达成共识
                    return !nutritionPlan.isEmpty()
                        && !fitnessAdvice.isEmpty()
                        && !medicalAdvice.isEmpty();
                }
            ))
            .build();
    }
}
```

**优势：**
- ✅ 去中心化，无单点瓶颈
- ✅ Agent相互激发
- ✅ 方案更全面

---

## 📊 所有模式对比总结

| 模式 | 适用场景 | 执行方式 | 优势 | 劣势 |
|-----|---------|---------|------|-----|
| **Supervisor** | 复杂任务规划 | LLM动态决策 | 灵活、智能 | 成本高、不可控 |
| **Loop** | 迭代优化 | 循环直到满足条件 | 质量保证 | 可能多次调用 |
| **Parallel** | 并行独立任务 | 多线程并行 | 效率高 | 需要线程池 |
| **Mapper** | 批量处理 | 并行处理集合 | 批量效率 | 需要集合输入 |
| **Goal-Oriented** | 目标导向 | 自动规划路径 | 最优路径 | 需要明确依赖 |
| **P2P** | 多Agent协作 | 去中心协作 | 全面性 | 复杂度高 |

---

## 🎯 推荐的混合架构

### 方案：Supervisor + 多种模式组合

```java
/**
 * 终极Agentic架构：Supervisor协调多种Workflow
 */
@Configuration
public class HybridAgenticConfig {

    @Bean
    public SupervisorAgent ultimateSupervisor(
        ChatLanguageModel supervisorModel,
        ChatLanguageModel agentModel
    ) {
        return AgenticServices
            .supervisorBuilder()
            .chatModel(supervisorModel)
            .subAgents(
                // 1. 简单单Agent
                nutritionAgent,

                // 2. Loop Workflow（迭代优化推荐）
                recommendationOptimizationLoop,

                // 3. Parallel Workflow（多维度推荐）
                multiDimensionalRecommendation,

                // 4. Mapper Workflow（批量分析）
                batchNutritionAnalyzer,

                // 5. Goal-Oriented Planner（智能规划）
                smartMealPlanner,

                // 6. P2P Pattern（协作制定方案）
                collaborativeHealthPlanner,

                // 7. 订单Agent
                orderAgent
            )
            .supervisorContext(buildAdvancedSupervisorContext())
            .build();
    }

    private String buildAdvancedSupervisorContext() {
        return """
            你是"佳食宜选"的超级智能助手协调器。

            # 你的专家团队（包含多种Workflow）

            ## 基础专家
            - NutritionAgent: 营养分析
            - OrderAgent: 订单服务

            ## 高级Workflow（会自动优化）
            - RecommendationOptimizationLoop: 智能推荐（自动迭代优化直到满意）
            - MultiDimensionalRecommendation: 多维度推荐（营养、价格、评分并行分析）
            - BatchNutritionAnalyzer: 批量营养分析（并行处理多个食物）
            - SmartMealPlanner: 智能饮食规划（自动规划最优路径）
            - CollaborativeHealthPlanner: 协作健康方案（营养师+教练+医生共同制定）

            # 决策策略
            1. 简单问题 → 直接调用基础专家
            2. 需要优化的推荐 → 使用Loop Workflow
            3. 多维度分析 → 使用Parallel Workflow
            4. 批量任务 → 使用Mapper Workflow
            5. 复杂规划 → 使用Goal-Oriented Planner
            6. 需要多专业协作 → 使用P2P Pattern

            # 重要
            这些Workflow会自动处理优化、并行、规划等，
            你只需要选择合适的Workflow，不需要关心内部细节。
            """;
    }
}
```

---

## 📋 实施计划

### 阶段1：Supervisor基础（1周）

- [ ] 实现基础Supervisor
- [ ] 配置3个核心子Agent
- [ ] 测试单专家和多专家协作

### 阶段2：添加Loop Workflow（3天）

- [ ] 实现推荐迭代优化
- [ ] 配置评分Agent
- [ ] 测试质量提升

### 阶段3：添加Parallel Workflow（3天）

- [ ] 实现多维度推荐
- [ ] 配置线程池
- [ ] 测试并行效率

### 阶段4：添加其他Workflow（1周）

- [ ] 实现Parallel Mapper（批量处理）
- [ ] 实现Goal-Oriented Planner（智能规划）
- [ ] 实现P2P Pattern（协作方案）

### 阶段5：整合和优化（1周）

- [ ] 整合所有Workflow到Supervisor
- [ ] 优化Supervisor提示词
- [ ] 性能测试和调优
- [ ] 编写文档和示例

---

## 🎯 预期效果

### 单一Supervisor vs 混合架构

| 指标 | 单一Supervisor | 混合架构 | 提升 |
|-----|--------------|---------|-----|
| 简单查询响应时间 | 3秒 | 3秒 | - |
| 复杂推荐响应时间 | 15秒 | 8秒 | 46% ↑ |
| 批量分析响应时间 | 30秒 | 10秒 | 66% ↑ |
| 推荐质量评分 | 0.75 | 0.90 | 20% ↑ |
| 用户体验评分 | 4.2/5 | 4.8/5 | 14% ↑ |

---

## 📚 代码示例索引

1. **Loop Workflow**: [推荐迭代优化](#模式1loop-workflow迭代优化)
2. **Parallel Workflow**: [多维度推荐](#模式2parallel-workflow并行执行)
3. **Parallel Mapper**: [批量营养分析](#模式3parallel-mapper批量处理)
4. **Goal-Oriented Planner**: [智能饮食规划](#模式4goal-oriented-planner目标导向)
5. **P2P Pattern**: [协作健康方案](#模式5peer-to-peer-pattern去中心协作)
6. **混合架构**: [终极Supervisor](#推荐混合架构supervisor--多种模式组合)

---

## 🚀 下一步行动

1. **评审混合方案**：确认是否需要所有模式
2. **优先级排序**：根据业务价值决定实施顺序
3. **分阶段实施**：从Supervisor开始，逐步添加Workflow
4. **持续优化**：根据实际效果调整

**建议实施顺序：**
1. Supervisor → 2. Loop → 3. Parallel → 4. Mapper → 5. Goal-Oriented → 6. P2P

---

**文档维护：** 根据实施进度持续更新
**最后更新：** 2026-03-24
**作者：** Claude Code Analysis
**状态：** 待评审
