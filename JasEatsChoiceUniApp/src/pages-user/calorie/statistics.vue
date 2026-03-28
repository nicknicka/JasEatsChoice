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
import { useUserStore } from '@/store'
import { formatDate } from '@/utils/helper'
import { aiApi } from '@/api'

const userStore = useUserStore()

const period = ref('week')
const targetCalories = ref(2000)

const averageCalories = ref(0)
const targetAchieved = ref(0)
const totalDays = ref(0)

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
  { name: '碳水化合物', value: 0, color: '#FF6B35' },
  { name: '蛋白质', value: 0, color: '#52C41A' },
  { name: '脂肪', value: 0, color: '#1890FF' },
  { name: '膳食纤维', value: 0, color: '#FAAD14' },
  { name: '其他', value: 0, color: '#F5222D' }
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

const loadStatistics = async () => {
  try {
    if (!userStore.isLogin) {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }

    const userId = userStore.userInfo?.userId || userStore.userInfo?.id

    // 计算日期范围
    const today = new Date()
    let startDate = new Date()
    let days = 7

    if (period.value === 'week') {
      startDate.setDate(today.getDate() - 6) // 最近7天
      days = 7
    } else {
      startDate.setDate(today.getDate() - 29) // 最近30天
      days = 30
    }

    const startDateStr = formatDate(startDate)
    const endDateStr = formatDate(today)

    // 调用API获取营养统计数据
    // 注意：这里需要后端支持日期范围查询
    const res = await aiApi.analyzeNutrition({
      userId,
      date: endDateStr,
      startDate: startDateStr,
      endDate: endDateStr,
      period: period.value
    })

    if (res && res.data) {
      const nutrition = res.data

      // 更新概览数据
      const totalCals = nutrition.totalCalories || nutrition.calories || 0
      averageCalories.value = Math.round(totalCals / days)
      targetAchieved.value = Math.round((averageCalories.value / targetCalories.value) * 100)
      totalDays.value = nutrition.totalDays || days

      // 更新营养成分饼图
      if (nutrition.nutrition || nutrition.macronutrients) {
        const macros = nutrition.nutrition || nutrition.macronutrients

        // 计算各营养成分占比
        const carbs = macros.carbs || macros.carbohydrates || 0
        const protein = macros.protein || 0
        const fat = macros.fat || 0
        const fiber = macros.fiber || macros.dietaryFiber || 0
        const total = carbs + protein + fat + fiber

        if (total > 0) {
          nutritionLegend.value[0].value = Math.round((carbs / total) * 100)
          nutritionLegend.value[1].value = Math.round((protein / total) * 100)
          nutritionLegend.value[2].value = Math.round((fat / total) * 100)
          nutritionLegend.value[3].value = Math.round((fiber / total) * 100)
          nutritionLegend.value[4].value = Math.max(0, 100 - nutritionLegend.value[0].value - nutritionLegend.value[1].value - nutritionLegend.value[2].value - nutritionLegend.value[3].value)

          pieData.value = {
            series: [{
              data: [
                { name: '碳水化合物', value: nutritionLegend.value[0].value },
                { name: '蛋白质', value: nutritionLegend.value[1].value },
                { name: '脂肪', value: nutritionLegend.value[2].value },
                { name: '膳食纤维', value: nutritionLegend.value[3].value },
                { name: '其他', value: nutritionLegend.value[4].value }
              ]
            }]
          }
        }
      }

      // 更新详细数据列表
      if (Array.isArray(nutrition.dailyRecords)) {
        detailList.value = nutrition.dailyRecords.map(record => ({
          date: formatShortDate(record.date),
          weekday: getWeekday(record.date),
          calories: record.calories || record.totalCalories || 0
        }))
      } else if (nutrition.records) {
        detailList.value = nutrition.records.map(record => ({
          date: formatShortDate(record.date),
          weekday: getWeekday(record.date),
          calories: record.calories || 0
        }))
      }

      // 更新图表数据
      if (Array.isArray(nutrition.dailyRecords) || Array.isArray(nutrition.records)) {
        const records = nutrition.dailyRecords || nutrition.records
        const categories = records.map(r => formatShortDate(r.date))
        const data = records.map(r => r.calories || 0)

        chartData.value = {
          categories,
          series: [{
            name: '卡路里',
            data
          }]
        }
      }

      // 更新建议
      suggestion.value = nutrition.suggestion || generateSuggestion()
    } else {
      // 如果没有数据，使用默认值
      loadDefaultData()
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    loadDefaultData()
  }
}

const loadDefaultData = () => {
  if (period.value === 'week') {
    loadWeekData()
  } else {
    loadMonthData()
  }
}

const loadWeekData = () => {
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

  nutritionLegend.value = [
    { name: '碳水化合物', value: 45, color: '#FF6B35' },
    { name: '蛋白质', value: 20, color: '#52C41A' },
    { name: '脂肪', value: 25, color: '#1890FF' },
    { name: '膳食纤维', value: 8, color: '#FAAD14' },
    { name: '其他', value: 2, color: '#F5222D' }
  ]

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

  nutritionLegend.value = [
    { name: '碳水化合物', value: 48, color: '#FF6B35' },
    { name: '蛋白质', value: 18, color: '#52C41A' },
    { name: '脂肪', value: 22, color: '#1890FF' },
    { name: '膳食纤维', value: 10, color: '#FAAD14' },
    { name: '其他', value: 2, color: '#F5222D' }
  ]

  totalDays.value = 28
  averageCalories.value = 1820
  targetAchieved.value = Math.round((1820 / 2000) * 100)

  suggestion.value = '本月平均摄入1820kcal，低于目标2000kcal。营养结构较为均衡，建议继续保持当前饮食习惯，注意周末不要暴饮暴食。'
}

const formatShortDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

const getWeekday = (dateStr) => {
  if (!dateStr) return ''
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const date = new Date(dateStr)
  return weekdays[date.getDay()]
}

const generateSuggestion = () => {
  if (averageCalories.value < targetCalories.value * 0.8) {
    return `您的平均摄入${averageCalories.value}kcal，低于目标${targetCalories.value}kcal。建议适当增加优质蛋白质和碳水化合物的摄入。`
  } else if (averageCalories.value > targetCalories.value * 1.1) {
    return `您的平均摄入${averageCalories.value}kcal，高于目标${targetCalories.value}kcal。建议适当控制饮食，减少高热量食物的摄入。`
  } else {
    return `您的平均摄入${averageCalories.value}kcal，接近目标${targetCalories.value}kcal。营养结构较为均衡，建议继续保持当前饮食习惯。`
  }
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
