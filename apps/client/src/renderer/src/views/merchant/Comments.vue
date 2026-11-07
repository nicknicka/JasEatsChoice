<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ArrowUp,
  ArrowDown,
  Search,
  ChatLineRound,
  Star,
  TrendCharts,
  Check,
  Clock,
  ChatDotRound,
  Edit,
  Refresh,
  Promotion,
  Ticket,
  Food
} from '@element-plus/icons-vue'
import { useAuthStore } from '../../store/authStore'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import AppCard from '../../components/common/AppCard.vue'
import AppEmptyState from '../../components/common/AppEmptyState.vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import reviewApi from '../../api/review'

const authStore = useAuthStore()

// 获取商家ID - 确保是字符串类型
const merchantId = authStore.merchantId
  ? String(authStore.merchantId)
  : localStorage.getItem('auth_merchantId')

// 快捷回复模板
const quickReplies = ref([
  '感谢您的评价！我们会继续努力提供更好的服务和菜品。',
  '非常抱歉给您带来不好的体验，我们会立即改进。',
  '感谢您的认可，期待您的再次光临！',
  '感谢您的宝贵意见，我们会认真参考。'
])

// 选择快捷回复
const selectQuickReply = (reply) => {
  replyComment.value = reply
}

// 筛选选项
const filterStatusOptions = [
  { label: '全部', value: 'all' },
  { label: '待回复', value: 'unreplied' },
  { label: '已回复', value: 'replied' }
]

const filterRatingOptions = [
  { label: '全部', value: 'all' },
  { label: '5星', value: '5' },
  { label: '4星', value: '4' },
  { label: '3星', value: '3' },
  { label: '2星', value: '2' },
  { label: '1星', value: '1' }
]

// 评价数据
const comments = ref([])
const loading = ref(false)

// 筛选条件
const activeStatusFilter = ref('all') // all, unreplied, replied
const activeRatingFilter = ref('all') // all, 5,4,3,2,1
const searchKeyword = ref('')

// 筛选后的评价
const filteredComments = ref([])

// 单个评论的回复/追评展开状态管理 (key: comment.id, value: boolean)
const isReplyExpanded = ref({})

// 评价统计
const commentsStats = ref({
  total: 0,
  avgRating: 0,
  ratingCounts: { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 },
  repliedCount: 0,
  unrepliedCount: 0
})

// 计算好评率
const positiveRate = computed(() => {
  if (commentsStats.value.total === 0) return 0
  const positiveCount = commentsStats.value.ratingCounts[5] + commentsStats.value.ratingCounts[4]
  return ((positiveCount / commentsStats.value.total) * 100).toFixed(1)
})

// 计算回复率
const replyRate = computed(() => {
  if (commentsStats.value.total === 0) return 0
  return ((commentsStats.value.repliedCount / commentsStats.value.total) * 100).toFixed(1)
})

// 评分柱宽度：无数据时为 0，首帧到实值之间由 CSS transition 形成生长动效（§3.5）
const ratingPercent = (rating) => {
  const { total, ratingCounts } = commentsStats.value
  if (!total) return '0%'
  return `${((ratingCounts[rating] || 0) / total) * 100}%`
}

// 清空全部筛选条件（空状态的替代动作，§4.5）
const clearFilters = () => {
  activeStatusFilter.value = 'all'
  activeRatingFilter.value = 'all'
  searchKeyword.value = ''
  updateFilter()
}

// 加载评价列表
const loadComments = async () => {
  if (!merchantId) {
    ElMessage.warning('请先登录商家账号')
    return
  }

  loading.value = true
  try {
    const params = {}
    if (activeStatusFilter.value !== 'all') {
      params.status = activeStatusFilter.value
    }
    // 注意：后端不支持评分范围查询，需要在前端进行过滤
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }

    const response = await reviewApi.getMerchantReviews(merchantId, params)

    // 后端返回的数据在 response.data 中
    if (response.data) {
      if (response.data.success) {
        comments.value = response.data.data || []
        // 对每个评论的回复按时间升序排序（早的在上，新的在下）
        comments.value.forEach(comment => {
          if (comment.replies && comment.replies.length > 0) {
            comment.replies.sort((a, b) => {
              return new Date(a.time) - new Date(b.time)
            })
          }
        })
        // 在前端进行所有筛选
        applyFilters()
      } else {
        console.warn('- API返回失败:', response.data.message)
      }
    }
  } catch (error) {
    console.error('加载评价列表失败:', error)
    ElMessage.error('加载评价列表失败')
  } finally {
    loading.value = false
  }
}

