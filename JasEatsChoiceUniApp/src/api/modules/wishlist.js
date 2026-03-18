/**
 * 心愿单相关API
 * 对接后端 WishlistController
 * 基础路径: /v1/wishlist
 */
import { get, post, del } from '@/utils/request'

export const wishlistApi = {
  /**
   * 获取心愿单列表
   * GET /v1/wishlist
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   */
  getList: (params) => get('/v1/wishlist', params),

  /**
   * 添加到心愿单
   * POST /v1/wishlist
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.targetType - 目标类型(dish/merchant/recipe)
   * @param {string} data.targetId - 目标ID
   * @param {string} data.note - 备注
   * @param {number} data.priority - 优先级(1-高, 2-中, 3-低)
   */
  add: (data) => post('/v1/wishlist', data),

  /**
   * 批量添加到心愿单
   * POST /v1/wishlist/batch
   * @param {Array} data - 数据数组
   */
  batchAdd: (data) => post('/v1/wishlist/batch', data),

  /**
   * 删除心愿单项
   * DELETE /v1/wishlist/{id}
   * @param {string} id - 心愿单项ID
   * @param {Object} params - 参数
   * @param {string} params.userId - 用户ID
   */
  delete: (id, params) => del(`/v1/wishlist/${id}`, params),

  /**
   * 批量删除心愿单项
   * DELETE /v1/wishlist/batch
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {Array} data.ids - 心愿单项ID数组
   */
  batchDelete: (data) => del('/v1/wishlist/batch', data),

  /**
   * 检查是否已添加到心愿单
   * GET /v1/wishlist/check
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {string} params.targetType - 目标类型
   * @param {string} params.targetId - 目标ID
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
   */
  update: (id, data) => post(`/v1/wishlist/${id}/update`, data),

  /**
   * 实现心愿单
   * POST /v1/wishlist/{id}/achieve
   * @param {string} id - 心愿单项ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   */
  achieve: (id, data) => post(`/v1/wishlist/${id}/achieve`, data),

  /**
   * 获取已实现的心愿单
   * GET /v1/wishlist/achieved
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   */
  getAchieved: (params) => get('/v1/wishlist/achieved', params),

  /**
   * 获取未实现的心愿单
   * GET /v1/wishlist/unachieved
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   */
  getUnachieved: (params) => get('/v1/wishlist/unachieved', params),

  /**
   * 心愿单排序
   * POST /v1/wishlist/sort
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {Array} data.ids - 排序后的ID数组
   */
  sort: (data) => post('/v1/wishlist/sort', data),

  /**
   * 获取心愿单统计
   * GET /v1/wishlist/statistics
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   */
  getStatistics: (params) => get('/v1/wishlist/statistics', params)
}

export default wishlistApi
