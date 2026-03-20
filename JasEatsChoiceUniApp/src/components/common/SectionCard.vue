<!--
组件名称：SectionCard
用途：通用卡片容器（带标题和可选操作）
复用情况：全项目通用
创建时间：2026-03-20
-->
<template>
  <view class="section-card">
    <view class="section-header" v-if="title || $slots.header">
      <slot name="header">
        <text class="section-title">{{ title }}</text>
        <text class="extra" v-if="extra" @tap="$emit('extra-click')">{{ extra }}</text>
      </slot>
    </view>
    <view class="section-content">
      <slot></slot>
    </view>
  </view>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: ''
  },
  extra: {
    type: String,
    default: ''
  }
})

defineEmits(['extra-click'])
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.section-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.extra {
  font-size: 26rpx;
  color: #FF6B35;
}

.section-content {
  // 内容区域样式
}
</style>
