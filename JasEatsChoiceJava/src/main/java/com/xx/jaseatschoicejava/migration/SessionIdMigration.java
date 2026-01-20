package com.xx.jaseatschoicejava.migration;

import com.xx.jaseatschoicejava.util.IdGenerator;
import com.xx.jaseatschoicejava.entity.ChatMsg;
import com.xx.jaseatschoicejava.entity.ChatSession;
import com.xx.jaseatschoicejava.service.ChatMsgService;
import com.xx.jaseatschoicejava.service.ChatSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 会话ID迁移工具
 *
 * 功能：
 * 1. 将旧的MD5哈希session_id迁移为新的IdGenerator格式
 * 2. 保持群聊session_id不变（群ID本身已经是IdGenerator生成的）
 * 3. 只迁移单聊session_id
 *
 * 特点：
 * - 使用IdGenerator生成新的session_id
 * - 无序、不可预测
 * - 与系统其他ID保持一致
 *
 * @author xx
 * @date 2026-01-20
 */
@Slf4j
@Component
public class SessionIdMigration {

    @Autowired
    private ChatMsgService chatMsgService;

    @Autowired
    private ChatSessionService chatSessionService;

    /**
     * 迁移单聊消息的session_id
     *
     * @return 迁移的记录数
     */
    public int migrateChatMsgSessionIds() {
        log.info("开始迁移 t_chat_msg 表的 session_id...");

        int migratedCount = 0;
        int batchSize = 100; // 每批处理100条
        int offset = 0;

        while (true) {
            // 查询需要迁移的消息（单聊）
            List<ChatMsg> messages = chatMsgService.lambdaQuery()
                    .eq(ChatMsg::getMsgType, "single")
                    .isNotNull(ChatMsg::getSessionId)
                    .like(ChatMsg::getSessionId, "S%")
                    .notLike(ChatMsg::getSessionId, "S_________%") // 排除已经是新格式的
                    .last("LIMIT " + batchSize + " OFFSET " + offset)
                    .list();

            if (messages.isEmpty()) {
                break;
            }

            // 批量更新
            for (ChatMsg msg : messages) {
                try {
                    String oldSessionId = msg.getSessionId();
                    String newSessionId = "S" + IdGenerator.generateId();

                    msg.setSessionId(newSessionId);
                    boolean updated = chatMsgService.updateById(msg);

                    if (updated) {
                        migratedCount++;
                        log.debug("迁移消息ID: {} 旧session_id: {} -> 新session_id: {}",
                                msg.getMsgId(), oldSessionId, newSessionId);
                    }
                } catch (Exception e) {
                    log.error("迁移消息ID: {} 失败: {}", msg.getMsgId(), e.getMessage());
                }
            }

            offset += batchSize;
            log.info("已处理 {} 条记录，当前共迁移 {} 条", offset, migratedCount);
        }

        log.info("t_chat_msg 表 session_id 迁移完成，共迁移 {} 条记录", migratedCount);
        return migratedCount;
    }

    /**
     * 迁移会话表的session_id
     *
     * @return 迁移的记录数
     */
    public int migrateChatSessionIds() {
        log.info("开始迁移 t_chat_session 表的 session_id...");

        int migratedCount = 0;
        int batchSize = 100;
        int offset = 0;

        while (true) {
            // 查询需要迁移的会话（单聊）
            List<ChatSession> sessions = chatSessionService.lambdaQuery()
                    .isNotNull(ChatSession::getSessionId)
                    .like(ChatSession::getSessionId, "S%")
                    .notLike(ChatSession::getSessionId, "S_________%") // 排除已经是新格式的
                    .notLike(ChatSession::getSessionId, "G%") // 排除群聊
                    .last("LIMIT " + batchSize + " OFFSET " + offset)
                    .list();

            if (sessions.isEmpty()) {
                break;
            }

            // 批量更新
            for (ChatSession session : sessions) {
                try {
                    String oldSessionId = session.getSessionId();
                    String newSessionId = "S" + IdGenerator.generateId();

                    session.setSessionId(newSessionId);
                    boolean updated = chatSessionService.updateById(session);

                    if (updated) {
                        migratedCount++;
                        log.debug("迁移会话ID: {} 旧session_id: {} -> 新session_id: {}",
                                session.getId(), oldSessionId, newSessionId);
                    }
                } catch (Exception e) {
                    log.error("迁移会话ID: {} 失败: {}", session.getId(), e.getMessage());
                }
            }

            offset += batchSize;
            log.info("已处理 {} 条记录，当前共迁移 {} 条", offset, migratedCount);
        }

        log.info("t_chat_session 表 session_id 迁移完成，共迁移 {} 条记录", migratedCount);
        return migratedCount;
    }

    /**
     * 执行完整的迁移（消息+会话）
     *
     * @return 总迁移记录数
     */
    public int migrateAll() {
        log.info("========== 开始执行会话ID迁移 ==========");

        int msgCount = migrateChatMsgSessionIds();
        int sessionCount = migrateChatSessionIds();

        int totalCount = msgCount + sessionCount;
        log.info("========== 会话ID迁移完成，总计迁移 {} 条记录 ==========", totalCount);

        return totalCount;
    }

    /**
     * 验证迁移结果
     */
    public void validateMigration() {
        log.info("========== 开始验证迁移结果 ==========");

        // 检查消息表
        long totalMsgCount = chatMsgService.count();
        long singleMsgCount = chatMsgService.lambdaQuery()
                .eq(ChatMsg::getMsgType, "single")
                .count();
        long newFormatMsgCount = chatMsgService.lambdaQuery()
                .eq(ChatMsg::getMsgType, "single")
                .isNotNull(ChatMsg::getSessionId)
                .likeRight(ChatMsg::getSessionId, "S")
                .count();
        long nullSessionIdMsgCount = chatMsgService.lambdaQuery()
                .isNull(ChatMsg::getSessionId)
                .count();

        log.info("消息表统计:");
        log.info("  总记录数: {}", totalMsgCount);
        log.info("  单聊记录数: {}", singleMsgCount);
        log.info("  新格式session_id数: {}", newFormatMsgCount);
        log.info("  session_id为NULL的记录数: {}", nullSessionIdMsgCount);

        // 检查会话表
        long totalSessionCount = chatSessionService.count();
        long newFormatSessionCount = chatSessionService.lambdaQuery()
                .isNotNull(ChatSession::getSessionId)
                .likeRight(ChatSession::getSessionId, "S")
                .notLike(ChatSession::getSessionId, "G%")
                .count();
        long nullSessionIdSessionCount = chatSessionService.lambdaQuery()
                .isNull(ChatSession::getSessionId)
                .count();

        log.info("会话表统计:");
        log.info("  总记录数: {}", totalSessionCount);
        log.info("  新格式session_id数: {}", newFormatSessionCount);
        log.info("  session_id为NULL的记录数: {}", nullSessionIdSessionCount);

        log.info("========== 验证完成 ==========");
    }
}
