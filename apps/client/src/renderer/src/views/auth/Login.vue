<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const handleLogin = () => {
  // Here you would implement actual login logic (API call, authentication, etc.)
  console.log('Logging in with:', loginForm)

  // For demo purposes, we'll just simulate a successful login and redirect to user home
  router.push('/user/home')
}
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <div class="logo">
        <el-icon size="48" color="#FF6B6B"><Shop /></el-icon>
        <h1>佳食宜选</h1>
      </div>

      <el-form ref="form" :model="loginForm" label-width="100px" class="login-form">
        <el-form-item label="用户名" required>
          <el-input v-model="loginForm.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码" required>
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
          <el-link type="primary" :underline="false" class="forgot-password"> 忘记密码？ </el-link>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" block @click="handleLogin"> 登录 </el-button>
        </el-form-item>

        <el-form-item class="register-link">
          <span>没有账号？</span>
          <el-link type="primary" :underline="false"> 立即注册 </el-link>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped lang="less">
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.login-card {
  width: 420px;
  padding: 32px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

  .logo {
    text-align: center;
    margin-bottom: 32px;

    h1 {
      margin: 12px 0 0 0;
      color: #ff6b6b;
      font-size: 2rem /* 原值: 28px */;
    }
  }

  .login-form {
    width: 100%;
  }

  .forgot-password {
    float: right;
  }

  .register-link {
    text-align: center;
    margin-top: 24px;
    margin-bottom: 0;
  }
}
</style>
