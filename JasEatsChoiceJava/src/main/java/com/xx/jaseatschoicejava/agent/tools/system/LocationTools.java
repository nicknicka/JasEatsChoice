package com.xx.jaseatschoicejava.agent.tools.system;

import com.xx.jaseatschoicejava.agent.annotation.CardType;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.MerchantService;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 位置服务工具类
 *
 * 为Agent提供位置相关功能
 *
 * 注意：不使用 @Component/@Service 注解，通过 @Bean 方法手动创建，避免Spring AOP代理导致LangChain4j无法扫描@Tool注解
 *
 * @author Claude
 * @since 2026-03-24
 */
@Slf4j
public class LocationTools {

    @Resource
    private MerchantService merchantService;

    @Resource
    private DishService dishService;

    // 校园位置映射（示例数据）
    private static final Map<String, String> CAMPUS_LOCATIONS = new HashMap<>();

    static {
        CAMPUS_LOCATIONS.put("学生宿舍1栋", "宿舍区");
        CAMPUS_LOCATIONS.put("学生宿舍2栋", "宿舍区");
        CAMPUS_LOCATIONS.put("学生宿舍3栋", "宿舍区");
        CAMPUS_LOCATIONS.put("第一食堂", "食堂区");
        CAMPUS_LOCATIONS.put("第二食堂", "食堂区");
        CAMPUS_LOCATIONS.put("第三食堂", "食堂区");
        CAMPUS_LOCATIONS.put("图书馆", "教学区");
        CAMPUS_LOCATIONS.put("教学楼A", "教学区");
        CAMPUS_LOCATIONS.put("教学楼B", "教学区");
        CAMPUS_LOCATIONS.put("体育馆", "运动区");
        CAMPUS_LOCATIONS.put("操场", "运动区");
    }

