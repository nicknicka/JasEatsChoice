package com.xx.jaseatschoicejava.enums;

/**
 * 群订单支付模式枚举
 * <p>
 * 0-统一支付,1-个人单独支付
 *
 * @Author nickxiao
 * @Date 2026/01/24
 */
public enum PaymentMode {

    /**
     * 统一支付（加菜需要审核）
     */
    UNIFIED(0, "统一支付"),

    /**
     * 个人单独支付（加菜无需审核）
     */
    INDIVIDUAL(1, "个人单独支付");

    private final Integer value;
    private final String desc;

    PaymentMode(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据值获取枚举
     */
    public static PaymentMode fromValue(Integer value) {
        for (PaymentMode mode : values()) {
            if (mode.getValue().equals(value)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Invalid payment mode value: " + value);
    }
}
