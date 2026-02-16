<template>
  <div class="dish-management-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>菜品管理</h1>
      <p class="subtitle">管理系统所有菜品</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card">
        <el-statistic title="菜品总数" :value="stats.total" />
      </el-card>
      <el-card class="stat-card">
        <el-statistic title="已上架" :value="stats.active" />
      </el-card>
      <el-card class="stat-card">
        <el-statistic title="已下架" :value="stats.inactive" />
      </el-card>
      <el-card class="stat-card">
        <el-statistic title="待审核" :value="stats.pending" />
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
        <el-form-item label="菜品分类">
          <el-select v-model="searchForm.category" placeholder="全部" clearable style="width: 140px">
            <el-option label="主食" value="主食" />
            <el-option label="菜品" value="菜品" />
            <el-option label="汤品" value="汤品" />
            <el-option label="甜品" value="甜品" />
            <el-option label="饮品" value="饮品" />
            <el-option label="小吃" value="小吃" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="已上架" value="ACTIVE" />
            <el-option label="已下架" value="INACTIVE" />
            <el-option label="待审核" value="PENDING" />
          </el-select>
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
        <el-table-column prop="dishId" label="菜品ID" width="100" />
        <el-table-column prop="dishName" label="菜品名称" min-width="150" />
        <el-table-column prop="merchantName" label="商家名称" min-width="150" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleView(row)">查看</el-button>
            <el-button
              :type="isActiveStatus(row.status, row.statusCode) ? 'warning' : 'success'"
              size="small"
              link
              @click="handleToggleStatus(row)"
            >
              {{ isActiveStatus(row.status, row.statusCode) ? '下架' : '上架' }}
            </el-button>
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

    <!-- 菜品详情对话框 -->
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
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentDish.status)">
            {{ getStatusText(currentDish.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="热量">{{ currentDish.calories || '-' }} kcal</el-descriptions-item>
        <el-descriptions-item label="库存">{{ currentDish.stock || '不限量' }}</el-descriptions-item>
        <el-descriptions-item label="销量">{{ currentDish.sales || 0 }}</el-descriptions-item>
        <el-descriptions-item label="评分">{{ currentDish.rating || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentDish.createTime }}</el-descriptions-item>
        <el-descriptions-item label="菜品描述" :span="2">{{ currentDish.description || '-' }}</el-descriptions-item>
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
          :type="currentDish?.status === 'ACTIVE' ? 'warning' : 'success'"
          @click="handleToggleStatus(currentDish)"
        >
          {{ currentDish?.status === 'ACTIVE' ? '下架' : '上架' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getDishList, getDishDetail, updateDishStatus } from '@/api/admin'

const loading = ref(false)
const dishList = ref([])
const currentDish = ref(null)
const detailDialogVisible = ref(false)

const stats = reactive({
  total: 0,
  active: 0,
  inactive: 0,
  pending: 0
})

const searchForm = reactive({
  keyword: '',
  category: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 获取菜品列表
const fetchDishList = async () => {
  loading.value = true
  try {
    console.log('[菜品管理] 获取菜品列表, 页码:', pagination.page, '每页:', pagination.pageSize)
    const response = await getDishList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      category: searchForm.category,
      status: searchForm.status
    })

    if (response) {
      dishList.value = response.records || []
      pagination.total = response.total || 0

      // 更新统计数据
      stats.total = pagination.total
      stats.active = dishList.value.filter(d => d.status === 'ACTIVE').length
      stats.inactive = dishList.value.filter(d => d.status === 'INACTIVE').length
      stats.pending = dishList.value.filter(d => d.status === 'PENDING').length
      console.log('[菜品管理] 获取菜品列表成功, 总数:', pagination.total)
    }
  } catch (error) {
    console.error('[菜品管理] 获取菜品列表失败:', error)
    ElMessage.error('获取菜品列表失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

// 获取状态类型
const getStatusType = (status) => {
  // 如果是Boolean类型，转换为状态代码
  if (typeof status === 'boolean') {
    return status ? 'success' : 'info'
  }

  // 如果是String类型，使用映射
  const types = {
    'ACTIVE': 'success',
    'INACTIVE': 'info',
    'PENDING': 'warning'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  // 如果是Boolean类型，转换为文本
  if (typeof status === 'boolean') {
    return status ? '已上架' : '已下架'
  }

  // 如果是String类型，使用映射
  const texts = {
    'ACTIVE': '已上架',
    'INACTIVE': '已下架',
    'PENDING': '待审核'
  }
  return texts[status] || '未知'
}

// 判断是否为上架状态（支持Boolean和String两种类型）
const isActiveStatus = (status, statusCode) => {
  // 优先使用statusCode
  if (statusCode === 'ACTIVE') return true
  if (statusCode === 'INACTIVE') return false

  // 如果status是Boolean类型
  if (typeof status === 'boolean') {
    return status
  }

  // 如果status是String类型
  return status === 'ACTIVE'
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchDishList()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.category = ''
  searchForm.status = ''
  pagination.page = 1
  fetchDishList()
}

// 查看菜品详情
const handleView = async (row) => {
  try {
    console.log('[菜品管理] 查看菜品详情, 菜品ID:', row.dishId)
    const response = await getDishDetail(row.dishId)

    if (response.success || response.data) {
      currentDish.value = response.data || response
      detailDialogVisible.value = true
      console.log('[菜品管理] 获取菜品详情成功')
    } else {
      ElMessage.error(response.message || '获取菜品详情失败')
    }
  } catch (error) {
    console.error('[菜品管理] 获取菜品详情失败:', error)
    ElMessage.error('获取菜品详情失败: ' + (error.message || '网络错误'))
  }
}

// 切换状态
const handleToggleStatus = async (row) => {
  // 判断是否为上架状态（支持Boolean和String两种类型）
  const isActive = isActiveStatus(row.status, row.statusCode)
  const actionText = isActive ? '下架' : '上架'

  try {
    await ElMessageBox.confirm(`确定要${actionText}该菜品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const newStatus = isInactive ? 'INACTIVE' : 'ACTIVE'
    console.log('[菜品管理] 修改菜品状态, 菜品ID:', row.dishId, '新状态:', newStatus)

    const response = await updateDishStatus(row.dishId, newStatus)

    if (response.success) {
      ElMessage.success(`${actionText}成功`)
      detailDialogVisible.value = false
      fetchDishList()
      console.log('[菜品管理] 修改菜品状态成功')
    } else {
      ElMessage.error(response.message || `${actionText}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('[菜品管理] 修改菜品状态失败:', error)
      ElMessage.error(`${actionText}失败: ` + (error.message || '网络错误'))
    }
  }
}

onMounted(() => {
  fetchDishList()
})
</script>

<style scoped lang="less">
.dish-management-container {
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
