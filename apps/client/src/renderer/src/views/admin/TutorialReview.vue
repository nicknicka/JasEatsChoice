<template>
  <div class="tutorial-review-container">
    <!-- 页面标题 -->
    <AppPageHeader title="教程审核" :compact="true" class="review-page-header">
      <template #actions>
        <el-button type="primary" @click="fetchPendingTutorials">刷新</el-button>
      </template>
    </AppPageHeader>

    <!-- 审核列表 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>待审核教程队列</span>
          <el-badge :value="pendingTutorials.length" class="badge">
            <span class="badge-label">待审核</span>
          </el-badge>
        </div>
      </template>

      <!-- 加载状态 -->
      <div v-if="loading" v-loading="loading" style="min-height: 200px"></div>

      <!-- 教程列表 -->
      <el-table v-else :data="pendingTutorials" stripe>
        <el-table-column prop="id" label="ID" min-width="190" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="cell-id">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="教程标题" min-width="180" show-overflow-tooltip />

        <el-table-column label="来源" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getSourceTypeTag(row.sourceType).type" size="small">
              {{ getSourceTypeTag(row.sourceType).text }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="作者" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.author || '未知作者' }}</template>
        </el-table-column>

        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <span class="type-cell">
              <el-icon v-if="row.type === 'video'"><VideoCamera /></el-icon>
              <el-icon v-else><Document /></el-icon>
              {{ row.type === 'video' ? '视频' : '图文' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="难度" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.difficulty" type="info" size="small">{{ getDifficultyName(row.difficulty) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="关联信息" width="140" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="relation-text">{{ getRelationText(row) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <RowActions
              :primary="{ text: '查看', onClick: () => viewDetail(row) }"
              :items="[{ text: '通过', onClick: () => openReview(row) }]"
              :max-visible="1"
            />
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          :page-size="pagination.size"
          :total="pagination.total"
          layout="total, prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="showReviewDialog"
      :title="`审核教程：${currentTutorial?.title}`"
      width="800px"
      @close="reviewForm.decision = 'approve'; reviewForm.comment = ''"
    >
      <div v-if="currentTutorial" class="tutorial-preview">
        <!-- 封面图 -->
        <div class="cover-section">
          <img v-if="currentTutorial.coverImage" :src="currentTutorial.coverImage" :alt="currentTutorial.title" />
          <div class="type-badge">
            <el-icon v-if="currentTutorial.type === 'video'"><VideoCamera /></el-icon>
            <el-icon v-else><Document /></el-icon>
            {{ currentTutorial.type === 'video' ? '视频' : '图文' }}
          </div>
        </div>

        <!-- 元信息 -->
        <div class="meta-section">
          <div class="meta-item">
            <span class="label">来源:</span>
            <el-tag :type="getSourceTypeTag(currentTutorial.sourceType).type" size="small">
              {{ getSourceTypeTag(currentTutorial.sourceType).text }}
            </el-tag>
          </div>
          <div class="meta-item"><span class="label">作者:</span><span>{{ currentTutorial.author }}</span></div>
          <div class="meta-item" v-if="currentTutorial.difficulty">
            <span class="label">难度:</span>
            <el-tag type="info" size="small">{{ getDifficultyName(currentTutorial.difficulty) }}</el-tag>
          </div>
          <div class="meta-item" v-if="currentTutorial.duration">
            <span class="label">时长:</span>
            <span>{{ currentTutorial.duration }}</span>
          </div>
        </div>

        <!-- 内容预览 -->
        <div class="content-section">
          <h4>教程内容</h4>
          <div class="content-text markdown-body" v-html="renderMarkdown(currentTutorial.content)"></div>
        </div>

        <!-- 关联信息 -->
        <div class="related-section" v-if="currentTutorial.linkedDishId || currentTutorial.aiModelVersion">
          <h4>关联信息</h4>
          <p v-if="currentTutorial.linkedDishId">关联菜品 ID: {{ currentTutorial.linkedDishId }}</p>
          <p v-if="currentTutorial.aiModelVersion">AI 模型版本：{{ currentTutorial.aiModelVersion }}</p>
        </div>

        <!-- 审核表单 -->
        <el-divider />
        <el-form :model="reviewForm" label-width="100px">
          <el-form-item label="审核决定">
            <el-radio-group v-model="reviewForm.decision">
              <el-radio value="approve">通过并发布</el-radio>
              <el-radio value="approve_featured">通过并设为精选 ⭐</el-radio>
              <el-radio value="reject">拒绝</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="审核意见">
            <el-input v-model="reviewForm.comment" type="textarea" :rows="4" placeholder="请输入审核意见（选填）" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showReviewDialog = false">取消</el-button>
        <el-button :type="reviewForm.decision === 'reject' ? 'danger' : 'primary'" @click="submitReview">
          {{ reviewForm.decision === 'reject' ? '拒绝' : '通过' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Document, VideoCamera } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '../../utils/api.js'
import { getAvatarUrl } from '../../utils/avatar'
import { API_CONFIG } from '../../config/index.js'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import RowActions from '@/components/common/RowActions.vue'

// 配置 marked 选项
marked.setOptions({ breaks: true, gfm: true })

// Markdown 渲染函数 (带 XSS 防护)
const renderMarkdown = (content) => {
  if (!content) return ''
  const rawHtml = marked(content)
  return DOMPurify.sanitize(rawHtml)
}

// 数据
const pendingTutorials = ref([])
const loading = ref(false)
const currentTutorial = ref(null)
const showReviewDialog = ref(false)

const resolveTutorialImage = (url) => {
  const rawUrl = String(url || '').trim().replace(/\\/g, '/')
  if (!rawUrl) return ''
  if (rawUrl.startsWith('//')) return `${window.location.protocol}${rawUrl}`
  if (/^(data:|blob:|file:|https?:)/i.test(rawUrl)) return rawUrl
  return getAvatarUrl(rawUrl)
}

const normalizeTutorial = (tutorial = {}) => ({
  ...tutorial,
  sourceType: tutorial.sourceType || tutorial.source_type || '',
  reviewStatus: tutorial.reviewStatus || tutorial.review_status || '',
  coverImage: resolveTutorialImage(tutorial.coverImage || tutorial.cover_image || ''),
  linkedDishId: tutorial.linkedDishId || tutorial.linked_dish_id || '',
  aiModelVersion: tutorial.aiModelVersion || tutorial.ai_model_version || '',
  viewCount: tutorial.viewCount ?? tutorial.view_count ?? tutorial.views ?? 0,
  isOfficial: tutorial.isOfficial ?? tutorial.is_official ?? tutorial.official ?? false
})

const reviewForm = ref({ decision: 'approve', comment: '' })
const pagination = ref({ page: 1, size: 10, total: 0 })

const fetchPendingTutorials = async () => {
  loading.value = true
  try {
    const response = await api.get(API_CONFIG.tutorial.adminPending, {
      params: { page: pagination.value.page - 1, size: pagination.value.size }
    })
    console.log('待审核教程列表响应:', response)
    if (response && response.records) {
      pendingTutorials.value = response.records.map(normalizeTutorial)
      pagination.value.total = response.total ?? 0
    } else if (Array.isArray(response)) {
      pendingTutorials.value = response.map(normalizeTutorial)
      pagination.value.total = response.length
    }
  } catch (error) {
    console.error('获取待审核列表失败:', error)
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 关联信息列只保留可读的菜品关联，模型版本仍留在详情弹窗里
const getRelationText = (row) => (row.linkedDishId ? `菜品 #${row.linkedDishId}` : '-')

// 「通过」与「查看」都进同一个审核对话框，区别只在于显式把决定预置成通过
const openReview = (tutorial) => {
  viewDetail(tutorial)
  reviewForm.value.decision = 'approve'
}

const viewDetail = (tutorial) => {
  currentTutorial.value = normalizeTutorial(tutorial)
  showReviewDialog.value = true
  reviewForm.value = { decision: 'approve', comment: '' }
}

const approveTutorial = async () => {
  if (reviewForm.value.decision === 'reject' && !reviewForm.value.comment) {
    ElMessage.warning('拒绝时必须填写审核意见')
    return
  }
  try {
    const tutorialId = currentTutorial.value.id
    const setFeatured = reviewForm.value.decision === 'approve_featured'
    const response = await api.post(`${API_CONFIG.tutorial.adminApprove}${tutorialId}/approve`, {
      comment: reviewForm.value.comment,
      setFeatured: setFeatured
    })
    if (response?.success) {
      ElMessage.success('审核通过！')
      showReviewDialog.value = false
      fetchPendingTutorials()
    } else throw new Error('操作失败')
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败，请稍后重试')
  }
}

const rejectTutorial = async () => {
  if (!reviewForm.value.comment) {
    ElMessage.warning('拒绝时必须填写审核意见')
    return
  }
  try {
    const tutorialId = currentTutorial.value.id
    const response = await api.post(`${API_CONFIG.tutorial.adminReject}${tutorialId}/reject`, {
      comment: reviewForm.value.comment
    })
    if (response?.success) {
      ElMessage.success('已拒绝该教程')
      showReviewDialog.value = false
      fetchPendingTutorials()
    } else throw new Error('操作失败')
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

const submitReview = () => {
  if (reviewForm.value.decision === 'reject') rejectTutorial()
  else approveTutorial()
}

const getSourceTypeTag = (type) => {
  const map = { ADMIN: { type: 'danger', text: '管理员' }, MERCHANT: { type: 'warning', text: '商家' }, USER: { type: 'success', text: '用户' }, AI_GENERATED: { type: 'info', text: 'AI 生成' } }
  return map[type] || { type: 'info', text: type || '未知' }
}

const getDifficultyName = (difficulty) => {
  const map = { BEGINNER: '初级', INTERMEDIATE: '中级', ADVANCED: '高级' }
  return map[difficulty] || difficulty
}

const handlePageChange = (page) => { pagination.value.page = page; fetchPendingTutorials() }

onMounted(() => { fetchPendingTutorials() })
</script>

<style scoped lang="less">
@import '@/assets/css/nordic-theme.less';

.tutorial-review-container { padding: var(--card-padding-compact) var(--page-gutter); background: var(--canvas); max-width: 1440px; width: 100%; margin: 0 auto; box-sizing: border-box;
  .review-page-header { margin-bottom: var(--space-6); }
  /* 长 ID 不可拆：等宽数字 + 单行（§4.3 §5.4） */
  .cell-id { font-family: var(--font-num); font-variant-numeric: tabular-nums; white-space: nowrap; }
  .type-cell { display: inline-flex; align-items: center; gap: var(--space-1); color: var(--ink-700); white-space: nowrap; }
  .relation-text { color: var(--ink-600); }
  .table-card { .card-header { display: flex; justify-content: space-between; align-items: center; font-weight: 600; h3 { margin: 0; color: var(--ink-900); } /* 待审核是状态不是操作，改用纯徽标（§4.6） */ .badge-label { color: var(--ink-700); font-weight: var(--weight-medium); } :deep(.el-badge__content) { background-color: var(--danger); } } .pagination-container { margin-top: var(--space-5); display: flex; justify-content: flex-end; } }
  .tutorial-preview { .cover-section { position: relative; margin-bottom: var(--space-6); img { max-width: 100%; height: auto; border-radius: var(--radius-lg); } .type-badge { position: absolute; top: var(--space-2); right: var(--space-2); background: rgba(0,0,0,0.6); color: white; padding: var(--space-1) var(--space-3); border-radius: var(--radius-md); display: flex; align-items: center; gap: var(--space-1); } } .meta-section { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: var(--space-3); margin-bottom: var(--space-6); .meta-item { display: flex; align-items: center; gap: var(--space-2); .label { color: var(--ink-500); font-weight: 500; } } } .content-section h4, .related-section h4 { color: var(--ink-900); font-size: var(--text-base); margin-bottom: var(--space-3); } .content-text { line-height: 1.8; color: var(--ink-800); } .related-section p { color: var(--ink-600); font-size: var(--text-sm); margin-bottom: var(--space-2); } }
}
</style>
