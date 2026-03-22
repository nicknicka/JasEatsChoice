# 佳食宜选 - AI全流程Agent架构设计

## 🎯 核心理念

**传统流程**：
```
用户浏览菜品 → 自己选择 → 自己下单 → 自己查询
```

**AI全流程**：
```
用户说出需求 → AI理解 → AI推荐 → AI帮下单 → AI跟踪订单
```

---

## 📊 重新设计：4个核心Agent

### 用户端（3个Agent）

```
┌─────────────────────────────────────────────────┐
│              1. 智能顾问Agent                    │
│              （大脑 - 理解和决策）                │
│  ┌──────────────────────────────────────────┐  │
│  │  核心能力：                                │  │
│  │  - 理解用户模糊需求                        │  │
│  │  - 协调其他Agent                          │  │
│  │  - 综合决策和建议                          │  │
│  └──────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
           ↓
    ┌──────┼──────┐
    ↓      ↓      ↓
┌──────┐┌──────┐┌──────┐
│营养  ││推荐  ││订单  │
│Agent ││Agent ││Agent │
└──────┘└──────┘└──────┘
```

### 商家端（1个Agent）

```
┌─────────────────────────────────────────────────┐
│              商家经营助手Agent                    │
│  - 订单管理                                      │
│  - 数据分析                                      │
│  - 智能客服                                      │
└─────────────────────────────────────────────────┘
```

---

## 🤖 详细设计：用户端Agent

### Agent 1：智能顾问Agent（主Agent）

**角色定位**：用户饮食私人助理

**核心能力**：
```java
@Service
public class IntelligentAdvisorAgent {

    /**
     * 智能顾问接口
     */
    public interface AdvisorAgent {
        @SystemMessage("""
            你是"佳食宜选"的智能饮食顾问，是用户的私人饮食助理。

            # 核心职责
            1. 理解用户的模糊需求和偏好
            2. 协调营养、推荐、订单三个专业Agent
            3. 为用户提供最佳的综合解决方案

            # 工作流程
            1. 倾听用户需求（可能很模糊）
            2. 询问必要信息（过敏源、预算、地址等）
            3. 调用专业Agent获取信息
            4. 综合分析并给出建议
            5. 帮助用户完成下单

            # 交互风格
            - 主动询问关键信息
            - 友好专业
            - 以用户利益为先
            - 透明说明推荐理由
        """)
        String chat(@UserMessage String message);
    }

    /**
     * 创建Agent（带工具调用）
     */
    public AdvisorAgent createAgent(String userId) {
        return AiServices.builder(AdvisorAgent.class)
            .chatLanguageModel(advisorChatModel)
            .chatMemory(chatMemory)
            .tools(new AdvisorTools(userId))
            .build();
    }
}
```

**使用场景示例**：

```
场景1：用户说"我想吃点好的"

顾问Agent：
├─ 理解：需求很模糊
├─ 询问：
│   ├─ "您的预算大概是多少？"
│   ├─ "想吃什么类型的菜？"
│   ├─ "有什么忌口吗？"
│   └─ "送哪里？"
├─ 获取信息后：
│   ├─ 调用推荐Agent → 获取附近高分商家
│   ├─ 调用营养Agent → 分析推荐菜品营养
│   └─ 综合建议："根据您的预算和口味，我推荐..."
└─ 帮助下单

────────────────────────────────────

场景2：用户说"帮我推荐附近的健康晚餐"

顾问Agent：
├─ 理解：需要健康、附近、晚餐
├─ 调用推荐Agent：
│   ├─ 推荐工具：获取附近商家
│   ├─ 过滤：健康标签
│   └─ 排序：距离 + 评分
├─ 调用营养Agent：
│   ├─ 分析推荐菜品的营养
│   └─ 评估健康度
├─ 综合结果：
│   "为您推荐以下3家附近餐厅的健康晚餐：
│    1. XX餐厅 - 清蒸鲈鱼（120卡，距离500m）
│    2. XX餐厅 - 鸡胸肉沙拉（180卡，距离800m）
│    3. XX餐厅 - 西兰花虾仁（150卡，距离1.2km）
│    基于健康度和距离，我最推荐第1家。
│    需要我帮您下单吗？"
└─ 等待用户确认 → 调用订单Agent下单
```

