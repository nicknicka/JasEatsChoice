<template>
  <view class="wallet-container">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <view class="nav-title">我的钱包</view>
      <view class="nav-action" @click="viewTransactionHistory">
        <text class="action-text">明细</text>
      </view>
    </view>

    <scroll-view class="wallet-scroll" scroll-y>
      <!-- 余额卡片 -->
      <view class="balance-card">
        <view class="balance-header">
          <text class="balance-label">平台币余额</text>
          <view class="balance-tips">
            <text class="tips-icon">💡</text>
            <text class="tips-text">1平台币 = 1元</text>
          </view>
        </view>

        <view class="balance-amount-wrapper">
          <text class="balance-currency">¥</text>
          <text class="balance-amount">{{ formatAmount(walletInfo.balance) }}</text>
        </view>

        <view class="balance-actions">
          <button class="action-btn primary-btn" @click="showRechargeDialog">
            <text class="btn-icon">💰</text>
            <text class="btn-text">充值</text>
          </button>
          <button class="action-btn success-btn" @click="showWithdrawDialog">
            <text class="btn-icon">🏦</text>
            <text class="btn-text">提现</text>
          </button>
        </view>
      </view>

      <!-- 统计数据 -->
      <view class="stats-section">
        <view class="stats-grid">
          <view class="stat-item" @click="viewTransactionHistory('recharge')">
            <text class="stat-icon">📈</text>
            <text class="stat-value">{{ formatAmount(walletInfo.totalRecharge) }}</text>
            <text class="stat-label">累计充值</text>
          </view>

          <view class="stat-item" @click="viewTransactionHistory('consume')">
            <text class="stat-icon">🛒</text>
            <text class="stat-value">{{ formatAmount(walletInfo.totalConsume) }}</text>
            <text class="stat-label">累计消费</text>
          </view>

          <view class="stat-item" @click="viewTransactionHistory('withdraw')">
            <text class="stat-icon">📉</text>
            <text class="stat-value">{{ formatAmount(walletInfo.totalWithdraw) }}</text>
            <text class="stat-label">累计提现</text>
          </view>
        </view>
      </view>

      <!-- 积分和红包 -->
      <view class="assets-section">
        <view class="asset-card" @click="viewPoints">
          <view class="asset-header">
            <text class="asset-icon">⭐</text>
            <view class="asset-info">
              <text class="asset-name">我的积分</text>
              <text class="asset-value">{{ walletInfo.points }}</text>
            </view>
            <text class="asset-arrow">→</text>
          </view>
          <view class="asset-desc">
            <text class="desc-text">可兑换优惠券或抵扣现金</text>
          </view>
        </view>

        <view class="asset-card" @click="viewRedEnvelopes">
          <view class="asset-header">
            <text class="asset-icon">🧧</text>
            <view class="asset-info">
              <text class="asset-name">我的红包</text>
              <text class="asset-value">{{ walletInfo.redEnvelopes }}</text>
            </view>
            <text class="asset-arrow">→</text>
          </view>
          <view class="asset-desc">
            <text class="desc-text">{{ walletInfo.availableRedEnvelopes }}个可使用</text>
          </view>
        </view>
      </view>

      <!-- 快捷功能 -->
      <view class="quick-actions-section">
        <view class="section-header">
          <text class="section-title">快捷功能</text>
        </view>

        <view class="quick-actions-grid">
          <view class="quick-action-item" @click="setupPaymentPassword">
            <text class="action-icon">🔒</text>
            <text class="action-text">支付密码</text>
          </view>

          <view class="quick-action-item" @click="viewSecurity">
            <text class="action-icon">🛡️</text>
            <text class="action-text">安全设置</text>
          </view>

          <view class="quick-action-item" @click="contactSupport">
            <text class="action-icon">💬</text>
            <text class="action-text">联系客服</text>
          </view>

          <view class="quick-action-item" @click="viewHelp">
            <text class="action-icon">❓</text>
            <text class="action-text">帮助中心</text>
          </view>
        </view>
      </view>

      <!-- 最近交易 -->
      <view class="transactions-section">
        <view class="section-header">
          <text class="section-title">最近交易</text>
          <text class="section-more" @click="viewTransactionHistory()">查看全部 →</text>
        </view>

        <view class="transactions-list" v-if="recentTransactions.length > 0">
          <view
            class="transaction-item"
            v-for="transaction in recentTransactions"
            :key="transaction.id"
            @click="viewTransactionDetail(transaction)"
          >
            <view class="transaction-icon" :class="'type-' + transaction.type">
              <text class="icon-text">{{ getTransactionIcon(transaction.type) }}</text>
            </view>

            <view class="transaction-info">
              <text class="transaction-type">{{ getTransactionTypeText(transaction.type) }}</text>
              <text class="transaction-time">{{ formatTime(transaction.createTime) }}</text>
            </view>

            <view class="transaction-amount" :class="transaction.type === 'recharge' ? 'amount-plus' : 'amount-minus'">
              <text class="amount-text">{{ transaction.type === 'recharge' ? '+' : '-' }}{{ formatAmount(transaction.amount) }}</text>
            </view>
          </view>
        </view>

        <view class="empty-container" v-else>
          <text class="empty-text">暂无交易记录</text>
        </view>
      </view>

      <!-- 底部安全区 -->
      <view class="bottom-safe-area"></view>
    </scroll-view>

    <!-- 充值弹窗 -->
    <view class="recharge-modal" v-if="rechargeDialogVisible" @click="rechargeDialogVisible = false">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">充值</text>
          <text class="modal-close" @click="rechargeDialogVisible = false">×</text>
        </view>

        <view class="modal-body">
          <view class="balance-display">
            <text class="display-label">当前余额</text>
            <text class="display-amount">¥{{ formatAmount(walletInfo.balance) }}</text>
          </view>

          <view class="input-section">
            <view class="input-label">充值金额</view>
            <view class="amount-input-wrapper">
              <text class="input-currency">¥</text>
              <input
                class="amount-input"
                type="digit"
                v-model="rechargeForm.amount"
                placeholder="请输入充值金额"
              />
            </view>
          </view>

          <view class="quick-amounts">
            <view
              class="quick-amount-item"
              v-for="amount in quickAmounts"
              :key="amount"
              @click="setRechargeAmount(amount)"
            >
              <text class="quick-amount-text">¥{{ amount }}</text>
            </view>
          </view>

          <view class="payment-methods">
            <view class="method-title">支付方式</view>
            <view class="method-list">
              <view
                class="method-item"
                :class="{ active: rechargeForm.paymentMethod === 'wechat' }"
                @click="rechargeForm.paymentMethod = 'wechat'"
              >
                <text class="method-icon">💚</text>
                <text class="method-text">微信支付</text>
              </view>

              <view
                class="method-item"
                :class="{ active: rechargeForm.paymentMethod === 'alipay' }"
                @click="rechargeForm.paymentMethod = 'alipay'"
              >
                <text class="method-icon">💙</text>
                <text class="method-text">支付宝</text>
              </view>
            </view>
          </view>

          <button class="confirm-btn" @click="handleRecharge">确认充值</button>
        </view>
      </view>
    </view>

    <!-- 提现弹窗 -->
    <view class="withdraw-modal" v-if="withdrawDialogVisible" @click="withdrawDialogVisible = false">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">提现</text>
          <text class="modal-close" @click="withdrawDialogVisible = false">×</text>
        </view>

        <view class="modal-body">
          <view class="balance-display">
            <text class="display-label">可提现余额</text>
            <text class="display-amount">¥{{ formatAmount(walletInfo.balance) }}</text>
          </view>

          <view class="input-section">
            <view class="input-label">提现金额</view>
            <view class="amount-input-wrapper">
              <text class="input-currency">¥</text>
              <input
                class="amount-input"
                type="digit"
                v-model="withdrawForm.amount"
                placeholder="请输入提现金额"
              />
            </view>
          </view>

          <view class="withdraw-tips">
            <text class="tips-text">最低提现金额：¥1.00</text>
            <text class="tips-text">预计到账时间：1-3个工作日</text>
          </view>

          <button class="confirm-btn" @click="handleWithdraw">确认提现</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { walletApi } from '@/api'

