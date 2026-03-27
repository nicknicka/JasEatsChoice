package com.xx.jaseatschoicejava.agent.memory;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xx.jaseatschoicejava.entity.AIChatHistory;
import com.xx.jaseatschoicejava.mapper.AIChatHistoryMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Redis + MySQL 混合存储的 ChatMemory 实现
 *
 * 工作流程：
 * 1. Redis 存储最近20条消息（TTL=2小时）
 * 2. 异步写入 MySQL 持久化
 * 3. Redis 未命中时从 MySQL 加载历史
 *
 * @author Claude
 * @since 2026-03-26
 */
@Slf4j
public class RedisBackedChatMemory implements ChatMemory {

    private final RedisTemplate<String, String> redisTemplate;
    private final AIChatHistoryMapper chatHistoryMapper;
    private final Long userId;
    private final String redisKey;
    private final Duration ttl;
    private final int maxMessages;

    // 本地缓存（避免每次都查Redis）
    private List<ChatMessage> localMessages;

    public RedisBackedChatMemory(
            RedisTemplate<String, String> redisTemplate,
            AIChatHistoryMapper chatHistoryMapper,
            Long userId,
            Duration ttl,
            int maxMessages) {
        this.redisTemplate = redisTemplate;
        this.chatHistoryMapper = chatHistoryMapper;
        this.userId = userId;
        this.redisKey = "chat:memory:" + userId;
        this.ttl = ttl;
        this.maxMessages = maxMessages;
        this.localMessages = new ArrayList<>();

        // 首次加载：先查Redis，未命中则查MySQL
        loadFromStorage();
    }

    @Override
    public void add(ChatMessage message) {
        // ========== 【智能过滤】只保存最终结果到数据库 ==========

        // 1. 过滤SystemMessage（包含大量prompt）
        if (isSystemMessage(message)) {
            log.debug("⏭️ 跳过SystemMessage，不保存到存储: userId={}, 类型={}",
                userId, message.getClass().getSimpleName());
            // 仍然添加到本地缓存（用于本次对话的上下文）
            localMessages.add(message);
            return;
        }

        // 2. 过滤中间的Agent调用（JSON格式的Agent调用记录）
        if (isIntermediateAgentCall(message)) {
            log.debug("⏭️ 跳过中间Agent调用，不保存到存储: userId={}", userId);
            // 仍然添加到本地缓存（用于本次对话的上下文）
            localMessages.add(message);
            return;
        }

        // 3. 保存最终结果到存储
        // 1. 添加到本地缓存（关键操作，必须成功）
        localMessages.add(message);

        // 2. 序列化并写入Redis（添加异常处理）
        try {
            String messageData = serializeMessage(message);
            redisTemplate.opsForList().rightPush(redisKey, messageData);

            // 3. 保留最近N条消息
            if (localMessages.size() > maxMessages) {
                localMessages = localMessages.subList(
                    localMessages.size() - maxMessages,
                    localMessages.size()
                );
            }
            redisTemplate.opsForList().trim(redisKey, -maxMessages, -1);

            // 4. 设置TTL（如果key不存在）
            redisTemplate.expire(redisKey, ttl);

            log.debug("✅ Redis写入成功: userId={}, messageCount={}",
                userId, localMessages.size());
        } catch (Exception e) {
            // Redis连接失败时优雅降级，只使用本地缓存
            log.warn("⚠️ Redis写入失败，降级到本地缓存: userId={}, error={}",
                userId, e.getMessage());
            // 不抛出异常，继续执行
        }

        // 5. 异步写入MySQL（独立于Redis状态）
        asyncSaveToMySQL(message);

        log.debug("用户 {} 添加消息到ChatMemory, 当前消息数: {}",
            userId, localMessages.size());
    }

    /**
     * 判断是否为SystemMessage
     * SystemMessage包含系统提示词，不需要持久化到数据库
     */
    private boolean isSystemMessage(ChatMessage message) {
        if (message == null) {
            return false;
        }

        // 检查是否为LangChain4j的SystemMessage
        String className = message.getClass().getSimpleName();
        if ("SystemMessage".equals(className)) {
            return true;
        }

        // 通过toString检查
        String messageStr = message.toString();
        if (messageStr != null && messageStr.contains("SystemMessage {")) {
            return true;
        }

        return false;
    }

