package com.xx.jaseatschoicejava.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 内容类型枚举
 *
 * @author Claude
 * @since 2025-01-31
 */
@Getter
@AllArgsConstructor
public enum ContentType {

    /**
     * 视频内容
     */
    VIDEO("VIDEO", "视频"),

    /**
     * 文章内容
     */
    ARTICLE("ARTICLE", "文章"),

    /**
     * 图片内容
     */
    IMAGE("IMAGE", "图片");

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
    public static ContentType getByCode(String code) {
        for (ContentType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
