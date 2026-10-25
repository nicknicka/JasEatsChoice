<!--
组件名称：ChatRoomHeader
用途：聊天室页面头部导航
页面专用：仅聊天室页面使用
创建时间：2026-03-20
-->
<template>
  <view class="chat-room-header">
    <view class="header-main">
      <view class="header-left" @tap="onBack">
        <view class="nav-btn">
          <uni-icons type="back" size="20" color="#2F241E" />
        </view>
      </view>
      <view class="header-center" @tap="onShowUserInfo">
        <image class="user-avatar" :src="userInfo.avatar" mode="aspectFill" />
        <view class="user-info">
          <view class="name-row">
            <text class="user-name">{{ userInfo.name }}</text>
            <view class="status-dot" :class="{ online: userInfo.isOnline }"></view>
          </view>
          <text class="user-status">
            {{ userInfo.isOnline ? '正在沟通中' : '暂时离线' }}
          </text>
        </view>
      </view>
      <view class="header-right" @tap="onShowMoreMenu">
        <view class="nav-btn ghost">
          <uni-icons type="more" size="18" color="#2F241E" />
        </view>
      </view>
    </view>
    <view class="header-summary" v-if="userInfo.name">
      <text class="summary-text">
        今日沟通更顺畅，支持发送图片、菜品和订单卡片
      </text>
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
  background: rgba(255, 252, 248, 0.92);
  backdrop-filter: blur(18rpx);
  padding: calc(18rpx + env(safe-area-inset-top)) 24rpx 18rpx;
  border-bottom: 1rpx solid rgba(255, 107, 53, 0.08);
  box-shadow: 0 10rpx 30rpx rgba(123, 86, 53, 0.05);
}

.header-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.header-left,
.header-right {
  flex-shrink: 0;
}

.nav-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 8rpx 18rpx rgba(123, 86, 53, 0.08);
  @include flex-center;

  &.ghost {
    background: rgba(255, 255, 255, 0.7);
  }
}

.header-center {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 18rpx;
  padding: 10rpx 18rpx;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.78);
}

.user-avatar {
  width: 76rpx;
  height: 76rpx;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 20rpx rgba(255, 107, 53, 0.18);
}

.user-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.user-name {
  @include text-ellipsis;
  font-size: 30rpx;
  font-weight: 700;
  color: #2F241E;
}

.user-status {
  @include text-ellipsis;
  font-size: 22rpx;
  color: #8A7568;
}

.status-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: #D9D9D9;
  box-shadow: 0 0 0 8rpx rgba(217, 217, 217, 0.22);

  &.online {
    background: #52C41A;
    box-shadow: 0 0 0 8rpx rgba(82, 196, 26, 0.18);
  }
}

.header-summary {
  margin-top: 16rpx;
  padding: 14rpx 18rpx;
  border-radius: 22rpx;
  background: rgba(255, 107, 53, 0.08);
}

.summary-text {
  font-size: 22rpx;
  color: #A05F44;
  line-height: 1.5;
}
</style>
