-- 修复 t_order 表的 ID 字段类型
-- 将 INT 类型改为 VARCHAR 以支持字符串类型的 ID（如 'JD1768309113785'）

USE jaseatschoice;

-- 修改订单表 t_order 的 ID 字段类型
ALTER TABLE t_order
MODIFY COLUMN id VARCHAR(50) NOT NULL COMMENT '订单ID',
MODIFY COLUMN user_id VARCHAR(50) NOT NULL COMMENT '用户ID',
MODIFY COLUMN merchant_id VARCHAR(50) NOT NULL COMMENT '商家ID',
MODIFY COLUMN payment_id VARCHAR(50) DEFAULT NULL COMMENT '支付记录ID',
MODIFY COLUMN address_id VARCHAR(50) DEFAULT NULL COMMENT '配送地址ID';

-- 检查是否需要调整主键约束（如果有的话）
-- 如果 id 是主键，MySQL 会自动调整，但可能需要重新设置自增（如果有）
-- ALTER TABLE t_order MODIFY id VARCHAR(50) NOT NULL PRIMARY KEY;

-- 验证修改
DESCRIBE t_order;
