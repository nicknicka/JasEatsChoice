-- V6: 时间字段统一为 created_at / updated_at
--
-- 背景：ADR-0005 决定把四种并存的时间列名统一。
-- 用 RENAME COLUMN 而非 DROP+ADD，保留数据、默认值、ON UPDATE 与注释。
-- 按领域分组，一个域一段，便于 review 与失败定位。
--
-- 视图依赖说明：v_active_festivals 引用了 cnt_festival 的时间列，
-- 本脚本末尾重建它；v_user_behavior_stats 引用 ai_user_behavior.created_time，
-- 同样在末尾重建。

-- ===== AI 域 (ai_) =====
ALTER TABLE `ai_chat_history` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `ai_chat_msg` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `ai_chat_session` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `ai_chat_session` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `ai_message_record` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `ai_message_record` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `ai_recommendation_log` RENAME COLUMN `created_time` TO `created_at`;
ALTER TABLE `ai_user_behavior` RENAME COLUMN `created_time` TO `created_at`;

-- ===== 内容域 (cnt_) =====
ALTER TABLE `cnt_add_dish_request` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `cnt_add_dish_request` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `cnt_add_dish_setting` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `cnt_add_dish_setting` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `cnt_banner` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `cnt_banner` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `cnt_dish_step_config` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `cnt_dish_step_config` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `cnt_dish_step_history` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `cnt_festival` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `cnt_festival` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `cnt_festival_dish_recommend` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `cnt_festival_dish_recommend` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `cnt_hot_topic` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `cnt_hot_topic` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `cnt_recipe` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `cnt_recipe` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `cnt_tutorial` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `cnt_tutorial` RENAME COLUMN `update_time` TO `updated_at`;

-- ===== 外部集成域 (ext_) =====
ALTER TABLE `ext_content_extraction` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `ext_content_extraction` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `ext_content_source` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `ext_content_source` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `ext_extraction_config` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `ext_extraction_config` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `ext_extraction_task` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `ext_extraction_task` RENAME COLUMN `update_time` TO `updated_at`;

-- ===== 商家域 (mch_) =====
ALTER TABLE `mch_dish` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `mch_dish` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `mch_dish_features` RENAME COLUMN `created_time` TO `created_at`;
ALTER TABLE `mch_dish_features` RENAME COLUMN `updated_time` TO `updated_at`;
ALTER TABLE `mch_dish_similarity` RENAME COLUMN `updated_time` TO `updated_at`;
ALTER TABLE `mch_menu` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `mch_menu` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `mch_merchant` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `mch_merchant` RENAME COLUMN `update_time` TO `updated_at`;

-- ===== 营养域 (nut_) =====
ALTER TABLE `nut_ingredient_conflict_rule` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `nut_ingredient_conflict_rule` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `nut_nutrition` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `nut_nutrition` RENAME COLUMN `update_time` TO `updated_at`;

-- ===== 订单域 (ord_) =====
ALTER TABLE `ord_order` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `ord_order` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `ord_payment_record` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `ord_payment_record` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `ord_refund_record` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `ord_refund_record` RENAME COLUMN `update_time` TO `updated_at`;

-- ===== 社交域 (soc_) =====
ALTER TABLE `soc_collection` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `soc_group` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `soc_group` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `soc_group_order` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `soc_group_order` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `soc_group_order_addition` RENAME COLUMN `created_time` TO `created_at`;
ALTER TABLE `soc_group_order_addition` RENAME COLUMN `updated_time` TO `updated_at`;
ALTER TABLE `soc_group_order_member` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `soc_group_order_member` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `soc_review` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `soc_review` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `soc_review_reply` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `soc_review_reply` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `soc_wish_list_item` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `soc_wish_list_item` RENAME COLUMN `update_time` TO `updated_at`;

-- ===== 系统管理域 (sys_) =====
ALTER TABLE `sys_admin` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `sys_admin` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `sys_admin_operation_log` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `sys_admin_role_relation` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `sys_announcement` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `sys_announcement` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `sys_permission` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `sys_permission` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `sys_role` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `sys_role` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `sys_role_permission` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `sys_role_permission_relation` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `sys_scheduled_task` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `sys_scheduled_task` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `sys_scheduled_task_log` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `sys_system_config` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `sys_system_config` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `sys_system_log` RENAME COLUMN `create_time` TO `created_at`;

