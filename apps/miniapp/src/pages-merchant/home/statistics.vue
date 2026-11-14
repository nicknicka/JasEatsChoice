<template>
  <view class="statistics-container">
    <!-- 时间选择 -->
    <view class="time-tabs">
      <view
        class="tab-item"
        :class="{ active: timeType === 'today' }"
        @tap="changeTimeType('today')"
      >
        今日
      </view>
      <view
        class="tab-item"
        :class="{ active: timeType === 'week' }"
        @tap="changeTimeType('week')"
      >
        本周
      </view>
      <view
        class="tab-item"
        :class="{ active: timeType === 'month' }"
        @tap="changeTimeType('month')"
      >
        本月
      </view>
    </view>

    <!-- 营业额概览 -->
    <view class="revenue-card">
      <view class="revenue-header">
        <text class="label">营业额</text>
      </view>
      <text class="revenue-value">¥{{ statistics.revenue }}</text>
      <text class="revenue-desc">{{ timeRangeLabel }}累计</text>
    </view>

    <!-- 核心指标 -->
    <view class="metrics-grid">
      <view class="metric-card">
        <text class="metric-value">{{ statistics.orders }}</text>
        <text class="metric-label">订单数</text>
      </view>
      <view class="metric-card">
        <text class="metric-value">{{ statistics.customers }}</text>
        <text class="metric-label">顾客数</text>
      </view>
      <view class="metric-card">
        <text class="metric-value">¥{{ statistics.avgPrice }}</text>
        <text class="metric-label">客单价</text>
      </view>
      <view class="metric-card">
        <text class="metric-value">{{ statistics.rating }}</text>
        <text class="metric-label">评分</text>
        <text class="metric-trend">
          {{ statistics.reviews }}条评价
        </text>
      </view>
    </view>

    <!-- 订单趋势图 -->
    <view class="chart-section">
      <view class="section-title">订单趋势</view>
      <view class="chart-container">
        <qiun-ucharts type="line" :opts="chartOpts" :chartData="chartData" />
      </view>
    </view>

    <!-- 热销菜品排行 -->
    <view class="ranking-section" v-if="hotDishes.length > 0">
      <view class="section-title">热销菜品 TOP5</view>
      <view class="ranking-list">
        <view
          class="ranking-item"
          v-for="(dish, index) in hotDishes"
          :key="dish.id"
        >
          <view class="ranking-number" :class="'rank-' + (index + 1)">
            {{ index + 1 }}
          </view>
          <view class="dish-info">
            <text class="dish-name">{{ dish.name }}</text>
            <text class="dish-sales">售出 {{ dish.sales }}份</text>
          </view>
          <text class="dish-revenue">¥{{ dish.revenue }}</text>
        </view>
      </view>
    </view>

    <!-- 订单时段分布（仅今日可由后端小时数据推导） -->
    <view class="time-distribution" v-if="timeDistribution.length > 0">
      <view class="section-title">订单时段分布</view>
      <view class="time-bars">
        <view
          class="time-bar-item"
          v-for="item in timeDistribution"
          :key="item.period"
        >
          <view class="bar-header">
            <text class="period">{{ item.period }}</text>
            <text class="count">{{ item.count }}单</text>
          </view>
          <view class="bar-wrapper">
            <view
              class="bar-fill"
              :style="{ width: (maxOrders > 0 ? (item.count / maxOrders * 100) : 0) + '%' }"
            ></view>
          </view>
        </view>
      </view>
    </view>

    <!-- 加载 / 错误 / 空态 -->
    <view class="state-block" v-if="loading">
      <text class="state-text">加载中...</text>
    </view>
    <view class="state-block" v-else-if="loadError">
      <text class="state-text">{{ loadError }}</text>
      <button class="state-btn" @tap="loadStatistics">重试</button>
    </view>
    <view class="state-block" v-else-if="isEmpty">
      <text class="state-text">{{ timeRangeLabel }}暂无经营数据</text>
      <button class="state-btn" @tap="loadStatistics">刷新</button>
    </view>

    <!-- 数据导出 -->
    <view class="export-section">
      <button class="export-btn" @tap="exportData">
        <text class="export-icon">📥</text>
        <text>导出数据报表</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { merchantApi } from '@/api/modules/merchant.js'
import { reviewApi } from '@/api/modules/review.js'

const timeType = ref('today')

const TIME_RANGE_LABELS = {
  today: '今日',
  week: '本周',
  month: '本月'
}

const timeRangeLabel = computed(() => TIME_RANGE_LABELS[timeType.value] || '当前周期')

// 加载与错误状态
const loading = ref(false)
const loadError = ref('')

// 当前登录商家 ID
const merchantId = ref('')

// 统计数据
const statistics = ref({
  revenue: '0',
  orders: 0,
  customers: 0,
  avgPrice: '0',
  rating: '-',
  reviews: 0
})

