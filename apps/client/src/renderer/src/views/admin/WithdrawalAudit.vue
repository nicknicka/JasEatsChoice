<template>
  <div class="withdrawal-audit-page">
    <!-- 页头：统一标题容器 + 右侧动作区（§4.7）；自动刷新带开关态与最近刷新时间（P2） -->
    <AppPageHeader title="提现审核" :compact="true">
      <template #actions>
        <label class="refresh-toggle">
          <el-switch
            v-model="autoRefresh"
            size="small"
            @change="handleAutoRefreshChange"
          />
          <span class="refresh-toggle__text">自动刷新</span>
        </label>
        <span v-if="lastRefreshAt" class="refresh-time">最近刷新 {{ lastRefreshAt }}</span>
        <el-button :icon="Download" @click="handleExport">导出</el-button>
      </template>
    </AppPageHeader>

    <!-- 概览卡：线性图标 + 数值 + 标签，四个一行（§4.8） -->
    <div class="stats-section">
      <AppStatCard
        label="待审核"
        :value="stats.pending"
        icon="Clock"
        :trend="trendOf(stats.pendingTrend)"
      />
      <AppStatCard label="处理中" :value="stats.processing" icon="Loading" />
      <AppStatCard
        label="今日提现"
        :value="`¥${formatAmount(stats.todayAmount)}`"
        icon="Money"
        :trend="trendOf(stats.todayTrend)"
      />
      <AppStatCard
        label="累计提现"
        :value="`¥${formatAmount(stats.totalAmount)}`"
        icon="Wallet"
      />
    </div>

    <!-- 趋势图：次级可折叠区块，默认收起，把提现列表提上首屏（P0 / §4.8） -->
    <el-card shadow="never" class="chart-card">
      <el-collapse v-model="chartExpanded" class="chart-collapse" @change="handleChartToggle">
        <el-collapse-item name="trend">
          <template #title>
            <span class="chart-title">提现趋势</span>
          </template>

          <div class="chart-toolbar">
            <el-radio-group v-model="chartPeriod" size="small" @change="handleChartPeriodChange">
              <el-radio-button label="week">近7天</el-radio-button>
              <el-radio-button label="month">近30天</el-radio-button>
            </el-radio-group>
          </div>

          <!-- 趋势空态：该周期没有提现时不画贴底折线（P0） -->
          <div v-loading="chartLoading" class="chart-body">
            <AppEmptyState
              v-if="!chartLoading && chartEmpty"
              compact
              icon="TrendCharts"
              description="该周期暂无提现"
            />
            <div v-show="!chartEmpty" ref="chartRef" class="chart-container"></div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </el-card>

    <!-- 筛选条：所有条件并入同一条 inline 表单，减少折行留白（P1 / §4.3） -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="searchForm" class="search-form" @submit.prevent>
        <el-form-item>
          <el-input
            v-model="searchForm.keyword"
            placeholder="流水号/用户ID/用户昵称"
            clearable
            class="filter-keyword"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-select
            v-model="searchForm.status"
            placeholder="状态"
            clearable
            class="filter-select"
            @change="handleSearch"
          >
            <el-option
              v-for="item in STATUS_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select
            v-model="searchForm.withdrawMethod"
            placeholder="提现方式"
            clearable
            class="filter-select"
            @change="handleSearch"
          >
            <el-option
              v-for="item in METHOD_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            class="filter-date"
            @change="handleSearch"
          />
        </el-form-item>
        <el-form-item class="filter-amount">
          <span class="filter-label">金额范围：</span>
          <el-input-number
            v-model="searchForm.minAmount"
            :min="0"
            :precision="2"
            placeholder="最小金额"
            controls-position="right"
            class="filter-number"
          />
          <span class="range-divider">-</span>
          <el-input-number
            v-model="searchForm.maxAmount"
            :min="0"
            :precision="2"
            placeholder="最大金额"
            controls-position="right"
            class="filter-number"
          />
          <el-button type="primary" link @click="handleSearch">应用</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="RefreshLeft" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 提现列表：页面主角，紧跟页头与筛选条（P0 / §4.3） -->
    <el-card shadow="never" class="table-card">
      <!-- 空数据：紧凑中文空状态替换表体（§4.3、§5.5） -->
      <AppEmptyState
        v-if="!loading && tableData.length === 0"
        compact
        icon="Wallet"
        description="没有符合条件的提现记录，换个关键词或清空筛选试试"
        action-text="重置筛选"
        @action="handleReset"
      />

      <template v-else>
        <el-table
          :data="tableData"
          v-loading="loading"
          stripe
          class="data-table"
          @selection-change="handleSelectionChange"
          @row-dblclick="handleView"
        >
          <el-table-column type="selection" width="48" fixed="left" />
          <el-table-column type="expand">
            <template #default="{ row }">
              <div class="expand-content">
                <el-descriptions :column="3" border>
                  <el-descriptions-item label="流水号" :span="3">{{ row.withdrawNo }}</el-descriptions-item>
                  <el-descriptions-item label="用户昵称">{{ row.nickname || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="用户ID">{{ row.userId }}</el-descriptions-item>
                  <el-descriptions-item label="提现方式">
                    {{ getMethodText(row.withdrawMethod) }}
                  </el-descriptions-item>
                  <el-descriptions-item label="提现金额">
                    <span class="amount-text">¥{{ formatAmount(row.amount) }}</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="手续费">¥{{ formatAmount(row.fee || 0) }}</el-descriptions-item>
                  <el-descriptions-item label="实际到账">
                    <span class="amount-text">¥{{ formatAmount(row.actualAmount || row.amount) }}</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="提现账号" :span="3">{{ row.accountInfo || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="申请时间" :span="3">{{ formatTime(row.createdAt) }}</el-descriptions-item>
                  <el-descriptions-item v-if="row.auditor" label="审核人">{{ row.auditor }}</el-descriptions-item>
                  <el-descriptions-item v-if="row.auditTime" label="审核时间">{{ formatTime(row.auditTime) }}</el-descriptions-item>
                  <el-descriptions-item v-if="row.rejectReason" label="拒绝原因" :span="3">
                    <span class="error-text">{{ row.rejectReason }}</span>
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </template>
          </el-table-column>
          <!-- 流水号：不可拆分标识，单行 + 等宽数字，点击复制（§4.3、§5.4） -->
          <el-table-column prop="withdrawNo" label="流水号" width="170" fixed="left">
            <template #default="{ row }">
              <el-tooltip content="点击复制" placement="top">
                <span class="cell-num copy-text" @click="copyText(row.withdrawNo)">
                  {{ row.withdrawNo }}
                  <el-icon><DocumentCopy /></el-icon>
                </span>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column label="用户" width="170" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="user-cell">
                <el-avatar v-if="row.avatar" :src="row.avatar" :size="32" />
                <el-avatar v-else :size="32">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <div class="user-info">
                  <div class="user-nickname">{{ row.nickname || '-' }}</div>
                  <div class="user-id">{{ row.userId }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <!-- 金额：右对齐 + 中性深色 + 无衬线等宽数字，不带红绿蓝（§4.3、§5.2） -->
          <el-table-column label="提现金额" width="120" align="right" sortable prop="amount">
            <template #default="{ row }">
              <span :class="['amount-text', { 'high-amount': row.amount >= 10000 }]">
                ¥{{ formatAmount(row.amount) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="提现方式" width="100">
            <template #default="{ row }">
              {{ getMethodText(row.withdrawMethod) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <StatusTag :status="row.withdrawStatus" :text="getStatusText(row.withdrawStatus)" />
            </template>
          </el-table-column>
          <!-- 时间统一 YYYY-MM-DD HH:mm，去掉 ISO 的 T（§4.3） -->
          <el-table-column label="申请时间" width="160" sortable prop="createdAt">
            <template #default="{ row }">
              <span class="cell-num">{{ formatTime(row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="showAuditInfo" label="审核人" width="110">
            <template #default="{ row }">
              {{ row.auditor || '-' }}
            </template>
          </el-table-column>
          <el-table-column v-if="showAuditInfo" label="审核时间" width="160">
            <template #default="{ row }">
              <span class="cell-num">{{ formatTime(row.auditTime) }}</span>
            </template>
          </el-table-column>
          <!-- 行操作：查看 + 通过可见（均先弹对话框），拒绝收进「更多」（§4.3 行操作可见性规则） -->
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <RowActions
                :primary="{ text: '查看', onClick: () => handleView(row) }"
                :items="rowActions(row)"
                :max-visible="1"
              />
            </template>
          </el-table-column>
        </el-table>

        <!-- 批量操作 -->
        <div v-if="selectedRows.length > 0" class="batch-actions">
          <el-alert
            :title="`已选择 ${selectedRows.length} 条记录`"
            type="info"
            :closable="false"
          >
            <el-button size="small" @click="handleBatchApprove">批量通过</el-button>
            <el-button size="small" type="danger" plain @click="handleBatchReject">批量拒绝</el-button>
            <el-button size="small" @click="selectedRows = []">取消选择</el-button>
          </el-alert>
        </div>

        <!-- 分页：合计用实际记录数兜底，空数据时不渲染（§4.3、§5.4） -->
        <div v-if="tableData.length > 0" class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="fetchData"
            @current-change="fetchData"
          />
        </div>
      </template>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="提现详情" width="700px" destroy-on-close>
      <div v-if="currentRow" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="流水号" :span="2">
            <div class="copy-item">
              {{ currentRow.withdrawNo }}
              <el-button link :icon="DocumentCopy" @click="copyText(currentRow.withdrawNo)" />
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="用户信息" :span="2">
            <div class="user-detail">
              <el-avatar v-if="currentRow.avatar" :src="currentRow.avatar" :size="40" />
              <el-avatar v-else :size="40">
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="user-detail-info">
                <div class="user-nickname">{{ currentRow.nickname || '-' }}</div>
                <div class="user-id">{{ currentRow.userId }}</div>
              </div>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="提现金额">
            <span class="amount-text">¥{{ formatAmount(currentRow.amount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="手续费">¥{{ formatAmount(currentRow.fee || 0) }}</el-descriptions-item>
          <el-descriptions-item label="实际到账" :span="2">
            <span class="amount-text">¥{{ formatAmount(currentRow.actualAmount || currentRow.amount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="提现方式" :span="2">
            {{ getMethodText(currentRow.withdrawMethod) }}
          </el-descriptions-item>
          <el-descriptions-item label="提现账号" :span="2">
            {{ currentRow.accountInfo || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <StatusTag
              :status="currentRow.withdrawStatus"
              :text="getStatusText(currentRow.withdrawStatus)"
            />
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatTime(currentRow.createdAt) }}</el-descriptions-item>
          <el-descriptions-item v-if="currentRow.auditor" label="审核人">
            {{ currentRow.auditor }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentRow.auditTime" label="审核时间">
            {{ formatTime(currentRow.auditTime) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentRow.rejectReason" label="拒绝原因" :span="2">
            <span class="error-text">{{ currentRow.rejectReason }}</span>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentRow.auditComment && currentRow.withdrawStatus !== 'rejected'" label="审核意见" :span="2">
            {{ currentRow.auditComment }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 操作日志 -->
        <div v-if="currentRow.logs && currentRow.logs.length > 0" class="logs-section">
          <div class="logs-title">操作日志</div>
          <el-timeline>
            <el-timeline-item
              v-for="(log, index) in currentRow.logs"
              :key="index"
              :timestamp="formatTime(log.time)"
              placement="top"
            >
              <div class="log-item">
                <span class="log-action">{{ log.action }}</span>
                <span class="log-user">{{ log.operator }}</span>
                <span v-if="log.comment" class="log-comment">: {{ log.comment }}</span>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="currentRow && currentRow.withdrawStatus === 'pending'"
          type="primary"
          :icon="CircleCheck"
          @click="handleAudit(currentRow, 'APPROVE')"
        >
          审核通过
        </el-button>
        <el-button
          v-if="currentRow && currentRow.withdrawStatus === 'pending'"
          type="danger"
          plain
          :icon="CircleClose"
          @click="handleAudit(currentRow, 'REJECT')"
        >
          审核拒绝
        </el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditVisible"
      :title="auditDecision === 'APPROVE' ? '审核通过' : '审核拒绝'"
      width="500px"
      destroy-on-close
    >
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="流水号">
          <el-input :value="currentRow?.withdrawNo" disabled />
        </el-form-item>
        <el-form-item label="用户信息">
          <el-input :value="`${currentRow?.nickname || '-'} (ID: ${currentRow?.userId})`" disabled />
        </el-form-item>
        <el-form-item label="提现金额">
          <el-input :value="'¥' + (currentRow?.amount || 0)" disabled>
            <template #prefix>
              <span v-if="currentRow?.amount >= 10000" class="high-amount">大额</span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item v-if="auditDecision === 'REJECT'" label="快速原因">
          <el-select
            v-model="auditForm.quickReason"
            placeholder="选择快速原因"
            clearable
            style="width: 100%"
            @change="handleQuickReason"
          >
            <el-option
              v-for="item in REJECT_REASONS"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input
            v-model="auditForm.comment"
            type="textarea"
            :rows="3"
            :placeholder="auditDecision === 'APPROVE' ? '请输入通过意见（可选）' : '请输入拒绝原因'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button
          :type="auditDecision === 'APPROVE' ? 'primary' : 'danger'"
          :plain="auditDecision !== 'APPROVE'"
          @click="submitAudit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量审核对话框 -->
    <el-dialog
      v-model="batchAuditVisible"
      :title="batchAuditDecision === 'APPROVE' ? '批量通过' : '批量拒绝'"
      width="500px"
      destroy-on-close
    >
      <el-alert
        :title="getBatchAuditTitle()"
        type="warning"
        :closable="false"
        style="margin-bottom: 20px"
      />
      <el-form :model="batchAuditForm" label-width="80px">
        <el-form-item v-if="batchAuditDecision === 'REJECT'" label="快速原因">
          <el-select
            v-model="batchAuditForm.quickReason"
            placeholder="选择快速原因"
            clearable
            style="width: 100%"
            @change="handleBatchQuickReason"
          >
            <el-option
              v-for="item in REJECT_REASONS"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input
            v-model="batchAuditForm.comment"
            type="textarea"
            :rows="3"
            :placeholder="batchAuditDecision === 'APPROVE' ? '请输入通过意见（可选）' : '请输入拒绝原因'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchAuditVisible = false">取消</el-button>
        <el-button
          :type="batchAuditDecision === 'APPROVE' ? 'primary' : 'danger'"
          :plain="batchAuditDecision !== 'APPROVE'"
          @click="submitBatchAudit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, RefreshLeft, Download, CircleCheck, CircleClose, DocumentCopy, User } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import AppStatCard from '@/components/common/AppStatCard.vue'
import AppEmptyState from '@/components/common/AppEmptyState.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import RowActions from '@/components/common/RowActions.vue'
import { formatTime } from '@/utils/formatters'
import {
  getWithdrawList,
  getWithdrawDetail,
  processWithdraw,
  batchProcessWithdraw,
  getWithdrawStatistics,
  getWithdrawTrend
} from '@/api/admin'

// ============ 常量配置 ============
const STATUS_OPTIONS = [
  { label: '待审核', value: 'pending' },
  { label: '已通过', value: 'approved' },
  { label: '已拒绝', value: 'rejected' },
  { label: '处理中', value: 'processing' },
  { label: '成功', value: 'success' },
  { label: '失败', value: 'failed' }
]

const METHOD_OPTIONS = [
  { label: '微信', value: 'wechat' },
  { label: '支付宝', value: 'alipay' },
  { label: '银行卡', value: 'bank' }
]

const REJECT_REASONS = [
  '账户信息异常',
  '涉嫌违规操作',
  '资料不完整',
  '需要补充验证',
  '风控系统拦截'
]

const STATUS_TEXT_MAP = {
  'pending': '待审核',
  'approved': '已通过',
  'rejected': '已拒绝',
  'processing': '处理中',
  'success': '成功',
  'failed': '失败'
}

const METHOD_TEXT_MAP = {
  'wechat': '微信',
  'alipay': '支付宝',
  'bank': '银行卡'
}

const AUTO_REFRESH_INTERVAL = 30000 // 30秒

// ============ 状态变量 ============
const loading = ref(false)
const tableData = ref([])
const currentRow = ref(null)
const detailVisible = ref(false)
const auditVisible = ref(false)
const batchAuditVisible = ref(false)
const selectedRows = ref([])
const autoRefresh = ref(false)
const autoRefreshTimer = ref(null)
const chartRef = ref(null)
const chartInstance = ref(null)
let chartResizeObserver = null
const chartPeriod = ref('week')
const chartExpanded = ref([])
const chartEmpty = ref(false)
const chartLoading = ref(false)
const trendSeries = ref({ dates: [], amounts: [], counts: [] })
const lastRefreshAt = ref('')

const stats = reactive({
  pending: 0,
  pendingTrend: 0,
  processing: 0,
  todayAmount: 0,
  todayTrend: 0,
  totalAmount: 0
})

const searchForm = reactive({
  keyword: '',
  status: '',
  withdrawMethod: '',
  dateRange: null,
  minAmount: null,
  maxAmount: null
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const auditForm = reactive({
  comment: '',
  quickReason: ''
})

const batchAuditForm = reactive({
  comment: '',
  quickReason: ''
})

const auditDecision = ref('')
const batchAuditDecision = ref('')

// ============ 计算属性 ============
const showAuditInfo = computed(() => {
  return searchForm.status === 'approved' || searchForm.status === 'rejected' ||
         searchForm.status === 'success' || searchForm.status === 'failed' ||
         !searchForm.status
})

// ============ 工具函数 ============
const formatAmount = (amount) => {
  if (!amount && amount !== 0) return '0.00'
  return Number(amount).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

const getStatusText = (status) => STATUS_TEXT_MAP[status] || '未知'

const getMethodText = (method) => METHOD_TEXT_MAP[method] || '未知'

// 概览卡趋势：0 表示无变化，交给 AppStatCard 渲染趋势标（§4.8）
const trendOf = (value) => {
  const delta = Number(value) || 0
  if (delta === 0) return null
  return { direction: delta > 0 ? 'up' : 'down', text: `${Math.abs(delta)}%` }
}

// 行操作：先弹对话框的动作保持可见，「拒绝」按每行最多 2 个可见动作收进「更多」（§4.3 行操作可见性规则）
const rowActions = (row) => {
  if (row.withdrawStatus !== 'pending') return []
  return [
    { text: '通过', onClick: () => handleAudit(row, 'APPROVE') },
    { text: '拒绝', onClick: () => handleAudit(row, 'REJECT') }
  ]
}

const copyText = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('复制成功')
  } catch {
    ElMessage.error('复制失败')
  }
}

// ============ 数据获取 ============
const fetchData = async () => {
  loading.value = true
  try {
    const [startDate, endDate] = searchForm.dateRange || [null, null]

    const response = await getWithdrawList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      status: searchForm.status,
      withdrawMethod: searchForm.withdrawMethod,
      startDate,
      endDate,
      minAmount: searchForm.minAmount,
      maxAmount: searchForm.maxAmount
    })

    if (response && response.success) {
      tableData.value = response.records || []
      pagination.total = response.total ?? 0
      lastRefreshAt.value = new Date().toLocaleTimeString('zh-CN', { hour12: false })
    } else {
      tableData.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('[提现审核] 获取数据失败:', error)
    ElMessage.error('获取数据失败')
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const fetchStats = async () => {
  try {
    const response = await getWithdrawStatistics()
    if (response && response.success && response.data) {
      stats.pending = response.data.pendingCount || 0
      stats.pendingTrend = response.data.pendingTrend || 0
      stats.processing = response.data.processingCount || 0
      stats.todayAmount = response.data.todayWithdraw || 0
      stats.todayTrend = response.data.todayTrend || 0
      stats.totalAmount = response.data.totalWithdraw || 0
    }
  } catch (error) {
    console.error('[提现审核] 获取统计失败:', error)
  }
}

// ============ 图表相关 ============
// 图表颜色只从 tokens.css 读，不在 JS 里写裸色值（§7.1）
const cssVar = (name) => getComputedStyle(document.documentElement).getPropertyValue(name).trim()

const syncAxisVisibility = (params) => {
  if (!chartInstance.value) return
  const option = chartInstance.value.getOption()
  if (params.name === '提现金额') {
    option.yAxis[0].show = params.selected['提现金额']
  } else if (params.name === '提现笔数') {
    option.yAxis[1].show = params.selected['提现笔数']
  }
  chartInstance.value.setOption(option)
}

const ensureChart = () => {
  if (!chartRef.value) return null
  if (!chartInstance.value) {
    chartInstance.value = echarts.init(chartRef.value)
    chartInstance.value.on('legendselectchanged', syncAxisVisibility)
    if (typeof ResizeObserver !== 'undefined') {
      chartResizeObserver = new ResizeObserver(() => chartInstance.value?.resize())
      chartResizeObserver.observe(chartRef.value)
    }
  }
  return chartInstance.value
}

const buildChartOption = ({ dates, amounts, counts }) => {
  const amountColor = cssVar('--brand-500')
  const countColor = cssVar('--info')

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['提现金额', '提现笔数'],
      bottom: 5,
      left: 'center',
      itemGap: 20,
      selectedMode: true,
      textStyle: {
        fontSize: 12
      },
      selected: {
        '提现金额': true,
        '提现笔数': true
      }
    },
    grid: {
      left: 50,
      right: 50,
      bottom: 60,
      top: 40,
      containLabel: false
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: [
      {
        type: 'value',
        name: '金额(元)',
        position: 'left',
        nameTextStyle: {
          fontSize: 12
        },
        show: true,
        axisLine: {
          show: true
        },
        axisLabel: {
          show: true
        }
      },
      {
        type: 'value',
        name: '笔数',
        position: 'right',
        nameTextStyle: {
          fontSize: 12
        },
        show: true,
        axisLine: {
          show: true
        },
        axisLabel: {
          show: true
        },
        // 笔数是计数：量纲只能取整数，不再出现 0~1 的 0.2 刻度（P0）
        minInterval: 1,
        min: 0
      }
    ],
    series: [
      {
        name: '提现金额',
        type: 'line',
        smooth: true,
        data: amounts,
        itemStyle: {
          color: amountColor
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: echarts.color.modifyAlpha(amountColor, 0.3) },
            { offset: 1, color: echarts.color.modifyAlpha(amountColor, 0.05) }
          ])
        }
      },
      {
        name: '提现笔数',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: counts,
        itemStyle: {
          color: countColor
        }
      }
    ]
  }
}

// 趋势图只在展开时取数；该周期全为 0 时走空态，不画贴底折线（P0）
const updateChart = async () => {
  const days = chartPeriod.value === 'week' ? 7 : 30
  const dates = []
  const amounts = []
  const counts = []
  chartLoading.value = true

  try {
    const response = await getWithdrawTrend(days)
    if (response && response.data) {
      response.data.forEach((item) => {
        dates.push(item.date)
        amounts.push(item.amount || 0)
        counts.push(item.count || 0)
      })
    }
  } catch (error) {
    console.error('[提现审核] 获取图表数据失败:', error)
  } finally {
    chartLoading.value = false
  }

  trendSeries.value = { dates, amounts, counts }
  chartEmpty.value = !amounts.some((value) => value > 0) && !counts.some((value) => value > 0)

  if (chartEmpty.value) return

  await nextTick()
  const instance = ensureChart()
  if (!instance) return
  instance.setOption(buildChartOption(trendSeries.value), true)
  instance.resize()
}

const handleChartToggle = (activeNames) => {
  if (Array.isArray(activeNames) && activeNames.includes('trend')) {
    updateChart()
  }
}

const handleChartPeriodChange = () => {
  updateChart()
}

// ============ 搜索相关 ============
const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    status: '',
    withdrawMethod: '',
    dateRange: null,
    minAmount: null,
    maxAmount: null
  })
  pagination.page = 1
  fetchData()
}

// ============ 表格操作 ============
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const handleView = async (row) => {
  try {
    const response = await getWithdrawDetail(row.id)
    if (response && response.success) {
      currentRow.value = response.data
      detailVisible.value = true
    } else {
      ElMessage.error('获取详情失败')
    }
  } catch (error) {
    console.error('[提现审核] 获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// ============ 审核相关 ============
const handleAudit = (row, decision) => {
  auditDecision.value = decision
  auditForm.comment = ''
  auditForm.quickReason = ''
  currentRow.value = row
  detailVisible.value = false
  auditVisible.value = true
}

const handleQuickReason = () => {
  if (auditForm.quickReason) {
    auditForm.comment = auditForm.quickReason
  }
}

const submitAudit = async () => {
  if (auditDecision.value === 'REJECT' && !auditForm.comment) {
    ElMessage.warning('请填写拒绝原因')
    return
  }

  try {
    const response = await processWithdraw(currentRow.value.id, {
      decision: auditDecision.value,
      comment: auditForm.comment
    })

    if (response && response.success) {
      ElMessage.success(auditDecision.value === 'APPROVE' ? '审核通过' : '已拒绝')
      auditVisible.value = false
      fetchData()
      fetchStats()
    } else {
      ElMessage.error(response?.message || '审核失败')
    }
  } catch (error) {
    console.error('[提现审核] 审核失败:', error)
    ElMessage.error('审核失败')
  }
}

// ============ 批量操作 ============
const handleBatchApprove = () => {
  batchAuditDecision.value = 'APPROVE'
  batchAuditForm.comment = ''
  batchAuditForm.quickReason = ''
  batchAuditVisible.value = true
}

const handleBatchReject = () => {
  batchAuditDecision.value = 'REJECT'
  batchAuditForm.comment = ''
  batchAuditForm.quickReason = ''
  batchAuditVisible.value = true
}

const handleBatchQuickReason = () => {
  if (batchAuditForm.quickReason) {
    batchAuditForm.comment = batchAuditForm.quickReason
  }
}

const getBatchAuditTitle = () => {
  const action = batchAuditDecision.value === 'APPROVE' ? '通过' : '拒绝'
  const count = selectedRows.value.length
  return `确定要${action}选中的 ${count} 条提现申请吗？`
}

const submitBatchAudit = async () => {
  if (batchAuditDecision.value === 'REJECT' && !batchAuditForm.comment) {
    ElMessage.warning('请填写拒绝原因')
    return
  }

  try {
    const ids = selectedRows.value.map(row => row.id)
    const response = await batchProcessWithdraw({
      withdrawIds: ids,
      decision: batchAuditDecision.value,
      comment: batchAuditForm.comment
    })

    if (response && response.success) {
      const successCount = response.successCount || 0
      const failCount = response.failCount || 0
      let msg = `批量处理完成：成功 ${successCount} 个`
      if (failCount > 0) {
        msg += `，失败 ${failCount} 个`
      }
      ElMessage.success(msg)
      batchAuditVisible.value = false
      selectedRows.value = []
      fetchData()
      fetchStats()
    } else {
      ElMessage.error(response?.message || '批量审核失败')
    }
  } catch (error) {
    console.error('[提现审核] 批量审核失败:', error)
    ElMessage.error('批量审核失败')
  }
}

// ============ 导出功能 ============
const handleExport = async () => {
  // TODO: 导出功能待后端实现
  ElMessage.info('导出功能开发中，敬请期待')
  return

  // 后续实现代码（需后端支持 exportWithdrawList 接口）
  /*
  try {
    const [startDate, endDate] = searchForm.dateRange || [null, null]

    await ElMessageBox.confirm(
      '确定要导出当前搜索条件下的数据吗？',
      '导出确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    const response = await exportWithdrawList({
      keyword: searchForm.keyword,
      status: searchForm.status,
      withdrawMethod: searchForm.withdrawMethod,
      startDate,
      endDate,
      minAmount: searchForm.minAmount,
      maxAmount: searchForm.maxAmount
    })

    if (response) {
      // 处理文件下载
      const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `提现记录_${new Date().toLocaleDateString('zh-CN')}.xlsx`
      link.click()
      window.URL.revokeObjectURL(url)
      ElMessage.success('导出成功')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('[提现审核] 导出失败:', error)
      ElMessage.error('导出失败')
    }
  } finally {
    loading.value = false
  }
  */
}

// ============ 自动刷新 ============
// 开关态 + 最近刷新时间（P2）：切换开关才注册/清理定时器（§4.6）
const handleAutoRefreshChange = (enabled) => {
  if (autoRefreshTimer.value) {
    clearInterval(autoRefreshTimer.value)
    autoRefreshTimer.value = null
  }

  if (!enabled) {
    ElMessage.info('已停止自动刷新')
    return
  }

  autoRefreshTimer.value = setInterval(() => {
    fetchData()
    fetchStats()
  }, AUTO_REFRESH_INTERVAL)
  ElMessage.success(`已开启自动刷新（每${AUTO_REFRESH_INTERVAL / 1000}秒）`)
}

// ============ 生命周期 ============
const handleWindowResize = () => {
  chartInstance.value?.resize()
}

onMounted(async () => {
  await fetchData()
  await fetchStats()
  window.addEventListener('resize', handleWindowResize)
})

onUnmounted(() => {
  if (autoRefreshTimer.value) {
    clearInterval(autoRefreshTimer.value)
    autoRefreshTimer.value = null
  }
  window.removeEventListener('resize', handleWindowResize)
  chartResizeObserver?.disconnect()
  chartResizeObserver = null
  chartInstance.value?.dispose()
  chartInstance.value = null
})
</script>

<style scoped lang="less">
.withdrawal-audit-page {
  padding: var(--card-padding-compact) var(--page-gutter);
  background: var(--canvas);
  max-width: 1440px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;

  /* 页头动作区：自动刷新开关 + 最近刷新时间 + 导出（§4.7） */
  .refresh-toggle {
    display: inline-flex;
    align-items: center;
    gap: var(--space-2);
    cursor: pointer;
  }

  .refresh-toggle__text,
  .refresh-time {
    font-size: var(--text-body-sm);
    color: var(--ink-500);
    white-space: nowrap;
  }

  .refresh-time {
    font-family: var(--font-num);
    font-variant-numeric: tabular-nums;
  }

  .stats-section {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: var(--card-gap-compact);
    margin-bottom: var(--space-8);
  }

  /* 趋势图：次级可折叠区块（P0） */
  .chart-card {
    margin-bottom: var(--space-8);

    :deep(.el-collapse),
    :deep(.el-collapse-item__header),
    :deep(.el-collapse-item__wrap) {
      border: 0;
    }

    :deep(.el-collapse-item__header) {
      height: auto;
      padding: var(--space-1) 0;
      line-height: normal;
    }

    :deep(.el-collapse-item__content) {
      padding-bottom: 0;
    }

    .chart-title {
      font-size: var(--text-h3);
      font-weight: var(--weight-semibold);
      color: var(--ink-900);
    }

    .chart-toolbar {
      display: flex;
      justify-content: flex-end;
      margin-bottom: var(--space-3);
    }

    .chart-container {
      width: 100%;
      height: 280px;
    }
  }

  /* 筛选条（§4.3） */
  .filter-card {
    margin-bottom: var(--space-8);

    .search-form {
      margin-bottom: 0;
    }

    :deep(.el-form-item) {
      margin-right: var(--space-4);
      margin-bottom: var(--space-3);
    }

    .filter-keyword {
      width: 220px;
    }

    .filter-select {
      width: 140px;
    }

    .filter-date {
      width: 260px;
    }

    .filter-amount {
      display: inline-flex;
      align-items: center;
      gap: var(--space-2);
      margin-right: 0;
    }

    .filter-number {
      width: 140px;
    }

    .filter-label {
      font-size: var(--text-body-sm);
      color: var(--ink-500);
      white-space: nowrap;
    }

    .range-divider {
      color: var(--ink-400);
    }
  }

  /* 表格（§4.3、§5.4） */
  .table-card {
    .pagination-container {
      margin-top: var(--space-5);
      display: flex;
      justify-content: flex-end;
    }

    /* 长表滚动时表头可辨：浅底色 + 中量字重（§4.3） */
    :deep(.el-table__header th) {
      background-color: var(--canvas-sunken);
      font-weight: var(--weight-semibold);
    }

    /* 财务长表行 hover 高亮，避免横向读串行（§3.5 高频微交互用 CSS transition） */
    :deep(.el-table__body tr:hover > td) {
      background-color: var(--canvas-sunken);
      transition: background-color var(--duration-fast) var(--ease-out);
    }
  }

  .cell-num {
    font-family: var(--font-num);
    font-variant-numeric: tabular-nums;
    white-space: nowrap;
  }

  .copy-text {
    display: inline-flex;
    align-items: center;
    gap: var(--space-1);
    color: var(--brand-600);
    cursor: pointer;

    &:hover {
      color: var(--brand-500);
      text-decoration: underline;
    }
  }

  .user-cell {
    display: flex;
    align-items: center;
    gap: var(--space-2);
    min-width: 0;

    .user-info {
      min-width: 0;

      .user-nickname {
        font-size: var(--text-body-sm);
        color: var(--ink-700);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .user-id {
        font-family: var(--font-num);
        font-variant-numeric: tabular-nums;
        font-size: var(--text-caption);
        color: var(--ink-500);
      }
    }
  }

  /* 金额：无衬线等宽数字 + 中性深色，不带红绿蓝（§4.3、§5.2） */
  .amount-text {
    color: var(--ink-900);
    font-family: var(--font-num);
    font-variant-numeric: tabular-nums;
    font-weight: var(--weight-semibold);
    white-space: nowrap;

    /* 大额只加粗一档，不用颜色表达（§5.2） */
    &.high-amount {
      font-weight: var(--weight-bold);
    }
  }

  .expand-content {
    padding: var(--space-4);
    background: var(--canvas-sunken);
  }

  .batch-actions {
    margin-top: var(--space-4);

    :deep(.el-alert__content) {
      display: flex;
      align-items: center;
      gap: var(--space-2);
      flex-wrap: wrap;
    }
  }

  /* 详情对话框 */
  .detail-content {
    .copy-item {
      display: flex;
      align-items: center;
      gap: var(--space-2);
      font-family: var(--font-num);
      font-variant-numeric: tabular-nums;
    }

    .user-detail {
      display: flex;
      align-items: center;
      gap: var(--space-3);

      .user-detail-info {
        .user-nickname {
          font-size: var(--text-body);
          color: var(--ink-900);
          font-weight: var(--weight-medium);
        }

        .user-id {
          font-family: var(--font-num);
          font-variant-numeric: tabular-nums;
          font-size: var(--text-body-sm);
          color: var(--ink-500);
        }
      }
    }

    .logs-section {
      margin-top: var(--space-6);
      padding-top: var(--space-4);
      border-top: 1px solid var(--line);

      .logs-title {
        font-size: var(--text-body);
        font-weight: var(--weight-semibold);
        color: var(--ink-900);
        margin-bottom: var(--space-3);
      }

      .log-item {
        .log-action {
          font-weight: var(--weight-medium);
          color: var(--ink-900);
        }

        .log-user {
          margin-left: var(--space-2);
          color: var(--brand-600);
        }

        .log-comment {
          color: var(--ink-700);
        }
      }
    }
  }

  .error-text {
    color: var(--danger);
  }

  :deep(.el-dialog) {
    border-radius: var(--radius-lg);
  }
}
</style>
