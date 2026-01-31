package com.xx.jaseatschoicejava.vo;

import lombok.Data;

import java.util.List;

/**
 * 节日推荐VO
 *
 * @author Claude
 * @since 2025-01-31
 */
@Data
public class FestivalRecommendVO {

    /**
     * 节日ID
     */
    private String festivalId;

    /**
     * 节日名称
     */
    private String festivalName;

    /**
     * 节日类型
     */
    private String festivalType;

    /**
     * 节日描述
     */
    private String description;

    /**
     * 节日图标
     */
    private String icon;

    /**
     * 主题颜色
     */
    private String themeColor;

    /**
     * 背景图片
     */
    private String backgroundImage;

    /**
     * 是否当前生效
     */
    private Boolean isCurrent;

    /**
     * 距离节日天数
     */
    private Integer daysUntilFestival;

    /**
     * 推荐菜品列表
     */
    private List<DishRecommendItemVO> recommendDishes;

    /**
     * 推荐菜品项内部类
     */
    @Data
    public static class DishRecommendItemVO {
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
         * 菜品价格
         */
        private java.math.BigDecimal dishPrice;

        /**
         * 推荐类型
         */
        private String recommendType;

        /**
         * 推荐理由
         */
        private String recommendReason;

        /**
         * 优先级
         */
        private Integer priority;

        /**
         * 推荐记录ID
         */
        private String recommendHistoryId;
    }
}
