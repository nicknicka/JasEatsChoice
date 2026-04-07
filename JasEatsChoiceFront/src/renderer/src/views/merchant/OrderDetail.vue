<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  CircleCheck,
  CircleCheckFilled,
  CircleClose,
  Goods,
  Dish,
  Phone,
  Location,
  User,
  Clock,
  Document,
  ArrowLeft,
  Refresh,
  Printer,
  Star
} from '@element-plus/icons-vue'
import axios from 'axios'
import { API_CONFIG } from '../../config/index.js'
import CommonBackButton from '../../components/common/CommonBackButton.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)

// 订单状态映射（对应后端状态码）
// 0-待支付、1-待接单、2-备菜中、3-烹饪中、4-待上菜、5-已送达、6-已取消、7-待评价、8-已评价
const orderStatusMap = {
  0: { text: '待支付', type: 'info', color: '#909399', icon: '💳' },
  1: { text: '待接单', type: 'danger', color: '#f56c6c', icon: '⏰' },
  2: { text: '备菜中', type: 'warning', color: '#e6a23c', icon: '🔪' },
  3: { text: '烹饪中', type: 'warning', color: '#ff9800', icon: '🍳' },
  4: { text: '待上菜', type: 'primary', color: '#409eff', icon: '🔔' },
  5: { text: '已送达', type: 'success', color: '#67c23a', icon: '✅' },
  6: { text: '已取消', type: 'info', color: '#c0c4cc', icon: '❌' },
  7: { text: '待评价', type: 'success', color: '#95d475', icon: '⭐' },
  8: { text: '已评价', type: 'success', color: '#85ce61', icon: '🌟' }
}

// 订单详情数据
const orderDetail = ref({
  id: null,
  orderNo: '',
  status: 1,
  userId: '',
  user: '',
  phone: '',
  address: '',
  totalAmount: 0,
  createTime: '',
  updateTime: '',
  orderDishes: [],
  remark: ''
})

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '--'
  const date = new Date(dateTime)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 加载订单详情
const loadOrderDetail = async () => {
  loading.value = true
  try {
    const orderId = route.params.id
    console.log('🔍 [订单详情] 开始加载订单，ID:', orderId)
    console.log('🔗 [订单详情] 请求URL:', `${API_CONFIG.baseURL}/v1/orders/${orderId}`)

    // 第一步：获取订单基本信息
    const orderResponse = await axios.get(`${API_CONFIG.baseURL}/v1/orders/${orderId}`)
    console.log('📦 [订单详情] 订单基本信息响应:', orderResponse.data)

    if (!orderResponse.data?.success || !orderResponse.data?.data) {
      throw new Error(orderResponse.data?.message || '获取订单基本信息失败')
    }

    // 第二步：获取订单菜品列表
    console.log('🍽️ [订单详情] 开始获取菜品列表...')
    const dishesResponse = await axios.get(`${API_CONFIG.baseURL}/v1/orders/${orderId}/dishes`)
    console.log('🍽️ [订单详情] 菜品列表响应:', dishesResponse.data)

    // 第三步：获取用户信息（如果有userId）
    const orderData = orderResponse.data.data
    let userInfo = null
    if (orderData.userId) {
      console.log('👤 [订单详情] 开始获取用户信息，userId:', orderData.userId)
      try {
        const userResponse = await axios.get(`${API_CONFIG.baseURL}/v1/users/${orderData.userId}`)
        console.log('👤 [订单详情] 用户信息响应:', userResponse.data)
        if (userResponse.data?.success && userResponse.data?.data) {
          userInfo = userResponse.data.data
        }
      } catch (userError) {
        console.warn('⚠️ [订单详情] 获取用户信息失败，将继续显示订单:', userError.message)
      }
    }

    // 组装完整的订单数据
    const dishesData = dishesResponse.data?.success ? dishesResponse.data.data : []

    orderDetail.value = {
      ...orderData,
      orderDishes: dishesData,
      // 补充订单号（使用 ID）
      orderNo: orderData.id,
      // 补充用户信息
      user: userInfo?.nickname || userInfo?.phone || orderData.userId,
      phone: userInfo?.phone || '--',
      // 格式化时间
      createTime: formatDateTime(orderData.createTime),
      updateTime: formatDateTime(orderData.updateTime)
    }

    console.log('✨ [订单详情] 订单详情已完整组装:', orderDetail.value)
    console.log('✅ [订单详情] 菜品数量:', orderDetail.value?.orderDishes?.length || 0)
    console.log('👤 [订单详情] 用户信息:', { user: orderDetail.value.user, phone: orderDetail.value.phone })
    console.log('📝 [订单详情] 订单备注:', orderDetail.value.remark)
  } catch (error) {
    console.error('❌ [订单详情] 加载失败:', error)
    console.error('❌ [订单详情] 错误详情:', {
      message: error.message,
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data
    })

    ElMessage.error(`加载订单详情失败: ${error.message}`)

    // 加载失败时，保留当前页面，但不显示数据
    orderDetail.value = {
      id: route.params.id,
      orderNo: '加载失败',
      status: 0,
      orderDishes: []
    }
  } finally {
    loading.value = false
    console.log('✅ [订单详情] 加载完成，loading:', loading.value)
  }
}

