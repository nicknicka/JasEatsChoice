<template>
  <div class="register-container">
    <!-- 装饰光球 -->
    <div class="orb orb-1"></div>
    <div class="orb orb-2"></div>
    <div class="orb orb-3"></div>

    <!-- 自定义标题栏 -->
    <WindowTitleBar />

    <!-- 标题 -->
    <div class="register-header">
      <h1 class="register-title">创建账号</h1>
      <!-- 步骤指示器 -->
      <div class="steps-indicator">
        <div class="step-dot" :class="{ active: currentStep >= 1, done: currentStep > 1 }">
          <span v-if="currentStep > 1">
            <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg>
          </span>
          <span v-else>1</span>
        </div>
        <div class="step-line" :class="{ filled: currentStep > 1 }"></div>
        <div class="step-dot" :class="{ active: currentStep >= 2 }">2</div>
      </div>
      <p class="step-desc">{{ currentStep === 1 ? '设置登录信息' : '填写联系方式' }}</p>
    </div>

    <!-- 表单卡片 -->
    <div class="glass-card">
      <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" @submit.prevent>
        <transition name="slide" mode="out-in">
          <!-- 步骤1：基本信息 -->
          <div v-if="currentStep === 1" key="step1" class="step-content">
            <el-form-item prop="nickname">
              <el-input
                v-model="registerForm.nickname"
                placeholder="用户名"
                autocomplete="off"
              >
                <template #prefix>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                  </svg>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="密码（6-32位）"
                show-password
              >
                <template #prefix>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <rect x="3" y="11" width="18" height="11" rx="2"/>
                    <path d="M7 11V7a5 5 0 0110 0v4"/>
                  </svg>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="确认密码"
                show-password
              >
                <template #prefix>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                  </svg>
                </template>
              </el-input>
            </el-form-item>

            <button class="step-btn next-btn" @click="nextStep">
              <span>下一步</span>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="9 18 15 12 9 6"/>
              </svg>
            </button>
          </div>

          <!-- 步骤2：联系方式 -->
          <div v-else key="step2" class="step-content">
            <el-form-item prop="email">
              <el-input
                v-model="registerForm.email"
                placeholder="邮箱"
                type="email"
              >
                <template #prefix>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <rect x="2" y="4" width="20" height="16" rx="2"/>
                    <path d="M22 4l-10 8L2 4"/>
                  </svg>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="phone">
              <el-input
                v-model="registerForm.phone"
                placeholder="手机号"
              >
                <template #prefix>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <rect x="5" y="2" width="14" height="20" rx="2"/>
                    <line x1="12" y1="18" x2="12" y2="18.01"/>
                  </svg>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="captcha">
              <div class="captcha-row">
                <el-input
                  v-model="registerForm.captcha"
                  placeholder="验证码"
                >
                  <template #prefix>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                      <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                      <path d="M2 17l10 5 10-5"/>
                      <path d="M2 12l10 5 10-5"/>
                    </svg>
                  </template>
                </el-input>
                <img
                  :src="captchaBase64"
                  alt="验证码"
                  class="captcha-img"
                  @click="generateCaptcha"
                />
                <button class="captcha-refresh" @click.prevent="generateCaptcha" title="刷新验证码">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M23 4v6h-6"/>
                    <path d="M20.49 15a9 9 0 11-2.12-9.36L23 10"/>
                  </svg>
                </button>
              </div>
            </el-form-item>

            <div class="btn-row">
              <button class="step-btn back-btn" @click="prevStep">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="15 18 9 12 15 6"/>
                </svg>
                <span>返回</span>
              </button>
              <button class="step-btn submit-btn" @click="submitForm" :disabled="isSubmitting">
                <span>{{ isSubmitting ? '注册中...' : '注册' }}</span>
              </button>
            </div>
          </div>
        </transition>
      </el-form>

      <!-- 底部链接 -->
      <div class="bottom-links">
        <span>已有账号？</span>
        <a class="link" @click="toLogin">立即登录</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { API_CONFIG } from '../../config'
import { useWindowControl } from '../../composables/useWindowControl'
import WindowTitleBar from '../../components/WindowTitleBar.vue'

const router = useRouter()
const { shrinkToLogin } = useWindowControl()

// 当前步骤
const currentStep = ref(1)

// 提交状态
const isSubmitting = ref(false)

// 注册表单数据
const registerForm = reactive({
  nickname: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  captcha: ''
})

// 表单验证规则
const registerRules = reactive({
  nickname: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度在 6 到 32 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3456789]\d{9}$/, message: '请输入正确的手机号码', trigger: ['blur', 'change'] }
  ],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
})

// 验证码相关
const captchaBase64 = ref('')
const checkCodeKey = ref('')

