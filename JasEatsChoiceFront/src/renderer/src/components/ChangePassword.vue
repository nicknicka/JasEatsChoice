<template>
  <el-dialog
    v-model="visible"
    title="修改密码"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input
          v-model="passwordForm.oldPassword"
          type="password"
          placeholder="请输入旧密码"
          show-password
        />
      </el-form-item>

      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="passwordForm.newPassword"
          type="password"
          placeholder="请输入新密码"
          show-password
          @input="checkPasswordStrength"
        />
        <div v-if="passwordForm.newPassword" class="password-strength">
          <span>密码强度：</span>
          <span
            class="strength-bar"
            :style="{ backgroundColor: strengthColor }"
          >{{ strengthText }}</span>
        </div>
        <div v-if="passwordErrors.length > 0" class="password-errors">
          <div v-for="(error, index) in passwordErrors" :key="index" class="error-item">
            <el-icon class="error-icon"><Close /></el-icon>
            {{ error }}
          </div>
        </div>
        <div v-else-if="passwordForm.newPassword && passwordErrors.length === 0" class="password-success">
          <el-icon class="success-icon"><Check /></el-icon>
          密码符合要求
        </div>
      </el-form-item>

      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="passwordForm.confirmPassword"
          type="password"
          placeholder="请再次输入新密码"
          show-password
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, Close } from '@element-plus/icons-vue'
import { validatePassword, getPasswordStrength, getPasswordStrengthText, getPasswordStrengthColor } from '@/utils/validator.js'
import axios from 'axios'
import { changeAdminPassword } from '../api/admin'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const passwordFormRef = ref(null)
const submitting = ref(false)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordErrors = ref([])
const passwordStrength = ref(0)

const strengthText = computed(() => getPasswordStrengthText(passwordStrength.value))
const strengthColor = computed(() => getPasswordStrengthColor(passwordStrength.value))

// 自定义验证规则
const validateNewPassword = (rule, value, callback) => {
  if (!value) {
    return callback(new Error('请输入新密码'))
  }

  const result = validatePassword(value)
  passwordErrors.value = result.errors

  if (!result.valid) {
    callback(new Error('密码不符合要求'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    return callback(new Error('请再次输入新密码'))
  }

  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, validator: validateNewPassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 检查密码强度
const checkPasswordStrength = () => {
  if (passwordForm.newPassword) {
    passwordStrength.value = getPasswordStrength(passwordForm.newPassword)
  } else {
    passwordStrength.value = 0
  }
}

// 提交修改
const handleSubmit = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
    submitting.value = true

    // 调用修改密码API
    const response = await changeAdminPassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })

    if (response.code === '200') {
      ElMessage.success('密码修改成功，请重新登录')
      emit('success')
      handleClose()

      // 清除登录状态并跳转到登录页
      setTimeout(() => {
        localStorage.removeItem('adminToken')
        localStorage.removeItem('adminInfo')
        window.location.href = '/admin/login'
      }, 1500)
    } else {
      ElMessage.error(response.message || '密码修改失败，请检查当前密码是否正确')
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error(error.message || '密码修改失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  passwordFormRef.value?.resetFields()
  passwordErrors.value = []
  passwordStrength.value = 0
  visible.value = false
}
</script>

<style scoped lang="less">
.password-strength {
  margin-top: 8px;
  font-size: 0.857rem /* 原值: 12px */;

  .strength-bar {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 4px;
    color: white;
    font-weight: bold;
    margin-left: 8px;
  }
}

.password-errors {
  margin-top: 8px;
  font-size: 0.857rem /* 原值: 12px */;
  color: #f56c6c;

  .error-item {
    display: flex;
    align-items: center;
    margin-bottom: 4px;

    .error-icon {
      margin-right: 4px;
    }
  }
}

.password-success {
  margin-top: 8px;
  font-size: 0.857rem /* 原值: 12px */;
  color: #67c23a;
  display: flex;
  align-items: center;

  .success-icon {
    margin-right: 4px;
  }
}
</style>
