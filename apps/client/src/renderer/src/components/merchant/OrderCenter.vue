<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Tickets,
  Document,
  Clock,
  View,
  CircleCheck,
  Bell,
  ArrowRight
} from '@element-plus/icons-vue'
import api from '../../utils/api.js'
import { useAuthStore } from '../../store/authStore'
import { normalizeOrderStatusCode } from '../../utils/orderStatus'

const router = useRouter()
const authStore = useAuthStore()

let merchantId = authStore.merchantId

// 如果 Pinia 中没有商家ID，尝试从 localStorage 读取
if (!merchantId) {
  const localStorageMerchantId = localStorage.getItem('auth_merchantId')
  if (localStorageMerchantId) {
    merchantId = localStorageMerchantId
    authStore.setMerchantId(localStorageMerchantId) // 更新到 Pinia 中
  }
}

// 所有订单数据
const allOrders = ref([])

// 筛选后的订单
const filteredOrders = ref([])

// 当前激活的筛选条件
const activeFilter = ref('today')

// 订单状态映射（5状态系统：0-待支付、1-待接单、2-制作中、3-已完成、4-已取消）
const orderStatusMap = {
  0: '待支付',
  1: '待接单',
  2: '制作中',
  3: '已完成',
  4: '已取消'
}

// 筛选订单
const filterOrders = (filterType) => {
  activeFilter.value = filterType

  // 简单的筛选逻辑，根据实际时间处理
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const weekStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - now.getDay())
  const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)

  filteredOrders.value = allOrders.value.filter((order) => {
    const orderDate = new Date(order.createdAt)

    // 时间范围过滤
    let timeMatch = true
    switch (filterType) {
      case 'today':
        timeMatch = orderDate >= today
        break
      case 'week':
        timeMatch = orderDate >= weekStart
        break
      case 'month':
        timeMatch = orderDate >= monthStart
        break
    }

    return timeMatch
  })
}

// 页面跳转
const navigateToOrders = () => {
  router.push('/merchant/home/orders')
}

// 查看订单详情
const viewOrderDetails = (order) => {
  // 跳转到订单详情页面
  router.push(`/merchant/home/order-detail/${order.id}`)
}

// 获取下一个状态按钮文案（5状态系统）
const getNextStatusText = (currentStatus) => {
  const statusFlow = {
    1: '开始制作', // 待接单 -> 制作中
    2: '完成订单' // 制作中 -> 已完成
  }
  return statusFlow[currentStatus] || null
}

// 判断是否可以更新状态
const canUpdateStatus = (currentStatus) => {
  return [1, 2].includes(currentStatus)
}

// 判断是否可以通知用户
const canNotifyUser = (currentStatus) => {
  return ![3, 4].includes(currentStatus) // 排除已完成和已取消
}

// 获取订单状态标签的类型
const getStatusTagType = (status) => {
  const typeMap = {
    0: 'warning',    // 待支付 - 橙色
    1: 'info',       // 待接单 - 蓝灰色
    2: 'primary',    // 制作中 - 蓝色
    3: 'success',    // 已完成 - 绿色
    4: 'danger'      // 已取消 - 红色
  }
  return typeMap[status] || 'info'
}

// 更新订单状态
const updateOrderStatus = async (order) => {
  // 定义订单状态流转逻辑（5状态系统：0-待支付、1-待接单、2-制作中、3-已完成、4-已取消）
  const statusFlow = {
    1: 2, // 待接单 -> 制作中
    2: 3, // 制作中 -> 已完成
  }

  const nextStatus = statusFlow[order.status] || order.status

  // 如果状态没有变化
  if (nextStatus === order.status) {
    ElMessage.warning(`订单 ${order.id} 当前状态不可变更`)
    return
  }

  try {
    const response = await api.put(`/v1/orders/${order.id}/status`, { status: nextStatus })

    if (!response.success) {
      ElMessage.error(response.message || '更新订单状态失败')
      return
    }

    order.status = nextStatus

    const notifyData = {
      orderId: order.id,
      message: `您的订单 ${order.id} 状态已更新为 ${orderStatusMap[nextStatus]}`
    }

    try {
      const notifyResponse = await api.post(`/v1/orders/${order.id}/notify`, notifyData)
      if (notifyResponse.success) {
        ElMessage.success(`订单 ${order.id} 状态已更新并通知用户`)
        return
      }
    } catch (error) {
      console.error('自动通知用户失败:', error)
    }

    ElMessage.success(`订单 ${order.id} 状态已更新为 ${orderStatusMap[nextStatus]}`)
  } catch (error) {
    console.error('更新订单状态失败:', error)
    ElMessage.error('更新订单状态失败')
  }
}

