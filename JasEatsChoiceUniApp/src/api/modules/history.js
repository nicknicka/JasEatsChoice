/**
 * 浏览历史相关API
 * 对接后端 HistoryController
 * 基础路径: /v1/history
 */
import { get, post, del } from '@/utils/request'

export const historyApi = {
  /**
   * 获取浏览历史列表
   * GET /v1/history
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.type - 类型(dish/merchant/recipe/all)
   */
  getList: (params) => get('/v1/history', params),

  /**
   * 获取菜品浏览历史
   * GET /v1/history/dishes
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   */
  getDishHistory: (params) => get('/v1/history/dishes', params),

  /**
   * 获取商家浏览历史
   * GET /v1/history/merchants
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   */
  getMerchantHistory: (params) => get('/v1/history/merchants', params),

  /**
   * 获取食谱浏览历史
   * GET /v1/history/recipes
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   */
  getRecipeHistory: (params) => get('/v1/history/recipes', params),

  /**
   * 添加浏览记录
   * POST /v1/history
   * @param {Object} data - 浏览数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.targetType - 目标类型(dish/merchant/recipe)
   * @param {string} data.targetId - 目标ID
   */
  add: (data) => post('/v1/history', data),

  /**
   * 批量添加浏览记录
   * POST /v1/history/batch
   * @param {Array} data - 浏览数据数组
   */
  batchAdd: (data) => post('/v1/history/batch', data),

  /**
   * 删除单条浏览记录
   * DELETE /v1/history/{id}
   * @param {string} id - 记录ID
   * @param {Object} params - 参数
   * @param {string} params.userId - 用户ID
   */
  delete: (id, params) => del(`/v1/history/${id}`, params),

  /**
   * 批量删除浏览记录
   * DELETE /v1/history/batch
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {Array} data.ids - 记录ID数组
   */
  batchDelete: (data) => del('/v1/history/batch', data),

  /**
   * 清空浏览历史
   * DELETE /v1/history/clear
   * @param {Object} params - 参数
   * @param {string} params.userId - 用户ID
   * @param {string} params.type - 类型(dish/merchant/recipe/all)
   */
  clear: (params) => del('/v1/history/clear', params),

  /**
   * 获取浏览统计
   * GET /v1/history/statistics
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   */
  getStatistics: (params) => get('/v1/history/statistics', params)
}

export default historyApi
