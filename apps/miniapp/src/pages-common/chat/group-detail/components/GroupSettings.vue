<!--
组件名称：GroupSettings
用途：群设置项列表
页面专用：仅群详情页面使用
创建时间：2026-03-20
-->
<template>
  <view class="group-settings">
    <!-- 免打扰 -->
    <view class="setting-item" @tap="$emit('toggle-mute')">
      <view class="setting-left">
        <uni-icons type="sound" size="20" color="#666" />
        <text class="setting-label">消息免打扰</text>
      </view>
      <switch
        :checked="groupInfo.isMuted"
        color="#FF6B35"
        @change="$emit('mute-change', $event)"
      />
    </view>

    <!-- 置顶聊天 -->
    <view class="setting-item" @tap="$emit('toggle-pin')">
      <view class="setting-left">
        <uni-icons type="star" size="20" color="#666" />
        <text class="setting-label">置顶聊天</text>
      </view>
      <switch
        :checked="groupInfo.isPinned"
        color="#FF6B35"
        @change="$emit('pin-change', $event)"
      />
    </view>

    <!-- 群订单 -->
    <view class="setting-item" @tap="$emit('view-group-order')">
      <view class="setting-left">
        <uni-icons type="shop" size="20" color="#666" />
        <text class="setting-label">群订单</text>
      </view>
      <uni-icons type="arrowright" size="16" color="#999" />
    </view>

    <!-- 群名称 -->
    <view class="setting-item" @tap="$emit('edit-name')">
      <view class="setting-left">
        <uni-icons type="compose" size="20" color="#666" />
        <text class="setting-label">群名称</text>
      </view>
      <view class="setting-right">
        <text class="setting-value">{{ groupInfo.name }}</text>
        <uni-icons type="arrowright" size="16" color="#999" />
      </view>
    </view>

    <!-- 群头像 -->
    <view class="setting-item" @tap="$emit('edit-avatar')">
      <view class="setting-left">
        <uni-icons type="image" size="20" color="#666" />
        <text class="setting-label">群头像</text>
      </view>
      <uni-icons type="arrowright" size="16" color="#999" />
    </view>

    <!-- 聊天记录 -->
    <view class="setting-item" @tap="$emit('view-history')">
      <view class="setting-left">
        <uni-icons type="list" size="20" color="#666" />
        <text class="setting-label">聊天记录</text>
      </view>
      <uni-icons type="arrowright" size="16" color="#999" />
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

defineEmits([
  'toggle-mute',
  'mute-change',
  'toggle-pin',
  'pin-change',
  'view-group-order',
  'edit-name',
  'edit-avatar',
  'view-history'
])
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.group-settings {
  background: #fff;
  border-radius: 16rpx;
  padding: 0 30rpx;
  margin-bottom: 20rpx;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx 0;
  border-bottom: 1rpx solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }
}

.setting-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.setting-label {
  font-size: 28rpx;
  color: #333;
}

.setting-right {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.setting-value {
  font-size: 26rpx;
  color: #999;
}
</style>
