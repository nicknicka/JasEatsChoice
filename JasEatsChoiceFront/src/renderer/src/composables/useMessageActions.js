/**
 * 消息操作功能（搜索、导出、撤回等）
 */
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MESSAGE_CONFIG, MESSAGE_STATUS } from '@/constants/chatConstants'
import { debounce } from 'lodash-es'
import api from '@/utils/api.js'

export function useMessageActions({ chatHistory, chatMessages, userId, formatMessageTime }) {
  // 搜索相关
  const searchKeyword = ref('')
  const messageSearchResults = ref([])
  const isSearching = ref(false)
  const currentSearchIndex = ref(-1)

  // 回复相关
  const replyingTo = ref(null)

  // 转发相关
  const forwardDialogVisible = ref(false)
  const forwardMessage = ref(null)
  const selectedForwardTarget = ref('')

  /**
   * 搜索消息（带防抖）
   */
  const searchMessages = debounce(() => {
    if (!searchKeyword.value.trim()) {
      isSearching.value = false
      messageSearchResults.value = []
      return
    }

    const keyword = searchKeyword.value.toLowerCase().trim()
    const results = []

    chatMessages.value.forEach((msg, index) => {
      if (msg.content && msg.content.toLowerCase().includes(keyword)) {
        // 高亮关键词
        const highlightedContent = msg.content.replace(
          new RegExp(`(${keyword})`, 'gi'),
          '<mark>$1</mark>'
        )

        results.push({
          ...msg,
          originalIndex: index,
          highlightedContent: highlightedContent
        })
      }
    })

    messageSearchResults.value = results
    isSearching.value = results.length > 0

    if (results.length > 0) {
      ElMessage.success(`找到 ${results.length} 条相关消息`)
    } else {
      ElMessage.info('未找到相关消息')
    }
  }, MESSAGE_CONFIG.SEARCH_DEBOUNCE_DELAY)

  /**
   * 跳转到指定搜索结果
   */
  const jumpToSearchResult = (index) => {
    currentSearchIndex.value = index
    const result = messageSearchResults.value[index]

    if (!result) return

    setTimeout(() => {
      const messageElement = document.querySelector(`[data-message-id="${result.id}"]`)
      if (messageElement) {
        messageElement.scrollIntoView({ behavior: 'smooth', block: 'center' })
        messageElement.style.backgroundColor = '#fff3cd'
        setTimeout(() => {
          messageElement.style.backgroundColor = ''
        }, 2000)
      }
    }, 100)
  }

  /**
   * 清除搜索
   */
  const clearSearch = () => {
    searchKeyword.value = ''
    isSearching.value = false
    messageSearchResults.value = []
    currentSearchIndex.value = -1
  }

  /**
   * 导出聊天记录
   */
  const exportChatHistory = (selectedConversation) => {
    if (!selectedConversation) {
      ElMessage.warning('请先选择一个会话')
      return
    }

    const messages = chatHistory.value[selectedConversation.id] || chatMessages.value

    if (messages.length === 0) {
      ElMessage.info('当前会话暂无聊天记录')
      return
    }

    // 格式化导出内容
    let content = `与 ${selectedConversation.name} 的聊天记录\n`
    content += `导出时间: ${new Date().toLocaleString('zh-CN')}\n`
    content += `消息数量: ${messages.length} 条\n`
    content += `${'='.repeat(50)}\n\n`

    messages.forEach((msg) => {
      const time = msg.formattedTime || formatMessageTime(msg.createTime || msg.time)
      const sender = msg.fromId === userId.value.toString() ? '我' : msg.senderName || msg.fromId
      const status = msg.readStatus !== false ? '' : ' (未读)'

      content += `[${time}] ${sender}${status}\n`
      content += `${msg.content}\n`
      content += `${'-'.repeat(30)}\n\n`
    })

    // 创建Blob并下载
    const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `聊天记录_${selectedConversation.name}_${new Date()
      .toLocaleDateString('zh-CN')
      .replace(/\//g, '-')}.txt`
    a.click()

    URL.revokeObjectURL(url)
    ElMessage.success('聊天记录已导出')
  }

  /**
   * 判断消息是否可以撤回
   */
  const canRecallMessage = (message) => {
    if (!message.createTime && !message.time) return false

    const msgTime = new Date(message.createTime || message.time)
    const now = new Date()
    const diffMinutes = (now - msgTime) / 1000 / 60

    return diffMinutes <= MESSAGE_CONFIG.RECALL_TIME_LIMIT && message.content !== '消息已撤回'
  }

  /**
   * 撤回消息
   */
  const recallMessage = async (message, chatMessages, selectedConversation) => {
    try {
      await ElMessageBox.confirm('确认撤回这条消息吗？', '撤回消息', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      })

      const response = await api.post(`/v1/chat/messages/${message.id}/recall`, {
        userId: userId.value.toString()
      })

      if (response.code === '200') {
        const index = chatMessages.value.findIndex((msg) => msg.id === message.id)
        if (index !== -1) {
          chatMessages.value[index].content = '消息已撤回'
        }

        if (selectedConversation.value) {
          selectedConversation.value.lastMessage = '消息已撤回'
        }

        ElMessage.success('消息已撤回')
      }
    } catch (error) {
      if (error !== 'cancel') {
        console.error('撤回消息失败:', error)
        ElMessage.error('撤回消息失败')
      }
    }
  }

  /**
   * 处理消息操作命令
   */
  const handleMessageCommand = async (command, message, conversations) => {
    switch (command) {
      case 'recall':
        await recallMessage(message, chatMessages, selectedConversation)
        break
      case 'reply':
        replyingTo.value = message
        document.querySelector('.chat-input textarea')?.focus()
        break
      case 'forward':
        showForwardDialog(message)
        break
      case 'copy':
        await copyMessageContent(message.content)
        break
    }
  }

  /**
   * 复制消息内容
   */
  const copyMessageContent = async (content) => {
    try {
      await navigator.clipboard.writeText(content)
      ElMessage.success('已复制到剪贴板')
    } catch (error) {
      // 降级方案
      const textarea = document.createElement('textarea')
      textarea.value = content
      document.body.appendChild(textarea)
      textarea.select()
      try {
        document.execCommand('copy')
        ElMessage.success('已复制到剪贴板')
      } catch (err) {
        ElMessage.error('复制失败')
      }
      document.body.removeChild(textarea)
    }
  }

  /**
   * 显示转发对话框
   */
  const showForwardDialog = (message) => {
    forwardMessage.value = message
    selectedForwardTarget.value = ''
    forwardDialogVisible.value = true
  }

  /**
   * 确认转发
   */
  const confirmForward = async () => {
    if (!selectedForwardTarget.value) {
      ElMessage.warning('请选择转发目标')
      return
    }

    // 由于这个函数在 composable 中无法直接访问 conversations 和 selectedConversation
    // 这里只返回需要的数据，由调用方处理
    try {
      const messageData = {
        fromId: userId.value.toString(),
        toId: selectedForwardTarget.value,
        msgType: 'single',
        content: forwardMessage.value.content,
        forwardedFrom: forwardMessage.value.id
      }

      const response = await api.post('/v1/chat/messages', messageData)

      if (response.code === '200') {
        ElMessage.success('转发成功')
        forwardDialogVisible.value = false
        return response.data
      }
    } catch (error) {
      console.error('转发失败:', error)
      ElMessage.error('转发失败')
    }
  }

  /**
   * 取消回复
   */
  const cancelReply = () => {
    replyingTo.value = null
  }

  return {
    searchKeyword,
    messageSearchResults,
    isSearching,
    currentSearchIndex,
    replyingTo,
    forwardDialogVisible,
    forwardMessage,
    selectedForwardTarget,
    searchMessages,
    clearSearch,
    jumpToSearchResult,
    exportChatHistory,
    canRecallMessage,
    handleMessageCommand,
    showForwardDialog,
    confirmForward,
    cancelReply
  }
}
