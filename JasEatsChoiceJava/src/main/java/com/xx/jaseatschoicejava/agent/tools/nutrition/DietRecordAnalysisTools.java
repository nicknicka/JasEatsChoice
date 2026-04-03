package com.xx.jaseatschoicejava.agent.tools.nutrition;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xx.jaseatschoicejava.entity.CalorieRecord;
import com.xx.jaseatschoicejava.mapper.CalorieRecordMapper;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 饮食记录分析工具类
 *
 * 为Agent提供饮食记录分析功能
 *
 * @author Claude
 * @since 2026-03-27
 */
@Slf4j
@Service
public class DietRecordAnalysisTools {

    @Resource
    private CalorieRecordMapper calorieRecordMapper;

    /**
     * 查询用户今日营养摄入统计
     *
     * @param userId 用户ID
     * @return 今日营养摄入
     */
    @Tool("""
        查询用户今日的营养摄入统计

        **统计内容：**
        - 总热量摄入
        - 蛋白质、脂肪、碳水摄入
        - 营养素配比
        - 三餐分布
        - 与推荐值对比

        **何时使用：**
        - 用户询问"今天摄入了多少卡路里"
        - 评估每日饮食质量
        - 调整饮食计划

        **无需参数**，userId自动从上下文获取

        **返回：** 今日营养统计
        """)
    public String getTodayNutritionStats(
        AgenticScope scope
    ) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return "❌ 无法获取用户信息，请重新登录";
        }
        log.info("🔍 [Tool] 查询今日营养统计，userId: {}", userId);

        try {
            // 查询今日饮食记录
            QueryWrapper<CalorieRecord> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId)
                   .ge("record_time", LocalDateTime.of(LocalDate.now(), LocalTime.MIN))
                   .le("record_time", LocalDateTime.of(LocalDate.now(), LocalTime.MAX));

            List<CalorieRecord> records = calorieRecordMapper.selectList(wrapper);

            if (records == null || records.isEmpty()) {
                return "📊 今日营养摄入\n\n⚠️ 今日还没有饮食记录，建议记录每餐的摄入";
            }

            // 统计营养素
            double totalCalories = 0;
            double totalProtein = 0;
            double totalFat = 0;
            double totalCarbs = 0;
            int mealCount = records.size();

            for (CalorieRecord record : records) {
                totalCalories += record.getCalorie() != null ? record.getCalorie() : 0;
                totalProtein += record.getProtein() != null ? record.getProtein() : 0;
                totalFat += record.getFat() != null ? record.getFat() : 0;
                totalCarbs += record.getCarbohydrate() != null ? record.getCarbohydrate() : 0;
            }

            // 计算营养素配比
            double proteinCalories = totalProtein * 4;
            double fatCalories = totalFat * 9;
            double carbsCalories = totalCarbs * 4;
            double totalNutrientCalories = proteinCalories + fatCalories + carbsCalories;

            int proteinRatio = totalNutrientCalories > 0 ? (int) (proteinCalories / totalNutrientCalories * 100) : 0;
            int fatRatio = totalNutrientCalories > 0 ? (int) (fatCalories / totalNutrientCalories * 100) : 0;
            int carbsRatio = totalNutrientCalories > 0 ? (int) (carbsCalories / totalNutrientCalories * 100) : 0;

            // 构建结果
            StringBuilder sb = new StringBuilder();
            sb.append("📊 今日营养摄入统计\n\n");

            sb.append("🔥 **总热量**\n");
            sb.append(String.format("  • 今日摄入：%.0f kcal\n", totalCalories));
            sb.append(String.format("  • 推荐范围：1800-2200 kcal\n"));
            sb.append(String.format("  • 记录次数：%d餐\n\n", mealCount));

            sb.append("📈 **营养素详情**\n");
            sb.append(String.format("  • 蛋白质：%.1fg (%d%%)\n", totalProtein, proteinRatio));
            sb.append(String.format("  • 脂肪：%.1fg (%d%%)\n", totalFat, fatRatio));
            sb.append(String.format("  • 碳水化合物：%.1fg (%d%%)\n\n", totalCarbs, carbsRatio));

            sb.append("🎯 **营养素配比**\n");
            sb.append("  蛋白质：");
            int proteinBars = Math.max(0, Math.min(20, proteinRatio / 5));
            sb.append("■".repeat(proteinBars));
            sb.append(String.format(" %d%%\n", proteinRatio));
            sb.append("  脂肪：  ");
            sb.append("█".repeat(Math.max(0, Math.min(20, fatRatio / 5))));
            sb.append(String.format(" %d%%\n", fatRatio));
            sb.append("  碳水：  ");
            sb.append("█".repeat(Math.max(0, Math.min(20, carbsRatio / 5))));
            sb.append(String.format(" %d%%\n\n", carbsRatio));

            // 评估建议
            sb.append("💡 **评估与建议**\n");
            if (totalCalories < 1500) {
                sb.append("  ⚠️ 今日摄入热量偏低，建议适当加餐\n");
            } else if (totalCalories > 2500) {
                sb.append("  ⚠️ 今日摄入热量偏高，建议控制总量\n");
            } else {
                sb.append("  ✅ 热量摄入在合理范围内\n");
            }

            if (proteinRatio < 15) {
                sb.append("  • 蛋白质摄入偏低，建议增加肉类、蛋奶\n");
            } else if (proteinRatio > 35) {
                sb.append("  • 蛋白质摄入充足，注意肾脏负担\n");
            }

            if (fatRatio > 35) {
                sb.append("  • 脂肪摄入偏高，建议减少油炸食品\n");
            }

            log.info("✅ [Tool] 查询今日营养统计成功，热量: {}", String.format("%.0f", totalCalories));
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 查询今日营养统计失败", e);
            return "❌ 查询失败：" + e.getMessage();
        }
    }

    /**
     * 分析用户饮食习惯健康度
     *
     * @param userId 用户ID
     * @param days 分析天数
     * @return 饮食习惯分析
     */
    @Tool("""
        分析用户最近N天的饮食习惯健康度

        **分析维度：**
        - 热量摄入稳定性
        - 营养素均衡性
        - 用餐规律性
        - 健康评分

        **何时使用：**
        - 评估长期饮食质量
        - 发现饮食习惯问题
        - 制定改善计划

        **参数：**
        - days - 分析天数（1-30天）

        **无需参数**，userId自动从上下文获取

        **返回：** 饮食习惯分析报告
        """)
    public String analyzeDietHealth(
        AgenticScope scope,
        @P("分析天数（1-30天）") Integer days
    ) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return "❌ 无法获取用户信息，请重新登录";
        }
        log.info("🔍 [Tool] 分析饮食习惯健康度，userId: {}, days: {}", userId, days);

        try {
            if (days == null || days < 1 || days > 30) {
                days = 7; // 默认分析7天
            }

            // 查询最近N天的饮食记录
            LocalDateTime startDate = LocalDateTime.now().minusDays(days);
            QueryWrapper<CalorieRecord> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId)
                   .ge("record_time", startDate);

            List<CalorieRecord> records = calorieRecordMapper.selectList(wrapper);

            if (records == null || records.isEmpty()) {
                return String.format("📊 饮食习惯分析（最近%d天）\n\n⚠️ 暂无饮食记录", days);
            }

            // 计算平均热量
            double avgCalories = records.stream()
                .mapToDouble(r -> r.getCalorie() != null ? r.getCalorie() : 0)
                .average()
                .orElse(0);

            // 计算健康评分
            int score = calculateHealthScore(records, days);

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("📊 饮食习惯分析（最近%d天）\n\n", days));

            sb.append("📈 **统计信息**\n");
            sb.append(String.format("  • 记录次数：%d次\n", records.size()));
            sb.append(String.format("  • 平均热量：%.0f kcal/天\n\n", avgCalories));

            sb.append(String.format("🏆 **健康评分**：%d分\n\n", score));

            // 评分说明
            sb.append("📝 **评分详情**\n");
            if (score >= 90) {
                sb.append("  • 优秀：饮食非常健康，继续保持！\n");
            } else if (score >= 75) {
                sb.append("  • 良好：饮食较为健康，仍有优化空间\n");
            } else if (score >= 60) {
                sb.append("  • 一般：需要改善饮食习惯\n");
            } else {
                sb.append("  • 较差：建议重新规划饮食结构\n");
            }

            // 改善建议
            sb.append("\n💡 **改善建议**\n");

            if (avgCalories > 2500) {
                sb.append("  • 控制每日热量摄入，建议在1800-2200 kcal\n");
            } else if (avgCalories < 1500) {
                sb.append("  • 增加热量摄入，确保营养充足\n");
            }

            // 营养均衡性
            double totalProtein = records.stream().mapToDouble(r -> r.getProtein() != null ? r.getProtein() : 0).sum();
            double totalFat = records.stream().mapToDouble(r -> r.getFat() != null ? r.getFat() : 0).sum();
            double totalCarbs = records.stream().mapToDouble(r -> r.getCarbohydrate() != null ? r.getCarbohydrate() : 0).sum();

            double totalNutrients = totalProtein + totalFat + totalCarbs;
            if (totalNutrients > 0) {
                double proteinRatio = totalProtein / totalNutrients;
                if (proteinRatio < 0.15) {
                    sb.append("  • 增加蛋白质摄入（肉类、蛋奶、豆制品）\n");
                }

                if (totalFat / totalNutrients > 0.3) {
                    sb.append("  • 减少脂肪摄入，避免油炸食品\n");
                }
            }

            sb.append("\n🎯 **下阶段目标**\n");
            sb.append("  • 保持规律记录\n");
            sb.append("  • 均衡营养素配比\n");
            sb.append("  • 控制总热量摄入");

            log.info("✅ [Tool] 分析饮食习惯成功，评分: {}", score);
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 分析饮食习惯失败", e);
            return "❌ 分析失败：" + e.getMessage();
        }
    }

    /**
     * 生成营养改善建议
     *
     * @param userId 用户ID
     * @param goalType 目标类型
     * @return 营养改善建议
     */
    @Tool("""
        根据健康目标生成饮食改善建议

        **建议内容：**
        - 营养素摄入目标
        - 食物推荐
        - 饮食禁忌
        - 用餐建议

        **何时使用：**
        - 制定饮食计划
        - 调整饮食结构
        - 达成健康目标

        **参数：**
        - goalType - 目标类型（减肥/增肌/保持/增重）

        **无需参数**，userId自动从上下文获取

        **返回：** 饮食改善建议
        """)
    public String generateDietAdvice(
        AgenticScope scope,
        @P("目标类型：减肥/增肌/保持/增重") String goalType
    ) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return "❌ 无法获取用户信息，请重新登录";
        }
        log.info("🔍 [Tool] 生成饮食改善建议，userId: {}, goalType: {}", userId, goalType);

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("💊 营养改善建议\n\n");

            sb.append(String.format("🎯 **目标**：%s\n\n", goalType));

            switch (goalType) {
                case "减肥" -> {
                    sb.append("📊 **营养素目标**\n");
                    sb.append("  • 每日热量：1500-1800 kcal\n");
                    sb.append("  • 蛋白质：25-30%%（增加饱腹感）\n");
                    sb.append("  • 碳水化合物：40-45%%（选择复合碳水）\n");
                    sb.append("  • 脂肪：25-30%%（控制总量）\n\n");

                    sb.append("🥗 **推荐食物**\n");
                    sb.append("  • 高蛋白：鸡胸肉、鱼虾、豆腐、鸡蛋\n");
                    sb.append("  • 复合碳水：燕麦、糙米、红薯、玉米\n");
                    sb.append("  • 膳食纤维：西兰花、菠菜、黄瓜、番茄\n");
                    sb.append("  • 健康脂肪：牛油果、坚果（少量）\n\n");

                    sb.append("⚠️ **饮食禁忌**\n");
                    sb.append("  • 油炸食品、快餐\n");
                    sb.append("  • 甜食、含糖饮料\n");
                    sb.append("  • 精制碳水（白米饭、白面包）\n");
                    sb.append("  • 高盐食品（腌制、罐头）\n\n");

                    sb.append("💡 **用餐建议**\n");
                    sb.append("  • 早餐（7:00-8:00）：高蛋白+复合碳水\n");
                    sb.append("  • 午餐（12:00-13:00）：均衡搭配，七分饱\n");
                    sb.append("  • 晚餐（18:00-19:00）：清淡为主，五分饱\n");
                    sb.append("  • 避免夜宵，睡前3小时不进食");
                    break;
                }
                case "增肌" -> {
                    sb.append("📊 **营养素目标**\n");
                    sb.append("  • 每日热量：2500-2800 kcal\n");
                    sb.append("  • 蛋白质：1.6-2.0g/kg体重\n");
                    sb.append("  • 碳水化合物：50-55%%（提供训练能量）\n");
                    sb.append("  • 脂肪：20-25%%（维持激素水平）\n\n");

                    sb.append("💪 **推荐食物**\n");
                    sb.append("  • 蛋白质：鸡胸肉、牛肉、鱼虾、鸡蛋、蛋白粉\n");
                    sb.append("  • 碳水化合物：米饭、香蕉、燕麦、红薯\n");
                    sb.append("  • 健康脂肪：坚果、牛油果、橄榄油\n");
                    sb.append("  • 维生素：各种新鲜蔬菜和水果\n\n");

                    sb.append("⏰ **用餐时机**\n");
                    sb.append("  • 早餐：丰富蛋白质和碳水\n");
                    sb.append("  • 训练前1-2小时：复合碳水+适量蛋白\n");
                    sb.append("  • 训练后30分钟：快速吸收蛋白质（蛋白粉）\n");
                    sb.append("  • 睡前：酪蛋白或鸡蛋（缓释蛋白）\n\n");

                    sb.append("💊 **补充剂建议**\n");
                    sb.append("  • 乳清蛋白粉：训练后补充\n");
                    sb.append("  • 酪蛋白：睡前使用\n");
                    sb.append("  • 肌酸：提升训练表现\n");
                    sb.append("  • 复合维生素：补充微量元素");
                    break;
                }
                case "保持" -> {
                    sb.append("📊 **营养素目标**\n");
                    sb.append("  • 每日热量：2000-2200 kcal\n");
                    sb.append("  • 蛋白质：15-20%%\n");
                    sb.append("  • 碳水化合物：50-55%%\n");
                    sb.append("  • 脂肪：25-30%%\n\n");

                    sb.append("🍽️ **用餐建议**\n");
                    sb.append("  • 规律三餐，不暴饮暴食\n");
                    sb.append("  • 食物多样化，不挑食\n");
                    sb.append("  • 控制零食和含糖饮料\n");
                    sb.append("  • 适量运动，保持活力");
                    break;
                }
                case "增重" -> {
                    sb.append("📊 **营养素目标**\n");
                    sb.append("  • 每日热量：2500-3000 kcal\n");
                    sb.append("  • 蛋白质：20-25%%\n");
                    sb.append("  • 碳水化合物：55-60%%（增加热量）\n");
                    sb.append("  • 脂肪：25-30%%\n\n");

                    sb.append("🌟 **推荐食物**\n");
                    sb.append("  • 蛋白质：瘦肉、鸡蛋、牛奶、豆制品\n");
                    sb.append("  • 碳水化合物：米饭、面食、土豆、香蕉\n");
                    sb.append("  • 健康脂肪：坚果、牛油果、橄榄油\n");
                    sb.append("  • 营养密集型食物：牛肉、三文鱼、全脂奶\n\n");

                    sb.append("⏰ **用餐建议**\n");
                    sb.append("  • 增加进食次数：5-6餐/天\n");
                    sb.append("  • 每餐间隔：2-3小时\n");
                    sb.append("  • 睡前加餐：牛奶+坚果\n");
                    sb.append("  • 训练后及时补充");
                    break;
                }
            }

            sb.append("\n📝 **记录提醒**\n");
            sb.append("  • 每天记录饮食，追踪营养摄入\n");
            sb.append("  • 每周测量体重，监控进度\n");
            sb.append("  • 根据进度调整计划");

            log.info("✅ [Tool] 生成营养改善建议成功");
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 生成营养改善建议失败", e);
            return "❌ 生成失败：" + e.getMessage();
        }
    }

    /**
     * 计算健康评分
     */
    private int calculateHealthScore(List<CalorieRecord> records, int days) {
        int score = 100;

        // 记录频率（30分）
        int recordCount = records.size();
        int frequencyScore = Math.min(30, recordCount * 3);
        score = Math.min(score, frequencyScore + 70);

        // 热量合理性（40分）
        double avgCalories = records.stream()
            .mapToDouble(r -> r.getCalorie() != null ? r.getCalorie() : 0)
            .average()
            .orElse(0);

        if (avgCalories >= 1800 && avgCalories <= 2200) {
            score = Math.min(100, score + 40);
        } else if (avgCalories >= 1500 && avgCalories <= 2500) {
            score = Math.min(100, score + 25);
        } else {
            score = Math.min(100, score + 10);
        }

        // 营养均衡性（30分）
        double totalProtein = records.stream().mapToDouble(r -> r.getProtein() != null ? r.getProtein() : 0).sum();
        double totalFat = records.stream().mapToDouble(r -> r.getFat() != null ? r.getFat() : 0).sum();
        double totalCarbs = records.stream().mapToDouble(r -> r.getCarbohydrate() != null ? r.getCarbohydrate() : 0).sum();

        double totalNutrients = totalProtein + totalFat + totalCarbs;
        if (totalNutrients > 0) {
            double proteinRatio = totalProtein / totalNutrients;
            if (proteinRatio >= 0.15 && proteinRatio <= 0.35) {
                score = Math.min(100, score + 30);
            } else {
                score = Math.min(100, score + 15);
            }
        }

        return Math.max(0, score);
    }
}
