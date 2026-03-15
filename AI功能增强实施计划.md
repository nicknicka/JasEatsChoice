# 佳食宜选 AI功能增强实施计划

## 📋 功能总览

| 类别 | 功能数量 | 高优先级 | 中优先级 | 低优先级 |
|------|---------|---------|---------|---------|
| 订单管理增强 | 5 | 3 | 2 | 0 |
| 智能菜品推荐 | 4 | 2 | 1 | 1 |
| 营养健康分析 | 4 | 1 | 2 | 1 |
| 用户服务功能 | 4 | 1 | 2 | 1 |
| **合计** | **17** | **7** | **7** | **3** |

---

## 🎯 功能路线图

### 第一阶段：核心订单功能（预计1-2天）

#### 1.1 取消订单 ⭐⭐⭐ 高优先级
- **功能名称**：`cancel_order`
- **用户意图**：用户说"取消订单"、"不要了"
- **实现难度**：⭐⭐ 简单
- **依赖条件**：
  - 订单状态为"待确认"或"已确认"
  - 后端已有订单状态更新接口
- **技术实现**：
  ```java
  // 参数
  {
    "order_id": "订单ID"
  }

  // 返回
  {
    "success": true,
    "message": "订单已取消",
    "refund_amount": 58.00
  }
  ```
- **业务规则**：
  - 只能取消状态为0(待确认)或1(已确认)的订单
  - 已制作或配送中的订单不允许取消
  - 取消后需要退款（如有支付）
- **系统提示词**：
  ```
  当用户要求取消订单时：
  1. 确认要取消的订单号（如果用户未提供）
  2. 说明取消规则（哪些状态可以取消）
  3. 调用cancel_order函数
  4. 告知取消结果和退款信息
  ```

#### 1.2 查询订单状态 ⭐⭐⭐ 高优先级
- **功能名称**：`get_order_status`（已存在，需优化）
- **优化内容**：
  - 增加详细的订单进度信息
  - 显示预计等待时间
  - 支持模糊查询（"我的订单好了没"）
- **实现难度**：⭐ 简单
- **技术实现**：
  ```java
  // 参数
  {
    "order_id": "订单ID"
  }

  // 返回优化后
  {
    "order_id": "123456",
    "status": "制作中",
    "status_code": 2,
    "progress": "60%",
    "estimated_time": "约15分钟",
    "items": [
      {"name": "宫保鸡丁", "status": "制作中"},
      {"name": "红烧肉", "status": "等待中"}
    ]
  }
  ```

#### 1.3 催单功能 ⭐⭐ 高优先级
- **功能名称**：`urge_order`
- **用户意图**：用户说"快点"、"怎么还没好"、"催一下"
- **实现难度**：⭐ 简单
- **技术实现**：
  ```java
  // 参数
  {
    "order_id": "订单ID"
  }

  // 返回
  {
    "success": true,
    "message": "已通知商家加急处理",
    "estimated_time": "预计10分钟内完成"
  }
  ```
- **业务规则**：
  - 限制催单频率（5分钟内只能催1次）
  - 发送通知给商家端
  - 返回安抚性消息

---

### 第二阶段：智能推荐功能（预计2-3天）

#### 2.1 热门菜品推荐 ⭐⭐⭐ 高优先级
- **功能名称**：`get_hot_dishes`
- **用户意图**："有什么推荐的"、"热门菜"、"大家点什么"
- **实现难度**：⭐⭐ 中等
- **依赖条件**：需要基于销量数据统计
- **技术实现**：
  ```java
  // 参数
  {
    "category": "菜品分类（可选）",
    "limit": 10
  }

  // 返回
  {
    "hot_dishes": [
      {
        "id": 1,
        "name": "宫保鸡丁",
        "sales_count": 1258,
        "rating": 4.8,
        "price": 28.00,
        "reason": "本周销量冠军，好评率98%"
      }
    ]
  }
  ```
