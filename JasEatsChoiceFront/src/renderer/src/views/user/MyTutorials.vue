<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Edit, Delete, Send, VideoCamera, Document } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'

const router = useRouter()

// 数据
const myTutorials = ref([])
const loading = ref(false)
const activeTab = ref('all')

// 统计数据
const stats = computed(() => {
  const all = myTutorials.value
  return {
    total: all.length,
    draft: all.filter(t => t.status === 'DRAFT').length,
    pending: all.filter(t => t.review_status === 'PENDING').length,
    published: all.filter(t => t.status === 'PUBLISHED').length,
    rejected: all.filter(t => t.review_status === 'REJECTED').length
  }
})

// 获取我的教程
const fetchMyTutorials = async () => {
  loading.value = true
  try {
    // TODO: 调用实际的API
    // const response = await api.get(API_CONFIG.tutorial.userMy)

    // 临时使用模拟数据
    myTutorials.value = [
      {
        id: 100,
        title: '我的拿手菜：宫保鸡丁',
        type: 'video',
        source_type: 'USER',
        status: 'PUBLISHED',
        review_status: 'APPROVED',
        duration: '18分钟',
        difficulty: 'INTERMEDIATE',
        content: '## 宫保鸡丁制作教程\n\n### 食材准备\n...',
        cover_image: 'https://images.unsplash.com/photo-1626645738199-c3a4f32706ad?w=800',
        view_count: 3420,
        rating: 4.6,
        rating_count: 56,
        create_time: '2025-01-25'
      },
      {
        id: 101,
        title: '家常豆腐的做法',
        type: 'article',
        source_type: 'USER',
        status: 'DRAFT',
        review_status: 'NOT_SUBMITTED',
        duration: '12分钟',
        difficulty: 'BEGINNER',
        content: '## 家常豆腐制作教程\n\n### 做法...',
        cover_image: '',
        view_count: 0,
        rating: 0,
        create_time: '2025-01-26'
      }
    ]
  } catch (error) {
    console.error('获取教程列表失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 编辑教程
const editTutorial = (tutorial) => {
  // 只能编辑草稿或被拒绝的教程
  if (tutorial.status !== 'DRAFT' && tutorial.review_status !== 'REJECTED') {
    ElMessage.warning('只能编辑草稿或被拒绝的教程')
    return
  }

  // 跳转到编辑页面（可以复用PublishTutorial页面）
  ElMessage.info('编辑功能开发中...')
}

// 提交审核
const submitForReview = async (tutorial) => {
  if (tutorial.review_status !== 'NOT_SUBMITTED' && tutorial.review_status !== 'REJECTED') {
    ElMessage.warning('该教程已提交或在审核中')
    return
  }

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

    // TODO: 调用API
    ElMessage.success('已提交审核！')
    fetchMyTutorials()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交失败:', error)
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

    // TODO: 调用API
    ElMessage.success('删除成功！')
    fetchMyTutorials()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 发布新教程
const publishNew = () => {
  router.push('/user/publish-tutorial')
}

// 查看教程
const viewTutorial = (tutorial) => {
  router.push(`/user/home/tutorials/${tutorial.id}`)
}

// 获取状态标签
const getStatusTag = (status) => {
  const map = {
    DRAFT: { type: 'info', text: '草稿' },
    PENDING: { type: 'warning', text: '待审核' },
    PUBLISHED: { type: 'success', text: '已发布' },
    REJECTED: { type: 'danger', text: '已拒绝' }
  }
  return map[status] || { type: '', text: status }
}

// 获取审核状态标签
const getReviewStatusTag = (status) => {
  const map = {
    NOT_SUBMITTED: { type: 'info', text: '未提交' },
    PENDING: { type: 'warning', text: '待审核' },
    APPROVED: { type: 'success', text: '已通过' },
    REJECTED: { type: 'danger', text: '已拒绝' }
  }
  return map[status] || { type: '', text: status }
}

// 判断是否可编辑
const isEditable = (tutorial) => {
  return tutorial.status === 'DRAFT' || tutorial.review_status === 'REJECTED'
}

// 判断是否可提交
const canSubmit = (tutorial) => {
  return tutorial.review_status === 'NOT_SUBMITTED' || tutorial.review_status === 'REJECTED'
}

// 判断是否可删除
const canDelete = (tutorial) => {
  return tutorial.status === 'DRAFT'
}

// 页面加载时获取数据
onMounted(() => {
  fetchMyTutorials()
})
</script>

<template>
  <div class="my-tutorials-container">
    <div class="page-header">
      <common-back-button type="primary" size="small" text="返回首页" @click="() => router.push('/user/home')" :use-router-back="false" />
      <h1>我的教程</h1>
      <el-button type="primary" @click="publishNew">
        <el-icon><Plus /></el-icon> 发布新教程
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-value">{{ stats.total }}</div>
        <div class="stat-label">总计</div>
      </div>
      <div class="stat-card draft">
        <div class="stat-value">{{ stats.draft }}</div>
        <div class="stat-label">草稿</div>
      </div>
      <div class="stat-card pending">
        <div class="stat-value">{{ stats.pending }}</div>
        <div class="stat-label">待审核</div>
      </div>
      <div class="stat-card published">
        <div class="stat-value">{{ stats.published }}</div>
        <div class="stat-label">已发布</div>
      </div>
      <div class="stat-card rejected">
        <div class="stat-value">{{ stats.rejected }}</div>
        <div class="stat-label">已拒绝</div>
      </div>
    </div>

    <!-- Tab切换 -->
    <el-tabs v-model="activeTab" class="tabs">
      <!-- 全部教程 -->
      <el-tab-pane label="全部教程" name="all">
        <tutorial-list
          :tutorials="myTutorials"
          :loading="loading"
          @view="viewTutorial"
          @edit="editTutorial"
          @submit="submitForReview"
          @delete="deleteTutorial"
        />
      </el-tab-pane>

      <!-- 草稿 -->
      <el-tab-pane label="草稿" name="draft">
        <tutorial-list
          :tutorials="myTutorials.filter(t => t.status === 'DRAFT')"
          :loading="loading"
          @view="viewTutorial"
          @edit="editTutorial"
          @submit="submitForReview"
          @delete="deleteTutorial"
        />
      </el-tab-pane>

      <!-- 待审核 -->
      <el-tab-pane label="待审核" name="pending">
        <tutorial-list
          :tutorials="myTutorials.filter(t => t.review_status === 'PENDING')"
          :loading="loading"
          @view="viewTutorial"
          @edit="editTutorial"
          @submit="submitForReview"
          @delete="deleteTutorial"
        />
      </el-tab-pane>

      <!-- 已发布 -->
      <el-tab-pane label="已发布" name="published">
        <tutorial-list
          :tutorials="myTutorials.filter(t => t.status === 'PUBLISHED')"
          :loading="loading"
          @view="viewTutorial"
          @edit="editTutorial"
          @submit="submitForReview"
          @delete="deleteTutorial"
        />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
// 教程列表子组件
const TutorialList = {
  name: 'TutorialList',
  props: {
    tutorials: Array,
    loading: Boolean
  },
  emits: ['view', 'edit', 'submit', 'delete'],
  setup(props, { emit }) {
    return {
      emit
    }
  },
  template: `
    <div v-loading="loading" style="min-height: 300px">
      <el-empty v-if="!loading && tutorials.length === 0" description="暂无教程">
        <el-button type="primary" @click="$emit('publish-new')">发布第一个教程</el-button>
      </el-empty>
      <el-table v-else :data="tutorials" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="教程标题" min-width="200" />

        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-icon v-if="row.type === 'video'" class="type-icon video">
              <VideoCamera />
            </el-icon>
            <el-icon v-else class="type-icon article">
              <Document />
            </el-icon>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status).type" size="small">
              {{ getStatusTag(row.status).text }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getReviewStatusTag(row.review_status).type" size="small">
              {{ getReviewStatusTag(row.review_status).text }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="浏览量" width="120">
          <template #default="{ row }">
            <span>{{ row.view_count?.toLocaleString() || 0 }}</span>
          </template>
        </el-table-column>

        <el-table-column label="评分" width="100">
          <template #default="{ row }">
            <span v-if="row.rating > 0">{{ row.rating }} ⭐ ({{ row.rating_count }})</span>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="120">
          <template #default="{ row }">
            {{ row.create_time?.split(' ')[0] }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button
              type="info"
              size="small"
              @click="$emit('view', row)"
            >
              <el-icon><View /></el-icon> 查看
            </el-button>
            <el-button
              v-if="isEditable(row)"
              type="primary"
              size="small"
              @click="$emit('edit', row)"
            >
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button
              v-if="canSubmit(row)"
              type="success"
              size="small"
              @click="$emit('submit', row)"
            >
              <el-icon><Send /></el-icon> 提交审核
            </el-button>
            <el-button
              v-if="canDelete(row)"
              type="danger"
              size="small"
              @click="$emit('delete', row)"
            >
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  `
}
</script>

<style scoped lang="less">
.my-tutorials-container {
  padding: 20px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    h1 {
      margin: 0;
      font-size: 24px;
      color: #303133;
    }
  }

  .stats-cards {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 16px;
    margin-bottom: 24px;

    .stat-card {
      background: white;
      padding: 20px;
      border-radius: 8px;
      text-align: center;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

      .stat-value {
        font-size: 32px;
        font-weight: bold;
        margin-bottom: 8px;

        .draft & { color: #909399; }
        &.pending { color: #e6a23c; }
        &.published { color: #67c23a; }
        &.rejected { color: #f56c6c; }
      }

      .stat-label {
        font-size: 14px;
        color: #909399;
      }
    }
  }

  .type-icon {
    font-size: 18px;

    &.video {
      color: #ff6b6b;
    }

    &.article {
      color: #f7b267;
    }
  }
}

@media (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;

    h1 {
      text-align: center;
    }
  }
}
</style>
