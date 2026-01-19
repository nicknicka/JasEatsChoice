-- =====================================================
-- 优化聊天系统数据库结构
-- 日期: 2026-01-19
-- 说明: 为 t_chat_msg 添加 session_id 字段，优化消息与会话关联
-- =====================================================

-- 步骤1: 添加 session_id 字段
ALTER TABLE `t_chat_msg`
ADD COLUMN `session_id` VARCHAR(64) DEFAULT NULL COMMENT '会话ID(私聊为生成的会话标识,群聊为群ID)' AFTER `to_id`,
ADD INDEX `idx_session_time` (`session_id`, `create_time`);

-- 步骤2: 生成 session_id 的存储函数
DELIMITER $$

DROP FUNCTION IF EXISTS `generate_session_id`$$

CREATE FUNCTION `generate_session_id`(user1 VARCHAR(64), user2 VARCHAR(64))
RETURNS VARCHAR(128)
DETERMINISTIC
READS SQL DATA
COMMENT '生成单聊会话ID(两个用户ID按字典序拼接)'
BEGIN
  DECLARE result VARCHAR(128);

  -- 按字典序排序后拼接，确保唯一性
  IF user1 < user2 THEN
    SET result = CONCAT(user1, '_', user2);
  ELSE
    SET result = CONCAT(user2, '_', user1);
  END IF;

  RETURN result;
END$$

DELIMITER ;

-- 步骤3: 更新现有消息的 session_id
-- 3.1 更新单聊消息的 session_id
UPDATE `t_chat_msg`
SET `session_id` = generate_session_id(`from_id`, `to_id`)
WHERE `msg_type` = 'single';

-- 3.2 更新群聊消息的 session_id (群聊的 to_id 就是群ID)
UPDATE `t_chat_msg`
SET `session_id` = `to_id`
WHERE `msg_type` = 'group';

-- 步骤4: 添加 reply 相关字段（如果不存在）
-- 检查并添加回复相关字段
ALTER TABLE `t_chat_msg`
ADD COLUMN IF NOT EXISTS `reply_to` VARCHAR(64) DEFAULT NULL COMMENT '回复的消息ID' AFTER `content`,
ADD COLUMN IF NOT EXISTS `reply_content` TEXT DEFAULT NULL COMMENT '被回复的消息内容' AFTER `reply_to`,
ADD COLUMN IF NOT EXISTS `reply_from_id` VARCHAR(64) DEFAULT NULL COMMENT '被回复消息的发送者ID' AFTER `reply_content`,
ADD COLUMN IF NOT EXISTS `reply_from_name` VARCHAR(100) DEFAULT NULL COMMENT '被回复消息的发送者名称' AFTER `reply_from_id`;

-- 步骤5: 优化索引（删除旧索引，使用新索引）
-- 可以选择性删除以下索引，改用 session_id 查询
-- ALTER TABLE `t_chat_msg` DROP INDEX `idx_from_to_time`;

-- 步骤6: 验证数据更新
SELECT
  '单聊消息' as type,
  COUNT(*) as total,
  COUNT(session_id) as has_session_id,
  COUNT(*) - COUNT(session_id) as missing_session_id
FROM `t_chat_msg` WHERE `msg_type` = 'single'

UNION ALL

SELECT
  '群聊消息' as type,
  COUNT(*) as total,
  COUNT(session_id) as has_session_id,
  COUNT(*) - COUNT(session_id) as missing_session_id
FROM `t_chat_msg` WHERE `msg_type` = 'group';

-- 步骤7: 设置 session_id 为 NOT NULL（验证数据完整性后执行）
-- ALTER TABLE `t_chat_msg` MODIFY COLUMN `session_id` VARCHAR(64) NOT NULL COMMENT '会话ID';

-- =====================================================
-- 使用说明
-- =====================================================
-- 1. 执行步骤1-3完成字段添加和数据迁移
-- 2. 执行步骤6验证数据完整性
-- 3. 确认无误后执行步骤7设置为必填字段
-- 4. 新消息插入时需要同步设置 session_id
--
-- Session ID 生成规则：
-- - 单聊: generate_session_id(userA, userB) -> "小ID_大ID"
-- - 群聊: 直接使用群组 ID (to_id)
-- =====================================================
