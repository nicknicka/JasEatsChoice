package com.xx.jaseatschoicejava.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 难度等级枚举
 *
 * @author Claude
 * @since 2025-01-31
 */
@Getter
@AllArgsConstructor
public enum DifficultyLevel {

    /**
     * 简单
     */
    EASY("EASY", "简单", 1),

    /**
     * 中等
     */
    MEDIUM("MEDIUM", "中等", 2),

    /**
     * 困难
     */
    HARD("HARD", "困难", 3);

    /**
     * 等级代码
     */
    private final String code;

    /**
     * 等级描述
     */
    private final String description;

    /**
     * 等级值（用于排序）
     */
    private final Integer level;

    /**
     * 根据代码获取枚举
     */
    public static DifficultyLevel getByCode(String code) {
        for (DifficultyLevel difficulty : values()) {
            if (difficulty.getCode().equals(code)) {
                return difficulty;
            }
        }
        return MEDIUM;
    }
}
