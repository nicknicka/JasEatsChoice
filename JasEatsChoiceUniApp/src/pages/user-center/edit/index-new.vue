<template>
  <view class="edit-container">
    <!-- 头像区域 -->
    <view class="avatar-section">
      <AvatarUploader :avatar="userInfo.avatar" @change="handleAvatarChange" />
    </view>

    <!-- 基本信息 -->
    <view class="form-section">
      <view class="section-title">基本信息</view>

      <!-- 昵称 -->
      <view class="form-item">
        <text class="form-label">昵称</text>
        <input
          class="form-input"
          v-model="userInfo.nickname"
          placeholder="请输入昵称"
          maxlength="20"
        />
      </view>

      <!-- 性别 -->
      <GenderPicker v-model="userInfo.gender" />

      <!-- 生日 -->
      <picker
        mode="date"
        :value="userInfo.birthday"
        :end="currentDate"
        @change="handleBirthdayChange"
      >
        <view class="form-item">
          <text class="form-label">生日</text>
          <view class="form-value">
            <text class="value-text">{{ userInfo.birthday || '请选择生日' }}</text>
            <text class="value-arrow">›</text>
          </view>
        </view>
      </picker>

      <!-- 个性签名 -->
      <view class="form-item textarea-item">
        <text class="form-label">个性签名</text>
        <textarea
          class="form-textarea"
          v-model="userInfo.bio"
          placeholder="介绍一下自己吧"
          maxlength="100"
          :show-confirm-bar="false"
        />
        <text class="char-count">{{ (userInfo.bio || '').length }}/100</text>
      </view>
    </view>

    <!-- 联系方式 -->
    <view class="form-section">
      <view class="section-title">联系方式</view>

      <!-- 手机号 -->
      <view class="form-item">
        <text class="form-label">手机号</text>
        <view class="form-value">
          <text class="value-text">{{ userInfo.phone || '未绑定' }}</text>
          <text class="value-link" @click.stop="bindPhone">{{ userInfo.phone ? '更换' : '绑定' }}</text>
        </view>
      </view>

      <!-- 邮箱 -->
      <view class="form-item">
        <text class="form-label">邮箱</text>
        <input
          class="form-input"
          v-model="userInfo.email"
          placeholder="请输入邮箱"
        />
      </view>
    </view>

    <!-- 饮食偏好 -->
    <view class="form-section">
      <view class="section-title">饮食偏好</view>

      <!-- 口味偏好 -->
      <TasteSelector v-model="userInfo.taste" />

      <!-- 过敏原 -->
      <AllergySelector v-model="userInfo.allergies" />

      <!-- 饮食目标 -->
      <picker
        mode="selector"
        :range="goalOptions"
        :value="goalIndex"
        @change="handleGoalChange"
      >
        <view class="form-item">
          <text class="form-label">饮食目标</text>
          <view class="form-value">
            <text class="value-text">{{ goalText || '请选择' }}</text>
            <text class="value-arrow">›</text>
          </view>
        </view>
      </picker>
    </view>

    <!-- 底部按钮 -->
    <view class="bottom-bar">
      <button class="save-btn" @click="saveUserInfo">保存修改</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import AvatarUploader from './components/AvatarUploader.vue'
import GenderPicker from './components/GenderPicker.vue'
import TasteSelector from './components/TasteSelector.vue'
import AllergySelector from './components/AllergySelector.vue'
import api from '@/api'

// 用户信息
const userInfo = ref({
  avatar: '',
  nickname: '',
  gender: 0,
  birthday: '',
  bio: '',
  phone: '',
  email: '',
  taste: [],
  allergies: [],
  goal: ''
})

// 饮食目标选项
const goalOptions = [
  { value: '', label: '请选择' },
  { value: 'lose_weight', label: '减脂' },
  { value: 'gain_muscle', label: '增肌' },
  { value: 'keep_fit', label: '保持健康' },
  { value: 'no_goal', label: '无特殊目标' }
]

// 当前日期
const currentDate = computed(() => {
  const date = new Date()
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
})

// 目标索引
const goalIndex = computed(() => {
  const index = goalOptions.findIndex(opt => opt.value === userInfo.value.goal)
  return index > -1 ? index : 0
})

// 目标文本
const goalText = computed(() => {
  const option = goalOptions.find(opt => opt.value === userInfo.value.goal)
  return option ? option.label : ''
})

