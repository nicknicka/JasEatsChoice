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
  List,
  Money,
  Clock,
  KnifeFork,
  Location,
  EditPen
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import api from '../../utils/api.js'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import AppCard from '../../components/common/AppCard.vue'
import AppStatCard from '../../components/common/AppStatCard.vue'
import AppEmptyState from '../../components/common/AppEmptyState.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useAuthStore } from '../../store/authStore'
import { normalizeOrderStatusCode } from '../../utils/orderStatus'

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

// 订单状态映射（对应后端状态码）
// 0-待支付、1-待接单、2-制作中、3-已完成、4-已取消
// tagKey 交给 StatusTag 按 §3.1 状态表统一取色
const orderStatusMap = {
  0: { text: '待支付', tagKey: 'pendingpayment' },
  1: { text: '待接单', tagKey: 'pendingaccept' },
  2: { text: '制作中', tagKey: 'processing' },
  3: { text: '已完成', tagKey: 'completed' },
  4: { text: '已取消', tagKey: 'cancelled' }
}

// 全部订单数据
const orders = ref([])

// 当前选中的状态筛选（使用数字状态码）
const activeStatusFilter = ref('all')

// 搜索关键词
const searchKeyword = ref('')

// 是否当天真的没有订单：0 单时收起统计与筛选（DESIGN-BASELINE §4.5）
const hasOrders = computed(() => orders.value.length > 0)

