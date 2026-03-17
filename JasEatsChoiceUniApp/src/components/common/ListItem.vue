<template>
  <view class="list-item" :class="{ 'is-clickable': clickable }" @click="handleClick">
    <!-- 左侧图标/头像 -->
    <view class="item-left" v-if="icon || $slots.left">
      <slot name="left">
        <text class="item-icon">{{ icon }}</text>
      </slot>
    </view>

    <!-- 中间内容 -->
    <view class="item-content">
      <view class="item-title-row">
        <text class="item-title">{{ title }}</text>
        <text class="item-extra" v-if="extra">{{ extra }}</text>
      </view>
      <text class="item-desc" v-if="description">{{ description }}</text>
    </view>

    <!-- 右侧箭头/自定义内容 -->
    <view class="item-right">
      <slot name="right">
        <text class="item-arrow" v-if="showArrow">→</text>
      </slot>
    </view>

    <!-- 底部边框 -->
    <view class="item-border" v-if="showBorder"></view>
  </view>
</template>

<script setup>
const props = defineProps({
  // 左侧图标
  icon: String,
  // 标题
  title: String,
  // 额外内容
  extra: String,
  // 描述文字
  description: String,
  // 是否可点击
  clickable: {
    type: Boolean,
    default: true
  },
  // 是否显示箭头
  showArrow: {
    type: Boolean,
    default: true
  },
  // 是否显示底部边框
  showBorder: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['click'])

/**
 * 点击事件
 */
const handleClick = () => {
  if (props.clickable) {
    emit('click')
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.list-item {
  position: relative;
  @include flex-center;
  padding: $spacing-lg $spacing-md;
  background-color: $bg-color-white;

  &.is-clickable {
    &:active {
      background-color: $bg-color-base;
    }
  }
}

.item-left {
  margin-right: $spacing-md;
  flex-shrink: 0;
}

.item-icon {
  font-size: $font-size-xl;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-title-row {
  @include flex-between;
  align-items: center;
  margin-bottom: $spacing-xs;
}

.item-title {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
}

.item-extra {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  flex-shrink: 0;
  margin-left: $spacing-sm;
}

.item-desc {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  @include text-ellipsis;
}

.item-right {
  margin-left: $spacing-sm;
  flex-shrink: 0;
}

.item-arrow {
  font-size: $font-size-lg;
  color: $text-color-placeholder;
}

.item-border {
  position: absolute;
  bottom: 0;
  left: $spacing-md;
  right: 0;
  height: 1rpx;
  background-color: $border-color-lighter;
}
</style>
