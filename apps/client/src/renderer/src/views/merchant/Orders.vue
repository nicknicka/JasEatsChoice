<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  View,
  CircleCheck,
  CircleCheckFilled,
  Dish,
  Select,
  MoreFilled,
  CircleClose,
  Delete,
  Search,
  Refresh,
  List,
  Timer,
  Coin
} from '@element-plus/icons-vue'
import api from '../../utils/api.js'
import { useRouter, useRoute } from 'vue-router'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import AppStatCard from '../../components/common/AppStatCard.vue'
import AppCard from '../../components/common/AppCard.vue'
import AppEmptyState from '../../components/common/AppEmptyState.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import { useAuthStore } from '../../store/authStore'
import { normalizeOrderStatusCode } from '../../utils/orderStatus'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const loading = ref(false)

// 获取商家ID
const merchantId = authStore.merchantId || localStorage.getItem('auth_merchantId')

// 订单状态映射（对应后端状态码）
// 0-待支付、1-待接单、2-制作中、3-已完成、4-已取消
// type 按 DESIGN-BASELINE §3.1 状态表：待处理=warning、进行中=info、已完成=success、已取消=danger
const orderStatusMap = {
  0: { text: '待支付', type: 'warning' },
  1: { text: '待接单', type: 'warning' },
  2: { text: '制作中', type: 'info' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'danger' }
}

// StatusTag 词典键（§3.1、§4.6）
const statusTagKeyMap = {
  0: 'pendingpayment',
  1: 'pendingaccept',
  2: 'preparing',
  3: 'completed',
  4: 'cancelled'
}

const getStatusTagKey = (status) => statusTagKeyMap[status] || 'unknown'

// 状态筛选映射
const statusFilterMap = {
  all: { text: '全部', value: 'all' },
  0: { text: '待支付', value: 0 },
  1: { text: '待接单', value: 1 },
  2: { text: '制作中', value: 2 },
  3: { text: '已完成', value: 3 },
  4: { text: '已取消', value: 4 }
}

// 状态筛选顺序
const filterStatuses = ['all', 0, 1, 2, 3, 4]

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

// 统计数值 count-up：时长取 §3.5 --duration-num，减少动态偏好下直接落终值
const prefersReducedMotion =
  typeof window !== 'undefined' &&
  typeof window.matchMedia === 'function' &&
  window.matchMedia('(prefers-reduced-motion: reduce)').matches

const countUpDuration = () => {
  const raw = getComputedStyle(document.documentElement).getPropertyValue('--duration-num').trim()
  const value = parseFloat(raw)
  if (!Number.isFinite(value)) return 0
  return raw.includes('ms') ? value : value * 1000
}

// 动画数字
const animateValue = (key, endValue) => {
  const duration = countUpDuration()
  const startValue = animatedValues.value[key]
  if (prefersReducedMotion || duration <= 0) {
    animatedValues.value[key] = endValue
    return
  }
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

// 获取筛选状态列表
const getStatusFilters = (statusFilter) => {
  if (statusFilter === 'all' || statusFilter === '') return []
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
          return new Date(b.createdAt) - new Date(a.createdAt)
        case 'time-asc':
          return new Date(a.createdAt) - new Date(b.createdAt)
        case 'amount-desc':
          return (b.totalAmount || 0) - (a.totalAmount || 0)
        case 'amount-asc':
          return (a.totalAmount || 0) - (b.totalAmount || 0)
        default:
          return new Date(b.createdAt) - new Date(a.createdAt)
      }
    })
})

