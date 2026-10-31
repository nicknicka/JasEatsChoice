package com.xx.jaseatschoicejava.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

import org.springframework.stereotype.Service;

import com.xx.jaseatschoicejava.agent.config.ChatModelProvider;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiApiKeyResolver;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import com.xx.jaseatschoicejava.dto.AiConnectivityResult;
import com.xx.jaseatschoicejava.dto.AiRuntimeConfigSnapshot;
import com.xx.jaseatschoicejava.service.AiRuntimeConfigService;

import dev.langchain4j.model.chat.ChatModel;

/**
 * 汇总启动配置中的 AI 运行时信息。当前切片是只读的，不持久化或热切换配置。
 */
@Service
public class AiRuntimeConfigServiceImpl implements AiRuntimeConfigService {

    private static final List<String> ZHIPU_KNOWN_MODELS = List.of(
        "glm-4-flash",
        "glm-4.6v-flash",
        "glm-4.1v-thinking-flash"
    );

    private final AIConfig aiConfig;
    private final ZhipuAIConfig zhipuConfig;
    private final ChatModelProvider chatModelProvider;
    private final String loadedAt;

    public AiRuntimeConfigServiceImpl(
            AIConfig aiConfig,
            ZhipuAIConfig zhipuConfig,
            ChatModelProvider chatModelProvider) {
        this.aiConfig = aiConfig;
        this.zhipuConfig = zhipuConfig;
        this.chatModelProvider = chatModelProvider;
        this.loadedAt = LocalDateTime.now().withNano(0).toString();
    }

    @Override
    public AiRuntimeConfigSnapshot getCurrentSnapshot() {
        String model = effectiveModel();
        String visionModel = hasText(zhipuConfig.getVisionModel())
            ? zhipuConfig.getVisionModel()
            : aiConfig.getVisionModel();
        String baseUrl = hasText(zhipuConfig.getBaseUrl()) ? zhipuConfig.getBaseUrl() : aiConfig.getBaseUrl();
        Integer timeout = zhipuConfig.getTimeout() != null ? zhipuConfig.getTimeout() : aiConfig.getTimeout();
        String apiKey = AiApiKeyResolver.resolve(zhipuConfig, aiConfig);

        List<String> supportedModels = new ArrayList<>(ZHIPU_KNOWN_MODELS);
        for (String currentModel : List.of(model, aiConfig.getSupervisorModel(), visionModel)) {
            if (hasText(currentModel) && !supportedModels.contains(currentModel)) {
                supportedModels.add(currentModel);
            }
        }

        return AiRuntimeConfigSnapshot.builder()
            .provider(aiConfig.getProvider())
            .model(model)
            .supervisorModel(aiConfig.getSupervisorModel())
            .visionModel(visionModel)
            .baseUrl(baseUrl == null ? "" : baseUrl)
            .timeoutMs(timeout != null && timeout > 0 ? timeout : 30000)
            .temperature(aiConfig.getTemperature())
            .updatedAt(loadedAt)
            .keyStatus(keyStatus(apiKey))
            .capabilities(chatModelProvider.getCapabilities())
            .supportedModels(supportedModels)
            .providers(List.of(new AiRuntimeConfigSnapshot.AiProviderOption(
                "zhipu", "智谱", ZHIPU_KNOWN_MODELS)))
            .build();
    }

    private String effectiveModel() {
        return hasText(zhipuConfig.getModel()) ? zhipuConfig.getModel() : aiConfig.getModel();
    }

    @Override
    public AiConnectivityResult testCurrentConnectivity() {
        String apiKey = AiApiKeyResolver.resolve(zhipuConfig, aiConfig);
        if (!hasText(apiKey)) {
            return failure(effectiveModel(), "AI key 未配置");
        }

        try {
            ChatModel chatModel = chatModelProvider.buildAiModel();
            chatModel.chat("ping");
            return new AiConnectivityResult(true, "连接成功", testedAt(), effectiveModel());
        } catch (Exception exception) {
            return failure(effectiveModel(), safeMessage(exception, apiKey));
        }
    }

    private AiConnectivityResult failure(String model, String message) {
        return new AiConnectivityResult(false, message, testedAt(), model);
    }

    private AiRuntimeConfigSnapshot.AiKeyStatus keyStatus(String apiKey) {
        if (!hasText(apiKey)) {
            return new AiRuntimeConfigSnapshot.AiKeyStatus(false, null, null);
        }
        String key = apiKey.trim();
        return new AiRuntimeConfigSnapshot.AiKeyStatus(
            true,
            shortSha256(key),
            key.length() >= 8 ? key.substring(key.length() - 4) : null
        );
    }

    private String shortSha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 6);
        } catch (Exception exception) {
            // SHA-256 is guaranteed by the JVM; this fallback avoids leaking the key.
            return "unavailable";
        }
    }

    private String safeMessage(Exception exception, String apiKey) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return "连接失败";
        }
        return hasText(apiKey) ? message.replace(apiKey, "***") : message;
    }

    private String testedAt() {
        return LocalDateTime.now().withNano(0).toString();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
