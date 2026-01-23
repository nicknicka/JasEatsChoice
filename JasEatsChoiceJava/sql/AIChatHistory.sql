-- AI聊天历史表
CREATE TABLE IF NOT EXISTS `t_ai_chat_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` VARCHAR(50) NOT NULL COMMENT '用户ID',
  `sender` VARCHAR(20) NOT NULL COMMENT '发送者：user-用户，ai-AI助手',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天历史记录表';
