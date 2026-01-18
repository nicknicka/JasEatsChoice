/**
 * 好友管理 Composable
 * 负责好友列表获取、搜索用户、添加好友、新建聊天等功能
 */
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

export function useFriendManagement({ userId, conversations, chatHistory }) {
  // ========== 状态管理 ==========
  const friends = ref([])
  const searchQuery = ref('')
  const searchResults = ref([])

  // 对话框状态
  const newChatDialogVisible = ref(false)

  /**
   * 从后端获取好友列表
   */
  const fetchFriends = async () => {
    try {
      const response = await api.get(`/v1/contacts/friends?userId=${userId.value}`)
      if (response.code === '200') {
        // 获取每个好友的详细信息
        const friendsWithInfo = await Promise.all(
          response.data.map(async (contact) => {
            try {
              const userResponse = await api.get(`/v1/users/${contact.targetId}`)
              if (userResponse.code === '200' && userResponse.data) {
                return {
                  id: contact.targetId,
                  name: userResponse.data.nickname || userResponse.data.username || '好友',
                  avatar: userResponse.data.avatar || '👤',
                  lastMessage: '',
                  time: '',
                  unreadCount: 0,
                  type: 'friend'
                }
              }
            } catch (error) {
              console.error(`获取好友 ${contact.targetId} 信息失败:`, error)
            }
            // 降级方案：显示基本信息
            return {
              id: contact.targetId,
              name: '好友',
              avatar: '👤',
              lastMessage: '',
              time: '',
              unreadCount: 0,
              type: 'friend'
            }
          })
        )
        friends.value = friendsWithInfo
      }
    } catch (error) {
      console.error('获取好友列表失败:', error)
    }
  }

  /**
   * 打开新建聊天对话框
   */
  const openNewChatDialog = () => {
    newChatDialogVisible.value = true
    searchResults.value = [...friends.value]
  }

  /**
   * 搜索好友
   */
  const searchFriends = () => {
    if (!searchQuery.value) {
      searchResults.value = [...friends.value]
    } else {
      searchResults.value = friends.value.filter((friend) =>
        friend.name.includes(searchQuery.value)
      )
    }
  }

  /**
   * 检查是否已有会话
   */
  const hasExistingConversation = (friendId) => {
    return conversations.value?.some(
      (conv) => (conv.id === friendId && conv.type === 'friend') || conv.type === 'private'
    )
  }

  /**
   * 选择好友开始聊天
   */
  const selectFriendForChat = (friend) => {
    const existingConversation = conversations.value.find((conv) => conv.id === friend.id)

    if (existingConversation) {
      // 已有会话，切换到该会话
      return existingConversation
    } else {
      // 创建新会话
      const newConversation = {
        ...friend,
        lastMessage: '开始聊天吧！',
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      }

      conversations.value.unshift(newConversation)
      chatHistory.value[newConversation.id] = []

      return newConversation
    }
  }

  /**
   * 搜索用户（用于加好友）
   * @param {Object} params - 搜索参数 { keyword, searchType }
   * @returns {Promise} 搜索结果
   */
  const searchUsers = async (params) => {
    try {
      const searchParams = new URLSearchParams()
      searchParams.append('keyword', encodeURIComponent(params.keyword))
      if (params.searchType) {
        searchParams.append('searchType', params.searchType)
      }

      const response = await api.get(`/v1/users/search?${searchParams.toString()}`)

      if (response.code === '200') {
        return response.data.map((user) => ({
          id: user.userId,
          nickname: user.nickname,
          username: user.username,
          phone: user.phone,
          email: user.email,
          avatar: '👤',
          isFriend: false
        }))
      } else {
        ElMessage.error('搜索用户失败')
        return []
      }
    } catch (error) {
      console.error('搜索用户失败:', error)
      ElMessage.error('搜索用户失败')
      return []
    }
  }

  /**
   * 发送好友请求
   */
  const sendFriendRequest = async (user) => {
    try {
      const response = await api.post(`/v1/contacts/friends/request`, {
        userId: userId.value,
        targetId: user.id
      })

      if (response.code === '200') {
        const userName = user.nickname || user.username || user.email || user.phone || '未知用户'
        ElMessage.success(`已向 ${userName} 发送好友请求`)
        return true
      } else {
        ElMessage.error('发送好友请求失败: ' + response.message)
        return false
      }
    } catch (error) {
      console.error('发送好友请求失败:', error)
      ElMessage.error('发送好友请求失败')
      return false
    }
  }

  /**
   * 获取待处理的好友请求列表
   */
  const getFriendRequests = async () => {
    try {
      const response = await api.get(`/v1/contacts/friends/requests`, {
        params: { userId: userId.value }
      })

      if (response.code === '200') {
        // 获取每个请求者的详细信息
        const requestsWithInfo = await Promise.all(
          response.data.map(async (contact) => {
            try {
              const userResponse = await api.get(`/v1/users/${contact.userId}`)
              if (userResponse.code === '200' && userResponse.data) {
                return {
                  ...contact,
                  requesterInfo: {
                    id: userResponse.data.userId,
                    nickname: userResponse.data.nickname || userResponse.data.username,
                    username: userResponse.data.username,
                    avatar: userResponse.data.avatar,
                    phone: userResponse.data.phone,
                    email: userResponse.data.email
                  }
                }
              }
            } catch (error) {
              console.error(`获取用户 ${contact.userId} 信息失败:`, error)
            }
            return {
              ...contact,
              requesterInfo: {
                id: contact.userId,
                nickname: '未知用户',
                avatar: '👤'
              }
            }
          })
        )
        return requestsWithInfo
      } else {
        ElMessage.error('获取好友请求失败')
        return []
      }
    } catch (error) {
      console.error('获取好友请求失败:', error)
      ElMessage.error('获取好友请求失败')
      return []
    }
  }

  /**
   * 接受好友请求
   */
  const acceptFriendRequest = async (requesterId) => {
    try {
      const response = await api.post(`/v1/contacts/friends/accept`, {
        userId: userId.value,
        requesterId: requesterId
      })

      if (response.code === '200') {
        ElMessage.success('已接受好友请求')
        // 刷新好友列表
        await fetchFriends()
        return true
      } else {
        ElMessage.error('接受好友请求失败: ' + response.message)
        return false
      }
    } catch (error) {
      console.error('接受好友请求失败:', error)
      ElMessage.error('接受好友请求失败')
      return false
    }
  }

  /**
   * 拒绝好友请求
   */
  const rejectFriendRequest = async (requesterId) => {
    try {
      const response = await api.post(`/v1/contacts/friends/reject`, {
        userId: userId.value,
        requesterId: requesterId
      })

      if (response.code === '200') {
        ElMessage.success('已拒绝好友请求')
        return true
      } else {
        ElMessage.error('拒绝好友请求失败: ' + response.message)
        return false
      }
    } catch (error) {
      console.error('拒绝好友请求失败:', error)
      ElMessage.error('拒绝好友请求失败')
      return false
    }
  }

  return {
    // 状态
    friends,
    searchQuery,
    searchResults,
    newChatDialogVisible,

    // 方法
    fetchFriends,
    openNewChatDialog,
    searchFriends,
    hasExistingConversation,
    selectFriendForChat,
    searchUsers,
    sendFriendRequest,
    getFriendRequests,
    acceptFriendRequest,
    rejectFriendRequest
  }
}
