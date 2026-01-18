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
        friends.value = response.data.map((contact) => ({
          id: contact.targetId,
          name: '好友', // 需要从用户信息接口获取真实名称
          avatar: '👤', // 需要从用户信息接口获取真实头像
          lastMessage: '',
          time: '',
          unreadCount: 0,
          type: 'friend'
        }))
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
    sendFriendRequest
  }
}
