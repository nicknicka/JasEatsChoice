<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Bell,
  ChatDotRound,
  Delete,
  Message,
  Notification,
  Promotion,
  ShoppingCart
} from '@element-plus/icons-vue'
import pinia from '../../store'
import { useAuthStore } from '../../store/authStore'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'
import AppEmptyState from '../../components/common/AppEmptyState.vue'
import AppPageHeader from '../../components/common/AppPageHeader.vue'

const authStore = useAuthStore(pinia)

// 注入父组件提供的刷新方法
const refreshUnreadCount = inject('refreshUnreadCount', null)

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
          time: message.sendTime || message.createdAt,
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
 * 消息类型 → 标签 / 图标 / 颜色。
 * 后端 usr_notification.type 存的是 NotificationTypeEnum 的 code（例如
 * order_payment_success / review_submitted / system），不是 order/system 这类短值，
 * 这里按前缀归到「订单 / 优惠 / 互动 / 系统」四类，标签与图标随之区分。
 * 颜色只取 tokens.css 的 CSS 变量（DESIGN-BASELINE §3.1、§7.1）。
 */
const MESSAGE_TYPES = {
  order: { key: 'order', label: '订单消息', tagType: 'info', tagClass: '', icon: ShoppingCart },
  promotion: { key: 'promotion', label: '优惠活动', tagType: 'success', tagClass: '', icon: Promotion },
  review: { key: 'review', label: '互动消息', tagType: 'warning', tagClass: '', icon: ChatDotRound },
  system: { key: 'system', label: '系统通知', tagType: 'info', tagClass: 'is-system', icon: Notification }
}

const resolveMessageType = (type) => {
  const code = String(type || '').toLowerCase()
  if (code.startsWith('order') || code.startsWith('group_order')) return MESSAGE_TYPES.order
  if (code.startsWith('review')) return MESSAGE_TYPES.review
  if (code.startsWith('promotion') || code.startsWith('discount') || code.startsWith('coupon')) {
    return MESSAGE_TYPES.promotion
  }
  return MESSAGE_TYPES.system
}

