<!--
组件名称：GroupMembers
用途：群成员列表展示
页面专用：仅群详情页面使用
创建时间：2026-03-20
-->
<template>
  <view class="group-members">
    <view class="section-header">
      <text class="section-title">群成员</text>
      <text class="section-action" @tap="$emit('invite')">邀请</text>
    </view>

    <scroll-view class="members-list" scroll-x>
      <!-- 邀请按钮 -->
      <view class="member-item" @tap="$emit('invite')">
        <view class="add-avatar">
          <uni-icons type="plus" size="20" color="#FF6B35" />
        </view>
        <text class="member-name">邀请</text>
      </view>

      <!-- 成员列表 -->
      <view
        class="member-item"
        v-for="member in members"
        :key="member.id"
        @tap="$emit('member-tap', member)"
        @longpress="$emit('member-longpress', member)"
      >
        <view class="member-avatar-wrapper">
          <image class="member-avatar" :src="member.avatar" mode="aspectFill" />
          <view class="owner-badge" v-if="member.role === 'owner'">
            <uni-icons type="star-filled" size="10" color="#FFA500" />
          </view>
          <view class="admin-badge" v-if="member.role === 'admin'">
            <uni-icons type="gear-filled" size="10" color="#52C41A" />
          </view>
        </view>
        <text class="member-name">{{ member.name }}</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
const props = defineProps({
  members: {
    type: Array,
    default: () => []
  }
})

defineEmits(['invite', 'member-tap', 'member-longpress'])
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.group-members {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.section-action {
  font-size: 26rpx;
  color: #FF6B35;
}

.members-list {
  display: flex;
  white-space: nowrap;
}

.member-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  margin-right: 30rpx;

  &:last-child {
    margin-right: 0;
  }
}

.add-avatar {
  width: 80rpx;
  height: 80rpx;
  background: #F5F5F5;
  border-radius: 50%;
  @include flex-center;
}

.member-avatar-wrapper {
  position: relative;
}

.member-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
}

.owner-badge,
.admin-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 24rpx;
  height: 24rpx;
  background: #fff;
  border-radius: 50%;
  @include flex-center;
}

.member-name {
  font-size: 22rpx;
  color: #666;
  max-width: 100rpx;
  @include text-ellipsis;
}
</style>
