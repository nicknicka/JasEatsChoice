<template>
  <div class="chat-area-header">
    <div class="conversation-info">
      <!-- 头像 -->
      <div class="conversation-avatar">
        <img v-if="isImageAvatar(conversation.avatar)" :src="conversation.avatar" alt="" />
        <span v-else>{{ conversation.avatar || (conversation.type === 'group' ? '👥' : '💬') }}</span>
      </div>

      <!-- 名称和信息 -->
      <div class="name-info">
        <div class="name-row">
          <span class="name">{{ conversation.name }}</span>
          <span v-if="conversation.type === 'group'" class="member-count">
            ({{ conversation.memberCount || '0' }}人)
          </span>
          <el-tag v-if="conversation.pinned" size="small" type="warning" effect="plain">置顶</el-tag>
        </div>
        <div class="status-info">
          <span class="online-status"></span>
          <span class="status-text">{{ getStatusText() }}</span>
        </div>
      </div>
    </div>

    <div class="header-actions">
      <!-- 搜索按钮（带图标） -->
      <el-tooltip content="搜索消息" placement="bottom">
        <el-button
          :icon="Search"
          circle
          size="small"
          @click="toggleSearch"
          :class="{ 'is-active': showSearch }"
        />
      </el-tooltip>

      <!-- 更多操作下拉菜单 -->
      <el-dropdown trigger="click" @command="handleCommand">
        <el-button :icon="MoreFilled" circle size="small" />
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="search">
              <el-icon><Search /></el-icon> 搜索消息
            </el-dropdown-item>
            <el-dropdown-item command="export">
              <el-icon><Download /></el-icon> 导出记录
            </el-dropdown-item>
            <el-dropdown-item command="clear" divided>
              <el-icon><Delete /></el-icon> 清空记录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <!-- 群聊快速操作 -->
      <div class="group-quick-actions" v-if="conversation.type === 'group'">
        <el-tooltip content="创建群订单" placement="bottom">
          <el-button
            :icon="ShoppingCart"
            circle
            size="small"
            type="primary"
            v-if="!hasGroupOrder"
            @click="$emit('create-group-order')"
          />
        </el-tooltip>
        <el-tooltip content="群聊详情" placement="bottom">
          <el-button
            :icon="InfoFilled"
            circle
            size="small"
            @click="$emit('show-group-detail')"
          />
        </el-tooltip>
      </div>
    </div>

    <!-- 搜索展开面板 -->
    <transition name="slide-down">
      <div v-if="showSearch" class="search-panel">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索消息记录..."
          size="default"
          @input="$emit('search', searchKeyword)"
          clearable
          autofocus
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Download, Delete, MoreFilled, ShoppingCart, InfoFilled } from '@element-plus/icons-vue'

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

const emit = defineEmits(['search', 'export', 'clear', 'create-group-order', 'join-group-order', 'show-group-detail'])

const searchKeyword = ref('')
const showSearch = ref(false)

// 判断头像是否为图片
const isImageAvatar = (avatar) => {
  if (!avatar) return false
  return avatar.match(/^https?:/) || avatar.match(/^data:image/)
}

// 获取状态文本
const getStatusText = () => {
  if (props.conversation.type === 'group') {
    return '群聊'
  }
  // 这里可以根据实际情况判断在线状态
  return '在线'
}

// 切换搜索面板
const toggleSearch = () => {
  showSearch.value = !showSearch.value
  if (showSearch.value) {
    searchKeyword.value = ''
    emit('search', '')
  }
}

// 处理下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'search':
      showSearch.value = !showSearch.value
      break
    case 'export':
      emit('export')
      ElMessage.success('正在导出聊天记录...')
      break
    case 'clear':
      emit('clear')
      ElMessage.success('聊天记录已清空')
      break
  }
}
</script>

<style scoped lang="less">
.chat-area-header {
  display: flex;
  flex-direction: column;
  padding: 6px 10px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-bottom: 1px solid #e8ecef;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.03);
  transition: all 0.3s ease;

  .conversation-info {
    display: flex;
    align-items: center;
    gap: 10px;
    flex: 1;

    .conversation-avatar {
      width: 29px;
      height: 29px;
      border-radius: 6px;
      overflow: hidden;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      box-shadow: 0 2px 5px rgba(102, 126, 234, 0.15);
      transition: all 0.3s ease;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      span {
        font-size: 14px;
      }

      &:hover {
        transform: scale(1.03);
        box-shadow: 0 3px 10px rgba(102, 126, 234, 0.25);
      }
    }

    .name-info {
      display: flex;
      flex-direction: column;
      gap: 2px;

      .name-row {
        display: flex;
        align-items: center;
        gap: 6px;

        .name {
          font-size: 11px;
          font-weight: 600;
          color: #1a1a1a;
          letter-spacing: 0.2px;
        }

        .member-count {
          font-size: 9px;
          color: #8b949e;
          font-weight: 500;
        }
      }

      .status-info {
        display: flex;
        align-items: center;
        gap: 4px;

        .online-status {
          width: 4px;
          height: 4px;
          border-radius: 50%;
          background: linear-gradient(135deg, #10b981 0%, #059669 100%);
          box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.1);
          animation: pulse 2s infinite;
        }

        .status-text {
          font-size: 9px;
          color: #8b949e;
        }
      }
    }
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 6px;

    :deep(.el-button) {
      border: 1px solid #e8ecef;
      background: #ffffff;
      color: #5a6c7d;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

      &:hover {
        border-color: #667eea;
        color: #667eea;
        transform: translateY(-1px);
        box-shadow: 0 3px 8px rgba(102, 126, 234, 0.15);
      }

      &.is-active {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-color: #667eea;
        color: #fff;
      }
    }

    .group-quick-actions {
      display: flex;
      gap: 6px;
      margin-left: 6px;
      padding-left: 6px;
      border-left: 2px solid #e8ecef;
    }
  }

  .search-panel {
    margin-top: 5px;
    animation: slideDown 0.3s ease;

    :deep(.el-input) {
      .el-input__wrapper {
        border-radius: 10px;
        box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);
        border: 2px solid transparent;
        transition: all 0.3s ease;

        &:hover,
        &.is-focus {
          border-color: #667eea;
          box-shadow: 0 2px 10px rgba(102, 126, 234, 0.12);
        }
      }
    }
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.1);
  }
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.slide-down-enter-active {
  animation: slideDown 0.25s ease;
}

.slide-down-leave-active {
  animation: slideDown 0.2s ease reverse;
}
</style>
