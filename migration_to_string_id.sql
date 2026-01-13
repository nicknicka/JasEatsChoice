-- ====================================================================
-- 数据迁移脚本：将BIGINT类型ID改为VARCHAR(64)类型
-- 数据库：jia_shi_yi_xuan
-- 日期：2026-01-13
-- 说明：此脚本将现有数据的BIGINT类型ID转换为带前缀的VARCHAR(64)类型
-- ====================================================================

-- 备份数据库（执行前请先手动备份）
-- mysqldump -uroot -p123456 jia_shi_yi_xuan > jia_shi_yi_xuan_backup_$(date +%Y%m%d_%H%M%S).sql

USE jia_shi_yi_xuan;

-- ====================================================================
-- 第一步：为现有数据添加前缀
-- ====================================================================

-- 1. 用户表（t_user）
-- 注意：如果数据已经有前缀则跳过，否则添加U前缀
UPDATE t_user SET user_id = CONCAT('U', CAST(user_id AS CHAR)) WHERE user_id NOT LIKE 'U%';
UPDATE t_user SET merchant_id = CONCAT('M', CAST(merchant_id AS CHAR)) WHERE merchant_id IS NOT NULL AND merchant_id NOT LIKE 'M%';

-- 2. 商家表（t_merchant）
UPDATE t_merchant SET id = CONCAT('M', CAST(id AS CHAR)) WHERE id NOT LIKE 'M%';

-- 3. 群表（t_group）
UPDATE t_group SET id = CONCAT('G', CAST(id AS CHAR)) WHERE id NOT LIKE 'G%';
UPDATE t_group SET creator_id = CONCAT('U', CAST(creator_id AS CHAR)) WHERE creator_id NOT LIKE 'U%';

-- 4. 菜品表（t_dish）
-- 先为新数据生成D前缀的ID（如果有自增ID的话）
UPDATE t_dish SET id = CONCAT('D', CAST(id AS CHAR)) WHERE id NOT LIKE 'D%';
UPDATE t_dish SET merchant_id = CONCAT('M', CAST(merchant_id AS CHAR)) WHERE merchant_id NOT LIKE 'M%';

-- 5. 菜单表（t_menu）
UPDATE t_menu SET id = CONCAT('MN', CAST(id AS CHAR)) WHERE id NOT LIKE 'MN%';
UPDATE t_menu SET merchant_id = CONCAT('M', CAST(merchant_id AS CHAR)) WHERE merchant_id NOT LIKE 'M%';

-- 6. 菜单菜品关联表（t_menu_dish）
UPDATE t_menu_dish SET id = CONCAT('MND', CAST(id AS CHAR)) WHERE id NOT LIKE 'MND%';
UPDATE t_menu_dish SET menu_id = CONCAT('MN', CAST(menu_id AS CHAR)) WHERE menu_id NOT LIKE 'MN%';
UPDATE t_menu_dish SET dish_id = CONCAT('D', CAST(dish_id AS CHAR)) WHERE dish_id NOT LIKE 'D%';

-- 7. 订单表（t_order）
UPDATE t_order SET id = CONCAT('O', CAST(id AS CHAR)) WHERE id NOT LIKE 'O%';
UPDATE t_order SET user_id = CONCAT('U', CAST(user_id AS CHAR)) WHERE user_id NOT LIKE 'U%';
UPDATE t_order SET merchant_id = CONCAT('M', CAST(merchant_id AS CHAR)) WHERE merchant_id NOT LIKE 'M%';
UPDATE t_order SET address_id = CONCAT('A', CAST(address_id AS CHAR)) WHERE address_id IS NOT NULL AND address_id NOT LIKE 'A%';
UPDATE t_order SET payment_id = CONCAT('P', CAST(payment_id AS CHAR)) WHERE payment_id IS NOT NULL AND payment_id NOT LIKE 'P%';

-- 8. 订单菜品表（t_order_dish）
UPDATE t_order_dish SET id = CONCAT('OD', CAST(id AS CHAR)) WHERE id NOT LIKE 'OD%';
UPDATE t_order_dish SET order_id = CONCAT('O', CAST(order_id AS CHAR)) WHERE order_id NOT LIKE 'O%';
UPDATE t_order_dish SET dish_id = CONCAT('D', CAST(dish_id AS CHAR)) WHERE dish_id NOT LIKE 'D%';

