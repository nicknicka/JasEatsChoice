<template>
  <div class="login-container">
    <!-- 装饰光球 -->
    <div class="orb orb-1"></div>
    <div class="orb orb-2"></div>
    <div class="orb orb-3"></div>

    <!-- 自定义标题栏 -->
    <WindowTitleBar />

    <!-- 品牌区域：与表单共用同一 400px 中轴（§4.4） -->
    <div class="brand-area">
      <div class="brand-logo">
        <svg width="44" height="44" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M6 20C6 20 8 34 24 34C40 34 42 20 42 20" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" fill="none"/>
          <path d="M4 20H44" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.6"/>
          <path d="M20 16L30 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.8"/>
          <path d="M24 16L36 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.8"/>
          <path d="M18 38H30" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.4"/>
        </svg>
      </div>
      <h1 class="brand-name">佳食宜选</h1>
      <p class="brand-tagline">美食由你选择</p>
    </div>

    <!-- 表单卡片：认证类 400px 窄列居中（§4.4） -->
    <AppFormContainer variant="auth" class="login-form-container">
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

      <template #actions>
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
      </template>
    </AppFormContainer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { API_CONFIG } from '../../config'
import { useAuthStore } from '../../store/authStore'
import { useUserStore } from '../../store/userStore'
import { useWindowControl } from '../../composables/useWindowControl'
import { useLoginTransition } from '../../composables/useLoginTransition'
import WindowTitleBar from '../../components/WindowTitleBar.vue'
import AppFormContainer from '../../components/common/AppFormContainer.vue'
import { resolveAndStorePublicIp } from '../../utils/publicIp'
import { safeRedirect } from '../../utils/authSession'

const router = useRouter()
const route = useRoute()
const { expandToMain } = useWindowControl()
const { showTransition, hideTransition } = useLoginTransition()

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

const applyAutoCaptcha = (result) => {
  const isDevMode = import.meta.env.MODE === 'development' || import.meta.env.DEV
  if (isDevMode && result.captchaAnswer) {
    loginForm.captcha = result.captchaAnswer.toUpperCase()
    return true
  }

  if (result.fixedCaptchaEnabled === 'true' && result.fixedCaptchaCode) {
    loginForm.captcha = result.fixedCaptchaCode.toUpperCase()
    return true
  }

  loginForm.captcha = ''
  return false
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
    captchaBase64.value = ''
    checkCodeKey.value = ''
  }
}

const refreshCaptcha = async () => {
  if (captchaSpinning.value) return
  captchaSpinning.value = true
  await generateCaptcha()
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

          // 登录后异步刷新公网IP，失败不影响登录主流程
          resolveAndStorePublicIp()
            .then((ip) => {
              if (!ip) {
                console.warn('[定位] 登录后未获取到公网IP，将使用后端请求头兜底定位')
              }
            })
            .catch((error) => {
              console.warn('[定位] 登录后获取公网IP失败:', error?.message || error)
            })

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

          // 显示过渡动画覆盖层
          showTransition()
          showLoading.value = true

          // 等覆盖层动画显示后再拉伸窗口并跳转
          setTimeout(async () => {
            try {
              await expandToMain()
              const redirect =
                safeRedirect(route.query.redirect, '/user') ||
                safeRedirect(route.query.redirect, '/merchant') ||
                '/user/home'
              await router.push(redirect)
            } catch (error) {
              console.error('登录后跳转失败:', error)
              ElMessage.error('登录成功，但页面跳转失败，请重试')
              hideTransition()
              showLoading.value = false
            }
          }, 400)
        } catch (error) {
          console.error('登录失败:', error)
          ElMessage.error(error.response?.data?.message || '登录失败，请检查验证码或账号密码是否正确')
          generateCaptcha()
          showLoading.value = false
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
  router.push('/forgot-password')
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
// 颜色、圆角、间距一律取 tokens.css 的 CSS 变量，页面内不写裸色值（DESIGN-BASELINE §7.1）。
.login-container {
  width: 100%;
  height: 100vh;
  background: var(--canvas);
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  overflow: hidden;
  font-family: var(--font-sans);
}

// === 柔和装饰光球：保留暖橙渐变基调（§2），色调走 --brand-*，透明度交给 opacity ===
.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
}

.orb-1 {
  width: 280px;
  height: 280px;
  background: radial-gradient(circle, var(--brand-500) 0%, transparent 70%);
  opacity: 0.2;
  top: -80px;
  right: -60px;
}

