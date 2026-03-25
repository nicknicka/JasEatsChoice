package com.xx.jaseatschoicejava.agent.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商家下单卡片数据传输对象
 *
 * 用于AI推荐商家时返回结构化的卡片数据，前端可以根据此数据展示商家卡片和菜品选择弹窗
 *
 * @author Claude
 * @since 2026-03-25
 */
@Data
public class MerchantOrderCardDTO {

    /**
     * 卡片类型标识
     */
    private String cardType;

    /**
     * 商家信息
     */
    private MerchantInfo merchant;

    /**
     * AI预选的菜品列表
     */
    private List<PreSelectedDish> preSelectedDishes;

    /**
     * 就餐方式
     */
    private String diningMode;

    /**
     * 预估总价
     */
    private BigDecimal estimatedTotal;

    /**
     * 操作按钮配置
     */
    private ActionButtons actionButtons;

    /**
     * 推荐理由
     */
    private String recommendationReason;

    /**
     * 商家信息
     */
    @Data
    public static class MerchantInfo {
        /**
         * 商家ID
         */
        private String merchantId;

        /**
         * 商家名称
         */
        private String name;

        /**
         * 商家评分
         */
        private Double rating;

        /**
         * 人均价格
         */
        private BigDecimal averagePrice;

        /**
         * 商家地址
         */
        private String address;

        /**
         * 距离（米）
         */
        private Integer distance;

        /**
         * 预计送达/取餐时间（分钟）
         */
        private Integer estimatedTime;

        /**
         * 商家状态（是否营业中）
         */
        private Boolean isOpen;
    }

    /**
     * 预选菜品
     */
    @Data
    public static class PreSelectedDish {
        /**
         * 菜品ID
         */
        private String dishId;

        /**
         * 菜品名称
         */
        private String dishName;

        /**
         * 价格
         */
        private BigDecimal price;

        /**
         * 数量
         */
        private Integer quantity;

        /**
         * 卡路里
         */
        private Integer calories;

        /**
         * 菜品图片URL
         */
        private String imageUrl;

        /**
         * AI推荐理由
         */
        private String reason;

        /**
         * 分类
         */
        private String category;
    }

    /**
     * 操作按钮配置
     */
    @Data
    public static class ActionButtons {
        /**
         * 主按钮文案（确认下单）
         */
        private String primaryButton;

        /**
         * 次要按钮文案（调整菜品）
         */
        private String secondaryButton;

        /**
         * 第三按钮文案（换一家）
         */
        private String tertiaryButton;

        /**
         * 是否允许AI自动下单
         */
        private Boolean allowAIOrder;

        /**
         * 是否需要用户确认支付
         */
        private Boolean requirePaymentConfirmation;
    }
}
