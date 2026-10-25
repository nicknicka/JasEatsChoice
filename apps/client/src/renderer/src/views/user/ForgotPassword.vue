<template>
  <div class="forgot-container">
    <!-- 装饰光球 -->
    <div class="orb orb-1"></div>
    <div class="orb orb-2"></div>

    <!-- 自定义标题栏 -->
    <WindowTitleBar />

    <!-- 标题 -->
    <div class="forgot-header">
      <h1 class="forgot-title">重置密码</h1>
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
      <p class="step-desc">{{ currentStep === 1 ? '验证手机号' : '设置新密码' }}</p>
    </div>

    <!-- 表单卡片 -->
    <div class="glass-card">
      <el-form ref="formRef" :model="formData" :rules="formRules" @submit.prevent>
        <transition name="slide" mode="out-in">
          <!-- 步骤1：验证手机号 -->
          <div v-if="currentStep === 1" key="step1" class="step-content">
            <el-form-item prop="phone">
              <el-input
                v-model="formData.phone"
                placeholder="注册时使用的手机号"
                autocomplete="off"
              >
                <template #prefix>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <rect x="5" y="2" width="14" height="20" rx="2"/>
                    <line x1="12" y1="18" x2="12" y2="18.01"/>
                  </svg>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="code">
              <div class="code-row">
                <el-input
                  v-model="formData.code"
                  placeholder="短信验证码"
                >
                  <template #prefix>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                      <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                      <path d="M2 17l10 5 10-5"/>
                      <path d="M2 12l10 5 10-5"/>
                    </svg>
                  </template>
                </el-input>
                <button
                  class="code-btn"
                  :disabled="codeCooldown > 0 || isSendingCode"
                  @click.prevent="sendSmsCode"
                >
                  {{ codeCooldown > 0 ? `${codeCooldown}s` : '获取验证码' }}
                </button>
              </div>
            </el-form-item>

            <button class="step-btn next-btn" @click="nextStep">
              <span>下一步</span>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="9 18 15 12 9 6"/>
              </svg>
            </button>
          </div>

          <!-- 步骤2：设置新密码 -->
          <div v-else key="step2" class="step-content">
            <el-form-item prop="newPassword">
              <el-input
                v-model="formData.newPassword"
                type="password"
                placeholder="新密码（6-32位）"
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
                v-model="formData.confirmPassword"
                type="password"
                placeholder="确认新密码"
                show-password
              >
                <template #prefix>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                  </svg>
                </template>
              </el-input>
            </el-form-item>

            <div class="btn-row">
              <button class="step-btn back-btn" @click="prevStep">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="15 18 9 12 15 6"/>
                </svg>
                <span>返回</span>
              </button>
              <button class="step-btn submit-btn" @click="submitReset" :disabled="isSubmitting">
                <span>{{ isSubmitting ? '重置中...' : '确认重置' }}</span>
              </button>
            </div>
          </div>
        </transition>
      </el-form>

      <!-- 底部链接 -->
      <div class="bottom-links">
        <span>想起密码了？</span>
        <a class="link" @click="toLogin">返回登录</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { API_CONFIG } from '../../config'
import { useWindowControl } from '../../composables/useWindowControl'
import WindowTitleBar from '../../components/WindowTitleBar.vue'

const router = useRouter()
const { shrinkToLogin } = useWindowControl()

const currentStep = ref(1)
const isSubmitting = ref(false)
const isSendingCode = ref(false)
const codeCooldown = ref(0)
let cooldownTimer = null

