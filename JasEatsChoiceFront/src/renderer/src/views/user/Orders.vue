<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { API_CONFIG, WS_CONFIG } from '../../config'
import { ElMessage, ElMessageBox } from 'element-plus'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import { Refresh, ArrowDown, EditPen, Check, Search, Delete, Clock, Calendar, Timer, Coin, Wallet } from '@element-plus/icons-vue'
import orderApi from '../../api/order'

const router = useRouter()
const route = useRoute()

// 用户订单数据
const orders = ref([])

// 加载状态
const loading = ref(false)
// 刷新动画状态
const isRefreshing = ref(false)
// 刷新成功状态
const refreshSuccess = ref(false)
// 展开的订单ID集合
const expandedOrderIds = ref(new Set())
// 列表过渡动画状态
const listTransitionName = ref('list-fade')

// 切换订单展开状态
const toggleOrderExpand = (orderId) => {
  if (expandedOrderIds.value.has(orderId)) {
    expandedOrderIds.value.delete(orderId)
  } else {
    expandedOrderIds.value.add(orderId)
  }
  // 触发响应式更新
  expandedOrderIds.value = new Set(expandedOrderIds.value)
}

// 判断订单是否展开
const isOrderExpanded = (orderId) => {
  return expandedOrderIds.value.has(orderId)
}

// 获取订单显示的商品列表（超过3个商品时，收起状态只显示前3个）
const getDisplayItems = (order) => {
  const maxDisplay = 3
  if (isOrderExpanded(order.id) || order.items.length <= maxDisplay) {
    return order.items
  }
  return order.items.slice(0, maxDisplay)
}

// 判断是否有更多商品
const hasMoreItems = (order) => {
  return order.items.length > 3
}

// 处理刷新点击
const handleRefresh = () => {
  // 启动刷新动画
  isRefreshing.value = true
  refreshSuccess.value = false
  listTransitionName.value = 'list-fade-out'

  // 确保动画至少持续一点时间
  const startTime = Date.now()

  // 调用加载订单函数
  loadOrders().finally(() => {
    const duration = Date.now() - startTime
    // 如果加载太快，延迟关闭动画以提供更好的视觉反馈
    const delay = Math.max(0, 600 - duration)

    setTimeout(() => {
      isRefreshing.value = false
      refreshSuccess.value = true
      listTransitionName.value = 'list-fade-in'

      // 显示成功提示
      ElMessage.success({
        message: '刷新成功',
        duration: 2000,
        offset: 60
      })

      // 重置成功状态
      setTimeout(() => {
        refreshSuccess.value = false
        listTransitionName.value = ''
      }, 300)
    }, delay)
  })
}

