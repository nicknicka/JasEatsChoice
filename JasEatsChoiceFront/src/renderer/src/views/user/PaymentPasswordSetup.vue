<template>
  <div class="payment-password-setup-container">
    <common-back-button
      type="default"
      size="small"
      @click="goBack"
      :use-router-back="false"
      style="margin-bottom: 20px"
    />

    <h2 class="page-title">{{ hasPassword ? '修改支付密码' : '设置支付密码' }}</h2>

    <!-- 安全提示 -->
    <el-alert title="安全提示" type="info" :closable="false" show-icon style="margin-bottom: 20px">
      <ul class="security-tips">
        <li>支付密码用于钱包充值、提现、支付等敏感操作</li>
        <li>请设置6位数字密码，不要与登录密码相同</li>
        <li>请勿向任何人透露您的支付密码</li>
      </ul>
    </el-alert>

    <!-- 密码设置表单 -->
    <el-card class="password-card" shadow="hover">
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="120px"
        class="password-form"
      >
        <!-- 旧密码（仅修改时显示） -->
        <el-form-item v-if="hasPassword" label="旧支付密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入旧支付密码"
            maxlength="6"
            show-password
            clearable
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 新密码 -->
        <el-form-item label="支付密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入6位数字支付密码"
            maxlength="6"
            show-password
            clearable
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 确认密码 -->
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入支付密码"
            maxlength="6"
            show-password
            clearable
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button type="primary" size="large" :loading="submitting" @click="submitPassword">
            {{ hasPassword ? '确认修改' : '确认设置' }}
          </el-button>
          <el-button size="large" @click="clearPasswordForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 忘记密码（仅已设置密码时显示） -->
    <el-card v-if="hasPassword" class="forgot-card" shadow="hover">
      <div class="forgot-content">
        <span class="forgot-text">忘记支付密码？</span>
        <el-button type="primary" link @click="showResetDialog = true">
          通过手机验证码重置
        </el-button>
      </div>
    </el-card>

    <!-- 重置密码对话框 -->
    <el-dialog v-model="showResetDialog" title="重置支付密码" width="400px" center>
      <el-form ref="resetFormRef" :model="resetForm" :rules="resetRules" label-width="100px">
        <el-form-item label="手机号">
          <el-input v-model="userPhone" disabled />
        </el-form-item>
        <el-form-item label="验证码" prop="verificationCode">
          <div class="verification-code-wrapper">
            <el-input
              v-model="resetForm.verificationCode"
              placeholder="请输入验证码"
              maxlength="6"
            />
            <el-button type="primary" :disabled="countdown > 0" @click="sendVerificationCode">
              {{ countdown > 0 ? `${countdown}秒后重试` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="resetForm.newPassword"
            type="password"
            placeholder="请输入6位数字支付密码"
            maxlength="6"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="resetForm.confirmPassword"
            type="password"
            placeholder="请再次输入支付密码"
            maxlength="6"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showResetDialog = false">取消</el-button>
        <el-button type="primary" :loading="resetting" @click="confirmReset"> 确认重置 </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock } from '@element-plus/icons-vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import paymentApi from '../../api/payment'
import userApi from '../../api/user'
import { useAuthStore } from '../../store/authStore'

const router = useRouter()
const authStore = useAuthStore()

// 是否已设置支付密码
const hasPassword = ref(false)
const userPhone = ref('')

// 密码表单
const passwordFormRef = ref()
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 重置表单
const resetFormRef = ref()
const resetForm = ref({
  verificationCode: '',
  newPassword: '',
  confirmPassword: ''
})

const showResetDialog = ref(false)
const submitting = ref(false)
const resetting = ref(false)
const countdown = ref(0)

// 密码验证规则
const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入支付密码'))
  } else if (!/^\d{6}$/.test(value)) {
    callback(new Error('支付密码必须是6位数字'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入支付密码'))
  } else if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  ...(hasPassword.value
    ? { oldPassword: [{ required: true, message: '请输入旧支付密码', trigger: 'blur' }] }
    : {}),
  newPassword: [{ validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
}

const resetRules = {
  verificationCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  newPassword: [{ validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入支付密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== resetForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 检查是否已设置支付密码
const checkPaymentPasswordStatus = async () => {
  const userId = authStore.userId
  if (!userId || userId === '0') return

  try {
    const result = await paymentApi.checkPaymentPassword(userId)
    if (result.code === '200' && result.data) {
      hasPassword.value = result.data.hasPaymentPassword || false
    }
  } catch (error) {
    console.error('检查支付密码状态失败:', error)
  }
}

// 获取用户手机号
const fetchUserPhone = async () => {
  const userId = authStore.userId
  if (!userId || userId === '0') return

  try {
    const result = await userApi.getUserInfo(userId)
    if (result.code === '200' && result.data) {
      userPhone.value = result.data.phone || ''
      // 脱敏显示
      if (userPhone.value.length > 7) {
        userPhone.value =
          userPhone.value.substring(0, 3) +
          '****' +
          userPhone.value.substring(userPhone.value.length - 4)
      }
    }
  } catch (error) {
    console.error('获取用户手机号失败:', error)
  }
}

// 提交密码设置
const submitPassword = async () => {
  await passwordFormRef.value.validate()

  const userId = authStore.userId
  if (!userId || userId === '0') {
    ElMessage.error('用户未登录')
    return
  }

  submitting.value = true
  try {
    let result
    if (hasPassword.value) {
      result = await paymentApi.changePaymentPassword(
        userId,
        passwordForm.value.oldPassword,
        passwordForm.value.newPassword
      )
    } else {
      result = await paymentApi.setupPaymentPassword(userId, passwordForm.value.newPassword, null)
    }

    if (result.code === '200') {
      ElMessage.success(hasPassword.value ? '支付密码修改成功' : '支付密码设置成功')
      clearPasswordForm()
      hasPassword.value = true
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (error) {
    console.error('提交支付密码失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 发送验证码
const sendVerificationCode = () => {
  // TODO: 实现发送验证码逻辑
  ElMessage.info('验证码发送功能开发中，请稍后')
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

// 确认重置
const confirmReset = async () => {
  await resetFormRef.value.validate()

  const userId = authStore.userId
  if (!userId || userId === '0') {
    ElMessage.error('用户未登录')
    return
  }

  resetting.value = true
  try {
    const result = await paymentApi.resetPaymentPassword(
      userId,
      resetForm.value.newPassword,
      resetForm.value.verificationCode
    )

    if (result.code === '200') {
      ElMessage.success('支付密码重置成功')
      showResetDialog.value = false
      clearPasswordForm()
    } else {
      ElMessage.error(result.message || '重置失败')
    }
  } catch (error) {
    console.error('重置支付密码失败:', error)
    ElMessage.error('重置失败，请稍后重试')
  } finally {
    resetting.value = false
  }
}

// 清空密码设置表单
const clearPasswordForm = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordFormRef.value?.resetFields()
}

// 返回
const goBack = () => {
  router.back()
}

onMounted(() => {
  checkPaymentPasswordStatus()
  fetchUserPhone()
})
</script>

<style scoped>
.payment-password-setup-container {
  padding: 0 20px 20px 20px;
  min-height: 100vh;
  background: #f5f7fa;
}

.page-title {
  font-size: 28px;
  margin: 0 0 25px 0;
  color: #333;
  font-weight: 700;
}

.security-tips {
  margin: 0;
  padding-left: 20px;
}

.security-tips li {
  line-height: 1.8;
  color: #606266;
}

.password-card {
  border-radius: 16px;
  border: none;
  max-width: 600px;
}

.password-form {
  max-width: 500px;
}

.forgot-card {
  border-radius: 16px;
  border: none;
  max-width: 600px;
  margin-top: 20px;
}

.forgot-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.forgot-text {
  color: #606266;
  font-size: 14px;
}

.verification-code-wrapper {
  display: flex;
  gap: 10px;
  width: 100%;
}

.verification-code-wrapper .el-input {
  flex: 1;
}
</style>
