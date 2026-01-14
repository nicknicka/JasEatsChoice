<template>
  <el-dialog
    v-model="visible"
    title="我的购物车"
    width="500px"
    @close="handleClose"
    :lock-scroll="false"
  >
    <div class="cart-content">
      <div v-if="cartItems.length === 0" class="empty-cart">
        <div class="empty-cart-icon">
          <el-icon :size="64"><ShoppingCart /></el-icon>
        </div>
        <div class="empty-cart-text">购物车是空的</div>
      </div>
      <div v-else class="cart-items-list">
        <div class="cart-item-card" v-for="(item, index) in cartItems" :key="item.id">
          <!-- 商品信息区(左侧) -->
          <div class="cart-item-left">
            <div class="cart-item-name">{{ item.name }}</div>

            <!-- 单价 -->
            <div class="cart-item-price">¥{{ item.price.toFixed(2) }}</div>

            <!-- 备注区域 -->
            <div class="cart-item-note">
              <div class="note-display" v-if="!item.isEditingNote">
                <div class="note-content-wrapper">
                  <span v-if="item.note" class="note-text">{{ item.note }}</span>
                  <span v-else class="note-empty">暂无备注</span>
                </div>
                <el-button
                  size="small"
                  class="edit-note-btn"
                  @click="startEditNote(item)"
                  text
                >
                  <el-icon class="edit-icon"><Edit /></el-icon>
                </el-button>
              </div>
              <div class="note-edit" v-else>
                <el-input
                  v-model="item.tempNote"
                  placeholder="输入备注..."
                  size="small"
                  type="textarea"
                  :rows="2"
                  resize="none"
                  autofocus
                />
                <div class="note-actions">
                  <el-button size="small" type="primary" @click="confirmNote(item)" class="confirm-note-btn">
                    确认
                  </el-button>
                  <el-button size="small" @click="cancelNote(item)" class="cancel-note-btn">取消</el-button>
                </div>
              </div>
            </div>

            <!-- 可选食材展示 -->
            <div
              v-if="item.selectedOptionalIngredients && item.selectedOptionalIngredients.length > 0"
              class="cart-item-ingredients"
            >
              <span
                v-for="(ingredient, idx) in item.selectedOptionalIngredients"
                :key="idx"
                class="ingredient-tag"
              >
                +{{ ingredient.name }} (¥{{ ingredient.price.toFixed(2) }})
              </span>
            </div>
          </div>

          <!-- 数量和总价区(右侧) -->
          <div class="cart-item-right">
            <!-- 数量调整 -->
            <div class="quantity-control">
              <el-button
                class="quantity-btn quantity-btn-decrease"
                :disabled="item.quantity <= 1"
                @click="decreaseQuantity(index)"
                circle
                size="small"
              >
                <el-icon><Minus /></el-icon>
              </el-button>
              <span class="quantity-number">{{ item.quantity }}</span>
              <el-button
                class="quantity-btn quantity-btn-increase"
                @click="increaseQuantity(index)"
                circle
                size="small"
              >
                <el-icon><Plus /></el-icon>
              </el-button>
            </div>

            <!-- 商品总价 -->
            <div class="cart-item-total">¥{{ item.totalPrice.toFixed(2) }}</div>
          </div>
        </div>

        <!-- 总计区域 -->
        <div class="cart-total-section">
          <div class="total-label">总计</div>
          <div class="total-amount">
            ¥{{ cartItems.reduce((total, item) => total + item.totalPrice, 0).toFixed(2) }}
          </div>
        </div>
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button class="cancel-btn" @click="handleClose">取消</el-button>
        <el-button type="primary" v-if="cartItems.length > 0" @click="handleSubmitOrder" class="submit-btn">
          提交订单
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ShoppingCart, Edit, Plus, Minus } from '@element-plus/icons-vue'
import { computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  cartItems: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'update-cart', 'submit-order'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const handleClose = () => {
  visible.value = false
}

const startEditNote = (item) => {
  item.isEditingNote = true
}

const confirmNote = (item) => {
  item.note = item.tempNote
  item.isEditingNote = false
}

const cancelNote = (item) => {
  item.tempNote = item.note
  item.isEditingNote = false
}

const decreaseQuantity = (index) => {
  const item = props.cartItems[index]
  if (item.quantity > 1) {
    item.quantity--
    item.totalPrice = (item.price + getOptionalPrice(item)) * item.quantity
  } else {
    // 移除商品
    emit('update-cart', { action: 'remove', index })
  }
  emit('update-cart', { action: 'update', item })
}

const increaseQuantity = (index) => {
  const item = props.cartItems[index]
  item.quantity++
  item.totalPrice = (item.price + getOptionalPrice(item)) * item.quantity
  emit('update-cart', { action: 'update', item })
}

const getOptionalPrice = (item) => {
  if (!item.selectedOptionalIngredients || item.selectedOptionalIngredients.length === 0) {
    return 0
  }
  return item.selectedOptionalIngredients.reduce((sum, ingredient) => sum + ingredient.price, 0)
}

const handleSubmitOrder = () => {
  emit('submit-order')
  handleClose()
}
</script>

<style scoped lang="less">
.cart-content {
  padding: 0;

  .empty-cart {
    text-align: center;
    padding: 60px 20px;

    .empty-cart-icon {
      margin-bottom: 20px;
      opacity: 0.3;
      color: #94a3b8;
    }

    .empty-cart-text {
      font-size: 16px;
      color: #64748b;
      font-weight: 500;
    }
  }

  .cart-items-list {
    max-height: 500px;
    overflow-y: auto;
    padding: 16px;

    .cart-item-card {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      padding: 16px;
      margin-bottom: 16px;
      background: #ffffff;
      border-radius: 12px;
      border: 1px solid #e8e8e8;
      transition: all 0.3s ease;

      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
        border-color: #1890ff;
      }

      .cart-item-left {
        flex: 1;
        min-width: 0;
        margin-right: 16px;

        .cart-item-name {
          font-size: 16px;
          font-weight: 600;
          color: #1a1a1a;
          margin-bottom: 8px;
          line-height: 1.4;
        }

        .cart-item-price {
          font-size: 14px;
          color: #1890ff;
          font-weight: 500;
          margin-bottom: 12px;
        }

        .cart-item-note {
          margin: 8px 0;

          .note-display {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 8px;
            padding: 8px 12px;
            background: #f5f7fa;
            border-radius: 6px;
            min-height: 36px;

            .note-content-wrapper {
              flex: 1;
              min-width: 0;

              .note-text {
                font-size: 13px;
                color: #333333;
                word-wrap: break-word;
                word-break: break-all;
                line-height: 1.5;
              }

              .note-empty {
                font-size: 13px;
                color: #999999;
              }
            }

            .edit-note-btn {
              flex-shrink: 0;
              padding: 4px;
              color: #1890ff;
              transition: all 0.3s ease;

              .edit-icon {
                font-size: 16px;
              }

              &:hover {
                background: rgba(24, 144, 255, 0.1);
                border-radius: 4px;
              }
            }
          }

          .note-edit {
            .el-textarea {
              margin-bottom: 8px;

              :deep(.el-textarea__inner) {
                font-size: 13px;
                padding: 8px;
                border-radius: 6px;
              }
            }

            .note-actions {
              display: flex;
              gap: 8px;

              .confirm-note-btn {
                border-radius: 6px;
                padding: 6px 16px;
                background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
                border: none;
                font-weight: 500;
                transition: all 0.3s ease;

                &:hover {
                  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
                  transform: translateY(-1px);
                  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
                }
              }

              .cancel-note-btn {
                border-radius: 6px;
                padding: 6px 16px;
                border: 1px solid #d9d9d9;
                color: #666;
                background: #ffffff;
                font-weight: 500;
                transition: all 0.3s ease;

                &:hover {
                  color: #3b82f6;
                  border-color: #3b82f6;
                  background: rgba(59, 130, 246, 0.05);
                  transform: translateY(-1px);
                }
              }
            }
          }
        }

        .cart-item-ingredients {
          margin-top: 8px;

          .ingredient-tag {
            display: inline-block;
            font-size: 12px;
            color: #1890ff;
            background: #e6f7ff;
            border: 1px solid #91d5ff;
            padding: 4px 8px;
            border-radius: 4px;
            margin-right: 6px;
            margin-bottom: 4px;
            font-weight: 500;
          }
        }
      }

      .cart-item-right {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 12px;
        flex-shrink: 0;

        .quantity-control {
          display: flex;
          align-items: center;
          gap: 12px;

          .quantity-btn {
            width: 32px;
            height: 32px;
            padding: 0;
            display: flex;
            align-items: center;
            justify-content: center;
            border: 1px solid #d9d9d9;
            transition: all 0.3s ease;

            &.quantity-btn-decrease {
              &:not(:disabled):hover {
                color: #1890ff;
                border-color: #1890ff;
              }

              &:disabled {
                color: #d9d9d9;
                border-color: #d9d9d9;
                cursor: not-allowed;
              }
            }

            &.quantity-btn-increase {
              background: #1890ff;
              border-color: #1890ff;
              color: #ffffff;

              &:hover {
                background: #40a9ff;
                border-color: #40a9ff;
              }
            }
          }

          .quantity-number {
            min-width: 24px;
            text-align: center;
            font-size: 16px;
            font-weight: 600;
            color: #1a1a1a;
          }
        }

        .cart-item-total {
          font-size: 16px;
          font-weight: 700;
          color: #1890ff;
          text-align: center;
          min-width: 80px;
        }
      }
    }

    .cart-total-section {
      display: flex;
      justify-content: flex-end;
      align-items: center;
      padding: 16px 20px;
      margin-top: 8px;
      background: linear-gradient(135deg, #f0f5ff 0%, #e6f7ff 100%);
      border-radius: 12px;
      border: 1px solid #adc6ff;

      .total-label {
        font-size: 16px;
        font-weight: 600;
        color: #333333;
        margin-right: 12px;
      }

      .total-amount {
        font-size: 20px;
        font-weight: 700;
        color: #1890ff;
      }
    }
  }
}

:deep(.el-dialog__footer) {
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;

    .cancel-btn {
      min-width: 80px;
      height: 38px;
      border: 1px solid #d9d9d9;
      color: #666;
      background: #ffffff;
      border-radius: 8px;
      font-weight: 500;
      transition: all 0.3s ease;

      &:hover {
        color: #3b82f6;
        border-color: #3b82f6;
        background: rgba(59, 130, 246, 0.05);
        transform: translateY(-1px);
      }
    }

    .submit-btn {
      min-width: 100px;
      height: 38px;
      background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
      border: none;
      border-radius: 8px;
      font-weight: 600;
      transition: all 0.3s ease;
      box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);

      &:hover {
        background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
        transform: translateY(-1px);
        box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
      }
    }
  }
}
</style>