// 订单概览统计
const orderOverview = computed(() => {
  const activeOrders = filteredOrders.value.filter((order) => order.status !== 4)
  const total = activeOrders.length
  const totalAmount = activeOrders.reduce((sum, order) => sum + (order.totalAmount || 0), 0)
  const pendingCount = filteredOrders.value.filter((order) => order.status === 1).length
  const preparingCount = filteredOrders.value.filter((order) => order.status === 2).length
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
    const response = await api.put(`/v1/orders/${order.id}/status`, { status: newStatus })
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
        const response = await api.put(`/v1/orders/${order.id}/cancel`, null, {
          params: { reason }
        })
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
        const response = await api.put(
          `/v1/orders/${order.id}/cancel?reason=${encodeURIComponent('商家取消订单')}`
        )
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
const deleteOrder = async (order) => {
  try {
    await ElMessageBox.confirm('删除后订单将无法恢复，确定要删除吗？', '删除订单确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error',
      distinguishCancelAndClose: true
    })
  } catch {
    return
  }

  try {
    const response = await api.delete(`/v1/orders/${encodeURIComponent(order.id)}`, {
      params: { merchantId }
    })
    if (!response.success) {
      ElMessage.error(response.message || '删除订单失败')
      return
    }

    const refreshed = await fetchOrders()
    if (!refreshed) return
    if (orders.value.some((item) => item.id === order.id)) {
      ElMessage.error('删除后订单仍然存在，请重试')
      return
    }
    ElMessage.success('订单已删除')
  } catch (error) {
    console.error('删除订单失败:', error)
    ElMessage.error(error.message || '删除订单失败')
  }
}

// 获取状态的订单数量
const getStatusCount = (status) => {
  if (status === 'all') return orders.value.length
  return orders.value.filter((o) => o.status === status).length
}

// 获取状态标签文本
const getStatusLabel = (status) => {
  if (status === 'all') return '全部'
  return statusFilterMap[status]?.text || '未知'
}

// 页面标题（根据当前筛选状态动态显示）
const pageTitle = computed(() => {
  if (activeStatusFilter.value === '' || activeStatusFilter.value === 'all') {
    return '全部订单'
  }
  return getStatusLabel(activeStatusFilter.value) + '订单'
})

// 是否处于筛选/搜索态（决定空状态的替代动作）
const hasActiveQuery = computed(
  () =>
    Boolean(searchKeyword.value) ||
    (activeStatusFilter.value !== 'all' && activeStatusFilter.value !== '')
)

const clearFilters = () => {
  searchKeyword.value = ''
  activeStatusFilter.value = 'all'
}

// 获取空状态描述
const getEmptyDescription = () => {
  if (searchKeyword.value) {
    return '未找到匹配的订单'
  }
  if (activeStatusFilter.value === 0) {
    return '暂无待支付订单'
  }
  if (activeStatusFilter.value === 1) {
    return '暂无待接单订单'
  }
  if (activeStatusFilter.value === 2) {
    return '暂无制作中订单'
  }
  if (activeStatusFilter.value === 3) {
    return '暂无已完成订单'
  }
  if (activeStatusFilter.value === 4) {
    return '暂无已取消订单'
  }
  return '暂无订单'
}

// 时间统一显示 YYYY-MM-DD HH:mm（§4.3、落地检查清单）
const formatOrderTime = (value) => {
  if (!value) return '--'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const pad = (num) => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
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

      // 一次性批量获取所有订单的菜品列表（避免逐单串行 N+1）
      let dishMap = {}
      if (ordersData.length > 0) {
        try {
          const dishesResponse = await api.post('/v1/orders/dishes/batch', {
            orderIds: ordersData.map((order) => order.id)
          })
          console.log('[Orders] 批量订单菜品API响应:', dishesResponse)
          if (dishesResponse.success) {
            dishMap = dishesResponse.data || {}
          }
        } catch (error) {
          console.error('[Orders] 批量获取订单菜品失败:', error)
        }
      }

      console.log('[Orders] 订单数据:', ordersData)
      orders.value = ordersData.map((order) => ({
        ...order,
        orderDishes: dishMap[order.id] || [],
        status: normalizeOrderStatusCode(order.status)
      }))
      return true
    } else {
      console.error('[Orders] API返回失败:', response)
      ElMessage.error(response.message || '获取订单列表失败')
      return false
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
    return false
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
    <!-- 页头：merchant 紧凑版，返回在右侧动作区（§4.7） -->
    <AppPageHeader :title="pageTitle" compact>
      <template #actions>
        <CommonBackButton type="default" />
        <el-button :loading="loading" @click="refreshOrders">
          <el-icon><Refresh /></el-icon>
          <span>刷新</span>
        </el-button>
      </template>
    </AppPageHeader>

    <!-- 概览：5 卡闭合等宽网格，无 4+1 空位（§4.8） -->
    <div class="stat-row">
      <AppStatCard label="总订单" :value="animatedValues.total" :icon="List" />
      <AppStatCard
        label="总金额"
        :value="`¥${animatedValues.totalAmount.toFixed(0)}`"
        :icon="Coin"
      />
      <AppStatCard label="待处理" :value="animatedValues.pendingCount" :icon="Timer" />
      <AppStatCard label="准备中" :value="animatedValues.preparingCount" :icon="Dish" />
      <AppStatCard label="已完成" :value="animatedValues.completedCount" :icon="CircleCheck" />
    </div>

    <!-- 筛选工具条：状态 + 搜索 + 排序收成一行（§3.3、§4.3） -->
    <AppCard class="toolbar-card" padding="md">
      <div class="toolbar">
        <div class="status-filter-group">
          <button
            v-for="status in filterStatuses"
            :key="status"
            type="button"
            class="status-chip"
            :class="{ 'is-active': String(activeStatusFilter) === String(status) }"
            @click="activeStatusFilter = status"
          >
            <span class="status-chip__text">
              {{ status === 'all' ? '全部' : orderStatusMap[status].text }}
            </span>
            <span v-if="status !== 'all'" class="status-chip__count">
              {{ getStatusCount(status) }}
            </span>
          </button>
        </div>

        <div class="toolbar-right">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索订单号/地址"
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

          <span class="order-count-info">
            共 <strong>{{ filteredOrders.length }}</strong> 个订单
          </span>
        </div>
      </div>
    </AppCard>

    <!-- 订单列表 -->
    <div v-loading="loading" class="orders-list-section">
      <div v-for="order in filteredOrders" :key="order.id" class="order-item">
        <div class="order-main">
          <div class="order-content">
            <div class="order-left">
              <div class="order-basic-info">
                <div class="order-no">
                  <span class="no-label">订单号</span>
                  <span class="no-value" :title="order.id">{{ order.id }}</span>
                </div>
                <div class="order-amount">
                  <span class="amount-label">金额</span>
                  <span class="amount-value">¥{{ (order.totalAmount || 0).toFixed(2) }}</span>
                </div>
                <div class="order-time" :title="formatOrderTime(order.createdAt)">
                  <span class="time-label">时间</span>
                  <span class="time-value">{{ formatOrderTime(order.createdAt) }}</span>
                </div>
              </div>

              <!-- 菜品列表 -->
              <div
                v-if="order.orderDishes && order.orderDishes.length > 0"
                class="order-dishes-list"
              >
                <div class="dishes-header">
                  <span class="dishes-title">菜品清单</span>
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
                <div class="user-address">
                  <span class="info-label">地址</span>
                  <span class="info-value" :title="order.address || ''">{{
                    order.address || '--'
                  }}</span>
                </div>
              </div>
            </div>

            <div class="order-status">
              <StatusTag
                :status="getStatusTagKey(order.status)"
                :text="orderStatusMap[order.status]?.text || '未知'"
              />
            </div>
          </div>

          <div class="order-divider"></div>

          <div class="order-actions">
            <div class="primary-actions">
              <el-button class="row-btn" @click="viewOrderDetails(order)">
                <el-icon><View /></el-icon>
                <span>详情</span>
              </el-button>

              <el-button
                v-if="order.status === 1"
                type="primary"
                class="row-btn"
                @click="updateOrderStatus(order, 2)"
              >
                <el-icon><CircleCheck /></el-icon>
                <span>接单</span>
              </el-button>

              <el-button
                v-if="order.status === 2"
                type="primary"
                class="row-btn"
                @click="updateOrderStatus(order, 3)"
              >
                <el-icon><CircleCheckFilled /></el-icon>
                <span>完成订单</span>
              </el-button>

              <el-button v-if="order.status === 3" class="row-btn" disabled>
                <el-icon><Select /></el-icon>
                <span>已完成</span>
              </el-button>
            </div>

            <el-dropdown trigger="click" class="more-dropdown">
              <el-button class="row-btn more-btn">
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="order.status === 1" @click="rejectOrder(order)">
                    <el-icon><CircleClose /></el-icon>
                    <span>拒绝接单</span>
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="order.status === 0 || order.status === 1"
                    divided
                    @click="cancelOrder(order)"
                  >
                    <el-icon><CircleClose /></el-icon>
                    <span>取消订单</span>
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="deleteOrder(order)">
                    <el-icon><Delete /></el-icon>
                    <span class="dropdown-danger">删除订单</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>

      <!-- 空数据提示（§4.5） -->
      <AppEmptyState
        v-if="filteredOrders.length === 0"
        :description="getEmptyDescription()"
        :action-text="hasActiveQuery ? '清除筛选' : '刷新'"
        @action="hasActiveQuery ? clearFilters() : refreshOrders()"
      />
    </div>
  </div>
</template>

<style scoped>
.merchant-orders-container {
  padding: 0 var(--space-6) var(--space-6);
}

/* 页头动作区控件高度取 --control-height-md（§3.4、§4.7） */
.merchant-orders-container :deep(.app-page-header__actions .el-button) {
  height: var(--control-height-md);
}

/* 概览卡：5 个闭合等宽网格，不留 4+1 空位（§4.8） */
.stat-row {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: var(--card-gap-compact);
  margin-bottom: var(--card-gap-compact);
}

/* 筛选工具条：状态 + 搜索 + 排序一行（§3.3、§4.3） */
.toolbar-card {
  margin-bottom: var(--card-gap-compact);
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3) var(--space-4);
}

.status-filter-group {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
}

.status-chip {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  height: var(--control-height-sm);
  padding: 0 var(--space-3);
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  background: var(--surface);
  color: var(--ink-500);
  font-family: inherit;
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  white-space: nowrap;
  cursor: pointer;
  transition:
    background-color var(--duration-fast) var(--ease-out),
    border-color var(--duration-fast) var(--ease-out),
    color var(--duration-fast) var(--ease-out);
}

.status-chip:hover {
  border-color: var(--brand-400);
  color: var(--brand-600);
  background: var(--brand-50);
}

.status-chip:focus-visible {
  outline: 2px solid var(--brand-400);
  outline-offset: 2px;
}

.status-chip.is-active {
  border-color: var(--brand-500);
  background: var(--brand-500);
  color: var(--surface);
}

.status-chip__count {
  font-family: var(--font-num);
  font-variant-numeric: tabular-nums;
}

.toolbar-right {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-3);
}

