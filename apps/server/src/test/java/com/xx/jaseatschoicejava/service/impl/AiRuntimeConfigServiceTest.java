package com.xx.jaseatschoicejava.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;


import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiModelConfig;
import com.xx.jaseatschoicejava.config.AiRuntimeKeyStore;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import com.xx.jaseatschoicejava.agent.config.DynamicAiModelRegistry;
import com.xx.jaseatschoicejava.dto.AiModelCandidateDTO;
import com.xx.jaseatschoicejava.dto.AiRuntimeConfigSnapshot;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.service.AiRuntimeConfigService;
import com.xx.jaseatschoicejava.service.SystemConfigService;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AiRuntimeConfigServiceTest {

    private static final String CANDIDATE_KEY = "candidate-key-1234567890";

    @Mock
    private AiModelClientFactory modelFactory;

    private AIConfig aiConfig;
    private ZhipuAIConfig zhipuConfig;
    private AiRuntimeConfigService service;

    @BeforeEach
    void setUp() {
        aiConfig = new AIConfig();
        zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setModel("");
        zhipuConfig.setVisionModel("");
        zhipuConfig.setBaseUrl("");
        zhipuConfig.setTimeout(null);
        when(modelFactory.providerCode()).thenReturn("zhipu");
        when(modelFactory.providerDisplayName()).thenReturn("智谱");
        when(modelFactory.knownModels()).thenReturn(java.util.List.of(
            "glm-4-flash", "glm-4.6v-flash", "glm-4.1v-thinking-flash"));
        when(modelFactory.environmentConfig(aiConfig)).thenAnswer(ignored -> AiModelConfig.builder()
            .provider("zhipu")
            .model(zhipuConfig.getModel() != null && !zhipuConfig.getModel().isBlank()
                ? zhipuConfig.getModel()
                : (aiConfig.getModel() != null && !aiConfig.getModel().isBlank()
                    ? aiConfig.getModel() : "glm-4-flash"))
            .supervisorModel(aiConfig.getSupervisorModel() != null
                ? aiConfig.getSupervisorModel() : "glm-4-flash")
            .visionModel(zhipuConfig.getVisionModel() != null && !zhipuConfig.getVisionModel().isBlank()
                ? zhipuConfig.getVisionModel()
                : (aiConfig.getVisionModel() != null ? aiConfig.getVisionModel() : "glm-4.1v-thinking-flash"))
            .baseUrl(zhipuConfig.getBaseUrl() != null && !zhipuConfig.getBaseUrl().isBlank()
                ? zhipuConfig.getBaseUrl() : aiConfig.getBaseUrl())
            .timeoutMs(zhipuConfig.getTimeout() != null ? zhipuConfig.getTimeout()
                : (aiConfig.getTimeout() != null ? aiConfig.getTimeout() : 30000))
            .temperature(aiConfig.getTemperature() != null ? aiConfig.getTemperature() : 0.7)
            .build());
        when(modelFactory.resolveKey(null)).thenAnswer(ignored -> {
            if (zhipuConfig.getApiKey() != null && !zhipuConfig.getApiKey().isBlank()) {
                return zhipuConfig.getApiKey();
            }
            return aiConfig.getApiKey() == null ? "" : aiConfig.getApiKey();
        });
        service = new AiRuntimeConfigServiceImpl(aiConfig, null, registry(),
            new AiProviderRegistry(java.util.List.of(modelFactory)), new ObjectMapper(), null, null, null, null, null);
    }

    private DynamicAiModelRegistry registry() {
        return new DynamicAiModelRegistry();
    }

    @Test
    void snapshotLoadsEffectiveNonSecretRuntimeConfig() throws Exception {
        aiConfig.setProvider("zhipu");
        aiConfig.setApiKey("secret-key-12345678901");
        aiConfig.setModel("generic-chat-model");
        aiConfig.setSupervisorModel("generic-supervisor-model");
        aiConfig.setVisionModel("generic-vision-model");
        aiConfig.setBaseUrl("");
        aiConfig.setTimeout(12345);
        aiConfig.setTemperature(0.2);

        AiRuntimeConfigSnapshot snapshot = service.getCurrentSnapshot();
        String json = new ObjectMapper().writeValueAsString(snapshot);

        assertEquals("zhipu", snapshot.getProvider());
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
    void snapshotPrefersProviderSpecificValuesAndMasksProviderKey() throws Exception {
        aiConfig.setApiKey("generic-key-12345678901");
        zhipuConfig.setApiKey("zhipu-key-98765432109");
        zhipuConfig.setModel("zhipu-chat-model");
        zhipuConfig.setVisionModel("zhipu-vision-model");
        zhipuConfig.setBaseUrl("https://ai.example.com");
        zhipuConfig.setTimeout(54321);

        AiRuntimeConfigSnapshot snapshot = service.getCurrentSnapshot();
        String json = new ObjectMapper().writeValueAsString(snapshot);

        assertEquals("zhipu-chat-model", snapshot.getModel());
        assertEquals("zhipu-vision-model", snapshot.getVisionModel());
        assertEquals("https://ai.example.com", snapshot.getBaseUrl());
        assertEquals(54321, snapshot.getTimeoutMs());
        assertFalse(json.contains("zhipu-key-98765432109"));
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
            .provider("zhipu")
            .textChat(true)
            .streaming(true)
            .vision(true)
            .functionCalling(true)
            .agentic(true)
            .build());

        AiRuntimeConfigSnapshot snapshot = service.getCurrentSnapshot();
        String json = new ObjectMapper().writeValueAsString(snapshot);

        assertTrue(snapshot.getCapabilities().isAvailable());
        assertTrue(snapshot.getCapabilities().isTextChat());
        assertTrue(snapshot.getCapabilities().isStreaming());
        assertTrue(snapshot.getCapabilities().isVision());
        assertTrue(snapshot.getCapabilities().isFunctionCalling());
        assertTrue(snapshot.getCapabilities().isAgentic());
        assertFalse(json.contains("secret-key-12345678901"));
    }

    @Test
    void snapshotIncludesImplementedProvidersAndKnownModels() {
        aiConfig.setModel("custom-current-model");

        AiRuntimeConfigSnapshot snapshot = service.getCurrentSnapshot();

        assertEquals(1, snapshot.getProviders().size());
        assertEquals("zhipu", snapshot.getProviders().get(0).getCode());
        assertTrue(snapshot.getProviders().get(0).getModels()
            .containsAll(List.of("glm-4-flash", "glm-4.6v-flash", "glm-4.1v-thinking-flash")));
        assertTrue(snapshot.getSupportedModels().contains("custom-current-model"));
    }

    @Test
    void connectivityTestUsesEffectiveSnapshotModel() throws Exception {
        aiConfig.setApiKey("secret-key-12345678901");
        zhipuConfig.setModel("zhipu-chat-model");
        ChatModel chatModel = mock(ChatModel.class);
        when(chatModel.chat("ping")).thenReturn("pong");
        when(modelFactory.buildChatModel(any(AiModelConfig.class), isNull())).thenReturn(chatModel);
        when(modelFactory.buildSupervisorModel(any(AiModelConfig.class), isNull()))
            .thenReturn(org.mockito.Mockito.mock(ChatModel.class));
        when(modelFactory.buildVisionModel(any(AiModelConfig.class), isNull()))
            .thenReturn(org.mockito.Mockito.mock(ChatModel.class));

        var result = service.testCurrentConnectivity();

        assertEquals("zhipu-chat-model", result.getModel());
    }

    @Test
    void connectivityTestSucceedsWithoutReturningKey() throws Exception {
        aiConfig.setApiKey("secret-key-12345678901");
        ChatModel chatModel = mock(ChatModel.class);
        when(chatModel.chat("ping")).thenReturn("pong");
        when(modelFactory.buildChatModel(any(AiModelConfig.class), isNull())).thenReturn(chatModel);
        when(modelFactory.buildSupervisorModel(any(AiModelConfig.class), isNull()))
            .thenReturn(org.mockito.Mockito.mock(ChatModel.class));
        when(modelFactory.buildVisionModel(any(AiModelConfig.class), isNull()))
            .thenReturn(org.mockito.Mockito.mock(ChatModel.class));

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
            .thenReturn(org.mockito.Mockito.mock(ChatModel.class));
        when(modelFactory.buildVisionModel(any(AiModelConfig.class), isNull()))
            .thenReturn(org.mockito.Mockito.mock(ChatModel.class));

        var result = service.testCurrentConnectivity();
        String json = new ObjectMapper().writeValueAsString(result);

        assertFalse(result.isSuccess());
        assertEquals("模型不可用", result.getMessage());
        assertFalse(json.contains("secret-key-12345678901"));
    }

    @Test
    void activationBuildsStreamingModelWithCandidateKey(@TempDir Path tempDir) throws Exception {
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
        when(modelFactory.buildStreamingModel(any(AiModelConfig.class), eq(CANDIDATE_KEY)))
            .thenReturn(mock(StreamingChatModel.class));

        activationService.saveAndActivate(AiModelCandidateDTO.builder()
            .provider("zhipu")
            .model("chat-model")
            .supervisorModel("supervisor-model")
            .visionModel("vision-model")
            .apiKey(CANDIDATE_KEY)
            .build(), "tester");

        verify(modelFactory).buildStreamingModel(any(AiModelConfig.class), eq(CANDIDATE_KEY));
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
