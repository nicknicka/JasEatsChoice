<template>
  <div class="favorite-list-card">
    <div class="card-header">
      <div class="header-title">
        <span class="icon">⭐</span>
        <span class="title">我的收藏</span>
      </div>
      <div class="header-summary">{{ data.summary }}</div>
    </div>

    <div class="card-content">
      <!-- 空状态 -->
      <div v-if="!data.favorites || data.favorites.length === 0" class="empty-state">
        <el-empty description="您还没有收藏任何菜品" />
      </div>

      <!-- 收藏列表 -->
      <div v-else class="favorite-grid">
        <div
          v-for="favorite in data.favorites"
          :key="favorite.dishId"
          class="favorite-item"
        >
          <div class="dish-image">
            <img :src="favorite.imageUrl" :alt="favorite.dishName" />
          </div>

          <div class="dish-info">
            <div class="dish-name">{{ favorite.dishName }}</div>
            <div class="dish-meta">
              <el-rate
                v-model="favorite.rating"
                disabled
                show-score
                text-color="#ff9900"
              />
              <span class="price">¥{{ favorite.price }}</span>
            </div>
            <div class="dish-tags">
              <el-tag
                v-for="(tag, index) in favorite.tags"
                :key="index"
                size="small"
                type="info"
              >
                {{ tag }}
              </el-tag>
            </div>
            <div class="collection-time">
              收藏于 {{ favorite.collectionTime }}
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="dish-actions">
            <el-button
              v-for="action in favorite.actions"
              :key="action.type"
              :type="action.type === 'add_to_cart' ? 'primary' : 'danger'"
              :icon="getActionIcon(action.icon)"
              size="small"
              @click="handleAction(action.type, favorite)"
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
    'Delete': 'Delete'
  }
  return iconMap[iconName] || 'Operation'
}

// 处理操作
const handleAction = (actionType, favorite) => {
  emit('action', {
    type: actionType,
    data: favorite
  })
}
</script>

<style scoped>
.favorite-list-card {
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  background: linear-gradient(135deg, #ff6b6b 0%, #f78fb3 100%);
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

.favorite-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.favorite-item {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
}

.favorite-item:hover {
  border-color: #f5576c;
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.15);
  transform: translateY(-2px);
}

.dish-image {
  width: 100%;
  height: 140px;
  overflow: hidden;
  background: #f5f5f5;
}

.dish-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dish-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.price {
  font-size: 18px;
  font-weight: 600;
  color: #f56c6c;
}

.dish-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.collection-time {
  font-size: 12px;
  color: #999;
  margin-top: auto;
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
