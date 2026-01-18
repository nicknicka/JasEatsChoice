/**
 * 订单相关工具函数
 */

/**
 * 查找已存在的订单项（比较ID、可选食材和备注）
 * @param {Array} orderItems - 订单项列表
 * @param {Object} product - 商品对象
 * @returns {number} 已存在订单项的索引，不存在返回-1
 */
export function findExistingOrderItem(orderItems, product) {
  if (!Array.isArray(orderItems) || !product) return -1

  return orderItems.findIndex(
    (item) =>
      item.id === product.id &&
      JSON.stringify(item.selectedOptionalIngredients) ===
        JSON.stringify(product.selectedOptionalIngredients) &&
      item.remark === product.remark
  )
}

/**
 * 合并商品到订单
 * @param {Array} orderItems - 订单项列表
 * @param {Object} product - 要添加的商品
 * @param {number} existingIndex - 已存在项的索引
 */
export function mergeOrderItem(orderItems, product, existingIndex) {
  if (!Array.isArray(orderItems) || !product) return

  if (existingIndex === -1) {
    // 没有完全相同的商品，添加新项
    orderItems.push({ ...product })
  } else {
    // 有完全相同的商品，更新数量
    orderItems[existingIndex].quantity += product.quantity
  }
}

/**
 * 计算订单总金额
 * @param {Array} orderItems - 订单项列表
 * @returns {number} 总金额
 */
export function calculateOrderTotal(orderItems) {
  if (!Array.isArray(orderItems)) return 0

  return orderItems.reduce((total, item) => {
    return total + (item.price || 0) * (item.quantity || 0)
  }, 0)
}

/**
 * 验证订单项
 * @param {Array} orderItems - 订单项列表
 * @returns {boolean} 是否有效
 */
export function validateOrderItems(orderItems) {
  return Array.isArray(orderItems) && orderItems.length > 0
}

/**
 * 格式化订单项为提交格式
 * @param {Array} orderItems - 订单项列表
 * @returns {Array} 格式化后的订单项
 */
export function formatOrderItemsForSubmit(orderItems) {
  if (!Array.isArray(orderItems)) return []

  return orderItems.map((item) => ({
    productId: item.id,
    productName: item.name,
    quantity: item.quantity,
    price: item.price,
    remark: item.remark || '',
    requiredIngredients: item.requiredIngredients || [],
    optionalIngredients: item.selectedOptionalIngredients || []
  }))
}
