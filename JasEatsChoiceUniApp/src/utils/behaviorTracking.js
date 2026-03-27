/**
 * 用户行为追踪工具
 * 用于批量记录用户行为数据，支持批量发送
 */

import { http } from './request.js'

// 行为队列
let behaviorQueue = []

// 批量发送阈值
const BATCH_SIZE = 10

// 发送间隔（毫秒）
const SEND_INTERVAL = 5000

// 定时器
let sendTimer = null

/**
 * 初始化行为追踪
 */
export function initBehaviorTracking() {
  // 页面卸载时发送剩余数据
  uni.$on('beforeUnload', () => {
    flushBehaviors()
  })

  // 启动定时发送
  startSendTimer()
}

/**
 * 记录菜品浏览行为
 * @param {String} dishId - 菜品ID
 * @param {Object} options - 额外选项
 * @param {String} options.behaviorType - 行为类型，默认为'view'
 * @param {Object} options.context - 额外上下文信息
 */
export function trackDishView(dishId, options = {}) {
  const { behaviorType = 'view', context = {} } = options

  addBehavior({
    itemType: 'dish',
    itemId: dishId,
    behaviorType,
    context: {
      ...context,
      source: context.source || 'dish_view',
      timestamp: Date.now()
    }
  })
}

/**
 * 记录菜品点击行为
 * @param {String} dishId - 菜品ID
 * @param {Object} options - 额外选项
 */
export function trackDishClick(dishId, options = {}) {
  trackDishView(dishId, {
    behaviorType: 'click',
    ...options
  })
}

/**
 * 记录菜品收藏行为
 * @param {String} dishId - 菜品ID
 * @param {Object} options - 额外选项
 */
export function trackDishFavorite(dishId, options = {}) {
  trackDishView(dishId, {
    behaviorType: 'favorite',
    ...options
  })
}

/**
 * 记录菜品分享行为
 * @param {String} dishId - 菜品ID
 * @param {Object} options - 额外选项
 */
export function trackDishShare(dishId, options = {}) {
  trackDishView(dishId, {
    behaviorType: 'share',
    ...options
  })
}

/**
 * 记录菜品列表浏览
 * @param {String} listType - 列表类型
 * @param {Number} itemCount - 列表项数量
 * @param {Object} options - 额外选项
 */
export function trackDishListView(listType, itemCount, options = {}) {
  const { context = {} } = options

  addBehavior({
    itemType: 'dish_list',
    itemId: `${listType}_${Date.now()}`,
    behaviorType: 'view',
    context: {
      ...context,
      listType,
      itemCount,
      source: context.source || 'dish_list_view',
      timestamp: Date.now()
    }
  })
}

/**
 * 记录商家浏览行为
 * @param {String} merchantId - 商家ID
 * @param {Object} options - 额外选项
 */
export function trackMerchantView(merchantId, options = {}) {
  const { context = {} } = options

  addBehavior({
    itemType: 'merchant',
    itemId: merchantId,
    behaviorType: 'view',
    context: {
      ...context,
      source: context.source || 'merchant_view',
      timestamp: Date.now()
    }
  })
}

/**
 * 添加行为到队列
 * @param {Object} behavior - 行为数据
 */
function addBehavior(behavior) {
  behaviorQueue.push(behavior)

  // 如果队列达到批量阈值，立即发送
  if (behaviorQueue.length >= BATCH_SIZE) {
    flushBehaviors()
  }
}

/**
 * 启动定时发送
 */
function startSendTimer() {
  if (sendTimer) {
    clearInterval(sendTimer)
  }

  sendTimer = setInterval(() => {
    if (behaviorQueue.length > 0) {
      flushBehaviors()
    }
  }, SEND_INTERVAL)
}

/**
 * 发送队列中的所有行为
 */
async function flushBehaviors() {
  if (behaviorQueue.length === 0) {
    return
  }

  const behaviorsToSend = [...behaviorQueue]
  behaviorQueue = []

  try {
    // 调用后端接口批量记录行为
    await http.post('/api/v1/user-behavior/batch', {
      behaviors: behaviorsToSend
    })

    console.log(`[行为追踪] 成功发送 ${behaviorsToSend.length} 条行为记录`)
  } catch (error) {
    console.error('[行为追踪] 发送失败:', error)

    // 发送失败，将数据放回队列
    behaviorQueue = [...behaviorsToSend, ...behaviorQueue]

    // 限制队列大小，防止内存溢出
    if (behaviorQueue.length > 100) {
      behaviorQueue = behaviorQueue.slice(-100)
    }
  }
}

/**
 * 清空行为队列
 */
export function clearBehaviorQueue() {
  behaviorQueue = []
}

/**
 * 获取当前队列大小
 */
export function getQueueSize() {
  return behaviorQueue.length
}

// 默认导出行为追踪对象
export default {
  init: initBehaviorTracking,
  trackDishView,
  trackDishClick,
  trackDishFavorite,
  trackDishShare,
  trackDishListView,
  trackMerchantView,
  flush: flushBehaviors,
  clear: clearBehaviorQueue,
  getQueueSize
}
