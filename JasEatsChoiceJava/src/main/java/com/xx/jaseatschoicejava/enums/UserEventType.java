package com.xx.jaseatschoicejava.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户自定义事件类型枚举
 *
 * @author Claude
 * @since 2025-01-31
 */
@Getter
@AllArgsConstructor
public enum UserEventType {

    /**
     * 生日
     */
    BIRTHDAY("BIRTHDAY", "生日", "🎂"),

    /**
     * 纪念日
     */
    ANNIVERSARY("ANNIVERSARY", "纪念日", "💍"),

    /**
     * 聚会
     */
    PARTY("PARTY", "聚会", "🎉"),

    /**
     * 其他
     */
    OTHER("OTHER", "其他", "📅");

    /**
     * 类型代码
     */
    private final String code;

    /**
     * 类型描述
     */
    private final String description;

    /**
     * 类型图标
     */
    private final String icon;

    /**
     * 根据代码获取枚举
     */
    public static UserEventType getByCode(String code) {
        for (UserEventType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