// 应用所有筛选条件
const applyFilters = () => {
  let result = [...comments.value]

  // 评分筛选（支持半星）
  if (activeRatingFilter.value !== 'all') {
    const { min, max } = getRatingRange(activeRatingFilter.value)
    result = result.filter(comment => {
      const rating = comment.rating
      return rating >= min && rating < max
    })
  }

  // 状态筛选（如果后端没有处理）
  if (activeStatusFilter.value !== 'all') {
    result = result.filter(comment => comment.status === activeStatusFilter.value)
  }

  filteredComments.value = result
}

// 加载评价统计
const loadStatistics = async () => {
  if (!merchantId) return

  try {
    const response = await reviewApi.getMerchantStatistics(merchantId)
    if (response.data && response.data.success) {
      commentsStats.value = response.data.data
    }
  } catch (error) {
    console.error('加载评价统计失败:', error)
  }
}

// 更新筛选
const updateFilter = () => {
  // 如果有搜索关键词或状态筛选，需要重新从后端获取数据
  // 如果只是评分筛选，可以在前端过滤
  if (searchKeyword.value || activeStatusFilter.value !== 'all') {
    loadComments()
  } else {
    // 仅评分筛选，直接在前端过滤
    applyFilters()
  }
}

// 获取评分范围
const getRatingRange = (ratingValue) => {
  if (ratingValue === 'all') return { min: 0, max: 5 }
  const rating = parseFloat(ratingValue)
  // 向上取整：5星筛选>=4.5，4星筛选>=3.5且<4.5，以此类推
  const min = rating - 0.5
  const max = rating + 0.5
  return { min, max }
}

// 回复评价
const replyComment = ref('')
const currentComment = ref(null)
const showReplyDialog = ref(false)
const submitLoading = ref(false)

const openReplyDialog = (comment) => {
  currentComment.value = comment
  replyComment.value = ''
  showReplyDialog.value = true
}

const submitReply = async () => {
  if (!replyComment.value.trim() || !currentComment.value) {
    ElMessage.warning('请输入回复内容')
    return
  }

  if (!merchantId) {
    ElMessage.warning('请先登录商家账号')
    return
  }

  submitLoading.value = true
  try {
    const response = await reviewApi.replyReview(currentComment.value.id, {
      content: replyComment.value,
      merchantId: merchantId
    })

    if (response.data && response.data.success) {
      ElMessage.success('回复成功')
      showReplyDialog.value = false
      replyComment.value = ''
      currentComment.value = null
      // 重新加载数据
      await loadComments()
      await loadStatistics()
    } else {
      ElMessage.error(response.data?.message || '回复失败')
    }
  } catch (error) {
    console.error('回复评价失败:', error)
    ElMessage.error('回复失败')
  } finally {
    submitLoading.value = false
  }
}

// 刷新数据
const refreshData = async () => {
  await Promise.all([loadComments(), loadStatistics()])
  ElMessage.success('数据已刷新')
}

// 页面加载时获取数据
onMounted(() => {
  loadComments()
  loadStatistics()
})
</script>

