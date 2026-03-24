package com.xx.jaseatschoicejava.agent.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 营养信息DTO
 *
 * 用于Agent工具类返回食物营养信息
 *
 * @author Claude
 * @since 2026-03-24
 */
@Data
@Builder
public class NutritionInfo {

    /**
     * 食物名称
     */
    private String foodName;

    /**
     * 食物编码
     */
    private String foodCode;

    /**
     * 能量（千卡/100g）
     */
    private BigDecimal calories;

    /**
     * 蛋白质（g/100g）
     */
    private BigDecimal protein;

    /**
     * 脂肪（g/100g）
     */
    private BigDecimal fat;

    /**
     * 碳水化合物（g/100g）
     */
    private BigDecimal carbohydrates;

    /**
     * 膳食纤维（g/100g）
     */
    private BigDecimal dietaryFiber;

    /**
     * 胆固醇（mg/100g）
     */
    private BigDecimal cholesterol;

    /**
     * 维生素A（μgRE/100g）
     */
    private BigDecimal vitaminA;

    /**
     * 维生素C（mg/100g）
     */
    private BigDecimal vitaminC;

    /**
     * 钙（mg/100g）
     */
    private BigDecimal calcium;

    /**
     * 铁（mg/100g）
     */
    private BigDecimal iron;

    /**
     * 锌（mg/100g）
     */
    private BigDecimal zinc;

    /**
     * 数据来源
     */
    private String dataSource;

    /**
     * 是否找到数据
     */
    private Boolean found;
}
