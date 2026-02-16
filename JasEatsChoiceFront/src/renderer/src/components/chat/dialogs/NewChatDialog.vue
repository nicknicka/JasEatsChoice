<template>
  <el-dialog v-model="visible" title="新建聊天" width="400px" @close="handleClose">
    <el-input
      v-model="searchQuery"
      placeholder="搜索好友"
      @input="handleSearch"
      style="margin-bottom: 15px"
    >
      <template #append>
        <el-button :icon="Search" @click="handleSearch"></el-button>
      </template>
    </el-input>

    <div class="friend-list">
      <div
        v-for="friend in filteredFriends"
        :key="friend.id"
        class="friend-item"
        :class="{ disabled: isFriendInConversation(friend) }"
        @click="!isFriendInConversation(friend) && selectFriend(friend)"
      >
        <div class="friend-avatar">{{ friend.avatar }}</div>
        <div class="friend-info">
          <div class="friend-name">{{ friend.name }}</div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  friends: {
    type: Array,
    default: () => []
  },
  conversations: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'select'])

const visible = ref(props.modelValue)
const searchQuery = ref('')

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const filteredFriends = computed(() => {
  if (!searchQuery.value) {
    return props.friends
  }
  return props.friends.filter(friend =>
    friend.name.includes(searchQuery.value)
  )
})

const handleSearch = () => {
  // 搜索逻辑已在 computed 中处理
}

const isFriendInConversation = (friend) => {
  return props.conversations.some(
    (conv) => (conv.id === friend.id && conv.type === 'friend') || conv.type === 'private'
  )
}

const selectFriend = (friend) => {
  emit('select', friend)
  searchQuery.value = ''
}

const handleClose = () => {
  visible.value = false
  searchQuery.value = ''
}
</script>

<style scoped lang="less">
.friend-list {
  max-height: 300px;
  overflow-y: auto;

  .friend-item {
    display: flex;
    align-items: center;
    padding: 12px;
    cursor: pointer;
    border-radius: 4px;
    transition: background-color 0.3s ease;

    &:hover {
      background-color: #f5f7fa;
    }

    &.disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }

  .friend-avatar {
    font-size: 2rem /* 原值: 28px */;
    margin-right: 12px;
  }

  .friend-info {
    flex: 1;

    .friend-name {
      font-weight: 500;
      font-size: 1rem /* 原值: 14px */;
    }
  }
}
</style>
