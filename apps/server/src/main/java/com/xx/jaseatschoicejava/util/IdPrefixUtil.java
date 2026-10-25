package com.xx.jaseatschoicejava.util;

/**
 * ID前缀工具类
 * 统一为不同类型的ID添加前缀，确保系统内ID的唯一性和可读性
 */
public class IdPrefixUtil {

    // 用户ID前缀
    public static final String USER_PREFIX = "U";
    // 商家ID前缀
    public static final String MERCHANT_PREFIX = "M";
    // 群ID前缀
    public static final String GROUP_PREFIX = "G";
    // 订单ID前缀
    public static final String ORDER_PREFIX = "O";

    /**
     * 为用户ID添加前缀
     * @param userId 用户ID（Long或String类型）
     * @return 带前缀的用户ID
     */
    public static String addUserPrefix(Object userId) {
        if (userId == null) {
            return null;
        }
        String idStr = userId.toString().trim();
        if (idStr.startsWith(USER_PREFIX)) {
            return idStr; // 已经有前缀，直接返回
        }
        return USER_PREFIX + idStr;
    }

    /**
     * 为商家ID添加前缀
     * @param merchantId 商家ID（Long或String类型）
     * @return 带前缀的商家ID
     */
    public static String addMerchantPrefix(Object merchantId) {
        if (merchantId == null) {
            return null;
        }
        String idStr = merchantId.toString().trim();
        if (idStr.startsWith(MERCHANT_PREFIX)) {
            return idStr; // 已经有前缀，直接返回
        }
        return MERCHANT_PREFIX + idStr;
    }

    /**
     * 为群ID添加前缀
     * @param groupId 群ID（Long或String类型）
     * @return 带前缀的群ID
     */
    public static String addGroupPrefix(Object groupId) {
        if (groupId == null) {
            return null;
        }
        String idStr = groupId.toString().trim();
        if (idStr.startsWith(GROUP_PREFIX)) {
            return idStr; // 已经有前缀，直接返回
        }
        return GROUP_PREFIX + idStr;
    }

    /**
     * 为订单ID添加前缀
     * @param orderId 订单ID（Long或String类型）
     * @return 带前缀的订单ID
     */
    public static String addOrderPrefix(Object orderId) {
        if (orderId == null) {
            return null;
        }
        String idStr = orderId.toString().trim();
        if (idStr.startsWith(ORDER_PREFIX)) {
            return idStr; // 已经有前缀，直接返回
        }
        return ORDER_PREFIX + idStr;
    }

    /**
     * 移除ID前缀
     * @param prefixedId 带前缀的ID
     * @return 不带前缀的ID
     */
    public static String removePrefix(String prefixedId) {
        if (prefixedId == null) {
            return null;
        }
        prefixedId = prefixedId.trim();
        // 检查并移除已知前缀
        if (prefixedId.startsWith(USER_PREFIX)) {
            return prefixedId.substring(USER_PREFIX.length());
        }
        if (prefixedId.startsWith(MERCHANT_PREFIX)) {
            return prefixedId.substring(MERCHANT_PREFIX.length());
        }
        if (prefixedId.startsWith(GROUP_PREFIX)) {
            return prefixedId.substring(GROUP_PREFIX.length());
        }
        if (prefixedId.startsWith(ORDER_PREFIX)) {
            return prefixedId.substring(ORDER_PREFIX.length());
        }
        // 没有已知前缀，直接返回
        return prefixedId;
    }
}
