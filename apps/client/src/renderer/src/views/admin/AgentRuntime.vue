<template>
  <div class="agent-runtime-container">
    <AppPageHeader title="Agent 运行时" subtitle="模型配置、密钥与健康状态" :compact="true">
      <template #actions>
        <el-button :icon="Refresh" :loading="loading" @click="loadRuntime">刷新</el-button>
      </template>
    </AppPageHeader>

    <div class="settings-column">
    <!-- 健康状态：状态列表而非统计卡，四项同权、一屏扫完 -->
    <el-card class="health-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">运行健康</span>
          <StatusTag
            v-if="healthItems.length"
            :status="healthSummary.ok ? 'healthy' : 'unhealthy'"
            :text="healthSummary.text"
          />
          <span class="card-note">按当前配置实时判定</span>
        </div>
      </template>

      <ul class="health-list">
        <li v-for="item in healthItems" :key="item.name" class="health-item">
          <el-icon class="health-icon" :class="item.ok ? 'health-icon--ok' : 'health-icon--bad'">
            <component :is="item.ok ? CircleCheckFilled : WarningFilled" />
          </el-icon>
          <div class="health-text">
            <span class="health-name">{{ item.name }}</span>
            <span class="health-detail">{{ item.detail }}</span>
          </div>
          <StatusTag :status="item.ok ? 'healthy' : 'unhealthy'" :text="item.ok ? '正常' : '异常'" />
        </li>
      </ul>
    </el-card>

    <!-- 模型配置：本页唯一实心主按钮就在这里（保存配置） -->
    <AppFormContainer
      class="config-form-container"
      variant="settings"
      title="模型配置"
      :subtitle="runtime.updatedBy ? `最近修改：${runtime.updatedBy}` : ''"
    >
      <div class="version-row">
        <span class="version-text">
          当前版本 <b class="cell-num">v{{ runtime.version || 0 }}</b>
        </span>
        <el-button link type="primary" @click="openHistory">查看版本历史</el-button>
      </div>

      <el-form label-position="top" @submit.prevent="saveConfig">
        <el-form-item label="供应商">
          <el-select v-model="form.provider" class="form-control" placeholder="请选择">
            <el-option v-for="item in providerOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="模型">
          <el-input v-model="form.model" class="form-control" placeholder="如 gpt-4o-mini" />
        </el-form-item>
        <el-form-item label="Endpoint">
          <el-input v-model="form.endpoint" placeholder="https://api.example.com/v1/models" />
        </el-form-item>
        <el-form-item>
          <div class="policy-row">
            <el-checkbox v-model="form.dataPolicyAck">
              已确认不把生产数据送入该模型
            </el-checkbox>
            <p class="form-hint">供应商只能从白名单中选择。勾选后表示你确认该模型不会接收生产数据。</p>
          </div>
        </el-form-item>
      </el-form>

      <p v-if="!configReady" class="save-hint">填写模型与 Endpoint、并勾选数据政策确认后才能保存。</p>
      <p v-if="testResult" class="key-result">
        <StatusTag :status="testResult.ok ? 'healthy' : 'unhealthy'" :text="testResult.ok ? '连接正常' : '连接失败'" />
        <span class="card-note">{{ testResult.detail }}</span>
      </p>

      <template #actions>
        <el-button :loading="testing" @click="testConnection">测试连接</el-button>
        <el-button type="primary" :loading="submitting" :disabled="!configReady" @click="saveConfig">
          保存配置
        </el-button>
      </template>
    </AppFormContainer>

    <!-- 密钥：只写不可读 -->
    <el-card class="key-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">模型密钥</span>
          <span class="key-state">
            <StatusTag
              :status="runtime.modelKeyConfigured ? 'healthy' : 'none'"
              :text="runtime.modelKeyConfigured ? '已配置' : '未配置'"
            />
            <span v-if="runtime.modelKeyUpdatedAt" class="card-note cell-num">
              更新于 {{ formatTime(runtime.modelKeyUpdatedAt) }}
            </span>
          </span>
        </div>
      </template>

      <div class="key-row">
        <el-input
          v-model="newKey"
          type="password"
          show-password
          autocomplete="new-password"
          placeholder="输入新的模型 key"
          class="key-input"
        />
        <el-button :disabled="!newKey" :loading="submitting" @click="rotateKey">轮换</el-button>
        <el-button type="danger" plain :loading="submitting" @click="revokeKey">吊销</el-button>
      </div>

      <p class="form-hint">密钥只写不可读：保存后接口与日志都不会回显明文，轮换与吊销互不影响其它配置。</p>
    </el-card>

    <!-- 硬边界：不可关闭，因此不用可交互开关表达 -->
    <el-card class="boundary-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">硬边界</span>
          <span class="card-note">由后端固定，配置页无法关闭</span>
        </div>
      </template>

      <ul class="boundary-list">
        <li v-for="item in runtime.boundaries || []" :key="item.code" class="boundary-item">
          <el-icon class="boundary-icon"><Lock /></el-icon>
          <div class="boundary-text">
            <span class="boundary-code">{{ item.code }}</span>
            <span class="boundary-desc">{{ item.description }}</span>
          </div>
          <StatusTag status="active" text="已生效" />
        </li>
      </ul>
    </el-card>
    </div>

    <el-drawer v-model="historyVisible" title="配置版本历史" size="660px">
      <el-table v-loading="historyLoading" :data="history" stripe>
        <el-table-column label="版本" width="70">
          <template #default="{ row }"><span class="cell-num">v{{ row.version }}</span></template>
        </el-table-column>
        <el-table-column label="供应商" width="120" prop="provider" />
        <el-table-column label="模型" min-width="130" prop="model" show-overflow-tooltip />
        <el-table-column label="修改时间" width="140">
          <template #default="{ row }"><span class="cell-num">{{ formatTime(row.createdAt) }}</span></template>
        </el-table-column>
        <el-table-column label="修改人" width="100" prop="updatedBy" />
        <template #empty>
          <AppEmptyState
            compact
            :icon="Clock"
            description="还没有配置版本记录，保存一次配置就会写入第一条"
            action-text="去填写模型配置"
            @action="historyVisible = false"
          />
        </template>
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheckFilled, Clock, Lock, Refresh, WarningFilled } from '@element-plus/icons-vue'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import AppEmptyState from '@/components/common/AppEmptyState.vue'
import AppFormContainer from '@/components/common/AppFormContainer.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { formatTime } from '../../utils/formatters'
import {
  getAgentRuntime,
  getAgentRuntimeHistory,
  revokeAgentRuntimeKey,
  rotateAgentRuntimeKey,
  testAgentRuntimeKey,
  updateAgentRuntime
} from '../../api/admin'

