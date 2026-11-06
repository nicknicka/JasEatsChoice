<script setup>
import pinia from '../../store'
import { useAuthStore } from '../../store/authStore'
import { inject, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Bell, Notification, Promotion, Lock, ShoppingCart } from '@element-plus/icons-vue'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import AppCard from '../../components/common/AppCard.vue'
import AppEmptyState from '../../components/common/AppEmptyState.vue'
import StatusTag from '../../components/common/StatusTag.vue'

const authStore = useAuthStore(pinia)

// 注入父组件提供的刷新方法
const refreshUnreadCount = inject('refreshUnreadCount', null)

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
        time: item.sendTime || item.createdAt,
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

// ========== 日期分组 ==========
// 单条时间统一 YYYY-MM-DD HH:mm（§4.3、§6.1，去掉 ISO 的 T）
const formatDateTime = (time) => {
  const d = new Date(time)
  if (Number.isNaN(d.getTime())) return ''
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const dateKey = (time) => {
  const d = new Date(time)
  if (Number.isNaN(d.getTime())) return 'unknown'
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

// 分组标题：今天 / 昨天 / YYYY-MM-DD（§4.3）
const groupLabel = (time) => {
  const d = new Date(time)
  if (Number.isNaN(d.getTime())) return '更早'
  const startOfDay = (x) => new Date(x.getFullYear(), x.getMonth(), x.getDate()).getTime()
  const diff = Math.round((startOfDay(new Date()) - startOfDay(d)) / 86400000)
  if (diff === 0) return '今天'
  if (diff === 1) return '昨天'
  return dateKey(time)
}

// 按日期分组，保持后端返回的时间倒序
const groupedNotifications = computed(() => {
  const groups = []
  const index = new Map()
  notifications.value.forEach((notification) => {
    const key = dateKey(notification.time)
    if (!index.has(key)) {
      const group = { key, label: groupLabel(notification.time), items: [] }
      index.set(key, group)
      groups.push(group)
    }
    index.get(key).items.push(notification)
  })
  return groups
})

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
  selectAll.value =
    selectedIds.value.length === notifications.value.length && notifications.value.length > 0
}

const handleSelectAll = (checked) => {
  if (checked) {
    selectedIds.value = notifications.value.map((n) => n.id)
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

// 通知类型配置：标签语义色 + 线性图标（§3.1 状态表、§4.9）
const getTypeConfig = (type) => {
  const configs = {
    system: { icon: Notification, label: '系统公告', color: 'primary' },
    promotion: { icon: Promotion, label: '活动通知', color: 'success' },
    security: { icon: Lock, label: '安全提醒', color: 'danger' },
    order: { icon: ShoppingCart, label: '订单更新', color: 'warning' }
  }
  return configs[type] || configs.system
}
</script>

<template>
  <div class="system-notification-page">
    <!-- 页头：标准版，标题 --text-h1 + 上下 --space-6（§4.7） -->
    <AppPageHeader title="系统通知" subtitle="管理您的系统消息和提醒">
      <template #extra>
        <span v-if="unreadCount > 0" class="unread-chip">
          <el-icon><Bell /></el-icon>{{ unreadCount }} 条未读
        </span>
      </template>
      <template #actions>
        <el-button type="primary" :disabled="unreadCount === 0" @click="markAllAsRead">
          全部标记已读
        </el-button>
      </template>
    </AppPageHeader>

    <!-- 统一消息列表：单列表 + 日期分组，去掉重复的灰色 tab（P0，§4.2） -->
    <AppCard class="notice-card" padding="none">
      <div class="notice-toolbar">
        <el-checkbox
          v-model="selectAll"
          :disabled="notifications.length === 0"
          @change="handleSelectAll"
        >
          全选
        </el-checkbox>
        <el-button
          type="danger"
          plain
          size="small"
          :disabled="selectedIds.length === 0"
          @click="deleteSelected"
        >
          <el-icon><Delete /></el-icon>
          批量删除 ({{ selectedIds.length }})
        </el-button>
      </div>

      <div v-loading="loading" class="notice-list">
        <template v-if="groupedNotifications.length > 0">
          <section v-for="group in groupedNotifications" :key="group.key" class="notice-group">
            <h2 class="notice-group__title">{{ group.label }}</h2>

            <article
              v-for="notification in group.items"
              :key="notification.id"
              class="notice-row"
              :class="{
                'is-unread': !notification.read,
                'is-selected': isSelected(notification.id)
              }"
            >
              <el-checkbox
                class="notice-row__check"
                :model-value="isSelected(notification.id)"
                @change="handleSelect(notification, $event)"
              />

              <div class="notice-row__icon">
                <el-icon><component :is="getTypeConfig(notification.type).icon" /></el-icon>
              </div>

              <div
                class="notice-row__main"
                role="button"
                tabindex="0"
                @click="viewNotification(notification)"
                @keydown.enter="viewNotification(notification)"
              >
                <div class="notice-row__head">
                  <el-tag
                    :type="getTypeConfig(notification.type).color"
                    size="small"
                    effect="light"
                  >
                    {{ getTypeConfig(notification.type).label }}
                  </el-tag>
                  <h3 class="notice-row__title">{{ notification.title }}</h3>
                  <StatusTag v-if="!notification.read" status="unread" size="sm" />
                </div>
                <p class="notice-row__content">{{ notification.content }}</p>
                <time class="notice-row__time">{{ formatDateTime(notification.time) }}</time>
              </div>

              <div class="notice-row__actions">
                <el-button
                  type="primary"
                  text
                  size="small"
                  @click.stop="viewNotification(notification)"
                >
                  查看详情
                </el-button>
                <el-button
                  type="danger"
                  text
                  size="small"
                  @click.stop="deleteNotification(notification.id)"
                >
                  删除
                </el-button>
              </div>
            </article>
          </section>
        </template>

        <AppEmptyState
          v-else-if="!loading"
          icon="Bell"
          description="暂无通知"
          action-text="重新加载"
          @action="loadNotifications"
        />
      </div>
    </AppCard>

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
            <el-icon><component :is="getTypeConfig(detailNotification?.type).icon" /></el-icon>
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
              <span class="detail-time">{{ formatDateTime(detailNotification?.time) }}</span>
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

<style scoped>
/* 页面颜色、间距、圆角、字阶与动效均取自 tokens.css（§3、§7.1），无裸色值。 */
.system-notification-page {
  box-sizing: border-box;
  width: 100%;
  max-width: var(--page-max);
  min-height: 100%;
  margin: 0 auto;
  padding: 0 var(--page-gutter) var(--space-10);
  background: var(--canvas);
}

/* 页头未读计数：辅助信息，不动用主色（§3.1、§4.7） */
.unread-chip {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: 2px var(--space-2);
  border-radius: var(--radius-full);
  background: var(--canvas-sunken);
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  line-height: 20px;
}

/* 列表面板：L1 微质感 */
.notice-card {
  overflow: hidden;
}

/* 批量操作条：中性表面 + 唯一主按钮已在页头（§4.1、§3.4） */
.notice-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-3) var(--card-padding);
  background: var(--canvas-sunken);
  border-bottom: 1px solid var(--line);
}

