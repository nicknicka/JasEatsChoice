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
            <div class="image-error">
              <el-icon><Picture /></el-icon>
            </div>
          </template>
        </el-image>
        <div v-else class="image-error">
          <el-icon><Picture /></el-icon>
        </div>

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
      <el-text type="info" size="small">
        还有 {{ remainingCount }} 个菜品...
      </el-text>
    </div>

    <!-- 空状态 -->
    <el-empty
      v-if="!items || items.length === 0"
      description="暂无菜品"
      :image-size="60"
    />
  </div>
</template>

<script>
import { Picture } from '@element-plus/icons-vue'

export default {
  components: {
    Picture
  }
}
</script>

<style scoped lang="less">
.order-items-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px 0;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);

  .order-item {
    display: flex;
    gap: 12px;
    padding: 12px;
    background: rgba(235, 244, 255, 0.2);
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    &:hover {
      background: rgba(235, 244, 255, 0.4);
      transform: translateX(4px);
      box-shadow: 0 2px 8px rgba(92, 142, 255, 0.15);
    }

    .item-image {
      position: relative;
      width: 80px;
      height: 80px;
      flex-shrink: 0;

      .dish-image {
        width: 100%;
        height: 100%;
        border-radius: 8px;
        overflow: hidden;

        :deep(.el-image__inner) {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .image-error {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
        border-radius: 8px;
        color: #69c0ff;
        font-size: 2.286rem /* 原值: 32px */;
      }

      .quantity-badge {
        position: absolute;
        top: -6px;
        right: -6px;
        min-width: 24px;
        height: 24px;
        padding: 0 6px;
        background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
        color: white;
        font-size: 0.857rem /* 原值: 12px */;
        font-weight: 600;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 2px 6px rgba(255, 107, 107, 0.4);
      }
    }

    .item-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      gap: 6px;

      .item-name {
        font-size: 1.071rem /* 原值: 15px */;
        font-weight: 600;
        color: #2c5282;
        line-height: 1.4;
      }

      .item-details {
        display: flex;
        flex-direction: column;
        gap: 4px;
        font-size: 0.857rem /* 原值: 12px */;

        .item-customization {
          color: #5c8eff;
          background: rgba(235, 244, 255, 0.6);
          padding: 2px 8px;
          border-radius: 4px;
          display: inline-block;
        }

        .item-note {
          color: #718096;
        }
      }

      .item-price {
        font-size: 1.143rem /* 原值: 16px */;
        font-weight: 700;
        color: #e53e3e;
      }
    }
  }

  .more-items {
    padding: 8px 12px;
    text-align: center;
    background: rgba(235, 244, 255, 0.3);
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      background: rgba(235, 244, 255, 0.5);
    }
  }
}
</style>
