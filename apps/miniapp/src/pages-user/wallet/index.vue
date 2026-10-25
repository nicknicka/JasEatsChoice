<template>
  <view class="wallet-container">
    <view class="wallet-card">
      <view class="card-header">
        <view>
          <text class="header-title">我的钱包</text>
          <text class="header-subtitle">余额可用于下单支付，充值与提现记录统一查看</text>
        </view>
        <view class="header-icon-wrapper">
          <uni-icons type="wallet-filled" size="26" color="#FFFFFF"></uni-icons>
        </view>
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
          <uni-icons type="plus-filled" size="18" color="#FF6B35"></uni-icons>
          <text>充值</text>
        </button>
        <button class="action-btn outline" @click="withdraw">
          <uni-icons type="redo-filled" size="18" color="#FFFFFF"></uni-icons>
          <text>提现</text>
        </button>
      </view>
    </view>

    <view class="transactions-section">
      <view class="section-header">
        <view>
          <text class="section-title">交易记录</text>
          <text class="section-subtitle">按日期归类查看收入与支出明细</text>
        </view>
        <view class="section-more" @click="viewAllTransactions">
          <text>查看全部</text>
          <uni-icons type="right" size="14" color="#FF6B35"></uni-icons>
        </view>
      </view>

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
        <view class="empty-state" v-if="transactions.length === 0 && !loading">
          <view class="empty-icon-wrapper">
            <uni-icons type="wallet-filled" size="42" color="#FF6B35"></uni-icons>
          </view>
          <text class="empty-title">还没有交易记录</text>
          <text class="empty-desc">充值、消费或退款后会在这里生成明细</text>
        </view>

        <view class="transaction-list" v-else>
          <view
            class="date-group"
            v-for="group in groupedTransactions"
            :key="group.date"
          >
            <view class="date-title">
              <text class="date-text">{{ group.dateText }}</text>
              <text class="date-amount">
                收入 +{{ formatAmount(group.income) }} / 支出 -{{ formatAmount(group.expense) }}
              </text>
            </view>

            <view class="transaction-items">
              <view
                class="transaction-item"
                v-for="item in group.items"
                :key="item.id"
                @click="viewTransactionDetail(item)"
              >
                <view class="transaction-icon" :class="item.type">
                  <uni-icons :type="item.icon" size="22" :color="item.iconColor"></uni-icons>
                </view>

                <view class="transaction-info">
                  <text class="transaction-name">{{ item.name }}</text>
                  <text class="transaction-time">{{ item.time }}</text>
                </view>

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
              <uni-icons type="weixin" size="20" color="#07C160"></uni-icons>
              <text class="method-name">微信支付</text>
              <view class="method-check" v-if="paymentMethod === 'wechat'">✓</view>
            </view>
            <view
              class="method-item"
              :class="{ active: paymentMethod === 'alipay' }"
              @click="selectPaymentMethod('alipay')"
            >
              <text class="method-badge alipay">支</text>
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

    <uni-popup ref="withdrawPopup" type="bottom">
      <view class="recharge-popup">
        <view class="popup-header">
          <text class="popup-title">余额提现</text>
          <text class="popup-close" @click="closeWithdrawPopup">×</text>
        </view>

        <view class="recharge-amount">
          <text class="amount-label">提现金额</text>
          <view class="amount-input">
            <text class="currency-symbol">¥</text>
            <input
              class="amount-field"
              type="digit"
              v-model="withdrawAmount"
              placeholder="请输入提现金额"
            />
          </view>
        </view>

        <view class="quick-amounts">
          <view
            class="amount-item"
            v-for="amount in quickAmounts"
            :key="`withdraw-${amount}`"
            @click="selectWithdrawAmount(amount)"
          >
            <text class="amount-text">{{ amount }}元</text>
          </view>
        </view>

        <view class="payment-methods">
          <text class="methods-title">提现方式</text>
          <view class="method-list">
            <view
              class="method-item"
              :class="{ active: withdrawMethod === 'wechat' }"
              @click="selectWithdrawMethod('wechat')"
            >
              <uni-icons type="weixin" size="20" color="#07C160"></uni-icons>
              <text class="method-name">微信零钱</text>
              <view class="method-check" v-if="withdrawMethod === 'wechat'">✓</view>
            </view>
            <view
              class="method-item"
              :class="{ active: withdrawMethod === 'alipay' }"
              @click="selectWithdrawMethod('alipay')"
            >
              <text class="method-badge alipay">支</text>
              <text class="method-name">支付宝</text>
              <view class="method-check" v-if="withdrawMethod === 'alipay'">✓</view>
            </view>
          </view>
        </view>

        <button class="confirm-btn" @click="confirmWithdraw" :disabled="!withdrawAmount">
          确认提现
        </button>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store'
import { walletApi } from '@/api'

// 用户信息store
const userStore = useUserStore()

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
const rechargePopup = ref(null)
const withdrawPopup = ref(null)
const withdrawAmount = ref('')
const withdrawMethod = ref('wechat')