const applyAutoCaptcha = (result) => {
  if (result.fixedCaptchaEnabled === 'true' && result.fixedCaptchaCode) {
    registerForm.captcha = result.fixedCaptchaCode.toUpperCase()
    return
  }

  const isDevMode = import.meta.env.MODE === 'development' || import.meta.env.DEV
  if (isDevMode && result.captchaAnswer) {
    registerForm.captcha = result.captchaAnswer.toUpperCase()
    return
  }

  registerForm.captcha = ''
}

const generateCaptcha = async () => {
  try {
    const response = await axios.get(`${API_CONFIG.baseURL}/v1/captcha/checkCode`)
    const result = response.data.data
    captchaBase64.value = 'data:image/png;base64,' + result.checkCode
    checkCodeKey.value = result.checkCodeKey
    applyAutoCaptcha(result)
  } catch (error) {
    console.error('获取验证码失败:', error)
    ElMessage.error('获取验证码失败，请稍后重试')
  }
}

const registerFormRef = ref(null)

onMounted(() => {
  generateCaptcha()
})

watch(
  () => router.currentRoute.value.path,
  (newPath) => {
    if (newPath === '/register') {
      generateCaptcha()
      currentStep.value = 1
    }
  }
)

// 步骤1验证的字段
const step1Fields = ['nickname', 'password', 'confirmPassword']

// 下一步
const nextStep = async () => {
  if (!registerFormRef.value) return
  try {
    await registerFormRef.value.validateField(step1Fields)
    currentStep.value = 2
  } catch {
    ElMessage.error('请完善第一步信息')
  }
}

// 上一步
const prevStep = () => {
  currentStep.value = 1
}

// 提交表单
const submitForm = async () => {
  if (!registerFormRef.value) return

  try {
    await registerFormRef.value.validate()
    isSubmitting.value = true

    const response = await axios.post(`${API_CONFIG.baseURL}${API_CONFIG.user.register}`, {
      nickname: registerForm.nickname,
      password: registerForm.password,
      email: registerForm.email,
      phone: registerForm.phone,
      captcha: registerForm.captcha,
      checkCodeKey: checkCodeKey.value
    })

    if (response.data.code === '200') {
      ElMessage.success('注册成功！')
      await shrinkToLogin()
      setTimeout(() => {
        router.push('/login')
      }, 200)
    } else {
      ElMessage.error(response.data.message || '注册失败，请稍后重试')
      generateCaptcha()
    }
  } catch (error) {
    // 区分表单验证错误和接口请求错误
    const isFormError = error && typeof error === 'object' && !Array.isArray(error) && !(error instanceof Error)
    if (isFormError && error.fields) {
      // Element Plus 表单验证失败
      ElMessage.error('请检查表单填写是否正确')
    } else {
      ElMessage.error(error.response?.data?.message || '注册请求失败，请稍后重试')
    }
    generateCaptcha()
  } finally {
    isSubmitting.value = false
  }
}

// 跳转到登录页面
const toLogin = async () => {
  await shrinkToLogin()
  setTimeout(() => {
    router.push('/login')
  }, 200)
}
</script>

<style scoped lang="less">
// === 配色：简约暖橙 + 玻璃感 ===
@accent: #F2784B;
@accent-light: #FF9A76;
@accent-gradient: linear-gradient(135deg, #F2784B, #E85D3A);
@text-dark: #2C3E50;
@text-muted: #8E9AAF;
@text-placeholder: #B8C4CE;
@card-bg: rgba(255, 255, 255, 0.55);
@card-border: rgba(255, 255, 255, 0.7);
@input-bg: #FFFFFF;
@input-border: #E8E4E0;

.register-container {
  width: 100%;
  height: 100vh;
  background: #FFF7F2;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  overflow: hidden;
  font-family: "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
}

// 装饰光球
.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
}

.orb-1 {
  width: 260px;
  height: 260px;
  background: radial-gradient(circle, rgba(242, 120, 75, 0.2) 0%, transparent 70%);
  top: -70px;
  left: -50px;
}

.orb-2 {
  width: 180px;
  height: 180px;
  background: radial-gradient(circle, rgba(255, 154, 118, 0.18) 0%, transparent 70%);
  bottom: 30px;
  right: -50px;
}

.orb-3 {
  width: 120px;
  height: 120px;
  background: radial-gradient(circle, rgba(242, 120, 75, 0.12) 0%, transparent 70%);
  top: 50%;
  left: 65%;
}

// 标题区域
.register-header {
  text-align: center;
  margin-top: 8px;
  position: relative;
  z-index: 2;
}

