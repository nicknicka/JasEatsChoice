<script setup>
import pinia from '../../store'
import { useAuthStore } from '../../store/authStore'
import { inject } from 'vue'

const authStore = useAuthStore(pinia)

// 注入父组件提供的刷新方法
const refreshUnreadCount = inject('refreshUnreadCount', null)

import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'

// 用户ID
const userId = ref(parseInt(String(authStore.userId || 1) || '1', 10))

// ========== 消息中心数据 ==========
const messages = ref([])

// 页面加载时初始化
onMounted(async () => {
  // 加载系统消息
  await loadMessages()

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

// 切换消息分类
const activeTab = ref('all')

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
      // 刷新父组件的未读徽章
      if (refreshUnreadCount) {
        refreshUnreadCount()
      }
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
      // 刷新父组件的未读徽章
      if (refreshUnreadCount) {
        refreshUnreadCount()
      }
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
      // 刷新父组件的未读徽章
      if (refreshUnreadCount) {
        refreshUnreadCount()
      }
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
      // 刷新父组件的未读徽章
      if (refreshUnreadCount) {
        refreshUnreadCount()
      }
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
 * 获取空状态描述
 */
const getEmptyDescription = () => {
  const descriptions = {
    all: '暂无消息',
    order: '暂无订单消息',
    system: '暂无系统通知',
    promotion: '暂无优惠活动'
  }
  return descriptions[activeTab.value] || '暂无消息'
}

/**
 * 获取空状态图标
 */
const getEmptyIcon = () => {
  const icons = {
    all: '📭',
    order: '📦',
    system: '📢',
    promotion: '🎉'
  }
  return icons[activeTab.value] || '📭'
}

/**
 * 获取消息类型图标
 */
const getMessageIcon = (type) => {
  const icons = {
    order: '📦',
    system: '📢',
    promotion: '🎉',
    all: '📭'
  }
  return icons[type] || '📭'
}

/**
 * 从详情页删除消息
 */
const handleDeleteFromDetail = async (messageId) => {
  await deleteMessage(messageId)
  showDetailModal.value = false
}
</script>

<template>
  <div class="message-center-container">
    <h2 class="fade-in-up">消息中心</h2>

    <!-- 消息中心汇总卡片 -->
    <transition name="summary-fade">
      <div v-if="unreadCount > 0" class="message-summary-card">
        <div class="summary-content">
          <div class="summary-icon">🔔</div>
          <div class="summary-text">
            <div class="summary-title">您有 {{ unreadCount }} 条未读通知</div>
          </div>
        </div>
      </div>
    </transition>

    <!-- 消息分类标签页 -->
    <el-tabs v-model="activeTab" class="message-tabs slide-in-left delay-100">
      <el-tab-pane label="全部消息" name="all">
        <template #label>
          <span class="tab-label">
            <span>全部消息</span>
            <el-badge v-if="unreadCount > 0" :value="unreadCount" class="tab-badge" />
          </span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="订单消息" name="order">
        <template #label>
          <span class="tab-label">
            <span>订单消息</span>
          </span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="系统通知" name="system">
        <template #label>
          <span class="tab-label">
            <span>系统通知</span>
          </span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="优惠活动" name="promotion">
        <template #label>
          <span class="tab-label">
            <span>优惠活动</span>
          </span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- 消息列表 - 移除外层transition避免与transition-group冲突 -->
    <div class="message-list-container">
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
        <transition-group name="list" tag="div">
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
            <el-empty :description="getEmptyDescription()">
              <template #image>
                <div class="empty-icon-animated">
                  <div class="empty-icon-circle">{{ getEmptyIcon() }}</div>
                </div>
              </template>
            </el-empty>
          </div>
        </transition>
      </div>
    </div>

    <!-- 消息详情模态框 -->
    <el-dialog
      v-model="showDetailModal"
      width="700px"
      :close-on-click-modal="false"
      class="message-detail-dialog"
    >
      <template #header>
        <div class="detail-modal-header">
          <div class="header-left">
            <div class="detail-icon-wrapper">
              <span class="detail-icon">{{ getMessageIcon(messageDetail?.type) }}</span>
            </div>
            <div class="header-title-section">
              <h3 class="detail-modal-title">{{ messageDetail?.title }}</h3>
              <div class="detail-meta">
                <el-tag
                  :type="
                    messageDetail?.type === 'order'
                      ? 'primary'
                      : messageDetail?.type === 'system'
                        ? 'warning'
                        : 'success'
                  "
                  size="small"
                >
                  {{
                    messageDetail?.type === 'order'
                      ? '订单消息'
                      : messageDetail?.type === 'system'
                        ? '系统通知'
                        : '优惠活动'
                  }}
                </el-tag>
                <span class="detail-time">{{ messageDetail?.time }}</span>
              </div>
            </div>
          </div>
        </div>
      </template>

      <div v-if="messageDetail" class="message-detail-content">
        <div class="detail-body">
          <div class="detail-text">
            {{ messageDetail.content }}
          </div>
        </div>
      </div>

      <template #footer>
        <div class="detail-footer">
          <el-button @click="showDetailModal = false">关闭</el-button>
          <el-button
            v-if="messageDetail"
            type="danger"
            plain
            @click="handleDeleteFromDetail(messageDetail.id)"
          >
            删除此消息
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
// 优化列表项动画性能 - 使用GPU加速
.list-enter-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: opacity, transform;
}

.list-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 1, 1);
  will-change: opacity, transform;
  position: absolute; /* 优化：移除的元素不占用空间 */
  width: 100%;
}