- **数据来源**：
  ```sql
  SELECT d.*, COUNT oi.id as sales_count, AVG(o.rating) as rating
  FROM dish d
  LEFT JOIN order_item oi ON d.id = oi.dish_id
  LEFT JOIN `order` o ON oi.order_id = o.id
  WHERE d.status = 1 AND o.create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
  GROUP BY d.id
  ORDER BY sales_count DESC
  LIMIT 10
  ```

#### 2.2 今日推荐 ⭐⭐⭐ 高优先级
- **功能名称**：`get_today_recommendations`
- **用户意图**："今天吃什么"、"今天有什么推荐"
- **实现难度**：⭐⭐⭐ 较复杂
- **推荐逻辑**：
  1. 时间因素（早餐/午餐/晚餐/夜宵）
  2. 季节因素（夏季清淡/冬季温补）
  3. 天气因素（雨天推荐热汤）
  4. 用户偏好（历史订单分析）
- **技术实现**：
  ```java
  // 参数
  {
    "meal_type": "breakfast/lunch/dinner/late_night" // 自动推断
  }

  // 返回
  {
    "meal_type": "午餐",
    "recommendations": [
      {
        "dish": "宫保鸡丁",
        "reason": "午餐首选，经典川菜",
        "match_score": 0.95
      }
    ]
  }
  ```
- **推荐算法**：
  ```
  匹配分数 = 时间匹配度 × 0.3 + 历史偏好 × 0.4 + 销量权重 × 0.3
  ```

#### 2.3 时间场景推荐 ⭐⭐ 中优先级
- **功能名称**：`get_time_recommendations`
- **用户意图**："早餐吃什么"、"宵夜推荐"
- **实现难度**：⭐⭐ 中等
- **场景划分**：
  - 早餐（6:00-9:00）：豆浆、油条、包子、粥
  - 午餐（11:00-13:00）：快餐、盖浇饭、面食
  - 晚餐（17:00-20:00）：炒菜、汤类、聚餐推荐
  - 夜宵（21:00-23:00）：烧烤、小龙虾、粥
- **技术实现**：
  ```java
  // 参数（自动推断当前时间）
  {
    "time_slot": "lunch", // 自动判断
    "weather": "晴" // 可选，接入天气API
  }

  // 返回
  {
    "time_slot": "午餐",
    "weather": "晴",
    "recommendations": [
      {
        "dish": "番茄鸡蛋面",
        "reason": "午餐便捷之选，适合晴朗天气",
        "nutrition": "碳水化合物丰富，提供充足能量"
      }
    ]
  }
  ```

#### 2.4 相似菜品推荐 ⭐ 低优先级
- **功能名称**：`get_similar_dishes`
- **用户意图**："和宫保鸡丁类似的菜"、"还有别的鸡肉菜吗"
- **实现难度**：⭐⭐⭐ 较复杂
- **推荐逻辑**：
  - 同分类菜品
  - 相似口味
  - 相似价格区间
  - 相同主料

---

### 第三阶段：营养健康功能（预计2-3天）

#### 3.1 今日热量统计 ⭐⭐ 中优先级
- **功能名称**：`get_today_calories`
- **用户意图**："我今天吃了多少卡路里"、"热量超标了吗"
- **实现难度**：⭐⭐ 中等
- **技术实现**：
  ```java
  // 参数
  {
    "user_id": "用户ID（自动获取）",
    "date": "2026-03-15" // 默认今天
  }

  // 返回
  {
    "date": "2026-03-15",
    "total_calories": 1850,
    "target_calories": 2000,
    "percentage": 92.5,
    "status": "正常范围内",
    "meals": [
      {"meal": "早餐", "calories": 450, "items": ["豆浆", "包子"]},
      {"meal": "午餐", "calories": 800, "items": ["宫保鸡丁", "米饭"]},
      {"meal": "晚餐", "calories": 600, "items": ["红烧肉", "青菜"]}
    ],
    "advice": "晚餐热量适中，还可摄入150卡路里的健康食品"
  }
  ```
