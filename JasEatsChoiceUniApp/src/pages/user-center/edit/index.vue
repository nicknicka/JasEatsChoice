<template>
  <view class="edit-container">
    <!-- 头像区域 -->
    <view class="avatar-section">
      <view class="avatar-wrapper" @click="chooseAvatar">
        <image class="avatar-image" :src="userInfo.avatar || '/static/default-avatar.png'" mode="aspectFill" />
        <view class="avatar-edit">
          <text class="edit-icon">📷</text>
          <text class="edit-text">更换头像</text>
        </view>
      </view>
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
      <view class="form-item" @click="showGenderPicker = true">
        <text class="form-label">性别</text>
        <view class="form-value">
          <text class="value-text">{{ genderText }}</text>
          <text class="value-arrow">›</text>
        </view>
      </view>

      <!-- 生日 -->
      <view class="form-item" @click="showBirthdayPicker = true">
        <text class="form-label">生日</text>
        <view class="form-value">
          <text class="value-text">{{ userInfo.birthday || '请选择生日' }}</text>
          <text class="value-arrow">›</text>
        </view>
      </view>

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
      <view class="form-item" @click="showTastePicker = true">
        <text class="form-label">口味偏好</text>
        <view class="form-value">
          <text class="value-text">{{ tasteText || '请选择' }}</text>
          <text class="value-arrow">›</text>
        </view>
      </view>

      <!-- 过敏原 -->
      <view class="form-item" @click="showAllergyPicker = true">
        <text class="form-label">过敏原</text>
        <view class="form-value">
          <text class="value-text">{{ allergyText || '无' }}</text>
          <text class="value-arrow">›</text>
        </view>
      </view>

      <!-- 饮食目标 -->
      <view class="form-item" @click="showGoalPicker = true">
        <text class="form-label">饮食目标</text>
        <view class="form-value">
          <text class="value-text">{{ goalText || '请选择' }}</text>
          <text class="value-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="bottom-bar">
      <button class="save-btn" @click="saveUserInfo">保存修改</button>
    </view>

    <!-- 性别选择器 -->
    <picker
      v-if="showGenderPicker"
      mode="selector"
      :range="genderOptions"
      :value="genderIndex"
      @change="onGenderChange"
      @cancel="showGenderPicker = false"
    >
      <view></view>
    </picker>

    <!-- 生日选择器 -->
    <picker
      v-if="showBirthdayPicker"
      mode="date"
      :value="userInfo.birthday"
      :end="currentDate"
      @change="onBirthdayChange"
      @cancel="showBirthdayPicker = false"
    >
      <view></view>
    </picker>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
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

// 选择器显示状态
const showGenderPicker = ref(false)
const showBirthdayPicker = ref(false)
const showTastePicker = ref(false)
const showAllergyPicker = ref(false)
const showGoalPicker = ref(false)

// 性别选项
const genderOptions = ['保密', '男', '女']
const genderIndex = ref(0)

// 口味偏好选项
const tasteOptions = [
  { value: 'spicy', label: '辣味' },
  { value: 'sweet', label: '甜味' },
  { value: 'sour', label: '酸味' },
  { value: 'salty', label: '咸鲜' },
  { value: 'light', label: '清淡' }
]
const selectedTastes = ref([])

// 过敏原选项
const allergyOptions = [
  { value: 'seafood', label: '海鲜' },
  { value: 'peanut', label: '花生' },
  { value: 'milk', label: '牛奶' },
  { value: 'egg', label: '鸡蛋' },
  { value: 'gluten', label: '麸质' },
  { value: 'soy', label: '大豆' }
]
const selectedAllergies = ref([])

