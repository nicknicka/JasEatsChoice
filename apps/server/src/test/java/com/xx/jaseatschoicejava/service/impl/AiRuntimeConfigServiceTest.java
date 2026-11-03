package com.xx.jaseatschoicejava.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

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
import com.xx.jaseatschoicejava.config.AiModelConfig;
import com.xx.jaseatschoicejava.config.AiRuntimeKeyStore;
import com.xx.jaseatschoicejava.dto.AiModelCandidateDTO;
import com.xx.jaseatschoicejava.dto.AiRuntimeConfigSnapshot;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.service.AiRuntimeConfigService;
import com.xx.jaseatschoicejava.service.SystemConfigService;

import dev.langchain4j.model.chat.ChatModel;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AiRuntimeConfigServiceTest {

    private static final String CANDIDATE_KEY = "candidate-key-1234567890";

    @Mock
    private AiModelClientFactory modelFactory;

    private AIConfig aiConfig;
    private AiRuntimeConfigService service;

    @BeforeEach
    void setUp() {
        aiConfig = new AIConfig();
        when(modelFactory.providerCode()).thenReturn("openai-compatible");
        when(modelFactory.providerDisplayName()).thenReturn("OpenAI 兼容");
        when(modelFactory.knownModels()).thenReturn(List.of("gpt-4o-mini", "gpt-4o"));
        when(modelFactory.environmentConfig(aiConfig)).thenAnswer(ignored -> AiModelConfig.builder()
            .provider("openai-compatible")
            .model(firstText(aiConfig.getModel(), "gpt-4o-mini"))
            .supervisorModel(firstText(aiConfig.getSupervisorModel(), "gpt-4o-mini"))
            .visionModel(firstText(aiConfig.getVisionModel(), "gpt-4o-mini"))
            .baseUrl(aiConfig.getBaseUrl() == null ? "" : aiConfig.getBaseUrl())
            .timeoutMs(aiConfig.getTimeout() != null ? aiConfig.getTimeout() : 30000)
            .temperature(aiConfig.getTemperature() != null ? aiConfig.getTemperature() : 0.7)
            .build());
        when(modelFactory.resolveKey(null)).thenAnswer(
            ignored -> aiConfig.getApiKey() == null ? "" : aiConfig.getApiKey());
        service = new AiRuntimeConfigServiceImpl(aiConfig, null, registry(),
            new AiProviderRegistry(List.of(modelFactory)), new ObjectMapper(),
            null, null, null, null, null);
    }

    private DynamicAiModelRegistry registry() {
        return new DynamicAiModelRegistry();
    }

    private String firstText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    @Test
    void snapshotLoadsEffectiveNonSecretRuntimeConfig() throws Exception {
        aiConfig.setProvider("openai-compatible");
        aiConfig.setApiKey("secret-key-12345678901");
        aiConfig.setModel("generic-chat-model");
        aiConfig.setSupervisorModel("generic-supervisor-model");
        aiConfig.setVisionModel("generic-vision-model");
        aiConfig.setBaseUrl("");
        aiConfig.setTimeout(12345);
        aiConfig.setTemperature(0.2);

        AiRuntimeConfigSnapshot snapshot = service.getCurrentSnapshot();
        String json = new ObjectMapper().writeValueAsString(snapshot);

        assertEquals("openai-compatible", snapshot.getProvider());
        assertEquals("generic-chat-model", snapshot.getModel());
        assertEquals("generic-supervisor-model", snapshot.getSupervisorModel());
        assertEquals("generic-vision-model", snapshot.getVisionModel());
        assertEquals("", snapshot.getBaseUrl());
        assertEquals(12345, snapshot.getTimeoutMs());
        assertEquals(0.2, snapshot.getTemperature());
        assertNotNull(snapshot.getUpdatedAt());
        assertNotNull(snapshot.getSupportedModels());
        assertTrue(snapshot.getSupportedModels().contains("generic-chat-model"));
        assertTrue(snapshot.getKeyStatus().isConfigured());
        assertNotNull(snapshot.getKeyStatus().getFingerprint());
        assertFalse(json.contains("secret-key-12345678901"));
    }

    @Test
    void snapshotUsesGenericConfigAndMasksEnvironmentKey() throws Exception {
        aiConfig.setApiKey("generic-key-12345678901");
        aiConfig.setModel("generic-chat-model");
        aiConfig.setVisionModel("generic-vision-model");
        aiConfig.setBaseUrl("https://ai.example.com");
        aiConfig.setTimeout(54321);

        AiRuntimeConfigSnapshot snapshot = service.getCurrentSnapshot();
        String json = new ObjectMapper().writeValueAsString(snapshot);

        assertEquals("generic-chat-model", snapshot.getModel());
        assertEquals("generic-vision-model", snapshot.getVisionModel());
        assertEquals("https://ai.example.com", snapshot.getBaseUrl());
        assertEquals(54321, snapshot.getTimeoutMs());
        assertFalse(json.contains("generic-key-12345678901"));
    }

    @Test
    void veryShortConfiguredKeyDoesNotExposeTailAsPlaintext() throws Exception {
        aiConfig.setApiKey("abcd");

        AiRuntimeConfigSnapshot snapshot = service.getCurrentSnapshot();
        String json = new ObjectMapper().writeValueAsString(snapshot);

        assertTrue(snapshot.getKeyStatus().isConfigured());
        assertFalse(json.contains("abcd"));
    }

    @Test
    void snapshotReportsCapabilityStateFromCurrentProvider() throws Exception {
        aiConfig.setApiKey("secret-key-12345678901");
        when(modelFactory.capabilities(any(AiModelConfig.class))).thenReturn(AiCapabilitiesDTO.builder()
            .available(true)
            .provider("openai-compatible")
            .textChat(true)
            .streaming(true)
            .vision(false)
            .functionCalling(true)
            .agentic(true)
            .build());

        AiRuntimeConfigSnapshot snapshot = service.getCurrentSnapshot();
        String json = new ObjectMapper().writeValueAsString(snapshot);

        assertTrue(snapshot.getCapabilities().isAvailable());
        assertTrue(snapshot.getCapabilities().isTextChat());
        assertTrue(snapshot.getCapabilities().isStreaming());
        assertFalse(snapshot.getCapabilities().isVision());
        assertTrue(snapshot.getCapabilities().isFunctionCalling());
        assertTrue(snapshot.getCapabilities().isAgentic());
        assertFalse(json.contains("secret-key-12345678901"));
    }

    @Test
    void snapshotIncludesOnlyImplementedProviderAndKnownModels() {
        aiConfig.setModel("custom-current-model");

        AiRuntimeConfigSnapshot snapshot = service.getCurrentSnapshot();

        assertEquals(1, snapshot.getProviders().size());
        assertEquals("openai-compatible", snapshot.getProviders().get(0).getCode());
        assertTrue(snapshot.getProviders().get(0).getModels()
            .containsAll(List.of("gpt-4o-mini", "gpt-4o")));
        assertTrue(snapshot.getSupportedModels().contains("custom-current-model"));
    }

    @Test
    void connectivityTestUsesEffectiveSnapshotModel() throws Exception {
        aiConfig.setApiKey("secret-key-12345678901");
        aiConfig.setModel("generic-chat-model");
        ChatModel chatModel = mock(ChatModel.class);
        when(chatModel.chat("ping")).thenReturn("pong");
        when(modelFactory.buildChatModel(any(AiModelConfig.class), isNull())).thenReturn(chatModel);
        when(modelFactory.buildSupervisorModel(any(AiModelConfig.class), isNull()))
            .thenReturn(mock(ChatModel.class));
        when(modelFactory.buildVisionModel(any(AiModelConfig.class), isNull()))
            .thenReturn(mock(ChatModel.class));

        var result = service.testCurrentConnectivity();

        assertEquals("generic-chat-model", result.getModel());
    }

    @Test
    void connectivityTestSucceedsWithoutReturningKey() throws Exception {
        aiConfig.setApiKey("secret-key-12345678901");
        ChatModel chatModel = mock(ChatModel.class);
        when(chatModel.chat("ping")).thenReturn("pong");
        when(modelFactory.buildChatModel(any(AiModelConfig.class), isNull())).thenReturn(chatModel);
        when(modelFactory.buildSupervisorModel(any(AiModelConfig.class), isNull()))
            .thenReturn(mock(ChatModel.class));
        when(modelFactory.buildVisionModel(any(AiModelConfig.class), isNull()))
            .thenReturn(mock(ChatModel.class));

        var result = service.testCurrentConnectivity();
        String json = new ObjectMapper().writeValueAsString(result);

        assertTrue(result.isSuccess());
        assertEquals("连接成功", result.getMessage());
        assertNotNull(result.getTestedAt());
        assertFalse(json.contains("secret-key-12345678901"));
    }

    @Test
    void connectivityTestReportsFailureWithoutExposingKey() throws Exception {
        aiConfig.setApiKey("secret-key-12345678901");
        ChatModel chatModel = mock(ChatModel.class);
        when(chatModel.chat("ping")).thenThrow(new BusinessException("AI_TEST_FAILED", "模型不可用"));
        when(modelFactory.buildChatModel(any(AiModelConfig.class), isNull())).thenReturn(chatModel);
        when(modelFactory.buildSupervisorModel(any(AiModelConfig.class), isNull()))
            .thenReturn(mock(ChatModel.class));
        when(modelFactory.buildVisionModel(any(AiModelConfig.class), isNull()))
            .thenReturn(mock(ChatModel.class));

        var result = service.testCurrentConnectivity();
        String json = new ObjectMapper().writeValueAsString(result);

        assertFalse(result.isSuccess());
        assertEquals("模型不可用", result.getMessage());
        assertFalse(json.contains("secret-key-12345678901"));
    }

    @Test
    void activationUsesCandidateKeyAndStoresIt(@TempDir Path tempDir) throws Exception {
        AiRuntimeKeyStore keyStore = new AiRuntimeKeyStore(tempDir.resolve("active-key").toString());
        when(modelFactory.resolveKey(any(AiRuntimeKeyStore.class)))
            .thenAnswer(ignored -> keyStore.readKey());
        SystemConfigService systemConfigService = mock(SystemConfigService.class);
        when(systemConfigService.setConfigValue(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(true);
        AiRuntimeConfigServiceImpl activationService = new AiRuntimeConfigServiceImpl(
            aiConfig, systemConfigService, registry(), new AiProviderRegistry(List.of(modelFactory)),
            new ObjectMapper(), keyStore, null, null, null, null);

        ChatModel chatModel = mock(ChatModel.class);
        when(chatModel.chat("ping")).thenReturn("pong");
        when(modelFactory.buildChatModel(any(AiModelConfig.class), eq(CANDIDATE_KEY)))
            .thenReturn(chatModel);
        when(modelFactory.buildSupervisorModel(any(AiModelConfig.class), eq(CANDIDATE_KEY)))
            .thenReturn(mock(ChatModel.class));
        when(modelFactory.buildVisionModel(any(AiModelConfig.class), eq(CANDIDATE_KEY)))
            .thenReturn(mock(ChatModel.class));

        activationService.saveAndActivate(AiModelCandidateDTO.builder()
            .provider("openai-compatible")
            .model("chat-model")
            .supervisorModel("supervisor-model")
            .visionModel("vision-model")
            .apiKey(CANDIDATE_KEY)
            .build(), "tester");

        assertEquals(CANDIDATE_KEY, keyStore.readKey());
    }

    @Test
    void unconfiguredKeyFailsConnectivityBeforeCallingProviderModel() throws Exception {
        aiConfig.setApiKey("");

        var result = service.testCurrentConnectivity();
        String json = new ObjectMapper().writeValueAsString(result);

        assertFalse(result.isSuccess());
        assertEquals("AI key 未配置", result.getMessage());
        assertFalse(json.contains("secret"));
    }
}
