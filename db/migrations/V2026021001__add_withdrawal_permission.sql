-- 添加提现审核权限（修复权限编码不匹配问题）
-- 代码中使用的是 admin:finance:withdrawals（复数）

-- 首先检查权限是否存在，不存在则插入
INSERT IGNORE INTO t_permission (permission_name, permission_code, resource_type, parent_id, path, sort_order, status)
VALUES ('提现审核', 'admin:finance:withdrawals', 'API', 8, '/api/admin/withdrawals', 1, 'ACTIVE');

-- 给超级管理员（role_id=1）分配提现审核权限
INSERT IGNORE INTO t_role_permission (role_id, permission_id)
SELECT 1, permission_id FROM t_permission WHERE permission_code = 'admin:finance:withdrawals';

-- 给普通管理员（role_id=2）分配提现审核权限
INSERT IGNORE INTO t_role_permission (role_id, permission_id)
SELECT 2, permission_id FROM t_permission WHERE permission_code = 'admin:finance:withdrawals';
