-- ============================================================
-- 佳食宜选 - 未实现功能数据库表创建脚本
-- 生成日期：2026-02-11
-- ============================================================

-- ------------------------------------------------------------
-- 1. 用户优惠券表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_coupon` (
    `id` VARCHAR(64) NOT NULL COMMENT '优惠券ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `name` VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '优惠金额',
    `min_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '最低消费金额',
    `status` VARCHAR(20) NOT NULL DEFAULT 'available' COMMENT '优惠券状态：available-可用, used-已使用, expired-已过期',
    `order_id` VARCHAR(64) DEFAULT NULL COMMENT '关联订单ID',
    `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
    `use_time` DATETIME DEFAULT NULL COMMENT '使用时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户优惠券表';

-- ------------------------------------------------------------
-- 2. 更新钱包表，添加安全相关字段
-- ------------------------------------------------------------
-- 使用存储过程检查并添加字段
DELIMITER $$
DROP PROCEDURE IF EXISTS add_wallet_columns$$
CREATE PROCEDURE add_wallet_columns()
BEGIN
    -- 添加 locked 字段
    IF NOT EXISTS (
        SELECT * FROM information_schema.columns
        WHERE table_schema = DATABASE()
        AND table_name = 't_wallet'
        AND column_name = 'locked'
    ) THEN
        ALTER TABLE `t_wallet` ADD COLUMN `locked` TINYINT(1) DEFAULT 0 COMMENT '是否锁定（用于钱包安全设置）0-未锁定, 1-锁定';
    END IF;

    -- 添加 verify_enabled 字段
    IF NOT EXISTS (
        SELECT * FROM information_schema.columns
        WHERE table_schema = DATABASE()
        AND table_name = 't_wallet'
        AND column_name = 'verify_enabled'
    ) THEN
        ALTER TABLE `t_wallet` ADD COLUMN `verify_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否开启支付验证 0-关闭, 1-开启';
    END IF;

    -- 添加 daily_limit 字段
    IF NOT EXISTS (
        SELECT * FROM information_schema.columns
        WHERE table_schema = DATABASE()
        AND table_name = 't_wallet'
        AND column_name = 'daily_limit'
    ) THEN
        ALTER TABLE `t_wallet` ADD COLUMN `daily_limit` DECIMAL(10,2) DEFAULT 5000.00 COMMENT '单日交易限额';
    END IF;
END$$
DELIMITER ;

-- 执行存储过程
CALL add_wallet_columns();
-- 删除存储过程
DROP PROCEDURE IF EXISTS add_wallet_columns;

-- ------------------------------------------------------------
-- 3. 为现有地址表添加索引（如果需要）
-- ------------------------------------------------------------
-- 使用存储过程检查并添加 is_default 字段
DELIMITER $$
DROP PROCEDURE IF EXISTS add_address_column$$
CREATE PROCEDURE add_address_column()
BEGIN
    -- 添加 is_default 字段
    IF NOT EXISTS (
        SELECT * FROM information_schema.columns
        WHERE table_schema = DATABASE()
        AND table_name = 't_address'
        AND column_name = 'is_default'
    ) THEN
        ALTER TABLE `t_address` ADD COLUMN `is_default` INT DEFAULT 0 COMMENT '是否默认地址 0-否 1-是';
    END IF;
END$$
DELIMITER ;

-- 执行存储过程
CALL add_address_column();
-- 删除存储过程
DROP PROCEDURE IF EXISTS add_address_column;

-- ------------------------------------------------------------
-- 4. 插入测试优惠券数据（可选）
-- ------------------------------------------------------------
-- INSERT INTO `user_coupon` (`id`, `user_id`, `name`, `amount`, `min_amount`, `status`, `expire_time`)
-- VALUES ('1', '1', '新用户专享50元优惠券', 50.00, 100.00, 'available', DATE_ADD(NOW(), INTERVAL 30 DAY));

-- ------------------------------------------------------------
-- 5. 创建索引优化查询性能
-- ------------------------------------------------------------
-- 使用存储过程检查并创建索引
DELIMITER $$
DROP PROCEDURE IF EXISTS add_coupon_indexes$$
CREATE PROCEDURE add_coupon_indexes()
BEGIN
    -- 创建 idx_user_id_status 索引
    IF NOT EXISTS (
        SELECT * FROM information_schema.statistics
        WHERE table_schema = DATABASE()
        AND table_name = 'user_coupon'
        AND index_name = 'idx_user_id_status'
    ) THEN
        CREATE INDEX `idx_user_id_status` ON `user_coupon` (`user_id`, `status`);
    END IF;

    -- 创建 idx_user_id_expire 索引
    IF NOT EXISTS (
        SELECT * FROM information_schema.statistics
        WHERE table_schema = DATABASE()
        AND table_name = 'user_coupon'
        AND index_name = 'idx_user_id_expire'
    ) THEN
        CREATE INDEX `idx_user_id_expire` ON `user_coupon` (`user_id`, `expire_time`);
    END IF;
END$$
DELIMITER ;

-- 执行存储过程
CALL add_coupon_indexes();
-- 删除存储过程
DROP PROCEDURE IF EXISTS add_coupon_indexes;