// 图表配置
const chartOpts = ref({
  color: ['#FF6B35'],
  padding: [15, 15, 0, 5],
  dataLabel: false,
  xAxis: {
    disableGrid: true
  },
  yAxis: {
    data: [{ min: 0 }]
  },
  extra: {
    line: {
      type: 'curve',
      width: 2
    }
  }
})

// 图表数据
const chartData = ref({})

// 热销菜品
const hotDishes = ref([])

// 订单时段分布
const timeDistribution = ref([])

// 最大订单数（用于计算进度条）
const maxOrders = computed(() => {
  const counts = timeDistribution.value.map(item => Number(item.count) || 0)
  return counts.length > 0 ? Math.max(...counts) : 0
})

// 是否存在可展示的经营数据（无数据时走空态，不回填任何假数据）
const isEmpty = computed(() => {
  const hasTrend = (chartData.value?.series?.[0]?.data || []).some(v => Number(v) > 0)
  return statistics.value.orders === 0 && !hasTrend && hotDishes.value.length === 0
})

onMounted(() => {
  merchantId.value = uni.getStorageSync('merchantId') || ''
  loadStatistics()
})

/**
 * 切换时间类型
 */
const changeTimeType = (type) => {
  timeType.value = type
  loadStatistics()
}

/**
 * 把后端 orderTrend（按小时/天/月）折算成「时段分布」。
 * 仅使用服务端返回的原始点位，不做任何推断或填充。
 */
const buildTimeDistribution = (orderTrend, range) => {
  if (!Array.isArray(orderTrend) || orderTrend.length === 0) return []

  if (range === 'today') {
    // 小时点位 → 五个时段分桶，桶内数值来自服务端各小时点数
    const buckets = [
      { period: '早餐时段 (7:00-9:00)', hours: [7, 8, 9] },
      { period: '午餐时段 (11:00-13:00)', hours: [11, 12, 13] },
      { period: '下午茶 (14:00-16:00)', hours: [14, 15, 16] },
      { period: '晚餐时段 (17:00-19:00)', hours: [17, 18, 19] },
      { period: '夜宵时段 (20:00-22:00)', hours: [20, 21, 22] }
    ]
    const byHour = {}
    orderTrend.forEach(item => {
      const hour = parseInt(String(item.time).split(':')[0], 10)
      if (!Number.isNaN(hour)) byHour[hour] = Number(item.orders) || 0
    })
    return buckets.map(b => ({
      period: b.period,
      count: b.hours.reduce((sum, h) => sum + (byHour[h] || 0), 0)
    }))
  }

  // 按天/月的点位直接作为分布项展示
  return orderTrend.map(item => ({
    period: String(item.time),
    count: Number(item.orders) || 0
  }))
}

/**
 * 加载统计数据（数据全部来自服务端；失败展示错误态 + 重试）
 */
const loadStatistics = async () => {
  if (loading.value) return

  if (!merchantId.value) {
    loadError.value = '未获取到商家信息，请重新登录'
    return
  }

  loading.value = true
  loadError.value = ''

  try {
    // 经营数据：/v1/merchant/{merchantId}/statistics?timeRange=today|week|month
    const res = await merchantApi.getStatistics(merchantId.value, { timeRange: timeType.value })

    if (!res || res.code !== 200 || !res.data) {
      throw new Error(res?.message || '获取统计数据失败')
    }

    const { basicStats = {}, orderTrend = [], dishSalesRank = [] } = res.data

    statistics.value = {
      revenue: formatAmount(basicStats.totalAmount),
      orders: Number(basicStats.orders) || 0,
      customers: Number(basicStats.newCustomers) || 0,
      avgPrice: (Number(basicStats.avgAmount) || 0).toFixed(2),
      rating: statistics.value.rating,
      reviews: statistics.value.reviews
    }

    // 订单趋势图（后端今日/昨日按小时，周按星期，月按日，all 按月）
    chartData.value = {
      categories: orderTrend.map(item => String(item.time)),
      series: [{
        name: '订单数',
        data: orderTrend.map(item => Number(item.orders) || 0)
      }]
    }

    timeDistribution.value = buildTimeDistribution(orderTrend, timeType.value)

    hotDishes.value = (Array.isArray(dishSalesRank) ? dishSalesRank : []).map((dish, index) => ({
      id: `${index}-${dish.name}`,
      name: dish.name,
      sales: Number(dish.sales) || 0,
      revenue: formatAmount(dish.revenue)
    }))

    // 评分与评价数：/v1/reviews/merchant/{merchantId}/statistics
    await loadReviewStats()
  } catch (error) {
    console.error('加载统计数据失败:', error)
    loadError.value = error?.message || '统计数据加载失败'
    chartData.value = {}
    hotDishes.value = []
    timeDistribution.value = []
    statistics.value = {
      revenue: '0',
      orders: 0,
      customers: 0,
      avgPrice: '0',
      rating: '-',
      reviews: 0
    }
  } finally {
    loading.value = false
  }
}

