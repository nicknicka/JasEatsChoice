<template>
  <view class="settle-container">
    <!-- 顶部提示 -->
    <view class="tips-header">
      <uni-icons type="info" size="18" color="#FF6B35"></uni-icons>
      <text class="tips-text">确认订单信息后，将统一支付并分别下单</text>
    </view>

    <!-- 订单汇总 -->
    <view class="summary-card">
      <view class="summary-title">订单汇总</view>
      <view class="summary-list">
        <view class="summary-item">
          <text class="item-label">菜品总数</text>
          <text class="item-value">{{ orderSummary.dishCount }}道</text>
        </view>
        <view class="summary-item">
          <text class="item-label">参与人数</text>
          <text class="item-value">{{ orderSummary.participantCount }}人</text>
        </view>
        <view class="summary-item">
          <text class="item-label">订单总额</text>
          <text class="item-value amount">¥{{ orderSummary.totalAmount }}</text>
        </view>
      </view>
    </view>

    <!-- 个人明细 -->
    <view class="my-bill-card">
      <view class="bill-title">我的明细</view>
      <view class="dishes-list">
        <view
          class="dish-item"
          v-for="dish in myDishes"
          :key="dish.id"
        >
          <view class="dish-info">
            <text class="dish-name">{{ dish.name }}</text>
            <text class="dish-spec" v-if="dish.spec">{{ dish.spec }}</text>
          </view>
          <view class="dish-amount">
            <text class="amount-price">¥{{ dish.price }}</text>
            <text class="amount-count">x{{ dish.count }}</text>
          </view>
        </view>
      </view>
      <view class="bill-footer">
        <view class="bill-total">
          <text class="total-label">我的金额</text>
          <text class="total-amount">¥{{ myAmount }}</text>
        </view>
      </view>
    </view>

    <!-- 支付方式 -->
    <view class="payment-method-card">
      <view class="method-title">支付方式</view>
      <view class="method-list">
        <view
          class="method-item"
          :class="{ active: paymentMethod === 'wechat' }"
          @tap="selectPayment('wechat')"
        >
          <view class="method-icon wechat">
            <uni-icons type="weixin" size="24" color="#fff"></uni-icons>
          </view>
          <text class="method-label">微信支付</text>
          <view class="method-check" v-if="paymentMethod === 'wechat'">
            <uni-icons type="checkbox-filled" size="20" color="#09BB07"></uni-icons>
          </view>
        </view>
        <view
          class="method-item"
          :class="{ active: paymentMethod === 'alipay' }"
          @tap="selectPayment('alipay')"
        >
          <view class="method-icon alipay">
            <uni-icons type="wallet-filled" size="24" color="#fff"></uni-icons>
          </view>
          <text class="method-label">支付宝</text>
          <view class="method-check" v-if="paymentMethod === 'alipay'">
            <uni-icons type="checkbox-filled" size="20" color="#1677FF"></uni-icons>
          </view>
        </view>
        <view
          class="method-item"
          :class="{ active: paymentMethod === 'balance' }"
          @tap="selectPayment('balance')"
        >
          <view class="method-icon balance">
            <uni-icons type="wallet" size="24" color="#fff"></uni-icons>
          </view>
          <view class="method-info">
            <text class="method-label">余额支付</text>
            <text class="balance-amount">¥{{ userBalance }}</text>
          </view>
          <view class="method-check" v-if="paymentMethod === 'balance'">
            <uni-icons type="checkbox-filled" size="20" color="#FF6B35"></uni-icons>
          </view>
        </view>
      </view>
    </view>

    <!-- 订单备注 -->
    <view class="remark-card">
      <view class="remark-title">订单备注</view>
      <textarea
        class="remark-input"
        v-model="orderRemark"
        placeholder="添加备注，比如：少放辣椒、多放葱花..."
        maxlength="200"
        :show-confirm-bar="false"
      />
      <view class="word-count">{{ orderRemark.length }}/200</view>
    </view>

    <!-- 优惠信息 -->
    <view class="coupon-card" @tap="selectCoupon">
      <view class="coupon-info">
        <uni-icons type="gift-filled" size="20" color="#FF6B35"></uni-icons>
        <text class="coupon-text">{{ selectedCoupon ? selectedCoupon.name : '选择优惠券' }}</text>
      </view>
      <view class="coupon-right">
        <text class="coupon-discount" v-if="selectedCoupon">
          -¥{{ selectedCoupon.discount }}
        </text>
        <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
      </view>
    </view>

    <!-- 底部结算栏 -->
    <view class="settle-bar">
      <view class="settle-info">
        <text class="settle-label">实付金额</text>
        <text class="settle-amount">¥{{ finalAmount }}</text>
      </view>
      <button
        class="settle-btn"
        @tap="confirmSettle"
      >
        确认支付
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// 订单汇总
const orderSummary = ref({
  dishCount: 8,
  participantCount: 3,
  totalAmount: '256.00'
})

// 我的菜品
const myDishes = ref([
  {
    id: 1,
    name: '宫保鸡丁',
    spec: '微辣',
    price: '28.00',
    count: 1
  },
  {
    id: 2,
    name: '鱼香肉丝',
    spec: '',
    price: '26.00',
    count: 1
  }
])

// 我的金额
const myAmount = computed(() => {
  return myDishes.value.reduce((sum, dish) => {
    return sum + parseFloat(dish.price) * dish.count
  }, 0).toFixed(2)
})

// 用户余额
const userBalance = ref('58.50')

// 支付方式
const paymentMethod = ref('wechat')

