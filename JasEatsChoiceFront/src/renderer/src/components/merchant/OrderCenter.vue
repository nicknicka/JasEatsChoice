<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'
import { useAuthStore } from '../../store/authStore'

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

// 订单状态映射（对应后端状态码）
// 0-待支付、1-待接单、2-备菜中、3-烹饪中、4-待上菜、5-已送达、6-已取消、7-待评价、8-已评价
const orderStatusMap = {
  0: '待支付',
  1: '待接单',
  2: '备菜中',
  3: '烹饪中',
  4: '待上菜',
  5: '已送达',
  6: '已取消',
  7: '待评价',
  8: '已评价'
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
    const orderDate = new Date(order.createTime)

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

// 获取下一个状态按钮文案
const getNextStatusText = (currentStatus) => {
  const statusFlow = {
    1: '👉 开始备菜',    // 待接单 -> 备菜中
    2: '🔥 开始烹饪',    // 备菜中 -> 烹饪中
    3: '🚦 准备配送',    // 烹饪中 -> 待配送
    4: '✅ 完成订单',    // 待配送 -> 已完成
    5: null,             // 已完成 -> 不可再改
    6: null              // 已取消 -> 不可再改
  }
  return statusFlow[currentStatus] || null
}

// 判断是否可以更新状态
const canUpdateStatus = (currentStatus) => {
  return [1, 2, 3, 4].includes(currentStatus)
}

// 判断是否可以通知用户
const canNotifyUser = (currentStatus) => {
  return ![6, 8].includes(currentStatus) // 排除已取消和已评价
}

// 获取订单状态标签的类型
const getStatusTagType = (status) => {
  const typeMap = {
    0: 'warning',    // 待支付 - 橙色
    1: 'info',       // 待接单 - 蓝灰色
    2: 'primary',    // 备菜中 - 蓝色
    3: '',           // 烹饪中 - 默认
    4: 'warning',    // 待配送 - 橙色
    5: 'success',    // 已送达 - 绿色
    6: 'danger',     // 已取消 - 红色
    7: 'info',       // 待评价 - 蓝灰色
    8: 'success'     // 已评价 - 绿色
  }
  return typeMap[status] || 'info'
}

// 更新订单状态
const updateOrderStatus = (order) => {
  // 定义订单状态流转逻辑
  const statusFlow = {
    1: 2, // 待处理 -> 备菜中
    2: 3, // 备菜中 -> 烹饪中
    3: 4, // 烹饪中 -> 待配送
    4: 5, // 待配送 -> 已完成
    5: 5, // 已完成 -> 已完成（不可再改）
    6: 6 // 已取消 -> 已取消（不可再改）
  }

  const nextStatus = statusFlow[order.status] || order.status

  // 如果状态没有变化
  if (nextStatus === order.status) {
    ElMessage.warning(`订单 ${order.id} 当前状态不可变更`)
    return
  }

  // 调用API更新订单状态
  const updateData = {
    orderId: order.id,
    status: nextStatus
  }

  api
    .put(API_CONFIG.merchant.updateOrderStatus.replace('{orderId}', order.id), updateData)
    .then((response) => {
      if (response.data && response.data.success) {
        // 更新本地订单状态
        order.status = nextStatus

        // ✅ 自动通知用户（后端实现自动推送）
        const notifyData = {
          orderId: order.id,
          message: `您的订单 ${order.id} 状态已更新为 ${orderStatusMap[nextStatus]}`
        }

        api
          .post(API_CONFIG.merchant.notifyUser.replace('{orderId}', order.id), notifyData)
          .then(() => {
            ElMessage.success(`订单 ${order.id} 状态已更新并通知用户`)
          })
          .catch((error) => {
            console.error('自动通知用户失败:', error)
            // 即使通知失败，状态更新成功也提示用户
            ElMessage.success(`订单 ${order.id} 状态已更新为 ${orderStatusMap[nextStatus]}`)
          })
      }
    })
    .catch((error) => {
      console.error('更新订单状态失败:', error)
      ElMessage.error('更新订单状态失败')
    })
}

// 手动通知用户（用于特殊情况）
const notifyUser = (order) => {
  // 调用API通知用户
  const notifyData = {
    orderId: order.id,
    message: `您的订单 ${order.id} 当前状态：${orderStatusMap[order.status]}`
  }

  api
    .post(API_CONFIG.merchant.notifyUser.replace('{orderId}', order.id), notifyData)
    .then((response) => {
      if (response.data && response.data.success) {
        ElMessage.success(`已手动通知用户订单 ${order.id} 的最新状态`)
      }
    })
    .catch((error) => {
      console.error('通知用户失败:', error)
      ElMessage.error('通知用户失败')
    })
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
        allOrders.value = response.data
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
      <h3 class="card-title">📋 订单中心</h3>
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
            <div class="order-no">🍽️ 订单号：{{ order.id }}</div>
            <div class="order-details">
              <span class="amount"
                >¥{{ order.totalAmount ? order.totalAmount.toFixed(2) : '0.00' }}</span
              >
              <span class="time">⏱️ {{ order.createTime }}</span>
            </div>
          </div>

          <!-- 中间：详情按钮 -->
          <div class="order-detail-section">
            <el-button
              type="primary"
              size="small"
              @click="viewOrderDetails(order)"
            >
              详情
            </el-button>
          </div>

          <!-- 右侧：操作按钮 -->
          <div class="order-actions">
            <!-- 更新状态按钮 -->
            <el-button
              v-if="canUpdateStatus(order.status)"
              type="success"
              size="small"
              @click="updateOrderStatus(order)"
            >
              {{ getNextStatusText(order.status) }}
            </el-button>
            <!-- 提醒按钮 -->
            <el-tooltip
              content="提醒用户订单状态"
              placement="top"
              effect="dark"
            >
              <el-button
                v-if="canNotifyUser(order.status)"
                type="warning"
                size="small"
                @click="notifyUser(order)"
                class="circle-icon-btn"
              >
                🔔
              </el-button>
            </el-tooltip>
          </div>
        </div>
      </div>
      <!-- 🔧 移除Transition，避免高度跳动 -->
      <div v-if="filteredOrders.length === 0" class="no-orders">
        <div class="empty-icon">📋</div>
        <p>暂无订单数据</p>
      </div>
    </div>

    <div class="view-all">
      <el-button type="text" @click="navigateToOrders">📤 查看全部订单</el-button>
    </div>
  </div>
</template>

<style scoped lang="less">
.orders-card {
  margin-bottom: 24px;
  padding: 20px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #f0f0f0;

  .orders-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 16px;
    border-bottom: 1px solid #f0f0f0;

    .card-title {
      font-size: 18px;
      font-weight: 600;
      color: #1a1a1a;
      margin: 0;
      display: flex;
      align-items: center;
      gap: 8px;
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
      background: #f1f1f1;
      border-radius: 3px;
    }

    &::-webkit-scrollbar-thumb {
      background: #c1c1c1;
      border-radius: 3px;
      transition: background 0.2s ease;

      &:hover {
        background: #a8a8a8;
      }
    }

    // Firefox滚动条样式
    scrollbar-width: thin;
    scrollbar-color: #c1c1c1 #f1f1f1;

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
      color: #999;

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
      padding: 16px;
      background: #fafafa;
      border-radius: 12px;
      border: 1px solid #e8e8e8;
      // 🔧 修复：只过渡hover相关的颜色属性
      transition: background 0.2s ease, border-color 0.2s ease;
      // 🔧 完全移除动画，避免布局跳动
      animation: none;

      &:hover {
        background: #ffffff;
        border-color: #409eff;
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
          font-size: 14px;
          font-weight: 600;
          color: #1a1a1a;
          margin-bottom: 8px;
        }

        .order-details {
          display: flex;
          align-items: center;
          gap: 12px;
          font-size: 13px;
          color: #666;

          .amount {
            font-weight: 600;
            color: #ff4d4f;
            font-size: 15px;
          }

          .time {
            display: flex;
            align-items: center;
          }
        }
      }

      .order-actions {
        grid-area: actions;
        display: flex;
        gap: 8px;
        flex-shrink: 0;
        align-items: center;

        :deep(.el-button) {
          height: 32px;
          padding: 0 12px;
          font-size: 13px;
          border-radius: 6px;
          // 🔧 修复：只过渡颜色和背景，不过渡transform避免高度变化
          transition: background-color 0.2s ease, border-color 0.2s ease, color 0.2s ease;

          // 纯图标按钮样式
          &.icon-only-btn {
            padding: 0 8px;
            min-width: 36px;
            font-size: 16px;
          }

          // 圆形图标按钮样式
          &.circle-icon-btn {
            width: 32px;
            height: 32px;
            padding: 0;
            border-radius: 50%;
            min-width: 32px;
            font-size: 16px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
          }

          &:hover {
            // 🔧 移除transform，改用其他视觉反馈
            filter: brightness(1.1);
          }

          &.el-button--primary {
            background: #409eff;
            border-color: #409eff;

            &:hover {
              background: #66b1ff;
              border-color: #66b1ff;
            }
          }

          &.el-button--success {
            background: #67c23a;
            border-color: #67c23a;

            &:hover {
              background: #85ce61;
              border-color: #85ce61;
            }
          }

          &.el-button--warning {
            background: #e6a23c;
            border-color: #e6a23c;

            &:hover {
              background: #ebb563;
              border-color: #ebb563;
            }
          }
        }
      }
    }
  }

  .view-all {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;
    text-align: center;

    :deep(.el-button) {
      color: #409eff;
      font-size: 14px;
      transition: all 0.2s ease;

      &:hover {
        color: #66b1ff;
      }
    }
  }
}
</style>
