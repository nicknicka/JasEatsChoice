/**
 * 数据统计相关API
 *
 * 后端需要实现的接口：
 * - GET /v1/admin/statistics/dashboard - 获取仪表板统计数据
 * - GET /v1/admin/statistics/daily - 获取每日详细数据
 * - GET /v1/admin/statistics/export - 导出统计数据
 *
 * 当前状态：使用模拟数据，待后端API实现后启用真实API调用
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
    console.log(`[模拟] 获取统计数据，天数: ${days}`)

    const dates = []
    const newUsersData = []
    const ordersData = []
    const revenueData = []

    for (let i = days - 1; i >= 0; i--) {
      const date = new Date()
      date.setDate(date.getDate() - i)
      const dateStr = date.toISOString().split('T')[0]
      dates.push(dateStr)
      newUsersData.push(Math.floor(Math.random() * 100) + 20)
      ordersData.push(Math.floor(Math.random() * 200) + 50)
      revenueData.push(Math.floor(Math.random() * 10000) + 2000)
    }

    const totalUsers = 15680
    const totalOrders = 28930

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
    console.log('[模拟] 导出统计数据:', params)

    return Promise.resolve({
      code: '200',
      message: '数据导出功能开发中，请稍后',
      data: {
        downloadUrl: null,
        note: '模拟数据 - 后端导出API待实现'
      }
    })
  }
}
