-- 为聊天消息表添加回复/引用功能
-- 添加回复相关字段
ALTER TABLE `t_chat_msg`
ADD COLUMN `reply_to` VARCHAR(64) DEFAULT NULL COMMENT '回复的消息ID' AFTER `content`,
ADD COLUMN `reply_content` TEXT DEFAULT NULL COMMENT '被回复的消息内容' AFTER `reply_to`,
ADD COLUMN `reply_from_id` VARCHAR(64) DEFAULT NULL COMMENT '被回复消息的发送者ID' AFTER `reply_content`,
ADD COLUMN `reply_from_name` VARCHAR(100) DEFAULT NULL COMMENT '被回复消息的发送者名称' AFTER `reply_from_id`;

-- 添加索引以提升查询性能
ALTER TABLE `t_chat_msg`
ADD INDEX `idx_reply_to` (`reply_to`);
