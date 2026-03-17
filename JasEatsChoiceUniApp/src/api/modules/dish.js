import { get, post, put, del } from '@/utils/request'

/**
 * 菜品相关API
 */
export const dishApi = {
  /**
   * 获取菜品列表
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.category - 分类
   * @param {string} params.keyword - 搜索关键词
   */
  getList: (params) => get('/api/dish/list', params),

  /**
   * 获取菜品详情
   * @param {number} id - 菜品ID
   */
  getDetail: (id) => get(`/api/dish/${id}`),

  /**
   * 获取商家菜品列表
   * @param {number} merchantId - 商家ID
   * @param {Object} params - 查询参数
   */
  getMerchantDishes: (merchantId, params) => get(`/api/merchant/${merchantId}/dishes`, params),

  /**
   * 获取推荐菜品
   * @param {Object} params - 推荐参数
   * @param {number} params.limit - 数量限制
   */
  getRecommend: (params) => get('/api/recommend/dishes', params),

  /**
   * 搜索菜品
   * @param {string} keyword - 搜索关键词
   * @param {Object} params - 其他查询参数
   */
  search: (keyword, params) => get('/api/dish/search', { keyword, ...params }),

  /**
   * 获取菜品分类
   * @param {number} merchantId - 商家ID
   */
  getCategories: (merchantId) => get(`/api/merchant/${merchantId}/categories`),

  /**
   * 获取菜品食材
   * @param {number} dishId - 菜品ID
   */
  getIngredients: (dishId) => get(`/api/dish/${dishId}/ingredients`),

  /**
   * 获取菜品营养信息
   * @param {number} dishId - 菜品ID
   */
  getNutrition: (dishId) => get(`/api/dish/${dishId}/nutrition`)
}
