<template>
  <view class="forgot-container">
    <!-- 头部 -->
    <view class="header">
      <view class="back-btn" @click="goBack">
        <uni-icons type="left" size="20" color="#fff"></uni-icons>
      </view>
      <view class="title">重置密码</view>
    </view>

    <!-- 步骤指示器 -->
    <view class="steps-indicator">
      <view class="step-dot" :class="{ active: currentStep >= 1, done: currentStep > 1 }">
        <text v-if="currentStep > 1">✓</text>
        <text v-else>1</text>
      </view>
      <view class="step-line" :class="{ filled: currentStep > 1 }"></view>
      <view class="step-dot" :class="{ active: currentStep >= 2 }">
        <text>2</text>
      </view>
    </view>
    <view class="step-desc">{{ currentStep === 1 ? '验证手机号' : '设置新密码' }}</view>

    <!-- 步骤1：验证手机号 -->
    <view v-if="currentStep === 1" class="form-section">
      <view class="input-group">
        <InputField
          v-model="formData.phone"
          icon="phone"
          type="number"
          placeholder="请输入注册时的手机号"
          :maxlength="11"
          :error="formErrors.phone"
          clearable
          @blur="validateField('phone')"
          @validate="validateField('phone')"
        />

        <InputField
          v-model="formData.code"
          icon="locked"
          type="number"
          placeholder="请输入短信验证码"
          :maxlength="6"
          :error="formErrors.code"
          :button="{
            text: countdown > 0 ? `${countdown}s` : '获取验证码',
            disabled: countdown > 0,
            onClick: sendCode
          }"
          @blur="validateField('code')"
          @validate="validateField('code')"
        />
      </view>

      <button class="next-btn" @click="nextStep">
        下一步
      </button>
    </view>

    <!-- 步骤2：设置新密码 -->
    <view v-else class="form-section">
      <view class="input-group">
        <InputField
          v-model="formData.newPassword"
          icon="locked"
          type="text"
          placeholder="请输入新密码（6-32位）"
          :maxlength="32"
          :error="formErrors.newPassword"
          :isPassword="true"
          :showPassword="showNewPassword"
          :toggle="{
            icon: showNewPassword ? 'eye-filled' : 'eye',
            color: showNewPassword ? '#FF6B35' : '#999',
            onClick: () => showNewPassword = !showNewPassword
          }"
          @blur="validateField('newPassword')"
          @validate="validateField('newPassword')"
        />

        <InputField
          v-model="formData.confirmPassword"
          icon="locked"
          type="text"
          placeholder="请确认新密码"
          :maxlength="32"
          :error="formErrors.confirmPassword"
          :isPassword="true"
          :showPassword="showConfirmPassword"
          :toggle="{
            icon: showConfirmPassword ? 'eye-filled' : 'eye',
            color: showConfirmPassword ? '#FF6B35' : '#999',
            onClick: () => showConfirmPassword = !showConfirmPassword
          }"
          @blur="validateField('confirmPassword')"
          @validate="validateField('confirmPassword')"
        />
      </view>

      <view class="btn-row">
        <button class="back-btn-text" @click="prevStep">返回</button>
        <button class="submit-btn" :disabled="isSubmitting" @click="submitReset">
          {{ isSubmitting ? '重置中...' : '确认重置' }}
        </button>
      </view>
    </view>

    <!-- 底部链接 -->
    <view class="footer-links">
      <text class="hint">想起密码了？</text>
      <text class="link" @click="goBack">返回登录</text>
    </view>

    <!-- 加载提示 -->
    <view v-if="loading" class="loading-overlay">
      <uni-load-more status="loading" :contentText="{ contentrefresh: '处理中...' }"></uni-load-more>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { userApi } from '@/api'
import InputField from '@/pages/login/components/InputField.vue'

// 当前步骤
const currentStep = ref(1)

// 加载状态
const loading = ref(false)
const isSubmitting = ref(false)

// 倒计时
const countdown = ref(0)
let countdownTimer = null

// 密码显示状态
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)

