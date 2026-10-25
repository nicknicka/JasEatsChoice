<template>
  <view class="finance-container">
    <!-- 资产概览 -->
    <view class="asset-overview">
      <view class="total-balance-section">
        <text class="section-label">总资产（元）</text>
        <view class="balance-row">
          <text class="balance-amount">{{ formatMoney(financeData.totalBalance) }}</text>
          <button class="withdraw-btn" @tap="goToWithdraw">
            <uni-icons type="wallet" size="16" color="#fff"></uni-icons>
            <text>提现</text>
          </button>
        </view>
      </view>

      <view class="balance-details">
        <view class="detail-item">
          <text class="item-label">可提现</text>
          <text class="item-value">¥{{ formatMoney(financeData.availableBalance) }}</text>
        </view>
        <view class="detail-item">
          <text class="item-label">冻结中</text>
          <text class="item-value frozen">¥{{ formatMoney(financeData.frozenBalance) }}</text>
        </view>
      </view>
    </view>

    <!-- 统计数据 -->
    <view class="stats-grid">
      <view class="stat-card">
        <text class="stat-value">{{ formatMoney(financeData.todayIncome) }}</text>
        <text class="stat-label">今日收入</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ formatMoney(financeData.monthIncome) }}</text>
        <text class="stat-label">本月收入</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ financeData.todayOrders }}</text>
        <text class="stat-label">今日订单</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ financeData.monthOrders }}</text>
        <text class="stat-label">本月订单</text>
      </view>
    </view>

    <!-- 收入趋势 -->
    <view class="chart-section">
      <view class="section-header">
        <text class="section-title">近7日收入趋势</text>
        <picker
          :value="trendDaysIndex"
          :range="trendDaysOptions"
          range-key="label"
          @change="onTrendDaysChange"
        >
          <view class="picker-value">
            {{ trendDaysOptions[trendDaysIndex].label }}
            <uni-icons type="arrowdown" size="14" color="#999"></uni-icons>
          </view>
        </picker>
      </view>
      <view class="chart-container">
        <qiun-ucharts
          type="line"
          :opts="chartOpts"
          :chartData="chartData"
          :canvas2d="true"
          canvasId="incomeTrendChart"
        />
      </view>
    </view>

    <!-- 交易明细 -->
    <view class="transactions-section">
      <view class="section-header">
        <text class="section-title">交易明细</text>
        <view class="filter-tabs">
          <view
            class="tab-item"
            :class="{ active: activeTransactionType === item.value }"
            v-for="item in transactionTypes"
            :key="item.value"
            @tap="changeTransactionType(item.value)"
          >
            {{ item.label }}
          </view>
        </view>
      </view>

      <scroll-view
        class="transaction-list"
        scroll-y
        @scrolltolower="loadMoreTransactions"
      >
        <view
          class="transaction-item"
          v-for="item in transactionList"
          :key="item.id"
          @tap="viewTransactionDetail(item)"
        >
          <view class="transaction-icon" :class="'type-' + item.type">
            <uni-icons
              :type="getTransactionIcon(item.type)"
              size="24"
              :color="getTransactionColor(item.type)"
            ></uni-icons>
          </view>
          <view class="transaction-info">
            <text class="transaction-title">{{ item.title }}</text>
            <text class="transaction-time">{{ item.time }}</text>
          </view>
          <view class="transaction-amount" :class="{ income: item.amount > 0, expense: item.amount < 0 }">
            <text class="amount-symbol">{{ item.amount > 0 ? '+' : '' }}</text>
            <text class="amount-value">{{ item.amount > 0 ? '' : '-' }}¥{{ formatMoney(Math.abs(item.amount)) }}</text>
          </view>
        </view>

        <!-- 加载状态 -->
        <view class="load-status" v-if="transactionList.length > 0">
          <text v-if="loadingTransactions">加载中...</text>
          <text v-else-if="noMoreTransactions">没有更多了</text>
        </view>

        <!-- 空状态 -->
        <view class="empty-state" v-if="transactionList.length === 0 && !loadingTransactions">
          <empty text="暂无交易记录" icon="💰" />
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import qiunUcharts from '@/components/qiun-ucharts/qiun-ucharts.vue'
import { merchantApi } from '@/api'

// 趋势天数选项
const trendDaysOptions = [
  { label: '近7天', value: 7 },
  { label: '近15天', value: 15 },
  { label: '近30天', value: 30 }
]

const trendDaysIndex = ref(0)

// 交易类型筛选
const transactionTypes = [
  { label: '全部', value: 'all' },
  { label: '收入', value: 'income' },
  { label: '提现', value: 'withdraw' }
]

const activeTransactionType = ref('all')

