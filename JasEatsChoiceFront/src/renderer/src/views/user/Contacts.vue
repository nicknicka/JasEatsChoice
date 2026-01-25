<template>
  <div class="contacts-container">
    <!-- 页面头部 -->
    <div class="contacts-header">
      <h2 class="page-title">
        <span class="title-icon">👥</span>
        通讯录
      </h2>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="openAddFriendDialog">
          添加好友
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-filter-section">
      <div class="search-box">
        <el-input
          v-model="searchQuery"
          placeholder="搜索好友（姓名/ID）"
          :prefix-icon="Search"
          clearable
          @input="handleSearch"
        />
      </div>

      <!-- 筛选选项 -->
      <div class="filter-options">
        <el-radio-group v-model="filterType" size="small" @change="handleFilter">
          <el-radio-button label="all">全部 ({{ totalCount }})</el-radio-button>
          <el-radio-button label="recent">最近联系 ({{ recentCount }})</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 内容区域：好友列表 + 字母索引 -->
    <div v-if="!loading && filteredFriends.length > 0" class="contacts-content">
      <!-- 好友列表 -->
      <div class="friends-list-wrapper">
        <!-- 按字母分组的好友列表 -->
        <div class="friends-list-scroll">
          <div v-for="(group, letter) in groupedFriends" :key="letter" class="friend-group">
            <!-- 字母标题 -->
            <div class="group-letter">{{ letter }}</div>

            <!-- 该字母下的好友 -->
            <div
              v-for="friend in group"
              :key="friend.id"
              class="friend-item"
              :class="{ 'has-recent-chat': friend.recentChatTime }"
              @click="startChat(friend)"
              @contextmenu.prevent="showContextMenu($event, friend)"
            >
              <!-- 头像 -->
              <div class="friend-avatar">
                <img v-if="isImageAvatar(friend.avatar)" :src="friend.avatar" alt="头像" />
                <span v-else class="avatar-emoji">{{ friend.avatar || '👤' }}</span>
                <!-- 最近聊天标签 -->
                <div v-if="friend.recentChatTime" class="recent-chat-badge" :title="`最近: ${formatRecentTime(friend.recentChatTime)}`">
                  💬
                </div>
              </div>

              <!-- 好友信息 -->
              <div class="friend-info">
                <div class="friend-name-row">
                  <span class="friend-name">{{ friend.name }}</span>
                  <span v-if="friend.recentChatTime" class="recent-time">
                    {{ formatRecentTime(friend.recentChatTime) }}
                  </span>
                </div>
                <div class="friend-id">ID: {{ friend.id }}</div>
              </div>

              <!-- 快捷操作按钮 -->
              <div class="friend-quick-actions">
                <el-button
                  type="primary"
                  :icon="ChatDotRound"
                  circle
                  size="small"
                  class="chat-btn"
                  @click.stop="startChat(friend)"
                />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 字母索引导航 -->
      <div class="alphabet-index">
        <div
          v-for="letter in availableLetters"
          :key="letter"
          class="alphabet-item"
          :class="{ active: activeLetter === letter }"
          @click="scrollToLetter(letter)"
        >
          {{ letter }}
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-else-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-container" @click="handleEmptyClick">
      <div class="empty-icon">👥</div>
      <p class="empty-title">{{ emptyText }}</p>
      <p v-if="!searchQuery && filterType === 'all'" class="empty-tip">点击此处或上方"添加好友"按钮</p>
      <el-button v-if="searchQuery || filterType !== 'all'" type="primary" @click.stop="resetFilter">
        清除筛选
      </el-button>
    </div>

    <!-- 右键菜单 -->
    <div
      v-if="contextMenuVisible"
      class="context-menu"
      :style="{ top: contextMenuPosition.y + 'px', left: contextMenuPosition.x + 'px' }"
      @click="closeContextMenu"
    >
      <div class="context-menu-item" @click="viewFriendDetail(contextMenuFriend)">
        <el-icon><User /></el-icon>
        <span>查看详情</span>
      </div>
      <div class="context-menu-item" @click="startChat(contextMenuFriend)">
        <el-icon><ChatDotRound /></el-icon>
        <span>开始聊天</span>
      </div>
      <div class="context-menu-divider"></div>
      <div class="context-menu-item danger" @click="deleteFriend(contextMenuFriend)">
        <el-icon><Delete /></el-icon>
        <span>删除好友</span>
      </div>
    </div>

    <!-- 添加好友对话框 -->
    <el-dialog
      v-model="addFriendDialogVisible"
      title="添加好友"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="add-friend-content">
        <!-- 搜索类型选择 -->
        <div class="search-type-selector">
          <el-radio-group v-model="friendSearchType" size="small">
            <el-radio-button label="nickname">昵称</el-radio-button>
            <el-radio-button label="phone">手机号</el-radio-button>
            <el-radio-button label="email">邮箱</el-radio-button>
          </el-radio-group>
        </div>

        <!-- 搜索输入框 -->
        <el-input
          v-model="friendSearchKeyword"
          placeholder="请输入搜索关键词"
          clearable
          @keyup.enter="handleFriendSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="handleFriendSearch">搜索</el-button>
          </template>
        </el-input>

        <!-- 搜索结果 -->
        <div v-if="searchResults.length > 0" class="search-results">
          <div v-for="user in searchResults" :key="user.id" class="result-item">
            <div class="user-avatar">
              <img v-if="isImageAvatar(user.avatar)" :src="user.avatar" alt="" />
              <span v-else>{{ user.avatar || '👤' }}</span>
            </div>
            <div class="user-info">
              <div class="user-name">{{ user.name }}</div>
              <div v-if="user.phone" class="user-detail">手机: {{ user.phone }}</div>
              <div v-if="user.email" class="user-detail">邮箱: {{ user.email }}</div>
            </div>
            <el-button
              type="primary"
              :loading="user.adding"
              :disabled="user.added"
              @click="sendFriendRequest(user)"
            >
              {{ user.added ? '已发送' : '添加' }}
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 查看好友详情对话框 -->
    <el-dialog
      v-model="friendDetailDialogVisible"
      title="好友详情"
      width="400px"
    >
      <div v-if="selectedFriend" class="friend-detail">
        <div class="detail-avatar">
          <img v-if="isImageAvatar(selectedFriend.avatar)" :src="selectedFriend.avatar" alt="" />
          <span v-else class="avatar-emoji">{{ selectedFriend.avatar || '👤' }}</span>
        </div>
        <div class="detail-info">
          <div class="info-item">
            <span class="label">昵称：</span>
            <span class="value">{{ selectedFriend.name }}</span>
          </div>
          <div class="info-item">
            <span class="label">用户ID：</span>
            <span class="value">{{ selectedFriend.id }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="friendDetailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="startChatFromDetail">开始聊天</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Plus,
  ChatDotRound,
  User,
  Delete
} from '@element-plus/icons-vue'
import api from '../../utils/api'
import { decodeJwt } from '../../utils/api'
import { useAuthStore } from '../../store/authStore'

