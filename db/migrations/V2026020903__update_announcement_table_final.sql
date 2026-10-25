-- ====================================================================
-- 公告表升级脚本（最终版）
-- 数据库：jia_shi_yi_xuan
-- 日期：2026-02-09
-- ====================================================================

USE jia_shi_yi_xuan;

-- 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- ====================================================================
-- 第一步：先修改列类型允许NULL
-- ====================================================================

-- 1. 先查看当前表结构
SELECT CONCAT('-- 当前merchant_id类型') AS '';

-- 2. 如果merchant_id是BIGINT且NOT NULL，先改为允许NULL
-- 尝试直接修改（可能会失败如果数据不符合）
ALTER TABLE announcement MODIFY COLUMN merchant_id BIGINT NULL;

-- 3. 清空现有数据（因为我们将用NULL表示系统公告）
UPDATE announcement SET merchant_id = NULL;

-- 4. 修改为VARCHAR类型
ALTER TABLE announcement MODIFY COLUMN merchant_id VARCHAR(64) NULL COMMENT '商家ID，NULL表示系统公告';

-- ====================================================================
-- 第二步：添加新字段
-- ====================================================================

-- 检查字段是否已存在
SELECT COUNT(*) INTO @column_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'jia_shi_yi_xuan'
  AND TABLE_NAME = 'announcement'
  AND COLUMN_NAME = 'type';

-- 只在字段不存在时添加
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE announcement ADD COLUMN type VARCHAR(20) DEFAULT ''system'' COMMENT ''公告类型：system-系统公告, activity-活动公告, urgent-紧急公告, update-更新说明'' AFTER content',
  'SELECT ''Column type already exists'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加优先级字段
SELECT COUNT(*) INTO @column_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'jia_shi_yi_xuan'
  AND TABLE_NAME = 'announcement'
  AND COLUMN_NAME = 'priority';

SET @sql = IF(@column_exists = 0,
  'ALTER TABLE announcement ADD COLUMN priority VARCHAR(20) DEFAULT ''normal'' COMMENT ''优先级：normal-普通, important-重要, urgent-紧急'' AFTER type',
  'SELECT ''Column priority already exists'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加目标用户群体字段
SELECT COUNT(*) INTO @column_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'jia_shi_yi_xuan'
  AND TABLE_NAME = 'announcement'
  AND COLUMN_NAME = 'target_audience';

SET @sql = IF(@column_exists = 0,
  'ALTER TABLE announcement ADD COLUMN target_audience VARCHAR(20) DEFAULT ''all'' COMMENT ''目标群体：all-全部用户, merchant-商家端, customer-用户端'' AFTER priority',
  'SELECT ''Column target_audience already exists'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加阅读量字段
SELECT COUNT(*) INTO @column_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'jia_shi_yi_xuan'
  AND TABLE_NAME = 'announcement'
  AND COLUMN_NAME = 'read_count';

SET @sql = IF(@column_exists = 0,
  'ALTER TABLE announcement ADD COLUMN read_count BIGINT DEFAULT 0 COMMENT ''阅读量'' AFTER target_audience',
  'SELECT ''Column read_count already exists'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加阅读人数字段
SELECT COUNT(*) INTO @column_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'jia_shi_yi_xuan'
  AND TABLE_NAME = 'announcement'
  AND COLUMN_NAME = 'read_users';

SET @sql = IF(@column_exists = 0,
  'ALTER TABLE announcement ADD COLUMN read_users BIGINT DEFAULT 0 COMMENT ''阅读人数'' AFTER read_count',
  'SELECT ''Column read_users already exists'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ====================================================================
-- 第三步：添加索引
-- ====================================================================

-- 创建索引（如果不存在）
CREATE INDEX IF NOT EXISTS idx_announcement_type ON announcement(type);
CREATE INDEX IF NOT EXISTS idx_announcement_priority ON announcement(priority);
CREATE INDEX IF NOT EXISTS idx_announcement_target ON announcement(target_audience);
CREATE INDEX IF NOT EXISTS idx_announcement_status ON announcement(status);
CREATE INDEX IF NOT EXISTS idx_announcement_merchant ON announcement(merchant_id);

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- ====================================================================
-- 验证
-- ====================================================================

-- 检查表结构
SHOW CREATE TABLE announcement;

-- 检查数据统计
SELECT
  IFNULL(type, 'NULL') AS 公告类型,
  IFNULL(priority, 'NULL') AS 优先级,
  IFNULL(target_audience, 'NULL') AS 目标群体,
  IFNULL(status, 'NULL') AS 状态,
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
WHERE merchant_id IS NULL
LIMIT 10;
