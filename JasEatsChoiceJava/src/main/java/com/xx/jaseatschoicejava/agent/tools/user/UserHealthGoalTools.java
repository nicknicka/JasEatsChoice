package com.xx.jaseatschoicejava.agent.tools.user;

import com.xx.jaseatschoicejava.agent.dto.UserHealthGoal;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.UserService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * 用户健康目标工具类
 *
 * 为Agent提供用户健康目标的设置和查询功能
 *
 * @author Claude
 * @since 2026-03-24
 */
@Slf4j
@Service
public class UserHealthGoalTools {

    @Resource
    private UserService userService;

    /**
     * 获取用户健康目标
     *
     * @param userId 用户ID
     * @return 健康目标信息
     */
    @Tool("""
        获取用户的健康目标，包括：
        - 目标类型（减肥、增肌、保持、增重）
        - 当前体重
        - 每日热量目标
        - 当前进度

        **何时使用：**
        - 制定饮食计划
        - 追踪健康进度
        - 营养建议

        **参数：** userId - 用户ID

        **返回：** 健康目标信息
        """)
    public UserHealthGoal getHealthGoal(
        @P("用户ID") String userId
    ) {
        log.info("🔍 [Tool] 查询用户健康目标，userId: {}", userId);

        try {
            User user = userService.getById(userId);

            if (user == null) {
                log.warn("❌ [Tool] 用户不存在，userId: {}", userId);
                return UserHealthGoal.builder()
                        .userId(userId)
                        .exists(false)
                        .build();
            }

            // 从dietGoal字段获取目标类型
            String goalType = user.getDietGoal();
            if (goalType == null || goalType.isEmpty()) {
                goalType = "保持";
            }

            UserHealthGoal goal = UserHealthGoal.builder()
                    .userId(userId)
                    .goalType(goalType)
                    .currentWeight(user.getWeight())
                    .dailyCalorieTarget(calculateCalorieTarget(user, goalType))
                    .progressPercentage(0.0)
                    .status("进行中")
                    .exists(true)
                    .build();

            log.info("✅ [Tool] 查询健康目标成功: {}", goal.getGoalType());
            return goal;

        } catch (Exception e) {
            log.error("❌ [Tool] 查询健康目标失败，userId: {}", userId, e);
            return UserHealthGoal.builder()
                    .userId(userId)
                    .exists(false)
                    .build();
        }
    }

    /**
     * 设置健康目标
     *
     * @param userId 用户ID
     * @param goalType 目标类型
     * @param targetWeight 目标体重（可选）
     * @param deadlineWeeks 目标期限（可选）
     * @return 设置结果
     */
    @Tool("""
        设置用户的健康目标

        **支持的目标类型：**
        - 减肥：减少体重，控制热量
        - 增肌：增加肌肉，提高蛋白质摄入
        - 保持：维持当前体重
        - 增重：健康增重

        **何时使用：**
        - 用户明确表示要减肥/增肌
        - 用户制定健康计划

        **参数：**
        - userId - 用户ID
        - goalType - 目标类型（减肥/增肌/保持/增重）
        - targetWeight - 目标体重（kg，可选）
        - deadlineWeeks - 目标期限（周，可选）

        **返回：** 设置结果和热量目标
        """)
    public String setHealthGoal(
        @P("用户ID") String userId,
        @P("目标类型：减肥/增肌/保持/增重") String goalType,
        @P("目标体重（kg，可选）") Double targetWeight,
        @P("目标期限（周，可选）") Integer deadlineWeeks
    ) {
        log.info("🔍 [Tool] 设置健康目标，userId: {}, goalType: {}", userId, goalType);

        try {
            User user = userService.getById(userId);

            if (user == null) {
                return "❌ 用户不存在";
            }

            // 更新用户的dietGoal
            user.setDietGoal(goalType);
            userService.updateById(user);

            // 计算热量目标
            int calorieTarget = calculateCalorieTarget(user, goalType);

            StringBuilder sb = new StringBuilder();
            sb.append("✅ 健康目标设置成功！\n\n");
            sb.append(String.format("🎯 目标类型：%s\n", goalType));
            sb.append(String.format("⚖️ 当前体重：%.1fkg\n", user.getWeight()));

            if (targetWeight != null) {
                double weightDiff = targetWeight - user.getWeight();
                sb.append(String.format("🏁 目标体重：%.1fkg\n", targetWeight));

                if (weightDiff < 0) {
                    sb.append(String.format("📉 需要减重：%.1fkg\n", -weightDiff));
                } else if (weightDiff > 0) {
                    sb.append(String.format("📈 需要增重：%.1fkg\n", weightDiff));
                }
            }

            if (deadlineWeeks != null && deadlineWeeks > 0) {
                sb.append(String.format("📅 目标期限：%d周\n", deadlineWeeks));
            }

            sb.append(String.format("\n🔥 每日热量目标：%d千卡/天\n", calorieTarget));
            sb.append("\n💡 建议配合运动和合理饮食，祝您早日达成目标！");

            log.info("✅ [Tool] 设置健康目标成功: {}", goalType);
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 设置健康目标失败，userId: {}", userId, e);
            return "❌ 设置失败：" + e.getMessage();
        }
    }

