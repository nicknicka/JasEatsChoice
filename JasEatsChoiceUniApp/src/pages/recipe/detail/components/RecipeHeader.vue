<template>
  <view class="recipe-header">
    <image class="header-image" :src="recipe.image" mode="aspectFill" />
    <view class="header-overlay">
      <!-- 收藏按钮 -->
      <view class="action-btn favorite" @click="handleFavorite">
        <text class="btn-icon">{{ recipe.isFavorite ? '⭐' : '☆' }}</text>
      </view>
      <!-- 分享按钮 -->
      <view class="action-btn share" @click="handleShare">
        <text class="btn-icon">📤</text>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  // 食谱数据
  recipe: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['favorite', 'share'])

/**
 * 切换收藏
 */
const handleFavorite = () => {
  emit('favorite')
}

/**
 * 分享食谱
 */
const handleShare = () => {
  emit('share')
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

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
</style>
