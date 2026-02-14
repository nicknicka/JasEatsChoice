package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Recipe;
import com.xx.jaseatschoicejava.mapper.RecipeMapper;
import com.xx.jaseatschoicejava.service.RecipeService;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 食谱Service实现
 */
@Service
public class RecipeServiceImpl extends ServiceImpl<RecipeMapper, Recipe> implements RecipeService {

    Logger logger = LoggerFactory.getLogger(RecipeServiceImpl.class);

    @Override
    public Map<String, Object> getTodayRecipes(String userId) {
        QueryWrapper<Recipe> queryWrapper = new QueryWrapper<>();

        // 1. 查询当前用户的所有食谱
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        } else {
            logger.error("请求食谱的用户ID为null，请检查！");
        }

        // 2. 根据createTime筛选出今日的食谱
        LocalDate today = LocalDate.now();
        // 使用ge和le方法筛选出今天的所有食谱
        queryWrapper.ge("create_time", today.atStartOfDay());
        queryWrapper.le("create_time", today.plusDays(1).atStartOfDay());

        // 3. 按照updateTime排序
        queryWrapper.orderByDesc("update_time");

        List<Recipe> recipes = list(queryWrapper);
        logger.info("返回的recipes: {}", recipes);
        // 确保recipes不为null
        List<Recipe> safeRecipes = recipes != null ? recipes : new ArrayList<>();

        // 按照餐点类型分组，items现在是String类型
        Map<String, List<String>> recipesByType = safeRecipes.stream()
                .collect(Collectors.groupingBy(
                        Recipe::getType,
                        Collectors.mapping(Recipe::getItems, Collectors.toList())
                ));

        // 计算营养总摄入
        int totalCalories = safeRecipes.stream().mapToInt(Recipe::getCalories).sum();
        int totalProtein = safeRecipes.stream().mapToInt(Recipe::getProtein).sum();
        int totalCarbs = safeRecipes.stream().mapToInt(Recipe::getCarbs).sum();
        int totalFat = safeRecipes.stream().mapToInt(Recipe::getFat).sum();

        // 营养摄入数据
        Map<String, Object> nutrition = new HashMap<>();
        nutrition.put("calories", totalCalories);
        nutrition.put("protein", totalProtein);
        nutrition.put("carbs", totalCarbs);
        nutrition.put("fat", totalFat);

        // 构造响应数据
        Map<String, Object> result = new HashMap<>();
        result.put("recipes", safeRecipes);
        result.put("nutrition", nutrition);
        result.put("date", LocalDateTime.now());

        // 添加recipesByType到响应中（如果前端需要）
        result.put("recipesByType", recipesByType);

        logger.info("返回的recipesByType: {}", recipesByType);
        logger.info("返回的result: {}", result);
        return result;
    }

    @Override
    public List<Recipe> getFavoriteRecipes(String userId) {
        // 查询收藏的食谱
        QueryWrapper<Recipe> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_favorite", true);
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        queryWrapper.orderByDesc("update_time");

        return list(queryWrapper);
    }

    @Override
    public List<Recipe> getRecommendedRecipes() {
        // 查询推荐的食谱
        QueryWrapper<Recipe> queryWrapper = new QueryWrapper<>();
        // 这里可以根据实际业务逻辑调整推荐算法，比如按卡路里排序或随机推荐
        queryWrapper.orderByAsc("calories");
        queryWrapper.last("LIMIT 3"); // 只返回前3条

        return list(queryWrapper);
    }

    @Override
    public List<Recipe> getAllRecipes(String userId) {
        QueryWrapper<Recipe> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("create_time");
        return list(queryWrapper);
    }

    @Override
    public Recipe addRecipe(Recipe recipe) {
        recipe.setCreateTime(LocalDateTime.now());
        recipe.setUpdateTime(LocalDateTime.now());
        if (recipe.getFavorite() == null) {
            recipe.setFavorite(false);
        }
        // 为items字段设置默认值为空JSON字符串，避免数据库报错
        if (recipe.getItems() == null) {
            recipe.setItems("[]");
        }
        // 为营养相关字段设置默认值为0，避免数据库报错
        if (recipe.getCalories() == null) {
            recipe.setCalories(0);
        }
        if (recipe.getProtein() == null) {
            recipe.setProtein(0);
        }
        if (recipe.getCarbs() == null) {
            recipe.setCarbs(0);
        }
        if (recipe.getFat() == null) {
            recipe.setFat(0);
        }
        // 为cookTime字段设置默认值为空字符串
        if (recipe.getCookTime() == null) {
            recipe.setCookTime("");
        }
        save(recipe);
        return recipe;
    }

    @Override
    public Recipe updateRecipe(Long id, Recipe recipe) {
        Recipe existingRecipe = getById(id);
        if (existingRecipe == null) {
            return null;
        }
        existingRecipe.setName(recipe.getName());
        existingRecipe.setType(recipe.getType());
        existingRecipe.setItems(recipe.getItems());
        existingRecipe.setDetail(recipe.getDetail());
        existingRecipe.setCalories(recipe.getCalories());
        existingRecipe.setProtein(recipe.getProtein());
        existingRecipe.setCarbs(recipe.getCarbs());
        existingRecipe.setFat(recipe.getFat());
        existingRecipe.setCookTime(recipe.getCookTime());
        existingRecipe.setFavorite(recipe.getFavorite());
        existingRecipe.setUpdateTime(LocalDateTime.now());
        updateById(existingRecipe);
        return existingRecipe;
    }

    @Override
    public boolean deleteRecipe(Long id) {
        return removeById(id);
    }

    @Override
    public Recipe toggleFavorite(Long id) {
        Recipe recipe = getById(id);
        if (recipe == null) {
            return null;
        }
        recipe.setFavorite(!recipe.getFavorite());
        recipe.setUpdateTime(LocalDateTime.now());
        updateById(recipe);
        return recipe;
    }

    @Override
    public List<Recipe> batchToggleFavorite(List<Long> recipeIds, boolean favorite) {
        // 查询所有要更新的食谱
        List<Recipe> recipes = listByIds(recipeIds);
        if (recipes == null || recipes.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量更新收藏状态和时间
        LocalDateTime now = LocalDateTime.now();
        for (Recipe recipe : recipes) {
            recipe.setFavorite(favorite);
            recipe.setUpdateTime(now);
        }

        // 批量更新数据库
        updateBatchById(recipes);

        return recipes;
    }

}
