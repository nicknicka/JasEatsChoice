<template>
  <div class="login-container">
    <!-- 装饰光球 -->
    <div class="orb orb-1"></div>
    <div class="orb orb-2"></div>
    <div class="orb orb-3"></div>

    <!-- 自定义标题栏 -->
    <WindowTitleBar />

    <!-- 品牌区域 -->
    <div class="brand-area">
      <div class="brand-logo">
        <svg width="44" height="44" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M6 20C6 20 8 34 24 34C40 34 42 20 42 20" stroke="#E8825C" stroke-width="2.5" stroke-linecap="round" fill="none"/>
          <path d="M4 20H44" stroke="#E8825C" stroke-width="2" stroke-linecap="round" opacity="0.6"/>
          <path d="M20 16L30 4" stroke="#E8825C" stroke-width="2" stroke-linecap="round" opacity="0.8"/>
          <path d="M24 16L36 6" stroke="#E8825C" stroke-width="2" stroke-linecap="round" opacity="0.8"/>
          <path d="M18 38H30" stroke="#E8825C" stroke-width="2" stroke-linecap="round" opacity="0.4"/>
        </svg>
      </div>
      <h1 class="brand-name">佳食宜选</h1>
      <p class="brand-tagline">美食由你选择</p>
    </div>

    <!-- 表单卡片 -->
    <div class="glass-card">
      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" @submit.prevent>
        <!-- 手机号 -->
        <el-form-item prop="phone">
          <el-autocomplete
            v-model="loginForm.phone"
            :fetch-suggestions="querySearch"
            :trigger-on-focus="true"
            @select="handlePhoneChange"
            @input="clearFieldError('phone')"
            placeholder="手机号"
            clearable
            popper-class="login-autocomplete-popper"
          >
            <template #prefix>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                <rect x="5" y="2" width="14" height="20" rx="2"/>
                <line x1="12" y1="18" x2="12" y2="18.01"/>
              </svg>
            </template>
            <template #default="{ item }">
              <div class="saved-account-item">
                <span>{{ item.label }}</span>
                <span class="delete-btn" @click.stop="deleteSavedAccount(item.value)">删除</span>
              </div>
            </template>
          </el-autocomplete>
        </el-form-item>

        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            show-password
            @input="clearFieldError('password')"
          >
            <template #prefix>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                <rect x="3" y="11" width="18" height="11" rx="2"/>
                <path d="M7 11V7a5 5 0 0110 0v4"/>
              </svg>
            </template>
          </el-input>
        </el-form-item>

        <!-- 验证码 -->
        <el-form-item prop="captcha">
          <div class="captcha-row">
            <el-input
              v-model="loginForm.captcha"
              placeholder="验证码"
              @input="(val) => { loginForm.captcha = val.toUpperCase(); clearFieldError('captcha') }"
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
              @click="refreshCaptcha"
            />
            <button class="captcha-refresh" :class="{ 'is-spinning': captchaSpinning }" @click.prevent="refreshCaptcha" title="刷新验证码">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M23 4v6h-6"/>
                <path d="M20.49 15a9 9 0 11-2.12-9.36L23 10"/>
              </svg>
            </button>
          </div>
        </el-form-item>

        <!-- 记住密码 + 忘记密码 -->
        <div class="remember-row">
          <el-checkbox v-model="rememberPassword">记住密码</el-checkbox>
          <a class="forgot-link" @click="forgotPassword">忘记密码？</a>
        </div>

        <!-- 用户协议 -->
        <div class="agreement-row">
          <el-checkbox v-model="agreedToTerms">
            <span class="agreement-text">
              我已阅读并同意
              <a class="agreement-link" @click.stop="showAgreement('user')">《用户协议》</a>
              和
              <a class="agreement-link" @click.stop="showAgreement('privacy')">《隐私政策》</a>
            </span>
          </el-checkbox>
        </div>

        <!-- 登录按钮 -->
        <button class="login-btn" @click="submitForm" :disabled="showLoading">
          <span>登 录</span>
        </button>
      </el-form>

      <!-- 第三方登录 -->
      <div class="third-party">
        <div class="divider-line">
          <span>其他方式</span>
        </div>
        <div class="social-btns">
          <button class="social-btn wechat-btn" @click="wechatScanLogin" title="微信登录">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
              <path d="M9.5 4C5.36 4 2 6.69 2 10c0 1.89 1.08 3.56 2.78 4.66L4 17l2.5-1.18C7.45 16.07 8.46 16.2 9.5 16.2c.34 0 .67-.02 1-.06C10.17 15.7 10 15.12 10 14.5 10 11.47 12.69 9 16 9c.35 0 .69.03 1.02.08C16.43 6.15 13.27 4 9.5 4zM7 9a1 1 0 110-2 1 1 0 010 2zm5 0a1 1 0 110-2 1 1 0 010 2z" fill="#07C160"/>
              <path d="M22 14.5c0-2.49-2.46-4.5-5.5-4.5S11 12.01 11 14.5 13.46 19 16.5 19c.86 0 1.67-.15 2.39-.42L21 20l-.58-2.11C21.37 16.95 22 15.79 22 14.5zm-7-1a.75.75 0 110-1.5.75.75 0 010 1.5zm3.5 0a.75.75 0 110-1.5.75.75 0 010 1.5z" fill="#07C160"/>
            </svg>
          </button>
          <button class="social-btn qq-btn" @click="thirdPartyLogin('QQ')" title="QQ登录">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M12 2C8.13 2 5 5.13 5 9.5c0 2.38 1 4.28 2.5 5.5v3.5l2.83-1.42C11.18 17.36 11.57 17.5 12 17.5c.43 0 .82-.14 1.67-.42L16.5 18.5V15c1.5-1.22 2.5-3.12 2.5-5.5C19 5.13 15.87 2 12 2z" fill="#12B7F5"/>
              <circle cx="9.8" cy="9" r="1.3" fill="white"/>
              <circle cx="14.2" cy="9" r="1.3" fill="white"/>
              <path d="M7 15.5c-1.5 1.5-1 3.5.5 4.5" stroke="#12B7F5" stroke-width="1.2" stroke-linecap="round"/>
              <path d="M17 15.5c1.5 1.5 1 3.5-.5 4.5" stroke="#12B7F5" stroke-width="1.2" stroke-linecap="round"/>
              <ellipse cx="12" cy="14" rx="2.5" ry="1.2" fill="#FFD54F" opacity="0.8"/>
              <path d="M9.5 21c.83.5 1.5.5 2.5.5s1.67 0 2.5-.5" stroke="#12B7F5" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- 底部链接 -->
      <div class="bottom-links">
        <span>没有账号？</span>
        <a class="link" @click="toRegister">立即注册</a>
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
import { useAuthStore } from '../../store/authStore'
import { useUserStore } from '../../store/userStore'
import { useWindowControl } from '../../composables/useWindowControl'
import WindowTitleBar from '../../components/WindowTitleBar.vue'

