-- ====================================================================
-- 公告表升级脚本
-- 数据库：jia_shi_yi_xuan
-- 日期：2026-02-09
-- 说明：
--   1. 修复 merchant_id 字段类型（BIGINT -> VARCHAR，允许 NULL）
--   2. 添加新字段：type, priority, target_audience, read_count, read_users
--   3. 更新现有数据的 merchant_id
-- ====================================================================

USE jia_shi_yi_xuan;

-- ====================================================================
-- 第一步：修改 merchant_id 字段类型
-- ====================================================================

-- 先查看当前表结构
-- SHOW CREATE TABLE announcement;

-- 备份现有数据（将现有 merchant_id 转为带 M 前缀的字符串）
UPDATE announcement SET merchant_id = CONCAT('M', CAST(merchant_id AS CHAR))
WHERE merchant_id IS NOT NULL
  AND merchant_id REGEXP '^[0-9]+$';  -- 只更新纯数字的

-- 将系统公告的 merchant_id 设为 NULL（后续用 NULL 表示系统公告）
UPDATE announcement SET merchant_id = NULL
WHERE merchant_id = '0' OR merchant_id = '';

-- 修改字段类型（允许 NULL）
ALTER TABLE announcement
  MODIFY COLUMN merchant_id VARCHAR(64) NULL COMMENT '商家ID，NULL表示系统公告';

-- ====================================================================
-- 第二步：添加新字段
-- ====================================================================

-- 添加公告类型字段
ALTER TABLE announcement
  ADD COLUMN type VARCHAR(20) DEFAULT 'system' COMMENT '公告类型：system-系统公告, activity-活动公告, urgent-紧急公告, update-更新说明'
  AFTER content;

-- 添加优先级字段
ALTER TABLE announcement
  ADD COLUMN priority VARCHAR(20) DEFAULT 'normal' COMMENT '优先级：normal-普通, important-重要, urgent-紧急'
  AFTER type;

-- 添加目标用户群体字段
ALTER TABLE announcement
  ADD COLUMN target_audience VARCHAR(20) DEFAULT 'all' COMMENT '目标群体：all-全部用户, merchant-商家端, customer-用户端'
  AFTER priority;

-- 添加阅读量字段
ALTER TABLE announcement
  ADD COLUMN read_count BIGINT DEFAULT 0 COMMENT '阅读量'
  AFTER target_audience;

-- 添加阅读人数字段
ALTER TABLE announcement
  ADD COLUMN read_users BIGINT DEFAULT 0 COMMENT '阅读人数'
  AFTER read_count;

-- ====================================================================
-- 第三步：更新现有数据
-- ====================================================================

-- 为现有公告设置默认值
UPDATE announcement
SET type = 'system',
    priority = 'normal',
    target_audience = 'all',
    read_count = 0,
    read_users = 0
WHERE type IS NULL;

-- ====================================================================
-- 第四步：添加索引
-- ====================================================================

-- 为常用查询字段添加索引
CREATE INDEX idx_announcement_type ON announcement(type);
CREATE INDEX idx_announcement_priority ON announcement(priority);
CREATE INDEX idx_announcement_target ON announcement(target_audience);
CREATE INDEX idx_announcement_status ON announcement(status);
CREATE INDEX idx_announcement_merchant ON announcement(merchant_id);

-- ====================================================================
-- 验证
-- ====================================================================

-- 检查表结构
SHOW CREATE TABLE announcement;

-- 检查数据统计
SELECT
  type AS 公告类型,
  priority AS 优先级,
  target_audience AS 目标群体,
  status AS 状态,
  COUNT(*) AS 数量
FROM announcement
GROUP BY type, priority, target_audience, status
ORDER BY type, priority, target_audience, status;

-- 检查系统公告
SELECT
  id,
  title,
  merchant_id,
  type,
  priority,
  target_audience,
  status
FROM announcement
WHERE merchant_id IS NULL OR merchant_id = 'SYSTEM'
LIMIT 10;
