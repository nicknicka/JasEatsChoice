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
 * Agent测试配置类
 * 为测试提供所有Agent和工具的Bean
 *
 * @author Claude
 * @since 2026-03-24
 */
@Configuration
@EnableConfigurationProperties(ZhipuAIConfig.class)
public class AgentTestConfig {

    /**
     * 测试用ChatLanguageModel
     */
    @Bean
    public ChatLanguageModel testChatLanguageModel(ZhipuAIConfig zhipuAIConfig) {
        return ZhipuAiChatModel.builder()
                .apiKey(zhipuAIConfig.getApiKey())
                .model(zhipuAIConfig.getModel())
                .temperature(0.7)
                .maxRetries(2)
                .callTimeout(java.time.Duration.ofSeconds(60))
                .connectTimeout(java.time.Duration.ofSeconds(60))
                .writeTimeout(java.time.Duration.ofSeconds(60))
                .readTimeout(java.time.Duration.ofSeconds(60))
                .build();
    }

    /**
     * 测试用ChatMemory
     */
    @Bean
    public ChatMemory testChatMemory() {
        return MessageWindowChatMemory.withMaxMessages(20);
    }
}