// 钱包信息
const walletInfo = ref({
  balance: 128.50,
  totalRecharge: 500.00,
  totalConsume: 371.50,
  totalWithdraw: 0,
  points: 2580,
  redEnvelopes: 3,
  availableRedEnvelopes: 2
})

// 最近交易
const recentTransactions = ref([])

// 充值表单
const rechargeDialogVisible = ref(false)
const rechargeForm = ref({
  amount: '',
  paymentMethod: 'wechat'
})

// 提现表单
const withdrawDialogVisible = ref(false)
const withdrawForm = ref({
  amount: ''
})

// 快捷金额
const quickAmounts = [10, 20, 50, 100, 200, 500]

// 组件挂载
onMounted(() => {
  loadWalletInfo()
  loadRecentTransactions()
})

/**
 * 加载钱包信息
 */
const loadWalletInfo = async () => {
  try {
    // TODO: 调用真实API
    // const res = await walletApi.getInfo()
    // walletInfo.value = res

    // 使用模拟数据
  } catch (error) {
    console.error('加载钱包信息失败:', error)
  }
}

/**
 * 加载最近交易
 */
const loadRecentTransactions = async () => {
  try {
    // 模拟数据
    const mockData = [
      {
        id: 1,
        type: 'consume',
        amount: 38.00,
        createTime: new Date(Date.now() - 1000 * 60 * 30).toISOString()
      },
      {
        id: 2,
        type: 'recharge',
        amount: 100.00,
        createTime: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString()
      },
      {
        id: 3,
        type: 'consume',
        amount: 28.50,
        createTime: new Date(Date.now() - 1000 * 60 * 60 * 5).toISOString()
      },
      {
        id: 4,
        type: 'recharge',
        amount: 50.00,
        createTime: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString()
      }
    ]

    recentTransactions.value = mockData
  } catch (error) {
    console.error('加载交易记录失败:', error)
  }
}

