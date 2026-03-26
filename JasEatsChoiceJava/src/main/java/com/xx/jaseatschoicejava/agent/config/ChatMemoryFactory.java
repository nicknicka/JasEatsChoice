package com.xx.jaseatschoicejava.agent.config;

import com.xx.jaseatschoicejava.agent.memory.RedisBackedChatMemory;
import com.xx.jaseatschoicejava.mapper.AIChatHistoryMapper;
import dev.langchain4j.memory.ChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Duration;

/**
 * ChatMemory 工厂类
 *
 * 提供Redis + MySQL混合存储的ChatMemory实例
 *
 * @author Claude
 * @since 2026-03-26
 */
@Slf4j
@Component
public class ChatMemoryFactory {

    @Value("${chat.memory.ttl-hours:2}")
    private int ttlHours;

    @Value("${chat.memory.max-messages:20}")
    private int maxMessages;

    private final RedisTemplate<String, String> redisTemplate;
    private final AIChatHistoryMapper chatHistoryMapper;

    public ChatMemoryFactory(
            RedisTemplate<String, String> redisTemplate,
            AIChatHistoryMapper chatHistoryMapper) {
        this.redisTemplate = redisTemplate;
        this.chatHistoryMapper = chatHistoryMapper;
    }

    /**
     * 初始化后回调（在@Value注入完成后执行）
     */
    @PostConstruct
    public void init() {
        log.info("初始化ChatMemoryFactory，TTL={}小时，maxMessages={}",
            ttlHours, maxMessages);
    }

    /**
     * 为指定用户创建ChatMemory
     *
     * @param userId 用户ID
     * @return ChatMemory实例
     */
    public ChatMemory createChatMemory(Long userId) {
        Duration ttl = Duration.ofHours(ttlHours);

        log.debug("为用户 {} 创建ChatMemory，TTL={}小时，maxMessages={}",
            userId, ttlHours, maxMessages);

        return new RedisBackedChatMemory(
            redisTemplate,
            chatHistoryMapper,
            userId,
            ttl,
            maxMessages
        );
    }

    /**
     * 为指定用户创建ChatMemory（String参数）
     *
     * @param userIdStr 用户ID字符串
     * @return ChatMemory实例
     */
    public ChatMemory createChatMemory(String userIdStr) {
        try {
            Long userId = Long.parseLong(userIdStr);
            return createChatMemory(userId);
        } catch (NumberFormatException e) {
            log.error("无效的userId: {}", userIdStr, e);
            // 返回一个默认的内存ChatMemory
            return dev.langchain4j.memory.chat.MessageWindowChatMemory
                .withMaxMessages(maxMessages);
        }
    }
}
