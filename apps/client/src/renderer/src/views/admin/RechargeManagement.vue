<template>
  <div class="recharge-management-container">
    <!-- 页头：统一标题容器 + 右侧动作区（§4.7） -->
    <AppPageHeader title="充值记录" subtitle="管理用户充值记录" :compact="true">
      <template #actions>
        <el-button :icon="Download" @click="handleExport">导出</el-button>
      </template>
    </AppPageHeader>

    <!-- 概览卡：线性图标 + 数值 + 标签，四个一行（§4.8） -->
    <div class="stats-section">
      <AppStatCard compact label="今日充值金额" :value="formatMoney(stats.todayAmount)" icon="Money" />
      <AppStatCard compact label="今日充值笔数" :value="stats.todayCount" icon="Tickets" />
      <AppStatCard compact label="本月充值金额" :value="formatMoney(stats.monthAmount)" icon="Wallet" />
      <AppStatCard compact label="本月充值笔数" :value="stats.monthCount" icon="TrendCharts" />
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
      <!-- 空数据：紧凑中文空状态替换表体（§4.3、§5.5） -->
      <AppEmptyState
        v-if="!loading && rechargeList.length === 0"
        compact
        icon="Money"
        description="没有符合条件的充值记录，换个关键词或清空筛选试试"
        action-text="重置筛选"
        @action="handleReset"
      />

      <template v-else>
        <el-table :data="rechargeList" v-loading="loading" stripe>
          <!-- 记录 ID：单行完整，不折行（§4.3、§5.4） -->
          <el-table-column label="记录ID" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="cell-num">{{ getRechargeRecordId(row) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="username" label="用户名" width="160" show-overflow-tooltip />
          <!-- 金额：右对齐 + 无衬线等宽数字 + 中性深色，不带红绿蓝（§4.3、§5.2） -->
          <el-table-column label="充值金额" width="95" align="right">
            <template #default="{ row }">
              <span class="amount">¥{{ row.amount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="paymentMethod" label="充值方式" width="100">
            <template #default="{ row }">
              {{ getPaymentMethodText(row.paymentMethod) }}
            </template>
          </el-table-column>
          <!-- 状态：待支付 warning、处理中 info，同色不同义拆开（§3.1、§4.6） -->
          <el-table-column label="状态" width="95">
            <template #default="{ row }">
              <StatusTag :status="row.status" :text="getStatusText(row.status)" />
            </template>
          </el-table-column>
          <el-table-column label="交易单号" min-width="165" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="cell-num">{{ row.transactionId || '-' }}</span>
            </template>
          </el-table-column>
          <!-- 时间统一 YYYY-MM-DD HH:mm，去掉 ISO 的 T（§4.3） -->
          <el-table-column label="充值时间" width="165">
            <template #default="{ row }">
              <span class="cell-num">{{ formatTime(row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="85" fixed="right">
            <template #default="{ row }">
              <RowActions :primary="{ text: '详情', onClick: () => handleView(row) }" :items="[]" />
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页：合计用实际记录数兜底，空数据时不渲染（§4.3、§5.4） -->
        <div v-if="rechargeList.length > 0" class="pagination-container">
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
      </template>
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
          <span class="amount amount--lg">¥{{ currentRecord.amount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="充值方式">
          {{ getPaymentMethodText(currentRecord.paymentMethod) }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <StatusTag :status="currentRecord.status" :text="getStatusText(currentRecord.status)" />
        </el-descriptions-item>
        <el-descriptions-item label="充值前余额">¥{{ currentRecord.balanceBefore || 0 }}</el-descriptions-item>
        <el-descriptions-item label="充值后余额">¥{{ currentRecord.balanceAfter || 0 }}</el-descriptions-item>
        <el-descriptions-item label="交易单号" :span="2">{{ currentRecord.transactionId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="充值时间" :span="2">{{ formatTime(currentRecord.createdAt) }}</el-descriptions-item>
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
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import AppStatCard from '@/components/common/AppStatCard.vue'
import AppEmptyState from '@/components/common/AppEmptyState.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import RowActions from '@/components/common/RowActions.vue'
import { formatTime, formatAmount } from '@/utils/formatters'
import { exportToExcel } from '@/utils/export.js'
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

const formatMoney = (value) => `¥${formatAmount(value)}`

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
      pagination.total = response.total ?? 0
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

// 获取状态文本：状态色由 StatusTag 按 §3.1 状态表统一映射
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

// 导出当前列表（§4.1 次级动作，与 12-提现审核 的导出能力对齐）
const handleExport = () => {
  if (!rechargeList.value.length) {
    ElMessage.warning('暂无可导出的充值记录')
    return
  }

  const success = exportToExcel(
    rechargeList.value.map((row) => ({
      记录ID: getRechargeRecordId(row),
      用户名: row.username || '-',
      充值金额: row.amount,
      充值方式: getPaymentMethodText(row.paymentMethod),
      状态: getStatusText(row.status),
      交易单号: row.transactionId || '-',
      充值时间: formatTime(row.createdAt)
    })),
    {
      filename: '充值记录',
      headers: [
        { key: '记录ID', label: '记录ID', width: 22 },
        { key: '用户名', label: '用户名', width: 22 },
        { key: '充值金额', label: '充值金额', width: 12 },
        { key: '充值方式', label: '充值方式', width: 12 },
        { key: '状态', label: '状态', width: 10 },
        { key: '交易单号', label: '交易单号', width: 22 },
        { key: '充值时间', label: '充值时间', width: 20 }
      ]
    }
  )

  if (success) {
    ElMessage.success('导出成功')
  } else {
    ElMessage.error('导出失败')
  }
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
  padding: var(--card-padding-compact) var(--page-gutter);
  background: var(--canvas);
  max-width: 1440px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;

  .stats-section {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: var(--card-gap-compact);
    margin-bottom: var(--space-8);
  }

  .search-card {
    margin-bottom: var(--space-8);

    .search-form {
      margin-bottom: 0;
    }
  }

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

  /* ID / 交易单号 / 时间：单行不折行 + 等宽数字（§4.3、§5.4） */
  .cell-num {
    font-family: var(--font-num);
    font-variant-numeric: tabular-nums;
    white-space: nowrap;
  }

  /* 金额：无衬线等宽数字 + 中性深色，不带红绿蓝（§4.3、§5.2） */
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
}

/* §4.3：admin 列表只保留单行 40px / 双行 ≤56px 两档，列宽不足由单元格省略而不是折行。 */
.recharge-management-container :deep(.el-table__body .el-table__cell) {
  padding-top: 0;
  padding-bottom: 0;
}

.recharge-management-container :deep(.el-table__body .el-table__row) {
  height: 40px;
}

/* §4.10：统计卡使用紧凑档后，收口卡片间距，保证 1440×900 下的首屏预算。 */
.recharge-management-container .stats-section {
  margin-bottom: var(--space-3);
}

.recharge-management-container .search-card {
  margin-bottom: var(--space-3);
}

.recharge-management-container :deep(.search-card > .el-card__body) {
  padding: var(--space-2) var(--space-4);
}

.recharge-management-container :deep(.table-card > .el-card__body) {
  padding-top: var(--space-2);
}
</style>
