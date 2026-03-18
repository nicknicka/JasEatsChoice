<template>
  <view class="payment-result">
    <!-- 状态图标 -->
    <view class="status-icon" :class="{ success: isSuccess, fail: !isSuccess }">
      <text class="icon">{{ isSuccess ? '✓' : '✗' }}</text>
    </view>

    <!-- 状态文本 -->
    <view class="status-text">{{ isSuccess ? '支付成功' : '支付失败' }}</view>
    <view class="status-desc" v-if="isSuccess">
      感谢您的支付，商家将尽快为您配送
    </view>
    <view class="status-desc" v-else>
      {{ failReason || '支付过程中出现错误，请重试' }}
    </view>

    <!-- 订单信息 -->
    <view class="order-info">
      <view class="info-item">
        <text class="label">订单编号</text>
        <text class="value">{{ orderNo }}</text>
      </view>
      <view class="info-item">
        <text class="label">支付金额</text>
        <text class="value amount">¥{{ amount }}</text>
      </view>
      <view class="info-item" v-if="isSuccess">
        <text class="label">支付方式</text>
        <text class="value">{{ paymentMethod }}</text>
      </view>
      <view class="info-item" v-if="isSuccess">
        <text class="label">支付时间</text>
        <text class="value">{{ payTime }}</text>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button class="btn btn-outline" @click="viewOrder">查看订单</button>
      <button class="btn btn-primary" @click="returnHome">返回首页</button>
    </view>

    <!-- 失败时的额外操作 -->
    <view class="fail-actions" v-if="!isSuccess">
      <button class="btn btn-primary" @click="retryPayment">重新支付</button>
      <button class="btn btn-outline" @click="contactSupport">联系客服</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onLoad } from 'vue'
import { useRouter } from 'uni-mini-router'

const router = useRouter()

// 支付状态
const isSuccess = ref(true)
const failReason = ref('')
const orderNo = ref('')
const amount = ref('0.00')
const paymentMethod = ref('')
const payTime = ref('')

// 自动跳转定时器
let autoRedirectTimer = null

onLoad((options) => {
  // 获取支付结果参数
  isSuccess.value = options.status === 'success'
  failReason.value = options.failReason || ''
  orderNo.value = options.orderNo || ''
  amount.value = options.amount || '0.00'
  paymentMethod.value = options.paymentMethod || ''
  payTime.value = options.payTime || new Date().toLocaleString('zh-CN')

  // 支付成功后自动跳转倒计时
  if (isSuccess.value) {
    startAutoRedirect()
  }
})

// 开始自动跳转倒计时
const startAutoRedirect = () => {
  let countdown = 5
  autoRedirectTimer = setTimeout(() => {
    // 5秒后自动返回首页
    uni.reLaunch({
      url: '/pages/index/index'
    })
  }, countdown * 1000)
}

// 查看订单
const viewOrder = () => {
  if (autoRedirectTimer) {
    clearTimeout(autoRedirectTimer)
  }
  uni.redirectTo({
    url: `/pages-user/order/detail?id=${orderNo.value}`
  })
}

// 返回首页
const returnHome = () => {
  if (autoRedirectTimer) {
    clearTimeout(autoRedirectTimer)
  }
  uni.reLaunch({
    url: '/pages/index/index'
  })
}

// 重新支付
const retryPayment = () => {
  uni.navigateBack()
}

// 联系客服
const contactSupport = () => {
  uni.showModal({
    title: '联系客服',
    content: '客服电话：400-123-4567\n工作时间：9:00-21:00',
    confirmText: '拨打电话',
    success: (res) => {
      if (res.confirm) {
        uni.makePhoneCall({
          phoneNumber: '400-123-4567'
        })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.payment-result {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 80rpx 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.status-icon {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32rpx;

  &.success {
    background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  }

  &.fail {
    background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  }

  .icon {
    font-size: 64rpx;
    color: #ffffff;
    font-weight: bold;
  }
}

.status-text {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 16rpx;
}

.status-desc {
  font-size: 28rpx;
  color: #999999;
  margin-bottom: 48rpx;
  text-align: center;
}

.order-info {
  width: 100%;
  background: #ffffff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 48rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);

  .info-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .label {
      font-size: 28rpx;
      color: #666666;
    }

    .value {
      font-size: 28rpx;
      color: #333333;
      font-weight: 500;

      &.amount {
        font-size: 32rpx;
        font-weight: bold;
        color: #ff6b6b;
      }
    }
  }
}

.action-buttons {
  width: 100%;
  display: flex;
  gap: 24rpx;
}

.fail-actions {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.btn {
  flex: 1;
  height: 88rpx;
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 500;
  border: none;

  &.btn-primary {
    background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
    color: #ffffff;
  }

  &.btn-outline {
    background: #ffffff;
    color: #ff6b6b;
    border: 2rpx solid #ff6b6b;
  }

  &:active {
    opacity: 0.8;
  }
}
</style>
