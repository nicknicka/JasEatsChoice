<script setup>
import { ref, onMounted, computed } from 'vue'
import { Plus, VideoCamera, Document, Collection, EditPen, Clock, CircleCheck } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import AppCard from '../../components/common/AppCard.vue'
import AppEmptyState from '../../components/common/AppEmptyState.vue'
import StatusTag from '../../components/common/StatusTag.vue'

// 数据
const merchantTutorials = ref([])
const loading = ref(false)
const showEditDialog = ref(false)
const activeTab = ref('all')
const dialogMode = ref('create') // create 或 edit

// 表单数据
const tutorialForm = ref({
  id: null,
  title: '',
  type: 'article',
  content: '',
  difficulty: 'BEGINNER',
  duration: '',
  calories: null,
  prep_time: '',
  servings: null,
  linked_dish_id: null,
  cover_image: ''
})

// 商家的菜品列表（用于关联）
const merchantDishes = ref([
  { id: 1, name: '招牌红烧肉', price: 68, image: 'https://example.com/dish1.jpg' },
  { id: 2, name: '清蒸鲈鱼', price: 88, image: 'https://example.com/dish2.jpg' },
  { id: 3, name: '宫保鸡丁', price: 48, image: 'https://example.com/dish3.jpg' }
])

// 统计数据
const stats = computed(() => {
  const all = merchantTutorials.value
  return {
    total: all.length,
    draft: all.filter(t => t.status === 'DRAFT').length,
    pending: all.filter(t => t.reviewStatus === 'PENDING').length,
    published: all.filter(t => t.status === 'PUBLISHED').length,
    rejected: all.filter(t => t.reviewStatus === 'REJECTED').length
  }
})

const filters = [
  { value: 'all', label: '全部教程', count: 'total', icon: Collection },
  { value: 'draft', label: '草稿', count: 'draft', icon: EditPen },
  { value: 'pending', label: '待审核', count: 'pending', icon: Clock },
  { value: 'published', label: '已发布', count: 'published', icon: CircleCheck }
]

const filteredTutorials = computed(() => {
  const tutorials = merchantTutorials.value
  if (activeTab.value === 'draft') return tutorials.filter(t => t.status === 'DRAFT')
  if (activeTab.value === 'pending') return tutorials.filter(t => t.reviewStatus === 'PENDING')
  if (activeTab.value === 'published') return tutorials.filter(t => t.status === 'PUBLISHED')
  return tutorials
})

