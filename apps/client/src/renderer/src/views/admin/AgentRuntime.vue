<template>
  <div class="agent-runtime-container">
    <AppPageHeader title="Agent 运行时" subtitle="模型配置、密钥与健康状态" :compact="true">
      <template #actions>
        <el-button type="primary" :loading="loading" @click="loadRuntime">刷新</el-button>
      </template>
    </AppPageHeader>

    <div class="health-section">
      <AppStatCard
        v-for="item in healthItems"
        :key="item.name"
        compact
        :label="item.name"
        :value="item.ok ? '正常' : '异常'"
        :icon="item.ok ? 'CircleCheck' : 'WarningFilled'"
      />
    </div>

    <el-card class="config-card" shadow="never">
      <template #header>
        <div class="card-header">模型配置（版本 {{ runtime.version || 0 }}）</div>
      </template>
      <el-form label-position="top" class="config-form">
        <el-form-item label="供应商">
          <el-select v-model="form.provider" placeholder="请选择">
            <el-option v-for="item in providerOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="模型">
          <el-input v-model="form.model" placeholder="如 gpt-4o-mini" />
        </el-form-item>
        <el-form-item label="Endpoint">
          <el-input v-model="form.endpoint" placeholder="https://api.example.com/v1/models" />
        </el-form-item>
        <el-form-item label="数据政策确认">
          <el-switch v-model="form.dataPolicyAck" />
          <span class="form-hint">确认不把生产数据送入模型</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="saveConfig">保存</el-button>
          <span class="form-hint">最近修改人：{{ runtime.updatedBy || '—' }}</span>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="key-card" shadow="never">
      <template #header>
        <div class="card-header">模型密钥（只写不可读）</div>
      </template>
      <p class="key-status">
        当前状态：
        <el-tag :type="runtime.modelKeyConfigured ? 'success' : 'info'" size="small" effect="plain">
          {{ runtime.modelKeyConfigured ? '已配置' : '未配置' }}
        </el-tag>
        <span v-if="runtime.modelKeyUpdatedAt" class="form-hint">更新于 {{ formatTime(runtime.modelKeyUpdatedAt) }}</span>
      </p>
      <div class="key-row">
        <el-input
          v-model="newKey"
          type="password"
          show-password
          placeholder="输入新的模型 key（保存后不回显）"
          class="key-input"
        />
        <el-button type="primary" :loading="submitting" :disabled="!newKey" @click="rotateKey">轮换</el-button>
        <el-button :loading="testing" @click="testConnection">测试连接</el-button>
        <el-button type="danger" plain :loading="submitting" @click="revokeKey">吊销</el-button>
      </div>
      <p v-if="testResult" class="key-status">
        连接测试：<el-tag :type="testResult.ok ? 'success' : 'danger'" size="small" effect="plain">
          {{ testResult.ok ? '通过' : '失败' }}
        </el-tag>
        <span class="form-hint">{{ testResult.detail }}</span>
      </p>
    </el-card>

    <el-card class="boundary-card" shadow="never">
      <template #header>
        <div class="card-header">硬边界（不可关闭）</div>
      </template>
      <ul class="boundary-list">
        <li v-for="item in runtime.boundaries || []" :key="item.code">
          <el-switch :model-value="item.enabled" disabled />
          <span class="boundary-code">{{ item.code }}</span>
          <span class="form-hint">{{ item.description }}</span>
        </li>
      </ul>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import AppStatCard from '@/components/common/AppStatCard.vue'
import { formatTime } from '../../utils/formatters'
import {
  getAgentRuntime,
  revokeAgentRuntimeKey,
  rotateAgentRuntimeKey,
  testAgentRuntimeKey,
  updateAgentRuntime
} from '../../api/admin'

const providerOptions = ['openai-compatible', 'openai', 'anthropic', 'azure-openai']

const loading = ref(false)
const submitting = ref(false)
const testing = ref(false)
const runtime = ref({ boundaries: [] })
const newKey = ref('')
const testResult = ref(null)

const form = reactive({ provider: 'openai-compatible', model: '', endpoint: '', dataPolicyAck: false })

const healthItems = computed(() => [
  runtime.value.github,
  runtime.value.dshProfile,
  runtime.value.modelHealth,
  runtime.value.isolatedAccount
].filter(Boolean).map((item) => ({ ...item, name: item.name })))

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
  submitting.value = true
  try {
    const response = await updateAgentRuntime({ ...form })
    apply(response.data)
    ElMessage.success('配置已保存')
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
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
    ElMessage.success('密钥已轮换')
  } catch (error) {
    ElMessage.error(error.message || '轮换失败')
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
    await ElMessageBox.confirm('吊销后模型调用会立即失败，确认继续？', '吊销模型密钥', { type: 'warning' })
  } catch {
    return
  }
  submitting.value = true
  try {
    const response = await revokeAgentRuntimeKey()
    apply(response.data)
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
.health-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--card-gap, 16px);
  margin-bottom: var(--card-gap, 16px);
}

.config-card,
.key-card,
.boundary-card {
  margin-bottom: var(--card-gap, 16px);
}

.card-header {
  font-weight: var(--weight-semibold, 600);
}

.config-form {
  max-width: 560px;
}

.key-status {
  display: flex;
  align-items: center;
  gap: var(--space-2, 8px);
  margin: 0 0 var(--space-3, 12px);
}

.key-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3, 12px);
}

.key-input {
  max-width: 360px;
}

.boundary-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: var(--space-3, 12px);
}

.boundary-code {
  margin-left: var(--space-3, 12px);
  font-family: var(--font-mono, monospace);
}

.form-hint {
  margin-left: var(--space-2, 8px);
  color: var(--text-secondary, #6b7280);
  font-size: var(--text-caption, 12px);
}
</style>
