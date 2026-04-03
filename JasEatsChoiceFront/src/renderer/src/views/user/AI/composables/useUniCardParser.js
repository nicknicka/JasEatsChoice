/**
 * UniCard 解析器
 * 统一处理各种卡片数据格式，转换为标准 UniCard 格式（jaseat_card_v1）
 */

const UNICARD_SCHEMA = 'jaseat_card_v1'

/**
 * 检测是否为 UniCard 格式
 * @param {Object} rawData - 原始数据
 * @returns {{ isUniCard: boolean, card?: Object }}
 */
function parseUniCard(rawData) {
  if (!rawData || typeof rawData !== 'object') {
    return { isUniCard: false }
  }

  if (rawData.schema === UNICARD_SCHEMA) {
    return { isUniCard: true, card: rawData }
  }

  return { isUniCard: false }
}

/**
 * 将老格式菜品数据标准化为 dishes 数组
 * @param {Object} dish - 老格式菜品数据
 * @returns {Object} 标准化的菜品对象
 */
function normalizeDish(dish) {
  return {
    dishId: dish.dishId || dish.id || null,
    dishName: dish.dishName || dish.name || dish.title || '未命名菜品',
    imageUrl: dish.imageUrl || dish.image || null,
    description: dish.description || dish.recommendReason || dish.highlight || dish.subtitle || null,
    price: dish.price !== undefined ? dish.price : null,
    rating: dish.rating || null,
    category: dish.category || null,
    tags: dish.tags || [],
    actions: dish.actions || []
  }
}

/**
 * 将老格式订单数据标准化为 orders 数组
 * @param {Object} order - 老格式订单数据
 * @returns {Object} 标准化的订单对象
 */
function normalizeOrder(order) {
  return {
    orderId: order.orderId || order.id || null,
    statusText: order.statusText || order.status || null,
    totalAmount: order.totalAmount || order.total || 0,
    createTime: order.createTime || null,
    dishCount: order.dishCount || (order.items ? order.items.length : 0),
    actions: order.actions || []
  }
}

/**
 * 将老格式转换为 UniCard 格式
 * @param {Object} legacyData - 老格式卡片数据
 * @returns {Object|null} UniCard 格式数据，无法识别返回 null
 */
