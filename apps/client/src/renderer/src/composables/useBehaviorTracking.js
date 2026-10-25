/**
 * 用户行为埋点工具
 * 用于记录用户在系统中的各种行为，为推荐系统提供数据支持
 */

import { ref } from 'vue'
import axios from 'axios'
import { API_CONFIG } from '../config/index.js'

/**
 * 行为类型枚举
 */
export const BEHAVIOR_TYPES = {
  VIEW: 'view', // 浏览
  CLICK: 'click', // 点击
  FAVORITE: 'favorite', // 收藏
  ORDER: 'order', // 下单
  REJECT: 'reject', // 拒绝
  SHARE: 'share', // 分享
  SEARCH: 'search' // 搜索
}

/**
 * 项目类型枚举
 */
export const ITEM_TYPES = {
  DISH: 'dish', // 菜品
  RECOMMENDATION: 'recommendation', // 推荐
  MERCHANT: 'merchant', // 商家
  CATEGORY: 'category' // 分类
}

/**
 * 使用行为埋点功能
 */
export function useBehaviorTracking() {
  // 是否启用埋点
  const trackingEnabled = ref(true)

  // 批量发送队列
  const behaviorQueue = ref([])
  const MAX_QUEUE_SIZE = 10
  const FLUSH_INTERVAL = 5000 // 5秒

  /**
   * 获取用户ID
   */
  const getUserId = () => {
    // 从localStorage获取用户信息
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      try {
        const parsed = JSON.parse(userInfo)
        return parsed.id || parsed.userId
      } catch (e) {
        console.error('解析用户信息失败:', e)
      }
    }
    return null
  }

  /**
   * 记录用户行为
   */
  const trackBehavior = async (behaviorType, itemType, itemId, options = {}) => {
    if (!trackingEnabled.value) {
      return
    }

    const userId = getUserId()
    if (!userId) {
      console.warn('未获取到用户ID，跳过行为记录')
      return
    }

    const behavior = {
      userId: String(userId),
      behaviorType,
      itemType,
      itemId: String(itemId || ''),
      context: options.context || {},
      duration: options.duration || null,
      timestamp: new Date().toISOString()
    }

    // 添加到队列
    behaviorQueue.value.push(behavior)

    // 如果队列达到最大长度，立即发送
    if (behaviorQueue.value.length >= MAX_QUEUE_SIZE) {
      await flushBehaviorQueue()
    }
  }

  /**
   * 批量发送行为数据
   */
  const flushBehaviorQueue = async () => {
    if (behaviorQueue.value.length === 0) {
      return
    }

    const behaviorsToSend = [...behaviorQueue.value]
    behaviorQueue.value = []

    try {
      // 批量发送行为记录
      await Promise.all(
        behaviorsToSend.map((behavior) =>
          axios.post(`${API_CONFIG.baseURL}/v1/recommend/behavior`, behavior)
        )
      )
      console.log(`成功发送${behaviorsToSend.length}条行为记录`)
    } catch (error) {
      console.error('发送行为记录失败:', error)
      // 失败时重新加入队列
      behaviorQueue.value = [...behaviorsToSend, ...behaviorQueue.value]
    }
  }

  /**
   * 页面浏览埋点
   */
  const trackPageView = (pageName, options = {}) => {
    return trackBehavior(BEHAVIOR_TYPES.VIEW, ITEM_TYPES.RECOMMENDATION, pageName, {
      context: {
        page: pageName,
        referrer: document.referrer,
        userAgent: navigator.userAgent,
        ...options.context
      }
    })
  }

  /**
   * 菜品点击埋点
   */
  const trackDishClick = (dishId, options = {}) => {
    return trackBehavior(BEHAVIOR_TYPES.CLICK, ITEM_TYPES.DISH, dishId, options)
  }

  /**
   * 菜品收藏埋点
   */
  const trackDishFavorite = (dishId, options = {}) => {
    return trackBehavior(BEHAVIOR_TYPES.FAVORITE, ITEM_TYPES.DISH, dishId, options)
  }

  /**
   * 菜品下单埋点
   */
  const trackDishOrder = (dishId, options = {}) => {
    return trackBehavior(BEHAVIOR_TYPES.ORDER, ITEM_TYPES.DISH, dishId, options)
  }

  /**
   * 推荐菜品拒绝埋点
   */
  const trackRecommendReject = (dishId, reason, options = {}) => {
    return trackBehavior(BEHAVIOR_TYPES.REJECT, ITEM_TYPES.DISH, dishId, {
      context: {
        reason,
        ...options.context
      }
    })
  }

  /**
   * 推荐反馈埋点（点击、下单等）
   */
  const trackRecommendFeedback = async (dishId, recommendationId, feedbackData) => {
    try {
      const userId = getUserId()
      if (!userId) {
        console.warn('未获取到用户ID，跳过反馈记录')
        return
      }

      await axios.post(`${API_CONFIG.baseURL}/v1/recommend/feedback`, {
        userId: String(userId),
        dishId: String(dishId),
        recommendationId: recommendationId || null,
        isClicked: feedbackData.isClicked || false,
        isOrdered: feedbackData.isOrdered || false
      })

      console.log('推荐反馈记录成功')
    } catch (error) {
      console.error('记录推荐反馈失败:', error)
    }
  }

  /**
   * 搜索行为埋点
   */
  const trackSearch = (keyword, resultCount, options = {}) => {
    return trackBehavior(BEHAVIOR_TYPES.SEARCH, ITEM_TYPES.CATEGORY, keyword, {
      context: {
        keyword,
        resultCount,
        ...options.context
      }
    })
  }

  /**
   * 分享行为埋点
   */
  const trackShare = (itemType, itemId, shareChannel, options = {}) => {
    return trackBehavior(BEHAVIOR_TYPES.SHARE, itemType, itemId, {
      context: {
        shareChannel,
        ...options.context
      }
    })
  }

  /**
   * 启动定时刷新（页面卸载时自动发送剩余队列）
   */
  const startAutoFlush = () => {
    // 定时刷新
    const intervalId = setInterval(flushBehaviorQueue, FLUSH_INTERVAL)

    // 页面卸载时发送剩余数据
    window.addEventListener('beforeunload', () => {
      clearInterval(intervalId)
      flushBehaviorQueue()
    })

    return intervalId
  }

  /**
   * 停止埋点
   */
  const stopTracking = () => {
    trackingEnabled.value = false
    flushBehaviorQueue()
  }

  /**
   * 启用埋点
   */
  const startTracking = () => {
    trackingEnabled.value = true
  }

  return {
    // 状态
    trackingEnabled,
    behaviorQueue,

    // 基础方法
    trackBehavior,
    flushBehaviorQueue,
    startAutoFlush,
    stopTracking,
    startTracking,

    // 便捷方法
    trackPageView,
    trackDishClick,
    trackDishFavorite,
    trackDishOrder,
    trackRecommendReject,
    trackRecommendFeedback,
    trackSearch,
    trackShare,

    // 常量
    BEHAVIOR_TYPES,
    ITEM_TYPES
  }
}
