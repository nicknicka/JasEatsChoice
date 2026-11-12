<template>
  <div class="agent-runtime-container">
    <AppPageHeader class="runtime-header" title="Agent 运行时" subtitle="运行状态与硬边界" :compact="true">
      <template #actions>
        <el-button :icon="Refresh" :loading="loading" @click="loadRuntime">刷新</el-button>
      </template>
    </AppPageHeader>

    <div class="settings-column">
    <!-- 健康状态：状态列表而非统计卡，各项同权、一屏扫完 -->
    <el-card class="health-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">运行健康</span>
          <StatusTag
            v-if="healthItems.length"
            :status="healthSummary.ok ? 'healthy' : 'unhealthy'"
            :text="healthSummary.text"
          />
          <span class="card-note">按主机侧实时探测</span>
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
          <!-- 这些项都改不了：模型/密钥等配置在 DSH，其余是主机侧状态 -->
          <span v-if="!item.ok" class="health-owner">主机侧处理</span>
        </li>
      </ul>
    </el-card>

    <!-- 硬边界：不可关闭，因此不用可交互开关表达；真实生效依赖 DSH 部署与 agent 账号权限 -->
    <el-card class="boundary-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">硬边界</span>
          <span class="card-note">由后端固定；真实生效依赖 DSH 部署与 agent 账号权限</span>
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
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheck, Lock, Refresh, Warning } from '@element-plus/icons-vue'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { getAgentRuntime } from '../../api/admin'

const loading = ref(false)
const runtime = ref({ boundaries: [] })

// 后端给的 name 是英文缩写，页面上换成人话
const HEALTH_NAME_LABELS = { 'DSH profile': 'DSH 运行环境' }

const healthItems = computed(() =>
  [runtime.value.github, runtime.value.dshProfile, runtime.value.isolatedAccount]
    .filter(Boolean)
    .map((item) => ({
      name: HEALTH_NAME_LABELS[item.name] || item.name,
      ok: !!item.ok,
      detail: item.detail
    }))
)

const healthSummary = computed(() => {
  const items = healthItems.value
  const bad = items.filter((item) => !item.ok).length
  return { ok: bad === 0, text: bad === 0 ? '全部正常' : `${bad} / ${items.length} 项异常` }
})

const loadRuntime = async () => {
  loading.value = true
  try {
    const response = await getAgentRuntime()
    runtime.value = response.data || { boundaries: [] }
  } catch (error) {
    ElMessage.error(error.message || '加载运行状态失败')
  } finally {
    loading.value = false
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

/* 状态页是设置类页面：整页一列，宽度取 --form-width-settings（§4.4、§8） */
.settings-column {
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: var(--form-width-settings);
  margin: 0 auto;
}

.health-card,
.boundary-card {
  margin-bottom: var(--card-gap-compact);
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

/* 健康项归属：这些项 JAS 都改不了，统一指到主机侧 */
.health-owner {
  flex-shrink: 0;
  color: var(--ink-400);
  font-size: var(--text-caption);
  white-space: nowrap;
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
