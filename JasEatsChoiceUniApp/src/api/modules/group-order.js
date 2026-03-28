import { get, post } from '@/utils/request'

/**
 * 群订单管理 API
 */
export const groupOrderApi = {
  /**
   * IM-037: 获取群订单详情
   */
  getDetail: (orderId) => get(`/v1/group-orders/${orderId}`),

  /**
   * IM-038: 支付群订单
   */
  payOrder: (orderId, data) => post(`/v1/group-orders/${orderId}/pay`, data),

  /**
   * 支付单个成员订单
   */
  payMemberOrder: (orderId, memberId, data) => post(`/v1/group-orders/${orderId}/members/${memberId}/pay`, data),

  /**
   * 添加菜品
   */
  addDish: (orderId, data) => post(`/v1/group-orders/${orderId}/dishes`, data)
}
