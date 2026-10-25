/**
 * 智能推荐系统 Composable
 * 与后端推荐系统API对接（与桌面端保持一致）
 * 提供多源推荐：个性化推荐 + 天气推荐 + 节日推荐
 */
import { ref, computed } from 'vue'
import { recommendationApi } from '@/api'
import { useUserStore, useLocationStore } from '@/store'

// 推荐数据缓存
let recommendationsCache = null
let cacheTimestamp = 0
const CACHE_DURATION = 5 * 60 * 1000 // 5分钟缓存

/**
 * 获取当前时段
 */
const getTimePeriod = () => {
  const hour = new Date().getHours()
  if (hour >= 6 && hour < 10) return '早餐'
  if (hour >= 10 && hour < 14) return '午餐'
  if (hour >= 14 && hour < 18) return '下午茶'
  if (hour >= 18 && hour < 22) return '晚餐'
  return '夜宵'
}

/**
 * 推荐系统 Hook
 */
export function useRecommendations() {
  const userStore = useUserStore()
  const locationStore = useLocationStore()

  // 状态
  const recommendations = ref([])
  const isLoading = ref(false)
  const error = ref(null)

  // 计算属性
  const hasRecommendations = computed(() => recommendations.value.length > 0)
  const isEmpty = computed(() => !isLoading.value && recommendations.value.length === 0)

  /**
   * 获取天气类型
   */
  const getWeatherType = () => {
    if (!locationStore.weather) return 'sunny'

    const condition = locationStore.weather.condition || locationStore.weather.text || ''
    const temp = locationStore.weather.temperature || locationStore.weather.temp || 0

    if (condition.includes('雨')) return 'rainy'
    if (condition.includes('雪')) return 'cold'
    if (temp > 30) return 'hot'
    if (temp < 10) return 'cold'

    return 'sunny'
  }

  /**
   * 加载推荐菜品（使用后端推荐系统）
   * @param {Object} options - 推荐选项
   * @param {string} options.scene - 推荐场景: home/personal/cart/dish_detail
   * @param {number} options.limit - 返回数量
   * @param {boolean} options.useTime - 是否使用时段推荐
   * @param {boolean} options.useWeather - 是否使用天气推荐
   * @param {boolean} options.forceRefresh - 是否强制刷新（忽略缓存）
   */
  const loadRecommendations = async (options = {}) => {
    try {
      // 检查缓存
      const now = Date.now()
      if (!options.forceRefresh && recommendationsCache && (now - cacheTimestamp < CACHE_DURATION)) {
        console.log('✓ 使用缓存的推荐数据')
        recommendations.value = recommendationsCache
        return recommendationsCache
      }

      isLoading.value = true
      error.value = null

      // 获取用户ID
      const userId = userStore.userInfo?.userId || userStore.userInfo?.id || '1'

      // 构建推荐参数
      const params = {
        scene: options.scene || 'home',
        limit: options.limit || 20
      }

      // 添加时段参数
      if (options.useTime !== false) {
        params.timePeriod = getTimePeriod()
      }

      // 添加天气参数（可选）
      if (options.useWeather !== false && locationStore.weather) {
        params.weather = getWeatherType()
      }

      console.log('📡 调用后端推荐系统:', params)

      // 调用后端推荐接口
      const res = await recommendationApi.getRecommendations(userId, params)

      // 处理返回数据
      let dishes = []
      if (res && res.data) {
        if (res.data.recommendations) {
          dishes = res.data.recommendations
        } else if (Array.isArray(res.data)) {
          dishes = res.data
        }
      }

      // 数据映射（统一字段名）
      recommendations.value = dishes.map(item => ({
        id: item.dishId || item.id,
        dishId: item.dishId || item.id,
        name: item.dishName || item.name,
        description: item.description || item.desc || '',
        price: item.price ? String(item.price) : '0',
        calories: item.calories || item.calorie || 0,
        category: item.category,
        image: item.image || item.coverImage,
        rating: item.rating || item.avgRating || 4.5,
        score: item.score,
        recommendReason: item.recommendReason || item.reason,
        recommendSource: item.recommendSource || '系统推荐',
        tags: item.tags || [],
        _rawRecommendation: item
      }))

      // 更新缓存
      recommendationsCache = recommendations.value
      cacheTimestamp = now

      console.log(`✅ 推荐加载成功: ${recommendations.value.length}个菜品`)

      return recommendations.value
    } catch (err) {
      console.error('❌ 加载推荐失败:', err)
      error.value = err.message || '加载推荐失败'
      recommendations.value = []

      // 降级方案：调用简单的推荐接口
      try {
        console.log('🔄 降级方案：使用简单推荐接口')
        const { dishApi } = await import('@/api')
        const fallbackRes = await dishApi.getRecommend({
          page: 1,
          size: 10
        })

        if (fallbackRes && Array.isArray(fallbackRes)) {
          recommendations.value = fallbackRes.map(item => ({
            id: item.dishId || item.id,
            dishId: item.dishId || item.id,
            name: item.dishName || item.name,
            description: item.description || item.desc || '',
            price: String(item.price || '0'),
            image: item.image || item.coverImage,
            recommendSource: '基础推荐'
          }))
          console.log(`✅ 降级方案成功: ${recommendations.value.length}个菜品`)
        }
      } catch (fallbackErr) {
        console.error('❌ 降级方案也失败:', fallbackErr)
      }

      return recommendations.value
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 刷新推荐
   */
  const refreshRecommendations = async () => {
    // 清除缓存
    recommendationsCache = null
    cacheTimestamp = 0

    const userId = userStore.userInfo?.userId || userStore.userInfo?.id || '1'

    try {
      isLoading.value = true
      const res = await recommendationApi.refreshRecommendations(userId)

      let dishes = []
      if (res && res.data) {
        if (res.data.recommendations) {
          dishes = res.data.recommendations
        } else if (Array.isArray(res.data)) {
          dishes = res.data
        }
      }

      recommendations.value = dishes.map(item => ({
        id: item.dishId || item.id,
        dishId: item.dishId || item.id,
        name: item.dishName || item.name,
        description: item.description || '',
        price: String(item.price || '0'),
        image: item.image,
        recommendSource: '刷新推荐',
        _rawRecommendation: item
      }))

      // 更新缓存
      recommendationsCache = recommendations.value
      cacheTimestamp = Date.now()

      console.log('✅ 推荐刷新成功')
      uni.showToast({ title: '刷新成功', icon: 'success' })
    } catch (err) {
      console.error('❌ 刷新推荐失败:', err)
      uni.showToast({ title: '刷新失败', icon: 'none' })
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 记录点击反馈
   */
  const recordClickFeedback = async (item) => {
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id || '1'

    try {
      await recommendationApi.recordFeedback({
        userId,
        dishId: String(item.dishId || item.id),
        recommendationId: String(item.id || item.dishId),
        isClicked: true,
        isOrdered: false
      })
      console.log('✓ 点击反馈已记录')
    } catch (err) {
      console.warn('记录点击反馈失败:', err)
      // 不阻塞用户操作
    }
  }

  /**
   * 记录下单反馈
   */
  const recordOrderFeedback = async (item) => {
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id || '1'

    try {
      await recommendationApi.recordFeedback({
        userId,
        dishId: String(item.dishId || item.id),
        recommendationId: String(item.id || item.dishId),
        isClicked: true,
        isOrdered: true
      })
      console.log('✓ 下单反馈已记录')
    } catch (err) {
      console.warn('记录下单反馈失败:', err)
    }
  }

  /**
   * 拒绝推荐
   */
  const rejectRecommendation = async (item, reason = '不感兴趣') => {
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id || '1'

    try {
      await recommendationApi.rejectRecommendation(userId, {
        dishId: String(item.dishId || item.id),
        reason
      })

      // 从列表中移除
      const index = recommendations.value.findIndex(r => r.id === item.id)
      if (index > -1) {
        recommendations.value.splice(index, 1)
      }

      uni.showToast({ title: '已标记为不感兴趣', icon: 'success' })
    } catch (err) {
      console.error('拒绝推荐失败:', err)
      uni.showToast({ title: '操作失败', icon: 'none' })
    }
  }

  return {
    // 状态
    recommendations,
    isLoading,
    error,

    // 计算属性
    hasRecommendations,
    isEmpty,

    // 方法
    loadRecommendations,
    refreshRecommendations,
    recordClickFeedback,
    recordOrderFeedback,
    rejectRecommendation,

    // 工具函数
    getTimePeriod,
    getWeatherType
  }
}
