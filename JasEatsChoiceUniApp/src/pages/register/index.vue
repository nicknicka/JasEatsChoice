<template>
  <view class="register-container">
    <!-- 头部 -->
    <view class="header">
      <view class="back-btn" @click="back">←</view>
      <view class="title">用户注册</view>
    </view>

    <!-- 注册步骤 -->
    <view class="steps">
      <view class="step-item" :class="{ active: currentStep >= 1 }">
        <view class="step-num">1</view>
        <view class="step-text">基本信息</view>
      </view>
      <view class="step-line" :class="{ active: currentStep >= 2 }"></view>
      <view class="step-item" :class="{ active: currentStep >= 2 }">
        <view class="step-num">2</view>
        <view class="step-text">身体数据</view>
      </view>
      <view class="step-line" :class="{ active: currentStep >= 3 }"></view>
      <view class="step-item" :class="{ active: currentStep >= 3 }">
        <view class="step-num">3</view>
        <view class="step-text">饮食偏好</view>
      </view>
    </view>

    <!-- 步骤1：基本信息 -->
    <view v-if="currentStep === 1" class="step-content">
      <view class="form-group">
        <view class="form-item">
          <view class="label">手机号</view>
          <view class="input-wrapper">
            <input
              type="number"
              v-model="form.phone"
              placeholder="请输入手机号"
              maxlength="11"
            />
          </view>
        </view>

        <view class="form-item">
          <view class="label">验证码</view>
          <view class="input-wrapper">
            <input
              type="number"
              v-model="form.code"
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

        <view class="form-item">
          <view class="label">密码</view>
          <view class="input-wrapper">
            <input
              type="password"
              v-model="form.password"
              placeholder="请设置密码（6-20位）"
              maxlength="20"
            />
          </view>
        </view>

        <view class="form-item">
          <view class="label">确认密码</view>
          <view class="input-wrapper">
            <input
              type="password"
              v-model="form.confirmPassword"
              placeholder="请再次输入密码"
              maxlength="20"
            />
          </view>
        </view>
      </view>

      <button class="next-btn" @click="nextStep" :disabled="!isStep1Valid">
        下一步
      </button>
    </view>

    <!-- 步骤2：身体数据 -->
    <view v-if="currentStep === 2" class="step-content">
      <view class="form-group">
        <view class="form-item">
          <view class="label">身高</view>
          <view class="input-wrapper">
            <input
              type="number"
              v-model="form.height"
              placeholder="请输入身高（cm）"
            />
            <view class="unit">cm</view>
          </view>
        </view>

        <view class="form-item">
          <view class="label">体重</view>
          <view class="input-wrapper">
            <input
              type="number"
              v-model="form.weight"
              placeholder="请输入体重（kg）"
            />
            <view class="unit">kg</view>
          </view>
        </view>

        <view class="form-item">
          <view class="label">性别</view>
          <view class="radio-group">
            <label
              class="radio-item"
              :class="{ active: form.gender === 'male' }"
              @click="form.gender = 'male'"
            >
              <view class="radio-icon">👨</view>
              <text>男</text>
            </label>
            <label
              class="radio-item"
              :class="{ active: form.gender === 'female' }"
              @click="form.gender = 'female'"
            >
              <view class="radio-icon">👩</view>
              <text>女</text>
            </label>
          </view>
        </view>

        <view class="form-item">
          <view class="label">出生日期</view>
          <picker
            mode="date"
            :value="form.birthday"
            :end="new Date().toISOString().split('T')[0]"
            @change="onBirthdayChange"
          >
            <view class="input-wrapper">
              <input
                :value="form.birthday"
                placeholder="请选择出生日期"
                disabled
              />
              <view class="arrow">▼</view>
            </view>
          </picker>
        </view>

        <view class="form-item">
          <view class="label">饮食目标</view>
          <view class="goal-group">
            <view
              class="goal-item"
              :class="{ active: form.goal === 'lose_weight' }"
              @click="form.goal = 'lose_weight'"
            >
              <view class="goal-icon">🏃</view>
              <view class="goal-text">减脂</view>
            </view>
            <view
              class="goal-item"
              :class="{ active: form.goal === 'keep_healthy' }"
              @click="form.goal = 'keep_healthy'"
            >
              <view class="goal-icon">💪</view>
              <view class="goal-text">保持健康</view>
            </view>
            <view
              class="goal-item"
              :class="{ active: form.goal === 'gain_muscle' }"
              @click="form.goal = 'gain_muscle'"
            >
              <view class="goal-icon">🏋️</view>
              <view class="goal-text">增肌</view>
            </view>
          </view>
        </view>
      </view>

      <view class="btn-group">
        <button class="prev-btn" @click="prevStep">上一步</button>
        <button class="next-btn" @click="nextStep" :disabled="!isStep2Valid">
          下一步
        </button>
      </view>
    </view>

    <!-- 步骤3：饮食偏好 -->
    <view v-if="currentStep === 3" class="step-content">
      <view class="form-group">
        <view class="form-item">
          <view class="label">口味偏好（可多选）</view>
          <view class="tag-group">
            <view
              class="tag-item"
              v-for="taste in tastes"
              :key="taste.value"
              :class="{ active: form.tastes.includes(taste.value) }"
              @click="toggleTaste(taste.value)"
            >
              {{ taste.label }}
            </view>
          </view>
        </view>

        <view class="form-item">
          <view class="label">禁忌食材（可多选）</view>
          <view class="tag-group">
            <view
              class="tag-item"
              v-for="allergy in allergies"
              :key="allergy.value"
              :class="{ active: form.allergies.includes(allergy.value) }"
              @click="toggleAllergy(allergy.value)"
            >
              {{ allergy.label }}
            </view>
          </view>
        </view>

        <view class="form-item">
          <view class="label">辣度偏好</view>
          <view class="slider-group">
            <slider
              :value="spicinessLevel"
              @change="onSpicinessChange"
              min="0"
              max="3"
              step="1"
              show-value
              activeColor="#FF6B35"
            />
            <view class="spiciness-labels">
              <text>不吃辣</text>
              <text>微辣</text>
              <text>中辣</text>
              <text>重辣</text>
            </view>
          </view>
        </view>
      </view>

      <view class="btn-group">
        <button class="prev-btn" @click="prevStep">上一步</button>
        <button class="submit-btn" @click="handleSubmit" :disabled="!isStep3Valid">
          完成注册
        </button>
      </view>
    </view>

    <!-- 协议 -->
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

    <!-- 加载提示 -->
    <view v-if="loading" class="loading-overlay">
      <uni-load-more status="loading" contentText="注册中..."></uni-load-more>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/store'
