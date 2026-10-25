<template>
  <view class="calorie-container">
    <view class="date-header">
      <view class="date-arrow" @click="changeDate(-1)">
        <uni-icons type="left" size="20" color="#666666"></uni-icons>
      </view>
      <text class="date-text">{{ selectedDateText }}</text>
      <view class="date-arrow" @click="changeDate(1)">
        <uni-icons type="right" size="20" color="#666666"></uni-icons>
      </view>
    </view>

    <view class="overview-card">
      <view class="overview-title">今日摄入概览</view>
      <view class="calorie-circle">
        <view class="circle-progress">
          <text class="progress-value">{{ todayCalorie }}</text>
          <text class="progress-unit">kcal</text>
          <text class="progress-label">已摄入</text>
        </view>
      </view>
      <view class="progress-bar">
        <view class="bar-fill" :style="{ width: caloriePercent + '%' }"></view>
      </view>
      <text class="goal-text">目标：{{ calorieGoal }}kcal</text>
    </view>

    <view class="nutrition-cards">
      <view
        class="nutrition-item"
        v-for="item in nutritionList"
        :key="item.name"
      >
        <view class="nutrition-icon-badge" :style="{ backgroundColor: item.bgColor, color: item.color }">
          <text class="nutrition-icon-text">{{ item.shortLabel }}</text>
        </view>
        <view class="nutrition-info">
          <text class="nutrition-name">{{ item.name }}</text>
          <text class="nutrition-value">{{ item.current }}/{{ item.target }}{{ item.unit }}</text>
          <view class="nutrition-bar">
            <view class="bar-fill" :style="{ width: item.percent + '%', background: item.color }"></view>
          </view>
        </view>
      </view>
    </view>

    <view class="quick-actions">
      <button class="action-btn primary" @click="recordDiet">
        <uni-icons type="compose" size="18" color="#FFFFFF"></uni-icons>
        <text>记录饮食</text>
      </button>
      <button class="action-btn outline" @click="viewStatistics">
        <uni-icons type="chartbar" size="18" color="#FF6B35"></uni-icons>
        <text>统计分析</text>
      </button>
    </view>

    <view class="records-section">
      <view class="section-header">
        <text class="section-title">今日饮食记录</text>
        <view class="section-more" @click="viewAllRecords">
          <text>查看全部</text>
          <uni-icons type="right" size="14" color="#999999"></uni-icons>
        </view>
      </view>

      <view class="record-list">
        <view class="empty-records" v-if="records.length === 0">
          <view class="empty-icon-wrapper">
            <uni-icons type="compose" size="34" color="#FF6B35"></uni-icons>
          </view>
          <text class="empty-text">今天还没有记录</text>
          <text class="empty-tips">记录每一餐，系统会按日期汇总您的摄入情况</text>
        </view>

        <view
          class="record-item"
          v-for="record in records"
          :key="record.id"
          @click="viewRecordDetail(record)"
        >
          <view class="meal-icon" :class="record.mealType">
            <text class="meal-icon-text">{{ record.mealLabel }}</text>
          </view>

          <view class="record-content">
            <text class="record-name">{{ record.name }}</text>
            <view class="record-meta">
              <text class="record-calorie">{{ record.calorie }}kcal</text>
              <text class="record-time">{{ record.time }}</text>
            </view>
            <view class="record-foods" v-if="record.foods">
              <text
                class="food-item"
                v-for="(food, index) in record.foods.slice(0, 3)"
                :key="index"
              >
                {{ food }}
                <text v-if="index < Math.min(record.foods.length, 3) - 1">、</text>
              </text>
              <text v-if="record.foods.length > 3">等</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="bottom-bar">
      <button class="add-btn" @click="recordDiet">
        <uni-icons type="plus-filled" size="18" color="#FFFFFF"></uni-icons>
        <text>记录饮食</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store'
import { aiApi } from '@/api'

