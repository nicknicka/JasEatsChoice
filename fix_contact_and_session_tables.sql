-- ============================================
-- 修复群聊创建时的数据库表结构问题
-- ============================================

USE `jia_shi_yi_xuan`;

-- ============================================
-- 问题1：修复 t_contact 表的字段类型
-- ============================================
-- 将 user_id 从 bigint 改为 varchar(64)，以支持字符串类型的用户ID
ALTER TABLE `t_contact`
MODIFY COLUMN `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID';

-- 将 target_id 从 bigint 改为 varchar(64)，以支持群ID（如 G5704945145933460）
ALTER TABLE `t_contact`
MODIFY COLUMN `target_id` VARCHAR(64) NOT NULL COMMENT '目标ID（好友ID或群ID）';

-- ============================================
-- 问题2：修复 t_chat_session 表的 id 字段类型
-- ============================================
-- 先删除旧表（如果存在）
DROP TABLE IF EXISTS `t_chat_session`;

-- 重新创建 t_chat_session 表，使用自增ID作为主键
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
  `pinned` TINYINT(1) DEFAULT 0 COMMENT '是否置顶（0-未置顶，1-置顶）',
  `member_count` INT DEFAULT 0 COMMENT '成员数量（群聊）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_session` (`user_id`, `session_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_session_type` (`session_type`),
  KEY `idx_last_message_time` (`last_message_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- ============================================
-- 说明
-- ============================================
-- 1. target_id 改为 VARCHAR(64)：可以同时存储用户ID（数字字符串）和群ID（带G前缀的字符串）
-- 2. t_chat_session 重新创建：使用自增BIGINT作为主键，符合 MyBatis-Plus 的 IdType.AUTO
-- 3. 添加唯一索引 uk_user_session：确保同一用户对同一会话只有一条记录
-- 4. 添加其他索引：优化查询性能
-- ============================================
