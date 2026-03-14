package com.xx.jaseatschoicejava.service.impl;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.ChatCompletionCreateParams;
import ai.z.openapi.service.model.ChatMessage;
import ai.z.openapi.service.model.ToolCalls;
import ai.z.openapi.service.model.ChatTool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.ai.function.AiFunctionDefinitionsOptimized;
import com.xx.jaseatschoicejava.ai.function.AiFunctionExecutorOptimized;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import com.xx.jaseatschoicejava.service.ZhipuAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智谱AI服务实现类（集成Function Calling）
 * 使用官方SDK并支持完整的Function Calling功能
 *
 * @author Claude
 * @since 2026-03-14
 */
@Slf4j
@Service
@Primary  // 标记为主要实现，优先使用
public class ZhipuAIServiceImpl implements ZhipuAIService {

    @Resource
    private ZhipuAiClient zhipuClient;

    @Resource
    private ZhipuAIConfig zhipuAIConfig;

    @Resource
    private AiFunctionExecutorOptimized functionExecutor;

    @Resource
    private AiFunctionDefinitionsOptimized functionDefinitions;

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

    // 食谱推荐系统提示词
    private static final String RECIPE_PROMPT = """
            你是一位专业的烹饪顾问。请根据用户的需求推荐合适的食谱。
            每个食谱应包含：菜名、卡路里、难度等级、食材清单、详细步骤。
            请以JSON数组格式返回，例如：
            [
              {
                "name": "菜名",
                "calorie": 150.5,
                "difficulty": "简单",
                "ingredients": "食材1, 食材2",
                "steps": "步骤1; 步骤2; 步骤3"
              }
            ]
            """;

    // 推荐理由生成系统提示词
    private static final String RECOMMENDATION_REASON_PROMPT = """
            你是"佳食宜选"的智能推荐系统。请根据菜品信息、用户画像和上下文，生成有说服力的推荐理由。
            要求：
            1. 理由要个性化，结合用户偏好
            2. 突出菜品的营养价值和特色
            3. 考虑当前时间和天气因素
            4. 语言简洁有力，15-30字
            5. 避免空洞的套话
            """;

    // 菜品识别系统提示词（方案B：严格格式要求）
    private static final String DISH_RECOGNITION_PROMPT = """
            你是专业的菜品识别专家。请分析图片中的菜品并识别。

            【重要】必须严格按以下JSON格式返回，不允许任何额外文字、markdown标记或解释：
            {
              "name": "菜品名称",
              "calories": 数字,
              "protein": 数字,
              "fat": 数字,
              "carbs": 数字,
              "difficulty": "简单/中等/困难",
              "preparationTime": "XX分钟",
              "ingredients": ["食材1", "食材2"],
              "tags": ["标签1", "标签2"],
              "confidence": 0.95
            }

            字段说明：
            - name: 菜品名称（字符串）
            - calories: 每100克的热量（数字，0-2000之间）
            - protein: 每100克的蛋白质含量（数字，0-100克）
            - fat: 每100克的脂肪含量（数字，0-100克）
            - carbs: 每100克的碳水化合物含量（数字，0-200克）
            - difficulty: 烹饪难度，必须是"简单"、"中等"或"困难"之一
            - preparationTime: 估算烹饪时间（字符串，格式："XX分钟"）
            - ingredients: 主要食材列表（数组，3-8个字符串）
            - tags: 菜系、口味、特色标签（数组，2-5个字符串）
            - confidence: 识别置信度（数字，0-1之间）

            【严格要求】
            1. 只返回纯JSON对象，不要添加```json标记
            2. 不要在JSON前后添加任何解释文字
            3. 确保JSON格式正确，可以被直接解析
            4. 所有必填字段都必须存在
            5. 数值必须在合理范围内
            """;

