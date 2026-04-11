package com.xx.jaseatschoicejava.controller;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.agent.agents.CustomerServiceAgent;
import com.xx.jaseatschoicejava.agent.listener.SSEAgentListener;
import com.xx.jaseatschoicejava.agent.service.SupervisorAgentFactory;

import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * SupervisorAgent SSE 流式输出控制器（V2 架构）
 *
 * 架构：同步 Supervisor（数据收集）+ 流式 Response（逐字输出）
 *
 * 执行流程：
 * 1. 同步阶段：SupervisorAgent 协调 L1 专家 Agent 收集数据（用户看到进度事件）
 * 2. 流式阶段：StreamingResponseAgent 将结果逐字输出（用户看到打字机效果）
 *
 * SSE 事件格式（兼容前端 useStreamResponse.js）：
 * - 进度事件: {"message":"正在搜索菜品","progress":true} → 前端过滤跳过
 * - Token事件: {"content":"推"} → 前端逐字追加
 * - 完成事件: {"done":true} → 前端触发 onComplete
 *
 * @author Claude
 * @since 2026-03-26
 * @updated 2026-04-03 V2: 同步 Supervisor + 流式 Response 架构
 */
@Tag(name = "Supervisor监督代理（SSE流式）", description = "SupervisorAgent流式输出接口")
@RestController
@RequestMapping("/agent/supervisor-sse")
public class SupervisorSSEController {

    private static final Logger log = LoggerFactory.getLogger(SupervisorSSEController.class);
    private static final long SSE_TIMEOUT_MS = 300000L;

    private final SupervisorAgentFactory supervisorAgentFactory;
    private final CustomerServiceAgent customerServiceAgent;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ObjectMapper objectMapper;

    public SupervisorSSEController(
            SupervisorAgentFactory supervisorAgentFactory,
            CustomerServiceAgent customerServiceAgent) {
        this.supervisorAgentFactory = supervisorAgentFactory;
        this.customerServiceAgent = customerServiceAgent;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * SSE流式聊天接口
     *
     * @param message 用户消息
     * @param userId 用户ID（推荐传入，以保持对话历史）
     * @return SSE流
     */
    @Operation(summary = "SSE流式聊天", description = "实时推送Agent执行过程和流式结果")
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @Parameter(description = "用户消息", required = true)
            @RequestParam String message,

            @Parameter(description = "用户ID（开启个性化服务后传入）", required = false)
            @RequestParam(required = false) String userId) {

        log.info("收到SSE聊天请求: message={}, userId={}", message, userId);

        // 路由逻辑：无userId使用客服助手，有userId使用SupervisorAgent
        if (userId == null || userId.isEmpty()) {
            log.info("未提供userId，使用客服助手Agent（无个性化服务）");
            return handleCustomerServiceChat(message);
        } else {
            log.info("提供userId={}，使用SupervisorAgent + StreamingResponse（个性化服务）", userId);
            return handleSupervisorChat(message, userId);
        }
    }

    /**
     * 处理客服助手对话（无个性化服务）
     */
    private SseEmitter handleCustomerServiceChat(String message) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        CompletableFuture.runAsync(() -> {
            try {
                String response = customerServiceAgent.chat(message);
                sendSseEvent(emitter, "message", Map.of("content", response));
                sendSseEvent(emitter, "message", Map.of("done", true));
            } catch (Exception e) {
                log.error("客服助手处理失败", e);
                sendSseEvent(emitter, "message", Map.of("error", e.getMessage()));
                emitter.completeWithError(e);
            } finally {
                try { emitter.complete(); } catch (Exception ignored) {}
            }
        }, executorService);

