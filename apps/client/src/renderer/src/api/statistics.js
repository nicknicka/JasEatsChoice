/**
 * 数据统计相关API
 */
import api from '../utils/api'

export default {
  /**
   * 获取仪表板统计数据
   * @param {number} days - 统计天数
   * @returns {Promise} 统计数据
   */
  getDashboardStatistics(days = 7) {
    return api.get('/admin/statistics/dashboard', {
      params: { days }
    })
  },

  /**
   * 导出统计数据
   * @param {Object} params - 导出参数
   * @returns {Promise} 导出结果
   */
  exportStatistics(params) {
    return api.get('/admin/statistics/export', {
      params,
      responseType: 'blob'
    })
  }
}
