<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading, Select, Close } from '@element-plus/icons-vue'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'
import { useFriendManagement } from '../../composables/useFriendManagement.js'

// 用户ID
const userId = ref(parseInt(localStorage.getItem('userId') || '1', 10))

// ========== 好友管理 ==========
const {
  getFriendRequests,
  acceptFriendRequest,
  rejectFriendRequest
} = useFriendManagement({ userId, conversations: ref([]), chatHistory: ref({}) })

// 好友请求列表
const friendRequests = ref([])
const loadingRequests = ref(false)

// ========== 消息中心数据 ==========
const messages = ref([])

// 页面加载时初始化
onMounted(async () => {
  // 加载系统消息
  await loadMessages()

  // 加载好友请求
  await loadFriendRequests()
})

/**
 * 加载系统消息
 */
const loadMessages = async () => {
  try {
    const response = await api.get(API_CONFIG.message.list, {
      params: { userId: userId.value }
    })

    if (response && response.code === '200') {
      // 转换后端返回的数据格式以匹配前端期望的字段
      const formattedMessages = response.data.map((message) => ({
        id: message.id,
        title: message.content,
        content: message.content,
        time: message.sendTime,
        read: message.readStatus,
        type: message.type || 'system'
      }))

      messages.value = formattedMessages
    }
  } catch (error) {
    console.error('加载消息失败:', error)
  }
}

/**
 * 加载好友请求列表
 */
const loadFriendRequests = async () => {
  loadingRequests.value = true
  try {
    const requests = await getFriendRequests()
    friendRequests.value = requests
  } catch (error) {
    console.error('加载好友请求失败:', error)
  } finally {
    loadingRequests.value = false
  }
}

// 切换消息分类
const activeTab = ref('all')

// 筛选消息
const filteredMessages = computed(() => {
  if (activeTab.value === 'all') {
    return messages.value
  }
  return messages.value.filter((msg) => msg.type === activeTab.value)
})

// 好友请求数量
const friendRequestCount = computed(() => friendRequests.value.length)

// 消息详情模态框
const messageDetail = ref(null)
const showDetailModal = ref(false)

// 查看消息详情
const viewMessage = (message) => {
  message.read = true
  messageDetail.value = message
  showDetailModal.value = true
}

// 接受好友请求
const handleAcceptRequest = async (request) => {
  const success = await acceptFriendRequest(request.userId)
  if (success) {
    // 从列表中移除已处理的请求
    const index = friendRequests.value.findIndex((r) => r.id === request.id)
    if (index !== -1) {
      friendRequests.value.splice(index, 1)
    }
  }
}

// 拒绝好友请求
const handleRejectRequest = async (request) => {
  const success = await rejectFriendRequest(request.userId)
  if (success) {
    // 从列表中移除已处理的请求
    const index = friendRequests.value.findIndex((r) => r.id === request.id)
    if (index !== -1) {
      friendRequests.value.splice(index, 1)
    }
  }
}

// 批量删除消息
const deleteSelected = () => {
  console.log('批量删除消息')
}

// 全部标记为已读
const markAllAsRead = () => {
  messages.value.forEach((message) => {
    message.read = true
  })
  console.log('已将所有消息标记为已读')
}

/**
 * 判断头像是否为图片URL
 */
const isImageAvatar = (avatar) => {
  if (!avatar) return false
  return avatar.match(/^https?:/) || avatar.match(/^data:image/)
}
</script>

