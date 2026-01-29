<script setup>
import { ref, onMounted, computed } from 'vue'
import { Edit, Delete, View, VideoCamera, Document, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'

// 数据
const tutorials = ref([])
const loading = ref(false)
const showEditDialog = ref(false)
const dialogMode = ref('create')

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
  cover_image: '',
  featured: false,
  is_official: true
})

// 统计数据
const stats = computed(() => {
  const all = tutorials.value
  return {
    total: all.length,
    published: all.filter(t => t.status === 'PUBLISHED').length,
    pending: all.filter(t => t.review_status === 'PENDING').length,
    featured: all.filter(t => t.featured).length
  }
})

// 获取所有教程
const fetchAllTutorials = async () => {
  loading.value = true
  try {
    const response = await api.get(API_CONFIG.tutorial.list)
    if (response.data) {
      tutorials.value = response.data
    }
  } catch (error) {
    console.error('获取教程列表失败:', error)
    ElMessage.error('加载失败，显示模拟数据')
    // 使用模拟数据
    tutorials.value = [
      {
        id: 7,
        title: '青木瓜沙拉制作教程',
        type: 'video',
        source_type: 'ADMIN',
        status: 'PUBLISHED',
        review_status: 'APPROVED',
        featured: true,
        is_official: true,
        duration: '5:30',
        difficulty: 'BEGINNER',
        view_count: 12500,
        rating: 4.8,
        create_time: '2025-01-29'
      },
      {
        id: 8,
        title: '夏日低卡饮食指南',
        type: 'article',
        source_type: 'ADMIN',
        status: 'PUBLISHED',
        review_status: 'APPROVED',
        featured: true,
        is_official: true,
        duration: '8分钟',
        difficulty: 'BEGINNER',
        view_count: 8200,
        rating: 4.9,
        create_time: '2025-01-28'
      },
      {
        id: 10,
        title: '秘制红烧肉做法',
        type: 'video',
        source_type: 'MERCHANT',
        status: 'PENDING',
        review_status: 'PENDING',
        featured: false,
        is_official: false,
        duration: '12:30',
        difficulty: 'INTERMEDIATE',
        view_count: 0,
        rating: 0,
        create_time: '2025-01-27'
      }
    ]
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
    cover_image: '',
    featured: false,
    is_official: true
  }
  showEditDialog.value = true
}

// 打开编辑对话框
const openEditDialog = (tutorial) => {
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
    cover_image: tutorial.cover_image || '',
    featured: tutorial.featured,
    is_official: tutorial.is_official
  }
  showEditDialog.value = true
}

// 保存教程
const saveTutorial = async () => {
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
      const response = await api.post(API_CONFIG.tutorial.adminCreate, tutorialForm.value)
      if (response.data) {
        ElMessage.success('创建成功！')
        showEditDialog.value = false
        fetchAllTutorials()
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
        fetchAllTutorials()
      }
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请稍后重试')
  }
}

// 删除教程
const deleteTutorial = async (tutorial) => {
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

    const response = await api.delete(`${API_CONFIG.tutorial.adminDelete}${tutorial.id}`)

    if (response.data?.success) {
      ElMessage.success('删除成功！')
      fetchAllTutorials()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

// 设置/取消精选
const toggleFeatured = async (tutorial) => {
  try {
    const response = await api.put(
      `${API_CONFIG.tutorial.adminToggleFeatured}${tutorial.id}/featured`,
      { featured: !tutorial.featured }
    )

    if (response.data?.success) {
      ElMessage.success(tutorial.featured ? '已取消精选' : '已设为精选')
      fetchAllTutorials()
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

// 获取来源类型标签
const getSourceTypeTag = (type) => {
  const map = {
    ADMIN: { type: 'danger', text: '管理员' },
    MERCHANT: { type: 'warning', text: '商家' },
    AI_GENERATED: { type: 'info', text: 'AI生成' }
  }
  return map[type] || { type: '', text: type }
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

// 获取难度名称
const getDifficultyName = (difficulty) => {
  const map = {
    BEGINNER: '初级',
    INTERMEDIATE: '中级',
    ADVANCED: '高级'
  }
  return map[difficulty] || difficulty
}

// 页面加载时获取数据
onMounted(() => {
  fetchAllTutorials()
})
</script>

<template>
  <div class="admin-tutorial-manage-container">
    <el-card shadow="never">
      <template #header>
        <div class="header">
          <h3>教程管理</h3>
          <el-button type="primary" @click="openCreateDialog">
            <el-icon><Plus /></el-icon> 创建教程
          </el-button>
        </div>
      </template>

      <!-- 统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">总计</div>
        </div>
        <div class="stat-card published">
          <div class="stat-value">{{ stats.published }}</div>
          <div class="stat-label">已发布</div>
        </div>
        <div class="stat-card pending">
          <div class="stat-value">{{ stats.pending }}</div>
          <div class="stat-label">待审核</div>
        </div>
        <div class="stat-card featured">
          <div class="stat-value">{{ stats.featured }}</div>
          <div class="stat-label">精选</div>
        </div>
      </div>

      <!-- Tab切换 -->
      <el-tabs style="margin-top: 20px">
        <!-- 全部教程 -->
        <el-tab-pane label="全部教程" name="all">
          <el-table :data="tutorials" v-loading="loading" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="教程标题" min-width="200" />

            <el-table-column label="来源" width="120">
              <template #default="{ row }">
                <el-tag :type="getSourceTypeTag(row.source_type).type" size="small">
                  {{ getSourceTypeTag(row.source_type).text }}
                </el-tag>
              </template>
            </el-table-column>

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

            <el-table-column label="精选" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.featured" type="success" size="small">
                  ⭐
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column label="难度" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.difficulty" type="info" size="small">
                  {{ getDifficultyName(row.difficulty) }}
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
                <span v-if="row.rating">{{ row.rating }} ⭐</span>
                <span v-else style="color: #909399">-</span>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="280" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="openEditDialog(row)">
                  <el-icon><Edit /></el-icon> 编辑
                </el-button>
                <el-button
                  :type="row.featured ? 'warning' : 'success'"
                  size="small"
                  @click="toggleFeatured(row)"
                >
                  {{ row.featured ? '取消精选' : '设为精选' }}
                </el-button>
                <el-button type="danger" size="small" @click="deleteTutorial(row)">
                  <el-icon><Delete /></el-icon> 删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="showEditDialog"
      :title="dialogMode === 'create' ? '创建教程' : '编辑教程'"
      width="700px"
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

        <el-form-item label="设为精选">
          <el-switch v-model="tutorialForm.featured" />
        </el-form-item>

        <el-form-item label="官方认证">
          <el-switch v-model="tutorialForm.is_official" />
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

<style scoped lang="less">
.admin-tutorial-manage-container {
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
  }

  .stats-cards {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 20px;

    .stat-card {
      padding: 20px;
      border-radius: 8px;
      text-align: center;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

      &.published {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }

      &.pending {
        background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
      }

      &.featured {
        background: linear-gradient(135deg, #ffd700 0%, #ffb347 100%);
      }

      .stat-value {
        font-size: 32px;
        font-weight: bold;
        margin-bottom: 8px;
      }

      .stat-label {
        font-size: 14px;
        opacity: 0.9;
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
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
}
</style>
