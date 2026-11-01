package com.xx.jaseatschoicejava.service.impl;

import java.time.Duration;
import java.util.List;

import org.springframework.stereotype.Component;

import com.xx.jaseatschoicejava.agent.config.NoopChatModel;
import com.xx.jaseatschoicejava.agent.config.NoopStreamingChatModel;
import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;
import com.xx.jaseatschoicejava.config.AiApiKeyResolver;
import com.xx.jaseatschoicejava.config.AiModelConfig;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiRuntimeKeyStore;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;

import dev.langchain4j.community.model.zhipu.ZhipuAiChatModel;
import dev.langchain4j.community.model.zhipu.ZhipuAiStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * 智谱模型客户端适配器。
 */
@Component
public class ZhipuAiModelClientFactory implements AiModelClientFactory {

    private static final List<String> KNOWN_MODELS = List.of(
        "glm-4-flash",
        "glm-4.6v-flash",
        "glm-4.1v-thinking-flash"
    );

    private final AIConfig aiConfig;
    private final ZhipuAIConfig zhipuConfig;
    private final AiRuntimeKeyStore runtimeKeyStore;

    public ZhipuAiModelClientFactory(
            AIConfig aiConfig,
            ZhipuAIConfig zhipuConfig,
            AiRuntimeKeyStore runtimeKeyStore) {
        this.aiConfig = aiConfig;
        this.zhipuConfig = zhipuConfig;
        this.runtimeKeyStore = runtimeKeyStore;
    }

    @Override
    public String providerCode() {
        return "zhipu";
    }

    @Override
    public String providerDisplayName() {
        return "智谱";
    }

    @Override
    public List<String> knownModels() {
        return KNOWN_MODELS;
    }

    @Override
    public AiModelConfig environmentConfig(AIConfig genericConfig) {
        return AiModelConfig.builder()
            .provider(providerCode())
            .model(hasText(zhipuConfig.getModel())
                ? zhipuConfig.getModel()
                : (hasText(genericConfig.getModel()) ? genericConfig.getModel() : "glm-4-flash"))
            .supervisorModel(hasText(genericConfig.getSupervisorModel())
                ? genericConfig.getSupervisorModel() : "glm-4-flash")
            .visionModel(hasText(zhipuConfig.getVisionModel())
                ? zhipuConfig.getVisionModel()
                : (hasText(genericConfig.getVisionModel()) ? genericConfig.getVisionModel() : "glm-4.1v-thinking-flash"))
            .baseUrl(hasText(zhipuConfig.getBaseUrl())
                ? zhipuConfig.getBaseUrl()
                : (genericConfig.getBaseUrl() == null ? "" : genericConfig.getBaseUrl()))
            .timeoutMs(timeout(zhipuConfig.getTimeout() != null
                ? zhipuConfig.getTimeout() : genericConfig.getTimeout()))
            .temperature(genericConfig.getTemperature() != null ? genericConfig.getTemperature() : 0.7)
            .build();
    }

    @Override
    public String resolveKey(AiRuntimeKeyStore runtimeKeyStore) {
        return AiApiKeyResolver.resolve(zhipuConfig, aiConfig, runtimeKeyStore);
    }

    @Override
    public ChatModel buildChatModel(AiModelConfig config) {
        return buildChatModel(config, null);
    }

    @Override
    public ChatModel buildChatModel(AiModelConfig config, String candidateApiKey) {
        String key = key(candidateApiKey);
        if (!hasText(key)) {
            return new NoopChatModel();
        }
        var builder = ZhipuAiChatModel.builder()
            .apiKey(key)
            .model(config.getModel())
            .temperature(config.getTemperature())
            .maxRetries(2)
            .connectTimeout(Duration.ofMillis(config.getTimeoutMs()))
            .readTimeout(Duration.ofMillis(config.getTimeoutMs()));
        if (hasText(config.getBaseUrl())) {
            builder.baseUrl(config.getBaseUrl());
        }
        return builder.build();
    }

    @Override
    public ChatModel buildSupervisorModel(AiModelConfig config) {
        return buildSupervisorModel(config, null);
    }

    @Override
    public ChatModel buildSupervisorModel(AiModelConfig config, String candidateApiKey) {
        String key = key(candidateApiKey);
        if (!hasText(key)) {
            return new NoopChatModel();
        }
        var builder = ZhipuAiChatModel.builder()
            .apiKey(key)
            .model(config.getSupervisorModel())
            .temperature(0.3)
            .maxToken(4096)
            .maxRetries(1)
            .connectTimeout(Duration.ofMillis(config.getTimeoutMs()))
            .readTimeout(Duration.ofMillis(config.getTimeoutMs()));
        if (hasText(config.getBaseUrl())) {
            builder.baseUrl(config.getBaseUrl());
        }
        return builder.build();
    }

    @Override
    public ChatModel buildVisionModel(AiModelConfig config) {
        return buildVisionModel(config, null);
    }

    @Override
    public ChatModel buildVisionModel(AiModelConfig config, String candidateApiKey) {
        String key = key(candidateApiKey);
        if (!hasText(key)) {
            return new NoopChatModel();
        }
        var builder = ZhipuAiChatModel.builder()
            .apiKey(key)
            .model(config.getVisionModel())
            .temperature(0.3)
            .maxToken(2048)
            .maxRetries(1)
            .connectTimeout(Duration.ofMillis(config.getTimeoutMs()))
            .readTimeout(Duration.ofMillis(config.getTimeoutMs()));
        if (hasText(config.getBaseUrl())) {
            builder.baseUrl(config.getBaseUrl());
        }
        return builder.build();
    }

    @Override
    public StreamingChatModel buildStreamingModel(AiModelConfig config) {
        return buildStreamingModel(config, null);
    }

    @Override
    public StreamingChatModel buildStreamingModel(AiModelConfig config, String candidateApiKey) {
        String apiKey = key(candidateApiKey);
        if (!hasText(apiKey)) {
            return new NoopStreamingChatModel();
        }
        var builder = ZhipuAiStreamingChatModel.builder()
            .apiKey(apiKey)
            .model(config.getModel())
            .temperature(config.getTemperature())
            .connectTimeout(Duration.ofMillis(config.getTimeoutMs()))
            .readTimeout(Duration.ofMillis(config.getTimeoutMs()));
        if (hasText(config.getBaseUrl())) {
            builder.baseUrl(config.getBaseUrl());
        }
        return builder.build();
    }

    @Override
    public AiCapabilitiesDTO capabilities(AiModelConfig config) {
        boolean hasKey = hasKey();
        boolean visionAvailable = hasKey && hasText(config.getVisionModel());
        return AiCapabilitiesDTO.builder()
            .available(hasKey)
            .provider(config.getProvider())
            .model(config.getModel())
            .visionModel(config.getVisionModel())
            .textChat(hasKey)
            .streaming(hasKey)
            .vision(visionAvailable)
            .functionCalling(hasKey)
            .agentic(hasKey)
            .unavailableMessage(hasKey ? "" : "AI 未配置：请设置 AI_API_KEY 后重启服务")
            .build();
    }

    private String key() {
        return resolveKey(runtimeKeyStore);
    }

    private String key(String candidateApiKey) {
        return hasText(candidateApiKey) ? candidateApiKey.trim() : key();
    }

    private boolean hasKey() {
        return hasText(key());
    }

    private int timeout(Integer value) {
        return value != null && value > 0 ? value : 30000;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