const router = useRouter()
const { expandToMain } = useWindowControl()

// 登录表单数据
const loginForm = reactive({
  phone: '',
  password: '',
  captcha: ''
})

// 保存的账号列表
const savedAccounts = ref([])

// 记住密码选项
const rememberPassword = ref(false)

// 用户协议勾选
const agreedToTerms = ref(false)

// 表单验证规则
const loginRules = reactive({
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3456789]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
})

// 验证码相关
const captchaBase64 = ref('')
const checkCodeKey = ref('')

const authStore = useAuthStore()
const userStore = useUserStore()

// 从后端获取验证码
const captchaSpinning = ref(false)

const generateCaptcha = async () => {
  try {
    const response = await axios.get(`${API_CONFIG.baseURL}/v1/captcha/checkCode`)
    const result = response.data.data
    captchaBase64.value = 'data:image/png;base64,' + result.checkCode
    checkCodeKey.value = result.checkCodeKey
    const isDevMode = import.meta.env.MODE === 'development' || import.meta.env.DEV
    if (isDevMode && result.captchaAnswer) {
      loginForm.captcha = result.captchaAnswer.toUpperCase()
    }
  } catch (error) {
    console.error('获取验证码失败:', error)
    ElMessage.error('获取验证码失败，请稍后重试')
    captchaBase64.value = ''
    checkCodeKey.value = ''
  }
}

