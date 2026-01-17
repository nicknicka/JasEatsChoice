-- 创建聊天会话表
CREATE TABLE IF NOT EXISTS `t_chat_session` (
  `id` VARCHAR(64) NOT NULL COMMENT '会话ID',
  `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
  `session_id` VARCHAR(64) NOT NULL COMMENT '会话标识(私聊为对方用户ID,群聊为群ID)',
  `session_type` VARCHAR(20) NOT NULL COMMENT '会话类型: single-私聊, group-群聊',
  `session_name` VARCHAR(100) NOT NULL COMMENT '会话名称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '会话头像',
  `last_message` TEXT DEFAULT NULL COMMENT '最后一条消息内容',
  `last_message_time` DATETIME DEFAULT NULL COMMENT '最后一条消息时间',
  `unread_count` INT DEFAULT 0 COMMENT '未读消息数',
  `pinned` BOOLEAN DEFAULT FALSE COMMENT '是否置顶',
  `member_count` INT DEFAULT 0 COMMENT '成员数量(群聊)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`),
  UNIQUE KEY `uk_user_session` (`user_id`, `session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- 为聊天消息表添加索引(使用存储过程方式兼容旧版本MySQL)
DELIMITER $$

DROP PROCEDURE IF EXISTS add_chat_msg_indexes$$

CREATE PROCEDURE add_chat_msg_indexes()
BEGIN
  -- 检查并添加 idx_from_to_time 索引
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.statistics
    WHERE table_schema = DATABASE()
    AND table_name = 't_chat_msg'
    AND index_name = 'idx_from_to_time'
  ) THEN
    ALTER TABLE `t_chat_msg` ADD INDEX `idx_from_to_time` (`from_id`, `to_id`, `create_time`);
  END IF;

  -- 检查并添加 idx_to_read 索引
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.statistics
    WHERE table_schema = DATABASE()
    AND table_name = 't_chat_msg'
    AND index_name = 'idx_to_read'
  ) THEN
    ALTER TABLE `t_chat_msg` ADD INDEX `idx_to_read` (`to_id`, `read_status`);
  END IF;
END$$

DELIMITER ;

-- 执行存储过程添加索引
CALL add_chat_msg_indexes();

-- 删除存储过程
DROP PROCEDURE IF EXISTS add_chat_msg_indexes;
