<template>
  <div class="comments-section">
    <div class="comments-header">
      <h3 class="comments-title">用户评价</h3>
      <div class="comments-stats">
        <div class="average-rating" v-if="merchantRating">
          <div class="rating-number">{{ merchantRating }}</div>
          <div class="rating-stars">
            <el-rate :model-value="merchantRating" disabled size="small" />
          </div>
        </div>
        <div class="total-comments">共 {{ comments.length }} 条评价</div>
      </div>
    </div>

    <div class="comments-list">
      <div class="comment-card" v-for="comment in comments" :key="comment.id">
        <div class="comment-main">
          <div class="comment-avatar">
            <el-icon :size="24"><User /></el-icon>
          </div>
          <div class="comment-body">
            <div class="comment-header">
              <div class="comment-user-info">
                <span class="user-name">{{ comment.userName }}</span>
                <el-tag size="small" class="user-badge">VIP会员</el-tag>
              </div>
              <span class="comment-date">{{ comment.date }}</span>
            </div>
            <div class="comment-rating">
              <el-rate v-model="comment.rating" :disabled="true" size="small" />
            </div>
            <div class="comment-content">
              {{ comment.comment }}
            </div>

            <!-- 展开/折叠回复按钮 -->
            <div v-if="comment.replies && comment.replies.length > 0" class="reply-toggle">
              <el-button
                text
                size="small"
                @click="comment.expandReplies = !comment.expandReplies"
                class="toggle-button"
              >
                <el-icon class="toggle-icon">
                  <component :is="comment.expandReplies ? 'ArrowUp' : 'ArrowDown'" />
                </el-icon>
                {{ comment.expandReplies ? '收起回复' : `查看回复 (${comment.replies.length})` }}
              </el-button>
            </div>
          </div>
        </div>

        <!-- 回复列表 -->
        <div v-if="comment.expandReplies && comment.replies.length > 0" class="replies-wrapper">
          <div class="replies-list">
            <div
              class="reply-card"
              v-for="reply in comment.replies"
              :key="reply.id"
              :class="{ 'merchant-reply': reply.type === 'merchant' }"
            >
              <div class="reply-avatar">
                <el-icon :size="20">
                  <component :is="reply.type === 'merchant' ? 'Shop' : 'User'" />
                </el-icon>
              </div>
              <div class="reply-body">
                <div class="reply-header">
                  <div class="reply-user-info">
                    <span class="reply-username">{{ reply.userName }}</span>
                    <el-tag
                      v-if="reply.type === 'merchant'"
                      size="small"
                      type="success"
                      class="merchant-badge"
                    >
                      商家
                    </el-tag>
                  </div>
                  <span class="reply-date">{{ reply.date }}</span>
                </div>
                <div class="reply-content">
                  {{ reply.comment }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { User, Shop, ArrowUp, ArrowDown } from '@element-plus/icons-vue'

defineProps({
  comments: {
    type: Array,
    default: () => []
  },
  merchantRating: {
    type: Number,
    default: 4.5
  }
})
</script>

<style scoped lang="less">
.comments-section {
  margin-bottom: 32px;

  .comments-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    padding: 20px;
    background: linear-gradient(135deg, #eff6ff 0%, #f0f9ff 100%);
    border-radius: 12px;
    border: 1px solid rgba(59, 130, 246, 0.1);

    .comments-title {
      font-size: 20px;
      font-weight: 700;
      color: #1e40af;
      margin: 0;
    }

    .comments-stats {
      display: flex;
      gap: 24px;
      align-items: center;

      .average-rating {
        display: flex;
        align-items: center;
        gap: 12px;

        .rating-number {
          font-size: 32px;
          font-weight: 700;
          color: #f59e0b;
          line-height: 1;
        }

        .rating-stars {
          display: flex;
          flex-direction: column;
          align-items: flex-start;
        }
      }

      .total-comments {
        font-size: 14px;
        color: #64748b;
        font-weight: 500;
        padding-left: 24px;
        border-left: 2px solid rgba(59, 130, 246, 0.2);
      }
    }
  }

  .comments-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .comment-card {
    background: #ffffff;
    border-radius: 16px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    border: 1px solid rgba(59, 130, 246, 0.08);
    overflow: hidden;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 4px 20px rgba(59, 130, 246, 0.12);
      transform: translateY(-2px);
    }

    .comment-main {
      display: flex;
      gap: 16px;
      padding: 20px;

      .comment-avatar {
        width: 48px;
        height: 48px;
        border-radius: 50%;
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        color: #ffffff;
        flex-shrink: 0;
        box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
      }

      .comment-body {
        flex: 1;
        min-width: 0;

        .comment-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 12px;

          .comment-user-info {
            display: flex;
            align-items: center;
            gap: 8px;

            .user-name {
              font-size: 15px;
              font-weight: 600;
              color: #1e293b;
            }

            .user-badge {
              background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
              border: none;
              color: white;
              font-size: 11px;
              padding: 2px 8px;
              height: auto;
              font-weight: 500;
            }
          }

          .comment-date {
            font-size: 12px;
            color: #94a3b8;
            font-weight: 500;
          }
        }

        .comment-rating {
          margin-bottom: 12px;

          :deep(.el-rate) {
            .el-rate__icon {
              font-size: 16px;
            }
          }
        }

        .comment-content {
          font-size: 14px;
          color: #475569;
          line-height: 1.7;
          margin-bottom: 12px;
          font-weight: 400;
        }

        .reply-toggle {
          margin-top: 12px;
          padding-top: 12px;
          border-top: 1px dashed rgba(59, 130, 246, 0.15);

          .toggle-button {
            color: #3b82f6;
            font-weight: 500;
            padding: 0;
            font-size: 13px;

            &:hover {
              color: #2563eb;
              background: transparent;
            }

            .toggle-icon {
              margin-right: 4px;
              font-size: 14px;
            }
          }
        }
      }
    }

    .replies-wrapper {
      background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
      border-top: 1px solid rgba(59, 130, 246, 0.1);
      padding: 16px 20px;

      .replies-list {
        display: flex;
        flex-direction: column;
        gap: 12px;
        margin: 0;
        padding: 0;

        .reply-card {
          display: flex;
          gap: 12px;
          padding: 0;
          background: transparent;
          border-radius: 0;
          transition: all 0.3s ease;

          &:hover {
            background: rgba(59, 130, 246, 0.03);
            border-radius: 8px;
            padding: 8px;
            margin: -8px;
          }

          .reply-avatar {
            width: 36px;
            height: 36px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
            background: linear-gradient(135deg, #64748b 0%, #475569 100%);
            color: #ffffff;
            box-shadow: 0 2px 8px rgba(100, 116, 139, 0.3);
          }

          .reply-body {
            flex: 1;
            min-width: 0;

            .reply-header {
              display: flex;
              justify-content: space-between;
              align-items: center;
              margin-bottom: 8px;

              .reply-user-info {
                display: flex;
                align-items: center;
                gap: 8px;

                .reply-username {
                  font-size: 14px;
                  font-weight: 600;
                  color: #334155;
                }

                .merchant-badge {
                  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
                  border: none;
                  color: white;
                  font-size: 11px;
                  padding: 2px 8px;
                  height: auto;
                  font-weight: 500;
                }
              }

              .reply-date {
                font-size: 11px;
                color: #94a3b8;
                font-weight: 500;
              }
            }

            .reply-content {
              font-size: 13px;
              color: #475569;
              line-height: 1.6;
            }
          }

          &.merchant-reply {
            .reply-avatar {
              background: linear-gradient(135deg, #10b981 0%, #059669 100%);
              box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
            }
          }
        }
      }
    }
  }
}
</style>
