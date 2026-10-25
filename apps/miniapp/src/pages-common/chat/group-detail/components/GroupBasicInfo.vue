<!--
组件名称：GroupBasicInfo
用途：群基本信息展示
页面专用：仅群详情页面使用
创建时间：2026-03-20
-->
<template>
  <view class="group-basic-info">
    <view class="group-basic">
      <image class="group-avatar" :src="groupInfo.avatar" mode="aspectFill" />
      <view class="group-text">
        <text class="group-name">{{ groupInfo.name }}</text>
        <text class="group-meta">{{ groupInfo.memberCount }}人 · 群号：{{ groupInfo.groupNo }}</text>
      </view>
      <button class="qrcode-btn" @tap="$emit('show-qrcode')">
        <uni-icons type="qrcode" size="18" color="#FF6B35" />
      </button>
    </view>

    <!-- 群公告 -->
    <view class="group-notice-section" v-if="groupInfo.notice" @tap="$emit('edit-notice')">
      <view class="notice-header">
        <uni-icons type="notification" size="16" color="#FF6B35" />
        <text class="notice-title">群公告</text>
      </view>
      <text class="notice-content">{{ groupInfo.notice }}</text>
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

defineEmits(['show-qrcode', 'edit-notice'])
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.group-basic-info {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.group-basic {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.group-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 16rpx;
}

.group-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.group-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.group-meta {
  font-size: 24rpx;
  color: #999;
}

.qrcode-btn {
  width: 60rpx;
  height: 60rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  border: none;
  @include flex-center;
}

.group-notice-section {
  margin-top: 30rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #eee;
}

.notice-header {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 15rpx;
}

.notice-title {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
}

.notice-content {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}
</style>
