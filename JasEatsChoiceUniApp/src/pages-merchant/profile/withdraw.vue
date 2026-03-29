<template>
  <view class="withdraw-container">
    <!-- 可提现金额 -->
    <view class="balance-section">
      <text class="balance-label">可提现金额</text>
      <view class="balance-row">
        <text class="balance-symbol">¥</text>
        <text class="balance-amount">{{ withdrawData.availableBalance }}</text>
      </view>
      <view class="balance-tips">
        <uni-icons type="info" size="14" color="#FF6B35"></uni-icons>
        <text>冻结金额：¥{{ withdrawData.frozenBalance }}</text>
      </view>
    </view>

    <!-- 提现表单 -->
    <view class="withdraw-form">
      <view class="form-section">
        <view class="section-title">提现金额</view>
        <view class="amount-input-wrapper">
          <text class="currency-symbol">¥</text>
          <input
            class="amount-input"
            v-model="withdrawAmount"
            placeholder="请输入提现金额"
            type="digit"
            @input="onAmountChange"
          />
          <button class="all-withdraw-btn" @tap="withdrawAll">全部提现</button>
        </view>
        <view class="amount-tips">
          <text class="tips-text">最低提现金额 ¥100，手续费 {{ withdrawData.feeRate }}%</text>
          <text class="fee-text">预计手续费：¥{{ estimatedFee }}</text>
        </view>
      </view>

      <view class="form-section">
        <view class="section-title">到账方式</view>
        <view class="withdraw-method-list">
          <view
            class="method-item"
            :class="{ active: withdrawMethod === item.value }"
            v-for="item in withdrawMethods"
            :key="item.value"
            @tap="selectWithdrawMethod(item.value)"
          >
            <view class="method-icon">
              <uni-icons :type="item.icon" size="28" :color="withdrawMethod === item.value ? '#FF6B35' : '#999'"></uni-icons>
            </view>
            <view class="method-info">
              <text class="method-name">{{ item.label }}</text>
              <text class="method-desc">{{ item.desc }}</text>
            </view>
            <view class="method-check" v-if="withdrawMethod === item.value">
              <uni-icons type="checkbox-filled" size="20" color="#FF6B35"></uni-icons>
            </view>
          </view>
        </view>
      </view>

      <view class="form-section">
        <view class="section-title">收款账户</view>
        <view class="account-card">
          <view class="account-icon">
            <uni-icons type="wallet-filled" size="24" color="#FF6B35"></uni-icons>
          </view>
          <view class="account-info">
            <text class="bank-name">{{ withdrawData.bankName }}</text>
            <text class="account-number">{{ maskAccountNumber(withdrawData.bankAccount) }}</text>
          </view>
          <button class="change-account-btn" @tap="changeAccount">更换</button>
        </view>
      </view>

      <view class="form-section">
        <view class="section-title">提现说明</view>
        <view class="withdraw-rules">
          <view class="rule-item">
            <uni-icons type="checkmarkempty" size="16" color="#52C41A"></uni-icons>
            <text>单笔最低提现金额 ¥100，单笔最高 ¥50000</text>
          </view>
          <view class="rule-item">
            <uni-icons type="checkmarkempty" size="16" color="#52C41A"></uni-icons>
            <text>每日可提现 3 次，提现后实时到账</text>
          </view>
          <view class="rule-item">
            <uni-icons type="checkmarkempty" size="16" color="#52C41A"></uni-icons>
            <text>手续费按提现金额的 {{ withdrawData.feeRate }}% 收取</text>
          </view>
          <view class="rule-item">
            <uni-icons type="checkmarkempty" size="16" color="#52C41A"></uni-icons>
            <text>提现金额将在1-3个工作日内到账</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 提现记录 -->
    <view class="withdraw-history">
      <view class="section-header">
        <text class="section-title">提现记录</text>
        <text class="view-all" @tap="viewAllHistory">查看全部</text>
      </view>
      <view class="history-list">
        <view
          class="history-item"
          v-for="item in withdrawHistory"
          :key="item.id"
          @tap="viewWithdrawDetail(item)"
        >
          <view class="history-info">
            <text class="history-amount">-¥{{ item.amount }}</text>
            <text class="history-time">{{ item.time }}</text>
          </view>
          <view class="history-status" :class="'status-' + item.status">
            {{ getStatusText(item.status) }}
          </view>
        </view>
      </view>
    </view>

    <!-- 确认提现按钮 -->
    <view class="submit-button-container">
      <button class="submit-button" @tap="confirmWithdraw" :disabled="!canSubmit">
        确认提现
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { merchantApi } from '@/api'

