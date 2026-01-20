-- ====================================================================
-- 会话ID（session_id）迁移脚本
-- 功能：将旧的MD5哈希session_id迁移为新的IdGenerator格式
-- 特点：无序、不可预测、与系统其他ID保持一致
-- ====================================================================

-- 备份原数据（安全起见）
CREATE TABLE IF NOT EXISTS t_chat_msg_backup_20260120 AS
SELECT * FROM t_chat_msg;

CREATE TABLE IF NOT EXISTS t_chat_session_backup_20260120 AS
SELECT * FROM t_chat_session;

-- ====================================================================
-- 迁移策略：
-- 1. 对于单聊：生成新的S开头的16位数字session_id
-- 2. 对于群聊：保持原有的群ID（无需修改）
-- ====================================================================

-- ====================================================================
-- 第一步：迁移 t_chat_msg 表的 session_id
-- ====================================================================

-- 查看当前session_id分布情况
SELECT
    msg_type,
    COUNT(*) as count,
    COUNT(DISTINCT session_id) as unique_sessions
FROM t_chat_msg
GROUP BY msg_type;

-- 迁移单聊消息的session_id
-- 注意：这里使用临时表来避免锁表时间过长
DROP TEMPORARY TABLE IF EXISTS temp_msg_id_mapping;

CREATE TEMPORARY TABLE temp_msg_id_mapping AS
SELECT
    id,
    -- 为单聊生成新的session_id
    CONCAT('S', LPAD(FLOOR(RAND() * 10000000000000000), 16, '0')) as new_session_id
FROM t_chat_msg
WHERE msg_type = 'single'
  AND session_id IS NOT NULL
  AND session_id LIKE 'S%';

-- 更新t_chat_msg表
UPDATE t_chat_msg cm
INNER JOIN temp_msg_id_mapping tmp ON cm.id = tmp.id
SET cm.session_id = tmp.new_session_id
WHERE cm.msg_type = 'single'
  AND cm.session_id IS NOT NULL
  AND cm.session_id LIKE 'S%';

-- ====================================================================
-- 第二步：迁移 t_chat_session 表的 session_id
-- ====================================================================

DROP TEMPORARY TABLE IF EXISTS temp_session_id_mapping;

CREATE TEMPORARY TABLE temp_session_id_mapping AS
SELECT
    id,
    -- 为单聊会话生成新的session_id
    CONCAT('S', LPAD(FLOOR(RAND() * 10000000000000000), 16, '0')) as new_session_id
FROM t_chat_session
WHERE session_id LIKE 'S%'
  AND session_id NOT LIKE 'G%';  -- 排除群聊

-- 更新t_chat_session表
UPDATE t_chat_session cs
INNER JOIN temp_session_id_mapping tmp ON cs.id = tmp.id
SET cs.session_id = tmp.new_session_id
WHERE cs.session_id LIKE 'S%'
  AND cs.session_id NOT LIKE 'G%';

-- ====================================================================
-- 验证迁移结果
-- ====================================================================

-- 检查新的session_id格式
SELECT
    CASE
        WHEN session_id REGEXP '^S[0-9]{16}$' THEN '新格式（正确）'
        WHEN session_id REGEXP '^G[0-9]{16}$' THEN '群聊（正确）'
        ELSE '旧格式或其他'
    END as id_format,
    COUNT(*) as count
FROM t_chat_msg
GROUP BY id_format;

-- 检查是否有NULL值
SELECT
    't_chat_msg中session_id为NULL的记录' as check_item,
    COUNT(*) as count
FROM t_chat_msg
WHERE session_id IS NULL;

SELECT
    't_chat_session中session_id为NULL的记录' as check_item,
    COUNT(*) as count
FROM t_chat_session
WHERE session_id IS NULL;

-- ====================================================================
-- 清理临时表
-- ====================================================================
DROP TEMPORARY TABLE IF EXISTS temp_msg_id_mapping;
DROP TEMPORARY TABLE IF EXISTS temp_session_id_mapping;

-- ====================================================================
-- 回滚脚本（如果需要回滚，请执行以下语句）
-- ====================================================================

-- 恢复t_chat_msg
-- DROP TABLE IF EXISTS t_chat_msg;
-- CREATE TABLE t_chat_msg AS SELECT * FROM t_chat_msg_backup_20260120;

-- 恢复t_chat_session
-- DROP TABLE IF EXISTS t_chat_session;
-- CREATE TABLE t_chat_session AS SELECT * FROM t_chat_session_backup_20260120;

-- ====================================================================
-- 注意事项
-- ====================================================================

-- 1. 此脚本会更新现有的session_id，请确保在非高峰期执行
-- 2. 执行前务必备份数据
-- 3. 迁移过程中，应用可能需要暂停服务或处理session_id变化
-- 4. 群聊的session_id（群ID）不会被修改
-- 5. 单聊的session_id会被重新生成，不影响聊天记录的关联性
-- 6. 建议在测试环境先执行，确认无误后再在生产环境执行

-- ====================================================================
-- 执行建议
-- ====================================================================

-- 1. 先在测试环境验证
-- 2. 确认备份已创建
-- 3. 在业务低峰期执行
-- 4. 执行后验证数据完整性
-- 5. 监控应用日志，确保无异常
