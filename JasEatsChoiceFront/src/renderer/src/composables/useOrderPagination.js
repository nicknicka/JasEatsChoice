/**
 * 订单分页组合式函数
 */
import { ref, computed, watch } from 'vue'

// 默认分页配置
const DEFAULT_PAGE_SIZE = 5
const PAGE_SIZE_OPTIONS = [5, 10, 20, 50]

/**
 * 订单分页管理
 * @param {Ref<Array>} sortedOrders - 排序后的订单数组
 * @param {Function} onReset - 重置回调
 */
export function useOrderPagination(sortedOrders, onReset) {
  // 分页状态
  const currentPage = ref(1)
  const pageSize = ref(DEFAULT_PAGE_SIZE)

  /**
   * 分页后的订单
   */
  const paginatedOrders = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    const result = sortedOrders.value.slice(start, end)
    console.log(
      `分页: 第${currentPage.value}页, 每页${pageSize.value}条, 范围[${start},${end}), 返回${result.length}条`
    )
    return result
  })

  /**
   * 总数
   */
  const total = computed(() => sortedOrders.value.length)

  /**
   * 处理页码变化
   * @param {number} page - 新页码
   */
  function handlePageChange(page) {
    currentPage.value = page
    // 滚动到顶部
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  /**
   * 处理每页数量变化
   * @param {number} size - 新的每页数量
   */
  function handleSizeChange(size) {
    pageSize.value = size
    currentPage.value = 1
    if (onReset) {
      onReset()
    }
  }

  /**
   * 重置到第一页
   */
  function resetToFirstPage() {
    currentPage.value = 1
  }

  /**
   * 监听排序变化，重置到第一页
   */
  watch(
    () => sortedOrders.value.length,
    () => {
      if (currentPage.value > Math.ceil(total.value / pageSize.value)) {
        resetToFirstPage()
      }
    }
  )

  return {
    // 状态
    currentPage,
    pageSize,
    pageSizeOptions: PAGE_SIZE_OPTIONS,

    // 计算属性
    paginatedOrders,
    total,

    // 方法
    handlePageChange,
    handleSizeChange,
    resetToFirstPage
  }
}
