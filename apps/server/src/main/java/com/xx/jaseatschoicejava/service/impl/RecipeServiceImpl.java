package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.entity.ContentExtraction;
import com.xx.jaseatschoicejava.entity.ContentSource;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 食谱Service实现
 */
@Service
public class RecipeServiceImpl extends ServiceImpl<RecipeMapper, Recipe> implements RecipeService {

    Logger logger = LoggerFactory.getLogger(RecipeServiceImpl.class);
    private static final Set<String> MEAL_TYPES = Set.of("breakfast", "lunch", "dinner", "snack");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> getTodayRecipes(String userId) {
        QueryWrapper<Recipe> queryWrapper = new QueryWrapper<>();

        // 1. 查询当前用户的所有食谱
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        } else {
            logger.error("请求食谱的用户ID为null，请检查！");
        }

        // 2. 根据createdAt筛选出今日的食谱
        LocalDate today = LocalDate.now();
        // 使用ge和le方法筛选出今天的所有食谱
        queryWrapper.ge("created_at", today.atStartOfDay());
        queryWrapper.le("created_at", today.plusDays(1).atStartOfDay());

        // 3. 按照updatedAt排序
        queryWrapper.orderByDesc("updated_at");

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
        queryWrapper.orderByDesc("updated_at");

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
        queryWrapper.orderByDesc("created_at");
        return list(queryWrapper);
    }

    @Override
    public Recipe addRecipe(Recipe recipe) {
        recipe.setCreatedAt(LocalDateTime.now());
        recipe.setUpdatedAt(LocalDateTime.now());
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
    public Recipe updateRecipe(String id, Recipe recipe) {
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
        existingRecipe.setCustomNutrition(recipe.getCustomNutrition());
        existingRecipe.setUpdatedAt(LocalDateTime.now());
        updateById(existingRecipe);
        return existingRecipe;
    }

    @Override
    public boolean deleteRecipe(String id) {
        return removeById(id);
    }

    @Override
    public Recipe toggleFavorite(String id) {
        Recipe recipe = getById(id);
        if (recipe == null) {
            return null;
        }
        recipe.setFavorite(!recipe.getFavorite());
        recipe.setUpdatedAt(LocalDateTime.now());
        updateById(recipe);
        return recipe;
    }

    @Override
    public Recipe createFromExtraction(ContentExtraction extraction, ContentSource source, String mealType) {
        String resolvedMealType = mealType == null || mealType.isBlank() ? "lunch" : mealType.trim();
        if (!MEAL_TYPES.contains(resolvedMealType)) {
            throw new IllegalArgumentException("餐次不合法");
        }

        Recipe recipe = new Recipe();
        recipe.setUserId(source.getUserId());
        recipe.setName(extraction.getDishName() == null || extraction.getDishName().isBlank()
            ? "未命名菜品" : extraction.getDishName());
        recipe.setType(resolvedMealType);
        recipe.setItems(buildItems(extraction));
        recipe.setCalories(extraction.getCalories() == null ? 0 : extraction.getCalories());
        recipe.setProtein(0);
        recipe.setCarbs(0);
        recipe.setFat(0);
        recipe.setDetail(buildDetail(extraction));
        recipe.setCookTime(extraction.getCookingTime() == null ? "" : extraction.getCookingTime() + "分钟");
        recipe.setFavorite(false);
        recipe.setSourceUrl(source.getContentUrl());
        recipe.setSourcePlatform(source.getPlatform());
        addRecipe(recipe);
        return recipe;
    }

    private String buildItems(ContentExtraction extraction) {
        List<String> ingredients = new ArrayList<>();
        if (extraction.getIngredients() != null && !extraction.getIngredients().isBlank()) {
            try {
                List<Map<String, Object>> parsed = objectMapper.readValue(
                    extraction.getIngredients(),
                    new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
                );
                for (Map<String, Object> ingredient : parsed) {
                    String name = valueAsString(ingredient.get("name"));
                    String amount = valueAsString(ingredient.get("amount"));
                    if (!name.isBlank()) {
                        ingredients.add(amount.isBlank() ? name : name + " " + amount);
                    }
                }
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("食材数据格式不合法", e);
            }
        }

        Map<String, Object> dish = new HashMap<>();
        dish.put("name", extraction.getDishName());
        dish.put("ingredients", ingredients);
        dish.put("calories", extraction.getCalories() == null ? 0 : extraction.getCalories());
        dish.put("protein", 0);
        dish.put("carbs", 0);
        dish.put("fat", 0);
        try {
            return objectMapper.writeValueAsString(List.of(dish));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("食谱菜品数据序列化失败", e);
        }
    }

    private String buildDetail(ContentExtraction extraction) {
        StringBuilder detail = new StringBuilder();
        if (extraction.getDescription() != null && !extraction.getDescription().isBlank()) {
            detail.append(extraction.getDescription().trim());
        }
        if (extraction.getStepsDescription() != null && !extraction.getStepsDescription().isBlank()) {
            appendSection(detail, extraction.getStepsDescription().trim());
        }
        if (extraction.getSteps() != null && !extraction.getSteps().isBlank()) {
            try {
                List<Map<String, Object>> steps = objectMapper.readValue(
                    extraction.getSteps(),
                    new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
                );
                List<String> formattedSteps = new ArrayList<>();
                for (int i = 0; i < steps.size(); i++) {
                    Map<String, Object> step = steps.get(i);
                    String title = valueAsString(step.get("title"));
                    String description = valueAsString(step.get("description"));
                    int stepNumber = extractStepNumber(step.get("stepNumber"), i + 1);
                    String text = title;
                    if (!description.isBlank()) {
                        text = text.isBlank() ? description : title + "：" + description;
                    }
                    if (!text.isBlank()) {
                        formattedSteps.add(stepNumber + ". " + text);
                    }
                }
                if (!formattedSteps.isEmpty()) {
                    appendSection(detail, "制作步骤：\n" + String.join("\n", formattedSteps));
                }
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("步骤数据格式不合法", e);
            }
        }
        return detail.toString();
    }

    private void appendSection(StringBuilder detail, String section) {
        if (!detail.isEmpty()) {
            detail.append("\n\n");
        }
        detail.append(section);
    }

    private String valueAsString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private int extractStepNumber(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException ignored) {
                // 使用列表位置作为兜底序号
            }
        }
        return defaultValue;
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
            recipe.setUpdatedAt(now);
        }

        // 批量更新数据库
        updateBatchById(recipes);

        return recipes;
    }

}
