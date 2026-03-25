package com.xx.jaseatschoicejava.agent.tools.recommendation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.service.DishService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Comparator;
import java.util.List;

/**
 * 推荐排序工具类
 *
 * 为Agent提供菜品推荐排序功能
 *
 * @author Claude
 * @since 2026-03-24
 */
@Slf4j
@Service
public class RecommendationRankTools {

    @Resource
    private DishService dishService;

    /**
     * 按评分排序菜品
     *
     * @param limit 返回数量
     * @param category 分类（可选）
     * @return 排序后的菜品列表
     */
    @Tool("""
        按用户评分从高到低排序菜品

        **排序依据：** 平均评分（avgRating）

        **何时使用：**
        - 寻找最受欢迎的菜品
        - 查看高评分推荐
        - 质量优先选择

        **参数：**
        - limit - 返回数量（默认10）
        - category - 分类（可选）

        **返回：** 按评分排序的菜品列表
        """)
    public String rankByRating(
        @P("返回数量（默认10）") Integer limit,
        @P("分类（可选）") String category
    ) {
        log.info("🔍 [Tool] 按评分排序，limit: {}, category: {}", limit, category);

        try {
            int actualLimit = limit != null && limit > 0 ? limit : 10;

            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<Dish>()
                .eq(Dish::getIsOnline, true)
                .isNotNull(Dish::getAvgRating)
                .orderByDesc(Dish::getAvgRating)
                .last("LIMIT " + actualLimit);

            if (category != null && !category.isEmpty()) {
                queryWrapper.eq(Dish::getCategory, category);
            }

            List<Dish> dishes = dishService.list(queryWrapper);

            if (dishes.isEmpty()) {
                return "📋 暂无评分数据";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("⭐ 按评分排序（Top %d）\n\n", dishes.size()));

            for (int i = 0; i < dishes.size(); i++) {
                Dish dish = dishes.get(i);
                sb.append(String.format(
                    "%d. **%s** ⭐ %.1f分\n" +
                    "   💰 %.2f元 | 🔥 %d千卡 | %s\n\n",
                    i + 1,
                    dish.getName(),
                    dish.getAvgRating() != null ? dish.getAvgRating() : 0,
                    dish.getPrice(),
                    dish.getCalorie() != null ? dish.getCalorie() : 0,
                    dish.getCategory()
                ));
            }

            log.info("✅ [Tool] 评分排序完成，数量: {}", dishes.size());
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 评分排序失败", e);
            return "❌ 排序失败：" + e.getMessage();
        }
    }

    /**
     * 按价格排序菜品
     *
     * @param limit 返回数量
     * @param order 排序方向（asc/desc）
     * @param category 分类（可选）
     * @return 排序后的菜品列表
     */
    @Tool("""
        按价格排序菜品

        **排序方向：**
        - asc：价格从低到高
        - desc：价格从高到低

        **何时使用：**
        - 寻找经济实惠的菜品
        - 按预算筛选
        - 性价比比较

        **参数：**
        - limit - 返回数量（默认10）
        - order - 排序方向（asc/desc，默认asc）
        - category - 分类（可选）

        **返回：** 按价格排序的菜品列表
        """)
    public String rankByPrice(
        @P("返回数量（默认10）") Integer limit,
        @P("排序方向：asc（从低到高）/desc（从高到低）") String order,
        @P("分类（可选）") String category
    ) {
        log.info("🔍 [Tool] 按价格排序，limit: {}, order: {}, category: {}", limit, order, category);

        try {
            int actualLimit = limit != null && limit > 0 ? limit : 10;
            boolean isAsc = !"desc".equalsIgnoreCase(order);

            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<Dish>()
                .eq(Dish::getIsOnline, true)
                .orderBy(true, isAsc, Dish::getPrice)
                .last("LIMIT " + actualLimit);

            if (category != null && !category.isEmpty()) {
                queryWrapper.eq(Dish::getCategory, category);
            }

            List<Dish> dishes = dishService.list(queryWrapper);

            if (dishes.isEmpty()) {
                return "📋 暂无菜品数据";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format(
                "💰 按价格排序（%s，Top %d）\n\n",
                isAsc ? "从低到高" : "从高到低",
                dishes.size()
            ));

            for (int i = 0; i < dishes.size(); i++) {
                Dish dish = dishes.get(i);
                sb.append(String.format(
                    "%d. **%s**\n" +
                    "   💰 %.2f元 | 🔥 %d千卡 | ⭐ %.1f分\n\n",
                    i + 1,
                    dish.getName(),
                    dish.getPrice(),
                    dish.getCalorie() != null ? dish.getCalorie() : 0,
                    dish.getAvgRating() != null ? dish.getAvgRating() : 0
                ));
            }

            log.info("✅ [Tool] 价格排序完成，数量: {}", dishes.size());
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 价格排序失败", e);
            return "❌ 排序失败：" + e.getMessage();
        }
    }

