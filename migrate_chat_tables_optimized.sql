-- =============================================
-- 聊天表结构优化迁移脚本（修正版）
-- 创建时间: 2026-01-20
-- 优化内容:
-- 1. chat_session表添加自增id主键，(session_id, user_id)作为业务唯一键
-- 2. chat_msg表的id改为msg_id，移除冗余的回复内容字段
-- =============================================

-- 备份现有数据
CREATE TABLE IF NOT EXISTS `t_chat_session_backup` LIKE `t_chat_session`;
INSERT INTO `t_chat_session_backup` SELECT * FROM `t_chat_session`;

CREATE TABLE IF NOT EXISTS `t_chat_msg_backup` LIKE `t_chat_msg`;
INSERT INTO `t_chat_msg_backup` SELECT * FROM `t_chat_msg`;

-- =============================================
-- 步骤1: 重建 t_chat_session 表
-- =============================================

-- 删除旧表
DROP TABLE IF EXISTS `t_chat_session`;

-- 创建新表（优化后结构，添加自增主键）
CREATE TABLE `t_chat_session` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` VARCHAR(64) NOT NULL COMMENT '会话标识（私聊为双方用户ID组合，群聊为群ID）',
  `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
  `session_type` VARCHAR(20) NOT NULL COMMENT '会话类型: single-私聊, group-群聊',
  `session_name` VARCHAR(100) NOT NULL COMMENT '会话名称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '会话头像',
  `last_message` TEXT DEFAULT NULL COMMENT '最后一条消息内容',
  `last_message_time` DATETIME DEFAULT NULL COMMENT '最后一条消息时间',
  `unread_count` INT DEFAULT 0 COMMENT '未读消息数',
  `pinned` TINYINT(1) DEFAULT 0 COMMENT '是否置顶：0-未置顶，1-置顶',
  `member_count` INT DEFAULT 0 COMMENT '成员数量（群聊）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_user` (`session_id`, `user_id`),  -- 业务唯一键
  INDEX `idx_user_unread` (`user_id`, `unread_count`),
  INDEX `idx_update_time` (`user_id`, `update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- 从备份恢复数据（注意：旧表没有id字段，会自动生成）
INSERT INTO `t_chat_session` (
  `session_id`, `user_id`, `session_type`, `session_name`, `avatar`,
  `last_message`, `last_message_time`, `unread_count`, `pinned`,
  `member_count`, `create_time`, `update_time`
)
SELECT
  `session_id`, `user_id`, `session_type`, `session_name`, `avatar`,
  `last_message`, `last_message_time`, `unread_count`,
  CASE WHEN `pinned` = TRUE THEN 1 ELSE 0 END AS pinned,  -- 转换BOOLEAN为TINYINT
  `member_count`, `create_time`, `update_time`
FROM `t_chat_session_backup`;

-- =============================================
-- 步骤2: 重建 t_chat_msg 表
-- =============================================

-- 删除旧表
DROP TABLE IF EXISTS `t_chat_msg`;

-- 创建新表（优化后结构）
CREATE TABLE `t_chat_msg` (
  `msg_id` VARCHAR(64) NOT NULL COMMENT '消息ID（主键）',
  `from_id` VARCHAR(64) NOT NULL COMMENT '发送者ID',
  `to_id` VARCHAR(64) DEFAULT NULL COMMENT '接收者ID（群聊时为NULL）',
  `session_id` VARCHAR(64) NOT NULL COMMENT '会话ID',
  `msg_type` VARCHAR(20) NOT NULL COMMENT '消息类型：single/group/order_sync/order_status',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `reply_to` VARCHAR(64) DEFAULT NULL COMMENT '回复的消息ID（引用原消息ID）',
  `read_status` TINYINT(1) DEFAULT 0 COMMENT '已读状态：0-未读，1-已读',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`msg_id`),
  INDEX `idx_session_time` (`session_id`, `create_time`),
  INDEX `idx_from_id` (`from_id`),
  INDEX `idx_to_id` (`to_id`),
  INDEX `idx_reply_to` (`reply_to`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 从备份恢复数据（注意：字段映射）
INSERT INTO `t_chat_msg` (
  `msg_id`, `from_id`, `to_id`, `session_id`, `msg_type`, `content`,
  `reply_to`, `read_status`, `create_time`
)
SELECT
  `id` AS msg_id,
  `from_id`,
  `to_id`,
  `session_id`,
  `msg_type`,
  `content`,
  `reply_to`,
  CASE WHEN `read_status` = TRUE THEN 1 ELSE 0 END AS read_status,  -- 转换BOOLEAN为TINYINT
  `create_time`
FROM `t_chat_msg_backup`;

-- =============================================
-- 步骤3: 验证数据迁移
-- =============================================

-- 验证会话表数据
SELECT
  '会话数据迁移' AS check_type,
  COUNT(*) AS total_count,
  COUNT(DISTINCT CONCAT(session_id, '_', user_id)) AS unique_sessions
FROM `t_chat_session`;

-- 验证消息表数据
SELECT
  '消息数据迁移' AS check_type,
  COUNT(*) AS total_count,
  COUNT(DISTINCT msg_id) AS unique_messages
FROM `t_chat_msg`;

-- 检查业务唯一键约束
SELECT
  '会话业务唯一键检查' AS check_type,
  COUNT(*) AS total_count,
  COUNT(DISTINCT CONCAT(session_id, '_', user_id)) AS unique_session_users,
  (COUNT(*) - COUNT(DISTINCT CONCAT(session_id, '_', user_id))) AS duplicate_count
FROM `t_chat_session`;

-- 检查回复消息引用完整性
SELECT
  '回复消息引用检查' AS check_type,
  COUNT(*) AS total_reply_messages,
  SUM(CASE WHEN m.reply_to IS NOT NULL AND r.msg_id IS NULL THEN 1 ELSE 0 END) AS broken_references
FROM `t_chat_msg` m
LEFT JOIN `t_chat_msg` r ON m.reply_to = r.msg_id
WHERE m.reply_to IS NOT NULL;

-- =============================================
-- 迁移完成提示
-- =============================================
SELECT '迁移完成！请检查上方数据验证结果。如果一切正常，可删除备份表：' AS message;
SELECT 'DROP TABLE IF EXISTS `t_chat_session_backup`;' AS sql_command;
SELECT 'DROP TABLE IF EXISTS `t_chat_msg_backup`;' AS sql_command;
