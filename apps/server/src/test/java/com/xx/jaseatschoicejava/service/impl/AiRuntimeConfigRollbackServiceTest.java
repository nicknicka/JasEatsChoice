package com.xx.jaseatschoicejava.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
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
import com.xx.jaseatschoicejava.dto.AiPreviousConfigSummary;
import com.xx.jaseatschoicejava.dto.AiRuntimeConfigSnapshot;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.dto.AiConfigAuditEvent;
import com.xx.jaseatschoicejava.service.AiConfigAuditService;
import com.xx.jaseatschoicejava.service.AiConfigHistoryStore;
import com.xx.jaseatschoicejava.service.SystemConfigService;

import dev.langchain4j.model.chat.ChatModel;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AiRuntimeConfigRollbackServiceTest {

    private static final String OLD_KEY = "old-active-key-0987654321";
    private static final String NEW_KEY = "new-active-key-1234567890";

    @TempDir
    Path tempDir;

    @Mock
    private SystemConfigService systemConfigService;

    @Mock
    private AiModelClientFactory modelFactory;

    @Mock
    private AiConfigAuditService auditService;

    @Mock
    private AiConfigHistoryStore historyStore;

    private AIConfig aiConfig;
    private DynamicAiModelRegistry registry;
    private AiRuntimeKeyStore keyStore;
    private AiRuntimeConfigServiceImpl service;

    @BeforeEach
    void setUp() throws IOException {
        aiConfig = new AIConfig();
        aiConfig.setProvider("openai-compatible");
        aiConfig.setApiKey(OLD_KEY);
        aiConfig.setModel("env-chat");
        aiConfig.setSupervisorModel("env-supervisor");
        aiConfig.setVisionModel("env-vision");
        aiConfig.setBaseUrl("https://env.example.com");
        aiConfig.setTimeout(31000);
        aiConfig.setTemperature(0.3);


        registry = new DynamicAiModelRegistry();
        keyStore = new AiRuntimeKeyStore(tempDir.resolve("ai-active-key").toString());
                when(modelFactory.providerCode()).thenReturn("openai-compatible");
        when(modelFactory.environmentConfig(any(AIConfig.class))).thenReturn(AiModelConfig.builder()
            .provider("openai-compatible")
            .model(aiConfig.getModel())
            .supervisorModel(aiConfig.getSupervisorModel())
            .visionModel(aiConfig.getVisionModel())
            .baseUrl(aiConfig.getBaseUrl())
            .timeoutMs(aiConfig.getTimeout())
            .temperature(aiConfig.getTemperature())
            .build());
        when(modelFactory.resolveKey(any())).thenAnswer(invocation -> {
            AiRuntimeKeyStore store = invocation.getArgument(0);
            String runtimeKey = store.readKey();
            return runtimeKey.isBlank() ? aiConfig.getApiKey() : runtimeKey;
        });
service = new AiRuntimeConfigServiceImpl(aiConfig, systemConfigService, registry,
            new AiProviderRegistry(java.util.List.of(modelFactory)), new ObjectMapper(), keyStore,
            auditService, historyStore, null, null);
        service.initialize();
    }

    private AiModelCandidateDTO candidate() {
        return AiModelCandidateDTO.builder()
            .provider("openai-compatible")
            .model("gpt-4o-mini")
            .supervisorModel("supervisor-model")
            .visionModel("vision-model")
            .baseUrl("https://candidate.example.com")
            .timeoutMs(30000)
            .temperature(0.7)
            .apiKey(NEW_KEY)
            .build();
    }

    private ChatModel successfulModel() {
        ChatModel chatModel = mock(ChatModel.class);
        org.mockito.Mockito.doReturn("pong").when(chatModel).chat("ping");
        return chatModel;
    }

    @Test
    void exposesPreviousValidConfigSummaryWithoutKeyMaterial() throws Exception {
        assertNull(service.getPreviousConfigSummary());

        ChatModel candidateModel = successfulModel();
        ChatModel rollbackModel = successfulModel();
        ChatModel supervisorModel = mock(ChatModel.class);
        ChatModel visionModel = mock(ChatModel.class);
        when(modelFactory.buildChatModel(any(AiModelConfig.class), eq(NEW_KEY)))
            .thenReturn(candidateModel);
        when(modelFactory.buildChatModel(any(AiModelConfig.class), isNull())).thenReturn(rollbackModel);
        when(modelFactory.buildSupervisorModel(any(AiModelConfig.class), eq(NEW_KEY))).thenReturn(supervisorModel);
        when(modelFactory.buildVisionModel(any(AiModelConfig.class), eq(NEW_KEY))).thenReturn(visionModel);
        when(modelFactory.buildSupervisorModel(any(AiModelConfig.class), isNull())).thenReturn(supervisorModel);
        when(modelFactory.buildVisionModel(any(AiModelConfig.class), isNull())).thenReturn(visionModel);
        when(modelFactory.buildStreamingModel(any(AiModelConfig.class))).thenReturn(null);
        when(systemConfigService.setConfigValue(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(true);
        service.saveAndActivate(candidate(), "13800000000");

        AiPreviousConfigSummary summary = service.getPreviousConfigSummary();
        assertNotNull(summary);
        assertEquals("env-chat", summary.getModel());
        assertEquals("env-supervisor", summary.getSupervisorModel());
        assertEquals("env-vision", summary.getVisionModel());
        assertEquals("https://env.example.com", summary.getBaseUrl());
        assertEquals(31000, summary.getTimeoutMs());
        assertEquals(0.3, summary.getTemperature());
        org.junit.jupiter.api.Assertions.assertTrue(summary.isKeyConfigured());
        assertNotNull(summary.getKeyFingerprint());
        String json = new ObjectMapper().writeValueAsString(summary);
        org.junit.jupiter.api.Assertions.assertFalse(json.contains(OLD_KEY));
        org.junit.jupiter.api.Assertions.assertFalse(json.contains(NEW_KEY));

        com.xx.jaseatschoicejava.dto.AiRuntimeConfigSnapshot rolledBack = service.rollbackToPrevious("13800000000");
        assertEquals("env-chat", rolledBack.getModel());
        assertEquals("env-supervisor", rolledBack.getSupervisorModel());
        assertEquals("https://env.example.com", rolledBack.getBaseUrl());
    }

    private void saveCandidate() throws Exception {
        ChatModel candidateModel = successfulModel();
        ChatModel supervisorModel = org.mockito.Mockito.mock(ChatModel.class);
        ChatModel visionModel = org.mockito.Mockito.mock(ChatModel.class);
        when(modelFactory.buildChatModel(any(AiModelConfig.class), eq(NEW_KEY)))
            .thenReturn(candidateModel);
        when(modelFactory.buildSupervisorModel(any(AiModelConfig.class), eq(NEW_KEY)))
            .thenReturn(supervisorModel);
        when(modelFactory.buildVisionModel(any(AiModelConfig.class), eq(NEW_KEY)))
            .thenReturn(visionModel);
        when(modelFactory.buildStreamingModel(any(AiModelConfig.class))).thenReturn(null);
        when(systemConfigService.setConfigValue(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(true);
        service.saveAndActivate(candidate(), "13800000000");
    }

    @Test
    void clearedKeyRejectsRollbackWithoutChangingActiveConfig() throws Exception {
        saveCandidate();

        assertTrue(java.nio.file.Files.deleteIfExists(tempDir.resolve("ai-active-key")));
        aiConfig.setApiKey("");
        BusinessException exception = assertThrows(BusinessException.class,
            () -> service.rollbackToPrevious("13800000000"));

        assertEquals("AI_KEY_REQUIRED", exception.getCode());
        assertTrue(exception.getMessage().contains("重新输入"));
        assertEquals("gpt-4o-mini", service.getCurrentSnapshot().getModel());
    }

    @Test
    void failedRollbackTestKeepsCurrentConfig() throws Exception {
        saveCandidate();
        ChatModel failingModel = org.mockito.Mockito.mock(ChatModel.class);
        org.mockito.Mockito.doThrow(new IllegalStateException("model unavailable"))
            .when(failingModel).chat("ping");
        when(modelFactory.buildChatModel(any(AiModelConfig.class), isNull())).thenReturn(failingModel);
        when(modelFactory.buildSupervisorModel(any(AiModelConfig.class), isNull())).thenReturn(org.mockito.Mockito.mock(ChatModel.class));
        when(modelFactory.buildVisionModel(any(AiModelConfig.class), isNull())).thenReturn(org.mockito.Mockito.mock(ChatModel.class));

        BusinessException exception = assertThrows(BusinessException.class,
            () -> service.rollbackToPrevious("13800000000"));

        assertEquals("AI_CONFIG_ROLLBACK_TEST_FAILED", exception.getCode());
        assertEquals("gpt-4o-mini", service.getCurrentSnapshot().getModel());
    }

    @Test
    void failedRollbackPersistenceKeepsActiveConfig() throws Exception {
        saveCandidate();
        ChatModel rollbackModel = successfulModel();
        when(modelFactory.buildChatModel(any(AiModelConfig.class), isNull())).thenReturn(rollbackModel);
        when(modelFactory.buildSupervisorModel(any(AiModelConfig.class), isNull())).thenReturn(org.mockito.Mockito.mock(ChatModel.class));
        when(modelFactory.buildVisionModel(any(AiModelConfig.class), isNull())).thenReturn(org.mockito.Mockito.mock(ChatModel.class));
        clearInvocations(systemConfigService, historyStore);
        when(systemConfigService.setConfigValue(
            eq("ai.runtime-config"), anyString(), anyString(), anyString())).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
            () -> service.rollbackToPrevious("13800000000"));

        assertEquals("AI_CONFIG_ROLLBACK_FAILED", exception.getCode());
        assertEquals("gpt-4o-mini", service.getCurrentSnapshot().getModel());
        verify(systemConfigService, never()).setConfigValue(
            eq("ai.runtime-config-history"), anyString(), anyString(), anyString());
        verify(historyStore, never()).save(anyString());
    }

    @Test
    void historyPersistenceFailureKeepsOldConfigKeyAndDatabase() throws Exception {
        saveCandidate();
        clearInvocations(systemConfigService, historyStore, auditService);
        doThrow(new IllegalStateException("history unavailable"))
            .when(historyStore).save(anyString());

        BusinessException exception = assertThrows(BusinessException.class,
            () -> service.saveAndActivate(candidate().toBuilder()
                .model("history-failure-model")
                .apiKey(NEW_KEY).build(), "13800000000"));

        assertEquals("AI_CONFIG_HISTORY_SAVE_FAILED", exception.getCode());
        assertEquals("gpt-4o-mini", service.getCurrentSnapshot().getModel());
        assertEquals(2L, service.getCurrentSnapshot().getVersion());
        assertEquals(NEW_KEY, java.nio.file.Files.readString(tempDir.resolve("ai-active-key")).trim());
        verify(systemConfigService).setConfigValue(
            eq("ai.runtime-config"), anyString(), anyString(), anyString());
        verify(systemConfigService, atLeastOnce()).deleteConfig("ai.runtime-config");
    }

    @Test
    void rollbackIncreasesVersionMonotonically() throws Exception {
        saveCandidate();
        ChatModel rollbackModel = successfulModel();
        ChatModel supervisorModel = org.mockito.Mockito.mock(ChatModel.class);
        ChatModel visionModel = org.mockito.Mockito.mock(ChatModel.class);
        when(modelFactory.buildChatModel(any(AiModelConfig.class), isNull())).thenReturn(rollbackModel);
        when(modelFactory.buildSupervisorModel(any(AiModelConfig.class), isNull())).thenReturn(supervisorModel);
        when(modelFactory.buildVisionModel(any(AiModelConfig.class), isNull())).thenReturn(visionModel);
        when(modelFactory.buildStreamingModel(any(AiModelConfig.class))).thenReturn(null);

        AiRuntimeConfigSnapshot rolledBack = service.rollbackToPrevious("13800000000");

        assertEquals("env-chat", rolledBack.getModel());
        assertEquals(3L, rolledBack.getVersion());
        assertTrue(rolledBack.getVersion() > 2L);
    }

    @Test
    void auditRecordsActivationAndSuccessfulRollbackWithoutKeyMaterial() throws Exception {
        saveCandidate();
        ChatModel rollbackModel = successfulModel();
        ChatModel supervisorModel = org.mockito.Mockito.mock(ChatModel.class);
        ChatModel visionModel = org.mockito.Mockito.mock(ChatModel.class);
        when(modelFactory.buildChatModel(any(AiModelConfig.class), isNull())).thenReturn(rollbackModel);
        when(modelFactory.buildSupervisorModel(any(AiModelConfig.class), isNull())).thenReturn(supervisorModel);
        when(modelFactory.buildVisionModel(any(AiModelConfig.class), isNull())).thenReturn(visionModel);
        when(modelFactory.buildStreamingModel(any(AiModelConfig.class))).thenReturn(null);
        service.rollbackToPrevious("13800000000");

        ArgumentCaptor<AiConfigAuditEvent> captor = ArgumentCaptor.forClass(AiConfigAuditEvent.class);
        verify(auditService, atLeastOnce()).record(captor.capture());
        var events = captor.getAllValues();
        assertEquals("SAVE_SUCCEEDED", events.get(0).getAction());
        assertEquals("13800000000", events.get(0).getOperator());
        assertTrue(events.stream().anyMatch(event -> "ACTIVATED".equals(event.getAction())));
        assertTrue(events.stream().anyMatch(event -> "ROLLBACK_SUCCEEDED".equals(event.getAction())));

        ObjectMapper mapper = new ObjectMapper();
        for (AiConfigAuditEvent event : events) {
            String json = mapper.writeValueAsString(event);
            assertFalse(json.contains(OLD_KEY));
            assertFalse(json.contains(NEW_KEY));
        }
    }

    @Test
    void auditRecordsRollbackFailureWithoutKeyMaterial() throws Exception {
        saveCandidate();
        assertTrue(java.nio.file.Files.deleteIfExists(tempDir.resolve("ai-active-key")));
        aiConfig.setApiKey("");

        assertThrows(BusinessException.class, () -> service.rollbackToPrevious("13800000000"));

        ArgumentCaptor<AiConfigAuditEvent> captor = ArgumentCaptor.forClass(AiConfigAuditEvent.class);
        verify(auditService, atLeastOnce()).record(captor.capture());
        AiConfigAuditEvent event = captor.getValue();
        assertEquals("ROLLBACK_FAILED", event.getAction());
        assertEquals("FAILED", event.getStatus());
        assertEquals("13800000000", event.getOperator());
        String json = new ObjectMapper().writeValueAsString(event);
        assertFalse(json.contains(OLD_KEY));
        assertFalse(json.contains(NEW_KEY));
    }

    @Test
    void auditRecordsFailedSaveTestWithoutKeyMaterial() throws Exception {
        aiConfig.setApiKey("");
        AiModelCandidateDTO failedCandidate = candidate();
        failedCandidate.setApiKey(NEW_KEY);

        BusinessException exception = assertThrows(BusinessException.class,
            () -> service.saveAndActivate(failedCandidate, "13800000000"));

        assertEquals("AI_CONFIG_TEST_FAILED", exception.getCode());
        ArgumentCaptor<AiConfigAuditEvent> captor = ArgumentCaptor.forClass(AiConfigAuditEvent.class);
        verify(auditService, atLeastOnce()).record(captor.capture());
        AiConfigAuditEvent event = captor.getValue();
        assertEquals("SAVE_FAILED", event.getAction());
        assertEquals("FAILED", event.getStatus());
        assertEquals("FAILED", event.getTestResult());
        String json = new ObjectMapper().writeValueAsString(event);
        assertFalse(json.contains(OLD_KEY));
        assertFalse(json.contains(NEW_KEY));
    }
}
