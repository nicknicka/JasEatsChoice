package com.xx.jaseatschoicejava.agent.config;

import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiModelConfig;
import com.xx.jaseatschoicejava.service.impl.AiModelClientFactory;
import com.xx.jaseatschoicejava.service.impl.AiProviderRegistry;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 模型装配。运行时客户端由动态代理按当前激活快照解析。
 *
 * 能力与可用性按当前激活快照的 provider 从 {@link AiProviderRegistry} 解析，
 * 因此新增供应商适配器（如 openai-compatible）时这里无需改动。
 */
@Configuration
@EnableConfigurationProperties({ AIConfig.class })
public class AiChatModelFactory {

    @Bean
    public ChatModelProvider chatModelProvider(
            @Autowired DynamicAiModelRegistry registry,
            @Autowired AiProviderRegistry providerRegistry,
            @Autowired AIConfig aiConfig) {
        return new ChatModelProvider() {
            @Override
            public boolean isAvailable() {
                return activeAdapter().capabilities(activeConfig()).isAvailable();
            }

            @Override
            public ChatModel buildAiModel() {
                return registry.chatModel();
            }

            @Override
            public ChatModel buildSupervisorModel() {
                return registry.supervisorModel();
            }

            @Override
            public ChatModel buildVisionModel() {
                return registry.visionModel();
            }

            @Override
            public StreamingChatModel buildStreamingModel() {
                return registry.streamingModel();
            }

            @Override
            public AiCapabilitiesDTO getCapabilities() {
                return activeAdapter().capabilities(activeConfig());
            }

            private AiModelClientFactory activeAdapter() {
                return providerRegistry.required(activeConfig().getProvider());
            }

            private AiModelConfig activeConfig() {
                AiModelConfig config = registry.current().config();
                if (config != null) {
                    return config;
                }
                return providerRegistry.required(aiConfig.getProvider()).environmentConfig(aiConfig);
            }
        };
    }
}
