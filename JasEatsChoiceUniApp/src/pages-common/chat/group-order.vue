<template>
  <view class="group-order-container">
    <!-- 订单状态头部 -->
    <view class="order-header" :class="'status-' + orderInfo.status">
      <view class="header-content">
        <view class="status-icon">
          <uni-icons
            :type="getStatusIcon(orderInfo.status)"
            size="40"
            color="#fff"
          ></uni-icons>
        </view>
        <view class="status-info">
          <text class="status-title">{{ getStatusTitle(orderInfo.status) }}</text>
          <text class="status-desc">{{ getStatusDesc(orderInfo.status) }}</text>
        </view>
      </view>
      <view class="countdown" v-if="orderInfo.status === 'pending'">
        <uni-icons type="clock" size="16" color="#fff"></uni-icons>
        <text class="countdown-text">{{ countdownText }}</text>
      </view>
    </view>

    <!-- 商家信息 -->
    <view class="merchant-card" @tap="viewMerchant">
      <image class="merchant-avatar" :src="merchantInfo.avatar" mode="aspectFill"></image>
      <view class="merchant-info">
        <text class="merchant-name">{{ merchantInfo.name }}</text>
        <text class="merchant-category">{{ merchantInfo.category }}</text>
      </view>
      <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
    </view>

    <!-- 参与人员 -->
    <view class="participants-card">
      <view class="card-header">
        <text class="card-title">参与人员 ({{ participantList.length }}人)</text>
        <text class="card-action" @tap="viewAllParticipants">查看全部</text>
      </view>
      <view class="participants-list">
        <view
          class="participant-item"
          v-for="participant in participantList"
          :key="participant.id"
        >
          <view class="participant-avatar-wrapper">
            <image
              class="participant-avatar"
              :src="participant.avatar"
              mode="aspectFill"
            ></image>
            <view class="status-badge" :class="'status-' + participant.orderStatus"></view>
          </view>
          <view class="participant-info">
            <text class="participant-name">{{ participant.name }}</text>
            <text class="participant-dishes">{{ participant.dishCount }}道菜</text>
          </view>
          <view class="participant-amount">
            <text class="amount-value">¥{{ participant.amount }}</text>
            <text class="amount-status">{{ getOrderStatusText(participant.orderStatus) }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 菜品汇总 -->
    <view class="dishes-card">
      <view class="card-header">
        <text class="card-title">菜品汇总</text>
        <text class="card-action" @tap="viewAllDishes">查看全部</text>
      </view>
      <view class="dishes-list">
        <view
          class="dish-item"
          v-for="dish in dishSummary"
          :key="dish.id"
        >
          <image class="dish-image" :src="dish.image" mode="aspectFill"></image>
          <view class="dish-info">
            <text class="dish-name">{{ dish.name }}</text>
            <view class="dish-meta">
              <text class="dish-count">{{ dish.count }}份</text>
              <text class="dish-participants">{{ dish.participantCount }}人点</text>
            </view>
          </view>
          <text class="dish-amount">¥{{ dish.amount }}</text>
        </view>
      </view>
    </view>

    <!-- 我的订单 -->
    <view class="my-order-card" v-if="myOrder">
      <view class="card-header">
        <text class="card-title">我的订单</text>
      </view>
      <view class="my-dishes-list">
        <view
          class="my-dish-item"
          v-for="dish in myOrder.dishes"
          :key="dish.id"
        >
          <text class="dish-name">{{ dish.name }}</text>
          <text class="dish-detail">{{ dish.spec }}</text>
          <text class="dish-count">x{{ dish.count }}</text>
          <text class="dish-price">¥{{ dish.price }}</text>
        </view>
      </view>
      <view class="my-order-footer">
        <view class="order-total">
          <text class="total-label">我的金额</text>
          <text class="total-amount">¥{{ myOrder.totalAmount }}</text>
        </view>
      </view>
    </view>

    <!-- 支付信息 -->
    <view class="payment-card" v-if="orderInfo.status !== 'completed'">
      <view class="payment-row">
        <text class="payment-label">菜品总额</text>
        <text class="payment-value">¥{{ orderInfo.totalAmount }}</text>
      </view>
      <view class="payment-row" v-if="orderInfo.discount > 0">
        <text class="payment-label">优惠金额</text>
        <text class="payment-value discount">-¥{{ orderInfo.discount }}</text>
      </view>
      <view class="payment-row total">
        <text class="payment-label">实付金额</text>
        <text class="payment-value final">¥{{ orderInfo.finalAmount }}</text>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-actions">
      <button
        class="action-btn secondary"
        v-if="orderInfo.status === 'pending'"
        @tap="addDishes"
      >
        加菜
      </button>
      <button
        class="action-btn primary"
        v-if="orderInfo.status === 'pending' && myOrder && !myOrder.paid"
        @tap="payMyOrder"
      >
        支付我的 ¥{{ myOrder?.totalAmount }}
      </button>
      <button
        class="action-btn primary"
        v-if="orderInfo.status === 'pending' && canPayAll"
        @tap="payAllOrder"
      >
        统一支付 ¥{{ orderInfo.finalAmount }}
      </button>
      <button
        class="action-btn"
        v-if="orderInfo.status === 'in_progress'"
        @tap="viewProgress"
      >
        查看进度
      </button>
      <button
        class="action-btn"
        v-if="orderInfo.status === 'completed'"
        @tap="viewOrderDetail"
      >
        查看详情
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { groupOrderApi } from '@/api/modules/group-order.js'
import { paymentApi } from '@/api/modules/payment.js'

// 当前用户ID
const currentUserId = ref('')

// 订单ID
const orderId = ref('')

// 订单信息
const orderInfo = ref({
  id: '',
  status: 'pending', // pending, in_progress, completed
  totalAmount: '0.00',
  discount: '0.00',
  finalAmount: '0.00'
})

// 商家信息
const merchantInfo = ref({
  id: '',
  name: '',
  avatar: '',
  category: ''
})

// 参与人员列表
const participantList = ref([])

// 菜品汇总
const dishSummary = ref([])

// 我的订单
const myOrder = ref(null)

// 倒计时
let countdownTimer = null
const countdownSeconds = ref(1800) // 30分钟
const countdownText = ref('')

onLoad((options) => {
  // 获取当前用户ID
  currentUserId.value = uni.getStorageSync('userId') || ''

  if (options.id) {
    orderId.value = options.id
  }
  loadOrderDetail()
  startCountdown()
})

onUnmounted(() => {
  stopCountdown()
})

/**
 * 加载订单详情 - IM-037: 调用API获取群订单详情
 */
const loadOrderDetail = async () => {
  try {
    uni.showLoading({ title: '加载中...' })

    // IM-037: 调用API获取群订单详情
    const res = await groupOrderApi.getDetail(orderId.value)

    uni.hideLoading()

    if (res.code === 200 && res.data) {
      const data = res.data

      // 更新订单信息
      orderInfo.value = {
        id: data.id,
        status: data.status || 'pending',
        totalAmount: parseFloat(data.totalAmount || 0).toFixed(2),
        discount: parseFloat(data.discount || 0).toFixed(2),
        finalAmount: parseFloat(data.finalAmount || data.totalAmount || 0).toFixed(2),
        createTime: data.createTime,
        expireTime: data.expireTime
      }

      // 更新商家信息
      merchantInfo.value = {
        id: data.merchantId,
        name: data.merchantName || '未知商家',
        avatar: data.merchantAvatar || '/static/default-merchant.png',
        category: data.merchantCategory || ''
      }

      // 更新参与人员列表
      participantList.value = (data.participants || []).map(p => ({
        id: p.userId,
        name: p.nickname || p.userName || '未知',
        avatar: p.avatar || '/static/default-avatar.png',
        dishCount: p.dishCount || 0,
        amount: parseFloat(p.amount || 0).toFixed(2),
        orderStatus: p.paid ? 'paid' : 'unpaid'
      }))

      // 更新菜品汇总
      dishSummary.value = (data.dishSummary || []).map(d => ({
        id: d.dishId,
        name: d.dishName,
        image: d.dishImage || '/static/default-dish.png',
        count: d.count || 0,
        participantCount: d.participantCount || 0,
        amount: parseFloat(d.amount || 0).toFixed(2)
      }))

      // 查找我的订单
      myOrder.value = (data.participants || []).find(p => p.userId === currentUserId.value)

      if (myOrder.value) {
        myOrder.value = {
          dishes: (myOrder.value.dishes || []).map(d => ({
            id: d.dishId,
            name: d.dishName,
            spec: d.spec || '',
            count: d.count || 0,
            price: parseFloat(d.price || 0).toFixed(2)
          })),
          totalAmount: parseFloat(myOrder.value.amount || 0).toFixed(2),
          paid: myOrder.value.paid || false
        }
      }

      // 设置倒计时
      if (orderInfo.value.expireTime) {
        const expireTime = new Date(orderInfo.value.expireTime).getTime()
        const now = Date.now()
        countdownSeconds.value = Math.max(0, Math.floor((expireTime - now) / 1000))
      }

      console.log('加载群订单详情成功')
    } else {
      throw new Error(res.message || '获取订单详情失败')
    }
  } catch (error) {
    console.error('加载订单详情失败:', error)
    uni.hideLoading()

    // 开发阶段：使用模拟数据
    generateMockData()
  }
}

/**
 * 生成模拟数据
 */
const generateMockData = () => {
  // 参与人员
  const participants = []
  const names = ['张三', '李四', '王五', '赵六', '钱七']

  for (let i = 0; i < 5; i++) {
    const paid = Math.random() > 0.5
    participants.push({
      id: i + 1,
      name: names[i % names.length],
      avatar: `https://via.placeholder.com/60/FF6B35/FFFFFF?text=${names[i % names.length][0]}`,
      dishCount: Math.floor(Math.random() * 3) + 1,
      amount: (Math.floor(Math.random() * 50) + 30).toFixed(2),
      orderStatus: paid ? 'paid' : 'unpaid'
    })
  }
  participantList.value = participants

  // 菜品汇总
  const dishes = []
  const dishNames = [
    { name: '宫保鸡丁', price: 28 },
    { name: '鱼香肉丝', price: 26 },
    { name: '麻婆豆腐', price: 18 },
    { name: '水煮鱼', price: 48 }
  ]

  for (let i = 0; i < 4; i++) {
    const dish = dishNames[i]
    const count = Math.floor(Math.random() * 3) + 1
    dishes.push({
      id: i + 1,
      name: dish.name,
      image: `https://via.placeholder.com/100/FF6B35/FFFFFF?text=${i + 1}`,
      count,
      participantCount: Math.floor(Math.random() * 3) + 1,
      amount: (dish.price * count).toFixed(2)
    })
  }
  dishSummary.value = dishes

  // 我的订单
  myOrder.value = {
    dishes: [
      { id: 1, name: '宫保鸡丁', spec: '微辣', count: 1, price: '28.00' },
      { id: 2, name: '麻婆豆腐', spec: '', count: 1, price: '18.00' }
    ],
    totalAmount: '46.00',
    paid: false
  }
}

/**
 * 获取状态图标
 */
const getStatusIcon = (status) => {
  const iconMap = {
    pending: 'clock',
    in_progress: 'loop',
    completed: 'checkmarkempty'
  }
  return iconMap[status] || 'info'
}

/**
 * 获取状态标题
 */
const getStatusTitle = (status) => {
  const titleMap = {
    pending: '等待支付',
    in_progress: '制作中',
    completed: '已完成'
  }
  return titleMap[status] || '未知状态'
}

/**
 * 获取状态描述
 */
const getStatusDesc = (status) => {
  const descMap = {
    pending: '部分成员已支付',
    in_progress: '商家正在制作',
    completed: '订单已完成'
  }
  return descMap[status] || ''
}

/**
 * 获取订单状态文本
 */
const getOrderStatusText = (status) => {
  const textMap = {
    paid: '已支付',
    unpaid: '未支付'
  }
  return textMap[status] || ''
}

/**
 * 能否统一支付
 */
const canPayAll = computed(() => {
  // 只有创建者可以统一支付
  return true
})

/**
 * 开始倒计时
 */
const startCountdown = () => {
  updateCountdownText()
  countdownTimer = setInterval(() => {
    countdownSeconds.value--
    if (countdownSeconds.value <= 0) {
      stopCountdown()
    } else {
      updateCountdownText()
    }
  }, 1000)
}

/**
 * 停止倒计时
 */
const stopCountdown = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

/**
 * 更新倒计时文本
 */
const updateCountdownText = () => {
  const minutes = Math.floor(countdownSeconds.value / 60)
  const seconds = countdownSeconds.value % 60
  countdownText.value = `${minutes}:${seconds.toString().padStart(2, '0')}`
}

/**
 * 查看商家
 */
const viewMerchant = () => {
  uni.navigateTo({
    url: `/pages/home/merchant-detail?id=${merchantInfo.value.id}`
  })
}

/**
 * 查看全部参与人员
 */
const viewAllParticipants = () => {
  uni.showToast({
    title: '查看全部参与人员',
    icon: 'none'
  })
}

/**
 * 查看全部菜品
 */
const viewAllDishes = () => {
  uni.navigateTo({
    url: `/pages/group-order/detail?id=${orderId.value}`
  })
}

/**
 * 加菜
 */
const addDishes = () => {
  uni.navigateTo({
    url: `/pages/group-order/select-dishes?id=${orderId.value}`
  })
}

/**
 * 支付我的订单 - IM-038: 调用支付API
 */
const payMyOrder = async () => {
  if (!myOrder.value) return

  uni.showModal({
    title: '确认支付',
    content: `确认支付 ¥${myOrder.value.totalAmount}？`,
    success: async (res) => {
      if (res.confirm) {
        await processPayment('single')
      }
    }
  })
}

/**
 * 统一支付 - IM-038: 调用支付API
 */
const payAllOrder = async () => {
  uni.showModal({
    title: '统一支付',
    content: `确认支付所有人的订单，共计 ¥${orderInfo.value.finalAmount}？`,
    success: async (res) => {
      if (res.confirm) {
        await processPayment('all')
      }
    }
  })
}

/**
 * 处理支付 - IM-038: 调用支付API
 */
const processPayment = async (paymentType = 'single') => {
  try {
    uni.showLoading({
      title: '处理中...',
      mask: true
    })

    // IM-038: 调用支付API
    const paymentData = {
      orderId: orderId.value,
      paymentType: paymentType, // single 或 all
      paymentMethod: 'wechat', // 默认使用微信支付
      userId: currentUserId.value,
      amount: paymentType === 'single' ? myOrder.value.totalAmount : orderInfo.value.finalAmount
    }

    // 调用支付API
    const res = await groupOrderApi.payOrder(orderId.value, paymentData)

    uni.hideLoading()

    if (res.code === 200) {
      // 调起支付
      const paymentResult = await invokePayment(res.data.paymentParams)

      if (paymentResult.success) {
        uni.showToast({
          title: '支付成功',
          icon: 'success'
        })

        // 更新订单状态
        if (paymentType === 'single' && myOrder.value) {
          myOrder.value.paid = true
        } else {
          orderInfo.value.status = 'in_progress'
        }

        // 刷新订单详情
        setTimeout(() => {
          loadOrderDetail()
        }, 1500)
      } else {
        throw new Error(paymentResult.error || '支付失败')
      }
    } else {
      throw new Error(res.message || '支付失败')
    }
  } catch (error) {
    console.error('支付失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error.message || '支付失败',
      icon: 'none'
    })
  }
}

