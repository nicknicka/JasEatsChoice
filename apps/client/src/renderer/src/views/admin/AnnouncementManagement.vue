<template>
  <div class="announcement-management-container">
    <!-- 页头：标题 + 唯一实心主操作（§4.1、§4.7） -->
    <AppPageHeader title="公告管理" subtitle="管理系统所有公告" :compact="true">
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="showCreateDialog">发布公告</el-button>
      </template>
    </AppPageHeader>

    <!-- 统计卡片：四卡一行，线性图标 + 数值 + 标签（§4.8） -->
    <div class="stats-section">
      <AppStatCard
        v-for="stat in statsConfig"
        :key="stat.key"
        :label="stat.label"
        :value="statistics[stat.key] || 0"
        :icon="stat.icon"
      />
    </div>

    <!-- 主内容卡片 -->
    <el-card shadow="never" class="main-card">
      <!-- 搜索筛选 -->
      <div class="filter-section">
        <el-form :inline="true" :model="filters" class="filter-form">
          <el-form-item label="关键词">
            <el-input
              v-model="filters.keyword"
              placeholder="搜索公告标题或内容"
              clearable
              style="width: 220px"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="公告类型">
            <el-select v-model="filters.type" placeholder="全部" clearable style="width: 130px">
              <el-option
                v-for="item in announcementTypes"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="优先级">
            <el-select v-model="filters.priority" placeholder="全部" clearable style="width: 130px">
              <el-option
                v-for="item in priorityOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="filters.status" placeholder="全部" clearable style="width: 130px">
              <el-option label="已发布" value="active" />
              <el-option label="草稿" value="draft" />
              <el-option label="已下线" value="inactive" />
            </el-select>
          </el-form-item>
          <el-form-item label="目标群体">
            <el-select
              v-model="filters.targetAudience"
              placeholder="全部"
              clearable
              style="width: 130px"
            >
              <el-option
                v-for="item in targetAudienceOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="创建时间">
            <el-date-picker
              v-model="filters.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 260px"
            />
          </el-form-item>
          <el-form-item>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button @click="resetFilters">
              <el-icon><RefreshLeft /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 批量操作 -->
      <div v-if="selectedRows.length > 0" class="batch-actions">
        <el-alert :title="`已选择 ${selectedRows.length} 项`" type="info" :closable="false">
          <template #default>
            <el-button size="small" link type="primary" @click="batchPublish">
              <el-icon><Promotion /></el-icon>
              批量发布
            </el-button>
            <el-button size="small" link type="warning" @click="batchOffline">
              <el-icon><RemoveFilled /></el-icon>
              批量下线
            </el-button>
            <el-button size="small" link type="danger" @click="batchDelete">
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
            <el-button size="small" link @click="clearSelection">
              <el-icon><Close /></el-icon>
              取消选择
            </el-button>
          </template>
        </el-alert>
      </div>

      <!-- 表格：紧凑列组合，保证 1440 宽度内所有表头完整可读（§4.3、§5.4） -->
      <AppEmptyState
        v-if="!loading && announcements.length === 0"
        compact
        :icon="Document"
        description="没有符合条件的公告，换个关键词或清空筛选试试"
        action-text="重置筛选"
        @action="resetFilters"
      />

      <template v-else>
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="announcements"
          stripe
          class="announcement-table"
          @selection-change="handleSelectionChange"
          @row-dblclick="handleEdit"
        >
          <el-table-column type="selection" width="44" fixed="left" />
          <el-table-column label="公告" min-width="260">
            <template #default="{ row }">
              <div class="announcement-cell">
                <div class="announcement-title">{{ row.title }}</div>
                <div class="content-preview">{{ row.content }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="分类" width="140">
            <template #default="{ row }">
              <div class="tag-stack">
                <StatusTag
                  v-if="row.priority"
                  :status="priorityStatus(row.priority)"
                  :text="priorityLabel(row.priority)"
                />
                <StatusTag :status="typeStatus(row.type)" :text="typeLabel(row.type)" />
              </div>
            </template>
          </el-table-column>
          <el-table-column label="目标群体" width="88">
            <template #default="{ row }">
              <StatusTag status="neutral" :text="targetAudienceLabel(row.targetAudience)" />
            </template>
          </el-table-column>
          <el-table-column label="阅读" width="80">
            <template #default="{ row }">
              <div v-if="row.readCount !== undefined" class="read-stats">
                <el-tooltip :content="`阅读人数: ${row.readUsers || 0}`" placement="top">
                  <div class="stat-item-inline">
                    <el-icon :size="14"><View /></el-icon>
                    <span>{{ formatNumber(row.readCount) }}</span>
                  </div>
                </el-tooltip>
              </div>
              <span v-else class="muted">-</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="72">
            <template #default="{ row }">
              <StatusTag :status="statusKey(row.status)" />
            </template>
          </el-table-column>
          <el-table-column label="时间" width="208">
            <template #default="{ row }">
              <div class="time-cell">
                <div>
                  <span class="time-label">生效</span>
                  <span v-if="row.startTime || row.endTime">
                    {{ formatDate(row.startTime) }} 至 {{ formatDate(row.endTime) }}
                  </span>
                  <span v-else>永久</span>
                </div>
                <div>
                  <span class="time-label">创建</span>
                  <span>{{ formatDateTime(row.createdAt) }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="176" fixed="right">
            <template #default="{ row }">
              <RowActions
                :primary="{ text: '查看', onClick: () => handleView(row) }"
                :items="rowActions(row)"
                :max-visible="1"
              />
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页：合计与实际记录数一致，空数据时隐藏（§4.3） -->
        <div v-if="announcements.length > 0" class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handlePageChange"
            @current-change="handlePageChange"
          />
        </div>
      </template>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="showDialog"
      :title="dialogMode === 'create' ? '发布公告' : '编辑公告'"
      width="800px"
      :close-on-click-modal="false"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="公告标题" prop="title">
              <el-input
                v-model="form.title"
                placeholder="请输入公告标题"
                maxlength="100"
                show-word-limit
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="公告类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
                <el-option
                  v-for="item in announcementTypes"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="form.priority" placeholder="请选择优先级" style="width: 100%">
                <el-option
                  v-for="item in priorityOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="目标群体" prop="targetAudience">
              <el-select
                v-model="form.targetAudience"
                placeholder="请选择目标群体"
                style="width: 100%"
              >
                <el-option
                  v-for="item in targetAudienceOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="公告内容" prop="content">
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="8"
                placeholder="请输入公告内容，支持换行"
                maxlength="5000"
                show-word-limit
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="生效时间">
              <div style="display: flex; gap: 12px; align-items: center; width: 100%">
                <el-date-picker
                  ref="startDatePickerRef"
                  v-model="form.startTime"
                  type="datetime"
                  placeholder="开始时间（可选）"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  style="flex: 1"
                  @change="handleStartDateChange"
                />
                <span class="dialog-separator">至</span>
                <el-date-picker
                  ref="endDatePickerRef"
                  v-model="form.endTime"
                  type="datetime"
                  placeholder="结束时间（可选）"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  style="flex: 1"
                />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio label="active">立即发布</el-radio>
                <el-radio label="draft">存为草稿</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit"> 确定 </el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="showViewDialog" title="公告详情" width="700px">
      <div v-if="currentAnnouncement" class="announcement-detail">
        <h3 class="detail-title">{{ currentAnnouncement.title }}</h3>
        <el-divider />
        <div class="detail-meta">
          <el-space wrap>
            <StatusTag
              :status="typeStatus(currentAnnouncement.type)"
              :text="typeLabel(currentAnnouncement.type)"
            />
            <StatusTag
              v-if="currentAnnouncement.priority"
              :status="priorityStatus(currentAnnouncement.priority)"
              :text="priorityLabel(currentAnnouncement.priority)"
            />
            <StatusTag :status="statusKey(currentAnnouncement.status)" />
            <StatusTag
              status="neutral"
              :text="targetAudienceLabel(currentAnnouncement.targetAudience)"
            />
          </el-space>
        </div>
        <el-divider />
        <div class="detail-info">
          <div class="info-row">
            <span class="label">生效时间：</span>
            <span v-if="currentAnnouncement.startTime || currentAnnouncement.endTime">
              {{ formatDateTime(currentAnnouncement.startTime) }} 至
              {{ formatDateTime(currentAnnouncement.endTime) }}
            </span>
            <span v-else class="muted">永久有效</span>
          </div>
          <div class="info-row">
            <span class="label">创建时间：</span>
            <span>{{ formatDateTime(currentAnnouncement.createdAt) }}</span>
          </div>
          <div v-if="currentAnnouncement.readCount !== undefined" class="info-row">
            <span class="label">阅读统计：</span>
            <span>
              阅读量 {{ formatNumber(currentAnnouncement.readCount) }} 次， 阅读人数
              {{ formatNumber(currentAnnouncement.readUsers || 0) }} 人
            </span>
          </div>
        </div>
        <el-divider />
        <div class="content-detail">
          <div class="detail-label">公告内容：</div>
          <div class="content-text">{{ currentAnnouncement.content }}</div>
        </div>
      </div>

      <template #footer>
        <el-button type="primary" @click="handleEdit(currentAnnouncement)">编辑</el-button>
        <el-button @click="showViewDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 用户端预览对话框 -->
    <el-dialog v-model="showPreviewDialog" title="用户端预览" width="500px">
      <div v-if="previewAnnouncement" class="announcement-preview">
        <div class="preview-header">
          <StatusTag
            v-if="previewAnnouncement.priority"
            :status="priorityStatus(previewAnnouncement.priority)"
            :text="priorityLabel(previewAnnouncement.priority)"
          />
          <span class="preview-date">{{ formatDateTime(previewAnnouncement.createdAt) }}</span>
        </div>
        <h3 class="preview-title">{{ previewAnnouncement.title }}</h3>
        <div class="preview-content">{{ previewAnnouncement.content }}</div>
        <div
          v-if="previewAnnouncement.startTime || previewAnnouncement.endTime"
          class="preview-footer"
        >
          有效期：{{ formatDate(previewAnnouncement.startTime) }} ~
          {{ formatDate(previewAnnouncement.endTime) }}
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showPreviewDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document,
  SuccessFilled,
  CircleClose,
  Calendar,
  Plus,
  Search,
  RefreshLeft,
  Delete,
  View,
  RemoveFilled,
  Promotion,
  Close
} from '@element-plus/icons-vue'
import { debounce } from 'lodash-es'
import dayjs from 'dayjs'
import api from '../../utils/api.js'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import AppStatCard from '@/components/common/AppStatCard.vue'
import AppEmptyState from '@/components/common/AppEmptyState.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import RowActions from '@/components/common/RowActions.vue'
import { API_CONFIG } from '../../config/index.js'

// ============ 常量配置 ============
const announcementTypes = [
  { label: '系统公告', value: 'system' },
  { label: '活动公告', value: 'activity' },
  { label: '紧急公告', value: 'urgent' },
  { label: '更新说明', value: 'update' }
]

const priorityOptions = [
  { label: '普通', value: 'normal' },
  { label: '重要', value: 'important' },
  { label: '紧急', value: 'urgent' }
]

const targetAudienceOptions = [
  { label: '全部用户', value: 'all' },
  { label: '商家端', value: 'merchant' },
  { label: '用户端', value: 'customer' }
]

const statsConfig = [
  { key: 'totalCount', label: '总公告数', icon: Document },
  { key: 'activeCount', label: '已发布', icon: SuccessFilled },
  { key: 'draftCount', label: '草稿箱', icon: CircleClose },
  { key: 'todayNewCount', label: '今日新增', icon: Calendar }
]

// ============ 工具函数 ============
// 统一错误处理
const handleError = (error, defaultMessage = '操作失败') => {
  console.error(`${defaultMessage}:`, error)
  const message = error.response?.data?.message || error.message || defaultMessage
  ElMessage.error(message)
}

// 格式化数字（千分位）
const formatNumber = (num) => {
  if (!num && num !== 0) return '0'
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 日期时间格式化
const formatDateTime = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
}

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD') : '-'
}