import { userApi } from '@/api'

// Pinia store
const userStore = useUserStore()

// 当前步骤
const currentStep = ref(1)

// 是否同意协议
const agreedToTerms = ref(false)

// 加载状态
const loading = ref(false)

// 倒计时
const countdown = ref(0)
let countdownTimer = null

// 辣度等级
const spicinessLevel = ref(1)

// 表单数据
const form = ref({
  phone: '',
  code: '',
  password: '',
  confirmPassword: '',
  height: '',
  weight: '',
  gender: 'male',
  birthday: '',
  goal: 'keep_healthy',
  tastes: [],
  allergies: []
})

// 口味选项
const tastes = [
  { label: '清淡', value: 'bland' },
  { label: '咸鲜', value: 'salty' },
  { label: '酸甜', value: 'sweet_sour' },
  { label: '香辣', value: 'spicy' },
  { label: '麻辣', value: 'numbing_spicy' }
]

// 禁忌食材
const allergies = [
  { label: '香菜', value: 'cilantro' },
  { label: '葱', value: 'onion' },
  { label: '蒜', value: 'garlic' },
  { label: '姜', value: 'ginger' },
  { label: '辣椒', value: 'chili' },
  { label: '海鲜', value: 'seafood' },
  { label: '花生', value: 'peanut' },
  { label: '牛肉', value: 'beef' },
  { label: '羊肉', value: 'lamb' }
]

// 计算属性：步骤1是否有效
const isStep1Valid = computed(() => {
  return form.value.phone.length === 11 &&
         form.value.code.length === 6 &&
         form.value.password.length >= 6 &&
         form.value.password === form.value.confirmPassword &&
         agreedToTerms.value
})

