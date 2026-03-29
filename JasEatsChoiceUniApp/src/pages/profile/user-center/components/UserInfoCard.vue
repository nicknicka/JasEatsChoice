<template>
  <view class="user-info-card clickable" @click="handleEdit">
    <!-- 头像区域 -->
    <view class="user-avatar">
      <image
        class="avatar-image"
        :src="displayAvatar"
        mode="aspectFill"
        @error="handleAvatarError"
      />
      <!-- VIP徽章 -->
      <view class="avatar-badge" v-if="userInfo.vipLevel > 0">
        <text class="badge-text">VIP{{ userInfo.vipLevel }}</text>
      </view>
    </view>

    <!-- 用户详细信息 -->
    <view class="user-detail">
      <!-- 用户名和性别 -->
      <view class="user-name-row">
        <text class="user-name">{{ displayName }}</text>
        <text class="gender-icon">{{ genderIcon }}</text>
      </view>

      <!-- 用户ID -->
      <text class="user-id">ID: {{ displayId }}</text>

      <!-- 用户标签 -->
      <view class="user-tags" v-if="userInfo.tags && userInfo.tags.length > 0">
        <text class="tag-item" v-for="tag in userInfo.tags" :key="tag">
          {{ tag }}
        </text>
      </view>
    </view>

    <!-- 编辑按钮 -->
    <view class="edit-btn">
      <uni-icons type="compose" size="20" color="#FFFFFF"></uni-icons>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store'

// Props
const props = defineProps({
  userInfo: {
    type: Object,
    default: () => ({
      id: '',
      name: '佳食宜选用户',
      avatar: '',
      gender: 'female',
      tags: [],
      vipLevel: 0
    })
  }
})

// Emits
const emit = defineEmits(['edit'])

// Store
const userStore = useUserStore()

// 计算属性：显示的头像URL
const displayAvatar = computed(() => {
  if (props.userInfo.avatar) {
    return props.userInfo.avatar
  }
  // 默认头像占位图
  return 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=用户'
})

// 计算属性：显示的用户名
const displayName = computed(() => {
  return props.userInfo.name || '佳食宜选用户'
})

// 计算属性：显示的用户ID
const displayId = computed(() => {
  return props.userInfo.id || '未设置'
})

// 计算属性：性别图标
const genderIcon = computed(() => {
  return props.userInfo.gender === 'male' ? '👨' : '👩'
})

/**
 * 头像加载失败处理
 */
const handleAvatarError = (e) => {
  console.warn('头像加载失败:', e)
  // 可以设置一个默认头像
}

/**
 * 点击编辑
 */
const handleEdit = () => {
  emit('edit')
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.user-info-card {
  background-color: $primary-color;  // 纯色背景，不使用渐变
  margin: $spacing-md;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  display: flex;
  align-items: center;
  gap: $spacing-md;
  box-shadow: $box-shadow-md;
  min-height: 160rpx;
}

// 头像区域
.user-avatar {
  position: relative;
  width: 120rpx;
  height: 120rpx;
  flex-shrink: 0;
}

.avatar-image {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
  background-color: $bg-color-base;
}

.avatar-badge {
  position: absolute;
  bottom: -4rpx;
  right: -4rpx;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  padding: 4rpx 8rpx;
  border-radius: $border-radius-round;
  border: 2rpx solid #FFFFFF;
  box-shadow: $box-shadow-sm;

  .badge-text {
    font-size: $font-size-xs;
    color: #FFFFFF;
    font-weight: $font-weight-bold;
  }
}

// 用户详细信息
.user-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  min-width: 0;  // 防止文本溢出
}

.user-name-row {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.user-name {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: #FFFFFF;  // 纯白色，对比度 7.1:1
  max-width: 400rpx;
  @include text-ellipsis;
}

.gender-icon {
  font-size: $font-size-lg;
  flex-shrink: 0;
}

.user-id {
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.9);  // 提高对比度
  @include text-ellipsis;
}

.user-tags {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-xs;
  margin-top: $spacing-xs;
}

.tag-item {
  padding: 4rpx 12rpx;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: $border-radius-round;
  font-size: $font-size-xs;
  color: #FFFFFF;
  backdrop-filter: blur(10px);
}

// 编辑按钮
.edit-btn {
  width: 64rpx;
  height: 64rpx;
  @include flex-center;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: $border-radius-round;
  flex-shrink: 0;
  transition: all 0.3s ease;

  &:active {
    background-color: rgba(255, 255, 255, 0.3);
    transform: scale(0.95);
  }
}
</style>
