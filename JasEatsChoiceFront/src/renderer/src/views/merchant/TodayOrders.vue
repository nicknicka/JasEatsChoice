<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  View,
  CircleCheck,
  CircleCheckFilled,
  Goods,
  Dish,
  Select,
  MoreFilled,
  User,
  CircleClose,
  Delete,
  Search,
  Refresh,
  Filter,
  List
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import api from '../../utils/api.js'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import { useAuthStore } from '../../store/authStore'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)

// 获取商家ID
const merchantId = authStore.merchantId || localStorage.getItem('auth_merchantId')

// 用户信息展开状态
const expandedUserInfo = ref(new Set())

// 切换用户信息展开状态
const toggleUserInfoExpand = (orderId) => {
  if (expandedUserInfo.value.has(orderId)) {
    expandedUserInfo.value.delete(orderId)
  } else {
    expandedUserInfo.value.add(orderId)
  }
}

// 数字动画
const animatedValues = ref({
  total: 0,
  totalAmount: 0,
  pendingCount: 0,
  preparingCount: 0,
  completedCount: 0
})

// 动画数字
const animateValue = (key, endValue, duration = 1000) => {
  const startValue = animatedValues.value[key]
  const startTime = performance.now()

  const animate = (currentTime) => {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)

    // 使用 easeOutQuart 缓动函数
    const easeOutQuart = 1 - Math.pow(1 - progress, 4)
    animatedValues.value[key] = Math.floor(startValue + (endValue - startValue) * easeOutQuart)

    if (progress < 1) {
      requestAnimationFrame(animate)
    } else {
      animatedValues.value[key] = endValue
    }
  }

  requestAnimationFrame(animate)
}

// 获取相对时间显示
const getRelativeTime = (timeStr) => {
  const orderTime = new Date(timeStr)
  const now = new Date()
  const diff = now - orderTime
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  return timeStr.split(' ')[1] // 显示具体时间
}

// 获取订单卡片样式类
const getOrderCardClass = (order) => {
  const classes = []
  classes.push(`status-${order.status}`)
  return classes.join(' ')
}

// 订单状态映射（对应后端状态码）
// 0-待支付、1-待接单、2-备菜中、3-烹饪中、4-待上菜、5-已送达、6-已取消、7-待评价、8-已评价
const orderStatusMap = {
  0: { text: '待支付', type: 'info', color: '#909399' },
  1: { text: '待接单', type: 'danger', color: '#f56c6c' },
  2: { text: '备菜中', type: 'warning', color: '#e6a23c' },
  3: { text: '烹饪中', type: 'warning', color: '#ff9800' },
  4: { text: '待上菜', type: 'primary', color: '#409eff' },
  5: { text: '已送达', type: 'success', color: '#67c23a' },
  6: { text: '已取消', type: 'info', color: '#c0c4cc' },
  7: { text: '待评价', type: 'success', color: '#95d475' },
  8: { text: '已评价', type: 'success', color: '#85ce61' }
}

// 全部订单数据
const orders = ref([])

// 当前选中的状态筛选（使用数字状态码）
const activeStatusFilter = ref('all')

// 搜索关键词
const searchKeyword = ref('')

// 筛选后的订单（改为computed自动计算）
const filteredOrders = computed(() => {
  return (
    orders.value
      .filter((order) => {
        // 状态筛选（支持数字状态码和组合状态）
        let statusMatch = false
        if (activeStatusFilter.value === 'all') {
          statusMatch = true
        } else if (activeStatusFilter.value === '2-4') {
          // 进行中：备菜中、烹饪中、待上菜
          statusMatch = order.status === 2 || order.status === 3 || order.status === 4
        } else {
          statusMatch = order.status === activeStatusFilter.value
        }

        // 搜索筛选（按订单ID或地址搜索）
        const searchMatch =
          !searchKeyword.value ||
          (order.id && order.id.toLowerCase().includes(searchKeyword.value.toLowerCase())) ||
          (order.address && order.address.toLowerCase().includes(searchKeyword.value.toLowerCase()))

        return statusMatch && searchMatch
      })
      // 按创建时间倒序排序
      .sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
  )
})

// 获取今天的日期（格式：YYYY-MM-DD）
const today = computed(() => {
  const date = new Date()
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
})

// 订单概览统计
const orderOverview = computed(() => {
  const total = filteredOrders.value.length
  const totalAmount = filteredOrders.value.reduce((sum, order) => sum + (order.totalAmount || 0), 0)
  // 使用数字状态码统计：1-待接单、2-备菜中、3-烹饪中、4-待上菜、5-已完成
  const pendingCount = filteredOrders.value.filter((order) => order.status === 1).length
  const preparingCount = filteredOrders.value.filter(
    (order) => order.status === 2 || order.status === 3
  ).length
  const cookingCount = filteredOrders.value.filter((order) => order.status === 3).length
  const readyCount = filteredOrders.value.filter((order) => order.status === 4).length
  const completedCount = filteredOrders.value.filter((order) => order.status === 3).length

  return {
    total,
    totalAmount,
    pendingCount,
    preparingCount,
    cookingCount,
    readyCount,
    completedCount
  }
})

// 监听订单概览变化，触发动画
watch(
  orderOverview,
  (newVal) => {
    animateValue('total', newVal.total)
    animateValue('totalAmount', newVal.totalAmount)
    animateValue('pendingCount', newVal.pendingCount)
    animateValue('preparingCount', newVal.preparingCount)
    animateValue('completedCount', newVal.completedCount)
  },
  { deep: true }
)

// 获取订单数据
const fetchOrders = async () => {
  console.log('[今日订单] 开始获取订单列表')
  console.log('[今日订单] 商家ID:', merchantId)

  if (!merchantId) {
    console.error('[今日订单] 商家ID为空')
    ElMessage.warning('未找到商家信息,请重新登录')
    return
  }

  loading.value = true
  try {
    const apiUrl = `/v1/orders/merchant/${merchantId}`
    console.log('[今日订单] 请求API:', apiUrl)

    const response = await api.get(apiUrl)
    console.log('[今日订单] API响应:', response)
    console.log('[今日订单] success字段:', response.success)
    console.log('[今日订单] message字段:', response.message)

    if (response.success) {
      const ordersData = response.data || []
      console.log('[今日订单] 订单数量:', ordersData.length)

      // 为每个订单获取菜品列表
      for (const order of ordersData) {
        try {
          const dishesResponse = await api.get(`/v1/orders/${order.id}/dishes`)
          console.log('[今日订单] 订单菜品API响应:', order.id, dishesResponse)
          console.log('[今日订单] 订单菜品数据:', JSON.stringify(dishesResponse.data, null, 2))
          if (dishesResponse.success) {
            order.orderDishes = dishesResponse.data || []
          }
        } catch (error) {
          console.error('[今日订单] 获取订单菜品失败:', order.id, error)
          order.orderDishes = []
        }
      }

      console.log('[今日订单] 订单数据:', ordersData)
      orders.value = ordersData
    } else {
      console.error('[今日订单] API返回失败:', response)
      ElMessage.error(response.message || '获取订单列表失败')
    }
  } catch (error) {
    console.error('[今日订单] 加载订单异常:', error)
    console.error('[今日订单] 错误详情:', {
      message: error.message,
      status: error.status,
      data: error.data
    })
    ElMessage.error('加载订单失败')
    orders.value = []
  } finally {
    loading.value = false
    console.log('[今日订单] 获取订单完成，loading设置为false')
  }
}

