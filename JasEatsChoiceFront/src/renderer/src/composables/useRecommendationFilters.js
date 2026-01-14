/**
 * 推荐筛选和排序功能
 */
import { ref, computed } from 'vue'
import { CALORIE_RANGES, RECOMMENDATION_TYPES } from '../constants/recommendationConstants.js'

export function useRecommendationFilters(recommendations) {
  // 筛选条件
  const selectedCalorieRange = ref(null)
  const selectedTypes = ref([])
  const selectedSources = ref([])
  const searchKeyword = ref('')

  // 排序条件
  const sortBy = ref('default') // default, calories_asc, calories_desc, rating_desc, rating_asc

  /**
   * 根据卡路里范围筛选
   */
  const filterByCalories = (items) => {
    if (!selectedCalorieRange.value) return items

    // 通过 ID 找到对应的范围对象
    const range = CALORIE_RANGES.find(r => r.id === selectedCalorieRange.value)
    if (!range) return items

    return items.filter((item) => {
      const calories = item.calories || 0
      return calories >= range.min && calories <= range.max
    })
  }

  /**
   * 根据类型筛选
   */
  const filterByTypes = (items) => {
    if (selectedTypes.value.length === 0) return items

    return items.filter((item) => {
      return selectedTypes.value.some((type) => {
        return item.type === type || (item.tags && item.tags.includes(type))
      })
    })
  }

  /**
   * 根据推荐来源筛选
   */
  const filterBySources = (items) => {
    if (selectedSources.value.length === 0) return items

    return items.filter((item) => {
      const source = item.recommendSource || item.type
      return selectedSources.value.includes(source)
    })
  }

  /**
   * 根据关键词搜索
   */
  const filterByKeyword = (items) => {
    if (!searchKeyword.value.trim()) return items

    const keyword = searchKeyword.value.toLowerCase()
    return items.filter((item) => {
      return (
        item.name.toLowerCase().includes(keyword) ||
        (item.reason && item.reason.toLowerCase().includes(keyword)) ||
        (item.tags && item.tags.some((tag) => tag.toLowerCase().includes(keyword)))
      )
    })
  }

  /**
   * 排序
   */
  const sortItems = (items) => {
    const sorted = [...items]

    switch (sortBy.value) {
      case 'calories_asc':
        return sorted.sort((a, b) => (a.calories || 0) - (b.calories || 0))
      case 'calories_desc':
        return sorted.sort((a, b) => (b.calories || 0) - (a.calories || 0))
      case 'rating_desc':
        return sorted.sort((a, b) => (b.rating || 0) - (a.rating || 0))
      case 'rating_asc':
        return sorted.sort((a, b) => (a.rating || 0) - (b.rating || 0))
      default:
        return sorted
    }
  }

  /**
   * 应用所有筛选和排序
   */
  const filteredAndSortedRecommendations = computed(() => {
    let result = recommendations.value

    // 应用所有筛选
    result = filterByCalories(result)
    result = filterByTypes(result)
    result = filterBySources(result)
    result = filterByKeyword(result)

    // 应用排序
    result = sortItems(result)

    return result
  })

  /**
   * 重置所有筛选条件
   */
  const resetFilters = () => {
    selectedCalorieRange.value = null
    selectedTypes.value = []
    selectedSources.value = []
    searchKeyword.value = ''
    sortBy.value = 'default'
  }

  /**
   * 获取可用的类型列表（动态从推荐数据中提取）
   */
  const availableTypes = computed(() => {
    const typeSet = new Set()
    recommendations.value.forEach((item) => {
      if (item.type) typeSet.add(item.type)
      if (item.tags) {
        item.tags.forEach((tag) => typeSet.add(tag))
      }
    })
    return Array.from(typeSet).sort()
  })

  /**
   * 检查是否有激活的筛选条件
   */
  const hasActiveFilters = computed(() => {
    return (
      selectedCalorieRange.value !== null ||
      selectedTypes.value.length > 0 ||
      selectedSources.value.length > 0 ||
      searchKeyword.value.trim() !== ''
    )
  })

  return {
    // 筛选条件
    selectedCalorieRange,
    selectedTypes,
    selectedSources,
    searchKeyword,
    sortBy,

    // 常量
    CALORIE_RANGES,
    RECOMMENDATION_TYPES,

    // 计算属性
    filteredAndSortedRecommendations,
    availableTypes,
    hasActiveFilters,

    // 方法
    resetFilters
  }
}
