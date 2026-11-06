<template>
  <div class="contacts-container">
    <!-- 页面头部：标准版标题容器（DESIGN-BASELINE §4.7） -->
    <AppPageHeader title="通讯录" :subtitle="`总好友 ${totalCount}`">
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openAddFriendDialog">
          添加好友
        </el-button>
      </template>
    </AppPageHeader>

    <!-- 搜索和筛选区域 -->
    <AppCard class="contacts-toolbar" :level="1" padding="md">
      <div class="toolbar-row">
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
    </AppCard>

    <!-- 好友请求列表 -->
    <div v-if="friendRequests.length > 0" class="friend-requests-section">
      <AppCard :level="1" padding="md">
        <div class="requests-header">
          <h2 class="requests-title">好友请求 ({{ friendRequests.length }})</h2>
          <el-button
            text
            size="small"
            @click="showFriendRequests = !showFriendRequests"
          >
            {{ showFriendRequests ? '收起' : '展开' }}
            <el-icon :class="{ 'is-expanded': showFriendRequests }">
              <component :is="ArrowDown" />
            </el-icon>
          </el-button>
        </div>

        <transition name="el-zoom-in-top">
          <div v-show="showFriendRequests" class="requests-list">
            <div
              v-for="request in friendRequests"
              :key="request.id"
              class="request-row"
            >
              <div class="request-avatar">
                <img
                  v-if="
                    shouldShowAvatarImage(
                      request.requesterInfo?.avatar,
                      `request-${request.userId ?? request.requesterInfo?.id ?? ''}`
                    )
                  "
                  :src="request.requesterInfo.avatar"
                  :alt="`${request.requesterInfo?.nickname || '未知用户'} 的头像`"
                  @error="handleAvatarError(`request-${request.userId ?? request.requesterInfo?.id ?? ''}`)"
                />
                <el-icon v-else class="avatar-fallback"><User /></el-icon>
              </div>

              <div class="request-info">
                <h3 class="request-name">
                  {{ request.requesterInfo?.nickname || '未知用户' }}
                </h3>
                <p class="request-id">用户ID: {{ request.requesterInfo?.id }}</p>
                <p v-if="request.createdAt" class="request-time">
                  {{ request.createdAt }}
                </p>
              </div>

              <div class="request-actions">
                <el-button
                  type="primary"
                  plain
                  size="default"
                  @click="handleAcceptRequest(request)"
                >
                  <el-icon><Select /></el-icon>
                  接受
                </el-button>
                <el-button size="default" @click="handleRejectRequest(request)">
                  <el-icon><Close /></el-icon>
                  拒绝
                </el-button>
              </div>
            </div>
          </div>
        </transition>
      </AppCard>
    </div>

    <!-- 内容区域：好友列表 + 字母索引 -->
    <div v-if="!loading && filteredFriends.length > 0" class="contacts-content">
      <AppCard class="contacts-list-card" :level="1" padding="none">
        <div class="contacts-list-layout">
          <div class="friends-list-wrapper">
            <!-- 列表字段表头：明确列含义（DESIGN-BASELINE §4.3） -->
            <div class="list-header">
              <span class="list-header__field">联系人</span>
              <span class="list-header__field">用户 ID</span>
              <span class="list-header__field list-header__field--action">操作</span>
            </div>

            <!-- 按字母分组的好友列表 -->
            <div class="friends-list-scroll">
              <div
                v-for="(group, letter) in groupedFriends"
                :key="letter"
                class="friend-group"
              >
                <!-- 字母分组标题 -->
                <div class="group-letter" :data-letter="letter">{{ letter }}</div>

                <!-- 该字母下的好友 -->
                <div
                  v-for="friend in group"
                  :key="friend.id"
                  class="friend-item"
                  role="button"
                  tabindex="0"
                  @click="startChat(friend)"
                  @keydown.enter="startChat(friend)"
                  @keydown.space.prevent="startChat(friend)"
                  @contextmenu.prevent="showContextMenu($event, friend)"
                >
                  <div class="friend-identity">
                    <div class="friend-avatar">
                      <img
                        v-if="shouldShowAvatarImage(friend.avatar, `friend-${friend.id}`)"
                        :src="friend.avatar"
                        :alt="`${friend.name} 的头像`"
                        @error="handleAvatarError(`friend-${friend.id}`)"
                      />
                      <el-icon v-else class="avatar-fallback"><User /></el-icon>
                    </div>
                    <span class="friend-name">{{ friend.name }}</span>
                  </div>

                  <div class="friend-id">ID: {{ friend.id }}</div>

                  <!-- 行操作：与行选中态绑定的操作区（DESIGN-BASELINE §4.1 / §4.6） -->
                  <div class="friend-actions">
                    <el-button
                      class="chat-btn"
                      :icon="ChatDotRound"
                      size="small"
                      @click.stop="startChat(friend)"
                    >
                      发消息
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 字母索引导航 -->
          <div class="alphabet-index">
            <button
              v-for="letter in availableLetters"
              :key="letter"
              type="button"
              class="alphabet-item"
              :class="{ active: activeLetter === letter }"
              @click="scrollToLetter(letter)"
            >
              {{ letter }}
            </button>
          </div>
        </div>
      </AppCard>
    </div>

    <!-- 加载状态 -->
    <div v-else-if="loading" class="loading-container">
      <AppCard :level="1" padding="md">
        <el-skeleton :rows="5" animated />
      </AppCard>
    </div>

    <!-- 空状态：图标 + 中文说明 + 替代动作（DESIGN-BASELINE §4.5） -->
    <div v-else class="empty-container">
      <AppCard :level="1" padding="lg">
        <AppEmptyState
          :icon="User"
          :description="emptyText"
          :action-text="emptyActionText"
          @action="handleEmptyAction"
        />
      </AppCard>
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
              <img
                v-if="shouldShowAvatarImage(user.avatar, `search-${user.id}`)"
                :src="user.avatar"
                :alt="`${user.name} 的头像`"
                @error="handleAvatarError(`search-${user.id}`)"
              />
              <el-icon v-else class="avatar-fallback"><User /></el-icon>
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
          <img
            v-if="shouldShowAvatarImage(selectedFriend.avatar, `detail-${selectedFriend.id}`)"
            :src="selectedFriend.avatar"
            :alt="`${selectedFriend.name} 的头像`"
            @error="handleAvatarError(`detail-${selectedFriend.id}`)"
          />
          <el-icon v-else class="avatar-fallback"><User /></el-icon>
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
import { ref, computed, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Plus,
  ChatDotRound,
  User,
  Delete,
  Select,
  Close,
  ArrowDown
} from '@element-plus/icons-vue'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import AppCard from '../../components/common/AppCard.vue'
import AppEmptyState from '../../components/common/AppEmptyState.vue'
import api from '../../utils/api'
import { decodeJwt } from '../../utils/api'
import { useAuthStore } from '../../store/authStore'

