<!--
组件名称：CategoryTabs
用途：菜品分类标签
复用情况：商家详情、菜单管理
创建时间：2026-03-20
-->
<template>
  <view class="category-tabs">
    <scroll-view class="tabs-scroll" scroll-x show-scrollbar="false">
      <view class="tabs-list">
        <view
          class="tab-item"
          :class="{ active: modelValue === category.id }"
          v-for="category in categories"
          :key="category.id"
          @tap="$emit('update:modelValue', category.id)"
        >
          {{ category.name }}
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
const props = defineProps({
  modelValue: {
    type: [String, Number],
    required: true
  },
  categories: {
    type: Array,
    default: () => []
  }
})

defineEmits(['update:modelValue'])
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.category-tabs {
  background: #fff;
  padding: 20rpx 0;
  margin-bottom: 20rpx;
}

.tabs-scroll {
  white-space: nowrap;
}

.tabs-list {
  display: inline-flex;
  padding: 0 20rpx;
}

.tab-item {
  padding: 15rpx 30rpx;
  margin-right: 20rpx;
  font-size: 28rpx;
  color: #666;
  background: #F5F5F5;
  border-radius: 30rpx;
  white-space: nowrap;

  &.active {
    color: #fff;
    background: #FF6B35;
    font-weight: 500;
  }
}
</style>
