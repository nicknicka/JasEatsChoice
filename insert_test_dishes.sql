-- =====================================================
-- 佳食宜选 - 菜品测试数据
-- 版本: 1.0
-- 创建日期: 2026-01-24
-- 说明: 为推荐系统补充丰富的菜品测试数据
-- =====================================================

-- 清空现有测试数据（可选）
-- TRUNCATE TABLE t_dish;
-- TRUNCATE TABLE t_dish_feature;

-- =====================================================
-- 1. 插入菜品基础数据
-- =====================================================
INSERT INTO t_dish (name, category, price, calorie, description, merchant_id, status, create_time, update_time) VALUES
-- 早餐类
('皮蛋瘦肉粥', '早餐', 12.00, 180, '经典粤式早餐粥品，营养暖胃', 1, 1, NOW(), NOW()),
('豆浆油条', '早餐', 8.00, 350, '传统中式早餐搭配', 1, 1, NOW(), NOW()),
('小笼包', '早餐', 15.00, 280, '皮薄馅多，汁水丰富', 1, 1, NOW(), NOW()),
('煎饼果子', '早餐', 10.00, 320, '天津风味早餐', 1, 1, NOW(), NOW()),
('馄饨', '早餐', 12.00, 220, '皮薄馅嫩，汤清味鲜', 1, 1, NOW(), NOW()),

-- 午餐/晚餐类
('宫保鸡丁', '川菜', 38.00, 450, '经典川菜，酸甜微辣，鸡肉嫩滑', 1, 1, NOW(), NOW()),
('鱼香肉丝', '川菜', 35.00, 380, '传统川菜，酸甜开胃', 1, 1, NOW(), NOW()),
('麻婆豆腐', '川菜', 28.00, 320, '麻辣鲜香，下饭神器', 1, 1, NOW(), NOW()),
('水煮鱼', '川菜', 68.00, 520, '麻辣鲜香，鱼肉滑嫩', 1, 1, NOW(), NOW()),
('回锅肉', '川菜', 42.00, 580, '川菜之首，肥而不腻', 1, 1, NOW(), NOW()),

('红烧肉', '家常菜', 45.00, 580, '色泽红亮，肥而不腻，入口即化', 1, 1, NOW(), NOW()),
('糖醋排骨', '家常菜', 52.00, 520, '酸甜可口，外酥里嫩', 1, 1, NOW(), NOW()),
('清蒸鲈鱼', '粤菜', 78.00, 280, '原汁原味，肉质鲜嫩', 1, 1, NOW(), NOW()),
('白切鸡', '粤菜', 58.00, 350, '皮爽肉滑，原汁原味', 1, 1, NOW(), NOW()),
('蒜蓉西兰花', '素菜', 22.00, 120, '清香爽脆，健康美味', 1, 1, NOW(), NOW()),

('羊肉火锅', '火锅', 128.00, 800, '暖身驱寒，滋补养生', 1, 1, NOW(), NOW()),
('牛肉火锅', '火锅', 118.00, 750, '肉质鲜嫩，汤底浓郁', 1, 1, NOW(), NOW()),
('番茄锅', '火锅', 88.00, 320, '酸甜开胃，老少皆宜', 1, 1, NOW(), NOW()),

-- 轻食/健康类
('鸡肉沙拉', '轻食', 32.00, 350, '低脂高蛋白，健康轻食', 1, 1, NOW(), NOW()),
('牛肉波奇饭', '轻食', 38.00, 420, '营养均衡，饱腹感强', 1, 1, NOW(), NOW()),
('蔬菜沙拉', '轻食', 25.00, 150, '清爽健康，减脂必备', 1, 1, NOW(), NOW()),
('全麦三明治', '轻食', 28.00, 280, '粗粮细作，营养健康', 1, 1, NOW(), NOW()),
('藜麦饭', '轻食', 35.00, 320, '超级食物，营养满分', 1, 1, NOW(), NOW()),

-- 汤品类
('西湖牛肉羹', '汤类', 28.00, 180, '杭州名菜，鲜香滑嫩', 1, 1, NOW(), NOW()),
('酸辣汤', '汤类', 18.00, 120, '开胃爽口，酸辣鲜香', 1, 1, NOW(), NOW()),
('冬瓜排骨汤', '汤类', 32.00, 220, '清淡滋补，清热解暑', 1, 1, NOW(), NOW()),
('紫菜蛋花汤', '汤类', 12.00, 80, '简单快捷，营养丰富', 1, 1, NOW(), NOW()),
('菌菇汤', '汤类', 38.00, 160, '鲜美醇厚，增强免疫', 1, 1, NOW(), NOW()),

