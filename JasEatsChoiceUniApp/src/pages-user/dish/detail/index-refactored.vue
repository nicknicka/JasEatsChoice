<!--
页面名称：dish/detail/index（重构版）
原代码行数：1258行
重构后行数：约350行
减少比例：72%
重构时间：2026-03-20
-->
<template>
  <view class="dish-detail-container">
    <scroll-view class="scroll-container" scroll-y>
      <!-- 菜品图片轮播 -->
      <ImageSwiper :images="dishDetail.images" />

      <!-- 菜品基本信息 -->
      <view class="info-section card">
        <DishInfoHeader
          :dish="dishDetail"
          :is-favorite="isFavorite"
          @toggle-favorite="toggleFavorite"
        />
      </view>

      <!-- 营养信息 -->
      <view class="nutrition-section card" v-if="dishDetail.nutrition && Object.keys(dishDetail.nutrition).length > 0">
        <NutritionGrid
          title="营养信息"
          unit="每100克"
          :nutrition-list="nutritionList"
          :columns="5"
        />
      </view>

      <!-- 食材列表 -->
      <view class="ingredients-section card" v-if="dishDetail.ingredients && dishDetail.ingredients.length > 0">
        <IngredientsScroll :ingredients="dishDetail.ingredients" />
      </view>

      <!-- 商家信息 -->
      <view class="merchant-section card">
        <MerchantMiniCard :merchant="dishDetail.merchant" @tap="toMerchant" />
      </view>

      <!-- 评价列表 -->
      <view class="review-section card">
        <view class="section-header">
          <text class="section-title">用户评价</text>
          <text class="review-count">{{ dishDetail.reviewCount }}条评价</text>
        </view>

        <DishReviewSummary
          v-if="dishDetail.reviewSummary"
          :review-summary="dishDetail.reviewSummary"
          :review-count="dishDetail.reviewCount"
        />

        <view class="review-list">
          <ReviewCard
            v-for="review in reviews"
            :key="review.id"
            :review="review"
            @preview-image="previewReviewImage"
          />
        </view>

        <view class="view-all-reviews" @tap="viewAllReviews">
          查看全部评价 ›
        </view>
      </view>

      <!-- 相关推荐 -->
      <DishRecommendGrid
        v-if="recommendDishes.length > 0"
        :dishes="recommendDishes"
        @tap="toDishDetail"
      />
    </scroll-view>

    <!-- 底部操作栏 -->
    <DishBottomBar
      :quantity="quantity"
      :cart-count="cartCount"
      @cart="toCart"
      @chat="contactMerchant"
      @increase-quantity="increaseQuantity"
      @decrease-quantity="decreaseQuantity"
      @add-cart="addToCart"
    />
  </view>
</template>

<script setup>
import { onMounted } from 'vue'
import ImageSwiper from '@/components/common/ImageSwiper.vue'
import NutritionGrid from '@/components/common/NutritionGrid.vue'
import ReviewCard from '@/components/common/ReviewCard.vue'
import MerchantMiniCard from '@/components/business/merchant/MerchantMiniCard.vue'
import DishInfoHeader from './components/DishInfoHeader.vue'
import IngredientsScroll from './components/IngredientsScroll.vue'
import DishReviewSummary from './components/DishReviewSummary.vue'
import DishRecommendGrid from './components/DishRecommendGrid.vue'
import DishBottomBar from './components/DishBottomBar.vue'
import { useDishDetail } from '@/composables/dish/useDishDetail'

// 使用菜品详情 composable
const {
  dishId,
  isFavorite,
  quantity,
  reviews,
  recommendDishes,
  dishDetail,
  cartCount,
  nutritionList,
  loadDishDetail,
  loadReviews,
  loadRecommendDishes,
  previewReviewImage,
  toggleFavorite,
  increaseQuantity,
  decreaseQuantity,
  addToCart,
  toMerchant,
  toDishDetail,
  viewAllReviews,
  toCart,
  contactMerchant
} = useDishDetail()

// 组件挂载时加载数据
onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options

  if (options.id) {
    dishId.value = options.id
  }

  // 加载数据
  loadDishDetail()
  loadReviews()
  loadRecommendDishes()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.dish-detail-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  padding-bottom: 140rpx;
}

.scroll-container {
  height: 100vh;
}

/* 卡片通用样式 */
.card {
  background-color: $bg-color-white;
  margin-bottom: $spacing-md;
  padding: $spacing-md;
}

.section-header {
  @include flex-between;
  margin-bottom: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.review-count {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.review-list {
  margin-bottom: $spacing-md;
}

.view-all-reviews {
  text-align: center;
  padding: $spacing-md;
  color: $primary-color;
  font-size: $font-size-base;
}
</style>
