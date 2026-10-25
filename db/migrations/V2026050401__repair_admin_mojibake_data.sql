-- 修复管理员相关历史乱码数据。
-- 仅处理命中典型 UTF-8 被按 latin1/西文编码写入后的乱码模式字段，
-- 并将角色种子数据恢复为规范中文，避免继续依赖前端局部兜底。

-- 用户管理
UPDATE t_user
SET nickname = CONVERT(BINARY(CONVERT(nickname USING latin1)) USING utf8mb4)
WHERE nickname REGEXP 'å|ç|æ|é|è|ä|ï|ð';

UPDATE t_user
SET location = CONVERT(BINARY(CONVERT(location USING latin1)) USING utf8mb4)
WHERE location REGEXP 'å|ç|æ|é|è|ä|ï|ð';

UPDATE t_user
SET bio = CONVERT(BINARY(CONVERT(bio USING latin1)) USING utf8mb4)
WHERE bio REGEXP 'å|ç|æ|é|è|ä|ï|ð';

-- 菜品管理
UPDATE t_dish
SET name = CONVERT(BINARY(CONVERT(name USING latin1)) USING utf8mb4)
WHERE name REGEXP 'å|ç|æ|é|è|ä|ï|ð';

UPDATE t_dish
SET description = CONVERT(BINARY(CONVERT(description USING latin1)) USING utf8mb4)
WHERE description REGEXP 'å|ç|æ|é|è|ä|ï|ð';

UPDATE t_dish
SET audit_comment = CONVERT(BINARY(CONVERT(audit_comment USING latin1)) USING utf8mb4)
WHERE audit_comment REGEXP 'å|ç|æ|é|è|ä|ï|ð';

-- 热点话题管理
UPDATE hot_topic
SET content = CONVERT(BINARY(CONVERT(content USING latin1)) USING utf8mb4)
WHERE content REGEXP 'å|ç|æ|é|è|ä|ï|ð';

UPDATE hot_topic
SET remark = CONVERT(BINARY(CONVERT(remark USING latin1)) USING utf8mb4)
WHERE remark REGEXP 'å|ç|æ|é|è|ä|ï|ð';

UPDATE hot_topic
SET review_comment = CONVERT(BINARY(CONVERT(review_comment USING latin1)) USING utf8mb4)
WHERE review_comment REGEXP 'å|ç|æ|é|è|ä|ï|ð';

-- 商家管理
UPDATE t_merchant
SET name = CONVERT(BINARY(CONVERT(name USING latin1)) USING utf8mb4)
WHERE name REGEXP 'å|ç|æ|é|è|ä|ï|ð';

UPDATE t_merchant
SET address = CONVERT(BINARY(CONVERT(address USING latin1)) USING utf8mb4)
WHERE address REGEXP 'å|ç|æ|é|è|ä|ï|ð';

UPDATE t_merchant
SET contact_name = CONVERT(BINARY(CONVERT(contact_name USING latin1)) USING utf8mb4)
WHERE contact_name REGEXP 'å|ç|æ|é|è|ä|ï|ð';

UPDATE t_merchant
SET audit_reason = CONVERT(BINARY(CONVERT(audit_reason USING latin1)) USING utf8mb4)
WHERE audit_reason REGEXP 'å|ç|æ|é|è|ä|ï|ð';

-- 公告管理
UPDATE announcement
SET title = CONVERT(BINARY(CONVERT(title USING latin1)) USING utf8mb4)
WHERE title REGEXP 'å|ç|æ|é|è|ä|ï|ð';

UPDATE announcement
SET content = CONVERT(BINARY(CONVERT(content USING latin1)) USING utf8mb4)
WHERE content REGEXP 'å|ç|æ|é|è|ä|ï|ð';

-- 角色与权限管理
UPDATE t_role
SET role_name = '超级管理员',
    description = '拥有系统所有权限，不受权限系统限制'
WHERE role_code = 'SUPER_ADMIN';

UPDATE t_role
SET role_name = '普通管理员',
    description = '拥有大部分管理权限，但不能管理系统配置和角色权限'
WHERE role_code = 'ADMIN';

UPDATE t_role
SET role_name = '审核员',
    description = '只能进行内容审核操作'
WHERE role_code = 'AUDITOR';

UPDATE t_permission
SET permission_name = CONVERT(BINARY(CONVERT(permission_name USING latin1)) USING utf8mb4)
WHERE permission_name REGEXP 'å|ç|æ|é|è|ä|ï|ð';

UPDATE t_permission
SET description = CONVERT(BINARY(CONVERT(description USING latin1)) USING utf8mb4)
WHERE description REGEXP 'å|ç|æ|é|è|ä|ï|ð';

-- 管理员资料
UPDATE t_admin
SET real_name = CONVERT(BINARY(CONVERT(real_name USING latin1)) USING utf8mb4)
WHERE real_name REGEXP 'å|ç|æ|é|è|ä|ï|ð';

UPDATE t_admin
SET remark = CONVERT(BINARY(CONVERT(remark USING latin1)) USING utf8mb4)
WHERE remark REGEXP 'å|ç|æ|é|è|ä|ï|ð';