    /**
     * 获取校园位置信息
     *
     * @param locationName 位置名称
     * @return 位置信息
     */
    @Tool("""
        获取校园内位置的信息

        **支持的位置：**
        - 宿舍区：学生宿舍1-3栋
        - 食堂区：第一、二、三食堂
        - 教学区：图书馆、教学楼A/B
        - 运动区：体育馆、操场

        **何时使用：**
        - 用户询问位置
        - 估算配送距离

        **参数：** locationName - 位置名称

        **返回：** 位置信息
        """)
    public String getLocationInfo(
        @P("位置名称，如：学生宿舍1栋、第二食堂") String locationName
    ) {
        log.info("🔍 [Tool] 查询位置信息，location: {}", locationName);

        try {
            if (locationName == null || locationName.isEmpty()) {
                return "⚠️ 请提供位置名称";
            }

            String area = CAMPUS_LOCATIONS.get(locationName);

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("📍 **%s**\n\n", locationName));

            if (area != null) {
                sb.append(String.format("  • 所在区域：%s\n", area));
                sb.append("  • 状态：正常营业中\n");
                sb.append("  • 可配送：是\n");
            } else {
                sb.append("  • 所在区域：未知位置\n");
                sb.append("  • 建议：请提供更详细的位置信息\n");
                sb.append("\n💡 支持的位置示例：");
                sb.append("\n  • 学生宿舍1-3栋");
                sb.append("\n  • 第一、二、三食堂");
                sb.append("\n  • 图书馆、教学楼A/B");
                sb.append("\n  • 体育馆、操场");
            }

            log.info("✅ [Tool] 查询位置信息成功");
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 查询位置信息失败", e);
            return "❌ 查询失败：" + e.getMessage();
        }
    }

    /**
     * 计算两个位置之间的距离
     *
     * @param location1 位置1
     * @param location2 位置2
     * @return 距离信息
     */
    @Tool("""
        计算校园内两个位置之间的距离

        **距离类型：**
        - 步行距离（米）
        - 预估步行时间（分钟）

        **何时使用：**
        - 估算配送时间
        - 规划取餐路线

        **参数：**
        - location1 - 位置1
        - location2 - 位置2

        **返回：** 距离和时间估算
        """)
    public String calculateDistance(
        @P("位置1，如：学生宿舍1栋") String location1,
        @P("位置2，如：第二食堂") String location2
    ) {
        log.info("🔍 [Tool] 计算距离，location1: {}, location2: {}", location1, location2);

        try {
            if (location1 == null || location2 == null) {
                return "⚠️ 请提供两个位置名称";
            }

            // 简化版距离计算（实际项目中应使用真实的地理距离API）
            int distance = estimateDistance(location1, location2);
            int walkTime = (int) Math.ceil(distance / 80.0); // 假设步行速度80米/分钟

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("📏 距离计算：**%s** → **%s**\n\n", location1, location2));
            sb.append(String.format("  • 直线距离：约%d米\n", distance));
            sb.append(String.format("  • 步行时间：约%d分钟\n\n", walkTime));

            if (distance < 500) {
                sb.append("💡 距离很近，适合步行取餐");
            } else if (distance < 1000) {
                sb.append("💡 距离适中，步行约15分钟");
            } else {
                sb.append("💡 距离较远，建议选择配送服务");
            }

            log.info("✅ [Tool] 计算距离成功，距离: {}米", distance);
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 计算距离失败", e);
            return "❌ 计算失败：" + e.getMessage();
        }
    }

    /**
     * 获取附近的商家
     *
     * @param userLocation 用户位置
     * @param limit 返回数量
     * @return 附近商家列表
     */
    @Tool("""
        获取用户位置附近的商家

        **推荐依据：**
        - 距离最近
        - 配送范围内
        - 评分较高

        **何时使用：**
        - 用户询问"附近有什么"
        - 默认推荐商家

        **参数：**
        - userLocation - 用户位置
        - limit - 返回数量（默认10）

        **返回：** 附近商家列表
        """)
    public String getNearbyMerchants(
        @P("用户位置，如：学生宿舍1栋") String userLocation,
        @P("返回数量（默认10）") Integer limit
    ) {
        log.info("🔍 [Tool] 获取附近商家，location: {}, limit: {}", userLocation, limit);

        try {
            int actualLimit = limit != null && limit > 0 ? limit : 10;

            // 简化版：返回所有商家（实际应根据距离排序）
            List<Merchant> merchants = merchantService.list();

            if (merchants.isEmpty()) {
                return "📋 暂无商家数据";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("📍 **%s** 附近的商家\n\n", userLocation));

            // 显示前actualLimit个商家
            int count = Math.min(actualLimit, merchants.size());
            for (int i = 0; i < count; i++) {
                Merchant merchant = merchants.get(i);
                int distance = 300 + (i * 100); // 模拟距离
                int deliveryTime = 15 + (distance / 100); // 预估时间

                // 重要：必须显示商家ID，方便后续下单时使用
                sb.append(String.format(
                    "%d. **商家ID：%s**\n" +
                    "   🏪 %s\n" +
                    "   📍 距离约%d米 | ⏰ 预计%d分钟\n" +
                    "   ⭐ %.1f分 | 📍 %s\n\n",
                    i + 1,
                    merchant.getId(),  // ← 新增：显示商家ID
                    merchant.getName(),
                    distance,
                    deliveryTime,
                    merchant.getRating() != null ? merchant.getRating() : 0,
                    merchant.getAddress() != null ? merchant.getAddress() : "地址暂无"
                ));
            }

            sb.append(String.format("💡 共找到%d家附近商家，按距离排序", count));

            log.info("✅ [Tool] 获取附近商家成功，数量: {}", count);
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 获取附近商家失败", e);
            return "❌ 获取失败：" + e.getMessage();
        }
    }

    /**
     * 验证位置是否在配送范围
     *
     * @param userLocation 用户位置
     * @param merchantLocation 商家位置
     * @return 验证结果
     */
    @Tool("""
        验证用户位置是否在商家配送范围内

        **何时使用：**
        - 下单前验证
        - 检查是否可配送

        **参数：**
        - userLocation - 用户位置
        - merchantLocation - 商家位置

        **返回：** 验证结果和配送费
        """)
    public String checkDeliveryRange(
        @P("用户位置，如：学生宿舍1栋") String userLocation,
        @P("商家位置，如：第二食堂") String merchantLocation
    ) {
        log.info("🔍 [Tool] 检查配送范围，userLocation: {}, merchantLocation: {}",
            userLocation, merchantLocation);

        try {
            int distance = estimateDistance(userLocation, merchantLocation);
            int maxDeliveryRange = 2000; // 假设最大配送距离2000米
            double baseDeliveryFee = 5.0; // 基础配送费

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("🚚 配送范围检查\n\n"));
            sb.append(String.format("  • 配送距离：%d米\n", distance));
            sb.append(String.format("  • 最大配送距离：%d米\n\n", maxDeliveryRange));

            if (distance <= maxDeliveryRange) {
                double deliveryFee = baseDeliveryFee;
                if (distance > 1000) {
                    deliveryFee += 3.0;
                } else if (distance > 500) {
                    deliveryFee += 1.5;
                }

                sb.append("✅ **在配送范围内**\n\n");
                sb.append(String.format("💰 配送费：%.2f元\n", deliveryFee));
                sb.append(String.format("⏰ 预计送达：%d分钟", 15 + (distance / 100)));
            } else {
                sb.append("❌ **超出配送范围**\n\n");
                sb.append("💡 建议：");
                sb.append("\n  • 选择距离更近的商家");
                sb.append("\n  • 或者到店自取");
            }

            log.info("✅ [Tool] 检查配送范围成功，距离: {}米", distance);
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 检查配送范围失败", e);
            return "❌ 检查失败：" + e.getMessage();
        }
    }

    /**
     * 获取校园地图说明
     *
     * @return 地图说明
     */
    @Tool("""
        获取校园地图和使用说明

        **何时使用：**
        - 新用户询问位置
        - 不熟悉校园环境

        **返回：** 校园地图说明
        """)
    public String getCampusMap() {
        log.info("🔍 [Tool] 获取校园地图");

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("🗺️ **校园地图**\n\n");

            sb.append("📍 **主要区域**\n\n");

            sb.append("1️⃣ **宿舍区**\n");
            sb.append("   • 学生宿舍1-3栋\n");
            sb.append("   • 位置：校园东侧\n\n");

            sb.append("2️⃣ **食堂区**\n");
            sb.append("   • 第一食堂（清真）\n");
            sb.append("   • 第二食堂（大众）\n");
            sb.append("   • 第三食堂（风味）\n");
            sb.append("   • 位置：校园中心\n\n");

            sb.append("3️⃣ **教学区**\n");
            sb.append("   • 图书馆\n");
            sb.append("   • 教学楼A、B\n");
            sb.append("   • 位置：校园西侧\n\n");

            sb.append("4️⃣ **运动区**\n");
            sb.append("   • 体育馆\n");
            sb.append("   • 操场\n");
            sb.append("   • 位置：校园北侧\n\n");

            sb.append("💡 **使用提示**\n");
            sb.append("  • 所有食堂支持配送服务\n");
            sb.append("  • 宿舍区配送时间：11:00-13:30, 17:00-19:30\n");
            sb.append("  • 教学区配送时间：11:30-13:00, 17:30-19:00\n");

            log.info("✅ [Tool] 获取校园地图成功");
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 获取校园地图失败", e);
            return "❌ 获取失败：" + e.getMessage();
        }
    }

    /**
     * 估算距离（简化版）
     */
    private int estimateDistance(String location1, String location2) {
        // 简化版距离计算
        // 实际项目中应使用真实的地理坐标计算
        String area1 = CAMPUS_LOCATIONS.getOrDefault(location1, "");
        String area2 = CAMPUS_LOCATIONS.getOrDefault(location2, "");

        if (area1.equals(area2) && !area1.isEmpty()) {
            return 200; // 同一区域约200米
        }

        return 600; // 不同区域约600米
    }

    /**
     * 推荐附近美食
     * 综合评分：口味30% + 营养20% + 价格10% + 距离15% + 评分25%
     *
     * @param userId 用户ID
     * @param maxDistance 最大距离（公里）
     * @param preference 偏好标签（可选，如"辣"、"清淡"）
     * @return 附近美食推荐
     */
    @Tool("""
        推荐附近美食，综合口味、营养、价格、距离、评分多个维度

        **评分算法：**
        - 口味匹配度（30%）
        - 营养健康度（20%）
        - 价格合理性（10%）
        - 距离便利性（15%）
        - 商家评分（25%）

        **何时使用：**
        - 用户询问"附近有什么好吃的"
        - 用户想要美食推荐
        - 用户没有明确目标，需要建议

        **参数：**
        - maxDistance - 最大距离（公里）
        - preference - 偏好标签（可选）

        **无需参数**，userId自动从上下文获取

        **返回：** 附近美食推荐列表
        """)
    @CardType("food_recommendation_card")
    public String recommendNearbyFood(
        AgenticScope scope,
        @P("最大距离（公里）") Double maxDistance,
        @P("偏好标签，如：辣、清淡") String preference
    ) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return "❌ 无法获取用户信息，请重新登录";
        }
        log.info("🔍 [Tool] 推荐附近美食，用户：{}，距离：{}km，偏好：{}",
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

            // 2. 获取这些商家的所有在线菜品
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

            // 3. 为每个菜品计算综合评分
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

            // 4. 按综合评分排序，取前10个
            List<DishScore> topDishes = dishScores.stream()
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .limit(10)
                .collect(Collectors.toList());

            if (topDishes.isEmpty()) {
                return "没有找到符合条件的美食，换个条件试试？";
            }

            // 5. 构建推荐结果
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

                // 重要：必须显示菜品ID和商家ID，方便后续下单时使用
                result.append(String.format("**%d. 菜品ID：%s**\n", i + 1, dish.getId()));
                result.append(String.format("   🍲 %s\n", dish.getName()));
                result.append(String.format("   💰 ¥%.2f | 🔥 %d kcal",
                    dish.getPrice(), dish.getCalorie()));

                if (dish.getAvgRating() != null) {
                    result.append(String.format(" | ⭐ %.1f分", dish.getAvgRating()));
                }

                result.append(String.format("\n   🏪 商家ID：%s - %s\n",
                    merchant.getId(), merchant.getName()));

                if (merchant.getAveragePrice() != null) {
                    result.append(String.format("   人均：¥%.0f\n", merchant.getAveragePrice()));
                }

                // 显示综合评分
                result.append(String.format("   综合评分：%.2f分\n\n", ds.score));
            }

            result.append("💡 综合评分包含：口味(30%) + 营养(20%) + 价格(10%) + 距离(15%) + 评分(25%)");

            log.info("✅ [Tool] 推荐附近美食成功，数量：{}", topDishes.size());
            return result.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 推荐附近美食失败", e);
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
    private double calculateRealDistance(double lat1, double lon1, double lat2, double lon2) {
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
}