/**
 * 格式化金额
 */
const formatAmount = (amount) => {
  if (!amount) return '0.00'
  return Number(amount).toFixed(2)
}

/**
 * 显示充值弹窗
 */
const showRechargeDialog = () => {
  rechargeForm.value = {
    amount: '',
    paymentMethod: 'wechat'
  }
  rechargeDialogVisible.value = true
}

/**
 * 设置充值金额
 */
const setRechargeAmount = (amount) => {
  rechargeForm.value.amount = amount.toString()
}

/**
 * 处理充值
 */
const handleRecharge = () => {
  const amount = parseFloat(rechargeForm.value.amount)

  if (!amount || amount <= 0) {
    uni.showToast({
      title: '请输入充值金额',
      icon: 'none'
    })
    return
  }

  if (amount < 1) {
    uni.showToast({
      title: '最低充值1元',
      icon: 'none'
    })
    return
  }

  // TODO: 调用支付接口
  uni.showToast({
    title: '功能开发中...',
    icon: 'none'
  })

  rechargeDialogVisible.value = false
}

/**
 * 显示提现弹窗
 */
const showWithdrawDialog = () => {
  withdrawForm.value = {
    amount: ''
  }
  withdrawDialogVisible.value = true
}

/**
 * 处理提现
 */
const handleWithdraw = () => {
  const amount = parseFloat(withdrawForm.value.amount)

  if (!amount || amount <= 0) {
    uni.showToast({
      title: '请输入提现金额',
      icon: 'none'
    })
    return
  }

  if (amount < 1) {
    uni.showToast({
      title: '最低提现1元',
      icon: 'none'
    })
    return
  }

  if (amount > walletInfo.value.balance) {
    uni.showToast({
      title: '余额不足',
      icon: 'none'
    })
    return
  }

  // TODO: 调用提现接口
  uni.showToast({
    title: '提现申请已提交',
    icon: 'success'
  })

  withdrawDialogVisible.value = false
}

