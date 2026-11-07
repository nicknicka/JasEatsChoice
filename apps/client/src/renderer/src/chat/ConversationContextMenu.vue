<template>
  <div
    v-if="visible && conversation"
    class="context-menu"
    :style="{ left: position.x + 'px', top: position.y + 'px' }"
    @click.stop
  >
    <div class="menu-item" @click="$emit('toggle-pin', conversation)">
      {{ conversation.pinned ? '取消置顶' : '置顶会话' }}
    </div>
    <div class="menu-item" @click="$emit('delete', conversation)" style="color: #ff4d4f">
      删除会话
    </div>
  </div>
</template>

<script setup>
defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  conversation: {
    type: Object,
    default: null
  },
  position: {
    type: Object,
    default: () => ({ x: 0, y: 0 })
  }
})

defineEmits(['toggle-pin', 'delete'])
</script>

<style scoped lang="less">
.context-menu {
  position: fixed;
  z-index: 10000;
  background-color: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  min-width: 160px;
  padding: 8px 0;

  .menu-item {
    padding: 10px 20px;
    cursor: pointer;
    font-size: 1rem /* 原值: 14px */;
    color: #303133;
    white-space: nowrap;
    transition: background-color 0.3s ease;

    &:hover {
      background-color: #f5f7fa;
    }
  }
}
</style>
