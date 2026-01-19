-- =====================================================
-- 升级 session_id 为哈希格式
-- 日期: 2026-01-19
-- 说明: 将现有的简单格式 session_id 升级为 MD5 哈希格式
-- =====================================================

-- 注意：由于数据库函数无法直接调用Java的MD5方法
-- 我们需要通过Java代码来更新数据

-- 方法1：创建临时存储过程（可选，用于批量更新）
DELIMITER $$

DROP PROCEDURE IF EXISTS upgrade_session_ids$$

CREATE PROCEDURE upgrade_session_ids()
BEGIN
  DECLARE done INT DEFAULT FALSE;
  DECLARE old_id VARCHAR(128);
  DECLARE new_id VARCHAR(128);
  DECLARE user1 VARCHAR(64);
  DECLARE user2 VARCHAR(64);

  -- 声明游标
  DECLARE cursor CURSOR FOR
    SELECT DISTINCT session_id
    FROM t_chat_msg
    WHERE msg_type = 'single'
      AND session_id LIKE '%\\_%'  -- 包含下划线的旧格式
      AND session_id NOT LIKE 'S%'; -- 不以S开头的（未升级的）

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

  OPEN cursor;

  read_loop: LOOP
    FETCH cursor INTO old_id;
    IF done THEN
      LEAVE read_loop;
    END IF;

    -- 解析旧的 session_id（格式：user1_user2）
    SET user1 = SUBSTRING_INDEX(old_id, '_', 1);
    SET user2 = SUBSTRING_INDEX(old_id, '_', -1);

    -- 注意：这里需要Java代码生成新的哈希值
    -- 由于MySQL无法直接生成MD5，这个部分需要在Java中完成
    SELECT old_id, user1, user2;
  END LOOP;

  CLOSE cursor;
END$$

DELIMITER ;

-- 方法2：查看需要升级的数据
SELECT
  session_id as old_session_id,
  CONCAT('需要升级: ', session_id) as note
FROM t_chat_msg
WHERE msg_type = 'single'
  AND session_id LIKE '%\\_%'
  AND session_id NOT LIKE 'S%'
GROUP BY session_id;

-- 方法3：手动更新示例（一条一条更新）
-- 实际的哈希值需要通过Java代码生成
-- UPDATE t_chat_msg
-- SET session_id = 'S[新生成的MD5哈希]'
-- WHERE session_id = '1000000000000000_3384650106421960';

-- =====================================================
-- Java代码迁移示例（推荐使用）
-- =====================================================

/*
// 在Java代码中执行迁移：

@Service
public class SessionIdUpgradeService {

    @Autowired
    private ChatMsgService chatMsgService;

    public void upgradeSessionIds() {
        // 1. 查询所有需要升级的消息
        LambdaQueryWrapper<ChatMsg> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMsg::getMsgType, "single")
               .likeRight(ChatMsg::getSessionId, "1")  // 旧格式以数字开头
               .notLikeRight(ChatMsg::getSessionId, "S");  // 不以S开头

        List<ChatMsg> messages = chatMsgService.list(wrapper);

        // 2. 批量更新
        messages.forEach(msg -> {
            String oldSessionId = msg.getSessionId();
            String newSessionId = ChatSessionIdGenerator.upgradeSessionId(oldSessionId);

            if (!oldSessionId.equals(newSessionId)) {
                msg.setSessionId(newSessionId);
                chatMsgService.updateById(msg);

                System.out.println("升级: " + oldSessionId + " -> " + newSessionId);
            }
        });

        System.out.println("升级完成，共处理 " + messages.size() + " 条消息");
    }
}
*/
