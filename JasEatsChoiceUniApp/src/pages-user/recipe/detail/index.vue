<template>
  <view class="recipe-detail-container">
    <!-- 加载状态 -->
    <view class="loading-state" v-if="loading">
      <Loading type="spinner" text="加载中..." />
    </view>

    <!-- 食谱详情 -->
    <view class="recipe-content" v-else-if="recipe">
      <!-- 食谱头部图片 -->
      <view class="recipe-header">
        <image class="header-image" :src="recipe.image" mode="aspectFill" />
        <view class="header-overlay">
          <!-- 收藏按钮 -->
          <view class="action-btn favorite" @click="toggleFavorite">
            <text class="btn-icon">{{ recipe.isFavorite ? '⭐' : '☆' }}</text>
          </view>
          <!-- 分享按钮 -->
          <view class="action-btn share" @click="shareRecipe">
            <text class="btn-icon">📤</text>
          </view>
        </view>
      </view>

      <!-- 食谱信息卡片 -->
      <view class="recipe-info-card">
        <view class="recipe-title-row">
          <text class="recipe-name">{{ recipe.name }}</text>
          <view class="recipe-tags" v-if="recipe.tags && recipe.tags.length">
            <text
              class="tag-item"
              v-for="(tag, index) in recipe.tags.slice(0, 3)"
              :key="index"
            >{{ tag }}</text>
          </view>
        </view>

        <!-- 营养信息 -->
        <view class="nutrition-info">
          <view class="nutrition-item" v-for="item in nutritionList" :key="item.name">
            <text class="nutrition-icon">{{ item.icon }}</text>
            <view class="nutrition-detail">
              <text class="nutrition-value">{{ item.value }}</text>
              <text class="nutrition-name">{{ item.name }}</text>
            </view>
          </view>
        </view>

        <!-- 食谱元信息 -->
        <view class="recipe-meta">
          <view class="meta-item">
            <text class="meta-icon">⏱️</text>
            <text class="meta-text">{{ recipe.cookTime }}分钟</text>
          </view>
          <view class="meta-item">
            <text class="meta-icon">👥</text>
            <text class="meta-text">{{ recipe.servings }}人份</text>
          </view>
          <view class="meta-item">
            <text class="meta-icon">📊</text>
            <text class="meta-text">{{ recipe.difficulty }}</text>
          </view>
        </view>

        <!-- 食谱描述 -->
        <view class="recipe-description" v-if="recipe.description">
          <text class="description-text">{{ recipe.description }}</text>
        </view>
      </view>

      <!-- 食材清单 -->
      <view class="ingredients-section">
        <view class="section-header">
          <text class="section-title">食材清单</text>
          <text class="section-subtitle">共{{ ingredients.length }}种食材</text>
        </view>

        <view class="ingredients-list">
          <view
            class="ingredient-item"
            v-for="(group, groupIndex) in ingredientGroups"
            :key="groupIndex"
          >
            <view class="group-title" v-if="group.name">{{ group.name }}</view>
            <view
              class="ingredient-row"
              v-for="(item, index) in group.items"
              :key="index"
              @click="toggleIngredientCheck(item)"
            >
              <view class="check-box" :class="{ checked: item.checked }">
                <text class="check-icon" v-if="item.checked">✓</text>
              </view>
              <text class="ingredient-name" :class="{ checked: item.checked }">{{ item.name }}</text>
              <text class="ingredient-amount">{{ item.amount }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 制作步骤 -->
      <view class="steps-section">
        <view class="section-header">
          <text class="section-title">制作步骤</text>
          <text class="section-subtitle">共{{ steps.length }}步</text>
        </view>

        <view class="steps-list">
          <view
            class="step-item"
            v-for="(step, index) in steps"
            :key="index"
          >
            <view class="step-number">{{ index + 1 }}</view>
            <view class="step-content">
              <text class="step-text">{{ step.text }}</text>
              <image
                class="step-image"
                v-if="step.image"
                :src="step.image"
                mode="aspectFill"
                @click="previewImage(step.image)"
              />
            </view>
          </view>
        </view>
      </view>

      <!-- 小贴士 -->
      <view class="tips-section" v-if="recipe.tips && recipe.tips.length">
        <view class="section-header">
          <text class="section-title">烹饪小贴士</text>
        </view>

        <view class="tips-list">
          <view
            class="tip-item"
            v-for="(tip, index) in recipe.tips"
            :key="index"
          >
            <text class="tip-icon">💡</text>
            <text class="tip-text">{{ tip }}</text>
          </view>
        </view>
      </view>

      <!-- 相关食谱 -->
      <view class="related-section" v-if="relatedRecipes.length">
        <view class="section-header">
          <text class="section-title">相关食谱</text>
        </view>

        <scroll-view class="related-scroll" scroll-x>
          <view
            class="related-item"
            v-for="item in relatedRecipes"
            :key="item.id"
            @click="goToRecipe(item.id)"
          >
            <image class="related-image" :src="item.image" mode="aspectFill" />
            <text class="related-name">{{ item.name }}</text>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-else>
      <Empty
        icon="🍳"
        text="食谱不存在"
        description="该食谱可能已被删除"
      />
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar" v-if="recipe">
      <button class="action-btn secondary" @click="addToMenu">
        <text class="btn-icon">📋</text>
        <text>加入菜单</text>
      </button>
      <button class="action-btn primary" @click="cookNow">
        <text class="btn-icon">🍳</text>
        <text>开始烹饪</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/store'
import { recipeApi, favoriteApi } from '@/api'
import Loading from '@/components/common/Loading.vue'
import Empty from '@/components/common/Empty.vue'

// Store
const userStore = useUserStore()

// 食谱ID
const recipeId = ref('')

// 食谱数据
const recipe = ref(null)

// 加载状态
const loading = ref(true)

// 食材列表
const ingredients = ref([])

// 步骤列表
const steps = ref([])

// 相关食谱
const relatedRecipes = ref([])

// 营养信息列表
const nutritionList = computed(() => {
  if (!recipe.value) return []
  return [
    {
      icon: '🔥',
      name: '卡路里',
      value: recipe.value.calories + 'kcal'
    },
    {
      icon: '🥩',
      name: '蛋白质',
      value: recipe.value.protein + 'g'
    },
    {
      icon: '🍚',
      name: '碳水',
      value: recipe.value.carbs + 'g'
    },
    {
      icon: '🥑',
      name: '脂肪',
      value: recipe.value.fat + 'g'
    }
  ]
})

// 食材分组
const ingredientGroups = computed(() => {
  const groups = []
  const currentGroup = { name: '', items: [] }

  ingredients.value.forEach(item => {
    if (item.isGroup) {
      if (currentGroup.items.length > 0) {
        groups.push({ ...currentGroup })
      }
      groups.push({ name: item.name, items: [] })
    } else {
      if (groups.length === 0) {
        currentGroup.items.push(item)
      } else {
        groups[groups.length - 1].items.push(item)
      }
    }
  })

  if (currentGroup.items.length > 0) {
    groups.unshift(currentGroup)
  }

  return groups.filter(g => g.items.length > 0)
})

/**
 * 加载食谱详情
 */
const loadRecipeDetail = async () => {
  loading.value = true

  try {
    const res = await recipeApi.getDetail(recipeId.value)

    // 数据映射
    recipe.value = {
      id: res.recipeId || res.id,
      name: res.recipeName || res.name,
      image: res.image || res.coverImage || '',
      description: res.description || '',
      tags: res.tags || [],
      cookTime: res.cookTime || 30,
      servings: res.servings || 2,
      difficulty: res.difficulty || '简单',
      calories: res.calories || 0,
      protein: res.protein || 0,
      carbs: res.carbs || 0,
      fat: res.fat || 0,
      isFavorite: false
    }

    // 设置食材和步骤
    ingredients.value = (res.ingredients || []).map(ing => ({
      ...ing,
      checked: false
    }))
    steps.value = res.steps || []

    // 检查收藏状态
    await checkFavorite()

    // 加载相关食谱
    loadRelatedRecipes()
  } catch (error) {
    console.error('加载食谱详情失败:', error)
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 加载相关食谱
 */
const loadRelatedRecipes = async () => {
  try {
    const res = await recipeApi.getRecommend({
      limit: 6,
      excludeId: recipeId.value
    })

    // 数据映射
    if (Array.isArray(res)) {
      relatedRecipes.value = res.map(r => ({
        id: r.recipeId || r.id,
        name: r.recipeName || r.name,
        image: r.image || r.coverImage || '',
        cookTime: r.cookTime || 30,
        difficulty: r.difficulty || '简单'
      }))
    } else {
      relatedRecipes.value = []
    }
  } catch (error) {
    console.error('加载相关食谱失败:', error)
    relatedRecipes.value = []
  }
}

/**
 * 检查收藏状态
 */
const checkFavorite = async () => {
  try {
    if (!userStore.isLogin || !recipeId.value) {
      return
    }

    const userId = userStore.userInfo?.userId || userStore.userInfo?.id
    const res = await favoriteApi.checkRecipe(recipeId.value, { userId })
    recipe.value.isFavorite = res || false
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

/**
 * 切换收藏
 */
const toggleFavorite = async () => {
  if (!userStore.isLogin) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    setTimeout(() => {
      uni.navigateTo({
        url: '/pages/login/index'
      })
    }, 1500)
    return
  }

  try {
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id

    if (recipe.value.isFavorite) {
      await favoriteApi.removeRecipe(recipeId.value, { userId })
      recipe.value.isFavorite = false
      uni.showToast({
        title: '已取消收藏',
        icon: 'success'
      })
    } else {
      await favoriteApi.addRecipe({
        userId,
        recipeId: recipeId.value,
        recipeName: recipe.value.name,
        recipeImage: recipe.value.image
      })
      recipe.value.isFavorite = true
      uni.showToast({
        title: '收藏成功',
        icon: 'success'
      })
    }
  } catch (error) {
    console.error('操作失败:', error)
    uni.showToast({
      title: error.message || '操作失败',
      icon: 'none'
    })
  }
}

/**
 * 分享食谱
 */
const shareRecipe = () => {
  uni.showShareMenu({
    withShareTicket: true
  })
}

/**
 * 切换食材勾选状态
 */
const toggleIngredientCheck = (item) => {
  item.checked = !item.checked
}

/**
 * 预览图片
 */
const previewImage = (url) => {
  uni.previewImage({
    urls: [url],
    current: url
  })
}

/**
 * 加入菜单
 */
const addToMenu = async () => {
  if (!userStore.isLogin) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }

  try {
    // TODO: 创建 addToMenu API
    // await recipeApi.addToMenu(recipeId.value)
    uni.showToast({
      title: '功能开发中',
      icon: 'none'
    })
  } catch (error) {
    console.error('加入菜单失败:', error)
    uni.showToast({
      title: '操作失败',
      icon: 'none'
    })
  }
}

/**
 * 开始烹饪
 */
const cookNow = () => {
  uni.navigateTo({
    url: `/pages/recipe/cook/index?id=${recipeId.value}`
  })
}

/**
 * 跳转食谱
 */
const goToRecipe = (id) => {
  uni.redirectTo({
    url: `/pages/recipe/detail/index?id=${id}`
  })
}

// 页面加载
onLoad((options) => {
  recipeId.value = options.id
  loadRecipeDetail()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.recipe-detail-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  padding-bottom: 120rpx;
}

/* 加载状态 */
.loading-state {
  @include flex-center;
  height: 100vh;
}

/* 食谱头部 */
.recipe-header {
  position: relative;
  width: 100%;
  height: 500rpx;
  overflow: hidden;
}

.header-image {
  width: 100%;
  height: 100%;
}

.header-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, rgba(0,0,0,0.3), transparent);
  @include flex-between;
  padding: $spacing-lg;
  padding-top: calc(#{$spacing-lg} + var(--status-bar-height));
}

.action-btn {
  width: 72rpx;
  height: 72rpx;
  @include flex-center;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  box-shadow: $box-shadow-md;

  &:active {
    transform: scale(0.95);
  }
}

.btn-icon {
  font-size: $font-size-xl;
}

/* 食谱信息卡片 */
.recipe-info-card {
  background-color: $bg-color-white;
  margin: -$spacing-lg $spacing-md $spacing-md;
  margin-top: calc(-#{$spacing-lg} - 40rpx);
  border-radius: $border-radius-lg $border-radius-lg 0 0;
  padding: $spacing-lg;
  position: relative;
  z-index: 1;
  box-shadow: $box-shadow-sm;
}

.recipe-title-row {
  margin-bottom: $spacing-lg;
}

.recipe-name {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  line-height: $line-height-lg;
  margin-bottom: $spacing-sm;
  display: block;
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

/* 营养信息 */
.nutrition-info {
  @include flex-center;
  gap: $spacing-md;
  padding: $spacing-md 0;
  margin-bottom: $spacing-lg;
  background-color: $bg-color-base;
  border-radius: $border-radius-lg;
}

.nutrition-item {
  flex: 1;
  @include flex-center-column;
  gap: $spacing-xs;
}

.nutrition-icon {
  font-size: $font-size-xl;
}

.nutrition-detail {
  @include flex-center-column;
  gap: 4rpx;
}

.nutrition-value {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.nutrition-name {
  font-size: $font-size-xs;
  color: $text-color-secondary;
}

/* 食谱元信息 */
.recipe-meta {
  @include flex-center;
  gap: $spacing-lg;
  padding: $spacing-md 0;
  border-top: 1rpx solid $border-color-lighter;
  border-bottom: 1rpx solid $border-color-lighter;
  margin-bottom: $spacing-md;
}

.meta-item {
  @include flex-center;
  gap: $spacing-xs;
}

.meta-icon {
  font-size: $font-size-lg;
}

.meta-text {
  font-size: $font-size-sm;
  color: $text-color-regular;
}

/* 食谱描述 */
.recipe-description {
  padding: $spacing-md 0;
}

.description-text {
  font-size: $font-size-base;
  color: $text-color-regular;
  line-height: $line-height-lg;
}

/* 通用区块样式 */
.ingredients-section,
.steps-section,
.tips-section,
.related-section {
  background-color: $bg-color-white;
  margin: $spacing-md;
  padding: $spacing-lg;
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

.section-subtitle {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

/* 食材列表 */
.ingredients-list {
  @include flex-center-column;
  gap: $spacing-md;
}

.ingredient-item {
  width: 100%;
}

.group-title {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  margin-bottom: $spacing-sm;
  padding-left: $spacing-md;
}

.ingredient-row {
  @include flex-center;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  margin-bottom: $spacing-xs;

  &:active {
    background-color: rgba(0, 0, 0, 0.02);
  }
}

.check-box {
  width: 36rpx;
  height: 36rpx;
  @include flex-center;
  border: 2rpx solid $border-color;
  border-radius: $border-radius-sm;
  flex-shrink: 0;
  margin-right: $spacing-md;

  &.checked {
    background-color: $primary-color;
    border-color: $primary-color;
  }
}

.check-icon {
  font-size: $font-size-sm;
  color: #fff;
}

.ingredient-name {
  flex: 1;
  font-size: $font-size-base;
  color: $text-color-primary;

  &.checked {
    text-decoration: line-through;
    color: $text-color-placeholder;
  }
}

.ingredient-amount {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

/* 制作步骤 */
.steps-list {
  @include flex-center-column;
  gap: $spacing-lg;
}

.step-item {
  @include flex-start;
  gap: $spacing-md;
}

.step-number {
  width: 56rpx;
  height: 56rpx;
  @include flex-center;
  background: linear-gradient(135deg, $primary-color, #FF8F61);
  color: #fff;
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  border-radius: 50%;
  flex-shrink: 0;
}

.step-content {
  flex: 1;
  @include flex-center-column;
  align-items: flex-start;
  gap: $spacing-sm;
}

.step-text {
  font-size: $font-size-base;
  color: $text-color-primary;
  line-height: $line-height-lg;
}

.step-image {
  width: 100%;
  height: 360rpx;
  border-radius: $border-radius-base;
  margin-top: $spacing-sm;

  &:active {
    opacity: 0.8;
  }
}

/* 小贴士 */
.tips-list {
  @include flex-center-column;
  gap: $spacing-md;
}

.tip-item {
  @include flex-start;
  gap: $spacing-sm;
  padding: $spacing-md;
  background-color: rgba(255, 107, 53, 0.05);
  border-radius: $border-radius-base;
  border-left: 4rpx solid $primary-color;
}

.tip-icon {
  font-size: $font-size-xl;
  flex-shrink: 0;
}

.tip-text {
  flex: 1;
  font-size: $font-size-sm;
  color: $text-color-regular;
  line-height: $line-height-lg;
}

/* 相关食谱 */
.related-scroll {
  white-space: nowrap;
}

.related-item {
  display: inline-block;
  width: 240rpx;
  margin-right: $spacing-md;
  vertical-align: top;

  &:last-child {
    margin-right: 0;
  }

  &:active {
    opacity: 0.8;
  }
}

.related-image {
  width: 100%;
  height: 180rpx;
  border-radius: $border-radius-base;
  margin-bottom: $spacing-sm;
}

.related-name {
  display: block;
  font-size: $font-size-sm;
  color: $text-color-primary;
  @include text-ellipsis;
}

/* 底部操作栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: $bg-color-white;
  padding: $spacing-md;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
  z-index: $z-index-fixed;
  @include flex-center;
  gap: $spacing-md;
  @include safe-area-bottom;
}

.bottom-bar .action-btn {
  flex: 1;
  height: 88rpx;
  @include flex-center;
  gap: $spacing-sm;
  border-radius: $border-radius-round;
  font-size: $font-size-base;
  border: none;

  &.secondary {
    background-color: $bg-color-base;
    color: $text-color-primary;
  }

  &.primary {
    background: linear-gradient(135deg, $primary-color, #FF8F61);
    color: #fff;
  }

  &:active {
    transform: scale(0.98);
  }
}

/* 空状态 */
.empty-state {
  padding: 120rpx $spacing-lg;
}
</style>
