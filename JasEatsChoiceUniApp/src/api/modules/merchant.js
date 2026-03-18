import { get, post, put, del } from '@/utils/request'

/**
 * 商家相关API
 */
export const merchantApi = {
  /**
   * 获取商家列表
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.keyword - 搜索关键词
   * @param {string} params.category - 分类
   * @param {string} params.sort - 排序方式
   */
  getList: (params) => get('/api/merchant/list', params),

  /**
   * 获取商家详情
   * @param {number} id - 商家ID
   */
  getDetail: (id) => get(`/api/merchant/${id}`),

  /**
   * 获取附近商家
   * @param {Object} params - 查询参数
   * @param {number} params.latitude - 纬度
   * @param {number} params.longitude - 经度
   * @param {number} params.radius - 半径(米)
   */
  getNearby: (params) => get('/api/merchant/nearby', params),

  /**
   * 获取商家优惠券
   * @param {number} merchantId - 商家ID
   */
  getCoupons: (merchantId) => get(`/api/merchant/${merchantId}/coupons`),

  /**
   * 获取商家评价
   * @param {number} merchantId - 商家ID
   * @param {Object} params - 查询参数
   */
  getReviews: (merchantId, params) => get(`/api/merchant/${merchantId}/reviews`, params),

  /**
   * 获取商家统计数据
   * @param {number} merchantId - 商家ID
   */
  getStatistics: (merchantId) => get(`/api/merchant/${merchantId}/statistics`),

  /**
   * 收藏商家
   * @param {number} merchantId - 商家ID
   */
  favorite: (merchantId) => post(`/api/merchant/${merchantId}/favorite`),

  /**
   * 取消收藏商家
   * @param {number} merchantId - 商家ID
   */
  unfavorite: (merchantId) => del(`/api/merchant/${merchantId}/favorite`)
}
