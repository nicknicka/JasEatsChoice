package com.xx.jaseatschoicejava.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.agent.config.DynamicAiModelRegistry;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiModelConfig;
import com.xx.jaseatschoicejava.config.AiRuntimeKeyStore;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import com.xx.jaseatschoicejava.dto.AiModelCandidateDTO;
import com.xx.jaseatschoicejava.dto.AiRuntimeConfigSnapshot;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.service.SystemConfigService;

import dev.langchain4j.model.chat.ChatModel;

@ExtendWith(MockitoExtension.class)
class AiRuntimeKeyRotationServiceTest {

    private static final String OLD_KEY = "old-active-key-0987654321";
    private static final String NEW_KEY = "new-active-key-1234567890";
    private static final String OLD_FINGERPRINT = "928dd9fd3be8";
    private static final String NEW_FINGERPRINT = "5bbbb3dc058c";

    @TempDir
    Path tempDir;

    @Mock
    private SystemConfigService systemConfigService;

    @Mock
    private AiModelClientFactory modelFactory;

    private AIConfig aiConfig;
    private ZhipuAIConfig zhipuConfig;
    private DynamicAiModelRegistry registry;
    private AiRuntimeKeyStore keyStore;
    private AiRuntimeConfigServiceImpl service;

    @BeforeEach
    void setUp() throws IOException {
        aiConfig = new AIConfig();
        aiConfig.setProvider("zhipu");
        aiConfig.setApiKey("env-active-key-1234567890");
        aiConfig.setModel("env-chat");
        aiConfig.setSupervisorModel("env-supervisor");
        aiConfig.setVisionModel("env-vision");
        aiConfig.setTimeout(30000);
        aiConfig.setTemperature(0.7);
        zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("");
        zhipuConfig.setModel("");
        zhipuConfig.setVisionModel("");
        zhipuConfig.setBaseUrl("");
        zhipuConfig.setTimeout(null);
        registry = new DynamicAiModelRegistry();
        keyStore = spy(new AiRuntimeKeyStore(tempDir.resolve("ai-active-key").toString()));
        service = new AiRuntimeConfigServiceImpl(
            aiConfig, zhipuConfig, null, systemConfigService, registry, modelFactory,
            new ObjectMapper(), keyStore);
        service.initialize();
    }

    private AiModelCandidateDTO candidate() {
        return AiModelCandidateDTO.builder()
            .provider("zhipu")
            .model("glm-4-flash")
            .supervisorModel("glm-4-flash")
            .visionModel("glm-4.6v-flash")
            .baseUrl("")
            .timeoutMs(30000)
            .temperature(0.7)
            .apiKey(NEW_KEY)
            .build();
    }

    @Test
    void successfulCandidateKeyIsTestedThenPersistedAndActivated() throws Exception {
        ChatModel candidateModel = successfulModel();
        when(modelFactory.buildChatModel(any(AiModelConfig.class), eq(NEW_KEY)))
            .thenReturn(candidateModel);
        when(systemConfigService.setConfigValue(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(true);

        AiRuntimeConfigSnapshot snapshot = service.saveAndActivate(candidate());
        String json = new ObjectMapper().writeValueAsString(snapshot);

        assertEquals("glm-4-flash", snapshot.getModel());
        assertEquals(NEW_KEY, Files.readString(tempDir.resolve("ai-active-key")).trim());
        assertEquals(NEW_FINGERPRINT, snapshot.getKeyStatus().getFingerprint());
        assertFalse(json.contains(NEW_KEY));
        assertFalse(json.contains(OLD_KEY));
        assertTrue(json.contains(NEW_FINGERPRINT));
        verify(systemConfigService).setConfigValue(
            eq("ai.runtime-config"), anyString(), anyString(), anyString());
    }

    @Test
    void failedCandidateTestDoesNotWriteRuntimeKeyOrPersistConfig() throws IOException {
        keyStore.writeKey(OLD_KEY);
        ChatModel failingModel = mock(ChatModel.class);
        when(failingModel.chat("ping")).thenThrow(new IllegalStateException("model unavailable"));
        when(modelFactory.buildChatModel(any(AiModelConfig.class), eq(NEW_KEY)))
            .thenReturn(failingModel);

        BusinessException exception = assertThrows(BusinessException.class,
            () -> service.saveAndActivate(candidate()));

        assertEquals("AI_CONFIG_TEST_FAILED", exception.getCode());
        assertEquals(OLD_KEY, Files.readString(tempDir.resolve("ai-active-key")).trim());
        verify(systemConfigService, never()).setConfigValue(
            anyString(), anyString(), anyString(), anyString());
        assertEquals("env-chat", service.getCurrentSnapshot().getModel());
    }

    @Test
    void runtimeKeyWriteFailureKeepsPreviousKeyAndDatabase() throws IOException {
        keyStore.writeKey(OLD_KEY);
        ChatModel candidateModel = successfulModel();
        when(modelFactory.buildChatModel(any(AiModelConfig.class), eq(NEW_KEY)))
            .thenReturn(candidateModel);
        doThrow(new IllegalStateException("disk full")).when(keyStore).writeKey(NEW_KEY);

        BusinessException exception = assertThrows(BusinessException.class,
            () -> service.saveAndActivate(candidate()));

        assertEquals("AI_KEY_WRITE_FAILED", exception.getCode());
        assertEquals(OLD_KEY, Files.readString(tempDir.resolve("ai-active-key")).trim());
        verify(systemConfigService, never()).setConfigValue(
            anyString(), anyString(), anyString(), anyString());
        assertEquals("env-chat", service.getCurrentSnapshot().getModel());
    }

    @Test
    void persistenceFailureRollsRuntimeKeyBack() throws IOException {
        keyStore.writeKey(OLD_KEY);
        ChatModel candidateModel = successfulModel();
        when(modelFactory.buildChatModel(any(AiModelConfig.class), eq(NEW_KEY)))
            .thenReturn(candidateModel);
        when(modelFactory.buildChatModel(any(AiModelConfig.class)))
            .thenReturn(mock(ChatModel.class));
        when(systemConfigService.setConfigValue(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
            () -> service.saveAndActivate(candidate()));

        assertEquals("AI_CONFIG_SAVE_FAILED", exception.getCode());
        assertEquals(OLD_KEY, Files.readString(tempDir.resolve("ai-active-key")).trim());
        assertEquals("env-chat", service.getCurrentSnapshot().getModel());
        AiRuntimeConfigSnapshot snapshot = service.getCurrentSnapshot();
        assertEquals(OLD_FINGERPRINT, snapshot.getKeyStatus().getFingerprint());
    }

    @Test
    void deletedRuntimeKeyFallsBackToEnvironmentKey() {
        AiRuntimeConfigSnapshot snapshot = service.getCurrentSnapshot();

        assertEquals("6c6efe348ea5", snapshot.getKeyStatus().getFingerprint());
    }

    private ChatModel successfulModel() {
        ChatModel chatModel = mock(ChatModel.class);
        org.mockito.Mockito.doReturn("pong").when(chatModel).chat("ping");
        return chatModel;
    }
}