// 标签获取函数
const typeLabel = (type) => {
  return announcementTypes.find((t) => t.value === type)?.label || '未知'
}

const typeStatus = (type) => {
  const map = { system: 'info', activity: 'warning', urgent: 'danger', update: 'success' }
  return map[type] || 'neutral'
}

// 线上数据里既有 normal/important/urgent，也有 low/high，展示层都要认
const priorityLabel = (priority) => {
  const map = { low: '低', normal: '普通', high: '高', important: '重要', urgent: '紧急' }
  return map[priority] || '-'
}

const priorityStatus = (priority) => {
  const map = { low: 'neutral', normal: 'neutral', high: 'warning', important: 'warning', urgent: 'danger' }
  return map[priority] || 'neutral'
}

const statusKey = (status) => {
  const map = { active: 'active', draft: 'draft', inactive: 'offline' }
  return map[status] || 'unknown'
}

const targetAudienceLabel = (audience) => {
  return targetAudienceOptions.find((t) => t.value === audience)?.label || '全部'
}

// ============ 数据状态 ============
const announcements = ref([])
const loading = ref(false)
const submitting = ref(false)
const selectedRows = ref([])
const tableRef = ref(null)

const statistics = ref({
  totalCount: 0,
  activeCount: 0,
  draftCount: 0,
  inactiveCount: 0,
  todayNewCount: 0
})