// 刷新订单数据
const refreshOrders = async () => {
  await fetchOrders()
}

// 查看订单详情
const viewOrderDetails = (order) => {
  // 标记为已读
  if (order.unread) {
    order.unread = false
    ElMessage.success('订单已标记为已读')
  }

  // 导航到订单详情页
  router.push(`/merchant/home/order-detail/${order.id}`)
}

// 更新订单状态
const updateOrderStatus = async (order, newStatus) => {
  try {
    const response = await api.put(`/v1/orders/${order.id}/status?status=${newStatus}`)
    if (response.success) {
      order.status = newStatus
      ElMessage.success(`订单状态已更新为${orderStatusMap[newStatus].text}`)
    } else {
      ElMessage.error(response.message || '更新失败')
    }
  } catch (error) {
    console.error('更新订单状态失败:', error)
    ElMessage.error('更新订单状态失败')
  }
}

// 拒绝接单
const rejectOrder = async (order) => {
  ElMessageBox.prompt('请输入拒绝原因（可选）', '拒绝接单', {
    confirmButtonText: '确定拒绝',
    cancelButtonText: '取消',
    type: 'warning',
    distinguishCancelAndClose: true,
    inputPattern: /^.{0,200}$/,
    inputErrorMessage: '拒绝原因不能超过200个字符'
  })
    .then(async ({ value }) => {
      const reason = value || '商家拒绝接单'
      try {
        // 使用状态更新接口将订单状态改为已取消(6)
        const response = await api.put(`/v1/orders/${order.id}/status?status=4`)
        if (response.success) {
          order.status = 4
          order.rejectReason = reason
          ElMessage.warning(`已拒绝接单: ${reason}`)
        } else {
          ElMessage.error(response.message || '拒绝接单失败')
        }
      } catch (error) {
        console.error('拒绝接单失败:', error)
        ElMessage.error('拒绝接单失败')
      }
    })
    .catch(() => {})
}

// 取消订单
const cancelOrder = async (order) => {
  ElMessageBox.confirm('确定要取消此订单吗？取消后将影响商家信誉。', '取消订单确认', {
    confirmButtonText: '确定取消',
    cancelButtonText: '再想想',
    type: 'warning',
    distinguishCancelAndClose: true
  })
    .then(async () => {
      try {
        const response = await api.put(`/v1/orders/${order.id}/cancel`)
        if (response.success) {
          order.status = 4
          ElMessage.warning('订单已取消')
        } else {
          ElMessage.error(response.message || '取消订单失败')
        }
      } catch (error) {
        console.error('取消订单失败:', error)
        ElMessage.error('取消订单失败')
      }
    })
    .catch(() => {})
}

// 删除订单
const deleteOrder = (order) => {
  ElMessageBox.confirm('删除后订单将无法恢复，确定要删除吗？', '删除订单确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'error',
    distinguishCancelAndClose: true
  })
    .then(() => {
      const index = orders.value.findIndex((item) => item.id === order.id)
      if (index !== -1) {
        orders.value.splice(index, 1)
        ElMessage.success('订单已删除')
      }
    })
    .catch(() => {})
}

// 获取状态标签文本（显示全部订单的数量，不受搜索影响）
const getStatusLabel = (status) => {
  if (status === 'all') return `全部 (${orders.value.length})`
  if (status === '2-4') {
    const count = orders.value.filter((o) => o.status === 2 || o.status === 3 || o.status === 4).length
    return `进行中 (${count})`
  }
  const count = orders.value.filter((o) => o.status === status).length
  return `${orderStatusMap[status].text} (${count})`
}

// 获取状态的订单数量
const getStatusCount = (status) => {
  if (status === 'all') return orders.value.length
  if (status === '2-4') {
    return orders.value.filter((o) => o.status === 2 || o.status === 3 || o.status === 4).length
  }
  return orders.value.filter((o) => o.status === status).length
}

// 获取空状态描述
const getEmptyDescription = () => {
  if (searchKeyword.value) {
    return '未找到匹配的订单'
  }
  if (activeStatusFilter.value === 1) {
    return '暂无待接单订单'
  }
  if (activeStatusFilter.value === 2) {
    return '暂无备菜中订单'
  }
  if (activeStatusFilter.value === 3) {
    return '暂无烹饪中订单'
  }
  if (activeStatusFilter.value === 4) {
    return '暂无待上菜订单'
  }
  if (activeStatusFilter.value === 5) {
    return '暂无已完成订单'
  }
  return '今日暂无订单'
}

// 页面加载时初始化
onMounted(() => {
  fetchOrders()
})
</script>

