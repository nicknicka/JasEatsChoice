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
        // 1. 添加到本地缓存
        localMessages.add(message);

        // 2. 序列化并写入Redis
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

        // 5. 异步写入MySQL
        asyncSaveToMySQL(message);

        log.debug("用户 {} 添加消息到ChatMemory, 当前消息数: {}",
            userId, localMessages.size());
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
            AIChatHistory history = new AIChatHistory();
            history.setUserId(userId.toString());
            history.setContent(extractText(message));
            history.setSender(getSenderType(message));
            history.setCreateTime(LocalDateTime.now());

            chatHistoryMapper.insert(history);

            log.debug("异步保存消息到MySQL成功，userId={}", userId);
        } catch (Exception e) {
            log.error("异步保存消息到MySQL失败，userId={}", userId, e);
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
