<template>
  <div class="contacts-container">
    <!-- 页面头部 -->
    <div class="contacts-header">
      <div class="header-content">
        <h2 class="page-title">
          <span class="title-icon">👥</span>
          通讯录
        </h2>
        <div class="header-stats">
          <div class="stat-item">
            <span class="stat-label">总好友</span>
            <span class="stat-value">{{ totalCount }}</span>
          </div>
        </div>
      </div>
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
            <div class="group-letter" :data-letter="letter">{{ letter }}</div>

            <!-- 该字母下的好友 -->
            <div
              v-for="friend in group"
              :key="friend.id"
              class="friend-item"
              @click="startChat(friend)"
              @contextmenu.prevent="showContextMenu($event, friend)"
            >
              <!-- 头像 -->
              <div class="friend-avatar">
                <img v-if="isImageAvatar(friend.avatar)" :src="friend.avatar" alt="头像" />
                <span v-else class="avatar-emoji">{{ friend.avatar || '👤' }}</span>
              </div>

              <!-- 好友信息 -->
              <div class="friend-info">
                <div class="friend-name-row">
                  <span class="friend-name">{{ friend.name }}</span>
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
        <div class="friend-search-input">
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
        </div>

        <!-- 搜索结果 -->
        <div v-if="searchResults.length > 0" class="search-results">
          <div v-for="user in searchResults" :key="user.id" class="result-item">
            <div class="user-avatar">
              <img v-if="isImageAvatar(user.avatar)" :src="user.avatar" alt="" />
              <span v-else>{{ user.avatar || '👤' }}</span>
            </div>
            <div class="user-info">
              <div class="user-name">{{ user.name }}</div>
              <div v-if="user.phone" class="user-detail">手机: {{ maskPhone(user.phone) }}</div>
              <div v-if="user.email" class="user-detail">邮箱: {{ maskEmail(user.email) }}</div>
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