**工具定义**：
```java
public static class AdvisorTools {

    @Tool("获取用户当前位置和附近商家")
    NearbyInfo getNearbyInfo(String userId);

    @Tool("查询用户历史偏好")
    UserPreference getUserPreference(String userId);

    @Tool("调用营养分析Agent")
    String callNutritionAgent(String question);

    @Tool("调用推荐Agent")
    String callRecommendationAgent(String requirement);

    @Tool("调用订单Agent")
    String callOrderAgent(OrderRequest request);

    @Tool("生成个性化建议")
    PersonalizedSuggestion generateSuggestion(
        String userId,
        List<NutritionInfo> nutrition,
        List<Dish> recommendations
    );
}
```

---

### Agent 2：营养分析Agent

**角色定位**：专业营养师

**核心能力**：
```java
@Service
public class NutritionAgent {

    public interface NutritionAgent {
        @SystemMessage("""
            你是专业的营养师Agent。

            # 专业能力
            1. 精确计算营养成分
            2. 评估饮食健康度
            3. 提供科学营养建议

            # 工作原则
            - 数据必须准确
            - 建议必须科学
            - 不编造信息
        """)
        String analyze(@UserMessage String request);
    }

    public static class NutritionTools {
        @Tool("查询食物营养")
        NutritionInfo getNutrition(String food, Double amount);

        @Tool("评估饮食健康度")
        HealthAssessment assessHealth(List<Meal> meals);

        @Tool("计算每日热量")
        DailyCalorieSummary calculateDailyCalories(String userId, Date date);

        @Tool("分析营养均衡")
        NutritionBalance analyzeBalance(String userId, Integer days);
    }
}
```

---

### Agent 3：智能推荐Agent

**角色定位**：个性化推荐专家

**核心能力**：
```java
@Service
public class RecommendationAgent {

    public interface RecommendationAgent {
        @SystemMessage("""
            你是智能推荐Agent，专注于为用户推荐最合适的菜品。

            # 推荐原则
            1. 个性化：基于用户历史和偏好
            2. 多维度：口味、营养、价格、距离
            3. 透明化：说明推荐理由
            4. 多样性：避免重复推荐

            # 推荐维度
            - 口味匹配度
            - 营养健康度
            - 价格合理性
            - 距离便利性
            - 商家评分
        """)
        String recommend(@UserMessage String request);
    }

    public static class RecommendationTools {
        @Tool("推荐附近美食")
        List<Dish> recommendNearby(
            String location,
            String preference,
            Double maxDistance,
            Integer calorieLimit
        );

        @Tool("搜索菜品")
        List<Dish> searchDishes(
            String keyword,
            List<String> tags,
            Integer calorieMin,
            Integer calorieMax
        );

        @Tool("获取商家详情")
        MerchantDetail getMerchantDetail(String merchantId);

        @Tool("推荐搭配")
        List<Dish> recommendCombination(String mainDishId);

        @Tool("搜索食谱")
        Recipe searchRecipe(String dishName);

        @Tool("生成购物清单")
        ShoppingList generateShoppingList(List<Recipe> recipes);
    }
}
```

**关键实现：附近推荐**
```java
@Tool("推荐附近美食")
public List<Dish> recommendNearby(
    String userId,
    String preference,
    Double maxDistance,
    Integer calorieLimit
) {
    // 1. 获取用户位置
    Location userLocation = getLocationService().getUserLocation(userId);

    // 2. 查询附近商家（地理距离计算）
    List<Merchant> nearbyMerchants = merchantRepository
        .findNearby(userLocation, maxDistance);

    // 3. 获取商家菜品
    List<Dish> allDishes = dishRepository
        .findByMerchants(nearbyMerchants);

    // 4. 过滤（偏好、热量）
    List<Dish> filteredDishes = allDishes.stream()
        .filter(d -> matchPreference(d, preference))
        .filter(d -> calorieLimit == null || d.getCalories() <= calorieLimit)
        .collect(Collectors.toList());

    // 5. 排序（综合评分）
    return filteredDishes.stream()
        .sorted((d1, d2) -> {
            double score1 = calculateScore(d1, userId);
            double score2 = calculateScore(d2, userId);
            return Double.compare(score2, score1);
        })
        .limit(10)
        .collect(Collectors.toList());
}

/**
 * 综合评分算法
 */
private double calculateScore(Dish dish, String userId) {
    double score = 0;

    // 1. 菜品评分 (30%)
    score += dish.getRating() * 0.3;

    // 2. 用户偏好匹配度 (25%)
    UserPreference pref = getUserPreference(userId);
    score += calculatePreferenceMatch(dish, pref) * 0.25;

    // 3. 营养健康度 (20%)
    score += calculateHealthScore(dish) * 0.2;

    // 4. 距离便利性 (15%)
    score += calculateDistanceScore(dish, userId) * 0.15;

    // 5. 价格合理性 (10%)
    score += calculatePriceScore(dish, userId) * 0.1;

    return score;
}
```

