<script setup>
import { ref, onMounted } from 'vue'
import { Check, Close, View, Star, Document, VideoCamera } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'

// 数据
const pendingTutorials = ref([])
const loading = ref(false)
const currentTutorial = ref(null)
const showReviewDialog = ref(false)

// 审核表单
const reviewForm = ref({
  decision: 'approve', // approve, approve_featured, reject
  comment: ''
})

// 分页
const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

// 获取待审核列表
const fetchPendingTutorials = async () => {
  loading.value = true
  try {
    const response = await api.get(API_CONFIG.tutorial.adminPending, {
      params: {
        page: pagination.value.page - 1,
        size: pagination.value.size
      }
    })

    console.log('待审核教程列表响应:', response)
    console.log('响应类型:', typeof response)

    // api拦截器已经返回了 response.data
    // 支持多种响应格式：
    // 1. response 本身是分页对象 {records: [], total: 10}
    // 2. response.data 是分页对象
    if (response && response.records) {
      pendingTutorials.value = response.records
      pagination.value.total = response.total || 0
    } else if (response && response.data && response.data.records) {
      pendingTutorials.value = response.data.records
      pagination.value.total = response.data.total || 0
    } else if (Array.isArray(response)) {
      pendingTutorials.value = response
      pagination.value.total = response.length
    } else if (response && response.data && Array.isArray(response.data)) {
      pendingTutorials.value = response.data
      pagination.value.total = response.data.length || 0
    }
  } catch (error) {
    console.error('获取待审核列表失败:', error)
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 查看详情
const viewDetail = (tutorial) => {
  currentTutorial.value = tutorial
  showReviewDialog.value = true
  reviewForm.value = {
    decision: 'approve',
    comment: ''
  }
}

// 审核通过
const approveTutorial = async () => {
  if (reviewForm.value.decision === 'reject' && !reviewForm.value.comment) {
    ElMessage.warning('拒绝时必须填写审核意见')
    return
  }

  try {
    const tutorialId = currentTutorial.value.id
    const setFeatured = reviewForm.value.decision === 'approve_featured'

    const response = await api.post(
      `${API_CONFIG.tutorial.adminApprove}${tutorialId}/approve`,
      {
        comment: reviewForm.value.comment,
        setFeatured: setFeatured
      }
    )

    if (response.data?.success) {
      ElMessage.success('审核通过！')
      showReviewDialog.value = false
      fetchPendingTutorials()
    } else {
      throw new Error('操作失败')
    }
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败，请稍后重试')
  }
}

// 审核拒绝
const rejectTutorial = async () => {
  if (!reviewForm.value.comment) {
    ElMessage.warning('拒绝时必须填写审核意见')
    return
  }

  try {
    const tutorialId = currentTutorial.value.id

    const response = await api.post(
      `${API_CONFIG.tutorial.adminReject}${tutorialId}/reject`,
      {
        comment: reviewForm.value.comment
      }
    )

    if (response.data?.success) {
      ElMessage.success('已拒绝该教程')
      showReviewDialog.value = false
      fetchPendingTutorials()
    } else {
      throw new Error('操作失败')
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

// 提交审核
const submitReview = () => {
  if (reviewForm.value.decision === 'reject') {
    rejectTutorial()
  } else {
    approveTutorial()
  }
}

// 获取来源类型标签样式
const getSourceTypeTag = (type) => {
  const map = {
    ADMIN: { type: 'danger', text: '管理员' },
    MERCHANT: { type: 'warning', text: '商家' },
    USER: { type: 'success', text: '用户' },
    AI_GENERATED: { type: 'info', text: 'AI生成' }
  }
  return map[type] || { type: 'info', text: type || '未知' }
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

// 获取难度名称
const getDifficultyName = (difficulty) => {
  const map = {
    BEGINNER: '初级',
    INTERMEDIATE: '中级',
    ADVANCED: '高级'
  }
  return map[difficulty] || difficulty
}

// 分页改变
const handlePageChange = (page) => {
  pagination.value.page = page
  fetchPendingTutorials()
}

// 页面加载时获取数据
onMounted(() => {
  fetchPendingTutorials()
})
</script>

<template>
  <div class="tutorial-review-container">
    <el-card shadow="never">
      <template #header>
        <div class="header">
          <h3>教程审核队列</h3>
          <el-badge :value="pendingTutorials.length" class="badge">
            <el-button type="primary">待审核</el-button>
          </el-badge>
        </div>
      </template>

      <!-- 加载状态 -->
      <div v-if="loading" v-loading="loading" style="min-height: 200px"></div>

      <!-- 教程列表 -->
      <el-table v-else :data="pendingTutorials" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="教程标题" min-width="200" />

        <el-table-column label="来源" width="120">
          <template #default="{ row }">
            <el-tag :type="getSourceTypeTag(row.sourceType).type" size="small">
              {{ getSourceTypeTag(row.sourceType).text }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="author" label="作者" width="150" />

        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-icon v-if="row.type === 'video'" class="type-icon video"><VideoCamera /></el-icon>
            <el-icon v-else class="type-icon article"><Document /></el-icon>
          </template>
        </el-table-column>

        <el-table-column label="难度" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.difficulty" type="info" size="small">
              {{ getDifficultyName(row.difficulty) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="关联信息" width="150">
          <template #default="{ row }">
            <span v-if="row.linkedDishId">
              关联菜品: #{{ row.linkedDishId }}
            </span>
            <span v-else-if="row.aiModelVersion" class="ai-info">
              {{ row.aiModelVersion }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="viewDetail(row)">
              <el-icon><View /></el-icon> 查看
            </el-button>
            <el-button type="primary" size="small" @click="viewDetail(row)">
              <el-icon><Check /></el-icon> 审核
            </el-button>
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
      :title="`审核教程: ${currentTutorial?.title}`"
      width="800px"
      @close="reviewForm.decision = 'approve'; reviewForm.comment = ''"
    >
      <div v-if="currentTutorial" class="tutorial-preview">
        <!-- 封面图 -->
        <div class="cover-section">
          <img :src="currentTutorial.coverImage" :alt="currentTutorial.title" />
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
          <div class="meta-item">
            <span class="label">作者:</span>
            <span>{{ currentTutorial.author }}</span>
          </div>
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
          <div class="content-text" v-html="currentTutorial.content"></div>
        </div>

        <!-- 关联信息 -->
        <div class="related-section" v-if="currentTutorial.linkedDishId || currentTutorial.aiModelVersion">
          <h4>关联信息</h4>
          <p v-if="currentTutorial.linkedDishId">
            关联菜品ID: {{ currentTutorial.linkedDishId }}
          </p>
          <p v-if="currentTutorial.aiModelVersion">
            AI模型版本: {{ currentTutorial.aiModelVersion }}
          </p>
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
            <el-input
              v-model="reviewForm.comment"
              type="textarea"
              :rows="4"
              :placeholder="reviewForm.decision === 'reject' ? '拒绝时必填' : '请输入审核意见（选填）'"
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="showReviewDialog = false">取消</el-button>
        <el-button
          :type="reviewForm.decision === 'reject' ? 'danger' : 'primary'"
          @click="submitReview"
        >
          {{ reviewForm.decision === 'reject' ? '拒绝' : '通过' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.tutorial-review-container {
  padding: 20px;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    h3 {
      margin: 0;
      font-size: 20px;
      color: #303133;
    }

    .badge {
      :deep(.el-badge__content) {
        background-color: #ff6b6b;
      }
    }
  }

  .type-icon {
    font-size: 20px;

    &.video {
      color: #ff6b6b;
    }

    &.article {
      color: #f7b267;
    }
  }

  .ai-info {
    color: #909399;
    font-size: 12px;
    font-style: italic;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .tutorial-preview {
    .cover-section {
      position: relative;
      width: 100%;
      height: 300px;
      margin-bottom: 20px;
      border-radius: 8px;
      overflow: hidden;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .type-badge {
        position: absolute;
        top: 15px;
        right: 15px;
        background: rgba(255, 255, 255, 0.95);
        padding: 8px 16px;
        border-radius: 20px;
        display: flex;
        align-items: center;
        gap: 6px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        font-weight: 600;
      }
    }

    .meta-section {
      display: flex;
      flex-wrap: wrap;
      gap: 20px;
      margin-bottom: 25px;
      padding: 15px;
      background: #f5f7fa;
      border-radius: 8px;

      .meta-item {
        display: flex;
        align-items: center;
        gap: 8px;

        .label {
          color: #909399;
          font-size: 14px;
        }

        span {
          color: #606266;
        }
      }
    }

    .content-section,
    .related-section {
      margin-bottom: 20px;

      h4 {
        font-size: 16px;
        color: #303133;
        margin-bottom: 12px;
        padding-bottom: 8px;
        border-bottom: 2px solid #ff6b6b;
      }

      .content-text {
        font-size: 14px;
        line-height: 1.8;
        color: #606266;
        max-height: 300px;
        overflow-y: auto;
        padding: 15px;
        background: #f9f9f9;
        border-radius: 4px;

        :deep(h1),
        :deep(h2),
        :deep(h3) {
          margin-top: 15px;
          margin-bottom: 10px;
          color: #303133;
        }

        :deep(p) {
          margin-bottom: 10px;
        }

        :deep(ul),
        :deep(ol) {
          padding-left: 20px;
          margin-bottom: 10px;
        }

        :deep(strong) {
          color: #ff6b6b;
        }
      }

      p {
        color: #606266;
        font-size: 14px;
        line-height: 1.8;
      }
    }
  }
}
</style>
