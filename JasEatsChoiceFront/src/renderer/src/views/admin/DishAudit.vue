<template>
  <div class="dish-audit-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>菜品审核</h1>
      <p class="subtitle">审核商家提交的菜品</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card">
        <el-statistic title="待审核" :value="stats.pending" />
      </el-card>
      <el-card class="stat-card">
        <el-statistic title="今日已审核" :value="stats.todayApproved" />
      </el-card>
      <el-card class="stat-card">
        <el-statistic title="今日已拒绝" :value="stats.todayRejected" />
      </el-card>
      <el-card class="stat-card">
        <el-statistic title="本月审核总数" :value="stats.monthTotal" />
      </el-card>
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

    <!-- 审核列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="auditList" v-loading="loading" stripe>
        <el-table-column prop="dishId" label="菜品ID" width="100" />
        <el-table-column prop="dishName" label="菜品名称" min-width="150" />
        <el-table-column prop="merchantName" label="商家名称" min-width="150" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getAuditStatusType(row.auditStatus)">
              {{ getAuditStatusText(row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleAudit(row)">审核</el-button>
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
          @size-change="fetchAuditList"
          @current-change="fetchAuditList"
        />
      </div>
    </el-card>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      title="菜品审核"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="currentDish" class="audit-content">
        <el-descriptions :column="2" border class="dish-info">
          <el-descriptions-item label="菜品名称">{{ currentDish.dishName }}</el-descriptions-item>
          <el-descriptions-item label="商家名称">{{ currentDish.merchantName }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ currentDish.category || '-' }}</el-descriptions-item>
          <el-descriptions-item label="价格">
            <span style="color: #f56c6c; font-weight: bold">¥{{ currentDish.price }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="热量">{{ currentDish.calories || '-' }} kcal</el-descriptions-item>
          <el-descriptions-item label="库存">{{ currentDish.stock || '不限量' }}</el-descriptions-item>
          <el-descriptions-item label="提交时间" :span="2">{{ currentDish.submitTime }}</el-descriptions-item>
          <el-descriptions-item label="菜品描述" :span="2">{{ currentDish.description || '-' }}</el-descriptions-item>
          <el-descriptions-item label="菜品图片" :span="2">
            <el-image
              v-if="currentDish.image"
              :src="currentDish.image"
              style="width: 200px; height: 200px"
              fit="cover"
              :preview-src-list="[currentDish.image]"
            />
            <span v-else>-</span>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider>审核操作</el-divider>
        <el-form :model="auditForm" label-width="80px">
          <el-form-item label="审核结果">
            <el-radio-group v-model="auditForm.decision">
              <el-radio label="approve">通过</el-radio>
              <el-radio label="reject">拒绝</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="审核意见">
            <el-input
              v-model="auditForm.comment"
              type="textarea"
              :rows="4"
              placeholder="请输入审核意见（拒绝时必填）"
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">提交审核</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="菜品详情"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-descriptions v-if="currentDish" :column="2" border>
        <el-descriptions-item label="菜品ID">{{ currentDish.dishId }}</el-descriptions-item>
        <el-descriptions-item label="菜品名称">{{ currentDish.dishName }}</el-descriptions-item>
        <el-descriptions-item label="商家名称">{{ currentDish.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentDish.category || '-' }}</el-descriptions-item>
        <el-descriptions-item label="价格">
          <span style="color: #f56c6c; font-weight: bold">¥{{ currentDish.price }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="getAuditStatusType(currentDish.auditStatus)">
            {{ getAuditStatusText(currentDish.auditStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="热量">{{ currentDish.calories || '-' }} kcal</el-descriptions-item>
        <el-descriptions-item label="库存">{{ currentDish.stock || '不限量' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间" :span="2">{{ currentDish.submitTime }}</el-descriptions-item>
        <el-descriptions-item label="菜品描述" :span="2">{{ currentDish.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2">{{ currentDish.auditComment || '-' }}</el-descriptions-item>
        <el-descriptions-item label="菜品图片" :span="2">
          <el-image
            v-if="currentDish.image"
            :src="currentDish.image"
            style="width: 100px; height: 100px"
            fit="cover"
            :preview-src-list="[currentDish.image]"
          />
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="currentDish?.auditStatus === 'PENDING'"
          type="primary"
          @click="detailDialogVisible = false; handleAudit(currentDish)"
        >
          去审核
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
const auditList = ref([])
const currentDish = ref(null)
const auditDialogVisible = ref(false)
const detailDialogVisible = ref(false)

const stats = reactive({
  pending: 0,
  todayApproved: 0,
  todayRejected: 0,
  monthTotal: 0
})

const searchForm = reactive({
  keyword: '',
  auditStatus: 'PENDING',
  dateRange: null
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const auditForm = reactive({
  decision: 'approve',
  comment: ''
})

// 获取审核列表
const fetchAuditList = async () => {
  loading.value = true
  try {
    const response = await api.get('http://localhost:8080/api/admin/dishes/audit', {
      params: {
        page: pagination.page,
        pageSize: pagination.pageSize,
        keyword: searchForm.keyword,
        auditStatus: searchForm.auditStatus
      }
    })

    if (response) {
      auditList.value = response.records || []
      pagination.total = response.total || 0

      // 更新统计数据
      stats.pending = auditList.value.filter(d => d.auditStatus === 'PENDING').length
    }
  } catch (error) {
    console.error('获取审核列表失败:', error)
    ElMessage.error('获取审核列表失败')
  } finally {
    loading.value = false
  }
}

// 获取审核状态类型
const getAuditStatusType = (status) => {
  const types = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return types[status] || 'info'
}

// 获取审核状态文本
const getAuditStatusText = (status) => {
  const texts = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return texts[status] || '未知'
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchAuditList()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.auditStatus = 'PENDING'
  searchForm.dateRange = null
  pagination.page = 1
  fetchAuditList()
}

// 查看详情
const handleView = async (row) => {
  try {
    const response = await api.get(`http://localhost:8080/api/admin/dishes/audit/${row.dishId}`)
    if (response) {
      currentDish.value = response
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// 审核
const handleAudit = async (row) => {
  try {
    const response = await api.get(`http://localhost:8080/api/admin/dishes/audit/${row.dishId}`)
    if (response) {
      currentDish.value = response
      auditForm.decision = 'approve'
      auditForm.comment = ''
      auditDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取菜品信息失败:', error)
    ElMessage.error('获取菜品信息失败')
  }
}

// 提交审核
const submitAudit = async () => {
  if (auditForm.decision === 'reject' && !auditForm.comment.trim()) {
    ElMessage.warning('拒绝时必须填写审核意见')
    return
  }

  try {
    const response = await api.post(
      `http://localhost:8080/api/admin/dishes/audit/${currentDish.value.dishId}`,
      {
        decision: auditForm.decision,
        comment: auditForm.comment
      }
    )

    if (response) {
      ElMessage.success('审核提交成功')
      auditDialogVisible.value = false
      fetchAuditList()
    }
  } catch (error) {
    console.error('审核提交失败:', error)
    ElMessage.error('审核提交失败')
  }
}

onMounted(() => {
  fetchAuditList()
})
</script>

<style scoped lang="less">
.dish-audit-container {
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

  .audit-content {
    .dish-info {
      margin-bottom: 20px;
    }
  }
}
</style>