-- 9. 群订单表（t_group_order）
UPDATE t_group_order SET id = CONCAT('GO', CAST(id AS CHAR)) WHERE id NOT LIKE 'GO%';
UPDATE t_group_order SET initiator_id = CONCAT('U', CAST(initiator_id AS CHAR)) WHERE initiator_id NOT LIKE 'U%';
UPDATE t_group_order SET merchant_id = CONCAT('M', CAST(merchant_id AS CHAR)) WHERE merchant_id NOT LIKE 'M%';
UPDATE t_group_order SET group_id = CONCAT('G', CAST(group_id AS CHAR)) WHERE group_id NOT LIKE 'G%';
UPDATE t_group_order SET address_id = CONCAT('A', CAST(address_id AS CHAR)) WHERE address_id IS NOT NULL AND address_id NOT LIKE 'A%';

-- 10. 群订单菜品表（t_group_order_dish）
UPDATE t_group_order_dish SET id = CONCAT('GOD', CAST(id AS CHAR)) WHERE id NOT LIKE 'GOD%';
UPDATE t_group_order_dish SET group_order_id = CONCAT('GO', CAST(group_order_id AS CHAR)) WHERE group_order_id NOT LIKE 'GO%';
UPDATE t_group_order_dish SET dish_id = CONCAT('D', CAST(dish_id AS CHAR)) WHERE dish_id NOT LIKE 'D%';
UPDATE t_group_order_dish SET user_id = CONCAT('U', CAST(user_id AS CHAR)) WHERE user_id IS NOT NULL AND user_id NOT LIKE 'U%';

-- 11. 评价表（t_review）
UPDATE t_review SET id = CONCAT('R', CAST(id AS CHAR)) WHERE id NOT LIKE 'R%';
UPDATE t_review SET user_id = CONCAT('U', CAST(user_id AS CHAR)) WHERE user_id NOT LIKE 'U%';
UPDATE t_review SET merchant_id = CONCAT('M', CAST(merchant_id AS CHAR)) WHERE merchant_id NOT LIKE 'M%';
UPDATE t_review SET order_id = CONCAT('O', CAST(order_id AS CHAR)) WHERE order_id NOT LIKE 'O%';
UPDATE t_review SET dish_id = CONCAT('D', CAST(dish_id AS CHAR)) WHERE dish_id IS NOT NULL AND dish_id NOT LIKE 'D%';

-- 12. 支付记录表（t_payment_record）
UPDATE t_payment_record SET id = CONCAT('P', CAST(id AS CHAR)) WHERE id NOT LIKE 'P%';
UPDATE t_payment_record SET user_id = CONCAT('U', CAST(user_id AS CHAR)) WHERE user_id NOT LIKE 'U%';
UPDATE t_payment_record SET merchant_id = CONCAT('M', CAST(merchant_id AS CHAR)) WHERE merchant_id NOT LIKE 'M%';

-- 13. 地址表（t_address）
UPDATE t_address SET id = CONCAT('A', CAST(id AS CHAR)) WHERE id NOT LIKE 'A%';
UPDATE t_address SET user_id = CONCAT('U', CAST(user_id AS CHAR)) WHERE user_id NOT LIKE 'U%';

-- 14. 钱包表（t_wallet）
UPDATE t_wallet SET id = CONCAT('W', CAST(id AS CHAR)) WHERE id NOT LIKE 'W%';
UPDATE t_wallet SET user_id = CONCAT('U', CAST(user_id AS CHAR)) WHERE user_id NOT LIKE 'U%';

-- 15. 消费记录表（t_consume_history）
UPDATE t_consume_history SET id = CONCAT('CH', CAST(id AS CHAR)) WHERE id NOT LIKE 'CH%';
UPDATE t_consume_history SET user_id = CONCAT('U', CAST(user_id AS CHAR)) WHERE user_id NOT LIKE 'U%';

-- 16. 充值记录表（t_recharge_record）
UPDATE t_recharge_record SET id = CONCAT('RR', CAST(id AS CHAR)) WHERE id NOT LIKE 'RR%';
UPDATE t_recharge_record SET user_id = CONCAT('U', CAST(user_id AS CHAR)) WHERE user_id NOT LIKE 'U%';

-- 17. 提现记录表（t_withdraw_record）
UPDATE t_withdraw_record SET id = CONCAT('WR', CAST(id AS CHAR)) WHERE id NOT LIKE 'WR%';
UPDATE t_withdraw_record SET user_id = CONCAT('U', CAST(user_id AS CHAR)) WHERE user_id NOT LIKE 'U%';