        emitter.onTimeout(() -> emitter.complete());
        emitter.onError(error -> emitter.completeWithError(Objects.requireNonNull(error)));
        return emitter;
    }

    /**
     * 处理 SupervisorAgent + StreamingResponse 对话（核心方法）
     *
     * 执行流程：
     * 阶段1（同步）：SupervisorAgent 协调 L1 专家 Agent，进度事件实时推送
     * 阶段2（流式）：StreamingResponseAgent 逐字输出，每个 token 作为 SSE 事件发送
     */
    private SseEmitter handleSupervisorChat(String message, String userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        // 创建监听器（进度推送）
        SSEAgentListener listener = new SSEAgentListener(emitter, userId);

        CompletableFuture.runAsync(() -> {
            long totalStartTime = System.currentTimeMillis();
            try {
                // ===== 阶段1：同步 Supervisor 执行 =====
                log.info("[阶段1] SupervisorAgent开始处理: userId={}, message={}", userId, message);

                long t1 = System.currentTimeMillis();
                SupervisorAgent agent = supervisorAgentFactory.createWithListener(listener, userId);
                long t1Create = System.currentTimeMillis();
                log.info("⏱️ [耗时] SupervisorAgent创建: {}ms", t1Create - t1);

                String supervisorResult = agent.invoke(message);
                long t1Invoke = System.currentTimeMillis();
                log.info("⏱️ [耗时] SupervisorAgent.invoke(): {}ms", t1Invoke - t1Create);

                // 清理 LangChain4j 调试信息
                String cleanedResult = supervisorAgentFactory.cleanDebugInfo(supervisorResult);
                long t1Clean = System.currentTimeMillis();
                log.info("⏱️ [耗时] cleanDebugInfo: {}ms", t1Clean - t1Invoke);

                log.info("[阶段1] SupervisorAgent完成，结果长度: {} 字符，总耗时: {}ms", cleanedResult.length(), t1Clean - t1);

                // ===== 预提取卡片数据 =====
                // 从 ToolExecutionContext 中提取工具返回的卡片数据，直接发送给前端
                // 不依赖 Supervisor LLM 或 StreamingResponseAgent LLM 透传
                java.util.List<String> preExtractedCards = com.xx.jaseatschoicejava.agent.context.ToolExecutionContext.extractCardJsonData();
                boolean hasPreExtractedCards = !preExtractedCards.isEmpty();
                if (hasPreExtractedCards) {
                    log.info("[预提取] 从工具结果中提取到 {} 张卡片，直接发送给前端", preExtractedCards.size());
                    for (String cardJson : preExtractedCards) {
                        sendSseEvent(emitter, "message", Map.of("card_data", cardJson, "type", "card"));
                    }
                }

                // ===== 阶段2：直接输出阶段1结果 =====
                log.info("[阶段2] 直接输出阶段1结果: userId={}", userId);

                boolean hasResultCards = !preExtractedCards.isEmpty();
                emitFinalResponse(emitter, cleanedResult, hasResultCards);

                long totalEndTime = System.currentTimeMillis();
                log.info("⏱️ [耗时] 总耗时: {}ms (阶段1={}ms, 阶段2={}ms)",
                        totalEndTime - totalStartTime, t1Clean - t1, totalEndTime - t1Clean);

                try { emitter.complete(); } catch (Exception ignored) {}
                log.info("Supervisor + Direct Response 完成: userId={}", userId);

            } catch (Exception e) {
                log.error("SupervisorAgent处理失败: userId={}", userId, e);
                // 通过SSE发送错误事件，前端可以正常显示错误信息
                sendSseEvent(emitter, "message", Map.of("error", "处理失败，请重试"));
                // 使用complete()而非completeWithError()，避免异常传播到Spring MVC的
                // GlobalExceptionHandler，导致尝试用text/event-stream Content-Type返回JSON
                try { emitter.complete(); } catch (Exception ignored) {}
            } finally {
                // 清理工具执行上下文，防止 ThreadLocal 泄漏
                com.xx.jaseatschoicejava.agent.context.ToolExecutionContext.clear();
            }
        }, executorService);

        // 设置超时和错误回调
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时: userId={}", userId);
            try { emitter.complete(); } catch (Exception ignored) {}
        });

        emitter.onError(error -> {
            Throwable safeError = Objects.requireNonNull(error);
            log.error("SSE连接错误: userId={}", userId, safeError);
            try { emitter.completeWithError(safeError); } catch (Exception ignored) {}
        });

        emitter.onCompletion(() -> log.debug("SSE连接完成: userId={}", userId));

        return emitter;
    }

    /**
     * POST方式的SSE流式聊天（支持更复杂的请求体）
     */
    @Operation(summary = "POST方式SSE流式聊天", description = "支持复杂请求体的流式聊天")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStreamPost(@RequestBody ChatRequest request) {
        return chatStream(request.getMessage(), request.getUserId());
    }

    /**
     * 直接输出阶段1结果，必要时同步拆出卡片数据。
     *
     * @param emitter SSE 发送器
     * @param result 阶段1清理后的结果
     * @param includeCardExtraction 是否从结果中额外提取卡片事件
     */
    private void emitFinalResponse(SseEmitter emitter, String result, boolean includeCardExtraction) {
        final String CARD_START = "[CARD_DATA_START]";
        final String CARD_END = "[CARD_DATA_END]";

        StringBuilder plainText = new StringBuilder();
        int cursor = 0;

        while (cursor < result.length()) {
            int startIdx = result.indexOf(CARD_START, cursor);
            if (startIdx == -1) {
                plainText.append(result.substring(cursor));
                break;
            }

            plainText.append(result, cursor, startIdx);

            int endIdx = result.indexOf(CARD_END, startIdx + CARD_START.length());
            if (endIdx == -1) {
                break;
            }

            if (includeCardExtraction) {
                String cardJson = result.substring(startIdx + CARD_START.length(), endIdx).trim();
                if (!cardJson.isEmpty()) {
                    sendSseEvent(emitter, "message", Map.of("card_data", cardJson, "type", "card"));
                }
            }

            cursor = endIdx + CARD_END.length();
        }

        String finalText = plainText.toString().trim();
        if (!finalText.isEmpty()) {
            sendSseEvent(emitter, "message", Map.of("type", "info", "content", finalText));
        }

        sendSseEvent(emitter, "message", Map.of("done", true));
    }

    /**
     * 发送 SSE 事件
     */
    private void sendSseEvent(SseEmitter emitter, String eventName, Object data) {
        try {
            final String jsonData = Objects.requireNonNull(objectMapper.writeValueAsString(data));
            final String safeEventName = Objects.requireNonNull(eventName);
            emitter.send(SseEmitter.event().name(safeEventName).data(jsonData));
        } catch (java.io.IOException e) {
            log.debug("SSE事件发送失败（连接可能已关闭）: {}", e.getMessage());
        }
    }

    /**
     * 聊天请求DTO
     */
    public static class ChatRequest {
        private String message;
        private String userId;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }
}
