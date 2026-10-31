<script setup>
import { ref, onMounted } from 'vue'
import { Setting, Connection } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getSystemConfigsByGroup,
  batchUpdateSystemConfigs,
  getAiRuntimeConfig,
  testAiConnectivity
} from '@/api/admin'

const activeTab = ref('system')
const loading = ref(false)
const saving = ref(false)

// 系统设置
const systemSettings = ref({
  siteName: '佳食宜选',
  allowUserUpload: false,
  requireTutorialReview: true,
  aiTutorialEnabled: true
})

// 通知设置
const notificationSettings = ref({
  emailEnabled: true,
  smsEnabled: false,
  reviewNotifyEnabled: true
})

// 安全设置
const securitySettings = ref({
  sessionTimeout: 30,
  maxLoginAttempts: 5,
  passwordMinLength: 6
})

// AI 配置（只读）
const aiLoading = ref(false)
const testingAi = ref(false)
const connectivityResult = ref(null)
const aiConfig = ref({
  provider: '',
  model: '',
  supervisorModel: '',
  visionModel: '',
  baseUrl: '',
  timeoutMs: 30000,
  temperature: 0.7,
  updatedAt: '',
  keyStatus: { configured: false, fingerprint: '', last4: '' },
  capabilities: {
    available: false,
    textChat: false,
    streaming: false,
    vision: false,
    functionCalling: false,
    agentic: false,
    unavailableMessage: ''
  },
  supportedModels: [],
  providers: []
})

// 配置键映射
const configKeyMap = {
  // 系统设置
  system: {
    siteName: 'system.site.name',
    allowUserUpload: 'system.user.upload.enabled',
    requireTutorialReview: 'system.tutorial.review.required',
    aiTutorialEnabled: 'system.ai.tutorial.enabled'
  },
  // 通知设置
  notification: {
    emailEnabled: 'notification.email.enabled',
    smsEnabled: 'notification.sms.enabled',
    reviewNotifyEnabled: 'notification.review.notify.enabled'
  },
  // 安全设置
  security: {
    sessionTimeout: 'security.session.timeout',
    maxLoginAttempts: 'security.login.maxAttempts',
    passwordMinLength: 'security.password.minLength'
  }
}

/**
 * 加载系统配置
 */
async function loadSystemSettings() {
  loading.value = true
  try {
    const response = await getSystemConfigsByGroup('system')
    if (response.success && response.data) {
      const configs = response.data
      systemSettings.value.siteName = configs[configKeyMap.system.siteName] || '佳食宜选'
      systemSettings.value.allowUserUpload = configs[configKeyMap.system.allowUserUpload] === 'true'
      systemSettings.value.requireTutorialReview = configs[configKeyMap.system.requireTutorialReview] === 'true'
      systemSettings.value.aiTutorialEnabled = configs[configKeyMap.system.aiTutorialEnabled] === 'true'
    }
  } catch (error) {
    console.error('加载系统设置失败:', error)
    ElMessage.error('加载系统设置失败')
  } finally {
    loading.value = false
  }
}

/**
 * 加载通知配置
 */
async function loadNotificationSettings() {
  loading.value = true
  try {
    const response = await getSystemConfigsByGroup('notification')
    if (response.success && response.data) {
      const configs = response.data
      notificationSettings.value.emailEnabled = configs[configKeyMap.notification.emailEnabled] === 'true'
      notificationSettings.value.smsEnabled = configs[configKeyMap.notification.smsEnabled] === 'true'
      notificationSettings.value.reviewNotifyEnabled = configs[configKeyMap.notification.reviewNotifyEnabled] === 'true'
    }
  } catch (error) {
    console.error('加载通知设置失败:', error)
    ElMessage.error('加载通知设置失败')
  } finally {
    loading.value = false
  }
}

/**
 * 加载安全配置
 */