/**
 * 查看交易记录
 */
const viewTransactionHistory = (type = '') => {
  uni.navigateTo({
    url: `/pages/wallet/transactions?type=${type}`
  })
}

/**
 * 查看积分
 */
const viewPoints = () => {
  uni.navigateTo({
    url: '/pages/wallet/points'
  })
}

/**
 * 查看红包
 */
const viewRedEnvelopes = () => {
  uni.navigateTo({
    url: '/pages/wallet/red-envelopes'
  })
}

/**
 * 设置支付密码
 */
const setupPaymentPassword = () => {
  uni.navigateTo({
    url: '/pages/wallet/payment-password'
  })
}

/**
 * 查看安全设置
 */
const viewSecurity = () => {
  uni.navigateTo({
    url: '/pages/wallet/security'
  })
}

/**
 * 联系客服
 */
const contactSupport = () => {
  uni.navigateTo({
    url: '/pages/customer-service/index'
  })
}

/**
 * 查看帮助
 */
const viewHelp = () => {
  uni.navigateTo({
    url: '/pages/help/index'
  })
}

/**
 * 查看交易详情
 */
const viewTransactionDetail = (transaction) => {
  uni.showModal({
    title: '交易详情',
    content: `${getTransactionTypeText(transaction.type)}\n金额：¥${formatAmount(transaction.amount)}\n时间：${formatTime(transaction.createTime)}`,
    showCancel: false
  })
}

/**
 * 获取交易图标
 */
const getTransactionIcon = (type) => {
  const iconMap = {
    'recharge': '💰',
    'consume': '🛒',
    'withdraw': '🏦',
    'refund': '🔄',
    'transfer': '↔️'
  }
  return iconMap[type] || '💳'
}

/**
 * 获取交易类型文本
 */
const getTransactionTypeText = (type) => {
  const typeMap = {
    'recharge': '充值',
    'consume': '消费',
    'withdraw': '提现',
    'refund': '退款',
    'transfer': '转账'
  }
  return typeMap[type] || '交易'
}

/**
 * 格式化时间
 */
const formatTime = (time) => {
  if (!time) return ''

  const now = Date.now()
  const itemTime = new Date(time).getTime()
  const diff = now - itemTime

  if (diff < 1000 * 60 * 60) {
    return `${Math.floor(diff / (1000 * 60))}分钟前`
  } else if (diff < 1000 * 60 * 60 * 24) {
    return `${Math.floor(diff / (1000 * 60 * 60))}小时前`
  } else if (diff < 1000 * 60 * 60 * 24 * 7) {
    return `${Math.floor(diff / (1000 * 60 * 60 * 24))}天前`
  } else {
    const date = new Date(time)
    return `${date.getMonth() + 1}-${date.getDate()}`
  }
}

/**
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.wallet-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  display: flex;
  flex-direction: column;
}

/* 导航栏 */
.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 88rpx;
  background-color: $bg-color-white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 $spacing-md;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.nav-back {
  width: 88rpx;
  height: 88rpx;
  @include flex-center;
}

.back-icon {
  font-size: 48rpx;
  color: $text-color-primary;
  font-weight: bold;
}

.nav-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.nav-action {
  width: 88rpx;
  height: 88rpx;
  @include flex-center;
}

.action-text {
  font-size: $font-size-base;
  color: $primary-color;
}

