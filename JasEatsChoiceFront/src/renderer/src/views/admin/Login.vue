<template>
  <div class="admin-login-container">
    <div class="login-box">
      <div class="login-header">
        <el-icon :size="60" color="#409eff"><Platform /></el-icon>
        <h1>管理员登录</h1>
        <p>JasEatsChoice 管理后台</p>
      </div>

      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="loginForm.remember">记住密码</el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            style="width: 100%"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <p>默认账号：admin / admin123</p>
        <el-button type="text" @click="goToUserLogin">用户端登录</el-button>
        <el-button type="text" @click="goToMerchantLogin">商家端登录</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Platform } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: 'admin123',
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

    // 调用登录API
    const response = await axios.post('http://localhost:8080/api/admin/login', {
      username: loginForm.username,
      password: loginForm.password
    })

    if (response.data.success) {
      // 保存token
      localStorage.setItem('admin_token', response.data.token)

      // 保存管理员信息
      localStorage.setItem('admin_info', JSON.stringify(response.data.admin))

      ElMessage.success('登录成功')

      // 跳转到控制台
      router.push('/admin/dashboard')
    } else {
      ElMessage.error(response.data.message || '登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.response?.data?.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

const goToUserLogin = () => {
  router.push('/login?role=user')
}

const goToMerchantLogin = () => {
  router.push('/login?role=merchant')
}
</script>

<style scoped lang="less">
.admin-login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;

  .login-box {
    width: 100%;
    max-width: 400px;
    background: white;
    border-radius: 12px;
    padding: 40px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);

    .login-header {
      text-align: center;
      margin-bottom: 40px;

      h1 {
        font-size: 28px;
        color: #303133;
        margin: 16px 0 10px 0;
      }

      p {
        font-size: 14px;
        color: #909399;
        margin: 0;
      }
    }

    .login-form {
      margin-bottom: 20px;
    }

    .login-footer {
      text-align: center;

      p {
        font-size: 12px;
        color: #909399;
        margin-bottom: 10px;
      }
    }
  }
}
</style>
