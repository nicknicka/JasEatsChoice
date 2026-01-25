<template>
  <div class="settings-container">
    <h2>设置</h2>

    <el-card class="settings-card">
      <div class="settings-section">
        <h3>🧑 用户设置</h3>
        <div class="form-row">
          <div class="form-label">头像</div>
          <div class="form-content">
            <el-avatar :size="60" class="user-avatar" :src="userInfo.avatar || ''">👤</el-avatar>
            <input
              id="avatar-upload"
              type="file"
              accept="image/*"
              style="display: none"
              @change="handleAvatarUpload"
            />
            <el-button
              type="primary"
              size="small"
              style="margin-left: 10px"
              @click="handleAvatarClick"
              >更换头像</el-button
            >
          </div>
        </div>

        <div class="form-row">
          <div class="form-label">手机号</div>
          <div class="form-content">
            <el-input :model-value="userInfo.phone" readonly style="width: 200px" />
            <el-button type="text" size="small" style="margin-left: 10px" @click="handleEditPhone"
              >修改</el-button
            >
          </div>
        </div>

        <div class="form-row">
          <div class="form-label">邮箱</div>
          <div class="form-content">
            <el-input :model-value="userInfo.email" readonly style="width: 200px" />
            <el-button type="text" size="small" style="margin-left: 10px" @click="handleEditEmail"
              >修改</el-button
            >
          </div>
        </div>

        <div class="form-row">
          <div class="form-label">密码</div>
          <div class="form-content">
            <el-input type="password" placeholder="********" readonly style="width: 200px" />
            <el-button
              type="text"
              size="small"
              style="margin-left: 10px"
              @click="handleEditPassword"
              >修改</el-button
            >
          </div>
        </div>
      </div>

      <el-divider />

      <div class="settings-section">
        <h3>🔍 显示设置</h3>

        <div class="form-row">
          <div class="form-label">字体大小</div>
          <div class="form-content">
            <el-radio-group v-model="fontSize" style="margin-right: 20px">
              <el-radio label="small">小</el-radio>
              <el-radio label="medium">中</el-radio>
              <el-radio label="large">大</el-radio>
              <el-radio label="extra-large">超大</el-radio>
            </el-radio-group>
          </div>
        </div>

        <div class="form-row">
          <div class="form-label">主题切换</div>
          <div class="form-content">
            <el-switch v-model="theme" active-text="深色" inactive-text="浅色" />
          </div>
        </div>
      </div>

      <el-divider />

      <div class="settings-section">
        <h3>🔔 通知设置</h3>

        <div class="form-row">
          <div class="form-label">订单通知</div>
          <div class="form-content">
            <el-switch v-model="notifications.order" />
          </div>
        </div>

        <div class="form-row">
          <div class="form-label">活动通知</div>
          <div class="form-content">
            <el-switch v-model="notifications.activity" />
          </div>
        </div>

        <div class="form-row">
          <div class="form-label">商家回复</div>
          <div class="form-content">
            <el-switch v-model="notifications.merchantReply" />
          </div>
        </div>

        <div class="form-row">
          <div class="form-label">群聊消息</div>
          <div class="form-content">
            <el-switch v-model="notifications.groupChat" />
          </div>
        </div>
      </div>

      <el-divider />

      <div class="settings-section">
        <h3>🕶️ 隐私设置</h3>

        <div class="form-row">
          <div class="form-label">定位权限</div>
          <div class="form-content">
            <el-switch v-model="privacy.location" />
          </div>
        </div>

        <div class="form-row">
          <div class="form-label">推荐权限</div>
          <div class="form-content">
            <el-switch v-model="privacy.recommendation" />
          </div>
        </div>

        <div class="form-row">
          <div class="form-label">天气推荐</div>
          <div class="form-content">
            <el-switch v-model="privacy.weatherRecommendation" />
          </div>
        </div>

        <div class="form-row">
          <div class="form-label">AI个性化建议</div>
          <div class="form-content">
            <el-switch v-model="privacy.aiPersonalData" />
            <span style="margin-left: 10px; color: #909399; font-size: 12px">
              允许AI使用身高、体重、饮食记录等数据提供个性化建议
            </span>
          </div>
        </div>

        <div class="form-row">
          <div class="form-content">
            <el-button type="warning" size="small" style="margin-right: 10px" @click="clearCache"
              >清除缓存</el-button
            >
            <el-button type="primary" size="small" @click="exportData">数据导出</el-button>
          </div>
        </div>
      </div>

      <el-divider />

      <div class="settings-section">
        <h3>📱 版本信息</h3>

        <div class="form-row">
          <div class="form-label">当前版本</div>
          <div class="form-content">1.0.0</div>
        </div>

        <div class="form-row">
          <div class="form-content">
            <el-button type="text" size="small" style="margin-right: 10px" @click="checkUpdate"
              >检查更新</el-button
            >
            <el-button type="text" size="small" @click="submitFeedback">反馈建议</el-button>
          </div>
        </div>
      </div>

      <div class="settings-actions">
        <el-button type="primary" @click="saveSettings">保存设置</el-button>
        <el-button type="warning" style="margin-left: 10px" @click="resetSettings"
          >重置默认</el-button
        >
      </div>
    </el-card>

    <!-- Edit Phone Dialog -->
    <el-dialog v-model="editPhoneDialogVisible" title="修改手机号" width="400px">
      <el-form ref="phoneFormRef" :model="phoneForm" label-width="80px">
        <el-form-item label="手机号">
          <el-input v-model="phoneForm.phone" placeholder="请输入新手机号" />
        </el-form-item>
        <el-form-item label="验证码">
          <div style="display: flex">
            <el-input
              v-model="phoneForm.verificationCode"
              placeholder="请输入验证码"
              style="width: 150px; margin-right: 10px"
            />
            <el-button type="primary" :disabled="smsCodeCountdown > 0" @click="sendSmsCode">
              {{ smsCodeCountdown > 0 ? `${smsCodeCountdown}秒后重新发送` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editPhoneDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPhoneEdit">确认</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Edit Email Dialog -->
    <el-dialog v-model="editEmailDialogVisible" title="修改邮箱" width="400px">
      <el-form ref="emailFormRef" :model="emailForm" label-width="80px">
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="emailForm.email" placeholder="请输入新邮箱" />
        </el-form-item>
        <el-form-item label="验证码" prop="verificationCode">
          <div style="display: flex">
            <el-input
              v-model="emailForm.verificationCode"
              placeholder="请输入验证码"
              style="width: 150px; margin-right: 10px"
              maxlength="6"
            />
            <el-button type="primary" :disabled="emailCodeCountdown > 0" @click="sendEmailCode">
              {{ emailCodeCountdown > 0 ? `${emailCodeCountdown}秒后重新发送` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editEmailDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEmailEdit">确认</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Edit Password Dialog -->
    <el-dialog v-model="editPasswordDialogVisible" title="修改密码" width="400px">
      <el-form ref="passwordFormRef" :model="passwordForm" label-width="80px">
        <el-form-item label="旧密码">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请确认新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editPasswordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPasswordEdit">确认</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Feedback Dialog -->
    <el-dialog v-model="feedbackDialogVisible" title="反馈建议" width="500px">
      <el-form ref="feedbackFormRef" :model="feedbackForm" label-width="80px">
        <el-form-item label="反馈内容">
          <el-input
            v-model="feedbackForm.content"
            type="textarea"
            :rows="5"
            placeholder="请输入您的建议或反馈（最多500字）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="feedbackForm.contact" placeholder="选填，方便我们联系您" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="feedbackDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitFeedbackForm">提交</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Update Dialog -->
    <el-dialog v-model="updateDialogVisible" title="检查更新" width="400px">
      <div class="update-content">
        <p>当前版本：1.0.0</p>
        <p>最新版本：{{ latestVersion }}</p>
        <p v-if="hasUpdate" style="color: #409eff">有新版本可用，建议更新</p>
        <p v-else style="color: #67c23a">当前已是最新版本</p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="updateDialogVisible = false">关闭</el-button>
          <el-button v-if="hasUpdate" type="primary" @click="downloadUpdate">下载更新</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElDialog, ElInput, ElForm, ElFormItem } from 'element-plus'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'
import pinia from '../../store'
import { useAuthStore } from '../../store/authStore'
import { useUserStore } from '../../store/userStore'

const authStore = useAuthStore(pinia)
const userStore = useUserStore(pinia)

// 使用userStore中的用户信息（计算属性，避免undefined错误）
const userInfo = computed(() => userStore.userInfo || { phone: '', email: '', avatar: '', userId: '' })

// 正式设置数据（用于保存到localStorage）
const officialSettings = ref({
  fontSize: 'medium',
  theme: false,
  notifications: {
    order: true,
    activity: true,
    merchantReply: true,
    groupChat: true
  },
  privacy: {
    location: true,
    recommendation: true,
    weatherRecommendation: true, // 添加天气推荐设置
    aiPersonalData: true // AI个性化建议
  }
})

// 临时设置数据（用于页面实时修改）
const tempSettings = ref({ ...officialSettings.value })

// 辅助变量，方便模板中直接使用
const fontSize = ref(tempSettings.value.fontSize)
const theme = ref(tempSettings.value.theme)
const notifications = ref({ ...tempSettings.value.notifications })
const privacy = ref({ ...tempSettings.value.privacy })

// User info edit dialogs
const editPhoneDialogVisible = ref(false)
const editEmailDialogVisible = ref(false)
const editPasswordDialogVisible = ref(false)

// Form data
const phoneForm = ref({
  phone: '',
  verificationCode: ''
})

const emailForm = ref({
  email: '',
  verificationCode: ''
})

// Verification code countdowns
const smsCodeCountdown = ref(0)
const emailCodeCountdown = ref(0)

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// Feedback and update dialogs
const feedbackDialogVisible = ref(false)
const updateDialogVisible = ref(false)
const feedbackForm = ref({
  content: '',
  contact: ''
})
const latestVersion = ref('1.0.0')
const hasUpdate = ref(false)

// Avatar upload

// Load saved settings from localStorage on mount
onMounted(() => {
  // 如果userStore中没有用户信息，则从后端获取
  if (!userStore.userInfo) {
    userStore.fetchUserInfo().catch((error) => {
      console.error('加载用户信息失败:', error)
    })
  }

  // Load saved settings
  const savedSettings = localStorage.getItem('userSettings')
  if (savedSettings) {
    const parsed = JSON.parse(savedSettings)

    // 更新正式设置数据
    officialSettings.value = {
      fontSize: parsed.fontSize || 'medium',
      theme: parsed.theme || false,
      notifications: parsed.notifications || {
        order: true,
        activity: true,
        merchantReply: true,
        groupChat: true
      },
      privacy: parsed.privacy || {
        location: true,
        recommendation: true,
        weatherRecommendation: true
      }
    }

    // 更新临时设置变量
    fontSize.value = officialSettings.value.fontSize
    theme.value = officialSettings.value.theme
    notifications.value = { ...officialSettings.value.notifications }
    privacy.value = { ...officialSettings.value.privacy }

    // Update theme and font size
    updateTheme()
    updateFontSize()
  }
})

// Handle save settings with localStorage persistence
const saveSettings = async () => {
  // 将临时修改的设置同步到正式设置数据中
  const updatedSettings = {
    fontSize: fontSize.value,
    theme: theme.value,
    notifications: { ...notifications.value },
    privacy: { ...privacy.value }
  }

  // 更新正式设置数据
  officialSettings.value = { ...updatedSettings }

  // 保存到localStorage
  localStorage.setItem('userSettings', JSON.stringify(officialSettings.value))

  // 同步偏好设置到后端
  try {
    const userId = authStore.userId || '1'
    await api.put(API_CONFIG.user.preferences.replace('{userId}', userId), {
      enableAiPersonalData: privacy.value.aiPersonalData,
      enableOrderNotification: notifications.value.order,
      enableActivityNotification: notifications.value.activity,
      enableMerchantReplyNotification: notifications.value.merchantReply,
      enableGroupChatNotification: notifications.value.groupChat
    })
    console.log('✅ 偏好设置同步成功')
  } catch (error) {
    console.error('❌ 保存偏好设置失败:', error)
    // 不影响其他设置的保存
  }

  ElMessage.success('设置已保存')
  console.log('Saved settings:', officialSettings.value)

  // 更新主题和字体大小
  updateTheme()
  updateFontSize()
}

// Handle reset settings
const resetSettings = () => {
  // 默认设置值
  const defaultSettings = {
    fontSize: 'medium',
    theme: false,
    notifications: {
      order: true,
      activity: true,
      merchantReply: true,
      groupChat: true
    },
    privacy: {
      location: true,
      recommendation: true,
      weatherRecommendation: true
    }
  }

  // 更新临时设置变量
  fontSize.value = defaultSettings.fontSize
  theme.value = defaultSettings.theme
  notifications.value = { ...defaultSettings.notifications }
  privacy.value = { ...defaultSettings.privacy }

  // 更新正式设置数据并保存
  saveSettings()

  ElMessage.info('设置已重置为默认值')
}

// Update theme
const updateTheme = () => {
  if (theme.value) {
    document.body.classList.add('dark-theme')
    document.body.classList.remove('light-theme')
  } else {
    document.body.classList.add('light-theme')
    document.body.classList.remove('dark-theme')
  }
}

// Update font size
const updateFontSize = () => {
  // Remove all font size classes
  document.body.classList.remove('font-small', 'font-medium', 'font-large', 'font-extra-large')

  // Add the selected font size class
  switch (fontSize.value) {
    case 'small':
      document.body.classList.add('font-small')
      break
    case 'medium':
      document.body.classList.add('font-medium')
      break
    case 'large':
      document.body.classList.add('font-large')
      break
    case 'extra-large':
      document.body.classList.add('font-extra-large')
      break
  }
}

// Avatar upload functionality
// 不再需要手动调用updateSidebarAvatar，因为更新userStore会自动同步

const handleAvatarClick = () => {
  document.getElementById('avatar-upload').click()
}

const handleAvatarUpload = async (event) => {
  const file = event.target.files[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = async (e) => {
      const newAvatarBase64 = e.target.result

      try {
        // 上传到后端
        const userId = authStore.userId || userInfo.value.userId
        const response = await api.put(
          API_CONFIG.user.uploadAvatar.replace('{userId}', userId),
          { avatarBase64: newAvatarBase64 }
        )

        if (response && (response.code === '200' || response.success)) {
          // ✅ 直接更新userStore，会自动同步到所有使用该store的地方
          userStore.userInfo.avatar = response.data.avatarBase64

          // 保存到localStorage作为备份
          localStorage.setItem('userAvatar', response.data.avatarBase64)

          ElMessage.success('头像已更换')
        } else {
          ElMessage.error(response.message || '头像上传失败')
        }
      } catch (error) {
        console.error('头像上传失败:', error)
        ElMessage.error('头像上传失败，请重试')
      }
    }
    reader.readAsDataURL(file)
  }
}

// Handle send SMS verification code
const sendSmsCode = () => {
  const phone = phoneForm.value.phone
  if (!phone) {
    ElMessage.warning('请输入手机号')
    return
  }

  // 手机号格式验证
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(phone)) {
    ElMessage.warning('请输入有效的手机号')
    return
  }

  // Call backend API to send SMS code
  api
    .post(API_CONFIG.user.sendSmsCode, { phone })
    .then((response) => {
      // 统一响应处理
      const isSuccess = response.code === '200' || response.success
      if (isSuccess) {
        ElMessage.success('验证码已发送')
        // Start countdown
        smsCodeCountdown.value = 60
        const timer = setInterval(() => {
          smsCodeCountdown.value--
          if (smsCodeCountdown.value <= 0) {
            clearInterval(timer)
          }
        }, 1000)
      } else {
        ElMessage.error(response.message || '发送失败')
      }
    })
    .catch((error) => {
      ElMessage.error(error.message || '发送失败')
    })
}

// Handle edit phone
const handleEditPhone = () => {
  editPhoneDialogVisible.value = true
  // Auto-fill current phone number
  phoneForm.value.phone = userInfo.value.phone || ''
}

const submitPhoneEdit = () => {
  if (phoneForm.value.phone && phoneForm.value.verificationCode) {
    // 手机号格式验证
    const phoneRegex = /^1[3-9]\d{9}$/
    if (!phoneRegex.test(phoneForm.value.phone)) {
      ElMessage.warning('请输入有效的手机号')
      return
    }

    // 验证码长度验证
    if (phoneForm.value.verificationCode.length !== 6) {
      ElMessage.warning('请输入6位验证码')
      return
    }

    // Call backend API to update phone number
    // 后端期望的字段名是 smsCode，不是 verificationCode
    api
      .put(API_CONFIG.user.update.replace('{userId}', userInfo.value.userId), {
        phone: phoneForm.value.phone,
        smsCode: phoneForm.value.verificationCode  // ← 使用 smsCode
      })
      .then((response) => {
        const isSuccess = response.code === '200' || response.success
        if (isSuccess) {
          ElMessage.success('手机号已修改')
          // 更新userStore中的用户信息
          userStore.userInfo.phone = phoneForm.value.phone
          editPhoneDialogVisible.value = false
          phoneForm.value = { phone: '', verificationCode: '' }
        } else {
          ElMessage.error(response.message || '手机号修改失败')
        }
      })
      .catch((error) => {
        ElMessage.error(error.message || '手机号修改失败')
      })
  } else {
    ElMessage.warning('请填写完整信息')
  }
}

// Handle send email verification code
const sendEmailCode = () => {
  const email = emailForm.value.email
  if (!email) {
    ElMessage.warning('请输入邮箱地址')
    return
  }

  // 邮箱格式验证
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  if (!emailRegex.test(email)) {
    ElMessage.warning('请输入有效的邮箱地址')
    return
  }

  // Call backend API to send email code
  api
    .post(API_CONFIG.user.sendEmailCode, { email })
    .then((response) => {
      // 统一响应处理
      const isSuccess = response.code === '200' || response.success
      if (isSuccess) {
        ElMessage.success('验证码已发送')
        // Start countdown
        emailCodeCountdown.value = 60
        const timer = setInterval(() => {
          emailCodeCountdown.value--
          if (emailCodeCountdown.value <= 0) {
            clearInterval(timer)
          }
        }, 1000)
      } else {
        ElMessage.error(response.message || '发送失败')
      }
    })
    .catch((error) => {
      ElMessage.error(error.message || '发送失败')
    })
}

// Handle edit email
const handleEditEmail = () => {
  editEmailDialogVisible.value = true
  // 重置表单并自动填充当前邮箱
  emailForm.value = {
    email: userInfo.value.email || '',
    verificationCode: ''
  }
}

const submitEmailEdit = () => {
  // 去除首尾空格，避免空格导致验证失败
  const email = (emailForm.value.email || '').trim()
  const verificationCode = (emailForm.value.verificationCode || '').trim()

  // 检查是否填写完整（使用字符串长度判断更准确）
  if (!email || email.length === 0) {
    ElMessage.warning('请输入邮箱地址')
    return
  }

  if (!verificationCode || verificationCode.length === 0) {
    ElMessage.warning('请输入验证码')
    return
  }

  // 邮箱格式验证
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  if (!emailRegex.test(email)) {
    ElMessage.warning('请输入有效的邮箱地址')
    return
  }

  // 验证码格式验证（必须是6位数字）
  const codeRegex = /^\d{6}$/
  if (!codeRegex.test(verificationCode)) {
    ElMessage.warning('请输入6位数字验证码')
    return
  }

  // Call backend API to update email（注意：后端期望的字段名是 emailCode）
  api
    .put(API_CONFIG.user.update.replace('{userId}', userInfo.value.userId), {
      email: email,
      emailCode: verificationCode
    })
    .then((response) => {
      const isSuccess = response.code === '200' || response.success
      if (isSuccess) {
        ElMessage.success('邮箱已修改')
        // 更新userStore中的用户信息
        userStore.userInfo.email = email
        editEmailDialogVisible.value = false
        emailForm.value = { email: '', verificationCode: '' }
      } else {
        ElMessage.error(response.message || '邮箱修改失败')
      }
    })
    .catch((error) => {
      ElMessage.error(error.message || '邮箱修改失败')
    })
}

// Handle edit password
const handleEditPassword = () => {
  editPasswordDialogVisible.value = true
}

const submitPasswordEdit = async () => {
  if (
    passwordForm.value.oldPassword &&
    passwordForm.value.newPassword &&
    passwordForm.value.confirmPassword
  ) {
    if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
      ElMessage.warning('新密码和确认密码不一致')
      return
    }

    if (passwordForm.value.newPassword.length < 6) {
      ElMessage.warning('新密码长度不能少于6位')
      return
    }

    try {
      // 调用后端API修改密码
      const userId = authStore.userId || userInfo.value.userId
      const response = await api.put(
        API_CONFIG.user.updatePassword.replace('{userId}', userId),
        {
          oldPassword: passwordForm.value.oldPassword,
          newPassword: passwordForm.value.newPassword
        }
      )

      const isSuccess = response.code === '200' || response.success
      if (isSuccess) {
        ElMessage.success('密码已修改')
        editPasswordDialogVisible.value = false
        passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
      } else {
        ElMessage.error(response.message || '密码修改失败')
      }
    } catch (error) {
      ElMessage.error(error.message || '密码修改失败')
    }
  } else {
    ElMessage.warning('请填写完整信息')
  }
}

// Handle clear cache
const clearCache = () => {
  // Clear localStorage except for user settings and avatar
  const userSettings = localStorage.getItem('userSettings')
  const userAvatar = localStorage.getItem('userAvatar')

  localStorage.clear()

  if (userSettings) localStorage.setItem('userSettings', userSettings)
  if (userAvatar) localStorage.setItem('userAvatar', userAvatar)

  ElMessage.success('缓存已清除')
  console.log('Cache cleared')
}

// Handle data export
const exportData = () => {
  // Create data to export
  const userData = {
    profile: {
      phone: userInfo.value.phone || '未设置',
      email: userInfo.value.email || '未设置'
    },
    settings: JSON.parse(localStorage.getItem('userSettings') || '{}'),
    exportDate: new Date().toISOString()
  }

  // Convert to JSON and download
  const dataStr = JSON.stringify(userData, null, 2)
  const dataBlob = new Blob([dataStr], { type: 'application/json' })
  const dataUrl = URL.createObjectURL(dataBlob)

  const a = document.createElement('a')
  a.href = dataUrl
  a.download = '用户数据导出.json'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(dataUrl)

  ElMessage.success('数据导出成功')
  console.log('Data export completed:', userData)
}

// Handle check for updates
const checkUpdate = () => {
  // 模拟检查更新
  updateDialogVisible.value = true
  // 在实际应用中，这里应该调用API检查最新版本
  // latestVersion.value = '1.0.1'
  // hasUpdate.value = latestVersion.value !== '1.0.0'
}

// Handle download update
const downloadUpdate = () => {
  ElMessage.info('更新功能即将推出')
  updateDialogVisible.value = false
}

// Handle feedback dialog open
const submitFeedback = () => {
  feedbackDialogVisible.value = true
}

// Handle submit feedback form
const submitFeedbackForm = async () => {
  if (!feedbackForm.value.content || feedbackForm.value.content.trim().length === 0) {
    ElMessage.warning('请输入反馈内容')
    return
  }

  try {
    const userId = authStore.userId || '1'
    const response = await api.post(API_CONFIG.user.feedback, {
      userId: userId,
      content: feedbackForm.value.content,
      contact: feedbackForm.value.contact || ''
    })

    const isSuccess = response.code === '200' || response.success
    if (isSuccess) {
      ElMessage.success('反馈已提交，感谢您的建议')
      feedbackDialogVisible.value = false
      feedbackForm.value = { content: '', contact: '' }
    } else {
      ElMessage.error(response.message || '反馈提交失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '反馈提交失败')
  }
}
</script>

<style scoped>
.settings-container {
  padding: 0 20px 20px 20px;
}

.settings-container h2 {
  font-size: 24px;
  margin: 0 0 20px 0;
}

.settings-container .settings-card {
  padding: 20px;
}

.settings-container .settings-section {
  margin-bottom: 20px;
}

.settings-container .settings-section h3 {
  font-size: 18px;
  margin: 0 0 20px 0;
  font-weight: bold;
}

.settings-container .form-row {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.settings-container .form-row .form-label {
  width: 150px;
  font-weight: bold;
}

.settings-container .form-row .form-content {
  flex: 1;
}

.settings-container .settings-actions {
  margin-top: 30px;
  text-align: center;
}

.settings-container .user-avatar {
  background-color: transparent; /* 移除额外的背景颜色 */
}

.update-content p {
  margin: 10px 0;
  font-size: 14px;
}
</style>

<style>
/* 全局主题样式 */
/* 浅色主题 */
.light-theme {
  background-color: #ffffff;
  color: #333333;
}

/* 深色主题 */
.dark-theme {
  background-color: #1a1a1a;
  color: #ffffff;

  /* Element Plus 组件深色适配 */
  .el-card {
    background-color: #2d2d2d;
    border-color: #444444;
  }

  .el-divider {
    background-color: #444444;
  }

  .el-input__wrapper {
    background-color: #2d2d2d;
    color: #ffffff;
    border-color: #444444;
  }

  .el-switch__core {
    background-color: #444444;
  }

  .el-button {
    background-color: #409eff;
    border-color: #409eff;
  }

  .el-button--default {
    background-color: #2d2d2d;
    border-color: #444444;
    color: #ffffff;
  }

  .el-button--default:hover {
    background-color: #444444;
    border-color: #666666;
  }

  .el-dialog {
    background-color: #2d2d2d;
    color: #ffffff;
  }

  .el-dialog__header {
    border-bottom-color: #444444;
  }

  .el-form-item__label {
    color: #ffffff !important;
  }
}

/* 字体大小 */
.font-small {
  font-size: 12px;
}

.font-medium {
  font-size: 14px;
}

.font-large {
  font-size: 16px;
}

.font-extra-large {
  font-size: 18px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .font-small {
    font-size: 11px;
  }

  .font-medium {
    font-size: 13px;
  }

  .font-large {
    font-size: 15px;
  }

  .font-extra-large {
    font-size: 17px;
  }
}
</style>
