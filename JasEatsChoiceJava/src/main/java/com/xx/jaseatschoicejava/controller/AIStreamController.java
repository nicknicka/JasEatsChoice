package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.agent.service.IntelligentAdvisorAgent;
import com.xx.jaseatschoicejava.common.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * AI流式响应控制器（使用LangChain4j）
 * 提供SSE流式聊天接口
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
    private IntelligentAdvisorAgent intelligentAdvisorAgent;

    /**
     * SSE流式聊天接口（简化版）
     *
     * @param params 请求参数
     * @return SseEmitter
     */
    @ApiOperation(value = "SSE流式聊天", notes = "使用Server-Sent Events实现流式响应（目前为简化版，待升级到真正的流式）")
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
            log.info("   - 参数详情: {}", params);

            // 3. 在新线程中处理（避免阻塞）
            new Thread(() -> {
                try {
                    // 发送开始事件
                    emitter.send(SseEmitter.event()
                        .name("start")
                        .data(Map.of("message", "开始处理...")));

                    // 调用智能顾问Agent（总协调器，会自动路由到合适的专家Agent）
                    String response = intelligentAdvisorAgent.chat(message, userId);

                    log.info("开始发送SSE流式响应，总长度: {} 字符", response.length());

                    // 模拟流式输出（逐字发送）
                    // 注意：SSE的data()方法会对特殊字符进行处理，需要将换行符转义
                    for (int i = 0; i < response.length(); i++) {
                        char ch = response.charAt(i);

                        // 记录换行符
                        if (ch == '\n') {
                            log.debug("发送换行符，位置: {}", i);
                        }

                        // 将字符包装为JSON对象，确保换行符等特殊字符被正确传输
                        Map<String, String> charData = Map.of("char", String.valueOf(ch));
                        emitter.send(SseEmitter.event()
                            .name("message")
                            .data(charData));
                        Thread.sleep(20); // 模拟打字效果
                    }

                    log.info("SSE流式响应发送完成");

                    // 发送完成事件
                    emitter.send(SseEmitter.event()
                        .name("end")
                        .data(Map.of("done", true)));

                    emitter.complete();

                } catch (Exception e) {
                    log.error("流式聊天处理失败", e);
                    try {
                        emitter.send(SseEmitter.event()
                            .name("error")
                            .data("处理失败：" + e.getMessage()));
                        emitter.completeWithError(e);
                    } catch (Exception ex) {
                        log.error("发送错误失败", ex);
                    }
                }
            }).start();

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
        result.put("version", "2.0.0");
        result.put("framework", "LangChain4j 0.29.1");
        result.put("note", "目前为简化版，待升级到真正的流式Agent");
        result.put("timestamp", System.currentTimeMillis());

        return ResponseResult.success(result);
    }
}
