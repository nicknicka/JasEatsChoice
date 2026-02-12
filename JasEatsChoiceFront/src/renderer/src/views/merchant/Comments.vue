<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import { useAuthStore } from '../../store/authStore'
import reviewApi from '../../api/review'

const authStore = useAuthStore()

// 获取商家ID
const merchantId = authStore.merchantId || localStorage.getItem('auth_merchantId')

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
    if (activeRatingFilter.value !== 'all') {
      params.rating = parseInt(activeRatingFilter.value)
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }

    const response = await reviewApi.getMerchantReviews(merchantId, params)
    if (response.success) {
      comments.value = response.data
      filteredComments.value = response.data
    }
  } catch (error) {
    console.error('加载评价列表失败:', error)
    ElMessage.error('加载评价列表失败')
  } finally {
    loading.value = false
  }
}

// 加载评价统计
const loadStatistics = async () => {
  if (!merchantId) return

  try {
    const response = await reviewApi.getReviewStatistics(merchantId)
    if (response.success) {
      commentsStats.value = response.data
    }
  } catch (error) {
    console.error('加载评价统计失败:', error)
  }
}

// 更新筛选
const updateFilter = () => {
  loadComments()
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

    if (response.success) {
      ElMessage.success('回复成功')
      showReplyDialog.value = false
      replyComment.value = ''
      currentComment.value = null
      // 重新加载数据
      await loadComments()
      await loadStatistics()
    } else {
      ElMessage.error(response.message || '回复失败')
    }
  } catch (error) {
    console.error('回复评价失败:', error)
    ElMessage.error('回复失败')
  } finally {
    submitLoading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadComments()
  loadStatistics()
})
</script>

