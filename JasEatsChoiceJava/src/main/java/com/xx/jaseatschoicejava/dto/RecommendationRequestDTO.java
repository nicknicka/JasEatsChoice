package com.xx.jaseatschoicejava.dto;

import lombok.Data;

import java.util.Map;

/**
 * 推荐请求DTO
 */
@Data
public class RecommendationRequestDTO {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 推荐场景: home(首页)/personal(个性化)/cart(购物车)/dish_detail(菜品详情)
     */
    private String scene;

    /**
     * 返回数量限制
     */
    private Integer limit;

    /**
     * 上下文信息
     * - weather: 天气信息
     * - timePeriod: 时段 (早餐/午餐/晚餐/宵夜)
     * - location: 位置信息 {latitude, longitude}
     * - season: 季节
     */
    private Map<String, Object> context;

    /**
     * 排除的菜品ID列表
     */
    private String excludeDishIds;

    /**
     * 是否需要推荐理由
     */
    private Boolean needReason;

    /**
     * 推荐场景枚举
     */
    public enum Scene {
        HOME("home", "首页"),
        PERSONAL("personal", "个性化推荐"),
        CART("cart", "购物车推荐"),
        DISH_DETAIL("dish_detail", "菜品详情相似推荐"),
        MERCHANT("merchant", "商家页面推荐");

        private final String code;
        private final String desc;

        Scene(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
