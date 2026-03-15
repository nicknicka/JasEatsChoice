<template>
  <div class="dish-list-card">
    <div class="card-header">
      <div class="header-title">
        <span class="icon">🍽️</span>
        <span class="title">菜品列表</span>
      </div>
      <div class="header-summary">{{ data.summary }}</div>
    </div>

    <div class="card-content">
      <!-- 空状态 -->
      <div v-if="!data.dishes || data.dishes.length === 0" class="empty-state">
        <el-empty description="没有找到相关菜品" />
      </div>

      <!-- 菜品列表 -->
      <div v-else class="dish-grid">
        <div
          v-for="dish in data.dishes"
          :key="dish.dishId"
          class="dish-item"
        >
          <div class="dish-image">
            <img :src="dish.imageUrl" :alt="dish.dishName" />
            <div class="dish-rating">
              <el-rate
                v-model="dish.rating"
                disabled
                show-score
                text-color="#ff9900"
                size="small"
              />
            </div>
          </div>

          <div class="dish-info">
            <div class="dish-name">{{ dish.dishName }}</div>
            <div class="dish-description">{{ dish.description }}</div>

            <div class="dish-meta">
              <span class="price">¥{{ dish.price }}</span>
              <el-tag
                v-if="dish.category"
                size="small"
                type="success"
              >
                {{ dish.category }}
              </el-tag>
            </div>

            <div class="dish-tags" v-if="dish.tags && dish.tags.length > 0">
              <el-tag
                v-for="(tag, index) in dish.tags"
                :key="index"
                size="small"
                type="info"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="dish-actions">
            <el-button
              v-for="action in dish.actions"
              :key="action.type"
              :type="action.type === 'add_to_cart' ? 'primary' : 'success'"
              :icon="getActionIcon(action.icon)"
              size="small"
              @click="handleAction(action.type, dish)"
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
const props = defineProps({
  data: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['action'])

// 获取操作图标
const getActionIcon = (iconName) => {
  const iconMap = {
    'ShoppingCart': 'ShoppingCart',
    'Star': 'Star'
  }
  return iconMap[iconName] || 'Operation'
}

// 处理操作
const handleAction = (actionType, dish) => {
  emit('action', {
    type: actionType,
    data: dish
  })
}
</script>

<style scoped>
.dish-list-card {
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
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

.empty-state {
  padding: 20px;
}

.dish-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.dish-item {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
}

.dish-item:hover {
  border-color: #fcb69f;
  box-shadow: 0 4px 12px rgba(252, 182, 159, 0.2);
  transform: translateY(-2px);
}

.dish-image {
  width: 100%;
  height: 160px;
  position: relative;
  overflow: hidden;
  background: #f5f5f5;
}

.dish-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.dish-rating {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(255, 255, 255, 0.95);
  padding: 4px 8px;
  border-radius: 12px;
  display: flex;
  align-items: center;
}

.dish-info {
  padding: 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.dish-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dish-description {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.dish-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.price {
  font-size: 20px;
  font-weight: 600;
  color: #f56c6c;
}

.dish-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.dish-actions {
  padding: 12px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 8px;
}

.dish-actions .el-button {
  flex: 1;
}
</style>
