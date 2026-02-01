-- ============================================
-- RBAC权限角色管理 - 最终版（跳过已存在的字段）
-- ============================================

-- 1. 创建角色表
CREATE TABLE IF NOT EXISTS `t_role` (
    `role_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `role_name` VARCHAR(50) NOT NULL,
    `role_code` VARCHAR(50) UNIQUE NOT NULL,
    `description` VARCHAR(200),
    `status` VARCHAR(20) DEFAULT 'ACTIVE',
    `sort_order` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_role_code (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 创建角色-权限关联表
CREATE TABLE IF NOT EXISTS `t_role_permission_relation` (
    `relation_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `role_id` BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 创建管理员-角色关联表
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
ON DUPLICATE KEY UPDATE role_name=VALUES(role_name);

-- 插入权限数据
INSERT INTO `t_permission` (`permission_name`, `permission_code`, `resource_type`, `parent_id`, `path`, `icon`, `description`, `sort_order`, `status`) VALUES
-- 用户管理
('用户管理', 'user:manage', 'MENU', 0, '/admin/users', 'User', '用户管理', 1, 'ACTIVE'),
('用户列表', 'admin:user:list', 'API', 1, NULL, NULL, '查看用户列表', 1, 'ACTIVE'),
('用户详情', 'admin:user:detail', 'API', 1, NULL, NULL, '查看用户详情', 2, 'ACTIVE'),
('编辑用户', 'admin:user:edit', 'API', 1, NULL, NULL, '编辑用户', 3, 'ACTIVE'),
('删除用户', 'admin:user:delete', 'API', 1, NULL, NULL, '删除用户', 4, 'ACTIVE'),
-- 商家管理
('商家管理', 'merchant:manage', 'MENU', 0, '/admin/merchants', 'Shop', '商家管理', 2, 'ACTIVE'),
('商家列表', 'admin:merchant:list', 'API', 6, NULL, NULL, '查看商家列表', 1, 'ACTIVE'),
('商家详情', 'admin:merchant:detail', 'API', 6, NULL, NULL, '查看商家详情', 2, 'ACTIVE'),
('审核商家', 'admin:merchant:audit', 'API', 6, NULL, NULL, '审核商家', 3, 'ACTIVE'),
('商家状态', 'admin:merchant:status', 'API', 6, NULL, NULL, '修改商家状态', 4, 'ACTIVE'),
-- 订单管理
('订单管理', 'order:manage', 'MENU', 0, '/admin/orders', 'Document', '订单管理', 3, 'ACTIVE'),
('订单列表', 'admin:order:list', 'API', 11, NULL, NULL, '查看订单列表', 1, 'ACTIVE'),
('订单详情', 'admin:order:detail', 'API', 11, NULL, NULL, '查看订单详情', 2, 'ACTIVE'),
('订单状态', 'admin:order:status', 'API', 11, NULL, NULL, '修改订单状态', 3, 'ACTIVE'),
('订单统计', 'admin:order:view', 'API', 11, NULL, NULL, '查看订单统计', 4, 'ACTIVE'),
-- 菜品管理
('菜品管理', 'dish:manage', 'MENU', 0, '/admin/dishes', 'Food', '菜品管理', 4, 'ACTIVE'),
('菜品列表', 'admin:dish:list', 'API', 16, NULL, NULL, '查看菜品列表', 1, 'ACTIVE'),
('菜品详情', 'admin:dish:detail', 'API', 16, NULL, NULL, '查看菜品详情', 2, 'ACTIVE'),
('审核菜品', 'admin:dish:audit', 'API', 16, NULL, NULL, '审核菜品', 3, 'ACTIVE'),
-- 财务管理
('财务管理', 'finance:manage', 'MENU', 0, '/admin/finance', 'Money', '财务管理', 5, 'ACTIVE'),
('充值记录', 'admin:finance:recharge', 'API', 21, NULL, NULL, '查看充值记录', 1, 'ACTIVE'),
('退款管理', 'admin:finance:refund', 'API', 21, NULL, NULL, '管理退款', 2, 'ACTIVE'),
('财务统计', 'admin:finance:statistics', 'API', 21, NULL, NULL, '查看财务统计', 3, 'ACTIVE'),
-- 系统管理
('系统管理', 'system:manage', 'MENU', 0, '/admin/system', 'Setting', '系统管理', 6, 'ACTIVE'),
('系统日志', 'admin:system:logs', 'API', 26, NULL, NULL, '查看系统日志', 1, 'ACTIVE'),
('清理日志', 'admin:system:logs:clean', 'API', 26, NULL, NULL, '清理日志', 2, 'ACTIVE'),
('导出日志', 'admin:system:logs:export', 'API', 26, NULL, NULL, '导出日志', 3, 'ACTIVE'),
-- 角色权限管理
('角色权限', 'role:manage', 'MENU', 0, '/admin/roles', 'Lock', '角色权限', 7, 'ACTIVE'),
('角色列表', 'admin:role:list', 'API', 31, NULL, NULL, '查看角色列表', 1, 'ACTIVE'),
('角色详情', 'admin:role:detail', 'API', 31, NULL, NULL, '查看角色详情', 2, 'ACTIVE'),
('创建角色', 'admin:role:create', 'API', 31, NULL, NULL, '创建角色', 3, 'ACTIVE'),
('更新角色', 'admin:role:update', 'API', 31, NULL, NULL, '更新角色', 4, 'ACTIVE'),
('删除角色', 'admin:role:delete', 'API', 31, NULL, NULL, '删除角色', 5, 'ACTIVE'),
('分配权限', 'admin:role:assign', 'API', 31, NULL, NULL, '分配权限', 6, 'ACTIVE'),
('权限列表', 'admin:permission:list', 'API', 31, NULL, NULL, '查看权限列表', 7, 'ACTIVE'),
('权限详情', 'admin:permission:detail', 'API', 31, NULL, NULL, '查看权限详情', 8, 'ACTIVE'),
('创建权限', 'admin:permission:create', 'API', 31, NULL, NULL, '创建权限', 9, 'ACTIVE'),
('更新权限', 'admin:permission:update', 'API', 31, NULL, NULL, '更新权限', 10, 'ACTIVE'),
('删除权限', 'admin:permission:delete', 'API', 31, NULL, NULL, '删除权限', 11, 'ACTIVE'),
-- 数据统计
('数据统计', 'statistics:manage', 'MENU', 0, '/admin/statistics', 'DataLine', '数据统计', 8, 'ACTIVE'),
('仪表盘', 'admin:statistics:dashboard', 'API', 44, NULL, NULL, '查看仪表盘', 1, 'ACTIVE')
ON DUPLICATE KEY UPDATE permission_name=VALUES(permission_name);

-- 为超级管理员分配所有权限
INSERT INTO `t_role_permission_relation` (`role_id`, `permission_id`)
SELECT 1, permission_id FROM `t_permission`
ON DUPLICATE KEY UPDATE role_id=role_id;

-- 为普通管理员分配权限（排除系统管理和角色权限）
INSERT INTO `t_role_permission_relation` (`role_id`, `permission_id`)
SELECT 2, permission_id FROM `t_permission`
WHERE permission_code NOT LIKE 'system:%' AND permission_code NOT LIKE 'role:%' AND permission_code NOT LIKE 'permission:%'
ON DUPLICATE KEY UPDATE role_id=role_id;

-- 为审核员分配权限
INSERT INTO `t_role_permission_relation` (`role_id`, `permission_id`)
SELECT 3, permission_id FROM `t_permission`
WHERE permission_code IN ('admin:merchant:audit', 'admin:dish:audit', 'admin:merchant:list', 'admin:dish:list', 'admin:merchant:detail', 'admin:dish:detail')
ON DUPLICATE KEY UPDATE role_id=role_id;
