package com.xx.jaseatschoicejava.agent.config;

import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiApiKeyResolver;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import com.xx.jaseatschoicejava.service.impl.AiModelClientFactory;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 智谱 AI 模型装配。运行时客户端由动态代理按当前激活快照解析。
 */
@Configuration
@ConditionalOnProperty(prefix = "ai", name = "provider", havingValue = "zhipu", matchIfMissing = true)
@EnableConfigurationProperties({ AIConfig.class, ZhipuAIConfig.class })
public class ZhipuChatModelFactory {

    @Bean
    public ChatModelProvider chatModelProvider(
            @Autowired DynamicAiModelRegistry registry,
            @Autowired AiModelClientFactory modelFactory,
            @Autowired AIConfig aiConfig,
            @Autowired ZhipuAIConfig zhipuConfig) {
        return new ChatModelProvider() {
            @Override
            public boolean isAvailable() {
                return AiApiKeyResolver.resolve(zhipuConfig, aiConfig) != null
                    && !AiApiKeyResolver.resolve(zhipuConfig, aiConfig).isBlank();
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
                return modelFactory.capabilities(registry.current().config());
            }
        };
    }
}
