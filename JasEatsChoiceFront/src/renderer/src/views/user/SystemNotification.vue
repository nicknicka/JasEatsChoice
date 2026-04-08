<script setup>
import pinia from '../../store'
import { useAuthStore } from '../../store/authStore'
import { inject } from 'vue'

const authStore = useAuthStore(pinia)

// 注入父组件提供的刷新方法
const refreshUnreadCount = inject('refreshUnreadCount', null)

import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Bell, Notification, Promotion, Lock, ShoppingCart } from '@element-plus/icons-vue'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'

// 用户ID
const userId = ref(parseInt(String(authStore.userId || 1) || '1', 10))

// ========== 通知数据 ==========
const notifications = ref([])
const loading = ref(false)

// 页面加载时初始化
onMounted(async () => {
  await loadNotifications()
  await loadUnreadCount()
})

/**
 * 加载系统通知
 */
const loadNotifications = async () => {
  loading.value = true
  try {
    const response = await api.get(`${API_CONFIG.message.list}/${userId.value}`)

    if (response && (response.code === '200' || response.code === 200)) {
      // 转换后端返回的数据格式
      const formattedNotifications = response.data.map((item) => ({
        id: item.id,
        title: item.title || '系统通知',
        content: item.content,
        time: item.sendTime || item.createTime,
        read: item.readStatus,
        type: item.type || 'system'
      }))

      notifications.value = formattedNotifications
    }
  } catch (error) {
    console.error('加载通知失败:', error)
  } finally {
    loading.value = false
  }
}

// 当前选中的标签
const activeTab = ref('all')

// 筛选通知
const filteredNotifications = computed(() => {
  if (activeTab.value === 'all') {
    return notifications.value
  }
  return notifications.value.filter((n) => n.type === activeTab.value)
})

// 未读数量
const unreadCount = computed(() => {
  return notifications.value.filter((n) => !n.read).length
})

// 加载未读数量
const loadUnreadCount = async () => {
  try {
    const response = await api.get('/notifications/unread-count', {
      params: { userId: userId.value }
    })

    if (response.code === '200') {
      // 已通过computed计算
    }
  } catch (error) {
    console.error('获取未读数量失败:', error)
  }
}

// ========== 通知详情 ==========
const detailNotification = ref(null)
const showDetailModal = ref(false)

// 查看通知详情
const viewNotification = async (notification) => {
  if (!notification.read) {
    await markAsRead(notification.id)
  }
  detailNotification.value = notification
  showDetailModal.value = true
}

// ========== 通知操作 ==========

// 单条删除
const deleteNotification = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除这条通知吗？', '删除通知', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await api.delete(`/notifications/${id}`)
    if (response.code === '200') {
      ElMessage.success('通知已删除')
      notifications.value = notifications.value.filter((n) => n.id !== id)
      if (refreshUnreadCount) refreshUnreadCount()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除通知失败:', error)
      ElMessage.error('删除通知失败')
    }
  }
}

// 批量选择
const selectedIds = ref([])
const selectAll = ref(false)

const isSelected = (id) => selectedIds.value.includes(id)

const handleSelect = (notification, checked) => {
  if (checked) {
    if (!isSelected(notification.id)) {
      selectedIds.value.push(notification.id)
    }
  } else {
    selectedIds.value = selectedIds.value.filter((id) => id !== notification.id)
  }
  selectAll.value = selectedIds.value.length === filteredNotifications.value.length && filteredNotifications.value.length > 0
}

const handleSelectAll = (checked) => {
  if (checked) {
    selectedIds.value = filteredNotifications.value.map((n) => n.id)
  } else {
    selectedIds.value = []
  }
}

