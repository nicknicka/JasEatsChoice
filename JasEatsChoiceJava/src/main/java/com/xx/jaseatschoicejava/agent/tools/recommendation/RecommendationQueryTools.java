package com.xx.jaseatschoicejava.agent.tools.recommendation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.UserService;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * 推荐查询工具类
 *
 * 为Agent提供菜品推荐查询功能
 *
 * @author Claude
 * @since 2026-03-24
 */
@Slf4j
@Service
public class RecommendationQueryTools {

    @Resource
    private DishService dishService;

    @Resource
    private UserService userService;

    /**
     * 查询推荐菜品列表
     *
     * @param userId 用户ID
     * @param category 分类（可选）
     * @return 推荐菜品列表
     */
    @Tool("""
        根据用户偏好查询推荐菜品

        **推荐因素：**
        - 用户饮食目标
        - 口味偏好
        - 过敏信息
        - 菜品评分

        **何时使用：**
        - 用户要求推荐
        - 菜品搜索
        - 个性化建议

        **参数：**
        - category - 分类（可选，如：主食、汤羹、小吃）

        **无需参数**，userId自动从上下文获取

        **返回：** 推荐菜品列表（文本格式）
        """)
    public String queryRecommendations(
        AgenticScope scope,
        @P("分类（可选）") String category
    ) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return "❌ 无法获取用户信息，请重新登录";
        }
        log.info("🔍 [Tool] 查询推荐菜品，userId: {}, category: {}", userId, category);

        try {
            User user = userService.getById(userId);

            if (user == null) {
                return "❌ 用户不存在";
            }

            // 构建查询条件
            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<Dish>()
                .eq(Dish::getIsOnline, true);

            if (category != null && !category.isEmpty()) {
                queryWrapper.eq(Dish::getCategory, category);
            }

            // 根据用户偏好过滤
            String allergies = user.getAllergies() != null ?
                user.getAllergies().toString() : "[]";

            // 按推荐得分排序
            queryWrapper.orderByDesc(Dish::getScore)
                .last("LIMIT 10");

            List<Dish> dishes = dishService.list(queryWrapper);

            if (dishes.isEmpty()) {
                return "📋 暂无推荐菜品";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("🌟 为您推荐的菜品\n\n");

            for (int i = 0; i < dishes.size(); i++) {
                Dish dish = dishes.get(i);
                sb.append(String.format(
                    "%d. **%s**\n" +
                    "   💰 %.2f元 | 🔥 %d千卡 | ⭐ %.1f分\n" +
                    "   📝 %s\n\n",
                    i + 1,
                    dish.getName(),
                    dish.getPrice(),
                    dish.getCalorie() != null ? dish.getCalorie() : 0,
                    dish.getAvgRating() != null ? dish.getAvgRating() : 0,
                    dish.getDescription() != null && !dish.getDescription().isEmpty()
                        ? dish.getDescription()
                        : "暂无描述"
                ));
            }

            // 添加个性化建议
            if (user.getDietGoal() != null && !user.getDietGoal().isEmpty()) {
                sb.append(String.format(
                    "💡 根据您的目标（%s），建议选择合适的份量\n",
                    user.getDietGoal()
                ));
            }

            log.info("✅ [Tool] 查询推荐成功，数量: {}", dishes.size());
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 查询推荐失败", e);
            return "❌ 查询失败：" + e.getMessage();
        }
    }

    /**
     * 获取热门菜品
     *
     * @param limit 返回数量
     * @param category 分类（可选）
     * @return 热门菜品列表
     */
    @Tool("""
        获取当前热门菜品

        **热度依据：**
        - 评分
        - 推荐得分
        - 上架状态

        **何时使用：**
        - 用户询问热门推荐
        - 首页展示
        - 菜品发现

        **参数：**
        - limit - 返回数量（默认10）
        - category - 分类（可选）

        **返回：** 热门菜品列表
        """)
    public String getHotDishes(
        @P("返回数量（默认10）") Integer limit,
        @P("分类（可选）") String category
    ) {
        log.info("🔍 [Tool] 查询热门菜品，limit: {}, category: {}", limit, category);

        try {
            int actualLimit = limit != null && limit > 0 ? limit : 10;

            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<Dish>()
                .eq(Dish::getIsOnline, true)
                .isNotNull(Dish::getAvgRating)
                .gt(Dish::getAvgRating, BigDecimal.valueOf(3.5))
                .orderByDesc(Dish::getAvgRating)
                .orderByDesc(Dish::getScore)
                .last("LIMIT " + actualLimit);

            if (category != null && !category.isEmpty()) {
                queryWrapper.eq(Dish::getCategory, category);
            }

            List<Dish> dishes = dishService.list(queryWrapper);

            if (dishes.isEmpty()) {
                return "📋 暂无热门菜品";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("🔥 当前热门菜品（Top %d）\n\n", dishes.size()));

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

            log.info("✅ [Tool] 查询热门菜品成功，数量: {}", dishes.size());
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 查询热门菜品失败", e);
            return "❌ 查询失败：" + e.getMessage();
        }
    }

    /**
     * 获取个性化推荐
     *
     * @param userId 用户ID
     * @return 个性化推荐
     */
    @Tool("""
        获取基于用户偏好的个性化推荐

        **个性化因素：**
        - 历史订单
        - 饮食目标
        - 口味偏好
        - 过敏信息

        **何时使用：**
        - 首页推荐
        - 个性化建议
        - 智能推荐

        **无需参数**，userId自动从上下文获取

        **返回：** 个性化推荐菜品
        """)
    public String getPersonalizedRecommendations(
        AgenticScope scope
    ) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return "❌ 无法获取用户信息，请重新登录";
        }
        log.info("🔍 [Tool] 获取个性化推荐，userId: {}", userId);

        try {
            User user = userService.getById(userId);

            if (user == null) {
                return "❌ 用户不存在";
            }

            // 获取用户偏好
            String dietGoal = user.getDietGoal();
            JsonNode preferTags = user.getPreferTags();

            StringBuilder sb = new StringBuilder();
            sb.append("🎯 为您量身定制的推荐\n\n");

            if (dietGoal != null && !dietGoal.isEmpty()) {
                sb.append(String.format("🎯 您的目标：%s\n\n", dietGoal));
            }

            // 根据目标推荐不同类型的菜品
            String targetCategory;
            switch (dietGoal != null ? dietGoal : "") {
                case "减肥":
                    targetCategory = "汤羹";
                    sb.append("💡 推荐：低热量、高纤维的菜品\n");
                    break;
                case "增肌":
                    targetCategory = "主食";
                    sb.append("💡 推荐：高蛋白的菜品\n");
                    break;
                default:
                    targetCategory = null;
                    sb.append("💡 推荐：营养均衡的菜品\n");
                    break;
            }

            // 查询推荐菜品
            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<Dish>()
                .eq(Dish::getIsOnline, true)
                .orderByDesc(Dish::getScore)
                .last("LIMIT 8");

            if (targetCategory != null) {
                queryWrapper.eq(Dish::getCategory, targetCategory);
            }

            List<Dish> dishes = dishService.list(queryWrapper);

            if (!dishes.isEmpty()) {
                sb.append("\n📋 推荐菜品：\n\n");
                for (int i = 0; i < dishes.size(); i++) {
                    Dish dish = dishes.get(i);
                    sb.append(String.format(
                        "%d. **%s**\n" +
                        "   💰 %.2f元 | 🔥 %d千卡\n\n",
                        i + 1,
                        dish.getName(),
                        dish.getPrice(),
                        dish.getCalorie() != null ? dish.getCalorie() : 0
                    ));
                }
            }

            sb.append("\n💡 提示：推荐基于您的饮食目标和偏好");
            log.info("✅ [Tool] 个性化推荐完成");
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 个性化推荐失败", e);
            return "❌ 推荐失败：" + e.getMessage();
        }
    }

    /**
     * 根据热量查询菜品
     *
     * @param maxCalories 最大热量
     * @return 低热量菜品列表
     */
    @Tool("""
        查询指定热量范围内的菜品

        **何时使用：**
        - 用户控制热量摄入
        - 减肥餐推荐
        - 健康饮食

        **参数：** maxCalories - 最大热量（千卡/100g）

        **返回：** 低热量菜品列表
        """)
    public String queryLowCalorieDishes(
        @P("最大热量（千卡/100g）") Integer maxCalories
    ) {
        log.info("🔍 [Tool] 查询低热量菜品，maxCalories: {}", maxCalories);

        try {
            if (maxCalories == null || maxCalories <= 0) {
                return "❌ 请输入有效的热量值";
            }

            List<Dish> dishes = dishService.list(
                new LambdaQueryWrapper<Dish>()
                    .eq(Dish::getIsOnline, true)
                    .isNotNull(Dish::getCalorie)
                    .le(Dish::getCalorie, maxCalories)
                    .orderByAsc(Dish::getCalorie)
                    .last("LIMIT 10")
            );

            if (dishes.isEmpty()) {
                return String.format("📋 暂无热量低于%d千卡的菜品", maxCalories);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("🥗 低热量菜品（< %d千卡/100g）\n\n", maxCalories));

            for (int i = 0; i < dishes.size(); i++) {
                Dish dish = dishes.get(i);
                sb.append(String.format(
                    "%d. **%s**\n" +
                    "   🔥 %d千卡 | 💰 %.2f元\n\n",
                    i + 1,
                    dish.getName(),
                    dish.getCalorie(),
                    dish.getPrice()
                ));
            }

            sb.append("💡 这些菜品热量较低，适合控制热量摄入");

            log.info("✅ [Tool] 查询低热量菜品成功，数量: {}", dishes.size());
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 查询低热量菜品失败", e);
            return "❌ 查询失败：" + e.getMessage();
        }
    }
}
