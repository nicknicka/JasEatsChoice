<template>
  <div class="order-list-card">
    <div class="card-header">
      <div class="header-title">
        <span class="icon">📋</span>
        <span class="title">我的订单</span>
      </div>
      <div class="header-summary">{{ data.summary }}</div>
    </div>

    <div class="card-content">
      <!-- 统计信息 -->
      <div class="stats-row">
        <div class="stat-item">
          <span class="stat-label">总订单</span>
          <span class="stat-value">{{ data.total }}</span>
        </div>
        <div class="stat-item pending" v-if="data.pendingCount > 0">
          <span class="stat-label">待处理</span>
          <span class="stat-value">{{ data.pendingCount }}</span>
        </div>
      </div>

      <!-- 订单列表 -->
      <div class="order-list">
        <div
          v-for="order in data.orders"
          :key="order.orderId"
          class="order-item"
        >
          <div class="order-header">
            <div class="order-id">订单 #{{ order.orderId }}</div>
            <el-tag
              :type="getStatusColor(order.status)"
              size="small"
            >
              {{ order.statusText }}
            </el-tag>
          </div>

          <div class="order-info">
            <div class="info-row">
              <span class="label">菜品数量：</span>
              <span class="value">{{ order.dishCount }} 道</span>
            </div>
            <div class="info-row">
              <span class="label">总金额：</span>
              <span class="value price">¥{{ order.totalAmount }}</span>
            </div>
            <div class="info-row">
              <span class="label">下单时间：</span>
              <span class="value">{{ order.createTime }}</span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="order-actions" v-if="order.actions && order.actions.length > 0">
            <el-button
              v-for="action in order.actions"
              :key="action.type"
              :type="getActionType(action.type)"
              :icon="getActionIcon(action.icon)"
              size="small"
              @click="handleAction(action.type, order)"
            >
              {{ action.text }}
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { OrderStatusMap } from '../../constants/messageTypes'

const props = defineProps({
  data: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['action'])

// 获取状态颜色
const getStatusColor = (status) => {
  return OrderStatusMap[status]?.color || 'info'
}

// 获取操作按钮类型
const getActionType = (actionType) => {
  const typeMap = {
    'detail': 'primary',
    'cancel': 'danger',
    'urge': 'warning'
  }
  return typeMap[actionType] || 'default'
}

// 获取操作图标
const getActionIcon = (iconName) => {
  // 这里返回图标名称，实际使用时需要从 @element-plus/icons-vue 导入
  const iconMap = {
    'View': 'View',
    'Delete': 'Delete',
    'Bell': 'Bell'
  }
  return iconMap[iconName] || 'Operation'
}

// 处理操作
const handleAction = (actionType, order) => {
  emit('action', {
    type: actionType,
    data: order
  })
}
</script>

<style scoped>
.order-list-card {
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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

.stat-item.pending {
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

.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 12px;
  transition: all 0.3s;
}

.order-item:hover {
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.order-id {
  font-weight: 600;
  color: #333;
}

.order-info {
  margin-bottom: 12px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 14px;
}

.label {
  color: #666;
}

.value {
  color: #333;
  font-weight: 500;
}

.value.price {
  color: #f56c6c;
  font-size: 16px;
  font-weight: 600;
}

.order-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