    /**
     * 判断是否为中间的Agent调用
     * 中间的Agent调用是JSON格式的工具调用记录，不需要持久化到数据库
     *
     * 例如：
     * {
     *   "agentName": "DishRecommendationAgent$0",
     *   "arguments": {"userMessage": "..."}
     * }
     */
    private boolean isIntermediateAgentCall(ChatMessage message) {
        if (message == null || !(message instanceof AiMessage)) {
            return false;
        }

        try {
            String content = ((AiMessage) message).text();

            // 检查是否为JSON格式的Agent调用
            if (content == null || content.isEmpty()) {
                return false;
            }

            // 去除首尾空白
            content = content.trim();

            // 检查是否以 { 开头且包含 agentName 字段（Agent调用JSON）
            if (content.startsWith("{") && content.contains("\"agentName\"")) {
                // 进一步检查：如果包含 "$" 数字后缀，说明是中间调用
                if (content.contains("$0") || content.contains("$1") ||
                    content.contains("$2") || content.contains("$3") ||
                    content.contains("$4") || content.contains("$5") ||
                    content.contains("$6")) {
                    return true;
                }
            }

            // 检查是否包含 "The user request is:" 或 "You must answer strictly in the following JSON format"
            // 这些是SupervisorPlanner的中间输出
            if (content.contains("The user request is:") ||
                content.contains("You must answer strictly in the following JSON format")) {
                return true;
            }

            return false;
        } catch (Exception e) {
            log.warn("检查Agent调用失败: userId={}, error={}", userId, e.getMessage());
            return false;
        }
    }

    @Override
    public List<ChatMessage> messages() {
        return new ArrayList<>(localMessages);
    }

    @Override
    public void clear() {
        // 清空本地缓存
        localMessages.clear();

        // 删除Redis key
        redisTemplate.delete(redisKey);

        log.debug("用户 {} 清空ChatMemory", userId);
    }

    @Override
    public String id() {
        return "chat-memory-" + userId;
    }

    /**
     * 从存储加载数据
     * 优先从Redis加载，未命中则从MySQL加载
     */
    private void loadFromStorage() {
        // 1. 尝试从Redis加载
        List<ChatMessage> redisMessages = loadFromRedis();

        if (redisMessages != null && !redisMessages.isEmpty()) {
            this.localMessages = redisMessages;
            log.debug("用户 {} 从Redis加载 {} 条消息", userId, redisMessages.size());
            return;
        }

        // 2. Redis未命中，从MySQL加载
        List<ChatMessage> mysqlMessages = loadFromMySQL();

        if (mysqlMessages != null && !mysqlMessages.isEmpty()) {
            this.localMessages = mysqlMessages;

            // 回写到Redis（加速下次访问）
            for (ChatMessage msg : mysqlMessages) {
                String messageData = serializeMessage(msg);
                redisTemplate.opsForList().rightPush(redisKey, messageData);
            }
            redisTemplate.expire(redisKey, ttl);

            log.debug("用户 {} 从MySQL加载 {} 条消息并回写Redis",
                userId, mysqlMessages.size());
        }
    }

    /**
     * 从Redis加载消息
     */
    private List<ChatMessage> loadFromRedis() {
        try {
            List<String> dataList = redisTemplate.opsForList()
                .range(redisKey, 0, -1);

            if (dataList == null || dataList.isEmpty()) {
                return null;
            }

            List<ChatMessage> messages = new ArrayList<>();
            for (String data : dataList) {
                ChatMessage msg = deserializeMessage(data);
                if (msg != null) {
                    messages.add(msg);
                }
            }

            return messages;
        } catch (Exception e) {
            log.error("从Redis加载消息失败，userId={}", userId, e);
            return null;
        }
    }

