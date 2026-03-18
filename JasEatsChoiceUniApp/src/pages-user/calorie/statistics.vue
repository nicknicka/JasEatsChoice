<template>
  <view class="statistics-container">
    <!-- 切换按钮 -->
    <view class="tab-buttons">
      <view
        class="tab-btn"
        :class="{ active: period === 'week' }"
        @tap="changePeriod('week')"
      >
        周统计
      </view>
      <view
        class="tab-btn"
        :class="{ active: period === 'month' }"
        @tap="changePeriod('month')"
      >
        月统计
      </view>
    </view>

    <!-- 统计概览 -->
    <view class="overview-cards">
      <view class="overview-card">
        <text class="card-value">{{ averageCalories }}</text>
        <text class="card-label">日均摄入</text>
      </view>
      <view class="overview-card">
        <text class="card-value">{{ targetAchieved }}%</text>
        <text class="card-label">目标达成</text>
      </view>
      <view class="overview-card">
        <text class="card-value">{{ totalDays }}</text>
        <text class="card-label">记录天数</text>
      </view>
    </view>

    <!-- 图表区域 -->
    <view class="chart-section">
      <view class="section-title">卡路里趋势</view>
      <view class="chart-container">
        <qiun-ucharts type="line" :opts="chartOpts" :chartData="chartData" />
      </view>
    </view>

    <!-- 营养成分分析 -->
    <view class="nutrition-section">
      <view class="section-title">营养成分分析</view>
      <view class="nutrition-chart">
        <qiun-ucharts type="pie" :opts="pieOpts" :chartData="pieData" />
      </view>
      <view class="nutrition-legend">
        <view class="legend-item" v-for="item in nutritionLegend" :key="item.name">
          <view class="legend-color" :style="{ background: item.color }"></view>
          <text class="legend-name">{{ item.name }}</text>
          <text class="legend-value">{{ item.value }}%</text>
        </view>
      </view>
    </view>

    <!-- 详细数据列表 -->
    <view class="detail-section">
      <view class="section-title">详细数据</view>
      <scroll-view scroll-y class="detail-list">
        <view class="detail-item" v-for="item in detailList" :key="item.date">
          <view class="detail-date">
            <text class="date-text">{{ item.date }}</text>
            <text class="weekday-text">{{ item.weekday }}</text>
          </view>
          <view class="detail-info">
            <text class="detail-calories">{{ item.calories }} kcal</text>
            <view class="detail-progress">
              <view class="progress-bar">
                <view
                  class="progress-fill"
                  :style="{
                    width: (item.calories / targetCalories * 100) + '%',
                    background: item.calories > targetCalories ? '#FF6B35' : '#52C41A'
                  }"
                ></view>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 建议卡片 -->
    <view class="suggestion-card">
      <view class="suggestion-header">
        <uni-icons type="info" size="20" color="#FF6B35"></uni-icons>
        <text class="suggestion-title">健康建议</text>
      </view>
      <view class="suggestion-content">
        <text class="suggestion-text">{{ suggestion }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const period = ref('week')
const targetCalories = ref(2000)

const averageCalories = ref(1850)
const targetAchieved = ref(92)
const totalDays = ref(7)

const chartData = ref({})
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

const pieData = ref({})
const pieOpts = ref({
  color: ['#FF6B35', '#52C41A', '#1890FF', '#FAAD14', '#F5222D'],
  padding: [5, 5, 5, 5],
  extra: {
    pie: {
      activeOpacity: 0.5,
      activeRadius: 10
    }
  }
})

const nutritionLegend = ref([
  { name: '碳水化合物', value: 45, color: '#FF6B35' },
  { name: '蛋白质', value: 20, color: '#52C41A' },
  { name: '脂肪', value: 25, color: '#1890FF' },
  { name: '膳食纤维', value: 8, color: '#FAAD14' },
  { name: '其他', value: 2, color: '#F5222D' }
])

const detailList = ref([])
const suggestion = ref('')

onMounted(() => {
  loadStatistics()
})

const changePeriod = (newPeriod) => {
  period.value = newPeriod
  loadStatistics()
}

const loadStatistics = () => {
  // TODO: 调用API获取统计数据
  if (period.value === 'week') {
    loadWeekData()
  } else {
    loadMonthData()
  }
}

const loadWeekData = () => {
  // 模拟周数据
  chartData.value = {
    categories: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
    series: [{
      name: '卡路里',
      data: [1750, 1920, 1680, 2050, 1880, 2200, 1750]
    }]
  }

  pieData.value = {
    series: [{
      data: [
        { name: '碳水化合物', value: 45 },
        { name: '蛋白质', value: 20 },
        { name: '脂肪', value: 25 },
        { name: '膳食纤维', value: 8 },
        { name: '其他', value: 2 }
      ]
    }]
  }

  totalDays.value = 7
  averageCalories.value = 1890
  targetAchieved.value = Math.round((1890 / 2000) * 100)

  const today = new Date()
  detailList.value = []
  for (let i = 6; i >= 0; i--) {
    const date = new Date(today)
    date.setDate(date.getDate() - i)

    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    const calories = Math.floor(Math.random() * 800) + 1500

    detailList.value.push({
      date: `${date.getMonth() + 1}/${date.getDate()}`,
      weekday: weekdays[date.getDay()],
      calories: calories
    })
  }

  suggestion.value = '本周平均摄入1890kcal，略低于目标2000kcal。建议适当增加优质蛋白质摄入，保持营养均衡。周末摄入较高，可适当控制。'
}

const loadMonthData = () => {
  // 模拟月数据
  const categories = []
  const data = []

  for (let i = 1; i <= 30; i++) {
    categories.push(`${i}日`)
    data.push(Math.floor(Math.random() * 800) + 1500)
  }

  chartData.value = {
    categories,
    series: [{
      name: '卡路里',
      data
    }]
  }

  pieData.value = {
    series: [{
      data: [
        { name: '碳水化合物', value: 48 },
        { name: '蛋白质', value: 18 },
        { name: '脂肪', value: 22 },
        { name: '膳食纤维', value: 10 },
        { name: '其他', value: 2 }
      ]
    }]
  }

  totalDays.value = 28
  averageCalories.value = 1820
  targetAchieved.value = Math.round((1820 / 2000) * 100)

  suggestion.value = '本月平均摄入1820kcal，低于目标2000kcal。营养结构较为均衡，建议继续保持当前饮食习惯，注意周末不要暴饮暴食。'
}
</script>

<style lang="scss" scoped>
.statistics-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 40rpx;
}

