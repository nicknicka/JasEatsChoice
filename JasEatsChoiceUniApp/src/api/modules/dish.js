/**
 * 菜品相关API
 * 对接后端 DishController
 * 基础路径: /v1/dishes
 */
import { get, post, put, del } from '@/utils/request'
import { DISH_API, buildUrl } from '../urlEnum'

export const dishApi = {
  /**
   * 获取菜品列表
   * GET /v1/dishes
   * @param {Object} params - 查询参数
   * @param {string} params.merchantId - 商家ID
   * @param {string} params.category - 分类
   * @param {string} params.keyword - 搜索关键词
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回菜品列表
   */
  getList: (params) => get(DISH_API.GET_LIST, params),

  /**
   * 获取菜品详情
   * GET /v1/dishes/{dishId}
   * @param {string} dishId - 菜品ID
   * @returns {Promise} 返回菜品详情
   */
  getDetail: (dishId) => get(buildUrl(DISH_API.GET_DETAIL, { dishId })),

  /**
   * 获取商家菜品列表
   * GET /v1/merchants/{merchantId}/dishes
   * @param {string} merchantId - 商家ID
   * @param {Object} params - 查询参数
   * @param {string} params.category - 分类
   * @param {boolean} params.available - 是否仅显示上架菜品
   * @returns {Promise} 返回菜品列表
   */
  getMerchantDishes: (merchantId, params) => get(buildUrl('/v1/merchants/:merchantId/dishes', { merchantId }), params),

  /**
   * 获取推荐菜品
   * GET /v1/dishes/recommend
   * @param {Object} params - 推荐参数
   * @param {string} params.userId - 用户ID（用于个性化推荐）
   * @param {number} params.limit - 数量限制
   * @returns {Promise} 返回推荐菜品列表
   */
  getRecommend: (params) => get(DISH_API.GET_RECOMMEND, params),

  /**
   * 搜索菜品
   * GET /v1/dishes/search
   * @param {Object} params - 查询参数
   * @param {string} params.keyword - 搜索关键词
   * @param {string} params.merchantId - 商家ID（可选）
   * @param {string} params.category - 分类（可选）
   * @returns {Promise} 返回搜索结果
   */
  search: (params) => get(DISH_API.SEARCH, params),

  /**
   * 获取菜品分类
   * GET /v1/dishes/categories
   * @returns {Promise} 返回分类列表
   */
  getCategories: () => get(DISH_API.GET_CATEGORIES),

  /**
   * 获取菜品食材
   * GET /v1/dishes/{dishId}/ingredients
   * @param {string} dishId - 菜品ID
   * @returns {Promise} 返回食材列表
   */
  getIngredients: (dishId) => get(buildUrl('/v1/dishes/:dishId/ingredients', { dishId })),

  /**
   * 获取菜品营养信息
   * GET /v1/dishes/{dishId}/nutrition
   * @param {string} dishId - 菜品ID
   * @returns {Promise} 返回营养信息
   */
  getNutrition: (dishId) => get(buildUrl('/v1/dishes/:dishId/nutrition', { dishId })),

  /**
   * 创建菜品（商家端）
   * POST /v1/dishes
   * @param {Object} data - 菜品数据
   * @param {string} data.merchantId - 商家ID
   * @param {string} data.name - 菜品名称
   * @param {string} data.category - 分类
   * @param {number} data.price - 价格
   * @param {string} data.description - 描述
   * @param {string} data.image - 图片URL
   * @param {number} data.calories - 卡路里
   * @param {boolean} data.available - 是否上架
   * @returns {Promise} 返回创建结果
   */
  create: (data) => post(DISH_API.CREATE, data),

  /**
   * 更新菜品（商家端）
   * PUT /v1/dishes/{dishId}
   * @param {string} dishId - 菜品ID
   * @param {Object} data - 菜品数据
   * @returns {Promise} 返回更新结果
   */
  update: (dishId, data) => put(buildUrl(DISH_API.UPDATE, { dishId }), data),

  /**
   * 删除菜品（商家端）
   * DELETE /v1/dishes/{dishId}
   * @param {string} dishId - 菜品ID
   * @returns {Promise} 返回删除结果
   */
  delete: (dishId) => del(buildUrl(DISH_API.DELETE, { dishId })),

  /**
   * 上架/下架菜品（商家端）
   * PUT /v1/dishes/{dishId}/availability
   * @param {string} dishId - 菜品ID
   * @param {boolean} available - 是否上架
   * @returns {Promise} 返回更新结果
   */
  setAvailability: (dishId, available) => put(buildUrl('/v1/dishes/:dishId/availability', { dishId }), { available })
}

export default dishApi
