-- ====================================================================
-- 公告表升级脚本（简化版）
-- 数据库：jia_shi_yi_xuan
-- 日期：2026-02-09
-- ====================================================================

USE jia_shi_yi_xuan;

-- ====================================================================
-- 第一步：修改 merchant_id 列类型
-- ====================================================================

-- 先允许NULL
ALTER TABLE announcement MODIFY COLUMN merchant_id BIGINT NULL;

-- 清空数据
UPDATE announcement SET merchant_id = NULL;

-- 改为VARCHAR
ALTER TABLE announcement MODIFY COLUMN merchant_id VARCHAR(64) NULL COMMENT '商家ID，NULL表示系统公告';

-- ====================================================================
-- 第二步：添加新字段
-- ====================================================================

ALTER TABLE announcement
  ADD COLUMN type VARCHAR(20) DEFAULT 'system' COMMENT '公告类型' AFTER content;

ALTER TABLE announcement
  ADD COLUMN priority VARCHAR(20) DEFAULT 'normal' COMMENT '优先级' AFTER type;

ALTER TABLE announcement
  ADD COLUMN target_audience VARCHAR(20) DEFAULT 'all' COMMENT '目标群体' AFTER priority;

ALTER TABLE announcement
  ADD COLUMN read_count BIGINT DEFAULT 0 COMMENT '阅读量' AFTER target_audience;

ALTER TABLE announcement
  ADD COLUMN read_users BIGINT DEFAULT 0 COMMENT '阅读人数' AFTER read_count;

-- ====================================================================
-- 验证
-- ====================================================================

SHOW COLUMNS FROM announcement;