    /**
     * 计算每日热量目标
     *
     * @param userId 用户ID
     * @param goalType 目标类型
     * @return 热量目标
     */
    @Tool("""
        根据用户信息计算每日热量目标

        使用Mifflin-St Jeor公式计算：
        1. 基础代谢率(BMR)
        2. 每日总消耗(TDEE)
        3. 根据目标调整

        **热量调整：**
        - 减肥：TDEE - 500千卡
        - 增肌：TDEE + 300千卡
        - 保持：TDEE
        - 增重：TDEE + 500千卡

        **何时使用：**
        - 制定饮食计划
        - 设置健康目标

        **参数：**
        - userId - 用户ID
        - goalType - 目标类型（减肥/增肌/保持/增重）

        **返回：** 每日热量目标和建议
        """)
    public String calculateCalorieTarget(
        @P("用户ID") String userId,
        @P("目标类型：减肥/增肌/保持/增重") String goalType
    ) {
        log.info("🔍 [Tool] 计算热量目标，userId: {}, goalType: {}", userId, goalType);

        try {
            User user = userService.getById(userId);

            if (user == null) {
                return "❌ 用户不存在";
            }

            if (user.getWeight() == null || user.getHeight() == null) {
                return "❌ 用户信息不完整，需要身高体重数据";
            }

            // 计算BMR
            double weight = user.getWeight();
            double height = user.getHeight();
            int age = 25; // 默认年龄，实际应该从用户信息获取
            String gender = user.getGender() != null ? user.getGender() : "male";

            double bmr;
            if ("male".equals(gender) || "男".equals(gender)) {
                bmr = 10 * weight + 6.25 * height - 5 * age + 5;
            } else {
                bmr = 10 * weight + 6.25 * height - 5 * age - 161;
            }

            // 计算TDEE（假设中度活动）
            double tdee = bmr * 1.55;

            // 根据目标调整
            int calorieTarget;
            String advice;

            switch (goalType) {
                case "减肥":
                    calorieTarget = (int) Math.round(tdee - 500);
                    advice = "每周可减重约0.5kg，建议配合每周3-5次中等强度运动";
                    break;
                case "增肌":
                    calorieTarget = (int) Math.round(tdee + 300);
                    advice = "建议蛋白质摄入1.6-2.2g/kg体重，配合力量训练2-4次/周";
                    break;
                case "增重":
                    calorieTarget = (int) Math.round(tdee + 500);
                    advice = "建议选择营养密集的食物，少食多餐";
                    break;
                case "保持":
                default:
                    calorieTarget = (int) Math.round(tdee);
                    advice = "保持当前饮食和运动习惯，定期监测体重变化";
                    break;
            }

            String result = String.format(
                "🔥 热量目标计算结果\n\n" +
                "📊 基础代谢率(BMR)：%.0f千卡/天\n" +
                "🏃 每日总消耗(TDEE)：%.0f千卡/天\n" +
                "🎯 目标类型：%s\n\n" +
                "💡 **建议每日热量：%d千卡/天**\n\n" +
                "📝 建议：%s",
                bmr, tdee, goalType, calorieTarget, advice
            );

            log.info("✅ [Tool] 计算热量目标成功: {}千卡/天", calorieTarget);
            return result;

        } catch (Exception e) {
            log.error("❌ [Tool] 计算热量目标失败，userId: {}", userId, e);
            return "❌ 计算失败：" + e.getMessage();
        }
    }

    /**
     * 追踪健康目标进度
     *
     * @param userId 用户ID
     * @return 进度信息
     */
    @Tool("""
        追踪用户健康目标的进度

        **何时使用：**
        - 用户询问进度
        - 评估目标完成情况

        **参数：** userId - 用户ID

        **返回：** 目标进度信息
        """)
    public String trackGoalProgress(
        @P("用户ID") String userId
    ) {
        log.info("🔍 [Tool] 追踪健康目标进度，userId: {}", userId);

        try {
            User user = userService.getById(userId);

            if (user == null) {
                return "❌ 用户不存在";
            }

            String goalType = user.getDietGoal();
            if (goalType == null || goalType.isEmpty()) {
                return "📋 您还没有设置健康目标，可以使用'设置健康目标'功能来制定计划";
            }

            // 简化版本：返回目标状态
            String result = String.format(
                "🎯 健康目标进度\n\n" +
                "📋 目标类型：%s\n" +
                "⚖️ 当前体重：%.1fkg\n" +
                "📊 状态：进行中\n\n" +
                "💡 提示：定期记录饮食和体重数据，系统会自动计算进度",
                goalType,
                user.getWeight()
            );

            log.info("✅ [Tool] 追踪进度成功");
            return result;

        } catch (Exception e) {
            log.error("❌ [Tool] 追踪进度失败，userId: {}", userId, e);
            return "❌ 查询失败：" + e.getMessage();
        }
    }

    /**
     * 内部方法：计算热量目标
     */
    private int calculateCalorieTarget(User user, String goalType) {
        if (user.getWeight() == null || user.getHeight() == null) {
            return 2000; // 默认值
        }

        double weight = user.getWeight();
        double height = user.getHeight();
        int age = 25;
        String gender = user.getGender() != null ? user.getGender() : "male";

        // 计算BMR
        double bmr;
        if ("male".equals(gender) || "男".equals(gender)) {
            bmr = 10 * weight + 6.25 * height - 5 * age + 5;
        } else {
            bmr = 10 * weight + 6.25 * height - 5 * age - 161;
        }

        // 计算TDEE
        double tdee = bmr * 1.55;

        // 根据目标调整
        return switch (goalType) {
            case "减肥" -> (int) Math.round(tdee - 500);
            case "增肌" -> (int) Math.round(tdee + 300);
            case "增重" -> (int) Math.round(tdee + 500);
            default -> (int) Math.round(tdee);
        };
    }
}
