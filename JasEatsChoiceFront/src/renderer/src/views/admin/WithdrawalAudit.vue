<template>
  <div class="withdrawal-audit-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>提现审核</h2>
          <div class="header-actions">
            <el-button
              :type="autoRefresh ? 'success' : 'default'"
              :icon="autoRefresh ? VideoPause : VideoPlay"
              @click="toggleAutoRefresh"
            >
              {{ autoRefresh ? '停止刷新' : '自动刷新' }}
            </el-button>
            <el-button :icon="Download" @click="handleExport">导出</el-button>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="16" class="stats-row">
        <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <div class="stat-icon" style="background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);">
                <el-icon :size="24" color="#fa8c16"><Clock /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">待审核</div>
                <div class="stat-value">{{ stats.pending }}</div>
                <div v-if="stats.pendingTrend !== 0" class="stat-trend" :class="stats.pendingTrend > 0 ? 'up' : 'down'">
                  <el-icon><CaretTop v-if="stats.pendingTrend > 0" /><CaretBottom v-else /></el-icon>
                  {{ Math.abs(stats.pendingTrend) }}%
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <div class="stat-icon" style="background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);">
                <el-icon :size="24" color="#1890ff"><Loading /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">处理中</div>
                <div class="stat-value">{{ stats.processing }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <div class="stat-icon" style="background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);">
                <el-icon :size="24" color="#52c41a"><Money /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">今日提现</div>
                <div class="stat-value">¥{{ formatAmount(stats.todayAmount) }}</div>
                <div v-if="stats.todayTrend !== 0" class="stat-trend" :class="stats.todayTrend > 0 ? 'up' : 'down'">
                  <el-icon><CaretTop v-if="stats.todayTrend > 0" /><CaretBottom v-else /></el-icon>
                  {{ Math.abs(stats.todayTrend) }}%
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <div class="stat-icon" style="background: linear-gradient(135deg, #fff0f6 0%, #ffd6e7 100%);">
                <el-icon :size="24" color="#eb2f96"><Wallet /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">累计提现</div>
                <div class="stat-value">¥{{ formatAmount(stats.totalAmount) }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 趋势图表 -->
      <el-card shadow="never" class="chart-card">
        <div class="chart-header">
          <span class="chart-title">提现趋势</span>
          <el-radio-group v-model="chartPeriod" size="small" @change="handleChartPeriodChange">
            <el-radio-button label="week">近7天</el-radio-button>
            <el-radio-button label="month">近30天</el-radio-button>
          </el-radio-group>
        </div>
        <div ref="chartRef" class="chart-container"></div>
      </el-card>

      <!-- 高级搜索表单 -->
      <div class="filter-section">
        <el-row :gutter="12" class="filter-row">
          <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
            <el-input
              v-model="searchForm.keyword"
              placeholder="流水号/用户ID/用户昵称"
              clearable
              @clear="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :xs="12" :sm="12" :md="4" :lg="4" :xl="4">
            <el-select
              v-model="searchForm.status"
              placeholder="状态"
              clearable
              @change="handleSearch"
              style="width: 100%"
            >
              <el-option
                v-for="item in STATUS_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-col>
          <el-col :xs="12" :sm="12" :md="4" :lg="4" :xl="4">
            <el-select
              v-model="searchForm.withdrawMethod"
              placeholder="提现方式"
              clearable
              @change="handleSearch"
              style="width: 100%"
            >
              <el-option
                v-for="item in METHOD_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              @change="handleSearch"
              style="width: 100%"
            />
          </el-col>
          <el-col :xs="24" :sm="24" :md="4" :lg="4" :xl="4">
            <div class="filter-buttons-inline">
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon> 搜索
              </el-button>
              <el-button @click="handleReset">
                <el-icon><RefreshLeft /></el-icon> 重置
              </el-button>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="12" class="filter-row-second">
          <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <div class="amount-range-filter">
              <span class="filter-label">金额范围：</span>
              <el-input-number
                v-model="searchForm.minAmount"
                :min="0"
                :precision="2"
                placeholder="最小金额"
                controls-position="right"
              />
              <span class="range-divider">-</span>
              <el-input-number
                v-model="searchForm.maxAmount"
                :min="0"
                :precision="2"
                placeholder="最大金额"
                controls-position="right"
              />
              <el-button type="primary" link @click="handleSearch">应用</el-button>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 数据表格 -->
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        @selection-change="handleSelectionChange"
        @row-dblclick="handleView"
        class="data-table"
      >
        <el-table-column type="selection" width="55" fixed="left" />
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand-content">
              <el-descriptions :column="3" border>
                <el-descriptions-item label="流水号" :span="3">{{ row.withdrawNo }}</el-descriptions-item>
                <el-descriptions-item label="用户昵称">{{ row.nickname || '-' }}</el-descriptions-item>
                <el-descriptions-item label="用户ID">{{ row.userId }}</el-descriptions-item>
                <el-descriptions-item label="提现方式">
                  <el-tag :type="getMethodTagType(row.withdrawMethod)">
                    {{ getMethodText(row.withdrawMethod) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="提现金额">
                  <span class="amount-text">¥{{ row.amount }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="手续费">¥{{ row.fee || 0 }}</el-descriptions-item>
                <el-descriptions-item label="实际到账">
                  <span class="amount-text highlight">¥{{ row.actualAmount || row.amount }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="提现账号" :span="3">{{ row.accountInfo || '-' }}</el-descriptions-item>
                <el-descriptions-item label="申请时间" :span="3">{{ row.createTime }}</el-descriptions-item>
                <el-descriptions-item v-if="row.auditor" label="审核人">{{ row.auditor }}</el-descriptions-item>
                <el-descriptions-item v-if="row.auditTime" label="审核时间">{{ row.auditTime }}</el-descriptions-item>
                <el-descriptions-item v-if="row.rejectReason" label="拒绝原因" :span="3">
                  <span class="error-text">{{ row.rejectReason }}</span>
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="withdrawNo" label="流水号" width="180" fixed="left">
          <template #default="{ row }">
            <el-tooltip content="点击复制" placement="top">
              <span class="copy-text" @click="copyText(row.withdrawNo)">
                {{ row.withdrawNo }}
                <el-icon><DocumentCopy /></el-icon>
              </span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="用户" width="180">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar v-if="row.avatar" :src="row.avatar" :size="32" />
              <el-avatar v-else :size="32">
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="user-info">
                <div class="user-nickname">{{ row.nickname || '-' }}</div>
                <div class="user-id">ID: {{ row.userId }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="提现金额" width="120" sortable prop="amount">
          <template #default="{ row }">
            <span :class="['amount-text', { 'high-amount': row.amount >= 10000 }]">
              ¥{{ formatAmount(row.amount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="提现方式" width="100">
          <template #default="{ row }">
            <el-tag :type="getMethodTagType(row.withdrawMethod)">
              {{ getMethodText(row.withdrawMethod) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.withdrawStatus)">
              {{ getStatusText(row.withdrawStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="180" sortable />
        <el-table-column v-if="showAuditInfo" label="审核人" width="120">
          <template #default="{ row }">
            {{ row.auditor || '-' }}
          </template>
        </el-table-column>
        <el-table-column v-if="showAuditInfo" prop="auditTime" label="审核时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" :icon="View" @click="handleView(row)">
              查看
            </el-button>
            <el-button
              v-if="row.withdrawStatus === 'pending'"
              type="success"
              link
              size="small"
              :icon="CircleCheck"
              @click="handleAudit(row, 'APPROVE')"
            >
              通过
            </el-button>
            <el-button
              v-if="row.withdrawStatus === 'pending'"
              type="danger"
              link
              size="small"
              :icon="CircleClose"
              @click="handleAudit(row, 'REJECT')"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <el-empty v-if="!loading && tableData.length === 0" description="暂无提现记录" />

      <!-- 批量操作按钮 -->
      <div v-if="selectedRows.length > 0" class="batch-actions">
        <el-alert
          :title="`已选择 ${selectedRows.length} 条记录`"
          type="info"
          :closable="false"
        >
          <el-button size="small" type="success" @click="handleBatchApprove">批量通过</el-button>
          <el-button size="small" type="danger" @click="handleBatchReject">批量拒绝</el-button>
          <el-button size="small" @click="selectedRows = []">取消选择</el-button>
        </el-alert>
      </div>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
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
                <div class="user-id">ID: {{ currentRow.userId }}</div>
              </div>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="提现金额">
            <span class="amount-text">¥{{ currentRow.amount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="手续费">¥{{ currentRow.fee || 0 }}</el-descriptions-item>
          <el-descriptions-item label="实际到账" :span="2">
            <span class="amount-text highlight">¥{{ currentRow.actualAmount || currentRow.amount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="提现方式" :span="2">
            <el-tag :type="getMethodTagType(currentRow.withdrawMethod)">
              {{ getMethodText(currentRow.withdrawMethod) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提现账号" :span="2">
            {{ currentRow.accountInfo || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentRow.withdrawStatus)">
              {{ getStatusText(currentRow.withdrawStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ currentRow.createTime }}</el-descriptions-item>
          <el-descriptions-item v-if="currentRow.auditor" label="审核人">
            {{ currentRow.auditor }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentRow.auditTime" label="审核时间">
            {{ currentRow.auditTime }}
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
              :timestamp="log.time"
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
          type="success"
          :icon="CircleCheck"
          @click="handleAudit(currentRow, 'APPROVE')"
        >
          审核通过
        </el-button>
        <el-button
          v-if="currentRow && currentRow.withdrawStatus === 'pending'"
          type="danger"
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
              <span :class="{ 'high-amount': currentRow?.amount >= 10000 }">
                {{ currentRow?.amount >= 10000 ? '⚠️ 大额' : '' }}
              </span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="快速原因" v-if="auditDecision === 'REJECT'">
          <el-select
            v-model="auditForm.quickReason"
            placeholder="选择快速原因"
            @change="handleQuickReason"
            clearable
            style="width: 100%"
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
          :type="auditDecision === 'APPROVE' ? 'success' : 'danger'"
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
        <el-form-item label="快速原因" v-if="batchAuditDecision === 'REJECT'">
          <el-select
            v-model="batchAuditForm.quickReason"
            placeholder="选择快速原因"
            @change="handleBatchQuickReason"
            clearable
            style="width: 100%"
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
          :type="batchAuditDecision === 'APPROVE' ? 'success' : 'danger'"
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
import {
  Clock, Loading, Money, Wallet, Search, RefreshLeft, Download,
  View, CircleCheck, CircleClose, DocumentCopy, User,
  VideoPlay, VideoPause, CaretTop, CaretBottom
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import {
  getWithdrawList,
  getWithdrawDetail,
  processWithdraw,
  batchProcessWithdraw,
  getWithdrawStatistics
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

const STATUS_TYPE_MAP = {
  'pending': 'warning',
  'approved': 'primary',
  'rejected': 'danger',
  'processing': 'info',
  'success': 'success',
  'failed': 'danger'
}

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

const METHOD_TAG_TYPE_MAP = {
  'wechat': 'success',
  'alipay': 'primary',
  'bank': 'info'
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
const chartPeriod = ref('week')

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

const getStatusType = (status) => STATUS_TYPE_MAP[status] || 'info'

const getStatusText = (status) => STATUS_TEXT_MAP[status] || '未知'

const getMethodText = (method) => METHOD_TEXT_MAP[method] || '未知'

const getMethodTagType = (method) => METHOD_TAG_TYPE_MAP[method] || 'info'

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
      pagination.total = response.total || 0
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
const initChart = () => {
  if (!chartRef.value) return

  chartInstance.value = echarts.init(chartRef.value)

  // 监听图例切换事件，同步控制 Y 轴显示/隐藏
  chartInstance.value.on('legendselectchanged', (params) => {
    const option = chartInstance.value.getOption()

    // 根据图例选中状态，同步控制对应的 Y 轴
    if (params.name === '提现金额') {
      option.yAxis[0].show = params.selected['提现金额']
    } else if (params.name === '提现笔数') {
      option.yAxis[1].show = params.selected['提现笔数']
    }

    chartInstance.value.setOption(option)
  })

  updateChart()
}

const updateChart = async () => {
  if (!chartInstance.value) return

  // 模拟图表数据，实际应从后端获取
  const days = chartPeriod.value === 'week' ? 7 : 30
  const dates = []
  const amounts = []
  const counts = []

  for (let i = days - 1; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    dates.push(date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' }))
    amounts.push(Math.random() * 10000 + 5000)
    counts.push(Math.floor(Math.random() * 50 + 10))
  }

  const option = {
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
        }
      }
    ],
    series: [
      {
        name: '提现金额',
        type: 'line',
        smooth: true,
        data: amounts,
        itemStyle: {
          color: '#67C23A'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
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
          color: '#409EFF'
        }
      }
    ]
  }

  chartInstance.value.setOption(option, true)
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
const toggleAutoRefresh = () => {
  autoRefresh.value = !autoRefresh.value
  if (autoRefresh.value) {
    autoRefreshTimer.value = setInterval(() => {
      fetchData()
      fetchStats()
    }, AUTO_REFRESH_INTERVAL)
    ElMessage.success(`已开启自动刷新（每${AUTO_REFRESH_INTERVAL / 1000}秒）`)
  } else {
    if (autoRefreshTimer.value) {
      clearInterval(autoRefreshTimer.value)
      autoRefreshTimer.value = null
    }
    ElMessage.info('已停止自动刷新')
  }
}

// ============ 生命周期 ============
onMounted(async () => {
  await fetchData()
  await fetchStats()

  nextTick(() => {
    initChart()
  })

  window.addEventListener('resize', () => {
    chartInstance.value?.resize()
  })
})

onUnmounted(() => {
  if (autoRefreshTimer.value) {
    clearInterval(autoRefreshTimer.value)
  }
  chartInstance.value?.dispose()
})
</script>

<style scoped lang="less">
.withdrawal-audit-page {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    h2 {
      margin: 0;
      font-size: 18px;
      color: #303133;
    }

    .header-actions {
      display: flex;
      gap: 10px;
    }
  }

  // 统计卡片
  .stats-row {
    margin-bottom: 20px;

    .stat-card {
      transition: all 0.3s ease;
      cursor: pointer;
      height: 100%;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
      }

      :deep(.el-card__body) {
        padding: 20px;
      }

      .stat-item {
        display: flex;
        align-items: center;
        gap: 16px;

        .stat-icon {
          width: 56px;
          height: 56px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
          transition: transform 0.3s ease;
        }

        &:hover .stat-icon {
          transform: scale(1.1) rotate(5deg);
        }

        .stat-content {
          flex: 1;
          display: flex;
          flex-direction: column;
          gap: 8px;

          .stat-label {
            font-size: 14px;
            color: #909399;
            font-weight: 500;
          }

          .stat-value {
            font-size: 32px;
            font-weight: 700;
            color: #303133;
            line-height: 1;
          }

          .stat-trend {
            font-size: 12px;
            display: flex;
            align-items: center;
            gap: 2px;

            &.up {
              color: #f56c6c;
            }

            &.down {
              color: #67c23a;
            }
          }
        }
      }
    }
  }

  // 图表卡片
  .chart-card {
    margin-bottom: 20px;
    border: none;
    box-shadow: none;

    .chart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      .chart-title {
        font-size: 16px;
        font-weight: bold;
        color: #303133;
      }
    }

    .chart-container {
      width: 100%;
      height: 280px;
    }
  }

  // 筛选区域
  .filter-section {
    margin-bottom: 20px;
    padding: 16px;
    background: #fafafa;
    border-radius: 8px;
    border: 1px solid #ebeef5;

    .filter-row {
      margin-bottom: 12px;

      &:last-child {
        margin-bottom: 0;
      }
    }

    .filter-row-second {
      margin-top: 8px;
    }

    .filter-buttons-inline {
      display: flex;
      gap: 8px;
      align-items: center;
      height: 32px;

      .el-button {
        flex: 1;
      }
    }

    .amount-range-filter {
      display: flex;
      align-items: center;
      gap: 8px;

      .filter-label {
        font-size: 14px;
        color: #606266;
        white-space: nowrap;
      }

      .range-divider {
        color: #909399;
      }
    }
  }

  // 数据表格
  :deep(.el-table) {
    border-radius: 8px;
    overflow: hidden;

    .el-table__header th {
      background-color: #fafafa;
      font-weight: 600;
    }
  }

  .data-table {
    border-radius: 8px;
    overflow: hidden;

    .copy-text {
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 4px;
      color: #409eff;

      &:hover {
        text-decoration: underline;
      }
    }

    .user-cell {
      display: flex;
      align-items: center;
      gap: 8px;

      .user-info {
        .user-nickname {
          font-size: 14px;
          color: #303133;
        }

        .user-id {
          font-size: 12px;
          color: #909399;
        }
      }
    }

    .amount-text {
      color: #f56c6c;
      font-weight: bold;

      &.highlight {
        font-size: 16px;
      }

      &.high-amount {
        color: #ff4d4f;
        font-size: 15px;
      }
    }

    .expand-content {
      padding: 20px;
      background: #fafafa;
    }
  }

  // 批量操作
  .batch-actions {
    margin-bottom: 16px;

    :deep(.el-alert) {
      border-radius: 8px;
    }

    :deep(.el-alert__content) {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
    }
  }

  // 分页
  .el-pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
    padding-top: 16px;
    border-top: 1px solid #ebeef5;
  }

  // 详情对话框
  .detail-content {
    .copy-item {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .user-detail {
      display: flex;
      align-items: center;
      gap: 12px;

      .user-detail-info {
        .user-nickname {
          font-size: 14px;
          color: #303133;
          font-weight: 500;
        }

        .user-id {
          font-size: 12px;
          color: #909399;
        }
      }
    }

    .logs-section {
      margin-top: 24px;
      padding-top: 16px;
      border-top: 1px solid #ebeef5;

      .logs-title {
        font-size: 14px;
        font-weight: bold;
        color: #303133;
        margin-bottom: 12px;
      }

      .log-item {
        .log-action {
          font-weight: 500;
          color: #303133;
        }

        .log-user {
          margin-left: 8px;
          color: #409eff;
        }

        .log-comment {
          color: #606266;
        }
      }
    }
  }

  .error-text {
    color: #f56c6c;
  }

  .high-amount {
    color: #ff4d4f;
    font-weight: bold;
  }

  // 空状态
  .el-empty {
    padding: 40px 0;
  }

  // 对话框圆角
  :deep(.el-dialog) {
    border-radius: 8px;
  }

  :deep(.el-dialog__body) {
    padding: 24px;
  }

  // 响应式优化
  @media (max-width: 1200px) {
    .stats-row {
      :deep(.el-col) {
        margin-bottom: 12px;
      }
    }

    .filter-section {
      .filter-row {
        > [class*="el-col-"] {
          margin-bottom: 12px;
        }
      }
    }
  }

  @media (max-width: 768px) {
    padding: 12px;

    .stats-row {
      :deep(.el-col) {
        margin-bottom: 12px;
      }
    }

    .card-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;

      .header-actions {
        width: 100%;

        .el-button {
          flex: 1;
        }
      }
    }
  }
}
</style>