---

### Agent 4：订单助手Agent ⭐新增

**角色定位**：订单管家

**核心能力**：
```java
@Service
public class OrderAgent {

    public interface OrderAgent {
        @SystemMessage("""
            你是智能订单助手，帮助用户高效完成订单操作。

            # 核心职责
            1. 理解用户的下单需求
            2. 帮助用户选择最合适的商家和菜品
            3. 智能填写订单信息
            4. 跟踪订单状态
            5. 处理订单问题

            # 工作原则
            - 确认关键信息（菜品、地址、电话）
            - 主动提示优惠信息
            - 及时告知订单状态
            - 遇到问题主动解决
        """)
        String handleOrder(@UserMessage String request);
    }

    public static class OrderTools {
        @Tool("创建订单")
        Order createOrder(CreateOrderRequest request);

        @Tool("查询订单状态")
        OrderStatus getOrderStatus(String orderId);

        @Tool("修改订单")
        Order modifyOrder(String orderId, OrderModification modification);

        @Tool("取消订单")
        RefundInfo cancelOrder(String orderId, String reason);

        @Tool("跟踪配送")
        DeliveryTracking trackDelivery(String orderId);

        @Tool("查询可用优惠")
        List<Coupon> getAvailableCoupons(String userId, String merchantId);

        @Tool("估算配送时间")
        Integer estimateDeliveryTime(String merchantId, String address);

        @Tool("智能填充地址")
        Address suggestAddress(String userId);

        @Tool("计算订单金额")
        OrderAmount calculateAmount(
            List<OrderItem> items,
            String couponId
        );
    }
}
```

**使用场景示例**：

```
场景1：用户说"帮我下单宫保鸡丁，送到学生宿舍3栋"

订单Agent：
├─ 理解需求：
│   ├─ 菜品：宫保鸡丁
│   ├─ 地址：学生宿舍3栋
│   └─ 缺少信息：具体商家？
├─ 主动询问：
│   "附近有3家商家做宫保鸡丁：
│   1. XX食堂 - ¥18，评分4.8，距离200m
│   2. XX餐厅 - ¥25，评分4.6，距离500m
│   3. XX外卖 - ¥22，评分4.5，距离800m
│   推荐第1家，性价比最高。可以吗？"
├─ 用户确认：
│   "可以"
├─ 智能填充：
│   ├─ 地址：自动填充历史地址"学生宿舍3栋201"
│   ├─ 电话：自动填充用户电话
│   └─ 优惠：自动查询可用优惠券
├─ 确认下单：
│   "订单确认：
│   - 菜品：宫保鸡丁
│   - 商家：XX食堂
│   - 金额：¥18（使用优惠券-¥2）
│   - 配送：学生宿舍3栋201
│   - 预计：15分钟送达
│   确认下单吗？"
└─ 用户确认 → 创建订单 → 返回订单号

────────────────────────────────────

场景2：用户说"我的订单到哪了？"

订单Agent：
├─ 理解：查询订单状态
├─ 查询最近订单
├─ 返回：
│   "您的订单（ORD123456）正在配送中：
│   - 骑手：李师傅
│   - 位置：距离您500米
│   - 预计：5分钟送达
│   - 联系电话：138****8888"
└─ 如果超时：
    "您的订单已超时10分钟，我已帮您催单，
     并申请了延迟赔付。"

────────────────────────────────────

场景3：用户说"我想吃辣的，有什么推荐，帮我点"

订单Agent（协同其他Agent）：
├─ 理解：需要推荐 + 下单
├─ 调用推荐Agent：
│   "为您推荐附近辣味菜品：
│   1. 麻婆豆腐（XX食堂，¥15）
│   2. 辣子鸡（XX餐厅，¥28）
│   3. 水煮鱼（XX川菜，¥35）"
├─ 调用营养Agent：
│   "营养分析：
│   - 麻婆豆腐：150卡，适中
│   - 辣子鸡：320卡，较高
│   - 水煮鱼：280卡，较高
│   建议搭配清淡菜品。"
├─ 综合建议：
│   "推荐麻婆豆腐+清炒时蔬，营养均衡。
│    需要帮您下单吗？"
├─ 用户确认：
│   "好的"
└─ 智能下单：
    ├─ 地址：自动填充
    ├─ 电话：自动填充
    ├─ 优惠：自动查询
    └─ 下单成功
```