.orb-2 {
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, var(--brand-300) 0%, transparent 70%);
  opacity: 0.18;
  bottom: 40px;
  left: -60px;
}

.orb-3 {
  width: 150px;
  height: 150px;
  background: radial-gradient(circle, var(--brand-500) 0%, transparent 70%);
  opacity: 0.12;
  top: 40%;
  left: 60%;
}

// === 品牌区域：与表单共用同一 400px 中轴（§4.4） ===
.brand-area {
  width: var(--form-width-auth);
  max-width: calc(100% - var(--space-10));
  margin-block: auto var(--space-8);
  text-align: center;
  position: relative;
  z-index: 2;
}

.brand-logo {
  color: var(--brand-400);
  animation: breathe 4s ease-in-out infinite;
}

@keyframes breathe {
  0%, 100% { transform: scale(1); opacity: 0.9; }
  50% { transform: scale(1.04); opacity: 1; }
}

.brand-name {
  font-size: var(--text-h1);
  font-weight: var(--weight-bold);
  color: var(--ink-900);
  margin: var(--space-1) 0 var(--space-1);
  letter-spacing: 3px;
}

.brand-tagline {
  font-size: var(--text-body-sm);
  color: var(--ink-500);
  letter-spacing: 2px;
  font-weight: var(--weight-regular);
}

// === 表单卡片：认证类 400px 窄列居中，动作区紧跟主按钮（§4.4） ===
.login-form-container {
  position: relative;
  z-index: 2;
  margin-block-end: auto;
}

// === Element Plus 输入框覆盖 ===
:deep(.el-form-item) {
  margin-bottom: var(--space-4);
}

:deep(.el-form-item__error) {
  color: var(--danger);
  font-size: var(--text-caption);
  padding-top: 2px;
}

:deep(.el-input__wrapper),
:deep(.el-autocomplete .el-input__wrapper) {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  padding: 7px 12px;
  transition:
    border-color var(--duration-fast) var(--ease-out),
    box-shadow var(--duration-fast) var(--ease-out);

  &:hover {
    border-color: var(--line-strong);
  }

  &.is-focus {
    border-color: var(--brand-500);
    box-shadow: 0 0 0 3px var(--brand-100);
  }
}

:deep(.el-input__inner) {
  color: var(--ink-900);
  font-size: var(--text-body-sm);
  caret-color: var(--brand-500);

  &::placeholder {
    color: var(--ink-400);
    font-size: var(--text-body-sm);
  }
}

:deep(.el-input__prefix) {
  color: var(--ink-500);
  margin-right: 6px;
}

:deep(.el-input__suffix) {
  color: var(--ink-500);
}

:deep(.el-input__clear) {
  color: var(--ink-500);
  &:hover { color: var(--ink-900); }
}

:deep(.el-input__password) {
  color: var(--ink-500);
  &:hover { color: var(--ink-900); }
}

// 下拉菜单项样式
.saved-account-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;

  .delete-btn {
    font-size: var(--text-body-sm);
    color: var(--danger);
    cursor: pointer;
    padding: 2px 8px;
    border-radius: var(--radius-sm);
    transition: background var(--duration-fast) var(--ease-out);

    &:hover {
      background: var(--danger-bg);
    }
  }
}

// === 验证码行：输入按内容收窄，与图形验证码、刷新同排（§4.4） ===
.captcha-row {
  display: flex;
  gap: var(--space-2);
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
  border-radius: var(--radius-sm);
  border: 1px solid var(--line);
  background: var(--canvas-sunken);
  cursor: pointer;
  object-fit: contain;
  transition: border-color var(--duration-fast) var(--ease-out);
  flex-shrink: 0;
  padding: 2px;

  &:hover {
    border-color: var(--brand-500);
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
  color: var(--ink-500);
  cursor: pointer;
  transition: color var(--duration-fast) var(--ease-out);
  flex-shrink: 0;

  &:hover {
    color: var(--brand-500);
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
  margin-bottom: var(--space-3);
  padding: 0 2px;

  :deep(.el-checkbox__label) {
    color: var(--ink-500);
    font-size: var(--text-body-sm);
  }

  :deep(.el-checkbox__inner) {
    background: transparent;
    border-color: var(--line-strong);
    border-radius: 3px;
  }

  :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
    background: var(--brand-500);
    border-color: var(--brand-500);

    &::after {
      border-color: var(--surface);
    }
  }

  :deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
    color: var(--ink-900);
  }
}

