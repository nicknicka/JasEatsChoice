-- ==========================================
-- 添加自定义营养信息字段到食谱表
-- 创建日期：2026-02-15
-- ==========================================

-- 添加 custom_nutrition 字段（存储自定义营养信息）
ALTER TABLE `t_recipe`
ADD COLUMN `custom_nutrition` TEXT COMMENT '自定义营养信息(JSON格式)' AFTER `detail`;

-- 验证添加的字段
DESC `t_recipe`;
