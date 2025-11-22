package com.xx.jaseatschoicejava.enums;

/**
 * 菜单状态枚举
 *
 * @Author nickxiao
 * @Date 2025/11/22
 */
public enum MenuStatus {

    /**
     * 启用
     */
    ACTIVE("active", "启用"),

    /**
     * 禁用
     */
    INACTIVE("inactive", "禁用");

    private String value;
    private String desc;

    MenuStatus(String value, String desc) {
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

