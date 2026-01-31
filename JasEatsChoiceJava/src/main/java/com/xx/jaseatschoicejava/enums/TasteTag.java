package com.xx.jaseatschoicejava.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 标准化口味标签枚举
 *
 * @author Claude
 * @since 2025-01-30
 */
@Getter
@AllArgsConstructor
public enum TasteTag {

    /**
     * 辣度标签
     */
    MILD_NO_SPICY("mild_no_spicy", "免辣", "spicy"),
    MILD_SPICY("mild_spicy", "微辣", "spicy"),
    MEDIUM_SPICY("medium_spicy", "中辣", "spicy"),
    HOT_SPICY("hot_spicy", "特辣", "spicy"),
    EXTRA_HOT_SPICY("extra_hot_spicy", "变态辣", "spicy"),

    /**
     * 甜度标签
     */
    NO_SUGAR("no_sugar", "免糖", "sweet"),
    LESS_SUGAR("less_sugar", "少糖", "sweet"),
    NORMAL_SUGAR("normal_sugar", "正常糖", "sweet"),
    EXTRA_SUGAR("extra_sugar", "多糖", "sweet"),

    /**
     * 盐度标签
     */
    NO_SALT("no_salt", "免盐", "salty"),
    LESS_SALT("less_salt", "少盐", "salty"),
    NORMAL_SALT("normal_salt", "正常盐", "salty"),

    /**
     * 口感标签
     */
    EXTRA_SOFT("extra_soft", "特软", "texture"),
    NORMAL_SOFT("normal_soft", "偏软", "texture"),
    NORMAL_TEXTURE("normal_texture", "正常口感", "texture"),
    EXTRA_HARD("extra_hard", "偏硬", "texture"),
    EXTRA_CRISPY("extra_crispy", "特脆", "texture"),

    /**
     * 温度标签
     */
    ICY_COLD("icy_cold", "冰镇", "temperature"),
    WARM("warm", "温热", "temperature"),
    HOT("hot", "烫", "temperature"),
    NO_HEAT("no_heat", "不要加热", "temperature"),

    /**
     * 其他常见标签
     */
    NO_ONION("no_onion", "不要葱", "ingredient"),
    NO_GINGER("no_ginger", "不要姜", "ingredient"),
    NO_GARLIC("no_garlic", "不要蒜", "ingredient"),
    NO_CORIANDER("no_coriander", "不要香菜", "ingredient"),
    NO_PEPPER("no_pepper", "不要胡椒", "ingredient"),
    NO_VINEGAR("no_vinegar", "不要醋", "ingredient"),
    NO_SESAME("no_sesame", "不要芝麻", "ingredient"),
    DRIED_CHILI_ON_SIDE("dried_chili_on_side", "干辣椒分装", "ingredient"),
    FRESH_CHILI_ON_SIDE("fresh_chili_on_side", "鲜辣椒分装", "ingredient"),
    SAUCE_ON_SIDE("sauce_on_side", "酱汁分装", "ingredient");

    /**
     * 标签代码
     */
    private final String code;

    /**
     * 标签名称
     */
    private final String name;

    /**
     * 标签类别
     */
    private final String category;

    /**
     * 根据代码获取枚举
     */
    public static TasteTag getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (TasteTag tag : values()) {
            if (tag.getCode().equals(code)) {
                return tag;
            }
        }
        return null;
    }

    /**
     * 根据名称获取枚举（模糊匹配）
     */
    public static TasteTag getByName(String name) {
        if (name == null) {
            return null;
        }
        for (TasteTag tag : values()) {
            if (tag.getName().contains(name) || name.contains(tag.getName())) {
                return tag;
            }
        }
        return null;
    }

    /**
     * 判断是否为辣度标签
     */
    public boolean isSpicyTag() {
        return "spicy".equals(this.category);
    }

    /**
     * 判断是否为食材排除标签
     */
    public boolean isIngredientExclusion() {
        return "ingredient".equals(this.category) &&
               (this == NO_ONION || this == NO_GINGER || this == NO_GARLIC ||
                this == NO_CORIANDER || this == NO_PEPPER || this == NO_VINEGAR ||
                this == NO_SESAME);
    }
}
