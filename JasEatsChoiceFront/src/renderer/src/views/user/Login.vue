<template>
  <div class="login-container">
    <!-- 加载动画遮罩 -->
    <div class="loading-mask" v-if="showLoading">
      <div class="loading-content">
        <svg
          t="1578185648636"
          class="loading-svg"
          viewBox="0 0 1024 1024"
          version="1.1"
          xmlns="http://www.w3.org/2000/svg"
          p-id="3603"
          width="80"
          height="80"
        >
          <path
            d="M512 96c-229.888 0-416 186.112-416 416s186.112 416 416 416 416-186.112 416-416S741.888 96 512 96z m0 768c-194.688 0-352-157.312-352-352s157.312-352 352-352 352 157.312 352 352-157.312 352-352 352z"
            p-id="3604"
            fill="#fff"
          ></path>
          <path
            d="M671.808 512c0-88.256-71.552-159.808-159.808-159.808V256c123.776 0 224 100.224 224 224 0 5.184-0.128 10.304-0.384 15.424-2.048 39.36-16.384 75.712-39.488 105.408l-87.488-87.488zM352.192 512c0 88.256 71.552 159.808 159.808 159.808V768c-123.776 0-224-100.224-224-224 0-5.184 0.128-10.304 0.384-15.424 2.048-39.36 16.384-75.712 39.488-105.408l87.488 87.488z"
            p-id="3605"
            fill="#409eff"
            animation-name="loadingRotate"
            animation-duration="1s"
            animation-iteration-count="infinite"
            animation-timing-function="linear"
          ></path>
        </svg>
        <p class="loading-text">正在加载...</p>
      </div>
    </div>

    <div class="login-card">
      <h2 class="login-title">用户登录</h2>

      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-width="80px">
        <el-form-item label="手机号" prop="phone">
          <el-autocomplete
            v-model="loginForm.phone"
            :fetch-suggestions="querySearch"
            :trigger-on-focus="true"
            @select="handlePhoneChange"
            @input="clearFieldError('phone')"
            placeholder="请输入手机号"
            clearable
            size="large"
          >
            <template #default="{ item }">
              <div class="saved-account-item">
                <span>{{ item.label }}</span>
                <el-link
                  type="danger"
                  size="small"
                  class="delete-account-btn"
                  @click.stop="deleteSavedAccount(item.value)"
                >
                  x
                </el-link>
              </div>
            </template>
          </el-autocomplete>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            @input="clearFieldError('password')"
          />
        </el-form-item>

        <el-form-item label="验证码" prop="captcha">
          <div style="display: flex; align-items: center">
            <el-input
              v-model="loginForm.captcha"
              placeholder="请输入验证码"
              style="width: 160px; margin-right: 10px"
              @input="
                (val) => {
                  loginForm.captcha = val.toUpperCase()
                  clearFieldError('captcha')
                }
              "
            />
            <img
              :src="captchaBase64"
              alt="验证码"
              style="width: 100px; height: 36px; background-color: #f5f7fa; cursor: pointer"
              @click="generateCaptcha"
            />
            <el-link
              type="primary"
              size="small"
              :underline="false"
              class="captcha-refresh-link"
              @click="generateCaptcha"
              >刷新</el-link
            >
          </div>
        </el-form-item>

        <el-form-item>
          <div style="display: flex; align-items: center; justify-content: center">
            <el-button type="primary" style="margin-right: 20px" @click="submitForm"
              >登录</el-button
            >
            <el-checkbox v-model="rememberPassword" size="small">记住密码</el-checkbox>
          </div>
        </el-form-item>

        <!-- 第三方登录 -->
        <el-divider>第三方登录</el-divider>

        <el-form-item>
          <el-button type="warning" block @click="wechatScanLogin">
            <svg
              t="1631581532856"
              class="icon"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
              p-id="6153"
              width="18"
              height="18"
              style="margin-right: 8px"
            >
              <path
                d="M976.9 478.8C977.2 454 953.7 435.4 928.6 435.4h-72.1c-19 0-34.4 15.6-34.4 34.6 0 19.1 15.4 34.5 34.4 34.5h72.1c8.1 0 13.2 10.1 7.9 16.3L798.6 602.1c-4.6 5.5-10.3 5.8-14.8 0.7-20.7-24.5-34.7-52.8-39.9-83.6-5-30.1 3.8-60.5 16.4-89.2 4.5-10 10.5-19.6 18-28.8 42.6-56.4 78.6-102.5 86.2-113.7 10.4-14.7 7.3-28.3-6.2-38.1-13.8-9.9-27.7-7.9-38.2 6.7-8.6 13.2-49 63.9-93.9 123.7h-37.1c-27.8 0-50.4 21.8-53.7 49.4-0.8 6.8-0.6 28.5 0.4 45.4 5.1 83.6 41.6 160 88.1 224.3 11.2 15.4 35.1 18.8 48.9 7.2 16.4-13.5 18.8-33 9.7-53-6.3-13.4-20.3-41.3-23.2-66.4 0-0.1-0.1-7.2-0.1-12.2 0-17.3 4-33.7 6.2-37.8 1.8-3.2 5.3-4.2 8.3-2.1 4.1 2.7 22.8 23.9 41.9 63.3 8 15.4 12.6 33.3 11.7 51.2-0.6 11.5-1.8 23.2-3.8 34.5-7.4 42.7-13.1 74.2-28 108.8-10.5 24.6-23.6 45.8-48.5 66.1-34.9 27.3-82.1 36.3-124.1 34.9-62.6-2-121.2-24.2-165.7-53.5-41.1-26.5-74.2-58.8-98.8-96.5 54.7 61.5 104 93.3 154.8 93.3 52.6 0.5 113.3-10.2 149.8-51.8 24.9-27.2 40.4-58.9 43.6-95.4 0.9-9.7 0.9-19.8 0-29.3-7.4-77-24.9-148.1-69.9-207.3 22.9-11.8 44.5-26.3 64.3-43.1 18.4-15.5 36.2-32.5 52.6-51.1 19.3-21.3 36-44.2 49.2-68.4 17.5-31.8 18.3-76-1.4-108.4-19.6-32.4-57.5-40.7-89.1-20.8-42.6 27.6-81.1 83.3-128.7 179.3C404.6 184 378.6 158 350.1 133.5c-45.1-41.3-94.6-51.8-145.2-51.8C84.7 81.7 0 166.4 0 274.1c0 107.7 84.7 192.4 192.3 192.4h112.6c5.8 0 10.5 4.7 10.5 10.5v1.1c0 5.8-4.7 10.5-10.5 10.5H192.3c-30.1 0-56.2 22.6-61 52.1-5.1 31.5 18.3 59 49.2 59.9 31.8 1 57.8-22.4 63.4-54.3 16.2 16.4 41.8 32.5 75.2 44.9 65.1 24.1 119.8 22.9 170.1-0.6C707 638.2 763 604.4 813 560.9c13.1 83.7 55.5 126.3 126.5 131.1 5.9 0.4 10.5 5.6 10.5 11.5 0 6-4.6 10.9-10.5 10.9-82.7 2.8-136.9-66.9-149-127.8-1.7-8.6-1.5-17.5 0.6-25.9 4.5-18.8 26.2-87.2 30.1-119.7 1.1-10.3 0.8-21.7-0.8-32.2-2.5-17.2-45.5-126.2-89.1-177.7 12.5 4.6 30.1 13.1 51.6 25.2 21.5 12.1 47.5 29.4 75.9 49.9 21.9 15.5 43.7 33.3 64.3 53.2 27.4 26 51.8 56.7 70.2 89.4 2.4 4.2 6.8 6.1 10.9 4.3 3.8-1.6 5.9-5.6 5.3-9.6L976.9 478.8z"
                fill="#00d52e"
                p-id="6154"
              ></path>
            </svg>
            微信扫码登录
          </el-button>

          <el-button type="info" block @click="thirdPartyLogin('QQ')">
            <svg
              t="1631581785467"
              class="icon"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
              p-id="7863"
              width="18"
              height="18"
              style="margin-right: 8px"
            >
              <path
                d="M511.2 81.1c-237.7 0-430.2 186.9-430.2 430.1 0 241.9 192.5 439.4 430.2 439.4 245.7 0 437.4-197.5 437.4-439.4C948.5 267.9 756.9 81.1 511.2 81.1zM750.4 617.3c-4.7 0.2-9.1 0.4-14 0.4-2.6 0-5.2-0.1-7.7-0.2 61.1-28 107.9-90.2 107.9-162.2 0-98.5-84.4-178.3-186.6-178.3-101.1 0-187.9 85.2-187.9 187.7 0 0.9 0 1.8 0 2.8 0.6 0.1 1.3 0.3 1.9 0.4 83.3 0.2 142.3 72.3 139.7 154.2-0.2 4.2 0.8 9.8 6.3 13.4 5.8 3.8 12.6 1.4 16.9-4.4 32.3-44.8 15.7-103-31.4-122.1-49.2-20-107-3.5-126.9 48.6-2.7 7.4-4.4 14.7-5 22.1-0.2 17.9 1.7 35.8 5.6 52.8 14.5 0.2 27.4 0.2 39.8 0.1 24.6 0 45.1-17.5 45.1-40.6 0-11.4-6.8-21.9-17.7-29.7-0.7 3.8-1.4 7.7-2.1 11.5-7.4 40-44.2 69.2-86.2 69.2-49.6 0-91.1-40.7-91.1-91.5 0-50.9 42.5-91.3 91.2-91.3 37.1 0 68.7 20.1 85 51.8 0.8 1.8 2.1 3.1 3.6 3.8 4.4 2.2 6.6 7.2 5.4 12-0.4 1.5-1.2 3.2-2.2 5.3-18.6 39.5-59 68.7-103.3 75.3v2.2h0-0.1C377 616.7 319.9 555.5 319.9 476c0-86.1 73.5-154.6 159.3-154.6 85.9 0 154.6 73.5 154.6 154.6 0 20.2-4.2 40.4-12.4 60.7C779.6 569.2 774.8 589.7 750.4 617.3z"
                p-id="7864"
                fill="#00c5ff"
              ></path>
            </svg>
            QQ登录
          </el-button>
        </el-form-item>

        <div class="register-link">
          <span>没有账号？</span>
          <el-link type="primary" @click="toRegister">立即注册</el-link>
        </div>

        <div class="admin-link">
          <el-link type="info" @click="toAdminLogin">管理员登录</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElForm, ElFormItem, ElInput, ElButton } from 'element-plus'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { API_CONFIG } from '../../config'
