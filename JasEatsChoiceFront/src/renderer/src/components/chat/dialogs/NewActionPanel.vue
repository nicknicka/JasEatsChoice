<template>
  <el-dialog
    v-model="visible"
    title="新建对话"
    width="700px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <!-- 功能选项卡 -->
    <div class="action-tabs">
      <div
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-item"
        :class="{ active: activeTab === tab.key }"
        @click="switchTab(tab.key)"
      >
        <div class="tab-icon">{{ tab.icon }}</div>
        <div class="tab-label">{{ tab.label }}</div>
      </div>
    </div>

    <!-- 快速搜索区域 -->
    <div class="quick-search">
      <el-input
        v-model="searchQuery"
        placeholder="输入手机号/用户ID/昵称快速搜索"
        :prefix-icon="Search"
        clearable
        @input="handleQuickSearch"
      />
    </div>

    <!-- 内容区域 -->
    <div class="content-area">
      <!-- 新建单聊 -->
      <transition name="tab-fade" mode="out-in">
        <div v-if="activeTab === 'chat'" key="chat" class="tab-content">
        <div v-if="!searchQuery" class="recommend-section">
          <div class="section-title">
            <span>推荐好友</span>
            <el-button text type="primary" size="small" @click="refreshRecommendations">
              <el-icon><Refresh /></el-icon> 换一批
            </el-button>
          </div>
          <div class="user-grid">
            <div
              v-for="user in recommendedFriends"
              :key="user.id"
              class="user-card"
              @click="startChat(user)"
            >
              <div class="user-avatar">
                <img v-if="isImageAvatar(user.avatar)" :src="user.avatar" alt="" />
                <span v-else>{{ user.avatar || '👤' }}</span>
              </div>
              <div class="user-name">{{ user.name }}</div>
              <div class="user-reason">{{ user.reason }}</div>
            </div>
          </div>
        </div>

        <div v-else class="search-results">
          <div v-if="searchResults.length === 0" class="empty-result">
            <el-empty description="未找到相关用户" />
          </div>
          <div v-else class="user-list">
            <div
              v-for="user in searchResults"
              :key="user.id"
              class="user-item"
              @click="startChat(user)"
            >
              <div class="user-avatar">
                <img v-if="isImageAvatar(user.avatar)" :src="user.avatar" alt="" />
                <span v-else>{{ user.avatar || '👤' }}</span>
              </div>
              <div class="user-info">
                <div class="user-name">{{ user.name }}</div>
                <div class="user-detail">{{ user.detail || '用户ID: ' + user.id }}</div>
              </div>
              <el-button type="primary" size="small" circle>
                <el-icon><ChatDotRound /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
      </transition>

      <!-- 新建群聊 -->
      <transition name="tab-fade" mode="out-in">
        <div v-if="activeTab === 'group'" key="group" class="tab-content">
        <div class="group-form">
          <el-form :model="groupForm" label-width="80px">
            <el-form-item label="群名称">
              <el-input
                v-model="groupForm.name"
                placeholder="请输入群名称"
                maxlength="50"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </div>

        <div class="member-selection">
          <div class="section-header">
            <span class="section-title">已选成员 ({{ selectedMembers.length }}/50)</span>
            <div class="section-actions">
              <el-button text size="small" @click="clearAllMembers">清空</el-button>
            </div>
          </div>

          <!-- 已选成员标签 -->
          <div v-if="selectedMembers.length > 0" class="selected-members">
            <el-tag
              v-for="member in selectedMembers"
              :key="member.id"
              closable
              class="member-tag"
              @close="removeMember(member)"
            >
              <img v-if="isImageAvatar(member.avatar)" :src="member.avatar" class="member-tag-avatar" />
              <span v-else class="member-tag-emoji">{{ member.avatar || '👤' }}</span>
              {{ member.name }}
            </el-tag>
          </div>

          <!-- 成员选择区域 -->
          <div class="member-select-area">
            <el-input
              v-model="memberSearchQuery"
              placeholder="搜索好友添加到群聊"
              :prefix-icon="Search"
              clearable
              @input="handleMemberSearch"
            />

            <div class="friend-list">
              <div v-for="friend in filteredFriends" :key="friend.id" class="friend-item">
                <div class="friend-avatar">
                  <img v-if="isImageAvatar(friend.avatar)" :src="friend.avatar" alt="" />
                  <span v-else>{{ friend.avatar || '👤' }}</span>
                </div>
                <div class="friend-info">
                  <div class="friend-name">{{ friend.name }}</div>
                </div>
                <el-button
                  :type="isMemberSelected(friend) ? 'danger' : 'primary'"
                  size="small"
                  @click="toggleMember(friend)"
                >
                  {{ isMemberSelected(friend) ? '移除' : '添加' }}
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      </transition>

      <!-- 加好友 -->
      <transition name="tab-fade" mode="out-in">
        <div v-if="activeTab === 'friend'" key="friend" class="tab-content">
        <div class="add-friend-content">
          <!-- 搜索类型选择 -->
          <div class="search-type-selector">
            <el-radio-group v-model="friendSearchType" size="small">
              <el-radio-button label="nickname">用户名/昵称</el-radio-button>
              <el-radio-button label="phone">手机号</el-radio-button>
              <el-radio-button label="email">邮箱</el-radio-button>
            </el-radio-group>
          </div>

          <!-- 搜索结果 -->
          <div v-if="friendSearchResults.length === 0 && !friendSearched" class="empty-state">
            <el-empty description="输入关键词搜索用户">
              <template #image>
                <div class="empty-icon">🔍</div>
              </template>
            </el-empty>
          </div>

          <div v-else-if="friendSearchResults.length === 0 && friendSearched" class="empty-state">
            <el-empty description="未找到相关用户" />
          </div>

          <div v-else class="search-result-list">
            <div v-for="user in friendSearchResults" :key="user.id" class="result-user-item">
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
                size="small"
                :loading="user.adding"
                :disabled="user.added"
                @click="addFriend(user)"
              >
                {{ user.added ? '已发送' : '加好友' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
      </transition>
    </div>

    <!-- 底部操作栏 -->
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          v-if="activeTab === 'group'"
          type="primary"
          :disabled="!canCreateGroup"
          @click="createGroup"
        >
          创建群聊 ({{ selectedMembers.length + 1 }}人)
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, ChatDotRound } from '@element-plus/icons-vue'
import api from '../../../utils/api'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  friends: {
    type: Array,
    default: () => []
  },
  conversations: {
    type: Array,
    default: () => []
  },
  userId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits([
  'update:modelValue',
  'startChat',
  'createGroup',
  'addFriend',
  'refreshFriends'
])

// ========== 基础状态 ==========
const visible = ref(props.modelValue)
const activeTab = ref('chat')
const searchQuery = ref('')

// ========== Tab 配置 ==========
const tabs = [
  { key: 'chat', label: '单聊', icon: '💬' },
  { key: 'group', label: '群聊', icon: '👥' },
  { key: 'friend', label: '加好友', icon: '👤' }
]

// ========== 监听 modelValue ==========
watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val) {
      resetState()
    }
  }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
})

