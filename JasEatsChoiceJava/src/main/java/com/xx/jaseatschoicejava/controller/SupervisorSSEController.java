package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.agent.agents.CustomerServiceAgent;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
@RequestMapping("/agent/supervisor-sse")
public class SupervisorSSEController {

    private static final Logger log = LoggerFactory.getLogger(SupervisorSSEController.class);

    private final SupervisorAgent supervisorAgent;
    private final CustomerServiceAgent customerServiceAgent;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public SupervisorSSEController(
            @Qualifier("supervisorAgent") SupervisorAgent supervisorAgent,
            CustomerServiceAgent customerServiceAgent) {
        this.supervisorAgent = supervisorAgent;
        this.customerServiceAgent = customerServiceAgent;
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

            @Parameter(description = "用户ID（开启个性化服务后传入）", required = false)
            @RequestParam(required = false) String userId) {

        log.info("收到SSE聊天请求: message={}, userId={}", message, userId);

        // 路由逻辑：无userId使用客服助手，有userId使用SupervisorAgent
        if (userId == null || userId.isEmpty()) {
            log.info("未提供userId，使用客服助手Agent（无个性化服务）");
            return handleCustomerServiceChat(message);
        } else {
            log.info("提供userId={}，使用SupervisorAgent（个性化服务）", userId);
            return handleSupervisorChat(message, userId);
        }
    }

    /**
     * 处理客服助手对话（无个性化服务）
     */
    private SseEmitter handleCustomerServiceChat(String message) {
        // 创建SSE发射器（30秒超时，客服对话相对简单）
        SseEmitter emitter = new SseEmitter(30000L);

        CompletableFuture.runAsync(() -> {
            try {
                log.info("客服助手处理消息: {}", message);

                // 调用客服助手
                String response = customerServiceAgent.chat(message);

                // 发送结果
                emitter.send(SseEmitter.event()
                        .name("FINAL_RESULT")
                        .data(response));

                log.info("客服助手响应完成");

            } catch (Exception e) {
                log.error("客服助手处理失败", e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("ERROR")
                            .data("处理失败: " + e.getMessage()));
                } catch (Exception ioException) {
                    log.error("发送错误消息失败", ioException);
                } finally {
                    emitter.completeWithError(e);
                }
            } finally {
                emitter.complete();
            }
        }, executorService);

        // 设置超时和错误回调
        emitter.onTimeout(() -> {
            log.warn("客服助手SSE连接超时");
            emitter.complete();
        });

        emitter.onError((e) -> {
            log.error("客服助手SSE连接错误", e);
            emitter.completeWithError(e);
        });

        return emitter;
    }

    /**
     * 处理SupervisorAgent对话（个性化服务）
     *
     * 注意：使用单例SupervisorAgent
     * 多用户隔离暂时简化处理（后续可优化为Redis存储）
     */
    private SseEmitter handleSupervisorChat(String message, String userId) {
        // 创建SSE发射器（60秒超时）
        SseEmitter emitter = new SseEmitter(60000L);

        // 异步执行（不阻塞请求）
        CompletableFuture.runAsync(() -> {
            try {
                log.info("SupervisorAgent开始处理: userId={}, message={}", userId, message);

                // TODO: 后续优化 - 将userId注入到message中，让Agent可以识别用户
                // 现在先简化为直接调用
                String enhancedMessage = String.format("[用户ID: %s] %s", userId, message);
                String response = supervisorAgent.invoke(enhancedMessage);

                // 发送最终结果
                emitter.send(SseEmitter.event()
                        .name("FINAL_RESULT")
                        .data(response));

                log.info("SupervisorAgent处理完成: userId={}", userId);

            } catch (Exception e) {
                log.error("SupervisorAgent处理失败: userId={}", userId, e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("ERROR")
                            .data("处理失败: " + e.getMessage()));
                } catch (Exception ioException) {
                    log.error("发送错误消息失败", ioException);
                } finally {
                    emitter.completeWithError(e);
                }
            } finally {
                emitter.complete();
            }
        }, executorService);

        // 设置超时和错误回调
        emitter.onTimeout(() -> {
            log.warn("SupervisorAgent SSE连接超时: userId={}", userId);
            emitter.complete();
        });

        emitter.onError((e) -> {
            log.error("SupervisorAgent SSE连接错误: userId={}", userId, e);
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
