package com.xx.jaseatschoicejava.agent.tools.nutrition;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.entity.Nutrition;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.NutritionService;
import com.xx.jaseatschoicejava.service.UserService;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 营养分析工具类
 *
 * 为Agent提供营养分析功能
 *
 * @author Claude
 * @since 2026-03-24
 */
@Slf4j
@Service
public class NutritionAnalysisTools {

    @Resource
    private NutritionService nutritionService;

    @Resource
    private UserService userService;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 分析饮食营养均衡性
     *
     * @param foodItemsJson 食物列表JSON
     * @param userId 用户ID（可选）
     * @return 营养分析报告
     */
    @Tool("""
        分析饮食的营养均衡性

        **评估维度：**
        - 热量是否合理
        - 蛋白质是否充足
        - 脂肪占比是否合理
        - 碳水化合物是否适量

        **评分标准：**
        - 优秀：90-100分
        - 良好：80-89分
        - 中等：70-79分
        - 较差：60-69分
        - 很差：<60分

        **何时使用：**
        - 评估一餐的营养
        - 分析一天的饮食
        - 改进饮食建议

        **参数：**
        - foodItemsJson - 食物列表（JSON格式）

        **无需参数**，userId自动从上下文获取（用于对比目标）

        **返回：** 营养分析报告和评分
        """)
    public String analyzeNutritionBalance(
        @P("食物列表（JSON格式）") String foodItemsJson,
        AgenticScope scope
    ) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return "❌ 无法获取用户信息，请重新登录";
        }
        log.info("🔍 [Tool] 分析营养均衡性，userId: {}", userId);

        try {
            List<Map<String, Object>> foodItems = objectMapper.readValue(
                foodItemsJson,
                new TypeReference<List<Map<String, Object>>>() {}
            );

            if (foodItems == null || foodItems.isEmpty()) {
                return "❌ 食物列表为空";
            }

            // 计算总营养
            double totalCalories = 0;
            double totalProtein = 0;
            double totalFat = 0;
            double totalCarbs = 0;

            StringBuilder detail = new StringBuilder();
            detail.append("📊 营养成分分析\n\n");

            for (Map<String, Object> item : foodItems) {
                String name = (String) item.get("name");
                Double weight = ((Number) item.get("weight")).doubleValue();

                Nutrition nutrition = nutritionService.getByFoodName(name);
                if (nutrition != null) {
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

                    detail.append(String.format(
                        "• %s (%.0fg): %.1f千卡, 蛋白质%.1fg, 脂肪%.1fg, 碳水%.1fg\n",
                        name, weight, calories, protein, fat, carbs
                    ));
                }
            }

            // 计算营养占比
            double proteinPercent = totalCalories > 0 ? (totalProtein * 4 / totalCalories) * 100 : 0;
            double fatPercent = totalCalories > 0 ? (totalFat * 9 / totalCalories) * 100 : 0;
            double carbsPercent = totalCalories > 0 ? (totalCarbs * 4 / totalCalories) * 100 : 0;

            // 评分
            int score = 100;
            StringBuilder advice = new StringBuilder();

            // 蛋白质评估（理想15-20%）
            if (proteinPercent < 10) {
                score -= 15;
                advice.append("⚠️ 蛋白质不足，建议增加优质蛋白（蛋、奶、豆类、瘦肉）\n");
            } else if (proteinPercent > 30) {
                score -= 10;
                advice.append("⚠️ 蛋白质过高，可能增加肾脏负担\n");
            }

            // 脂肪评估（理想25-30%）
            if (fatPercent < 15) {
                score -= 10;
                advice.append("⚠️ 脂肪偏低，需要适量健康脂肪（坚果、橄榄油）\n");
            } else if (fatPercent > 40) {
                score -= 15;
                advice.append("⚠️ 脂肪过高，建议减少油炸食品\n");
            }

            // 碳水评估（理想45-60%）
            if (carbsPercent < 40) {
                score -= 10;
                advice.append("⚠️ 碳水偏低，建议增加全谷物、薯类\n");
            } else if (carbsPercent > 70) {
                score -= 15;
                advice.append("⚠️ 碳水过高，建议控制精制糖摄入\n");
            }

            // 评级
            String grade;
            if (score >= 90) grade = "优秀 🌟";
            else if (score >= 80) grade = "良好 👍";
            else if (score >= 70) grade = "中等 😐";
            else if (score >= 60) grade = "较差 😟";
            else grade = "很差 😞";

            String result = detail.toString() +
                "\n" + "─".repeat(40) + "\n" +
                String.format("总热量：%.1f千卡\n", totalCalories) +
                String.format("蛋白质：%.1fg (%.1f%%)\n", totalProtein, proteinPercent) +
                String.format("脂肪：%.1fg (%.1f%%)\n", totalFat, fatPercent) +
                String.format("碳水：%.1fg (%.1f%%)\n\n", totalCarbs, carbsPercent) +
                String.format("🎯 营养评分：%d分 - %s\n\n", score, grade) +
                "💡 改进建议：\n" +
                (advice.length() > 0 ? advice.toString() : "✅ 营养均衡，继续保持！");

            log.info("✅ [Tool] 营养分析完成，评分: {}分", score);
            return result;

        } catch (Exception e) {
            log.error("❌ [Tool] 营养分析失败", e);
            return "❌ 分析失败：" + e.getMessage();
        }
    }

    /**
     * 生成营养改进建议
     *
     * @param userId 用户ID
     * @return 改进建议
     */
    @Tool("""
        根据用户健康目标生成营养改进建议

        **建议包括：**
        - 热量控制建议
        - 营养素摄入建议
        - 食物选择建议
        - 饮食习惯建议

        **何时使用：**
        - 用户询问如何改善饮食
        - 制定饮食计划
        - 健康咨询

        **无需参数**，userId自动从上下文获取

        **返回：** 营养改进建议
        """)
    public String generateNutritionAdvice(
        AgenticScope scope
    ) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return "❌ 无法获取用户信息，请重新登录";
        }
        log.info("🔍 [Tool] 生成营养建议，userId: {}", userId);

        try {
            User user = userService.getById(userId);

            if (user == null) {
                return "❌ 用户不存在";
            }

            String goalType = user.getDietGoal();
            if (goalType == null || goalType.isEmpty()) {
                goalType = "保持";
            }

            StringBuilder advice = new StringBuilder();
            advice.append(String.format("📋 营养改进建议\n\n🎯 您的目标：%s\n\n", goalType));

            switch (goalType) {
                case "减肥":
                    advice.append("🔥 **热量控制**\n");
                    advice.append("• 每日热量控制在目标范围内\n");
                    advice.append("• 选择低热量、高纤维的食物\n");
                    advice.append("• 多吃蔬菜水果，增加饱腹感\n\n");

                    advice.append("💪 **蛋白质充足**\n");
                    advice.append("• 每日蛋白质1.2-1.5g/kg体重\n");
                    advice.append("• 优质蛋白：鸡蛋、鱼肉、鸡胸肉、豆制品\n\n");

                    advice.append("🥗 **饮食建议**\n");
                    advice.append("• 主食选全谷物（糙米、燕麦、全麦面包）\n");
                    advice.append("• 蔬菜水果每天500g以上\n");
                    advice.append("• 少油少盐，清淡饮食\n");
                    break;

                case "增肌":
                    advice.append("💪 **蛋白质优先**\n");
                    advice.append("• 每日蛋白质1.6-2.2g/kg体重\n");
                    advice.append("• 分配到每餐，确保持续供应\n\n");

                    advice.append("🔥 **热量盈余**\n");
                    advice.append("• 每日比消耗多300-500千卡\n");
                    advice.append("• 增加健康脂肪（坚果、牛油果）\n\n");

                    advice.append("🥗 **饮食建议**\n");
                    advice.append("• 训练后30分钟内补充蛋白质\n");
                    advice.append("• 复合碳水（香蕉、燕麦）提供能量\n");
                    advice.append("• 充足睡眠促进肌肉恢复\n");
                    break;

                case "增重":
                    advice.append("🔥 **增加热量**\n");
                    advice.append("• 每日比消耗多500千卡\n");
                    advice.append("• 增加进食频率（5-6餐/天）\n\n");

                    advice.append("🥗 **营养密集**\n");
                    advice.append("• 选择营养密度高的食物\n");
                    advice.append("• 牛奶、坚果、奶酪、鸡蛋\n");
                    advice.append("• 肉类、鱼类提供优质蛋白\n\n");

                    advice.append("💡 **建议**\n");
                    advice.append("• 少食多餐，避免单次过饱\n");
                    advice.append("• 配合力量训练增加肌肉\n");
                    break;

                default: // 保持
                    advice.append("⚖️ **均衡饮食**\n");
                    advice.append("• 三大营养素比例合理\n");
                    advice.append("• 蛋白质15-20%，脂肪25-30%，碳水45-60%\n\n");

                    advice.append("🥗 **多样化**\n");
                    advice.append("• 食物种类丰富，颜色多样\n");
                    advice.append("• 粗细搭配，荤素搭配\n");
                    advice.append("• 规律进餐，定时定量\n");
                    break;
            }

            advice.append("\n💡 **通用建议**\n");
            advice.append("• 充足饮水，每天1.5-2L\n");
            advice.append("• 规律作息，不熬夜\n");
            advice.append("• 适量运动，促进健康");

            log.info("✅ [Tool] 生成营养建议成功");
            return advice.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 生成营养建议失败", e);
            return "❌ 生成失败：" + e.getMessage();
        }
    }
}
