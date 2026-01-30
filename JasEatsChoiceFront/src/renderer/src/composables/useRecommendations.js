/**
 * 推荐系统相关逻辑
 */

import { ref } from 'vue'
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
   * 拒绝推荐
   */
  const rejectRecommendation = async (item) => {
    const userId = String(authStore.userId || 1)
    const dishId = String(item.dishId || item.id || '')

    // 保存拒绝记录到后端
    await saveRejectionHistory(userId, dishId)

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
   * 从后端获取推荐数据
   */
  const fetchRecommendationsFromBackend = async (rejectedDishIds) => {
    try {
      const userId = parseInt(String(authStore.userId || 1) || '1', 10)

      const response = await axios.get(`${API_CONFIG.baseURL}/v1/recommendations/${userId}`)

      const data = response.data.data
      if (data && data.dishes) {
        const personalizedRecs = data.dishes
          .filter(dish => {
            // 过滤掉被拒绝的菜品
            if (!rejectedDishIds || rejectedDishIds.length === 0) return true
            return !isRejected(rejectedDishIds, dish.dishId || dish.id)
          })
          .map((dish, index) => ({
            ...dish,
            id: Date.now() + index + 2000,
            dishId: dish.dishId || dish.id || String(Date.now() + index + 2000), // 确保 dishId 存在
            recommendSource: RECOMMENDATION_TYPES.PERSONALIZED,
            reason: dish.reason || '基于您的饮食偏好推荐'
          }))

        assignRandomTagTypes(personalizedRecs)
        return personalizedRecs
      }
      return []
    } catch (error) {
      console.error('获取推荐数据失败:', error)
      return null
    }
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
   * 加载所有推荐
   */
  const loadAllRecommendations = async () => {
    try {
      isLoading.value = true

      // 加载拒绝历史（从后端）
      const rejectedDishIds = await loadRejectionHistory()

      // 并行加载多种推荐
      const [personalizedRecs, weatherTimeRecs] = await Promise.all([
        fetchRecommendationsFromBackend(rejectedDishIds),
        updateRecommendationsByWeatherAndTime(rejectedDishIds)
      ])

      const festivalRecs = addFestivalRecommendations(rejectedDishIds)

      // 合并推荐
      let allRecommendations = []

      if (personalizedRecs && personalizedRecs.length > 0) {
        allRecommendations = [...allRecommendations, ...personalizedRecs]
      } else {
        // 后端失败时使用天气推荐作为后备
        allRecommendations = [...allRecommendations, ...weatherTimeRecs]
      }

      allRecommendations = [...allRecommendations, ...festivalRecs]

      // 去重和多样性保证
      allRecommendations = deduplicateRecommendations(allRecommendations)
      allRecommendations = ensureDiversity(allRecommendations)

      // 为所有推荐分配标签类型
      assignRandomTagTypes(allRecommendations)

      recommendations.value = allRecommendations

      return allRecommendations
    } catch (error) {
      console.error('加载推荐失败:', error)
      ElMessage.error('加载推荐失败，请稍后重试')
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

  return {
    // 状态
    recommendations,
    isLoading,
    refreshing,

    // 方法
    loadAllRecommendations,
    rejectRecommendation,
    onRefresh,
    fetchRecommendationsFromBackend,
    deduplicateRecommendations,
    ensureDiversity
  }
}
