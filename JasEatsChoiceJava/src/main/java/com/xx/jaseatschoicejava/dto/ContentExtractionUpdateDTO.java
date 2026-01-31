package com.xx.jaseatschoicejava.dto;

import lombok.Data;

import java.util.List;

/**
 * 内容提取更新DTO
 *
 * @author Claude
 * @since 2025-01-31
 */
@Data
public class ContentExtractionUpdateDTO {

    /**
     * 提取ID
     */
    private String extractionId;

    /**
     * 菜品名称
     */
    private String dishName;

    /**
     * 菜品图片
     */
    private String dishImage;

    /**
     * 菜品描述
     */
    private String description;

    /**
     * 食材列表
     */
    private List<IngredientItem> ingredients;

    /**
     * 制作步骤
     */
    private List<StepItem> steps;

    /**
     * 制作时长（分钟）
     */
    private Integer cookingTime;

    /**
     * 难度
     */
    private String difficulty;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 卡路里
     */
    private Integer calories;

    /**
     * 食材项
     */
    @Data
    public static class IngredientItem {
        /**
         * 食材名称
         */
        private String name;

        /**
         * 用量
         */
        private String amount;
    }

    /**
     * 步骤项
     */
    @Data
    public static class StepItem {
        /**
         * 步骤序号
         */
        private Integer stepNumber;

        /**
         * 步骤描述
         */
        private String description;

        /**
         * 步骤图片（可选）
         */
        private String image;
    }
}
