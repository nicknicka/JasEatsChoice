<!--
组件名称：TabSwitcher
用途：通用标签切换器
复用情况：AI分析、统计页面等
创建时间：2026-03-20
-->
<template>
  <view class="tab-switcher">
    <view
      class="tab-item"
      :class="{ active: modelValue === tab.value }"
      v-for="tab in tabs"
      :key="tab.value"
      @tap="$emit('update:modelValue', tab.value)"
    >
      <text>{{ tab.label }}</text>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  modelValue: {
    type: [String, Number],
    required: true
  },
  tabs: {
    type: Array,
    default: () => []
  }
})

defineEmits(['update:modelValue'])
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '../../styles/mixins.scss';

.tab-switcher {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  padding: 10rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 20rpx 0;
  border-radius: 12rpx;
  transition: all 0.3s;

  text {
    font-size: 28rpx;
    color: #666;
  }

  &.active {
    background: #FF6B35;

    text {
      color: #fff;
      font-weight: bold;
    }
  }
}
</style>