-- 主食类
('扬州炒饭', '主食', 22.00, 420, '粒粒分明，香味浓郁', 1, 1, NOW(), NOW()),
('广式腊肠煲仔饭', '主食', 35.00, 580, '锅巴香脆，腊味浓郁', 1, 1, NOW(), NOW()),
('牛肉面', '主食', 26.00, 520, '汤浓面劲，牛肉软烂', 1, 1, NOW(), NOW()),
('重庆小面', '主食', 18.00, 480, '麻辣鲜香，重庆风味', 1, 1, NOW(), NOW()),
('日式拉面', '主食', 38.00, 550, '汤底浓郁，面条劲道', 1, 1, NOW(), NOW()),

-- 甜点/小吃类
('提拉米苏', '甜点', 32.00, 380, '意大利经典甜点', 1, 1, NOW(), NOW()),
('芒果布丁', '甜点', 18.00, 220, '口感顺滑，芒果香甜', 1, 1, NOW(), NOW()),
('红豆薏米汤', '甜点', 15.00, 150, '祛湿排毒，养颜美白', 1, 1, NOW(), NOW()),
('冰糖雪梨', '甜点', 16.00, 120, '润肺止咳，清热解毒', 1, 1, NOW(), NOW()),
('双皮奶', '甜点', 18.00, 200, '香甜嫩滑，广东名点', 1, 1, NOW(), NOW()),

-- 饮品类
('冰爽柠檬水', '饮品', 8.00, 50, '清爽解渴，夏日必备', 1, 1, NOW(), NOW()),
('现磨豆浆', '饮品', 10.00, 120, '浓郁香醇，营养健康', 1, 1, NOW(), NOW()),
('鲜榨橙汁', '饮品', 18.00, 140, '维生素C满满', 1, 1, NOW(), NOW()),
('珍珠奶茶', '饮品', 22.00, 380, 'Q弹爽滑，奶香浓郁', 1, 1, NOW(), NOW()),
('菊花茶', '饮品', 12.00, 30, '清热降火，清肝明目', 1, 1, NOW(), NOW());

-- =====================================================
-- 2. 插入菜品特征数据
-- =====================================================
INSERT INTO dish_features (dish_id, category, tags, price_level, popularity_score, created_time, updated_time) VALUES
-- 早餐类特征
((SELECT id FROM t_dish WHERE name='皮蛋瘦肉粥'), '早餐', '["粥品", "暖胃", "营养", "早餐"]', 1, 75.5, NOW(), NOW()),
((SELECT id FROM t_dish WHERE name='豆浆油条'), '早餐', '["经典", "搭配", "传统", "早餐"]', 1, 82.3, NOW(), NOW()),
((SELECT id FROM t_dish WHERE name='小笼包'), '早餐', '["皮薄", "汁多", "精致", "早餐"]', 2, 88.6, NOW(), NOW()),
((SELECT id FROM t_dish WHERE name='煎饼果子'), '早餐', '["天津", "风味", "饱腹", "早餐"]', 1, 79.2, NOW(), NOW()),
((SELECT id FROM t_dish WHERE name='馄饨'), '早餐', '["皮薄", "汤鲜", "清淡", "早餐"]', 1, 76.8, NOW(), NOW()),

-- 川菜特征
((SELECT id FROM t_dish WHERE name='宫保鸡丁'), '川菜', '["麻辣", "酸甜", "鸡肉", "下饭"]', 3, 91.5, NOW(), NOW()),
((SELECT id FROM t_dish WHERE name='鱼香肉丝'), '川菜', '["酸甜", "开胃", "下饭", "经典"]', 3, 89.3, NOW(), NOW()),
((SELECT id FROM t_dish WHERE name='麻婆豆腐'), '川菜', '["麻辣", "素食", "下饭", "经典"]', 2, 92.1, NOW(), NOW()),
((SELECT id FROM t_dish WHERE name='水煮鱼'), '川菜', '["麻辣", "鲜嫩", "重口味", "聚餐"]', 4, 87.6, NOW(), NOW()),
((SELECT id FROM t_dish WHERE name='回锅肉'), '川菜', '["肥而不腻", "下饭", "经典", "川菜"]', 3, 90.4, NOW(), NOW()),

