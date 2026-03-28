<template>
  <view class="ingredients-list">
    <!-- 食材分组 -->
    <view
      class="ingredient-group"
      v-for="(group, groupIndex) in ingredientGroups"
      :key="groupIndex"
    >
      <!-- 分组标题 -->
      <view class="group-title" v-if="group.name">{{ group.name }}</view>

      <!-- 食材项列表 -->
      <view class="ingredient-items">
        <view
          class="ingredient-item"
          v-for="(item, index) in group.items"
          :key="index"
          @click="toggleIngredientCheck(item)"
        >
          <view class="check-box" :class="{ checked: item.checked }">
            <text class="check-icon" v-if="item.checked">✓</text>
          </view>
          <text class="ingredient-name" :class="{ checked: item.checked }">{{ item.name }}</text>
          <text class="ingredient-amount">{{ item.amount }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 食材列表
  ingredients: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['check'])

/**
 * 食材分组
 */
const ingredientGroups = computed(() => {
  const groups = []
  const currentGroup = { name: '', items: [] }

  props.ingredients.forEach(item => {
    if (item.isGroup) {
      if (currentGroup.items.length > 0) {
        groups.push({ ...currentGroup })
      }
      groups.push({ name: item.name, items: [] })
    } else {
      if (groups.length === 0) {
        currentGroup.items.push(item)
      } else {
        groups[groups.length - 1].items.push(item)
      }
    }
  })

  if (currentGroup.items.length > 0) {
    groups.unshift(currentGroup)
  }

  return groups.filter(g => g.items.length > 0)
})

/**
 * 切换食材勾选状态
 */
const toggleIngredientCheck = (item) => {
  item.checked = !item.checked
  emit('check', item)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.ingredients-list {
  @include flex-center-column;
  gap: $spacing-md;
}

.ingredient-group {
  width: 100%;
}

.group-title {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  margin-bottom: $spacing-sm;
  padding-left: $spacing-md;
}

.ingredient-items {
  background-color: $bg-color-white;
  border-radius: $border-radius-base;
  overflow: hidden;
}

.ingredient-item {
  @include flex-center;
  padding: $spacing-md;
  background-color: $bg-color-white;
  border-bottom: 1rpx solid $border-color-lighter;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background-color: $bg-color-base;
  }
}

.check-box {
  width: 36rpx;
  height: 36rpx;
  @include flex-center;
  border: 2rpx solid $border-color;
  border-radius: $border-radius-sm;
  flex-shrink: 0;
  margin-right: $spacing-md;

  &.checked {
    background-color: $primary-color;
    border-color: $primary-color;
  }
}

.check-icon {
  font-size: $font-size-sm;
  color: #fff;
}

.ingredient-name {
  flex: 1;
  font-size: $font-size-base;
  color: $text-color-primary;

  &.checked {
    text-decoration: line-through;
    color: $text-color-placeholder;
  }
}

.ingredient-amount {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}
</style>
