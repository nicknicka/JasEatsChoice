-- 创建聊天消息表(使用String类型ID,与ChatMsg实体类匹配)
DROP TABLE IF EXISTS `t_chat_msg`;
CREATE TABLE `t_chat_msg` (
  `id` VARCHAR(64) NOT NULL COMMENT '消息ID',
  `from_id` VARCHAR(64) NOT NULL COMMENT '发送者ID',
  `to_id` VARCHAR(64) NOT NULL COMMENT '接收者ID',
  `msg_type` VARCHAR(20) NOT NULL COMMENT '消息类型(single/group/order_sync/order_status)',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `read_status` TINYINT(1) DEFAULT 0 COMMENT '已读状态:0-未读,1-已读',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_from_to_time` (`from_id`, `to_id`, `create_time`),
  KEY `idx_to_read` (`to_id`, `read_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- 创建聊天会话表
DROP TABLE IF EXISTS `t_chat_session`;
CREATE TABLE `t_chat_session` (
  `id` VARCHAR(64) NOT NULL COMMENT '会话ID',
  `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
  `session_id` VARCHAR(64) NOT NULL COMMENT '会话标识(私聊为对方用户ID,群聊为群ID)',
  `session_type` VARCHAR(20) NOT NULL COMMENT '会话类型: single-私聊, group-群聊',
  `session_name` VARCHAR(100) NOT NULL COMMENT '会话名称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '会话头像',
  `last_message` TEXT DEFAULT NULL COMMENT '最后一条消息内容',
  `last_message_time` DATETIME DEFAULT NULL COMMENT '最后一条消息时间',
  `unread_count` INT DEFAULT 0 COMMENT '未读消息数',
  `pinned` TINYINT(1) DEFAULT 0 COMMENT '是否置顶:0-否,1-是',
  `member_count` INT DEFAULT 0 COMMENT '成员数量(群聊)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_last_time` (`last_message_time`),
  UNIQUE KEY `uk_user_session` (`user_id`, `session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';
