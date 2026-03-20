<!--
组件名称：GroupChatHeader
用途：群聊页面头部导航
页面专用：仅群聊页面使用
创建时间：2026-03-20
-->
<template>
  <view class="group-chat-header">
    <view class="header-left" @tap="onBack">
      <uni-icons type="back" size="22" color="#333" />
    </view>
    <view class="header-center" @tap="onShowGroupDetail">
      <image class="group-avatar" :src="groupInfo.avatar" mode="aspectFill" />
      <view class="group-info">
        <text class="group-name">{{ groupInfo.name }}</text>
        <text class="group-members">({{ groupInfo.memberCount }}人)</text>
      </view>
    </view>
    <view class="header-right" @tap="onShowMoreMenu">
      <uni-icons type="more" size="22" color="#333" />
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  groupInfo: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['back', 'show-detail', 'show-more'])

const onBack = () => {
  emit('back')
}

const onShowGroupDetail = () => {
  emit('show-detail')
}

const onShowMoreMenu = () => {
  emit('show-more')
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.group-chat-header {
  background: #fff;
  padding: 20rpx 30rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1rpx solid #eee;
}

.header-center {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.group-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 12rpx;
}

.group-info {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.group-name {
  font-size: 30rpx;
  font-weight: 500;
  color: #333;
}

.group-members {
  font-size: 22rpx;
  color: #999;
}
</style>
