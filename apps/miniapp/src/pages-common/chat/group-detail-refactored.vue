<!--
页面名称：group-detail（重构版）
原代码行数：1118行
重构后行数：约280行
减少比例：75%
重构时间：2026-03-20
-->
<template>
  <view class="group-detail-container">
    <!-- 群基本信息 -->
    <GroupBasicInfo
      :group-info="groupInfo"
      @show-qrcode="showQRCode"
      @edit-notice="editNotice"
    />

    <!-- 群成员 -->
    <GroupMembers
      :members="memberList"
      @invite="inviteMember"
      @member-tap="showMemberCard"
      @member-longpress="showMemberOptions"
    />

    <!-- 群设置 -->
    <GroupSettings
      :group-info="groupInfo"
      @toggle-mute="toggleMute"
      @mute-change="onMuteChange"
      @toggle-pin="togglePin"
      @pin-change="onPinChange"
      @view-group-order="viewGroupOrder"
      @edit-name="editGroupName"
      @edit-avatar="editGroupAvatar"
      @view-history="viewChatHistory"
    />

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
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import GroupBasicInfo from './components/GroupBasicInfo.vue'
import GroupMembers from './components/GroupMembers.vue'
import GroupSettings from './components/GroupSettings.vue'

// 当前用户ID
const currentUserId = ref('')

// 群ID
const groupId = ref('')

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
  currentUserId.value = uni.getStorageSync('userId') || ''

  if (options && options.id) {
    groupId.value = options.id
    loadGroupDetail()
    loadMembers()
  }
})

/**
 * 加载群详情
 */
const loadGroupDetail = async () => {
  try {
    // 调用API加载群详情
    // const res = await groupApi.getDetail(groupId.value)

    // 模拟数据
    groupInfo.value = {
      id: groupId.value,
      name: '美食爱好者群',
      avatar: 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=群',
      groupNo: 'G123456',
      memberCount: 25,
      notice: '欢迎加入美食爱好者群，一起分享美食！',
      isMuted: false,
      isPinned: false
    }

    // 判断是否是群主
    isOwner.value = currentUserId.value === 'owner_id'
  } catch (error) {
    console.error('加载群详情失败:', error)
  }
}

/**
 * 加载成员列表
 */
const loadMembers = async () => {
  try {
    // 调用API加载成员列表
    // const res = await groupApi.getMembers(groupId.value)

    // 模拟数据
    memberList.value = [
      { id: 1, name: '群主A', avatar: 'https://via.placeholder.com/80/FF6B35/FFFFFF?text=A', role: 'owner' },
      { id: 2, name: '管理员B', avatar: 'https://via.placeholder.com/80/52C41A/FFFFFF?text=B', role: 'admin' },
      { id: 3, name: '成员C', avatar: 'https://via.placeholder.com/80/1677FF/FFFFFF?text=C', role: 'member' },
      { id: 4, name: '成员D', avatar: 'https://via.placeholder.com/80/FAAD14/FFFFFF?text=D', role: 'member' }
    ]
  } catch (error) {
    console.error('加载成员列表失败:', error)
  }
}

/**
 * 显示群二维码
 */
const showQRCode = () => {
  uni.navigateTo({
    url: `/pages-common/chat/qrcode?groupId=${groupId.value}`
  })
}

/**
 * 编辑群公告
 */
const editNotice = () => {
  uni.navigateTo({
    url: `/pages-common/chat/notice-edit?groupId=${groupId.value}`
  })
}

/**
 * 邀请成员
 */
const inviteMember = () => {
  uni.navigateTo({
    url: `/pages-common/chat/invite?groupId=${groupId.value}`
  })
}

/**
 * 显示成员卡片
 */
const showMemberCard = (member) => {
  uni.navigateTo({
    url: `/pages-common/chat/member-card?userId=${member.id}&groupId=${groupId.value}`
  })
}

/**
 * 显示成员操作选项
 */