const router = useRouter()
const authStore = useAuthStore()

// ========== 状态管理 ==========
const loading = ref(true)
const searchQuery = ref('')
const filterType = ref('all')
const friends = ref([])
const userId = ref(1)
const activeLetter = ref('')

// ========== 右键菜单状态 ==========
const contextMenuVisible = ref(false)
const contextMenuPosition = ref({ x: 0, y: 0 })
const contextMenuFriend = ref(null)

// ========== 对话框状态 ==========
const addFriendDialogVisible = ref(false)
const friendDetailDialogVisible = ref(false)
const selectedFriend = ref(null)

// ========== 添加好友相关 ==========
const friendSearchType = ref('nickname')
const friendSearchKeyword = ref('')
const searchResults = ref([])

// ========== 计算属性 ==========
const filteredFriends = computed(() => {
  let result = [...friends.value]

  // 根据筛选类型过滤
  if (filterType.value === 'recent') {
    result = result.filter(friend => friend.recentChatTime)
  }

  // 根据搜索关键词过滤
  if (searchQuery.value.trim()) {
    const keyword = searchQuery.value.toLowerCase().trim()
    result = result.filter(friend =>
      friend.name.toLowerCase().includes(keyword) ||
      friend.id.toString().includes(keyword)
    )
  }

  return result
})

