import { get, post, put, del } from '@/utils/request'

/**
 * 食谱相关API
 */
export const recipeApi = {
  /**
   * 获取今日食谱推荐
   * @param {Object} params - 查询参数
   */
  getToday: (params) => get('/api/recipe/today', params),

  /**
   * 获取我的食谱
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   */
  getMyRecipes: (params) => get('/api/recipe/my', params),

  /**
   * 获取食谱详情
   * @param {number} id - 食谱ID
   */
  getDetail: (id) => get(`/api/recipe/${id}`),

  /**
   * 获取食谱制作步骤
   * @param {number} recipeId - 食谱ID
   */
  getSteps: (recipeId) => get(`/api/recipe/${recipeId}/steps`),

  /**
   * 获取食谱食材列表
   * @param {number} recipeId - 食谱ID
   */
  getIngredients: (recipeId) => get(`/api/recipe/${recipeId}/ingredients`),

  /**
   * 获取食谱营养信息
   * @param {number} recipeId - 食谱ID
   */
  getNutrition: (recipeId) => get(`/api/recipe/${recipeId}/nutrition`),

  /**
   * 搜索食谱
   * @param {Object} params - 查询参数
   * @param {string} params.keyword - 关键词
   * @param {string} params.category - 分类
   * @param {string} params.difficulty - 难度
   */
  search: (params) => get('/api/recipe/search', params),

  /**
   * 收藏食谱
   * @param {number} recipeId - 食谱ID
   */
  favorite: (recipeId) => post(`/api/recipe/${recipeId}/favorite`),

  /**
   * 取消收藏食谱
   * @param {number} recipeId - 食谱ID
   */
  unfavorite: (recipeId) => del(`/api/recipe/${recipeId}/favorite`),

  /**
   * 获取食谱分类
   */
  getCategories: () => get('/api/recipe/categories'),

  /**
   * 获取推荐食谱
   * @param {Object} params - 推荐参数
   */
  getRecommend: (params) => get('/api/recipe/recommend', params)
}
