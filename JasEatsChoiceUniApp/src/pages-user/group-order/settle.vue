<template>
  <view class="settle-group-order-container">
    <!-- 订单概览 -->
    <view class="order-overview">
      <view class="overview-header">
        <text class="title">群订单概览</text>
        <text class="order-code">订单码：{{ orderInfo.orderCode }}</text>
      </view>

      <!-- 成员列表 -->
      <view class="members-section">
        <text class="section-title">成员订单 ({{ orderInfo.members.length }}人)</text>
        <scroll-view scroll-x class="members-scroll">
          <view
            class="member-item"
            v-for="member in orderInfo.members"
            :key="member.id"
            :class="{ paid: member.paid }"
          >
            <image class="member-avatar" :src="member.avatar" mode="aspectFill"></image>
            <text class="member-name">{{ member.name }}</text>
            <text class="member-status">{{ member.paid ? '已支付' : '待支付' }}</text>
          </view>
        </scroll-view>
      </view>

      <!-- 菜品汇总 -->
      <view class="dishes-summary">
        <text class="section-title">菜品汇总</text>
        <view class="dish-list">
          <view class="dish-item" v-for="dish in orderInfo.dishes" :key="dish.id">
            <image class="dish-image" :src="dish.image" mode="aspectFill"></image>
            <view class="dish-info">
              <text class="dish-name">{{ dish.name }}</text>
              <text class="dish-spec">{{ dish.specification }}</text>
            </view>
            <view class="dish-quantity">
              <text class="quantity">×{{ dish.totalQuantity }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 费用明细 - GROUP-003 -->
    <view class="fee-details">
      <text class="section-title">费用明细</text>
      <view class="fee-item">
        <text class="label">菜品总价</text>
        <text class="value">¥{{ settlementInfo.subtotal }}</text>
      </view>
      <view class="fee-item">
        <text class="label">配送费</text>
        <text class="value">¥{{ settlementInfo.deliveryFee }}</text>
      </view>
      <view class="fee-item">
        <text class="label">包装费</text>
        <text class="value">¥{{ settlementInfo.packagingFee }}</text>
      </view>
      <view class="fee-item discount" v-if="settlementInfo.discount > 0">
        <text class="label">优惠</text>
        <text class="value">-¥{{ settlementInfo.discount }}</text>
      </view>
      <view class="fee-item total">
        <text class="label">总计</text>
        <text class="value">¥{{ settlementInfo.totalAmount }}</text>
      </view>

      <!-- 我的订单 -->
      <view class="my-order" v-if="myOrder">
        <view class="my-order-header">
          <text class="title">我的订单</text>
          <text class="amount">¥{{ myOrder.totalAmount }}</text>
        </view>
        <view class="my-order-dishes">
          <text class="dish-text" v-for="dish in myOrder.dishes" :key="dish.dishId">
            {{ dish.name }} ×{{ dish.quantity }}
          </text>
        </view>
      </view>
    </view>

    <!-- 优惠选择 -->
    <view class="coupon-section">
      <view class="coupon-item" @tap="selectCoupon">
        <text class="label">优惠券</text>
        <view class="coupon-value">
          <text class="value" v-if="selectedCoupon">{{ selectedCoupon.name }} (-¥{{ selectedCoupon.amount }})</text>
          <text class="placeholder" v-else>{{ availableCoupons.length }}张可用</text>
          <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 支付方式 -->
    <view class="payment-methods">
      <text class="section-title">支付方式</text>
      <radio-group @change="onPaymentMethodChange">
        <label class="payment-item" v-for="method in paymentMethods" :key="method.value">
          <view class="payment-info">
            <text class="icon">{{ method.icon }}</text>
            <text class="name">{{ method.label }}</text>
          </view>
          <radio :value="method.value" :checked="paymentMethod === method.value" color="#FF6B35" />
        </label>
      </radio-group>
    </view>

    <!-- 底部支付栏 -->
    <view class="payment-bar">
      <view class="price-info">
        <text class="label">应付金额：</text>
        <text class="amount">¥{{ finalAmount }}</text>
      </view>
      <button class="pay-btn" @tap="processPayment">立即支付</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { groupOrderApi } from '@/api/modules/group-order-api.js'
import { paymentApi } from '@/api/modules/payment.js'

const orderId = ref('')
const userId = ref('')

// 订单信息
const orderInfo = ref({
  orderCode: '',
  members: [],
  dishes: []
})

// 结算信息 - GROUP-003
const settlementInfo = ref({
  subtotal: '0.00',
  deliveryFee: '0.00',
  packagingFee: '0.00',
  discount: '0.00',
  totalAmount: '0.00'
})

// 我的订单
const myOrder = ref(null)

// 优惠券
const availableCoupons = ref([])
const selectedCoupon = ref(null)

// 支付方式
const paymentMethod = ref('wechat')
const paymentMethods = [
  { value: 'wechat', label: '微信支付', icon: '💚' },
  { value: 'alipay', label: '支付宝', icon: '💙' },
  { value: 'balance', label: '余额支付', icon: '💰' }
]

// 最终金额
const finalAmount = computed(() => {
  if (myOrder.value) {
    return myOrder.value.totalAmount
  }
  return settlementInfo.value.totalAmount
})

onMounted(async () => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || {}

  orderId.value = options.id || ''
  userId.value = uni.getStorageSync('userId') || ''

  // 加载数据
  await loadData()
})

