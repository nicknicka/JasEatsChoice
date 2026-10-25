-- ====================================================================
-- 数据库迁移：添加 session_type 字段到 t_chat_msg 表
-- 目的：分离会话类型和消息类型，解决字段混用问题
-- ====================================================================

-- 1. 添加 session_type 字段（会话类型：single/group）
ALTER TABLE t_chat_msg
ADD COLUMN session_type VARCHAR(20) NULL COMMENT '会话类型：single/group' AFTER msg_type;

-- 2. 迁移现有数据：将 msg_type 中的 single/group 复制到 session_type
UPDATE t_chat_msg
SET session_type = msg_type
WHERE msg_type IN ('single', 'group');

-- 3. 更新 msg_type 字段的注释和值
-- 对于文本消息，将 msg_type 设置为 'text'
-- 对于图片/文件消息，根据 content 判断
UPDATE t_chat_msg
SET msg_type = CASE
    WHEN content = '[图片]' OR file_url IS NOT NULL THEN 'image'
    WHEN content LIKE '[文件]%' OR file_name IS NOT NULL THEN 'file'
    ELSE 'text'
END
WHERE msg_type IN ('single', 'group');

-- 4. 确保 session_type 没有NULL值（如果有的话，设置为默认值）
UPDATE t_chat_msg
SET session_type = 'single'
WHERE session_type IS NULL;

-- 5. 设置 session_type 为 NOT NULL（数据迁移后）
ALTER TABLE t_chat_msg
MODIFY COLUMN session_type VARCHAR(20) NOT NULL COMMENT '会话类型：single/group';

-- 6. 更新 msg_type 字段注释
ALTER TABLE t_chat_msg
MODIFY COLUMN msg_type VARCHAR(20) NOT NULL COMMENT '消息类型：text/image/file';

-- 7. 添加索引以提高查询性能
ALTER TABLE t_chat_msg
ADD INDEX idx_session_type (session_type);

-- 验证迁移结果
SELECT
    '迁移验证' AS step,
    session_type,
    msg_type,
    COUNT(*) AS count
FROM t_chat_msg
GROUP BY session_type, msg_type
ORDER BY session_type, msg_type;
