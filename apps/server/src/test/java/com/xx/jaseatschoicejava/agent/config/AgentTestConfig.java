package com.xx.jaseatschoicejava.agent.config;

import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.xx.jaseatschoicejava.config.AIConfig;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

/**
 * Agent 测试配置：只装配唯一的 OpenAI-compatible 适配器。
 */
@Configuration
@EnableConfigurationProperties(AIConfig.class)
public class AgentTestConfig {

    @Bean
    public ChatModel testChatModel(AIConfig aiConfig) {
        if (isBlank(aiConfig.getApiKey()) || isBlank(aiConfig.getBaseUrl())) {
            // 无凭据时使用占位实现，避免测试上下文整体启动失败。
            return new NoopChatModel();
        }
        return OpenAiChatModel.builder()
                .apiKey(aiConfig.getApiKey())
                .baseUrl(aiConfig.getBaseUrl())
                .modelName(aiConfig.getModel())
                .temperature(0.7)
                .timeout(Duration.ofSeconds(60))
                .maxRetries(2)
                .build();
    }

    @Bean
    public ChatMemory testChatMemory() {
        return MessageWindowChatMemory.withMaxMessages(20);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
