-- 为用户表添加支付密码字段
-- 执行日期: 2026-01-13

USE jia_shi_yi_xuan;

-- 添加支付密码字段
ALTER TABLE t_user
ADD COLUMN payment_password VARCHAR(255) DEFAULT NULL COMMENT '支付密码（加密存储）' AFTER password;

-- 添加支付密码是否设置标识
ALTER TABLE t_user
ADD COLUMN has_payment_password TINYINT(1) DEFAULT 0 COMMENT '是否已设置支付密码：0-未设置，1-已设置' AFTER payment_password;

-- 创建索引
CREATE INDEX idx_has_payment_password ON t_user(has_payment_password);

-- 验证字段是否添加成功
SELECT
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'jia_shi_yi_xuan'
  AND TABLE_NAME = 't_user'
  AND COLUMN_NAME IN ('payment_password', 'has_payment_password');
