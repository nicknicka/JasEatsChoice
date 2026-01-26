<script setup>
import pinia from '../../store'
import { useAuthStore } from '../../store/authStore'

const authStore = useAuthStore(pinia)

import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Select, Close, Delete } from '@element-plus/icons-vue'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'
import { useFriendManagement } from '../../composables/useFriendManagement.js'

// 用户ID
const userId = ref(parseInt(String(authStore.userId || 1) || '1', 10))

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

  // 加载未读消息数量
  await loadUnreadCount()
})

/**
 * 加载系统消息
 */
const loadMessages = async () => {
  try {
    console.log('🚀 开始加载系统消息...')
    // 修改API调用路径：使用路径参数而不是查询参数
    const response = await api.get(`${API_CONFIG.message.list}/${userId.value}`)

    // 修复：兼容字符串和数字类型的code
    if (response && (response.code === '200' || response.code === 200)) {
      console.log('✅ 消息API返回成功，数据量:', response.data.length)

      // 转换后端返回的数据格式以匹配前端期望的字段
      const formattedMessages = response.data.map((message) => {
        return {
          id: message.id,
          title: message.title || message.content,
          content: message.content,
          time: message.sendTime || message.createTime,
          read: message.readStatus,
          type: message.type || 'system'
        }
      })

      messages.value = formattedMessages
      console.log('✅ 消息已设置到messages.value，数量:', messages.value.length)
      console.log('📋 当前activeTab:', activeTab.value)
    } else {
      console.warn('❌ 消息API返回失败:', response)
    }
  } catch (error) {
    console.error('❌ 加载消息失败:', error)
  }
}

/**
 * 加载好友请求列表
 */
const loadFriendRequests = async () => {
  loadingRequests.value = true
  try {
    console.log('MessageCenter: 开始加载好友请求')
    const requests = await getFriendRequests()
    console.log('MessageCenter: 获取到的好友请求:', requests)
    friendRequests.value = requests
    console.log('MessageCenter: 设置后的 friendRequests.value:', friendRequests.value)
  } catch (error) {
    console.error('加载好友请求失败:', error)
  } finally {
    loadingRequests.value = false
  }
}

// 切换消息分类
const activeTab = ref('all')

// 监听tab切换，切换到好友请求时重新加载数据
watch(activeTab, async (newTab) => {
  console.log('Tab切换到:', newTab)
  if (newTab === 'friend') {
    console.log('切换到好友请求tab，重新加载数据')
    await loadFriendRequests()
  }
})

// 筛选消息
const filteredMessages = computed(() => {
  console.log('🔄 filteredMessages computed被调用，activeTab=', activeTab.value, '消息总数=', messages.value.length)

  let result
  if (activeTab.value === 'all') {
    result = messages.value
  } else {
    result = messages.value.filter((msg) => {
      const match = msg.type === activeTab.value
      console.log(`  检查消息: type=${msg.type}, tab=${activeTab.value}, 匹配=${match}`)
      return match
    })
  }

  console.log('  ✅ 筛选完成，结果数量:', result.length)
  return result
})

// 好友请求数量
const friendRequestCount = computed(() => friendRequests.value.length)

// 消息详情模态框
const messageDetail = ref(null)
const showDetailModal = ref(false)