.list-enter-from {
  opacity: 0;
  transform: translateY(10px) scale(0.98);
}

.list-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

.list-move {
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

// 工具栏滑动动画 - 优化
.toolbar-slide-enter-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.toolbar-slide-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 1, 1);
}

.toolbar-slide-enter-from {
  opacity: 0;
  transform: translateY(-8px);
}

.toolbar-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

// 空状态动画 - 优化性能
.empty-fade-enter-active {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.empty-fade-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 1, 1);
}

.empty-fade-enter-from {
  opacity: 0;
  transform: scale(0.9) translateY(15px);
}

.empty-fade-leave-to {
  opacity: 0;
  transform: scale(0.95) translateY(-5px);
}

.message-center-container {
  padding: 0 20px 20px 20px;

  // 全局按钮圆角统一
  :deep(.el-button) {
    border-radius: 10px;
  }

  // Badge 徽章圆角
  :deep(.el-badge__content) {
    border-radius: 12px;
  }

  // Tag 标签圆角
  :deep(.el-tag) {
    border-radius: 8px;
  }

  // Checkbox 复选框圆角
  :deep(.el-checkbox__inner) {
    border-radius: 6px;
  }

  h2 {
    font-size: 24px;
    margin: 0 0 20px 0;
  }

  // 汇总卡片样式
  .message-summary-card {
    margin-bottom: 20px;
    padding: 16px 20px;
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    border-radius: 20px;
    box-shadow: 0 4px 16px rgba(79, 172, 254, 0.35);
    animation: slideInDown 0.5s ease-out;

    .summary-content {
      display: flex;
      align-items: center;
      gap: 16px;

      .summary-icon {
        font-size: 36px;
        animation: ring 2s ease-in-out infinite;
      }

      .summary-text {
        flex: 1;
        color: #ffffff;

        .summary-title {
          font-size: 16px;
          font-weight: 600;
          margin-bottom: 6px;
        }

        .summary-details {
          font-size: 14px;
          opacity: 0.95;
          display: flex;
          align-items: center;
          gap: 8px;

          .divider {
            opacity: 0.7;
          }
        }
      }
    }
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
        border-radius: 16px;

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
              border-radius: 16px;
              overflow: hidden;
              background: #f5f7fa;
              border: 1px solid #e4e7ed;
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
              border-radius: 12px;
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
    // 性能优化：启用GPU加速
    transform: translateZ(0);
    -webkit-transform: translateZ(0);

    .message-toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      border-radius: 16px;
      margin-bottom: 16px;
      box-shadow: 0 4px 12px rgba(79, 172, 254, 0.3);

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
      gap: 20px;

      // 性能优化
      transform: translateZ(0);
      -webkit-transform: translateZ(0);
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

      // 空状态提示
      .empty-tips {
        margin-top: 16px;
        padding: 12px 20px;
        background: linear-gradient(135deg, #fff5f5 0%, #ffe4e4 100%);
        border-radius: 12px;
        animation: emptyFadeIn 0.8s ease-out 0.5s both;

        p {
          margin: 0;
          font-size: 14px;
          color: #666;
          text-align: center;
        }
      }
    }
  }

  .message-card {
    cursor: pointer;
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
    border-radius: 16px;
    will-change: transform; // 性能优化
    margin-bottom: 17px;

    &:last-child {
      margin-bottom: 0;
    }

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      transform: translateY(-2px);
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
  :deep(.message-detail-dialog) {
    border-radius: 16px;

    .el-dialog__header {
      padding: 0;
      margin: 0;
    }

    .el-dialog__body {
      padding: 0 20px 20px 20px;
    }

    .el-dialog__footer {
      padding: 16px 20px;
      border-top: 1px solid #ebeef5;
      border-radius: 0 0 16px 16px;
    }
  }

  .detail-modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20px;
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    border-radius: 16px 16px 0 0;
    margin: -20px -20px 0 -20px;

    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;
      flex: 1;

      .detail-icon-wrapper {
        width: 56px;
        height: 56px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: rgba(255, 255, 255, 0.25);
        border-radius: 16px;
        backdrop-filter: blur(10px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

        .detail-icon {
          font-size: 32px;
          filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
        }
      }

      .header-title-section {
        flex: 1;
        color: #ffffff;

        .detail-modal-title {
          margin: 0 0 8px 0;
          font-size: 20px;
          font-weight: 600;
          color: #ffffff;
          line-height: 1.3;
        }

        .detail-meta {
          display: flex;
          align-items: center;
          gap: 12px;

          .detail-time {
            font-size: 13px;
            opacity: 0.95;
            color: #ffffff;
          }
        }
      }
    }
  }

  .message-detail-content {
    .detail-body {
      padding: 24px 0;

      .detail-text {
        font-size: 15px;
        line-height: 1.8;
        color: #303133;
        text-align: justify;
        word-wrap: break-word;
        white-space: pre-wrap;
      }
    }
  }

  .detail-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
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

@keyframes ring {
  0%, 100% {
    transform: rotate(0deg);
  }
  10%, 30% {
    transform: rotate(-10deg);
  }
  20%, 40% {
    transform: rotate(10deg);
  }
  50% {
    transform: rotate(0deg);
  }
}

@keyframes slideInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.summary-fade-enter-active {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.summary-fade-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 1, 1);
}

.summary-fade-enter-from {
  opacity: 0;
  transform: translateY(-20px);
}

.summary-fade-leave-to {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}
</style>
