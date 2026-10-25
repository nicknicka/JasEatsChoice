package com.xx.jaseatschoicejava.enums;

/**
 * 饮食目标枚举
 *
 * @Author nickxiao
 * @Date 2025/11/22
 */
public enum DietGoal {

    /**
     * 减肥
     */
    LOSE_WEIGHT("lose_weight", "减肥"),

    /**
     * 保持健康
     */
    KEEP_FIT("keep_fit", "保持健康"),

    /**
     * 增肌
     */
    GAIN_WEIGHT("gain_weight", "增肌");

    private String value;
    private String desc;

    DietGoal(String value, String desc) {
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

