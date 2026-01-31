package com.xx.jaseatschoicejava.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 节日类型枚举
 *
 * @author Claude
 * @since 2025-01-31
 */
@Getter
@AllArgsConstructor
public enum FestivalType {

    /**
     * 传统节日（春节、中秋、端午等）
     */
    TRADITIONAL("TRADITIONAL", "传统节日"),

    /**
     * 西方节日（圣诞节、情人节等）
     */
    WESTERN("WESTERN", "西方节日"),

    /**
     * 季节性推荐
     */
    SEASONAL("SEASONAL", "季节性推荐"),

    /**
     * 用户自定义
     */
    CUSTOM("CUSTOM", "用户自定义");

    /**
     * 类型代码
     */
    private final String code;

    /**
     * 类型描述
     */
    private final String description;

    /**
     * 根据代码获取枚举
     */
    public static FestivalType getByCode(String code) {
        for (FestivalType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
