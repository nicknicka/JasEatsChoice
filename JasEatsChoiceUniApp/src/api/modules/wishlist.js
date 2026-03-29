/**
 * 心愿单相关API
 * 对接后端 WishlistController
 * 基础路径: /v1/wishlist
 */
import { get, post, put, del } from '@/utils/request'
import { WISHLIST_API, buildUrl } from '../urlEnum'

export const wishlistApi = {
  /**
   * 获取心愿单列表
   * GET /v1/wishlist
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回心愿单列表
   */
  getWishlist: (params) => get(WISHLIST_API.GET_WISHLIST, params),

  /**
   * 获取心愿单列表（别名）
   * @param {Object} params - 查询参数
   * @returns {Promise} 返回心愿单列表
   */
  getList: (params) => get(WISHLIST_API.GET_WISHLIST, params),

  /**
   * 添加到心愿单
   * POST /v1/wishlist
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.targetType - 目标类型(dish/merchant/recipe)
   * @param {string} data.targetId - 目标ID
   * @param {string} data.note - 备注
   * @param {number} data.priority - 优先级(1-高, 2-中, 3-低)
   * @returns {Promise} 返回添加结果
   */
  addWish: (data) => post(WISHLIST_API.ADD_WISH, data),

  /**
   * 添加到心愿单（别名）
   * @param {Object} data - 数据
   * @returns {Promise} 返回添加结果
   */
  add: (data) => post(WISHLIST_API.ADD_WISH, data),

  /**
   * 批量添加到心愿单
   * POST /v1/wishlist/batch
   * @param {Array} data - 数据数组
   * @returns {Promise} 返回添加结果
   */
  batchAdd: (data) => post('/v1/wishlist/batch', data),

  /**
   * 删除心愿单项
   * DELETE /v1/wishlist/{wishId}
   * @param {string} id - 心愿单项ID
   * @param {Object} params - 参数
   * @param {string} params.userId - 用户ID
   * @returns {Promise} 返回删除结果
   */
  deleteWish: (id, params) => del(buildUrl(WISHLIST_API.DELETE_WISH, { wishId: id }), params),

  /**
   * 删除心愿单项（别名）
   * @param {string} id - 心愿单项ID
   * @param {Object} params - 参数
   * @returns {Promise} 返回删除结果
   */
  delete: (id, params) => del(`/v1/wishlist/${id}`, params),

  /**
   * 批量删除心愿单项
   * DELETE /v1/wishlist/batch
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {Array} data.ids - 心愿单项ID数组
   * @returns {Promise} 返回删除结果
   */
  batchDelete: (data) => del('/v1/wishlist/batch', data),

  /**
   * 检查是否已添加到心愿单
   * GET /v1/wishlist/check
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {string} params.targetType - 目标类型
   * @param {string} params.targetId - 目标ID
   * @returns {Promise} 返回检查结果
   */
  check: (params) => get('/v1/wishlist/check', params),

  /**
   * 更新心愿单项
   * POST /v1/wishlist/{id}/update
   * @param {string} id - 心愿单项ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.note - 备注
   * @param {number} data.priority - 优先级
   * @returns {Promise} 返回更新结果
   */
  update: (id, data) => post(`/v1/wishlist/${id}/update`, data),

  /**
   * 完成心愿
   * PUT /v1/wishlist/{wishId}/complete
   * @param {string} id - 心愿单项ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @returns {Promise} 返回完成结果
   */
  completeWish: (id, data) => put(buildUrl(WISHLIST_API.COMPLETE_WISH, { wishId: id }), data),

  /**
   * 实现心愿单（别名）
   * @param {string} id - 心愿单项ID
   * @param {Object} data - 数据
   * @returns {Promise} 返回实现结果
   */
  achieve: (id, data) => post(`/v1/wishlist/${id}/achieve`, data),

  /**
   * 获取已实现的心愿单
   * GET /v1/wishlist/achieved
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回已实现心愿单
   */
  getAchieved: (params) => get('/v1/wishlist/achieved', params),

  /**
   * 获取未实现的心愿单
   * GET /v1/wishlist/unachieved
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回未实现心愿单
   */
  getUnachieved: (params) => get('/v1/wishlist/unachieved', params),

  /**
   * 心愿单排序
   * POST /v1/wishlist/sort
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {Array} data.ids - 排序后的ID数组
   * @returns {Promise} 返回排序结果
   */
  sort: (data) => post('/v1/wishlist/sort', data),

  /**
   * 获取心愿单统计
   * GET /v1/wishlist/statistics
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @returns {Promise} 返回统计数据
   */
  getStatistics: (params) => get('/v1/wishlist/statistics', params)
}

export default wishlistApi
