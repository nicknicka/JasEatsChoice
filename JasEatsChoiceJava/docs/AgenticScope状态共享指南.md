# AgenticScope 状态共享指南

## 概述

AgenticScope 是 LangChain4j Agentic 模式的核心机制，用于在多个 Agent 之间共享状态和数据。

## 已配置的 L1 Agent 输出键

| L1 Agent | Output Key | 输出内容 |
|----------|------------|----------|
| **NutritionGuideAgent** | `nutritionInfo` | 营养分析结果（热量、营养成分等） |
| **DishRecommendationAgent** | `recommendations` | 菜品推荐列表 |
| **UserPreferenceAgent** | `userPreferences` | 用户偏好设置（饮食喜好、禁忌等） |
| **MerchantInfoAgent** | `merchantInfo` | 商家信息（评分、营业时间等） |
| **OrderHelperAgent** | `orderInfo` | 订单信息（状态、金额等） |

## 状态传递方式

### 方式1：通过 Agent 调用链（推荐）

L2 Agent 将 L1 Agent 作为 tool 注入，当 LLM 调用 L1 Agent 时，返回值会自动传递给 L2 Agent。

```java
// L2 Agent 配置
@Bean
public SmartRecommendationAgent smartRecommendationAgent(
        ChatModel chatModel,
        UserPreferenceAgent userPreferenceAgent,  // 注入 L1 Agent
        DishRecommendationAgent dishRecommendationAgent) {

    return AgenticServices.agentBuilder(SmartRecommendationAgent.class)
            .chatModel(chatModel)
            .tools(
                userPreferenceAgent,      // 作为 tool 注入
                dishRecommendationAgent   // 作为 tool 注入
            )
            .build();
}
```

**工作流程**：
1. 用户向 SmartRecommendationAgent 提问
2. LLM 决定需要调用 UserPreferenceAgent
3. 自动调用 UserPreferenceAgent
4. UserPreferenceAgent 的返回值自动传递给 LLM
5. LLM 结合返回值生成最终回复

### 方式2：通过 AgenticScope 手动读取（高级）

在 Agent 工具中直接访问 AgenticScope 读取其他 Agent 的输出。

```java
@Component
public class RecommendationTools {

    @Tool("根据营养偏好推荐菜品")
    public List<Dish> recommendByNutrition(
        @P("用户ID") String userId,
        AgenticScope scope  // 注入 AgenticScope
    ) {
        // 读取 L1 Agent 的输出
        String nutritionInfo = (String) scope.readState("nutritionInfo");
        String userPreferences = (String) scope.readState("userPreferences");

        // 结合多个 L1 Agent 的输出进行推荐
        return service.recommend(nutritionInfo, userPreferences);
    }
}
```

## 实际应用示例

### 示例1：智能推荐场景

**用户问题**："推荐一些适合我的低卡路里川菜"

**处理流程**：
1. **SmartRecommendationAgent** (L2) 接收用户问题
2. LLM 调用 **UserPreferenceAgent** (L1) 获取用户偏好
   - 输出键：`userPreferences`
   - 输出示例：`{"dietGoal": "WEIGHT_LOSS", "spicyLevel": "MEDIUM"}`
3. LLM 调用 **DishRecommendationAgent** (L1) 获取川菜推荐
   - 输出键：`recommendations`
   - 输出示例：`[{"name": "宫保鸡丁", "calorie": 300}, ...]`
4. LLM 调用 **NutritionGuideAgent** (L1) 分析营养
   - 输出键：`nutritionInfo`
   - 输出示例：`{"totalCalories": 450, "protein": 25g}`
5. LLM 综合三个 Agent 的输出，生成推荐回复

### 示例2：健康饮食规划

**用户问题**："帮我制定一个减肥期间的饮食计划"

**处理流程**：
1. **HealthManagementAgent** (L2) 接收请求
2. 调用 **UserPreferenceAgent** 获取用户信息（身高、体重、目标）
3. 调用 **NutritionGuideAgent** 计算每日热量需求
4. 调用 **DishRecommendationAgent** 推荐符合要求的菜品
5. 综合生成个性化饮食计划

## 配置示例

### L1 Agent 配置

```java
@Bean
public UserPreferenceAgent userPreferenceAgent(ChatModel chatModel, ChatMemory chatMemory) {
    return AgenticServices.agentBuilder(UserPreferenceAgent.class)
            .chatModel(chatModel)
            .chatMemory(chatMemory)
            .tools(userProfileTools)
            .outputKey("userPreferences")  // 配置输出键
            .build();
}
```

### L2 Agent 配置

```java
@Bean
public SmartRecommendationAgent smartRecommendationAgent(
        ChatModel chatModel,
        UserPreferenceAgent userPreferenceAgent,
        DishRecommendationAgent dishRecommendationAgent) {

    return AgenticServices.agentBuilder(SmartRecommendationAgent.class)
            .chatModel(chatModel)
            .tools(
                userPreferenceAgent,      // 注入 L1 Agent
                dishRecommendationAgent   // 注入 L1 Agent
            )
            .build();
}
```

## 关键点

1. **自动传递**：Agent 作为 tool 注入后，LLM 会自动决定何时调用
2. **状态隔离**：每次对话有独立的 AgenticScope
3. **类型安全**：建议使用 TypedKey 来确保类型安全
4. **调试支持**：可以通过 `AgenticScope.agentInvocations()` 查看调用链

## 注意事项

1. **不要手动管理 AgenticScope**：框架会自动创建和管理
2. **OutputKey 只是元数据**：主要用于工作流引擎和监控
3. **实际数据传递通过返回值**：Agent 的方法返回值会自动传递给调用者
4. **避免循环依赖**：L1 Agent 不应该注入 L2 Agent，只注入工具

## 后续优化方向

1. 添加 `@Agent` 注解到接口定义
2. 使用 TypedKey 确保类型安全
3. 实现调用链监控和追踪
4. 添加单元测试验证状态传递

---

**文档版本**: 1.0
**更新时间**: 2026-03-25
**作者**: Claude