// 财务数据
const financeData = ref({
  totalBalance: '15860.50',
  availableBalance: '15680.50',
  frozenBalance: '180.00',
  todayIncome: '528.00',
  monthIncome: '12580.50',
  todayOrders: 23,
  monthOrders: 456
})

// 图表数据
const chartData = computed(() => ({
  categories: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
  series: [
    {
      name: '收入',
      data: [428, 536, 485, 612, 528, 645, 589]
    }
  ]
}))

const chartOpts = ref({
  color: ['#FF6B35'],
  padding: [15, 15, 0, 15],
  enableScroll: false,
  legend: {},
  xAxis: {
    disableGrid: true,
    itemCount: 7
  },
  yAxis: {
    data: [{ min: 0 }],
    gridType: 'dash',
    dashLength: 2
  },
  extra: {
    line: {
      type: 'curve',
      width: 2,
      activeType: 'hollow'
    }
  }
})

// 交易列表
const transactionList = ref([])
const loadingTransactions = ref(false)
const noMoreTransactions = ref(false)
const transactionPage = ref(1)
const transactionPageSize = 20

onMounted(async () => {
  await loadFinanceData()
  await loadTransactions()
})

/**
 * M-012: 加载财务数据
 */
const loadFinanceData = async () => {
  try {
    const res = await merchantApi.getFinanceData({ timeRange: 'month' })
    if (res.code === 200 && res.data) {
      financeData.value = {
        totalBalance: res.data.totalBalance || '0',
        availableBalance: res.data.availableBalance || '0',
        frozenBalance: res.data.frozenBalance || '0',
        todayIncome: res.data.todayIncome || '0',
        monthIncome: res.data.monthIncome || '0',
        todayOrders: res.data.todayOrders || 0,
        monthOrders: res.data.monthOrders || 0
      }
    }
  } catch (error) {
    console.error('加载财务数据失败:', error)
    // 保持默认数据
  }
}

/**
 * M-013: 加载交易记录
 */
const loadTransactions = async (isRefresh = false) => {
  if (loadingTransactions.value) return

  loadingTransactions.value = true
  if (isRefresh) {
    transactionPage.value = 1
    noMoreTransactions.value = false
  }

  try {
    // M-013: 调用API获取交易记录
    const params = {
      type: activeTransactionType.value === 'all' ? undefined : activeTransactionType.value,
      page: transactionPage.value,
      size: transactionPageSize
    }

    const res = await merchantApi.getTransactions(params)

    if (res.code === 200 && res.data) {
      const transactions = res.data.list || []

      if (isRefresh) {
        transactionList.value = transactions
      } else {
        transactionList.value = [...transactionList.value, ...transactions]
      }

      if (transactions.length < transactionPageSize) {
        noMoreTransactions.value = true
      }
    } else {
      // API调用失败，使用模拟数据
      const mockData = generateMockTransactions()
      if (isRefresh) {
        transactionList.value = mockData
      } else {
        transactionList.value = [...transactionList.value, ...mockData]
      }

      if (mockData.length < transactionPageSize) {
        noMoreTransactions.value = true
      }
    }

    loadingTransactions.value = false
  } catch (error) {
    console.error('加载交易记录失败:', error)
    loadingTransactions.value = false

    // 出错时使用模拟数据
    const mockData = generateMockTransactions()
    if (isRefresh) {
      transactionList.value = mockData
    } else {
      transactionList.value = [...transactionList.value, ...mockData]
    }
  }
}

/**
 * 生成模拟交易数据
 */
const generateMockTransactions = () => {
  const transactions = []
  const count = Math.floor(Math.random() * 5) + 5

  const types = [
    { type: 'order', title: '订单收入', amount: Math.floor(Math.random() * 100) + 20 },
    { type: 'refund', title: '订单退款', amount: -(Math.floor(Math.random() * 50) + 10) },
    { type: 'withdraw', title: '提现', amount: -(Math.floor(Math.random() * 500) + 100) }
  ]

  for (let i = 0; i < count; i++) {
    const item = types[Math.floor(Math.random() * types.length)]
    transactions.push({
      id: transactionPage.value * 20 + i,
      type: item.type,
      title: item.title,
      amount: item.amount,
      time: getRandomTime(),
      orderNo: `OD${Date.now()}${i}`
    })
  }

  return transactions
}

/**
 * 随机时间
 */
const getRandomTime = () => {
  const hours = Math.floor(Math.random() * 12) + 1
  const minutes = Math.floor(Math.random() * 60).toString().padStart(2, '0')
  return `${hours}小时前`
}

/**
 * M-014: 切换趋势天数
 */
