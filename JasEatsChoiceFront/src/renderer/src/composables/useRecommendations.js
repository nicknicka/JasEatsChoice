/**
 * 推荐系统相关逻辑
 */
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { API_CONFIG } from '../config/index.js'
import {
  FESTIVAL_DISHES,
  getCurrentFestival,
  getDishIcon
} from '../constants/festivalDishes.js'
import {
  RECOMMENDATION_TYPES,
  MOCK_DISHES,
  REJECTION_THRESHOLD,
  DIVERSITY_CONFIG,
  getRandomTagType,
  getCurrentTimePeriod
} from '../constants/recommendationConstants.js'

export function useRecommendations() {
  // 状态管理
  const recommendations = ref([])
  const isLoading = ref(false)
  const refreshing = ref(false)

  /**
   * 加载用户拒绝的推荐历史
   */
  const loadRejectionHistory = () => {
    const saved = localStorage.getItem('rejectionHistory')
    return saved ? JSON.parse(saved) : []
  }

  /**
   * 保存用户拒绝的推荐历史
   */
  const saveRejectionHistory = (history) => {
    localStorage.setItem('rejectionHistory', JSON.stringify(history))
  }

  /**
   * 拒绝推荐
   */
  const rejectRecommendation = (item) => {
    let rejectionHistory = loadRejectionHistory()

    const existingIndex = rejectionHistory.findIndex(
      (entry) => entry.name === item.name && entry.type === item.type
    )

    if (existingIndex > -1) {
      rejectionHistory[existingIndex].count += 1
    } else {
      rejectionHistory.push({
        name: item.name,
        type: item.type,
        tags: item.tags,
        count: 1,
        rejectedAt: new Date().toISOString()
      })
    }

    saveRejectionHistory(rejectionHistory)

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
  const generateWeatherTimeRecommendations = (timeType, weatherTags) => {
    const rejectionHistory = loadRejectionHistory()

    const filteredDishes = MOCK_DISHES.filter((dish) => {
      const matchesCriteria =
        dish.type.includes(timeType) || weatherTags.some((tag) => dish.tags.includes(tag))

      const rejectionEntry = rejectionHistory.find(
        (entry) => entry.name.includes(dish.name) || dish.name.includes(entry.name)
      )

      return matchesCriteria && (!rejectionEntry || rejectionEntry.count <= REJECTION_THRESHOLD)
    })

    return filteredDishes.map((dish, index) => ({
      id: Date.now() + index,
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
  const addFestivalRecommendations = () => {
    const currentFestival = getCurrentFestival()
    if (!currentFestival || !FESTIVAL_DISHES[currentFestival]) {
      return []
    }

    const festivalDishList = FESTIVAL_DISHES[currentFestival]
    const rejectionHistory = loadRejectionHistory()

    const festivalRecommendations = festivalDishList
      .filter((dishName) => {
        const rejectionEntry = rejectionHistory.find(
          (entry) => entry.name.includes(dishName) || dishName.includes(entry.name)
        )
        return !rejectionEntry || rejectionEntry.count <= REJECTION_THRESHOLD
      })
      .map((dishName, index) => {
        return {
          id: Date.now() + index + 1000,
          name: `${currentFestival}特色: ${dishName}`,
          type: RECOMMENDATION_TYPES.FESTIVAL,
          calories: 0,
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
  const updateRecommendationsByWeatherAndTime = async () => {
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

      return generateWeatherTimeRecommendations(timeType, weatherTags)
    } catch (error) {
      console.error('天气推荐失败:', error)
      return []
    }
  }

  /**
   * 从后端获取推荐数据
   */
  const fetchRecommendationsFromBackend = async () => {
    try {
      isLoading.value = true
      const userId = parseInt(localStorage.getItem('userId') || '1', 10)

      const response = await axios.get(
        `${API_CONFIG.baseURL}/v1/recommend/recommend/${userId}`
      )

      const data = response.data.data
      if (data && data.dishes) {
        const personalizedRecs = data.dishes.map((dish, index) => ({
          ...dish,
          id: Date.now() + index + 2000,
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
    } finally {
      isLoading.value = false
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

      // 并行加载多种推荐
      const [personalizedRecs, weatherTimeRecs] = await Promise.all([
        fetchRecommendationsFromBackend(),
        updateRecommendationsByWeatherAndTime()
      ])

      const festivalRecs = addFestivalRecommendations()

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
