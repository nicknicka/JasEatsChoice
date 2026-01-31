package com.xx.jaseatschoicejava.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 想吃列表审核状态枚举
 *
 * @author Claude
 * @since 2025-01-30
 */
@Getter
@AllArgsConstructor
public enum WishListAuditStatus {

    /**
     * 待审核
     */
    PENDING(0, "待审核"),

    /**
     * 已通过
     */
    APPROVED(1, "已通过"),

    /**
     * 已拒绝
     */
    REJECTED(2, "已拒绝"),

    /**
     * 申诉中
     */
    APPEALING(3, "申诉中"),

    /**
     * 申诉成功
     */
    APPEAL_APPROVED(4, "申诉成功"),

    /**
     * 申诉失败
     */
    APPEAL_REJECTED(5, "申诉失败"),

    /**
     * 超时自动通过
     */
    AUTO_APPROVED(6, "超时自动通过"),

    /**
     * 已下架（用户主动撤回）
     */
    WITHDRAWN(7, "已撤回");

    private final Integer code;
    private final String description;

    /**
     * 根据code获取枚举
     */
    public static WishListAuditStatus getByCode(Integer code) {
        if (code == null) {
            return PENDING;
        }
        for (WishListAuditStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return PENDING;
    }

    /**
     * 判断是否为终态（不再变化）
     */
    public boolean isFinalStatus() {
        return this == APPROVED ||
               this == APPEAL_APPROVED ||
               this == WITHDRAWN;
    }

    /**
     * 判断是否可以申诉
     */
    public boolean canAppeal() {
        return this == REJECTED;
    }

    /**
     * 判断是否为拒绝状态
     */
    public boolean isRejected() {
        return this == REJECTED || this == APPEAL_REJECTED;
    }

    /**
     * 判断是否为通过状态
     */
    public boolean isApproved() {
        return this == APPROVED ||
               this == AUTO_APPROVED ||
               this == APPEAL_APPROVED;
    }
}
