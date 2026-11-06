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
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import AppEmptyState from '../../components/common/AppEmptyState.vue'

const router = useRouter()

// 数据
const myTutorials = ref([])
const loading = ref(false)
const activeTab = ref('all')
const failedCovers = ref({})

// 状态筛选的数量（只驱动 tabs，不再另立统计卡）
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

// 状态文案统一走一份词典（§3.1、P1 统一状态色与状态文字）
const STATUS_TEXT = {
  DRAFT: '草稿',
  PENDING: '待审核',
  PUBLISHED: '已发布',
  REJECTED: '已拒绝'
}
const statusText = (status) => STATUS_TEXT[status] || status

// 难度不是语义状态，只用中性标签文字，不参与状态配色
const DIFFICULTY_TEXT = {
  BEGINNER: '初级',
  INTERMEDIATE: '中级',
  ADVANCED: '高级'
}
const difficultyText = (difficulty) => DIFFICULTY_TEXT[difficulty] || ''

// 封面：加载失败立刻退回统一空封面，不再露出 alt 文本（P0）
const hasCover = (tutorial) => Boolean(tutorial.coverImage) && !failedCovers.value[tutorial.id]
const markCoverFailed = (id) => {
  failedCovers.value = { ...failedCovers.value, [id]: true }
}

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

// 空状态要说清为什么空 + 下一步做什么（§4.5）
const emptyDescription = computed(() => {
  if (myTutorials.value.length === 0) {
    return '还没有教程，发布你的第一条美食制作教程吧'
  }
  const label = {
    draft: '草稿',
    pending: '待审核',
    published: '已发布',
    rejected: '已拒绝'
  }[activeTab.value]
  return label ? `当前没有${label}状态的教程，可切换到其他状态查看` : '还没有教程'
})

// 页面加载时获取数据
onMounted(() => {
  fetchMyTutorials()
})
</script>

