package com.xx.jaseatschoicejava.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import com.xx.jaseatschoicejava.service.ZhipuAIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 智谱AI服务实现类
 */
@Service
public class ZhipuAIServiceImpl implements ZhipuAIService {

    private static final Logger log = LoggerFactory.getLogger(ZhipuAIServiceImpl.class);

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
        long startTime = System.currentTimeMillis();

        log.info("-> 智谱AI聊天服务调用开始");
        log.info("请求模型: {}", zhipuAIConfig.getModel());

        try {
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

            // 添加历史对话（如果有）
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                // 只保留最近的5轮对话，避免token超限
                int historySize = Math.min(conversationHistory.size(), 10);
                log.info("历史对话数量: {}, 保留: {}", conversationHistory.size(), historySize);
                messages.addAll(conversationHistory.subList(
                        conversationHistory.size() - historySize,
                        conversationHistory.size()
                ));
            }

            // 添加当前用户消息
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", message);
            messages.add(userMessage);

            requestBody.put("messages", messages);
            log.info("总消息数: {}", messages.size());

            // 可选参数（移除深度思考模式，glm-4-flash不支持）
            requestBody.put("temperature", 0.7);
            requestBody.put("top_p", 0.9);

            // 发送请求（暂时不使用流式，保持原有逻辑）
            log.info("发送HTTP请求到智谱AI...");
            String response = sendRequest(requestBody);

            long apiTime = System.currentTimeMillis() - startTime;
            log.info("智谱AI API响应成功，耗时: {} ms", apiTime);

