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

// 群ID
const groupId = ref('')

// 是否是群主
const isOwner = ref(false)

// 群信息
const groupInfo = ref({
  id: 1,
  name: '美食爱好者群',
  avatar: 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=群',
  groupNo: '123456',
  memberCount: 25,
  notice: '欢迎加入美食爱好者群，一起分享美食！',
  isMuted: false,
  isPinned: false
})

// 成员列表
const memberList = ref([])

onLoad((options) => {
  if (options.id) {
    groupId.value = options.id
  }
  if (options.action === 'create') {
    // 创建群聊
    initCreateGroup()
  } else {
    // 查看群详情
    loadGroupDetail()
  }
  loadMembers()
})

/**
 * 初始化创建群聊
 */
const initCreateGroup = () => {
  // TODO: 初始化创建群聊的表单
}

/**
 * 加载群详情
 */
const loadGroupDetail = async () => {
  try {
    // TODO: 调用API获取群详情
    // const res = await chatApi.getGroupDetail(groupId.value)
    // groupInfo.value = res.data

    // 模拟数据
    setTimeout(() => {
      // 判断是否是群主
      isOwner.value = memberList.value.length > 0 && memberList.value[0].role === 'owner'
    }, 300)
  } catch (error) {
    console.error('加载群详情失败:', error)
  }
}

/**
 * 加载成员列表
 */
const loadMembers = async () => {
  try {
    // TODO: 调用API获取成员列表
    // const res = await chatApi.getGroupMembers(groupId.value)

    // 模拟数据
    setTimeout(() => {
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
    }, 300)
  } catch (error) {
    console.error('加载成员失败:', error)
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
 * 编辑公告
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
    success: (res) => {
      if (res.confirm && res.content) {
        groupInfo.value.notice = res.content
        // TODO: 调用API更新群公告
        uni.showToast({
          title: '公告已更新',
          icon: 'success'
        })
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
 * 显示成员选项（长按）
 */
const showMemberOptions = (member) => {
  if (!isOwner.value && member.role !== 'owner') {
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
    success: (res) => {
      if (res.tapIndex === 0 && member.role === 'member') {
        // 设为管理员
        member.role = 'admin'
        uni.showToast({
          title: '已设为管理员',
          icon: 'success'
        })
      } else if (res.tapIndex === 0 && member.role === 'admin') {
        // 取消管理员
        member.role = 'member'
        uni.showToast({
          title: '已取消管理员',
          icon: 'success'
        })
      } else if (res.tapIndex === 1 || (res.tapIndex === 0 && member.role !== 'admin')) {
        // 移出群聊
        removeMember(member)
      }
    }
  })
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
 * 移除成员
 */
const removeMember = (member) => {
  uni.showModal({
    title: '确认移除',
    content: `确定将${member.name}移出群聊吗？`,
    success: (res) => {
      if (res.confirm) {
        // TODO: 调用API移除成员
        memberList.value = memberList.value.filter(m => m.id !== member.id)
        groupInfo.value.memberCount--
        uni.showToast({
          title: '已移除',
          icon: 'success'
        })
      }
    }
  })
}

/**
 * 切换免打扰
 */
const toggleMute = () => {
  groupInfo.value.isMuted = !groupInfo.value.isMuted
  // TODO: 调用API更新设置
}

const onMuteChange = (e) => {
  groupInfo.value.isMuted = e.detail.value
}

/**
 * 切换置顶
 */
const togglePin = () => {
  groupInfo.value.isPinned = !groupInfo.value.isPinned
  // TODO: 调用API更新设置
}

const onPinChange = (e) => {
  groupInfo.value.isPinned = e.detail.value
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
 * 编辑群名称
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
    success: (res) => {
      if (res.confirm && res.content) {
        groupInfo.value.name = res.content
        // TODO: 调用API更新群名称
        uni.showToast({
          title: '群名称已更新',
          icon: 'success'
        })
      }
    }
  })
}

/**
 * 编辑群头像
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
    success: (res) => {
      groupInfo.value.avatar = res.tempFilePaths[0]
      // TODO: 上传头像并调用API更新
      uni.showToast({
        title: '头像已更新',
        icon: 'success'
      })
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
 * 清空聊天记录
 */
const clearChatHistory = () => {
  uni.showModal({
    title: '清空聊天记录',
    content: '确定清空所有聊天记录吗？此操作不可恢复。',
    confirmColor: '#F5222D',
    success: (res) => {
      if (res.confirm) {
        // TODO: 调用API清空记录
        uni.showToast({
          title: '已清空',
          icon: 'success'
        })
      }
    }
  })
}

/**
 * 确认退出群聊
 */
const confirmQuit = () => {
  uni.showModal({
    title: '退出群聊',
    content: '确定退出该群聊吗？',
    confirmColor: '#F5222D',
    success: (res) => {
      if (res.confirm) {
        // TODO: 调用API退出群聊
        uni.showToast({
          title: '已退出群聊',
          icon: 'success'
        })
        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
      }
    }
  })
}

/**
 * 确认解散群聊
 */
const confirmDismiss = () => {
  uni.showModal({
    title: '解散群聊',
    content: '确定解散该群聊吗？此操作不可恢复，所有成员将被移除。',
    confirmColor: '#F5222D',
    success: (res) => {
      if (res.confirm) {
        // TODO: 调用API解散群聊
        uni.showToast({
          title: '群聊已解散',
          icon: 'success'
        })
        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
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
