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
        v-for="tab in loginTabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: loginType === tab.value }"
        @click="loginType = tab.value"
      >
        {{ tab.label }}
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

    <!-- 手机号登录 -->
    <view v-if="loginType === 'phone'" class="phone-login">
      <view class="input-group">
        <InputField
          v-model="phoneForm.phone"
          icon="phone"
          type="number"
          placeholder="请输入手机号"
          :maxlength="11"
          :error="phoneFormErrors.phone"
          clearable
          @blur="() => validateField(phoneForm.phone, 'phone', phoneFormErrors)"
          @validate="(val) => validateField(val, 'phone', phoneFormErrors)"
        />

        <InputField
          v-model="phoneForm.code"
          icon="locked"
          type="number"
          placeholder="请输入验证码"
          :maxlength="6"
          :error="phoneFormErrors.code"
          :button="{
            text: countdown > 0 ? `${countdown}s` : '获取验证码',
            disabled: countdown > 0,
            onClick: sendCode
          }"
          @blur="() => validateField(phoneForm.code, 'code', phoneFormErrors)"
          @validate="(val) => validateField(val, 'code', phoneFormErrors)"
        />
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
        <InputField
          v-model="passwordForm.phone"
          icon="phone"
          type="number"
          placeholder="请输入手机号"
          :maxlength="11"
          :error="passwordFormErrors.phone"
          clearable
          :showHistory="true"
          :historyItems="phoneHistory"
          @selectHistory="selectPhone"
          @deleteHistory="deletePhone"
          @blur="() => validateField(passwordForm.phone, 'phone', passwordFormErrors)"
          @validate="(val) => validateField(val, 'phone', passwordFormErrors)"
        />

        <InputField
          v-model="passwordForm.password"
          icon="locked"
          type="text"
          placeholder="请输入密码（6-20位）"
          :maxlength="20"
          :error="passwordFormErrors.password"
          clearable
          :isPassword="true"
          :showPassword="showPassword"
          :toggle="{
            icon: showPassword ? 'eye-filled' : 'eye',
            color: showPassword ? '#FF6B35' : '#999',
            onClick: togglePassword
          }"
          @blur="() => validateField(passwordForm.password, 'password', passwordFormErrors, { minLength: 6, maxLength: 20 })"
          @validate="(val) => validateField(val, 'password', passwordFormErrors, { minLength: 6, maxLength: 20 })"
        />

        <InputField
          v-model="passwordForm.captcha"
          icon="checkmarkempty"
          type="text"
          placeholder="请输入验证码"
          :maxlength="4"
          :error="passwordFormErrors.captcha"
          :captcha="{
            image: captchaBase64,
            onRefresh: refreshCaptcha
          }"
          clearable
          @blur="() => validateField(passwordForm.captcha, 'captcha', passwordFormErrors)"
          @validate="(val) => validateField(val, 'captcha', passwordFormErrors)"
        />
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
import InputField from './components/InputField.vue'

// Pinia store
const userStore = useUserStore()

// 登录方式配置
const loginTabs = [
  { label: '微信登录', value: 'wechat' },
  { label: '验证码登录', value: 'phone' },
  { label: '密码登录', value: 'password' }
]

// 登录方式
const loginType = ref('wechat')

// 监听登录类型变化
watch(loginType, (newType) => {
  if (newType === 'password') {
    refreshCaptcha()
  }
})