    @Override
    public String chat(String message, List<Map<String, String>> conversationHistory) {
        log.info("=== AI聊天请求（Function Calling版本） ===");
        log.info("用户消息: {}", message);

        try {
            // 1. 构建消息列表
            List<ChatMessage> messages = buildMessages(message, conversationHistory);

            // 2. 构建请求（包含工具函数定义）
            ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                    .model(zhipuAIConfig.getModel())
                    .messages(messages)
                    .tools(convertToolDefinitionsToSDK())
                    .temperature(0.7f)
                    .build();

            log.debug("发送AI请求，模型: {}", zhipuAIConfig.getModel());

            // 3. 调用SDK
            var response = zhipuClient.chat().createChatCompletion(request);

            if (response == null || response.getData() == null ||
                response.getData().getChoices() == null || response.getData().getChoices().isEmpty()) {
                log.warn("AI响应为空");
                return "抱歉，AI服务暂时无响应，请稍后重试。";
            }

            // 4. 处理响应
            List<ToolCalls> toolCalls = response.getData().getChoices()
                    .get(0)
                    .getMessage()
                    .getToolCalls();

            // 如果需要调用工具函数
            if (!CollectionUtils.isEmpty(toolCalls)) {
                log.info("AI请求调用工具函数，数量: {}", toolCalls.size());
                return handleToolCalls(messages, toolCalls);
            }

            // 直接返回AI回复
            Object contentObj = response.getData().getChoices().get(0).getMessage().getContent();
            String content = contentObj != null ? contentObj.toString() : "";
            log.info("AI直接回复: {}", content);
            return content;

        } catch (Exception e) {
            log.error("AI聊天失败", e);
            return "抱歉，AI服务出现错误：" + e.getMessage();
        }
    }

    /**
     * 构建消息列表
     */
    private List<ChatMessage> buildMessages(String userMessage, List<Map<String, String>> conversationHistory) {
        List<ChatMessage> messages = new ArrayList<>();

        // 添加系统提示词
        String systemPrompt = functionDefinitions != null ?
                functionDefinitions.getPrimarySystemPrompt() : DIET_ASSISTANT_PROMPT;

        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(ChatMessage.builder()
                    .role("system")
                    .content(systemPrompt)
                    .build());
            log.debug("添加系统提示词，长度: {} 字符", systemPrompt.length());
        }

        // 添加历史对话
        if (!CollectionUtils.isEmpty(conversationHistory)) {
            for (Map<String, String> msg : conversationHistory) {
                String role = msg.get("role");
                String content = msg.get("content");

                if ("user".equals(role)) {
                    messages.add(ChatMessage.builder()
                            .role("user")
                            .content(content)
                            .build());
                } else if ("assistant".equals(role)) {
                    messages.add(ChatMessage.builder()
                            .role("assistant")
                            .content(content)
                            .build());
                }
            }
            log.debug("添加历史对话，数量: {}", conversationHistory.size());
        }

        // 添加当前用户消息
        messages.add(ChatMessage.builder()
                .role("user")
                .content(userMessage)
                .build());

