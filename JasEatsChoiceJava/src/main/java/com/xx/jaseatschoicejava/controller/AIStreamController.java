package com.xx.jaseatschoicejava.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.ai.function.AiFunctionDefinitionsOptimized;
import com.xx.jaseatschoicejava.ai.function.AiFunctionExecutorOptimized;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import com.xx.jaseatschoicejava.entity.UserPreference;
import com.xx.jaseatschoicejava.service.UserContextService;
import com.xx.jaseatschoicejava.service.UserPreferenceService;
import com.xx.jaseatschoicejava.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI流式对话控制器 - 参考智谱AI官方API设计
 * 后端职责：接收智谱AI的SSE数据，提取content，封装成{content, done}格式发送给前端
 * 支持用户个性化上下文注入
 * 支持Function Calling功能
 */
@RestController
@RequestMapping("/v1/ai/stream")
public class AIStreamController {

    private static final Logger log = LoggerFactory.getLogger(AIStreamController.class);

    @Resource
    private ZhipuAIConfig zhipuAIConfig;

    @Resource
    private UserContextService userContextService;

    @Resource
    private UserPreferenceService userPreferenceService;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private AiFunctionDefinitionsOptimized functionDefinitions;

    @Resource
    private AiFunctionExecutorOptimized functionExecutor;

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
     * 支持用户个性化上下文注入
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(
            @RequestBody Map<String, Object> params,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 创建SSE发射器，超时时间60秒
        SseEmitter emitter = new SseEmitter(60000L);

        // 异步处理流式请求
        new Thread(() -> {
            try {
                String userMessage = (String) params.get("message");

                // 1. 从JWT提取userId
                String userId = extractUserId(authHeader);
                log.debug("AI聊天请求 - userId: {}, message: {}", userId, userMessage);

                // 2. 获取用户偏好设置
                boolean enablePersonalData = getEnableAiPersonalData(userId);
                log.debug("用户个性化数据授权 - userId: {}, enabled: {}", userId, enablePersonalData);

                // 3. 构建用户上下文
                String userContext = userContextService.buildUserContext(userId, enablePersonalData);
                if (!userContext.isEmpty()) {
                    System.out.println("已注入用户上下文 - userId: " + userId + ", context length: " + userContext.length());
                }

                // 4. 构建请求参数（包含工具函数定义）
                Map<String, Object> requestBody = buildChatRequest(userMessage, userContext, null);

                // 5. 发送SSE流式请求到智谱AI并转发给前端（传递userId用于函数调用）
                processStreamResponse(emitter, requestBody, new ArrayList<>(), userId);

            } catch (Exception e) {
                log.error("流式聊天失败", e);
                sendErrorResponse(emitter, "抱歉，AI服务暂时不可用，请稍后重试。");
            }
        }).start();

        return emitter;
    }

