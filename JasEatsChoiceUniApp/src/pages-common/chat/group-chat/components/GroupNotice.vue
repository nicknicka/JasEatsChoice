<!--
组件名称：GroupNotice
用途：群公告展示
页面专用：仅群聊页面使用
创建时间：2026-03-20
-->
<template>
  <view class="group-notice" v-if="notice" @tap="onTap">
    <uni-icons type="notification" size="16" color="#FF6B35" />
    <text class="notice-text">{{ notice }}</text>
    <uni-icons type="arrowright" size="14" color="#999" />
  </view>
</template>

<script setup>
const props = defineProps({
  notice: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['tap'])

const onTap = () => {
  emit('tap')
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.group-notice {
  background: #FFF7E6;
  padding: 15rpx 30rpx;
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.notice-text {
  flex: 1;
  font-size: 24rpx;
  color: #FF6B35;
  @include text-ellipsis;
}
</style>