    /**
     * 按热量排序菜品
     *
     * @param limit 返回数量
     * @param order 排序方向（asc/desc）
     * @param category 分类（可选）
     * @return 排序后的菜品列表
     */
    @Tool("""
        按热量排序菜品

        **排序方向：**
        - asc：热量从低到高（适合减肥）
        - desc：热量从高到低（适合增重）

        **何时使用：**
        - 控制热量摄入
        - 减肥/增重需求
        - 热量对比

        **参数：**
        - limit - 返回数量（默认10）
        - order - 排序方向（asc/desc，默认asc）
        - category - 分类（可选）

        **返回：** 按热量排序的菜品列表
        """)
    public String rankByCalorie(
        @P("返回数量（默认10）") Integer limit,
        @P("排序方向：asc（从低到高）/desc（从高到低）") String order,
        @P("分类（可选）") String category
    ) {
        log.info("🔍 [Tool] 按热量排序，limit: {}, order: {}, category: {}", limit, order, category);

        try {
            int actualLimit = limit != null && limit > 0 ? limit : 10;
            boolean isAsc = !"desc".equalsIgnoreCase(order);

            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<Dish>()
                .eq(Dish::getIsOnline, true)
                .isNotNull(Dish::getCalorie)
                .orderBy(true, isAsc, Dish::getCalorie)
                .last("LIMIT " + actualLimit);

            if (category != null && !category.isEmpty()) {
                queryWrapper.eq(Dish::getCategory, category);
            }

            List<Dish> dishes = dishService.list(queryWrapper);

            if (dishes.isEmpty()) {
                return "📋 暂无热量数据";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format(
                "🔥 按热量排序（%s，Top %d）\n\n",
                isAsc ? "从低到高" : "从高到低",
                dishes.size()
            ));

            for (int i = 0; i < dishes.size(); i++) {
                Dish dish = dishes.get(i);
                sb.append(String.format(
                    "%d. **%s**\n" +
                    "   🔥 %d千卡/100g | 💰 %.2f元 | %s\n\n",
                    i + 1,
                    dish.getName(),
                    dish.getCalorie(),
                    dish.getPrice(),
                    dish.getCategory()
                ));
            }

            sb.append(String.format(
                "💡 %s",
                isAsc ?
                "这些菜品热量较低，适合控制热量摄入" :
                "这些菜品热量较高，适合需要补充能量的情况"
            ));

            log.info("✅ [Tool] 热量排序完成，数量: {}", dishes.size());
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 热量排序失败", e);
            return "❌ 排序失败：" + e.getMessage();
        }
    }

