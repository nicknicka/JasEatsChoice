-- =====================================================
-- 迁移脚本：将 tutorial 表的 ID 字段从 BIGINT 改为 VARCHAR
-- 版本：V2026013101
-- 日期：2026-01-31
-- 说明：支持雪花算法生成的 String 类型 ID
-- =====================================================

-- 修改 source_id 字段（来源ID: 管理员ID/商家ID/用户ID/AI版本）
ALTER TABLE tutorial MODIFY COLUMN source_id VARCHAR(64) COMMENT '来源ID: 管理员ID/商家ID/用户ID/AI版本';

-- 修改 author_id 字段（作者ID）
ALTER TABLE tutorial MODIFY COLUMN author_id VARCHAR(64) COMMENT '作者ID';

-- 修改 reviewer_id 字段（审核人ID）
ALTER TABLE tutorial MODIFY COLUMN reviewer_id VARCHAR(64) COMMENT '审核人ID';

-- 修改 linked_merchant_id 字段（关联商家ID）
ALTER TABLE tutorial MODIFY COLUMN linked_merchant_id VARCHAR(64) COMMENT '关联商家ID（商家教程可用）';

-- 修改 linked_dish_id 字段（关联菜品ID）
ALTER TABLE tutorial MODIFY COLUMN linked_dish_id VARCHAR(64) COMMENT '关联菜品ID（商家教程可用）';

-- 添加索引以优化查询性能
CREATE INDEX idx_author_id ON tutorial(author_id);
CREATE INDEX idx_source_id ON tutorial(source_id);
CREATE INDEX idx_linked_merchant_id ON tutorial(linked_merchant_id);
CREATE INDEX idx_linked_dish_id ON tutorial(linked_dish_id);
