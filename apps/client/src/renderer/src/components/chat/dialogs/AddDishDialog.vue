<template>
  <el-dialog
    v-model="visible"
    title="加菜"
    width="900px"
    @close="handleClose"
    :close-on-click-modal="false"
    class="add-dish-dialog"
  >
    <!-- 订单进度 -->
    <div class="order-progress">
      <div class="progress-header">
        <el-icon :size="16" color="#409eff"><Clock /></el-icon>
        <span>订单进度</span>
      </div>
      <el-steps :active="currentStep" finish-status="success" simple>
        <el-step title="待接单"></el-step>
        <el-step title="备菜中"></el-step>
        <el-step title="烹饪中"></el-step>
        <el-step title="待上菜"></el-step>
      </el-steps>
    </div>

    <!-- 已点菜品清单 -->
    <div class="ordered-dishes">
      <div class="section-header">
        <el-icon :size="16" color="#67c23a"><Dish /></el-icon>
        <span>已点菜品</span>
      </div>
      <el-table :data="orderedDishes" max-height="200" size="small">
        <el-table-column prop="dishName" label="菜品" min-width="120">
          <template #default="{ row }">
            <el-tag v-if="row.isAddDish" type="danger" size="small" class="add-dish-tag">
              {{ ADD_DISH_PREFIX }}
            </el-tag>
            <span style="margin-left: 8px">{{ row.dishName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" align="center"></el-table-column>
        <el-table-column prop="price" label="单价" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.price?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="addDishUser" label="加菜人" width="120" align="center">
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
      <div class="section-header">
        <el-icon :size="16" color="#e6a23c"><ShoppingCart /></el-icon>
        <span>选择加菜菜品</span>
      </div>
      <div class="dish-list">
        <div
          v-for="dish in availableDishes"
          :key="dish.id"
          class="dish-item"
          :class="{ selected: isDishSelected(dish.id) }"
        >
          <div class="dish-main">
            <!-- 菜品图片 -->
            <div class="dish-image-wrapper">
              <img v-if="dish.image" :src="dish.image" class="dish-image" />
              <div class="dish-no-image" v-else>
                <el-icon :size="28"><Food /></el-icon>
              </div>
            </div>

            <!-- 菜品信息 -->
            <div class="dish-info">
              <div class="dish-name-price">
                <div class="dish-name">{{ dish.name }}</div>
                <div class="dish-price">¥{{ dish.price?.toFixed(2) || '0.00' }}</div>
              </div>

              <!-- 食材组成 -->
              <div class="dish-ingredients" v-if="hasIngredients(dish)">
                <!-- 必选食材 -->
                <div
                  class="ingredient-section"
                  v-if="dish.requiredIngredients && dish.requiredIngredients.length > 0"
                >
                  <div class="ingredient-header">
                    <el-icon :size="12" color="#409eff"><CircleCheck /></el-icon>
                    <span class="ingredient-label">必选食材</span>
                    <span class="ingredient-count">{{ dish.requiredIngredients.length }}</span>
                  </div>
                  <div class="ingredient-tags">
                    <el-tag
                      v-for="ingredient in dish.requiredIngredients"
                      :key="ingredient"
                      size="small"
                      type="primary"
                      effect="plain"
                      class="required-tag"
                    >
                      <el-icon :size="10" style="margin-right: 2px"><Check /></el-icon>
                      {{ ingredient }}
                    </el-tag>
                  </div>
                </div>

                <!-- 可选食材 -->
                <div
                  class="ingredient-section"
                  v-if="dish.optionalIngredients && dish.optionalIngredients.length > 0"
                >
                  <div class="ingredient-header">
                    <el-icon :size="12" color="#67c23a"><CirclePlus /></el-icon>
                    <span class="ingredient-label">可选食材</span>
                    <span class="ingredient-count">{{ dish.optionalIngredients.length }}</span>
                  </div>
                  <div class="ingredient-options">
                    <el-checkbox
                      v-for="ingredient in dish.optionalIngredients"
                      :key="ingredient.id || ingredient.name"
                      v-model="ingredient.selected"
                      size="small"
                      class="option-checkbox"
                    >
                      <span class="checkbox-content">
                        <span class="checkbox-name">{{ ingredient.name }}</span>
                        <span class="ingredient-price" v-if="ingredient.price">
                          +¥{{ ingredient.price.toFixed(2) }}
                        </span>
                      </span>
                    </el-checkbox>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 操作区域 -->
          <div class="dish-actions">
            <div class="action-row" v-if="isDishSelected(dish.id)">
              <span class="total-price">¥{{ calculateDishPrice(dish).toFixed(2) }}</span>
              <el-input-number
                v-model="dish.quantity"
                :min="1"
                :max="99"
                size="small"
                :controls-position="'right'"
              />
            </div>
            <el-button
              :type="isDishSelected(dish.id) ? 'danger' : 'primary'"
              size="small"
              @click="toggleDish(dish)"
              class="action-btn"
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
          <div class="info-item">
            <el-icon :size="18"><ShoppingCart /></el-icon>
            <span>已选 <strong>{{ selectedDishes.length }}</strong> 道菜</span>
          </div>
          <div class="info-item amount">
            <el-icon :size="18"><Wallet /></el-icon>
            <span>累计: <strong>¥{{ totalAmount.toFixed(2) }}</strong></span>
          </div>
        </div>
        <el-button
          type="primary"
          @click="handleSubmit"
          :disabled="selectedDishes.length === 0"
          size="default"
        >
          提交加菜请求
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Food, Clock, Dish, ShoppingCart, Wallet, CircleCheck, CirclePlus, Check } from '@element-plus/icons-vue'
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

// 检查菜品是否有食材信息
const hasIngredients = (dish) => {
  return (dish.requiredIngredients && dish.requiredIngredients.length > 0) ||
         (dish.optionalIngredients && dish.optionalIngredients.length > 0)
}

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
.add-dish-dialog {
  :deep(.el-dialog__body) {
    padding: 16px 20px;
    max-height: 70vh;
    overflow-y: auto;
  }
}

.order-progress {
  margin-bottom: 16px;
  padding: 12px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 8px;

  .progress-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    font-size: 1rem /* 原值: 14px */;
    font-weight: 600;
    color: #409eff;
  }

  :deep(.el-steps--simple) {
    background: transparent;
    padding: 0;
  }
}