<template>
  <div class="merchant-comments-container">
    <!-- 页头：merchant 紧凑版，「返回」「刷新」收右侧动作区且均为次级（§4.1、§4.7） -->
    <AppPageHeader title="评价中心" subtitle="查看评分表现并回复用户评价" compact>
      <template #actions>
        <CommonBackButton type="default" />
        <el-button :loading="loading" :icon="Refresh" @click="refreshData">刷新</el-button>
      </template>
    </AppPageHeader>

    <!-- KPI：4 卡闭合等宽，图标统一品牌浅底线性图标，数值统一中性色（§4.8、§5.6） -->
    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-icon"><el-icon><Star /></el-icon></div>
        <div class="stat-value">{{ commentsStats.avgRating.toFixed(1) }}</div>
        <div class="stat-label">平均评分</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon"><el-icon><TrendCharts /></el-icon></div>
        <div class="stat-value">{{ commentsStats.total }}</div>
        <div class="stat-label">总评价数</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon"><el-icon><Check /></el-icon></div>
        <div class="stat-value">{{ commentsStats.repliedCount }}</div>
        <div class="stat-label">已回复 ({{ replyRate }}%)</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon"><el-icon><Clock /></el-icon></div>
        <div class="stat-value">{{ commentsStats.unrepliedCount }}</div>
        <div class="stat-label">待回复</div>
      </div>
    </div>

    <!-- 筛选工具条：状态 / 评分 chip + 搜索 + 结果数，规范取 Orders 页同款 chip（§3.3、§4.3） -->
    <AppCard class="toolbar-card" padding="md">
      <div class="toolbar">
        <div class="filter-group">
          <span class="filter-label">状态</span>
          <button
            v-for="status in filterStatusOptions"
            :key="status.value"
            type="button"
            class="filter-chip"
            :class="{ 'is-active': activeStatusFilter === status.value }"
            @click="
              () => {
                activeStatusFilter = status.value
                updateFilter()
              }
            "
          >
            {{ status.label }}
          </button>
        </div>

        <div class="filter-group">
          <span class="filter-label">评分</span>
          <button
            v-for="rating in filterRatingOptions"
            :key="rating.value"
            type="button"
            class="filter-chip"
            :class="{ 'is-active': activeRatingFilter === rating.value }"
            @click="
              () => {
                activeRatingFilter = rating.value
                updateFilter()
              }
            "
          >
            {{ rating.label }}
          </button>
        </div>

        <div class="toolbar-right">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索订单号/用户名称/菜品名称"
            clearable
            size="small"
            class="search-input"
            @input="updateFilter"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <span class="result-count">共 <strong>{{ filteredComments.length }}</strong> 条评价</span>
        </div>
      </div>
    </AppCard>

    <!-- 评价列表：主角，位于 KPI 之后、首屏可见（§4.8 统计卡不抢首屏） -->
    <div class="comments-list" v-loading="loading">
      <article
        v-for="(comment, index) in filteredComments"
        :key="comment.id"
        class="comment-item"
        :style="{ animationDelay: `${Math.min(index, 8) * 0.05}s` }"
      >
        <header class="comment-header">
          <div class="user-info">
            <el-avatar :size="40" class="user-avatar">{{ comment.userName.charAt(0) }}</el-avatar>
            <div class="user-details">
              <span class="user-name">{{ comment.userName }}</span>
              <div class="order-info">
                <span class="order-no">
                  <el-icon><Ticket /></el-icon>
                  {{ comment.orderNo }}
                </span>
                <span class="time">
                  <el-icon><Clock /></el-icon>
                  {{ comment.time }}
                </span>
              </div>
            </div>
          </div>

          <div class="rating-info">
            <el-rate :model-value="comment.rating" disabled show-score />
            <StatusTag :status="comment.status" size="sm" />
          </div>
        </header>

        <div class="comment-body">
          <div v-if="comment.dishes && comment.dishes.length" class="comment-dishes">
            <span class="dish-label"><el-icon><Food /></el-icon>菜品</span>
            <el-tag
              v-for="dish in comment.dishes"
              :key="dish"
              size="small"
              type="info"
              effect="plain"
              class="dish-tag"
            >
              {{ dish }}
            </el-tag>
          </div>

          <div class="comment-text">
            <div class="comment-label">
              <el-icon><ChatLineRound /></el-icon>
              用户评价
            </div>
            <p class="comment-value">{{ comment.content }}</p>
          </div>

          <!-- 所有回复（商家回复 + 用户追评混合，按时间排序） -->
          <div v-if="comment.replies && comment.replies.length > 0" class="comment-replies">
            <div
              v-for="(reply, replyIndex) in isReplyExpanded[comment.id]
                ? comment.replies
                : comment.replies.slice(0, 3)"
              :key="reply.id || replyIndex"
              class="comment-reply"
              :class="{ 'comment-reply--followup': !reply.isMerchant }"
            >
              <div class="reply-label">
                <el-icon><ChatDotRound /></el-icon>
                <span>{{ reply.isMerchant ? '商家回复' : '追评' }}</span>
                <span class="reply-time">（{{ reply.time }}）</span>
              </div>
              <p class="reply-value">{{ reply.content }}</p>
            </div>

            <button
              v-if="comment.replies.length > 3"
              type="button"
              class="reply-expand"
              @click="isReplyExpanded[comment.id] = !isReplyExpanded[comment.id]"
            >
              <span>{{
                isReplyExpanded[comment.id] ? '收起' : `查看全部 ${comment.replies.length} 条回复`
              }}</span>
              <el-icon>
                <ArrowUp v-if="isReplyExpanded[comment.id]" />
                <ArrowDown v-else />
              </el-icon>
            </button>
          </div>
        </div>

        <footer class="comment-actions">
          <el-button type="primary" link :icon="Edit" @click="openReplyDialog(comment)">
            {{ comment.status === 'unreplied' ? '回复评价' : '追加回复' }}
          </el-button>
        </footer>
      </article>

      <!-- 空状态：中文说明 + 替代动作（§4.5） -->
      <AppEmptyState
        v-if="filteredComments.length === 0 && !loading"
        description="当前筛选条件下没有评价，清除筛选可查看全部"
        action-text="清除筛选条件"
        @action="clearFilters"
      />
    </div>

    <!-- 分析卡：评分分布 + 数据概览合并为一张（P1），柱色按星级取语义色阶（§3.1、§5.3） -->
    <AppCard class="overview-section" padding="md">
      <header class="analysis-header">
        <h2 class="analysis-title">评分分析</h2>
      </header>

      <div class="analysis-body">
        <div class="rating-bars">
          <div v-for="rating in [5, 4, 3, 2, 1]" :key="rating" class="rating-bar-item">
            <span class="rating-label">{{ rating }}星</span>
            <div class="rating-track">
              <div
                class="rating-fill"
                :class="`rating-fill--${rating}`"
                :style="{ width: ratingPercent(rating) }"
              ></div>
            </div>
            <span class="rating-count">{{ commentsStats.ratingCounts[rating] }}</span>
          </div>
          <p class="rating-note">半星评分已向上取整统计</p>
        </div>

        <dl class="overview-stats">
          <div class="overview-stat-item">
            <dt class="stat-item-label">好评率</dt>
            <dd class="stat-item-value">{{ positiveRate }}%</dd>
          </div>
          <div class="overview-stat-item">
            <dt class="stat-item-label">回复率</dt>
            <dd class="stat-item-value">{{ replyRate }}%</dd>
          </div>
          <div class="overview-stat-item">
            <dt class="stat-item-label">平均评分</dt>
            <dd class="stat-item-value">{{ commentsStats.avgRating.toFixed(1) }}</dd>
          </div>
        </dl>
      </div>
    </AppCard>

    <!-- 回复对话框 -->
    <el-dialog
      v-model="showReplyDialog"
      :title="currentComment?.status === 'replied' ? '追加回复' : '回复评价'"
      width="600px"
      class="reply-dialog"
    >
      <div class="dialog-content">
        <!-- 快捷回复 -->
        <div class="quick-replies">
          <div class="quick-replies-label">快捷回复</div>
          <div class="quick-replies-list">
            <el-tag
              v-for="(reply, index) in quickReplies"
              :key="index"
              @click="selectQuickReply(reply)"
              class="quick-reply-tag"
            >
              {{ reply }}
            </el-tag>
          </div>
        </div>

        <!-- 回复输入框 -->
        <el-input
          v-model="replyComment"
          type="textarea"
          placeholder="请输入回复内容..."
          :rows="5"
          maxlength="200"
          show-word-limit
          resize="none"
        />
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showReplyDialog = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" :icon="Promotion" @click="submitReply">
            提交回复
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 本页颜色、间距、圆角、字阶、动效全部取自 tokens.css 的 CSS 变量（§3、§7.1），无裸色值。 */
.merchant-comments-container {
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: var(--card-gap-compact);
  padding: 0 var(--space-6) var(--space-6);
}

