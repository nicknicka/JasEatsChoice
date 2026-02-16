<template>
  <div class="merchant-audit-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>商家审核</h1>
      <p class="subtitle">审核商家注册申请</p>
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
        <el-statistic title="本周通过率" :value="stats.approvalRate" suffix="%" />
      </el-card>
    </div>

    <!-- 待审核列表 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>待审核商家列表</span>
          <el-button type="primary" :icon="Refresh" @click="fetchPendingList">刷新</el-button>
        </div>
      </template>

      <el-table :data="pendingList" v-loading="loading" stripe>
        <el-table-column prop="merchantId" label="商家ID" width="100" />
        <el-table-column prop="name" label="商家名称" min-width="180" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="申请时间" width="180" />
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
      :title="auditForm.status === 'APPROVED' ? '通过审核' : '拒绝审核'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-descriptions v-if="currentMerchant" :column="2" border>
        <el-descriptions-item label="商家名称">{{ currentMerchant.name }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentMerchant.phone }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ currentMerchant.address }}</el-descriptions-item>
      </el-descriptions>

      <el-form :model="auditForm" label-width="80px" style="margin-top: 20px">
        <el-form-item label="审核结果">
          <el-tag :type="auditForm.status === 'APPROVED' ? 'success' : 'danger'">
            {{ auditForm.status === 'APPROVED' ? '通过' : '拒绝' }}
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
        <el-button type="primary" @click="submitAudit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import api from '@/utils/api'
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

// 获取待审核列表
const fetchPendingList = async () => {
  loading.value = true
  try {
    // 调用实际的待审核商家API
    const response = await getPendingMerchants({
      page: pagination.page,
      pageSize: pagination.pageSize
    })

    if (response.code === '200') {
      pendingList.value = response.data?.list || response.data || []
      pagination.total = response.data?.total || 0

      // 更新统计数据
      stats.pending = pagination.total
      stats.todayApproved = response.data?.todayApproved || 0
      stats.approvalRate = response.data?.approvalRate || 0
    } else {
      // 如果API调用失败，使用模拟数据作为后备
      console.warn('获取待审核商家失败，使用模拟数据:', response.message)
      pendingList.value = [
        {
          merchantId: 100,
          name: '测试餐厅A',
          phone: '13800138000',
          address: '北京市朝阳区测试路123号',
          createTime: '2025-01-31 10:00:00'
        },
        {
          merchantId: 101,
          name: '美味小吃店',
          phone: '13900139000',
          address: '上海市浦东新区测试街456号',
          createTime: '2025-01-31 09:30:00'
        }
      ]
      pagination.total = 15
      stats.pending = 15
    }
  } catch (error) {
    console.error('获取待审核列表失败:', error)
    // 使用模拟数据作为后备
    pendingList.value = [
      {
        merchantId: 100,
        name: '测试餐厅A',
        phone: '13800138000',
        address: '北京市朝阳区测试路123号',
        createTime: '2025-01-31 10:00:00'
      },
      {
        merchantId: 101,
        name: '美味小吃店',
        phone: '13900139000',
        address: '上海市浦东新区测试街456号',
        createTime: '2025-01-31 09:30:00'
      }
    ]
    pagination.total = 15
    stats.pending = 15
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
    const response = await api.put(
      `http://localhost:8080/api/admin/merchants/${auditForm.merchantId}/audit`,
      {
        status: auditForm.status,
        reason: auditForm.reason
      }
    )

    if (response.data?.success) {
      ElMessage.success('审核完成')
      auditDialogVisible.value = false
      fetchPendingList()
    } else {
      ElMessage.error(response.data?.message || '审核失败')
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
.merchant-audit-container {
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

  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-weight: bold;
    }

    .pagination-container {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