// 按字母分组的好友
const groupedFriends = computed(() => {
  const groups = {}

  // 对好友按拼音首字母分组（简化版：按首字母）
  filteredFriends.value.forEach(friend => {
    // 获取首字母（如果是中文，使用简单的拼音转换；否则使用首字符）
    let firstChar = friend.name.charAt(0).toUpperCase()
    // 如果是中文字符，使用 # 作为分组
    if (/[\u4e00-\u9fa5]/.test(firstChar)) {
      firstChar = '#'
    }
    // 如果是数字，使用 # 作为分组
    if (/[0-9]/.test(firstChar)) {
      firstChar = '#'
    }

    if (!groups[firstChar]) {
      groups[firstChar] = []
    }
    groups[firstChar].push(friend)
  })

  // 对每个分组内的好友按名称排序
  Object.keys(groups).forEach(letter => {
    groups[letter].sort((a, b) => a.name.localeCompare(b.name, 'zh-CN'))
  })

  // 按字母顺序排序分组
  const sortedGroups = {}
  Object.keys(groups).sort().forEach(letter => {
    sortedGroups[letter] = groups[letter]
  })

  return sortedGroups
})

// 可用的字母列表
const availableLetters = computed(() => {
  return Object.keys(groupedFriends.value).sort()
})

const totalCount = computed(() => friends.value.length)
const recentCount = computed(() => friends.value.filter(f => f.recentChatTime).length)

const emptyText = computed(() => {
  if (searchQuery.value.trim()) {
    return '未找到匹配的好友'
  }
  if (filterType.value === 'recent') {
    return '暂无最近联系的好友'
  }
  return '还没有好友，快去添加吧'
})

// ========== 获取用户ID ==========
const getUserId = () => {
  if (authStore.token) {
    const decodedToken = decodeJwt(authStore.token)
    if (decodedToken && decodedToken.userId) {
      return parseInt(decodedToken.userId, 10)
    }
  }
  return 1
}

// ========== 判断头像是否为图片 ==========
const isImageAvatar = (avatar) => {
  if (!avatar) return false
  return /^https?:\/\//.test(avatar) || /^data:image/.test(avatar)
}

// ========== 获取好友列表 ==========
const fetchFriends = async () => {
  loading.value = true
  try {
    const response = await api.get(`/v1/contacts/friends?userId=${userId.value}`)
    if (response.code === '200') {
      // 获取每个好友的详细信息
      const friendsWithDetails = await Promise.all(
        response.data.map(async (contact) => {
          try {
            const userResponse = await api.get(`/v1/users/${contact.targetId}`)
            const userData = userResponse.data

            return {
              id: contact.targetId,
              name: userData.nickname || userData.username || '好友',
              avatar: userData.avatar || '👤',
              isOnline: false,
              recentChatTime: null
            }
          } catch (error) {
            console.error(`获取好友 ${contact.targetId} 信息失败:`, error)
            return {
              id: contact.targetId,
              name: '好友',
              avatar: '👤',
              isOnline: false,
              recentChatTime: null
            }
          }
        })
      )

      friends.value = friendsWithDetails
    } else {
      ElMessage.error('获取好友列表失败')
    }
  } catch (error) {
    console.error('获取好友列表失败:', error)
    ElMessage.error('获取好友列表失败')
  } finally {
    loading.value = false
  }
}

// ========== 搜索好友 ==========
const handleSearch = () => {
  // 搜索逻辑由 computed 处理
}

// ========== 筛选好友 ==========
const handleFilter = () => {
  // 筛选逻辑由 computed 处理
}

// ========== 重置筛选 ==========
const resetFilter = () => {
  searchQuery.value = ''
  filterType.value = 'all'
}

