<script setup>
import { ref, computed, onMounted } from 'vue'
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
  Reading,
  Filter,
  List,
  Calendar,
  Timer,
  Coin
} from '@element-plus/icons-vue'
import api from '../../utils/api.js'
import { useRouter, useRoute } from 'vue-router'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import { useAuthStore } from '../../store/authStore'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const loading = ref(false)

// 获取商家ID
const merchantId = authStore.merchantId || localStorage.getItem('auth_merchantId')

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

// 状态筛选映射
const statusFilterMap = {
  all: { text: '全部', value: 'all' },
  0: { text: '待支付', value: 0 },
  1: { text: '待接单', value: 1 },
  2: { text: '备菜中', value: 2 },
  3: { text: '烹饪中', value: 3 },
  4: { text: '待上菜', value: 4 },
  5: { text: '已送达', value: 5 },
  6: { text: '已取消', value: 6 },
  7: { text: '待评价', value: 7 },
  8: { text: '已评价', value: 8 }
}

// 订单数据
const orders = ref([])

// 当前选中的状态筛选
const activeStatusFilter = ref('all')

// 搜索关键词
const searchKeyword = ref('')

// 排序方式
const sortOrder = ref('time-desc')

// 排序选项
const sortOptions = [
  { label: '时间降序', value: 'time-desc', icon: 'Timer' },
  { label: '时间升序', value: 'time-asc', icon: 'Timer' },
  { label: '金额降序', value: 'amount-desc', icon: 'Coin' },
  { label: '金额升序', value: 'amount-asc', icon: 'Coin' }
]

// 数字动画
const animatedValues = ref({
  total: 0,
  totalAmount: 0,
  pendingCount: 0,
  preparingCount: 0,
  completedCount: 0,
  cancelledCount: 0
})

