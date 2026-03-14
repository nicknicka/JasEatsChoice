package com.xx.jaseatschoicejava.enums;

/**
 * 订单状态枚举
 * <p>
 * 0-待支付、1-待接单、2-制作中、3-已完成、4-已取消
 *
 * @Author nickxiao
 * @Date 2025/11/22
 */
public enum OrderStatus {

    /**
     * 待支付
     */
    PENDING_PAYMENT(0, "待支付"),

    /**
     * 待接单
     */
    PENDING_ACCEPTANCE(1, "待接单"),

    /**
     * 制作中（包含备菜、烹饪、待上菜）
     */
    PREPARING(2, "制作中"),

    /**
     * 已完成（包含已送达、待评价、已评价）
     */
    COMPLETED(3, "已完成"),

    /**
     * 已取消
     */
    CANCELLED(4, "已取消");

    private Integer value;
    private String desc;

    OrderStatus(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
}