const emptyText = computed(() => {
  if (searchQuery.value.trim()) {
    return '未找到匹配的好友'
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

// ========== 手机号脱敏 ==========
const maskPhone = (phone) => {
  if (!phone) return phone
  if (phone.length === 11) {
    return phone.substring(0, 3) + '****' + phone.substring(7)
  }
  return phone
}

// ========== 邮箱脱敏 ==========
const maskEmail = (email) => {
  if (!email) return email

  const atIndex = email.indexOf('@')
  if (atIndex > 1) {
    return email[0] + '***' + email.substring(atIndex)
  } else if (atIndex === 1) {
    return email
  }
  return email
}

// ========== 移除未使用的函数 ==========
// formatRecentTime 函数已移除，因为不再显示最近联系时间

// ========== 从本地存储加载好友列表 ==========
const loadFriendsFromLocal = () => {
  try {
    const cachedFriends = localStorage.getItem(`friends_${userId.value}`)
    if (cachedFriends) {
      const parsedFriends = JSON.parse(cachedFriends)
      if (Array.isArray(parsedFriends) && parsedFriends.length > 0) {
        console.log('📦 [Contacts] 从本地缓存加载好友列表 - 共', parsedFriends.length, '个好友')
        friends.value = parsedFriends
        return true
      }
    }
  } catch (error) {
    console.error('❌ [Contacts] 加载本地缓存失败:', error)
  }
  return false
}

// ========== 保存好友列表到本地 ==========
const saveFriendsToLocal = (friendsData) => {
  try {
    localStorage.setItem(`friends_${userId.value}`, JSON.stringify(friendsData))
    console.log('💾 [Contacts] 好友列表已保存到本地缓存')
  } catch (error) {
    console.error('❌ [Contacts] 保存本地缓存失败:', error)
  }
}

// ========== 获取好友列表 ==========
const fetchFriends = async () => {
  console.log('🚀 [Contacts] 开始获取好友列表, userId:', userId.value)

  loading.value = true

  try {
    const response = await api.get(`/v1/contacts/friends?userId=${userId.value}`)

    console.log('📡 [Contacts] 好友列表 API 响应:', {
      code: response.code,
      dataLength: response.data?.length,
      userId: userId.value
    })

    if (response.code === '200') {
      const contacts = response.data || []

      // 获取每个好友的详细信息
      const friendsWithDetails = await Promise.all(
        contacts.map(async (contact) => {
          try {
            const userResponse = await api.get(`/v1/users/${contact.targetId}`)
            const userData = userResponse.data

            // 判断头像是否为有效的图片 URL
            const isValidAvatarUrl = (avatar) => {
              if (!avatar) return false
              return /^https?:\/\//.test(avatar) || /^data:image/.test(avatar)
            }

            const avatar = isValidAvatarUrl(userData.avatar) ? userData.avatar : '👤'

            return {
              id: contact.targetId,
              name: userData.nickname || userData.username || '好友',
              avatar: avatar
            }
          } catch (error) {
            console.error(`❌ [Contacts] 获取好友 ${contact.targetId} 信息失败:`, error)
            // 返回默认信息，确保一个好友失败不影响其他好友
            return {
              id: contact.targetId,
              name: '好友',
              avatar: '👤'
            }
          }
        })
      )

      friends.value = friendsWithDetails
      console.log(`✅ [Contacts] 好友列表已更新 - 共 ${friendsWithDetails.length} 个好友`)

      // 保存到本地缓存
      saveFriendsToLocal(friendsWithDetails)
    } else {
      console.error(`❌ [Contacts] 获取好友列表失败 - code: ${response.code}`)
      ElMessage.error('获取好友列表失败')
    }
  } catch (error) {
    console.error('❌ [Contacts] 获取好友列表失败:', error)

    // 如果网络请求失败，尝试从本地缓存加载
    const hasLocalData = loadFriendsFromLocal()
    if (hasLocalData) {
      ElMessage.warning('网络连接失败，已加载本地缓存数据')
    } else {
      ElMessage.error('获取好友列表失败，请检查网络连接')
    }
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
  console.log('💬 [Contacts] 开始聊天:', friend)
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

    console.log('🗑️ [Contacts] 开始删除好友:', friend)

    const response = await api.delete(`/v1/contacts/friends/${userId.value}/${friend.id}`)

    if (response.code === '200') {
      // 从列表中移除
      friends.value = friends.value.filter(f => f.id !== friend.id)

      // 更新本地缓存
      saveFriendsToLocal(friends.value)

      console.log('✅ [Contacts] 删除好友成功')
      ElMessage.success('删除好友成功')
    } else {
      console.error('❌ [Contacts] 删除好友失败 - code:', response.code)
      ElMessage.error('删除好友失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('❌ [Contacts] 删除好友失败:', error)
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

  console.log('🔍 [Contacts] 搜索用户:', {
    keyword: friendSearchKeyword.value.trim(),
    searchType: friendSearchType.value
  })

  try {
    const response = await api.get('/v1/users/search', {
      params: {
        keyword: friendSearchKeyword.value.trim(),
        searchType: friendSearchType.value
      }
    })

    console.log('📡 [Contacts] 搜索用户 API 响应:', {
      code: response.code,
      dataLength: response.data?.length
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

      console.log(`✅ [Contacts] 搜索完成，找到 ${searchResults.value.length} 个用户`)

      if (searchResults.value.length === 0) {
        ElMessage.info('未找到相关用户')
      }
    } else {
      console.error('❌ [Contacts] 搜索失败 - code:', response.code)
      ElMessage.error('搜索失败')
    }
  } catch (error) {
    console.error('❌ [Contacts] 搜索用户失败:', error)
    ElMessage.error('搜索失败')
  }
}

// ========== 发送好友请求 ==========
const sendFriendRequest = async (user) => {
  if (user.added) return

  console.log('📤 [Contacts] 发送好友请求:', user)

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
      console.log('✅ [Contacts] 好友请求已发送')
      ElMessage.success(`已向 ${user.name} 发送好友请求`)
    } else {
      console.error('❌ [Contacts] 发送好友请求失败 - code:', response.code)
      ElMessage.error('发送好友请求失败')
    }
  } catch (error) {
    console.error('❌ [Contacts] 发送好友请求失败:', error)
    ElMessage.error('发送好友请求失败')
  } finally {
    user.adding = false
  }
}

// ========== 生命周期 ==========
onMounted(async () => {
  console.log('🚀 [Contacts] Contacts组件挂载，开始初始化')
  userId.value = getUserId()

  try {
    // 先从本地加载缓存（同步操作，快速显示）
    loadFriendsFromLocal()

    // 再从服务器获取最新数据
    await fetchFriends()

    console.log('✅ [Contacts] 初始化完成')
  } catch (error) {
    console.error('❌ [Contacts] 初始化失败:', error)
  }

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
  padding: 24px;
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  min-height: calc(100vh - 100px);
  position: relative;
  overflow-x: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: radial-gradient(circle at 20% 30%, rgba(59, 130, 246, 0.06) 0%, transparent 50%),
                radial-gradient(circle at 80% 70%, rgba(96, 165, 250, 0.06) 0%, transparent 50%);
    pointer-events: none;
    z-index: 0;
  }

  > * {
    position: relative;
    z-index: 1;
  }
}

// 页面头部
.contacts-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.35);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    box-shadow: 0 12px 32px rgba(59, 130, 246, 0.45);
    transform: translateY(-2px);
  }

  .header-content {
    display: flex;
    align-items: center;
    gap: 32px;
  }

  .page-title {
    display: flex;
    align-items: center;
    margin: 0;
    color: #fff;
    font-size: 24px;
    font-weight: 700;
    letter-spacing: 0.5px;

    .title-icon {
      margin-right: 12px;
      font-size: 28px;
      animation: icon-pulse 2s ease-in-out infinite;
    }
  }

  .header-stats {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 8px 16px;
    background: rgba(255, 255, 255, 0.15);
    border-radius: 10px;
    backdrop-filter: blur(10px);

    .stat-item {
      display: flex;
      align-items: center;
      gap: 8px;

      .stat-label {
        font-size: 13px;
        color: rgba(255, 255, 255, 0.9);
        font-weight: 500;
      }

      .stat-value {
        font-size: 18px;
        font-weight: 700;
        color: #fff;
      }
    }

    .stat-divider {
      width: 1px;
      height: 24px;
      background: rgba(255, 255, 255, 0.3);
    }
  }

  .header-actions {
    :deep(.el-button) {
      background: rgba(255, 255, 255, 0.25);
      border: 1px solid rgba(255, 255, 255, 0.4);
      color: #fff;
      font-weight: 600;
      padding: 12px 24px;
      font-size: 14px;
      backdrop-filter: blur(10px);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

      &:hover {
        background: rgba(255, 255, 255, 0.35);
        transform: translateY(-2px) scale(1.02);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }

      &:active {
        transform: translateY(0) scale(0.98);
      }
    }
  }
}

@keyframes icon-pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

// 搜索和筛选区域
.search-filter-section {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;

  .search-box {
    flex: 1;

    :deep(.el-input) {
      .el-input__wrapper {
        border-radius: 12px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        border: 2px solid #e4e7ed;
        padding: 8px 16px;

        &:hover {
          box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
          border-color: #c0c4cc;
        }

        &.is-focus {
          border-color: #3b82f6;
          box-shadow: 0 4px 20px rgba(59, 130, 246, 0.2);
        }
      }
    }
  }

  .filter-options {
    :deep(.el-radio-group) {
      display: flex;
      gap: 10px;

      .el-radio-button {
        .el-radio-button__inner {
          border-radius: 10px;
          border: 2px solid #e2e8f0;
          background: #fff;
          color: #4a5568;
          font-weight: 600;
          padding: 12px 20px;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

          &:hover {
            border-color: #3b82f6;
            color: #3b82f6;
            transform: translateY(-1px);
            box-shadow: 0 4px 8px rgba(59, 130, 246, 0.15);
          }
        }

        &.is-active {
          .el-radio-button__inner {
            background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
            border-color: #3b82f6;
            color: #fff;
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.35);
            transform: translateY(-1px);
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
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  height: calc(100vh - 280px);
  min-height: 400px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
  }

  .friends-list-wrapper {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    border-radius: 16px 0 0 16px;

    .friends-list-scroll {
      flex: 1;
      overflow-y: auto;
      overflow-x: hidden;

      &::-webkit-scrollbar {
        width: 8px;
      }

      &::-webkit-scrollbar-track {
        background: #f5f7fa;
        border-radius: 4px;
      }

      &::-webkit-scrollbar-thumb {
        background: linear-gradient(135deg, #c0c4cc 0%, #dcdfe6 100%);
        border-radius: 4px;
        transition: all 0.3s ease;

        &:hover {
          background: linear-gradient(135deg, #909399 0%, #b0b4bc 100%);
        }
      }

      .friend-group {
        .group-letter {
          padding: 12px 20px;
          background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
          color: #3b82f6;
          font-size: 15px;
          font-weight: 700;
          position: sticky;
          top: 0;
          z-index: 10;
          border-bottom: 2px solid #bfdbfe;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          letter-spacing: 0.5px;

          &.active {
            background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
            color: #fff;
            animation: letter-highlight 0.5s ease;
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
          }
        }

        .friend-item {
          display: flex;
          align-items: center;
          padding: 14px 20px;
          cursor: pointer;
          border-bottom: 1px solid #f0f2f5;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          position: relative;
          background: #fff;

          &:hover {
            background: linear-gradient(90deg, #eff6ff 0%, #ffffff 100%);
            transform: translateX(4px);
            box-shadow: 0 4px 16px rgba(59, 130, 246, 0.12);
            border-left: 3px solid #3b82f6;
          }

          &:active {
            transform: translateX(2px) scale(0.99);
          }

          .friend-avatar {
            margin-right: 16px;
            position: relative;
            flex-shrink: 0;

            img {
              width: 48px;
              height: 48px;
              border-radius: 12px;
              object-fit: cover;
              box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
              transition: all 0.3s ease;
            }

            .avatar-emoji {
              width: 48px;
              height: 48px;
              border-radius: 12px;
              background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 24px;
              box-shadow: 0 4px 12px rgba(59, 130, 246, 0.25);
            }
          }

          .friend-info {
            flex: 1;
            min-width: 0;

            .friend-name-row {
              display: flex;
              align-items: center;
              margin-bottom: 6px;

              .friend-name {
                font-size: 15px;
                font-weight: 600;
                color: #303133;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
                letter-spacing: 0.3px;
              }
            }

            .friend-id {
              font-size: 13px;
              color: #909399;
              font-weight: 500;
              letter-spacing: 0.3px;
            }
          }

          .friend-quick-actions {
            flex-shrink: 0;
            opacity: 0;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            transform: translateX(-10px);

            .chat-btn {
              background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
              border: none;
              box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
              transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

              &:hover {
                transform: scale(1.15) rotate(5deg);
                box-shadow: 0 4px 16px rgba(59, 130, 246, 0.45);
              }
            }
          }

          &:hover {
            .friend-quick-actions {
              opacity: 1;
              transform: translateX(0);
            }

            .friend-avatar img {
              transform: scale(1.05);
              box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
            }
          }
        }
      }
    }
  }

  .alphabet-index {
    width: 50px;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 16px 0;
    background: linear-gradient(180deg, #eff6ff 0%, #dbeafe 100%);
    border-left: 2px solid #bfdbfe;
    overflow-y: auto;
    flex-shrink: 0;
    border-radius: 0 16px 16px 0;

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
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 13px;
      font-weight: 600;
      color: #606266;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      margin-bottom: 4px;
      letter-spacing: 0.3px;

      &:hover {
        background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
        color: #fff;
        transform: scale(1.15);
        box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
      }

      &.active {
        background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
        color: #fff;
        font-weight: 700;
        box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
        animation: letter-active 0.3s ease;
        transform: scale(1.2);
      }
    }
  }
}

// 加载状态
.loading-container {
  padding: 60px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

// 空状态
.empty-container {
  padding: 80px 20px;
  background: linear-gradient(135deg, #ffffff 0%, #f0f9ff 100%);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  text-align: center;
  cursor: pointer;
  user-select: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px dashed #bfdbfe;
  min-height: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  &:hover {
    background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
    border-color: #3b82f6;
    transform: translateY(-4px);
    box-shadow: 0 8px 30px rgba(59, 130, 246, 0.2);

    .empty-icon {
      transform: scale(1.15) rotate(5deg);
    }

    .empty-title {
      color: #3b82f6;
    }

    .empty-tip {
      color: #60a5fa;
    }
  }

  &:active {
    transform: translateY(-2px);
  }

  .empty-icon {
    font-size: 100px;
    margin-bottom: 24px;
    opacity: 0.9;
    animation: float 3s ease-in-out infinite;
    transition: transform 0.3s ease;
    filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.1));
  }

  .empty-title {
    font-size: 20px;
    font-weight: 600;
    color: #1a1a1a;
    margin: 0 0 12px 0;
    transition: color 0.3s ease;
    letter-spacing: 0.5px;
  }

  .empty-tip {
    font-size: 15px;
    color: #666;
    margin: 0 0 24px 0;
    transition: color 0.3s ease;
    line-height: 1.6;
  }
}

// 右键菜单
.context-menu {
  position: fixed;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  padding: 8px 0;
  min-width: 180px;
  z-index: 9999;
  animation: context-menu-fadein 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #e4e7ed;
  backdrop-filter: blur(10px);

  .context-menu-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 18px;
    font-size: 14px;
    color: #303133;
    cursor: pointer;
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
    font-weight: 500;

    &:hover {
      background: linear-gradient(90deg, #eff6ff 0%, #ffffff 100%);
      color: #3b82f6;
      transform: translateX(2px);
    }

    &:active {
      transform: translateX(1px);
    }

    .el-icon {
      font-size: 18px;
      transition: transform 0.2s ease;
    }

    &:hover .el-icon {
      transform: scale(1.1);
    }

    &.danger {
      &:hover {
        background: linear-gradient(90deg, #fef0f0 0%, #ffffff 100%);
        color: #f56c6c;
      }
    }
  }

  .context-menu-divider {
    height: 1px;
    background: linear-gradient(90deg, transparent 0%, #e4e7ed 50%, transparent 100%);
    margin: 8px 0;
  }
}

// 添加好友对话框
.add-friend-content {
  .search-type-selector {
    margin-bottom: 12px;

    :deep(.el-radio-group) {
      display: flex;
      gap: 12px;

      .el-radio-button {
        .el-radio-button__inner {
          border-radius: 8px;
          border: 2px solid #e2e8f0;
          background: #fff;
          color: #4a5568;
          font-weight: 500;
          transition: all 0.3s ease;

          &:hover {
            border-color: #3b82f6;
            color: #3b82f6;
          }
        }

        &.is-active {
          .el-radio-button__inner {
            background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
            border-color: #3b82f6;
            color: #fff;
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
          }
        }
      }
    }
  }

  .friend-search-input {
    margin-bottom: 16px;

    :deep(.el-input) {
      .el-input__wrapper {
        border-radius: 10px 0 0 10px;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
        border: 2px solid transparent;
        transition: all 0.3s ease;

        &:hover,
        &.is-focus {
          border-color: #3b82f6;
          box-shadow: 0 4px 16px rgba(59, 130, 246, 0.2);
        }
      }

      .el-input__inner {
        font-size: 15px;
      }

      .el-input-group__append {
        background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
        border: none;
        color: #fff;
        border-radius: 0 10px 10px 0;
        padding: 0 20px;

        .el-button {
          background: transparent;
          border: none;
          color: #fff;
          font-weight: 500;

          &:hover {
            background: rgba(255, 255, 255, 0.1);
          }
        }
      }
    }
  }

  .search-results {
    margin-top: 16px;
    max-height: 400px;
    overflow-y: auto;
    padding-right: 4px;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-track {
      background: #f1f1f1;
      border-radius: 3px;
    }

    &::-webkit-scrollbar-thumb {
      background: linear-gradient(135deg, #c0c4cc 0%, #dcdfe6 100%);
      border-radius: 3px;

      &:hover {
        background: linear-gradient(135deg, #909399 0%, #b0b4bc 100%);
      }
    }

    .result-item {
      display: flex;
      align-items: center;
      padding: 14px 16px;
      border-radius: 10px;
      margin-bottom: 10px;
      background: linear-gradient(135deg, #ffffff 0%, #f7fafc 100%);
      transition: all 0.3s ease;
      border: 2px solid #e2e8f0;
      animation: fadeInUp 0.5s ease backwards;

      &:hover {
        background: #fff;
        border-color: #3b82f6;
        box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
        transform: translateX(4px);
      }

      .user-avatar {
        width: 48px;
        height: 48px;
        margin-right: 14px;
        flex-shrink: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
        border-radius: 50%;
        box-shadow: 0 4px 12px rgba(59, 130, 246, 0.35);
        overflow: hidden;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        span {
          font-size: 24px;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }

      .user-info {
        flex: 1;
        min-width: 0;

        .user-name {
          font-size: 14px;
          font-weight: 600;
          color: #2d3748;
          margin-bottom: 6px;
        }

        .user-detail {
          font-size: 12px;
          color: #718096;
        }
      }

      :deep(.el-button) {
        border-radius: 8px;
        padding: 10px 20px;
        font-weight: 500;
        background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
        border: none;
        flex-shrink: 0;
        transition: all 0.3s ease;

        &:hover:not(:disabled) {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
        }

        &:disabled {
          background: #cbd5e0;
          transform: none;
          box-shadow: none;
        }
      }
    }
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(15px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
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
      background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
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
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

:deep(.el-dialog__header) {
  background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
  padding: 20px 24px;
  margin: 0;

  .el-dialog__title {
    color: #fff;
    font-weight: 600;
    font-size: 17px;
  }

  .el-dialog__headerbtn {
    top: 20px;
    right: 20px;

    .el-dialog__close {
      color: #fff;
      font-size: 20px;

      &:hover {
        color: #f0f0f0;
      }
    }
  }
}

:deep(.el-dialog__body) {
  padding: 20px 24px;
  background: linear-gradient(to bottom, #eff6ff 0%, #ffffff 100%);
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  background-color: #eff6ff;
  border-top: 1px solid #dbeafe;
}

// 动画
@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 2px 8px rgba(59, 130, 246, 0.4);
  }
  50% {
    transform: scale(1.08);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.6);
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-12px);
  }
}

@keyframes letter-highlight {
  0% {
    transform: scale(1);
    box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
  }
  50% {
    transform: scale(1.03);
    box-shadow: 0 6px 16px rgba(59, 130, 246, 0.45);
  }
  100% {
    transform: scale(1);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
  }
}

@keyframes letter-active {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.25);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes context-menu-fadein {
  from {
    opacity: 0;
    transform: scale(0.92) translateY(-8px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}
</style>