// 组件挂载
onMounted(() => {
  loadPhoneHistory()
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

// 密码显示/隐藏
const showPassword = ref(false)

// 手机号历史记录
const phoneHistory = ref([])

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

// 表单错误提示
const phoneFormErrors = ref({
  phone: '',
  code: ''
})

const passwordFormErrors = ref({
  phone: '',
  password: '',
  captcha: ''
})

// 计算属性：表单是否有效
const isPhoneFormValid = computed(() => {
  return phoneForm.value.phone.length === 11 &&
         phoneForm.value.code.length > 0 &&
         agreedToTerms.value &&
         !Object.values(phoneFormErrors.value).some(error => error !== '')
})

const isPasswordFormValid = computed(() => {
  return passwordForm.value.phone.length === 11 &&
         passwordForm.value.password.length >= 6 &&
         passwordForm.value.captcha.length > 0 &&
         agreedToTerms.value &&
         !Object.values(passwordFormErrors.value).some(error => error !== '')
})

/**
 * 通用字段验证函数
 */
const validateField = (formValue, fieldType, errorObj, options = {}) => {
  const value = formValue

  // 空值清除错误
  if (!value || value.length === 0) {
    errorObj[fieldType] = ''
    return
  }

  // 手机号验证
  if (fieldType === 'phone') {
    if (value.length !== 11) {
      errorObj[fieldType] = '请输入正确的手机号'
      return
    }
    const phoneReg = /^1[3-9]\d{9}$/
    if (!phoneReg.test(value)) {
      errorObj[fieldType] = '手机号格式不正确'
      return
    }
    errorObj[fieldType] = ''
    return
  }

  // 密码验证
  if (fieldType === 'password') {
    const { minLength = 6, maxLength = 20 } = options
    if (value.length < minLength) {
      errorObj[fieldType] = `密码至少需要${minLength}位`
      return
    }
    if (value.length > maxLength) {
      errorObj[fieldType] = `密码最多${maxLength}位`
      return
    }
    errorObj[fieldType] = ''
    return
  }

  // 验证码验证 - 只做空值检测
  if (fieldType === 'code' || fieldType === 'captcha') {
    if (!value || value.length === 0) {
      errorObj[fieldType] = ''
      return
    }
    errorObj[fieldType] = ''
    return
  }
}

/**
 * 发送验证码
 */
const sendCode = async () => {
  validateField(phoneForm.value.phone, 'phone', phoneFormErrors)
  if (phoneFormErrors.value.phone) return

  try {
    await userApi.sendCode({ phone: phoneForm.value.phone })
    uni.showToast({ title: '验证码已发送', icon: 'success' })

    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(countdownTimer)
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败:', error)
    uni.showToast({ title: error.message || '发送失败，请重试', icon: 'none' })
  }
}

/**
 * 处理微信登录
 */
const handleWechatClick = () => {
  if (!agreedToTerms.value) {
    uni.showToast({ title: '请先阅读并同意用户协议和隐私政策', icon: 'none' })
  }
}

const handleWechatLogin = async (e) => {
  if (!agreedToTerms.value) {
    uni.showToast({ title: '请先阅读并同意用户协议和隐私政策', icon: 'none' })
    return
  }

  const { userInfo } = e.detail
  if (!userInfo) {
    uni.showToast({ title: '需要授权才能登录', icon: 'none' })
    return
  }

  loading.value = true

  try {
    await userStore.wechatLogin({
      nickName: userInfo.nickName,
      avatarUrl: userInfo.avatarUrl,
      gender: userInfo.gender,
      country: userInfo.country,
      province: userInfo.province,
      city: userInfo.city,
      language: userInfo.language
    })

    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages-user/home/index' })
    }, 1500)
  } catch (error) {
    console.error('微信登录失败:', error)
    uni.showToast({ title: error.message || '登录失败，请重试', icon: 'none' })
  } finally {
    loading.value = false
  }
}

/**
 * 处理手机号登录
 */
const handlePhoneLogin = async () => {
  // 验证所有字段
  validateField(phoneForm.value.phone, 'phone', phoneFormErrors)
  validateField(phoneForm.value.code, 'code', phoneFormErrors)

  if (!isPhoneFormValid.value) {
    const firstError = Object.values(phoneFormErrors.value).find(error => error !== '')
    if (firstError) {
      uni.showToast({ title: firstError, icon: 'none' })
      return
    }
    return
  }

  loading.value = true

  try {
    await userStore.login({
      phone: phoneForm.value.phone,
      code: phoneForm.value.code
    })

    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages-user/home/index' })
    }, 1500)
  } catch (error) {
    console.error('登录失败:', error)
    uni.showToast({ title: error.message || '登录失败，请重试', icon: 'none' })
  } finally {
    loading.value = false
  }
}

/**
 * 处理密码登录
 */
