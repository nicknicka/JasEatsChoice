-- 统一拼单主链与加菜链业务ID为字符类型，避免大数ID溢出和前后端精度丢失
-- Date: 2026-04-17

SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE `t_user`
    MODIFY COLUMN `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    MODIFY COLUMN `merchant_id` VARCHAR(64) DEFAULT NULL COMMENT '商家ID';

ALTER TABLE `t_merchant`
    MODIFY COLUMN `id` VARCHAR(64) NOT NULL COMMENT '商家ID';

ALTER TABLE `t_dish`
    MODIFY COLUMN `id` VARCHAR(64) NOT NULL COMMENT '菜品ID',
    MODIFY COLUMN `merchant_id` VARCHAR(64) NOT NULL COMMENT '商家ID';

ALTER TABLE `t_group`
    MODIFY COLUMN `creator_id` VARCHAR(64) NOT NULL COMMENT '创建者ID';

ALTER TABLE `t_group_order`
    MODIFY COLUMN `id` VARCHAR(64) NOT NULL COMMENT '群订单ID';

ALTER TABLE `t_group_order_dish`
    MODIFY COLUMN `id` VARCHAR(64) NOT NULL COMMENT 'ID',
    MODIFY COLUMN `group_order_id` VARCHAR(64) NOT NULL COMMENT '群订单ID',
    MODIFY COLUMN `dish_id` VARCHAR(64) NOT NULL COMMENT '菜品ID',
    MODIFY COLUMN `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID';

ALTER TABLE `t_group_order_member`
    MODIFY COLUMN `group_order_id` VARCHAR(64) NOT NULL COMMENT '拼单ID',
    MODIFY COLUMN `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    MODIFY COLUMN `invite_by` VARCHAR(64) DEFAULT NULL COMMENT '邀请人用户ID';

ALTER TABLE `t_payment_record`
    MODIFY COLUMN `user_id` VARCHAR(64) NOT NULL COMMENT '支付用户ID',
    MODIFY COLUMN `merchant_id` VARCHAR(64) NOT NULL COMMENT '商家ID';

ALTER TABLE `t_chat_session`
    MODIFY COLUMN `group_id` VARCHAR(64) DEFAULT NULL COMMENT '群组ID（仅群聊会话有效）';

ALTER TABLE `t_add_dish_request`
    MODIFY COLUMN `group_order_id` VARCHAR(64) NOT NULL COMMENT '群订单ID',
    MODIFY COLUMN `request_user_id` VARCHAR(64) NOT NULL COMMENT '加菜请求人ID',
    MODIFY COLUMN `merchant_id` VARCHAR(64) NOT NULL COMMENT '商家ID',
    MODIFY COLUMN `reviewer_id` VARCHAR(64) DEFAULT NULL COMMENT '审核人ID(群订单发起者)';

ALTER TABLE `t_add_dish_setting`
    MODIFY COLUMN `group_order_id` VARCHAR(64) NOT NULL COMMENT '群订单ID';

ALTER TABLE `t_group_member`
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE `t_chat_session`
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE `t_chat_msg`
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE `t_group_order_dish`
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE `t_group_order_member`
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE `t_payment_record`
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE `t_add_dish_request`
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE `t_add_dish_setting`
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;
