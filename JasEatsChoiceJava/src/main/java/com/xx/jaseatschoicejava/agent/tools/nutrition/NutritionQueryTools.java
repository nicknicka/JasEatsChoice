package com.xx.jaseatschoicejava.agent.tools.nutrition;

import com.xx.jaseatschoicejava.agent.dto.NutritionInfo;
import com.xx.jaseatschoicejava.entity.Nutrition;
import com.xx.jaseatschoicejava.service.NutritionService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 营养查询工具类
 *
 * 为Agent提供食物营养成分查询功能
 *
 * @author Claude
 * @since 2026-03-24
 */
@Slf4j
@Service
public class NutritionQueryTools {

    @Resource
    private NutritionService nutritionService;

    /**
     * 查询食物营养成分
     *
     * @param foodName 食物名称
     * @return 营养成分信息
     */
    @Tool("""
        查询食物的营养成分信息（每100g可食部分）

        **返回信息：**
        - 食物名称
        - 热量（千卡）
        - 蛋白质（g）
        - 脂肪（g）
        - 碳水化合物（g）
        - 膳食纤维（g）
        - 胆固醇（mg）
        - 主要维生素和矿物质

        **何时使用：**
        - 用户询问食物营养
        - 需要营养数据做决策
        - 制定饮食计划

        **参数：** foodName - 食物名称（如'苹果'、'鸡蛋'、'米饭'）

        **返回：** 营养成分信息
        """)
    public NutritionInfo queryNutrition(
        @P("食物名称，如'苹果'、'鸡蛋'、'米饭'") String foodName
    ) {
        log.info("🔍 [Tool] 查询营养成分，foodName: {}", foodName);

        try {
            // 先尝试精确匹配
            Nutrition nutrition = nutritionService.getByFoodName(foodName);

            // 如果精确匹配失败，尝试模糊搜索
            if (nutrition == null) {
                List<Nutrition> results = nutritionService.searchByFoodName(foodName);
                if (results != null && !results.isEmpty()) {
                    nutrition = results.get(0);
                }
            }

            if (nutrition == null) {
                log.warn("❌ [Tool] 未找到营养数据，foodName: {}", foodName);
                return NutritionInfo.builder()
                        .foodName(foodName)
                        .found(false)
                        .build();
            }

            NutritionInfo info = NutritionInfo.builder()
                    .foodName(nutrition.getFoodName())
                    .foodCode(nutrition.getFoodCode())
                    .calories(nutrition.getEnergyKcal())
                    .protein(nutrition.getProtein())
                    .fat(nutrition.getFat())
                    .carbohydrates(nutrition.getCho())
                    .dietaryFiber(nutrition.getDietaryFiber())
                    .cholesterol(nutrition.getCholesterol())
                    .vitaminA(nutrition.getVitaminA())
                    .vitaminC(nutrition.getVitaminC())
                    .calcium(nutrition.getCa())
                    .iron(nutrition.getFe())
                    .zinc(nutrition.getZn())
                    .dataSource("中国食物成分表（第6版）")
                    .found(true)
                    .build();

            log.info("✅ [Tool] 查询营养成功: {} - {}千卡/100g",
                info.getFoodName(), info.getCalories());
            return info;

        } catch (Exception e) {
            log.error("❌ [Tool] 查询营养失败，foodName: {}", foodName, e);
            return NutritionInfo.builder()
                    .foodName(foodName)
                    .found(false)
                    .build();
        }
    }

    /**
     * 批量查询营养
     *
     * @param foodNames 食物名称列表
     * @return 营养信息列表
     */
    @Tool("""
        批量查询多个食物的营养成分

        **何时使用：**
        - 用户提到多个食物
        - 需要汇总营养信息
        - 对比不同食物的营养

        **参数：** foodNames - 食物名称列表

        **返回：** 营养信息列表
        """)
    public List<NutritionInfo> batchQueryNutrition(
        @P("食物名称列表") List<String> foodNames
    ) {
        log.info("🔍 [Tool] 批量查询营养成分，数量: {}", foodNames.size());

        try {
            List<NutritionInfo> results = new ArrayList<>();
            for (String foodName : foodNames) {
                NutritionInfo info = queryNutrition(foodName);
                if (info.getFound()) {
                    results.add(info);
                }
            }

            log.info("✅ [Tool] 批量查询成功，找到: {}/{}", results.size(), foodNames.size());
            return results;

        } catch (Exception e) {
            log.error("❌ [Tool] 批量查询营养失败", e);
            return List.of();
        }
    }