// ========== 格式化最近时间 ==========
const formatRecentTime = (time) => {
  if (!time) return ''

  const now = new Date()
  const timeDate = new Date(time)
  const diff = now - timeDate

  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`

  return timeDate.toLocaleDateString()
}

// ========== 开始聊天 ==========
const startChat = (friend) => {
  router.push({
    path: '/user/home/chat',
    query: { friendId: friend.id, friendName: friend.name }
  })
}

// ========== 右键菜单操作 ==========
const showContextMenu = (event, friend) => {
  contextMenuFriend.value = friend
  contextMenuPosition.value = {
    x: event.clientX,
    y: event.clientY
  }
  contextMenuVisible.value = true
}

const closeContextMenu = () => {
  contextMenuVisible.value = false
}

// ========== 字母索引操作 ==========
const scrollToLetter = (letter) => {
  activeLetter.value = letter
  const element = document.querySelector(`.group-letter[data-letter="${letter}"]`)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
  // 2秒后清除激活状态
  setTimeout(() => {
    activeLetter.value = ''
  }, 2000)
}

// ========== 空状态点击 ==========
const handleEmptyClick = () => {
  if (!searchQuery.value && filterType.value === 'all') {
    openAddFriendDialog()
  }
}

// ========== 查看好友详情 ==========
const viewFriendDetail = (friend) => {
  selectedFriend.value = friend
  friendDetailDialogVisible.value = true
}

// ========== 从详情页开始聊天 ==========
const startChatFromDetail = () => {
  if (selectedFriend.value) {
    friendDetailDialogVisible.value = false
    startChat(selectedFriend.value)
  }
}

// ========== 删除好友 ==========
const deleteFriend = async (friend) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除好友 "${friend.name}" 吗？`,
      '删除好友',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await api.delete(`/v1/contacts/friends/${userId.value}/${friend.id}`)

    if (response.code === '200') {
      ElMessage.success('删除好友成功')
      // 从列表中移除
      friends.value = friends.value.filter(f => f.id !== friend.id)
    } else {
      ElMessage.error('删除好友失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除好友失败:', error)
      ElMessage.error('删除好友失败')
    }
  }
}

// ========== 打开添加好友对话框 ==========
const openAddFriendDialog = () => {
  addFriendDialogVisible.value = true
  friendSearchKeyword.value = ''
  searchResults.value = []
}

// ========== 搜索用户（用于添加好友）==========
const handleFriendSearch = async () => {
  if (!friendSearchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  try {
    const response = await api.get('/v1/users/search', {
      params: {
        keyword: friendSearchKeyword.value.trim(),
        searchType: friendSearchType.value
      }
    })

    if (response.code === '200') {
      searchResults.value = (response.data || []).map(user => ({
        id: user.userId,
        name: user.nickname || user.username,
        avatar: user.avatar || '👤',
        phone: user.phone,
        email: user.email,
        adding: false,
        added: false
      }))

      if (searchResults.value.length === 0) {
        ElMessage.info('未找到相关用户')
      }
    } else {
      ElMessage.error('搜索失败')
    }
  } catch (error) {
    console.error('搜索用户失败:', error)
    ElMessage.error('搜索失败')
  }
}

// ========== 发送好友请求 ==========
const sendFriendRequest = async (user) => {
  if (user.added) return

  user.adding = true

  try {
    const response = await api.post('/v1/contacts/friends/request', {
      userId: userId.value.toString(),
      targetId: user.id.toString(),
      relationType: 'friend',
      status: 'pending',
      message: '你好，我想加你为好友'
    })

    if (response.code === '200') {
      user.added = true
      ElMessage.success(`已向 ${user.name} 发送好友请求`)
    } else {
      ElMessage.error('发送好友请求失败')
    }
  } catch (error) {
    console.error('发送好友请求失败:', error)
    ElMessage.error('发送好友请求失败')
  } finally {
    user.adding = false
  }
}

// ========== 生命周期 ==========
onMounted(() => {
  userId.value = getUserId()
  fetchFriends()

  // 全局点击事件，关闭右键菜单
  document.addEventListener('click', () => {
    if (contextMenuVisible.value) {
      contextMenuVisible.value = false
    }
  })
})
</script>

<style scoped lang="less">
.contacts-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 100px);
}

// 页面头部
.contacts-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

  .page-title {
    display: flex;
    align-items: center;
    margin: 0;
    color: #fff;
    font-size: 22px;
    font-weight: 600;

    .title-icon {
      margin-right: 10px;
      font-size: 26px;
    }
  }

  .header-actions {
    :deep(.el-button) {
      background: rgba(255, 255, 255, 0.2);
      border: 1px solid rgba(255, 255, 255, 0.3);
      color: #fff;
      font-weight: 500;

      &:hover {
        background: rgba(255, 255, 255, 0.3);
        transform: translateY(-2px);
      }
    }
  }
}

// 搜索和筛选区域
.search-filter-section {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;

  .search-box {
    flex: 1;

    :deep(.el-input) {
      .el-input__wrapper {
        border-radius: 10px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        transition: all 0.3s ease;

        &:hover {
          box-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
        }
      }
    }
  }

  .filter-options {
    :deep(.el-radio-group) {
      display: flex;
      gap: 8px;

      .el-radio-button {
        .el-radio-button__inner {
          border-radius: 8px;
          border: 2px solid #e2e8f0;
          background: #fff;
          color: #4a5568;
          font-weight: 500;
          padding: 10px 16px;
          transition: all 0.3s ease;

          &:hover {
            border-color: #667eea;
            color: #667eea;
          }
        }

        &.is-active {
          .el-radio-button__inner {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-color: #667eea;
            color: #fff;
            box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
          }
        }
      }
    }
  }
}

