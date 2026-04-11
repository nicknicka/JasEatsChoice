package com.xx.jaseatschoicejava.service.impl;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.ChatCompletionCreateParams;
import ai.z.openapi.service.model.ChatMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.service.ZhipuAIService;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.*;
import java.io.IOException;

import com.xx.jaseatschoicejava.config.ZhipuAIConfig;

/**
 * 智谱AI服务实现（视觉识别与特殊功能）
 *
 * 注意：以下功能已迁移到 Agent 系统
 * - chat() → NutritionAiAgent
 * - analyzeNutrition() → NutritionAiAgent
 * - recommendRecipe() → RecommendationAiAgent
 *
 * 本类保留视觉识别和特殊功能
 *
 * @author Claude
 * @since 2026-03-22
 * @updated 2026-04-08 实现真正的AI调用
 */
@Slf4j
@Service
public class ZhipuAIServiceImpl implements ZhipuAIService {

    @Resource
    @Qualifier("visionModel")
    private ChatModel visionModel;

    @Resource
    @Qualifier("aiModel")
    private ChatModel aiModel;

    @Resource
    private ZhipuAiClient zhipuClient;

    @Resource
    private ZhipuAIConfig zhipuAIConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== Prompt 常量 ====================

    /**
     * 菜品识别提示词
     */
    private static final String DISH_RECOGNITION_PROMPT = """
        你是一个专业的菜品识别专家和营养师。请仔细分析这张图片。

        首先判断图片中是否包含食物/菜品。如果不是食物图片（如风景、人物、动物、物品等），请返回：
        {
          "isDish": false,
          "reason": "只能识别食物图片，请确认上传的是食物图片"
        }

        如果图片中确实是食物/菜品，请返回以下JSON格式：
        {
          "isDish": true,
          "name": "菜品名称（具体且准确）",
          "calories": 估算卡路里(数字，单位大卡),
          "protein": 蛋白质含量(数字，单位克),
          "fat": 脂肪含量(数字，单位克),
          "carbs": 碳水化合物含量(数字，单位克),
          "difficulty": "难度(简单/中等/困难)",
          "preparationTime": "准备时间(如：30分钟)",
          "ingredients": ["食材1", "食材2", "食材3"],
          "tags": ["标签1", "标签2"],
          "confidence": 置信度(0-1之间的小数),
          "nutritionScore": 营养评分(1-10分)
        }

        注意：
        1. 必须先判断是否为食物图片，isDish字段必填
        2. 卡路里和营养成分要根据菜品分量合理估算
        3. 食材列表要包含主要食材和调料
        4. 标签可以是菜系、口味、场景等
        5. 只返回JSON，不要其他解释文字
        """;

    /**
     * 食谱优化提示词
     */
    private static final String RECIPE_OPTIMIZATION_PROMPT = """
        你是一个专业的营养师和烹饪专家。用户提供了食材或食谱描述，请基于此推荐优化后的食谱方案。

        用户输入：
        %s

        请严格返回以下JSON格式（不要添加任何其他文字，不要用markdown代码块）：
        [
          {
            "name": "食谱名称",
            "difficulty": "简单/中等/困难",
            "calorie": 数字（总卡路里，单位kcal），
            "ingredients": "食材及用量列表",
            "steps": "详细烹饪步骤",
            "protein": 数字（蛋白质，单位g），
            "fat": 数字（脂肪，单位g），
            "carb": 数字（碳水化合物，单位g）
          }
        ]

        要求：
        1. 返回1-3个优化后的食谱方案
        2. 食材搭配合理，营养均衡
        3. 减少油盐用量，保持健康
        4. 步骤清晰，易于操作
        5. 只返回JSON数组，不要其他解释文字
        """;

    /**
     * 推荐理由生成提示词
     */
    private static final String RECOMMENDATION_REASON_PROMPT = """
        你是一个美食推荐专家。请为以下菜品生成一段吸引人的推荐理由。

        菜品名称：%s
        用户偏好：%s
        当前场景：%s

        要求：
        1. 突出菜品特色和美味
        2. 结合用户偏好和场景
        3. 语言生动有感染力
        4. 控制在50字以内

        直接返回推荐理由文字，不要JSON格式。
        """;

    // ==================== 菜品识别 ====================

    @Override
    public Map<String, Object> recognizeDish(String imageUrl) {
        // 通过URL识别暂不支持，建议使用Base64方式
        log.warn("通过URL识别暂不支持，建议使用Base64方式上传图片");
        Map<String, Object> result = new HashMap<>();
        result.put("error", true);
        result.put("message", "请使用图片上传方式进行识别");
        return result;
    }

