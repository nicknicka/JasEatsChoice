/**
 * 订单筛选和排序组合式函数
 */
import { ref, computed } from 'vue'
import { STATUS_LIST, STATUS_PRIORITY } from '../utils/orderStatus'

/**
 * 排序选项配置
 */
const SORT_OPTIONS = [
  { value: 'timeDesc', label: '最新订单', icon: 'Clock' },
  { value: 'timeAsc', label: '最早订单', icon: 'Calendar' },
  { value: 'statusPriority', label: '待处理优先', icon: 'Timer' },
  { value: 'amountDesc', label: '金额最高', icon: 'Coin' },
  { value: 'amountAsc', label: '金额最低', icon: 'Wallet' }
]

/**
 * 订单筛选和排序管理
 * @param {Ref<Array>} orders - 订单数组
 */
export function useOrderFilter(orders) {
  // 筛选状态
  const activeStatus = ref('all')
  const searchKeyword = ref('')

  // 排序状态
  const sortBy = ref('timeDesc')

  /**
   * 搜索订单
   * @param {Array} ordersToSearch - 待搜索的订单数组
   * @param {string} keyword - 搜索关键词
   * @returns {Array} 过滤后的订单数组
   */
  function searchOrders(ordersToSearch, keyword) {
    if (!keyword || keyword.trim() === '') {
      return ordersToSearch
    }

    const searchTerm = keyword.toLowerCase().trim()

    return ordersToSearch.filter((order) => {
      // 搜索订单号
      if (order.orderNo && order.orderNo.toString().toLowerCase().includes(searchTerm)) {
        return true
      }

      // 搜索商家名称
      if (order.merchant && order.merchant.toLowerCase().includes(searchTerm)) {
        return true
      }

      // 搜索菜品名称
      if (order.items && order.items.length > 0) {
        return order.items.some((item) => item.name && item.name.toLowerCase().includes(searchTerm))
      }

      // 搜索总金额
      if (order.total && order.total.toString().includes(searchTerm)) {
        return true
      }

      return false
    })
  }

  /**
   * 排序订单
   * @param {Array} ordersToSort - 待排序的订单数组
   * @returns {Array} 排序后的订单数组
   */
  function sortOrders(ordersToSort) {
    const sortedOrders = [...ordersToSort]

    switch (sortBy.value) {
      case 'timeDesc': // 最新订单（时间倒序）
        return sortedOrders.sort((a, b) => {
          const timeA = new Date(a._raw?.createTime || a.time).getTime()
          const timeB = new Date(b._raw?.createTime || b.time).getTime()
          return timeB - timeA
        })

      case 'timeAsc': // 最早订单（时间正序）
        return sortedOrders.sort((a, b) => {
          const timeA = new Date(a._raw?.createTime || a.time).getTime()
          const timeB = new Date(b._raw?.createTime || b.time).getTime()
          return timeA - timeB
        })

      case 'statusPriority': // 待处理优先
        return sortedOrders.sort((a, b) => {
          const priorityA = STATUS_PRIORITY[a.status] || 999
          const priorityB = STATUS_PRIORITY[b.status] || 999
          // 如果优先级相同，按时间倒序
          if (priorityA !== priorityB) {
            return priorityA - priorityB
          }
          const timeA = new Date(a._raw?.createTime || a.time).getTime()
          const timeB = new Date(b._raw?.createTime || b.time).getTime()
          return timeB - timeA
        })

      case 'amountDesc': // 金额最高
        return sortedOrders.sort((a, b) => b.total - a.total)

      case 'amountAsc': // 金额最低
        return sortedOrders.sort((a, b) => a.total - b.total)

      default:
        return sortedOrders
    }
  }

  /**
   * 筛选和排序后的订单
   */
  const filteredOrders = computed(() => {
    let result = orders.value

    // 调试日志
    console.log('useOrderFilter - 原始订单数量:', orders.value.length)
    console.log('useOrderFilter - 当前筛选状态:', activeStatus.value)

    // 先应用搜索
    if (searchKeyword.value && searchKeyword.value.trim() !== '') {
      result = searchOrders(result, searchKeyword.value)
      console.log('useOrderFilter - 搜索后数量:', result.length)
    }

    // 再应用状态筛选
    if (activeStatus.value !== 'all') {
      const beforeFilter = result.length
      result = result.filter((order) => {
        const match = order.status === activeStatus.value
        if (!match && orders.value.length > 0) {
          console.log(
            `订单 ${order.id} 状态不匹配: order.status=${order.status}, activeStatus=${activeStatus.value}`
          )
        }
        return match
      })
      console.log(`useOrderFilter - 状态筛选: ${beforeFilter} -> ${result.length}`)
    }

    console.log('useOrderFilter - 最终筛选结果数量:', result.length)
    return result
  })

  /**
   * 排序后的订单
   */
  const sortedOrders = computed(() => sortOrders(filteredOrders.value))

  /**
   * 获取当前排序选项
   */
  const currentSortOption = computed(
    () => SORT_OPTIONS.find((option) => option.value === sortBy.value) || SORT_OPTIONS[0]
  )

  /**
   * 处理排序变化
   * @param {string} value - 排序选项值
   */
  function handleSortChange(value) {
    sortBy.value = value
  }

  /**
   * 清除搜索
   */
  function clearSearch() {
    searchKeyword.value = ''
  }

  /**
   * 设置状态筛选
   * @param {string} status - 状态值
   */
  function setStatusFilter(status) {
    activeStatus.value = status
  }

  return {
    // 状态
    activeStatus,
    searchKeyword,
    sortBy,
    statusList: STATUS_LIST,
    sortOptions: SORT_OPTIONS,

    // 计算属性
    filteredOrders,
    sortedOrders,
    currentSortOption,

    // 方法
    handleSortChange,
    clearSearch,
    setStatusFilter
  }
}
