/**
 * 会话管理
 */
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'

export function useConversations(userId = ref(null)) {
  const conversations = ref([])
  const selectedConversation = ref(null)
  const isLoadingSessions = ref(false)

  // 右键菜单
  const contextMenuVisible = ref(false)
  const selectedContextConversation = ref(null)
  const contextMenuPosition = ref({ x: 0, y: 0 })

  /**
   * 加载会话列表
   */
  const loadConversations = async () => {
    if (!userId.value) return

    try {
      isLoadingSessions.value = true
      const response = await api.get(`/v1/chat/users/${userId.value}/chat-sessions`)

      if (response.data && response.data.success) {
        conversations.value = response.data.data
        console.log('✅ [loadConversations] 会话列表加载成功，共', conversations.value.length, '个会话')
      }
    } catch (error) {
      console.error('❌ [loadConversations] 加载会话列表失败:', error)
    } finally {
      isLoadingSessions.value = false
    }
  }

  /**
   * 排序后的会话列表
   */
  const sortedConversations = computed(() => {
    return [...conversations.value].sort((a, b) => {
      // 置顶会话在前
      if (a.pinned && !b.pinned) return -1
      if (!a.pinned && b.pinned) return 1

      // 按时间降序排列
      return new Date(b.time) - new Date(a.time)
    })
  })

  /**
   * 显示右键菜单
   */
  const showContextMenu = (conversation, event) => {
    selectedContextConversation.value = conversation
    contextMenuPosition.value = {
      x: event.clientX,
      y: event.clientY
    }
    contextMenuVisible.value = true
  }

  /**
   * 关闭右键菜单
   */
  const closeContextMenu = () => {
    contextMenuVisible.value = false
    selectedContextConversation.value = null
  }

  /**
   * 切换置顶状态
   */
  const togglePin = (conversation) => {
    if (conversation.type === 'group') {
      ElMessage.info('群聊不支持置顶')
      return
    }

    conversation.pinned = !conversation.pinned
    contextMenuVisible.value = false
    selectedContextConversation.value = null

    ElMessage({
      message: conversation.pinned ? '会话已置顶' : '会话已取消置顶',
      type: 'success'
    })

    // TODO: 持久化到后端
  }

  /**
   * 删除会话
   */
  const deleteConversation = async (conversation) => {
    try {
      // 显示确认对话框
      await ElMessageBox.confirm(
        `确定要删除与 "${conversation.name}" 的会话吗？删除后聊天记录也将被清除。`,
        '删除会话',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning',
          distinguishCancelAndClose: true
        }
      )

      // 调用后端API删除会话
      console.log('🗑️ [deleteConversation] 删除会话:', conversation)

      const response = await api.delete(`/v1/chat/sessions/${conversation.id}`, {
        params: {
          userId: userId.value?.toString() || '1'
        }
      })

      if (response.code === '200' || response.success) {
        // 从前端列表中移除
        const index = conversations.value.findIndex((item) => item.id === conversation.id)
        if (index !== -1) {
          conversations.value.splice(index, 1)
        }

        // 关闭右键菜单
        contextMenuVisible.value = false
        selectedContextConversation.value = null

        // 如果删除的是当前选中的会话，清空选中状态
        if (selectedConversation.value?.id === conversation.id) {
          selectedConversation.value = null
        }

        console.log('✅ [deleteConversation] 会话删除成功')
        ElMessage.success('会话已删除')
      } else {
        console.error('❌ [deleteConversation] 删除会话失败:', response.message)
        ElMessage.error(response.message || '删除会话失败')
      }
    } catch (error) {
      if (error === 'cancel') {
        console.log('🚫 [deleteConversation] 用户取消删除')
        // 用户点击了取消，不做任何操作
      } else {
        console.error('❌ [deleteConversation] 删除会话异常:', error)
        ElMessage.error('删除会话失败，请稍后重试')
      }
    }
  }

  /**
   * 选择会话
   */
  const selectConversation = async (conversation, userId) => {
    selectedConversation.value = conversation

    // 清空未读消息
    if (conversation.unreadCount > 0) {
      try {
        // 调用后端API清空未读数
        await api.post(`/v1/chat/sessions/${conversation.id}/unread-clear`, {
          userId: userId.toString()
        })

        conversation.unreadCount = 0
        ElMessage.success('消息已标记为已读')
      } catch (error) {
        console.error('标记已读失败:', error)
        // 即使API调用失败，也清空前端未读数（用户体验优先）
        conversation.unreadCount = 0
      }
    }
  }

  /**
   * 更新会话最后一条消息
   */
  const updateConversationLastMessage = async (sessionId, message) => {
    const conversation = conversations.value.find((conv) => conv.id === sessionId)

    if (!conversation) {
      console.warn('⚠️ [updateConversationLastMessage] 未找到会话, sessionId:', sessionId)
      console.warn(
        '⚠️ [updateConversationLastMessage] 当前会话列表:',
        conversations.value.map((c) => ({ id: c.id, name: c.name }))
      )

      // ⭐ 如果找不到会话，可能是新会话，重新加载会话列表
      console.log('🔄 [updateConversationLastMessage] 重新加载会话列表...')
      await loadConversations()
      return
    }

    console.log('📝 [updateConversationLastMessage] 更新会话消息:', {
      sessionId,
      conversationName: conversation.name,
      oldMessage: conversation.lastMessage,
      newMessage: message.content
    })

    conversation.lastMessage = message.content
    conversation.time =
      message.time || new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })

    // 如果不是当前会话，增加未读数
    if (selectedConversation.value?.id !== sessionId) {
      const oldUnreadCount = conversation.unreadCount || 0
      conversation.unreadCount = oldUnreadCount + 1
      console.log('🔔 [updateConversationLastMessage] 未读数增加:', {
        sessionId,
        oldUnreadCount,
        newUnreadCount: conversation.unreadCount
      })
    }
  }

  return {
    conversations,
    selectedConversation,
    contextMenuVisible,
    selectedContextConversation,
    contextMenuPosition,
    sortedConversations,
    showContextMenu,
    closeContextMenu,
    togglePin,
    deleteConversation,
    selectConversation,
    updateConversationLastMessage,
    loadConversations,
    isLoadingSessions
  }
}
