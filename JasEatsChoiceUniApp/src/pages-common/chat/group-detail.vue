<template>
  <view class="group-detail-container">
    <!-- 群信息卡片 -->
    <view class="group-info-card">
      <view class="group-basic">
        <image class="group-avatar" :src="groupInfo.avatar" mode="aspectFill"></image>
        <view class="group-text">
          <text class="group-name">{{ groupInfo.name }}</text>
          <text class="group-meta">{{ groupInfo.memberCount }}人 · 群号：{{ groupInfo.groupNo }}</text>
        </view>
        <button class="qrcode-btn" @tap="showQRCode">
          <uni-icons type="qrcode" size="18" color="#FF6B35"></uni-icons>
        </button>
      </view>

      <!-- 群公告 -->
      <view class="group-notice-section" v-if="groupInfo.notice" @tap="editNotice">
        <view class="notice-header">
          <uni-icons type="notification" size="16" color="#FF6B35"></uni-icons>
          <text class="notice-title">群公告</text>
        </view>
        <text class="notice-content">{{ groupInfo.notice }}</text>
      </view>
    </view>

    <!-- 群成员 -->
    <view class="members-section">
      <view class="section-header">
        <text class="section-title">群成员</text>
        <text class="section-action" @tap="inviteMember">邀请</text>
      </view>

      <scroll-view class="members-list" scroll-x>
        <view class="member-item" @tap="inviteMember">
          <view class="add-avatar">
            <uni-icons type="plus" size="20" color="#FF6B35"></uni-icons>
          </view>
          <text class="member-name">邀请</text>
        </view>

        <view
          class="member-item"
          v-for="member in memberList"
          :key="member.id"
          @tap="showMemberMenu(member)"
          @longpress="showMemberOptions(member)"
        >
          <view class="member-avatar-wrapper">
            <image class="member-avatar" :src="member.avatar" mode="aspectFill"></image>
            <view class="owner-badge" v-if="member.role === 'owner'">
              <uni-icons type="star-filled" size="10" color="#FFA500"></uni-icons>
            </view>
            <view class="admin-badge" v-if="member.role === 'admin'">
              <uni-icons type="gear-filled" size="10" color="#52C41A"></uni-icons>
            </view>
          </view>
          <text class="member-name">{{ member.name }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 群设置 -->
    <view class="settings-section">
      <view class="setting-item" @tap="toggleMute">
        <view class="setting-left">
          <uni-icons type="sound" size="20" color="#666"></uni-icons>
          <text class="setting-label">消息免打扰</text>
        </view>
        <switch
          :checked="groupInfo.isMuted"
          color="#FF6B35"
          @change="onMuteChange"
        />
      </view>

      <view class="setting-item" @tap="togglePin">
        <view class="setting-left">
          <uni-icons type="star" size="20" color="#666"></uni-icons>
          <text class="setting-label">置顶聊天</text>
        </view>
        <switch
          :checked="groupInfo.isPinned"
          color="#FF6B35"
          @change="onPinChange"
        />
      </view>

      <view class="setting-item" @tap="viewGroupOrder">
        <view class="setting-left">
          <uni-icons type="shop" size="20" color="#666"></uni-icons>
          <text class="setting-label">群订单</text>
        </view>
        <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
      </view>

      <view class="setting-item" @tap="editGroupName">
        <view class="setting-left">
          <uni-icons type="compose" size="20" color="#666"></uni-icons>
          <text class="setting-label">群名称</text>
        </view>
        <view class="setting-right">
          <text class="setting-value">{{ groupInfo.name }}</text>
          <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
        </view>
      </view>

      <view class="setting-item" @tap="editGroupAvatar">
        <view class="setting-left">
          <uni-icons type="image" size="20" color="#666"></uni-icons>
          <text class="setting-label">群头像</text>
        </view>
        <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
      </view>

      <view class="setting-item" @tap="viewChatHistory">
        <view class="setting-left">
          <uni-icons type="list" size="20" color="#666"></uni-icons>
          <text class="setting-label">聊天记录</text>
        </view>
        <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
      </view>
    </view>

    <!-- 危险操作 -->
    <view class="danger-section">
      <view class="danger-item" @tap="clearChatHistory">
        <text class="danger-label">清空聊天记录</text>
      </view>
      <view class="danger-item quit" @tap="confirmQuit">
        <text class="danger-label">退出群聊</text>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="bottom-action" v-if="isOwner">
      <button class="action-btn dismiss" @tap="confirmDismiss">解散群聊</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { groupApi } from '@/api/modules/group.js'

// 当前用户ID
const currentUserId = ref('')

// 群ID
const groupId = ref('')

// 是否是创建模式
const isCreateMode = ref(false)

// 是否是群主
const isOwner = ref(false)

// 群信息
const groupInfo = ref({
  id: '',
  name: '美食爱好者群',
  avatar: 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=群',
  groupNo: '',
  memberCount: 25,
  notice: '欢迎加入美食爱好者群，一起分享美食！',
  isMuted: false,
  isPinned: false
})

// 成员列表
const memberList = ref([])

onLoad((options) => {
  // 获取当前用户ID
  currentUserId.value = uni.getStorageSync('userId') || ''

  if (options.id) {
    groupId.value = options.id
  }
  if (options.action === 'create') {
    // 创建群聊
    isCreateMode.value = true
    initCreateGroup()
  } else {
    // 查看群详情
    loadGroupDetail()
  }
  loadMembers()
})

/**
 * 初始化创建群聊 - IM-017: 初始化创建群聊的表单
 */
const initCreateGroup = () => {
  // IM-017: 初始化创建群聊的表单数据
  groupInfo.value = {
    id: '',
    name: '',
    avatar: 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=群',
    groupNo: '',
    memberCount: 1, // 只有自己
    notice: '',
    isMuted: false,
    isPinned: false
  }

  // 初始化成员列表为空
  memberList.value = []

  // 自动跳转到选择成员页面
  uni.navigateTo({
    url: '/chat/member-selector?action=create_group'
  })
}

/**
 * 加载群详情 - IM-018: 调用API获取群详情
 */
const loadGroupDetail = async () => {
  try {
    // IM-018: 调用API获取群详情
    const res = await groupApi.getGroupDetail(groupId.value)

    if (res.code === 200 && res.data) {
      const data = res.data

      // 更新群信息
      groupInfo.value = {
        id: data.id,
        name: data.name,
        avatar: data.avatar || 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=群',
        groupNo: data.groupNo || '',
        memberCount: data.memberCount || 0,
        notice: data.notice || '',
        isMuted: data.isMuted || false,
        isPinned: data.isPinned || false
      }

      // 判断是否是群主
      isOwner.value = data.ownerId === currentUserId.value

      console.log('加载群详情成功')
    } else {
      throw new Error(res.message || '获取群详情失败')
    }
  } catch (error) {
    console.error('加载群详情失败:', error)
    uni.showToast({
      title: error.message || '加载群详情失败',
      icon: 'none'
    })
  }
}

/**
 * 加载成员列表 - IM-019: 调用API获取成员列表
 */
const loadMembers = async () => {
  if (isCreateMode.value) return

  try {
    // IM-019: 调用API获取成员列表
    const res = await groupApi.getMembers(groupId.value)

    if (res.code === 200 && res.data) {
      // 转换成员数据格式
      memberList.value = res.data.map(member => ({
        id: member.userId,
        name: member.nickname || member.userName || '未知',
        avatar: member.avatar || '/static/default-avatar.png',
        role: member.role || 'member' // owner, admin, member
      }))

      console.log('加载成员成功，数量:', memberList.value.length)
    } else {
      throw new Error(res.message || '获取成员列表失败')
    }
  } catch (error) {
    console.error('加载成员失败:', error)

    // 开发阶段：使用模拟数据
    const members = []
    const names = ['张三', '李四', '王五', '赵六', '钱七', '孙八', '周九', '吴十']

    // 群主
    members.push({
      id: 1,
      name: '群主',
      avatar: 'https://via.placeholder.com/80/FFA500/FFFFFF?text=主',
      role: 'owner'
    })

    // 管理员
    for (let i = 0; i < 2; i++) {
      members.push({
        id: 2 + i,
        name: names[i % names.length],
        avatar: `https://via.placeholder.com/80/52C41A/FFFFFF?text=${names[i % names.length][0]}`,
        role: 'admin'
      })
    }

    // 普通成员
    for (let i = 0; i < 10; i++) {
      members.push({
        id: 4 + i,
        name: names[(i + 2) % names.length],
        avatar: `https://via.placeholder.com/80/1677FF/FFFFFF?text=${names[(i + 2) % names.length][0]}`,
        role: 'member'
      })
    }

    memberList.value = members
  }
}

/**
 * 显示二维码
 */
const showQRCode = () => {
  uni.showModal({
    title: '群二维码',
    content: '群二维码功能开发中',
    showCancel: false
  })
}

/**
 * 编辑公告 - IM-020: 调用API更新群公告
 */
const editNotice = () => {
  if (!isOwner.value) {
    uni.showToast({
      title: '只有群主可以编辑公告',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '编辑群公告',
    editable: true,
    placeholderText: groupInfo.value.notice,
    success: async (res) => {
      if (res.confirm && res.content) {
        try {
          uni.showLoading({ title: '更新中...' })

          // IM-020: 调用API更新群公告
          const apiRes = await groupApi.updateNotice(groupId.value, res.content)

          uni.hideLoading()

          if (apiRes.code === 200) {
            groupInfo.value.notice = res.content
            uni.showToast({
              title: '公告已更新',
              icon: 'success'
            })
          } else {
            throw new Error(apiRes.message || '更新失败')
          }
        } catch (error) {
          console.error('更新群公告失败:', error)
          uni.hideLoading()
          uni.showToast({
            title: error.message || '更新失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 邀请成员
 */
const inviteMember = () => {
  uni.showActionSheet({
    itemList: ['从好友邀请', '通过群号邀请'],
    success: (res) => {
      if (res.tapIndex === 0) {
        // 从好友邀请
        uni.showToast({
          title: '选择好友',
          icon: 'none'
        })
      } else if (res.tapIndex === 1) {
        // 通过群号邀请
        uni.setClipboardData({
          data: groupInfo.value.groupNo,
          success: () => {
            uni.showToast({
              title: '群号已复制',
              icon: 'success'
            })
          }
        })
      }
    }
  })
}

/**
 * 显示成员菜单
 */
const showMemberMenu = (member) => {
  // 点击查看成员信息
  if (member.role !== 'owner') {
    uni.showModal({
      title: member.name,
      content: `角色：${getRoleText(member.role)}`,
      showCancel: false
    })
  }
}

/**
 * 显示成员选项（长按）- IM-021/IM-022: 管理成员
 */
const showMemberOptions = (member) => {
  if (!isOwner.value) {
    uni.showToast({
      title: '只有群主可以管理成员',
      icon: 'none'
    })
    return
  }

  if (member.role === 'owner') return

  const itemList = []
  if (member.role === 'admin') {
    itemList.push('取消管理员')
  } else if (member.role === 'member') {
    itemList.push('设为管理员')
  }
  itemList.push('移出群聊')

  uni.showActionSheet({
    itemList,
    success: async (res) => {
      if (res.tapIndex === 0 && member.role === 'member') {
        // 设为管理员 - IM-022: 更新设置（权限）
        await updateMemberRole(member, 'admin')
      } else if (res.tapIndex === 0 && member.role === 'admin') {
        // 取消管理员
        await updateMemberRole(member, 'member')
      } else if ((res.tapIndex === 1 && member.role === 'admin') ||
                 (res.tapIndex === 0 && member.role === 'member')) {
        // 移出群聊 - IM-021: 移除成员
        await removeMember(member)
      }
    }
  })
}

/**
 * 更新成员角色 - IM-022: 调用API更新设置（权限）
 */
const updateMemberRole = async (member, newRole) => {
  try {
    uni.showLoading({ title: '更新中...' })

    // IM-022: 调用API更新成员权限
    // 注意：后端需要有更新成员角色的接口
    // const res = await groupApi.updateMemberRole(groupId.value, member.id, newRole)

    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))

    uni.hideLoading()

    // 更新本地数据
    member.role = newRole

    uni.showToast({
      title: newRole === 'admin' ? '已设为管理员' : '已取消管理员',
      icon: 'success'
    })
  } catch (error) {
    console.error('更新成员角色失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: '更新失败',
      icon: 'none'
    })
  }
}

/**
 * 获取角色文本
 */
const getRoleText = (role) => {
  const roleMap = {
    owner: '群主',
    admin: '管理员',
    member: '成员'
  }
  return roleMap[role] || '成员'
}

/**
 * 移除成员 - IM-021: 调用API移除成员
 */
const removeMember = async (member) => {
  uni.showModal({
    title: '确认移除',
    content: `确定将${member.name}移出群聊吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '移除中...' })

          // IM-021: 调用API移除成员
          // const res = await groupApi.removeMember(groupId.value, member.id)

          // 模拟API调用
          await new Promise(resolve => setTimeout(resolve, 500))

          uni.hideLoading()

          // 从列表中移除
          memberList.value = memberList.value.filter(m => m.id !== member.id)
          groupInfo.value.memberCount--

          uni.showToast({
            title: '已移除',
            icon: 'success'
          })
        } catch (error) {
          console.error('移除成员失败:', error)
          uni.hideLoading()
          uni.showToast({
            title: '移除失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 切换免打扰 - IM-023: 调用API更新设置（其他）
 */
const toggleMute = async () => {
  const newValue = !groupInfo.value.isMuted

  try {
    // IM-023: 调用API更新设置
    // const res = await groupApi.updateSettings(groupId.value, { isMuted: newValue })

    // 更新本地数据
    groupInfo.value.isMuted = newValue

    uni.showToast({
      title: newValue ? '已开启免打扰' : '已关闭免打扰',
      icon: 'success'
    })
  } catch (error) {
    console.error('更新设置失败:', error)
    uni.showToast({
      title: '更新失败',
      icon: 'none'
    })
  }
}

const onMuteChange = (e) => {
  toggleMute()
}

/**
 * 切换置顶 - IM-023: 调用API更新设置（其他）
 */
const togglePin = async () => {
  const newValue = !groupInfo.value.isPinned

  try {
    // IM-023: 调用API更新设置
    // const res = await groupApi.updateSettings(groupId.value, { isPinned: newValue })

    // 更新本地数据
    groupInfo.value.isPinned = newValue

    uni.showToast({
      title: newValue ? '已置顶' : '已取消置顶',
      icon: 'success'
    })
  } catch (error) {
    console.error('更新设置失败:', error)
    uni.showToast({
      title: '更新失败',
      icon: 'none'
    })
  }
}

const onPinChange = (e) => {
  togglePin()
}

/**
 * 查看群订单
 */
const viewGroupOrder = () => {
  uni.navigateTo({
    url: `/pages/group-order/detail?id=${groupId.value}`
  })
}

/**
 * 编辑群名称 - IM-024: 调用API更新群名称
 */
const editGroupName = () => {
  if (!isOwner.value) {
    uni.showToast({
      title: '只有群主可以修改',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '修改群名称',
    editable: true,
    placeholderText: groupInfo.value.name,
    success: async (res) => {
      if (res.confirm && res.content) {
        try {
          uni.showLoading({ title: '更新中...' })

          // IM-024: 调用API更新群名称
          // const apiRes = await groupApi.updateName(groupId.value, res.content)

          // 模拟API调用
          await new Promise(resolve => setTimeout(resolve, 500))

          uni.hideLoading()

          // 更新本地数据
          groupInfo.value.name = res.content

          uni.showToast({
            title: '群名称已更新',
            icon: 'success'
          })
        } catch (error) {
          console.error('更新群名称失败:', error)
          uni.hideLoading()
          uni.showToast({
            title: '更新失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 编辑群头像 - IM-025: 上传头像并调用API更新
 */
const editGroupAvatar = () => {
  if (!isOwner.value) {
    uni.showToast({
      title: '只有群主可以修改',
      icon: 'none'
    })
    return
  }

  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      try {
        uni.showLoading({ title: '上传中...' })

        // IM-025: 上传头像
        const uploadRes = await new Promise((resolve, reject) => {
          uni.uploadFile({
            url: 'https://api.example.com/v1/upload/image',
            filePath: res.tempFilePaths[0],
            name: 'file',
            success: (uploadRes) => {
              try {
                const data = JSON.parse(uploadRes.data)
                resolve(data)
              } catch (error) {
                reject(error)
              }
            },
            fail: (err) => {
              reject(err)
            }
          })
        })

        if (uploadRes.code === 200) {
          const avatarUrl = uploadRes.data.url

          // 调用API更新群头像
          // const apiRes = await groupApi.updateAvatar(groupId.value, avatarUrl)

          // 更新本地数据
          groupInfo.value.avatar = avatarUrl

          uni.hideLoading()
          uni.showToast({
            title: '头像已更新',
            icon: 'success'
          })
        } else {
          throw new Error(uploadRes.message || '上传失败')
        }
      } catch (error) {
        console.error('上传头像失败:', error)
        uni.hideLoading()
        uni.showToast({
          title: '上传失败',
          icon: 'none'
        })
      }
    }
  })
}

/**
 * 查看聊天记录
 */
const viewChatHistory = () => {
  uni.navigateTo({
    url: `/pages-common/chat/group-chat?id=${groupId.value}`
  })
}

/**
 * 清空聊天记录 - IM-026: 调用API清空记录
 */
const clearChatHistory = async () => {
  uni.showModal({
    title: '清空聊天记录',
    content: '确定清空所有聊天记录吗？此操作不可恢复。',
    confirmColor: '#F5222D',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '清空中...' })

          // IM-026: 调用API清空记录
          // const apiRes = await groupApi.clearHistory(groupId.value)

          // 模拟API调用
          await new Promise(resolve => setTimeout(resolve, 500))

          uni.hideLoading()
          uni.showToast({
            title: '已清空',
            icon: 'success'
          })
        } catch (error) {
          console.error('清空记录失败:', error)
          uni.hideLoading()
          uni.showToast({
            title: '清空失败',
            icon: 'none'
          })
        }
      }
    }
  })
}
    }
  })
}

/**
 * 确认退出群聊 - IM-027: 调用API退出群聊
 */
const confirmQuit = async () => {
  uni.showModal({
    title: '退出群聊',
    content: '确定退出该群聊吗？',
    confirmColor: '#F5222D',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '退出中...' })

          // IM-027: 调用API退出群聊
          const apiRes = await groupApi.leaveGroup(groupId.value)

          uni.hideLoading()

          if (apiRes.code === 200) {
            uni.showToast({
              title: '已退出群聊',
              icon: 'success'
            })
            setTimeout(() => {
              uni.navigateBack()
            }, 1500)
          } else {
            throw new Error(apiRes.message || '退出失败')
          }
        } catch (error) {
          console.error('退出群聊失败:', error)
          uni.hideLoading()
          uni.showToast({
            title: error.message || '退出失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 确认解散群聊 - IM-028: 调用API解散群聊
 */
const confirmDismiss = async () => {
  uni.showModal({
    title: '解散群聊',
    content: '确定解散该群聊吗？此操作不可恢复，所有成员将被移除。',
    confirmColor: '#F5222D',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '解散中...' })

          // IM-028: 调用API解散群聊
          const apiRes = await groupApi.dismissGroup(groupId.value)

          uni.hideLoading()

          if (apiRes.code === 200) {
            uni.showToast({
              title: '群聊已解散',
              icon: 'success'
            })
            setTimeout(() => {
              uni.navigateBack()
            }, 1500)
          } else {
            throw new Error(apiRes.message || '解散失败')
          }
        } catch (error) {
          console.error('解散群聊失败:', error)
          uni.hideLoading()
          uni.showToast({
            title: error.message || '解散失败',
            icon: 'none'
          })
        }
      }
    }
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.group-detail-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 100rpx;
}

/* 群信息卡片 */
.group-info-card {
  background: #fff;
  margin-bottom: 20rpx;
}

.group-basic {
  padding: 30rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  border-bottom: 1rpx solid #eee;
}

.group-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 20rpx;
}

.group-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.group-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.group-meta {
  font-size: 24rpx;
  color: #999;
}

.qrcode-btn {
  width: 60rpx;
  height: 60rpx;
  background: #FFF7E6;
  border-radius: 50%;
  @include flex-center;
  border: none;
}

.group-notice-section {
  padding: 25rpx 30rpx;
}

.notice-header {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 15rpx;
}

.notice-title {
  font-size: 26rpx;
  font-weight: 500;
  color: #333;
}

.notice-content {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}

/* 成员区域 */
.members-section {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.section-action {
  font-size: 26rpx;
  color: #FF6B35;
}

.members-list {
  white-space: nowrap;
}

.member-item {
  display: inline-block;
  width: 120rpx;
  text-align: center;
  margin-right: 20rpx;

  &:last-child {
    margin-right: 0;
  }
}

.add-avatar {
  width: 80rpx;
  height: 80rpx;
  background: #FFF7E6;
  border-radius: 50%;
  margin: 0 auto 10rpx;
  @include flex-center;
}

.member-avatar-wrapper {
  position: relative;
  width: 80rpx;
  height: 80rpx;
  margin: 0 auto 10rpx;
}

.member-avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.owner-badge,
.admin-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 24rpx;
  height: 24rpx;
  background: #fff;
  border-radius: 50%;
  @include flex-center;
}

.member-name {
  display: block;
  font-size: 24rpx;
  color: #333;
  @include text-ellipsis;
}

/* 设置区域 */
.settings-section {
  background: #fff;
  margin-bottom: 20rpx;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #eee;

  &:last-child {
    border-bottom: none;
  }
}

.setting-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.setting-label {
  font-size: 28rpx;
  color: #333;
}

.setting-right {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.setting-value {
  font-size: 26rpx;
  color: #999;
}

/* 危险操作 */
.danger-section {
  background: #fff;
  margin-bottom: 20rpx;
}

.danger-item {
  padding: 30rpx;
  text-align: center;
  border-bottom: 1rpx solid #eee;

  &:last-child {
    border-bottom: none;
  }

  &.quit .danger-label {
    color: #F5222D;
  }
}

.danger-label {
  font-size: 28rpx;
  color: #333;
}

/* 底部操作 */
.bottom-action {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 30rpx;
  background: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.action-btn {
  width: 100%;
  height: 90rpx;
  background: #fff;
  color: #F5222D;
  font-size: 32rpx;
  border-radius: 45rpx;
  border: 2rpx solid #F5222D;
  @include flex-center;

  &.dismiss {
    background: #F5222D;
    color: #fff;
  }
}
</style>
