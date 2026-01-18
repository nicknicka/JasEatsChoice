/**
 * 订单数据获取组合式函数
 */
import { ref } from 'vue'
import axios from 'axios'
import { API_CONFIG } from '../config'
import { ElMessage } from 'element-plus'
import { orderStatusToText } from '../utils/orderStatus'
import { formatTime, calculateItemCount } from '../utils/formatters'

// 显示常量
const MAX_DISPLAY_ITEMS = 3
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
    const userId = localStorage.getItem('userId') || DEFAULT_USER_ID
    return parseInt(userId, 10)
  }

  /**
   * 获取单个订单的菜品信息
   * @param {Object} order - 订单对象
   * @returns {Promise<Object>} 包含菜品信息的订单对象
   */
  async function fetchOrderDishes(order) {
    try {
      const dishesResponse = await axios.get(`${API_CONFIG.baseURL}/v1/orders/${order.id}/dishes`)

      let items = []
      if (dishesResponse.data?.data && dishesResponse.data.data.length > 0) {
        items = await Promise.all(
          dishesResponse.data.data.map(async (orderDish) => {
            return await fetchDishDetail(orderDish)
          })
        )
      }

      return buildOrderObject(order, items)
    } catch (error) {
      console.error(`加载订单${order.id}的菜品失败:`, error)
      return buildOrderObject(order, [])
    }
  }

  /**
   * 获取菜品详细信息
   * @param {Object} orderDish - 订单菜品关联对象
   * @returns {Promise<Object>} 菜品详细信息
   */
  async function fetchDishDetail(orderDish) {
    try {
      const dishResponse = await axios.get(`${API_CONFIG.baseURL}/dishes/${orderDish.dishId}`)
      const dish = dishResponse.data?.data

      return {
        name: dish?.name || `菜品${orderDish.dishId}`,
        quantity: orderDish.quantity,
        price: orderDish.price,
        customization: orderDish.customization,
        image: dish?.image || '',
        imageLoadError: false, // 用于控制图片加载失败时的显示
        optionalIngredients: dish?.optionalIngredients || [],
        requiredIngredients: dish?.requiredIngredients || [],
        dishNote: orderDish.note || ''
      }
    } catch (error) {
      console.error(`获取菜品${orderDish.dishId}详情失败:`, error)
      return {
        name: `菜品${orderDish.dishId}`,
        quantity: orderDish.quantity,
        price: orderDish.price,
        image: '',
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
   * @returns {Object} 格式化后的订单对象
   */
  function buildOrderObject(order, items) {
    return {
      id: order.id,
      orderNo: order.id,
      status: orderStatusToText(order.status),
      merchant: `商家${order.merchantId}`,
      merchantId: order.merchantId,
      total: order.totalAmount,
      time: formatTime(order.createTime),
      items,
      itemCount: calculateItemCount(items),
      _raw: order
    }
  }

  /**
   * 加载订单列表
   * @returns {Promise<void>}
   */
  async function loadOrders() {
    loading.value = true

    try {
      const userId = getUserId()
      const response = await axios.get(API_CONFIG.baseURL + API_CONFIG.order.list + userId)

      if (response.data.data) {
        // 并行获取所有订单的菜品信息
        const ordersWithItems = await Promise.all(
          response.data.data.map((order) => fetchOrderDishes(order))
        )
        orders.value = ordersWithItems
      }
    } catch (error) {
      console.error('加载订单失败:', error)
      ElMessage.error('加载订单失败，请稍后重试')
      orders.value = []
    } finally {
      loading.value = false
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
   * @param {string} newStatus - 新状态
   */
  function updateOrderStatus(orderId, newStatus) {
    const order = orders.value.find((o) => o.id === orderId)
    if (order) {
      order.status = newStatus
    }
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
