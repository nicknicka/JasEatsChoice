-- 简化订单状态：从9个状态简化为5个状态
-- 旧状态：0-待支付、1-待接单、2-备菜中、3-烹饪中、4-待上菜、5-已送达、6-已取消、7-待评价、8-已评价
-- 新状态：0-待支付、1-待接单、2-制作中、3-已完成、4-已取消

-- 更新订单表的status字段注释
ALTER TABLE `t_order` MODIFY COLUMN `status` tinyint NOT NULL DEFAULT 0 COMMENT '订单状态（0-待支付/1-待接单/2-制作中/3-已完成/4-已取消）';

-- 数据迁移：将旧状态映射到新状态
-- 备菜中(2)、烹饪中(3)、待上菜(4) → 制作中(2)
UPDATE `t_order` SET `status` = 2 WHERE `status` IN (2, 3, 4);

-- 已送达(5)、待评价(7)、已评价(8) → 已完成(3)
UPDATE `t_order` SET `status` = 3 WHERE `status` IN (5, 7, 8);

-- 已取消(6) → 已取消(4)
UPDATE `t_order` SET `status` = 4 WHERE `status` = 6;

-- 添加注释说明状态变更
-- 此迁移将订单状态从9个简化为5个，合并了中间状态和完成状态
-- 制作中包含：备菜、烹饪、待上菜
-- 已完成包含：已送达、待评价、已评价
