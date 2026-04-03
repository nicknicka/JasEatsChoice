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
        SSEAgentListener listener = new SSEAgentListener(emitter, userId);

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

                // 跨 token 文本缓冲区：LLM 可能将 [CARD_DATA_START] 拆成多个 token 输出
                // 策略：KMP 流式匹配，逐字符扫描，O(m) per token
                StringBuilder textBuffer = new StringBuilder();     // 普通模式缓冲区
                StringBuilder cardDataBuffer = new StringBuilder(); // 收集模式：卡片 JSON 缓冲区
                StringBuilder matchBuffer = new StringBuilder();    // KMP 部分匹配暂存区
                final String CARD_START = "[CARD_DATA_START]";
                final String CARD_END = "[CARD_DATA_END]";
                final boolean[] isCollecting = {false};
                final int[] kmpMatchLen = {0}; // KMP 状态：已匹配 CARD_END 的字符数
                final int[] kmpFailure = _buildKMPFailure(CARD_END);

                String memoryId = userId + "_" + UUID.randomUUID().toString().substring(0, 8);

                TokenStream tokenStream = streamingResponseAgent.streamResponse(
                        message, cleanedResult, userId, memoryId
                );

                tokenStream
                    .onPartialResponse(token -> {
                        if (token == null || token.isEmpty()) return;
                        fullResponse.append(token);

                        // 收集模式：KMP 流式匹配 CARD_END，O(m) per token
                        if (isCollecting[0]) {
                            // Bug2修复：合并 _scanForMarkers 切换到收集模式时残留在 textBuffer 的数据
                            String effectiveToken = token;
                            if (textBuffer.length() > 0) {
                                String pending = textBuffer.toString();
                                int markerIdx = pending.indexOf(CARD_START);
                                if (markerIdx != -1) {
                                    effectiveToken = pending.substring(markerIdx + CARD_START.length()) + token;
                                }
                                textBuffer.setLength(0);
                            }

                            for (int i = 0; i < effectiveToken.length(); i++) {
                                char c = effectiveToken.charAt(i);

                                // KMP 状态转移
                                while (kmpMatchLen[0] > 0 && CARD_END.charAt(kmpMatchLen[0]) != c) {
                                    kmpMatchLen[0] = kmpFailure[kmpMatchLen[0] - 1];
                                }
                                if (CARD_END.charAt(kmpMatchLen[0]) == c) {
                                    kmpMatchLen[0]++;
                                }

                                if (kmpMatchLen[0] == CARD_END.length()) {
                                    // 匹配到 CARD_END！matchBuffer 中的部分匹配属于标记，丢弃
                                    String cardJson = cardDataBuffer.toString().trim();
                                    if (!cardJson.isEmpty()) {
                                        sendSseEvent(emitter, "message", Map.of("card_data", cardJson, "type", "card"));
                                    }
                                    cardDataBuffer.setLength(0);
                                    matchBuffer.setLength(0);
                                    kmpMatchLen[0] = 0;
                                    isCollecting[0] = false;

                                    // CARD_END 后的剩余字符转入普通模式
                                    String remaining = effectiveToken.substring(i + 1);
                                    if (!remaining.isEmpty()) {
                                        textBuffer.append(remaining);
                                        _scanForMarkers(textBuffer, isCollecting, CARD_START, CARD_END, emitter);
                                    }
                                    return;
                                }

                                // Bug1修复：区分部分匹配字符和确定非标记字符
                                if (kmpMatchLen[0] > 0) {
                                    // 可能是 CARD_END 前缀，暂存到 matchBuffer
                                    matchBuffer.append(c);
                                } else {
                                    // 确定非标记字符
                                    if (matchBuffer.length() > 0) {
                                        // 误报：之前的部分匹配实际是卡片数据
                                        cardDataBuffer.append(matchBuffer);
                                        matchBuffer.setLength(0);
                                    }
                                    cardDataBuffer.append(c);
                                }
                            }
                            return;
                        }

                        // 普通模式：追加 token 后扫描标记
                        textBuffer.append(token);
                        _scanForMarkers(textBuffer, isCollecting, CARD_START, CARD_END, emitter);
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
                // 通过SSE发送错误事件，前端可以正常显示错误信息
                sendSseEvent(emitter, "message", Map.of("error", "处理失败，请重试"));
                // 使用complete()而非completeWithError()，避免异常传播到Spring MVC的
                // GlobalExceptionHandler，导致尝试用text/event-stream Content-Type返回JSON
                try { emitter.complete(); } catch (Exception ignored) {}
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
     * 普通模式下扫描 textBuffer 中的卡片标记
     * 检测到 CARD_START 时切换为收集模式，检测到完整卡片时发送 card_data 事件
     */
    private void _scanForMarkers(StringBuilder textBuffer, boolean[] isCollecting,
                                  String CARD_START, String CARD_END, SseEmitter emitter) {
        while (true) {
            String buf = textBuffer.toString();
            int startIdx = buf.indexOf(CARD_START);
            int endIdx = buf.indexOf(CARD_END);

            // 情况1：同时有开始和结束标记
            if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                String before = buf.substring(0, startIdx).trim();
                if (!before.isEmpty()) {
                    sendSseEvent(emitter, "message", Map.of("content", before));
                }
                String cardJson = buf.substring(startIdx + CARD_START.length(), endIdx).trim();
                if (!cardJson.isEmpty()) {
                    sendSseEvent(emitter, "message", Map.of("card_data", cardJson, "type", "card"));
                }
                textBuffer.setLength(0);
                textBuffer.append(buf.substring(endIdx + CARD_END.length()));
                continue;
            }

            // 情况2：只有开始标记，进入收集模式
            if (startIdx != -1 && endIdx == -1) {
                String before = buf.substring(0, startIdx).trim();
                if (!before.isEmpty()) {
                    sendSseEvent(emitter, "message", Map.of("content", before));
                }
                textBuffer.setLength(0);
                textBuffer.append(buf.substring(startIdx));
                isCollecting[0] = true;
                break;
            }

            // 情况3：无开始标记，检查不完整前缀
            if (startIdx == -1) {
                String pending = _getPartialMarkerPrefix(buf, CARD_START);
                if (pending != null) {
                    String safeText = buf.substring(0, buf.length() - pending.length()).trim();
                    if (!safeText.isEmpty()) {
                        sendSseEvent(emitter, "message", Map.of("content", safeText));
                    }
                    textBuffer.setLength(0);
                    textBuffer.append(pending);
                    break;
                }
                if (!buf.isEmpty()) {
                    sendSseEvent(emitter, "message", Map.of("content", buf));
                    textBuffer.setLength(0);
                }
            }
            break;
        }
    }

    /**
     * 构建 KMP 失败函数（failure table）
     * 用于流式匹配 CARD_END 标记，避免每收到一个 token 就对整个缓冲区做 indexOf
     *
     * 时间复杂度：O(pattern.length())，只构建一次
     *
     * @param pattern 要匹配的模式串（如 "[CARD_DATA_END]"）
     * @return 失败函数数组
     */
    private int[] _buildKMPFailure(String pattern) {
        int[] fail = new int[pattern.length()];
        fail[0] = 0;
        int j = 0;
        for (int i = 1; i < pattern.length(); i++) {
            while (j > 0 && pattern.charAt(i) != pattern.charAt(j)) {
                j = fail[j - 1];
            }
            if (pattern.charAt(i) == pattern.charAt(j)) {
                j++;
            }
            fail[i] = j;
        }
        return fail;
    }

    /**
     * 检查缓冲区末尾是否包含标记的不完整前缀
     * 例如缓冲区为 "一些文本[CARD_DA"，返回 "[CARD_DA"（可能是 [CARD_DATA_START] 的前缀）
     *
     * @param buffer 当前缓冲区内容
     * @param marker 完整标记字符串（如 [CARD_DATA_START]）
     * @return 不完整的前缀，或 null（无前缀风险）
     */
    private String _getPartialMarkerPrefix(String buffer, String marker) {
        // 检查缓冲区末尾最多 marker.length()-1 个字符是否是 marker 的前缀
        int maxCheck = Math.min(buffer.length(), marker.length() - 1);
        for (int len = maxCheck; len >= 1; len--) {
            String tail = buffer.substring(buffer.length() - len);
            if (marker.startsWith(tail)) {
                return tail;
            }
        }
        return null;
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
