/**
 * 会话管理
 */
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

export function useConversations() {
  const conversations = ref([])
  const selectedConversation = ref(null)

  // 右键菜单
  const contextMenuVisible = ref(false)
  const selectedContextConversation = ref(null)
  const contextMenuPosition = ref({ x: 0, y: 0 })

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
  const deleteConversation = (conversation) => {
    const index = conversations.value.findIndex((item) => item.id === conversation.id)
    if (index !== -1) {
      conversations.value.splice(index, 1)
      contextMenuVisible.value = false
      selectedContextConversation.value = null

      if (selectedConversation.value?.id === conversation.id) {
        selectedConversation.value = null
      }

      ElMessage({
        message: '会话已删除',
        type: 'success'
      })

      // TODO: 调用后端API删除会话
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
  const updateConversationLastMessage = (sessionId, message) => {
    const conversation = conversations.value.find((conv) => conv.id === sessionId)
    if (conversation) {
      conversation.lastMessage = message.content
      conversation.time =
        message.time ||
        new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })

      // 如果不是当前会话，增加未读数
      if (selectedConversation.value?.id !== sessionId) {
        conversation.unreadCount = (conversation.unreadCount || 0) + 1
      }
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
    updateConversationLastMessage
  }
}
