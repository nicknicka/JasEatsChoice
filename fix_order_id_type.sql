-- 修复 t_order 表的字段类型
-- 将 BIGINT 类型改为 VARCHAR 以支持字符串类型的 ID（如 'JD1768311513696'）

USE jia_shi_yi_xuan;

-- 由于 id 是自增主键，需要先修改 id 字段
-- 1. 删除自增属性
ALTER TABLE t_order MODIFY COLUMN id BIGINT NOT NULL COMMENT '订单ID';

-- 2. 修改 id 字段类型为 VARCHAR
ALTER TABLE t_order MODIFY COLUMN id VARCHAR(50) NOT NULL COMMENT '订单ID';

-- 3. 修改其他外键字段类型
ALTER TABLE t_order MODIFY COLUMN user_id VARCHAR(50) NOT NULL COMMENT '用户ID',
                    MODIFY COLUMN merchant_id VARCHAR(50) NOT NULL COMMENT '商家ID',
                    MODIFY COLUMN address_id VARCHAR(50) DEFAULT NULL COMMENT '配送地址ID';

-- 4. 添加 payment_id 字段（如果不存在）
ALTER TABLE t_order ADD COLUMN IF NOT EXISTS payment_id VARCHAR(50) DEFAULT NULL COMMENT '支付记录ID' AFTER `status`;

-- 5. 添加 paid_amount 字段（如果不存在）
ALTER TABLE t_order ADD COLUMN IF NOT EXISTS paid_amount DECIMAL(10,2) DEFAULT NULL COMMENT '已支付金额' AFTER payment_id;

-- 6. 添加 payment_time 字段（如果不存在）
ALTER TABLE t_order ADD COLUMN IF NOT EXISTS payment_time DATETIME DEFAULT NULL COMMENT '支付时间' AFTER paid_amount;

-- 验证修改
DESCRIBE t_order;
