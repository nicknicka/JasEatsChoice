package com.xx.jaseatschoicejava.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.agent.config.ChatModelProvider;
import com.xx.jaseatschoicejava.agent.config.DynamicAiModelRegistry;
import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;
import com.xx.jaseatschoicejava.config.AiApiKeyResolver;
import com.xx.jaseatschoicejava.config.AiRuntimeKeyStore;
import com.xx.jaseatschoicejava.config.AiModelConfig;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import com.xx.jaseatschoicejava.dto.AiConnectivityResult;
import com.xx.jaseatschoicejava.dto.AiModelCandidateDTO;
import com.xx.jaseatschoicejava.dto.AiRuntimeConfigSnapshot;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.service.AiRuntimeConfigService;
import com.xx.jaseatschoicejava.service.SystemConfigService;

import dev.langchain4j.model.chat.ChatModel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * AI 运行时配置服务：加载数据库配置、环境变量兜底、测试候选并原子激活。
 */
@Slf4j
@Service
public class AiRuntimeConfigServiceImpl implements AiRuntimeConfigService {

    private static final String CONFIG_KEY = "ai.runtime-config";
    private static final String CONFIG_GROUP = "ai";
    private static final List<String> ZHIPU_KNOWN_MODELS = List.of(
        "glm-4-flash",
        "glm-4.6v-flash",
        "glm-4.1v-thinking-flash"
    );

    private final AIConfig aiConfig;
    private final ZhipuAIConfig zhipuConfig;
    private final ChatModelProvider chatModelProvider;
    private final SystemConfigService systemConfigService;
    private final DynamicAiModelRegistry registry;
    private final AiModelClientFactory modelFactory;
    private final ObjectMapper objectMapper;
    private final AiRuntimeKeyStore runtimeKeyStore;
    private volatile long version = 0L;
    private volatile String updatedAt = LocalDateTime.now().withNano(0).toString();

    @Autowired
    public AiRuntimeConfigServiceImpl(
            AIConfig aiConfig,
            ZhipuAIConfig zhipuConfig,
            ChatModelProvider chatModelProvider,
            SystemConfigService systemConfigService,
            DynamicAiModelRegistry registry,
            AiModelClientFactory modelFactory,
            ObjectMapper objectMapper,
            AiRuntimeKeyStore runtimeKeyStore) {
        this.aiConfig = aiConfig;
        this.zhipuConfig = zhipuConfig;
        this.chatModelProvider = chatModelProvider;
        this.systemConfigService = systemConfigService;
        this.registry = registry;
        this.modelFactory = modelFactory;
        this.objectMapper = objectMapper;
        this.runtimeKeyStore = runtimeKeyStore;
    }

    public AiRuntimeConfigServiceImpl(
            AIConfig aiConfig,
            ZhipuAIConfig zhipuConfig,
            ChatModelProvider chatModelProvider) {
        this(aiConfig, zhipuConfig, chatModelProvider, null, new DynamicAiModelRegistry(),
            new ZhipuAiModelClientFactory(aiConfig, zhipuConfig, null), new ObjectMapper(), null);
    }

    public AiRuntimeConfigServiceImpl(
            AIConfig aiConfig,
            ZhipuAIConfig zhipuConfig,
            SystemConfigService systemConfigService,
            DynamicAiModelRegistry registry,
            AiModelClientFactory modelFactory,
            ObjectMapper objectMapper) {
        this(aiConfig, zhipuConfig, null, systemConfigService, registry,
            modelFactory, objectMapper, null);
    }

    @PostConstruct
    public synchronized void initialize() {
        AiModelConfig config = effectiveConfig(systemConfigService == null ? null
            : systemConfigService.getConfigsByGroup(CONFIG_GROUP));
        activate(config, ++version);
    }

