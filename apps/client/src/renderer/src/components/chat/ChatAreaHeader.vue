<template>
  <div class="chat-area-header">
    <div class="conversation-info">
      <!-- 头像 -->
      <div class="conversation-avatar">
        <img v-if="imageAvatar" :src="imageAvatar" alt="" />
        <span v-else>{{ conversation.type === 'group' ? '👥' : '💬' }}</span>
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
            class="header-icon-btn"
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
        <el-button :icon="MoreFilled" circle size="small" class="header-icon-btn" />
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
            class="header-icon-btn order-shortcut"
            v-if="!hasGroupOrder"
            @click="handleCreateGroupOrder"
          />
        </el-tooltip>
        <el-tooltip content="群聊详情" placement="bottom">
          <el-button
            :icon="InfoFilled"
            circle
            size="small"
            class="header-icon-btn"
            @click="$emit('show-group-detail')"
          />
        </el-tooltip>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { getAvatarUrl } from '@/utils/avatar'
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

const normalizeImageAvatar = (avatar) => {
  if (!avatar) return ''

  const normalizedAvatar = String(avatar).replace(/\\/g, '/').trim()
  if (/^https?:/.test(normalizedAvatar) || /^data:image/.test(normalizedAvatar)) {
    return normalizedAvatar
  }

  return getAvatarUrl(normalizedAvatar)
}

const imageAvatar = computed(() => normalizeImageAvatar(props.conversation.avatar))

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
@import '../../assets/css/nordic-theme.less';

.chat-area-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  background:
    linear-gradient(180deg, #fffdfb 0%, #fbf7f1 100%);
  border-bottom: 1px solid @nordic-border;
  box-shadow: inset 0 -1px 0 rgba(255, 255, 255, 0.6);
  transition: all @nordic-transition-base ease;

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
      border-radius: 14px;
      overflow: hidden;
      display: flex;
      align-items: center;
      justify-content: center;
      background:
        radial-gradient(circle at 28% 28%, #f6fbf5 0%, #e5efe6 56%, #d4e2d6 100%);
      border: 1px solid fade(@nordic-green, 20%);
      box-shadow: 0 10px 18px rgba(76, 122, 77, 0.08);
      transition: all @nordic-transition-base ease;
      flex-shrink: 0;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      span {
        font-size: 18px;
      }

      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 14px 24px rgba(76, 122, 77, 0.12);
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
            font-size: @nordic-text-md;
            font-weight: 700;
            color: @nordic-text;
            letter-spacing: @nordic-letter-tight;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            max-width: 200px;
          }

          .member-count {
            font-size: @nordic-text-sm;
            color: @nordic-text-muted;
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

    .header-icon-btn {
      border: 1px solid @nordic-border;
      background: rgba(255, 255, 255, 0.9);
      color: @nordic-text-secondary;
      box-shadow: 0 8px 16px rgba(105, 78, 57, 0.06);
      transition: all @nordic-transition-fast ease;

      &:hover {
        color: @nordic-accent-dark;
        border-color: fade(@nordic-accent, 35%);
        background: #fff9f4;
        box-shadow: 0 12px 20px fade(@nordic-accent, 14%);
        transform: translateY(-1px);
      }

      &.is-active {
        color: @nordic-accent-dark;
        border-color: fade(@nordic-accent, 38%);
        background: #f9ede4;
      }
    }

    .order-shortcut {
      background: linear-gradient(180deg, @nordic-accent 0%, @nordic-accent-dark 100%);
      border-color: transparent;
      color: @nordic-surface;

      &:hover {
        color: @nordic-surface;
        border-color: transparent;
        background: linear-gradient(180deg, lighten(@nordic-accent, 4%) 0%, @nordic-accent-dark 100%);
      }
    }

    .search-button-wrapper {
      position: relative;
      display: flex;
      align-items: center;
    }

    .pinned-tag {
      font-size: @nordic-text-sm;
      padding: 2px 8px;
      height: 22px;
      line-height: 18px;
      border-radius: 999px;
      font-weight: 600;
      border-color: fade(@nordic-yellow, 28%);
      color: @nordic-yellow-dark;
      background: @nordic-yellow-light;
    }

    .group-quick-actions {
      display: flex;
      gap: 8px;
      margin-left: 8px;
      padding-left: 8px;
      border-left: 1px solid @nordic-border;
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
        border-radius: 12px;
        box-shadow: 0 12px 24px rgba(94, 70, 48, 0.08);
        border: 1px solid @nordic-border;
        transition: all @nordic-transition-fast ease;

        &:hover,
        &.is-focus {
          border-color: fade(@nordic-accent, 35%);
          box-shadow: 0 14px 28px fade(@nordic-accent, 14%);
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
