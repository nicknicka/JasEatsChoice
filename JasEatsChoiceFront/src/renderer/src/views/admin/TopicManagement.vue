<template>
  <div class="topic-management-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #ecf5ff;">
              <el-icon :size="24" color="#409eff"><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.total || 0 }}</div>
              <div class="stat-label">总热点数</div>
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
              <div class="stat-value">{{ statistics.active || 0 }}</div>
              <div class="stat-label">生效中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #fef0f0;">
              <el-icon :size="24" color="#f56c6c"><Clock /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.pending || 0 }}</div>
              <div class="stat-label">待审核</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #fdf6ec;">
              <el-icon :size="24" color="#e6a23c"><View /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalClicks || 0 }}</div>
              <div class="stat-label">总点击量</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主内容卡片 -->
    <el-card shadow="never" class="main-card">
      <template #header>
        <div class="card-header">
          <h3>热点话题管理</h3>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            新增热点
          </el-button>
        </div>
      </template>

      <!-- 搜索筛选 -->
      <div class="filter-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="filters.keyword"
              placeholder="搜索热点内容"
              clearable
              @clear="fetchTopics"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="4">
            <el-select v-model="filters.status" placeholder="状态筛选" clearable @change="fetchTopics">
              <el-option label="生效中" value="ACTIVE" />
              <el-option label="未生效" value="INACTIVE" />
              <el-option label="已过期" value="EXPIRED" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-button type="primary" @click="fetchTopics">查询</el-button>
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
          <el-button size="small" @click="clearSelection">取消选择</el-button>
        </el-alert>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="topics"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="content" label="热点内容" min-width="300" show-overflow-tooltip />
        <el-table-column label="来源" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getSourceTypeTag(row.source_type)" size="small">
              {{ getSourceTypeName(row.source_type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优先级" width="80" align="center">
          <template #default="{ row }">
            <el-tag type="warning" size="small">{{ row.priority || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" size="small">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.require_review" :type="getReviewStatusTag(row.review_status)" size="small">
              {{ getReviewStatusName(row.review_status) }}
            </el-tag>
            <span v-else style="color: #909399; font-size: 12px;">无需审核</span>
          </template>
        </el-table-column>
        <el-table-column label="点击/分享" width="100" align="center">
          <template #default="{ row }">
            <span>{{ row.click_count || 0 }} / {{ row.share_count || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="生效时间" width="180" align="center">
          <template #default="{ row }">
            <div style="font-size: 12px;">
              <div>{{ formatDate(row.start_date) }}</div>
              <div style="color: #909399;">至</div>
              <div>{{ formatDate(row.end_date) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button-group>
              <el-button size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" type="primary" @click="handleReview(row)" v-if="row.require_review && row.review_status === 'PENDING'">
                审核
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
          @size-change="fetchTopics"
          @current-change="fetchTopics"
        />
      </div>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="showDialog"
      :title="dialogMode === 'create' ? '新增热点' : '编辑热点'"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="热点内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请输入热点内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number v-model="form.priority" :min="0" :max="999" />
          <span style="margin-left: 10px; color: #909399; font-size: 12px;">数值越大优先级越高</span>
        </el-form-item>
        <el-form-item label="来源类型" prop="source_type">
          <el-select v-model="form.source_type">
            <el-option label="手动设置" value="MANUAL" />
            <el-option label="来自教程" value="TUTORIAL" />
            <el-option label="AI生成" value="AI" />
            <el-option label="第三方API" value="API" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源ID" prop="source_id">
          <el-input v-model="form.source_id" placeholder="可选，关联的教程ID等" />
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
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="ACTIVE">生效</el-radio>
            <el-radio label="INACTIVE">未生效</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="需要审核" prop="require_review">
          <el-switch v-model="form.require_review" />
          <span style="margin-left: 10px; color: #909399; font-size: 12px;">
            开启后需要管理员审核才能生效
          </span>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="2"
            placeholder="可选，备注信息"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="showReviewDialog"
      title="审核热点"
      width="500px"
    >
      <el-form :model="reviewForm" ref="reviewFormRef" label-width="100px">
        <el-form-item label="热点内容">
          <div style="padding: 10px; background: #f5f7fa; border-radius: 4px;">
            {{ currentTopic?.content }}
          </div>
        </el-form-item>
        <el-form-item label="审核决定" prop="approved">
          <el-radio-group v-model="reviewForm.approved">
            <el-radio :label="true">通过</el-radio>
            <el-radio :label="false">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见" prop="comment">
          <el-input
            v-model="reviewForm.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入审核意见"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showReviewDialog = false">取消</el-button>
        <el-button type="primary" @click="handleReviewSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document, SuccessFilled, Clock, View, Plus, Search,
  Edit, Delete, Check, Close
} from '@element-plus/icons-vue'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'

// 数据
const topics = ref([])
const loading = ref(false)
const selectedIds = ref([])

// 统计数据
const statistics = ref({
  total: 0,
  active: 0,
  pending: 0,
  totalClicks: 0,
  totalShares: 0
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
const dialogMode = ref('create')
const formRef = ref(null)

// 表单数据
const form = reactive({
  content: '',
  priority: 0,
  source_type: 'MANUAL',
  source_id: '',
  dateRange: null,
  status: 'INACTIVE',
  require_review: false,
  remark: ''
})

// 表单验证规则
const rules = {
  content: [{ required: true, message: '请输入热点内容', trigger: 'blur' }],
  source_type: [{ required: true, message: '请选择来源类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 审核对话框
const showReviewDialog = ref(false)
const currentTopic = ref(null)
const reviewForm = reactive({
  approved: true,
  comment: ''
})
const reviewFormRef = ref(null)

// 获取热点列表
const fetchTopics = async () => {
  loading.value = true
  try {
    const response = await api.get(API_CONFIG.admin.hotTopics, {
      params: {
        page: pagination.page,
        size: pagination.size,
        status: filters.status || undefined
      }
    })

    console.log('热点列表响应:', response)

    // 处理分页响应
    if (response && response.records) {
      topics.value = response.records
      pagination.total = response.total || 0
    } else if (Array.isArray(response)) {
      topics.value = response
      pagination.total = response.length
    }
  } catch (error) {
    console.error('获取热点列表失败:', error)
    ElMessage.error('获取热点列表失败')
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const response = await api.get(API_CONFIG.admin.hotTopicStatistics)
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
    content: '',
    priority: 0,
    source_type: 'MANUAL',
    source_id: '',
    dateRange: null,
    status: 'INACTIVE',
    require_review: false,
    remark: ''
  })
  formRef.value?.clearValidate()
}

// 处理编辑
const handleEdit = (row) => {
  dialogMode.value = 'edit'
  Object.assign(form, {
    id: row.id,
    content: row.content,
    priority: row.priority || 0,
    source_type: row.source_type,
    source_id: row.source_id || '',
    dateRange: row.start_date && row.end_date
      ? [row.start_date, row.end_date]
      : null,
    status: row.status,
    require_review: row.require_review || false,
    remark: row.remark || ''
  })
  showDialog.value = true
}

// 提交表单
const handleSubmit = async () => {
  await formRef.value.validate()

  const data = {
    content: form.content,
    priority: form.priority,
    source_type: form.source_type,
    source_id: form.source_id || undefined,
    start_date: form.dateRange?.[0] || undefined,
    end_date: form.dateRange?.[1] || undefined,
    status: form.status,
    require_review: form.require_review,
    remark: form.remark || undefined
  }

  try {
    let url, method
    if (dialogMode.value === 'create') {
      url = API_CONFIG.admin.hotTopicCreate
      method = 'post'
    } else {
      url = `${API_CONFIG.admin.hotTopicUpdate}/${form.id}`
      method = 'put'
    }

    const response = await api[method](url, data)

    if (response?.success !== false) {
      ElMessage.success(dialogMode.value === 'create' ? '创建成功' : '更新成功')
      showDialog.value = false
      fetchTopics()
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
    await ElMessageBox.confirm('确定要删除该热点吗？', '提示', {
      type: 'warning'
    })

    const response = await api.delete(`${API_CONFIG.admin.hotTopicDelete}/${row.id}`)

    if (response?.success !== false) {
      ElMessage.success('删除成功')
      fetchTopics()
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
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个热点吗？`, '批量删除', {
      type: 'warning'
    })

    const response = await api.delete(API_CONFIG.admin.hotTopicBatchDelete, {
      data: selectedIds.value
    })

    if (response?.success !== false) {
      ElMessage.success('批量删除成功')
      clearSelection()
      fetchTopics()
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

// 处理选择
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 清除选择
const clearSelection = () => {
  selectedIds.value = []
}

// 处理审核
const handleReview = (row) => {
  currentTopic.value = row
  reviewForm.approved = true
  reviewForm.comment = ''
  showReviewDialog.value = true
}

// 提交审核
const handleReviewSubmit = async () => {
  try {
    const data = {
      reviewerId: 1, // TODO: 从登录信息获取
      approved: reviewForm.approved,
      comment: reviewForm.comment
    }

    const response = await api.post(`${API_CONFIG.admin.hotTopicReview}/${currentTopic.value.id}`, null, {
      params: data
    })

    if (response?.success !== false) {
      ElMessage.success(reviewForm.approved ? '审核通过' : '已拒绝')
      showReviewDialog.value = false
      fetchTopics()
      fetchStatistics()
    } else {
      throw new Error(response?.message || '审核失败')
    }
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '审核失败')
  }
}

// 重置筛选
const resetFilters = () => {
  filters.keyword = ''
  filters.status = ''
  fetchTopics()
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

// 获取来源类型标签样式
const getSourceTypeTag = (type) => {
  const map = {
    'MANUAL': '',
    'TUTORIAL': 'success',
    'AI': 'warning',
    'API': 'info'
  }
  return map[type] || ''
}

// 获取来源类型名称
const getSourceTypeName = (type) => {
  const map = {
    'MANUAL': '手动',
    'TUTORIAL': '教程',
    'AI': 'AI',
    'API': 'API'
  }
  return map[type] || type
}

// 获取状态标签样式
const getStatusTag = (status) => {
  const map = {
    'ACTIVE': 'success',
    'INACTIVE': 'info',
    'EXPIRED': 'danger'
  }
  return map[status] || ''
}

// 获取状态名称
const getStatusName = (status) => {
  const map = {
    'ACTIVE': '生效',
    'INACTIVE': '未生效',
    'EXPIRED': '已过期'
  }
  return map[status] || status
}

// 获取审核状态标签样式
const getReviewStatusTag = (status) => {
  const map = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return map[status] || ''
}

// 获取审核状态名称
const getReviewStatusName = (status) => {
  const map = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return map[status] || status
}

// 页面加载时获取数据
onMounted(() => {
  fetchTopics()
  fetchStatistics()
})
</script>

<style scoped lang="less">
.topic-management-container {
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

  :deep(.el-table) {
    border-radius: 8px;
    overflow: hidden;
  }

  :deep(.el-dialog__body) {
    padding: 20px;
  }
}
</style>
