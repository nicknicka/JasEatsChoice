package com.xx.jaseatschoicejava.agent.config;

import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.zhipu.ZhipuAiChatModel;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j配置类
 * 配置ChatLanguageModel和ChatMemory等核心组件
 *
 * @author Claude
 * @since 2026-03-22
 */
@Configuration
@EnableConfigurationProperties(ZhipuAIConfig.class)
public class LangChain4jConfig {

    /**
     * 配置ChatLanguageModel（智谱AI）
     *
     * @param config 智谱AI配置
     * @return ChatLanguageModel实例
     */
    @Bean
    public ChatLanguageModel chatLanguageModel(ZhipuAIConfig config) {
        System.out.println("初始化ChatLanguageModel，模型：" + config.getModel());

        return ZhipuAiChatModel.builder()
                .apiKey(config.getApiKey())
                .model(config.getModel())
                .temperature(0.7)
                .maxRetries(3)
                .build();
    }

    /**
     * 配置ChatMemory（对话记忆）
     * 使用MessageWindowChatMemory保留最近20条消息
     *
     * @return ChatMemory实例
     */
    @Bean
    public ChatMemory chatMemory() {
        System.out.println("初始化ChatMemory，消息窗口大小：20");

        return MessageWindowChatMemory.withMaxMessages(20);
    }

    /**
     * 配置用于Agent的ChatMemory（更大的窗口）
     * Agent需要更长的上下文，保留最近50条消息
     *
     * @return ChatMemory实例
     */
    @Bean
    public ChatMemory agentChatMemory() {
        System.out.println("初始化Agent ChatMemory，消息窗口大小：50");

        return MessageWindowChatMemory.withMaxMessages(50);
    }
}
