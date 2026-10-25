package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.Recipe;

import java.util.List;
import java.util.Map;

/**
 * 食谱Service
 */
public interface RecipeService extends IService<Recipe> {

    /**
     * 获取今日食谱
     * @param userId 用户ID
     * @return 今日食谱和营养信息
     */
    Map<String, Object> getTodayRecipes(String userId);

    /**
     * 获取我的食谱
     * @param userId 用户ID
     * @return 我的食谱列表
     */
    List<Recipe> getFavoriteRecipes(String userId);

    /**
     * 获取用户所有食谱
     * @param userId 用户ID
     * @return 用户所有食谱列表
     */
    List<Recipe> getAllRecipes(String userId);

    /**
     * 获取推荐食谱
     * @return 推荐食谱列表
     */
    List<Recipe> getRecommendedRecipes();

    /**
     * 新增食谱
     * @param recipe 食谱信息
     * @return 新增的食谱
     */
    Recipe addRecipe(Recipe recipe);

    /**
     * 更新食谱
     * @param id 食谱ID
     * @param recipe 食谱信息
     * @return 更新后的食谱
     */
    Recipe updateRecipe(Long id, Recipe recipe);

    /**
     * 删除食谱
     * @param id 食谱ID
     * @return 是否删除成功
     */
    boolean deleteRecipe(Long id);

    /**
     * 切换食谱收藏状态
     * @param id 食谱ID
     * @return 更新后的食谱
     */
    Recipe toggleFavorite(Long id);

    /**
     * 批量切换食谱收藏状态
     * @param recipeIds 食谱ID列表
     * @param favorite 是否收藏
     * @return 更新后的食谱列表
     */
    List<Recipe> batchToggleFavorite(List<Long> recipeIds, boolean favorite);

}