<template>
  <div class="message-center-container">
    <h2>消息中心</h2>

    <!-- 消息分类标签页 -->
    <el-tabs v-model:active-name="activeTab" class="message-tabs" @tab-change="activeTab === 'friend' && loadFriendRequests()">
      <el-tab-pane label="全部消息" name="all"></el-tab-pane>
      <el-tab-pane label="订单消息" name="order"></el-tab-pane>
      <el-tab-pane label="系统通知" name="system"></el-tab-pane>
      <el-tab-pane label="优惠活动" name="promotion"></el-tab-pane>
      <el-tab-pane name="friend">
        <template #label>
          <span class="friend-tab">
            <span>好友请求</span>
            <el-badge v-if="friendRequestCount > 0" :value="friendRequestCount" class="friend-badge" />
          </span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- 好友请求列表 -->
    <div v-if="activeTab === 'friend'" class="friend-requests">
      <div v-if="loadingRequests" class="loading-container">
        <el-icon class="is-loading" :size="30"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <div v-else-if="friendRequests.length === 0" class="empty-requests">
        <el-empty description="暂无好友请求">
          <template #image>
            <div class="empty-icon">👋</div>
          </template>
        </el-empty>
      </div>

      <div v-else class="request-list">
        <el-card
          v-for="request in friendRequests"
          :key="request.id"
          class="request-card"
          shadow="hover"
        >
          <div class="request-content">
            <div class="requester-info">
              <div class="requester-avatar">
                <img v-if="isImageAvatar(request.requesterInfo?.avatar)" :src="request.requesterInfo.avatar" alt="头像" />
                <span v-else class="avatar-emoji">{{ request.requesterInfo?.avatar || '👤' }}</span>
              </div>

              <div class="requester-details">
                <h4 class="requester-name">{{ request.requesterInfo?.nickname || '未知用户' }}</h4>
                <p class="requester-id">用户ID: {{ request.requesterInfo?.id }}</p>
                <p v-if="request.createTime" class="request-time">{{ request.createTime }}</p>
              </div>
            </div>

            <div class="request-actions">
              <el-button type="primary" size="default" @click="handleAcceptRequest(request)">
                <el-icon><Select /></el-icon>
                接受
              </el-button>
              <el-button size="default" @click="handleRejectRequest(request)">
                <el-icon><Close /></el-icon>
                拒绝
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 消息列表 -->
    <div v-else class="message-list">
      <el-card
        v-for="message in filteredMessages"
        :key="message.id"
        class="message-card"
        :class="{ unread: !message.read }"
      >
        <div class="message-header">
          <div class="message-type">
            <el-tag
              :type="
                message.type === 'order'
                  ? 'primary'
                  : message.type === 'system'
                    ? 'warning'
                    : 'success'
              "
              size="small"
            >
              {{
                message.type === 'order'
                  ? '订单消息'
                  : message.type === 'system'
                    ? '系统通知'
                    : '优惠活动'
              }}
            </el-tag>
          </div>
          <div class="message-time">{{ message.time }}</div>
        </div>

        <div class="message-content">
          <h3 class="message-title">{{ message.title }}</h3>
          <p class="message-text">{{ message.content }}</p>
        </div>

        <div class="message-actions">
          <el-button type="text" size="small" @click="viewMessage(message)"> 查看详情 </el-button>
          <el-button type="text" size="small" danger> 删除 </el-button>
        </div>
      </el-card>

      <!-- 空数据提示 -->
      <el-empty v-if="filteredMessages.length === 0" description="暂无消息"></el-empty>
    </div>

    <!-- 消息详情模态框 -->
    <el-dialog
      v-model="showDetailModal"
      :title="messageDetail ? messageDetail.title : ''"
      width="600px"
      top="20%"
    >
      <div v-if="messageDetail" class="message-detail-content">
        <div class="detail-header">
          <el-tag
            :type="
              messageDetail.type === 'order'
                ? 'primary'
                : messageDetail.type === 'system'
                  ? 'warning'
                  : 'success'
            "
          >
            {{
              messageDetail.type === 'order'
                ? '订单消息'
                : messageDetail.type === 'system'
                  ? '系统通知'
                  : '优惠活动'
            }}
          </el-tag>
          <span class="detail-time">{{ messageDetail.time }}</span>
        </div>
        <div class="detail-content">
          {{ messageDetail.content }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.message-center-container {
  padding: 0 20px 20px 20px;

  h2 {
    font-size: 24px;
    margin: 0 0 20px 0;
  }

  .message-tabs {
    margin-bottom: 20px;

    .friend-tab {
      display: flex;
      align-items: center;
      gap: 8px;

      .friend-badge {
        :deep(.el-badge__content) {
          background-color: #f56c6c;
          border-color: #f56c6c;
        }
      }
    }
  }

  // 好友请求列表样式
  .friend-requests {
    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 60px 20px;
      color: #909399;

      p {
        margin-top: 16px;
        font-size: 14px;
      }
    }

    .empty-requests {
      .empty-icon {
        font-size: 80px;
        margin-bottom: 20px;
        opacity: 0.8;
      }
    }

    .request-list {
      display: flex;
      flex-direction: column;
      gap: 16px;

      .request-card {
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
        }

        .request-content {
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 20px;

          .requester-info {
            display: flex;
            align-items: center;
            gap: 16px;
            flex: 1;

            .requester-avatar {
              width: 64px;
              height: 64px;
              border-radius: 50%;
              overflow: hidden;
              background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
              display: flex;
              align-items: center;
              justify-content: center;
              flex-shrink: 0;

              img {
                width: 100%;
                height: 100%;
                object-fit: cover;
              }

              .avatar-emoji {
                font-size: 36px;
              }
            }

            .requester-details {
              flex: 1;

              .requester-name {
                font-size: 16px;
                font-weight: 600;
                color: #303133;
                margin: 0 0 6px 0;
              }

              .requester-id {
                font-size: 13px;
                color: #909399;
                margin: 0 0 4px 0;
              }

              .request-time {
                font-size: 12px;
                color: #c0c4cc;
                margin: 0;
              }
            }
          }

          .request-actions {
            display: flex;
            gap: 12px;
            flex-shrink: 0;

            .el-button {
              border-radius: 8px;
              padding: 10px 20px;
              font-weight: 500;
              transition: all 0.3s ease;

              &:hover {
                transform: translateY(-1px);
              }
            }
          }
        }
      }
    }
  }

  // 消息列表样式
  .message-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
  }

  .message-card {
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    &.unread {
      border-left: 4px solid #409eff;
    }

    .message-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;

      .message-time {
        font-size: 14px;
        color: #909399;
      }
    }

    .message-content {
      margin-bottom: 15px;

      .message-title {
        font-size: 16px;
        font-weight: bold;
        margin-bottom: 5px;
        margin: 0;
      }

      .message-text {
        color: #606266;
        font-size: 14px;
        margin: 0;
      }
    }

    .message-actions {
      text-align: right;
    }
  }

  /* 消息详情模态框样式 */
  .message-detail-content {
    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      .detail-time {
        font-size: 14px;
        color: #909399;
      }
    }

    .detail-content {
      font-size: 16px;
      color: #303133;
      line-height: 1.6;
    }
  }
}
</style>
