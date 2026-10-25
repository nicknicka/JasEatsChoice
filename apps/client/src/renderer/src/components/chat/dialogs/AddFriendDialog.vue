<template>
  <el-dialog
    v-model="visible"
    title="添加好友"
    :width="selectedUser ? '800px' : '500px'"
    @close="handleClose"
  >
    <div class="add-friend-container" :style="{ display: 'flex', height: '500px' }">
      <!-- 左侧搜索区域 -->
      <div
        :style="{
          flex: selectedUser ? '1' : 'auto',
          width: selectedUser ? '50%' : '100%',
          borderRight: selectedUser ? '1px solid #eee' : 'none',
          paddingRight: selectedUser ? '15px' : '0',
          overflowY: 'auto'
        }"
      >
        <!-- 搜索栏 -->
        <div class="search-bar">
          <el-dropdown trigger="click" @command="handleSearchTypeChange">
            <el-button size="small">
              {{ getSearchTypeLabel(searchType) }}
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="nickname">用户名/昵称</el-dropdown-item>
                <el-dropdown-item command="phone">手机号</el-dropdown-item>
                <el-dropdown-item command="email">邮箱</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <el-input
            v-model="searchQuery"
            placeholder="搜索内容"
            style="flex: 1; margin-left: 8px"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" type="primary" size="small" @click="handleSearch" />
            </template>
          </el-input>
        </div>

        <!-- 搜索结果 -->
        <div v-if="searchResults.length === 0" class="empty-result">
          暂无搜索结果
        </div>
        <div class="user-list" v-else>
          <transition-group name="slide-down" tag="div">
            <div
              v-for="user in paginatedUsers"
              :key="user.id"
              class="user-item"
              :class="{ selected: selectedUser?.id === user.id }"
              @click="handleSelectUser(user)"
            >
              <div class="user-avatar">{{ user.avatar }}</div>
              <div class="user-info">
                <div class="user-name">
                  {{ getUserName(user) }}
                </div>
                <div class="user-detail" v-if="shouldShowEmail(user)">
                  <span class="detail-label">邮箱: </span>{{ user.email }}
                </div>
                <div class="user-detail" v-if="shouldShowPhone(user)">
                  <span class="detail-label">手机号: </span>{{ user.phone }}
                </div>
              </div>
              <el-button type="primary" size="small" @click.stop="handleAddFriend(user)">
                加好友
              </el-button>
            </div>
          </transition-group>
        </div>

        <!-- 分页 -->
        <div v-if="searchResults.length > pageSize" class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[7, 14, 21]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="searchResults.length"
          />
        </div>
      </div>

      <!-- 右侧用户详情 -->
      <div v-if="selectedUser" class="user-detail-panel">
        <div class="user-detail-header">
          <div class="detail-avatar">{{ selectedUser.avatar }}</div>
          <div class="detail-name">
            {{ selectedUser.nickname || selectedUser.username }}
          </div>
          <el-button type="primary" size="small" @click="handleAddFriend(selectedUser)">
            加好友
          </el-button>
        </div>

        <div class="detail-info">
          <div class="detail-item">
            <label>用户名/昵称:</label>
            <span>{{ selectedUser.nickname || '未设置' }}</span>
          </div>
          <div class="detail-item">
            <label>手机号:</label>
            <span>{{ selectedUser.phone || '未绑定' }}</span>
          </div>
          <div class="detail-item">
            <label>邮箱:</label>
            <span>{{ selectedUser.email || '未绑定' }}</span>
          </div>
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
import { ref, watch, computed } from 'vue'
import { Search, ArrowDown } from '@element-plus/icons-vue'

/**
 * 添加好友对话框组件
 * @description 支持多种搜索方式（用户名/昵称、手机号、邮箱）
 */
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  userId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'search', 'addFriend'])

const visible = ref(props.modelValue)
const searchQuery = ref('')
const searchType = ref('nickname')
const searchResults = ref([])
const selectedUser = ref(null)
const currentPage = ref(1)
const pageSize = ref(7)

/**
 * 分页后的用户列表
 */
const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return searchResults.value.slice(start, end)
})

/**
 * 获取搜索类型标签
 */
const getSearchTypeLabel = (type) => {
  const labels = {
    nickname: '用户名/昵称',
    phone: '手机号',
    email: '邮箱'
  }
  return labels[type] || '用户名/昵称'
}

