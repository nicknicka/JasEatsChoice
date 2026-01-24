/**
 * 增强版推荐系统
 * 整合新的后端推荐API和行为埋点功能
 */

import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { recommendationAPI } from '../api/recommendation.js'
import { useBehaviorTracking } from './useBehaviorTracking.js'
import { useAuthStore } from '../store/authStore'
import pinia from '../store'

/**
 * 推荐来源映射
 */
const RECOMMENDATION_SOURCE_MAP = {
  user_profile: '用户画像',
  collaborative_filtering: '协同过滤',
  content_based: '内容推荐',
  hot: '热门推荐',
  context: '上下文推荐',
  hybrid: '混合推荐'
}

/**
 * 使用增强推荐功能
 */
export function useEnhancedRecommendations() {
  const authStore = useAuthStore(pinia)
  const { trackBehavior, trackRecommendFeedback, startAutoFlush, stopTracking } = useBehaviorTracking()

  // 状态管理
  const recommendations = ref([])
  const isLoading = ref(false)
  const refreshing = ref(false)
  const userProfile = ref(null)

  // 当前推荐批次ID
  const currentRecommendationId = ref(null)

  // 启动自动刷新行为队列
  let autoFlushInterval = null

  /**
   * 获取用户ID
   */
  const getUserId = () => {
    return authStore.userId || authStore.userInfo?.id || authStore.userInfo?.userId
  }

  /**
   * 格式化推荐数据
   */
  const formatRecommendation = (rec, index) => {
    return {
      id: rec.dishId,
      dishId: rec.dishId,
      name: rec.dishName,
      image: rec.dishImage,
      category: rec.category,
      calories: rec.calories,
      price: rec.price,
      rating: rec.rating,
      rank: rec.rank,
      score: rec.score,

      // 推荐理由
      reason: rec.reason?.primary || '系统推荐',
      reasonFactors: rec.reason?.factors || [],

      // 推荐来源
      recommendSource: rec.source || 'hybrid',
      recommendSourceText: RECOMMENDATION_SOURCE_MAP[rec.source] || '混合推荐',

      // 标签
      tags: rec.tags || [],

      // 原始数据
      rawData: rec
    }
  }

  /**
   * 加载推荐
   */
  const loadRecommendations = async (options = {}) => {
    const userId = getUserId()
    if (!userId) {
      ElMessage.warning('请先登录')
      return []
    }

    try {
      isLoading.value = true

      // 获取天气和时段信息
      const timePeriod = getCurrentTimePeriod()
      const weather = options.weather || await getCurrentWeather()

      // 构建请求参数
      const params = {
        scene: options.scene || 'home',
        limit: options.limit || 20,
        ...(timePeriod && { timePeriod }),
        ...(weather && { weather })
      }

      const response = await recommendationAPI.getRecommendations(userId, params)
      const data = response.data.data

      if (data && data.recommendations) {
        // 保存当前推荐批次ID
        if (data.recommendations.length > 0) {
          currentRecommendationId.value = data.recommendations[0].recommendationId
        }

        // 格式化推荐数据
        recommendations.value = data.recommendations.map((rec, index) =>
          formatRecommendation(rec, index)
        )

        // 记录推荐展示行为
        await trackBehavior(
          'view',
          'recommendation',
          'recommendation_list',
          {
            context: {
              count: recommendations.value.length,
              scene: params.scene,
              timePeriod: params.timePeriod,
              weather: params.weather
            }
          }
        )

        return recommendations.value
      }

      return []
    } catch (error) {
      console.error('加载推荐失败:', error)
      ElMessage.error('加载推荐失败，请稍后重试')
      return []
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 刷新推荐
   */
  const refreshRecommendations = async () => {
    refreshing.value = true
    try {
      const userId = getUserId()
      if (!userId) {
        ElMessage.warning('请先登录')
        return []
      }

      const response = await recommendationAPI.refreshRecommendations(userId)
      const data = response.data.data

      if (data && data.recommendations) {
        recommendations.value = data.recommendations.map((rec, index) =>
          formatRecommendation(rec, index)
        )

        ElMessage.success('推荐已刷新')
        return recommendations.value
      }

      return []
    } catch (error) {
      console.error('刷新推荐失败:', error)
      ElMessage.error('刷新推荐失败')
      return []
    } finally {
      refreshing.value = false
    }
  }

  /**
   * 菜品点击
   */
  const onDishClick = async (dish) => {
    try {
      // 记录点击行为
      await trackBehavior('click', 'dish', dish.dishId, {
        context: {
          recommendationId: currentRecommendationId.value,
          rank: dish.rank,
          score: dish.score
        }
      })

      // 记录推荐反馈
      await trackRecommendFeedback(
        dish.dishId,
        currentRecommendationId.value,
        { isClicked: true }
      )

      return true
    } catch (error) {
      console.error('记录点击失败:', error)
      return false
    }
  }

  /**
   * 菜品下单
   */
  const onDishOrder = async (dish) => {
    try {
      // 记录下单行为
      await trackBehavior('order', 'dish', dish.dishId, {
        context: {
          recommendationId: currentRecommendationId.value,
          rank: dish.rank,
          price: dish.price
        }
      })

      // 记录推荐反馈
      await trackRecommendFeedback(
        dish.dishId,
        currentRecommendationId.value,
        { isClicked: true, isOrdered: true }
      )

      return true
    } catch (error) {
      console.error('记录下单失败:', error)
      return false
    }
  }

  /**
   * 拒绝推荐
   */
  const rejectRecommendation = async (dish, reason = '不感兴趣') => {
    try {
      const userId = getUserId()
      if (!userId) {
        ElMessage.warning('请先登录')
        return false
      }

      // 调用后端API记录拒绝
      await recommendationAPI.rejectRecommendation(userId, {
        dishId: dish.dishId,
        reason
      })

      // 记录拒绝行为
      await trackBehavior('reject', 'dish', dish.dishId, {
        context: {
          reason,
          recommendationId: currentRecommendationId.value,
          rank: dish.rank
        }
      })

      // 从列表中移除
      const index = recommendations.value.findIndex(r => r.dishId === dish.dishId)
      if (index > -1) {
        recommendations.value.splice(index, 1)
      }

      ElMessage.success('已标记为不感兴趣')
      return true
    } catch (error) {
      console.error('拒绝推荐失败:', error)
      ElMessage.error('操作失败')
      return false
    }
  }

  /**
   * 替换推荐
   */
  const replaceRecommendations = async (dishIds) => {
    try {
      const userId = getUserId()
      if (!userId) {
        ElMessage.warning('请先登录')
        return []
      }

      const response = await recommendationAPI.replaceRecommendations(userId, dishIds)
      const data = response.data.data

      if (data && data.replacedDishes) {
        // 格式化替换的菜品
        const newDishes = data.replacedDishes.map((dish, index) => ({
          id: dish.id,
          dishId: String(dish.id),
          name: dish.name,
          image: dish.image || '🍱',
          category: dish.category,
          calories: dish.calorie,
          price: dish.price,
          rating: dish.avgRating || 4.5,
          recommendSource: 'replacement',
          recommendSourceText: '替换推荐',
          reason: '根据您的偏好为您推荐其他菜品'
        }))

        // 替换原列表中的菜品
        dishIds.forEach((dishId, index) => {
          const existingIndex = recommendations.value.findIndex(r => r.dishId === dishId)
          if (existingIndex > -1 && newDishes[index]) {
            recommendations.value[existingIndex] = newDishes[index]
          }
        })

        ElMessage.success(`已替换${newDishes.length}道菜品`)
        return newDishes
      }

      return []
    } catch (error) {
      console.error('替换推荐失败:', error)
      ElMessage.error('替换失败')
      return []
    }
  }

  /**
   * 筛选推荐
   */
  const filterRecommendations = async (filters) => {
    try {
      const userId = getUserId()
      if (!userId) {
        ElMessage.warning('请先登录')
        return []
      }

      const response = await recommendationAPI.filterRecommendations(userId, filters)
      const data = response.data.data

      if (data && data.filteredDishes) {
        return data.filteredDishes.map(dish => ({
          id: dish.id,
          dishId: String(dish.id),
          name: dish.name,
          image: dish.image || '🍱',
          category: dish.category,
          calories: dish.calorie,
          price: dish.price,
          rating: dish.avgRating || 4.5
        }))
      }

      return []
    } catch (error) {
      console.error('筛选推荐失败:', error)
      ElMessage.error('筛选失败')
      return []
    }
  }

  /**
   * 获取推荐理由
   */
  const getRecommendationReason = async (dishId) => {
    try {
      const userId = getUserId()
      if (!userId) {
        return '系统推荐'
      }

      const response = await recommendationAPI.getRecommendationReason(userId, dishId)
      const data = response.data.data

      return data?.reason || '系统推荐'
    } catch (error) {
      console.error('获取推荐理由失败:', error)
      return '系统推荐'
    }
  }

  /**
   * 获取用户画像
   */
  const loadUserProfile = async () => {
    try {
      const userId = getUserId()
      if (!userId) {
        return null
      }

      const response = await recommendationAPI.getUserProfile(userId)
      const data = response.data.data

      userProfile.value = data
      return data
    } catch (error) {
      console.error('获取用户画像失败:', error)
      return null
    }
  }

  /**
   * 获取当前时段
   */
  const getCurrentTimePeriod = () => {
    const hour = new Date().getHours()
    if (hour >= 6 && hour < 9) return '早餐'
    if (hour >= 11 && hour < 14) return '午餐'
    if (hour >= 17 && hour < 20) return '晚餐'
    return '宵夜'
  }

  /**
   * 获取当前天气
   */
  const getCurrentWeather = async () => {
    try {
      // 从localStorage获取天气缓存
      const cachedWeather = localStorage.getItem('currentWeather')
      if (cachedWeather) {
        const weather = JSON.parse(cachedWeather)
        const temp = weather.temperature || 20

        if (temp > 30) return 'hot'
        if (temp < 10) return 'cold'
        if (weather.condition?.includes('雨')) return 'rainy'
        return 'sunny'
      }
    } catch (error) {
      console.error('获取天气失败:', error)
    }
    return null
  }

  // 生命周期
  onMounted(() => {
    // 启动自动刷新
    autoFlushInterval = startAutoFlush()
  })

  onUnmounted(() => {
    // 停止追踪
    if (autoFlushInterval) {
      clearInterval(autoFlushInterval)
    }
    stopTracking()
  })

  return {
    // 状态
    recommendations,
    isLoading,
    refreshing,
    userProfile,

    // 方法
    loadRecommendations,
    refreshRecommendations,
    onDishClick,
    onDishOrder,
    rejectRecommendation,
    replaceRecommendations,
    filterRecommendations,
    getRecommendationReason,
    loadUserProfile,

    // 工具方法
    getUserId,
    getCurrentTimePeriod,
    getCurrentWeather
  }
}