    @Override
    public AiRuntimeConfigSnapshot getCurrentSnapshot() {
        AiModelConfig config = currentConfig();
        String model = effectiveModel();
        String supervisorModel = config != null && hasText(config.getSupervisorModel())
            ? config.getSupervisorModel() : aiConfig.getSupervisorModel();
        String visionModel = config != null && hasText(config.getVisionModel())
            ? config.getVisionModel() : aiConfig.getVisionModel();
        String baseUrl = config != null && config.getBaseUrl() != null
            ? config.getBaseUrl() : aiConfig.getBaseUrl();
        int timeout = config != null && config.getTimeoutMs() > 0
            ? config.getTimeoutMs() : timeout(aiConfig.getTimeout());
        double temperature = config != null ? config.getTemperature()
            : (aiConfig.getTemperature() != null ? aiConfig.getTemperature() : 0.7);
        String apiKey = resolveKey();

        List<String> supportedModels = new ArrayList<>(ZHIPU_KNOWN_MODELS);
        for (String currentModel : List.of(config != null ? config.getModel() : null, supervisorModel, visionModel)) {
            if (hasText(currentModel) && !supportedModels.contains(currentModel)) {
                supportedModels.add(currentModel);
            }
        }

        return AiRuntimeConfigSnapshot.builder()
            .provider("zhipu")
            .model(config != null && hasText(config.getModel()) ? config.getModel() : effectiveModel())
            .supervisorModel(supervisorModel)
            .visionModel(visionModel)
            .baseUrl(baseUrl == null ? "" : baseUrl)
            .timeoutMs(timeout)
            .temperature(temperature)
            .updatedAt(updatedAt)
            .version(version)
            .keyStatus(keyStatus(apiKey))
            .capabilities(capabilities(config))
            .supportedModels(supportedModels)
            .providers(List.of(new AiRuntimeConfigSnapshot.AiProviderOption(
                "zhipu", "智谱", ZHIPU_KNOWN_MODELS)))
            .build();
    }

    @Override
    public AiConnectivityResult testCurrentConnectivity() {
        String apiKey = resolveKey();
        if (!hasText(apiKey)) {
            return failure(currentConfig().getModel(), "AI key 未配置");
        }
        try {
            ChatModel chatModel = chatModelProvider != null
                ? chatModelProvider.buildAiModel()
                : modelFactory.buildChatModel(currentConfig());
            chatModel.chat("ping");
            return new AiConnectivityResult(true, "连接成功", testedAt(), currentConfig().getModel());
        } catch (Exception exception) {
            return failure(currentConfig().getModel(), safeMessage(exception, apiKey));
        }
    }

    @Override
    public AiConnectivityResult testCandidateConnectivity(AiModelCandidateDTO candidate) {
        AiModelConfig config;
        try {
            config = validatedConfig(candidate);
        } catch (BusinessException exception) {
            return failure(candidate == null ? null : candidate.getModel(), exception.getMessage());
        }
        String candidateKey = candidateKey(candidate);
        String apiKey = candidateKey != null ? candidateKey : resolveKey();
        if (!hasText(apiKey)) {
            return failure(config.getModel(), "AI key 未配置");
        }
        try {
            ChatModel candidateModel = candidateKey != null
                ? modelFactory.buildChatModel(config, candidateKey)
                : modelFactory.buildChatModel(config);
            candidateModel.chat("ping");
            return new AiConnectivityResult(true, "连接成功", testedAt(), config.getModel());
        } catch (Exception exception) {
            return failure(config.getModel(), safeMessage(exception, apiKey));
        }
    }

