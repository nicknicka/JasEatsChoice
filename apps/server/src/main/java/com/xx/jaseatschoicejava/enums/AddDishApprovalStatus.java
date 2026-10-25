package com.xx.jaseatschoicejava.enums;

/**
 * 加菜审核状态枚举
 * <p>
 * 0-待审核,1-审核通过,2-审核驳回,3-已撤回,4-超时驳回
 *
 * @Author nickxiao
 * @Date 2026/01/24
 */
public enum AddDishApprovalStatus {

    /**
     * 待审核
     */
    PENDING(0, "待审核"),

    /**
     * 审核通过
     */
    APPROVED(1, "审核通过"),

    /**
     * 审核驳回
     */
    REJECTED(2, "审核驳回"),

    /**
     * 已撤回
     */
    WITHDRAWN(3, "已撤回"),

    /**
     * 超时驳回
     */
    TIMEOUT_REJECTED(4, "超时驳回");

    private final Integer value;
    private final String desc;

    AddDishApprovalStatus(Integer value, String desc) {
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
    public static AddDishApprovalStatus fromValue(Integer value) {
        for (AddDishApprovalStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid approval status value: " + value);
    }
}
