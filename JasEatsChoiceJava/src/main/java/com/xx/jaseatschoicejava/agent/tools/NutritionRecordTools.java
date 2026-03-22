package com.xx.jaseatschoicejava.agent.tools;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xx.jaseatschoicejava.entity.CalorieRecord;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.service.CalorieRecordService;
import com.xx.jaseatschoicejava.service.DishService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 营养记录工具集
 * 使用LangChain4j的@Tool注解声明工具函数
 *
 * @author Claude
 * @since 2026-03-22
 */
@Service
public class NutritionRecordTools {

    private static final Logger log = LoggerFactory.getLogger(NutritionRecordTools.class);

    @Resource
    private CalorieRecordService calorieRecordService;

    @Resource
    private DishService dishService;

    /**
     * 记录用户的用餐信息
     *
     * @param userId 用户ID
     * @param dishIds 菜品ID列表（逗号分隔）
     * @param mealType 用餐类型（早餐/午餐/晚餐/加餐）
     * @return 记录结果
     */
    @Tool("记录用户的用餐信息和营养摄入，支持早餐、午餐、晚餐、加餐等类型")
    public String recordMeal(@P("用户ID") String userId, @P("菜品ID列表（逗号分隔）") String dishIds, @P("用餐类型（早餐/午餐/晚餐/加餐）") String mealType) {
        log.info("执行工具：recordMeal，用户：{}，菜品：{}，类型：{}", userId, dishIds, mealType);

        try {
            // 解析菜品ID
            String[] dishIdArray = dishIds.split(",");
            List<Dish> dishes = new java.util.ArrayList<>();
            int totalCalories = 0;
            BigDecimal totalPrice = BigDecimal.ZERO;

            // 获取菜品信息并计算营养
            for (String dishId : dishIdArray) {
                Dish dish = dishService.getById(dishId.trim());
                if (dish != null) {
                    dishes.add(dish);

                    if (dish.getCalorie() != null) {
                        totalCalories += dish.getCalorie();
                    }

                    if (dish.getPrice() != null) {
                        totalPrice = totalPrice.add(dish.getPrice());
                    }
                }
            }

            if (dishes.isEmpty()) {
                return "❌ 未找到指定的菜品，请确认菜品ID是否正确。";
            }

            // 为每个菜品创建营养记录
            StringBuilder result = new StringBuilder();
            result.append("✅ **用餐记录成功**\n\n");
            result.append(String.format("**用餐类型：** %s\n", mealType));
            result.append(String.format("**菜品数量：** %d个\n\n", dishes.size()));

            result.append("**📊 营养摄入：**\n");
            result.append(String.format("🔥 总热量：%d kcal\n", totalCalories));
            result.append(String.format("💰 总价格：¥%.2f\n\n", totalPrice));

            result.append("**菜品清单：**\n");
            for (Dish dish : dishes) {
                result.append(String.format("- %s - ¥%.2f (%d kcal)\n",
                    dish.getName(), dish.getPrice(), dish.getCalorie()));

                // 创建营养记录
                CalorieRecord record = new CalorieRecord();
                record.setUserId(userId);
                record.setDishId(dish.getId());
                record.setCalorie(dish.getCalorie());
                record.setMealTime(mealType);
                record.setRecordTime(LocalDateTime.now());
                record.setFoodName(dish.getName());

                calorieRecordService.save(record);
            }

            result.append("\n💡 提示：您可以说「我今天吃了多少」查看今日总摄入。");

            return result.toString();

        } catch (Exception e) {
            log.error("记录用餐失败", e);
            return "记录用餐失败：" + e.getMessage();
        }
    }

