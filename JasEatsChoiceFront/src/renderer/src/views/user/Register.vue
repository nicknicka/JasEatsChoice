<template>
  <div class="register-container">
    <div class="register-card">
      <h2 class="register-title">用户注册</h2>

      <el-form
        :model="registerForm"
        :rules="registerRules"
        ref="registerFormRef"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="nickname">
          <el-input v-model="registerForm.nickname" placeholder="请输入用户名" autocomplete="off" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱" type="email" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="验证码" prop="captcha">
          <div style="display: flex; align-items: center">
            <el-input
              v-model="registerForm.captcha"
              placeholder="请输入验证码"
              style="width: 60%; margin-right: 10px"
            />
            <img
              :src="captchaBase64"
              alt="验证码"
              style="width: 100px; height: 36px; background-color: #f5f7fa; cursor: pointer"
              @click="generateCaptcha"
            />
            <el-button type="text" size="small" style="margin-left: 10px" @click="generateCaptcha"
              >刷新</el-button
            >
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" block>注册</el-button>
        </el-form-item>

        <!-- 第三方登录 -->
        <el-divider>第三方登录</el-divider>

        <el-form-item>
          <el-button type="warning" @click="wechatScanLogin" block>
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

          <el-button type="info" @click="thirdPartyLogin('QQ')" block>
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

        <div class="login-link">
          <span>已有账号？</span>
          <el-button type="text" @click="toLogin">立即登录</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElForm, ElFormItem, ElInput, ElButton, ElLoading } from 'element-plus'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { API_CONFIG } from '../../config'

const router = useRouter()

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
    {
      pattern: /^1[3456789]\d{9}$/,
      message: '请输入正确的手机号码',
      trigger: ['blur', 'change']
    }
  ],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
})

// 验证码相关
const captchaBase64 = ref('') // 后端返回的验证码图片Base64
const checkCodeKey = ref('') // 后端返回的验证码会话key

// 从后端获取算术验证码
const generateCaptcha = async () => {
  try {
    const response = await axios.get(`${API_CONFIG.baseURL}/v1/captcha/checkCode`)
    const result = response.data.data
    // 添加base64图片前缀，否则浏览器无法识别
    captchaBase64.value = 'data:image/png;base64,' + result.checkCode
    checkCodeKey.value = result.checkCodeKey
  } catch (error) {
    console.error('获取验证码失败:', error)
    ElMessage.error('获取验证码失败，请稍后重试')
  }
}

// 表单引用
const registerFormRef = ref(null)

// 页面加载时生成验证码
onMounted(() => {
  generateCaptcha()
})

// 监听路由变化，确保每次进入页面都刷新验证码
watch(
  () => router.currentRoute.value.path,
  (newPath) => {
    if (newPath === '/register') {
      generateCaptcha()
    }
  }
)

// 提交表单
const submitForm = async () => {
  if (registerFormRef.value) {
    registerFormRef.value.validate(async (valid) => {
      if (valid) {
        try {
          // 发送注册请求到后端
          const response = await axios.post(`${API_CONFIG.baseURL}${API_CONFIG.user.register}`, {
            nickname: registerForm.nickname,
            password: registerForm.password,
            email: registerForm.email,
            phone: registerForm.phone,
            captcha: registerForm.captcha,
            checkCodeKey: checkCodeKey.value
          })

          // 检查后端返回的业务码
          if (response.data.code === '200') {
            ElMessage.success('注册成功！')
            // 清空表单
            resetForm()
            // 跳转到登录页面过程中显示loading动画
            const loadingInstance = ElLoading.service({
              lock: true,
              text: '正在跳转到登录页面...',
              background: 'rgba(0, 0, 0, 0.7)'
            })

            // 延迟270ms跳转到登录页面，确保用户看到loading动画
            setTimeout(() => {
              loadingInstance.close()
              router.push('/login')
            }, 270)
          } else {
            // 注册失败
            ElMessage.error(response.data.message || '注册失败，请稍后重试')
          }
        } catch (error) {
          console.error('注册请求失败:', error)
          ElMessage.error(error.response?.data?.message || '网络请求失败，请稍后重试')
        } finally {
          // 无论成功失败，重新生成验证码
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

// 重置表单
const resetForm = () => {
  if (registerFormRef.value) {
    registerFormRef.value.resetFields()
  }
}

// 跳转到登录页面
const toLogin = () => {
  // 清空表单
  resetForm()
  // 跳转到登录页面过程中显示loading动画
  const loadingInstance = ElLoading.service({
    lock: true,
    text: '正在跳转到登录页面...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  // 延迟270ms跳转到登录页面，确保用户看到loading动画
  setTimeout(() => {
    loadingInstance.close()
    router.push('/login')
  }, 270)
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
.register-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.register-card {
  width: 100%;
  max-width: 400px;
  background-color: #fff;
  border-radius: 10px;
  padding: 30px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
}

.register-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 1.714rem /* 原值: 24px */;
}

.login-link {
  text-align: center;
  margin-top: 15px;
  font-size: 1rem /* 原值: 14px */;
  color: #666;

  span {
    margin-right: 8px;
  }
}
</style>