// 更新订单状态
const updateOrderStatus = async (newStatus) => {
  try {
    const response = await axios.put(
      `${API_CONFIG.baseURL}/v1/orders/${orderDetail.value.id}/status`,
      null,
      {
        params: { status: newStatus }
      }
    )

    if (response.data && response.data.success) {
      orderDetail.value.status = newStatus
      const now = new Date()
      orderDetail.value.updateTime = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
      ElMessage.success(`订单状态已更新为${orderStatusMap[newStatus].text}`)
    }
  } catch (error) {
    console.error('更新订单状态失败:', error)
    ElMessage.error('更新订单状态失败')
  }
}

// 打印订单
const printOrder = () => {
  window.print()
}

// 刷新订单
const refreshOrder = () => {
  loadOrderDetail()
}

// 计算菜品小计
const getItemSubtotal = (item) => {
  return (item.price || 0) * (item.quantity || 0)
}

// 获取状态进度
const getStatusProgress = () => {
  const status = orderDetail.value.status
  if (status === 0) return 0
  if (status === 1) return 1
  if (status === 2) return 2
  if (status === 3) return 3
  if (status === 4) return 4
  if (status === 3) return 3  // 已完成
  if (status === 4) return -1  // 已取消
  return 0
}

// 页面加载时获取订单详情
onMounted(() => {
  loadOrderDetail()
})
</script>