.forgot-link {
  font-size: var(--text-body-sm);
  color: var(--ink-500);
  cursor: pointer;
  text-decoration: none;
  transition: color var(--duration-fast) var(--ease-out);

  &:hover {
    color: var(--brand-500);
  }
}

// === 用户协议 ===
.agreement-row {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-bottom: var(--space-4);
  padding: 0 2px;

  :deep(.el-checkbox__label) {
    color: var(--ink-500);
    font-size: var(--text-body-sm);
  }

  :deep(.el-checkbox__inner) {
    background: transparent;
    border-color: var(--line-strong);
    border-radius: 3px;
  }

  :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
    background: var(--brand-500);
    border-color: var(--brand-500);

    &::after {
      border-color: var(--surface);
    }
  }

  :deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
    color: var(--ink-900);
  }
}

.agreement-text {
  font-size: var(--text-body-sm);
}

.agreement-link {
  color: var(--brand-500);
  cursor: pointer;
  text-decoration: none;
  border-bottom: 1px solid var(--brand-200);
  transition: border-color var(--duration-fast) var(--ease-out);

  &:hover {
    border-bottom-color: var(--brand-500);
  }
}

// === 登录按钮：唯一实心主按钮，取 --brand-500（§4.1） ===
.login-btn {
  width: 100%;
  height: 42px;
  border: none;
  border-radius: var(--radius-md);
  background: var(--brand-500);
  color: var(--surface);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  letter-spacing: 6px;
  cursor: pointer;
  transition:
    background var(--duration-base) var(--ease-out),
    box-shadow var(--duration-base) var(--ease-out);

  &:hover:not(:disabled) {
    background: var(--brand-600);
    box-shadow: var(--shadow-md);
  }

  &:active:not(:disabled) {
    background: var(--brand-600);
  }

  &:disabled {
    background: var(--neutral-bg);
    color: var(--ink-400);
    cursor: not-allowed;
  }
}

// === 第三方登录：收进表单列、紧跟主按钮（§4.4） ===
.third-party {
  margin: 0;
}

.divider-line {
  display: flex;
  align-items: center;
  margin: 0 0 var(--space-3);

  &::before,
  &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: var(--line);
  }

  span {
    padding: 0 var(--space-3);
    font-size: var(--text-body-sm);
    color: var(--ink-400);
    white-space: nowrap;
  }
}

.social-btns {
  display: flex;
  justify-content: center;
  gap: var(--space-5);
}

.social-btn {
  width: 42px;
  height: 42px;
  border-radius: var(--radius-full);
  border: 1px solid var(--line);
  background: var(--surface);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition:
    border-color var(--duration-fast) var(--ease-out),
    box-shadow var(--duration-fast) var(--ease-out),
    transform var(--duration-fast) var(--ease-out);

  &:hover {
    border-color: var(--line-strong);
    box-shadow: var(--shadow-md);
  }

  &:active {
    transform: scale(0.95);
  }
}

// === 底部链接 ===
.bottom-links {
  text-align: center;
  margin-top: 0;
  font-size: var(--text-body-sm);
  color: var(--ink-500);

  .link {
    color: var(--brand-500);
    cursor: pointer;
    text-decoration: none;
    font-weight: var(--weight-medium);
    transition: color var(--duration-fast) var(--ease-out);

    &:hover {
      color: var(--brand-600);
    }
  }
}
</style>

<!-- 非 scoped 样式：下拉菜单 -->
<style lang="less">
.login-autocomplete-popper {
  &.el-popper {
    background: var(--surface) !important;
    border: 1px solid var(--line) !important;
    border-radius: var(--radius-md) !important;
    box-shadow: var(--shadow-lg) !important;
  }

  .el-autocomplete-suggestion {
    background: transparent !important;
    border: none !important;
    border-radius: var(--radius-md) !important;
    padding: var(--space-1) 0 !important;
  }

  .el-autocomplete-suggestion__wrap {
    max-height: 180px;
    padding: 0 !important;
  }

  .el-autocomplete-suggestion li {
    color: var(--ink-900) !important;
    font-size: var(--text-body-sm);
    line-height: 36px;
    padding: 0 14px;
    transition: background var(--duration-fast) var(--ease-out);
    border-radius: var(--radius-sm);
    margin: 0 var(--space-1);

    &:hover {
      background: var(--brand-50) !important;
    }
  }

  .el-popper__arrow::before {
    background: var(--surface) !important;
    border: 1px solid var(--line) !important;
  }
}
</style>