const providerOptions = [
  { value: 'openai-compatible', label: 'OpenAI 兼容' },
  { value: 'openai', label: 'OpenAI' },
  { value: 'anthropic', label: 'Anthropic' },
  { value: 'azure-openai', label: 'Azure OpenAI' }
]

const loading = ref(false)
const submitting = ref(false)
const testing = ref(false)
const runtime = ref({ boundaries: [] })
const newKey = ref('')
const testResult = ref(null)
const historyVisible = ref(false)
const historyLoading = ref(false)
const history = ref([])

const form = reactive({ provider: 'openai-compatible', model: '', endpoint: '', dataPolicyAck: false })

const healthItems = computed(() =>
  [runtime.value.github, runtime.value.dshProfile, runtime.value.modelHealth, runtime.value.isolatedAccount]
    .filter(Boolean)
    .map((item) => ({ name: item.name, ok: !!item.ok, detail: item.detail }))
)

// 本页表单负责的三个必填项：模型、Endpoint、数据政策确认
const configReady = computed(() =>
  !!form.model.trim() && !!form.endpoint.trim() && form.dataPolicyAck
)

const healthSummary = computed(() => {
  const items = healthItems.value
  const bad = items.filter((item) => !item.ok).length
  return { ok: bad === 0, text: bad === 0 ? '全部正常' : `${bad} / ${items.length} 项异常` }
})

const openHistory = async () => {
  historyVisible.value = true
  historyLoading.value = true
  try {
    const response = await getAgentRuntimeHistory()
    history.value = response.data || []
  } catch (error) {
    ElMessage.error(error.message || '加载版本历史失败')
    history.value = []
  } finally {
    historyLoading.value = false
  }
}

const apply = (data) => {
  runtime.value = data || { boundaries: [] }
  form.provider = data?.provider || 'openai-compatible'
  form.model = data?.model || ''
  form.endpoint = data?.endpoint || ''
  form.dataPolicyAck = !!data?.dataPolicyAck
}

const loadRuntime = async () => {
  loading.value = true
  try {
    const response = await getAgentRuntime()
    apply(response.data)
  } catch (error) {
    ElMessage.error(error.message || '加载运行时配置失败')
  } finally {
    loading.value = false
  }
}

