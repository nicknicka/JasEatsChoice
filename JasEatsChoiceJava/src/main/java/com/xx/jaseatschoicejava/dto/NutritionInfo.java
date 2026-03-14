package com.xx.jaseatschoicejava.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 营养信息数据传输对象
 * 用于返回食物的营养成分分析结果
 *
 * @author Claude
 * @since 2026-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionInfo {

    /**
     * 食物名称
     */
    private String foodName;

    /**
     * 份量（克）
     */
    private BigDecimal portion;

    /**
     * 热量（千卡）
     */
    private BigDecimal calories;

    /**
     * 蛋白质（克）
     */
    private BigDecimal protein;

    /**
     * 脂肪（克）
     */
    private BigDecimal fat;

    /**
     * 碳水化合物（克）
     */
    private BigDecimal carbohydrates;

    /**
     * 膳食纤维（克）
     */
    private BigDecimal dietaryFiber;

    /**
     * 钠（毫克）
     */
    private BigDecimal sodium;

    /**
     * 数据来源（database/api/ai）
     */
    private String dataSource;

    /**
     * 营养评级（A/B/C/D/E）
     */
    private String nutritionGrade;

    /**
     * 健康建议
     */
    private String healthAdvice;

    /**
     * 额外的营养信息（用于扩展）
     */
    private Map<String, BigDecimal> additionalNutrients;

    /**
     * 格式化为文本输出
     *
     * @return 格式化的营养信息文本
     */
    public String toFormattedText() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("《%s》的营养分析（%s克）：\n",
                foodName,
                portion != null ? portion : 100));

        if (calories != null) {
            sb.append(String.format("- 热量：%.1f kcal\n", calories));
        }
        if (protein != null) {
            sb.append(String.format("- 蛋白质：%.1fg\n", protein));
        }
        if (fat != null) {
            sb.append(String.format("- 脂肪：%.1fg\n", fat));
        }
        if (carbohydrates != null) {
            sb.append(String.format("- 碳水化合物：%.1fg\n", carbohydrates));
        }
        if (dietaryFiber != null && dietaryFiber.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("- 膳食纤维：%.1fg\n", dietaryFiber));
        }
        if (sodium != null && sodium.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("- 钠：%.1fmg\n", sodium));
        }

        if (nutritionGrade != null) {
            sb.append(String.format("- 营养评级：%s\n", nutritionGrade));
        }

        if (healthAdvice != null && !healthAdvice.isEmpty()) {
            sb.append("\n健康建议：").append(healthAdvice).append("\n");
        }

        return sb.toString();
    }

    /**
     * 计算总热量（基于蛋白质、脂肪、碳水化合物）
     * 蛋白质：4kcal/g，脂肪：9kcal/g，碳水化合物：4kcal/g
     *
     * @return 计算的总热量
     */
    public BigDecimal calculateTotalCalories() {
        BigDecimal total = BigDecimal.ZERO;

        if (protein != null) {
            total = total.add(protein.multiply(new BigDecimal("4")));
        }
        if (fat != null) {
            total = total.add(fat.multiply(new BigDecimal("9")));
        }
        if (carbohydrates != null) {
            total = total.add(carbohydrates.multiply(new BigDecimal("4")));
        }

        return total;
    }
}
