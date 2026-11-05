<script setup>
import { ref, computed, onMounted } from 'vue'
import { Setting, Connection, Refresh, Warning } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getSystemConfigsByGroup,
  batchUpdateSystemConfigs,
  getAiRuntimeConfig,
  testAiConnectivity,
  testAiCandidate,
  discoverAiModels,
  getAiPreviousConfigSummary,
  rollbackAiConfig,
  activateAiConfig,
  rebuildEmbeddingIndex
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

// AI 配置
const aiLoading = ref(false)
const testingAi = ref(false)
const activatingAi = ref(false)
const rollingBackAi = ref(false)
const refreshingAiState = ref(false)
const rebuildingEmbedding = ref(false)
const aiAdvanced = ref(false)
const supervisorFollowsChat = ref(true)
const aiEditing = ref(false)
const embeddingCollapsed = ref(["embedding"])
const historyCollapsed = ref([])
const embeddingExpanded = ref(false)
const candidateTestResult = ref(null)
const aiDiscoveredModels = ref([])
const loadingAiModels = ref(false)
const previousAiConfig = ref(null)
const connectivityResult = ref(null)
const aiCandidate = ref({
  provider: '',
  model: '',
  supervisorModel: '',
  visionModel: '',
  baseUrl: '',
  timeoutMs: 30000,
  temperature: 0.7,
  apiKey: '',
  embedding: {
    provider: '',
    model: '',
    dimensions: 1024,
    baseUrl: '',
    apiKey: ''
  }
})
const aiConfig = ref({
  provider: '',
  model: '',
  supervisorModel: '',
  visionModel: '',
  baseUrl: '',
  timeoutMs: 30000,
  temperature: 0.7,
  visionVerified: false,
  visionVerificationReason: '',
  needsReconfiguration: false,
  configurationNotice: '',
  updatedAt: '',
  keyStatus: { configured: false, fingerprint: '', updatedAt: '' },
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
  providers: [],
  embedding: {
    provider: '',
    model: '',
    dimensions: 1024,
    baseUrl: '',
    providers: [],
    keyStatus: { configured: false, fingerprint: '', updatedAt: '' }
  }
})

// #188 公告索引重建状态展示；状态字段可能为 null/缺失（老后端），按未知兜底
const EMBEDDING_STATE_LABELS = {
  READY: { label: '就绪', type: 'success' },
  REBUILDING: { label: '重建中', type: 'warning' },
  UNAVAILABLE: { label: '不可用', type: 'danger' },
  NOT_CONFIGURED: { label: '未配置', type: 'info' },
  DISABLED: { label: '已关闭', type: 'info' }
}

const embeddingStateMeta = computed(() => {
  const state = aiConfig.value.embedding?.state
  if (state && EMBEDDING_STATE_LABELS[state]) {
    return EMBEDDING_STATE_LABELS[state]
  }
  if (typeof state === 'string' && state.trim()) {
    return { label: state, type: 'info' }
  }
  return { label: '未知', type: 'info' }
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
    const [response, previousResponse] = await Promise.all([
      getAiRuntimeConfig(),
      getAiPreviousConfigSummary().catch(() => null)
    ])
    if (response.success && response.data) {
      previousAiConfig.value = previousResponse?.data || null
      applyAiSnapshot(response.data)
    }
  } catch (error) {
    console.error('加载 AI 配置失败:', error)
    ElMessage.error(error?.response?.data?.message || '无权访问 AI 配置或加载失败')
  } finally {
    aiLoading.value = false
  }
}

function applyAiSnapshot(snapshot = {}) {
  aiConfig.value = {
    ...aiConfig.value,
    ...snapshot,
    keyStatus: snapshot.keyStatus || aiConfig.value.keyStatus,
    capabilities: snapshot.capabilities || aiConfig.value.capabilities,
    supportedModels: snapshot.supportedModels || [],
    providers: snapshot.providers || [],
    embedding: {
      ...aiConfig.value.embedding,
      ...(snapshot.embedding || {}),
      providers: snapshot.embedding?.providers || aiConfig.value.embedding.providers,
      keyStatus: snapshot.embedding?.keyStatus || aiConfig.value.embedding.keyStatus
    }
  }
  resetAiCandidateFromCurrent(aiConfig.value)
}

