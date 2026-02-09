<template>
  <div class="announcement-management-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="12" :md="6" v-for="(stat, index) in statsConfig" :key="index">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" :style="{ background: stat.bgColor }">
              <el-icon :size="24" :color="stat.iconColor">
                <component :is="stat.icon" />
              </el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics[stat.key] || 0 }}</div>
              <div class="stat-label">{{ stat.label }}</div>
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
            <el-select v-model="filters.targetAudience" placeholder="全部" clearable style="width: 130px">
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
            <el-button type="primary" @click="handleSearch">
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
        <el-alert
          :title="`已选择 ${selectedRows.length} 项`"
          type="info"
          :closable="false"
        >
          <template #default>
            <el-button size="small" type="primary" @click="batchPublish">
              <el-icon><Promotion /></el-icon>
              批量发布
            </el-button>
            <el-button size="small" type="warning" @click="batchOffline">
              <el-icon><RemoveFilled /></el-icon>
              批量下线
            </el-button>
            <el-button size="small" type="danger" @click="batchDelete">
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
            <el-button size="small" @click="clearSelection">
              <el-icon><Close /></el-icon>
              取消选择
            </el-button>
          </template>
        </el-alert>
      </div>

      <!-- 表格 -->
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="announcements"
        stripe
        @selection-change="handleSelectionChange"
        @row-dblclick="handleEdit"
        class="announcement-table"
      >
        <el-table-column type="selection" width="55" fixed="left" />
        <el-table-column label="优先级" width="80" align="center">
          <template #default="{ row }">
            <el-tag
              v-if="row.priority"
              :type="priorityTagType(row.priority)"
              size="small"
              effect="plain"
            >
              {{ priorityLabel(row.priority) }}
            </el-tag>
            <span v-else style="color: #c0c4cc;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.type)" size="small">
              {{ typeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="公告标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="公告内容" min-width="280" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="content-preview">{{ row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column label="目标群体" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info" effect="plain">
              {{ targetAudienceLabel(row.target_audience) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="阅读统计" width="110" align="center">
          <template #default="{ row }">
            <div class="read-stats" v-if="row.read_count !== undefined">
              <el-tooltip :content="`阅读人数: ${row.read_users || 0}`" placement="top">
                <div class="stat-item-inline">
                  <el-icon :size="14"><View /></el-icon>
                  <span>{{ formatNumber(row.read_count) }}</span>
                </div>
              </el-tooltip>
            </div>
            <span v-else style="color: #c0c4cc;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="生效时间" width="160" align="center">
          <template #default="{ row }">
            <div class="time-range" v-if="row.start_time || row.end_time">
              <div>{{ formatDate(row.start_time) }}</div>
              <div class="time-separator">↓</div>
              <div>{{ formatDate(row.end_time) }}</div>
            </div>
            <el-tag v-else size="small" type="info" effect="plain">永久</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="155" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.create_time) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button-group class="action-buttons">
              <el-tooltip content="查看详情" placement="top">
                <el-button size="small" @click.stop="handleView(row)">
                  <el-icon><View /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="预览效果" placement="top">
                <el-button size="small" @click.stop="handlePreview(row)">
                  <el-icon><Monitor /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="编辑" placement="top">
                <el-button size="small" @click.stop="handleEdit(row)">
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-dropdown @command="(cmd) => handleDropdownCommand(cmd, row)" trigger="click">
                <el-button size="small">
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      :command="row.status === 'active' ? 'offline' : 'publish'"
                      :icon="row.status === 'active' ? RemoveFilled : Promotion"
                    >
                      {{ row.status === 'active' ? '下线公告' : '发布公告' }}
                    </el-dropdown-item>
                    <el-dropdown-item
                      command="copy"
                      :icon="DocumentCopy"
                      v-if="row.status === 'draft'"
                    >
                      复制为新公告
                    </el-dropdown-item>
                    <el-dropdown-item
                      command="delete"
                      :icon="Delete"
                      divided
                      style="color: #f56c6c;"
                    >
                      删除公告
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
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
          @size-change="handlePageChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="showDialog"
      :title="dialogMode === 'create' ? '发布公告' : '编辑公告'"
      width="800px"
      @close="resetForm"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="110px">
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
              <el-select v-model="form.targetAudience" placeholder="请选择目标群体" style="width: 100%">
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
              <div style="display: flex; gap: 12px; align-items: center; width: 100%;">
                <el-date-picker
                  ref="startDatePickerRef"
                  v-model="form.startTime"
                  type="datetime"
                  placeholder="开始时间（可选）"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  style="flex: 1;"
                  @change="handleStartDateChange"
                />
                <span style="color: #909399;">至</span>
                <el-date-picker
                  ref="endDatePickerRef"
                  v-model="form.endTime"
                  type="datetime"
                  placeholder="结束时间（可选）"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  style="flex: 1;"
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
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="showViewDialog"
      title="公告详情"
      width="700px"
    >
      <div class="announcement-detail" v-if="currentAnnouncement">
        <h3 class="detail-title">{{ currentAnnouncement.title }}</h3>
        <el-divider />
        <div class="detail-meta">
          <el-space wrap>
            <el-tag :type="typeTagType(currentAnnouncement.type)" size="small">
              {{ typeLabel(currentAnnouncement.type) }}
            </el-tag>
            <el-tag
              v-if="currentAnnouncement.priority"
              :type="priorityTagType(currentAnnouncement.priority)"
              size="small"
            >
              {{ priorityLabel(currentAnnouncement.priority) }}
            </el-tag>
            <el-tag :type="statusTagType(currentAnnouncement.status)" size="small">
              {{ statusLabel(currentAnnouncement.status) }}
            </el-tag>
            <el-tag type="info" size="small" effect="plain">
              {{ targetAudienceLabel(currentAnnouncement.target_audience) }}
            </el-tag>
          </el-space>
        </div>
        <el-divider />
        <div class="detail-info">
          <div class="info-row">
            <span class="label">生效时间：</span>
            <span v-if="currentAnnouncement.start_time || currentAnnouncement.end_time">
              {{ formatDateTime(currentAnnouncement.start_time) }} 至 {{ formatDateTime(currentAnnouncement.end_time) }}
            </span>
            <span v-else style="color: #909399;">永久有效</span>
          </div>
          <div class="info-row">
            <span class="label">创建时间：</span>
            <span>{{ formatDateTime(currentAnnouncement.create_time) }}</span>
          </div>
          <div class="info-row" v-if="currentAnnouncement.read_count !== undefined">
            <span class="label">阅读统计：</span>
            <span>
              阅读量 {{ formatNumber(currentAnnouncement.read_count) }} 次，
              阅读人数 {{ formatNumber(currentAnnouncement.read_users || 0) }} 人
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
        <el-button @click="handleEdit(currentAnnouncement)" type="primary">编辑</el-button>
        <el-button @click="showViewDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 用户端预览对话框 -->
    <el-dialog
      v-model="showPreviewDialog"
      title="用户端预览"
      width="500px"
    >
      <div class="announcement-preview" v-if="previewAnnouncement">
        <div class="preview-header">
          <el-tag
            v-if="previewAnnouncement.priority"
            :type="priorityTagType(previewAnnouncement.priority)"
            size="small"
          >
            {{ priorityLabel(previewAnnouncement.priority) }}
          </el-tag>
          <span class="preview-date">{{ formatDateTime(previewAnnouncement.create_time) }}</span>
        </div>
        <h3 class="preview-title">{{ previewAnnouncement.title }}</h3>
        <div class="preview-content">{{ previewAnnouncement.content }}</div>
        <div class="preview-footer" v-if="previewAnnouncement.start_time || previewAnnouncement.end_time">
          有效期：{{ formatDate(previewAnnouncement.start_time) }} ~ {{ formatDate(previewAnnouncement.end_time) }}
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
  Document, SuccessFilled, CircleClose, Calendar, Plus, Search, RefreshLeft,
  Edit, Delete, View, Monitor, MoreFilled, RemoveFilled, Promotion,
  DocumentCopy, Close
} from '@element-plus/icons-vue'
import { debounce } from 'lodash-es'
import dayjs from 'dayjs'
import api from '../../utils/api.js'
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
  { key: 'totalCount', label: '总公告数', icon: Document, bgColor: '#ecf5ff', iconColor: '#409eff' },
  { key: 'activeCount', label: '已发布', icon: SuccessFilled, bgColor: '#f0f9ff', iconColor: '#67c23a' },
  { key: 'draftCount', label: '草稿箱', icon: CircleClose, bgColor: '#fef0f0', iconColor: '#909399' },
  { key: 'todayNewCount', label: '今日新增', icon: Calendar, bgColor: '#fdf6ec', iconColor: '#e6a23c' }
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
  return announcementTypes.find(t => t.value === type)?.label || '未知'
}

const typeTagType = (type) => {
  const map = { system: '', activity: 'warning', urgent: 'danger', update: 'success' }
  return map[type] || 'info'
}

const priorityLabel = (priority) => {
  return priorityOptions.find(p => p.value === priority)?.label || '-'
}

const priorityTagType = (priority) => {
  const map = { normal: 'info', important: 'warning', urgent: 'danger' }
  return map[priority] || 'info'
}

const statusLabel = (status) => {
  const map = { active: '已发布', draft: '草稿', inactive: '已下线' }
  return map[status] || '未知'
}

const statusTagType = (status) => {
  const map = { active: 'success', draft: 'info', inactive: 'info' }
  return map[status] || 'info'
}

const targetAudienceLabel = (audience) => {
  return targetAudienceOptions.find(t => t.value === audience)?.label || '全部'
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
      pagination.total = response.total || 0
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
    targetAudience: row.target_audience || 'all',
    startTime: row.start_time || null,
    endTime: row.end_time || null,
    status: row.status
  })
  showDialog.value = true
}