**智能下单流程**：
```java
@Tool("智能下单")
public Order smartOrder(String userId, String requirement) {
    // 1. 理解用户需求
    OrderIntent intent = parseIntent(requirement);

    // 2. 调用推荐Agent获取推荐
    List<Dish> recommendations = callRecommendationAgent(
        userId,
        intent.getPreferences(),
        intent.getCalorieLimit()
    );

    // 3. 调用营养Agent分析
    List<NutritionInfo> nutrition = callNutritionAgent(recommendations);

    // 4. 综合建议
    OrderSuggestion suggestion = generateSuggestion(
        recommendations,
        nutrition
    );

    // 5. 确认用户
    if (!confirmWithUser(userId, suggestion)) {
        return null;
    }

    // 6. 智能填充订单信息
    CreateOrderRequest request = new CreateOrderRequest();
    request.setUserId(userId);
    request.setDishes(suggestion.getDishes());
    request.setAddress(getDefaultAddress(userId)); // 智能填充
    request.setPhone(getUserPhone(userId)); // 智能填充
    request.setCouponId(getBestCoupon(userId, suggestion.getMerchantId())); // 智能选择优惠券

    // 7. 创建订单
    return createOrder(request);
}
```

---

## 🤖 详细设计：商家端Agent

### Agent 5：商家经营助手Agent

**核心能力**：
```java
@Service
public class MerchantAssistantAgent {

    public interface MerchantAssistantAgent {
        @SystemMessage("""
            你是商家的智能经营助手。

            # 核心职责
            1. 分析经营数据
            2. 提供优化建议
            3. 处理订单问题
            4. 回复顾客咨询
        """)
        String assist(@UserMessage String request);
    }

    public static class MerchantTools {
        @Tool("分析销售数据")
        SalesAnalysis analyzeSales(String merchantId, DateRange range);

        @Tool("分析顾客评价")
        EvaluationAnalysis analyzeEvaluations(String merchantId);

        @Tool("菜品优化建议")
        List<DishOptimization> optimizeDishes(String merchantId);

        @Tool("定价建议")
        PricingSuggestion suggestPricing(String merchantId, String dishId);

        @Tool("库存预警")
        List<InventoryAlert> checkInventory(String merchantId);

        @Tool("回复顾客评价")
        String replyEvaluation(String evaluationId, String sentiment);

        @Tool("处理异常订单")
        void handleAbnormalOrder(String orderId);

        @Tool("营销策略建议")
        MarketingStrategy suggestMarketing(String merchantId);
    }
}
```

---

## 🔄 Agent协作：完整下单流程

```
用户："我想吃点健康的午餐，预算30元，帮我推荐并下单"

步骤1：智能顾问Agent接收
└─ 理解需求：健康、午餐、预算30元、需要下单

步骤2：收集信息
├─ 顾问："您的位置在哪里？"
├─ 用户："图书馆"
├─ 顾问："有什么忌口吗？"
└─ 用户："不吃海鲜"

步骤3：并行调用专业Agent
├─ 推荐Agent：
│   ├─ 查询附近商家（图书馆附近1km）
│   ├─ 过滤：健康标签、无海鲜、价格≤30元
│   └─ 返回：清蒸鲈鱼、鸡胸肉沙拉、西兰花虾仁
│
├─ 营养Agent：
│   ├─ 分析推荐菜品的营养
│   └─ 返回：都是低脂高蛋白，符合健康标准
│
└─ 顾问Agent综合：
    "为您推荐以下健康午餐：
     1. 清蒸鲈鱼套餐 - ¥28，180卡，距离300m
     2. 鸡胸肉沙拉 - ¥25，150卡，距离500m
     3. 西兰花虾仁 - ¥30，200卡，距离200m

     综合考虑营养、价格、距离，我最推荐西兰花虾仁。
     营养均衡，距离最近，可以帮您下单吗？"

步骤4：用户确认
└─ 用户："好的，就这个"

步骤5：调用订单Agent
├─ 智能填充信息：
│   ├─ 地址：自动填充"图书馆3楼阅览室"
│   ├─ 电话：自动填充用户电话
│   └─ 优惠：自动查询并使用优惠券
│
├─ 确认订单：
│   "订单确认：
│    - 菜品：西兰花虾仁
│    - 商家：XX餐厅
│    - 金额：¥30（优惠券-¥3）
│    - 配送：图书馆3楼
│    - 预计：20分钟送达
│    确认支付吗？"
│
└─ 用户："确认" → 创建订单

步骤6：订单跟踪
└─ 订单Agent自动跟踪：
    "您的订单已下单成功（ORD123456），
     商家已接单，预计20分钟送达。
     我会持续跟踪订单状态，有变化及时通知您。"

步骤7：配送完成
└─ 订单Agent："您的订单已送达，祝您用餐愉快！"
```