function resetAiCandidateFromCurrent(config = {}) {
  aiCandidate.value = {
    provider: config.provider || '',
    model: config.model || '',
    supervisorModel: config.model || '',
    visionModel: config.visionModel || '',
    baseUrl: config.baseUrl || '',
    timeoutMs: config.timeoutMs || 30000,
    temperature: config.temperature || 0.7,
    apiKey: '',
    embedding: {
      provider: config.embedding?.provider || '',
      model: config.embedding?.model || '',
      dimensions: config.embedding?.dimensions ?? 1024,
      baseUrl: config.embedding?.baseUrl || '',
      apiKey: ''
    }
  }
  supervisorFollowsChat.value = true
}

// Supervisor 默认跟随聊天模型；用户在高级设置里单独选择其它模型后才停止跟随
function handleChatModelChange(value) {
  if (supervisorFollowsChat.value) {
    aiCandidate.value.supervisorModel = value
  }
}

function handleSupervisorModelChange() {
  supervisorFollowsChat.value = false
}

function selectedProviderModels() {
  const provider = aiConfig.value.providers?.find(item => item.code === aiCandidate.value.provider)
  return provider?.models || []
}

// 优先用"从网关获取"的真实模型列表；否则回落到 registry 给的预设
const aiModelOptions = computed(() => {
  if (aiDiscoveredModels.value.length) {
    return aiDiscoveredModels.value.map(item => ({ value: item.id, label: item.name || item.id }))
  }
  return selectedProviderModels().map(id => ({ value: id, label: id }))
})

// 有图像能力信息时，视觉下拉只保留支持图片输入的模型
const aiVisionModelOptions = computed(() => {
  if (aiDiscoveredModels.value.length) {
    const vision = aiDiscoveredModels.value.filter(item => item.supportsImages)
    const usable = vision.length ? vision : aiDiscoveredModels.value
    return usable.map(item => ({ value: item.id, label: item.name || item.id }))
  }
  return aiModelOptions.value
})

// 能力清单（用于状态总览卡）
const capabilityList = computed(() => [
  { key: 'textChat', label: '文本对话', enabled: aiConfig.value.capabilities?.textChat },
  { key: 'streaming', label: '流式输出', enabled: aiConfig.value.capabilities?.streaming },
  { key: 'vision', label: '视觉理解', enabled: aiConfig.value.capabilities?.vision },
  { key: 'functionCalling', label: '函数调用', enabled: aiConfig.value.capabilities?.functionCalling },
  { key: 'agentic', label: 'Agent', enabled: aiConfig.value.capabilities?.agentic }
])

// AI 整体状态
const aiOverallStatus = computed(() => {
  if (!aiConfig.value.capabilities?.available) {
    return { label: '未配置', type: 'info', description: aiConfig.value.capabilities?.unavailableMessage || '尚未配置 AI 服务' }
  }
  if (!aiConfig.value.visionVerified && aiConfig.value.visionVerificationReason) {
    return { label: '部分能力受限', type: 'warning', description: '视觉理解未通过验证：' + aiConfig.value.visionVerificationReason }
  }
  return { label: '运行正常', type: 'success', description: '全部能力可用' }
})

// 进入编辑模式
function enterEditMode() {
  resetAiCandidateFromCurrent(aiConfig.value)
  aiDiscoveredModels.value = []
  candidateTestResult.value = null
  // 有 embedding 配置时默认展开
  embeddingExpanded.value = !!(aiConfig.value.embedding?.provider || aiConfig.value.embedding?.model)
  aiEditing.value = true
}

function cancelEditMode() {
  aiEditing.value = false
  candidateTestResult.value = null
}

function handleSaveAndActivate() {
  saveAndActivateAi().then((saved) => {
    if (saved) {
      aiEditing.value = false
      candidateTestResult.value = null
    }
  })
}

