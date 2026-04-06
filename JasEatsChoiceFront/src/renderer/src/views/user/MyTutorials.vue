<script>
// 教程列表子组件 - 使用普通的组件定义方式
export default {
  name: 'MyTutorials'
}
</script>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Edit, Delete, Promotion, VideoCamera, Document, View, Clock, Star } from '@element-plus/icons-vue'
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
    pending: all.filter(t => t.status === 'PENDING').length,
    published: all.filter(t => t.status === 'PUBLISHED').length,
    rejected: all.filter(t => t.status === 'REJECTED').length
  }
})

// 获取我的教程
const fetchMyTutorials = async () => {
  loading.value = true
  console.log('=== fetchMyTutorials 开始执行 ===')
  console.log('请求URL:', API_CONFIG.tutorial.userMy)

  try {
    const response = await api.get(API_CONFIG.tutorial.userMy)
    console.log('API响应完整内容:', response)
    console.log('response.data:', response.data)
    console.log('response类型:', typeof response)
    console.log('是否为数组:', Array.isArray(response))
    console.log('response.data是否为数组:', Array.isArray(response.data))

    // 支持多种响应格式：
    // 1. response.data 是数组
    // 2. response 本身是数组
    // 3. response 是分页对象（包含 records 字段）
    // 4. response.data 是对象但包含数据
    let tutorials = null

    if (Array.isArray(response.data)) {
      tutorials = response.data
      console.log('使用 response.data (数组)')
    } else if (Array.isArray(response)) {
      tutorials = response
      console.log('使用 response (数组)')
    } else if (response && Array.isArray(response.records)) {
      // 分页对象格式：{records: [], total: 10, ...}
      tutorials = response.records
      console.log('使用 response.records (分页对象)')
    } else if (response && typeof response === 'object' && response.data && typeof response.data === 'object') {
      // response.data 是对象，可能需要进一步处理
      tutorials = response.data.data || response.data.list || response.data.items || response.data.records || response.data
      console.log('从 response.data 对象中提取数据:', tutorials)
    }

    if (Array.isArray(tutorials)) {
      myTutorials.value = tutorials
      console.log('✅ 成功加载教程列表，共', tutorials.length, '条')
      console.log('教程列表:', tutorials)

      // 调试：检查每个教程的status和review_status
      tutorials.forEach((t, index) => {
        console.log(`教程${index + 1}:`, {
          id: t.id,
          title: t.title,
          status: t.status,
          review_status: t.review_status
        })
      })

      // 调试：统计所有可能的状态值
      const allStatuses = [...new Set(tutorials.map(t => t.status))]
      const allReviewStatuses = [...new Set(tutorials.map(t => t.review_status))]
      console.log('📊 所有 status 值:', allStatuses)
      console.log('📊 所有 review_status 值:', allReviewStatuses)

      // 详细统计
      console.log('📊 详细统计:')
      console.log('- DRAFT (草稿):', tutorials.filter(t => t.status === 'DRAFT').length)
      console.log('- PUBLISHED (已发布):', tutorials.filter(t => t.status === 'PUBLISHED').length)
      console.log('- review_status = PENDING:', tutorials.filter(t => t.review_status === 'PENDING').length)
      console.log('- review_status = APPROVED:', tutorials.filter(t => t.review_status === 'APPROVED').length)
      console.log('- review_status = REJECTED:', tutorials.filter(t => t.review_status === 'REJECTED').length)
      console.log('- review_status = NOT_SUBMITTED:', tutorials.filter(t => t.review_status === 'NOT_SUBMITTED').length)
    } else {
      console.warn('⚠️ 响应格式异常，无法解析教程列表')
      console.warn('最终解析结果:', tutorials)
      myTutorials.value = []
    }
  } catch (error) {
    console.error('❌ 获取教程列表失败')
    console.error('错误对象:', error)
    console.error('错误消息:', error.message)
    console.error('响应数据:', error.response?.data)
    console.error('响应状态:', error.response?.status)

    ElMessage.error(error.response?.data?.message || error.message || '加载失败，请稍后重试')
    myTutorials.value = []
  } finally {
    loading.value = false
    console.log('=== fetchMyTutorials 执行结束 ===')
  }
}

