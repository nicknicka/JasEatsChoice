package com.xx.jaseatschoicejava.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 拒绝原因枚举
 *
 * @author Claude
 * @since 2025-01-30
 */
@Getter
@AllArgsConstructor
public enum RejectionReason {

    /**
     * 食材季节性短缺
     */
    SEASONAL_UNAVAILABLE(1, "食材季节性短缺", "当前季节该食材无法获取"),

    /**
     * 食材供应链问题
     */
    SUPPLY_ISSUE(2, "食材供应链问题", "暂时无法采购到该食材"),

    /**
     * 制作工艺过于复杂
     */
    COMPLEX_PREPARATION(3, "制作工艺过于复杂", "该菜品制作工艺要求过高，暂时无法提供"),

    /**
     * 成本过高
     */
    HIGH_COST(4, "成本过高", "该菜品制作成本过高，无法定价"),

    /**
     * 与餐厅定位不符
     */
    NOT_MATCH_MENU(5, "与餐厅定位不符", "该菜品与本餐厅定位不符"),

    /**
     * 食品安全考虑
     */
    SAFETY_CONCERN(6, "食品安全考虑", "该菜品存在潜在食品安全风险"),

    /**
     * 原料品质不稳定
     */
    QUALITY_UNSTABLE(7, "原料品质不稳定", "该食材品质难以保证"),

    /**
     * 制作时间过长
     */
    TIME_CONSUMING(8, "制作时间过长", "该菜品制作时间过长，影响出餐效率"),

    /**
     * 特殊设备限制
     */
    EQUIPMENT_LIMIT(9, "特殊设备限制", "制作该菜品需要特殊设备，暂不具备"),

    /**
     * 其他原因
     */
    OTHER(99, "其他原因", "请在备注中说明具体原因");

    private final Integer code;
    private final String title;
    private final String description;

    /**
     * 根据code获取枚举
     */
    public static RejectionReason getByCode(Integer code) {
        if (code == null) {
            return OTHER;
        }
        for (RejectionReason reason : values()) {
            if (reason.getCode().equals(code)) {
                return reason;
            }
        }
        return OTHER;
    }
}
