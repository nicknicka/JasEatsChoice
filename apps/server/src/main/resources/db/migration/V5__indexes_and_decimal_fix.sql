-- V5: 补充缺失的二级索引
-- 基于实际查询模式分析

-- cnt_recipe: 按 user_id + create_time 范围查询 + 按 update_time 排序
CREATE INDEX idx_recipe_user_create ON cnt_recipe (user_id, create_time);
CREATE INDEX idx_recipe_user_favorite ON cnt_recipe (user_id, is_favorite);

-- wlt_discount: 频繁按 merchant_id 查询商家优惠列表
CREATE INDEX idx_discount_merchant ON wlt_discount (merchant_id);

-- nut_food_categories: 字典表仅 111 条，全表扫描即可，暂不加索引

-- 修复金额字段精度：double → decimal(10,2)
-- 浮点数存储金额存在精度丢失风险
ALTER TABLE soc_group_order MODIFY COLUMN total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '拼单总金额';
ALTER TABLE wlt_discount MODIFY COLUMN discount_value DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额';
ALTER TABLE wlt_discount MODIFY COLUMN min_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '最低消费金额';