-- 家常菜特征
((SELECT id FROM t_dish WHERE name='红烧肉'), '家常菜', '["肥而不腻", "入口即化", "经典", "下饭"]', 4, 94.2, NOW(), NOW()),
((SELECT id FROM t_dish WHERE name='糖醋排骨'), '家常菜', '["酸甜", "外酥里嫩", "下饭", "经典"]', 4, 88.9, NOW(), NOW()),
((SELECT id FROM t_dish WHERE name='清蒸鲈鱼'), '粤菜', '["清淡", "鲜嫩", "原汁原味", "健康"]', 4, 93.5, NOW(), NOW()),
((SELECT id FROM t_dish WHERE name='白切鸡'), '粤菜', '["皮爽肉滑", "清淡", "原味", "经典"]', 3, 85.7, NOW(), NOW()),
((SELECT id FROM t_dish WHERE name='蒜蓉西兰花'), '素菜', '["清爽", "健康", "低脂", "素食"]', 2, 80.1, NOW(), NOW()),

-- 火锅特征
((SELECT id FROM t_dish WHERE name='羊肉火锅'), '火锅', '["暖身", "滋补", "聚餐", "冬季"]', 5, 86.8, NOW(), NOW()),
((SELECT id FROM t_dish WHERE name='牛肉火锅'), '火锅', '["鲜美", "滋补", "聚餐", "牛肉"]', 5, 84.5, NOW(), NOW()),
((SELECT id FROM t_dish WHERE name='番茄锅'), '火锅', '["酸甜", "开胃", "老少皆宜", "清淡"]', 4, 81.2, NOW(), NOW()),

-- 轻食特征
((SELECT id FROM t_dish WHERE name='鸡肉沙拉'), '轻食', '["低脂", "高蛋白", "健康", "减脂"]', 3, 78.9, NOW(), NOW()),
((SELECT id FROM t_dish WHERE name='牛肉波奇饭'), '轻食', '["营养均衡", "饱腹", "健康", "轻食"]', 3, 76.4, NOW(), NOW()),
((SELECT id FROM t_dish WHERE name='蔬菜沙拉'), '轻食', '["清爽", "低卡", "健康", "素食"]', 2, 82.6, NOW(), NOW()),
((SELECT id FROM t_dish WHERE name='全麦三明治'), '轻食', '["粗粮", "健康", "便携", "早餐"]', 3, 75.3, NOW(), NOW()),
((SELECT id FROM t_dish WHERE name='藜麦饭'), '轻食', '["超级食物", "营养", "健康", "轻食"]', 3, 73.8, NOW(), NOW()),

-- 甜点特征
((SELECT id FROM t_dish WHERE name='提拉米苏'), '甜点', '["经典", "浓郁", "精致", "下午茶"]', 3, 86.4, NOW(), NOW()),
((SELECT id FROM t_dish WHERE name='芒果布丁'), '甜点', '["顺滑", "香甜", "清爽", "甜品"]', 2, 79.5, NOW(), NOW()),
((SELECT id FROM t_dish WHERE name='红豆薏米汤'), '甜点', '["祛湿", "养颜", "健康", "甜品"]', 1, 74.2, NOW(), NOW());

-- =====================================================
-- 3. 插入用户画像测试数据
-- =====================================================
INSERT INTO t_user_profile (user_id, preference_tags, dietary_restrictions, diet_goal, flavor_preference, price_preference, nutrition_goals, meal_pattern, statistics, last_updated) VALUES
('1', '[{"tag":"川菜", "score":0.9}, {"tag":"辣", "score":0.8}, {"tag":"下饭", "score":0.7}]', '[]', 'balanced', '{"spicy": 0.8, "sweet": 0.5, "salty": 0.6}', '{"min": 20, "max": 80, "optimal": 45}', '{"calories": 2000, "protein": 80}', '{"breakfast": "07:30", "lunch": "12:00", "dinner": "18:30"}', '{"total_orders": 50, "avg_order_amount": 45.5, "fav_categories": ["川菜", "家常菜"]}', NOW()),

('2', '[{"tag":"轻食", "score":0.9}, {"tag":"健康", "score":0.85}, {"tag":"低脂", "score":0.8}]', '[]', 'low_calorie', '{"spicy": 0.2, "sweet": 0.4, "salty": 0.3}', '{"min": 15, "max": 50, "optimal": 30}', '{"calories": 1500, "protein": 100}', '{"breakfast": "07:00", "lunch": "12:00", "dinner": "18:00"}', '{"total_orders": 35, "avg_order_amount": 32.0, "fav_categories": ["轻食", "素菜"]}', NOW()),