// 内容区域
.contacts-content {
  display: flex;
  gap: 12px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  height: calc(100vh - 280px);
  min-height: 400px;

  .friends-list-wrapper {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .friends-list-scroll {
      flex: 1;
      overflow-y: auto;
      overflow-x: hidden;

      &::-webkit-scrollbar {
        width: 6px;
      }

      &::-webkit-scrollbar-track {
        background: #f1f1f1;
        border-radius: 3px;
      }

      &::-webkit-scrollbar-thumb {
        background: #c1c1c1;
        border-radius: 3px;

        &:hover {
          background: #a8a8a8;
        }
      }

      .friend-group {
        .group-letter {
          padding: 8px 16px;
          background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
          color: #667eea;
          font-size: 14px;
          font-weight: 600;
          position: sticky;
          top: 0;
          z-index: 10;
          border-bottom: 1px solid #e4e7ed;
          transition: all 0.3s ease;

          &.active {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #fff;
            animation: letter-highlight 0.5s ease;
          }
        }

        .friend-item {
          display: flex;
          align-items: center;
          padding: 10px 16px;
          cursor: pointer;
          border-bottom: 1px solid #f0f2f5;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          position: relative;

          &:hover {
            background-color: #f5f7fa;
            transform: translateX(2px);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
          }

          &:active {
            transform: translateX(1px) scale(0.99);
          }

          &.has-recent-chat {
            background: linear-gradient(90deg, #fffbe6 0%, #fffcf5 100%);

            &:hover {
              background: linear-gradient(90deg, #fff7e6 0%, #fffaf0 100%);
            }
          }

          .friend-avatar {
            margin-right: 12px;
            position: relative;
            flex-shrink: 0;

            img {
              width: 40px;
              height: 40px;
              border-radius: 8px;
              object-fit: cover;
              box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
            }

            .avatar-emoji {
              width: 40px;
              height: 40px;
              border-radius: 8px;
              background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 22px;
              box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
            }

            .recent-chat-badge {
              position: absolute;
              top: -4px;
              right: -4px;
              width: 18px;
              height: 18px;
              background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
              border-radius: 50%;
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 10px;
              box-shadow: 0 2px 3px rgba(64, 158, 255, 0.3);
              animation: pulse 2s infinite;
            }
          }

          .friend-info {
            flex: 1;
            min-width: 0;

            .friend-name-row {
              display: flex;
              justify-content: space-between;
              align-items: center;
              margin-bottom: 4px;

              .friend-name {
                font-size: 14px;
                font-weight: 600;
                color: #303133;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
              }

              .recent-time {
                font-size: 11px;
                color: #909399;
                white-space: nowrap;
                flex-shrink: 0;
              }
            }

            .friend-id {
              font-size: 12px;
              color: #909399;
            }
          }

          .friend-quick-actions {
            flex-shrink: 0;
            opacity: 0;
            transition: opacity 0.2s;

            .chat-btn {
              background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
              border: none;

              &:hover {
                transform: scale(1.1);
                box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
              }
            }
          }

          &:hover .friend-quick-actions {
            opacity: 1;
          }
        }
      }
    }
  }

  .alphabet-index {
    width: 40px;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 12px 0;
    background: #f5f7fa;
    border-left: 1px solid #e4e7ed;
    overflow-y: auto;
    flex-shrink: 0;

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }

    &::-webkit-scrollbar-thumb {
      background: #dcdfe6;
      border-radius: 2px;
    }

    .alphabet-item {
      width: 28px;
      height: 28px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      font-weight: 500;
      color: #606266;
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.3s ease;
      margin-bottom: 2px;

      &:hover {
        background: #667eea;
        color: #fff;
        transform: scale(1.1);
      }

      &.active {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
        font-weight: 600;
        box-shadow: 0 2px 6px rgba(102, 126, 234, 0.4);
        animation: letter-active 0.3s ease;
      }
    }
  }
}

// 加载状态
.loading-container {
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

// 空状态
.empty-container {
  padding: 60px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  text-align: center;
  cursor: pointer;
  user-select: none;
  transition: all 0.3s ease;

  &:hover {
    background: linear-gradient(135deg, #f0f7ff 0%, #e6f2ff 100%);
    transform: translateY(-2px);

    .empty-icon {
      transform: scale(1.1);
    }

    .empty-title {
      color: #3b82f6;
    }

    .empty-tip {
      color: #60a5fa;
    }
  }

  .empty-icon {
    font-size: 80px;
    margin-bottom: 20px;
    opacity: 0.8;
    animation: float 3s ease-in-out infinite;
    transition: transform 0.3s ease;
  }

  .empty-title {
    font-size: 18px;
    font-weight: 500;
    color: #1a1a1a;
    margin: 0 0 8px 0;
    transition: color 0.3s ease;
  }

  .empty-tip {
    font-size: 14px;
    color: #666;
    margin: 0 0 20px 0;
    transition: color 0.3s ease;
  }
}

// 右键菜单
.context-menu {
  position: fixed;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  padding: 8px 0;
  min-width: 150px;
  z-index: 9999;
  animation: context-menu-fadein 0.2s ease;

  .context-menu-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 16px;
    font-size: 14px;
    color: #303133;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      background: #f5f7fa;
      color: #667eea;
    }

    .el-icon {
      font-size: 16px;
    }

    &.danger {
      &:hover {
        background: #fef0f0;
        color: #f56c6c;
      }
    }
  }

  .context-menu-divider {
    height: 1px;
    background: #e4e7ed;
    margin: 6px 0;
  }
}

// 添加好友对话框
.add-friend-content {
  .search-type-selector {
    margin-bottom: 16px;

    :deep(.el-radio-group) {
      display: flex;
      gap: 12px;

      .el-radio-button {
        .el-radio-button__inner {
          border-radius: 8px;
          border: 2px solid #e2e8f0;
          background: #fff;
          font-weight: 500;

          &:hover {
            border-color: #667eea;
            color: #667eea;
          }
        }

        &.is-active {
          .el-radio-button__inner {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-color: #667eea;
            color: #fff;
          }
        }
      }
    }
  }

  .search-results {
    margin-top: 20px;
    max-height: 400px;
    overflow-y: auto;

    .result-item {
      display: flex;
      align-items: center;
      padding: 12px;
      border-radius: 10px;
      margin-bottom: 10px;
      background: #f7fafc;
      border: 2px solid #e2e8f0;
      transition: all 0.3s ease;

      &:hover {
        border-color: #667eea;
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
      }

      .user-avatar {
        width: 50px;
        height: 50px;
        margin-right: 12px;
        flex-shrink: 0;

        img,
        span {
          width: 100%;
          height: 100%;
          border-radius: 50%;
          object-fit: cover;
        }

        span {
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24px;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }
      }

      .user-info {
        flex: 1;
        min-width: 0;

        .user-name {
          font-size: 14px;
          font-weight: 600;
          color: #2d3748;
          margin-bottom: 4px;
        }

        .user-detail {
          font-size: 12px;
          color: #718096;
        }
      }

      :deep(.el-button) {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border: none;
        flex-shrink: 0;

        &:hover {
          transform: translateY(-2px);
        }

        &:disabled {
          background: #cbd5e0;
        }
      }
    }
  }
}

// 好友详情对话框
.friend-detail {
  text-align: center;

  .detail-avatar {
    display: flex;
    justify-content: center;
    margin-bottom: 20px;

    img,
    .avatar-emoji {
      width: 120px;
      height: 120px;
      border-radius: 50%;
      object-fit: cover;
    }

    .avatar-emoji {
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 60px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }
  }

  .detail-info {
    text-align: left;
    padding: 0 20px;

    .info-item {
      margin-bottom: 16px;
      font-size: 15px;

      .label {
        font-weight: 600;
        color: #4a5568;
        margin-right: 8px;
      }

      .value {
        color: #718096;
      }
    }
  }
}

// 对话框样式
:deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;

  .el-dialog__title {
    color: #fff;
    font-weight: 600;
  }

  .el-dialog__headerbtn {
    .el-dialog__close {
      color: #fff;

      &:hover {
        color: #f0f0f0;
      }
    }
  }
}

// 动画
@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes letter-highlight {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes letter-active {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes context-menu-fadein {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
