<template>
  <el-dialog v-model="visible" title="选择好友" width="600px">
    <div class="friend-grid">
      <div
        v-for="friend in friends"
        :key="friend.id"
        class="friend-item"
        :class="{ selected: selectedMembers.includes(friend.id) }"
        @click="$emit('toggle', friend)"
      >
        <div class="friend-avatar">{{ friend.avatar }}</div>
        <div class="friend-name">{{ friend.name }}</div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleConfirm">确定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  friends: {
    type: Array,
    default: () => []
  },
  selectedMembers: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'confirm', 'toggle'])

const visible = ref(props.modelValue)

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const handleConfirm = () => {
  emit('confirm')
  visible.value = false
}

const handleCancel = () => {
  visible.value = false
}
</script>

<style scoped lang="less">
.friend-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 20px;
  max-height: 400px;
  overflow-y: auto;
  padding: 20px 0;

  .friend-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 15px;
    border: 2px solid #e4e7ed;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      border-color: #409eff;
      background-color: #ecf5ff;
    }

    &.selected {
      border-color: #67c23a;
      background-color: #f0f9eb;
    }
  }

  .friend-avatar {
    font-size: 2.857rem /* 原值: 40px */;
    margin-bottom: 10px;
  }

  .friend-name {
    font-size: 1rem /* 原值: 14px */;
    font-weight: 500;
  }
}
</style>
