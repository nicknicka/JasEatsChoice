package com.xx.jaseatschoicejava.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.service.AIChatHistoryService;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class AIChatHistoryControllerTest {

    @Mock
    private AIChatHistoryService aiChatHistoryService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AIChatHistoryController controller;

    @Test
    void saveMessage_whenServiceFails_shouldReturnRecognizableFailure() {
        doThrow(new RuntimeException("database down")).when(aiChatHistoryService).saveMessage(
                eq("merchant"), eq("ai"), eq("保存失败"), eq("uni_card"), eq("{}"), eq("retry-1"));

        Map<String, Object> result = controller.saveMessage(Map.of(
                "userId", "merchant",
                "sender", "ai",
                "content", "保存失败",
                "messageType", "uni_card",
                "cardData", Map.of(),
                "clientMessageId", "retry-1"
        ));

        assertEquals(500, result.get("code"));
        assertEquals("保存失败：database down", result.get("message"));
    }
}