<template>
  <div class="order-detail-container">
    <!-- 头部 -->
    <div class="detail-header">
      <div class="header-left">
        <div class="page-title">
          <el-icon class="title-icon"><Document /></el-icon>
          <span>订单详情</span>
        </div>
        <div class="order-no">{{ orderDetail.orderNo || '--' }}</div>
      </div>
      <div class="header-right">
        <el-button size="small" :loading="loading" @click="refreshOrder">
          <el-icon><Refresh /></el-icon>
          <span>刷新</span>
        </el-button>
        <el-button size="small" @click="printOrder">
          <el-icon><Printer /></el-icon>
          <span>打印</span>
        </el-button>
        <common-back-button type="default" />
      </div>
    </div>

    <div v-loading="loading" class="detail-content">
      <!-- 订单状态卡片 -->
      <div class="status-card" :class="'status-' + orderDetail.status">
        <div class="status-header">
          <div class="status-info">
            <span class="status-icon">
              <el-icon v-if="orderDetail.status === 0"><Document /></el-icon>
              <el-icon v-else-if="orderDetail.status === 1"><Clock /></el-icon>
              <el-icon v-else-if="orderDetail.status === 2"><Goods /></el-icon>
              <el-icon v-else-if="orderDetail.status === 3"><Dish /></el-icon>
              <el-icon v-else-if="orderDetail.status === 4"><Document /></el-icon>
              <el-icon v-else-if="orderDetail.status === 3"><CircleCheckFilled /></el-icon>
              <el-icon v-else-if="orderDetail.status === 4"><CircleClose /></el-icon>
              <el-icon v-else><Document /></el-icon>
            </span>
            <span class="status-text">{{ orderStatusMap[orderDetail.status]?.text }}</span>
          </div>
          <div class="status-actions">
            <el-button
              v-if="orderDetail.status === 1"
              type="success"
              size="small"
              @click="updateOrderStatus(2)"
            >
              <el-icon><CircleCheck /></el-icon>
              接单
            </el-button>
            <el-button
              v-if="orderDetail.status === 2"
              type="warning"
              size="small"
              @click="updateOrderStatus(3)"
            >
              <el-icon><Goods /></el-icon>
              开始烹饪
            </el-button>
            <el-button
              v-if="orderDetail.status === 3"
              type="primary"
              size="small"
              @click="updateOrderStatus(4)"
            >
              <el-icon><Dish /></el-icon>
              上菜
            </el-button>
            <el-button
              v-if="orderDetail.status === 4"
              type="success"
              size="small"
              @click="updateOrderStatus(5)"
            >
              <el-icon><CircleCheckFilled /></el-icon>
              确认送达
            </el-button>
            <el-button
              v-if="orderDetail.status === 3"
              type="success"
              size="small"
              @click="updateOrderStatus(7)"
            >
              <el-icon><CircleCheckFilled /></el-icon>
              完成订单
            </el-button>
          </div>
        </div>
        <div class="status-progress">
          <div class="progress-step" :class="{ active: getStatusProgress() >= 0 }">
            <div class="step-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="step-text">待支付</div>
          </div>
          <div class="progress-line" :class="{ active: getStatusProgress() >= 1 }"></div>
          <div class="progress-step" :class="{ active: getStatusProgress() >= 1 }">
            <div class="step-icon">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="step-text">待接单</div>
          </div>
          <div class="progress-line" :class="{ active: getStatusProgress() >= 2 }"></div>
          <div class="progress-step" :class="{ active: getStatusProgress() >= 2 }">
            <div class="step-icon">
              <el-icon><Goods /></el-icon>
            </div>
            <div class="step-text">备菜中</div>
          </div>
          <div class="progress-line" :class="{ active: getStatusProgress() >= 3 }"></div>
          <div class="progress-step" :class="{ active: getStatusProgress() >= 3 }">
            <div class="step-icon">
              <el-icon><Dish /></el-icon>
            </div>
            <div class="step-text">烹饪中</div>
          </div>
          <div class="progress-line" :class="{ active: getStatusProgress() >= 4 }"></div>
          <div class="progress-step" :class="{ active: getStatusProgress() >= 4 }">
            <div class="step-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="step-text">待上菜</div>
          </div>
          <div class="progress-line" :class="{ active: getStatusProgress() >= 5 }"></div>
          <div class="progress-step" :class="{ active: getStatusProgress() >= 5 }">
            <div class="step-icon">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="step-text">已送达</div>
          </div>
          <div class="progress-line" :class="{ active: getStatusProgress() >= 6 }"></div>
          <div class="progress-step" :class="{ active: getStatusProgress() >= 6 }">
            <div class="step-icon">
              <el-icon><Star /></el-icon>
            </div>
            <div class="step-text">待评价</div>
          </div>
          <div class="progress-line" :class="{ active: getStatusProgress() >= 7 }"></div>
          <div class="progress-step" :class="{ active: getStatusProgress() >= 7 }">
            <div class="step-icon">
              <el-icon><CircleCheckFilled /></el-icon>
            </div>
            <div class="step-text">已评价</div>
          </div>
        </div>
      </div>

      <!-- 订单基本信息 -->
      <div class="info-section">
        <div class="section-title">
          <el-icon><User /></el-icon>
          <span>顾客信息</span>
        </div>
        <div class="info-grid">
          <div class="info-card">
            <div class="info-label">
              <el-icon><User /></el-icon>
              <span>用户昵称</span>
            </div>
            <div class="info-value">{{ orderDetail.user || '--' }}</div>
          </div>
          <div class="info-card">
            <div class="info-label">
              <el-icon><Phone /></el-icon>
              <span>联系电话</span>
            </div>
            <div class="info-value">{{ orderDetail.phone || '--' }}</div>
          </div>
          <div class="info-card full-width">
            <div class="info-label">
              <el-icon><Location /></el-icon>
              <span>配送地址</span>
            </div>
            <div class="info-value">{{ orderDetail.address || '--' }}</div>
          </div>
        </div>
      </div>

      <!-- 订单时间信息 -->
      <div class="info-section">
        <div class="section-title">
          <el-icon><Clock /></el-icon>
          <span>时间信息</span>
        </div>
        <div class="info-grid">
          <div class="info-card">
            <div class="info-label">
              <el-icon><Clock /></el-icon>
              <span>下单时间</span>
            </div>
            <div class="info-value">{{ orderDetail.createTime || '--' }}</div>
          </div>
          <div class="info-card">
            <div class="info-label">
              <el-icon><Refresh /></el-icon>
              <span>更新时间</span>
            </div>
            <div class="info-value">{{ orderDetail.updateTime || '--' }}</div>
          </div>
          <div class="info-card full-width remark-card">
            <div class="info-label">
              <span>📝</span>
              <span>订单备注</span>
            </div>
            <div class="info-value">{{ orderDetail.remark || '无' }}</div>
          </div>
        </div>
      </div>

      <!-- 支付信息 -->
      <div v-if="orderDetail.paidAmount || orderDetail.paymentTime" class="info-section">
        <div class="section-title">
          <el-icon><Document /></el-icon>
          <span>支付信息</span>
        </div>
        <div class="info-grid">
          <div v-if="orderDetail.paidAmount" class="info-card">
            <div class="info-label">
              <span>💰</span>
              <span>已支付金额</span>
            </div>
            <div class="info-value payment-amount">¥{{ orderDetail.paidAmount?.toFixed(2) || '0.00' }}</div>
          </div>
          <div v-if="orderDetail.paymentTime" class="info-card">
            <div class="info-label">
              <el-icon><Clock /></el-icon>
              <span>支付时间</span>
            </div>
            <div class="info-value">{{ orderDetail.paymentTime || '--' }}</div>
          </div>
          <div v-if="orderDetail.totalAmount && orderDetail.paidAmount && orderDetail.totalAmount !== orderDetail.paidAmount" class="info-card full-width">
            <div class="info-label">
              <span>📊</span>
              <span>金额差异</span>
            </div>
            <div class="info-value amount-diff">
              {{ (orderDetail.totalAmount - orderDetail.paidAmount).toFixed(2) }} 元
              <span class="diff-hint">(订单总额 - 已支付金额)</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 订单商品 -->
      <div class="items-section">
        <div class="section-title">
          <span>🍽️</span>
          <span>订单商品</span>
        </div>
        <div class="items-list">
          <div v-for="(item, index) in orderDetail.orderDishes" :key="index" class="item-row">
            <div class="item-info">
              <div class="item-name">{{ item.dishName }}</div>
              <div v-if="item.customization" class="item-customization">
                备注：{{ item.customization }}
              </div>
            </div>
            <div class="item-details">
              <div class="item-price">¥{{ item.price?.toFixed(2) || '0.00' }}</div>
              <div class="item-quantity">× {{ item.quantity }}</div>
              <div class="item-subtotal">¥{{ getItemSubtotal(item).toFixed(2) }}</div>
            </div>
          </div>
        </div>
        <div class="order-total">
          <div class="total-label">订单总计</div>
          <div class="total-value">¥{{ (orderDetail.totalAmount || 0).toFixed(2) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';
@import '../../assets/css/merchant-theme.less';

.order-detail-container {
  padding: 0 20px 20px 20px;

  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    background: linear-gradient(135deg, @merchant-primary 0%, @merchant-primary-dark 100%);
    border-radius: 16px;
    margin-bottom: 20px;
    box-shadow: 0 4px 16px rgba(74, 122, 77, 0.3);

    .header-left {
      display: flex;
      flex-direction: column;
      gap: 8px;

      .page-title {
        display: flex;
        align-items: center;
        gap: 10px;
        font-size: 1.714rem;
        font-weight: 700;
        color: @merchant-surface;
        margin: 0;

        .title-icon {
          font-size: 2rem;
        }
      }

      .order-no {
        font-size: 1rem;
        color: rgba(255, 255, 255, 0.9);
        font-family: 'Consolas', 'Monaco', monospace;
        font-weight: 500;
      }
    }

    .header-right {
      display: flex;
      gap: 10px;
      align-items: center;

      :deep(.el-button) {
        height: 32px;
        padding: 6px 14px;
        font-size: 0.929rem;
        font-weight: 500;
        white-space: nowrap;
        backdrop-filter: blur(10px);
        border: 1px solid rgba(255, 255, 255, 0.3);
        background: rgba(255, 255, 255, 0.1);
        color: @merchant-surface;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

        &:hover {
          background: rgba(255, 255, 255, 0.2);
          transform: translateY(-1px);
          box-shadow: 0 3px 8px rgba(0, 0, 0, 0.15);
        }
      }
    }
  }

  .detail-content {
    .status-card {
      background: @merchant-surface;
      border-radius: 16px;
      padding: 24px;
      margin-bottom: 20px;
      box-shadow: 0 2px 12px @merchant-shadow;
      border: 2px solid @merchant-border;
      position: relative;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        bottom: 0;
        width: 4px;
        background: @merchant-border;
        transition: all 0.3s ease;
      }

      &.status-0::before {
        background: linear-gradient(180deg, @merchant-text-muted 0%, lighten(@merchant-text-muted, 10%) 100%);
      }
      &.status-1::before {
        background: linear-gradient(180deg, @merchant-error 0%, lighten(@merchant-error, 10%) 100%);
      }
      &.status-2::before {
        background: linear-gradient(180deg, @merchant-status-pending 0%, lighten(@merchant-status-pending, 8%) 100%);
      }
      &.status-3::before {
        background: linear-gradient(180deg, @merchant-warning 0%, lighten(@merchant-warning, 10%) 100%);
      }
      &.status-4::before {
        background: linear-gradient(180deg, @merchant-info 0%, lighten(@merchant-info, 10%) 100%);
      }
      &.status-3::before {
        background: linear-gradient(180deg, @merchant-success 0%, lighten(@merchant-success, 10%) 100%);
      }
      &.status-6::before {
        background: linear-gradient(180deg, @merchant-status-cancelled 0%, lighten(@merchant-status-cancelled, 8%) 100%);
      }
      &.status-3::before {
        background: linear-gradient(180deg, @merchant-success 0%, lighten(@merchant-success, 15%) 100%);
      }
      &.status-4::before {
        background: linear-gradient(180deg, @merchant-status-cancelled 0%, lighten(@merchant-status-cancelled, 8%) 100%);
      }

      .status-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 24px;
        padding-bottom: 20px;
        border-bottom: 1px solid @merchant-divider;

        .status-info {
          display: flex;
          align-items: center;
          gap: 12px;

          .status-icon {
            font-size: 28px;
            color: @merchant-info;
          }

          .status-text {
            font-size: 1.286rem;
            font-weight: 600;
            color: @merchant-text;
          }
        }

        .status-actions {
          display: flex;
          gap: 8px;
          justify-content: flex-end;

          :deep(.el-button) {
            height: 32px;
            padding: 6px 14px;
            font-size: 0.929rem;
            font-weight: 500;
            white-space: nowrap;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            border-radius: 6px;

            &:hover {
              transform: translateY(-1px);
              box-shadow: 0 3px 8px rgba(0, 0, 0, 0.15);
            }

            &.el-button--success {
              background: linear-gradient(135deg, @merchant-success 0%, lighten(@merchant-success, 8%) 100%);
              border-color: transparent;
              box-shadow: 0 2px 6px rgba(90, 143, 94, 0.25);

              &:hover {
                box-shadow: 0 4px 10px rgba(90, 143, 94, 0.35);
              }
            }

            &.el-button--warning {
              background: linear-gradient(135deg, @merchant-warning 0%, lighten(@merchant-warning, 10%) 100%);
              border-color: transparent;
              box-shadow: 0 2px 6px rgba(212, 168, 85, 0.25);

              &:hover {
                box-shadow: 0 4px 10px rgba(212, 168, 85, 0.35);
              }
            }

            &.el-button--primary {
              background: linear-gradient(135deg, @merchant-info 0%, lighten(@merchant-info, 8%) 100%);
              border-color: transparent;
              box-shadow: 0 2px 6px rgba(91, 139, 210, 0.25);

              &:hover {
                box-shadow: 0 4px 10px rgba(91, 139, 210, 0.35);
              }
            }
          }
        }
      }

      .status-progress {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 16px;
        gap: 4px;

        .progress-step {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 6px;
          flex: 1;
          opacity: 0.4;
          transition: all 0.3s ease;
          min-width: 0;

          &.active {
            opacity: 1;

            .step-icon {
              color: @merchant-info;
              background: @merchant-primary-light;
            }
          }

          .step-icon {
            width: 32px;
            height: 32px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: transparent;
            border-radius: 50%;
            font-size: 18px;
            color: @merchant-text-muted;
            transition: all 0.3s ease;
          }

          .step-text {
            font-size: 0.786rem;
            color: @merchant-text-muted;
            font-weight: 500;
          }
        }

        .progress-line {
          flex: 1;
          height: 1px;
          background: @merchant-divider;
          margin: 0 4px;
          position: relative;
          top: -16px;
          transition: all 0.3s ease;

          &.active {
            background: @merchant-info;
          }
        }
      }
    }

    .info-section {
      background: @merchant-surface;
      border-radius: 16px;
      padding: 24px;
      margin-bottom: 20px;
      box-shadow: 0 2px 12px @merchant-shadow;
      border: 2px solid @merchant-border;

      .section-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 1.143rem;
        font-weight: 600;
        color: @merchant-text;
        margin-bottom: 20px;
        padding-bottom: 12px;
        border-bottom: 2px solid @merchant-divider;

        .el-icon {
          font-size: 1.286rem;
          color: @merchant-primary;
        }
      }

      .info-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 16px;

        .info-card {
          display: flex;
          flex-direction: column;
          gap: 8px;
          padding: 16px;
          background: linear-gradient(135deg, @merchant-surface-alt 0%, @merchant-surface 100%);
          border-radius: 12px;
          border: 1px solid @merchant-border;
          transition: all 0.3s ease;

          &:hover {
            box-shadow: 0 2px 12px @merchant-shadow;
            transform: translateY(-2px);
          }

          &.full-width {
            grid-column: 1 / -1;
          }

          &.remark-card {
            background: linear-gradient(135deg, @merchant-warning-light 0%, lighten(@merchant-warning-light, 2%) 100%);
            border-color: @merchant-warning;

            .info-label span {
              color: darken(@merchant-warning, 15%);
            }

            .info-value {
              color: darken(@merchant-warning, 25%);
              font-style: italic;
            }
          }

          .info-label {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 0.929rem;
            color: @merchant-text-muted;
            font-weight: 500;

            .el-icon {
              font-size: 1rem;
            }
          }

          .info-value {
            font-size: 1.071rem;
            color: @merchant-text;
            font-weight: 500;

            &.payment-amount {
              color: @merchant-success;
              font-size: 1.286rem;
              font-weight: 700;
            }

            &.amount-diff {
              color: @merchant-status-pending;
              font-weight: 600;

              .diff-hint {
                font-size: 0.857rem;
                color: @merchant-text-muted;
                font-weight: 400;
                margin-left: 8px;
              }
            }
          }
        }
      }
    }

    .items-section {
      background: @merchant-surface;
      border-radius: 16px;
      padding: 24px;
      box-shadow: 0 2px 12px @merchant-shadow;
      border: 2px solid @merchant-border;

      .section-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 1.143rem;
        font-weight: 600;
        color: @merchant-text;
        margin-bottom: 20px;
        padding-bottom: 12px;
        border-bottom: 2px solid @merchant-divider;
      }

      .items-list {
        margin-bottom: 20px;

        .item-row {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 16px;
          margin-bottom: 12px;
          background: linear-gradient(135deg, @merchant-surface-alt 0%, @merchant-surface 100%);
          border-radius: 12px;
          border: 1px solid @merchant-border;
          transition: all 0.3s ease;

          &:last-child {
            margin-bottom: 0;
          }

          &:hover {
            box-shadow: 0 2px 12px @merchant-shadow;
            transform: translateX(4px);
          }

          .item-info {
            flex: 1;

            .item-name {
              font-size: 1.071rem;
              font-weight: 600;
              color: @merchant-text;
              margin-bottom: 4px;
            }

            .item-customization {
              font-size: 0.857rem;
              color: @merchant-status-pending;
              font-style: italic;
            }
          }

          .item-details {
            display: flex;
            align-items: center;
            gap: 16px;

            .item-price {
              font-size: 1rem;
              color: @merchant-text-sec;
              min-width: 60px;
              text-align: right;
            }

            .item-quantity {
              font-size: 1rem;
              color: @merchant-text-sec;
              min-width: 50px;
              text-align: center;
            }

            .item-subtotal {
              font-size: 1.143rem;
              font-weight: 600;
              color: @merchant-error;
              min-width: 80px;
              text-align: right;
            }
          }
        }
      }

      .order-total {
        display: flex;
        justify-content: flex-end;
        align-items: center;
        padding: 20px;
        background: linear-gradient(135deg, @merchant-bg 0%, @merchant-surface 100%);
        border-radius: 12px;
        border: 2px solid @merchant-border;

        .total-label {
          font-size: 1.143rem;
          color: @merchant-text-sec;
          margin-right: 16px;
          font-weight: 500;
        }

        .total-value {
          font-size: 2rem;
          font-weight: 700;
          color: @merchant-error;
          font-family: 'Consolas', 'Monaco', monospace;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .order-detail-container {
    padding: 12px;

    .detail-header {
      flex-direction: column;
      gap: 12px;
      align-items: stretch;
    }

    .detail-content {
      .status-card {
        padding: 16px;

        .status-header {
          flex-direction: column;
          gap: 16px;
          align-items: stretch;

          .status-actions {
            justify-content: center;
          }
        }

        .status-progress {
          padding: 0 4px;
          gap: 2px;

          .progress-step {
            gap: 4px;

            .step-text {
              font-size: 10px;
            }

            .step-icon {
              width: 24px;
              height: 24px;
              font-size: 14px;
            }
          }

          .progress-line {
            height: 1px;
            margin: 0 2px;
            top: -14px;
          }
        }
      }

      .info-section {
        padding: 16px;

        .info-grid {
          grid-template-columns: 1fr;
        }
      }

      .items-section {
        padding: 16px;

        .items-list {
          .item-row {
            flex-direction: column;
            gap: 12px;

            .item-details {
              width: 100%;
              justify-content: space-between;
            }
          }
        }

        .order-total {
          flex-direction: column;
          gap: 12px;
          align-items: center;
          text-align: center;

          .total-value {
            font-size: 1.714rem;
          }
        }
      }
    }
  }
}

// 打印样式
@media print {
  .detail-header {
    background: @merchant-surface !important;
    box-shadow: none !important;
  }

  .header-right {
    display: none !important;
  }

  .status-actions {
    display: none !important;
  }
}
</style>