.merchant-comments-container :deep(.app-page-header__actions .el-button) {
  height: var(--control-height-md);
}

/* ---------- KPI（§4.8：浅底 + 统一线性图标，禁止彩色渐变图标块） ---------- */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--card-gap-compact);
}

.stat-card {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  align-items: center;
  column-gap: var(--space-3);
  padding: var(--card-padding-compact);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--surface);
  box-shadow: inset 0 1px 0 var(--surface-highlight);
}

.stat-icon {
  grid-column: 1;
  grid-row: 1 / span 2;
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  background: var(--brand-50);
  color: var(--brand-500);
  font-size: var(--text-h2);
}

.stat-value {
  grid-column: 2;
  color: var(--ink-900);
  font-family: var(--font-num);
  font-size: var(--text-h1);
  font-weight: var(--weight-bold);
  font-variant-numeric: tabular-nums;
  letter-spacing: var(--tracking-tight);
  line-height: normal;
}

.stat-label {
  grid-column: 2;
  margin-top: var(--space-1);
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  line-height: normal;
}

/* ---------- 筛选工具条（§3.3 间距、§4.3 筛选条） ---------- */
.toolbar-card {
  margin: 0;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-3);
}

.filter-group {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.filter-label {
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  white-space: nowrap;
}

/* chip 规范与 merchant/02-orders 同款（§4.1 次级动作、§4.6 四态） */
.filter-chip {
  display: inline-flex;
  align-items: center;
  height: var(--control-height-sm);
  padding: 0 var(--space-3);
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  background: var(--surface);
  color: var(--ink-500);
  font-family: inherit;
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  white-space: nowrap;
  cursor: pointer;
  transition:
    background-color var(--duration-fast) var(--ease-out),
    border-color var(--duration-fast) var(--ease-out),
    color var(--duration-fast) var(--ease-out);
}

.filter-chip:hover {
  border-color: var(--brand-400);
  background: var(--brand-50);
  color: var(--brand-600);
}

.filter-chip:focus-visible {
  outline: 2px solid var(--brand-400);
  outline-offset: 2px;
}

.filter-chip.is-active {
  border-color: var(--brand-500);
  background: var(--brand-500);
  color: var(--surface);
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-left: auto;
}

.search-input {
  width: 240px;
}

.result-count {
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  white-space: nowrap;
}

.result-count strong {
  color: var(--ink-900);
  font-family: var(--font-num);
  font-variant-numeric: tabular-nums;
}

/* ---------- 评价列表（主角） ---------- */
.comments-list {
  display: flex;
  flex-direction: column;
  gap: var(--card-gap-compact);
  min-height: 120px;
}

.comment-item {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding: var(--card-padding-compact);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--surface);
  box-shadow: inset 0 1px 0 var(--surface-highlight);
  animation: comment-enter var(--duration-base) var(--ease-standard) both;
}

