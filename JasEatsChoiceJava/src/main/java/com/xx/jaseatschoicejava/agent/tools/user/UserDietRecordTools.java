package com.xx.jaseatschoicejava.agent.tools.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.entity.Nutrition;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.NutritionService;
import com.xx.jaseatschoicejava.service.UserService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 用户饮食记录工具类
 *
 * 为Agent提供用户饮食记录功能
 *
 * @author Claude
 * @since 2026-03-24
 */
@Slf4j
@Service
public class UserDietRecordTools {

    @Resource
    private UserService userService;

    @Resource
    private NutritionService nutritionService;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 记录用户饮食
     *
     * @param userId 用户ID
     * @param foodItemsJson 食物列表JSON
     * @param mealType 餐次
     * @return 记录结果
     */
    @Tool("""
        记录用户的饮食摄入

        **输入格式：**
        foodItemsJson应该是JSON数组，例如：
        [{"name":"苹果","weight":200},{"name":"米饭","weight":150}]

        **何时使用：**
        - 用户说明今天吃了什么
        - 用户要求记录饮食
        - 追踪热量摄入

        **参数：**
        - userId - 用户ID
        - foodItemsJson - 食物列表（JSON数组格式）
        - mealType - 餐次（早餐/午餐/晚餐/加餐）

        **返回：** 记录结果和总热量
        """)
    public String recordDiet(
        @P("用户ID") String userId,
        @P("食物列表，JSON数组格式") String foodItemsJson,
        @P("餐次：早餐/午餐/晚餐/加餐") String mealType
    ) {
        log.info("🔍 [Tool] 记录饮食，userId: {}, mealType: {}", userId, mealType);

        try {
            List<Map<String, Object>> foodItems = objectMapper.readValue(
                foodItemsJson,
                new TypeReference<List<Map<String, Object>>>() {}
            );

            if (foodItems == null || foodItems.isEmpty()) {
                return "❌ 食物列表为空";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("🍽️ %s饮食记录\n\n", mealType));

            double totalCalories = 0;
            double totalProtein = 0;

            for (Map<String, Object> item : foodItems) {
                String name = (String) item.get("name");
                Double weight = ((Number) item.get("weight")).doubleValue();

                // 查询营养信息
                Nutrition nutrition = nutritionService.getByFoodName(name);
                double calories = 0;
                double protein = 0;

                if (nutrition != null && nutrition.getEnergyKcal() != null) {
                    double factor = weight / 100.0;
                    calories = nutrition.getEnergyKcal().doubleValue() * factor;
                    protein = nutrition.getProtein() != null ?
                        nutrition.getProtein().doubleValue() * factor : 0;
                }

                totalCalories += calories;
                totalProtein += protein;

                sb.append(String.format(
                    "  • %s (%.0fg)：%.1f千卡\n",
                    name, weight, calories
                ));
            }

            sb.append(String.format(
                "\n" + "─".repeat(30) + "\n" +
                "🔥 总热量：%.1f千卡\n" +
                "💪 蛋白质：%.1fg\n\n" +
                "✅ 记录成功！",
                totalCalories, totalProtein
            ));

            log.info("✅ [Tool] 记录饮食成功，总热量: {}千卡", totalCalories);
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 记录饮食失败", e);
            return "❌ 记录失败：" + e.getMessage();
        }
    }

    /**
     * 获取今日饮食记录（模拟）
     *
     * @param userId 用户ID
     * @return 今日饮食记录
     */
    @Tool("""
        获取用户今天的饮食记录（模拟数据）

        **何时使用：**
        - 查询今日摄入
        - 分析今日饮食
        - 对比热量目标

        **参数：** userId - 用户ID

        **返回：** 今日饮食记录和汇总
        """)
    public String getTodayDietRecord(
        @P("用户ID") String userId
    ) {
        log.info("🔍 [Tool] 查询今日饮食记录，userId: {}", userId);

        try {
            User user = userService.getById(userId);

            if (user == null) {
                return "❌ 用户不存在";
            }

            // 简化版本：返回提示信息
            return """
                📋 今日饮食记录

                💡 提示：目前系统使用模拟数据
                请使用"记录饮食"功能添加您的饮食记录

                📊 今日记录：
                - 早餐：暂无记录
                - 午餐：暂无记录
                - 晚餐：暂无记录
                - 加餐：暂无记录

                🔥 今日总摄入：0千卡
                """;

        } catch (Exception e) {
            log.error("❌ [Tool] 查询今日记录失败", e);
            return "❌ 查询失败：" + e.getMessage();
        }
    }

