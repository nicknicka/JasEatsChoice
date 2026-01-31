<template>
  <div class="user-management-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>用户管理</h1>
      <p class="subtitle">管理系统所有用户</p>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索用户名、手机号、邮箱"
            clearable
            style="width: 250px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button type="success" :icon="Download" @click="handleExport">导出Excel</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="userList" v-loading="loading" stripe>
        <el-table-column prop="userId" label="用户ID" width="200" />
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :size="40" :src="getAvatarUrl(row.avatar)" />
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="位置" width="100">
          <template #default="{ row }">
            <span>{{ row.location || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleView(row)">查看</el-button>
            <el-button type="warning" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
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
          @size-change="fetchUserList"
          @current-change="fetchUserList"
        />
      </div>
    </el-card>

    <!-- 用户详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="用户详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-descriptions v-if="currentUser" :column="2" border>
        <el-descriptions-item label="用户ID">{{ currentUser.userId }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ currentUser.nickname || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentUser.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentUser.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身高">{{ currentUser.height ? currentUser.height + 'cm' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="体重">{{ currentUser.weight ? currentUser.weight + 'kg' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="饮食目标">{{ currentUser.dietGoal || '-' }}</el-descriptions-item>
        <el-descriptions-item label="位置">{{ currentUser.location || '-' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间" :span="2">{{ currentUser.createTime }}</el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleEdit(currentUser)">编辑</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import { getUserList, getUserDetail, deleteUser } from '@/api/admin'
import { exportToExcel } from '@/utils/export.js'
import { getAvatarUrl } from '@/utils/avatar'

const loading = ref(false)
const userList = ref([])
const currentUser = ref(null)
const detailDialogVisible = ref(false)

const searchForm = reactive({
  keyword: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    console.log('[用户管理] 获取用户列表, 页码:', pagination.page, '每页:', pagination.pageSize)
    const response = await getUserList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword
    })

    console.log('[用户管理] API响应:', response)

    // api.js返回的是response.data，所以response就是IPage对象
    if (response) {
      userList.value = response.records || []
      pagination.total = response.total || 0
      console.log('[用户管理] 获取用户列表成功, 总数:', pagination.total, '记录数:', userList.value.length)
    }
  } catch (error) {
    console.error('[用户管理] 获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchUserList()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  pagination.page = 1
  fetchUserList()
}

// 导出数据
const handleExport = () => {
  const headers = [
    { key: 'userId', label: '用户ID', width: 25 },
    { key: 'nickname', label: '昵称', width: 15 },
    { key: 'phone', label: '手机号', width: 15 },
    { key: 'email', label: '邮箱', width: 25 },
    { key: 'location', label: '位置', width: 12 },
    { key: 'createTime', label: '注册时间', width: 20 }
  ]

  const success = exportToExcel(userList.value, {
    filename: '用户列表',
    headers
  })

  if (success) {
    ElMessage.success('导出成功')
  } else {
    ElMessage.error('导出失败')
  }
}

// 查看用户详情
const handleView = async (row) => {
  try {
    console.log('[用户管理] 查看用户详情, 用户ID:', row.userId)
    const response = await getUserDetail(row.userId)

    if (response.success && response.user) {
      currentUser.value = response.user
      detailDialogVisible.value = true
      console.log('[用户管理] 获取用户详情成功')
    } else {
      ElMessage.error(response.message || '获取用户详情失败')
    }
  } catch (error) {
    console.error('[用户管理] 获取用户详情失败:', error)
    ElMessage.error('获取用户详情失败: ' + (error.message || '网络错误'))
  }
}

// 编辑用户
const handleEdit = (row) => {
  ElMessage.info('编辑功能开发中...')
  // TODO: 实现编辑功能
}

// 删除用户
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.nickname || row.phone}" 吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    console.log('[用户管理] 删除用户, 用户ID:', row.userId)
    const response = await deleteUser(row.userId)

    if (response.success) {
      ElMessage.success('删除成功')
      fetchUserList()
      console.log('[用户管理] 删除用户成功')
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('[用户管理] 删除用户失败:', error)
      ElMessage.error('删除用户失败: ' + (error.message || '网络错误'))
    }
  }
}

onMounted(() => {
  fetchUserList()
})
</script>

<style scoped lang="less">
.user-management-container {
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