// 动画数字
const animateValue = (key, endValue, duration = 1000) => {
  const startValue = animatedValues.value[key]
  const startTime = performance.now()

  const animate = (currentTime) => {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
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

// 获取筛选状态列表（支持多状态筛选，如"2-4"表示状态2、3、4）
const getStatusFilters = (statusFilter) => {
  if (statusFilter === 'all' || statusFilter === '') return []

  // 处理特殊状态 "2-4"（进行中 = 备菜+烹饪+待上菜）
  if (statusFilter === '2-4' || statusFilter === '2-3') {
    return [2, 3, 4] // 备菜中、烹饪中、待上菜
  }

  // 单个状态
  return [parseInt(statusFilter)]
}

// 筛选后的订单
const filteredOrders = computed(() => {
  return orders.value
    .filter((order) => {
      // 状态筛选（支持多状态）
      const statusFilters = getStatusFilters(activeStatusFilter.value)
      const statusMatch =
        activeStatusFilter.value === 'all' ||
        activeStatusFilter.value === '' ||
        statusFilters.includes(order.status)

      // 搜索筛选（按订单ID或地址搜索）
      const searchMatch =
        !searchKeyword.value ||
        (order.id && order.id.toLowerCase().includes(searchKeyword.value.toLowerCase())) ||
        (order.address && order.address.toLowerCase().includes(searchKeyword.value.toLowerCase()))

      return statusMatch && searchMatch
    })
    .sort((a, b) => {
      // 根据选择的排序方式排序
      switch (sortOrder.value) {
        case 'time-desc':
          return new Date(b.createTime) - new Date(a.createTime)
        case 'time-asc':
          return new Date(a.createTime) - new Date(b.createTime)
        case 'amount-desc':
          return (b.totalAmount || 0) - (a.totalAmount || 0)
        case 'amount-asc':
          return (a.totalAmount || 0) - (b.totalAmount || 0)
        default:
          return new Date(b.createTime) - new Date(a.createTime)
      }
    })
})

// 订单概览统计
const orderOverview = computed(() => {
  const total = filteredOrders.value.length
  const totalAmount = filteredOrders.value.reduce((sum, order) => sum + (order.totalAmount || 0), 0)
  const pendingCount = filteredOrders.value.filter((order) => order.status === 1).length
  const preparingCount = filteredOrders.value.filter(
    (order) => order.status === 2
  ).length
  const completedCount = filteredOrders.value.filter((order) => order.status === 3).length
  const cancelledCount = filteredOrders.value.filter((order) => order.status === 4).length

  return {
    total,
    totalAmount,
    pendingCount,
    preparingCount,
    completedCount,
    cancelledCount
  }
})

// 监听订单概览变化，触发动画
import { watch } from 'vue'
watch(
  orderOverview,
  (newVal) => {
    animateValue('total', newVal.total)
    animateValue('totalAmount', newVal.totalAmount)
    animateValue('pendingCount', newVal.pendingCount)
    animateValue('preparingCount', newVal.preparingCount)
    animateValue('completedCount', newVal.completedCount)
    animateValue('cancelledCount', newVal.cancelledCount)
  },
  { deep: true }
)

// 查看订单详情
const viewOrderDetails = (order) => {
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
        // 使用取消订单API来拒绝订单，传递拒绝原因
        const response = await api.put(`/v1/orders/${order.id}/cancel?reason=${encodeURIComponent(reason)}`)
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
        // 商家取消订单时传递明确的取消原因
        const response = await api.put(`/v1/orders/${order.id}/cancel?reason=${encodeURIComponent('商家取消订单')}`)
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

// 获取标签类型
const getTagType = (status) => {
  if (status === 'all') return 'primary'
  return orderStatusMap[status]?.type || 'info'
}

// 获取状态的订单数量
const getStatusCount = (status) => {
  if (status === 'all') return orders.value.length
  if (status === '2-4' || status === '2-3') {
    return orders.value.filter((o) => o.status === 2 || o.status === 3 || o.status === 4).length
  }
  return orders.value.filter((o) => o.status === status).length
}

// 获取状态标签文本
const getStatusLabel = (status) => {
  if (status === 'all') return '全部'
  if (status === '2-4' || status === '2-3') return '进行中'
  return statusFilterMap[status]?.text || '未知'
}

// 页面标题（根据当前筛选状态动态显示）
const pageTitle = computed(() => {
  if (activeStatusFilter.value === '' || activeStatusFilter.value === 'all') {
    return '全部订单'
  }
  return getStatusLabel(activeStatusFilter.value) + '订单'
})

// 获取空状态描述
const getEmptyDescription = () => {
  if (searchKeyword.value) {
    return '未找到匹配的订单'
  }
  if (activeStatusFilter.value === '2-4' || activeStatusFilter.value === '2-3') {
    return '暂无进行中订单'
  }
  if (activeStatusFilter.value === 0) {
    return '暂无待支付订单'
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
  if (activeStatusFilter.value === 6) {
    return '暂无已取消订单'
  }
  return '暂无订单'
}

// 刷新订单数据
const refreshOrders = async () => {
  await fetchOrders()
}

// 获取订单数据
const fetchOrders = async () => {
  console.log('[Orders] 开始获取订单列表')
  console.log('[Orders] 商家ID:', merchantId)

  if (!merchantId) {
    console.error('[Orders] 商家ID为空')
    ElMessage.warning('未找到商家信息，请重新登录')
    return
  }

  loading.value = true
  try {
    const apiUrl = `/v1/orders/merchant/${merchantId}?today=false`
    console.log('[Orders] 请求API:', apiUrl)

    const response = await api.get(apiUrl)
    console.log('[Orders] API响应:', response)
    console.log('[Orders] success字段:', response.success)
    console.log('[Orders] message字段:', response.message)

    if (response.success) {
      const ordersData = response.data || []
      console.log('[Orders] 订单数量:', ordersData.length)

      // 为每个订单获取菜品列表
      for (const order of ordersData) {
        try {
          const dishesResponse = await api.get(`/v1/orders/${order.id}/dishes`)
          console.log('[Orders] 订单菜品API响应:', order.id, dishesResponse)
          console.log('[Orders] 订单菜品数据:', JSON.stringify(dishesResponse.data, null, 2))
          if (dishesResponse.success) {
            order.orderDishes = dishesResponse.data || []
          }
        } catch (error) {
          console.error('[Orders] 获取订单菜品失败:', order.id, error)
          order.orderDishes = []
        }
      }

      console.log('[Orders] 订单数据:', ordersData)
      orders.value = ordersData
    } else {
      console.error('[Orders] API返回失败:', response)
      ElMessage.error(response.message || '获取订单列表失败')
    }
  } catch (error) {
    console.error('[Orders] 加载订单异常:', error)
    console.error('[Orders] 错误详情:', {
      message: error.message,
      status: error.status,
      data: error.data
    })
    ElMessage.error('加载订单失败')
    orders.value = []
  } finally {
    loading.value = false
    console.log('[Orders] 获取订单完成，loading设置为false')
  }
}

// 页面加载时初始化
onMounted(() => {
  // 从URL参数读取状态并设置筛选
  const statusParam = route.query.status
  if (statusParam) {
    activeStatusFilter.value = statusParam
  }

  // 从URL参数读取搜索关键词
  const searchParam = route.query.search
  if (searchParam) {
    searchKeyword.value = searchParam
  }

  // 从API获取订单数据
  fetchOrders()
})
</script>

<template>
  <div class="merchant-orders-container">
    <!-- 头部 -->
    <div class="orders-header">
      <div class="header-left">
        <h3 class="page-title">{{ pageTitle }}</h3>
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
            <el-icon v-else-if="status === 2" class="tag-icon"><Goods /></el-icon>
            <el-icon v-else-if="status === 3" class="tag-icon"><CircleCheckFilled /></el-icon>
            <el-icon v-else-if="status === 4" class="tag-icon"><CircleClose /></el-icon>

            <span class="tag-text">
              {{ status === 'all' ? '全部'
                : status === 0 ? '待支付'
                : orderStatusMap[status].text }}
              <template v-if="status !== 'all'">({{ getStatusCount(status) }})</template>
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
        <el-select
          v-model="sortOrder"
          placeholder="排序方式"
          class="sort-select"
          popper-class="sort-select-popper"
        >
          <el-option
            v-for="option in sortOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          >
            <div class="sort-option">
              <el-icon><component :is="option.icon" /></el-icon>
              <span>{{ option.label }}</span>
            </div>
          </el-option>
        </el-select>
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
      <div
        v-for="(order, index) in filteredOrders"
        :key="order.id"
        :class="['order-item', `status-${order.status}`]"
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
                  <span class="time-value">{{ order.createTime || '--' }}</span>
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
                    </div>
                    <div class="dish-price">
                      ¥{{ ((dish.price || 0) * (dish.quantity || 0)).toFixed(2) }}
                    </div>
                  </div>
                </div>
              </div>

              <div class="order-user-info">
                <div class="user-address" style="grid-column: 1 / -1">
                  <span class="info-label">📍 地址</span>
                  <span class="info-value">{{ order.address || '--' }}</span>
                </div>
              </div>
            </div>

            <div class="order-right">
              <div class="order-status">
                <el-tag :type="orderStatusMap[order.status]?.type || 'info'" size="large">
                  {{ orderStatusMap[order.status]?.text || '未知' }}
                </el-tag>
              </div>
            </div>
          </div>

          <div class="order-divider"></div>

          <div class="order-actions">
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

            <el-dropdown trigger="click" class="more-dropdown">
              <el-button type="info" size="small" plain class="more-btn">
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-if="order.status === 0 || order.status === 1"
                    divided
                    @click="cancelOrder(order)"
                  >
                    <el-icon><CircleClose /></el-icon>
                    <span style="color: #e6a23c">取消订单</span>
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="deleteOrder(order)">
                    <el-icon><Delete /></el-icon>
                    <span style="color: #f56c6c">删除订单</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>

      <!-- 空数据提示 -->
      <div v-if="filteredOrders.length === 0" class="empty-orders">
        <el-empty :description="getEmptyDescription()"></el-empty>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
// 复用今日订单的样式
.merchant-orders-container {
  padding: 0 20px 20px 20px;

  .orders-header {
    margin-bottom: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    background: linear-gradient(135deg, #2196f3 0%, #1976d2 100%);
    border-radius: 16px;
    box-shadow: 0 4px 16px rgba(33, 150, 243, 0.3);

    .header-left {
      display: flex;
      flex-direction: column;
      gap: 8px;

      .page-title {
        font-size: 1.714rem /* 原值: 24px */;
        font-weight: 700;
        margin: 0;
        color: #ffffff;
        letter-spacing: 0.5px;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }

      .page-subtitle {
        font-size: 1rem /* 原值: 14px */;
        color: rgba(255, 255, 255, 0.9);
      }
    }

    .header-right {
      display: flex;
      gap: 8px;
      align-items: center;

      :deep(.el-button) {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 4px;
        padding: 8px 14px;
        font-size: 0.929rem /* 原值: 13px */;
        font-weight: 500;
        border-radius: 6px;
        height: 34px;
        backdrop-filter: blur(10px);
        border: 1px solid rgba(255, 255, 255, 0.3);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

        .el-icon {
          font-size: 1rem /* 原值: 14px */;
        }

        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }

        &:active {
          transform: translateY(0);
        }
      }
    }
  }

  .overview-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24px;
    background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
    border: 1px solid #d1d5db;
    border-radius: 16px;
    margin-bottom: 24px;
    flex-wrap: wrap;
    gap: 16px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);

    .stat-card {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 20px 24px;
      background: #ffffff;
      border-radius: 16px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      cursor: pointer;
      flex: 1;
      min-width: 160px;
      border: 1px solid rgba(209, 213, 219, 0.3);

      &:hover {
        transform: translateY(-8px) scale(1.02);
        box-shadow: 0 12px 28px rgba(0, 0, 0, 0.15);
      }

      .stat-icon {
        width: 52px;
        height: 52px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        border-radius: 14px;

        svg {
          width: 28px;
          height: 28px;
        }
      }

      .stat-content {
        flex: 1;

        .stat-value {
          font-size: 26px;
          font-weight: 700;
          color: #374151;
          line-height: 1.2;
        }

        .stat-label {
          font-size: 0.929rem /* 原值: 13px */;
          color: #6b7280;
          margin-top: 6px;
          font-weight: 500;
        }
      }

      &.total {
        border-left: 4px solid #667eea;
        .stat-icon {
          background: linear-gradient(
            135deg,
            rgba(102, 126, 234, 0.15) 0%,
            rgba(102, 126, 234, 0.08) 100%
          );
          color: #667eea;
        }
        .stat-value {
          color: #667eea;
        }
      }

      &.amount {
        border-left: 4px solid #f56c6c;
        .stat-icon {
          background: linear-gradient(
            135deg,
            rgba(245, 108, 108, 0.15) 0%,
            rgba(245, 108, 108, 0.08) 100%
          );
          color: #f56c6c;
        }
        .stat-value {
          color: #d32f2f;
        }
        &.highlight .stat-value {
          font-size: 2rem /* 原值: 28px */;
        }
      }

      &.pending {
        border-left: 4px solid #ff9800;
        .stat-icon {
          background: linear-gradient(
            135deg,
            rgba(255, 152, 0, 0.15) 0%,
            rgba(255, 152, 0, 0.08) 100%
          );
          color: #ff9800;
        }
        .stat-value {
          color: #f57c00;
        }
      }

      &.preparing {
        border-left: 4px solid #ff9800;
        .stat-icon {
          background: linear-gradient(
            135deg,
            rgba(255, 152, 0, 0.15) 0%,
            rgba(255, 152, 0, 0.08) 100%
          );
          color: #ff9800;
        }
        .stat-value {
          color: #f57c00;
        }
      }

      &.completed {
        border-left: 4px solid #4caf50;
        .stat-icon {
          background: linear-gradient(
            135deg,
            rgba(76, 175, 80, 0.15) 0%,
            rgba(76, 175, 80, 0.08) 100%
          );
          color: #4caf50;
        }
        .stat-value {
          color: #388e3c;
        }
      }
    }
  }

  .filter-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 20px;
    background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
    border-radius: 10px;
    margin-bottom: 16px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    border: 1px solid #e8eef5;

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
          font-size: 1.071rem /* 原值: 15px */;
          color: #667eea;
        }

        .filter-label {
          font-size: 0.929rem /* 原值: 13px */;
          font-weight: 600;
          color: #303133;
        }
      }

      .status-filter-group {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        align-items: center;

        .custom-status-tag {
          // 默认未选中状态：透明/灰色
          background: transparent;
          color: #909399;
          border: 1px solid #dcdfe6;

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

          .tag-icon {
            font-size: 0.857rem /* 原值: 12px */;
            opacity: 0.6;
          }

          .tag-text {
            font-size: 0.857rem;
          }

          // hover 状态：轻微提示可点击
          &:hover {
            background: #f5f7fa;
            border-color: #409eff;
            color: #409eff;

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
            background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
            color: #ffffff;
            border-color: #1890ff;
            box-shadow: 0 2px 8px rgba(24, 144, 255, 0.4);

            .tag-icon {
              opacity: 1;
            }

            &:hover {
              background: linear-gradient(135deg, #40a9ff 0%, #69c0ff 100%);
            }
          }

          // 待支付选中 - 灰色
          &.status-tag-0.active {
            background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%);
            color: #ffffff;
            border-color: #909399;
            box-shadow: 0 2px 8px rgba(144, 147, 153, 0.3);

            .tag-icon {
              opacity: 1;
            }
          }

          // 待接单选中 - 红色
          &.status-tag-1.active {
            background: linear-gradient(135deg, #f56c6c 0%, #ff7875 100%);
            color: #ffffff;
            border-color: #f56c6c;
            box-shadow: 0 2px 8px rgba(245, 108, 108, 0.4);

            .tag-icon {
              opacity: 1;
            }
          }

          // 备菜中选中
          &.status-tag-2.active {
            background: linear-gradient(135deg, #e6a23c 0%, #f0a858 100%);
            color: #ffffff;
            border-color: #e6a23c;
            box-shadow: 0 2px 8px rgba(230, 162, 60, 0.4);

            .tag-icon {
              opacity: 1;
            }
          }

          // 进行中选中（合并状态）
          &.status-tag-2-4.active,
          &.status-tag-2-3.active {
            background: linear-gradient(135deg, #e6a23c 0%, #f0a858 100%);
            color: #ffffff;
            border-color: #e6a23c;
            box-shadow: 0 2px 8px rgba(230, 162, 60, 0.4);

            .tag-icon {
              opacity: 1;
            }
          }

          // 烹饪中选中
          &.status-tag-3.active {
            background: linear-gradient(135deg, #ff9800 0%, #ffa726 100%);
            color: #ffffff;
            border-color: #ff9800;
            box-shadow: 0 2px 8px rgba(255, 152, 0, 0.4);

            .tag-icon {
              opacity: 1;
            }
          }

          // 待上菜选中
          &.status-tag-4.active {
            background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
            color: #ffffff;
            border-color: #409eff;
            box-shadow: 0 2px 8px rgba(64, 158, 255, 0.4);

            .tag-icon {
              opacity: 1;
            }
          }

          // 已完成选中 - 绿色
          &.status-tag-3.active {
            background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
            color: #ffffff;
            border-color: #67c23a;
            box-shadow: 0 2px 8px rgba(103, 194, 58, 0.4);

            .tag-icon {
              opacity: 1;
            }
          }
        }
      }
    }

    .filter-right {
      flex-shrink: 0;
      display: flex;
      flex-direction: column;
      gap: 12px;
      align-items: stretch;

      :deep(.search-input) {
        width: 320px;

        .el-input__wrapper {
          border-radius: 10px;
          padding: 8px 12px;
          box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);

          &:hover {
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
          }

          &.is-focus {
            box-shadow:
              0 0 0 2px rgba(102, 126, 234, 0.2),
              0 2px 8px rgba(102, 126, 234, 0.3);
          }
        }
      }

      :deep(.sort-select) {
        width: 320px;

        .el-input__wrapper {
          border-radius: 10px;
          padding: 8px 12px;
          box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);

          &:hover {
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
          }

          &.is-focus {
            box-shadow:
              0 0 0 2px rgba(102, 126, 234, 0.2),
              0 2px 8px rgba(102, 126, 234, 0.3);
          }
        }
      }
    }
  }

  .quick-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 20px;
    background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
    border-radius: 10px;
    margin-bottom: 16px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    border: 1px solid #e8eef5;

    .quick-actions-left {
      display: flex;
      gap: 12px;

      .quick-action-btn {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 6px;
        padding: 8px 16px;
        border-radius: 6px;
        font-size: 0.929rem /* 原值: 13px */;
        font-weight: 500;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
        height: 34px;

        .el-icon {
          font-size: 1rem /* 原值: 14px */;
        }

        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 3px 10px rgba(0, 0, 0, 0.12);
        }

        &:active {
          transform: translateY(0);
        }
      }
    }

    .quick-actions-right {
      .order-count-info {
        font-size: 0.929rem /* 原值: 13px */;
        color: #606266;
        padding: 8px 14px;
        background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
        border-radius: 6px;
        border: 1px solid #bae6fd;
        font-weight: 500;

        strong {
          color: #667eea;
          font-size: 1.071rem /* 原值: 15px */;
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
      background-color: #fff;
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
        border: 2px solid #e2e8f0;
        border-radius: 12px;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
        position: relative;
        overflow: visible;
        gap: 16px;

        .order-content {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          gap: 24px;
        }

        .order-divider {
          height: 1px;
          background: linear-gradient(
            90deg,
            transparent 0%,
            #e2e8f0 10%,
            #e2e8f0 90%,
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
          background: #e2e8f0;
          transition: all 0.3s ease;
          border-radius: 12px 0 0 12px;
        }

        &:hover {
          transform: translateY(-4px) scale(1.01);
          box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
          border-color: #94a3b8;
        }

        &:active {
          transform: translateY(-2px) scale(0.998);
        }
      }

      // 状态边框颜色
      &.status-0 .order-main::before {
        background: linear-gradient(180deg, #909399 0%, #a6a9ad 100%);
      }

      &.status-1 .order-main::before {
        background: linear-gradient(180deg, #f56c6c 0%, #ff7875 100%);
        box-shadow: 0 0 8px rgba(245, 108, 108, 0.3);
      }

      &.status-2 .order-main::before {
        background: linear-gradient(180deg, #e6a23c 0%, #f0a858 100%);
        box-shadow: 0 0 8px rgba(230, 162, 60, 0.3);
      }

      &.status-3 .order-main::before {
        background: linear-gradient(180deg, #ff9800 0%, #ffa726 100%);
        box-shadow: 0 0 8px rgba(255, 152, 0, 0.3);
      }

      &.status-4 .order-main::before {
        background: linear-gradient(180deg, #409eff 0%, #66b1ff 100%);
        box-shadow: 0 0 8px rgba(64, 158, 255, 0.3);
      }

      &.status-3 .order-main::before {
        background: linear-gradient(180deg, #67c23a 0%, #7bcf58 100%);
        box-shadow: 0 0 8px rgba(103, 194, 58, 0.3);
      }

      &.status-4 .order-main::before {
        background: linear-gradient(180deg, #c0c4cc 0%, #d3d4d6 100%);
      }

      // 未读订单样式
      &.unread-order .order-main {
        background: linear-gradient(to right, #fef2f2 0%, #ffffff 40%);
        border-color: #fee2e2;
      }

      .order-left {
        flex: 1;
        min-width: 0;

        .order-basic-info {
          display: grid;
          grid-template-columns: 1fr auto auto;
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
            min-width: 0;
            overflow: hidden;

            .no-label {
              color: #909399;
              font-size: 0.857rem /* 原值: 12px */;
              font-weight: 500;
              margin-right: 4px;
              flex-shrink: 0;
            }

            .no-value {
              color: #303133;
              font-family: 'Consolas', 'Monaco', monospace;
              font-weight: 600;
              overflow: hidden;
              text-overflow: ellipsis;
            }
          }

          .order-amount {
            display: flex;
            align-items: center;
            gap: 6px;
            font-weight: 700;
            font-size: 17px;
            color: #f56c6c;
            white-space: nowrap;
            flex-shrink: 0;

            .amount-label {
              font-size: 0.857rem /* 原值: 12px */;
              color: #909399;
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
            color: #606266;
            font-size: 0.929rem /* 原值: 13px */;
            white-space: nowrap;
            flex-shrink: 0;
            justify-self: end;

            .time-label {
              color: #909399;
              font-size: 0.857rem /* 原值: 12px */;
              font-weight: 500;
            }

            .time-value {
              font-weight: 500;
            }
          }
        }
        // 菜品列表样式
        .order-dishes-list {
          margin-top: 12px;
          padding: 12px 16px;
          background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
          border-radius: 10px;
          border: 1px solid #e8eef5;
          box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);

          .dishes-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 10px;
            padding-bottom: 8px;
            border-bottom: 1px dashed #e2e8f0;

            .dishes-title {
              font-size: 0.929rem /* 原值: 13px */;
              font-weight: 600;
              color: #303133;
            }

            .dishes-count {
              font-size: 0.857rem /* 原值: 12px */;
              color: #909399;
              background: #f0f2f5;
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
              background: #ffffff;
              border-radius: 8px;
              border: 1px solid #f0f0f0;
              transition: all 0.2s ease;

              &:hover {
                border-color: #667eea;
                box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
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
                  color: #303133;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                }

                .dish-quantity {
                  font-size: 0.929rem /* 原值: 13px */;
                  color: #667eea;
                  font-weight: 600;
                  flex-shrink: 0;
                  background: linear-gradient(
                    135deg,
                    rgba(102, 126, 234, 0.1) 0%,
                    rgba(102, 126, 234, 0.05) 100%
                  );
                  padding: 2px 8px;
                  border-radius: 12px;
                }
              }

              .dish-price {
                font-size: 1rem /* 原值: 14px */;
                font-weight: 600;
                color: #f56c6c;
                font-family: 'Consolas', 'Monaco', monospace;
                flex-shrink: 0;
              }
            }
          }
        }

        .order-user-info {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
          gap: 10px 20px;
          margin-top: 16px;
          font-size: 1rem /* 原值: 14px */;
          color: #606266;

          > div {
            display: flex;
            align-items: center;
            gap: 8px;
            min-width: 0;

            .info-label {
              font-size: 0.857rem /* 原值: 12px */;
              color: #909399;
              font-weight: 500;
              flex-shrink: 0;
              min-width: 36px;
            }

            .info-value {
              font-weight: 500;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
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
        padding-left: 8px;

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
        justify-content: flex-end;
        align-items: center;
        gap: 8px;
        width: 100%;
        padding-top: 12px;
        flex-wrap: wrap;

        .primary-actions {
          display: flex;
          gap: 8px;
          align-items: center;
          flex-wrap: wrap;
        }

        :deep(.el-button) {
          display: inline-flex;
          align-items: center;
          justify-content: center;
          gap: 4px;
          padding: 6px 14px;
          font-size: 0.929rem /* 原值: 13px */;
          font-weight: 500;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          border-radius: 6px;
          box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
          height: 32px;
          white-space: nowrap;

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
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border: none;
            color: white;

            &:hover {
              background: linear-gradient(135deg, #5568d3 0%, #653a8b 100%);
            }
          }

          &.action-btn {
            min-width: 70px;
            font-weight: 500;
          }

          &.complete-btn {
            background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
            border: none;
            color: white;

            &:hover {
              background: linear-gradient(135deg, #0e837a 0%, #2ed16b 100%);
              box-shadow: 0 3px 10px rgba(56, 239, 125, 0.3);
            }
          }

          &:disabled {
            opacity: 0.5;
            cursor: not-allowed;
            background: #f5f5f5;
            border-color: #d9d9d9;
            color: #999;

            &:hover {
              transform: none;
              box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
            }
          }
        }

        .more-dropdown {
          flex-shrink: 0;

          :deep(.el-button) {
            &.more-btn {
              width: 32px;
              height: 32px;
              padding: 0;
              display: inline-flex;
              align-items: center;
              justify-content: center;
              border-radius: 6px;
            }
          }
        }

        :deep(.el-dropdown-menu) {
          .el-dropdown-menu__item {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 8px 16px;
            font-size: 0.929rem /* 原值: 13px */;

            .el-icon {
              font-size: 1.143rem /* 原值: 16px */;
            }

            &:hover {
              background: linear-gradient(135deg, #f5f7fa 0%, #e8eef5 100%);
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

// 响应式设计
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
      }
    }

    .filter-section {
      flex-direction: column;
      gap: 16px;
      padding: 16px;

      .filter-right {
        width: 100%;
        flex-direction: column;
        align-items: stretch;

        :deep(.sort-select),
        :deep(.search-input) {
          width: 100% !important;
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
            }

            .order-user-info {
              grid-template-columns: 1fr;
              gap: 8px;
            }
          }

          .order-right {
            align-items: flex-start;
            width: 100%;
          }

          .order-actions {
            flex-direction: row;
            width: 100%;
            gap: 6px;
            padding-top: 12px;
            justify-content: space-between;

            .primary-actions {
              flex: 1;
              justify-content: flex-start;
              gap: 6px;
            }

            :deep(.el-button) {
              flex: 1;
              min-width: 0;
              padding: 6px 8px;
              font-size: 0.857rem /* 原值: 12px */;
              height: 32px;

              span {
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
              }
            }

            .more-dropdown {
              :deep(.el-button) {
                width: 32px;
                flex: none;
              }
            }
          }
        }
      }
    }
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

// 概览卡片淡入动画
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

// 为概览卡片添加动画
.overview-section {
  animation: fadeIn 0.5s ease-out;
}

// 排序选项样式
.sort-option {
  display: flex;
  align-items: center;
  gap: 8px;

  .el-icon {
    font-size: 1rem;
    color: #667eea;
  }

  span {
    font-size: 0.929rem;
  }
}

// 排序下拉选择器弹窗样式
:deep(.sort-select-popper) {
  .el-select-dropdown__item {
    padding: 8px 12px;

    &.selected {
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 131, 248, 0.1) 100%);
      color: #667eea;
      font-weight: 500;
    }

    &:hover {
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 131, 248, 0.08) 100%);
    }
  }
}
</style>
