<template>
  <div class="agent-runtime-container">
    <AppPageHeader class="runtime-header" title="Agent 运行时" subtitle="模型配置、密钥与健康状态" :compact="true">
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
            <component :is="item.ok ? CircleCheck : Warning" />
          </el-icon>
          <div class="health-text">
            <span class="health-name">{{ item.name }}</span>
            <span class="health-detail">{{ item.detail }}</span>
          </div>
          <!-- 每条异常都给出归属：本页能改的指到表单，改不了的说明在主机侧 -->
          <span v-if="!item.ok" class="health-owner" :class="{ 'health-owner--here': item.name === '模型' }">
            {{ item.name === '模型' ? '本页可配' : '主机侧处理' }}
          </span>
        </li>
      </ul>

      <!-- 诊断到修复之间的入口：说清缺什么，并跳到该改的那张卡 -->
      <div v-if="nextStep" class="health-next">
        <span class="health-next__text">{{ nextStep.text }}</span>
        <el-button link type="primary" @click="focusModelConfig">{{ nextStep.action }}</el-button>
      </div>
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
          <el-input ref="modelInputRef" v-model="form.model" class="form-control" placeholder="如 gpt-4o-mini" />
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

      <p v-if="sourceVersion" class="save-hint save-hint--rollback">
        已回填 v{{ sourceVersion }}；保存后这条历史会记为「回滚自 v{{ sourceVersion }}」。
      </p>
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
    <el-card ref="keyCardRef" class="key-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">模型密钥</span>
          <span class="key-state">
            <StatusTag
              :status="runtime.modelKeyConfigured ? 'healthy' : 'pending'"
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
        <el-button :disabled="!newKey" :loading="submitting" @click="rotateKey">
          {{ runtime.modelKeyConfigured ? '轮换' : '保存密钥' }}
        </el-button>
        <el-dropdown trigger="click" @command="handleKeyCommand">
          <el-button :loading="submitting">
            更多<el-icon class="key-more-icon"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="revoke" class="key-danger-item">吊销密钥</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
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
          <span class="boundary-code">{{ item.code }}</span>
          <span class="boundary-desc">{{ item.description }}</span>
          <StatusTag status="active" text="已生效" />
        </li>
      </ul>
    </el-card>
    </div>

    <el-drawer v-model="historyVisible" title="配置版本历史" size="760px">
      <el-table
        v-loading="historyLoading"
        :data="historyRows"
        :row-class-name="historyRowClass"
        stripe
      >
        <el-table-column label="版本" width="70">
          <template #default="{ row }"><span class="cell-num">v{{ row.version }}</span></template>
        </el-table-column>
        <el-table-column label="相对上一版的变化" min-width="220">
          <template #default="{ row }">
            <span class="history-change">{{ row.change }}</span>
          </template>
        </el-table-column>
        <el-table-column label="修改时间" width="168">
          <template #default="{ row }"><span class="cell-num">{{ formatTime(row.createdAt) }}</span></template>
        </el-table-column>
        <el-table-column label="修改人" width="80" prop="updatedBy" />
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <span v-if="row.version === (runtime.version || 0)" class="history-current">当前版本</span>
            <el-button v-else link type="primary" @click="loadVersion(row)">回填表单</el-button>
          </template>
        </el-table-column>
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
import { ArrowDown, CircleCheck, Clock, Lock, Refresh, Warning } from '@element-plus/icons-vue'
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

// 后端给的 name 是英文缩写，页面上换成人话
const HEALTH_NAME_LABELS = { 'DSH profile': 'DSH 运行环境' }

const healthItems = computed(() =>
  [runtime.value.github, runtime.value.dshProfile, runtime.value.modelHealth, runtime.value.isolatedAccount]
    .filter(Boolean)
    .map((item) => ({
      name: HEALTH_NAME_LABELS[item.name] || item.name,
      ok: !!item.ok,
      detail: modelHealthDetail(item)
    }))
)

// 本页表单负责的三个必填项：模型、Endpoint、数据政策确认
const configReady = computed(() =>
  !!form.model.trim() && !!form.endpoint.trim() && form.dataPolicyAck
)

// 后端给的是笼统的「缺少 endpoint 或模型 key」，这里按当前配置说清到底缺哪一个
const modelHealthDetail = (item) => {
  if (item !== runtime.value.modelHealth || item.ok) return item.detail
  return runtime.value.endpoint ? '模型密钥未配置' : '缺少 Endpoint 或模型密钥'
}

const nextStep = computed(() => {
  if (healthSummary.value.ok) return null
  if (runtime.value.endpoint && !runtime.value.modelKeyConfigured) {
    return { text: '下一步：模型密钥还没配置，健康检查里的「模型」不会转正常。', action: '去配置密钥' }
  }
  return { text: '下一步：先补齐模型与 Endpoint，健康检查里的「模型」才会转正常。', action: '去配置' }
})

