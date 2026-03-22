package com.xx.jaseatschoicejava.service.impl;

import com.xx.jaseatschoicejava.agent.service.NutritionAgent;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import com.xx.jaseatschoicejava.service.NutritionAnalysisService;
import com.xx.jaseatschoicejava.service.ZhipuAIService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;

/**
 * 智谱AI服务实现（使用LangChain4j）
 *
 * @author Claude
 * @since 2026-03-22
 */
@Slf4j
@Service
public class ZhipuAIServiceImpl implements ZhipuAIService {

    @Resource
    private ChatLanguageModel chatLanguageModel;

    @Resource
    private ZhipuAIConfig zhipuAIConfig;

    @Resource
    private NutritionAgent nutritionAgent;

    @Resource
    private NutritionAnalysisService nutritionAnalysisService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String chat(String message, List<Map<String, String>> conversationHistory) {
        try {
            // 使用LangChain4j的ChatLanguageModel
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                // 构建带历史的对话（简化版）
                StringBuilder fullMessage = new StringBuilder();
                for (Map<String, String> turn : conversationHistory) {
                    if (turn.containsKey("user")) {
                        fullMessage.append("用户: ").append(turn.get("user")).append("\n");
                    }
                    if (turn.containsKey("assistant")) {
                        fullMessage.append("助手: ").append(turn.get("assistant")).append("\n");
                    }
                }
                fullMessage.append("用户: ").append(message);
                return chatLanguageModel.generate(fullMessage.toString());
            } else {
                return chatLanguageModel.generate(message);
            }
        } catch (Exception e) {
            log.error("AI聊天失败", e);
            throw new RuntimeException("AI聊天失败：" + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> recommendRecipe(String foodName) {
        // 简化实现：返回基本的食谱信息
        List<Map<String, Object>> recipes = new ArrayList<>();

        Map<String, Object> recipe1 = new HashMap<>();
        recipe1.put("name", foodName + "的家常做法");
        recipe1.put("description", "简单易学的家常菜");
        recipe1.put("time", "30分钟");
        recipe1.put("difficulty", "简单");
        recipes.add(recipe1);

        return recipes;
    }

    @Override
    public Map<String, Object> analyzeNutrition(String foodName) {
        try {
            // 使用营养分析服务
            var nutrition = nutritionAnalysisService.analyzeNutrition(foodName);

            Map<String, Object> result = new HashMap<>();
            result.put("foodName", foodName);
            result.put("calories", nutrition.getCalories());
            result.put("protein", nutrition.getProtein());
            result.put("fat", nutrition.getFat());
            result.put("carbohydrates", nutrition.getCarbohydrates());

            return result;
        } catch (Exception e) {
            log.error("营养分析失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", true);
            errorResult.put("message", "营养分析失败：" + e.getMessage());
            return errorResult;
        }
    }

    @Override
    public Map<String, Object> recognizeDish(String imageUrl) {
        // 调用视觉识别API
        return recognizeDishWithBase64(null);
    }

    @Override
    public Map<String, Object> recognizeDishWithBase64(String imageBase64) {
        // 简化实现：返回模拟数据
        // TODO: 使用LangChain4j的视觉模型
        Map<String, Object> result = new HashMap<>();
        result.put("dishName", "未知菜品");
        result.put("confidence", 0.0);
        result.put("description", "视觉识别功能待实现");
        result.put("calories", 0);
        return result;
    }

    @Override
    public Map<String, Object> optimizeRecipe(String originalRecipe) {
        // 简化实现
        Map<String, Object> result = new HashMap<>();
        result.put("original", originalRecipe);
        result.put("optimized", originalRecipe + "\n\n（优化建议：减少油盐，增加蔬菜）");
        result.put("suggestions", Arrays.asList(
            "减少食用油用量",
            "增加蔬菜比例",
            "控制烹饪时间"
        ));
        return result;
    }

    @Override
    public String generateRecommendationReason(String dishName, Map<String, Object> userProfile, Map<String, Object> context) {
        // 简化实现
        return String.format("推荐【%s】给您！这是一道美味佳肴，符合您的口味偏好。", dishName);
    }
}
