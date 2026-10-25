<template>
  <div class="recharge-management-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>充值记录</h1>
      <p class="subtitle">管理用户充值记录</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card">
        <el-statistic title="今日充值金额" :value="stats.todayAmount" :precision="2" prefix="¥" />
      </el-card>
      <el-card class="stat-card">
        <el-statistic title="今日充值笔数" :value="stats.todayCount" />
      </el-card>
      <el-card class="stat-card">
        <el-statistic title="本月充值金额" :value="stats.monthAmount" :precision="2" prefix="¥" />
      </el-card>
      <el-card class="stat-card">
        <el-statistic title="本月充值笔数" :value="stats.monthCount" />
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
        <el-form-item label="充值方式">
          <el-select v-model="searchForm.paymentMethod" placeholder="全部" clearable style="width: 140px">
            <el-option label="微信支付" value="wechat" />
            <el-option label="支付宝" value="alipay" />
            <el-option label="银行卡" value="bank" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待支付" value="pending" />
            <el-option label="成功" value="success" />
            <el-option label="失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
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

    <!-- 充值记录列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="rechargeList" v-loading="loading" stripe>
        <el-table-column label="记录ID" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getRechargeRecordId(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="150" show-overflow-tooltip />
        <el-table-column prop="amount" label="充值金额" width="120">
          <template #default="{ row }">
            <span style="color: #67c23a; font-weight: bold">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="充值方式" width="120">
          <template #default="{ row }">
            {{ getPaymentMethodText(row.paymentMethod) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="transactionId" label="交易单号" min-width="220" show-overflow-tooltip />
        <el-table-column prop="createTime" label="充值时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleView(row)">详情</el-button>
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
          @size-change="fetchRechargeList"
          @current-change="fetchRechargeList"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="充值详情"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-descriptions v-if="currentRecord" :column="2" border>
        <el-descriptions-item label="记录ID">{{ getRechargeRecordId(currentRecord) }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentRecord.username }}</el-descriptions-item>
        <el-descriptions-item label="充值金额">
          <span style="color: #67c23a; font-weight: bold; font-size: 1.286rem /* 原值: 18px */">¥{{ currentRecord.amount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="充值方式">
          {{ getPaymentMethodText(currentRecord.paymentMethod) }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentRecord.status)">
            {{ getStatusText(currentRecord.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="充值前余额">¥{{ currentRecord.balanceBefore || 0 }}</el-descriptions-item>
        <el-descriptions-item label="充值后余额">¥{{ currentRecord.balanceAfter || 0 }}</el-descriptions-item>
        <el-descriptions-item label="交易单号" :span="2">{{ currentRecord.transactionId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="充值时间" :span="2">{{ currentRecord.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentRecord.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getRechargeList, getRechargeStats, getRechargeDetail } from '@/api/admin'

const loading = ref(false)
const rechargeList = ref([])
const currentRecord = ref(null)
const detailDialogVisible = ref(false)

const stats = reactive({
  todayAmount: 0,
  todayCount: 0,
  monthAmount: 0,
  monthCount: 0
})

const searchForm = reactive({
  keyword: '',
  paymentMethod: '',
  status: '',
  dateRange: null
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 获取充值统计数据
const fetchRechargeStats = async () => {
  try {
    const response = await getRechargeStats()
    if (response) {
      stats.todayAmount = response.todayAmount || 0
      stats.todayCount = response.todayCount || 0
      stats.monthAmount = response.monthAmount || 0
      stats.monthCount = response.monthCount || 0
    }
  } catch (error) {
    console.error('获取充值统计失败:', error)
  }
}

// 获取充值记录列表
const fetchRechargeList = async () => {
  loading.value = true
  try {
    const response = await getRechargeList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      paymentMethod: searchForm.paymentMethod,
      status: searchForm.status
    })

    if (response) {
      rechargeList.value = (response.records || []).map(normalizeRechargeRecord)
      pagination.total = response.total || 0
    }
  } catch (error) {
    console.error('获取充值记录失败:', error)
    ElMessage.error('获取充值记录失败')
  } finally {
    loading.value = false
  }
}

const normalizeRechargeMethod = (method) => String(method || '').trim().toLowerCase()
const normalizeRechargeStatus = (status) => String(status || '').trim().toLowerCase()

const normalizeRechargeRecord = (record) => ({
  ...record,
  rechargeId: record?.rechargeId || record?.id || record?.rechargeNo || '-',
  username: record?.username || record?.userName || record?.nickname || record?.userId || '-',
  status: record?.status || record?.rechargeStatus || '',
  transactionId: record?.transactionId || record?.rechargeNo || '-'
})

const getRechargeRecordId = (record) => record?.rechargeId || record?.id || record?.rechargeNo || '-'

// 获取支付方式文本
const getPaymentMethodText = (method) => {
  const texts = {
    wechat: '微信支付',
    alipay: '支付宝',
    bank: '银行卡',
    bank_card: '银行卡'
  }
  const normalized = normalizeRechargeMethod(method)
  return texts[normalized] || method || '-'
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    success: 'success',
    pending: 'warning',
    processing: 'warning',
    failed: 'danger'
  }
  return types[normalizeRechargeStatus(status)] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    success: '成功',
    pending: '待支付',
    processing: '处理中',
    failed: '失败'
  }
  return texts[normalizeRechargeStatus(status)] || '未知'
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchRechargeList()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.paymentMethod = ''
  searchForm.status = ''
  searchForm.dateRange = null
  pagination.page = 1
  fetchRechargeList()
}

// 查看详情
const handleView = async (row) => {
  try {
    const rechargeId = getRechargeRecordId(row)
    const response = await getRechargeDetail(rechargeId)
    if (response) {
      currentRecord.value = normalizeRechargeRecord(response.data || response)
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取充值详情失败:', error)
    ElMessage.error('获取充值详情失败')
  }
}

onMounted(() => {
  fetchRechargeList()
  fetchRechargeStats()
})
</script>

<style scoped lang="less">
.recharge-management-container {
  .page-header {
    margin-bottom: 20px;

    h1 {
      font-size: 1.714rem /* 原值: 24px */;
      color: #303133;
      margin: 0 0 8px 0;
    }

    .subtitle {
      color: #909399;
      margin: 0;
      font-size: 1rem /* 原值: 14px */;
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
}
</style>
