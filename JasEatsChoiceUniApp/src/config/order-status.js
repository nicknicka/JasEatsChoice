/**
 * 订单状态配置
 * 定义订单状态的映射关系、图标、颜色等
 *
 * 后端状态码：
 * - 0: 待支付
 * - 1: 待接单
 * - 2: 制作中
 * - 3: 已完成
 * - 4: 已取消
 */

// uni-ui 图标映射
const ICON_MAP = {
  wallet: 'wallet',           // 钱包 - 待支付
  loading: 'loop',            // 加载 - 制作中
  close: 'close',             // 关闭 - 已取消
  checkmark: 'checkmarkempty' // 勾选 - 已完成
}

// 订单状态配置（5状态系统：0-待支付、1-待接单、2-制作中、3-已完成、4-已取消）
export const ORDER_STATUS_CONFIG = {
  // 待支付
  pending: {
    key: 'pending',
    label: '待支付',
    backendStatus: [0],
    icon: ICON_MAP.wallet,
    iconType: 'uni-ui',
    color: '#FF6B35',
    bgColor: '#FFF2ED',
    badgeColor: '#FF6B35',
    showBadge: true,
    urgent: true
  },

  // 制作中（包含：待接单 + 制作中）
  preparing: {
    key: 'preparing',
    label: '制作中',
    backendStatus: [1, 2],
    icon: ICON_MAP.loading,
    iconType: 'uni-ui',
    color: '#FF9800',
    bgColor: '#FFF3E0',
    badgeColor: null,
    showBadge: false,
    urgent: false
  },

  // 已完成
  completed: {
    key: 'completed',
    label: '已完成',
    backendStatus: [3],
    icon: ICON_MAP.checkmark,
    iconType: 'uni-ui',
    color: '#52C41A',
    bgColor: '#E8F5E9',
    badgeColor: null,
    showBadge: false,
    urgent: false
  },

  // 已取消
  cancelled: {
    key: 'cancelled',
    label: '已取消',
    backendStatus: [4],
    icon: ICON_MAP.close,
    iconType: 'uni-ui',
    color: '#9E9E9E',
    bgColor: '#F5F5F5',
    badgeColor: null,
    showBadge: false,
    urgent: false
  }
}

// 后端状态码到前端状态的映射
export const BACKEND_TO_FRONTEND_MAP = {
  0: 'pending',     // 待支付
  1: 'preparing',   // 待接单 -> 制作中
  2: 'preparing',   // 制作中 -> 制作中
  3: 'completed',   // 已完成 -> 待评价
  4: 'cancelled'    // 已取消
}

// 前端状态到后端状态码的映射
export const FRONTEND_TO_BACKEND_MAP = {
  pending: [0],
  preparing: [1, 2],
  completed: [3],
  cancelled: [4]
}

/**
 * 根据后端状态码获取前端状态配置
 * @param {number|string} backendStatus - 后端状态码
 * @returns {Object} 前端状态配置
 */
export function getStatusConfig(backendStatus) {
  const frontendKey = BACKEND_TO_FRONTEND_MAP[backendStatus]
  return ORDER_STATUS_CONFIG[frontendKey] || ORDER_STATUS_CONFIG.pending
}

/**
 * 根据前端状态key获取配置
 * @param {string} key - 前端状态key
 * @returns {Object} 状态配置
 */
export function getStatusByKey(key) {
  return ORDER_STATUS_CONFIG[key] || ORDER_STATUS_CONFIG.pending
}

/**
 * 将后端订单数量数据映射为前端格式
 * @param {Object} backendData - 后端返回的订单数量数据
 * @returns {Object} 前端格式的订单数量
 *
 * @example
 * // 后端返回格式
 * {
 *   pending: 2,       // 待支付
 *   processing: 1,    // 待接单
 *   delivering: 3,    // 制作中
 *   completed: 5,     // 已完成
 * }
 *
 * // 前端转换后格式
 * {
 *   pending: 2,        // 待支付
 *   preparing: 4,      // 制作中（processing + delivering）
 *   completed: 5,      // 已完成
 *   cancelled: 0       // 已取消
 * }
 */
export function mapOrderCounts(backendData) {
  return {
    pending: backendData.pending || 0,
    preparing: (backendData.processing || 0) + (backendData.delivering || 0),
    completed: backendData.completed || 0,
    cancelled: backendData.cancelled || 0
  }
}

/**
 * 计算活跃订单总数（排除已取消）
 * @param {Object} orderCounts - 前端格式的订单数量
 * @returns {number} 活跃订单总数
 */
export function calculateActiveOrders(orderCounts) {
  return Object.entries(orderCounts)
    .filter(([key]) => key !== 'cancelled')
    .reduce((sum, [, count]) => sum + count, 0)
}

/**
 * 获取用户中心显示的3个核心状态
 * @returns {Array} 核心状态配置数组
 */
export function getCoreStatuses() {
  return [
    ORDER_STATUS_CONFIG.pending,
    ORDER_STATUS_CONFIG.preparing,
    ORDER_STATUS_CONFIG.completed
  ]
}

export default ORDER_STATUS_CONFIG