// 筛选后的订单（改为computed自动计算）
const filteredOrders = computed(() => {
  return (
    orders.value
      .filter((order) => {
        // 状态筛选（支持数字状态码和组合状态）
        let statusMatch = false
        if (activeStatusFilter.value === 'all') {
          statusMatch = true
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
      .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
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
  const activeOrders = filteredOrders.value.filter((order) => order.status !== 4)
  const total = activeOrders.length
  const totalAmount = activeOrders.reduce((sum, order) => sum + (order.totalAmount || 0), 0)
  // 使用数字状态码统计：1-待接单、2-制作中、3-已完成、4-已取消
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

// 监听统计值变化，触发数字动画
watch(
  () => orderOverview.value,
  (newVal, oldVal) => {
    if (!oldVal) return

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
      orders.value = ordersData.map((order) => ({
        ...order,
        status: normalizeOrderStatusCode(order.status)
      }))
      return true
    } else {
      console.error('[今日订单] API返回失败:', response)
      ElMessage.error(response.message || '获取订单列表失败')
      return false
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
    return false
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
        // 使用状态更新接口将订单状态改为已取消(4)
        const response = await api.put(`/v1/orders/${order.id}/status`, { status: 4 })
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

// 获取状态标签文本（显示全部订单的数量，不受搜索影响）
const getStatusLabel = (status) => {
  if (status === 'all') return `全部 (${orders.value.length})`
  const count = orders.value.filter((o) => o.status === status).length
  return `${orderStatusMap[status].text} (${count})`
}

// 获取状态的订单数量
const getStatusCount = (status) => {
  if (status === 'all') return orders.value.length
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
    return '暂无制作中订单'
  }
  if (activeStatusFilter.value === 3) {
    return '暂无已完成订单'
  }
  if (activeStatusFilter.value === 4) {
    return '暂无已取消订单'
  }
  return '今日暂无订单'
}

// 空状态替代动作：0 单时给"查看全部订单"入口，筛不出结果时给"清除筛选"（§4.5）
const emptyActionText = computed(() => (hasOrders.value ? '清除筛选' : '查看全部订单'))

const handleEmptyAction = () => {
  if (hasOrders.value) {
    searchKeyword.value = ''
    activeStatusFilter.value = 'all'
    return
  }
  router.push('/merchant/home/orders')
}

// 页面加载时初始化
onMounted(() => {
  fetchOrders()
})
</script>

<template>
  <div class="today-orders-page">
    <!-- 页头：compact 版，标题 --text-h2 + 上下 --space-4，返回在右侧动作区（§4.7） -->
    <AppPageHeader title="今日订单" :compact="true">
      <template #extra>
        <span class="today-orders-page__date">{{ today }}</span>
      </template>
      <template #actions>
        <el-button type="default" size="large" :loading="loading" @click="refreshOrders">
          <el-icon><Refresh /></el-icon>
          <span>刷新</span>
        </el-button>
        <CommonBackButton type="default" />
      </template>
    </AppPageHeader>

    <!-- 概览：1 主指标 + 4 次级条，替代 4+1 空网格（§4.8） -->
    <div v-if="hasOrders" class="overview">
      <AppStatCard
        class="overview__primary"
        label="今日总订单"
        :value="animatedValues.total"
        :icon="List"
      />
      <AppCard class="overview__secondary" padding="md">
        <div class="overview__stats">
          <div class="overview-stat">
            <el-icon class="overview-stat__icon"><Money /></el-icon>
            <div class="overview-stat__body">
              <span class="overview-stat__value">¥{{ animatedValues.totalAmount.toFixed(0) }}</span>
              <span class="overview-stat__label">总金额</span>
            </div>
          </div>
          <div class="overview-stat">
            <el-icon class="overview-stat__icon"><Clock /></el-icon>
            <div class="overview-stat__body">
              <span class="overview-stat__value">{{ animatedValues.pendingCount }}</span>
              <span class="overview-stat__label">待处理</span>
            </div>
          </div>
          <div class="overview-stat">
            <el-icon class="overview-stat__icon"><KnifeFork /></el-icon>
            <div class="overview-stat__body">
              <span class="overview-stat__value">{{ animatedValues.preparingCount }}</span>
              <span class="overview-stat__label">准备中</span>
            </div>
          </div>
          <div class="overview-stat">
            <el-icon class="overview-stat__icon"><CircleCheckFilled /></el-icon>
            <div class="overview-stat__body">
              <span class="overview-stat__value">{{ animatedValues.completedCount }}</span>
              <span class="overview-stat__label">已完成</span>
            </div>
          </div>
        </div>
      </AppCard>
    </div>

    <!-- 状态筛选 + 搜索：控件统一 --control-height-sm（§3.4），0 单时收起（§4.5） -->
    <AppCard v-if="hasOrders" class="filter-card" padding="md">
      <div class="filter-bar">
        <div class="filter-bar__chips">
          <span class="filter-bar__label">状态筛选</span>
          <div class="status-filter-group">
            <button
              v-for="status in ['all', 0, 1, 2, 3, 4]"
              :key="status"
              type="button"
              class="status-chip"
              :class="{ 'status-chip--active': activeStatusFilter === status }"
              @click="activeStatusFilter = status"
            >
              <el-icon class="status-chip__icon">
                <List v-if="status === 'all'" />
                <CircleClose v-else-if="status === 0 || status === 1 || status === 4" />
                <Goods v-else-if="status === 2" />
                <CircleCheckFilled v-else />
              </el-icon>
              <span>
                {{
                  status === 'all' ? '全部' : status === 0 ? '待支付' : orderStatusMap[status].text
                }}
                <template v-if="status !== 'all'">({{ getStatusCount(status) }})</template>
              </span>
            </button>
          </div>
        </div>

        <div class="filter-bar__search">
          <el-input
            v-model="searchKeyword"
            size="default"
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
    </AppCard>

    <!-- 订单列表 -->
    <div v-loading="loading" class="orders-list">
      <div v-if="hasOrders" class="orders-count">
        共 <strong>{{ filteredOrders.length }}</strong> 个订单
      </div>

      <div v-if="filteredOrders.length > 0" class="orders-list__rows">
        <article v-for="order in filteredOrders" :key="order.id" class="order-item">
          <header class="order-item__head">
            <div class="order-item__id">
              <span class="order-item__id-label">订单号</span>
              <span class="order-item__id-value">{{ order.id }}</span>
            </div>
            <StatusTag
              size="sm"
              :status="orderStatusMap[order.status].tagKey"
              :text="orderStatusMap[order.status].text"
            />
          </header>

          <div class="order-item__meta">
            <span class="order-item__amount">¥{{ (order.totalAmount || 0).toFixed(2) }}</span>
            <span class="order-item__time" :title="order.createdAt">
              {{ getRelativeTime(order.createdAt) }}
            </span>
          </div>

          <div v-if="order.address" class="order-item__field">
            <el-icon class="order-item__field-icon"><Location /></el-icon>
            <span class="order-item__field-label">配送地址</span>
            <span class="order-item__field-value">{{ order.address }}</span>
          </div>

          <div v-if="expandedUserInfo.has(order.id)" class="order-item__details">
            <div class="order-item__field">
              <span class="order-item__field-label">用户ID</span>
              <span class="order-item__field-value">{{ order.userId || '未知' }}</span>
            </div>
            <div v-if="order.paymentTime" class="order-item__field">
              <span class="order-item__field-label">支付时间</span>
              <span class="order-item__field-value">{{ order.paymentTime }}</span>
            </div>
            <div v-if="order.paidAmount" class="order-item__field">
              <span class="order-item__field-label">已付金额</span>
              <span class="order-item__field-value">
                ¥{{ order.paidAmount ? order.paidAmount.toFixed(2) : '0.00' }}
              </span>
            </div>
            <div v-if="order.addressId" class="order-item__field">
              <span class="order-item__field-label">地址ID</span>
              <span class="order-item__field-value">{{ order.addressId }}</span>
            </div>
          </div>

          <div v-if="order.remark" class="order-item__field">
            <el-icon class="order-item__field-icon"><EditPen /></el-icon>
            <span class="order-item__field-label">备注</span>
            <span class="order-item__field-value">{{ order.remark }}</span>
          </div>

          <div v-if="order.orderDishes && order.orderDishes.length > 0" class="order-item__dishes">
            <div class="order-item__dishes-head">
              <span class="order-item__dishes-title">菜品清单</span>
              <span>共 {{ order.orderDishes.length }} 件</span>
            </div>
            <div v-for="dish in order.orderDishes" :key="dish.id" class="order-item__dish">
              <span class="order-item__dish-name">{{ dish.dishName || '未知菜品' }}</span>
              <span class="order-item__dish-qty">× {{ dish.quantity || 0 }}</span>
              <span v-if="dish.customization" class="order-item__dish-custom">
                ({{ dish.customization }})
              </span>
              <span class="order-item__dish-price">
                ¥{{ ((dish.price || 0) * (dish.quantity || 0)).toFixed(2) }}
              </span>
            </div>
          </div>

          <!-- 行操作：可见动作 ≤2，危险动作进「更多」下拉（§4.3） -->
          <div class="order-item__actions">
            <el-button type="default" size="default" @click="viewOrderDetails(order)">
              <el-icon><View /></el-icon>
              <span>详情</span>
            </el-button>

            <el-button
              v-if="order.status === 1"
              type="primary"
              size="default"
              @click="updateOrderStatus(order, 2)"
            >
              <el-icon><CircleCheck /></el-icon>
              <span>接单</span>
            </el-button>

            <el-button
              v-if="order.status === 2"
              type="primary"
              size="default"
              @click="updateOrderStatus(order, 3)"
            >
              <el-icon><CircleCheckFilled /></el-icon>
              <span>完成订单</span>
            </el-button>

            <el-button v-if="order.status === 3" type="info" size="default" disabled>
              <span>已完成</span>
            </el-button>

            <el-dropdown trigger="click" class="order-item__more">
              <el-button type="default" size="default" plain>
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-if="order.status === 1"
                    @click="rejectOrder(order)"
                  >
                    <el-icon><CircleClose /></el-icon>
                    <span class="dropdown-text-danger">拒绝</span>
                  </el-dropdown-item>
                  <el-dropdown-item @click="toggleUserInfoExpand(order.id)">
                    <el-icon><User /></el-icon>
                    <span>{{ expandedUserInfo.has(order.id) ? '隐藏' : '显示' }}用户信息</span>
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="order.status < 3"
                    divided
                    @click="cancelOrder(order)"
                  >
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
        </article>
      </div>

      <!-- 空状态：图标 + 说明 + 替代动作，整页不超 360px（§4.5） -->
      <AppEmptyState
        v-if="filteredOrders.length === 0 && !loading"
        class="orders-empty"
        :icon="List"
        :description="getEmptyDescription()"
        :action-text="emptyActionText"
        @action="handleEmptyAction"
      />
    </div>
  </div>
</template>

<style scoped lang="less">
/* 页面颜色、间距、圆角、字阶、动效均取自 tokens.css（§3、§7.1），无裸色值。 */
.today-orders-page {
  box-sizing: border-box;
  width: 100%;
  min-height: 100%;
  padding: 0 var(--page-gutter) var(--space-10);
  background: var(--canvas);
}

/* 页头日期：贴近标题行的可读日期（§4.7 extra 槽） */
.today-orders-page__date {
  color: var(--ink-500);
  font-family: var(--font-num);
  font-size: var(--text-body-sm);
  font-variant-numeric: tabular-nums;
  line-height: 20px;
}

/* 概览：1 主指标 + 4 次级条，替代 4+1 空网格（§4.8） */
.overview {
  display: grid;
  grid-template-columns: minmax(0, 240px) minmax(0, 1fr);
  gap: var(--card-gap-compact);
  margin-bottom: var(--space-6);
}

.overview__secondary {
  display: flex;
  align-items: center;
}

.overview__stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--space-4);
  width: 100%;
}

.overview-stat {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  min-width: 0;
}

.overview-stat__icon {
  flex-shrink: 0;
  color: var(--ink-400);
  font-size: var(--text-h3);
}

.overview-stat__body {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  min-width: 0;
}

.overview-stat__value {
  color: var(--ink-900);
  font-family: var(--font-num);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  font-variant-numeric: tabular-nums;
  line-height: 24px;
}

.overview-stat__label {
  color: var(--ink-500);
  font-size: var(--text-caption);
  line-height: 18px;
}

/* 筛选条：控件统一 --control-height-sm、同间距（§3.4） */
.filter-card {
  margin-bottom: var(--space-4);
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}

.filter-bar__chips {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
}

.filter-bar__label {
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  line-height: 32px;
}

.status-filter-group {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

/* 状态 chip：四态可见（§4.6） */
.status-chip {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  height: 28px;
  padding: 0 var(--space-3);
  border: 1px solid var(--line);
  border-radius: var(--radius-full);
  background: var(--surface);
  color: var(--ink-700);
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  line-height: 1;
  cursor: pointer;
  transition:
    border-color var(--duration-fast) var(--ease-out),
    background-color var(--duration-fast) var(--ease-out),
    color var(--duration-fast) var(--ease-out);
}

.status-chip:hover {
  border-color: var(--line-strong);
  background: var(--canvas-sunken);
}

.status-chip:active {
  transform: translateY(1px);
}

.status-chip:focus-visible {
  outline: none;
  box-shadow: 0 0 0 2px var(--brand-400);
}

.status-chip--active {
  border-color: var(--brand-500);
  background: var(--brand-50);
  color: var(--brand-600);
}

.status-chip__icon {
  font-size: var(--text-body-sm);
}

.filter-bar__search {
  min-width: 240px;
}

.search-input {
  width: 240px;
}

/* 计数条（§4.3 合计） */
.orders-count {
  margin-bottom: var(--space-3);
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  line-height: 20px;
}

.orders-count strong {
  color: var(--ink-900);
  font-family: var(--font-num);
  font-variant-numeric: tabular-nums;
}

/* 订单列表 */
.orders-list {
  min-height: 240px;
}

.orders-list__rows {
  display: flex;
  flex-direction: column;
  gap: var(--card-gap-compact);
}

/* 订单卡：L1 微质感，卡片内边距取 --card-padding-compact（§3.4） */
.order-item {
  box-sizing: border-box;
  padding: var(--card-padding-compact);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--surface);
  box-shadow: inset 0 1px 0 var(--surface-highlight);
}

.order-item__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}

.order-item__id {
  display: flex;
  align-items: baseline;
  gap: var(--space-2);
  min-width: 0;
}

.order-item__id-label {
  flex-shrink: 0;
  color: var(--ink-500);
  font-size: var(--text-body-sm);
}

.order-item__id-value {
  overflow: hidden;
  color: var(--ink-900);
  font-family: var(--font-num);
  font-size: var(--text-body);
  font-weight: var(--weight-medium);
  font-variant-numeric: tabular-nums;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-item__meta {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-top: var(--space-2);
}

.order-item__amount {
  color: var(--ink-900);
  font-family: var(--font-num);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  font-variant-numeric: tabular-nums;
}

.order-item__time {
  color: var(--ink-500);
  font-size: var(--text-body-sm);
}

.order-item__field {
  display: flex;
  align-items: baseline;
  gap: var(--space-2);
  margin-top: var(--space-2);
  font-size: var(--text-body-sm);
  line-height: 20px;
}

.order-item__field-icon {
  color: var(--ink-400);
  font-size: var(--text-body-sm);
}

.order-item__field-label {
  flex-shrink: 0;
  color: var(--ink-500);
}

.order-item__field-value {
  color: var(--ink-700);
  word-break: break-all;
}

.order-item__details {
  margin-top: var(--space-2);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-md);
  background: var(--canvas-sunken);
}

.order-item__details .order-item__field:first-child {
  margin-top: 0;
}

.order-item__dishes {
  margin-top: var(--space-3);
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  background: var(--canvas-sunken);
}

.order-item__dishes-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-2);
  color: var(--ink-500);
  font-size: var(--text-body-sm);
}

.order-item__dishes-title {
  color: var(--ink-700);
  font-weight: var(--weight-medium);
}

.order-item__dish {
  display: flex;
  align-items: baseline;
  gap: var(--space-2);
  padding: var(--space-1) 0;
  font-size: var(--text-body-sm);
}

.order-item__dish-name {
  color: var(--ink-700);
}

.order-item__dish-qty,
.order-item__dish-custom {
  color: var(--ink-500);
}

.order-item__dish-price {
  margin-left: auto;
  color: var(--ink-900);
  font-family: var(--font-num);
  font-variant-numeric: tabular-nums;
}

.order-item__actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-2);
  margin-top: var(--space-3);
  padding-top: var(--space-3);
  border-top: 1px solid var(--line);
}

/* 空状态：整页不超 360px，CTA 在视觉中心（§4.5） */
.orders-empty {
  min-height: 280px;
}

/* 危险/警示动作在下拉内（§4.3、§4.6） */
.dropdown-text-warning {
  color: var(--warning);
}

.dropdown-text-danger {
  color: var(--danger);
}
</style>