.notice-list {
  min-height: 240px;
}

/* 日期分组标题：分区标题从 h2 起（§4.7） */
.notice-group__title {
  margin: 0;
  padding: var(--space-3) var(--card-padding) var(--space-2);
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  line-height: 20px;
  background: var(--canvas-sunken);
  border-bottom: 1px solid var(--line);
}

/* 通知行：统一消息行骨架（§4.2） */
.notice-row {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  padding: var(--card-padding);
  border-bottom: 1px solid var(--line);
  transition: background-color var(--duration-fast) var(--ease-out);
}

.notice-row:last-child {
  border-bottom: none;
}

.notice-row:hover {
  background: var(--canvas-sunken);
}

.notice-row.is-selected {
  background: var(--canvas-sunken);
}

.notice-row.is-unread .notice-row__title {
  color: var(--ink-900);
  font-weight: var(--weight-semibold);
}

.notice-row__check {
  flex-shrink: 0;
  margin-top: var(--space-1);
}

.notice-row__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  background: var(--canvas-sunken);
  color: var(--ink-500);
  font-size: var(--text-h3);
}

.notice-row__main {
  flex: 1;
  min-width: 0;
  border-radius: var(--radius-sm);
  cursor: pointer;
}

.notice-row__main:focus-visible {
  outline: 2px solid var(--brand-400);
  outline-offset: 2px;
}

.notice-row__head {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-1);
  min-width: 0;
}

.notice-row__title {
  overflow: hidden;
  margin: 0;
  color: var(--ink-700);
  font-size: var(--text-body);
  font-weight: var(--weight-medium);
  line-height: 22px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-row__content {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  margin: 0;
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  line-height: 20px;
}

.notice-row__time {
  display: block;
  margin-top: var(--space-1);
  color: var(--ink-400);
  font-family: var(--font-num);
  font-size: var(--text-caption);
  line-height: 18px;
  font-variant-numeric: tabular-nums;
}

.notice-row__actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-shrink: 0;
}

/* 详情弹层 */
.detail-dialog :deep(.el-dialog__body) {
  padding: var(--space-5) var(--space-6);
}

.detail-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.detail-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  background: var(--canvas-sunken);
  color: var(--ink-500);
  font-size: var(--text-h3);
}

.detail-title-section {
  flex: 1;
  min-width: 0;
}

.detail-title {
  margin: 0 0 var(--space-1);
  color: var(--ink-900);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  line-height: 24px;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.detail-time {
  color: var(--ink-400);
  font-family: var(--font-num);
  font-size: var(--text-caption);
  font-variant-numeric: tabular-nums;
}

.detail-content {
  margin: 0;
  color: var(--ink-700);
  font-size: var(--text-body);
  line-height: 24px;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.detail-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
}

/* 无障碍：减少动态偏好下不播放过渡（§3.5） */
@media (prefers-reduced-motion: reduce) {
  .notice-row {
    transition: none;
  }
}
</style>
