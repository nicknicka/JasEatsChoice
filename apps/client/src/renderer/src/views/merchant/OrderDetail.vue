<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
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
  Refresh,
  Printer,
  Notebook,
  Money,
  Coin
} from '@element-plus/icons-vue'
import axios from 'axios'
import { API_CONFIG } from '../../config/index.js'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'

const route = useRoute()
const loading = ref(false)

// 订单状态映射（5状态系统：0-待支付、1-待接单、2-制作中、3-已完成、4-已取消）
// 状态色按 DESIGN-BASELINE §3.1 状态表：待处理=warning、进行中=info、已完成=success、已取消=danger
const orderStatusMap = {
  0: { text: '待支付', type: 'warning' },
  1: { text: '待接单', type: 'warning' },
  2: { text: '制作中', type: 'info' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'danger' }
}

// StatusTag 词典键（§3.1、§4.6）；与 02 订单列表同一套映射，保证同一状态跨页同色
const statusTagKeyMap = {
  0: 'pendingpayment',
  1: 'pendingaccept',
  2: 'preparing',
  3: 'completed',
  4: 'cancelled'
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
  createdAt: '',
  updatedAt: '',
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
      createdAt: formatDateTime(orderData.createdAt),
      updatedAt: formatDateTime(orderData.updatedAt)
    }

    console.log('✨ [订单详情] 订单详情已完整组装:', orderDetail.value)
    console.log('✅ [订单详情] 菜品数量:', orderDetail.value?.orderDishes?.length || 0)
    console.log('👤 [订单详情] 用户信息:', {
      user: orderDetail.value.user,
      phone: orderDetail.value.phone
    })
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
      { status: newStatus }
    )

    if (response.data && response.data.success) {
      orderDetail.value.status = newStatus
      const now = new Date()
      orderDetail.value.updatedAt = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
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

// 状态标签词典键（§3.1、§4.6）
const statusTagKey = computed(() => statusTagKeyMap[orderDetail.value.status] || 'unknown')

// 四步进度节点（§4.6）：节点配色走状态语义色，不新造颜色
const progressSteps = [
  { key: 'pending-payment', label: '待支付', icon: Document },
  { key: 'pending-accept', label: '待接单', icon: Clock },
  { key: 'preparing', label: '制作中', icon: Goods },
  { key: 'completed', label: '已完成', icon: CircleCheckFilled }
]

// 状态 → 进度索引（-1 表示已取消，不表达推进）
const STATUS_PROGRESS = { 0: 0, 1: 1, 2: 2, 3: 3, 4: -1 }

const progressIndex = computed(() => {
  const index = STATUS_PROGRESS[orderDetail.value.status]
  return index === undefined ? 0 : index
})

// 节点状态：done=成功绿、current=当前状态语义色、todo=中性灰
const stepState = (index) => {
  if (progressIndex.value < 0) return 'todo'
  if (index < progressIndex.value) return 'done'
  if (index === progressIndex.value) return 'current'
  return 'todo'
}

// 连线段：已走过的段与已完成节点同色（评审 P0）
const isLineDone = (index) => progressIndex.value > index

// 页面加载时获取订单详情
onMounted(() => {
  loadOrderDetail()
})
</script>

<template>
  <div class="order-detail-container">
    <!-- 页头：merchant 紧凑版；订单号与标题同行对照，刷新/打印降为次按钮，返回在右侧动作区（§4.1、§4.7） -->
    <AppPageHeader title="订单详情" :compact="true">
      <template #extra>
        <span class="order-no">{{ orderDetail.orderNo || '--' }}</span>
      </template>
      <template #actions>
        <el-button :loading="loading" @click="refreshOrder">
          <el-icon><Refresh /></el-icon>
          <span>刷新</span>
        </el-button>
        <el-button @click="printOrder">
          <el-icon><Printer /></el-icon>
          <span>打印</span>
        </el-button>
        <CommonBackButton type="default" />
      </template>
    </AppPageHeader>

    <div v-loading="loading" class="detail-content">
      <!-- 状态卡：状态标签与四步进度合并为一张紧凑卡（评审 P1）；已完成=成功绿、进行中=信息蓝（§3.1、§4.6） -->
      <div class="status-card" :class="'status-' + orderDetail.status">
        <div class="status-head">
          <div class="status-info">
            <span class="status-icon">
              <el-icon v-if="orderDetail.status === 0"><Document /></el-icon>
              <el-icon v-else-if="orderDetail.status === 1"><Clock /></el-icon>
              <el-icon v-else-if="orderDetail.status === 2"><Goods /></el-icon>
              <el-icon v-else-if="orderDetail.status === 3"><CircleCheckFilled /></el-icon>
              <el-icon v-else-if="orderDetail.status === 4"><CircleClose /></el-icon>
              <el-icon v-else><Document /></el-icon>
            </span>
            <StatusTag
              class="status-text"
              :status="statusTagKey"
              :text="orderStatusMap[orderDetail.status]?.text"
            />
          </div>
          <div class="status-actions">
            <el-button v-if="orderDetail.status === 1" type="primary" @click="updateOrderStatus(2)"
              ><el-icon><CircleCheck /></el-icon><span>接单</span></el-button
            >
            <el-button v-if="orderDetail.status === 2" type="primary" @click="updateOrderStatus(3)"
              ><el-icon><CircleCheckFilled /></el-icon><span>完成订单</span></el-button
            >
          </div>
        </div>

        <div class="status-progress">
          <template v-for="(step, index) in progressSteps" :key="step.key">
            <div class="progress-step" :class="'is-' + stepState(index)">
              <span class="step-icon">
                <el-icon><component :is="step.icon" /></el-icon>
              </span>
              <span class="step-text">{{ step.label }}</span>
            </div>
            <div
              v-if="index < progressSteps.length - 1"
              class="progress-line"
              :class="{ 'is-done': isLineDone(index) }"
            ></div>
          </template>
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
            <div class="info-value">{{ orderDetail.createdAt || '--' }}</div>
          </div>
          <div class="info-card">
            <div class="info-label">
              <el-icon><Refresh /></el-icon>
              <span>更新时间</span>
            </div>
            <div class="info-value">{{ orderDetail.updatedAt || '--' }}</div>
          </div>
          <div class="info-card full-width remark-card">
            <div class="info-label">
              <el-icon><Notebook /></el-icon>
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
              <el-icon><Money /></el-icon>
              <span>已支付金额</span>
            </div>
            <div class="info-value payment-amount">
              ¥{{ orderDetail.paidAmount?.toFixed(2) || '0.00' }}
            </div>
          </div>
          <div v-if="orderDetail.paymentTime" class="info-card">
            <div class="info-label">
              <el-icon><Clock /></el-icon>
              <span>支付时间</span>
            </div>
            <div class="info-value">{{ orderDetail.paymentTime || '--' }}</div>
          </div>
          <div
            v-if="
              orderDetail.totalAmount &&
              orderDetail.paidAmount &&
              orderDetail.totalAmount !== orderDetail.paidAmount
            "
            class="info-card full-width"
          >
            <div class="info-label">
              <el-icon><Coin /></el-icon>
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
          <el-icon><Dish /></el-icon>
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
/* 本页颜色、间距、圆角、字阶、动效全部取自 tokens.css 的 CSS 变量（§3、§7.1），无裸色值。 */
.order-detail-container {
  box-sizing: border-box;
  padding: 0 var(--space-6) var(--space-6);
}

/* 页头动作区控件高度取 --control-height-md（§3.4、§4.7） */
.order-detail-container :deep(.app-page-header__actions .el-button) {
  height: var(--control-height-md);
}

/* 订单号与标题同行对照：等宽数字 + 中性芯片，不再落成小字副标题（评审 P0；§3.2、§4.7） */
.order-no {
  display: inline-flex;
  align-items: center;
  padding: 2px var(--space-2);
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  background: var(--canvas-sunken);
  color: var(--ink-500);
  font-family: var(--font-num);
  font-size: var(--text-body-sm);
  line-height: 20px;
  letter-spacing: var(--tracking-wide);
}

.detail-content {
  /* 状态卡：状态标签与四步进度同卡，去掉彩色左边框（§4.6、评审 P1） */
  .status-card {
    margin-bottom: var(--card-gap-compact);
    padding: var(--card-padding-compact);
    background: var(--surface);
    border: 1px solid var(--line);
    border-radius: var(--radius-lg);
    box-shadow: inset 0 1px 0 var(--surface-highlight);
  }

  .status-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: var(--space-4);
    margin-bottom: var(--space-4);
  }

  .status-info {
    display: flex;
    align-items: center;
    gap: var(--space-3);
    min-width: 0;
  }

  .status-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    color: var(--ink-500);
    font-size: 22px;
    line-height: 1;
  }

  /* 状态语义色（§3.1 状态表）：待支付/待接单=警示、制作中=信息、已完成=成功、已取消=危险 */
  .status-0 .status-icon,
  .status-1 .status-icon {
    color: var(--warning);
  }

  .status-2 .status-icon {
    color: var(--info);
  }

  .status-3 .status-icon {
    color: var(--success);
  }

  .status-4 .status-icon {
    color: var(--danger);
  }

  .status-actions {
    display: flex;
    flex-shrink: 0;
    justify-content: flex-end;
    gap: var(--space-2);
  }

  /* 接单/完成订单是本页唯一实心主按钮，高度取 --control-height-md（§4.1、§3.4） */
  .status-actions :deep(.el-button) {
    height: var(--control-height-md);
  }

  /* 四步进度：节点与连线段都用状态语义色，不新造颜色（评审 P0；§4.6） */
  .status-progress {
    display: flex;
    align-items: flex-start;
    padding-top: var(--space-1);
  }

  .progress-step {
    display: flex;
    flex-shrink: 0;
    flex-direction: column;
    align-items: center;
    gap: var(--space-2);
    width: 88px;
  }

  .step-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    color: var(--ink-400);
    font-size: 18px;
    background: var(--canvas-sunken);
    border-radius: var(--radius-full);
    transition:
      background-color var(--duration-base) var(--ease-standard),
      color var(--duration-base) var(--ease-standard);
  }

  .step-text {
    color: var(--ink-500);
    font-size: var(--text-body-sm);
    line-height: 20px;
    font-weight: var(--weight-medium);
  }

  .progress-line {
    flex: 1;
    height: 2px;
    margin-top: 15px;
    background: var(--line);
    transition: background-color var(--duration-base) var(--ease-standard);
  }

  .progress-line.is-done {
    background: var(--success);
  }

  .progress-step.is-done .step-icon {
    color: var(--success);
    background: var(--success-bg);
  }

  .progress-step.is-done .step-text {
    color: var(--ink-700);
  }

  /* 当前节点按当前状态语义色高亮，状态切换走 200ms 过渡（评审 P2；§3.5） */
  .progress-step.is-current .step-text {
    color: var(--ink-900);
    font-weight: var(--weight-semibold);
  }

  .status-0 .progress-step.is-current .step-icon,
  .status-1 .progress-step.is-current .step-icon {
    color: var(--warning);
    background: var(--warning-bg);
  }

  .status-2 .progress-step.is-current .step-icon {
    color: var(--info);
    background: var(--info-bg);
  }

  .status-3 .progress-step.is-current .step-icon {
    color: var(--success);
    background: var(--success-bg);
  }

  /* 信息卡与明细卡：L1 微质感，内边距 --card-padding-compact，卡片间距 --card-gap-compact（§3.4、§4.2） */
  .info-section,
  .items-section {
    margin-bottom: var(--card-gap-compact);
    padding: var(--card-padding-compact);
    background: var(--surface);
    border: 1px solid var(--line);
    border-radius: var(--radius-lg);
    box-shadow: inset 0 1px 0 var(--surface-highlight);
  }

  .section-title {
    display: flex;
    align-items: center;
    gap: var(--space-2);
    margin-bottom: var(--space-4);
    padding-bottom: var(--space-2);
    color: var(--ink-900);
    font-size: var(--text-h3);
    line-height: 24px;
    font-weight: var(--weight-semibold);
    border-bottom: 1px solid var(--line);

    .el-icon {
      color: var(--brand-500);
      font-size: 18px;
    }
  }

  .info-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: var(--space-3);

    .info-card {
      display: flex;
      flex-direction: column;
      gap: var(--space-2);
      padding: var(--space-3) var(--space-4);
      background: var(--canvas-sunken);
      border: 1px solid var(--line);
      border-radius: var(--radius-md);

      &.full-width {
        grid-column: 1 / -1;
      }

      .info-label {
        display: flex;
        align-items: center;
        gap: var(--space-1);
        color: var(--ink-500);
        font-size: var(--text-body-sm);
        line-height: 20px;
        font-weight: var(--weight-medium);

        .el-icon {
          font-size: 16px;
        }
      }

      .info-value {
        color: var(--ink-900);
        font-size: var(--text-h3);
        line-height: 24px;
        font-weight: var(--weight-medium);
        word-break: break-word;
      }

      /* 金额不染色，只用中性深色 + 等宽数字（§3.1、§5.2） */
      .info-value.payment-amount {
        font-family: var(--font-num);
        font-weight: var(--weight-semibold);
      }

      .info-value.amount-diff {
        color: var(--ink-700);
        font-family: var(--font-num);
        font-size: var(--text-body);
        line-height: 22px;
      }

      .diff-hint {
        margin-left: var(--space-2);
        color: var(--ink-500);
        font-family: var(--font-sans);
        font-size: var(--text-caption);
        font-weight: var(--weight-regular);
      }
    }
  }

  .items-list {
    display: flex;
    flex-direction: column;
    gap: var(--space-2);
    margin-bottom: var(--space-4);

    .item-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: var(--space-4);
      padding: var(--space-3) var(--space-4);
      background: var(--canvas-sunken);
      border: 1px solid var(--line);
      border-radius: var(--radius-md);

      .item-info {
        flex: 1;
        min-width: 0;

        .item-name {
          color: var(--ink-900);
          font-size: var(--text-body);
          line-height: 22px;
          font-weight: var(--weight-semibold);
        }

        .item-customization {
          margin-top: var(--space-1);
          color: var(--ink-500);
          font-size: var(--text-body-sm);
          line-height: 20px;
        }
      }

      .item-details {
        display: flex;
        flex-shrink: 0;
        align-items: center;
        gap: var(--space-4);

        .item-price,
        .item-quantity {
          color: var(--ink-500);
          font-size: var(--text-body);
        }

        .item-price {
          min-width: 60px;
          text-align: right;
        }

        .item-quantity {
          min-width: 50px;
          text-align: center;
        }

        .item-subtotal {
          min-width: 80px;
          color: var(--ink-900);
          font-family: var(--font-num);
          font-size: var(--text-h3);
          font-weight: var(--weight-semibold);
          text-align: right;
        }
      }
    }
  }

  .order-total {
    display: flex;
    justify-content: flex-end;
    align-items: baseline;
    gap: var(--space-4);
    padding: var(--space-4);
    background: var(--canvas-sunken);
    border: 1px solid var(--line);
    border-radius: var(--radius-md);

    .total-label {
      color: var(--ink-500);
      font-size: var(--text-body);
      font-weight: var(--weight-medium);
    }

    .total-value {
      color: var(--ink-900);
      font-family: var(--font-num);
      font-size: var(--text-h2);
      font-weight: var(--weight-semibold);
      font-variant-numeric: tabular-nums;
    }
  }
}

@media (max-width: 768px) {
  .order-detail-container {
    padding: 0 var(--space-4) var(--space-4);
  }

  .detail-content {
    .status-head {
      flex-direction: column;
      align-items: stretch;
    }

    .status-actions {
      justify-content: flex-start;
    }

    .status-progress {
      .progress-step {
        width: auto;
        flex: 1;

        .step-text {
          font-size: var(--text-caption);
        }

        .step-icon {
          width: 28px;
          height: 28px;
          font-size: 16px;
        }
      }

      .progress-line {
        margin-top: 13px;
      }
    }

    .info-grid {
      grid-template-columns: 1fr;
    }

    .items-list .item-row {
      flex-direction: column;
      align-items: stretch;

      .item-details {
        justify-content: space-between;
      }
    }

    .order-total {
      flex-direction: column;
      align-items: center;
      gap: var(--space-1);
    }
  }
}

/* 打印：隐藏动作区，保留状态与明细信息（§4.7） */
@media print {
  .order-detail-container {
    padding: 0;
  }

  .status-actions,
  .order-detail-container :deep(.app-page-header__actions) {
    display: none !important;
  }

  .status-card,
  .info-section,
  .items-section {
    box-shadow: none !important;
  }
}
</style>
