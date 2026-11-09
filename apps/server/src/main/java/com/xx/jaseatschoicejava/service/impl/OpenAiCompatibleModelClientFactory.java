package com.xx.jaseatschoicejava.service.impl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.agent.config.NoopChatModel;
import com.xx.jaseatschoicejava.agent.config.NoopStreamingChatModel;
import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiModelConfig;
import com.xx.jaseatschoicejava.config.AiProviderCompatibility;
import com.xx.jaseatschoicejava.config.AiRuntimeKeyStore;
import com.xx.jaseatschoicejava.dto.AiModelCandidateDTO;
import com.xx.jaseatschoicejava.dto.AiModelOption;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;

/**
 * 通用 OpenAI Chat Completions 兼容适配器。
 *
 * 任何兼容 OpenAI Chat Completions 的服务都可接入；base URL 由管理员配置，不绑定具体厂商。
 */
@Component
@Slf4j
public class OpenAiCompatibleModelClientFactory implements AiModelClientFactory {


    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final List<String> KNOWN_MODELS = List.of(
        "gpt-4o-mini",
        "gpt-4o"
    );

    /** 普通对话输出上限，避免无界输出。 */
    private static final int CHAT_MAX_TOKENS = 4096;

    /** 食谱优化任务使用独立参数，不与普通聊天混用。 */
    private static final double RECIPE_TEMPERATURE = 0.2;
    private static final int RECIPE_MAX_TOKENS = 4096;

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
        return AiProviderCompatibility.PROVIDER_CODE;
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
            .provider(AiProviderCompatibility.PROVIDER_CODE)
            .model(model)
            .supervisorModel(firstText(genericConfig.getSupervisorModel(), model))
            .visionModel(firstText(genericConfig.getVisionModel(), model))
            .baseUrl(firstText(genericConfig.getBaseUrl(), ""))
            .timeoutMs(genericConfig.getTimeout() != null && genericConfig.getTimeout() > 0
                ? genericConfig.getTimeout() : 30000)
            .temperature(genericConfig.getTemperature() != null ? genericConfig.getTemperature() : 0.7)
            // 环境配置默认不宣称已校验视觉；只有显式声明才放开，避免仅凭模型名开放识别入口。
            .visionVerified(genericConfig.isVisionVerified())
            .build();
    }

    @Override
    public List<AiModelOption> discoverModels(AiModelCandidateDTO candidate) {
        String baseUrl = firstText(candidate == null ? null : candidate.getBaseUrl(),
            firstText(aiConfig.getBaseUrl(), ""));
        String apiKey = key(candidate == null ? null : candidate.getApiKey());
        if (!hasText(baseUrl) || !hasKey(apiKey)) {
            return List.of();
        }
        int timeoutMs = candidate != null && candidate.getTimeoutMs() != null && candidate.getTimeoutMs() > 0
            ? candidate.getTimeoutMs()
            : (aiConfig.getTimeout() != null && aiConfig.getTimeout() > 0 ? aiConfig.getTimeout() : 30000);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(trimTrailingSlash(baseUrl) + "/models"))
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofMillis(timeoutMs))
                .GET()
                .build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                log.warn("获取模型列表失败：HTTP {}", response.statusCode());
                return List.of();
            }
            return parseModelOptions(response.body());
        } catch (Exception exception) {
            log.warn("获取模型列表失败：{}", exception.getMessage());
            return List.of();
        }
    }

    private List<AiModelOption> parseModelOptions(String body) throws Exception {
        JsonNode data = OBJECT_MAPPER.readTree(body).path("data");
        if (!data.isArray()) {
            return List.of();
        }
        List<AiModelOption> options = new ArrayList<>();
        for (JsonNode node : data) {
            String id = node.path("id").asText("");
            if (id.isBlank()) {
                continue;
            }
            Boolean supportsImages = node.hasNonNull("supports_images")
                ? node.get("supports_images").asBoolean() : null;
            options.add(AiModelOption.builder()
                .id(id)
                .name(node.path("name").asText(id))
                .supportsImages(supportsImages)
                .build());
        }
        return options;
    }

    private String trimTrailingSlash(String value) {
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
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
            .maxTokens(CHAT_MAX_TOKENS)
            .timeout(Duration.ofMillis(config.getTimeoutMs()))
            .maxRetries(2)
            .build();
    }

    @Override
    public ChatModel buildRecipeModel(AiModelConfig config) {
        return buildRecipeModel(config, null);
    }

    @Override
    public ChatModel buildRecipeModel(AiModelConfig config, String candidateApiKey) {
        String key = key(candidateApiKey);
        if (!hasKey(key) || !hasText(config.getBaseUrl())) {
            return new NoopChatModel();
        }
        return OpenAiChatModel.builder()
            .apiKey(key)
            .baseUrl(config.getBaseUrl())
            .modelName(config.getModel())
            .temperature(RECIPE_TEMPERATURE)
            .maxTokens(RECIPE_MAX_TOKENS)
            .timeout(Duration.ofMillis(recipeStreamTimeoutMs()))
            // 不重试：食谱优化单次生成本身就要 30~150s，超时重试只会把等待时间翻倍，
            // 且每次重试都重复计费。
            .maxRetries(0)
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
    public StreamingChatModel buildRecipeStreamingModel(AiModelConfig config) {
        return buildRecipeStreamingModel(config, null);
    }

    @Override
    public StreamingChatModel buildRecipeStreamingModel(AiModelConfig config, String candidateApiKey) {
        String key = key(candidateApiKey);
        if (!hasKey(key) || !hasText(config.getBaseUrl())) {
            return new NoopStreamingChatModel();
        }
        return OpenAiStreamingChatModel.builder()
            .apiKey(key)
            .baseUrl(config.getBaseUrl())
            .modelName(config.getModel())
            .temperature(RECIPE_TEMPERATURE)
            .maxTokens(RECIPE_MAX_TOKENS)
            .timeout(Duration.ofMillis(recipeStreamTimeoutMs()))
            .build();
    }

    @Override
    public AiCapabilitiesDTO capabilities(AiModelConfig config) {
        boolean available = hasKey(key(null)) && hasText(config.getBaseUrl())
            && hasText(config.getModel());
        boolean vision = available && hasText(config.getVisionModel()) && config.isVisionVerified();
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
            .dishRecognitionTimeoutMs(modelTimeoutMs(config))
            .recipeStreamTimeoutMs(recipeStreamTimeoutMs())
            .unavailableMessage(available ? "" : "AI 未配置：请设置 AI_API_KEY 和 AI_BASE_URL")
            .build();
    }

    private long recipeStreamTimeoutMs() {
        Integer configured = aiConfig.getRecipeStreamTimeoutMs();
        return configured != null && configured > 0 ? configured : 120000L;
    }

    private long modelTimeoutMs(AiModelConfig config) {
        if (config != null && config.getTimeoutMs() > 0) {
            return config.getTimeoutMs();
        }
        Integer configured = aiConfig == null ? null : aiConfig.getTimeout();
        return configured != null && configured > 0 ? configured : 30000L;
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
