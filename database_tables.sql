-- ==========================================
-- 佳食宜选 - 数据库表创建脚本
-- ==========================================
-- 说明：本项目未启用 Flyway，所有数据库表需要手动创建
-- 创建日期：2026-01-29
-- ==========================================

-- -------------------------------------------------
-- 推荐拒绝记录表
-- -------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_reject_recommendation` (
    `id` VARCHAR(64) PRIMARY KEY COMMENT '主键ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `dish_id` VARCHAR(64) NOT NULL COMMENT '菜品ID',
    `reject_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '拒绝时间',
    `reason` VARCHAR(255) COMMENT '拒绝原因',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_dish_id` (`dish_id`),
    INDEX `idx_user_dish` (`user_id`, `dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推荐拒绝记录表';

-- 验证表创建
-- SHOW TABLES LIKE 't_reject_recommendation';
-- DESC t_reject_recommendation;