const getCurrentUserId = () => {
  return userStore.userInfo?.userId || userStore.userInfo?.id || uni.getStorageSync('userId') || ''
}

const formatAmount = (value) => {
  const amount = Number(value || 0)
  return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
}

const applyWalletAmount = (walletData) => {
  const amount = Number(walletData?.balance || 0)
  const [integer, decimal = '00'] = formatAmount(amount).split('.')
  balance.value = integer
  balanceDecimal.value = decimal
}

const resolveWalletTabType = (tabType) => {
  if (tabType === 'income') {
    return 'recharge'
  }
  if (tabType === 'expense') {
    return 'consume'
  }
  return 'all'
}

const resolveTransactionType = (type) => {
  if (type === 'recharge') {
    return 'income'
  }
  return 'expense'
}

const resolveTransactionMeta = (type) => {
  const map = {
    recharge: { icon: 'wallet-filled', iconColor: '#67C23A', name: '账户充值' },
    consume: { icon: 'cart-filled', iconColor: '#FF6B35', name: '订单消费' },
    withdraw: { icon: 'redo-filled', iconColor: '#E6A23C', name: '余额提现' },
    refund: { icon: 'undo-filled', iconColor: '#409EFF', name: '订单退款' },
    other: { icon: 'list', iconColor: '#909399', name: '交易' }
  }

  return map[type] || map.other
}

const resolveTransactionStatusText = (status) => {
  const map = {
    success: '交易成功',
    failed: '交易失败'
  }
  return map[status] || '交易成功'
}

const normalizeWalletTransaction = (record) => {
  const meta = resolveTransactionMeta(record?.type)
  const amount = Number(record?.amount || 0)
  const rawTime = record?.createTime || record?.time
  const timestamp = new Date(rawTime).getTime()

  return {
    id: record?.id || `${record?.type}-${Date.now()}-${Math.random()}`,
    type: resolveTransactionType(record?.type || 'other'),
    icon: meta.icon,
    iconColor: meta.iconColor,
    name: meta.name,
    amount: Number.isFinite(amount) ? amount : 0,
    status: record?.status,
    statusText: resolveTransactionStatusText(record?.status),
    time: formatTransactionTime(rawTime),
    timestamp: Number.isNaN(timestamp) ? Date.now() : timestamp
  }
}

const formatTransactionTime = (value) => {
  if (!value) {
    return ''
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return ''
  }

  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours().toString().padStart(2, '0')
  const minute = date.getMinutes().toString().padStart(2, '0')
  return `${month}月${day}日 ${hour}:${minute}`
}

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
  const groups = {}

  transactions.value.forEach(item => {
    const date = new Date(item.timestamp)
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
  if (!userStore.isLogin) {
    return
  }

  try {
    const userId = getCurrentUserId()
    const res = await walletApi.getInfo({ userId })
    if (res.code === 200 || res.code === '200') {
      applyWalletAmount(res.data || res)
    }
  } catch (error) {
    console.error('加载钱包数据失败:', error)
  }
}

/**
 * 加载交易记录
 */
const loadTransactions = async (showLoading = true) => {
  if (!userStore.isLogin) {
    return
  }

  if (showLoading) {
    loading.value = true
  }

  try {
    const userId = getCurrentUserId()
    const params = {
      userId,
      page: page.value,
      size: pageSize.value
    }

    const apiType = resolveWalletTabType(activeTab.value)
    if (apiType !== 'all') {
      params.type = apiType
    }

    const res = await walletApi.getTransactions(params)

    const rawList = res.list || res.data?.list || []
    const list = rawList.map((item) => normalizeWalletTransaction(item))
    if (page.value === 1) {
      transactions.value = list
    } else {
      transactions.value.push(...list)
    }

    hasMore.value = list.length >= pageSize.value
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
  if (!userStore.isLogin) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }

  rechargeAmount.value = ''
  paymentMethod.value = 'wechat'
  rechargePopup.value?.open()
}

/**
 * 提现
 */
const withdraw = () => {
  if (!userStore.isLogin) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }

  withdrawAmount.value = ''
  withdrawMethod.value = 'wechat'
  withdrawPopup.value?.open()
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
 * U-004: 确认充值
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
    uni.showLoading({
      title: '提交中...'
    })

    // U-004: 调用充值API
    const res = await walletApi.recharge({
      userId: getCurrentUserId(),
      amount: amount,
      paymentMethod: paymentMethod.value
    })

    uni.hideLoading()

    if (res.code === 200 || res.code === '200') {
      // 如果需要支付，跳转到支付页面
      if (res.data && res.data.needPay) {
        // 调起支付
        await handlePayment(res.data)
      } else {
        // 充值成功
        applyWalletAmount(res.data)
        uni.showToast({
          title: '充值成功',
          icon: 'success'
        })

        // 重新加载钱包数据
        await loadWalletData()
        await loadTransactions(false)

        // 关闭弹窗
        closeRechargePopup()
      }
    } else {
      throw new Error(res.message || '充值失败')
    }
  } catch (error) {
    console.error('充值失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error.message || '充值失败，请重试',
      icon: 'none'
    })
  }
}