const handleDropdownCommand = (command, row) => {
  const actions = {
    offline: () => handleToggleStatus(row, 'inactive'),
    publish: () => handleToggleStatus(row, 'active'),
    copy: () => copyAnnouncement(row),
    delete: () => handleDelete(row)
  }
  actions[command]?.()
}

const handleToggleStatus = async (row, newStatus) => {
  const action = newStatus === 'active' ? '发布' : '下线'

  try {
    await ElMessageBox.confirm(`确定要${action}该公告吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

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
    const url = isCreate ? API_CONFIG.admin.announcementCreate : `${API_CONFIG.admin.announcementUpdate}/${form.id}`
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
    await ElMessageBox.confirm('确定要删除该公告吗？此操作不可恢复！', '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--danger'
    })

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
    targetAudience: row.target_audience || 'all',
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
  const activeItems = selectedRows.value.filter(row => row.status !== 'active')
  if (activeItems.length === 0) {
    ElMessage.warning('选中的公告中没有可以发布的项')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要发布选中的 ${activeItems.length} 个公告吗？`, '批量发布', {
      type: 'warning'
    })

    const promises = activeItems.map(row =>
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
  const activeItems = selectedRows.value.filter(row => row.status === 'active')
  if (activeItems.length === 0) {
    ElMessage.warning('选中的公告中没有可以下线的项')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要下线选中的 ${activeItems.length} 个公告吗？`, '批量下线', {
      type: 'warning'
    })

    const promises = activeItems.map(row =>
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

    const ids = selectedRows.value.map(item => item.id)
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
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 40px);

  .stats-row {
    margin-bottom: 20px;

    .stat-card {
      .stat-item {
        display: flex;
        align-items: center;

        .stat-icon {
          width: 56px;
          height: 56px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 16px;
          flex-shrink: 0;
        }

        .stat-content {
          flex: 1;
          min-width: 0;

          .stat-value {
            font-size: 28px;
            font-weight: 700;
            color: #303133;
            line-height: 1;
            margin-bottom: 6px;
          }

          .stat-label {
            font-size: 13px;
            color: #909399;
          }
        }
      }
    }
  }

  .main-card {
    border-radius: 8px;

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
    }

    .filter-section {
      margin-bottom: 20px;
      padding: 16px;
      background: #fafafa;
      border-radius: 6px;

      .filter-form {
        margin: 0;

        :deep(.el-form-item) {
          margin-bottom: 12px;
          margin-right: 16px;
        }

        :deep(.el-form-item:last-child) {
          margin-right: 0;
        }
      }
    }

    .batch-actions {
      margin-bottom: 16px;

      :deep(.el-alert__content) {
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }

    .announcement-table {
      border-radius: 8px;
      overflow: hidden;

      .content-preview {
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        line-height: 1.6;
        color: #606266;
      }

      .read-stats {
        .stat-item-inline {
          display: inline-flex;
          align-items: center;
          gap: 4px;
          font-size: 13px;
          color: #606266;
        }
      }

      .time-range {
        font-size: 12px;
        line-height: 1.4;
        color: #606266;

        .time-separator {
          color: #c0c4cc;
          text-align: center;
          margin: 2px 0;
        }
      }

      .action-buttons {
        :deep(.el-button) {
          padding: 5px 8px;
        }
      }

      :deep(.el-table__row) {
        cursor: pointer;

        &:hover {
          background-color: #f5f7fa;
        }
      }
    }

    .pagination-container {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }

  .announcement-detail {
    .detail-title {
      margin: 0 0 16px 0;
      font-size: 20px;
      font-weight: 600;
      color: #303133;
      line-height: 1.5;
    }

    .detail-meta {
      margin-bottom: 8px;
    }

    .detail-info {
      .info-row {
        display: flex;
        align-items: center;
        margin-bottom: 12px;
        font-size: 14px;

        &:last-child {
          margin-bottom: 0;
        }

        .label {
          color: #909399;
          margin-right: 8px;
          min-width: 80px;
        }
      }
    }

    .content-detail {
      .detail-label {
        font-weight: 600;
        margin-bottom: 12px;
        color: #303133;
        font-size: 15px;
      }

      .content-text {
        padding: 16px 20px;
        background: #f5f7fa;
        border-radius: 6px;
        line-height: 1.8;
        white-space: pre-wrap;
        word-break: break-word;
        color: #606266;
        font-size: 14px;
      }
    }
  }

  .announcement-preview {
    background: #fff;
    border-radius: 8px;
    padding: 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

    .preview-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      padding-bottom: 12px;
      border-bottom: 1px solid #ebeef5;

      .preview-date {
        font-size: 13px;
        color: #909399;
      }
    }

    .preview-title {
      margin: 0 0 16px 0;
      font-size: 20px;
      font-weight: 600;
      color: #303133;
      line-height: 1.5;
    }

    .preview-content {
      margin-bottom: 20px;
      line-height: 1.8;
      color: #606266;
      white-space: pre-wrap;
      word-break: break-word;
    }

    .preview-footer {
      padding-top: 12px;
      border-top: 1px solid #ebeef5;
      font-size: 13px;
      color: #909399;
      text-align: center;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .announcement-management-container {
    padding: 12px;

    .stats-row {
      :deep(.el-col) {
        margin-bottom: 12px;
      }
    }

    .main-card {
      .filter-section {
        .filter-form {
          :deep(.el-form-item) {
            display: block;
            margin-right: 0;
            margin-bottom: 16px;

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
