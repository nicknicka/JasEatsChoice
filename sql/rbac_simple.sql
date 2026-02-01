-- ============================================
-- RBAC权限角色管理模块 - 简化版SQL脚本
-- ============================================

-- 1. 创建角色表
CREATE TABLE IF NOT EXISTS `t_role` (
    `role_id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(50) UNIQUE NOT NULL COMMENT '角色编码',
    `description` VARCHAR(200) COMMENT '角色描述',
    `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_role_code (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 2. 添加description字段到权限表
ALTER TABLE `t_permission` ADD COLUMN `description` VARCHAR(200) COMMENT '权限描述' AFTER `icon`;

-- 3. 创建角色-权限关联表
CREATE TABLE IF NOT EXISTS `t_role_permission_relation` (
    `relation_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `role_id` BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    INDEX idx_role_id (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 创建管理员-角色关联表
CREATE TABLE IF NOT EXISTS `t_admin_role_relation` (
    `relation_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `admin_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_admin_role` (`admin_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入基础角色
INSERT INTO `t_role` (`role_name`, `role_code`, `description`, `status`, `sort_order`) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有所有权限', 'ACTIVE', 1),
('普通管理员', 'ADMIN', '常规管理权限', 'ACTIVE', 2),
('审核员', 'AUDITOR', '负责审核', 'ACTIVE', 3)
ON DUPLICATE KEY UPDATE role_name=role_name;
