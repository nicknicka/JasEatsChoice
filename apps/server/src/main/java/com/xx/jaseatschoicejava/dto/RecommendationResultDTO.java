package com.xx.jaseatschoicejava.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 推荐结果DTO
 */
@Data
public class RecommendationResultDTO {

    /**
     * 菜品ID
     */
    private String dishId;

    /**
     * 菜品名称
     */
    private String dishName;

    /**
     * 菜品图片
     */
    private String dishImage;

    /**
     * 菜品分类
     */
    private String category;

    /**
     * 推荐得分
     */
    private BigDecimal score;

    /**
     * 推荐排序位置
     */
    private Integer rank;

    /**
     * 推荐理由
     */
    private RecommendationReason reason;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 卡路里
     */
    private Integer calories;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 评分
     */
    private BigDecimal rating;

    /**
     * 推荐来源算法
     */
    private String source;

    /**
     * 推荐理由内部类
     */
    @Data
    public static class RecommendationReason {
        /**
         * 主要推荐理由
         */
        private String primary;

        /**
         * 详细因素列表
         */
        private List<ReasonFactor> factors;
    }

    /**
     * 理由因素内部类
     */
    @Data
    public static class ReasonFactor {
        /**
         * 因素类型
         */
        private String type;

        /**
         * 因素名称
         */
        private String name;

        /**
         * 因素得分 (0-1)
         */
        private Double score;
    }
}