// 手动通知用户（用于特殊情况）
const notifyUser = async (order) => {
  const notifyData = {
    orderId: order.id,
    message: `您的订单 ${order.id} 当前状态：${orderStatusMap[order.status]}`
  }

  try {
    const response = await api.post(`/v1/orders/${order.id}/notify`, notifyData)
    if (response.success) {
      ElMessage.success(`已手动通知用户订单 ${order.id} 的最新状态`)
      return
    }

    ElMessage.error(response.message || '通知用户失败')
  } catch (error) {
    console.error('通知用户失败:', error)
    ElMessage.error('通知用户失败')
  }
}

// 获取订单列表
const fetchOrders = () => {
  console.log('[OrderCenter] 开始获取订单列表')
  console.log('[OrderCenter] 商家ID:', merchantId)

  if (!merchantId) {
    console.error('[OrderCenter] 商家ID为空')
    ElMessage.warning('未找到商家信息，请重新登录')
    return
  }

  api
    .get(`/v1/orders/merchant/${merchantId}?today=false`)
    .then((response) => {
      console.log('[OrderCenter] API响应:', response)
      console.log('[OrderCenter] success字段:', response.success)
      console.log('[OrderCenter] code字段:', response.code)
      console.log('[OrderCenter] data字段:', response.data)

      // 修改判断逻辑：使用 response.success 而不是 response.code
      if (response.success && response.data) {
        console.log('[OrderCenter] 订单数量:', response.data.length)
        allOrders.value = response.data.map((order) => ({
          ...order,
          status: normalizeOrderStatusCode(order.status)
        }))
        // 默认显示今日订单
        filterOrders('today')
      } else {
        console.warn('[OrderCenter] API返回失败或无数据:', response)
        allOrders.value = []
        filteredOrders.value = []
      }
    })
    .catch((error) => {
      console.error('[OrderCenter] 获取订单列表失败:', error)
      ElMessage.error('获取订单列表失败')
      allOrders.value = []
      filteredOrders.value = []
    })
}

onMounted(() => {
  fetchOrders()
})
</script>

