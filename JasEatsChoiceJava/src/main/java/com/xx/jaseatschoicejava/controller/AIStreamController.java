package com.xx.jaseatschoicejava.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI流式对话控制器 - 参考智谱AI官方API设计
 * 后端职责：接收智谱AI的SSE数据，提取content，封装成{content, done}格式发送给前端
 */
@Slf4j
@RestController
@RequestMapping("/v1/ai/stream")
public class AIStreamController {

    @Resource
    private ZhipuAIConfig zhipuAIConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 饮食助手系统提示词
    private static final String DIET_ASSISTANT_PROMPT = """
            你是"佳食宜选"的专业AI饮食助手。你的职责包括：
            1. 提供个性化的饮食建议和营养搭配指导
            2. 推荐适合不同需求的食谱（如减肥、增肌、控糖等）
            3. 分析食物营养成分和健康影响
            4. 解答用户关于饮食健康的疑问

            请用专业但易懂的语言回答，给出实用且可操作的建议。
            """;

    /**
     * 流式AI聊天接口（SSE）
     * 前端发送: { "message": "用户的问题" }
     * 后端响应: { "content": "文本片段", "done": false } 或 { "content": "", "done": true }
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody Map<String, Object> params) {
        // 创建SSE发射器，超时时间60秒
        SseEmitter emitter = new SseEmitter(60000L);

        // 异步处理流式请求
        new Thread(() -> {
            try {
                String userMessage = (String) params.get("message");

                // 构建请求参数（参考官方API示例结构）
                Map<String, Object> requestBody = buildChatRequest(userMessage);

                // 发送SSE流式请求到智谱AI并转发给前端
                processStreamResponse(emitter, requestBody);

            } catch (Exception e) {
                log.error("流式聊天失败", e);
                sendErrorResponse(emitter, "抱歉，AI服务暂时不可用，请稍后重试。");
            }
        }).start();

        return emitter;
    }

    /**
     * 构建聊天请求参数（参考官方API示例）
     */
    private Map<String, Object> buildChatRequest(String userMessage) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", zhipuAIConfig.getModel());

        // 构建消息列表（参考官方API的messages结构）
        List<Map<String, String>> messages = new ArrayList<>();

        // 系统提示词（对应官方API的SYSTEM角色）
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", DIET_ASSISTANT_PROMPT);
        messages.add(systemMessage);

        // 用户消息（对应官方API的USER角色）
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", userMessage);
        messages.add(message);

        request.put("messages", messages);

        // 启用深度思考模式（参考官方API的thinking参数）
        Map<String, String> thinking = new HashMap<>();
        thinking.put("type", "enabled");
        request.put("thinking", thinking);

        // 流式输出参数
        request.put("stream", true);
        request.put("temperature", 0.7);
        request.put("max_tokens", 65536);

        return request;
    }

    /**
     * 处理流式响应（参考官方API的Flowable订阅模式）
     * 对应官方API的: data -> { 处理流式数据块 }
     */
    private void processStreamResponse(SseEmitter emitter, Map<String, Object> requestBody) {
        try {
            String apiKey = zhipuAIConfig.getApiKey();
            String jsonBody = objectMapper.writeValueAsString(requestBody);

            URL url = new URI(zhipuAIConfig.getBaseUrl()).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            try {
                // 设置请求方法和超时
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(60000);
                conn.setDoOutput(true);

                // 设置请求头
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);

                // 发送请求体
                conn.getOutputStream().write(jsonBody.getBytes(StandardCharsets.UTF_8));

                // 读取流式响应（模拟官方API的Flowable流）
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6).trim();

                        // 跳过[DONE]标记
                        if ("[DONE]".equals(data)) {
                            break;
                        }

                        // 提取content并发送到前端（对应官方API的delta.getContent()）
                        String content = extractContent(data);

                        if (content != null && !content.isEmpty()) {
                            // 封装成标准格式：{ content: string, done: false }
                            sendContentData(emitter, content, false);
                        }
                    }
                }

                // 发送完成标记（对应官方API的完成事件: () -> {}）
                sendContentData(emitter, "", true);
                emitter.complete();

            } finally {
                conn.disconnect();
            }

        } catch (Exception e) {
            log.error("处理流式响应失败", e);
            sendErrorResponse(emitter, "抱歉，AI服务处理失败，请稍后重试。");
        }
    }

    /**
     * 提取content内容（对应官方API的delta.getContent()）
     */
    private String extractContent(String jsonData) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonData);
            JsonNode choices = jsonNode.get("choices");

            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode delta = choices.get(0).get("delta");
                if (delta != null && delta.has("content")) {
                    return delta.get("content").asText();
                }
            }
        } catch (Exception e) {
            log.debug("解析content失败，跳过此数据: {}", jsonData);
        }
        return null;
    }

    /**
     * 发送内容数据到前端
     * 格式: { "content": "文本内容", "done": false }
     */
    private void sendContentData(SseEmitter emitter, String content, boolean done) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("content", content);
            response.put("done", done);

            // 直接传递对象，让Spring Boot自动序列化为JSON
            emitter.send(SseEmitter.event()
                    .data(response)
                    .build());

            if (!done) {
                log.debug("发送内容: {}", content);
            } else {
                log.info("发送完成标记");
            }
        } catch (Exception e) {
            log.error("发送数据到前端失败", e);
        }
    }

    /**
     * 发送错误响应（对应官方API的错误处理: error -> {}）
     */
    private void sendErrorResponse(SseEmitter emitter, String errorMessage) {
        try {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("content", errorMessage);
            errorResponse.put("done", true);

            // 直接传递对象，让Spring Boot自动序列化为JSON
            emitter.send(SseEmitter.event()
                    .data(errorResponse)
                    .build());
            emitter.complete();
        } catch (Exception ex) {
            log.error("发送错误消息失败", ex);
            emitter.completeWithError(ex);
        }
    }
}
