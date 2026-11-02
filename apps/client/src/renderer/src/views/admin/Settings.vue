<script setup>
import { ref, computed, onMounted } from 'vue'
import { Setting, Connection, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
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
    supervisorModel: config.supervisorModel || '',
    visionModel: config.visionModel || '',
    baseUrl: config.baseUrl || '',
    timeoutMs: config.timeoutMs || 30000,
    temperature: config.temperature || 0.7,
    apiKey: '',
    embedding: {
      provider: config.embedding?.provider || '',
      model: config.embedding?.model || '',
      dimensions: config.embedding?.dimensions || 1024,
      baseUrl: config.embedding?.baseUrl || '',
      apiKey: ''
    }
  }
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

async function saveAndActivateAi() {
  activatingAi.value = true
  try {
    const response = await activateAiConfig(aiCandidate.value)
    if (response.success && response.data) {
      applyAiSnapshot(response.data)
      await loadAiPreviousSummary()
      ElMessage.success('AI 配置已保存并激活')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '保存或激活失败，当前配置未改变')
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
        <!-- AI 模型（只读） -->
        <el-tab-pane label="AI 模型" name="ai">
          <div v-loading="aiLoading" style="max-width: 900px">
            <el-alert
              title="AI 模型热切换"
              description="这里展示后端当前生效的 AI 运行时配置；候选配置测试通过后可保存并激活。API key 不会返回明文。首期仅支持单后端实例，运行时配置和 key 不会广播到其他实例。"
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

            <h4>Embedding 配置</h4>
            <div class="embedding-summary">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="Embedding Provider">{{ aiConfig.embedding.provider || '-' }}</el-descriptions-item>
                <el-descriptions-item label="Embedding 模型">{{ aiConfig.embedding.model || '-' }}</el-descriptions-item>
                <el-descriptions-item label="向量维度">{{ aiConfig.embedding.dimensions }}</el-descriptions-item>
                <el-descriptions-item label="服务地址">{{ aiConfig.embedding.baseUrl || '供应商默认' }}</el-descriptions-item>
              </el-descriptions>
              <div class="embedding-key-status">
                <el-tag :type="aiConfig.embedding.keyStatus.configured ? 'success' : 'danger'">
                  {{ aiConfig.embedding.keyStatus.configured ? '已配置' : '未配置' }}
                </el-tag>
                <span v-if="aiConfig.embedding.keyStatus.configured" class="key-status">
                  指纹：{{ aiConfig.embedding.keyStatus.fingerprint }}
                </span>
                <span class="key-status">更新时间：{{ aiConfig.embedding.keyStatus.updatedAt || '-' }}</span>
              </div>
              <div class="embedding-state">
                <span class="key-status">索引状态：</span>
                <el-tag :type="embeddingStateMeta.type">{{ embeddingStateMeta.label }}</el-tag>
                <span v-if="aiConfig.embedding.statusReason" class="embedding-state-reason">
                  {{ aiConfig.embedding.statusReason }}
                </span>
                <el-button :icon="Refresh" :loading="refreshingAiState" @click="refreshAiState">
                  刷新状态
                </el-button>
                <el-button
                  type="primary"
                  :loading="rebuildingEmbedding"
                  @click="retryEmbeddingRebuild"
                >
                  重试重建
                </el-button>
              </div>
            </div>

            <h4>上一版有效配置</h4>
            <el-descriptions v-if="previousAiConfig" :column="2" border>
              <el-descriptions-item label="版本">{{ previousAiConfig.version || '-' }}</el-descriptions-item>
              <el-descriptions-item label="生效时间">{{ previousAiConfig.updatedAt || '-' }}</el-descriptions-item>
              <el-descriptions-item label="聊天模型">{{ previousAiConfig.model || '-' }}</el-descriptions-item>
              <el-descriptions-item label="Supervisor 模型">{{ previousAiConfig.supervisorModel || '-' }}</el-descriptions-item>
              <el-descriptions-item label="视觉模型">{{ previousAiConfig.visionModel || '-' }}</el-descriptions-item>
              <el-descriptions-item label="Key 状态">
                {{ previousAiConfig.keyConfigured ? `已配置（${previousAiConfig.keyFingerprint || '-'}）` : '未配置' }}
              </el-descriptions-item>
            </el-descriptions>
            <el-empty v-else description="暂无可回滚的上一版配置" :image-size="60" />
            <div class="connectivity-section">
              <el-button
                type="warning"
                :loading="rollingBackAi"
                :disabled="!previousAiConfig"
                @click="rollbackAi"
              >
                回滚上一版
              </el-button>
              <span v-if="previousAiConfig && !previousAiConfig.keyConfigured" class="connectivity-message">
                上一版 key 已清除，回滚前请重新输入有效 key。
              </span>
            </div>

            <h4>API Key 状态</h4>
            <el-tag :type="aiConfig.keyStatus.configured ? 'success' : 'danger'">
              {{ aiConfig.keyStatus.configured ? '已配置' : '未配置' }}
            </el-tag>
            <span v-if="aiConfig.keyStatus.configured" class="key-status">
              指纹：{{ aiConfig.keyStatus.fingerprint }}
            </span>
            <span class="key-status">更新时间：{{ aiConfig.keyStatus.updatedAt || '-' }}</span>

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
              <el-button :icon="Connection" :loading="testingAi" @click="runAiConnectivityTest">
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

            <h4>候选配置</h4>
            <el-form :model="aiCandidate" label-width="180px" style="max-width: 700px">
              <el-form-item label="API Key">
                <el-input
                  v-model="aiCandidate.apiKey"
                  type="password"
                  show-password
                  autocomplete="new-password"
                  placeholder="输入新 active key；留空沿用当前 key"
                />
              </el-form-item>

              <el-form-item label="Provider">
                <el-select v-model="aiCandidate.provider" placeholder="选择后端支持的 Provider" @change="onProviderChange">
                  <el-option
                    v-for="provider in aiConfig.providers"
                    :key="provider.code"
                    :label="provider.name"
                    :value="provider.code"
                  />
                </el-select>
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
                  :placeholder="aiAdvanced ? '从已知模型选择或输入自定义模型' : '从已知模型选择'"
                >
                  <el-option v-for="option in aiModelOptions" :key="option.value" :label="option.label" :value="option.value" />
                </el-select>
              </el-form-item>

              <el-form-item label="Supervisor 模型">
                <el-select
                  v-model="aiCandidate.supervisorModel"
                  filterable
                  :allow-create="aiAdvanced"
                  default-first-option
                  :placeholder="aiAdvanced ? '从已知模型选择或输入自定义模型' : '从已知模型选择'"
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
                  :placeholder="aiAdvanced ? '从已知模型选择或输入自定义模型' : '从已知模型选择'"
                >
                  <el-option v-for="option in aiVisionModelOptions" :key="option.value" :label="option.label" :value="option.value" />
                </el-select>
              </el-form-item>

              <div class="embedding-candidate">
                <el-form-item label="Embedding Provider">
                  <el-select v-model="aiCandidate.embedding.provider" placeholder="选择后端支持的 Provider">
                    <el-option
                      v-for="provider in aiConfig.embedding.providers"
                      :key="provider"
                      :label="provider"
                      :value="provider"
                    />
                  </el-select>
                </el-form-item>

                <el-form-item label="Embedding 模型">
                  <el-input v-model="aiCandidate.embedding.model" placeholder="例如 bge-m3" />
                </el-form-item>

                <el-form-item label="向量维度">
                  <el-input-number v-model="aiCandidate.embedding.dimensions" :min="1" />
                </el-form-item>

                <el-form-item label="服务地址">
                  <el-input v-model="aiCandidate.embedding.baseUrl" placeholder="留空使用供应商默认地址" />
                </el-form-item>

                <el-form-item label="Embedding API Key">
                  <el-input
                    v-model="aiCandidate.embedding.apiKey"
                    type="password"
                    show-password
                    autocomplete="new-password"
                    placeholder="输入新 Embedding key；留空沿用当前"
                  />
                </el-form-item>
              </div>

              <el-form-item>
                <el-switch v-model="aiAdvanced" active-text="高级设置" />
              </el-form-item>

              <template v-if="aiAdvanced">
                <el-form-item label="服务地址">
                  <el-input v-model="aiCandidate.baseUrl" placeholder="留空使用供应商默认地址" />
                </el-form-item>
                <el-form-item label="超时时间">
                  <el-input-number v-model="aiCandidate.timeoutMs" :min="1000" :step="1000" />
                  <span class="form-tip">单位：毫秒</span>
                </el-form-item>
                <el-form-item label="温度">
                  <el-input-number v-model="aiCandidate.temperature" :min="0" :max="2" :step="0.1" />
                </el-form-item>
              </template>

              <el-form-item>
                <el-button :icon="Connection" :loading="testingAi" @click="runAiCandidateTest">
                  测试候选配置
                </el-button>
                <el-button type="primary" :loading="activatingAi" @click="saveAndActivateAi">
                  保存并激活
                </el-button>
              </el-form-item>
            </el-form>

            <el-alert
              title="热切换说明"
              description="新请求立即使用新模型；正在执行的请求继续使用旧模型。候选 key 测试成功后写入运行时密钥文件，并优先于 AI_API_KEY。页面只显示状态和不可逆指纹，不显示明文 key。"
              type="warning"
              show-icon
              :closable="false"
              class="ai-alert"
            />
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

  .embedding-summary {
    margin-bottom: 8px;
  }

  .embedding-key-status {
    margin-top: 10px;
  }

  .embedding-state {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 10px;
  }

  .embedding-state-reason {
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
