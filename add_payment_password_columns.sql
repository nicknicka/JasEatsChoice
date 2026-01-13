-- 添加支付密码相关字段到 t_user 表
-- 执行时间: 2026-01-13

USE jaseatschoice;

-- 添加支付密码字段
ALTER TABLE t_user ADD COLUMN payment_password VARCHAR(255) COMMENT '支付密码（加密存储）' AFTER password;

-- 添加是否已设置支付密码字段
ALTER TABLE t_user ADD COLUMN has_payment_password TINYINT(1) DEFAULT 0 COMMENT '是否已设置支付密码：0-未设置，1-已设置' AFTER payment_password;

-- 验证字段是否添加成功
DESCRIBE t_user;
