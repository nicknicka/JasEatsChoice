<template>
  <view class="message-page">
    <view class="header">消息中心</view>
    <view class="tabs">
      <view class="tab" :class="{active: currentTab === 'chat'}" @tap="switchTab('chat')">会话</view>
      <view class="tab" :class="{active: currentTab === 'notice'}" @tap="switchTab('notice')">通知</view>
    </view>

    <view v-if="currentTab === 'chat'" class="tab-content">
      <view class="list-item" v-for="item in chatList" :key="item.id" @tap="openChat(item)">
        <view class="avatar">{{ item.avatar }}</view>
        <view class="info">
          <view class="name">{{ item.name }}</view>
          <view class="last-msg">{{ item.lastMsg }}</view>
        </view>
        <view class="time">{{ item.time }}</view>
      </view>
    </view>

    <view v-if="currentTab === 'notice'" class="tab-content">
      <view class="list-item" v-for="item in noticeList" :key="item.id" @tap="openNotice(item)">
        <view class="icon">{{ item.icon }}</view>
        <view class="info">
          <view class="name">{{ item.title }}</view>
          <view class="last-msg">{{ item.content }}</view>
        </view>
        <view class="time">{{ item.time }}</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const currentTab = ref('chat')

const chatList = ref([
  { id: 1, avatar: '老', name: '老王家常菜', lastMsg: '订单已经准备好了', time: '10分钟前' },
  { id: 2, avatar: '美', name: '美食交流群', lastMsg: '今天的菜品很不错', time: '1小时前' }
])

const noticeList = ref([
  { id: 1, icon: '📦', title: '订单更新', content: '您的订单已发货', time: '10分钟前' },
  { id: 2, icon: '📢', title: '系统通知', content: '优惠券即将到期', time: '1小时前' }
])

const switchTab = (tab) => {
  currentTab.value = tab
}

const openChat = (item) => {
  uni.showToast({ title: '打开聊天', icon: 'none' })
}

const openNotice = (item) => {
  uni.showToast({ title: '查看通知', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.message-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  background: #fff;
  padding: 30rpx;
  text-align: center;
  font-size: 36rpx;
  font-weight: bold;
  border-bottom: 1rpx solid #eee;
}

.tabs {
  display: flex;
  background: #fff;
  border-bottom: 1rpx solid #eee;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 30rpx 0;
  font-size: 28rpx;
  color: #666;
  position: relative;

  &.active {
    color: #FF6B35;
    font-weight: bold;

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 60rpx;
      height: 4rpx;
      background: #FF6B35;
      border-radius: 2rpx;
    }
  }
}

.tab-content {
  padding: 20rpx;
}

.list-item {
  display: flex;
  align-items: center;
  background: #fff;
  padding: 25rpx;
  margin-bottom: 20rpx;
  border-radius: 12rpx;

  &:active {
    background: #f9f9f9;
  }
}

.avatar, .icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 12rpx;
  background: #FF6B35;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  margin-right: 20rpx;
}

.icon {
  background: #f0f0f0;
  color: #333;
}

.info {
  flex: 1;
}

.name {
  font-size: 30rpx;
  color: #333;
  margin-bottom: 8rpx;
}

.last-msg {
  font-size: 26rpx;
  color: #999;
}

.time {
  font-size: 24rpx;
  color: #ccc;
}
</style>