import { useAuthStore } from '../../store/authStore'
import { useUserStore } from '../../store/userStore'

const router = useRouter()

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

// 表单验证规则
const loginRules = reactive({
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    {
      pattern: /^1[3456789]\d{9}$/,
      message: '请输入正确的手机号码',
      trigger: 'blur'
    }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
})

// 验证码相关
const captchaBase64 = ref('') // 后端返回的验证码图片Base64
const checkCodeKey = ref('') // 后端返回的验证码会话key

const authStore = useAuthStore()
const userStore = useUserStore()

// 从后端获取验证码
const generateCaptcha = async () => {
  try {
    const response = await axios.get(`${API_CONFIG.baseURL}/v1/captcha/checkCode`)
    const result = response.data.data
    // 添加base64图片前缀，否则浏览器无法识别
    captchaBase64.value = 'data:image/png;base64,' + result.checkCode
    checkCodeKey.value = result.checkCodeKey
    // 开发者模式下自动填充验证码答案
    const isDevMode = import.meta.env.MODE === 'development' || import.meta.env.DEV
    if (isDevMode && result.captchaAnswer) {
      loginForm.captcha = result.captchaAnswer.toUpperCase()
      console.log('开发者模式: 自动填充验证码答案 ->', result.captchaAnswer)
    }
  } catch (error) {
    console.error('获取验证码失败:', error)
    ElMessage.error('获取验证码失败，请稍后重试')
    // 失败时使用本地模拟验证码作为 fallback
    captchaBase64.value = ''
    checkCodeKey.value = ''
  }
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
  generateCaptcha() // 生成验证码
  await loadSavedAccounts()
  // 自动填充最后登录的账号
  autofillLastAccount()
})

