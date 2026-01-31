package com.xx.jaseatschoicejava.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 备注优先级枚举
 *
 * @author Claude
 * @since 2025-01-30
 */
@Getter
@AllArgsConstructor
public enum RemarkPriority {

    /**
     * 高优先级 - 过敏食材（红色）
     */
    HIGH(1, "过敏食材", "red", 1),

    /**
     * 中高优先级 - 核心需求（黄色）
     */
    MEDIUM_HIGH(2, "核心需求", "orange", 2),

    /**
     * 中优先级 - 口味调整（蓝色）
     */
    MEDIUM(3, "口味调整", "blue", 3),

    /**
     * 低优先级 - 一般备注（灰色）
     */
    LOW(4, "一般备注", "gray", 4);

    /**
     * 优先级代码
     */
    private final Integer code;

    /**
     * 优先级名称
     */
    private final String name;

    /**
     * 显示颜色
     */
    private final String color;

    /**
     * 排序值
     */
    private final Integer sort;

    /**
     * 根据代码获取枚举
     */
    public static RemarkPriority getByCode(Integer code) {
        if (code == null) {
            return LOW;
        }
        for (RemarkPriority priority : values()) {
            if (priority.getCode().equals(code)) {
                return priority;
            }
        }
        return LOW;
    }
}
