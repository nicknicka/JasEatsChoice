-- 为群订单表添加总金额字段
-- Date: 2026-01-28

-- 添加 total_amount 字段到 t_group_order 表
ALTER TABLE t_group_order ADD COLUMN total_amount DOUBLE DEFAULT 0 COMMENT '订单总金额';

-- 为现有数据设置默认值
UPDATE t_group_order SET total_amount = 0 WHERE total_amount IS NULL;
