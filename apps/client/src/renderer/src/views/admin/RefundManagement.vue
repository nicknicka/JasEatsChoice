<template>
  <div class="refund-management-container">
    <AppPageHeader title="退款管理" subtitle="处理用户退款申请" :compact="true" />

    <div class="stats-section">
      <AppStatCard label="待处理" :value="stats.pending" icon="Clock" />
      <AppStatCard label="今日已退款" :value="formatMoney(stats.todayApproved, true)" icon="Money" />
      <AppStatCard label="今日已拒绝" :value="stats.todayRejected" icon="CircleClose" />
      <AppStatCard label="本月退款总额" :value="formatMoney(stats.monthTotal, true)" icon="Wallet" />
    </div>

    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索用户名、订单号"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="退款状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待处理" value="PENDING" />
            <el-option label="已批准" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="退款中" value="PROCESSING" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请日期">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            clearable
            style="width: 280px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <AppEmptyState
        v-if="!loading && refundList.length === 0"
        compact
        icon="Tickets"
        description="没有符合条件的退款记录，换个关键词或清空筛选试试"
        action-text="重置筛选"
        @action="handleReset"
      />

      <template v-else>
        <el-table :data="refundList" v-loading="loading" stripe>
          <el-table-column label="退款ID" width="180" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="cell-id">{{ row.refundId }}</span>
            </template>
          </el-table-column>
          <el-table-column label="订单ID" width="125">
            <template #default="{ row }">
              <span class="cell-num">{{ row.orderId }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="username" label="用户名" width="120" show-overflow-tooltip />
          <el-table-column label="退款金额" width="95" align="right">
            <template #default="{ row }">
              <span class="amount">¥{{ formatMoney(row.refundAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="退款原因" min-width="130" show-overflow-tooltip />
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <StatusTag :status="getStatusTagKey(row.status)" :text="getStatusText(row.status)" />
            </template>
          </el-table-column>
          <el-table-column label="申请时间" width="160" align="center">
            <template #default="{ row }">
              <span class="cell-date">{{ formatDateTime(row.applyTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="135" fixed="right" align="center">
            <template #default="{ row }">
              <RowActions
                :primary="{ text: '详情', onClick: () => handleView(row) }"
                :items="rowActions(row)"
              />
            </template>
          </el-table-column>
        </el-table>

        <div v-if="refundList.length > 0" class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="fetchRefundList"
            @current-change="fetchRefundList"
          />
        </div>
      </template>
    </el-card>

    <el-dialog
      v-model="processDialogVisible"
      title="处理退款申请"
      width="700px"
      :close-on-click-modal="false"
    >
      <div v-if="currentRefund" class="process-content">
        <el-descriptions :column="2" border class="refund-info">
          <el-descriptions-item label="退款ID">{{ currentRefund.refundId }}</el-descriptions-item>
          <el-descriptions-item label="订单ID">{{ currentRefund.orderId }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentRefund.username }}</el-descriptions-item>
          <el-descriptions-item label="退款金额">
            <span class="amount amount--lg">¥{{ formatMoney(currentRefund.refundAmount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="退款原因" :span="2">{{ currentRefund.reason }}</el-descriptions-item>
          <el-descriptions-item label="申请说明" :span="2">{{ currentRefund.reasonDescription || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请时间" :span="2">{{ formatDateTime(currentRefund.applyTime) }}</el-descriptions-item>
        </el-descriptions>

        <el-divider>处理操作</el-divider>
        <el-form :model="processForm" label-width="80px">
          <el-form-item label="处理结果">
            <el-radio-group v-model="processForm.decision">
              <el-radio label="APPROVE">批准退款</el-radio>
              <el-radio label="REJECT">拒绝退款</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="处理意见">
            <el-input
              v-model="processForm.comment"
              type="textarea"
              :rows="4"
              placeholder="请输入处理意见（拒绝时必填）"
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProcess">提交处理</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="detailDialogVisible"
      title="退款详情"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-descriptions v-if="currentRefund" :column="2" border>
        <el-descriptions-item label="退款ID">{{ currentRefund.refundId }}</el-descriptions-item>
        <el-descriptions-item label="订单ID">{{ currentRefund.orderId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentRefund.username }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">
          <span class="amount">¥{{ formatMoney(currentRefund.refundAmount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <StatusTag :status="getStatusTagKey(currentRefund.status)" :text="getStatusText(currentRefund.status)" />
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ formatDateTime(currentRefund.applyTime) }}</el-descriptions-item>
        <el-descriptions-item label="处理时间" :span="2">{{ formatDateTime(currentRefund.processTime) }}</el-descriptions-item>
        <el-descriptions-item label="退款原因" :span="2">{{ currentRefund.reason }}</el-descriptions-item>
        <el-descriptions-item label="申请说明" :span="2">{{ currentRefund.reasonDescription || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理意见" :span="2">{{ currentRefund.processComment || '-' }}</el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="currentRefund?.status === 'PENDING'"
          type="primary"
          @click="detailDialogVisible = false; handleProcess(currentRefund)"
        >
          去处理
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import AppStatCard from '@/components/common/AppStatCard.vue'
import AppEmptyState from '@/components/common/AppEmptyState.vue'
import RowActions from '@/components/common/RowActions.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { getRefundList, getRefundStats, getRefundDetail, processRefund } from '@/api/admin'

const loading = ref(false)
const refundList = ref([])
const currentRefund = ref(null)
const processDialogVisible = ref(false)
const detailDialogVisible = ref(false)

const stats = reactive({
  pending: 0,
  todayApproved: 0,
  todayRejected: 0,
  monthTotal: 0
})

const searchForm = reactive({
  keyword: '',
  status: '',
  dateRange: null
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const processForm = reactive({
  decision: 'APPROVE',
  comment: ''
})

// 状态统一映射到 StatusTag 词典：橙=待处理/待审核、蓝=处理中、绿=已通过/完成、红=已拒绝/失败
const STATUS_TAG_KEYS = Object.freeze({
  PENDING: 'pending',
  APPROVED: 'approved',
  PROCESSING: 'processing',
  REJECTED: 'rejected',
  COMPLETED: 'completed',
  SUCCESS: 'completed',
  FAILED: 'failed'
})

const getStatusTagKey = (status) => STATUS_TAG_KEYS[status] || 'unknown'

const getStatusText = (status) => {
  const texts = {
    PENDING: '待处理',
    APPROVED: '已批准',
    PROCESSING: '退款中',
    REJECTED: '已拒绝',
    COMPLETED: '已完成',
    SUCCESS: '已完成',
    FAILED: '退款失败'
  }
  return texts[status] || '未知'
}

const formatMoney = (value, withSymbol = false) => {
  const amount = Number(value || 0).toFixed(2)
  return withSymbol ? `¥${amount}` : amount
}

const formatDateTime = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const fetchRefundStats = async () => {
  try {
    const response = await getRefundStats()
    if (response) {
      stats.pending = response.pending || 0
      stats.todayApproved = response.todayApproved || 0
      stats.todayRejected = response.todayRejected || 0
      stats.monthTotal = response.monthTotal || 0
    }
  } catch (error) {
    console.error('获取退款统计失败:', error)
  }
}

const fetchRefundList = async () => {
  loading.value = true
  try {
    const response = await getRefundList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      status: searchForm.status
    })

    if (response) {
      refundList.value = response.records || []
      pagination.total = response.total ?? 0
    }
  } catch (error) {
    console.error('获取退款列表失败:', error)
    ElMessage.error('获取退款列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchRefundList()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  searchForm.dateRange = null
  pagination.page = 1
  fetchRefundList()
}

const rowActions = (row) => {
  if (row.status !== 'PENDING') return []
  return [{ text: '处理', onClick: () => handleProcess(row) }]
}

const handleView = async (row) => {
  try {
    const response = await getRefundDetail(row.refundId)
    const detail = response?.data || response
    if (detail?.refundId) {
      currentRefund.value = detail
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取退款详情失败:', error)
    ElMessage.error('获取退款详情失败')
  }
}

const handleProcess = async (row) => {
  try {
    const response = await getRefundDetail(row.refundId)
    const detail = response?.data || response
    if (detail?.refundId) {
      currentRefund.value = detail
      processForm.decision = 'APPROVE'
      processForm.comment = ''
      processDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取退款信息失败:', error)
    ElMessage.error('获取退款信息失败')
  }
}

const submitProcess = async () => {
  if (processForm.decision === 'REJECT' && !processForm.comment.trim()) {
    ElMessage.warning('拒绝时必须填写处理意见')
    return
  }

  try {
    const response = await processRefund(currentRefund.value.refundId, {
      decision: processForm.decision,
      comment: processForm.comment
    })

    if (response) {
      ElMessage.success('处理提交成功')
      processDialogVisible.value = false
      await fetchRefundList()
      await fetchRefundStats()
    }
  } catch (error) {
    console.error('处理提交失败:', error)
    ElMessage.error('处理提交失败')
  }
}

onMounted(() => {
  fetchRefundList()
  fetchRefundStats()
})
</script>

<style scoped lang="less">
.refund-management-container {
  padding: var(--card-padding-compact) var(--page-gutter);
  background: var(--canvas);
  max-width: 1440px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;

  .stats-section {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: var(--card-gap-compact);
    margin-bottom: var(--space-8);
  }

  .search-card {
    margin-bottom: var(--space-8);

    .search-form {
      margin-bottom: 0;

      :deep(.el-form-item) {
        margin-right: var(--space-4);
        margin-bottom: var(--space-2);
      }
    }
  }

  .table-card {
    .pagination-container {
      display: flex;
      justify-content: flex-end;
      margin-top: var(--space-5);
      padding-top: var(--space-4);
      border-top: 1px solid var(--line);
    }

    :deep(.el-table__header th) {
      background-color: var(--canvas-sunken);
      font-weight: var(--weight-semibold);
    }

    :deep(.el-table__row) {
      height: 44px;
    }

    :deep(.el-table__body tr:hover > td) {
      background-color: var(--canvas-sunken);
      transition: background-color var(--duration-fast) var(--ease-out);
    }
  }

  .cell-id,
  .cell-num,
  .cell-date {
    font-family: var(--font-num);
    font-variant-numeric: tabular-nums;
    white-space: nowrap;
  }

  .amount {
    color: var(--ink-900);
    font-family: var(--font-num);
    font-variant-numeric: tabular-nums;
    font-weight: var(--weight-semibold);
    white-space: nowrap;
  }

  .amount--lg {
    font-size: var(--text-h3);
  }

  .refund-info {
    margin-bottom: var(--space-5);
  }

  :deep(.el-dialog) {
    border-radius: var(--radius-md);
  }

  @media (max-width: 960px) {
    .stats-section {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media (max-width: 640px) {
    padding: var(--space-3);

    .stats-section {
      grid-template-columns: 1fr;
    }

    .search-form {
      :deep(.el-form-item) {
        margin-right: 0;
      }
    }
  }
}
</style>
