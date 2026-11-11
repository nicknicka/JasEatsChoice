<template>
  <div class="feedback-admin-container">
    <AppPageHeader title="反馈管理" subtitle="分诊、回复与研发队列桥接" :compact="true">
      <template #actions>
        <el-button type="primary" :loading="loading" @click="loadList">刷新</el-button>
      </template>
    </AppPageHeader>

    <el-card class="filter-card" shadow="never">
      <div class="filter-row">
        <el-select v-model="filters.status" placeholder="状态" clearable class="filter-item">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="filters.type" placeholder="类型" clearable class="filter-item">
          <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-input v-model="filters.userId" placeholder="用户ID" clearable class="filter-item" />
        <el-input v-model="filters.merchantId" placeholder="商家ID" clearable class="filter-item" />
        <el-input v-model="filters.orderId" placeholder="订单ID" clearable class="filter-item" />
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="feedbackId" label="反馈ID" min-width="180" show-overflow-tooltip />
        <el-table-column label="类型" width="120">
          <template #default="{ row }">{{ row.typeLabel || row.type }}</template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small" effect="plain">
              {{ row.statusLabel || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openDetail(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
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
    </el-card>

    <el-drawer v-model="detailVisible" title="反馈详情" size="60%">
      <div v-if="detail" class="detail-body">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="反馈ID">{{ detail.feedbackId }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ detail.userId }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ detail.typeLabel || detail.type }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detail.status)" size="small" effect="plain">
              {{ detail.statusLabel || detail.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单/商家">
            {{ detail.orderId || '—' }} / {{ detail.merchantId || '—' }}
          </el-descriptions-item>
          <el-descriptions-item label="结果编码">{{ detail.resolutionCode || '—' }}</el-descriptions-item>
          <el-descriptions-item label="环境" :span="2">{{ detail.environment || '—' }}</el-descriptions-item>
          <el-descriptions-item label="内容" :span="2">
            <span class="detail-content">{{ detail.content }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <h4 class="section-title">消息历史（内部备注仅此处可见）</h4>
        <ul class="message-list">
          <li v-for="msg in detail.messages" :key="msg.messageId" class="message-item">
            <el-tag size="small" :type="msg.visibility === 'INTERNAL' ? 'info' : 'success'" effect="plain">
              {{ msg.visibility === 'INTERNAL' ? '内部备注' : '用户可见' }}
            </el-tag>
            <span class="message-author">{{ msg.authorType }}</span>
            <span class="message-time">{{ formatTime(msg.createdAt) }}</span>
            <p class="message-content">{{ msg.content }}</p>
          </li>
          <li v-if="!detail.messages || detail.messages.length === 0" class="empty">暂无消息</li>
        </ul>

        <h4 class="section-title">附件</h4>
        <ul class="attachment-list">
          <li v-for="att in detail.attachments" :key="att.attachmentId">
            <a :href="att.url" target="_blank" rel="noopener">{{ att.originalName || att.url }}</a>
          </li>
          <li v-if="!detail.attachments || detail.attachments.length === 0" class="empty">暂无附件</li>
        </ul>

        <h4 class="section-title">审计轨迹</h4>
        <ul class="audit-list">
          <li v-for="(audit, idx) in detail.audits || []" :key="idx">
            <strong>{{ audit.action }}</strong> · {{ audit.actorType }} {{ audit.actorId || '' }} ·
            {{ formatTime(audit.createdAt) }}<br />
            <span class="audit-detail">{{ audit.detail }}</span>
          </li>
          <li v-if="!detail.audits || detail.audits.length === 0" class="empty">暂无审计记录</li>
        </ul>

        <div class="detail-actions">
          <el-button @click="openReply">回复 / 备注</el-button>
          <el-button @click="openTriage">分诊</el-button>
          <el-button @click="openResolve">解决</el-button>
          <el-button @click="openAssociation">修正关联</el-button>
          <el-button type="primary" @click="openIssue">建 GitHub Issue</el-button>
        </div>
      </div>
    </el-drawer>

    <el-dialog v-model="replyVisible" title="回复 / 内部备注" width="520px">
      <el-form label-position="top">
        <el-form-item label="可见性">
          <el-radio-group v-model="replyForm.visibility">
            <el-radio-button label="USER">用户可见</el-radio-button>
            <el-radio-button label="INTERNAL">内部备注</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="replyForm.content" type="textarea" :rows="4" maxlength="2000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitReply">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="triageVisible" title="分诊" width="480px">
      <el-form label-position="top">
        <el-form-item label="分诊结果">
          <el-radio-group v-model="triageForm.status">
            <el-radio-button label="needs_info">待补充信息</el-radio-button>
            <el-radio-button label="accepted">接受</el-radio-button>
            <el-radio-button label="rejected">拒绝</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="triageForm.status === 'accepted'" label="平台系统问题">
          <el-switch v-model="triageForm.platformSystem" />
          <span class="form-hint">只有勾选后才能建 GitHub Issue</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="triageVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitTriage">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resolveVisible" title="解决反馈" width="480px">
      <el-form label-position="top">
        <el-form-item label="结果编码">
          <el-select v-model="resolveForm.resolutionCode" placeholder="请选择">
            <el-option v-for="item in resolutionOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resolveVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitResolve">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="associationVisible" title="修正订单/商家关联" width="480px">
      <el-form label-position="top">
        <el-form-item label="订单ID"><el-input v-model="associationForm.orderId" /></el-form-item>
        <el-form-item label="商家ID"><el-input v-model="associationForm.merchantId" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="associationVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitAssociation">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="issueVisible" title="创建 GitHub Issue（脱敏）" width="640px">
      <el-form label-position="top">
        <el-form-item label="标题"><el-input v-model="issueForm.title" /></el-form-item>
        <el-form-item label="摘要"><el-input v-model="issueForm.summary" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="复现步骤"><el-input v-model="issueForm.reproductionSteps" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="期望行为"><el-input v-model="issueForm.expectedBehavior" /></el-form-item>
        <el-form-item label="实际行为"><el-input v-model="issueForm.actualBehavior" /></el-form-item>
        <el-form-item label="验收标准"><el-input v-model="issueForm.acceptanceCriteria" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="禁止范围（可选）"><el-input v-model="issueForm.forbiddenScope" /></el-form-item>
      </el-form>
      <p class="dialog-hint">提交前确认：不含联系方式、订单/支付标识、用户身份或附件正文。</p>
      <template #footer>
        <el-button @click="issueVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitIssue">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
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

const statusTagType = (status) => ({
  pending: 'info',
  triaging: 'warning',
  needs_info: 'warning',
  accepted: 'success',
  rejected: 'danger',
  resolved: 'success'
}[status] || 'info')

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
    ElMessage.error(error.message || '加载详情失败')
  }
}

const refreshDetail = async () => {
  const response = await getFeedbackDetail(currentId.value)
  detail.value = response.data
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
  issueForm.title = ''
  issueForm.summary = ''
  issueForm.reproductionSteps = ''
  issueForm.expectedBehavior = ''
  issueForm.actualBehavior = ''
  issueForm.acceptanceCriteria = ''
  issueForm.forbiddenScope = ''
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
.filter-card,
.table-card {
  margin-bottom: var(--card-gap, 16px);
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3, 12px);
  align-items: center;
}

.filter-item {
  width: 160px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-4, 16px);
}

.detail-body {
  display: flex;
  flex-direction: column;
  gap: var(--space-4, 16px);
}

.section-title {
  margin: var(--space-2, 8px) 0;
  font-size: var(--text-h3, 16px);
}

.detail-content {
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
  gap: var(--space-3, 12px);
}

.message-item {
  padding: var(--space-3, 12px);
  border: 1px solid var(--border-subtle, #e5e7eb);
  border-radius: var(--radius-md, 8px);
}

.message-author {
  margin-left: var(--space-2, 8px);
  font-weight: var(--weight-semibold, 600);
}

.message-time {
  margin-left: var(--space-2, 8px);
  color: var(--text-secondary, #6b7280);
  font-size: var(--text-caption, 12px);
}

.message-content {
  margin: var(--space-2, 8px) 0 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.audit-detail {
  color: var(--text-secondary, #6b7280);
  font-size: var(--text-caption, 12px);
}

.detail-actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2, 8px);
  padding-top: var(--space-3, 12px);
  border-top: 1px solid var(--border-subtle, #e5e7eb);
}

.form-hint,
.dialog-hint {
  margin-left: var(--space-2, 8px);
  color: var(--text-secondary, #6b7280);
  font-size: var(--text-caption, 12px);
}

.dialog-hint {
  margin-left: 0;
}

.empty {
  color: var(--text-secondary, #6b7280);
  font-size: var(--text-caption, 12px);
}
</style>
