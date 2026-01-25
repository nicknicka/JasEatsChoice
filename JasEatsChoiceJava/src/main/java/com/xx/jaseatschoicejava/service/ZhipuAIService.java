package com.xx.jaseatschoicejava.service;

import java.util.List;
import java.util.Map;

/**
 * 智谱AI服务接口
 */
public interface ZhipuAIService {

    /**
     * AI聊天对话
     * @param message 用户消息
     * @param conversationHistory 对话历史（可选，用于上下文理解）
     * @return AI回复内容
     */
    String chat(String message, List<Map<String, String>> conversationHistory);

    /**
     * AI食谱推荐
     * @param foodName 食物名称或需求描述
     * @return 推荐的食谱列表
     */
    List<Map<String, Object>> recommendRecipe(String foodName);

    /**
     * AI营养分析
     * @param foodName 食物名称
     * @return 营养成分信息
     */
    Map<String, Object> analyzeNutrition(String foodName);

    /**
     * AI菜品识别
     * @param imageUrl 图片URL
     * @return 识别结果
     */
    Map<String, Object> recognizeDish(String imageUrl);

    /**
     * AI食谱优化
     * @param originalRecipe 原始食谱
     * @return 优化后的食谱
     */
    Map<String, Object> optimizeRecipe(String originalRecipe);

    /**
     * AI生成推荐理由
     * @param dishName 菜品名称
     * @param userProfile 用户画像信息
     * @param context 上下文信息（天气、时间等）
     * @return 推荐理由
     */
    String generateRecommendationReason(String dishName, Map<String, Object> userProfile, Map<String, Object> context);
}
