-- 更新订单表状态字段注释为5状态系统
-- 5状态: 0-待支付、1-待接单、2-制作中、3-已完成、4-已取消

ALTER TABLE `t_order`
MODIFY COLUMN `status` tinyint NOT NULL DEFAULT '0'
COMMENT '订单状态：0-待支付、1-待接单、2-制作中、3-已完成、4-已取消';
