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
      <div v-if="normalizedDishes.length === 0" class="empty-state">
        <el-empty description="没有找到相关菜品" />
      </div>

      <!-- 菜品列表 -->
      <div v-else class="dish-list">
        <div
          v-for="dish in normalizedDishes"
          :key="dish.dishId || dish.dishName || Math.random()"
          class="dish-item"
        >
          <!-- 左侧：图片 -->
          <div class="dish-image">
            <CommonImage
              :src="dish.imageUrl"
              :width="120"
              :height="120"
              fit="cover"
              :radius="8"
              error-icon="🍲"
              :show-error-text="false"
              :lazy="true"
            />
          </div>

          <!-- 右侧：信息 -->
          <div class="dish-info">
            <div class="dish-header">
              <div class="dish-name">{{ dish.dishName || '未命名菜品' }}</div>
              <div class="dish-rating" v-if="hasValue(dish.rating)">
                <el-rate
                  v-model="dish.rating"
                  disabled
                  show-score
                  text-color="#ff9900"
                  size="small"
                />
              </div>
            </div>

            <!-- 描述（可选） -->
            <div v-if="hasValue(dish.description)" class="dish-description">
              {{ dish.description }}
            </div>

            <!-- 价格和分类（都可选） -->
            <div class="dish-meta" v-if="hasValue(dish.price) || hasValue(dish.category)">
              <span v-if="hasValue(dish.price)" class="price">
                ¥{{ dish.price }}
              </span>
              <el-tag
                v-if="hasValue(dish.category)"
                size="small"
                type="success"
              >
                {{ dish.category }}
              </el-tag>
            </div>

            <!-- 标签（可选） -->
            <div class="dish-tags" v-if="hasValue(dish.tags)">
              <el-tag
                v-for="(tag, index) in dish.tags"
                :key="index"
                size="small"
                type="info"
              >
                {{ tag }}
              </el-tag>
            </div>

            <!-- 操作按钮（可选） -->
            <div class="dish-actions" v-if="hasValue(dish.actions)">
              <el-button
                v-for="action in dish.actions"
                :key="action.type"
                :type="action.type === 'add_to_cart' ? 'primary' : 'success'"
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
  </div>
</template>

<script setup>
import { computed } from 'vue'
import CommonImage from '@/components/CommonImage.vue'

const props = defineProps({
  data: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['action'])

// ========== 公共工具方法 ==========

/**
 * 检查值是否有意义（非 null、非 undefined、非空字符串、非空数组）
 */
const hasValue = (value) => {
  if (value == null) return false
  if (typeof value === 'string' && value.trim() === '') return false
  if (Array.isArray(value) && value.length === 0) return false
  return true
}

// ========== 数据标准化 ==========

const normalizedDishes = computed(() => {
  if (!props.data) return []

  if (props.data.dishes && Array.isArray(props.data.dishes)) {
    return props.data.dishes
  }

  if (props.data.recommendations && Array.isArray(props.data.recommendations)) {
    return props.data.recommendations.map(rec => ({
      dishId: rec.dishId || rec.id,
      dishName: rec.dishName || rec.name || '未命名菜品',
      imageUrl: rec.imageUrl || rec.image || null,
      description: rec.description || rec.recommendReason || rec.highlight || null,
      price: rec.price !== undefined ? rec.price : null,
      rating: rec.rating || null,
      category: rec.category || null,
      tags: rec.tags || [],
      actions: rec.actions || [],
      _original: rec
    }))
  }

  if (Array.isArray(props.data)) {
    return props.data
  }

  console.warn('[DishListCard] 无法识别数据格式:', Object.keys(props.data))
  return []
})

// ========== 原有功能 ==========

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
  background: linear-gradient(135deg, #ff6b6b 0%, #ff9f43 100%);
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

.dish-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dish-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  background: white;
  transition: all 0.3s;
}

.dish-item:hover {
  border-color: #ff9f43;
  box-shadow: 0 4px 12px rgba(255, 159, 67, 0.15);
  transform: translateX(4px);
}

.dish-image {
  flex-shrink: 0;
  width: 120px;
  height: 120px;
  overflow: hidden;
  border-radius: 8px;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0; /* 防止内容溢出 */
}

.dish-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.dish-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  flex: 1;
  line-height: 1.4;
}

.dish-rating {
  flex-shrink: 0;
}

.dish-description {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.dish-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.price {
  font-size: 22px;
  font-weight: 600;
  color: #f56c6c;
}

.dish-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.dish-actions {
  display: flex;
  gap: 8px;
  margin-top: auto;
}

.dish-actions .el-button {
  flex-shrink: 0;
}
</style>