// ========== Tab 切换 ==========
const switchTab = (tab) => {
  if (activeTab.value === tab) return

  // 添加切换动画效果
  activeTab.value = tab
  searchQuery.value = ''
  resetSearchStates()

  // 触发震动反馈（如果支持）
  if (navigator.vibrate) {
    navigator.vibrate(10)
  }
}

// ========== 头像显示辅助函数 ==========
/**
 * 判断头像是否为图片（URL或base64）
 */
const isImageAvatar = (avatar) => {
  if (!avatar) return false
  return avatar.match(/^https?:/) || avatar.match(/^data:image/)
}

// ========== 快速搜索 ==========
const searchResults = ref([])

const handleQuickSearch = () => {
  if (!searchQuery.value.trim()) {
    searchResults.value = []
    return
  }

  // 实时搜索逻辑
  performSearch(searchQuery.value.trim())
}

const performSearch = async (keyword) => {
  try {
    const response = await api.get('/v1/users/search', {
      params: { keyword }
    })

    if (response.code === '200') {
      searchResults.value = (response.data || []).map((user) => ({
        id: user.userId,
        name: user.nickname || user.username,
        avatar: user.avatar,
        detail: `用户ID: ${user.userId}`
      }))
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败，请稍后重试')
  }
}

// ========== 推荐好友 ==========
const recommendedFriends = ref([])

const generateRecommendations = () => {
  // 基于好友列表生成推荐
  const recommendations = []

  // 最近聊天的好友（从会话列表中提取）
  const recentChats = props.conversations
    .filter((conv) => conv.type === 'friend' || conv.type === 'private')
    .slice(0, 3)
    .map((conv) => ({
      id: conv.id,
      name: conv.name,
      avatar: conv.avatar,
      reason: '最近联系'
    }))

  recommendations.push(...recentChats)

  // 随机选择一些好友作为推荐
  const randomFriends = props.friends
    .filter((f) => !recommendations.find((r) => r.id === f.id))
    .sort(() => Math.random() - 0.5)
    .slice(0, 3)
    .map((f) => ({
      ...f,
      reason: '推荐好友'
    }))

  recommendations.push(...randomFriends)

  recommendedFriends.value = recommendations.slice(0, 6)
}

const refreshRecommendations = () => {
  generateRecommendations()
}

// ========== 新建单聊 ==========
const startChat = (user) => {
  emit('startChat', user)
  handleClose()
}

// ========== 新建群聊 ==========
const groupForm = ref({
  name: ''
})

const selectedMembers = ref([])
const memberSearchQuery = ref('')

const filteredFriends = computed(() => {
  if (!memberSearchQuery.value.trim()) {
    return props.friends
  }
  return props.friends.filter((friend) =>
    friend.name.toLowerCase().includes(memberSearchQuery.value.toLowerCase())
  )
})

const isMemberSelected = (friend) => {
  return selectedMembers.value.some((m) => m.id === friend.id)
}

const toggleMember = (friend) => {
  const index = selectedMembers.value.findIndex((m) => m.id === friend.id)

  if (index === -1) {
    if (selectedMembers.value.length >= 49) {
      ElMessage.warning('群成员最多50人')
      return
    }
    selectedMembers.value.push(friend)
  } else {
    selectedMembers.value.splice(index, 1)
  }
}

const removeMember = (member) => {
  const index = selectedMembers.value.findIndex((m) => m.id === member.id)
  if (index !== -1) {
    selectedMembers.value.splice(index, 1)
  }
}

const clearAllMembers = () => {
  selectedMembers.value = []
}

const handleMemberSearch = () => {
  // 搜索逻辑已在 computed 中处理
}

const canCreateGroup = computed(() => {
  return groupForm.value.name.trim() && selectedMembers.value.length >= 1
})

const createGroup = () => {
  if (!canCreateGroup.value) {
    ElMessage.warning('请填写群名称并选择至少1个成员')
    return
  }

  emit('createGroup', {
    name: groupForm.value.name.trim(),
    members: selectedMembers.value
  })

  handleClose()
}

// ========== 加好友 ==========
const friendSearchType = ref('nickname')
const friendSearchResults = ref([])
const friendSearched = ref(false)

watch(searchQuery, async (newVal) => {
  if (activeTab.value === 'friend' && newVal.trim()) {
    await performFriendSearch()
  } else {
    friendSearchResults.value = []
    friendSearched.value = false
  }
})

const performFriendSearch = async () => {
  if (!searchQuery.value.trim()) {
    return
  }

  try {
    const response = await api.get('/v1/users/search', {
      params: {
        keyword: searchQuery.value.trim(),
        searchType: friendSearchType.value
      }
    })

    if (response.code === '200') {
      friendSearchResults.value = (response.data || []).map((user) => ({
        id: user.userId,
        name: user.nickname || user.username,
        avatar: user.avatar,
        phone: user.phone,
        email: user.email,
        adding: false,
        added: false
      }))
      friendSearched.value = true
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败，请稍后重试')
  }
}

const addFriend = async (user) => {
  if (user.added) return

  user.adding = true

  try {
    const response = await api.post('/v1/contacts/friends/request', {
      userId: props.userId.toString(),
      targetId: user.id.toString(),
      relationType: 'friend',
      status: 'pending',
      message: '你好，我想加你为好友'
    })

    if (response.code === '200') {
      user.added = true
      ElMessage.success('好友申请已发送')

      // 触发刷新好友列表
      emit('refreshFriends')
    }
  } catch (error) {
    console.error('添加好友失败:', error)
    ElMessage.error('添加失败，请稍后重试')
  } finally {
    user.adding = false
  }
}

// ========== 重置状态 ==========
const resetState = () => {
  searchQuery.value = ''
  activeTab.value = 'chat'
  searchResults.value = []
  groupForm.value.name = ''
  selectedMembers.value = []
  memberSearchQuery.value = ''
  friendSearchResults.value = []
  friendSearched.value = false
  generateRecommendations()
}

const resetSearchStates = () => {
  searchResults.value = []
  friendSearchResults.value = []
  friendSearched.value = false
}

const handleClose = () => {
  visible.value = false
  resetState()
}

// ========== 初始化 ==========
generateRecommendations()
</script>

<style scoped lang="less">
// 对话框样式优化
:deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

:deep(.el-dialog__header) {
  padding: 24px 24px 16px;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  margin: 0;

  .el-dialog__title {
    color: #fff;
    font-size: 20px;
    font-weight: 600;
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
  padding: 24px;
  background: linear-gradient(to bottom, #f8f9fe 0%, #ffffff 100%);
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  background-color: #f8f9fe;
  border-top: 1px solid #e8eaf0;
}

// 功能选项卡
.action-tabs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 16px;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.05);

  .tab-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 20px 16px;
    border-radius: 14px;
    cursor: pointer;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    background-color: rgba(255, 255, 255, 0.6);
    border: 2px solid transparent;
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(29, 78, 216, 0.1) 100%);
      opacity: 0;
      transition: opacity 0.3s ease;
    }

    &:hover {
      background-color: #fff;
      border-color: #3b82f6;
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(59, 130, 246, 0.25);

      &::before {
        opacity: 1;
      }

      .tab-icon {
        transform: scale(1.1);
      }
    }

    &.active {
      background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
      border-color: #3b82f6;
      box-shadow: 0 8px 24px rgba(59, 130, 246, 0.4);
      transform: translateY(-2px);

      .tab-icon {
        transform: scale(1.05);
      }

      .tab-label {
        color: #fff;
        font-weight: 600;
      }
    }

    .tab-icon {
      font-size: 42px;
      margin-bottom: 10px;
      transition: transform 0.3s ease;
      position: relative;
      z-index: 1;
    }

    .tab-label {
      font-size: 15px;
      font-weight: 500;
      color: #4a5568;
      transition: all 0.3s ease;
      position: relative;
      z-index: 1;
    }
  }
}