.tab-buttons {
  display: flex;
  background: #fff;
  padding: 20rpx 30rpx;
  gap: 20rpx;
}

.tab-btn {
  flex: 1;
  height: 70rpx;
  border-radius: 35rpx;
  background: #F5F5F5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: #666;

  &.active {
    background: #FF6B35;
    color: #fff;
  }
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
  padding: 30rpx;
}

.overview-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}

.card-value {
  font-size: 40rpx;
  font-weight: bold;
  color: #FF6B35;
}

.card-label {
  font-size: 24rpx;
  color: #999;
}

.chart-section,
.nutrition-section,
.detail-section {
  background: #fff;
  margin: 20rpx 30rpx;
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

.nutrition-chart {
  height: 350rpx;
}

.nutrition-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  margin-top: 30rpx;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 10rpx;
  width: 45%;
}

.legend-color {
  width: 30rpx;
  height: 30rpx;
  border-radius: 6rpx;
}

.legend-name {
  flex: 1;
  font-size: 26rpx;
  color: #333;
}

.legend-value {
  font-size: 26rpx;
  color: #666;
}

.detail-list {
  max-height: 500rpx;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25rpx 0;
  border-bottom: 1rpx solid #eee;
}

.detail-date {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.date-text {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.weekday-text {
  font-size: 24rpx;
  color: #999;
}

.detail-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10rpx;
}

.detail-calories {
  font-size: 28rpx;
  font-weight: bold;
  color: #FF6B35;
}

.detail-progress {
  width: 150rpx;
}

.progress-bar {
  height: 8rpx;
  background: #F5F5F5;
  border-radius: 4rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 4rpx;
  transition: width 0.3s;
}

.suggestion-card {
  background: linear-gradient(135deg, #FFF7E6, #FFE7BA);
  margin: 0 30rpx;
  padding: 30rpx;
  border-radius: 16rpx;
  border: 1rpx solid #FFD666;
}

.suggestion-header {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 15rpx;
}

.suggestion-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #FF6B35;
}

.suggestion-content {
  line-height: 1.8;
}

.suggestion-text {
  font-size: 26rpx;
  color: #666;
}
</style>