const saveConfig = async () => {
  if (submitting.value) return
  if (!configReady.value) {
    ElMessage.warning('请先补齐模型、Endpoint 与数据政策确认')
    return
  }
  submitting.value = true
  try {
    const response = await updateAgentRuntime({ ...form })
    apply(response.data)
    ElMessage.success('配置已保存')
  } catch (error) {
    ElMessage.error(error.message || '保存配置失败')
  } finally {
    submitting.value = false
  }
}

const rotateKey = async () => {
  submitting.value = true
  try {
    const response = await rotateAgentRuntimeKey(newKey.value)
    apply(response.data)
    newKey.value = ''
    testResult.value = null
    ElMessage.success('密钥已轮换')
  } catch (error) {
    ElMessage.error(error.message || '密钥轮换失败')
  } finally {
    submitting.value = false
  }
}

const testConnection = async () => {
  testing.value = true
  try {
    const response = await testAgentRuntimeKey()
    testResult.value = response.data
  } catch (error) {
    testResult.value = { ok: false, detail: error.message || '测试失败' }
  } finally {
    testing.value = false
  }
}

const revokeKey = async () => {
  try {
    await ElMessageBox.confirm('吊销后模型调用会立即失败，确认继续？', '吊销模型密钥', {
      confirmButtonText: '确认吊销',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  submitting.value = true
  try {
    const response = await revokeAgentRuntimeKey()
    apply(response.data)
    testResult.value = null
    ElMessage.success('密钥已吊销')
  } catch (error) {
    ElMessage.error(error.message || '吊销失败')
  } finally {
    submitting.value = false
  }
}

onMounted(loadRuntime)
</script>

<style scoped lang="less">
.agent-runtime-container {
  box-sizing: border-box;
  width: 100%;
  max-width: 1440px;
  min-height: calc(100vh - 100px);
  margin: 0 auto;
  padding: var(--card-padding-compact) var(--page-gutter) var(--space-8);
  background: var(--canvas);
}

/* 配置页是设置类页面：整页一列，宽度取 --form-width-settings（§4.4、§8） */
.settings-column {
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: var(--form-width-settings);
  margin: 0 auto;
}

.health-card,
.key-card,
.boundary-card,
.config-form-container {
  margin-bottom: var(--card-gap-compact);
}

.health-card,
.key-card,
.boundary-card {
  border-radius: var(--radius-lg);
}

.card-header {
  display: flex;
  align-items: baseline;
  gap: var(--space-3);
}

.card-title {
  color: var(--ink-900);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
}

.card-note {
  color: var(--ink-500);
  font-size: var(--text-caption);
}

.health-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3) var(--space-5);
  margin: 0;
  padding: 0;
  list-style: none;
}

.health-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  background: var(--canvas-sunken);
}

.health-icon {
  font-size: 18px;
}

.health-icon--ok {
  color: var(--success);
}

.health-icon--bad {
  color: var(--danger);
}

.health-text {
  display: flex;
  flex-direction: column;
  min-width: 0;
  margin-right: auto;
}

.health-name {
  color: var(--ink-900);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
}

.health-detail {
  overflow: hidden;
  color: var(--ink-500);
  font-size: var(--text-caption);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.form-control {
  width: 100%;
}

.version-row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
  color: var(--ink-500);
  font-size: var(--text-body-sm);
}

.version-text {
  color: var(--ink-500);
}

.save-hint {
  margin: 0 0 var(--space-3);
  color: var(--ink-500);
  font-size: var(--text-caption);
}

.policy-row {
  display: flex;
  flex-direction: column;
}

.form-hint {
  margin: var(--space-1) 0 0;
  color: var(--ink-500);
  font-size: var(--text-caption);
  line-height: 18px;
}

.key-state {
  display: inline-flex;
  align-items: center;
  gap: var(--space-3);
  margin-left: auto;
}

.key-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
}

.key-input {
  max-width: 360px;
}

.key-result {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin: var(--space-3) 0 0;
}

.cell-num {
  font-family: var(--font-num);
  font-variant-numeric: tabular-nums;
}

.boundary-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  margin: 0;
  padding: 0;
  list-style: none;
}

.boundary-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  background: var(--canvas-sunken);
}

.boundary-icon {
  color: var(--ink-500);
  font-size: 16px;
}

.boundary-text {
  display: flex;
  flex-direction: column;
  margin-right: auto;
}

.boundary-code {
  color: var(--ink-900);
  font-family: var(--font-num);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
}

.boundary-desc {
  color: var(--ink-500);
  font-size: var(--text-caption);
}
</style>
