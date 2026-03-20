<!--
组件名称：MerchantReviews
用途：商家评价展示
复用情况：商家详情、评价列表
创建时间：2026-03-20
-->
<template>
  <view class="merchant-reviews">
    <view class="section-header">
      <text class="section-title">用户评价</text>
      <text class="review-count">{{ reviewCount }}条</text>
    </view>

    <view class="review-summary">
      <view class="rating-overview">
        <text class="rating-score">{{ rating }}</text>
        <view class="rating-stars">
          <text class="star" v-for="i in 5" :key="i">
            {{ i <= Math.floor(rating) ? '⭐' : '☆' }}
          </text>
        </view>
      </view>

      <view class="rating-tags">
        <text class="rating-tag" v-for="tag in reviewTags" :key="tag.label">
          {{ tag.label }} {{ tag.count }}条
        </text>
      </view>
    </view>

    <view class="review-list">
      <view class="review-item" v-for="review in reviews" :key="review.id">
        <view class="review-user">
          <image class="user-avatar" :src="review.user.avatar" mode="aspectFill" />
          <view class="user-info">
            <view class="user-name">{{ review.user.name }}</view>
            <view class="review-stars">
              <text class="star" v-for="i in 5" :key="i">
                {{ i <= review.rating ? '⭐' : '☆' }}
              </text>
            </view>
          </view>
          <view class="review-date">{{ review.date }}</view>
        </view>

        <view class="review-content">{{ review.content }}</view>

        <view class="review-dishes" v-if="review.dishes && review.dishes.length > 0">
          <text class="dish-tag" v-for="dish in review.dishes" :key="dish">{{ dish }}</text>
        </view>
      </view>
    </view>

    <view class="view-all-reviews" @tap="$emit('view-all')">
      查看全部评价 ›
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  reviewCount: {
    type: Number,
    default: 0
  },
  rating: {
    type: Number,
    default: 5.0
  },
  reviewTags: {
    type: Array,
    default: () => []
  },
  reviews: {
    type: Array,
    default: () => []
  }
})

defineEmits(['view-all'])
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.merchant-reviews {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.review-count {
  font-size: 24rpx;
  color: #999;
}

.review-summary {
  margin-bottom: 30rpx;
}

.rating-overview {
  display: flex;
  align-items: center;
  gap: 15rpx;
  margin-bottom: 20rpx;
}

.rating-score {
  font-size: 48rpx;
  font-weight: bold;
  color: #FF6B35;
}

.rating-stars {
  display: flex;
  gap: 5rpx;
}

.star {
  font-size: 28rpx;
}

.rating-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 15rpx;
}

.rating-tag {
  font-size: 22rpx;
  color: #666;
  background: #F5F5F5;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.review-item {
  padding-bottom: 30rpx;
  border-bottom: 1rpx solid #f5f5f5;

  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }
}

.review-user {
  display: flex;
  gap: 15rpx;
  margin-bottom: 15rpx;
}

.user-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.user-name {
  font-size: 26rpx;
  font-weight: 500;
  color: #333;
}

.review-stars {
  display: flex;
  gap: 5rpx;
}

.review-date {
  font-size: 20rpx;
  color: #999;
}

.review-content {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
  margin-bottom: 15rpx;
}

.review-dishes {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.dish-tag {
  font-size: 22rpx;
  color: #FF6B35;
  background: #FFF7E6;
  padding: 6rpx 12rpx;
  border-radius: 4rpx;
  border: 1rpx solid #FFE7CC;
}

.view-all-reviews {
  text-align: center;
  padding: 20rpx 0;
  font-size: 26rpx;
  color: #FF6B35;
}
</style>
