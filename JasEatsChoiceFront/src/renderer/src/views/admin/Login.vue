<template>
  <div class="admin-login-container">
    <!-- 装饰光球 -->
    <div class="orb orb-1"></div>
    <div class="orb orb-2"></div>
    <div class="orb orb-3"></div>

    <!-- 自定义标题栏 -->
    <WindowTitleBar />

    <!-- 品牌区域 -->
    <div class="brand-area">
      <div class="brand-icon">
        <svg width="44" height="44" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M24 4L6 14v20l18 10 18-10V14L24 4z" stroke="#5B7BA5" stroke-width="1.5" fill="none"/>
          <path d="M24 4v40M6 14l18 10 18-10M6 34l18-10 18 10" stroke="#5B7BA5" stroke-width="1" opacity="0.4"/>
          <circle cx="24" cy="24" r="6" stroke="#5B7BA5" stroke-width="1.5" fill="none"/>
          <path d="M24 18v-4M24 34v-4M18 24h-4M34 24h-4" stroke="#5B7BA5" stroke-width="1" opacity="0.5"/>
        </svg>
      </div>
      <h1 class="brand-title">管理后台</h1>
      <p class="brand-subtitle">JasEatsChoice Admin</p>
    </div>

    <!-- 表单卡片 -->
    <div class="glass-card">
      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" @submit.prevent>
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="管理员账号"
            size="default"
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
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            show-password
            autocomplete="off"
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                <rect x="3" y="11" width="18" height="11" rx="2"/>
                <path d="M7 11V7a5 5 0 0110 0v4"/>
              </svg>
            </template>
          </el-input>
        </el-form-item>

        <div class="remember-row">
          <el-checkbox v-model="loginForm.remember">记住密码</el-checkbox>
        </div>

        <button class="login-btn" @click="handleLogin" :disabled="loading">
          <span>{{ loading ? '登录中...' : '登 录' }}</span>
        </button>
      </el-form>

      <!-- 底部 -->
      <div class="footer-area">
        <p class="default-account">默认账号：admin / admin123</p>
        <div class="switch-links">
          <a class="link" @click="goToUserLogin">用户端登录</a>
          <span class="sep">|</span>
          <a class="link" @click="goToMerchantLogin">商家端登录</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminLogin } from '@/api/admin'
import { setAdminToken, setAdminInfo } from '@/utils/auth'
import { useWindowControl } from '@/composables/useWindowControl'
import WindowTitleBar from '@/components/WindowTitleBar.vue'

const router = useRouter()
const { expandToMain } = useWindowControl()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    loading.value = true

    const response = await adminLogin(loginForm.username, loginForm.password)

    if (response.success) {
      setAdminToken(response.token)
      setAdminInfo(response.admin)
      ElMessage.success('登录成功')

      await expandToMain()
      router.push('/admin/dashboard')
    } else {
      ElMessage.error(response.message || '登录失败')
      generateCaptcha()
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.message || '登录失败，请检查用户名和密码')
    generateCaptcha()
  } finally {
    loading.value = false
  }
}

const goToUserLogin = async () => {
  router.push('/login')
}

const goToMerchantLogin = async () => {
  router.push('/login')
}
</script>

<style scoped lang="less">
// === 管理员：简约蓝灰 + 玻璃感 ===
@accent: #5B7BA5;
@accent-gradient: linear-gradient(135deg, #5B7BA5, #4A6A94);
@text-dark: #2C3E50;
@text-muted: #8E9AAF;
@text-placeholder: #B8C4CE;
@card-bg: rgba(255, 255, 255, 0.55);
@card-border: rgba(255, 255, 255, 0.7);
@input-bg: #FFFFFF;
@input-border: #DEE2E8;

.admin-login-container {
  width: 100%;
  height: 100vh;
  background: #F2F5F9;
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
  width: 240px;
  height: 240px;
  background: radial-gradient(circle, rgba(91, 123, 165, 0.15) 0%, transparent 70%);
  top: -70px;
  right: -50px;
}

.orb-2 {
  width: 180px;
  height: 180px;
  background: radial-gradient(circle, rgba(91, 123, 165, 0.12) 0%, transparent 70%);
  bottom: 30px;
  left: -50px;
}

.orb-3 {
  width: 120px;
  height: 120px;
  background: radial-gradient(circle, rgba(91, 123, 165, 0.08) 0%, transparent 70%);
  top: 45%;
  left: 60%;
}

// 品牌区域
.brand-area {
  text-align: center;
  margin-top: 24px;
  position: relative;
  z-index: 2;
}

.brand-icon {
  animation: breathe 4s ease-in-out infinite;
}

@keyframes breathe {
  0%, 100% { transform: scale(1); opacity: 0.9; }
  50% { transform: scale(1.04); opacity: 1; }
}

.brand-title {
  font-size: 22px;
  font-weight: 700;
  color: @text-dark;
  margin: 10px 0 4px;
  letter-spacing: 3px;
}

.brand-subtitle {
  font-size: 11px;
  color: @text-muted;
  letter-spacing: 1.5px;
  font-weight: 400;
}

// 毛玻璃卡片
.glass-card {
  width: calc(100% - 40px);
  margin: 24px 20px 16px;
  padding: 24px 24px 16px;
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
}

// Element Plus 输入框
:deep(.el-form-item) {
  margin-bottom: 14px;
}

:deep(.el-form-item__error) {
  color: #E07060;
  font-size: 11px;
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
    box-shadow: 0 0 0 3px rgba(91, 123, 165, 0.1);
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

:deep(.el-input__password) {
  color: @text-muted;
  &:hover { color: @text-dark; }
}

// 记住密码
.remember-row {
  margin-bottom: 14px;
  padding-left: 2px;

  :deep(.el-checkbox__label) {
    color: @text-muted;
    font-size: 12px;
  }

  :deep(.el-checkbox__inner) {
    background: transparent;
    border-color: #CCC;
    border-radius: 3px;
  }

  :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
    background: @accent;
    border-color: @accent;

    &::after {
      border-color: white;
    }
  }

  :deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
    color: @text-dark;
  }
}

// 登录按钮
.login-btn {
  width: 100%;
  height: 42px;
  border: none;
  border-radius: 10px;
  background: @accent-gradient;
  color: white;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 6px;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 4px 14px rgba(91, 123, 165, 0.3);

  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 6px 20px rgba(91, 123, 165, 0.4);
  }

  &:active:not(:disabled) {
    transform: translateY(0) scale(0.98);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

// 底部
.footer-area {
  margin-top: auto;
  text-align: center;
}

.default-account {
  font-size: 11px;
  color: @text-muted;
  margin-bottom: 12px;
}

.switch-links {
  font-size: 12px;

  .link {
    color: @text-muted;
    cursor: pointer;
    transition: color 0.2s;

    &:hover { color: @accent; }
  }

  .sep {
    margin: 0 8px;
    color: #DDD;
  }
}
</style>
