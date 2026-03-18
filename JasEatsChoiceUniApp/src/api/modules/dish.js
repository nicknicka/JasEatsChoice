/**
 * 菜品相关API
 * 对接后端 DishController
 * 基础路径: /v1/dishes
 */
import { get, post, put, del } from '@/utils/request'

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
   */
  getList: (params) => get('/v1/dishes', params),

  /**
   * 获取菜品详情
   * GET /v1/dishes/{dishId}
   * @param {string} dishId - 菜品ID
   */
  getDetail: (dishId) => get(`/v1/dishes/${dishId}`),

  /**
   * 获取商家菜品列表
   * GET /v1/merchants/{merchantId}/dishes
   * @param {string} merchantId - 商家ID
   * @param {Object} params - 查询参数
   * @param {string} params.category - 分类
   * @param {boolean} params.available - 是否仅显示上架菜品
   */
  getMerchantDishes: (merchantId, params) => get(`/v1/merchants/${merchantId}/dishes`, params),

  /**
   * 获取推荐菜品
   * GET /v1/dishes/recommended
   * @param {Object} params - 推荐参数
   * @param {string} params.userId - 用户ID（用于个性化推荐）
   * @param {number} params.limit - 数量限制
   */
  getRecommend: (params) => get('/v1/dishes/recommended', params),

  /**
   * 搜索菜品
   * GET /v1/dishes/search
   * @param {Object} params - 查询参数
   * @param {string} params.keyword - 搜索关键词
   * @param {string} params.merchantId - 商家ID（可选）
   * @param {string} params.category - 分类（可选）
   */
  search: (params) => get('/v1/dishes/search', params),

  /**
   * 获取菜品分类
   * GET /v1/merchants/{merchantId}/categories
   * @param {string} merchantId - 商家ID
   */
  getCategories: (merchantId) => get(`/v1/merchants/${merchantId}/categories`),

  /**
   * 获取菜品食材
   * GET /v1/dishes/{dishId}/ingredients
   * @param {string} dishId - 菜品ID
   */
  getIngredients: (dishId) => get(`/v1/dishes/${dishId}/ingredients`),

  /**
   * 获取菜品营养信息
   * GET /v1/dishes/{dishId}/nutrition
   * @param {string} dishId - 菜品ID
   */
  getNutrition: (dishId) => get(`/v1/dishes/${dishId}/nutrition`),

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
   */
  create: (data) => post('/v1/dishes', data),

  /**
   * 更新菜品（商家端）
   * PUT /v1/dishes/{dishId}
   * @param {string} dishId - 菜品ID
   * @param {Object} data - 菜品数据
   */
  update: (dishId, data) => put(`/v1/dishes/${dishId}`, data),

  /**
   * 删除菜品（商家端）
   * DELETE /v1/dishes/{dishId}
   * @param {string} dishId - 菜品ID
   */
  delete: (dishId) => del(`/v1/dishes/${dishId}`),

  /**
   * 上架/下架菜品（商家端）
   * PUT /v1/dishes/{dishId}/availability
   * @param {string} dishId - 菜品ID
   * @param {boolean} available - 是否上架
   */
  setAvailability: (dishId, available) => put(`/v1/dishes/${dishId}/availability`, { available })
}

export default dishApi
