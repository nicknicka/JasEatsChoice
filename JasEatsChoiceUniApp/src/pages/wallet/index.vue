<template>
  <view class="wallet-container">
    <!-- 钱包卡片 -->
    <view class="wallet-card">
      <view class="card-header">
        <text class="header-title">我的钱包</text>
        <text class="header-icon">💰</text>
      </view>

      <view class="balance-section">
        <text class="balance-label">账户余额（元）</text>
        <view class="balance-value">
          <text class="balance-amount">{{ balance }}</text>
          <text class="balance-unit">.{{ balanceDecimal }}</text>
        </view>
      </view>

      <view class="action-buttons">
        <button class="action-btn primary" @click="recharge">
          <text class="btn-icon">💵</text>
          <text>充值</text>
        </button>
        <button class="action-btn outline" @click="withdraw">
          <text class="btn-icon">🏦</text>
          <text>提现</text>
        </button>
      </view>
    </view>

    <!-- 交易记录 -->
    <view class="transactions-section">
      <view class="section-header">
        <text class="section-title">交易记录</text>
        <text class="section-more" @click="viewAllTransactions">查看全部 →</text>
      </view>

      <!-- 筛选Tab -->
      <view class="filter-tabs">
        <view
          class="tab-item"
          :class="{ active: activeTab === 'all' }"
          @click="changeTab('all')"
        >
          <text class="tab-text">全部</text>
        </view>
        <view
          class="tab-item"
          :class="{ active: activeTab === 'income' }"
          @click="changeTab('income')"
        >
          <text class="tab-text">收入</text>
        </view>
        <view
          class="tab-item"
          :class="{ active: activeTab === 'expense' }"
          @click="changeTab('expense')"
        >
          <text class="tab-text">支出</text>
        </view>
      </view>

      <!-- 交易列表 -->
      <scroll-view
        class="scroll-container"
        scroll-y
        refresher-enabled
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
        @scrolltolower="onLoadMore"
      >
        <!-- 空状态 -->
        <view class="empty-state" v-if="transactions.length === 0 && !loading">
          <Empty
            icon="💳"
            text="还没有交易记录"
            description="充值或消费后会显示在这里"
          />
        </view>

        <!-- 交易列表 -->
        <view class="transaction-list" v-else>
          <!-- 日期分组 -->
          <view
            class="date-group"
            v-for="group in groupedTransactions"
            :key="group.date"
          >
            <!-- 日期标题 -->
            <view class="date-title">
              <text class="date-text">{{ group.dateText }}</text>
              <text class="date-amount">
                {{ group.income > 0 ? '+' : '' }}{{ group.income }}
                {{ group.expense > 0 ? '-' : '' }}{{ group.expense }}
              </text>
            </view>

            <!-- 交易项 -->
            <view class="transaction-items">
              <view
                class="transaction-item"
                v-for="item in group.items"
                :key="item.id"
                @click="viewTransactionDetail(item)"
              >
                <!-- 交易图标 -->
                <view class="transaction-icon" :class="item.type">
                  <text class="icon-text">{{ item.icon }}</text>
                </view>

                <!-- 交易信息 -->
                <view class="transaction-info">
                  <text class="transaction-name">{{ item.name }}</text>
                  <text class="transaction-time">{{ item.time }}</text>
                </view>

                <!-- 交易金额 -->
                <view class="transaction-amount" :class="item.type">
                  <text class="amount-text">{{ item.type === 'income' ? '+' : '-' }}{{ item.amount }}</text>
                  <text class="amount-status" v-if="item.status">{{ item.statusText }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 加载状态 -->
        <view class="load-more" v-if="hasMore && transactions.length > 0">
          <view class="load-text" v-if="loading">加载中...</view>
          <view class="load-text" v-else>上拉加载更多</view>
        </view>
      </scroll-view>
    </view>

    <!-- 充值弹窗 -->
    <uni-popup ref="rechargePopup" type="bottom">
      <view class="recharge-popup">
        <view class="popup-header">
          <text class="popup-title">账户充值</text>
          <text class="popup-close" @click="closeRechargePopup">×</text>
        </view>

        <view class="recharge-amount">
          <text class="amount-label">充值金额</text>
          <view class="amount-input">
            <text class="currency-symbol">¥</text>
            <input
              class="amount-field"
              type="digit"
              v-model="rechargeAmount"
              placeholder="请输入充值金额"
            />
          </view>
        </view>

        <view class="quick-amounts">
          <view
            class="amount-item"
            v-for="amount in quickAmounts"
            :key="amount"
            @click="selectRechargeAmount(amount)"
          >
            <text class="amount-text">{{ amount }}元</text>
          </view>
        </view>

        <view class="payment-methods">
          <text class="methods-title">支付方式</text>
          <view class="method-list">
            <view
              class="method-item"
              :class="{ active: paymentMethod === 'wechat' }"
              @click="selectPaymentMethod('wechat')"
            >
              <text class="method-icon">💚</text>
              <text class="method-name">微信支付</text>
              <view class="method-check" v-if="paymentMethod === 'wechat'">✓</view>
            </view>
            <view
              class="method-item"
              :class="{ active: paymentMethod === 'alipay' }"
              @click="selectPaymentMethod('alipay')"
            >
              <text class="method-icon">💙</text>
              <text class="method-name">支付宝</text>
              <view class="method-check" v-if="paymentMethod === 'alipay'">✓</view>
            </view>
          </view>
        </view>

        <button class="confirm-btn" @click="confirmRecharge" :disabled="!rechargeAmount">
          确认充值
        </button>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import Empty from '@/components/common/Empty.vue'
import api from '@/api'

// 账户余额
const balance = ref('0')
const balanceDecimal = ref('00')

// 交易记录
const transactions = ref([])

// 加载状态
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)

