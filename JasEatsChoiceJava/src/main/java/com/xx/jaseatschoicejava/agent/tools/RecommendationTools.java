package com.xx.jaseatschoicejava.agent.tools;

import com.xx.jaseatschoicejava.dto.RecommendationRequestDTO;
import com.xx.jaseatschoicejava.dto.RecommendationResultDTO;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.RecommendationService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 推荐系统工具集
 * 使用LangChain4j的@Tool注解声明工具函数
 *
 * @author Claude
 * @since 2026-03-22
 */
@Service
public class RecommendationTools {

    private static final Logger log = LoggerFactory.getLogger(RecommendationTools.class);

    @Resource
    @Lazy  // 使用@Lazy打破循环依赖
    private RecommendationService recommendationService;

    @Resource
    private DishService dishService;

    /**
     * 获取今日推荐菜品
     *
     * @param userId 用户ID
     * @return 推荐菜品列表
     */
    @Tool("获取今日个性化推荐菜品，基于用户历史偏好和行为数据")
    public String getTodayRecommendations(@P("用户ID") String userId) {
        log.info("执行工具：getTodayRecommendations，用户：{}", userId);

        try {
            // 构建推荐请求
            RecommendationRequestDTO request = new RecommendationRequestDTO();
            request.setUserId(userId);
            request.setLimit(10);

            List<RecommendationResultDTO> recommendations = recommendationService.getRecommendations(request);

            if (recommendations == null || recommendations.isEmpty()) {
                return "暂时没有为您生成推荐，可以先浏览一下菜品哦~";
            }

            StringBuilder result = new StringBuilder();
            result.append("🍽️ **今日推荐**\n\n");

            for (int i = 0; i < Math.min(5, recommendations.size()); i++) {
                RecommendationResultDTO rec = recommendations.get(i);

                result.append(String.format("**%d. %s**\n", i + 1, rec.getDishName()));

                if (rec.getPrice() != null && rec.getCalories() != null) {
                    result.append(String.format("   💰 ¥%.2f | 🔥 %d kcal\n",
                            rec.getPrice(), rec.getCalories()));
                }

                if (rec.getReason() != null && rec.getReason().getPrimary() != null) {
                    result.append(String.format("   💡 %s\n", rec.getReason().getPrimary()));
                }
                result.append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("获取今日推荐失败", e);
            return "获取推荐失败：" + e.getMessage();
        }
    }

    /**
     * 根据卡路里限制推荐菜品
     *
     * @param maxCalories 最大卡路里
     * @param userId 用户ID
     * @return 推荐菜品列表
     */
    @Tool("根据卡路里限制推荐菜品，帮助用户控制热量摄入")
    public String recommendByCalories(@P("最大卡路里数") Integer maxCalories, @P("用户ID") String userId) {
        log.info("执行工具：recommendByCalories，最大卡路里：{}，用户：{}", maxCalories, userId);

        try {
            // 使用筛选功能
            List<Dish> dishes = recommendationService.filterRecommendDishes(
                    userId, null, null, maxCalories, null, null
            );

            if (dishes == null || dishes.isEmpty()) {
                return String.format("没有找到低于%d卡路里的菜品，建议适当放宽热量限制。", maxCalories);
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("🥗 **低卡推荐（<%d kcal）**\n\n", maxCalories));

            // 取前5个
            List<Dish> topDishes = dishes.stream()
                    .sorted((a, b) -> Integer.compare(a.getCalorie(), b.getCalorie()))
                    .limit(5)
                    .collect(Collectors.toList());

            for (int i = 0; i < topDishes.size(); i++) {
                Dish dish = topDishes.get(i);
                result.append(String.format("**%d. %s**\n", i + 1, dish.getName()));
                result.append(String.format("   🔥 %d kcal | 💰 ¥%.2f\n\n",
                        dish.getCalorie(), dish.getPrice()));
            }

            return result.toString();

        } catch (Exception e) {
            log.error("按卡路里推荐失败", e);
            return "推荐失败：" + e.getMessage();
        }
    }

    /**
     * 搜索菜品
     *
     * @param keyword 搜索关键词
     * @return 搜索结果
     */
    @Tool("搜索菜品，支持按菜名、分类等关键词搜索")
    public String searchDishes(@P("搜索关键词") String keyword) {
        log.info("执行工具：searchDishes，关键词：{}", keyword);

        try {
            List<Dish> dishes = dishService.list(); // 获取所有菜品，然后在内存中过滤

            // 简单的内存过滤
            List<Dish> filtered = dishes.stream()
                    .filter(d -> d.getName() != null && d.getName().contains(keyword))
                    .limit(8)
                    .collect(Collectors.toList());

            if (filtered.isEmpty()) {
                return String.format("没有找到与\"%s\"相关的菜品，换个关键词试试？", keyword);
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("🔍 **搜索结果：\"%s\"**\n\n", keyword));

            for (int i = 0; i < filtered.size(); i++) {
                Dish dish = filtered.get(i);
                result.append(String.format("**%d. %s**\n", i + 1, dish.getName()));
                result.append(String.format("   💰 ¥%.2f | 🔥 %d kcal\n",
                        dish.getPrice(), dish.getCalorie()));

                if (dish.getMerchantName() != null) {
                    result.append(String.format("   🏪 %s\n\n", dish.getMerchantName()));
                } else {
                    result.append("\n");
                }
            }

            return result.toString();

        } catch (Exception e) {
            log.error("搜索菜品失败", e);
            return "搜索失败：" + e.getMessage();
        }
    }

    /**
     * 获取热门菜品
     *
     * @return 热门菜品列表
     */
    @Tool("获取当前热门菜品，基于销量和用户评价")
    public String getPopularDishes() {
        log.info("执行工具：getPopularDishes");

        try {
            // 获取所有在线菜品，按评分排序
            List<Dish> dishes = dishService.list().stream()
                    .filter(d -> d.getIsOnline() != null && d.getIsOnline())
                    .filter(d -> d.getAvgRating() != null)
                    .sorted((a, b) -> b.getAvgRating().compareTo(a.getAvgRating()))
                    .limit(10)
                    .collect(Collectors.toList());

            if (dishes.isEmpty()) {
                return "暂时没有热门菜品数据";
            }

            StringBuilder result = new StringBuilder();
            result.append("🔥 **热门菜品**\n\n");

            for (int i = 0; i < Math.min(5, dishes.size()); i++) {
                Dish dish = dishes.get(i);
                result.append(String.format("**%d. %s**\n", i + 1, dish.getName()));
                result.append(String.format("   💰 ¥%.2f | 🔥 %d kcal",
                        dish.getPrice(), dish.getCalorie()));

                if (dish.getAvgRating() != null) {
                    result.append(String.format(" | ⭐ %.1f分", dish.getAvgRating()));
                }
                result.append("\n\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("获取热门菜品失败", e);
            return "获取热门菜品失败：" + e.getMessage();
        }
    }

    /**
     * 刷新推荐
     *
     * @param userId 用户ID
     * @return 新的推荐列表
     */
    @Tool("刷新推荐列表，获取新的推荐菜品")
    public String refreshRecommendations(@P("用户ID") String userId) {
        log.info("执行工具：refreshRecommendations，用户：{}", userId);

        try {
            List<RecommendationResultDTO> recommendations = recommendationService.refreshRecommendations(userId);

            if (recommendations == null || recommendations.isEmpty()) {
                return "已经为您刷新了推荐，暂时没有更多新推荐了~";
            }

            return "✅ 已为您刷新推荐！\n\n" + formatRecommendations(recommendations, 5);

        } catch (Exception e) {
            log.error("刷新推荐失败", e);
            return "刷新推荐失败：" + e.getMessage();
        }
    }

    /**
     * 推荐菜品组合
     *
     * @param budget 预算
     * @param peopleCount 人数
     * @param userId 用户ID
     * @return 推荐的菜品组合
     */
    @Tool("为多人推荐合理的菜品组合，平衡营养和口味")
    public String recommendCombination(@P("预算金额") Double budget, @P("用餐人数") Integer peopleCount, @P("用户ID") String userId) {
        log.info("执行工具：recommendCombination，预算：{}，人数：{}，用户：{}", budget, peopleCount, userId);

        try {
            // 计算人均预算
            double avgBudget = budget / peopleCount;

            // 构建推荐请求
            RecommendationRequestDTO request = new RecommendationRequestDTO();
            request.setUserId(userId);
            request.setLimit(peopleCount * 2); // 每人2道菜

            List<RecommendationResultDTO> recommendations = recommendationService.getRecommendations(request);

            if (recommendations == null || recommendations.isEmpty()) {
                return String.format(
                        "预算%.2f元（%.0f元/人）的推荐组合正在生成中，请稍后再试或调整预算~",
                        budget, avgBudget
                );
            }

            // 选择合适的菜品组合
            List<RecommendationResultDTO> selectedRecommendations = recommendations.stream()
                    .limit(peopleCount + 2) // 人数+2个菜
                    .collect(Collectors.toList());

            // 计算总价格和总卡路里
            BigDecimal totalPrice = selectedRecommendations.stream()
                    .map(RecommendationResultDTO::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int totalCalories = selectedRecommendations.stream()
                    .mapToInt(RecommendationResultDTO::getCalories)
                    .sum();

            StringBuilder result = new StringBuilder();
            result.append(String.format("🍱 **%d人套餐推荐**\n\n", peopleCount));
            result.append(String.format("💰 总价：¥%.2f（¥%.1f/人）\n", totalPrice, totalPrice.doubleValue() / peopleCount));
            result.append(String.format("🔥 总热量：%d kcal（%d kcal/人）\n\n",
                    totalCalories, totalCalories / peopleCount));

            result.append("**推荐菜品：**\n");
            for (int i = 0; i < selectedRecommendations.size(); i++) {
                RecommendationResultDTO rec = selectedRecommendations.get(i);
                result.append(String.format("%d. %s - ¥%.2f (%d kcal)\n",
                        i + 1, rec.getDishName(), rec.getPrice(), rec.getCalories()));
            }

            result.append(String.format("\n✅ 符合预算要求，营养均衡~"));

            return result.toString();

        } catch (Exception e) {
            log.error("推荐组合失败", e);
            return "推荐组合失败：" + e.getMessage();
        }
    }

    /**
     * 格式化推荐结果
     */
    private String formatRecommendations(List<RecommendationResultDTO> recommendations, int limit) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < Math.min(limit, recommendations.size()); i++) {
            RecommendationResultDTO rec = recommendations.get(i);

            result.append(String.format("**%d. %s**\n", i + 1, rec.getDishName()));

            if (rec.getPrice() != null && rec.getCalories() != null) {
                result.append(String.format("   💰 ¥%.2f | 🔥 %d kcal\n",
                        rec.getPrice(), rec.getCalories()));
            }

            if (rec.getReason() != null && rec.getReason().getPrimary() != null) {
                result.append(String.format("   💡 %s\n", rec.getReason().getPrimary()));
            }
            result.append("\n");
        }

        return result.toString();
    }
}
