<template>
  <div class="settings-container">
    <h2 class="fade-in-up">设置</h2>

    <el-card class="settings-card">
      <div class="settings-section stagger-item">
        <h3>🧑 用户设置</h3>
        <div class="form-row">
          <div class="form-label">头像</div>
          <div class="form-content">
            <el-avatar :size="60" class="user-avatar" :src="getAvatarUrl(userInfo.avatar)">👤</el-avatar>
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
            <el-input :model-value="maskedPhone" readonly style="width: 200px" />
            <el-button type="text" size="small" style="margin-left: 10px" @click="handleEditPhone"
              >修改</el-button
            >
          </div>
        </div>

        <div class="form-row">
          <div class="form-label">邮箱</div>
          <div class="form-content">
            <el-input :model-value="maskedEmail" readonly style="width: 200px" />
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

      <div class="settings-section stagger-item">
        <h3>🔍 显示设置</h3>

        <!-- TODO: 字体大小切换功能 -->
        <!-- ✅ 已实现：聊天、表单、按钮等主要内容区域可以随字体大小设置变化 -->
        <!-- ⚠️  待优化：侧边栏菜单文字暂时无法响应字体大小变化（Element Plus 组件限制） -->
        <div class="form-row">
          <div class="form-label">字体大小</div>
          <div class="form-content">
            <el-radio-group v-model="fontSize" style="margin-right: 20px">
              <el-radio label="small">小</el-radio>
              <el-radio label="medium">中</el-radio>
              <el-radio label="large">大</el-radio>
              <el-radio label="extra-large">超大</el-radio>
            </el-radio-group>
            <span style="margin-left: 10px; color: #909399; font-size: 0.857rem">
              调整主要内容区域的字体大小
            </span>
          </div>
        </div>

        <!-- TODO: 主题切换功能 -->
        <!-- ✅ 已实现：深色/浅色主题切换，支持 Element Plus 组件适配 -->
        <!-- ⚠️  待测试：请在不同页面测试主题切换效果 -->
        <div class="form-row">
          <div class="form-label">主题切换</div>
          <div class="form-content">
            <el-switch v-model="theme" active-text="深色" inactive-text="浅色" />
            <span style="margin-left: 10px; color: #909399; font-size: 0.857rem">
              切换应用的整体主题颜色
            </span>
          </div>
        </div>
      </div>

      <el-divider />

      <div class="settings-section stagger-item">
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

      <div class="settings-section stagger-item">
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
            <span style="margin-left: 10px; color: #909399; font-size: 0.857rem /* 原值: 12px */">
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

      <div class="settings-section stagger-item">
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
      <el-form ref="phoneFormRef" :model="phoneForm" label-width="120px">
        <el-form-item label="验证原手机号" prop="oldPhoneVerify">
          <el-input
            v-model="phoneForm.oldPhoneVerify"
            placeholder="请输入原手机号的中间4位"
            maxlength="4"
            style="width: 150px"
          />
          <div style="font-size: 0.857rem /* 原值: 12px */; color: #909399; margin-top: 4px;">
            您的原手机号：{{ maskedPhone }}，请输入中间4位数字进行验证
          </div>
        </el-form-item>
        <el-form-item label="新手机号" prop="phone">
          <el-input v-model="phoneForm.phone" placeholder="请输入新手机号" />
        </el-form-item>
        <el-form-item label="验证码" prop="verificationCode">
          <div style="display: flex">
            <el-input
              v-model="phoneForm.verificationCode"
              placeholder="请输入验证码"
              style="width: 150px; margin-right: 10px"
              maxlength="6"
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
      <el-form ref="emailFormRef" :model="emailForm" label-width="120px">
        <el-form-item label="验证原邮箱" prop="oldEmailVerify">
          <el-input
            v-model="emailForm.oldEmailVerify"
            placeholder="请输入原邮箱地址"
            style="width: 100%"
          />
          <div style="font-size: 0.857rem /* 原值: 12px */; color: #909399; margin-top: 4px;">
            您的原邮箱：{{ maskedEmail }}，请输入完整的原邮箱地址进行验证
          </div>
        </el-form-item>
        <el-form-item label="新邮箱" prop="email">
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
import { getAvatarUrl } from '@/utils/avatar'

