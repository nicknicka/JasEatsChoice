<template>
  <div class="admin-password-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>修改密码</h1>
      <p class="subtitle">修改管理员登录密码</p>
    </div>

    <!-- 修改密码表单 -->
    <el-card class="password-card" shadow="never">
      <el-form
        :model="passwordForm"
        :rules="rules"
        ref="passwordFormRef"
        label-width="120px"
        class="password-form"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
            clearable
            style="width: 400px"
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
            clearable
            style="width: 400px"
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
            clearable
            style="width: 400px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            修改密码
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 密码强度提示 -->
      <el-alert
        title="密码要求"
        type="info"
        :closable="false"
        show-icon
      >
        <ul class="password-requirements">
          <li>长度不少于 6 个字符</li>
          <li>包含大小写字母、数字或特殊字符</li>
          <li>不能与用户名相同</li>
        </ul>
      </el-alert>
    </el-card>

    <!-- 安全提示 -->
    <el-card class="security-tips" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Lock /></el-icon>
          <span>安全提示</span>
        </div>
      </template>
      <ul class="tips-list">
        <li>建议定期更换密码，保障账号安全</li>
        <li>不要使用过于简单的密码，如：123456、password等</li>
        <li>不要在多个平台使用相同密码</li>
        <li>不要将密码告知他人或写在明文位置</li>
      </ul>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Lock } from '@element-plus/icons-vue'
import { changeAdminPassword } from '@/api/admin'

const passwordFormRef = ref(null)
const loading = ref(false)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码强度校验
const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入新密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度不能少于 6 个字符'))
  } else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)|(?=.*[a-z])(?=.*\d)(?=.*[^a-zA-Z0-9])|(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z0-9])/.test(value)) {
    callback(new Error('密码必须包含大小写字母、数字或特殊字符中的至少两种'))
  } else {
    callback()
  }
}

// 确认密码校验
const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 提交表单
const handleSubmit = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
    loading.value = true

    console.log('[修改密码] 提交密码修改请求')

    // 调用修改密码API
    const response = await changeAdminPassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })

    if (response.code === '200') {
      ElMessage.success('密码修改成功，请重新登录')
      handleReset()

      // 可以选择跳转到登录页
      setTimeout(() => {
        // 清除登录状态
        localStorage.removeItem('adminToken')
        localStorage.removeItem('adminInfo')
        // 跳转到登录页
        window.location.href = '/admin/login'
      }, 1500)
    } else {
      ElMessage.error(response.message || '密码修改失败，请检查当前密码是否正确')
    }
  } catch (error) {
    console.error('[修改密码] 修改失败:', error)
    if (error !== 'cancel') {
      ElMessage.error('修改失败: ' + (error.message || '请检查当前密码是否正确'))
    }
  } finally {
    loading.value = false
  }
}

// 重置表单
const handleReset = () => {
  passwordFormRef.value?.resetFields()
}
</script>

<style scoped lang="less">
.admin-password-container {
  .page-header {
    margin-bottom: 20px;

    h1 {
      font-size: 24px;
      color: #303133;
      margin: 0 0 8px 0;
    }

    .subtitle {
      color: #909399;
      margin: 0;
      font-size: 14px;
    }
  }

  .password-card {
    max-width: 600px;
    margin-bottom: 20px;

    .password-form {
      margin-top: 20px;
    }

    .password-requirements {
      margin: 10px 0 0 0;
      padding-left: 20px;

      li {
        line-height: 1.8;
        color: #606266;
      }
    }
  }

  .security-tips {
    max-width: 600px;

    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: bold;
    }

    .tips-list {
      margin: 10px 0 0 0;
      padding-left: 20px;

      li {
        line-height: 1.8;
        color: #606266;

        &::marker {
          color: #409eff;
        }
      }
    }
  }
}
</style>