// 查看消息详情
const viewMessage = async (message) => {
  // 调用后端接口标记为已读
  if (!message.read) {
    await markMessageAsRead(message.id)
  }
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

// ========== 消息操作功能 ==========

// 单条消息删除
const deleteMessage = async (messageId) => {
  try {
    await ElMessageBox.confirm('确认删除这条消息吗？', '删除消息', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 调用后端接口删除消息
    const response = await api.delete(`/notifications/${messageId}`)
    if (response.code === '200') {
      ElMessage.success('消息删除成功')
      // 从前端列表中移除
      const index = messages.value.findIndex((msg) => msg.id === messageId)
      if (index !== -1) {
        messages.value.splice(index, 1)
      }
      // 重新加载未读数量
      await loadUnreadCount()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除消息失败:', error)
      ElMessage.error('删除消息失败')
    }
  }
}

// 批量删除相关
const selectedMessages = ref([])
const selectAll = ref(false)

// 判断消息是否被选中
const isMessageSelected = (messageId) => {
  return selectedMessages.value.some((msg) => msg.id === messageId)
}

// 处理单条消息选择
const handleMessageSelect = (message, checked) => {
  if (checked) {
    if (!isMessageSelected(message.id)) {
      selectedMessages.value.push(message)
    }
  } else {
    selectedMessages.value = selectedMessages.value.filter((msg) => msg.id !== message.id)
  }
  // 更新全选状态
  selectAll.value = selectedMessages.value.length === filteredMessages.value.length && filteredMessages.value.length > 0
}

// 处理全选/取消全选
const handleSelectAll = (checked) => {
  if (checked) {
    selectedMessages.value = [...filteredMessages.value]
  } else {
    selectedMessages.value = []
  }
}

const deleteSelected = async () => {
  if (selectedMessages.value.length === 0) {
    ElMessage.warning('请先选择要删除的消息')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${selectedMessages.value.length} 条消息吗？`,
      '批量删除消息',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 调用后端接口批量删除
    const messageIds = selectedMessages.value.map((msg) => msg.id)
    const response = await api.delete('/notifications/batch', { data: messageIds })
    if (response.code === '200') {
      ElMessage.success(`成功删除 ${messageIds.length} 条消息`)
      // 从前端列表中移除
      const idsToDelete = selectedMessages.value.map((msg) => msg.id)
      messages.value = messages.value.filter((msg) => !idsToDelete.includes(msg.id))
      selectedMessages.value = []
      selectAll.value = false
      // 重新加载未读数量
      await loadUnreadCount()
    } else {
      ElMessage.error(response.message || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 全部标记为已读
const markAllAsRead = async () => {
  try {
    const response = await api.put('/notifications/all-read', null, {
      params: { userId: userId.value }
    })

    if (response.code === '200') {
      // 更新前端状态
      messages.value.forEach((message) => {
        message.read = true
      })
      // 重新加载未读数量
      await loadUnreadCount()
      ElMessage.success('已将所有消息标记为已读')
    } else {
      ElMessage.error(response.message || '标记失败')
    }
  } catch (error) {
    console.error('标记全部已读失败:', error)
    ElMessage.error('标记全部已读失败')
  }
}

// 标记单条消息为已读
const markMessageAsRead = async (messageId) => {
  try {
    const response = await api.put(`/notifications/${messageId}/read`)

    if (response.code === '200') {
      // 更新前端状态
      const message = messages.value.find((msg) => msg.id === messageId)
      if (message) {
        message.read = true
      }
      // 重新加载未读数量
      await loadUnreadCount()
    }
  } catch (error) {
    console.error('标记消息已读失败:', error)
  }
}

// 未读消息数量
const unreadCount = ref(0)

const loadUnreadCount = async () => {
  try {
    const response = await api.get('/notifications/unread-count', {
      params: { userId: userId.value }
    })

    if (response.code === '200') {
      unreadCount.value = response.data || 0
    }
  } catch (error) {
    console.error('获取未读消息数量失败:', error)
  }
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
    <el-tabs v-model="activeTab" class="message-tabs">
      <el-tab-pane label="全部消息" name="all">
        <template #label>
          <span class="tab-label">
            <span>全部消息</span>
            <el-badge v-if="unreadCount > 0" :value="unreadCount" class="tab-badge" />
          </span>
        </template>
      </el-tab-pane>
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
    <transition name="tab-fade-slide" mode="out-in">
      <div v-if="activeTab === 'friend'" key="friend" class="friend-requests">
      <div v-if="loadingRequests" class="loading-container">
        <el-icon class="is-loading" :size="30"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <div v-else-if="friendRequests.length === 0" class="empty-requests">
        <transition name="empty-fade" mode="out-in" appear>
          <el-empty description="暂无好友请求" key="empty-friend">
            <template #image>
              <div class="empty-icon-animated">
                <div class="empty-icon-circle friend-empty">👋</div>
              </div>
            </template>
          </el-empty>
        </transition>
      </div>

      <div v-else class="request-list">
        <transition-group name="list">
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
        </transition-group>
      </div>
    </div>
    </transition>

    <!-- 消息列表 -->
    <transition name="tab-fade-slide" mode="out-in">
      <div v-if="activeTab !== 'friend'" key="messages" class="message-list-container">
      <!-- 操作工具栏 - 只在有选中消息或全部标记已读可用时显示 -->
      <transition name="toolbar-slide">
        <div v-if="selectedMessages.length > 0 || unreadCount > 0" class="message-toolbar">
          <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
          <div class="toolbar-actions">
            <el-button
              type="danger"
              :disabled="selectedMessages.length === 0"
              @click="deleteSelected"
              :icon="Delete"
            >
              批量删除 ({{ selectedMessages.length }})
            </el-button>
            <el-button type="primary" @click="markAllAsRead" :disabled="unreadCount === 0">
              全部标记为已读
            </el-button>
          </div>
        </div>
      </transition>

      <!-- 消息列表 -->
      <div class="message-list">
        <transition-group name="list">
          <el-card
            v-for="message in filteredMessages"
            :key="message.id"
            class="message-card"
            :class="{ unread: !message.read, selected: isMessageSelected(message.id) }"
          >
          <div class="message-card-content">
            <el-checkbox
              :model-value="isMessageSelected(message.id)"
              @change="handleMessageSelect(message, $event)"
              class="message-checkbox"
            ></el-checkbox>

            <div class="message-body" @click="viewMessage(message)">
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
            </div>

            <div class="message-actions">
              <el-button type="text" size="small" @click.stop="viewMessage(message)"> 查看详情 </el-button>
              <el-button type="text" size="small" danger @click.stop="deleteMessage(message.id)">
                删除
              </el-button>
            </div>
          </div>
        </el-card>
        </transition-group>

        <!-- 空数据提示 -->
        <transition name="empty-fade" mode="out-in">
          <div v-if="filteredMessages.length === 0" class="empty-state-wrapper" key="empty">
            <el-empty description="暂无消息">
              <template #image>
                <div class="empty-icon-animated">
                  <div class="empty-icon-circle">📭</div>
                </div>
              </template>
            </el-empty>
          </div>
        </transition>
      </div>
    </div>
    </transition>

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
// Tabs切换动画
.tab-fade-slide-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.tab-fade-slide-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 1, 1);
}

.tab-fade-slide-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.98);
}

.tab-fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-15px) scale(0.98);
}

// 列表项动画
.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}

.list-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

// 工具栏滑动动画
.toolbar-slide-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.toolbar-slide-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 1, 1);
}

.toolbar-slide-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.toolbar-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

// 空状态动画
.empty-fade-enter-active {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.empty-fade-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 1, 1);
}

.empty-fade-enter-from {
  opacity: 0;
  transform: scale(0.8) translateY(20px);
}

.empty-fade-leave-to {
  opacity: 0;
  transform: scale(0.9) translateY(-10px);
}

.message-center-container {
  padding: 0 20px 20px 20px;

  h2 {
    font-size: 24px;
    margin: 0 0 20px 0;
  }

  .message-tabs {
    margin-bottom: 20px;

    .tab-label {
      display: flex;
      align-items: center;
      gap: 8px;

      .tab-badge {
        :deep(.el-badge__content) {
          background-color: #f56c6c;
          border-color: #f56c6c;
        }
      }
    }

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
      padding: 60px 20px;

      .empty-icon-animated {
        .empty-icon-circle {
          width: 120px;
          height: 120px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 64px;
          margin: 0 auto 20px;
          background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
          border-radius: 50%;
          animation: emptyFloat 3s ease-in-out infinite;
          box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);

          &.friend-empty {
            background: linear-gradient(135deg, #fff5f5 0%, #ffe4e4 100%);
            animation: waveHand 2s ease-in-out infinite;
          }
        }
      }

      :deep(.el-empty__description) {
        animation: emptyFadeIn 0.8s ease-out 0.3s both;
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
  .message-list-container {
    .message-toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 8px;
      margin-bottom: 16px;
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

      // 使复选框文字在深色背景上更清晰
      :deep(.el-checkbox__label) {
        color: #ffffff;
        font-weight: 500;
      }

      :deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
        color: #ffffff;
      }

      :deep(.el-checkbox__inner) {
        background-color: rgba(255, 255, 255, 0.2);
        border-color: rgba(255, 255, 255, 0.4);
      }

      :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
        background-color: #ffffff;
        border-color: #ffffff;
      }

      :deep(.el-checkbox__input.is-checked .el-checkbox__inner::after) {
        border-color: #667eea;
      }

      .toolbar-actions {
        display: flex;
        gap: 12px;
      }
    }

    .message-list {
      display: flex;
      flex-direction: column;
      gap: 15px;
    }

    // 空状态包装器
    .empty-state-wrapper {
      padding: 40px 20px;

      .empty-icon-animated {
        .empty-icon-circle {
          width: 120px;
          height: 120px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 64px;
          margin: 0 auto 20px;
          background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
          border-radius: 50%;
          animation: emptyFloat 3s ease-in-out infinite;
          box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
        }
      }

      // 为空状态描述添加动画延迟
      :deep(.el-empty__description) {
        animation: emptyFadeIn 0.8s ease-out 0.3s both;
      }
    }
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

    &.selected {
      background-color: #ecf5ff;
    }

    .message-card-content {
      display: flex;
      align-items: flex-start;
      gap: 12px;

      .message-checkbox {
        margin-top: 20px;
        flex-shrink: 0;
      }

      .message-body {
        flex: 1;
        cursor: pointer;
      }

      .message-actions {
        display: flex;
        flex-direction: column;
        gap: 8px;
        flex-shrink: 0;
      }
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

// 关键帧动画定义
@keyframes emptyFloat {
  0%,
  100% {
    transform: translateY(0px) scale(1);
  }
  50% {
    transform: translateY(-10px) scale(1.02);
  }
}

@keyframes waveHand {
  0%,
  100% {
    transform: rotate(0deg) scale(1);
  }
  25% {
    transform: rotate(-10deg) scale(1.05);
  }
  75% {
    transform: rotate(10deg) scale(1.05);
  }
}

@keyframes emptyFadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
