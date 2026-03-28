<template>
  <view class="my-recipe-container">
    <!-- 顶部筛选栏 -->
    <view class="filter-bar">
      <scroll-view class="filter-scroll" scroll-x>
        <view
          class="filter-item"
          :class="{ active: selectedFilter === filter.value }"
          v-for="filter in filters"
          :key="filter.value"
          @click="changeFilter(filter.value)"
        >
          <text class="filter-text">{{ filter.label }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 食谱列表 -->
    <scroll-view
      class="scroll-container"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
    >
      <!-- 空状态 -->
      <view class="empty-state" v-if="recipes.length === 0 && !loading">
        <text class="empty-icon">📖</text>
        <text class="empty-text">还没有收藏的食谱</text>
        <text class="empty-tips">去今日食谱看看推荐的美食吧</text>
        <button class="go-today-btn" @click="goToToday">去看看</button>
      </view>

      <!-- 食谱列表 -->
      <view class="recipe-list" v-else>
        <view
          class="recipe-item"
          v-for="recipe in recipes"
          :key="recipe.id"
        >
          <!-- 日期和标签 -->
          <view class="recipe-header">
            <view class="date-info">
              <text class="recipe-date">{{ recipe.date }}</text>
              <view class="meal-tag" :class="recipe.mealType">
                {{ recipe.mealTypeText }}
              </view>
            </view>
            <view class="recipe-actions">
              <text class="action-btn edit" @click="editRecipe(recipe)">✏️ 编辑</text>
              <text class="action-btn delete" @click="deleteRecipe(recipe.id)">🗑️ 删除</text>
            </view>
          </view>

          <!-- 食谱内容 -->
          <view class="recipe-content" @click="viewRecipeDetail(recipe)">
            <image class="recipe-image" :src="recipe.image" mode="aspectFill" />

            <view class="recipe-info">
              <text class="recipe-title">{{ recipe.title }}</text>

              <view class="recipe-tags" v-if="recipe.tags && recipe.tags.length">
                <text class="tag-item" v-for="tag in recipe.tags" :key="tag">{{ tag }}</text>
              </view>

              <view class="recipe-meta">
                <view class="meta-item">
                  <text class="meta-icon">🔥</text>
                  <text class="meta-text">{{ recipe.calories }} kcal</text>
                </view>
                <view class="meta-item">
                  <text class="meta-icon">⏱️</text>
                  <text class="meta-text">{{ recipe.time }}</text>
                </view>
                <view class="meta-item">
                  <text class="meta-icon">👥</text>
                  <text class="meta-text">{{ recipe.servings }}人份</text>
                </view>
              </view>

              <view class="recipe-ingredients">
                <text class="ingredients-label">食材：</text>
                <text class="ingredients-text">{{ recipe.ingredients }}</text>
              </view>

              <!-- 营养成分 -->
              <view class="nutrition-summary" v-if="recipe.nutrition">
                <view class="nutrition-item" v-for="(value, key) in recipe.nutrition" :key="key">
                  <text class="nutrition-label">{{ key }}</text>
                  <text class="nutrition-value">{{ value }}</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 底部操作 -->
          <view class="recipe-footer">
            <button class="footer-btn outline" @click="shareRecipe(recipe)">
              <text class="btn-icon">📤</text>
              <text>分享</text>
            </button>
            <button class="footer-btn primary" @click="cookRecipe(recipe)">
              <text class="btn-icon">🍳</text>
              <text>开始烹饪</text>
            </button>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="load-more" v-if="recipes.length > 0">
        <view class="load-text" v-if="loading">加载中...</view>
        <view class="load-text" v-else-if="!hasMore">没有更多了</view>
        <view class="load-text" v-else>上拉加载更多</view>
      </view>
    </scroll-view>

    <!-- 底部添加按钮 -->
    <view class="bottom-bar">
      <button class="add-btn" @click="addRecipe">
        <text class="add-icon">➕</text>
        <text>添加自定义食谱</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/store/modules/user'
import { favoriteApi } from '@/api/modules/favorite'

// 获取用户store
const userStore = useUserStore()

// 筛选选项
const filters = ref([
  { label: '全部', value: 'all' },
  { label: '早餐', value: 'breakfast' },
  { label: '午餐', value: 'lunch' },
  { label: '晚餐', value: 'dinner' },
  { label: '收藏', value: 'favorite' }
])

// 当前筛选
const selectedFilter = ref('all')

// 食谱列表
const recipes = ref([])

// 加载状态
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)

// 分页参数
const page = ref(1)
const pageSize = ref(10)

/**
 * 获取餐次文本
 */
