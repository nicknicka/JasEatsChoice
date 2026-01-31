-- =============================================
-- 菜品步骤细化管理功能 - 数据库迁移脚本
-- 版本：V2025_01_30
-- 作者：Claude
-- 描述：添加菜品步骤状态跟踪和历史记录功能
-- =============================================

-- 1. 创建菜品步骤历史记录表
CREATE TABLE IF NOT EXISTS `t_dish_step_history` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `order_dish_id` VARCHAR(32) NOT NULL COMMENT '订单菜品ID',
  `order_id` VARCHAR(32) NOT NULL COMMENT '订单ID',
  `dish_id` VARCHAR(32) NOT NULL COMMENT '菜品ID',
  `old_step_status` INT DEFAULT NULL COMMENT '原步骤状态',
  `new_step_status` INT NOT NULL COMMENT '新步骤状态',
  `operation_type` VARCHAR(20) DEFAULT NULL COMMENT '操作类型：FORWARD-前进, BACKWARD-回退, SKIP-跳过',
  `operator_id` VARCHAR(32) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
  `rollback_reason` VARCHAR(500) DEFAULT NULL COMMENT '回退原因',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `estimated_minutes` INT DEFAULT NULL COMMENT '预计完成时间（分钟）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_order_dish_id` (`order_dish_id`),
  INDEX `idx_order_id` (`order_id`),
  INDEX `idx_dish_id` (`dish_id`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品步骤历史记录表';

-- 2. 为订单菜品表添加步骤相关字段（使用存储过程检查字段是否存在）
DROP PROCEDURE IF EXISTS add_order_dish_step_columns;

DELIMITER //
CREATE PROCEDURE add_order_dish_step_columns()
BEGIN
  -- 添加 step_status 字段
  IF NOT EXISTS (
    SELECT * FROM information_schema.columns
    WHERE table_schema = DATABASE()
    AND table_name = 't_order_dish'
    AND column_name = 'step_status'
  ) THEN
    ALTER TABLE `t_order_dish`
    ADD COLUMN `step_status` INT DEFAULT 0 COMMENT '步骤状态：0-待备菜 1-备菜中 2-预处理中 3-烹饪中 4-摆盘中 5-待上菜 6-已上菜 10-快餐制作中 11-快餐打包中 12-快餐待出餐 13-快餐已出餐' AFTER `customization`;
  END IF;

  -- 添加 step_start_time 字段
  IF NOT EXISTS (
    SELECT * FROM information_schema.columns
    WHERE table_schema = DATABASE()
    AND table_name = 't_order_dish'
    AND column_name = 'step_start_time'
  ) THEN
    ALTER TABLE `t_order_dish`
    ADD COLUMN `step_start_time` DATETIME DEFAULT NULL COMMENT '当前步骤开始时间' AFTER `step_status`;
  END IF;

  -- 添加 estimated_completion_time 字段
  IF NOT EXISTS (
    SELECT * FROM information_schema.columns
    WHERE table_schema = DATABASE()
    AND table_name = 't_order_dish'
    AND column_name = 'estimated_completion_time'
  ) THEN
    ALTER TABLE `t_order_dish`
    ADD COLUMN `estimated_completion_time` DATETIME DEFAULT NULL COMMENT '预计完成时间' AFTER `step_start_time`;
  END IF;

  -- 添加 cooking_minutes 字段
  IF NOT EXISTS (
    SELECT * FROM information_schema.columns
    WHERE table_schema = DATABASE()
    AND table_name = 't_order_dish'
    AND column_name = 'cooking_minutes'
  ) THEN
    ALTER TABLE `t_order_dish`
    ADD COLUMN `cooking_minutes` INT DEFAULT NULL COMMENT '烹饪耗时（分钟）' AFTER `estimated_completion_time`;
  END IF;

  -- 添加 step_sort 字段
  IF NOT EXISTS (
    SELECT * FROM information_schema.columns
    WHERE table_schema = DATABASE()
    AND table_name = 't_order_dish'
    AND column_name = 'step_sort'
  ) THEN
    ALTER TABLE `t_order_dish`
    ADD COLUMN `step_sort` INT DEFAULT 999 COMMENT '步骤排序（优先级），数值越小越优先处理' AFTER `cooking_minutes`;
  END IF;

  -- 添加 is_fast_food 字段
  IF NOT EXISTS (
    SELECT * FROM information_schema.columns
    WHERE table_schema = DATABASE()
    AND table_name = 't_order_dish'
    AND column_name = 'is_fast_food'
  ) THEN
    ALTER TABLE `t_order_dish`
    ADD COLUMN `is_fast_food` TINYINT(1) DEFAULT 0 COMMENT '是否为快餐：1-是 0-否' AFTER `step_sort`;
  END IF;

  -- 添加 serving_status 字段
  IF NOT EXISTS (
    SELECT * FROM information_schema.columns
    WHERE table_schema = DATABASE()
    AND table_name = 't_order_dish'
    AND column_name = 'serving_status'
  ) THEN
    ALTER TABLE `t_order_dish`
    ADD COLUMN `serving_status` INT DEFAULT 0 COMMENT '上菜状态：0-未上菜 1-已上菜 2-已撤餐' AFTER `is_fast_food`;
  END IF;

  -- 添加索引
  IF NOT EXISTS (
    SELECT * FROM information_schema.statistics
    WHERE table_schema = DATABASE()
    AND table_name = 't_order_dish'
    AND index_name = 'idx_step_status'
  ) THEN
    ALTER TABLE `t_order_dish` ADD INDEX `idx_step_status` (`step_status`);
  END IF;

  IF NOT EXISTS (
    SELECT * FROM information_schema.statistics
    WHERE table_schema = DATABASE()
    AND table_name = 't_order_dish'
    AND index_name = 'idx_step_sort'
  ) THEN
    ALTER TABLE `t_order_dish` ADD INDEX `idx_step_sort` (`step_sort`);
  END IF;
END //
DELIMITER ;

-- 执行存储过程
CALL add_order_dish_step_columns();

-- 删除存储过程
DROP PROCEDURE IF EXISTS add_order_dish_step_columns;

-- 3. 为菜品表添加烹饪时长字段
DROP PROCEDURE IF EXISTS add_dish_step_columns;

DELIMITER //
CREATE PROCEDURE add_dish_step_columns()
BEGIN
  -- 添加 cooking_minutes 字段
  IF NOT EXISTS (
    SELECT * FROM information_schema.columns
    WHERE table_schema = DATABASE()
    AND table_name = 't_dish'
    AND column_name = 'cooking_minutes'
  ) THEN
    ALTER TABLE `t_dish`
    ADD COLUMN `cooking_minutes` INT DEFAULT 15 COMMENT '标准烹饪时长（分钟）' AFTER `calorie`;
  END IF;

  -- 添加 is_fast_food 字段
  IF NOT EXISTS (
    SELECT * FROM information_schema.columns
    WHERE table_schema = DATABASE()
    AND table_name = 't_dish'
    AND column_name = 'is_fast_food'
  ) THEN
    ALTER TABLE `t_dish`
    ADD COLUMN `is_fast_food` TINYINT(1) DEFAULT 0 COMMENT '是否为快餐：1-是 0-否' AFTER `cooking_minutes`;
  END IF;

  -- 添加 step_template 字段
  IF NOT EXISTS (
    SELECT * FROM information_schema.columns
    WHERE table_schema = DATABASE()
    AND table_name = 't_dish'
    AND column_name = 'step_template'
  ) THEN
    ALTER TABLE `t_dish`
    ADD COLUMN `step_template` VARCHAR(20) DEFAULT 'NORMAL' COMMENT '步骤模板：NORMAL-正餐流程(备菜→预处理→烹饪→摆盘→上菜), FAST-快餐流程(制作→打包→出餐)' AFTER `is_fast_food`;
  END IF;
END //
DELIMITER ;

-- 执行存储过程
CALL add_dish_step_columns();

-- 删除存储过程
DROP PROCEDURE IF EXISTS add_dish_step_columns;

-- 4. 创建菜品步骤配置表（用于商家自定义步骤）
CREATE TABLE IF NOT EXISTS `t_dish_step_config` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `merchant_id` VARCHAR(32) NOT NULL COMMENT '商家ID',
  `dish_id` VARCHAR(32) DEFAULT NULL COMMENT '菜品ID（为NULL表示全局默认配置）',
  `step_code` INT NOT NULL COMMENT '步骤代码',
  `step_name` VARCHAR(50) NOT NULL COMMENT '步骤名称',
  `step_order` INT NOT NULL COMMENT '步骤顺序',
  `estimated_minutes` INT DEFAULT NULL COMMENT '预计耗时（分钟）',
  `is_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用：1-启用 0-禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_dish_step` (`merchant_id`, `dish_id`, `step_code`),
  INDEX `idx_merchant_id` (`merchant_id`),
  INDEX `idx_dish_id` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品步骤配置表';

-- 5. 插入默认的步骤配置（使用INSERT IGNORE避免重复）
INSERT IGNORE INTO `t_dish_step_config` (`id`, `merchant_id`, `dish_id`, `step_code`, `step_name`, `step_order`, `estimated_minutes`)
VALUES
(UUID(), 'DEFAULT', NULL, 0, '待备菜', 1, 0),
(UUID(), 'DEFAULT', NULL, 1, '备菜中', 2, 10),
(UUID(), 'DEFAULT', NULL, 2, '预处理中', 3, 5),
(UUID(), 'DEFAULT', NULL, 3, '烹饪中', 4, 15),
(UUID(), 'DEFAULT', NULL, 4, '摆盘中', 5, 3),
(UUID(), 'DEFAULT', NULL, 5, '待上菜', 6, 0),
(UUID(), 'DEFAULT', NULL, 6, '已上菜', 7, 0);

-- 6. 插入快餐步骤配置
INSERT IGNORE INTO `t_dish_step_config` (`id`, `merchant_id`, `dish_id`, `step_code`, `step_name`, `step_order`, `estimated_minutes`)
VALUES
(UUID(), 'DEFAULT', NULL, 10, '制作中', 1, 8),
(UUID(), 'DEFAULT', NULL, 11, '打包中', 2, 2),
(UUID(), 'DEFAULT', NULL, 12, '待出餐', 3, 0),
(UUID(), 'DEFAULT', NULL, 13, '已出餐', 4, 0);

-- 7. 创建视图：订单菜品步骤总览（使用BINARY操作避免字符集冲突）
CREATE OR REPLACE VIEW `v_order_dish_step_overview` AS
SELECT
    od.id AS order_dish_id,
    od.order_id,
    BINARY od.dish_id AS dish_id,
    d.name AS dish_name,
    od.quantity,
    od.step_status,
    od.step_start_time,
    od.estimated_completion_time,
    od.cooking_minutes,
    od.step_sort,
    od.is_fast_food,
    od.serving_status,
    o.status AS order_status,
    (SELECT COUNT(*) FROM t_dish_step_history dsh WHERE BINARY dsh.order_dish_id = BINARY od.id) AS step_change_count,
    (SELECT MAX(dsh.create_time) FROM t_dish_step_history dsh WHERE BINARY dsh.order_dish_id = BINARY od.id) AS last_step_change_time
FROM t_order_dish od
LEFT JOIN t_dish d ON BINARY od.dish_id = BINARY d.id
LEFT JOIN t_order o ON BINARY od.order_id = BINARY o.id;

-- 8. 添加组合索引以优化查询性能
DROP PROCEDURE IF EXISTS add_order_dish_indexes;

DELIMITER //
CREATE PROCEDURE add_order_dish_indexes()
BEGIN
  IF NOT EXISTS (
    SELECT * FROM information_schema.statistics
    WHERE table_schema = DATABASE()
    AND table_name = 't_order_dish'
    AND index_name = 'idx_order_step_status'
  ) THEN
    ALTER TABLE `t_order_dish` ADD INDEX `idx_order_step_status` (`order_id`, `step_status`);
  END IF;
END //
DELIMITER ;

CALL add_order_dish_indexes();
DROP PROCEDURE IF EXISTS add_order_dish_indexes;

-- =============================================
-- 迁移完成提示
-- =============================================
SELECT '菜品步骤细化管理功能数据库迁移完成！' AS message;