/**
 * 加载数据
 */
const loadData = async () => {
  try {
    uni.showLoading({ title: '加载中...' })

    // 并行加载订单详情和结算信息
    await Promise.all([
      loadOrderDetail(),
      loadSettlement()
    ])

    // 加载可用优惠券
    await loadAvailableCoupons()

    uni.hideLoading()
  } catch (error) {
    console.error('加载数据失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

/**
 * 加载订单详情
 */
const loadOrderDetail = async () => {
  const res = await groupOrderApi.getDetail(orderId.value)

  if (res.code === 200 && res.data) {
    const data = res.data
    orderInfo.value = {
      orderCode: data.orderCode || '',
      members: (data.members || []).map(m => ({
        id: m.id,
        name: m.name,
        avatar: m.avatar || 'https://via.placeholder.com/100',
        paid: m.paid || false
      })),
      dishes: (data.dishes || []).map(d => ({
        id: d.dishId,
        name: d.name,
        image: d.image || 'https://via.placeholder.com/100',
        specification: d.specification || '',
        totalQuantity: d.totalQuantity || 0
      }))
    }

    // 查找我的订单
    const myOrderData = (data.members || []).find(m => m.userId === userId.value)
    if (myOrderData) {
      myOrder.value = {
        totalAmount: myOrderData.totalAmount || '0.00',
        dishes: myOrderData.dishes || []
      }
    }
  }
}

/**
 * GROUP-003: 加载结算信息
 */
const loadSettlement = async () => {
  const res = await groupOrderApi.getSettlement(orderId.value)

  if (res.code === 200 && res.data) {
    const data = res.data
    settlementInfo.value = {
      subtotal: parseFloat(data.subtotal || 0).toFixed(2),
      deliveryFee: parseFloat(data.deliveryFee || 0).toFixed(2),
      packagingFee: parseFloat(data.packagingFee || 0).toFixed(2),
      discount: parseFloat(data.discount || 0).toFixed(2),
      totalAmount: parseFloat(data.totalAmount || 0).toFixed(2)
    }
  }
}

/**
 * 加载可用优惠券
 */
const loadAvailableCoupons = async () => {
  try {
    const amount = myOrder.value ? parseFloat(myOrder.value.totalAmount) : parseFloat(settlementInfo.value.totalAmount)
    const res = await paymentApi.getAvailableCoupons({
      userId: userId.value,
      orderAmount: amount
    })

    if (res.code === 200 && res.data) {
      availableCoupons.value = res.data || []
    }
  } catch (error) {
    console.error('加载优惠券失败:', error)
  }
}

/**
 * 选择优惠券
 */
const selectCoupon = () => {
  if (availableCoupons.value.length === 0) {
    uni.showToast({
      title: '暂无可用优惠券',
      icon: 'none'
    })
    return
  }

  const items = availableCoupons.value.map(c => `${c.name} (-¥${c.amount})`)
  uni.showActionSheet({
    itemList: items,
    success: (res) => {
      selectedCoupon.value = availableCoupons.value[res.tapIndex]
      // 重新计算金额
      recalculateAmount()
    }
  })
}

/**
 * 重新计算金额
 */
const recalculateAmount = () => {
  if (selectedCoupon.value && myOrder.value) {
    const originalAmount = parseFloat(myOrder.value.totalAmount)
    const discount = parseFloat(selectedCoupon.value.amount)
    const newAmount = Math.max(0, originalAmount - discount)
    myOrder.value.totalAmount = newAmount.toFixed(2)
  }
}

/**
 * 支付方式变更
 */
const onPaymentMethodChange = (e) => {
  paymentMethod.value = e.detail.value
}

/**
 * GROUP-004: 处理支付
 */
const processPayment = async () => {
  if (!myOrder.value) {
    uni.showToast({
      title: '未找到订单信息',
      icon: 'none'
    })
    return
  }

  if (myOrder.value.paid) {
    uni.showToast({
      title: '您已支付过此订单',
      icon: 'none'
    })
    return
  }

  try {
    uni.showLoading({
      title: '支付中...',
      mask: true
    })

    // GROUP-004: 调用API处理支付
    const res = await groupOrderApi.pay(orderId.value, {
      userId: userId.value,
      paymentType: 'single',
      paymentMethod: paymentMethod.value,
      couponId: selectedCoupon.value ? selectedCoupon.value.id : ''
    })

    if (res.code === 200 && res.data) {
      uni.hideLoading()

      // 调用支付接口
      const paymentParams = res.data.paymentParams

      if (paymentMethod.value === 'wechat') {
        await invokeWechatPayment(paymentParams)
      } else if (paymentMethod.value === 'alipay') {
        await invokeAlipayPayment(paymentParams)
      } else if (paymentMethod.value === 'balance') {
        await handleBalancePayment(res.data)
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
 * 调用微信支付
 */
const invokeWechatPayment = (paymentParams) => {
  return new Promise((resolve, reject) => {
    uni.requestPayment({
      provider: 'wxpay',
      ...paymentParams,
      success: () => {
        handlePaymentSuccess()
        resolve()
      },
      fail: (err) => {
        if (err.errMsg.includes('cancel')) {
          uni.showToast({
            title: '已取消支付',
            icon: 'none'
          })
        } else {
          uni.showToast({
            title: '支付失败',
            icon: 'none'
          })
        }
        reject(err)
      }
    })
  })
}

/**
 * 调用支付宝支付
 */
const invokeAlipayPayment = (paymentParams) => {
  return new Promise((resolve, reject) => {
    // H5环境
    // #ifdef H5
    if (paymentParams.payUrl) {
      window.location.href = paymentParams.payUrl
    }
    // #endif

    // APP环境
    // #ifdef APP-PLUS
    uni.requestPayment({
      provider: 'alipay',
      orderInfo: paymentParams.orderInfo,
      success: () => {
        handlePaymentSuccess()
        resolve()
      },
      fail: (err) => {
        uni.showToast({
          title: '支付失败',
          icon: 'none'
        })
        reject(err)
      }
    })
    // #endif

    resolve()
  })
}

/**
 * 处理余额支付
 */
const handleBalancePayment = async (paymentData) => {
  try {
    // 轮询查询支付状态
    await paymentApi.pollPaymentStatus(paymentData.paymentNo, {
      interval: 2000,
      maxAttempts: 15,
      onSuccess: (data) => {
        handlePaymentSuccess()
      },
      onFailed: (data) => {
        uni.showToast({
          title: '支付失败',
          icon: 'none'
        })
      },
      onTimeout: () => {
        uni.showToast({
          title: '支付超时',
          icon: 'none'
        })
      }
    })
  } catch (error) {
    console.error('余额支付失败:', error)
  }
}

/**
 * 支付成功处理
 */
const handlePaymentSuccess = () => {
  uni.showToast({
    title: '支付成功',
    icon: 'success'
  })

  // 更新我的订单状态
  if (myOrder.value) {
    myOrder.value.paid = true
  }

  // 延迟跳转到订单详情
  setTimeout(() => {
    uni.redirectTo({
      url: `/pages-user/group-order/detail?id=${orderId.value}`
    })
  }, 1500)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.settle-group-order-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 120rpx;
}

/* 订单概览 */
.order-overview {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.overview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #eee;
}

.title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.order-code {
  font-size: 26rpx;
  color: #999;
}

/* 成员列表 */
.members-section {
  margin-bottom: 30rpx;
}

.section-title {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 20rpx;
}

.members-scroll {
  white-space: nowrap;
}

.member-item {
  display: inline-block;
  width: 140rpx;
  padding: 20rpx;
  margin-right: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  text-align: center;
  border: 2rpx solid transparent;

  &.paid {
    background: rgba(82, 196, 26, 0.1);
    border-color: #52C41A;
  }
}

.member-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-bottom: 10rpx;
}

.member-name {
  display: block;
  font-size: 24rpx;
  color: #333;
  margin-bottom: 5rpx;
}

.member-status {
  display: block;
  font-size: 22rpx;
  color: #999;
}

/* 菜品汇总 */
.dishes-summary {
  margin-bottom: 20rpx;
}

.dish-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.dish-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
}

.dish-image {
  width: 100rpx;
  height: 100rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
}

.dish-name {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 5rpx;
}

.dish-spec {
  display: block;
  font-size: 24rpx;
  color: #999;
}

.dish-quantity {
  font-size: 28rpx;
  color: #FF6B35;
  font-weight: bold;
}

/* 费用明细 */
.fee-details {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.fee-item {
  display: flex;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #eee;

  &:last-child {
    border-bottom: none;
  }

  &.discount .value {
    color: #52C41A;
  }

  &.total {
    padding-top: 30rpx;
    margin-top: 10rpx;
    border-top: 2rpx solid #eee;
  }
}

.fee-item .label {
  font-size: 28rpx;
  color: #666;
}

.fee-item .value {
  font-size: 28rpx;
  color: #333;
}

.fee-item.total .label,
.fee-item.total .value {
  font-size: 32rpx;
  font-weight: bold;
  color: #FF6B35;
}

/* 我的订单 */
.my-order {
  margin-top: 30rpx;
  padding: 20rpx;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C5A 100%);
  border-radius: 12rpx;
}

.my-order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15rpx;
}

.my-order-header .title {
  font-size: 28rpx;
  color: #fff;
}

.my-order-header .amount {
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
}

.my-order-dishes {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.dish-text {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.9);
}

/* 优惠券 */
.coupon-section {
  background: #fff;
  padding: 0 30rpx;
  margin-bottom: 20rpx;
}

.coupon-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 1rpx solid #eee;
}

.coupon-item .label {
  font-size: 28rpx;
  color: #333;
}

.coupon-value {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.coupon-value .value {
  font-size: 26rpx;
  color: #FF6B35;
}

.coupon-value .placeholder {
  font-size: 26rpx;
  color: #999;
}

/* 支付方式 */
.payment-methods {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.payment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25rpx 0;
  border-bottom: 1rpx solid #eee;

  &:last-child {
    border-bottom: none;
  }
}

.payment-info {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.payment-info .icon {
  font-size: 36rpx;
}

.payment-info .name {
  font-size: 28rpx;
  color: #333;
}

/* 支付栏 */
.payment-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.price-info {
  display: flex;
  align-items: baseline;
  gap: 5rpx;
}

.price-info .label {
  font-size: 26rpx;
  color: #666;
}

.price-info .amount {
  font-size: 36rpx;
  font-weight: bold;
  color: #FF6B35;
}

.pay-btn {
  padding: 0 50rpx;
  height: 80rpx;
  background: #FF6B35;
  color: #fff;
  border-radius: 40rpx;
  font-size: 28rpx;
  border: none;
}
</style>
