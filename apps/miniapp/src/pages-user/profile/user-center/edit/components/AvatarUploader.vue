<template>
  <view class="avatar-uploader">
    <view class="avatar-wrapper" @click="chooseAvatar">
      <image class="avatar-image" :src="avatar || '/static/default-avatar.png'" mode="aspectFill" />
      <view class="avatar-edit">
        <text class="edit-icon">📷</text>
        <text class="edit-text">更换头像</text>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  // 头像URL
  avatar: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['change'])

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
      emit('change', tempFilePath)
    }
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.avatar-uploader {
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
</style>
