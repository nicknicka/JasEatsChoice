<template>
  <view class="section-card">
    <!-- 区块头部（标题 + 查看全部） -->
    <view
      class="section-header clickable"
      v-if="title || showMore"
      @click="handleMoreClick"
    >
      <text class="section-title" v-if="title">{{ title }}</text>
      <view class="section-more" v-if="showMore">
        <text class="more-text">查看全部</text>
        <uni-icons
          type="right"
          size="14"
          color="$text-color-secondary"
        ></uni-icons>
      </view>
    </view>

    <!-- 内容插槽 -->
    <slot></slot>
  </view>
</template>

<script setup>
// Props
const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  showMore: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['more'])

/**
 * 点击"查看全部"
 */
const handleMoreClick = () => {
  if (props.showMore) {
    emit('more')
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.section-card {
  background-color: $bg-color-white;
  margin: 0 $spacing-md $spacing-md;
  border-radius: $border-radius-lg;
  padding: $spacing-md;
  box-shadow: $box-shadow-sm;
}

.section-header {
  @include flex-between;
  align-items: center;
  margin-bottom: $spacing-md;
  cursor: pointer;
  transition: all 0.3s ease;

  // 可点击反馈
  &:active {
    opacity: 0.7;
  }
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.section-more {
  @include flex-center;
  gap: $spacing-xs;

  .more-text {
    font-size: $font-size-sm;
    color: $text-color-secondary;
  }
}
</style>
