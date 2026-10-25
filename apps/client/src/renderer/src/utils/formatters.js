/**
 * 格式化工具函数
 */

/**
 * 格式化时间
 * @param {string|Date} time - 时间字符串或Date对象
 * @returns {string} 格式化后的时间字符串
 */
export function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

/**
 * 格式化金额
 * @param {number} amount - 金额数值
 * @param {number} decimals - 小数位数，默认2位
 * @returns {string} 格式化后的金额字符串
 */
export function formatAmount(amount, decimals = 2) {
  if (typeof amount !== 'number') {
    amount = parseFloat(amount) || 0
  }
  return amount.toFixed(decimals)
}

/**
 * 格式化菜品数量显示
 * @param {Array} items - 菜品数组
 * @param {number} maxDisplay - 最大显示数量
 * @returns {Object} { items: 显示的菜品, hasMore: 是否有更多 }
 */
export function formatDisplayItems(items, maxDisplay = 3) {
  if (!items || items.length === 0) {
    return { items: [], hasMore: false }
  }

  if (items.length <= maxDisplay) {
    return { items, hasMore: false }
  }

  return {
    items: items.slice(0, maxDisplay),
    hasMore: true
  }
}

/**
 * 计算订单商品总数
 * @param {Array} items - 菜品数组
 * @returns {number} 商品总数
 */
export function calculateItemCount(items) {
  if (!items || items.length === 0) return 0
  return items.reduce((sum, item) => sum + (item.quantity || 0), 0)
}

/**
 * 格式化菜品价格
 * @param {number} price - 单价
 * @param {number} quantity - 数量
 * @returns {Object} { unitPrice, totalPrice }
 */
export function formatDishPrice(price, quantity = 1) {
  const unitPrice = parseFloat(price) || 0
  const totalPrice = unitPrice * quantity
  return {
    unitPrice: unitPrice.toFixed(2),
    totalPrice: totalPrice.toFixed(2)
  }
}
