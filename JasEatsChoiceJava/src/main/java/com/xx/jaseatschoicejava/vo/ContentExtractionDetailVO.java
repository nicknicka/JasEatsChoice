package com.xx.jaseatschoicejava.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 内容提取详情VO
 *
 * @author Claude
 * @since 2025-01-31
 */
@Data
public class ContentExtractionDetailVO {

    /**
     * 提取ID
     */
    private String id;

    /**
     * 内容源ID
     */
    private String sourceId;

    /**
     * 内容URL
     */
    private String contentUrl;

    /**
     * 平台
     */
    private String platform;

    /**
     * 平台名称
     */
    private String platformName;

    /**
     * 原始标题
     */
    private String originalTitle;

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
     * 步骤整体描述
     */
    private String stepsDescription;

    /**
     * 制作时长（分钟）
     */
    private Integer cookingTime;

    /**
     * 难度
     */
    private String difficulty;

    /**
     * 难度名称
     */
    private String difficultyName;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 卡路里
     */
    private Integer calories;

    /**
     * 是否已发布为食谱
     */
    private Boolean isPublished;

    /**
     * 食谱ID
     */
    private String recipeId;

    /**
     * 人工评分
     */
    private Integer manualScore;

    /**
     * 是否人工验证
     */
    private Boolean isVerified;

    /**
     * 提取方式（OCR/NLP/VIDEO）
     */
    private String extractionMethod;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

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
         * 步骤标题
         */
        private String title;

        /**
         * 步骤详细描述
         */
        private String description;

        /**
         * 步骤图片
         */
        private String image;
    }
}