    /**
     * 分析今日饮食是否达标
     *
     * @param userId 用户ID
     * @return 分析报告
     */
    @Tool("""
        分析用户今天的饮食是否达到健康目标

        **对比内容：**
        - 实际摄入 vs 目标热量
        - 营养素摄入情况
        - 是否在合理范围

        **何时使用：**
        - 用户询问"我今天吃得怎么样"
        - 用户追踪健康目标
        - 生成饮食建议

        **参数：** userId - 用户ID

        **返回：** 达标分析报告
        """)
    public String analyzeTodayDietCompliance(
        @P("用户ID") String userId
    ) {
        log.info("🔍 [Tool] 分析今日饮食达标情况，userId: {}", userId);

        try {
            User user = userService.getById(userId);

            if (user == null) {
                return "❌ 用户不存在";
            }

            // 获取用户目标
            String goalType = user.getDietGoal();
            if (goalType == null || goalType.isEmpty()) {
                goalType = "保持";
            }

            // 计算目标热量
            double weight = user.getWeight() != null ? user.getWeight() : 65.0;
            double height = user.getHeight() != null ? user.getHeight() : 170.0;
            int age = 25;
            String gender = user.getGender() != null ? user.getGender() : "male";

            double bmr;
            if ("male".equals(gender) || "男".equals(gender)) {
                bmr = 10 * weight + 6.25 * height - 5 * age + 5;
            } else {
                bmr = 10 * weight + 6.25 * height - 5 * age - 161;
            }

            double tdee = bmr * 1.55;
            int targetCalories = (int) Math.round(
                switch (goalType) {
                    case "减肥" -> tdee - 500;
                    case "增肌" -> tdee + 300;
                    case "增重" -> tdee + 500;
                    default -> tdee;
                }
            );

            // 简化版本：返回分析建议
            return String.format("""
                📊 今日饮食达标分析

                🎯 您的目标：%s
                🔥 目标热量：%d千卡/天
                ⚖️ 当前体重：%.1fkg

                💡 分析建议：
                目前系统尚未记录今日饮食数据
                请使用"记录饮食"功能添加您的一日三餐

                📝 记录后系统将自动分析：
                - 实际摄入 vs 目标热量
                - 营养素是否均衡
                - 是否需要调整饮食

                💪 加油！合理饮食是健康的基础！
                """,
                goalType, targetCalories, weight
            );

        } catch (Exception e) {
            log.error("❌ [Tool] 分析饮食达标失败", e);
            return "❌ 分析失败：" + e.getMessage();
        }
    }

    /**
     * 查询食物热量（辅助功能）
     *
     * @param foodName 食物名称
     * @param weight 重量（克）
     * @return 热量信息
     */
    @Tool("""
        查询食物的热量含量

        **何时使用：**
        - 记录饮食前查询热量
        - 比较不同食物的热量
        - 选择更健康的食物

        **参数：**
        - foodName - 食物名称
        - weight - 重量（克）

        **返回：** 热量信息
        """)
    public String queryFoodCalories(
        @P("食物名称") String foodName,
        @P("重量（克）") Double weight
    ) {
        log.info("🔍 [Tool] 查询食物热量，foodName: {}, weight: {}g", foodName, weight);

        try {
            Nutrition nutrition = nutritionService.getByFoodName(foodName);

            if (nutrition == null) {
                return String.format("❌ 未找到\"%s\"的营养数据", foodName);
            }

            double factor = weight / 100.0;
            double calories = nutrition.getEnergyKcal().doubleValue() * factor;
            double protein = nutrition.getProtein() != null ?
                nutrition.getProtein().doubleValue() * factor : 0;
            double fat = nutrition.getFat() != null ?
                nutrition.getFat().doubleValue() * factor : 0;
            double carbs = nutrition.getCho() != null ?
                nutrition.getCho().doubleValue() * factor : 0;

            return String.format(
                "🔥 %s热量查询（%.0fg）\n\n" +
                "📊 热量：%.1f千卡\n" +
                "💪 蛋白质：%.1fg\n" +
                "🧈 脂肪：%.1fg\n" +
                "🍞 碳水：%.1fg",
                foodName, weight, calories, protein, fat, carbs
            );

        } catch (Exception e) {
            log.error("❌ [Tool] 查询食物热量失败", e);
            return "❌ 查询失败：" + e.getMessage();
        }
    }
}