/**
 * 加载用户信息
 */
const loadUserInfo = async () => {
  try {
    const res = await api.user.getUserInfo()
    userInfo.value = {
      ...userInfo.value,
      ...res.data
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

/**
 * 头像更改
 */
const handleAvatarChange = (tempFilePath) => {
  // TODO: 上传头像到服务器
  userInfo.value.avatar = tempFilePath

  uni.showToast({
    title: '头像上传功能开发中',
    icon: 'none'
  })
}

/**
 * 生日选择
 */
const handleBirthdayChange = (e) => {
  userInfo.value.birthday = e.detail.value
}

/**
 * 目标选择
 */
const handleGoalChange = (e) => {
  userInfo.value.goal = goalOptions[e.detail.value].value
}

/**
 * 绑定手机号
 */
const bindPhone = () => {
  uni.navigateTo({
    url: '/pages/user-center/bind-phone/index'
  })
}

/**
 * 保存用户信息
 */
const saveUserInfo = async () => {
  // 验证昵称
  if (!userInfo.value.nickname || userInfo.value.nickname.trim() === '') {
    uni.showToast({
      title: '请输入昵称',
      icon: 'none'
    })
    return
  }

  // 验证邮箱格式
  if (userInfo.value.email && !isValidEmail(userInfo.value.email)) {
    uni.showToast({
      title: '邮箱格式不正确',
      icon: 'none'
    })
    return
  }

  try {
    await api.user.updateUserInfo(userInfo.value)

    uni.showToast({
      title: '保存成功',
      icon: 'success'
    })

    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (error) {
    console.error('保存失败:', error)
    uni.showToast({
      title: '保存失败，请重试',
      icon: 'none'
    })
  }
}

/**
 * 验证邮箱格式
 */
function isValidEmail(email) {
  const reg = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/
  return reg.test(email)
}

// 组件挂载
onMounted(() => {
  loadUserInfo()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.edit-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  padding-bottom: 120rpx;
}

/* 头像区域 */
.avatar-section {
  background-color: $bg-color-white;
  padding: $spacing-xl;
  @include flex-center;
}

/* 表单区域 */
.form-section {
  background-color: $bg-color-white;
  margin-top: $spacing-md;
  padding: 0 $spacing-md;
}

.section-title {
  padding: $spacing-lg $spacing-md;
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  border-bottom: 1rpx solid $border-color-lighter;
}

.form-item {
  @include flex-between;
  align-items: center;
  padding: $spacing-lg $spacing-md;
  border-bottom: 1rpx solid $border-color-lighter;
  position: relative;

  &:last-child {
    border-bottom: none;
  }

  &.textarea-item {
    flex-direction: column;
    align-items: flex-start;
  }
}

.form-label {
  width: 160rpx;
  font-size: $font-size-base;
  color: $text-color-primary;
  flex-shrink: 0;
}

.form-input {
  flex: 1;
  text-align: right;
  font-size: $font-size-base;
  color: $text-color-primary;
}

.form-value {
  flex: 1;
  @include flex-center;
  justify-content: flex-end;
  gap: $spacing-sm;
}

.value-text {
  font-size: $font-size-base;
  color: $text-color-primary;
}

.value-arrow {
  font-size: $font-size-xl;
  color: $text-color-placeholder;
}

.value-link {
  font-size: $font-size-sm;
  color: $primary-color;
  padding: $spacing-xs $spacing-md;
  background-color: rgba(255, 107, 53, 0.1);
  border-radius: $border-radius-round;

  &:active {
    opacity: 0.6;
  }
}

.form-textarea {
  width: 100%;
  min-height: 160rpx;
  padding: $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  font-size: $font-size-base;
  color: $text-color-primary;
  line-height: $line-height-lg;
  margin-top: $spacing-sm;
}

.char-count {
  position: absolute;
  bottom: $spacing-md;
  right: $spacing-md;
  font-size: $font-size-xs;
  color: $text-color-placeholder;
}

/* 底部按钮 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: $bg-color-white;
  padding: $spacing-md;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
  z-index: $z-index-fixed;
  @include safe-area-bottom;
}

.save-btn {
  width: 100%;
  height: 88rpx;
  @include flex-center;
  background: linear-gradient(135deg, $primary-color, #FF8F61);
  color: #fff;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  border-radius: $border-radius-round;
  border: none;

  &:active {
    transform: scale(0.98);
  }
}
</style>
