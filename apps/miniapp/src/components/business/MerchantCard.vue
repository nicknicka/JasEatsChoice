<template>
  <view class="merchant-card" @click="handleClick">
    <!-- 商家图片 -->
    <image class="merchant-image" :src="merchant.image" mode="aspectFill" />

    <!-- 商家信息 -->
    <view class="merchant-info">
      <text class="merchant-name">{{ merchant.name }}</text>

      <!-- 评分 -->
      <view class="merchant-rating">
        <text class="rating-star">⭐ {{ merchant.rating }}</text>
        <text class="rating-count" v-if="merchant.reviewCount">
          ({{ merchant.reviewCount }}条评价)
        </text>
      </view>

      <!-- 标签 -->
      <view class="merchant-tags" v-if="merchant.tags && merchant.tags.length">
        <text
          class="tag-item"
          v-for="(tag, index) in merchant.tags.slice(0, 3)"
          :key="index"
        >
          {{ tag }}
        </text>
      </view>

      <!-- 描述 -->
      <text class="merchant-desc" v-if="merchant.description">
        {{ merchant.description }}
      </text>

      <!-- 地址和距离 -->
      <view class="merchant-location" v-if="merchant.address || merchant.distance">
        <text class="location-text" v-if="merchant.address">{{ merchant.address }}</text>
        <text class="distance-text" v-if="merchant.distance">
          距离{{ merchant.distance }}
        </text>
      </view>
    </view>

    <!-- 收藏按钮 -->
    <view class="favorite-btn" @click.stop="toggleFavorite">
      <text class="favorite-icon">{{ merchant.isFavorite ? '⭐' : '☆' }}</text>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  // 商家数据
  merchant: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click', 'favorite'])

/**
 * 点击卡片
 */
const handleClick = () => {
  emit('click', props.merchant)
}

/**
 * 切换收藏
 */
const toggleFavorite = () => {
  emit('favorite', props.merchant)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.merchant-card {
  position: relative;
  @include flex-center;
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: $spacing-md;
  box-shadow: $box-shadow-sm;

  &:active {
    background-color: $bg-color-base;
  }
}

.merchant-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: $border-radius-base;
  flex-shrink: 0;
}

.merchant-info {
  flex: 1;
  margin-left: $spacing-md;
  @include flex-center-column;
  align-items: flex-start;
  gap: $spacing-xs;
  min-width: 0;
}

.merchant-name {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.merchant-rating {
  @include flex-center;
  gap: $spacing-sm;
}

.rating-star {
  font-size: $font-size-sm;
  color: $warning-color;
  font-weight: $font-weight-medium;
}

.rating-count {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.merchant-tags {
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

.merchant-desc {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  @include text-ellipsis-multiline(2);
  line-height: $line-height-lg;
}

.merchant-location {
  @include flex-center;
  gap: $spacing-md;
  width: 100%;
}

.location-text {
  flex: 1;
  font-size: $font-size-sm;
  color: $text-color-secondary;
  @include text-ellipsis;
}

.distance-text {
  font-size: $font-size-sm;
  color: $primary-color;
  flex-shrink: 0;
}

.favorite-btn {
  position: absolute;
  top: $spacing-md;
  right: $spacing-md;
  width: 56rpx;
  height: 56rpx;
  @include flex-center;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  box-shadow: $box-shadow-sm;

  &:active {
    transform: scale(0.9);
  }
}

.favorite-icon {
  font-size: $font-size-xl;
}
</style>
