<template>
  <div class="merchant-management-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>商家管理</h1>
      <p class="subtitle">管理系统所有商家</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card">
        <el-statistic title="总商家数" :value="stats.total" />
      </el-card>
      <el-card class="stat-card">
        <el-statistic title="营业中" :value="stats.active" />
      </el-card>
      <el-card class="stat-card">
        <el-statistic title="待审核" :value="stats.pending" />
      </el-card>
      <el-card class="stat-card">
        <el-statistic title="已暂停" :value="stats.inactive" />
      </el-card>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索商家名称、地址"
            clearable
            style="width: 250px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="营业中" value="ACTIVE" />
            <el-option label="待审核" value="PENDING" />
            <el-option label="已暂停" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 商家列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="merchantList" v-loading="loading" stripe>
        <el-table-column prop="merchantId" label="商家ID" width="100" />
        <el-table-column prop="name" label="商家名称" min-width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleView(row)">查看</el-button>
            <el-button type="warning" size="small" link @click="handleAudit(row)" v-if="row.status === 'PENDING'">审核</el-button>
            <el-button type="info" size="small" link @click="handleEditStatus(row)" v-else>状态</el-button>
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
          @size-change="fetchMerchantList"
          @current-change="fetchMerchantList"
        />
      </div>
    </el-card>

    <!-- 商家详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="商家详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-descriptions v-if="currentMerchant" :column="2" border>
        <el-descriptions-item label="商家ID">{{ currentMerchant.merchantId }}</el-descriptions-item>
        <el-descriptions-item label="商家名称">{{ currentMerchant.name }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentMerchant.status)">
            {{ getStatusText(currentMerchant.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentMerchant.phone }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ currentMerchant.address }}</el-descriptions-item>
        <el-descriptions-item label="营业时间" :span="2">{{ currentMerchant.businessHours || '-' }}</el-descriptions-item>
        <el-descriptions-item label="商家简介" :span="2">{{ currentMerchant.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间" :span="2">{{ currentMerchant.createTime }}</el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleAudit(currentMerchant)" v-if="currentMerchant.status === 'PENDING'">去审核</el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      title="商家审核"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.status">
            <el-radio label="APPROVED">通过</el-radio>
            <el-radio label="REJECTED">拒绝</el-radio>
          </el-radio-group>
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
        <el-button type="primary" @click="submitAudit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import axios from 'axios'

const loading = ref(false)
const merchantList = ref([])
const currentMerchant = ref(null)
const detailDialogVisible = ref(false)
const auditDialogVisible = ref(false)

const stats = reactive({
  total: 0,
  active: 0,
  pending: 0,
  inactive: 0
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
  merchantId: null,
  status: 'APPROVED',
  reason: ''
})

// 获取商家列表
const fetchMerchantList = async () => {
  loading.value = true
  try {
    const response = await axios.get('http://localhost:8080/api/admin/merchants', {
      params: {
        page: pagination.page,
        pageSize: pagination.pageSize,
        keyword: searchForm.keyword,
        status: searchForm.status
      }
    })

    if (response.data) {
      merchantList.value = response.data.records || []
      pagination.total = response.data.total || 0

      // 更新统计数据
      stats.total = response.data.total || 0
      stats.active = merchantList.value.filter(m => m.status === 'ACTIVE').length
      stats.pending = merchantList.value.filter(m => m.status === 'PENDING').length
      stats.inactive = merchantList.value.filter(m => m.status === 'INACTIVE').length
    }
  } catch (error) {
    console.error('获取商家列表失败:', error)
    ElMessage.error('获取商家列表失败')
  } finally {
    loading.value = false
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    'ACTIVE': 'success',
    'PENDING': 'warning',
    'INACTIVE': 'info',
    'REJECTED': 'danger'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    'ACTIVE': '营业中',
    'PENDING': '待审核',
    'INACTIVE': '已暂停',
    'REJECTED': '已拒绝'
  }
  return texts[status] || '未知'
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchMerchantList()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  pagination.page = 1
  fetchMerchantList()
}

// 查看商家详情
const handleView = (row) => {
  currentMerchant.value = row
  detailDialogVisible.value = true
}

// 审核
const handleAudit = (row) => {
  auditForm.merchantId = row.merchantId
  auditForm.status = 'APPROVED'
  auditForm.reason = ''
  auditDialogVisible.value = true
}

// 提交审核
const submitAudit = async () => {
  try {
    const response = await axios.put(
      `http://localhost:8080/api/admin/merchants/${auditForm.merchantId}/audit`,
      {
        status: auditForm.status,
        reason: auditForm.reason
      }
    )

    if (response.data?.success) {
      ElMessage.success('审核完成')
      auditDialogVisible.value = false
      detailDialogVisible.value = false
      fetchMerchantList()
    } else {
      ElMessage.error(response.data?.message || '审核失败')
    }
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  }
}

// 修改状态
const handleEditStatus = async (row) => {
  try {
    await ElMessageBox.prompt('请输入新状态（ACTIVE/INACTIVE）', '修改状态', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: row.status
    })

    ElMessage.info('状态修改功能开发中...')
    // TODO: 实现状态修改
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  fetchMerchantList()
})
</script>

<style scoped lang="less">
.merchant-management-container {
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
}
</style>
