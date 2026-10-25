<template>
  <div class="review-list-card">
    <div class="card-header">
      <div class="header-title">
        <span class="icon">💬</span>
        <span class="title">我的评价</span>
      </div>
      <div class="header-summary">{{ data.summary }}</div>
    </div>

    <div class="card-content">
      <!-- 统计信息 -->
      <div class="stats-row" v-if="data.avgRating">
        <div class="stat-item">
          <span class="stat-label">平均评分</span>
          <div class="rating-wrapper">
            <span class="stat-value">{{ data.avgRating }}</span>
            <el-rate
              v-model="avgRatingValue"
              disabled
              show-score
              text-color="#ff9900"
            />
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!data.reviews || data.reviews.length === 0" class="empty-state">
        <el-empty description="您还没有发布任何评价" />
      </div>

      <!-- 评价列表 -->
      <div v-else class="review-list">
        <div
          v-for="review in data.reviews"
          :key="review.reviewId"
          class="review-item"
        >
          <div class="review-header">
            <div class="dish-info">
              <img
                v-if="review.dishImage"
                :src="review.dishImage"
                :alt="review.dishName"
                class="dish-thumb"
              />
              <div class="dish-details">
                <div class="dish-name">{{ review.dishName }}</div>
                <div class="order-id">订单 #{{ review.orderId }}</div>
              </div>
            </div>
            <div class="rating">
              <el-rate
                v-model="review.rating"
                disabled
                show-score
                text-color="#ff9900"
              />
            </div>
          </div>

          <div class="review-content">{{ review.content }}</div>

          <!-- 评价图片 -->
          <div v-if="review.images && review.images.length > 0" class="review-images">
            <img
              v-for="(img, index) in review.images.slice(0, 3)"
              :key="index"
              :src="img"
              :alt="`评价图片${index + 1}`"
              class="review-image"
            />
          </div>

          <div class="review-footer">
            <span class="review-time">{{ review.createTime }}</span>
            <div class="review-actions">
              <el-button
                v-for="action in review.actions"
                :key="action.type"
                :type="action.type === 'delete' ? 'danger' : 'primary'"
                :icon="getActionIcon(action.icon)"
                size="small"
                text
                @click="handleAction(action.type, review)"
              >
                {{ action.text }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  data: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['action'])

// 平均评分转换
const avgRatingValue = computed(() => {
  return props.data.avgRating ? parseFloat(props.data.avgRating) : 0
})

// 获取操作图标
const getActionIcon = (iconName) => {
  const iconMap = {
    'View': 'View',
    'Delete': 'Delete'
  }
  return iconMap[iconName] || 'Operation'
}

// 处理操作
const handleAction = (actionType, review) => {
  emit('action', {
    type: actionType,
    data: review
  })
}
</script>

<style scoped>
.review-list-card {
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  background: linear-gradient(135deg, #ff6b6b 0%, #e66767 100%);
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
  margin-bottom: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
}

.rating-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #ff9900;
}

.empty-state {
  padding: 20px;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s;
}

.review-item:hover {
  border-color: #fa709a;
  box-shadow: 0 2px 8px rgba(250, 112, 154, 0.1);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.dish-info {
  display: flex;
  gap: 12px;
  flex: 1;
}

.dish-thumb {
  width: 50px;
  height: 50px;
  border-radius: 6px;
  object-fit: cover;
}

.dish-details {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.dish-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.order-id {
  font-size: 12px;
  color: #999;
}

.review-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
}

.review-images {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.review-image {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.2s;
}

.review-image:hover {
  transform: scale(1.1);
}

.review-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.review-time {
  font-size: 12px;
  color: #999;
}

.review-actions {
  display: flex;
  gap: 8px;
}
</style>
