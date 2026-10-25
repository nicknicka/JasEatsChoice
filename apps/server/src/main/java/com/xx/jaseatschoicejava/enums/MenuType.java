package com.xx.jaseatschoicejava.enums;

/**
 * 菜单类型枚举
 *
 * @Author nickxiao
 * @Date 2025/11/22
 */
public enum MenuType {

    /**
     * 早餐
     */
    BREAKFAST("breakfast", "早餐"),

    /**
     * 午餐
     */
    LUNCH("lunch", "午餐"),

    /**
     * 晚餐
     */
    DINNER("dinner", "晚餐"),

    /**
     * 零食
     */
    SNACK("snack", "零食");

    private String value;
    private String desc;

    MenuType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
}

