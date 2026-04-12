/**
 * 订单数据获取组合式函数
 */

import pinia from '../store'
import { useAuthStore } from '../store/authStore'

const authStore = useAuthStore(pinia)

import { ref } from 'vue'
import axios from 'axios'
import { API_CONFIG } from '../config'
import { ElMessage } from 'element-plus'
import { orderStatusToText } from '../utils/orderStatus'
import { formatTime, calculateItemCount } from '../utils/formatters'

// 默认用户ID
const DEFAULT_USER_ID = '1'

/**
 * 订单数据管理
 */
export function useOrderData() {
  // 状态
  const orders = ref([])
  const loading = ref(false)
  const isRefreshing = ref(false)
  const refreshSuccess = ref(false)
  const listTransitionName = ref('')

  /**
   * 获取用户ID
   * @returns {number} 用户ID
   */
  function getUserId() {
    const userId = String(authStore.userId || 1) || DEFAULT_USER_ID
    return parseInt(userId, 10)
  }

  /**
   * 获取单个订单的菜品信息
   * @param {Object} order - 订单对象
   * @returns {Promise<Object>} 包含菜品信息的订单对象
   */
  async function fetchOrderDishes(order) {
    try {
      // 并行获取订单菜品和商家名称
      const [dishesResponse, merchantResponse] = await Promise.all([
        axios.get(`${API_CONFIG.baseURL}/v1/orders/${order.id}/dishes`),
        axios
          .get(`${API_CONFIG.baseURL}${API_CONFIG.merchant.detail}${order.merchantId}`)
          .catch(() => ({ data: { data: { name: '' } } }))
      ])

      let items = []
      if (dishesResponse.data?.data && dishesResponse.data.data.length > 0) {
        items = await Promise.all(
          dishesResponse.data.data.map(async (orderDish) => {
            return await fetchDishDetail(orderDish)
          })
        )
      }

      // 获取商家名称
      const merchantName = merchantResponse.data?.data?.name || ''

      return buildOrderObject(order, items, merchantName)
    } catch (error) {
      console.error(`加载订单${order.id}的菜品失败:`, error)
      return buildOrderObject(order, [], '')
    }
  }

  /**
   * 获取菜品详细信息
   * @param {Object} orderDish - 订单菜品关联对象
   * @returns {Promise<Object>} 菜品详细信息
   */
  async function fetchDishDetail(orderDish) {
    try {
      const dishResponse = await axios.get(
        `${API_CONFIG.baseURL}${API_CONFIG.dish.detail}${orderDish.dishId}`
      )
      const dish = dishResponse.data?.data

      // 过滤不可达的外部占位图URL，避免无意义网络请求
      const unreachableHosts = ['via.placeholder.com', 'placehold.co', 'placeholder.com']
      const rawImage = dish?.image || ''
      const isPlaceholder = unreachableHosts.some((host) => rawImage.includes(host))

      return {
        name: dish?.name || orderDish.dishName || '菜品',
        quantity: orderDish.quantity,
        price: orderDish.price,
        customization: orderDish.customization,
        image: isPlaceholder ? '' : rawImage,
        imageLoadError: isPlaceholder,
        optionalIngredients: dish?.optionalIngredients || [],
        requiredIngredients: dish?.requiredIngredients || [],
        dishNote: orderDish.note || ''
      }
    } catch (error) {
      console.error(`获取菜品${orderDish.dishId}详情失败:`, error)
      return {
        name: orderDish.dishName || '菜品',
        quantity: orderDish.quantity,
        price: orderDish.price,
        image: orderDish.dishImage || '',
        imageLoadError: true,
        optionalIngredients: [],
        requiredIngredients: [],
        dishNote: ''
      }
    }
  }

  /**
   * 构建订单对象
   * @param {Object} order - 原始订单对象
   * @param {Array} items - 菜品数组
   * @param {String} merchantName - 商家名称
   * @returns {Object} 格式化后的订单对象
   */
  function buildOrderObject(order, items, merchantName = '') {
    return {
      id: order.id,
      orderNo: order.id,
      status: orderStatusToText(order.status),
      merchant: merchantName,
      merchantId: order.merchantId,
      total: order.totalAmount,
      time: formatTime(order.createTime),
      items,
      itemCount: calculateItemCount(items),
      _raw: order
    }
  }

  // 用于防止并发请求的标志
  let isLoadingRequest = false

  /**
   * 加载订单列表
   * @returns {Promise<void>}
   */
  async function loadOrders() {
    // 防止并发请求
    if (isLoadingRequest) {
      return
    }

    isLoadingRequest = true
    loading.value = true
    listTransitionName.value = ''

    try {
      const userId = getUserId()
      if (!userId) {
        loading.value = false
        isLoadingRequest = false
        return
      }

      const response = await axios.get(API_CONFIG.baseURL + API_CONFIG.order.list + userId)

      const orderList = response.data?.data
      if (orderList && Array.isArray(orderList)) {
        const ordersWithItems = await Promise.all(orderList.map((order) => fetchOrderDishes(order)))
        orders.value = ordersWithItems
      } else if (orderList !== undefined && orderList !== null) {
        // API 返回了有效但为空的数据
        orders.value = []
      }
      // API 返回结构异常时保留现有订单数据
    } catch (error) {
      console.error('加载订单失败:', error)
      ElMessage.error('加载订单失败，请稍后重试')
    } finally {
      isLoadingRequest = false
      loading.value = false
      listTransitionName.value = ''
    }
  }

  /**
   * 处理刷新点击
   * @param {number} minDuration - 最小动画持续时间（毫秒）
   * @returns {Promise<void>}
   */
  async function handleRefresh(minDuration = 600) {
    // 启动刷新动画
    isRefreshing.value = true
    refreshSuccess.value = false
    listTransitionName.value = 'list-fade-out'

    const startTime = Date.now()

    try {
      await loadOrders()

      const duration = Date.now() - startTime
      const delay = Math.max(0, minDuration - duration)

      setTimeout(() => {
        isRefreshing.value = false
        refreshSuccess.value = true
        listTransitionName.value = 'list-fade-in'

        ElMessage.success({
          message: '刷新成功',
          duration: 2000,
          offset: 60
        })

        setTimeout(() => {
          refreshSuccess.value = false
          listTransitionName.value = ''
        }, 300)
      }, delay)
    } catch (error) {
      isRefreshing.value = false
      listTransitionName.value = ''
    }
  }

  /**
   * 处理图片加载错误
   * @param {Object} item - 菜品对象
   */
  function handleImageError(item) {
    item.imageLoadError = true
  }

  /**
   * 更新订单状态（用于WebSocket更新）
   * @param {number} orderId - 订单ID
   * @param {string|number} newStatus - 新状态（可能是后端状态码或前端状态文本）
   */
  function updateOrderStatus(orderId, newStatus) {
    const index = orders.value.findIndex((o) => o.id === orderId)
    if (index === -1) return

    const statusText = typeof newStatus === 'number' ? orderStatusToText(newStatus) : newStatus
    // 不可变更新：创建新数组替换整个 orders
    orders.value = orders.value.map((o, i) =>
      i === index ? { ...o, status: statusText } : o
    )
  }

  return {
    // 状态
    orders,
    loading,
    isRefreshing,
    refreshSuccess,
    listTransitionName,

    // 方法
    loadOrders,
    handleRefresh,
    handleImageError,
    updateOrderStatus,
    getUserId
  }
}
