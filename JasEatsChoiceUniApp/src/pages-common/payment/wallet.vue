<template>
  <view class="wallet-payment">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="icon">‹</text>
      </view>
      <view class="nav-title">钱包支付</view>
      <view class="nav-placeholder"></view>
    </view>

    <!-- 余额卡片 -->
    <view class="balance-card">
      <view class="card-header">
        <text class="title">钱包余额</text>
        <view class="refresh-btn" @click="refreshBalance">
          <text class="icon">🔄</text>
        </view>
      </view>
      <view class="balance-amount">
        <text class="symbol">¥</text>
        <text class="amount">{{ balance }}</text>
      </view>
      <view class="card-footer">
        <text class="desc">可用于支付订单、充值等</text>
        <button class="recharge-btn" @click="goRecharge">充值</button>
      </view>
    </view>

    <!-- 支付信息 -->
    <view class="payment-info">
      <view class="info-header">支付信息</view>
      <view class="info-item">
        <text class="label">收款商户</text>
        <text class="value">{{ merchantName }}</text>
      </view>
      <view class="info-item">
        <text class="label">订单编号</text>
        <text class="value">{{ orderNo }}</text>
      </view>
      <view class="info-item amount">
        <text class="label">支付金额</text>
        <text class="value">¥{{ payAmount }}</text>
      </view>
    </view>

    <!-- 支付密码 -->
    <view class="password-section">
      <view class="section-header">
        <text class="title">请输入支付密码</text>
        <text class="forget" @click="forgetPassword">忘记密码？</text>
      </view>
      <view class="password-inputs">
        <view
          class="password-dot"
          v-for="(item, index) in 6"
          :key="index"
          :class="{ filled: index < password.length }"
        >
          <text v-if="index < password.length">●</text>
        </view>
      </view>
    </view>

    <!-- 数字键盘 -->
    <view class="number-keyboard">
      <view class="keyboard-row" v-for="(row, rowIndex) in keyboard" :key="rowIndex">
        <view
          class="keyboard-key"
          v-for="(key, keyIndex) in row"
          :key="keyIndex"
          :class="{ delete: key === 'delete', empty: key === '' }"
          @click="onKeyPress(key)"
        >
          <text v-if="key === 'delete'" class="icon">⌫</text>
          <text v-else>{{ key }}</text>
        </view>
      </view>
    </view>

    <!-- 充值提示弹窗 -->
    <uni-popup ref="rechargePopup" type="center">
      <view class="recharge-popup">
        <view class="popup-header">
          <text class="title">余额不足</text>
          <view class="close-btn" @click="closeRechargePopup">✕</view>
        </view>
        <view class="popup-content">
          <view class="balance-info">
            <text class="label">当前余额</text>
            <text class="amount">¥{{ balance }}</text>
          </view>
          <view class="pay-amount-info">
            <text class="label">应付金额</text>
            <text class="amount">¥{{ payAmount }}</text>
          </view>
          <view class="shortage-info">
            <text class="label">还需充值</text>
            <text class="amount shortage">¥{{ shortageAmount }}</text>
          </view>
        </view>
        <view class="popup-actions">
          <button class="btn btn-outline" @click="closeRechargePopup">取消</button>
          <button class="btn btn-primary" @click="goRecharge">去充值</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'uni-mini-router'

const router = useRouter()

// 余额和订单信息
const balance = ref('158.60')
const merchantName = ref('美味餐厅')
const orderNo = ref('')
const payAmount = ref('0.00')

// 支付密码
const password = ref('')

// 数字键盘配置
const keyboard = [
  ['1', '2', '3'],
  ['4', '5', '6'],
  ['7', '8', '9'],
  ['', '0', 'delete']
]

// 充值提示弹窗
const rechargePopup = ref(null)

// 计算还需充值金额
const shortageAmount = computed(() => {
  const currentBalance = parseFloat(balance.value) || 0
  const amount = parseFloat(payAmount.value) || 0
  const shortage = amount - currentBalance
  return shortage > 0 ? shortage.toFixed(2) : '0.00'
})

// 加载页面
uni.$on('walletPaymentData', (data) => {
  merchantName.value = data.merchantName || ''
  orderNo.value = data.orderNo || ''
  payAmount.value = data.amount || '0.00'
})

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 刷新余额
const refreshBalance = () => {
  uni.showLoading({ title: '刷新中...' })
  // 模拟刷新余额
  setTimeout(() => {
    balance.value = '168.60'
    uni.hideLoading()
    uni.showToast({
      title: '刷新成功',
      icon: 'success'
    })
  }, 1000)
}

// 去充值
const goRecharge = () => {
  rechargePopup.value?.close()
  uni.navigateTo({
    url: '/pages-user/wallet/recharge'
  })
}

// 忘记密码
const forgetPassword = () => {
  uni.showModal({
    title: '重置密码',
    content: '请通过手机号验证重置支付密码',
    confirmText: '去重置',
    success: (res) => {
      if (res.confirm) {
        uni.navigateTo({
          url: '/pages-user/settings/reset-password'
        })
      }
    }
  })
}

// 键盘按键
const onKeyPress = (key) => {
  if (key === 'delete') {
    password.value = password.value.slice(0, -1)
  } else if (key !== '') {
    if (password.value.length < 6) {
      password.value += key
    }
  }

  // 密码输入完成
  if (password.value.length === 6) {
    setTimeout(() => {
      verifyPassword()
    }, 100)
  }
}