// 监听路由变化，确保每次进入页面都刷新验证码、重置表单和清除加载状态
watch(
  () => router.currentRoute.value.path,
  async (newPath) => {
    if (newPath === '/login') {
      generateCaptcha()
      // 清除加载状态
      showLoading.value = false
      // 重置表单
      if (loginFormRef.value) {
        loginFormRef.value.resetFields()
      }
      // 重新加载保存的账号并自动填充（必须在重置表单之后）
      await loadSavedAccounts()
      // 使用 setTimeout 确保在 resetFields 完成后再填充
      setTimeout(() => {
        autofillLastAccount()
      }, 0)
    }
  }
)

// 读取保存的账号信息
const loadSavedAccounts = async () => {
  try {
    // 从 electron-store 读取账号信息
    const accounts = await window.api.store.get('savedAccounts')
    if (accounts && Array.isArray(accounts)) {
      // 解析并清理旧格式的数据
      const parsedAccounts = accounts
      // 过滤掉没有phone字段的旧数据，并将旧的username字段转换为phone字段
      savedAccounts.value = parsedAccounts
        .map((account) => {
          // 如果账号有username字段但没有phone字段，将username转换为phone
          if (account.username && !account.phone) {
            return {
              phone: account.username,
              password: account.password || ''
            }
          }
          // 只保留有phone字段的账号
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
    // 获取最后一个保存的账号（通常是最后登录的）
    const lastAccount = savedAccounts.value[savedAccounts.value.length - 1]
    if (lastAccount && lastAccount.phone) {
      loginForm.phone = lastAccount.phone
      // 如果有保存的密码，则自动填充密码并勾选记住密码
      if (lastAccount.password) {
        loginForm.password = lastAccount.password
        rememberPassword.value = true
      }
    }
  }
}

// 手机号搜索函数
const querySearch = (queryString, cb) => {
  // 根据输入的手机号过滤已保存的账号
  const results = queryString
    ? savedAccounts.value.filter((account) => account.phone && account.phone.includes(queryString))
    : savedAccounts.value // 当用户没有输入时，显示所有保存的账号
  // 返回处理后的结果，注意需要将结果转换为el-autocomplete需要的格式
  cb(results.map((account) => ({ value: account.phone, label: account.phone })))
}

// 当手机号变化或选择时，自动填充密码
const handlePhoneChange = (value) => {
  // 确保value是字符串类型
  const selectedPhone = typeof value === 'object' ? value.value : value
  if (!selectedPhone) return

  const account = savedAccounts.value.find((acc) => acc.phone === selectedPhone)
  if (account) {
    loginForm.password = account.password || ''
    rememberPassword.value = !!account.password // 如果有保存的密码，则自动勾选记住密码
  } else {
    loginForm.password = ''
    rememberPassword.value = false
  }
  // 清除手机号和密码字段的验证错误
  clearFieldError('phone')
  clearFieldError('password')
  // 清除整个表单的验证错误
  if (loginFormRef.value) {
    loginFormRef.value.clearValidate()
  }
}

// 删除保存的账号
const deleteSavedAccount = async (phone) => {
  // 过滤掉要删除的账号
  savedAccounts.value = savedAccounts.value.filter((account) => account.phone !== phone)
  // 更新 electron-store（将 Vue 响应式对象转换为普通对象）
  await window.api.store.set('savedAccounts', JSON.parse(JSON.stringify(savedAccounts.value)))

  // 清空密码输入框和记住密码选项
  loginForm.password = ''
  rememberPassword.value = false

  // 如果当前输入的手机号就是被删除的账号，清空手机号输入框
  if (loginForm.phone === phone) {
    loginForm.phone = ''
  } else {
    // 如果输入框不是被删除的账号，强制刷新下拉列表
    const currentPhone = loginForm.phone
    if (currentPhone === '') {
      // 输入框为空时，设置一个临时值再清空，强制触发组件更新
      loginForm.phone = ' '
      setTimeout(() => {
        loginForm.phone = ''
      }, 0)
    } else {
      // 输入框有内容时，清空再恢复
      loginForm.phone = ''
      setTimeout(() => {
        loginForm.phone = currentPhone
      }, 0)
    }
  }
  // 提示删除成功
  ElMessage.success('已删除保存的账号')
}

// 提交表单
const submitForm = async () => {
  if (loginFormRef.value) {
    loginFormRef.value.validate(async (valid) => {
      if (valid) {
        try {
          // 实际登录逻辑 - 发送用户名、密码、验证码和验证码key到后端
          const response = await axios.post(`${API_CONFIG.baseURL}${API_CONFIG.user.login}`, {
            phone: loginForm.phone,
            password: loginForm.password,
            captcha: loginForm.captcha,
            checkCodeKey: checkCodeKey.value
          })
          console.log('登录响应:', response.data)

          // 检查后端返回的业务码，不是200则抛出错误
          if (response.data.code !== '200') {
            throw {
              response: {
                data: response.data
              }
            }
          }

          // 登录成功处理
          const responseData = response.data.data // 后端返回对象包含token和user
          const token = responseData.token // 提取token
          const userData = responseData.user // 提取用户信息
          console.log('Token:', token)
          // 使用 Pinia 存储认证信息和用户信息

          // 保存认证信息到 Pinia
          authStore.setToken(token)
          authStore.setUserId(userData.userId)
          authStore.setPhone(userData.phone)

          // 保存用户完整信息到 Pinia
          userStore.setUserInfo(userData)

          // 将关键信息保存到localStorage，用于页面刷新或重新登录时恢复
          localStorage.setItem('userId', userData.userId)
          localStorage.setItem('token', token)
          localStorage.setItem('phone', userData.phone)
          // 可以考虑只保存用户基本信息，避免localStorage过大
          localStorage.setItem(
            'userInfo',
            JSON.stringify({
              userId: userData.userId,
              phone: userData.phone,
              nickname: userData.nickname || '',
              avatar: userData.avatar || ''
            })
          )

          console.log('userStore', userStore.userInfo)
          console.log('authStore', authStore)
          // 保存账号信息
          // 检查账号是否已经存在
          const accountIndex = savedAccounts.value.findIndex((acc) => acc.phone === loginForm.phone)

          if (accountIndex !== -1) {
            // 更新已有账号
            savedAccounts.value[accountIndex].password = rememberPassword.value
              ? loginForm.password
              : ''
          } else {
            // 添加新账号
            savedAccounts.value.push({
              phone: loginForm.phone,
              password: rememberPassword.value ? loginForm.password : ''
            })
          }

          // 保存到 electron-store（持久化存储，不会因清除缓存而丢失）
          // 将 Vue 响应式对象转换为普通对象，避免 IPC 克隆错误
          await window.api.store.set('savedAccounts', JSON.parse(JSON.stringify(savedAccounts.value)))
          ElMessage.success('登录成功！')

          // 登录成功后显示加载动画
          showLoading.value = true
          // 登录成功后根据当前角色跳转到对应首页
          setTimeout(() => {
            // 直接跳转到用户首页
            router.push('/user/home')
          }, 270)
        } catch (error) {
          // console.log('登录失败:', error)
          // 登录失败处理
          console.error('登录失败:', error)
          ElMessage.error(
            error.response?.data?.message || '登录失败，请检查验证码或账号密码是否正确'
          )
          // 登录失败，重新生成验证码
          generateCaptcha()
        }
      } else {
        ElMessage.error('表单验证失败，请检查输入')
        // 验证失败，重新生成验证码
        generateCaptcha()
      }
    })
  }
}

// 跳转到注册页面
const toRegister = () => {
  // 重置表单
  if (loginFormRef.value) {
    loginFormRef.value.resetFields()
  }
  router.push('/register')
}

// 跳转到管理员登录页面
const toAdminLogin = () => {
  // 重置表单
  if (loginFormRef.value) {
    loginFormRef.value.resetFields()
  }
  router.push('/admin/login')
}

// 微信扫码登录
const wechatScanLogin = () => {
  // 弹出微信扫码登录窗口或显示二维码
  ElMessage.info('微信扫码登录功能开发中...')
  // 实际实现中，这里会调用微信SDK或后端接口生成二维码
  // 并监听扫码结果和登录状态
}

// 第三方登录
const thirdPartyLogin = (type) => {
  // 根据第三方类型执行不同的登录逻辑
  ElMessage.info(`${type}登录功能开发中...`)
  // 实际实现中，这里会调用对应第三方SDK的登录接口
}
</script>

<style scoped lang="less">
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background-color: #fff;
  border-radius: 10px;
  padding: 30px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
}

/* 确保表单标签和输入框垂直居中对齐 */
.el-form-item {
  display: flex !important;
  align-items: center !important;
  margin-bottom: 20px;
}

/* 验证码区域内部垂直居中 */
.el-form-item .el-form-item__content {
  align-self: center;
}

.register-link {
  text-align: center;
  margin-top: 15px;
  font-size: 14px;
  color: #666;

  span {
    margin-right: 8px;
  }
}

.admin-link {
  text-align: center;
  margin-top: 10px;
  font-size: 13px;
}

// 加载动画样式
.loading-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  transition: opacity 0.3s ease;
}

.loading-content {
  text-align: center;
  color: #fff;
}

.loading-svg {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  100% {
    transform: rotate(360deg);
  }
}

.saved-account-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;

  .delete-account-btn {
    color: #f56c6c;
    padding: 0;
    margin: 0;
    transition: all 0.2s ease;

    &:hover {
      color: #ff0000;
      text-decoration: none;
      transform: scale(1.2);
    }
  }

  &:hover {
    background-color: #f5f7fa;
  }
}

.loading-text {
  margin-top: 20px;
  font-size: 18px;
  color: #fff;
}

.captcha-refresh-link {
  margin-left: 10px;
  font-size: 12px;
  opacity: 0.8;
  transition: opacity 0.2s;
}

.captcha-refresh-link:hover {
  font-size: 13px;
  opacity: 1;
}
</style>
