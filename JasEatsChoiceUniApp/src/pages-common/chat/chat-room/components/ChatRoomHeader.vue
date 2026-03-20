<!--
组件名称：ChatRoomHeader
用途：聊天室页面头部导航
页面专用：仅聊天室页面使用
创建时间：2026-03-20
-->
<template>
  <view class="chat-room-header">
    <view class="header-left" @tap="onBack">
      <uni-icons type="back" size="22" color="#333" />
    </view>
    <view class="header-center" @tap="onShowUserInfo">
      <image class="user-avatar" :src="userInfo.avatar" mode="aspectFill" />
      <view class="user-info">
        <text class="user-name">{{ userInfo.name }}</text>
        <text class="user-status" :class="{ online: userInfo.isOnline }">
          {{ userInfo.isOnline ? '在线' : '离线' }}
        </text>
      </view>
    </view>
    <view class="header-right" @tap="onShowMoreMenu">
      <uni-icons type="more" size="22" color="#333" />
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  userInfo: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['back', 'show-user-info', 'show-more'])

const onBack = () => {
  emit('back')
}

const onShowUserInfo = () => {
  emit('show-user-info')
}

const onShowMoreMenu = () => {
  emit('show-more')
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.chat-room-header {
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

.user-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.user-name {
  font-size: 30rpx;
  font-weight: 500;
  color: #333;
}

.user-status {
  font-size: 22rpx;
  color: #999;

  &.online {
    color: #52C41A;
  }
}
</style>
