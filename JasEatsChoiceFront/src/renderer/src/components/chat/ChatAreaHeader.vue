<template>
  <div class="chat-area-header">
    <div class="conversation-info">
      <div class="name-info">
        <span class="name">{{ conversation.name }}</span>
        <span v-if="conversation.type === 'group'" class="member-count">
          ({{ conversation.memberCount || '0' }}人)
        </span>
      </div>
    </div>

    <div class="header-actions">
      <!-- 消息搜索 -->
      <el-input
        v-model="searchKeyword"
        placeholder="搜索消息记录"
        size="small"
        style="width: 200px; margin-right: 10px"
        @input="$emit('search', searchKeyword)"
        clearable
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <!-- 导出聊天记录 -->
      <el-button size="small" @click="$emit('export')" style="margin-right: 10px">
        <el-icon><Download /></el-icon> 导出记录
      </el-button>

      <!-- 群聊操作 -->
      <div class="chat-actions" v-if="conversation.type === 'group'">
        <el-button
          type="primary"
          size="small"
          @click="$emit('create-group-order')"
          v-if="!hasGroupOrder"
        >
          创建群订单
        </el-button>
        <el-button size="small" @click="$emit('join-group-order')">加入群订单</el-button>
        <el-button size="small" @click="$emit('show-group-detail')">群聊详情</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Search, Download } from '@element-plus/icons-vue'

const props = defineProps({
  conversation: {
    type: Object,
    required: true
  },
  hasGroupOrder: {
    type: Boolean,
    default: false
  }
})

const searchKeyword = ref('')

defineEmits(['search', 'export', 'create-group-order', 'join-group-order', 'show-group-detail'])
</script>

<style scoped lang="less">
.chat-area-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
  background-color: #fafafa;

  .conversation-info {
    .name-info {
      .name {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }

      .member-count {
        font-size: 12px;
        color: #909399;
        margin-left: 4px;
      }
    }
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 8px;

    .chat-actions {
      display: flex;
      gap: 8px;
    }
  }
}
</style>
