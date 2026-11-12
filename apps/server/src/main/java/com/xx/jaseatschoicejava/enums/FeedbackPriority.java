package com.xx.jaseatschoicejava.enums;

/**
 * 反馈分诊优先级。
 *
 * <p>取值与 admin 端公告的优先级保持一致（normal/important/urgent），
 * 避免同一个后台出现两套「优先级」词汇；默认 normal。
 */
public enum FeedbackPriority {

    NORMAL("normal", "普通"),
    IMPORTANT("important", "重要"),
    URGENT("urgent", "紧急");

    private final String code;
    private final String label;

    FeedbackPriority(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 按存储编码解析；未知编码返回 null（由调用方决定拒绝还是回落）。
     */
    public static FeedbackPriority fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (FeedbackPriority priority : values()) {
            if (priority.code.equalsIgnoreCase(code.trim())) {
                return priority;
            }
        }
        return null;
    }

    /**
     * 展示用中文名；未知编码原样返回，便于排查脏数据。
     */
    public static String labelOf(String code) {
        FeedbackPriority priority = fromCode(code);
        return priority == null ? code : priority.getLabel();
    }
}
