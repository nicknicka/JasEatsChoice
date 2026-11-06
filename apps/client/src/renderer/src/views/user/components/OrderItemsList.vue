<script setup>
/**
 * 订单菜品列表组件
 */
import { computed } from 'vue'

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

const emit = defineEmits(['item-click', 'image-error'])

// 需要显示的菜品
const displayItems = computed(() => {
  if (!props.items || props.items.length === 0) return []
  return props.items.slice(0, props.maxDisplay)
})

// 是否有更多菜品
const hasMore = computed(() => {
  return props.dishCount > props.maxDisplay
})

// 剩余菜品数量
const remainingCount = computed(() => {
  return props.dishCount - props.maxDisplay
})

// 缩略图缺失/失败时的占位首字，保证仍能识别菜品（DESIGN-BASELINE §4.2）
function getItemInitial(item) {
  const name = (item?.name || '菜').trim()
  return name.charAt(0)
}

// 处理图片错误
function handleImageError(item) {
  emit('image-error', item)
}

// 处理菜品点击
function handleItemClick(item) {
  emit('item-click', item)
}
</script>

<template>
  <div class="order-items-list">
    <!-- 显示菜品 -->
    <div
      v-for="(item, index) in displayItems"
      :key="index"
      class="order-item"
      @click="handleItemClick(item)"
    >
      <!-- 菜品图片 -->
      <div class="item-image">
        <el-image
          v-if="item.image && !item.imageLoadError"
          :src="item.image"
          fit="cover"
          class="dish-image"
          @error="handleImageError(item)"
        >
          <template #error>
            <div class="image-placeholder">{{ getItemInitial(item) }}</div>
          </template>
        </el-image>
        <div v-else class="image-placeholder">{{ getItemInitial(item) }}</div>

        <!-- 数量角标 -->
        <div v-if="item.quantity > 1" class="quantity-badge">
          {{ item.quantity }}
        </div>
      </div>

      <!-- 菜品信息 -->
      <div class="item-info">
        <div class="item-name">{{ item.name }}</div>
        <div class="item-details">
          <span v-if="item.customization" class="item-customization">
            {{ item.customization }}
          </span>
          <span v-if="item.dishNote" class="item-note">
            备注: {{ item.dishNote }}
          </span>
        </div>
        <div class="item-price">¥{{ item.price }}</div>
      </div>
    </div>

    <!-- 更多菜品提示 -->
    <div v-if="hasMore" class="more-items">
      还有 {{ remainingCount }} 个菜品...
    </div>

    <!-- 空状态 -->
    <el-empty
      v-if="!items || items.length === 0"
      description="暂无菜品"
      :image-size="60"
    />
  </div>
</template>

<style scoped lang="less">
.order-items-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--space-3) 0;
  border-top: 1px solid var(--line);
  border-bottom: 1px solid var(--line);

  .order-item {
    display: flex;
    gap: var(--space-3);
    padding: var(--space-2);
    background: var(--canvas-sunken);
    border-radius: var(--radius-md);
    cursor: pointer;
    transition: background-color var(--duration-fast) var(--ease-out);

    &:hover {
      background: var(--brand-50);
    }

    .item-image {
      position: relative;
      width: 64px;
      height: 64px;
      flex-shrink: 0;

      .dish-image {
        width: 100%;
        height: 100%;
        border-radius: var(--radius-sm);
        overflow: hidden;

        :deep(.el-image__inner) {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .image-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--neutral-bg);
        border-radius: var(--radius-sm);
        color: var(--ink-500);
        font-size: var(--text-h2);
        font-weight: var(--weight-semibold);
      }

      .quantity-badge {
        position: absolute;
        top: calc(-1 * var(--space-1));
        right: calc(-1 * var(--space-1));
        min-width: 20px;
        height: 20px;
        padding: 0 var(--space-1);
        background: var(--ink-700);
        color: var(--surface);
        font-family: var(--font-num);
        font-size: var(--text-caption);
        font-weight: var(--weight-medium);
        border-radius: var(--radius-full);
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }

    .item-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      gap: var(--space-1);
      min-width: 0;

      .item-name {
        font-size: var(--text-body);
        font-weight: var(--weight-medium);
        color: var(--ink-900);
        line-height: 22px;
      }

      .item-details {
        display: flex;
        flex-wrap: wrap;
        gap: var(--space-1) var(--space-2);
        font-size: var(--text-body-sm);

        .item-customization {
          color: var(--ink-500);
          background: var(--canvas);
          padding: 2px var(--space-2);
          border-radius: var(--radius-sm);
          display: inline-block;
        }

        .item-note {
          color: var(--ink-500);
        }
      }

      .item-price {
        font-family: var(--font-num);
        font-variant-numeric: tabular-nums;
        font-size: var(--text-body);
        font-weight: var(--weight-medium);
        color: var(--ink-700);
      }
    }
  }

  .more-items {
    padding: var(--space-2) var(--space-3);
    text-align: center;
    background: var(--canvas-sunken);
    border-radius: var(--radius-sm);
    color: var(--ink-500);
    font-size: var(--text-body-sm);
    cursor: pointer;
    transition: background-color var(--duration-fast) var(--ease-out);

    &:hover {
      background: var(--brand-50);
    }
  }
}
</style>