const refreshCaptcha = async () => {
  if (captchaSpinning.value) return
  captchaSpinning.value = true
  await generateCaptcha()
  loginForm.captcha = ''
  setTimeout(() => { captchaSpinning.value = false }, 500)
}

// 表单引用
const loginFormRef = ref(null)

// 清除单个字段的验证错误
const clearFieldError = (fieldName) => {
  if (loginFormRef.value) {
    loginFormRef.value.clearValidate([fieldName])
  }
}

// 加载动画控制
const showLoading = ref(false)

// 页面加载时生成验证码并读取保存的账号信息
onMounted(async () => {
  generateCaptcha()
  await loadSavedAccounts()
  autofillLastAccount()
})

// 监听路由变化
watch(
  () => router.currentRoute.value.path,
  async (newPath) => {
    if (newPath === '/login') {
      generateCaptcha()
      showLoading.value = false
      if (loginFormRef.value) {
        loginFormRef.value.resetFields()
      }
      await loadSavedAccounts()
      setTimeout(() => autofillLastAccount(), 0)
    }
  }
)

// 读取保存的账号信息
const loadSavedAccounts = async () => {
  try {
    const accounts = await window.api.store.get('savedAccounts')
    if (accounts && Array.isArray(accounts)) {
      savedAccounts.value = accounts
        .map((account) => {
          if (account.username && !account.phone) {
            return { phone: account.username, password: account.password || '' }
          }
          return account
        })
        .filter((account) => account.phone)
    } else {
      savedAccounts.value = []
    }
  } catch (error) {
    console.error('读取保存的账号失败:', error)
    savedAccounts.value = []
  }
}

// 自动填充最后登录的账号
const autofillLastAccount = () => {
  if (savedAccounts.value.length > 0) {
    const lastAccount = savedAccounts.value[savedAccounts.value.length - 1]
    if (lastAccount && lastAccount.phone) {
      loginForm.phone = lastAccount.phone
      if (lastAccount.password) {
        loginForm.password = lastAccount.password
        rememberPassword.value = true
      }
    }
  }
}

// 手机号搜索函数
const querySearch = (queryString, cb) => {
  const results = queryString
    ? savedAccounts.value.filter((account) => account.phone && account.phone.includes(queryString))
    : savedAccounts.value
  cb(results.map((account) => ({ value: account.phone, label: account.phone })))
}

// 当手机号变化或选择时，自动填充密码
const handlePhoneChange = (value) => {
  const selectedPhone = typeof value === 'object' ? value.value : value
  if (!selectedPhone) return

  const account = savedAccounts.value.find((acc) => acc.phone === selectedPhone)
  if (account) {
    loginForm.password = account.password || ''
    rememberPassword.value = !!account.password
  } else {
    loginForm.password = ''
    rememberPassword.value = false
  }
  clearFieldError('phone')
  clearFieldError('password')
  if (loginFormRef.value) {
    loginFormRef.value.clearValidate()
  }
}

// 删除保存的账号
const deleteSavedAccount = async (phone) => {
  savedAccounts.value = savedAccounts.value.filter((account) => account.phone !== phone)
  await window.api.store.set('savedAccounts', JSON.parse(JSON.stringify(savedAccounts.value)))
  loginForm.password = ''
  rememberPassword.value = false
  if (loginForm.phone === phone) {
    loginForm.phone = ''
  }
  ElMessage.success('已删除保存的账号')
}

