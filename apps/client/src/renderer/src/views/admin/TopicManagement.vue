<template>
  <div class="topic-management-container">
    <AppPageHeader title="热点话题管理" subtitle="管理热点话题、优先级与审核状态" :compact="true">
      <template #actions>
        <el-button :icon="Download" @click="handleExport">导出</el-button>
        <el-button type="primary" :icon="Plus" @click="showCreateDialog">新增热点</el-button>
      </template>
    </AppPageHeader>

    <div class="stats-section">
      <AppStatCard label="总热点数" :value="statistics.total || 0" icon="Document" />
      <AppStatCard label="生效中" :value="statistics.active || 0" icon="CircleCheck" />
      <AppStatCard label="待审核" :value="statistics.pending || 0" icon="Clock" />
      <AppStatCard label="总点击量" :value="statistics.totalClicks || 0" icon="View" />
    </div>

    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="filters" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索热点内容"
            clearable
            style="width: 240px"
            @clear="handleSearchClear"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable style="width: 130px" @change="handleSearch">
            <el-option label="生效中" value="ACTIVE" />
            <el-option label="未生效" value="INACTIVE" />
            <el-option label="已过期" value="EXPIRED" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源">
          <el-select v-model="filters.sourceType" placeholder="全部" clearable style="width: 130px" @change="handleSearch">
            <el-option label="手动设置" value="MANUAL" />
            <el-option label="来自教程" value="TUTORIAL" />
            <el-option label="AI生成" value="AI" />
            <el-option label="第三方API" value="API" />
          </el-select>
        </el-form-item>
        <el-form-item label="生效日期">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 280px"
            @change="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshLeft" @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div v-if="selectedIds.length > 0" class="batch-actions">
        <el-alert :title="`已选择 ${selectedIds.length} 项`" type="info" :closable="false">
          <div class="batch-actions__buttons">
            <el-button :icon="CircleCheck" @click="batchUpdateStatus('ACTIVE')">批量启用</el-button>
            <el-button :icon="CircleClose" @click="batchUpdateStatus('INACTIVE')">批量禁用</el-button>
            <el-button type="danger" plain :icon="Delete" @click="batchDelete">批量删除</el-button>
            <el-button @click="clearSelection">取消选择</el-button>
          </div>
        </el-alert>
      </div>

      <AppEmptyState
        v-if="!loading && topics.length === 0"
        compact
        icon="Document"
        description="没有符合条件的热点话题，换个关键词或清空筛选试试"
        action-text="重置筛选"
        @action="resetFilters"
      />

      <template v-else>
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="topics"
          stripe
          class="topic-table"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="44" />
          <el-table-column prop="content" label="热点内容" min-width="220" show-overflow-tooltip />
          <el-table-column label="来源" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="getSourceTypeTag(row.sourceType)" size="small" effect="plain">
                {{ getSourceTypeName(row.sourceType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="priority" label="优先级" width="80" align="center" sortable>
            <template #default="{ row }">
              <span class="priority-value">{{ row.priority || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <StatusTag :status="row.status" :text="getStatusName(row.status)" />
            </template>
          </el-table-column>
          <el-table-column label="审核状态" width="95" align="center">
            <template #default="{ row }">
              <StatusTag
                v-if="row.requireReview"
                :status="row.reviewStatus"
                :text="getReviewStatusName(row.reviewStatus)"
              />
              <span v-else class="muted-text">无需审核</span>
            </template>
          </el-table-column>
          <el-table-column label="点击/分享" width="100" align="center">
            <template #default="{ row }">
              <span class="cell-num">{{ row.clickCount || 0 }} / {{ row.shareCount || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="生效时间" width="145" align="center">
            <template #default="{ row }">
              <span class="cell-date">{{ formatDate(row.startDate) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right" align="center">
            <template #default="{ row }">
              <RowActions
                :primary="{ text: '编辑', onClick: () => handleEdit(row) }"
                :items="rowActions(row)"
              />
            </template>
          </el-table-column>
        </el-table>

        <div v-if="topics.length > 0" class="pagination-container">
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
      </template>
    </el-card>

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
          <span class="helper-text">数值越大优先级越高</span>
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
          <span class="helper-text">开启后需要管理员审核才能生效</span>
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

    <el-dialog v-model="showReviewDialog" title="审核热点" width="500px">
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
import { Plus, Search, Download, CircleClose, CircleCheck, Delete, RefreshLeft } from '@element-plus/icons-vue'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import AppStatCard from '@/components/common/AppStatCard.vue'
import AppEmptyState from '@/components/common/AppEmptyState.vue'
import RowActions from '@/components/common/RowActions.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'
import { debounce } from 'lodash-es'

// 数据
const topics = ref([])
const loading = ref(false)
const selectedIds = ref([])
const tableRef = ref(null)

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
      pagination.total = response.total ?? 0
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

// 查询：统一回到第一页，避免筛选后停留在空页
const handleSearch = () => {
  pagination.page = 1
  fetchTopics()
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
  handleSearch()
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

// 处理删除：确认由 RowActions 的危险动作统一承担
const handleDelete = async (row) => {
  try {
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
  tableRef.value?.clearSelection()
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

// 行操作：审核优先占可见位；状态变更可见；删除进「更多」，由 RowActions 承担危险色与二次确认
const rowActions = (row) => {
  const actions = []

  if (row.requireReview && row.reviewStatus === 'PENDING') {
    actions.push({ text: '审核', onClick: () => handleReview(row) })
  }

  actions.push({
    text: row.status === 'ACTIVE' ? '禁用' : '启用',
    onClick: () => toggleStatus(row)
  })
  actions.push({ text: '删除', danger: true, onClick: () => handleDelete(row) })
  return actions
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

// 格式化日期：全站列表统一 YYYY-MM-DD HH:mm，禁止 ISO 的 T（§4.3）
const formatDate = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  if (Number.isNaN(d.getTime())) return '-'
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
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

// 获取状态名称
const getStatusName = (status) => {
  const map = {
    'ACTIVE': '生效',
    'INACTIVE': '未生效',
    'EXPIRED': '已过期'
  }
  return map[status] || status
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
  padding: var(--card-padding-compact) var(--page-gutter);
  background: var(--canvas);
  max-width: 1440px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;

  .stats-section {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: var(--card-gap-compact);
    margin-bottom: var(--space-8);
  }

  .search-card {
    margin-bottom: var(--space-8);

    .search-form {
      margin-bottom: 0;

      :deep(.el-form-item) {
        margin-right: var(--space-4);
        margin-bottom: var(--space-2);
      }
    }
  }

  .table-card {
    .batch-actions {
      margin-bottom: var(--space-4);

      :deep(.el-alert__content) {
        display: flex;
        flex-direction: column;
        gap: var(--space-3);
      }
    }

    .batch-actions__buttons {
      display: flex;
      flex-wrap: wrap;
      gap: var(--space-8);

      :deep(.el-button + .el-button) {
        margin-left: 0;
      }
    }

    .pagination-container {
      display: flex;
      justify-content: flex-end;
      margin-top: var(--space-5);
      padding-top: var(--space-4);
      border-top: 1px solid var(--line);
    }

    :deep(.el-table__header th) {
      background-color: var(--canvas-sunken);
      font-weight: var(--weight-semibold);
    }

    :deep(.el-table__row) {
      height: 44px;
    }

    :deep(.el-table__body tr:hover > td) {
      background-color: var(--canvas-sunken);
      transition: background-color var(--duration-fast) var(--ease-out);
    }
  }

  .priority-value,
  .cell-num {
    color: var(--ink-900);
    font-family: var(--font-num);
    font-variant-numeric: tabular-nums;
    font-weight: var(--weight-semibold);
    white-space: nowrap;
  }

  .cell-date {
    white-space: nowrap;
  }

  .muted-text {
    color: var(--ink-500);
    font-size: var(--text-body-sm);
    white-space: nowrap;
  }

  .helper-text {
    margin-left: var(--space-2);
    color: var(--ink-500);
    font-size: var(--text-body-sm);
  }

  .preview-content {
    padding: var(--space-4);
    background: var(--canvas-sunken);
    border-radius: var(--radius-md);
    color: var(--ink-700);
    line-height: 1.6;
    white-space: pre-wrap;
    word-break: break-word;
  }

  :deep(.el-dialog) {
    border-radius: var(--radius-md);
  }

  :deep(.el-dialog__body) {
    padding: var(--space-6);
  }

  @media (max-width: 960px) {
    .stats-section {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media (max-width: 640px) {
    padding: var(--space-3);

    .stats-section {
      grid-template-columns: 1fr;
    }

    .search-form {
      :deep(.el-form-item) {
        margin-right: 0;
      }
    }
  }
}
</style>
