/**
 * 推荐系统相关逻辑
 */

import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { API_CONFIG } from '../config/index.js'
import {
  RECOMMENDATION_TYPES,
  DIVERSITY_CONFIG,
  getRandomTagType
} from '../constants/recommendationConstants.js'
import pinia from '../store'
import { useAuthStore } from '../store/authStore'

export function useRecommendations() {
  // 在函数内部获取 store，避免模块加载时的初始化问题
  const authStore = useAuthStore(pinia)
  const getCurrentUserId = () => {
    const userId = authStore.userId || localStorage.getItem('auth_userId') || ''
    if (!userId) {
      throw new Error('用户未登录')
    }
    return String(userId)
  }

  // 状态管理
  const recommendations = ref([])
  const isLoading = ref(false)
  const refreshing = ref(false)

  // 新增：推荐缓存和统计
  const recommendCache = ref(new Map())
  const lastUpdateTime = ref(null)
  const recommendationStats = ref({
    totalViews: 0,
    totalClicks: 0,
    totalOrders: 0,
    totalRejects: 0
  })

  // 新增：计算属性
  const hasRecommendations = computed(() => recommendations.value.length > 0)
  const recommendationsCount = computed(() => recommendations.value.length)
  const cacheHitRate = computed(() => {
    const total = recommendationStats.value.totalViews
    const hits = recommendCache.value.size
    return total > 0 ? Math.round((hits / total) * 100) : 0
  })

  /**
   * 加载用户拒绝的推荐历史
   */
  const loadRejectionHistory = async () => {
    try {
      const userId = getCurrentUserId()
      const response = await axios.get(`${API_CONFIG.baseURL}${API_CONFIG.recommendReject.list}`, {
        params: { userId }
      })
      // 返回已拒绝的菜品ID列表
      return response.data.data || []
    } catch (error) {
      console.error('加载拒绝历史失败:', error)
      // 如果后端失败，尝试从 localStorage 读取作为后备
      const saved = localStorage.getItem('rejectionHistory')
      return saved ? JSON.parse(saved) : []
    }
  }

  /**
   * 保存用户拒绝的推荐历史到后端
   */
  const saveRejectionHistory = async (userId, dishId) => {
    try {
      await axios.post(`${API_CONFIG.baseURL}${API_CONFIG.recommendReject.add}`, null, {
        params: { userId, dishId }
      })
      // 清除本地缓存，以使用后端数据
      localStorage.removeItem('rejectionHistory')
    } catch (error) {
      console.error('保存拒绝历史失败:', error)
      // 如果后端失败，保存到 localStorage 作为后备
      let history = []
      const saved = localStorage.getItem('rejectionHistory')
      if (saved) {
        history = JSON.parse(saved)
      }
      const existingIndex = history.findIndex((entry) => entry.dishId === dishId)
      if (existingIndex > -1) {
        history[existingIndex].count += 1
      } else {
        history.push({
          dishId,
          count: 1,
          rejectedAt: new Date().toISOString()
        })
      }
      localStorage.setItem('rejectionHistory', JSON.stringify(history))
    }
  }

  /**
   * 检查菜品是否被拒绝过
   */
  const isRejected = (rejectedDishIds, dishId) => {
    if (!rejectedDishIds || rejectedDishIds.length === 0) return false
    // 支持两种格式：字符串ID和数字ID
    const normalizedDishId = String(dishId)
    return rejectedDishIds.some((id) => String(id) === normalizedDishId)
  }

  /**
   * 记录点击反馈
   */
  const recordClickFeedback = async (item) => {
    const userId = getCurrentUserId()
    const dishId = String(item.dishId || item.id || '')
    const recommendationId = item.recommendationId || ''
    if (!recommendationId) {
      ElMessage.warning('推荐结果缺少 recommendationId，无法反馈')
      return
    }

    try {
      // 异步记录到后端
      const feedbackData = {
        userId,
        dishId,
        recommendationId,
        isClicked: true,
        isOrdered: false
      }

      await axios.post(`${API_CONFIG.baseURL}/v1/recommendations/feedback`, feedbackData)

      // 记录用户行为
      const behaviorData = {
        userId,
        behaviorType: 'click',
        itemType: 'dish',
        itemId: dishId,
        context: {
          recommendSource: item.recommendSource,
          scene: 'home'
        }
      }

      await axios.post(`${API_CONFIG.baseURL}/v1/recommendations/behavior`, behaviorData)

      console.log('✓ 点击反馈记录成功')

      // 触发实时更新
      await updateRecommendationsOnBehavior('click', item)
    } catch (error) {
      console.warn('记录点击反馈失败:', error)
      ElMessage.warning('推荐反馈记录失败，请稍后重试')
    }
  }

  /**
   * 记录下单反馈
   */
  const recordOrderFeedback = async (item) => {
    const userId = getCurrentUserId()
    const dishId = String(item.dishId || item.id || '')
    const recommendationId = item.recommendationId || ''
    if (!recommendationId) {
      ElMessage.warning('推荐结果缺少 recommendationId，无法反馈')
      return
    }

    try {
      // 记录到后端
      const feedbackData = {
        userId,
        dishId,
        recommendationId,
        isClicked: true,
        isOrdered: true
      }

      await axios.post(`${API_CONFIG.baseURL}/v1/recommendations/feedback`, feedbackData)

      // 记录用户行为
      const behaviorData = {
        userId,
        behaviorType: 'order',
        itemType: 'dish',
        itemId: dishId,
        context: {
          recommendSource: item.recommendSource,
          scene: 'home',
          price: item.price,
          calories: item.calories
        }
      }

      await axios.post(`${API_CONFIG.baseURL}/v1/recommendations/behavior`, behaviorData)

      console.log('✓ 下单反馈记录成功')

      // 触发实时更新（最重要！）
      await updateRecommendationsOnBehavior('order', item)
    } catch (error) {
      console.warn('记录下单反馈失败:', error)
      ElMessage.warning('推荐反馈记录失败，请稍后重试')
    }
  }

  /**
   * 记录收藏行为
   */
  const recordFavoriteBehavior = async (item, isFavorited) => {
    const userId = getCurrentUserId()
    const dishId = String(item.dishId || item.id || '')

    try {
      const behaviorData = {
        userId,
        behaviorType: isFavorited ? 'favorite' : 'unfavorite',
        itemType: 'dish',
        itemId: dishId,
        context: {
          recommendSource: item.recommendSource,
          name: item.name
        }
      }

      await axios.post(`${API_CONFIG.baseURL}/v1/recommendations/behavior`, behaviorData)

      console.log(`✓ ${isFavorited ? '收藏' : '取消收藏'}行为记录成功`)
    } catch (error) {
      console.warn('记录收藏行为失败:', error)
    }
  }

  /**
   * 拒绝推荐（增强版 - 记录拒绝原因）
   */
  const rejectRecommendation = async (item, reason = '不感兴趣') => {
    const userId = getCurrentUserId()
    const dishId = String(item.dishId || item.id || '')

    try {
      // 保存拒绝记录到后端（带原因）
      await axios.post(`${API_CONFIG.baseURL}/v1/recommendations/${userId}/reject`, {
        dishId,
        reason
      })

      // 记录拒绝行为
      const behaviorData = {
        userId,
        behaviorType: 'reject',
        itemType: 'dish',
        itemId: dishId,
        context: {
          reason,
          recommendSource: item.recommendSource
        }
      }

      await axios.post(`${API_CONFIG.baseURL}/v1/recommendations/behavior`, behaviorData)

      console.log('✓ 拒绝反馈记录成功')
    } catch (error) {
      console.warn('记录拒绝反馈失败，仅从本地移除:', error)
      // 即使后端失败，也继续本地操作
    }

    // 从推荐列表中移除
    const itemIndex = recommendations.value.findIndex((rec) => rec.id === item.id)
    if (itemIndex > -1) {
      recommendations.value.splice(itemIndex, 1)
    }

    ElMessage.success('已标记为不感兴趣')
  }

  /**
   * 为标签分配随机类型
   */
  const assignRandomTagTypes = (recommendations) => {
    recommendations.forEach((item) => {
      if (item.tags && Array.isArray(item.tags)) {
        item.tagsWithType = item.tags
          .map((tag) => ({
            name: tag,
            type: getRandomTagType()
          }))
          .filter((tag) => tag.name && tag.name.trim() !== '')
      } else {
        item.tagsWithType = []
      }
    })
  }

  /**
   * 从后端获取推荐数据（优化版）
   */
  const fetchRecommendationsFromBackend = async (rejectedDishIds) => {
    try {
      const userId = getCurrentUserId()

      // 传入上下文信息：时段和天气
      const timePeriod = getCurrentTimePeriod()
      const params = {
        scene: 'home',
        limit: 20,
        timePeriod
        // 可以根据实际天气添加 weather 参数
      }

      const response = await axios.get(`${API_CONFIG.baseURL}/v1/recommendations/${userId}`, {
        params
      })

      const data = response.data.data

      // 兼容两种数据格式：1) recommendations 数组 2) dishes 数组
      const rawRecommendations = data?.recommendations || data?.dishes || []

      if (rawRecommendations.length > 0) {
        const personalizedRecs = rawRecommendations
          .filter((dish) => {
            // 过滤掉被拒绝的菜品
            if (!rejectedDishIds || rejectedDishIds.length === 0) return true
            const dishId = dish.dishId || dish.id || dish.dish_id
            return !isRejected(rejectedDishIds, dishId)
          })
          .map((dish, index) => {
            // 兼容不同字段命名风格
            const dishId = dish.dishId || dish.id || dish.dish_id
            const dishName = dish.dishName || dish.name || dish.dish_name
            const dishImage = dish.dishImage || dish.image || dish.dish_image
            const calories = dish.calories || dish.calorie || dish.calories
            const rating = Number(dish.rating ?? dish.avgRating ?? dish.avg_rating)

            return {
              id: dish.id || Date.now() + index + 2000,
              dishId: String(dishId || Date.now() + index + 2000),
              recommendationId: dish.recommendationId || null,
              name: dishName,
              image: dishImage,
              category: dish.category,
              type: dish.type,
              calories: calories,
              price: dish.price,
              rating: Number.isFinite(rating) && rating > 0 ? rating : 4.5,
              tags: dish.tags || [],
              nutrition: dish.nutrition,
              recommendSource: RECOMMENDATION_TYPES.PERSONALIZED,
              reason: dish.reason?.primary || dish.reason || '基于您的饮食偏好推荐',
              score: dish.score, // 保留原始分数用于排序
              rank: dish.rank || index + 1
            }
          })

        assignRandomTagTypes(personalizedRecs)

        // 记录推荐展示行为（异步，不阻塞）
        if (personalizedRecs.length > 0) {
          recordRecommendationView(userId, personalizedRecs).catch((err) => {
            console.warn('记录推荐展示失败:', err)
          })
        }

        return personalizedRecs
      }

      return []
    } catch (error) {
      console.error('获取推荐数据失败:', error)
      throw error
    }
  }

  /**
   * 记录推荐展示行为
   */
  const recordRecommendationView = async (userId, recommendations) => {
    try {
      // 获取当前时段
      const timePeriod = getCurrentTimePeriod()

      // 批量记录推荐展示
      const behaviorData = {
        userId,
        behaviorType: 'view',
        itemType: 'recommendation',
        itemId: `recommendation_list_${timePeriod}`,
        context: {
          timePeriod,
          count: recommendations.length,
          dishIds: recommendations.map((r) => r.dishId).slice(0, 10) // 只记录前10个
        }
      }

      await axios.post(`${API_CONFIG.baseURL}/v1/recommendations/behavior`, behaviorData)
    } catch (error) {
      console.warn('记录推荐展示行为失败:', error)
      // 不抛出错误，避免影响主流程
    }
  }

  /**
   * 获取当前时段
   */
  const getCurrentTimePeriod = () => {
    const hour = new Date().getHours()
    if (hour >= 6 && hour < 10) return '早餐'
    if (hour >= 10 && hour < 14) return '午餐'
    if (hour >= 14 && hour < 18) return '下午茶'
    if (hour >= 18 && hour < 22) return '晚餐'
    return '夜宵'
  }

  /**
   * 推荐去重
   */
  const deduplicateRecommendations = (recommendations) => {
    const seen = new Set()
    return recommendations.filter((item) => {
      const key = item.name + item.type
      if (seen.has(key)) return false
      seen.add(key)
      return true
    })
  }

  /**
   * 保证推荐多样性
   */
  const ensureDiversity = (recommendations) => {
    const typeCounts = {}
    const result = []

    // 先确保每种类型至少有minPerType个
    recommendations.forEach((item) => {
      const source = item.recommendSource || item.type
      if (!typeCounts[source]) {
        typeCounts[source] = 0
      }

      if (
        typeCounts[source] < DIVERSITY_CONFIG.minPerType ||
        typeCounts[source] < DIVERSITY_CONFIG.maxPerType
      ) {
        result.push(item)
        typeCounts[source]++
      }
    })

    // 限制总数
    return result.slice(0, DIVERSITY_CONFIG.totalMax)
  }

  /**
   * 加载所有推荐（仅使用后端数据）
   */
  const loadAllRecommendations = async () => {
    try {
      isLoading.value = true

      // 步骤1: 加载拒绝历史
      let rejectedDishIds = []
      try {
        rejectedDishIds = await loadRejectionHistory()
      } catch (error) {
        console.warn('加载拒绝历史失败，使用空列表:', error)
        rejectedDishIds = []
      }

      // 步骤2: 只加载后端推荐
      let allRecommendations = await fetchRecommendationsFromBackend(rejectedDishIds)

      if (!Array.isArray(allRecommendations)) {
        allRecommendations = []
      }

      // 如果所有推荐都为空，直接返回空数组显示暂无推荐
      if (allRecommendations.length === 0) {
        console.log('暂无推荐数据')
        recommendations.value = []
        return []
      }

      // 步骤3: 去重和多样性保证
      allRecommendations = deduplicateRecommendations(allRecommendations)
      allRecommendations = ensureDiversity(allRecommendations)

      // 步骤5: 为所有推荐分配标签类型
      assignRandomTagTypes(allRecommendations)

      recommendations.value = allRecommendations

      console.log(`✓ 推荐加载完成: 总计${allRecommendations.length}个`)

      return allRecommendations
    } catch (error) {
      console.error('加载推荐失败:', error)
      ElMessage.error('加载推荐失败，请稍后重试')
      recommendations.value = []
      return []
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 下拉刷新
   */
  const onRefresh = async () => {
    refreshing.value = true
    await loadAllRecommendations()
    setTimeout(() => {
      refreshing.value = false
    }, 500)
  }

  /**
   * 实时更新推荐（基于用户行为）
   */
  const updateRecommendationsOnBehavior = async (behaviorType, item) => {
    console.log(`🔄 实时更新推荐: 行为类型=${behaviorType}`)

    // 更新统计
    switch (behaviorType) {
      case 'view':
        recommendationStats.value.totalViews++
        break
      case 'click':
        recommendationStats.value.totalClicks++
        break
      case 'order':
        recommendationStats.value.totalOrders++

        // 下单后触发智能更新
        await smartRefreshAfterOrder(item)
        break
      case 'reject':
        recommendationStats.value.totalRejects++
        break
    }

    lastUpdateTime.value = new Date().toISOString()
  }

  /**
   * 下单后的智能刷新
   */
  const smartRefreshAfterOrder = async (orderedItem) => {
    try {
      // 延迟1秒后更新，避免影响用户操作
      await new Promise((resolve) => setTimeout(resolve, 1000))

      // 获取当前推荐数量
      const currentCount = recommendations.value.length

      // 只刷新，不显示loading
      const newRecs = await fetchRecommendationsFromBackend([])

      if (newRecs && newRecs.length > 0) {
        // 合并现有推荐和新推荐（保持一定连续性）
        const keptRecs = recommendations.value.slice(0, Math.floor(currentCount * 0.7))
        recommendations.value = [...keptRecs, ...newRecs.slice(0, 5)]

        console.log(
          `✓ 智能更新完成: 保留${keptRecs.length}个，新增${Math.min(5, newRecs.length)}个`
        )
      }
    } catch (error) {
      console.warn('智能刷新失败:', error)
    }
  }

  /**
   * 清除推荐缓存
   */
  const clearRecommendCache = () => {
    recommendCache.value.clear()
    console.log('🗑️ 推荐缓存已清除')
  }

  /**
   * 获取推荐统计信息
   */
  const getRecommendationStats = () => {
    return {
      ...recommendationStats.value,
      cacheHitRate: cacheHitRate.value,
      lastUpdateTime: lastUpdateTime.value,
      currentCount: recommendations.value.length
    }
  }

  return {
    // 状态
    recommendations,
    isLoading,
    refreshing,
    hasRecommendations,
    recommendationsCount,

    // 新增：统计和缓存
    recommendationStats,
    cacheHitRate,
    lastUpdateTime,

    // 方法
    loadAllRecommendations,
    rejectRecommendation,
    onRefresh,
    fetchRecommendationsFromBackend,
    deduplicateRecommendations,
    ensureDiversity,

    // 用户行为反馈记录
    recordClickFeedback,
    recordOrderFeedback,
    recordFavoriteBehavior,

    // 新增：实时更新功能
    updateRecommendationsOnBehavior,
    clearRecommendCache,
    getRecommendationStats
  }
}