// 编辑教程
const editTutorial = (tutorial) => {
  // 只能编辑草稿或被拒绝的教程
  if (tutorial.status !== 'DRAFT' && tutorial.status !== 'REJECTED') {
    ElMessage.warning('只能编辑草稿或被拒绝的教程')
    return
  }

  // 跳转到编辑页面（传递教程ID）
  router.push(`/user/home/tutorials/publish?id=${tutorial.id}`)
}

// 提交审核
const submitForReview = async (tutorial) => {
  if (tutorial.status !== 'DRAFT' && tutorial.status !== 'REJECTED') {
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

    loading.value = true
    await api.post(`${API_CONFIG.tutorial.userSubmit}/${tutorial.id}/submit`)
    ElMessage.success('已提交审核！')
    fetchMyTutorials()
  } catch (error) {
    loading.value = false
    if (error !== 'cancel') {
      console.error('提交失败:', error)
      ElMessage.error(error.response?.data?.message || '提交失败，请稍后重试')
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

    loading.value = true
    await api.delete(`${API_CONFIG.tutorial.userDelete}/${tutorial.id}`)
    ElMessage.success('删除成功！')
    fetchMyTutorials()
  } catch (error) {
    loading.value = false
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.response?.data?.message || '删除失败，请稍后重试')
    }
  }
}

// 发布新教程
const publishNew = () => {
  router.push('/user/home/tutorials/publish')
}

// 查看教程
const viewTutorial = (tutorial) => {
  router.push(`/user/home/tutorials/${tutorial.id}?from=my`)
}

// 获取状态标签
const getStatusTag = (status) => {
  const map = {
    DRAFT: { type: 'info', effect: 'plain', text: '草稿', icon: '📝' },
    PENDING: { type: 'warning', effect: 'plain', text: '待审核', icon: '⏳' },
    PUBLISHED: { type: 'success', effect: 'dark', text: '已发布', icon: '✅' },
    REJECTED: { type: 'danger', effect: 'plain', text: '已拒绝', icon: '❌' }
  }
  return map[status] || { type: '', text: status, icon: '' }
}

// 获取难度标签
const getDifficultyTag = (difficulty) => {
  const map = {
    BEGINNER: { text: '初级', icon: '🌱', color: '#67c23a' },
    INTERMEDIATE: { text: '中级', icon: '🌿', color: '#e6a23c' },
    ADVANCED: { text: '高级', icon: '🌳', color: '#f56c6c' }
  }
  return map[difficulty] || { text: '', icon: '', color: '' }
}

// 判断是否可编辑
const isEditable = (tutorial) => {
  return tutorial.status === 'DRAFT' || tutorial.status === 'REJECTED'
}

// 判断是否可提交
const canSubmit = (tutorial) => {
  return tutorial.status === 'DRAFT' || tutorial.status === 'REJECTED'
}

// 判断是否可删除
const canDelete = (tutorial) => {
  return tutorial.status === 'DRAFT'
}

// 根据Tab筛选数据
const filteredTutorials = computed(() => {
  switch (activeTab.value) {
    case 'draft':
      return myTutorials.value.filter(t => t.status === 'DRAFT')
    case 'pending':
      return myTutorials.value.filter(t => t.status === 'PENDING')
    case 'published':
      return myTutorials.value.filter(t => t.status === 'PUBLISHED')
    case 'rejected':
      return myTutorials.value.filter(t => t.status === 'REJECTED')
    default:
      return myTutorials.value
  }
})

// 页面加载时获取数据
onMounted(() => {
  fetchMyTutorials()
})
</script>

