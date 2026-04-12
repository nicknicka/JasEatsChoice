package com.xx.jaseatschoicejava.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 提取状态枚举
 *
 * @author Claude
 * @since 2025-01-31
 */
@Getter
@AllArgsConstructor
public enum ExtractionStatus {

    /**
     * 待提取
     */
    PENDING("PENDING", "待提取"),

    /**
     * 提取中
     */
    PROCESSING("PROCESSING", "提取中"),

    /**
     * 提取成功
     */
    SUCCESS("SUCCESS", "成功"),

    /**
     * 提取失败
     */
    FAILED("FAILED", "失败"),

    /**
     * 解析失败
     */
    PARSE_FAILED("PARSE_FAILED", "解析失败");

    /**
     * 状态代码
     */
    private final String code;

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 根据代码获取枚举
     */
    public static ExtractionStatus getByCode(String code) {
        for (ExtractionStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 是否为最终状态
     */
    public boolean isFinalStatus() {
        return this == SUCCESS || this == FAILED || this == PARSE_FAILED;
    }

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }

    /**
     * 是否失败
     */
    public boolean isFailed() {
        return this == FAILED || this == PARSE_FAILED;
    }

    /**
     * 是否为解析失败
     */
    public boolean isParseFailed() {
        return this == PARSE_FAILED;
    }
}
