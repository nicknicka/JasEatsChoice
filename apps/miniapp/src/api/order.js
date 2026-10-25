/**
 * 订单相关API
 */
import request from '@/utils/request'

/**
 * 创建订单
 */
export const createOrder = (data) => {
  return request({
    url: '/api/order/create',
    method: 'POST',
    data
  })
}

/**
 * 获取订单列表
 */
export const getOrderList = (params) => {
  return request({
    url: '/api/order/list',
    method: 'GET',
    params
  })
}

/**
 * 获取订单详情
 */
export const getOrderDetail = (orderId) => {
  return request({
    url: `/api/order/${orderId}`,
    method: 'GET'
  })
}

/**
 * 取消订单
 */
export const cancelOrder = (orderId) => {
  return request({
    url: `/api/order/${orderId}/cancel`,
    method: 'POST'
  })
}

/**
 * 确认订单
 */
export const confirmOrder = (orderId) => {
  return request({
    url: `/api/order/${orderId}/confirm`,
    method: 'POST'
  })
}

/**
 * 支付订单
 */
export const payOrder = (data) => {
  return request({
    url: '/api/order/pay',
    method: 'POST',
    data
  })
}

/**
 * 申请退款
 */
export const refundOrder = (orderId, data) => {
  return request({
    url: `/api/order/${orderId}/refund`,
    method: 'POST',
    data
  })
}

/**
 * 确认收货
 */
export const confirmReceipt = (orderId) => {
  return request({
    url: `/api/order/${orderId}/receipt`,
    method: 'POST'
  })
}

/**
 * 评价订单
 */
export const reviewOrder = (data) => {
  return request({
    url: '/api/order/review',
    method: 'POST',
    data
  })
}

/**
 * 再来一单
 */
export const reorder = (orderId) => {
  return request({
    url: `/api/order/${orderId}/reorder`,
    method: 'POST'
  })
}

/**
 * 获取订单数量统计
 */
export const getOrderCounts = () => {
  return request({
    url: '/api/order/counts',
    method: 'GET'
  })
}

/**
 * 获取购物车
 */
export const getCart = () => {
  return request({
    url: '/api/cart',
    method: 'GET'
  })
}

/**
 * 添加到购物车
 */
export const addToCart = (data) => {
  return request({
    url: '/api/cart/add',
    method: 'POST',
    data
  })
}

/**
 * 更新购物车
 */
export const updateCartItem = (cartId, data) => {
  return request({
    url: `/api/cart/${cartId}`,
    method: 'PUT',
    data
  })
}

/**
 * 删除购物车项
 */
export const deleteCartItem = (cartId) => {
  return request({
    url: `/api/cart/${cartId}`,
    method: 'DELETE'
  })
}

/**
 * 清空购物车
 */
export const clearCart = () => {
  return request({
    url: '/api/cart/clear',
    method: 'POST'
  })
}

export default {
  createOrder,
  getOrderList,
  getOrderDetail,
  cancelOrder,
  confirmOrder,
  payOrder,
  refundOrder,
  confirmReceipt,
  reviewOrder,
  reorder,
  getOrderCounts,
  getCart,
  addToCart,
  updateCartItem,
  deleteCartItem,
  clearCart
}
