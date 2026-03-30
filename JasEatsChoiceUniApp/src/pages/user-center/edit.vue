<template>
  <view class="edit-profile-container">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <view class="nav-title">编辑资料</view>
      <view class="nav-save" @click="saveProfile">
        <text class="save-text">保存</text>
      </view>
    </view>

    <!-- 头像区域 -->
    <view class="avatar-section">
      <view class="avatar-wrapper" @click="chooseAvatar">
        <image class="avatar-image" :src="formData.avatar" mode="aspectFill" />
        <view class="avatar-edit-mask">
          <text class="edit-icon">📷</text>
          <text class="edit-text">更换头像</text>
        </view>
      </view>
    </view>

    <!-- 表单区域 -->
    <view class="form-section">
      <!-- 昵称 -->
      <view class="form-item">
        <view class="form-label">
          <text class="label-text">昵称</text>
          <text class="required">*</text>
        </view>
        <input
          class="form-input"
          v-model="formData.nickname"
          placeholder="请输入昵称"
          maxlength="20"
          :placeholder-style="'color: #999'"
        />
        <view class="char-count">{{ formData.nickname.length }}/20</view>
      </view>

      <!-- 性别 -->
      <view class="form-item">
        <view class="form-label">
          <text class="label-text">性别</text>
        </view>
        <view class="gender-options">
          <view
            class="gender-option"
            :class="{ active: formData.gender === 'male' }"
            @click="formData.gender = 'male'"
          >
            <text class="gender-icon">👨</text>
            <text class="gender-text">男</text>
          </view>
          <view
            class="gender-option"
            :class="{ active: formData.gender === 'female' }"
            @click="formData.gender = 'female'"
          >
            <text class="gender-icon">👩</text>
            <text class="gender-text">女</text>
          </view>
        </view>
      </view>

      <!-- 个性签名 -->
      <view class="form-item">
        <view class="form-label">
          <text class="label-text">个性签名</text>
        </view>
        <textarea
          class="form-textarea"
          v-model="formData.signature"
          placeholder="介绍一下自己吧..."
          maxlength="100"
          :placeholder-style="'color: #999'"
        />
        <view class="char-count">{{ formData.signature.length }}/100</view>
      </view>

      <!-- 饮食偏好标签 -->
      <view class="form-item">
        <view class="form-label">
          <text class="label-text">饮食偏好</text>
          <text class="label-desc">（选择您喜欢的饮食类型）</text>
        </view>
        <view class="tags-container">
          <view
            class="tag-item"
            v-for="tag in dietPreferenceTags"
            :key="tag"
            :class="{ selected: formData.tags.includes(tag) }"
            @click="toggleTag(tag)"
          >
            {{ tag }}
          </view>
        </view>
      </view>
    </view>

    <!-- 底部提示 -->
    <view class="bottom-tip">
      <text class="tip-text">* 为必填项，修改后请点击保存</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store'
import { userApi } from '@/api'

const userStore = useUserStore()

// 表单数据
const formData = ref({
  nickname: '',
  avatar: '',
  gender: 'female',
  signature: '',
  tags: []
})

// 饮食偏好标签选项
const dietPreferenceTags = [
  '清淡饮食', '麻辣口味', '素食主义', '低糖低脂',
  '高蛋白', '无辣不欢', '甜食控', '海鲜爱好者',
  '家常菜', '地方菜系', '健康饮食', '随心搭配'
])

// 加载用户数据
onMounted(() => {
  if (userStore.userInfo) {
    formData.value = {
      nickname: userStore.userInfo.nickname || '',
      avatar: userStore.userInfo.avatar || 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=用户',
      gender: userStore.userInfo.gender || 'female',
      signature: userStore.userInfo.signature || '',
      tags: userStore.userInfo.tags || []
    }
  }
})

/**
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack()
}

/**
 * 选择头像
 */
const chooseAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0]

      // 显示加载提示
      uni.showLoading({
        title: '上传中...'
      })

      // 转换为base64并上传
      uni.getFileInfo({
        filePath: tempFilePath,
        success: (fileInfo) => {
          const fileManager = uni.getFileSystemManager()
          fileManager.readFile({
            filePath: tempFilePath,
            encoding: 'base64',
            success: (readRes) => {
              const base64 = 'data:image/jpeg;base64,' + readRes.data
              uploadAvatar(base64)
            },
            fail: (error) => {
              console.error('读取文件失败:', error)
              uni.hideLoading()
              uni.showToast({
                title: '读取图片失败',
                icon: 'none'
              })
            }
          })
        },
        fail: () => {
          uni.hideLoading()
          uni.showToast({
            title: '获取文件信息失败',
            icon: 'none'
          })
        }
      })
    }
  })
}

/**
 * 上传头像
 */
const uploadAvatar = async (base64) => {
  try {
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id
    const res = await userApi.uploadAvatar(userId, { avatarBase64: base64 })

    uni.hideLoading()

    if (res && res.avatar) {
      formData.value.avatar = res.avatar
      uni.showToast({
        title: '头像上传成功',
        icon: 'success'
      })
    }
  } catch (error) {
    console.error('上传头像失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error.message || '上传失败',
      icon: 'none'
    })
  }
}

/**
 * 切换标签选择
 */
const toggleTag = (tag) => {
  const index = formData.value.tags.indexOf(tag)
  if (index > -1) {
    formData.value.tags.splice(index, 1)
  } else {
    if (formData.value.tags.length >= 6) {
      uni.showToast({
        title: '最多选择6个标签',
        icon: 'none'
      })
      return
    }
    formData.value.tags.push(tag)
  }
}

