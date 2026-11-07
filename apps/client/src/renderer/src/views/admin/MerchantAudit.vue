<template>
  <div class="merchant-audit-container">
    <!-- 页面标题 -->
    <AppPageHeader title="商家审核" :compact="true" class="audit-page-header">
      <template #actions>
        <el-button type="primary" @click="fetchPendingList">刷新</el-button>
      </template>
    </AppPageHeader>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <AppStatCard label="待审核" :value="stats.pending" icon="Clock" />
      <AppStatCard label="今日已审核" :value="stats.todayApproved" icon="Checked" />
      <AppStatCard label="本周通过率" :value="stats.approvalRate + '%'" icon="TrendCharts" />
    </div>

    <!-- 待审核列表 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">待审核商家列表</div>
      </template>

      <el-table :data="pendingList" v-loading="loading" stripe>
        <el-table-column prop="merchantId" label="商家 ID" width="100" />
        <el-table-column prop="name" label="商家名称" min-width="180" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="申请时间" width="180" />
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

      <el-form :model="auditForm" label-width="80px" style="margin-top: var(--space-5)">
        <el-form-item label="本次操作">
          <el-tag :type="auditForm.status === 'APPROVED' ? 'success' : 'danger'">
            {{ auditForm.status === 'APPROVED' ? '确认通过' : '确认拒绝' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="审核意见" v-if="auditForm.status === 'REJECTED'">
          <el-input
            v-model="auditForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">
          {{ auditForm.status === 'APPROVED' ? '确认通过' : '确认拒绝' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Clock, Checked, TrendCharts } from '@element-plus/icons-vue'
import AppPageHeader from '@/components/AppPageHeader.vue'
import AppStatCard from '@/components/AppStatCard.vue'
import { getPendingMerchants, auditMerchant } from '../../api/admin'

const loading = ref(false)
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
      pagination.total = response.total || 0
      stats.pending = pagination.total
      stats.todayApproved = response.todayApproved || 0
      stats.approvalRate = response.approvalRate || 0
    } else if (response?.code === '200' || response?.code === 200) {
      pendingList.value = response.data?.records || response.data?.list || response.data || []
      pagination.total = response.data?.total || pendingList.value.length || 0
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
  try {
    const response = await auditMerchant(auditForm.merchantId, {
      status: auditForm.status,
      reason: auditForm.reason
    })

    if (response.success) {
      ElMessage.success('审核完成')
      auditDialogVisible.value = false
      fetchPendingList()
    } else {
      ElMessage.error(response.message || '审核失败')
    }
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
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

  .audit-page-header {
    margin-bottom: var(--space-6);
  }

  .stats-section {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: var(--card-gap-compact);
    margin-bottom: var(--space-8);
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
</style>
