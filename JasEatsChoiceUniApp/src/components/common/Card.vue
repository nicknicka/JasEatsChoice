<template>
  <view class="custom-card" :class="[`card-${type}`, { 'is-shadow': shadow }]" :style="customStyle">
    <!-- 卡片头部 -->
    <view class="card-header" v-if="title || $slots.header">
      <slot name="header">
        <text class="card-title">{{ title }}</text>
        <text class="card-extra" v-if="extra" @click="handleExtraClick">{{ extra }}</text>
      </slot>
    </view>

    <!-- 卡片内容 -->
    <view class="card-body" :class="{ 'is-padding': padding }">
      <slot></slot>
    </view>

    <!-- 卡片底部 -->
    <view class="card-footer" v-if="$slots.footer">
      <slot name="footer"></slot>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 卡片标题
  title: String,
  // 额外内容
  extra: String,
  // 卡片类型
  type: {
    type: String,
    default: 'default'
  },
  // 是否显示阴影
  shadow: {
    type: Boolean,
    default: true
  },
  // 内容是否需要padding
  padding: {
    type: Boolean,
    default: true
  },
  // 自定义样式
  customStyle: [String, Object]
})

const emit = defineEmits(['extra-click'])

/**
 * 额外内容点击
 */
const handleExtraClick = () => {
  emit('extra-click')
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.custom-card {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  overflow: hidden;

  &.is-shadow {
    box-shadow: $box-shadow-sm;
  }

  &.card-primary {
    background: linear-gradient(135deg, $primary-color, #FF8F61);
    color: #fff;
  }

  &.card-success {
    background-color: $success-color;
    color: #fff;
  }

  &.card-warning {
    background-color: $warning-color;
    color: #fff;
  }

  &.card-danger {
    background-color: $danger-color;
    color: #fff;
  }
}

.card-header {
  @include flex-between;
  align-items: center;
  padding: $spacing-md;
  border-bottom: 1rpx solid $border-color-lighter;

  .card-title {
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;
    color: $text-color-primary;
  }

  .card-extra {
    font-size: $font-size-sm;
    color: $primary-color;

    &:active {
      opacity: 0.6;
    }
  }
}

.card-body {
  &.is-padding {
    padding: $spacing-md;
  }
}

.card-footer {
  padding: $spacing-md;
  border-top: 1rpx solid $border-color-lighter;
}
</style>