    @Override
    public AiRuntimeConfigSnapshot saveAndActivate(AiModelCandidateDTO candidate) {
        AiConnectivityResult testResult = testCandidateConnectivity(candidate);
        if (!testResult.isSuccess()) {
            throw new BusinessException("AI_CONFIG_TEST_FAILED", testResult.getMessage());
        }

        AiModelConfig config = validatedConfig(candidate);
        String candidateKey = candidateKey(candidate);
        String previousKey = resolveKey();
        long nextVersion = version + 1;
        DynamicAiModelRegistry.ActiveModels clients;
        try {
            if (candidateKey != null) {
                runtimeKeyStore.writeKey(candidateKey);
            }
            clients = new DynamicAiModelRegistry.ActiveModels(
                config,
                modelFactory.buildChatModel(config),
                modelFactory.buildSupervisorModel(config),
                modelFactory.buildVisionModel(config),
                modelFactory.buildStreamingModel(config),
                nextVersion);
        } catch (Exception exception) {
            rollbackRuntimeKey(candidateKey, previousKey);
            if (exception instanceof BusinessException businessException) {
                throw businessException;
            }
            throw new BusinessException("AI_KEY_WRITE_FAILED", "AI key 保存失败，当前 key 未变更");
        }

        String configValue = writeConfigValue(config);
        boolean saved;
        try {
            saved = systemConfigService.setConfigValue(
                CONFIG_KEY, configValue, "AI 模型运行时配置", CONFIG_GROUP);
        } catch (Exception exception) {
            rollbackRuntimeKey(candidateKey, previousKey);
            throw new BusinessException("AI_CONFIG_SAVE_FAILED", "AI 配置保存失败");
        }
        if (!saved) {
            rollbackRuntimeKey(candidateKey, previousKey);
            throw new BusinessException("AI_CONFIG_SAVE_FAILED", "AI 配置保存失败");
        }

        registry.activate(clients);
        version = nextVersion;
        updatedAt = LocalDateTime.now().withNano(0).toString();
        return getCurrentSnapshot();
    }

    private void activate(AiModelConfig config, long nextVersion) {
        DynamicAiModelRegistry.ActiveModels clients = new DynamicAiModelRegistry.ActiveModels(
            config,
            modelFactory.buildChatModel(config),
            modelFactory.buildSupervisorModel(config),
            modelFactory.buildVisionModel(config),
            modelFactory.buildStreamingModel(config),
            nextVersion);
        registry.activate(clients);
        version = nextVersion;
        updatedAt = LocalDateTime.now().withNano(0).toString();
        log.info("AI 运行时配置已激活：version={}, model={}, vision={}",
            nextVersion, config.getModel(), config.getVisionModel());
    }

    private AiModelConfig effectiveConfig(Map<String, String> databaseConfigs) {
        if (databaseConfigs == null || !hasText(databaseConfigs.get(CONFIG_KEY))) {
            return environmentConfig();
        }
        try {
            AiModelConfig dbConfig = objectMapper.readValue(
                databaseConfigs.get(CONFIG_KEY), AiModelConfig.class);
            return AiModelConfig.builder()
                .provider(hasText(dbConfig.getProvider()) ? dbConfig.getProvider() : aiConfig.getProvider())
                .model(hasText(dbConfig.getModel()) ? dbConfig.getModel() : aiConfig.getModel())
                .supervisorModel(hasText(dbConfig.getSupervisorModel())
                    ? dbConfig.getSupervisorModel() : aiConfig.getSupervisorModel())
                .visionModel(hasText(dbConfig.getVisionModel())
                    ? dbConfig.getVisionModel() : aiConfig.getVisionModel())
                .baseUrl(dbConfig.getBaseUrl() != null ? dbConfig.getBaseUrl() : aiConfig.getBaseUrl())
                .timeoutMs(dbConfig.getTimeoutMs() > 0
                    ? dbConfig.getTimeoutMs() : timeout(aiConfig.getTimeout()))
                .temperature(dbConfig.getTemperature() >= 0 ? dbConfig.getTemperature()
                    : (aiConfig.getTemperature() != null ? aiConfig.getTemperature() : 0.7))
                .build();
        } catch (Exception exception) {
            log.warn("AI 数据库配置无效，回退环境变量", exception);
            return environmentConfig();
        }
    }

    private AiModelConfig environmentConfig() {
        return AiModelConfig.builder()
            .provider(hasText(aiConfig.getProvider()) ? aiConfig.getProvider() : "zhipu")
            .model(effectiveModel())
            .supervisorModel(aiConfig.getSupervisorModel())
            .visionModel(hasText(zhipuConfig.getVisionModel())
                ? zhipuConfig.getVisionModel() : aiConfig.getVisionModel())
            .baseUrl(hasText(zhipuConfig.getBaseUrl())
                ? zhipuConfig.getBaseUrl() : aiConfig.getBaseUrl())
            .timeoutMs(timeout(zhipuConfig.getTimeout() != null ? zhipuConfig.getTimeout() : aiConfig.getTimeout()))
            .temperature(aiConfig.getTemperature() != null ? aiConfig.getTemperature() : 0.7)
            .build();
    }

