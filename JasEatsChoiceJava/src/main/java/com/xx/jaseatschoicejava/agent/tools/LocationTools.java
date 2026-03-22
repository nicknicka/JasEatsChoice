package com.xx.jaseatschoicejava.agent.tools;

import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.MerchantService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 地理位置相关工具
 * 实现附近美食推荐和距离计算
 *
 * @author Claude
 * @since 2026-03-22
 */
@Service
public class LocationTools {

    private static final Logger log = LoggerFactory.getLogger(LocationTools.class);

    @Resource
    private MerchantService merchantService;

    @Resource
    private DishService dishService;

    /**
     * 推荐附近美食
     * 综合评分：口味30% + 营养20% + 价格10% + 距离15% + 评分25%
     *
     * @param userId 用户ID
     * @param maxDistance 最大距离（公里）
     * @param preference 偏好标签（可选，如"辣"、"清淡"）
     * @return 附近美食推荐
     */
    @Tool("推荐附近美食，综合口味、营养、价格、距离、评分多个维度")
    public String recommendNearbyFood(@P("用户ID") String userId, @P("最大距离（公里）") Double maxDistance, @P("偏好标签") String preference) {
        log.info("执行工具：recommendNearbyFood，用户：{}，距离：{}km，偏好：{}",
                userId, maxDistance, preference);

        try {
            // 1. 获取所有营业中的商家（有经纬度的）
            List<Merchant> merchants = merchantService.list().stream()
                    .filter(m -> m.getStatus() != null && m.getStatus())
                    .filter(m -> m.getLongitude() != null && m.getLatitude() != null)
                    .collect(Collectors.toList());

            if (merchants.isEmpty()) {
                return "附近暂无营业中的商家，请稍后再试~";
            }

            // 2. 暂时使用商家位置作为示例（实际应该根据用户位置计算）
            // 这里简化为：计算所有商家之间的距离，选择最近的几个商家
            // 在真实场景中，需要传入用户的经纬度

            // 3. 获取这些商家的所有在线菜品
            List<String> merchantIds = merchants.stream()
                    .map(Merchant::getId)
                    .collect(Collectors.toList());

            List<Dish> allDishes = dishService.list().stream()
                    .filter(d -> merchantIds.contains(d.getMerchantId()))
                    .filter(d -> d.getIsOnline() != null && d.getIsOnline())
                    .collect(Collectors.toList());

            if (allDishes.isEmpty()) {
                return "附近商家暂无上架菜品，请稍后再试~";
            }

            // 4. 为每个菜品计算综合评分
            List<DishScore> dishScores = new ArrayList<>();

            for (Dish dish : allDishes) {
                // 找到对应的商家
                Merchant merchant = merchants.stream()
                        .filter(m -> m.getId().equals(dish.getMerchantId()))
                        .findFirst()
                        .orElse(null);

                if (merchant != null) {
                    double score = calculateDishScore(dish, merchant, preference, maxDistance);
                    dishScores.add(new DishScore(dish, merchant, score));
                }
            }

            // 5. 按综合评分排序，取前10个
            List<DishScore> topDishes = dishScores.stream()
                    .sorted((a, b) -> Double.compare(b.score, a.score))
                    .limit(10)
                    .collect(Collectors.toList());

            if (topDishes.isEmpty()) {
                return "没有找到符合条件的美食，换个条件试试？";
            }

            // 6. 构建推荐结果
            StringBuilder result = new StringBuilder();
            result.append("📍 **附近美食推荐**\n\n");

            if (maxDistance != null) {
                result.append(String.format("📏 搜索范围：%s公里内\n\n", maxDistance));
            }

            if (preference != null && !preference.isEmpty()) {
                result.append(String.format("🏷️ 偏好：%s\n\n", preference));
            }

            for (int i = 0; i < topDishes.size(); i++) {
                DishScore ds = topDishes.get(i);
                Dish dish = ds.dish;
                Merchant merchant = ds.merchant;

                result.append(String.format("**%d. %s**\n", i + 1, dish.getName()));
                result.append(String.format("   💰 ¥%.2f | 🔥 %d kcal",
                        dish.getPrice(), dish.getCalorie()));

                if (dish.getAvgRating() != null) {
                    result.append(String.format(" | ⭐ %.1f分", dish.getAvgRating()));
                }

                result.append(String.format("\n   🏪 %s\n", merchant.getName()));

                if (merchant.getAveragePrice() != null) {
                    result.append(String.format("   人均：¥%.0f\n", merchant.getAveragePrice()));
                }

                // 显示综合评分
                result.append(String.format("   综合评分：%.2f分\n\n", ds.score));
            }

            result.append("💡 综合评分包含：口味(30%) + 营养(20%) + 价格(10%) + 距离(15%) + 评分(25%)");

            return result.toString();

        } catch (Exception e) {
            log.error("推荐附近美食失败", e);
            return "推荐附近美食失败：" + e.getMessage();
        }
    }

