-- 创建待审核的测试菜品数据
-- 执行时间：2026-01-31

USE jia_shi_yi_xuan;

-- 查找商家ID
SET @merchant_id = (SELECT id FROM t_merchant LIMIT 1);

-- 如果没有商家ID，使用默认值
SET @merchant_id = IFNULL(@merchant_id, '7638432224340229');

-- 创建5个待审核的测试菜品
INSERT INTO t_dish (merchant_id, name, category, price, calorie, estimated_cooking_minutes, step_template, description, is_online, audit_status, view_count, order_count, favorite_count, avg_rating, stock, create_time, update_time)
VALUES
(@merchant_id, '宫保鸡丁', '川菜', 38.00, 320, 15, 'NORMAL', '经典川菜，酸甜微辣，鸡肉嫩滑', 1, 'PENDING', 0, 0, 0, 4.5, 100, NOW(), NOW()),
(@merchant_id, '鱼香肉丝', '川菜', 32.00, 280, 12, 'NORMAL', '无骨猪肉丝，配木耳笋丝，酸甜口', 1, 'PENDING', 0, 0, 0, 4.3, 100, NOW(), NOW()),
(@merchant_id, '水煮鱼', '川菜', 68.00, 450, 25, 'NORMAL', '草鱼片，豆芽配菜，麻辣鲜香', 1, 'PENDING', 0, 0, 0, 4.7, 50, NOW(), NOW()),
(@merchant_id, '麻婆豆腐', '川菜', 22.00, 180, 10, 'NORMAL', '嫩豆腐配肉末，麻辣味浓', 1, 'PENDING', 0, 0, 0, 4.2, 200, NOW(), NOW()),
(@merchant_id, '回锅肉', '川菜', 42.00, 380, 18, 'NORMAL', '五花肉片配青椒，咸鲜微辣', 1, 'PENDING', 0, 0, 0, 4.6, 80, NOW(), NOW());

-- 验证插入结果
SELECT id, name, category, price, audit_status, is_online
FROM t_dish
WHERE audit_status = 'PENDING'
ORDER BY create_time DESC;
