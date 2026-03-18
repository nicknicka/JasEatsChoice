<template>
  <view class="login-container">
    <!-- Logo区域 -->
    <view class="logo-section">
      <view class="logo">🍽️</view>
      <view class="app-name">佳食宜选</view>
      <view class="slogan">智能饮食，健康生活</view>
    </view>

    <!-- 登录方式Tab -->
    <view class="login-tabs">
      <view
        class="tab-item"
        :class="{ active: loginType === 'wechat' }"
        @click="loginType = 'wechat'"
      >
        微信登录
      </view>
      <view
        class="tab-item"
        :class="{ active: loginType === 'phone' }"
        @click="loginType = 'phone'"
      >
        手机登录
      </view>
    </view>

    <!-- 微信登录 -->
    <view v-if="loginType === 'wechat'" class="wechat-login">
      <button
        class="wechat-auth-btn"
        open-type="getUserInfo"
        @getuserinfo="handleWechatLogin"
        @click="handleWechatClick"
      >
        <view class="btn-icon">💬</view>
        <view class="btn-text">微信一键登录</view>
      </button>

      <view class="agreement">
        <checkbox-group @change="handleAgreementChange">
          <label class="agreement-label">
            <checkbox value="1" :checked="agreedToTerms" color="#FF6B35" />
            <text>我已阅读并同意</text>
            <text class="link" @click.stop="toTerms">《用户协议》</text>
            <text>和</text>
            <text class="link" @click.stop="toPrivacy">《隐私政策》</text>
          </label>
        </checkbox-group>
      </view>
    </view>

    <!-- 手机登录 -->
    <view v-if="loginType === 'phone'" class="phone-login">
      <view class="input-group">
        <view class="input-item">
          <view class="input-icon">📱</view>
          <input
            type="number"
            v-model="phoneForm.phone"
            placeholder="请输入手机号"
            maxlength="11"
            @input="handlePhoneInput"
          />
        </view>

        <view class="input-item">
          <view class="input-icon">🔐</view>
          <input
            type="number"
            v-model="phoneForm.code"
            placeholder="请输入验证码"
            maxlength="6"
          />
          <button
            class="code-btn"
            :disabled="countdown > 0"
            @click="sendCode"
          >
            {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
          </button>
        </view>
      </view>

      <button
        class="login-btn"
        :disabled="!isPhoneFormValid"
        @click="handlePhoneLogin"
      >
        登录
      </button>
    </view>

    <!-- 底部链接 -->
    <view class="footer-links">
      <text class="link" @click="toRegister">还没有账号？立即注册</text>
    </view>

    <!-- 加载提示 -->
    <view v-if="loading" class="loading-overlay">
      <uni-load-more status="loading" contentText="登录中..."></uni-load-more>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/store'
import { userApi } from '@/api'

// Pinia store
const userStore = useUserStore()

// 登录方式：wechat | phone
const loginType = ref('wechat')

// 是否同意协议
const agreedToTerms = ref(false)

// 加载状态
const loading = ref(false)

// 倒计时
const countdown = ref(0)
let countdownTimer = null

// 手机号表单
const phoneForm = ref({
  phone: '',
  code: ''
})

// 计算属性：手机号表单是否有效
const isPhoneFormValid = computed(() => {
  return phoneForm.value.phone.length === 11 &&
         phoneForm.value.code.length === 6 &&
         agreedToTerms.value
})

/**
 * 处理微信登录点击
 */
const handleWechatClick = () => {
  if (!agreedToTerms.value) {
    uni.showToast({
      title: '请先阅读并同意用户协议和隐私政策',
      icon: 'none'
    })
    return
  }
}

/**
 * 处理微信授权登录
 */
const handleWechatLogin = async (e) => {
  if (!agreedToTerms.value) {
    uni.showToast({
      title: '请先阅读并同意用户协议和隐私政策',
      icon: 'none'
    })
    return
  }

  const { userInfo } = e.detail

  if (!userInfo) {
    uni.showToast({
      title: '需要授权才能登录',
      icon: 'none'
    })
    return
  }

  loading.value = true

  try {
    // 调用后端微信登录接口
    const loginData = {
      nickName: userInfo.nickName,
      avatarUrl: userInfo.avatarUrl,
      gender: userInfo.gender,
      country: userInfo.country,
      province: userInfo.province,
      city: userInfo.city,
      language: userInfo.language
    }

    const res = await userStore.wechatLogin(loginData)

    uni.showToast({
      title: '登录成功',
      icon: 'success'
    })

    // 跳转到首页
    setTimeout(() => {
      uni.switchTab({
        url: '/pages-user/home/index'
      })
    }, 1500)

  } catch (error) {
    console.error('微信登录失败:', error)
    uni.showToast({
      title: error.message || '登录失败，请重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 处理手机号输入
 */
const handlePhoneInput = (e) => {
  const value = e.detail.value
  if (value.length > 11) {
    phoneForm.value.phone = value.substring(0, 11)
  }
}

/**
 * 发送验证码
 */
const sendCode = async () => {
  if (phoneForm.value.phone.length !== 11) {
    uni.showToast({
      title: '请输入正确的手机号',
      icon: 'none'
    })
    return
  }

  // 验证手机号格式
  const phoneReg = /^1[3-9]\d{9}$/
  if (!phoneReg.test(phoneForm.value.phone)) {
    uni.showToast({
      title: '请输入正确的手机号',
      icon: 'none'
    })
    return
  }

  try {
    // 调用后端发送验证码接口
    await userApi.sendCode(phoneForm.value.phone)

    uni.showToast({
      title: '验证码已发送',
      icon: 'success'
    })

    // 开始倒计时
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(countdownTimer)
      }
    }, 1000)

  } catch (error) {
    console.error('发送验证码失败:', error)
    uni.showToast({
      title: error.message || '发送失败，请重试',
      icon: 'none'
    })
  }
}

/**
 * 处理手机号登录
 */
const handlePhoneLogin = async () => {
  if (!isPhoneFormValid.value) {
    return
  }

  loading.value = true

  try {
    // 调用后端登录接口
    await userStore.login({
      phone: phoneForm.value.phone,
      code: phoneForm.value.code
    })

    uni.showToast({
      title: '登录成功',
      icon: 'success'
    })

    // 跳转到首页
    setTimeout(() => {
      uni.switchTab({
        url: '/pages-user/home/index'
      })
    }, 1500)

  } catch (error) {
    console.error('登录失败:', error)
    uni.showToast({
      title: error.message || '登录失败，请重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 处理协议同意
 */
const handleAgreementChange = (e) => {
  agreedToTerms.value = e.detail.value.length > 0
}

/**
 * 跳转到注册页
 */
const toRegister = () => {
  uni.navigateTo({
    url: '/pages/register/index'
  })
}

/**
 * 查看用户协议
 */
const toTerms = () => {
  // TODO: 跳转到用户协议页面
  uni.showToast({
    title: '用户协议',
    icon: 'none'
  })
}

/**
 * 查看隐私政策
 */
const toPrivacy = () => {
  // TODO: 跳转到隐私政策页面
  uni.showToast({
    title: '隐私政策',
    icon: 'none'
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40rpx;
  display: flex;
  flex-direction: column;
}

/* Logo区域 */
.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 80rpx;
  margin-bottom: 60rpx;
}

.logo {
  font-size: 120rpx;
  margin-bottom: 20rpx;
}

.app-name {
  font-size: 48rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 10rpx;
}

.slogan {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* Tab切换 */
.login-tabs {
  display: flex;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 50rpx;
  padding: 6rpx;
  margin-bottom: 60rpx;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 20rpx;
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.6);
  border-radius: 50rpx;
  transition: all 0.3s;
}

.tab-item.active {
  background: #fff;
  color: #667eea;
  font-weight: 500;
}

/* 微信登录 */
.wechat-login {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 40rpx;
}

.wechat-auth-btn {
  width: 500rpx;
  height: 90rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 45rpx;
  font-size: 28rpx;
  color: #667eea;
  border: none;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.btn-icon {
  font-size: 36rpx;
  margin-right: 10rpx;
}

.agreement {
  margin-top: 20rpx;
}

.agreement-label {
  display: flex;
  align-items: center;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.agreement-label text {
  margin-left: 10rpx;
}

.link {
  color: #FFD700;
  text-decoration: underline;
}

/* 手机登录 */
.phone-login {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.input-item {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 50rpx;
  padding: 0 40rpx;
  height: 90rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.input-icon {
  font-size: 36rpx;
  margin-right: 20rpx;
}

.input-item input {
  flex: 1;
  font-size: 28rpx;
  height: 100%;
}

.code-btn {
  padding: 0 30rpx;
  font-size: 24rpx;
  color: #FF6B35;
  background: transparent;
  border: none;
  border-left: 1rpx solid #eee;
}

.code-btn:disabled {
  color: #999;
}

.login-btn {
  height: 90rpx;
  line-height: 90rpx;
  text-align: center;
  background: #fff;
  color: #667eea;
  font-size: 32rpx;
  font-weight: 500;
  border-radius: 50rpx;
  border: none;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.login-btn:disabled {
  opacity: 0.6;
}

/* 底部链接 */
.footer-links {
  margin-top: auto;
  padding: 40rpx 0;
  text-align: center;
}

.footer-links .link {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* 加载遮罩 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
</style>
