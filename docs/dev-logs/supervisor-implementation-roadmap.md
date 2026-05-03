# 佳食宜选 Supervisor Agent 完整实施计划书

> 从0到1搭建完整的Agentic System
>
> 项目：佳食宜选 - 校园餐饮推荐平台
> 计划周期：6-8周
> 团队规模：2-3人

---

## 📋 目录

1. [总体架构](#总体架构)
2. [工具类设计与实现](#工具类设计与实现)
3. [L1基础Agent实现](#l1基础agent实现)
4. [L2复合Agent实现](#l2复合agent实现)
5. [L3/L4 Supervisor实现](#l3l4-supervisor实现)
6. [测试与验证](#测试与验证)
7. [时间规划](#时间规划)
8. [资源需求](#资源需求)

---

## 总体架构

### 🎯 技术栈确认

```xml
<!-- 核心依赖 -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-agentic</artifactId>
    <version>0.36.2</version>
</dependency>
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-zhipu-ai</artifactId>
    <version>0.36.2</version>
</dependency>
```

### 📐 分层架构

```
┌─────────────────────────────────────────┐
│  前端 (Vue + Element Plus)              │
└──────────────┬──────────────────────────┘
               │ REST API
┌──────────────▼──────────────────────────┐
│  Controller层                            │
│  - AgentController                      │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│  L4: MainSupervisor (总协调)            │
├──────────────┬──────────────────────────┤
│  L3: UserDiningSupervisor               │
│      MerchantManagementSupervisor       │
├──────────────┬──────────────────────────┤
│  L2: 7个复合Agent (Workflow)             │
├──────────────┬──────────────────────────┤
│  L1: 16个基础Agent                      │
├──────────────┬──────────────────────────┤
│  Tools: 30+ 工具类                       │
├──────────────┬──────────────────────────┤
│  Service层 (业务服务)                   │
├──────────────┬──────────────────────────┤
│  DAO层 (数据访问)                       │
├──────────────┬──────────────────────────┤
│  数据库 (MySQL) + 缓存 (Redis)          │
└─────────────────────────────────────────┘
```

---

## 工具类设计与实现

### 🎯 工具类分层架构

```
Tools (30+)
├── 用户相关 (5个)
│   ├── UserQueryTools
│   ├── UserPreferenceTools
│   ├── UserProfileTools
│   ├── UserHealthGoalTools
│   └── UserDietRecordTools
│
├── 营养相关 (6个)
│   ├── NutritionQueryTools
│   ├── NutritionAnalysisTools
│   ├── CalorieCalculatorTools
│   ├── NutritionDatabaseTools
│   ├── NutritionComparisonTools
│   └── HealthAdviceTools
│
├── 推荐相关 (5个)
│   ├── RecommendationQueryTools
│   ├── RecommendationFilterTools
│   ├── RecommendationRankTools
│   ├── PersonalizedRecommendationTools
│   └── HotDishTools
│
├── 订单相关 (6个)
│   ├── OrderQueryTools
│   ├── OrderCreateTools
│   ├── OrderCancelTools
│   ├── OrderStatusTools
│   ├── CouponQueryTools
│   └── DeliveryTrackTools
│
├── 菜品相关 (4个)
│   ├── MenuQueryTools
│   ├── DishDetailTools
│   ├── DishSearchTools
│   └── DishCompareTools
│
├── 商家相关 (4个)
│   ├── MerchantQueryTools
│   ├── MerchantStatsTools
│   ├── MerchantAnalyticsTools
│   └── MerchantComparisonTools
│
└── 系统相关 (5个)
    ├── LocationTools
    ├── TimeTools
    ├── WeatherTools
    ├── ValidationTools
    └── FormatTools
```

---

## 📦 详细工具类设计

### 1. 用户相关工具 (UserTools包)

#### 1.1 UserQueryTools

```java
package com.xx.jaseatschoicejava.agent.tools.user;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户查询工具
 *
 * 提供用户信息的查询功能
 */
@Slf4j
@Service
public class UserQueryTools {

    @Resource
    private UserService userService;

    /**
     * 获取用户基本信息
     */
    @Tool("""
        获取用户的基本信息，包括：
        - 用户ID
        - 昵称
        - 手机号
        - 注册时间
        - 会员等级

        **何时使用：**
        - 需要了解用户基本信息
        - 验证用户身份

        **参数：** userId - 用户ID
        **返回：** 用户基本信息
        """)
    public UserBasicInfo getUserInfo(
        @P("用户ID") String userId
    ) {
        log.info("查询用户信息：{}", userId);
        return userService.getUserBasicInfo(userId);
    }

    /**
     * 获取用户详细资料
     */
    @Tool("""
        获取用户的详细资料，包括：
        - 基本信息
        - 个人资料（身高、体重、生日等）
        - 健康目标
        - 饮食偏好
        - 过敏信息

        **何时使用：**
        - 需要全面了解用户
        - 个性化推荐
        - 制定饮食计划

        **参数：** userId - 用户ID
        **返回：** 用户详细资料
        """)
    public UserProfile getUserProfile(
        @P("用户ID") String userId
    ) {
        log.info("查询用户详细资料：{}", userId);
        return userService.getUserProfile(userId);
    }

    /**
     * 批量获取用户信息
     */
    @Tool("""
        批量获取多个用户的基本信息

        **何时使用：**
        - 需要查询多个用户
        - 用户列表展示

        **参数：** userIds - 用户ID列表
        **返回：** 用户信息列表
        """)
    public List<UserBasicInfo> batchGetUsers(
        @P("用户ID列表") List<String> userIds
    ) {
        log.info("批量查询用户信息，数量：{}", userIds.size());
        return userService.batchGetUsers(userIds);
    }
}
```

#### 1.2 UserPreferenceTools

```java
@Slf4j
@Service
public class UserPreferenceTools {

    @Resource
    private UserPreferenceService preferenceService;

    /**
     * 获取用户饮食偏好
     */
    @Tool("""
        获取用户的饮食偏好，包括：
        - 口味偏好（辣度、甜度等）
        - 菜系偏好（川菜、粤菜等）
        - 素食/荤食
        - 忌口/过敏
        - 特殊需求（低卡、低脂等）

        **何时使用：**
        - 个性化推荐
        - 菜品筛选

        **参数：** userId - 用户ID
        **返回：** 饮食偏好信息
        """)
    public UserDietPreference getDietPreference(
        @P("用户ID") String userId
    ) {
        return preferenceService.getDietPreference(userId);
    }

    /**
     * 更新用户饮食偏好
     */
    @Tool("""
        更新用户的饮食偏好

        **何时使用：**
        - 用户明确表示喜欢/不喜欢某类食物
        - 用户修改偏好设置

        **参数：**
        - userId - 用户ID
        - preference - 偏好信息（JSON格式）

        **返回：** 更新结果
        """)
    public String updateDietPreference(
        @P("用户ID") String userId,
        @P("偏好信息（JSON格式）") String preferenceJson
    ) {
        return preferenceService.updatePreference(userId, preferenceJson);
    }

    /**
     * 添加用户忌口
     */
    @Tool("""
        添加用户的忌口食物

        **何时使用：**
        - 用户表示对某食物过敏
        - 用户不想吃某类食物

        **参数：**
        - userId - 用户ID
        - foodItem - 忌口食物

        **返回：** 添加结果
        """)
    public String addAllergy(
        @P("用户ID") String userId,
        @P("忌口食物") String foodItem
    ) {
        return preferenceService.addAllergy(userId, foodItem);
    }
}
```

#### 1.3 UserHealthGoalTools

```java
@Slf4j
@Service
public class UserHealthGoalTools {

    @Resource
    private UserHealthGoalService healthGoalService;

    /**
     * 获取用户健康目标
     */
    @Tool("""
        获取用户的健康目标，包括：
        - 目标类型（减肥、增肌、保持、增重）
        - 目标体重
        - 每日热量目标
        - 目标期限
        - 当前进度

        **何时使用：**
        - 制定饮食计划
        - 追踪健康进度
        - 营养建议

        **参数：** userId - 用户ID
        **返回：** 健康目标信息
        """)
    public UserHealthGoal getHealthGoal(
        @P("用户ID") String userId
    ) {
        return healthGoalService.getHealthGoal(userId);
    }

    /**
     * 设置健康目标
     */
    @Tool("""
        设置用户的健康目标

        **何时使用：**
        - 用户明确表示要减肥/增肌
        - 用户制定健康计划

        **参数：**
        - userId - 用户ID
        - goalType - 目标类型（减肥/增肌/保持/增重）
        - targetWeight - 目标体重（可选）
        - deadline - 目标期限（可选）

        **返回：** 设置结果
        """)
    public String setHealthGoal(
        @P("用户ID") String userId,
        @P("目标类型：减肥/增肌/保持/增重") String goalType,
        @P("目标体重（kg，可选）") Double targetWeight,
        @P("目标期限（周，可选）") Integer deadlineWeeks
    ) {
        return healthGoalService.setHealthGoal(
            userId, goalType, targetWeight, deadlineWeeks
        );
    }

    /**
     * 计算每日热量目标
     */
    @Tool("""
        根据用户信息计算每日热量目标

        使用Mifflin-St Jeor公式计算：
        1. 基础代谢率(BMR)
        2. 每日总消耗(TDEE)
        3. 根据目标调整

        **何时使用：**
        - 制定饮食计划
        - 设置健康目标

        **参数：**
        - userId - 用户ID
        - goalType - 目标类型

        **返回：** 每日热量目标和建议
        """)
    public String calculateCalorieTarget(
        @P("用户ID") String userId,
        @P("目标类型：减肥/增肌/保持/增重") String goalType
    ) {
        return healthGoalService.calculateCalorieTarget(userId, goalType);
    }
}
```

#### 1.4 UserDietRecordTools

```java
@Slf4j
@Service
public class UserDietRecordTools {

    @Resource
    private UserDietRecordService dietRecordService;

    /**
     * 记录用户饮食
     */
    @Tool("""
        记录用户的饮食摄入

        **何时使用：**
        - 用户说明今天吃了什么
        - 用户要求记录饮食
        - 追踪热量摄入

        **参数：**
        - userId - 用户ID
        - foodItems - 食物列表（JSON数组）
        - mealType - 餐次（早餐/午餐/晚餐/加餐）

        **返回：** 记录结果和总热量
        """)
    public String recordDiet(
        @P("用户ID") String userId,
        @P("食物列表，JSON数组格式") String foodItemsJson,
        @P("餐次：早餐/午餐/晚餐/加餐") String mealType
    ) {
        return dietRecordService.recordDiet(userId, foodItemsJson, mealType);
    }

    /**
     * 获取今日饮食记录
     */
    @Tool("""
        获取用户今天的饮食记录

        **何时使用：**
        - 查询今日摄入
        - 分析今日饮食
        - 对比热量目标

        **参数：** userId - 用户ID
        **返回：** 今日饮食记录和汇总
        """)
    public DailyDietSummary getTodayDietRecord(
        @P("用户ID") String userId
    ) {
        return dietRecordService.getTodayRecord(userId);
    }

    /**
     * 获取历史饮食记录
     */
    @Tool("""
        获取用户一段时间的历史饮食记录

        **何时使用：**
        - 分析饮食习惯
        - 生成饮食报告
        - 追踪长期目标

        **参数：**
        - userId - 用户ID
        - days - 天数

        **返回：** 历史饮食记录和统计
        """)
    public DietHistorySummary getDietHistory(
        @P("用户ID") String userId,
        @P("查询天数") int days
    ) {
        return dietRecordService.getHistory(userId, days);
    }

    /**
     * 分析今日饮食是否达标
     */
    @Tool("""
        分析用户今天的饮食是否达到健康目标

        **对比内容：**
        - 实际摄入 vs 目标热量
        - 营养素摄入情况
        - 是否在合理范围

        **何时使用：**
        - 用户询问"我今天吃得怎么样"
        - 用户追踪健康目标
        - 生成饮食建议

        **参数：** userId - 用户ID
        **返回：** 达标分析报告
        """)
    public String analyzeTodayDietCompliance(
        @P("用户ID") String userId
    ) {
        return dietRecordService.analyzeCompliance(userId);
    }
}
```

---

### 2. 营养相关工具 (NutritionTools包)

#### 2.1 NutritionQueryTools

```java
@Slf4j
@Service
public class NutritionQueryTools {

    @Resource
    private NutritionService nutritionService;

    /**
     * 查询食物营养成分
     */
    @Tool("""
        查询食物的营养成分信息

        **返回信息：**
        - 食物名称
        - 卡路里（每100g）
        - 蛋白质（g）
        - 脂肪（g）
        - 碳水化合物（g）
        - 膳食纤维（g）
        - 维生素、矿物质
        - 数据来源

        **何时使用：**
        - 用户询问食物营养
        - 需要营养数据做决策

        **参数：** foodName - 食物名称
        **返回：** 营养成分信息
        """)
    public NutritionInfo queryNutrition(
        @P("食物名称，如'苹果'、'鸡蛋'") String foodName
    ) {
        log.info("查询营养成分：{}", foodName);
        return nutritionService.queryNutrition(foodName);
    }

    /**
     * 批量查询营养
     */
    @Tool("""
        批量查询多个食物的营养成分

        **何时使用：**
        - 用户提到多个食物
        - 需要汇总营养信息

        **参数：** foodNames - 食物名称列表
        **返回：** 营养信息列表
        """)
    public List<NutritionInfo> batchQueryNutrition(
        @P("食物名称列表") List<String> foodNames
    ) {
        log.info("批量查询营养成分，数量：{}", foodNames.size());
        return nutritionService.batchQuery(foodNames);
    }

    /**
     * 搜索营养相似的食物
     */
    @Tool("""
        搜索与指定食物营养相似的其他食物

        **何时使用：**
        - 用户想要替代食物
        - 推荐相似营养的食物

        **参数：**
        - foodName - 参考食物
        - limit - 返回数量

        **返回：** 相似食物列表
        """)
    public List<NutritionInfo> findSimilarNutrition(
        @P("参考食物") String foodName,
        @P("返回数量限制") int limit
    ) {
        return nutritionService.findSimilar(foodName, limit);
    }
}
```

#### 2.2 CalorieCalculatorTools

```java
@Slf4j
@Service
public class CalorieCalculatorTools {

    @Resource
    private CalorieCalculatorService calculatorService;

    /**
     * 计算基础代谢率(BMR)
     */
    @Tool("""
        计算用户的基础代谢率(BMR)

        使用Mifflin-St Jeor公式：
        - 男性：BMR = 10×体重 + 6.25×身高 - 5×年龄 + 5
        - 女性：BMR = 10×体重 + 6.25×身高 - 5×年龄 - 161

        **何时使用：**
        - 制定饮食计划
        - 计算热量目标

        **参数：**
        - weight - 体重
        - height - 身高
        - age - 年龄
        - gender - 性别（男/女）

        **返回：** BMR数值和说明
        """)
    public double calculateBMR(
        @P("体重") double weight,
        @P("身高") double height,
        @P("年龄") int age,
        @P("性别：男/女") String gender
    ) {
        return calculatorService.calculateBMR(weight, height, age, gender);
    }

    /**
     * 计算每日总消耗(TDEE)
     */
    @Tool("""
        计算每日总能量消耗(TDEE)

        TDEE = BMR × 活动系数

        活动系数：
        - 久坐：1.2
        - 轻度活动：1.375
        - 中度活动：1.55
        - 重度活动：1.725

        **何时使用：**
        - 制定饮食计划
        - 设置热量目标

        **参数：**
        - bmr - 基础代谢率
        - activityLevel - 活动水平

        **返回：** TDEE数值
        """)
    public double calculateTDEE(
        @P("基础代谢率") double bmr,
        @P("活动水平：久坐/轻度/中度/重度") String activityLevel
    ) {
        return calculatorService.calculateTDEE(bmr, activityLevel);
    }

    /**
     * 计算食物总热量
     */
    @Tool("""
        计算多个食物的总热量

        **何时使用：**
        - 用户提到吃了多个食物
        - 汇总一餐的热量

        **参数：**
        - foodItems - 食物列表，每项包含名称和重量
        - 返回：总热量和详细说明
        """)
    public String calculateTotalCalories(
        @P("食物列表，JSON格式：[{name:'苹果',weight:100},...]")
        String foodItemsJson
    ) {
        return calculatorService.calculateTotalCalories(foodItemsJson);
    }

    /**
     * 根据目标计算每日热量目标
     */
    @Tool("""
        根据用户的健康目标计算每日热量目标

        **目标热量调整：**
        - 减肥：TDEE - 500 kcal
        - 增肌：TDEE + 300 kcal
        - 保持：TDEE
        - 增重：TDEE + 500 kcal

        **何时使用：**
        - 设置健康目标
        - 制定饮食计划

        **参数：**
        - tdee - 每日总消耗
        - goalType - 目标类型

        **返回：** 每日热量目标和建议范围
        """)
    public String calculateCalorieGoal(
        @P("每日总消耗") double tdee,
        @P("目标类型：减肥/增肌/保持/增重") String goalType
    ) {
        return calculatorService.calculateGoal(tdee, goalType);
    }
}
```

#### 2.3 NutritionAnalysisTools

```java
@Slf4j
@Service
public class NutritionAnalysisTools {

    @Resource
    private NutritionAnalysisService analysisService;

    /**
     * 分析饮食营养均衡性
     */
    @Tool("""
        分析饮食的营养均衡性

        **评估维度：**
        - 热量是否合理
        - 蛋白质是否充足
        - 脂肪占比是否合理
        - 碳水化合物是否适量
        - 维生素矿物质是否丰富

        **评分标准：**
        - 优秀：90-100分
        - 良好：80-89分
        - 中等：70-79分
        - 较差：60-69分
        - 很差：<60分

        **何时使用：**
        - 评估一餐的营养
        - 分析一天的饮食
        - 改进饮食建议

        **参数：**
        - foodItems - 食物列表
        - userId - 用户ID（用于对比目标）

        **返回：** 营养分析报告和评分
        """)
    public NutritionAnalysisReport analyzeNutritionBalance(
        @P("食物列表（JSON格式）") String foodItemsJson,
        @P("用户ID（可选）") String userId
    ) {
        return analysisService.analyzeBalance(foodItemsJson, userId);
    }

    /**
     * 比较两个食物的营养
     */
    @Tool("""
        比较两个食物的营养差异

        **对比内容：**
        - 热量对比
        - 营养素对比
        - 优劣势分析
        - 推荐建议

        **何时使用：**
        - 用户在选择食物
        - 需要对比推荐

        **参数：**
        - food1 - 食物1名称
        - food2 - 食物2名称

        **返回：** 对比分析报告
        """)
    public String compareNutrition(
        @P("食物1名称") String food1,
        @P("食物2名称") String food2
    ) {
        return analysisService.compare(food1, food2);
    }

    /**
     * 生成营养改进建议
     */
    @Tool("""
        根据营养分析生成改进建议

        **建议类型：**
        - 缺少的营养素
        - 需要减少的成分
        - 推荐的替代食物
        - 饮食调整方案

        **何时使用：**
        - 用户询问如何改进饮食
        - 分析报告后给出建议

        **参数：**
        - analysisReport - 营养分析报告
        - userGoal - 用户目标（可选）

        **返回：** 改进建议列表
        """)
    public List<String> generateImprovementSuggestions(
        @P("营养分析报告") String analysisReport,
        @P("用户目标（可选）") String userGoal
    ) {
        return analysisService.generateSuggestions(analysisReport, userGoal);
    }
}
```

---

### 3. 推荐相关工具 (RecommendationTools包)

#### 3.1 RecommendationQueryTools

```java
@Slf4j
@Service
public class RecommendationQueryTools {

    @Resource
    private RecommendationService recommendationService;

    /**
     * 查询推荐菜品列表
     */
    @Tool("""
        根据条件查询推荐菜品

        **支持筛选：**
        - 口味偏好
        - 菜系类型
        - 价格区间
        - 营养要求（低卡、低脂等）
        - 时间段（早餐、午餐、晚餐）
        - 地理位置

        **何时使用：**
        - 用户要求推荐
        - 菜品搜索

        **参数：**
        - userId - 用户ID（用于个性化）
        - filters - 筛选条件（JSON格式）

        **返回：** 推荐菜品列表
        """)
    public List<RecommendationDish> queryRecommendations(
        @P("用户ID") String userId,
        @P("筛选条件（JSON格式）") String filtersJson
    ) {
        return recommendationService.query(userId, filtersJson);
    }

    /**
     * 获取热门菜品
     */
    @Tool("""
        获取当前热门菜品

        **热度依据：**
        - 销量
        - 评分
        - 好评率
        - 最近订单数

        **何时使用：**
        - 用户询问热门推荐
        - 首页展示

        **参数：**
        - limit - 返回数量
        - timeRange - 时间范围（今天/本周/本月）

        **返回：** 热门菜品列表
        """)
    public List<RecommendationDish> getHotDishes(
        @P("返回数量") int limit,
        @P("时间范围：今天/本周/本月") String timeRange
    ) {
        return recommendationService.getHotDishes(limit, timeRange);
    }

    /**
     * 获取个性化推荐
     */
    @Tool("""
        获取基于用户偏好的个性化推荐

        **个性化因素：**
        - 历史订单
        - 浏览历史
        - 收藏菜品
        - 评分记录
        - 饮食偏好

        **何时使用：**
        - 首页推荐
        - "猜你喜欢"

        **参数：**
        - userId - 用户ID
        - limit - 返回数量

        **返回：** 个性化推荐列表
        """)
    public List<RecommendationDish> getPersonalizedRecommendations(
        @P("用户ID") String userId,
        @P("返回数量") int limit
    ) {
        return recommendationService.getPersonalized(userId, limit);
    }
}
```

#### 3.2 RecommendationFilterTools

```java
@Slf4j
@Service
public class RecommendationFilterTools {

    @Resource
    private RecommendationFilterService filterService;

    /**
     * 按营养要求筛选
     */
    @Tool("""
        按营养要求筛选菜品

        **营养标签：**
        - 低卡路里（<400kcal/100g）
        - 低脂（<3g/100g）
        - 高蛋白（>20g/100g）
        - 低糖（<5g/100g）
        - 高纤维（>6g/100g）

        **何时使用：**
        - 用户要求"健康的"、"低卡的"
        - 特殊饮食需求

        **参数：**
        - dishes - 待筛选的菜品列表
        - nutritionTags - 营养标签列表

        **返回：** 符合条件的菜品
        """)
    public List<RecommendationDish> filterByNutrition(
        @P("菜品列表") List<RecommendationDish> dishes,
        @P("营养标签列表") List<String> nutritionTags
    ) {
        return filterService.filterByNutrition(dishes, nutritionTags);
    }

    /**
     * 按价格区间筛选
     */
    @Tool("""
        按价格区间筛选菜品

        **何时使用：**
        - 用户要求"便宜的"、"实惠的"
        - 用户有预算限制

        **参数：**
        - dishes - 待筛选的菜品列表
        - minPrice - 最低价格
        - maxPrice - 最高价格

        **返回：** 符合价格区间的菜品
        """)
    public List<RecommendationDish> filterByPrice(
        @P("菜品列表") List<RecommendationDish> dishes,
        @P("最低价格") double minPrice,
        @P("最高价格") double maxPrice
    ) {
        return filterService.filterByPrice(dishes, minPrice, maxPrice);
    }

    /**
     * 过滤用户忌口
     */
    @Tool("""
        过滤掉用户忌口或过敏的食物

        **何时使用：**
        - 推荐前检查
        - 用户有过敏史

        **参数：**
        - dishes - 待筛选的菜品列表
        - userId - 用户ID

        **返回：** 过滤后的安全菜品
        """)
    public List<RecommendationDish> filterAllergies(
        @P("菜品列表") List<RecommendationDish> dishes,
        @P("用户ID") String userId
    ) {
        return filterService.filterAllergies(dishes, userId);
    }
}
```

#### 3.3 RecommendationRankTools

```java
@Slf4j
@Service
public class RecommendationRankTools {

    @Resource
    private RecommendationRankService rankService;

    /**
     * 综合评分排序
     */
    @Tool("""
        对菜品进行综合评分并排序

        **评分维度：**
        - 营养健康度（0-100）
        - 价格合理性（0-100）
        - 用户评分（0-100）
        - 匹配度（0-100）

        **综合得分** = 加权平均

        **何时使用：**
        - 优化推荐顺序
        - 提升推荐质量

        **参数：**
        - dishes - 菜品列表
        - userId - 用户ID（用于个性化）

        **返回：** 排序后的菜品列表
        """)
    public List<RecommendationDish> rankByComprehensiveScore(
        @P("菜品列表") List<RecommendationDish> dishes,
        @P("用户ID") String userId
    ) {
        return rankService.rankByScore(dishes, userId);
    }

    /**
     * 评估推荐质量
     */
    @Tool("""
        评估推荐结果的质量

        **评估指标：**
        - 多样性（菜品是否多样）
        - 个性化（是否符合用户偏好）
        - 相关性（是否满足用户需求）
        - 实用性（价格、时间是否合理）

        **返回评分：** 0.0-1.0之间

        **何时使用：**
        - Loop优化时评分
        - 推荐质量监控

        **参数：**
        - recommendations - 推荐结果
        - userRequest - 用户需求

        **返回：** 质量评分
        """)
    public double evaluateRecommendationQuality(
        @P("推荐结果") List<RecommendationDish> recommendations,
        @P("用户需求") String userRequest
    ) {
        return rankService.evaluateQuality(recommendations, userRequest);
    }
}
```

---

### 4. 订单相关工具 (OrderTools包)

#### 4.1 OrderQueryTools

```java
@Slf4j
@Service
public class OrderQueryTools {

    @Resource
    private OrderQueryService orderQueryService;

    /**
     * 查询订单详情
     */
    @Tool("""
        查询订单的详细信息

        **返回信息：**
        - 订单号
        - 订单状态
        - 菜品列表
        - 价格明细
        - 配送信息
        - 优惠信息
        - 下单时间

        **何时使用：**
        - 用户查询订单
        - 订单状态跟踪

        **参数：** orderId - 订单ID

        **返回：** 订单详情
        """)
    public OrderDetail getOrderDetail(
        @P("订单ID") String orderId
    ) {
        return orderQueryService.getOrderDetail(orderId);
    }

    /**
     * 查询用户所有订单
     */
    @Tool("""
        查询用户的所有订单

        **支持筛选：**
        - 订单状态
        - 时间范围
        - 商家

        **何时使用：**
        - 用户查看历史订单
        - 订单管理

        **参数：**
        - userId - 用户ID
        - filters - 筛选条件（可选）

        **返回：** 订单列表
        """)
    public List<OrderSummary> getUserOrders(
        @P("用户ID") String userId,
        @P("筛选条件（JSON格式，可选）") String filtersJson
    ) {
        return orderQueryService.getUserOrders(userId, filtersJson);
    }

    /**
     * 查询订单状态
     */
    @Tool("""
        查询订单的当前状态

        **订单状态：**
        - 待确认
        - 已确认
        - 制作中
        - 配送中
        - 已完成
        - 已取消
        - 已退款

        **何时使用：**
        - 快速查询订单状态
        - 配送跟踪

        **参数：** orderId - 订单ID

        **返回：** 订单状态和预计送达时间
        """)
    public OrderStatusInfo getOrderStatus(
        @P("订单ID") String orderId
    ) {
        return orderQueryService.getOrderStatus(orderId);
    }

    /**
     * 获取推荐地址
     */
    @Tool("""
        获取用户的推荐配送地址

        **推荐地址基于：**
        - 历史订单地址
        - 使用频率
        - 最近使用时间

        **何时使用：**
        - 创建订单时智能填充
        - 用户询问"送到哪里"

        **参数：** userId - 用户ID

        **返回：** 推荐地址列表（按优先级排序）
        """)
    public List<String> getRecommendedAddress(
        @P("用户ID") String userId
    ) {
        return orderQueryService.getRecommendedAddress(userId);
    }
}
```

#### 4.2 OrderCreateTools

```java
@Slf4j
@Service
public class OrderCreateTools {

    @Resource
    private OrderCreateService orderCreateService;

    /**
     * 创建订单
     */
    @Tool("""
        创建一个新的订单

        **必需参数：**
        - userId: 用户ID
        - merchantId: 商家ID
        - dishItems: 菜品列表（JSON格式）
        - deliveryAddress: 配送地址
        - phoneNumber: 联系电话

        **可选参数：**
        - couponId: 优惠券ID
        - note: 备注信息

        **何时使用：**
        - 用户下单
        - 确认订单信息

        **参数：** orderRequest - 订单信息（JSON格式）

        **返回：** 订单号和预计送达时间
        """)
    public String createOrder(
        @P("订单信息（JSON格式）") String orderRequestJson
    ) {
        return orderCreateService.createOrder(orderRequestJson);
    }

    /**
     * 计算订单价格
     */
    @Tool("""
        计算订单的价格

        **价格包含：**
        - 菜品总价
        - 配送费
        - 包装费
        - 优惠折扣
        - 最终实付

        **何时使用：**
        - 下单前确认价格
        - 比较不同方案

        **参数：**
        - dishItems - 菜品列表
        - couponId - 优惠券ID（可选）
        - userId - 用户ID（用于会员折扣）

        **返回：** 价格明细
        """)
    public PriceBreakdown calculateOrderPrice(
        @P("菜品列表（JSON格式）") String dishItemsJson,
        @P("优惠券ID（可选）") String couponId,
        @P("用户ID") String userId
    ) {
        return orderCreateService.calculatePrice(dishItemsJson, couponId, userId);
    }

    /**
     * 查询可用优惠券
     */
    @Tool("""
        查询用户可用的优惠券

        **筛选条件：**
        - 订单金额是否满足门槛
        - 商家是否适用
        - 是否在有效期
        - 使用次数限制

        **何时使用：**
        - 下单前查询优惠
        - 推荐最优优惠

        **参数：**
        - userId - 用户ID
        - orderAmount - 订单金额
        - merchantId - 商家ID

        **返回：** 可用优惠券列表，按优惠金额排序
        """)
    public List<CouponInfo> getAvailableCoupons(
        @P("用户ID") String userId,
        @P("订单金额") double orderAmount,
        @P("商家ID") String merchantId
    ) {
        return orderCreateService.getAvailableCoupons(userId, orderAmount, merchantId);
    }

    /**
     * 推荐最优优惠券
     */
    @Tool("""
        为订单推荐最优的优惠券

        **推荐依据：**
        - 优惠金额最大
        - 门槛合适
        - 适用商家

        **何时使用：**
        - 自动为用户选择最优优惠
        - 提升用户体验

        **参数：**
        - userId - 用户ID
        - orderAmount - 订单金额
        - merchantId - 商家ID

        **返回：** 最优优惠券和节省金额
        """)
    public CouponInfo recommendBestCoupon(
        @P("用户ID") String userId,
        @P("订单金额") double orderAmount,
        @P("商家ID") String merchantId
    ) {
        return orderCreateService.recommendBestCoupon(userId, orderAmount, merchantId);
    }
}
```

---

### 5. 菜品相关工具 (MenuTools包)

#### 5.1 MenuQueryTools

```java
@Slf4j
@Service
public class MenuQueryTools {

    @Resource
    private MenuQueryService menuQueryService;

    /**
     * 查询商家菜单
     */
    @Tool("""
        查询商家的完整菜单

        **菜单包含：**
        - 菜品分类
        - 菜品列表
        - 价格
        - 描述
        - 图片
        - 库存状态

        **何时使用：**
        - 用户浏览菜单
        - 菜品搜索

        **参数：** merchantId - 商家ID

        **返回：** 商家菜单
        """)
    public MerchantMenu getMenu(
        @P("商家ID") String merchantId
    ) {
        return menuQueryService.getMenu(merchantId);
    }

    /**
     * 搜索菜品
     */
    @Tool("""
        搜索菜品

        **搜索范围：**
        - 菜品名称
        - 菜品描述
        - 食材标签

        **支持筛选：**
        - 菜系
        - 价格区间
        - 营养标签

        **何时使用：**
        - 用户搜索特定菜品
        - 按需求查找

        **参数：**
        - keyword - 搜索关键词
        - filters - 筛选条件（可选）

        **返回：** 搜索结果
        """)
    public List<DishInfo> searchDishes(
        @P("搜索关键词") String keyword,
        @P("筛选条件（JSON格式，可选）") String filtersJson
    ) {
        return menuQueryService.searchDishes(keyword, filtersJson);
    }

    /**
     * 获取菜品详情
     */
    @Tool("""
        获取菜品的详细信息

        **详情包含：**
        - 基本信息（名称、价格、描述）
        - 营养成分
        - 用户评价
        - 销量数据
        - 推荐度

        **何时使用：**
        - 用户查看菜品详情
        - 决定是否购买

        **参数：** dishId - 菜品ID

        **返回：** 菜品详情
        """)
    public DishDetail getDishDetail(
        @P("菜品ID") String dishId
    ) {
        return menuQueryService.getDishDetail(dishId);
    }
}
```

---

### 6. 系统相关工具 (SystemTools包)

#### 6.1 LocationTools

```java
@Slf4j
@Service
public class LocationTools {

    @Resource
    private LocationService locationService;

    /**
     * 获取用户位置
     */
    @Tool("""
        获取用户的位置信息

        **位置信息：**
        - 校园区域
        - 具体建筑
        - 楼层
        - 房间（可选）

        **何时使用：**
        - 推荐附近商家
        - 估算配送时间

        **参数：** userId - 用户ID

        **返回：** 位置信息
        """)
    public UserLocation getUserLocation(
        @P("用户ID") String userId
    ) {
        return locationService.getUserLocation(userId);
    }

    /**
     * 计算距离
     */
    @Tool("""
        计算两个位置之间的距离

        **何时使用：**
        - 估算配送时间
        - 推荐最近商家

        **参数：**
        - location1 - 位置1
        - location2 - 位置2

        **返回：** 距离（米）
        """)
    public double calculateDistance(
        @P("位置1（如：学生宿舍3栋）") String location1,
        @P("位置2（如：第二食堂）") String location2
    ) {
        return locationService.calculateDistance(location1, location2);
    }

    /**
     * 推荐附近商家
     */
    @Tool("""
        推荐用户附近的商家

        **推荐依据：**
        - 距离最近
        - 配送范围内
        - 营业中

        **何时使用：**
        - 用户询问"附近有什么"
        - 默认推荐

        **参数：**
        - userId - 用户ID
        - limit - 返回数量

        **返回：** 附近商家列表
        """)
    public List<MerchantInfo> getNearbyMerchants(
        @P("用户ID") String userId,
        @P("返回数量") int limit
    ) {
        return locationService.getNearbyMerchants(userId, limit);
    }
}
```

#### 6.2 TimeTools

```java
@Slf4j
@Service
public class TimeTools {

    /**
     * 获取当前时间
     */
    @Tool("""
        获取当前时间和日期

        **返回格式：**
        - 日期：YYYY-MM-DD
        - 时间：HH:mm
        - 星期：星期一 ~ 星期日
        - 时段：早晨/上午/中午/下午/晚上/深夜

        **何时使用：**
        - 推荐早午晚餐
        - 判断商家营业状态

        **返回：** 当前时间信息
        """)
    public String getCurrentTime() {
        return TimeUtils.getCurrentTimeInfo();
    }

    /**
     * 判断时间段
     */
    @Tool("""
        判断当前属于哪个时间段

        **时间段定义：**
        - 早晨：5:00-8:00（早餐）
        - 上午：8:00-11:00
        - 中午：11:00-13:00（午餐）
        - 下午：13:00-17:00
        - 晚上：17:00-20:00（晚餐）
        - 深夜：20:00-5:00

        **何时使用：**
        - 推荐时段菜品
        - 问候语

        **返回：** 时间段名称
        """)
    public String getTimePeriod() {
        return TimeUtils.getTimePeriod();
    }

    /**
     * 估算配送时间
     */
    @Tool("""
        估算订单的配送时间

        **估算因素：**
        - 商家距离
        - 当前订单量
        - 制作时间
        - 配送速度

        **何时使用：**
        - 下单前告知送达时间
        - 用户询问配送时间

        **参数：**
        - merchantId - 商家ID
        - userLocation - 用户位置

        **返回：** 预计送达时间（分钟）
        """)
    public int estimateDeliveryTime(
        @P("商家ID") String merchantId,
        @P("用户位置") String userLocation
    ) {
        return TimeUtils.estimateDeliveryTime(merchantId, userLocation);
    }
}
```

---

## 📊 工具类实施计划

### 阶段1：核心工具（Week 1）

**优先级最高，必须实现：**

| 工具类 | 功能 | 优先级 | 工作量 |
|-------|------|--------|--------|
| UserQueryTools | 用户信息查询 | P0 | 1天 |
| UserPreferenceTools | 用户偏好 | P0 | 1天 |
| NutritionQueryTools | 营养查询 | P0 | 1天 |
| CalorieCalculatorTools | 热量计算 | P0 | 1天 |
| OrderQueryTools | 订单查询 | P0 | 1天 |
| OrderCreateTools | 创建订单 | P0 | 2天 |
| MenuQueryTools | 菜品查询 | P0 | 1天 |

**小计：** 7个工具类，8天

---

### 阶段2：扩展工具（Week 2）

**重要功能，提升体验：**

| 工具类 | 功能 | 优先级 | 工作量 |
|-------|------|--------|--------|
| UserHealthGoalTools | 健康目标 | P1 | 2天 |
| UserDietRecordTools | 饮食记录 | P1 | 2天 |
| NutritionAnalysisTools | 营养分析 | P1 | 2天 |
| RecommendationQueryTools | 推荐查询 | P1 | 1天 |
| RecommendationFilterTools | 推荐筛选 | P1 | 1天 |
| RecommendationRankTools | 推荐排序 | P1 | 2天 |

**小计：** 6个工具类，10天

---

### 阶段3：增强工具（Week 3）

**锦上添花，完善功能：**

| 工具类 | 功能 | 优先级 | 工作量 |
|-------|------|--------|--------|
| UserDietRecordTools | 饮食记录 | P1 | 2天 |
| MerchantQueryTools | 商家查询 | P1 | 1天 |
| MerchantStatsTools | 商家统计 | P2 | 2天 |
| LocationTools | 位置服务 | P2 | 1天 |
| TimeTools | 时间服务 | P2 | 1天 |

**小计：** 5个工具类，7天

---

### 阶段4：高级工具（Week 4）

**高级分析，智能优化：**

| 工具类 | 功能 | 优先级 | 工作量 |
|-------|------|--------|--------|
| NutritionAnalysisTools | 营养分析 | P2 | 2天 |
| NutritionComparisonTools | 营养对比 | P2 | 1天 |
| MerchantAnalyticsTools | 经营分析 | P2 | 2天 |
| RecommendationRankTools | 推荐评分 | P2 | 2天 |

**小计：** 4个工具类，7天

---

## 🎯 总时间规划

### 工具类开发：4周

| 周次 | 工具类 | 数量 | 工作量 |
|-----|-------|------|--------|
| Week 1 | 核心工具 | 7个 | 8天 |
| Week 2 | 扩展工具 | 6个 | 10天 |
| Week 3 | 增强工具 | 5个 | 7天 |
| Week 4 | 高级工具 | 4个 | 7天 |

**总计：** 22个工具类，32天

---

### L1 Agent开发：1周

| Agent | 功能 | 工作量 |
|-------|------|--------|
| NutritionAnalysisAgent | 营养分析 | 0.5天 |
| BasicRecommendationAgent | 基础推荐 | 0.5天 |
| OrderQueryAgent | 订单查询 | 0.5天 |
| OrderCreateAgent | 创建订单 | 0.5天 |
| UserProfileAgent | 用户资料 | 0.5天 |
| FavoriteManageAgent | 收藏管理 | 0.5天 |
| RecipeAgent | 食谱管理 | 0.5天 |

**小计：** 7个基础Agent，3.5天

---

### L2 Agent开发：2周

| Agent | Workflow类型 | 工作量 |
|-------|-------------|--------|
| SmartRecommendationAgent | Loop | 2天 |
| MultiDimensionalRecAgent | Parallel | 2天 |
| BatchNutritionAnalyzerAgent | Mapper | 1天 |
| SmartMealPlannerAgent | Goal-Oriented | 2天 |
| IntelligentOrderAgent | Sequential | 2天 |
| HealthGoalTrackerAgent | Sequential+Loop | 2天 |
| MerchantAnalyticsAgent | Parallel | 1天 |

**小计：** 7个复合Agent，12天

---

### L3/L4 Supervisor开发：1周

| Supervisor | 功能 | 工作量 |
|-----------|------|--------|
| UserDiningSupervisor | 用户餐饮领域 | 2天 |
| MerchantManagementSupervisor | 商家管理领域 | 1天 |
| MainSupervisor | 总协调 | 2天 |

**小计：** 3个Supervisor，5天

---

### 测试与优化：1周

- 单元测试
- 集成测试
- 性能优化
- 提示词调优

---

## 📅 完整时间表（8周）

```
Week 1: 核心工具类 (7个)
    ├── UserQueryTools
    ├── UserPreferenceTools
    ├── NutritionQueryTools
    ├── CalorieCalculatorTools
    ├── OrderQueryTools
    ├── OrderCreateTools
    └── MenuQueryTools

Week 2: 扩展工具类 (6个)
    ├── UserHealthGoalTools
    ├── UserDietRecordTools
    ├── NutritionAnalysisTools
    ├── RecommendationQueryTools
    ├── RecommendationFilterTools
    └── RecommendationRankTools

Week 3: 增强工具类 (5个) + L1基础Agent开始
    ├── MerchantQueryTools
    ├── MerchantStatsTools
    ├── LocationTools
    ├── TimeTools
    └── 开始L1 Agent (3-4个)

Week 4: 高级工具类 (4个) + L1基础Agent完成
    ├── NutritionComparisonTools
    ├── MerchantAnalyticsTools
    ├── RecommendationRankTools
    └── 完成L1 Agent (剩余3个)

Week 5-6: L2复合Agent (7个)
    ├── SmartRecommendationAgent (Loop)
    ├── MultiDimensionalRecAgent (Parallel)
    ├── BatchNutritionAnalyzerAgent (Mapper)
    ├── SmartMealPlannerAgent (Goal-Oriented)
    ├── IntelligentOrderAgent (Sequential)
    ├── HealthGoalTrackerAgent (Sequential+Loop)
    └── MerchantAnalyticsAgent (Parallel)

Week 7: L3/L4 Supervisor (3个)
    ├── UserDiningSupervisor
    ├── MerchantManagementSupervisor
    └── MainSupervisor

Week 8: 测试与优化
    ├── 单元测试
    ├── 集成测试
    ├── 性能优化
    └── 提示词调优
```

---

## 📦 交付物清单

### 代码交付

1. **工具类**：22个Tools类
2. **L1 Agent**：16个基础Agent接口
3. **L2 Agent**：7个复合Agent接口
4. **L3 Supervisor**：2个领域Supervisor
5. **L4 Supervisor**：1个主Supervisor
6. **配置类**：Agent配置、Supervisor配置
7. **Controller**：统一的API接口

### 文档交付

1. **架构设计文档**：本文档
2. **API接口文档**：所有Agent的接口说明
3. **工具类文档**：每个工具类的使用说明
4. **部署文档**：环境配置、依赖安装
5. **测试文档**：测试用例、测试报告

---

## 🎯 成功标准

### 功能指标

- ✅ 支持16个基础Agent
- ✅ 支持7个复合Agent（Workflow）
- ✅ 支持3个Supervisor
- ✅ 工具类覆盖所有业务场景
- ✅ 响应时间 < 10秒

### 质量指标

- ✅ 单元测试覆盖率 > 80%
- ✅ 集成测试通过率 100%
- ✅ 代码规范符合率 100%
- ✅ 文档完整性 100%

### 性能指标

- ✅ 简单查询 < 2秒
- ✅ 复杂Workflow < 10秒
- ✅ 批量处理 < 5秒
- ✅ 并行处理效率提升 > 50%

---

## 🚀 下一步行动

### 立即开始（Week 1）

**优先实现这7个核心工具类：**

1. **UserQueryTools** - 用户信息查询
2. **UserPreferenceTools** - 用户偏好
3. **NutritionQueryTools** - 营养查询
4. **CalorieCalculatorTools** - 热量计算
5. **OrderQueryTools** - 订单查询
6. **OrderCreateTools** - 创建订单
7. **MenuQueryTools** - 菜品查询

需要我帮你：
1. **编写这7个工具类的完整代码**？
2. **设计工具类的接口定义**？
3. **开始第一个Agent的实现**？

请告诉我你想从哪里开始！
