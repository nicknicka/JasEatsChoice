package com.xx.jaseatschoicejava.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.service.ZhipuAIService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.*;

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
    @Qualifier("agentModel")
    private ChatModel agentModel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== Prompt 常量 ====================

    /**
     * 菜品识别提示词
     */
    private static final String DISH_RECOGNITION_PROMPT = """
        你是一个专业的菜品识别专家和营养师。请仔细分析这张食物图片，返回以下JSON格式：
        {
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
        1. 卡路里和营养成分要根据菜品分量合理估算
        2. 食材列表要包含主要食材和调料
        3. 标签可以是菜系、口味、场景等
        4. 只返回JSON，不要其他解释文字
        """;

    /**
     * 食谱优化提示词
     */
    private static final String RECIPE_OPTIMIZATION_PROMPT = """
        你是一个专业的营养师和烹饪专家。请优化以下食谱，使其更健康、更美味、更易于操作。

        原食谱：
        %s

        请返回以下JSON格式：
        {
          "original": "原食谱内容（保持原样）",
          "optimized": "优化后的完整食谱，格式如下：\\n推荐食谱：[菜名]\\n难度：[简单/中等/困难]\\n卡路里：[数字]大卡\\n食材：[食材列表]\\n步骤：[详细步骤]",
          "improvements": ["改进点1：具体说明", "改进点2：具体说明", "改进点3：具体说明"]
        }

        优化原则：
        1. 减少油盐用量，保持健康
        2. 增加蔬菜搭配，营养均衡
        3. 简化步骤，易于操作
        4. 保留菜品特色和美味

        只返回JSON，不要其他解释文字。
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
            UserMessage userMessage = UserMessage.builder()
                    .content(List.of(
                            TextContent.from(DISH_RECOGNITION_PROMPT),
                            ImageContent.fromBase64(imageBase64, "image/jpeg")
                    ))
                    .build();

            // 调用视觉模型
            Response<AiMessage> response = visionModel.generate(userMessage);
            String responseText = response.content().text();

            log.info("视觉模型返回结果: {}", responseText.length() > 200 ? responseText.substring(0, 200) + "..." : responseText);

            // 解析JSON响应
            Map<String, Object> result = parseDishRecognitionResult(responseText);

            // 添加成功标记
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

            // 调用对话模型
            Response<AiMessage> response = agentModel.generate(prompt);
            String responseText = response.content().text();

            log.info("AI返回优化结果: {}", responseText.length() > 200 ? responseText.substring(0, 200) + "..." : responseText);

            // 解析JSON响应
            Map<String, Object> result = parseRecipeOptimizationResult(responseText, originalRecipe);

            // 添加成功标记
            result.put("error", false);

            log.info("食谱优化成功");
            return result;

        } catch (Exception e) {
            log.error("食谱优化失败", e);
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

            Response<AiMessage> response = agentModel.generate(prompt);
            return response.content().text().trim();

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

        } catch (Exception e) {
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
     * 解析食谱优化结果
     */
    private Map<String, Object> parseRecipeOptimizationResult(String responseText, String originalRecipe) {
        try {
            // 提取JSON
            String json = extractJson(responseText);

            // 解析为Map
            Map<String, Object> result = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});

            // 确保必要字段存在
            result.putIfAbsent("original", originalRecipe);
            result.putIfAbsent("optimized", originalRecipe + "\n\n（AI优化建议：减少油盐，增加蔬菜）");
            result.putIfAbsent("improvements", Arrays.asList("营养均衡", "口味优化", "步骤简化"));

            return result;

        } catch (Exception e) {
            log.error("解析食谱优化结果失败: {}", responseText, e);
            // 返回默认值
            Map<String, Object> defaultResult = new HashMap<>();
            defaultResult.put("original", originalRecipe);
            defaultResult.put("optimized", originalRecipe + "\n\n（AI优化建议：减少油盐，增加蔬菜）");
            defaultResult.put("improvements", Arrays.asList("营养均衡", "口味优化", "步骤简化"));
            defaultResult.put("parseError", e.getMessage());
            return defaultResult;
        }
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
}
