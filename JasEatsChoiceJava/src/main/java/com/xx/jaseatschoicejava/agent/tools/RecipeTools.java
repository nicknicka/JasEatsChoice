package com.xx.jaseatschoicejava.agent.tools;

import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Recipe;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.RecipeService;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 食谱查询工具集
 * 使用LangChain4j的@Tool注解声明工具函数
 *
 * @author Claude
 * @since 2026-03-22
 */
@Service
public class RecipeTools {

    private static final Logger log = LoggerFactory.getLogger(RecipeTools.class);

    @Resource
    private RecipeService recipeService;

    @Resource
    private DishService dishService;

    /**
     * 获取今日食谱
     *
     * @param userId 用户ID
     * @return 今日食谱
     */
    @Tool("获取用户今日的食谱推荐和营养信息")
    public String getTodayRecipes(String userId) {
        log.info("执行工具：getTodayRecipes，用户：{}", userId);

        try {
            Map<String, Object> todayData = recipeService.getTodayRecipes(userId);

            if (todayData == null || todayData.isEmpty()) {
                return "📖 **今日食谱**\n\n" +
                       "暂无今日食谱推荐。\n\n" +
                       "💡 您可以说「给我推荐一些食谱」来获取推荐。";
            }

            StringBuilder result = new StringBuilder();
            result.append("📖 **今日食谱**\n\n");

            // 获取食谱列表
            @SuppressWarnings("unchecked")
            List<Recipe> recipes = (List<Recipe>) todayData.get("recipes");

            if (recipes != null && !recipes.isEmpty()) {
                result.append("**🍽️ 推荐食谱：**\n\n");

                for (int i = 0; i < recipes.size(); i++) {
                    Recipe recipe = recipes.get(i);
                    result.append(String.format("**%d. %s**\n", i + 1, recipe.getName()));

                    if (recipe.getType() != null) {
                        result.append(String.format("   类型：%s\n", recipe.getType()));
                    }

                    if (recipe.getCalories() != null) {
                        result.append(String.format("   热量：%d kcal\n", recipe.getCalories()));
                    }

                    if (recipe.getCookTime() != null) {
                        result.append(String.format("   烹饪时间：%s\n", recipe.getCookTime()));
                    }

                    if (recipe.getFavorite() != null && recipe.getFavorite()) {
                        result.append("   ⭐ 已收藏\n");
                    }

                    result.append("\n");
                }
            }

            // 获取营养信息
            Map<String, Object> nutrition = (Map<String, Object>) todayData.get("nutrition");
            if (nutrition != null && !nutrition.isEmpty()) {
                result.append("**📊 营养统计：**\n");
                if (nutrition.containsKey("totalCalories")) {
                    result.append(String.format("- 总热量：%s\n", nutrition.get("totalCalories")));
                }
                if (nutrition.containsKey("totalProtein")) {
                    result.append(String.format("- 蛋白质：%s g\n", nutrition.get("totalProtein")));
                }
                if (nutrition.containsKey("totalCarbs")) {
                    result.append(String.format("- 碳水化合物：%s g\n", nutrition.get("totalCarbs")));
                }
                if (nutrition.containsKey("totalFat")) {
                    result.append(String.format("- 脂肪：%s g\n", nutrition.get("totalFat")));
                }
            }

            result.append("\n💡 提示：您可以说「收藏食谱XXX」来收藏喜欢的食谱。");

            return result.toString();

        } catch (Exception e) {
            log.error("获取今日食谱失败", e);
            return "获取今日食谱失败：" + e.getMessage();
        }
    }