/**
 * 加载评价统计（评分 / 评价条数），失败不影响主数据展示
 */
const loadReviewStats = async () => {
  try {
    const res = await reviewApi.getStatistics(merchantId.value)
    if (res && res.code === 200 && res.data) {
      statistics.value = {
        ...statistics.value,
        rating: res.data.avgRating != null ? res.data.avgRating : '-',
        reviews: Number(res.data.total) || 0
      }
    }
  } catch (error) {
    console.error('加载评价统计失败:', error)
  }
}

/**
 * 金额格式化：保留两位小数并加千分位
 */
const formatAmount = (value) => {
  const num = Number(value)
  if (!Number.isFinite(num)) return '0.00'
  return num.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

/**
 * 导出数据
 */
const exportData = () => {
  uni.showActionSheet({
    itemList: ['导出Excel报表', '导出PDF报表'],
    success: (res) => {
      uni.showToast({
        title: '导出功能开发中',
        icon: 'none'
      })
    }
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.statistics-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 40rpx;
}

/* 时间选择 */
.time-tabs {
  background: #fff;
  display: flex;
  padding: 20rpx;
  gap: 20rpx;
}

.tab-item {
  flex: 1;
  height: 70rpx;
  border-radius: 35rpx;
  background: #F5F5F5;
  @include flex-center;
  font-size: 28rpx;
  color: #666;

  &.active {
    background: #FF6B35;
    color: #fff;
    font-weight: bold;
  }
}

/* 营业额卡片 */
.revenue-card {
  background: linear-gradient(135deg, #FF6B35, #FF8F6B);
  margin: 20rpx;
  padding: 40rpx 30rpx;
  border-radius: 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15rpx;
}

.revenue-header {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.label {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

.revenue-value {
  font-size: 64rpx;
  font-weight: bold;
  color: #fff;
}

.revenue-desc {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* 核心指标 */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  padding: 0 20rpx;
  margin-bottom: 20rpx;
}

.metric-card {
  background: #fff;
  padding: 30rpx;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}

.metric-value {
  font-size: 48rpx;
  font-weight: bold;
  color: #FF6B35;
}

.metric-label {
  font-size: 26rpx;
  color: #666;
}

.metric-trend {
  font-size: 24rpx;
  color: #999;

  &.up {
    color: #52C41A;
  }
}

/* 图表区域 */
.chart-section,
.ranking-section,
.time-distribution {
  background: #fff;
  margin: 0 20rpx 20rpx;
  padding: 30rpx;
  border-radius: 16rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.chart-container {
  height: 400rpx;
}

/* 排行榜 */
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
}

.ranking-number {
  width: 50rpx;
  height: 50rpx;
  @include flex-center;
  font-size: 28rpx;
  font-weight: bold;
  color: #999;
  background: #E8E8E8;
  border-radius: 8rpx;

  &.rank-1 {
    background: linear-gradient(135deg, #FFD700, #FFA500);
    color: #fff;
  }

  &.rank-2 {
    background: linear-gradient(135deg, #C0C0C0, #A8A8A8);
    color: #fff;
  }

  &.rank-3 {
    background: linear-gradient(135deg, #CD7F32, #B87333);
    color: #fff;
  }
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.dish-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.dish-sales {
  font-size: 24rpx;
  color: #999;
}

.dish-revenue {
  font-size: 32rpx;
  font-weight: bold;
  color: #FF6B35;
}

/* 时段分布 */
.time-bars {
  display: flex;
  flex-direction: column;
  gap: 25rpx;
}

.time-bar-item {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.bar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.period {
  font-size: 26rpx;
  color: #333;
}

.count {
  font-size: 26rpx;
  color: #FF6B35;
  font-weight: bold;
}

.bar-wrapper {
  height: 16rpx;
  background: #F5F5F5;
  border-radius: 8rpx;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #FF6B35, #FF8F6B);
  border-radius: 8rpx;
  transition: width 0.3s;
}

/* 导出按钮 */
.export-section {
  padding: 0 20rpx;
}

/* 加载 / 错误 / 空态 */
.state-block {
  @include flex-center;
  flex-direction: column;
  gap: 20rpx;
  padding: 80rpx 40rpx;
  margin: 0 20rpx 20rpx;
  background: #fff;
  border-radius: 16rpx;
}

.state-text {
  font-size: 28rpx;
  color: #999;
}

.state-btn {
  min-width: 200rpx;
  height: 72rpx;
  @include flex-center;
  background: linear-gradient(135deg, #FF6B35, #FF8F61);
  color: #fff;
  font-size: 28rpx;
  border-radius: 36rpx;
  border: none;
}

.export-btn {
  width: 100%;
  height: 90rpx;
  background: #fff;
  border: 2rpx solid #FF6B35;
  border-radius: 45rpx;
  @include flex-center;
  gap: 15rpx;
  font-size: 28rpx;
  color: #FF6B35;
}

.export-icon {
  font-size: 32rpx;
}
</style>
