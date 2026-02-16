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
        axios.get(`${API_CONFIG.baseURL}${API_CONFIG.merchant.detail}${order.merchantId}`).catch(() => ({ data: { data: { name: '' } } }))
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

      return {
        name: dish?.name || orderDish.dishName || '菜品',
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
      console.log('订单正在加载中，跳过重复请求')
      return
    }

    isLoadingRequest = true
    loading.value = true
    // 重置列表动画状态，确保列表可见
    listTransitionName.value = ''

    try {
      const userId = getUserId()
      console.log('正在加载订单，用户ID:', userId)
      const response = await axios.get(API_CONFIG.baseURL + API_CONFIG.order.list + userId)

      console.log('订单API响应:', response.data)

      const orderList = response.data?.data
      if (orderList && Array.isArray(orderList)) {
        // 并行获取所有订单的菜品信息
        const ordersWithItems = await Promise.all(orderList.map((order) => fetchOrderDishes(order)))
        orders.value = ordersWithItems
        console.log('订单加载成功，共', ordersWithItems.length, '条订单')
      } else {
        console.warn('订单数据格式不正确或为空:', orderList)
        // 只有在确实没有数据时才清空，保留旧数据以防 API 错误
        if (orderList === undefined || orderList === null) {
          console.error('API 返回数据结构异常，保留现有订单数据')
        } else {
          orders.value = []
        }
      }
    } catch (error) {
      console.error('加载订单失败:', error)
      ElMessage.error('加载订单失败，请稍后重试')
      // 加载失败时不清空现有数据，保留用户的订单显示
    } finally {
      isLoadingRequest = false
      loading.value = false
      // 确保列表动画状态被重置
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
    const order = orders.value.find((o) => o.id === orderId)
    if (order) {
      // 如果是数字，转换为前端状态文本
      const statusText = typeof newStatus === 'number' ? orderStatusToText(newStatus) : newStatus
      order.status = statusText
      console.log(`订单 ${orderId} 状态已更新为:`, statusText)
    } else {
      console.warn(`未找到订单 ${orderId}，无法更新状态`)
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
