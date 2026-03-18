<template>
  <view class="wallet-container">
    <!-- 钱包卡片 -->
    <WalletCard
      :balance="balance"
      @recharge="handleRecharge"
      @withdraw="handleWithdraw"
    />

    <!-- 交易记录 -->
    <TransactionList
      :transactions="transactions"
      :activeTab="activeTab"
      :loading="loading"
      :refreshing="refreshing"
      :hasMore="hasMore"
      @tabChange="changeTab"
      @refresh="onRefresh"
      @loadMore="onLoadMore"
      @viewAll="viewAllTransactions"
      @itemClick="viewTransactionDetail"
    />

    <!-- 充值弹窗 -->
    <RechargePopup
      ref="rechargePopupRef"
      v-model="rechargeAmount"
      :quickAmounts="[10, 20, 50, 100, 200, 500]"
      @confirm="confirmRecharge"
      @close="closeRechargePopup"
    />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import WalletCard from './components/WalletCard.vue'
import TransactionList from './components/TransactionList.vue'
import RechargePopup from './components/RechargePopup.vue'
import api from '@/api'

// 账户余额
const balance = ref('0')

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
const rechargePopupRef = ref(null)

/**
 * 加载钱包数据
 */
const loadWalletData = async () => {
  try {
    const res = await api.user.getWalletInfo()
    const amount = parseFloat(res.data.balance).toFixed(2)
    balance.value = amount
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
const handleRecharge = () => {
  rechargeAmount.value = ''
  rechargePopupRef.value?.open()
}

/**
 * 提现
 */
const handleWithdraw = () => {
  uni.showModal({
    title: '提现',
    content: '提现功能开发中',
    showCancel: false
  })
}

/**
 * 确认充值
 */
const confirmRecharge = async (data) => {
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

.wallet-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  padding-bottom: $spacing-md;
}
</style>
