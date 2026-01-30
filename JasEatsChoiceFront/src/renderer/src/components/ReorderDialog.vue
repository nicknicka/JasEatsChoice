<template>
  <el-dialog
    v-model="dialogVisible"
    title="再来一单"
    width="700px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-if="loading" v-loading="loading" class="loading-container">
      正在分析菜品状态...
    </div>

    <div v-else-if="reorderData" class="reorder-content">
      <!-- 价格变动提示 -->
      <div v-if="reorderData.hasChanges" class="price-change-alert">
        <el-alert
          :title="reorderData.amountChangeNote"
          :type="reorderData.soldOutCount > 0 ? 'warning' : 'info'"
          show-icon
          :closable="false"
        >
          <template #default>
            <div class="change-summary">
              <span v-if="reorderData.soldOutCount > 0">
                {{ reorderData.soldOutCount }}个菜品已下架
              </span>
              <span v-if="reorderData.priceIncreasedCount > 0">
                、{{ reorderData.priceIncreasedCount }}个菜品涨价
              </span>
              <span v-if="reorderData.priceDecreasedCount > 0">
                、{{ reorderData.priceDecreasedCount }}个菜品降价
              </span>
            </div>
          </template>
        </el-alert>
      </div>

      <!-- 订单信息继承提示 -->
      <div class="inherit-info">
        <el-icon class="info-icon"><InfoFilled /></el-icon>
        <span>将自动继承原订单的配送地址和备注信息</span>
      </div>

      <!-- 菜品列表 -->
      <div class="items-list">
        <div
          v-for="(item, index) in reorderData.items"
          :key="index"
          class="item-row"
          :class="{
            'sold-out': item.dishStatus === 1,
            'out-of-stock': item.dishStatus === 2,
            'price-increased': item.isPriceIncreased
          }"
        >
          <!-- 菜品基本信息 -->
          <div class="item-info">
            <el-checkbox
              v-model="item.selected"
              :disabled="!item.canSelect"
              @change="handleItemSelect"
            />
            <el-image
              :src="item.dishImage || defaultImage"
              class="item-image"
              fit="cover"
            >
              <template #error>
                <div class="image-slot">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="item-details">
              <div class="item-name">{{ item.dishName }}</div>
              <div class="item-specs">
                <span class="quantity">x{{ item.quantity }}</span>
                <span v-if="item.customization" class="customization">
                  {{ item.customization }}
                </span>
              </div>
            </div>
          </div>

          <!-- 价格信息 -->
          <div class="item-price">
            <div v-if="item.dishStatus === 0" class="price-info">
              <div class="current-price">¥{{ item.currentPrice }}</div>
              <div v-if="item.priceChangeNote" class="price-change" :class="{ increased: item.isPriceIncreased }">
                {{ item.priceChangeNote }}
              </div>
            </div>
            <div v-else-if="item.dishStatus === 1" class="status-text sold-out-text">
              已下架
            </div>
            <div v-else-if="item.dishStatus === 2" class="status-text out-of-stock-text">
              库存不足
            </div>
          </div>

          <!-- 推荐替换菜品 -->
          <div v-if="item.suggestedDishId" class="suggestion">
            <el-tag type="info" size="small">推荐替换</el-tag>
            <div class="suggestion-item">
              <span class="suggestion-name">{{ item.suggestedDishName }}</span>
              <span class="suggestion-price">¥{{ item.suggestedDishPrice }}</span>
              <el-button
                type="primary"
                size="small"
                link
                @click="handleReplaceWithSuggested(index)"
              >
                替换
              </el-button>
            </div>
            <div class="suggestion-reason">{{ item.suggestionReason }}</div>
          </div>
        </div>
      </div>

      <!-- 价格汇总 -->
      <div class="price-summary">
        <div class="summary-row">
          <span>原订单总价：</span>
          <span class="original-price">¥{{ reorderData.originalTotalAmount }}</span>
        </div>
        <div class="summary-row">
          <span>当前总价：</span>
          <span class="current-price">¥{{ selectedTotalPrice }}</span>
        </div>
        <div v-if="priceDifference !== 0" class="summary-row difference">
          <span>价格变动：</span>
          <span :class="{ increase: priceDifference > 0, decrease: priceDifference < 0 }">
            {{ priceDifference > 0 ? '+' : '' }}¥{{ Math.abs(priceDifference) }}
          </span>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="primary"
          :disabled="!hasSelectedItems"
          :loading="submitting"
          @click="handleConfirm"
        >
          确认下单（{{ selectedCount }}件）
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled, Picture } from '@element-plus/icons-vue'
import axios from 'axios'
import { API_CONFIG } from '../config'
import { useRouter } from 'vue-router'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  orderId: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:visible', 'confirm'])

