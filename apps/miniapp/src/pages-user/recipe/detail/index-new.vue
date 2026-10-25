<template>
  <view class="recipe-detail-container">
    <!-- 加载状态 -->
    <view class="loading-state" v-if="loading">
      <Loading type="spinner" text="加载中..." />
    </view>

    <!-- 食谱详情 -->
    <view class="recipe-content" v-else-if="recipe">
      <!-- 食谱头部 -->
      <RecipeHeader
        :recipe="recipe"
        @favorite="toggleFavorite"
        @share="shareRecipe"
      />

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

        <!-- 食谱描述 -->
        <view class="recipe-description" v-if="recipe.description">
          <text class="description-text">{{ recipe.description }}</text>
        </view>

        <!-- 营养信息 -->
        <NutritionInfo :recipe="recipe" />
      </view>

      <!-- 食材清单 -->
      <view class="ingredients-section">
        <view class="section-header">
          <text class="section-title">食材清单</text>
          <text class="section-subtitle">共{{ ingredients.length }}种食材</text>
        </view>

        <IngredientList
          :ingredients="ingredients"
          @check="handleIngredientCheck"
        />
      </view>

      <!-- 制作步骤 -->
      <view class="steps-section">
        <view class="section-header">
          <text class="section-title">制作步骤</text>
          <text class="section-subtitle">共{{ steps.length }}步</text>
        </view>

        <CookSteps :steps="steps" />
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
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import Loading from '@/components/common/Loading.vue'
import Empty from '@/components/common/Empty.vue'
import RecipeHeader from './components/RecipeHeader.vue'
import IngredientList from './components/IngredientList.vue'
import CookSteps from './components/CookSteps.vue'
import NutritionInfo from './components/NutritionInfo.vue'
import api from '@/api'

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

/**
 * 加载食谱详情
 */
const loadRecipeDetail = async () => {
  loading.value = true

  try {
    const res = await api.recipe.getRecipeDetail(recipeId.value)
    recipe.value = res.data

    // 设置食材和步骤
    ingredients.value = recipe.value.ingredients || []
    steps.value = recipe.value.steps || []

    // 加载相关食谱
    loadRelatedRecipes()
  } catch (error) {
    console.error('加载食谱详情失败:', error)
    uni.showToast({
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
    const res = await api.recipe.getRelatedRecipes({
      recipeId: recipeId.value,
      limit: 6
    })
    relatedRecipes.value = res.data.list || []
  } catch (error) {
    console.error('加载相关食谱失败:', error)
  }
}

/**
 * 切换收藏
 */
const toggleFavorite = async () => {
  try {
    if (recipe.value.isFavorite) {
      await api.recipe.unfavoriteRecipe(recipeId.value)
      recipe.value.isFavorite = false
      uni.showToast({
        title: '已取消收藏',
        icon: 'success'
      })
    } else {
      await api.recipe.favoriteRecipe(recipeId.value)
      recipe.value.isFavorite = true
      uni.showToast({
        title: '收藏成功',
        icon: 'success'
      })
    }
  } catch (error) {
    console.error('操作失败:', error)
    uni.showToast({
      title: '操作失败',
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
 * 食材勾选
 */
const handleIngredientCheck = (item) => {
  // 勾选状态已在组件内部处理
}

/**
 * 加入菜单
 */
const addToMenu = async () => {
  try {
    await api.recipe.addToMenu(recipeId.value)
    uni.showToast({
      title: '已加入菜单',
      icon: 'success'
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

.loading-state {
  @include flex-center;
  height: 100vh;
}

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

.recipe-description {
  padding: $spacing-md 0;
  margin-bottom: $spacing-md;
}

.description-text {
  font-size: $font-size-base;
  color: $text-color-regular;
  line-height: $line-height-lg;
}

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

.empty-state {
  padding: 120rpx $spacing-lg;
}
</style>
