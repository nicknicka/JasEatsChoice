package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.dto.NutritionInfo;

/**
 * 营养分析服务接口
 * 提供食物营养成分分析功能
 *
 * @author Claude
 * @since 2026-03-13
 */
public interface NutritionAnalysisService {

    /**
     * 分析食物的营养成分
     *
     * @param foodName 食物名称
     * @return 营养信息
     */
    NutritionInfo analyzeNutrition(String foodName);

    /**
     * 分析食物的营养成分（指定份量）
     *
     * @param foodName 食物名称
     * @param portion  份量（克）
     * @return 营养信息
     */
    NutritionInfo analyzeNutrition(String foodName, int portion);

    /**
     * 批量分析多种食物的营养成分
     *
     * @param foodNames 食物名称列表
     * @return 营养信息列表
     */
    java.util.List<NutritionInfo> batchAnalyzeNutrition(java.util.List<String> foodNames);

    /**
     * 获取食物的营养评级
     *
     * @param foodName 食物名称
     * @return 营养评级（A/B/C/D/E）
     */
    String getNutritionGrade(String foodName);

    /**
     * 根据营养需求推荐食物
     *
     * @param maxCalories     最大卡路里
     * @param minProtein      最小蛋白质（克）
     * @param dietaryRestrictions 饮食限制（如：素食、低盐等）
     * @return 推荐的食物列表
     */
    java.util.List<String> recommendFoodByNutrition(
            Integer maxCalories,
            Integer minProtein,
            String dietaryRestrictions
    );
}