function handleCandidateTest() {
  runAiCandidateTest().then(() => {
    candidateTestResult.value = connectivityResult.value
  })
}

function handleCurrentTest() {
  runAiConnectivityTest()
}

function onProviderChange() {
  aiDiscoveredModels.value = []
}

async function loadAiModels() {
  if (!aiCandidate.value.provider) {
    ElMessage.warning('请先选择 Provider')
    return
  }
  loadingAiModels.value = true
  try {
    const response = await discoverAiModels(aiCandidate.value)
    const list = response?.data || []
    aiDiscoveredModels.value = list
    if (list.length) {
      ElMessage.success(`已获取 ${list.length} 个模型`)
    } else {
      ElMessage.warning('网关未返回模型列表，已回落到内置预设')
    }
  } catch (error) {
    aiDiscoveredModels.value = []
    ElMessage.error(error?.response?.data?.message || '获取模型列表失败')
  } finally {
    loadingAiModels.value = false
  }
}

async function runAiCandidateTest() {
  testingAi.value = true
  try {
    const response = await testAiCandidate(aiCandidate.value)
    if (response.success && response.data) {
      connectivityResult.value = response.data
      ElMessage.success(response.data.message || '候选配置测试完成')
    }
  } catch (error) {
    connectivityResult.value = {
      success: false,
      message: error?.response?.data?.message || '候选配置测试失败'
    }
    ElMessage.error(connectivityResult.value.message)
  } finally {
    testingAi.value = false
  }
}

async function rollbackAi() {
  rollingBackAi.value = true
  try {
    const response = await rollbackAiConfig()
    if (response.success && response.data) {
      applyAiSnapshot(response.data)
      await loadAiPreviousSummary()
      ElMessage.success('AI 配置已回滚')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '回滚失败，当前配置未改变')
  } finally {
    rollingBackAi.value = false
  }
}

async function loadAiPreviousSummary() {
  try {
    const response = await getAiPreviousConfigSummary()
    previousAiConfig.value = response?.data || null
  } catch {
    previousAiConfig.value = null
  }
}

// 后端 message 可能内嵌网关返回的 JSON，取出可读原因后原样展示
function aiErrorText(raw, fallback) {
  const message = typeof raw === 'string' ? raw : raw?.message || ''
  const text = message.trim()
  if (!text) {
    return fallback
  }
  if (text.startsWith('{')) {
    try {
      const parsed = JSON.parse(text)
      return parsed?.error?.message || parsed?.message || text
    } catch {
      return text
    }
  }
  return text
}

function showAiSaveError(rawMessage) {
  const message = aiErrorText(rawMessage, '保存或激活失败，当前配置未改变')
  ElMessageBox.alert(message, 'AI 配置保存失败', {
    type: 'error',
    confirmButtonText: '知道了'
  }).catch(() => {})
}

async function saveAndActivateAi() {
  activatingAi.value = true
  try {
    const response = await activateAiConfig(aiCandidate.value)
    if (response.success && response.data) {
      applyAiSnapshot(response.data)
      await loadAiPreviousSummary()
      ElMessage.success('AI 配置已保存并激活')
      return true
    }
    showAiSaveError(response?.message)
    return false
  } catch (error) {
    showAiSaveError(error?.response?.data?.message)
    return false
  } finally {
    activatingAi.value = false
  }
}

/**
 * 刷新 AI 运行时状态（只刷新展示，不改变候选配置以外的其它行为）
 */
async function refreshAiState() {
  refreshingAiState.value = true
  try {
    const response = await getAiRuntimeConfig()
    if (response.success && response.data) {
      applyAiSnapshot(response.data)
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '刷新 AI 状态失败')
  } finally {
    refreshingAiState.value = false
  }
}

/**
 * 重试公告 Embedding 索引重建
 */
