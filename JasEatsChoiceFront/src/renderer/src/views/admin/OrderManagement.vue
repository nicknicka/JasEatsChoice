<template>
  <div class="order-management-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>订单管理</h1>
      <p class="subtitle">管理系统所有订单</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card">
        <el-statistic title="今日订单" :value="stats.today" />
      </el-card>
      <el-card class="stat-card">
        <el-statistic title="待处理" :value="stats.pending" />
      </el-card>
      <el-card class="stat-card">
        <el-statistic title="进行中" :value="stats.processing" />
      </el-card>
      <el-card class="stat-card">
        <el-statistic title="已完成" :value="stats.completed" />
      </el-card>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索订单号、用户名"
            clearable
            style="width: 250px"
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待处理" value="PENDING" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="制作中" value="PREPARING" />
            <el-option label="配送中" value="DELIVERING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
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

    <!-- 订单列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="orderList" v-loading="loading" stripe>
        <el-table-column prop="orderId" label="订单ID" width="100" />
        <el-table-column prop="userId" label="用户ID" width="150" />
        <el-table-column prop="merchantName" label="商家名称" min-width="150" />
        <el-table-column label="订单状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="订单金额" width="120">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleView(row)">查看</el-button>
            <el-button type="warning" size="small" link @click="handleUpdateStatus(row)">状态</el-button>
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
          @size-change="fetchOrderList"
          @current-change="fetchOrderList"
        />
      </div>
    </el-card>

    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
      width="900px"
      :close-on-click-modal="false"
    >
      <el-descriptions v-if="currentOrder" :column="2" border>
        <el-descriptions-item label="订单ID">{{ currentOrder.orderId }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentOrder.userId }}</el-descriptions-item>
        <el-descriptions-item label="商家名称">{{ currentOrder.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(currentOrder.status)">
            {{ getStatusText(currentOrder.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="订单金额">
          <span style="color: #f56c6c; font-weight: bold">¥{{ currentOrder.totalAmount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ currentOrder.paymentMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.deliveryAddress || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="下单时间" :span="2">{{ currentOrder.createTime }}</el-descriptions-item>
      </el-descriptions>

      <!-- 订单菜品 -->
      <el-divider content-position="left">订单菜品</el-divider>
      <el-table v-if="currentOrder?.dishes" :data="currentOrder.dishes" size="small">
        <el-table-column prop="dishName" label="菜品名称" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="price" label="单价" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column label="小计" width="100">
          <template #default="{ row }">¥{{ (row.price * row.quantity).toFixed(2) }}</template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleUpdateStatus(currentOrder)">修改状态</el-button>
      </template>
    </el-dialog>

    <!-- 状态修改对话框 -->
    <el-dialog
      v-model="statusDialogVisible"
      title="修改订单状态"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="statusForm" label-width="80px">
        <el-form-item label="当前状态">
          <el-tag :type="getStatusType(currentOrder?.status)">
            {{ getStatusText(currentOrder?.status) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="新状态">
          <el-select v-model="statusForm.status" placeholder="请选择新状态">
            <el-option label="待处理" value="PENDING" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="制作中" value="PREPARING" />
            <el-option label="配送中" value="DELIVERING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStatusUpdate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getOrderList, getOrderDetail } from '@/api/admin'

const loading = ref(false)
const orderList = ref([])
const currentOrder = ref(null)
const detailDialogVisible = ref(false)
const statusDialogVisible = ref(false)

const stats = reactive({
  today: 0,
  pending: 0,
  processing: 0,
  completed: 0
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

const statusForm = reactive({
  orderId: null,
  status: ''
})

// 获取订单列表
const fetchOrderList = async () => {
  loading.value = true
  try {
    console.log('[订单管理] 获取订单列表, 页码:', pagination.page, '每页:', pagination.pageSize)
    const response = await getOrderList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      status: searchForm.status
    })

    if (response.data) {
      orderList.value = response.data.records || []
      pagination.total = response.data.total || 0

      // 更新统计数据
      stats.pending = orderList.value.filter(o => o.status === 'PENDING').length
      stats.processing = orderList.value.filter(o =>
        ['CONFIRMED', 'PREPARING', 'DELIVERING'].includes(o.status)
      ).length
      stats.completed = orderList.value.filter(o => o.status === 'COMPLETED').length
      console.log('[订单管理] 获取订单列表成功, 总数:', pagination.total)
    }
  } catch (error) {
    console.error('[订单管理] 获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    'PENDING': 'warning',
    'CONFIRMED': 'primary',
    'PREPARING': 'info',
    'DELIVERING': 'primary',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    'PENDING': '待处理',
    'CONFIRMED': '已确认',
    'PREPARING': '制作中',
    'DELIVERING': '配送中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return texts[status] || '未知'
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchOrderList()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  searchForm.dateRange = null
  pagination.page = 1
  fetchOrderList()
}

// 查看订单详情
const handleView = async (row) => {
  try {
    console.log('[订单管理] 查看订单详情, 订单ID:', row.orderId)
    const response = await getOrderDetail(row.orderId)

    if (response.success || response.data) {
      currentOrder.value = response.data || response
      detailDialogVisible.value = true
      console.log('[订单管理] 获取订单详情成功')
    } else {
      ElMessage.error(response.message || '获取订单详情失败')
    }
  } catch (error) {
    console.error('[订单管理] 获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败: ' + (error.message || '网络错误'))
  }
}

// 修改状态
const handleUpdateStatus = (row) => {
  statusForm.orderId = row.orderId
  statusForm.status = row.status
  statusDialogVisible.value = true
}

// 提交状态修改
const submitStatusUpdate = async () => {
  try {
    const response = await api.put(
      `http://localhost:8080/api/admin/orders/${statusForm.orderId}/status`,
      { status: statusForm.status }
    )

    if (response) {
      ElMessage.success('状态修改成功')
      statusDialogVisible.value = false
      detailDialogVisible.value = false
      fetchOrderList()
    }
  } catch (error) {
    console.error('状态修改失败:', error)
    ElMessage.error('状态修改失败')
  }
}

onMounted(() => {
  fetchOrderList()
})
</script>

<style scoped lang="less">
.order-management-container {
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