// 快速搜索
.quick-search {
  margin-bottom: 24px;

  :deep(.el-input) {
    .el-input__wrapper {
      border-radius: 12px;
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

      &::placeholder {
        color: #a0aec0;
      }
    }

    .el-input__append {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      color: #fff;
      border-radius: 0 10px 10px 0;

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

// 内容区域
.content-area {
  min-height: 420px;
  max-height: 520px;
  overflow-y: auto;
  padding-right: 8px;

  &::-webkit-scrollbar {
    width: 8px;
  }

  &::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
    border-radius: 4px;

    &:hover {
      background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%);
    }
  }

  .tab-content {
    .section-title {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      font-size: 17px;
      font-weight: 600;
      color: #2d3748;
      padding: 0 4px;
    }

    // 用户网格
    .user-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
      gap: 20px;

      .user-card {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 20px;
        border-radius: 16px;
        background: linear-gradient(135deg, #ffffff 0%, #f7fafc 100%);
        cursor: pointer;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        border: 2px solid #e2e8f0;
        position: relative;
        overflow: hidden;

        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          height: 4px;
          background: linear-gradient(90deg, #3b82f6 0%, #1d4ed8 100%);
          transform: scaleX(0);
          transition: transform 0.3s ease;
        }

        &:hover {
          background: #fff;
          border-color: #3b82f6;
          transform: translateY(-6px);
          box-shadow: 0 12px 24px rgba(59, 130, 246, 0.2);

          &::before {
            transform: scaleX(1);
          }

          .user-avatar {
            transform: scale(1.08);
          }
        }

        .user-avatar {
          width: 70px;
          height: 70px;
          font-size: 38px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
          border-radius: 50%;
          margin-bottom: 14px;
          transition: transform 0.3s ease;
          box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
          overflow: hidden;

          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }

        .user-name {
          font-size: 15px;
          font-weight: 600;
          color: #2d3748;
          margin-bottom: 6px;
          text-align: center;
        }

        .user-reason {
          font-size: 12px;
          color: #718096;
          background-color: #edf2f7;
          padding: 4px 10px;
          border-radius: 12px;
        }
      }
    }

    // 搜索结果列表
    .search-results {
      .user-list {
        .user-item {
          display: flex;
          align-items: center;
          padding: 16px;
          border-radius: 12px;
          margin-bottom: 12px;
          background: linear-gradient(135deg, #ffffff 0%, #f7fafc 100%);
          cursor: pointer;
          transition: all 0.3s ease;
          border: 2px solid #e2e8f0;

          &:hover {
            background: #fff;
            border-color: #3b82f6;
            transform: translateX(6px);
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
          }

          .user-avatar {
            width: 56px;
            height: 56px;
            font-size: 30px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
            border-radius: 50%;
            margin-right: 16px;
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
            overflow: hidden;

            img {
              width: 100%;
              height: 100%;
              object-fit: cover;
            }
          }

          .user-info {
            flex: 1;

            .user-name {
              font-size: 15px;
              font-weight: 600;
              color: #2d3748;
              margin-bottom: 6px;
            }

            .user-detail {
              font-size: 13px;
              color: #718096;
            }
          }
        }
      }

      .empty-result {
        margin-top: 80px;
      }
    }
  }

  // 群聊成员选择
  .member-selection {
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      padding: 0 4px;

      .section-title {
        font-size: 15px;
        font-weight: 600;
        color: #2d3748;
      }
    }

    .selected-members {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      margin-bottom: 20px;
      padding: 16px;
      background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
      border-radius: 12px;
      min-height: 56px;
      border: 2px dashed #cbd5e0;

      .member-tag {
        font-size: 14px;
        padding: 8px 14px;
        background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
        border: none;
        color: #fff;
        font-weight: 500;
        border-radius: 8px;
        display: flex;
        align-items: center;
        gap: 6px;

        :deep(.el-tag__close) {
          color: #fff;

          &:hover {
            background-color: rgba(255, 255, 255, 0.2);
          }
        }

        .member-tag-avatar {
          width: 20px;
          height: 20px;
          border-radius: 50%;
          object-fit: cover;
        }

        .member-tag-emoji {
          font-size: 16px;
        }
      }
    }

    .member-select-area {
      :deep(.el-input) {
        margin-bottom: 16px;

        .el-input__wrapper {
          border-radius: 10px;
        }
      }

      .friend-list {
        max-height: 320px;
        overflow-y: auto;
        padding-right: 8px;

        &::-webkit-scrollbar {
          width: 6px;
        }

        &::-webkit-scrollbar-thumb {
          background: #cbd5e0;
          border-radius: 3px;
        }

        .friend-item {
          display: flex;
          align-items: center;
          padding: 14px;
          border-radius: 10px;
          margin-bottom: 10px;
          background: linear-gradient(135deg, #ffffff 0%, #f7fafc 100%);
          transition: all 0.3s ease;
          border: 2px solid #e2e8f0;

          &:hover {
            background: #fff;
            border-color: #3b82f6;
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
          }

          .friend-avatar {
            width: 48px;
            height: 48px;
            font-size: 26px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
            border-radius: 50%;
            margin-right: 14px;
            box-shadow: 0 3px 10px rgba(59, 130, 246, 0.3);
            overflow: hidden;

            img {
              width: 100%;
              height: 100%;
              object-fit: cover;
            }
          }

          .friend-info {
            flex: 1;

            .friend-name {
              font-size: 15px;
              font-weight: 600;
              color: #2d3748;
            }
          }
        }
      }
    }
  }

  // 加好友
  .add-friend-content {
    .search-type-selector {
      margin-bottom: 20px;

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
              background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
              border-color: #3b82f6;
              color: #fff;
              box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
            }
          }
        }
      }
    }

    .empty-state {
      margin-top: 80px;
      text-align: center;

      .empty-icon {
        font-size: 80px;
        margin-bottom: 20px;
        opacity: 0.8;
      }
    }

    .search-result-list {
      .result-user-item {
        display: flex;
        align-items: center;
        padding: 18px;
        border-radius: 12px;
        margin-bottom: 14px;
        background: linear-gradient(135deg, #ffffff 0%, #f7fafc 100%);
        transition: all 0.3s ease;
        border: 2px solid #e2e8f0;

        &:hover {
          background: #fff;
          border-color: #3b82f6;
          box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
        }

        .user-avatar {
          width: 56px;
          height: 56px;
          font-size: 30px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
          border-radius: 50%;
          margin-right: 16px;
          box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
          overflow: hidden;

          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }

        .user-info {
          flex: 1;

          .user-name {
            font-size: 15px;
            font-weight: 600;
            color: #2d3748;
            margin-bottom: 6px;
          }

          .user-detail {
            font-size: 13px;
            color: #718096;
          }
        }

        :deep(.el-button) {
          border-radius: 8px;
          padding: 10px 20px;
          font-weight: 500;
          background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
          border: none;

          &:hover {
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
}

// 底部按钮
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;

  :deep(.el-button) {
    border-radius: 8px;
    padding: 10px 24px;
    font-weight: 500;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
    }

    &.el-button--primary {
      background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
      border: none;

      &:hover {
        background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%);
        box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
      }

      &:disabled {
        background: #cbd5e0;
        box-shadow: none;
      }
    }
  }
}

.group-form {
  margin-bottom: 24px;
  padding: 20px;
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  border-radius: 12px;

  :deep(.el-form-item) {
    margin-bottom: 0;

    .el-input__wrapper {
      border-radius: 10px;
    }
  }
}

// Tab 切换过渡动画
.tab-fade-enter-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.tab-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.tab-fade-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

.tab-fade-leave-to {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}

.tab-fade-enter-to {
  opacity: 1;
  transform: translateY(0) scale(1);
}

.tab-fade-leave-from {
  opacity: 1;
  transform: translateY(0) scale(1);
}

// 推荐卡片动画
.tab-content {
  .user-grid {
    .user-card {
      animation: cardFadeIn 0.5s ease backwards;
    }
  }
}

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 列表项动画
.tab-content {
  .user-list,
  .friend-list,
  .search-result-list {
    .user-item,
    .friend-item,
    .result-user-item {
      animation: slideInFromLeft 0.4s ease backwards;
    }
  }
}

@keyframes slideInFromLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

// 加好友搜索结果动画
.add-friend-content {
  .search-result-list {
    .result-user-item {
      animation: fadeInUp 0.5s ease backwards;
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

// Tab 切换时内容区域的脉冲效果
.content-area {
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
  }
}
</style>
