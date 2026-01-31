<template>
  <div class="festival-recommend-card" v-if="festival">
    <!-- 节日头部 -->
    <div class="festival-header" :style="{ background: festival.themeColor || '#FF6B6B' }">
      <div class="festival-icon">{{ festival.icon }}</div>
      <div class="festival-info">
        <h3 class="festival-name">{{ festival.festivalName }}</h3>
        <p class="festival-desc">{{ festival.description }}</p>
        <div class="festival-tags">
          <el-tag v-if="festival.isCurrent" type="success" size="small">进行中</el-tag>
          <el-tag v-else-if="festival.daysUntilFestival !== null" type="info" size="small">
            {{ festival.daysUntilFestival }}天后
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 推荐菜品列表 -->
    <div class="dish-list" v-if="festival.recommendDishes && festival.recommendDishes.length > 0">
      <div
        class="dish-item"
        v-for="dish in festival.recommendDishes"
        :key="dish.dishId"
        @click="handleDishClick(dish)"
      >
        <div class="dish-image">
          <img :src="dish.dishImage || '/default-dish.png'" :alt="dish.dishName" />
          <div class="dish-badge" :class="getRecommendTypeClass(dish.recommendType)">
            {{ getRecommendTypeText(dish.recommendType) }}
          </div>
        </div>
        <div class="dish-info">
          <h4 class="dish-name">{{ dish.dishName }}</h4>
          <p class="dish-reason" v-if="dish.recommendReason">{{ dish.recommendReason }}</p>
          <div class="dish-footer">
            <span class="dish-price">¥{{ dish.dishPrice }}</span>
            <el-button type="text" size="small" @click.stop="handleAddToCart(dish)">
              加入购物车
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-else description="暂无推荐菜品" :image-size="80" />
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  festival: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['dish-click', 'add-to-cart', 'feedback'])

const getRecommendTypeClass = (type) => {
  const classMap = {
    'MAIN': 'badge-main',
    'SECONDARY': 'badge-secondary',
    'THEME': 'badge-theme',
    'SEASONAL': 'badge-seasonal'
  }
  return classMap[type] || 'badge-default'
}

const getRecommendTypeText = (type) => {
  const textMap = {
    'MAIN': '主推',
    'SECONDARY': '推荐',
    'THEME': '主题',
    'SEASONAL': '季节'
  }
  return textMap[type] || '推荐'
}

const handleDishClick = (dish) => {
  // 记录点击
  emit('dish-click', dish)
  // 记录反馈
  emit('feedback', {
    recommendHistoryId: dish.recommendHistoryId,
    isClicked: true
  })
}

const handleAddToCart = (dish) => {
  emit('add-to-cart', dish)
}
</script>

<style scoped>
.festival-recommend-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
}

.festival-header {
  display: flex;
  align-items: center;
  padding: 20px;
  color: #fff;
}

.festival-icon {
  font-size: 48px;
  margin-right: 16px;
}

.festival-info {
  flex: 1;
}

.festival-name {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: bold;
}

.festival-desc {
  margin: 0 0 12px 0;
  font-size: 14px;
  opacity: 0.9;
}

.festival-tags {
  display: flex;
  gap: 8px;
}

.dish-list {
  padding: 16px;
}

.dish-item {
  display: flex;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  margin-bottom: 12px;
}

.dish-item:hover {
  background: #f5f7fa;
}

.dish-item:last-child {
  margin-bottom: 0;
}

.dish-image {
  position: relative;
  width: 100px;
  height: 100px;
  margin-right: 12px;
  flex-shrink: 0;
}

.dish-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.dish-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  color: #fff;
}

.badge-main {
  background: #f56c6c;
}

.badge-secondary {
  background: #e6a23c;
}

.badge-theme {
  background: #409eff;
}

.badge-seasonal {
  background: #67c23a;
}

.badge-default {
  background: #909399;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.dish-name {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.dish-reason {
  margin: 0 0 8px 0;
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}

.dish-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dish-price {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}
</style>
