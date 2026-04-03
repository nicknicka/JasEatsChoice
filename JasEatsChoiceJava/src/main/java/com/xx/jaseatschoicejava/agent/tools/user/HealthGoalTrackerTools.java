package com.xx.jaseatschoicejava.agent.tools.user;

import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.UserService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agentic.scope.AgenticScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * 用户健康目标跟踪工具类
 *
 * 为Agent提供健康目标管理功能
 *
 * @author Claude
 * @since 2026-03-27
 */
@Slf4j
@Service
public class HealthGoalTrackerTools {

    @Resource
    private UserService userService;

    /**
     * 查询用户健康目标
     *
     * @param userId 用户ID
     * @return 健康目标信息
     */
    @Tool("""
        查询用户的健康目标和当前状态

        **返回内容：**
        - 健康目标类型（减肥/增肌/保持/增重）
        - 当前体重和BMI
        - 目标进度分析
        - 改进建议

        **何时使用：**
        - 用户询问"我的目标是什么"
        - 制定饮食计划时参考
        - 追踪健康改善情况

        **无需参数**，userId自动从上下文获取

        **返回：** 目标信息详情
        """)
    public String getHealthGoalStatus(AgenticScope scope) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return "❌ 无法获取用户信息，请重新登录";
        }
        log.info("🔍 [Tool] 查询健康目标状态，userId: {}", userId);

        try {
            User user = userService.getById(userId);

            if (user == null) {
                return "❌ 用户不存在";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("🎯 健康目标状态\n\n");

            // 目标类型
            String goal = user.getDietGoal() != null ? user.getDietGoal() : "未设置";
            sb.append(String.format("  • 目标类型：%s\n", goal));

            // 当前身体数据
            if (user.getHeight() != null && user.getWeight() != null) {
                double heightM = user.getHeight() / 100.0;
                double bmi = user.getWeight() / (heightM * heightM);

                sb.append(String.format("  • 当前身高：%.1f cm\n", user.getHeight()));
                sb.append(String.format("  • 当前体重：%.1f kg\n", user.getWeight()));
                sb.append(String.format("  • 当前BMI：%.1f\n\n", bmi));

                // BMI状态
                sb.append("📊 **BMI状态**\n");
                if (bmi < 18.5) {
                    sb.append("  • 偏瘦：建议适当增重\n");
                } else if (bmi < 24) {
                    sb.append("  • 正常：保持当前体重\n");
                } else if (bmi < 28) {
                    sb.append("  • 偏胖：建议适当减重\n");
                } else {
                    sb.append("  • 肥胖：建议制定减重计划\n");
                }
            } else {
                sb.append("\n⚠️ 请先完善身体数据（身高、体重）\n");
            }

            // 目标建议
            if ("减肥".equals(goal)) {
                sb.append("\n💡 **减肥建议**\n");
                sb.append("  • 控制每日热量摄入（1500-1800 kcal）\n");
                sb.append("  • 增加蛋白质摄入（25-30%%）\n");
                sb.append("  • 选择复合碳水（燕麦、糙米）\n");
                sb.append("  • 避免油炸食品和甜食");
            } else if ("增肌".equals(goal)) {
                sb.append("\n💡 **增肌建议**\n");
                sb.append("  • 增加热量摄入（2500-2800 kcal）\n");
                sb.append("  • 蛋白质1.6-2.0g/kg体重\n");
                sb.append("  • 训练后及时补充蛋白质\n");
                sb.append("  • 保证充足睡眠");
            } else if ("保持".equals(goal)) {
                sb.append("\n💡 **保持建议**\n");
                sb.append("  • 均衡饮食，规律三餐\n");
                sb.append("  • 控制总热量（2000-2200 kcal）\n");
                sb.append("  • 适量运动，保持活力");
            } else if ("增重".equals(goal)) {
                sb.append("\n💡 **增重建议**\n");
                sb.append("  • 增加热量摄入（2500-3000 kcal）\n");
                sb.append("  • 增加进食次数（5-6餐/天）\n");
                sb.append("  • 选择营养密集型食物");
            } else {
                sb.append("\n💡 请先设置健康目标（减肥/增肌/保持/增重）");
            }

            log.info("✅ [Tool] 查询健康目标状态成功");
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 查询健康目标状态失败", e);
            return "❌ 查询失败：" + e.getMessage();
        }
    }

    /**
     * 更新用户健康目标
     *
     * @param userId 用户ID
     * @param goalType 目标类型（减肥/增肌/保持/增重）
     * @return 更新结果
     */
    @Tool("""
        更新用户的健康目标

        **目标类型：**
        - 减肥：降低体重
        - 增肌：增加肌肉量
        - 保持：维持当前体重
        - 增重：健康增重

        **何时使用：**
        - 用户设置新的健康目标
        - 调整现有目标
        - 制定健康计划

        **参数：**
        - goalType - 目标类型（减肥/增肌/保持/增重）
        - userId自动从上下文获取

        **返回：** 更新结果和建议
        """)
    public String updateHealthGoal(
        AgenticScope scope,
        @P("目标类型：减肥/增肌/保持/增重") String goalType
    ) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return "❌ 无法获取用户信息，请重新登录";
        }
        log.info("🔍 [Tool] 更新健康目标，userId: {}, goalType: {}", userId, goalType);

        try {
            User user = userService.getById(userId);
            if (user == null) {
                return "❌ 用户不存在";
            }

            // 更新目标
            user.setDietGoal(goalType);
            userService.updateById(user);

            StringBuilder sb = new StringBuilder();
            sb.append("✅ 健康目标更新成功！\n\n");

            sb.append("🎯 **新目标**：").append(goalType).append("\n\n");

            // 根据目标提供建议
            switch (goalType) {
                case "减肥" -> {
                    sb.append("📋 **执行要点**\n");
                    sb.append("  • 每日热量目标：1500-1800 kcal\n");
                    sb.append("  • 蛋白质占比：25-30%%\n");
                    sb.append("  • 推荐食物：鸡胸肉、鱼虾、蔬菜\n");
                    sb.append("  • 避免食物：油炸、甜食、精制碳水\n\n");
                    sb.append("📅 **预期效果**\n");
                    sb.append("  • 每周减重：0.5-1 kg\n");
                    sb.append("  • 建议周期：8-12周");
                    break;
                }
                case "增肌" -> {
                    sb.append("📋 **执行要点**\n");
                    sb.append("  • 每日热量目标：2500-2800 kcal\n");
                    sb.append("  • 蛋白质：1.6-2.0g/kg体重\n");
                    sb.append("  • 推荐食物：牛肉、鸡蛋、蛋白粉\n");
                    sb.append("  • 训练后30分钟补充蛋白质\n\n");
                    sb.append("📅 **预期效果**\n");
                    sb.append("  • 每周增重：0.3-0.5 kg\n");
                    sb.append("  • 建议周期：12-16周");
                    break;
                }
                case "保持" -> {
                    sb.append("📋 **执行要点**\n");
                    sb.append("  • 每日热量目标：2000-2200 kcal\n");
                    sb.append("  • 营养均衡，不挑食\n");
                    sb.append("  • 规律三餐，控制零食\n");
                    sb.append("  • 适量运动，保持活力\n\n");
                    sb.append("📅 **维持策略**\n");
                    sb.append("  • 每周测量体重\n");
                    sb.append("  • 及时调整饮食");
                    break;
                }
                case "增重" -> {
                    sb.append("📋 **执行要点**\n");
                    sb.append("  • 每日热量目标：2500-3000 kcal\n");
                    sb.append("  • 增加进食次数：5-6餐/天\n");
                    sb.append("  • 推荐食物：瘦肉、米饭、坚果\n");
                    sb.append("  • 睡前可加餐\n\n");
                    sb.append("📅 **预期效果**\n");
                    sb.append("  • 每周增重：0.5 kg\n");
                    sb.append("  • 建议周期：8-12周");
                    break;
                }
            }

            log.info("✅ [Tool] 更新健康目标成功");
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 更新健康目标失败", e);
            return "❌ 更新失败：" + e.getMessage();
        }
    }

    /**
     * 生成个性化饮食建议
     *
     * @param userId 用户ID
     * @return 个性化建议
     */
    @Tool("""
        根据用户健康目标生成个性化饮食建议

        **建议内容：**
        - 每日热量目标
        - 营养素配比建议
        - 食物推荐
        - 饮食禁忌
        - 用餐建议

        **何时使用：**
        - 用户询问"我应该怎么吃"
        - 制定饮食计划
        - 调整饮食习惯

        **无需参数**，userId自动从上下文获取

        **返回：** 个性化饮食建议
        """)
    public String generatePersonalizedAdvice(AgenticScope scope) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return "❌ 无法获取用户信息，请重新登录";
        }
        log.info("🔍 [Tool] 生成个性化饮食建议，userId: {}", userId);

        try {
            User user = userService.getById(userId);
            if (user == null) {
                return "❌ 用户不存在";
            }

            String goalType = user.getDietGoal();
            if (goalType == null || goalType.isEmpty()) {
                return "⚠️ 请先设置健康目标（减肥/增肌/保持/增重）";
            }

            // 计算基础代谢
            int bmr = calculateBMR(user);
            int dailyCalories = calculateDailyCalories(bmr, goalType);

            StringBuilder sb = new StringBuilder();
            sb.append("💡 个性化饮食建议\n\n");

            sb.append("🔥 **每日热量目标**\n");
            sb.append(String.format("  • 基础代谢：%d kcal\n", bmr));
            sb.append(String.format("  • 每日目标：%d kcal\n\n", dailyCalories));

            sb.append("📊 **营养素配比**\n");
            switch (goalType) {
                case "减肥" -> {
                    sb.append("  • 蛋白质：30-35%%（增加饱腹感）\n");
                    sb.append("  • 碳水化合物：40-45%%（选择复合碳水）\n");
                    sb.append("  • 脂肪：25-30%%（控制总量）\n\n");
                    sb.append("🥗 **推荐食物**\n");
                    sb.append("  • 蛋白质：鸡胸肉、鱼虾、豆腐、鸡蛋\n");
                    sb.append("  • 碳水：燕麦、糙米、红薯、玉米\n");
                    sb.append("  • 蔬菜：西兰花、菠菜、黄瓜、番茄\n\n");
                    sb.append("⚠️ **饮食禁忌**\n");
                    sb.append("  • 油炸食品、快餐\n");
                    sb.append("  • 甜食、含糖饮料\n");
                    sb.append("  • 精制碳水（白米饭、白面包）");
                    break;
                }
                case "增肌" -> {
                    sb.append("  • 蛋白质：25-30%%（支持肌肉生长）\n");
                    sb.append("  • 碳水化合物：50-55%%（提供训练能量）\n");
                    sb.append("  • 脂肪：20-25%%（维持激素水平）\n\n");
                    sb.append("💪 **推荐食物**\n");
                    sb.append("  • 蛋白质：鸡胸肉、牛肉、鱼虾、鸡蛋、蛋白粉\n");
                    sb.append("  • 碳水：米饭、香蕉、燕麦、红薯\n");
                    sb.append("  • 脂肪：坚果、牛油果、橄榄油\n\n");
                    sb.append("⏰ **用餐时机**\n");
                    sb.append("  • 训练后30分钟内补充蛋白质\n");
                    sb.append("  • 保证三餐规律\n");
                    sb.append("  • 睡前可补充酪蛋白");
                    break;
                }
                case "保持" -> {
                    sb.append("  • 蛋白质：15-20%%\n");
                    sb.append("  • 碳水化合物：50-55%%\n");
                    sb.append("  • 脂肪：25-30%%\n\n");
                    sb.append("🍽️ **用餐建议**\n");
                    sb.append("  • 均衡饮食，不挑食\n");
                    sb.append("  • 规律三餐，不暴饮暴食\n");
                    sb.append("  • 控制零食和含糖饮料");
                    break;
                }
                case "增重" -> {
                    sb.append("  • 蛋白质：20-25%%\n");
                    sb.append("  • 碳水化合物：55-60%%（增加热量）\n");
                    sb.append("  • 脂肪：25-30%%\n\n");
                    sb.append("🌟 **推荐食物**\n");
                    sb.append("  • 蛋白质：瘦肉、鸡蛋、牛奶\n");
                    sb.append("  • 碳水：米饭、面食、土豆、香蕉\n");
                    sb.append("  • 脂肪：坚果、牛油果、橄榄油\n\n");
                    sb.append("⏰ **用餐建议**\n");
                    sb.append("  • 增加进食次数（5-6餐/天）\n");
                    sb.append("  • 训练后及时补充\n");
                    sb.append("  • 睡前可加餐");
                    break;
                }
            }

            // 三餐分配
            sb.append("\n🍴 **三餐热量分配**\n");
            sb.append(String.format("  • 早餐：%d kcal（20-25%%）\n", (int)(dailyCalories * 0.25)));
            sb.append(String.format("  • 午餐：%d kcal（35-40%%）\n", (int)(dailyCalories * 0.40)));
            sb.append(String.format("  • 晚餐：%d kcal（30-35%%）\n", (int)(dailyCalories * 0.35)));

            log.info("✅ [Tool] 生成个性化建议成功");
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 生成个性化建议失败", e);
            return "❌ 生成失败：" + e.getMessage();
        }
    }

    /**
     * 计算基础代谢率（BMR）
     * 使用Mifflin-St Jeor公式（简化版）
     */
    private int calculateBMR(User user) {
        if (user.getWeight() == null || user.getHeight() == null) {
            return 1500; // 默认值
        }

        double weight = user.getWeight();
        double height = user.getHeight();
        int age = 25; // 简化，使用默认年龄

        // 简化公式，假设男性
        double bmr = 10 * weight + 6.25 * height - 5 * age + 5;

        return (int) bmr;
    }

    /**
     * 计算每日热量目标
     */
    private int calculateDailyCalories(int bmr, String goalType) {
        // 活动系数（假设中等活动量）
        double activityFactor = 1.55;

        double tdee = bmr * activityFactor;

        // 根据目标调整
        return switch (goalType) {
            case "减肥" -> (int) (tdee - 500); // 热量缺口
            case "增肌" -> (int) (tdee + 300); // 热量盈余
            case "增重" -> (int) (tdee + 500); // 更大盈余
            default -> (int) tdee; // 保持
        };
    }
}
