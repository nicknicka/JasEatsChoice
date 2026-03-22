package com.xx.jaseatschoicejava.agent.service;

import com.xx.jaseatschoicejava.agent.tools.NutritionTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 营养分析Agent
 *
 * 说明：LangChain4j依赖下载中，暂时使用简化版本
 * 等依赖下载完成后，将使用AiServices.builder()构建完整Agent
 *
 * @author Claude
 * @since 2026-03-22
 */
@Slf4j
@Service
public class NutritionAgent {

    @Resource
    private NutritionTools nutritionTools;

    /**
     * Agent的System Prompt（系统提示词）
     */
    private static final String SYSTEM_PROMPT = """
            你是"佳食宜选"的专业营养师助手。

            你的职责包括：
            1. 分析食物营养成分（卡路里、蛋白质、脂肪、碳水等）
            2. 评估饮食健康度
            3. 提供营养建议
            4. 计算每日热量需求

            回答要求：
            - 使用专业但易懂的语言
            - 提供具体的数据支持
            - 给出可操作的建议
            - 关注用户健康目标

            可用工具：
            - analyzeNutrition：分析单一食物营养
            - analyzeMultipleFoods：批量分析多个食物
            - calculateDailyCalories：计算每日热量需求
            """;

    @PostConstruct
    public void init() {
        log.info("初始化NutritionAgent");
        log.info("System Prompt：{}", SYSTEM_PROMPT.substring(0, 100) + "...");
    }

    /**
     * 处理用户消息
     *
     * @param userMessage 用户消息
     * @return Agent回复
     */
    public String chat(String userMessage) {
        return chat(userMessage, "anonymous");
    }

    /**
     * 处理用户消息（带用户ID）
     *
     * @param userMessage 用户消息
     * @param userId 用户ID
     * @return Agent回复
     */
    public String chat(String userMessage, String userId) {
        log.info("NutritionAgent收到消息 [用户:{}]：{}", userId, userMessage);

        // 简化版本：根据关键词调用工具
        if (userMessage.contains("营养") || userMessage.contains("成分")) {
            return handleNutritionQuery(userMessage);
        } else if (userMessage.contains("卡路里") || userMessage.contains("热量")) {
            return handleCalorieQuery(userMessage);
        } else {
            return "你好！我是营养师助手。我可以帮你：\n" +
                   "1. 分析食物营养成分（例如：苹果的营养成分）\n" +
                   "2. 计算每日热量需求\n" +
                   "3. 提供饮食建议\n\n" +
                   "请问有什么可以帮你的？";
        }
    }

    /**
     * 处理营养查询
     */
    private String handleNutritionQuery(String message) {
        // 提取食物名称（简单实现）
        String foodName = extractFoodName(message);
        if (foodName == null || foodName.isEmpty()) {
            return "请告诉我你想了解哪种食物的营养成分？例如：苹果的营养成分";
        }

        try {
            var nutrition = nutritionTools.analyzeNutrition(foodName);
            // 使用NutritionInfo自带的格式化方法
            return nutrition.toFormattedText();
        } catch (Exception e) {
            System.err.println("营养分析失败：" + e.getMessage());
            return "抱歉，分析" + foodName + "的营养成分时出现错误：" + e.getMessage();
        }
    }

    /**
     * 处理卡路里查询
     */
    private String handleCalorieQuery(String message) {
        return nutritionTools.calculateDailyCalories(70.0, 175.0, 25, "男", "中度");
    }

    /**
     * 从消息中提取食物名称（简单实现）
     */
    private String extractFoodName(String message) {
        // 移除常见的查询词
        String query = message
                .replaceAll("(?i)(营养|成分|分析|多少|怎么样|如何)", "")
                .trim();

        return query.isEmpty() ? null : query;
    }
}
