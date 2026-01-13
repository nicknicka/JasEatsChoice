-- ========================================
-- 将钱包相关表的ID字段从BIGINT改为VARCHAR
-- ========================================

-- 注意：如果表中已有数据，请先备份！

-- 1. 修改钱包表 t_wallet
ALTER TABLE `t_wallet`
MODIFY COLUMN `id` VARCHAR(64) NOT NULL COMMENT '钱包ID';

-- 2. 修改支付记录表 t_payment_record
ALTER TABLE `t_payment_record`
MODIFY COLUMN `id` VARCHAR(64) NOT NULL COMMENT '支付记录ID';

-- 3. 修改充值记录表 t_recharge_record
ALTER TABLE `t_recharge_record`
MODIFY COLUMN `id` VARCHAR(64) NOT NULL COMMENT '充值记录ID';

-- 4. 修改提现记录表 t_withdraw_record
ALTER TABLE `t_withdraw_record`
MODIFY COLUMN `id` VARCHAR(64) NOT NULL COMMENT '提现记录ID';

-- 验证修改结果
SELECT
    't_wallet' AS table_name,
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'jia_shi_yi_xuan'
  AND TABLE_NAME = 't_wallet'
  AND COLUMN_NAME = 'id'

UNION ALL

SELECT
    't_payment_record' AS table_name,
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'jia_shi_yi_xuan'
  AND TABLE_NAME = 't_payment_record'
  AND COLUMN_NAME = 'id'

UNION ALL

SELECT
    't_recharge_record' AS table_name,
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'jia_shi_yi_xuan'
  AND TABLE_NAME = 't_recharge_record'
  AND COLUMN_NAME = 'id'

UNION ALL

SELECT
    't_withdraw_record' AS table_name,
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'jia_shi_yi_xuan'
  AND TABLE_NAME = 't_withdraw_record'
  AND COLUMN_NAME = 'id';