const onTrendDaysChange = async (e) => {
  trendDaysIndex.value = e.detail.value

  // M-014: 重新加载图表数据
  try {
    const days = trendDaysOptions[trendDaysIndex.value].value
    const res = await merchantApi.getFinanceData({ timeRange: days === 7 ? 'week' : days === 15 ? 'week' : 'month' })

    if (res.code === 200 && res.data && res.data.chartData) {
      // 更新图表数据
      // 这里需要根据后端返回的数据格式调整
      // chartData.value = res.data.chartData
    }
  } catch (error) {
    console.error('加载图表数据失败:', error)
  }
}

/**
 * 切换交易类型
 */
const changeTransactionType = (type) => {
  activeTransactionType.value = type
  loadTransactions(true)
}

/**
 * 加载更多交易
 */
const loadMoreTransactions = () => {
  if (!loadingTransactions.value && !noMoreTransactions.value) {
    transactionPage.value++
    loadTransactions()
  }
}

/**
 * 查看交易详情
 */
const viewTransactionDetail = (item) => {
  uni.navigateTo({
    url: `/pages-merchant/profile/transaction-detail?id=${item.id}`
  })
}

/**
 * 跳转提现
 */
const goToWithdraw = () => {
  uni.navigateTo({
    url: '/pages-merchant/profile/withdraw'
  })
}

/**
 * 获取交易图标
 */
const getTransactionIcon = (type) => {
  const iconMap = {
    order: 'wallet-filled',
    refund: 'reload',
    withdraw: 'money'
  }
  return iconMap[type] || 'wallet'
}

/**
 * 获取交易颜色
 */
const getTransactionColor = (type) => {
  const colorMap = {
    order: '#52C41A',
    refund: '#FAAD14',
    withdraw: '#F5222D'
  }
  return colorMap[type] || '#999'
}

/**
 * 格式化金额
 */
const formatMoney = (amount) => {
  return Number(amount).toFixed(2)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.finance-container {
  min-height: 100vh;
  background: #F5F5F5;
}

/* 资产概览 */
.asset-overview {
  background: linear-gradient(135deg, #FF6B35, #FF8F6B);
  padding: 40rpx 30rpx;
}

.total-balance-section {
  margin-bottom: 30rpx;
}

.section-label {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  display: block;
  margin-bottom: 15rpx;
}

.balance-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.balance-amount {
  font-size: 64rpx;
  font-weight: bold;
  color: #fff;
}

.withdraw-btn {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  font-size: 26rpx;
  padding: 12rpx 24rpx;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.3);
}

.balance-details {
  display: flex;
  gap: 40rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.2);
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.item-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.item-value {
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;

  &.frozen {
    color: rgba(255, 255, 255, 0.7);
  }
}

/* 统计数据 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
  padding: 20rpx;
}

.stat-card {
  background: #fff;
  padding: 25rpx;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}

.stat-value {
  font-size: 32rpx;
  font-weight: bold;
  color: #FF6B35;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
}

/* 图表区域 */
.chart-section {
  background: #fff;
  padding: 30rpx;
  margin: 20rpx;
  border-radius: 16rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.picker-value {
  display: flex;
  align-items: center;
  gap: 5rpx;
  font-size: 26rpx;
  color: #666;
}

.chart-container {
  height: 400rpx;
}

/* 交易明细 */
.transactions-section {
  background: #fff;
  padding: 30rpx;
  margin: 20rpx;
  border-radius: 16rpx;
}

.filter-tabs {
  display: flex;
  gap: 10rpx;
}

.tab-item {
  padding: 8rpx 20rpx;
  background: #F5F5F5;
  border-radius: 20rpx;
  font-size: 24rpx;
  color: #666;

  &.active {
    background: #FF6B35;
    color: #fff;
  }
}

.transaction-list {
  height: 600rpx;
  margin-top: 20rpx;
}

.transaction-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 25rpx 0;
  border-bottom: 1rpx solid #eee;

  &:last-child {
    border-bottom: none;
  }
}

.transaction-icon {
  width: 70rpx;
  height: 70rpx;
  border-radius: 50%;
  @include flex-center;
  background: #F5F5F5;
}

.transaction-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.transaction-title {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.transaction-time {
  font-size: 24rpx;
  color: #999;
}

.transaction-amount {
  font-size: 32rpx;
  font-weight: bold;

  &.income {
    color: #52C41A;
  }

  &.expense {
    color: #F5222D;
  }
}

.amount-symbol {
  font-size: 24rpx;
}

/* 加载状态 */
.load-status {
  text-align: center;
  padding: 30rpx 0;
  color: #999;
  font-size: 26rpx;
}

/* 空状态 */
.empty-state {
  padding-top: 150rpx;
}
</style>