const handlePasswordLogin = async () => {
  // 验证所有字段
  validateField(passwordForm.value.phone, 'phone', passwordFormErrors)
  validateField(passwordForm.value.password, 'password', passwordFormErrors, { minLength: 6, maxLength: 20 })
  validateField(passwordForm.value.captcha, 'captcha', passwordFormErrors)

  if (!isPasswordFormValid.value) {
    const firstError = Object.values(passwordFormErrors.value).find(error => error !== '')
    if (firstError) {
      uni.showToast({ title: firstError, icon: 'none' })
      return
    }
    return
  }

  loading.value = true

  try {
    await userStore.login({
      phone: passwordForm.value.phone,
      password: passwordForm.value.password,
      captcha: passwordForm.value.captcha,
      checkCodeKey: passwordForm.value.checkCodeKey
    })

    if (passwordForm.value.rememberPassword) {
      savePhoneHistory({
        phone: passwordForm.value.phone,
        password: passwordForm.value.password
      })
    }

    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages-user/home/index' })
    }, 1500)
  } catch (error) {
    console.error('密码登录失败:', error)
    uni.showToast({ title: error.message || '登录失败，请重试', icon: 'none' })
    refreshCaptcha()
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
 * 手机号历史记录
 */
const selectPhone = (item) => {
  passwordForm.value.phone = item.phone
  if (item.password) {
    passwordForm.value.password = item.password
    passwordForm.value.rememberPassword = true
  }
}

const deletePhone = (phone) => {
  phoneHistory.value = phoneHistory.value.filter(item => item.phone !== phone)
  savePhoneHistoryToLocal()
  uni.showToast({ title: '已删除', icon: 'success' })
}

const savePhoneHistory = (account) => {
  const existingIndex = phoneHistory.value.findIndex(item => item.phone === account.phone)
  if (existingIndex !== -1) {
    phoneHistory.value[existingIndex] = account
  } else {
    phoneHistory.value.push(account)
    if (phoneHistory.value.length > 10) {
      phoneHistory.value.shift()
    }
  }
  savePhoneHistoryToLocal()
}

const savePhoneHistoryToLocal = () => {
  try {
    uni.setStorageSync('phoneHistory', JSON.stringify(phoneHistory.value))
  } catch (error) {
    console.error('保存到本地存储失败:', error)
  }
}

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
 * 获取/刷新验证码
 */
const getCaptcha = async () => {
  try {
    const response = await userApi.getCaptcha()
    console.log('验证码响应:', response)

    // 兼容不同的响应格式
    let result = null
    if (response.data) {
      result = response.data
    } else if (response.checkCode) {
      result = response
    }

    if (result && result.checkCode) {
      captchaBase64.value = 'data:image/png;base64,' + result.checkCode
      if (result.checkCodeKey) {
        passwordForm.value.checkCodeKey = result.checkCodeKey
      }
    } else {
      console.error('验证码响应格式不正确:', response)
    }
  } catch (error) {
    console.error('获取验证码失败:', error)
    uni.showToast({ title: '获取验证码失败', icon: 'none' })
  }
}

const refreshCaptcha = () => {
  getCaptcha()
}

/**
 * 记住密码处理
 */
const handlePhoneRememberChange = (e) => {
  phoneForm.value.rememberPassword = e.detail.value.length > 0
}

const handlePasswordRememberChange = (e) => {
  passwordForm.value.rememberPassword = e.detail.value.length > 0
}

/**
 * 协议处理
 */
const handleAgreementChange = (e) => {
  agreedToTerms.value = e.detail.value.length > 0
}

const toTerms = () => {
  uni.showToast({ title: '用户协议', icon: 'none' })
}

const toPrivacy = () => {
  uni.showToast({ title: '隐私政策', icon: 'none' })
}

/**
 * 跳转
 */
const toRegister = () => {
  uni.navigateTo({ url: '/pages/register/index' })
}

const toForgotPassword = () => {
  uni.showToast({ title: '忘记密码功能开发中', icon: 'none' })
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

/* 手机登录 & 密码登录 */
.phone-login,
.password-login {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 80rpx;
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
