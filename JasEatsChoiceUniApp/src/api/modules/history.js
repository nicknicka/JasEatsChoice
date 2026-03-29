/**
 * 浏览历史相关API
 * 对接后端 HistoryController
 * 基础路径: /v1/history
 */
import { get, post, del } from '@/utils/request'
import { HISTORY_API, buildUrl } from '../urlEnum'

export const historyApi = {
  /**
   * 获取浏览历史列表
   * GET /v1/history/browse
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.type - 类型(dish/merchant/recipe/all)
   * @returns {Promise} 返回浏览历史列表
   */
  getBrowseHistory: (params) => get(HISTORY_API.GET_BROWSE_HISTORY, params),

  /**
   * 获取浏览历史列表（别名）
   * @param {Object} params - 查询参数
   * @returns {Promise} 返回浏览历史列表
   */
  getList: (params) => get('/v1/history', params),

  /**
   * 获取菜品浏览历史
   * GET /v1/history/dishes
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回菜品浏览历史
   */
  getDishHistory: (params) => get('/v1/history/dishes', params),

  /**
   * 获取商家浏览历史
   * GET /v1/history/merchants
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回商家浏览历史
   */
  getMerchantHistory: (params) => get('/v1/history/merchants', params),

  /**
   * 获取食谱浏览历史
   * GET /v1/history/recipes
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回食谱浏览历史
   */
  getRecipeHistory: (params) => get('/v1/history/recipes', params),

  /**
   * 获取搜索历史
   * GET /v1/history/search
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @returns {Promise} 返回搜索历史
   */
  getSearchHistory: (params) => get(HISTORY_API.GET_SEARCH_HISTORY, params),

  /**
   * 添加浏览记录
   * POST /v1/history
   * @param {Object} data - 浏览数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.targetType - 目标类型(dish/merchant/recipe)
   * @param {string} data.targetId - 目标ID
   * @returns {Promise} 返回添加结果
   */
  add: (data) => post('/v1/history', data),

  /**
   * 批量添加浏览记录
   * POST /v1/history/batch
   * @param {Array} data - 浏览数据数组
   * @returns {Promise} 返回添加结果
   */
  batchAdd: (data) => post('/v1/history/batch', data),

  /**
   * 删除单条浏览记录
   * DELETE /v1/history/{id}
   * @param {string} id - 记录ID
   * @param {Object} params - 参数
   * @param {string} params.userId - 用户ID
   * @returns {Promise} 返回删除结果
   */
  delete: (id, params) => del(`/v1/history/${id}`, params),

  /**
   * 批量删除浏览记录
   * DELETE /v1/history/batch
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @param {Array} data.ids - 记录ID数组
   * @returns {Promise} 返回删除结果
   */
  batchDelete: (data) => del('/v1/history/batch', data),

  /**
   * 清空浏览历史
   * DELETE /v1/history/clear
   * @param {Object} params - 参数
   * @param {string} params.userId - 用户ID
   * @param {string} params.type - 类型(dish/merchant/recipe/all)
   * @returns {Promise} 返回清空结果
   */
  clear: (params) => del('/v1/history/clear', params),

  /**
   * 清空历史记录（别名）
   * DELETE /v1/history/clear
   * @param {Object} params - 参数
   * @returns {Promise} 返回清空结果
   */
  clearHistory: (params) => del(HISTORY_API.CLEAR_HISTORY, params),

  /**
   * 获取浏览统计
   * GET /v1/history/statistics
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @returns {Promise} 返回统计数据
   */
  getStatistics: (params) => get('/v1/history/statistics', params)
}

export default historyApi
