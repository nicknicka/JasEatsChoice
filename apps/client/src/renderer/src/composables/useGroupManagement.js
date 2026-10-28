/**
 * 群聊管理 Composable
 * 负责群列表获取、创建群聊、群详情等功能
 */
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

export function useGroupManagement({ userId, conversations, chatMessages }) {
  // ========== 状态管理 ==========
  const groups = ref([])

  // 新建群聊对话框
  const groupDialogVisible = ref(false)
  const groupForm = ref({
    name: '',
    members: ''
  })

  // 好友选择对话框
  const friendSelectionDialogVisible = ref(false)
  const selectedGroupMembers = ref([])

  // 群详情对话框
  const groupDetailDialogVisible = ref(false)
  const currentGroupInfo = ref(null)

  /**
   * 从后端获取群列表
   */
  const fetchGroups = async () => {
    try {
      const response = await api.get(`/v1/groups/my?userId=${userId.value}`)
      if (response.code === '200') {
        groups.value = response.data.map((group) => ({
          id: group.id,
          name: group.groupName,
          avatar: '👥',
          lastMessage: '',
          time: '',
          unreadCount: 0,
          type: 'group',
          memberCount: group.memberCount || 0
        }))
      }
    } catch (error) {
      console.error('获取群列表失败:', error)
    }
  }

  /**
   * 打开新建群聊对话框
   */
  const openCreateGroupDialog = () => {
    groupDialogVisible.value = true
    selectedGroupMembers.value = []
    groupForm.value = {
      name: '',
      members: ''
    }
  }

  /**
   * 显示好友选择对话框
   */
  const showFriendSelectionDialog = () => {
    friendSelectionDialogVisible.value = true
  }

  /**
   * 切换好友选择状态
   */
  const toggleFriendSelection = (friendId) => {
    const index = selectedGroupMembers.value.indexOf(friendId)
    if (index === -1) {
      selectedGroupMembers.value.push(friendId)
    } else {
      selectedGroupMembers.value.splice(index, 1)
    }
  }

  /**
   * 确认好友选择
   */
  const confirmFriendSelection = (friends) => {
    const selectedFriendNames = friends
      .filter((friend) => selectedGroupMembers.value.includes(friend.id))
      .map((friend) => friend.name)

    groupForm.value.members = selectedFriendNames.join(', ')
    friendSelectionDialogVisible.value = false
  }

  /**
   * 取消创建群聊
   */
  const cancelCreateGroup = () => {
    groupDialogVisible.value = false
    groupForm.value = {
      name: '',
      members: ''
    }
    selectedGroupMembers.value = []
    friendSelectionDialogVisible.value = false
  }

  /**
   * 创建群聊
   */
  const handleCreateGroup = async () => {
    if (!groupForm.value.name.trim()) {
      ElMessage.error('请输入群名称')
      return
    }

    try {
      // 调用后端API创建群聊
      const response = await api.post('/v1/groups', {
        groupName: groupForm.value.name.trim(),
        creatorId: userId.value,
        memberIds: selectedGroupMembers.value
      })

      if (response.code === '200') {
        const newGroup = {
          id: response.data.id,
          type: 'group',
          name: groupForm.value.name.trim(),
          avatar: '👥',
          lastMessage: '暂无消息',
          time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
          unreadCount: 0,
          memberCount: selectedGroupMembers.value.length + 1,
          pinned: false
        }

        // 添加到会话列表
        conversations.value.push(newGroup)

        // 添加系统消息
        const systemMsg = {
          id: chatMessages.value.length + 1,
          sender: '系统',
          content: `群聊 "${newGroup.name}" 已创建`,
          time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        }

        chatMessages.value.push(systemMsg)
        newGroup.lastMessage = systemMsg.content

        ElMessage.success('群聊已创建')

        // 关闭对话框并重置表单
        groupDialogVisible.value = false
        groupForm.value = {
          name: '',
          members: ''
        }
        selectedGroupMembers.value = []

        return newGroup
      } else {
        ElMessage.error('创建群聊失败: ' + response.message)
        return null
      }
    } catch (error) {
      console.error('创建群聊失败:', error)
      ElMessage.error('创建群聊失败，请稍后重试')
      return null
    }
  }

  /**
   * 打开群详情
   */
  const openGroupDetail = async (conversation) => {
    if (!conversation || conversation.type !== 'group') {
      ElMessage.warning('请选择一个群聊')
      return
    }

    try {
      const groupId = conversation.groupId || conversation.id
      const response = await api.get(`/v1/groups/${groupId}/members`)

      if (response.code !== '200' || !Array.isArray(response.data)) {
        ElMessage.error('获取群成员失败')
        return
      }

      currentGroupInfo.value = {
        id: groupId,
        name: conversation.name,
        avatar: conversation.avatar,
        memberCount: response.data.length,
        members: response.data.map((member) => member.userId),
        memberDetails: response.data.map((member) => ({
          id: member.userId,
          name: member.userId,
          role: member.role || 'member'
        })),
        creator: response.data.find((member) => member.role === 'owner')?.userId || '未知',
        creatorId: response.data.find((member) => member.role === 'owner')?.userId || '',
        createdAt: ''
      }

      groupDetailDialogVisible.value = true
    } catch (error) {
      console.error('获取群详情失败:', error)
      ElMessage.error('获取群详情失败，请稍后重试')
    }
  }

  /**
   * 关闭群详情
   */
  const closeGroupDetail = () => {
    groupDetailDialogVisible.value = false
    currentGroupInfo.value = null
  }

  return {
    // 状态
    groups,
    groupDialogVisible,
    groupForm,
    friendSelectionDialogVisible,
    selectedGroupMembers,
    groupDetailDialogVisible,
    currentGroupInfo,

    // 方法
    fetchGroups,
    openCreateGroupDialog,
    showFriendSelectionDialog,
    toggleFriendSelection,
    confirmFriendSelection,
    cancelCreateGroup,
    handleCreateGroup,
    openGroupDetail,
    closeGroupDetail
  }
}
