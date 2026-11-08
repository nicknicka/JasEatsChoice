<template>
  <div class="merchant-audit-container">
    <!-- 页面标题 -->
    <AppPageHeader title="商家审核" subtitle="审核商家注册申请" :compact="true">
      <template #actions>
        <el-button type="primary" :loading="loading" @click="fetchPendingList">刷新</el-button>
      </template>
    </AppPageHeader>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <AppStatCard compact class="stat-card-pending" label="待审核" :value="stats.pending" icon="Clock" />
      <AppStatCard compact label="今日已审核" :value="stats.todayApproved" icon="Checked" />
      <AppStatCard compact label="本周通过率" :value="stats.approvalRate" suffix="%" icon="TrendCharts" />
    </div>

    <!-- 待审核列表 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">待审核商家列表</div>
      </template>

      <el-table :data="pendingList" v-loading="loading" stripe>
        <el-table-column prop="merchantId" label="商家 ID" min-width="180" show-overflow-tooltip />
        <el-table-column prop="name" label="商家名称" min-width="180" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column label="申请时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="success" size="small" link @click="handleAudit(row, 'APPROVED')">通过</el-button>
            <el-button type="danger" size="small" link @click="handleAudit(row, 'REJECTED')">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchPendingList"
          @current-change="fetchPendingList"
        />
      </div>
    </el-card>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      :title="auditForm.status === 'APPROVED' ? '确认通过商家审核' : '确认拒绝商家审核'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-descriptions v-if="currentMerchant" :column="2" border>
        <el-descriptions-item label="商家名称">{{ currentMerchant.name }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentMerchant.phone }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ currentMerchant.address }}</el-descriptions-item>
      </el-descriptions>

      <el-form :model="auditForm" label-width="80px" class="audit-form">
        <el-form-item label="本次操作">
          <el-tag :type="auditForm.status === 'APPROVED' ? 'success' : 'danger'">
            {{ auditForm.status === 'APPROVED' ? '确认通过' : '确认拒绝' }}
          </el-tag>
        </el-form-item>
        <el-form-item v-if="auditForm.status === 'REJECTED'" label="审核意见" required>
          <el-input
            v-model="auditForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button :disabled="submitting" @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitAudit">
          {{ auditForm.status === 'APPROVED' ? '确认通过' : '确认拒绝' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import AppStatCard from '@/components/common/AppStatCard.vue'
import { formatTime } from '../../utils/formatters'
import { getPendingMerchants, auditMerchant } from '../../api/admin'

const loading = ref(false)
const submitting = ref(false)
const pendingList = ref([])
const currentMerchant = ref(null)
const auditDialogVisible = ref(false)

const stats = reactive({
  pending: 0,
  todayApproved: 0,
  approvalRate: 0
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const auditForm = reactive({
  merchantId: null,
  status: 'APPROVED',
  reason: ''
})

const resetPendingStats = () => {
  pendingList.value = []
  pagination.total = 0
  stats.pending = 0
  stats.todayApproved = 0
  stats.approvalRate = 0
}

// 获取待审核列表
const fetchPendingList = async () => {
  loading.value = true
  try {
    const response = await getPendingMerchants({
      page: pagination.page,
      pageSize: pagination.pageSize
    })

    if (response?.records) {
      pendingList.value = response.records
      pagination.total = response.total ?? 0
      stats.pending = pagination.total
      stats.todayApproved = response.todayApproved || 0
      stats.approvalRate = response.approvalRate || 0
    } else if (response?.code === '200' || response?.code === 200) {
      pendingList.value = response.data?.records || response.data?.list || response.data || []
      pagination.total = response.data?.total ?? 0
      stats.pending = pagination.total
      stats.todayApproved = response.data?.todayApproved || 0
      stats.approvalRate = response.data?.approvalRate || 0
    } else {
      console.warn('获取待审核商家失败，接口返回异常:', response)
      resetPendingStats()
      ElMessage.warning(response?.message || '获取待审核商家失败，请稍后重试')
    }
  } catch (error) {
    console.error('获取待审核列表失败:', error)
    resetPendingStats()
    ElMessage.error('获取待审核列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 审核
const handleAudit = (row, status) => {
  currentMerchant.value = row
  auditForm.merchantId = row.merchantId
  auditForm.status = status
  auditForm.reason = ''
  auditDialogVisible.value = true
}

// 提交审核
const submitAudit = async () => {
  if (auditForm.status === 'REJECTED' && !auditForm.reason.trim()) {
    ElMessage.warning('请填写拒绝原因')
    return
  }

  if (auditForm.status === 'REJECTED') {
    try {
      await ElMessageBox.confirm(
        `确认拒绝「${currentMerchant.value?.name || '该商家'}」的注册申请？`,
        '拒绝确认',
        {
          type: 'warning',
          confirmButtonText: '确认拒绝',
          cancelButtonText: '取消',
          confirmButtonClass: 'el-button--danger'
        }
      )
    } catch {
      return
    }
  }

  submitting.value = true
  try {
    const response = await auditMerchant(auditForm.merchantId, {
      status: auditForm.status,
      reason: auditForm.reason
    })

    if (response.success) {
      const auditedId = auditForm.merchantId
      auditDialogVisible.value = false
      // 行内即时反馈：先就地移除该行，再同步计数
      pendingList.value = pendingList.value.filter((item) => item.merchantId !== auditedId)
      pagination.total = Math.max(0, pagination.total - 1)
      stats.pending = Math.max(0, stats.pending - 1)
      if (auditForm.status === 'APPROVED') {
        stats.todayApproved += 1
      }
      ElMessage.success(auditForm.status === 'APPROVED' ? '已通过该商家' : '已拒绝该商家')
    } else {
      ElMessage.error(response.message || '审核失败')
    }
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error(error?.response?.data?.message || '审核失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchPendingList()
})
</script>

<style scoped lang="less">
@import '@/assets/css/nordic-theme.less';

.merchant-audit-container {
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

  /* 待审核是待办入口，按 §3.1 语义色给警示黄强调（§4.8） */
  .stat-card-pending :deep(.app-stat-card__icon) {
    color: var(--warning);
  }

  .audit-form {
    margin-top: var(--space-5);
  }

  .table-card {
    .card-header {
      font-weight: 600;
    }

    .pagination-container {
      margin-top: var(--space-5);
      display: flex;
      justify-content: flex-end;
    }
  }
}

/* §4.3：admin 列表只保留单行 40px / 双行 ≤56px 两档，列宽不足由单元格省略而不是折行。 */
.merchant-audit-container :deep(.el-table__body .el-table__cell) {
  padding-top: 0;
  padding-bottom: 0;
}

.merchant-audit-container :deep(.el-table__body .el-table__row) {
  height: 40px;
}

/* §4.10：统计卡使用紧凑档后，收口卡片间距，保证 1440×900 下的首屏预算。 */
.merchant-audit-container .stats-section {
  margin-bottom: var(--space-3);
}

.merchant-audit-container :deep(.table-card > .el-card__body) {
  padding-top: var(--space-2);
}
</style>
