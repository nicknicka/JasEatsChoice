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
          size="large"
          @input="handleSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>

      <!-- 筛选选项 -->
      <div class="filter-options">
        <el-radio-group v-model="filterType" size="large" @change="handleFilter">
          <el-radio-button label="all">全部好友 ({{ totalCount }})</el-radio-button>
          <el-radio-button label="recent">最近聊天 ({{ recentCount }})</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 空状态 -->
    <div v-else-if="filteredFriends.length === 0" class="empty-container">
      <el-empty :description="emptyText">
        <template #image>
          <div class="empty-icon">👥</div>
        </template>
        <el-button v-if="searchQuery || filterType !== 'all'" type="primary" @click="resetFilter">
          清除筛选
        </el-button>
        <el-button v-else type="primary" @click="openAddFriendDialog">
          去添加好友
        </el-button>
      </el-empty>
    </div>

    <!-- 好友列表 -->
    <div v-else class="friends-list">
      <div
        v-for="friend in filteredFriends"
        :key="friend.id"
        class="friend-card"
        @click="startChat(friend)"
      >
        <!-- 头像 -->
        <div class="friend-avatar">
          <img v-if="isImageAvatar(friend.avatar)" :src="friend.avatar" alt="头像" />
          <span v-else class="avatar-emoji">{{ friend.avatar || '👤' }}</span>
          <!-- 在线状态指示器 -->
          <div v-if="friend.isOnline" class="online-indicator"></div>
        </div>

        <!-- 好友信息 -->
        <div class="friend-info">
          <div class="friend-header">
            <h3 class="friend-name">{{ friend.name }}</h3>
            <el-tag v-if="friend.recentChatTime" type="info" size="small" class="recent-tag">
              最近: {{ formatRecentTime(friend.recentChatTime) }}
            </el-tag>
          </div>
          <div class="friend-details">
            <span class="detail-item">ID: {{ friend.id }}</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="friend-actions">
          <el-button
            type="primary"
            :icon="ChatDotRound"
            circle
            size="large"
            @click.stop="startChat(friend)"
            title="开始聊天"
          />
          <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, friend)">
            <el-button :icon="MoreFilled" circle size="large" />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="view" :icon="User">查看详情</el-dropdown-item>
                <el-dropdown-item command="delete" :icon="Delete" divided>删除好友</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
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
  MoreFilled,
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

const totalCount = computed(() => friends.value.length)
const recentCount = computed(() => friends.value.filter(f => f.recentChatTime).length)

const emptyText = computed(() => {
  if (searchQuery.value.trim()) {
    return '未找到匹配的好友'
  }
  if (filterType.value === 'recent') {
    return '暂无最近聊天的好友'
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

// ========== 处理命令 ==========
const handleCommand = (command, friend) => {
  switch (command) {
    case 'view':
      viewFriendDetail(friend)
      break
    case 'delete':
      deleteFriend(friend)
      break
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
  margin-bottom: 24px;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

  .page-title {
    display: flex;
    align-items: center;
    margin: 0;
    color: #fff;
    font-size: 24px;
    font-weight: 600;

    .title-icon {
      margin-right: 12px;
      font-size: 28px;
    }
  }

  .header-actions {
    :deep(.el-button) {
      background: rgba(255, 255, 255, 0.2);
      border: 1px solid rgba(255, 255, 255, 0.3);
      color: #fff;

      &:hover {
        background: rgba(255, 255, 255, 0.3);
        transform: translateY(-2px);
      }
    }
  }
}

// 搜索和筛选区域
.search-filter-section {
  margin-bottom: 24px;

  .search-box {
    margin-bottom: 16px;

    :deep(.el-input) {
      .el-input__wrapper {
        border-radius: 12px;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      }

      .el-input-group__append {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border: none;
        color: #fff;
        border-radius: 0 12px 12px 0;

        .el-button {
          background: transparent;
          border: none;
          color: #fff;
        }
      }
    }
  }

  .filter-options {
    :deep(.el-radio-group) {
      display: flex;
      gap: 12px;

      .el-radio-button {
        .el-radio-button__inner {
          border-radius: 10px;
          border: 2px solid #e2e8f0;
          background: #fff;
          color: #4a5568;
          font-weight: 500;
          padding: 12px 20px;
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
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
          }
        }
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

  .empty-icon {
    font-size: 80px;
    margin-bottom: 20px;
    opacity: 0.8;
  }
}

// 好友列表
.friends-list {
  display: grid;
  gap: 16px;

  .friend-card {
    display: flex;
    align-items: center;
    padding: 20px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    cursor: pointer;
    transition: all 0.3s ease;
    border: 2px solid transparent;

    &:hover {
      border-color: #667eea;
      box-shadow: 0 4px 20px rgba(102, 126, 234, 0.2);
      transform: translateY(-2px);
    }

    .friend-avatar {
      position: relative;
      width: 60px;
      height: 60px;
      margin-right: 16px;
      flex-shrink: 0;

      img,
      .avatar-emoji {
        width: 100%;
        height: 100%;
        border-radius: 50%;
        object-fit: cover;
      }

      .avatar-emoji {
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 32px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      .online-indicator {
        position: absolute;
        bottom: 2px;
        right: 2px;
        width: 14px;
        height: 14px;
        background: #67c23a;
        border: 2px solid #fff;
        border-radius: 50%;
      }
    }

    .friend-info {
      flex: 1;
      min-width: 0;

      .friend-header {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 6px;

        .friend-name {
          margin: 0;
          font-size: 16px;
          font-weight: 600;
          color: #2d3748;
        }

        .recent-tag {
          flex-shrink: 0;
        }
      }

      .friend-details {
        display: flex;
        gap: 16px;
        font-size: 13px;
        color: #718096;

        .detail-item {
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }
    }

    .friend-actions {
      display: flex;
      gap: 8px;
      flex-shrink: 0;

      :deep(.el-button) {
        &.el-button--primary {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border: none;

          &:hover {
            transform: scale(1.1);
          }
        }
      }
    }
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
</style>
