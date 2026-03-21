/**
 * Composable: useListLoad
 * 用途：通用列表加载逻辑
 * 创建时间：2026-03-20
 */
import { ref } from 'vue'

/**
 * 通用列表加载 Composable
 * @param {Function} loadFn - 加载函数，接收 (page, size) 参数，返回数据数组
 * @param {Object} options - 配置选项
 * @param {number} options.pageSize - 每页大小，默认 20
 * @param {boolean} options.immediate - 是否立即加载，默认 true
 * @returns {Object} 返回响应式数据和方法
 */
export function useListLoad(loadFn, options = {}) {
  const {
    pageSize = 20,
    immediate = true
  } = options

  // 响应式数据
  const list = ref([])
  const loading = ref(false)
  const refreshing = ref(false)
  const hasMore = ref(true)
  const currentPage = ref(1)

  /**
   * 加载数据
   * @param {boolean} refresh - 是否刷新（重置到第一页）
   */
  const loadData = async (refresh = false) => {
    // 防止重复加载
    if (loading.value) return

    if (refresh) {
      currentPage.value = 1
      refreshing.value = true
    } else {
      loading.value = true
    }

    try {
      const data = await loadFn(currentPage.value, pageSize)

      if (refresh) {
        list.value = data
      } else {
        list.value = [...list.value, ...data]
      }

      hasMore.value = data.length >= pageSize

      if (!refresh && data.length >= pageSize) {
        currentPage.value++
      }
    } catch (error) {
      console.error('加载数据失败:', error)
      throw error
    } finally {
      loading.value = false
      refreshing.value = false
    }
  }

  /**
   * 刷新数据
   */
  const refresh = () => {
    return loadData(true)
  }

  /**
   * 加载更多
   */
  const loadMore = () => {
    if (!loading.value && hasMore.value) {
      return loadData(false)
    }
  }

  // 立即加载（如果配置）
  if (immediate) {
    loadData(true)
  }

  return {
    // 数据
    list,
    loading,
    refreshing,
    hasMore,
    currentPage,

    // 方法
    loadData,
    refresh,
    loadMore
  }
}

export default useListLoad
