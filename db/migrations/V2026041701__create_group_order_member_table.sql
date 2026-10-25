-- 创建拼单成员表，并为群订单补充真实业务字段
-- Date: 2026-04-17

CREATE TABLE IF NOT EXISTS `t_group_order_member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `group_order_id` VARCHAR(64) NOT NULL COMMENT '拼单ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `role` VARCHAR(32) NOT NULL DEFAULT 'member' COMMENT '成员角色：initiator-发起人，member-普通成员',
    `join_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `pay_status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '支付状态：pending-待支付，paid-已支付，refund-已退款',
    `paid_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '累计实付金额',
    `leave_status` TINYINT NOT NULL DEFAULT 0 COMMENT '离开状态：0-在拼单中，1-已退出',
    `leave_time` DATETIME DEFAULT NULL COMMENT '退出时间',
    `invite_by` VARCHAR(64) DEFAULT NULL COMMENT '邀请人用户ID',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_order_member_order_user` (`group_order_id`, `user_id`),
    KEY `idx_group_order_member_order_id` (`group_order_id`),
    KEY `idx_group_order_member_user_id` (`user_id`),
    KEY `idx_group_order_member_leave_status` (`leave_status`),
    KEY `idx_group_order_member_pay_status` (`pay_status`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='拼单成员表';

ALTER TABLE `t_group_order_member`
    MODIFY COLUMN `group_order_id` VARCHAR(64) NOT NULL COMMENT '拼单ID',
    MODIFY COLUMN `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    MODIFY COLUMN `invite_by` VARCHAR(64) DEFAULT NULL COMMENT '邀请人用户ID';

ALTER TABLE `t_group_order_member`
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

SET @ddl = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE t_group_order ADD COLUMN max_participants INT NOT NULL DEFAULT 1 COMMENT ''最大参与人数'' AFTER total_amount',
        'SELECT 1'
    )
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 't_group_order'
      AND COLUMN_NAME = 'max_participants'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE t_group_order ADD COLUMN locked TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''是否已锁单：0-未锁单，1-已锁单'' AFTER max_participants',
        'SELECT 1'
    )
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 't_group_order'
      AND COLUMN_NAME = 'locked'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE t_group_order ADD COLUMN confirmed_time DATETIME DEFAULT NULL COMMENT ''确认成团时间'' AFTER locked',
        'SELECT 1'
    )
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 't_group_order'
      AND COLUMN_NAME = 'confirmed_time'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO `t_group_order_member` (
    `group_order_id`,
    `user_id`,
    `role`,
    `join_time`,
    `pay_status`,
    `paid_amount`,
    `leave_status`,
    `leave_time`,
    `invite_by`,
    `remark`,
    `create_time`,
    `update_time`
)
SELECT
    go.`id`,
    go.`initiator_id`,
    'initiator',
    COALESCE(go.`create_time`, NOW()),
    'pending',
    0.00,
    0,
    NULL,
    NULL,
    NULL,
    COALESCE(go.`create_time`, NOW()),
    COALESCE(go.`update_time`, go.`create_time`, NOW())
FROM `t_group_order` go
WHERE go.`initiator_id` IS NOT NULL
  AND go.`initiator_id` <> ''
  AND NOT EXISTS (
      SELECT 1
      FROM `t_group_order_member` gom
      WHERE gom.`group_order_id` = go.`id`
        AND gom.`user_id` = go.`initiator_id`
  );

INSERT INTO `t_group_order_member` (
    `group_order_id`,
    `user_id`,
    `role`,
    `join_time`,
    `pay_status`,
    `paid_amount`,
    `leave_status`,
    `leave_time`,
    `invite_by`,
    `remark`,
    `create_time`,
    `update_time`
)
SELECT
    dish_user.`group_order_id`,
    dish_user.`user_id`,
    CASE
        WHEN dish_user.`user_id` = go.`initiator_id` THEN 'initiator'
        ELSE 'member'
    END,
    COALESCE(go.`create_time`, NOW()),
    'pending',
    0.00,
    0,
    NULL,
    NULL,
    NULL,
    COALESCE(go.`create_time`, NOW()),
    COALESCE(go.`update_time`, go.`create_time`, NOW())
FROM (
    SELECT `group_order_id`, `user_id`
    FROM `t_group_order_dish`
    WHERE `group_order_id` IS NOT NULL
      AND `group_order_id` <> ''
      AND `user_id` IS NOT NULL
      AND `user_id` <> ''
    GROUP BY `group_order_id`, `user_id`
) dish_user
INNER JOIN `t_group_order` go
    ON go.`id` = dish_user.`group_order_id`
WHERE NOT EXISTS (
    SELECT 1
    FROM `t_group_order_member` gom
    WHERE gom.`group_order_id` = dish_user.`group_order_id`
      AND gom.`user_id` = dish_user.`user_id`
);

UPDATE `t_group_order_member` gom
INNER JOIN (
    SELECT
        `order_id` AS `group_order_id`,
        `user_id`,
        SUM(`amount`) AS `paid_amount`
    FROM `t_payment_record`
    WHERE `payment_status` = 'success'
      AND (`remark` IS NULL OR `remark` <> 'all')
    GROUP BY `order_id`, `user_id`
) paid
    ON paid.`group_order_id` = gom.`group_order_id`
    AND paid.`user_id` = gom.`user_id`
SET
    gom.`pay_status` = 'paid',
    gom.`paid_amount` = paid.`paid_amount`;

UPDATE `t_group_order_member` gom
INNER JOIN (
    SELECT DISTINCT `order_id` AS `group_order_id`
    FROM `t_payment_record`
    WHERE `payment_status` = 'success'
      AND `remark` = 'all'
) paid_all
    ON paid_all.`group_order_id` = gom.`group_order_id`
SET gom.`pay_status` = 'paid';

UPDATE `t_group_order` go
LEFT JOIN (
    SELECT `group_order_id`, COUNT(*) AS `participant_count`
    FROM `t_group_order_member`
    WHERE `leave_status` = 0
    GROUP BY `group_order_id`
) stat
    ON stat.`group_order_id` = go.`id`
SET go.`max_participants` = GREATEST(IFNULL(go.`max_participants`, 1), IFNULL(stat.`participant_count`, 0), 1);

UPDATE `t_group_order`
SET `locked` = CASE
        WHEN `remark` IS NOT NULL AND `remark` LIKE '[GROUP_ORDER_CONFIRMED]%' THEN 1
        WHEN `status` IN (1, 2, 3, 4, 5) THEN 1
        ELSE IFNULL(`locked`, 0)
    END,
    `confirmed_time` = CASE
        WHEN `confirmed_time` IS NOT NULL THEN `confirmed_time`
        WHEN (`remark` IS NOT NULL AND `remark` LIKE '[GROUP_ORDER_CONFIRMED]%')
          OR `status` IN (1, 2, 3, 4, 5)
            THEN COALESCE(`update_time`, `create_time`, NOW())
        ELSE NULL
    END;
