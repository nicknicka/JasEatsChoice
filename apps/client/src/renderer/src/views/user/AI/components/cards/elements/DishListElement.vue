<template>
  <div class="dish-list-element">
    <!-- 空状态 -->
    <div v-if="dishes.length === 0" class="empty-state">
      <el-empty description="没有找到相关菜品" />
    </div>

    <!-- 菜品列表 -->
    <div v-else class="dish-list">
      <div
        v-for="dish in dishes"
        :key="dish.dishId || dish.dishName"
        class="dish-item"
      >
        <!-- 左侧：图片 -->
        <div class="dish-image">
          <CommonImage
            :src="dish.imageUrl"
            :width="80"
            :height="80"
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
            <div class="dish-name">
              {{ dish.dishName || '未命名菜品' }}
              <span v-if="dish.isRecommended" class="recommend-pill">AI推荐</span>
            </div>
            <div class="dish-rating" v-if="hasValue(dish.rating)">
              <el-rate
                :model-value="dish.rating"
                disabled
                show-score
                text-color="#ff9900"
                size="small"
              />
            </div>
          </div>

          <!-- 描述 -->
          <div v-if="hasValue(dish.description)" class="dish-description">
            {{ dish.description }}
          </div>

          <!-- 价格和分类 -->
          <div class="dish-meta" v-if="hasValue(dish.price) || hasValue(dish.category)">
            <span v-if="hasValue(dish.price)" class="price">
              ¥{{ dish.price }}
            </span>
            <span v-if="hasValue(dish.category)" class="category-pill">
              {{ dish.category }}
            </span>
          </div>

          <!-- 标签 -->
          <div class="dish-tags" v-if="hasValue(dish.tags)">
            <span
              v-for="(tag, index) in dish.tags"
              :key="index"
              class="tag-pill"
            >
              {{ tag }}
            </span>
          </div>

          <!-- 操作按钮 -->
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
</template>

<script setup>
import { computed } from 'vue'
import CommonImage from '@/components/CommonImage.vue'

const props = defineProps({
  element: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['action'])

// 菜品列表
const dishes = computed(() => props.element.dishes || [])

/**
 * 检查值是否有意义（非 null、非 undefined、非空字符串、非空数组）
 * @param {*} value
 * @returns {boolean}
 */
const hasValue = (value) => {
  if (value == null) return false
  if (typeof value === 'string' && value.trim() === '') return false
  if (Array.isArray(value) && value.length === 0) return false
  return true
}

/**
 * 处理菜品操作
 * @param {string} actionType - 操作类型
 * @param {Object} dish - 菜品数据
 */
const handleAction = (actionType, dish) => {
  emit('action', {
    type: actionType,
    data: dish
  })
}
</script>

<style scoped>
.dish-list-element {
  width: 100%;
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
  gap: 12px;
  padding: 12px;
  border: 1px solid #f0f2f5;
  border-radius: 14px;
  background: white;
  transition: all 0.2s ease;
}

.dish-item:hover {
  border-color: rgba(255, 159, 67, 0.35);
  box-shadow: 0 4px 12px rgba(255, 159, 67, 0.12);
  transform: translateY(2px);
}

.dish-image {
  flex-shrink: 0;
  width: 80px;
  height: 80px;
  overflow: hidden;
  border-radius: 10px;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.dish-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
}

.dish-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  flex: 1;
  line-height: 1.4;
  display: flex;
  align-items: center;
  gap: 6px;
}

.recommend-pill {
  display: inline-flex;
  align-items: center;
  padding: 1px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 500;
  color: white;
  background: linear-gradient(135deg, #667eea, #764ba2);
  flex-shrink: 0;
  letter-spacing: 0.3px;
}

.dish-rating {
  flex-shrink: 0;
}

.dish-description {
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.dish-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.price {
  font-size: 18px;
  font-weight: 600;
  color: #ff6347;
}

.category-pill {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 500;
  color: #38b26a;
  background: rgba(86, 212, 143, 0.12);
}

.dish-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tag-pill {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 400;
  color: #64748b;
  background: #f0f2f5;
  letter-spacing: 0.2px;
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
