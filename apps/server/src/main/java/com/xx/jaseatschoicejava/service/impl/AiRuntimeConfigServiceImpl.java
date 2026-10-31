package com.xx.jaseatschoicejava.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.xx.jaseatschoicejava.dto.AiConfigAuditEvent;
import com.xx.jaseatschoicejava.dto.AiModelCandidateDTO;
import com.xx.jaseatschoicejava.dto.AiPreviousConfigSummary;
import com.xx.jaseatschoicejava.dto.AiRuntimeConfigHistory;
import com.xx.jaseatschoicejava.dto.AiRuntimeConfigSnapshot;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.service.AiConfigAuditService;
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
    private static final String CONFIG_HISTORY_KEY = "ai.runtime-config-history";
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
    private final AiConfigAuditService auditService;
    private volatile long version = 1L;
    private volatile String updatedAt = LocalDateTime.now().withNano(0).toString();
    private AiRuntimeConfigHistory configHistory = AiRuntimeConfigHistory.builder()
        .activeVersion(1L)
        .activeUpdatedAt(updatedAt)
        .history(new ArrayList<>())
        .build();

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
        this(aiConfig, zhipuConfig, chatModelProvider, systemConfigService, registry,
            modelFactory, objectMapper, runtimeKeyStore, null);
    }

    public AiRuntimeConfigServiceImpl(
            AIConfig aiConfig,
            ZhipuAIConfig zhipuConfig,
            ChatModelProvider chatModelProvider,
            SystemConfigService systemConfigService,
            DynamicAiModelRegistry registry,
            AiModelClientFactory modelFactory,
            ObjectMapper objectMapper,
            AiRuntimeKeyStore runtimeKeyStore,
            AiConfigAuditService auditService) {
        this.aiConfig = aiConfig;
        this.zhipuConfig = zhipuConfig;
        this.chatModelProvider = chatModelProvider;
        this.systemConfigService = systemConfigService;
        this.registry = registry;
        this.modelFactory = modelFactory;
        this.objectMapper = objectMapper;
        this.runtimeKeyStore = runtimeKeyStore;
        this.auditService = auditService;
    }

    public AiRuntimeConfigServiceImpl(
            AIConfig aiConfig,
            ZhipuAIConfig zhipuConfig,
            ChatModelProvider chatModelProvider) {
        this(aiConfig, zhipuConfig, chatModelProvider, null, new DynamicAiModelRegistry(),
            new ZhipuAiModelClientFactory(aiConfig, zhipuConfig, null), new ObjectMapper(), null, null);
    }

    public AiRuntimeConfigServiceImpl(
            AIConfig aiConfig,
            ZhipuAIConfig zhipuConfig,
            SystemConfigService systemConfigService,
            DynamicAiModelRegistry registry,
            AiModelClientFactory modelFactory,
            ObjectMapper objectMapper) {
        this(aiConfig, zhipuConfig, null, systemConfigService, registry,
            modelFactory, objectMapper, null, null);
    }

    @PostConstruct
    public synchronized void initialize() {
        AiModelConfig config = effectiveConfig(systemConfigService == null ? null
            : systemConfigService.getConfigsByGroup(CONFIG_GROUP));
        configHistory = loadHistory();
        activate(config, configHistory.getActiveVersion());
    }

    @Override
    public AiPreviousConfigSummary getPreviousConfigSummary() {
        AiRuntimeConfigHistory.Entry previous = previousEntry();
        if (previous == null) {
            return null;
        }
        String apiKey = resolveKey();
        return AiPreviousConfigSummary.builder()
            .version(previous.getVersion())
            .updatedAt(previous.getUpdatedAt())
            .provider(previous.getConfig().getProvider())
            .model(previous.getConfig().getModel())
            .supervisorModel(previous.getConfig().getSupervisorModel())
            .visionModel(previous.getConfig().getVisionModel())
            .baseUrl(previous.getConfig().getBaseUrl())
            .timeoutMs(previous.getConfig().getTimeoutMs())
            .temperature(previous.getConfig().getTemperature())
            .keyConfigured(hasText(apiKey))
            .keyFingerprint(hasText(apiKey) ? shortSha256(apiKey) : null)
            .build();
    }

    @Override
    public AiRuntimeConfigSnapshot rollbackToPrevious(String operator) {
        AiRuntimeConfigHistory.Entry previous = previousEntry();
        if (previous == null) {
            recordAudit("ROLLBACK_FAILED", version, null, currentConfig(), null, operator,
                null, "FAILED", "没有可回滚的上一版配置");
            throw new BusinessException("AI_CONFIG_ROLLBACK_INVALID", "没有可回滚的上一版配置");
        }

        String apiKey = resolveKey();
        if (!hasText(apiKey)) {
            recordAudit("ROLLBACK_FAILED", version, null, currentConfig(), null, operator,
                null, "FAILED", "上一版 key 已清除，请重新输入后保存");
            throw new BusinessException("AI_KEY_REQUIRED", "上一版 key 已清除，请重新输入后保存");
        }
        try {
            ChatModel rollbackModel = chatModelProvider != null
                ? chatModelProvider.buildAiModel()
                : modelFactory.buildChatModel(previous.getConfig());
            rollbackModel.chat("ping");
        } catch (Exception exception) {
            recordAudit("ROLLBACK_FAILED", version, null, currentConfig(), null, operator,
                null, "FAILED", "上一版配置测试失败，请重新输入有效 key");
            throw new BusinessException("AI_CONFIG_ROLLBACK_TEST_FAILED",
                "上一版配置测试失败，请重新输入有效 key");
        }

        AiModelConfig currentConfig = currentConfig();
        long currentVersion = version;
        String currentUpdatedAt = updatedAt;
        AiRuntimeConfigHistory.Entry currentEntry = historyEntry(
            currentConfig, currentVersion, currentUpdatedAt);

        List<AiRuntimeConfigHistory.Entry> entries = new ArrayList<>(configHistory.getHistory());
        entries.removeIf(entry -> entry.getVersion() == previous.getVersion());
        entries.add(currentEntry);

        String configValue = writeConfigValue(previous.getConfig());
        boolean saved;
        try {
            saved = systemConfigService != null && systemConfigService.setConfigValue(
                CONFIG_KEY, configValue, "AI 模型运行时配置", CONFIG_GROUP);
        } catch (Exception exception) {
            recordAudit("ROLLBACK_FAILED", version, null, currentConfig(), null, operator,
                null, "FAILED", "回滚失败，当前配置未改变");
            throw new BusinessException("AI_CONFIG_ROLLBACK_FAILED", "回滚失败，当前配置未改变");
        }
        if (!saved) {
            recordAudit("ROLLBACK_FAILED", version, null, currentConfig(), null, operator,
                null, "FAILED", "回滚失败，当前配置未改变");
            throw new BusinessException("AI_CONFIG_ROLLBACK_FAILED", "回滚失败，当前配置未改变");
        }
        AiRuntimeConfigHistory restored = AiRuntimeConfigHistory.builder()
            .activeVersion(previous.getVersion())
            .activeUpdatedAt(previous.getUpdatedAt())
            .history(entries)
            .build();
        persistHistory(restored);

        DynamicAiModelRegistry.ActiveModels clients = new DynamicAiModelRegistry.ActiveModels(
            previous.getConfig(),
            modelFactory.buildChatModel(previous.getConfig()),
            modelFactory.buildSupervisorModel(previous.getConfig()),
            modelFactory.buildVisionModel(previous.getConfig()),
            modelFactory.buildStreamingModel(previous.getConfig()),
            previous.getVersion());
        registry.activate(clients);
        configHistory = restored;
        version = previous.getVersion();
        updatedAt = previous.getUpdatedAt();
        recordAudit("ROLLBACK_SUCCEEDED", currentVersion, previous.getVersion(), currentConfig,
            previous.getConfig(), operator, null, "SUCCESS", "AI 配置已回滚");
        return getCurrentSnapshot();
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
        return saveAndActivate(candidate, null);
    }

    @Override
    public AiRuntimeConfigSnapshot saveAndActivate(AiModelCandidateDTO candidate, String operator) {
        AiConnectivityResult testResult = testCandidateConnectivity(candidate);
        if (!testResult.isSuccess()) {
            recordAudit("SAVE_FAILED", null, null, null, candidate, operator, testResult,
                "FAILED", testResult.getMessage());
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
                recordAudit("SAVE_FAILED", version, null, currentConfig(), candidate, operator,
                    testResult, "FAILED", businessException.getMessage());
                throw businessException;
            }
            recordAudit("SAVE_FAILED", version, null, currentConfig(), candidate, operator,
                testResult, "FAILED", "AI key 保存失败");
            throw new BusinessException("AI_KEY_WRITE_FAILED", "AI key 保存失败，当前 key 未变更");
        }

        String configValue = writeConfigValue(config);
        boolean saved;
        try {
            saved = systemConfigService.setConfigValue(
                CONFIG_KEY, configValue, "AI 模型运行时配置", CONFIG_GROUP);
        } catch (Exception exception) {
            rollbackRuntimeKey(candidateKey, previousKey);
            recordAudit("SAVE_FAILED", version, null, currentConfig(), candidate, operator,
                testResult, "FAILED", "AI 配置保存失败");
            throw new BusinessException("AI_CONFIG_SAVE_FAILED", "AI 配置保存失败");
        }
        if (!saved) {
            rollbackRuntimeKey(candidateKey, previousKey);
            recordAudit("SAVE_FAILED", version, null, currentConfig(), candidate, operator,
                testResult, "FAILED", "AI 配置保存失败");
            throw new BusinessException("AI_CONFIG_SAVE_FAILED", "AI 配置保存失败");
        }

        AiModelConfig previousConfig = currentConfig();
        long previousVersion = version;
        String previousUpdatedAt = updatedAt;

        recordAudit("SAVE_SUCCEEDED", version, nextVersion, currentConfig(), candidate,
            operator, testResult, "SUCCESS", "AI 非密钥配置已保存");
        String now = LocalDateTime.now().withNano(0).toString();
        registry.activate(clients);
        version = nextVersion;
        updatedAt = now;
        List<AiRuntimeConfigHistory.Entry> entries = new ArrayList<>(configHistory.getHistory());
        entries.add(historyEntry(previousConfig, previousVersion, previousUpdatedAt));
        configHistory = AiRuntimeConfigHistory.builder()
            .activeVersion(nextVersion)
            .activeUpdatedAt(now)
            .history(entries)
            .build();
        persistHistory(configHistory);
        recordAudit("ACTIVATED", previousVersion, nextVersion, previousConfig, candidate,
            operator, testResult, "SUCCESS", "AI 配置已保存并激活");
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
        updatedAt = configHistory.getActiveUpdatedAt() != null
            ? configHistory.getActiveUpdatedAt()
            : LocalDateTime.now().withNano(0).toString();
        log.info("AI 运行时配置已激活：version={}, model={}, vision={}",
            nextVersion, config.getModel(), config.getVisionModel());
    }

    private AiRuntimeConfigHistory loadHistory() {
        if (systemConfigService == null) {
            return AiRuntimeConfigHistory.builder()
                .activeVersion(1L)
                .activeUpdatedAt(LocalDateTime.now().withNano(0).toString())
                .history(new ArrayList<>())
                .build();
        }
        String value = systemConfigService.getConfigValue(CONFIG_HISTORY_KEY);
        if (!hasText(value)) {
            return AiRuntimeConfigHistory.builder()
                .activeVersion(1L)
                .activeUpdatedAt(LocalDateTime.now().withNano(0).toString())
                .history(new ArrayList<>())
                .build();
        }
        try {
            AiRuntimeConfigHistory history = objectMapper.readValue(value, AiRuntimeConfigHistory.class);
            return AiRuntimeConfigHistory.builder()
                .activeVersion(history.getActiveVersion() > 0 ? history.getActiveVersion() : 1L)
                .activeUpdatedAt(hasText(history.getActiveUpdatedAt())
                    ? history.getActiveUpdatedAt()
                    : LocalDateTime.now().withNano(0).toString())
                .history(history.getHistory() == null ? new ArrayList<>() : history.getHistory())
                .build();
        } catch (Exception exception) {
            log.warn("AI 配置历史无效，忽略回滚历史", exception);
            return AiRuntimeConfigHistory.builder()
                .activeVersion(1L)
                .activeUpdatedAt(LocalDateTime.now().withNano(0).toString())
                .history(new ArrayList<>())
                .build();
        }
    }

    private void persistHistory(AiRuntimeConfigHistory history) {
        if (systemConfigService == null) {
            return;
        }
        try {
            systemConfigService.setConfigValue(
                CONFIG_HISTORY_KEY, objectMapper.writeValueAsString(history),
                "AI 模型运行时配置历史", CONFIG_GROUP);
        } catch (Exception exception) {
            log.warn("AI 配置历史保存失败", exception);
        }
    }

    private AiRuntimeConfigHistory.Entry previousEntry() {
        List<AiRuntimeConfigHistory.Entry> entries = configHistory.getHistory();
        return entries == null || entries.isEmpty() ? null : entries.get(entries.size() - 1);
    }

    private AiRuntimeConfigHistory.Entry historyEntry(
            AiModelConfig config, long version, String updatedAt) {
        String apiKey = resolveKey();
        return AiRuntimeConfigHistory.Entry.builder()
            .version(version)
            .updatedAt(updatedAt)
            .config(config)
            .keyConfigured(hasText(apiKey))
            .keyFingerprint(hasText(apiKey) ? shortSha256(apiKey) : null)
            .build();
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

    private void recordAudit(
            String action,
            Long oldVersion,
            Long newVersion,
            AiModelConfig oldConfig,
            Object newConfig,
            String operator,
            AiConnectivityResult testResult,
            String status,
            String message) {
        if (auditService == null) {
            return;
        }
        String key = resolveKey();
        Map<String, Object> oldSummary = oldConfig == null ? Map.of() : summary(oldConfig, key);
        Map<String, Object> newSummary;
        if (newConfig instanceof AiModelConfig modelConfig) {
            newSummary = summary(modelConfig, key);
        } else if (newConfig instanceof AiModelCandidateDTO candidate) {
            newSummary = summary(candidate, key);
        } else {
            newSummary = Map.of();
        }
        auditService.record(AiConfigAuditEvent.builder()
            .action(action)
            .operator(operator)
            .operatedAt(LocalDateTime.now().withNano(0).toString())
            .oldVersion(oldVersion)
            .newVersion(newVersion)
            .oldSummary(oldSummary)
            .newSummary(newSummary)
            .testResult(testResult == null ? "" : (testResult.isSuccess() ? "SUCCESS" : "FAILED"))
            .status(status)
            .message(message)
            .build());
    }

    private Map<String, Object> summary(AiModelConfig config, String key) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("provider", config.getProvider());
        summary.put("model", config.getModel());
        summary.put("supervisorModel", config.getSupervisorModel());
        summary.put("visionModel", config.getVisionModel());
        summary.put("baseUrl", config.getBaseUrl());
        summary.put("timeoutMs", config.getTimeoutMs());
        summary.put("temperature", config.getTemperature());
        summary.put("keyConfigured", hasText(key));
        summary.put("keyFingerprint", hasText(key) ? shortSha256(key) : null);
        return summary;
    }

    private Map<String, Object> summary(AiModelCandidateDTO candidate, String key) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("provider", candidate.getProvider());
        summary.put("model", candidate.getModel());
        summary.put("supervisorModel", candidate.getSupervisorModel());
        summary.put("visionModel", candidate.getVisionModel());
        summary.put("baseUrl", candidate.getBaseUrl());
        summary.put("timeoutMs", candidate.getTimeoutMs());
        summary.put("temperature", candidate.getTemperature());
        summary.put("keyChanged", hasText(candidate.getApiKey()));
        summary.put("keyConfigured", hasText(candidate.getApiKey()) || hasText(key));
        summary.put("keyFingerprint", hasText(candidate.getApiKey())
            ? shortSha256(candidate.getApiKey()) : (hasText(key) ? shortSha256(key) : null));
        return summary;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