// 表单数据
const formData = reactive({
  phone: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单错误
const formErrors = reactive({
  phone: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

/**
 * 字段验证
 */
const validateField = (field) => {
  const value = formData[field]

  switch (field) {
    case 'phone':
      if (!value) {
        formErrors.phone = '请输入手机号'
      } else if (value.length !== 11) {
        formErrors.phone = '请输入正确的手机号'
      } else if (!/^1[3-9]\d{9}$/.test(value)) {
        formErrors.phone = '手机号格式不正确'
      } else {
        formErrors.phone = ''
      }
      break

    case 'code':
      if (!value) {
        formErrors.code = '请输入验证码'
      } else if (value.length !== 6) {
        formErrors.code = '验证码为6位数字'
      } else {
        formErrors.code = ''
      }
      break

    case 'newPassword':
      if (!value) {
        formErrors.newPassword = '请输入新密码'
      } else if (value.length < 6) {
        formErrors.newPassword = '密码至少6位'
      } else if (value.length > 32) {
        formErrors.newPassword = '密码最多32位'
      } else {
        formErrors.newPassword = ''
      }
      // 同时验证确认密码
      if (formData.confirmPassword) {
        validateField('confirmPassword')
      }
      break

    case 'confirmPassword':
      if (!value) {
        formErrors.confirmPassword = '请确认新密码'
      } else if (value !== formData.newPassword) {
        formErrors.confirmPassword = '两次密码不一致'
      } else {
        formErrors.confirmPassword = ''
      }
      break
  }
}

/**
 * 发送验证码
 */
const sendCode = async () => {
  // 先验证手机号
  validateField('phone')
  if (formErrors.phone) return

  try {
    await userApi.sendCode(formData.phone)
    uni.showToast({ title: '验证码已发送', icon: 'success' })

    // 开始倒计时
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(countdownTimer)
      }
    }, 1000)
  } catch (error) {
    uni.showToast({ title: error.message || '发送失败', icon: 'none' })
  }
}

/**
 * 下一步
 */
const nextStep = () => {
  validateField('phone')
  validateField('code')

  if (formErrors.phone || formErrors.code) {
    const firstError = formErrors.phone || formErrors.code
    uni.showToast({ title: firstError, icon: 'none' })
    return
  }

  currentStep.value = 2
}

/**
 * 上一步
 */
const prevStep = () => {
  currentStep.value = 1
}

/**
 * 提交重置
 */
const submitReset = async () => {
  validateField('newPassword')
  validateField('confirmPassword')

  if (formErrors.newPassword || formErrors.confirmPassword) {
    const firstError = formErrors.newPassword || formErrors.confirmPassword
    uni.showToast({ title: firstError, icon: 'none' })
    return
  }

  isSubmitting.value = true

  try {
    await userApi.resetPassword({
      phone: formData.phone,
      code: formData.code,
      newPassword: formData.newPassword
    })

    uni.showToast({ title: '密码重置成功', icon: 'success' })

    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (error) {
    uni.showToast({ title: error.message || '重置失败', icon: 'none' })
  } finally {
    isSubmitting.value = false
  }
}

/**
 * 返回
 */
const goBack = () => {
  uni.navigateBack()
}

// 清理
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<style scoped>
.forgot-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 0 40rpx;
  display: flex;
  flex-direction: column;
}

/* 头部 */
.header {
  display: flex;
  align-items: center;
  padding: 60rpx 0 40rpx;
  position: relative;
}

.header .back-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 50%;
}

.header .title {
  flex: 1;
  text-align: center;
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
  margin-right: 60rpx;
}

/* 步骤指示器 */
.steps-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 40rpx;
}

.step-dot {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  border: 2rpx solid rgba(255, 255, 255, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.6);
  transition: all 0.3s;
}

.step-dot.active {
  background: #fff;
  border-color: #fff;
  color: #667eea;
}

.step-dot.done {
  background: #4CAF50;
  border-color: #4CAF50;
  color: #fff;
}

.step-line {
  width: 80rpx;
  height: 4rpx;
  background: rgba(255, 255, 255, 0.3);
  margin: 0 20rpx;
  transition: all 0.3s;
}

.step-line.filled {
  background: #fff;
}

.step-desc {
  text-align: center;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 20rpx;
  margin-bottom: 60rpx;
}

/* 表单区域 */
.form-section {
  flex: 1;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 40rpx;
}

/* 按钮 */
.next-btn {
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
  margin-top: 60rpx;
}

.btn-row {
  display: flex;
  gap: 20rpx;
  margin-top: 60rpx;
}

.back-btn-text {
  flex: 0 0 180rpx;
  height: 90rpx;
  line-height: 90rpx;
  text-align: center;
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  font-size: 28rpx;
  border-radius: 50rpx;
  border: none;
}

.submit-btn {
  flex: 1;
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

.submit-btn:disabled {
  opacity: 0.6;
}

/* 底部链接 */
.footer-links {
  text-align: center;
  padding: 60rpx 0;
  margin-top: auto;
}

.footer-links .hint {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.6);
}

.footer-links .link {
  font-size: 26rpx;
  color: #FFD700;
  margin-left: 10rpx;
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
