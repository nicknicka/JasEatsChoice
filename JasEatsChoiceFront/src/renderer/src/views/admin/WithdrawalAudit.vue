<template>
  <div class="withdrawal-audit-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>提现审核</h1>
      <p class="subtitle">管理用户提现申请</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card">
        <el-statistic title="待审核" :value="stats.pendingCount">
          <template #prefix>
            <el-icon style="vertical-align: -0.125em"><Clock /></el-icon>
          </template>
        </el-statistic>
      </el-card>
      <el-card class="stat-card">
        <el-statistic title="处理中" :value="stats.processingCount">
          <template #prefix>
            <el-icon style="vertical-align: -0.125em"><Loading /></el-icon>
          </template>
        </el-statistic>
      </el-card>
      <el-card class="stat-card">
        <el-statistic title="今日提现" :value="stats.todayWithdraw" :precision="2" prefix="¥">
          <template #prefix>
            <el-icon style="vertical-align: -0.125em"><Money /></el-icon>
          </template>
        </el-statistic>
      </el-card>
      <el-card class="stat-card">
        <el-statistic title="累计提现" :value="stats.totalWithdraw" :precision="2" prefix="¥">
          <template #prefix>
            <el-icon style="vertical-align: -0.125em"><WalletFilled /></el-icon>
          </template>
        </el-statistic>
      </el-card>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索流水号、用户ID"
            clearable
            style="width: 250px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已拒绝" value="rejected" />
            <el-option label="处理中" value="processing" />
            <el-option label="成功" value="success" />
            <el-option label="失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 提现记录列表 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>提现记录</span>
          <div v-if="selectedRows.length > 0" class="batch-actions">
            <el-button size="small" type="success" @click="handleBatchApprove">
              批量通过 ({{ selectedRows.length }})
            </el-button>
            <el-button size="small" type="danger" @click="handleBatchReject">
              批量拒绝 ({{ selectedRows.length }})
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="withdrawList"
        v-loading="loading"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" :selectable="(row) => row.withdrawStatus === 'pending'" />
        <el-table-column prop="withdrawNo" label="流水号" width="180" />
        <el-table-column prop="userId" label="用户ID" width="150" />
        <el-table-column label="提现金额" width="120">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="实际到账" width="120">
          <template #default="{ row }">
            <span style="color: #67c23a; font-weight: bold">¥{{ row.actualAmount || row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="withdrawMethod" label="提现方式" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getMethodType(row.withdrawMethod)">
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
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleView(row)">查看</el-button>
            <el-button
              v-if="row.withdrawStatus === 'pending'"
              type="success"
              size="small"
              link
              @click="handleAudit(row, 'APPROVE')"
            >
              通过
            </el-button>
            <el-button
              v-if="row.withdrawStatus === 'pending'"
              type="danger"
              size="small"
              link
              @click="handleAudit(row, 'REJECT')"
            >
              拒绝
            </el-button>
            <el-button
              v-if="row.withdrawStatus === 'processing'"
              type="success"
              size="small"
              link
              @click="handleComplete(row)"
            >
              完成
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchWithdrawList"
          @current-change="fetchWithdrawList"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="提现详情"
      width="700px"
      :close-on-click-modal="false"
    >
      <div v-if="currentWithdraw" class="withdraw-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="流水号" :span="2">{{ currentWithdraw.withdrawNo }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ currentWithdraw.userId }}</el-descriptions-item>
          <el-descriptions-item label="提现方式">
            <el-tag :type="getMethodType(currentWithdraw.withdrawMethod)" size="small">
              {{ getMethodText(currentWithdraw.withdrawMethod) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提现金额">
            <span style="color: #f56c6c; font-weight: bold">¥{{ currentWithdraw.amount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="手续费">
            <span style="color: #909399">¥{{ currentWithdraw.fee || 0 }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="实际到账" :span="2">
            <span style="color: #67c23a; font-weight: bold">¥{{ currentWithdraw.actualAmount || currentWithdraw.amount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="提现账号" :span="2">
            {{ currentWithdraw.accountInfo || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentWithdraw.withdrawStatus)">
              {{ getStatusText(currentWithdraw.withdrawStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ currentWithdraw.createTime }}</el-descriptions-item>
          <el-descriptions-item v-if="currentWithdraw.auditTime" label="审核时间">
            {{ currentWithdraw.auditTime }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentWithdraw.auditUser" label="审核人">
            {{ currentWithdraw.auditUser }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentWithdraw.rejectReason" label="拒绝原因" :span="2">
            <span style="color: #f56c6c">{{ currentWithdraw.rejectReason }}</span>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentWithdraw.remark" label="备注" :span="2">
            {{ currentWithdraw.remark }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="currentWithdraw?.withdrawStatus === 'pending'"
          type="success"
          @click="handleAudit(currentWithdraw, 'APPROVE')"
        >
          审核通过
        </el-button>
        <el-button
          v-if="currentWithdraw?.withdrawStatus === 'pending'"
          type="danger"
          @click="handleAudit(currentWithdraw, 'REJECT')"
        >
          审核拒绝
        </el-button>
        <el-button
          v-if="currentWithdraw?.withdrawStatus === 'processing'"
          type="success"
          @click="handleComplete(currentWithdraw)"
        >
          完成提现
        </el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      :title="auditForm.decision === 'APPROVE' ? '审核通过' : '审核拒绝'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="流水号">
          <el-input v-model="currentWithdraw?.withdrawNo" disabled />
        </el-form-item>
        <el-form-item label="提现金额">
          <el-input :value="'¥' + currentWithdraw?.amount" disabled />
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input
            v-model="auditForm.comment"
            type="textarea"
            :rows="3"
            :placeholder="auditForm.decision === 'APPROVE' ? '请输入通过意见（可选）' : '请输入拒绝原因'"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button :type="auditForm.decision === 'APPROVE' ? 'success' : 'danger'" @click="submitAudit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量审核对话框 -->
    <el-dialog
      v-model="batchAuditDialogVisible"
      :title="batchAuditForm.decision === 'APPROVE' ? '批量通过' : '批量拒绝'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-alert
        :title="batchAuditTitle"
        type="warning"
        :closable="false"
        style="margin-bottom: 20px"
      />
      <el-form :model="batchAuditForm" label-width="80px">
        <el-form-item label="审核意见">
          <el-input
            v-model="batchAuditForm.comment"
            type="textarea"
            :rows="3"
            :placeholder="batchAuditForm.decision === 'APPROVE' ? '请输入通过意见（可选）' : '请输入拒绝原因'"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="batchAuditDialogVisible = false">取消</el-button>
        <el-button :type="batchAuditForm.decision === 'APPROVE' ? 'success' : 'danger'" @click="submitBatchAudit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
// 版本: 2.0 - 修复模板字符串嵌套问题
console.log('[WithdrawalAudit] 组件已加载 v2.0')

import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Refresh, Clock, Loading, Money, WalletFilled
} from '@element-plus/icons-vue'
import {
  getWithdrawList,
  getWithdrawDetail,
  processWithdraw,
  batchProcessWithdraw,
  completeWithdraw,
  getWithdrawStatistics
} from '@/api/admin'

const loading = ref(false)
const withdrawList = ref([])
const currentWithdraw = ref(null)
const detailDialogVisible = ref(false)
const auditDialogVisible = ref(false)
const batchAuditDialogVisible = ref(false)
const selectedRows = ref([])

const stats = reactive({
  pendingCount: 0,
  processingCount: 0,
  todayWithdraw: 0,
  totalWithdraw: 0
})

const searchForm = reactive({
  keyword: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const auditForm = reactive({
  withdrawId: null,
  decision: '',
  comment: ''
})

const batchAuditForm = reactive({
  withdrawIds: [],
  decision: '',
  comment: ''
})

// 批量审核对话框标题
const batchAuditTitle = computed(() => {
  const action = batchAuditForm.decision === 'APPROVE' ? '通过' : '拒绝'
  return `确定要${action}选中的 ${selectedRows.value.length} 条提现申请吗？`
})

// 获取提现列表
const fetchWithdrawList = async () => {
  loading.value = true
  try {
    console.log('[提现审核] 获取提现列表, 页码:', pagination.page, '每页:', pagination.pageSize)
    const response = await getWithdrawList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      status: searchForm.status
    })

    if (response && response.success) {
      withdrawList.value = response.records || []
      pagination.total = response.total || 0
      console.log('[提现审核] 获取提现列表成功, 总数:', pagination.total)
    }
  } catch (error) {
    console.error('[提现审核] 获取提现列表失败:', error)
    ElMessage.error('获取提现列表失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const response = await getWithdrawStatistics()
    if (response && response.success) {
      Object.assign(stats, response.data)
    }
  } catch (error) {
    console.error('[提现审核] 获取统计数据失败:', error)
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    'pending': 'warning',
    'approved': 'primary',
    'rejected': 'danger',
    'processing': 'info',
    'success': 'success',
    'failed': 'danger'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    'pending': '待审核',
    'approved': '已通过',
    'rejected': '已拒绝',
    'processing': '处理中',
    'success': '成功',
    'failed': '失败'
  }
  return texts[status] || '未知'
}

// 获取提现方式类型
const getMethodType = (method) => {
  const types = {
    'wechat': 'success',
    'alipay': 'primary',
    'bank': 'info'
  }
  return types[method] || 'info'
}

// 获取提现方式文本
const getMethodText = (method) => {
  const texts = {
    'wechat': '微信',
    'alipay': '支付宝',
    'bank': '银行卡'
  }
  return texts[method] || '未知'
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchWithdrawList()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  pagination.page = 1
  fetchWithdrawList()
}

// 多选变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 查看详情
const handleView = async (row) => {
  try {
    const response = await getWithdrawDetail(row.id)
    if (response && response.success) {
      currentWithdraw.value = response.data
      detailDialogVisible.value = true
    } else {
      ElMessage.error(response?.message || '获取详情失败')
    }
  } catch (error) {
    console.error('[提现审核] 获取详情失败:', error)
    ElMessage.error('获取详情失败: ' + (error.message || '网络错误'))
  }
}

// 审核
const handleAudit = (row, decision) => {
  auditForm.withdrawId = row.id
  auditForm.decision = decision
  auditForm.comment = ''
  detailDialogVisible.value = false
  auditDialogVisible.value = true
}

// 提交审核
const submitAudit = async () => {
  try {
    const response = await processWithdraw(auditForm.withdrawId, {
      decision: auditForm.decision,
      comment: auditForm.comment
    })

    if (response && response.success) {
      ElMessage.success(auditForm.decision === 'APPROVE' ? '审核通过' : '已拒绝申请')
      auditDialogVisible.value = false
      fetchWithdrawList()
      fetchStatistics()
    } else {
      ElMessage.error(response?.message || '审核失败')
    }
  } catch (error) {
    console.error('[提现审核] 审核失败:', error)
    ElMessage.error('审核失败: ' + (error.message || '网络错误'))
  }
}

// 批量通过
const handleBatchApprove = () => {
  batchAuditForm.withdrawIds = selectedRows.value.map(row => row.id)
  batchAuditForm.decision = 'APPROVE'
  batchAuditForm.comment = ''
  batchAuditDialogVisible.value = true
}

// 批量拒绝
const handleBatchReject = () => {
  batchAuditForm.withdrawIds = selectedRows.value.map(row => row.id)
  batchAuditForm.decision = 'REJECT'
  batchAuditForm.comment = ''
  batchAuditDialogVisible.value = true
}

// 提交批量审核
const submitBatchAudit = async () => {
  try {
    const response = await batchProcessWithdraw({
      withdrawIds: batchAuditForm.withdrawIds,
      decision: batchAuditForm.decision,
      comment: batchAuditForm.comment
    })

    if (response && response.success) {
      const { successCount, failCount } = response
      const failMsg = failCount > 0 ? `，失败${failCount}个` : ''
      ElMessage.success(`批量处理完成：成功${successCount}个${failMsg}`)
      batchAuditDialogVisible.value = false
      selectedRows.value = []
      fetchWithdrawList()
      fetchStatistics()
    } else {
      ElMessage.error(response?.message || '批量审核失败')
    }
  } catch (error) {
    console.error('[提现审核] 批量审核失败:', error)
    ElMessage.error('批量审核失败: ' + (error.message || '网络错误'))
  }
}

// 完成提现
const handleComplete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要将此提现标记为已完成吗？', '确认完成', {
      type: 'warning'
    })

    const response = await completeWithdraw(row.id, { remark: '提现已完成' })

    if (response && response.success) {
      ElMessage.success('提现已完成')
      detailDialogVisible.value = false
      fetchWithdrawList()
      fetchStatistics()
    } else {
      ElMessage.error(response?.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('[提现审核] 完成提现失败:', error)
      ElMessage.error('操作失败: ' + (error.message || '网络错误'))
    }
  }
}

onMounted(() => {
  fetchWithdrawList()
  fetchStatistics()
})
</script>

<style scoped lang="less">
.withdrawal-audit-container {
  .page-header {
    margin-bottom: 20px;

    h1 {
      font-size: 24px;
      color: #303133;
      margin: 0 0 8px 0;
    }

    .subtitle {
      color: #909399;
      margin: 0;
      font-size: 14px;
    }
  }

  .stats-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
    gap: 16px;
    margin-bottom: 20px;

    .stat-card {
      text-align: center;
    }
  }

  .search-card {
    margin-bottom: 20px;

    .search-form {
      margin-bottom: 0;
    }
  }

  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .batch-actions {
        display: flex;
        gap: 8px;
      }
    }

    .pagination-container {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }

  .withdraw-detail {
    :deep(.el-descriptions__label) {
      width: 100px;
    }
  }
}
</style>
