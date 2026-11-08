<template>
  <div class="admin-password-container">
    <!-- 页头：统一容器（§4.7） -->
    <AppPageHeader title="修改密码" subtitle="修改管理员登录密码" :compact="true" />

    <!-- 提交结果：明确的成功态 + 后续动作（§4.6，P2） -->
    <el-alert
      v-if="submitSuccess"
      class="submit-result"
      type="success"
      title="密码修改成功，请使用新密码重新登录"
      :closable="false"
      show-icon
    >
      <el-button type="primary" link @click="goToLogin">立即前往登录</el-button>
    </el-alert>

    <!-- 修改密码表单（§4.1、§4.4） -->
    <el-card class="password-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Key /></el-icon>
          <span>修改密码</span>
        </div>
      </template>

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
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <div class="field-stack">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码"
              show-password
              clearable
            />
            <!-- 实时强度反馈：随输入更新（§4.6，P1） -->
            <div
              v-if="passwordForm.newPassword"
              class="password-strength"
              :class="`password-strength--${strengthLevel}`"
            >
              <span class="password-strength__bar"><i /><i /><i /></span>
              <span class="password-strength__text">{{ strengthLabel }}</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            修改密码
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 密码要求：独立提示卡，与「安全提示」同级、统一卡头（§4.4，P1） -->
    <el-card class="requirements-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><InfoFilled /></el-icon>
          <span>密码要求</span>
        </div>
      </template>
      <ul class="requirements-list">
        <li
          v-for="rule in requirementRules"
          :key="rule.key"
          class="requirements-list__item"
          :class="{ 'is-met': rule.met }"
        >
          <el-icon class="requirements-list__icon">
            <Check v-if="rule.met" />
            <Close v-else />
          </el-icon>
          <span>{{ rule.text }}</span>
        </li>
      </ul>
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
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, Close, InfoFilled, Key, Lock } from '@element-plus/icons-vue'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import { changeAdminPassword } from '@/api/admin'
import { getAdminInfo, removeAdminToken } from '@/utils/auth'
import { getPasswordStrength, getPasswordStrengthText } from '@/utils/validator'

// 复杂度正则与长度阈值：字段校验与实时提示共用同一常量，避免两处漂移（校验行为与改动前一致）。
const PASSWORD_MIN_LENGTH = 6
const PASSWORD_COMPLEXITY = /(?=.*[a-z])(?=.*[A-Z])(?=.*\d)|(?=.*[a-z])(?=.*\d)(?=.*[^a-zA-Z0-9])|(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z0-9])/

const passwordFormRef = ref(null)
const loading = ref(false)
const submitSuccess = ref(false)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码强度校验
const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入新密码'))
  } else if (value.length < PASSWORD_MIN_LENGTH) {
    callback(new Error('密码长度不能少于 6 个字符'))
  } else if (!PASSWORD_COMPLEXITY.test(value)) {
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

// 密码要求逐条实时命中（P1）：只做展示，不参与提交校验
const requirementRules = computed(() => {
  const value = passwordForm.newPassword || ''
  const username = getAdminInfo()?.username || ''
  return [
    { key: 'length', text: '长度不少于 6 个字符', met: value.length >= PASSWORD_MIN_LENGTH },
    { key: 'complexity', text: '包含大小写字母、数字或特殊字符', met: PASSWORD_COMPLEXITY.test(value) },
    { key: 'username', text: '不能与用户名相同', met: value.length > 0 && value !== username }
  ]
})

// 强度等级取共享工具，颜色交给 tokens 语义色（§3.1、§7.1）
const strengthLevel = computed(() => {
  const value = passwordForm.newPassword || ''
  return ['weak', 'medium', 'strong'][getPasswordStrength(value)] || 'weak'
})

const strengthLabel = computed(() => getPasswordStrengthText(getPasswordStrength(passwordForm.newPassword || '')))

// 后续动作：清会话（用 utils/auth 的正确 key）后回登录页（§4.6，P2）
const goToLogin = () => {
  removeAdminToken()
  window.location.href = '/admin/login'
}

// 提交表单
const handleSubmit = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
    loading.value = true

    console.log('[修改密码] 提交密码修改请求')

    // 调用修改密码API
    const adminId = getAdminInfo()?.adminId
    if (!adminId) {
      ElMessage.error('未获取到管理员信息，请重新登录')
      return
    }

    const response = await changeAdminPassword(adminId, passwordForm.newPassword)

    if (response.success) {
      submitSuccess.value = true
      passwordFormRef.value.resetFields()
      ElMessage.success('密码修改成功，请重新登录')
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
  submitSuccess.value = false
  passwordFormRef.value?.resetFields()
}
</script>

<style scoped lang="less">
.admin-password-container {
  box-sizing: border-box;
  width: 100%;
  /* 设置域卡宽统一到 §3.3 的 --form-width-settings（720px），单列居中，消除右侧半屏空置（§4.4）。 */
  max-width: var(--form-width-settings);
  margin-inline: auto;

  .password-card,
  .requirements-card {
    margin-bottom: var(--card-gap-compact);
  }

  /* 卡头：图标 + 标题，三张卡同源（§4.2、§4.4） */
  .card-header {
    display: flex;
    align-items: center;
    gap: var(--space-2);
    font-size: var(--text-h3);
    font-weight: var(--weight-semibold);
    color: var(--ink-900);
  }

  .password-form {
    /* 输入框铺满表单列宽，不再固定 400px 造成列内空白（§4.4）。 */
    :deep(.el-input) {
      width: 100%;
    }
  }

  .field-stack {
    display: flex;
    flex-direction: column;
    gap: var(--space-2);
    width: 100%;
  }

  /* 实时强度条：颜色只取 §3.1 语义色 token。 */
  .password-strength {
    display: flex;
    align-items: center;
    gap: var(--space-2);
    font-size: var(--text-caption);

    &__bar {
      display: inline-flex;
      gap: var(--space-1);

      i {
        display: block;
        width: 28px;
        height: 4px;
        border-radius: var(--radius-full);
        background: var(--line);
        transition: background-color var(--duration-fast) var(--ease-out);
      }
    }

    &__text {
      color: var(--ink-500);
    }

    &--weak {
      .password-strength__text {
        color: var(--danger);
      }

      .password-strength__bar i:nth-child(1) {
        background: var(--danger);
      }
    }

    &--medium {
      .password-strength__text {
        color: var(--warning);
      }

      .password-strength__bar i:nth-child(-n+2) {
        background: var(--warning);
      }
    }

    &--strong {
      .password-strength__text {
        color: var(--success);
      }

      .password-strength__bar i {
        background: var(--success);
      }
    }
  }

  .requirements-list {
    margin: 0;
    padding: 0;
    list-style: none;

    &__item {
      display: flex;
      align-items: center;
      gap: var(--space-2);
      line-height: 1.8;
      color: var(--ink-500);
      transition: color var(--duration-fast) var(--ease-out);

      &.is-met {
        color: var(--ink-700);

        .requirements-list__icon {
          color: var(--success);
        }
      }
    }

    &__icon {
      color: var(--ink-400);
    }
  }

  .tips-list {
    margin: 0;
    padding-left: var(--space-5);

    li {
      line-height: 1.8;
      color: var(--ink-700);

      &::marker {
        color: var(--brand-500);
      }
    }
  }

  .submit-result {
    margin-bottom: var(--card-gap-compact);
  }
}
</style>