/**
 * 处理支付
 */
const handlePayment = async (paymentData) => {
  try {
    // 根据支付方式调用不同的支付接口
    if (paymentMethod.value === 'wechat') {
      // 微信支付
      const payRes = await walletApi.wechatPay(paymentData)
      if ((payRes.code === 200 || payRes.code === '200') && payRes.data) {
        // 调起微信支付
        uni.requestPayment({
          provider: 'wxpay',
          ...payRes.data,
          success: () => {
            uni.showToast({
              title: '充值成功',
              icon: 'success'
            })
            loadWalletData()
            closeRechargePopup()
          },
          fail: (err) => {
            uni.showToast({
              title: '支付失败',
              icon: 'none'
            })
          }
        })
      }
    } else if (paymentMethod.value === 'alipay') {
      // 支付宝支付
      const payRes = await walletApi.alipayPay(paymentData)
      if ((payRes.code === 200 || payRes.code === '200') && payRes.data) {
        // H5支付跳转
        if (typeof window !== 'undefined') {
          window.location.href = payRes.data.payUrl
        }
      }
    }
  } catch (error) {
    console.error('支付失败:', error)
    uni.showToast({
      title: '支付失败',
      icon: 'none'
    })
  }
}

/**
 * 关闭充值弹窗
 */
const closeRechargePopup = () => {
  rechargePopup.value?.close()
  rechargeAmount.value = ''
}

/**
 * 关闭提现弹窗
 */
const closeWithdrawPopup = () => {
  withdrawPopup.value?.close()
  withdrawAmount.value = ''
}

/**
 * 选择提现金额
 */
const selectWithdrawAmount = (amount) => {
  withdrawAmount.value = amount.toString()
}

/**
 * 选择提现方式
 */
const selectWithdrawMethod = (method) => {
  withdrawMethod.value = method
}

/**
 * 确认提现
 */
const confirmWithdraw = async () => {
  if (!withdrawAmount.value) {
    uni.showToast({
      title: '请输入提现金额',
      icon: 'none'
    })
    return
  }

  const amount = parseFloat(withdrawAmount.value)
  if (amount <= 0) {
    uni.showToast({
      title: '提现金额必须大于0',
      icon: 'none'
    })
    return
  }

  const currentBalance = Number(`${balance.value}.${balanceDecimal.value}`)
  if (amount > currentBalance) {
    uni.showToast({
      title: '余额不足',
      icon: 'none'
    })
    return
  }

  try {
    uni.showLoading({
      title: '提交中...'
    })

    const res = await walletApi.withdraw({
      userId: getCurrentUserId(),
      amount,
      withdrawMethod: withdrawMethod.value
    })

    uni.hideLoading()

    if (res.code === 200 || res.code === '200') {
      uni.showToast({
        title: '提现申请已提交',
        icon: 'success'
      })

      await loadWalletData()
      await loadTransactions(false)
      closeWithdrawPopup()
    } else {
      throw new Error(res.message || '提现失败')
    }
  } catch (error) {
    console.error('提现失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error.message || '提现失败，请重试',
      icon: 'none'
    })
  }
}

/**
 * 查看全部交易记录
 */
const viewAllTransactions = () => {
  uni.navigateTo({
    url: '/pages-user/wallet/transactions'
  })
}

/**
 * 查看交易详情
 */
const viewTransactionDetail = (item) => {
  uni.showModal({
    title: '交易详情',
    content: `${item.name}\n金额：¥${formatAmount(item.amount)}\n时间：${item.time || '暂无时间'}`,
    showCancel: false
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

.header-subtitle {
  display: block;
  margin-top: $spacing-xs;
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.82);
}

.header-icon-wrapper {
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  background-color: rgba(255, 255, 255, 0.16);
  @include flex-center;
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

.section-subtitle {
  display: block;
  margin-top: 8rpx;
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.section-more {
  display: flex;
  align-items: center;
  gap: 8rpx;
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
  @include flex-center-column;
  padding: 100rpx 0;
  text-align: center;
}

.empty-icon-wrapper {
  width: 104rpx;
  height: 104rpx;
  border-radius: 28rpx;
  background-color: #FFF3ED;
  @include flex-center;
  margin-bottom: $spacing-lg;
}

.empty-title {
  font-size: $font-size-lg;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
  margin-bottom: $spacing-sm;
}

.empty-desc {
  font-size: $font-size-sm;
  color: $text-color-secondary;
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

.method-badge {
  width: 40rpx;
  height: 40rpx;
  border-radius: 12rpx;
  @include flex-center;
  margin-right: $spacing-md;
  font-size: $font-size-sm;
  font-weight: $font-weight-bold;
  color: #FFFFFF;

  &.alipay {
    background-color: #1677FF;
  }
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