    /**
     * 从Authorization header中提取userId
     */
    private String extractUserId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                return jwtUtil.extractUserId(token);
            } catch (Exception e) {
                log.warn("从JWT提取userId失败: {}", e.getMessage());
            }
        }
        return null;  // 未登录用户返回null
    }

    /**
     * 获取用户是否启用AI个性化数据
     * 隐私保护原则：默认未授权，需要用户主动开启
     */
    private boolean getEnableAiPersonalData(String userId) {
        if (userId == null) {
            return false;  // 未登录用户不使用个性化数据
        }

        try {
            UserPreference preference = userPreferenceService.getByUserId(userId);
            if (preference != null && preference.getEnableAiPersonalData() != null) {
                return preference.getEnableAiPersonalData();
            }
        } catch (Exception e) {
            log.warn("获取用户偏好设置失败: userId={}, error={}", userId, e.getMessage());
        }

        return false;  // 默认未授权（隐私保护原则）
    }

    /**
     * 构建工具调用后的后续请求（Function Calling第二轮）
     * @param conversationHistory 已包含完整对话历史（system → user → assistant → tool）
     * @return 请求体
     */
    private Map<String, Object> buildFollowUpRequest(List<Map<String, Object>> conversationHistory) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", zhipuAIConfig.getModel());
        request.put("messages", conversationHistory);

        // 添加工具函数定义
        List<Map<String, Object>> tools = buildToolDefinitions();
        if (!tools.isEmpty()) {
            request.put("tools", tools);
            log.info("已添加{}个工具函数定义", tools.size());
        }

        // 启用深度思考模式
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
     * 构建聊天请求参数（参考官方API示例）
     * @param userMessage 用户消息
     * @param userContext 用户上下文（可为空）
     * @param conversationHistory 对话历史（用于工具调用后的多轮对话）
     */
    private Map<String, Object> buildChatRequest(String userMessage, String userContext, List<Map<String, Object>> conversationHistory) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", zhipuAIConfig.getModel());

        // 构建消息列表
        List<Map<String, Object>> messages = new ArrayList<>();

        // 系统提示词（使用Function Calling优化的提示词）
        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        // 使用Function Calling的系统提示词
        String fullPrompt = functionDefinitions != null ?
                functionDefinitions.getPrimarySystemPrompt() + userContext :
                DIET_ASSISTANT_PROMPT + userContext;
        systemMessage.put("content", fullPrompt);
        messages.add(systemMessage);

        // 添加对话历史（如果有）
        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            messages.addAll(conversationHistory);
        }

        // 用户消息处理
        // Function Calling场景下，如果对话历史的最后一条是tool消息，则不应再添加用户消息
        boolean shouldAddUserMessage = true;
        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            Map<String, Object> lastMessage = conversationHistory.get(conversationHistory.size() - 1);
            String lastRole = (String) lastMessage.get("role");
            if ("tool".equals(lastRole)) {
                // 最后一条是tool消息，这是Function Calling场景，不应添加用户消息
                shouldAddUserMessage = false;
                log.info("检测到Function Calling场景，跳过用户消息添加");
            }
        }

        if (shouldAddUserMessage && userMessage != null && !userMessage.trim().isEmpty()) {
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", userMessage);
            messages.add(message);
        }

        request.put("messages", messages);

        // 添加工具函数定义
        List<Map<String, Object>> tools = buildToolDefinitions();
        if (!tools.isEmpty()) {
            request.put("tools", tools);
            log.info("已添加{}个工具函数定义", tools.size());
        }

        // 启用深度思考模式
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
     * 构建工具函数定义列表（转换为HTTP API格式）
     */
    private List<Map<String, Object>> buildToolDefinitions() {
        try {
            if (functionDefinitions == null) {
                log.warn("工具函数定义未初始化");
                return new ArrayList<>();
            }

            List<AiFunctionDefinitionsOptimized.ToolFunction> toolFunctions =
                    functionDefinitions.getToolFunctions();

            if (toolFunctions == null || toolFunctions.isEmpty()) {
                log.warn("工具函数列表为空");
                return new ArrayList<>();
            }

            List<Map<String, Object>> tools = new ArrayList<>();

            for (AiFunctionDefinitionsOptimized.ToolFunction func : toolFunctions) {
                Map<String, Object> tool = new HashMap<>();
                tool.put("type", "function");

                Map<String, Object> functionDef = new HashMap<>();
                // 使用反射获取私有字段的值
                try {
                    java.lang.reflect.Field nameField = AiFunctionDefinitionsOptimized.ToolFunction.class.getDeclaredField("name");
                    nameField.setAccessible(true);
                    functionDef.put("name", nameField.get(func));

                    java.lang.reflect.Field descField = AiFunctionDefinitionsOptimized.ToolFunction.class.getDeclaredField("description");
                    descField.setAccessible(true);
                    functionDef.put("description", descField.get(func));

                    java.lang.reflect.Field paramsField = AiFunctionDefinitionsOptimized.ToolFunction.class.getDeclaredField("parameters");
                    paramsField.setAccessible(true);
                    functionDef.put("parameters", paramsField.get(func));

                } catch (Exception e) {
                    log.error("反射访问ToolFunction字段失败", e);
                    continue;
                }

                tool.put("function", functionDef);
                tools.add(tool);
            }

            return tools;

        } catch (Exception e) {
            log.error("构建工具函数定义失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 处理流式响应（支持Function Calling）
     * @param emitter SSE发射器
     * @param requestBody 请求体
     * @param conversationHistory 对话历史（用于工具调用后的二次请求）
     * @param userId 当前登录用户的ID（用于自动注入到工具函数）
     */
    private void processStreamResponse(
            SseEmitter emitter,
            Map<String, Object> requestBody,
            List<Map<String, Object>> conversationHistory,
            String userId) {
        try {
            String apiKey = zhipuAIConfig.getApiKey();
            String jsonBody = objectMapper.writeValueAsString(requestBody);

            // 调试日志：打印请求体（仅打印messages部分）
            try {
                List<Map<String, Object>> messages = (List<Map<String, Object>>) requestBody.get("messages");
                System.out.println("=== 发送给智谱AI的消息列表 ===");
                System.out.println("消息数量: " + (messages != null ? messages.size() : 0));
                if (messages != null) {
                    for (int i = 0; i < messages.size(); i++) {
                        Map<String, Object> msg = messages.get(i);
                        String role = (String) msg.get("role");
                        Object content = msg.get("content");
                        Object toolCalls = msg.get("tool_calls");

                        System.out.println("[" + i + "] role: " + role);
                        if (content != null && !content.toString().isEmpty()) {
                            String contentStr = content.toString();
                            System.out.println("    content: " + (contentStr.length() > 100 ? contentStr.substring(0, 100) + "..." : contentStr));
                        }
                        if (toolCalls != null) {
                            System.out.println("    tool_calls: " + toolCalls);
                        }
                        // 打印tool_call_id（如果有）
                        Object toolCallId = msg.get("tool_call_id");
                        if (toolCallId != null) {
                            System.out.println("    tool_call_id: " + toolCallId);
                        }
                    }
                }
                System.out.println("===============================");
            } catch (Exception e) {
                System.err.println("打印调试信息失败: " + e.getMessage());
            }

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

                // 读取流式响应
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

                // 累积完整的响应（用于检测tool_calls）
                StringBuilder fullResponse = new StringBuilder();
                List<Map<String, Object>> toolCalls = new ArrayList<>();

                String line;
                boolean hasToolCalls = false;

                // 第一轮：读取流式响应并检测tool_calls
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6).trim();

                        // 跳过[DONE]标记
                        if ("[DONE]".equals(data)) {
                            break;
                        }

                        fullResponse.append(data);

                        // 检测是否有tool_calls
                        List<Map<String, Object>> currentToolCalls = extractToolCalls(data);
                        if (currentToolCalls != null && !currentToolCalls.isEmpty()) {
                            hasToolCalls = true;
                            toolCalls.addAll(currentToolCalls);
                        }

                        // 提取content并发送到前端
                        String content = extractContent(data);
                        if (content != null && !content.isEmpty()) {
                            sendContentData(emitter, content, false);
                        }
                    }
                }

                // 如果检测到工具函数调用
                if (hasToolCalls) {
                    System.out.println("检测到工具函数调用，数量: " + toolCalls.size());

                    // 执行工具函数（传递userId用于自动注入）
                    List<Map<String, Object>> updatedHistory = executeToolCalls(
                            requestBody,
                            toolCalls,
                            conversationHistory,
                            userId);

                    // 发送工具调用执行提示
                    sendContentData(emitter, "\n\n🔧 正在执行工具函数...\n\n", false);

                    // 第二轮：使用工具函数结果再次请求AI
                    // 注意：updatedHistory已经包含完整的对话历史（system → user → assistant → tool）
                    // 直接构建请求，不需要再添加用户消息
                    Map<String, Object> followUpRequest = buildFollowUpRequest(updatedHistory);
                    processStreamResponse(emitter, followUpRequest, updatedHistory, userId);
                    return;
                }

                // 没有工具函数调用，正常结束
                sendContentData(emitter, "", true);
                emitter.complete();

            } finally {
                conn.disconnect();
            }

        } catch (Exception e) {
            System.err.println("处理流式响应失败: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(emitter, "抱歉，AI服务处理失败，请稍后重试。");
        }
    }

    /**
     * 执行工具函数调用
     * @param originalRequest 原始请求
     * @param toolCalls 工具调用列表
     * @param conversationHistory 对话历史
     * @param userId 当前登录用户的ID（用于自动注入到工具函数参数）
     * @return 更新后的对话历史
     */
    private List<Map<String, Object>> executeToolCalls(
            Map<String, Object> originalRequest,
            List<Map<String, Object>> toolCalls,
            List<Map<String, Object>> conversationHistory,
            String userId) {

        try {
            // 构建新的对话历史
            List<Map<String, Object>> updatedHistory = new ArrayList<>(conversationHistory);

            // 添加原始用户消息和assistant的tool_calls请求
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> originalMessages =
                    (List<Map<String, Object>>) originalRequest.get("messages");

            if (originalMessages != null) {
                updatedHistory.addAll(originalMessages);
            }

            // 添加assistant的tool_calls消息
            // 注意：assistant消息如果只有tool_calls，不应该有content字段（或者content为null）
            Map<String, Object> assistantMessage = new HashMap<>();
            assistantMessage.put("role", "assistant");
            assistantMessage.put("tool_calls", toolCalls);
            // 不添加content字段，让其为null（符合智谱AI规范）
            updatedHistory.add(assistantMessage);

            // 执行每个工具函数
            for (Map<String, Object> toolCall : toolCalls) {
                String toolCallId = (String) toolCall.get("id");
                System.out.println("处理tool_call，id: " + toolCallId + ", 完整对象: " + toolCall);

                Map<String, Object> function = (Map<String, Object>) toolCall.get("function");
                if (function == null) continue;

                String functionName = (String) function.get("name");
                // arguments现在是字符串形式的JSON，需要解析成Map
                Object argumentsObj = function.get("arguments");
                Map<String, Object> arguments;

                if (argumentsObj instanceof String) {
                    // 字符串形式的JSON，需要解析
                    try {
                        arguments = objectMapper.readValue((String) argumentsObj, Map.class);
                    } catch (Exception e) {
                        System.err.println("解析arguments失败: " + argumentsObj + ", 错误: " + e.getMessage());
                        arguments = new HashMap<>();
                    }
                } else if (argumentsObj instanceof Map) {
                    // 已经是Map对象
                    arguments = (Map<String, Object>) argumentsObj;
                } else {
                    arguments = new HashMap<>();
                }

                System.out.println("执行工具函数: " + functionName + ", 参数: " + arguments + ", 用户ID: " + userId);

                try {
                    // 执行工具函数（传递userId用于自动注入）
                    String result = functionExecutor.executeFunction(functionName, arguments, userId);
                    System.out.println("工具函数执行结果: " + result);

                    // 添加工具函数结果到对话历史
                    Map<String, Object> toolMessage = new HashMap<>();
                    toolMessage.put("role", "tool");
                    toolMessage.put("content", result);
                    toolMessage.put("tool_call_id", toolCall.get("id"));
                    updatedHistory.add(toolMessage);

                } catch (Exception e) {
                    System.err.println("工具函数执行失败: " + functionName + ", 错误: " + e.getMessage());

                    // 添加错误消息
                    Map<String, Object> toolMessage = new HashMap<>();
                    toolMessage.put("role", "tool");
                    toolMessage.put("content", "错误: " + e.getMessage());
                    toolMessage.put("tool_call_id", toolCall.get("id"));
                    updatedHistory.add(toolMessage);
                }
            }

            return updatedHistory;

        } catch (Exception e) {
            System.err.println("执行工具函数失败: " + e.getMessage());
            e.printStackTrace();
            return conversationHistory;
        }
    }

    /**
     * 从响应数据中提取tool_calls
     */
    private List<Map<String, Object>> extractToolCalls(String jsonData) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonData);
            JsonNode choices = jsonNode.get("choices");

            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode delta = choices.get(0).get("delta");
                if (delta != null && delta.has("tool_calls")) {
                    JsonNode toolCallsNode = delta.get("tool_calls");
                    List<Map<String, Object>> toolCalls = new ArrayList<>();

                    if (toolCallsNode.isArray()) {
                        for (JsonNode toolCallNode : toolCallsNode) {
                            Map<String, Object> toolCall = new HashMap<>();

                            // 提取id
                            if (toolCallNode.has("id")) {
                                toolCall.put("id", toolCallNode.get("id").asText());
                            }

                            // 提取type（重要：智谱AI规范要求）
                            if (toolCallNode.has("type")) {
                                toolCall.put("type", toolCallNode.get("type").asText());
                            } else {
                                // 如果没有type字段，默认设置为"function"
                                toolCall.put("type", "function");
                            }

                            // 提取function信息
                            if (toolCallNode.has("function")) {
                                JsonNode functionNode = toolCallNode.get("function");
                                Map<String, Object> function = new HashMap<>();

                                if (functionNode.has("name")) {
                                    function.put("name", functionNode.get("name").asText());
                                }

                                if (functionNode.has("arguments")) {
                                    // arguments必须保持为字符串形式的JSON（符合智谱AI规范）
                                    String argsStr = functionNode.get("arguments").asText();
                                    function.put("arguments", argsStr);
                                }

                                toolCall.put("function", function);
                            }

                            if (!toolCall.isEmpty()) {
                                toolCalls.add(toolCall);
                            }
                        }
                    }

                    return toolCalls;
                }
            }
        } catch (Exception e) {
            System.err.println("解析tool_calls失败: " + e.getMessage());
        }
        return null;
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