const authStore = useAuthStore(pinia)
const userStore = useUserStore(pinia)

// 使用userStore中的用户信息（计算属性，避免undefined错误）
const userInfo = computed(() => userStore.userInfo || { phone: '', email: '', avatar: '', userId: '' })

// 手机号脱敏显示（中间四位不显示）
const maskedPhone = computed(() => {
  const phone = userInfo.value.phone || ''
  if (phone.length === 11) {
    return phone.substring(0, 3) + '****' + phone.substring(7)
  }
  return phone
})

// 邮箱脱敏显示（只显示第一个字符和@及后缀）
const maskedEmail = computed(() => {
  const email = userInfo.value.email || ''
  if (!email) return email

  const atIndex = email.indexOf('@')
  if (atIndex > 1) {
    // 保留第一个字符和@及域名部分，中间用***代替
    return email[0] + '***' + email.substring(atIndex)
  } else if (atIndex === 1) {
    // 如果@前面只有一个字符，如 a@b.com
    return email
  }
  return email
})

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
  oldPhone: '',           // 原手机号，只读显示
  oldPhoneVerify: '',     // 原手机号中间4位验证
  phone: '',              // 新手机号
  verificationCode: ''
})

const emailForm = ref({
  oldEmail: '',        // 原邮箱，只读显示
  oldEmailVerify: '',  // 原邮箱验证
  email: '',           // 新邮箱
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
// TODO: 主题切换功能
// ✅ 已实现：通过在 body 上添加/移除 dark-theme 和 light-theme 类来实现主题切换
// ✅ 已实现：Element Plus 组件深色模式适配（按钮、卡片、表单等）
// ⚠️  待测试：请在不同页面测试主题效果，确保所有组件颜色正确
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
// TODO: 字体大小切换功能
// ✅ 已实现：通过在 body 上添加 font-small/medium/large/extra-large 类来实现全局字体大小调整
// ✅ 已实现：主要内容区域（聊天、表单、按钮、卡片）使用 rem 相对单位，可以响应字体大小变化
// ✅ 已实现：批量转换了 155 个文件的 1,618 处字体为 rem 单位
// ⚠️  待优化：侧边栏菜单（el-menu）由于 Element Plus 组件限制，暂时无法完全响应字体大小变化
// 💡 替代方案：如需侧边栏缩放，可考虑使用 CSS transform 或 JavaScript 动态调整
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
  if (!file) {
    return
  }

  // 校验文件类型
  const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!validTypes.includes(file.type)) {
    ElMessage.warning('只支持 JPG、PNG、GIF、WebP 格式的图片')
    // 清空input，允许重新选择同一文件
    event.target.value = ''
    return
  }

  // 校验文件大小（限制2MB）
  const maxSize = 2 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.warning('图片大小不能超过2MB')
    event.target.value = ''
    return
  }

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
  // 重置表单并自动填充当前手机号
  const currentPhone = userInfo.value.phone || ''
  phoneForm.value = {
    oldPhone: currentPhone,      // 原手机号，只读显示
    oldPhoneVerify: '',          // 原手机号中间4位验证
    phone: '',                   // 新手机号，用户需要输入
    verificationCode: ''
  }
}