/* 钱包滚动 */
.wallet-scroll {
  flex: 1;
  margin-top: 108rpx;
  padding: $spacing-md;
  padding-bottom: env(safe-area-inset-bottom);
}

/* 余额卡片 */
.balance-card {
  background: linear-gradient(135deg, #FF6B35, #FF8C61);
  border-radius: $border-radius-lg;
  padding: $spacing-xl;
  margin-bottom: $spacing-md;
  box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.3);
}

.balance-header {
  @include flex-between;
  align-items: center;
  margin-bottom: $spacing-lg;
}

.balance-label {
  font-size: $font-size-base;
  color: rgba(255, 255, 255, 0.9);
}

.balance-tips {
  @include flex-center;
  gap: 4rpx;
  padding: 4rpx 12rpx;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: $border-radius-round;
}

.tips-icon {
  font-size: 20rpx;
}

.tips-text {
  font-size: $font-size-xs;
  color: #fff;
}

.balance-amount-wrapper {
  @include flex-center;
  margin-bottom: $spacing-xl;
}

.balance-currency {
  font-size: 48rpx;
  color: #fff;
  font-weight: $font-weight-bold;
  margin-right: 8rpx;
}

.balance-amount {
  font-size: 88rpx;
  color: #fff;
  font-weight: $font-weight-bold;
  line-height: 1;
}

.balance-actions {
  @include flex-center;
  gap: $spacing-md;
}

.action-btn {
  flex: 1;
  height: 80rpx;
  @include flex-center;
  gap: $spacing-sm;
  border-radius: $border-radius-round;
  font-size: $font-size-base;
  border: none;

  &::after {
    border: none;
  }
}

.primary-btn {
  background-color: #fff;
  color: $primary-color;
  font-weight: $font-weight-bold;
}

.success-btn {
  background-color: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.btn-icon {
  font-size: 32rpx;
}

.btn-text {
  font-size: $font-size-base;
}

/* 统计数据 */
.stats-section {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  margin-bottom: $spacing-md;
  box-shadow: $box-shadow-sm;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-md;
}

.stat-item {
  @include flex-center-column;
  gap: $spacing-xs;
  padding: $spacing-md 0;
}

.stat-icon {
  font-size: 48rpx;
}

.stat-value {
  font-size: $font-size-xl;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
}

.stat-label {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

/* 资产部分 */
.assets-section {
  margin-bottom: $spacing-md;
}

.asset-card {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  margin-bottom: $spacing-sm;
  box-shadow: $box-shadow-sm;

  &:active {
    background-color: $bg-color-base;
  }
}

.asset-header {
  @include flex-center;
  margin-bottom: $spacing-sm;
}

.asset-icon {
  font-size: 48rpx;
  margin-right: $spacing-md;
}

.asset-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.asset-name {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
}

.asset-value {
  font-size: $font-size-xl;
  color: $primary-color;
  font-weight: $font-weight-bold;
}

.asset-arrow {
  font-size: 32rpx;
  color: $text-color-secondary;
}

.asset-desc {
  padding-left: 64rpx;
}

.desc-text {
  font-size: $font-size-xs;
  color: $text-color-secondary;
}

/* 快捷功能 */
.quick-actions-section {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  margin-bottom: $spacing-md;
  box-shadow: $box-shadow-sm;
}

.section-header {
  @include flex-between;
  margin-bottom: $spacing-md;
}

.section-title {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
}

.section-more {
  font-size: $font-size-sm;
  color: $primary-color;
}

.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-md;
}

.quick-action-item {
  @include flex-center-column;
  gap: $spacing-xs;
  padding: $spacing-md 0;
}

.action-icon {
  font-size: 48rpx;
}

.action-text {
  font-size: $font-size-sm;
  color: $text-color-primary;
}

/* 交易记录 */
.transactions-section {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  box-shadow: $box-shadow-sm;
}

.transactions-list {
  .transaction-item {
    display: flex;
    align-items: center;
    padding: $spacing-md 0;
    border-bottom: 1rpx solid $border-color-lighter;

    &:last-child {
      border-bottom: none;
    }

    &:active {
      background-color: $bg-color-base;
    }
  }
}

.transaction-icon {
  width: 80rpx;
  height: 80rpx;
  @include flex-center;
  border-radius: $border-radius-base;
  margin-right: $spacing-md;

  &.type-recharge {
    background-color: rgba($success-color, 0.1);
  }

  &.type-consume {
    background-color: rgba($primary-color, 0.1);
  }

  &.type-withdraw {
    background-color: rgba($warning-color, 0.1);
  }
}

.icon-text {
  font-size: 36rpx;
}

.transaction-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.transaction-type {
  font-size: $font-size-base;
  color: $text-color-primary;
}

.transaction-time {
  font-size: $font-size-xs;
  color: $text-color-secondary;
}

.transaction-amount {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
}

.amount-plus {
  color: $success-color;
}

.amount-minus {
  color: $text-color-primary;
}

/* 空状态 */
.empty-container {
  padding: $spacing-xl;
  text-align: center;
}

.empty-text {
  font-size: $font-size-base;
  color: $text-color-secondary;
}

/* 弹窗 */
.recharge-modal,
.withdraw-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  z-index: 1000;
  @include flex-center;
}