-- ===== 用户域 (usr_) =====
ALTER TABLE `usr_address` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `usr_address` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `usr_contact` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `usr_contact` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `usr_mock_oauth_user` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `usr_notification` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `usr_user` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `usr_user` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `usr_user_coupon` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `usr_user_coupon` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `usr_user_custom_event` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `usr_user_custom_event` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `usr_user_oauth` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `usr_user_oauth` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `usr_user_preference` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `usr_user_recommend_history` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `usr_user_recommend_history` RENAME COLUMN `update_time` TO `updated_at`;

-- ===== 钱包域 (wlt_) =====
ALTER TABLE `wlt_consume_history` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `wlt_discount` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `wlt_discount` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `wlt_recharge_record` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `wlt_recharge_record` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `wlt_wallet` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `wlt_wallet` RENAME COLUMN `update_time` TO `updated_at`;
ALTER TABLE `wlt_withdraw_record` RENAME COLUMN `create_time` TO `created_at`;
ALTER TABLE `wlt_withdraw_record` RENAME COLUMN `update_time` TO `updated_at`;

-- ===== 重建依赖时间列的视图 =====

DROP VIEW IF EXISTS `v_active_festivals`;

CREATE VIEW `v_active_festivals` AS
SELECT
    f.id,
    f.festival_name,
    f.festival_type,
    f.festival_date,
    f.year,
    f.description,
    f.icon,
    f.background_image,
    f.theme_color,
    f.start_date,
    f.end_date,
    f.is_active,
    f.sort_order,
    f.created_at,
    f.updated_at,
    -- is_current: 节日是否当前生效
    -- 简化逻辑：is_active = 1 且（今天在 start_date~end_date 之间，或今天就是节日当天）
    CASE
        WHEN f.is_active = 0 THEN 0
        -- 有明确日期范围的季节性节日
        WHEN f.start_date IS NOT NULL AND f.end_date IS NOT NULL
            AND CURDATE() BETWEEN f.start_date AND f.end_date THEN 1
        -- MM-DD 格式的固定日期节日，今年的
        WHEN f.festival_date IS NOT NULL
            AND DATE(CONCAT(YEAR(CURDATE()), '-', f.festival_date)) = CURDATE() THEN 1
        -- MM-DD 格式的固定日期节日，明年的（跨年场景）
        WHEN f.festival_date IS NOT NULL
            AND f.year IS NULL
            AND DATE(CONCAT(YEAR(CURDATE()) + 1, '-', f.festival_date)) = CURDATE() THEN 1
        ELSE 0
    END AS is_current,
    -- days_until_festival: 距离下一个节日的天数
    -- 简化逻辑：基于 festival_date 计算今年或明年的日期
    CASE
        WHEN f.is_active = 0 THEN NULL
        WHEN f.festival_date IS NULL THEN NULL
        ELSE
            DATEDIFF(
                CASE
                    -- 今年的节日还没到
                    WHEN DATE(CONCAT(YEAR(CURDATE()), '-', f.festival_date)) >= CURDATE()
                    THEN DATE(CONCAT(YEAR(CURDATE()), '-', f.festival_date))
                    -- 今年的已经过了，且是年度循环节日，算明年的
                    WHEN f.year IS NULL
                    THEN DATE(CONCAT(YEAR(CURDATE()) + 1, '-', f.festival_date))
                    -- 今年的过了且不是年度循环，返回 NULL
                    ELSE NULL
                END,
                CURDATE()
            )
    END AS days_until_festival
FROM `cnt_festival` f
WHERE f.is_active = 1;

DROP VIEW IF EXISTS `v_user_behavior_stats`;
CREATE VIEW `v_user_behavior_stats` AS
SELECT
  `user_id` AS `user_id`,
  `behavior_type` AS `behavior_type`,
  COUNT(0) AS `behavior_count`,
  COUNT(DISTINCT `item_id`) AS `unique_items`,
  MIN(`created_at`) AS `first_behavior`,
  MAX(`created_at`) AS `last_behavior`
FROM `ai_user_behavior`
GROUP BY `user_id`, `behavior_type`;
