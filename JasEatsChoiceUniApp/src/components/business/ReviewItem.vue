<template>
  <view class="review-item" @click="handleClick">
    <!-- 用户信息 -->
    <view class="review-header">
      <image class="user-avatar" :src="review.userAvatar" mode="aspectFill" />
      <view class="user-info">
        <text class="user-name">{{ review.userName }}</text>
        <view class="review-rating">
          <text
            class="rating-star"
            v-for="index in 5"
            :key="index"
          >{{ index <= review.rating ? '⭐' : '☆' }}</text>
        </view>
      </view>
      <text class="review-time">{{ review.timeText }}</text>
    </view>

    <!-- 评价内容 -->
    <view class="review-content">
      <text class="review-text">{{ review.content }}</text>
    </view>

    <!-- 评价图片 -->
    <view class="review-images" v-if="review.images && review.images.length">
      <image
        class="review-image"
        v-for="(image, index) in review.images.slice(0, 3)"
        :key="index"
        :src="image"
        mode="aspectFill"
        @click.stop="previewImage(index)"
      />
      <view class="more-images" v-if="review.images.length > 3">
        <text class="more-text">+{{ review.images.length - 3 }}</text>
      </view>
    </view>

    <!-- 商家回复 -->
    <view class="merchant-reply" v-if="review.merchantReply">
      <view class="reply-header">
        <text class="reply-label">商家回复：</text>
        <text class="reply-time">{{ review.replyTime }}</text>
      </view>
      <text class="reply-content">{{ review.merchantReply }}</text>
    </view>

    <!-- 评价标签 -->
    <view class="review-tags" v-if="review.tags && review.tags.length">
      <text
        class="tag-item"
        v-for="(tag, index) in review.tags"
        :key="index"
      >{{ tag }}</text>
    </view>

    <!-- 菜品信息（可选） -->
    <view class="dish-info" v-if="review.dishName && showDish">
      <text class="dish-icon">🍽️</text>
      <text class="dish-name">{{ review.dishName }}</text>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  // 评价数据
  review: {
    type: Object,
    required: true
  },
  // 是否显示菜品信息
  showDish: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click'])

/**
 * 点击评价
 */
const handleClick = () => {
  emit('click', props.review)
}

/**
 * 预览图片
 */
const previewImage = (index) => {
  uni.previewImage({
    urls: props.review.images,
    current: index
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.review-item {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: $spacing-md;
  margin-bottom: $spacing-md;

  &:active {
    background-color: $bg-color-base;
  }
}

.review-header {
  @include flex-center;
  margin-bottom: $spacing-md;
}

.user-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  margin-left: $spacing-md;
  @include flex-center-column;
  align-items: flex-start;
  gap: 4rpx;
}

.user-name {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-color-primary;
}

.review-rating {
  @include flex-center;
  gap: 4rpx;
}

.rating-star {
  font-size: $font-size-sm;
  color: $warning-color;
}

.review-time {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  flex-shrink: 0;
}

.review-content {
  margin-bottom: $spacing-md;
}

.review-text {
  font-size: $font-size-base;
  color: $text-color-primary;
  line-height: $line-height-lg;
}

.review-images {
  @include flex-center;
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
  flex-wrap: wrap;
}

.review-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: $border-radius-base;

  &:active {
    opacity: 0.8;
  }
}

.more-images {
  width: 160rpx;
  height: 160rpx;
  @include flex-center;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
}

.more-text {
  font-size: $font-size-base;
  color: $text-color-secondary;
}

.merchant-reply {
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  margin-bottom: $spacing-md;
}

.reply-header {
  @include flex-between;
  align-items: center;
  margin-bottom: $spacing-xs;
}

.reply-label {
  font-size: $font-size-sm;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
}

.reply-time {
  font-size: $font-size-xs;
  color: $text-color-secondary;
}

.reply-content {
  font-size: $font-size-sm;
  color: $text-color-regular;
  line-height: $line-height-lg;
}

.review-tags {
  @include flex-center;
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
  flex-wrap: wrap;
}

.tag-item {
  padding: 4rpx 12rpx;
  background-color: rgba(255, 107, 53, 0.1);
  color: $primary-color;
  font-size: $font-size-xs;
  border-radius: $border-radius-round;
}

.dish-info {
  @include flex-center;
  gap: $spacing-xs;
  padding-top: $spacing-sm;
  border-top: 1rpx solid $border-color-lighter;
}

.dish-icon {
  font-size: $font-size-base;
}

.dish-name {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}
</style>
