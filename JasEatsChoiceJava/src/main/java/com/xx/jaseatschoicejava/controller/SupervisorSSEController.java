package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.agent.listener.SSEAgentListener;
import com.xx.jaseatschoicejava.agent.service.SupervisorAgentFactory;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SupervisorAgent SSE 流式输出控制器
 *
 * 提供实时流式输出，展示SupervisorAgent的执行过程
 * 支持每个用户独立的ChatMemory（Redis + MySQL混合存储）
 *
 * @author Claude
 * @since 2026-03-26
 */
@Tag(name = "Supervisor监督代理（SSE流式）", description = "SupervisorAgent流式输出接口")
@RestController
@RequestMapping("/api/agent/supervisor-sse")
@CrossOrigin(origins = "*")
public class SupervisorSSEController {

    private static final Logger log = LoggerFactory.getLogger(SupervisorSSEController.class);

    private final SupervisorAgentFactory supervisorAgentFactory;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public SupervisorSSEController(SupervisorAgentFactory supervisorAgentFactory) {
        this.supervisorAgentFactory = supervisorAgentFactory;
    }

    /**
     * SSE流式聊天接口
     *
     * 实时推送Agent执行过程，包括：
     * - Agent调用开始
     * - Agent调用完成
     * - 工具执行过程
     * - 最终结果
     *
     * @param message 用户消息
     * @param userId 用户ID（推荐传入，以保持对话历史）
     * @return SSE流
     */
    @Operation(summary = "SSE流式聊天", description = "实时推送Agent执行过程和结果")
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @Parameter(description = "用户消息", required = true)
            @RequestParam String message,

            @Parameter(description = "用户ID（推荐传入，以保持对话历史）", required = false)
            @RequestParam(required = false) String userId) {

        log.info("收到SSE聊天请求: message={}, userId={}", message, userId);

        // 如果没有userId，生成临时ID（不保存历史）
        if (userId == null || userId.isEmpty()) {
            userId = UUID.randomUUID().toString();
            log.info("未提供userId，生成临时ID: {}", userId);
        }

        // 创建SSE发射器（60秒超时）
        SseEmitter emitter = new SseEmitter(60000L);

        // 创建监听器
        SSEAgentListener listener = new SSEAgentListener(emitter);

        // 创建带监听器的SupervisorAgent（使用userId作为memoryId）
        SupervisorAgent agent = supervisorAgentFactory.createWithListener(listener, userId);

        // 异步执行（不阻塞请求）
        final String finalUserId = userId;
        CompletableFuture.runAsync(() -> {
            try {
                // 1. 获取原始结果
                String originalResponse = agent.invoke(message);

                // 2. 渲染为卡片格式
                String renderedResponse = supervisorAgentFactory.renderCards(originalResponse);

                // 3. 发送最终结果
                listener.getEmitter().send(SseEmitter.event()
                        .name("FINAL_RESULT")
                        .data(renderedResponse));

                log.info("SSE聊天完成: message={}, userId={}", message, finalUserId);

            } catch (Exception e) {
                log.error("SSE聊天失败: userId={}", finalUserId, e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("ERROR")
                            .data("处理失败: " + e.getMessage()));
                } catch (Exception ioException) {
                    log.error("发送错误消息失败", ioException);
                } finally {
                    emitter.completeWithError(e);
                }
            }
        }, executorService);

        // 设置超时和错误回调
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时: userId={}", finalUserId);
            emitter.complete();
        });

        emitter.onError((e) -> {
            log.error("SSE连接错误: userId={}", finalUserId, e);
            emitter.completeWithError(e);
        });

        return emitter;
    }

    /**
     * POST方式的SSE流式聊天（支持更复杂的请求体）
     *
     * @param request 聊天请求
     * @return SSE流
     */
    @Operation(summary = "POST方式SSE流式聊天", description = "支持复杂请求体的流式聊天")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStreamPost(@RequestBody ChatRequest request) {
        return chatStream(request.getMessage(), request.getUserId());
    }

    /**
     * 聊天请求DTO
     */
    public static class ChatRequest {
        private String message;
        private String userId;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }
}