    /**
     * 按推荐度排序菜品
     *
     * @param limit 返回数量
     * @param category 分类（可选）
     * @return 排序后的菜品列表
     */
    @Tool("""
        按推荐度（综合得分）排序菜品

        **推荐度依据：**
        - 系统推荐算法得分
        - 评分、销量等多因素综合

        **何时使用：**
        - 获取系统推荐
        - 发现优质菜品
        - 智能推荐

        **参数：**
        - limit - 返回数量（默认10）
        - category - 分类（可选）

        **返回：** 按推荐度排序的菜品列表
        """)
    public String rankByRecommendationScore(
        @P("返回数量（默认10）") Integer limit,
        @P("分类（可选）") String category
    ) {
        log.info("🔍 [Tool] 按推荐度排序，limit: {}, category: {}", limit, category);

        try {
            int actualLimit = limit != null && limit > 0 ? limit : 10;

            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<Dish>()
                .eq(Dish::getIsOnline, true)
                .isNotNull(Dish::getScore)
                .orderByDesc(Dish::getScore)
                .last("LIMIT " + actualLimit);

            if (category != null && !category.isEmpty()) {
                queryWrapper.eq(Dish::getCategory, category);
            }

            List<Dish> dishes = dishService.list(queryWrapper);

            if (dishes.isEmpty()) {
                return "📋 暂无推荐数据";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("🌟 按推荐度排序（Top %d）\n\n", dishes.size()));

            for (int i = 0; i < dishes.size(); i++) {
                Dish dish = dishes.get(i);
                sb.append(String.format(
                    "%d. **%s**\n" +
                    "   🎯 推荐度: %.1f | 💰 %.2f元 | ⭐ %.1f分\n\n",
                    i + 1,
                    dish.getName(),
                    dish.getScore() != null ? dish.getScore() : 0,
                    dish.getPrice(),
                    dish.getAvgRating() != null ? dish.getAvgRating() : 0
                ));
            }

            sb.append("💡 这些菜品是系统综合推荐的高质量选择");

            log.info("✅ [Tool] 推荐度排序完成，数量: {}", dishes.size());
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 推荐度排序失败", e);
            return "❌ 排序失败：" + e.getMessage();
        }
    }

    /**
     * 按性价比排序菜品
     *
     * @param limit 返回数量
     * @param category 分类（可选）
     * @return 排序后的菜品列表
     */
    @Tool("""
        按性价比（评分/价格）排序菜品

        **性价比计算：** 评分 ÷ 价格

        **何时使用：**
        - 寻找高性价比菜品
        - 预算有限但想要好品质
        - 经济实惠选择

        **参数：**
        - limit - 返回数量（默认10）
        - category - 分类（可选）

        **返回：** 按性价比排序的菜品列表
        """)
    public String rankByValueForMoney(
        @P("返回数量（默认10）") Integer limit,
        @P("分类（可选）") String category
    ) {
        log.info("🔍 [Tool] 按性价比排序，limit: {}, category: {}", limit, category);

        try {
            int actualLimit = limit != null && limit > 0 ? limit : 10;

            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<Dish>()
                .eq(Dish::getIsOnline, true)
                .isNotNull(Dish::getAvgRating)
                .gt(Dish::getPrice, 0)
                .last("LIMIT 50");

            if (category != null && !category.isEmpty()) {
                queryWrapper.eq(Dish::getCategory, category);
            }

            List<Dish> dishes = dishService.list(queryWrapper);

            if (dishes.isEmpty()) {
                return "📋 暂无菜品数据";
            }

            // 计算性价比并排序
            dishes.sort((d1, d2) -> {
                double value1 = (d1.getAvgRating() != null ? d1.getAvgRating().doubleValue() : 0) / d1.getPrice().doubleValue();
                double value2 = (d2.getAvgRating() != null ? d2.getAvgRating().doubleValue() : 0) / d2.getPrice().doubleValue();
                return Double.compare(value2, value1); // 降序
            });

            // 限制返回数量
            if (dishes.size() > actualLimit) {
                dishes = dishes.subList(0, actualLimit);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("💎 按性价比排序（Top %d）\n\n", dishes.size()));

            for (int i = 0; i < dishes.size(); i++) {
                Dish dish = dishes.get(i);
                double value = (dish.getAvgRating() != null ? dish.getAvgRating().doubleValue() : 0) / dish.getPrice().doubleValue();
                sb.append(String.format(
                    "%d. **%s**\n" +
                    "   💰 %.2f元 | ⭐ %.1f分 | 性价比: %.2f\n\n",
                    i + 1,
                    dish.getName(),
                    dish.getPrice(),
                    dish.getAvgRating() != null ? dish.getAvgRating() : 0,
                    value
                ));
            }

            sb.append("💡 这些菜品物美价廉，性价比突出");

            log.info("✅ [Tool] 性价比排序完成，数量: {}", dishes.size());
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 性价比排序失败", e);
            return "❌ 排序失败：" + e.getMessage();
        }
    }

