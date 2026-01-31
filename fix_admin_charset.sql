-- 修复管理员表和相关表的字符集问题
-- 使用此脚本可以修复数据库中已存在的乱码数据

USE jia_shi_yi_xuan;

-- 1. 检查当前表的字符集
SELECT
    TABLE_NAME,
    TABLE_COLLATION,
    CHARACTER_SET_NAME,
    COLLATION_NAME
FROM
    information_schema.TABLES
WHERE
    TABLE_SCHEMA = 'jia_shi_yi_xuan'
    AND TABLE_NAME IN ('t_admin', 't_role');

-- 2. 修改表的字符集为UTF-8
ALTER TABLE t_admin CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE t_role CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 3. 修改管理员表中字段的字符集
ALTER TABLE t_admin
    MODIFY COLUMN real_name VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    MODIFY COLUMN username VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 4. 修改角色表中字段的字符集
ALTER TABLE t_role
    MODIFY COLUMN role_name VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    MODIFY COLUMN role_code VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    MODIFY COLUMN description VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 5. 更新角色名称（如果数据库中已有乱码数据，需要手动更正）
-- 请根据实际情况调整以下UPDATE语句

-- 查看当前角色数据
SELECT role_id, role_code, role_name FROM t_role;

-- 如果发现乱码，使用以下语句更新（示例）：
-- UPDATE t_role SET role_name = '超级管理员' WHERE role_code = 'SUPER_ADMIN';
-- UPDATE t_role SET role_name = '管理员' WHERE role_code = 'ADMIN';
-- UPDATE t_role SET role_name = '用户管理员' WHERE role_code = 'USER_MANAGER';

-- 6. 检查管理员数据
SELECT admin_id, username, real_name, role_id FROM t_admin;

-- 7. 如果管理员表中的real_name也是乱码，可以批量更新
-- UPDATE t_admin SET real_name = '系统管理员' WHERE username = 'admin';