.modal-content {
  width: 600rpx;
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  overflow: hidden;
}

.modal-header {
  @include flex-between;
  padding: $spacing-lg;
  border-bottom: 1rpx solid $border-color-lighter;
}

.modal-title {
  font-size: $font-size-lg;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
}

.modal-close {
  font-size: 48rpx;
  color: $text-color-secondary;
  line-height: 1;
  padding: 0 $spacing-xs;
}

.modal-body {
  padding: $spacing-xl;
}

.balance-display {
  @include flex-between;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  margin-bottom: $spacing-lg;
}

.display-label {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.display-amount {
  font-size: $font-size-xl;
  color: $primary-color;
  font-weight: $font-weight-bold;
}

.input-section {
  margin-bottom: $spacing-lg;
}

.input-label {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
  margin-bottom: $spacing-sm;
}

.amount-input-wrapper {
  @include flex-center;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
}

.input-currency {
  font-size: $font-size-xl;
  color: $text-color-primary;
  margin-right: $spacing-sm;
}

.amount-input {
  flex: 1;
  font-size: $font-size-xl;
  color: $text-color-primary;
}

.quick-amounts {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-sm;
  margin-bottom: $spacing-lg;
}

.quick-amount-item {
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  text-align: center;
  border: 2rpx solid transparent;

  &:active {
    border-color: $primary-color;
    background-color: rgba($primary-color, 0.1);
  }
}

.quick-amount-text {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
}

.payment-methods {
  margin-bottom: $spacing-xl;
}

.method-title {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
  margin-bottom: $spacing-sm;
}

.method-list {
  display: flex;
  gap: $spacing-md;
}

.method-item {
  flex: 1;
  @include flex-center;
  gap: $spacing-sm;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  border: 2rpx solid transparent;

  &.active {
    border-color: $primary-color;
    background-color: rgba($primary-color, 0.1);
  }
}

.method-icon {
  font-size: 32rpx;
}

.method-text {
  font-size: $font-size-base;
  color: $text-color-primary;
}

.withdraw-tips {
  margin-bottom: $spacing-xl;
}

.tips-text {
  display: block;
  font-size: $font-size-xs;
  color: $text-color-secondary;
  margin-bottom: 4rpx;
}

.confirm-btn {
  width: 100%;
  padding: $spacing-md;
  background-color: $primary-color;
  color: #fff;
  border-radius: $border-radius-lg;
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  border: none;

  &::after {
    border: none;
  }
}

/* 底部安全区 */
.bottom-safe-area {
  height: 40rpx;
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
