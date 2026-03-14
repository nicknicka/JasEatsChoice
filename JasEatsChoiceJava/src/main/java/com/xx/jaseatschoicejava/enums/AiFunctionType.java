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
     * 获取用户偏好
     */
    GET_USER_PREFERENCES("get_user_preferences", "获取用户的饮食偏好和历史记录", 3000),

    /**
     * 分析营养信息
     */
    ANALYZE_NUTRITION("analyze_nutrition", "分析食物的营养成分和热量", 5000);

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