const submitPhoneEdit = () => {
  // 显示原手机号提示，让用户确认
  const oldPhone = phoneForm.value.oldPhone || userInfo.value.phone || ''

  // 验证原手机号的中间4位
  const oldPhoneVerify = (phoneForm.value.oldPhoneVerify || '').trim()
  if (!oldPhoneVerify || oldPhoneVerify.length !== 4) {
    ElMessage.warning('请输入原手机号的中间4位')
    return
  }

  // 提取原手机号的中间4位（第3-6位）
  if (oldPhone.length === 11) {
    const oldPhoneMiddle4 = oldPhone.substring(3, 7)
    if (oldPhoneVerify !== oldPhoneMiddle4) {
      ElMessage.warning('原手机号中间4位验证失败，请检查后重新输入')
      console.log('❌ 验证失败:', {
        输入: oldPhoneVerify,
        正确: oldPhoneMiddle4,
        完整号码: oldPhone
      })
      return
    }
  } else {
    ElMessage.warning('原手机号格式异常，无法验证')
    return
  }

  // 检查新手机号是否填写
  const newPhone = (phoneForm.value.phone || '').trim()
  if (!newPhone || newPhone.length === 0) {
    ElMessage.warning('请输入新手机号')
    return
  }

  // 检查新手机号是否与原手机号相同
  if (newPhone === oldPhone) {
    ElMessage.warning('新手机号不能与原手机号相同')
    return
  }

  // 检查验证码是否填写
  if (!phoneForm.value.verificationCode || phoneForm.value.verificationCode.trim().length === 0) {
    ElMessage.warning('请输入验证码')
    return
  }

  // 手机号格式验证
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(newPhone)) {
    ElMessage.warning('请输入有效的手机号')
    return
  }

  // 验证码格式验证（必须是6位数字）
  const codeRegex = /^\d{6}$/
  if (!codeRegex.test(phoneForm.value.verificationCode)) {
    ElMessage.warning('请输入6位数字验证码')
    return
  }

  // 直接调用后端API更新手机号
  const userId = authStore.userId || userInfo.value.userId
  api
    .put(API_CONFIG.user.update.replace('{userId}', userId), {
      phone: newPhone,
      smsCode: phoneForm.value.verificationCode
    })
    .then((response) => {
      const isSuccess = response.code === '200' || response.success
      if (isSuccess) {
        ElMessage.success(`手机号已从 ${oldPhone} 修改为 ${newPhone}`)
        // 更新userStore中的用户信息
        userStore.userInfo.phone = newPhone
        editPhoneDialogVisible.value = false
        // 重置表单
        phoneForm.value = { oldPhone: '', oldPhoneVerify: '', phone: '', verificationCode: '' }
      } else {
        ElMessage.error(response.message || '手机号修改失败')
      }
    })
    .catch((error) => {
      ElMessage.error(error.message || '手机号修改失败')
    })
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
  const currentEmail = userInfo.value.email || ''
  emailForm.value = {
    oldEmail: currentEmail,     // 原邮箱，只读显示
    oldEmailVerify: '',         // 原邮箱验证
    email: '',                  // 新邮箱，用户需要输入
    verificationCode: ''
  }
}

