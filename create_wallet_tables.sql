-- ========================================
-- 钱包和支付相关数据库表
-- ========================================

-- 1. 创建用户钱包表
DROP TABLE IF EXISTS `t_wallet`;
CREATE TABLE `t_wallet` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '钱包ID',
  `user_id` BIGINT NOT NULL UNIQUE COMMENT '用户ID',
  `balance` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '当前余额（元）',
  `total_recharge` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '累计充值金额',
  `total_consume` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '累计消费金额',
  `total_withdraw` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '累计提现金额',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '钱包状态：active-正常, frozen-冻结',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户钱包表';

-- 2. 创建支付记录表
DROP TABLE IF EXISTS `t_payment_record`;
CREATE TABLE `t_payment_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
  `payment_no` VARCHAR(64) NOT NULL UNIQUE COMMENT '支付流水号',
  `order_id` VARCHAR(64) NOT NULL COMMENT '订单ID',
  `user_id` BIGINT NOT NULL COMMENT '支付用户ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `amount` DECIMAL(10, 2) NOT NULL COMMENT '支付金额',
  `payment_method` VARCHAR(20) NOT NULL DEFAULT 'wallet' COMMENT '支付方式：wallet-钱包, wechat-微信, alipay-支付宝',
  `payment_status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '支付状态：pending-待支付, success-成功, failed-失败, refund-已退款',
  `transaction_id` VARCHAR(128) DEFAULT NULL COMMENT '第三方交易ID',
  `paid_time` DATETIME DEFAULT NULL COMMENT '支付完成时间',
  `refund_amount` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '退款金额',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`payment_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- 3. 创建充值记录表
DROP TABLE IF EXISTS `t_recharge_record`;
CREATE TABLE `t_recharge_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '充值记录ID',
  `recharge_no` VARCHAR(64) NOT NULL UNIQUE COMMENT '充值流水号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `amount` DECIMAL(10, 2) NOT NULL COMMENT '充值金额',
  `payment_method` VARCHAR(20) NOT NULL COMMENT '支付方式：wechat-微信, alipay-支付宝, bank-银行卡',
  `recharge_status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '充值状态：pending-待支付, success-成功, failed-失败',
  `transaction_id` VARCHAR(128) DEFAULT NULL COMMENT '第三方交易ID',
  `paid_time` DATETIME DEFAULT NULL COMMENT '支付完成时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_recharge_no` (`recharge_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`recharge_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充值记录表';

-- 4. 创建提现记录表
DROP TABLE IF EXISTS `t_withdraw_record`;
CREATE TABLE `t_withdraw_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '提现记录ID',
  `withdraw_no` VARCHAR(64) NOT NULL UNIQUE COMMENT '提现流水号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `amount` DECIMAL(10, 2) NOT NULL COMMENT '提现金额',
  `fee` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '手续费',
  `actual_amount` DECIMAL(10, 2) NOT NULL COMMENT '实际到账金额',
  `withdraw_method` VARCHAR(20) NOT NULL COMMENT '提现方式：wechat-微信, alipay-支付宝, bank-银行卡',
  `account_info` VARCHAR(200) NOT NULL COMMENT '提现账号信息（脱敏）',
  `withdraw_status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '提现状态：pending-待审核, approved-已通过, rejected-已拒绝, processing-处理中, success-成功, failed-失败',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `audit_user` VARCHAR(50) DEFAULT NULL COMMENT '审核人',
  `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_withdraw_no` (`withdraw_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`withdraw_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提现记录表';

-- 5. 扩展订单表，添加支付相关字段
ALTER TABLE `t_order`
ADD COLUMN `payment_id` BIGINT DEFAULT NULL COMMENT '支付记录ID' AFTER `status`,
ADD COLUMN `paid_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '已支付金额' AFTER `payment_id`,
ADD COLUMN `payment_time` DATETIME DEFAULT NULL COMMENT '支付时间' AFTER `paid_amount`,
ADD INDEX `idx_payment_id` (`payment_id`);

-- 6. 初始化钱包数据（为现有用户创建钱包）
INSERT INTO `t_wallet` (`user_id`, `balance`, `total_recharge`, `total_consume`, `total_withdraw`, `status`)
SELECT `user_id`, 0.00, 0.00, 0.00, 0.00, 'active'
FROM `t_user`
WHERE NOT EXISTS (SELECT 1 FROM `t_wallet` WHERE `t_wallet`.`user_id` = `t_user`.`user_id`);
