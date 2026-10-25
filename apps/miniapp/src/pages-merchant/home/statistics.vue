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
        <text class="trend" :class="{ up: revenueTrend > 0, down: revenueTrend < 0 }">
          {{ revenueTrend > 0 ? '↑' : '↓' }} {{ Math.abs(revenueTrend) }}%
        </text>
      </view>
      <text class="revenue-value">¥{{ statistics.revenue }}</text>
      <text class="revenue-desc">较上期{{ revenueTrend > 0 ? '增长' : '下降' }}</text>
    </view>

    <!-- 核心指标 -->
    <view class="metrics-grid">
      <view class="metric-card">
        <text class="metric-value">{{ statistics.orders }}</text>
        <text class="metric-label">订单数</text>
        <text class="metric-trend" :class="{ up: statistics.ordersTrend > 0 }">
          {{ statistics.ordersTrend > 0 ? '+' : '' }}{{ statistics.ordersTrend }}%
        </text>
      </view>
      <view class="metric-card">
        <text class="metric-value">{{ statistics.customers }}</text>
        <text class="metric-label">顾客数</text>
        <text class="metric-trend" :class="{ up: statistics.customersTrend > 0 }">
          {{ statistics.customersTrend > 0 ? '+' : '' }}{{ statistics.customersTrend }}%
        </text>
      </view>
      <view class="metric-card">
        <text class="metric-value">¥{{ statistics.avgPrice }}</text>
        <text class="metric-label">客单价</text>
        <text class="metric-trend" :class="{ up: statistics.avgPriceTrend > 0 }">
          {{ statistics.avgPriceTrend > 0 ? '+' : '' }}{{ statistics.avgPriceTrend }}%
        </text>
      </view>
      <view class="metric-card">
        <text class="metric-value">{{ statistics.rating }}</text>
        <text class="metric-label">评分</text>
        <text class="metric-trend">
          {{ statistics.reviews }}条评价
        </text>
      </view>
    </view>

    <!-- 营业额趋势图 -->
    <view class="chart-section">
      <view class="section-title">营业额趋势</view>
      <view class="chart-container">
        <qiun-ucharts type="line" :opts="chartOpts" :chartData="chartData" />
      </view>
    </view>

    <!-- 热销菜品排行 -->
    <view class="ranking-section">
      <view class="section-title">热销菜品 TOP10</view>
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

    <!-- 订单时段分布 -->
    <view class="time-distribution">
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
              :style="{ width: (item.count / maxOrders * 100) + '%' }"
            ></view>
          </view>
        </view>
      </view>
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

const timeType = ref('today')

// 营业额趋势
const revenueTrend = ref(12.5)

// 统计数据
const statistics = ref({
  revenue: '1,680',
  orders: 28,
  ordersTrend: 15,
  customers: 32,
  customersTrend: 8,
  avgPrice: '52.5',
  avgPriceTrend: -3,
  rating: 4.8,
  reviews: 156
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
  return Math.max(...timeDistribution.value.map(item => item.count))
})

onMounted(() => {
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
 * 加载统计数据
 */
const loadStatistics = () => {
  // TODO: 调用API获取统计数据
  // const res = await merchantApi.getStatistics({ type: timeType.value })

  // 模拟数据
  if (timeType.value === 'today') {
    chartData.value = {
      categories: ['8:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00'],
      series: [{
        name: '营业额',
        data: [0, 120, 680, 350, 180, 420, 150]
      }]
    }

    timeDistribution.value = [
      { period: '早餐时段 (7:00-9:00)', count: 3 },
      { period: '午餐时段 (11:00-13:00)', count: 18 },
      { period: '下午茶 (14:00-16:00)', count: 5 },
      { period: '晚餐时段 (17:00-19:00)', count: 15 },
      { period: '夜宵时段 (20:00-22:00)', count: 8 }
    ]

    hotDishes.value = [
      { id: 1, name: '宫保鸡丁', sales: 25, revenue: 700 },
      { id: 2, name: '鱼香肉丝', sales: 22, revenue: 572 },
      { id: 3, name: '回锅肉', sales: 18, revenue: 576 },
      { id: 4, name: '麻婆豆腐', sales: 15, revenue: 270 },
      { id: 5, name: '水煮鱼', sales: 12, revenue: 480 }
    ]
  } else if (timeType.value === 'week') {
    const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    chartData.value = {
      categories: weekDays,
      series: [{
        name: '营业额',
        data: [1200, 1500, 1350, 1680, 1420, 2100, 1950]
      }]
    }

    hotDishes.value = [
      { id: 1, name: '宫保鸡丁', sales: 156, revenue: 4368 },
      { id: 2, name: '鱼香肉丝', sales: 142, revenue: 3692 },
      { id: 3, name: '回锅肉', sales: 128, revenue: 4096 },
      { id: 4, name: '水煮鱼', sales: 98, revenue: 3920 },
      { id: 5, name: '麻婆豆腐', sales: 87, revenue: 1566 }
    ]
  } else {
    const days = []
    for (let i = 1; i <= 30; i++) {
      days.push(`${i}日`)
    }

    chartData.value = {
      categories: days,
      series: [{
        name: '营业额',
        data: Array.from({ length: 30 }, () => Math.floor(Math.random() * 2000) + 1000)
      }]
    }

    hotDishes.value = [
      { id: 1, name: '宫保鸡丁', sales: 680, revenue: 19040 },
      { id: 2, name: '鱼香肉丝', sales: 620, revenue: 16120 },
      { id: 3, name: '回锅肉', sales: 550, revenue: 17600 },
      { id: 4, name: '水煮鱼', sales: 420, revenue: 16800 },
      { id: 5, name: '麻婆豆腐', sales: 380, revenue: 6840 }
    ]
  }
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

.trend {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.9);
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  background: rgba(255, 255, 255, 0.2);

  &.up {
    background: rgba(82, 196, 26, 0.3);
  }

  &.down {
    background: rgba(245, 34, 34, 0.3);
  }
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