const filters = reactive({
  keyword: '',
  type: '',
  priority: '',
  status: '',
  targetAudience: '',
  dateRange: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const showDialog = ref(false)
const showViewDialog = ref(false)
const showPreviewDialog = ref(false)
const dialogMode = ref('create')
const formRef = ref(null)
const startDatePickerRef = ref(null)
const endDatePickerRef = ref(null)
const currentAnnouncement = ref(null)
const previewAnnouncement = ref(null)

const form = reactive({
  title: '',
  content: '',
  type: 'system',
  priority: 'normal',
  targetAudience: 'all',
  startTime: null,
  endTime: null,
  status: 'active'
})

const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }],
  type: [{ required: true, message: '请选择公告类型', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
  targetAudience: [{ required: true, message: '请选择目标群体', trigger: 'change' }]
}

// ============ API 调用 ============
const fetchAnnouncements = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.size,
      keyword: filters.keyword || undefined,
      type: filters.type || undefined,
      priority: filters.priority || undefined,
      status: filters.status || undefined,
      targetAudience: filters.targetAudience || undefined,
      startDate: filters.dateRange?.[0] || undefined,
      endDate: filters.dateRange?.[1] || undefined
    }

    const response = await api.get(API_CONFIG.admin.announcements, { params })

    if (response?.records) {
      announcements.value = response.records
      pagination.total = response.total ?? 0
    } else if (Array.isArray(response)) {
      announcements.value = response
      pagination.total = response.length
    }
  } catch (error) {
    handleError(error, '获取公告列表失败')
  } finally {
    loading.value = false
  }
}