        return messages;
    }

    /**
     * 处理工具函数调用
     */
    private String handleToolCalls(List<ChatMessage> messages, List<ToolCalls> toolCalls) {
        log.info("开始处理工具函数调用...");

        // 添加AI的请求消息
        ChatMessage assistantMessage = ChatMessage.builder()
                .role("assistant")
                .content("")
                .toolCalls(toolCalls)
                .build();
        messages.add(assistantMessage);

        // 执行所有工具函数
        for (ToolCalls toolCall : toolCalls) {
            String functionName = toolCall.getFunction().getName();
            JsonNode argumentsJson = toolCall.getFunction().getArguments();

            log.info("执行工具函数: {}", functionName);
            log.debug("函数参数: {}", argumentsJson);

            try {
                // 解析参数
                Map<String, Object> arguments = parseArguments(argumentsJson != null ? argumentsJson.toString() : null);

                // 执行函数
                String result = functionExecutor.executeFunction(functionName, arguments);

                log.info("工具函数执行成功: {}, 结果长度: {} 字符", functionName,
                        result != null ? result.length() : 0);

                // 添加函数结果到对话
                messages.add(ChatMessage.builder()
                        .role("tool")
                        .content(result)
                        .toolCallId(toolCall.getId())
                        .build());

            } catch (Exception e) {
                log.error("工具函数执行失败: {}", functionName, e);
                String errorMsg = "错误：" + e.getMessage();
                messages.add(ChatMessage.builder()
                        .role("tool")
                        .content(errorMsg)
                        .toolCallId(toolCall.getId())
                        .build());
            }
        }

        // 再次调用AI，生成最终回复
        try {
            log.info("再次调用AI生成最终回复...");

            ChatCompletionCreateParams followUpRequest = ChatCompletionCreateParams.builder()
                    .model(zhipuAIConfig.getModel())
                    .messages(messages)
                    .tools(convertToolDefinitionsToSDK())
                    .temperature(0.7f)
                    .build();

            var followUpResponse = zhipuClient.chat().createChatCompletion(followUpRequest);

            if (followUpResponse == null || followUpResponse.getData() == null ||
                    followUpResponse.getData().getChoices() == null ||
                    followUpResponse.getData().getChoices().isEmpty()) {
                return "抱歉，生成回复时出现错误。";
            }

            Object finalReplyObj = followUpResponse.getData().getChoices().get(0).getMessage().getContent();
            String finalReply = finalReplyObj != null ? finalReplyObj.toString() : "";
            log.info("AI最终回复: {}", finalReply);
            return finalReply;

        } catch (Exception e) {
            log.error("工具函数调用后生成回复失败", e);
            return "抱歉，处理您的请求时出现了错误。";
        }
    }

    /**
     * 解析函数参数JSON字符串
     */
    private Map<String, Object> parseArguments(String argumentsJson) {
        try {
            if (argumentsJson == null || argumentsJson.isEmpty()) {
                return new HashMap<>();
            }
            return objectMapper.readValue(argumentsJson, HashMap.class);
        } catch (Exception e) {
            log.error("解析函数参数失败: {}", argumentsJson, e);
            return new HashMap<>();
        }
    }

    /**
     * 转换工具函数定义为SDK格式
     */
    private List<ChatTool> convertToolDefinitionsToSDK() {
        try {
            if (functionDefinitions == null) {
                log.warn("工具函数定义未初始化，返回空列表");
                return new ArrayList<>();
            }

            List<AiFunctionDefinitionsOptimized.ToolFunction> toolFunctions =
                    functionDefinitions.getToolFunctions();

            if (toolFunctions == null || toolFunctions.isEmpty()) {
                log.warn("工具函数列表为空");
                return new ArrayList<>();
            }

            List<ChatTool> convertedTools = new ArrayList<>();

            for (AiFunctionDefinitionsOptimized.ToolFunction func : toolFunctions) {
                ChatTool tool = ChatTool.builder()
                        .type("function")
                        .function(ai.z.openapi.service.model.ChatFunction.builder()
                                .name(func.getName())
                                .description(func.getDescription())
                                .parameters(func.getParameters())
                                .build())
                        .build();
                convertedTools.add(tool);
            }

            log.debug("转换工具函数定义，数量: {}", convertedTools.size());
            return convertedTools;

        } catch (Exception e) {
            log.error("转换工具函数定义失败", e);
            return new ArrayList<>();
        }
    }

    // ==================== 以下为原有功能保留（使用SDK重构） ====================

    @Override
    public List<Map<String, Object>> recommendRecipe(String foodName) {
        log.info("AI食谱推荐，食物名称: {}", foodName);

        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(ChatMessage.builder()
                    .role("system")
                    .content(RECIPE_PROMPT)
                    .build());
            messages.add(ChatMessage.builder()
                    .role("user")
                    .content("请推荐适合" + foodName + "的食谱")
                    .build());

            ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                    .model(zhipuAIConfig.getModel())
                    .messages(messages)
                    .temperature(0.8f)
                    .build();

            var response = zhipuClient.chat().createChatCompletion(request);
            Object contentObj = response.getData().getChoices().get(0).getMessage().getContent();
            String content = contentObj != null ? contentObj.toString() : "";

            // 清理AI返回内容中的markdown代码块标记
            if (content.contains("```")) {
                // 移除 ```json 或 ``` 标记
                content = content.replaceAll("```json\\s*", "")
                                .replaceAll("```\\s*", "");
                // 移除结尾的 ```标记
                content = content.replaceAll("\\s*```$", "");
            }

            // 解析JSON返回
            JsonNode jsonNode = objectMapper.readTree(content);
            List<Map<String, Object>> recipes = new ArrayList<>();

            if (jsonNode.isArray()) {
                for (JsonNode node : jsonNode) {
                    Map<String, Object> recipe = new HashMap<>();
                    recipe.put("name", node.get("name").asText());
                    recipe.put("calorie", node.get("calorie").asDouble());
                    recipe.put("difficulty", node.get("difficulty").asText());
                    recipe.put("ingredients", node.get("ingredients").asText());
                    recipe.put("steps", node.get("steps").asText());
                    recipes.add(recipe);
                }
            }

            return recipes;

        } catch (Exception e) {
            log.error("AI食谱推荐失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> analyzeNutrition(String foodName) {
        log.info("AI营养分析，食物名称: {}", foodName);

        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(ChatMessage.builder()
                    .role("system")
                    .content(DIET_ASSISTANT_PROMPT)
                    .build());
            messages.add(ChatMessage.builder()
                    .role("user")
                    .content("请分析" + foodName + "的营养成分")
                    .build());

            ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                    .model(zhipuAIConfig.getModel())
                    .messages(messages)
                    .temperature(0.7f)
                    .build();

            var response = zhipuClient.chat().createChatCompletion(request);
            Object contentObj = response.getData().getChoices().get(0).getMessage().getContent();
            String content = contentObj != null ? contentObj.toString() : "";

            Map<String, Object> result = new HashMap<>();
            result.put("foodName", foodName);
            result.put("analysis", content);

            return result;

        } catch (Exception e) {
            log.error("AI营养分析失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }

    @Override
    public Map<String, Object> recognizeDish(String imageUrl) {
        log.info("AI菜品识别，图片URL: {}", imageUrl);

        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(ChatMessage.builder()
                    .role("system")
                    .content(DISH_RECOGNITION_PROMPT)
                    .build());
            messages.add(ChatMessage.builder()
                    .role("user")
                    .content(List.of(
                            Map.of("type", "image_url", "image_url", Map.of("url", imageUrl))
                    ))
                    .build());

            ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                    .model(zhipuAIConfig.getVisionModel())
                    .messages(messages)
                    .temperature(0.3f)
                    .maxTokens(500)
                    .build();

            var response = zhipuClient.chat().createChatCompletion(request);
            Object contentObj = response.getData().getChoices().get(0).getMessage().getContent();
            String content = contentObj != null ? contentObj.toString() : "";

            // 清理AI返回内容中的markdown代码块标记
            if (content.contains("```")) {
                content = content.replaceAll("```json\\s*", "")
                                .replaceAll("```\\s*", "");
                content = content.replaceAll("\\s*```$", "");
            }

            JsonNode jsonNode = objectMapper.readTree(content);
            Map<String, Object> result = new HashMap<>();
            result.put("name", jsonNode.get("name").asText());
            result.put("calories", jsonNode.get("calories").asDouble());
            result.put("protein", jsonNode.get("protein").asDouble());
            result.put("fat", jsonNode.get("fat").asDouble());
            result.put("carbs", jsonNode.get("carbs").asDouble());
            result.put("confidence", jsonNode.get("confidence").asDouble());

            return result;

        } catch (Exception e) {
            log.error("AI菜品识别失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }

    @Override
    public Map<String, Object> recognizeDishWithBase64(String imageBase64) {
        log.info("AI菜品识别（Base64）");

        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(ChatMessage.builder()
                    .role("system")
                    .content(DISH_RECOGNITION_PROMPT)
                    .build());
            messages.add(ChatMessage.builder()
                    .role("user")
                    .content(List.of(
                            Map.of("type", "image_url", "image_url",
                                    Map.of("url", "data:image/jpeg;base64," + imageBase64))
                    ))
                    .build());

            ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                    .model(zhipuAIConfig.getVisionModel())
                    .messages(messages)
                    .temperature(0.3f)
                    .maxTokens(500)
                    .build();

            var response = zhipuClient.chat().createChatCompletion(request);
            Object contentObj = response.getData().getChoices().get(0).getMessage().getContent();
            String content = contentObj != null ? contentObj.toString() : "";

            // 清理AI返回内容中的markdown代码块标记
            if (content.contains("```")) {
                content = content.replaceAll("```json\\s*", "")
                                .replaceAll("```\\s*", "");
                content = content.replaceAll("\\s*```$", "");
            }

            JsonNode jsonNode = objectMapper.readTree(content);
            Map<String, Object> result = new HashMap<>();
            result.put("name", jsonNode.get("name").asText());
            result.put("calories", jsonNode.get("calories").asDouble());
            result.put("confidence", jsonNode.get("confidence").asDouble());

            return result;

        } catch (Exception e) {
            log.error("AI菜品识别（Base64）失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }

    @Override
    public Map<String, Object> optimizeRecipe(String originalRecipe) {
        log.info("AI食谱优化");

        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(ChatMessage.builder()
                    .role("system")
                    .content(RECIPE_PROMPT)
                    .build());
            messages.add(ChatMessage.builder()
                    .role("user")
                    .content("请优化以下食谱：" + originalRecipe)
                    .build());

            ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                    .model(zhipuAIConfig.getModel())
                    .messages(messages)
                    .temperature(0.7f)
                    .build();

            var response = zhipuClient.chat().createChatCompletion(request);
            Object contentObj = response.getData().getChoices().get(0).getMessage().getContent();
            String content = contentObj != null ? contentObj.toString() : "";

            Map<String, Object> result = new HashMap<>();
            result.put("original", originalRecipe);
            result.put("optimized", content);

            return result;

        } catch (Exception e) {
            log.error("AI食谱优化失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }

    @Override
    public String generateRecommendationReason(String dishName, Map<String, Object> userProfile, Map<String, Object> context) {
        log.info("AI生成推荐理由，菜品: {}", dishName);

        try {
            String prompt = String.format("""
                    请为菜品"%s"生成推荐理由。
                    用户画像：%s
                    上下文信息：%s
                    """,
                    dishName,
                    userProfile != null ? userProfile.toString() : "无",
                    context != null ? context.toString() : "无"
            );

            List<ChatMessage> messages = new ArrayList<>();
            messages.add(ChatMessage.builder()
                    .role("system")
                    .content(RECOMMENDATION_REASON_PROMPT)
                    .build());
            messages.add(ChatMessage.builder()
                    .role("user")
                    .content(prompt)
                    .build());

            ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                    .model(zhipuAIConfig.getModel())
                    .messages(messages)
                    .temperature(0.8f)
                    .maxTokens(100)
                    .build();

            var response = zhipuClient.chat().createChatCompletion(request);
            Object contentObj = response.getData().getChoices().get(0).getMessage().getContent();
            return contentObj != null ? contentObj.toString() : "";

        } catch (Exception e) {
            log.error("AI生成推荐理由失败", e);
            return "为您精心挑选的优质菜品";
        }
    }
}
