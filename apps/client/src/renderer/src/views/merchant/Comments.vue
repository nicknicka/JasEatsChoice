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
  Food,
  InfoFilled
} from '@element-plus/icons-vue'
import { useAuthStore } from '../../store/authStore'
import reviewApi from '../../api/review'

const authStore = useAuthStore()

// 获取商家ID - 确保是字符串类型
const merchantId = authStore.merchantId
  ? String(authStore.merchantId)
  : localStorage.getItem('auth_merchantId')

// 页面加载时调试信息
console.log('📋 Comments页面加载')
console.log('- authStore.merchantId:', authStore.merchantId, '(类型:', typeof authStore.merchantId, ')')
console.log('- localStorage.auth_merchantId:', localStorage.getItem('auth_merchantId'))
console.log('- 最终使用的merchantId:', merchantId, '(类型:', typeof merchantId, ')')

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

// 获取评分对应的颜色
const getRatingColor = (rating) => {
  const colors = {
    5: '#67C23A',
    4: '#95d475',
    3: '#E6A23C',
    2: '#F56C6C',
    1: '#f78989'
  }
  return colors[rating] || '@merchant-text-muted'
}

// 评价评分对应文本
const ratingTextMap = {
  5: '✨ 非常满意',
  4: '👍 满意',
  3: '😐 一般',
  2: '👎 不满意',
  1: '💢 非常不满意'
}