/**
 * 保存个人资料
 */
const saveProfile = async () => {
  // 表单验证
  if (!formData.value.nickname.trim()) {
    uni.showToast({
      title: '请输入昵称',
      icon: 'none'
    })
    return
  }

  // 敏感词检测
  const sensitiveWords = ['垃圾', '傻逼', '去死', '杀']
  const hasSensitiveWord = sensitiveWords.some(word =>
    formData.value.nickname.includes(word) ||
    formData.value.signature.includes(word)
  )

  if (hasSensitiveWord) {
    uni.showToast({
      title: '内容包含敏感词',
      icon: 'none'
    })
    return
  }

  try {
    uni.showLoading({
      title: '保存中...'
    })

    const userId = userStore.userInfo?.userId || userStore.userInfo?.id
    const updateData = {
      nickname: formData.value.nickname,
      avatar: formData.value.avatar,
      gender: formData.value.gender,
      signature: formData.value.signature,
      tags: formData.value.tags
    }

    const res = await userApi.updateUserInfo(userId, updateData)

    uni.hideLoading()

    if (res) {
      // 更新store中的用户信息
      userStore.setUserInfo({
        ...userStore.userInfo,
        ...updateData
      })

      // 更新本地存储
      uni.setStorageSync('userInfo', {
        ...userStore.userInfo,
        ...updateData
      })

      uni.showToast({
        title: '保存成功',
        icon: 'success'
      })

      // 延迟返回
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  } catch (error) {
    console.error('保存失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error.message || '保存失败',
      icon: 'none'
    })
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.edit-profile-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  padding-bottom: env(safe-area-inset-bottom);
}

/* 导航栏 */
.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 88rpx;
  background-color: $bg-color-white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 $spacing-md;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.nav-back {
  width: 88rpx;
  height: 88rpx;
  @include flex-center;

  .back-icon {
    font-size: 48rpx;
    color: $text-color-primary;
    font-weight: bold;
  }
}

.nav-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.nav-save {
  width: 88rpx;
  height: 88rpx;
  @include flex-center;

  .save-text {
    font-size: $font-size-base;
    color: $primary-color;
    font-weight: $font-weight-bold;
  }
}

/* 头像区域 */
.avatar-section {
  margin-top: 108rpx;
  padding: $spacing-xl $spacing-md;
  background-color: $bg-color-white;
  margin-bottom: $spacing-md;
}

.avatar-wrapper {
  position: relative;
  width: 200rpx;
  height: 200rpx;
  margin: 0 auto;
  border-radius: 50%;
  overflow: hidden;
  border: 4rpx solid $primary-color;
}

.avatar-image {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.avatar-edit-mask {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 80rpx;
  background-color: rgba(0, 0, 0, 0.6);
  @include flex-center-column;
  gap: 4rpx;

  .edit-icon {
    font-size: 32rpx;
  }

  .edit-text {
    font-size: $font-size-xs;
    color: #fff;
  }
}

/* 表单区域 */
.form-section {
  background-color: $bg-color-white;
  padding: 0 $spacing-md;
}

.form-item {
  padding: $spacing-lg 0;
  border-bottom: 1rpx solid $border-color-lighter;

  &:last-child {
    border-bottom: none;
  }
}

.form-label {
  @include flex-center;
  margin-bottom: $spacing-md;

  .label-text {
    font-size: $font-size-base;
    color: $text-color-primary;
    font-weight: $font-weight-bold;
  }

  .required {
    color: $danger-color;
    margin-left: 4rpx;
  }

  .label-desc {
    font-size: $font-size-sm;
    color: $text-color-secondary;
    margin-left: 8rpx;
    font-weight: normal;
  }
}

.form-input {
  width: 100%;
  padding: $spacing-sm 0;
  font-size: $font-size-base;
  color: $text-color-primary;
  border: none;
  outline: none;
}

.form-textarea {
  width: 100%;
  min-height: 160rpx;
  padding: $spacing-sm 0;
  font-size: $font-size-base;
  color: $text-color-primary;
  border: none;
  outline: none;
}

.char-count {
  text-align: right;
  font-size: $font-size-xs;
  color: $text-color-secondary;
  margin-top: $spacing-xs;
}

/* 性别选择 */
.gender-options {
  @include flex-center;
  gap: $spacing-lg;
}

.gender-option {
  flex: 1;
  height: 160rpx;
  @include flex-center-column;
  gap: $spacing-sm;
  border: 2rpx solid $border-color-base;
  border-radius: $border-radius-lg;
  background-color: $bg-color-base;
  transition: all 0.3s;

  &.active {
    border-color: $primary-color;
    background-color: rgba($primary-color, 0.1);
  }

  .gender-icon {
    font-size: 64rpx;
  }

  .gender-text {
    font-size: $font-size-base;
    color: $text-color-primary;
  }
}

/* 标签选择 */
.tags-container {
  @include flex-center;
  gap: $spacing-sm;
  flex-wrap: wrap;
}

.tag-item {
  padding: $spacing-sm $spacing-md;
  border: 2rpx solid $border-color-base;
  border-radius: $border-radius-round;
  font-size: $font-size-sm;
  color: $text-color-secondary;
  background-color: $bg-color-base;
  transition: all 0.3s;

  &.selected {
    border-color: $primary-color;
    background-color: $primary-color;
    color: #fff;
  }
}

/* 底部提示 */
.bottom-tip {
  padding: $spacing-lg;
  text-align: center;

  .tip-text {
    font-size: $font-size-xs;
    color: $text-color-secondary;
  }
}
</style>
