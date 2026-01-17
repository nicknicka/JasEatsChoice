<template>
  <div class="order-items-list">
    <div class="items-header">
      <div class="header-left">
        <span class="item-count">共 {{ itemCount || 0 }} 件商品</span>
        <el-tag v-if="itemCount > 0" type="info" size="small" class="item-tag">
          {{ dishCount }} 种菜品
        </el-tag>
      </div>
      <el-button
        v-if="hasMoreItems"
        type="primary"
        link
        size="small"
        class="expand-btn"
        @click="handleToggleExpand"
      >
        {{ isExpanded ? '收起' : `展开全部 (${itemCount}件)` }}
        <el-icon :class="{ 'expand-icon': true, expanded: isExpanded }">
          <ArrowDown />
        </el-icon>
      </el-button>
    </div>

    <div class="items-list">
      <div
        v-for="(item, index) in displayItems"
        :key="index"
        class="item-row"
        @click="handleItemClick(item)"
      >
        <!-- 商品图片 -->
        <div class="item-image">
          <img
            v-if="item.image && !item.imageLoadError"
            :src="item.image"
            :alt="item.name"
            loading="lazy"
            @error="handleImageError(item)"
          />
          <div v-else class="no-image">
            <span>{{ item.name?.charAt(0) || '菜' }}</span>
          </div>
          <!-- 数量徽章 -->
          <div class="quantity-badge" :class="{ 'large-number': item.quantity > 9 }">
            {{ item.quantity }}
          </div>
        </div>

        <!-- 商品信息 -->
        <div class="item-info">
          <div class="item-name">{{ item.name }}</div>

          <!-- 必选食材 -->
          <div
            v-if="item.requiredIngredients && item.requiredIngredients.length > 0"
            class="item-ingredients"
          >
            <div class="ingredients-label">
              <span class="label-text">必选:</span>
            </div>
            <div class="ingredients-list">
              <span
                v-for="ing in item.requiredIngredients"
                :key="ing"
                class="ingredient-tag required"
              >
                {{ ing }}
              </span>
            </div>
          </div>

          <!-- 可选食材 -->
          <div
            v-if="item.optionalIngredients && item.optionalIngredients.length > 0"
            class="item-ingredients"
          >
            <div class="ingredients-label">
              <span class="label-text">可选:</span>
            </div>
            <div class="ingredients-list">
              <span
                v-for="ing in item.optionalIngredients"
                :key="ing.id || ing.name"
                class="ingredient-tag optional"
              >
                {{ ing.name }}
                <span v-if="ing.price" class="ingredient-price">+¥{{ ing.price.toFixed(2) }}</span>
              </span>
            </div>
          </div>

          <!-- 备注信息 -->
          <div v-if="item.dishNote" class="item-note">
            <el-icon><EditPen /></el-icon>
            <span class="note-text">{{ item.dishNote }}</span>
          </div>

          <!-- 自定义信息（兼容旧数据） -->
          <div v-if="item.customization && !item.dishNote" class="item-customization">
            <el-icon><EditPen /></el-icon>
            <el-tooltip :content="item.customization" placement="top" :show-after="500">
              <span class="customization-text">{{ item.customization }}</span>
            </el-tooltip>
          </div>

          <div class="item-price-detail">
            <span class="unit-price">¥{{ item.price.toFixed(2) }} /份</span>
            <span class="total-price">小计 ¥{{ (item.price * item.quantity).toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!items || items.length === 0" class="items-empty">
        <el-empty description="暂无商品信息" :image-size="60" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ArrowDown, EditPen } from '@element-plus/icons-vue'
import { formatDisplayItems } from '../../../utils/formatters'

/**
 * 订单菜品列表组件
 */
const props = defineProps({
  items: {
    type: Array,
    default: () => []
  },
  itemCount: {
    type: Number,
    default: 0
  },
  dishCount: {
    type: Number,
    default: 0
  },
  maxDisplay: {
    type: Number,
    default: 3
  }
})

const emit = defineEmits(['toggle-expand', 'item-click', 'image-error'])

// 展开状态
const isExpanded = ref(false)

/**
 * 显示的菜品
 */
const displayItems = computed(() => {
  const result = formatDisplayItems(props.items, props.maxDisplay)
  return result.items
})

/**
 * 是否有更多菜品
 */
const hasMoreItems = computed(() => {
  return props.items.length > props.maxDisplay
})

/**
 * 切换展开状态
 */
function handleToggleExpand() {
  isExpanded.value = !isExpanded.value
  emit('toggle-expand', isExpanded.value)
}

/**
 * 处理菜品点击
 */
function handleItemClick(item) {
  emit('item-click', item)
}

/**
 * 处理图片加载错误
 */
function handleImageError(item) {
  emit('image-error', item)
}

// 监听外部展开状态变化
watch(
  () => props.maxDisplay,
  () => {
    isExpanded.value = false
  }
)
</script>

<style scoped lang="less">
.order-items-list {
  background: #fafbfc;
  border-radius: 12px;
  padding: 14px;
  border: 1px solid rgba(0, 0, 0, 0.06);

  .items-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    padding-bottom: 10px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);

    .header-left {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .item-count {
      font-size: 13px;
      color: #64748b;
      font-weight: 500;
    }

    .item-tag {
      font-size: 12px;
      border-radius: 12px;
      background: #e6f7ff;
      border-color: #91d5ff;
      color: #1890ff;
    }

    .expand-btn {
      font-size: 13px;
      padding: 6px 12px;
      height: auto;
      border-radius: 16px;
      background: #ffffff;
      border: 1px solid #d9d9d9;
      color: #5c8eff;
      transition: all 0.3s ease;

      &:hover {
        background: #f0f9ff;
        border-color: #6ba4ff;
        transform: translateY(-1px);
      }

      .expand-icon {
        transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        margin-left: 4px;

        &.expanded {
          transform: rotate(180deg);
        }
      }
    }
  }

  .items-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .item-row {
    display: flex;
    align-items: stretch;
    padding: 14px;
    background: #ffffff;
    border-radius: 12px;
    transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    border: 1px solid rgba(0, 0, 0, 0.06);
    min-height: 94px;
    cursor: pointer;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);

    &:hover {
      box-shadow: 0 6px 20px rgba(92, 142, 255, 0.2);
      transform: translateY(-3px) scale(1.015);
      border-color: rgba(92, 142, 255, 0.5);
      background: linear-gradient(to bottom, #ffffff 0%, #f8faff 100%);

      &::after {
        content: '';
        position: absolute;
        inset: 0;
        border-radius: 12px;
        box-shadow: inset 0 0 0 1px rgba(92, 142, 255, 0.3);
        pointer-events: none;
      }
    }

    &:active {
      transform: translateY(-1px) scale(1.008);
      box-shadow: 0 3px 12px rgba(92, 142, 255, 0.15);
    }

    .item-image {
      width: 70px;
      min-height: 70px;
      height: 70px;
      border-radius: 10px;
      overflow: visible;
      margin-right: 14px;
      flex-shrink: 0;
      background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
      position: relative;
      z-index: 1;
      box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
      display: flex;
      align-items: center;
      justify-content: center;
      border: 1px solid #91d5ff;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 10px;
        position: relative;
        z-index: 1;
      }

      .no-image {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;

        span {
          font-size: 28px;
          font-weight: 600;
          color: #1890ff;
        }
      }

      .quantity-badge {
        position: absolute;
        top: -4px;
        right: -4px;
        background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
        color: white;
        font-size: 11px;
        font-weight: 700;
        min-width: 18px;
        height: 18px;
        padding: 0 5px;
        border-radius: 9px;
        box-shadow:
          0 2px 6px rgba(255, 77, 79, 0.4),
          0 0 0 1.5px rgba(255, 255, 255, 1);
        z-index: 100;
        display: flex;
        align-items: center;
        justify-content: center;
        line-height: 1;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        animation: badge-bounce 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
        pointer-events: none;

        .item-row:hover & {
          transform: scale(1.1) rotate(-3deg);
          box-shadow:
            0 3px 8px rgba(255, 77, 79, 0.5),
            0 0 0 1.5px rgba(255, 255, 255, 1);
          background: linear-gradient(135deg, #ff4d4f 0%, #ff2626 100%);
        }

        &.large-number {
          font-size: 9px;
          min-width: 20px;
          padding: 0 4px;
        }
      }

      @keyframes badge-bounce {
        0% {
          transform: scale(0) rotate(-180deg);
          opacity: 0;
        }
        50% {
          transform: scale(1.2) rotate(10deg);
        }
        100% {
          transform: scale(1) rotate(0deg);
          opacity: 1;
        }
      }
    }

    .item-info {
      flex: 1;
      min-width: 0;
      display: flex;
      flex-direction: column;
      justify-content: space-between;

      .item-name {
        font-size: 15px;
        font-weight: 600;
        color: #2c5282;
        margin-bottom: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        flex-shrink: 0;
      }

      .item-ingredients {
        display: flex;
        align-items: flex-start;
        gap: 6px;
        margin-bottom: 8px;
        font-size: 12px;

        .ingredients-label {
          flex-shrink: 0;

          .label-text {
            color: #64748b;
            font-weight: 500;
          }
        }

        .ingredients-list {
          display: flex;
          flex-wrap: wrap;
          gap: 5px;
          flex: 1;

          .ingredient-tag {
            display: inline-flex;
            align-items: center;
            gap: 3px;
            padding: 3px 8px;
            border-radius: 6px;
            font-size: 11px;
            line-height: 1.4;
            transition: all 0.2s ease;

            &.required {
              background: linear-gradient(
                135deg,
                rgba(103, 194, 58, 0.9) 0%,
                rgba(93, 175, 52, 0.9) 100%
              );
              color: white;
              font-weight: 500;
              box-shadow: 0 1px 4px rgba(103, 194, 58, 0.25);
            }

            &.optional {
              background: rgba(232, 244, 232, 0.8);
              color: #5da842;
              border: 1px solid rgba(179, 225, 157, 0.5);
            }

            .ingredient-price {
              font-size: 10px;
              opacity: 0.85;
              margin-left: 2px;
            }
          }
        }
      }

      .item-note {
        display: flex;
        align-items: flex-start;
        gap: 5px;
        margin-bottom: 8px;
        font-size: 12px;
        color: #c4873a;
        padding: 6px 10px;
        background: rgba(253, 246, 236, 0.8);
        border-radius: 8px;
        border: 1px solid rgba(245, 218, 177, 0.5);
        line-height: 1.5;
        box-shadow: 0 1px 4px rgba(230, 162, 60, 0.08);

        .el-icon {
          font-size: 13px;
          color: #c4873a;
          flex-shrink: 0;
          margin-top: 1px;
        }

        .note-text {
          flex: 1;
          word-break: break-word;
        }
      }

      .item-customization {
        font-size: 12px;
        color: #3a7bd5;
        margin-bottom: 8px;
        padding: 6px 10px;
        background: rgba(227, 242, 253, 0.7);
        border-radius: 8px;
        border: 1px solid rgba(187, 222, 251, 0.5);
        line-height: 1.5;
        max-height: 40px;
        overflow: hidden;
        display: flex;
        align-items: flex-start;
        gap: 5px;
        box-shadow: 0 1px 4px rgba(33, 150, 243, 0.08);

        .customization-text {
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          line-clamp: 2;
          -webkit-box-orient: vertical;
          word-break: break-word;
        }

        .el-icon {
          font-size: 14px;
          color: #4a90e2;
          flex-shrink: 0;
          margin-top: 1px;
        }
      }

      .item-price-detail {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-top: auto;
        flex-shrink: 0;

        .unit-price {
          font-size: 12px;
          color: #94a3b8;
        }

        .total-price {
          font-size: 16px;
          font-weight: 700;
          color: #ff6b6b;
        }
      }
    }
  }

  .items-empty {
    text-align: center;
    padding: 24px;
    color: #94a3b8;
    font-size: 14px;
  }
}