// 评价标签样式
const ratingTagTypeMap = {
  5: 'success',
  4: 'success',
  3: 'warning',
  2: 'danger',
  1: 'danger'
}

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

    // 调试信息
    console.log('🔍 调试信息:')
    console.log('- merchantId类型:', typeof merchantId)
    console.log('- merchantId值:', merchantId)
    console.log('- merchantId === 7638432224340229:', merchantId === 7638432224340229)
    console.log('- merchantId === "7638432224340229":', merchantId === '7638432224340229')
    console.log('- String(merchantId):', String(merchantId))
    console.log('- 请求参数:', params)

    const response = await reviewApi.getMerchantReviews(merchantId, params)
    console.log('- API返回完整响应:', response)
    console.log('- response.data:', response.data)
    console.log('- response.status:', response.status)

    // 后端返回的数据在 response.data 中
    if (response.data) {
      console.log('- response.data.success:', response.data.success)
      console.log('- response.data.data:', response.data.data)
      console.log('- 评价列表长度:', response.data.data?.length || 0)

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
      console.log('评价统计数据:', response.data.data)
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
    <!-- 页面标题 -->
    <div class="comments-header">
      <div class="header-left">
        <h3 class="page-title">
          <el-icon class="title-icon"><ChatLineRound /></el-icon>
          评价中心
        </h3>
      </div>
      <div class="header-right">
        <el-button type="primary" :loading="loading" @click="refreshData" :icon="Refresh">
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 评价统计概览 -->
    <div class="overview-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card stat-card-primary">
            <div class="stat-row-first">
              <div class="stat-icon">
                <el-icon><Star /></el-icon>
              </div>
              <div class="stat-value">{{ commentsStats.avgRating.toFixed(1) }}</div>
            </div>
            <div class="stat-label">平均评分</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card stat-card-success">
            <div class="stat-row-first">
              <div class="stat-icon">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="stat-value">{{ commentsStats.total }}</div>
            </div>
            <div class="stat-label">总评价数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card stat-card-info">
            <div class="stat-row-first">
              <div class="stat-icon">
                <el-icon><Check /></el-icon>
              </div>
              <div class="stat-value">{{ commentsStats.repliedCount }}</div>
            </div>
            <div class="stat-label">已回复 ({{ replyRate }}%)</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card stat-card-warning">
            <div class="stat-row-first">
              <div class="stat-icon">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stat-value">{{ commentsStats.unrepliedCount }}</div>
            </div>
            <div class="stat-label">待回复</div>
          </div>
        </el-col>
      </el-row>

      <!-- 评分分布和好评率 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="16">
          <div class="overview-card">
            <div class="overview-header">
              <h4 class="overview-title">
                <el-icon><TrendCharts /></el-icon>
                评分分布
              </h4>
            </div>
            <div class="overview-content">
              <div class="rating-bars">
                <div v-for="rating in [5, 4, 3, 2, 1]" :key="rating" class="rating-bar-item">
                  <div class="rating-label">
                    <span class="rating-text">{{ rating }}星</span>
                  </div>
                  <el-progress
                    :percentage="
                      (commentsStats.ratingCounts[rating] / commentsStats.total) * 100 || 0
                    "
                    :stroke-width="12"
                    :color="getRatingColor(rating)"
                    :show-text="false"
                  />
                  <div class="rating-count">{{ commentsStats.ratingCounts[rating] }}</div>
                </div>
              </div>
              <div class="rating-note">
                <el-icon><InfoFilled /></el-icon>
                <span>半星评分已向上取整统计</span>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="overview-card">
            <div class="overview-header">
              <h4 class="overview-title">
                <el-icon><TrendCharts /></el-icon>
                数据概览
              </h4>
            </div>
            <div class="overview-content">
              <div class="overview-stats">
                <div class="overview-stat-item">
                  <span class="stat-item-label">好评率</span>
                  <span class="stat-item-value stat-item-success">{{ positiveRate }}%</span>
                </div>
                <div class="overview-stat-item">
                  <span class="stat-item-label">回复率</span>
                  <span class="stat-item-value stat-item-primary">{{ replyRate }}%</span>
                </div>
                <div class="overview-stat-item">
                  <span class="stat-item-label">平均评分</span>
                  <span class="stat-item-value stat-item-warning">
                    {{ commentsStats.avgRating.toFixed(1) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 评价筛选和搜索 -->
    <div class="filter-section">
      <div class="filter-group">
        <div class="filter-row">
          <div class="filter-item">
            <div class="filter-label">
              <el-icon><Check /></el-icon>
              状态筛选
            </div>
            <el-tag
              v-for="status in filterStatusOptions"
              :key="status.value"
              :type="activeStatusFilter === status.value ? 'primary' : 'info'"
              effect="plain"
              @click="
                () => {
                  activeStatusFilter = status.value
                  updateFilter()
                }
              "
              class="filter-tag"
            >
              {{ status.label }}
            </el-tag>
          </div>
          <div class="filter-item">
            <div class="filter-label">
              <el-icon><Star /></el-icon>
              评分筛选
            </div>
            <el-tag
              v-for="rating in filterRatingOptions"
              :key="rating.value"
              :type="activeRatingFilter === rating.value ? 'primary' : 'info'"
              effect="plain"
              @click="
                () => {
                  activeRatingFilter = rating.value
                  updateFilter()
                }
              "
              class="filter-tag"
            >
              {{ rating.label }}
            </el-tag>
          </div>
          <div class="filter-item search-item">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索订单号/用户名称/菜品名称..."
              clearable
              @input="updateFilter"
              class="search-input"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>
      </div>
    </div>

    <!-- 评价列表 -->
    <div class="comments-section">
      <el-card class="comments-card">
        <template #header>
          <div class="comments-list-header">
            <div class="header-title">
              <el-icon><ChatLineRound /></el-icon>
              <span>用户评价列表</span>
            </div>
            <div class="header-actions">
              <el-tag type="info" size="small">
                共 {{ filteredComments.length }} 条评价
              </el-tag>
            </div>
          </div>
        </template>

        <div class="comments-list" v-loading="loading">
          <div
            v-for="(comment, index) in filteredComments"
            :key="comment.id"
            class="comment-item"
            :style="{ animationDelay: `${index * 0.05}s` }"
          >
            <div class="comment-header">
              <div class="user-info">
                <div class="user-avatar">
                  <el-avatar :size="48">{{ comment.userName.charAt(0) }}</el-avatar>
                </div>
                <div class="user-details">
                  <div class="user-name">{{ comment.userName }}</div>
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
                <el-tag
                  v-if="comment.status === 'unreplied'"
                  type="warning"
                  size="small"
                  effect="plain"
                >
                  待回复
                </el-tag>
                <el-tag v-else type="success" size="small" effect="plain">
                  已回复
                </el-tag>
              </div>
            </div>

            <div class="comment-content">
              <div class="comment-dishes">
                <span class="dish-label">
                  <el-icon><Food /></el-icon>
                  菜品：
                </span>
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
                <div class="comment-value">{{ comment.content }}</div>
              </div>

              <!-- 所有回复（商家回复 + 用户追评混合，按时间排序） -->
              <div v-if="comment.replies && comment.replies.length > 0">
                <div class="all-replies">
                  <div
                    v-for="(reply, index) in isReplyExpanded[comment.id]
                      ? comment.replies
                      : comment.replies.slice(0, 3)"
                    :key="reply.id || index"
                    class="comment-reply"
                    :class="reply.isMerchant ? '' : 'comment-reply-followup'"
                  >
                    <div class="reply-label">
                      <el-icon><ChatDotRound /></el-icon>
                      <span>{{ reply.isMerchant ? '商家回复' : '追评' }}</span>
                      <span class="reply-time">({{ reply.time }})</span>
                    </div>
                    <div class="reply-value">{{ reply.content }}</div>
                  </div>
                  <div
                    v-if="comment.replies.length > 3"
                    class="reply-expand-btn"
                    @click="isReplyExpanded[comment.id] = !isReplyExpanded[comment.id]"
                  >
                    <span class="btn-text">{{
                      isReplyExpanded[comment.id]
                        ? '收起'
                        : `查看所有 ${comment.replies.length} 条回复`
                    }}</span>
                    <el-icon class="arrow-icon">
                      <ArrowDown v-if="!isReplyExpanded[comment.id]" />
                      <ArrowUp v-else />
                    </el-icon>
                  </div>
                </div>
              </div>
            </div>

            <div class="comment-actions">
              <el-button type="primary" size="default" @click="openReplyDialog(comment)">
                <el-icon><Edit /></el-icon>
                {{ comment.status === 'unreplied' ? '回复评价' : '追加回复' }}
              </el-button>
            </div>
          </div>

          <!-- 空数据提示 -->
          <div v-if="filteredComments.length === 0 && !loading" class="empty-comments">
            <el-empty
              :image-size="120"
              description="暂无评价数据"
            >
              <el-button
                type="primary"
                @click="
                  () => {
                    activeStatusFilter = 'all'
                    activeRatingFilter = 'all'
                    searchKeyword = ''
                    updateFilter()
                  }
                "
              >
                <el-icon><Refresh /></el-icon>
                清除筛选条件
              </el-button>
            </el-empty>
          </div>
        </div>
      </el-card>
    </div>

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
          <div class="quick-replies-label">快捷回复：</div>
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
          <el-button @click="showReplyDialog = false" size="default">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitReply" size="default">
            <el-icon><Promotion /></el-icon>
            提交回复
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';
@import '../../assets/css/merchant-theme.less';

.merchant-comments-container {
  padding: 20px;
  background: @merchant-bg;
  min-height: 100vh;

  // 页面头部
  .comments-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 20px;
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

    .page-title {
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 1.714rem;
      font-weight: 600;
      margin: 0;
      color: @merchant-text;

      .title-icon {
        font-size: 2rem;
        color: @merchant-info;
      }
    }

    .header-right {
      :deep(.el-button) {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 10px 20px;
        font-size: 0.929rem;
        border-radius: 8px;
        font-weight: 500;
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
        }
      }
    }
  }

  // 概览卡片
  .overview-section {
    margin-bottom: 20px;

    // 统计卡片样式
    .stat-card {
      display: flex;
      flex-direction: column;
      gap: 16px;
      padding: 20px;
      background: white;
      border-radius: 12px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      transition: all 0.3s ease;
      cursor: default;
      min-height: 120px;

      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }

      // 第一行：图标 + 数值
      .stat-row-first {
        display: flex;
        align-items: center;
        gap: 12px;
        flex: 1;

        .stat-icon {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 48px;
          height: 48px;
          border-radius: 10px;
          font-size: 1.5rem;
          color: white;
          flex-shrink: 0;
        }

        .stat-value {
          font-size: 2rem;
          font-weight: 700;
          color: @merchant-text;
          line-height: 1;
          flex: 1;
        }
      }

      // 第二行：描述
      .stat-label {
        font-size: 0.929rem;
        color: @merchant-text-muted;
        text-align: center;
      }

      &.stat-card-primary {
        .stat-icon {
          background: linear-gradient(135deg, @merchant-secondary 0%, @merchant-secondary 100%);
        }
        .stat-value {
          background: linear-gradient(135deg, @merchant-secondary 0%, @merchant-secondary 100%);
          background-clip: text;
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
        }
      }

      &.stat-card-success {
        .stat-icon {
          background: linear-gradient(135deg, @merchant-success-light 0%, lighten(@merchant-success-light, 5%) 100%);
        }
        .stat-value {
          color: @merchant-success;
        }
      }

      &.stat-card-info {
        .stat-icon {
          background: linear-gradient(135deg, @merchant-info-light 0%, lighten(@merchant-info-light, 5%) 100%);
        }
        .stat-value {
          color: @merchant-info;
        }
      }

      &.stat-card-warning {
        .stat-icon {
          background: linear-gradient(135deg, @merchant-warning-light 0%, lighten(@merchant-warning-light, 5%) 100%);
        }
        .stat-value {
          color: @merchant-warning;
        }
      }
    }

    .overview-card {
      background: white;
      border-radius: 12px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      overflow: hidden;
      transition: all 0.3s ease;
      height: 100%;
      display: flex;
      flex-direction: column;

      &:hover {
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
      }

      .overview-header {
        padding: 20px;
        border-bottom: 1px solid @merchant-border;
        background: @merchant-surface-alt;

        .overview-title {
          display: flex;
          align-items: center;
          gap: 8px;
          margin: 0;
          font-size: 1.143rem;
          font-weight: 600;
          color: @merchant-text;

          .el-icon {
            font-size: 1.286rem;
            color: @merchant-info;
          }
        }
      }

      .overview-content {
        padding: 24px;
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: center;

        .overview-stats {
          display: flex;
          flex-direction: column;
          gap: 16px;

          .overview-stat-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 12px 16px;
            background: @merchant-surface-alt;
            border-radius: 8px;

            .stat-item-label {
              font-size: 1rem;
              color: @merchant-text-sec;
              font-weight: 500;
            }

            .stat-item-value {
              font-size: 1.286rem;
              font-weight: 700;

              &.stat-item-success {
                color: @merchant-success;
              }

              &.stat-item-primary {
                color: @merchant-info;
              }

              &.stat-item-warning {
                color: @merchant-warning;
              }
            }
          }
        }

        .rating-bars {
          .rating-bar-item {
            display: flex;
            align-items: center;
            margin-bottom: 16px;

            &:last-child {
              margin-bottom: 0;
            }

            .rating-label {
              width: 80px;
              flex-shrink: 0;
              display: flex;
              align-items: center;
              gap: 4px;

              .rating-text {
                font-size: 1rem;
                font-weight: 600;
                color: @merchant-text-sec;
              }

              :deep(.el-rate) {
                .el-rate__icon {
                  font-size: 18px;
                }
              }
            }

            :deep(.el-progress) {
              flex: 1;
              margin: 0 16px;

              .el-progress-bar__outer {
                border-radius: 6px;
                background-color: @merchant-surface-alt;
              }

              .el-progress-bar__inner {
                border-radius: 6px;
                transition: all 0.3s ease;
              }
            }

            .rating-count {
              width: 40px;
              text-align: right;
              font-size: 1rem;
              font-weight: 600;
              color: @merchant-text;
            }
          }
        }

        .rating-note {
          display: flex;
          align-items: center;
          gap: 6px;
          margin-top: 20px;
          padding: 10px 14px;
          background: @merchant-info-light;
          border-left: 3px solid @merchant-info;
          border-radius: 4px;
          font-size: 0.857rem;
          color: @merchant-text-sec;

          .el-icon {
            color: @merchant-info;
            font-size: 1rem;
          }
        }
      }
    }
  }

  // 筛选区域
  .filter-section {
    margin-bottom: 20px;

    .filter-group {
      padding: 20px;
      background: white;
      border-radius: 12px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

      .filter-row {
        display: flex;
        flex-direction: row;
        gap: 24px;
        align-items: center;
        flex-wrap: wrap;

        .filter-item {
          display: flex;
          align-items: center;
          flex-wrap: wrap;
          gap: 12px;

          .filter-label {
            display: flex;
            align-items: center;
            gap: 6px;
            font-weight: 600;
            font-size: 1rem;
            color: @merchant-text;
            white-space: nowrap;

            .el-icon {
              color: @merchant-info;
            }
          }

          .filter-tag {
            cursor: pointer;
            transition: all 0.3s ease;
            padding: 8px 16px;
            font-size: 0.929rem;

            &:hover {
              transform: translateY(-2px);
              box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
            }
          }
        }

        .search-item {
          flex: 1;
          min-width: 280px;
          margin-left: auto;

          .search-input {
            :deep(.el-input__wrapper) {
              border-radius: 24px;
              padding: 8px 16px;
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
              transition: all 0.3s ease;

              &:hover,
              &.is-focus {
                box-shadow: 0 4px 16px rgba(64, 158, 255, 0.15);
              }
            }
          }
        }
      }
    }
  }

  // 评价列表区域
  .comments-section {
    .comments-card {
      border-radius: 12px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

      :deep(.el-card__header) {
        background: @merchant-surface-alt;
        border-bottom: 1px solid @merchant-border;
        padding: 16px 24px;

        .comments-list-header {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .header-title {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 1.143rem;
            font-weight: 600;
            color: @merchant-text;

            .el-icon {
              color: @merchant-info;
              font-size: 1.286rem;
            }
          }
        }
      }

      .comments-list {
        padding: 16px;

        .comment-item {
          padding: 24px;
          border: 1px solid @merchant-border;
          border-radius: 12px;
          margin-bottom: 16px;
          background: white;
          transition: all 0.3s ease;
          animation: slideInUp 0.5s ease-out forwards;
          opacity: 0;

          &:last-child {
            margin-bottom: 0;
          }

          &:hover {
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
            transform: translateY(-3px);
            border-color: @merchant-info;
          }

          .comment-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 20px;

            .user-info {
              display: flex;
              gap: 16px;

              .user-avatar {
                :deep(.el-avatar) {
                  background: linear-gradient(135deg, @merchant-secondary 0%, @merchant-secondary 100%);
                  font-weight: 600;
                  font-size: 1.286rem;
                }
              }

              .user-details {
                .user-name {
                  font-weight: 600;
                  font-size: 1.143rem;
                  margin-bottom: 8px;
                  color: @merchant-text;
                }

                .order-info {
                  display: flex;
                  gap: 20px;
                  font-size: 0.857rem;
                  color: @merchant-text-muted;

                  .order-no,
                  .time {
                    display: flex;
                    align-items: center;
                    gap: 4px;

                    .el-icon {
                      font-size: 1rem;
                    }
                  }
                }
              }
            }

            .rating-info {
              display: flex;
              flex-direction: column;
              align-items: flex-end;
              gap: 8px;

              :deep(.el-rate) {
                .el-rate__icon {
                  font-size: 20px;
                }
              }
            }
          }

          .comment-content {
            margin-bottom: 16px;

            .comment-dishes {
              display: flex;
              align-items: center;
              flex-wrap: wrap;
              gap: 10px;
              margin-bottom: 16px;
              padding: 12px;
              background: @merchant-surface-alt;
              border-radius: 8px;

              .dish-label {
                display: flex;
                align-items: center;
                gap: 6px;
                font-weight: 600;
                font-size: 1rem;
                color: @merchant-text;

                .el-icon {
                  color: @merchant-warning;
                }
              }

              .dish-tag {
                margin: 2px 0;
                border-radius: 6px;
              }
            }

            .comment-text,
            .comment-reply {
              margin-bottom: 16px;

              .comment-label,
              .reply-label {
                display: flex;
                align-items: center;
                gap: 6px;
                font-weight: 600;
                font-size: 1rem;
                margin-bottom: 10px;
                color: @merchant-text;

                .el-icon {
                  color: @merchant-info;
                }
              }

              .comment-value,
              .reply-value {
                font-size: 1rem;
                color: @merchant-text;
                line-height: 1.8;
                padding: 16px;
                border-radius: 8px;
                transition: all 0.3s ease;
              }

              .comment-value {
                background: linear-gradient(135deg, @merchant-surface-alt 0%, @merchant-border 100%);
                border-left: 4px solid @merchant-info;
              }

              .reply-value {
                background: linear-gradient(135deg, @merchant-info-light 0%, lighten(@merchant-info-light, 5%) 100%);
                color: @merchant-info;
                border-left: 4px solid @merchant-info;
              }
            }

            // 商家回复样式（蓝色系 - 专业商务）
            .comment-reply {
              .reply-label {
                .el-icon {
                  color: @merchant-info;
                }
              }

              .reply-value {
                background: linear-gradient(135deg, @merchant-info-light 0%, lighten(@merchant-info-light, 5%) 100%);
                color: @merchant-info;
                border-left: 4px solid @merchant-info;
              }
            }

            // 用户追评样式（橙色系 - 醒目突出）
            .comment-reply-followup {
              margin-top: 16px;
              margin-bottom: 16px;

              .reply-label {
                .el-icon {
                  color: @merchant-warning;
                }
                color: @merchant-warning;
              }

              .reply-value {
                background: linear-gradient(135deg, @merchant-warning-light 0%, lighten(@merchant-warning-light, 5%) 100%);
                color: @merchant-secondary;
                border-left: 4px solid @merchant-warning;
              }
            }

            .reply-expand-btn {
              display: inline-flex;
              align-items: center;
              gap: 6px;
              padding: 8px 16px;
              margin-top: 12px;
              cursor: pointer;
              color: @merchant-info;
              font-size: 0.929rem;
              border-radius: 6px;
              transition: all 0.3s ease;

              &:hover {
                background: rgba(64, 158, 255, 0.1);
              }

              .arrow-icon {
                transition: transform 0.3s ease;
              }
            }
          }

          .comment-actions {
            display: flex;
            justify-content: flex-end;
            gap: 12px;

            :deep(.el-button) {
              border-radius: 8px;
              padding: 10px 20px;
              font-weight: 500;
              transition: all 0.3s ease;

              &:hover {
                transform: translateY(-2px);
                box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
              }
            }
          }
        }
      }

      .empty-comments {
        padding: 60px 0;
        text-align: center;
      }
    }
  }
}