---

## 📊 最终架构：4个Agent

| Agent | 定位 | 核心职责 | 优先级 |
|-------|------|---------|--------|
| 智能顾问Agent | 大脑 | 理解需求、协调Agent、综合决策 | P0 |
| 营养分析Agent | 专家 | 营养计算、健康评估、饮食建议 | P0 |
| 智能推荐Agent | 专家 | 个性化推荐、附近美食、食谱搜索 | P0 |
| 订单助手Agent | 专家 | 智能下单、订单跟踪、问题处理 | P0 |
| 商家经营助手Agent | 专家 | 数据分析、经营建议、智能客服 | P1 |

---

## 🎯 实施计划（4-5周）

### 第1周：基础架构
```
├─ 搭建LangChain4j环境
├─ 配置智谱AI
├─ 创建数据库表
└─ 实现基础工具类
```

### 第2周：营养+推荐Agent
```
├─ 营养分析Agent开发
│   ├─ 营养计算工具
│   ├─ 健康评估工具
│   └─ Agent测试
│
└─ 智能推荐Agent开发
    ├─ 附近推荐算法
    ├─ 个性化排序
    └─ Agent测试
```

### 第3周：订单Agent
```
├─ 订单助手Agent开发
│   ├─ 智能下单工具
│   ├─ 订单跟踪工具
│   ├─ 地址智能填充
│   └─ 优惠智能匹配
│
└─ 与推荐Agent的协作测试
```

### 第4周：智能顾问Agent
```
├─ 智能顾问Agent开发
│   ├─ 对话管理
│   ├─ Agent协调
│   └─ 综合决策
│
└─ 完整流程测试
```

### 第5周：商家端+优化
```
├─ 商家经营助手Agent开发
├─ 性能优化
├─ 提示词优化
└─ 上线准备
```

---

## 💡 关键创新点

### 1. AI全流程下单
```
传统：浏览→选择→填写→下单
AI全流程：说需求→AI推荐→AI确认→AI下单

用户只需要：
"我想吃点健康的午餐，预算30元"

剩下的全部AI搞定：
✅ 理解需求
✅ 推荐菜品
✅ 分析营养
✅ 智能下单
✅ 跟踪订单
```

### 2. 附近美食智能推荐
```
基于位置服务：
├─ GPS定位
├─ 商家距离计算
├─ 配送时间估算
└─ 综合排序（距离+评分+营养）
```

### 3. 智能填充
```
记住用户偏好：
├─ 历史地址
├─ 常用电话
├─ 饮食偏好
├─ 过敏源
└─ 优惠信息

下单时自动填充，用户只确认即可
```

### 4. 主动服务
```
AI不是被动响应，而是主动：
├─ "您还没吃晚饭，需要推荐吗？"
├─ "您的订单已超时，我已催单"
├─ "根据您的饮食计划，今天该吃..."
└─ "附近新开了一家健康餐厅，试试吗？"
```

---

## ✅ 验收标准

### 用户体验
```
✅ 用户只需说出需求，AI理解并推荐
✅ 推荐结果精准（符合口味、营养、预算）
✅ 一键下单，信息自动填充
✅ 订单状态实时跟踪
✅ 遇到问题AI主动解决
```

### 技术指标
```
✅ 对话响应 < 2秒
✅ 推荐准确率 > 85%
✅ 下单成功率 > 95%
✅ 订单跟踪实时性 < 5秒
```

### 商业价值
```
✅ 下单转化率提升30%
✅ 用户满意度提升40%
✅ 复购率提升25%
✅ 客单价提升15%
```

---

## 🎓 总结

### 核心变化
```
之前：3个Agent
├─ 营养Agent
├─ 推荐Agent
└─ 商家助手Agent

现在：4个Agent（增加订单Agent）
├─ 智能顾问Agent（主）
├─ 营养分析Agent
├─ 智能推荐Agent
└─ 订单助手Agent ⭐新增
```

### 关键差异
```
❌ 之前：
用户自己浏览→自己选择→自己下单

✅ 现在：
AI理解需求→AI推荐→AI帮下单

真正的AI全流程！
```

需要我详细设计某个具体功能的实现吗？比如：
- 附近推荐算法
- 智能下单流程
- Agent协作机制