const healthSummary = computed(() => {
  const items = healthItems.value
  const bad = items.filter((item) => !item.ok).length
  return { ok: bad === 0, text: bad === 0 ? '全部正常' : `${bad} / ${items.length} 项异常` }
})

const modelInputRef = ref(null)
// 回填来源版本：保存时带给后端，让这条历史记成 rollback（决策 6B）
const sourceVersion = ref(null)
const keyCardRef = ref(null)

// 健康卡「去配置」：滚到模型配置并聚焦模型输入，把诊断和修复接起来
const focusModelConfig = () => {
  if (runtime.value.endpoint && !runtime.value.modelKeyConfigured) {
    keyCardRef.value?.$el?.scrollIntoView({ behavior: 'smooth', block: 'center' })
    return
  }
  modelInputRef.value?.$el?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  modelInputRef.value?.focus()
}

// 把某个历史版本载回表单；不直接落库，由管理员确认后再点保存
// 供应商在库里存的是编码，展示换成中文
// 版本历史：desc 序，下一条就是上一版；用它算出“这一版改了什么”
const historyRows = computed(() => {
  const rows = history.value || []
  return rows.map((row, index) => {
    const previous = rows[index + 1]
    if (!previous) {
      return { ...row, change: `初始版本：${providerLabel(row.provider)} · ${row.model || '—'}` }
    }
    const changes = []
    if (previous.model !== row.model) changes.push(`模型 ${previous.model || '—'} → ${row.model || '（清空）'}`)
    if (previous.provider !== row.provider) changes.push(`供应商 → ${providerLabel(row.provider)}`)
    if (previous.endpoint !== row.endpoint) changes.push('Endpoint 变更')
    if (!!previous.dataPolicyAck !== !!row.dataPolicyAck) changes.push('数据政策确认')
    const prefix = row.changeKind === 'rollback' ? `回滚自 v${row.sourceVersion ?? '—'}：` : ''
    return { ...row, change: prefix + (changes.length ? changes.join('、') : '无字段变化') }
  })
})

const historyRowClass = ({ row }) =>
  row.version === (runtime.value.version || 0) ? 'history-row--current' : ''

const providerLabel = (value) =>
  providerOptions.find((item) => item.value === value)?.label || value || '—'

const loadVersion = async (row) => {
  try {
    await ElMessageBox.confirm(
      `会把 v${row.version} 的供应商、模型与 Endpoint 覆盖到表单，不会直接生效；需要你再点「保存配置」。`,
      `回填 v${row.version} 到表单`,
      { confirmButtonText: '回填到表单', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  form.provider = row.provider || 'openai-compatible'
  form.model = row.model || ''
  form.endpoint = row.endpoint || ''
  form.dataPolicyAck = !!row.dataPolicyAck
  sourceVersion.value = row.version
  historyVisible.value = false
  ElMessage.success(`已把 v${row.version} 回填到表单，确认后点「保存配置」生效`)
}

const handleKeyCommand = (command) => {
  if (command === 'revoke') revokeKey()
}

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
    const response = await updateAgentRuntime({ ...form, sourceVersion: sourceVersion.value })
    apply(response.data)
    sourceVersion.value = null
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

/* 页头与主体共用同一条 720px 中轴（§4.4、§4.7） */
.runtime-header {
  box-sizing: border-box;
  width: 100%;
  max-width: var(--form-width-settings);
  margin-right: auto;
  margin-left: auto;
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

/* 健康项归属：本页可配 / 主机侧处理，避免每条异常都指向同一个入口 */
.health-owner {
  flex-shrink: 0;
  color: var(--ink-400);
  font-size: var(--text-caption);
  white-space: nowrap;
}

.health-owner--here {
  color: var(--brand-600);
}

/* 健康卡底部：诊断到修复的下一步 */
.health-next {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin: var(--space-4) 0 0;
  padding-top: var(--space-3);
  border-top: 1px solid var(--line);
}

.health-next__text {
  color: var(--ink-500);
  font-size: var(--text-caption);
}

:deep(.el-table .history-row--current td.el-table__cell) {
  background: var(--brand-50);
}

.save-hint--rollback {
  color: var(--brand-600);
}

.history-change {
  display: block;
  color: var(--ink-700);
  line-height: 18px;
  white-space: normal;
}

.history-current {
  color: var(--ink-500);
  font-size: var(--text-caption);
}

.key-more-icon {
  margin-left: var(--space-1);
}

.key-danger-item {
  color: var(--danger);
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
  white-space: nowrap; /* §4.3：时间列不允许折行 */
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
  align-items: baseline;
  gap: var(--space-3);
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-md);
  background: var(--canvas-sunken);
}

.boundary-icon {
  color: var(--ink-500);
  font-size: 16px;
}

.boundary-desc {
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
