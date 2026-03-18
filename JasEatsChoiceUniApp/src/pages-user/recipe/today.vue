<template>
  <view class="today-recipe-container">
    <scroll-view class="scroll-container" scroll-y>
      <!-- 日期头部 -->
      <view class="date-header">
        <view class="date-info">
          <text class="date-text">{{ todayDate }}</text>
          <text class="weekday-text">{{ todayWeekday }}</text>
        </view>
        <view class="calorie-info">
          <text class="calorie-label">今日推荐</text>
          <text class="calorie-value">{{ totalCalorie }} kcal</text>
        </view>
      </view>

      <!-- 营养摄入卡片 -->
      <view class="nutrition-card">
        <view class="nutrition-title">营养摄入建议</view>
        <view class="nutrition-list">
          <view class="nutrition-item" v-for="item in nutritionList" :key="item.name">
            <view class="nutrition-header">
              <text class="nutrition-icon">{{ item.icon }}</text>
              <text class="nutrition-name">{{ item.name }}</text>
            </view>
            <view class="nutrition-bar">
              <view
                class="nutrition-bar-fill"
                :style="{ width: item.percent + '%', background: item.color }"
              ></view>
            </view>
            <view class="nutrition-footer">
              <text class="nutrition-current">{{ item.current }}</text>
              <text class="nutrition-separator">/</text>
              <text class="nutrition-target">{{ item.target }}</text>
              <text class="nutrition-unit">{{ item.unit }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 三餐食谱 -->
      <view class="meal-section" v-for="meal in meals" :key="meal.type">
        <view class="meal-header">
          <view class="meal-title-row">
            <text class="meal-icon">{{ meal.icon }}</text>
            <text class="meal-title">{{ meal.title }}</text>
          </view>
          <view class="meal-calorie">{{ meal.calorie }} kcal</view>
        </view>

        <view class="recipe-card">
          <image class="recipe-image" :src="meal.image" mode="aspectFill" @click="viewRecipeDetail(meal)" />
          <view class="recipe-content">
            <view class="recipe-title" @click="viewRecipeDetail(meal)">{{ meal.recipeName }}</view>
            <view class="recipe-tags">
              <text class="tag-item" v-for="tag in meal.tags" :key="tag">{{ tag }}</text>
            </view>
            <view class="recipe-meta">
              <view class="meta-item">
                <text class="meta-icon">⏱️</text>
                <text class="meta-text">{{ meal.time }}</text>
              </view>
              <view class="meta-item">
                <text class="meta-icon">🔥</text>
                <text class="meta-text">{{ meal.calorie }} kcal</text>
              </view>
            </view>
            <view class="recipe-ingredients">
              <text class="ingredients-label">食材：</text>
              <text class="ingredients-text">{{ meal.ingredients }}</text>
            </view>
          </view>
        </view>

        <view class="recipe-actions">
          <button class="action-btn outline" @click="replaceRecipe(meal)">
            <text class="btn-icon">🔄</text>
            <text>换一换</text>
          </button>
          <button class="action-btn primary" @click="orderRecipe(meal)">
            <text class="btn-icon">🛒</text>
            <text>一键订餐</text>
          </button>
        </view>
      </view>

      <!-- 底部提示 -->
      <view class="tips-section">
        <view class="tips-title">💡 饮食小贴士</view>
        <view class="tips-list">
          <view class="tip-item" v-for="(tip, index) in tips" :key="index">
            <text class="tip-text">{{ tip }}</text>
          </view>
        </view>
      </view>

      <!-- 底部空白 -->
      <view class="bottom-spacer"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// 日期信息
const todayDate = ref('')
const todayWeekday = ref('')

// 今日总卡路里
const totalCalorie = computed(() => {
  return meals.value.reduce((sum, meal) => sum + meal.calorie, 0)
})

// 营养摄入建议
const nutritionList = ref([
  {
    name: '蛋白质',
    icon: '🥩',
    current: 65,
    target: 80,
    unit: 'g',
    percent: 81,
    color: '#FF6B35'
  },
  {
    name: '碳水化合物',
    icon: '🍚',
    current: 250,
    target: 300,
    unit: 'g',
    percent: 83,
    color: '#FFB74D'
  },
  {
    name: '脂肪',
    icon: '🥑',
    current: 45,
    target: 60,
    unit: 'g',
    percent: 75,
    color: '#81C784'
  },
  {
    name: '膳食纤维',
    icon: '🥦',
    current: 18,
    target: 25,
    unit: 'g',
    percent: 72,
    color: '#64B5F6'
  }
])

// 三餐食谱
const meals = ref([
  {
    type: 'breakfast',
    icon: '🌅',
    title: '早餐',
    calorie: 450,
    recipeName: '营养早餐套餐',
    image: 'https://via.placeholder.com/400x300/FFE0B2/FF6B35?text=早餐',
    tags: ['高蛋白', '营养均衡'],
    time: '15分钟',
    ingredients: '牛奶、鸡蛋、全麦面包、水果沙拉'
  },
  {
    type: 'lunch',
    icon: '☀️',
    title: '午餐',
    calorie: 800,
    recipeName: '健康均衡午餐',
    image: 'https://via.placeholder.com/400x300/FFCCBC/FF6B35?text=午餐',
    tags: ['低脂', '高纤维'],
    time: '30分钟',
    ingredients: '糙米饭、清蒸鱼、炒时蔬、豆腐汤'
  },
  {
    type: 'dinner',
    icon: '🌙',
    title: '晚餐',
    calorie: 550,
    recipeName: '轻食晚餐',
    image: 'https://via.placeholder.com/400x300/C8E6C9/FF6B35?text=晚餐',
    tags: ['低卡', '易消化'],
    time: '20分钟',
    ingredients: '蔬菜沙拉、鸡胸肉、杂粮粥'
  }
])

// 饮食小贴士
const tips = ref([
  '早餐要吃好，为一天提供充足能量',
  '午餐要吃饱，保证下午工作效率',
  '晚餐要吃少，减轻肠胃负担',
  '每天饮水量建议2000ml以上',
  '定时定量进食，避免暴饮暴食'
])

/**
 * 查看食谱详情
 */
const viewRecipeDetail = (meal) => {
  uni.navigateTo({
    url: `/pages/recipe/detail/index?id=${meal.type}`
  })
}

/**
 * 换一换食谱
 */
const replaceRecipe = async (meal) => {
  try {
    uni.showLoading({
      title: '推荐中...'
    })

    // TODO: 调用后端API获取新推荐
    // const res = await recipeApi.recommend({
    //   mealType: meal.type,
    //   calorie: meal.calorie
    // })

    // 模拟推荐
    await new Promise(resolve => setTimeout(resolve, 1000))

    uni.hideLoading()

    // 更新食谱（这里模拟更新，实际应该替换为新食谱）
    meal.recipeName = `新推荐${meal.title}`
    meal.image = `https://via.placeholder.com/400x300/${getRandomColor()}/FF6B35?text=${meal.title}`

    uni.showToast({
      title: '已为您推荐新食谱',
      icon: 'success'
    })
  } catch (error) {
    console.error('推荐食谱失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: '推荐失败，请重试',
      icon: 'none'
    })
  }
}

