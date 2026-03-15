package com.xx.jaseatschoicejava.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI工具函数类型枚举
 * 用于Function Calling功能，替代硬编码的字符串
 *
 * @author Claude
 * @since 2026-03-13
 */
@Getter
@AllArgsConstructor
public enum AiFunctionType {

    /**
     * 搜索菜品
     */
    SEARCH_DISHES("search_dishes", "根据关键词或分类搜索菜品", 5000),

    /**
     * 获取菜品详情
     */
    GET_DISH_DETAILS("get_dish_details", "获取指定菜品的详细信息", 3000),

    /**
     * 创建订单
     */
    CREATE_ORDER("create_order", "创建一个新的订单", 10000),

    /**
     * 查询订单状态
     */
    GET_ORDER_STATUS("get_order_status", "查询订单的当前状态", 3000),

    /**
     * 查询订单列表
     */
    LIST_ORDERS("list_orders", "查询用户的所有订单列表", 5000),

    /**
     * 获取用户偏好
     */
    GET_USER_PREFERENCES("get_user_preferences", "获取用户的饮食偏好和历史记录", 3000),

    /**
     * 分析营养信息
     */
    ANALYZE_NUTRITION("analyze_nutrition", "分析食物的营养成分和热量", 5000),

    /**
     * 取消订单
     */
    CANCEL_ORDER("cancel_order", "取消用户指定的订单", 5000),

    /**
     * 催单
     */
    URGE_ORDER("urge_order", "催促商家加快订单处理进度", 3000),

    /**
     * 获取热门菜品
     */
    GET_HOT_DISHES("get_hot_dishes", "获取热门菜品推荐", 5000),

    /**
     * 获取今日推荐
     */
    GET_TODAY_RECOMMENDATIONS("get_today_recommendations", "根据时间和用户偏好获取今日推荐菜品", 5000),

    /**
     * 获取时间场景推荐
     */
    GET_TIME_RECOMMENDATIONS("get_time_recommendations", "根据用餐时间场景推荐菜品（早餐/午餐/晚餐/夜宵）", 5000),

    /**
     * 获取今日热量统计
     */
    GET_TODAY_CALORIES("get_today_calories", "统计用户今日摄入的热量", 5000),

    /**
     * 分析营养摄入
     */
    ANALYZE_NUTRITION_INTAKE("analyze_nutrition_intake", "分析用户的营养摄入情况", 5000),

    /**
     * 计算BMI
     */
    CALCULATE_BMI("calculate_bmi", "计算用户的BMI指数", 3000),

    /**
     * 获取健康建议
     */
    GET_HEALTH_ADVICE("get_health_advice", "根据用户情况提供健康饮食建议", 5000),

    /**
     * 查看收藏列表
     */
    GET_FAVORITES("get_favorites", "查看用户的收藏列表", 3000),

    /**
     * 添加收藏
     */
    ADD_FAVORITE("add_favorite", "添加菜品到收藏", 3000),

    /**
     * 获取用户评价
     */
    GET_USER_REVIEWS("get_user_reviews", "获取用户的评价列表", 5000),

    /**
     * 获取用户优惠券
     */
    GET_USER_COUPONS("get_user_coupons", "获取用户的可用优惠券", 3000),

    /**
     * 获取用户信息
     */
    GET_USER_INFO("get_user_info", "获取用户的详细信息和档案", 5000);

    /**
     * 函数名称（用于API调用）
     */
    private final String functionName;

    /**
     * 函数描述
     */
    private final String description;

    /**
     * 超时时间（毫秒）
     */
    private final int timeout;

    /**
     * 根据函数名称获取枚举
     *
     * @param functionName 函数名称
     * @return 枚举实例，未找到返回null
     */
    public static AiFunctionType fromFunctionName(String functionName) {
        if (functionName == null || functionName.trim().isEmpty()) {
            return null;
        }

        for (AiFunctionType type : values()) {
            if (type.functionName.equals(functionName)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 验证函数名称是否有效
     *
     * @param functionName 函数名称
     * @return 是否有效
     */
    public static boolean isValidFunction(String functionName) {
        return fromFunctionName(functionName) != null;
    }

    /**
     * 获取所有已启用的函数名称列表
     *
     * @return 函数名称列表
     */
    public static String[] getAllFunctionNames() {
        AiFunctionType[] types = values();
        String[] names = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            names[i] = types[i].functionName;
        }
        return names;
    }
}
