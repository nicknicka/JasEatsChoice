<template>
  <view class="splash-container">
    <view class="logo-section">
      <view class="logo-placeholder">
        <text class="logo-text">佳</text>
      </view>
      <text class="app-name">佳食宜选</text>
      <text class="app-slogan">智能饮食，健康生活</text>
    </view>
    <view class="loading-section">
      <view class="loading-bar">
        <view class="loading-progress" :style="{ width: progress + '%' }"></view>
      </view>
      <text class="loading-text">{{ loadingText }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { toLogin, toUserHome, toMerchantHome } from '@/utils/router'
import { useUserStore } from '@/store/modules/user'

const progress = ref(0)
const loadingText = ref('加载中...')

const userStore = useUserStore()

onMounted(() => {
  initApp()
})

const initApp = async () => {
  try {
    // 模拟加载进度
    const interval = setInterval(() => {
      progress.value += 5
      if (progress.value >= 100) {
        clearInterval(interval)
      }
    }, 100)

    // 等待至少 2 秒，让用户看到启动页
    await new Promise(resolve => setTimeout(resolve, 2000))

    // 检查登录状态
    const token = uni.getStorageSync('token')
    if (!token) {
      loadingText.value = '请先登录'
      setTimeout(() => {
        toLogin()
      }, 1000)
      return
    }

    // 获取用户信息
    loadingText.value = '获取用户信息...'
    await userStore.fetchUserInfo()

    // 根据角色跳转
    loadingText.value = '准备进入...'
    setTimeout(() => {
      const role = userStore.userInfo?.role
      if (role === 'merchant') {
        toMerchantHome()
      } else {
        toUserHome()
      }
    }, 1000)
  } catch (error) {
    console.error('启动失败:', error)
    loadingText.value = '加载失败，请重试'
    setTimeout(() => {
      toLogin()
    }, 2000)
  }
}
</script>

<style lang="scss" scoped>
.splash-container {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8F6B 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 100rpx;
}

.logo-placeholder {
  width: 200rpx;
  height: 200rpx;
  border-radius: 40rpx;
  background: #fff;
  margin-bottom: 40rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  font-size: 100rpx;
  font-weight: bold;
  color: #FF6B35;
}

.app-name {
  font-size: 56rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 20rpx;
  letter-spacing: 4rpx;
}

.app-slogan {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
}

.loading-section {
  width: 80%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.loading-bar {
  width: 100%;
  height: 8rpx;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 4rpx;
  overflow: hidden;
  margin-bottom: 30rpx;
}

.loading-progress {
  height: 100%;
  background: #fff;
  border-radius: 4rpx;
  transition: width 0.3s ease;
}

.loading-text {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}
</style>