const getMealTypeText = (mealType) => {
  const mealTypeMap = {
    breakfast: '早餐',
    lunch: '午餐',
    dinner: '晚餐',
    snack: '加餐'
  }
  return mealTypeMap[mealType] || '午餐'
}

/**
 * 格式化食谱数据
 */
const formatRecipeData = (recipe) => {
  return {
    id: recipe.recipeId || recipe.id,
    date: recipe.createTime || recipe.createdAt || '',
    mealType: recipe.mealType || 'lunch',
    mealTypeText: getMealTypeText(recipe.mealType),
    title: recipe.recipeName || recipe.name,
    image: recipe.image || recipe.coverImage || '',
    tags: recipe.tags || [],
    calories: recipe.calories || 0,
    time: `${recipe.cookTime || 30}分钟`,
    servings: recipe.servings || 2,
    ingredients: recipe.ingredients?.map(i => i.name).join('、') || '',
    nutrition: {
      '蛋白质': `${recipe.protein || 0}g`,
      '碳水': `${recipe.carbs || 0}g`,
      '脂肪': `${recipe.fat || 0}g`
    }
  }
}

/**
 * 切换筛选
 */
const changeFilter = (value) => {
  selectedFilter.value = value
  page.value = 1
  recipes.value = []
  loadRecipes()
}

/**
 * 加载食谱列表
 */