<template>
  <div class="my-tutorials-container">
    <!-- 页头：标准版标题 --text-h1 + 上下 --space-6，返回放右侧动作区（§4.7） -->
    <AppPageHeader title="我的教程" subtitle="管理和分享你的美食制作技巧">
      <template #actions>
        <CommonBackButton type="default" />
        <el-button type="primary" @click="publishNew">
          <el-icon><Plus /></el-icon> 发布新教程
        </el-button>
      </template>
    </AppPageHeader>

    <!-- 状态筛选：全空时收起，避免空状态与筛选重复（§4.5、§4.8） -->
    <div v-if="myTutorials.length > 0" class="filter-bar">
      <el-tabs v-model="activeTab" class="custom-tabs">
        <el-tab-pane name="all">
          <template #label>
            <span class="tab-label">
              全部教程
              <el-badge v-if="stats.total > 0" :value="stats.total" class="tab-badge" />
            </span>
          </template>
        </el-tab-pane>

        <el-tab-pane name="draft">
          <template #label>
            <span class="tab-label">
              草稿
              <el-badge v-if="stats.draft > 0" :value="stats.draft" class="tab-badge" />
            </span>
          </template>
        </el-tab-pane>

        <el-tab-pane name="pending">
          <template #label>
            <span class="tab-label">
              待审核
              <el-badge v-if="stats.pending > 0" :value="stats.pending" class="tab-badge" />
            </span>
          </template>
        </el-tab-pane>

        <el-tab-pane name="published">
          <template #label>
            <span class="tab-label">
              已发布
              <el-badge v-if="stats.published > 0" :value="stats.published" class="tab-badge" />
            </span>
          </template>
        </el-tab-pane>

        <el-tab-pane name="rejected">
          <template #label>
            <span class="tab-label">
              已拒绝
              <el-badge v-if="stats.rejected > 0" :value="stats.rejected" class="tab-badge" />
            </span>
          </template>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 教程列表 -->
    <div v-loading="loading" class="tutorial-list">
      <AppEmptyState
        v-if="!loading && filteredTutorials.length === 0"
        :description="emptyDescription"
        action-text="发布新教程"
        :icon="Document"
        @action="publishNew"
      />

      <div v-else :key="activeTab" class="tutorial-grid">
        <article
          v-for="tutorial in filteredTutorials"
          :key="tutorial.id"
          class="tutorial-card"
          tabindex="0"
          @click="viewTutorial(tutorial)"
          @keydown.enter="viewTutorial(tutorial)"
        >
          <!-- 教程封面：失败或缺失都退回同一套空封面（P0） -->
          <div class="tutorial-cover">
            <img
              v-if="hasCover(tutorial)"
              :src="tutorial.coverImage"
              :alt="tutorial.title"
              @error="markCoverFailed(tutorial.id)"
            />
            <div v-else class="tutorial-cover__placeholder">
              <el-icon :size="40">
                <VideoCamera v-if="tutorial.type === 'video'" />
                <Document v-else />
              </el-icon>
              <span>{{ tutorial.type === 'video' ? '视频教程' : '图文指南' }}</span>
            </div>
            <span class="tutorial-cover__badge">
              <el-icon>
                <VideoCamera v-if="tutorial.type === 'video'" />
                <Document v-else />
              </el-icon>
              {{ tutorial.type === 'video' ? '视频' : '图文' }}
            </span>
          </div>

          <!-- 教程内容 -->
          <div class="tutorial-content">
            <div class="tutorial-tags">
              <StatusTag :status="tutorial.status" :text="statusText(tutorial.status)" size="sm" />
              <el-tag v-if="tutorial.difficulty" size="small" effect="plain" class="difficulty-tag">
                {{ difficultyText(tutorial.difficulty) }}
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
              <span class="create-time">{{ tutorial.created_at?.split(' ')[0] || '' }}</span>
              <div class="action-buttons" @click.stop>
                <!-- 草稿：编辑 / 提交审核 / 删除 -->
                <template v-if="tutorial.status === 'DRAFT'">
                  <el-button type="primary" link @click="editTutorial(tutorial)">
                    <el-icon><Edit /></el-icon> 编辑
                  </el-button>
                  <el-button link @click="submitForReview(tutorial)">
                    <el-icon><Promotion /></el-icon> 提交审核
                  </el-button>
                  <el-button type="danger" link @click="deleteTutorial(tutorial)">
                    <el-icon><Delete /></el-icon> 删除
                  </el-button>
                </template>
                <!-- 已拒绝：重新提交 / 编辑 -->
                <template v-else-if="tutorial.status === 'REJECTED'">
                  <el-button type="primary" link @click="submitForReview(tutorial)">
                    <el-icon><Promotion /></el-icon> 重新提交
                  </el-button>
                  <el-button link @click="editTutorial(tutorial)">
                    <el-icon><Edit /></el-icon> 编辑
                  </el-button>
                </template>
                <!-- 待审核：查看审核 -->
                <template v-else-if="tutorial.status === 'PENDING'">
                  <el-button type="primary" link @click="viewTutorial(tutorial)">
                    <el-icon><View /></el-icon> 查看审核
                  </el-button>
                </template>
                <!-- 已发布：查看 -->
                <template v-else>
                  <el-button type="primary" link @click="viewTutorial(tutorial)">
                    <el-icon><View /></el-icon> 查看
                  </el-button>
                </template>
              </div>
            </div>
          </div>
        </article>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
/* 颜色、间距、圆角、字阶全部来自 tokens.css（§3、§7.1）。 */
.my-tutorials-container {
  box-sizing: border-box;
  width: 100%;
  max-width: var(--page-max);
  min-height: 100%;
  margin: 0 auto;
  padding: 0 var(--page-gutter) var(--space-10);
  background: var(--canvas);
}

/* 页头动作区控件高度取 --control-height-md（§3.4、§4.7）。 */
.my-tutorials-container :deep(.app-page-header__actions .el-button) {
  height: var(--control-height-md);
  min-height: var(--control-height-md);
}

