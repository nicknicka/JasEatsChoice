/**
 * 食谱相关API
 * 对接后端 RecipeController
 * 基础路径: /api/recipe
 */
import { get, post, put, del } from '@/utils/request'
import { RECIPE_API, buildUrl } from '../urlEnum'

export const recipeApi = {
  /**
   * 获取今日食谱推荐
   * GET /api/recipe/today
   * @returns {Promise} 返回今日食谱
   */
  getTodayRecipe: () => get(RECIPE_API.GET_TODAY_RECIPE),

  /**
   * 获取今日食谱（别名）
   * @param {Object} params - 查询参数
   * @returns {Promise} 返回今日食谱
   */
  getToday: (params) => get(RECIPE_API.GET_TODAY_RECIPE, params),

  /**
   * 获取我的食谱列表
   * GET /api/recipe/my
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回食谱列表
   */
  getMyRecipes: (params) => get(RECIPE_API.GET_MY_RECIPES, params),

  /**
   * 获取食谱详情
   * GET /api/recipe/{recipeId}
   * @param {string} recipeId - 食谱ID
   * @returns {Promise} 返回食谱详情
   */
  getRecipeDetail: (recipeId) => get(buildUrl(RECIPE_API.GET_RECIPE_DETAIL, { recipeId })),

  /**
   * 获取食谱详情（别名）
   * @param {number} id - 食谱ID
   * @returns {Promise} 返回食谱详情
   */
  getDetail: (id) => get(`/api/recipe/${id}`),

  /**
   * 获取食谱制作步骤
   * GET /api/recipe/{recipeId}/steps
   * @param {string} recipeId - 食谱ID
   * @returns {Promise} 返回制作步骤
   */
  getRecipeSteps: (recipeId) => get(buildUrl(RECIPE_API.GET_RECIPE_STEPS, { recipeId })),

  /**
   * 获取食谱制作步骤（别名）
   * @param {number} recipeId - 食谱ID
   * @returns {Promise} 返回制作步骤
   */
  getSteps: (recipeId) => get(`/api/recipe/${recipeId}/steps`),

  /**
   * 获取食谱食材列表
   * GET /api/recipe/{recipeId}/ingredients
   * @param {string} recipeId - 食谱ID
   * @returns {Promise} 返回食材列表
   */
  getIngredients: (recipeId) => get(`/api/recipe/${recipeId}/ingredients`),

  /**
   * 获取食谱营养信息
   * GET /api/recipe/{recipeId}/nutrition
   * @param {string} recipeId - 食谱ID
   * @returns {Promise} 返回营养信息
   */
  getNutrition: (recipeId) => get(`/api/recipe/${recipeId}/nutrition`),

  /**
   * 创建自定义食谱
   * POST /api/recipe/create
   * @param {Object} data - 食谱数据
   * @param {string} data.name - 食谱名称
   * @param {string} data.description - 描述
   * @param {Array} data.ingredients - 食材列表
   * @param {Array} data.steps - 制作步骤
   * @param {Array} data.images - 图片列表
   * @param {number} data.calories - 卡路里
   * @param {Array} data.tags - 标签
   * @returns {Promise} 返回创建结果
   */
  createRecipe: (data) => post(RECIPE_API.CREATE_RECIPE, data),

  /**
   * 更新食谱
   * PUT /api/recipe/{recipeId}
   * @param {string} recipeId - 食谱ID
   * @param {Object} data - 食谱数据
   * @returns {Promise} 返回更新结果
   */
  updateRecipe: (recipeId, data) => put(buildUrl(RECIPE_API.UPDATE_RECIPE, { recipeId }), data),

  /**
   * 删除食谱
   * DELETE /api/recipe/{recipeId}
   * @param {string} recipeId - 食谱ID
   * @returns {Promise} 返回删除结果
   */
  deleteRecipe: (recipeId) => del(buildUrl(RECIPE_API.DELETE_RECIPE, { recipeId })),

  /**
   * 搜索食谱
   * GET /api/recipe/search
   * @param {Object} params - 查询参数
   * @param {string} params.keyword - 关键词
   * @param {string} params.category - 分类
   * @param {string} params.difficulty - 难度
   * @returns {Promise} 返回搜索结果
   */
  search: (params) => get('/api/recipe/search', params),

  /**
   * 推荐食谱
   * GET /api/recipe/recommend
   * @param {Object} params - 推荐参数
   * @param {string} params.userId - 用户ID（可选）
   * @param {string} params.preference - 偏好（可选）
   * @param {number} params.limit - 数量限制（可选）
   * @returns {Promise} 返回推荐食谱列表
   */
  recommendRecipe: (params) => get(RECIPE_API.RECOMMEND_RECIPE, params),

  /**
   * 推荐食谱（别名）
   * @param {Object} params - 推荐参数
   * @returns {Promise} 返回推荐食谱列表
   */
  getRecommend: (params) => get(RECIPE_API.RECOMMEND_RECIPE, params),

  /**
   * 收藏食谱
   * POST /api/recipe/{recipeId}/favorite
   * @param {string} recipeId - 食谱ID
   * @returns {Promise} 返回收藏结果
   */
  favoriteRecipe: (recipeId) => post(buildUrl(RECIPE_API.FAVORITE_RECIPE, { recipeId })),

  /**
   * 收藏食谱（别名）
   * @param {number} recipeId - 食谱ID
   * @returns {Promise} 返回收藏结果
   */
  favorite: (recipeId) => post(`/api/recipe/${recipeId}/favorite`),

  /**
   * 取消收藏食谱
   * DELETE /api/recipe/{recipeId}/favorite
   * @param {string} recipeId - 食谱ID
   * @returns {Promise} 返回取消收藏结果
   */
  unfavoriteRecipe: (recipeId) => del(buildUrl(RECIPE_API.UNFAVORITE_RECIPE, { recipeId })),

  /**
   * 取消收藏食谱（别名）
   * @param {number} recipeId - 食谱ID
   * @returns {Promise} 返回取消收藏结果
   */
  unfavorite: (recipeId) => del(`/api/recipe/${recipeId}/favorite`),

  /**
   * 分享食谱
   * POST /api/recipe/{recipeId}/share
   * @param {string} recipeId - 食谱ID
   * @returns {Promise} 返回分享结果
   */
  shareRecipe: (recipeId) => post(buildUrl(RECIPE_API.SHARE_RECIPE, { recipeId })),

  /**
   * 获取食谱分类
   * GET /api/recipe/categories
   * @returns {Promise} 返回分类列表
   */
  getCategories: () => get('/api/recipe/categories'),

  /**
   * 获取营养分析
   * GET /api/recipe/nutrition
   * @param {Object} params - 查询参数
   * @param {string} params.recipeId - 食谱ID
   * @returns {Promise} 返回营养分析
   */
  getNutritionAnalysis: (params) => get(RECIPE_API.GET_NUTRITION, params)
}

export default recipeApi