// 提现方式
const withdrawMethods = [
  {
    label: '银行卡',
    value: 'bank',
    icon: 'wallet',
    desc: '1-3个工作日到账'
  },
  {
    label: '微信零钱',
    value: 'wechat',
    icon: 'weixin',
    desc: '实时到账'
  },
  {
    label: '支付宝',
    value: 'alipay',
    icon: 'locked',
    desc: '实时到账'
  }
]

const withdrawMethod = ref('bank')
const withdrawAmount = ref('')

// 提现数据
const withdrawData = ref({
  availableBalance: '15680.50',
  frozenBalance: '180.00',
  feeRate: 0.2,
  todayWithdrawCount: 1,
  maxWithdrawCount: 3,
  bankName: '中国工商银行',
  bankAccount: '6212260200034567890'
})

// 提现记录
const withdrawHistory = ref([
  {
    id: 1,
    amount: '1000.00',
    time: '2026-03-17 15:30',
    status: 'success'
  },
  {
    id: 2,
    amount: '500.00',
    time: '2026-03-16 10:20',
    status: 'success'
  },
  {
    id: 3,
    amount: '2000.00',
    time: '2026-03-15 14:45',
    status: 'processing'
  }
])

onMounted(async () => {
  await loadWithdrawData()
})

/**
 * M-015: 加载提现数据
 */
const loadWithdrawData = async () => {
  try {
    const res = await merchantApi.getWithdrawData()
    if (res.code === 200 && res.data) {
      withdrawData.value = {
        availableBalance: res.data.availableBalance || '0',
        frozenBalance: res.data.frozenBalance || '0',
        feeRate: res.data.feeRate || 0.2,
        todayWithdrawCount: res.data.todayWithdrawCount || 0,
        maxWithdrawCount: res.data.maxWithdrawCount || 3,
        bankName: res.data.bankName || '',
        bankAccount: res.data.bankAccount || ''
      }

      // 加载提现记录
      if (res.data.history) {
        withdrawHistory.value = res.data.history
      }
    }
  } catch (error) {
    console.error('加载提现数据失败:', error)
    // 保持默认数据
  }
}

/**
 * 金额输入变化
 */
const onAmountChange = (e) => {
  let value = e.detail.value
  // 限制小数点后两位
  if (value.includes('.')) {
    const parts = value.split('.')
    if (parts[1] && parts[1].length > 2) {
      value = `${parts[0]}.${parts[1].substring(0, 2)}`
      withdrawAmount.value = value
    }
  }
}

/**
 * 全部提现
 */
const withdrawAll = () => {
  withdrawAmount.value = withdrawData.value.availableBalance
}

/**
 * 选择提现方式
 */
const selectWithdrawMethod = (method) => {
  withdrawMethod.value = method
}

/**
 * 更换收款账户
 */
const changeAccount = () => {
  uni.navigateTo({
    url: '/profile/edit?scene=bank'
  })
}

/**
 * 预估手续费
 */
const estimatedFee = computed(() => {
  if (!withdrawAmount.value) return '0.00'
  const amount = parseFloat(withdrawAmount.value)
  if (isNaN(amount)) return '0.00'
  const fee = amount * (withdrawData.value.feeRate / 100)
  return fee.toFixed(2)
})

/**
 * 是否可以提交
 */
const canSubmit = computed(() => {
  const amount = parseFloat(withdrawAmount.value)
  if (isNaN(amount)) return false
  if (amount < 100) return false
  if (amount > parseFloat(withdrawData.value.availableBalance)) return false
  if (withdrawData.value.todayWithdrawCount >= withdrawData.value.maxWithdrawCount) return false
  return true
})

/**
 * 确认提现
 */
const confirmWithdraw = () => {
  const amount = parseFloat(withdrawAmount.value)

  if (amount < 100) {
    uni.showToast({
      title: '最低提现金额 ¥100',
      icon: 'none'
    })
    return
  }

  if (amount > parseFloat(withdrawData.value.availableBalance)) {
    uni.showToast({
      title: '提现金额超过可用余额',
      icon: 'none'
    })
    return
  }

  if (withdrawData.value.todayWithdrawCount >= withdrawData.value.maxWithdrawCount) {
    uni.showToast({
      title: '今日提现次数已用完',
      icon: 'none'
    })
    return
  }

  const actualAmount = (amount - parseFloat(estimatedFee.value)).toFixed(2)

  uni.showModal({
    title: '确认提现',
    content: `提现金额 ¥${amount.toFixed(2)}，手续费 ¥${estimatedFee.value}，实际到账 ¥${actualAmount}，确认提现吗？`,
    confirmColor: '#FF6B35',
    success: (res) => {
      if (res.confirm) {
        submitWithdraw()
      }
    }
  })
}

/**
 * M-016: 提交提现
 */