const fetchStatistics = async () => {
  try {
    const response = await api.get(API_CONFIG.admin.announcementStatistics)
    if (response?.data) {
      Object.assign(statistics.value, response.data)
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// ============ 事件处理 ============
const handleSearch = debounce(() => {
  pagination.page = 1
  fetchAnnouncements()
}, 300)

const handlePageChange = () => {
  fetchAnnouncements()
}

const resetFilters = () => {
  Object.assign(filters, {
    keyword: '',
    type: '',
    priority: '',
    status: '',
    targetAudience: '',
    dateRange: null
  })
  pagination.page = 1
  fetchAnnouncements()
}

const showCreateDialog = () => {
  dialogMode.value = 'create'
  showDialog.value = true
}

// 开始时间选择完成后，自动触发结束时间选择器
const handleStartDateChange = () => {
  if (form.startTime && !form.endTime) {
    // 延迟触发，确保开始时间选择器完全关闭后再打开结束时间选择器
    setTimeout(() => {
      endDatePickerRef.value?.focus()
    }, 300)
  }
}

const resetForm = () => {
  Object.assign(form, {
    title: '',
    content: '',
    type: 'system',
    priority: 'normal',
    targetAudience: 'all',
    startTime: null,
    endTime: null,
    status: 'active'
  })
  formRef.value?.clearValidate()
}

const handleView = (row) => {
  currentAnnouncement.value = row
  showViewDialog.value = true
}

const handlePreview = (row) => {
  previewAnnouncement.value = row
  showPreviewDialog.value = true
}

const handleEdit = (row) => {
  dialogMode.value = 'edit'
  showViewDialog.value = false
  Object.assign(form, {
    id: row.id,
    title: row.title,
    content: row.content,
    type: row.type || 'system',
    priority: row.priority || 'normal',
    targetAudience: row.targetAudience || 'all',
    startTime: row.startTime || null,
    endTime: row.endTime || null,
    status: row.status
  })
  showDialog.value = true
}

const rowActions = (row) => {
  const items = [
    {
      text: row.status === 'active' ? '下线公告' : '发布公告',
      danger: row.status === 'active',
      onClick: () => handleToggleStatus(row, row.status === 'active' ? 'inactive' : 'active')
    },
    { text: '预览', onClick: () => handlePreview(row) },
    { text: '编辑', onClick: () => handleEdit(row) }
  ]

  if (row.status === 'draft') {
    items.push({ text: '复制为新公告', onClick: () => copyAnnouncement(row) })
  }

  items.push({ text: '删除公告', danger: true, onClick: () => handleDelete(row) })
  return items
}

const handleToggleStatus = async (row, newStatus) => {
  const action = newStatus === 'active' ? '发布' : '下线'

  try {
    if (newStatus === 'active') {
      await ElMessageBox.confirm(`确定要${action}该公告吗？`, '提示', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      })
    }

    const response = await api.put(`${API_CONFIG.admin.announcementUpdateStatus}/${row.id}`, {
      status: newStatus
    })

    if (response?.success !== false) {
      ElMessage.success(`${action}成功`)
      await Promise.all([fetchAnnouncements(), fetchStatistics()])
    } else {
      throw new Error(response?.message || `${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      handleError(error, `${action}失败`)
    }
  }
}

const handleSubmit = async () => {
  await formRef.value.validate()

  submitting.value = true
  const data = {
    title: form.title,
    content: form.content,
    type: form.type,
    priority: form.priority,
    targetAudience: form.targetAudience,
    startTime: form.startTime,
    endTime: form.endTime,
    status: form.status
  }

  try {
    const isCreate = dialogMode.value === 'create'
    const url = isCreate
      ? API_CONFIG.admin.announcementCreate
      : `${API_CONFIG.admin.announcementUpdate}/${form.id}`
    const method = isCreate ? 'post' : 'put'

    const response = await api[method](url, data)

    if (response?.success !== false) {
      ElMessage.success(isCreate ? '发布成功' : '更新成功')
      showDialog.value = false
      await Promise.all([fetchAnnouncements(), fetchStatistics()])
    } else {
      throw new Error(response?.message || '操作失败')
    }
  } catch (error) {
    handleError(error, '提交失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    const response = await api.delete(`${API_CONFIG.admin.announcementDelete}/${row.id}`)

    if (response?.success !== false) {
      ElMessage.success('删除成功')
      await Promise.all([fetchAnnouncements(), fetchStatistics()])
    } else {
      throw new Error(response?.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      handleError(error, '删除失败')
    }
  }
}

const copyAnnouncement = (row) => {
  dialogMode.value = 'create'
  Object.assign(form, {
    title: `${row.title} (副本)`,
    content: row.content,
    type: row.type || 'system',
    priority: row.priority || 'normal',
    targetAudience: row.targetAudience || 'all',
    startTime: null,
    endTime: null,
    status: 'draft'
  })
  showDialog.value = true
}

// ============ 批量操作 ============
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const clearSelection = () => {
  tableRef.value?.clearSelection()
  selectedRows.value = []
}

const batchPublish = async () => {
  const activeItems = selectedRows.value.filter((row) => row.status !== 'active')
  if (activeItems.length === 0) {
    ElMessage.warning('选中的公告中没有可以发布的项')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要发布选中的 ${activeItems.length} 个公告吗？`, '批量发布', {
      type: 'warning'
    })

    const promises = activeItems.map((row) =>
      api.put(`${API_CONFIG.admin.announcementUpdateStatus}/${row.id}`, { status: 'active' })
    )

    await Promise.all(promises)
    ElMessage.success('批量发布成功')
    clearSelection()
    await Promise.all([fetchAnnouncements(), fetchStatistics()])
  } catch (error) {
    if (error !== 'cancel') {
      handleError(error, '批量发布失败')
    }
  }
}

const batchOffline = async () => {
  const activeItems = selectedRows.value.filter((row) => row.status === 'active')
  if (activeItems.length === 0) {
    ElMessage.warning('选中的公告中没有可以下线的项')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要下线选中的 ${activeItems.length} 个公告吗？`, '批量下线', {
      type: 'warning'
    })

    const promises = activeItems.map((row) =>
      api.put(`${API_CONFIG.admin.announcementUpdateStatus}/${row.id}`, { status: 'inactive' })
    )

    await Promise.all(promises)
    ElMessage.success('批量下线成功')
    clearSelection()
    await Promise.all([fetchAnnouncements(), fetchStatistics()])
  } catch (error) {
    if (error !== 'cancel') {
      handleError(error, '批量下线失败')
    }
  }
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 个公告吗？此操作不可恢复！`,
      '批量删除',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        confirmButtonClass: 'el-button--danger'
      }
    )

    const ids = selectedRows.value.map((item) => item.id)
    const response = await api.delete(API_CONFIG.admin.announcementBatchDelete, { data: ids })

    if (response?.success !== false) {
      ElMessage.success('批量删除成功')
      clearSelection()
      await Promise.all([fetchAnnouncements(), fetchStatistics()])
    } else {
      throw new Error(response?.message || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      handleError(error, '批量删除失败')
    }
  }
}

// ============ 初始化 ============
onMounted(() => {
  fetchAnnouncements()
  fetchStatistics()
})
</script>

<style scoped lang="less">
.announcement-management-container {
  box-sizing: border-box;
  width: 100%;
  max-width: 1440px;
  min-height: calc(100vh - 100px);
  margin: 0 auto;
  padding: var(--card-padding-compact) var(--page-gutter) var(--space-8);
  background: var(--canvas);

  .stats-section {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: var(--card-gap-compact);
    margin-bottom: var(--space-8);
  }

  .main-card {
    border-radius: var(--radius-lg);

    .filter-section {
      margin-bottom: var(--space-5);
      padding: var(--space-4);
      border-radius: var(--radius-md);
      background: var(--canvas-sunken);

      .filter-form {
        margin: 0;

        :deep(.el-form-item) {
          margin-right: var(--space-4);
          margin-bottom: var(--space-3);
        }

        :deep(.el-form-item:last-child) {
          margin-right: 0;
        }
      }
    }

    .batch-actions {
      margin-bottom: var(--space-4);

      :deep(.el-alert__content) {
        display: flex;
        align-items: center;
        gap: var(--space-2);
        flex-wrap: wrap;
      }
    }

    .announcement-table {
      overflow: hidden;
      border-radius: var(--radius-md);

      .announcement-cell {
        min-width: 0;
      }

      .announcement-title {
        overflow: hidden;
        color: var(--ink-900);
        font-weight: var(--weight-medium);
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .content-preview {
        overflow: hidden;
        color: var(--ink-500);
        font-size: var(--text-body-sm);
        line-height: 20px;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .tag-stack {
        display: flex;
        flex-wrap: wrap;
        gap: var(--space-1);
      }

      .read-stats .stat-item-inline {
        display: inline-flex;
        align-items: center;
        gap: var(--space-1);
        color: var(--ink-700);
        font-size: var(--text-body-sm);
      }

      .time-cell {
        color: var(--ink-700);
        font-size: var(--text-caption);
        line-height: 18px;
        white-space: nowrap;

        .time-label {
          margin-right: var(--space-1);
          color: var(--ink-500);
        }
      }

      .muted,
      .dialog-separator {
        color: var(--ink-400);
      }

      :deep(.el-table__row) {
        cursor: pointer;
      }

      :deep(.el-table__row:hover > td.el-table__cell) {
        background-color: var(--canvas-sunken);
        transition: background-color var(--duration-fast) var(--ease-out);
      }
    }

    .pagination-container {
      display: flex;
      justify-content: flex-end;
      margin-top: var(--space-5);
    }
  }

  .announcement-detail {
    .detail-title {
      margin: 0 0 var(--space-4);
      color: var(--ink-900);
      font-size: var(--text-h2);
      font-weight: var(--weight-semibold);
      line-height: 28px;
    }

    .detail-meta {
      margin-bottom: var(--space-2);
    }

    .detail-info {
      .info-row {
        display: flex;
        align-items: center;
        margin-bottom: var(--space-3);
        color: var(--ink-700);
        font-size: var(--text-body);

        &:last-child {
          margin-bottom: 0;
        }

        .label {
          min-width: 80px;
          margin-right: var(--space-2);
          color: var(--ink-500);
        }
      }
    }

    .content-detail {
      .detail-label {
        margin-bottom: var(--space-3);
        color: var(--ink-900);
        font-size: var(--text-h3);
        font-weight: var(--weight-semibold);
      }

      .content-text {
        padding: var(--space-4) var(--space-5);
        border-radius: var(--radius-md);
        background: var(--canvas-sunken);
        color: var(--ink-700);
        font-size: var(--text-body);
        line-height: 1.8;
        white-space: pre-wrap;
        word-break: break-word;
      }
    }
  }

  .announcement-preview {
    padding: var(--space-6);
    border-radius: var(--radius-lg);
    background: var(--surface);
    box-shadow: var(--shadow-md);

    .preview-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: var(--space-4);
      padding-bottom: var(--space-3);
      border-bottom: 1px solid var(--line);

      .preview-date {
        color: var(--ink-500);
        font-size: var(--text-body-sm);
      }
    }

    .preview-title {
      margin: 0 0 var(--space-4);
      color: var(--ink-900);
      font-size: var(--text-h2);
      font-weight: var(--weight-semibold);
      line-height: 28px;
    }

    .preview-content {
      margin-bottom: var(--space-5);
      color: var(--ink-700);
      line-height: 1.8;
      white-space: pre-wrap;
      word-break: break-word;
    }

    .preview-footer {
      padding-top: var(--space-3);
      border-top: 1px solid var(--line);
      color: var(--ink-500);
      font-size: var(--text-body-sm);
      text-align: center;
    }
  }
}

@media (max-width: 768px) {
  .announcement-management-container {
    padding-right: var(--space-4);
    padding-left: var(--space-4);

    .main-card {
      .filter-section {
        .filter-form {
          :deep(.el-form-item) {
            display: block;
            margin-right: 0;
            margin-bottom: var(--space-4);

            .el-form-item__content {
              width: 100% !important;
            }
          }
        }
      }
    }
  }
}
</style>
