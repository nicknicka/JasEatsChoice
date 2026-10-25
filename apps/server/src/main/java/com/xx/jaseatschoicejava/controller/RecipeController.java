package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Recipe;
import com.xx.jaseatschoicejava.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 食谱控制器
 */
@RestController
@RequestMapping("/v1/recipe")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;



    /**
     * 获取今日食谱
     */
    @GetMapping("/today")
    public ResponseResult<?> getTodayRecipes(@RequestParam(value = "userId", required = true) String userId) {
        Map<String, Object> result = recipeService.getTodayRecipes(userId);
        return ResponseResult.success(result);
    }

    /**
     * 获取我的食谱
     */
    @GetMapping("/favorite")
    public ResponseResult<?> getFavoriteRecipes(@RequestParam(value = "userId", required = false) String userId) {
        List<Recipe> recipes = recipeService.getFavoriteRecipes(userId);
        return ResponseResult.success(recipes);
    }

    /**
     * 获取推荐食谱
     */
    @GetMapping("/recommend")
    public ResponseResult<?> getRecommendedRecipes() {
        List recipes = recipeService.getRecommendedRecipes();
        return ResponseResult.success(recipes);
    }

    /**
     * 获取用户所有食谱
     */
    @GetMapping("/all")
    public ResponseResult<?> getAllRecipes(@RequestParam(value = "userId", required = true) String userId) {
        List<Recipe> recipes = recipeService.getAllRecipes(userId);
        return ResponseResult.success(recipes);
    }

    /**
     * 新增食谱
     */
    @PostMapping
    public ResponseResult<?> addRecipe(@RequestBody Recipe recipe) {
        Recipe newRecipe = recipeService.addRecipe(recipe);
        return ResponseResult.success(newRecipe);
    }

    /**
     * 更新食谱
     */
    @PutMapping("/{id}")
    public ResponseResult<?> updateRecipe(@PathVariable Long id, @RequestBody Recipe recipe) {
        Recipe updatedRecipe = recipeService.updateRecipe(id, recipe);
        if (updatedRecipe == null) {
            return ResponseResult.fail("404", "食谱不存在");
        }
        return ResponseResult.success(updatedRecipe);
    }

    /**
     * 删除食谱
     */
    @DeleteMapping("/{id}")
    public ResponseResult<?> deleteRecipe(@PathVariable Long id) {
        boolean success = recipeService.deleteRecipe(id);
        if (success) {
            return ResponseResult.success("删除成功");
        } else {
            return ResponseResult.fail("404", "删除失败，食谱不存在");
        }
    }

    /**
     * 切换食谱收藏状态
     */
    @PutMapping("/toggle-favorite/{id}")
    public ResponseResult<?> toggleFavorite(@PathVariable Long id) {
        Recipe updatedRecipe = recipeService.toggleFavorite(id);
        if (updatedRecipe == null) {
            return ResponseResult.fail("404", "食谱不存在");
        }
        return ResponseResult.success(updatedRecipe);
    }

    /**
     * 批量切换食谱收藏状态
     */
    @PutMapping("/batch-toggle-favorite")
    public ResponseResult<?> batchToggleFavorite(@RequestBody Map<String, Object> request) {
        try {
            // 解析请求参数
            List<Long> recipeIds = (List<Long>) request.get("recipeIds");
            boolean favorite = (Boolean) request.get("favorite");

            // 调用服务层方法
            List<Recipe> updatedRecipes = recipeService.batchToggleFavorite(recipeIds, favorite);
            return ResponseResult.success(updatedRecipes);
        } catch (Exception e) {
            return ResponseResult.fail("500", "批量操作失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取食谱详情
     */
    @GetMapping("/{id}")
    public ResponseResult<?> getRecipeDetail(@PathVariable Long id) {
        Recipe recipe = recipeService.getById(id);
        if (recipe != null) {
            return ResponseResult.success(recipe);
        }
        return ResponseResult.fail("404", "食谱不存在");
    }

}
