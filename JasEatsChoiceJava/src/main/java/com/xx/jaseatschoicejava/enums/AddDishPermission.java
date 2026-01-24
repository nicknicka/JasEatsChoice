package com.xx.jaseatschoicejava.enums;

/**
 * 加菜权限枚举
 * <p>
 * 0-全员可加菜,1-仅发起者可加菜
 *
 * @Author nickxiao
 * @Date 2026/01/24
 */
public enum AddDishPermission {

    /**
     * 全员可加菜
     */
    ALL_MEMBERS(0, "全员可加菜"),

    /**
     * 仅发起者可加菜
     */
    INITIATOR_ONLY(1, "仅发起者可加菜");

    private final Integer value;
    private final String desc;

    AddDishPermission(Integer value, String desc) {
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
    public static AddDishPermission fromValue(Integer value) {
        for (AddDishPermission permission : values()) {
            if (permission.getValue().equals(value)) {
                return permission;
            }
        }
        throw new IllegalArgumentException("Invalid permission value: " + value);
    }
}
