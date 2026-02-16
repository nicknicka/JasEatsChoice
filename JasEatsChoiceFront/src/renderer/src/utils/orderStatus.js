/**
 * 订单状态工具函数
 */

/**
 * 订单状态优先级（数值越小优先级越高）
 */
export const STATUS_PRIORITY = {
  pending: 1, // 待支付 - 最高优先级
  pendingAccept: 2, // 待接单
  processing: 3, // 进行中
  delivered: 4, // 已上菜
  reviewed: 5, // 已评价
  cancelled: 6 // 已取消 - 最低优先级
}

/**
 * 订单状态映射
 */
export const ORDER_STATUS_MAP = {
  all: '全部订单',
  pendingAccept: '待接单',
  processing: '进行中',
  pending: '待确认',
  pendingComment: '待评价',
  delivered: '已上菜',
  reviewed: '已评价',
  cancelled: '已取消'
}

/**
 * 订单状态标签样式映射
 */
export const STATUS_TAG_TYPE_MAP = {
  pendingAccept: 'warning',
  processing: 'primary',
  pending: 'info',
  pendingComment: 'info',
  delivered: 'success',
  reviewed: 'success', // 已评价
  cancelled: 'danger'
}

/**
 * 订单筛选按钮顺序
 */
export const STATUS_LIST = [
  { value: 'all', label: '全部订单' },
  { value: 'pendingAccept', label: '待接单' },
  { value: 'processing', label: '进行中' },
  { value: 'pending', label: '待支付' },
  { value: 'pendingComment', label: '待评价' },
  { value: 'delivered', label: '已上菜' },
  { value: 'reviewed', label: '已评价' },
  { value: 'cancelled', label: '已取消' }
]

/**
 * 后端状态码到前端状态的映射
 */
const BACKEND_STATUS_MAP = {
  0: 'pending', // 待支付
  1: 'pendingAccept', // 待接单
  2: 'processing', // 备菜中
  3: 'processing', // 烹饪中
  4: 'processing', // 待上菜
  5: 'delivered', // 已上菜
  6: 'cancelled', // 已取消
  7: 'pendingComment', // 待评价（已完成但未评价）
  8: 'reviewed' // 已评价（已完成并已评价）
}

/**
 * 将后端状态码转换为前端状态文本
 * @param {number} statusCode - 后端状态码
 * @returns {string} 前端状态标识
 */
export function orderStatusToText(statusCode) {
  return BACKEND_STATUS_MAP[statusCode] || 'pending'
}

/**
 * 获取订单状态显示文本
 * @param {string} status - 前端状态标识
 * @returns {string} 状态显示文本
 */
export function getOrderStatusText(status) {
  return ORDER_STATUS_MAP[status] || status
}

/**
 * 获取订单状态标签类型
 * @param {string} status - 前端状态标识
 * @returns {string} Element Plus Tag 类型
 */
export function getOrderStatusTagType(status) {
  return STATUS_TAG_TYPE_MAP[status] || 'info'
}

/**
 * 获取订单状态优先级
 * @param {string} status - 前端状态标识
 * @returns {number} 优先级数值
 */
export function getStatusPriority(status) {
  return STATUS_PRIORITY[status] || 999
}

/**
 * 判断订单是否可以取消
 * @param {string} status - 订单状态
 * @returns {boolean}
 */
export function canCancelOrder(status) {
  return status === 'pendingAccept'
}

/**
 * 判断订单是否可以确认收货
 * @param {string} status - 订单状态
 * @returns {boolean}
 */
export function canConfirmReceipt(status) {
  const canConfirm = status === 'delivered'
  console.log('📦 orderStatus.canConfirmReceipt', {
    inputStatus: status,
    statusText: ORDER_STATUS_MAP[status],
    canConfirm,
    requiredStatus: 'delivered',
    requiredStatusText: '已上菜',
    timestamp: new Date().toISOString()
  })
  return canConfirm
}

/**
 * 判断订单是否可以评价
 * @param {string} status - 订单状态
 * @returns {boolean}
 */
export function canEvaluateOrder(status) {
  return status === 'pendingComment'
}