// 提交表单
const submitForm = async () => {
  if (!agreedToTerms.value) {
    ElMessage.warning('请先阅读并同意用户协议和隐私政策')
    return
  }

  if (loginFormRef.value) {
    loginFormRef.value.validate(async (valid) => {
      if (valid) {
        try {
          const response = await axios.post(`${API_CONFIG.baseURL}${API_CONFIG.user.login}`, {
            phone: loginForm.phone,
            password: loginForm.password,
            captcha: loginForm.captcha,
            checkCodeKey: checkCodeKey.value
          })

          if (response.data.code !== '200') {
            throw { response: { data: response.data } }
          }

          const responseData = response.data.data
          const token = responseData.token
          const userData = responseData.user

          authStore.setToken(token)
          authStore.setUserId(userData.userId)
          authStore.setPhone(userData.phone)
          userStore.setUserInfo(userData)

          localStorage.setItem('userId', userData.userId)
          localStorage.setItem('token', token)
          localStorage.setItem('phone', userData.phone)
          localStorage.setItem(
            'userInfo',
            JSON.stringify({
              userId: userData.userId,
              phone: userData.phone,
              nickname: userData.nickname || '',
              avatar: userData.avatar || ''
            })
          )

          // 保存账号信息
          const accountIndex = savedAccounts.value.findIndex((acc) => acc.phone === loginForm.phone)
          if (accountIndex !== -1) {
            savedAccounts.value[accountIndex].password = rememberPassword.value ? loginForm.password : ''
          } else {
            savedAccounts.value.push({
              phone: loginForm.phone,
              password: rememberPassword.value ? loginForm.password : ''
            })
          }
          await window.api.store.set('savedAccounts', JSON.parse(JSON.stringify(savedAccounts.value)))
          ElMessage.success('登录成功！')

          // 放大窗口并跳转
          showLoading.value = true
          setTimeout(async () => {
            await expandToMain()
            router.push('/user/home')
          }, 300)
        } catch (error) {
          console.error('登录失败:', error)
          ElMessage.error(error.response?.data?.message || '登录失败，请检查验证码或账号密码是否正确')
          generateCaptcha()
        }
      } else {
        ElMessage.error('表单验证失败，请检查输入')
        generateCaptcha()
      }
    })
  }
}

// 跳转到注册页面
const toRegister = () => {
  if (loginFormRef.value) {
    loginFormRef.value.resetFields()
  }
  router.push('/register')
}

// 忘记密码
const forgotPassword = () => {
  ElMessage.info('忘记密码功能开发中，请联系客服重置密码')
}

// 显示协议
const showAgreement = (type) => {
  const title = type === 'user' ? '用户协议' : '隐私政策'
  ElMessage.info(`${title}页面开发中...`)
}

// 微信扫码登录
const wechatScanLogin = () => {
  ElMessage.info('微信扫码登录功能开发中...')
}

// 第三方登录
const thirdPartyLogin = (type) => {
  ElMessage.info(`${type}登录功能开发中...`)
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

.login-container {
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

// === 柔和装饰光球 ===
.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
}

.orb-1 {
  width: 280px;
  height: 280px;
  background: radial-gradient(circle, rgba(242, 120, 75, 0.2) 0%, transparent 70%);
  top: -80px;
  right: -60px;
}

.orb-2 {
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(255, 154, 118, 0.18) 0%, transparent 70%);
  bottom: 40px;
  left: -60px;
}

.orb-3 {
  width: 150px;
  height: 150px;
  background: radial-gradient(circle, rgba(242, 120, 75, 0.12) 0%, transparent 70%);
  top: 40%;
  left: 60%;
}

// === 品牌区域 ===
.brand-area {
  text-align: center;
  margin-top: 8px;
  position: relative;
  z-index: 2;
}

.brand-logo {
  animation: breathe 4s ease-in-out infinite;
}

@keyframes breathe {
  0%, 100% { transform: scale(1); opacity: 0.9; }
  50% { transform: scale(1.04); opacity: 1; }
}

.brand-name {
  font-size: 22px;
  font-weight: 700;
  color: @text-dark;
  margin: 6px 0 2px;
  letter-spacing: 3px;
}

.brand-tagline {
  font-size: 11px;
  color: @text-muted;
  letter-spacing: 2px;
  font-weight: 400;
}

// === 毛玻璃卡片 ===
.glass-card {
  width: calc(100% - 40px);
  margin: 14px 20px 16px;
  padding: 22px 24px 16px;
  position: relative;
  z-index: 2;
  flex: 1;
  display: flex;
  flex-direction: column;
}

// === Element Plus 输入框覆盖 ===
:deep(.el-form-item) {
  margin-bottom: 10px;
}

:deep(.el-form-item__error) {
  color: #E07060;
  font-size: 11px;
  padding-top: 2px;
}