- **数据计算**：
  ```sql
  SELECT
    DATE(o.create_time) as date,
    SUM(d.calories * oi.quantity) as total_calories
  FROM `order` o
  JOIN order_item oi ON o.id = oi.order_id
  JOIN dish d ON oi.dish_id = d.id
  WHERE o.user_id = ? AND DATE(o.create_time) = CURDATE()
  GROUP BY DATE(o.create_time)
  ```

#### 3.2 营养摄入分析 ⭐⭐ 中优先级
- **功能名称**：`analyze_nutrition_intake`
- **用户意图**："我的营养均衡吗"、"缺少什么营养"
- **实现难度**：⭐⭐⭐ 较复杂
- **营养维度**：
  - 蛋白质（Protein）
  - 碳水化合物（Carbs）
  - 脂肪（Fat）
  - 维生素（Vitamins）
  - 矿物质（Minerals）
- **技术实现**：
  ```java
  // 返回
  {
    "date": "2026-03-15",
    "nutrition": {
      "protein": {"value": 65, "target": 80, "percentage": 81, "status": "略低"},
      "carbs": {"value": 250, "target": 300, "percentage": 83, "status": "正常"},
      "fat": {"value": 55, "target": 60, "percentage": 92, "status": "正常"}
    },
    "recommendations": [
      "蛋白质摄入略低，建议增加豆类或蛋奶食品",
      "维生素摄入不足，建议多吃蔬菜水果"
    ]
  }
  ```
- **数据库要求**：
  - dish表需包含：protein, carbs, fat, vitamins, minerals字段

#### 3.3 BMI计算与分析 ⭐ 低优先级
- **功能名称**：`calculate_bmi`
- **用户意图**："我的BMI多少"、"健康吗"
- **实现难度**：⭐ 简单
- **技术实现**：
  ```java
  // 参数
  {
    "height": 175, // cm
    "weight": 70  // kg
  }

  // 返回
  {
    "bmi": 22.9,
    "category": "正常",
    "standard_range": "18.5-24.9",
    "health_advice": [
      "您的BMI在正常范围内，请保持",
      "建议每周运动3-5次，保持健康体重"
    ]
  }
  ```

#### 3.4 健康饮食建议 ⭐⭐ 中优先级
- **功能名称**：`get_health_advice`
- **用户意图**："健康饮食建议"、"怎么吃更健康"
- **实现难度**：⭐⭐ 中等
- **建议维度**：
  - 基于用户历史订单分析
  - 基于BMI和健康目标
  - 基于季节和天气
- **技术实现**：
  ```java
  // 返回
  {
    "user_profile": {
      "bmi": 24.5,
      "health_goal": "减重"
    },
    "advice": [
      "建议选择低油低盐的菜品，如清蒸、凉拌类",
      "增加蔬菜摄入，建议每餐1-2个蔬菜菜品",
      "控制主食分量，建议每餐150-200克"
    ],
    "recommended_dishes": [
      {"name": "清蒸鲈鱼", "reason": "高蛋白低脂肪"},
      {"name": "凉拌黄瓜", "reason": "清爽解腻"}
    ]
  }
  ```

---

### 第四阶段：用户服务功能（预计1-2天）

#### 4.1 查看收藏列表 ⭐⭐ 中优先级
- **功能名称**：`get_favorites`
- **用户意图**："我的收藏"、"收藏了什么"
- **实现难度**：⭐ 简单
- **技术实现**：
  ```java
  // 参数
  {
    "user_id": "用户ID（自动获取）"
  }

  // 返回
  {
    "total": 8,
    "favorites": [
      {
        "dish_id": 1,
        "dish_name": "宫保鸡丁",
        "price": 28.00,
        "image": "xxx.jpg",
        "added_time": "2026-03-10"
      }
    ]
  }
  ```

#### 4.2 添加收藏 ⭐⭐ 中优先级
- **功能名称**：`add_favorite`
- **用户意图**："收藏宫保鸡丁"、"加入收藏"
- **实现难度**：⭐ 简单
- **技术实现**：
  ```java
  // 参数
  {
    "dish_id": "菜品ID"
  }

  // 返回
  {
    "success": true,
    "message": "已添加到收藏"
  }
  ```
