package com.xx.jaseatschoicejava.enums;

/**
 * 反馈消息作者类型。
 */
public enum FeedbackAuthorType {

    USER("USER"),
    ADMIN("ADMIN"),
    SYSTEM("SYSTEM");

    private final String code;

    FeedbackAuthorType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
