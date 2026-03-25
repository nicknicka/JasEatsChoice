package com.xx.jaseatschoicejava.agent.tools.nutrition;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.service.NutritionService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 热量计算工具类
 *
 * 为Agent提供热量相关的计算功能
 *
 * @author Claude
 * @since 2026-03-24
 */
@Slf4j
@Service
public class CalorieCalculatorTools {

    @Resource
    private NutritionService nutritionService;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 计算基础代谢率(BMR)
     *
     * 使用Mifflin-St Jeor公式
     *
     * @param weight 体重
     * @param height 身高
     * @param age 年龄
     * @param gender 性别
     * @return BMR数值和说明
     */
    @Tool("""
        计算用户的基础代谢率(BMR)

        使用Mifflin-St Jeor公式（最准确）：
        - 男性：BMR = 10×体重kg + 6.25×身高cm - 5×年龄 + 5
        - 女性：BMR = 10×体重kg + 6.25×身高cm - 5×年龄 - 161

        **何时使用：**
        - 制定饮食计划
        - 计算热量目标
        - 评估基础代谢

        **参数：**
        - weight - 体重
        - height - 身高
        - age - 年龄
        - gender - 性别（男/女）

        **返回：** BMR数值和说明
        """)
    public String calculateBMR(
        @P("体重（千克）") double weight,
        @P("身高（厘米）") double height,
        @P("年龄") int age,
        @P("性别：男/女") String gender
    ) {
        log.info("🔍 [Tool] 计算BMR，体重: {}kg, 身高: {}cm, 年龄: {}, 性别: {}",
            weight, height, age, gender);

        try {
            double bmr;
            if ("男".equals(gender) || "male".equalsIgnoreCase(gender)) {
                // 男性公式
                bmr = 10 * weight + 6.25 * height - 5 * age + 5;
            } else if ("女".equals(gender) || "female".equalsIgnoreCase(gender)) {
                // 女性公式
                bmr = 10 * weight + 6.25 * height - 5 * age - 161;
            } else {
                return "❌ 性别参数错误，请提供'男'或'女'";
            }

            // 四舍五入到整数
            int bmrRounded = (int) Math.round(bmr);

            String result = String.format(
                "✅ 基础代谢率(BMR)计算结果：\n" +
                "📊 **%s**，%d岁，身高%.0fcm，体重%.1fkg\n\n" +
                "🔥 **基础代谢率：约 %d 千卡/天**\n\n" +
                "💡 这是你身体在完全静息状态下维持基本生理功能所需的最低热量。\n" +
                "📌 实际每日总消耗会根据活动水平增加。",
                gender, age, height, weight, bmrRounded
            );

            log.info("✅ [Tool] BMR计算完成: {} 千卡/天", bmrRounded);
            return result;

        } catch (Exception e) {
            log.error("❌ [Tool] BMR计算失败", e);
            return "❌ BMR计算失败：" + e.getMessage();
        }
    }

    /**
     * 计算每日总消耗(TDEE)
     *
     * @param bmr 基础代谢率
     * @param activityLevel 活动水平
     * @return TDEE数值
     */
    @Tool("""
        计算每日总能量消耗(TDEE)

        TDEE = BMR × 活动系数

        **活动系数标准：**
        - 久坐（几乎不运动）：1.2
        - 轻度活动（每周1-3天轻度运动）：1.375
        - 中度活动（每周3-5天中等运动）：1.55
        - 高度活动（每周6-7天运动）：1.725
        - 极高度活动（体力劳动或高强度训练）：1.9

        **何时使用：**
        - 制定饮食计划
        - 设置热量目标
        - 了解每日总消耗

        **参数：**
        - bmr - 基础代谢率
        - activityLevel - 活动水平（久坐/轻度/中度/高度/极高度）

        **返回：** TDEE数值和详细说明
        """)
    public String calculateTDEE(
        @P("基础代谢率（千卡/天）") double bmr,
        @P("活动水平：久坐/轻度/中度/高度/极高度") String activityLevel
    ) {
        log.info("🔍 [Tool] 计算TDEE，BMR: {}, 活动水平: {}", bmr, activityLevel);

        try {
            // 活动系数映射
            Map<String, Double> activityFactors = new HashMap<>();
            activityFactors.put("久坐", 1.2);
            activityFactors.put("轻度", 1.375);
            activityFactors.put("中度", 1.55);
            activityFactors.put("高度", 1.725);
            activityFactors.put("极高度", 1.9);

            Double factor = activityFactors.get(activityLevel);
            if (factor == null) {
                return "❌ 活动水平参数错误，请选择：久坐/轻度/中度/高度/极高度";
            }

            double tdee = bmr * factor;
            int tdeeRounded = (int) Math.round(tdee);

            String result = String.format(
                "✅ 每日总消耗(TDEE)计算结果：\n\n" +
                "📊 基础代谢率：%.0f 千卡/天\n" +
                "🏃 活动水平：%s（系数 %.2f）\n\n" +
                "🔥 **每日总消耗：约 %d 千卡/天**\n\n" +
                "💡 这是维持当前体重所需的热量摄入量。\n" +
                "📌 想要减肥可以减少300-500千卡，增肌可以增加300-500千卡。",
                bmr, activityLevel, factor, tdeeRounded
            );

            log.info("✅ [Tool] TDEE计算完成: {} 千卡/天", tdeeRounded);
            return result;

        } catch (Exception e) {
            log.error("❌ [Tool] TDEE计算失败", e);
            return "❌ TDEE计算失败：" + e.getMessage();
        }
    }