    /**
     * 获取我的收藏食谱
     *
     * @param userId 用户ID
     * @return 收藏的食谱列表
     */
    @Tool("获取用户收藏的食谱列表")
    public String getFavoriteRecipes(String userId) {
        log.info("执行工具：getFavoriteRecipes，用户：{}", userId);

        try {
            List<Recipe> recipes = recipeService.getFavoriteRecipes(userId);

            if (recipes == null || recipes.isEmpty()) {
                return "⭐ **我的收藏**\n\n" +
                       "您还没有收藏任何食谱。\n\n" +
                       "💡 您可以说「给我推荐一些食谱」来发现好吃的食谱！";
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("⭐ **我的收藏**（共%d个）\n\n", recipes.size()));

            for (int i = 0; i < recipes.size(); i++) {
                Recipe recipe = recipes.get(i);
                result.append(String.format("**%d. %s**\n", i + 1, recipe.getName()));

                if (recipe.getType() != null) {
                    result.append(String.format("   类型：%s\n", recipe.getType()));
                }

                if (recipe.getCalories() != null) {
                    result.append(String.format("   热量：%d kcal\n", recipe.getCalories()));
                }

                if (recipe.getCookTime() != null) {
                    result.append(String.format("   烹饪时间：%s\n", recipe.getCookTime()));
                }

                result.append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("获取收藏食谱失败", e);
            return "获取收藏食谱失败：" + e.getMessage();
        }
    }

    /**
     * 获取所有食谱
     *
     * @param userId 用户ID
     * @return 所有食谱列表
     */
    @Tool("获取用户创建的所有食谱")
    public String getAllRecipes(String userId) {
        log.info("执行工具：getAllRecipes，用户：{}", userId);

        try {
            List<Recipe> recipes = recipeService.getAllRecipes(userId);

            if (recipes == null || recipes.isEmpty()) {
                return "📖 **我的食谱**\n\n" +
                       "您还没有创建任何食谱。\n\n" +
                       "💡 您可以说「创建食谱宫保鸡丁」来创建您的第一个食谱！";
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("📖 **我的食谱**（共%d个）\n\n", recipes.size()));

            for (int i = 0; i < recipes.size(); i++) {
                Recipe recipe = recipes.get(i);
                result.append(String.format("**%d. %s**\n", i + 1, recipe.getName()));

                if (recipe.getType() != null) {
                    result.append(String.format("   类型：%s\n", recipe.getType()));
                }

                if (recipe.getCalories() != null) {
                    result.append(String.format("   热量：%d kcal\n", recipe.getCalories()));
                }

                if (recipe.getFavorite() != null && recipe.getFavorite()) {
                    result.append("   ⭐ 已收藏\n");
                }

                result.append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("获取所有食谱失败", e);
            return "获取所有食谱失败：" + e.getMessage();
        }
    }

    /**
     * 获取推荐食谱
     *
     * @return 推荐食谱列表
     */
    @Tool("获取系统推荐的食谱")
    public String getRecommendedRecipes() {
        log.info("执行工具：getRecommendedRecipes");

        try {
            List<Recipe> recipes = recipeService.getRecommendedRecipes();

            if (recipes == null || recipes.isEmpty()) {
                return "🌟 **推荐食谱**\n\n" +
                       "暂无推荐食谱。\n\n" +
                       "💡 稍后再来看看吧！";
            }

            StringBuilder result = new StringBuilder();
            result.append("🌟 **推荐食谱**\n\n");

            for (int i = 0; i < Math.min(10, recipes.size()); i++) {
                Recipe recipe = recipes.get(i);
                result.append(String.format("**%d. %s**\n", i + 1, recipe.getName()));

                if (recipe.getType() != null) {
                    result.append(String.format("   类型：%s\n", recipe.getType()));
                }

                if (recipe.getCalories() != null) {
                    result.append(String.format("   热量：%d kcal\n", recipe.getCalories()));
                }

                if (recipe.getCookTime() != null) {
                    result.append(String.format("   烹饪时间：%s\n", recipe.getCookTime()));
                }

                if (recipe.getDetail() != null && recipe.getDetail().length() > 0) {
                    result.append(String.format("   简介：%s\n",
                        recipe.getDetail().length() > 50 ?
                            recipe.getDetail().substring(0, 50) + "..." :
                            recipe.getDetail()));
                }

                result.append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("获取推荐食谱失败", e);
            return "获取推荐食谱失败：" + e.getMessage();
        }
    }

    /**
     * 搜索菜品
     *
     * @param keyword 搜索关键词
     * @return 搜索结果
     */
    @Tool("搜索菜品，支持按菜名或关键词搜索")
    public String searchDishes(String keyword) {
        log.info("执行工具：searchDishes，关键词：{}", keyword);

        try {
            List<Dish> dishes = dishService.list();

            // 简单的内存过滤
            List<Dish> filtered = dishes.stream()
                    .filter(d -> d.getName() != null && d.getName().contains(keyword))
                    .limit(8)
                    .collect(java.util.stream.Collectors.toList());

            if (filtered.isEmpty()) {
                return String.format("🔍 **搜索结果：\"%s\"**\n\n" +
                       "未找到相关菜品。\n\n" +
                       "💡 换个关键词试试？", keyword);
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("🔍 **搜索结果：\"%s\"**\n\n", keyword));

            for (int i = 0; i < filtered.size(); i++) {
                Dish dish = filtered.get(i);
                result.append(String.format("**%d. %s**\n", i + 1, dish.getName()));
                result.append(String.format("   💰 ¥%.2f | 🔥 %d kcal\n",
                        dish.getPrice(), dish.getCalorie()));

                if (dish.getDescription() != null && !dish.getDescription().isEmpty()) {
                    result.append(String.format("   📝 %s\n",
                        dish.getDescription().length() > 30 ?
                            dish.getDescription().substring(0, 30) + "..." :
                            dish.getDescription()));
                }

                result.append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("搜索菜品失败", e);
            return "搜索菜品失败：" + e.getMessage();
        }
    }

    /**
     * 创建食谱
     *
     * @param userId 用户ID
     * @param name 食谱名称
     * @param type 食谱类型
     * @return 创建结果
     */
    @Tool("创建新的食谱，需要提供食谱名称和类型（早餐/午餐/晚餐/加餐）")
    public String createRecipe(String userId, String name, String type) {
        log.info("执行工具：createRecipe，用户：{}，名称：{}，类型：{}", userId, name, type);

        try {
            Recipe recipe = new Recipe();
            recipe.setUserId(userId);
            recipe.setName(name);
            recipe.setType(type);
            recipe.setFavorite(false);

            Recipe created = recipeService.addRecipe(recipe);

            if (created != null) {
                StringBuilder result = new StringBuilder();
                result.append("✅ **食谱创建成功**\n\n");
                result.append(String.format("**食谱名称：** %s\n", created.getName()));
                result.append(String.format("**食谱类型：** %s\n\n", created.getType()));
                result.append("💡 提示：您可以说「给食谱添加菜品」来添加菜品到食谱中。");

                return result.toString();
            } else {
                return "❌ 创建食谱失败，请稍后重试。";
            }

        } catch (Exception e) {
            log.error("创建食谱失败", e);
            return "创建食谱失败：" + e.getMessage();
        }
    }

    /**
     * 切换食谱收藏状态
     *
     * @param recipeId 食谱ID
     * @return 操作结果
     */
    @Tool("切换食谱的收藏状态（收藏/取消收藏）")
    public String toggleRecipeFavorite(String recipeId) {
        log.info("执行工具：toggleRecipeFavorite，食谱：{}", recipeId);

        try {
            Long id = Long.parseLong(recipeId);
            Recipe updated = recipeService.toggleFavorite(id);

            if (updated != null) {
                String status = updated.getFavorite() != null && updated.getFavorite() ?
                    "已收藏 ⭐" : "已取消收藏";

                return String.format("✅ **操作成功**\n\n" +
                       "食谱「%s」%s", updated.getName(), status);
            } else {
                return "❌ 操作失败，请确认食谱ID是否正确。";
            }

        } catch (NumberFormatException e) {
            return "❌ 食谱ID格式错误，请输入数字ID。";
        } catch (Exception e) {
            log.error("切换收藏状态失败", e);
            return "切换收藏状态失败：" + e.getMessage();
        }
    }
}