    /**
     * 搜索营养相似的食物
     *
     * @param foodName 参考食物
     * @param limit 返回数量
     * @return 相似食物列表
     */
    @Tool("""
        搜索与指定食物营养相似的其他食物

        相似标准：热量相差±20%以内

        **何时使用：**
        - 用户想要替代食物
        - 推荐相似营养的食物
        - 寻找同类食物

        **参数：**
        - foodName - 参考食物
        - limit - 返回数量限制

        **返回：** 相似食物列表
        """)
    public List<NutritionInfo> findSimilarNutrition(
        @P("参考食物") String foodName,
        @P("返回数量限制") int limit
    ) {
        log.info("🔍 [Tool] 搜索相似营养食物，foodName: {}, limit: {}", foodName, limit);

        try {
            // 获取参考食物的营养
            NutritionInfo reference = queryNutrition(foodName);
            if (!reference.getFound() || reference.getCalories() == null) {
                log.warn("❌ [Tool] 参考食物未找到或无热量数据");
                return List.of();
            }

            // 计算热量范围（±20%）
            double referenceCalories = reference.getCalories().doubleValue();
            double minCalories = referenceCalories * 0.8;
            double maxCalories = referenceCalories * 1.2;

            // 模糊搜索食物
            List<Nutrition> allResults = nutritionService.searchByFoodName("");
            List<NutritionInfo> similar = new ArrayList<>();

            for (Nutrition nutrition : allResults) {
                // 跳过自己
                if (nutrition.getFoodName().equals(foodName)) {
                    continue;
                }

                // 检查热量是否在范围内
                if (nutrition.getEnergyKcal() != null) {
                    double calories = nutrition.getEnergyKcal().doubleValue();
                    if (calories >= minCalories && calories <= maxCalories) {
                        similar.add(NutritionInfo.builder()
                                .foodName(nutrition.getFoodName())
                                .foodCode(nutrition.getFoodCode())
                                .calories(nutrition.getEnergyKcal())
                                .protein(nutrition.getProtein())
                                .fat(nutrition.getFat())
                                .carbohydrates(nutrition.getCho())
                                .found(true)
                                .build());
                    }
                }

                // 达到限制数量则停止
                if (similar.size() >= limit) {
                    break;
                }
            }

            log.info("✅ [Tool] 搜索相似食物成功，找到: {} 个", similar.size());
            return similar;

        } catch (Exception e) {
            log.error("❌ [Tool] 搜索相似营养食物失败", e);
            return List.of();
        }
    }

    /**
     * 比较两个食物的营养
     *
     * @param foodName1 食物1
     * @param foodName2 食物2
     * @return 比较结果
     */
    @Tool("""
        比较两个食物的营养成分差异

        **何时使用：**
        - 用户想要对比两个食物
        - 选择更健康的食物
        - 了解营养差异

        **参数：**
        - foodName1 - 食物1名称
        - foodName2 - 食物2名称

        **返回：** 营养比较结果
        """)
    public String compareNutrition(
        @P("食物1名称") String foodName1,
        @P("食物2名称") String foodName2
    ) {
        log.info("🔍 [Tool] 比较营养，{} vs {}", foodName1, foodName2);

        try {
            NutritionInfo info1 = queryNutrition(foodName1);
            NutritionInfo info2 = queryNutrition(foodName2);

            if (!info1.getFound() || !info2.getFound()) {
                return "❌ 无法完成比较，未找到食物营养数据";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("📊 营养对比分析\n\n");
            sb.append(String.format("**%s** vs **%s**\n\n", foodName1, foodName2));

            // 热量对比
            sb.append("🔥 **热量**\n");
            double cal1 = info1.getCalories().doubleValue();
            double cal2 = info2.getCalories().doubleValue();
            sb.append(String.format("- %s: %.1f 千卡/100g\n", foodName1, cal1));
            sb.append(String.format("- %s: %.1f 千卡/100g\n", foodName2, cal2));
            if (cal1 < cal2) {
                sb.append(String.format("→ %s 热量更低 %.1f%%\n",
                    foodName1, (cal2 - cal1) / cal2 * 100));
            } else {
                sb.append(String.format("→ %s 热量更低 %.1f%%\n",
                    foodName2, (cal1 - cal2) / cal1 * 100));
            }

            // 蛋白质对比
            sb.append("\n💪 **蛋白质**\n");
            double pro1 = info1.getProtein() != null ? info1.getProtein().doubleValue() : 0;
            double pro2 = info2.getProtein() != null ? info2.getProtein().doubleValue() : 0;
            sb.append(String.format("- %s: %.1fg/100g\n", foodName1, pro1));
            sb.append(String.format("- %s: %.1fg/100g\n", foodName2, pro2));
            if (pro1 > pro2) {
                sb.append(String.format("→ %s 蛋白质更高 %.1f%%\n",
                    foodName1, (pro1 - pro2) / (pro2 > 0 ? pro2 : 1) * 100));
            } else {
                sb.append(String.format("→ %s 蛋白质更高 %.1f%%\n",
                    foodName2, (pro2 - pro1) / (pro1 > 0 ? pro1 : 1) * 100));
            }

            // 脂肪对比
            sb.append("\n🧈 **脂肪**\n");
            double fat1 = info1.getFat() != null ? info1.getFat().doubleValue() : 0;
            double fat2 = info2.getFat() != null ? info2.getFat().doubleValue() : 0;
            sb.append(String.format("- %s: %.1fg/100g\n", foodName1, fat1));
            sb.append(String.format("- %s: %.1fg/100g\n", foodName2, fat2));
            if (fat1 < fat2) {
                sb.append(String.format("→ %s 脂肪更低\n", foodName1));
            } else {
                sb.append(String.format("→ %s 脂肪更低\n", foodName2));
            }

            log.info("✅ [Tool] 营养对比完成");
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 营养对比失败", e);
            return "❌ 营养对比失败：" + e.getMessage();
        }
    }
}
