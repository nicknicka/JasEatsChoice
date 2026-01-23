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
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", zhipuAIConfig.getModel());

            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", RECIPE_PROMPT);
            messages.add(systemMessage);

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", "请推荐与\"" + foodName + "\"相关的食谱，返回2-3个食谱");
            messages.add(userMessage);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.8);

            String response = sendRequest(requestBody);

            // 解析响应
            JsonNode jsonResponse = objectMapper.readTree(response);
            JsonNode choices = jsonResponse.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).get("message").get("content").asText();

                // 尝试解析JSON数组
                try {
                    JsonNode recipesArray = objectMapper.readTree(content);
                    if (recipesArray.isArray()) {
                        List<Map<String, Object>> recipes = new ArrayList<>();
                        for (JsonNode recipeNode : recipesArray) {
                            Map<String, Object> recipe = new HashMap<>();
                            recipe.put("name", recipeNode.get("name").asText());
                            recipe.put("calorie", recipeNode.get("calorie").asDouble());
                            recipe.put("difficulty", recipeNode.get("difficulty").asText());
                            recipe.put("ingredients", recipeNode.get("ingredients").asText());
                            recipe.put("steps", recipeNode.get("steps").asText());
                            recipes.add(recipe);
                        }
                        return recipes;
                    }
                } catch (Exception e) {
                    // 如果AI返回的不是标准JSON，返回模拟数据
                    log.warn("AI返回的不是标准JSON格式，使用模拟数据: {}", content);
                    return getMockRecipes();
                }
            }

            return getMockRecipes();

        } catch (Exception e) {
            log.error("食谱推荐失败", e);
            return getMockRecipes();
        }
    }

    @Override
    public Map<String, Object> analyzeNutrition(String foodName) {
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
                    JsonNode nutritionData = objectMapper.readTree(content);
                    Map<String, Object> result = new HashMap<>();
                    result.put("foodName", foodName);
                    result.put("calorie", nutritionData.get("calorie").asDouble());
                    result.put("protein", nutritionData.get("protein").asDouble());
                    result.put("fat", nutritionData.get("fat").asDouble());
                    result.put("carbohydrate", nutritionData.get("carbohydrate").asDouble());
                    return result;
                } catch (Exception e) {
                    log.warn("营养数据解析失败，使用模拟数据");
                }
            }

            return getMockNutrition(foodName);

        } catch (Exception e) {
            log.error("营养分析失败", e);
            return getMockNutrition(foodName);
        }
    }

    @Override
    public Map<String, Object> recognizeDish(String imageUrl) {
        // 注意：GLM-4.7-Flash不支持图片识别，需要使用GLM-4V
        // 这里暂时返回模拟数据
        log.warn("GLM-4.7-Flash不支持图片识别，返回模拟数据");

        // 模拟多种菜品识别结果
        List<Map<String, Object>> mockDishes = Arrays.asList(
            createMockDish("宫保鸡丁", 450, 28, 18, 15, "中等", "25分钟",
                Arrays.asList("鸡肉", "花生米", "辣椒", "黄瓜", "胡萝卜"),
                Arrays.asList("川菜", "经典", "蛋白质丰富"), 0.95),
            createMockDish("红烧肉", 580, 22, 35, 20, "中等", "45分钟",
                Arrays.asList("五花肉", "冰糖", "生抽", "老抽", "姜", "葱"),
                Arrays.asList("家常菜", "下饭菜", "经典"), 0.92),
            createMockDish("清蒸鲈鱼", 280, 30, 8, 12, "简单", "20分钟",
                Arrays.asList("鲈鱼", "姜", "葱", "料酒", "蒸鱼豉油"),
                Arrays.asList("粤菜", "清淡", "高蛋白", "低脂"), 0.98),
            createMockDish("麻婆豆腐", 320, 18, 22, 18, "简单", "15分钟",
                Arrays.asList("豆腐", "牛肉末", "豆瓣酱", "花椒", "蒜苗"),
                Arrays.asList("川菜", "素食", "下饭"), 0.90),
            createMockDish("糖醋排骨", 520, 25, 28, 25, "中等", "40分钟",
                Arrays.asList("排骨", "冰糖", "醋", "生抽", "姜"),
                Arrays.asList("家常菜", "酸甜口", "经典"), 0.88)
        );

        // 随机返回一个菜品（实际AI会根据图片内容识别）
        int randomIndex = (int) (Math.random() * mockDishes.size());
        return mockDishes.get(randomIndex);
    }

    /**
     * 创建模拟菜品数据
     */
    private Map<String, Object> createMockDish(String name, int calories, int protein, int fat,
                                                int carbs, String difficulty, String prepTime,
                                                List<String> ingredients, List<String> tags, double confidence) {
        Map<String, Object> dish = new HashMap<>();
        dish.put("name", name);
        dish.put("calories", calories);
        dish.put("protein", protein);
        dish.put("fat", fat);
        dish.put("carbs", carbs);
        dish.put("difficulty", difficulty);
        dish.put("preparationTime", prepTime);
        dish.put("ingredients", ingredients);
        dish.put("tags", tags);
        dish.put("confidence", confidence);
        dish.put("nutritionScore", calculateNutritionScore(calories, protein, fat, carbs));
        return dish;
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
                    JsonNode recipeData = objectMapper.readTree(content);
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
                    return result;
                } catch (Exception e) {
                    log.warn("食谱优化解析失败，使用模拟数据");
                }
            }

            return getMockOptimizedRecipe(originalRecipe);

        } catch (Exception e) {
            log.error("食谱优化失败", e);
            return getMockOptimizedRecipe(originalRecipe);
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

    // ========== 模拟数据方法 ==========

    private List<Map<String, Object>> getMockRecipes() {
        List<Map<String, Object>> recipes = new ArrayList<>();

        Map<String, Object> recipe1 = new HashMap<>();
        recipe1.put("name", "清蒸鲈鱼");
        recipe1.put("calorie", 280.0);
        recipe1.put("difficulty", "简单");
        recipe1.put("ingredients", "鲈鱼, 姜, 葱, 料酒, 蒸鱼豉油");
        recipe1.put("steps", "1. 鲈鱼处理干净; 2. 姜葱切好铺在盘子上; 3. 鲈鱼放姜葱上; 4. 蒸8分钟; 5. 倒蒸鱼豉油, 热油浇上");
        recipes.add(recipe1);

        Map<String, Object> recipe2 = new HashMap<>();
        recipe2.put("name", "清炒时蔬");
        recipe2.put("calorie", 120.3);
        recipe2.put("difficulty", "简单");
        recipe2.put("ingredients", "西兰花, 胡萝卜, 蒜, 盐, 鸡精");
        recipe2.put("steps", "1. 时蔬洗净切好; 2. 蒜切末; 3. 热油炒蒜末; 4. 加入时蔬翻炒; 5. 加盐和鸡精调味");
        recipes.add(recipe2);

        return recipes;
    }

    private Map<String, Object> getMockNutrition(String foodName) {
        Map<String, Object> response = new HashMap<>();
        response.put("foodName", foodName);
        response.put("calorie", 350.5);
        response.put("protein", 20.3);
        response.put("fat", 15.7);
        response.put("carbohydrate", 40.2);
        return response;
    }

    private Map<String, Object> getMockOptimizedRecipe(String originalRecipe) {
        Map<String, Object> result = new HashMap<>();
        result.put("name", "优化食谱");
        result.put("original", originalRecipe);
        result.put("optimized", originalRecipe + "\n\nAI优化建议：减少油盐用量，增加蔬菜比例，采用更健康的烹饪方式。");
        result.put("calorie", 250.0);
        result.put("improvements", Arrays.asList("减少油盐", "增加蔬菜", "营养均衡"));
        return result;
    }
}
