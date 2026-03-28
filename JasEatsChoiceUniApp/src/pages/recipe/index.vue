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
import { useUserStore } from '@/store/modules/user'
import { recipeApi } from '@/api/modules/recipe'

// 用户store
const userStore = useUserStore()

// 日期信息
const todayDate = ref('')
const todayWeekday = ref('')

// 今日总卡路里
const totalCalorie = ref(0)

// 营养摄入建议
const nutritionList = ref([
  {
    name: '蛋白质',
    icon: '🥩',
    current: 0,
    target: 80,
    unit: 'g',
    percent: 0,
    color: '#FF6B35'
  },
  {
    name: '碳水化合物',
    icon: '🍚',
    current: 0,
    target: 300,
    unit: 'g',
    percent: 0,
    color: '#FFB74D'
  },
  {
    name: '脂肪',
    icon: '🥑',
    current: 0,
    target: 60,
    unit: 'g',
    percent: 0,
    color: '#81C784'
  },
  {
    name: '膳食纤维',
    icon: '🥦',
    current: 0,
    target: 25,
    unit: 'g',
    percent: 0,
    color: '#64B5F6'
  }
])

// 三餐食谱
const meals = ref([])

/**
 * 加载今日推荐食谱
 */
const loadTodayRecommend = async () => {
  try {
    uni.showLoading({ title: '加载中...' })

    // 调用API获取今日食谱
    const res = await recipeApi.getToday({
      userId: userStore.userInfo?.userId || userStore.userInfo?.id
    })

    uni.hideLoading()

    if (res.data && res.data.recipes) {
      const recipes = res.data.recipes

      // 更新营养数据
      if (res.data.nutrition) {
        const nutrition = res.data.nutrition
        totalCalorie.value = nutrition.calories || 0

        // 更新营养列表
        nutritionList.value[0].current = nutrition.protein || 0
        nutritionList.value[0].percent = Math.min(100, Math.round((nutrition.protein || 0) / 80 * 100))

        nutritionList.value[1].current = nutrition.carbs || 0
        nutritionList.value[1].percent = Math.min(100, Math.round((nutrition.carbs || 0) / 300 * 100))

        nutritionList.value[2].current = nutrition.fat || 0
        nutritionList.value[2].percent = Math.min(100, Math.round((nutrition.fat || 0) / 60 * 100))

        // 膳食纤维暂时使用计算值（后端暂未提供）
        nutritionList.value[3].current = Math.round((nutrition.carbs || 0) * 0.1)
        nutritionList.value[3].percent = Math.min(100, Math.round(((nutrition.carbs || 0) * 0.1) / 25 * 100))
      }

      // 映射食谱数据为三餐格式
      const mealTypeMap = {
        breakfast: { icon: '🌅', title: '早餐' },
        lunch: { icon: '☀️', title: '午餐' },
        dinner: { icon: '🌙', title: '晚餐' },
        snack: { icon: '🍎', title: '加餐' }
      }

      const mealList = recipes.map(recipe => {
        const typeInfo = mealTypeMap[recipe.type] || { icon: '🍽️', title: '其他' }

        // 解析items字段（可能是JSON字符串）
        let items = []
        try {
          if (recipe.items) {
            items = typeof recipe.items === 'string' ? JSON.parse(recipe.items) : recipe.items
          }
        } catch (e) {
          console.error('解析items失败:', e)
        }

        return {
          type: recipe.type,
          icon: typeInfo.icon,
          title: typeInfo.title,
          calorie: recipe.calories || 0,
          recipeName: recipe.name || '未命名食谱',
          image: getRecipeImage(recipe, typeInfo.title),
          tags: getRecipeTags(recipe),
          time: recipe.cookTime ? `${recipe.cookTime}分钟` : '30分钟',
          ingredients: Array.isArray(items) && items.length > 0
            ? items.map(item => typeof item === 'object' ? item.name : item).join('、')
            : '暂无食材信息',
          recipeId: recipe.id,
          detail: recipe.detail || ''
        }
      })

      // 按照早餐、午餐、晚餐的顺序排序
      const order = ['breakfast', 'lunch', 'dinner', 'snack']
      mealList.sort((a, b) => order.indexOf(a.type) - order.indexOf(b.type))

      meals.value = mealList
    } else {
      // 如果没有今日食谱，使用默认空数据
      meals.value = []
      uni.showToast({
        title: '暂无今日食谱',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('加载今日食谱失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: '加载失败，请重试',
      icon: 'none'
    })
  }
}

/**
 * 获取食谱图片（如果没有图片则使用默认占位图）
 */
const getRecipeImage = (recipe, title) => {
  // 这里可以尝试从recipe中提取图片，如果没有则使用默认占位图
  const colors = {
    '早餐': 'FFE0B2',
    '午餐': 'FFCCBC',
    '晚餐': 'C8E6C9',
    '加餐': 'B2DFDB'
  }
  const color = colors[title] || 'B3E5FC'
  return `https://via.placeholder.com/400x300/${color}/FF6B35?text=${encodeURIComponent(title)}`
}

/**
 * 获取食谱标签
 */
const getRecipeTags = (recipe) => {
  const tags = []

  // 根据卡路里添加标签
  if (recipe.calories < 400) {
    tags.push('低卡')
  } else if (recipe.calories > 800) {
    tags.push('高能量')
  }

  // 根据类型添加标签
  if (recipe.type === 'breakfast') {
    tags.push('营养早餐')
  } else if (recipe.type === 'dinner') {
    tags.push('清淡')
  }

  // 根据营养比例添加标签
  if (recipe.protein && recipe.protein > 20) {
    tags.push('高蛋白')
  }

  return tags.length > 0 ? tags : ['健康']
}

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

    // 调用API获取新推荐
    const res = await recipeApi.getRecommend({
      type: meal.type,
      calories: meal.calorie
    })

    uni.hideLoading()

    if (res.data && res.data.length > 0) {
      // 随机选择一个推荐的食谱
      const newRecipe = res.data[Math.floor(Math.random() * res.data.length)]

      // 解析items字段
      let items = []
      try {
        if (newRecipe.items) {
          items = typeof newRecipe.items === 'string' ? JSON.parse(newRecipe.items) : newRecipe.items
        }
      } catch (e) {
        console.error('解析items失败:', e)
      }

      // 更新当前餐次的数据
      const mealIndex = meals.value.findIndex(m => m.type === meal.type)
      if (mealIndex !== -1) {
        meals.value[mealIndex] = {
          ...meals.value[mealIndex],
          calorie: newRecipe.calories || 0,
          recipeName: newRecipe.name || '新推荐食谱',
          image: getRecipeImage(newRecipe, meal.title),
          tags: getRecipeTags(newRecipe),
          time: newRecipe.cookTime ? `${newRecipe.cookTime}分钟` : '30分钟',
          ingredients: Array.isArray(items) && items.length > 0
            ? items.map(item => typeof item === 'object' ? item.name : item).join('、')
            : '暂无食材信息',
          recipeId: newRecipe.id,
          detail: newRecipe.detail || ''
        }

        // 更新总卡路里
        totalCalorie.value = meals.value.reduce((sum, m) => sum + m.calorie, 0)
      }

      uni.showToast({
        title: '已为您推荐新食谱',
        icon: 'success'
      })
    } else {
      uni.showToast({
        title: '暂无更多推荐',
        icon: 'none'
      })
    }
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
  // 检查登录状态
  if (!userStore.checkLogin()) {
    return
  }

  uni.showModal({
    title: '订餐确认',
    content: `确定要订「${meal.recipeName}」吗？`,
    confirmColor: '#FF6B35',
    success: (res) => {
      if (res.confirm) {
        // 解析食材列表
        let items = []
        try {
          if (meal.ingredients) {
            // 从ingredients字符串中提取食材名称
            items = meal.ingredients.split('、').filter(item => item.trim())
          }
        } catch (e) {
          console.error('解析食材失败:', e)
        }

        // 这里可以跳转到商家列表或菜品详情页面
        // 暂时跳转到商家页面，用户可以在商家页面选择具体的菜品
        uni.showToast({
          title: '正在前往商家页面...',
          icon: 'none'
        })

        setTimeout(() => {
          // 跳转到商家列表页面，传递餐次类型和卡路里信息
          uni.navigateTo({
            url: `/pages/merchant/list?mealType=${meal.type}&calorie=${meal.calorie}`
          })
        }, 1000)
      }
    }
  })
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

  // 加载今日推荐食谱
  loadTodayRecommend()
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
  @include text-ellipsis-multiline(2);
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
