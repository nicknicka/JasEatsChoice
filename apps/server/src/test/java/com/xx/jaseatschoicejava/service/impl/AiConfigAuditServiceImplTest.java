package com.xx.jaseatschoicejava.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.dto.AiConfigAuditEvent;
import com.xx.jaseatschoicejava.entity.SystemLog;
import com.xx.jaseatschoicejava.service.SystemLogService;

@ExtendWith(MockitoExtension.class)
class AiConfigAuditServiceImplTest {

    @Mock
    private SystemLogService systemLogService;

    @Test
    void recordsNullableVersionsAndRedactsUnexpectedKeyMaterial() {
        AiConfigAuditServiceImpl service = new AiConfigAuditServiceImpl(
            systemLogService, new ObjectMapper());
        AiConfigAuditEvent event = AiConfigAuditEvent.builder()
            .action("ROLLBACK_FAILED")
            .operator("13800000000")
            .operatedAt(LocalDateTime.of(2026, 10, 31, 12, 0).toString())
            .oldVersion(null)
            .newVersion(null)
            .oldSummary(null)
            .newSummary(Map.of("apiKey", "secret-key-1234567890", "keyFingerprint", "abcd12345678"))
            .testResult("FAILED")
            .status("FAILED")
            .message("回滚失败")
            .build();

        service.record(event);

        ArgumentCaptor<SystemLog> captor = ArgumentCaptor.forClass(SystemLog.class);
        verify(systemLogService).saveLog(captor.capture());
        SystemLog saved = captor.getValue();
        assertEquals("ROLLBACK_FAILED", saved.getOperationType());
        assertEquals("AI_CONFIG", saved.getModule());
        assertEquals("13800000000", saved.getOperatorName());
        assertEquals(LocalDateTime.of(2026, 10, 31, 12, 0), saved.getCreatedAt());

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode params = mapper.readTree(saved.getParams());
            assertEquals(0L, params.path("oldVersion").asLong());
            assertEquals(0L, params.path("newVersion").asLong());
            assertEquals("", params.path("oldSummary").asText());
            assertEquals("***", params.path("newSummary").path("apiKey").asText());
            assertEquals("abcd12345678", params.path("newSummary").path("keyFingerprint").asText());
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
        assertFalse(String.valueOf(saved.getParams()).contains("secret-key-1234567890"));
        assertFalse(String.valueOf(saved.getResult()).contains("secret-key-1234567890"));
    }
}
