<template>
  <el-dialog v-model="visible" title="菜品详情" width="600px" @close="handleClose">
    <div v-if="dish" class="dish-detail-container">
      <!-- 菜品头部 -->
      <div class="dish-header">
        <div class="dish-image-wrapper">
          <img v-if="dish.image" :src="dish.image" :alt="dish.name" class="dish-image" />
          <div v-else class="dish-image-placeholder">
            <el-icon :size="80"><Food /></el-icon>
          </div>
        </div>
        <div class="dish-basic-info">
          <h2 class="dish-name">{{ dish.name || '未知菜品' }}</h2>
          <el-tag v-if="dish.category" type="success" size="large" class="dish-category">
            {{ dish.category }}
          </el-tag>
          <div class="dish-price">¥{{ (dish.price || 0).toFixed(2) }}</div>
        </div>
      </div>

      <el-divider />

      <!-- 菜品描述 -->
      <div v-if="dish.description" class="dish-section">
        <h3 class="section-title">
          <el-icon><Document /></el-icon>
          菜品介绍
        </h3>
        <p class="dish-description">{{ dish.description }}</p>
      </div>

      <!-- 必选食材 -->
      <div
        v-if="dish.requiredIngredients && dish.requiredIngredients.length > 0"
        class="dish-section"
      >
        <h3 class="section-title">
          <el-icon><CircleCheck /></el-icon>
          必选食材
        </h3>
        <div class="ingredients-list">
          <el-tag
            v-for="(ingredient, index) in dish.requiredIngredients"
            :key="index"
            type="primary"
            effect="plain"
            size="large"
            class="ingredient-tag"
          >
            {{ ingredient }}
          </el-tag>
        </div>
      </div>

      <!-- 可选食材 -->
      <div
        v-if="dish.optionalIngredients && dish.optionalIngredients.length > 0"
        class="dish-section"
      >
        <h3 class="section-title">
          <el-icon><Plus /></el-icon>
          可选食材
        </h3>
        <div class="ingredients-list">
          <el-tag
            v-for="(ingredient, index) in dish.optionalIngredients"
            :key="index"
            type="info"
            effect="plain"
            size="large"
            class="ingredient-tag"
          >
            {{ typeof ingredient === 'string' ? ingredient : ingredient.name }}
            <span v-if="typeof ingredient === 'object' && ingredient.price">
              (+¥{{ ingredient.price.toFixed(2) }})
            </span>
          </el-tag>
        </div>
      </div>

      <!-- 营养信息 -->
      <div
        v-if="dish.calories || dish.protein || dish.fat || dish.carbs"
        class="dish-section nutrition-info"
      >
        <h3 class="section-title">
          <el-icon><TrendCharts /></el-icon>
          营养信息
        </h3>
        <div class="nutrition-grid">
          <div v-if="dish.calories" class="nutrition-item">
            <span class="nutrition-label">热量</span>
            <span class="nutrition-value">{{ dish.calories }} kcal</span>
          </div>
          <div v-if="dish.protein" class="nutrition-item">
            <span class="nutrition-label">蛋白质</span>
            <span class="nutrition-value">{{ dish.protein }}g</span>
          </div>
          <div v-if="dish.fat" class="nutrition-item">
            <span class="nutrition-label">脂肪</span>
            <span class="nutrition-value">{{ dish.fat }}g</span>
          </div>
          <div v-if="dish.carbs" class="nutrition-item">
            <span class="nutrition-label">碳水</span>
            <span class="nutrition-value">{{ dish.carbs }}g</span>
          </div>
        </div>
      </div>

      <!-- 所属商家 -->
      <div v-if="dish.merchantName" class="dish-section">
        <h3 class="section-title">
          <el-icon><Shop /></el-icon>
          所属商家
        </h3>
        <div class="merchant-info">
          <span class="merchant-name">{{ dish.merchantName }}</span>
          <el-button type="primary" size="small" @click="goToMerchant"> 查看商家 </el-button>
        </div>
      </div>
    </div>

    <div v-else class="loading-state">
      <el-icon class="is-loading"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button v-if="dish && dish.merchantId" type="primary" @click="orderDish">
          立即订购
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Food,
  Document,
  CircleCheck,
  Plus,
  TrendCharts,
  Shop,
  Loading
} from '@element-plus/icons-vue'
import axios from 'axios'
import { API_CONFIG } from '../../config/index.js'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  dishId: {
    type: [String, Number],
    default: null
  },
  dishData: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue'])

