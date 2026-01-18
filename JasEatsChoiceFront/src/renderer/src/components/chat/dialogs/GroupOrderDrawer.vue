<template>
  <el-drawer
    v-model="visible"
    title="当前群订单"
    direction="rtl"
    size="45%"
    :close-on-click-modal="true"
  >
    <div v-if="groupOrder" class="drawer-content">
      <!-- 订单概览 -->
      <div class="order-overview">
        <div class="overview-item">
          <span class="info-label">群名称：</span>
          <span class="info-value">{{ groupOrder.groupName }}</span>
        </div>
        <div class="overview-item">
          <span class="info-label">订单创建人：</span>
          <span class="info-value">{{ groupOrder.creator }}</span>
        </div>
        <div class="overview-item" v-if="groupOrder.merchantName">
          <span class="info-label">已选商家：</span>
          <span class="info-value">
            {{ groupOrder.merchantName }}
            <el-button
              type="text"
              size="small"
              style="margin-left: 10px; color: #409eff"
              @click="$emit('change-merchant')"
              v-if="canChangeMerchant"
            >
              更换商家
            </el-button>
          </span>
        </div>
        <div class="overview-item">
          <span class="info-label">总金额：</span>
          <span class="info-value">¥{{ groupOrder.totalAmount.toFixed(2) }}</span>
        </div>
        <div class="overview-item">
          <span class="info-label">参与人数：</span>
          <span class="info-value">{{ groupOrder.members.length }}人</span>
        </div>
      </div>

      <!-- 快速点餐入口 -->
      <div
        class="quick-order-entry"
        v-if="hasMerchant && groupOrder.status === 'active'"
      >
        <el-button type="primary" size="small" @click="$emit('continue-order')">
          + 继续点餐
        </el-button>
      </div>

      <!-- 订单商品列表 -->
      <div class="order-items">
        <h4
          class="section-title"
          v-if="groupOrder.orderItems && groupOrder.orderItems.length > 0"
        >
          订单商品
        </h4>
        <div class="item-list">
          <el-card
            v-for="item in groupOrder.orderItems"
            :key="item.id"
            class="order-item-card"
            size="small"
          >
            <div class="order-item-header">
              <span class="item-name">{{ item.name }}</span>
              <span class="item-quantity">×{{ item.quantity }}</span>
              <span class="item-price">¥{{ item.price.toFixed(2) }}</span>
            </div>

            <!-- 必选食材 -->
            <div
              class="item-ingredients"
              v-if="item.requiredIngredients && item.requiredIngredients.length > 0"
            >
              <div class="ingredient-label">必选食材:</div>
              <div class="ingredient-list">
                <el-tag
                  v-for="ingredient in item.requiredIngredients"
                  :key="ingredient"
                  size="small"
                  type="info"
                  style="margin: 0 4px 4px 0"
                >
                  {{ ingredient }}
                </el-tag>
              </div>
            </div>

            <!-- 可选食材 -->
            <div
              class="item-ingredients"
              v-if="item.selectedOptionalIngredients && item.selectedOptionalIngredients.length > 0"
            >
              <div class="ingredient-label">已选可选食材:</div>
              <div class="ingredient-list">
                <el-tag
                  v-for="ingredient in item.selectedOptionalIngredients"
                  :key="ingredient.id || ingredient"
                  size="small"
                  type="success"
                  style="margin: 0 4px 4px 0"
                >
                  {{ ingredient.name }}
                </el-tag>
              </div>
            </div>

            <!-- 商品备注 -->
            <div class="item-remark" v-if="item.remark">
              <div class="remark-label">备注:</div>
              <div class="remark-content">{{ item.remark }}</div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div class="drawer-footer">
        <el-button @click="$emit('select-merchant')">选择商家和商品</el-button>
        <el-button type="success" @click="$emit('go-to-pay')">去支付</el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  groupOrder: {
    type: Object,
    default: null
  },
  currentUserId: {
    type: [String, Number],
    required: true
  }
})

const emit = defineEmits([
  'update:modelValue',
  'change-merchant',
  'continue-order',
  'select-merchant',
  'go-to-pay'
])

const visible = ref(props.modelValue)

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const hasMerchant = computed(() => {
  return props.groupOrder && props.groupOrder.merchantName
})

const canChangeMerchant = computed(() => {
  return (
    props.groupOrder &&
    props.groupOrder.creator === '我' &&
    props.groupOrder.orderItems.length === 0 &&
    props.groupOrder.status === 'active'
  )
})
</script>

<style scoped lang="less">
.drawer-content {
  display: flex;
  flex-direction: column;
  height: 100%;

  .order-overview {
    margin-bottom: 20px;

    .overview-item {
      margin-bottom: 12px;
      display: flex;
      align-items: center;

      .info-label {
        font-weight: 500;
        color: #606266;
        margin-right: 8px;
      }

      .info-value {
        color: #303133;
      }
    }
  }

  .quick-order-entry {
    margin-bottom: 20px;
    padding: 12px;
    background-color: #ecf5ff;
    border-radius: 4px;
    text-align: center;
  }

  .order-items {
    flex: 1;
    overflow-y: auto;

    .section-title {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 12px;
      color: #303133;
    }

    .item-list {
      display: flex;
      flex-direction: column;
      gap: 12px;

      .order-item-card {
        .order-item-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;

          .item-name {
            font-weight: 500;
            font-size: 14px;
          }

          .item-quantity {
            color: #909399;
            font-size: 13px;
          }

          .item-price {
            color: #e6a23c;
            font-weight: 600;
          }
        }

        .item-ingredients {
          margin-top: 8px;

          .ingredient-label {
            font-size: 12px;
            color: #909399;
            margin-bottom: 4px;
          }

          .ingredient-list {
            display: flex;
            flex-wrap: wrap;
          }
        }

        .item-remark {
          margin-top: 8px;
          padding: 8px;
          background-color: #f5f7fa;
          border-radius: 4px;

          .remark-label {
            font-size: 12px;
            color: #909399;
            margin-bottom: 4px;
          }

          .remark-content {
            font-size: 13px;
            color: #606266;
          }
        }
      }
    }
  }

  .drawer-footer {
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #e4e7ed;
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }
}
</style>
