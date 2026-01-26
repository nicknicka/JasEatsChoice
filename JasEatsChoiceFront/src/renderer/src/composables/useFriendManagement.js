/**
 * 好友管理 Composable
 * 负责好友列表获取、搜索用户、添加好友、新建聊天等功能
 */
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'

/**
 * 验证并处理用户头像数据
 * @param {string} avatar - 原始头像数据
 * @returns {string} 处理后的头像数据（有效头像或默认emoji）
 */
const processAvatar = (avatar) => {
  // 如果头像为空，返回默认头像
  if (!avatar) {
    return '👤'
  }

  // 如果是默认emoji或短文本，直接返回
  if (avatar === '👤' || avatar.length <= 10) {
    return avatar
  }

  // 如果是HTTP/HTTPS URL，直接返回
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }

  // 如果是base64格式，验证其完整性
  if (avatar.startsWith('data:image')) {
    const parts = avatar.split(',')
    if (parts.length < 2) {
      // base64格式不正确
      console.warn('头像base64格式不正确，已使用默认头像')
      return '👤'
    }

    const base64Data = parts[1]
    if (!base64Data || base64Data.length < 100) {
      // base64数据太短，可能被截断
      console.warn('头像base64数据不完整，长度:', base64Data?.length, '已使用默认头像')
      return '👤'
    }

    // base64数据完整，返回原始数据
    return avatar
  }

  // 如果不是以上任何一种有效格式，使用默认头像
  console.warn('头像格式未知，已使用默认头像。原始数据:', typeof avatar === 'string' ? avatar.substring(0, 50) : avatar)
  return '👤'
}

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
                  avatar: processAvatar(userResponse.data.avatar),
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
      // 传递当前用户ID，让后端在数据库层面过滤自己
      searchParams.append('userId', String(userId.value))

      const response = await api.get(`/v1/users/search?${searchParams.toString()}`)

      if (response.code === '200') {
        // 获取当前用户的好友列表
        let friendIdSet = new Set()
        try {
          const friendsResponse = await api.get(`/v1/contacts/friends?userId=${userId.value}`)
          if (friendsResponse.code === '200') {
            friendIdSet = new Set(friendsResponse.data.map(contact => String(contact.targetId)))
          }
        } catch (error) {
          console.error('获取好友列表失败:', error)
        }

        // 前端只过滤已经是好友的用户（后端已过滤自己）
        const filteredUsers = response.data
          .filter((user) => {
            const userIdStr = String(user.userId)
            // 只过滤已经是好友的用户
            return !friendIdSet.has(userIdStr)
          })
          .map((user) => ({
            id: user.userId,
            nickname: user.nickname,
            username: user.username,
            phone: user.phone,
            email: user.email,
            avatar: processAvatar(user.avatar),
            isFriend: false
          }))

        return filteredUsers
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
   * 检查是否已有待处理的好友请求
   */
  const hasPendingRequest = async (targetId) => {
    try {
      const response = await api.get(`/v1/contacts/friends/requests`, {
        params: { userId: userId.value }
      })
      if (response.code === '200') {
        // 使用字符串比较，确保类型一致
        const targetIdStr = String(targetId)
        const userIdStr = String(userId.value)
        return response.data.some(request =>
          String(request.userId) === targetIdStr || String(request.targetId) === userIdStr
        )
      }
      return false
    } catch (error) {
      console.error('检查好友请求失败:', error)
      return false
    }
  }

  /**
   * 发送好友请求
   */
  const sendFriendRequest = async (user) => {
    try {
      // 1. 检查是否已有待处理的好友请求
      const pendingRequest = await hasPendingRequest(user.id)

      // 如果已有待处理请求，给出提示但仍允许发送
      if (pendingRequest) {
        const userName = user.nickname || user.username || user.email || user.phone || '未知用户'
        // 使用确认对话框让用户选择是否继续
        try {
          await ElMessageBox.confirm(
            `你已向 ${userName} 发送过好友请求，或对方已向你发送请求。确定要再次发送吗？`,
            '重复发送提示',
            {
              confirmButtonText: '继续发送',
              cancelButtonText: '取消',
              type: 'warning'
            }
          )
        } catch {
          // 用户取消
          return false
        }
      }

      // 2. 发送好友请求
      // 确保ID转换为字符串类型
      const requestData = {
        userId: String(userId.value),
        targetId: String(user.id)
      }

      console.log('发送好友请求，参数:', requestData)

      const response = await api.post(`/v1/contacts/friends/request`, requestData)

      console.log('好友请求响应:', response)

      if (response.code === '200') {
        const userName = user.nickname || user.username || user.email || user.phone || '未知用户'
        ElMessage.success(`已向 ${userName} 发送好友请求`)
        return true
      } else {
        console.error('发送好友请求失败，响应:', response)
        const errorMsg = response.message || response.data || '未知错误'
        ElMessage.error('发送好友请求失败: ' + errorMsg)
        return false
      }
    } catch (error) {
      if (error !== 'cancel') {
        console.error('发送好友请求异常:', error)
        const errorMsg = error.response?.data?.message || error.message || '网络错误'
        ElMessage.error('发送好友请求失败: ' + errorMsg)
      }
      return false
    }
  }

  /**
   * 获取待处理的好友请求列表
   */
  const getFriendRequests = async () => {
    try {
      console.log('开始获取好友请求，用户ID:', userId.value)
      const response = await api.get(`/v1/contacts/friends/requests`, {
        params: { userId: userId.value }
      })

      console.log('好友请求响应:', response)

      if (response.code === '200') {
        console.log('原始好友请求列表:', response.data)

        // 获取每个请求者的详细信息
        const requestsWithInfo = await Promise.all(
          response.data.map(async (contact) => {
            console.log('处理联系人:', contact)
            try {
              const userResponse = await api.get(`/v1/users/${contact.userId}`)
              console.log(`用户 ${contact.userId} 信息响应:`, userResponse)

              if (userResponse.code === '200' && userResponse.data) {
                const userInfo = {
                  ...contact,
                  requesterInfo: {
                    id: userResponse.data.userId,
                    nickname: userResponse.data.nickname || '未知用户',
                    avatar: processAvatar(userResponse.data.avatar),
                    phone: userResponse.data.phone,
                    email: userResponse.data.email
                  }
                }
                console.log('成功构建用户信息:', userInfo)
                return userInfo
              } else {
                console.warn(`获取用户 ${contact.userId} 失败，响应码:`, userResponse.code)
              }
            } catch (error) {
              console.error(`获取用户 ${contact.userId} 信息失败:`, error)
            }

            // 降级方案：使用基本信息
            const fallbackInfo = {
              ...contact,
              requesterInfo: {
                id: contact.userId,
                nickname: '未知用户',
                avatar: '👤'
              }
            }
            console.log('使用降级方案:', fallbackInfo)
            return fallbackInfo
          })
        )

        console.log('最终好友请求列表:', requestsWithInfo)
        return requestsWithInfo
      } else {
        console.error('获取好友请求失败，响应码:', response.code)
        ElMessage.error('获取好友请求失败: ' + response.message)
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
    rejectFriendRequest,

    // 工具函数
    processAvatar
  }
}