// 饮食目标选项
const goalOptions = [
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

// 性别文本
const genderText = computed(() => {
  return genderOptions[userInfo.value.gender] || '保密'
})

// 口味文本
const tasteText = computed(() => {
  if (!userInfo.value.taste || userInfo.value.taste.length === 0) return ''
  return userInfo.value.taste.map(value => {
    const option = tasteOptions.find(opt => opt.value === value)
    return option ? option.label : value
  }).join('、')
})

// 过敏原文文
const allergyText = computed(() => {
  if (!userInfo.value.allergies || userInfo.value.allergies.length === 0) return ''
  return userInfo.value.allergies.map(value => {
    const option = allergyOptions.find(opt => opt.value === value)
    return option ? option.label : value
  }).join('、')
})

// 目标文本
const goalText = computed(() => {
  if (!userInfo.value.goal) return ''
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

    // 设置性别索引
    genderIndex.value = userInfo.value.gender || 0

    // 设置选择的口味和过敏原
    selectedTastes.value = userInfo.value.taste || []
    selectedAllergies.value = userInfo.value.allergies || []
  } catch (error) {
    console.error('加载用户信息失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

/**
 * 选择头像
 */
const chooseAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const tempFilePath = res.tempFilePaths[0]

      try {
        // TODO: 上传头像到服务器
        // const uploadRes = await api.user.uploadAvatar(tempFilePath)
        // userInfo.value.avatar = uploadRes.data.url

        // 临时预览
        userInfo.value.avatar = tempFilePath

        uni.showToast({
          title: '头像上传功能开发中',
          icon: 'none'
        })
      } catch (error) {
        console.error('上传头像失败:', error)
        uni.showToast({
          title: '上传失败',
          icon: 'none'
        })
      }
    }
  })
}

/**
 * 性别选择
 */
const onGenderChange = (e) => {
  genderIndex.value = e.detail.value
  userInfo.value.gender = parseInt(e.detail.value)
  showGenderPicker.value = false
}

/**
 * 生日选择
 */
const onBirthdayChange = (e) => {
  userInfo.value.birthday = e.detail.value
  showBirthdayPicker.value = false
}

/**
 * 口味偏好选择
 */
const onTasteChange = () => {
  // TODO: 实现多选口味偏好
  uni.showActionSheet({
    itemList: tasteOptions.map(opt => opt.label),
    success: (res) => {
      const selected = tasteOptions[res.tapIndex]
      if (!userInfo.value.taste) {
        userInfo.value.taste = []
      }
      const index = userInfo.value.taste.indexOf(selected.value)
      if (index > -1) {
        userInfo.value.taste.splice(index, 1)
      } else {
        userInfo.value.taste.push(selected.value)
      }
    }
  })
  showTastePicker.value = false
}

/**
 * 过敏原选择
 */
const onAllergyChange = () => {
  // TODO: 实现多选过敏原
  uni.showActionSheet({
    itemList: allergyOptions.map(opt => opt.label),
    success: (res) => {
      const selected = allergyOptions[res.tapIndex]
      if (!userInfo.value.allergies) {
        userInfo.value.allergies = []
      }
      const index = userInfo.value.allergies.indexOf(selected.value)
      if (index > -1) {
        userInfo.value.allergies.splice(index, 1)
      } else {
        userInfo.value.allergies.push(selected.value)
      }
    }
  })
  showAllergyPicker.value = false
}

/**
 * 饮食目标选择
 */
const onGoalChange = (e) => {
  userInfo.value.goal = goalOptions[e.detail.value].value
  showGoalPicker.value = false
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

.avatar-wrapper {
  position: relative;
  width: 200rpx;
  height: 200rpx;

  &:active {
    opacity: 0.8;
  }
}

.avatar-image {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 4rpx solid $border-color-light;
}

.avatar-edit {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60rpx;
  background-color: rgba(0, 0, 0, 0.6);
  border-radius: 0 0 100rpx 100rpx;
  @include flex-center-column;
  gap: 4rpx;
}

.edit-icon {
  font-size: $font-size-base;
}

.edit-text {
  font-size: $font-size-xs;
  color: #fff;
}

/* 表单区域 */
.form-section {
  background-color: $bg-color-white;
  margin-top: $spacing-md;
  padding: 0 $spacing-md;
}

.section-title {
  padding: $spacing-lg 0;
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  border-bottom: 1rpx solid $border-color-lighter;
}

.form-item {
  @include flex-between;
  align-items: center;
  padding: $spacing-lg 0;
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
  text-align: right;
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
