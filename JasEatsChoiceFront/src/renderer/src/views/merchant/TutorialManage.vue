<script setup>
import { ref, onMounted, computed } from 'vue'
import { Plus, Edit, Delete, Send, VideoCamera, Document } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'

// 数据
const merchantTutorials = ref([])
const loading = ref(false)
const showEditDialog = ref(false)
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
    <el-card shadow="never">
      <template #header>
        <div class="header">
          <h3>我的教程</h3>
          <el-button type="primary" @click="openCreateDialog">
            <el-icon><Plus /></el-icon> 创建教程
          </el-button>
        </div>
      </template>

      <!-- 统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card total">
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
      </div>

      <!-- Tab切换 -->
      <el-tabs v-model="activeTab" style="margin-top: 20px">
        <!-- 全部教程 -->
        <el-tab-pane label="全部教程" name="all">
          <tutorial-list
            :tutorials="merchantTutorials"
            :loading="loading"
            @edit="openEditDialog"
            @submit="submitForReview"
            @delete="deleteTutorial"
          />
        </el-tab-pane>

        <!-- 草稿 -->
        <el-tab-pane label="草稿" name="draft">
          <tutorial-list
            :tutorials="merchantTutorials.filter(t => t.status === 'DRAFT')"
            :loading="loading"
            @edit="openEditDialog"
            @submit="submitForReview"
            @delete="deleteTutorial"
          />
        </el-tab-pane>

        <!-- 待审核 -->
        <el-tab-pane label="待审核" name="pending">
          <tutorial-list
            :tutorials="merchantTutorials.filter(t => t.reviewStatus === 'PENDING')"
            :loading="loading"
            @edit="openEditDialog"
            @submit="submitForReview"
            @delete="deleteTutorial"
          />
        </el-tab-pane>

        <!-- 已发布 -->
        <el-tab-pane label="已发布" name="published">
          <tutorial-list
            :tutorials="merchantTutorials.filter(t => t.status === 'PUBLISHED')"
            :loading="loading"
            @edit="openEditDialog"
            @submit="submitForReview"
            @delete="deleteTutorial"
          />
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


<style scoped lang="less">
.merchant-tutorial-manage-container {
  padding: 20px;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    h3 {
      margin: 0;
      font-size: 1.429rem /* 原值: 20px */;
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
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

      &.total {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
      }

      &.draft {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        color: white;
      }

      &.pending {
        background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
        color: white;
      }

      &.published {
        background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
        color: white;
      }

      .stat-value {
        font-size: 2.571rem /* 原值: 36px */;
        font-weight: bold;
        margin-bottom: 8px;
      }

      .stat-label {
        font-size: 1rem /* 原值: 14px */;
        opacity: 0.9;
      }
    }
  }

  .type-icon {
    font-size: 1.286rem /* 原值: 18px */;

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