    /**
     * 获取用户今日的营养摄入统计
     *
     * @param userId 用户ID
     * @return 今日营养摄入
     */
    @Tool("获取用户今日的营养摄入统计，包括热量、蛋白质、脂肪、碳水化合物等")
    public String getDailyNutrition(@P("用户ID") String userId) {
        log.info("执行工具：getDailyNutrition，用户：{}", userId);

        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

            QueryWrapper<CalorieRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .ge("record_time", startOfDay)
                    .lt("record_time", endOfDay)
                    .orderByDesc("record_time");

            List<CalorieRecord> records = calorieRecordService.list(queryWrapper);

            if (records == null || records.isEmpty()) {
                return "📊 **今日营养摄入**\n\n" +
                       "您今天还没有记录用餐哦~\n\n" +
                       "💡 可以说：「帮我记录一下午餐，宫保鸡丁和米饭」";
            }

            // 统计今日摄入
            int totalCalories = 0;
            java.util.Map<String, Integer> mealCalories = new java.util.HashMap<>();
            StringBuilder mealDetails = new StringBuilder();

            for (CalorieRecord record : records) {
                int calorie = record.getCalorie() != null ? record.getCalorie() : 0;
                totalCalories += calorie;

                String mealType = record.getMealTime() != null ? record.getMealTime() : "未知";
                mealCalories.put(mealType, mealCalories.getOrDefault(mealType, 0) + calorie);

                if (record.getFoodName() != null) {
                    mealDetails.append(String.format("- **%s**: %s (%d kcal)\n",
                        mealType, record.getFoodName(), calorie));
                }
            }

            StringBuilder result = new StringBuilder();
            result.append("📊 **今日营养摄入统计**\n\n");
            result.append(String.format("**📅 日期：** %s\n\n", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));

            result.append("**🍽️ 各餐热量统计：**\n");
            for (java.util.Map.Entry<String, Integer> entry : mealCalories.entrySet()) {
                result.append(String.format("- **%s：** %d kcal\n", entry.getKey(), entry.getValue()));
            }
            result.append("\n");

            result.append("**📊 总计摄入：**\n");
            result.append(String.format("🔥 **总热量：** %d kcal\n\n", totalCalories));

            result.append("**📝 用餐记录：**\n");
            result.append(mealDetails);

            result.append("\n💡 提示：您可以说「我的营养目标达标了吗」查看目标完成情况。");

            return result.toString();

        } catch (Exception e) {
            log.error("获取今日营养摄入失败", e);
            return "获取今日营养摄入失败：" + e.getMessage();
        }
    }

    /**
     * 获取用户营养目标的完成进度
     *
     * @param userId 用户ID
     * @return 目标进度
     */
    @Tool("获取用户营养目标的完成进度，对比实际摄入与目标值")
    public String getNutritionGoalProgress(@P("用户ID") String userId) {
        log.info("执行工具：getNutritionGoalProgress，用户：{}", userId);

        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

            QueryWrapper<CalorieRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .ge("record_time", startOfDay)
                    .lt("record_time", endOfDay);

            List<CalorieRecord> records = calorieRecordService.list(queryWrapper);

            // 获取营养目标（这里使用默认值，实际项目中应该从用户设置中获取）
            int calorieGoal = 2000; // 默认2000kcal

            // 计算今日摄入
            int totalCalories = 0;

            if (records != null && !records.isEmpty()) {
                for (CalorieRecord record : records) {
                    totalCalories += record.getCalorie() != null ? record.getCalorie() : 0;
                }
            }

            // 计算完成百分比
            BigDecimal caloriePercent = BigDecimal.valueOf(totalCalories)
                    .divide(BigDecimal.valueOf(calorieGoal), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            StringBuilder result = new StringBuilder();
            result.append("🎯 **今日营养目标完成进度**\n\n");
            result.append(String.format("**📅 日期：** %s\n\n", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));

            result.append("**🔥 热量目标：**\n");
            result.append(String.format("%d / %d kcal (%.0f%%) %s\n\n",
                totalCalories, calorieGoal, caloriePercent,
                getProgressEmoji(caloriePercent)));

            // 总体评估
            result.append("**📊 总体评估：**\n");
            if (caloriePercent.compareTo(BigDecimal.valueOf(95)) >= 0 &&
                caloriePercent.compareTo(BigDecimal.valueOf(105)) <= 0) {
                result.append("✅ 热量摄入达标，做得很好！\n\n");
            } else if (caloriePercent.compareTo(BigDecimal.valueOf(105)) > 0) {
                result.append("⚠️ 热量摄入超标，建议适当控制。\n\n");
            } else {
                result.append("💡 热量摄入不足，还可以再吃点。\n\n");
            }

            result.append("💡 提示：\n");
            result.append("- 您可以说「帮我记录晚餐」来补充摄入\n");
            result.append("- 您可以在个人中心设置您的营养目标");

            return result.toString();

        } catch (Exception e) {
            log.error("获取营养目标进度失败", e);
            return "获取营养目标进度失败：" + e.getMessage();
        }
    }

    /**
     * 获取营养进度表情符号
     *
     * @param percent 完成百分比
     * @return 表情符号
     */
    private String getProgressEmoji(BigDecimal percent) {
        if (percent.compareTo(BigDecimal.valueOf(100)) == 0) {
            return "✅";
        } else if (percent.compareTo(BigDecimal.valueOf(90)) >= 0) {
            return "🟢";
        } else if (percent.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return "🟡";
        } else if (percent.compareTo(BigDecimal.valueOf(50)) >= 0) {
            return "🟠";
        } else {
            return "🔴";
        }
    }

    /**
     * 获取营养摄入历史（最近7天）
     *
     * @param userId 用户ID
     * @return 历史记录
     */
    @Tool("获取用户最近7天的营养摄入历史，包括每日总热量和趋势")
    public String getNutritionHistory(@P("用户ID") String userId) {
        log.info("执行工具：getNutritionHistory，用户：{}", userId);

        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(6);
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

            QueryWrapper<CalorieRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .ge("record_time", startDateTime)
                    .lt("record_time", endDateTime)
                    .orderByDesc("record_time");

            List<CalorieRecord> records = calorieRecordService.list(queryWrapper);

            if (records == null || records.isEmpty()) {
                return "📊 **营养摄入历史**\n\n" +
                       "最近7天暂无营养记录。\n\n" +
                       "💡 开始记录每餐的饮食，我会帮您追踪营养摄入！";
            }

            // 按日期统计
            java.util.Map<LocalDate, Integer> dailyCalories = new java.util.HashMap<>();

            for (CalorieRecord record : records) {
                LocalDate date = record.getRecordTime().toLocalDate();
                int calories = record.getCalorie() != null ? record.getCalorie() : 0;

                dailyCalories.put(date, dailyCalories.getOrDefault(date, 0) + calories);
            }

            StringBuilder result = new StringBuilder();
            result.append("📊 **最近7天营养摄入历史**\n\n");

            int totalCalories = 0;
            for (int i = 6; i >= 0; i--) {
                LocalDate date = endDate.minusDays(i);
                int calories = dailyCalories.getOrDefault(date, 0);
                totalCalories += calories;

                String dateStr = date.format(DateTimeFormatter.ofPattern("MM-dd"));
                String dayOfWeek = switch (date.getDayOfWeek()) {
                    case MONDAY -> "周一";
                    case TUESDAY -> "周二";
                    case WEDNESDAY -> "周三";
                    case THURSDAY -> "周四";
                    case FRIDAY -> "周五";
                    case SATURDAY -> "周六";
                    case SUNDAY -> "周日";
                };

                result.append(String.format("**%s (%s):** %d kcal\n",
                    dateStr, dayOfWeek, calories));
            }

            result.append("\n**📈 统计：**\n");
            int avgCalories = totalCalories / 7;
            result.append(String.format("- 7天总摄入：%d kcal\n", totalCalories));
            result.append(String.format("- 日均摄入：%d kcal\n", avgCalories));

            return result.toString();

        } catch (Exception e) {
            log.error("获取营养历史失败", e);
            return "获取营养历史失败：" + e.getMessage();
        }
    }
}