// 本地时间格式化：YYYY-MM-DD HH:mm（DESIGN-BASELINE §8 检查清单：时间不带 ISO 的 T）
const formatTime = (value) => {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

// 消息分类标签页（顺序对齐评审 P0：订单 / 优惠 / 互动 / 系统）
const tabs = [
  { name: 'all', label: '全部消息' },
  { name: 'order', label: '订单消息' },
  { name: 'promotion', label: '优惠活动' },
  { name: 'review', label: '互动消息' },
  { name: 'system', label: '系统通知' }
]

// 切换消息分类
const activeTab = ref('all')

// 筛选消息
const filteredMessages = computed(() => {
  console.log('🔄 filteredMessages computed被调用，activeTab=', activeTab.value, '消息总数=', messages.value.length)

  if (activeTab.value === 'all') {
    return messages.value
  }

  return messages.value.filter((msg) => resolveMessageType(msg.type).key === activeTab.value)
})

// 消息详情模态框
const messageDetail = ref(null)
const showDetailModal = ref(false)

// 详情弹窗的类型标签与图标
const messageDetailType = computed(() =>
  messageDetail.value ? resolveMessageType(messageDetail.value.type) : MESSAGE_TYPES.system
)

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
    review: '暂无互动消息',
    system: '暂无系统通知',
    promotion: '暂无优惠活动'
  }
  return descriptions[activeTab.value] || '暂无消息'
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
    <!-- 页头：标准版标题容器（§4.7） -->
    <AppPageHeader class="page-header" title="消息中心" />

    <!-- 未读摘要：中性浅底一条，替代原来的橙色渐变卡（§3.4、§4.8） -->
    <div v-if="unreadCount > 0" class="unread-summary">
      <el-icon class="unread-summary__icon" aria-hidden="true"><Bell /></el-icon>
      <span class="unread-summary__text">
        您有 <strong class="unread-summary__count">{{ unreadCount }}</strong> 条未读通知
      </span>
      <el-button class="unread-summary__action" type="primary" link @click="markAllAsRead">
        全部标记为已读
      </el-button>
    </div>

    <!-- 消息分类标签页：独立的浅表面块，与下方列表拉出层级（P1） -->
    <el-tabs v-model="activeTab" class="message-tabs">
      <el-tab-pane v-for="tab in tabs" :key="tab.name" :name="tab.name">
        <template #label>
          <span class="tab-label">
            <span>{{ tab.label }}</span>
            <el-badge
              v-if="tab.name === 'all' && unreadCount > 0"
              :value="unreadCount"
              :max="99"
              class="tab-badge"
            />
          </span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <div class="message-list-container">
      <!-- 操作工具栏：有选中项或未读消息时出现 -->
      <transition name="toolbar-slide">
        <div v-if="selectedMessages.length > 0 || unreadCount > 0" class="message-toolbar">
          <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
          <div class="toolbar-actions">
            <el-button
              type="danger"
              plain
              :disabled="selectedMessages.length === 0"
              :icon="Delete"
              @click="deleteSelected"
            >
              批量删除 ({{ selectedMessages.length }})
            </el-button>
          </div>
        </div>
      </transition>

      <!-- 消息列表 -->
      <div class="message-list">
        <transition-group v-if="filteredMessages.length > 0" name="list" tag="div" class="message-list-items">
          <el-card
            v-for="message in filteredMessages"
            :key="message.id"
            class="message-card"
            :class="{ unread: !message.read, selected: isMessageSelected(message.id) }"
          >
            <div class="message-card-content">
              <el-checkbox
                :model-value="isMessageSelected(message.id)"
                class="message-checkbox"
                @change="handleMessageSelect(message, $event)"
              ></el-checkbox>

              <div class="message-body" @click="viewMessage(message)">
                <div class="message-header">
                  <el-tag
                    :type="resolveMessageType(message.type).tagType"
                    size="small"
                    effect="plain"
                    round
                    class="message-type-tag"
                    :class="resolveMessageType(message.type).tagClass"
                  >
                    <el-icon class="message-type-tag__icon" aria-hidden="true">
                      <component :is="resolveMessageType(message.type).icon" />
                    </el-icon>
                    {{ resolveMessageType(message.type).label }}
                  </el-tag>
                  <span class="message-time">{{ formatTime(message.time) }}</span>
                </div>

                <div class="message-content">
                  <h3 class="message-title">
                    <span v-if="!message.read" class="message-unread-dot" aria-hidden="true"></span>
                    {{ message.title }}
                  </h3>
                  <p class="message-text">{{ message.content }}</p>
                </div>
              </div>

              <div class="message-actions">
                <el-button text size="small" @click.stop="viewMessage(message)">查看详情</el-button>
                <el-button text size="small" type="danger" @click.stop="deleteMessage(message.id)">
                  删除
                </el-button>
              </div>
            </div>
          </el-card>
        </transition-group>

        <!-- 空状态：线性图标 + 中文说明 + 替代动作（§4.5） -->
        <AppEmptyState
          v-else
          class="message-empty"
          :icon="Message"
          :description="getEmptyDescription()"
          action-text="刷新"
          @action="loadMessages"
        />
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
          <div class="detail-icon-wrapper">
            <el-icon aria-hidden="true"><component :is="messageDetailType.icon" /></el-icon>
          </div>
          <div class="header-title-section">
            <h3 class="detail-modal-title">{{ messageDetail?.title }}</h3>
            <div class="detail-meta">
              <el-tag
                :type="messageDetailType.tagType"
                size="small"
                effect="plain"
                round
                class="message-type-tag"
                :class="messageDetailType.tagClass"
              >
                {{ messageDetailType.label }}
              </el-tag>
              <span class="detail-time">{{ formatTime(messageDetail?.time) }}</span>
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

<style scoped>
/* 页面颜色、间距、圆角、字阶与动效均取自 tokens.css（§3、§7.1），无裸色值。 */
.message-center-container {
  box-sizing: border-box;
  width: 100%;
  max-width: var(--page-max);
  min-height: 100%;
  margin: 0 auto;
  padding: 0 var(--page-gutter) var(--space-10);
  background: var(--canvas);

  /* 圆角统一走 token（§3.4） */
  :deep(.el-button) {
    border-radius: var(--radius-md);
  }

  :deep(.el-tag) {
    border-radius: var(--radius-sm);
  }

  :deep(.el-badge__content) {
    border-radius: var(--radius-full);
  }

  :deep(.el-checkbox__inner) {
    border-radius: var(--radius-sm);
  }
}

.page-header {
  margin-bottom: var(--space-6);
}

/* 未读摘要：浅底 + 线性图标 + 数值（§4.8） */
.unread-summary {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  margin-bottom: var(--space-4);
  background: var(--canvas-sunken);
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  color: var(--ink-700);
  font-size: var(--text-body);
}

.unread-summary__icon {
  color: var(--brand-500);
  font-size: 18px;
}

.unread-summary__count {
  font-family: var(--font-num);
  font-variant-numeric: tabular-nums;
  color: var(--ink-900);
}

.unread-summary__action {
  margin-left: auto;
}

/* tab 区做成独立浅表面块，列表区保持在页面底色上，拉出层级差（P1） */
.message-tabs {
  padding: 0 var(--space-5);
  margin-bottom: var(--card-gap);
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
}

.message-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.message-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: var(--line);
}

.tab-label {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-body);
}

/* 工具栏：中性浅底，不再用橙色渐变条（§4.6） */
.message-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-3) var(--space-4);
  margin-bottom: var(--card-gap);
  background: var(--canvas-sunken);
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
}

