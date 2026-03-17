/**
 * 食谱相关API
 */
import request from '@/utils/request'

/**
 * 获取今日食谱推荐
 */
export const getTodayRecipe = () => {
  return request({
    url: '/api/recipe/today',
    method: 'GET'
  })
}

/**
 * 获取我的食谱列表
 */
export const getMyRecipes = (params) => {
  return request({
    url: '/api/recipe/my',
    method: 'GET',
    params
  })
}

/**
 * 获取食谱详情
 */
export const getRecipeDetail = (recipeId) => {
  return request({
    url: `/api/recipe/${recipeId}`,
    method: 'GET'
  })
}

/**
 * 创建自定义食谱
 */
export const createRecipe = (data) => {
  return request({
    url: '/api/recipe/create',
    method: 'POST',
    data
  })
}

/**
 * 更新食谱
 */
export const updateRecipe = (recipeId, data) => {
  return request({
    url: `/api/recipe/${recipeId}`,
    method: 'PUT',
    data
  })
}

/**
 * 删除食谱
 */
export const deleteRecipe = (recipeId) => {
  return request({
    url: `/api/recipe/${recipeId}`,
    method: 'DELETE'
  })
}

/**
 * 推荐食谱
 */
export const recommendRecipe = (params) => {
  return request({
    url: '/api/recipe/recommend',
    method: 'GET',
    params
  })
}

/**
 * 获取食谱制作步骤
 */
export const getRecipeSteps = (recipeId) => {
  return request({
    url: `/api/recipe/${recipeId}/steps`,
    method: 'GET'
  })
}

/**
 * 收藏食谱
 */
export const favoriteRecipe = (recipeId) => {
  return request({
    url: `/api/recipe/${recipeId}/favorite`,
    method: 'POST'
  })
}

/**
 * 取消收藏食谱
 */
export const unfavoriteRecipe = (recipeId) => {
  return request({
    url: `/api/recipe/${recipeId}/favorite`,
    method: 'DELETE'
  })
}

/**
 * 分享食谱
 */
export const shareRecipe = (recipeId) => {
  return request({
    url: `/api/recipe/${recipeId}/share`,
    method: 'POST'
  })
}

/**
 * 获取营养分析
 */
export const getNutritionAnalysis = (params) => {
  return request({
    url: '/api/recipe/nutrition',
    method: 'GET',
    params
  })
}

export default {
  getTodayRecipe,
  getMyRecipes,
  getRecipeDetail,
  createRecipe,
  updateRecipe,
  deleteRecipe,
  recommendRecipe,
  getRecipeSteps,
  favoriteRecipe,
  unfavoriteRecipe,
  shareRecipe,
  getNutritionAnalysis
}