('3', '[{"tag":"粤菜", "score":0.85}, {"tag":"清淡", "score":0.8}, {"tag":"海鲜", "score":0.75}]', '["海鲜过敏"]', 'balanced', '{"spicy": 0.3, "sweet": 0.6, "salty": 0.4}', '{"min": 30, "max": 100, "optimal": 60}', '{"calories": 1800, "protein": 90}', '{"breakfast": "08:00", "lunch": "12:30", "dinner": "19:00"}', '{"total_orders": 42, "avg_order_amount": 58.5, "fav_categories": ["粤菜", "汤类"]}', NOW());

-- =====================================================
-- 4. 插入用户行为测试数据
-- =====================================================
INSERT INTO t_user_behavior (user_id, behavior_type, item_type, item_id, context, duration, created_time) VALUES
-- 用户1的行为（喜欢川菜）
('1', 'order', 'dish', (SELECT id FROM t_dish WHERE name='宫保鸡丁'), '{"time": "12:00", "weather": "sunny"}', 300, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('1', 'order', 'dish', (SELECT id FROM t_dish WHERE name='麻婆豆腐'), '{"time": "18:30", "weather": "cloudy"}', 450, DATE_SUB(NOW(), INTERVAL 2 DAY)),
('1', 'click', 'dish', (SELECT id FROM t_dish WHERE name='水煮鱼'), '{"time": "12:15", "weather": "rainy"}', 120, DATE_SUB(NOW(), INTERVAL 3 DAY)),
('1', 'favorite', 'dish', (SELECT id FROM t_dish WHERE name='回锅肉'), '{"time": "19:00", "weather": "sunny"}', 180, DATE_SUB(NOW(), INTERVAL 4 DAY)),

-- 用户2的行为（喜欢轻食）
('2', 'order', 'dish', (SELECT id FROM t_dish WHERE name='鸡肉沙拉'), '{"time": "12:00", "weather": "sunny"}', 240, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('2', 'order', 'dish', (SELECT id FROM t_dish WHERE name='蔬菜沙拉'), '{"time": "18:00", "weather": "cloudy"}', 300, DATE_SUB(NOW(), INTERVAL 2 DAY)),
('2', 'click', 'dish', (SELECT id FROM t_dish WHERE name='牛肉波奇饭'), '{"time": "12:30", "weather": "sunny"}', 150, DATE_SUB(NOW(), INTERVAL 3 DAY)),
('2', 'favorite', 'dish', (SELECT id FROM t_dish WHERE name='藜麦饭'), '{"time": "07:30", "weather": "rainy"}', 200, DATE_SUB(NOW(), INTERVAL 5 DAY)),

-- 用户3的行为（喜欢粤菜）
('3', 'order', 'dish', (SELECT id FROM t_dish WHERE name='清蒸鲈鱼'), '{"time": "12:30", "weather": "cloudy"}', 360, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('3', 'order', 'dish', (SELECT id FROM t_dish WHERE name='白切鸡'), '{"time": "19:00", "weather": "sunny"}', 480, DATE_SUB(NOW(), INTERVAL 2 DAY)),
('3', 'click', 'dish', (SELECT id FROM t_dish WHERE name='冬瓜排骨汤'), '{"time": "12:00", "weather": "rainy"}', 90, DATE_SUB(NOW(), INTERVAL 3 DAY)),
('3', 'favorite', 'dish', (SELECT id FROM t_dish WHERE name='西湖牛肉羹'), '{"time": "18:30", "weather": "cloudy"}', 150, DATE_SUB(NOW(), INTERVAL 6 DAY));

-- =====================================================
-- 验证数据插入
-- =====================================================
-- SELECT COUNT(*) as dish_count FROM t_dish;
-- SELECT COUNT(*) as feature_count FROM t_dish_feature;
-- SELECT COUNT(*) as profile_count FROM t_user_profile;
-- SELECT COUNT(*) as behavior_count FROM t_user_behavior;

-- =====================================================
-- 数据统计信息
-- =====================================================
-- SELECT category, COUNT(*) as count, AVG(price) as avg_price FROM t_dish GROUP BY category;
-- SELECT JSON_LENGTH(tags) as tag_count, tags FROM t_dish_feature;
