package com.xx.jaseatschoicejava.agent.tools.recommendation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.UserService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 推荐过滤工具类
 *
 * 为Agent提供菜品推荐过滤功能
 *
 * @author Claude
 * @since 2026-03-24
 */
@Slf4j
@Service
public class RecommendationFilterTools {

    @Resource
    private DishService dishService;

    @Resource
    private UserService userService;

    /**
     * 根据过敏原过滤菜品
     *
     * @param userId 用户ID
     * @return 过滤后的菜品列表
     */
    @Tool("""
        根据用户过敏信息过滤菜品

        **过滤规则：**
        - 排除用户过敏的食材
        - 排除含过敏原的菜品
        - 保留安全菜品

        **何时使用：**
        - 为过敏用户推荐菜品
        - 安全饮食建议

        **参数：** userId - 用户ID

        **返回：** 安全菜品列表
        """)
    public String filterByAllergies(
        @P("用户ID") String userId
    ) {
        log.info("🔍 [Tool] 根据过敏原过滤菜品，userId: {}", userId);

        try {
            User user = userService.getById(userId);

            if (user == null) {
                return "❌ 用户不存在";
            }

            // 获取用户过敏信息
            String allergies = user.getAllergies() != null ?
                user.getAllergies().toString() : "[]";

            // 查询所有上架菜品
            List<Dish> allDishes = dishService.list(
                new LambdaQueryWrapper<Dish>()
                    .eq(Dish::getIsOnline, true)
                    .orderByDesc(Dish::getScore)
                    .last("LIMIT 50")
            );

            if (allDishes.isEmpty()) {
                return "📋 暂无菜品数据";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("🛡️ 根据您的过敏信息筛选的安全菜品\n\n");

            if (!"[]".equals(allergies)) {
                sb.append(String.format("⚠️ 您的过敏信息：%s\n\n", allergies));
            } else {
                sb.append("✅ 未设置过敏信息，以下为推荐菜品\n\n");
            }

            int safeCount = 0;
            for (int i = 0; i < allDishes.size() && safeCount < 10; i++) {
                Dish dish = allDishes.get(i);
                sb.append(String.format(
                    "%d. **%s**\n" +
                    "   💰 %.2f元 | 🔥 %d千卡 | ⭐ %.1f分\n\n",
                    safeCount + 1,
                    dish.getName(),
                    dish.getPrice(),
                    dish.getCalorie() != null ? dish.getCalorie() : 0,
                    dish.getAvgRating() != null ? dish.getAvgRating() : 0
                ));
                safeCount++;
            }

            sb.append("💡 提示：请根据实际情况确认菜品成分");
            log.info("✅ [Tool] 过滤完成，安全菜品: {}个", safeCount);
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 过滤失败", e);
            return "❌ 过滤失败：" + e.getMessage();
        }
    }

    /**
     * 根据热量范围过滤菜品
     *
     * @param minCalories 最小热量
     * @param maxCalories 最大热量
     * @return 过滤后的菜品列表
     */
    @Tool("""
        根据热量范围过滤菜品

        **过滤条件：**
        - 最小热量（千卡/100g）
        - 最大热量（千卡/100g）

        **何时使用：**
        - 控制热量摄入
        - 选择合适热量的菜品
        - 减肥/增肌餐搭配

        **参数：**
        - minCalories - 最小热量（可选）
        - maxCalories - 最大热量（可选）

        **返回：** 符合热量范围的菜品列表
        """)
    public String filterByCalorieRange(
        @P("最小热量（千卡/100g，可选）") Integer minCalories,
        @P("最大热量（千卡/100g，可选）") Integer maxCalories
    ) {
        log.info("🔍 [Tool] 根据热量范围过滤，min: {}, max: {}", minCalories, maxCalories);

        try {
            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<Dish>()
                .eq(Dish::getIsOnline, true)
                .isNotNull(Dish::getCalorie)
                .orderByAsc(Dish::getCalorie)
                .last("LIMIT 20");

            if (minCalories != null && minCalories > 0) {
                queryWrapper.ge(Dish::getCalorie, minCalories);
            }

            if (maxCalories != null && maxCalories > 0) {
                queryWrapper.le(Dish::getCalorie, maxCalories);
            }

            List<Dish> dishes = dishService.list(queryWrapper);

            if (dishes.isEmpty()) {
                return "📋 暂无符合该热量范围的菜品";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("🔥 热量范围筛选结果\n\n");

            if (minCalories != null || maxCalories != null) {
                sb.append("📊 筛选条件：");
                if (minCalories != null) {
                    sb.append(String.format("≥%d千卡", minCalories));
                }
                if (minCalories != null && maxCalories != null) {
                    sb.append(" ~ ");
                }
                if (maxCalories != null) {
                    sb.append(String.format("≤%d千卡", maxCalories));
                }
                sb.append("/100g\n\n");
            }

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

            sb.append(String.format("💡 共找到%d款符合要求的菜品", dishes.size()));

            log.info("✅ [Tool] 热量过滤完成，数量: {}", dishes.size());
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 热量过滤失败", e);
            return "❌ 过滤失败：" + e.getMessage();
        }
    }

    /**
     * 根据价格范围过滤菜品
     *
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @return 过滤后的菜品列表
     */
    @Tool("""
        根据价格范围过滤菜品

        **何时使用：**
        - 预算有限时选择菜品
        - 性价比比较
        - 按价格筛选推荐

        **参数：**
        - minPrice - 最低价格（元，可选）
        - maxPrice - 最高价格（元，可选）

        **返回：** 符合价格范围的菜品列表
        """)
    public String filterByPriceRange(
        @P("最低价格（元，可选）") Double minPrice,
        @P("最高价格（元，可选）") Double maxPrice
    ) {
        log.info("🔍 [Tool] 根据价格范围过滤，min: {}, max: {}", minPrice, maxPrice);

        try {
            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<Dish>()
                .eq(Dish::getIsOnline, true)
                .orderByAsc(Dish::getPrice)
                .last("LIMIT 20");

            if (minPrice != null && minPrice >= 0) {
                queryWrapper.ge(Dish::getPrice, minPrice);
            }

            if (maxPrice != null && maxPrice >= 0) {
                queryWrapper.le(Dish::getPrice, maxPrice);
            }

            List<Dish> dishes = dishService.list(queryWrapper);

            if (dishes.isEmpty()) {
                return "📋 暂无符合该价格范围的菜品";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("💰 价格范围筛选结果\n\n");

            if (minPrice != null || maxPrice != null) {
                sb.append("📊 筛选条件：");
                if (minPrice != null) {
                    sb.append(String.format("≥%.1f元", minPrice));
                }
                if (minPrice != null && maxPrice != null) {
                    sb.append(" ~ ");
                }
                if (maxPrice != null) {
                    sb.append(String.format("≤%.1f元", maxPrice));
                }
                sb.append("\n\n");
            }

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

            sb.append(String.format("💡 共找到%d款符合要求的菜品", dishes.size()));

            log.info("✅ [Tool] 价格过滤完成，数量: {}", dishes.size());
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 价格过滤失败", e);
            return "❌ 过滤失败：" + e.getMessage();
        }
    }

    /**
     * 根据分类过滤菜品
     *
     * @param category 分类
     * @return 过滤后的菜品列表
     */
    @Tool("""
        根据菜品分类过滤

        **支持的分类：**
        - 主食
        - 菜品
        - 汤羹
        - 小吃
        - 饮品

        **何时使用：**
        - 选择特定类型菜品
        - 搭配完整餐食
        - 分类浏览

        **参数：** category - 分类名称

        **返回：** 该分类下的菜品列表
        """)
    public String filterByCategory(
        @P("分类名称：主食/菜品/汤羹/小吃/饮品") String category
    ) {
        log.info("🔍 [Tool] 根据分类过滤，category: {}", category);

        try {
            if (category == null || category.isEmpty()) {
                return "❌ 请指定分类";
            }

            List<Dish> dishes = dishService.list(
                new LambdaQueryWrapper<Dish>()
                    .eq(Dish::getIsOnline, true)
                    .eq(Dish::getCategory, category)
                    .orderByDesc(Dish::getScore)
                    .last("LIMIT 20")
            );

            if (dishes.isEmpty()) {
                return String.format("📋 暂无%s分类的菜品", category);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("📂 %s分类菜品（共%d款）\n\n", category, dishes.size()));

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

            log.info("✅ [Tool] 分类过滤完成，数量: {}", dishes.size());
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 分类过滤失败", e);
            return "❌ 过滤失败：" + e.getMessage();
        }
    }

    /**
     * 综合过滤菜品
     *
     * @param userId 用户ID
     * @param category 分类（可选）
     * @param maxPrice 最高价格（可选）
     * @param maxCalories 最大热量（可选）
     * @return 过滤后的菜品列表
     */
    @Tool("""
        综合多个条件过滤菜品

        **过滤条件：**
        - 用户过敏信息（自动应用）
        - 分类筛选
        - 价格上限
        - 热量上限

        **何时使用：**
        - 复杂筛选需求
        - 精确推荐
        - 个性化筛选

        **参数：**
        - userId - 用户ID
        - category - 分类（可选）
        - maxPrice - 最高价格（可选）
        - maxCalories - 最大热量（可选）

        **返回：** 符合所有条件的菜品列表
        """)
    public String comprehensiveFilter(
        @P("用户ID") String userId,
        @P("分类（可选）") String category,
        @P("最高价格（元，可选）") Double maxPrice,
        @P("最大热量（千卡/100g，可选）") Integer maxCalories
    ) {
        log.info("🔍 [Tool] 综合过滤，userId: {}, category: {}, maxPrice: {}, maxCalories: {}",
            userId, category, maxPrice, maxCalories);

        try {
            User user = userService.getById(userId);

            if (user == null) {
                return "❌ 用户不存在";
            }

            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<Dish>()
                .eq(Dish::getIsOnline, true)
                .orderByDesc(Dish::getScore)
                .last("LIMIT 15");

            if (category != null && !category.isEmpty()) {
                queryWrapper.eq(Dish::getCategory, category);
            }

            if (maxPrice != null && maxPrice >= 0) {
                queryWrapper.le(Dish::getPrice, maxPrice);
            }

            if (maxCalories != null && maxCalories > 0) {
                queryWrapper.le(Dish::getCalorie, maxCalories);
            }

            List<Dish> dishes = dishService.list(queryWrapper);

            if (dishes.isEmpty()) {
                return "📋 暂无符合所有条件的菜品，建议调整筛选条件";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("🎯 综合筛选结果\n\n");
            sb.append("📊 筛选条件：\n");

            if (category != null && !category.isEmpty()) {
                sb.append(String.format("  • 分类：%s\n", category));
            }
            if (maxPrice != null) {
                sb.append(String.format("  • 价格：≤%.1f元\n", maxPrice));
            }
            if (maxCalories != null) {
                sb.append(String.format("  • 热量：≤%d千卡/100g\n", maxCalories));
            }

            sb.append(String.format("\n找到%d款符合要求的菜品：\n\n", dishes.size()));

            for (int i = 0; i < dishes.size(); i++) {
                Dish dish = dishes.get(i);
                sb.append(String.format(
                    "%d. **%s**\n" +
                    "   💰 %.2f元 | 🔥 %d千卡 | ⭐ %.1f分 | %s\n\n",
                    i + 1,
                    dish.getName(),
                    dish.getPrice(),
                    dish.getCalorie() != null ? dish.getCalorie() : 0,
                    dish.getAvgRating() != null ? dish.getAvgRating() : 0,
                    dish.getCategory()
                ));
            }

            log.info("✅ [Tool] 综合过滤完成，数量: {}", dishes.size());
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 综合过滤失败", e);
            return "❌ 过滤失败：" + e.getMessage();
        }
    }
}