:deep(.el-input__wrapper),
:deep(.el-autocomplete .el-input__wrapper) {
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

:deep(.el-input__clear) {
  color: @text-muted;
  &:hover { color: @text-dark; }
}

:deep(.el-input__password) {
  color: @text-muted;
  &:hover { color: @text-dark; }
}

// 下拉菜单项样式
.saved-account-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;

  .delete-btn {
    font-size: 11px;
    color: #E07060;
    cursor: pointer;
    padding: 2px 8px;
    border-radius: 4px;
    transition: all 0.2s;

    &:hover {
      background: rgba(224, 112, 96, 0.08);
    }
  }
}

// === 验证码行 ===
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
  transition: color 0.2s ease;
  flex-shrink: 0;

  &:hover {
    color: @accent;
  }

  &.is-spinning svg {
    animation: spin-once 0.5s ease-out;
  }
}

@keyframes spin-once {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

// === 记住密码 + 忘记密码 ===
.remember-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  padding: 0 2px;

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

.forgot-link {
  font-size: 12px;
  color: @text-muted;
  cursor: pointer;
  text-decoration: none;
  transition: color 0.2s;

  &:hover {
    color: @accent;
  }
}

// === 用户协议 ===
.agreement-row {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-bottom: 10px;
  padding: 0 2px;

  :deep(.el-checkbox__label) {
    color: @text-muted;
    font-size: 11px;
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

.agreement-text {
  font-size: 11px;
}

.agreement-link {
  color: @accent;
  cursor: pointer;
  text-decoration: none;
  border-bottom: 1px solid rgba(242, 120, 75, 0.3);
  transition: border-color 0.2s;

  &:hover {
    border-bottom-color: @accent;
  }
}

// === 登录按钮 ===
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
  box-shadow: 0 4px 14px rgba(242, 120, 75, 0.3);

  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 6px 20px rgba(242, 120, 75, 0.4);
  }

  &:active:not(:disabled) {
    transform: translateY(0) scale(0.98);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

// === 第三方登录 ===
.third-party {
  margin-top: auto;
}

.divider-line {
  display: flex;
  align-items: center;
  margin: 14px 0 10px;

  &::before,
  &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: rgba(0, 0, 0, 0.06);
  }

  span {
    padding: 0 12px;
    font-size: 11px;
    color: @text-placeholder;
    white-space: nowrap;
  }
}

.social-btns {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.social-btn {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  border: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(255, 255, 255, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }

  &:active {
    transform: scale(0.95);
  }
}

// === 底部链接 ===
.bottom-links {
  text-align: center;
  margin-top: 14px;
  font-size: 12px;
  color: @text-muted;

  .link {
    color: @accent;
    cursor: pointer;
    text-decoration: none;
    font-weight: 500;
    transition: opacity 0.2s;

    &:hover {
      opacity: 0.8;
    }
  }
}
</style>

<!-- 非 scoped 样式：下拉菜单毛玻璃 -->
<style lang="less">
.login-autocomplete-popper {
  &.el-popper {
    background: rgba(255, 255, 255, 0.85) !important;
    backdrop-filter: blur(20px) saturate(1.2);
    -webkit-backdrop-filter: blur(20px) saturate(1.2);
    border: 1px solid rgba(0, 0, 0, 0.06) !important;
    border-radius: 10px !important;
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08) !important;
  }

  .el-autocomplete-suggestion {
    background: transparent !important;
    border: none !important;
    border-radius: 10px !important;
    padding: 4px 0 !important;
  }

  .el-autocomplete-suggestion__wrap {
    max-height: 180px;
    padding: 0 !important;
  }

  .el-autocomplete-suggestion li {
    color: #2C3E50 !important;
    font-size: 13px;
    line-height: 36px;
    padding: 0 14px;
    transition: background 0.15s;
    border-radius: 6px;
    margin: 0 4px;

    &:hover {
      background: rgba(242, 120, 75, 0.08) !important;
    }
  }

  .el-popper__arrow::before {
    background: rgba(255, 255, 255, 0.85) !important;
    border: 1px solid rgba(0, 0, 0, 0.06) !important;
  }
}
</style>
