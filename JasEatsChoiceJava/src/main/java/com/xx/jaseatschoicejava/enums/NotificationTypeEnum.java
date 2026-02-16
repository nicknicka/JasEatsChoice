package com.xx.jaseatschoicejava.enums;

/**
 * 通知类型枚举
 * <p>
 * 定义系统中所有的通知类型常量
 *
 * @Author nickxiao
 * @Date 2026/02/16
 */
public enum NotificationTypeEnum {

    // ==================== 订单相关 ====================
    /**
     * 支付成功
     */
    ORDER_PAYMENT_SUCCESS("order_payment_success", "订单", "支付成功"),

    /**
     * 商家接单
     */
    ORDER_MERCHANT_ACCEPT("order_merchant_accept", "订单", "商家接单"),

    /**
     * 备菜完成
     */
    ORDER_PREPARING_COMPLETE("order_preparing_complete", "订单", "备菜完成"),

    /**
     * 烹饪完成
     */
    ORDER_COOKING_COMPLETE("order_cooking_complete", "订单", "烹饪完成"),

    /**
     * 待上菜
     */
    ORDER_WAITING_SERVING("order_waiting_serving", "订单", "待上菜"),

    /**
     * 订单已送达
     */
    ORDER_DELIVERED("order_delivered", "订单", "订单送达"),

    /**
     * 订单完成（待评价）
     */
    ORDER_COMPLETE("order_complete", "订单", "订单完成"),

    /**
     * 订单已评价
     */
    ORDER_REVIEWED("order_reviewed", "订单", "订单已评价"),

    /**
     * 订单已取消
     */
    ORDER_CANCELLED("order_cancelled", "订单", "订单取消"),

    // ==================== 群订单相关 ====================
    /**
     * 群订单支付成功
     */
    GROUP_ORDER_PAYMENT_SUCCESS("group_order_payment_success", "群订单", "支付成功"),

    /**
     * 群订单商家接单
     */
    GROUP_ORDER_MERCHANT_ACCEPT("group_order_merchant_accept", "群订单", "商家接单"),

    /**
     * 群订单备菜完成
     */
    GROUP_ORDER_PREPARING_COMPLETE("group_order_preparing_complete", "群订单", "备菜完成"),

    /**
     * 群订单烹饪完成
     */
    GROUP_ORDER_COOKING_COMPLETE("group_order_cooking_complete", "群订单", "烹饪完成"),

    /**
     * 群订单待上菜
     */
    GROUP_ORDER_WAITING_SERVING("group_order_waiting_serving", "群订单", "待上菜"),

    /**
     * 群订单已送达
     */
    GROUP_ORDER_DELIVERED("group_order_delivered", "群订单", "订单送达"),

    /**
     * 群订单完成
     */
    GROUP_ORDER_COMPLETE("group_order_complete", "群订单", "订单完成"),

    /**
     * 群订单已取消
     */
    GROUP_ORDER_CANCELLED("group_order_cancelled", "群订单", "订单取消"),

    /**
     * 群订单加菜请求
     */
    GROUP_ORDER_ADDITION_REQUEST("group_order_addition_request", "群订单", "加菜请求"),

    /**
     * 群订单加菜审核通过
     */
    GROUP_ORDER_ADDITION_APPROVED("group_order_addition_approved", "群订单", "加菜审核通过"),

    /**
     * 群订单加菜被驳回
     */
    GROUP_ORDER_ADDITION_REJECTED("group_order_addition_rejected", "群订单", "加菜被驳回"),

    /**
     * 群订单加菜超时
     */
    GROUP_ORDER_ADDITION_TIMEOUT("group_order_addition_timeout", "群订单", "加菜超时"),

    /**
     * 群订单加菜已支付
     */
    GROUP_ORDER_ADDITION_PAID("group_order_addition_paid", "群订单", "加菜已支付"),

    // ==================== 评价相关 ====================
    /**
     * 用户提交评价
     */
    REVIEW_SUBMITTED("review_submitted", "评价", "评价提交"),

    /**
     * 商家回复评价
     */
    REVIEW_REPLY("review_reply", "评价", "商家回复"),

    /**
     * 用户追加评价
     */
    REVIEW_ADDITIONAL("review_additional", "评价", "追加评价"),

    // ==================== 提现相关 ====================
    /**
     * 申请提现
     */
    WITHDRAW_REQUEST("withdraw_request", "钱包", "申请提现"),

    /**
     * 提现审核通过
     */
    WITHDRAW_APPROVED("withdraw_approved", "钱包", "提现审核通过"),

    /**
     * 提现审核拒绝
     */
    WITHDRAW_REJECTED("withdraw_rejected", "钱包", "提现审核拒绝"),

    /**
     * 提现已完成
     */
    WITHDRAW_SUCCESS("withdraw_success", "钱包", "提现已完成"),

    /**
     * 提现失败
     */
    WITHDRAW_FAILED("withdraw_failed", "钱包", "提现失败"),

    // ==================== 商家相关 ====================
    /**
     * 商家注册审核通过
     */
    MERCHANT_APPROVED("merchant_approved", "商家", "注册审核通过"),

    /**
     * 商家注册审核拒绝
     */
    MERCHANT_REJECTED("merchant_rejected", "商家", "注册审核拒绝"),

    // ==================== 菜品相关 ====================
    /**
     * 菜品审核通过
     */
    DISH_APPROVED("dish_approved", "菜品", "审核通过"),

    /**
     * 菜品审核拒绝
     */
    DISH_REJECTED("dish_rejected", "菜品", "审核拒绝"),

    // ==================== 系统通知 ====================
    /**
     * 系统通知
     */
    SYSTEM("system", "系统", "系统通知");

    /**
     * 通知类型代码
     */
    private final String code;

    /**
     * 通知分类
     */
    private final String category;

    /**
     * 通知类型描述
     */
    private final String description;

    NotificationTypeEnum(String code, String category, String description) {
        this.code = code;
        this.category = category;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取枚举
     */
    public static NotificationTypeEnum fromCode(String code) {
        for (NotificationTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid notification type code: " + code);
    }
}
