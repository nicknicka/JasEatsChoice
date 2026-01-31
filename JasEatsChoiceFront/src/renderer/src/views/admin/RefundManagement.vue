<template>
  <div class="refund-management-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>退款管理</h1>
      <p class="subtitle">处理用户退款申请</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card">
        <el-statistic title="待处理" :value="stats.pending" />
      </el-card>
      <el-card class="stat-card">
        <el-statistic title="今日已退款" :value="stats.todayApproved" :precision="2" prefix="¥" />
      </el-card>
      <el-card class="stat-card">
        <el-statistic title="今日已拒绝" :value="stats.todayRejected" />
      </el-card>
      <el-card class="stat-card">
        <el-statistic title="本月退款总额" :value="stats.monthTotal" :precision="2" prefix="¥" />
      </el-card>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索用户名、订单号"
            clearable
            style="width: 250px"
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

    <!-- 退款列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="refundList" v-loading="loading" stripe>
        <el-table-column prop="refundId" label="退款ID" width="120" />
        <el-table-column prop="orderId" label="订单ID" width="120" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="refundAmount" label="退款金额" width="120">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold">¥{{ row.refundAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="退款原因" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              type="primary"
              size="small"
              link
              @click="handleProcess(row)"
            >
              处理
            </el-button>
            <el-button type="info" size="small" link @click="handleView(row)">详情</el-button>
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
          @size-change="fetchRefundList"
          @current-change="fetchRefundList"
        />
      </div>
    </el-card>

    <!-- 处理退款对话框 -->
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
            <span style="color: #f56c6c; font-weight: bold; font-size: 18px">¥{{ currentRefund.refundAmount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="退款原因" :span="2">{{ currentRefund.reason }}</el-descriptions-item>
          <el-descriptions-item label="申请说明" :span="2">{{ currentRefund.description || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请时间" :span="2">{{ currentRefund.applyTime }}</el-descriptions-item>
        </el-descriptions>

        <el-divider>处理操作</el-divider>
        <el-form :model="processForm" label-width="80px">
          <el-form-item label="处理结果">
            <el-radio-group v-model="processForm.decision">
              <el-radio label="approve">批准退款</el-radio>
              <el-radio label="reject">拒绝退款</el-radio>
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

    <!-- 详情对话框 -->
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
          <span style="color: #f56c6c; font-weight: bold">¥{{ currentRefund.refundAmount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentRefund.status)">
            {{ getStatusText(currentRefund.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentRefund.applyTime }}</el-descriptions-item>
        <el-descriptions-item label="处理时间" :span="2">{{ currentRefund.processTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="退款原因" :span="2">{{ currentRefund.reason }}</el-descriptions-item>
        <el-descriptions-item label="申请说明" :span="2">{{ currentRefund.description || '-' }}</el-descriptions-item>
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
import api from '@/utils/api'

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
  decision: 'approve',
  comment: ''
})

// 获取退款列表
const fetchRefundList = async () => {
  loading.value = true
  try {
    const response = await api.get('http://localhost:8080/api/admin/finance/refunds', {
      params: {
        page: pagination.page,
        pageSize: pagination.pageSize,
        keyword: searchForm.keyword,
        status: searchForm.status
      }
    })

    if (response) {
      refundList.value = response.records || []
      pagination.total = response.total || 0

      // 更新统计数据
      stats.pending = refundList.value.filter(r => r.status === 'PENDING').length
      stats.todayApproved = refundList.value
        .filter(r => r.status === 'COMPLETED')
        .reduce((sum, r) => sum + (r.refundAmount || 0), 0)
      stats.todayRejected = refundList.value.filter(r => r.status === 'REJECTED').length
    }
  } catch (error) {
    console.error('获取退款列表失败:', error)
    ElMessage.error('获取退款列表失败')
  } finally {
    loading.value = false
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    'PENDING': 'warning',
    'APPROVED': 'primary',
    'REJECTED': 'danger',
    'PROCESSING': 'info',
    'COMPLETED': 'success'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    'PENDING': '待处理',
    'APPROVED': '已批准',
    'REJECTED': '已拒绝',
    'PROCESSING': '退款中',
    'COMPLETED': '已完成'
  }
  return texts[status] || '未知'
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchRefundList()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  searchForm.dateRange = null
  pagination.page = 1
  fetchRefundList()
}

// 查看详情
const handleView = async (row) => {
  try {
    const response = await api.get(`http://localhost:8080/api/admin/finance/refunds/${row.refundId}`)
    if (response) {
      currentRefund.value = response
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取退款详情失败:', error)
    ElMessage.error('获取退款详情失败')
  }
}

// 处理退款
const handleProcess = async (row) => {
  try {
    const response = await api.get(`http://localhost:8080/api/admin/finance/refunds/${row.refundId}`)
    if (response) {
      currentRefund.value = response
      processForm.decision = 'approve'
      processForm.comment = ''
      processDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取退款信息失败:', error)
    ElMessage.error('获取退款信息失败')
  }
}

// 提交处理
const submitProcess = async () => {
  if (processForm.decision === 'reject' && !processForm.comment.trim()) {
    ElMessage.warning('拒绝时必须填写处理意见')
    return
  }

  try {
    const response = await api.post(
      `http://localhost:8080/api/admin/finance/refunds/${currentRefund.value.refundId}/process`,
      {
        decision: processForm.decision,
        comment: processForm.comment
      }
    )

    if (response) {
      ElMessage.success('处理提交成功')
      processDialogVisible.value = false
      fetchRefundList()
    }
  } catch (error) {
    console.error('处理提交失败:', error)
    ElMessage.error('处理提交失败')
  }
}

onMounted(() => {
  fetchRefundList()
})
</script>

<style scoped lang="less">
.refund-management-container {
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
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
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
    .pagination-container {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }

  .process-content {
    .refund-info {
      margin-bottom: 20px;
    }
  }
}
</style>