// 分页参数
const page = ref(1)
const pageSize = ref(20)

// 当前激活的Tab
const activeTab = ref('all')

// 充值相关
const rechargeAmount = ref('')
const quickAmounts = [10, 20, 50, 100, 200, 500]
const paymentMethod = ref('wechat')

/**
 * 整数和小数部分
 */
const balanceParts = computed(() => {
  const amount = parseFloat(balance.value + '.' + balanceDecimal.value)
  const parts = amount.toFixed(2).split('.')
  return {
    integer: parts[0],
    decimal: parts[1] || '00'
  }
})

/**
 * 按日期分组交易记录
 */
const groupedTransactions = computed(() => {
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)

  const groups = {}

  transactions.value.forEach(item => {
    const date = new Date(item.time)
    const dateKey = `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`

    if (!groups[dateKey]) {
      const dateText = formatDateText(date)
      groups[dateKey] = {
        date: dateKey,
        dateText,
        items: [],
        income: 0,
        expense: 0
      }
    }

    groups[dateKey].items.push(item)
    if (item.type === 'income') {
      groups[dateKey].income += item.amount
    } else {
      groups[dateKey].expense += item.amount
    }
  })

  return Object.values(groups).sort((a, b) => {
    return new Date(b.date) - new Date(a.date)
  })
})

/**
 * 格式化日期文本
 */
function formatDateText(date) {
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)

  const todayStr = `${today.getFullYear()}-${today.getMonth() + 1}-${today.getDate()}`
  const yesterdayStr = `${yesterday.getFullYear()}-${yesterday.getMonth() + 1}-${yesterday.getDate()}`
  const dateStr = `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`

  if (dateStr === todayStr) {
    return '今天'
  } else if (dateStr === yesterdayStr) {
    return '昨天'
  } else {
    return `${date.getMonth() + 1}月${date.getDate()}日`
  }
}

/**
 * 加载钱包数据
 */
const loadWalletData = async () => {
  try {
    const res = await api.user.getWalletInfo()
    const amount = parseFloat(res.data.balance).toFixed(2)
    const parts = amount.split('.')
    balance.value = parts[0]
    balanceDecimal.value = parts[1] || '00'
  } catch (error) {
    console.error('加载钱包数据失败:', error)
  }
}

/**
 * 加载交易记录
 */
