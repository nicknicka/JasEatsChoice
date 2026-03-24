# LangChain4j Agent系统文档

**项目名称**: 佳食宜选 AI Agent系统
**技术框架**: LangChain4j + Spring Boot + ZhipuAI GLM-4
**作者**: Claude
**完成时间**: 2026-03-24

---

## 目录

1. [系统概述](#系统概述)
2. [系统架构](#系统架构)
3. [工具类详解](#工具类详解)
4. [智能体详解](#智能体详解)
5. [配置说明](#配置说明)
6. [使用指南](#使用指南)
7. [测试指南](#测试指南)
8. [性能优化](#性能优化)
9. [未来规划](#未来规划)

---

## 系统概述

### 项目目标

构建一个基于LangChain4j的智能Agent系统，为"佳食宜选"校园餐饮平台提供全方位的AI服务，包括：

- 智能菜品推荐
- 营养健康咨询
- 全流程订餐服务
- 个性化饮食规划
- 长期健康目标管理

### 技术栈

| 组件 | 技术选型 | 版本 |
|------|----------|------|
| Java框架 | Spring Boot | 2.7.18 |
| AI框架 | LangChain4j | 0.34.0 |
| LLM模型 | ZhipuAI GLM-4 | 最新 |
| 构建工具 | Maven | 3.x |
| JDK版本 | Java | 17 |

### 实施周期

- **Week 1-2**: 基础工具类（9个）
- **Week 3**: 增强工具类（9个）
- **Week 4**: L1基础智能体（7个）
- **Week 5**: L2领域智能体（4个）
- **Week 6**: L3编排智能体（3个）
- **Week 7**: 集成测试与优化
- **Week 8**: 文档与部署

---

## 系统架构

### 三层架构设计

```
┌─────────────────────────────────────────────────────────┐
│                     L3 编排层                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │LifeService  │  │DailyPlanning │  │GoalAchieve   │  │
│  │Agent        │  │Agent         │  │Agent         │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└──────────────────────┬──────────────────────────────────┘
                       │ 调用/编排
┌──────────────────────┴──────────────────────────────────┐
│                     L2 领域层                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │Smart        │  │Health        │  │FullOrder     │  │
│  │Recommend    │  │Management    │  │Agent         │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────────────────────────────────────────┐  │
│  │IntelligentAssistant Agent                        │  │
│  └──────────────────────────────────────────────────┘  │
└──────────────────────┬──────────────────────────────────┘
                       │ 调用/协作
┌──────────────────────┴──────────────────────────────────┐
│                     L1 基础层                            │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │
│  │User      │ │Nutrition │ │Dish      │ │Merchant  │  │
│  │Prefer    │ │Guide     │ │Recommend │ │Info      │  │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘  │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐                │
│  │Time      │ │Location  │ │Order     │                │
│  │Aware     │ │Service   │ │Helper    │                │
│  └──────────┘ └──────────┘ └──────────┘                │
└──────────────────────┬──────────────────────────────────┘
                       │ 调用工具
┌──────────────────────┴──────────────────────────────────┐
│                    工具层 (18个)                         │
│  用户工具(1) | 营养工具(2) | 推荐工具(3)                 │
│  商家工具(2) | 订单工具(2) | 系统工具(2)                 │
└──────────────────────┬──────────────────────────────────┘
                       │ 访问数据
┌──────────────────────┴──────────────────────────────────┐
│                    数据层                                │
│  MySQL数据库 | Redis缓存 | 业务Service层                │
└─────────────────────────────────────────────────────────┘
```

### Agent数量统计

| 层级 | 数量 | 职责 |
|------|------|------|
| L1 基础智能体 | 7个 | 单一领域专注处理 |
| L2 领域智能体 | 4个 | 多工具协作领域专家 |
| L3 编排智能体 | 3个 | 跨Agent协调编排 |
| **总计** | **14个** | 完整Agent生态 |

---

## 工具类详解

### Week 1-2: 基础工具类 (9个)

#### 1. NutritionTools - 营养分析工具
**位置**: `com.xx.jaseatschoicejava.agent.tools.NutritionTools`

**主要方法**:
- `analyzeDishNutrition()` - 分析菜品营养成分
- `calculateMealCalories()` - 计算餐食总热量
- `getNutritionAdvice()` - 获取营养建议
- `compareDishes()` - 对比菜品营养

#### 2. NutritionRecordTools - 营养记录工具
**位置**: `com.xx.jaseatschoicejava.agent.tools.NutritionRecordTools`

**主要方法**:
- `recordDailyIntake()` - 记录每日摄入
- `getDailyReport()` - 获取每日报告
- `getWeeklySummary()` - 获取周总结
- `getNutritionTrends()` - 获取营养趋势

#### 3. RecommendationTools - 推荐工具
**位置**: `com.xx.jaseatschoicejava.agent.tools.RecommendationTools`

**主要方法**:
- `getPersonalRecommendations()` - 获取个性化推荐
- `recommendByFlavor()` - 按口味推荐
- `recommendByNutrition()` - 按营养推荐
- `recommendByPrice()` - 按价格推荐

#### 4. RecipeTools - 食谱工具
**位置**: `com.xx.jaseatschoicejava.agent.tools.RecipeTools`

**主要方法**:
- `getRecipeDetails()` - 获取食谱详情
- `searchRecipes()` - 搜索食谱
- `recommendRecipes()` - 推荐食谱
- `getCookingSteps()` - 获取烹饪步骤

#### 5. OrderTools - 订单工具
**位置**: `com.xx.jaseatschoicejava.agent.tools.OrderTools`

**主要方法**:
- `createOrder()` - 创建订单
- `getOrderStatus()` - 获取订单状态
- `cancelOrder()` - 取消订单
- `getOrderHistory()` - 获取订单历史

#### 6. CollectionTools - 收藏工具
**位置**: `com.xx.jaseatschoicejava.agent.tools.CollectionTools`

**主要方法**:
- `addFavorite()` - 添加收藏
- `removeFavorite()` - 取消收藏
- `getFavorites()` - 获取收藏列表
- `checkFavorite()` - 检查收藏状态

#### 7. UserTools - 用户工具
**位置**: `com.xx.jaseatschoicejava.agent.tools.UserTools`

**主要方法**:
- `getUserProfile()` - 获取用户资料
- `updateUserProfile()` - 更新用户资料
- `getHealthGoals()` - 获取健康目标
- `updateHealthGoals()` - 更新健康目标

#### 8. LocationTools - 位置工具
**位置**: `com.xx.jaseatschoicejava.agent.tools.system.LocationTools`

**主要方法**:
- `getCurrentLocation()` - 获取当前位置
- `calculateDistance()` - 计算距离
- `isInDeliveryRange()` - 检查配送范围
- `mapCampusLocation()` - 映射校园位置

#### 9. TimeTools - 时间工具
**位置**: `com.xx.jaseatschoicejava.agent.tools.system.TimeTools`

**主要方法**:
- `getCurrentTime()` - 获取当前时间
- `getTimePeriod()` - 获取时段
- `isBusinessHour()` - 检查营业时间
- `estimateDeliveryTime()` - 估算配送时间

### Week 3: 增强工具类 (9个)

#### 1. UserProfileTools - 用户资料工具
**位置**: `com.xx.jaseatschoicejava.agent.tools.user.UserProfileTools`

**主要方法**:
- `getCompleteProfile()` - 获取完整资料
- `updateBasicInfo()` - 更新基本信息
- `updateBodyData()` - 更新身体数据
- `analyzeProfileCompleteness()` - 分析资料完整度
- `getProfileImprovementSuggestions()` - 获取完善建议

#### 2. NutritionAnalysisTools - 营养分析工具
**位置**: `com.xx.jaseatschoicejava.agent.tools.nutrition.NutritionAnalysisTools`

**主要方法**:
- `analyzeDishNutritionDetail()` - 详细营养分析
- `analyzeMealNutrition()` - 餐食营养分析
- `analyzeDailyIntake()` - 每日摄入分析
- `generateNutritionReport()` - 生成营养报告
- `getNutrientAdvice()` - 获取营养素建议

#### 3. CalorieCalculatorTools - 热量计算工具
**位置**: `com.xx.jaseatschoicejava.agent.tools.nutrition.CalorieCalculatorTools`

**主要方法**:
- `calculateBMR()` - 计算基础代谢
- `calculateTDEE()` - 计算每日总消耗
- `calculateIdealWeight()` - 计算理想体重
- `calculateBMI()` - 计算BMI
- `calculateCalorieGoal()` - 计算热量目标

#### 4. RecommendationQueryTools - 推荐查询工具
**位置**: `com.xx.jaseatschoicejava.agent.tools.recommendation.RecommendationQueryTools`

**主要方法**:
- `queryRecommendedDishes()` - 查询推荐菜品
- `queryDishesByCategory()` - 按分类查询
- `queryDishesByTags()` - 按标签查询
- `getDishDetails()` - 获取菜品详情

#### 5. RecommendationFilterTools - 推荐筛选工具
**位置**: `com.xx.jaseatschoicejava.agent.tools.recommendation.RecommendationFilterTools`

**主要方法**:
- `filterByCalories()` - 按热量筛选
- `filterByPrice()` - 按价格筛选
- `filterByNutrition()` - 按营养筛选
- `filterByAllergens()` - 按过敏源筛选

#### 6. RecommendationRankTools - 推荐排序工具
**位置**: `com.xx.jaseatschoicejava.agent.tools.recommendation.RecommendationRankTools`

**主要方法**:
- `rankByRelevance()` - 按相关性排序
- `rankByRating()` - 按评分排序
- `rankByPopularity()` - 按热度排序
- `rankByHealthScore()` - 按健康分排序

#### 7. MerchantQueryTools - 商家查询工具
**位置**: `com.xx.jaseatschoicejava.agent.tools.merchant.MerchantQueryTools`

**主要方法**:
- `getMerchantInfo()` - 获取商家信息
- `searchMerchants()` - 搜索商家
- `getHotMerchants()` - 获取热门商家
- `filterMerchantsByRating()` - 按评分筛选
- `compareMerchants()` - 对比商家

#### 8. MerchantStatsTools - 商家统计工具
**位置**: `com.xx.jaseatschoicejava.agent.tools.merchant.MerchantStatsTools`

**主要方法**:
- `getMerchantStats()` - 获取商家统计
- `compareMerchantStats()` - 对比商家数据
- `getMerchantRanking()` - 获取商家排名
- `analyzeMerchantAdvantages()` - 分析商家优势

#### 9. OrderQueryTools & OrderCreateTools
**位置**: `com.xx.jaseatschoicejava.agent.tools.order`

**OrderQueryTools主要方法**:
- `queryOrderById()` - 按ID查询订单
- `queryUserOrders()` - 查询用户订单
- `getActiveOrders()` - 获取活跃订单
- `getOrderStatistics()` - 获取订单统计

**OrderCreateTools主要方法**:
- `createQuickOrder()` - 创建快速订单
- `validateOrder()` - 验证订单
- `calculateOrderTotal()` - 计算订单总额
- `submitOrder()` - 提交订单

---

## 智能体详解

### L1 基础智能体 (7个)

#### 1. UserPreferenceAgent - 用户偏好Agent
**位置**: `com.xx.jaseatschoicejava.agent.agents.UserPreferenceAgent`

**职责**: 管理用户饮食偏好和健康目标

**使用工具**: UserProfileTools

**核心能力**:
- 分析用户饮食偏好
- 记录和更新用户资料
- 提供个性化建议
- 跟踪健康目标进度

**提示词长度**: ~2,500字

**典型对话**:
- "我的资料"
- "更新身高体重"
- "我的健康目标"

#### 2. NutritionGuideAgent - 营养指导Agent
**位置**: `com.xx.jaseatschoicejava.agent.agents.NutritionGuideAgent`

**职责**: 提供营养分析和饮食指导

**使用工具**: NutritionAnalysisTools, CalorieCalculatorTools

**核心能力**:
- BMR/TDEE计算
- 营养素配比建议
- 健康目标推荐
- 饮食改善建议

**提示词长度**: ~3,000字

**典型对话**:
- "这个菜有多少卡路里"
- "我今天该吃多少"
- "减肥吃什么好"

#### 3. DishRecommendationAgent - 菜品推荐Agent
**位置**: `com.xx.jaseatschoicejava.agent.agents.DishRecommendationAgent`

**职责**: 智能菜品推荐和菜单查询

**使用工具**: RecommendationQueryTools, RecommendationFilterTools, RecommendationRankTools

**核心能力**:
- 多维度菜品推荐
- 菜品筛选和排序
- 个性化菜单生成
- 菜品详情查询

**提示词长度**: ~3,500字

**典型对话**:
- "推荐一些好吃的"
- "川菜推荐"
- "低卡路里菜品"

#### 4. MerchantInfoAgent - 商家信息Agent
**位置**: `com.xx.jaseatschoicejava.agent.agents.MerchantInfoAgent`

**职责**: 商家信息查询、搜索和对比

**使用工具**: MerchantQueryTools, MerchantStatsTools

**核心能力**:
- 商家信息查询
- 商家搜索
- 商家对比
- 商家评分分析

**提示词长度**: ~3,000字

**典型对话**:
- "第一食堂怎么样"
- "附近有哪些商家"
- "商家评分对比"

#### 5. TimeAwareAgent - 时间感知Agent
**位置**: `com.xx.jaseatschoicejava.agent.agents.TimeAwareAgent`

**职责**: 时间相关服务和推荐

**使用工具**: TimeTools

**核心能力**:
- 时段识别（6个时段）
- 营业时间确认
- 配送时间估算
- 时段推荐

**提示词长度**: ~2,800字

**典型对话**:
- "现在几点了"
- "现在是什么时段"
- "商家营业了吗"

#### 6. LocationServiceAgent - 位置服务Agent
**位置**: `com.xx.jaseatschoicejava.agent.agents.LocationServiceAgent`

**职责**: 位置相关服务和配送支持

**使用工具**: LocationTools

**核心能力**:
- 校园位置映射
- 距离计算
- 配送范围验证
- 位置相关推荐

**提示词长度**: ~3,000字

**典型对话**:
- "送到学生宿舍1栋"
- "距离有多远"
- "在配送范围吗"

#### 7. OrderHelperAgent - 订单辅助Agent
**位置**: `com.xx.jaseatschoicejava.agent.agents.OrderHelperAgent`

**职责**: 订单创建、查询和管理

**使用工具**: OrderQueryTools, OrderCreateTools

**核心能力**:
- 订单查询
- 订单创建
- 订单状态跟踪
- 订单历史管理

**提示词长度**: ~3,200字

**典型对话**:
- "我要订餐"
- "查询订单"
- "订单状态"

### L2 领域智能体 (4个)

#### 1. SmartRecommendationAgent - 智能推荐Agent
**位置**: `com.xx.jaseatschoicejava.agent.agents.SmartRecommendationAgent`

**职责**: 提供全方位的个性化推荐服务

**使用工具**:
- UserProfileTools
- RecommendationQueryTools, RecommendationFilterTools, RecommendationRankTools
- MerchantQueryTools, MerchantStatsTools
- NutritionAnalysisTools
- TimeTools, LocationTools

**核心能力**:
- **综合评分模型**:
  ```
  推荐分数 = 用户匹配度×30% + 健康适配度×25% +
             时段适配度×20% + 商家评分×15% + 性价比×10%
  ```
- **场景推荐**: 一人食、多人聚餐、加班、约会、健康、预算
- **时段推荐**: 早餐、午餐、晚餐、下午茶、夜宵

**提示词长度**: ~5,500字

**典型对话**:
- "有什么推荐的"
- "适合加班的晚餐"
- "约会推荐"

#### 2. HealthManagementAgent - 健康管理Agent
**位置**: `com.xx.jaseatschoicejava.agent.agents.HealthManagementAgent`

**职责**: 全面的健康管理和饮食指导

**使用工具**:
- UserProfileTools
- NutritionAnalysisTools
- CalorieCalculatorTools

**核心能力**:
- **健康评估**: BMI、体脂率、BMR、饮食评估、风险分析
- **目标制定**: 减脂、增肌、保持、增重
- **饮食计划**: 热量分配、营养配比、餐单规划
- **进度跟踪**: 体重、饮食、热量、目标达成率

**提示词长度**: ~5,000字

**典型对话**:
- "我想减肥"
- "制定健康计划"
- "我的BMI是多少"

#### 3. FullOrderAgent - 全流程订单Agent
**位置**: `com.xx.jaseatschoicejava.agent.agents.FullOrderAgent`

**职责**: 处理从选择到下单的完整流程

**使用工具**:
- UserProfileTools
- MerchantQueryTools
- RecommendationQueryTools
- OrderQueryTools, OrderCreateTools
- TimeTools, LocationTools

**核心能力**:
- **5阶段流程**:
  1. 需求了解（地址、时间、人数、预算）
  2. 商家选择（评分、距离、价格）
  3. 菜品选择（推荐、搭配、份量）
  4. 订单确认（信息核对、费用明细）
  5. 提交订单（最终检查、跟踪）
- **智能推荐**: 按人数、预算、时段推荐
- **问题处理**: 超范围、打烊、售罄、预算不足

**提示词长度**: ~4,800字

**典型对话**:
- "我要订餐"
- "帮我下单"
- "推荐一个商家"

#### 4. IntelligentAssistantAgent - 智能助手Agent
**位置**: `com.xx.jaseatschoicejava.agent.agents.IntelligentAssistantAgent`

**职责**: 处理各类用户问题并智能路由

**使用工具**: 全部12个工具

**核心能力**:
- **意图识别**: 判断用户想做什么
- **智能路由**: 路由到相应的L1/L2 Agent
- **多Agent协作**: 顺序、并行、聚合、冲突解决
- **8大问题分类**:
  1. 用户资料类
  2. 营养健康类
  3. 菜品推荐类
  4. 商家信息类
  5. 时间相关类
  6. 位置相关类
  7. 订餐相关类
  8. 闲聊问候类

**提示词长度**: ~5,500字

**典型对话**:
- "你好"
- "我想吃川菜，但是减肥期间，有什么推荐的吗"
- "今天吃什么好"

### L3 编排智能体 (3个)

#### 1. LifeServiceAgent - 生活服务编排Agent
**位置**: `com.xx.jaseatschoicejava.agent.agents.LifeServiceAgent`

**职责**: 协调整个订餐、健康、推荐等服务流程

**使用工具**: 全部12个工具

**核心能力**:
- **4种编排模式**:
  - 顺序编排：按逻辑顺序调用
  - 并行编排：同时调用多个Agent
  - 条件编排：根据条件选择
  - 循环编排：迭代调用
- **3种协作模式**:
  - 主从模式：L3主控，L1/L2执行
  - 协作模式：多Agent共同解决
  - 层级模式：L3→L2→L1
- **典型场景**:
  - 订餐+健康咨询（并行）
  - 完整订餐流程（顺序6步）
  - 健康目标规划（评估→规划→跟踪）
  - 日常饮食管理（时段+偏好）

**提示词长度**: ~4,500字

**典型对话**:
- "我要订午餐，送到学生宿舍1栋，我正在减肥，有什么推荐吗"
- "我想减肥，帮我制定计划"
- "今天吃什么好"

#### 2. DailyPlanningAgent - 每日规划Agent
**位置**: `com.xx.jaseatschoicejava.agent.agents.DailyPlanningAgent`

**职责**: 制定每日饮食和生活规划

**使用工具**:
- UserProfileTools
- RecommendationQueryTools, RecommendationFilterTools, RecommendationRankTools
- NutritionAnalysisTools, CalorieCalculatorTools
- TimeTools

**核心能力**:
- **三餐规划**:
  - 早餐25-30%（7:00-8:00）
  - 午餐35-40%（12:00-13:00）
  - 晚餐25-30%（18:00-19:00）
  - 加餐0-10%（10:00、15:30）
- **营养平衡**: 热量分配、营养配比、食物多样性
- **时段安排**: 规律用餐、禁食时间
- **生活协调**: 工作/学习/运动/社交
- **典型场景**: 工作日、学习日、运动日、减脂日、社交日

**提示词长度**: ~5,000字

**典型对话**:
- "帮我制定一个工作日的饮食计划"
- "明天我有考试，帮我安排饮食"
- "今天下午5点我要去健身，饮食怎么安排"

#### 3. GoalAchievementAgent - 目标达成Agent
**位置**: `com.xx.jaseatschoicejava.agent.agents.GoalAchievementAgent`

**职责**: 帮助用户达成长期健康目标

**使用工具**:
- UserProfileTools
- NutritionAnalysisTools, CalorieCalculatorTools
- RecommendationQueryTools, RecommendationFilterTools, RecommendationRankTools
- TimeTools

**核心能力**:
- **SMART目标设定**: 具体、可衡量、可达成、相关、有时限
- **里程碑规划**: 将大目标分解为小目标
- **进度跟踪**: 每日/每周/每月
- **激励支持**: 正面激励、困难疏导、持续陪伴
- **目标策略**:
  - 减脂：每周0.5-1kg，热量缺口300-500kcal
  - 增肌：每月0.5-1kg，热量盈余200-300kcal
  - 保持健康：热量平衡
  - 增重：每周0.5-1kg，热量盈余300-500kcal
- **困难应对**: 平台期、复食、动力不足、时间冲突

**提示词长度**: ~6,000字

**典型对话**:
- "我想在3个月内减重10kg"
- "帮我制定增肌计划"
- "我的进度怎么样"

---

## 配置说明

### LangChain4j配置类

**位置**: `com.xx.jaseatschoicejava.agent.config.LangChain4jConfig`

**核心配置**:

```java
@Configuration
public class LangChain4jConfig {

    @Resource
    private ZhipuAIConfig zhipuAIConfig;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return ZhipuAiChatModel.builder()
                .apiKey(zhipuAIConfig.getApiKey())
                .model(zhipuAIConfig.getModel())  // glm-4-plus
                .temperature(0.7)
                .maxRetries(2)
                .callTimeout(Duration.ofSeconds(60))
                .connectTimeout(Duration.ofSeconds(60))
                .build();
    }

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(20);
    }

    // 14个Agent Bean定义...
}
```

### Agent Bean定义示例

```java
@Bean
public UserPreferenceAgent userPreferenceAgent(
        ChatLanguageModel chatLanguageModel,
        ChatMemory chatMemory) {
    return AiServices.builder(UserPreferenceAgent.class)
            .chatLanguageModel(chatLanguageModel)
            .chatMemory(chatMemory)
            .tools(userProfileTools)
            .build();
}
```

### 配置参数

| 参数 | 值 | 说明 |
|------|---|------|
| model | glm-4-plus | ZhipuAI模型 |
| temperature | 0.7 | 创造性程度 |
| maxRetries | 2 | 最大重试次数 |
| timeout | 60秒 | 超时时间 |
| chatMemory | 20条 | 消息窗口大小 |

---

## 使用指南

### 基本使用

#### 1. 在Service中注入Agent

```java
@Service
public class AgentService {

    @Resource
    private IntelligentAssistantAgent intelligentAssistantAgent;

    public String chat(String userMessage) {
        return intelligentAssistantAgent.chat(userMessage);
    }
}
```

#### 2. 在Controller中调用

```java
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    @Resource
    private AgentService agentService;

    @PostMapping("/chat")
    public Result<String> chat(@RequestBody String message) {
        String response = agentService.chat(message);
        return Result.success(response);
    }
}
```

### Agent选择指南

| 用户需求 | 推荐Agent | 理由 |
|----------|-----------|------|
| 简单查询 | L1基础Agent | 专注、快速 |
| 领域问题 | L2领域Agent | 多工具协作 |
| 复杂流程 | L3编排Agent | 跨Agent协调 |
| 未知问题 | IntelligentAssistantAgent | 智能路由 |

### 典型使用场景

#### 场景1: 简单查询
```java
// 查询商家信息
String response = merchantInfoAgent.chat("第一食堂怎么样？");
```

#### 场景2: 智能推荐
```java
// 获取推荐
String response = smartRecommendationAgent.chat("有什么推荐的？");
```

#### 场景3: 健康管理
```java
// 制定健康计划
String response = healthManagementAgent.chat("我想减肥，帮我制定计划");
```

#### 场景4: 复杂编排
```java
// 订餐+健康咨询
String response = lifeServiceAgent.chat(
    "我要订午餐，送到学生宿舍1栋，我正在减肥，有什么推荐吗？"
);
```

---

## 测试指南

### 单元测试

**测试类位置**: `src/test/java/com/xx/jaseatschoicejava/agent/`

#### Agent集成测试

```java
@SpringBootTest
public class AgentIntegrationTest {

    @Autowired
    private UserPreferenceAgent userPreferenceAgent;

    @Test
    public void testUserPreferenceAgentChat() {
        String response = userPreferenceAgent.chat("你好");
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }
}
```

### 运行测试

```bash
# 编译测试
./mvnw test-compile

# 运行所有测试
./mvnw test

# 运行单个测试类
./mvnw test -Dtest=AgentIntegrationTest

# 运行单个测试方法
./mvnw test -Dtest=AgentIntegrationTest#testUserPreferenceAgentChat
```

### 测试覆盖

**Week 7测试目标**:
- ✅ Agent Bean创建测试
- ✅ Agent基础对话测试
- ⏳ Agent间通信测试
- ⏳ 性能测试
- ⏳ 压力测试

---

## 性能优化

### 1. 提示词优化

**当前状态**:
- L1平均: ~3,000字
- L2平均: ~4,000字
- L3平均: ~5,000字
- 总计: ~52,000字

**优化方向**:
- 精简冗余描述
- 结构化输出格式
- 减少重复内容

### 2. 工具调用优化

**当前策略**:
- L1使用1-2个工具
- L2使用7-9个工具
- L3使用12个工具

**优化方向**:
- 按需加载工具
- 缓存常用结果
- 并行调用独立工具

### 3. 内存优化

**当前配置**:
- ChatMemory: 20条消息
- 会话隔离: 每个用户独立

**优化方向**:
- 动态调整窗口大小
- 定期清理过期会话
- 使用Redis缓存会话

### 4. 响应速度优化

**当前性能**:
- 平均响应: 3-5秒
- 超时设置: 60秒

**优化方向**:
- 流式输出
- 预加载常用数据
- 异步处理

---

## 未来规划

### Week 8: 文档与部署

**待完成**:
1. ✅ 系统文档（本文档）
2. ⏳ API文档
3. ⏳ 部署指南
4. ⏳ 运维手册
5. ⏳ 用户手册

### 功能扩展

**计划中**:
1. **多轮对话优化**
   - 上下文记忆增强
   - 意图识别优化
   - 情感分析

2. **Agent能力增强**
   - 图像识别（菜品识别）
   - 语音交互
   - 多模态输入

3. **数据驱动优化**
   - 用户反馈学习
   - 推荐算法优化
   - 个性化增强

4. **系统集成**
   - 与订餐系统深度集成
   - 与支付系统对接
   - 与消息系统联动

### 性能优化

**计划中**:
1. Agent缓存机制
2. 数据库查询优化
3. 并发处理优化
4. 资源调度优化

---

## 附录

### A. Agent索引

| Agent | 层级 | 工具数 | 提示词长度 |
|-------|------|--------|------------|
| UserPreferenceAgent | L1 | 1 | ~2,500字 |
| NutritionGuideAgent | L1 | 2 | ~3,000字 |
| DishRecommendationAgent | L1 | 3 | ~3,500字 |
| MerchantInfoAgent | L1 | 2 | ~3,000字 |
| TimeAwareAgent | L1 | 1 | ~2,800字 |
| LocationServiceAgent | L1 | 1 | ~3,000字 |
| OrderHelperAgent | L1 | 2 | ~3,200字 |
| SmartRecommendationAgent | L2 | 9 | ~5,500字 |
| HealthManagementAgent | L2 | 3 | ~5,000字 |
| FullOrderAgent | L2 | 7 | ~4,800字 |
| IntelligentAssistantAgent | L2 | 12 | ~5,500字 |
| LifeServiceAgent | L3 | 12 | ~4,500字 |
| DailyPlanningAgent | L3 | 7 | ~5,000字 |
| GoalAchievementAgent | L3 | 7 | ~6,000字 |

### B. 工具索引

| 工具 | 类别 | 方法数 |
|------|------|--------|
| UserProfileTools | 用户 | 5 |
| NutritionAnalysisTools | 营养 | 5 |
| CalorieCalculatorTools | 营养 | 5 |
| RecommendationQueryTools | 推荐 | 4 |
| RecommendationFilterTools | 推荐 | 4 |
| RecommendationRankTools | 推荐 | 4 |
| MerchantQueryTools | 商家 | 5 |
| MerchantStatsTools | 商家 | 4 |
| OrderQueryTools | 订单 | 4 |
| OrderCreateTools | 订单 | 4 |
| LocationTools | 系统 | 4 |
| TimeTools | 系统 | 4 |
| NutritionTools | 基础 | 4 |
| NutritionRecordTools | 基础 | 4 |
| RecommendationTools | 基础 | 4 |
| RecipeTools | 基础 | 4 |
| OrderTools | 基础 | 4 |
| CollectionTools | 基础 | 4 |
| UserTools | 基础 | 4 |

### C. 术语表

- **Agent**: 智能体，基于LLM的自主代理
- **Tool**: 工具，Agent可以调用的功能函数
- **ChatMemory**: 对话记忆，存储对话历史
- **System Prompt**: 系统提示词，定义Agent角色和能力
- **User Message**: 用户消息，用户输入的文本
- **Temperature**: 温度参数，控制LLM输出的随机性
- **L1/L2/L3**: 三层架构，基础层/领域层/编排层

### D. 参考资料

- [LangChain4j官方文档](https://docs.langchain4j.dev/)
- [ZhipuAI API文档](https://open.bigmodel.cn/dev/api)
- [Spring Boot官方文档](https://spring.io/projects/spring-boot)

---

**文档版本**: v1.0
**最后更新**: 2026-03-24
**维护者**: Claude