function convertLegacyToUniCard(legacyData) {
  if (!legacyData || typeof legacyData !== 'object') {
    return null
  }

  // 1. {type: "dish", title, subtitle, tags, price, rating, image, highlight}
  if (legacyData.type === 'dish') {
    return {
      schema: UNICARD_SCHEMA,
      header: {
        title: { text: legacyData.title || '菜品推荐', icon: '🍽️' },
        subtitle: legacyData.subtitle || null,
        theme: 'dish'
      },
      elements: [
        {
          tag: 'dish_list',
          dishes: [normalizeDish(legacyData)]
        }
      ]
    }
  }

  // 2. {cardType: "food_recommendation_card", recommendations: [...]}
  if (legacyData.cardType === 'food_recommendation_card') {
    return {
      schema: UNICARD_SCHEMA,
      header: {
        title: { text: '菜品推荐', icon: '🍽️' },
        subtitle: null,
        theme: 'dish'
      },
      elements: [
        {
          tag: 'dish_list',
          dishes: (legacyData.recommendations || []).map(normalizeDish)
        }
      ]
    }
  }

  // 3. {dishes: [...]}
  if (legacyData.dishes && Array.isArray(legacyData.dishes) && !legacyData.cardType) {
    return {
      schema: UNICARD_SCHEMA,
      header: {
        title: { text: '菜品列表', icon: '🍽️' },
        subtitle: null,
        theme: 'dish'
      },
      elements: [
        {
          tag: 'dish_list',
          dishes: legacyData.dishes.map(normalizeDish)
        }
      ]
    }
  }

  // 4. {recommendations: [...]}
  if (legacyData.recommendations && Array.isArray(legacyData.recommendations)) {
    return {
      schema: UNICARD_SCHEMA,
      header: {
        title: { text: '菜品推荐', icon: '🍽️' },
        subtitle: null,
        theme: 'dish'
      },
      elements: [
        {
          tag: 'dish_list',
          dishes: legacyData.recommendations.map(normalizeDish)
        }
      ]
    }
  }

  // 5. {type: "order", title, subtitle, status, items, total, timeline}
  if (legacyData.type === 'order') {
    return {
      schema: UNICARD_SCHEMA,
      header: {
        title: { text: legacyData.title || '我的订单', icon: '📋' },
        subtitle: legacyData.subtitle || null,
        theme: 'order'
      },
      elements: [
        {
          tag: 'order_list',
          orders: [normalizeOrder(legacyData)]
        }
      ]
    }
  }

  // 6. {orders: [...]}
  if (legacyData.orders && Array.isArray(legacyData.orders)) {
    return {
      schema: UNICARD_SCHEMA,
      header: {
        title: { text: '我的订单', icon: '📋' },
        subtitle: null,
        theme: 'order'
      },
      elements: [
        {
          tag: 'order_list',
          orders: legacyData.orders.map(normalizeOrder)
        }
      ]
    }
  }

  // 7. {type: "health", title, subtitle, stats, suggestion}
  if (legacyData.type === 'health') {
    return {
      schema: UNICARD_SCHEMA,
      header: {
        title: { text: legacyData.title || '健康报告', icon: '💪' },
        subtitle: legacyData.subtitle || null,
        theme: 'health'
      },
      elements: [
        {
          tag: 'health_stats',
          stats: legacyData.stats || [],
          suggestion: legacyData.suggestion || null
        }
      ]
    }
  }

  // 8. {cardType: "merchant_order_card", merchant, preSelectedDishes, actionButtons}
  if (legacyData.cardType === 'merchant_order_card') {
    return {
      schema: UNICARD_SCHEMA,
      header: {
        title: { text: legacyData.merchant?.name || '商家下单', icon: '🏪' },
        subtitle: legacyData.merchant?.address || null,
        theme: 'dish'
      },
      elements: [
        {
          tag: 'dish_list',
          dishes: (legacyData.preSelectedDishes || []).map(normalizeDish)
        }
      ],
      actions: (legacyData.actionButtons || []).map(btn => ({
        type: btn.type || 'primary',
        text: btn.text || '操作',
        value: btn.value || btn.action || null
      }))
    }
  }

  // 9. {cardType: "MERCHANT_MENU_CARD", merchant, dishes, defaultSelection}
  if (legacyData.cardType === 'MERCHANT_MENU_CARD') {
    const allDishes = [
      ...(legacyData.defaultSelection || []).map(d => ({
        ...normalizeDish(d),
        isRecommended: true
      })),
      ...(legacyData.dishes || []).map(normalizeDish)
    ]

    return {
      schema: UNICARD_SCHEMA,
      displayMode: 'modal',
      header: {
        title: { text: legacyData.merchant?.name || '商家菜单', icon: '🏪' },
        subtitle: legacyData.merchant?.address || null,
        theme: 'dish'
      },
      elements: [
        {
          tag: 'dish_list',
          dishes: allDishes
        }
      ],
      actions: [
        { type: 'primary', text: '让AI下单', value: 'ai_submit' },
        { type: 'success', text: '手动提交', value: 'manual_submit' }
      ]
    }
  }

  // 无法识别的格式
  return null
}

/**
 * 统一入口：先尝试 UniCard 解析，失败则走兼容转换
 * @param {Object} rawData - 原始卡片数据
 * @returns {Object} 解析结果 { parsed: boolean, card: Object|null, isUniCard: boolean }
 */
function parseCardData(rawData) {
  // 先尝试 UniCard 格式
  const uniCardResult = parseUniCard(rawData)
  if (uniCardResult.isUniCard) {
    return {
      parsed: true,
      card: uniCardResult.card,
      isUniCard: true
    }
  }

  // 尝试老格式转换
  const converted = convertLegacyToUniCard(rawData)
  if (converted) {
    return {
      parsed: true,
      card: converted,
      isUniCard: false
    }
  }

  // 解析失败
  return {
    parsed: false,
    card: null,
    isUniCard: false
  }
}

/**
 * UniCard 解析器 composable
 * @returns {{ parseUniCard, convertLegacyToUniCard, parseCardData }}
 */
export function useUniCardParser() {
  return {
    parseUniCard,
    convertLegacyToUniCard,
    parseCardData
  }
}