    private AiModelConfig validatedConfig(AiModelCandidateDTO candidate) {
        if (candidate == null
                || !hasText(candidate.getProvider())
                || !hasText(candidate.getModel())
                || !hasText(candidate.getSupervisorModel())
                || !hasText(candidate.getVisionModel())) {
            throw new BusinessException("AI_CONFIG_INVALID", "provider 和模型名不能为空");
        }
        if (!"zhipu".equals(candidate.getProvider())) {
            throw new BusinessException("AI_CONFIG_INVALID", "暂不支持的 AI provider");
        }
        int timeout = candidate.getTimeoutMs() != null ? candidate.getTimeoutMs() : timeout(aiConfig.getTimeout());
        if (timeout <= 0) {
            throw new BusinessException("AI_CONFIG_INVALID", "超时时间必须大于 0");
        }
        double temperature = candidate.getTemperature() != null
            ? candidate.getTemperature()
            : (aiConfig.getTemperature() != null ? aiConfig.getTemperature() : 0.7);
        if (temperature < 0 || temperature > 2) {
            throw new BusinessException("AI_CONFIG_INVALID", "温度必须在 0 到 2 之间");
        }
        return AiModelConfig.builder()
            .provider(candidate.getProvider())
            .model(candidate.getModel().trim())
            .supervisorModel(candidate.getSupervisorModel().trim())
            .visionModel(candidate.getVisionModel().trim())
            .baseUrl(candidate.getBaseUrl() == null ? "" : candidate.getBaseUrl().trim())
            .timeoutMs(timeout)
            .temperature(temperature)
            .build();
    }

    private String writeConfigValue(AiModelConfig config) {
        try {
            return objectMapper.writeValueAsString(config);
        } catch (Exception exception) {
            throw new BusinessException("AI_CONFIG_INVALID", "AI 配置序列化失败");
        }
    }

    private AiModelConfig currentConfig() {
        DynamicAiModelRegistry.ActiveModels active = registry.current();
        return active.config() != null ? active.config() : environmentConfig();
    }

    private String resolveKey() {
        return AiApiKeyResolver.resolve(zhipuConfig, aiConfig, runtimeKeyStore);
    }

    private String candidateKey(AiModelCandidateDTO candidate) {
        return candidate != null && hasText(candidate.getApiKey())
            ? candidate.getApiKey().trim() : null;
    }

    private void rollbackRuntimeKey(String candidateKey, String previousKey) {
        if (candidateKey == null || runtimeKeyStore == null) {
            return;
        }
        try {
            if (runtimeKeyStore.exists() && hasText(previousKey)) {
                runtimeKeyStore.writeKey(previousKey);
            } else {
                runtimeKeyStore.deleteKey();
            }
        } catch (Exception exception) {
            log.error("AI key 回滚失败", exception);
        }
    }

    private AiCapabilitiesDTO capabilities(AiModelConfig config) {
        if (chatModelProvider != null) {
            return chatModelProvider.getCapabilities();
        }
        return modelFactory.capabilities(config);
    }

    private String effectiveModel() {
        if (hasText(zhipuConfig.getModel())) {
            return zhipuConfig.getModel();
        }
        return hasText(aiConfig.getModel()) ? aiConfig.getModel() : "glm-4-flash";
    }

    private int timeout(Integer value) {
        return value != null && value > 0 ? value : 30000;
    }

    private AiConnectivityResult failure(String model, String message) {
        return new AiConnectivityResult(false, message, testedAt(), model);
    }

    private AiRuntimeConfigSnapshot.AiKeyStatus keyStatus(String apiKey) {
        if (!hasText(apiKey)) {
            return new AiRuntimeConfigSnapshot.AiKeyStatus(false, null);
        }
        String key = apiKey.trim();
        return new AiRuntimeConfigSnapshot.AiKeyStatus(
            true,
            shortSha256(key)
        );
    }

    private String shortSha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 6);
        } catch (Exception exception) {
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