const router = useRouter()
const authStore = useAuthStore()

// 注入父组件提供的刷新方法
const refreshUnreadCount = inject('refreshUnreadCount', null)

// ========== 状态管理 ==========
const loading = ref(true)
const searchQuery = ref('')
const filterType = ref('all')
const friends = ref([])
const userId = ref(1)
const activeLetter = ref('')

// ========== 好友请求管理 ==========
const friendRequests = ref([])
const loadingRequests = ref(false)
const showFriendRequests = ref(false) // 控制是否显示好友请求列表

// ========== 头像加载失败记录 ==========
// 图片地址存在但实际 404 时，记录该 key 并回退到线性图标，避免碎图（DESIGN-BASELINE §4.2）
const avatarErrors = ref({})

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

// 空状态的替代动作：有筛选条件时先清筛选，否则去添加好友（DESIGN-BASELINE §4.5）
const emptyActionText = computed(() => {
  if (searchQuery.value.trim() || filterType.value !== 'all') {
    return '清除筛选'
  }
  return '添加好友'
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

// ========== 头像是否应渲染为图片 ==========
const shouldShowAvatarImage = (avatar, key) => {
  if (!isImageAvatar(avatar)) return false
  return !avatarErrors.value[key]
}

const handleAvatarError = (key) => {
  avatarErrors.value = { ...avatarErrors.value, [key]: true }
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

            const avatar = isValidAvatarUrl(userData.avatar) ? userData.avatar : ''

            return {
              id: contact.targetId,
              name: userData.nickname || userData.username || '好友',
              avatar: avatar,
              createdAt: contact.createdAt // 保留创建时间用于去重时选择最新的
            }
          } catch (error) {
            console.error(`❌ [Contacts] 获取好友 ${contact.targetId} 信息失败:`, error)
            // 返回默认信息，确保一个好友失败不影响其他好友
            return {
              id: contact.targetId,
              name: '好友',
              avatar: '',
              createdAt: contact.createdAt
            }
          }
        })
      )

      // 对相同用户的多个好友关系进行去重，只保留最新的一个
      const friendMap = new Map()
      friendsWithDetails.forEach((friend) => {
        const existingFriend = friendMap.get(friend.id)
        if (!existingFriend) {
          // 第一次添加该用户
          friendMap.set(friend.id, friend)
        } else {
          // 比较创建时间，保留最新的
          const existingTime = new Date(existingFriend.createdAt || 0).getTime()
          const newTime = new Date(friend.createdAt || 0).getTime()
          if (newTime > existingTime) {
            friendMap.set(friend.id, friend)
            console.log(`🔄 [Contacts] 发现重复好友关系，保留最新的 - id: ${friend.id}`)
          }
        }
      })

      // 转换回数组
      const uniqueFriends = Array.from(friendMap.values())

      friends.value = uniqueFriends
      console.log(`✅ [Contacts] 好友列表已更新 - 共 ${uniqueFriends.length} 个好友 (去重后)`)

      // 保存到本地缓存（保存去重后的数据）
      saveFriendsToLocal(uniqueFriends)
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

