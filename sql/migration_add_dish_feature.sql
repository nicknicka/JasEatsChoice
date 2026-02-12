-- =====================================================
-- 佳食宜选 - 加菜功能数据库迁移脚本
-- 版本: 1.0
-- 创建日期: 2026-01-24
-- 说明: 新增加菜请求表、加菜设置表，修改群订单表、订单表
-- =====================================================

-- =====================================================
-- 1. 新增加菜请求表
-- =====================================================
DROP TABLE IF EXISTS `t_add_dish_request`;
CREATE TABLE `t_add_dish_request` (
    `id` VARCHAR(64) PRIMARY KEY COMMENT '加菜请求ID',
    `group_order_id` BIGINT NOT NULL COMMENT '群订单ID',
    `original_order_id` VARCHAR(64) COMMENT '原订单ID(关联已支付订单)',
    `request_user_id` BIGINT NOT NULL COMMENT '加菜请求人ID',
    `merchant_id` BIGINT NOT NULL COMMENT '商家ID',

    -- 加菜内容
    `dish_info` JSON NOT NULL COMMENT '加菜菜品信息JSON: [{dishId,quantity,customization,price}]',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '加菜总金额',

    -- 审核相关
    `approval_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态: 0-待审核,1-审核通过,2-审核驳回,3-已撤回,4-超时驳回',
    `reject_reason` VARCHAR(255) COMMENT '驳回原因',
    `reviewer_id` BIGINT COMMENT '审核人ID(群订单发起者)',
    `review_time` DATETIME COMMENT '审核时间',

    -- 超时控制
    `first_remind_time` DATETIME COMMENT '首次提醒时间(10分钟)',
    `second_remind_time` DATETIME COMMENT '二次提醒时间',
    `timeout_time` DATETIME COMMENT '超时时间(15分钟)',

    -- 支付关联
    `related_payment_id` VARCHAR(64) COMMENT '关联支付记录ID',
    `related_order_id` VARCHAR(64) COMMENT '关联订单ID(审核通过后创建)',

    -- 通用字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX `idx_group_order_id` (`group_order_id`),
    INDEX `idx_approval_status` (`approval_status`),
    INDEX `idx_timeout_time` (`timeout_time`),
    INDEX `idx_request_user` (`request_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='加菜请求表';

-- =====================================================
-- 2. 新增加菜设置表
-- =====================================================
DROP TABLE IF EXISTS `t_add_dish_setting`;
CREATE TABLE `t_add_dish_setting` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `group_order_id` BIGINT NOT NULL UNIQUE COMMENT '群订单ID',
    `add_dish_permission` TINYINT NOT NULL DEFAULT 0 COMMENT '加菜权限: 0-全员可加菜,1-仅发起者可加菜',
    `budget_limit` DECIMAL(10,2) COMMENT '单次加菜预算限制(可选)',
    `max_dish_count` INT COMMENT '单次加菜数量限制(可选)',

    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX `idx_group_order_id` (`group_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='加菜设置表';

-- =====================================================
-- 3. 修改群订单表 - 新增支付模式和加菜开关
-- =====================================================
-- 检查列是否存在，不存在则添加
SET @dbname = DATABASE();
SET @tablename = 't_group_order';
SET @columnname1 = 'payment_mode';
SET @columnname2 = 'add_dish_enabled';

SET @preparedStatement1 = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
    AND TABLE_NAME = @tablename
    AND COLUMN_NAME = @columnname1
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN payment_mode TINYINT NOT NULL DEFAULT 0 COMMENT ''支付模式: 0-统一支付,1-个人单独支付'' AFTER status')
));
PREPARE alterIfNotExists1 FROM @preparedStatement1;
EXECUTE alterIfNotExists1;
DEALLOCATE PREPARE alterIfNotExists1;

SET @preparedStatement2 = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
    AND TABLE_NAME = @tablename
    AND COLUMN_NAME = @columnname2
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN add_dish_enabled TINYINT NOT NULL DEFAULT 1 COMMENT ''是否允许加菜: 0-否,1-是'' AFTER payment_mode')
));
PREPARE alterIfNotExists2 FROM @preparedStatement2;
EXECUTE alterIfNotExists2;
DEALLOCATE PREPARE alterIfNotExists2;

-- =====================================================
-- 4. 修改订单表 - 新增加菜订单关联字段
-- =====================================================
-- 检查列是否存在，不存在则添加
SET @tablename = 't_order';
SET @columnname1 = 'parent_order_id';
SET @columnname2 = 'is_add_order';
SET @columnname3 = 'add_dish_request_id';
SET @columnname4 = 'add_dish_user_id';

-- 添加 parent_order_id
SET @preparedStatement1 = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
    AND TABLE_NAME = @tablename
    AND COLUMN_NAME = @columnname1
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN parent_order_id VARCHAR(64) COMMENT ''父订单ID(加菜订单关联原订单)'' AFTER id')
));
PREPARE alterIfNotExists1 FROM @preparedStatement1;
EXECUTE alterIfNotExists1;
DEALLOCATE PREPARE alterIfNotExists1;

-- 添加 is_add_order
SET @preparedStatement2 = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
    AND TABLE_NAME = @tablename
    AND COLUMN_NAME = @columnname2
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN is_add_order TINYINT NOT NULL DEFAULT 0 COMMENT ''是否为加菜订单: 0-否,1-是'' AFTER parent_order_id')
));
PREPARE alterIfNotExists2 FROM @preparedStatement2;
EXECUTE alterIfNotExists2;
DEALLOCATE PREPARE alterIfNotExists2;

-- 添加 add_dish_request_id
SET @preparedStatement3 = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
    AND TABLE_NAME = @tablename
    AND COLUMN_NAME = @columnname3
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN add_dish_request_id VARCHAR(64) COMMENT ''加菜请求ID'' AFTER is_add_order')
));
PREPARE alterIfNotExists3 FROM @preparedStatement3;
EXECUTE alterIfNotExists3;
DEALLOCATE PREPARE alterIfNotExists3;

-- 添加 add_dish_user_id
SET @preparedStatement4 = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
    AND TABLE_NAME = @tablename
    AND COLUMN_NAME = @columnname4
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN add_dish_user_id BIGINT COMMENT ''加菜人ID'' AFTER add_dish_request_id')
));
PREPARE alterIfNotExists4 FROM @preparedStatement4;
EXECUTE alterIfNotExists4;
DEALLOCATE PREPARE alterIfNotExists4;

-- 添加索引
SET @indexname = 'idx_parent_order';
SET @preparedStatement5 = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @dbname
    AND TABLE_NAME = @tablename
    AND INDEX_NAME = @indexname
  ) > 0,
  'SELECT 1',
  CONCAT('CREATE INDEX idx_parent_order ON ', @tablename, ' (parent_order_id)')
));
PREPARE createIndexIfNotExists FROM @preparedStatement5;
EXECUTE createIndexIfNotExists;
DEALLOCATE PREPARE createIndexIfNotExists;

-- =====================================================
-- 5. 修改订单菜品表 - 新增加菜标识字段
-- =====================================================
-- 检查列是否存在，不存在则添加
SET @tablename = 't_order_dish';
SET @columnname1 = 'is_add_dish';
SET @columnname2 = 'add_dish_user_id';

-- 添加 is_add_dish
SET @preparedStatement1 = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
    AND TABLE_NAME = @tablename
    AND COLUMN_NAME = @columnname1
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN is_add_dish TINYINT NOT NULL DEFAULT 0 COMMENT ''是否加菜: 0-否,1-是'' AFTER customization')
));
PREPARE alterIfNotExists1 FROM @preparedStatement1;
EXECUTE alterIfNotExists1;
DEALLOCATE PREPARE alterIfNotExists1;

-- 添加 add_dish_user_id
SET @preparedStatement2 = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
    AND TABLE_NAME = @tablename
    AND COLUMN_NAME = @columnname2
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN add_dish_user_id BIGINT COMMENT ''加菜人ID'' AFTER is_add_dish')
));
PREPARE alterIfNotExists2 FROM @preparedStatement2;
EXECUTE alterIfNotExists2;
DEALLOCATE PREPARE alterIfNotExists2;

-- =====================================================
-- 6. 插入初始化数据（示例）
-- =====================================================
-- 为现有群订单创建默认加菜设置
INSERT INTO t_add_dish_setting (group_order_id, add_dish_permission, budget_limit, max_dish_count)
SELECT id, 0, NULL, NULL
FROM t_group_order
WHERE id NOT IN (SELECT group_order_id FROM t_add_dish_setting);

-- =====================================================
-- 迁移完成
-- =====================================================
SELECT '加菜功能数据库迁移完成！' AS message;
