/**
 * 搜索历史管理 Composable
 * 提供搜索历史的增删查改功能
 */
import { ref, computed } from 'vue'

const STORAGE_KEY = 'searchHistory'
const MAX_HISTORY = 20 // 最多保存20条历史记录

// 全局搜索历史状态
const searchHistory = ref([])

/**
 * 搜索历史管理 Hook
 */
export function useSearchHistory() {
  /**
   * 从本地存储加载搜索历史
   */
  const loadHistory = () => {
    try {
      const historyStr = uni.getStorageSync(STORAGE_KEY)
      if (historyStr) {
        searchHistory.value = JSON.parse(historyStr)
      }
    } catch (error) {
      console.error('加载搜索历史失败:', error)
      searchHistory.value = []
    }
  }

  /**
   * 保存搜索历史到本地存储
   */
  const saveHistory = () => {
    try {
      uni.setStorageSync(STORAGE_KEY, JSON.stringify(searchHistory.value))
    } catch (error) {
      console.error('保存搜索历史失败:', error)
    }
  }

  /**
   * 添加搜索历史
   * @param {string} keyword - 搜索关键词
   */
  const addHistory = (keyword) => {
    if (!keyword || typeof keyword !== 'string') {
      return
    }

    const trimmedKeyword = keyword.trim()
    if (!trimmedKeyword) {
      return
    }

    // 删除已存在的相同关键词（将已有记录移除）
    const index = searchHistory.value.findIndex(item => item === trimmedKeyword)
    if (index > -1) {
      searchHistory.value.splice(index, 1)
    }

    // 添加到开头
    searchHistory.value.unshift(trimmedKeyword)

    // 限制数量
    if (searchHistory.value.length > MAX_HISTORY) {
      searchHistory.value = searchHistory.value.slice(0, MAX_HISTORY)
    }

    // 保存到本地
    saveHistory()
  }

  /**
   * 删除单条搜索历史
   * @param {string} keyword - 要删除的关键词
   */
  const removeHistory = (keyword) => {
    const index = searchHistory.value.findIndex(item => item === keyword)
    if (index > -1) {
      searchHistory.value.splice(index, 1)
      saveHistory()
    }
  }

  /**
   * 清空搜索历史
   */
  const clearHistory = () => {
    uni.showModal({
      title: '确认清空',
      content: '确定要清空所有搜索历史吗？',
      success: (res) => {
        if (res.confirm) {
          searchHistory.value = []
          saveHistory()
          uni.showToast({
            title: '已清空',
            icon: 'success'
          })
        }
      }
    })
  }

  /**
   * 获取搜索历史列表
   */
  const getHistoryList = () => {
    return searchHistory.value
  }

  /**
   * 获取热门搜索（基于历史频率）
   */
  const getFrequentSearches = (limit = 5) => {
    // 这里可以实现更复杂的频率统计
    // 目前简单地返回前几条
    return searchHistory.value.slice(0, limit)
  }

  // 计算属性
  const hasHistory = computed(() => searchHistory.value.length > 0)
  const historyCount = computed(() => searchHistory.value.length)

  // 初始化时加载历史
  loadHistory()

  return {
    // 状态
    searchHistory,
    hasHistory,
    historyCount,

    // 方法
    addHistory,
    removeHistory,
    clearHistory,
    getHistoryList,
    getFrequentSearches,
    loadHistory
  }
}
