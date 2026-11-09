package com.xx.jaseatschoicejava.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

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
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiModelConfig;
import com.xx.jaseatschoicejava.config.AiRuntimeKeyStore;
import com.xx.jaseatschoicejava.dto.AiModelCandidateDTO;
import com.xx.jaseatschoicejava.dto.AiRuntimeConfigSnapshot;
import com.xx.jaseatschoicejava.service.AiConfigHistoryStore;
import com.xx.jaseatschoicejava.service.SystemConfigService;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.ChatModel;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AiRuntimeConfigMigrationServiceTest {

    private static final String CONFIG_KEY = "ai.runtime-config";
    private static final String KEY = "migration-test-key-1234567890";

    @TempDir
    Path tempDir;

    @Mock
    private SystemConfigService systemConfigService;

    @Mock
    private AiConfigHistoryStore historyStore;

    @Mock
    private AiModelClientFactory modelFactory;

    private final Map<String, String> databaseConfigs = new HashMap<>();
    private final List<String> historyRows = new java.util.ArrayList<>();
    private AIConfig aiConfig;

    @BeforeEach
    void setUp() {
        aiConfig = new AIConfig();
        aiConfig.setProvider("openai-compatible");
        aiConfig.setApiKey(KEY);
        aiConfig.setModel("env-chat");
        aiConfig.setSupervisorModel("env-supervisor");
        aiConfig.setVisionModel("env-vision");
        aiConfig.setBaseUrl("https://env.example.com");
        aiConfig.setTimeout(30000);
        aiConfig.setTemperature(0.2);
        when(modelFactory.providerCode()).thenReturn("openai-compatible");
        when(modelFactory.environmentConfig(any(AIConfig.class))).thenReturn(AiModelConfig.builder()
            .provider("openai-compatible")
            .model("env-chat")
            .supervisorModel("env-supervisor")
            .visionModel("env-vision")
            .baseUrl("https://env.example.com")
            .timeoutMs(30000)
            .temperature(0.2)
            .build());
        when(systemConfigService.getConfigsByGroup("ai")).thenAnswer(
            ignored -> new HashMap<>(databaseConfigs));
        when(systemConfigService.setConfigValue(anyString(), anyString(), anyString(), anyString()))
            .thenAnswer(invocation -> {
                databaseConfigs.put(invocation.getArgument(0), invocation.getArgument(1));
                return true;
            });
        when(historyStore.load()).thenAnswer(
            ignored -> historyRows.isEmpty() ? null : historyRows.get(historyRows.size() - 1));
        doAnswer(invocation -> {
            historyRows.add(invocation.getArgument(0));
            return null;
        }).when(historyStore).save(anyString());
        when(modelFactory.resolveKey(any())).thenReturn(KEY);
        ChatModel initialChatModel = successfulModel();
        when(modelFactory.buildChatModel(any(AiModelConfig.class), isNull()))
            .thenReturn(initialChatModel);
        when(modelFactory.buildSupervisorModel(any(AiModelConfig.class), isNull()))
            .thenReturn(mock(ChatModel.class));
        when(modelFactory.buildVisionModel(any(AiModelConfig.class), isNull()))
            .thenReturn(mock(ChatModel.class));
    }

    @Test
    void legacyProvidersAndChatCompletionsUrlMigrateOnceAndWriteBack() throws Exception {
        databaseConfigs.put(CONFIG_KEY, new ObjectMapper().writeValueAsString(AiModelConfig.builder()
            .provider("zhipu")
            .embeddingProvider("zhipu")
            .model("legacy-chat")
            .supervisorModel("legacy-supervisor")
            .visionModel("legacy-vision")
            .baseUrl("https://legacy.example.com/v1/chat/completions")
            .embeddingBaseUrl("https://legacy.example.com/v1/chat/completions")
            .timeoutMs(30000)
            .temperature(0.2)
            .build()));

        AiRuntimeConfigServiceImpl runtime = service();
        runtime.initialize();
        runtime.initialize();
        AiRuntimeConfigSnapshot snapshot = runtime.getCurrentSnapshot();

        assertEquals("openai-compatible", snapshot.getProvider());
        assertTrue(databaseConfigs.get(CONFIG_KEY).contains("\"provider\":\"openai-compatible\""));
        assertTrue(databaseConfigs.get(CONFIG_KEY).contains("\"embeddingProvider\":\"openai-compatible\""));
        assertTrue(databaseConfigs.get(CONFIG_KEY).contains("\"baseUrl\":\"https://legacy.example.com/v1\""));
        verify(systemConfigService, org.mockito.Mockito.times(1))
            .setConfigValue(org.mockito.ArgumentMatchers.eq(CONFIG_KEY), anyString(),
                anyString(), anyString());
    }

    @Test
    void legacyZhipuModelNamesAreNeutralizedWithReconfigurationNotice() throws Exception {
        databaseConfigs.put(CONFIG_KEY, new ObjectMapper().writeValueAsString(AiModelConfig.builder()
            .provider("zhipu")
            .embeddingProvider("zhipu")
            .model("glm-4")
            .supervisorModel("glm-4-plus")
            .visionModel("glm-4v")
            .embeddingModel("glm-embedding")
            .baseUrl("https://legacy.example.com/v1")
            .embeddingBaseUrl("https://legacy.example.com/v1")
            .timeoutMs(30000)
            .temperature(0.2)
            .build()));

        AiRuntimeConfigServiceImpl runtime = service();
        runtime.initialize();
        runtime.initialize();
        AiRuntimeConfigSnapshot snapshot = runtime.getCurrentSnapshot();

        assertEquals("openai-compatible", snapshot.getProvider());
        assertEquals("gpt-4o-mini", snapshot.getModel());
        assertEquals("gpt-4o-mini", snapshot.getSupervisorModel());
        assertEquals("gpt-4o-mini", snapshot.getVisionModel());
        assertTrue(snapshot.isNeedsReconfiguration());
        assertTrue(snapshot.getConfigurationNotice().contains("重新"));
        assertFalse(databaseConfigs.get(CONFIG_KEY).contains("glm-"));
        verify(systemConfigService, org.mockito.Mockito.times(1))
            .setConfigValue(org.mockito.ArgumentMatchers.eq(CONFIG_KEY), anyString(),
                anyString(), anyString());
    }

    @Test
    void legacyHistoryIsReadableThroughSummaryAndMigratedOnWrite() throws Exception {
        historyRows.add(new ObjectMapper().writeValueAsString(
            com.xx.jaseatschoicejava.dto.AiRuntimeConfigHistory.builder()
                .activeVersion(1L)
                .activeUpdatedAt("2026-01-01T00:00:00")
                .history(List.of(com.xx.jaseatschoicejava.dto.AiRuntimeConfigHistory.Entry.builder()
                    .version(1L)
                    .updatedAt("2026-01-01T00:00:00")
                    .config(AiModelConfig.builder()
                        .provider("zhipu")
                        .model("legacy-chat")
                        .supervisorModel("legacy-supervisor")
                        .visionModel("legacy-vision")
                        .baseUrl("https://legacy.example.com/v1")
                        .timeoutMs(30000)
                        .temperature(0.2)
                        .build())
                    .build()))
                .build()));

        AiRuntimeConfigServiceImpl runtime = service();
        runtime.initialize();

        assertEquals("openai-compatible",
            runtime.getPreviousConfigSummary().getProvider());
        assertTrue(historyRows.get(historyRows.size() - 1).contains("\"provider\":\"openai-compatible\""));
    }

    @Test
    void invalidLegacyBaseUrlIsClearedAndConfigurationNoticeSurvivesRestart() throws Exception {
        databaseConfigs.put(CONFIG_KEY, new ObjectMapper().writeValueAsString(AiModelConfig.builder()
            .provider("zhipu")
            .model("legacy-chat")
            .supervisorModel("legacy-supervisor")
            .visionModel("legacy-vision")
            .baseUrl("https:///chat/completions")
            .timeoutMs(30000)
            .temperature(0.2)
            .build()));

        AiRuntimeConfigServiceImpl runtime = service();
        runtime.initialize();
        AiRuntimeConfigSnapshot migrated = runtime.getCurrentSnapshot();
        assertEquals("", migrated.getBaseUrl());
        assertTrue(migrated.isNeedsReconfiguration());
        assertTrue(migrated.getConfigurationNotice().contains("重新填写"));

        runtime.initialize();
        AiRuntimeConfigSnapshot restarted = runtime.getCurrentSnapshot();
        assertEquals("", restarted.getBaseUrl());
        assertTrue(restarted.isNeedsReconfiguration());
        assertTrue(restarted.getConfigurationNotice().contains("重新填写"));
    }

    @Test
    void failedVisionVerificationIsPersistedAndRestoredAfterRestart() throws Exception {
        AiRuntimeConfigServiceImpl first = service();
        ChatModel chat = successfulModel();
        ChatModel vision = mock(ChatModel.class);
        when(vision.chat(any(UserMessage.class))).thenThrow(new RuntimeException("视觉模型不支持图片"));
        when(modelFactory.buildChatModel(any(AiModelConfig.class), any()))
            .thenReturn(chat);
        when(modelFactory.buildSupervisorModel(any(AiModelConfig.class), any()))
            .thenReturn(mock(ChatModel.class));
        when(modelFactory.buildVisionModel(any(AiModelConfig.class), any()))
            .thenReturn(vision);

        AiRuntimeConfigSnapshot saved = first.saveAndActivate(candidate(), "admin");
        assertFalse(saved.isVisionVerified());
        assertTrue(saved.getVisionVerificationReason().contains("视觉模型不支持图片"));

        AiRuntimeConfigServiceImpl restarted = service();
        restarted.initialize();
        AiRuntimeConfigSnapshot restored = restarted.getCurrentSnapshot();
        assertFalse(restored.isVisionVerified());
        assertTrue(restored.getVisionVerificationReason().contains("视觉模型不支持图片"));
    }

    @Test
    void successfulVisionVerificationIsPersistedAfterRestart() throws Exception {
        AiRuntimeConfigServiceImpl first = service();
        ChatModel candidateModel = successfulModel();
        ChatModel visionModel = successfulModel();
        when(modelFactory.buildChatModel(any(AiModelConfig.class), any()))
            .thenReturn(candidateModel);
        when(modelFactory.buildSupervisorModel(any(AiModelConfig.class), any()))
            .thenReturn(mock(ChatModel.class));
        when(modelFactory.buildVisionModel(any(AiModelConfig.class), any()))
            .thenReturn(visionModel);

        AiRuntimeConfigSnapshot saved = first.saveAndActivate(candidate(), "admin");
        assertTrue(saved.isVisionVerified());
        assertEquals("", saved.getVisionVerificationReason());

        AiRuntimeConfigServiceImpl restarted = service();
        restarted.initialize();
        assertTrue(restarted.getCurrentSnapshot().isVisionVerified());
    }

    private AiRuntimeConfigServiceImpl service() {
        return new AiRuntimeConfigServiceImpl(aiConfig, systemConfigService,
            new DynamicAiModelRegistry(), new AiProviderRegistry(List.of(modelFactory)),
            new ObjectMapper(), new AiRuntimeKeyStore(tempDir.resolve("active-key").toString()),
            null, historyStore, null, null);
    }

    private AiModelCandidateDTO candidate() {
        return AiModelCandidateDTO.builder()
            .provider("openai-compatible")
            .model("candidate-chat")
            .supervisorModel("candidate-supervisor")
            .visionModel("candidate-vision")
            .baseUrl("https://candidate.example.com/v1")
            .timeoutMs(30000)
            .temperature(0.2)
            .apiKey(KEY)
            .build();
    }

    private ChatModel successfulModel() {
        ChatModel model = mock(ChatModel.class, withSettings().lenient());
        org.mockito.Mockito.doReturn("pong").when(model).chat("ping");
        org.mockito.Mockito.doReturn(ChatResponse.builder()
            .aiMessage(AiMessage.from("OK"))
            .build()).when(model).chat(any(UserMessage.class));
        return model;
    }
}
