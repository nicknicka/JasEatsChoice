package com.xx.jaseatschoicejava.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.agent.config.ChatModelProvider;
import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import com.xx.jaseatschoicejava.dto.AiRuntimeConfigSnapshot;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.service.AiRuntimeConfigService;

import dev.langchain4j.model.chat.ChatModel;

@ExtendWith(MockitoExtension.class)
class AiRuntimeConfigServiceTest {

    @Mock
    private ChatModelProvider chatModelProvider;

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
        service = new AiRuntimeConfigServiceImpl(aiConfig, zhipuConfig, chatModelProvider);
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
        assertEquals("8901", snapshot.getKeyStatus().getLast4());
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
        assertEquals("2109", snapshot.getKeyStatus().getLast4());
        assertFalse(json.contains("zhipu-key-98765432109"));
        assertFalse(json.contains("generic-key-12345678901"));
    }

    @Test
    void veryShortConfiguredKeyDoesNotExposeLast4AsPlaintext() throws Exception {
        aiConfig.setApiKey("abcd");

        AiRuntimeConfigSnapshot snapshot = service.getCurrentSnapshot();
        String json = new ObjectMapper().writeValueAsString(snapshot);

        assertTrue(snapshot.getKeyStatus().isConfigured());
        assertNull(snapshot.getKeyStatus().getLast4());
        assertFalse(json.contains("abcd"));
    }

    @Test
    void snapshotReportsCapabilityStateFromCurrentProvider() throws Exception {
        aiConfig.setApiKey("secret-key-12345678901");
        when(chatModelProvider.getCapabilities()).thenReturn(AiCapabilitiesDTO.builder()
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
        when(chatModelProvider.buildAiModel()).thenReturn(chatModel);

        var result = service.testCurrentConnectivity();

        assertEquals("zhipu-chat-model", result.getModel());
    }

    @Test
    void connectivityTestSucceedsWithoutReturningKey() throws Exception {
        aiConfig.setApiKey("secret-key-12345678901");
        ChatModel chatModel = mock(ChatModel.class);
        when(chatModel.chat("ping")).thenReturn("pong");
        when(chatModelProvider.buildAiModel()).thenReturn(chatModel);

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
        when(chatModelProvider.buildAiModel()).thenReturn(chatModel);

        var result = service.testCurrentConnectivity();
        String json = new ObjectMapper().writeValueAsString(result);

        assertFalse(result.isSuccess());
        assertEquals("模型不可用", result.getMessage());
        assertFalse(json.contains("secret-key-12345678901"));
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
