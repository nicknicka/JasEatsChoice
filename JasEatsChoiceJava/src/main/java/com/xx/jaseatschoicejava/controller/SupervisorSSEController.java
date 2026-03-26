package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.agent.agents.CustomerServiceAgent;
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
@RequestMapping("/agent/supervisor-sse")
public class SupervisorSSEController {

    private static final Logger log = LoggerFactory.getLogger(SupervisorSSEController.class);

    private final SupervisorAgentFactory supervisorAgentFactory;
    private final CustomerServiceAgent customerServiceAgent;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public SupervisorSSEController(
            SupervisorAgentFactory supervisorAgentFactory,
            CustomerServiceAgent customerServiceAgent) {
        this.supervisorAgentFactory = supervisorAgentFactory;
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
     */
    private SseEmitter handleSupervisorChat(String message, String userId) {
        // 创建SSE发射器（60秒超时）
        SseEmitter emitter = new SseEmitter(60000L);

        // 创建监听器
        SSEAgentListener listener = new SSEAgentListener(emitter);

        // 创建带监听器的SupervisorAgent（使用userId作为memoryId）
        SupervisorAgent agent = supervisorAgentFactory.createWithListener(listener, userId);

        // 异步执行（不阻塞请求）
        final String finalUserId = userId;
        final SseEmitter finalEmitter = emitter;
        CompletableFuture.runAsync(() -> {
            try {
                log.info("SupervisorAgent开始处理: userId={}, message={}", userId, message);

                // 1. 获取原始结果
                String originalResponse = agent.invoke(message);

                // 2. 渲染为卡片格式
                String renderedResponse = supervisorAgentFactory.renderCards(originalResponse);

                // 3. 发送最终结果（检查连接是否还活着）
                log.info("📤 [Controller] 准备发送FINAL_RESULT, userId={}, length={}",
                    finalUserId, renderedResponse.length());

                try {
                    // ✅ 使用 "message" 事件名，前端才能接收
                    finalEmitter.send(SseEmitter.event()
                            .name("message")  // 改为message事件名
                            .data(renderedResponse));

                    log.info("✅ [Controller] FINAL_RESULT发送成功, userId={}", finalUserId);

                    // 发送成功后完成SSE流
                    finalEmitter.complete();
                    log.info("✅ [Controller] SSE连接正常完成: userId={}", finalUserId);
                } catch (IllegalStateException e) {
                    // 连接已关闭，记录WARN级别日志
                    log.warn("⚠️ [Controller] SSE连接已关闭，无法发送最终结果: userId={}", finalUserId);
                } catch (Exception e) {
                    log.error("❌ [Controller] 发送FINAL_RESULT失败: userId={}, error={}",
                        finalUserId, e.getMessage(), e);
                }

                log.info("🏁 [Controller] SupervisorAgent处理完成: message={}, userId={}", message, finalUserId);

            } catch (Exception e) {
                log.error("SupervisorAgent处理失败: userId={}", finalUserId, e);
                try {
                    finalEmitter.send(SseEmitter.event()
                            .name("ERROR")
                            .data("处理失败: " + e.getMessage()));
                } catch (IllegalStateException ie) {
                    // 连接已关闭，记录DEBUG级别日志
                    log.debug("SSE连接已关闭，无法发送错误消息: userId={}", finalUserId);
                } catch (Exception ioException) {
                    log.error("发送错误消息失败（非连接关闭问题）", ioException);
                } finally {
                    try {
                        finalEmitter.completeWithError(e);
                    } catch (IllegalStateException ie) {
                        // emitter已经完成，忽略
                        log.debug("SSE emitter已经完成: userId={}", finalUserId);
                    }
                }
            }
        }, executorService);

        // 设置超时和错误回调
        emitter.onTimeout(() -> {
            log.warn("SupervisorAgent SSE连接超时: userId={}", finalUserId);
            try {
                emitter.complete();
            } catch (IllegalStateException e) {
                log.debug("SSE emitter已经完成（超时回调）: userId={}", finalUserId);
            }
        });

        emitter.onError((e) -> {
            log.error("SupervisorAgent SSE连接错误: userId={}", finalUserId, e);
            try {
                emitter.completeWithError(e);
            } catch (IllegalStateException ie) {
                log.debug("SSE emitter已经完成（错误回调）: userId={}", finalUserId);
            }
        });

        emitter.onCompletion(() -> {
            log.debug("SupervisorAgent SSE连接完成: userId={}", finalUserId);
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
