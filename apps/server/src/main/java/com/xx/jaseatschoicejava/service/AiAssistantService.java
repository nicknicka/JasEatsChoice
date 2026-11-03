package com.xx.jaseatschoicejava.service;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * AI 饮食助手服务接口
 *
 * 注意：部分功能已迁移到 Agent 系统
 * - chat() → NutritionAiAgent
 * - analyzeNutrition() → NutritionAiAgent
 * - recommendRecipe() → RecommendationAiAgent
 *
 * 本接口保留视觉识别和特殊功能
 *

 * @since 2026-03-22
 */
public interface AiAssistantService {

    /** 食谱优化支持的优化目标标识。 */
    Set<String> RECIPE_GOALS = Set.of("balanced", "fat_loss", "muscle_gain", "blood_sugar_control");

    /** 未指定优化目标时的默认值。 */
    String DEFAULT_RECIPE_GOAL = "balanced";

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
    default Map<String, Object> recognizeDishWithBase64(String imageBase64) {
        return recognizeDishWithBase64(imageBase64, "image/jpeg");
    }

    /**
     * AI菜品识别（Base64编码，保留真实图片 MIME）
     * @param imageBase64 Base64编码的图片数据
     * @param mimeType 图片 MIME 类型
     * @return 识别结果
     */
    Map<String, Object> recognizeDishWithBase64(String imageBase64, String mimeType);

    /**
     * AI食谱优化
     * @param originalRecipe 原始食谱
     * @param goal 优化目标，见 {@link #RECIPE_GOALS}
     * @return 单方案统一结构
     */
    Map<String, Object> optimizeRecipe(String originalRecipe, String goal);

    /**
     * AI食谱优化（带进度回调，用于 SSE 流式推送）
     *
     * 三阶段进度：分析食谱 → AI优化 → 生成结果
     *
     * @param originalRecipe 原始食谱
     * @param goal 优化目标，见 {@link #RECIPE_GOALS}
     * @param progressCallback 进度回调，接收进度描述文本
     * @return 单方案统一结构
     */
    Map<String, Object> optimizeRecipeWithProgress(String originalRecipe, String goal,
            Consumer<String> progressCallback);

    /**
     * AI食谱优化（真正流式输出，逐 token 推送）
     *
     * @param originalRecipe 原始食谱
     * @param goal 优化目标，见 {@link #RECIPE_GOALS}
     * @param progressCallback 阶段进度回调
     * @param tokenCallback 逐 token 回调
     * @param onComplete 流式结束回调
     */
    void optimizeRecipeStreaming(String originalRecipe,
            String goal,
            Consumer<String> progressCallback,
            Consumer<String> tokenCallback,
            Runnable onComplete);

    /**
     * AI生成推荐理由
     * @param dishName 菜品名称
     * @param userProfile 用户画像信息
     * @param context 上下文信息（天气、时间等）
     * @return 推荐理由
     */
    String generateRecommendationReason(String dishName, Map<String, Object> userProfile, Map<String, Object> context);
}
