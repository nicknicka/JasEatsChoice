package com.xx.jaseatschoicejava.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import com.xx.jaseatschoicejava.dto.AiConnectivityResult;
import com.xx.jaseatschoicejava.dto.AiModelCandidateDTO;
import com.xx.jaseatschoicejava.dto.AiRuntimeConfigSnapshot;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.service.SystemConfigService;
import com.xx.jaseatschoicejava.service.impl.AiModelClientFactory;

import dev.langchain4j.model.chat.ChatModel;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class AiModelHotSwitchServiceTest {

    @Mock
    private SystemConfigService systemConfigService;

    @Mock
    private AiModelClientFactory modelFactory;

    private AIConfig aiConfig;
    private ZhipuAIConfig zhipuConfig;
    private DynamicAiModelRegistry registry;
    private AiRuntimeConfigServiceImpl service;

    @BeforeEach
    void setUp() {
        aiConfig = new AIConfig();
        aiConfig.setProvider("zhipu");
        aiConfig.setApiKey("secret-key-12345678901");
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
                when(modelFactory.providerCode()).thenReturn("zhipu");
        when(modelFactory.resolveKey(null)).thenAnswer(ignored -> aiConfig.getApiKey());
        when(modelFactory.buildChatModel(any(AiModelConfig.class), isNull()))
            .thenReturn(org.mockito.Mockito.mock(ChatModel.class));
        when(modelFactory.buildSupervisorModel(any(AiModelConfig.class), isNull()))
            .thenReturn(org.mockito.Mockito.mock(ChatModel.class));
        when(modelFactory.buildVisionModel(any(AiModelConfig.class), isNull()))
            .thenReturn(org.mockito.Mockito.mock(ChatModel.class));
        when(modelFactory.environmentConfig(any(AIConfig.class))).thenReturn(AiModelConfig.builder()
            .provider("zhipu")
            .model(aiConfig.getModel())
            .supervisorModel(aiConfig.getSupervisorModel())
            .visionModel(aiConfig.getVisionModel())
            .baseUrl(aiConfig.getBaseUrl())
            .timeoutMs(aiConfig.getTimeout())
            .temperature(aiConfig.getTemperature())
            .build());
        when(modelFactory.resolveKey(any())).thenAnswer(invocation -> {
            AiRuntimeKeyStore store = invocation.getArgument(0);
            String runtimeKey = store == null ? "" : store.readKey();
            return runtimeKey.isBlank() ? aiConfig.getApiKey() : runtimeKey;
        });
service = new AiRuntimeConfigServiceImpl(
            aiConfig, systemConfigService, registry, new AiProviderRegistry(java.util.List.of(modelFactory)),
            new ObjectMapper(), null, null, null);
        service.initialize();
    }

    private AiModelCandidateDTO candidate() {
        return AiModelCandidateDTO.builder()
            .provider("zhipu")
            .model("glm-4-flash")
            .supervisorModel("glm-4-flash")
            .visionModel("glm-4.6v-flash")
            .baseUrl("https://ai.example.com")
            .timeoutMs(12000)
            .temperature(0.4)
            .build();
    }

    @Test
    void databaseAiGroupTakesPriorityOverEnvironment() {
        AiModelConfig dbConfig = AiModelConfig.builder()
            .provider("zhipu")
            .model("db-chat")
            .supervisorModel("db-supervisor")
            .visionModel("db-vision")
            .baseUrl("https://db.example.com")
            .timeoutMs(9000)
            .temperature(0.3)
            .build();
        when(systemConfigService.getConfigsByGroup("ai")).thenReturn(Map.of(
            "ai.runtime-config", new ObjectMapper().valueToTree(dbConfig).toString()));

        service.initialize();
        AiRuntimeConfigSnapshot snapshot = service.getCurrentSnapshot();

        assertEquals("db-chat", snapshot.getModel());
        assertEquals("db-supervisor", snapshot.getSupervisorModel());
        assertEquals("db-vision", snapshot.getVisionModel());
        assertEquals("https://db.example.com", snapshot.getBaseUrl());
        assertEquals(9000, snapshot.getTimeoutMs());
        assertEquals(0.3, snapshot.getTemperature());
    }

    @Test
    void missingDatabaseGroupFallsBackToEnvironment() {
        when(systemConfigService.getConfigsByGroup("ai")).thenReturn(Map.of());
        service.initialize();

        AiRuntimeConfigSnapshot snapshot = service.getCurrentSnapshot();

        assertEquals("env-chat", snapshot.getModel());
        assertEquals("env-supervisor", snapshot.getSupervisorModel());
        assertEquals("env-vision", snapshot.getVisionModel());
        assertEquals(30000, snapshot.getTimeoutMs());
        assertEquals(0.7, snapshot.getTemperature());
    }

    @Test
    void candidateTestDoesNotPersistOrActivate() {
        ChatModel candidateModel = mock(ChatModel.class);
        when(candidateModel.chat("ping")).thenReturn("pong");
        when(modelFactory.buildChatModel(any(AiModelConfig.class), isNull())).thenReturn(candidateModel);

        AiConnectivityResult result = service.testCandidateConnectivity(candidate());

        assertTrue(result.isSuccess());
        assertEquals("glm-4-flash", result.getModel());
        verify(systemConfigService, never()).setConfigValue(anyString(), anyString(), anyString(), anyString());
        assertEquals("env-chat", service.getCurrentSnapshot().getModel());
    }

    @Test
    void savePersistsThenAtomicallyActivatesCandidate() {
        ChatModel candidateModel = mock(ChatModel.class);
        when(candidateModel.chat("ping")).thenReturn("pong");
        when(modelFactory.buildChatModel(any(AiModelConfig.class), isNull())).thenReturn(candidateModel);
        when(systemConfigService.setConfigValue(anyString(), anyString(), anyString(), anyString())).thenReturn(true);

        AiRuntimeConfigSnapshot snapshot = service.saveAndActivate(candidate());

        assertEquals("glm-4-flash", snapshot.getModel());
        assertEquals("glm-4.6v-flash", snapshot.getVisionModel());
        assertEquals("glm-4-flash", service.getCurrentSnapshot().getModel());
        verify(systemConfigService).setConfigValue(
            eq("ai.runtime-config"), anyString(), anyString(), anyString());
    }

    @Test
    void failedCandidateTestKeepsOldConfigAndDatabase() {
        ChatModel candidateModel = mock(ChatModel.class);
        when(candidateModel.chat("ping")).thenThrow(new IllegalStateException("model unavailable"));
        when(modelFactory.buildChatModel(any(AiModelConfig.class), isNull())).thenReturn(candidateModel);

        BusinessException exception = assertThrows(BusinessException.class,
            () -> service.saveAndActivate(candidate()));

        assertEquals("AI_CONFIG_TEST_FAILED", exception.getCode());
        verify(systemConfigService, never()).setConfigValue(anyString(), anyString(), anyString(), anyString());
        assertEquals("env-chat", service.getCurrentSnapshot().getModel());
    }

    @Test
    void persistenceFailureKeepsOldActiveConfig() {
        ChatModel candidateModel = mock(ChatModel.class);
        when(candidateModel.chat("ping")).thenReturn("pong");
        when(modelFactory.buildChatModel(any(AiModelConfig.class), isNull())).thenReturn(candidateModel);
        when(systemConfigService.setConfigValue(anyString(), anyString(), anyString(), anyString())).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
            () -> service.saveAndActivate(candidate()));

        assertEquals("AI_CONFIG_SAVE_FAILED", exception.getCode());
        assertEquals("env-chat", service.getCurrentSnapshot().getModel());
    }

    @Test
    void candidateTestValidatesChatSupervisorAndVisionModels() {
        AiModelCandidateDTO candidate = candidate().toBuilder().apiKey("candidate-key-1234567890").build();
        ChatModel chatModel = mock(ChatModel.class);
        ChatModel supervisorModel = mock(ChatModel.class);
        ChatModel visionModel = mock(ChatModel.class);
        when(modelFactory.buildChatModel(any(AiModelConfig.class), eq("candidate-key-1234567890")))
            .thenReturn(chatModel);
        when(modelFactory.buildSupervisorModel(any(AiModelConfig.class), eq("candidate-key-1234567890")))
            .thenReturn(supervisorModel);
        when(modelFactory.buildVisionModel(any(AiModelConfig.class), eq("candidate-key-1234567890")))
            .thenReturn(visionModel);
        when(chatModel.chat("ping")).thenReturn("pong");
        when(visionModel.chat("ping")).thenReturn("pong");
        when(supervisorModel.chat("ping")).thenThrow(new IllegalStateException("invalid supervisor"));

        AiConnectivityResult result = service.testCandidateConnectivity(candidate);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("invalid supervisor"));
        verify(modelFactory).buildChatModel(any(AiModelConfig.class), eq("candidate-key-1234567890"));
        verify(modelFactory).buildSupervisorModel(any(AiModelConfig.class), eq("candidate-key-1234567890"));
        verify(systemConfigService, never()).setConfigValue(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void unsupportedProviderIsRejected() {
        AiModelCandidateDTO invalid = candidate().toBuilder().provider("qwen").build();

        AiConnectivityResult result = service.testCandidateConnectivity(invalid);

        assertFalse(result.isSuccess());
        verify(systemConfigService, never()).setConfigValue(anyString(), anyString(), anyString(), anyString());
    }
}
