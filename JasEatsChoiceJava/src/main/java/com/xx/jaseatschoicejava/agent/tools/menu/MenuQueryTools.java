package com.xx.jaseatschoicejava.agent.tools.menu;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.agent.annotation.CardType;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.service.DishService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 菜品查询工具类
 *
 * 为Agent提供菜品信息的查询功能
 *
 * @author Claude
 * @since 2026-03-24
 */
@Slf4j
@Service
public class MenuQueryTools {

    @Resource
    private DishService dishService;

    /**
     * 查询商家菜单
     *
     * @param merchantId 商家ID
     * @return 菜单列表（文本格式）
     */
    @Tool("""
        查询商家的完整菜单

        **菜单包含：**
        - 菜品名称
        - 价格
        - 描述
        - 分类

        **何时使用：**
        - 用户浏览菜单
        - 查看商家有哪些菜

        **参数：** merchantId - 商家ID

        **返回：** 商家菜单（文本格式）
        """)
    @CardType("menu_card")
    public String getMerchantMenu(
        @P("商家ID") String merchantId
    ) {
        log.info("🔍 [Tool] 查询商家菜单，merchantId: {}", merchantId);

        try {
            List<Dish> dishes = dishService.list(
                new LambdaQueryWrapper<Dish>()
                    .eq(Dish::getMerchantId, merchantId)
                    .eq(Dish::getIsOnline, true)
                    .orderByAsc(Dish::getCategory)
            );

            if (dishes.isEmpty()) {
                return "📋 该商家暂无菜品";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("📋 商家菜单\n\n");

            String currentCategory = "";
            for (Dish dish : dishes) {
                // 如果分类改变，添加分类标题
                if (!dish.getCategory().equals(currentCategory)) {
                    currentCategory = dish.getCategory();
                    sb.append(String.format("🍽️ 【%s】\n\n", currentCategory));
                }

                sb.append(String.format(
                    "  • %s\n" +
                    "    💰 %.2f元 | %s\n\n",
                    dish.getName(),
                    dish.getPrice(),
                    dish.getDescription() != null && !dish.getDescription().isEmpty()
                        ? dish.getDescription()
                        : "暂无描述"
                ));
            }

            log.info("✅ [Tool] 查询商家菜单成功，菜品数量: {}", dishes.size());
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 查询商家菜单失败，merchantId: {}", merchantId, e);
            return "❌ 查询失败：" + e.getMessage();
        }
    }

    /**
     * 搜索菜品
     *
     * @param keyword 搜索关键词
     * @return 搜索结果
     */
    @Tool("""
        搜索菜品

        **搜索范围：**
        - 菜品名称
        - 菜品描述

        **何时使用：**
        - 用户搜索特定菜品
        - 按关键词查找

        **参数：** keyword - 搜索关键词

        **返回：** 搜索结果（文本格式）
        """)
    public String searchDishes(
        @P("搜索关键词") String keyword
    ) {
        log.info("🔍 [Tool] 搜索菜品，keyword: {}", keyword);

        try {
            // 模糊搜索菜品名称和描述
            List<Dish> dishes = dishService.list(
                new LambdaQueryWrapper<Dish>()
                    .and(wrapper -> wrapper
                        .like(Dish::getName, keyword)
                        .or()
                        .like(Dish::getDescription, keyword)
                    )
                    .eq(Dish::getIsOnline, true)
                    .orderByAsc(Dish::getMerchantId)
                    .last("LIMIT 20")
            );

            if (dishes.isEmpty()) {
                return String.format("🔍 未找到与\"%s\"相关的菜品", keyword);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("🔍 搜索\"%s\"的结果（共%d个）\n\n", keyword, dishes.size()));

            for (int i = 0; i < dishes.size(); i++) {
                Dish dish = dishes.get(i);
                sb.append(String.format(
                    "%d. %s\n" +
                    "   💰 %.2f元 | 🏪 商家ID: %s\n" +
                    "   📝 %s\n\n",
                    i + 1,
                    dish.getName(),
                    dish.getPrice(),
                    dish.getMerchantId(),
                    dish.getDescription() != null && !dish.getDescription().isEmpty()
                        ? dish.getDescription()
                        : "暂无描述"
                ));
            }

            log.info("✅ [Tool] 搜索菜品成功，结果数量: {}", dishes.size());
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 搜索菜品失败，keyword: {}", keyword, e);
            return "❌ 搜索失败：" + e.getMessage();
        }
    }

    /**
     * 获取菜品详情
     *
     * @param dishId 菜品ID
     * @return 菜品详情
     */
    @Tool("""
        获取菜品的详细信息

        **详情包含：**
        - 基本信息（名称、价格、描述）
        - 分类
        - 商家信息
        - 卡路里
        - 食材信息

        **何时使用：**
        - 用户查看菜品详情
        - 了解菜品具体信息

        **参数：** dishId - 菜品ID

        **返回：** 菜品详情（文本格式）
        """)
    public String getDishDetail(
        @P("菜品ID") String dishId
    ) {
        log.info("🔍 [Tool] 查询菜品详情，dishId: {}", dishId);

        try {
            Dish dish = dishService.getById(dishId);

            if (dish == null) {
                return "❌ 菜品不存在";
            }

            String result = String.format(
                "🍽️ 菜品详情\n\n" +
                "📝 名称：%s\n" +
                "💰 价格：%.2f元\n" +
                "🏷️ 分类：%s\n" +
                "🏪 商家ID：%s\n" +
                "📖 描述：%s\n" +
                "🔥 卡路里：%d千卡\n\n" +
                "🥗 主要食材：%s",
                dish.getName(),
                dish.getPrice(),
                dish.getCategory(),
                dish.getMerchantId(),
                dish.getDescription() != null && !dish.getDescription().isEmpty()
                    ? dish.getDescription()
                    : "暂无描述",
                dish.getCalorie() != null ? dish.getCalorie() : 0,
                dish.getIngredients() != null && !dish.getIngredients().isEmpty()
                    ? dish.getIngredients()
                    : "暂无"
            );

            log.info("✅ [Tool] 查询菜品详情成功: {}", dish.getName());
            return result;

        } catch (Exception e) {
            log.error("❌ [Tool] 查询菜品详情失败，dishId: {}", dishId, e);
            return "❌ 查询失败：" + e.getMessage();
        }
    }

    /**
     * 按分类查询菜品
     *
     * @param merchantId 商家ID
     * @param category 分类名称
     * @return 分类菜品列表
     */
    @Tool("""
        按分类查询商家的菜品

        **何时使用：**
        - 用户浏览特定分类
        - 筛选菜品类型

        **参数：**
        - merchantId - 商家ID
        - category - 分类名称（如：主食、汤羹、小吃）

        **返回：** 分类菜品列表（文本格式）
        """)
    public String getDishesByCategory(
        @P("商家ID") String merchantId,
        @P("分类名称") String category
    ) {
        log.info("🔍 [Tool] 按分类查询菜品，merchantId: {}, category: {}", merchantId, category);

        try {
            List<Dish> dishes = dishService.list(
                new LambdaQueryWrapper<Dish>()
                    .eq(Dish::getMerchantId, merchantId)
                    .eq(Dish::getCategory, category)
                    .eq(Dish::getIsOnline, true)
            );

            if (dishes.isEmpty()) {
                return String.format("📋 该商家暂无【%s】分类的菜品", category);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("📋 【%s】分类（共%d个）\n\n", category, dishes.size()));

            for (int i = 0; i < dishes.size(); i++) {
                Dish dish = dishes.get(i);
                sb.append(String.format(
                    "%d. %s\n" +
                    "   💰 %.2f元\n" +
                    "   📖 %s\n\n",
                    i + 1,
                    dish.getName(),
                    dish.getPrice(),
                    dish.getDescription() != null && !dish.getDescription().isEmpty()
                        ? dish.getDescription()
                        : "暂无描述"
                ));
            }

            log.info("✅ [Tool] 按分类查询成功，结果数量: {}", dishes.size());
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 按分类查询失败", e);
            return "❌ 查询失败：" + e.getMessage();
        }
    }
}