.register-title {
  font-size: 22px;
  font-weight: 700;
  color: @text-dark;
  margin-bottom: 16px;
  letter-spacing: 2px;
}

// 步骤指示器
.steps-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
}

.step-dot {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 2px solid #E0D8D2;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: @text-muted;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  background: white;

  &.active {
    border-color: @accent;
    background: @accent;
    color: white;
  }

  &.done {
    border-color: @accent;
    background: @accent;
    color: white;
  }
}

.step-line {
  width: 40px;
  height: 2px;
  background: #E0D8D2;
  transition: all 0.4s ease;

  &.filled {
    background: @accent;
  }
}

.step-desc {
  font-size: 11px;
  color: @text-muted;
  margin-top: 10px;
  letter-spacing: 1px;
}

// 毛玻璃卡片
.glass-card {
  width: calc(100% - 40px);
  margin: 14px 20px 16px;
  padding: 22px 24px 16px;
  background: @card-bg;
  backdrop-filter: blur(24px) saturate(1.3);
  -webkit-backdrop-filter: blur(24px) saturate(1.3);
  border: 1px solid @card-border;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06), 0 1px 3px rgba(0, 0, 0, 0.04);
  position: relative;
  z-index: 2;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

// 步骤切换动画
.slide-enter-active {
  animation: slideIn 0.3s ease forwards;
}

.slide-leave-active {
  animation: slideOut 0.2s ease forwards;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideOut {
  from {
    opacity: 1;
    transform: translateX(0);
  }
  to {
    opacity: 0;
    transform: translateX(-20px);
  }
}

// Element Plus 输入框
:deep(.el-form-item) {
  margin-bottom: 10px;
}

:deep(.el-form-item__error) {
  color: #E07060;
  font-size: 11px;
  padding-top: 2px;
}

:deep(.el-input__wrapper) {
  background: @input-bg;
  border: 1px solid @input-border;
  border-radius: 10px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  padding: 7px 12px;
  transition: all 0.2s ease;

  &:hover {
    border-color: darken(@input-border, 12%);
  }

  &.is-focus {
    border-color: @accent;
    box-shadow: 0 0 0 3px rgba(242, 120, 75, 0.1);
  }
}

:deep(.el-input__inner) {
  color: @text-dark;
  font-size: 13px;
  caret-color: @accent;

  &::placeholder {
    color: @text-placeholder;
    font-size: 13px;
  }
}

:deep(.el-input__prefix) {
  color: @text-muted;
  margin-right: 6px;
}

:deep(.el-input__suffix) {
  color: @text-muted;
}

:deep(.el-input__password) {
  color: @text-muted;
  &:hover { color: @text-dark; }
}

// 验证码行
.captcha-row {
  display: flex;
  gap: 8px;
  align-items: center;
  width: 100%;

  :deep(.el-input) {
    flex: 1;
    min-width: 0;
  }
}

.captcha-img {
  height: 36px;
  width: 90px;
  border-radius: 8px;
  border: 1px solid @input-border;
  background: #FAFAFA;
  cursor: pointer;
  object-fit: contain;
  transition: all 0.2s;
  flex-shrink: 0;
  padding: 2px;

  &:hover {
    border-color: @accent;
  }
}

.captcha-refresh {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  color: @text-muted;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;

  &:hover {
    color: @accent;
    transform: rotate(90deg);
  }
}

// 按钮样式
.step-btn {
  height: 42px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.25s ease;
  letter-spacing: 2px;

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.next-btn {
  width: 100%;
  margin-top: 4px;
  background: @accent-gradient;
  color: white;
  box-shadow: 0 4px 14px rgba(242, 120, 75, 0.3);

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 20px rgba(242, 120, 75, 0.4);
  }

  &:active {
    transform: translateY(0) scale(0.98);
  }
}

.btn-row {
  display: flex;
  gap: 10px;
  margin-top: 4px;
}

.back-btn {
  flex: 0 0 100px;
  background: white;
  border: 1px solid @input-border;
  color: @text-muted;

  &:hover {
    border-color: @accent;
    color: @accent;
  }
}

.submit-btn {
  flex: 1;
  background: @accent-gradient;
  color: white;
  box-shadow: 0 4px 14px rgba(242, 120, 75, 0.3);

  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 6px 20px rgba(242, 120, 75, 0.4);
  }

  &:active:not(:disabled) {
    transform: translateY(0) scale(0.98);
  }
}

// 底部链接
.bottom-links {
  text-align: center;
  margin-top: auto;
  padding-top: 14px;
  font-size: 12px;
  color: @text-muted;

  .link {
    color: @accent;
    cursor: pointer;
    font-weight: 500;
    transition: opacity 0.2s;

    &:hover { opacity: 0.8; }
  }
}
</style>