@keyframes comment-enter {
  from {
    opacity: 0;
    transform: translateY(4px);
  }
  to {
    opacity: 1;
    transform: none;
  }
}

.comment-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  min-width: 0;
}

.user-avatar {
  flex-shrink: 0;
  background: var(--brand-100);
  color: var(--brand-700);
  font-weight: var(--weight-semibold);
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  min-width: 0;
}

.user-name {
  color: var(--ink-900);
  font-size: var(--text-body);
  font-weight: var(--weight-semibold);
  line-height: 22px;
}

.order-info {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  line-height: 20px;
}

.order-no,
.time {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
}

.rating-info {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: var(--space-3);
}

.comment-body {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.comment-dishes {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
}

.dish-label {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  color: var(--ink-500);
  font-size: var(--text-body-sm);
}

/* 菜品标签是中性信息而非状态，覆盖 Element 的 info 蓝为中性地暖色（§3.1、§5.3） */
.comment-dishes :deep(.el-tag.dish-tag.el-tag--info) {
  --el-tag-bg-color: var(--neutral-bg);
  --el-tag-border-color: var(--line);
  --el-tag-text-color: var(--ink-700);
}

.dish-tag {
  margin: 0;
}

.comment-text {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.comment-label,
.reply-label {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
}

.comment-value,
.reply-value {
  margin: 0;
  color: var(--ink-700);
  font-size: var(--text-body);
  line-height: 22px;
  overflow-wrap: anywhere;
}

.comment-replies {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--space-3);
  border-radius: var(--radius-md);
  background: var(--canvas-sunken);
}

.comment-reply {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.comment-reply--followup .reply-label {
  color: var(--ink-400);
}

.reply-time {
  color: var(--ink-400);
}

.reply-expand {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  align-self: flex-start;
  padding: 0;
  border: 0;
  background: none;
  color: var(--brand-600);
  font-family: inherit;
  font-size: var(--text-body-sm);
  cursor: pointer;
  transition: color var(--duration-fast) var(--ease-out);
}

.reply-expand:hover {
  color: var(--brand-700);
}

.reply-expand:focus-visible {
  outline: 2px solid var(--brand-400);
  outline-offset: 2px;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
}

/* ---------- 分析卡：评分分布 + 数据概览（P1 合并） ---------- */
.overview-section {
  margin: 0;
}

.analysis-header {
  margin-bottom: var(--space-4);
}

.analysis-title {
  margin: 0;
  color: var(--ink-900);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  line-height: 24px;
}

.analysis-body {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(0, 1fr);
  gap: var(--space-8);
}

.rating-bars {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.rating-bar-item {
  display: grid;
  grid-template-columns: 40px minmax(0, 1fr) 32px;
  align-items: center;
  gap: var(--space-3);
}

.rating-label {
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  white-space: nowrap;
}

.rating-track {
  height: 8px;
  border-radius: var(--radius-full);
  background: var(--neutral-bg);
  overflow: hidden;
}

.rating-fill {
  height: 100%;
  border-radius: var(--radius-full);
  transition: width var(--duration-slow) var(--ease-out);
}

/* 评分柱按星级取语义色阶：好评绿 → 一般黄 → 差评红（§3.1、§5.3） */
.rating-fill--5,
.rating-fill--4 {
  background: var(--success);
}

.rating-fill--3 {
  background: var(--warning);
}

.rating-fill--2,
.rating-fill--1 {
  background: var(--danger);
}

.rating-count {
  color: var(--ink-900);
  font-family: var(--font-num);
  font-size: var(--text-body-sm);
  font-variant-numeric: tabular-nums;
  text-align: right;
}

.rating-note {
  margin: var(--space-1) 0 0;
  color: var(--ink-400);
  font-size: var(--text-caption);
  line-height: 18px;
}

/* 概览数值统一中性色，仅在阈值告警时才着语义色（§5.2、§5.3） */
.overview-stats {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  margin: 0;
  padding-left: var(--space-8);
  border-left: 1px solid var(--line);
}

.overview-stat-item {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: var(--space-3);
}

.stat-item-label {
  color: var(--ink-500);
  font-size: var(--text-body-sm);
}

.stat-item-value {
  margin: 0;
  color: var(--ink-900);
  font-family: var(--font-num);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  font-variant-numeric: tabular-nums;
}

/* ---------- 回复对话框 ---------- */
.dialog-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.quick-replies {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.quick-replies-label {
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
}

.quick-replies-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.quick-reply-tag {
  cursor: pointer;
  transition:
    background-color var(--duration-fast) var(--ease-out),
    border-color var(--duration-fast) var(--ease-out);
}

.quick-reply-tag:hover {
  border-color: var(--brand-400);
  background: var(--brand-50);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-2);
}

@media (max-width: 1024px) {
  .stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .analysis-body {
    grid-template-columns: minmax(0, 1fr);
  }

  .overview-stats {
    padding-left: 0;
    border-left: 0;
  }
}
</style>