const loadTransactions = async (showLoading = true) => {
  if (showLoading) {
    loading.value = true
  }

  try {
    const params = {
      page: page.value,
      pageSize: pageSize.value
    }

    if (activeTab.value !== 'all') {
      params.type = activeTab.value
    }

    const res = await api.user.getTransactionList(params)

    if (page.value === 1) {
      transactions.value = res.data.list
    } else {
      transactions.value.push(...res.data.list)
    }

    hasMore.value = res.data.list.length >= pageSize.value
  } catch (error) {
    console.error('加载交易记录失败:', error)
    uni.showToast({
      title: '加载失败，请重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 下拉刷新
 */
const onRefresh = async () => {
  refreshing.value = true
  page.value = 1
  await loadWalletData()
  await loadTransactions(false)
  refreshing.value = false
}

/**
 * 上拉加载更多
 */
const onLoadMore = () => {
  if (loading.value || !hasMore.value) return
  page.value++
  loadTransactions()
}

/**
 * 切换Tab
 */
const changeTab = (tab) => {
  activeTab.value = tab
  page.value = 1
  transactions.value = []
  loadTransactions()
}

/**
 * 充值
 */
const recharge = () => {
  rechargeAmount.value = ''
  paymentMethod.value = 'wechat'
  // 打开充值弹窗
  uni.showModal({
    title: '充值',
    content: '充值功能开发中',
    showCancel: false
  })
}

/**
 * 提现
 */
const withdraw = () => {
  uni.showModal({
    title: '提现',
    content: '提现功能开发中',
    showCancel: false
  })
}

/**
 * 选择充值金额
 */
const selectRechargeAmount = (amount) => {
  rechargeAmount.value = amount.toString()
}

/**
 * 选择支付方式
 */
const selectPaymentMethod = (method) => {
  paymentMethod.value = method
}

/**
 * 确认充值
 */
const confirmRecharge = async () => {
  if (!rechargeAmount.value) {
    uni.showToast({
      title: '请输入充值金额',
      icon: 'none'
    })
    return
  }

  const amount = parseFloat(rechargeAmount.value)
  if (amount <= 0) {
    uni.showToast({
      title: '充值金额必须大于0',
      icon: 'none'
    })
    return
  }

  try {
    // TODO: 调用充值API
    uni.showToast({
      title: '充值功能开发中',
      icon: 'none'
    })
  } catch (error) {
    console.error('充值失败:', error)
    uni.showToast({
      title: '充值失败，请重试',
      icon: 'none'
    })
  }
}

/**
 * 关闭充值弹窗
 */
const closeRechargePopup = () => {
  rechargeAmount.value = ''
}

/**
 * 查看全部交易记录
 */
const viewAllTransactions = () => {
  uni.navigateTo({
    url: '/pages/wallet/transactions/index'
  })
}

/**
 * 查看交易详情
 */
const viewTransactionDetail = (item) => {
  uni.navigateTo({
    url: `/pages/wallet/detail/index?id=${item.id}`
  })
}

// 组件挂载
onMounted(() => {
  loadWalletData()
  loadTransactions()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.wallet-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  padding-bottom: $spacing-md;
}

/* 钱包卡片 */
.wallet-card {
  background: linear-gradient(135deg, #FF6B35, #FF8F61);
  margin: $spacing-md;
  padding: $spacing-xl;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-md;
}

.card-header {
  @include flex-between;
  align-items: center;
  margin-bottom: $spacing-xl;
}

.header-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: #fff;
}

.header-icon {
  font-size: 48rpx;
}

.balance-section {
  margin-bottom: $spacing-xl;
}

.balance-label {
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: $spacing-sm;
}

.balance-value {
  @include flex-center;
  align-items: baseline;
}

.balance-amount {
  font-size: 64rpx;
  font-weight: $font-weight-bold;
  color: #fff;
  line-height: 1;
}

.balance-unit {
  font-size: $font-size-base;
  color: rgba(255, 255, 255, 0.8);
  margin-left: 4rpx;
}

.action-buttons {
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

  &.primary {
    background-color: #fff;
    color: $primary-color;
  }

  &.outline {
    background-color: transparent;
    color: #fff;
    border: 2rpx solid #fff;
  }

  &:active {
    transform: scale(0.98);
  }
}

.btn-icon {
  font-size: $font-size-xl;
}

/* 交易记录 */
.transactions-section {
  background-color: $bg-color-white;
  margin: 0 $spacing-md;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  box-shadow: $box-shadow-sm;
}

.section-header {
  @include flex-between;
  align-items: center;
  margin-bottom: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.section-more {
  font-size: $font-size-sm;
  color: $primary-color;

  &:active {
    opacity: 0.6;
  }
}

/* 筛选Tab */
.filter-tabs {
  @include flex-center;
  background-color: $bg-color-base;
  padding: $spacing-xs;
  border-radius: $border-radius-lg;
  margin-bottom: $spacing-md;
}

.tab-item {
  flex: 1;
  @include flex-center;
  padding: $spacing-sm;
  border-radius: $border-radius-base;
  transition: all 0.3s;

  &.active {
    background-color: #fff;
    box-shadow: $box-shadow-sm;

    .tab-text {
      color: $primary-color;
      font-weight: $font-weight-bold;
    }
  }
}

.tab-text {
  font-size: $font-size-sm;
  color: $text-color-regular;
}

/* 滚动容器 */
.scroll-container {
  max-height: 800rpx;
}

/* 空状态 */
.empty-state {
  padding: 80rpx 0;
}

/* 交易列表 */
.transaction-list {
  @include flex-center-column;
  gap: $spacing-md;
}

.date-group {
  margin-bottom: $spacing-sm;
}

.date-title {
  @include flex-between;
  padding: $spacing-sm $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
}

.date-text {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $text-color-primary;
}

.date-amount {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.transaction-items {
  background-color: $bg-color-white;
  border-radius: $border-radius-base;
  overflow: hidden;
}

.transaction-item {
  @include flex-center;
  padding: $spacing-md;
  border-bottom: 1rpx solid $border-color-lighter;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background-color: $bg-color-base;
  }
}

.transaction-icon {
  width: 72rpx;
  height: 72rpx;
  @include flex-center;
  border-radius: 50%;
  flex-shrink: 0;

  &.income {
    background-color: rgba(103, 194, 58, 0.1);
  }

  &.expense {
    background-color: rgba(255, 107, 53, 0.1);
  }
}

.icon-text {
  font-size: $font-size-xl;
}

.transaction-info {
  flex: 1;
  margin-left: $spacing-md;
  @include flex-center-column;
  align-items: flex-start;
  gap: $spacing-xs;
  min-width: 0;
}

.transaction-name {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-color-primary;
}

.transaction-time {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.transaction-amount {
  @include flex-center-column;
  align-items: flex-end;
  gap: $spacing-xs;

  &.income .amount-text {
    color: $success-color;
  }

  &.expense .amount-text {
    color: $text-color-primary;
  }
}

.amount-text {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
}

.amount-status {
  font-size: $font-size-xs;
  color: $text-color-placeholder;
  padding: 2rpx 8rpx;
  background-color: $bg-color-base;
  border-radius: $border-radius-round;
}

/* 加载状态 */
.load-more {
  @include flex-center;
  padding: $spacing-lg 0;
}

.load-text {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

/* 充值弹窗 */
.recharge-popup {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg $border-radius-lg 0 0;
  padding: $spacing-lg;
  max-height: 80vh;
  overflow-y: auto;
}

.popup-header {
  @include flex-between;
  align-items: center;
  margin-bottom: $spacing-xl;
}

.popup-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.popup-close {
  width: 48rpx;
  height: 48rpx;
  @include flex-center;
  font-size: 48rpx;
  color: $text-color-placeholder;

  &:active {
    opacity: 0.6;
  }
}

.recharge-amount {
  margin-bottom: $spacing-xl;
}

.amount-label {
  font-size: $font-size-base;
  color: $text-color-primary;
  margin-bottom: $spacing-md;
}

.amount-input {
  @include flex-center;
  padding: $spacing-lg;
  background-color: $bg-color-base;
  border-radius: $border-radius-lg;
}

.currency-symbol {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  margin-right: $spacing-sm;
}

.amount-field {
  flex: 1;
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.quick-amounts {
  @include flex-center;
  flex-wrap: wrap;
  gap: $spacing-sm;
  margin-bottom: $spacing-xl;
}

.amount-item {
  flex: 0 0 calc(33.33% - #{$spacing-sm} * 2 / 3);
  @include flex-center;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-lg;
  border: 2rpx solid transparent;

  &:active {
    border-color: $primary-color;
    background-color: rgba(255, 107, 53, 0.05);
  }
}

.amount-text {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
}

.payment-methods {
  margin-bottom: $spacing-xl;
}

.methods-title {
  font-size: $font-size-base;
  color: $text-color-primary;
  margin-bottom: $spacing-md;
}

.method-list {
  @include flex-center-column;
  gap: $spacing-sm;
}

.method-item {
  @include flex-center;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-lg;
  border: 2rpx solid transparent;
  position: relative;

  &.active {
    border-color: $primary-color;
    background-color: rgba(255, 107, 53, 0.05);
  }

  &:active {
    background-color: rgba(255, 107, 53, 0.1);
  }
}

.method-icon {
  font-size: $font-size-xl;
  margin-right: $spacing-md;
}

.method-name {
  flex: 1;
  font-size: $font-size-base;
  color: $text-color-primary;
}

.method-check {
  position: absolute;
  right: $spacing-md;
  top: 50%;
  transform: translateY(-50%);
  width: 32rpx;
  height: 32rpx;
  @include flex-center;
  background-color: $primary-color;
  color: #fff;
  border-radius: 50%;
  font-size: $font-size-sm;
  font-weight: $font-weight-bold;
}

.confirm-btn {
  width: 100%;
  height: 88rpx;
  @include flex-center;
  background: linear-gradient(135deg, $primary-color, #FF8F61);
  color: #fff;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  border-radius: $border-radius-round;
  border: none;

  &:active {
    transform: scale(0.98);
  }

  &[disabled] {
    opacity: 0.5;
  }
}
</style>
