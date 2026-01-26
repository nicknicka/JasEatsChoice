-- 为 t_chat_session 表添加 target_id 字段
-- 用于存储单聊会话对方用户的userId，方便前端快速获取聊天对象
-- 创建时间：2026-01-26

ALTER TABLE t_chat_session
ADD COLUMN target_id VARCHAR(255) NULL COMMENT '目标用户ID（仅单聊会话有效，表示对方的userId）' AFTER group_id;

-- 添加索引以提高查询性能
CREATE INDEX idx_target_id ON t_chat_session(target_id);