// 用户信息store
const userStore = useUserStore()

// 选中的日期
const selectedDate = ref(new Date())

// 今日卡路里
const todayCalorie = ref(0)
const calorieGoal = ref(2000)

const createNutritionItem = (name, shortLabel, current, target, unit, color, percent) => {
  const safeCurrent = Number(current || 0)
  const safeTarget = Number(target || 0)
  const safePercent = typeof percent === 'number'
    ? percent
    : (safeTarget > 0 ? Math.round((safeCurrent / safeTarget) * 100) : 0)

  return {
    name,
    shortLabel,
    current: safeCurrent,
    target: safeTarget,
    unit,
    percent: Math.min(Math.max(safePercent, 0), 100),
    color,
    bgColor: `${color}1A`
  }
}

const createNutritionList = (nutrition = {}) => ([
  createNutritionItem('蛋白质', '蛋', nutrition.protein, nutrition.proteinTarget || 80, 'g', '#FF6B35', nutrition.proteinPercent),
  createNutritionItem('碳水化合物', '碳', nutrition.carbs, nutrition.carbsTarget || 300, 'g', '#FFB74D', nutrition.carbsPercent),
  createNutritionItem('脂肪', '脂', nutrition.fat, nutrition.fatTarget || 60, 'g', '#81C784', nutrition.fatPercent),
  createNutritionItem('膳食纤维', '纤', nutrition.fiber, nutrition.fiberTarget || 25, 'g', '#64B5F6', nutrition.fiberPercent)
])

// 卡路里百分比
const caloriePercent = computed(() => {
  return Math.min((todayCalorie.value / calorieGoal.value) * 100, 100)
})

// 营养成分列表
const nutritionList = ref(createNutritionList())

// 饮食记录
const records = ref([])

// 选中的日期文本
const selectedDateText = computed(() => {
  const today = new Date()
  const date = selectedDate.value

  if (isSameDay(date, today)) {
    return '今天'
  } else if (isYesterday(date, today)) {
    return '昨天'
  } else {
    return formatDate(date)
  }
})

/**
 * 判断是否同一天
 */
function isSameDay(date1, date2) {
  return date1.getFullYear() === date2.getFullYear() &&
    date1.getMonth() === date2.getMonth() &&
    date1.getDate() === date2.getDate()
}

/**
 * 判断是否是昨天
 */
function isYesterday(date1, date2) {
  const yesterday = new Date(date2)
  yesterday.setDate(yesterday.getDate() - 1)
  return isSameDay(date1, yesterday)
}

/**
 * 格式化日期
 */
