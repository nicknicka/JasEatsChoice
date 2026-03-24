package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.service.StreamingAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

/**
 * 流式Agent控制器
 * 提供Server-Sent Events (SSE)方式的流式输出
 *
 * @author Claude
 * @since 2026-03-24
 */
@RestController
@RequestMapping("/api/agent/stream")
@CrossOrigin(origins = "*")
public class StreamingAgentController {

    private static final Logger log = LoggerFactory.getLogger(StreamingAgentController.class);

    @Resource
    private StreamingAgentService streamingAgentService;

    /**
     * 流式对话接口（SSE）
     *
     * @param params 请求参数，包含message和userId
     * @return SseEmitter 流式输出
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody Map<String, Object> params) {
        String message = (String) params.get("message");
        String userId = (String) params.getOrDefault("userId", "anonymous");

        log.info("收到流式对话请求, 用户: {}, 消息: {}", userId, message);

        // 创建SSE发射器（30秒超时）
        SseEmitter emitter = new SseEmitter(30000L);

        // 调用流式服务
        streamingAgentService.chatStream(message, userId, new StreamingAgentService.TokenHandler() {
            @Override
            public void onToken(String token) throws IOException {
                // 发送每个token
                emitter.send(SseEmitter.event()
                        .data(token)
                        .name("message"));
            }

            @Override
            public void onComplete() throws IOException {
                // 发送完成事件
                emitter.send(SseEmitter.event()
                        .data("[DONE]")
                        .name("done"));
                emitter.complete();
                log.info("流式输出完成");
            }

            @Override
            public void onError(Throwable error) throws IOException {
                // 发送错误事件
                emitter.send(SseEmitter.event()
                        .data(error.getMessage())
                        .name("error"));
                emitter.completeWithError(error);
                log.error("流式输出出错", error);
            }
        });

        return emitter;
    }

    /**
     * 简单的流式对话测试接口
     *
     * @param message 用户消息
     * @param userId 用户ID（可选）
     * @return 流式输出
     */
    @GetMapping(value = "/test", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter testStream(
            @RequestParam(defaultValue = "你好") String message,
            @RequestParam(defaultValue = "test_user") String userId) {
        log.info("收到流式测试请求, 用户: {}, 消息: {}", userId, message);

        SseEmitter emitter = new SseEmitter(30000L);

        streamingAgentService.chatStream(message, userId, new StreamingAgentService.TokenHandler() {
            @Override
            public void onToken(String token) throws IOException {
                emitter.send(SseEmitter.event()
                        .data(token)
                        .name("message"));
            }

            @Override
            public void onComplete() throws IOException {
                emitter.send(SseEmitter.event()
                        .data("[DONE]")
                        .name("done"));
                emitter.complete();
            }

            @Override
            public void onError(Throwable error) throws IOException {
                emitter.send(SseEmitter.event()
                        .data(error.getMessage())
                        .name("error"));
                emitter.completeWithError(error);
            }
        });

        return emitter;
    }
}