// ========== 空状态替代动作 ==========
const handleEmptyAction = () => {
  if (searchQuery.value.trim() || filterType.value !== 'all') {
    resetFilter()
    return
  }
  openAddFriendDialog()
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
    searchType: friendSearchType.value,
    userId: userId.value
  })

  try {
    const response = await api.get('/v1/users/search', {
      params: {
        keyword: friendSearchKeyword.value.trim(),
        searchType: friendSearchType.value,
        userId: userId.value // 传递当前用户ID，让后端过滤自己
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
        avatar: user.avatar || '',
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

// ========== 好友请求相关功能 ==========
/**
 * 加载好友请求列表
 */
const loadFriendRequests = async () => {
  loadingRequests.value = true
  try {
    console.log('Contacts: 开始加载好友请求')

    // 1. 获取当前用户的好友列表
    let friendIdSet = new Set()
    try {
      const friendsResponse = await api.get(`/v1/contacts/friends?userId=${userId.value}`)
      if (friendsResponse.code === '200') {
        friendIdSet = new Set(friendsResponse.data.map(contact => String(contact.targetId)))
        console.log('Contacts: 当前好友列表:', Array.from(friendIdSet))
      }
    } catch (error) {
      console.error('获取好友列表失败:', error)
    }

    // 2. 获取好友请求
    const response = await api.get(`/v1/contacts/friends/requests`, {
      params: { userId: userId.value }
    })

    console.log('Contacts: 获取到的好友请求:', response)

    if (response.code === '200' && response.data) {
      // 3. 对相同用户的请求进行去重，只保留最新的一个
      const requestMap = new Map()

      response.data.forEach((request) => {
        const requestUserId = request.userId || request.requesterInfo?.id
        if (requestUserId) {
          // 如果该用户已有请求，比较创建时间，保留最新的
          const existingRequest = requestMap.get(requestUserId)
          if (!existingRequest) {
            // 第一次添加该用户的请求
            requestMap.set(requestUserId, request)
          } else {
            // 比较创建时间，保留最新的
            const existingTime = new Date(existingRequest.createdAt || 0).getTime()
            const newTime = new Date(request.createdAt || 0).getTime()
            if (newTime > existingTime) {
              requestMap.set(requestUserId, request)
            }
          }
        }
      })

      // 转换回数组
      let uniqueRequests = Array.from(requestMap.values())
      console.log('Contacts: 去重后的好友请求:', uniqueRequests)

      // 4. 获取每个请求者的详细信息
      const requestsWithInfo = await Promise.all(
        uniqueRequests.map(async (request) => {
          try {
            const userResponse = await api.get(`/v1/users/${request.userId}`)
            if (userResponse.code === '200' && userResponse.data) {
              return {
                ...request,
                requesterInfo: {
                  id: userResponse.data.userId,
                  nickname: userResponse.data.nickname || '未知用户',
                  avatar: isImageAvatar(userResponse.data.avatar) ? userResponse.data.avatar : '',
                  phone: userResponse.data.phone,
                  email: userResponse.data.email
                }
              }
            }
          } catch (error) {
            console.error(`获取用户 ${request.userId} 信息失败:`, error)
          }

          // 降级方案：使用基本信息
          return {
            ...request,
            requesterInfo: {
              id: request.userId,
              nickname: '未知用户',
              avatar: ''
            }
          }
        })
      )

      // 5. 过滤掉已经是好友的请求
      const filteredRequests = requestsWithInfo.filter((request) => {
        const requestUserId = String(request.userId || request.requesterInfo?.id)
        const isFriend = friendIdSet.has(requestUserId)
        if (isFriend) {
          console.log(`Contacts: 过滤掉已是好友的请求 - userId: ${requestUserId}`)
        }
        return !isFriend
      })

      console.log('Contacts: 最终好友请求列表（已过滤好友）:', filteredRequests)
      friendRequests.value = filteredRequests
    } else {
      friendRequests.value = []
    }
  } catch (error) {
    console.error('加载好友请求失败:', error)
    friendRequests.value = []
  } finally {
    loadingRequests.value = false
  }
}

/**
 * 接受好友请求
 */
const handleAcceptRequest = async (request) => {
  try {
    const response = await api.post(`/v1/contacts/friends/accept`, {
      userId: userId.value,
      requesterId: request.userId
    })

    if (response.code === '200') {
      ElMessage.success('已接受好友请求')
      // 从列表中移除已处理的请求
      const index = friendRequests.value.findIndex((r) => r.id === request.id)
      if (index !== -1) {
        friendRequests.value.splice(index, 1)
      }
      // 刷新好友列表
      await fetchFriends()
      // 刷新父组件的未读徽章
      if (refreshUnreadCount) {
        refreshUnreadCount()
      }
    } else {
      ElMessage.error('接受好友请求失败: ' + response.message)
    }
  } catch (error) {
    console.error('接受好友请求失败:', error)
    ElMessage.error('接受好友请求失败')
  }
}

/**
 * 拒绝好友请求
 */
const handleRejectRequest = async (request) => {
  try {
    const response = await api.post(`/v1/contacts/friends/reject`, {
      userId: userId.value,
      requesterId: request.userId
    })

    if (response.code === '200') {
      ElMessage.success('已拒绝好友请求')
      // 从列表中移除已处理的请求
      const index = friendRequests.value.findIndex((r) => r.id === request.id)
      if (index !== -1) {
        friendRequests.value.splice(index, 1)
      }
      // 刷新父组件的未读徽章
      if (refreshUnreadCount) {
        refreshUnreadCount()
      }
    } else {
      ElMessage.error('拒绝好友请求失败: ' + response.message)
    }
  } catch (error) {
    console.error('拒绝好友请求失败:', error)
    ElMessage.error('拒绝好友请求失败')
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

    // 加载好友请求列表
    await loadFriendRequests()

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
/* 页面容器：单列居中，最大宽度 --page-max（DESIGN-BASELINE §3.3 / §4.7） */
.contacts-container {
  box-sizing: border-box;
  width: 100%;
  max-width: var(--page-max);
  min-height: 100%;
  margin: 0 auto;
  padding: 0 var(--page-gutter) var(--space-10);
  background: var(--canvas);
}

/* ===== 搜索 + 筛选工具条（§4.3：筛选条） ===== */
.contacts-toolbar {
  margin-bottom: var(--card-gap);
}

.toolbar-row {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.search-box {
  flex: 1;
  min-width: 0;
}

.filter-options {
  flex-shrink: 0;
}

.search-box :deep(.el-input__wrapper) {
  min-height: 36px;
  padding: 0 var(--space-3);
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  box-shadow: none;
  transition:
    border-color var(--duration-fast) var(--ease-out),
    outline-color var(--duration-fast) var(--ease-out);
}

.search-box :deep(.el-input__wrapper:hover) {
  border-color: var(--line-strong);
}

.search-box :deep(.el-input__wrapper.is-focus) {
  border-color: var(--brand-500);
  box-shadow: none;
  outline: 2px solid var(--brand-200);
  outline-offset: 1px;
}

.search-box :deep(.el-input__inner) {
  color: var(--ink-900);
  font-size: var(--text-body);
}

.search-box :deep(.el-input__prefix),
.search-box :deep(.el-input__suffix .el-icon) {
  color: var(--ink-400);
}

/* 分段筛选：用品牌色浅底强调，不做第二个实心主按钮（§3.1 / §4.1） */
.filter-options :deep(.el-radio-group),
.search-type-selector :deep(.el-radio-group) {
  gap: var(--space-2);
}

.filter-options :deep(.el-radio-button__inner),
.search-type-selector :deep(.el-radio-button__inner) {
  min-height: 32px;
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  background: var(--surface);
  color: var(--ink-700);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  box-shadow: none;
  transition:
    background-color var(--duration-fast) var(--ease-out),
    border-color var(--duration-fast) var(--ease-out),
    color var(--duration-fast) var(--ease-out);
}

.filter-options :deep(.el-radio-button__inner:hover),
.search-type-selector :deep(.el-radio-button__inner:hover) {
  border-color: var(--brand-300);
  color: var(--brand-600);
}

.filter-options :deep(.el-radio-button.is-active .el-radio-button__inner),
.search-type-selector :deep(.el-radio-button.is-active .el-radio-button__inner) {
  background: var(--brand-50);
  border-color: var(--brand-400);
  color: var(--brand-700);
  box-shadow: none;
}

/* ===== 好友请求 ===== */
.friend-requests-section {
  margin-bottom: var(--card-gap);
}

.requests-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
}

.requests-title {
  margin: 0;
  color: var(--ink-900);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
}

.requests-header :deep(.el-icon) {
  transition: transform var(--duration-base) var(--ease-out);
}

.requests-header :deep(.el-icon.is-expanded) {
  transform: rotate(180deg);
}

.requests-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.request-row {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-3);
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  background: var(--surface);
  transition:
    background-color var(--duration-fast) var(--ease-out),
    border-color var(--duration-fast) var(--ease-out);
}

.request-row:hover {
  border-color: var(--line-strong);
  background: var(--canvas);
}

.request-avatar {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: var(--radius-full);
  background: var(--canvas-sunken);
  color: var(--ink-400);
}

.request-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.request-info {
  flex: 1;
  min-width: 0;
}

.request-name {
  margin: 0 0 var(--space-1);
  color: var(--ink-900);
  font-size: var(--text-body);
  font-weight: var(--weight-semibold);
}

.request-id,
.request-time {
  margin: 0;
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  line-height: 20px;
}

.request-actions {
  display: flex;
  flex-shrink: 0;
  gap: var(--space-2);
}

.avatar-fallback {
  color: var(--ink-400);
  font-size: 20px;
}

/* ===== 好友列表 ===== */
.contacts-list-card {
  overflow: hidden;
}

.contacts-list-layout {
  display: flex;
  align-items: stretch;
  height: calc(100vh - 300px);
  min-height: 420px;
}

.friends-list-wrapper {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-width: 0;
}

/* 列表字段表头（§4.3：列头 → 数据行 → 行操作） */
.list-header {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 200px 112px;
  gap: var(--space-4);
  align-items: center;
  padding: var(--space-3) var(--space-5);
  background: var(--canvas-sunken);
  border-bottom: 1px solid var(--line);
}

.list-header__field {
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-semibold);
}

.list-header__field--action {
  text-align: right;
}

.friends-list-scroll {
  flex: 1;
  overflow-x: hidden;
  overflow-y: auto;
}

.friends-list-scroll::-webkit-scrollbar {
  width: 8px;
}

.friends-list-scroll::-webkit-scrollbar-track {
  background: transparent;
}

.friends-list-scroll::-webkit-scrollbar-thumb {
  background: var(--line-strong);
  border-radius: var(--radius-full);
}

.friends-list-scroll::-webkit-scrollbar-thumb:hover {
  background: var(--ink-400);
}

/* 字母分组分隔条：中性表面，只做分组，不冒充表头 */
.group-letter {
  position: sticky;
  top: 0;
  z-index: 10;
  padding: var(--space-2) var(--space-5);
  background: var(--canvas);
  color: var(--ink-500);
  font-size: var(--text-caption);
  font-weight: var(--weight-semibold);
  letter-spacing: var(--tracking-wide);
  border-bottom: 1px solid var(--line);
}

.friend-item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 200px 112px;
  gap: var(--space-4);
  align-items: center;
  box-sizing: border-box;
  min-height: 80px;
  padding: var(--space-2) var(--space-5);
  background: var(--surface);
  border-bottom: 1px solid var(--line);
  cursor: pointer;
  transition: background-color var(--duration-fast) var(--ease-out);
}

/* 选中态统一用暖橙强调（§3.1 / §4.6） */
.friend-item:hover {
  background: var(--brand-50);
}

.friend-item:active {
  background: var(--brand-100);
}

.friend-item:focus-visible {
  background: var(--brand-50);
  outline: 2px solid var(--brand-400);
  outline-offset: -2px;
}

.friend-identity {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  min-width: 0;
}

.friend-avatar {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: var(--radius-full);
  background: var(--canvas-sunken);
  color: var(--ink-400);
}

.friend-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.friend-name {
  min-width: 0;
  overflow: hidden;
  color: var(--ink-900);
  font-size: var(--text-body);
  font-weight: var(--weight-medium);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.friend-id {
  overflow: hidden;
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  font-variant-numeric: tabular-nums;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.friend-actions {
  display: flex;
  justify-content: flex-end;
}

/* 行操作：够大的点击区域 + 与行状态联动的反馈（§4.1 / §4.6） */
.chat-btn {
  --el-button-bg-color: transparent;
  --el-button-border-color: transparent;
  --el-button-text-color: var(--brand-600);
  --el-button-hover-bg-color: var(--brand-50);
  --el-button-hover-border-color: transparent;
  --el-button-hover-text-color: var(--brand-700);
  --el-button-active-bg-color: var(--brand-100);
  --el-button-active-border-color: transparent;
  --el-button-active-text-color: var(--brand-700);
  min-height: 32px;
  padding: 0 var(--space-3);
  border-radius: var(--radius-md);
  font-weight: var(--weight-medium);
  transition:
    background-color var(--duration-fast) var(--ease-out),
    color var(--duration-fast) var(--ease-out);
}

/* ===== 字母索引 ===== */
.alphabet-index {
  display: flex;
  flex-shrink: 0;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  width: 56px;
  padding: var(--space-3) 0;
  overflow-y: auto;
  background: var(--canvas-sunken);
  border-left: 1px solid var(--line);
}

.alphabet-item {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
  border: 0;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  cursor: pointer;
  transition:
    background-color var(--duration-fast) var(--ease-out),
    color var(--duration-fast) var(--ease-out);
}

.alphabet-item:hover {
  background: var(--brand-50);
  color: var(--brand-600);
}

.alphabet-item.active {
  background: var(--brand-100);
  color: var(--brand-700);
  font-weight: var(--weight-semibold);
}

.alphabet-item:focus-visible {
  outline: 2px solid var(--brand-400);
  outline-offset: 1px;
}

/* ===== 加载 / 空状态 ===== */
.loading-container {
  min-height: 400px;
}

.empty-container {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-container > * {
  width: 100%;
}

/* ===== 右键菜单 ===== */
.context-menu {
  position: fixed;
  z-index: 3000;
  min-width: 180px;
  padding: var(--space-2) 0;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  animation: menu-in var(--duration-base) var(--ease-out);
}

.context-menu-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  color: var(--ink-700);
  font-size: var(--text-body);
  cursor: pointer;
  transition:
    background-color var(--duration-fast) var(--ease-out),
    color var(--duration-fast) var(--ease-out);
}

.context-menu-item:hover {
  background: var(--canvas);
  color: var(--ink-900);
}

.context-menu-item.danger {
  color: var(--danger);
}

.context-menu-item.danger:hover {
  background: var(--danger-bg);
  color: var(--danger);
}

.context-menu-divider {
  height: 1px;
  margin: var(--space-2) 0;
  background: var(--line);
}

/* ===== 对话框 ===== */
:deep(.el-dialog) {
  overflow: hidden;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
}

:deep(.el-dialog__header) {
  padding: var(--space-5) var(--space-6);
  margin: 0;
  background: var(--surface);
  border-bottom: 1px solid var(--line);
}

:deep(.el-dialog__title) {
  color: var(--ink-900);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
}

:deep(.el-dialog__headerbtn .el-dialog__close) {
  color: var(--ink-500);
}

:deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: var(--brand-600);
}

:deep(.el-dialog__body) {
  padding: var(--space-5) var(--space-6);
  background: var(--surface);
}

:deep(.el-dialog__footer) {
  padding: var(--space-4) var(--space-6);
  background: var(--canvas);
  border-top: 1px solid var(--line);
}

.add-friend-content .search-type-selector {
  margin-bottom: var(--space-3);
}

.add-friend-content .friend-search-input {
  margin-bottom: var(--space-4);
}

.friend-search-input :deep(.el-input__wrapper) {
  min-height: 36px;
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  box-shadow: none;
}

.friend-search-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--brand-500);
  box-shadow: none;
  outline: 2px solid var(--brand-200);
  outline-offset: 1px;
}

.friend-search-input :deep(.el-input-group__append) {
  background: var(--surface);
  border: 1px solid var(--line);
  border-left: 0;
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
}

.friend-search-input :deep(.el-input__inner) {
  color: var(--ink-900);
  font-size: var(--text-body);
}

.search-results {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  max-height: 400px;
  overflow-y: auto;
}

.result-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  background: var(--surface);
  transition:
    background-color var(--duration-fast) var(--ease-out),
    border-color var(--duration-fast) var(--ease-out);
}

.result-item:hover {
  border-color: var(--brand-200);
  background: var(--brand-50);
}

.user-avatar {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: var(--radius-full);
  background: var(--canvas-sunken);
  color: var(--ink-400);
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  margin-bottom: 2px;
  color: var(--ink-900);
  font-size: var(--text-body);
  font-weight: var(--weight-medium);
}

.user-detail {
  color: var(--ink-500);
  font-size: var(--text-body-sm);
}

/* ===== 好友详情 ===== */
.friend-detail {
  text-align: center;
}

.detail-avatar {
  display: flex;
  justify-content: center;
  margin-bottom: var(--space-5);
}

.detail-avatar img,
.detail-avatar .avatar-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 96px;
  height: 96px;
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: var(--radius-full);
  background: var(--canvas-sunken);
  color: var(--ink-400);
}

.detail-avatar img {
  object-fit: cover;
}

.detail-avatar .avatar-fallback {
  font-size: 40px;
}

.detail-info {
  text-align: left;
}

.info-item {
  margin-bottom: var(--space-3);
  font-size: var(--text-body);
}

.info-item .label {
  margin-right: var(--space-2);
  color: var(--ink-500);
}

.info-item .value {
  color: var(--ink-900);
}

@keyframes menu-in {
  from {
    opacity: 0;
    transform: translateY(-4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
