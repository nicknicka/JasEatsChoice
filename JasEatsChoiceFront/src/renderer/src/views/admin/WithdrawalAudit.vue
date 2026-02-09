<template>
  <div class="withdrawal-audit-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>提现审核</h2>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="16" class="stats-row">
        <el-col :span="6">
          <el-card shadow="hover">
            <el-statistic title="待审核" :value="stats.pending">
              <template #prefix>
                <el-icon><Clock /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <el-statistic title="处理中" :value="stats.processing">
              <template #prefix>
                <el-icon><Loading /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <el-statistic title="今日提现" :value="stats.todayAmount" :precision="2" prefix="¥">
              <template #prefix>
                <el-icon><Money /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <el-statistic title="累计提现" :value="stats.totalAmount" :precision="2" prefix="¥">
              <template #prefix>
                <el-icon><Wallet /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
      </el-row>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="流水号/用户ID"
            clearable
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="全部"
            clearable
            @change="handleSearch"
          >
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已拒绝" value="rejected" />
            <el-option label="处理中" value="processing" />
            <el-option label="成功" value="success" />
            <el-option label="失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="withdrawNo" label="流水号" width="180" />
        <el-table-column prop="userId" label="用户ID" width="120" />
        <el-table-column label="提现金额" width="120">
          <template #default="scope">
            <span class="amount-text">¥{{ scope.row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="提现方式" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.withdrawMethod === 'wechat'" type="success">微信</el-tag>
            <el-tag v-else-if="scope.row.withdrawMethod === 'alipay'" type="primary">支付宝</el-tag>
            <el-tag v-else type="info">银行卡</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.withdrawStatus)">
              {{ getStatusText(scope.row.withdrawStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button
              v-if="scope.row.withdrawStatus === 'pending'"
              type="success"
              link
              size="small"
              @click="handleAudit(scope.row, 'APPROVE')"
            >
              通过
            </el-button>
            <el-button
              v-if="scope.row.withdrawStatus === 'pending'"
              type="danger"
              link
              size="small"
              @click="handleAudit(scope.row, 'REJECT')"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 批量操作按钮 -->
      <div v-if="selectedRows.length > 0" class="batch-actions">
        <el-button type="success" @click="handleBatchApprove">
          批量通过 ({{ selectedRows.length }})
        </el-button>
        <el-button type="danger" @click="handleBatchReject">
          批量拒绝 ({{ selectedRows.length }})
        </el-button>
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
    <el-dialog v-model="detailVisible" title="提现详情" width="600px">
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="流水号" :span="2">{{ currentRow.withdrawNo }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentRow.userId }}</el-descriptions-item>
        <el-descriptions-item label="提现方式">{{ getMethodText(currentRow.withdrawMethod) }}</el-descriptions-item>
        <el-descriptions-item label="提现金额">¥{{ currentRow.amount }}</el-descriptions-item>
        <el-descriptions-item label="手续费">¥{{ currentRow.fee || 0 }}</el-descriptions-item>
        <el-descriptions-item label="实际到账" :span="2">¥{{ currentRow.actualAmount || currentRow.amount }}</el-descriptions-item>
        <el-descriptions-item label="提现账号" :span="2">{{ currentRow.accountInfo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentRow.withdrawStatus)">
            {{ getStatusText(currentRow.withdrawStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentRow.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRow.rejectReason" label="拒绝原因" :span="2">
          <span class="error-text">{{ currentRow.rejectReason }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="currentRow && currentRow.withdrawStatus === 'pending'"
          type="success"
          @click="handleAudit(currentRow, 'APPROVE')"
        >
          审核通过
        </el-button>
        <el-button
          v-if="currentRow && currentRow.withdrawStatus === 'pending'"
          type="danger"
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
    >
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="流水号">
          <el-input :value="currentRow?.withdrawNo" disabled />
        </el-form-item>
        <el-form-item label="提现金额">
          <el-input :value="'¥' + (currentRow?.amount || 0)" disabled />
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
    >
      <el-alert
        :title="getBatchAuditTitle()"
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Clock, Loading, Money, Wallet } from '@element-plus/icons-vue'
import {
  getWithdrawList,
  getWithdrawDetail,
  processWithdraw,
  batchProcessWithdraw,
  getWithdrawStatistics
} from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const currentRow = ref(null)
const detailVisible = ref(false)
const auditVisible = ref(false)
const batchAuditVisible = ref(false)
const selectedRows = ref([])

const stats = reactive({
  pending: 0,
  processing: 0,
  todayAmount: 0,
  totalAmount: 0
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
  comment: ''
})

const batchAuditForm = reactive({
  comment: ''
})

const auditDecision = ref('')
const batchAuditDecision = ref('')

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const response = await getWithdrawList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      status: searchForm.status
    })

    if (response && response.success) {
      tableData.value = response.records || []
      pagination.total = response.total || 0
    }
  } catch (error) {
    console.error('[提现审核] 获取数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const fetchStats = async () => {
  try {
    const response = await getWithdrawStatistics()
    if (response && response.success && response.data) {
      stats.pending = response.data.pendingCount || 0
      stats.processing = response.data.processingCount || 0
      stats.todayAmount = response.data.todayWithdraw || 0
      stats.totalAmount = response.data.totalWithdraw || 0
    }
  } catch (error) {
    console.error('[提现审核] 获取统计失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  pagination.page = 1
  fetchData()
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

// 审核
const handleAudit = (row, decision) => {
  auditDecision.value = decision
  auditForm.comment = ''
  currentRow.value = row
  detailVisible.value = false
  auditVisible.value = true
}

// 提交审核
const submitAudit = async () => {
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

// 批量通过
const handleBatchApprove = () => {
  batchAuditDecision.value = 'APPROVE'
  batchAuditForm.comment = ''
  batchAuditVisible.value = true
}

// 批量拒绝
const handleBatchReject = () => {
  batchAuditDecision.value = 'REJECT'
  batchAuditForm.comment = ''
  batchAuditVisible.value = true
}

// 获取批量审核标题
const getBatchAuditTitle = () => {
  const action = batchAuditDecision.value === 'APPROVE' ? '通过' : '拒绝'
  const count = selectedRows.value.length
  return '确定要' + action + '选中的 ' + count + ' 条提现申请吗？'
}

// 提交批量审核
const submitBatchAudit = async () => {
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
      let msg = '批量处理完成：成功' + successCount + '个'
      if (failCount > 0) {
        msg = msg + '，失败' + failCount + '个'
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

// 获取状态类型
const getStatusType = (status) => {
  const map = {
    'pending': 'warning',
    'approved': 'primary',
    'rejected': 'danger',
    'processing': 'info',
    'success': 'success',
    'failed': 'danger'
  }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    'pending': '待审核',
    'approved': '已通过',
    'rejected': '已拒绝',
    'processing': '处理中',
    'success': '成功',
    'failed': '失败'
  }
  return map[status] || '未知'
}

// 获取提现方式文本
const getMethodText = (method) => {
  const map = {
    'wechat': '微信',
    'alipay': '支付宝',
    'bank': '银行卡'
  }
  return map[method] || '未知'
}

onMounted(() => {
  fetchData()
  fetchStats()
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
  }

  .stats-row {
    margin-bottom: 20px;
  }

  .search-form {
    margin-bottom: 20px;
  }

  .batch-actions {
    margin-top: 16px;
    display: flex;
    gap: 10px;
  }

  .el-pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .amount-text {
    color: #f56c6c;
    font-weight: bold;
  }

  .error-text {
    color: #f56c6c;
  }
}
</style>
