package com.xx.jaseatschoicejava.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.agent.config.DynamicAiModelRegistry;
import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiEmbeddingRuntimeKeyStore;
import com.xx.jaseatschoicejava.config.AiModelConfig;
import com.xx.jaseatschoicejava.config.AiRuntimeKeyStore;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import com.xx.jaseatschoicejava.dto.AiEmbeddingCandidateDTO;
import com.xx.jaseatschoicejava.dto.AiModelCandidateDTO;
import com.xx.jaseatschoicejava.dto.AiRuntimeConfigSnapshot;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.rag.AnnouncementEmbeddingConfig;
import com.xx.jaseatschoicejava.service.AnnouncementEmbeddingRuntime;
import com.xx.jaseatschoicejava.service.SystemConfigService;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * AI 运行时配置服务边界上的公告 Embedding 行为：快照可见、切换生效、换模型拒绝、密钥槽位独立、重启恢复。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AiRuntimeConfigEmbeddingServiceTest {

    private static final String CONFIG_KEY = "ai.runtime-config";
    private static final String CHAT_KEY = "chat-runtime-key";
    private static final String EMBEDDING_KEY = "embedding-runtime-key";

    @Mock
    private AiModelClientFactory modelFactory;

    @Mock
    private SystemConfigService systemConfigService;

    @TempDir
    Path tempDir;

    private AIConfig aiConfig;
    private ZhipuAIConfig zhipuConfig;
    private DynamicAiModelRegistry registry;
    private Map<String, String> databaseConfigs;
    private AiRuntimeKeyStore chatKeyStore;
    private AiEmbeddingRuntimeKeyStore embeddingKeyStore;
    private StubEmbeddingRuntime embeddingRuntime;

    @BeforeEach
    void setUp() {
        aiConfig = new AIConfig();
        aiConfig.setProvider("zhipu");
        aiConfig.setModel("glm-4-flash");
        aiConfig.setSupervisorModel("glm-4-flash");
        aiConfig.setVisionModel("glm-4.6v-flash");
        aiConfig.setApiKey("");
        zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("");
        zhipuConfig.setTimeout(30000);
        databaseConfigs = new HashMap<>();
        registry = new DynamicAiModelRegistry();
        chatKeyStore = new AiRuntimeKeyStore(tempDir.resolve("chat-active-key").toString());
        embeddingKeyStore = new AiEmbeddingRuntimeKeyStore(
            tempDir.resolve("embedding-active-key").toString());
        embeddingRuntime = new StubEmbeddingRuntime(embeddingKeyStore);

        when(modelFactory.providerCode()).thenReturn("zhipu");
        when(modelFactory.providerDisplayName()).thenReturn("智谱");
        when(modelFactory.knownModels()).thenReturn(List.of("glm-4-flash"));
        when(modelFactory.environmentConfig(aiConfig)).thenReturn(AiModelConfig.builder()
            .provider("zhipu")
            .model("glm-4-flash")
            .supervisorModel("glm-4-flash")
            .visionModel("glm-4.6v-flash")
            .baseUrl("")
            .timeoutMs(30000)
            .temperature(0.7)
            .build());
        when(modelFactory.capabilities(any(AiModelConfig.class)))
            .thenReturn(AiCapabilitiesDTO.builder().available(true).build());
        when(modelFactory.resolveKey(any())).thenReturn(CHAT_KEY);
        ChatModel chatModel = mock(ChatModel.class);
        lenient().when(chatModel.chat("ping")).thenReturn("pong");
        when(modelFactory.buildChatModel(any(AiModelConfig.class), isNull())).thenReturn(chatModel);
        when(modelFactory.buildSupervisorModel(any(AiModelConfig.class), isNull())).thenReturn(chatModel);
        when(modelFactory.buildVisionModel(any(AiModelConfig.class), isNull())).thenReturn(chatModel);
        when(modelFactory.buildStreamingModel(any(AiModelConfig.class)))
            .thenReturn(mock(StreamingChatModel.class));
        when(systemConfigService.getConfigsByGroup("ai"))
            .thenAnswer(ignored -> new HashMap<>(databaseConfigs));
        when(systemConfigService.setConfigValue(anyString(), anyString(), anyString(), anyString()))
            .thenAnswer(invocation -> {
                databaseConfigs.put(invocation.getArgument(0), invocation.getArgument(1));
                return true;
            });
    }

    @Test
    void snapshotExposesEmbeddingConfigAndKeyStatus() {
        AiRuntimeConfigSnapshot snapshot = service().getCurrentSnapshot();

        assertNotNull(snapshot.getEmbedding());
        assertEquals("zhipu", snapshot.getEmbedding().getProvider());
        assertEquals("embedding-3", snapshot.getEmbedding().getModel());
        assertEquals(2048, snapshot.getEmbedding().getDimensions());
        assertEquals(List.of("zhipu", "openai-compatible"), snapshot.getEmbedding().getProviders());
        assertFalse(snapshot.getEmbedding().getKeyStatus().isConfigured());
    }

    @Test
    void savingNewAddressAppliesToRetrievalWithoutTouchingChatKey() {
        AiRuntimeConfigServiceImpl service = service();

        AiRuntimeConfigSnapshot snapshot = service.saveAndActivate(candidate(embedding(
            "zhipu", "embedding-3", 2048, "https://embedding.example.com/v1", EMBEDDING_KEY)), "admin");

        assertEquals(1, embeddingRuntime.validateCount);
        assertEquals(EMBEDDING_KEY, embeddingRuntime.lastCandidateKey);
        assertEquals("https://embedding.example.com/v1", embeddingRuntime.activated.baseUrl());
        assertEquals("https://embedding.example.com/v1", snapshot.getEmbedding().getBaseUrl());
        assertEquals(EMBEDDING_KEY, embeddingKeyStore.readKey());
        assertFalse(chatKeyStore.exists());
        assertTrue(databaseConfigs.get(CONFIG_KEY).contains("https://embedding.example.com/v1"));
    }

    @Test
    void chatOnlyChangeKeepsEmbeddingConfigAndSkipsEmbeddingProbe() {
        AiRuntimeConfigServiceImpl service = service();
        service.saveAndActivate(candidate(embedding(
            "zhipu", "embedding-3", 2048, "https://embedding.example.com/v1", EMBEDDING_KEY)), "admin");
        embeddingRuntime.validateCount = 0;

        AiModelCandidateDTO chatOnly = candidate(null);
        chatOnly.setModel("glm-4-flash");
        chatOnly.setApiKey("");

        AiRuntimeConfigSnapshot snapshot = service.saveAndActivate(chatOnly, "admin");

        assertEquals(0, embeddingRuntime.validateCount);
        assertEquals("https://embedding.example.com/v1", snapshot.getEmbedding().getBaseUrl());
    }

    @Test
    void switchingModelIsRejectedUntilIndexRebuildExists() {
        AiRuntimeConfigServiceImpl service = service();

        BusinessException error = assertThrows(BusinessException.class, () -> service.saveAndActivate(
            candidate(embedding("zhipu", "bge-m3", 2048, "", EMBEDDING_KEY)), "admin"));

        assertEquals("EMBEDDING_MODEL_REBUILD_REQUIRED", error.getCode());
        assertEquals(0, embeddingRuntime.validateCount);
        assertEquals("", embeddingRuntime.activated.baseUrl());
        assertNull(databaseConfigs.get(CONFIG_KEY));
        assertFalse(embeddingKeyStore.exists());
    }

    @Test
    void failedEmbeddingValidationDoesNotSwitchConfig() {
        embeddingRuntime.failValidation = true;
        AiRuntimeConfigServiceImpl service = service();

        BusinessException error = assertThrows(BusinessException.class, () -> service.saveAndActivate(
            candidate(embedding("zhipu", "embedding-3", 2048, "https://broken.example.com", EMBEDDING_KEY)),
            "admin"));

        assertEquals("AI_CONFIG_TEST_FAILED", error.getCode());
        assertEquals("", embeddingRuntime.activated.baseUrl());
        assertNull(databaseConfigs.get(CONFIG_KEY));
        assertFalse(embeddingKeyStore.exists());
    }

    @Test
    void embeddingKeyRotationDoesNotTouchChatKeySlot() throws Exception {
        chatKeyStore.writeKey("current-chat-key");
        AiRuntimeConfigServiceImpl service = service();

        service.saveAndActivate(candidate(embedding(
            "zhipu", "embedding-3", 2048, "", "rotated-embedding-key")), "admin");

        assertEquals("current-chat-key", chatKeyStore.readKey());
        assertEquals("rotated-embedding-key", embeddingKeyStore.readKey());
    }

    @Test
    void rollbackRestoresPreviousEmbeddingConfigThroughSamePath() {
        AiRuntimeConfigServiceImpl service = service();
        service.saveAndActivate(candidate(embedding(
            "zhipu", "embedding-3", 2048, "https://first.example.com/v1", EMBEDDING_KEY)), "admin");
        service.saveAndActivate(candidate(embedding(
            "zhipu", "embedding-3", 2048, "https://second.example.com/v1", "")), "admin");
        embeddingRuntime.activated = null;

        AiRuntimeConfigSnapshot snapshot = service.rollbackToPrevious("admin");

        assertEquals("https://first.example.com/v1", embeddingRuntime.activated.baseUrl());
        assertEquals("https://first.example.com/v1", snapshot.getEmbedding().getBaseUrl());
        assertTrue(databaseConfigs.get(CONFIG_KEY).contains("https://first.example.com/v1"));
    }

    @Test
    void restartRecoversPersistedEmbeddingConfig() {
        service().saveAndActivate(candidate(embedding(
            "openai-compatible", "embedding-3", 2048, "http://localhost:11434/v1", "")), "admin");
        StubEmbeddingRuntime restartedRuntime = new StubEmbeddingRuntime(embeddingKeyStore);

        AiRuntimeConfigServiceImpl restarted = new AiRuntimeConfigServiceImpl(
            aiConfig, systemConfigService, new DynamicAiModelRegistry(),
            new AiProviderRegistry(List.of(modelFactory)), new ObjectMapper(), chatKeyStore, null, null,
            restartedRuntime, embeddingKeyStore);
        restarted.initialize();

        AiRuntimeConfigSnapshot snapshot = restarted.getCurrentSnapshot();
        assertEquals("openai-compatible", snapshot.getEmbedding().getProvider());
        assertEquals("http://localhost:11434/v1", snapshot.getEmbedding().getBaseUrl());
        assertEquals("http://localhost:11434/v1", restartedRuntime.activated.baseUrl());
    }

    @Test
    void auditRecordsEmbeddingChange() {
        AiConfigAuditServiceImpl auditService = mock(AiConfigAuditServiceImpl.class);
        AiRuntimeConfigServiceImpl service = new AiRuntimeConfigServiceImpl(
            aiConfig, systemConfigService, registry, new AiProviderRegistry(List.of(modelFactory)),
            new ObjectMapper(), chatKeyStore, auditService, null, embeddingRuntime, embeddingKeyStore);
        service.initialize();

        service.saveAndActivate(candidate(embedding(
            "zhipu", "embedding-3", 2048, "https://audited.example.com/v1", EMBEDDING_KEY)), "admin");

        verify(auditService, org.mockito.Mockito.atLeastOnce())
            .record(org.mockito.ArgumentMatchers.argThat(event ->
            event.getNewSummary().toString().contains("https://audited.example.com/v1")));
    }

    private AiRuntimeConfigServiceImpl service() {
        AiRuntimeConfigServiceImpl service = new AiRuntimeConfigServiceImpl(
            aiConfig, systemConfigService, registry, new AiProviderRegistry(List.of(modelFactory)),
            new ObjectMapper(), chatKeyStore, null, null, embeddingRuntime, embeddingKeyStore);
        service.initialize();
        return service;
    }

    private AiModelCandidateDTO candidate(AiEmbeddingCandidateDTO embedding) {
        return AiModelCandidateDTO.builder()
            .provider("zhipu")
            .model("glm-4-flash")
            .supervisorModel("glm-4-flash")
            .visionModel("glm-4.6v-flash")
            .baseUrl("")
            .timeoutMs(30000)
            .temperature(0.7)
            .embedding(embedding)
            .build();
    }

    private AiEmbeddingCandidateDTO embedding(
            String provider, String model, int dimensions, String baseUrl, String apiKey) {
        return AiEmbeddingCandidateDTO.builder()
            .provider(provider)
            .model(model)
            .dimensions(dimensions)
            .baseUrl(baseUrl)
            .apiKey(apiKey)
            .build();
    }

    /**
     * 测试替身：只保留环境兜底合并、激活记录与校验计数，维度比对本身由
     * {@link com.xx.jaseatschoicejava.rag.AnnouncementRagEmbeddingRuntimeTest} 覆盖。
     */
    private static final class StubEmbeddingRuntime implements AnnouncementEmbeddingRuntime {

        private final AnnouncementEmbeddingConfig baseline =
            new AnnouncementEmbeddingConfig("zhipu", "embedding-3", 2048, "");
        private final AiEmbeddingRuntimeKeyStore keyStore;

        private AnnouncementEmbeddingConfig activated;
        private int validateCount;
        private String lastCandidateKey;
        private boolean failValidation;

        private StubEmbeddingRuntime(AiEmbeddingRuntimeKeyStore keyStore) {
            this.keyStore = keyStore;
        }

        @Override
        public AnnouncementEmbeddingConfig effective(AnnouncementEmbeddingConfig persisted) {
            if (persisted == null) {
                return baseline;
            }
            return new AnnouncementEmbeddingConfig(
                text(persisted.provider()) ? persisted.provider() : baseline.provider(),
                text(persisted.model()) ? persisted.model() : baseline.model(),
                persisted.dimensions() > 0 ? persisted.dimensions() : baseline.dimensions(),
                persisted.baseUrl() != null ? persisted.baseUrl() : baseline.baseUrl());
        }

        @Override
        public void validate(AnnouncementEmbeddingConfig candidate, String candidateKey) {
            validateCount++;
            lastCandidateKey = candidateKey;
            if (failValidation) {
                throw new BusinessException("EMBEDDING_CONNECTIVITY_FAILED", "公告 Embedding 连通性校验失败");
            }
        }

        @Override
        public void activate(AnnouncementEmbeddingConfig config) {
            activated = config;
        }

        @Override
        public List<String> providers() {
            return List.of("zhipu", "openai-compatible");
        }

        @Override
        public KeyStatus keyStatus() {
            String key = keyStore == null ? "" : keyStore.readKey();
            if (key.isBlank()) {
                return new KeyStatus(false, null, null);
            }
            return new KeyStatus(true, "fingerprint", keyStore.updatedAt());
        }

        private boolean text(String value) {
            return value != null && !value.isBlank();
        }
    }
}