    /**
     * 从MySQL加载最近N条消息
     */
    private List<ChatMessage> loadFromMySQL() {
        try {
            QueryWrapper<AIChatHistory> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId.toString())
                   .orderByDesc("create_time")
                   .last("LIMIT " + maxMessages);

            List<AIChatHistory> histories = chatHistoryMapper.selectList(wrapper);

            // MySQL是倒序的，需要反转
            List<ChatMessage> messages = new ArrayList<>();
            for (int i = histories.size() - 1; i >= 0; i--) {
                AIChatHistory history = histories.get(i);
                ChatMessage msg = deserializeFromHistory(history);
                if (msg != null) {
                    messages.add(msg);
                }
            }

            return messages;
        } catch (Exception e) {
            log.error("从MySQL加载消息失败，userId={}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 异步保存到MySQL
     */
    @Async
    protected void asyncSaveToMySQL(ChatMessage message) {
        try {
            log.info("==================== 💾 [ChatMemory] 异步数据库写入开始 ====================");
            log.info("📝 [RedisBackedChatMemory] ChatMemory自动保存到MySQL");
            log.info("📝 [RedisBackedChatMemory] userId: {}", userId);
            log.info("📝 [RedisBackedChatMemory] 消息类型: {}", message.getClass().getSimpleName());
            log.info("📝 [RedisBackedChatMemory] 发送者类型: {}", getSenderType(message));

            String content = extractText(message);
            log.info("📝 [RedisBackedChatMemory] 内容长度: {} 字符", content != null ? content.length() : 0);
            log.info("📝 [RedisBackedChatMemory] 完整内容:");
            log.info("─ 开始 ({} 字符) ─", content != null ? content.length() : 0);
            log.info(content != null ? content : "null");
            log.info("─ 结束 ─");
            log.info("📝 [RedisBackedChatMemory] 当前线程: {}", Thread.currentThread().getName());
            log.info("📝 [RedisBackedChatMemory] 调用栈:");
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (int i = 1; i < Math.min(6, stackTrace.length); i++) {
                log.info("   at {}", stackTrace[i]);
            }
            log.info("=====================================================");

            AIChatHistory history = new AIChatHistory();
            history.setUserId(userId.toString());
            history.setContent(content);
            history.setSender(getSenderType(message));
            history.setCreateTime(LocalDateTime.now());

            chatHistoryMapper.insert(history);

            log.info("✅ [RedisBackedChatMemory] ChatMemory自动保存成功! ID={}", history.getId());
            log.info("=====================================================");
        } catch (Exception e) {
            log.error("❌ [RedisBackedChatMemory] 异步保存消息到MySQL失败: userId={}", userId, e);
            log.error("❌ [RedisBackedChatMemory] 错误详情:", e);
            // 失败不影响主流程
        }
    }

    /**
     * 序列化ChatMessage为字符串
     * 格式: TYPE|content
     */
    private String serializeMessage(ChatMessage message) {
        if (message == null) {
            return "";
        }

        String type = getSenderType(message);
        String content = extractText(message);

        return type + "|" + content;
    }

    /**
     * 从字符串反序列化ChatMessage
     */
    private ChatMessage deserializeMessage(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }

        try {
            String[] parts = data.split("\\|", 2);
            if (parts.length != 2) {
                return null;
            }

            String type = parts[0];
            String content = parts[1];

            if ("user".equalsIgnoreCase(type)) {
                return new UserMessage(content);
            } else if ("ai".equalsIgnoreCase(type)) {
                return new AiMessage(content);
            }

            return null;
        } catch (Exception e) {
            log.error("反序列化消息失败: {}", data, e);
            return null;
        }
    }

    /**
     * 从AIChatHistory实体转换为ChatMessage
     */
    private ChatMessage deserializeFromHistory(AIChatHistory history) {
        if (history == null) {
            return null;
        }

        String content = history.getContent();
        String sender = history.getSender();

        if ("user".equalsIgnoreCase(sender)) {
            return new UserMessage(content);
        } else if ("ai".equalsIgnoreCase(sender)) {
            return new AiMessage(content);
        }

        return null;
    }

    /**
     * 从ChatMessage提取文本
     */
    private String extractText(ChatMessage message) {
        if (message == null) {
            return "";
        }

        try {
            if (message instanceof UserMessage) {
                return ((UserMessage) message).singleText();
            } else if (message instanceof AiMessage) {
                return ((AiMessage) message).text();
            } else {
                return message.toString();
            }
        } catch (Exception e) {
            log.warn("提取ChatMessage文本失败", e);
            return message.toString();
        }
    }

    /**
     * 获取发送者类型
     */
    private String getSenderType(ChatMessage message) {
        if (message instanceof UserMessage) {
            return "user";
        } else if (message instanceof AiMessage) {
            return "ai";
        } else {
            return "ai"; // 默认AI
        }
    }
}
