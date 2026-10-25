-- 为 t_dish 表添加烹饪步骤和营养信息字段
-- 用于支持菜品详情页面展示

-- 添加烹饪步骤字段
ALTER TABLE t_dish
ADD COLUMN cooking_steps TEXT NULL COMMENT '烹饪步骤（JSON格式）' AFTER description;

-- 添加营养信息字段
ALTER TABLE t_dish
ADD COLUMN nutrition TEXT NULL COMMENT '营养信息（JSON格式）' AFTER cooking_steps;

-- 添加索引以提高查询性能
CREATE INDEX idx_cooking_steps ON t_dish(cooking_steps);
CREATE INDEX idx_nutrition ON t_dish(nutrition);
