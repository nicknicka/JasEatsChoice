<template>
  <div class="payment-password-setup-page">
    <!-- 页头：标准版标题容器，「返回」并入右侧动作区（§4.7） -->
    <AppPageHeader :title="pageTitle">
      <template #actions>
        <CommonBackButton type="default" :use-router-back="false" @click="goBack" />
      </template>
    </AppPageHeader>

    <!-- 认证类单列窄列：宽度只取 --form-width-auth，水平居中（§3.3、§4.4） -->
    <div class="payment-password-column">
      <!-- 安全提示：表单上方的辅助说明，中性底 + 线性图标，不与主操作抢权重（§3.1、§4.4） -->
      <div class="security-note">
        <el-icon class="security-note__icon"><InfoFilled /></el-icon>
        <div class="security-note__body">
          <p class="security-note__title">安全提示</p>
          <ul class="security-tips">
            <li>支付密码用于钱包充值、提现、支付等敏感操作</li>
            <li>请设置6位数字密码，不要与登录密码相同</li>
            <li>请勿向任何人透露您的支付密码</li>
          </ul>
        </div>
      </div>

      <AppFormContainer variant="auth">
        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-position="top"
          class="password-form"
          @submit.prevent
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

          <!-- 提交动作：主按钮取 md、次级动作取 sm（§3.4） -->
          <div class="form-actions">
            <el-button
              class="submit-btn"
              type="primary"
              :loading="submitting"
              @click="submitPassword"
            >
              {{ hasPassword ? '确认修改' : '确认设置' }}
            </el-button>
            <el-button class="reset-btn" @click="clearPasswordForm">重置</el-button>
          </div>
        </el-form>
      </AppFormContainer>

      <!-- 忘记密码：并入同一列的次级入口（仅已设置密码时显示） -->
      <div v-if="hasPassword" class="forgot-card">
        <span class="forgot-text">忘记支付密码？</span>
        <el-button class="forgot-link" type="primary" link @click="showResetDialog = true">
          通过手机验证码重置
        </el-button>
      </div>
    </div>

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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, InfoFilled } from '@element-plus/icons-vue'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import AppFormContainer from '../../components/common/AppFormContainer.vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import paymentApi from '../../api/payment'
import userApi from '../../api/user'
import verificationApi from '../../api/verification'
import { useAuthStore } from '../../store/authStore'

const router = useRouter()
const authStore = useAuthStore()

// 是否已设置支付密码
const hasPassword = ref(false)
const userPhone = ref('')

// 页面标题：设置 / 修改随密码状态切换（§4.7 每页唯一 h1）
const pageTitle = computed(() => (hasPassword.value ? '修改支付密码' : '设置支付密码'))

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
const sendVerificationCode = async () => {
  const userId = authStore.userId
  if (!userId || userId === '0') {
    ElMessage.error('用户未登录')
    return
  }

  // 获取用户真实手机号（未脱敏的）
  try {
    const result = await userApi.getUserInfo(userId)
    if (result.code !== '200' || !result.data || !result.data.phone) {
      ElMessage.error('未获取到用户手机号，请先完善个人信息')
      return
    }

    const realPhone = result.data.phone

    // 调用发送验证码API
    const response = await verificationApi.sendVerificationCode(realPhone, 'payment')

    if (response.code === '200') {
      ElMessage.success('验证码已发送，请注意查收')

      // 开始倒计时
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    } else {
      ElMessage.error(response.message || '发送验证码失败，请稍后重试')
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
    ElMessage.error('发送验证码失败，请稍后重试')
  }
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

<style scoped lang="less">
/* 页面颜色、间距、圆角、字阶与动效均取自 tokens.css（§3、§7.1），不写裸色值。 */

/* 单列居中，最大宽度 --page-max；页头用标准版（§3.3、§4.7）。 */
.payment-password-setup-page {
  box-sizing: border-box;
  width: 100%;
  max-width: var(--page-max);
  min-height: 100%;
  margin: 0 auto;
  padding: 0 var(--page-gutter) var(--space-10);
  background: var(--canvas);
}

/* 认证类窄列：宽度只取 --form-width-auth，水平居中（§3.3、§4.4）。 */
.payment-password-column {
  display: flex;
  flex-direction: column;
  gap: var(--card-gap);
  width: min(100%, var(--form-width-auth));
  margin-inline: auto;
}

/* 安全提示降级为表单上方的辅助说明：中性底 + 线性图标（§3.1、§4.4）。 */
.security-note {
  display: flex;
  gap: var(--space-3);
  padding: var(--space-4);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--canvas-sunken);
}

.security-note__icon {
  flex: 0 0 auto;
  font-size: var(--text-h3);
  color: var(--info);
}

.security-note__body {
  min-width: 0;
}

.security-note__title {
  margin: 0;
  font-size: var(--text-body-sm);
  font-weight: var(--weight-semibold);
  color: var(--ink-700);
}

.security-tips {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  margin: var(--space-2) 0 0;
  padding-left: var(--space-4);
}

.security-tips li {
  font-size: var(--text-body-sm);
  line-height: 20px;
  color: var(--ink-500);
}

/* 字段间距 --space-4、错误提示 --danger / 12px（§4.4）。 */
.payment-password-column :deep(.el-form-item) {
  margin-bottom: var(--space-4);
}

.payment-password-column :deep(.el-form-item__error) {
  font-size: var(--text-caption);
  color: var(--danger);
}

/* 提交动作：主按钮 --control-height-md、次级动作 --control-height-sm（§3.4）。 */
.form-actions {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  margin-top: var(--space-2);
}

.form-actions .el-button {
  width: 100%;
  margin-left: 0;
}

.submit-btn {
  height: var(--control-height-md);
}

.reset-btn {
  height: var(--control-height-sm);
}

/* 忘记密码：并入同一列的次级入口（§4.6）。 */
.forgot-card {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-1);
}

.forgot-text {
  font-size: var(--text-body-sm);
  color: var(--ink-500);
}

.verification-code-wrapper {
  display: flex;
  gap: var(--space-2);
  width: 100%;
}

.verification-code-wrapper .el-input {
  flex: 1;
}
</style>
