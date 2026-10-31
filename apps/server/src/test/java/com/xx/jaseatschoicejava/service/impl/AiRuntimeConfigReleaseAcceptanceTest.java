package com.xx.jaseatschoicejava.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.nio.file.Files;
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
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import com.xx.jaseatschoicejava.dto.AiModelCandidateDTO;
import com.xx.jaseatschoicejava.dto.AiRuntimeConfigSnapshot;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.service.AiConfigHistoryStore;
import com.xx.jaseatschoicejava.service.SystemConfigService;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AiRuntimeConfigReleaseAcceptanceTest {

    private static final String RUNTIME_KEY = "release-runtime-key-0987654321";
    private static final String BOOTSTRAP_KEY = "release-bootstrap-key-0123456789";
    private static final String INVALID_KEY = "release-invalid-key-1234567890";
    private static final String RUNTIME_FINGERPRINT = "8d43aa55aa4f";
    private static final String BOOTSTRAP_FINGERPRINT = "14c3888cde2e";

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
    private ZhipuAIConfig zhipuConfig;

    @BeforeEach
    void setUp() {
        aiConfig = new AIConfig();
        aiConfig.setProvider("zhipu");
        aiConfig.setModel("env-chat");
        aiConfig.setSupervisorModel("env-supervisor");
        aiConfig.setVisionModel("env-vision");
        aiConfig.setBaseUrl("https://bootstrap.example.com");
        aiConfig.setTimeout(31000);
        aiConfig.setTemperature(0.2);

        zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("");
        zhipuConfig.setModel("");
        zhipuConfig.setVisionModel("");
        zhipuConfig.setBaseUrl("");
        zhipuConfig.setTimeout(null);

        databaseConfigs.clear();
        historyRows.clear();
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
        when(modelFactory.buildSupervisorModel(any(AiModelConfig.class))).thenReturn(mock(ChatModel.class));
        when(modelFactory.buildVisionModel(any(AiModelConfig.class))).thenReturn(mock(ChatModel.class));
        when(modelFactory.buildStreamingModel(any(AiModelConfig.class))).thenReturn(mock(StreamingChatModel.class));
        ChatModel initialChatModel = successfulModel();
        when(modelFactory.buildChatModel(any(AiModelConfig.class))).thenReturn(initialChatModel);
    }

    @Test
    void invalidModelAndProviderFailurePreserveCurrentConfigAndRuntimeKey() throws Exception {
        Path keyPath = newKeyPath();
        AiRuntimeConfigServiceImpl service = service(keyPath);
        keyStore(keyPath).writeKey(RUNTIME_KEY);
        when(modelFactory.buildChatModel(any(AiModelConfig.class), eq(RUNTIME_KEY)))
            .thenThrow(new IllegalStateException("provider rejected model"));

        BusinessException exception = assertThrows(BusinessException.class,
            () -> service.saveAndActivate(candidate("not-a-real-model")));

        assertEquals("AI_CONFIG_TEST_FAILED", exception.getCode());
        AiRuntimeConfigSnapshot snapshot = service.getCurrentSnapshot();
        assertEquals("env-chat", snapshot.getModel());
        assertEquals(1L, snapshot.getVersion());
        assertEquals(RUNTIME_FINGERPRINT, snapshot.getKeyStatus().getFingerprint());
        assertFalse(databaseConfigs.containsKey("ai.runtime-config"));
        assertEquals(RUNTIME_KEY, Files.readString(keyPath));
        assertFalse(serialize(snapshot).contains(RUNTIME_KEY));
        assertFalse(serialize(snapshot).contains(INVALID_KEY));
    }

    @Test
    void hotSwitchRestartAndRollbackRestoreRuntimeKeyAndActiveConfig() throws Exception {
        Path keyPath = newKeyPath();
        AiRuntimeConfigServiceImpl service = service(keyPath);
        keyStore(keyPath).writeKey(RUNTIME_KEY);
        ChatModel candidateChatModel = successfulModel();
        when(modelFactory.buildChatModel(any(AiModelConfig.class), eq(RUNTIME_KEY)))
            .thenReturn(candidateChatModel);

        AiRuntimeConfigSnapshot activated = service.saveAndActivate(candidate("release-chat-one"));
        assertEquals("release-chat-one", activated.getModel());
        assertEquals(2L, activated.getVersion());
        assertEquals(RUNTIME_FINGERPRINT, activated.getKeyStatus().getFingerprint());

        AiRuntimeConfigSnapshot switched = service.saveAndActivate(candidate("release-chat-two"));
        assertEquals("release-chat-two", switched.getModel());
        assertEquals(3L, switched.getVersion());

        AiRuntimeConfigServiceImpl restartedService = service(keyPath);
        restartedService.initialize();
        AiRuntimeConfigSnapshot restarted = restartedService.getCurrentSnapshot();
        assertEquals("release-chat-two", restarted.getModel());
        assertEquals(3L, restarted.getVersion());
        assertEquals(RUNTIME_FINGERPRINT, restarted.getKeyStatus().getFingerprint());

        AiRuntimeConfigSnapshot rollback = restartedService.rollbackToPrevious("13800000000");
        assertEquals("release-chat-one", rollback.getModel());
        assertEquals(2L, rollback.getVersion());
        assertEquals(RUNTIME_FINGERPRINT, rollback.getKeyStatus().getFingerprint());

        assertFalse(serialize(databaseConfigs).contains(RUNTIME_KEY));
        assertFalse(serialize(historyRows).contains(RUNTIME_KEY));
        assertFalse(serialize(restartedService.getCurrentSnapshot()).contains(RUNTIME_KEY));
    }

    @Test
    void deletedRuntimeKeyFallsBackToEnvironmentKeyAfterRestart() throws Exception {
        Path keyPath = newKeyPath();
        AiRuntimeConfigServiceImpl service = service(keyPath);
        keyStore(keyPath).writeKey(RUNTIME_KEY);
        ChatModel candidateChatModel = successfulModel();
        when(modelFactory.buildChatModel(any(AiModelConfig.class), eq(RUNTIME_KEY)))
            .thenReturn(candidateChatModel);
        service.saveAndActivate(candidate("release-chat-one"));

        Files.deleteIfExists(keyPath);
        aiConfig.setApiKey(BOOTSTRAP_KEY);
        AiRuntimeConfigServiceImpl restartedService = service(keyPath);
        restartedService.initialize();

        AiRuntimeConfigSnapshot snapshot = restartedService.getCurrentSnapshot();
        assertEquals("release-chat-one", snapshot.getModel());
        assertEquals(2L, snapshot.getVersion());
        assertEquals(BOOTSTRAP_FINGERPRINT, snapshot.getKeyStatus().getFingerprint());
        assertFalse(serialize(snapshot).contains(BOOTSTRAP_KEY));
        assertFalse(serialize(snapshot).contains(RUNTIME_KEY));
        assertFalse(serialize(databaseConfigs).contains(BOOTSTRAP_KEY));
        assertFalse(serialize(databaseConfigs).contains(RUNTIME_KEY));
    }

    private AiRuntimeConfigServiceImpl service(Path keyPath) {
        return new AiRuntimeConfigServiceImpl(aiConfig, zhipuConfig, null, systemConfigService,
            new DynamicAiModelRegistry(), modelFactory, new ObjectMapper(),
            new AiRuntimeKeyStore(keyPath.toString()), null, historyStore);
    }

    private AiRuntimeKeyStore keyStore(Path keyPath) {
        return new AiRuntimeKeyStore(keyPath.toString());
    }

    private Path newKeyPath() {
        return tempDir.resolve("active-key-" + System.nanoTime());
    }

    private AiModelCandidateDTO candidate(String model) {
        return candidate(model, RUNTIME_KEY);
    }

    private AiModelCandidateDTO candidate(String model, String apiKey) {
        return AiModelCandidateDTO.builder()
            .provider("zhipu")
            .model(model)
            .supervisorModel(model)
            .visionModel("glm-4.6v-flash")
            .baseUrl("https://candidate.example.com")
            .timeoutMs(30000)
            .temperature(0.7)
            .apiKey(apiKey)
            .build();
    }

    private ChatModel successfulModel() {
        ChatModel chatModel = mock(ChatModel.class, withSettings().lenient());
        org.mockito.Mockito.doReturn("pong").when(chatModel).chat("ping");
        return chatModel;
    }

    private String serialize(Object value) throws Exception {
        return new ObjectMapper().writeValueAsString(value);
    }
}
