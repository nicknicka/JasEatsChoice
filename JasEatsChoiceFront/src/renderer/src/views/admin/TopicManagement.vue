<template>
  <div class="topic-management-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background: #ecf5ff;">
              <el-icon :size="24" color="#409eff"><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">总热点数</div>
              <div class="stat-value">{{ statistics.total || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background: #f0f9ff;">
              <el-icon :size="24" color="#67c23a"><SuccessFilled /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">生效中</div>
              <div class="stat-value">{{ statistics.active || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background: #fef0f0;">
              <el-icon :size="24" color="#f56c6c"><Clock /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">待审核</div>
              <div class="stat-value">{{ statistics.pending || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background: #fdf6ec;">
              <el-icon :size="24" color="#e6a23c"><View /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">总点击量</div>
              <div class="stat-value">{{ statistics.totalClicks || 0 }}</div>
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
          <div class="header-actions">
            <el-button @click="handleExport">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
            <el-button type="primary" @click="showCreateDialog">
              <el-icon><Plus /></el-icon>
              新增热点
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索筛选 -->
      <div class="filter-section">
        <el-row :gutter="12" class="filter-row">
          <el-col :xs="24" :sm="24" :md="10" :lg="10" :xl="10">
            <el-input
              v-model="filters.keyword"
              placeholder="搜索热点内容"
              clearable
              @clear="handleSearchClear"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :xs="12" :sm="12" :md="5" :lg="5" :xl="5">
            <el-select v-model="filters.status" placeholder="状态" clearable @change="fetchTopics" style="width: 100%;">
              <el-option label="生效中" value="ACTIVE" />
              <el-option label="未生效" value="INACTIVE" />
              <el-option label="已过期" value="EXPIRED" />
            </el-select>
          </el-col>
          <el-col :xs="12" :sm="12" :md="5" :lg="5" :xl="5">
            <el-select v-model="filters.sourceType" placeholder="来源" clearable @change="fetchTopics" style="width: 100%;">
              <el-option label="手动设置" value="MANUAL" />
              <el-option label="来自教程" value="TUTORIAL" />
              <el-option label="AI生成" value="AI" />
              <el-option label="第三方API" value="API" />
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="24" :md="4" :lg="4" :xl="4">
            <div class="filter-buttons-inline">
              <el-button type="primary" @click="fetchTopics">
                <el-icon><Search /></el-icon> 查询
              </el-button>
              <el-button @click="resetFilters">
                <el-icon><RefreshLeft /></el-icon> 重置
              </el-button>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="12" class="filter-row-action">
          <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
            <el-date-picker
              v-model="filters.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              @change="fetchTopics"
              style="width: 50%;"
            />
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
          <el-button size="small" type="success" @click="batchUpdateStatus('ACTIVE')">批量启用</el-button>
          <el-button size="small" type="warning" @click="batchUpdateStatus('INACTIVE')">批量禁用</el-button>
          <el-button size="small" type="danger" @click="batchDelete">批量删除</el-button>
          <el-button size="small" @click="clearSelection">取消选择</el-button>
        </el-alert>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="topics"
        stripe
        @selection-change="handleSelectionChange"
        class="topic-table"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="content" label="热点内容" min-width="200" max-width="300">
          <template #default="{ row }">
            <div class="content-cell">
              <div class="content-text" :title="row.content">{{ row.content }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="来源" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getSourceTypeTag(row.sourceType)" size="small">
              {{ getSourceTypeName(row.sourceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优先级" width="80" align="center" sortable>
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
            <el-tag v-if="row.requireReview" :type="getReviewStatusTag(row.reviewStatus)" size="small">
              {{ getReviewStatusName(row.reviewStatus) }}
            </el-tag>
            <span v-else style="color: #909399; font-size: 12px;">无需审核</span>
          </template>
        </el-table-column>
        <el-table-column label="点击/分享" width="100" align="center">
          <template #default="{ row }">
            <span>{{ row.clickCount || 0 }} / {{ row.shareCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="生效时间" width="120" align="center">
          <template #default="{ row }">
            <div class="date-range">
              <div>{{ formatDate(row.startDate) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-dropdown @command="(cmd) => handleActionCommand(cmd, row)">
              <el-button size="small" type="primary">
                操作 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">
                    <el-icon><Edit /></el-icon> 编辑
                  </el-dropdown-item>
                  <el-dropdown-item command="toggleStatus" v-if="row.status === 'ACTIVE'">
                    <el-icon><CircleClose /></el-icon> 禁用
                  </el-dropdown-item>
                  <el-dropdown-item command="toggleStatus" v-else>
                    <el-icon><CircleCheck /></el-icon> 启用
                  </el-dropdown-item>
                  <el-dropdown-item command="review" v-if="row.requireReview && row.reviewStatus === 'PENDING'" divided>
                    <el-icon><Check /></el-icon> 审核
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided style="color: #f56c6c;">
                    <el-icon><Delete /></el-icon> 删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <div v-if="!loading && topics.length === 0" class="empty-state">
        <el-empty description="暂无热点数据">
          <el-button type="primary" @click="showCreateDialog">创建第一个热点</el-button>
        </el-empty>
      </div>

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
        <el-form-item label="来源类型" prop="sourceType">
          <el-select v-model="form.sourceType">
            <el-option label="手动设置" value="MANUAL" />
            <el-option label="来自教程" value="TUTORIAL" />
            <el-option label="AI生成" value="AI" />
            <el-option label="第三方API" value="API" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源ID" prop="sourceId">
          <el-input v-model="form.sourceId" placeholder="可选，关联的教程ID等" />
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
            clearable
            unlink-panels
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="ACTIVE">生效</el-radio>
            <el-radio label="INACTIVE">未生效</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="需要审核" prop="requireReview">
          <el-switch v-model="form.requireReview" />
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
          <div class="preview-content">
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
import { ref, onMounted, reactive, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document, SuccessFilled, Clock, View, Plus, Search,
  Edit, Delete, Check, Close, Download, ArrowDown,
  CircleClose, CircleCheck, RefreshLeft
} from '@element-plus/icons-vue'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'
import { debounce } from 'lodash-es'

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
  status: '',
  sourceType: '',
  dateRange: null
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
const showReviewDialog = ref(false)
const currentTopic = ref(null)

// 表单数据
const form = reactive({
  content: '',
  priority: 0,
  sourceType: 'MANUAL',
  sourceId: '',
  dateRange: null,
  status: 'INACTIVE',
  requireReview: false,
  remark: ''
})

// 表单验证规则
const rules = {
  content: [{ required: true, message: '请输入热点内容', trigger: 'blur' }],
  sourceType: [{ required: true, message: '请选择来源类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 审核表单
const reviewForm = reactive({
  approved: true,
  comment: ''
})
const reviewFormRef = ref(null)

// 获取热点列表
const fetchTopics = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }

    // 添加筛选条件
    if (filters.status) params.status = filters.status
    if (filters.sourceType) params.sourceType = filters.sourceType
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.dateRange && filters.dateRange.length === 2) {
      params.startDate = filters.dateRange[0]
      params.endDate = filters.dateRange[1]
    }

    const response = await api.get(API_CONFIG.admin.hotTopics, { params })

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

// 搜索防抖处理
const debouncedSearch = debounce(() => {
  pagination.page = 1
  fetchTopics()
}, 500)

// 监听搜索关键词变化
watch(() => filters.keyword, () => {
  debouncedSearch()
})

// 清除搜索
const handleSearchClear = () => {
  filters.keyword = ''
  fetchTopics()
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
    sourceType: 'MANUAL',
    sourceId: '',
    dateRange: null,
    status: 'INACTIVE',
    requireReview: false,
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
    sourceType: row.sourceType,
    sourceId: row.sourceId || '',
    dateRange: row.startDate && row.endDate
      ? [row.startDate, row.endDate]
      : null,
    status: row.status,
    requireReview: row.requireReview || false,
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
    sourceType: form.sourceType,
    sourceId: form.sourceId || undefined,
    startDate: form.dateRange?.[0] || undefined,
    endDate: form.dateRange?.[1] || undefined,
    status: form.status,
    requireReview: form.requireReview,
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
    // 从localStorage获取当前管理员ID
    const adminInfo = JSON.parse(localStorage.getItem('adminInfo') || '{}')
    const reviewerId = adminInfo.id || adminInfo.adminId || 1

    const data = {
      reviewerId,
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
  filters.sourceType = ''
  filters.dateRange = null
  pagination.page = 1
  fetchTopics()
}

// 处理操作命令
const handleActionCommand = (command, row) => {
  switch (command) {
    case 'edit':
      handleEdit(row)
      break
    case 'delete':
      handleDelete(row)
      break
    case 'toggleStatus':
      toggleStatus(row)
      break
    case 'review':
      handleReview(row)
      break
  }
}

// 切换状态
const toggleStatus = async (row) => {
  const newStatus = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  const statusText = newStatus === 'ACTIVE' ? '启用' : '禁用'

  try {
    await ElMessageBox.confirm(`确定要${statusText}该热点吗？`, '提示', {
      type: 'warning'
    })

    const response = await api.put(`${API_CONFIG.admin.hotTopicUpdate}/${row.id}`, {
      ...row,
      status: newStatus
    })

    if (response?.success !== false) {
      ElMessage.success(`${statusText}成功`)
      fetchTopics()
      fetchStatistics()
    } else {
      throw new Error(response?.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${statusText}失败:`, error)
      ElMessage.error(error.response?.data?.message || error.message || `${statusText}失败`)
    }
  }
}

// 批量更新状态
const batchUpdateStatus = async (status) => {
  const statusText = status === 'ACTIVE' ? '启用' : '禁用'

  try {
    await ElMessageBox.confirm(`确定要批量${statusText}选中的 ${selectedIds.value.length} 个热点吗？`, `批量${statusText}`, {
      type: 'warning'
    })

    // 循环调用单个更新接口
    const updatePromises = selectedIds.value.map(id =>
      api.put(`${API_CONFIG.admin.hotTopicUpdate}/${id}`, { status })
    )

    await Promise.all(updatePromises)

    ElMessage.success(`批量${statusText}成功`)
    clearSelection()
    fetchTopics()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`批量${statusText}失败:`, error)
      ElMessage.error(error.response?.data?.message || error.message || `批量${statusText}失败`)
    }
  }
}

// 导出数据
const handleExport = async () => {
  try {
    const params = {
      status: filters.status || undefined,
      sourceType: filters.sourceType || undefined,
      keyword: filters.keyword || undefined
    }

    if (filters.dateRange && filters.dateRange.length === 2) {
      params.startDate = filters.dateRange[0]
      params.endDate = filters.dateRange[1]
    }

    // 导出数据
    const response = await api.get(API_CONFIG.admin.hotTopics, {
      params: { ...params, page: 1, size: 10000 }
    })

    // 创建下载链接
    const data = response.records || response
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `热点话题_${new Date().toISOString().slice(0, 10)}.json`
    link.click()
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  // 只返回月日时分，节省空间
  return `${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

// 获取来源类型标签样式
const getSourceTypeTag = (type) => {
  const map = {
    'MANUAL': 'info',
    'TUTORIAL': 'success',
    'AI': 'warning',
    'API': 'primary'
  }
  return map[type] || 'info'
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
  return map[status] || 'info'
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
  return map[status] || 'info'
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
  background: #f5f7fa;
  min-height: 100vh;

  .stats-row {
    margin-bottom: 20px;

    .stat-card {
      transition: all 0.3s ease;
      cursor: pointer;
      height: 100%;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
      }

      :deep(.el-card__body) {
        padding: 20px;
      }
    }

    .stat-item {
      display: flex;
      align-items: center;
      gap: 16px;

      .stat-icon {
        width: 56px;
        height: 56px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        transition: transform 0.3s ease;
      }

      &:hover .stat-icon {
        transform: scale(1.1) rotate(5deg);
      }

      .stat-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 8px;

        .stat-label {
          font-size: 14px;
          color: #909399;
          font-weight: 500;
        }

        .stat-value {
          font-size: 32px;
          font-weight: 700;
          color: #303133;
          line-height: 1;
        }
      }
    }
  }

  .main-card {
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

    :deep(.el-card__header) {
      padding: 20px 24px;
      border-bottom: 1px solid #ebeef5;
    }

    :deep(.el-card__body) {
      padding: 24px;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      h3 {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
        color: #303133;
      }

      .header-actions {
        display: flex;
        gap: 12px;
      }
    }

    .filter-section {
      margin-bottom: 20px;
      padding: 16px;
      background: #fafafa;
      border-radius: 8px;
      border: 1px solid #ebeef5;

      .filter-row {
        margin-bottom: 12px;

        &:last-child {
          margin-bottom: 0;
        }
      }

      .filter-buttons-inline {
        display: flex;
        gap: 8px;
        align-items: center;
        height: 32px;

        .el-button {
          flex: 1;
        }
      }

      .filter-row-action {
        margin-top: 8px;
      }
    }

    .batch-actions {
      margin-bottom: 16px;

      :deep(.el-alert) {
        border-radius: 8px;
      }

      :deep(.el-alert__content) {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-wrap: wrap;
      }
    }

    .topic-table {
      border-radius: 8px;
      overflow: hidden;

      .content-cell {
        .content-text {
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          line-clamp: 2;
          -webkit-box-orient: vertical;
          line-height: 1.5;
          max-height: 3em;
        }
      }

      .date-range {
        font-size: 11px;
        line-height: 1.3;
        word-break: break-all;

        .date-divider {
          color: #909399;
          font-size: 10px;
          margin: 2px 0;
        }
      }
    }

    .empty-state {
      padding: 40px 0;
      text-align: center;
    }

    .pagination-container {
      margin-top: 24px;
      display: flex;
      justify-content: flex-end;
      padding-top: 16px;
      border-top: 1px solid #ebeef5;
    }
  }

  .preview-content {
    padding: 16px;
    background: #f5f7fa;
    border-radius: 8px;
    line-height: 1.6;
    color: #606266;
    white-space: pre-wrap;
    word-break: break-word;
  }

  :deep(.el-table) {
    border-radius: 8px;
    overflow: hidden;

    .el-table__header th {
      background-color: #fafafa;
      font-weight: 600;
    }
  }

  :deep(.el-dialog) {
    border-radius: 8px;
  }

  :deep(.el-dialog__body) {
    padding: 24px;
  }

  :deep(.el-dropdown) {
    .el-button {
      font-size: 13px;
    }
  }

  // 响应式优化
  @media (max-width: 1200px) {
    .stats-row {
      :deep(.el-col) {
        margin-bottom: 12px;
      }
    }

    .filter-section {
      .filter-row {
        > [class*="el-col-"] {
          margin-bottom: 12px;
        }
      }
    }
  }

  @media (max-width: 768px) {
    padding: 12px;

    .stats-row {
      :deep(.el-col) {
        margin-bottom: 12px;
      }
    }

    .main-card {
      :deep(.el-card__header) {
        padding: 16px;
      }

      :deep(.el-card__body) {
        padding: 16px;
      }

      .card-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;

        .header-actions {
          width: 100%;

          .el-button {
            flex: 1;
          }
        }
      }
    }
  }
}
</style>
