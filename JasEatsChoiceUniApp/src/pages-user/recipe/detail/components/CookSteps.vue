<template>
  <view class="cook-steps">
    <!-- 烹饪步骤列表 -->
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
</template>

<script setup>
const props = defineProps({
  // 步骤列表
  steps: {
    type: Array,
    default: () => []
  }
})

/**
 * 预览图片
 */
const previewImage = (url) => {
  uni.previewImage({
    urls: [url],
    current: url
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.cook-steps {
  @include flex-center-column;
  gap: $spacing-lg;
}

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
</style>
