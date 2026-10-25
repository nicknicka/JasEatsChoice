-- ========================================
-- 管理员系统数据库表设计
-- ========================================

-- 1. 管理员表
CREATE TABLE IF NOT EXISTS t_admin (
    admin_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '管理员用户名',
    password VARCHAR(64) NOT NULL COMMENT '密码（加密）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(11) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(255) COMMENT '头像URL',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-活跃, LOCKED-锁定, DELETED-删除',
    role_id BIGINT COMMENT '角色ID（关联t_role表）',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    remark VARCHAR(255) COMMENT '备注',
    INDEX idx_username (username),
    INDEX idx_status (status),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 2. 角色表
CREATE TABLE IF NOT EXISTS t_role (
    role_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(20) NOT NULL UNIQUE COMMENT '角色编码：SUPER_ADMIN-超级管理员, ADMIN-普通管理员, AUDITOR-审核员',
    description VARCHAR(255) COMMENT '角色描述',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-启用, DISABLED-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    INDEX idx_role_code (role_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 3. 权限表
CREATE TABLE IF NOT EXISTS t_permission (
    permission_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(50) NOT NULL UNIQUE COMMENT '权限编码',
    resource_type VARCHAR(20) NOT NULL COMMENT '资源类型：MENU-菜单, BUTTON-按钮, API-接口',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID（0表示顶级权限）',
    path VARCHAR(100) COMMENT '路由路径（菜单类型使用）',
    icon VARCHAR(50) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-启用, DISABLED-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_permission_code (permission_code),
    INDEX idx_resource_type (resource_type),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 4. 角色权限关联表
CREATE TABLE IF NOT EXISTS t_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 5. 管理员操作日志表
CREATE TABLE IF NOT EXISTS t_admin_operation_log (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    admin_id BIGINT COMMENT '管理员ID',
    username VARCHAR(50) COMMENT '管理员用户名',
    operation_type VARCHAR(50) COMMENT '操作类型：LOGIN, LOGOUT, CREATE, UPDATE, DELETE, AUDIT等',
    module_name VARCHAR(50) COMMENT '模块名称',
    operation_desc VARCHAR(255) COMMENT '操作描述',
    request_method VARCHAR(10) COMMENT '请求方法：GET, POST, PUT, DELETE',
    request_url VARCHAR(255) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    execute_time INT COMMENT '执行时长（毫秒）',
    status VARCHAR(20) COMMENT '状态：SUCCESS-成功, FAIL-失败',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_admin_id (admin_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作日志表';

-- ========================================
-- 初始化数据
-- ========================================

-- 插入默认角色
INSERT INTO t_role (role_name, role_code, description, sort_order) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有系统所有权限，不受权限系统限制', 1),
('普通管理员', 'ADMIN', '拥有大部分管理权限，但不能管理系统配置和角色权限', 2),
('审核员', 'AUDITOR', '只能进行内容审核操作', 3);

-- 插入权限数据
INSERT INTO t_permission (permission_name, permission_code, resource_type, parent_id, path, sort_order) VALUES
-- 一级菜单
('控制台', 'admin:dashboard', 'MENU', 0, '/admin/dashboard', 1),
('用户管理', 'admin:user', 'MENU', 0, '/admin/users', 2),
('商家管理', 'admin:merchant', 'MENU', 0, '/admin/merchants', 3),
('订单管理', 'admin:order', 'MENU', 0, '/admin/orders', 4),
('菜品管理', 'admin:dish', 'MENU', 0, '/admin/dishes', 5),
('教程管理', 'admin:tutorial', 'MENU', 0, '/admin/tutorials', 6),
('内容管理', 'admin:content', 'MENU', 0, '/admin/content', 7),
('财务管理', 'admin:finance', 'MENU', 0, '/admin/finance', 8),
('系统设置', 'admin:setting', 'MENU', 0, '/admin/settings', 9),
('数据统计', 'admin:statistics', 'MENU', 0, '/admin/statistics', 10),

-- 用户管理子权限
('用户列表', 'admin:user:list', 'API', 2, '/api/admin/users', 1),
('用户详情', 'admin:user:detail', 'API', 2, '/api/admin/users/*', 2),
('修改用户状态', 'admin:user:status', 'API', 2, '/api/admin/users/*/status', 3),
('删除用户', 'admin:user:delete', 'API', 2, '/api/admin/users/*', 4),

-- 商家管理子权限
('商家列表', 'admin:merchant:list', 'API', 3, '/api/admin/merchants', 1),
('商家审核', 'admin:merchant:audit', 'API', 3, '/api/admin/merchants/*/audit', 2),
('修改商家状态', 'admin:merchant:status', 'API', 3, '/api/admin/merchants/*/status', 3),

-- 订单管理子权限
('订单列表', 'admin:order:list', 'API', 4, '/api/admin/orders', 1),
('订单详情', 'admin:order:detail', 'API', 4, '/api/admin/orders/*', 2),
('修改订单状态', 'admin:order:status', 'API', 4, '/api/admin/orders/*/status', 3),

-- 菜品管理子权限
('菜品列表', 'admin:dish:list', 'API', 5, '/api/admin/dishes', 1),
('菜品审核', 'admin:dish:audit', 'API', 5, '/api/admin/dishes/*/audit', 2),

-- 教程管理子权限
('教程列表', 'admin:tutorial:list', 'API', 6, '/api/admin/tutorials', 1),
('教程审核', 'admin:tutorial:audit', 'API', 6, '/api/admin/tutorials/*/audit', 2),
('教程删除', 'admin:tutorial:delete', 'API', 6, '/api/admin/tutorials/*', 3),

-- 热点话题管理
('热点话题列表', 'admin:topic:list', 'API', 7, '/api/admin/topics', 1),
('热点话题审核', 'admin:topic:audit', 'API', 7, '/api/admin/topics/*/audit', 2),

-- 公告管理
('公告列表', 'admin:announcement:list', 'API', 7, '/api/admin/announcements', 1),
('公告发布', 'admin:announcement:create', 'API', 7, '/api/admin/announcements', 2),

-- 财务管理子权限
('提现审核', 'admin:finance:withdrawal', 'API', 8, '/api/admin/finance/withdrawals', 1),
('充值记录', 'admin:finance:recharge', 'API', 8, '/api/admin/finance/recharges', 2),
('退款管理', 'admin:finance:refund', 'API', 8, '/api/admin/finance/refunds', 3),

-- 系统设置子权限
('角色管理', 'admin:setting:role', 'MENU', 9, '/admin/settings/roles', 1),
('角色列表', 'admin:setting:role:list', 'API', 9, '/api/admin/settings/roles', 2),
('角色创建', 'admin:setting:role:create', 'API', 9, '/api/admin/settings/roles', 3),
('权限分配', 'admin:setting:permission', 'API', 9, '/api/admin/settings/permissions', 4),
('系统日志', 'admin:setting:log', 'MENU', 9, '/admin/settings/logs', 5),
('日志查看', 'admin:setting:log:view', 'API', 9, '/api/admin/settings/logs', 6),

-- 统计数据
('统计数据', 'admin:statistics:view', 'API', 10, '/api/admin/statistics', 1);

-- 给超级管理员分配所有权限
INSERT INTO t_role_permission (role_id, permission_id)
SELECT 1, permission_id FROM t_permission;

-- 给普通管理员分配基础权限（排除角色权限管理和系统配置）
INSERT INTO t_role_permission (role_id, permission_id)
SELECT 2, permission_id FROM t_permission
WHERE permission_code NOT LIKE 'admin:setting:role%'
AND permission_code NOT LIKE 'admin:setting:permission%'
AND permission_code NOT LIKE 'admin:setting:log%';

-- 给审核员分配审核权限
INSERT INTO t_role_permission (role_id, permission_id)
SELECT 3, permission_id FROM t_permission
WHERE permission_code LIKE '%audit%';

-- 插入默认超级管理员账号
-- 密码：admin123（BCrypt加密后的值，需要在代码中生成）
-- 注意：这里需要使用真实的BCrypt加密密码
INSERT INTO t_admin (username, password, real_name, status, role_id) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'ACTIVE', 1);

-- ========================================
-- 注意事项：
-- 1. 默认管理员密码需要在代码中使用 BCrypt 加密
-- 2. 建议首次登录后立即修改默认密码
-- 3. 实际使用时应该删除或注释掉默认管理员账号的 INSERT 语句，通过后台创建
-- ========================================