@media (max-width: 768px) {
  .order-items-list {
    padding: 12px;
    border-radius: 10px;

    .items-header {
      margin-bottom: 10px;
      padding-bottom: 8px;

      .header-left {
        gap: 6px;
      }

      .expand-btn {
        font-size: 12px;
        padding: 5px 10px;
      }
    }

    .item-row {
      padding: 12px;
      min-height: 80px;
      border-radius: 10px;

      .item-image {
        width: 56px;
        min-height: 56px;
        height: 56px;
        margin-right: 12px;
        border-radius: 8px;

        img {
          border-radius: 8px;
        }

        .quantity-badge {
          min-width: 16px;
          height: 16px;
          font-size: 10px;
          padding: 0 4px;
          top: -3px;
          right: -3px;
          border-radius: 8px;

          &.large-number {
            font-size: 8px;
            min-width: 18px;
            padding: 0 3px;
          }
        }

        .no-image span {
          font-size: 22px;
        }
      }

      .item-info {
        .item-name {
          font-size: 14px;
          margin-bottom: 6px;
        }

        .item-ingredients {
          font-size: 11px;
          margin-bottom: 6px;
          gap: 5px;

          .ingredients-list {
            gap: 4px;

            .ingredient-tag {
              font-size: 10px;
              padding: 2px 6px;
              border-radius: 4px;
            }
          }
        }

        .item-note {
          font-size: 11px;
          padding: 5px 8px;
          margin-bottom: 6px;
          border-radius: 6px;

          .el-icon {
            font-size: 12px;
          }
        }

        .item-customization {
          font-size: 11px;
          padding: 5px 8px;
          max-height: 36px;
          min-height: 28px;
          border-radius: 6px;
          margin-bottom: 6px;

          .el-icon {
            font-size: 12px;
          }
        }

        .item-price-detail {
          flex-direction: column;
          align-items: flex-start;
          gap: 4px;

          .unit-price {
            font-size: 11px;
          }

          .total-price {
            font-size: 15px;
          }
        }
      }
    }
  }
}
</style>