// 加载用户订单数据
const loadOrders = async () => {
  loading.value = true

  try {
    const userId = parseInt(localStorage.getItem('userId') || '1', 10)

    const response = await axios.get(API_CONFIG.baseURL + API_CONFIG.order.list + userId)

    if (response.data.data) {
      // 转换后端Order实体到前端期望的格式，并加载菜品信息
      const ordersWithItems = await Promise.all(
        response.data.data.map(async (order) => {
          try {
            // 获取订单菜品信息
            const dishesResponse = await axios.get(
              `${API_CONFIG.baseURL}/v1/orders/${order.id}/dishes`
            )

            // 处理菜品列表，获取菜品详细信息
            let items = []
            if (dishesResponse.data?.data && dishesResponse.data.data.length > 0) {
              items = await Promise.all(
                dishesResponse.data.data.map(async (orderDish) => {
                  try {
                    // 获取菜品详情
                    const dishResponse = await axios.get(
                      `${API_CONFIG.baseURL}/dishes/${orderDish.dishId}`
                    )
                    const dish = dishResponse.data?.data
                    return {
                      name: dish?.name || `菜品${orderDish.dishId}`,
                      quantity: orderDish.quantity,
                      price: orderDish.price,
                      customization: orderDish.customization,
                      image: dish?.image || '',
                      // 添加可选食材和备注信息
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
                      optionalIngredients: [],
                      requiredIngredients: [],
                      dishNote: ''
                    }
                  }
                })
              )
            }

            return {
              id: order.id,
              orderNo: order.id,
              status: orderStatusToText(order.status),
              merchant: `商家${order.merchantId}`,
              total: order.totalAmount,
              time: formatTime(order.createTime),
              items: items,
              itemCount: items.reduce((sum, item) => sum + item.quantity, 0),
              _raw: order
            }
          } catch (error) {
            console.error(`加载订单${order.id}的菜品失败:`, error)
            return {
              id: order.id,
              orderNo: order.id,
              status: orderStatusToText(order.status),
              merchant: `商家${order.merchantId}`,
              total: order.totalAmount,
              time: formatTime(order.createTime),
              items: [],
              itemCount: 0,
              _raw: order
            }
          }
        })
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

// 订单状态筛选
const activeStatus = ref('all')

// 明确按钮顺序的状态列表
const statusList = ref(['all', 'pendingAccept', 'processing', 'pending', 'pendingComment', 'delivered', 'completed', 'cancelled'])

// 排序选项
const sortBy = ref('timeDesc') // 默认按时间倒序

// 搜索关键词
const searchKeyword = ref('')

// 排序选项列表
const sortOptions = [
  { value: 'timeDesc', label: '最新订单', icon: Clock, iconName: 'Clock' },
  { value: 'timeAsc', label: '最早订单', icon: Calendar, iconName: 'Calendar' },
  { value: 'statusPriority', label: '待处理优先', icon: Timer, iconName: 'Timer' },
  { value: 'amountDesc', label: '金额最高', icon: Coin, iconName: 'Coin' },
  { value: 'amountAsc', label: '金额最低', icon: Wallet, iconName: 'Wallet' }
]

// 订单状态优先级（数值越小优先级越高）
const statusPriority = {
  pending: 1,          // 待支付 - 最高优先级
  pendingAccept: 2,    // 待接单
  processing: 3,       // 进行中
  delivered: 4,        // 已送达
  completed: 5,        // 已完成
  cancelled: 6         // 已取消 - 最低优先级
}

// 订单状态映射
const orderStatusMap = {
  all: '全部订单',
  pendingAccept: '待接单',
  processing: '进行中',
  pending: '待确认',
  pendingComment: '待评价',
  delivered: '已送达',
  completed: '已完成',
  cancelled: '已取消'
}

// 订单状态标签样式映射
const statusTagTypeMap = {
  pendingAccept: 'warning',
  processing: 'primary',
  pending: 'info',
  pendingComment: 'info',
  delivered: 'success',
  completed: 'success',  // 已完成订单使用绿色更合理
  cancelled: 'danger'
}

// 将后端状态码转换为前端状态文本
const orderStatusToText = (statusCode) => {
  const statusMap = {
    0: 'pending',        // 待支付
    1: 'pendingAccept',  // 待接单
    2: 'processing',     // 备菜中
    3: 'processing',     // 烹饪中
    4: 'processing',     // 待上菜
    5: 'delivered',      // 已送达
    6: 'cancelled',      // 已取消
    7: 'completed'       // 已完成
  }
  return statusMap[statusCode] || 'pending'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// WebSocket实例
const ws = ref(null)

// 组件挂载时加载数据和初始化WebSocket
onMounted(() => {
  // 检查是否有传递的状态参数
  if (route.query.status) {
    activeStatus.value = route.query.status
  }
  loadOrders()

  // 初始化WebSocket连接
  initWebSocket()
})

// 组件卸载时关闭WebSocket连接
onUnmounted(() => {
  if (ws.value) {
    ws.value.close()
  }
})

// 初始化WebSocket
const initWebSocket = () => {
  try {
    ws.value = new WebSocket(WS_CONFIG.url)

    // 连接成功
    ws.value.onopen = () => {
      console.log('WebSocket连接成功')

      // 可以在这里发送用户ID等信息到服务器，以便服务器推送相关订单更新
      // ws.value.send(JSON.stringify({ userId: localStorage.getItem('userId') }))
    }

    // 接收消息
    ws.value.onmessage = (event) => {
      try {
        const orderUpdate = JSON.parse(event.data)

        // 更新本地订单状态
        const index = orders.value.findIndex(order => order.id === orderUpdate.id)
        if (index !== -1) {
          orders.value[index].status = orderUpdate.status

          // 显示更新提示
          const statusText = orderStatusMap[orderUpdate.status] || orderUpdate.status
          ElMessage.info(`订单 ${orders.value[index].orderNo} 状态已更新为: ${statusText}`)
        }
      } catch (error) {
        console.error('解析WebSocket消息失败:', error)
      }
    }

    // 连接关闭
    ws.value.onclose = () => {
      console.log('WebSocket连接关闭')

      // 可以在这里实现重连逻辑
      // setTimeout(() => initWebSocket(), 3000)
    }

    // 连接错误
    ws.value.onerror = (error) => {
      console.error('WebSocket连接错误:', error)
    }
  } catch (error) {
    console.error('初始化WebSocket失败:', error)
    ElMessage.error('WebSocket连接失败，无法接收实时订单更新')
  }
}

// 监听路由参数变化
watch(
  () => route.query.status,
  (newStatus) => {
    if (newStatus) {
      activeStatus.value = newStatus
    }
  }
)

// 筛选后的订单（整合搜索功能）
const filteredOrders = computed(() => {
  let result = orders.value

  // 先应用搜索
  if (searchKeyword.value && searchKeyword.value.trim() !== '') {
    result = searchOrders(result, searchKeyword.value)
  }

  // 再应用状态筛选
  if (activeStatus.value !== 'all') {
    result = result.filter((order) => order.status === activeStatus.value)
  }

  return result
})

// 排序函数
const sortOrders = (ordersToSort) => {
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
        const priorityA = statusPriority[a.status] || 999
        const priorityB = statusPriority[b.status] || 999
        // 如果优先级相同，按时间倒序
        if (priorityA !== priorityB) {
          return priorityA - priorityB
        }
        const timeA = new Date(a._raw?.createTime || a.time).getTime()
        const timeB = new Date(b._raw?.createTime || b.time).getTime()
        return timeB - timeA
      })

    case 'amountDesc': // 金额最高
      return sortedOrders.sort((a, b) => {
        return b.total - a.total
      })

    case 'amountAsc': // 金额最低
      return sortedOrders.sort((a, b) => {
        return a.total - b.total
      })

    default:
      return sortedOrders
  }
}

// 排序后的订单
const sortedOrders = computed(() => {
  return sortOrders(filteredOrders.value)
})

// 分页相关
const currentPage = ref(1)
const pageSize = ref(5)

// 监听排序变化，重置到第一页
watch(sortBy, () => {
  currentPage.value = 1
})

// 分页后的订单
const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return sortedOrders.value.slice(start, end)
})

// 查看订单详情
const viewOrderDetails = (order) => {
  // 导航到订单详情页
  router.push({
    path: `/user/home/order-detail/${order.id}`,
    name: 'user-order-detail',
    params: { id: order.id }
  })
}

// 取消订单
const cancelOrder = (order) => {
  // 调用后端API取消订单
  axios.put(API_CONFIG.baseURL + API_CONFIG.order.detail + order.id + '/cancel')
    .then((response) => {
      if (response.data.success) {
        order.status = 'cancelled'
        ElMessage.success('订单已取消')
      } else {
        ElMessage.error(response.data.message || '取消订单失败')
      }
    })
    .catch((error) => {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败，请稍后重试')
    })
}

