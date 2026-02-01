-- ============================================
-- RBAC权限角色管理模块 - 数据库表创建脚本（修复版）
-- ============================================

-- 1. 角色表（如果不存在）
CREATE TABLE IF NOT EXISTS `t_role` (
    `role_id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(50) UNIQUE NOT NULL COMMENT '角色编码：SUPER_ADMIN-超级管理员, ADMIN-普通管理员, AUDITOR-审核员',
    `description` VARCHAR(200) COMMENT '角色描述',
    `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-启用, DISABLED-禁用',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_role_code (`role_code`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 2. 为权限表添加description字段（如果还没有）
-- 注意：先检查字段是否存在，不存在才添加
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = 'jia_shi_yi_xuan'
                   AND TABLE_NAME = 't_permission'
                   AND COLUMN_NAME = 'description');

SET @sql = IF(@col_exists = 0,
              'ALTER TABLE `t_permission` ADD COLUMN `description` VARCHAR(200) COMMENT ''权限描述'' AFTER `icon`',
              'SELECT ''Column description already exists'' AS message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 角色-权限关联表（修正表名）
CREATE TABLE IF NOT EXISTS `t_role_permission_relation` (
    `relation_id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    INDEX idx_role_id (`role_id`),
    INDEX idx_permission_id (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限关联表';

-- 4. 管理员-角色关联表
CREATE TABLE IF NOT EXISTS `t_admin_role_relation` (
    `relation_id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    `admin_id` BIGINT NOT NULL COMMENT '管理员ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_admin_role` (`admin_id`, `role_id`),
    INDEX idx_admin_id (`admin_id`),
    INDEX idx_role_id (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员-角色关联表';

-- ============================================
-- 初始化基础角色数据
-- ============================================

-- 插入基础角色
INSERT INTO `t_role` (`role_name`, `role_code`, `description`, `status`, `sort_order`) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有所有权限的超级管理员', 'ACTIVE', 1),
('普通管理员', 'ADMIN', '拥有常规管理权限', 'ACTIVE', 2),
('审核员', 'AUDITOR', '负责审核商家和内容', 'ACTIVE', 3)
ON DUPLICATE KEY UPDATE
    `role_name` = VALUES(`role_name`),
    `description` = VALUES(`description`);

-- ============================================
-- 初始化权限树数据
-- ============================================

-- 一级：用户管理
INSERT INTO `t_permission` (`permission_name`, `permission_code`, `resource_type`, `parent_id`, `path`, `icon`, `description`, `sort_order`, `status`) VALUES
('用户管理', 'user:manage', 'MENU', 0, '/admin/users', 'User', '用户管理模块', 1, 'ACTIVE')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

-- 二级：用户管理子项
SET @parent_id = LAST_INSERT_ID();
IF @parent_id = 0 THEN
    SET @parent_id = (SELECT permission_id FROM t_permission WHERE permission_code = 'user:manage');
END IF;

INSERT INTO `t_permission` (`permission_name`, `permission_code`, `resource_type`, `parent_id`, `path`, `icon`, `description`, `sort_order`, `status`) VALUES
('用户列表', 'admin:user:list', 'API', @parent_id, NULL, NULL, '查看用户列表', 1, 'ACTIVE'),
('用户详情', 'admin:user:detail', 'API', @parent_id, NULL, NULL, '查看用户详情', 2, 'ACTIVE'),
('编辑用户', 'admin:user:edit', 'API', @parent_id, NULL, NULL, '编辑用户信息', 3, 'ACTIVE'),
('删除用户', 'admin:user:delete', 'API', @parent_id, NULL, NULL, '删除用户', 4, 'ACTIVE')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

-- 一级：商家管理
INSERT INTO `t_permission` (`permission_name`, `permission_code`, `resource_type`, `parent_id`, `path`, `icon`, `description`, `sort_order`, `status`) VALUES
('商家管理', 'merchant:manage', 'MENU', 0, '/admin/merchants', 'Shop', '商家管理模块', 2, 'ACTIVE')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

-- 二级：商家管理子项
SET @parent_id = LAST_INSERT_ID();
IF @parent_id = 0 THEN
    SET @parent_id = (SELECT permission_id FROM t_permission WHERE permission_code = 'merchant:manage');
END IF;

INSERT INTO `t_permission` (`permission_name`, `permission_code`, `resource_type`, `parent_id`, `path`, `icon`, `description`, `sort_order`, `status`) VALUES
('商家列表', 'admin:merchant:list', 'API', @parent_id, NULL, NULL, '查看商家列表', 1, 'ACTIVE'),
('商家详情', 'admin:merchant:detail', 'API', @parent_id, NULL, NULL, '查看商家详情', 2, 'ACTIVE'),
('审核商家', 'admin:merchant:audit', 'API', @parent_id, NULL, NULL, '审核商家申请', 3, 'ACTIVE'),
('商家状态', 'admin:merchant:status', 'API', @parent_id, NULL, NULL, '修改商家状态', 4, 'ACTIVE')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

-- 一级：订单管理
INSERT INTO `t_permission` (`permission_name`, `permission_code`, `resource_type`, `parent_id`, `path`, `icon`, `description`, `sort_order`, `status`) VALUES
('订单管理', 'order:manage', 'MENU', 0, '/admin/orders', 'Document', '订单管理模块', 3, 'ACTIVE')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

-- 二级：订单管理子项
SET @parent_id = LAST_INSERT_ID();
IF @parent_id = 0 THEN
    SET @parent_id = (SELECT permission_id FROM t_permission WHERE permission_code = 'order:manage');
END IF;

INSERT INTO `t_permission` (`permission_name`, `permission_code`, `resource_type`, `parent_id`, `path`, `icon`, `description`, `sort_order`, `status`) VALUES
('订单列表', 'admin:order:list', 'API', @parent_id, NULL, NULL, '查看订单列表', 1, 'ACTIVE'),
('订单详情', 'admin:order:detail', 'API', @parent_id, NULL, NULL, '查看订单详情', 2, 'ACTIVE'),
('订单状态', 'admin:order:status', 'API', @parent_id, NULL, NULL, '修改订单状态', 3, 'ACTIVE'),
('订单统计', 'admin:order:view', 'API', @parent_id, NULL, NULL, '查看订单统计', 4, 'ACTIVE')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

-- 一级：菜品管理
INSERT INTO `t_permission` (`permission_name`, `permission_code`, `resource_type`, `parent_id`, `path`, `icon`, `description`, `sort_order`, `status`) VALUES
('菜品管理', 'dish:manage', 'MENU', 0, '/admin/dishes', 'Food', '菜品管理模块', 4, 'ACTIVE')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

-- 二级：菜品管理子项
SET @parent_id = LAST_INSERT_ID();
IF @parent_id = 0 THEN
    SET @parent_id = (SELECT permission_id FROM t_permission WHERE permission_code = 'dish:manage');
END IF;

INSERT INTO `t_permission` (`permission_name`, `permission_code`, `resource_type`, `parent_id`, `path`, `icon`, `description`, `sort_order`, `status`) VALUES
('菜品列表', 'admin:dish:list', 'API', @parent_id, NULL, NULL, '查看菜品列表', 1, 'ACTIVE'),
('菜品详情', 'admin:dish:detail', 'API', @parent_id, NULL, NULL, '查看菜品详情', 2, 'ACTIVE'),
('审核菜品', 'admin:dish:audit', 'API', @parent_id, NULL, NULL, '审核菜品', 3, 'ACTIVE')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

-- 一级：财务管理
INSERT INTO `t_permission` (`permission_name`, `permission_code`, `resource_type`, `parent_id`, `path`, `icon`, `description`, `sort_order`, `status`) VALUES
('财务管理', 'finance:manage', 'MENU', 0, '/admin/finance', 'Money', '财务管理模块', 5, 'ACTIVE')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

-- 二级：财务管理子项
SET @parent_id = LAST_INSERT_ID();
IF @parent_id = 0 THEN
    SET @parent_id = (SELECT permission_id FROM t_permission WHERE permission_code = 'finance:manage');
END IF;

INSERT INTO `t_permission` (`permission_name`, `permission_code`, `resource_type`, `parent_id`, `path`, `icon`, `description`, `sort_order`, `status`) VALUES
('充值记录', 'admin:finance:recharge', 'API', @parent_id, NULL, NULL, '查看充值记录', 1, 'ACTIVE'),
('退款管理', 'admin:finance:refund', 'API', @parent_id, NULL, NULL, '管理退款申请', 2, 'ACTIVE'),
('财务统计', 'admin:finance:statistics', 'API', @parent_id, NULL, NULL, '查看财务统计', 3, 'ACTIVE')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

-- 一级：系统管理
INSERT INTO `t_permission` (`permission_name`, `permission_code`, `resource_type`, `parent_id`, `path`, `icon`, `description`, `sort_order`, `status`) VALUES
('系统管理', 'system:manage', 'MENU', 0, '/admin/system', 'Setting', '系统管理模块', 6, 'ACTIVE')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

-- 二级：系统管理子项
SET @parent_id = LAST_INSERT_ID();
IF @parent_id = 0 THEN
    SET @parent_id = (SELECT permission_id FROM t_permission WHERE permission_code = 'system:manage');
END IF;

INSERT INTO `t_permission` (`permission_name`, `permission_code`, `resource_type`, `parent_id`, `path`, `icon`, `description`, `sort_order`, `status`) VALUES
('系统日志', 'admin:system:logs', 'API', @parent_id, NULL, NULL, '查看系统日志', 1, 'ACTIVE'),
('清理日志', 'admin:system:logs:clean', 'API', @parent_id, NULL, NULL, '清理过期日志', 2, 'ACTIVE'),
('导出日志', 'admin:system:logs:export', 'API', @parent_id, NULL, NULL, '导出系统日志', 3, 'ACTIVE')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

-- 一级：角色权限管理
INSERT INTO `t_permission` (`permission_name`, `permission_code`, `resource_type`, `parent_id`, `path`, `icon`, `description`, `sort_order`, `status`) VALUES
('角色权限', 'role:manage', 'MENU', 0, '/admin/roles', 'Lock', '角色权限管理', 7, 'ACTIVE')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

-- 二级：角色权限子项
SET @parent_id = LAST_INSERT_ID();
IF @parent_id = 0 THEN
    SET @parent_id = (SELECT permission_id FROM t_permission WHERE permission_code = 'role:manage');
END IF;

INSERT INTO `t_permission` (`permission_name`, `permission_code`, `resource_type`, `parent_id`, `path`, `icon`, `description`, `sort_order`, `status`) VALUES
('角色列表', 'admin:role:list', 'API', @parent_id, NULL, NULL, '查看角色列表', 1, 'ACTIVE'),
('角色详情', 'admin:role:detail', 'API', @parent_id, NULL, NULL, '查看角色详情', 2, 'ACTIVE'),
('创建角色', 'admin:role:create', 'API', @parent_id, NULL, NULL, '创建角色', 3, 'ACTIVE'),
('更新角色', 'admin:role:update', 'API', @parent_id, NULL, NULL, '更新角色', 4, 'ACTIVE'),
('删除角色', 'admin:role:delete', 'API', @parent_id, NULL, NULL, '删除角色', 5, 'ACTIVE'),
('分配权限', 'admin:role:assign', 'API', @parent_id, NULL, NULL, '为角色分配权限', 6, 'ACTIVE'),
('权限列表', 'admin:permission:list', 'API', @parent_id, NULL, NULL, '查看权限列表', 7, 'ACTIVE'),
('权限详情', 'admin:permission:detail', 'API', @parent_id, NULL, NULL, '查看权限详情', 8, 'ACTIVE'),
('创建权限', 'admin:permission:create', 'API', @parent_id, NULL, NULL, '创建权限', 9, 'ACTIVE'),
('更新权限', 'admin:permission:update', 'API', @parent_id, NULL, NULL, '更新权限', 10, 'ACTIVE'),
('删除权限', 'admin:permission:delete', 'API', @parent_id, NULL, NULL, '删除权限', 11, 'ACTIVE')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

-- 一级：数据统计
INSERT INTO `t_permission` (`permission_name`, `permission_code`, `resource_type`, `parent_id`, `path`, `icon`, `description`, `sort_order`, `status`) VALUES
('数据统计', 'statistics:manage', 'MENU', 0, '/admin/statistics', 'DataLine', '数据统计模块', 8, 'ACTIVE')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

-- 二级：数据统计子项
SET @parent_id = LAST_INSERT_ID();
IF @parent_id = 0 THEN
    SET @parent_id = (SELECT permission_id FROM t_permission WHERE permission_code = 'statistics:manage');
END IF;

INSERT INTO `t_permission` (`permission_name`, `permission_code`, `resource_type`, `parent_id`, `path`, `icon`, `description`, `sort_order`, `status`) VALUES
('仪表盘', 'admin:statistics:dashboard', 'API', @parent_id, NULL, NULL, '查看数据仪表盘', 1, 'ACTIVE')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

-- ============================================
-- 为超级管理员角色分配所有权限
-- ============================================
INSERT INTO `t_role_permission_relation` (`role_id`, `permission_id`)
SELECT
    (SELECT role_id FROM t_role WHERE role_code = 'SUPER_ADMIN'),
    permission_id
FROM t_permission
ON DUPLICATE KEY UPDATE role_id = role_id;

-- ============================================
-- 为普通管理员角色分配常规权限（排除系统管理和角色权限管理）
-- ============================================
INSERT INTO `t_role_permission_relation` (`role_id`, `permission_id`)
SELECT
    (SELECT role_id FROM t_role WHERE role_code = 'ADMIN'),
    permission_id
FROM t_permission
WHERE permission_code NOT LIKE 'system:%'
  AND permission_code NOT LIKE 'role:%'
  AND permission_code NOT LIKE 'permission:%'
ON DUPLICATE KEY UPDATE role_id = role_id;

-- ============================================
-- 为审核员角色分配审核相关权限
-- ============================================
INSERT INTO `t_role_permission_relation` (`role_id`, `permission_id`)
SELECT
    (SELECT role_id FROM t_role WHERE role_code = 'AUDITOR'),
    permission_id
FROM t_permission
WHERE permission_code IN (
    'admin:merchant:audit',
    'admin:dish:audit',
    'admin:merchant:list',
    'admin:dish:list',
    'admin:merchant:detail',
    'admin:dish:detail'
)
ON DUPLICATE KEY UPDATE role_id = role_id;
