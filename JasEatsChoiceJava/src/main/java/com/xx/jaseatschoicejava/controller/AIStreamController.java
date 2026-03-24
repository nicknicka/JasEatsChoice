package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.agent.agents.stream.StreamingIntelligentAssistantAgent;
import com.xx.jaseatschoicejava.common.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * AI流式响应控制器（使用LangChain4j）
 * 提供SSE流式聊天接口 - 流式输出
 *
 * @author Claude
 * @since 2026-03-22
 */
@Api(tags = "AI流式对话（LangChain4j）")
@RestController
@RequestMapping("/v1/ai/stream")
public class AIStreamController {

    private static final Logger log = LoggerFactory.getLogger(AIStreamController.class);

    @Resource
    private StreamingIntelligentAssistantAgent streamingIntelligentAssistantAgent;

    /**
     * SSE流式聊天接口（真正的流式输出）
     *
     * @param params 请求参数
     * @return SseEmitter
     */
    @ApiOperation(value = "SSE流式聊天", notes = "使用LangChain4j的StreamingChatLanguageModel实现真正的流式响应")
    @PostMapping("/chat")
    public SseEmitter streamChat(@RequestBody Map<String, Object> params) {
        // 创建SseEmitter（30秒超时）
        SseEmitter emitter = new SseEmitter(30000L);

        try {
            // 1. 提取参数
            String message = (String) params.get("message");
            String userId = (String) params.getOrDefault("userId", "anonymous");

            // 2. 参数验证
            if (message == null || message.trim().isEmpty()) {
                emitter.send(SseEmitter.event()
                    .name("error")
                    .data("消息内容不能为空"));
                emitter.complete();
                return emitter;
            }

            log.info("📥 收到流式聊天请求");
            log.info("   - 用户ID: {}", userId);
            log.info("   - 消息内容: {}", message);

            // 3. 调用真正的流式Agent（传递userId）
            streamingIntelligentAssistantAgent.chat(message, userId)
                .onNext(token -> {
                    // 处理每个token（从LLM流式接收）
                    try {
                        if (token != null && !token.isEmpty()) {
                            // 将token包装为JSON对象，确保换行符等特殊字符被正确传输
                            Map<String, String> charData = Map.of("char", token);
                            emitter.send(SseEmitter.event()
                                .name("message")
                                .data(charData));
                        }
                    } catch (IOException e) {
                        log.error("发送token失败", e);
                    }
                })
                .onComplete(response -> {
                    // 流完成时调用
                    try {
                        log.info("✅ 流式响应完成");
                        emitter.send(SseEmitter.event()
                            .name("end")
                            .data(Map.of("done", true)));
                        emitter.complete();
                    } catch (IOException e) {
                        log.error("发送完成事件失败", e);
                        emitter.completeWithError(e);
                    }
                })
                .onError(error -> {
                    // 发生错误时调用
                    log.error("❌ 流式响应出错", error);
                    try {
                        emitter.send(SseEmitter.event()
                            .name("error")
                            .data("处理失败：" + error.getMessage()));
                        emitter.completeWithError(error);
                    } catch (IOException e) {
                        log.error("发送错误事件失败", e);
                    }
                })
                .start(); // 启动流式处理

        } catch (Exception e) {
            log.error("创建SseEmitter失败", e);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * 健康检查接口
     *
     * @return 服务状态
     */
    @ApiOperation(value = "健康检查", notes = "检查AI流式服务是否正常")
    @GetMapping("/health")
    public ResponseResult<?> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "AI Stream (LangChain4j)");
        result.put("version", "3.0.0");
        result.put("framework", "LangChain4j 0.34.0");
        result.put("streaming", "true");
        result.put("note", "已升级到真正的流式Agent，使用ZhipuAiStreamingChatModel");
        result.put("timestamp", System.currentTimeMillis());

        return ResponseResult.success(result);
    }
}