const router = useRouter()
const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const loading = ref(false)
const submitting = ref(false)
const reorderData = ref(null)
const defaultImage = 'https://via.placeholder.com/60?text=暂无图片'

// 选中的菜品数量
const selectedCount = computed(() => {
  if (!reorderData.value) return 0
  return reorderData.value.items.filter(item => item.selected && item.canSelect).length
})

// 选中菜品总价
const selectedTotalPrice = computed(() => {
  if (!reorderData.value) return 0
  return reorderData.value.items
    .filter(item => item.selected && item.canSelect)
    .reduce((total, item) => {
      return total + (item.currentPrice || item.originalPrice) * item.quantity
    }, 0)
    .toFixed(2)
})

// 价格差异
const priceDifference = computed(() => {
  if (!reorderData.value) return 0
  return (selectedTotalPrice.value - reorderData.value.originalTotalAmount).toFixed(2)
})

// 是否有选中的菜品
const hasSelectedItems = computed(() => selectedCount.value > 0)

// 监听弹窗打开，加载数据
watch(() => props.visible, (newVal) => {
  if (newVal && props.orderId) {
    loadReorderData()
  }
})

/**
 * 加载再来一单数据
 */
async function loadReorderData() {
  loading.value = true
  try {
    const response = await axios.post(
      `${API_CONFIG.baseURL}${API_CONFIG.order.detail}${props.orderId}/reorder`
    )

    if (response.data.success) {
      reorderData.value = response.data.data
      // 初始化选中状态
      reorderData.value.items.forEach(item => {
        item.selected = item.defaultSelected
      })
    } else {
      ElMessage.error(response.data.message || '加载订单信息失败')
      dialogVisible.value = false
    }
  } catch (error) {
    console.error('加载再来一单数据失败:', error)
    ElMessage.error('加载订单信息失败，请稍后重试')
    dialogVisible.value = false
  } finally {
    loading.value = false
  }
}

/**
 * 菜品选择变化
 */
function handleItemSelect() {
  // 触发重新计算
}

/**
 * 替换为推荐菜品
 */
function handleReplaceWithSuggested(index) {
  const item = reorderData.value.items[index]
  if (item.suggestedDishId) {
    // 创建新订单，使用替换的菜品
    const selectedItem = {
      dishId: item.suggestedDishId,
      dishName: item.suggestedDishName,
      price: item.suggestedDishPrice,
      quantity: item.quantity,
      customization: item.customization
    }

    // 保存商家信息到sessionStorage
    const merchantInfo = {
      id: reorderData.value.merchantId,
      name: reorderData.value.merchantName
    }
    sessionStorage.setItem('selectedMerchant', JSON.stringify(merchantInfo))

    // 跳转到商家页面，添加到购物车
    ElMessage.success(`已将"${item.dishName}"替换为"${item.suggestedDishName}"`)
    router.push({
      path: '/user/home/merchant-detail',
      query: {
        addToCart: JSON.stringify(selectedItem),
        merchantId: reorderData.value.merchantId
      }
    })

    dialogVisible.value = false
  }
}

/**
 * 确认下单
 */
function handleConfirm() {
  if (!hasSelectedItems.value) {
    ElMessage.warning('请至少选择一个菜品')
    return
  }

  const selectedItems = reorderData.value.items
    .filter(item => item.selected && item.canSelect)
    .map(item => ({
      dishId: item.dishId,
      dishName: item.dishName,
      price: item.currentPrice || item.originalPrice,
      quantity: item.quantity,
      customization: item.customization
    }))

  emit('confirm', {
    merchantId: reorderData.value.merchantId,
    merchantName: reorderData.value.merchantName,
    items: selectedItems,
    originalRemark: reorderData.value.originalRemark,
    originalAddress: reorderData.value.originalAddress,
    originalAddressId: reorderData.value.originalAddressId,
    totalAmount: selectedTotalPrice.value
  })

  dialogVisible.value = false
}