.search-input,
.sort-select {
  width: 220px;
}

.search-input :deep(.el-input__wrapper),
.sort-select :deep(.el-select__wrapper) {
  height: var(--control-height-sm);
  padding: 0 var(--space-3);
  border-radius: var(--radius-sm);
}

.order-count-info {
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  white-space: nowrap;
}

.order-count-info strong {
  color: var(--ink-900);
  font-family: var(--font-num);
  font-variant-numeric: tabular-nums;
}

.orders-list-section {
  display: flex;
  flex-direction: column;
  gap: var(--card-gap-compact);
  min-height: 320px;
}

/* 订单卡：同一列表只用一个质感层级 L1，hover 只提边框与阴影（§3.4、§4.2） */
.order-main {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  padding: var(--card-padding-compact);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--surface);
  box-shadow: var(--shadow-sm);
  transition:
    border-color var(--duration-fast) var(--ease-out),
    box-shadow var(--duration-fast) var(--ease-out);
}

.order-main:hover {
  border-color: var(--line-strong);
  box-shadow: var(--shadow-md);
}

.order-content {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
}

.order-left {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: var(--space-3);
  min-width: 0;
}

.order-basic-info {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  gap: var(--space-2) var(--space-6);
  align-items: baseline;
}

.order-no,
.order-amount,
.order-time {
  display: flex;
  align-items: baseline;
  gap: var(--space-2);
  min-width: 0;
}