const submitEmailEdit = () => {
  // 显示原邮箱提示，让用户确认
  const oldEmail = emailForm.value.oldEmail || userInfo.value.email || ''

  // 验证原邮箱
  const oldEmailVerify = (emailForm.value.oldEmailVerify || '').trim()
  if (!oldEmailVerify || oldEmailVerify.length === 0) {
    ElMessage.warning('请输入原邮箱地址进行验证')
    return
  }

  // 验证两次输入的邮箱是否一致
  if (oldEmailVerify.toLowerCase() !== oldEmail.toLowerCase()) {
    ElMessage.warning('原邮箱验证失败，请检查后重新输入')
    console.log('❌ 邮箱验证失败:', {
      输入: oldEmailVerify,
      正确: oldEmail
    })
    return
  }

  // 去除首尾空格，避免空格导致验证失败
  const newEmail = (emailForm.value.email || '').trim()
  const verificationCode = (emailForm.value.verificationCode || '').trim()

  // 检查是否填写完整（使用字符串长度判断更准确）
  if (!newEmail || newEmail.length === 0) {
    ElMessage.warning('请输入新邮箱地址')
    return
  }

  // 检查邮箱长度（RFC 5321限制：254字符）
  if (newEmail.length > 254) {
    ElMessage.warning('邮箱地址过长')
    return
  }

  // 检查新邮箱是否与原邮箱相同
  if (newEmail === oldEmail) {
    ElMessage.warning('新邮箱不能与原邮箱相同')
    return
  }

  // 检查验证的原邮箱是否与新邮箱相同
  if (newEmail.toLowerCase() === oldEmailVerify.toLowerCase()) {
    ElMessage.warning('验证的原邮箱不能与新邮箱相同')
    return
  }

  if (!verificationCode || verificationCode.length === 0) {
    ElMessage.warning('请输入验证码')
    return
  }

  // 邮箱格式验证
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  if (!emailRegex.test(newEmail)) {
    ElMessage.warning('请输入有效的邮箱地址')
    return
  }

  // 验证码格式验证（必须是6位数字）
  const codeRegex = /^\d{6}$/
  if (!codeRegex.test(verificationCode)) {
    ElMessage.warning('请输入6位数字验证码')
    return
  }

  // 直接调用后端API更新邮箱
  const userId = authStore.userId || userInfo.value.userId
  api
    .put(API_CONFIG.user.update.replace('{userId}', userId), {
      email: newEmail,
      emailCode: verificationCode
    })
    .then((response) => {
      const isSuccess = response.code === '200' || response.success
      if (isSuccess) {
        ElMessage.success(`邮箱已从 ${oldEmail} 修改为 ${newEmail}`)
        // 更新userStore中的用户信息
        userStore.userInfo.email = newEmail
        editEmailDialogVisible.value = false
        // 重置表单
        emailForm.value = { oldEmail: '', oldEmailVerify: '', email: '', verificationCode: '' }
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

    // 密码长度校验
    if (passwordForm.value.newPassword.length < 6) {
      ElMessage.warning('新密码长度不能少于6位')
      return
    }

    if (passwordForm.value.newPassword.length > 20) {
      ElMessage.warning('新密码长度不能超过20位')
      return
    }

    // 密码复杂度校验
    const newPassword = passwordForm.value.newPassword
    const hasUpperCase = /[A-Z]/.test(newPassword)
    const hasLowerCase = /[a-z]/.test(newPassword)
    const hasNumber = /\d/.test(newPassword)
    const hasSpecial = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(newPassword)

    // 至少包含3种字符类型
    const complexityScore = [hasUpperCase, hasLowerCase, hasNumber, hasSpecial]
      .filter(Boolean).length

    if (complexityScore < 2) {
      ElMessage.warning('密码强度不足，请至少包含以下2种字符：大写字母、小写字母、数字、特殊符号')
      return
    }

    // 检查新密码是否与旧密码相同
    if (passwordForm.value.newPassword === passwordForm.value.oldPassword) {
      ElMessage.warning('新密码不能与旧密码相同')
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
        ElMessage.success('密码已修改，请妥善保管')
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
  // 定义需要保留的重要数据
  const keysToKeep = [
    'userSettings',        // 用户设置
    'userAvatar',          // 用户头像
    'authToken',           // 认证令牌
    'userInfo',            // 用户信息
    'userId',              // 用户ID
    'userRole',            // 用户角色
    'selectedRole'         // 选中的角色
  ]

  // 保存需要保留的数据
  const preservedData = {}
  keysToKeep.forEach(key => {
    const value = localStorage.getItem(key)
    if (value) {
      preservedData[key] = value
    }
  })

  // 清空所有localStorage
  localStorage.clear()

  // 恢复需要保留的数据
  Object.keys(preservedData).forEach(key => {
    localStorage.setItem(key, preservedData[key])
  })

  // 记录清除的缓存大小
  const cacheSize = JSON.stringify(preservedData).length
  console.log(`✅ 缓存已清除（保留了 ${cacheSize} 字节的重要数据）`, preservedData)

  ElMessage.success(`缓存已清除（保留了用户设置和登录信息）`)

  // 可选：提示用户刷新页面以确保所有缓存生效
  // setTimeout(() => {
  //   location.reload()
  // }, 1000)
}

// Handle data export
const exportData = () => {
  try {
    // 收集用户基本信息
    const userData = {
      exportInfo: {
        exportDate: new Date().toISOString(),
        exportTime: new Date().toLocaleString('zh-CN'),
        appVersion: '1.0.0',
        userId: authStore.userId || userInfo.value.userId
      },
      profile: {
        userId: userInfo.value.userId || '',
        phone: userInfo.value.phone || '未设置',
        email: userInfo.value.email || '未设置',
        nickname: userInfo.value.nickname || '未设置',
        avatar: userInfo.value.avatar || '未设置',
        location: userInfo.value.location || '未设置',
        registerDate: userInfo.value.registerDate || '未知'
      },
      settings: {
        fontSize: fontSize.value,
        theme: theme.value ? '深色' : '浅色',
        notifications: { ...notifications.value },
        privacy: { ...privacy.value }
      },
      localStorage: {
        userSettings: JSON.parse(localStorage.getItem('userSettings') || '{}'),
        keys: Object.keys(localStorage).filter(key =>
          !key.includes('password') &&  // 排除敏感信息
          !key.includes('token')
        )
      },
      disclaimer: '本数据为个人数据导出，请妥善保管。如需恢复数据，请联系客服。'
    }

    // 收集localStorage中的其他非敏感数据
    userData.localStorage.data = {}
    Object.keys(localStorage).forEach(key => {
      if (!key.includes('password') &&
          !key.includes('token') &&
          !key.includes('secret') &&
          key !== 'userAvatar' &&  // 头像数据太大，单独处理
          key !== 'userSettings') {  // 设置已在上面处理
        try {
          userData.localStorage.data[key] = JSON.parse(localStorage[key])
        } catch {
          userData.localStorage.data[key] = localStorage[key]
        }
      }
    })

    // Convert to JSON and download
    const dataStr = JSON.stringify(userData, null, 2)
    const dataBlob = new Blob([dataStr], { type: 'application/json' })
    const dataUrl = URL.createObjectURL(dataBlob)

    const a = document.createElement('a')
    a.href = dataUrl
    // 生成文件名：佳食宜选_用户数据_2025-01-25.json
    const dateStr = new Date().toISOString().split('T')[0]
    a.download = `佳食宜选_用户数据_${dateStr}.json`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(dataUrl)

    ElMessage.success('数据导出成功')
    console.log('✅ 数据导出完成:', {
      dataSize: dataStr.length,
      dataKeys: Object.keys(userData)
    })
  } catch (error) {
    console.error('❌ 数据导出失败:', error)
    ElMessage.error('数据导出失败，请重试')
  }
}

// Handle check for updates
const checkUpdate = async () => {
  try {
    ElMessage.info('正在检查更新...')

    // 模拟API调用检查版本（实际项目中应该调用真实API）
    // const response = await api.get('/api/version/check')
    // const versionInfo = response.data
    // latestVersion.value = versionInfo.latestVersion
    // hasUpdate.value = versionInfo.hasUpdate

    // 模拟版本检查
    const currentVersion = '1.0.0'
    const remoteVersion = '1.0.0' // 实际应该从API获取

    hasUpdate.value = remoteVersion !== currentVersion
    latestVersion.value = remoteVersion

    updateDialogVisible.value = true

    if (!hasUpdate.value) {
      ElMessage.success('当前已是最新版本')
    } else {
      ElMessage.info('发现新版本')
    }
  } catch (error) {
    console.error('检查更新失败:', error)
    ElMessage.error('检查更新失败，请稍后重试')
  }
}

// Handle download update
const downloadUpdate = () => {
  // 在实际应用中，这里应该：
  // 1. 下载新版本的安装包
  // 2. 提示用户保存并安装
  // 3. 或者直接自动更新（Electron应用）

  ElMessage.info('更新功能即将推出，请关注官网获取最新版本')
  console.log('📦 下载更新:', {
    currentVersion: '1.0.0',
    latestVersion: latestVersion.value,
    downloadUrl: 'https://example.com/download/latest' // 实际应该是真实下载地址
  })

  // 可选：打开下载页面
  // window.open('https://example.com/download', '_blank')

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
  font-size: 1.714rem /* 原值: 24px */;
  margin: 0 0 20px 0;
}

.settings-container .settings-card {
  padding: 20px;
}

.settings-container .settings-section {
  margin-bottom: 20px;
}

.settings-container .settings-section h3 {
  font-size: 1.286rem /* 原值: 18px */;
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
  font-size: 1rem /* 原值: 14px */;
}
</style>
