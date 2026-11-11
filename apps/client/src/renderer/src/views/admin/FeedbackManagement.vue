<template>
  <div class="feedback-admin-container">
    <AppPageHeader title="反馈管理" subtitle="分诊、回复与研发队列桥接" :compact="true">
      <template #actions>
        <el-button :icon="Refresh" :loading="loading" @click="loadList">刷新</el-button>
      </template>
    </AppPageHeader>

    <!-- 筛选：admin 规则 §4.3，底槽用 --canvas-sunken 与表格分层 -->
    <el-card class="filter-card" shadow="never">
      <el-form class="filter-form" label-position="top" @submit.prevent="handleSearch">
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable class="filter-control">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="filters.type" placeholder="全部" clearable class="filter-control">
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="filters.userId" placeholder="精确匹配" clearable class="filter-control" />
        </el-form-item>
        <el-form-item label="商家ID">
          <el-input v-model="filters.merchantId" placeholder="精确匹配" clearable class="filter-control" />
        </el-form-item>
        <el-form-item label="订单ID">
          <el-input v-model="filters.orderId" placeholder="精确匹配" clearable class="filter-control" />
        </el-form-item>
        <el-form-item class="filter-actions">
          <el-button type="primary" native-type="submit" :loading="loading">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <!-- 管线：筛选 → 合计 → 列 → 行操作（§4.3） -->
      <div class="table-toolbar">
        <span class="table-total">共 <b class="table-num">{{ pagination.total }}</b> 条反馈</span>
        <span v-if="activeFilterText" class="table-filter-hint">{{ activeFilterText }}</span>
      </div>

      <AppEmptyState
        v-if="!loading && list.length === 0"
        compact
        :icon="ChatDotSquare"
        description="没有符合条件的反馈，换个筛选条件或清空筛选试试"
        action-text="重置筛选"
        @action="handleReset"
      />

      <template v-else>
        <el-table v-loading="loading" :data="list" stripe class="feedback-table">
          <el-table-column label="反馈ID" width="170" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="cell-num">{{ row.feedbackId }}</span>
            </template>
          </el-table-column>

          <el-table-column label="类型" width="110">
            <template #default="{ row }">{{ row.typeLabel || row.type }}</template>
          </el-table-column>

          <el-table-column label="内容" min-width="280" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="cell-content">{{ row.content }}</span>
            </template>
          </el-table-column>

          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <StatusTag :status="statusKey(row.status)" :text="row.statusLabel || row.status" />
            </template>
          </el-table-column>

          <el-table-column label="提交时间" width="160">
            <template #default="{ row }">
              <span class="cell-num">{{ formatTime(row.createdAt) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <RowActions :primary="{ text: '处理', onClick: () => openDetail(row) }" :items="[]" />
            </template>
          </el-table-column>
        </el-table>

        <div v-if="pagination.total > pagination.size" class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next"
            @current-change="loadList"
            @size-change="handleSearch"
          />
        </div>
      </template>
    </el-card>

    <el-drawer v-model="detailVisible" title="反馈详情" size="60%" class="feedback-drawer">
      <div v-if="detail" class="detail-body">
        <div class="detail-head">
          <span class="detail-id cell-num">{{ detail.feedbackId }}</span>
          <StatusTag :status="statusKey(detail.status)" :text="detail.statusLabel || detail.status" />
          <span class="detail-type">{{ detail.typeLabel || detail.type }}</span>
        </div>

        <section class="detail-section">
          <h4 class="section-title">处理信息</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="用户ID">
              <span class="cell-num">{{ detail.userId }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="提交时间">
              <span class="cell-num">{{ formatTime(detail.createdAt) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="订单ID">
              <span class="cell-num">{{ detail.orderId || '—' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="商家ID">
              <span class="cell-num">{{ detail.merchantId || '—' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="结果编码">{{ resolutionLabel(detail.resolutionCode) }}</el-descriptions-item>
            <el-descriptions-item label="环境">{{ detail.environment || '—' }}</el-descriptions-item>
          </el-descriptions>
        </section>

        <section class="detail-section">
          <h4 class="section-title">反馈内容</h4>
          <p class="detail-content">{{ detail.content }}</p>
        </section>

        <section class="detail-section">
          <h4 class="section-title">消息历史</h4>
          <p class="section-hint">内部备注只在后台可见，不会出现在用户端。</p>
          <ul v-if="detail.messages && detail.messages.length" class="message-list">
            <li v-for="msg in detail.messages" :key="msg.messageId" class="message-item">
              <div class="message-head">
                <span class="message-badge" :class="msg.visibility === 'INTERNAL' ? 'message-badge--internal' : 'message-badge--user'">
                  {{ msg.visibility === 'INTERNAL' ? '内部备注' : '用户可见' }}
                </span>
                <span class="message-author">{{ authorLabel(msg.authorType) }}</span>
                <span class="message-time cell-num">{{ formatTime(msg.createdAt) }}</span>
              </div>
              <p class="message-content">{{ msg.content }}</p>
            </li>
          </ul>
          <p v-else class="section-empty">还没有追加消息。</p>
        </section>

        <section class="detail-section">
          <h4 class="section-title">附件</h4>
          <ul v-if="detail.attachments && detail.attachments.length" class="attachment-list">
            <li v-for="att in detail.attachments" :key="att.attachmentId">
              <a class="attachment-link" :href="att.url" target="_blank" rel="noopener">
                {{ att.originalName || att.url }}
              </a>
            </li>
          </ul>
          <AppEmptyState
            v-else
            compact
            :icon="Paperclip"
            description="这条反馈没有附件（附件由用户提交时上传，后台不代为上传）"
          />
        </section>

        <section class="detail-section">
          <h4 class="section-title">操作轨迹</h4>
          <ul v-if="detail.audits && detail.audits.length" class="audit-list">
            <li v-for="(audit, idx) in detail.audits" :key="idx" class="audit-item">
              <span class="audit-action">{{ auditLabel(audit.action) }}</span>
              <span class="audit-meta">
                {{ audit.actorType === 'ADMIN' ? '管理员' : audit.actorType }} {{ audit.actorId || '' }} ·
                <span class="cell-num">{{ formatTime(audit.createdAt) }}</span>
              </span>
              <p v-if="audit.detail" class="audit-detail">{{ audit.detail }}</p>
            </li>
          </ul>
          <p v-else class="section-empty">还没有操作记录。</p>
        </section>
      </div>

      <template #footer>
        <div class="drawer-actions">
          <el-button v-if="nextAction" type="primary" @click="runAction(nextAction.key)">
            {{ nextAction.label }}
          </el-button>
          <el-button v-for="action in secondaryActions" :key="action.key" @click="runAction(action.key)">
            {{ action.label }}
          </el-button>
        </div>
      </template>
    </el-drawer>

    <el-dialog v-model="replyVisible" title="回复 / 内部备注" width="520px" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="可见性">
          <el-radio-group v-model="replyForm.visibility">
            <el-radio-button value="USER">用户可见</el-radio-button>
            <el-radio-button value="INTERNAL">内部备注</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="replyForm.content" type="textarea" :rows="4" maxlength="2000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" :disabled="!replyForm.content.trim()" @click="submitReply">
          提交
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="triageVisible" title="分诊" width="480px" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="分诊结果">
          <el-radio-group v-model="triageForm.status">
            <el-radio-button value="needs_info">待补充信息</el-radio-button>
            <el-radio-button value="accepted">接受</el-radio-button>
            <el-radio-button value="rejected">拒绝</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="triageForm.status === 'accepted'" label="任务类型">
          <el-checkbox v-model="triageForm.platformSystem">属于平台系统问题</el-checkbox>
          <p class="form-hint">只有平台系统问题才能创建 GitHub Issue。</p>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="triageVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitTriage">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resolveVisible" title="解决反馈" width="480px" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="结果编码">
          <el-select v-model="resolveForm.resolutionCode" placeholder="请选择" class="filter-control">
            <el-option v-for="item in resolutionOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resolveVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitResolve">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="associationVisible" title="修正订单 / 商家关联" width="480px" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="订单ID"><el-input v-model="associationForm.orderId" placeholder="留空表示清除" /></el-form-item>
        <el-form-item label="商家ID"><el-input v-model="associationForm.merchantId" placeholder="留空表示不指定" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="associationVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitAssociation">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="issueVisible" title="创建 GitHub Issue（脱敏）" width="640px" :close-on-click-modal="false">
      <el-alert
        class="issue-alert"
        type="warning"
        show-icon
        :closable="false"
        title="提交前确认正文不含联系方式、订单 / 支付标识、用户身份或附件正文。"
      />
      <el-form label-position="top">
        <el-form-item label="标题"><el-input v-model="issueForm.title" placeholder="如：[bug] 下单后白屏" /></el-form-item>
        <el-form-item label="问题摘要" required>
          <el-input v-model="issueForm.summary" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="复现步骤" required>
          <el-input v-model="issueForm.reproductionSteps" type="textarea" :rows="3" placeholder="一行一步，不要粘贴用户原始内容" />
        </el-form-item>
        <el-form-item label="期望行为" required><el-input v-model="issueForm.expectedBehavior" /></el-form-item>
        <el-form-item label="实际行为" required><el-input v-model="issueForm.actualBehavior" /></el-form-item>
        <el-form-item label="验收标准" required>
          <el-input v-model="issueForm.acceptanceCriteria" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="禁止范围（可选）"><el-input v-model="issueForm.forbiddenScope" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="issueVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" :disabled="!issueFormValid" @click="submitIssue">
          创建 Issue
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotSquare, Paperclip, Refresh } from '@element-plus/icons-vue'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import AppEmptyState from '@/components/common/AppEmptyState.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import RowActions from '@/components/common/RowActions.vue'
import { formatTime } from '../../utils/formatters'
import {
  createFeedbackIssue,
  getFeedbackDetail,
  listFeedbacks,
  replyFeedback,
  resolveFeedback,
  triageFeedback,
  updateFeedbackAssociation
} from '../../api/admin'

const statusOptions = [
  { value: 'pending', label: '待分诊' },
  { value: 'triaging', label: '分诊中' },
  { value: 'needs_info', label: '待补充信息' },
  { value: 'accepted', label: '已接受' },
  { value: 'rejected', label: '已拒绝' },
  { value: 'resolved', label: '已解决' }
]

const typeOptions = [
  { value: 'bug', label: '功能异常' },
  { value: 'feature', label: '功能建议' },
  { value: 'ui', label: '界面问题' },
  { value: 'performance', label: '性能问题' },
  { value: 'order', label: '订单问题' },
  { value: 'payment', label: '支付问题' },
  { value: 'other', label: '其他问题' }
]

const resolutionOptions = [
  { value: 'duplicate', label: '重复反馈' },
  { value: 'wontfix', label: '不予处理' },
  { value: 'not_reproducible', label: '无法复现' },
  { value: 'fixed', label: '已修复' },
  { value: 'answered', label: '已答复' }
]

const authorLabels = { USER: '用户', ADMIN: '管理员', SYSTEM: '系统' }
const auditLabels = { ASSOCIATION: '修正关联', TRIAGE: '分诊', RESOLVE: '解决', REPLY: '回复', BRIDGE: '创建 Issue' }

// 状态 → 语义色；沿用 StatusTag 现有键 + text 覆盖，不新增第二套状态色（§3.1 状态表）
const statusKey = (status) => ({
  pending: 'pending',
  triaging: 'processing',
  needs_info: 'pendingreview',
  accepted: 'approved',
  rejected: 'rejected',
  resolved: 'resolved'
}[status] || 'neutral')

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const detail = ref(null)
const detailVisible = ref(false)
const currentId = ref('')

const replyVisible = ref(false)
const triageVisible = ref(false)
const resolveVisible = ref(false)
const associationVisible = ref(false)
const issueVisible = ref(false)

const filters = reactive({ status: '', type: '', userId: '', merchantId: '', orderId: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const replyForm = reactive({ visibility: 'USER', content: '' })
const triageForm = reactive({ status: 'accepted', platformSystem: false })
const resolveForm = reactive({ resolutionCode: 'fixed' })
const associationForm = reactive({ orderId: '', merchantId: '' })
const issueForm = reactive({
  title: '', summary: '', reproductionSteps: '', expectedBehavior: '',
  actualBehavior: '', acceptanceCriteria: '', forbiddenScope: ''
})

const issueFormValid = computed(() =>
  ['summary', 'reproductionSteps', 'expectedBehavior', 'actualBehavior', 'acceptanceCriteria']
    .every((key) => String(issueForm[key] || '').trim().length > 0)
)

const activeFilterText = computed(() => {
  const labels = []
  if (filters.status) labels.push(`状态：${statusOptions.find((i) => i.value === filters.status)?.label || filters.status}`)
  if (filters.type) labels.push(`类型：${typeOptions.find((i) => i.value === filters.type)?.label || filters.type}`)
  if (filters.userId) labels.push(`用户：${filters.userId}`)
  if (filters.merchantId) labels.push(`商家：${filters.merchantId}`)
  if (filters.orderId) labels.push(`订单：${filters.orderId}`)
  return labels.join(' · ')
})

// 每屏一个主动作：由当前状态决定「下一步」，其余动作降级为描边（§0 原则 3）
const nextAction = computed(() => {
  const status = detail.value?.status
  if (status === 'pending' || status === 'triaging') return { key: 'triage', label: '分诊' }
  if (status === 'needs_info') return { key: 'reply', label: '回复用户' }
  if (status === 'accepted') return { key: 'issue', label: '建 GitHub Issue' }
  return null
})

const secondaryActions = computed(() => {
  const all = [
    { key: 'reply', label: '回复 / 备注' },
    { key: 'triage', label: '分诊' },
    { key: 'resolve', label: '解决' },
    { key: 'association', label: '修正关联' },
    { key: 'issue', label: '建 GitHub Issue' }
  ]
  const primaryKey = nextAction.value?.key
  const status = detail.value?.status
  return all.filter((action) => {
    if (action.key === primaryKey) return false
    if (action.key === 'resolve' && status !== 'accepted') return false
    if (action.key === 'issue' && status !== 'accepted') return false
    if (action.key === 'triage' && ['rejected', 'resolved'].includes(status)) return false
    return true
  })
})

const authorLabel = (type) => authorLabels[type] || type
const auditLabel = (action) => auditLabels[action] || action
const resolutionLabel = (code) =>
  code ? (resolutionOptions.find((item) => item.value === code)?.label || code) : '—'

const loadList = async () => {
  loading.value = true
  try {
    const response = await listFeedbacks({
      page: pagination.page,
      size: pagination.size,
      status: filters.status || undefined,
      type: filters.type || undefined,
      userId: filters.userId || undefined,
      merchantId: filters.merchantId || undefined,
      orderId: filters.orderId || undefined
    })
    const data = response?.data || {}
    list.value = data.list || []
    pagination.total = data.total || 0
  } catch (error) {
    console.error('加载反馈列表失败:', error)
    ElMessage.error(error.message || '加载反馈列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadList()
}

const handleReset = () => {
  filters.status = ''
  filters.type = ''
  filters.userId = ''
  filters.merchantId = ''
  filters.orderId = ''
  handleSearch()
}

const openDetail = async (row) => {
  currentId.value = row.feedbackId
  try {
    const response = await getFeedbackDetail(row.feedbackId)
    detail.value = response.data
    detailVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '加载反馈详情失败')
  }
}

const refreshDetail = async () => {
  const response = await getFeedbackDetail(currentId.value)
  detail.value = response.data
}

const runAction = (key) => {
  if (key === 'reply') openReply()
  else if (key === 'triage') openTriage()
  else if (key === 'resolve') openResolve()
  else if (key === 'association') openAssociation()
  else if (key === 'issue') openIssue()
}

const openReply = () => { replyForm.visibility = 'USER'; replyForm.content = ''; replyVisible.value = true }
const openTriage = () => { triageForm.status = 'accepted'; triageForm.platformSystem = false; triageVisible.value = true }
const openResolve = () => { resolveForm.resolutionCode = 'fixed'; resolveVisible.value = true }
const openAssociation = () => {
  associationForm.orderId = detail.value?.orderId || ''
  associationForm.merchantId = detail.value?.merchantId || ''
  associationVisible.value = true
}
const openIssue = () => {
  Object.assign(issueForm, {
    title: '', summary: '', reproductionSteps: '', expectedBehavior: '',
    actualBehavior: '', acceptanceCriteria: '', forbiddenScope: ''
  })
  issueVisible.value = true
}

const withSubmit = async (action, successMessage, closeDialog) => {
  submitting.value = true
  try {
    const response = await action()
    if (response?.success === false) {
      ElMessage.error(response.message || '操作失败')
      return
    }
    ElMessage.success(successMessage)
    closeDialog?.()
    await refreshDetail()
    await loadList()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const submitReply = () => withSubmit(
  () => replyFeedback(currentId.value, replyForm.content, replyForm.visibility),
  '已提交', () => { replyVisible.value = false }
)

const submitTriage = () => withSubmit(
  () => triageFeedback(currentId.value, triageForm.status, triageForm.platformSystem),
  '分诊完成', () => { triageVisible.value = false }
)

const submitResolve = () => withSubmit(
  () => resolveFeedback(currentId.value, resolveForm.resolutionCode),
  '已解决', () => { resolveVisible.value = false }
)

const submitAssociation = () => withSubmit(
  () => updateFeedbackAssociation(currentId.value, {
    orderId: associationForm.orderId || undefined,
    merchantId: associationForm.merchantId || undefined
  }),
  '关联已更新', () => { associationVisible.value = false }
)

const submitIssue = () => withSubmit(
  () => createFeedbackIssue(currentId.value, { ...issueForm }),
  'Issue 已创建或已存在', () => { issueVisible.value = false }
)

onMounted(loadList)
</script>

<style scoped lang="less">
/* admin 页容器：与 11-公告管理 等列表页同构（§4.3、§7.1） */
.feedback-admin-container {
  box-sizing: border-box;
  width: 100%;
  max-width: 1440px;
  min-height: calc(100vh - 100px);
  margin: 0 auto;
  padding: var(--card-padding-compact) var(--page-gutter) var(--space-8);
  background: var(--canvas);
}

.filter-card {
  margin-bottom: var(--card-gap-compact);
  border-radius: var(--radius-lg);

  .filter-form {
    display: flex;
    flex-wrap: wrap;
    align-items: flex-end;
    gap: var(--space-3) var(--space-4);
    margin: 0;
    padding: var(--space-4);
    border-radius: var(--radius-md);
    background: var(--canvas-sunken);

    :deep(.el-form-item) {
      margin: 0;
    }
  }

  .filter-control {
    width: 160px;
  }

  .filter-actions {
    display: flex;
    gap: var(--space-2);
  }
}

.table-card {
  border-radius: var(--radius-lg);

  .table-toolbar {
    display: flex;
    align-items: center;
    gap: var(--space-3);
    margin-bottom: var(--space-4);
    color: var(--ink-500);
    font-size: var(--text-body-sm);
  }

  .table-num {
    color: var(--ink-900);
    font-family: var(--font-num);
    font-variant-numeric: tabular-nums;
    font-weight: var(--weight-semibold);
  }

  .table-filter-hint {
    overflow: hidden;
    padding-left: var(--space-3);
    border-left: 1px solid var(--line);
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.feedback-table {
  :deep(.el-table__cell) {
    vertical-align: middle;
  }
}

.cell-num {
  font-family: var(--font-num);
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

.cell-content {
  color: var(--ink-700);
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-4);
}

.detail-body {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.detail-head {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--line);
}

.detail-id {
  color: var(--ink-900);
  font-weight: var(--weight-semibold);
}

.detail-type {
  margin-left: auto;
  color: var(--ink-500);
  font-size: var(--text-body-sm);
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.section-title {
  margin: 0;
  color: var(--ink-900);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
}

.section-hint {
  margin: 0;
  color: var(--ink-500);
  font-size: var(--text-caption);
}

.section-empty {
  margin: 0;
  color: var(--ink-500);
  font-size: var(--text-body-sm);
}

.detail-content {
  margin: 0;
  padding: var(--space-4);
  border-radius: var(--radius-md);
  background: var(--canvas-sunken);
  color: var(--ink-700);
  font-size: var(--text-body);
  line-height: 22px;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-list,
.attachment-list,
.audit-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.message-item {
  padding: var(--space-3) var(--space-4);
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  background: var(--surface);
}

.message-head {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.message-badge {
  padding: 2px 8px;
  border-radius: var(--radius-full);
  font-size: var(--text-caption);
  line-height: 18px;
}

/* 可见范围不是流程状态，不占用状态色（§3.1 状态表、§4.6） */
.message-badge--user {
  color: var(--ink-500);
  background: var(--neutral-bg);
}

.message-badge--internal {
  color: var(--ink-700);
  background: var(--surface);
  border: 1px solid var(--line-strong);
}

.message-author {
  color: var(--ink-700);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
}

.message-time {
  margin-left: auto;
  color: var(--ink-500);
  font-size: var(--text-caption);
}

.message-content {
  margin: var(--space-2) 0 0;
  color: var(--ink-700);
  font-size: var(--text-body);
  line-height: 22px;
  white-space: pre-wrap;
  word-break: break-word;
}

.attachment-link {
  color: var(--brand-600);
  font-size: var(--text-body-sm);
  text-decoration: none;

  &:hover,
  &:focus-visible {
    color: var(--brand-500);
    text-decoration: underline;
  }
}

.audit-item {
  padding-left: var(--space-3);
  border-left: 2px solid var(--line);
}

.audit-action {
  color: var(--ink-900);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
}

.audit-meta {
  margin-left: var(--space-2);
  color: var(--ink-500);
  font-size: var(--text-caption);
}

.audit-detail {
  margin: var(--space-1) 0 0;
  color: var(--ink-500);
  font-size: var(--text-caption);
  word-break: break-word;
}

/* 吸底操作栏不得遮挡最后一段内容（§4.3） */
.feedback-drawer :deep(.el-drawer__body) {
  padding-bottom: var(--space-10);
}

.feedback-drawer :deep(.el-drawer__footer) {
  border-top: 1px solid var(--line);
}

.drawer-actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  justify-content: flex-end;
}

.form-hint {
  margin: var(--space-1) 0 0;
  color: var(--ink-500);
  font-size: var(--text-caption);
}

.issue-alert {
  margin-bottom: var(--space-4);
}
</style>