const loadRecipes = async (showLoading = true) => {
  // 检查登录状态
  if (!userStore.checkLogin()) {
    return
  }

  if (showLoading) {
    loading.value = true
  }

  try {
    // 调用后端API获取收藏的食谱列表
    const res = await favoriteApi.getRecipeList({
      userId: userStore.userInfo.userId,
      page: page.value,
      size: pageSize.value
    })

    // 格式化数据
    const formattedRecipes = (res.data?.list || res.data || []).map(formatRecipeData)

    // 根据筛选条件过滤数据
    let filteredRecipes = formattedRecipes
    if (selectedFilter.value !== 'all' && selectedFilter.value !== 'favorite') {
      filteredRecipes = formattedRecipes.filter(recipe => recipe.mealType === selectedFilter.value)
    }

    // 分页处理
    if (page.value === 1) {
      recipes.value = filteredRecipes
    } else {
      recipes.value.push(...filteredRecipes)
    }

    // 判断是否还有更多数据
    const totalCount = res.data?.total || 0
    hasMore.value = recipes.value.length < totalCount
  } catch (error) {
    console.error('加载食谱列表失败:', error)
    uni.showToast({
      title: error.message || '加载失败，请重试',
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
  await loadRecipes(false)
  refreshing.value = false
}

/**
 * 上拉加载更多
 */
const onLoadMore = () => {
  if (loading.value || !hasMore.value) return
  page.value++
  loadRecipes()
}

/**
 * 查看食谱详情
 */
const viewRecipeDetail = (recipe) => {
  uni.navigateTo({
    url: `/pages/recipe/detail/index?id=${recipe.id}`
  })
}

/**
 * 编辑食谱
 */
const editRecipe = (recipe) => {
  const params = encodeURIComponent(JSON.stringify(recipe))
  uni.navigateTo({
    url: `/pages/recipe/edit/index?data=${params}`
  })
}

/**
 * 删除食谱（取消收藏）
 */
const deleteRecipe = (id) => {
  // 检查登录状态
  if (!userStore.checkLogin()) {
    return
  }

  uni.showModal({
    title: '删除食谱',
    content: '确定要删除这个食谱吗？',
    confirmColor: '#FF6B35',
    success: async (res) => {
      if (res.confirm) {
        try {
          // 调用后端API取消收藏
          await favoriteApi.removeRecipe(id, userStore.userInfo.userId)

          // 从列表中移除
          const index = recipes.value.findIndex(item => item.id === id)
          if (index > -1) {
            recipes.value.splice(index, 1)
          }

          uni.showToast({
            title: '删除成功',
            icon: 'success'
          })
        } catch (error) {
          console.error('删除食谱失败:', error)
          uni.showToast({
            title: error.message || '删除失败，请重试',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 分享食谱
 */
const shareRecipe = (recipe) => {
  uni.showShareMenu({
    withShareTicket: true
  })

  uni.showToast({
    title: '点击右上角分享',
    icon: 'none'
  })
}

/**
 * 开始烹饪
 */
const cookRecipe = (recipe) => {
  uni.navigateTo({
    url: `/pages/recipe/cook/index?id=${recipe.id}`
  })
}

/**
 * 添加自定义食谱
 */
const addRecipe = () => {
  uni.navigateTo({
    url: '/pages/recipe/edit/index'
  })
}

/**
 * 去今日食谱
 */
const goToToday = () => {
  uni.switchTab({
    url: '/pages/recipe/today'
  })
}

// 组件挂载
onMounted(() => {
  // 检查登录状态后再加载数据
  if (userStore.isLogin) {
    loadRecipes()
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.my-recipe-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  display: flex;
  flex-direction: column;
}

/* 筛选栏 */
.filter-bar {
  background-color: $bg-color-white;
  box-shadow: $box-shadow-sm;
}

.filter-scroll {
  @include flex-center;
  white-space: nowrap;
  padding: $spacing-md $spacing-md;
}

.filter-item {
  padding: $spacing-sm $spacing-md;
  margin-right: $spacing-sm;
  background-color: $bg-color-base;
  border-radius: $border-radius-round;
  @include flex-center;
  flex-shrink: 0;
  transition: all 0.3s;

  &.active {
    background: linear-gradient(135deg, $primary-color, #FF8F61);
    color: #fff;
  }

  &:active {
    transform: scale(0.95);
  }
}

.filter-text {
  font-size: $font-size-sm;
  color: $text-color-regular;

  .active & {
    color: #fff;
    font-weight: $font-weight-medium;
  }
}

/* 滚动容器 */
.scroll-container {
  flex: 1;
  height: calc(100vh - 200rpx);
}

/* 空状态 */
.empty-state {
  @include flex-center-column;
  padding: 200rpx $spacing-lg;

  .empty-icon {
    font-size: 120rpx;
    margin-bottom: $spacing-lg;
    opacity: 0.5;
  }

  .empty-text {
    font-size: $font-size-lg;
    color: $text-color-primary;
    margin-bottom: $spacing-sm;
  }

  .empty-tips {
    font-size: $font-size-sm;
    color: $text-color-secondary;
    margin-bottom: $spacing-xl;
  }
}

.go-today-btn {
  width: 240rpx;
  height: 72rpx;
  @include flex-center;
  background: linear-gradient(135deg, $primary-color, #FF8F61);
  color: #fff;
  font-size: $font-size-base;
  border-radius: $border-radius-round;
  border: none;
}

/* 食谱列表 */
.recipe-list {
  padding: $spacing-md;
}

.recipe-item {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: $spacing-md;
  margin-bottom: $spacing-md;
  box-shadow: $box-shadow-sm;
}

/* 食谱头部 */
.recipe-header {
  @include flex-between;
  align-items: flex-start;
  margin-bottom: $spacing-md;
  padding-bottom: $spacing-md;
  border-bottom: 1rpx solid $border-color-lighter;
}

.date-info {
  @include flex-center;
  gap: $spacing-sm;
}

.recipe-date {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.meal-tag {
  padding: 4rpx 12rpx;
  border-radius: $border-radius-round;
  font-size: $font-size-xs;
  color: #fff;
  font-weight: $font-weight-medium;

  &.breakfast {
    background-color: #FFB74D;
  }

  &.lunch {
    background-color: #FF7043;
  }

  &.dinner {
    background-color: #81C784;
  }
}

.recipe-actions {
  @include flex-center;
  gap: $spacing-md;
}

.action-btn {
  font-size: $font-size-sm;
  padding: $spacing-xs $spacing-sm;

  &.edit {
    color: $info-color;
  }

  &.delete {
    color: $danger-color;
  }
}

/* 食谱内容 */
.recipe-content {
  @include flex-center;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
}

.recipe-image {
  width: 240rpx;
  height: 240rpx;
  border-radius: $border-radius-lg;
  flex-shrink: 0;
}

.recipe-info {
  flex: 1;
  @include flex-center-column;
  gap: $spacing-sm;
  align-items: flex-start;
}

.recipe-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  @include text-ellipsis-multiline(2);
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
  flex-wrap: wrap;
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

.nutrition-summary {
  @include flex-center;
  gap: $spacing-md;
  flex-wrap: wrap;
  padding: $spacing-sm;
  background-color: rgba(255, 107, 53, 0.05);
  border-radius: $border-radius-base;
  width: 100%;
}

.nutrition-item {
  @include flex-center-column;
  gap: $spacing-xs;
  padding: $spacing-xs;
}

.nutrition-label {
  font-size: $font-size-xs;
  color: $text-color-secondary;
}

.nutrition-value {
  font-size: $font-size-sm;
  color: $primary-color;
  font-weight: $font-weight-medium;
}

/* 食谱底部操作 */
.recipe-footer {
  @include flex-center;
  gap: $spacing-md;
  padding-top: $spacing-md;
  border-top: 1rpx solid $border-color-lighter;
}

.footer-btn {
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

/* 底部添加按钮 */
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

  .add-icon {
    font-size: $font-size-xl;
  }
}

/* 加载状态 */
.load-more {
  @include flex-center;
  padding: $spacing-lg 0;
}

.load-text {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}
</style>