/**
 * 获取用户显示名称
 */
const getUserName = (user) => {
  if (searchType.value === 'email') return user.email
  if (searchType.value === 'phone') return user.phone
  return user.nickname || user.username
}

/**
 * 是否显示邮箱
 */
const shouldShowEmail = (user) => {
  return searchType.value !== 'email' && user.email
}

/**
 * 是否显示手机号
 */
const shouldShowPhone = (user) => {
  return searchType.value !== 'phone' && user.phone
}

/**
 * 搜索类型变更
 */
const handleSearchTypeChange = (command) => {
  searchType.value = command
  if (searchQuery.value) {
    handleSearch()
  }
}

/**
 * 搜索用户
 */
const handleSearch = () => {
  if (!searchQuery.value) {
    searchResults.value = []
    return
  }

  emit('search', {
    keyword: searchQuery.value,
    searchType: searchType.value
  })
}

/**
 * 处理搜索结果
 */
const handleSearchResults = (results) => {
  searchResults.value = results
  currentPage.value = 1
}

/**
 * 选择用户
 */
const handleSelectUser = (user) => {
  if (selectedUser.value?.id === user.id) {
    selectedUser.value = null
  } else {
    selectedUser.value = user
  }
}

/**
 * 添加好友
 */
const handleAddFriend = (user) => {
  emit('addFriend', user)
}

/**
 * 关闭对话框
 */
const handleClose = () => {
  visible.value = false
  // 重置状态
  searchQuery.value = ''
  searchResults.value = []
  selectedUser.value = null
  currentPage.value = 1
  emit('update:modelValue', false)
}

/**
 * 监听外部 modelValue 变化
 */
watch(() => props.modelValue, (newVal) => {
  visible.value = newVal
  if (newVal) {
    // 对话框打开时重置状态
    searchQuery.value = ''
    searchResults.value = []
    selectedUser.value = null
    currentPage.value = 1
  }
})

/**
 * 监听内部 visible 变化
 */
watch(visible, (newVal) => {
  if (!newVal) {
    emit('update:modelValue', false)
  }
})

/**
 * 暴露方法供父组件调用
 */
defineExpose({
  handleSearchResults
})
</script>

<style scoped lang="less">
.add-friend-container {
  .search-bar {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
  }

  .empty-result {
    margin: 20px 0;
    text-align: center;
    color: #999;
  }

  .user-list {
    .user-item {
      display: flex;
      align-items: center;
      padding: 12px;
      border: 1px solid #e4e7ed;
      border-radius: 8px;
      margin-bottom: 12px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        background-color: #f5f7fa;
        border-color: #409eff;
      }

      &.selected {
        background-color: #ecf5ff;
        border-color: #409eff;
      }

      .user-avatar {
        width: 50px;
        height: 50px;
        font-size: 2rem /* 原值: 28px */;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 12px;
        background-color: #f0f0f0;
        border-radius: 50%;
      }

      .user-info {
        flex: 1;

        .user-name {
          font-size: 1rem /* 原值: 14px */;
          font-weight: 600;
          color: #303133;
          margin-bottom: 4px;
        }

        .user-detail {
          font-size: 0.857rem /* 原值: 12px */;
          color: #909399;

          .detail-label {
            font-weight: 600;
          }
        }
      }
    }
  }

  .pagination {
    text-align: center;
    margin-top: 15px;
  }

  .user-detail-panel {
    flex: 1;
    padding-left: 15px;

    .user-detail-header {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20px 0;
      border-bottom: 1px solid #e4e7ed;

      .detail-avatar {
        width: 80px;
        height: 80px;
        font-size: 3.429rem /* 原值: 48px */;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #f0f0f0;
        border-radius: 50%;
        margin-bottom: 12px;
      }

      .detail-name {
        font-size: 1.286rem /* 原值: 18px */;
        font-weight: 600;
        margin-bottom: 12px;
      }
    }

    .detail-info {
      padding: 20px 0;

      .detail-item {
        display: flex;
        margin-bottom: 16px;

        label {
          font-weight: 600;
          color: #606266;
          width: 100px;
        }

        span {
          color: #303133;
        }
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}

.slide-down-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.slide-down-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
