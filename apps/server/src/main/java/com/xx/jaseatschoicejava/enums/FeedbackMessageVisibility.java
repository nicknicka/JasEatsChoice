package com.xx.jaseatschoicejava.enums;

/**
 * 反馈消息可见性。内部备注永不出现在用户端。
 */
public enum FeedbackMessageVisibility {

    USER("USER"),
    INTERNAL("INTERNAL");

    private final String code;

    FeedbackMessageVisibility(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