async function retryEmbeddingRebuild() {
  rebuildingEmbedding.value = true
  try {
    const response = await rebuildEmbeddingIndex()
    if (response.success && response.data) {
      applyAiSnapshot(response.data)
      ElMessage.success('已触发公告索引重建')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '重试重建失败')
  } finally {
    rebuildingEmbedding.value = false
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
        <!-- AI 模型 -->
        <el-tab-pane label="AI 模型" name="ai">
          <div v-loading="aiLoading" class="ai-settings-wrapper">
            <!-- 状态总览卡 -->
            <el-card class="status-card" shadow="never">
              <template #header>
                <div class="status-card-header">
                  <div class="status-title-group">
                    <span class="status-dot" :class="aiOverallStatus.type"></span>
                    <span class="status-title">AI 服务</span>
                    <el-tag :type="aiOverallStatus.type" effect="dark" size="small" class="status-tag">
                      {{ aiOverallStatus.label }}
                    </el-tag>
                  </div>
                  <el-button v-if="!aiEditing" type="primary" size="default" @click="enterEditMode">
                    编辑配置
                  </el-button>
                  <el-button v-else text size="default" @click="cancelEditMode">
                    取消编辑
                  </el-button>
                </div>
              </template>

              <div class="capability-row">
                <div
                  v-for="cap in capabilityList"
                  :key="cap.key"
                  class="capability-item"
                  :class="{ enabled: cap.enabled }"
                >
                  <div class="capability-icon">
                    <svg v-if="cap.enabled" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="20 6 9 17 4 12"/>
                    </svg>
                    <svg v-else viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                      <line x1="18" y1="6" x2="6" y2="18"/>
                      <line x1="6" y1="6" x2="18" y2="18"/>
                    </svg>
                  </div>
                  <span class="capability-label">{{ cap.label }}</span>
                </div>
              </div>

              <div v-if="aiOverallStatus.type !== 'success'" class="status-description">
                {{ aiOverallStatus.description }}
              </div>

              <div v-if="aiConfig.needsReconfiguration" class="status-warning">
                <el-icon><Warning /></el-icon>
                <span>{{ aiConfig.configurationNotice || '当前配置需要重新填写' }}</span>
              </div>
            </el-card>

            <!-- 配置卡：只读态 -->
            <el-card v-if="!aiEditing" class="config-card" shadow="never">
              <template #header>
                <div class="card-header">
                  <span class="card-title">当前配置</span>
                  <span class="card-subtitle">更新于 {{ aiConfig.updatedAt || '—' }}</span>
                </div>
              </template>

              <el-descriptions :column="2" border size="default">
                <el-descriptions-item label="服务商">{{ aiConfig.provider || '—' }}</el-descriptions-item>
                <el-descriptions-item label="服务地址">
                  <span class="mono-text">{{ aiConfig.baseUrl || '未配置' }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="聊天模型">
                  <span class="mono-text">{{ aiConfig.model || '—' }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="API Key">
                  <el-tag :type="aiConfig.keyStatus.configured ? 'success' : 'danger'" size="small">
                    {{ aiConfig.keyStatus.configured ? '已配置' : '未配置' }}
                  </el-tag>
                  <span v-if="aiConfig.keyStatus.configured" class="key-fingerprint">
                    指纹：{{ aiConfig.keyStatus.fingerprint }}
                  </span>
                </el-descriptions-item>
                <el-descriptions-item label="视觉模型">
                  <span class="mono-text">{{ aiConfig.visionModel || '—' }}</span>
                  <el-tag
                    :type="aiConfig.visionVerified ? 'success' : 'warning'"
                    size="small"
                    style="margin-left: 8px"
                  >
                    {{ aiConfig.visionVerified ? '已验证' : '未验证' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="Supervisor 模型">
                  <span class="mono-text">{{ aiConfig.supervisorModel || '—' }}</span>
                </el-descriptions-item>
              </el-descriptions>

              <div class="config-actions">
                <el-button :icon="Connection" :loading="testingAi" @click="handleCurrentTest">
                  测试连接
                </el-button>
                <el-button type="primary" @click="enterEditMode">
                  修改配置
                </el-button>
                <div v-if="connectivityResult" class="connectivity-inline-result">
                  <el-tag :type="connectivityResult.success ? 'success' : 'danger'" size="small">
                    {{ connectivityResult.success ? '连通成功' : '连通失败' }}
                  </el-tag>
                  <span class="connectivity-message">{{ connectivityResult.message }}</span>
                </div>
              </div>
            </el-card>

            <!-- 配置卡：编辑态 -->
            <el-card v-else class="config-card editing" shadow="never">
              <template #header>
                <div class="card-header">
                  <span class="card-title">编辑配置</span>
                  <span class="card-subtitle editing-hint">修改后需保存并激活才会生效</span>
                </div>
              </template>

              <el-form :model="aiCandidate" label-width="120px" class="config-form">
                <el-form-item label="API Key">
                  <el-input
                    v-model="aiCandidate.apiKey"
                    type="password"
                    show-password
                    autocomplete="new-password"
                    placeholder="留空则沿用当前 key"
                  />
                </el-form-item>

                <el-form-item label="服务商">
                  <el-select v-model="aiCandidate.provider" placeholder="选择 Provider" @change="onProviderChange" style="width: 100%">
                    <el-option
                      v-for="provider in aiConfig.providers"
                      :key="provider.code"
                      :label="provider.name"
                      :value="provider.code"
                    />
                  </el-select>
                </el-form-item>

                <el-form-item label="服务地址">
                  <el-input v-model="aiCandidate.baseUrl" placeholder="OpenAI 兼容服务根地址" />
                </el-form-item>

                <el-form-item label="模型列表">
                  <el-button :icon="Refresh" :loading="loadingAiModels" @click="loadAiModels">
                    从网关获取模型
                  </el-button>
                  <span v-if="aiDiscoveredModels.length" class="form-tip">已获取 {{ aiDiscoveredModels.length }} 个模型</span>
                </el-form-item>

                <el-form-item label="聊天模型">
                  <el-select
                    v-model="aiCandidate.model"
                    filterable
                    :allow-create="aiAdvanced"
                    default-first-option
                    :placeholder="aiAdvanced ? '选择或输入自定义模型' : '选择模型'"
                    style="width: 100%"
                    @change="handleChatModelChange"
                  >
                    <el-option v-for="option in aiModelOptions" :key="option.value" :label="option.label" :value="option.value" />
                  </el-select>
                </el-form-item>

                <el-form-item label="视觉模型">
                  <el-select
                    v-model="aiCandidate.visionModel"
                    filterable
                    :allow-create="aiAdvanced"
                    default-first-option
                    :placeholder="aiAdvanced ? '选择或输入自定义模型' : '选择模型'"
                    style="width: 100%"
                  >
                    <el-option v-for="option in aiVisionModelOptions" :key="option.value" :label="option.label" :value="option.value" />
                  </el-select>
                  <div class="form-tip">保存激活时会发送真实图片请求验证；验证失败时菜品识别保持关闭。</div>
                </el-form-item>

                <el-form-item label="高级设置">
                  <el-switch v-model="aiAdvanced" active-text="展开" inactive-text="收起" />
                </el-form-item>

                <template v-if="aiAdvanced">
                  <el-form-item label="Supervisor">
                    <el-select
                      v-model="aiCandidate.supervisorModel"
                      filterable
                      :allow-create="aiAdvanced"
                      default-first-option
                      placeholder="选择 Supervisor 模型"
                      style="width: 100%"
                      @change="handleSupervisorModelChange"
                    >
                      <el-option v-for="option in aiModelOptions" :key="option.value" :label="option.label" :value="option.value" />
                    </el-select>
                    <div class="form-tip">默认跟随聊天模型；选择其它模型可单独指定。</div>
                  </el-form-item>
                  <el-form-item label="超时时间">
                    <el-input-number v-model="aiCandidate.timeoutMs" :min="1000" :step="1000" />
                    <span class="form-tip">毫秒</span>
                  </el-form-item>
                  <el-form-item label="温度">
                    <el-input-number v-model="aiCandidate.temperature" :min="0" :max="2" :step="0.1" />
                  </el-form-item>
                </template>

                <el-form-item label="Embedding">
                  <el-switch v-model="embeddingExpanded" active-text="配置" inactive-text="不配置" />
                </el-form-item>

                <template v-if="embeddingExpanded">
                  <div class="embedding-candidate">
                    <el-form-item label="Provider">
                      <el-select v-model="aiCandidate.embedding.provider" placeholder="选择 Embedding Provider" style="width: 100%">
                        <el-option
                          v-for="provider in aiConfig.embedding.providers"
                          :key="provider"
                          :label="provider"
                          :value="provider"
                        />
                      </el-select>
                    </el-form-item>
                    <el-form-item label="模型">
                      <el-input v-model="aiCandidate.embedding.model" placeholder="例如 bge-m3" />
                    </el-form-item>
                    <el-form-item label="向量维度">
                      <el-input-number v-model="aiCandidate.embedding.dimensions" :min="1" />
                    </el-form-item>
                    <el-form-item label="服务地址">
                      <el-input v-model="aiCandidate.embedding.baseUrl" placeholder="OpenAI 兼容服务根地址" />
                    </el-form-item>
                    <el-form-item label="API Key">
                      <el-input
                        v-model="aiCandidate.embedding.apiKey"
                        type="password"
                        show-password
                        autocomplete="new-password"
                        placeholder="留空沿用当前"
                      />
                    </el-form-item>
                  </div>
                </template>
              </el-form>

              <div class="edit-actions">
                <el-button @click="cancelEditMode">取消</el-button>
                <el-button :icon="Connection" :loading="testingAi" @click="handleCandidateTest">
                  测试候选配置
                </el-button>
                <el-button type="primary" :loading="activatingAi" @click="handleSaveAndActivate">
                  保存并激活
                </el-button>
              </div>

              <div v-if="candidateTestResult" class="test-result" :class="{ success: candidateTestResult.success, fail: !candidateTestResult.success }">
                <el-tag :type="candidateTestResult.success ? 'success' : 'danger'" size="small">
                  {{ candidateTestResult.success ? '测试通过' : '测试失败' }}
                </el-tag>
                <span class="test-result-message">{{ candidateTestResult.message }}</span>
                <span v-if="candidateTestResult.model" class="test-result-model">（{{ candidateTestResult.model }}）</span>
              </div>
            </el-card>

            <!-- Embedding 状态（折叠） -->
            <el-card class="section-card embedding-summary" shadow="never">
              <template #header>
                <el-collapse v-model="embeddingCollapsed">
                  <el-collapse-item name="embedding">
                    <template #title>
                      <div class="collapse-header">
                        <span>Embedding 配置</span>
                        <el-tag :type="embeddingStateMeta.type" size="small">
                          {{ embeddingStateMeta.label }}
                        </el-tag>
                      </div>
                    </template>

                    <el-descriptions :column="2" border size="small">
                      <el-descriptions-item label="Provider">{{ aiConfig.embedding.provider || '—' }}</el-descriptions-item>
                      <el-descriptions-item label="模型">
                        <span class="mono-text">{{ aiConfig.embedding.model || '—' }}</span>
                      </el-descriptions-item>
                      <el-descriptions-item label="向量维度">{{ aiConfig.embedding.dimensions || '—' }}</el-descriptions-item>
                      <el-descriptions-item label="服务地址">
                        <span class="mono-text">{{ aiConfig.embedding.baseUrl || '未配置' }}</span>
                      </el-descriptions-item>
                      <el-descriptions-item label="Key 状态" :span="2">
                        <el-tag :type="aiConfig.embedding.keyStatus?.configured ? 'success' : 'danger'" size="small">
                          {{ aiConfig.embedding.keyStatus?.configured ? '已配置' : '未配置' }}
                        </el-tag>
                        <span v-if="aiConfig.embedding.keyStatus?.configured" class="key-fingerprint">
                          指纹：{{ aiConfig.embedding.keyStatus.fingerprint }}
                          · 更新于 {{ aiConfig.embedding.keyStatus.updatedAt || '—' }}
                        </span>
                      </el-descriptions-item>
                    </el-descriptions>

                    <div class="embedding-actions">
                      <el-button :icon="Refresh" :loading="refreshingAiState" size="small" @click="refreshAiState">
                        刷新状态
                      </el-button>
                      <el-button type="primary" :loading="rebuildingEmbedding" size="small" @click="retryEmbeddingRebuild">
                        重试重建索引
                      </el-button>
                      <span v-if="aiConfig.embedding.statusReason" class="embedding-state-reason">
                        {{ aiConfig.embedding.statusReason }}
                      </span>
                    </div>
                  </el-collapse-item>
                </el-collapse>
              </template>
            </el-card>

            <!-- 配置历史（折叠） -->
            <el-card class="section-card" shadow="never">
              <el-collapse v-model="historyCollapsed">
                <el-collapse-item name="history">
                  <template #title>
                    <div class="collapse-header">
                      <span>配置历史</span>
                      <span v-if="previousAiConfig" class="history-subtitle">上一版可回滚</span>
                      <span v-else class="history-subtitle muted">暂无可回滚版本</span>
                    </div>
                  </template>

                  <div v-if="previousAiConfig" class="history-item">
                    <el-descriptions :column="2" border size="small">
                      <el-descriptions-item label="版本">{{ previousAiConfig.version || '—' }}</el-descriptions-item>
                      <el-descriptions-item label="生效时间">{{ previousAiConfig.updatedAt || '—' }}</el-descriptions-item>
                      <el-descriptions-item label="聊天模型">
                        <span class="mono-text">{{ previousAiConfig.model || '—' }}</span>
                      </el-descriptions-item>
                      <el-descriptions-item label="视觉模型">
                        <span class="mono-text">{{ previousAiConfig.visionModel || '—' }}</span>
                        <el-tag
                          :type="previousAiConfig.visionVerified ? 'success' : 'info'"
                          size="small"
                          style="margin-left: 8px"
                        >
                          {{ previousAiConfig.visionVerified ? '已验证' : '未验证' }}
                        </el-tag>
                      </el-descriptions-item>
                      <el-descriptions-item label="Key 状态" :span="2">
                        {{ previousAiConfig.keyConfigured ? `已配置（${previousAiConfig.keyFingerprint || '—'}）` : '未配置' }}
                      </el-descriptions-item>
                    </el-descriptions>
                    <div class="history-actions">
                      <el-button
                        type="warning"
                        :loading="rollingBackAi"
                        :disabled="!previousAiConfig"
                        @click="rollbackAi"
                        size="small"
                      >
                        回滚上一版
                      </el-button>
                      <span v-if="previousAiConfig && !previousAiConfig.keyConfigured" class="form-tip">
                        上一版 key 已清除，回滚前请重新输入有效 key。
                      </span>
                    </div>
                  </div>

                  <el-empty v-else description="暂无可回滚的上一版配置" :image-size="60" />
                </el-collapse-item>
              </el-collapse>
            </el-card>

            <!-- 热切换说明 -->
            <el-alert
              type="info"
              :closable="false"
              show-icon
              class="bottom-alert"
            >
              <template #title>热切换说明</template>
              <template #default>
                新请求立即使用新模型；正在执行的请求继续使用旧模型。API key 不会返回明文，只显示指纹。首期仅支持单后端实例，运行时配置不会广播到其他实例。
              </template>
            </el-alert>
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

  // ===== AI 设置页新样式 =====
  .ai-settings-wrapper {
    display: flex;
    flex-direction: column;
    gap: 16px;
    max-width: 960px;
  }

  .mono-text {
    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
    font-size: 13px;
  }

  .key-fingerprint {
    margin-left: 8px;
    color: #909399;
    font-size: 12px;
  }

  // 状态总览卡
  .status-card {
    border: none;
    background: linear-gradient(135deg, #f8f9ff 0%, #fff5f0 100%);
    border-radius: 12px;

    :deep(.el-card__header) {
      padding: 0;
      border-bottom: none;
    }

    :deep(.el-card__body) {
      padding-top: 8px;
    }
  }

  .status-card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .status-title-group {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .status-dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: #909399;

    &.success { background: #67c23a; box-shadow: 0 0 0 4px rgba(103,194,58,0.15); }
    &.warning { background: #e6a23c; box-shadow: 0 0 0 4px rgba(230,162,60,0.15); }
    &.danger  { background: #f56c6c; box-shadow: 0 0 0 4px rgba(245,108,108,0.15); }
    &.info    { background: #909399; box-shadow: 0 0 0 4px rgba(144,147,153,0.15); }
  }

  .status-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .status-tag {
    margin-left: 4px;
  }

  .capability-row {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    margin-top: 16px;
  }

  .capability-item {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 6px 12px;
    border-radius: 20px;
    background: rgba(255,255,255,0.7);
    border: 1px solid rgba(144,147,153,0.2);
    color: #909399;
    font-size: 13px;
    transition: all 0.2s;

    &.enabled {
      color: #67c23a;
      border-color: rgba(103,194,58,0.3);
      background: rgba(103,194,58,0.08);
    }
  }

  .capability-icon {
    display: flex;
    align-items: center;
  }

  .capability-label {
    font-weight: 500;
  }

  .status-description {
    margin-top: 12px;
    padding: 10px 14px;
    background: rgba(255,255,255,0.8);
    border-radius: 8px;
    font-size: 13px;
    color: #e6a23c;
    line-height: 1.5;
  }

  .status-warning {
    margin-top: 12px;
    padding: 10px 14px;
    background: rgba(245,108,108,0.08);
    border-radius: 8px;
    font-size: 13px;
    color: #f56c6c;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  // 配置卡
  .config-card {
    border-radius: 12px;

    :deep(.el-card__header) {
      padding: 16px 20px;
    }

    :deep(.el-card__body) {
      padding: 20px;
    }
  }

  .card-header {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
  }

  .card-title {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }

  .card-subtitle {
    font-size: 12px;
    color: #909399;
  }

  .editing-hint {
    color: #e6a23c;
  }

  .config-actions {
    margin-top: 16px;
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
  }

  .connectivity-inline-result {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .connectivity-message {
    color: #606266;
    font-size: 13px;
  }

  // 编辑态
  .config-form {
    :deep(.el-form-item) {
      margin-bottom: 18px;
    }

    .form-tip {
      display: block;
      margin-left: 0;
      margin-top: 4px;
    }
  }

  .edit-actions {
    display: flex;
    gap: 10px;
    justify-content: flex-end;
    padding-top: 16px;
    border-top: 1px solid #ebeef5;
  }

  .test-result {
    margin-top: 14px;
    padding: 10px 14px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;

    &.success {
      background: rgba(103,194,58,0.08);
      color: #67c23a;
    }
    &.fail {
      background: rgba(245,108,108,0.08);
      color: #f56c6c;
    }
  }

  .test-result-message {
    color: #606266;
  }

  .test-result-model {
    color: #909399;
  }

  // 折叠分区卡
  .section-card {
    border-radius: 12px;

    :deep(.el-card__header) {
      padding: 0 20px;
      border-bottom: none;
    }

    :deep(.el-card__body) {
      padding-top: 0;
    }

    :deep(.el-collapse-item__header) {
      padding-left: 0;
      padding-right: 0;
      font-weight: 500;
    }

    :deep(.el-collapse-item__wrap) {
      border-bottom: none;
    }

    :deep(.el-collapse-item__content) {
      padding-bottom: 0;
    }
  }

  .collapse-header {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .history-subtitle {
    font-size: 12px;
    color: #e6a23c;
    font-weight: normal;

    &.muted {
      color: #c0c4cc;
    }
  }

  .embedding-actions {
    margin-top: 12px;
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;
  }

  .embedding-state-reason {
    color: #909399;
    font-size: 12px;
  }

  .history-item {
    .history-actions {
      margin-top: 12px;
      display: flex;
      align-items: center;
      gap: 10px;
    }
  }

  .bottom-alert {
    margin-top: 4px;
  }

  .form-tip {
    margin-left: 10px;
    color: #909399;
    font-size: 0.857rem /* 原值: 12px */;
  }
}
</style>