/**
 * 关闭弹窗
 */
function handleClose() {
  dialogVisible.value = false
  reorderData.value = null
}
</script>

<style scoped lang="less">
.loading-container {
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.reorder-content {
  .price-change-alert {
    margin-bottom: 16px;

    .change-summary {
      margin-top: 8px;
      font-size: 13px;
    }
  }

  .inherit-info {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px;
    background: #f0f9ff;
    border-radius: 8px;
    margin-bottom: 16px;
    color: #0284c7;
    font-size: 13px;

    .info-icon {
      font-size: 16px;
    }
  }

  .items-list {
    max-height: 400px;
    overflow-y: auto;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    padding: 12px;
    margin-bottom: 16px;

    .item-row {
      display: flex;
      align-items: flex-start;
      padding: 12px;
      border-radius: 8px;
      margin-bottom: 12px;
      background: #f9fafb;
      border: 1px solid #e5e7eb;
      transition: all 0.3s;

      &:last-child {
        margin-bottom: 0;
      }

      &.sold-out {
        background: #fef2f2;
        border-color: #fecaca;
        opacity: 0.8;
      }

      &.out-of-stock {
        background: #fffbeb;
        border-color: #fde68a;
        opacity: 0.8;
      }

      &.price-increased {
        background: #fef2f2;
        border-color: #fca5a5;
      }

      .item-info {
        display: flex;
        align-items: center;
        gap: 12px;
        flex: 1;

        .item-image {
          width: 60px;
          height: 60px;
          border-radius: 8px;
          overflow: hidden;
          flex-shrink: 0;

          .image-slot {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 100%;
            height: 100%;
            background: #f3f4f6;
            color: #9ca3af;
            font-size: 24px;
          }
        }

        .item-details {
          flex: 1;

          .item-name {
            font-weight: 500;
            color: #1f2937;
            margin-bottom: 4px;
            font-size: 14px;
          }

          .item-specs {
            display: flex;
            gap: 8px;
            font-size: 12px;
            color: #6b7280;

            .customization {
              color: #8b5cf6;
            }
          }
        }
      }

      .item-price {
        text-align: right;
        margin-right: 12px;

        .price-info {
          .current-price {
            font-size: 16px;
            font-weight: 600;
            color: #ef4444;
            margin-bottom: 4px;
          }

          .price-change {
            font-size: 12px;

            &.increased {
              color: #ef4444;
            }
          }
        }

        .status-text {
          padding: 4px 8px;
          border-radius: 4px;
          font-size: 12px;
          font-weight: 500;

          &.sold-out-text {
            background: #fee2e2;
            color: #dc2626;
          }

          &.out-of-stock-text {
            background: #fef3c7;
            color: #d97706;
          }
        }
      }

      .suggestion {
        width: 100%;
        margin-top: 12px;
        padding: 12px;
        background: #eff6ff;
        border-radius: 6px;
        border: 1px solid #bfdbfe;

        .suggestion-item {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-top: 8px;

          .suggestion-name {
            flex: 1;
            font-size: 13px;
            font-weight: 500;
            color: #1e40af;
          }

          .suggestion-price {
            font-size: 14px;
            font-weight: 600;
            color: #ef4444;
          }
        }

        .suggestion-reason {
          font-size: 12px;
          color: #64748b;
          margin-top: 8px;
        }
      }
    }
  }

  .price-summary {
    background: #f9fafb;
    border-radius: 8px;
    padding: 16px;

    .summary-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      font-size: 14px;

      &:last-child {
        margin-bottom: 0;
      }

      &.difference {
        padding-top: 12px;
        border-top: 1px solid #e5e7eb;
        font-weight: 600;
        font-size: 15px;

        .increase {
          color: #ef4444;
        }

        .decrease {
          color: #10b981;
        }
      }

      .original-price {
        color: #6b7280;
        text-decoration: line-through;
      }

      .current-price {
        font-size: 18px;
        font-weight: 700;
        color: #ef4444;
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
