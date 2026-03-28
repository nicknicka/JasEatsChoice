<template>
  <view class="order-detail-container">
    <!-- 订单状态卡片 -->
    <view class="status-card" :class="'status-' + orderInfo.status">
      <view class="status-icon">
        <uni-icons :type="getStatusIcon(orderInfo.status)" size="48" color="#fff"></uni-icons>
      </view>
      <view class="status-info">
        <text class="status-text">{{ orderInfo.statusText }}</text>
        <text class="status-desc">{{ getStatusDesc(orderInfo.status) }}</text>
      </view>
    </view>

    <!-- 订单进度 -->
    <view class="progress-section" v-if="orderInfo.status !== 'cancelled'">
      <view class="section-title">订单进度</view>
      <view class="progress-timeline">
        <view
          class="timeline-item"
          v-for="(step, index) in progressSteps"
          :key="index"
          :class="{ active: step.active, completed: step.completed }"
        >
          <view class="timeline-dot"></view>
          <view class="timeline-content">
            <text class="step-name">{{ step.name }}</text>
            <text class="step-time" v-if="step.time">{{ step.time }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 菜品详情 -->
    <view class="dish-section">
      <view class="section-title">菜品信息</view>
      <view class="dish-list">
        <view class="dish-item" v-for="dish in orderInfo.dishes" :key="dish.id">
          <image class="dish-image" :src="dish.image" mode="aspectFill"></image>
          <view class="dish-info">
            <text class="dish-name">{{ dish.name }}</text>
            <text class="dish-spec" v-if="dish.spec">{{ dish.spec }}</text>
            <view class="dish-price-row">
              <text class="dish-price">¥{{ dish.price }}</text>
              <text class="dish-quantity">x{{ dish.quantity }}</text>
            </view>
          </view>
          <text class="dish-total">¥{{ (dish.price * dish.quantity).toFixed(2) }}</text>
        </view>
      </view>
    </view>

    <!-- 订单备注 -->
    <view class="remark-section" v-if="orderInfo.remark">
      <view class="section-title">
        <uni-icons type="chatbubble" size="18" color="#FF6B35"></uni-icons>
        <text>订单备注</text>
      </view>
      <text class="remark-text">{{ orderInfo.remark }}</text>
    </view>

    <!-- 订单信息 -->
    <view class="order-info-section">
      <view class="section-title">订单信息</view>
      <view class="info-list">
        <view class="info-item">
          <text class="label">订单号</text>
          <text class="value">{{ orderInfo.orderNo }}</text>
        </view>
        <view class="info-item">
          <text class="label">下单时间</text>
          <text class="value">{{ orderInfo.orderTime }}</text>
        </view>
        <view class="info-item">
          <text class="label">期望时间</text>
          <text class="value">{{ orderInfo.expectTime }}</text>
        </view>
        <view class="info-item">
          <text class="label">取餐方式</text>
          <text class="value">{{ orderInfo.takeType }}</text>
        </view>
        <view class="info-item" v-if="orderInfo.takeType === '外卖配送'">
          <text class="label">配送地址</text>
          <text class="value">{{ orderInfo.address }}</text>
        </view>
        <view class="info-item" v-if="orderInfo.tableNo">
          <text class="label">餐桌号</text>
          <text class="value">{{ orderInfo.tableNo }}</text>
        </view>
        <view class="info-item">
          <text class="label">支付方式</text>
          <text class="value">{{ orderInfo.paymentMethod }}</text>
        </view>
      </view>
    </view>

    <!-- 顾客信息 -->
    <view class="customer-section">
      <view class="section-title">顾客信息</view>
      <view class="customer-info">
        <image class="customer-avatar" :src="orderInfo.customer.avatar" mode="aspectFill"></image>
        <view class="customer-details">
          <text class="customer-name">{{ orderInfo.customer.name }}</text>
          <text class="customer-phone">{{ orderInfo.customer.phone }}</text>
        </view>
        <view class="customer-actions">
          <button class="action-btn" @tap="makeCall">
            <uni-icons type="phone" size="18" color="#FF6B35"></uni-icons>
          </button>
          <button class="action-btn" @tap="sendMessage">
            <uni-icons type="chat" size="18" color="#FF6B35"></uni-icons>
          </button>
        </view>
      </view>
    </view>

    <!-- 费用明细 -->
    <view class="amount-section">
      <view class="section-title">费用明细</view>
      <view class="amount-list">
        <view class="amount-item">
          <text class="label">菜品金额</text>
          <text class="value">¥{{ orderInfo.dishAmount }}</text>
        </view>
        <view class="amount-item" v-if="orderInfo.discount > 0">
          <text class="label">优惠金额</text>
          <text class="value discount">-¥{{ orderInfo.discount }}</text>
        </view>
        <view class="amount-item" v-if="orderInfo.deliveryFee > 0">
          <text class="label">配送费</text>
          <text class="value">¥{{ orderInfo.deliveryFee }}</text>
        </view>
        <view class="amount-item total">
          <text class="label">实付金额</text>
          <text class="value">¥{{ orderInfo.amount }}</text>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button
        class="action-btn primary"
        v-if="orderInfo.status === 1"
        @tap="acceptOrder"
      >
        接单
      </button>
      <button
        class="action-btn primary"
        v-if="orderInfo.status === 2 || orderInfo.status === 3"
        @tap="updateProgress"
      >
        更新进度
      </button>
      <button
        class="action-btn success"
        v-if="orderInfo.status === 4"
        @tap="completeOrder"
      >
        完成订单
      </button>
      <button
        class="action-btn"
        v-if="orderInfo.status === 1"
        @tap="rejectOrder"
      >
        拒单
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { merchantApi } from '@/api'
import { useMerchantStore } from '@/store/modules/merchant'

const merchantStore = useMerchantStore()

// 订单ID
const orderId = ref('')

// 订单信息
const orderInfo = ref({
  id: 1,
  orderNo: 'OD202603180001',
  status: 'cooking',
  statusText: '制作中',
  dishes: [
    {
      id: 1,
      name: '宫保鸡丁',
      spec: '中辣',
      price: 28,
      quantity: 1,
      image: 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=1'
    },
    {
      id: 2,
      name: '鱼香肉丝',
      spec: '',
      price: 26,
      quantity: 2,
      image: 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=2'
    }
  ],
  remark: '少放辣，多放葱花',
  orderTime: '2026-03-18 12:30:25',
  expectTime: '2026-03-18 13:00',
  takeType: '到店自取',
  tableNo: 'A05',
  paymentMethod: '微信支付',
  dishAmount: '80.00',
  discount: '5.00',
  deliveryFee: '0.00',
  amount: '75.00',
  customer: {
    id: 1,
    name: '张同学',
    phone: '138****8888',
    avatar: 'https://via.placeholder.com/60/FF6B35/FFFFFF?text=张'
  }
})

// 进度步骤（5状态系统）
const progressSteps = ref([
  { name: '待接单', time: '', active: false, completed: false },
  { name: '制作中', time: '', active: false, completed: false },
  { name: '已完成', time: '', active: false, completed: false }
])

onMounted(() => {
  // 从页面参数获取订单ID
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options

  if (options.id) {
    orderId.value = options.id
    loadOrderDetail()
  }
})

/**
 * 加载订单详情
 */
const loadOrderDetail = async () => {
  try {
    // 调用API获取订单详情
    const res = await merchantApi.getOrderDetail(orderId.value)

    if (res && res.success && res.data) {
      const data = res.data

      // 获取订单菜品列表
      const dishesRes = await merchantApi.getOrderDishes(orderId.value)
      const dishes = dishesRes && dishesRes.success ? dishesRes.data || [] : []

      // 转换订单数据格式
      orderInfo.value = {
        id: data.id,
        orderNo: `OD${String(data.id).padStart(6, '0')}`,
        status: data.status,
        statusText: getStatusText(data.status),
        dishes: dishes.map(dish => ({
          id: dish.dishId || dish.id,
          name: dish.dishName || dish.name,
          spec: dish.spec || '',
          price: parseFloat(dish.price || 0),
          quantity: dish.quantity || 1,
          image: dish.image || dish.coverImage || ''
        })),
        remark: data.remark || '',
        orderTime: formatFullTime(data.createTime),
        expectTime: formatFullTime(data.updateTime) || '未设置',
        takeType: '到店自取',
        tableNo: '',
        paymentMethod: '微信支付',
        dishAmount: formatAmount(data.totalAmount || 0),
        discount: '0.00',
        deliveryFee: '0.00',
        amount: formatAmount(data.totalAmount || 0),
        customer: {
          id: data.userId,
          name: '顾客',
          phone: '未提供',
          avatar: ''
        }
      }

      // 更新进度步骤
      updateProgressSteps(data.status)
    }
  } catch (error) {
    console.error('加载订单详情失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

/**
 * 更新进度步骤（5状态系统）
 */
const updateProgressSteps = (status) => {
  const steps = [
    { name: '待接单', time: '', active: status === 1, completed: status > 1 },
    { name: '制作中', time: '', active: status === 2, completed: status > 2 },
    { name: '已完成', time: '', active: status === 3, completed: status > 3 }
  ]

  progressSteps.value = steps
}

/**
 * 格式化完整时间
 */
const formatFullTime = (time) => {
  if (!time) return ''
  if (typeof time === 'string') return time
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

/**
 * 格式化金额
 */
const formatAmount = (amount) => {
  if (typeof amount === 'number') {
    return amount.toFixed(2)
  }
  return String(amount)
}

/**
 * 获取状态图标
 */
const getStatusIcon = (status) => {
  const iconMap = {
    pending: 'clock',
    cooking: 'loop',
    ready: 'checkbox',
    completed: 'checkmarkempty',
    cancelled: 'closeempty'
  }
  return iconMap[status] || 'info'
}

/**
 * 获取状态描述（5状态系统）
 */
const getStatusDesc = (status) => {
  const descMap = {
    0: '等待支付',
    1: '等待商家接单',
    2: '商家正在制作中',
    3: '订单已完成',
    4: '订单已取消'
  }
  return descMap[status] || ''
}

/**
 * 获取状态文本（5状态系统）
 */
const getStatusText = (status) => {
  const statusMap = {
    0: '待支付',
    1: '待接单',
    2: '制作中',
    3: '已完成',
    4: '已取消'
  }
  return statusMap[status] || '未知状态'
}

/**
 * 接单
 */
const acceptOrder = async () => {
  uni.showModal({
    title: '确认接单',
    content: '确认接受此订单吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          // 调用API接单（状态改为2-备菜中）
          await merchantApi.acceptOrder(orderId.value)

          uni.showToast({
            title: '接单成功',
            icon: 'success'
          })

          // 重新加载订单详情
          loadOrderDetail()
        } catch (error) {
          console.error('接单失败:', error)
          uni.showToast({
            title: '接单失败，请重试',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 更新进度
 */
const updateProgress = () => {
  uni.navigateTo({
    url: `/pages-merchant/order/process?id=${orderInfo.value.id}`
  })
}

/**
 * 完成订单
 */
const completeOrder = async () => {
  uni.showModal({
    title: '确认完成',
    content: '确认订单已完成吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          // 调用API完成订单（状态改为3-已完成）
          await merchantApi.completeOrder(orderId.value)

          uni.showToast({
            title: '订单已完成',
            icon: 'success'
          })

          // 重新加载订单详情
          loadOrderDetail()
        } catch (error) {
          console.error('完成订单失败:', error)
          uni.showToast({
            title: '操作失败，请重试',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 拒单
 */
const rejectOrder = () => {
  uni.showModal({
    title: '确认拒单',
    content: '确认拒绝此订单吗？',
    editable: true,
    placeholderText: '请输入拒单原因',
    success: async (res) => {
      if (res.confirm) {
        try {
          const reason = res.content || '商家拒绝接单'

          // 调用API拒单
          await merchantApi.rejectOrder(orderId.value, reason)

          uni.showToast({
            title: '已拒单',
            icon: 'success'
          })

          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } catch (error) {
          console.error('拒单失败:', error)
          uni.showToast({
            title: '拒单失败，请重试',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 拨打电话
 */
const makeCall = () => {
  uni.makePhoneCall({
    phoneNumber: '13888888888'
  })
}

/**
 * 发送消息
 */
const sendMessage = () => {
  uni.navigateTo({
    url: `/pages-common/chat/chat-room?userId=${orderInfo.value.customer.id}`
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.order-detail-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 40rpx;
}

/* 状态卡片 */
.status-card {
  padding: 40rpx 30rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;

  &.status-pending {
    background: linear-gradient(135deg, #FAAD14, #FFC53D);
  }

  &.status-cooking {
    background: linear-gradient(135deg, #1890FF, #40A9FF);
  }

  &.status-ready {
    background: linear-gradient(135deg, #52C41A, #73D13D);
  }

  &.status-completed {
    background: linear-gradient(135deg, #52C41A, #73D13D);
  }

  &.status-cancelled {
    background: linear-gradient(135deg, #999, #BFBFBF);
  }
}

.status-icon {
  width: 80rpx;
  height: 80rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  @include flex-center;
}

.status-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.status-text {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
}

.status-desc {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* 通用section */
.progress-section,
.dish-section,
.remark-section,
.order-info-section,
.customer-section,
.amount-section {
  background: #fff;
  margin: 20rpx;
  padding: 30rpx;
  border-radius: 16rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
  gap: 10rpx;
}

/* 订单进度 */
.progress-timeline {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.timeline-item {
  display: flex;
  gap: 20rpx;
  position: relative;

  &:not(:last-child)::after {
    content: '';
    position: absolute;
    left: 15rpx;
    top: 30rpx;
    width: 2rpx;
    height: calc(100% + 30rpx);
    background: #E8E8E8;
  }

  &.completed::after {
    background: #52C41A;
  }
}

.timeline-dot {
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
  background: #E8E8E8;
  flex-shrink: 0;
  position: relative;
  z-index: 1;

  .timeline-item.completed & {
    background: #52C41A;
  }

  .timeline-item.active & {
    background: #FF6B35;
    box-shadow: 0 0 0 6rpx rgba(255, 107, 53, 0.2);
  }
}

.timeline-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.step-name {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;

  .timeline-item.active & {
    color: #FF6B35;
  }

  .timeline-item.completed & {
    color: #52C41A;
  }
}

.step-time {
  font-size: 24rpx;
  color: #999;
}

/* 菜品列表 */
.dish-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.dish-item {
  display: flex;
  gap: 20rpx;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
}

.dish-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.dish-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.dish-spec {
  font-size: 24rpx;
  color: #999;
  margin-top: 6rpx;
}

.dish-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10rpx;
}

.dish-price {
  font-size: 28rpx;
  color: #FF6B35;
  font-weight: bold;
}

.dish-quantity {
  font-size: 24rpx;
  color: #999;
}

.dish-total {
  font-size: 32rpx;
  font-weight: bold;
  color: #FF6B35;
  align-self: center;
}

/* 备注信息 */
.remark-text {
  font-size: 28rpx;
  color: #FF6B35;
  line-height: 1.6;
}

/* 订单信息 */
.info-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 28rpx;
}

.label {
  color: #999;
  min-width: 150rpx;
}

.value {
  color: #333;
  flex: 1;
  text-align: right;
}

/* 顾客信息 */
.customer-info {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.customer-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.customer-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.customer-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.customer-phone {
  font-size: 26rpx;
  color: #999;
}

.customer-actions {
  display: flex;
  gap: 15rpx;
}

.customer-actions .action-btn {
  width: 60rpx;
  height: 60rpx;
  padding: 0;
  background: #F5F5F5;
  border-radius: 50%;
  @include flex-center;
  border: none;
}

/* 费用明细 */
.amount-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.amount-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 28rpx;

  &.total {
    padding-top: 20rpx;
    border-top: 1rpx solid #eee;
  }

  .label {
    color: #666;
  }

  .value {
    color: #333;
    font-weight: bold;

    &.discount {
      color: #52C41A;
    }
  }

  &.total {
    .label {
      font-size: 32rpx;
      color: #333;
    }

    .value {
      font-size: 40rpx;
      color: #FF6B35;
    }
  }
}

/* 操作按钮 */
.action-buttons {
  padding: 40rpx 20rpx 0;
  display: flex;
  gap: 20rpx;
}

.action-btn {
  flex: 1;
  height: 90rpx;
  border-radius: 45rpx;
  font-size: 28rpx;
  background: #fff;
  color: #666;
  border: none;

  &.primary {
    background: #FF6B35;
    color: #fff;
  }

  &.success {
    background: #52C41A;
    color: #fff;
  }
}
</style>
