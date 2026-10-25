<template>
  <div class="coupon-list-card">
    <div class="card-header">
      <div class="header-title">
        <span class="icon">🎟️</span>
        <span class="title">我的优惠券</span>
      </div>
      <div class="header-summary">{{ data.summary }}</div>
    </div>

    <div class="card-content">
      <!-- 统计信息 -->
      <div class="stats-row">
        <div class="stat-item available">
          <span class="stat-label">可用</span>
          <span class="stat-value">{{ data.availableCount }}</span>
        </div>
        <div class="stat-item used">
          <span class="stat-label">已使用</span>
          <span class="stat-value">{{ data.usedCount }}</span>
        </div>
        <div class="stat-item expired">
          <span class="stat-label">已过期</span>
          <span class="stat-value">{{ data.expiredCount }}</span>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!data.coupons || data.coupons.length === 0" class="empty-state">
        <el-empty description="您还没有任何优惠券" />
      </div>

      <!-- 优惠券列表 -->
      <div v-else class="coupon-list">
        <div
          v-for="coupon in data.coupons"
          :key="coupon.couponId"
          class="coupon-item"
          :class="getCouponStatusClass(coupon.status)"
        >
          <div class="coupon-left">
            <div class="coupon-amount">
              <span class="currency">¥</span>
              <span class="amount">{{ coupon.amount }}</span>
            </div>
            <div class="coupon-condition">
              满¥{{ coupon.minAmount }}可用
            </div>
          </div>

          <div class="coupon-divider">
            <div class="divider-circle top"></div>
            <div class="divider-line"></div>
            <div class="divider-circle bottom"></div>
          </div>

          <div class="coupon-right">
            <div class="coupon-name">{{ coupon.name }}</div>
            <div class="coupon-expire">
              <el-tag
                :type="getStatusTagType(coupon.status)"
                size="small"
              >
                {{ getStatusText(coupon.status) }}
              </el-tag>
              <span class="expire-time">{{ coupon.expireTime }}</span>
            </div>
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

// 获取优惠券状态类名
const getCouponStatusClass = (status) => {
  const classMap = {
    'available': 'available',
    'used': 'used',
    'expired': 'expired'
  }
  return classMap[status] || ''
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    'available': 'success',
    'used': 'info',
    'expired': 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'available': '可用',
    'used': '已使用',
    'expired': '已过期'
  }
  return textMap[status] || '未知'
}
</script>

<style scoped>
.coupon-list-card {
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  background: linear-gradient(135deg, #ff6b6b 0%, #f8a5c2 100%);
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
  gap: 12px;
  margin-bottom: 16px;
}

.stat-item {
  flex: 1;
  text-align: center;
  padding: 12px;
  border-radius: 8px;
}

.stat-item.available {
  background: #e8f5e9;
  color: #2e7d32;
}

.stat-item.used {
  background: #e3f2fd;
  color: #1565c0;
}

.stat-item.expired {
  background: #ffebee;
  color: #c62828;
}

.stat-label {
  display: block;
  font-size: 12px;
  margin-bottom: 4px;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: 600;
}

.empty-state {
  padding: 20px;
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.coupon-item {
  display: flex;
  background: white;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
}

.coupon-item.available {
  border-color: #4caf50;
  background: linear-gradient(to right, #f1f8e9, #ffffff);
}

.coupon-item.used {
  border-color: #9e9e9e;
  background: #fafafa;
}

.coupon-item.expired {
  border-color: #f44336;
  background: #ffebee;
}

.coupon-left {
  width: 120px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff6b6b 0%, #c44569 100%);
  color: white;
}

.coupon-amount {
  display: flex;
  align-items: baseline;
  margin-bottom: 4px;
}

.currency {
  font-size: 14px;
}

.amount {
  font-size: 28px;
  font-weight: 600;
}

.coupon-condition {
  font-size: 12px;
  opacity: 0.9;
}

.coupon-divider {
  position: relative;
  width: 20px;
  background: #f0f0f0;
}

.divider-circle {
  position: absolute;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: white;
  left: 0;
}

.divider-circle.top {
  top: -10px;
}

.divider-circle.bottom {
  bottom: -10px;
}

.divider-line {
  position: absolute;
  left: 50%;
  top: 0;
  bottom: 0;
  width: 2px;
  background: repeating-linear-gradient(
    to bottom,
    #e0e0e0 0,
    #e0e0e0 4px,
    transparent 4px,
    transparent 8px
  );
  transform: translateX(-50%);
}

.coupon-right {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.coupon-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.coupon-expire {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.expire-time {
  font-size: 12px;
  color: #999;
}
</style>