const submitWithdraw = async () => {
  try {
    uni.showLoading({
      title: '提交中...'
    })

    // M-016: 调用API提交提现
    const data = {
      amount: parseFloat(withdrawAmount.value),
      bankName: withdrawData.value.bankName,
      bankAccount: withdrawData.value.bankAccount,
      accountName: '商户账户' // 这里应该从实际数据中获取
    }

    const res = await merchantApi.submitWithdraw(data)

    uni.hideLoading()

    if (res.code === 200) {
      uni.showToast({
        title: '提现申请已提交',
        icon: 'success'
      })

      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    } else {
      throw new Error(res.message || '提交失败')
    }
  } catch (error) {
    console.error('提交提现失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error.message || '提交失败，请重试',
      icon: 'none'
    })
  }
}

/**
 * 查看全部记录
 */
const viewAllHistory = () => {
  uni.navigateTo({
    url: '/profile/withdraw-history'
  })
}

/**
 * 查看提现详情
 */
const viewWithdrawDetail = (item) => {
  uni.navigateTo({
    url: `/pages-merchant/profile/withdraw-detail?id=${item.id}`
  })
}

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  const statusMap = {
    success: '已到账',
    processing: '处理中',
    failed: '已失败'
  }
  return statusMap[status] || ''
}

/**
 * 遮罩银行卡号
 */
const maskAccountNumber = (account) => {
  if (!account) return ''
  const len = account.length
  const start = account.substring(0, 4)
  const end = account.substring(len - 4)
  const middle = '*'.repeat(len - 8)
  return `${start}${middle}${end}`
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.withdraw-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 140rpx;
}

/* 余额区域 */
.balance-section {
  background: linear-gradient(135deg, #FF6B35, #FF8F6B);
  padding: 50rpx 30rpx;
  text-align: center;
}

.balance-label {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  display: block;
  margin-bottom: 20rpx;
}

.balance-row {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 5rpx;
  margin-bottom: 15rpx;
}

.balance-symbol {
  font-size: 36rpx;
  color: #fff;
  font-weight: 500;
}

.balance-amount {
  font-size: 72rpx;
  color: #fff;
  font-weight: bold;
}

.balance-tips {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* 提现表单 */
.withdraw-form {
  padding: 20rpx;
}

.form-section {
  background: #fff;
  padding: 30rpx;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 25rpx;
}

/* 金额输入 */
.amount-input-wrapper {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  margin-bottom: 15rpx;
}

.currency-symbol {
  font-size: 40rpx;
  color: #FF6B35;
  font-weight: bold;
  flex-shrink: 0;
}

.amount-input {
  flex: 1;
  font-size: 40rpx;
  color: #333;
  font-weight: bold;
}

.all-withdraw-btn {
  padding: 10rpx 20rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 24rpx;
  border-radius: 20rpx;
  border: none;
  flex-shrink: 0;
}

.amount-tips {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tips-text {
  font-size: 24rpx;
  color: #999;
}

.fee-text {
  font-size: 26rpx;
  color: #FF6B35;
  font-weight: 500;
}

/* 提现方式 */
.withdraw-method-list {
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
  width: 70rpx;
  height: 70rpx;
  border-radius: 50%;
  background: #fff;
  @include flex-center;
}

.method-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.method-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.method-desc {
  font-size: 24rpx;
  color: #999;
}

.method-check {
  flex-shrink: 0;
}

/* 收款账户 */
.account-card {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 25rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
}

.account-icon {
  width: 70rpx;
  height: 70rpx;
  border-radius: 50%;
  background: #fff;
  @include flex-center;
}

.account-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.bank-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.account-number {
  font-size: 26rpx;
  color: #666;
}

.change-account-btn {
  padding: 10rpx 20rpx;
  background: #fff;
  color: #FF6B35;
  font-size: 24rpx;
  border-radius: 20rpx;
  border: 1rpx solid #FF6B35;
}

/* 提现说明 */
.withdraw-rules {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.rule-item {
  display: flex;
  align-items: center;
  gap: 10rpx;
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}

/* 提现记录 */
.withdraw-history {
  background: #fff;
  padding: 30rpx;
  margin: 0 20rpx 20rpx;
  border-radius: 16rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.view-all {
  font-size: 26rpx;
  color: #1890FF;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
}

.history-info {
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.history-amount {
  font-size: 32rpx;
  color: #333;
  font-weight: bold;
}

.history-time {
  font-size: 24rpx;
  color: #999;
}

.history-status {
  font-size: 26rpx;
  font-weight: 500;

  &.status-success {
    color: #52C41A;
  }

  &.status-processing {
    color: #1890FF;
  }

  &.status-failed {
    color: #F5222D;
  }
}

/* 提交按钮 */
.submit-button-container {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx;
  background: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.submit-button {
  width: 100%;
  height: 90rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  border-radius: 45rpx;
  border: none;
  @include flex-center;

  &[disabled] {
    background: #D9D9D9;
    color: #999;
  }
}
</style>