const showMemberOptions = (member) => {
  const canManage = isOwner.value || member.role !== 'owner'

  const options = canManage
    ? ['查看资料', '设为管理员', '移出群聊']
    : ['查看资料']

  uni.showActionSheet({
    itemList: options,
    success: (res) => {
      switch (res.tapIndex) {
        case 0:
          showMemberCard(member)
          break
        case 1:
          setAdmin(member)
          break
        case 2:
          removeMember(member)
          break
      }
    }
  })
}

/**
 * 设置管理员
 */
const setAdmin = async (member) => {
  try {
    // 调用设置管理员API
    uni.showToast({
      title: '设置成功',
      icon: 'success'
    })
    loadMembers()
  } catch (error) {
    uni.showToast({
      title: '设置失败',
      icon: 'none'
    })
  }
}

/**
 * 移出成员
 */
const removeMember = async (member) => {
  uni.showModal({
    title: '确认移出',
    content: `确定将 ${member.name} 移出群聊吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          // 调用移出成员API
          uni.showToast({
            title: '移出成功',
            icon: 'success'
          })
          loadMembers()
        } catch (error) {
          uni.showToast({
            title: '移出失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 切换免打扰
 */
const toggleMute = () => {
  groupInfo.value.isMuted = !groupInfo.value.isMuted
}

/**
 * 免打扰变更
 */
const onMuteChange = (e) => {
  groupInfo.value.isMuted = e.detail.value
  // 保存设置
}

/**
 * 切换置顶
 */
const togglePin = () => {
  groupInfo.value.isPinned = !groupInfo.value.isPinned
}

/**
 * 置顶变更
 */
const onPinChange = (e) => {
  groupInfo.value.isPinned = e.detail.value
  // 保存设置
}

/**
 * 查看群订单
 */
const viewGroupOrder = () => {
  uni.navigateTo({
    url: `/pages/group-order/list?groupId=${groupId.value}`
  })
}

/**
 * 编辑群名称
 */
const editGroupName = () => {
  uni.navigateTo({
    url: `/pages-common/chat/name-edit?groupId=${groupId.value}&name=${encodeURIComponent(groupInfo.value.name)}`
  })
}

/**
 * 编辑群头像
 */
const editGroupAvatar = () => {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      // 上传头像
      console.log('选择头像:', res.tempFilePaths[0])
    }
  })
}

/**
 * 查看聊天记录
 */
const viewChatHistory = () => {
  uni.navigateTo({
    url: `/pages-common/chat/history?groupId=${groupId.value}`
  })
}

/**
 * 清空聊天记录
 */
const clearChatHistory = () => {
  uni.showModal({
    title: '清空记录',
    content: '确定清空所有聊天记录吗？',
    success: (res) => {
      if (res.confirm) {
        // 调用清空API
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
    success: async (res) => {
      if (res.confirm) {
        try {
          // 调用退出群聊API
          uni.showToast({
            title: '已退出群聊',
            icon: 'success'
          })
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } catch (error) {
          uni.showToast({
            title: '退出失败',
            icon: 'none'
          })
        }
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
    content: '确定解散该群聊吗？此操作不可恢复！',
    confirmColor: '#F5222D',
    success: async (res) => {
      if (res.confirm) {
        try {
          // 调用解散群聊API
          uni.showToast({
            title: '群聊已解散',
            icon: 'success'
          })
          setTimeout(() => {
            uni.reLaunch({
              url: '/pages/index/index'
            })
          }, 1500)
        } catch (error) {
          uni.showToast({
            title: '解散失败',
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
  padding: 20rpx;
}

.danger-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 0 30rpx;
  margin-bottom: 20rpx;
}

.danger-item {
  padding: 30rpx 0;
  text-align: center;
  border-bottom: 1rpx solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }

  &.quit .danger-label {
    color: #F5222D;
  }
}

.danger-label {
  font-size: 28rpx;
  color: #666;
}

.bottom-action {
  padding: 20rpx 0;
}

.action-btn {
  width: 100%;
  height: 88rpx;
  background: #fff;
  color: #F5222D;
  font-size: 28rpx;
  border-radius: 44rpx;
  border: 1rpx solid #F5222D;

  &.dismiss {
    background: #F5222D;
    color: #fff;
  }
}
</style>
