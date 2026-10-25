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
import { walletApi } from '@/api'
import { useUserStore } from '@/store'

const userStore = useUserStore()

const getCurrentUserId = () => {
  return userStore.userInfo?.userId || userStore.userInfo?.id || uni.getStorageSync('userId') || ''
}

const normalizeType = (type) => {
  const types = ['recharge', 'consume', 'withdraw', 'refund']
  return types.includes(type) ? type : 'recharge'
}

const normalizeTransactionType = (type) => {
  if (type === 'income') {
    return 'recharge'
  }
  if (type === 'expense') {
    return 'consume'
  }
  return normalizeType(type)
}

const normalizeTransactionItem = (record) => {
  const type = normalizeTransactionType(record?.type)
  const createTime = record?.createTime || record?.time || Date.now()
  const amount = Number(record?.amount || 0)
  return {
    id: record?.id || `${type}-${Date.now()}-${Math.random()}`,
    type: ['recharge', 'withdraw', 'consume', 'refund'].includes(type) ? (type === 'recharge' ? 'income' : 'expense') : 'expense',
    name: type === 'recharge'
      ? '账户充值'
      : type === 'consume'
        ? '订单消费'
        : type === 'withdraw'
          ? '余额提现'
          : '其他交易',
    icon: type === 'recharge'
      ? '💰'
      : type === 'consume'
        ? '🛒'
        : type === 'withdraw'
          ? '💸'
          : '💳',
    time: new Date(createTime).toLocaleString(),
    amount: Number.isFinite(amount) ? amount : 0,
    statusText: record?.status === 'failed' ? '失败' : '成功',
    status: record?.status || 'success'
  }
}

const resolveApiType = (uiTab) => {
  if (uiTab === 'income') return 'recharge'
  if (uiTab === 'expense') return 'consume'
  return uiTab
}

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
  if (!userStore.isLogin) return
  try {
    const userId = getCurrentUserId()
    const res = await walletApi.getInfo({ userId })

    const amount = Number((res?.balance || res?.data?.balance || 0)).toFixed(2)
    balance.value = amount
  } catch (error) {
    console.error('加载钱包数据失败:', error)
    uni.showToast({
      title: '余额加载失败',
      icon: 'none'
    })
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
    if (!userStore.isLogin) {
      return
    }

    const userId = getCurrentUserId()
    const params = {
      userId,
      page: page.value,
      pageSize: pageSize.value
    }

    if (activeTab.value !== 'all') {
      params.type = resolveApiType(activeTab.value)
    }

    const res = await walletApi.getTransactions(params)
    const rawList = res.list || res.data?.list || []
    const list = rawList.map((item) => normalizeTransactionItem(item))

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
const handleRecharge = () => {
  rechargeAmount.value = ''
  rechargePopupRef.value?.open()
}

/**
 * 提现
 */
const handleWithdraw = () => {
  if (!userStore.isLogin) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }

  const options = ['10元', '20元', '50元', '100元', '200元', '500元']

  uni.showActionSheet({
    itemList: options,
    success: async ({ tapIndex }) => {
      if (tapIndex < 0 || tapIndex >= options.length) return
      const amount = Number(options[tapIndex].replace('元', ''))

      try {
        const res = await walletApi.withdraw({
          userId: getCurrentUserId(),
          amount,
          withdrawMethod: 'wechat'
        })

        if (res.code === 200 || res.code === '200') {
          uni.showToast({
            title: '提现申请已提交',
            icon: 'success'
          })
          await loadWalletData()
          await loadTransactions(false)
        } else {
          throw new Error(res.message || '提现失败')
        }
      } catch (error) {
        console.error('提现失败:', error)
        uni.showToast({
          title: error.message || '提现失败，请重试',
          icon: 'none'
        })
      }
    }
  })
}

/**
 * 确认充值
 */
const confirmRecharge = async (data) => {
  if (!userStore.isLogin) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }

  try {
    const amount = Number(data?.amount)
    const res = await walletApi.recharge({
      userId: getCurrentUserId(),
      amount,
      paymentMethod: data?.paymentMethod || 'wechat'
    })

    if (res.code === 200 || res.code === '200') {
      uni.showToast({
        title: '充值申请已提交',
        icon: 'success'
      })
      await loadWalletData()
      await loadTransactions(false)
      closeRechargePopup()
    } else {
      throw new Error(res.message || '充值失败')
    }
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
  rechargePopupRef.value?.close()
  rechargeAmount.value = ''
}

/**
 * 查看全部交易记录
 */
const viewAllTransactions = () => {
  uni.navigateTo({
    url: '/pages-user/wallet/transactions/index'
  })
}

/**
 * 查看交易详情
 */
const viewTransactionDetail = (item) => {
  uni.navigateTo({
    url: '/pages-user/wallet/transactions/index'
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