<template>
  <div class="my-tutorials-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="breadcrumb-nav">
          <el-breadcrumb separator=">">
            <el-breadcrumb-item
              @click="() => router.push('/user/home/tutorials')"
              style="color: #e8eaf5; font-weight: 500; font-size: 0.929rem /* 原值: 13px */; cursor: pointer; transition: all 0.3s;"
              onmouseover="this.style.color='#ffffff'"
              onmouseout="this.style.color='#e8eaf5'"
            >
              教程广场
            </el-breadcrumb-item>
            <el-breadcrumb-item style="color: #ffffff; font-weight: 600; font-size: 0.929rem /* 原值: 13px */;">
              我的教程
            </el-breadcrumb-item>
          </el-breadcrumb>
          <div class="title-section">
            <h1>我的教程</h1>
            <p class="subtitle">管理和分享你的美食制作技巧</p>
          </div>
        </div>
        <el-button type="primary" @click="publishNew" class="publish-btn" size="large">
          <el-icon><Plus /></el-icon> 发布新教程
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card total">
        <div class="stat-icon">
          <el-icon :size="28"><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">全部教程</div>
        </div>
      </div>

      <div class="stat-card draft">
        <div class="stat-icon">
          <span class="icon-emoji">📝</span>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.draft }}</div>
          <div class="stat-label">草稿</div>
        </div>
      </div>

      <div class="stat-card pending">
        <div class="stat-icon">
          <span class="icon-emoji">⏳</span>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.pending }}</div>
          <div class="stat-label">待审核</div>
        </div>
      </div>

      <div class="stat-card published">
        <div class="stat-icon">
          <span class="icon-emoji">✅</span>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.published }}</div>
          <div class="stat-label">已发布</div>
        </div>
      </div>

      <div class="stat-card rejected">
        <div class="stat-icon">
          <span class="icon-emoji">❌</span>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.rejected }}</div>
          <div class="stat-label">已拒绝</div>
        </div>
      </div>
    </div>

    <!-- Tab切换和内容 -->
    <div class="content-card">
      <el-tabs v-model="activeTab" class="custom-tabs">
        <el-tab-pane name="all">
          <template #label>
            <span class="tab-label">
              <span class="tab-icon">📚</span>
              全部教程
              <el-badge v-if="stats.total > 0" :value="stats.total" class="tab-badge" />
            </span>
          </template>
        </el-tab-pane>

        <el-tab-pane name="draft">
          <template #label>
            <span class="tab-label">
              <span class="tab-icon">📝</span>
              草稿
              <el-badge v-if="stats.draft > 0" :value="stats.draft" class="tab-badge" />
            </span>
          </template>
        </el-tab-pane>

        <el-tab-pane name="pending">
          <template #label>
            <span class="tab-label">
              <span class="tab-icon">⏳</span>
              待审核
              <el-badge v-if="stats.pending > 0" :value="stats.pending" class="tab-badge" />
            </span>
          </template>
        </el-tab-pane>

        <el-tab-pane name="published">
          <template #label>
            <span class="tab-label">
              <span class="tab-icon">✅</span>
              已发布
              <el-badge v-if="stats.published > 0" :value="stats.published" class="tab-badge" />
            </span>
          </template>
        </el-tab-pane>

        <el-tab-pane name="rejected">
          <template #label>
            <span class="tab-label">
              <span class="tab-icon">❌</span>
              已拒绝
              <el-badge v-if="stats.rejected > 0" :value="stats.rejected" class="tab-badge" />
            </span>
          </template>
        </el-tab-pane>
      </el-tabs>

      <!-- 教程列表 -->
      <div v-loading="loading" class="tutorial-list">
        <el-empty v-if="!loading && filteredTutorials.length === 0" description="暂无教程">
          <template #image>
            <div class="empty-icon">
              <el-icon :size="100" color="#ddd"><Document /></el-icon>
            </div>
          </template>
          <el-button type="primary" @click="publishNew" :icon="Plus">
            发布第一个教程
          </el-button>
        </el-empty>

        <div v-else class="tutorial-grid">
          <div
            v-for="tutorial in filteredTutorials"
            :key="tutorial.id"
            class="tutorial-card"
            @click="viewTutorial(tutorial)"
          >
            <!-- 教程封面 -->
            <div class="tutorial-cover" v-if="tutorial.coverImage">
              <img :src="tutorial.coverImage" :alt="tutorial.title" />
              <div class="cover-type-badge" :class="tutorial.type">
                <el-icon v-if="tutorial.type === 'video'"><VideoCamera /></el-icon>
                <el-icon v-else><Document /></el-icon>
              </div>
            </div>
            <div class="tutorial-cover placeholder" :class="`type-${tutorial.type}`" v-else>
              <div class="placeholder-content">
                <div class="placeholder-icon">
                  <el-icon :size="64">
                    <VideoCamera v-if="tutorial.type === 'video'" />
                    <Document v-else />
                  </el-icon>
                </div>
                <div class="placeholder-text">
                  <span class="placeholder-type">{{ tutorial.type === 'video' ? '视频教程' : '图文指南' }}</span>
                  <span class="placeholder-hint">暂无封面</span>
                </div>
              </div>
              <div class="cover-type-badge" :class="tutorial.type">
                <el-icon v-if="tutorial.type === 'video'"><VideoCamera /></el-icon>
                <el-icon v-else><Document /></el-icon>
              </div>
            </div>

            <!-- 教程内容 -->
            <div class="tutorial-content">
              <!-- 状态标签 -->
              <div class="status-tags">
                <el-tag
                  :type="getStatusTag(tutorial.status).type"
                  :effect="getStatusTag(tutorial.status).effect"
                  size="small"
                  class="status-tag"
                >
                  <span class="tag-icon">{{ getStatusTag(tutorial.status).icon }}</span>
                  {{ getStatusTag(tutorial.status).text }}
                </el-tag>
                <el-tag
                  v-if="tutorial.difficulty"
                  size="small"
                  effect="plain"
                  class="difficulty-tag"
                  :style="{ color: getDifficultyTag(tutorial.difficulty).color }"
                >
                  <span class="tag-icon">{{ getDifficultyTag(tutorial.difficulty).icon }}</span>
                  {{ getDifficultyTag(tutorial.difficulty).text }}
                </el-tag>
              </div>

              <h3 class="tutorial-title">{{ tutorial.title }}</h3>

              <div class="tutorial-meta">
                <div class="meta-item" v-if="tutorial.duration">
                  <el-icon><Clock /></el-icon>
                  <span>{{ tutorial.duration }}</span>
                </div>
                <div class="meta-item" v-if="tutorial.view_count !== undefined">
                  <el-icon><View /></el-icon>
                  <span>{{ tutorial.view_count?.toLocaleString() || 0 }} 次浏览</span>
                </div>
                <div class="meta-item" v-if="tutorial.rating > 0">
                  <el-icon><Star /></el-icon>
                  <span>{{ tutorial.rating }} ({{ tutorial.rating_count }}人评价)</span>
                </div>
              </div>

              <div class="tutorial-footer">
                <span class="create-time">{{ tutorial.create_time?.split(' ')[0] || '' }}</span>
                <div class="action-buttons" @click.stop>
                  <el-button
                    v-if="isEditable(tutorial)"
                    type="primary"
                    size="small"
                    @click="editTutorial(tutorial)"
                  >
                    <el-icon><Edit /></el-icon> 编辑
                  </el-button>
                  <el-button
                    v-if="canSubmit(tutorial)"
                    type="success"
                    size="small"
                    @click="submitForReview(tutorial)"
                  >
                    <el-icon><Promotion /></el-icon> 提交审核
                  </el-button>
                  <el-button
                    v-if="canDelete(tutorial)"
                    type="danger"
                    size="small"
                    @click="deleteTutorial(tutorial)"
                  >
                    <el-icon><Delete /></el-icon> 删除
                  </el-button>
                  <el-button
                    v-if="tutorial.status === 'PUBLISHED'"
                    type="info"
                    size="small"
                    @click="viewTutorial(tutorial)"
                  >
                    <el-icon><View /></el-icon> 查看
                  </el-button>
                </div>
              </div>
            </div>

            <!-- 装饰性渐变 -->
            <div class="card-decoration" :class="getStatusTag(tutorial.status).type"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';

