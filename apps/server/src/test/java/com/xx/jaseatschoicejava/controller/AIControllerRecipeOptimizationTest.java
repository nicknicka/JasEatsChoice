package com.xx.jaseatschoicejava.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.xx.jaseatschoicejava.agent.config.ChatModelProvider;
import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.service.AiAssistantService;

/**
 * 食谱优化统一契约：目标校验、默认目标、错误透传与内部 error 字段清理。
 */
@ExtendWith(MockitoExtension.class)
class AIControllerRecipeOptimizationTest {

    @Mock
    private ChatModelProvider chatModelProvider;

    @Mock
    private AiAssistantService aiAssistantService;

    private AIController controller;

    @BeforeEach
    void setUp() {
        controller = new AIController();
        ReflectionTestUtils.setField(controller, "chatModelProvider", chatModelProvider);
        ReflectionTestUtils.setField(controller, "aiAssistantService", aiAssistantService);
    }

    private static AiCapabilitiesDTO textChatEnabled() {
        return AiCapabilitiesDTO.builder().available(true).textChat(true).streaming(true).build();
    }

    private static Map<String, Object> mutableSuccessPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("error", false);
        payload.put("goal", "balanced");
        payload.put("recipe", Map.of("name", "番茄炒蛋"));
        return payload;
    }

    @Test
    void recipeOptimize_unknownGoal_returns400AndNeverTouchesService() {
        ResponseResult<?> result = controller.recipeOptimize(
            Map.of("recipe", "番茄炒蛋：鸡蛋2个、番茄2个", "goal", "keto"));

        assertFalse(result.getSuccess());
        assertEquals("400", result.getCode());
        assertTrue(result.getMessage().contains("不支持的优化目标"));
        verifyNoInteractions(aiAssistantService);
    }

    @Test
    void recipeOptimize_blankRecipe_returns400() {
        ResponseResult<?> result = controller.recipeOptimize(Map.of("recipe", "   "));

        assertFalse(result.getSuccess());
        assertEquals("400", result.getCode());
        verifyNoInteractions(aiAssistantService);
    }

    @Test
    void recipeOptimize_missingGoal_defaultsToBalanced() {
        when(chatModelProvider.getCapabilities()).thenReturn(textChatEnabled());
        when(aiAssistantService.optimizeRecipe(anyString(), anyString()))
            .thenReturn(mutableSuccessPayload());

        ResponseResult<?> result = controller.recipeOptimize(Map.of("recipe", "番茄炒蛋"));

        assertTrue(result.getSuccess());
        verify(aiAssistantService).optimizeRecipe("番茄炒蛋", "balanced");
    }

    @Test
    void recipeOptimize_forwardsRequestedGoalToService() {
        when(chatModelProvider.getCapabilities()).thenReturn(textChatEnabled());
        when(aiAssistantService.optimizeRecipe(anyString(), anyString()))
            .thenReturn(mutableSuccessPayload());

        controller.recipeOptimize(Map.of("recipe", "番茄炒蛋", "goal", "fat_loss"));

        verify(aiAssistantService).optimizeRecipe("番茄炒蛋", "fat_loss");
    }

    @Test
    void recipeOptimize_serviceError_returnsFailureAndKeepsMessage() {
        when(chatModelProvider.getCapabilities()).thenReturn(textChatEnabled());
        when(aiAssistantService.optimizeRecipe(anyString(), anyString()))
            .thenReturn(Map.of("error", true, "message", "没有找到合适的优化食谱"));

        ResponseResult<?> result = controller.recipeOptimize(Map.of("recipe", "番茄炒蛋"));

        assertFalse(result.getSuccess());
        assertTrue(result.getMessage().contains("没有找到合适的优化食谱"));
    }

    @Test
    void recipeOptimize_successDropsInternalErrorFlag() {
        when(chatModelProvider.getCapabilities()).thenReturn(textChatEnabled());
        when(aiAssistantService.optimizeRecipe(anyString(), anyString()))
            .thenReturn(mutableSuccessPayload());

        ResponseResult<?> result = controller.recipeOptimize(Map.of("recipe", "番茄炒蛋"));

        assertTrue(result.getSuccess());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertFalse(data.containsKey("error"));
        assertEquals("番茄炒蛋", ((Map<?, ?>) data.get("recipe")).get("name"));
    }

    @Test
    void recipeOptimizeStream_unknownGoal_returnsEmitterAndSkipsService() {
        SseEmitter emitter = controller.recipeOptimizeStream(
            Map.of("recipe", "番茄炒蛋", "goal", "keto"));

        assertNotNull(emitter);
        verifyNoInteractions(aiAssistantService);
    }
}
