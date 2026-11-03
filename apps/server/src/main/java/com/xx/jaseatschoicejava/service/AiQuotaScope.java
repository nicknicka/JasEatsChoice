package com.xx.jaseatschoicejava.service;

/**
 * 独立计数的 AI 能力类型。
 */
public enum AiQuotaScope {

    DISH_RECOGNITION("dish-recognition"),
    RECIPE("recipe");

    private final String key;

    AiQuotaScope(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