async function loadSecuritySettings() {
  loading.value = true
  try {
    const response = await getSystemConfigsByGroup('security')
    if (response.success && response.data) {
      const configs = response.data
      securitySettings.value.sessionTimeout = parseInt(configs[configKeyMap.security.sessionTimeout]) || 30
      securitySettings.value.maxLoginAttempts = parseInt(configs[configKeyMap.security.maxLoginAttempts]) || 5
      securitySettings.value.passwordMinLength = parseInt(configs[configKeyMap.security.passwordMinLength]) || 6
    }
  } catch (error) {
    console.error('加载安全设置失败:', error)
    ElMessage.error('加载安全设置失败')
  } finally {
    loading.value = false
  }
}

/**
 * 保存系统设置
 */
async function saveSystemSettings() {
  saving.value = true
  try {
    const configs = {
      [configKeyMap.system.siteName]: systemSettings.value.siteName,
      [configKeyMap.system.allowUserUpload]: String(systemSettings.value.allowUserUpload),
      [configKeyMap.system.requireTutorialReview]: String(systemSettings.value.requireTutorialReview),
      [configKeyMap.system.aiTutorialEnabled]: String(systemSettings.value.aiTutorialEnabled)
    }

    const response = await batchUpdateSystemConfigs('system', configs)
    if (response.success) {
      ElMessage.success('系统设置保存成功')
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存系统设置失败:', error)
    ElMessage.error('保存系统设置失败')
  } finally {
    saving.value = false
  }
}

/**
 * 保存通知设置
 */
async function saveNotificationSettings() {
  saving.value = true
  try {
    const configs = {
      [configKeyMap.notification.emailEnabled]: String(notificationSettings.value.emailEnabled),
      [configKeyMap.notification.smsEnabled]: String(notificationSettings.value.smsEnabled),
      [configKeyMap.notification.reviewNotifyEnabled]: String(notificationSettings.value.reviewNotifyEnabled)
    }

    const response = await batchUpdateSystemConfigs('notification', configs)
    if (response.success) {
      ElMessage.success('通知设置保存成功')
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存通知设置失败:', error)
    ElMessage.error('保存通知设置失败')
  } finally {
    saving.value = false
  }
}

/**
 * 保存安全设置
 */
async function saveSecuritySettings() {
  saving.value = true
  try {
    const configs = {
      [configKeyMap.security.sessionTimeout]: String(securitySettings.value.sessionTimeout),
      [configKeyMap.security.maxLoginAttempts]: String(securitySettings.value.maxLoginAttempts),
      [configKeyMap.security.passwordMinLength]: String(securitySettings.value.passwordMinLength)
    }

    const response = await batchUpdateSystemConfigs('security', configs)
    if (response.success) {
      ElMessage.success('安全设置保存成功')
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存安全设置失败:', error)
    ElMessage.error('保存安全设置失败')
  } finally {
    saving.value = false
  }
}

/**
 * 加载 AI 运行时配置快照
 */
async function loadAiConfig() {
  aiLoading.value = true
  try {
    const response = await getAiRuntimeConfig()
    if (response.success && response.data) {
      aiConfig.value = {
        ...aiConfig.value,
        ...response.data,
        keyStatus: response.data.keyStatus || aiConfig.value.keyStatus,
        capabilities: response.data.capabilities || aiConfig.value.capabilities,
        supportedModels: response.data.supportedModels || [],
        providers: response.data.providers || []
      }
    }
  } catch (error) {
    console.error('加载 AI 配置失败:', error)
    ElMessage.error(error?.response?.data?.message || '无权访问 AI 配置或加载失败')
  } finally {
    aiLoading.value = false
  }
}

/**
 * 测试当前 AI 配置连通性
 */
async function runAiConnectivityTest() {
  testingAi.value = true
  try {
    const response = await testAiConnectivity()
    if (response.success && response.data) {
      connectivityResult.value = response.data
      ElMessage.success(response.data.message || '连通性测试完成')
    }
  } catch (error) {
    connectivityResult.value = {
      success: false,
      message: error?.response?.data?.message || '连通性测试失败'
    }
    ElMessage.error(connectivityResult.value.message)
  } finally {
    testingAi.value = false
  }
}

function handleTabChange(name) {
  if (name === 'ai' && !aiConfig.value.updatedAt) {
    loadAiConfig()
  }
}

// 组件挂载时加载所有配置
onMounted(async () => {
  await Promise.all([
    loadSystemSettings(),
    loadNotificationSettings(),
    loadSecuritySettings()
  ])
})
</script>

<template>
  <div class="admin-settings-container">
    <el-card shadow="never">
      <template #header>
        <div class="header">
          <h3>
            <el-icon><Setting /></el-icon>
            系统设置
          </h3>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 系统设置 -->
        <el-tab-pane label="系统设置" name="system">
          <el-form :model="systemSettings" label-width="180px" style="max-width: 600px" v-loading="loading">
            <h4>基本设置</h4>
            <el-form-item label="网站名称">
              <el-input v-model="systemSettings.siteName" />
            </el-form-item>

            <el-form-item label="允许用户上传教程">
              <el-switch v-model="systemSettings.allowUserUpload" />
              <span class="form-tip">开启后，普通用户可以上传自己的教程</span>
            </el-form-item>

            <el-form-item label="教程审核机制">
              <el-switch v-model="systemSettings.requireTutorialReview" />
              <span class="form-tip">商家和AI生成的教程需要审核后发布</span>
            </el-form-item>

            <el-form-item label="AI教程生成">
              <el-switch v-model="systemSettings.aiTutorialEnabled" />
              <span class="form-tip">启用AI自动生成教程内容</span>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveSystemSettings" :loading="saving">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 通知设置 -->
        <el-tab-pane label="通知设置" name="notification">
          <el-form :model="notificationSettings" label-width="180px" style="max-width: 600px" v-loading="loading">
            <h4>通知方式</h4>

            <el-form-item label="邮件通知">
              <el-switch v-model="notificationSettings.emailEnabled" />
              <span class="form-tip">审核结果等重要事件将通过邮件通知</span>
            </el-form-item>

            <el-form-item label="短信通知">
              <el-switch v-model="notificationSettings.smsEnabled" />
              <span class="form-tip">紧急通知将通过短信发送</span>
            </el-form-item>

            <el-form-item label="审核通知">
              <el-switch v-model="notificationSettings.reviewNotifyEnabled" />
              <span class="form-tip">商家提交审核时通知管理员</span>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveNotificationSettings" :loading="saving">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 安全设置 -->
        <el-tab-pane label="安全设置" name="security">
          <el-form :model="securitySettings" label-width="180px" style="max-width: 600px" v-loading="loading">
            <h4>会话管理</h4>

            <el-form-item label="会话超时时间">
              <el-input-number v-model="securitySettings.sessionTimeout" :min="5" :max="120" />
              <span class="form-tip">单位：分钟，超时后需要重新登录</span>
            </el-form-item>

            <h4>登录安全</h4>

            <el-form-item label="最大登录尝试次数">
              <el-input-number v-model="securitySettings.maxLoginAttempts" :min="3" :max="10" />
              <span class="form-tip">超过次数后将锁定账户</span>
            </el-form-item>

            <el-form-item label="密码最小长度">
              <el-input-number v-model="securitySettings.passwordMinLength" :min="6" :max="20" />
              <span class="form-tip">用户密码的最小长度要求</span>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveSecuritySettings" :loading="saving">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <!-- AI 模型（只读） -->
        <el-tab-pane label="AI 模型" name="ai">
          <div v-loading="aiLoading" style="max-width: 900px">
            <el-alert
              title="AI 配置当前为只读模式"
              description="这里展示后端当前生效的 AI 运行时配置，不会保存或热切换配置。API key 不会返回明文。"
              type="info"
              show-icon
              :closable="false"
              class="ai-alert"
            />

            <h4>当前配置</h4>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="Provider">{{ aiConfig.provider || '-' }}</el-descriptions-item>
              <el-descriptions-item label="更新时间">{{ aiConfig.updatedAt || '-' }}</el-descriptions-item>
              <el-descriptions-item label="聊天模型">{{ aiConfig.model || '-' }}</el-descriptions-item>
              <el-descriptions-item label="Supervisor 模型">{{ aiConfig.supervisorModel || '-' }}</el-descriptions-item>
              <el-descriptions-item label="视觉模型">{{ aiConfig.visionModel || '-' }}</el-descriptions-item>
              <el-descriptions-item label="服务地址">{{ aiConfig.baseUrl || '供应商默认' }}</el-descriptions-item>
              <el-descriptions-item label="超时时间">{{ aiConfig.timeoutMs }} ms</el-descriptions-item>
              <el-descriptions-item label="温度">{{ aiConfig.temperature }}</el-descriptions-item>
            </el-descriptions>

            <h4>API Key 状态</h4>
            <el-tag :type="aiConfig.keyStatus.configured ? 'success' : 'danger'">
              {{ aiConfig.keyStatus.configured ? '已配置' : '未配置' }}
            </el-tag>
            <span v-if="aiConfig.keyStatus.configured" class="key-status">
              指纹：{{ aiConfig.keyStatus.fingerprint }} / 尾号：{{ aiConfig.keyStatus.last4 }}
            </span>

            <h4>能力状态</h4>
            <div class="capability-tags">
              <el-tag :type="aiConfig.capabilities.available ? 'success' : 'info'">可用</el-tag>
              <el-tag :type="aiConfig.capabilities.textChat ? 'success' : 'info'">文本对话</el-tag>
              <el-tag :type="aiConfig.capabilities.streaming ? 'success' : 'info'">流式输出</el-tag>
              <el-tag :type="aiConfig.capabilities.vision ? 'success' : 'info'">视觉理解</el-tag>
              <el-tag :type="aiConfig.capabilities.functionCalling ? 'success' : 'info'">函数调用</el-tag>
              <el-tag :type="aiConfig.capabilities.agentic ? 'success' : 'info'">Agent</el-tag>
            </div>
            <div v-if="aiConfig.capabilities.unavailableMessage" class="unavailable-message">
              {{ aiConfig.capabilities.unavailableMessage }}
            </div>

            <h4>支持模型</h4>
            <div class="model-tags">
              <el-tag v-for="model in aiConfig.supportedModels" :key="model" class="model-tag">
                {{ model }}
              </el-tag>
            </div>

            <div class="connectivity-section">
              <el-button type="primary" :icon="Connection" :loading="testingAi" @click="runAiConnectivityTest">
                测试当前配置连通性
              </el-button>
              <div v-if="connectivityResult" class="connectivity-result">
                <el-tag :type="connectivityResult.success ? 'success' : 'danger'">
                  {{ connectivityResult.success ? '连通成功' : '连通失败' }}
                </el-tag>
                <span class="connectivity-message">{{ connectivityResult.message }}</span>
                <span v-if="connectivityResult.model" class="connectivity-message">（{{ connectivityResult.model }}）</span>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<style scoped lang="less">
.admin-settings-container {
  padding: 20px;

  .header {
    h3 {
      margin: 0;
      font-size: 1.429rem /* 原值: 20px */;
      color: #303133;
      display: flex;
      align-items: center;
      gap: 10px;
    }
  }

  h4 {
    font-size: 1.143rem /* 原值: 16px */;
      color: #303133;
      margin: 20px 0 15px 0;
      padding-bottom: 8px;
      border-bottom: 2px solid #ff6b6b;
  }

  .ai-alert {
    margin-bottom: 16px;
  }

  .key-status {
    margin-left: 10px;
    color: #606266;
  }

  .capability-tags,
  .model-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .model-tag {
    font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  }

  .unavailable-message {
    margin-top: 8px;
    color: #909399;
  }

  .connectivity-section {
    margin-top: 20px;
    padding-top: 16px;
    border-top: 1px solid #ebeef5;
  }

  .connectivity-result {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 12px;
  }

  .connectivity-message {
    color: #606266;
  }

  .form-tip {
    margin-left: 10px;
    color: #909399;
    font-size: 0.857rem /* 原值: 12px */;
  }
}
</style>
