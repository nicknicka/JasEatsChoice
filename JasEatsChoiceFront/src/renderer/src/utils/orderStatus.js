/**
 * 订单状态工具函数
 */

/**
 * 订单状态优先级（数值越小优先级越高）
 */
export const STATUS_PRIORITY = {
  pending: 1, // 待支付 - 最高优先级
  pendingAccept: 2, // 待接单
  preparing: 3, // 制作中
  completed: 4, // 已完成
  cancelled: 5 // 已取消 - 最低优先级
}

/**
 * 订单状态映射
 */
export const ORDER_STATUS_MAP = {
  all: '全部订单',
  pending: '待支付',
  pendingAccept: '待接单',
  preparing: '制作中',
  completed: '已完成',
  cancelled: '已取消'
}

/**
 * 订单状态标签样式映射
 */
export const STATUS_TAG_TYPE_MAP = {
  pending: 'warning', // 待支付
  pendingAccept: 'warning', // 待接单
  preparing: 'primary', // 制作中
  completed: 'success', // 已完成
  cancelled: 'danger' // 已取消
}

/**
 * 订单筛选按钮顺序
 */
export const STATUS_LIST = [
  { value: 'all', label: '全部订单' },
  { value: 'pending', label: '待支付' },
  { value: 'pendingAccept', label: '待接单' },
  { value: 'preparing', label: '制作中' },
  { value: 'completed', label: '已完成' },
  { value: 'cancelled', label: '已取消' }
]

/**
 * 后端状态码到前端状态的映射
 * 旧状态兼容：0-待支付、1-待接单、2-备菜中、3-烹饪中、4-待上菜、5-已送达、6-已取消、7-待评价、8-已评价
 * 新状态：0-待支付、1-待接单、2-制作中、3-已完成、4-已取消
 */
const BACKEND_STATUS_MAP = {
  0: 'pending', // 待支付
  1: 'pendingAccept', // 待接单
  2: 'preparing', // 制作中
  3: 'completed', // 已完成
  4: 'cancelled', // 已取消（新5状态系统）
  5: 'completed', // 兼容旧状态：已送达 → 已完成
  6: 'cancelled', // 兼容旧状态：已取消
  7: 'completed', // 兼容旧状态：待评价 → 已完成
  8: 'completed' // 兼容旧状态：已评价 → 已完成
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
  return status === 'pending' || status === 'pendingAccept'
}

/**
 * 判断订单是否可以确认收货
 * @param {string} status - 订单状态
 * @returns {boolean}
 */
export function canConfirmReceipt(status) {
  return status === 'preparing'
}

/**
 * 判断订单是否可以评价
 * @param {string} status - 订单状态
 * @returns {boolean}
 */
export function canEvaluateOrder(status) {
  return status === 'completed'
}