    @Override
    public Map<String, Object> recognizeDishWithBase64(String imageBase64) {
        if (imageBase64 == null || imageBase64.isEmpty()) {
            log.error("图片Base64数据为空");
            return Map.of("error", true, "message", "图片数据不能为空");
        }

        try {
            log.info("开始调用视觉模型进行菜品识别，Base64长度: {}", imageBase64.length());

            // 构建多模态消息
            UserMessage userMessage = UserMessage.from(
                    TextContent.from(DISH_RECOGNITION_PROMPT),
                    ImageContent.from(imageBase64, "image/jpeg")
            );

            // 调用视觉模型
            ChatResponse chatResponse = visionModel.chat(userMessage);
            String responseText = chatResponse.aiMessage().text();

            log.info("视觉模型返回结果: {}", responseText.length() > 200 ? responseText.substring(0, 200) + "..." : responseText);

            // 解析JSON响应
            Map<String, Object> result = parseDishRecognitionResult(responseText);

            // 非菜品图片直接返回（parseDishRecognitionResult 已设置 error=true）
            if (Boolean.TRUE.equals(result.get("notDish"))) {
                log.warn("非菜品图片: {}", result.get("message"));
                return result;
            }

            result.put("error", false);

            log.info("菜品识别成功: {}", result.get("name"));
            return result;

        } catch (Exception e) {
            log.error("菜品识别失败", e);
            return Map.of("error", true, "message", "菜品识别失败：" + e.getMessage());
        }
    }

    // ==================== 食谱优化 ====================

