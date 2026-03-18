/**
 * 收藏相关API
 * 对接后端 FavoriteController
 * 基础路径: /v1/favorites
 */
import { get, post, put, del } from '@/utils/request'

export const favoriteApi = {
  /**
   * 获取收藏列表
   * GET /v1/favorites
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {string} params.type - 类型(dish/merchant/recipe)
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   */
  getList: (params) => get('/v1/favorites', params),

  /**
   * 获取收藏菜品列表
   * GET /v1/favorites/dishes
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   */
  getDishList: (params) => get('/v1/favorites/dishes', params),

  /**
   * 获取收藏食谱列表
   * GET /v1/favorites/recipes
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   */
  getRecipeList: (params) => get('/v1/favorites/recipes', params),

  /**
   * 检查菜品是否收藏
   * GET /v1/favorites/dishes/{dishId}/check
   * @param {string} dishId - 菜品ID
   * @param {string} userId - 用户ID
   */
  checkDish: (dishId, userId) => get(`/v1/favorites/dishes/${dishId}/check`, { userId }),

  /**
   * 收藏菜品
   * POST /v1/favorites/dishes
   * @param {Object} data - 收藏数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.dishId - 菜品ID
   */
  addDish: (data) => post('/v1/favorites/dishes', data),

  /**
   * 取消收藏菜品
   * DELETE /v1/favorites/dishes/{dishId}
   * @param {string} dishId - 菜品ID
   * @param {string} userId - 用户ID
   */
  removeDish: (dishId, userId) => del(`/v1/favorites/dishes/${dishId}`, { userId }),

  /**
   * 检查食谱是否收藏
   * GET /v1/favorites/recipes/{recipeId}/check
   * @param {string} recipeId - 食谱ID
   * @param {string} userId - 用户ID
   */
  checkRecipe: (recipeId, userId) => get(`/v1/favorites/recipes/${recipeId}/check`, { userId }),

  /**
   * 收藏食谱
   * POST /v1/favorites/recipes
   * @param {Object} data - 收藏数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.recipeId - 食谱ID
   */
  addRecipe: (data) => post('/v1/favorites/recipes', data),

  /**
   * 取消收藏食谱
   * DELETE /v1/favorites/recipes/{recipeId}
   * @param {string} recipeId - 食谱ID
   * @param {string} userId - 用户ID
   */
  removeRecipe: (recipeId, userId) => del(`/v1/favorites/recipes/${recipeId}`, { userId }),

  /**
   * 检查商家是否收藏
   * GET /v1/favorites/merchants/{merchantId}/check
   * @param {string} merchantId - 商家ID
   * @param {string} userId - 用户ID
   */
  checkMerchant: (merchantId, userId) => get(`/v1/favorites/merchants/${merchantId}/check`, { userId }),

  /**
   * 收藏商家
   * POST /v1/favorites/merchants
   * @param {Object} data - 收藏数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.merchantId - 商家ID
   */
  addMerchant: (data) => post('/v1/favorites/merchants', data),

  /**
   * 取消收藏商家
   * DELETE /v1/favorites/merchants/{merchantId}
   * @param {string} merchantId - 商家ID
   * @param {string} userId - 用户ID
   */
  removeMerchant: (merchantId, userId) => del(`/v1/favorites/merchants/${merchantId}`, { userId }),

  /**
   * 切换收藏状态（统一接口）
   * POST /v1/favorites/toggle
   * @param {Object} data - 切换数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.type - 类型(dish/merchant/recipe)
   * @param {string} data.targetId - 目标ID
   * @param {boolean} data.isFavorite - 是否收藏
   */
  toggle: (data) => post('/v1/favorites/toggle', data),

  /**
   * 批量删除收藏
   * DELETE /v1/favorites/batch
   * @param {Object} data - 删除数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.type - 类型(dish/merchant/recipe)
   * @param {Array} data.ids - ID列表
   */
  batchRemove: (data) => del('/v1/favorites/batch', data)
}

export default favoriteApi
