package com.xx.jaseatschoicejava.enums;

/**
 * 反馈类型。首期保留全部类型，只有分诊后判定为平台系统问题的反馈才进入研发队列。
 */
public enum FeedbackType {

    BUG("bug", "功能异常"),
    FEATURE("feature", "功能建议"),
    UI("ui", "界面问题"),
    PERFORMANCE("performance", "性能问题"),
    ORDER("order", "订单问题"),
    PAYMENT("payment", "支付问题"),
    OTHER("other", "其他问题");

    private final String code;
    private final String label;

    FeedbackType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static FeedbackType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (FeedbackType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}
