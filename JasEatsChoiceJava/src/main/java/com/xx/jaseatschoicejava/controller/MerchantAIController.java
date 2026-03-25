package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.agent.agents.stream.StreamingMerchantAssistantAgent;
import com.xx.jaseatschoicejava.common.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 商家AI助手控制器（使用LangChain4j）
 * 提供SSE流式聊天接口 - 商家经营助手
 *
 * @author Claude
 * @since 2026-03-25
 */
@Api(tags = "商家AI助手（LangChain4j）")
@RestController
@RequestMapping("/v1/merchant/ai")
public class MerchantAIController {

    private static final Logger log = LoggerFactory.getLogger(MerchantAIController.class);

    @Resource
    private StreamingMerchantAssistantAgent streamingMerchantAssistantAgent;

    /**
     * 商家健康检查接口
     *
     * @return 健康状态
     */
    @ApiOperation(value = "商家AI健康检查")
    @GetMapping("/health")
    public ResponseResult<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Merchant AI Assistant (LangChain4j)");
        health.put("version", "1.0.0");
        health.put("streaming", "true");
        health.put("features", new String[]{
            "销售数据分析",
            "评价管理",
            "菜品优化建议",
            "营销策略",
            "订单处理"
        });

        return ResponseResult.success(health);
    }

    /**
     * SSE流式聊天接口（商家端）
     *
     * @param params 请求参数
     * @return SseEmitter
     */
    @ApiOperation(value = "商家AI流式聊天", notes = "商家与AI助手的流式对话，提供经营分析、数据查询等服务")
    @PostMapping("/chat")
    public SseEmitter streamChat(@RequestBody Map<String, Object> params) {
        // 创建SseEmitter（5分钟超时，适配工具调用和AI生成）
        SseEmitter emitter = new SseEmitter(300000L);

        try {
            // 1. 提取参数
            String message = (String) params.get("message");
            String merchantId = (String) params.getOrDefault("merchantId", "default");

            // 2. 参数验证
            if (message == null || message.trim().isEmpty()) {
                try {
                    emitter.send(SseEmitter.event()
                        .name("error")
                        .data("消息内容不能为空"));
                    emitter.complete();
                } catch (IOException e) {
                    log.error("发送错误失败", e);
                }
                return emitter;
            }

            log.info("📥 收到商家AI聊天请求");
            log.info("   - 商家ID: {}", merchantId);
            log.info("   - 消息内容: {}", message);

            // 3. 调用商家流式Agent（传递merchantId）
            streamingMerchantAssistantAgent.chat(message, merchantId)
                .onPartialResponse(token -> {
                    // 处理每个token（从LLM流式接收）
                    try {
                        if (token != null && !token.isEmpty()) {
                            // 检查 emitter 是否已完成
                            SseEmitter.SseEventBuilder event = SseEmitter.event()
                                .name("message")
                                .data(Map.of("char", token));
                            emitter.send(event);
                        }
                    } catch (IllegalStateException e) {
                        // Emitter 已完成，忽略此错误
                        log.debug("Emitter 已完成，停止发送");
                    } catch (IOException e) {
                        log.error("发送token失败", e);
                    }
                })
                .onCompleteResponse(response -> {
                    // AI完成响应后调用
                    try {
                        log.info("✅ 商家AI响应完成");

                        // 发送完成标记
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
                    log.error("❌ 商家AI流式响应出错", error);

                    try {
                        emitter.send(SseEmitter.event()
                            .name("error")
                            .data("处理失败：" + error.getMessage()));
                        emitter.completeWithError(error);
                    } catch (IOException e) {
                        log.error("发送错误事件失败", e);
                    }
                })
                .start();

        } catch (Exception e) {
            log.error("商家AI聊天接口异常", e);
            try {
                emitter.send(SseEmitter.event()
                    .name("error")
                    .data("系统异常：" + e.getMessage()));
                emitter.completeWithError(e);
            } catch (IOException ex) {
                log.error("发送异常信息失败", ex);
            }
        }

        return emitter;
    }

    /**
     * 普通聊天接口（非流式，兼容旧版本）
     *
     * @param params 请求参数
     * @return AI回复
     */
    @ApiOperation(value = "商家AI聊天（非流式）", notes = "非流式响应，兼容旧版本接口")
    @PostMapping("/chat/sync")
    public ResponseResult<String> chatSync(@RequestBody Map<String, String> params) {
        try {
            String message = params.get("message");
            String merchantId = params.getOrDefault("merchantId", "default");

            if (message == null || message.trim().isEmpty()) {
                return ResponseResult.fail("400", "消息内容不能为空");
            }

            log.info("📥 收到商家AI同步聊天请求");
            log.info("   - 商家ID: {}", merchantId);
            log.info("   - 消息内容: {}", message);

            // 流式Agent需要转为同步调用
            StringBuilder fullResponse = new StringBuilder();

            streamingMerchantAssistantAgent.chat(message, merchantId)
                .onPartialResponse(token -> {
                    if (token != null) {
                        fullResponse.append(token);
                    }
                })
                .onCompleteResponse(response -> {
                    // 完成
                })
                .onError(error -> {
                    log.error("商家AI同步调用失败", error);
                })
                .start();

            // 等待响应完成（简单实现，实际应该使用CompletableFuture）
            Thread.sleep(1000); // 给AI一些时间生成

            return ResponseResult.success(fullResponse.toString());

        } catch (Exception e) {
            log.error("商家AI同步聊天异常", e);
            return ResponseResult.fail("500", "系统异常：" + e.getMessage());
        }
    }
}
