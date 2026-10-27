-- V3: 74 张表按领域加上前缀，统一命名规范
-- 参见 ADR-0002：数据库重构第一阶段
-- 执行方式：RENAME TABLE，原子操作，不影响数据

-- ===== ai_ 领域 (7 张表) =====
RENAME TABLE `recommendation_log` TO `ai_recommendation_log`;
RENAME TABLE `t_ai_chat_history` TO `ai_chat_history`;
RENAME TABLE `t_chat_msg` TO `ai_chat_msg`;
RENAME TABLE `t_chat_session` TO `ai_chat_session`;
RENAME TABLE `t_message_record` TO `ai_message_record`;
RENAME TABLE `t_reject_recommendation` TO `ai_reject_recommendation`;
RENAME TABLE `user_behavior` TO `ai_user_behavior`;

-- ===== cnt_ 领域 (10 张表) =====
RENAME TABLE `hot_topic` TO `cnt_hot_topic`;
RENAME TABLE `t_add_dish_request` TO `cnt_add_dish_request`;
RENAME TABLE `t_add_dish_setting` TO `cnt_add_dish_setting`;
RENAME TABLE `t_banner` TO `cnt_banner`;
RENAME TABLE `t_dish_step_config` TO `cnt_dish_step_config`;
RENAME TABLE `t_dish_step_history` TO `cnt_dish_step_history`;
RENAME TABLE `t_festival` TO `cnt_festival`;
RENAME TABLE `t_festival_dish_recommend` TO `cnt_festival_dish_recommend`;
RENAME TABLE `t_recipe` TO `cnt_recipe`;
RENAME TABLE `tutorial` TO `cnt_tutorial`;

-- ===== ext_ 领域 (4 张表) =====
RENAME TABLE `t_content_extraction` TO `ext_content_extraction`;
RENAME TABLE `t_content_source` TO `ext_content_source`;
RENAME TABLE `t_extraction_config` TO `ext_extraction_config`;
RENAME TABLE `t_extraction_task` TO `ext_extraction_task`;

-- ===== mch_ 领域 (6 张表) =====
RENAME TABLE `dish_features` TO `mch_dish_features`;
RENAME TABLE `dish_similarity` TO `mch_dish_similarity`;
RENAME TABLE `t_dish` TO `mch_dish`;
RENAME TABLE `t_menu` TO `mch_menu`;
RENAME TABLE `t_menu_dish` TO `mch_menu_dish`;
RENAME TABLE `t_merchant` TO `mch_merchant`;

-- ===== nut_ 领域 (5 张表) =====
RENAME TABLE `j_food_categories` TO `nut_food_categories`;
RENAME TABLE `j_food_nutrition` TO `nut_food_nutrition`;
RENAME TABLE `t_calorie_record` TO `nut_calorie_record`;
RENAME TABLE `t_ingredient_conflict_rule` TO `nut_ingredient_conflict_rule`;
RENAME TABLE `t_nutrition` TO `nut_nutrition`;

-- ===== ord_ 领域 (4 张表) =====
RENAME TABLE `t_order` TO `ord_order`;
RENAME TABLE `t_order_dish` TO `ord_order_dish`;
RENAME TABLE `t_payment_record` TO `ord_payment_record`;
RENAME TABLE `t_refund_record` TO `ord_refund_record`;

-- ===== soc_ 领域 (10 张表) =====
RENAME TABLE `t_collection` TO `soc_collection`;
RENAME TABLE `t_group` TO `soc_group`;
RENAME TABLE `t_group_member` TO `soc_group_member`;
RENAME TABLE `t_group_order` TO `soc_group_order`;
RENAME TABLE `t_group_order_addition` TO `soc_group_order_addition`;
RENAME TABLE `t_group_order_dish` TO `soc_group_order_dish`;
RENAME TABLE `t_group_order_member` TO `soc_group_order_member`;
RENAME TABLE `t_review` TO `soc_review`;
RENAME TABLE `t_review_reply` TO `soc_review_reply`;
RENAME TABLE `t_wish_list_item` TO `soc_wish_list_item`;

-- ===== sys_ 领域 (12 张表) =====
RENAME TABLE `announcement` TO `sys_announcement`;
RENAME TABLE `system_config` TO `sys_system_config`;
RENAME TABLE `t_admin` TO `sys_admin`;
RENAME TABLE `t_admin_operation_log` TO `sys_admin_operation_log`;
RENAME TABLE `t_admin_role_relation` TO `sys_admin_role_relation`;
RENAME TABLE `t_permission` TO `sys_permission`;
RENAME TABLE `t_role` TO `sys_role`;
RENAME TABLE `t_role_permission` TO `sys_role_permission`;
RENAME TABLE `t_role_permission_relation` TO `sys_role_permission_relation`;
RENAME TABLE `t_scheduled_task` TO `sys_scheduled_task`;
RENAME TABLE `t_scheduled_task_log` TO `sys_scheduled_task_log`;
RENAME TABLE `t_system_log` TO `sys_system_log`;

-- ===== usr_ 领域 (11 张表) =====
RENAME TABLE `t_address` TO `usr_address`;
RENAME TABLE `t_contact` TO `usr_contact`;
RENAME TABLE `t_mock_oauth_user` TO `usr_mock_oauth_user`;
RENAME TABLE `t_notification` TO `usr_notification`;
RENAME TABLE `t_user` TO `usr_user`;
RENAME TABLE `t_user_custom_event` TO `usr_user_custom_event`;
RENAME TABLE `t_user_oauth` TO `usr_user_oauth`;
RENAME TABLE `t_user_preference` TO `usr_user_preference`;
RENAME TABLE `t_user_recommend_history` TO `usr_user_recommend_history`;
RENAME TABLE `user_coupon` TO `usr_user_coupon`;
RENAME TABLE `user_profile` TO `usr_profile`;

-- ===== wlt_ 领域 (5 张表) =====
RENAME TABLE `discount` TO `wlt_discount`;
RENAME TABLE `t_consume_history` TO `wlt_consume_history`;
RENAME TABLE `t_recharge_record` TO `wlt_recharge_record`;
RENAME TABLE `t_wallet` TO `wlt_wallet`;
RENAME TABLE `t_withdraw_record` TO `wlt_withdraw_record`;

-- ===== 视图重建 =====
-- user_behavior 表已重命名为 ai_user_behavior，视图需要重建
DROP VIEW IF EXISTS `v_user_behavior_stats`;
CREATE VIEW `v_user_behavior_stats` AS
SELECT
  `user_id` AS `user_id`,
  `behavior_type` AS `behavior_type`,
  COUNT(0) AS `behavior_count`,
  COUNT(DISTINCT `item_id`) AS `unique_items`,
  MIN(`created_time`) AS `first_behavior`,
  MAX(`created_time`) AS `last_behavior`
FROM `ai_user_behavior`
GROUP BY `user_id`, `behavior_type`;


-- ===== 视图处理说明 =====
-- 以下视图引用了已重命名的表，但 mysqldump 未导出完整定义。
-- 暂时 DROP，待后续补充完整定义（属于数据库重构第二阶段）。
-- v_user_behavior_stats 已在上方重建。

DROP VIEW IF EXISTS `v_active_festivals`;
DROP VIEW IF EXISTS `v_content_extraction_overview`;
