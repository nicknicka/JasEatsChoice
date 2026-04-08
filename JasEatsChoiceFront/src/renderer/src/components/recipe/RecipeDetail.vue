<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Star } from '@element-plus/icons-vue'

// 接收props
const props = defineProps({
  visible: Boolean,
  recipe: Object
})

// 定义事件
const emit = defineEmits(['update:visible', 'update:recipe'])

// 关闭对话框
const closeDialog = () => {
  emit('update:visible', false)
}

// 切换收藏状态
const toggleFavorite = () => {
  if (props.recipe) {
    // 创建一个新的recipe对象
    const updatedRecipe = { ...props.recipe, favorite: !props.recipe.favorite }

    // 发射事件通知父组件更新
    emit('update:recipe', updatedRecipe)

    // 显示消息提示
    ElMessage.success(updatedRecipe.favorite ? '已添加到收藏' : '已取消收藏')
  }
}

// 获取标签类型
const getTagType = (type) => {
  switch (type) {
    case '早餐':
      return 'warning'
    case '午餐':
      return 'success'
    case '晚餐':
      return 'primary'
    case '加餐':
    case 'afternoon_tea':
    case 'tea':
      return 'info'
    case 'night_snack':
    case 'snack':
      return 'primary'
    default:
      return 'info'
  }
}

// 激活的菜品名称
const activeDishName = ref('')
</script>

<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="emit('update:visible', $event)"
    @close="emit('update:visible', false)"
    :title="recipe ? recipe.name : '食谱详情'"
    width="70%"
    :style="{ minWidth: '600px' }"
    top="8%"
    body-class="recipe-detail-dialog"
    draggable
  >
    <div class="recipe-detail-container">
      <div class="detail-header">
        <el-tag v-if="recipe" :type="getTagType(recipe.type)" size="large" class="type-tag">
          {{ recipe.type }}
        </el-tag>
        <el-icon
          v-if="recipe"
          :class="recipe.favorite ? 'favorite-icon active' : 'favorite-icon'"
          @click="toggleFavorite"
          title="点击切换收藏状态"
        >
          <Star />
        </el-icon>
      </div>

      <!-- 总卡路里 -->
      <el-card
        v-if="recipe"
        shadow="hover"
        class="stat-card"
        :body-style="{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'flex-start',
          justifyContent: 'center',
          gap: '16px',
          padding: '24px 32px',
          background: '#f8f9fa',
          borderRadius: '8px'
        }"
      >
        <div class="stat-label">🔥 <strong>总卡路里</strong></div>
        <div class="stat-text">
          <div class="stat-value">{{ recipe.calories }} kcal</div>
        </div>
      </el-card>

      <!-- 准备时间 -->
      <el-card
        v-if="recipe"
        shadow="hover"
        class="stat-card"
        :body-style="{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'flex-start',
          justifyContent: 'center',
          gap: '16px',
          padding: '24px 32px',
          background: '#f8f9fa',
          borderRadius: '8px'
        }"
      >
        <div class="stat-label">⏰ <strong>准备时间</strong></div>
        <div class="stat-text">
          <div class="stat-value">{{ recipe.time ? recipe.time : '00:00:00' }}（时:分:秒）</div>
        </div>
      </el-card>

      <!-- 食谱详情 -->
      <el-card
        v-if="recipe"
        shadow="hover"
        class="stat-card"
        :body-style="{
          background: '#f8f9fa',
          borderRadius: '8px'
        }"
      >
        <h4 class="section-title">
          <el-icon class="section-icon">📝</el-icon>
          食谱详情
        </h4>
        <div class="detail-content">
          {{ recipe.details || '这是一个健康美味的' + recipe.type + '食谱，营养均衡，味道鲜美。' }}
        </div>
      </el-card>

      <!-- 菜品组成 -->
      <el-card
        v-if="recipe"
        shadow="hover"
        class="stat-card"
        :body-style="{
          background: '#f8f9fa',
          borderRadius: '8px'
        }"
      >
        <h4 class="section-title">
          <el-icon class="section-icon">🍽️</el-icon>
          菜品组成
        </h4>
        <div class="dish-composition">
          <el-collapse v-model="activeDishName" accordion class="dish-collapse">
            <el-collapse-item
              v-for="(dish, index) in recipe.dishComposition || [
                { name: '空', ingredients: ['空'] }
              ]"
              :key="index"
              :title="dish.name"
              :name="dish.name"
              :class="{ 'empty-dish': dish.name === '空' }"
            >
              <div class="dish-ingredients">
                <el-tag
                  v-for="(ingredient, ingIdx) in dish.ingredients"
                  :key="ingIdx"
                  type="primary"
                  effect="plain"
                  size="small"
                >
                  {{ ingredient }}
                </el-tag>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
      </el-card>

      <!-- 主要食材 -->
      <el-card
        v-if="recipe"
        shadow="hover"
        class="stat-card"
        :body-style="{
          background: '#f8f9fa',
          borderRadius: '8px'
        }"
      >
        <h4 class="section-title">
          <el-icon class="section-icon">🥬</el-icon>
          主要食材
        </h4>
        <div class="ingredient-grid">
          <el-tag
            v-for="(ingredient, index) in recipe.ingredients || [
              '鸡蛋',
              '牛奶',
              '燕麦',
              '水果',
              '蜂蜜',
              '苹果',
              '香蕉'
            ]"
            :key="index"
            type="info"
            effect="light"
            size="large"
            class="ingredient-tag"
          >
            {{ ingredient }}
          </el-tag>
        </div>
      </el-card>

      <!-- 烹饪步骤 -->
      <el-card
        v-if="recipe"
        shadow="hover"
        class="stat-card"
        :body-style="{
          background: '#f8f9fa',
          borderRadius: '8px'
        }"
      >
        <h4 class="section-title">
          <el-icon class="section-icon">📋</el-icon>
          烹饪步骤
        </h4>
        <el-timeline class="cooking-steps">
          <el-timeline-item
            v-for="(step, index) in recipe.steps || [
              '这是一个健康美味的' + recipe.type + '食谱',
              '可以根据个人口味调整食材用量'
            ]"
            :key="index"
          >
            <el-card shadow="never" :border="false">
              {{ step }}
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="closeDialog">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';

.recipe-detail-container {
  text-align: left;
}

.stat-card {
  margin: @nordic-space-md 0;
  text-align: left;
  border-radius: @nordic-radius-md !important;
  border: 1px solid @nordic-border !important;
  box-shadow: none !important;

  :deep(.el-card__body) {
    background: @nordic-surface !important;
    border-radius: @nordic-radius-md !important;
  }
}

.stat-label {
  font-size: @nordic-text-md;
  font-weight: 600;
  color: @nordic-text;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-size: @nordic-text-md;
  font-weight: 600;
  color: @nordic-text;
  margin-bottom: @nordic-space-md;
  display: flex;
  align-items: center;
  gap: 8px;
  padding-left: @nordic-space-sm;
  border-left: 3px solid @nordic-accent;
}

.section-icon {
  font-size: @nordic-text-lg;
}

.ingredient-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.empty-dish {
  color: @nordic-text-muted;
  font-style: italic;
}

.detail-content {
  color: @nordic-text-secondary;
  line-height: 1.8;
  text-align: left;
}

.favorite-icon {
  font-size: 1.714rem;
  cursor: pointer;
  transition: all 0.3s ease;
  color: @nordic-border;

  &.active {
    color: @nordic-yellow;
    animation: pulse 0.5s ease;
  }

  &:hover {
    transform: scale(1.1);
  }
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.2); }
  100% { transform: scale(1); }
}
</style>
