/**
 * 推荐系统相关逻辑
 */

import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { API_CONFIG } from '../config/index.js'
import {
  FESTIVAL_DISHES,
  getCurrentFestival,
  getDishIcon,
  getDishCalories
} from '../constants/festivalDishes.js'
import {
  RECOMMENDATION_TYPES,
  MOCK_DISHES,
  REJECTION_THRESHOLD,
  DIVERSITY_CONFIG,
  getRandomTagType,
  getCurrentTimePeriod
} from '../constants/recommendationConstants.js'
import pinia from '../store'
import { useAuthStore } from '../store/authStore'

export function useRecommendations() {
  // 在函数内部获取 store，避免模块加载时的初始化问题
  const authStore = useAuthStore(pinia)

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
      const userId = String(authStore.userId || 1)
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
      const existingIndex = history.findIndex(
        (entry) => entry.dishId === dishId
      )
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
    return rejectedDishIds.some(id => String(id) === normalizedDishId)
  }

  /**
   * 记录点击反馈
   */
  const recordClickFeedback = async (item) => {
    const userId = String(authStore.userId || 1)
    const dishId = String(item.dishId || item.id || '')
    const recommendationId = item.recommendationId || item.id || ''

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
      // 不阻塞用户操作
    }
  }

  /**
   * 记录下单反馈
   */
  const recordOrderFeedback = async (item) => {
    const userId = String(authStore.userId || 1)
    const dishId = String(item.dishId || item.id || '')
    const recommendationId = item.recommendationId || item.id || ''

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
      // 不阻塞用户操作
    }
  }

  /**
   * 记录收藏行为
   */
  const recordFavoriteBehavior = async (item, isFavorited) => {
    const userId = String(authStore.userId || 1)
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
    const userId = String(authStore.userId || 1)
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
   * 根据天气和时间生成推荐菜品
   */
  const generateWeatherTimeRecommendations = (timeType, weatherTags, rejectedDishIds) => {
    const filteredDishes = MOCK_DISHES.filter((dish) => {
      const matchesCriteria =
        dish.type.includes(timeType) || weatherTags.some((tag) => dish.tags.includes(tag))

      // 检查是否被拒绝过（基于菜品名称匹配）
      const isRejectedDish = rejectedDishIds && rejectedDishIds.some(id => {
        // 这里使用名称匹配作为临时方案，因为 MOCK_DISHES 没有 dishId
        const rejectedDish = MOCK_DISHES.find(d => String(d.id || '') === String(id))
        return rejectedDish && rejectedDish.name === dish.name
      })

      return matchesCriteria && !isRejectedDish
    })

    return filteredDishes.map((dish, index) => ({
      id: Date.now() + index,
      dishId: String(dish.id || Date.now() + index), // 添加 dishId 用于后端记录
      name: `${timeType}推荐: ${dish.name}`,
      type: RECOMMENDATION_TYPES.TIME,
      calories: dish.calories,
      tags: [...dish.tags, timeType],
      nutrition: dish.nutrition,
      reason: `${timeType}${weatherTags.length > 0 ? `，${weatherTags.join('、')}适合` : '适合'}`,
      rating: 4.8,
      image: '🍱',
      recommendSource: RECOMMENDATION_TYPES.TIME
    }))
  }

  /**
   * 根据节日/节气添加特色菜品推荐
   */
  const addFestivalRecommendations = (rejectedDishIds) => {
    const currentFestival = getCurrentFestival()
    if (!currentFestival || !FESTIVAL_DISHES[currentFestival]) {
      return []
    }

    const festivalDishList = FESTIVAL_DISHES[currentFestival]

    // 非食品项目列表（需要过滤掉）
    const nonFoodItems = ['花灯', '年夜饭', '贴秋膘'] // 贴秋膘是习俗不是菜品

    const festivalRecommendations = festivalDishList
      .filter((dishName) => {
        // 过滤非食品项目
        if (nonFoodItems.some(nonFood => dishName.includes(nonFood))) {
          return false
        }

        // 检查是否被拒绝过
        return !rejectedDishIds || !rejectedDishIds.some(id => {
          // 这里使用名称匹配作为临时方案
          return String(id).includes(dishName) || dishName.includes(String(id))
        })
      })
      .map((dishName, index) => {
        return {
          id: Date.now() + index + 1000,
          dishId: `festival_${currentFestival}_${dishName}`, // 添加 dishId 用于后端记录
          name: `${currentFestival}特色: ${dishName}`,
          type: RECOMMENDATION_TYPES.FESTIVAL,
          calories: getDishCalories(dishName), // 使用卡路里估算函数
          tags: ['节日特供', currentFestival],
          reason: `${currentFestival}传统特色美食`,
          rating: 4.9,
          image: getDishIcon(dishName),
          recommendSource: RECOMMENDATION_TYPES.FESTIVAL
        }
      })

    return festivalRecommendations
  }

  /**
   * 天气与时间双维度推荐逻辑
   */
  const updateRecommendationsByWeatherAndTime = async (rejectedDishIds) => {
    const savedSettings = localStorage.getItem('userSettings')
    let weatherRecommendationEnabled = true

    if (savedSettings) {
      const parsedSettings = JSON.parse(savedSettings)
      weatherRecommendationEnabled = parsedSettings.privacy?.weatherRecommendation !== false
    }

    if (!weatherRecommendationEnabled) {
      return []
    }

    try {
      const response = await axios.get(API_CONFIG.baseURL + API_CONFIG.weather.current, {
        params: { city: '北京' }
      })
      const weatherData = response.data.data
      const { temperature, humidity } = weatherData

      const timeType = getCurrentTimePeriod()

      let weatherTags = []
      if (temperature > 30) weatherTags.push('冰饮', '凉菜', '轻食')
      else if (temperature < 10) weatherTags.push('热饮', '热菜', '火锅')
      if (humidity > 80) weatherTags.push('祛湿粥品', '清淡饮食')

      return generateWeatherTimeRecommendations(timeType, weatherTags, rejectedDishIds)
    } catch (error) {
      console.error('天气推荐失败:', error)
      return []
    }
  }

  /**
   * 从后端获取推荐数据（优化版）
   */
  const fetchRecommendationsFromBackend = async (rejectedDishIds) => {
    try {
      const userId = String(authStore.userId || 1)

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
        // 调试日志：检查后端返回的 score
        console.log('=== 推荐得分调试 ===')
        rawRecommendations.forEach((dish, index) => {
          const dishId = dish.dishId || dish.id || dish.dish_id
          const score = dish.score || dish.rating || dish.avgRating || dish.avg_rating
          console.log(`#${index + 1} ${dish.dishName || dish.name}: score=${score}`)
          if (score > 1) {
            console.warn(`⚠️ 警告: ${dish.dishName || dish.name} 的 score=${score} 超过 1.0`)
          }
        })
        console.log('==================')

        const personalizedRecs = rawRecommendations
          .filter(dish => {
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
            const score = dish.score || dish.rating || dish.avgRating || dish.avg_rating

            return {
              id: dish.id || Date.now() + index + 2000,
              dishId: String(dishId || Date.now() + index + 2000),
              name: dishName,
              image: dishImage,
              category: dish.category,
              type: dish.type,
              calories: calories,
              price: dish.price,
              rating: score || 4.5,
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
          recordRecommendationView(userId, personalizedRecs).catch(err => {
            console.warn('记录推荐展示失败:', err)
          })
        }

        return personalizedRecs
      }

      return []
    } catch (error) {
      console.error('获取推荐数据失败:', error)
      // 降级方案：返回 null 让系统使用本地推荐
      return null
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
          dishIds: recommendations.map(r => r.dishId).slice(0, 10) // 只记录前10个
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
   * 加载所有推荐（优化版 - 多层降级策略）
   */
  const loadAllRecommendations = async () => {
    try {
      isLoading.value = true

      // 步骤1: 加载拒绝历史（带降级）
      let rejectedDishIds = []
      try {
        rejectedDishIds = await loadRejectionHistory()
      } catch (error) {
        console.warn('加载拒绝历史失败，使用空列表:', error)
        rejectedDishIds = []
      }

      // 步骤2: 并行加载多种推荐（独立错误处理）
      let personalizedRecs = []
      let weatherTimeRecs = []
      let festivalRecs = []

      const results = await Promise.allSettled([
        fetchRecommendationsFromBackend(rejectedDishIds),
        updateRecommendationsByWeatherAndTime(rejectedDishIds)
      ])

      // 处理后端推荐结果
      if (results[0].status === 'fulfilled' && results[0].value) {
        personalizedRecs = results[0].value
        console.log(`✓ 后端推荐加载成功: ${personalizedRecs.length}个`)
      } else if (results[0].status === 'rejected') {
        console.warn('✗ 后端推荐失败，将使用降级方案:', results[0].reason)
      } else {
        console.log('ℹ 后端推荐返回空，使用本地推荐')
      }

      // 处理天气推荐结果
      if (results[1].status === 'fulfilled') {
        weatherTimeRecs = results[1].value
        console.log(`✓ 天气推荐加载成功: ${weatherTimeRecs.length}个`)
      } else {
        console.warn('✗ 天气推荐失败:', results[1].reason)
      }

      // 处理节日推荐
      try {
        festivalRecs = addFestivalRecommendations(rejectedDishIds)
        console.log(`✓ 节日推荐加载成功: ${festivalRecs.length}个`)
      } catch (error) {
        console.warn('✗ 节日推荐失败:', error)
      }

      // 步骤3: 智能合并推荐（多层降级）
      let allRecommendations = []

      // 优先级1: 后端个性化推荐
      if (personalizedRecs.length > 0) {
        allRecommendations = [...allRecommendations, ...personalizedRecs]
        console.log(`使用后端推荐: ${personalizedRecs.length}个`)
      }
      // 降级1: 如果后端推荐数量不足，补充天气推荐
      else if (weatherTimeRecs.length > 0) {
        allRecommendations = [...allRecommendations, ...weatherTimeRecs]
        console.log(`使用天气推荐降级: ${weatherTimeRecs.length}个`)
      }

      // 降级2: 补充节日推荐
      if (festivalRecs.length > 0) {
        allRecommendations = [...allRecommendations, ...festivalRecs]
      }

      // 降级3: 如果所有推荐都为空，使用Mock数据
      if (allRecommendations.length === 0) {
        console.warn('⚠ 所有推荐源均失败，使用Mock数据')
        allRecommendations = MOCK_DISHES.slice(0, 10).map((dish, index) => ({
          id: Date.now() + index,
          dishId: String(dish.id || Date.now() + index),
          name: dish.name,
          type: dish.type,
          calories: dish.calories,
          tags: dish.tags,
          nutrition: dish.nutrition,
          reason: '系统推荐',
          rating: 4.5,
          image: '🍱',
          recommendSource: 'system'
        }))
      }

      // 步骤4: 去重和多样性保证
      allRecommendations = deduplicateRecommendations(allRecommendations)
      allRecommendations = ensureDiversity(allRecommendations)

      // 步骤5: 为所有推荐分配标签类型
      assignRandomTagTypes(allRecommendations)

      recommendations.value = allRecommendations

      console.log(`✓ 推荐加载完成: 总计${allRecommendations.length}个`)

      return allRecommendations
    } catch (error) {
      console.error('❌ 加载推荐失败:', error)
      ElMessage.error('加载推荐失败，请稍后重试')

      // 最终降级: 返回空数组而不是抛出错误
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
      await new Promise(resolve => setTimeout(resolve, 1000))

      // 获取当前推荐数量
      const currentCount = recommendations.value.length

      // 只刷新，不显示loading
      const newRecs = await fetchRecommendationsFromBackend([])

      if (newRecs && newRecs.length > 0) {
        // 合并现有推荐和新推荐（保持一定连续性）
        const keptRecs = recommendations.value.slice(0, Math.floor(currentCount * 0.7))
        recommendations.value = [...keptRecs, ...newRecs.slice(0, 5)]

        console.log(`✓ 智能更新完成: 保留${keptRecs.length}个，新增${Math.min(5, newRecs.length)}个`)
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
