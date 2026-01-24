<template>
  <div
    :class="['record-card', `record-card-${record.mealType}`]"
    @mouseenter="onMouseEnter"
    @mouseleave="onMouseLeave"
  >
    <div class="record-info">
      <div class="record-time">{{ record.time }}</div>
      <div class="record-content">
        <div class="food-name">{{ record.foodName }}</div>
        <div class="food-description" :title="record.description || '暂无描述信息'">
          {{ record.description || '暂无描述信息' }}
        </div>
        <div
          class="food-nutrition"
          v-if="record.calories || record.protein || record.fat || record.carbohydrate"
        >
          <el-tag size="small" type="danger">{{ record.calories }} kcal</el-tag>
          <el-tag size="small" type="info">蛋白 {{ record.protein || 0 }}g</el-tag>
          <el-tag size="small" type="warning">脂肪 {{ record.fat || 0 }}g</el-tag>
          <el-tag size="small" type="success">碳水 {{ record.carbohydrate || 0 }}g</el-tag>
        </div>
      </div>
    </div>
    <div class="record-actions" v-if="isHovered">
      <el-button type="text" size="small" @click="onEdit"> 编辑 </el-button>
      <el-button type="text" size="small" @click="onDelete"> 删除 </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue'

const props = defineProps({
  record: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['edit', 'delete'])

const isHovered = ref(false)

const onMouseEnter = () => {
  isHovered.value = true
}

const onMouseLeave = () => {
  isHovered.value = false
}

const onEdit = () => {
  emit('edit', props.record)
}

const onDelete = () => {
  emit('delete', props.record)
}
</script>

<style scoped>
/* 记录卡片 */
.record-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background-color: #f9fafb;
  border-radius: 12px;
  border-left: 4px solid #e5e7eb;
  transition: all 0.3s ease;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.record-card:hover {
  background-color: white;
  border-left-color: #667eea;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

/* 早餐卡片 */
.record-card-breakfast {
  border-left-color: #667eea;
}

/* 午餐卡片 */
.record-card-lunch {
  border-left-color: #f093fb;
}

/* 晚餐卡片 */
.record-card-dinner {
  border-left-color: #4facfe;
}

/* 加餐卡片 */
.record-card-snack {
  border-left-color: #43e97b;
}

.record-info {
  flex: 1;
  min-width: 0;
}

.record-time {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
  margin-bottom: 4px;
}

.food-name {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 2px;
}

.food-description {
  font-size: 14px;
  color: #6b7280;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 最多显示2行 */
  -webkit-box-orient: vertical;
  margin-bottom: 8px;
  line-height: 1.5; /* 调整行高，优化显示效果 */
  text-indent: 2em; /* 添加首行缩进 */
}

/* 营养信息 */
.food-nutrition {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.record-actions {
  display: flex;
  gap: 8px;
  margin-left: 24px;
  flex-shrink: 0;
}
</style>
