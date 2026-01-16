<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowUp, ArrowDown } from '@element-plus/icons-vue'

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

// 模拟评价数据
const comments = ref([
  {
    id: 1,
    orderNo: 'JD20241121001',
    user: '小明',
    rating: 5,
    comment: '这家店的食物真的太好吃了！味道很正宗，配送也非常快，下次还会再来的！',
    reply: '',
    status: 'unreplied', // unreplied, replied
    time: '2024-11-21 12:30',
    dishes: ['宫保鸡丁', '鱼香肉丝', '米饭'],
    replies: []
  },
  {
    id: 2,
    orderNo: 'JD20241121002',
    user: '小红',
    rating: 4,
    comment: '味道还不错，就是有点辣，希望下次可以少放一点辣椒。',
    reply: '非常抱歉给您带来不便，下次您可以在订单备注中说明辣度要求哦！',
    status: 'replied',
    time: '2024-11-21 13:15',
    dishes: ['麻婆豆腐', '青菜', '米饭']
  },
  {
    id: 3,
    orderNo: 'JD20241120058',
    user: '小刚',
    rating: 3,
    comment: '食物味道一般，配送有点慢，希望改进。',
    reply: '感谢您的反馈，我们会改进配送速度和菜品质量！',
    status: 'replied',
    time: '2024-11-20 18:45',
    dishes: ['红烧肉', '西红柿炒蛋', '米饭']
  },
  {
    id: 4,
    orderNo: 'JD20241119032',
    user: '小李',
    rating: 5,
    comment: '服务态度很好，食物分量足，味道也很棒，强烈推荐！',
    reply: '',
    status: 'unreplied',
    time: '2024-11-19 20:00',
    dishes: ['水煮鱼', '回锅肉', '米饭']
  },
  {
    id: 5,
    orderNo: 'JD20241118012',
    user: '小王',
    rating: 2,
    comment: '食物凉了，而且分量很少，价格也不便宜，非常不满意！',
    reply: '非常抱歉给您带来不好的体验，我们会加强质量检查！',
    status: 'replied',
    time: '2024-11-18 19:30',
    dishes: ['牛肉面', '拍黄瓜', '可乐']
  }
])

// 筛选条件
const activeStatusFilter = ref('all') // all, unreplied, replied
const activeRatingFilter = ref('all') // all, 5,4,3,2,1
const searchKeyword = ref('')

// 筛选后的评价
const filteredComments = ref([])
filteredComments.value = [...comments.value]

// 单个评论的回复/追评展开状态管理 (key: comment.id, value: boolean)
const isReplyExpanded = ref({})

// 评价统计
const commentsStats = computed(() => {
  const total = filteredComments.value.length
  const ratingCounts = { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 }
  filteredComments.value.forEach((comment) => {
    ratingCounts[comment.rating]++
  })

  // 计算平均评分
  const avgRating =
    total > 0 ? filteredComments.value.reduce((sum, comment) => sum + comment.rating, 0) / total : 0

  const repliedCount = filteredComments.value.filter(
    (comment) => comment.status === 'replied'
  ).length
  const unrepliedCount = total - repliedCount

  return {
    total,
    avgRating,
    ratingCounts,
    repliedCount,
    unrepliedCount
  }
})

// 更新筛选
const updateFilter = () => {
  filteredComments.value = comments.value.filter((comment) => {
    // 状态筛选
    const statusMatch =
      activeStatusFilter.value === 'all' || comment.status === activeStatusFilter.value

    // 评分筛选
    const ratingMatch =
      activeRatingFilter.value === 'all' || comment.rating === parseInt(activeRatingFilter.value)

    // 搜索筛选
    const searchMatch =
      !searchKeyword.value ||
      comment.orderNo.includes(searchKeyword.value) ||
      comment.user.includes(searchKeyword.value) ||
      comment.dishes.some((dish) => dish.includes(searchKeyword.value))

    return statusMatch && ratingMatch && searchMatch
  })
}

// 回复评价
const replyComment = ref('')
const currentComment = ref(null)
const showReplyDialog = ref(false)

const openReplyDialog = (comment) => {
  currentComment.value = comment
  replyComment.value = '' // 追评时清空输入框，准备输入新内容
  showReplyDialog.value = true
}

const submitReply = () => {
  if (!replyComment.value.trim() || !currentComment.value) {
    ElMessage.warning('请输入回复内容')
    return
  }

  // 更新回复内容：如果已经有回复则追加到追评列表，否则直接设置
  if (currentComment.value.status === 'replied' && currentComment.value.reply) {
    // 追加追评，使用数组存储追评
    const now = new Date().toLocaleString('zh-CN')
    // 初始化追评数组
    if (!currentComment.value.replies) {
      currentComment.value.replies = []
    }
    // 添加新追评
    currentComment.value.replies.push({
      content: replyComment.value,
      time: now
    })
  } else {
    // 首次回复
    currentComment.value.reply = replyComment.value
    currentComment.value.status = 'replied'
    // 初始化追评数组
    currentComment.value.replies = []
  }

  updateFilter()
  replyComment.value = ''
  currentComment.value = null
  showReplyDialog.value = false
  ElMessage.success('回复成功')
}

// 页面加载时初始化筛选
updateFilter()
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

        <div class="comments-list">
          <div v-for="comment in filteredComments" :key="comment.id" class="comment-item">
            <div class="comment-header">
              <div class="user-info">
                <div class="user-avatar">
                  <el-avatar>{{ comment.user.charAt(0) }}</el-avatar>
                </div>
                <div class="user-details">
                  <div class="user-name">{{ comment.user }}</div>
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
                <div class="comment-value">{{ comment.comment }}</div>
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
          <el-button type="primary" @click="submitReply">提交回复</el-button>
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
</style>
