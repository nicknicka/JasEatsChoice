/**
 * 菜品相关API
 * 对接后端 DishController，基础路径 /v1/dishes
 *
 * 路径以 DishController 里真实的 @GetMapping/@PostMapping/@PutMapping 为准，后端只有：
 *   GET    /v1/dishes                      列表（keyword/category/merchantId）
 *   GET    /v1/dishes/categories           分类
 *   GET    /v1/dishes/merchant/{merchantId} 商家菜品（只返回已上架）
 *   GET    /v1/dishes/{dishId}             详情（含 ingredients/nutrition）
 *   GET    /v1/dishes/recommended          推荐
 *   POST   /v1/dishes                      新建
 *   PUT    /v1/dishes/{dishId}             更新
 *   PUT    /v1/dishes/{dishId}/status      上架/下架（请求体 { status: boolean }）
 *   PUT    /v1/dishes/batch                批量删除（请求体 { dishIds: [...] }）
 *
 * 没有 /v1/merchants/{merchantId}/dishes、/v1/dishes/search、/v1/dishes/{dishId}/ingredients、
 * /v1/dishes/{dishId}/nutrition，也没有单菜品 DELETE 路由；下面已按真实路由修正。
 */
import { get, post, put } from '@/utils/request'
import { DISH_API, buildUrl } from '../urlEnum'

export const dishApi = {
  /**
   * 获取菜品列表
   * GET /v1/dishes
   * @param {Object} params - 查询参数
   * @param {string} params.merchantId - 商家ID
   * @param {string} params.category - 分类
   * @param {string} params.keyword - 搜索关键词
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
   * GET /v1/dishes/merchant/{merchantId}
   * 注意：后端只返回已上架菜品，且不接受 category/available 过滤参数（多传的查询参数后端会忽略）。
   * @param {string} merchantId - 商家ID
   * @param {Object} params - 查询参数
   * @returns {Promise} 返回菜品列表
   */
  getMerchantDishes: (merchantId, params) => get(buildUrl('/v1/dishes/merchant/:merchantId', { merchantId }), params),

  /**
   * 获取推荐菜品
   * GET /v1/dishes/recommended
   * @param {Object} params - 推荐参数
   * @param {string} params.userId - 用户ID（用于个性化推荐）
   * @param {number} params.limit - 数量限制
   * @returns {Promise} 返回推荐菜品列表
   */
  getRecommend: (params) => get(DISH_API.GET_RECOMMEND, params),

  /**
   * 搜索菜品
   * 后端没有 /v1/dishes/search，搜索由列表接口的 keyword 参数承担（支持 keyword/category/merchantId）。
   * @param {Object} params - 查询参数
   * @param {string} params.keyword - 搜索关键词
   * @param {string} params.merchantId - 商家ID（可选）
   * @param {string} params.category - 分类（可选）
   * @returns {Promise} 返回搜索结果
   */
  search: (params) => get(DISH_API.GET_LIST, params),

  /**
   * 获取菜品分类
   * GET /v1/dishes/categories
   * @returns {Promise} 返回分类列表
   */
  getCategories: (params) => get(DISH_API.GET_CATEGORIES, params),

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
   * 后端没有单菜品删除路由，只有批量删除 PUT /v1/dishes/batch（请求体 { dishIds: [...] }），
   * 这里用单元素数组复用批量删除接口。
   * @param {string} dishId - 菜品ID
   * @returns {Promise} 返回删除结果
   */
  delete: (dishId) => put('/v1/dishes/batch', { dishIds: [dishId] }),

  /**
   * 上架/下架菜品（商家端）
   * PUT /v1/dishes/{dishId}/status，后端读的是请求体里的 status 布尔值
   * @param {string} dishId - 菜品ID
   * @param {boolean} available - 是否上架
   * @returns {Promise} 返回更新结果
   */
  setAvailability: (dishId, available) => put(buildUrl('/v1/dishes/:dishId/status', { dishId }), { status: available })
}

export default dishApi