// 验证支付密码
const verifyPassword = () => {
  const currentBalance = parseFloat(balance.value) || 0
  const amount = parseFloat(payAmount.value) || 0

  // 检查余额是否足够
  if (currentBalance < amount) {
    // 余额不足，显示充值提示
    rechargePopup.value?.open()
    password.value = ''
    return
  }

  // 验证密码
  uni.showLoading({ title: '支付中...' })
  setTimeout(() => {
    uni.hideLoading()

    // 模拟密码验证
    if (password.value === '123456') {
      // 支付成功
      uni.redirectTo({
        url: `/pages-common/payment/result?status=success&orderNo=${orderNo.value}&amount=${payAmount.value}&paymentMethod=钱包支付&payTime=${encodeURIComponent(new Date().toLocaleString('zh-CN'))}`
      })
    } else {
      // 密码错误
      password.value = ''
      uni.showToast({
        title: '密码错误',
        icon: 'error'
      })
    }
  }, 1500)
}

// 关闭充值弹窗
const closeRechargePopup = () => {
  rechargePopup.value?.close()
  password.value = ''
}
</script>

<style lang="scss" scoped>
.wallet-payment {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.nav-bar {
  height: 88rpx;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  border-bottom: 1rpx solid #f0f0f0;

  .nav-back {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;

    .icon {
      font-size: 48rpx;
      color: #333333;
    }
  }

  .nav-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333333;
  }

  .nav-placeholder {
    width: 60rpx;
  }
}

.balance-card {
  margin: 24rpx 32rpx;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  border-radius: 24rpx;
  padding: 40rpx 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(255, 107, 107, 0.3);

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;

    .title {
      font-size: 28rpx;
      color: rgba(255, 255, 255, 0.8);
    }

    .refresh-btn {
      width: 48rpx;
      height: 48rpx;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;

      .icon {
        font-size: 28rpx;
        color: #ffffff;
      }

      &:active {
        opacity: 0.8;
      }
    }
  }

  .balance-amount {
    display: flex;
    align-items: baseline;
    margin-bottom: 32rpx;

    .symbol {
      font-size: 48rpx;
      color: #ffffff;
      margin-right: 8rpx;
    }

    .amount {
      font-size: 96rpx;
      font-weight: bold;
      color: #ffffff;
    }
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .desc {
      font-size: 24rpx;
      color: rgba(255, 255, 255, 0.7);
    }

    .recharge-btn {
      height: 56rpx;
      padding: 0 24rpx;
      background: #ffffff;
      border-radius: 28rpx;
      font-size: 26rpx;
      color: #ff6b6b;
      border: none;
      font-weight: 500;

      &:active {
        opacity: 0.8;
      }
    }
  }
}

.payment-info {
  margin: 0 32rpx 24rpx;
  background: #ffffff;
  border-radius: 16rpx;
  padding: 32rpx;

  .info-header {
    font-size: 28rpx;
    font-weight: bold;
    color: #333333;
    margin-bottom: 24rpx;
  }

  .info-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16rpx 0;

    .label {
      font-size: 28rpx;
      color: #666666;
    }

    .value {
      font-size: 28rpx;
      color: #333333;

      &.amount {
        font-size: 36rpx;
        font-weight: bold;
        color: #ff6b6b;
      }
    }
  }
}

.password-section {
  margin: 0 32rpx 24rpx;
  background: #ffffff;
  border-radius: 16rpx;
  padding: 32rpx;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32rpx;

    .title {
      font-size: 28rpx;
      font-weight: bold;
      color: #333333;
    }

    .forget {
      font-size: 26rpx;
      color: #ff6b6b;
    }
  }

  .password-inputs {
    display: flex;
    justify-content: space-between;
    gap: 16rpx;

    .password-dot {
      width: 88rpx;
      height: 88rpx;
      border: 2rpx solid #e0e0e0;
      border-radius: 12rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #ffffff;

      &.filled {
        border-color: #ff6b6b;
        background: #fff5f5;
      }

      text {
        font-size: 48rpx;
        color: #333333;
      }
    }
  }
}

.number-keyboard {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #ffffff;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  border-top: 1rpx solid #f0f0f0;

  .keyboard-row {
    display: flex;
    gap: 16rpx;
    padding: 8rpx 32rpx;
    background: #ffffff;

    .keyboard-key {
      flex: 1;
      height: 100rpx;
      background: #ffffff;
      border: 1rpx solid #e0e0e0;
      border-radius: 12rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 36rpx;
      font-weight: 500;
      color: #333333;

      &:active {
        background: #f5f5f5;
      }

      &.delete {
        background: #f5f5f5;
        border-color: #e0e0e0;

        .icon {
          font-size: 40rpx;
        }
      }

      &.empty {
        background: transparent;
        border: none;
      }
    }
  }
}

.recharge-popup {
  width: 560rpx;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;

  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32rpx;

    .title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333333;
    }

    .close-btn {
      width: 48rpx;
      height: 48rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 36rpx;
      color: #999999;
    }
  }

  .popup-content {
    margin-bottom: 32rpx;

    .balance-info,
    .pay-amount-info,
    .shortage-info {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 1rpx solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .label {
        font-size: 28rpx;
        color: #666666;
      }

      .amount {
        font-size: 32rpx;
        font-weight: 500;
        color: #333333;

        &.shortage {
          font-size: 36rpx;
          font-weight: bold;
          color: #ff6b6b;
        }
      }
    }
  }

  .popup-actions {
    display: flex;
    gap: 16rpx;

    .btn {
      flex: 1;
      height: 80rpx;
      border-radius: 40rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28rpx;
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
  }
}
</style>
