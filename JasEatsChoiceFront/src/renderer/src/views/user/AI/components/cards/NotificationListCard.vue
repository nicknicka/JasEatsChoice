<template>
  <div class="notification-list-card">
    <div class="card-header">
      <div class="header-title">
        <span class="icon">📬</span>
        <span class="title">通知消息</span>
      </div>
      <div class="header-summary">{{ data.summary }}</div>
    </div>

    <div class="card-content">
      <!-- 统计信息 -->
      <div class="stats-row">
        <div class="stat-item">
          <span class="stat-label">总通知</span>
          <span class="stat-value">{{ data.total }}</span>
        </div>
        <div class="stat-item unread" v-if="data.unreadCount > 0">
          <span class="stat-label">未读</span>
          <span class="stat-value">{{ data.unreadCount }}</span>
        </div>
      </div>

      <!-- 类型统计 -->
      <div class="type-stats" v-if="data.typeStats && Object.keys(data.typeStats).length > 0">
        <div class="type-stat-item" v-for="(count, type) in data.typeStats" :key="type">
          <span class="type-icon">{{ getTypeIcon(type) }}</span>
          <span class="type-label">{{ getTypeLabel(type) }}</span>
          <span class="type-count">{{ count }}</span>
        </div>
      </div>

      <!-- 通知列表 -->
      <div class="notification-list">
        <div
          v-for="(notification, index) in data.notifications"
          :key="notification.notificationId"
          class="notification-item"
          :class="{ 'unread': !notification.isRead }"
        >
          <div class="notification-header">
            <div class="notification-title-row">
              <span class="unread-badge" v-if="!notification.isRead">🔴</span>
              <span class="type-icon">{{ getTypeIcon(notification.type) }}</span>
              <span class="notification-title">{{ notification.title }}</span>
            </div>
            <span class="notification-time">{{ notification.time }}</span>
          </div>

          <div class="notification-content" v-if="notification.content">
            {{ notification.content }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  data: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['action'])

// 获取类型图标
const getTypeIcon = (type) => {
  const iconMap = {
    'order': '📦',
    'system': '📢',
    'promotion': '🎁'
  }
  return iconMap[type] || '📢'
}

// 获取类型标签
const getTypeLabel = (type) => {
  const labelMap = {
    'order': '订单',
    'system': '系统',
    'promotion': '活动'
  }
  return labelMap[type] || '其他'
}
</script>

<style scoped>
.notification-list-card {
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  padding: 16px 20px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 4px;
}

.icon {
  font-size: 24px;
}

.header-summary {
  font-size: 14px;
  opacity: 0.9;
}

.card-content {
  background: white;
  padding: 16px;
}

.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.stat-item {
  flex: 1;
  text-align: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-item.unread {
  background: #fff3e0;
  color: #e65100;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: 600;
}

.type-stats {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.type-stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 20px;
  font-size: 13px;
}

.type-icon {
  font-size: 16px;
}

.type-label {
  color: #666;
}

.type-count {
  font-weight: 600;
  color: #333;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 12px;
  transition: all 0.3s;
  background: white;
}

.notification-item.unread {
  border-color: #f5576c;
  background: #fff8f9;
}

.notification-item:hover {
  border-color: #f5576c;
  box-shadow: 0 2px 8px rgba(245, 87, 108, 0.1);
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.notification-title-row {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
}

.unread-badge {
  font-size: 12px;
}

.type-icon {
  font-size: 16px;
}

.notification-title {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.notification-time {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
}

.notification-content {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  padding-left: 26px;
}
</style>
