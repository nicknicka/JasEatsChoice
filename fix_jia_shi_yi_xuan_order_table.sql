-- 修复 jia_shi_yi_xuan 数据库的 t_order 表
-- 将 BIGINT 类型改为 VARCHAR 以支持字符串类型的 ID（如 'JD1768311807059'）

USE jia_shi_yi_xuan;

-- 1. 修改 id 字段：先删除自增属性，然后修改类型
ALTER TABLE t_order MODIFY id BIGINT NOT NULL COMMENT '订单ID';

-- 2. 修改 id 字段类型为 VARCHAR
ALTER TABLE t_order MODIFY id VARCHAR(50) NOT NULL COMMENT '订单ID';

-- 3. 修改其他外键字段类型
ALTER TABLE t_order
    MODIFY COLUMN user_id VARCHAR(50) NOT NULL COMMENT '用户ID',
    MODIFY COLUMN merchant_id VARCHAR(50) NOT NULL COMMENT '商家ID',
    MODIFY COLUMN payment_id VARCHAR(50) DEFAULT NULL COMMENT '支付记录ID',
    MODIFY COLUMN address_id VARCHAR(50) DEFAULT NULL COMMENT '配送地址ID';

-- 验证修改
DESCRIBE t_order;