// 计算属性：步骤2是否有效
const isStep2Valid = computed(() => {
  return form.value.height &&
         form.value.weight &&
         form.value.birthday &&
         form.value.goal &&
         agreedToTerms.value
})

// 计算属性：步骤3是否有效
const isStep3Valid = computed(() => {
  return agreedToTerms.value
})

/**
 * 发送验证码
 */
const sendCode = async () => {
  if (form.value.phone.length !== 11) {
    uni.showToast({
      title: '请输入正确的手机号',
      icon: 'none'
    })
    return
  }

  // 验证手机号格式
  const phoneReg = /^1[3-9]\d{9}$/
  if (!phoneReg.test(form.value.phone)) {
    uni.showToast({
      title: '请输入正确的手机号',
      icon: 'none'
    })
    return
  }

  try {
    // 调用后端发送验证码接口
    await userApi.sendCode(form.value.phone)

    uni.showToast({
      title: '验证码已发送',
      icon: 'success'
    })

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
 * 下一步
 */
const nextStep = () => {
  if (currentStep.value === 1 && !isStep1Valid.value) {
    return
  }
  if (currentStep.value === 2 && !isStep2Valid.value) {
    return
  }
  currentStep.value++
}

/**
 * 上一步
 */
const prevStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--
  }
}

/**
 * 切换口味偏好
 */
const toggleTaste = (value) => {
  const index = form.value.tastes.indexOf(value)
  if (index > -1) {
    form.value.tastes.splice(index, 1)
  } else {
    form.value.tastes.push(value)
  }
}

/**
 * 切换禁忌食材
 */
const toggleAllergy = (value) => {
  const index = form.value.allergies.indexOf(value)
  if (index > -1) {
    form.value.allergies.splice(index, 1)
  } else {
    form.value.allergies.push(value)
  }
}

/**
 * 辣度变化
 */
const onSpicinessChange = (e) => {
  spicinessLevel.value = e.detail.value
}

/**
 * 生日选择
 */
const onBirthdayChange = (e) => {
  form.value.birthday = e.detail.value
}

/**
 * 提交注册
 */