<template>
  <div class="merchant-orders-container">
    <!-- 头部 -->
    <div class="orders-header">
      <div class="header-left">
        <h3 class="page-title">今日订单</h3>
        <span class="current-date">{{ today }}</span>
      </div>
      <div class="header-right">
        <el-button size="small" :loading="loading" @click="refreshOrders"> 刷新 </el-button>
        <common-back-button type="default" />
      </div>
    </div>

    <!-- 订单统计卡片 -->
    <div class="overview-section">
      <div class="stat-card total">
        <div class="stat-icon">
          <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
            <path
              d="M912 160H128c-35.3 0-64 28.7-64 64v576c0 35.3 28.7 64 64 64h784c35.3 0 64-28.7 64-64V224c0-35.3-28.7-64-64-64z m-56 464H472c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8h384c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8z m0-192H472c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8h384c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8z m0-192H472c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8h384c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8z M168 624h200c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8H168c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8z m0-192h200c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8H168c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8z m0-192h200c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8H168c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8z"
              fill="currentColor"
            />
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value animated-number">{{ animatedValues.total }}</div>
          <div class="stat-label">总订单</div>
        </div>
      </div>

      <div class="stat-card amount highlight">
        <div class="stat-icon">
          <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
            <path
              d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64z m0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z m151.2-500.2L534.6 536.2c-3.1 3.1-8.2 3.1-11.3 0l-109-109c-3.1-3.1-3.1-8.2 0-11.3l36.4-36.4c3.1-3.1 8.2-3.1 11.3 0l82 82 214.6-214.6c3.1-3.1 8.2-3.1 11.3 0l36.4 36.4c3.1 3.1 3.1 8.2 0 11.3z"
              fill="currentColor"
            />
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value animated-number">¥{{ animatedValues.totalAmount.toFixed(0) }}</div>
          <div class="stat-label">总金额</div>
        </div>
      </div>

      <div class="stat-card pending">
        <div class="stat-icon">
          <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
            <path
              d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64z m0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z"
              fill="currentColor"
            />
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value animated-number">{{ animatedValues.pendingCount }}</div>
          <div class="stat-label">待处理</div>
        </div>
      </div>

      <div class="stat-card preparing">
        <div class="stat-icon">
          <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
            <path
              d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64z m193.5 301.7l-210.6 292c-12.7 17.7-39 17.7-51.7 0L318.5 484.9c-3.8-5.3 0-12.7 6.5-12.7h46.9c10.2 0 19.9 4.9 25.9 13.3l71.2 98.8 157.2-218c6-8.3 15.6-13.3 25.9-13.3H699c6.5 0 10.3 7.4 6.5 12.7z"
              fill="currentColor"
            />
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value animated-number">{{ animatedValues.preparingCount }}</div>
          <div class="stat-label">准备中</div>
        </div>
      </div>

      <div class="stat-card completed">
        <div class="stat-icon">
          <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
            <path
              d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64z m193.5 301.7l-210.6 292c-12.7 17.7-39 17.7-51.7 0L318.5 484.9c-3.8-5.3 0-12.7 6.5-12.7h46.9c10.2 0 19.9 4.9 25.9 13.3l71.2 98.8 157.2-218c6-8.3 15.6-13.3 25.9-13.3H699c6.5 0 10.3 7.4 6.5 12.7z"
              fill="currentColor"
            />
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value animated-number">{{ animatedValues.completedCount }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filter-section">
      <div class="filter-left">
        <div class="filter-header">
          <el-icon class="filter-icon"><Filter /></el-icon>
          <span class="filter-label">状态筛选</span>
        </div>
        <div class="status-filter-group">
          <div
            v-for="status in ['all', 0, 1, 2, 3, 4]"
            :key="status"
            :class="[
              'custom-status-tag',
              `status-tag-${status}`,
              { active: activeStatusFilter === status }
            ]"
            @click="activeStatusFilter = status"
          >
            <el-icon v-if="status === 'all'" class="tag-icon"><List /></el-icon>
            <el-icon v-else-if="status === 0" class="tag-icon"><CircleClose /></el-icon>
            <el-icon v-else-if="status === 1" class="tag-icon"><CircleClose /></el-icon>
            <el-icon v-else-if="status === '2-4'" class="tag-icon"><Goods /></el-icon>
            <el-icon v-else-if="status === 3" class="tag-icon"><CircleCheckFilled /></el-icon>
            <el-icon v-else-if="status === 4" class="tag-icon"><CircleClose /></el-icon>

            <span class="tag-text">
              {{ status === 'all' ? '全部'
                : status === '2-4' ? '进行中'
                : status === 0 ? '待支付'
                : orderStatusMap[status].text }}
              <template v-if="status !== 'all' && status !== '2-4'">({{ getStatusCount(status) }})</template>
              <template v-else-if="status === '2-4'">({{ getStatusCount(status) }})</template>
            </span>
          </div>
        </div>
      </div>

      <div class="filter-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索订单号/用户/电话"
          class="search-input"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 快捷操作栏 -->
    <div class="quick-actions">
      <div class="quick-actions-left">
        <el-button size="small" @click="refreshOrders" :loading="loading" class="quick-action-btn">
          <el-icon><Refresh /></el-icon>
          <span>刷新</span>
        </el-button>
      </div>
      <div class="quick-actions-right">
        <span class="order-count-info">
          共 <strong>{{ filteredOrders.length }}</strong> 个订单
        </span>
      </div>
    </div>

    <!-- 订单列表 -->
    <div v-loading="loading" class="orders-list-section">
      <transition-group name="list" tag="div">
        <div
          v-for="(order, index) in filteredOrders"
          :key="order.id"
          :class="['order-item', getOrderCardClass(order)]"
          :style="{ animationDelay: `${index * 0.05}s` }"
        >
          <div class="order-main">
            <div class="order-content">
              <div class="order-left">
                <div class="order-basic-info">
                  <div class="order-no">
                    <span class="no-label">订单号</span>
                    <span class="no-value">{{ order.id }}</span>
                  </div>
                  <div class="order-amount">
                    <span class="amount-label">金额</span>
                    <span class="amount-value">¥{{ (order.totalAmount || 0).toFixed(2) }}</span>
                  </div>
                  <div class="order-time" :title="order.createTime">
                    <span class="time-label">时间</span>
                    <span class="time-value">{{ getRelativeTime(order.createTime) }}</span>
                  </div>
                </div>

                <!-- 配送地址（始终显示） -->
                <div
                  v-if="order.address"
                  class="order-user-info"
                  style="margin-top: 10px; margin-bottom: 10px"
                >
                  <div class="user-address" style="grid-column: 1 / -1">
                    <span class="info-label">📍 配送地址</span>
                    <span class="info-value">{{ order.address }}</span>
                  </div>
                </div>

                <!-- 用户详细信息（可展开/收起） -->
                <div
                  v-if="expandedUserInfo.has(order.id)"
                  class="order-user-info user-detail-expanded"
                >
                  <div class="user-detail-item" style="grid-column: 1 / -1">
                    <span class="info-label">👤 用户ID</span>
                    <span class="info-value">{{ order.userId || '未知' }}</span>
                  </div>
                  <div v-if="order.paymentTime" class="user-detail-item">
                    <span class="info-label">💳 支付时间</span>
                    <span class="info-value">{{ order.paymentTime || '未支付' }}</span>
                  </div>
                  <div v-if="order.paidAmount" class="user-detail-item">
                    <span class="info-label">💰 已付金额</span>
                    <span class="info-value"
                      >¥{{ order.paidAmount ? order.paidAmount.toFixed(2) : '0.00' }}</span
                    >
                  </div>
                  <div v-if="order.addressId" class="user-detail-item">
                    <span class="info-label">📍 地址ID</span>
                    <span class="info-value">{{ order.addressId }}</span>
                  </div>
                </div>

                <!-- 订单备注（始终显示） -->
                <div v-if="order.remark" class="order-remark">
                  <div class="remark-icon">📝</div>
                  <div class="remark-content">
                    <span class="remark-label">备注：</span>
                    <span class="remark-text">{{ order.remark }}</span>
                  </div>
                </div>

                <!-- 菜品列表 -->
                <div
                  v-if="order.orderDishes && order.orderDishes.length > 0"
                  class="order-dishes-list"
                >
                  <div class="dishes-header">
                    <span class="dishes-title">🍽️ 菜品清单</span>
                    <span class="dishes-count">共{{ order.orderDishes.length }}件</span>
                  </div>
                  <div class="dishes-items">
                    <div v-for="dish in order.orderDishes" :key="dish.id" class="dish-item">
                      <div class="dish-info">
                        <span class="dish-name">{{ dish.dishName || '未知菜品' }}</span>
                        <span class="dish-quantity">× {{ dish.quantity || 0 }}</span>
                        <span v-if="dish.customization" class="dish-customization"
                          >({{ dish.customization }})</span
                        >
                      </div>
                      <div class="dish-price">
                        ¥{{ ((dish.price || 0) * (dish.quantity || 0)).toFixed(2) }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="order-right">
                <div class="order-status">
                  <el-tag :type="orderStatusMap[order.status].type" size="large">
                    {{ orderStatusMap[order.status].text }}
                  </el-tag>
                </div>
              </div>
            </div>

            <div class="order-divider"></div>

            <div class="order-actions">
              <!-- 主要操作按钮组 -->
              <div class="primary-actions">
                <el-button
                  type="primary"
                  size="small"
                  @click="viewOrderDetails(order)"
                  class="detail-btn"
                >
                  <el-icon><View /></el-icon>
                  <span>详情</span>
                </el-button>

                <!-- 状态转换按钮 -->
                <el-button
                  v-if="order.status === 1"
                  type="success"
                  size="small"
                  @click="updateOrderStatus(order, 2)"
                  class="action-btn"
                >
                  <el-icon><CircleCheck /></el-icon>
                  <span>接单</span>
                </el-button>

                <el-button
                  v-if="order.status === 1"
                  type="danger"
                  size="small"
                  @click="rejectOrder(order)"
                  class="action-btn"
                >
                  <el-icon><CircleClose /></el-icon>
                  <span>拒绝</span>
                </el-button>

                <el-button
                  v-if="order.status === 2"
                  type="warning"
                  size="small"
                  @click="updateOrderStatus(order, 3)"
                  class="action-btn"
                >
                  <el-icon><Goods /></el-icon>
                  <span>烹饪</span>
                </el-button>

                <el-button
                  v-if="order.status === 3"
                  type="primary"
                  size="small"
                  @click="updateOrderStatus(order, 4)"
                  class="action-btn"
                >
                  <el-icon><Dish /></el-icon>
                  <span>上菜</span>
                </el-button>

                <el-button
                  v-if="order.status === 4"
                  type="success"
                  size="small"
                  @click="updateOrderStatus(order, 5)"
                  class="action-btn complete-btn"
                >
                  <el-icon><CircleCheckFilled /></el-icon>
                  <span>完成</span>
                </el-button>

                <el-button
                  v-if="order.status === 3"
                  type="info"
                  size="small"
                  disabled
                  class="action-btn"
                >
                  <el-icon><Select /></el-icon>
                  <span>已完成</span>
                </el-button>
              </div>

              <!-- 更多操作下拉菜单 -->
              <el-dropdown trigger="click" class="more-dropdown">
                <el-button type="info" size="small" plain class="more-btn">
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="toggleUserInfoExpand(order.id)">
                      <el-icon><User /></el-icon>
                      <span>{{ expandedUserInfo.has(order.id) ? '隐藏' : '显示' }}用户信息</span>
                    </el-dropdown-item>
                    <el-dropdown-item v-if="order.status < 3" divided @click="cancelOrder(order)">
                      <el-icon><CircleClose /></el-icon>
                      <span class="dropdown-text-warning">取消订单</span>
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="deleteOrder(order)">
                      <el-icon><Delete /></el-icon>
                      <span class="dropdown-text-danger">删除订单</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>

        <!-- 空数据提示 -->
        <div v-if="filteredOrders.length === 0" key="empty" class="empty-orders">
          <el-empty :description="getEmptyDescription()"></el-empty>
        </div>
      </transition-group>
    </div>
  </div>
</template>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';
@import '../../assets/css/merchant-theme.less';

.merchant-orders-container {
  padding: 0 20px 20px 20px;

  .orders-header {
    margin-bottom: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    background: linear-gradient(135deg, @merchant-primary 0%, @merchant-primary-dark 100%);
    border-radius: 16px;
    box-shadow: 0 4px 16px rgba(74, 122, 77, 0.3);

    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;

      .page-title {
        font-size: 1.714rem /* 原值: 24px */;
        font-weight: 700;
        margin: 0;
        color: @merchant-surface;
        letter-spacing: 0.5px;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }

      .current-date {
        font-size: 1rem /* 原值: 14px */;
        color: @merchant-surface;
        background: rgba(255, 255, 255, 0.25);
        padding: 6px 16px;
        border-radius: 20px;
        font-weight: 500;
        backdrop-filter: blur(10px);
        border: 1px solid rgba(255, 255, 255, 0.3);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }
    }

    .header-right {
      display: flex;
      gap: 10px;
      align-items: center;

      :deep(.el-button) {
        backdrop-filter: blur(10px);
        border: 1px solid rgba(255, 255, 255, 0.3);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }
      }
    }
  }

  .overview-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24px;
    background: @merchant-primary-light;
    border: 1px solid @merchant-primary;
    border-radius: 16px;
    margin-bottom: 24px;
    flex-wrap: wrap;
    gap: 16px;
    box-shadow: 0 4px 12px @merchant-shadow;

    .stat-card {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 20px 24px;
      background: @merchant-surface;
      border-radius: 16px;
      box-shadow: 0 2px 12px @merchant-shadow;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      cursor: pointer;
      flex: 1;
      min-width: 160px;
      border: 1px solid @merchant-border;
      position: relative;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: linear-gradient(
          135deg,
          rgba(255, 255, 255, 0) 0%,
          rgba(255, 255, 255, 0.5) 100%
        );
        opacity: 0;
        transition: opacity 0.3s ease;
      }

      &:hover {
        transform: translateY(-8px) scale(1.02);
        box-shadow: 0 12px 28px rgba(74, 122, 77, 0.25);
        border-color: @merchant-primary-light;

        &::before {
          opacity: 1;
        }

        .stat-icon {
          transform: rotate(10deg) scale(1.1);
        }
      }

      &:active {
        transform: translateY(-6px) scale(1.01);
      }

      .stat-icon {
        width: 52px;
        height: 52px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        border-radius: 14px;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        position: relative;
        z-index: 1;

        svg {
          width: 28px;
          height: 28px;
        }
      }

      .stat-content {
        flex: 1;
        position: relative;
        z-index: 1;

        .stat-value {
          font-size: 26px;
          font-weight: 700;
          color: @merchant-primary-dark;
          line-height: 1.2;
          letter-spacing: -0.5px;
          transition: all 0.3s ease;

          &.animated-number {
            display: inline-block;
            animation: countUp 0.6s ease-out;
          }
        }

        .stat-label {
          font-size: 0.929rem /* 原值: 13px */;
          color: @merchant-info;
          margin-top: 6px;
          font-weight: 500;
          letter-spacing: 0.3px;
        }
      }

      &.total {
        border-left: 4px solid @merchant-info;

        .stat-icon {
          background: linear-gradient(
            135deg,
            rgba(91, 139, 210, 0.15) 0%,
            rgba(91, 139, 210, 0.08) 100%
          );
          color: @merchant-info;
        }

        .stat-value {
          color: @merchant-info;
        }
      }

      &.amount {
        border-left: 4px solid @merchant-secondary;

        .stat-icon {
          background: linear-gradient(
            135deg,
            rgba(181, 106, 74, 0.15) 0%,
            rgba(181, 106, 74, 0.08) 100%
          );
          color: @merchant-secondary;
        }

        .stat-value {
          color: @merchant-secondary;
        }

        &.highlight {
          background: linear-gradient(135deg, @merchant-secondary-light 0%, @merchant-surface 100%);
          border-width: 2px;

          .stat-value {
            font-size: 2rem /* 原值: 28px */;
          }
        }
      }

      &.pending {
        border-left: 4px solid @merchant-warning;

        .stat-icon {
          background: linear-gradient(
            135deg,
            rgba(212, 168, 85, 0.15) 0%,
            rgba(212, 168, 85, 0.08) 100%
          );
          color: @merchant-warning;
        }

        .stat-value {
          color: @merchant-warning;
        }
      }

      &.preparing {
        border-left: 4px solid @merchant-warning;

        .stat-icon {
          background: linear-gradient(
            135deg,
            rgba(212, 168, 85, 0.15) 0%,
            rgba(212, 168, 85, 0.08) 100%
          );
          color: @merchant-warning;
        }

        .stat-value {
          color: @merchant-warning;
        }
      }

      &.completed {
        border-left: 4px solid @merchant-success;

        .stat-icon {
          background: linear-gradient(
            135deg,
            rgba(90, 143, 94, 0.15) 0%,
            rgba(90, 143, 94, 0.08) 100%
          );
          color: @merchant-success;
        }

        .stat-value {
          color: @merchant-success;
        }
      }
    }
  }

  .filter-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 20px;
    background: linear-gradient(135deg, @merchant-surface-alt 0%, @merchant-surface 100%);
    border-radius: 12px;
    margin-bottom: 16px;
    box-shadow: 0 2px 12px @merchant-shadow;
    border: 1px solid @merchant-border;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 4px 16px @merchant-shadow-hover;
    }

    .filter-left {
      display: flex;
      flex-direction: column;
      gap: 8px;
      flex: 1;

      .filter-header {
        display: flex;
        align-items: center;
        gap: 6px;

        .filter-icon {
          font-size: 1.143rem /* 原值: 16px */;
          color: @merchant-primary;
        }

        .filter-label {
          font-size: 0.929rem /* 原值: 13px */;
          font-weight: 600;
          color: @merchant-text;
        }
      }

      .status-filter-group {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        align-items: center;

        .custom-status-tag {
          cursor: pointer;
          transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
          padding: 6px 12px;
          font-size: 0.857rem;
          font-weight: 500;
          border-radius: 8px;
          display: inline-flex;
          align-items: center;
          gap: 4px;
          user-select: none;
          white-space: nowrap;
          position: relative;

          // 默认未选中状态：透明/灰色
          background: transparent;
          color: @merchant-text-muted;
          border: 1px solid @merchant-border;

          .tag-icon {
            font-size: 0.857rem;
            opacity: 0.6;
          }

          .tag-text {
            font-size: 0.857rem;
          }

          .close-icon {
            font-size: 10px;
            margin-left: 2px;
            opacity: 0.8;
            transition: all 0.2s ease;

            &:hover {
              opacity: 1;
              transform: scale(1.1);
            }
          }

          // hover 状态：轻微提示可点击
          &:hover {
            background: @merchant-surface-alt;
            border-color: @merchant-primary;
            color: @merchant-primary;

            .tag-icon {
              opacity: 0.8;
            }

            transform: translateY(-1px);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
          }

          &:active {
            transform: translateY(0);
          }

          // ==================== 选中状态样式 ====================

          // 全部标签选中
          &.status-tag-all.active {
            background: linear-gradient(135deg, @merchant-primary 0%, lighten(@merchant-primary, 8%) 100%);
            color: @merchant-surface;
            border-color: @merchant-primary;
            box-shadow: 0 2px 8px rgba(74, 122, 77, 0.4);

            .tag-icon {
              opacity: 1;
            }

            &:hover {
              background: linear-gradient(135deg, lighten(@merchant-primary, 8%) 0%, lighten(@merchant-primary, 14%) 100%);
              transform: translateY(-1px);
              box-shadow: 0 4px 12px rgba(74, 122, 77, 0.5);
            }
          }

          // 待支付选中 - 灰色
          &.status-tag-0.active {
            background: linear-gradient(135deg, @merchant-text-muted 0%, lighten(@merchant-text-muted, 8%) 100%);
            color: @merchant-surface;
            border-color: @merchant-text-muted;
            box-shadow: 0 2px 8px rgba(158, 152, 147, 0.3);

            .tag-icon {
              opacity: 1;
            }

            &:hover {
              background: linear-gradient(135deg, lighten(@merchant-text-muted, 8%) 0%, lighten(@merchant-text-muted, 14%) 100%);
            }
          }

          // 待接单选中 - 红色
          &.status-tag-1.active {
            background: linear-gradient(135deg, @merchant-error 0%, lighten(@merchant-error, 8%) 100%);
            color: @merchant-surface;
            border-color: @merchant-error;
            box-shadow: 0 2px 8px rgba(196, 91, 91, 0.4);

            .tag-icon {
              opacity: 1;
            }

            &:hover {
              background: linear-gradient(135deg, lighten(@merchant-error, 8%) 0%, lighten(@merchant-error, 14%) 100%);
            }
          }

          // 进行中选中 - 橙色
          &.status-tag-2-4.active {
            background: linear-gradient(135deg, @merchant-warning 0%, lighten(@merchant-warning, 8%) 100%);
            color: @merchant-surface;
            border-color: @merchant-warning;
            box-shadow: 0 2px 8px rgba(212, 168, 85, 0.4);

            .tag-icon {
              opacity: 1;
            }

            &:hover {
              background: linear-gradient(135deg, lighten(@merchant-warning, 8%) 0%, lighten(@merchant-warning, 14%) 100%);
            }
          }

          // 备菜中选中
          &.status-tag-2.active {
            background: linear-gradient(135deg, @merchant-warning 0%, lighten(@merchant-warning, 8%) 100%);
            color: @merchant-surface;
            border-color: @merchant-warning;
            box-shadow: 0 2px 8px rgba(212, 168, 85, 0.4);

            .tag-icon {
              opacity: 1;
            }
          }

          // 烹饪中选中
          &.status-tag-3.active {
            background: linear-gradient(135deg, @merchant-warning 0%, lighten(@merchant-warning, 8%) 100%);
            color: @merchant-surface;
            border-color: @merchant-warning;
            box-shadow: 0 2px 8px rgba(212, 168, 85, 0.4);

            .tag-icon {
              opacity: 1;
            }
          }

          // 待上菜选中
          &.status-tag-4.active {
            background: linear-gradient(135deg, @merchant-info 0%, lighten(@merchant-info, 8%) 100%);
            color: @merchant-surface;
            border-color: @merchant-info;
            box-shadow: 0 2px 8px rgba(91, 139, 210, 0.4);

            .tag-icon {
              opacity: 1;
            }
          }

          // 已完成选中 - 绿色
          &.status-tag-3.active {
            background: linear-gradient(135deg, @merchant-success 0%, lighten(@merchant-success, 8%) 100%);
            color: @merchant-surface;
            border-color: @merchant-success;
            box-shadow: 0 2px 8px rgba(90, 143, 94, 0.4);

            .tag-icon {
              opacity: 1;
            }

            &:hover {
              background: linear-gradient(135deg, lighten(@merchant-success, 8%) 0%, lighten(@merchant-success, 14%) 100%);
            }
          }

          // 已取消选中
          &.status-tag-4.active {
            background: linear-gradient(135deg, @merchant-text-muted 0%, lighten(@merchant-text-muted, 8%) 100%);
            color: @merchant-surface;
            border-color: @merchant-text-muted;
            box-shadow: 0 2px 8px rgba(158, 152, 147, 0.3);

            .tag-icon {
              opacity: 1;
            }

            &:hover {
              background: linear-gradient(135deg, lighten(@merchant-text-muted, 8%) 0%, lighten(@merchant-text-muted, 14%) 100%);
            }
          }
        }
      }
    }

    .filter-right {
      flex-shrink: 0;

      :deep(.search-input) {
        width: 320px;

        .el-input__wrapper {
          border-radius: 10px;
          padding: 8px 12px;
          box-shadow: 0 1px 3px @merchant-shadow;
          transition: all 0.3s ease;

          &:hover {
            box-shadow: 0 2px 8px @merchant-shadow-hover;
          }

          &.is-focus {
            box-shadow:
              0 0 0 2px rgba(74, 122, 77, 0.2),
              0 2px 8px rgba(74, 122, 77, 0.3);
          }
        }

        .el-input__prefix {
          color: @merchant-text-muted;
        }
      }
    }
  }

  .quick-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    background: @merchant-surface;
    border-radius: 12px;
    margin-bottom: 20px;
    box-shadow: 0 2px 8px @merchant-shadow;
    border: 1px solid @merchant-border;

    .quick-actions-left {
      display: flex;
      gap: 10px;

      .quick-action-btn {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 8px 16px;
        border-radius: 8px;
        font-weight: 500;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        box-shadow: 0 1px 3px @merchant-shadow;

        .el-icon {
          font-size: 1rem /* 原值: 14px */;
        }

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }

        &:active {
          transform: translateY(0);
        }
      }
    }

    .quick-actions-right {
      .order-count-info {
        font-size: 1rem /* 原值: 14px */;
        color: @merchant-text-sec;
        padding: 8px 16px;
        background: linear-gradient(135deg, @merchant-primary-light 0%, @merchant-surface 100%);
        border-radius: 8px;
        border: 1px solid @merchant-primary;

        strong {
          color: @merchant-primary;
          font-size: 1.143rem /* 原值: 16px */;
          font-weight: 700;
        }
      }
    }
  }

  .orders-list-section {
    min-height: 400px;

    .order-item {
      margin-bottom: 16px;
      margin-top: 16px;
      background-color: @merchant-surface;
      position: relative;
      overflow: visible;
      border-radius: 12px;
      /* 卡片入场动画 */
      animation: card-slide-in 0.5s cubic-bezier(0.4, 0, 0.2, 1) forwards;
      opacity: 0;

      .order-main {
        display: flex;
        flex-direction: column;
        padding: 18px 20px;
        border: 2px solid @merchant-border;
        border-radius: 12px;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        box-shadow: 0 2px 8px @merchant-shadow;
        position: relative;
        overflow: visible;
        gap: 16px;

        .order-content {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          gap: 20px;
        }

        .order-divider {
          height: 1px;
          background: linear-gradient(
            90deg,
            transparent 0%,
            @merchant-border 10%,
            @merchant-border 90%,
            transparent 100%
          );
          margin: 0;
        }

        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 0;
          bottom: 0;
          width: 4px;
          background: @merchant-border;
          transition: all 0.3s ease;
          border-radius: 12px 0 0 12px;
        }

        &:hover {
          transform: translateY(-4px) scale(1.01);
          box-shadow: 0 12px 32px @merchant-shadow-hover;
          border-color: @merchant-text-muted;
        }

        &:active {
          transform: translateY(-2px) scale(0.998);
        }
      }

      // 状态边框颜色（使用数字状态码）
      &.status-1 .order-main::before {
        background: linear-gradient(180deg, @merchant-error 0%, lighten(@merchant-error, 8%) 100%);
        box-shadow: 0 0 8px rgba(196, 91, 91, 0.3);
      }

      &.status-2 .order-main::before {
        background: linear-gradient(180deg, @merchant-warning 0%, lighten(@merchant-warning, 8%) 100%);
        box-shadow: 0 0 8px rgba(212, 168, 85, 0.3);
      }

      &.status-3 .order-main::before {
        background: linear-gradient(180deg, @merchant-warning 0%, lighten(@merchant-warning, 8%) 100%);
        box-shadow: 0 0 8px rgba(212, 168, 85, 0.3);
      }

      &.status-4 .order-main::before {
        background: linear-gradient(180deg, @merchant-info 0%, lighten(@merchant-info, 8%) 100%);
        box-shadow: 0 0 8px rgba(91, 139, 210, 0.3);
      }

      &.status-3 .order-main::before {
        background: linear-gradient(180deg, @merchant-success 0%, lighten(@merchant-success, 8%) 100%);
        box-shadow: 0 0 8px rgba(90, 143, 94, 0.3);
      }

      &.status-4 .order-main::before {
        background: linear-gradient(180deg, @merchant-text-muted 0%, lighten(@merchant-text-muted, 8%) 100%);
      }

      // 未读订单样式
      &.unread-order .order-main {
        background: linear-gradient(to right, @merchant-error-light 0%, @merchant-surface 40%);
        border-color: @merchant-error-light;

        &::after {
          content: '新订单';
          position: absolute;
          top: -10px;
          left: 20px;
          background: linear-gradient(135deg, @merchant-error 0%, lighten(@merchant-error, 8%) 100%);
          color: @merchant-surface;
          font-size: 0.75rem /* 原值: 11px */;
          padding: 4px 12px;
          border-radius: 12px;
          font-weight: 600;
          animation: newOrderPulse 2s ease-in-out infinite;
          box-shadow: 0 2px 8px rgba(196, 91, 91, 0.4);
          z-index: 10;
        }
      }

      // 加急订单样式
      &.urgent-order .order-main {
        border-width: 2px;
        border-style: solid;

        &:not(.status-cancelled) {
          border-color: @merchant-error;
          background: linear-gradient(to right, @merchant-error-light 0%, @merchant-surface 20%);

          &::before {
            width: 5px;
            background: linear-gradient(180deg, @merchant-error 0%, lighten(@merchant-error, 8%) 100%);
            box-shadow: 0 0 12px rgba(196, 91, 91, 0.5);
          }
        }
      }

      .order-left {
        flex: 1;
        min-width: 0;

        .order-basic-info {
          display: grid;
          grid-template-columns: auto auto 1fr;
          gap: 12px 20px;
          margin-bottom: 12px;
          font-size: 1rem /* 原值: 14px */;
          align-items: center;

          .order-no {
            display: flex;
            align-items: center;
            gap: 8px;
            font-weight: 600;
            font-size: 1.071rem /* 原值: 15px */;
            white-space: nowrap;

            .no-label {
              color: @merchant-text-muted;
              font-size: 0.857rem /* 原值: 12px */;
              font-weight: 500;
              margin-right: 4px;
            }

            .no-value {
              color: @merchant-text;
              font-family: 'Consolas', 'Monaco', monospace;
              font-weight: 600;
            }

            .priority-tag {
              animation: urgentPulse 1.5s ease-in-out infinite;
              font-weight: 600;
              flex-shrink: 0;
              margin-left: 8px;
            }
          }

          .order-amount {
            display: flex;
            align-items: center;
            gap: 6px;
            font-weight: 700;
            font-size: 17px;
            color: @merchant-error;
            white-space: nowrap;

            .amount-label {
              font-size: 0.857rem /* 原值: 12px */;
              color: @merchant-text-muted;
              font-weight: 500;
            }

            .amount-value {
              font-family: 'Consolas', 'Monaco', monospace;
            }
          }

          .order-time {
            display: flex;
            align-items: center;
            gap: 6px;
            color: @merchant-text-sec;
            font-size: 0.929rem /* 原值: 13px */;
            justify-self: end;
            white-space: nowrap;

            .time-label {
              color: @merchant-text-muted;
              font-size: 0.857rem /* 原值: 12px */;
              font-weight: 500;
            }

            .time-value {
              font-weight: 500;
            }
          }
        }

        .order-user-info {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
          gap: 10px 20px;
          font-size: 1rem /* 原值: 14px */;
          color: @merchant-text-sec;
          margin-bottom: 8px;

          > div {
            display: flex;
            align-items: center;
            gap: 8px;
            min-width: 0;

            .info-label {
              font-size: 0.857rem /* 原值: 12px */;
              color: @merchant-text-muted;
              font-weight: 500;
              flex-shrink: 0;
              min-width: 36px;
            }

            .info-value {
              font-weight: 500;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;

              &.remark-text {
                color: @merchant-warning;
                font-style: italic;
              }
            }
          }

          .user-remark {
            grid-column: 1 / -1;
            background: linear-gradient(135deg, @merchant-warning-light 0%, lighten(@merchant-warning-light, 3%) 100%);
            padding: 8px 12px;
            border-radius: 8px;
            border: 1px solid @merchant-warning;

            .info-value {
              color: @merchant-warning;
            }
          }
        }

        .order-remark {
          display: flex;
          align-items: center;
          gap: 10px;
          padding: 10px 14px;
          background: linear-gradient(135deg, @merchant-warning-light 0%, lighten(@merchant-warning-light, 3%) 100%);
          border-radius: 10px;
          border: 1px solid @merchant-warning;
          margin-bottom: 10px;
          box-shadow: 0 1px 4px rgba(212, 168, 85, 0.2);

          .remark-icon {
            font-size: 1.143rem /* 原值: 16px */;
            flex-shrink: 0;
            animation: noteBounce 2s ease-in-out infinite;
          }

          .remark-content {
            flex: 1;
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 0.929rem /* 原值: 13px */;

            .remark-label {
              font-weight: 600;
              color: @merchant-warning;
              flex-shrink: 0;
            }

            .remark-text {
              color: @merchant-primary-dark;
              font-weight: 500;
              word-break: break-word;
            }
          }
        }

        // 菜品列表样式
        .order-dishes-list {
          margin-top: 12px;
          padding: 12px 16px;
          background: linear-gradient(135deg, @merchant-surface-alt 0%, @merchant-surface 100%);
          border-radius: 10px;
          border: 1px solid @merchant-border;
          box-shadow: 0 1px 4px @merchant-shadow;

          .dishes-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 10px;
            padding-bottom: 8px;
            border-bottom: 1px dashed @merchant-border;

            .dishes-title {
              font-size: 0.929rem /* 原值: 13px */;
              font-weight: 600;
              color: @merchant-text;
            }

            .dishes-count {
              font-size: 0.857rem /* 原值: 12px */;
              color: @merchant-text-muted;
              background: @merchant-surface-alt;
              padding: 2px 8px;
              border-radius: 10px;
            }
          }

          .dishes-items {
            display: flex;
            flex-direction: column;
            gap: 8px;

            .dish-item {
              display: flex;
              justify-content: space-between;
              align-items: center;
              padding: 8px 12px;
              background: @merchant-surface;
              border-radius: 8px;
              border: 1px solid @merchant-border;
              transition: all 0.2s ease;

              &:hover {
                border-color: @merchant-primary;
                box-shadow: 0 2px 8px rgba(74, 122, 77, 0.1);
                transform: translateX(4px);
              }

              .dish-info {
                display: flex;
                align-items: center;
                gap: 10px;
                flex: 1;
                min-width: 0;

                .dish-name {
                  font-size: 1rem /* 原值: 14px */;
                  font-weight: 500;
                  color: @merchant-text;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                }

                .dish-quantity {
                  font-size: 0.929rem /* 原值: 13px */;
                  color: @merchant-primary;
                  font-weight: 600;
                  flex-shrink: 0;
                  background: linear-gradient(
                    135deg,
                    rgba(74, 122, 77, 0.1) 0%,
                    rgba(74, 122, 77, 0.05) 100%
                  );
                  padding: 2px 8px;
                  border-radius: 12px;
                }

                .dish-customization {
                  font-size: 0.857rem /* 原值: 12px */;
                  color: @merchant-warning;
                  font-weight: 500;
                  font-style: italic;
                }
              }

              .dish-price {
                font-size: 1rem /* 原值: 14px */;
                font-weight: 600;
                color: @merchant-error;
                font-family: 'Consolas', 'Monaco', monospace;
                flex-shrink: 0;
              }
            }
          }
        }
      }

      .order-right {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        gap: 12px;
        flex-shrink: 0;
        align-self: flex-start;

        .order-status {
          position: relative;

          .unread-badge {
            position: absolute;
            top: -5px;
            right: -5px;
          }
        }
      }

      .order-actions {
        display: flex;
        gap: 8px;
        justify-content: flex-end;
        align-items: center;
        width: 100%;
        padding-top: 4px;

        .primary-actions {
          display: flex;
          gap: 6px;
          flex-wrap: wrap;
          justify-content: flex-end;
        }

        :deep(.el-button) {
          display: inline-flex;
          align-items: center;
          gap: 4px;
          height: 32px;
          padding: 6px 14px;
          font-size: 0.929rem /* 原值: 13px */;
          font-weight: 500;
          white-space: nowrap;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          border-radius: 8px;
          box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

          .el-icon {
            font-size: 1rem /* 原值: 14px */;
          }

          &:hover {
            transform: translateY(-1px);
            box-shadow: 0 3px 8px rgba(0, 0, 0, 0.15);
          }

          &:active {
            transform: translateY(0);
          }

          &.detail-btn {
            background: linear-gradient(135deg, @merchant-primary 0%, @merchant-secondary 100%);
            border: none;
            color: @merchant-surface;

            &:hover {
              background: linear-gradient(135deg, @merchant-primary-dark 0%, darken(@merchant-secondary, 5%) 100%);
              box-shadow: 0 4px 10px rgba(74, 122, 77, 0.35);
            }
          }

          &.action-btn {
            min-width: 70px;
            font-weight: 600;
          }

          &.complete-btn {
            background: linear-gradient(135deg, @merchant-success 0%, lighten(@merchant-success, 15%) 100%);
            border: none;
            color: @merchant-surface;

            &:hover {
              background: linear-gradient(135deg, @merchant-primary-dark 0%, @merchant-success 100%);
              box-shadow: 0 4px 10px rgba(90, 143, 94, 0.35);
            }
          }

          &:disabled {
            opacity: 0.6;
            cursor: not-allowed;
            background: @merchant-surface-alt;
            border-color: @merchant-border;
            color: @merchant-text-muted;

            &:hover {
              transform: none;
              box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
            }
          }
        }

        .more-dropdown {
          :deep(.el-button) {
            &.more-btn {
              width: 32px;
              height: 32px;
              padding: 0;
              display: inline-flex;
              align-items: center;
              justify-content: center;
              border-radius: 50%;
            }
          }
        }

        :deep(.el-dropdown-menu) {
          .el-dropdown-menu__item {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 8px 16px;

            .el-icon {
              font-size: 1.143rem /* 原值: 16px */;
            }

            &:hover {
              background: linear-gradient(135deg, @merchant-surface-alt 0%, @merchant-border 100%);
            }
          }
        }
      }
    }

    .empty-orders {
      text-align: center;
      margin-top: 50px;
    }
  }
}