-- 18. 通知表（t_notification）
UPDATE t_notification SET id = CONCAT('N', CAST(id AS CHAR)) WHERE id NOT LIKE 'N%';
UPDATE t_notification SET user_id = CONCAT('U', CAST(user_id AS CHAR)) WHERE user_id NOT LIKE 'U%';

-- 19. 收藏表（t_collection）
UPDATE t_collection SET id = CONCAT('C', CAST(id AS CHAR)) WHERE id NOT LIKE 'C%';
UPDATE t_collection SET user_id = CONCAT('U', CAST(user_id AS CHAR)) WHERE user_id NOT LIKE 'U%';
UPDATE t_collection SET collectable_id = CONCAT('D', CAST(collectable_id AS CHAR)) WHERE collectable_type = 'dish' AND collectable_id NOT LIKE 'D%';
UPDATE t_collection SET collectable_id = CONCAT('M', CAST(collectable_id AS CHAR)) WHERE collectable_type = 'merchant' AND collectable_id NOT LIKE 'M%';

-- 20. 联系人表（t_contact）
UPDATE t_contact SET id = CONCAT('CT', CAST(id AS CHAR)) WHERE id NOT LIKE 'CT%';
UPDATE t_contact SET user_id = CONCAT('U', CAST(user_id AS CHAR)) WHERE user_id NOT LIKE 'U%';
-- 根据relation_type更新target_id
UPDATE t_contact SET target_id = CONCAT('U', CAST(target_id AS CHAR)) WHERE relation_type = 'friend' AND target_id NOT LIKE 'U%';
UPDATE t_contact SET target_id = CONCAT('G', CAST(target_id AS CHAR)) WHERE relation_type = 'group' AND target_id NOT LIKE 'G%';

-- 21. 聊天消息表（t_chat_msg）
UPDATE t_chat_msg SET id = CONCAT('CM', CAST(id AS CHAR)) WHERE id NOT LIKE 'CM%';
UPDATE t_chat_msg SET from_id = CONCAT('U', CAST(from_id AS CHAR)) WHERE from_id NOT LIKE 'U%';
UPDATE t_chat_msg SET to_id = CONCAT('U', CAST(to_id AS CHAR)) WHERE to_id NOT LIKE 'U%' AND msg_type = 'single';
UPDATE t_chat_msg SET to_id = CONCAT('G', CAST(to_id AS CHAR)) WHERE to_id NOT LIKE 'G%' AND msg_type = 'group';

-- 22. 消息记录表（t_message_record）
UPDATE t_message_record SET id = CONCAT('MR', CAST(id AS CHAR)) WHERE id NOT LIKE 'MR%';
UPDATE t_message_record SET sender_id = CONCAT('U', CAST(sender_id AS CHAR)) WHERE sender_id IS NOT NULL AND sender_id NOT LIKE 'U%';
UPDATE t_message_record SET receiver_id = CONCAT('U', CAST(receiver_id AS CHAR)) WHERE receiver_id IS NOT NULL AND receiver_id NOT LIKE 'U%';

-- 23. 食谱表（t_recipe）
UPDATE t_recipe SET id = CONCAT('RC', CAST(id AS CHAR)) WHERE id NOT LIKE 'RC%';
UPDATE t_recipe SET user_id = CONCAT('U', CAST(user_id AS CHAR)) WHERE user_id IS NOT NULL AND user_id NOT LIKE 'U%';

-- 24. 用户偏好表（t_user_preference）
UPDATE t_user_preference SET id = CONCAT('UP', CAST(id AS CHAR)) WHERE id NOT LIKE 'UP%';
UPDATE t_user_preference SET user_id = CONCAT('U', CAST(user_id AS CHAR)) WHERE user_id NOT LIKE 'U%';

-- 25. 教程表（tutorial）
UPDATE tutorial SET id = CONCAT('T', CAST(id AS CHAR)) WHERE id NOT LIKE 'T%';

-- 26. 公告表（announcement）
UPDATE announcement SET id = CONCAT('AN', CAST(id AS CHAR)) WHERE id NOT LIKE 'AN%';
UPDATE announcement SET merchant_id = CONCAT('M', CAST(merchant_id AS CHAR)) WHERE merchant_id NOT LIKE 'M%';

-- 27. 想吃菜品表（t_want_to_eat）
UPDATE t_want_to_eat SET id = CONCAT('WTE', CAST(id AS CHAR)) WHERE id NOT LIKE 'WTE%';
UPDATE t_want_to_eat SET user_id = CONCAT('U', CAST(user_id AS CHAR)) WHERE user_id NOT LIKE 'U%';
UPDATE t_want_to_eat SET item_id = CONCAT('D', CAST(item_id AS CHAR)) WHERE item_id NOT LIKE 'D%';

