-- 为菜品表添加库存字段
-- Date: 2026-01-27

-- 添加 stock 字段到 t_dish 表
ALTER TABLE t_dish ADD COLUMN stock INT DEFAULT 100 COMMENT '库存数量';

-- 为现有数据设置默认库存值
UPDATE t_dish SET stock = 100 WHERE stock IS NULL;