// 动画关键帧
@keyframes newOrderPulse {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.85;
    transform: scale(1.08);
  }
}

@keyframes noteBounce {
  0%,
  100% {
    transform: rotate(0deg);
  }
  25% {
    transform: rotate(-10deg);
  }
  75% {
    transform: rotate(10deg);
  }
}

@keyframes urgentPulse {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(196, 91, 91, 0.4);
  }
  50% {
    opacity: 0.9;
    transform: scale(1.05);
    box-shadow: 0 0 0 6px rgba(196, 91, 91, 0);
  }
}

@keyframes countUp {
  0% {
    opacity: 0;
    transform: translateY(10px);
  }
  50% {
    opacity: 1;
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

// 卡片进入动画
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

// 列表进入动画
.list-enter-active,
.list-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.list-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

.list-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

.list-move {
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

// 折叠动画
.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.2s cubic-bezier(1, 0.5, 0.8, 1);
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateY(-10px);
  opacity: 0;
}

@media (max-width: 1200px) {
  .merchant-orders-container {
    .orders-list-section {
      .order-item {
        .order-main {
          .order-content {
            .order-left {
              .order-basic-info {
                grid-template-columns: auto auto 1fr;
                gap: 10px 14px;
              }

              .order-user-info {
                grid-template-columns: repeat(2, 1fr);
                gap: 8px 14px;
              }
            }
          }
        }
      }
    }

    .overview-section {
      .stat-card {
        min-width: 140px;
        padding: 16px 18px;

        .stat-value {
          font-size: 22px !important;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .merchant-orders-container {
    padding: 12px;

    .orders-header {
      flex-direction: column;
      gap: 12px;
      align-items: stretch;
    }

    .overview-section {
      gap: 10px;

      .stat-card {
        min-width: calc(50% - 5px);
        padding: 16px;

        .stat-value {
          font-size: 1.429rem /* 原值: 20px */ !important;
        }

        .stat-icon {
          width: 44px;
          height: 44px;

          svg {
            width: 24px;
            height: 24px;
          }
        }
      }
    }

    .filter-section {
      flex-direction: column;
      gap: 16px;
      padding: 16px;

      .filter-left {
        width: 100%;

        .filter-header {
          .filter-label {
            font-size: 1rem /* 原值: 14px */;
          }
        }

        .status-filter-group {
          gap: 4px;

          .custom-status-tag {
            padding: 3px 8px;
            font-size: 0.75rem /* 原值: 11px */;

            .tag-icon {
              font-size: 0.75rem /* 原值: 11px */;
            }

            .tag-text {
              font-size: 0.75rem /* 原值: 11px */;
            }

            .close-icon {
              font-size: 9px;
            }
          }
        }
      }

      .filter-right {
        width: 100%;

        :deep(.search-input) {
          width: 100% !important;
        }
      }
    }

    .quick-actions {
      flex-direction: column;
      gap: 12px;
      padding: 14px;

      .quick-actions-left {
        width: 100%;
        justify-content: center;
      }

      .quick-actions-right {
        width: 100%;
        text-align: center;

        .order-count-info {
          display: inline-block;
        }
      }
    }

    .orders-list-section {
      .order-item {
        .order-main {
          flex-direction: column;
          gap: 12px;
          padding: 14px;

          .order-content {
            flex-direction: column;
            gap: 12px;
          }

          .order-left {
            width: 100%;

            .order-basic-info {
              grid-template-columns: 1fr;
              gap: 8px;

              .order-no {
                font-size: 1rem /* 原值: 14px */;
              }

              .order-amount {
                font-size: 1.071rem /* 原值: 15px */;
              }

              .order-time {
                justify-self: start;
              }
            }

            .order-user-info {
              grid-template-columns: 1fr;
              gap: 8px;

              > div {
                font-size: 0.929rem /* 原值: 13px */;
              }
            }

            .order-items {
              flex-direction: column;
              gap: 8px;

              .items-label {
                font-size: 0.857rem /* 原值: 12px */;
              }
            }

            .order-remark {
              flex-direction: row;
              padding: 8px 12px;

              .remark-icon {
                font-size: 1rem /* 原值: 14px */;
              }

              .remark-content {
                font-size: 0.857rem /* 原值: 12px */;

                .remark-label {
                  font-size: 0.857rem /* 原值: 12px */;
                }
              }
            }
          }

          .order-right {
            align-items: flex-start;
            width: 100%;
          }

          .order-actions {
            flex-direction: column;
            width: 100%;
            gap: 10px;

            .primary-actions {
              width: 100%;
              justify-content: center;
            }

            :deep(.el-button) {
              width: 100%;
              justify-content: center;
              padding: 8px 12px;
              font-size: 0.929rem /* 原值: 13px */;

              .el-icon {
                font-size: 1.143rem /* 原值: 16px */;
              }
            }

            .more-dropdown {
              width: 100%;

              :deep(.el-button) {
                width: 100%;
                border-radius: 8px;
                padding: 8px;

                &.more-btn {
                  width: 100%;
                  border-radius: 8px;
                }
              }
            }
          }
        }
      }
    }
  }
}

// 展开的用户详情面板
.user-detail-expanded {
  margin-top: 10px;
  margin-bottom: 10px;
  padding: 12px;
  background: linear-gradient(135deg, @merchant-info-light 0%, lighten(@merchant-info-light, 3%) 100%);
  border-radius: 10px;
  border: 1px solid @merchant-info;
}

// 下拉菜单文字颜色
.dropdown-text-warning {
  color: @merchant-warning;
}

.dropdown-text-danger {
  color: @merchant-error;
}
</style>