// 订单备注
const orderRemark = ref('')

// 选择的优惠券
const selectedCoupon = ref(null)

// 最终金额
const finalAmount = computed(() => {
  let amount = parseFloat(myAmount.value)
  if (selectedCoupon.value) {
    amount -= parseFloat(selectedCoupon.value.discount)
  }
  return amount.toFixed(2)
})

onMounted(() => {
  loadOrderData()
})

/**
 * 加载订单数据
 */
const loadOrderData = () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  const orderId = options.id

  // TODO: 调用API获取订单结算信息
  // const res = await userApi.getGroupOrderSettle({ id: orderId })
  // orderSummary.value = res.data.summary
  // myDishes.value = res.data.myDishes
}

/**
 * 选择支付方式
 */
const selectPayment = (method) => {
  paymentMethod.value = method
}

/**
 * 选择优惠券
 */
const selectCoupon = () => {
  uni.navigateTo({
    url: '/pages/coupon/select?amount=' + myAmount.value
  })
}

/**
 * 确认结算
 */
const confirmSettle = () => {
  if (!paymentMethod.value) {
    uni.showToast({
      title: '请选择支付方式',
      icon: 'none'
    })
    return
  }

  // 余额支付检查
  if (paymentMethod.value === 'balance') {
    if (parseFloat(userBalance.value) < parseFloat(finalAmount.value)) {
      uni.showToast({
        title: '余额不足',
        icon: 'none'
      })
      return
    }
  }

  uni.showModal({
    title: '确认支付',
    content: `确认支付 ¥${finalAmount.value}？`,
    success: (res) => {
      if (res.confirm) {
        processPayment()
      }
    }
  })
}

/**
 * 处理支付
 */
const processPayment = () => {
  uni.showLoading({
    title: '支付中...'
  })

  // TODO: 调用API处理支付
  const paymentData = {
    orderId: orderSummary.value.id,
    paymentMethod: paymentMethod.value,
    amount: finalAmount.value,
    remark: orderRemark.value,
    couponId: selectedCoupon.value?.id
  }

  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '支付成功',
      icon: 'success'
    })

    setTimeout(() => {
      uni.redirectTo({
        url: '/pages/group-order/success'
      })
    }, 1500)
  }, 2000)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.settle-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 140rpx;
}

/* 提示头部 */
.tips-header {
  background: #FFF7E6;
  padding: 20rpx 30rpx;
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin: 20rpx;
  border-radius: 12rpx;
}

.tips-text {
  flex: 1;
  font-size: 26rpx;
  color: #FF6B35;
  line-height: 1.5;
}

/* 订单汇总 */
.summary-card {
  background: #fff;
  padding: 30rpx;
  margin: 0 20rpx 20rpx;
  border-radius: 16rpx;
}

.summary-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.summary-list {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-label {
  font-size: 28rpx;
  color: #666;
}

.item-value {
  font-size: 28rpx;
  color: #333;

  &.amount {
    font-size: 36rpx;
    color: #FF6B35;
    font-weight: bold;
  }
}

/* 个人明细 */
.my-bill-card {
  background: #fff;
  padding: 30rpx;
  margin: 0 20rpx 20rpx;
  border-radius: 16rpx;
}

.bill-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.dishes-list {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.dish-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.dish-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.dish-spec {
  font-size: 24rpx;
  color: #999;
}

.dish-amount {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.amount-price {
  font-size: 28rpx;
  color: #FF6B35;
  font-weight: bold;
}

.amount-count {
  font-size: 24rpx;
  color: #999;
}

.bill-footer {
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #eee;
}

.bill-total {
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

/* 支付方式 */
.payment-method-card {
  background: #fff;
  padding: 30rpx;
  margin: 0 20rpx 20rpx;
  border-radius: 16rpx;
}

.method-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.method-list {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.method-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 25rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  border: 2rpx solid transparent;

  &.active {
    background: #FFF7E6;
    border-color: #FF6B35;
  }
}

.method-icon {
  width: 60rpx;
  height: 60rpx;
  border-radius: 12rpx;
  @include flex-center;

  &.wechat {
    background: #09BB07;
  }

  &.alipay {
    background: #1677FF;
  }

  &.balance {
    background: #FF6B35;
  }
}

.method-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.method-label {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.balance-amount {
  font-size: 24rpx;
  color: #999;
}

/* 订单备注 */
.remark-card {
  background: #fff;
  padding: 30rpx;
  margin: 0 20rpx 20rpx;
  border-radius: 16rpx;
}

.remark-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.remark-input {
  width: 100%;
  min-height: 150rpx;
  padding: 15rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
}

.word-count {
  text-align: right;
  padding-top: 10rpx;
  font-size: 24rpx;
  color: #999;
}

/* 优惠券 */
.coupon-card {
  background: #fff;
  padding: 25rpx 30rpx;
  margin: 0 20rpx 20rpx;
  border-radius: 16rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.coupon-info {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.coupon-text {
  font-size: 28rpx;
  color: #333;
}

.coupon-right {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.coupon-discount {
  font-size: 28rpx;
  color: #FF6B35;
  font-weight: bold;
}

/* 结算栏 */
.settle-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  background: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.settle-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.settle-label {
  font-size: 24rpx;
  color: #999;
}

.settle-amount {
  font-size: 40rpx;
  color: #FF6B35;
  font-weight: bold;
}

.settle-btn {
  padding: 0 50rpx;
  height: 80rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  border-radius: 40rpx;
  border: none;
  @include flex-center;
}
</style>