    @Override
    public Map<String, Object> optimizeRecipe(String originalRecipe) {
        if (originalRecipe == null || originalRecipe.trim().isEmpty()) {
            log.error("原始食谱为空");
            return Map.of("error", true, "message", "食谱内容不能为空");
        }

        try {
            log.info("开始调用AI进行食谱优化，原文长度: {}", originalRecipe.length());

            // 构建提示词
            String prompt = String.format(RECIPE_OPTIMIZATION_PROMPT, originalRecipe);

            // 调用对话模型，优先使用 LangChain4j，空结果时回退到官方 SDK
            String responseText = callRecipeOptimizationModel(prompt);

            if (responseText.isBlank()) {
                log.warn("食谱优化模型返回空结果");
                return Map.of("error", true, "message", "AI返回空结果，请稍后重试");
            }

            log.info("AI返回优化结果: {}", responseText.length() > 300 ? responseText.substring(0, 300) + "..." : responseText);

            // 解析JSON响应，返回食谱列表
            List<Map<String, Object>> recipes = parseRecipeListResult(responseText);

            if (recipes.isEmpty()) {
                log.warn("AI返回的食谱列表为空");
                return Map.of("error", true, "message", "没有找到合适的优化食谱");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("error", false);
            result.put("recipes", recipes);
            // 兼容桌面端：直接返回数组（Controller 会包装在 ResponseResult.data 中）
            log.info("食谱优化成功，返回 {} 个食谱方案", recipes.size());
            return result;

        } catch (Exception e) {
            log.error("食谱优化失败", e);
            return Map.of("error", true, "message", "食谱优化失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> optimizeRecipeWithProgress(String originalRecipe,
            java.util.function.Consumer<String> progressCallback) {
        if (originalRecipe == null || originalRecipe.trim().isEmpty()) {
            return Map.of("error", true, "message", "食谱内容不能为空");
        }

        try {
            // 阶段1：分析食谱内容
            progressCallback.accept("正在分析食谱内容...");
            log.info("[食谱优化-SSE] 阶段1：分析食谱，长度={}", originalRecipe.length());

            String prompt = String.format(RECIPE_OPTIMIZATION_PROMPT, originalRecipe);

            // 阶段2：调用AI优化
            progressCallback.accept("正在调用AI进行优化...");
            log.info("[食谱优化-SSE] 阶段2：调用LLM");
            long startTime = System.currentTimeMillis();

            String responseText = callRecipeOptimizationModel(prompt);

            if (responseText.isBlank()) {
                log.warn("[食谱优化-SSE] 模型返回空结果");
                return Map.of("error", true, "message", "AI返回空结果，请稍后重试");
            }

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("[食谱优化-SSE] LLM调用完成，耗时={}ms，结果长度={}", elapsed, responseText.length());

            // 阶段3：生成优化结果
            progressCallback.accept("正在生成优化结果...");
            log.info("[食谱优化-SSE] 阶段3：解析结果");

            List<Map<String, Object>> recipes = parseRecipeListResult(responseText);

            if (recipes.isEmpty()) {
                return Map.of("error", true, "message", "没有找到合适的优化食谱");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("error", false);
            result.put("recipes", recipes);
            log.info("[食谱优化-SSE] 成功，返回 {} 个食谱方案", recipes.size());
            return result;

        } catch (Exception e) {
            log.error("[食谱优化-SSE] 失败", e);
            return Map.of("error", true, "message", "食谱优化失败：" + e.getMessage());
        }
    }

    // ==================== 推荐理由生成 ====================

    @Override
    public String generateRecommendationReason(String dishName, Map<String, Object> userProfile, Map<String, Object> context) {
        try {
            String preferences = userProfile != null ? userProfile.toString() : "无特殊偏好";
            String scene = context != null ? context.toString() : "日常用餐";

            String prompt = String.format(RECOMMENDATION_REASON_PROMPT, dishName, preferences, scene);

            return aiModel.chat(prompt).trim();

        } catch (Exception e) {
            log.error("生成推荐理由失败", e);
            return String.format("推荐【%s】给您！这是一道美味佳肴，符合您的口味偏好。", dishName);
        }
    }

    // ==================== JSON 解析工具方法 ====================

    /**
     * 解析菜品识别结果
     */
    private Map<String, Object> parseDishRecognitionResult(String responseText) {
        try {
            // 提取JSON
            String json = extractJson(responseText);

            // 解析为Map
            Map<String, Object> result = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});

            // 检查是否为非菜品图片
            Object isDishObj = result.get("isDish");
            if (isDishObj != null) {
                boolean isDish = Boolean.TRUE.equals(isDishObj);
                if (!isDish) {
                    String reason = (String) result.getOrDefault("reason", "图片中未检测到菜品");
                    log.warn("图片不是菜品: {}", reason);
                    Map<String, Object> notDishResult = new HashMap<>();
                    notDishResult.put("error", true);
                    notDishResult.put("notDish", true);
                    notDishResult.put("message", reason);
                    return notDishResult;
                }
            }

            // 确保必要字段存在
            result.putIfAbsent("name", "未知菜品");
            result.putIfAbsent("calories", 0);
            result.putIfAbsent("protein", 0);
            result.putIfAbsent("fat", 0);
            result.putIfAbsent("carbs", 0);
            result.putIfAbsent("difficulty", "中等");
            result.putIfAbsent("preparationTime", "30分钟");
            result.putIfAbsent("ingredients", new ArrayList<>());
            result.putIfAbsent("tags", new ArrayList<>());
            result.putIfAbsent("confidence", 0.5);
            result.putIfAbsent("nutritionScore", 7);

            return result;

        } catch (IOException e) {
            log.error("解析菜品识别结果失败: {}", responseText, e);
            // 返回默认值
            Map<String, Object> defaultResult = new HashMap<>();
            defaultResult.put("name", "识别失败");
            defaultResult.put("calories", 0);
            defaultResult.put("protein", 0);
            defaultResult.put("fat", 0);
            defaultResult.put("carbs", 0);
            defaultResult.put("difficulty", "未知");
            defaultResult.put("preparationTime", "未知");
            defaultResult.put("ingredients", new ArrayList<>());
            defaultResult.put("tags", new ArrayList<>());
            defaultResult.put("confidence", 0);
            defaultResult.put("nutritionScore", 0);
            defaultResult.put("parseError", e.getMessage());
            return defaultResult;
        }
    }

    /**
     * 解析食谱列表结果（AI返回JSON数组格式）
     */
    private List<Map<String, Object>> parseRecipeListResult(String responseText) {
        try {
            String json = extractJsonArray(responseText);

            // 尝试解析为数组
            if (json.trim().startsWith("[")) {
                List<Map<String, Object>> recipes = objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
                // 确保每个食谱有必要字段
                for (Map<String, Object> recipe : recipes) {
                    recipe.putIfAbsent("name", "未知食谱");
                    recipe.putIfAbsent("difficulty", "中等");
                    recipe.putIfAbsent("calorie", 0);
                    recipe.putIfAbsent("ingredients", "");
                    recipe.putIfAbsent("steps", "");
                    recipe.putIfAbsent("protein", 0);
                    recipe.putIfAbsent("fat", 0);
                    recipe.putIfAbsent("carb", 0);
                    // 确保数值类型
                    for (String key : List.of("calorie", "protein", "fat", "carb")) {
                        Object val = recipe.get(key);
                        if (val instanceof String stringValue) {
                            try {
                                recipe.put(key, Double.valueOf(stringValue));
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
                return recipes;
            }

            // 如果返回的是单个对象（非数组），包装为数组
            if (json.trim().startsWith("{")) {
                Map<String, Object> single = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
                return List.of(single);
            }

            log.warn("无法解析为JSON数组或对象: {}", json.substring(0, Math.min(100, json.length())));
            return new ArrayList<>();

        } catch (IOException e) {
            log.error("解析食谱列表失败: {}", responseText.substring(0, Math.min(200, responseText.length())), e);
            return new ArrayList<>();
        }
    }

    /**
     * 从响应文本中提取JSON数组
     */
    private String extractJsonArray(String text) {
        if (text == null || text.isEmpty()) {
            return "[]";
        }

        String trimmed = text.trim();

        // 处理 ```json ... ``` 格式
        if (trimmed.contains("```json")) {
            int start = trimmed.indexOf("```json") + 7;
            int end = trimmed.indexOf("```", start);
            if (end > start) {
                return trimmed.substring(start, end).trim();
            }
        }

        // 处理 ``` ... ``` 格式
        if (trimmed.contains("```")) {
            int start = trimmed.indexOf("```") + 3;
            while (start < trimmed.length() && !Character.isWhitespace(trimmed.charAt(start)) && trimmed.charAt(start) != '[' && trimmed.charAt(start) != '{') {
                start++;
            }
            int end = trimmed.indexOf("```", start);
            if (end > start) {
                return trimmed.substring(start, end).trim();
            }
        }

        // 尝试找到JSON数组 [ ... ]
        int arrayStart = trimmed.indexOf('[');
        int arrayEnd = trimmed.lastIndexOf(']');
        if (arrayStart >= 0 && arrayEnd > arrayStart) {
            return trimmed.substring(arrayStart, arrayEnd + 1);
        }

        // 回退到对象查找
        return extractJson(text);
    }

    /**
     * 从响应文本中提取JSON
     * 处理可能的markdown代码块格式
     */
    private String extractJson(String text) {
        if (text == null || text.isEmpty()) {
            return "{}";
        }

        String trimmed = text.trim();

        // 处理 ```json ... ``` 格式
        if (trimmed.contains("```json")) {
            int start = trimmed.indexOf("```json") + 7;
            int end = trimmed.indexOf("```", start);
            if (end > start) {
                return trimmed.substring(start, end).trim();
            }
        }

        // 处理 ``` ... ``` 格式
        if (trimmed.contains("```")) {
            int start = trimmed.indexOf("```") + 3;
            // 跳过可能的语言标识
            while (start < trimmed.length() && !Character.isWhitespace(trimmed.charAt(start)) && trimmed.charAt(start) != '{') {
                start++;
            }
            int end = trimmed.indexOf("```", start);
            if (end > start) {
                return trimmed.substring(start, end).trim();
            }
        }

        // 尝试找到JSON对象的起始和结束
        int jsonStart = trimmed.indexOf('{');
        int jsonEnd = trimmed.lastIndexOf('}');
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return trimmed.substring(jsonStart, jsonEnd + 1);
        }

        return trimmed;
    }

    /**
     * 调用食谱优化模型。
     *
     * 先走 LangChain4j 通用模型；如果返回空串，再回退到官方 SDK。
     */
    private String callRecipeOptimizationModel(String prompt) {
        try {
            String responseText = aiModel.chat(prompt);
            if (responseText != null && !responseText.isBlank()) {
                return responseText.trim();
            }
            log.warn("LangChain4j食谱优化返回空结果，改用官方SDK重试");
        } catch (Exception e) {
            log.warn("LangChain4j食谱优化调用失败，改用官方SDK重试: {}", e.getMessage());
        }

        try {
            ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                    .model(zhipuAIConfig.getModel())
                    .messages(List.of(
                            ChatMessage.builder()
                                    .role("user")
                                    .content(prompt)
                                    .build()
                    ))
                    .temperature(0.7f)
                    .build();

            var response = zhipuClient.chat().createChatCompletion(request);
            Object contentObj = response.getData().getChoices().get(0).getMessage().getContent();
            return contentObj != null ? contentObj.toString().trim() : "";
        } catch (Exception e) {
            log.error("官方SDK食谱优化重试失败", e);
            return "";
        }
    }
}
