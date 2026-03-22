package com.xx.jaseatschoicejava.agent.tools;

import com.xx.jaseatschoicejava.dto.NutritionInfo;
import com.xx.jaseatschoicejava.service.NutritionAnalysisService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 营养分析工具集
 * 使用LangChain4j的@Tool注解声明工具函数
 *
 * @author Claude
 * @since 2026-03-22
 */
@Service
public class NutritionTools {

    private static final Logger log = LoggerFactory.getLogger(NutritionTools.class);

    @Resource
    private NutritionAnalysisService nutritionAnalysisService;

    /**
     * 分析食物营养成分
     *
     * @param foodName 食物名称，例如"苹果"、"鸡蛋"
     * @return 营养信息，包括卡路里、蛋白质、脂肪等
     */
    @Tool("分析食物的营养成分，包括卡路里、蛋白质、脂肪、碳水化合物等")
    public NutritionInfo analyzeNutrition(@P("食物名称") String foodName) {
        log.info("执行工具：analyzeNutrition，参数：{}", foodName);

        try {
            NutritionInfo nutrition = nutritionAnalysisService.analyzeNutrition(foodName);
            log.info("营养分析完成：{}", nutrition);
            return nutrition;

        } catch (Exception e) {
            log.error("营养分析失败：{}", foodName, e);
            return NutritionInfo.builder()
                    .foodName(foodName)
                    .calories(java.math.BigDecimal.valueOf(0))
                    .dataSource("分析失败")
                    .build();
        }
    }

    /**
     * 批量分析多个食物的营养成分
     *
     * @param foodNames 食物名称列表，例如["苹果", "香蕉", "鸡蛋"]
     * @return 营养信息列表
     */
    @Tool("批量分析多个食物的营养成分")
    public String analyzeMultipleFoods(@P("食物名称列表") java.util.List<String> foodNames) {
        log.info("执行工具：analyzeMultipleFoods，参数：{}", foodNames);

        try {
            StringBuilder result = new StringBuilder();
            result.append("=== 营养分析报告 ===\n\n");

            for (String foodName : foodNames) {
                NutritionInfo nutrition = nutritionAnalysisService.analyzeNutrition(foodName);
                result.append(String.format("**%s**\n", nutrition.getFoodName()));
                result.append(String.format("- 卡路里：%.1f kcal\n", nutrition.getCalories()));
                result.append(String.format("- 蛋白质：%.1fg\n", nutrition.getProtein()));
                result.append(String.format("- 脂肪：%.1fg\n", nutrition.getFat()));
                result.append(String.format("- 碳水：%.1fg\n", nutrition.getCarbohydrates()));
                result.append(String.format("- 评级：%s\n", nutrition.getNutritionGrade()));
                result.append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("批量营养分析失败", e);
            return "分析失败：" + e.getMessage();
        }
    }

    /**
     * 计算每日建议卡路里摄入
     *
     * @param weight 体重（公斤）
     * @param height 身高（厘米）
     * @param age 年龄
     * @param gender 性别（男/女）
     * @param activityLevel 活动水平（久坐/轻度/中度/重度）
     * @return 每日建议卡路里
     */
    @Tool("计算每日建议卡路里摄入量")
    public String calculateDailyCalories(
            @P("体重（公斤）") Double weight,
            @P("身高（厘米）") Double height,
            @P("年龄") Integer age,
            @P("性别") String gender,
            @P("活动水平") String activityLevel
    ) {
        log.info("执行工具：calculateDailyCalories，参数：weight={}, height={}, age={}, gender={}, activityLevel={}",
                weight, height, age, gender, activityLevel);

        try {
            // 基础代谢率计算（Mifflin-St Jeor公式）
            double bmr;
            if ("男".equals(gender) || "male".equalsIgnoreCase(gender)) {
                bmr = 10 * weight + 6.25 * height - 5 * age + 5;
            } else {
                bmr = 10 * weight + 6.25 * height - 5 * age - 161;
            }

            // 活动系数
            double activityFactor = switch (activityLevel) {
                case "久坐", "sedentary" -> 1.2;
                case "轻度", "light" -> 1.375;
                case "中度", "moderate" -> 1.55;
                case "重度", "active" -> 1.725;
                default -> 1.2;
            };

            double tdee = bmr * activityFactor;

            return String.format("""
                    每日热量需求计算结果：
                    - 基础代谢率(BMR)：%.0f kcal
                    - 活动系数：%.2f
                    - 每日总消耗(TDEE)：%.0f kcal
                    - 建议摄入：%.0f - %.0f kcal

                    说明：TDEE是每日总能量消耗，建议摄入范围约为TDEE的90%-110%。
                    """,
                    bmr, activityFactor, tdee, tdee * 0.9, tdee * 1.1);

        } catch (Exception e) {
            log.error("计算卡路里失败", e);
            return "计算失败：" + e.getMessage();
        }
    }
}