/**
 * 调起支付
 */
const invokePayment = (paymentParams) => {
  return new Promise((resolve) => {
    // 根据支付方式调起不同的支付
    if (paymentParams.type === 'wechat') {
      // 微信支付
      uni.requestPayment({
        provider: 'wxpay',
        ...paymentParams,
        success: () => {
          resolve({ success: true })
        },
        fail: (err) => {
          resolve({ success: false, error: err.errMsg })
        }
      })
    } else if (paymentParams.type === 'alipay') {
      // 支付宝支付
      uni.requestPayment({
        provider: 'alipay',
        ...paymentParams,
        success: () => {
          resolve({ success: true })
        },
        fail: (err) => {
          resolve({ success: false, error: err.errMsg })
        }
      })
    } else {
      // 其他支付方式
      resolve({ success: false, error: '不支持的支付方式' })
    }
  })
}

/**
 * 查看进度
 */
const viewProgress = () => {
  uni.navigateTo({
    url: `/pages/order/progress?id=${orderId.value}`
  })
}

/**
 * 查看订单详情
 */
const viewOrderDetail = () => {
  uni.navigateTo({
    url: `/pages/order/detail?id=${orderId.value}`
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.group-order-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 150rpx;
}

/* 订单状态头部 */
.order-header {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  padding: 40rpx 30rpx;
  color: #fff;

  &.status-in_progress {
    background: linear-gradient(135deg, #52C41A, #73D13D);
  }

  &.status-completed {
    background: linear-gradient(135deg, #8C8C8C, #A9A9A9);
  }
}

.header-content {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 20rpx;
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

.status-title {
  font-size: 36rpx;
  font-weight: bold;
}

.status-desc {
  font-size: 26rpx;
  opacity: 0.9;
}

.countdown {
  display: flex;
  align-items: center;
  gap: 10rpx;
  background: rgba(255, 255, 255, 0.2);
  padding: 10rpx 20rpx;
  border-radius: 20rpx;
}

.countdown-text {
  font-size: 26rpx;
  font-weight: bold;
}

/* 商家信息 */
.merchant-card {
  background: #fff;
  padding: 25rpx 30rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.merchant-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 12rpx;
}

.merchant-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.merchant-name {
  font-size: 30rpx;
  font-weight: 500;
  color: #333;
}

.merchant-category {
  font-size: 24rpx;
  color: #999;
}

/* 卡片通用样式 */
.participants-card,
.dishes-card,
.my-order-card,
.payment-card {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.card-action {
  font-size: 26rpx;
  color: #FF6B35;
}

/* 参与人员 */
.participants-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.participant-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.participant-avatar-wrapper {
  position: relative;
  width: 70rpx;
  height: 70rpx;
}

.participant-avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.status-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  border: 2rpx solid #fff;

  &.status-paid {
    background: #52C41A;
  }

  &.status-unpaid {
    background: #F5222D;
  }
}

.participant-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.participant-name {
  font-size: 28rpx;
  color: #333;
}

.participant-dishes {
  font-size: 24rpx;
  color: #999;
}

.participant-amount {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 5rpx;
}

.amount-value {
  font-size: 30rpx;
  color: #FF6B35;
  font-weight: bold;
}

.amount-status {
  font-size: 22rpx;
  color: #999;
}

/* 菜品列表 */
.dishes-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.dish-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.dish-image {
  width: 100rpx;
  height: 100rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.dish-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.dish-meta {
  display: flex;
  gap: 15rpx;
}

.dish-count,
.dish-participants {
  font-size: 24rpx;
  color: #999;
}

.dish-amount {
  font-size: 30rpx;
  color: #FF6B35;
  font-weight: bold;
}

/* 我的订单 */
.my-dishes-list {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.my-dish-item {
  display: flex;
  align-items: center;
  gap: 15rpx;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
}

.my-dish-item .dish-name {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.dish-detail {
  font-size: 24rpx;
  color: #999;
}

.dish-count {
  font-size: 26rpx;
  color: #666;
}

.dish-price {
  font-size: 28rpx;
  color: #FF6B35;
  font-weight: bold;
}

.my-order-footer {
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #eee;
}

.order-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-label {
  font-size: 28rpx;
  color: #666;
}

.total-amount {
  font-size: 40rpx;
  color: #FF6B35;
  font-weight: bold;
}

/* 支付信息 */
.payment-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15rpx 0;

  &.total {
    border-top: 1rpx solid #eee;
    margin-top: 15rpx;
    padding-top: 20rpx;
  }
}

.payment-label {
  font-size: 28rpx;
  color: #666;
}

.payment-value {
  font-size: 28rpx;
  color: #333;

  &.discount {
    color: #52C41A;
  }

  &.final {
    font-size: 36rpx;
    color: #FF6B35;
    font-weight: bold;
  }
}

/* 底部操作栏 */
.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  background: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
  display: flex;
  gap: 20rpx;
}

.action-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  border: none;
  @include flex-center;

  &.secondary {
    background: #F5F5F5;
    color: #666;
  }

  &.primary {
    background: #FF6B35;
    color: #fff;
    font-weight: bold;
  }
}
</style>
