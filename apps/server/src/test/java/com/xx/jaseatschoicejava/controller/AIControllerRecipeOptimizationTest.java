package com.xx.jaseatschoicejava.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.agent.config.ChatModelProvider;
import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.service.AiAssistantService;
import com.xx.jaseatschoicejava.service.AiQuotaScope;
import com.xx.jaseatschoicejava.service.AiQuotaService;
import com.xx.jaseatschoicejava.util.UserIdentityUtil;

/**
 * 食谱优化统一契约：目标校验、默认目标、错误透传与内部 error 字段清理。
 */
@ExtendWith(MockitoExtension.class)
class AIControllerRecipeOptimizationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private ChatModelProvider chatModelProvider;

    @Mock
    private AiAssistantService aiAssistantService;

    @Mock
    private AiQuotaService aiQuotaService;

    private AIController controller;

    @BeforeEach
    void setUp() {
        controller = new AIController();
        ReflectionTestUtils.setField(controller, "chatModelProvider", chatModelProvider);
        ReflectionTestUtils.setField(controller, "aiAssistantService", aiAssistantService);
        ReflectionTestUtils.setField(controller, "aiQuotaService", aiQuotaService);
    }

    private static MockHttpServletRequest verifiedRequest(String userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(UserIdentityUtil.VERIFIED_USER_ID_ATTRIBUTE, userId);
        return request;
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

    /**
     * 截获 SseEmitter 初始化前的发送缓冲，拼回 SSE 文本帧。
     *
     * <p>控制器直接调用时 emitter 尚未绑定 Handler，Spring 会把每个发送块缓存在
     * ResponseBodyEmitter.earlySendAttempts 中；测试借此断言事件顺序，无需真实连接。</p>
     */
    private static List<String> bufferedSseBlocks(SseEmitter emitter) {
        @SuppressWarnings("unchecked")
        Set<ResponseBodyEmitter.DataWithMediaType> attempts =
            (Set<ResponseBodyEmitter.DataWithMediaType>) ReflectionTestUtils.getField(emitter, "earlySendAttempts");
        assertNotNull(attempts, "SseEmitter 应保留初始化前的发送缓冲");
        StringBuilder wire = new StringBuilder();
        for (ResponseBodyEmitter.DataWithMediaType attempt : attempts) {
            wire.append(attempt.getData());
        }
        return Arrays.stream(wire.toString().split("\n\n"))
            .filter(block -> !block.isBlank())
            .toList();
    }

    private static String eventName(String block) {
        return block.substring(0, block.indexOf('\n')).substring("event:".length());
    }

    private static JsonNode eventData(String block) {
        int dataIndex = block.indexOf("data:");
        assertTrue(dataIndex >= 0, () -> "SSE 事件缺少 data 字段: " + block);
        return assertDoesNotThrow(
            () -> OBJECT_MAPPER.readTree(block.substring(dataIndex + "data:".length()).trim()),
            () -> "SSE 事件 data 不是合法 JSON: " + block);
    }

    private CapturedCallbacks captureStreamCallbacks() {
        CapturedCallbacks callbacks = new CapturedCallbacks();
        doAnswer(invocation -> {
            callbacks.progress = invocation.getArgument(2);
            callbacks.token = invocation.getArgument(3);
            callbacks.complete = invocation.getArgument(4);
            return null;
        }).when(aiAssistantService).optimizeRecipeStreaming(
            anyString(), anyString(), any(), any(), any());
        return callbacks;
    }

    private static final class CapturedCallbacks {
        private Consumer<String> progress;
        private Consumer<String> token;
        private LongConsumer complete;

        void emitProgress(String message) {
            progress.accept(message);
        }

        void emitToken(String content) {
            token.accept(content);
        }

        void complete(long elapsedMs) {
            complete.accept(elapsedMs);
        }
    }

    @Test
    void recipeOptimize_unknownGoal_returns400AndNeverTouchesService() {
        ResponseResult<?> result = controller.recipeOptimize(
            Map.of("recipe", "番茄炒蛋：鸡蛋2个、番茄2个", "goal", "keto"),
            verifiedRequest("42"));

        assertFalse(result.getSuccess());
        assertEquals("400", result.getCode());
        assertTrue(result.getMessage().contains("不支持的优化目标"));
        verifyNoInteractions(aiAssistantService);
    }

    @Test
    void recipeOptimize_blankRecipe_returns400() {
        ResponseResult<?> result = controller.recipeOptimize(Map.of("recipe", "   "), verifiedRequest("42"));

        assertFalse(result.getSuccess());
        assertEquals("400", result.getCode());
        verifyNoInteractions(aiAssistantService);
    }

    @Test
    void recipeOptimize_missingGoal_defaultsToBalanced() {
        when(chatModelProvider.getCapabilities()).thenReturn(textChatEnabled());
        when(aiAssistantService.optimizeRecipe(anyString(), anyString()))
            .thenReturn(mutableSuccessPayload());

        ResponseResult<?> result = controller.recipeOptimize(Map.of("recipe", "番茄炒蛋"), verifiedRequest("42"));

        assertTrue(result.getSuccess());
        verify(aiAssistantService).optimizeRecipe("番茄炒蛋", "balanced");
        verify(aiQuotaService).consume("42", AiQuotaScope.RECIPE);
    }

    @Test
    void recipeOptimize_forwardsRequestedGoalToService() {
        when(chatModelProvider.getCapabilities()).thenReturn(textChatEnabled());
        when(aiAssistantService.optimizeRecipe(anyString(), anyString()))
            .thenReturn(mutableSuccessPayload());

        controller.recipeOptimize(Map.of("recipe", "番茄炒蛋", "goal", "fat_loss"), verifiedRequest("42"));

        verify(aiAssistantService).optimizeRecipe("番茄炒蛋", "fat_loss");
    }

    @Test
    void recipeOptimize_serviceError_returnsFailureAndKeepsMessage() {
        when(chatModelProvider.getCapabilities()).thenReturn(textChatEnabled());
        when(aiAssistantService.optimizeRecipe(anyString(), anyString()))
            .thenReturn(Map.of("error", true, "message", "没有找到合适的优化食谱"));

        ResponseResult<?> result = controller.recipeOptimize(Map.of("recipe", "番茄炒蛋"), verifiedRequest("42"));

        assertFalse(result.getSuccess());
        assertTrue(result.getMessage().contains("没有找到合适的优化食谱"));
    }

    @Test
    void recipeOptimize_successDropsInternalErrorFlag() {
        when(chatModelProvider.getCapabilities()).thenReturn(textChatEnabled());
        when(aiAssistantService.optimizeRecipe(anyString(), anyString()))
            .thenReturn(mutableSuccessPayload());

        ResponseResult<?> result = controller.recipeOptimize(Map.of("recipe", "番茄炒蛋"), verifiedRequest("42"));

        assertTrue(result.getSuccess());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertFalse(data.containsKey("error"));
        assertEquals("番茄炒蛋", ((Map<?, ?>) data.get("recipe")).get("name"));
    }

    @Test
    void recipeOptimizeStream_unknownGoal_returnsEmitterAndSkipsService() {
        SseEmitter emitter = controller.recipeOptimizeStream(
            Map.of("recipe", "番茄炒蛋", "goal", "keto"), verifiedRequest("42"));

        assertNotNull(emitter);
        verifyNoInteractions(aiAssistantService);
    }

    @Test
    void recipeOptimizeStream_emitsProgressTokenResultDoneInOrder() {
        when(chatModelProvider.getCapabilities()).thenReturn(textChatEnabled());
        CapturedCallbacks callbacks = captureStreamCallbacks();

        SseEmitter emitter = controller.recipeOptimizeStream(
            Map.of("recipe", "番茄炒蛋", "goal", "balanced"), verifiedRequest("42"));
        callbacks.emitProgress("正在分析食谱内容...");
        callbacks.emitToken("{\"recipe\":");
        callbacks.emitProgress("RESULT:{\"goal\":\"balanced\",\"recipe\":{\"name\":\"番茄炒蛋\"}}");
        callbacks.complete(123L);

        List<String> events = bufferedSseBlocks(emitter);
        assertEquals(4, events.size(), () -> "实际事件: " + events);
        assertEquals("progress", eventName(events.get(0)));
        assertEquals("token", eventName(events.get(1)));
        assertEquals("result", eventName(events.get(2)));
        assertEquals("done", eventName(events.get(3)));
        assertTrue(events.get(0).contains("\"message\":\"正在分析食谱内容...\""));
        assertTrue(events.get(1).contains("\"content\":\"{\\\"recipe\\\":\""));
        assertTrue(events.get(2).contains("\"name\":\"番茄炒蛋\""));
        assertTrue(events.get(3).contains("\"elapsedMs\":"));
        verify(aiQuotaService).consume("42", AiQuotaScope.RECIPE);
    }

    @Test
    void recipeOptimizeStream_emitsErrorThenDoneForModelFailure() {
        when(chatModelProvider.getCapabilities()).thenReturn(textChatEnabled());
        CapturedCallbacks callbacks = captureStreamCallbacks();

        SseEmitter emitter = controller.recipeOptimizeStream(
            Map.of("recipe", "番茄炒蛋"), verifiedRequest("42"));
        callbacks.emitProgress("ERROR:AI_CALL_FAILED:AI调用失败，请稍后重试");
        callbacks.complete(50L);

        List<String> events = bufferedSseBlocks(emitter);
        assertEquals(2, events.size(), () -> "实际事件: " + events);
        assertEquals("error", eventName(events.get(0)));
        assertEquals("done", eventName(events.get(1)));
        assertEquals("AI_CALL_FAILED", eventData(events.get(0)).path("code").asText(),
            () -> "实际事件: " + events);
        assertTrue(eventData(events.get(0)).path("message").asText().contains("AI调用失败"));
    }

    @Test
    void recipeOptimizeStream_forwardsAiTimeoutErrorCode() {
        when(chatModelProvider.getCapabilities()).thenReturn(textChatEnabled());
        CapturedCallbacks callbacks = captureStreamCallbacks();

        SseEmitter emitter = controller.recipeOptimizeStream(
            Map.of("recipe", "番茄炒蛋"), verifiedRequest("42"));
        callbacks.emitProgress("ERROR:AI_TIMEOUT:生成超时，请稍后重试");

        List<String> events = bufferedSseBlocks(emitter);
        assertEquals(2, events.size(), () -> "实际事件: " + events);
        assertEquals("error", eventName(events.get(0)));
        assertEquals("AI_TIMEOUT", eventData(events.get(0)).path("code").asText(),
            () -> "实际事件: " + events);
        assertTrue(eventData(events.get(0)).path("message").asText().contains("生成超时"));
        assertEquals("done", eventName(events.get(1)));
    }

    @Test
    void recipeOptimizeStream_forwardsAiResultParseFailedCode() {
        when(chatModelProvider.getCapabilities()).thenReturn(textChatEnabled());
        CapturedCallbacks callbacks = captureStreamCallbacks();

        SseEmitter emitter = controller.recipeOptimizeStream(
            Map.of("recipe", "番茄炒蛋"), verifiedRequest("42"));
        callbacks.emitProgress("ERROR:AI_RESULT_PARSE_FAILED:没有找到合适的优化食谱");

        List<String> events = bufferedSseBlocks(emitter);
        assertEquals(2, events.size(), () -> "实际事件: " + events);
        assertEquals("error", eventName(events.get(0)));
        assertEquals("AI_RESULT_PARSE_FAILED", eventData(events.get(0)).path("code").asText(),
            () -> "实际事件: " + events);
        assertTrue(eventData(events.get(0)).path("message").asText().contains("没有找到合适的优化食谱"));
    }

    @Test
    void recipeOptimizeStream_missingResultOnCompleteReportsParseFailure() {
        when(chatModelProvider.getCapabilities()).thenReturn(textChatEnabled());
        CapturedCallbacks callbacks = captureStreamCallbacks();

        SseEmitter emitter = controller.recipeOptimizeStream(
            Map.of("recipe", "番茄炒蛋"), verifiedRequest("42"));
        callbacks.emitProgress("正在生成优化结果...");
        callbacks.complete(80L);

        List<String> events = bufferedSseBlocks(emitter);
        assertEquals(3, events.size(), () -> "实际事件: " + events);
        assertEquals("progress", eventName(events.get(0)));
        assertEquals("error", eventName(events.get(1)));
        assertEquals("AI_RESULT_PARSE_FAILED", eventData(events.get(1)).path("code").asText(),
            () -> "实际事件: " + events);
        assertEquals("done", eventName(events.get(2)));
    }

    @Test
    void recipeOptimizeStream_stalledGenerationEmitsAiTimeoutAndDone() throws InterruptedException {
        when(chatModelProvider.getCapabilities()).thenReturn(textChatEnabled());
        AIConfig aiConfig = new AIConfig();
        aiConfig.setRecipeStreamTimeoutMs(60);
        ReflectionTestUtils.setField(controller, "aiConfig", aiConfig);
        captureStreamCallbacks();

        // 上游挂起、服务层不再回调任何进度：由看门狗在统一超时后主动结束 SSE。
        SseEmitter emitter = controller.recipeOptimizeStream(
            Map.of("recipe", "番茄炒蛋"), verifiedRequest("42"));
        Thread.sleep(400);

        List<String> events = bufferedSseBlocks(emitter);
        assertEquals(2, events.size(), () -> "实际事件: " + events);
        assertEquals("error", eventName(events.get(0)));
        assertEquals("AI_TIMEOUT", eventData(events.get(0)).path("code").asText(),
            () -> "实际事件: " + events);
        assertEquals("done", eventName(events.get(1)));
    }

    @Test
    void recipeOptimizeStream_duplicateCompletionEmitsDoneOnce() {
        when(chatModelProvider.getCapabilities()).thenReturn(textChatEnabled());
        CapturedCallbacks callbacks = captureStreamCallbacks();

        SseEmitter emitter = controller.recipeOptimizeStream(
            Map.of("recipe", "番茄炒蛋"), verifiedRequest("42"));
        callbacks.emitProgress("RESULT:{\"recipe\":{\"name\":\"番茄炒蛋\"}}");
        callbacks.complete(10L);
        callbacks.complete(20L);
        callbacks.emitProgress("ERROR:AI_CALL_FAILED:晚到的错误");
        callbacks.emitProgress("RESULT:{\"recipe\":{\"name\":\"重复结果\"}}");

        List<String> events = bufferedSseBlocks(emitter);
        assertEquals(1, events.stream().filter(event -> eventName(event).equals("result")).count(),
            () -> "实际事件: " + events);
        assertEquals(1, events.stream().filter(event -> eventName(event).equals("done")).count(),
            () -> "实际事件: " + events);
        assertFalse(events.stream().anyMatch(event -> eventName(event).equals("error")),
            () -> "实际事件: " + events);
    }

    @Test
    void recipeOptimizeStream_cancelAfterResultEmitsTerminalEventsOnce() {
        when(chatModelProvider.getCapabilities()).thenReturn(textChatEnabled());
        CapturedCallbacks callbacks = captureStreamCallbacks();

        SseEmitter emitter = controller.recipeOptimizeStream(
            Map.of("recipe", "番茄炒蛋"), verifiedRequest("42"));
        callbacks.emitProgress("RESULT:{\"recipe\":{\"name\":\"番茄炒蛋\"}}");

        // Spring 6.1 的 DefaultCallback.run() 会先把 emitter 标记为 complete 再执行控制器注册的回调；
        // 这里直接取出 controller 注册的 delegate，验证取消/超时回调自身的终态去重。
        Runnable timeoutCallback = (Runnable) ReflectionTestUtils.getField(emitter, "timeoutCallback");
        assertNotNull(timeoutCallback, "控制器应注册超时回调");
        Runnable timeoutDelegate = (Runnable) ReflectionTestUtils.getField(timeoutCallback, "delegate");
        assertNotNull(timeoutDelegate, "超时回调应包含控制器注册的 delegate");
        timeoutDelegate.run();
        timeoutDelegate.run();
        callbacks.complete(30L);
        callbacks.emitProgress("RESULT:{\"recipe\":{\"name\":\"取消后的重复结果\"}}");

        List<String> events = bufferedSseBlocks(emitter);
        assertEquals(1, events.stream().filter(event -> eventName(event).equals("result")).count(),
            () -> "实际事件: " + events);
        assertEquals(1, events.stream().filter(event -> eventName(event).equals("error")).count(),
            () -> "实际事件: " + events);
        assertEquals(1, events.stream().filter(event -> eventName(event).equals("done")).count(),
            () -> "实际事件: " + events);
    }
}
