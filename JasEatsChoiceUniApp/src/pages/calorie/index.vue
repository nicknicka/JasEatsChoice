<template>
  <view class="calorie-container">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <view class="nav-title">卡路里统计</view>
      <view class="nav-action" @click="toggleAdvice">
        <text class="action-text">建议</text>
      </view>
    </view>

    <scroll-view class="calorie-scroll" scroll-y>
      <!-- 今日摄入卡片 -->
      <view class="today-card">
        <view class="card-header">
          <text class="card-title">今日摄入</text>
          <view class="date-selector">
            <text class="date-text">{{ currentDate }}</text>
          </view>
        </view>

        <view class="calorie-circle-wrapper">
          <view class="calorie-circle">
            <view class="circle-progress" :style="{ background: getCircleGradient() }">
              <view class="circle-inner">
                <text class="calorie-value">{{ calorieData.today.consumed }}</text>
                <text class="calorie-unit">kcal</text>
              </view>
            </view>
          </view>

          <view class="calorie-info">
            <view class="info-item">
              <text class="info-label">目标</text>
              <text class="info-value">{{ calorieData.today.target }} kcal</text>
            </view>
            <view class="info-item">
              <text class="info-label">剩余</text>
              <text class="info-value" :class="{ 'value-warning': calorieData.today.remaining < 0 }">
                {{ calorieData.today.remaining }} kcal
              </text>
            </view>
          </view>
        </view>

        <view class="progress-bar-wrapper">
          <view class="progress-label">今日进度</view>
          <view class="progress-bar">
            <view
              class="progress-fill"
              :style="{ width: getProgressPercent() + '%' }"
            ></view>
          </view>
          <text class="progress-text">{{ getProgressPercent() }}%</text>
        </view>
      </view>

      <!-- 营养成分分析 -->
      <view class="nutrition-section">
        <view class="section-header">
          <text class="section-title">营养成分分析</text>
          <text class="section-desc">推荐摄入量对比</text>
        </view>

        <view class="nutrition-list">
          <!-- 蛋白质 -->
          <view class="nutrition-item">
            <view class="nutrition-header">
              <text class="nutrition-icon">🥩</text>
              <text class="nutrition-name">蛋白质</text>
              <text class="nutrition-amount">{{ calorieData.nutrition[0].value }}g</text>
            </view>
            <view class="nutrition-bar-wrapper">
              <view class="nutrition-bar">
                <view
                  class="nutrition-fill protein-fill"
                  :style="{ width: getNutritionPercent(0) + '%' }"
                ></view>
              </view>
              <text class="nutrition-percent">{{ getNutritionPercent(0) }}%</text>
            </view>
            <view class="nutrition-target">
              <text class="target-text">目标: {{ recommendedGoals.protein }}g</text>
            </view>
          </view>

          <!-- 碳水化合物 -->
          <view class="nutrition-item">
            <view class="nutrition-header">
              <text class="nutrition-icon">🍚</text>
              <text class="nutrition-name">碳水化合物</text>
              <text class="nutrition-amount">{{ calorieData.nutrition[1].value }}g</text>
            </view>
            <view class="nutrition-bar-wrapper">
              <view class="nutrition-bar">
                <view
                  class="nutrition-fill carb-fill"
                  :style="{ width: getNutritionPercent(1) + '%' }"
                ></view>
              </view>
              <text class="nutrition-percent">{{ getNutritionPercent(1) }}%</text>
            </view>
            <view class="nutrition-target">
              <text class="target-text">目标: {{ recommendedGoals.carbohydrate }}g</text>
            </view>
          </view>

          <!-- 脂肪 -->
          <view class="nutrition-item">
            <view class="nutrition-header">
              <text class="nutrition-icon">🥑</text>
              <text class="nutrition-name">脂肪</text>
              <text class="nutrition-amount">{{ calorieData.nutrition[2].value }}g</text>
            </view>
            <view class="nutrition-bar-wrapper">
              <view class="nutrition-bar">
                <view
                  class="nutrition-fill fat-fill"
                  :style="{ width: getNutritionPercent(2) + '%' }"
                ></view>
              </view>
              <text class="nutrition-percent">{{ getNutritionPercent(2) }}%</text>
            </view>
            <view class="nutrition-target">
              <text class="target-text">目标: {{ recommendedGoals.fat }}g</text>
            </view>
          </view>
        </view>

        <button class="add-record-btn" @click="addDietRecord">
          <text class="btn-icon">➕</text>
          <text class="btn-text">添加饮食记录</text>
        </button>
      </view>

      <!-- 周数据统计 -->
      <view class="weekly-section">
        <view class="section-header">
          <text class="section-title">本周数据</text>
          <text class="section-desc">最近7天摄入趋势</text>
        </view>

        <view class="weekly-chart">
          <view class="chart-bars">
            <view
              class="chart-bar-item"
              v-for="(day, index) in calorieData.weekly"
              :key="index"
            >
              <view class="bar-wrapper">
                <view
                  class="bar-fill"
                  :style="{ height: getBarHeight(day.consumed) + '%' }"
                ></view>
              </view>
              <text class="bar-label">{{ day.day }}</text>
              <text class="bar-value">{{ day.consumed }}</text>
            </view>
          </view>
        </view>

        <view class="weekly-stats">
          <view class="weekly-stat-item">
            <text class="stat-label">日均摄入</text>
            <text class="stat-value">{{ getWeeklyAverage() }} kcal</text>
          </view>
          <view class="weekly-stat-item">
            <text class="stat-label">最高一天</text>
            <text class="stat-value">{{ getWeeklyMax() }} kcal</text>
          </view>
          <view class="weekly-stat-item">
            <text class="stat-label">达标天数</text>
            <text class="stat-value">{{ get达标Days() }} 天</text>
          </view>
        </view>
      </view>

      <!-- 健康建议 -->
      <view class="advice-section" v-if="showAdvice">
        <view class="section-header">
          <text class="section-title">健康建议</text>
          <text class="section-close" @click="toggleAdvice">✕</text>
        </view>

        <view class="advice-list">
          <view
            class="advice-item"
            v-for="(advice, index) in healthAdvices"
            :key="index"
            :class="'advice-' + advice.type"
          >
            <text class="advice-icon">{{ advice.icon }}</text>
            <view class="advice-content">
              <text class="advice-title">{{ advice.title }}</text>
              <text class="advice-desc">{{ advice.desc }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 底部提示 -->
      <view class="bottom-tip">
        <text class="tip-text">数据基于您添加的饮食记录计算</text>
      </view>

      <!-- 底部安全区 -->
      <view class="bottom-safe-area"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { dietApi } from '@/api'

// 卡路里数据
const calorieData = ref({
  today: {
    consumed: 1450,
    remaining: 550,
    target: 2000
  },
  weekly: [
    { day: '一', consumed: 1800 },
    { day: '二', consumed: 2100 },
    { day: '三', consumed: 1650 },
    { day: '四', consumed: 1950 },
    { day: '五', consumed: 2200 },
    { day: '六', consumed: 1750 },
    { day: '日', consumed: 1450 }
  ],
  nutrition: [
    { name: '蛋白质', value: 65 },
    { name: '碳水化合物', value: 180 },
    { name: '脂肪', value: 55 }
  ]
})

// 推荐营养目标
const recommendedGoals = ref({
  蛋白质: 90,
  碳水化合物: 250,
  脂肪: 70
})

// 显示建议
const showAdvice = ref(false)

// 当前日期
const currentDate = computed(() => {
  const now = new Date()
  return `${now.getMonth() + 1}月${now.getDate()}日`
})

// 健康建议
const healthAdvices = computed(() => {
  const advices = []
  const { consumed, target } = calorieData.value.today

  // 基于卡路里摄入的建议
  if (consumed > target) {
    advices.push({
      type: 'warning',
      icon: '⚠️',
      title: '超出目标',
      desc: `今日已超出目标 ${consumed - target} kcal，建议适当控制饮食`
    })
  } else if (consumed < target * 0.5) {
    advices.push({
      type: 'info',
      icon: '💡',
      title: '摄入不足',
      desc: '今日摄入量偏低，建议适当增加营养摄入'
    })
  }

  // 基于营养成分的建议
  const proteinRatio = (calorieData.value.nutrition[0].value / recommendedGoals.value.蛋白质) * 100
  if (proteinRatio < 60) {
    advices.push({
      type: 'warning',
      icon: '🥩',
      title: '蛋白质不足',
      desc: '蛋白质摄入偏低，建议增加肉类、蛋类或豆制品摄入'
    })
  }

  const carbRatio = (calorieData.value.nutrition[1].value / recommendedGoals.value.碳水化合物) * 100
  if (carbRatio > 120) {
    advices.push({
      type: 'warning',
      icon: '🍚',
      title: '碳水偏高',
      desc: '碳水化合物摄入较多，建议适当减少主食摄入'
    })
  }

  // 默认建议
  if (advices.length === 0) {
    advices.push({
      type: 'success',
      icon: '✅',
      title: '饮食均衡',
      desc: '今日饮食结构合理，继续保持！'
    })
  }

  return advices
})

// 组件挂载
onMounted(() => {
  loadCalorieData()
})

/**
 * 加载卡路里数据
 */
const loadCalorieData = async () => {
  try {
    // TODO: 调用真实API
    // const res = await dietApi.getTodayData()

    // 计算剩余
    calorieData.value.today.remaining = calorieData.value.today.target - calorieData.value.today.consumed

  } catch (error) {
    console.error('加载卡路里数据失败:', error)
  }
}

/**
 * 获取圆环渐变
 */
const getCircleGradient = () => {
  const percent = getProgressPercent()
  if (percent >= 100) {
    return 'conic-gradient(#4CAF50 0%, #4CAF50 100%)'
  } else if (percent >= 80) {
    return 'conic-gradient(#FF6B35 0%, #FF6B35 ' + percent + '%%, #E0E0E0 ' + percent + '%%, #E0E0E0 100%)'
  } else if (percent >= 50) {
    return 'conic-gradient(#FFC107 0%, #FFC107 ' + percent + '%%, #E0E0E0 ' + percent + '%%, #E0E0E0 100%)'
  } else {
    return 'conic-gradient(#FF6B35 0%, #FF6B35 ' + percent + '%%, #E0E0E0 ' + percent + '%%, #E0E0E0 100%)'
  }
}

/**
 * 获取进度百分比
 */
const getProgressPercent = () => {
  const { consumed, target } = calorieData.value.today
  const percent = Math.round((consumed / target) * 100)
  return Math.min(percent, 100)
}

/**
 * 获取营养百分比
 */
const getNutritionPercent = (index) => {
  const nutrition = calorieData.value.nutrition[index]
  const goalMap = [recommendedGoals.value.蛋白质, recommendedGoals.value.碳水化合物, recommendedGoals.value.脂肪]
  const goal = goalMap[index]

  if (!goal) return 0
  return Math.round((nutrition.value / goal) * 100)
}

/**
 * 获取柱状图高度
 */
const getBarHeight = (value) => {
  const maxValue = Math.max(...calorieData.value.weekly.map(d => d.consumed))
  if (maxValue === 0) return 0
  return Math.round((value / maxValue) * 100)
}

/**
 * 获取周平均
 */
const getWeeklyAverage = () => {
  const total = calorieData.value.weekly.reduce((sum, day) => sum + day.consumed, 0)
  return Math.round(total / 7)
}

/**
 * 获取周最高
 */
const getWeeklyMax = () => {
  return Math.max(...calorieData.value.weekly.map(d => d.consumed))
}

/**
 * 获取达标天数
 */
const get达标Days = () => {
  return calorieData.value.weekly.filter(d => d.consumed >= 1800 && d.consumed <= 2200).length
}

/**
 * 切换建议显示
 */
const toggleAdvice = () => {
  showAdvice.value = !showAdvice.value
}

/**
 * 添加饮食记录
 */
const addDietRecord = () => {
  uni.navigateTo({
    url: '/pages/diet-record/add'
  })
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

.calorie-container {
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

/* 滚动内容 */
.calorie-scroll {
  flex: 1;
  margin-top: 108rpx;
  padding: $spacing-md;
  padding-bottom: env(safe-area-inset-bottom);
}

/* 今日摄入卡片 */
.today-card {
  background: linear-gradient(135deg, #FF6B35, #FF8C61);
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  margin-bottom: $spacing-md;
  box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.3);
}

.card-header {
  @include flex-between;
  align-items: center;
  margin-bottom: $spacing-lg;
}

.card-title {
  font-size: $font-size-lg;
  color: #fff;
  font-weight: $font-weight-bold;
}

.date-selector {
  padding: 4rpx 12rpx;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: $border-radius-round;
}

.date-text {
  font-size: $font-size-sm;
  color: #fff;
}

.calorie-circle-wrapper {
  @include flex-center;
  margin-bottom: $spacing-lg;
}

.calorie-circle {
  width: 240rpx;
  height: 240rpx;
  border-radius: 50%;
  position: relative;
  background-color: rgba(255, 255, 255, 0.1);
}

.circle-progress {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.circle-inner {
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  background-color: #fff;
  @include flex-center-column;
}

.calorie-value {
  font-size: 64rpx;
  color: $primary-color;
  font-weight: $font-weight-bold;
  line-height: 1;
}

.calorie-unit {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.calorie-info {
  @include flex-center;
  gap: $spacing-xl;
}

.info-item {
  @include flex-center-column;
  gap: 4rpx;
}

.info-label {
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.8);
}

.info-value {
  font-size: $font-size-xl;
  color: #fff;
  font-weight: $font-weight-bold;

  &.value-warning {
    color: #FFC107;
  }
}

.progress-bar-wrapper {
  @include flex-center;
  gap: $spacing-sm;
}

.progress-label {
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.8);
}

.progress-bar {
  flex: 1;
  height: 16rpx;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 8rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background-color: #fff;
  border-radius: 8rpx;
  transition: width 0.3s;
}

.progress-text {
  font-size: $font-size-base;
  color: #fff;
  font-weight: $font-weight-bold;
  min-width: 60rpx;
  text-align: right;
}

/* 营养成分 */
.nutrition-section {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  margin-bottom: $spacing-md;
  box-shadow: $box-shadow-sm;
}

.section-header {
  @include flex-between;
  align-items: center;
  margin-bottom: $spacing-lg;
}

.section-title {
  font-size: $font-size-lg;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
}

.section-desc {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.section-close {
  font-size: 32rpx;
  color: $text-color-secondary;
  padding: 0 $spacing-xs;
}

.nutrition-list {
  .nutrition-item {
    margin-bottom: $spacing-lg;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

.nutrition-header {
  @include flex-center;
  margin-bottom: $spacing-sm;
}

.nutrition-icon {
  font-size: 36rpx;
  margin-right: $spacing-sm;
}

.nutrition-name {
  flex: 1;
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
}

.nutrition-amount {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
}

.nutrition-bar-wrapper {
  @include flex-center;
  gap: $spacing-sm;
  margin-bottom: 4rpx;
}

.nutrition-bar {
  flex: 1;
  height: 16rpx;
  background-color: $bg-color-base;
  border-radius: 8rpx;
  overflow: hidden;
}

.nutrition-fill {
  height: 100%;
  border-radius: 8rpx;
  transition: width 0.3s;

  &.protein-fill {
    background-color: #FF6B35;
  }

  &.carb-fill {
    background-color: #FFC107;
  }

  &.fat-fill {
    background-color: #4CAF50;
  }
}

.nutrition-percent {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  min-width: 50rpx;
  text-align: right;
}

.nutrition-target {
  padding-left: 48rpx;
}

.target-text {
  font-size: $font-size-xs;
  color: $text-color-secondary;
}

.add-record-btn {
  width: 100%;
  margin-top: $spacing-lg;
  padding: $spacing-md;
  background-color: $primary-color;
  color: #fff;
  border-radius: $border-radius-lg;
  @include flex-center;
  gap: $spacing-sm;
  border: none;

  &::after {
    border: none;
  }
}

.btn-icon {
  font-size: 32rpx;
}

.btn-text {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
}

/* 周数据 */
.weekly-section {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  margin-bottom: $spacing-md;
  box-shadow: $box-shadow-sm;
}

.weekly-chart {
  margin-bottom: $spacing-lg;
}

.chart-bars {
  display: flex;
  justify-content: space-between;
  height: 240rpx;
  padding: 0 $spacing-sm;
}

.chart-bar-item {
  flex: 1;
  @include flex-center-column;
  gap: 8rpx;
}

.bar-wrapper {
  flex: 1;
  @include flex-center;
  align-items: flex-end;
  padding: 0 4rpx;
}

.bar-fill {
  width: 32rpx;
  background: linear-gradient(to top, #FF6B35, #FF8C61);
  border-radius: 16rpx 16rpx 0 0;
  transition: height 0.3s;
}

.bar-label {
  font-size: $font-size-xs;
  color: $text-color-secondary;
}

.bar-value {
  font-size: $font-size-xs;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
}

.weekly-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-md;
}

.weekly-stat-item {
  @include flex-center-column;
  gap: 4rpx;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
}

.stat-label {
  font-size: $font-size-xs;
  color: $text-color-secondary;
}

.stat-value {
  font-size: $font-size-base;
  color: $primary-color;
  font-weight: $font-weight-bold;
}

/* 健康建议 */
.advice-section {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  margin-bottom: $spacing-md;
  box-shadow: $box-shadow-sm;
}

.advice-list {
  .advice-item {
    display: flex;
    align-items: flex-start;
    gap: $spacing-md;
    padding: $spacing-md;
    border-radius: $border-radius-base;
    margin-bottom: $spacing-sm;

    &.advice-warning {
      background-color: rgba($warning-color, 0.1);
    }

    &.advice-info {
      background-color: rgba($primary-color, 0.1);
    }

    &.advice-success {
      background-color: rgba($success-color, 0.1);
    }

    &:last-child {
      margin-bottom: 0;
    }
  }
}

.advice-icon {
  font-size: 36rpx;
}

.advice-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.advice-title {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
}

.advice-desc {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  line-height: 1.4;
}

/* 底部提示 */
.bottom-tip {
  padding: $spacing-lg;
  text-align: center;
}

.tip-text {
  font-size: $font-size-xs;
  color: $text-color-secondary;
}

/* 底部安全区 */
.bottom-safe-area {
  height: 40rpx;
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
