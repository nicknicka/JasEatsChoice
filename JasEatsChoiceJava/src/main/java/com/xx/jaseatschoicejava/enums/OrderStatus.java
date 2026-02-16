package com.xx.jaseatschoicejava.enums;

/**
 * 订单状态枚举
 * <p>
 * 0-待支付、1-待接单、2-备菜中、3-烹饪中、4-待上菜、5-已送达、6-已取消、7-待评价、8-已评价
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
     * 备菜中
     */
    PREPARING_INGREDIENTS(2, "备菜中"),

    /**
     * 烹饪中
     */
    COOKING(3, "烹饪中"),

    /**
     * 待上菜
     */
    WAITING_FOR_SERVING(4, "待上菜"),

    /**
     * 已送达
     */
    DELIVERED(5, "已送达"),

    /**
     * 已取消
     */
    CANCELLED(6, "已取消"),

    /**
     * 待评价
     */
    PENDING_COMMENT(7, "待评价"),

    /**
     * 已评价
     */
    REVIEWED(8, "已评价");

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

