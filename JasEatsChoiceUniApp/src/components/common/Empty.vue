<template>
  <view class="empty-container">
    <!-- 空状态图标 -->
    <view class="empty-icon-wrapper">
      <text class="empty-icon">{{ icon }}</text>
    </view>

    <!-- 空状态文字 -->
    <text class="empty-text">{{ text }}</text>

    <!-- 空状态描述 -->
    <text class="empty-desc" v-if="description">{{ description }}</text>

    <!-- 操作按钮 -->
    <button class="empty-btn" v-if="buttonText" @click="handleButtonClick">
      {{ buttonText }}
    </button>
  </view>
</template>

<script setup>
const props = defineProps({
  // 图标（emoji）
  icon: {
    type: String,
    default: '📦'
  },
  // 主标题
  text: {
    type: String,
    required: true
  },
  // 描述文字
  description: String,
  // 按钮文字
  buttonText: String
})

const emit = defineEmits(['button-click'])

/**
 * 按钮点击
 */
const handleButtonClick = () => {
  emit('button-click')
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.empty-container {
  @include flex-center-column;
  padding: 120rpx $spacing-lg;
  text-align: center;
}

.empty-icon-wrapper {
  margin-bottom: $spacing-xl;
}

.empty-icon {
  font-size: 140rpx;
  opacity: 0.6;
}

.empty-text {
  font-size: $font-size-lg;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
  margin-bottom: $spacing-sm;
}

.empty-desc {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  margin-bottom: $spacing-xl;
  line-height: $line-height-lg;
}

.empty-btn {
  min-width: 240rpx;
  height: 72rpx;
  @include flex-center;
  background: linear-gradient(135deg, $primary-color, #FF8F61);
  color: #fff;
  font-size: $font-size-base;
  border-radius: $border-radius-round;
  border: none;
  padding: 0 $spacing-xl;

  &:active {
    opacity: 0.8;
  }
}
</style>
