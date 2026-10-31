package com.xx.jaseatschoicejava.service.impl;

import java.time.Duration;
import java.util.List;

import org.springframework.stereotype.Component;

import com.xx.jaseatschoicejava.agent.config.NoopChatModel;
import com.xx.jaseatschoicejava.agent.config.NoopStreamingChatModel;
import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiModelConfig;
import com.xx.jaseatschoicejava.config.AiRuntimeKeyStore;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

/**
 * 通用 OpenAI Chat Completions 兼容适配器。
 *
 * 用于 Trae/Sub2API 等本地兼容代理；base URL 由管理员配置，不绑定具体厂商。
 */
@Component
public class OpenAiCompatibleModelClientFactory implements AiModelClientFactory {

    public static final String PROVIDER_CODE = "openai-compatible";

    private static final List<String> KNOWN_MODELS = List.of(
        "gpt-4o-mini",
        "gpt-4o",
        "glm-5.3-solo",
        "deepseek-chat"
    );

    private final AIConfig aiConfig;
    private final AiRuntimeKeyStore runtimeKeyStore;

    public OpenAiCompatibleModelClientFactory(
            AIConfig aiConfig,
            AiRuntimeKeyStore runtimeKeyStore) {
        this.aiConfig = aiConfig;
        this.runtimeKeyStore = runtimeKeyStore;
    }

    @Override
    public String providerCode() {
        return PROVIDER_CODE;
    }

    @Override
    public String providerDisplayName() {
        return "OpenAI 兼容";
    }

    @Override
    public List<String> knownModels() {
        return KNOWN_MODELS;
    }

    @Override
    public AiModelConfig environmentConfig(AIConfig genericConfig) {
        String model = firstText(genericConfig.getModel(), "gpt-4o-mini");
        return AiModelConfig.builder()
            .provider(PROVIDER_CODE)
            .model(model)
            .supervisorModel(firstText(genericConfig.getSupervisorModel(), model))
            .visionModel(firstText(genericConfig.getVisionModel(), model))
            .baseUrl(firstText(genericConfig.getBaseUrl(), ""))
            .timeoutMs(genericConfig.getTimeout() != null && genericConfig.getTimeout() > 0
                ? genericConfig.getTimeout() : 30000)
            .temperature(genericConfig.getTemperature() != null ? genericConfig.getTemperature() : 0.7)
            .build();
    }

    @Override
    public String resolveKey(AiRuntimeKeyStore runtimeKeyStore) {
        String runtimeKey = runtimeKeyStore == null ? "" : runtimeKeyStore.readKey();
        return firstText(runtimeKey, firstText(aiConfig.getApiKey(), ""));
    }

    @Override
    public ChatModel buildChatModel(AiModelConfig config) {
        return buildChatModel(config, null);
    }

    @Override
    public ChatModel buildChatModel(AiModelConfig config, String candidateApiKey) {
        String key = key(candidateApiKey);
        if (!hasKey(key) || !hasText(config.getBaseUrl())) {
            return new NoopChatModel();
        }
        return OpenAiChatModel.builder()
            .apiKey(key)
            .baseUrl(config.getBaseUrl())
            .modelName(config.getModel())
            .temperature(config.getTemperature())
            .timeout(Duration.ofMillis(config.getTimeoutMs()))
            .maxRetries(2)
            .build();
    }

    @Override
    public ChatModel buildSupervisorModel(AiModelConfig config) {
        return buildSupervisorModel(config, null);
    }

    @Override
    public ChatModel buildSupervisorModel(AiModelConfig config, String candidateApiKey) {
        String key = key(candidateApiKey);
        if (!hasKey(key) || !hasText(config.getBaseUrl())) {
            return new NoopChatModel();
        }
        return OpenAiChatModel.builder()
            .apiKey(key)
            .baseUrl(config.getBaseUrl())
            .modelName(config.getSupervisorModel())
            .temperature(0.3)
            .maxTokens(4096)
            .timeout(Duration.ofMillis(config.getTimeoutMs()))
            .maxRetries(1)
            .build();
    }

    @Override
    public ChatModel buildVisionModel(AiModelConfig config) {
        return buildVisionModel(config, null);
    }

    @Override
    public ChatModel buildVisionModel(AiModelConfig config, String candidateApiKey) {
        String key = key(candidateApiKey);
        if (!hasKey(key) || !hasText(config.getBaseUrl())) {
            return new NoopChatModel();
        }
        return OpenAiChatModel.builder()
            .apiKey(key)
            .baseUrl(config.getBaseUrl())
            .modelName(config.getVisionModel())
            .temperature(0.3)
            .maxTokens(2048)
            .timeout(Duration.ofMillis(config.getTimeoutMs()))
            .maxRetries(1)
            .build();
    }

    @Override
    public StreamingChatModel buildStreamingModel(AiModelConfig config) {
        String key = key(null);
        if (!hasKey(key) || !hasText(config.getBaseUrl())) {
            return new NoopStreamingChatModel();
        }
        return OpenAiStreamingChatModel.builder()
            .apiKey(key)
            .baseUrl(config.getBaseUrl())
            .modelName(config.getModel())
            .temperature(config.getTemperature())
            .timeout(Duration.ofMillis(config.getTimeoutMs()))
            .build();
    }

    @Override
    public AiCapabilitiesDTO capabilities(AiModelConfig config) {
        boolean available = hasKey(key(null)) && hasText(config.getBaseUrl());
        boolean vision = available && hasText(config.getVisionModel());
        return AiCapabilitiesDTO.builder()
            .available(available)
            .provider(config.getProvider())
            .model(config.getModel())
            .visionModel(config.getVisionModel())
            .textChat(available)
            .streaming(available)
            .vision(vision)
            .functionCalling(available)
            .agentic(available)
            .unavailableMessage(available ? "" : "AI 未配置：请设置 AI_API_KEY 和 AI_BASE_URL")
            .build();
    }

    private String key(String candidateApiKey) {
        return hasText(candidateApiKey) ? candidateApiKey.trim() : resolveKey(runtimeKeyStore);
    }

    private boolean hasKey(String key) {
        return hasText(key);
    }

    private String firstText(String value, String defaultValue) {
        return hasText(value) ? value.trim() : defaultValue;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