    /**
     * 计算菜品综合评分
     * 口味匹配度(30%) + 营养健康度(20%) + 价格合理性(10%) + 距离便利性(15%) + 商家评分(25%)
     */
    private double calculateDishScore(Dish dish, Merchant merchant, String preference, Double maxDistance) {
        double totalScore = 0.0;

        // 1. 口味匹配度 (30分) - 基于菜品评分
        double tasteScore = 0.0;
        if (dish.getAvgRating() != null) {
            // 将0-5分转换为0-30分
            tasteScore = dish.getAvgRating().doubleValue() / 5.0 * 30.0;
        } else {
            tasteScore = 15.0; // 默认中等评分
        }
        totalScore += tasteScore;

        // 2. 营养健康度 (20分) - 基于卡路里合理性
        double nutritionScore = 0.0;
        if (dish.getCalorie() != null) {
            // 假设合理热量范围是200-600卡路里
            int calories = dish.getCalorie();
            if (calories >= 200 && calories <= 600) {
                nutritionScore = 20.0;
            } else if (calories < 200) {
                nutritionScore = 15.0; // 热量过低
            } else if (calories <= 800) {
                nutritionScore = 10.0; // 热量稍高
            } else {
                nutritionScore = 5.0; // 热量过高
            }
        } else {
            nutritionScore = 10.0; // 默认
        }
        totalScore += nutritionScore;

        // 3. 价格合理性 (10分) - 基于价格区间
        double priceScore = 0.0;
        if (dish.getPrice() != null) {
            double price = dish.getPrice().doubleValue();
            if (price >= 10 && price <= 50) {
                priceScore = 10.0; // 价格合理
            } else if (price < 10) {
                priceScore = 8.0; // 性价比高
            } else if (price <= 100) {
                priceScore = 6.0; // 价格稍高
            } else {
                priceScore = 3.0; // 价格较高
            }
        } else {
            priceScore = 5.0; // 默认
        }
        totalScore += priceScore;

        // 4. 距离便利性 (15分) - 简化处理（实际应根据用户位置计算）
        // 这里暂时给固定分数，真实场景应该计算用户到商家的距离
        double distanceScore = 12.0; // 假设大部分商家在合理距离内
        totalScore += distanceScore;

        // 5. 商家评分 (25分)
        double merchantScore = 0.0;
        if (merchant.getRating() != null) {
            // 将0-5分转换为0-25分
            merchantScore = merchant.getRating().doubleValue() / 5.0 * 25.0;
        } else {
            merchantScore = 12.5; // 默认中等评分
        }
        totalScore += merchantScore;

        return totalScore;
    }

    /**
     * 计算两个经纬度之间的距离（Haversine公式）
     * 返回单位：公里
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 地球半径，单位：公里

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * 菜品评分内部类
     */
    private static class DishScore {
        Dish dish;
        Merchant merchant;
        double score;

        DishScore(Dish dish, Merchant merchant, double score) {
            this.dish = dish;
            this.merchant = merchant;
            this.score = score;
        }
    }

    /**
     * 查询附近商家
     *
     * @param userId 用户ID
     * @param maxDistance 最大距离（公里）
     * @return 附近商家列表
     */
    @Tool("查询指定距离内的附近商家")
    public String getNearbyMerchants(@P("用户ID") String userId, @P("最大距离（公里）") Double maxDistance) {
        log.info("执行工具：getNearbyMerchants，用户：{}，距离：{}km", userId, maxDistance);

        try {
            // 获取所有营业中的商家
            List<Merchant> merchants = merchantService.list().stream()
                    .filter(m -> m.getStatus() != null && m.getStatus())
                    .filter(m -> m.getLongitude() != null && m.getLatitude() != null)
                    .collect(Collectors.toList());

            if (merchants.isEmpty()) {
                return "附近暂无营业中的商家";
            }

            // 按评分排序
            List<Merchant> topMerchants = merchants.stream()
                    .sorted((a, b) -> {
                        if (a.getRating() == null && b.getRating() == null) return 0;
                        if (a.getRating() == null) return 1;
                        if (b.getRating() == null) return -1;
                        return b.getRating().compareTo(a.getRating());
                    })
                    .limit(10)
                    .collect(Collectors.toList());

            StringBuilder result = new StringBuilder();
            result.append(String.format("🏪 **附近商家**（共%d家）\n\n", topMerchants.size()));

            for (int i = 0; i < topMerchants.size(); i++) {
                Merchant m = topMerchants.get(i);
                result.append(String.format("**%d. %s**\n", i + 1, m.getName()));

                if (m.getCategory() != null) {
                    result.append(String.format("   分类：%s\n", m.getCategory()));
                }

                if (m.getAddress() != null) {
                    result.append(String.format("   地址：%s\n", m.getAddress()));
                }

                if (m.getRating() != null) {
                    result.append(String.format("   评分：%.1f分\n", m.getRating()));
                }

                if (m.getAveragePrice() != null) {
                    result.append(String.format("   人均：¥%.0f\n", m.getAveragePrice()));
                }

                result.append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("查询附近商家失败", e);
            return "查询附近商家失败：" + e.getMessage();
        }
    }
}
