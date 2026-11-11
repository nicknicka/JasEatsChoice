package com.xx.jaseatschoicejava.enums;

/**
 * 反馈处理结果编码。用于在状态机之外表达「为什么解决/关闭」，避免发明额外状态。
 */
public enum FeedbackResolutionCode {

    DUPLICATE("duplicate", "重复反馈"),
    WONTFIX("wontfix", "不予处理"),
    NOT_REPRODUCIBLE("not_reproducible", "无法复现"),
    FIXED("fixed", "已修复"),
    ANSWERED("answered", "已答复");

    private final String code;
    private final String label;

    FeedbackResolutionCode(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static FeedbackResolutionCode fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (FeedbackResolutionCode value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }
        return null;
    }
}