<template>
  <div class="orders-card">
    <div class="orders-header">
      <h3 class="card-title">
        <el-icon aria-hidden="true"><Tickets /></el-icon>
        <span>订单中心</span>
      </h3>
      <div class="filter-section">
        <el-tag
          type="success"
          effect="light"
          class="order-filter-tag"
          :class="{ active: activeFilter === 'today' }"
          @click="filterOrders('today')"
          >今日订单</el-tag
        >
        <el-tag
          type="info"
          effect="light"
          class="order-filter-tag"
          :class="{ active: activeFilter === 'week' }"
          @click="filterOrders('week')"
          >本周订单</el-tag
        >
        <el-tag
          type="warning"
          effect="light"
          class="order-filter-tag"
          :class="{ active: activeFilter === 'month' }"
          @click="filterOrders('month')"
          >本月订单</el-tag
        >
        <el-tag
          type="primary"
          effect="light"
          class="order-filter-tag"
          :class="{ active: activeFilter === 'all' }"
          @click="filterOrders('all')"
          >全部订单</el-tag
        >
      </div>
    </div>

    <div class="orders-list">
      <div class="order-items-wrapper">
        <div
          class="order-item"
          :class="{ 'order-reviewed': order.status === 3 }"
          v-for="order in filteredOrders"
          :key="order.id"
        >
          <!-- 左侧：订单信息 + 状态标签 -->
          <div class="order-info">
            <div class="order-header">
              <el-tag
                :type="getStatusTagType(order.status)"
                size="small"
              >
                {{ orderStatusMap[order.status] || '未知状态' }}
              </el-tag>
            </div>
            <div class="order-no">
              <el-icon aria-hidden="true"><Document /></el-icon>
              <span>订单号：{{ order.id }}</span>
            </div>
            <div class="order-details">
              <span class="amount"
                >¥{{ order.totalAmount ? order.totalAmount.toFixed(2) : '0.00' }}</span
              >
              <span class="time">
                <el-icon aria-hidden="true"><Clock /></el-icon>
                <span>{{ order.createdAt }}</span>
              </span>
            </div>
          </div>

          <!-- 中间：详情按钮 -->
          <div class="order-detail-section">
            <el-button @click="viewOrderDetails(order)">
              <el-icon aria-hidden="true"><View /></el-icon>
              <span>详情</span>
            </el-button>
          </div>

          <!-- 右侧：操作按钮 -->
          <div class="order-actions">
            <!-- 更新状态按钮 -->
            <el-button
              v-if="canUpdateStatus(order.status)"
              type="primary"
              @click="updateOrderStatus(order)"
            >
              <el-icon aria-hidden="true"><CircleCheck /></el-icon>
              <span>{{ getNextStatusText(order.status) }}</span>
            </el-button>
            <!-- 提醒按钮 -->
            <el-tooltip
              content="提醒用户订单状态"
              placement="top"
              effect="dark"
            >
              <el-button
                v-if="canNotifyUser(order.status)"
                class="icon-btn"
                @click="notifyUser(order)"
              >
                <el-icon aria-hidden="true"><Bell /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </div>
      </div>
      <!-- 🔧 移除Transition，避免高度跳动 -->
      <div v-if="filteredOrders.length === 0" class="no-orders">
        <el-icon class="empty-icon" aria-hidden="true"><Tickets /></el-icon>
        <p>暂无订单数据</p>
      </div>
    </div>

    <div class="view-all">
      <el-button link @click="navigateToOrders">
        <el-icon aria-hidden="true"><ArrowRight /></el-icon>
        <span>查看全部订单</span>
      </el-button>
    </div>
  </div>
</template>

<style scoped lang="less">
@import '../../assets/css/merchant-theme.less';

