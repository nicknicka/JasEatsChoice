package com.xx.jaseatschoicejava.agent.config;

import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiApiKeyResolver;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import dev.langchain4j.community.model.zhipu.ZhipuAiChatModel;
import dev.langchain4j.community.model.zhipu.ZhipuAiStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;
import java.time.Duration;

/**
 * 智谱 AI 模型工厂（条件装配）
 *
 * 仅当 ai.provider = zhipu 时激活。
 * 不直接声明 aiModel/supervisorModel/visionModel 这些业务 Bean，
 * 而是产出一个 {@link ChatModelProvider}，由 {@link LangChain4jConfig}
 * 统一消费并做降级（Noop）包装。
 *
 * 兼容策略：
 * - apiKey 优先级：ZhipuAIConfig.apiKey > AIConfig.apiKey
 * - model 优先级：ZhipuAIConfig.model > AIConfig.model
 * - 保证老配置 zhipuai.* 仍然可用。
 *
 * @since 2026-10-27
 */
@Configuration
@ConditionalOnProperty(prefix = "ai", name = "provider", havingValue = "zhipu", matchIfMissing = true)
@EnableConfigurationProperties({ AIConfig.class, ZhipuAIConfig.class })
public class ZhipuChatModelFactory {

    private static final Logger log = LoggerFactory.getLogger(ZhipuChatModelFactory.class);

    @Resource
    private AIConfig aiConfig;

    @Resource
    private ZhipuAIConfig zhipuAIConfig;

    /**
     * 生效的 API Key：zhipuai.api-key 优先，其次 ai.api-key
     */
    private String effectiveApiKey() {
        return AiApiKeyResolver.resolve(zhipuAIConfig, aiConfig);
    }

    /**
     * 生效的文本模型名：zhipuai.model 优先，其次 ai.model
     */
    private String effectiveModel() {
        String zm = zhipuAIConfig.getModel();
        if (zm != null && !zm.isBlank()) {
            return zm;
        }
        return aiConfig.getModel();
    }

    /**
     * 生效的视觉模型名
     */
    private String effectiveVisionModel() {
        String zv = zhipuAIConfig.getVisionModel();
        if (zv != null && !zv.isBlank()) {
            return zv;
        }
        return aiConfig.getVisionModel();
    }

    private Duration timeout() {
        Integer t = zhipuAIConfig.getTimeout();
        if (t == null) {
            t = aiConfig.getTimeout();
        }
        if (t == null || t <= 0) {
            t = 30000;
        }
        return Duration.ofMillis(t);
    }

    @Bean
    public ChatModelProvider chatModelProvider() {
        String apiKey = effectiveApiKey();
        String model = effectiveModel();
        String visionModel = effectiveVisionModel();
        Duration to = timeout();
        boolean hasKey = apiKey != null && !apiKey.isBlank();
        boolean hasVisionModel = visionModel != null && !visionModel.isBlank();

        log.info("AI 供应商：智谱 (zhipu)，模型：{}，视觉模型：{}，key 已配置：{}",
            model, visionModel, hasKey);

        return new ChatModelProvider() {
            @Override
            public boolean isAvailable() {
                return hasKey;
            }

            @Override
            public ChatModel buildAiModel() {
                return ZhipuAiChatModel.builder()
                    .apiKey(apiKey)
                    .model(model)
                    .temperature(aiConfig.getTemperature() != null ? aiConfig.getTemperature() : 0.7)
                    .maxRetries(2)
                    .connectTimeout(to)
                    .readTimeout(to)
                    .build();
            }

            @Override
            public ChatModel buildSupervisorModel() {
                // Supervisor 走同一个模型，温度更低、输出更稳
                return ZhipuAiChatModel.builder()
                    .apiKey(apiKey)
                    .model(model)
                    .temperature(0.3)
                    .maxToken(4096)
                    .maxRetries(1)
                    .build();
            }

            @Override
            public ChatModel buildVisionModel() {
                return ZhipuAiChatModel.builder()
                    .apiKey(apiKey)
                    .model(visionModel)
                    .temperature(0.3)
                    .maxToken(2048)
                    .maxRetries(1)
                    .build();
            }

            @Override
            public StreamingChatModel buildStreamingModel() {
                return ZhipuAiStreamingChatModel.builder()
                    .apiKey(apiKey)
                    .model(model)
                    .temperature(aiConfig.getTemperature() != null ? aiConfig.getTemperature() : 0.7)
                    .build();
            }

            @Override
            public AiCapabilitiesDTO getCapabilities() {
                boolean visionAvailable = hasKey && hasVisionModel;
                return AiCapabilitiesDTO.builder()
                    .available(hasKey)
                    .provider("zhipu")
                    .model(model)
                    .visionModel(visionModel)
                    .textChat(hasKey)
                    .streaming(hasKey)
                    .vision(visionAvailable)
                    .functionCalling(hasKey)
                    .agentic(hasKey)
                    .unavailableMessage(hasKey ? "" :
                        "AI 未配置：请设置 AI_API_KEY（或 ZHIPU_API_KEY）后重启服务")
                    .build();
            }
        };
    }
}
