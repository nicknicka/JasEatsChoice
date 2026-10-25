-- 为 t_chat_session 表添加 group_id 字段
-- 用于存储群聊会话对应的群组ID，方便前端快速获取群信息

-- 添加字段
ALTER TABLE t_chat_session
ADD COLUMN group_id VARCHAR(20) NULL COMMENT '群组ID（仅群聊会话有效）' AFTER member_count;

-- 添加索引以提高查询性能
CREATE INDEX idx_group_id ON t_chat_session(group_id);
