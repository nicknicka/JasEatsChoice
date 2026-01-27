<template>
  <el-dialog
    v-model="visible"
    title="加菜"
    width="70%"
    @close="handleClose"
    :close-on-click-modal="false"
  >
    <!-- 订单进度 -->
    <div class="order-progress">
      <h4>订单进度</h4>
      <el-steps :active="currentStep" finish-status="success" simple>
        <el-step title="待接单"></el-step>
        <el-step title="备菜中"></el-step>
        <el-step title="烹饪中"></el-step>
        <el-step title="待上菜"></el-step>
      </el-steps>
    </div>

    <!-- 已点菜品清单 -->
    <div class="ordered-dishes">
      <h4>已点菜品</h4>
      <el-table :data="orderedDishes" max-height="200">
        <el-table-column prop="dishName" label="菜品">
          <template #default="{ row }">
            <span v-if="row.isAddDish" class="add-dish-tag">
              {{ ADD_DISH_PREFIX }}
            </span>
            {{ row.dishName }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80"></el-table-column>
        <el-table-column prop="price" label="单价" width="100">
          <template #default="{ row }">
            ¥{{ row.price?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="addDishUser" label="加菜人" width="120">
          <template #default="{ row }">
            {{ row.isAddDish ? row.addDishUser : '-' }}
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 饮食禁忌警告 -->
    <div class="allergy-warning" v-if="allergyConflicts.length > 0">
      <el-alert type="error" :closable="false">
        <template #title>
          <div class="conflict-content">
            <span>以下成员有饮食禁忌冲突：</span>
            <el-tag
              v-for="user in allergyConflicts"
              :key="user.userId"
              type="danger"
              size="small"
              style="margin-left: 8px"
            >
              {{ user.nickname }}: {{ user.allergies.join(', ') }}
            </el-tag>
          </div>
        </template>
      </el-alert>
    </div>

    <!-- 菜品选择区域 -->
    <div class="dish-selection">
      <h4>选择加菜菜品</h4>
      <div class="dish-list">
        <div
          v-for="dish in availableDishes"
          :key="dish.id"
          class="dish-item"
          :class="{ selected: isDishSelected(dish.id) }"
        >
          <div class="dish-info">
            <img v-if="dish.image" :src="dish.image" class="dish-image" />
            <div class="dish-no-image" v-else>
              <el-icon :size="32"><Food /></el-icon>
            </div>
            <div class="dish-details">
              <div class="dish-name">{{ dish.name }}</div>
              <div class="dish-price">¥{{ dish.price?.toFixed(2) || '0.00' }}</div>

              <!-- 食材组成 -->
              <div class="dish-ingredients">
                <!-- 必选食材 -->
                <div
                  class="ingredient-section required"
                  v-if="dish.requiredIngredients && dish.requiredIngredients.length > 0"
                >
                  <span class="ingredient-title">必选:</span>
                  <div class="ingredient-list">
                    <span
                      class="ingredient-item"
                      v-for="ingredient in dish.requiredIngredients"
                      :key="ingredient"
                    >
                      {{ ingredient }}
                    </span>
                  </div>
                </div>

                <!-- 可选食材 -->
                <div
                  class="ingredient-section optional"
                  v-if="dish.optionalIngredients && dish.optionalIngredients.length > 0"
                >
                  <span class="ingredient-title">可选:</span>
                  <div class="ingredient-list">
                    <el-checkbox
                      v-for="ingredient in dish.optionalIngredients"
                      :key="ingredient.id || ingredient.name"
                      v-model="ingredient.selected"
                      size="small"
                      class="ingredient-checkbox"
                    >
                      {{ ingredient.name }}
                      <span class="ingredient-price" v-if="ingredient.price">
                        (+¥{{ ingredient.price.toFixed(2) }})
                      </span>
                    </el-checkbox>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="dish-actions">
            <div class="dish-total-price" v-if="isDishSelected(dish.id)">
              ¥{{ calculateDishPrice(dish).toFixed(2) }}
            </div>
            <el-input-number
              v-model="dish.quantity"
              :min="1"
              :max="99"
              size="small"
              v-if="isDishSelected(dish.id)"
            />
            <el-button
              type="primary"
              size="small"
              @click="toggleDish(dish)"
            >
              {{ isDishSelected(dish.id) ? '取消' : '添加' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 备注 -->
    <div class="remark-section">
      <el-input
        v-model="remark"
        type="textarea"
        placeholder="请输入加菜备注（可选）"
        :rows="2"
        maxlength="200"
        show-word-limit
      />
    </div>

    <!-- 底部统计 -->
    <template #footer>
      <div class="footer-content">
        <div class="total-info">
          <span>已选 {{ selectedDishes.length }} 道菜</span>
          <span class="total-amount">累计金额: ¥{{ totalAmount.toFixed(2) }}</span>
        </div>
        <el-button type="primary" @click="handleSubmit" :disabled="selectedDishes.length === 0">
          提交加菜请求
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Food } from '@element-plus/icons-vue'
import { ADD_DISH_CONFIG } from '@/constants/addDishConstants'
import addDishApi from '@/api/addDish'

const props = defineProps({
  modelValue: Boolean,
  groupOrderId: [String, Number],
  orderedDishes: {
    type: Array,
    default: () => []
  },
  availableDishes: {
    type: Array,
    default: () => []
  },
  allergyConflicts: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const currentStep = ref(1)
const selectedDishes = ref([])
const remark = ref('')

const ADD_DISH_PREFIX = ADD_DISH_CONFIG.ADD_DISH_PREFIX

// 计算单个菜品价格(包含可选食材)
const calculateDishPrice = (dish) => {
  if (!dish) return 0
  const basePrice = dish.price || 0
  const optionalPrice = (dish.optionalIngredients || []).reduce((sum, ingredient) => {
    return sum + (ingredient.selected ? (ingredient.price || 0) : 0)
  }, 0)
  return basePrice + optionalPrice
}

const totalAmount = computed(() => {
  return selectedDishes.value.reduce((sum, dish) => {
    const dishPrice = calculateDishPrice(dish)
    return sum + dishPrice * (dish.quantity || 1)
  }, 0)
})

const isDishSelected = (dishId) => {
  return selectedDishes.value.some(d => d.id === dishId)
}

const toggleDish = (dish) => {
  const index = selectedDishes.value.findIndex(d => d.id === dish.id)
  if (index > -1) {
    selectedDishes.value.splice(index, 1)
  } else {
    selectedDishes.value.push({
      ...dish,
      quantity: 1
    })
  }
}

const handleClose = () => {
  selectedDishes.value = []
  remark.value = ''
}

const handleSubmit = async () => {
  try {
    const dishItems = selectedDishes.value.map(dish => {
      // 获取选中的可选食材
      const selectedOptional = (dish.optionalIngredients || [])
        .filter(ing => ing.selected)
        .map(ing => ({
          id: ing.id,
          name: ing.name,
          price: ing.price
        }))

      return {
        dishId: dish.id,
        quantity: dish.quantity,
        customization: remark.value,
        selectedOptionalIngredients: selectedOptional
      }
    })

    const data = {
      groupOrderId: props.groupOrderId,
      dishItems: dishItems
    }

    await addDishApi.createAddDishRequest(data)
    ElMessage.success('加菜请求已提交，等待发起者审核')
    emit('success')
    handleClose()
    visible.value = false
  } catch (error) {
    ElMessage.error('提交失败：' + (error.message || '未知错误'))
  }
}
</script>

<style scoped lang="scss">
.order-progress {
  margin-bottom: 20px;

  h4 {
    margin: 0 0 12px 0;
    font-size: 16px;
    font-weight: 500;
  }
}

.ordered-dishes {
  margin-bottom: 20px;

  h4 {
    margin: 0 0 12px 0;
    font-size: 16px;
    font-weight: 500;
  }

  .add-dish-tag {
    color: #f56c6c;
    font-weight: 500;
  }
}

.allergy-warning {
  margin-bottom: 20px;

  .conflict-content {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
  }
}

.dish-selection {
  margin-bottom: 20px;

  h4 {
    margin: 0 0 12px 0;
    font-size: 16px;
    font-weight: 500;
  }

  .dish-list {
    max-height: 300px;
    overflow-y: auto;
  }

  .dish-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    margin-bottom: 8px;
    transition: all 0.3s;

    &:hover {
      border-color: #409eff;
    }

    &.selected {
      background-color: #f0f9ff;
      border-color: #409eff;
    }
  }

  .dish-info {
    display: flex;
    align-items: center;
    flex: 1;
  }

  .dish-image {
    width: 60px;
    height: 60px;
    border-radius: 4px;
    object-fit: cover;
    margin-right: 12px;
  }

  .dish-no-image {
    width: 60px;
    height: 60px;
    border-radius: 4px;
    background-color: #f5f7fa;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;
    color: #909399;
  }

  .dish-details {
    flex: 1;
  }

  .dish-actions {
    display: flex;
    align-items: center;
    gap: 8px;

    .dish-total-price {
      font-size: 18px;
      color: #f56c6c;
      font-weight: 600;
      min-width: 80px;
      text-align: right;
    }
  }

  .dish-name {
    font-size: 14px;
    font-weight: 500;
    margin-bottom: 4px;
  }

  .dish-price {
    font-size: 16px;
    color: #f56c6c;
    font-weight: 500;
  }

  // 食材组成
  .dish-ingredients {
    margin-top: 8px;
    padding: 8px;
    background: #f5f7fa;
    border-radius: 4px;
    font-size: 12px;

    .ingredient-section {
      margin-bottom: 8px;

      &:last-child {
        margin-bottom: 0;
      }

      .ingredient-title {
        display: inline-block;
        font-weight: 600;
        color: #606266;
        margin-right: 6px;
      }

      .ingredient-list {
        display: inline;

        .ingredient-item {
          display: inline-block;
          background: linear-gradient(135deg, #409eff 0%, #53a8ff 100%);
          color: #ffffff;
          padding: 2px 8px;
          border-radius: 4px;
          font-size: 11px;
          margin-right: 4px;
          margin-bottom: 4px;
          font-weight: 500;
        }

        .ingredient-checkbox {
          display: inline-block;
          margin-right: 8px;
          margin-bottom: 4px;
          padding: 2px 6px;
          background: #ffffff;
          border: 1px solid #dcdfe6;
          border-radius: 4px;
          font-size: 11px;

          :deep(.el-checkbox__label) {
            font-size: 11px;
            padding-left: 4px;
          }

          .ingredient-price {
            color: #f56c6c;
            font-weight: 600;
            font-size: 11px;
          }
        }
      }
    }
  }
}

.remark-section {
  margin-bottom: 20px;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;

  .total-info {
    display: flex;
    align-items: center;
    gap: 16px;
    font-size: 14px;
  }

  .total-amount {
    font-size: 18px;
    font-weight: 500;
    color: #f56c6c;
  }
}
</style>
