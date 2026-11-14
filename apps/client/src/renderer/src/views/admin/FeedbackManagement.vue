<template>
  <div class="feedback-admin-container">
    <AppPageHeader title="反馈管理" subtitle="分诊、回复与研发队列桥接" :compact="true">
      <template #actions>
        <el-button :icon="Refresh" :loading="loading" @click="loadList">刷新</el-button>
      </template>
    </AppPageHeader>

    <!-- 筛选：admin 规则 §4.3，底槽用 --canvas-sunken 与表格分层 -->
    <el-card class="filter-card" shadow="never">
      <!-- 分诊队列：按状态聚合计数，点一下即筛选，不必先开详情判断先处理谁 -->
      <div class="queue-strip" role="tablist" aria-label="按状态筛选反馈">
        <button
          v-for="item in queueItems"
          :key="item.value || 'all'"
          type="button"
          role="tab"
          class="queue-chip"
          :class="{ 'queue-chip--active': filters.status === item.value }"
          :aria-selected="filters.status === item.value"
          @click="selectStatus(item.value)"
        >
          <span class="queue-chip__dot" :class="`queue-chip__dot--${item.tone}`"></span>
          <span>{{ item.label }}</span>
          <b class="queue-chip__count cell-num">{{ item.count }}</b>
        </button>
      </div>

      <el-form class="filter-form" label-position="top" @submit.prevent="handleSearch">
        <el-form-item label="类型">
          <el-select v-model="filters.type" placeholder="全部" clearable class="filter-control">
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <!-- 优先级是分诊排序的主信号（§4.10 决策 4A），单独成列且收窄宽度，保证筛选区仍是一行 -->
        <el-form-item label="优先级">
          <el-select
            v-model="filters.priority"
            placeholder="全部"
            clearable
            class="filter-control filter-control--priority"
          >
            <el-option v-for="item in priorityOptions" :key="item.value" :label="item.label" :value="item.value" />
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
          <el-table-column label="反馈ID" width="130">
            <template #default="{ row }">
              <el-tooltip :content="row.feedbackId" placement="top">
                <span class="cell-num cell-id">{{ shortId(row.feedbackId) }}</span>
              </el-tooltip>
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

          <el-table-column label="优先级" width="90">
            <template #default="{ row }">
              <StatusTag :status="priorityKey(row.priority)" :text="row.priorityLabel || '普通'" />
            </template>
          </el-table-column>

          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <StatusTag :status="statusKey(row.status)" :text="row.statusLabel || row.status" />
            </template>
          </el-table-column>

          <el-table-column label="负责人" width="110">
            <template #default="{ row }">
              <span v-if="row.assigneeName">{{ row.assigneeName }}</span>
              <span v-else class="cell-muted">未认领</span>
            </template>
          </el-table-column>

          <!-- 停留时长是分诊排序的主信号，绝对提交时间收进悬浮，腾出 §4.10 的首屏预算 -->
          <el-table-column label="停留" width="104">
            <template #default="{ row }">
              <el-tooltip :content="`提交于 ${formatTime(row.createdAt)}`" placement="top">
                <span class="cell-age" :class="{ 'cell-age--stale': isStale(row) }">{{ ageLabel(row) }}</span>
              </el-tooltip>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <RowActions :primary="{ text: '处理', onClick: () => openDetail(row) }" :items="[]" />
            </template>
          </el-table-column>
        </el-table>

        <div v-if="pagination.total > pagination.size" class="pagination-container">
          <el-config-provider :locale="zhCn">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next"
            @current-change="loadList"
            @size-change="handleSearch"
          />
          </el-config-provider>
        </div>
      </template>
    </el-card>

    <el-drawer v-model="detailVisible" title="反馈详情" size="760px" class="feedback-drawer">
      <div v-if="detail" class="detail-body">
        <div class="detail-head">
          <div class="detail-head__meta">
            <StatusTag :status="statusKey(detail.status)" :text="detail.statusLabel || detail.status" />
            <span class="detail-type">{{ detail.typeLabel || detail.type }}</span>
            <span v-if="!['resolved', 'rejected'].includes(detail.status)" class="detail-stay">
              已停留 {{ ageLabel(detail) }}
            </span>
          </div>
          <h3 class="detail-title">{{ detail.content }}</h3>
          <span class="detail-id cell-num">ID {{ detail.feedbackId }}</span>
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
          <h4 class="section-title">消息历史</h4>
          <p class="section-hint">内部备注只在后台可见，不会出现在用户端。</p>
          <ul v-if="detail.messages && detail.messages.length" class="message-list">
            <li v-for="msg in detail.messages" :key="msg.messageId" class="message-item">
              <div class="message-head">
                <span class="message-badge" :class="msg.visibility === 'INTERNAL' ? 'message-badge--internal' : 'message-badge--user'">
                  <el-icon v-if="msg.visibility === 'INTERNAL'" class="message-badge__icon"><Lock /></el-icon>
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
            description="这条反馈没有附件，用户提交时才会带图；需要补充信息可在「回复 / 备注」里说明"
            action-text="去回复 / 备注"
            @action="openReply"
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
              <p v-if="audit.detail" class="audit-detail">{{ auditDetailLabel(audit) }}</p>
            </li>
          </ul>
          <p v-else class="section-empty">还没有操作记录。</p>
        </section>
      </div>

      <template #footer>
        <div class="drawer-actions">
          <el-button v-if="footerActions.primary" type="primary" @click="runAction(footerActions.primary.key)">
            {{ footerActions.primary.label }}
          </el-button>
          <el-button v-if="footerActions.secondary" @click="runAction(footerActions.secondary.key)">
            {{ footerActions.secondary.label }}
          </el-button>
          <el-dropdown v-if="footerActions.more.length" trigger="click" @command="runAction">
            <el-button>
              更多<el-icon class="drawer-more-icon"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-for="action in footerActions.more" :key="action.key" :command="action.key">
                  {{ action.label }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
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

    <el-dialog
      v-model="triageVisible"
      :title="triageIsUpdate ? '更新分诊' : '分诊'"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form label-position="top">
        <!-- 已接受的反馈只能改派 / 调优先级，不再提供状态切换，避免 accepted -> needs_info 这类非法流转 -->
        <el-form-item v-if="!triageIsUpdate" label="分诊结果">
          <el-radio-group v-model="triageForm.status">
            <el-radio-button value="needs_info">待补充信息</el-radio-button>
            <el-radio-button value="accepted">接受</el-radio-button>
            <el-radio-button value="rejected">拒绝</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <p v-else class="form-hint form-hint--lead">
          该反馈已是「已接受」，这里只调整负责人与优先级，状态保持不变。
        </p>
        <el-form-item label="优先级">
          <el-radio-group v-model="triageForm.priority">
            <el-radio-button v-for="item in priorityOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="triageForm.assigneeId" placeholder="暂不指派" clearable class="form-control">
            <el-option
              v-for="item in adminOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <p class="form-hint">留空表示还没人认领，列表里会显示「未认领」。</p>
        </el-form-item>
        <el-form-item v-if="triageForm.status === 'accepted'" label="任务类型">
          <el-checkbox v-model="triageForm.platformSystem">属于平台系统问题</el-checkbox>
          <p class="form-hint">只有平台系统问题才能创建 GitHub Issue。</p>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="triageVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitTriage">
          {{ triageIsUpdate ? '保存调整' : '提交' }}
        </el-button>
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
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { ArrowDown, ChatDotSquare, Lock, Paperclip, Refresh } from '@element-plus/icons-vue'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import AppEmptyState from '@/components/common/AppEmptyState.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import RowActions from '@/components/common/RowActions.vue'
import { formatTime } from '../../utils/formatters'
import {
  createFeedbackIssue,
  getAdminList,
  getFeedbackDetail,
  getFeedbackStats,
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

// 取值沿用 admin 公告的优先级词汇（决策 1A），不新立一套
const priorityOptions = [
  { value: 'normal', label: '普通' },
  { value: 'important', label: '重要' },
  { value: 'urgent', label: '紧急' }
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
  accepted: 'accepted',
  rejected: 'rejected',
  resolved: 'resolved'
}[status] || 'neutral')

// 优先级 → 语义色：紧急=danger、重要=warning、普通=中性（沿用 §3.1 状态表语义）
const priorityKey = (priority) => ({
  urgent: 'rejected',
  important: 'pendingreview',
  normal: 'none'
}[priority] || 'none')

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

// 分诊队列：各状态条数，来自 /admin/feedbacks/stats
const stats = ref({})

// 默认落在待分诊：客服打开这页要看到的是待办队列，不是全量归档
const filters = reactive({ status: 'pending', type: '', priority: '', userId: '', merchantId: '', orderId: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })

// 负责人下拉：来自 /admin/list（IPage 原始结构，取 records）
const adminOptions = ref([])
const loadAdmins = async () => {
  try {
    const response = await getAdminList({ page: 1, pageSize: 100 })
    adminOptions.value = (response?.records || [])
      .filter((item) => item.status !== 'DELETED')
      .map((item) => ({
        value: Number(item.adminId),
        label: item.realName || item.username || String(item.adminId)
      }))
  } catch (error) {
    console.error('加载管理员名单失败:', error)
  }
}

const replyForm = reactive({ visibility: 'USER', content: '' })
const triageForm = reactive({ status: 'accepted', platformSystem: false, priority: 'normal', assigneeId: null })
// 已接受的反馈走「更新分诊」：只改属性不改状态；只提交真正变了的字段，
// 既避免误清 platformSystem，也让 TRIAGE 审计只记录本次变更。
const triageOriginal = reactive({ platformSystem: false, priority: 'normal', assigneeId: null })
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

// 状态 → 语义点：胶囊底色只表达“是否选中”，语义交给小圆点，和表格标签同色系
const QUEUE_TONES = {
  pending: 'warning',
  needs_info: 'warning',
  triaging: 'info',
  accepted: 'info',
  resolved: 'success',
  rejected: 'danger'
}

const queueItems = computed(() => {
  const total = statusOptions.reduce((sum, item) => sum + (stats.value[item.value] || 0), 0)
  return [
    { value: '', label: '全部', count: total, tone: 'none' },
    ...statusOptions.map((item) => ({
      ...item,
      count: stats.value[item.value] || 0,
      tone: QUEUE_TONES[item.value] || 'neutral'
    }))
  ]
})

const selectStatus = (value) => {
  filters.status = value
  handleSearch()
}

const HOUR_MS = 3600 * 1000
// 未结案且停留超过 24h：在队列里压太久了，需要先看
const OPEN_STATUSES = ['pending', 'triaging', 'needs_info', 'accepted']

const ageMs = (row) => {
  const started = new Date(row?.createdAt).getTime()
  return Number.isFinite(started) ? Date.now() - started : NaN
}

const ageLabel = (row) => {
  const ms = ageMs(row)
  if (!Number.isFinite(ms) || ms < 0) return '—'
  const hours = Math.floor(ms / HOUR_MS)
  if (hours < 1) return '刚刚'
  return hours < 24 ? `${hours} 小时` : `${Math.floor(hours / 24)} 天`
}

// 决策 4A：时限不落库，前端按优先级取阈值判定是否超时
const SLA_HOURS = { urgent: 4, important: 24, normal: 72 }

const isStale = (row) => {
  if (!OPEN_STATUSES.includes(row?.status)) return false
  const limit = (SLA_HOURS[row?.priority] || SLA_HOURS.normal) * HOUR_MS
  return ageMs(row) >= limit
}

const activeFilterText = computed(() => {
  const labels = []
  if (filters.status) labels.push(`状态：${statusOptions.find((i) => i.value === filters.status)?.label || filters.status}`)
  if (filters.type) labels.push(`类型：${typeOptions.find((i) => i.value === filters.type)?.label || filters.type}`)
  if (filters.priority) labels.push(`优先级：${priorityOptions.find((i) => i.value === filters.priority)?.label || filters.priority}`)
  if (filters.userId) labels.push(`用户：${filters.userId}`)
  if (filters.merchantId) labels.push(`商家：${filters.merchantId}`)
  if (filters.orderId) labels.push(`订单：${filters.orderId}`)
  return labels.join(' · ')
})

// 详情底栏只留 1 主 + 1 次，其余收进「更多」，避免五个动作平起平坐（§4.1）
const SECONDARY_BY_STATUS = {
  pending: 'reply',
  triaging: 'reply',
  needs_info: 'triage',
  accepted: 'resolve',
  rejected: 'association',
  resolved: 'association'
}

const footerActions = computed(() => {
  const status = detail.value?.status
  const items = [{ key: 'reply', label: status === 'needs_info' ? '回复用户' : '回复 / 备注' }]
  if (status === 'accepted') {
    // #548：已接受只能改派 / 调优先级，入口语义与首次分诊区分开，避免用户走到必然 409 的路径
    items.push({ key: 'triage', label: '转派 / 调整' })
    items.push({ key: 'resolve', label: '解决' })
    items.push({ key: 'issue', label: '建 GitHub Issue' })
  } else if (!['rejected', 'resolved'].includes(status)) {
    items.push({ key: 'triage', label: '分诊' })
  }
  items.push({ key: 'association', label: '修正关联' })

  // 主动作按状态取最高频的下一步：先分诊，之后都是沟通
  const primaryKey = ['pending', 'triaging'].includes(status) ? 'triage' : 'reply'
  const primary = items.find((item) => item.key === primaryKey)
  const rest = items.filter((item) => item.key !== primaryKey)
  const secondary = rest.find((item) => item.key === SECONDARY_BY_STATUS[status]) || rest[0]
  return { primary, secondary, more: rest.filter((item) => item !== secondary) }
})

const authorLabel = (type) => authorLabels[type] || type
const auditLabel = (action) => auditLabels[action] || action
const resolutionLabel = (code) =>
  code ? (resolutionOptions.find((item) => item.value === code)?.label || code) : '—'

// 审计详情由后端按 status=…/visibility=… 原始串落库，展示层转成中文，不改存储格式。
// #548 起 TRIAGE 可能带多段（status; priority; assigneeId; platformSystem），按 ; 拆开逐段翻译。
const auditDetailLabel = (audit) => {
  const raw = audit?.detail || ''
  const statusText = (code) => statusOptions.find((item) => item.value === code)?.label || code
  const priorityText = (code) =>
    code === 'none' ? '未设置' : (priorityOptions.find((item) => item.value === code)?.label || code)
  const labelSegment = (segment) => {
    const sep = segment.indexOf('=')
    if (sep < 0) return segment
    const key = segment.slice(0, sep).trim()
    const value = segment.slice(sep + 1).trim()
    if (key === 'status') {
      const [from, to] = value.split('->')
      return `状态：${statusText(from)} → ${statusText(to)}`
    }
    if (key === 'visibility') return value === 'INTERNAL' ? '范围：内部备注' : '范围：用户可见'
    if (key === 'resolution') return `结果：${resolutionOptions.find((item) => item.value === value)?.label || value}`
    if (key === 'orderId') {
      const [from, to] = value.split('->')
      return `订单归属：${from || '未关联'} → ${to || '未关联'}`
    }
    if (key === 'priority') {
      const [from, to] = value.split('->')
      return `优先级：${priorityText(from)} → ${priorityText(to)}`
    }
    if (key === 'assigneeId') {
      const [from, to] = value.split('->')
      const assigneeText = (v) => (!v || v === 'none' ? '未认领' : `#${v}`)
      return `负责人：${assigneeText(from)} → ${assigneeText(to)}`
    }
    if (key === 'platformSystem') {
      const [from, to] = value.split('->')
      const boolText = (v) => (v === 'true' ? '是' : '否')
      return `平台系统问题：${boolText(from)} → ${boolText(to)}`
    }
    if (key === 'issue') return `研发单：${value}`
    return segment
  }
  if (!raw.includes(';')) {
    return labelSegment(raw)
  }
  return raw.split(';').map((part) => labelSegment(part.trim())).join(' · ')
}

// 19 位反馈 ID 在列表里读不出信息，只留尾部便于人工比对，全量在悬浮里
const shortId = (id) => (id && id.length > 8 ? `…${id.slice(-8)}` : id || '—')

const loadStats = async () => {
  try {
    const response = await getFeedbackStats()
    stats.value = response?.data || {}
  } catch (error) {
    console.error('加载分诊队列计数失败:', error)
  }
}

const loadList = async () => {
  loading.value = true
  try {
    const response = await listFeedbacks({
      page: pagination.page,
      size: pagination.size,
      status: filters.status || undefined,
      type: filters.type || undefined,
      priority: filters.priority || undefined,
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
  filters.priority = ''
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
// accepted 状态下的按钮语义是「转派 / 调整」，对话框只允许改属性
const triageIsUpdate = computed(() => detail.value?.status === 'accepted')
const openTriage = () => {
  const current = detail.value || {}
  triageForm.status = 'accepted'
  triageForm.platformSystem = Boolean(current.platformSystem)
  triageOriginal.platformSystem = Boolean(current.platformSystem)
  triageForm.priority = current.priority || 'normal'
  triageOriginal.priority = triageForm.priority
  triageForm.assigneeId = current.assigneeId ?? null
  triageOriginal.assigneeId = triageForm.assigneeId
  triageVisible.value = true
}
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
    await Promise.all([loadList(), loadStats()])
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

const submitTriage = () => {
  const isAccepted = triageForm.status === 'accepted'
  const updateMode = triageIsUpdate.value
  // accepted 自流转：只提交真正变了的字段。未改动就不发送，避免后端误清 platformSystem、
  // 或往 TRIAGE 审计里写一条 X->X 的假变更（#548）。
  const changed = (next, original) => (next !== original ? next : undefined)
  const platformSystem = !isAccepted
    ? undefined
    : updateMode
      ? changed(Boolean(triageForm.platformSystem), triageOriginal.platformSystem)
      : triageForm.platformSystem
  const priority = updateMode ? changed(triageForm.priority, triageOriginal.priority) : triageForm.priority
  const assigneeId = updateMode ? changed(triageForm.assigneeId, triageOriginal.assigneeId) : triageForm.assigneeId
  if (updateMode && platformSystem === undefined && priority === undefined && assigneeId === undefined) {
    ElMessage.info('没有需要保存的改动')
    return undefined
  }
  return withSubmit(
    () => triageFeedback(currentId.value, triageForm.status, platformSystem, { priority, assigneeId }),
    updateMode ? '分诊已更新' : '分诊完成',
    () => { triageVisible.value = false }
  )
}

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

onMounted(() => {
  loadStats()
  loadAdmins()
  loadList()
})
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

/* 首屏是硬预算（§4.10），页头下边距在这里收一档 */
.app-page-header {
  margin-bottom: var(--space-2);
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
    padding: var(--space-2) var(--space-4);
    border-radius: var(--radius-md);
    background: var(--canvas-sunken);

    :deep(.el-form-item) {
      margin: 0;
    }
  }

  .filter-control {
    width: 160px;
  }

  /* 优先级取值只有三个，收窄到 120px，把新增控件的横向预算压到最小（§4.10 顶部 ≤330px） */
  .filter-control--priority {
    width: 120px;
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
    margin-bottom: var(--space-2);
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

/* 分诊队列：一眼看清各状态积压，点一下即筛选 */
.queue-strip {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-bottom: var(--space-2);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--line);
}

/* 筛选只有一行，吃掉 el-form-item 默认的下外边距，把纵向预算让给表格（§4.10） */
.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.queue-chip__dot {
  width: 6px;
  height: 6px;
  border-radius: var(--radius-full);
  background: var(--ink-400);
}

.queue-chip__dot--none {
  display: none;
}

.queue-chip__dot--warning {
  background: var(--warning);
}

.queue-chip__dot--info {
  background: var(--info);
}

.queue-chip__dot--success {
  background: var(--success);
}

.queue-chip__dot--danger {
  background: var(--danger);
}

.queue-chip--active .queue-chip__dot {
  background: var(--surface);
}

.queue-chip {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  height: var(--control-height-sm);
  padding: 0 var(--space-4);
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  color: var(--ink-700);
  cursor: pointer;
  background: var(--canvas-sunken);
  border: 1px solid transparent;
  border-radius: var(--radius-full);
  transition: background var(--duration-fast), color var(--duration-fast), border-color var(--duration-fast);
}

.queue-chip:hover {
  border-color: var(--line-strong);
}

.queue-chip:focus-visible {
  outline: 2px solid var(--brand-400);
  outline-offset: 1px;
}

.queue-chip--active {
  color: var(--surface);
  background: var(--brand-500);
}

.queue-chip__count {
  font-size: var(--text-caption);
  font-variant-numeric: tabular-nums;
  opacity: 0.75;
}

.queue-chip--active .queue-chip__count {
  opacity: 0.9;
}

/* 停留时长：超 24h 未结案才提示，避免整列都在喊 */
.cell-age {
  color: var(--ink-700);
  font-variant-numeric: tabular-nums;
}

.cell-id {
  color: var(--ink-500);
}

.cell-muted {
  color: var(--ink-400);
}

.cell-age--stale {
  color: var(--warning);
  font-weight: var(--weight-medium);
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
  flex-direction: column;
  gap: var(--space-2);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--line);
}

.detail-head__meta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

/* 抽屉主角是用户遇到的问题，不是单据号 */
.detail-title {
  margin: var(--space-1) 0 0;
  color: var(--ink-900);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  line-height: 24px;
}

.detail-id {
  color: var(--ink-400);
  font-size: var(--text-caption);
  font-weight: var(--weight-regular);
}

.detail-type,
.detail-stay {
  color: var(--ink-500);
  font-size: var(--text-body-sm);
}

.detail-stay {
  margin-left: auto;
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

.message-badge__icon {
  margin-right: var(--space-1);
  vertical-align: -1px;
}

.drawer-more-icon {
  margin-left: var(--space-1);
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
  align-items: center;
  justify-content: flex-end;
}

/* 抽屉里的主动作走 md 档，次级保持 sm（§3.4、§4.1） */
:deep(.el-drawer__footer .el-button--primary) {
  height: var(--control-height-md);
}

.form-hint {
  margin: var(--space-1) 0 0;
  color: var(--ink-500);
  font-size: var(--text-caption);
}

/* 「更新分诊」用一句说明替代状态选择器，避免用户误以为能改回 needs_info（#548） */
.form-hint--lead {
  margin: 0 0 var(--space-4);
  color: var(--ink-700);
  font-size: var(--text-body-sm);
  line-height: 20px;
}

.issue-alert {
  margin-bottom: var(--space-4);
}
</style>
