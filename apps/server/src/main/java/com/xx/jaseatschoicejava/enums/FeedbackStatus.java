package com.xx.jaseatschoicejava.enums;

/**
 * 反馈生命周期状态。
 *
 * <p>状态机：pending -> triaging -> needs_info -> triaging；
 * pending/triaging -> rejected；triaging -> accepted -> resolved。
 * 重复、无需修复、无法复现等属于处理结果编码（{@link FeedbackResolutionCode}），不是状态。
 */
public enum FeedbackStatus {

    PENDING("pending", "待分诊"),
    TRIAGING("triaging", "分诊中"),
    NEEDS_INFO("needs_info", "待补充信息"),
    ACCEPTED("accepted", "已接受"),
    REJECTED("rejected", "已拒绝"),
    RESOLVED("resolved", "已解决");

    private final String code;
    private final String label;

    FeedbackStatus(String code, String label) {
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
     * 按存储编码解析状态，未知编码返回 null。
     */
    public static FeedbackStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (FeedbackStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        return null;
    }
}