<template>
  <div class="merchant-comments-container">
    <div class="comments-header">
      <div class="header-left">
        <h3 class="page-title">【评价中心】</h3>
      </div>
    </div>

    <!-- 评价统计概览 -->
    <div class="overview-section">
      <el-row :gutter="20">
        <el-col :span="24">
          <div class="overview-card">
            <div class="overview-header">
              <h4 class="overview-title">📊 评价概览</h4>
            </div>
            <div class="overview-content">
              <el-row :gutter="20">
                <el-col :span="6">
                  <div class="stat-card">
                    <div class="stat-value">{{ commentsStats.avgRating.toFixed(1) }}</div>
                    <div class="stat-label">⭐ 平均评分</div>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="stat-card">
                    <div class="stat-value">{{ commentsStats.total }}</div>
                    <div class="stat-label">💬 总评价数</div>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="stat-card">
                    <div class="stat-value">{{ commentsStats.repliedCount }}</div>
                    <div class="stat-label">✅ 已回复</div>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="stat-card">
                    <div class="stat-value">{{ commentsStats.unrepliedCount }}</div>
                    <div class="stat-label">📝 待回复</div>
                  </div>
                </el-col>
              </el-row>

              <div class="rating-distribution">
                <h5 class="distribution-title">评分分布</h5>
                <div class="rating-bars">
                  <div v-for="rating in [5, 4, 3, 2, 1]" :key="rating" class="rating-bar-item">
                    <div class="rating-label">{{ ratingTextMap[rating] }}</div>
                    <el-progress
                      :percentage="
                        (commentsStats.ratingCounts[rating] / commentsStats.total) * 100 || 0
                      "
                      :stroke-width="10"
                      :color="rating >= 4 ? '#67C23A' : rating === 3 ? '#E6A23C' : '#F56C6C'"
                      striped
                      striped-flow
                    />
                    <div class="rating-count">{{ commentsStats.ratingCounts[rating] }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 评价筛选和搜索 -->
    <div class="filter-section">
      <el-row>
        <div class="filter-group">
          <div class="filter-row">
            <div class="filter-item">
              <div class="filter-label">📋 状态筛选：</div>
              <el-tag
                v-for="status in ['all', 'unreplied', 'replied']"
                :key="status"
                :type="activeStatusFilter === status ? 'primary' : 'info'"
                effect="plain"
                @click="
                  () => {
                    activeStatusFilter = status
                    updateFilter()
                  }
                "
                class="filter-tag"
              >
                {{ status === 'all' ? '全部' : status === 'unreplied' ? '未回复' : '已回复' }}
              </el-tag>
            </div>
            <div class="filter-item">
              <div class="filter-label">⭐ 评分筛选：</div>
              <el-tag
                v-for="rating in ['all', '5', '4', '3', '2', '1']"
                :key="rating"
                :type="activeRatingFilter === rating ? 'primary' : 'info'"
                effect="plain"
                @click="
                  () => {
                    activeRatingFilter = rating
                    updateFilter()
                  }
                "
                class="filter-tag"
              >
                {{ rating === 'all' ? '全部' : `${rating}分` }}
              </el-tag>
            </div>
            <div class="filter-item search-item">
              <div class="search-group">
                <el-input
                  v-model="searchKeyword"
                  placeholder="输入订单号/用户名称/菜品名称..."
                  clearable
                  @input="updateFilter"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
              </div>
            </div>
          </div>
        </div>
      </el-row>
    </div>

    <!-- 评价列表 -->
    <div class="comments-section">
      <el-card class="comments-card">
        <template #header>
          <div class="comments-header">
            <span>用户评价列表</span>
            <span class="comments-count">共 {{ filteredComments.length }} 条评价</span>
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
                  <el-avatar>{{ comment.userName.charAt(0) }}</el-avatar>
                </div>
                <div class="user-details">
                  <div class="user-name">{{ comment.userName }}</div>
                  <div class="order-info">
                    <span class="order-no">订单号：{{ comment.orderNo }}</span>
                    <span class="time">⏰ {{ comment.time }}</span>
                  </div>
                </div>
              </div>

              <div class="rating-info">
                <el-tag :type="ratingTagTypeMap[comment.rating]" size="small">
                  {{ ratingTextMap[comment.rating] }}
                </el-tag>
                <el-tag v-if="comment.status === 'unreplied'" type="warning" size="small">
                  未回复
                </el-tag>
                <el-tag v-if="comment.status === 'replied'" type="success" size="small">
                  已回复
                </el-tag>
              </div>
            </div>

            <div class="comment-content">
              <div class="comment-dishes">
                <span class="dish-label">🍽️ 菜品：</span>
                <el-tag
                  v-for="dish in comment.dishes"
                  :key="dish"
                  size="small"
                  type="info"
                  class="dish-tag"
                >
                  {{ dish }}
                </el-tag>
              </div>

              <div class="comment-text">
                <div class="comment-label">💬 用户评价：</div>
                <div class="comment-value">{{ comment.content }}</div>
              </div>

              <!-- 所有回复（包括原回复和追评） -->
              <div v-if="comment.reply || (comment.replies && comment.replies.length > 0)">
                <!-- 将原回复和追评整合为一个数组 -->
                <div class="all-replies">
                  <!-- 原回复 -->
                  <div v-if="comment.reply" class="comment-reply">
                    <div class="reply-label">📨 商家回复：</div>
                    <div class="reply-value">{{ comment.reply }}</div>
                  </div>
                  <!-- 追评列表 - 只显示前2个或全部 -->
                  <div
                    v-for="(reply, index) in isReplyExpanded[comment.id]
                      ? comment.replies || []
                      : (comment.replies || []).slice(0, 2 - (comment.reply ? 1 : 0))"
                    :key="index"
                    class="comment-reply comment-reply-followup"
                  >
                    <div class="reply-label">🔔 商家追评 ({{ reply.time }})：</div>
                    <div class="reply-value">{{ reply.content }}</div>
                  </div>
                  <!-- 展开/折叠按钮 -->
                  <div
                    v-if="1 + (comment.reply ? 1 : 0) + (comment.replies?.length || 0) > 3"
                    class="reply-expand-btn"
                    @click="isReplyExpanded[comment.id] = !isReplyExpanded[comment.id]"
                  >
                    <span class="btn-text">{{
                      isReplyExpanded[comment.id]
                        ? '收起'
                        : '查看所有 ' +
                          (1 + (comment.reply ? 1 : 0) + (comment.replies?.length || 0)) +
                          ' 条评价'
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
              <el-button type="primary" size="small" plain @click="openReplyDialog(comment)">
                {{ comment.status === 'unreplied' ? '回复评价' : '追评' }}
              </el-button>
            </div>
          </div>

          <!-- 空数据提示 -->
          <div v-if="filteredComments.length === 0" class="empty-comments">
            <el-empty description="暂无评价">
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
      :title="currentComment?.status === 'replied' ? '追评' : '回复评价'"
      width="500px"
    >
      <el-input
        v-model="replyComment"
        type="textarea"
        placeholder="请输入回复内容"
        :rows="4"
        maxlength="200"
        show-word-limit
      />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showReplyDialog = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitReply">提交回复</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.merchant-comments-container {
  padding: 20px;
  background-color: #f5f7fa;

  .comments-header {
    margin-bottom: 20px;

    .page-title {
      font-size: 24px;
      font-weight: 600;
      margin: 0;
      color: #303133;
    }
  }

  // 概览卡片
  .overview-section {
    margin-bottom: 20px;

    .overview-card {
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
      overflow: hidden;

      .overview-header {
        padding: 20px;
        border-bottom: 1px solid #eee;

        .overview-title {
          margin: 0;
          font-size: 18px;
          font-weight: 600;
          color: #303133;
        }
      }

      .overview-content {
        padding: 20px;

        .stat-card {
          text-align: center;
          padding: 15px;
          background: #f8f9fa;
          border-radius: 6px;
          transition: all 0.3s;

          &:hover {
            transform: translateY(-3px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
          }

          .stat-value {
            font-size: 28px;
            font-weight: 700;
            color: #409eff;
            margin-bottom: 5px;
          }

          .stat-label {
            font-size: 14px;
            color: #606266;
          }
        }

        .rating-distribution {
          margin-top: 30px;

          .distribution-title {
            font-size: 16px;
            font-weight: 600;
            margin-bottom: 15px;
            color: #303133;
          }

          .rating-bars {
            .rating-bar-item {
              display: flex;
              align-items: center;
              margin-bottom: 15px;

              .rating-label {
                width: 120px;
                font-size: 14px;
                color: #606266;
              }

              :deep(.el-progress) {
                flex: 1;
                margin: 0 15px;

                .el-progress-bar__outer {
                  border-radius: 5px;
                }

                .el-progress-bar__inner {
                  border-radius: 5px;
                }
              }

              .rating-count {
                width: 30px;
                font-size: 14px;
                font-weight: 600;
                color: #303133;
              }
            }
          }
        }
      }
    }
  }

  // 筛选区域
  .filter-section {
    margin-bottom: 20px;

    .filter-group {
      display: flex;
      padding: 15px 20px;
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
      width: 100%;

      .filter-row {
        display: flex;
        flex-direction: row;
        gap: 20px;
        width: 100%;
        flex-wrap: wrap;

        .filter-item {
          display: flex;
          align-items: center;
          flex-wrap: wrap;
          gap: 10px;

          .filter-label {
            font-weight: 600;
            color: #303133;
            white-space: nowrap;
          }

          .filter-tag {
            cursor: pointer;
            transition: all 0.3s;

            &:hover {
              transform: translateY(-2px);
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            }
          }
        }

        .search-item {
          flex: 1;
          min-width: 300px;
        }
      }
    }

    .search-group {
      height: 100%;
      display: flex;
      align-items: center;
      min-width: 300px;

      :deep(.el-input) {
        width: 100%;
        .el-input__wrapper {
          border-radius: 20px;
        }
      }
    }
  }

  // 评论区域
  .comments-section {
    .comments-card {
      border-radius: 8px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

      :deep(.el-card__header) {
        background: #f8f9fa;
        border-bottom: 1px solid #eee;
        padding: 15px 20px;

        .comments-header {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .comments-count {
            font-size: 14px;
            color: #606266;
          }
        }
      }

      .comments-list {
        .comment-item {
          padding: 20px;
          border: 1px solid #ebeef5;
          border-radius: 8px;
          margin-bottom: 15px;
          background-color: #fff;
          transition: all 0.3s ease;
          animation: slideInUp 0.4s ease-out forwards;
          opacity: 0;

          &:hover {
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
            transform: translateY(-2px);
          }

          .comment-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 15px;

            .user-info {
              display: flex;
              gap: 12px;

              .user-avatar {
                :deep(.el-avatar) {
                  background-color: #409eff;
                }
              }

              .user-details {
                .user-name {
                  font-weight: 600;
                  font-size: 16px;
                  margin-bottom: 5px;
                  color: #303133;
                }

                .order-info {
                  display: flex;
                  gap: 15px;
                  font-size: 13px;
                  color: #606266;

                  .order-no,
                  .time {
                    font-size: 12px;
                  }
                }
              }
            }

            .rating-info {
              display: flex;
              gap: 8px;
            }
          }

          .comment-content {
            margin-bottom: 15px;

            .comment-dishes {
              display: flex;
              align-items: center;
              flex-wrap: wrap;
              gap: 8px;
              margin-bottom: 15px;

              .dish-label {
                font-weight: 500;
                font-size: 14px;
                color: #303133;
              }

              .dish-tag {
                margin: 2px 0;
              }
            }

            .comment-text,
            .comment-reply {
              margin-bottom: 12px;

              .comment-label,
              .reply-label {
                font-weight: 600;
                font-size: 14px;
                margin-bottom: 5px;
                color: #303133;
              }

              .comment-value,
              .reply-value {
                font-size: 14px;
                color: #303133;
                line-height: 1.6;
                padding: 10px;
                border-radius: 4px;
              }

              .comment-value {
                background-color: #f5f7fa;
              }

              .reply-value {
                background-color: #ecf5ff;
                color: #409eff;
                border-left: 3px solid #409eff;
              }
            }

            // 追评样式
            .comment-reply-followup {
              margin-top: 12px;

              .reply-value {
                background-color: #ecf5ff;
                color: #409eff;
                border-left: 3px solid #409eff;
              }

              .reply-label {
                color: #409eff;
                font-weight: 600;
              }
            }

            // 回复展开/折叠按钮
            .reply-expand-btn {
              display: flex;
              align-items: center;
              justify-content: center;
              margin-top: 10px;
              cursor: pointer;
              color: #409eff;
              font-size: 14px;

              .btn-text {
                margin-right: 5px;
              }
            }
          }

          .comment-actions {
            text-align: right;
          }
        }
      }

      .empty-comments {
        padding: 40px 0;
        text-align: center;
      }
    }
  }
}

// 卡片进入动画
@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 概览卡片淡入动画
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

// 为概览卡片添加动画
.overview-section {
  .overview-card {
    animation: fadeIn 0.5s ease-out;
  }
}
</style>
