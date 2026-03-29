<template>
  <view class="login-container">
    <!-- Logo区域 -->
    <view class="logo-section">
      <view class="logo">
        <uni-icons type="shop" size="60" color="#fff"></uni-icons>
      </view>
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
        验证码登录
      </view>
      <view
        class="tab-item"
        :class="{ active: loginType === 'password' }"
        @click="loginType = 'password'"
      >
        密码登录
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
        <uni-icons type="weixin" size="20" color="#667eea"></uni-icons>
        <view class="btn-text">微信一键登录</view>
      </button>
    </view>

    <!-- 手机登录 -->
    <view v-if="loginType === 'phone'" class="phone-login">
      <view class="input-group">
        <view class="input-item-wrapper">
          <view class="input-item">
            <uni-icons type="phone" size="20" color="#999"></uni-icons>
            <input
              type="number"
              v-model="phoneForm.phone"
              placeholder="请输入手机号"
              maxlength="11"
              @input="handlePhoneInput"
              @blur="validatePhoneLogin"
            />
            <uni-icons
              v-if="phoneForm.phone"
              type="clear"
              size="18"
              color="#999"
              class="clear-icon"
              @click="phoneForm.phone = ''"
            ></uni-icons>
          </view>
          <view v-if="phoneFormErrors.phone" class="input-error-tip">
            {{ phoneFormErrors.phone }}
          </view>
        </view>

        <view class="input-item-wrapper">
          <view class="input-item">
            <uni-icons type="locked" size="20" color="#999"></uni-icons>
            <input
              type="number"
              v-model="phoneForm.code"
              placeholder="请输入验证码"
              maxlength="6"
              @blur="validatePhoneCode"
            />
            <button
              class="code-btn"
              :disabled="countdown > 0"
              @click="sendCode"
            >
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </button>
          </view>
          <view v-if="phoneFormErrors.code" class="input-error-tip">
            {{ phoneFormErrors.code }}
          </view>
        </view>
      </view>

      <view class="extra-options">
        <label class="remember-password">
          <checkbox-group @change="handlePhoneRememberChange">
            <label class="checkbox-label">
              <checkbox value="1" :checked="phoneForm.rememberPassword" color="#FF6B35" />
              <text>记住密码</text>
            </label>
          </checkbox-group>
        </label>
      </view>

      <button
        class="login-btn"
        :disabled="!isPhoneFormValid"
        @click="handlePhoneLogin"
      >
        登录
      </button>
    </view>

    <!-- 密码登录 -->
    <view v-if="loginType === 'password'" class="password-login">
      <view class="input-group">
        <view class="input-item-wrapper">
          <view class="input-item autocomplete-item">
            <uni-icons type="phone" size="20" color="#999"></uni-icons>
            <input
              type="number"
              v-model="passwordForm.phone"
              placeholder="请输入手机号"
              maxlength="11"
              @input="handlePasswordPhoneInput"
              @focus="showPhoneHistory"
              @blur="validatePhone"
            />
            <view class="input-right-icons">
              <uni-icons
                v-if="passwordForm.phone"
                type="clear"
                size="16"
                color="#999"
                class="clear-icon"
                @click="passwordForm.phone = ''"
              ></uni-icons>
              <uni-icons
                type="down"
                size="16"
                color="#999"
                class="dropdown-icon"
                @click="togglePhoneHistory"
              ></uni-icons>
            </view>
          </view>
          <view v-if="passwordFormErrors.phone" class="input-error-tip">
            {{ passwordFormErrors.phone }}
          </view>
        </view>

        <!-- 手机号历史记录下拉列表 -->
        <view v-if="showHistoryList" class="history-list">
          <view
            v-for="item in phoneHistory"
            :key="item.phone"
            class="history-item"
            @click="selectPhone(item)"
          >
            <view class="history-phone">{{ item.phone }}</view>
            <uni-icons
              type="clear"
              size="16"
              color="#999"
              class="delete-icon"
              @click.stop="deletePhone(item.phone)"
            ></uni-icons>
          </view>
        </view>

        <view class="input-item-wrapper">
          <view class="input-item">
            <uni-icons type="locked" size="20" color="#999"></uni-icons>
            <input
              :type="showPassword ? 'text' : 'password'"
              v-model="passwordForm.password"
              placeholder="请输入密码（6-20位）"
              maxlength="20"
              @blur="validatePassword"
            />
            <view class="password-toggle" @click="togglePassword">
              <uni-icons
                :type="showPassword ? 'eye-filled' : 'eye'"
                size="20"
                :color="showPassword ? '#FF6B35' : '#999'"
              ></uni-icons>
            </view>
          </view>
          <view v-if="passwordFormErrors.password" class="input-error-tip">
            {{ passwordFormErrors.password }}
          </view>
        </view>

        <view class="input-item-wrapper">
          <view class="input-item captcha-item">
            <uni-icons type="checkmarkempty" size="20" color="#999"></uni-icons>
            <input
              type="text"
              v-model="passwordForm.captcha"
              placeholder="请输入验证码"
              maxlength="4"
              @blur="validateCaptcha"
            />
            <view class="captcha-wrapper">
              <image
                class="captcha-img"
                :src="captchaBase64"
                mode="aspectFit"
                @click="refreshCaptcha"
              />
              <uni-icons
                type="refreshempty"
                size="20"
                color="#FF6B35"
                class="refresh-icon"
                @click="refreshCaptcha"
              ></uni-icons>
            </view>
          </view>
          <view v-if="passwordFormErrors.captcha" class="input-error-tip">
            {{ passwordFormErrors.captcha }}
          </view>
        </view>
      </view>

      <view class="extra-options">
        <label class="remember-password">
          <checkbox-group @change="handlePasswordRememberChange">
            <label class="checkbox-label">
              <checkbox value="1" :checked="passwordForm.rememberPassword" color="#FF6B35" />
              <text>记住密码</text>
            </label>
          </checkbox-group>
        </label>
      </view>

      <view class="extra-links">
        <text class="link" @click="toForgotPassword">忘记密码？</text>
      </view>

      <button
        class="login-btn"
        :disabled="!isPasswordFormValid"
        @click="handlePasswordLogin"
      >
        登录
      </button>
    </view>

    <!-- 协议复选框 -->
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
import { ref, computed, watch, onMounted } from 'vue'
import { useUserStore } from '@/store'
import { userApi } from '@/api'