    /**
     * 综合排序（多维度）
     *
     * @param limit 返回数量
     * @param category 分类（可选）
     * @return 排序后的菜品列表
     */
    @Tool("""
        综合多维度排序菜品

        **排序维度：**
        - 评分权重：40%
        - 推荐度权重：30%
        - 性价比权重：30%

        **何时使用：**
        - 获取综合最优推荐
        - 平衡多因素选择
        - 全面评估菜品

        **参数：**
        - limit - 返回数量（默认10）
        - category - 分类（可选）

        **返回：** 综合排序的菜品列表
        """)
    public String comprehensiveRank(
        @P("返回数量（默认10）") Integer limit,
        @P("分类（可选）") String category
    ) {
        log.info("🔍 [Tool] 综合排序，limit: {}, category: {}", limit, category);

        try {
            int actualLimit = limit != null && limit > 0 ? limit : 10;

            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<Dish>()
                .eq(Dish::getIsOnline, true)
                .isNotNull(Dish::getAvgRating)
                .last("LIMIT 50");

            if (category != null && !category.isEmpty()) {
                queryWrapper.eq(Dish::getCategory, category);
            }

            List<Dish> dishes = dishService.list(queryWrapper);

            if (dishes.isEmpty()) {
                return "📋 暂无菜品数据";
            }

            // 计算综合得分并排序
            dishes.sort((d1, d2) -> {
                // 归一化评分（0-5）
                double rating1 = (d1.getAvgRating() != null ? d1.getAvgRating().doubleValue() : 0) / 5.0;
                double rating2 = (d2.getAvgRating() != null ? d2.getAvgRating().doubleValue() : 0) / 5.0;

                // 归一化推荐度（假设0-100）
                double score1 = (d1.getScore() != null ? d1.getScore().doubleValue() : 0) / 100.0;
                double score2 = (d2.getScore() != null ? d2.getScore().doubleValue() : 0) / 100.0;

                // 归一化性价比（假设合理范围0-10）
                double price1 = d1.getPrice().doubleValue();
                double price2 = d2.getPrice().doubleValue();
                double value1 = (rating1 / (price1 > 0 ? price1 : 1));
                double value2 = (rating2 / (price2 > 0 ? price2 : 1));
                double normValue1 = Math.min(value1 / 5.0, 1.0);
                double normValue2 = Math.min(value2 / 5.0, 1.0);

                // 综合得分
                double total1 = rating1 * 0.4 + score1 * 0.3 + normValue1 * 0.3;
                double total2 = rating2 * 0.4 + score2 * 0.3 + normValue2 * 0.3;

                return Double.compare(total2, total1); // 降序
            });

            // 限制返回数量
            if (dishes.size() > actualLimit) {
                dishes = dishes.subList(0, actualLimit);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("🏆 综合排序（Top %d）\n\n", dishes.size()));
            sb.append("📊 排序维度：评分40% + 推荐度30% + 性价比30%\n\n");

            for (int i = 0; i < dishes.size(); i++) {
                Dish dish = dishes.get(i);
                sb.append(String.format(
                    "%d. **%s**\n" +
                    "   ⭐ %.1f分 | 🎯 %.1f推荐度 | 💰 %.2f元\n\n",
                    i + 1,
                    dish.getName(),
                    dish.getAvgRating() != null ? dish.getAvgRating() : 0,
                    dish.getScore() != null ? dish.getScore() : 0,
                    dish.getPrice()
                ));
            }

            sb.append("💡 这些菜品在多个维度综合表现优秀");

            log.info("✅ [Tool] 综合排序完成，数量: {}", dishes.size());
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 综合排序失败", e);
            return "❌ 排序失败：" + e.getMessage();
        }
    }
}