// 批量删除
const deleteSelected = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要删除的通知')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${selectedIds.value.length} 条通知吗？`,
      '批量删除',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await api.delete('/notifications/batch', { data: selectedIds.value })
    if (response.code === '200') {
      ElMessage.success(`成功删除 ${selectedIds.value.length} 条通知`)
      notifications.value = notifications.value.filter((n) => !selectedIds.value.includes(n.id))
      selectedIds.value = []
      selectAll.value = false
      if (refreshUnreadCount) refreshUnreadCount()
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

// 全部标记已读
const markAllAsRead = async () => {
  try {
    const response = await api.put('/notifications/all-read', null, {
      params: { userId: userId.value }
    })

    if (response.code === '200') {
      notifications.value.forEach((n) => {
        n.read = true
      })
      if (refreshUnreadCount) refreshUnreadCount()
      ElMessage.success('已将所有通知标记为已读')
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('标记全部已读失败:', error)
    ElMessage.error('标记全部已读失败')
  }
}

// 标记单条已读
const markAsRead = async (id) => {
  try {
    const response = await api.put(`/notifications/${id}/read`)
    if (response.code === '200') {
      const notification = notifications.value.find((n) => n.id === id)
      if (notification) {
        notification.read = true
      }
      if (refreshUnreadCount) refreshUnreadCount()
    }
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

// 从详情页删除
const handleDeleteFromDetail = async (id) => {
  await deleteNotification(id)
  showDetailModal.value = false
}

// ========== 辅助方法 ==========

// 获取通知类型配置
const getTypeConfig = (type) => {
  const configs = {
    system: { icon: Notification, label: '系统公告', color: 'primary' },
    promotion: { icon: Promotion, label: '活动通知', color: 'success' },
    security: { icon: Lock, label: '安全提醒', color: 'danger' },
    order: { icon: ShoppingCart, label: '订单更新', color: 'warning' }
  }
  return configs[type] || configs.system
}

// 获取通知图标
const getTypeIcon = (type) => {
  const icons = {
    system: '📢',
    promotion: '🎉',
    security: '🔒',
    order: '📦'
  }
  return icons[type] || '📢'
}

// 获取空状态配置
const getEmptyConfig = () => {
  const configs = {
    all: { icon: '📭', text: '暂无通知' },
    system: { icon: '📢', text: '暂无系统公告' },
    promotion: { icon: '🎉', text: '暂无活动通知' },
    security: { icon: '🔒', text: '暂无安全提醒' },
    order: { icon: '📦', text: '暂无订单更新' }
  }
  return configs[activeTab.value] || configs.all
}

// 标签页配置
const tabs = [
  { name: 'all', label: '全部通知', icon: Bell },
  { name: 'system', label: '系统公告', icon: Notification },
  { name: 'promotion', label: '活动通知', icon: Promotion },
  { name: 'security', label: '安全提醒', icon: Lock },
  { name: 'order', label: '订单更新', icon: ShoppingCart }
]
</script>

<template>
  <div class="notification-container">
    <!-- 页面头部 -->
    <header class="page-header fade-in-up">
      <div class="header-left">
        <h2>系统通知</h2>
        <p class="header-subtitle">管理您的系统消息和提醒</p>
      </div>
      <div v-if="unreadCount > 0" class="unread-badge">
        <span class="badge-icon">🔔</span>
        <span class="badge-text">{{ unreadCount }} 条未读</span>
      </div>
    </header>

    <!-- 筛选标签页 -->
    <nav class="tab-nav slide-in-left delay-100">
      <div class="tab-nav-track">
        <button
          v-for="tab in tabs"
          :key="tab.name"
          class="tab-btn"
          :class="{ active: activeTab === tab.name }"
          @click="activeTab = tab.name"
        >
          <el-icon :size="16"><component :is="tab.icon" /></el-icon>
          <span>{{ tab.label }}</span>
          <el-badge
            v-if="tab.name === 'all' && unreadCount > 0"
            :value="unreadCount"
            class="tab-badge"
          />
        </button>
      </div>
    </nav>

    <!-- 通知列表区域 -->
    <div class="notification-content slide-in-left delay-200">
      <!-- 操作工具栏 -->
      <transition name="toolbar-fade">
        <div v-if="selectedIds.length > 0 || unreadCount > 0" class="action-toolbar">
          <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
          <div class="toolbar-actions">
            <el-button
              type="danger"
              plain
              :disabled="selectedIds.length === 0"
              @click="deleteSelected"
            >
              <el-icon><Delete /></el-icon>
              批量删除 ({{ selectedIds.length }})
            </el-button>
            <el-button type="primary" plain :disabled="unreadCount === 0" @click="markAllAsRead">
              全部标记已读
            </el-button>
          </div>
        </div>
      </transition>

      <!-- 通知列表 -->
      <div class="notification-list">
        <transition-group name="list" tag="div">
          <div
            v-for="notification in filteredNotifications"
            :key="notification.id"
            class="notification-card"
            :class="{ unread: !notification.read, selected: isSelected(notification.id) }"
          >
            <div class="card-checkbox">
              <el-checkbox
                :model-value="isSelected(notification.id)"
                @change="handleSelect(notification, $event)"
              />
            </div>

            <div class="card-main" @click="viewNotification(notification)">
              <div class="card-header">
                <div class="type-indicator">
                  <span class="type-icon">{{ getTypeIcon(notification.type) }}</span>
                  <el-tag
                    :type="getTypeConfig(notification.type).color"
                    size="small"
                    effect="light"
                  >
                    {{ getTypeConfig(notification.type).label }}
                  </el-tag>
                </div>
                <span class="card-time">{{ notification.time }}</span>
              </div>

              <h3 class="card-title">{{ notification.title }}</h3>
              <p class="card-content">{{ notification.content }}</p>
            </div>

            <div class="card-actions">
              <el-button type="primary" text size="small" @click.stop="viewNotification(notification)">
                查看详情
              </el-button>
              <el-button type="danger" text size="small" @click.stop="deleteNotification(notification.id)">
                删除
              </el-button>
            </div>
          </div>
        </transition-group>

        <!-- 空状态 -->
        <transition name="empty-fade" mode="out-in">
          <div v-if="filteredNotifications.length === 0 && !loading" class="empty-state">
            <div class="empty-icon-wrapper">
              <span class="empty-icon">{{ getEmptyConfig().icon }}</span>
            </div>
            <p class="empty-text">{{ getEmptyConfig().text }}</p>
          </div>
        </transition>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <el-icon class="loading-icon" :size="32"><Loading /></el-icon>
          <p>加载中...</p>
        </div>
      </div>
    </div>

    <!-- 通知详情模态框 -->
    <el-dialog
      v-model="showDetailModal"
      width="600px"
      :close-on-click-modal="false"
      class="detail-dialog"
    >
      <template #header>
        <div class="detail-header">
          <div class="detail-icon-wrapper">
            <span class="detail-icon">{{ getTypeIcon(detailNotification?.type) }}</span>
          </div>
          <div class="detail-title-section">
            <h3 class="detail-title">{{ detailNotification?.title }}</h3>
            <div class="detail-meta">
              <el-tag
                :type="getTypeConfig(detailNotification?.type).color"
                size="small"
                effect="light"
              >
                {{ getTypeConfig(detailNotification?.type).label }}
              </el-tag>
              <span class="detail-time">{{ detailNotification?.time }}</span>
            </div>
          </div>
        </div>
      </template>

      <div v-if="detailNotification" class="detail-body">
        <p class="detail-content">{{ detailNotification.content }}</p>
      </div>

      <template #footer>
        <div class="detail-footer">
          <el-button @click="showDetailModal = false">关闭</el-button>
          <el-button
            v-if="detailNotification"
            type="danger"
            plain
            @click="handleDeleteFromDetail(detailNotification.id)"
          >
            删除此通知
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';

// 列表动画
.list-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.list-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 1, 1);
  position: absolute;
  width: calc(100% - 48px);
}

.list-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.list-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

.list-move {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

// 工具栏动画
.toolbar-fade-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.toolbar-fade-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 1, 1);
}

.toolbar-fade-enter-from,
.toolbar-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

// 空状态动画
.empty-fade-enter-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
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

.notification-container {
  padding: @nordic-space-lg;
  background: @nordic-bg;
  min-height: 100%;
  display: flex;
  flex-direction: column;
  gap: @nordic-space-lg;

  // 页面头部
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: @nordic-space-lg;

    .header-left {
      h2 {
        font-size: @nordic-text-xl;
        font-weight: 700;
        color: @nordic-text;
        margin: 0 0 6px 0;
        letter-spacing: -0.5px;
      }

      .header-subtitle {
        font-size: @nordic-text-base;
        color: @nordic-text-muted;
        margin: 0;
      }
    }

    .unread-badge {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 10px 18px;
      background: linear-gradient(135deg, @nordic-accent 0%, @nordic-accent-dark 100%);
      border-radius: @nordic-radius-pill;
      box-shadow: 0 4px 12px fade(@nordic-accent, 30%);

      .badge-icon {
        font-size: @nordic-text-lg;
        animation: bellRing 2s ease-in-out infinite;
      }

      .badge-text {
        font-size: @nordic-text-sm;
        font-weight: 600;
        color: @nordic-surface;
      }
    }
  }

  // 标签导航
  .tab-nav {
    .tab-nav-track {
      display: inline-flex;
      background: @nordic-border;
      border-radius: @nordic-radius-lg;
      padding: 4px;
      gap: 4px;
    }

    .tab-btn {
      position: relative;
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 10px 18px;
      border: none;
      background: transparent;
      color: @nordic-text-secondary;
      font-size: @nordic-text-sm;
      font-weight: 500;
      cursor: pointer;
      border-radius: @nordic-radius-md;
      transition: all 0.25s ease;
      font-family: inherit;

      &:hover:not(.active) {
        color: @nordic-text;
        background: fade(@nordic-surface, 50%);
      }

      &.active {
        background: @nordic-surface;
        color: @nordic-text;
        box-shadow: 0 1px 4px @nordic-shadow;
      }

      .tab-badge {
        margin-left: 4px;

        :deep(.el-badge__content) {
          background: @nordic-red;
          border-color: @nordic-red;
        }
      }
    }
  }

  // 内容区域
  .notification-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: @nordic-space-md;
    min-height: 0;

    // 操作工具栏
    .action-toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      background: linear-gradient(135deg, @nordic-blue 0%, darken(@nordic-blue, 10%) 100%);
      border-radius: @nordic-radius-lg;
      box-shadow: 0 4px 12px fade(@nordic-blue, 25%);

      :deep(.el-checkbox__label) {
        color: @nordic-surface;
        font-weight: 500;
      }

      :deep(.el-checkbox__inner) {
        background: fade(@nordic-surface, 20%);
        border-color: fade(@nordic-surface, 40%);
      }

      :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
        background: @nordic-surface;
        border-color: @nordic-surface;

        &::after {
          border-color: @nordic-blue;
        }
      }

      .toolbar-actions {
        display: flex;
        gap: 12px;
      }
    }

    // 通知列表
    .notification-list {
      flex: 1;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      gap: @nordic-space-md;
      padding-right: 8px;

      &::-webkit-scrollbar {
        width: 6px;
      }

      &::-webkit-scrollbar-track {
        background: transparent;
      }

      &::-webkit-scrollbar-thumb {
        background: @nordic-border;
        border-radius: 3px;

        &:hover {
          background: @nordic-text-muted;
        }
      }
    }
  }

  // 通知卡片
  .notification-card {
    .nordic-card();
    display: flex;
    align-items: flex-start;
    gap: @nordic-space-md;
    padding: @nordic-space-lg;
    cursor: pointer;
    position: relative;

    &.unread {
      border-left: 4px solid @nordic-blue;

      &::before {
        content: '';
        position: absolute;
        top: 16px;
        right: 16px;
        width: 8px;
        height: 8px;
        background: @nordic-blue;
        border-radius: 50%;
        animation: pulse 2s ease-in-out infinite;
      }
    }

    &.selected {
      background: @nordic-blue-light;
      border-color: @nordic-blue;
    }

    .card-checkbox {
      flex-shrink: 0;
      margin-top: 4px;
    }

    .card-main {
      flex: 1;
      min-width: 0;

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 10px;

        .type-indicator {
          display: flex;
          align-items: center;
          gap: 8px;

          .type-icon {
            font-size: @nordic-text-lg;
          }
        }

        .card-time {
          font-size: @nordic-text-xs;
          color: @nordic-text-muted;
        }
      }

      .card-title {
        font-size: @nordic-text-md;
        font-weight: 600;
        color: @nordic-text;
        margin: 0 0 6px 0;
        line-height: 1.4;
      }

      .card-content {
        font-size: @nordic-text-base;
        color: @nordic-text-secondary;
        margin: 0;
        line-height: 1.6;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
    }

    .card-actions {
      display: flex;
      flex-direction: column;
      gap: 8px;
      flex-shrink: 0;
    }
  }

  // 空状态
  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60px @nordic-space-lg;

    .empty-icon-wrapper {
      width: 100px;
      height: 100px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, @nordic-bg 0%, @nordic-border 100%);
      border-radius: 50%;
      margin-bottom: @nordic-space-lg;
      animation: float 3s ease-in-out infinite;
      box-shadow: 0 8px 24px @nordic-shadow;

      .empty-icon {
        font-size: 48px;
      }
    }

    .empty-text {
      font-size: @nordic-text-md;
      color: @nordic-text-muted;
      margin: 0;
    }
  }

  // 加载状态
  .loading-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60px @nordic-space-lg;
    color: @nordic-text-muted;

    .loading-icon {
      animation: spin 1s linear infinite;
      margin-bottom: @nordic-space-md;
    }

    p {
      margin: 0;
      font-size: @nordic-text-base;
    }
  }
}