// Pinia store
const userStore = useUserStore()

// 登录方式：wechat | phone | password
const loginType = ref('wechat')

// 监听登录类型变化，当切换到密码登录时获取验证码
watch(loginType, (newType) => {
  if (newType === 'password') {
    refreshCaptcha()
  }
})

// 组件挂载时，如果默认是密码登录，则获取验证码
onMounted(() => {
  loadPhoneHistory()
  console.log(showPassword.value)
  if (loginType.value === 'password') {
    refreshCaptcha()
  }
})

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
  code: '',
  rememberPassword: false
})

// 密码登录表单
const passwordForm = ref({
  phone: '',
  password: '',
  captcha: '',
  checkCodeKey: '',
  rememberPassword: false
})

// 验证码base64图片
const captchaBase64 = ref('')

// 手机号历史记录
const phoneHistory = ref([])
const showHistoryList = ref(false)

// 密码显示/隐藏
const showPassword = ref(false)

// 表单错误提示（每个字段独立）
const passwordFormErrors = ref({
  phone: '',
  password: '',
  captcha: ''
})

// 手机号登录表单错误提示
const phoneFormErrors = ref({
  phone: '',
  code: ''
})

// 计算属性：手机号表单是否有效
const isPhoneFormValid = computed(() => {
  return phoneForm.value.phone.length === 11 &&
         phoneForm.value.code.length > 0 &&
         agreedToTerms.value &&
         !Object.values(phoneFormErrors.value).some(error => error !== '')
})

// 计算属性：密码表单是否有效
const isPasswordFormValid = computed(() => {
  return passwordForm.value.phone.length === 11 &&
         passwordForm.value.password.length >= 6 &&
         passwordForm.value.captcha.length > 0 &&
         agreedToTerms.value &&
         !Object.values(passwordFormErrors.value).some(error => error !== '')
})

/**
 * 验证手机号
 */
const validatePhone = () => {
  if (passwordForm.value.phone.length === 0) {
    passwordFormErrors.value.phone = ''
    return
  }

  if (passwordForm.value.phone.length !== 11) {
    passwordFormErrors.value.phone = '请输入正确的手机号'
    return
  }

  const phoneReg = /^1[3-9]\d{9}$/
  if (!phoneReg.test(passwordForm.value.phone)) {
    passwordFormErrors.value.phone = '手机号格式不正确'
    return
  }

  passwordFormErrors.value.phone = ''
}

/**
 * 验证密码
 */
const validatePassword = () => {
  if (passwordForm.value.password.length === 0) {
    passwordFormErrors.value.password = ''
    return
  }

  if (passwordForm.value.password.length < 6) {
    passwordFormErrors.value.password = '密码至少需要6位'
    return
  }

  if (passwordForm.value.password.length > 20) {
    passwordFormErrors.value.password = '密码最多20位'
    return
  }

  passwordFormErrors.value.password = ''
}

/**
 * 验证验证码
 */