    /**
     * 计算多个食物的总热量
     *
     * @param foodItemsJson 食物列表JSON
     * @return 总热量和详细说明
     */
    @Tool("""
        计算多个食物的总热量

        **输入格式：** JSON数组，每项包含name（食物名称）和weight（克数）
        例如：[{"name":"苹果","weight":200},{"name":"鸡蛋","weight":50}]

        **何时使用：**
        - 用户提到吃了多个食物
        - 汇总一餐或一天的热量
        - 计算食谱总热量

        **参数：** foodItemsJson - 食物列表（JSON格式）

        **返回：** 总热量和详细说明
        """)
    public String calculateTotalCalories(
        @P("食物列表，JSON格式：[{\"name\":\"苹果\",\"weight\":100},...]") String foodItemsJson
    ) {
        log.info("🔍 [Tool] 计算食物总热量，JSON: {}", foodItemsJson);

        try {
            List<Map<String, Object>> foodItems = objectMapper.readValue(
                foodItemsJson,
                new TypeReference<List<Map<String, Object>>>() {}
            );

            if (foodItems == null || foodItems.isEmpty()) {
                return "❌ 食物列表为空";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("📊 热量计算结果\n\n");

            double totalCalories = 0;
            double totalProtein = 0;
            double totalFat = 0;
            double totalCarbs = 0;

            for (Map<String, Object> item : foodItems) {
                String name = (String) item.get("name");
                Double weight = ((Number) item.get("weight")).doubleValue();

                // 查询营养信息
                var nutrition = nutritionService.getByFoodName(name);
                if (nutrition == null) {
                    sb.append(String.format("⚠️ %s (%.0fg)：未找到营养数据\n", name, weight));
                    continue;
                }

                // 计算营养（按比例）
                double factor = weight / 100.0;
                double calories = nutrition.getEnergyKcal().doubleValue() * factor;
                double protein = nutrition.getProtein() != null ?
                    nutrition.getProtein().doubleValue() * factor : 0;
                double fat = nutrition.getFat() != null ?
                    nutrition.getFat().doubleValue() * factor : 0;
                double carbs = nutrition.getCho() != null ?
                    nutrition.getCho().doubleValue() * factor : 0;

                totalCalories += calories;
                totalProtein += protein;
                totalFat += fat;
                totalCarbs += carbs;

                sb.append(String.format("🔸 %s (%.0fg)：%.1f千卡\n",
                    name, weight, calories));
            }

            sb.append("\n" + "─".repeat(40) + "\n");
            sb.append(String.format("🔥 **总计：%.1f 千卡**\n\n", totalCalories));
            sb.append(String.format("💪 蛋白质：%.1fg\n", totalProtein));
            sb.append(String.format("🧈 脂肪：%.1fg\n", totalFat));
            sb.append(String.format("🍞 碳水化合物：%.1fg\n", totalCarbs));

            log.info("✅ [Tool] 热量计算完成: {} 千卡", totalCalories);
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 计算食物总热量失败", e);
            return "❌ 计算失败：" + e.getMessage();
        }
    }

    /**
     * 根据目标计算每日热量目标
     *
     * @param tdee 每日总消耗
     * @param goalType 目标类型
     * @return 每日热量目标和建议
     */
    @Tool("""
        根据用户的健康目标计算每日热量目标

        **目标热量调整：**
        - 减肥：TDEE - 500 千卡（每周减重约0.5kg）
        - 快速减肥：TDEE - 750 千卡（每周减重约0.75kg）
        - 增肌：TDEE + 300 千卡
        - 保持：TDEE
        - 增重：TDEE + 500 千卡

        **何时使用：**
        - 设置健康目标
        - 制定饮食计划
        - 调整热量摄入

        **参数：**
        - tdee - 每日总消耗（千卡/天）
        - goalType - 目标类型（减肥/快速减肥/增肌/保持/增重）

        **返回：** 每日热量目标和建议范围
        """)
    public String calculateCalorieGoal(
        @P("每日总消耗（千卡/天）") double tdee,
        @P("目标类型：减肥/快速减肥/增肌/保持/增重") String goalType
    ) {
        log.info("🔍 [Tool] 计算热量目标，TDEE: {}, 目标: {}", tdee, goalType);

        try {
            double goalCalories;
            String description;
            String advice;

            switch (goalType) {
                case "减肥":
                    goalCalories = tdee - 500;
                    description = "健康减肥";
                    advice = "每周预计减重约0.5kg，建议配合每周3-5次中等强度运动";
                    break;

                case "快速减肥":
                    goalCalories = tdee - 750;
                    description = "快速减肥";
                    advice = "每周预计减重约0.75kg，注意不要低于基础代谢率，建议在专业指导下进行";
                    break;

                case "增肌":
                    goalCalories = tdee + 300;
                    description = "增肌";
                    advice = "建议蛋白质摄入1.6-2.2g/kg体重，配合力量训练2-4次/周";
                    break;

                case "保持":
                    goalCalories = tdee;
                    description = "维持体重";
                    advice = "保持当前饮食和运动习惯，定期监测体重变化";
                    break;

                case "增重":
                    goalCalories = tdee + 500;
                    description = "健康增重";
                    advice = "每周预计增重约0.5kg，建议选择营养密集的食物";
                    break;

                default:
                    return "❌ 目标类型错误，请选择：减肥/快速减肥/增肌/保持/增重";
            }

            int goalRounded = (int) Math.round(goalCalories);
            int minGoal = goalRounded - 100;
            int maxGoal = goalRounded + 100;

            String result = String.format(
                "✅ 每日热量目标计算结果\n\n" +
                "🎯 目标：**%s**\n" +
                "📊 当前每日总消耗：%.0f 千卡/天\n\n" +
                "🔥 **建议每日热量摄入：%d 千卡/天**\n" +
                "📌 推荐范围：%d - %d 千卡/天\n\n" +
                "💡 建议：%s",
                description, tdee, goalRounded, minGoal, maxGoal, advice
            );

            log.info("✅ [Tool] 热量目标计算完成: {} 千卡/天", goalRounded);
            return result;

        } catch (Exception e) {
            log.error("❌ [Tool] 计算热量目标失败", e);
            return "❌ 计算失败：" + e.getMessage();
        }
    }

    /**
     * 计算BMI和健康体重范围
     *
     * @param weight 体重
     * @param height 身高
     * @return BMI和健康体重范围
     */
    @Tool("""
        计算BMI指数和健康体重范围

        **BMI标准（中国）：**
        - 偏瘦：< 18.5
        - 正常：18.5 - 23.9
        - 超重：24 - 27.9
        - 肥胖：≥ 28

        **何时使用：**
        - 评估当前体重状态
        - 设定目标体重
        - 健康管理

        **参数：**
        - weight - 体重
        - height - 身高

        **返回：** BMI值、体重状态和健康体重范围
        """)
    public String calculateBMI(
        @P("体重（千克）") double weight,
        @P("身高（厘米）") double height
    ) {
        log.info("🔍 [Tool] 计算BMI，体重: {}kg, 身高: {}cm", weight, height);

        try {
            // BMI = 体重 / 身高²
            double heightInMeters = height / 100.0;
            double bmi = weight / (heightInMeters * heightInMeters);

            String status;
            String advice;
            if (bmi < 18.5) {
                status = "偏瘦";
                advice = "建议适当增加营养摄入，可咨询营养师制定增重计划";
            } else if (bmi < 24) {
                status = "正常";
                advice = "体重状态良好，继续保持健康的生活方式";
            } else if (bmi < 28) {
                status = "超重";
                advice = "建议控制饮食并增加运动，可适当减少热量摄入";
            } else {
                status = "肥胖";
                advice = "建议咨询医生或营养师，制定科学的减重计划";
            }

            // 计算健康体重范围（BMI 18.5-23.9）
            double minWeight = 18.5 * heightInMeters * heightInMeters;
            double maxWeight = 23.9 * heightInMeters * heightInMeters;

            String result = String.format(
                "✅ BMI计算结果\n\n" +
                "📊 你的BMI指数：**%.1f**\n" +
                "🏷️ 体重状态：%s\n\n" +
                "💚 健康体重范围：%.1f - %.1f kg\n" +
                "📌 当前体重：%.1f kg\n\n" +
                "💡 建议：%s",
                bmi, status, minWeight, maxWeight, weight, advice
            );

            log.info("✅ [Tool] BMI计算完成: {}, 状态: {}", bmi, status);
            return result;

        } catch (Exception e) {
            log.error("❌ [Tool] BMI计算失败", e);
            return "❌ 计算失败：" + e.getMessage();
        }
    }
}