- **业务规则**：
  - 避免重复收藏
  - 收藏上限100个

#### 4.3 我的评价 ⭐⭐ 中优先级
- **功能名称**：`get_user_reviews`
- **用户意图**："我评价过什么"、"我的评价"
- **实现难度**：⭐ 简单
- **技术实现**：
  ```java
  // 参数
  {
    "user_id": "用户ID（自动获取）",
    "limit": 20
  }

  // 返回
  {
    "total": 15,
    "reviews": [
      {
        "order_id": "123456",
        "dish_name": "宫保鸡丁",
        "rating": 5,
        "comment": "味道很好，鸡肉嫩滑",
        "review_time": "2026-03-10"
      }
    ]
  }
  ```

#### 4.4 优惠券查询 ⭐ 低优先级
- **功能名称**：`get_user_coupons`
- **用户意图**："我的优惠券"、"有什么券"
- **实现难度**：⭐ 简单
- **技术实现**：
  ```java
  // 返回
  {
    "available": [
      {
        "coupon_id": "1",
        "type": "满减",
        "amount": 10,
        "condition": "满50可用",
        "expire_date": "2026-03-31"
      }
    ],
    "expired": []
  }
  ```

---

## 📊 实施优先级矩阵

### 高优先级（立即实施）⭐⭐⭐
1. **cancel_order** - 订单取消（基础功能）
2. **get_hot_dishes** - 热门菜品（提升转化率）
3. **get_today_recommendations** - 今日推荐（用户体验）
4. **get_order_status** - 订单状态优化（已有功能）
5. **urge_order** - 催单功能（用户诉求强）

### 中优先级（第二阶段）⭐⭐
6. **get_time_recommendations** - 时间场景推荐
7. **get_today_calories** - 热量统计
8. **analyze_nutrition_intake** - 营养分析
9. **get_health_advice** - 健康建议
10. **get_favorites** - 收藏列表
11. **add_favorite** - 添加收藏
12. **get_user_reviews** - 用户评价

### 低优先级（优化阶段）⭐
13. **calculate_bmi** - BMI计算
14. **get_similar_dishes** - 相似菜品
15. **get_user_coupons** - 优惠券查询

---

## 🔧 技术架构

### 数据库要求
```sql
-- 菜品表扩展
ALTER TABLE dish ADD COLUMN calories INT DEFAULT 0 COMMENT '热量(卡路里)';
ALTER TABLE dish ADD COLUMN protein DECIMAL(5,2) DEFAULT 0 COMMENT '蛋白质(g)';
ALTER TABLE dish ADD COLUMN carbs DECIMAL(5,2) DEFAULT 0 COMMENT '碳水(g)';
ALTER TABLE dish ADD COLUMN fat DECIMAL(5,2) DEFAULT 0 COMMENT '脂肪(g)';
ALTER TABLE dish ADD COLUMN sales_count INT DEFAULT 0 COMMENT '销量';
ALTER TABLE dish ADD COLUMN rating DECIMAL(3,2) DEFAULT 0 COMMENT '评分';

-- 收藏表
CREATE TABLE user_favorite (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  dish_id BIGINT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_dish (user_id, dish_id)
);

-- 营养目标表
CREATE TABLE user_nutrition_goal (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  target_calories INT DEFAULT 2000,
  target_protein DECIMAL(5,2) DEFAULT 80,
  target_carbs DECIMAL(5,2) DEFAULT 300,
  target_fat DECIMAL(5,2) DEFAULT 60,
  health_goal VARCHAR(50) DEFAULT '保持健康'
);
```

