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
              <div class="dish-rating" v-if="dish.rating">
                <el-rate
                  v-model="dish.rating"
                  disabled
                  show-score
                  text-color="#ff9900"
                  size="small"
                />
              </div>
            </div>

            <div class="dish-description">
              {{ dish.description || '暂无描述' }}
            </div>

            <div class="dish-meta">
              <span class="price">
                {{ dish.price !== undefined ? `¥${dish.price}` : '价格未知' }}
              </span>
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

            <!-- 操作按钮 -->
            <div class="dish-actions" v-if="dish.actions && dish.actions.length > 0">
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
import { watch, onMounted, computed } from 'vue'
import CommonImage from '@/components/CommonImage.vue'

// 检测开发模式
const isDevelopment = computed(() => {
  return import.meta.env.MODE === 'development' ||
         import.meta.env.DEV ||
         window.location.hostname === 'localhost'
})

const props = defineProps({
  data: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['action'])

// ========== 数据标准化 ==========
// 注意：必须定义在 validateAndLogData 之前，因为函数中会使用

// 计算属性：标准化菜品数据（兼容多种数据格式）
const normalizedDishes = computed(() => {
  if (!props.data) return []

  // 方案1: 使用 dishes 字段（标准格式）
  if (props.data.dishes && Array.isArray(props.data.dishes)) {
    console.log('✅ [DishListCard] 使用 dishes 字段')
    return props.data.dishes
  }

  // 方案2: 使用 recommendations 字段（推荐格式）
  if (props.data.recommendations && Array.isArray(props.data.recommendations)) {
    console.log('✅ [DishListCard] 使用 recommendations 字段，进行字段映射')
    return props.data.recommendations.map(rec => ({
      dishId: rec.dishId || rec.id,
      dishName: rec.dishName || rec.name || '未命名菜品',
      imageUrl: rec.imageUrl || rec.image || '',
      description: rec.description || rec.recommendReason || rec.highlight || '暂无描述',
      price: rec.price,
      rating: rec.rating,
      category: rec.category,
      tags: rec.tags || [],
      actions: rec.actions || [], // 不自动添加默认按钮
      // 保留原始字段
      _original: rec
    }))
  }

  // 方案3: 数据本身是数组
  if (Array.isArray(props.data)) {
    console.log('✅ [DishListCard] 数据本身是数组')
    return props.data
  }

  console.warn('⚠️ [DishListCard] 无法识别数据格式:', props.data)
  return []
})

// ========== 调试日志 ==========

// 数据验证和日志函数（必须定义在 watch 之前）
const validateAndLogData = () => {
  console.log('🔍 [DishListCard] 开始数据验证')

  // 检查1: data是否存在
  if (!props.data) {
    console.error('❌ [DishListCard] 错误: props.data 为空')
    return
  }
  console.log('✅ [DishListCard] data 存在')

  // 检查2: 可用字段
  console.log('📋 [DishListCard] 原始数据字段:', Object.keys(props.data))
  console.log('📋 [DishListCard] 包含:', {
    hasDishes: !!props.data.dishes,
    hasRecommendations: !!props.data.recommendations,
    isDataArray: Array.isArray(props.data)
  })

  // 检查3: 标准化后的数据
  const dishes = normalizedDishes.value
  console.log('📋 [DishListCard] 标准化后:', {
    length: dishes.length,
    firstItem: dishes[0] || null
  })

  if (dishes.length === 0) {
    console.warn('⚠️ [DishListCard] 警告: 标准化后数组为空（将显示空状态）')
    return
  }

  console.log(`✅ [DishListCard] 标准化后包含 ${dishes.length} 个菜品`)

  // 检查4: 遍历每个菜品
  dishes.forEach((dish, index) => {
    console.log(`🍲 [DishListCard] 菜品 #${index + 1}:`, {
      dishId: dish.dishId,
      dishName: dish.dishName,
      hasImage: !!dish.imageUrl,
      imageUrl: dish.imageUrl,
      price: dish.price,
      rating: dish.rating,
      hasActions: !!dish.actions && dish.actions.length > 0,
      actionsCount: dish.actions ? dish.actions.length : 0
    })

    // 检查必填字段（仅在开发模式警告）
    if (isDevelopment.value) {
      if (!dish.dishId) {
        console.warn(`⚠️ [DishListCard] 菜品 #${index + 1} 缺少 dishId（AI生成数据可能不包含此字段）`)
      }
      if (!dish.dishName) {
        console.warn(`⚠️ [DishListCard] 菜品 #${index + 1} 缺少 dishName`)
      }
      if (!dish.imageUrl) {
        console.warn(`⚠️ [DishListCard] 菜品 #${index + 1} 缺少 imageUrl（AI生成数据可能不包含此字段）`)
      }
    }
  })

  // 检查5: summary字段（仅在开发模式警告）
  if (props.data.summary) {
    console.log('📝 [DishListCard] summary:', props.data.summary)
  } else if (isDevelopment.value) {
    console.warn('⚠️ [DishListCard] 缺少 summary 字段')
  }

  console.log('✅ [DishListCard] 数据验证完成')
}

// 组件挂载时的日志
onMounted(() => {
  console.log('🍽️ [DishListCard] 组件已挂载')
  console.log('📊 [DishListCard] 接收到的数据:', {
    rawData: props.data,
    hasData: !!props.data,
    dataType: typeof props.data,
    keys: props.data ? Object.keys(props.data) : []
  })
  validateAndLogData()
})

// 监听数据变化
watch(() => props.data, (newData, oldData) => {
  console.log('🔄 [DishListCard] 数据变化')
  console.log('📥 [DishListCard] 新数据:', newData)
  console.log('📤 [DishListCard] 旧数据:', oldData)
  validateAndLogData()
}, { deep: true, immediate: true })

// 计算属性：用于调试
const debugInfo = computed(() => {
  return {
    hasData: !!props.data,
    hasDishes: !!props.data?.dishes,
    hasRecommendations: !!props.data?.recommendations,
    dishesCount: normalizedDishes.value.length,
    isEmpty: normalizedDishes.value.length === 0,
    firstDish: normalizedDishes.value[0] || null
  }
})

// 监听调试信息
watch(debugInfo, (info) => {
  console.log('🐛 [DishListCard] 调试信息:', info)
}, { immediate: true })

// ========== 原有功能 ==========

// 处理操作
const handleAction = (actionType, dish) => {
  console.log('🎯 [DishListCard] 卡片操作触发:', {
    actionType,
    dishId: dish.dishId,
    dishName: dish.dishName,
    fullDish: dish
  })

  emit('action', {
    type: actionType,
    data: dish
  })

  console.log('✅ [DishListCard] 事件已发送到父组件')
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
  border-color: #fcb69f;
  box-shadow: 0 4px 12px rgba(252, 182, 159, 0.15);
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
