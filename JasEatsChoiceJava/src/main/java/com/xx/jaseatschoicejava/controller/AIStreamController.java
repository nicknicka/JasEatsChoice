package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * AI流式对话控制器
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
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody Map<String, Object> params) {
        // 创建SSE发射器，超时时间30秒
        SseEmitter emitter = new SseEmitter(30000L);

        // 异步处理流式请求
        new Thread(() -> {
            try {
                String message = (String) params.get("message");

                // 构建请求体
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", zhipuAIConfig.getModel());

                // 构建消息列表
                List<Map<String, String>> messages = new ArrayList<>();

                // 添加系统提示词
                Map<String, String> systemMessage = new HashMap<>();
                systemMessage.put("role", "system");
                systemMessage.put("content", DIET_ASSISTANT_PROMPT);
                messages.add(systemMessage);

                // 添加当前用户消息
                Map<String, String> userMessage = new HashMap<>();
                userMessage.put("role", "user");
                userMessage.put("content", message);
                messages.add(userMessage);

                requestBody.put("messages", messages);

                // 启用深度思考模式
                Map<String, String> thinking = new HashMap<>();
                thinking.put("type", "enabled");
                requestBody.put("thinking", thinking);

                requestBody.put("temperature", 0.7);
                requestBody.put("top_p", 0.9);
                requestBody.put("max_tokens", 65536);

                // 启用流式传输
                requestBody.put("stream", true);

                // 发送SSE流式请求到智谱AI
                sendSseRequest(emitter, requestBody);

            } catch (Exception e) {
                log.error("流式聊天失败", e);
                try {
                    emitter.send(SseEmitter.event()
                            .data("{\"error\": \"" + e.getMessage() + "\"}")
                            .build());
                    emitter.complete();
                } catch (Exception ex) {
                    log.error("发送错误消息失败", ex);
                }
            }
        }).start();

        return emitter;
    }

    /**
     * 发送SSE请求到智谱AI并转发给前端
     */
    private void sendSseRequest(SseEmitter emitter, Map<String, Object> requestBody) throws Exception {
        String apiKey = zhipuAIConfig.getApiKey();
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        URL url = new URI(zhipuAIConfig.getBaseUrl()).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            // 设置请求方法和超时
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);

            // 设置请求头
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);

            // 发送请求体
            conn.getOutputStream().write(jsonBody.getBytes(StandardCharsets.UTF_8));

            // 读取流式响应
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

                    try {
                        // 解析JSON并提取content
                        JsonNode jsonNode = objectMapper.readTree(data);
                        JsonNode choices = jsonNode.get("choices");
                        if (choices != null && choices.isArray() && choices.size() > 0) {
                            JsonNode delta = choices.get(0).get("delta");
                            if (delta != null && delta.has("content")) {
                                String content = delta.get("content").asText();

                                // 发送内容到前端
                                emitter.send(SseEmitter.event()
                                        .data("{\"content\": \"" +
                                                content.replace("\"", "\\\"") +
                                                "\"}")
                                        .build());
                            }
                        }
                    } catch (Exception e) {
                        // 忽略解析错误，继续处理下一行
                        log.debug("解析SSE数据失败: {}", data);
                    }
                }
            }

            // 发送完成信号
            emitter.send(SseEmitter.event()
                    .data("{\"done\": true}")
                    .build());
            emitter.complete();

        } finally {
            conn.disconnect();
        }
    }
}