const handleSubmit = async () => {
  if (!agreedToTerms.value) {
    uni.showToast({
      title: '请先阅读并同意用户协议和隐私政策',
      icon: 'none'
    })
    return
  }

  loading.value = true

  try {
    // 1. 先调用注册接口
    const registerData = {
      phone: form.value.phone,
      code: form.value.code,
      password: form.value.password,
      nickname: form.value.phone.substring(7) // 使用手机号后4位作为默认昵称
    }

    const registerRes = await userStore.register(registerData)

    // 2. 注册成功后，完善身体数据
    if (form.value.height && form.value.weight) {
      try {
        const profileData = {
          userId: registerRes.userId || registerRes.data?.userId,
          height: Number(form.value.height),
          weight: Number(form.value.weight),
          gender: form.value.gender,
          birthday: form.value.birthday,
          goal: form.value.goal,
          preferences: {
            tastes: form.value.tastes,
            allergies: form.value.allergies,
            spiciness: spicinessLevel.value
          }
        }

        await userApi.completeProfile(profileData)
      } catch (profileError) {
        console.error('完善身体数据失败:', profileError)
        // 身体数据失败不影响注册流程
      }
    }

    uni.showToast({
      title: '注册成功',
      icon: 'success'
    })

    // 跳转到首页
    setTimeout(() => {
      uni.switchTab({
        url: '/pages-user/home/index'
      })
    }, 1500)

  } catch (error) {
    console.error('注册失败:', error)
    uni.showToast({
      title: error.message || '注册失败，请重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 返回上一步
 */
const back = () => {
  uni.navigateBack()
}

/**
 * 处理协议同意
 */
const handleAgreementChange = (e) => {
  agreedToTerms.value = e.detail.value.length > 0
}

/**
 * 查看用户协议
 */
const toTerms = () => {
  uni.showToast({
    title: '用户协议',
    icon: 'none'
  })
}

/**
 * 查看隐私政策
 */
const toPrivacy = () => {
  uni.showToast({
    title: '隐私政策',
    icon: 'none'
  })
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 40rpx;
}

/* 头部 */
.header {
  display: flex;
  align-items: center;
  margin-bottom: 40rpx;
}

.back-btn {
  font-size: 40rpx;
  color: #333;
  margin-right: 20rpx;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

/* 步骤条 */
.steps {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 60rpx;
  padding: 0 20rpx;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}

.step-num {
  width: 60rpx;
  height: 60rpx;
  line-height: 60rpx;
  text-align: center;
  background: #ddd;
  color: #fff;
  border-radius: 50%;
  font-size: 24rpx;
  font-weight: bold;
}

.step-item.active .step-num {
  background: #FF6B35;
}

.step-text {
  font-size: 24rpx;
  color: #999;
}

.step-item.active .step-text {
  color: #FF6B35;
}

.step-line {
  flex: 1;
  height: 4rpx;
  background: #ddd;
  margin: 0 10rpx;
}

.step-line.active {
  background: #FF6B35;
}

/* 步骤内容 */
.step-content {
  background: #fff;
  border-radius: 20rpx;
  padding: 40rpx;
  margin-bottom: 40rpx;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 40rpx;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.label {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
}

.input-wrapper {
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 10rpx;
  padding: 20rpx 30rpx;
}

.input-wrapper input {
  flex: 1;
  font-size: 28rpx;
}

.input-wrapper .unit {
  color: #999;
  font-size: 24rpx;
}

.input-wrapper .arrow {
  color: #999;
  font-size: 20rpx;
}

.code-btn {
  padding: 0 30rpx;
  font-size: 24rpx;
  color: #FF6B35;
  background: transparent;
  border: none;
  border-left: 1rpx solid #ddd;
}

.code-btn:disabled {
  color: #999;
}

/* 性别选择 */
.radio-group {
  display: flex;
  gap: 40rpx;
}

.radio-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  padding: 20rpx;
  border-radius: 10rpx;
  border: 2rpx solid transparent;
}

.radio-item.active {
  border-color: #FF6B35;
  background: #fff5f0;
}

.radio-icon {
  font-size: 48rpx;
}

/* 饮食目标 */
.goal-group {
  display: flex;
  gap: 20rpx;
}

.goal-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  padding: 30rpx 20rpx;
  background: #f5f5f5;
  border-radius: 10rpx;
  border: 2rpx solid transparent;
}

.goal-item.active {
  border-color: #FF6B35;
  background: #fff5f0;
}

.goal-icon {
  font-size: 48rpx;
}

.goal-text {
  font-size: 24rpx;
  color: #333;
}

/* 标签选择 */
.tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.tag-item {
  padding: 15rpx 30rpx;
  background: #f5f5f5;
  border-radius: 40rpx;
  font-size: 24rpx;
  color: #666;
  border: 2rpx solid transparent;
}

.tag-item.active {
  background: #FF6B35;
  color: #fff;
  border-color: #FF6B35;
}

/* 滑块 */
.slider-group {
  padding: 20rpx 0;
}

.spiciness-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 20rpx;
}

.spiciness-labels text {
  font-size: 24rpx;
  color: #666;
}

/* 按钮 */
.btn-group {
  display: flex;
  gap: 20rpx;
  margin-top: 40rpx;
}

.next-btn,
.submit-btn {
  flex: 1;
  height: 90rpx;
  line-height: 90rpx;
  text-align: center;
  background: #FF6B35;
  color: #fff;
  font-size: 32rpx;
  font-weight: 500;
  border-radius: 50rpx;
  border: none;
}

.next-btn:disabled,
.submit-btn:disabled {
  opacity: 0.6;
}

.prev-btn {
  flex: 1;
  height: 90rpx;
  line-height: 90rpx;
  text-align: center;
  background: #fff;
  color: #666;
  font-size: 32rpx;
  font-weight: 500;
  border-radius: 50rpx;
  border: none;
}

/* 协议 */
.agreement {
  margin-top: 40rpx;
}

.agreement-label {
  display: flex;
  align-items: center;
  font-size: 24rpx;
  color: #666;
}

.agreement-label text {
  margin-left: 10rpx;
}

.link {
  color: #FF6B35;
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
