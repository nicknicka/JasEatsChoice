package com.xx.jaseatschoicejava.service;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 智谱AI服务接口
 *
 * 注意：部分功能已迁移到 Agent 系统
 * - chat() → NutritionAiAgent
 * - analyzeNutrition() → NutritionAiAgent
 * - recommendRecipe() → RecommendationAiAgent
 *
 * 本接口保留视觉识别和特殊功能
 *
 * @author Claude
 * @since 2026-03-22
 */
public interface ZhipuAIService {

    /**
     * AI菜品识别
     * @param imageUrl 图片URL
     * @return 识别结果
     */
    Map<String, Object> recognizeDish(String imageUrl);

    /**
     * AI菜品识别（Base64编码）
     * @param imageBase64 Base64编码的图片数据
     * @return 识别结果
     */
    Map<String, Object> recognizeDishWithBase64(String imageBase64);

    /**
     * AI食谱优化
     * @param originalRecipe 原始食谱
     * @return 优化后的食谱
     */
    Map<String, Object> optimizeRecipe(String originalRecipe);

    /**
     * AI食谱优化（带进度回调，用于 SSE 流式推送）
     *
     * 三阶段进度：分析食谱 → AI优化 → 生成结果
     *
     * @param originalRecipe 原始食谱
     * @param progressCallback 进度回调，接收进度描述文本
     * @return 优化后的食谱
     */
    Map<String, Object> optimizeRecipeWithProgress(String originalRecipe, Consumer<String> progressCallback);

    /**
     * AI生成推荐理由
     * @param dishName 菜品名称
     * @param userProfile 用户画像信息
     * @param context 上下文信息（天气、时间等）
     * @return 推荐理由
     */
    String generateRecommendationReason(String dishName, Map<String, Object> userProfile, Map<String, Object> context);
}