.ordered-dishes {
  margin-bottom: 16px;

  .section-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    font-size: 1rem /* 原值: 14px */;
    font-weight: 600;
    color: #67c23a;
  }

  .add-dish-tag {
    font-weight: 500;
  }
}

.allergy-warning {
  margin-bottom: 16px;

  :deep(.el-alert) {
    .el-alert__content {
      .conflict-content {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-wrap: wrap;
      }
    }
  }
}

.dish-selection {
  margin-bottom: 16px;

  .section-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    font-size: 1rem /* 原值: 14px */;
    font-weight: 600;
    color: #e6a23c;
  }

  .dish-list {
    max-height: 400px;
    overflow-y: auto;
    padding-right: 4px;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background: #dcdfe6;
      border-radius: 3px;

      &:hover {
        background: #c0c4cc;
      }
    }
  }

  .dish-item {
    display: flex;
    justify-content: space-between;
    align-items: stretch;
    padding: 12px;
    border: 2px solid #e4e7ed;
    border-radius: 8px;
    margin-bottom: 10px;
    background: white;
    transition: all 0.3s ease;

    &:hover {
      border-color: #409eff;
      box-shadow: 0 2px 12px rgba(64, 158, 255, 0.15);
    }

    &.selected {
      background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
      border-color: #409eff;
    }
  }

  .dish-main {
    display: flex;
    flex: 1;
    gap: 12px;
    min-width: 0;
  }

  .dish-image-wrapper {
    flex-shrink: 0;
    width: 80px;
    height: 80px;

    .dish-image {
      width: 100%;
      height: 100%;
      border-radius: 8px;
      object-fit: cover;
      border: 1px solid #e4e7ed;
    }

    .dish-no-image {
      width: 100%;
      height: 100%;
      border-radius: 8px;
      background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #909399;
      border: 1px solid #e4e7ed;
    }
  }

  .dish-info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .dish-name-price {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;

    .dish-name {
      font-size: 1.071rem /* 原值: 15px */;
      font-weight: 600;
      color: #303133;
      flex: 1;
      min-width: 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .dish-price {
      font-size: 1.286rem /* 原值: 18px */;
      color: #f56c6c;
      font-weight: 700;
      flex-shrink: 0;
    }
  }

  .dish-ingredients {
    padding: 10px 12px;
    background: #f5f7fa;
    border-radius: 6px;
    font-size: 0.857rem /* 原值: 12px */;

    .ingredient-section {
      margin-bottom: 10px;

      &:last-child {
        margin-bottom: 0;
      }

      .ingredient-header {
        display: flex;
        align-items: center;
        gap: 6px;
        margin-bottom: 8px;

        .ingredient-label {
          font-weight: 600;
          color: #303133;
          font-size: 0.857rem /* 原值: 12px */;
        }

        .ingredient-count {
          margin-left: auto;
          padding: 2px 8px;
          background: #e4e7ed;
          border-radius: 10px;
          font-size: 0.75rem /* 原值: 11px */;
          color: #606266;
          font-weight: 600;
        }
      }

      .ingredient-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;

        .required-tag {
          :deep(.el-tag__content) {
            display: flex;
            align-items: center;
            gap: 2px;
          }
        }
      }

      .ingredient-options {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;

        .option-checkbox {
          margin: 0;
          padding: 6px 10px;
          background: white;
          border: 1px solid #dcdfe6;
          border-radius: 6px;
          transition: all 0.2s;

          &:hover {
            border-color: #67c23a;
            background: #f0f9ff;
            box-shadow: 0 2px 4px rgba(103, 194, 58, 0.1);
          }

          &.is-checked {
            border-color: #67c23a;
            background: #ecf5ff;
          }

          :deep(.el-checkbox__label) {
            font-size: 0.857rem /* 原值: 12px */;
            padding-left: 6px;
          }

          .checkbox-content {
            display: flex;
            align-items: center;
            gap: 6px;

            .checkbox-name {
              font-weight: 500;
            }

            .ingredient-price {
              color: #f56c6c;
              font-weight: 700;
              font-size: 0.75rem /* 原值: 11px */;
            }
          }
        }
      }
    }
  }

  .dish-actions {
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 8px;
    flex-shrink: 0;
    margin-left: 12px;
    min-width: 100px;

    .action-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 8px;
      padding: 4px 8px;
      background: white;
      border-radius: 6px;
      border: 1px solid #e4e7ed;

      .total-price {
        font-size: 1.143rem /* 原值: 16px */;
        color: #f56c6c;
        font-weight: 700;
      }

      :deep(.el-input-number) {
        width: 90px;
      }
    }

    .action-btn {
      width: 100%;
      font-weight: 600;
      height: 32px;
    }
  }
}

.remark-section {
  margin-bottom: 16px;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  border-radius: 8px;
  border: 1px solid #e4e7ed;

  .total-info {
    display: flex;
    gap: 20px;
    flex: 1;

    .info-item {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 1rem /* 原值: 14px */;
      color: #606266;

      strong {
        color: #303133;
        font-size: 1.143rem /* 原值: 16px */;
      }

      &.amount strong {
        color: #f56c6c;
        font-size: 1.286rem /* 原值: 18px */;
      }
    }
  }

  .el-button {
    height: 38px;
    padding: 0 24px;
    font-size: 1rem /* 原值: 14px */;
    font-weight: 600;
    flex-shrink: 0;
  }
}
</style>