// 确认收货
const confirmReceipt = async (order) => {
  try {
    await ElMessageBox.confirm(
      '确认已收到餐品并完成订单吗？',
      '确认收货',
      {
        confirmButtonText: '确认收货',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    // 调用确认收货API
    const response = await orderApi.confirmReceipt(order.id)

    if (response.data.success) {
      // 更新本地订单状态为已完成
      order.status = 'completed'
      ElMessage.success('已确认收货，订单完成')
    } else {
      ElMessage.error(response.data.message || '确认收货失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货失败:', error)
      ElMessage.error('确认收货失败，请稍后重试')
    }
  }
}

// 跳转到评价页面
const goToEvaluate = (order) => {
  // 导航到评价页面
  router.push({
    path: `/user/home/evaluate-order/${order.id}`,
    name: 'user-evaluate-order',
    params: { id: order.id }
  })
}

// 处理图片加载错误
const handleImageError = (event) => {
  const img = event.target
  img.style.display = 'none'
  const parent = img.parentElement
  if (parent && !parent.querySelector('.no-image')) {
    const noImageDiv = document.createElement('div')
    noImageDiv.className = 'no-image'
    noImageDiv.innerHTML = '<span>菜</span>'
    parent.appendChild(noImageDiv)
  }
}

// 获取当前排序选项
const getCurrentSortOption = () => {
  return sortOptions.find(option => option.value === sortBy.value) || sortOptions[0]
}

// 处理排序变化
const handleSortChange = (value) => {
  sortBy.value = value
  ElMessage.success(`已切换排序：${getCurrentSortOption().label}`)
}

// 搜索订单
const searchOrders = (ordersToSearch, keyword) => {
  if (!keyword || keyword.trim() === '') {
    return ordersToSearch
  }

  const searchTerm = keyword.toLowerCase().trim()

  return ordersToSearch.filter(order => {
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
      return order.items.some(item =>
        item.name && item.name.toLowerCase().includes(searchTerm)
      )
    }

    // 搜索总金额
    if (order.total && order.total.toString().includes(searchTerm)) {
      return true
    }

    return false
  })
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
  ElMessage.info('已清除搜索')
}

// 监听搜索关键词变化，重置到第一页
watch(searchKeyword, () => {
  currentPage.value = 1
})
</script>

<template>
  <div class="orders-container">
    <div class="page-header">
      <CommonBackButton />
      <h2 style="margin-left: 15px">订单中心</h2>
      <div style="flex: 1; text-align: right">
        <el-button
          type="default"
          size="small"
          @click="handleRefresh"
          :loading="loading"
          :class="{ 'refresh-btn': true, 'is-refreshing': isRefreshing, 'is-success': refreshSuccess }"
        >
          <el-icon :class="{ 'refresh-rotating': isRefreshing, 'refresh-success': refreshSuccess }">
            <Refresh />
          </el-icon>
          <span class="refresh-text">{{ refreshSuccess ? '完成' : '刷新' }}</span>
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索订单号、商家名称、菜品名称、金额..."
        clearable
        size="default"
        class="search-input"
      >
        <template #prefix>
          <el-icon class="search-icon">
            <Search />
          </el-icon>
        </template>
        <template #suffix>
          <el-icon
            v-if="searchKeyword"
            class="clear-icon"
            @click="clearSearch"
          >
            <Delete />
          </el-icon>
        </template>
      </el-input>
      <div v-if="searchKeyword" class="search-result-info">
        找到 <span class="result-count">{{ filteredOrders.length }}</span> 个相关订单
        <el-button
          type="primary"
          link
          size="small"
          @click="clearSearch"
          class="clear-search-btn"
        >
          清除搜索
        </el-button>
      </div>
    </div>

    <!-- 订单筛选 -->
    <div class="order-filters">
      <div class="filter-buttons">
        <el-button
          v-for="status in statusList"
          :key="status"
          type="primary"
          :plain="activeStatus !== status"
          @click="activeStatus = status"
          size="small"
        >
          {{ orderStatusMap[status] }}
        </el-button>
      </div>

      <!-- 排序选择器 -->
      <div class="sort-selector">
        <el-dropdown trigger="click" @command="handleSortChange">
          <el-button type="default" size="small" class="sort-dropdown-btn">
            <el-icon class="sort-icon">
              <component :is="getCurrentSortOption().icon" />
            </el-icon>
            <span class="sort-label">{{ getCurrentSortOption().label }}</span>
            <el-icon class="el-icon--right">
              <ArrowDown />
            </el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                v-for="option in sortOptions"
                :key="option.value"
                :command="option.value"
                :class="{ 'is-active': sortBy === option.value }"
              >
                <el-icon class="sort-option-icon">
                  <component :is="option.icon" />
                </el-icon>
                <span class="sort-option-label">{{ option.label }}</span>
                <el-icon v-if="sortBy === option.value" class="check-icon">
                  <Check />
                </el-icon>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="order-list" v-loading="loading" element-loading-text="加载中..." :class="listTransitionName">
      <el-card v-for="order in paginatedOrders" :key="order.id" class="order-card">
        <div class="order-header">
          <div class="order-info">
            <div class="order-no">订单号: {{ order.orderNo }}</div>
            <div class="order-merchant">商家: {{ order.merchant }}</div>
            <div class="order-time">时间: {{ order.time }}</div>
          </div>
          <div class="order-status">
            <el-tag :type="statusTagTypeMap[order.status]">
              {{ orderStatusMap[order.status] }}
            </el-tag>
          </div>
        </div>

        <!-- 菜品列表 -->
        <div class="order-items">
          <div class="items-header">
            <div class="header-left">
              <span class="item-count">共 {{ order.itemCount || 0 }} 件商品</span>
              <el-tag v-if="order.itemCount > 0" type="info" size="small" class="item-tag">
                {{ order.itemCount }} 种菜品
              </el-tag>
            </div>
            <el-button
              v-if="hasMoreItems(order)"
              type="primary"
              link
              size="small"
              @click="toggleOrderExpand(order.id)"
              class="expand-btn"
            >
              {{ isOrderExpanded(order.id) ? '收起' : `展开全部 (${order.itemCount}件)` }}
              <el-icon :class="{ 'expand-icon': true, 'expanded': isOrderExpanded(order.id) }">
                <ArrowDown />
              </el-icon>
            </el-button>
          </div>

          <div class="items-list">
            <div
              v-for="(item, index) in getDisplayItems(order)"
              :key="index"
              class="item-row"
              @click="viewOrderDetails(order)"
            >
              <!-- 商品图片 -->
              <div class="item-image">
                <img
                  v-if="item.image"
                  :src="item.image"
                  :alt="item.name"
                  loading="lazy"
                  @error="handleImageError($event)"
                />
                <div v-else class="no-image">
                  <span>{{ item.name?.charAt(0) || '菜' }}</span>
                </div>
                <!-- 数量徽章 -->
                <div class="quantity-badge" :class="{ 'large-number': item.quantity > 9 }">
                  {{ item.quantity }}
                </div>
              </div>

              <!-- 商品信息 -->
              <div class="item-info">
                <div class="item-name">{{ item.name }}</div>

                <!-- 必选食材 -->
                <div v-if="item.requiredIngredients && item.requiredIngredients.length > 0" class="item-ingredients">
                  <div class="ingredients-label">
                    <span class="label-text">必选:</span>
                  </div>
                  <div class="ingredients-list">
                    <span class="ingredient-tag required" v-for="ing in item.requiredIngredients" :key="ing">
                      {{ ing }}
                    </span>
                  </div>
                </div>

                <!-- 可选食材 -->
                <div v-if="item.optionalIngredients && item.optionalIngredients.length > 0" class="item-ingredients">
                  <div class="ingredients-label">
                    <span class="label-text">可选:</span>
                  </div>
                  <div class="ingredients-list">
                    <span
                      class="ingredient-tag optional"
                      v-for="ing in item.optionalIngredients"
                      :key="ing.id || ing.name"
                    >
                      {{ ing.name }}
                      <span v-if="ing.price" class="ingredient-price">+¥{{ ing.price.toFixed(2) }}</span>
                    </span>
                  </div>
                </div>

                <!-- 备注信息 -->
                <div v-if="item.dishNote" class="item-note">
                  <el-icon><EditPen /></el-icon>
                  <span class="note-text">{{ item.dishNote }}</span>
                </div>

                <!-- 自定义信息（兼容旧数据） -->
                <div v-if="item.customization && !item.dishNote" class="item-customization">
                  <el-icon><EditPen /></el-icon>
                  <el-tooltip
                    :content="item.customization"
                    placement="top"
                    :show-after="500"
                  >
                    <span class="customization-text">{{ item.customization }}</span>
                  </el-tooltip>
                </div>

                <div class="item-price-detail">
                  <span class="unit-price">¥{{ item.price.toFixed(2) }} /份</span>
                  <span class="total-price">小计 ¥{{ (item.price * item.quantity).toFixed(2) }}</span>
                </div>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-if="!order.items || order.items.length === 0" class="items-empty">
              <el-empty description="暂无商品信息" :image-size="60" />
            </div>
          </div>
        </div>

        <div class="order-total">
          <div class="total-text">总金额:</div>
          <div class="total-amount">¥{{ order.total.toFixed(2) }}</div>
        </div>

        <div class="order-actions">
          <el-button type="primary" size="small" @click="viewOrderDetails(order)">
            查看详情
          </el-button>
          <el-button
            v-if="order.status === 'pendingAccept'"
            type="danger"
            size="small"
            @click="cancelOrder(order)"
          >
            取消订单
          </el-button>
          <el-button
            v-if="order.status === 'delivered'"
            type="success"
            size="small"
            @click="confirmReceipt(order)"
          >
            确认收货
          </el-button>
          <el-button
            v-if="order.status === 'pendingComment'"
            type="success"
            size="small"
            @click="goToEvaluate(order)"
          >
            去评价
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 分页组件 -->
    <el-pagination
      v-if="sortedOrders.length > 0"
      background
      layout="total, sizes, prev, pager, next, jumper"
      :total="sortedOrders.length"
      :current-page="currentPage"
      :page-size="pageSize"
      @current-change="(page) => currentPage = page"
      @size-change="(size) => { pageSize = size; currentPage = 1; }"
      class="order-pagination"
    />

    <!-- 空数据提示 -->
    <el-empty v-if="filteredOrders.length === 0" description="暂无订单记录，快去下单吧！"></el-empty>
  </div>
</template>

<style scoped lang="less">
.orders-container {
  padding: 0 20px 20px 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 80px);

  .page-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    padding: 16px 20px;
    background: #ffffff;
    border-radius: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    border: 1px solid rgba(0, 0, 0, 0.06);
  }

  h2 {
    font-size: 24px;
    margin: 0;
    color: #2c5282;
    font-weight: 600;
  }

  .search-bar {
    margin-bottom: 16px;
    padding: 16px 20px;
    background: #ffffff;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    border: 1px solid rgba(0, 0, 0, 0.06);

    .search-input {
      :deep(.el-input__wrapper) {
        border-radius: 24px;
        padding: 8px 16px;
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
        border: 1px solid rgba(179, 212, 252, 0.3);
        background: linear-gradient(135deg, #f8faff 0%, #ffffff 100%);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

        &:hover {
          box-shadow: 0 4px 12px rgba(92, 142, 255, 0.15);
          border-color: rgba(92, 142, 255, 0.4);
        }

        &.is-focus {
          box-shadow: 0 4px 16px rgba(92, 142, 255, 0.25);
          border-color: #6ba4ff;
          background: #ffffff;
        }
      }

      :deep(.el-input__inner) {
        font-size: 14px;
        color: #2c5282;
        font-weight: 400;

        &::placeholder {
          color: #94a3b8;
          font-weight: 300;
        }
      }

      .search-icon {
        color: #5c8eff;
        font-size: 16px;
        animation: search-glow 2s ease-in-out infinite;
      }

      @keyframes search-glow {
        0%, 100% {
          opacity: 1;
          transform: scale(1);
        }
        50% {
          opacity: 0.7;
          transform: scale(1.1);
        }
      }

      .clear-icon {
        color: #94a3b8;
        font-size: 16px;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          color: #ff6b6b;
          transform: rotate(90deg) scale(1.1);
        }
      }
    }

    .search-result-info {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-top: 12px;
      padding: 8px 12px;
      background: linear-gradient(135deg, #e7f5ff 0%, #f0f9ff 100%);
      border-radius: 8px;
      font-size: 13px;
      color: #1971c2;
      border: 1px solid rgba(92, 142, 255, 0.2);

      .result-count {
        font-weight: 700;
        font-size: 16px;
        color: #5c8eff;
        padding: 0 4px;
      }

      .clear-search-btn {
        margin-left: auto;
        font-size: 13px;
        font-weight: 500;
        color: #5c8eff;
        transition: all 0.3s ease;

        &:hover {
          color: #4c7eff;
          transform: translateX(2px);
        }
      }
    }
  }

  .order-filters {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 10px;
    margin-bottom: 20px;
    padding: 12px 16px;
    background: #ffffff;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    border: 1px solid rgba(0, 0, 0, 0.06);
    flex-wrap: wrap;

    .filter-buttons {
      display: flex;
      gap: 10px;
      flex-wrap: wrap;
      flex: 1;
    }

    :deep(.el-button) {
      border-radius: 20px;
      border-color: rgba(179, 212, 252, 0.4);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

      &.el-button--primary {
        background: linear-gradient(135deg, #6ba4ff 0%, #5c8eff 100%);
        border-color: transparent;
        box-shadow: 0 2px 8px rgba(92, 142, 255, 0.3);

        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(92, 142, 255, 0.4);
        }
      }

      &.is-plain {
        background: #ffffff;
        color: #5c8eff;
        border-color: #d9d9d9;

        &:hover {
          background: #f0f9ff;
          border-color: #6ba4ff;
          color: #4c7eff;
        }
      }
    }

    // 排序选择器样式
    .sort-selector {
      flex-shrink: 0;

      .sort-dropdown-btn {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 8px 16px;
        border-radius: 20px;
        background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
        border: 1px solid #dee2e6;
        color: #495057;
        font-weight: 500;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);

        &:hover {
          background: linear-gradient(135deg, #e9ecef 0%, #dee2e6 100%);
          border-color: #adb5bd;
          transform: translateY(-1px);
          box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
        }

        .sort-icon {
          font-size: 16px;
          color: #5c8eff;
        }

        .sort-label {
          font-size: 13px;
        }

        .el-icon {
          font-size: 12px;
          transition: transform 0.3s ease;
        }
      }

      :deep(.el-dropdown-menu__item) {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 10px 16px;
        border-radius: 8px;
        margin: 4px 8px;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

        &.is-active {
          background: linear-gradient(135deg, #e7f5ff 0%, #d0ebff 100%);
          color: #1971c2;
          font-weight: 600;

          .sort-option-icon {
            transform: scale(1.1);
          }
        }

        &:hover {
          background: #f8f9fa;
          transform: translateX(2px);
        }

        .sort-option-icon {
          font-size: 16px;
          color: #5c8eff;
          transition: transform 0.3s ease;
        }

        .sort-option-label {
          flex: 1;
          font-size: 13px;
        }

        .check-icon {
          color: #1971c2;
          font-size: 14px;
          font-weight: bold;
        }
      }
    }
  }

  .order-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .order-pagination {
    margin-top: 24px;
    text-align: center;

    :deep(.el-pagination) {
      .btn-prev,
      .btn-next,
      .el-pager li {
        border-radius: 8px;
        border: 1px solid rgba(179, 212, 252, 0.3);
        background: rgba(255, 255, 255, 0.8);

        &:hover {
          color: #5c8eff;
          border-color: #6ba4ff;
          background: rgba(235, 244, 255, 0.6);
        }

        &.active {
          background: linear-gradient(135deg, #6ba4ff 0%, #5c8eff 100%);
          color: white;
          border-color: transparent;
        }
      }
    }
  }

  .order-card {
    background: #ffffff;
    border-radius: 16px;
    border: 1px solid rgba(0, 0, 0, 0.08);
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    overflow: hidden;
    position: relative;

    // 添加悬浮时的上移效果
    &:hover {
      transform: translateY(-4px) scale(1.01);
      box-shadow: 0 12px 32px rgba(92, 142, 255, 0.18);
      border-color: rgba(92, 142, 255, 0.4);

      // 为卡片添加微妙的渐变边框效果
      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        border-radius: 16px;
        padding: 2px;
        background: linear-gradient(135deg,
          rgba(92, 142, 255, 0.2) 0%,
          rgba(138, 180, 248, 0.15) 50%,
          rgba(92, 142, 255, 0.2) 100%);
        -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
        -webkit-mask-composite: xor;
        mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
        mask-composite: exclude;
        pointer-events: none;
      }
    }

    // 点击时的反馈
    &:active {
      transform: translateY(-2px) scale(1.005);
      box-shadow: 0 8px 24px rgba(92, 142, 255, 0.15);
    }

    :deep(.el-card__body) {
      padding: 20px;
    }

    .order-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 16px;
      padding-bottom: 16px;
      border-bottom: 1px solid rgba(0, 0, 0, 0.06);

      .order-info {
        .order-no {
          font-weight: 600;
          margin-bottom: 6px;
          color: #2c5282;
          font-size: 15px;
        }
        .order-merchant,
        .order-time {
          font-size: 13px;
          color: #64748b;
          margin-bottom: 4px;
        }
      }

      .order-status {
        :deep(.el-tag) {
          border-radius: 20px;
          padding: 6px 14px;
          font-weight: 500;
          border: none;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        }
      }
    }

    .order-items {
      margin-bottom: 16px;
      background: #fafbfc;
      border-radius: 12px;
      padding: 14px;
      border: 1px solid rgba(0, 0, 0, 0.06);

      .items-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;
        padding-bottom: 10px;
        border-bottom: 1px solid rgba(0, 0, 0, 0.06);

        .header-left {
          display: flex;
          align-items: center;
          gap: 10px;
        }

        .item-count {
          font-size: 13px;
          color: #64748b;
          font-weight: 500;
        }

        .item-tag {
          font-size: 12px;
          border-radius: 12px;
          background: #e6f7ff;
          border-color: #91d5ff;
          color: #1890ff;
        }

        .expand-btn {
          font-size: 13px;
          padding: 6px 12px;
          height: auto;
          border-radius: 16px;
          background: #ffffff;
          border: 1px solid #d9d9d9;
          color: #5c8eff;
          transition: all 0.3s ease;

          &:hover {
            background: #f0f9ff;
            border-color: #6ba4ff;
            transform: translateY(-1px);
          }

          .expand-icon {
            transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            margin-left: 4px;

            &.expanded {
              transform: rotate(180deg);
            }
          }
        }
      }

      .items-list {
        display: flex;
        flex-direction: column;
        gap: 12px;
      }

      .item-row {
        display: flex;
        align-items: stretch;
        padding: 14px;
        background: #ffffff;
        border-radius: 12px;
        transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
        position: relative;
        border: 1px solid rgba(0, 0, 0, 0.06);
        min-height: 94px;
        cursor: pointer;
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);

        &:hover {
          box-shadow: 0 6px 20px rgba(92, 142, 255, 0.2);
          transform: translateY(-3px) scale(1.015);
          border-color: rgba(92, 142, 255, 0.5);
          background: linear-gradient(to bottom, #ffffff 0%, #f8faff 100%);

          // 添加高亮边框效果
          &::after {
            content: '';
            position: absolute;
            inset: 0;
            border-radius: 12px;
            box-shadow: inset 0 0 0 1px rgba(92, 142, 255, 0.3);
            pointer-events: none;
          }
        }

        &:active {
          transform: translateY(-1px) scale(1.008);
          box-shadow: 0 3px 12px rgba(92, 142, 255, 0.15);
        }

        .item-image {
          width: 70px;
          min-height: 70px;
          height: 70px;
          border-radius: 10px;
          overflow: visible;
          margin-right: 14px;
          flex-shrink: 0;
          background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
          position: relative;
          z-index: 1;
          box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
          display: flex;
          align-items: center;
          justify-content: center;
          border: 1px solid #91d5ff;

          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            border-radius: 10px;
            position: relative;
            z-index: 1;
          }

          .no-image {
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;

            span {
              font-size: 28px;
              font-weight: 600;
              color: #1890ff;
            }
          }

          .quantity-badge {
            position: absolute;
            top: -4px;
            right: -4px;
            background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
            color: white;
            font-size: 11px;
            font-weight: 700;
            min-width: 18px;
            height: 18px;
            padding: 0 5px;
            border-radius: 9px;
            box-shadow:
              0 2px 6px rgba(255, 77, 79, 0.4),
              0 0 0 1.5px rgba(255, 255, 255, 1);
            z-index: 100;
            display: flex;
            align-items: center;
            justify-content: center;
            line-height: 1;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            animation: badge-bounce 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
            pointer-events: none;

            // 悬浮效果
            .item-row:hover & {
              transform: scale(1.1) rotate(-3deg);
              box-shadow:
                0 3px 8px rgba(255, 77, 79, 0.5),
                0 0 0 1.5px rgba(255, 255, 255, 1);
              background: linear-gradient(135deg, #ff4d4f 0%, #ff2626 100%);
            }

            // 数量大于9时显示更紧凑
            &.large-number {
              font-size: 9px;
              min-width: 20px;
              padding: 0 4px;
            }
          }

          // 徽章弹跳动画
          @keyframes badge-bounce {
            0% {
              transform: scale(0) rotate(-180deg);
              opacity: 0;
            }
            50% {
              transform: scale(1.2) rotate(10deg);
            }
            100% {
              transform: scale(1) rotate(0deg);
              opacity: 1;
            }
          }
        }

        .item-info {
          flex: 1;
          min-width: 0;
          display: flex;
          flex-direction: column;
          justify-content: space-between;

          .item-name {
            font-size: 15px;
            font-weight: 600;
            color: #2c5282;
            margin-bottom: 8px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            flex-shrink: 0;
          }

          .item-ingredients {
            display: flex;
            align-items: flex-start;
            gap: 6px;
            margin-bottom: 8px;
            font-size: 12px;

            .ingredients-label {
              flex-shrink: 0;

              .label-text {
                color: #64748b;
                font-weight: 500;
              }
            }

            .ingredients-list {
              display: flex;
              flex-wrap: wrap;
              gap: 5px;
              flex: 1;

              .ingredient-tag {
                display: inline-flex;
                align-items: center;
                gap: 3px;
                padding: 3px 8px;
                border-radius: 6px;
                font-size: 11px;
                line-height: 1.4;
                transition: all 0.2s ease;

                &.required {
                  background: linear-gradient(135deg, rgba(103, 194, 58, 0.9) 0%, rgba(93, 175, 52, 0.9) 100%);
                  color: white;
                  font-weight: 500;
                  box-shadow: 0 1px 4px rgba(103, 194, 58, 0.25);
                }

                &.optional {
                  background: rgba(232, 244, 232, 0.8);
                  color: #5da842;
                  border: 1px solid rgba(179, 225, 157, 0.5);
                }

                .ingredient-price {
                  font-size: 10px;
                  opacity: 0.85;
                  margin-left: 2px;
                }
              }
            }
          }

          .item-note {
            display: flex;
            align-items: flex-start;
            gap: 5px;
            margin-bottom: 8px;
            font-size: 12px;
            color: #c4873a;
            padding: 6px 10px;
            background: rgba(253, 246, 236, 0.8);
            border-radius: 8px;
            border: 1px solid rgba(245, 218, 177, 0.5);
            line-height: 1.5;
            box-shadow: 0 1px 4px rgba(230, 162, 60, 0.08);

            .el-icon {
              font-size: 13px;
              color: #c4873a;
              flex-shrink: 0;
              margin-top: 1px;
            }

            .note-text {
              flex: 1;
              word-break: break-word;
            }
          }

          .item-customization {
            font-size: 12px;
            color: #3a7bd5;
            margin-bottom: 8px;
            padding: 6px 10px;
            background: rgba(227, 242, 253, 0.7);
            border-radius: 8px;
            border: 1px solid rgba(187, 222, 251, 0.5);
            line-height: 1.5;
            max-height: 40px;
            overflow: hidden;
            display: flex;
            align-items: flex-start;
            gap: 5px;
            box-shadow: 0 1px 4px rgba(33, 150, 243, 0.08);

            .customization-text {
              flex: 1;
              overflow: hidden;
              text-overflow: ellipsis;
              display: -webkit-box;
              -webkit-line-clamp: 2;
              line-clamp: 2;
              -webkit-box-orient: vertical;
              word-break: break-word;
            }

            .el-icon {
              font-size: 14px;
              color: #4a90e2;
              flex-shrink: 0;
              margin-top: 1px;
            }
          }

          .item-price-detail {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-top: auto;
            flex-shrink: 0;

            .unit-price {
              font-size: 12px;
              color: #94a3b8;
            }

            .total-price {
              font-size: 16px;
              font-weight: 700;
              color: #ff6b6b;
            }
          }
        }
      }

      .items-empty {
        text-align: center;
        padding: 24px;
        color: #94a3b8;
        font-size: 14px;
      }
    }

    .order-total {
      display: flex;
      justify-content: flex-end;
      align-items: center;
      margin-bottom: 16px;
      padding-top: 4px;

      .total-text {
        margin-right: 12px;
        color: #64748b;
        font-size: 14px;
        font-weight: 500;
      }

      .total-amount {
        font-size: 20px;
        font-weight: 700;
        color: #ff6b6b;
        text-shadow: 0 1px 2px rgba(255, 107, 107, 0.1);
      }
    }

    .order-actions {
      display: flex;
      justify-content: flex-end;
      gap: 10px;

      :deep(.el-button) {
        border-radius: 20px;
        padding: 8px 18px;
        font-weight: 500;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);

        &:hover {
          transform: translateY(-1px);
        }

        &.el-button--primary {
          background: linear-gradient(135deg, #6ba4ff 0%, #5c8eff 100%);
          border-color: transparent;
          box-shadow: 0 3px 10px rgba(92, 142, 255, 0.3);

          &:hover {
            box-shadow: 0 4px 14px rgba(92, 142, 255, 0.4);
          }
        }

        &.el-button--danger {
          background: linear-gradient(135deg, #ff8a80 0%, #ff6b6b 100%);
          border-color: transparent;
          box-shadow: 0 3px 10px rgba(255, 107, 107, 0.3);

          &:hover {
            box-shadow: 0 4px 14px rgba(255, 107, 107, 0.4);
          }
        }

        &.el-button--success {
          background: linear-gradient(135deg, #81c784 0%, #66bb6a 100%);
          border-color: transparent;
          box-shadow: 0 3px 10px rgba(102, 187, 106, 0.3);

          &:hover {
            box-shadow: 0 4px 14px rgba(102, 187, 106, 0.4);
          }
        }
      }
    }
  }

  /* 刷新按钮动画优化 */
  .refresh-btn {
    position: relative;
    overflow: hidden;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);

    /* 按钮内图标和文字的容器 */
    :deep(.el-icon) {
      transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    }

    /* 刷新文字动画 */
    .refresh-text {
      display: inline-block;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }

    /* 刷新中的状态 */
    &.is-refreshing {
      background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%) !important;
      border-color: #69c0ff !important;
      color: #1890ff !important;
      box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);

      .refresh-text {
        animation: text-pulse 1s ease-in-out infinite;
      }

      /* 添加脉冲波纹效果 */
      &::after {
        content: '';
        position: absolute;
        top: 50%;
        left: 50%;
        width: 0;
        height: 0;
        border-radius: 50%;
        background: rgba(24, 144, 255, 0.3);
        transform: translate(-50%, -50%);
        animation: ripple 1.5s ease-out infinite;
      }
    }

    /* 刷新成功的状态 */
    &.is-success {
      background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%) !important;
      border-color: #95de64 !important;
      color: #52c41a !important;
      box-shadow: 0 2px 8px rgba(82, 196, 26, 0.3);

      /* 成功时的缩放动画 */
      animation: success-bounce 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55);

      .refresh-text {
        color: #52c41a;
        font-weight: 600;
      }
    }
  }

  /* 刷新图标旋转动画 */
  .refresh-rotating {
    animation: refresh-rotate 0.8s linear infinite;
  }

  /* 刷新成功时的图标动画 */
  .refresh-success {
    animation: success-check 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55);
    color: #52c41a;
  }

  @keyframes refresh-rotate {
    0% {
      transform: rotate(0deg);
    }
    100% {
      transform: rotate(360deg);
    }
  }

  @keyframes text-pulse {
    0%, 100% {
      opacity: 1;
      transform: scale(1);
    }
    50% {
      opacity: 0.7;
      transform: scale(0.95);
    }
  }

  @keyframes ripple {
    0% {
      width: 0;
      height: 0;
      opacity: 0.6;
    }
    100% {
      width: 200px;
      height: 200px;
      opacity: 0;
    }
  }

  @keyframes success-bounce {
    0% {
      transform: scale(1);
    }
    50% {
      transform: scale(1.05);
    }
    100% {
      transform: scale(1);
    }
  }

  @keyframes success-check {
    0% {
      transform: scale(0) rotate(-180deg);
      opacity: 0;
    }
    50% {
      transform: scale(1.2) rotate(10deg);
    }
    100% {
      transform: scale(1) rotate(0deg);
      opacity: 1;
    }
  }

  /* 订单列表过渡动画 */
  .order-list {
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);

    /* 淡出动画 */
    &.list-fade-out {
      animation: list-fade-out 0.3s cubic-bezier(0.4, 0, 0.2, 1) forwards;
    }

    /* 淡入动画 */
    &.list-fade-in {
      animation: list-fade-in 0.4s cubic-bezier(0.4, 0, 0.2, 1) forwards;
    }
  }

  @keyframes list-fade-out {
    0% {
      opacity: 1;
      transform: translateY(0);
    }
    100% {
      opacity: 0;
      transform: translateY(-10px);
    }
  }

  @keyframes list-fade-in {
    0% {
      opacity: 0;
      transform: translateY(10px);
    }
    100% {
      opacity: 1;
      transform: translateY(0);
    }
  }

  /* 订单卡片动画优化 */
  .order-card {
    /* 为每个卡片添加交错动画延迟 */
    animation: card-slide-in 0.5s cubic-bezier(0.4, 0, 0.2, 1) backwards;

    /* 为每个卡片添加递增的延迟，创建交错效果 */
    &:nth-child(1) { animation-delay: 0s; }
    &:nth-child(2) { animation-delay: 0.08s; }
    &:nth-child(3) { animation-delay: 0.16s; }
    &:nth-child(4) { animation-delay: 0.24s; }
    &:nth-child(5) { animation-delay: 0.32s; }
    &:nth-child(n+6) { animation-delay: 0.4s; }
  }

  /* 为订单卡片添加入场动画 */
  @keyframes card-slide-in {
    0% {
      opacity: 0;
      transform: translateY(20px) scale(0.95);
    }
    100% {
      opacity: 1;
      transform: translateY(0) scale(1);
    }
  }

  /* 响应式设计 */
  @media (max-width: 768px) {
    .orders-container {
      padding: 0 12px 16px 12px;
      background: #f5f7fa;

      .page-header {
        padding: 14px 16px;
        border-radius: 14px;
      }
    }

    .order-filters {
      padding: 10px 12px;
      border-radius: 10px;
      gap: 8px;
    }

    .order-list {
      gap: 12px;
    }

    .order-card {
      border-radius: 14px;

      :deep(.el-card__body) {
        padding: 16px;
      }

      .order-header {
        flex-direction: column;
        align-items: flex-start !important;
        margin-bottom: 12px;
        padding-bottom: 12px;

        .order-status {
          margin-top: 10px;
        }
      }

      .order-items {
        padding: 12px;
        border-radius: 10px;

        .items-header {
          margin-bottom: 10px;
          padding-bottom: 8px;

          .header-left {
            gap: 6px;
          }

          .expand-btn {
            font-size: 12px;
            padding: 5px 10px;
          }
        }

        .item-row {
          padding: 12px;
          min-height: 80px;
          border-radius: 10px;

          .item-image {
            width: 56px;
            min-height: 56px;
            height: 56px;
            margin-right: 12px;
            border-radius: 8px;
            overflow: visible;
            position: relative;
            z-index: 1;

            img {
              border-radius: 8px;
              position: relative;
              z-index: 1;
            }

            .quantity-badge {
              min-width: 16px;
              height: 16px;
              font-size: 10px;
              padding: 0 4px;
              top: -3px;
              right: -3px;
              border-radius: 8px;
              z-index: 100;
              box-shadow:
                0 2px 5px rgba(255, 77, 79, 0.4),
                0 0 0 1px rgba(255, 255, 255, 1);

              &.large-number {
                font-size: 8px;
                min-width: 18px;
                padding: 0 3px;
              }
            }

            .no-image span {
              font-size: 22px;
            }
          }

          .item-info {
            .item-name {
              font-size: 14px;
              margin-bottom: 6px;
            }

            .item-ingredients {
              font-size: 11px;
              margin-bottom: 6px;
              gap: 5px;

              .ingredients-list {
                gap: 4px;

                .ingredient-tag {
                  font-size: 10px;
                  padding: 2px 6px;
                  border-radius: 4px;
                }
              }
            }

            .item-note {
              font-size: 11px;
              padding: 5px 8px;
              margin-bottom: 6px;
              border-radius: 6px;

              .el-icon {
                font-size: 12px;
              }
            }

            .item-customization {
              font-size: 11px;
              padding: 5px 8px;
              max-height: 36px;
              min-height: 28px;
              border-radius: 6px;
              margin-bottom: 6px;

              .el-icon {
                font-size: 12px;
              }
            }

            .item-price-detail {
              flex-direction: column;
              align-items: flex-start;
              gap: 4px;

              .unit-price {
                font-size: 11px;
              }

              .total-price {
                font-size: 15px;
              }
            }
          }
        }
      }

      .order-total {
        margin-bottom: 12px;

        .total-text {
          font-size: 13px;
        }

        .total-amount {
          font-size: 18px;
        }
      }

      .order-actions {
        flex-direction: column;
        gap: 8px;

        :deep(.el-button) {
          width: 100%;
          padding: 10px;
        }
      }
    }

    .order-pagination {
      margin-top: 20px;
    }
  }
}
</style>