// 回复对话框
.reply-dialog {
  :deep(.el-dialog__header) {
    padding: 20px 24px;
    border-bottom: 1px solid @merchant-border;
  }

  :deep(.el-dialog__body) {
    padding: 24px;
  }

  .dialog-content {
    .quick-replies {
      margin-bottom: 20px;
      padding: 16px;
      background: @merchant-surface-alt;
      border-radius: 8px;

      .quick-replies-label {
        font-weight: 600;
        font-size: 1rem;
        color: @merchant-text;
        margin-bottom: 12px;
      }

      .quick-replies-list {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;

        .quick-reply-tag {
          cursor: pointer;
          transition: all 0.3s ease;
          max-width: 100%;
          white-space: normal;
          height: auto;
          padding: 8px 14px;
          line-height: 1.5;

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
            background: rgba(64, 158, 255, 0.1);
          }
        }
      }
    }

    :deep(.el-textarea) {
      .el-textarea__inner {
        border-radius: 8px;
        border: 2px solid @merchant-border;
        padding: 12px;
        font-size: 1rem;
        line-height: 1.6;
        transition: all 0.3s ease;

        &:focus {
          border-color: @merchant-info;
          box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
        }
      }
    }
  }

  :deep(.el-dialog__footer) {
    padding: 16px 24px;
    border-top: 1px solid @merchant-border;
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;

    :deep(.el-button) {
      border-radius: 8px;
      padding: 10px 24px;
      font-weight: 500;
    }
  }
}

// 动画定义
@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

// 响应式布局
@media (max-width: 1200px) {
  .merchant-comments-container {
    .overview-section {
      .el-col {
        margin-bottom: 16px;
      }
    }
  }
}

@media (max-width: 768px) {
  .merchant-comments-container {
    padding: 12px;

    .comments-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
    }

    .filter-section {
      .filter-group {
        .filter-row {
          flex-direction: column;
          align-items: stretch;

          .filter-item {
            flex-wrap: wrap;

            .search-item {
              width: 100%;
              margin-left: 0;
            }
          }
        }
      }
    }

    .comments-section {
      .comments-card {
        .comment-item {
          .comment-header {
            flex-direction: column;
            gap: 16px;

            .rating-info {
              align-items: flex-start;
            }
          }
        }
      }
    }
  }
}
</style>