.no-label,
.amount-label,
.time-label {
  flex-shrink: 0;
  color: var(--ink-400);
  font-size: var(--text-caption);
}

.no-value,
.time-value {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.no-value {
  color: var(--ink-900);
  font-family: var(--font-num);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-semibold);
  font-variant-numeric: tabular-nums;
}

.amount-value {
  color: var(--ink-900);
  font-family: var(--font-num);
  font-size: var(--text-body);
  font-weight: var(--weight-semibold);
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

.order-time {
  justify-self: end;
}

.time-value {
  color: var(--ink-500);
  font-size: var(--text-body-sm);
}

.order-dishes-list {
  padding: var(--space-3);
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  background: var(--canvas);
}

.dishes-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-2);
  padding-bottom: var(--space-2);
  border-bottom: 1px dashed var(--line);
}

.dishes-title {
  color: var(--ink-700);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-semibold);
}

.dishes-count {
  color: var(--ink-500);
  font-size: var(--text-caption);
}

.dishes-items {
  display: flex;
  flex-direction: column;
}

.dish-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-2) 0;
}

.dish-info {
  display: flex;
  align-items: baseline;
  gap: var(--space-2);
  min-width: 0;
}

.dish-name {
  overflow: hidden;
  color: var(--ink-700);
  font-size: var(--text-body-sm);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dish-quantity {
  flex-shrink: 0;
  color: var(--ink-400);
  font-size: var(--text-caption);
}

.dish-price {
  flex-shrink: 0;
  color: var(--ink-700);
  font-family: var(--font-num);
  font-size: var(--text-body-sm);
  font-variant-numeric: tabular-nums;
}

.order-user-info {
  display: flex;
}

.user-address {
  display: flex;
  align-items: baseline;
  gap: var(--space-2);
  min-width: 0;
}

.info-label {
  flex-shrink: 0;
  color: var(--ink-400);
  font-size: var(--text-caption);
}

.info-value {
  overflow: hidden;
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-status {
  flex-shrink: 0;
}

/* 状态切换留在原位，底色与文字色同时过渡（§3.5、§4.6） */
.order-status :deep(.status-tag) {
  transition:
    background-color var(--duration-base) var(--ease-standard),
    color var(--duration-base) var(--ease-standard);
}

.order-divider {
  height: 1px;
  background: var(--line);
}

.order-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}

.primary-actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

/* 行内控件高度取 --control-height-sm（§3.4） */
.row-btn {
  height: var(--control-height-sm);
  padding: 0 var(--space-3);
  border-radius: var(--radius-md);
  font-size: var(--text-body-sm);
}

.more-btn {
  padding: 0 var(--space-2);
}

.sort-option {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.dropdown-danger {
  color: var(--danger);
}

@media (max-width: 1280px) {
  .merchant-orders-container {
    padding: 0 var(--space-4) var(--space-4);
  }
}

@media (max-width: 1024px) {
  .stat-row {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .stat-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .toolbar,
  .toolbar-right {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input,
  .sort-select {
    width: 100%;
  }

  .order-content {
    flex-direction: column;
  }

  .order-basic-info {
    grid-template-columns: 1fr;
  }

  .order-time {
    justify-self: start;
  }

  .order-actions {
    flex-wrap: wrap;
  }
}
</style>
