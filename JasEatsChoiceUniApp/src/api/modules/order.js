import { get, post, put, del } from '@/utils/request'

/**
 * 订单相关API
 */
export const orderApi = {
  /**
   * 创建订单
   * @param {Object} data - 订单数据
   * @param {Array} data.items - 菜品列表
   * @param {Object} data.address - 收货地址
   * @param {string} data.remark - 订单备注
   * @param {number} data.couponId - 优惠券ID
   */
  create: (data) => post('/api/order/create', data),

  /**
   * 获取订单列表
   * @param {Object} params - 查询参数
   * @param {string} params.status - 订单状态
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   */
  getList: (params) => get('/api/order/list', params),

  /**
   * 获取订单详情
   * @param {number} id - 订单ID
   */
  getDetail: (id) => get(`/api/order/${id}`),

  /**
   * 获取订单进度
   * @param {number} id - 订单ID
   */
  getProgress: (id) => get(`/api/order/${id}/progress`),

  /**
   * 取消订单
   * @param {number} id - 订单ID
   * @param {string} reason - 取消原因
   */
  cancel: (id, reason) => post(`/api/order/${id}/cancel`, { reason }),

  /**
   * 确认收货
   * @param {number} id - 订单ID
   */
  confirm: (id) => post(`/api/order/${id}/confirm`),

  /**
   * 申请退款
   * @param {number} id - 订单ID
   * @param {string} reason - 退款原因
   */
  refund: (id, reason) => post(`/api/order/${id}/refund`, { reason }),

  /**
   * 评价订单
   * @param {number} id - 订单ID
   * @param {Object} data - 评价数据
   * @param {number} data.rating - 评分
   * @param {Array} data.tags - 评价标签
   * @param {string} data.content - 评价内容
   */
  comment: (id, data) => post(`/api/order/${id}/comment`, data),

  /**
   * 再来一单
   * @param {number} id - 订单ID
   */
  reorder: (id) => post(`/api/order/${id}/reorder`),

  /**
   * 获取订单数量统计
   */
  getOrderStats: () => get('/api/order/stats'),

  /**
   * 搜索订单
   * @param {string} keyword - 搜索关键词（订单号、菜品名）
   */
  search: (keyword) => get('/api/order/search', { keyword })
}