/**
 * 一键订餐
 */
const orderRecipe = (meal) => {
  uni.showModal({
    title: '订餐确认',
    content: `确定要订「${meal.recipeName}」吗？`,
    confirmColor: '#FF6B35',
    success: (res) => {
      if (res.confirm) {
        // 添加到购物车
        uni.showToast({
          title: '已加入购物车',
          icon: 'success'
        })

        // 跳转到购物车页面
        setTimeout(() => {
          uni.switchTab({
            url: '/pages/cart/index'
          })
        }, 1500)
      }
    }
  })
}

/**
 * 获取随机颜色
 */
const getRandomColor = () => {
  const colors = ['FFE0B2', 'FFCCBC', 'C8E6C9', 'B2DFDB', 'B3E5FC']
  return colors[Math.floor(Math.random() * colors.length)]
}

// 组件挂载
onMounted(() => {
  const date = new Date()
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']

  todayDate.value = `${year}-${month}-${day}`
  todayWeekday.value = weekdays[date.getDay()]
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.today-recipe-container {
  min-height: 100vh;
  background-color: $bg-color-base;
}

.scroll-container {
  height: 100vh;
}

/* 日期头部 */
.date-header {
  background: linear-gradient(135deg, #FF6B35, #FF8F61);
  padding: $spacing-lg $spacing-md;
  @include flex-between;
  box-shadow: $box-shadow-md;
}

.date-info {
  @include flex-center-column;
  align-items: flex-start;
  gap: $spacing-xs;
}

.date-text {
  font-size: 48rpx;
  font-weight: $font-weight-bold;
  color: #fff;
}

.weekday-text {
  font-size: $font-size-base;
  color: rgba(255, 255, 255, 0.9);
}

.calorie-info {
  @include flex-center-column;
  align-items: flex-end;
  gap: $spacing-xs;
}

.calorie-label {
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.8);
}

.calorie-value {
  font-size: 36rpx;
  font-weight: $font-weight-bold;
  color: #fff;
}

/* 营养摄入卡片 */
.nutrition-card {
  background-color: $bg-color-white;
  margin: $spacing-md;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-sm;
}

.nutrition-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  margin-bottom: $spacing-lg;
  text-align: center;
}

.nutrition-list {
  @include flex-center-column;
  gap: $spacing-lg;
}

.nutrition-item {
  width: 100%;
}

.nutrition-header {
  @include flex-center;
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;
}

.nutrition-icon {
  font-size: $font-size-xl;
}

.nutrition-name {
  flex: 1;
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
}

.nutrition-bar {
  width: 100%;
  height: 16rpx;
  background-color: $bg-color-base;
  border-radius: $border-radius-round;
  overflow: hidden;
}

.nutrition-bar-fill {
  height: 100%;
  border-radius: $border-radius-round;
  transition: width 0.3s;
}

.nutrition-footer {
  @include flex-center;
  gap: $spacing-xs;
  margin-top: $spacing-xs;
}

.nutrition-current {
  font-size: $font-size-base;
  color: $primary-color;
  font-weight: $font-weight-bold;
}

.nutrition-separator {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.nutrition-target {
  font-size: $font-size-base;
  color: $text-color-regular;
}

.nutrition-unit {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

/* 三餐食谱 */
.meal-section {
  background-color: $bg-color-white;
  margin: $spacing-md;
  margin-top: 0;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-sm;
}

.meal-header {
  @include flex-between;
  align-items: center;
  margin-bottom: $spacing-md;
  padding-bottom: $spacing-md;
  border-bottom: 1rpx solid $border-color-lighter;
}

.meal-title-row {
  @include flex-center;
  gap: $spacing-sm;
}

.meal-icon {
  font-size: 48rpx;
}

.meal-title {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.meal-calorie {
  font-size: $font-size-lg;
  color: $primary-color;
  font-weight: $font-weight-bold;
}

.recipe-card {
  @include flex-center;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
}

.recipe-image {
  width: 240rpx;
  height: 180rpx;
  border-radius: $border-radius-lg;
  flex-shrink: 0;
}

.recipe-content {
  flex: 1;
  @include flex-center-column;
  gap: $spacing-sm;
  align-items: flex-start;
}

.recipe-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  @include text-ellipsis;
}

.recipe-tags {
  @include flex-center;
  gap: $spacing-xs;
  flex-wrap: wrap;
}

.tag-item {
  padding: 4rpx 12rpx;
  background-color: rgba(255, 107, 53, 0.1);
  color: $primary-color;
  font-size: $font-size-xs;
  border-radius: $border-radius-round;
}

.recipe-meta {
  @include flex-center;
  gap: $spacing-md;
}

.meta-item {
  @include flex-center;
  gap: $spacing-xs;
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.meta-icon {
  font-size: $font-size-base;
}

.recipe-ingredients {
  @include flex-center;
  gap: $spacing-xs;
  font-size: $font-size-sm;
  color: $text-color-regular;
  width: 100%;
}

.ingredients-label {
  flex-shrink: 0;
  font-weight: $font-weight-medium;
}

.ingredients-text {
  flex: 1;
  @include text-ellipsis-multi(2);
}

.recipe-actions {
  @include flex-center;
  gap: $spacing-md;
}

.action-btn {
  flex: 1;
  height: 72rpx;
  @include flex-center;
  gap: $spacing-xs;
  border-radius: $border-radius-round;
  font-size: $font-size-base;
  border: none;

  &.outline {
    background-color: $bg-color-white;
    color: $text-color-regular;
    border: 1rpx solid $border-color-base;

    &:active {
      background-color: $bg-color-base;
    }
  }

  &.primary {
    background: linear-gradient(135deg, $primary-color, #FF8F61);
    color: #fff;

    &:active {
      opacity: 0.8;
    }
  }
}

.btn-icon {
  font-size: $font-size-lg;
}

/* 饮食小贴士 */
.tips-section {
  background-color: $bg-color-white;
  margin: $spacing-md;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-sm;
}

.tips-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  margin-bottom: $spacing-md;
}

.tips-list {
  @include flex-center-column;
  gap: $spacing-sm;
}

.tip-item {
  width: 100%;
  padding: $spacing-md;
  background-color: rgba(255, 107, 53, 0.05);
  border-radius: $border-radius-base;
  border-left: 4rpx solid $primary-color;
}

.tip-text {
  font-size: $font-size-sm;
  color: $text-color-regular;
  line-height: $line-height-lg;
}

/* 底部空白 */
.bottom-spacer {
  height: 40rpx;
}
</style>
