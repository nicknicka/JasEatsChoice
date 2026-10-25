-- 为 t_chat_session 表添加 related_order_id 字段
-- 用于存储群订单专属会话关联的群订单ID

-- 添加字段
ALTER TABLE t_chat_session
ADD COLUMN related_order_id VARCHAR(20) NULL COMMENT '关联的群订单ID（群订单专属会话有效）' AFTER group_id;

-- 添加索引以提高查询性能
CREATE INDEX idx_related_order_id ON t_chat_session(related_order_id);

-- 添加 session_type 枚举值支持
-- 'single'-私聊, 'group'-群聊, 'group_order'-群订单专属会话
-- 注意：如需要严格约束，可使用 CHECK 约束（MySQL 8.0.16+）
-- ALTER TABLE t_chat_session
-- MODIFY COLUMN session_type ENUM('single', 'group', 'group_order') NOT NULL COMMENT '会话类型: single-私聊, group-群聊, group_order-群订单专属会话';