-- 28. 卡路里记录表（t_calorie_record）
-- 注意：id字段可能已经是VARCHAR，只更新外键
UPDATE t_calorie_record SET user_id = CONCAT('U', CAST(user_id AS CHAR)) WHERE user_id NOT LIKE 'U%';
UPDATE t_calorie_record SET dish_id = CONCAT('D', CAST(dish_id AS CHAR)) WHERE dish_id IS NOT NULL AND dish_id NOT LIKE 'D%';

-- ====================================================================
-- 第二步：修改表结构（将BIGINT改为VARCHAR(64)）
-- ====================================================================

-- 用户相关表
ALTER TABLE t_address MODIFY COLUMN id VARCHAR(64) NOT NULL;
ALTER TABLE t_address MODIFY COLUMN user_id VARCHAR(64) NOT NULL;
ALTER TABLE t_user_preference MODIFY COLUMN id VARCHAR(64) NOT NULL;
ALTER TABLE t_user_preference MODIFY COLUMN user_id VARCHAR(64) NOT NULL;
ALTER TABLE t_collection MODIFY COLUMN id VARCHAR(64) NOT NULL;
ALTER TABLE t_collection MODIFY COLUMN user_id VARCHAR(64) NOT NULL;
ALTER TABLE t_collection MODIFY COLUMN collectable_id VARCHAR(64) NOT NULL;
ALTER TABLE t_contact MODIFY COLUMN id VARCHAR(64) NOT NULL;
ALTER TABLE t_contact MODIFY COLUMN user_id VARCHAR(64) NOT NULL;
ALTER TABLE t_contact MODIFY COLUMN target_id VARCHAR(64) NOT NULL;

-- 商家相关表
ALTER TABLE t_dish MODIFY COLUMN id VARCHAR(64) NOT NULL;
ALTER TABLE t_dish MODIFY COLUMN merchant_id VARCHAR(64) NOT NULL;
ALTER TABLE t_menu MODIFY COLUMN id VARCHAR(64) NOT NULL;
ALTER TABLE t_menu MODIFY COLUMN merchant_id VARCHAR(64) NOT NULL;
ALTER TABLE t_menu_dish MODIFY COLUMN id VARCHAR(64) NOT NULL;
ALTER TABLE t_menu_dish MODIFY COLUMN menu_id VARCHAR(64) NOT NULL;
ALTER TABLE t_menu_dish MODIFY COLUMN dish_id VARCHAR(64) NOT NULL;
ALTER TABLE announcement MODIFY COLUMN id VARCHAR(64) NOT NULL;
ALTER TABLE announcement MODIFY COLUMN merchant_id VARCHAR(64) NOT NULL;

-- 订单相关表
ALTER TABLE t_order MODIFY COLUMN id VARCHAR(64) NOT NULL;
ALTER TABLE t_order MODIFY COLUMN user_id VARCHAR(64) NOT NULL;
ALTER TABLE t_order MODIFY COLUMN merchant_id VARCHAR(64) NOT NULL;
ALTER TABLE t_order MODIFY COLUMN address_id VARCHAR(64);
ALTER TABLE t_order MODIFY COLUMN payment_id VARCHAR(64);
ALTER TABLE t_order_dish MODIFY COLUMN id VARCHAR(64) NOT NULL;
ALTER TABLE t_order_dish MODIFY COLUMN order_id VARCHAR(64) NOT NULL;
ALTER TABLE t_order_dish MODIFY COLUMN dish_id VARCHAR(64) NOT NULL;

-- 群订单相关表
ALTER TABLE t_group MODIFY COLUMN id VARCHAR(64) NOT NULL;
ALTER TABLE t_group MODIFY COLUMN creator_id VARCHAR(64) NOT NULL;
ALTER TABLE t_group_order MODIFY COLUMN id VARCHAR(64) NOT NULL;
ALTER TABLE t_group_order MODIFY COLUMN initiator_id VARCHAR(64) NOT NULL;
ALTER TABLE t_group_order MODIFY COLUMN merchant_id VARCHAR(64) NOT NULL;
ALTER TABLE t_group_order MODIFY COLUMN group_id VARCHAR(64) NOT NULL;
ALTER TABLE t_group_order MODIFY COLUMN address_id VARCHAR(64) NOT NULL;
ALTER TABLE t_group_order_dish MODIFY COLUMN id VARCHAR(64) NOT NULL;
ALTER TABLE t_group_order_dish MODIFY COLUMN group_order_id VARCHAR(64) NOT NULL;
ALTER TABLE t_group_order_dish MODIFY COLUMN dish_id VARCHAR(64) NOT NULL;
ALTER TABLE t_group_order_dish MODIFY COLUMN user_id VARCHAR(64);

