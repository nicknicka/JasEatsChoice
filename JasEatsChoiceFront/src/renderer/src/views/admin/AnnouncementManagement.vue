<template>
  <div class="announcement-management-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #ecf5ff;">
              <el-icon :size="24" color="#409eff"><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalCount || 0 }}</div>
              <div class="stat-label">总公告数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #f0f9ff;">
              <el-icon :size="24" color="#67c23a"><SuccessFilled /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.activeCount || 0 }}</div>
              <div class="stat-label">已发布</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #fef0f0;">
              <el-icon :size="24" color="#909399"><CircleClose /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.inactiveCount || 0 }}</div>
              <div class="stat-label">已下线</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #fdf6ec;">
              <el-icon :size="24" color="#e6a23c"><Calendar /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.todayNewCount || 0 }}</div>
              <div class="stat-label">今日新增</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主内容卡片 -->
    <el-card shadow="never" class="main-card">
      <template #header>
        <div class="card-header">
          <h3>系统公告管理</h3>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            发布公告
          </el-button>
        </div>
      </template>

      <!-- 搜索筛选 -->
      <div class="filter-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="filters.keyword"
              placeholder="搜索公告标题或内容"
              clearable
              @clear="fetchAnnouncements"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="4">
            <el-select v-model="filters.status" placeholder="状态筛选" clearable @change="fetchAnnouncements">
              <el-option label="已发布" value="active" />
              <el-option label="已下线" value="inactive" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-button type="primary" @click="fetchAnnouncements">查询</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 批量操作 -->
      <div v-if="selectedIds.length > 0" class="batch-actions">
        <el-alert
          :title="`已选择 ${selectedIds.length} 项`"
          type="info"
          :closable="false"
        >
          <el-button size="small" @click="batchDelete">批量删除</el-button>
          <el-button size="small" @click="batchOffline">批量下线</el-button>
          <el-button size="small" @click="clearSelection">取消选择</el-button>
        </el-alert>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="announcements"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" label="公告标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="公告内容" min-width="300" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="content-preview">{{ row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
              {{ row.status === 'active' ? '已发布' : '已下线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="生效时间" width="180" align="center">
          <template #default="{ row }">
            <div style="font-size: 12px;" v-if="row.start_time || row.end_time">
              <div>{{ formatDate(row.start_time) }}</div>
              <div style="color: #909399;">至</div>
              <div>{{ formatDate(row.end_time) }}</div>
            </div>
            <span v-else style="color: #909399; font-size: 12px;">永久有效</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.create_time) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="{ row }">
            <el-button-group>
              <el-button size="small" @click="handleView(row)">查看</el-button>
              <el-button size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button
                size="small"
                :type="row.status === 'active' ? 'warning' : 'success'"
                @click="handleToggleStatus(row)"
              >
                {{ row.status === 'active' ? '下线' : '发布' }}
              </el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchAnnouncements"
          @current-change="fetchAnnouncements"
        />
      </div>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="showDialog"
      :title="dialogMode === 'create' ? '发布公告' : '编辑公告'"
      width="700px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="公告标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入公告标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="请输入公告内容"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="生效时间" prop="dateRange">
          <el-date-picker
            v-model="form.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="可选，不设置则永久有效"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="active">立即发布</el-radio>
            <el-radio label="inactive">暂存草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="showViewDialog"
      title="公告详情"
      width="600px"
    >
      <div class="announcement-detail" v-if="currentAnnouncement">
        <h3 style="margin-top: 0; color: #303133;">{{ currentAnnouncement.title }}</h3>
        <el-divider />
        <div class="detail-info">
          <div class="info-item">
            <span class="label">状态：</span>
            <el-tag :type="currentAnnouncement.status === 'active' ? 'success' : 'info'" size="small">
              {{ currentAnnouncement.status === 'active' ? '已发布' : '已下线' }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="label">生效时间：</span>
            <span v-if="currentAnnouncement.start_time || currentAnnouncement.end_time">
              {{ formatDate(currentAnnouncement.start_time) }} 至 {{ formatDate(currentAnnouncement.end_time) }}
            </span>
            <span v-else style="color: #909399;">永久有效</span>
          </div>
          <div class="info-item">
            <span class="label">创建时间：</span>
            <span>{{ formatDateTime(currentAnnouncement.create_time) }}</span>
          </div>
        </div>
        <el-divider />
        <div class="content-detail">
          <div class="detail-label">公告内容：</div>
          <div class="content-text">{{ currentAnnouncement.content }}</div>
        </div>
      </div>

      <template #footer>
        <el-button type="primary" @click="showViewDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document, SuccessFilled, CircleClose, Calendar, Plus, Search,
  Edit, Delete
} from '@element-plus/icons-vue'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'

// 数据
const announcements = ref([])
const loading = ref(false)
const selectedIds = ref([])

// 统计数据
const statistics = ref({
  totalCount: 0,
  activeCount: 0,
  inactiveCount: 0,
  todayNewCount: 0
})

// 筛选条件
const filters = reactive({
  keyword: '',
  status: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 对话框
const showDialog = ref(false)
const showViewDialog = ref(false)
const dialogMode = ref('create')
const formRef = ref(null)
const currentAnnouncement = ref(null)

// 表单数据
const form = reactive({
  title: '',
  content: '',
  dateRange: null,
  status: 'active'
})

// 表单验证规则
const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 获取公告列表
const fetchAnnouncements = async () => {
  loading.value = true
  try {
    const response = await api.get(API_CONFIG.admin.announcements, {
      params: {
        page: pagination.page,
        pageSize: pagination.size,
        keyword: filters.keyword || undefined,
        status: filters.status || undefined
      }
    })

    console.log('公告列表响应:', response)

    // 处理分页响应
    if (response && response.records) {
      announcements.value = response.records
      pagination.total = response.total || 0
    } else if (Array.isArray(response)) {
      announcements.value = response
      pagination.total = response.length
    }
  } catch (error) {
    console.error('获取公告列表失败:', error)
    ElMessage.error('获取公告列表失败')
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const response = await api.get(API_CONFIG.admin.announcementStatistics)
    if (response && response.data) {
      Object.assign(statistics.value, response.data)
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  dialogMode.value = 'create'
  showDialog.value = true
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    title: '',
    content: '',
    dateRange: null,
    status: 'active'
  })
  formRef.value?.clearValidate()
}

// 查看详情
const handleView = (row) => {
  currentAnnouncement.value = row
  showViewDialog.value = true
}

// 处理编辑
const handleEdit = (row) => {
  dialogMode.value = 'edit'
  Object.assign(form, {
    id: row.id,
    title: row.title,
    content: row.content,
    dateRange: row.start_time && row.end_time
      ? [row.start_time, row.end_time]
      : null,
    status: row.status
  })
  showDialog.value = true
}

// 切换状态（发布/下线）
const handleToggleStatus = async (row) => {
  const newStatus = row.status === 'active' ? 'inactive' : 'active'
  const action = newStatus === 'active' ? '发布' : '下线'

  try {
    await ElMessageBox.confirm(`确定要${action}该公告吗？`, '提示', {
      type: 'warning'
    })

    const response = await api.put(`${API_CONFIG.admin.announcementUpdateStatus}/${row.id}`, {
      status: newStatus
    })

    if (response?.success !== false) {
      ElMessage.success(`${action}成功`)
      fetchAnnouncements()
      fetchStatistics()
    } else {
      throw new Error(response?.message || `${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${action}失败:`, error)
      ElMessage.error(error.response?.data?.message || error.message || `${action}失败`)
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  await formRef.value.validate()

  const data = {
    title: form.title,
    content: form.content,
    startTime: form.dateRange?.[0] || undefined,
    endTime: form.dateRange?.[1] || undefined,
    status: form.status
  }

  try {
    let url, method
    if (dialogMode.value === 'create') {
      url = API_CONFIG.admin.announcementCreate
      method = 'post'
    } else {
      url = `${API_CONFIG.admin.announcementUpdate}/${form.id}`
      method = 'put'
    }

    const response = await api[method](url, data)

    if (response?.success !== false) {
      ElMessage.success(dialogMode.value === 'create' ? '发布成功' : '更新成功')
      showDialog.value = false
      fetchAnnouncements()
      fetchStatistics()
    } else {
      throw new Error(response?.message || '操作失败')
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '操作失败')
  }
}

// 处理删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该公告吗？', '提示', {
      type: 'warning'
    })

    const response = await api.delete(`${API_CONFIG.admin.announcementDelete}/${row.id}`)

    if (response?.success !== false) {
      ElMessage.success('删除成功')
      fetchAnnouncements()
      fetchStatistics()
    } else {
      throw new Error(response?.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.response?.data?.message || error.message || '删除失败')
    }
  }
}

// 批量删除
const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个公告吗？`, '批量删除', {
      type: 'warning'
    })

    const response = await api.delete(API_CONFIG.admin.announcementBatchDelete, {
      data: selectedIds.value
    })

    if (response?.success !== false) {
      ElMessage.success('批量删除成功')
      clearSelection()
      fetchAnnouncements()
      fetchStatistics()
    } else {
      throw new Error(response?.message || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error(error.response?.data?.message || error.message || '批量删除失败')
    }
  }
}

// 批量下线
const batchOffline = async () => {
  try {
    await ElMessageBox.confirm(`确定要下线选中的 ${selectedIds.value.length} 个公告吗？`, '批量下线', {
      type: 'warning'
    })

    const promises = selectedIds.value.map(id =>
      api.put(`${API_CONFIG.admin.announcementUpdateStatus}/${id}`, { status: 'inactive' })
    )

    await Promise.all(promises)

    ElMessage.success('批量下线成功')
    clearSelection()
    fetchAnnouncements()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量下线失败:', error)
      ElMessage.error(error.response?.data?.message || error.message || '批量下线失败')
    }
  }
}

// 处理选择
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 清除选择
const clearSelection = () => {
  selectedIds.value = []
}

// 重置筛选
const resetFilters = () => {
  filters.keyword = ''
  filters.status = ''
  fetchAnnouncements()
}

// 格式化日期时间
const formatDateTime = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// 页面加载时获取数据
onMounted(() => {
  fetchAnnouncements()
  fetchStatistics()
})
</script>

<style scoped lang="less">
.announcement-management-container {
  padding: 20px;

  .stats-row {
    margin-bottom: 20px;

    .stat-item {
      display: flex;
      align-items: center;

      .stat-icon {
        width: 48px;
        height: 48px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 12px;
      }

      .stat-content {
        flex: 1;

        .stat-value {
          font-size: 24px;
          font-weight: 600;
          color: #303133;
          line-height: 1;
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
          margin-top: 4px;
        }
      }
    }
  }

  .main-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      h3 {
        margin: 0;
        font-size: 18px;
        color: #303133;
      }
    }

    .filter-section {
      margin-bottom: 20px;
    }

    .batch-actions {
      margin-bottom: 15px;
    }

    .pagination-container {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }

  .content-preview {
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    line-height: 1.5;
  }

  .announcement-detail {
    .detail-info {
      margin-bottom: 15px;

      .info-item {
        margin-bottom: 10px;
        font-size: 14px;

        .label {
          color: #909399;
          margin-right: 8px;
        }
      }
    }

    .content-detail {
      .detail-label {
        font-weight: 600;
        margin-bottom: 10px;
        color: #303133;
      }

      .content-text {
        padding: 15px;
        background: #f5f7fa;
        border-radius: 4px;
        line-height: 1.8;
        white-space: pre-wrap;
        word-break: break-word;
      }
    }
  }

  :deep(.el-table) {
    border-radius: 8px;
    overflow: hidden;
  }

  :deep(.el-dialog__body) {
    padding: 20px;
  }
}
</style>