/* 状态筛选条：本页唯一的状态入口，不与统计卡重复（§4.5、§4.8）。 */
.filter-bar {
  padding: 0 var(--space-5);
  margin-bottom: var(--card-gap);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--surface);
  box-shadow: inset 0 1px 0 var(--surface-highlight);

  :deep(.el-tabs__header) {
    margin: 0;
  }

  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }

  :deep(.el-tabs__item) {
    padding: var(--space-4);
    font-size: var(--text-body);
    font-weight: var(--weight-medium);
    color: var(--ink-500);
    transition: color var(--duration-fast) var(--ease-standard);
  }

  :deep(.el-tabs__item.is-active) {
    color: var(--brand-600);
  }

  :deep(.el-tabs__active-bar) {
    height: 2px;
    border-radius: var(--radius-full);
    background-color: var(--brand-500);
  }

  :deep(.el-badge__content) {
    border: none;
    background-color: var(--neutral-bg);
    color: var(--ink-700);
  }

  .tab-label {
    display: inline-flex;
    align-items: center;
    gap: var(--space-2);
  }

  .tab-badge {
    margin-left: var(--space-1);
  }
}

.tutorial-list {
  min-height: 320px;
}

.tutorial-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--card-gap);
  animation: grid-enter var(--duration-base) var(--ease-out) both;
}

/* 列表进入：只做一次性位移 + 淡入，时长/曲线取 token（§3.5） */
@keyframes grid-enter {
  from {
    opacity: 0;
    transform: translateY(4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 教程卡：L2 浮起，hover 抬升不超过 2px（§3.4） */
.tutorial-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--surface);
  box-shadow: var(--shadow-sm), inset 0 1px 0 var(--surface-highlight);
  cursor: pointer;
  transition:
    transform var(--duration-fast) var(--ease-standard),
    box-shadow var(--duration-fast) var(--ease-standard),
    border-color var(--duration-fast) var(--ease-standard);

  &:hover {
    transform: translateY(-2px);
    border-color: var(--brand-300);
    box-shadow: var(--shadow-md), inset 0 1px 0 var(--surface-highlight);
  }

  &:focus-visible {
    outline: 2px solid var(--brand-400);
    outline-offset: 2px;
  }
}

.tutorial-cover {
  position: relative;
  height: 176px;
  background: var(--canvas-sunken);

  img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

/* 统一空封面：中性表面，不用彩色渐变铺底（§3.4、§7.1） */
.tutorial-cover__placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  height: 100%;
  color: var(--ink-400);
  font-size: var(--text-body-sm);
}

.tutorial-cover__badge {
  position: absolute;
  top: var(--space-3);
  right: var(--space-3);
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  background: var(--surface-scrim);
  backdrop-filter: blur(8px);
  color: var(--ink-700);
  font-size: var(--text-caption);
  font-weight: var(--weight-medium);
}

.tutorial-content {
  display: flex;
  flex-direction: column;
  flex: 1;
  gap: var(--space-3);
  padding: var(--card-padding);
}

.tutorial-tags {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.difficulty-tag {
  border-color: var(--line-strong);
  border-radius: var(--radius-full);
  background: var(--surface);
  color: var(--ink-500);
}

.tutorial-title {
  margin: 0;
  min-height: 48px;
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  line-height: 24px;
  color: var(--ink-900);
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.tutorial-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-4);
  font-size: var(--text-body-sm);
  color: var(--ink-500);
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
}

.tutorial-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  margin-top: auto;
  padding-top: var(--space-3);
  border-top: 1px solid var(--line);
}

.create-time {
  font-size: var(--text-caption);
  color: var(--ink-400);
  font-variant-numeric: tabular-nums;
}

/* 卡片动作取 --control-height-sm（§3.4） */
.action-buttons {
  display: flex;
  align-items: center;
  gap: var(--space-2);

  :deep(.el-button) {
    height: var(--control-height-sm);
    min-height: var(--control-height-sm);
    padding: 0;
    font-size: var(--text-body-sm);
  }
}

@media (max-width: 768px) {
  .my-tutorials-container {
    padding: 0 var(--space-4) var(--space-8);
  }

  .filter-bar {
    padding: 0 var(--space-3);

    :deep(.el-tabs__item) {
      padding: var(--space-3) var(--space-2);
      font-size: var(--text-body-sm);
    }
  }

  .tutorial-grid {
    grid-template-columns: 1fr;
  }
}
</style>