.my-tutorials-container {
  padding: @nordic-space-lg;
  background: linear-gradient(180deg, @nordic-bg 0%, @nordic-surface 100%);
  min-height: calc(100vh - 60px);

  .page-header {
    margin-bottom: @nordic-space-lg;

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 28px @nordic-space-xl;
      background: linear-gradient(135deg, @nordic-accent 0%, @nordic-blue 100%);
      border-radius: @nordic-radius-lg;
      box-shadow: 0 8px 24px fade(@nordic-accent, 25%);

      .breadcrumb-nav {
        display: flex;
        flex-direction: column;
        gap: @nordic-space-sm + 4px;

        :deep(.el-breadcrumb) {
          .el-breadcrumb__separator {
            color: @nordic-border !important;
          }

          .el-breadcrumb__inner {
            color: @nordic-border !important;
            font-weight: 500 !important;
            font-size: @nordic-text-sm !important;
            transition: all 0.3s ease;
          }

          .el-breadcrumb__item {
            &:hover .el-breadcrumb__inner {
              color: @nordic-surface !important;
              text-shadow: 0 0 8px rgba(255, 255, 255, 0.5) !important;
            }
          }

          .el-breadcrumb__item:last-child {
            .el-breadcrumb__inner {
              color: @nordic-surface !important;
              font-weight: 600 !important;
            }
          }
        }

        .title-section {
          h1 {
            margin: 0 0 6px 0;
            font-size: 30px;
            font-weight: 800;
            color: @nordic-surface;
            letter-spacing: 0.5px;
            line-height: 1.3;
          }

          .subtitle {
            margin: 0;
            font-size: @nordic-text-base;
            font-weight: 400;
            color: rgba(255, 255, 255, 0.75);
            letter-spacing: 0.3px;
            line-height: 1.5;
          }
        }
      }

      .publish-btn {
        background: @nordic-surface;
        border: none;
        color: @nordic-accent;
        font-weight: 600;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        box-shadow: 0 4px 12px rgba(255, 255, 255, 0.3);

        &:hover {
          background: @nordic-bg;
          transform: translateY(-2px) scale(1.02);
          box-shadow: 0 8px 20px rgba(255, 255, 255, 0.4);
        }

        &:active {
          transform: translateY(0) scale(0.98);
        }
      }
    }
  }

  .stats-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: @nordic-space-md;
    margin-bottom: @nordic-space-lg;

    .stat-card {
      .nordic-card();
      border-radius: @nordic-radius-md;
      padding: 20px @nordic-space-lg;
      display: flex;
      align-items: center;
      gap: @nordic-space-md;
      box-shadow: 0 2px 12px @nordic-shadow;
      transition: all 0.3s;
      position: relative;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        width: 4px;
        height: 100%;
      }

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px @nordic-shadow-hover;
      }

      &.total::before {
        background: linear-gradient(180deg, @nordic-accent 0%, @nordic-blue 100%);
      }

      &.draft::before {
        background: linear-gradient(180deg, @nordic-text-muted 0%, @nordic-text-secondary 100%);
      }

      &.pending::before {
        background: linear-gradient(180deg, @nordic-yellow 0%, #d9983a 100%);
      }

      &.published::before {
        background: linear-gradient(180deg, @nordic-green 0%, #5abd34 100%);
      }

      &.rejected::before {
        background: linear-gradient(180deg, @nordic-red 0%, #f35858 100%);
      }

      .stat-icon {
        width: 56px;
        height: 56px;
        border-radius: @nordic-radius-md;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;

        .total & {
          background: linear-gradient(135deg, @nordic-accent 0%, @nordic-blue 100%);
          color: @nordic-surface;
        }

        .draft & {
          background: linear-gradient(135deg, @nordic-text-muted 0%, @nordic-text-secondary 100%);
        }

        .pending & {
          background: linear-gradient(135deg, @nordic-yellow 0%, #d9983a 100%);
        }

        .published & {
          background: linear-gradient(135deg, @nordic-green 0%, #5abd34 100%);
        }

        .rejected & {
          background: linear-gradient(135deg, @nordic-red 0%, #f35858 100%);
        }

        .icon-emoji {
          font-size: 2rem /* 原值: 28px */;
        }
      }

      .stat-info {
        .stat-value {
          font-size: 2rem /* 原值: 28px */;
          font-weight: bold;
          color: @nordic-text;
          margin-bottom: 4px;
          line-height: 1;
        }

        .stat-label {
          font-size: @nordic-text-sm;
          color: @nordic-text-muted;
        }
      }
    }
  }

  .content-card {
    .nordic-card();
    border-radius: @nordic-radius-lg;
    box-shadow: 0 2px 12px @nordic-shadow;
    overflow: hidden;

    .custom-tabs {
      padding: 0 @nordic-space-lg;

      :deep(.el-tabs__header) {
        margin: 0;
        border-bottom: 2px solid @nordic-divider;
      }

      :deep(.el-tabs__nav-wrap::after) {
        display: none;
      }

      :deep(.el-tabs__item) {
        padding: 20px @nordic-space-lg;
        font-size: 1.071rem /* 原值: 15px */;
        font-weight: 500;
        color: @nordic-text-secondary;

        &.is-active {
          color: @nordic-accent;
        }

        .tab-label {
          display: flex;
          align-items: center;
          gap: @nordic-space-sm;

          .tab-icon {
            font-size: 1.286rem /* 原值: 18px */;
          }

          .tab-badge {
            margin-left: 4px;
          }
        }
      }

      :deep(.el-tabs__active-bar) {
        background-color: @nordic-accent;
        height: 3px;
        border-radius: 3px 3px 0 0;
      }
    }

    .tutorial-list {
      padding: @nordic-space-lg;
      min-height: 400px;

      .tutorial-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
        gap: @nordic-space-lg;

        .tutorial-card {
          background: @nordic-surface;
          border: 1px solid @nordic-border;
          border-radius: @nordic-radius-md;
          overflow: hidden;
          cursor: pointer;
          transition: all 0.3s;
          position: relative;

          &:hover {
            transform: translateY(-8px);
            box-shadow: 0 12px 32px @nordic-shadow-hover;
            border-color: @nordic-accent;
          }

          .tutorial-cover {
            position: relative;
            height: 180px;
            overflow: hidden;

            img {
              width: 100%;
              height: 100%;
              object-fit: cover;
            }

            &.placeholder {
              display: flex;
              align-items: center;
              justify-content: center;
              color: @nordic-surface;
              position: relative;

              // 视频教程的渐变背景
              &.type-video {
                background: linear-gradient(135deg, @nordic-red 0%, #ff8e8e 50%, #ffa8a8 100%);
              }

              // 图文指南的渐变背景
              &.type-article {
                background: linear-gradient(135deg, @nordic-yellow 0%, #ffcc80 50%, #ffd699 100%);
              }

              // 装饰性图案
              &::before {
                content: '';
                position: absolute;
                top: -50%;
                left: -50%;
                width: 200%;
                height: 200%;
                background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
                animation: rotate 20s linear infinite;
              }

              @keyframes rotate {
                from { transform: rotate(0deg); }
                to { transform: rotate(360deg); }
              }

              .placeholder-content {
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                gap: 12px;
                z-index: 1;
                position: relative;

                .placeholder-icon {
                  opacity: 0.9;
                  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.15));
                  animation: float 3s ease-in-out infinite;
                }

                @keyframes float {
                  0%, 100% { transform: translateY(0); }
                  50% { transform: translateY(-8px); }
                }

                .placeholder-text {
                  display: flex;
                  flex-direction: column;
                  align-items: center;
                  gap: 4px;

                  .placeholder-type {
                    font-size: 1.143rem /* 原值: 16px */;
                    font-weight: 600;
                    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                  }

                  .placeholder-hint {
                    font-size: 0.857rem /* 原值: 12px */;
                    opacity: 0.85;
                    font-weight: 400;
                  }
                }
              }
            }

            .cover-type-badge {
              position: absolute;
              top: 12px;
              right: 12px;
              width: 36px;
              height: 36px;
              border-radius: @nordic-radius-sm;
              display: flex;
              align-items: center;
              justify-content: center;
              backdrop-filter: blur(10px);
              color: @nordic-surface;
              z-index: 2;

              &.video {
                background: rgba(212, 123, 123, 0.9);
              }

              &.article {
                background: rgba(226, 180, 85, 0.9);
              }
            }
          }

          .tutorial-content {
            padding: 20px;

            .status-tags {
              display: flex;
              gap: @nordic-space-sm;
              margin-bottom: 12px;
              flex-wrap: wrap;

              .status-tag,
              .difficulty-tag {
                border-radius: 20px;
                font-weight: 500;

                .tag-icon {
                  margin-right: 4px;
                }
              }
            }

            .tutorial-title {
              font-size: 1.286rem /* 原值: 18px */;
              font-weight: 700;
              color: @nordic-text;
              margin: 0 0 12px 0;
              line-height: 1.5;
              overflow: hidden;
              text-overflow: ellipsis;
              display: -webkit-box;
              -webkit-line-clamp: 2;
              -webkit-box-orient: vertical;
              min-height: 54px;
            }

            .tutorial-meta {
              display: flex;
              gap: @nordic-space-md;
              margin-bottom: @nordic-space-md;
              flex-wrap: wrap;

              .meta-item {
                display: flex;
                align-items: center;
                gap: 4px;
                font-size: @nordic-text-sm;
                color: @nordic-text-muted;

                .el-icon {
                  font-size: 1rem /* 原值: 14px */;
                }
              }
            }

            .tutorial-footer {
              display: flex;
              justify-content: space-between;
              align-items: center;
              padding-top: 12px;
              border-top: 1px solid @nordic-divider;

              .create-time {
                font-size: @nordic-text-xs;
                color: @nordic-text-muted;
              }

              .action-buttons {
                display: flex;
                gap: @nordic-space-sm;
              }
            }
          }

          .card-decoration {
            position: absolute;
            bottom: 0;
            left: 0;
            right: 0;
            height: 4px;
            opacity: 0;
            transition: opacity 0.3s;

            &.info {
              background: linear-gradient(90deg, @nordic-text-muted 0%, @nordic-text-secondary 100%);
            }

            &.warning {
              background: linear-gradient(90deg, @nordic-yellow 0%, #d9983a 100%);
            }

            &.success {
              background: linear-gradient(90deg, @nordic-green 0%, #5abd34 100%);
            }

            &.danger {
              background: linear-gradient(90deg, @nordic-red 0%, #f35858 100%);
            }
          }

          &:hover .card-decoration {
            opacity: 1;
          }
        }
      }
    }

    .empty-icon {
      margin-bottom: 20px;
      animation: float 3s ease-in-out infinite;
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@media (max-width: 1200px) {
  .my-tutorials-container {
    .stats-cards {
      grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
    }

    .content-card {
      .tutorial-list {
        .tutorial-grid {
          grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .my-tutorials-container {
    padding: @nordic-space-md;

    .page-header {
      .header-content {
        flex-direction: column;
        gap: @nordic-space-md;
        padding: 20px;

        .breadcrumb-nav {
          width: 100%;
          gap: @nordic-space-sm;

          :deep(.el-breadcrumb) {
            font-size: @nordic-text-sm;

            .breadcrumb-item,
            .breadcrumb-current {
              font-size: @nordic-text-sm;
            }
          }

          .title-section {
            h1 {
              font-size: 22px;
            }

            .subtitle {
              font-size: @nordic-text-sm;
            }
          }
        }

        .publish-btn {
          width: 100%;
        }
      }
    }

    .stats-cards {
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;

      .stat-card {
        padding: @nordic-space-md;

        .stat-icon {
          width: 48px;
          height: 48px;
        }

        .stat-info {
          .stat-value {
            font-size: 1.714rem /* 原值: 24px */;
          }

          .stat-label {
            font-size: @nordic-text-xs;
          }
        }
      }
    }

    .content-card {
      .custom-tabs {
        padding: 0 @nordic-space-md;

        :deep(.el-tabs__item) {
          padding: @nordic-space-md 12px;
          font-size: @nordic-text-base;

          .tab-label {
            gap: 4px;

            .tab-icon {
              font-size: 1.143rem /* 原值: 16px */;
            }
          }
        }
      }

      .tutorial-list {
        padding: @nordic-space-md;

        .tutorial-grid {
          grid-template-columns: 1fr;
          gap: @nordic-space-md;
        }
      }
    }
  }
}
</style>
