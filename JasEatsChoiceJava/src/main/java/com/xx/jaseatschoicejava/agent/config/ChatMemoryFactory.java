package com.xx.jaseatschoicejava.agent.config;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * ChatMemory 工厂类
 *
 * 使用LangChain4j默认的内存ChatMemory
 * 所有持久化由前端统一负责
 *
 * @author Claude
 * @since 2026-03-26
 */
@Slf4j
@Component
public class ChatMemoryFactory {

    @Value("${chat.memory.max-messages:20}")
    private int maxMessages;

    /**
     * 初始化后回调
     */
    @PostConstruct
    public void init() {
        log.info("初始化ChatMemoryFactory，使用默认内存ChatMemory，maxMessages={}", maxMessages);
    }

    /**
     * 为指定用户创建ChatMemory
     *
     * 使用LangChain4j默认的MessageWindowChatMemory
     * - 只保存在内存中
     * - 不自动保存到数据库
     * - 所有持久化由前端统一负责
     *
     * @param userId 用户ID
     * @return ChatMemory实例
     */
    public ChatMemory createChatMemory(Long userId) {
        log.debug("为用户 {} 创建默认内存ChatMemory，maxMessages={}", userId, maxMessages);

        // 使用LangChain4j默认的MessageWindowChatMemory
        return MessageWindowChatMemory.builder()
                .maxMessages(maxMessages)
                .id(userId.toString())  // 使用userId作为memoryId
                .build();
    }

    /**
     * 为指定用户创建ChatMemory（String参数）
     *
     * @param userIdStr 用户ID字符串
     * @return ChatMemory实例
     */
    public ChatMemory createChatMemory(String userIdStr) {
        log.debug("为用户 {} 创建默认内存ChatMemory，maxMessages={}", userIdStr, maxMessages);

        // 使用LangChain4j默认的MessageWindowChatMemory
        return MessageWindowChatMemory.builder()
                .maxMessages(maxMessages)
                .id(userIdStr)  // 使用userId作为memoryId
                .build();
    }
}
