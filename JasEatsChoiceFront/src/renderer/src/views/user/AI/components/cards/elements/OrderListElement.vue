<template>
  <div class="order-list-element">
    <!-- 订单列表 -->
    <div class="order-list">
      <div
        v-for="(order, index) in orders"
        :key="order.orderId || index"
        class="order-item"
      >
        <div class="order-header">
          <div class="order-id">
            <span class="order-number">第{{ index + 1 }}单</span>
            <span v-if="order.orderId" class="order-id-text">#{{ order.orderId }}</span>
          </div>
          <el-tag
            v-if="order.statusText"
            :type="getStatusColor(order.status || order.statusText)"
            size="small"
          >
            {{ order.statusText }}
          </el-tag>
        </div>

        <div class="order-info">
          <div v-if="order.dishCount" class="info-row">
            <span class="label">菜品数量：</span>
            <span class="value">{{ order.dishCount }} 道</span>
          </div>
          <div v-if="order.totalAmount" class="info-row">
            <span class="label">总金额：</span>
            <span class="value price">¥{{ order.totalAmount }}</span>
          </div>
          <div v-if="order.createTime" class="info-row">
            <span class="label">下单时间：</span>
            <span class="value">{{ order.createTime }}</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div v-if="order.actions?.length" class="order-actions">
          <el-button
            v-for="action in order.actions"
            :key="action.type"
            :type="getActionType(action.type)"
            size="small"
            @click="handleAction(action.type, order)"
          >
            {{ action.text }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  element: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['action'])

// 订单列表
const orders = computed(() => props.element.orders || [])

/**
 * 状态颜色映射
 * @param {string} status - 订单状态
 * @returns {string}
 */
const getStatusColor = (status) => {
  const colorMap = {
    'pending': 'warning',
    'confirmed': 'primary',
    'preparing': 'primary',
    'delivering': 'primary',
    'completed': 'success',
    'cancelled': 'danger',
    'refunded': 'info'
  }
  return colorMap[status] || 'info'
}

/**
 * 获取操作按钮类型
 * @param {string} actionType - 操作类型
 * @returns {string}
 */
const getActionType = (actionType) => {
  const typeMap = {
    detail: 'primary',
    cancel: 'danger',
    urge: 'warning'
  }
  return typeMap[actionType] || 'default'
}

/**
 * 处理订单操作
 * @param {string} actionType - 操作类型
 * @param {Object} order - 订单数据
 */
const handleAction = (actionType, order) => {
  emit('action', {
    type: actionType,
    data: order
  })
}
</script>

<style scoped>
.order-list-element {
  width: 100%;
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
  border-color: #ff6b6b;
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
  display: flex;
  align-items: center;
  gap: 8px;
}

.order-number {
  background: linear-gradient(135deg, #ff6b6b 0%, #c44569 100%);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.order-id-text {
  color: #666;
  font-size: 13px;
  font-weight: 500;
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