.message-list-items {
  display: flex;
  flex-direction: column;
  gap: var(--card-gap);
}

/* 消息卡：L2 浮起质感，hover 升一档（§3.4） */
.message-card {
  margin: 0;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition:
    box-shadow var(--duration-fast) var(--ease-standard),
    transform var(--duration-fast) var(--ease-standard);
}

.message-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.message-card.unread {
  border-left: 3px solid var(--brand-500);
}

.message-card.selected {
  background: var(--brand-50);
}

.message-card :deep(.el-card__body) {
  padding: var(--card-padding);
}

.message-card-content {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
}

.message-checkbox {
  margin-top: var(--space-5);
  flex-shrink: 0;
}

.message-body {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  margin-bottom: var(--space-2);
}

.message-type-tag {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  flex-shrink: 0;
}

.message-type-tag__icon {
  font-size: 12px;
}

/* 系统通知走中性标签，避免品牌橙被当成分类色（§3.1） */
.message-type-tag.is-system {
  --el-tag-text-color: var(--neutral);
  --el-tag-bg-color: var(--neutral-bg);
  --el-tag-border-color: var(--line-strong);
}

/* 时间用数字字体 + 等宽数字（§3.2、§8） */
.message-time {
  font-family: var(--font-num);
  font-variant-numeric: tabular-nums;
  font-size: var(--text-caption);
  color: var(--ink-400);
  white-space: nowrap;
}

.message-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0 0 var(--space-1);
  font-size: var(--text-h3);
  line-height: 24px;
  font-weight: var(--weight-semibold);
  color: var(--ink-900);
}

/* 未读标记：品牌色圆点（P1） */
.message-unread-dot {
  width: 8px;
  height: 8px;
  flex-shrink: 0;
  border-radius: var(--radius-full);
  background: var(--brand-500);
}

/* 摘要统一两行截断，保证卡片高度接近（P1） */
.message-text {
  display: -webkit-box;
  overflow: hidden;
  margin: 0;
  color: var(--ink-500);
  font-size: var(--text-body);
  line-height: 22px;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.message-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: var(--space-1);
  flex-shrink: 0;
}

.message-empty {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
}

/* 详情弹窗：头部改中性表面，去掉橙色渐变（§3.4） */
:deep(.message-detail-dialog) {
  border-radius: var(--radius-lg);
}

:deep(.message-detail-dialog .el-dialog__header) {
  padding: 0;
  margin: 0;
}

:deep(.message-detail-dialog .el-dialog__body) {
  padding: 0 var(--space-6) var(--space-6);
}

:deep(.message-detail-dialog .el-dialog__footer) {
  padding: var(--space-4) var(--space-6);
  border-top: 1px solid var(--line);
}

.detail-modal-header {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5) var(--space-6);
  background: var(--canvas-sunken);
  border-bottom: 1px solid var(--line);
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
}

.detail-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  flex-shrink: 0;
  color: var(--brand-500);
  font-size: 22px;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
}

.header-title-section {
  flex: 1;
  min-width: 0;
}

.detail-modal-title {
  margin: 0 0 var(--space-1);
  font-size: var(--text-h3);
  line-height: 24px;
  font-weight: var(--weight-semibold);
  color: var(--ink-900);
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.detail-time {
  font-family: var(--font-num);
  font-variant-numeric: tabular-nums;
  font-size: var(--text-caption);
  color: var(--ink-500);
}

.detail-body {
  padding: var(--space-6) 0;
}

.detail-text {
  font-size: var(--text-body);
  line-height: 24px;
  color: var(--ink-700);
  white-space: pre-wrap;
  word-break: break-word;
}

.detail-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
}

/* 列表增删/重排：用 transform + opacity，时长与曲线取 token（§3.5） */
.list-enter-active,
.list-leave-active {
  transition:
    opacity var(--duration-base) var(--ease-standard),
    transform var(--duration-base) var(--ease-standard);
}

.list-leave-active {
  position: absolute;
  width: 100%;
}

.list-enter-from {
  opacity: 0;
  transform: translateY(4px);
}

.list-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.list-move {
  transition: transform var(--duration-base) var(--ease-in-out);
}

/* 工具栏出现/收起 */
.toolbar-slide-enter-active,
.toolbar-slide-leave-active {
  transition:
    opacity var(--duration-fast) var(--ease-out),
    transform var(--duration-fast) var(--ease-out);
}

.toolbar-slide-enter-from,
.toolbar-slide-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

/* 减少动态偏好下直接落终值（§3.5） */
@media (prefers-reduced-motion: reduce) {
  .message-card,
  .list-enter-active,
  .list-leave-active,
  .list-move,
  .toolbar-slide-enter-active,
  .toolbar-slide-leave-active {
    transition: none;
  }

  .message-card:hover {
    transform: none;
  }
}
</style>