function formatDate(date) {
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${month}-${day} ${weekdays[date.getDay()]}`
}

/**
 * 切换日期
 */
const changeDate = (days) => {
  const newDate = new Date(selectedDate.value)
  newDate.setDate(newDate.getDate() + days)
  selectedDate.value = newDate
  loadData()
}

/**
 * 加载数据
 */
const loadData = async () => {
  if (!userStore.isLogin) {
    todayCalorie.value = 0
    calorieGoal.value = 2000
    nutritionList.value = createNutritionList()
    records.value = []
    return
  }

  try {
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id
    const dateStr = formatApiDate(selectedDate.value)

    const res = await aiApi.analyzeNutrition({
      userId,
      date: dateStr
    })

    if (res && res.data) {
      todayCalorie.value = res.data.calories || 0
      calorieGoal.value = res.data.targetCalories || res.data.calorieGoal || 2000

      if (res.data.nutrition) {
        nutritionList.value = createNutritionList(res.data.nutrition)
      } else {
        nutritionList.value = createNutritionList()
      }

      if (res.data.records && res.data.records.length > 0) {
        records.value = res.data.records.map(record => ({
          id: record.id,
          name: record.name,
          mealType: record.mealType,
          mealLabel: getMealLabel(record.mealType),
          calorie: record.calories,
          time: formatRecordTime(record.time),
          foods: record.foods || []
        }))
      } else {
        records.value = []
      }
    } else {
      todayCalorie.value = 0
      calorieGoal.value = 2000
      nutritionList.value = createNutritionList()
      records.value = []
    }
  } catch (error) {
    console.error('加载营养数据失败:', error)
    todayCalorie.value = 0
    calorieGoal.value = 2000
    nutritionList.value = createNutritionList()
    records.value = []
  }
}

/**
 * 格式化API日期格式
 */
const formatApiDate = (date) => {
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * 获取餐型标签
 */
const getMealLabel = (mealType) => {
  const labels = {
    breakfast: '早',
    lunch: '午',
    dinner: '晚',
    snack: '加'
  }
  return labels[mealType] || '餐'
}

/**
 * 格式化记录时间
 */
const formatRecordTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

/**
 * 记录饮食
 */
const recordDiet = () => {
  uni.navigateTo({
    url: '/pages-user/calorie/record'
  })
}

/**
 * 查看统计
 */
const viewStatistics = () => {
  uni.navigateTo({
    url: '/pages-user/calorie/statistics'
  })
}

/**
 * 查看全部记录
 */
const viewAllRecords = () => {
  uni.navigateTo({
    url: '/pages-user/calorie/record'
  })
}

/**
 * 查看记录详情
 */
const viewRecordDetail = (record) => {
  uni.navigateTo({
    url: '/pages-user/calorie/record'
  })
}

// 组件挂载
onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.calorie-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  padding-bottom: 120rpx;
}

/* 日期选择 */
.date-header {
  @include flex-center;
  background-color: $bg-color-white;
  padding: $spacing-lg;
  box-shadow: $box-shadow-sm;
}

.date-arrow {
  width: 60rpx;
  height: 60rpx;
  @include flex-center;
  margin: 0 $spacing-xl;

  &:active {
    opacity: 0.6;
  }
}

.date-text {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

/* 概览卡片 */
.overview-card {
  background: linear-gradient(135deg, #FF6B35, #FF8F61);
  margin: $spacing-md;
  padding: $spacing-xl;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-md;
}

.overview-title {
  font-size: $font-size-base;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: $spacing-lg;
  text-align: center;
}

.calorie-circle {
  @include flex-center-column;
  align-items: center;
  margin-bottom: $spacing-lg;
}

.circle-progress {
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  border: 8rpx solid rgba(255, 255, 255, 0.2);
  @include flex-center-column;
  align-items: center;
  justify-content: center;
}

.progress-value {
  font-size: 56rpx;
  font-weight: $font-weight-bold;
  color: #fff;
  line-height: 1;
}

.progress-unit {
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.8);
}

.progress-label {
  font-size: $font-size-xs;
  color: rgba(255, 255, 255, 0.7);
  margin-top: $spacing-xs;
}

.progress-bar {
  width: 100%;
  height: 8rpx;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: $border-radius-round;
  overflow: hidden;
  margin-bottom: $spacing-sm;
}

.bar-fill {
  height: 100%;
  background-color: #fff;
  border-radius: $border-radius-round;
  transition: width 0.3s;
}

.goal-text {
  text-align: center;
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.8);
}

/* 营养成分卡片 */
.nutrition-cards {
  @include flex-center;
  flex-wrap: wrap;
  gap: $spacing-sm;
  padding: 0 $spacing-md;
  margin-bottom: $spacing-md;
}

.nutrition-item {
  flex: 1;
  min-width: calc(50% - #{$spacing-sm});
  background-color: $bg-color-white;
  padding: $spacing-md;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-sm;
  @include flex-center;
  gap: $spacing-sm;
}

.nutrition-icon-badge {
  width: 64rpx;
  height: 64rpx;
  border-radius: 20rpx;
  @include flex-center;
  flex-shrink: 0;
}

.nutrition-icon-text {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
}

.nutrition-info {
  flex: 1;
  @include flex-center-column;
  gap: $spacing-xs;
}

.nutrition-name {
  font-size: $font-size-sm;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
}

.nutrition-value {
  font-size: $font-size-xs;
  color: $text-color-secondary;
}

.nutrition-bar {
  width: 100%;
  height: 6rpx;
  background-color: $bg-color-base;
  border-radius: $border-radius-round;
  overflow: hidden;
}

/* 快速操作 */
.quick-actions {
  @include flex-center;
  gap: $spacing-md;
  padding: 0 $spacing-md;
  margin-bottom: $spacing-md;
}

.action-btn {
  flex: 1;
  height: 88rpx;
  @include flex-center;
  gap: $spacing-sm;
  border-radius: $border-radius-lg;
  font-size: $font-size-base;
  border: none;

  &.primary {
    background: linear-gradient(135deg, $primary-color, #FF8F61);
    color: #fff;
  }

  &.outline {
    background-color: $bg-color-white;
    color: $primary-color;
    border: 1rpx solid $primary-color;
  }

  &:active {
    transform: scale(0.98);
  }
}

/* 记录部分 */
.records-section {
  background-color: $bg-color-white;
  margin: 0 $spacing-md;
  padding: $spacing-lg $spacing-md;
  border-radius: $border-radius-lg;
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
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: $font-size-sm;
  color: $text-color-secondary;

  &:active {
    opacity: 0.6;
  }
}

.record-list {
  @include flex-center-column;
  gap: $spacing-md;
}

.empty-records {
  @include flex-center-column;
  padding: 80rpx 0;
  text-align: center;
}

.empty-icon-wrapper {
  width: 96rpx;
  height: 96rpx;
  border-radius: 28rpx;
  background-color: #FFF3ED;
  @include flex-center;
  margin-bottom: $spacing-lg;
}

.empty-text {
  font-size: $font-size-base;
  color: $text-color-primary;
  margin-bottom: $spacing-xs;
}

.empty-tips {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.record-item {
  @include flex-center;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-lg;

  &:active {
    background-color: rgba(0, 0, 0, 0.02);
  }
}

.meal-icon {
  width: 88rpx;
  height: 88rpx;
  @include flex-center;
  border-radius: $border-radius-lg;
  flex-shrink: 0;

  &.breakfast {
    background: linear-gradient(135deg, #FFB74D, #FF9800);
  }

  &.lunch {
    background: linear-gradient(135deg, #FF7043, #F4511E);
  }

  &.dinner {
    background: linear-gradient(135deg, #81C784, #66BB6A);
  }

  &.snack {
    background: linear-gradient(135deg, #64B5F6, #42A5F5);
  }
}

.meal-icon-text {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  color: #FFFFFF;
}

.record-content {
  flex: 1;
  margin-left: $spacing-md;
  @include flex-center-column;
  align-items: flex-start;
  gap: $spacing-xs;
  min-width: 0;
}

.record-name {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-color-primary;
}

.record-meta {
  @include flex-center;
  gap: $spacing-md;
  width: 100%;
}

.record-calorie {
  font-size: $font-size-lg;
  color: $primary-color;
  font-weight: $font-weight-bold;
}

.record-time {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.record-foods {
  font-size: $font-size-sm;
  color: $text-color-regular;
  line-height: $line-height-lg;
}

.food-item {
  color: $text-color-regular;
}

/* 底部按钮 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: $bg-color-white;
  padding: $spacing-md;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
  z-index: $z-index-fixed;
  @include safe-area-bottom;
}

.add-btn {
  width: 100%;
  height: 88rpx;
  @include flex-center;
  gap: $spacing-sm;
  background: linear-gradient(135deg, $primary-color, #FF8F61);
  color: #fff;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  border-radius: $border-radius-round;
  border: none;
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.3);

  &:active {
    transform: scale(0.98);
  }
}
</style>
