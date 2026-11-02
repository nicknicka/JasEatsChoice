package com.xx.jaseatschoicejava.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.agent.config.DynamicAiModelRegistry;
import com.xx.jaseatschoicejava.config.AiModelConfig;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiEmbeddingRuntimeKeyStore;
import com.xx.jaseatschoicejava.config.AiRuntimeKeyStore;
import com.xx.jaseatschoicejava.dto.AiConnectivityResult;
import com.xx.jaseatschoicejava.dto.AiEmbeddingCandidateDTO;
import com.xx.jaseatschoicejava.dto.AiEmbeddingSnapshot;
import com.xx.jaseatschoicejava.dto.AiConfigAuditEvent;
import com.xx.jaseatschoicejava.dto.AiModelCandidateDTO;
import com.xx.jaseatschoicejava.dto.AiModelOption;
import com.xx.jaseatschoicejava.dto.AiPreviousConfigSummary;
import com.xx.jaseatschoicejava.dto.AiRuntimeConfigHistory;
import com.xx.jaseatschoicejava.dto.AiRuntimeConfigSnapshot;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.rag.AnnouncementEmbeddingConfig;
import com.xx.jaseatschoicejava.rag.AnnouncementRagStatus;
import com.xx.jaseatschoicejava.service.AnnouncementEmbeddingRuntime;
import com.xx.jaseatschoicejava.service.AiConfigAuditService;
import com.xx.jaseatschoicejava.service.AiConfigHistoryStore;
import com.xx.jaseatschoicejava.service.AiRuntimeConfigService;
import com.xx.jaseatschoicejava.service.SystemConfigService;
import com.xx.jaseatschoicejava.util.SecretFingerprint;

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

    private final AIConfig aiConfig;
    private final SystemConfigService systemConfigService;
    private final DynamicAiModelRegistry registry;
    private final AiProviderRegistry providerRegistry;
    private final ObjectMapper objectMapper;
    private final AiRuntimeKeyStore runtimeKeyStore;
    private final AiConfigAuditService auditService;
    private final AiConfigHistoryStore historyStore;
    private final AnnouncementEmbeddingRuntime embeddingRuntime;
    private final AiEmbeddingRuntimeKeyStore embeddingKeyStore;
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
            SystemConfigService systemConfigService,
            DynamicAiModelRegistry registry,
            AiProviderRegistry providerRegistry,
            ObjectMapper objectMapper,
            AiRuntimeKeyStore runtimeKeyStore,
            AiConfigAuditService auditService,
            AiConfigHistoryStore historyStore,
            AnnouncementEmbeddingRuntime embeddingRuntime,
            AiEmbeddingRuntimeKeyStore embeddingKeyStore) {
        this.aiConfig = aiConfig;
        this.systemConfigService = systemConfigService;
        this.registry = registry;
        this.providerRegistry = providerRegistry;
        this.objectMapper = objectMapper;
        this.runtimeKeyStore = runtimeKeyStore;
        this.auditService = auditService;
        this.historyStore = historyStore;
        this.embeddingRuntime = embeddingRuntime;
        this.embeddingKeyStore = embeddingKeyStore;
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
        String apiKey = activeKey();
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
            .keyFingerprint(hasText(apiKey) ? SecretFingerprint.shortSha256(apiKey) : null)
            .build();
    }

    @Override
    public synchronized AiRuntimeConfigSnapshot rollbackToPrevious(String operator) {
        AiRuntimeConfigHistory.Entry previous = previousEntry();
        if (previous == null) {
            recordAudit("ROLLBACK_FAILED", version, null, currentConfig(), null, operator,
                null, "FAILED", "没有可回滚的上一版配置");
            throw new BusinessException("AI_CONFIG_ROLLBACK_INVALID", "没有可回滚的上一版配置");
        }

        if (!hasText(activeKey())) {
            recordAudit("ROLLBACK_FAILED", version, null, currentConfig(), null, operator,
                null, "FAILED", "上一版 key 已清除，请重新输入后保存");
            throw new BusinessException("AI_KEY_REQUIRED", "上一版 key 已清除，请重新输入后保存");
        }

        AiModelClientFactory adapter = adapter(previous.getConfig().getProvider());
        try {
            pingRoles(adapter, previous.getConfig(), null);
            validateEmbeddingForRollback(previous.getConfig());
        } catch (BusinessException businessException) {
            recordAudit("ROLLBACK_FAILED", version, null, currentConfig(), null, operator,
                null, "FAILED", businessException.getMessage());
            throw businessException;
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
        long rollbackVersion = nextVersion();
        String now = LocalDateTime.now().withNano(0).toString();
        List<AiRuntimeConfigHistory.Entry> entries = new ArrayList<>(configHistory.getHistory());
        entries.add(currentEntry);
        AiRuntimeConfigHistory restored = AiRuntimeConfigHistory.builder()
            .activeVersion(rollbackVersion)
            .activeUpdatedAt(now)
            .history(entries)
            .build();

        String previousDatabaseValue = databaseConfigValue();
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

        try {
            persistHistory(restored);
        } catch (Exception exception) {
            restoreDatabaseConfig(previousDatabaseValue);
            recordAudit("ROLLBACK_FAILED", version, null, currentConfig(), null, operator,
                null, "FAILED", "回滚历史保存失败，当前配置未改变");
            throw new BusinessException("AI_CONFIG_ROLLBACK_HISTORY_FAILED",
                "回滚历史保存失败，当前配置未改变");
        }

        DynamicAiModelRegistry.ActiveModels clients = activeClients(
            adapter, previous.getConfig(), null, rollbackVersion);
        registry.activate(clients);
        boolean rebuildTriggered = switchEmbeddingConfig(previous.getConfig());
        configHistory = restored;
        version = rollbackVersion;
        updatedAt = now;
        recordAudit("ROLLBACK_SUCCEEDED", currentVersion, rollbackVersion, currentConfig,
            previous.getConfig(), operator, null, "SUCCESS", rebuildTriggered
                ? "AI 配置已回滚，已触发公告索引重建"
                : "AI 配置已回滚");
        return getCurrentSnapshot();
    }

    @Override
    public synchronized AiRuntimeConfigSnapshot rebuildEmbeddingIndex(String operator) {
        if (embeddingRuntime == null) {
            throw new BusinessException("EMBEDDING_REBUILD_UNSUPPORTED", "公告索引重建不可用");
        }
        embeddingRuntime.rebuildIndex();
        recordAudit("EMBEDDING_REBUILD_REQUESTED", version, version, currentConfig(), null, operator,
            null, "SUCCESS", "管理员触发公告索引重建");
        return getCurrentSnapshot();
    }

    @Override
    public AiRuntimeConfigSnapshot getCurrentSnapshot() {
        AiModelConfig config = currentConfig();
        AiModelClientFactory adapter = adapter(config.getProvider());
        String apiKey = activeKey();

        List<String> supportedModels = new ArrayList<>(adapter.knownModels());
        for (String currentModel : List.of(config.getModel(), config.getSupervisorModel(),
                config.getVisionModel())) {
            if (hasText(currentModel) && !supportedModels.contains(currentModel)) {
                supportedModels.add(currentModel);
            }
        }

        return AiRuntimeConfigSnapshot.builder()
            .provider(config.getProvider())
            .model(config.getModel())
            .supervisorModel(config.getSupervisorModel())
            .visionModel(config.getVisionModel())
            .baseUrl(config.getBaseUrl() == null ? "" : config.getBaseUrl())
            .timeoutMs(config.getTimeoutMs())
            .temperature(config.getTemperature())
            .updatedAt(updatedAt)
            .version(version)
            .keyStatus(keyStatus(apiKey))
            .capabilities(adapter.capabilities(config))
            .supportedModels(supportedModels)
            .providers(providerRegistry.options())
            .embedding(embeddingSnapshot(config))
            .build();
    }

    @Override
    public AiConnectivityResult testCurrentConnectivity() {
        AiModelConfig config = currentConfig();
        AiModelClientFactory adapter = adapter(config.getProvider());
        String apiKey = activeKey();
        if (!hasText(apiKey)) {
            return failure(config.getModel(), "AI key 未配置");
        }
        try {
            pingRoles(adapter, config, null);
            return new AiConnectivityResult(true, "连接成功", testedAt(), config.getModel());
        } catch (Exception exception) {
            return failure(config.getModel(), safeMessage(exception, apiKey));
        }
    }

    @Override
    public List<AiModelOption> discoverModels(AiModelCandidateDTO candidate) {
        AiModelClientFactory adapter = adapter(candidate == null ? null : candidate.getProvider());
        return adapter.discoverModels(candidate);
    }

    @Override
    public AiConnectivityResult testCandidateConnectivity(AiModelCandidateDTO candidate) {
        AiModelConfig config;
        AiModelClientFactory adapter;
        try {
            config = validatedConfig(candidate);
            adapter = adapter(config.getProvider());
        } catch (BusinessException | IllegalArgumentException exception) {
            return failure(candidate == null ? null : candidate.getModel(), exception.getMessage());
        }
        String candidateKey = candidateKey(candidate);
        String apiKey = candidateKey != null ? candidateKey : activeKey();
        if (!hasText(apiKey)) {
            return failure(config.getModel(), "AI key 未配置");
        }
        try {
            pingRoles(adapter, config, candidateKey);
            validateEmbeddingCandidate(candidate);
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
    public synchronized AiRuntimeConfigSnapshot saveAndActivate(
            AiModelCandidateDTO candidate, String operator) {
        AiConnectivityResult testResult = testCandidateConnectivity(candidate);
        if (!testResult.isSuccess()) {
            recordAudit("SAVE_FAILED", null, null, null, candidate, operator, testResult,
                "FAILED", testResult.getMessage());
            throw new BusinessException("AI_CONFIG_TEST_FAILED", testResult.getMessage());
        }

        AiModelConfig config = validatedConfig(candidate);
        AiModelConfig targetConfig = withCandidateEmbedding(config, candidate);
        AiModelClientFactory adapter = adapter(config.getProvider());
        String candidateKey = candidateKey(candidate);
        String candidateEmbeddingKey = candidateEmbeddingKey(candidate);
        String previousKey = activeKey();
        String previousEmbeddingKey = currentEmbeddingKey();
        String previousDatabaseValue = databaseConfigValue();
        long nextVersion = version + 1;
        DynamicAiModelRegistry.ActiveModels clients;
        try {
            clients = activeClients(adapter, targetConfig, candidateKey, nextVersion);
            if (candidateKey != null) {
                if (runtimeKeyStore == null) {
                    throw new IllegalStateException("AI_ACTIVE_KEY_FILE 未配置");
                }
                runtimeKeyStore.writeKey(candidateKey);
            }
            writeEmbeddingKey(candidateEmbeddingKey);
        } catch (Exception exception) {
            rollbackRuntimeKey(candidateKey, previousKey);
            rollbackEmbeddingKey(candidateEmbeddingKey, previousEmbeddingKey);
            if (exception instanceof BusinessException businessException) {
                recordAudit("SAVE_FAILED", version, null, currentConfig(), candidate, operator,
                    testResult, "FAILED", businessException.getMessage());
                throw businessException;
            }
            recordAudit("SAVE_FAILED", version, null, currentConfig(), candidate, operator,
                testResult, "FAILED", "AI key 保存失败");
            throw new BusinessException("AI_KEY_WRITE_FAILED", "AI key 保存失败，当前 key 未变更");
        }

        boolean saved;
        try {
            saved = systemConfigService != null && systemConfigService.setConfigValue(
                CONFIG_KEY, writeConfigValue(targetConfig), "AI 模型运行时配置", CONFIG_GROUP);
        } catch (Exception exception) {
            rollbackRuntimeKey(candidateKey, previousKey);
            rollbackEmbeddingKey(candidateEmbeddingKey, previousEmbeddingKey);
            recordAudit("SAVE_FAILED", version, null, currentConfig(), candidate, operator,
                testResult, "FAILED", "AI 配置保存失败");
            throw new BusinessException("AI_CONFIG_SAVE_FAILED", "AI 配置保存失败");
        }
        if (!saved) {
            rollbackRuntimeKey(candidateKey, previousKey);
            rollbackEmbeddingKey(candidateEmbeddingKey, previousEmbeddingKey);
            recordAudit("SAVE_FAILED", version, null, currentConfig(), candidate, operator,
                testResult, "FAILED", "AI 配置保存失败");
            throw new BusinessException("AI_CONFIG_SAVE_FAILED", "AI 配置保存失败");
        }

        AiModelConfig previousConfig = currentConfig();
        long previousVersion = version;
        String previousUpdatedAt = updatedAt;
        recordAudit("SAVE_SUCCEEDED", previousVersion, nextVersion, previousConfig, candidate,
            operator, testResult, "SUCCESS", "AI 非密钥配置已保存");

        String now = LocalDateTime.now().withNano(0).toString();
        List<AiRuntimeConfigHistory.Entry> entries = new ArrayList<>(configHistory.getHistory());
        entries.add(historyEntry(previousConfig, previousVersion, previousUpdatedAt));
        AiRuntimeConfigHistory history = AiRuntimeConfigHistory.builder()
            .activeVersion(nextVersion)
            .activeUpdatedAt(now)
            .history(entries)
            .build();
        try {
            persistHistory(history);
        } catch (Exception exception) {
            rollbackRuntimeKey(candidateKey, previousKey);
            rollbackEmbeddingKey(candidateEmbeddingKey, previousEmbeddingKey);
            restoreDatabaseConfig(previousDatabaseValue);
            recordAudit("SAVE_FAILED", previousVersion, null, previousConfig, candidate, operator,
                testResult, "FAILED", "AI 配置历史保存失败");
            throw new BusinessException("AI_CONFIG_HISTORY_SAVE_FAILED",
                "AI 配置历史保存失败，当前配置未变更");
        }

        registry.activate(clients);
        boolean rebuildTriggered = switchEmbeddingConfig(targetConfig);
        version = nextVersion;
        updatedAt = now;
        configHistory = history;
        recordAudit("ACTIVATED", previousVersion, nextVersion, previousConfig, candidate,
            operator, testResult, "SUCCESS", rebuildTriggered
                ? "AI 配置已保存并激活，已触发公告索引重建"
                : "AI 配置已保存并激活");
        return getCurrentSnapshot();
    }

    private DynamicAiModelRegistry.ActiveModels activeClients(
            AiModelClientFactory adapter,
            AiModelConfig config,
            String candidateKey,
            long version) {
        return new DynamicAiModelRegistry.ActiveModels(
            config,
            adapter.buildChatModel(config, candidateKey),
            adapter.buildSupervisorModel(config, candidateKey),
            adapter.buildVisionModel(config, candidateKey),
            adapter.buildStreamingModel(config, candidateKey),
            version);
    }

    private void pingRoles(
            AiModelClientFactory adapter,
            AiModelConfig config,
            String candidateKey) {
        adapter.buildChatModel(config, candidateKey).chat("ping");
        adapter.buildSupervisorModel(config, candidateKey).chat("ping");
        adapter.buildVisionModel(config, candidateKey).chat("ping");
    }

    private void activate(AiModelConfig config, long nextVersion) {
        AiModelClientFactory adapter = adapter(config.getProvider());
        DynamicAiModelRegistry.ActiveModels clients = new DynamicAiModelRegistry.ActiveModels(
            config,
            adapter.buildChatModel(config),
            adapter.buildSupervisorModel(config),
            adapter.buildVisionModel(config),
            adapter.buildStreamingModel(config),
            nextVersion);
        registry.activate(clients);
        activateEmbedding(config);
        version = nextVersion;
        updatedAt = configHistory.getActiveUpdatedAt() != null
            ? configHistory.getActiveUpdatedAt()
            : LocalDateTime.now().withNano(0).toString();
        log.info("AI 运行时配置已激活：version={}, model={}, vision={}",
            nextVersion, config.getModel(), config.getVisionModel());
    }

    private AiRuntimeConfigHistory loadHistory() {
        if (historyStore == null) {
            return emptyHistory();
        }
        String value = historyStore.load();
        if (!hasText(value)) {
            return emptyHistory();
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
            return emptyHistory();
        }
    }

    private AiRuntimeConfigHistory emptyHistory() {
        return AiRuntimeConfigHistory.builder()
            .activeVersion(1L)
            .activeUpdatedAt(LocalDateTime.now().withNano(0).toString())
            .history(new ArrayList<>())
            .build();
    }

    private void persistHistory(AiRuntimeConfigHistory history) {
        if (historyStore == null) {
            return;
        }
        try {
            historyStore.save(objectMapper.writeValueAsString(history));
        } catch (Exception exception) {
            throw new IllegalStateException("AI 配置历史保存失败", exception);
        }
    }

    private AiRuntimeConfigHistory.Entry previousEntry() {
        List<AiRuntimeConfigHistory.Entry> entries = configHistory.getHistory();
        return entries == null || entries.isEmpty() ? null : entries.get(entries.size() - 1);
    }

    private AiRuntimeConfigHistory.Entry historyEntry(
            AiModelConfig config, long version, String updatedAt) {
        String apiKey = activeKey();
        return AiRuntimeConfigHistory.Entry.builder()
            .version(version)
            .updatedAt(updatedAt)
            .config(config)
            .keyConfigured(hasText(apiKey))
            .keyFingerprint(hasText(apiKey) ? SecretFingerprint.shortSha256(apiKey) : null)
            .build();
    }

    private AiModelConfig effectiveConfig(Map<String, String> databaseConfigs) {
        String provider = bootstrapProvider();
        AiModelConfig fallback = adapter(provider).environmentConfig(aiConfig);
        if (databaseConfigs == null || !hasText(databaseConfigs.get(CONFIG_KEY))) {
            return withEmbedding(fallback, null);
        }
        try {
            AiModelConfig dbConfig = objectMapper.readValue(
                databaseConfigs.get(CONFIG_KEY), AiModelConfig.class);
            String dbProvider = hasText(dbConfig.getProvider()) ? dbConfig.getProvider() : provider;
            AiModelConfig providerFallback = adapter(dbProvider).environmentConfig(aiConfig);
            AiModelConfig effective = AiModelConfig.builder()
                .provider(dbProvider)
                .model(hasText(dbConfig.getModel()) ? dbConfig.getModel() : providerFallback.getModel())
                .supervisorModel(hasText(dbConfig.getSupervisorModel())
                    ? dbConfig.getSupervisorModel() : providerFallback.getSupervisorModel())
                .visionModel(hasText(dbConfig.getVisionModel())
                    ? dbConfig.getVisionModel() : providerFallback.getVisionModel())
                .baseUrl(dbConfig.getBaseUrl() != null ? dbConfig.getBaseUrl() : providerFallback.getBaseUrl())
                .timeoutMs(dbConfig.getTimeoutMs() > 0
                    ? dbConfig.getTimeoutMs() : providerFallback.getTimeoutMs())
                .temperature(dbConfig.getTemperature() >= 0 ? dbConfig.getTemperature()
                    : providerFallback.getTemperature())
                .build();
            return withEmbedding(effective, dbConfig);
        } catch (Exception exception) {
            log.warn("AI 数据库配置无效，回退环境变量", exception);
            return withEmbedding(fallback, null);
        }
    }

    /**
     * 把持久化的 Embedding 配置与环境变量兜底合并。首次上线与历史配置没有 Embedding 字段，
     * 由环境变量补齐，因此未配置 Embedding 时应用仍能启动、只有公告检索降级。
     */
    private AiModelConfig withEmbedding(AiModelConfig config, AiModelConfig persisted) {
        if (embeddingRuntime == null) {
            return config;
        }
        AnnouncementEmbeddingConfig effective = embeddingRuntime.effective(
            persisted == null ? null : embeddingConfig(persisted));
        return config.toBuilder()
            .embeddingProvider(effective.provider())
            .embeddingModel(effective.model())
            .embeddingDimensions(effective.dimensions())
            .embeddingBaseUrl(effective.baseUrl())
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
        adapter(candidate.getProvider());
        int timeout = candidate.getTimeoutMs() != null ? candidate.getTimeoutMs() : 30000;
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
        return active.config() != null ? active.config()
            : adapter(bootstrapProvider()).environmentConfig(aiConfig);
    }

    private String activeKey() {
        AiModelClientFactory adapter = adapter(currentConfig().getProvider());
        return adapter.resolveKey(runtimeKeyStore);
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

    /**
     * 候选带 Embedding 配置、且与当前生效值不同时才校验：先聊天连通性，再做一次真实 Embedding 调用，
     * 并比对现有 collection 维度。换模型在本切片直接拒绝——旧向量与新模型不在同一向量空间，
     * 必须先重建索引（后续切片），维度不一致同样拒绝并提示需要新建 collection。
     */
    private void validateEmbeddingCandidate(AiModelCandidateDTO candidate) {
        if (embeddingRuntime == null || candidate == null || candidate.getEmbedding() == null) {
            return;
        }
        AnnouncementEmbeddingConfig target = embeddingCandidate(candidate.getEmbedding());
        AnnouncementEmbeddingConfig current = currentEmbeddingConfig();
        if (target.equals(current) && !hasText(candidate.getEmbedding().getApiKey())) {
            return;
        }
        embeddingRuntime.validate(target, candidate.getEmbedding().getApiKey());
    }

    /**
     * 回滚与保存走同一条路径：先校验上一版 Embedding 配置可用，再切换生效并按需触发重建。
     */
    private void validateEmbeddingForRollback(AiModelConfig previousConfig) {
        if (embeddingRuntime == null || !hasText(previousConfig.getEmbeddingProvider())) {
            return;
        }
        embeddingRuntime.validate(embeddingConfig(previousConfig), null);
    }

    private AiModelConfig withCandidateEmbedding(AiModelConfig config, AiModelCandidateDTO candidate) {
        if (candidate.getEmbedding() == null) {
            AiModelConfig current = currentConfig();
            return config.toBuilder()
                .embeddingProvider(current.getEmbeddingProvider())
                .embeddingModel(current.getEmbeddingModel())
                .embeddingDimensions(current.getEmbeddingDimensions())
                .embeddingBaseUrl(current.getEmbeddingBaseUrl())
                .build();
        }
        AnnouncementEmbeddingConfig target = embeddingCandidate(candidate.getEmbedding());
        return config.toBuilder()
            .embeddingProvider(target.provider())
            .embeddingModel(target.model())
            .embeddingDimensions(target.dimensions())
            .embeddingBaseUrl(target.baseUrl())
            .build();
    }

    private AnnouncementEmbeddingConfig embeddingCandidate(AiEmbeddingCandidateDTO candidate) {
        if (!hasText(candidate.getProvider()) || !hasText(candidate.getModel())) {
            throw new BusinessException("AI_CONFIG_INVALID", "Embedding provider 与模型名不能为空");
        }
        int dimensions = candidate.getDimensions() == null ? 0 : candidate.getDimensions();
        if (dimensions <= 0) {
            throw new BusinessException("AI_CONFIG_INVALID", "Embedding 向量维度必须大于 0");
        }
        return new AnnouncementEmbeddingConfig(
            candidate.getProvider().trim(),
            candidate.getModel().trim(),
            dimensions,
            candidate.getBaseUrl() == null ? "" : candidate.getBaseUrl().trim());
    }

    private AnnouncementEmbeddingConfig embeddingConfig(AiModelConfig config) {
        return new AnnouncementEmbeddingConfig(
            config.getEmbeddingProvider(),
            config.getEmbeddingModel(),
            config.getEmbeddingDimensions(),
            config.getEmbeddingBaseUrl());
    }

    private AnnouncementEmbeddingConfig currentEmbeddingConfig() {
        return embeddingConfig(currentConfig());
    }

    private void activateEmbedding(AiModelConfig config) {
        if (embeddingRuntime == null || config == null || !hasText(config.getEmbeddingProvider())) {
            return;
        }
        embeddingRuntime.activate(embeddingConfig(config));
    }

    /**
     * 切换生效配置；Embedding 模型变化会触发公告索引重建。
     */
    private boolean switchEmbeddingConfig(AiModelConfig config) {
        if (embeddingRuntime == null || config == null || !hasText(config.getEmbeddingProvider())) {
            return false;
        }
        return embeddingRuntime.switchConfig(embeddingConfig(config));
    }

    private String candidateEmbeddingKey(AiModelCandidateDTO candidate) {
        if (candidate == null || candidate.getEmbedding() == null
                || !hasText(candidate.getEmbedding().getApiKey())) {
            return null;
        }
        return candidate.getEmbedding().getApiKey().trim();
    }

    private String currentEmbeddingKey() {
        return embeddingKeyStore == null ? "" : embeddingKeyStore.readKey();
    }

    private void writeEmbeddingKey(String candidateKey) {
        if (candidateKey == null) {
            return;
        }
        if (embeddingKeyStore == null) {
            throw new IllegalStateException("AI_EMBEDDING_ACTIVE_KEY_FILE 未配置");
        }
        embeddingKeyStore.writeKey(candidateKey);
    }

    private void rollbackEmbeddingKey(String candidateKey, String previousKey) {
        if (candidateKey == null || embeddingKeyStore == null) {
            return;
        }
        try {
            if (embeddingKeyStore.exists() && hasText(previousKey)) {
                embeddingKeyStore.writeKey(previousKey);
            } else {
                embeddingKeyStore.deleteKey();
            }
        } catch (Exception exception) {
            log.error("Embedding key 回滚失败", exception);
        }
    }

    private AiEmbeddingSnapshot embeddingSnapshot(AiModelConfig config) {
        AnnouncementRagStatus status = embeddingRuntime == null ? null : embeddingRuntime.status();
        return AiEmbeddingSnapshot.builder()
            .provider(config.getEmbeddingProvider())
            .model(config.getEmbeddingModel())
            .dimensions(config.getEmbeddingDimensions())
            .baseUrl(config.getEmbeddingBaseUrl() == null ? "" : config.getEmbeddingBaseUrl())
            .providers(embeddingRuntime == null ? List.of() : embeddingRuntime.providers())
            .state(status == null ? null : status.state().name())
            .statusReason(status == null ? null : status.reason())
            .keyStatus(embeddingKeyStatus())
            .build();
    }

    private AiRuntimeConfigSnapshot.AiKeyStatus embeddingKeyStatus() {
        if (embeddingRuntime == null) {
            return new AiRuntimeConfigSnapshot.AiKeyStatus(false, null, null);
        }
        AnnouncementEmbeddingRuntime.KeyStatus status = embeddingRuntime.keyStatus();
        String keyUpdatedAt = status.updatedAt();
        return new AiRuntimeConfigSnapshot.AiKeyStatus(
            status.configured(),
            status.fingerprint(),
            hasText(keyUpdatedAt) ? keyUpdatedAt : updatedAt);
    }

    private String bootstrapProvider() {
        if (hasText(aiConfig.getProvider())) {
            return aiConfig.getProvider();
        }
        List<AiRuntimeConfigSnapshot.AiProviderOption> options = providerRegistry.options();
        if (options.isEmpty()) {
            throw new IllegalStateException("没有已实现的 AI provider");
        }
        return options.get(0).getCode();
    }

    private AiModelClientFactory adapter(String provider) {
        return providerRegistry.required(hasText(provider) ? provider : bootstrapProvider());
    }

    private String databaseConfigValue() {
        if (systemConfigService == null) {
            return null;
        }
        return systemConfigService.getConfigsByGroup(CONFIG_GROUP).get(CONFIG_KEY);
    }

    private void restoreDatabaseConfig(String previousDatabaseValue) {
        if (systemConfigService == null) {
            return;
        }
        if (hasText(previousDatabaseValue)) {
            systemConfigService.setConfigValue(
                CONFIG_KEY, previousDatabaseValue, "AI 模型运行时配置", CONFIG_GROUP);
            return;
        }
        systemConfigService.deleteConfig(CONFIG_KEY);
        systemConfigService.refreshConfigCache();
    }

    private long nextVersion() {
        long result = version + 1;
        List<AiRuntimeConfigHistory.Entry> entries = configHistory.getHistory();
        if (entries != null) {
            for (AiRuntimeConfigHistory.Entry entry : entries) {
                result = Math.max(result, entry.getVersion() + 1);
            }
        }
        return result;
    }

    private AiConnectivityResult failure(String model, String message) {
        return new AiConnectivityResult(false, message, testedAt(), model);
    }

    private AiRuntimeConfigSnapshot.AiKeyStatus keyStatus(String apiKey) {
        String keyUpdatedAt = runtimeKeyStore == null ? null : runtimeKeyStore.updatedAt();
        if (!hasText(apiKey)) {
            return new AiRuntimeConfigSnapshot.AiKeyStatus(false, null, null);
        }
        String key = apiKey.trim();
        return new AiRuntimeConfigSnapshot.AiKeyStatus(
            true,
            SecretFingerprint.shortSha256(key),
            hasText(keyUpdatedAt) ? keyUpdatedAt : updatedAt
        );
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
        String key = activeKey();
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
        summary.put("keyFingerprint", hasText(key) ? SecretFingerprint.shortSha256(key) : null);
        summary.put("embeddingProvider", config.getEmbeddingProvider());
        summary.put("embeddingModel", config.getEmbeddingModel());
        summary.put("embeddingDimensions", config.getEmbeddingDimensions());
        summary.put("embeddingBaseUrl", config.getEmbeddingBaseUrl());
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
            ? SecretFingerprint.shortSha256(candidate.getApiKey()) : (hasText(key) ? SecretFingerprint.shortSha256(key) : null));
        AiEmbeddingCandidateDTO embedding = candidate.getEmbedding();
        if (embedding != null) {
            summary.put("embeddingProvider", embedding.getProvider());
            summary.put("embeddingModel", embedding.getModel());
            summary.put("embeddingDimensions", embedding.getDimensions());
            summary.put("embeddingBaseUrl", embedding.getBaseUrl());
            summary.put("embeddingKeyChanged", hasText(embedding.getApiKey()));
        }
        summary.put("embeddingKeyConfigured",
            embeddingRuntime != null && embeddingRuntime.keyStatus().configured());
        return summary;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