### API接口设计
```
POST /api/ai/function/cancel_order
POST /api/ai/function/urge_order
GET  /api/ai/function/get_hot_dishes
GET  /api/ai/function/get_today_recommendations
GET  /api/ai/function/get_time_recommendations
GET  /api/ai/function/get_today_calories
GET  /api/ai/function/analyze_nutrition
POST /api/ai/function/calculate_bmi
GET  /api/ai/function/get_health_advice
GET  /api/ai/function/get_favorites
POST /api/ai/function/add_favorite
GET  /api/ai/function/get_user_reviews
GET  /api/ai/function/get_user_coupons
```

---

## 📝 实施检查清单

### 第一阶段：订单管理增强 ✅
- [ ] 实现 `cancel_order` 函数
- [ ] 实现 `urge_order` 函数
- [ ] 优化 `get_order_status` 返回详情
- [ ] 更新系统提示词（订单取消/催单话术）
- [ ] 测试订单取消流程
- [ ] 测试催单功能
- [ ] 验证业务规则（状态限制、频率限制）

### 第二阶段：智能推荐 ✅
- [ ] 数据库添加销量和评分字段
- [ ] 实现热门菜品统计SQL
- [ ] 实现 `get_hot_dishes` 函数
- [ ] 实现 `get_today_recommendations` 函数
- [ ] 实现 `get_time_recommendations` 函数
- [ ] 更新系统提示词（推荐话术）
- [ ] 测试各时间段推荐
- [ ] 验证推荐算法准确性

### 第三阶段：营养健康 ✅
- [ ] 数据库添加营养字段
- [ ] 创建用户营养目标表
- [ ] 实现 `get_today_calories` 函数
- [ ] 实现 `analyze_nutrition_intake` 函数
- [ ] 实现 `calculate_bmi` 函数
- [ ] 实现 `get_health_advice` 函数
- [ ] 更新系统提示词（健康建议话术）
- [ ] 测试营养计算准确性

### 第四阶段：用户服务 ✅
- [ ] 创建收藏表
- [ ] 实现 `get_favorites` 函数
- [ ] 实现 `add_favorite` 函数
- [ ] 实现 `get_user_reviews` 函数
- [ ] 实现 `get_user_coupons` 函数
- [ ] 更新系统提示词（收藏/评价话术）
- [ ] 测试收藏功能
- [ ] 测试评价查询

---

## 📅 时间规划

| 阶段 | 功能 | 预计工时 | 完成日期 |
|------|------|---------|---------|
| 第一阶段 | 订单管理增强（3个功能） | 1-2天 | Day 2 |
| 第二阶段 | 智能推荐（3个功能） | 2-3天 | Day 5 |
| 第三阶段 | 营养健康（4个功能） | 2-3天 | Day 8 |
| 第四阶段 | 用户服务（4个功能） | 1-2天 | Day 10 |
| **总计** | **14个功能** | **6-10天** | **Day 10** |

---

## 🎯 成功指标

### 用户体验指标
- AI订单处理准确率 > 90%
- 推荐点击率提升 > 30%
- 用户满意度 > 4.5/5

### 技术性能指标
- 函数执行时间 < 2秒
- API响应时间 < 500ms
- 系统可用性 > 99.9%

### 业务价值指标
- 热门菜品订单转化率提升 > 20%
- 营养功能使用率 > 15%
- 收藏功能使用率 > 25%

---

## 📌 注意事项

### 开发规范
1. 所有AI函数需使用 `@AiFunctionHandler` 注解
2. 函数命名遵循 `动作_对象` 格式（如 `cancel_order`）
3. 统一使用 `AiFunctionType` 枚举管理
4. 返回消息使用友好的中文表述

### 测试要求
1. 每个功能至少3个测试用例
2. 包含正常流程和异常流程
3. 边界条件测试（空数据、超限等）
4. 用户体验测试（话术是否友好）

### 文档要求
1. 每个功能添加JavaDoc注释
2. 更新 API Function Calling 使用指南
3. 记录测试结果和问题
4. 维护功能清单文档

---

**文档版本**：v1.0
**创建日期**：2026-03-15
**最后更新**：2026-03-15
**负责人**：Claude AI Assistant
