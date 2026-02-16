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
        </div>
      </div>
    </div>

    <div class="header-actions">
      <!-- 搜索按钮（带图标） -->
      <div class="search-button-wrapper">
        <el-tooltip content="搜索消息" placement="bottom">
          <el-button
            :icon="Search"
            circle
            size="small"
            @click="toggleSearch"
            :class="{ 'is-active': showSearch }"
          />
        </el-tooltip>

        <!-- 搜索展开面板 -->
        <transition name="slide-left">
          <div v-if="showSearch" class="search-panel-outer">
            <div class="search-panel">
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
          </div>
        </transition>
      </div>

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

      <!-- 置顶标签 -->
      <el-tag v-if="conversation.pinned" size="small" type="warning" effect="plain" class="pinned-tag">置顶</el-tag>

      <!-- 群聊快速操作 -->
      <div class="group-quick-actions" v-if="conversation.type === 'group'">
        <el-tooltip content="创建群订单" placement="bottom">
          <el-button
            :icon="ShoppingCart"
            circle
            size="small"
            type="primary"
            v-if="!hasGroupOrder"
            @click="handleCreateGroupOrder"
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

// 处理创建群订单
const handleCreateGroupOrder = () => {
  console.log('🟢 [ChatAreaHeader] 创建群订单按钮被点击')
  console.log('🟢 [ChatAreaHeader] conversation:', props.conversation)
  emit('create-group-order')
}
</script>

<style scoped lang="less">
.chat-area-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-bottom: 1px solid #e8ecef;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.03);
  transition: all 0.3s ease;

  .conversation-info {
    display: flex;
    align-items: center;
    gap: 12px;
    flex: 1;
    min-height: 50px;
    position: relative;
    z-index: 101;

    .conversation-avatar {
      width: 42px;
      height: 42px;
      border-radius: 8px;
      overflow: hidden;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
      transition: all 0.3s ease;
      flex-shrink: 0;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      span {
        font-size: 1.286rem /* 原值: 18px */;
      }

      &:hover {
        transform: scale(1.05);
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
      }
    }

    .name-info {
      display: flex;
      flex-direction: column;
      gap: 2px;
      min-width: 0;
      flex: 1;

      .name-row {
        display: flex;
        align-items: center;
        gap: 6px;

        .name {
          font-size: 1.071rem /* 原值: 15px */;
          font-weight: 600;
          color: #1a1a1a;
          letter-spacing: 0.2px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          max-width: 200px;
        }

        .member-count {
          font-size: 0.857rem /* 原值: 12px */;
          color: #8b949e;
          font-weight: 500;
          flex-shrink: 0;
        }
      }

    }
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 8px;

    .search-button-wrapper {
      position: relative;
      display: flex;
      align-items: center;
    }

    .pinned-tag {
      font-size: 0.857rem /* 原值: 12px */;
      padding: 2px 8px;
      height: 22px;
      line-height: 18px;
      border-radius: 4px;
      font-weight: 500;
    }

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
      gap: 8px;
      margin-left: 8px;
      padding-left: 8px;
      border-left: 2px solid #e8ecef;
    }
  }

  .search-panel-outer {
    position: absolute;
    right: 100%;
    top: 50%;
    transform: translateY(-50%);
    margin-right: 8px;
    min-width: 180px;
    max-width: 320px;
    width: auto;
    z-index: 100;
  }

  .search-panel {
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

@keyframes slideLeft {
  from {
    opacity: 0;
    transform: translate(10px, -50%);
  }
  to {
    opacity: 1;
    transform: translate(0, -50%);
  }
}

.slide-left-enter-active {
  animation: slideLeft 0.25s ease;
}

.slide-left-leave-active {
  animation: slideLeft 0.2s ease reverse;
}
</style>