// 获取商家教程列表
const fetchMerchantTutorials = async () => {
  loading.value = true
  try {
    const response = await api.get(API_CONFIG.tutorial.merchantMy, {
      params: { page: 0, size: 100 }
    })

    console.log('商家教程列表响应:', response)
    console.log('响应类型:', typeof response)

    // api拦截器已经返回了 response.data
    // 支持多种响应格式：
    // 1. response 本身是分页对象 {records: [], total: 10}
    // 2. response.data 是分页对象
    if (response && response.records) {
      merchantTutorials.value = response.records
    } else if (response && response.data && response.data.records) {
      merchantTutorials.value = response.data.records
    } else if (Array.isArray(response)) {
      merchantTutorials.value = response
    } else if (response && response.data && Array.isArray(response.data)) {
      merchantTutorials.value = response.data
    }
  } catch (error) {
    console.error('获取教程列表失败:', error)
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 打开创建对话框
const openCreateDialog = () => {
  dialogMode.value = 'create'
  tutorialForm.value = {
    id: null,
    title: '',
    type: 'article',
    content: '',
    difficulty: 'BEGINNER',
    duration: '',
    calories: null,
    prep_time: '',
    servings: null,
    linked_dish_id: null,
    cover_image: ''
  }
  showEditDialog.value = true
}

// 打开编辑对话框
const openEditDialog = (tutorial) => {
  // 只能编辑草稿或被拒绝的教程
  if (tutorial.status !== 'DRAFT' && tutorial.reviewStatus !== 'REJECTED') {
    ElMessage.warning('只能编辑草稿或被拒绝的教程')
    return
  }

  dialogMode.value = 'edit'
  tutorialForm.value = {
    id: tutorial.id,
    title: tutorial.title,
    type: tutorial.type,
    content: tutorial.content,
    difficulty: tutorial.difficulty || 'BEGINNER',
    duration: tutorial.duration || '',
    calories: tutorial.calories,
    prep_time: tutorial.prep_time,
    servings: tutorial.servings,
    linked_dish_id: tutorial.linked_dish_id,
    cover_image: tutorial.cover_image || ''
  }
  showEditDialog.value = true
}

// 保存教程
const saveTutorial = async () => {
  // 验证表单
  if (!tutorialForm.value.title) {
    ElMessage.warning('请输入教程标题')
    return
  }
  if (!tutorialForm.value.content) {
    ElMessage.warning('请输入教程内容')
    return
  }

  try {
    if (dialogMode.value === 'create') {
      // 创建教程
      const response = await api.post(API_CONFIG.tutorial.merchantCreate, tutorialForm.value)
      if (response.data) {
        ElMessage.success('创建成功！教程已保存为草稿')
        showEditDialog.value = false
        fetchMerchantTutorials()
      }
    } else {
      // 更新教程
      const response = await api.put(
        `${API_CONFIG.tutorial.merchantUpdate}${tutorialForm.value.id}`,
        tutorialForm.value
      )
      if (response.data?.success) {
        ElMessage.success('更新成功！')
        showEditDialog.value = false
        fetchMerchantTutorials()
      }
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请稍后重试')
  }
}

// 提交审核
const submitForReview = async (tutorial) => {
  try {
    await ElMessageBox.confirm(
      '提交后将无法再编辑，确认提交审核？',
      '提交审核',
      {
        confirmButtonText: '确认提交',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await api.post(
      `${API_CONFIG.tutorial.merchantSubmit}${tutorial.id}/submit`
    )

    if (response.data?.success) {
      ElMessage.success('已提交审核！')
      fetchMerchantTutorials()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交失败:', error)
      ElMessage.error('提交失败，请稍后重试')
    }
  }
}

// 删除教程
const deleteTutorial = async (tutorial) => {
  // 只能删除草稿
  if (tutorial.status !== 'DRAFT') {
    ElMessage.warning('只能删除草稿状态的教程')
    return
  }

  try {
    await ElMessageBox.confirm(
      '删除后无法恢复，确认删除？',
      '删除教程',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await api.delete(`${API_CONFIG.tutorial.merchantDelete}${tutorial.id}`)

    if (response.data?.success) {
      ElMessage.success('删除成功！')
      fetchMerchantTutorials()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

// 获取状态标签
const getStatusTag = (status) => {
  const map = {
    DRAFT: { type: 'info', text: '草稿' },
    PENDING: { type: 'warning', text: '待审核' },
    PUBLISHED: { type: 'success', text: '已发布' },
    REJECTED: { type: 'danger', text: '已拒绝' }
  }
  return map[status] || { type: 'info', text: status || '未知' }
}

// 获取审核状态标签
const getReviewStatusTag = (status) => {
  const map = {
    NOT_SUBMITTED: { type: 'info', text: '未提交' },
    PENDING: { type: 'warning', text: '待审核' },
    APPROVED: { type: 'success', text: '已通过' },
    REJECTED: { type: 'danger', text: '已拒绝' }
  }
  return map[status] || { type: 'info', text: status || '未知' }
}

// 判断是否可编辑
const isEditable = (tutorial) => {
  return tutorial.status === 'DRAFT' || tutorial.reviewStatus === 'REJECTED'
}

// 判断是否可提交
const canSubmit = (tutorial) => {
  return tutorial.reviewStatus === 'NOT_SUBMITTED' || tutorial.reviewStatus === 'REJECTED'
}

// 判断是否可删除
const canDelete = (tutorial) => {
  return tutorial.status === 'DRAFT'
}

// 页面加载时获取数据
onMounted(() => {
  fetchMerchantTutorials()
})
</script>

<template>
  <div class="merchant-tutorial-manage-container">
    <AppPageHeader title="我的教程" subtitle="管理教程草稿，查看审核与发布状态" compact>
      <template #actions>
        <el-button type="primary" :plain="showEditDialog" :icon="Plus" @click="openCreateDialog">
          创建教程
        </el-button>
      </template>
    </AppPageHeader>

    <AppCard v-if="loading && !merchantTutorials.length" padding="none" class="tutorial-panel" aria-label="正在加载教程" aria-busy="true">
      <el-skeleton :rows="5" />
    </AppCard>
    <AppCard v-else-if="!merchantTutorials.length" padding="none" class="tutorial-panel">
      <AppEmptyState
        :icon="Document"
        description="还没有教程，创建第一篇教程分享你的烹饪经验"
        action-text="创建教程"
        compact
        @action="openCreateDialog"
      />
    </AppCard>
    <template v-else>
      <div class="tutorial-filters" role="group" aria-label="按教程状态筛选">
        <button
          v-for="filter in filters"
          :key="filter.value"
          type="button"
          class="tutorial-filter"
          :aria-pressed="activeTab === filter.value"
          :disabled="loading"
          @click="activeTab = filter.value"
        >
          <el-icon aria-hidden="true"><component :is="filter.icon" /></el-icon>
          <span>{{ filter.label }}</span>
          <span class="filter-count">{{ stats[filter.count] }}</span>
        </button>
      </div>

      <AppCard v-loading="loading" padding="none" class="tutorial-panel" :aria-busy="loading">
        <p class="tutorial-summary" role="status">共 <span>{{ filteredTutorials.length }}</span> 篇教程</p>
        <el-table v-if="filteredTutorials.length" :data="filteredTutorials" row-key="id" class="tutorial-table">
          <el-table-column prop="title" label="教程标题" min-width="240" show-overflow-tooltip />
          <el-table-column label="类型" width="120">
            <template #default="{ row }">
              <span class="tutorial-type">
                <el-icon aria-hidden="true"><component :is="row.type === 'video' ? VideoCamera : Document" /></el-icon>
                {{ row.type === 'video' ? '视频教程' : '图文指南' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="发布状态" width="110">
            <template #default="{ row }">
              <StatusTag :status="row.status || 'unknown'" :text="getStatusTag(row.status).text" />
            </template>
          </el-table-column>
          <el-table-column label="审核状态" width="110">
            <template #default="{ row }">
              <StatusTag :status="row.reviewStatus || 'unknown'" :text="getReviewStatusTag(row.reviewStatus).text" />
            </template>
          </el-table-column>
          <el-table-column prop="duration" label="时长" width="110" show-overflow-tooltip>
            <template #default="{ row }">{{ row.duration || '—' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <div class="tutorial-actions">
                <el-button v-if="isEditable(row)" type="primary" link @click="openEditDialog(row)">编辑</el-button>
                <el-dropdown v-if="canSubmit(row) || canDelete(row)" trigger="click" popper-class="merchant-tutorial-actions-menu">
                  <el-button link type="primary" :aria-label="`${row.title}的更多操作`">更多</el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item v-if="canSubmit(row)" @click="submitForReview(row)">提交审核</el-dropdown-item>
                      <el-dropdown-item v-if="canDelete(row)" :divided="canSubmit(row)" class="tutorial-delete" @click="deleteTutorial(row)">删除教程</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                <span v-if="!isEditable(row) && !canSubmit(row) && !canDelete(row)" class="tutorial-readonly">—</span>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <AppEmptyState
          v-else
          :icon="Document"
          description="当前筛选下没有教程，清除筛选查看全部教程"
          action-text="清除筛选"
          compact
          @action="activeTab = 'all'"
        />
      </AppCard>
    </template>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="showEditDialog"
      :title="dialogMode === 'create' ? '创建教程' : '编辑教程'"
      width="min(var(--form-width-settings), calc(100% - var(--space-8)))"
    >
      <el-form :model="tutorialForm" label-width="100px">
        <el-form-item label="教程标题" required>
          <el-input v-model="tutorialForm.title" placeholder="请输入教程标题" />
        </el-form-item>

        <el-form-item label="教程类型" required>
          <el-radio-group v-model="tutorialForm.type">
            <el-radio value="article">图文指南</el-radio>
            <el-radio value="video">视频教程</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="难度">
          <el-select v-model="tutorialForm.difficulty">
            <el-option label="初级" value="BEGINNER" />
            <el-option label="中级" value="INTERMEDIATE" />
            <el-option label="高级" value="ADVANCED" />
          </el-select>
        </el-form-item>

        <el-form-item label="时长">
          <el-input v-model="tutorialForm.duration" placeholder="例如: 15分钟" />
        </el-form-item>

        <el-form-item label="关联菜品">
          <el-select
            v-model="tutorialForm.linked_dish_id"
            placeholder="选择要关联的菜品（可选）"
            clearable
          >
            <el-option
              v-for="dish in merchantDishes"
              :key="dish.id"
              :label="dish.name"
              :value="dish.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="卡路里">
          <el-input-number
            v-model="tutorialForm.calories"
            :min="0"
            :step="10"
            placeholder="千卡"
          />
        </el-form-item>

        <el-form-item label="准备时间">
          <el-input v-model="tutorialForm.prep_time" placeholder="例如: 20分钟" />
        </el-form-item>

        <el-form-item label="份量">
          <el-input-number
            v-model="tutorialForm.servings"
            :min="1"
            :max="20"
            placeholder="人份"
          />
        </el-form-item>

        <el-form-item label="封面图URL">
          <el-input v-model="tutorialForm.cover_image" placeholder="图片URL（可选）" />
        </el-form-item>

        <el-form-item label="教程内容" required>
          <el-input
            v-model="tutorialForm.content"
            type="textarea"
            :rows="10"
            placeholder="支持Markdown格式，例如：## 标题、- 列表、**粗体**"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTutorial">
          {{ dialogMode === 'create' ? '创建' : '保存' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>


<style scoped>
.merchant-tutorial-manage-container {
  min-width: 0;
  padding: var(--space-6);
  font-family: var(--font-sans);
  color: var(--ink-700);
}

.merchant-tutorial-manage-container :deep(.app-page-header__actions .el-button) {
  height: var(--control-height-md);
}

.tutorial-panel {
  min-width: 0;
  padding: var(--card-padding-compact);
}

.tutorial-filters {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  padding: var(--space-2);
  margin-bottom: var(--card-gap-compact);
  border-radius: var(--radius-lg);
  background: var(--canvas-sunken);
}

.tutorial-filter {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  height: var(--control-height-sm);
  padding: 0 var(--space-3);
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  background: var(--surface);
  color: var(--ink-500);
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  cursor: pointer;
  transition:
    color var(--duration-fast) var(--ease-out),
    background-color var(--duration-fast) var(--ease-out),
    border-color var(--duration-fast) var(--ease-out);
}

.tutorial-filter:hover:not(:disabled),
.tutorial-filter[aria-pressed='true'] {
  color: var(--brand-600);
  border-color: var(--brand-300);
  background: var(--brand-50);
}

.tutorial-filter[aria-pressed='true'] {
  font-weight: var(--weight-semibold);
}

.tutorial-filter:active:not(:disabled) {
  background: var(--brand-100);
}

.tutorial-filter:focus-visible {
  outline: 2px solid var(--brand-400);
  outline-offset: 2px;
}

.tutorial-filter:disabled {
  color: var(--ink-400);
  border-color: var(--line);
  background: var(--neutral-bg);
  cursor: not-allowed;
}

.filter-count,
.tutorial-summary span {
  font-family: var(--font-num);
  font-variant-numeric: tabular-nums;
}

.tutorial-summary {
  margin: 0 0 var(--space-3);
  color: var(--ink-500);
  font-size: var(--text-body-sm);
}

.tutorial-table {
  --el-table-header-bg-color: var(--canvas);
  --el-table-header-text-color: var(--ink-500);
  --el-table-text-color: var(--ink-700);
  --el-table-row-hover-bg-color: var(--canvas);
  font-size: var(--text-body-sm);
}

.tutorial-table :deep(.el-table__cell) {
  height: var(--control-height-md);
  padding: var(--space-1) 0;
}

.tutorial-table :deep(th.el-table__cell) {
  font-weight: var(--weight-semibold);
}

.tutorial-type,
.tutorial-actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  white-space: nowrap;
}

.tutorial-type,
.tutorial-readonly {
  color: var(--ink-500);
}

.tutorial-actions :deep(.el-button) {
  height: var(--control-height-sm);
  font-size: var(--text-body-sm);
}

:global(.merchant-tutorial-actions-menu .tutorial-delete) {
  color: var(--danger);
}

:global(.merchant-tutorial-actions-menu .tutorial-delete:not(.is-disabled):hover),
:global(.merchant-tutorial-actions-menu .tutorial-delete:not(.is-disabled):focus) {
  color: var(--danger);
  background: var(--danger-bg);
}

@media (max-width: 1280px) {
  .merchant-tutorial-manage-container {
    padding: var(--space-4);
  }
}

@media (max-width: 768px) {
  .merchant-tutorial-manage-container :deep(.app-page-header__main) {
    flex-wrap: wrap;
  }

  .tutorial-filter {
    flex: 1 1 auto;
  }
}
</style>
