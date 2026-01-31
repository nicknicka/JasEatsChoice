package com.xx.jaseatschoicejava.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 推荐类型枚举
 *
 * @author Claude
 * @since 2025-01-31
 */
@Getter
@AllArgsConstructor
public enum RecommendType {

    /**
     * 主推菜品
     */
    MAIN("MAIN", "主推", 1),

    /**
     * 次推菜品
     */
    SECONDARY("SECONDARY", "次推", 2),

    /**
     * 主题菜品
     */
    THEME("THEME", "主题", 3),

    /**
     * 季节菜品
     */
    SEASONAL("SEASONAL", "季节", 4);

    /**
     * 类型代码
     */
    private final String code;

    /**
     * 类型描述
     */
    private final String description;

    /**
     * 推荐级别（数字越大越靠前）
     */
    private final Integer level;

    /**
     * 根据代码获取枚举
     */
    public static RecommendType getByCode(String code) {
        for (RecommendType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