// 详情模态框
:deep(.detail-dialog) {
  border-radius: @nordic-radius-lg;
  overflow: hidden;

  .el-dialog__header {
    padding: 0;
    margin: 0;
  }

  .el-dialog__body {
    padding: @nordic-space-lg;
  }

  .el-dialog__footer {
    padding: @nordic-space-md @nordic-space-lg;
    border-top: 1px solid @nordic-border;
  }
}

.detail-header {
  display: flex;
  align-items: center;
  gap: @nordic-space-md;
  padding: @nordic-space-lg;
  background: linear-gradient(135deg, @nordic-accent 0%, @nordic-accent-dark 100%);
  margin: -20px -20px 0 -20px;

  .detail-icon-wrapper {
    width: 52px;
    height: 52px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: fade(@nordic-surface, 25%);
    border-radius: @nordic-radius-lg;
    backdrop-filter: blur(10px);

    .detail-icon {
      font-size: @nordic-text-xl;
    }
  }

  .detail-title-section {
    flex: 1;

    .detail-title {
      font-size: @nordic-text-lg;
      font-weight: 600;
      color: @nordic-surface;
      margin: 0 0 8px 0;
      line-height: 1.3;
    }

    .detail-meta {
      display: flex;
      align-items: center;
      gap: 12px;

      .detail-time {
        font-size: @nordic-text-sm;
        color: fade(@nordic-surface, 90%);
      }
    }
  }
}

.detail-body {
  .detail-content {
    font-size: 15px;
    line-height: 1.8;
    color: @nordic-text;
    margin: 0;
    white-space: pre-wrap;
    word-wrap: break-word;
  }
}

.detail-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

// 动画关键帧
@keyframes bellRing {
  0%, 100% {
    transform: rotate(0deg);
  }
  10%, 30% {
    transform: rotate(-12deg);
  }
  20%, 40% {
    transform: rotate(12deg);
  }
  50% {
    transform: rotate(0deg);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.6;
    transform: scale(0.8);
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) scale(1);
  }
  50% {
    transform: translateY(-8px) scale(1.02);
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

// 入场动画
.fade-in-up {
  animation: fadeInUp 0.5s ease-out both;
}

.slide-in-left {
  animation: slideInLeft 0.5s ease-out both;

  &.delay-100 {
    animation-delay: 0.1s;
  }

  &.delay-200 {
    animation-delay: 0.2s;
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-16px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}
</style>
