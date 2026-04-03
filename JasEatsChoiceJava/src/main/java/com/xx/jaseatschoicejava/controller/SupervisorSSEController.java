package com.xx.jaseatschoicejava.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.agent.agents.CustomerServiceAgent;
import com.xx.jaseatschoicejava.agent.agents.stream.StreamingResponseAgent;
import com.xx.jaseatschoicejava.agent.listener.SSEAgentListener;
import com.xx.jaseatschoicejava.agent.service.SupervisorAgentFactory;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.service.TokenStream;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private final SupervisorAgentFactory supervisorAgentFactory;
    private final StreamingResponseAgent streamingResponseAgent;
    private final CustomerServiceAgent customerServiceAgent;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final com.xx.jaseatschoicejava.service.AIChatHistoryService aiChatHistoryService;
    private final ObjectMapper objectMapper;

    public SupervisorSSEController(
            SupervisorAgentFactory supervisorAgentFactory,
            StreamingResponseAgent streamingResponseAgent,
            CustomerServiceAgent customerServiceAgent,
            com.xx.jaseatschoicejava.service.AIChatHistoryService aiChatHistoryService) {
        this.supervisorAgentFactory = supervisorAgentFactory;
        this.streamingResponseAgent = streamingResponseAgent;
        this.customerServiceAgent = customerServiceAgent;
        this.aiChatHistoryService = aiChatHistoryService;
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
        SseEmitter emitter = new SseEmitter(30000L);

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
        emitter.onError(e -> emitter.completeWithError(e));
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
        SseEmitter emitter = new SseEmitter(120000L); // 120秒超时

        // 创建监听器（进度推送）
        SSEAgentListener listener = new SSEAgentListener(emitter);

        CompletableFuture.runAsync(() -> {
            try {
                // ===== 阶段1：同步 Supervisor 执行 =====
                log.info("[阶段1] SupervisorAgent开始处理: userId={}, message={}", userId, message);

                SupervisorAgent agent = supervisorAgentFactory.createWithListener(listener, userId);
                String supervisorResult = agent.invoke(message);

                // 清理 LangChain4j 调试信息
                String cleanedResult = supervisorAgentFactory.cleanDebugInfo(supervisorResult);

                log.info("[阶段1] SupervisorAgent完成，结果长度: {} 字符", cleanedResult.length());

                // ===== 阶段2：流式 Response 输出 =====
                log.info("[阶段2] StreamingResponseAgent开始流式输出: userId={}", userId);

                // 用于累积完整响应（保存到聊天历史）
                StringBuilder fullResponse = new StringBuilder();

                String memoryId = userId + "_" + UUID.randomUUID().toString().substring(0, 8);

                TokenStream tokenStream = streamingResponseAgent.streamResponse(
                        message, cleanedResult, userId, memoryId
                );

                tokenStream
                    .onPartialResponse(token -> {
                        if (token != null && !token.isEmpty()) {
                            fullResponse.append(token);
                            // 发送 token 级别的 SSE 事件
                            sendSseEvent(emitter, "message", Map.of("content", token));
                        }
                    })
                    .onCompleteResponse(response -> {
                        log.info("[阶段2] StreamingResponseAgent流式输出完成");

                        // 保存聊天历史
                        try {
                            aiChatHistoryService.saveMessage(userId, "user", message);
                            aiChatHistoryService.saveMessage(userId, "ai", fullResponse.toString());
                        } catch (Exception e) {
                            log.error("保存聊天历史失败", e);
                        }

                        // 发送完成事件
                        sendSseEvent(emitter, "message", Map.of("done", true));

                        try { emitter.complete(); } catch (Exception ignored) {}
                        log.info("Supervisor + Streaming 响应完成: userId={}", userId);
                    })
                    .onError(error -> {
                        log.error("[阶段2] StreamingResponseAgent流式输出错误", error);
                        sendSseEvent(emitter, "message", Map.of("error", error.getMessage()));
                        try { emitter.completeWithError(error); } catch (Exception ignored) {}
                    })
                    .start();

            } catch (Exception e) {
                log.error("SupervisorAgent处理失败: userId={}", userId, e);
                sendSseEvent(emitter, "message", Map.of("error", "处理失败: " + e.getMessage()));
                try { emitter.completeWithError(e); } catch (Exception ignored) {}
            }
        }, executorService);

        // 设置超时和错误回调
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时: userId={}", userId);
            try { emitter.complete(); } catch (Exception ignored) {}
        });

        emitter.onError(e -> {
            log.error("SSE连接错误: userId={}", userId, e);
            try { emitter.completeWithError(e); } catch (Exception ignored) {}
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
     * 发送 SSE 事件
     */
    private void sendSseEvent(SseEmitter emitter, String eventName, Object data) {
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            emitter.send(SseEmitter.event().name(eventName).data(jsonData));
        } catch (Exception e) {
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
