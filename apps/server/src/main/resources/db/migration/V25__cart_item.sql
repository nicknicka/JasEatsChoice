-- 购物车（#603）：后端此前从未实现，前端一直在用本地 store 冒充。
--
-- 设计要点：
--   1. 归属只认 user_id（来自验签后的 JWT），不接受请求体传入的用户身份。
--   2. 同一用户在同一商家下，dishId + spec 相同视为同一项，加购时合并数量，
--      否则「再来一单」「连点加购」会堆出多条重复项。
--   3. merchant_id 允许加购时缺省，由菜品反查补齐，避免前端传错。
--   4. selected 存在服务端，保证切页、重进后勾选状态一致。

CREATE TABLE `t_cart_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(64) NOT NULL COMMENT '所属用户ID（来自验签身份）',
  `merchant_id` varchar(64) NOT NULL COMMENT '商家ID（由菜品反查补齐）',
  `dish_id` varchar(64) NOT NULL COMMENT '菜品ID',
  `spec` varchar(200) NOT NULL DEFAULT '' COMMENT '规格，参与去重；无规格存空串',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `optional_ingredients` varchar(500) DEFAULT NULL COMMENT '可选食材，JSON 数组字符串',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `selected` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否选中：1-是，0-否',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_merchant_dish_spec` (`user_id`,`merchant_id`,`dish_id`,`spec`),
  KEY `idx_user_selected` (`user_id`,`selected`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车项';
