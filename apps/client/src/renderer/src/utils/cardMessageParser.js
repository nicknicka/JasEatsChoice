/**
 * AI卡片消息解析工具
 * 用于解析AI返回的卡片数据
 */

/**
 * 检测消息是否包含卡片数据
 * @param {string} message - AI返回的消息
 * @returns {boolean} 是否包含卡片数据
 */
export function hasCardData(message) {
  if (!message || typeof message !== 'string') {
    return false
  }
  return message.includes('[CARD_DATA_START]') && message.includes('[CARD_DATA_END]')
}

/**
 * 解析卡片数据
 * @param {string} message - AI返回的消息
 * @returns {object} 包含人类可读文本和卡片数据的对象
 */
export function parseCardData(message) {
  if (!hasCardData(message)) {
    return {
      hasCard: false,
      text: message,
      cardData: null
    }
  }

  try {
    // 提取JSON数据
    const jsonStart = message.indexOf('[CARD_DATA_START]') + '[CARD_DATA_START]'.length
    const jsonEnd = message.indexOf('[CARD_DATA_END]')
    const jsonStr = message.substring(jsonStart, jsonEnd).trim()

    // 提取人类可读的文本
    const text = message.substring(0, message.indexOf('[CARD_DATA_START]')).trim()

    // 解析JSON
    const cardData = JSON.parse(jsonStr)

    return {
      hasCard: true,
      text: text,
      cardData: cardData
    }
  } catch (error) {
    console.error('解析卡片数据失败:', error)
    return {
      hasCard: false,
      text: message,
      cardData: null
    }
  }
}

/**
 * 获取卡片类型
 * @param {object} cardData - 卡片数据
 * @returns {string} 卡片类型
 */
export function getCardType(cardData) {
  return cardData?.cardType || 'unknown'
}

/**
 * 判断是否为商家下单卡片
 * @param {object} cardData - 卡片数据
 * @returns {boolean} 是否为商家下单卡片
 */
export function isMerchantOrderCard(cardData) {
  return getCardType(cardData) === 'merchant_order_card'
}

/**
 * 判断是否为菜单卡片
 * @param {object} cardData - 卡片数据
 * @returns {boolean} 是否为菜单卡片
 */
export function isMenuCard(cardData) {
  return getCardType(cardData) === 'menu_card'
}

/**
 * 判断是否为美食推荐卡片
 * @param {object} cardData - 卡片数据
 * @returns {boolean} 是否为美食推荐卡片
 */
export function isFoodRecommendationCard(cardData) {
  return getCardType(cardData) === 'food_recommendation_card'
}

/**
 * 格式化价格显示
 * @param {number} price - 价格
 * @returns {string} 格式化后的价格
 */
export function formatPrice(price) {
  if (typeof price !== 'number') {
    return '¥0.00'
  }
  return `¥${price.toFixed(2)}`
}

/**
 * 计算订单总价
 * @param {Array} dishes - 菜品列表
 * @param {string} diningMode - 就餐方式
 * @returns {number} 总价
 */
export function calculateOrderTotal(dishes, diningMode = 'takeout') {
  let dishTotal = 0
  let totalItems = 0

  dishes.forEach((dish) => {
    const quantity = dish.quantity || 0
    const price = dish.price || 0
    dishTotal += price * quantity
    totalItems += quantity
  })

  // 包装费（仅自取）
  const packagingFee = diningMode === 'takeout' ? totalItems * 2 : 0

  return dishTotal + packagingFee
}

/**
 * 构建订单创建参数
 * @param {object} cardData - 卡片数据
 * @param {Array} selectedDishes - 用户选择的菜品
 * @param {string} userId - 用户ID
 * @returns {object} 订单创建参数
 */
export function buildOrderParams(cardData, selectedDishes, userId) {
  return {
    userId: userId,
    merchantId: cardData.merchant?.merchantId,
    dishItemsJson: JSON.stringify(
      selectedDishes.map((d) => ({
        dishId: d.dishId,
        quantity: d.quantity,
        price: d.price
      }))
    ),
    diningMode: cardData.diningMode || 'takeout',
    tableNumber: cardData.diningMode === 'dine_in' ? '' : null,
    note: ''
  }
}