.orders-card {
  margin-bottom: var(--space-5);
  padding: var(--card-padding-compact);
  background: var(--surface);
  border-radius: var(--radius-lg);
  box-shadow: inset 0 1px 0 var(--surface-highlight);
  border: 1px solid var(--line);

  .orders-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 16px;
    border-bottom: 1px solid @merchant-divider;

    .card-title {
      font-size: var(--text-h3);
      font-weight: var(--weight-semibold);
      color: var(--ink-900);
      margin: 0;
      display: flex;
      align-items: center;
      gap: var(--space-2);

      .el-icon {
        color: var(--brand-500);
      }
    }

    .filter-section {
      display: flex;
      gap: 8px;

      .order-filter-tag {
        padding: 6px 14px;
        font-size: 13px;
        cursor: pointer;
        border-radius: 6px;
        transition: all 0.2s ease;
        user-select: none;

        &:hover {
          transform: translateY(-1px);
        }

        &.active {
          font-weight: 600;
        }
      }
    }
  }

  // CSS变量定义 - 用于高度计算
  --order-item-height: 90px;
  --order-gap: 12px;
  --visible-items: 3; // 可调整显示数量

  .orders-list {
    // 🔧 修复：完全移除min-height，让内容自然撑开，避免跳动
    max-height: calc(var(--order-item-height) * var(--visible-items) + var(--order-gap) * (var(--visible-items) - 1) + 20px);
    overflow-y: auto;
    padding-right: 8px;

    // 优化滚动条样式（Webkit浏览器：Chrome、Safari、Edge）
    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-track {
      background: @merchant-surface-alt;
      border-radius: 3px;
    }

    &::-webkit-scrollbar-thumb {
      background: @merchant-text-muted;
      border-radius: 3px;
      transition: background 0.2s ease;

      &:hover {
        background: darken(@merchant-text-muted, 10%);
      }
    }

    // Firefox滚动条样式
    scrollbar-width: thin;
    scrollbar-color: @merchant-text-muted @merchant-surface-alt;

    // 响应式调整
    @media (max-width: 768px) {
      --visible-items: 3; // 小屏幕显示3个
      max-height: calc(var(--order-item-height) * var(--visible-items) + var(--order-gap) * (var(--visible-items) - 1) + 20px);
    }

    @media (min-width: 1440px) {
      --visible-items: 6; // 大屏幕显示6个
      max-height: calc(var(--order-item-height) * var(--visible-items) + var(--order-gap) * (var(--visible-items) - 1) + 20px);
    }

    .order-items-wrapper {
      display: flex;
      flex-direction: column;
      gap: var(--order-gap); // 使用CSS变量
    }

    .no-orders {
      text-align: center;
      padding: 40px 20px;
      color: @merchant-text-muted;

      .empty-icon {
        font-size: 48px;
        margin-bottom: 12px;
        opacity: 0.5;
      }

      p {
        margin: 0;
        font-size: 14px;
      }
    }

    .order-item {
      position: relative;
      display: grid;
      grid-template-columns: 1fr auto auto;
      grid-template-areas: 'info detail actions';
      gap: 12px;
      align-items: center;
      padding: var(--space-4);
      background: @merchant-surface-alt;
      border-radius: var(--radius-lg);
      border: 1px solid @merchant-border;
      // 🔧 修复：只过渡hover相关的颜色属性
      transition: background 0.2s ease, border-color 0.2s ease;
      // 🔧 完全移除动画，避免布局跳动
      animation: none;

      // 行内控件高度统一取 --control-height-sm（§3.4）
      :deep(.el-button) {
        height: var(--control-height-sm);
        padding: 0 var(--space-3);
        font-size: var(--text-body-sm);
        border-radius: var(--radius-md);
        transition:
          background-color var(--duration-fast) var(--ease-out),
          border-color var(--duration-fast) var(--ease-out),
          color var(--duration-fast) var(--ease-out);

        // 纯图标按钮样式
        &.icon-btn {
          padding: 0 var(--space-2);
          min-width: var(--control-height-sm);
        }
      }

      &:hover {
        background: @merchant-surface;
        border-color: var(--brand-400);
        // 🔧 移除box-shadow，避免影响布局计算
      }

      .order-detail-section {
        grid-area: detail;
        display: flex;
        justify-content: center;
        align-items: center;
      }

      .order-status-tag {
        position: absolute;
        top: 12px;
        right: 12px;
        font-size: 12px;
        padding: 4px 10px;
        border-radius: 6px;
        font-weight: 500;
        z-index: 1;
      }

      // 已评价订单增加额外间距
      &.order-reviewed {
        margin-bottom: 16px;
      }

      .order-info {
        grid-area: info;
        min-width: 0;

        .order-header {
          margin-bottom: 8px;
        }

        .order-no {
          display: flex;
          align-items: center;
          gap: var(--space-2);
          margin-bottom: var(--space-2);
          font-size: var(--text-body);
          font-weight: var(--weight-semibold);
          color: var(--ink-900);

          .el-icon {
            color: var(--ink-400);
          }
        }

        .order-details {
          display: flex;
          align-items: center;
          gap: var(--space-3);
          font-size: var(--text-body-sm);
          color: var(--ink-500);

          .amount {
            font-weight: 600;
            color: @merchant-error;
            font-size: 15px;
          }

          .time {
            display: flex;
            align-items: center;
            gap: var(--space-1);

            .el-icon {
              color: var(--ink-400);
            }
          }
        }
      }

      .order-actions {
        grid-area: actions;
        display: flex;
        gap: 8px;
        flex-shrink: 0;
        align-items: center;
      }
    }
  }

  .view-all {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid @merchant-divider;
    text-align: center;

    :deep(.el-button) {
      height: var(--control-height-sm);
      color: var(--brand-600);
      font-size: var(--text-body-sm);

      &:hover {
        color: var(--brand-500);
      }
    }
  }
}
</style>