const router = useRouter()
const visible = ref(props.modelValue)
const dish = ref(props.dishData)
const loading = ref(false)

// 监听 modelValue 变化
watch(
  () => props.modelValue,
  (newVal) => {
    visible.value = newVal
    if (newVal && props.dishId) {
      loadDishDetail()
    }
  }
)

// 监听 visible 变化
watch(visible, (newVal) => {
  emit('update:modelValue', newVal)
})

// 加载菜品详情
const loadDishDetail = async () => {
  if (!props.dishId || props.dishData) {
    // 如果已经有菜品数据,直接使用
    if (props.dishData) {
      dish.value = props.dishData
    }
    return
  }

  loading.value = true
  try {
    // 从商家菜单中获取菜品详情
    // 注意:这里需要根据实际的API结构进行调整
    const response = await axios.get(`${API_CONFIG.baseURL}/v1/dishes/${props.dishId}`)
    if (response.data && response.data.code === '200') {
      dish.value = response.data.data
    } else {
      ElMessage.error('加载菜品详情失败')
    }
  } catch (error) {
    console.error('加载菜品详情失败:', error)
    ElMessage.error('加载菜品详情失败,请稍后重试')
  } finally {
    loading.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
}

// 跳转到商家页面
const goToMerchant = () => {
  if (dish.value && dish.value.merchantId) {
    // 存储商家信息到sessionStorage
    sessionStorage.setItem(
      'selectedMerchant',
      JSON.stringify({
        id: dish.value.merchantId,
        name: dish.value.merchantName
      })
    )
    router.push({
      path: '/user/home/merchant-detail',
      query: { id: dish.value.merchantId }
    })
    handleClose()
  }
}

// 立即订购
const orderDish = () => {
  if (dish.value && dish.value.merchantId) {
    // 存储商家信息到sessionStorage
    sessionStorage.setItem(
      'selectedMerchant',
      JSON.stringify({
        id: dish.value.merchantId,
        name: dish.value.merchantName
      })
    )
    router.push({
      path: '/user/home/merchant-detail',
      query: {
        id: dish.value.merchantId,
        dishId: dish.value.id
      }
    })
    handleClose()
  }
}
</script>

<style scoped lang="less">
.dish-detail-container {
  .dish-header {
    display: flex;
    gap: 20px;
    margin-bottom: 20px;

    .dish-image-wrapper {
      flex-shrink: 0;
      width: 200px;
      height: 200px;
      border-radius: 12px;
      overflow: hidden;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

      .dish-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .dish-image-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: rgba(255, 255, 255, 0.8);
      }
    }

    .dish-basic-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: center;
      gap: 12px;

      .dish-name {
        font-size: 1.714rem /* 原值: 24px */;
        font-weight: 700;
        color: #2c3e50;
        margin: 0;
      }

      .dish-category {
        width: fit-content;
      }

      .dish-price {
        font-size: 2.286rem /* 原值: 32px */;
        font-weight: 700;
        color: #ff6b6b;
      }
    }
  }

  .dish-section {
    margin-bottom: 24px;

    .section-title {
      font-size: 1.286rem /* 原值: 18px */;
      font-weight: 600;
      color: #2c3e50;
      margin-bottom: 12px;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .dish-description {
      font-size: 1rem /* 原值: 14px */;
      color: #666;
      line-height: 1.8;
      margin: 0;
    }

    .ingredients-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .ingredient-tag {
        font-size: 1rem /* 原值: 14px */;
      }
    }

    .nutrition-info {
      background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
      padding: 16px;
      border-radius: 8px;

      .nutrition-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
        gap: 16px;

        .nutrition-item {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 4px;

          .nutrition-label {
            font-size: 0.857rem /* 原值: 12px */;
            color: #666;
          }

          .nutrition-value {
            font-size: 1.143rem /* 原值: 16px */;
            font-weight: 600;
            color: #2c3e50;
          }
        }
      }
    }

    .merchant-info {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px;
      background: #f8f9fa;
      border-radius: 8px;

      .merchant-name {
        font-size: 1.143rem /* 原值: 16px */;
        font-weight: 500;
        color: #2c3e50;
      }
    }
  }
}

.loading-state {
  text-align: center;
  padding: 40px 0;
  color: #999;

  .el-icon {
    font-size: 3.429rem /* 原值: 48px */;
    margin-bottom: 16px;
  }

  p {
    margin: 0;
    font-size: 1rem /* 原值: 14px */;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
