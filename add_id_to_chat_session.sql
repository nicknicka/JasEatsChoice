-- ============================================
-- 为 t_chat_session 表添加自增主键 id 列
-- ============================================

USE `jia_shi_yi_xuan`;

-- 添加自增主键列（如果不存在）
ALTER TABLE `t_chat_session`
ADD COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT FIRST,
ADD PRIMARY KEY (`id`);

-- 添加唯一索引（如果不存在）
ALTER TABLE `t_chat_session`
ADD UNIQUE KEY `uk_user_session` (`user_id`, `session_id`);

-- 说明：
-- 1. 添加 id 列作为自增主键，放在第一列（FIRST）
-- 2. 自动填充现有数据的 id 值（从1开始递增）
-- 3. 添加唯一索引确保同一用户对同一会话只有一条记录