-- 支付相关表
ALTER TABLE t_payment_record MODIFY COLUMN id VARCHAR(64) NOT NULL;
ALTER TABLE t_payment_record MODIFY COLUMN user_id VARCHAR(64) NOT NULL;
ALTER TABLE t_payment_record MODIFY COLUMN merchant_id VARCHAR(64) NOT NULL;

-- 消息相关表
ALTER TABLE t_chat_msg MODIFY COLUMN id VARCHAR(64) NOT NULL;
ALTER TABLE t_chat_msg MODIFY COLUMN from_id VARCHAR(50) NOT NULL;
ALTER TABLE t_chat_msg MODIFY COLUMN to_id VARCHAR(50) NOT NULL;
ALTER TABLE t_message_record MODIFY COLUMN id VARCHAR(64) NOT NULL;
ALTER TABLE t_message_record MODIFY COLUMN sender_id VARCHAR(64);
ALTER TABLE t_message_record MODIFY COLUMN receiver_id VARCHAR(64);
ALTER TABLE t_notification MODIFY COLUMN id VARCHAR(64) NOT NULL;
ALTER TABLE t_notification MODIFY COLUMN user_id VARCHAR(64) NOT NULL;

-- 其他表
ALTER TABLE tutorial MODIFY COLUMN id VARCHAR(64) NOT NULL;
ALTER TABLE t_recipe MODIFY COLUMN id VARCHAR(64) NOT NULL;
ALTER TABLE t_recipe MODIFY COLUMN user_id VARCHAR(64);
ALTER TABLE t_review MODIFY COLUMN id VARCHAR(64) NOT NULL;
ALTER TABLE t_review MODIFY COLUMN user_id VARCHAR(64) NOT NULL;
ALTER TABLE t_review MODIFY COLUMN merchant_id VARCHAR(64) NOT NULL;
ALTER TABLE t_review MODIFY COLUMN order_id VARCHAR(64) NOT NULL;
ALTER TABLE t_review MODIFY COLUMN dish_id VARCHAR(64);
ALTER TABLE t_consume_history MODIFY COLUMN id VARCHAR(64) NOT NULL;
ALTER TABLE t_consume_history MODIFY COLUMN user_id VARCHAR(64) NOT NULL;
ALTER TABLE t_recharge_record MODIFY COLUMN id VARCHAR(64) NOT NULL;
ALTER TABLE t_recharge_record MODIFY COLUMN user_id VARCHAR(64) NOT NULL;
ALTER TABLE t_withdraw_record MODIFY COLUMN id VARCHAR(64) NOT NULL;
ALTER TABLE t_withdraw_record MODIFY COLUMN user_id VARCHAR(64) NOT NULL;
ALTER TABLE t_want_to_eat MODIFY COLUMN id VARCHAR(64) NOT NULL;
ALTER TABLE t_want_to_eat MODIFY COLUMN user_id VARCHAR(64) NOT NULL;
ALTER TABLE t_want_to_eat MODIFY COLUMN item_id VARCHAR(64) NOT NULL;
ALTER TABLE t_calorie_record MODIFY COLUMN user_id VARCHAR(64) NOT NULL;
ALTER TABLE t_calorie_record MODIFY COLUMN dish_id VARCHAR(64);

-- ====================================================================
-- 第三步：验证数据完整性
-- ====================================================================

-- 检查是否有孤立的外键引用
-- 示例：检查订单表中的用户ID是否都存在于用户表中
SELECT COUNT(*) AS orphaned_orders FROM t_order o
LEFT JOIN t_user u ON o.user_id = u.user_id
WHERE u.user_id IS NULL;

-- 检查菜品表中的商家ID是否都存在于商家表中
SELECT COUNT(*) AS orphaned_dishes FROM t_dish d
LEFT JOIN t_merchant m ON d.merchant_id = m.id
WHERE m.id IS NULL;

-- ====================================================================
-- 注意事项：
-- 1. 执行前务必备份数据库
-- 2. 建议在测试环境先执行验证
-- 3. 执行过程中如有错误，请立即停止并检查
-- 4. 某些表可能有特殊字符或空值，需要根据实际情况调整SQL
-- 5. AUTO_INCREMENT属性会被自动移除（VARCHAR类型不支持）
-- ====================================================================