const validateCaptcha = () => {
  if (passwordForm.value.captcha.length === 0) {
    passwordFormErrors.value.captcha = ''
    return
  }

  if (passwordForm.value.captcha.length < 4) {
    passwordFormErrors.value.captcha = '请输入完整的验证码'
    return
  }

  passwordFormErrors.value.captcha = ''
}

/**
 * 验证手机号（手机号登录）
 */
const validatePhoneLogin = () => {
  if (phoneForm.value.phone.length === 0) {
    phoneFormErrors.value.phone = ''
    return
  }

  if (phoneForm.value.phone.length !== 11) {
    phoneFormErrors.value.phone = '请输入正确的手机号'
    return
  }

  const phoneReg = /^1[3-9]\d{9}$/
  if (!phoneReg.test(phoneForm.value.phone)) {
    phoneFormErrors.value.phone = '手机号格式不正确'
    return
  }

  phoneFormErrors.value.phone = ''
}

/**
 * 验证验证码（手机号登录）
 */
const validatePhoneCode = () => {
  if (phoneForm.value.code.length === 0) {
    phoneFormErrors.value.code = ''
    return
  }

  if (phoneForm.value.code.length < 4) {
    phoneFormErrors.value.code = '请输入完整的验证码'
    return
  }

  phoneFormErrors.value.code = ''
}

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

    await userStore.wechatLogin(loginData)

    uni.showToast({
      title: '登录成功',
      icon: 'success'
    })

    // 跳转到首页
    setTimeout(() => {
      uni.switchTab({
        url: '/home/index'
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
 * 处理密码登录的手机号输入
 */
const handlePasswordPhoneInput = (e) => {
  const value = e.detail.value
  if (value.length > 11) {
    passwordForm.value.phone = value.substring(0, 11)
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
        url: '/home/index'
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
 * 切换密码显示/隐藏
 */
const togglePassword = () => {
  showPassword.value = !showPassword.value
}

/**
 * 处理密码登录
 */
const handlePasswordLogin = async () => {
  // 先进行所有字段验证
  validatePhone()
  validatePassword()
  validateCaptcha()

  // 检查是否有错误
  if (!isPasswordFormValid.value) {
    // 如果有字段错误，显示第一个错误
    const firstError = Object.values(passwordFormErrors.value).find(error => error !== '')
    if (firstError) {
      uni.showToast({
        title: firstError,
        icon: 'none'
      })
      return
    }

    // 如果没有字段错误，检查协议
    if (!agreedToTerms.value) {
      uni.showToast({
        title: '请先阅读并同意用户协议和隐私政策',
        icon: 'none'
      })
      return
    }

    return
  }

  loading.value = true

  try {
    // 调用后端登录接口（统一接口，支持验证码和密码两种方式）
    await userStore.login({
      phone: passwordForm.value.phone,
      password: passwordForm.value.password,
      captcha: passwordForm.value.captcha,
      checkCodeKey: passwordForm.value.checkCodeKey
    })

    // 保存登录历史（如果勾选了记住密码）
    if (passwordForm.value.rememberPassword) {
      savePhoneHistory({
        phone: passwordForm.value.phone,
        password: passwordForm.value.password
      })
    }

    uni.showToast({
      title: '登录成功',
      icon: 'success'
    })

    // 跳转到首页
    setTimeout(() => {
      uni.switchTab({
        url: '/home/index'
      })
    }, 1500)

  } catch (error) {
    console.error('密码登录失败:', error)
    uni.showToast({
      title: error.message || '登录失败，请重试',
      icon: 'none'
    })
    // 登录失败后刷新验证码
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

/**
 * 处理手机号登录记住密码
 */
const handlePhoneRememberChange = (e) => {
  phoneForm.value.rememberPassword = e.detail.value.length > 0
}

/**
 * 处理密码登录记住密码
 */
const handlePasswordRememberChange = (e) => {
  passwordForm.value.rememberPassword = e.detail.value.length > 0
}

/**
 * 显示手机号历史记录
 */
const showPhoneHistory = () => {
  if (phoneHistory.value.length > 0) {
    showHistoryList.value = true
  }
}

/**
 * 切换手机号历史记录显示
 */
const togglePhoneHistory = () => {
  showHistoryList.value = !showHistoryList.value
}

/**
 * 选择手机号历史记录
 */
const selectPhone = (item) => {
  passwordForm.value.phone = item.phone
  if (item.password) {
    passwordForm.value.password = item.password
    passwordForm.value.rememberPassword = true
  }
  showHistoryList.value = false
}

/**
 * 删除手机号历史记录
 */
const deletePhone = (phone) => {
  phoneHistory.value = phoneHistory.value.filter(item => item.phone !== phone)
  savePhoneHistoryToLocal()
  uni.showToast({
    title: '已删除',
    icon: 'success'
  })
}

/**
 * 点击其他区域关闭历史记录
 */
const handleClickOutside = () => {
  showHistoryList.value = false
}

/**
 * 保存手机号历史记录到本地存储
 */
const savePhoneHistory = (account) => {
  try {
    // 检查是否已存在
    const existingIndex = phoneHistory.value.findIndex(item => item.phone === account.phone)

    if (existingIndex !== -1) {
      // 更新现有记录
      phoneHistory.value[existingIndex] = account
    } else {
      // 添加新记录，最多保存10条
      phoneHistory.value.push(account)
      if (phoneHistory.value.length > 10) {
        phoneHistory.value.shift() // 删除最早的记录
      }
    }

    savePhoneHistoryToLocal()
  } catch (error) {
    console.error('保存历史记录失败:', error)
  }
}

/**
 * 保存手机号历史记录到本地存储
 */
const savePhoneHistoryToLocal = () => {
  try {
    uni.setStorageSync('phoneHistory', JSON.stringify(phoneHistory.value))
  } catch (error) {
    console.error('保存到本地存储失败:', error)
  }
}

/**
 * 从本地存储加载手机号历史记录
 */
const loadPhoneHistory = () => {
  try {
    const history = uni.getStorageSync('phoneHistory')
    if (history) {
      phoneHistory.value = JSON.parse(history)
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
    phoneHistory.value = []
  }
}

/**
 * 获取验证码
 */
const getCaptcha = async () => {
  try {
    const response = await userApi.getCaptcha()
    const result = response.data
    // 添加base64图片前缀
    captchaBase64.value = 'data:image/png;base64,' + result.checkCode
    passwordForm.value.checkCodeKey = result.checkCodeKey
  } catch (error) {
    console.error('获取验证码失败:', error)
    uni.showToast({
      title: '获取验证码失败',
      icon: 'none'
    })
  }
}

/**
 * 刷新验证码
 */
const refreshCaptcha = () => {
  getCaptcha()
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

/**
 * 忘记密码
 */
const toForgotPassword = () => {
  // TODO: 跳转到忘记密码页面
  uni.showToast({
    title: '忘记密码功能开发中',
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
  width: 120rpx;
  height: 120rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20rpx;
}

.logo-icon {
  font-size: 120rpx;
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
  display: flex;
  justify-content: center;
  margin: 40rpx 0;
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

/* 密码登录 */
.password-login {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.extra-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 10rpx;
  margin-bottom: 20rpx;
}

.remember-password {
  display: flex;
  align-items: center;
}

.checkbox-label {
  display: flex;
  align-items: center;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.checkbox-label text {
  margin-left: 10rpx;
}

.extra-links {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 0 10rpx;
}

.extra-links .link {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* 手机号历史记录 */
.autocomplete-item {
  position: relative;
}

.history-list {
  position: absolute;
  top: 100rpx;
  left: 40rpx;
  right: 40rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 8rpx 30rpx rgba(0, 0, 0, 0.15);
  z-index: 100;
  max-height: 400rpx;
  overflow-y: auto;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.history-item:last-child {
  border-bottom: none;
}

.history-phone {
  font-size: 28rpx;
  color: #333;
}

.delete-icon {
  cursor: pointer;
  padding: 10rpx;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

/* 输入框包装器，用于定位错误提示 */
.input-item-wrapper {
  position: relative;
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

/* 输入框右侧图标区域 */
.input-right-icons {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

/* 清除按钮 */
.clear-icon {
  cursor: pointer;
  padding: 8rpx;
  transition: all 0.2s;
}

.clear-icon:active {
  transform: scale(0.9);
}

.captcha-clear {
  position: absolute;
  right: 220rpx;
}

.dropdown-icon {
  cursor: pointer;
  padding: 10rpx;
}

.captcha-item {
  position: relative;
}

.captcha-wrapper {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.captcha-img {
  width: 200rpx;
  height: 70rpx;
  border-radius: 10rpx;
  background-color: #f5f7fa;
}

.refresh-btn {
  font-size: 32rpx;
  cursor: pointer;
  padding: 5rpx;
}

.password-toggle {
  cursor: pointer;
  padding: 10rpx;
  margin-left: 10rpx;
}

/* 输入框错误提示 */
.input-error-tip {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  padding: 10rpx 20rpx;
  font-size: 22rpx;
  color: #FF6B35;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 0 0 20rpx 20rpx;
  margin-top: 5rpx;
  z-index: 10;
  animation: slideDown 0.2s ease-out;
  box-shadow: 0 2rpx 10rpx rgba(255, 107, 53, 0.1);
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
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
