<template>
  <div class="dish-audit-container">
    <!-- 页面标题 -->
    <AppPageHeader title="菜品审核" :compact="true" class="audit-page-header">
      <template #actions>
        <el-button type="primary" @click="fetchDishList">刷新</el-button>
      </template>
    </AppPageHeader>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <AppStatCard label="待审核" :value="stats.pending" icon="Clock" />
      <AppStatCard label="今日已审核" :value="stats.todayApproved" icon="Checked" />
      <AppStatCard label="今日已拒绝" :value="stats.todayRejected" icon="Close" />
      <AppStatCard label="本月审核总数" :value="stats.monthTotal" icon="TrendCharts" />
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索菜品名称、商家名称"
            clearable
            style="width: 250px"
          />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.auditStatus" placeholder="全部" clearable style="width: 140px">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="提交日期">
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

    <!-- 菜品列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="dishList" v-loading="loading" stripe>
        <el-table-column prop="dishId" label="菜品 ID" min-width="180" show-overflow-tooltip />
        <el-table-column prop="dishName" label="菜品名称" min-width="200" />
        <el-table-column prop="merchantName" label="商家名称" min-width="150" />
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <StatusTag :type="getStatusType(row.auditStatus)" :text="getStatusText(row.auditStatus)" />
          </template>
        </el-table-column>
        <el-table-column label="图片" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.imageUrl"
              :src="row.imageUrl"
              :preview-src-list="[row.imageUrl]"
              fit="cover"
              style="width: 60px; height: 60px"
            />
            <span v-else style="color: #909399">无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="180" />
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
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchDishList"
          @current-change="fetchDishList"
        />
      </div>
    </el-card>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      :title="auditForm.status === 'APPROVED' ? '确认通过菜品审核' : '确认拒绝菜品审核'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-descriptions v-if="currentDish" :column="2" border>
        <el-descriptions-item label="菜品名称">{{ currentDish.dishName }}</el-descriptions-item>
        <el-descriptions-item label="商家名称">{{ currentDish.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="菜品描述" :span="2">{{ currentDish.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <StatusTag :type="getStatusType(currentDish.auditStatus)" :text="getStatusText(currentDish.auditStatus)" />
        </el-descriptions-item>
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
import { Search, Refresh, Clock, Checked, Close, TrendCharts } from '@element-plus/icons-vue'
import AppPageHeader from '@/components/AppPageHeader.vue'
import AppStatCard from '@/components/AppStatCard.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { getDishAuditList, auditDish } from '@/api/admin'

const loading = ref(false)
const dishList = ref([])
const currentDish = ref(null)
const auditDialogVisible = ref(false)

const stats = reactive({
  pending: 0,
  todayApproved: 0,
  todayRejected: 0,
  monthTotal: 0
})

const searchForm = reactive({
  keyword: '',
  auditStatus: '',
  dateRange: null
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const auditForm = reactive({
  dishId: null,
  status: 'APPROVED',
  reason: ''
})

// 获取菜品列表
const fetchDishList = async () => {
  loading.value = true
  try {
    const response = await getDishAuditList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      auditStatus: searchForm.auditStatus,
      startDate: searchForm.dateRange?.[0] || '',
      endDate: searchForm.dateRange?.[1] || ''
    })

    if (response) {
      dishList.value = response.records || []
      pagination.total = response.total || 0
      
      // Update statistics
      stats.pending = response.pending || 0
      stats.todayApproved = response.todayApproved || 0
      stats.todayRejected = response.todayRejected || 0
      stats.monthTotal = response.monthTotal || 0
    }
  } catch (error) {
    console.error('获取菜品审核列表失败:', error)
    ElMessage.error('获取菜品审核列表失败：' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝'
  }
  return textMap[status] || '未知'
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchDishList()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.auditStatus = ''
  searchForm.dateRange = null
  pagination.page = 1
  fetchDishList()
}

// 审核
const handleAudit = (row, status) => {
  currentDish.value = row
  auditForm.dishId = row.dishId
  auditForm.status = status
  auditForm.reason = ''
  auditDialogVisible.value = true
}

// 提交审核
const submitAudit = async () => {
  try {
    const response = await auditDish(auditForm.dishId, {
      status: auditForm.status,
      reason: auditForm.reason
    })

    if (response) {
      ElMessage.success('审核完成')
      auditDialogVisible.value = false
      fetchDishList()
    }
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  }
}

onMounted(() => {
  fetchDishList()
})
</script>

<style scoped lang="less">
@import '@/assets/css/nordic-theme.less';

.dish-audit-container {
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

  .search-card {
    margin-bottom: var(--space-8);
  }

  .table-card {
    .pagination-container {
      margin-top: var(--space-5);
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