            // 解析响应
            JsonNode jsonResponse = objectMapper.readTree(response);
            JsonNode choices = jsonResponse.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode messageNode = choices.get(0).get("message");
                if (messageNode != null) {
                    JsonNode contentNode = messageNode.get("content");
                    if (contentNode != null) {
                        String content = contentNode.asText();
                        log.info("AI回复内容预览: {}...",
                            content.length() > 50 ? content.substring(0, 50) + "..." : content);

                        long totalTime = System.currentTimeMillis() - startTime;
                        log.info("-> 智谱AI聊天服务调用成功，总耗时: {} ms", totalTime);
                        return content;
                    }
                }
            }

            // 如果解析失败，返回默认回复
            log.error("解析AI响应失败: {}", response);
            log.warn("返回默认回复");
            return "抱歉，我现在无法回复，请稍后再试。";

        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            log.error("智谱AI聊天服务调用失败，总耗时: {} ms", totalTime, e);
            log.error("异常类型: {}", e.getClass().getSimpleName());
            log.error("异常消息: {}", e.getMessage());
            return "抱歉，服务暂时不可用，请稍后再试。错误：" + e.getMessage();
        }
    }

    @Override
    public List<Map<String, Object>> recommendRecipe(String foodName) {
        long startTime = System.currentTimeMillis();

        log.info("=== 食谱推荐AI服务调用开始 ===");
        log.info("输入参数 - foodName: \"{}\"", foodName);
        log.info("输入参数 - foodName长度: {} 字符", foodName != null ? foodName.length() : 0);
        log.info("请求模型: {}", zhipuAIConfig.getModel());

        try {
            // ========== 1. 构建请求体 ==========
            log.info("步骤1/5: 构建AI请求体");
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", zhipuAIConfig.getModel());
            log.info("✓ 模型设置: {}", zhipuAIConfig.getModel());

            // ========== 2. 构建消息列表 ==========
            log.info("步骤2/5: 构建系统提示词和用户消息");
            List<Map<String, String>> messages = new ArrayList<>();

            // 系统提示词
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", RECIPE_PROMPT);
            messages.add(systemMessage);
            log.info("✓ 系统提示词已添加 (长度: {} 字符)", RECIPE_PROMPT.length());

            // 用户消息
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            String userContent = "请推荐与\"" + foodName + "\"相关的食谱，返回2-3个食谱";
            userMessage.put("content", userContent);
            messages.add(userMessage);
            log.info("✓ 用户消息已添加: \"{}\"", userContent);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.8);
            log.info("✓ 温度参数: 0.8");
            log.info("✓ 总消息数: {}", messages.size());

            // ========== 3. 发送HTTP请求 ==========
            log.info("步骤3/5: 发送HTTP请求到智谱AI");
            log.info("请求URL: {}", zhipuAIConfig.getBaseUrl());
            String response = sendRequest(requestBody);

            long apiTime = System.currentTimeMillis() - startTime;
            log.info("✓ 智谱AI API响应成功，耗时: {} ms", apiTime);
            log.info("响应长度: {} 字符", response.length());

            // ========== 4. 解析AI响应 ==========
            log.info("步骤4/5: 解析AI响应");
            JsonNode jsonResponse = objectMapper.readTree(response);
            log.info("✓ JSON解析成功");

            JsonNode choices = jsonResponse.get("choices");
            if (choices == null || !choices.isArray() || choices.size() == 0) {
                log.error("❌ AI响应格式错误: choices字段为空或不是数组");
                log.error("完整响应: {}", response);
                throw new RuntimeException("AI响应格式错误: choices字段为空或不是数组");
            }

            log.info("✓ choices数组长度: {}", choices.size());

            String content = choices.get(0).get("message").get("content").asText();
            log.info("✓ 提取AI生成内容成功");
            log.info("AI返回内容预览: {}...",
                content.length() > 100 ? content.substring(0, 100) + "..." : content);
            log.info("AI返回内容长度: {} 字符", content.length());

            // ========== 5. 清洗并解析食谱JSON数组 ==========
            log.info("步骤5/5: 清洗并解析食谱JSON数组");

            // 清洗AI响应：去除Markdown标记
            String cleanedContent = cleanAIResponse(content);
            if (!cleanedContent.equals(content)) {
                log.info("✓ 内容已清洗（去除Markdown标记）");
                log.info("清洗后长度: {} 字符", cleanedContent.length());
            } else {
                log.info("✓ 内容无需清洗");
            }

            JsonNode recipesArray;
            try {
                recipesArray = objectMapper.readTree(cleanedContent);
                log.info("✓ JSON数组解析成功");
            } catch (Exception e) {
                log.error("❌ AI返回的内容不是有效的JSON格式");
                log.error("原始内容: {}", content);
                log.error("解析错误: {}", e.getMessage());
                throw new RuntimeException("AI返回的内容不是有效的JSON格式: " + e.getMessage(), e);
            }

            if (!recipesArray.isArray()) {
                log.error("❌ AI返回的内容不是JSON数组类型");
                log.error("实际类型: {}", recipesArray.getNodeType());
                log.error("完整内容: {}", content);
                throw new RuntimeException("AI返回的内容不是JSON数组类型，实际类型: " + recipesArray.getNodeType());
            }

            log.info("✓ 食谱数组类型验证通过");
            log.info("食谱数量: {}", recipesArray.size());

            // ========== 解析每个食谱 ==========
            List<Map<String, Object>> recipes = new ArrayList<>();
            for (int i = 0; i < recipesArray.size(); i++) {
                JsonNode recipeNode = recipesArray.get(i);
                log.info("解析食谱 {}/{}:", i + 1, recipesArray.size());

                // 验证必填字段
                String[] requiredFields = {"name", "calorie", "difficulty", "ingredients", "steps"};
                for (String field : requiredFields) {
                    if (!recipeNode.has(field)) {
                        log.error("❌ 食谱{}缺少必填字段: {}", i + 1, field);
                        log.error("食谱数据: {}", recipeNode.toString());
                        throw new RuntimeException("食谱" + (i + 1) + "缺少必填字段: " + field);
                    }
                    log.info("  ✓ {}: {}", field, recipeNode.get(field).asText());
                }

                Map<String, Object> recipe = new HashMap<>();
                recipe.put("name", recipeNode.get("name").asText());
                recipe.put("calorie", recipeNode.get("calorie").asDouble());
                recipe.put("difficulty", recipeNode.get("difficulty").asText());
                recipe.put("ingredients", recipeNode.get("ingredients").asText());
                recipe.put("steps", recipeNode.get("steps").asText());
                recipes.add(recipe);

                log.info("  ✓ 食谱\"{}\"解析成功", recipe.get("name"));
            }

            long totalTime = System.currentTimeMillis() - startTime;
            log.info("=== ✅ 食谱推荐AI服务调用成功 ===");
            log.info("总耗时: {} ms", totalTime);
            log.info("返回食谱数量: {}", recipes.size());

            return recipes;

        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            log.error("=== ❌ 食谱推荐AI服务调用失败 ===");
            log.error("总耗时: {} ms", totalTime);
            log.error("错误类型: {}", e.getClass().getSimpleName());
            log.error("错误信息: {}", e.getMessage());
            log.error("输入参数 - foodName: \"{}\"", foodName);

            // 构建详细的错误信息
            String errorDetails = String.format(
                "食谱推荐失败\n" +
                "错误类型: %s\n" +
                "错误信息: %s\n" +
                "可能原因:\n" +
                "1. 后端服务未启动（检查8080端口）\n" +
                "2. API Key无效（检查智谱AI配置）\n" +
                "3. 网络连接问题\n" +
                "4. AI模型名称错误（当前使用: %s）\n" +
                "5. AI返回的数据格式不符合要求",
                e.getClass().getSimpleName(),
                e.getMessage(),
                zhipuAIConfig.getModel()
            );

            log.error("详细错误信息:\n{}", errorDetails, e);

            // 直接抛出异常，不使用Mock数据
            throw new RuntimeException("食谱推荐失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> analyzeNutrition(String foodName) {
        long startTime = System.currentTimeMillis();

        log.info("=== 营养分析AI服务调用开始 ===");
        log.info("输入参数 - foodName: \"{}\"", foodName);

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", zhipuAIConfig.getModel());

            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是营养分析专家。请分析食物的营养成分，包括卡路里、蛋白质、脂肪、碳水化合物。返回JSON格式：{\"calorie\": 350.5, \"protein\": 20.3, \"fat\": 15.7, \"carbohydrate\": 40.2}");
            messages.add(systemMessage);

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", "请分析\"" + foodName + "\"的营养成分，100克的热量和主要营养素含量");
            messages.add(userMessage);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.3);

            String response = sendRequest(requestBody);

            JsonNode jsonResponse = objectMapper.readTree(response);
            JsonNode choices = jsonResponse.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).get("message").get("content").asText();

                try {
                    // 清洗AI响应：去除Markdown标记
                    String cleanedContent = cleanAIResponse(content);
                    if (!cleanedContent.equals(content)) {
                        log.info("✓ 营养数据内容已清洗（去除Markdown标记）");
                    }

                    JsonNode nutritionData = objectMapper.readTree(cleanedContent);
                    Map<String, Object> result = new HashMap<>();
                    result.put("foodName", foodName);
                    result.put("calorie", nutritionData.get("calorie").asDouble());
                    result.put("protein", nutritionData.get("protein").asDouble());
                    result.put("fat", nutritionData.get("fat").asDouble());
                    result.put("carbohydrate", nutritionData.get("carbohydrate").asDouble());

                    long totalTime = System.currentTimeMillis() - startTime;
                    log.info("=== ✅ 营养分析AI服务调用成功，耗时: {} ms ===", totalTime);
                    return result;
                } catch (Exception e) {
                    log.error("❌ 营养数据解析失败，原始内容: {}", content);
                    throw new RuntimeException("营养数据解析失败: " + e.getMessage(), e);
                }
            }

            throw new RuntimeException("AI响应格式错误：choices为空");

        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            log.error("=== ❌ 营养分析AI服务调用失败，耗时: {} ms ===", totalTime, e);
            throw new RuntimeException("营养分析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 清洗AI响应内容（方案C：正则提取 + 清洗）
     * 去除markdown标记、额外文字，提取纯JSON
     */
    /**
     * 清洗AI响应内容
     * 去除markdown标记、额外文字，提取纯JSON
     */
    private String cleanAIResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return response;
        }

        String cleaned = response.trim();

        // 去除markdown代码块标记
        cleaned = cleaned.replaceAll("```json\\n?", "")
                .replaceAll("```", "")
                .trim();

        // 智能提取JSON部分（保留数组结构）
        String trimmed = cleaned.trim();
        char firstChar = trimmed.charAt(0);

        // 如果以 [ 开头，提取整个数组
        if (firstChar == '[') {
            int lastBracket = trimmed.lastIndexOf("]");
            if (lastBracket > 0) {
                return trimmed.substring(0, lastBracket + 1);
            }
        }
        // 如果以 { 开头，提取整个对象
        else if (firstChar == '{') {
            int lastBrace = trimmed.lastIndexOf("}");
            if (lastBrace > 0) {
                return trimmed.substring(0, lastBrace + 1);
            }
        }

        return cleaned;
    }

    /**
     * 校验菜品数据的完整性和合理性（方案A：严格校验）
     */
    private void validateDishData(JsonNode dishData) throws Exception {
        // 校验必填字段
        String[] requiredFields = {"name", "calories", "protein", "fat", "carbs",
                                "difficulty", "preparationTime", "ingredients", "tags", "confidence"};

        for (String field : requiredFields) {
            if (!dishData.has(field) || dishData.get(field).isNull()) {
                throw new Exception("缺少必填字段：" + field);
            }
        }

        // 校验数值范围
        int calories = dishData.get("calories").asInt();
        if (calories < 0 || calories > 2000) {
            throw new Exception("卡路里数值异常： " + calories + "（范围：0-2000）");
        }

        int protein = dishData.get("protein").asInt();
        if (protein < 0 || protein > 100) {
            throw new Exception("蛋白质数值异常： " + protein + "（范围：0-100克）");
        }

        int fat = dishData.get("fat").asInt();
        if (fat < 0 || fat > 100) {
            throw new Exception("脂肪数值异常： " + fat + "（范围：0-100克）");
        }

        int carbs = dishData.get("carbs").asInt();
        if (carbs < 0 || carbs > 200) {
            throw new Exception("碳水数值异常： " + carbs + "（范围：0-200克）");
        }

        double confidence = dishData.get("confidence").asDouble();
        if (confidence < 0 || confidence > 1) {
            throw new Exception("置信度异常： " + confidence + "（范围：0-1）");
        }

        // 校验difficulty枚举值
        String difficulty = dishData.get("difficulty").asText();
        if (!difficulty.matches("简单|中等|困难")) {
            throw new Exception("难度值异常： " + difficulty + "（必须是：简单/中等/困难）");
        }

        // 校验数组长度
        if (!dishData.get("ingredients").isArray() ||
            dishData.get("ingredients").size() < 3 ||
            dishData.get("ingredients").size() > 8) {
            throw new Exception("食材数组长度异常（要求：3-8个）");
        }

        if (!dishData.get("tags").isArray() ||
            dishData.get("tags").size() < 2 ||
            dishData.get("tags").size() > 5) {
            throw new Exception("标签数组长度异常（要求：2-5个）");
        }
    }

    /**
     * 解析菜品数据JsonNode为Map
     */
    private Map<String, Object> parseDishData(JsonNode dishData) {
        Map<String, Object> result = new HashMap<>();
        result.put("name", dishData.get("name").asText());
        result.put("calories", dishData.get("calories").asInt());
        result.put("protein", dishData.get("protein").asInt());
        result.put("fat", dishData.get("fat").asInt());
        result.put("carbs", dishData.get("carbs").asInt());
        result.put("difficulty", dishData.get("difficulty").asText());
        result.put("preparationTime", dishData.get("preparationTime").asText());

        // 解析数组字段
        List<String> ingredients = new ArrayList<>();
        JsonNode ingredientsNode = dishData.get("ingredients");
        if (ingredientsNode.isArray()) {
            for (JsonNode item : ingredientsNode) {
                ingredients.add(item.asText());
            }
        }
        result.put("ingredients", ingredients);

        List<String> tags = new ArrayList<>();
        JsonNode tagsNode = dishData.get("tags");
        if (tagsNode.isArray()) {
            for (JsonNode item : tagsNode) {
                tags.add(item.asText());
            }
        }
        result.put("tags", tags);

        result.put("confidence", dishData.get("confidence").asDouble());
        result.put("nutritionScore", calculateNutritionScore(
            dishData.get("calories").asInt(),
            dishData.get("protein").asInt(),
            dishData.get("fat").asInt(),
            dishData.get("carbs").asInt()
        ));

        return result;
    }

    @Override
    public Map<String, Object> recognizeDish(String imageUrl) {
        try {
            log.info("开始调用视觉模型识别菜品，图片URL：{}", imageUrl);

            // 构建请求体（使用视觉模型）
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", zhipuAIConfig.getVisionModel());

            // 构建多模态消息（文本 + 图片）
            List<Map<String, Object>> messages = new ArrayList<>();

            // 系统提示词
            Map<String, Object> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", DISH_RECOGNITION_PROMPT);
            messages.add(systemMessage);

            // 用户消息（包含图片和文本）
            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");

            // 多模态内容：图片 + 文本
            List<Map<String, Object>> content = new ArrayList<>();

            // 添加图片
            Map<String, Object> imageContent = new HashMap<>();
            imageContent.put("type", "image_url");
            imageContent.put("image_url", Map.of("url", imageUrl));
            content.add(imageContent);

            // 添加文本指令
            Map<String, Object> textContent = new HashMap<>();
            textContent.put("type", "text");
            textContent.put("text", "请识别这张图片中的菜品，并按照要求的JSON格式返回详细信息。");
            content.add(textContent);

            userMessage.put("content", content);
            messages.add(userMessage);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.3); // 降低温度以提高识别准确性

            // 发送请求
            String response = sendRequest(requestBody);

            // 解析响应
            JsonNode jsonResponse = objectMapper.readTree(response);
            JsonNode choices = jsonResponse.get("choices");

            if (choices != null && choices.isArray() && choices.size() > 0) {
                String aiResponse = choices.get(0).get("message").get("content").asText();
                log.info("AI识别响应原始内容：{}", aiResponse);

                // ============ 第一层降级：尝试直接解析（方案B：严格提示词）============
                try {
                    JsonNode dishData = objectMapper.readTree(aiResponse);
                    validateDishData(dishData);  // 严格校验

                    Map<String, Object> result = parseDishData(dishData);
                    log.info("✅ 菜品识别成功（方案B）：{}", result.get("name"));
                    return result;

                } catch (Exception e) {
                    log.warn("⚠️ 方案B失败：{}", e.getMessage());
                }

                // ============ 第二层降级：清洗后解析+校验（方案A+C：客户端校验）============
                try {
                    String cleanedResponse = cleanAIResponse(aiResponse);
                    log.info("清洗后的响应：{}", cleanedResponse);

                    JsonNode dishData = objectMapper.readTree(cleanedResponse);
                    validateDishData(dishData);  // 严格校验

                    Map<String, Object> result = parseDishData(dishData);
                    log.info("✅ 菜品识别成功（方案A）：{}", result.get("name"));
                    return result;

                } catch (Exception e) {
                    log.warn("⚠️ 方案A失败：{}", e.getMessage());
                }

                // ============ 第三层：所有方案都失败，抛出详细错误============
                String errorMsg = "菜品识别失败：AI返回的数据格式不符合要求。\n" +
                        "原始响应：" + aiResponse + "\n" +
                        "建议：检查AI模型是否支持视觉识别（glm-4.6v-flash）";
                log.error("❌ {}", errorMsg);
                throw new Exception(errorMsg);
            }

        } catch (Exception e) {
            // ============ 最外层异常处理：直接抛出，不使用模拟数据============
            String errorDetails = "菜品识别请求失败\n" +
                    "错误类型：" + e.getClass().getSimpleName() + "\n" +
                    "错误信息：" + e.getMessage() + "\n" +
                    "可能原因：\n" +
                    "1. 后端服务未启动（检查8080端口）\n" +
                    "2. API Key无效（检查智谱AI配置）\n" +
                    "3. 网络连接问题\n" +
                    "4. AI模型名称错误（当前使用：" + zhipuAIConfig.getVisionModel() + "）";

            log.error("❌ 菜品识别失败：{}", errorDetails, e);

            // ============ 返回错误信息而不是抛出异常 ============
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", true);
            errorResult.put("message", "菜品识别失败：" + e.getMessage());
            errorResult.put("details", errorDetails);

            return errorResult;
        }
        return null;
    }


    /**
     * 计算营养评分（简单算法）
     */
    private int calculateNutritionScore(int calories, int protein, int fat, int carbs) {
        double score = 100;

        // 根据热量调整评分
        if (calories > 600) score -= 10;
        else if (calories > 500) score -= 5;
        else if (calories < 300) score += 5;

        // 蛋白质越高越好
        score += protein * 0.5;

        // 脂肪越低越好
        score -= fat * 0.3;

        return Math.max(60, Math.min(95, (int) score));
    }

    @Override
    public Map<String, Object> optimizeRecipe(String originalRecipe) {
        long startTime = System.currentTimeMillis();

        log.info("=== 食谱优化AI服务调用开始 ===");
        log.info("原始食谱长度: {} 字符", originalRecipe != null ? originalRecipe.length() : 0);

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", zhipuAIConfig.getModel());

            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是烹饪优化专家。请优化用户提供的食谱，使其更健康、更美味。返回JSON格式：{\"name\": \"优化后菜名\", \"original\": \"原始做法\", \"optimized\": \"优化后做法\", \"calorie\": 200.5, \"improvements\": [\"改进点1\", \"改进点2\"]}");
            messages.add(systemMessage);

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", "请优化这个食谱：" + originalRecipe);
            messages.add(userMessage);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);

            String response = sendRequest(requestBody);

            JsonNode jsonResponse = objectMapper.readTree(response);
            JsonNode choices = jsonResponse.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).get("message").get("content").asText();

                try {
                    // 清洗AI响应：去除Markdown标记
                    String cleanedContent = cleanAIResponse(content);
                    if (!cleanedContent.equals(content)) {
                        log.info("✓ 食谱优化内容已清洗（去除Markdown标记）");
                    }

                    JsonNode recipeData = objectMapper.readTree(cleanedContent);
                    Map<String, Object> result = new HashMap<>();
                    result.put("name", recipeData.get("name").asText());
                    result.put("original", recipeData.get("original").asText());
                    result.put("optimized", recipeData.get("optimized").asText());
                    result.put("calorie", recipeData.get("calorie").asDouble());

                    List<String> improvements = new ArrayList<>();
                    JsonNode improvementsNode = recipeData.get("improvements");
                    if (improvementsNode.isArray()) {
                        for (JsonNode item : improvementsNode) {
                            improvements.add(item.asText());
                        }
                    }
                    result.put("improvements", improvements);

                    long totalTime = System.currentTimeMillis() - startTime;
                    log.info("=== ✅ 食谱优化AI服务调用成功，耗时: {} ms ===", totalTime);
                    return result;
                } catch (Exception e) {
                    log.error("❌ 食谱优化数据解析失败，原始内容: {}", content);
                    throw new RuntimeException("食谱优化数据解析失败: " + e.getMessage(), e);
                }
            }

            throw new RuntimeException("AI响应格式错误：choices为空");

        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            log.error("=== ❌ 食谱优化AI服务调用失败，耗时: {} ms ===", totalTime, e);
            throw new RuntimeException("食谱优化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送HTTP请求到智谱AI（使用官方推荐方式：直接Bearer Token）
     */
    private String sendRequest(Map<String, Object> requestBody) throws Exception {
        // 使用官方推荐的方式：直接使用 API Key 作为 Bearer Token
        String apiKey = zhipuAIConfig.getApiKey();

        // 构建请求体
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // 使用Java原生HttpClient（Java 11+）
        java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(30))
                .build();

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(zhipuAIConfig.getBaseUrl()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)  // 官方推荐：直接Bearer Token
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        java.net.http.HttpResponse<String> response = client.send(
                request,
                java.net.http.HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException("请求失败，状态码: " + response.statusCode() + ", 响应: " + response.body());
        }

        return response.body();
    }

    @Override
    public String generateRecommendationReason(String dishName, Map<String, Object> userProfile, Map<String, Object> context) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", zhipuAIConfig.getModel());

            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", RECOMMENDATION_REASON_PROMPT);
            messages.add(systemMessage);

            // 构建用户提示词
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("请为菜品\"").append(dishName).append("\"生成推荐理由。\n");

            if (userProfile != null && !userProfile.isEmpty()) {
                promptBuilder.append("\n用户画像：\n");
                if (userProfile.containsKey("dietGoal")) {
                    promptBuilder.append("- 饮食目标：").append(userProfile.get("dietGoal")).append("\n");
                }
                if (userProfile.containsKey("flavorPreference")) {
                    promptBuilder.append("- 口味偏好：").append(userProfile.get("flavorPreference")).append("\n");
                }
                if (userProfile.containsKey("preferenceTags")) {
                    promptBuilder.append("- 偏好标签：").append(userProfile.get("preferenceTags")).append("\n");
                }
            }

            if (context != null && !context.isEmpty()) {
                promptBuilder.append("\n当前上下文：\n");
                context.forEach((key, value) -> promptBuilder.append("- ").append(key).append("：").append(value).append("\n"));
            }

            promptBuilder.append("\n请生成15-30字的推荐理由：");

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", promptBuilder.toString());
            messages.add(userMessage);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.8); // 提高创造性

            String response = sendRequest(requestBody);

            JsonNode jsonResponse = objectMapper.readTree(response);
            JsonNode choices = jsonResponse.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).get("message").get("content").asText();
                log.info("AI生成的推荐理由: {}", content);
                return content;
            }

            // 如果AI调用失败，返回默认推荐理由
            return generateDefaultReason(dishName, userProfile, context);

        } catch (Exception e) {
            log.error("AI生成推荐理由失败，使用默认理由", e);
            return generateDefaultReason(dishName, userProfile, context);
        }
    }

    /**
     * 生成默认推荐理由（AI失败时的降级方案）
     */
    private String generateDefaultReason(String dishName, Map<String, Object> userProfile, Map<String, Object> context) {
        List<String> reasons = new ArrayList<>();

        // 基于饮食目标
        if (userProfile != null && userProfile.containsKey("dietGoal")) {
            String goal = (String) userProfile.get("dietGoal");
            if ("low_calorie".equals(goal)) {
                reasons.add("低卡健康");
            } else if ("high_protein".equals(goal)) {
                reasons.add("高蛋白营养丰富");
            } else if ("balanced".equals(goal)) {
                reasons.add("营养均衡");
            }
        }

        // 基于上下文
        if (context != null) {
            if (context.containsKey("weather")) {
                String weather = (String) context.get("weather");
                if ("cold".equals(weather)) {
                    reasons.add("暖胃驱寒");
                } else if ("hot".equals(weather)) {
                    reasons.add("清爽解腻");
                }
            }
            if (context.containsKey("timePeriod")) {
                String timePeriod = (String) context.get("timePeriod");
                reasons.add("适合" + timePeriod);
            }
        }

        // 如果没有任何理由，返回默认值
        if (reasons.isEmpty()) {
            return "符合您的口味偏好，营养丰富又健康";
        }

        return String.join("，", reasons);
    }

    @Override
    public Map<String, Object> recognizeDishWithBase64(String imageBase64) {
        try {
            log.info("开始调用视觉模型识别菜品（Base64编码），图片大小：{} 字符",
                    imageBase64 != null ? imageBase64.length() : 0);

            // 构建请求体（使用视觉模型）
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", zhipuAIConfig.getVisionModel());

            // 构建多模态消息（文本 + 图片）
            List<Map<String, Object>> messages = new ArrayList<>();

            // 系统提示词
            Map<String, Object> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", DISH_RECOGNITION_PROMPT);
            messages.add(systemMessage);

            // 用户消息（包含图片和文本）
            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");

            // 多模态内容：图片（Base64） + 文本
            List<Map<String, Object>> content = new ArrayList<>();

            // 添加图片（Base64格式）
            Map<String, Object> imageContent = new HashMap<>();
            imageContent.put("type", "image_url");
            // Base64格式需要添加data URI前缀
            String base64DataUrl = "data:image/jpeg;base64," + imageBase64;
            imageContent.put("image_url", Map.of("url", base64DataUrl));
            content.add(imageContent);

            // 添加文本指令
            Map<String, Object> textContent = new HashMap<>();
            textContent.put("type", "text");
            textContent.put("text", "请识别这张图片中的菜品，并按照要求的JSON格式返回详细信息。");
            content.add(textContent);

            userMessage.put("content", content);
            messages.add(userMessage);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.3); // 降低温度以提高识别准确性

            // 发送请求
            String response = sendRequest(requestBody);

            // 解析响应
            JsonNode jsonResponse = objectMapper.readTree(response);
            JsonNode choices = jsonResponse.get("choices");

            if (choices != null && choices.isArray() && choices.size() > 0) {
                String aiResponse = choices.get(0).get("message").get("content").asText();
                log.info("AI识别响应原始内容：{}", aiResponse);

                // ============ 第一层降级：尝试直接解析（方案B：严格提示词）============
                try {
                    JsonNode dishData = objectMapper.readTree(aiResponse);
                    validateDishData(dishData);  // 严格校验

                    Map<String, Object> result = parseDishData(dishData);
                    log.info("✅ 菜品识别成功（Base64方案）：{}", result.get("name"));
                    return result;

                } catch (Exception e) {
                    log.warn("⚠️ Base64方案B失败：{}", e.getMessage());
                }

                // ============ 第二层降级：清洗后解析+校验（方案A+C：客户端校验）============
                try {
                    String cleanedResponse = cleanAIResponse(aiResponse);
                    log.info("清洗后的响应：{}", cleanedResponse);

                    JsonNode dishData = objectMapper.readTree(cleanedResponse);
                    validateDishData(dishData);  // 严格校验

                    Map<String, Object> result = parseDishData(dishData);
                    log.info("✅ 菜品识别成功（Base64方案A）：{}", result.get("name"));
                    return result;

                } catch (Exception e) {
                    log.warn("⚠️ Base64方案A失败：{}", e.getMessage());
                }

                // ============ 第三层：所有方案都失败，抛出详细错误============
                String errorMsg = "菜品识别失败：AI返回的数据格式不符合要求。\n" +
                        "原始响应：" + aiResponse + "\n" +
                        "建议：检查AI模型是否支持视觉识别（glm-4.6v-flash）";
                log.error("❌ {}", errorMsg);
                throw new Exception(errorMsg);
            }

            // 如果AI响应异常，返回错误信息
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", true);
            errorResult.put("message", "菜品识别失败：AI响应异常");

            return errorResult;

        } catch (Exception e) {
            // ============ 最外层异常处理：返回错误信息而不是抛出异常 ============
            String errorDetails = "菜品识别失败\n" +
                    "错误类型：" + e.getClass().getSimpleName() + "\n" +
                    "错误信息：" + e.getMessage() + "\n" +
                    "可能原因：\n" +
                    "1. Base64编码格式错误\n" +
                    "2. 网络连接问题\n" +
                    "3. AI模型名称错误（当前使用：" + zhipuAIConfig.getVisionModel() + "）";

            log.error("❌ 菜品识别失败（Base64）：{}", errorDetails, e);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", true);
            errorResult.put("message", "菜品识别失败：" + e.getMessage());
            errorResult.put("details", errorDetails);

            return errorResult;
        }
    }
}