const formData = reactive({
  phone: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const formRules = reactive({
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3456789]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度在6到32个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== formData.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

const formRef = ref(null)

// 发送短信验证码
const sendSmsCode = async () => {
  // 先校验手机号
  try {
    await formRef.value.validateField(['phone'])
  } catch {
    return
  }

  isSendingCode.value = true
  try {
    const response = await axios.post(`${API_CONFIG.baseURL}${API_CONFIG.user.sendSmsCode}`, {
      phone: formData.phone
    })

    if (response.data.code === '200') {
      ElMessage.success('验证码已发送')
      // 开始倒计时
      codeCooldown.value = 60
      cooldownTimer = setInterval(() => {
        codeCooldown.value--
        if (codeCooldown.value <= 0) {
          clearInterval(cooldownTimer)
          cooldownTimer = null
        }
      }, 1000)
    } else {
      ElMessage.error(response.data.message || '发送验证码失败')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '发送验证码失败，请稍后重试')
  } finally {
    isSendingCode.value = false
  }
}

// 下一步
const nextStep = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validateField(['phone', 'code'])
    currentStep.value = 2
  } catch {
    ElMessage.error('请完善验证信息')
  }
}

// 上一步
const prevStep = () => {
  currentStep.value = 1
}

// 提交重置密码
const submitReset = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    isSubmitting.value = true

    const response = await axios.post(`${API_CONFIG.baseURL}/v1/users/reset-password`, {
      phone: formData.phone,
      code: formData.code,
      newPassword: formData.newPassword
    })

    if (response.data.code === '200') {
      ElMessage.success('密码重置成功，请使用新密码登录')
      await shrinkToLogin()
      setTimeout(() => {
        router.push('/login')
      }, 200)
    } else {
      ElMessage.error(response.data.message || '密码重置失败')
    }
  } catch (error) {
    const isFormError = error && typeof error === 'object' && !Array.isArray(error) && !(error instanceof Error)
    if (isFormError && error.fields) {
      ElMessage.error('请检查表单填写是否正确')
    } else {
      ElMessage.error(error.response?.data?.message || '密码重置失败，请稍后重试')
    }
  } finally {
    isSubmitting.value = false
  }
}

// 返回登录
const toLogin = async () => {
  await shrinkToLogin()
  setTimeout(() => {
    router.push('/login')
  }, 200)
}

// 清理倒计时
onUnmounted(() => {
  if (cooldownTimer) {
    clearInterval(cooldownTimer)
  }
})
</script>

<style scoped lang="less">
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

.forgot-container {
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

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
}

.orb-1 {
  width: 240px;
  height: 240px;
  background: radial-gradient(circle, rgba(242, 120, 75, 0.2) 0%, transparent 70%);
  top: -60px;
  left: -40px;
}

.orb-2 {
  width: 160px;
  height: 160px;
  background: radial-gradient(circle, rgba(255, 154, 118, 0.18) 0%, transparent 70%);
  bottom: 40px;
  right: -40px;
}

.forgot-header {
  text-align: center;
  margin-top: 8px;
  position: relative;
  z-index: 2;
}

.forgot-title {
  font-size: 22px;
  font-weight: 700;
  color: @text-dark;
  margin-bottom: 16px;
  letter-spacing: 2px;
}

.steps-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
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

.slide-enter-active {
  animation: slideIn 0.3s ease forwards;
}

.slide-leave-active {
  animation: slideOut 0.2s ease forwards;
}

@keyframes slideIn {
  from { opacity: 0; transform: translateX(20px); }
  to { opacity: 1; transform: translateX(0); }
}

@keyframes slideOut {
  from { opacity: 1; transform: translateX(0); }
  to { opacity: 0; transform: translateX(-20px); }
}

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
.code-row {
  display: flex;
  gap: 8px;
  align-items: center;
  width: 100%;

  :deep(.el-input) {
    flex: 1;
    min-width: 0;
  }
}

.code-btn {
  flex-shrink: 0;
  height: 36px;
  padding: 0 14px;
  border: 1px solid @accent;
  border-radius: 10px;
  background: white;
  color: @accent;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;

  &:hover:not(:disabled) {
    background: rgba(242, 120, 75, 0.06);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
    border-color: @input-border;
    color: @text-muted;
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
