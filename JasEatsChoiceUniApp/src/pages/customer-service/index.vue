<template>
  <view class="customer-service-page">
    <!-- 顶部导航栏 -->
    <view class="navbar">
      <view class="nav-content">
        <text class="nav-title">客服中心</text>
        <view class="nav-actions">
          <text class="action-btn" @click="showContactInfo">联系方式</text>
        </view>
      </view>
    </view>

    <!-- 聊天区域 -->
    <scroll-view
      class="chat-container"
      scroll-y
      :scroll-into-view="scrollToView"
      :scroll-with-animation="true"
      @scrolltoupper="loadHistoryMessages"
    >
      <!-- 历史消息加载提示 -->
      <view v-if="loadingHistory" class="loading-history">
        <text class="loading-text">加载历史消息中...</text>
      </view>

      <!-- 消息列表 -->
      <view id="message-list" class="message-list">
        <!-- 时间分隔符 -->
        <view v-for="(group, index) in messageGroups" :key="index">
          <view class="time-divider">
            <text class="time-text">{{ group.timeText }}</text>
          </view>

          <!-- 该时间组的消息 -->
          <view
            v-for="message in group.messages"
            :key="message.id"
            :id="'msg-' + message.id"
            :class="['message-item', message.sender === 'user' ? 'user-message' : 'service-message']"
          >
            <!-- 客服消息 -->
            <view v-if="message.sender === 'service'" class="service-message-content">
              <image class="avatar" src="/static/images/customer-service-avatar.png" mode="aspectFill" />
              <view class="message-content-wrapper">
                <view class="message-content">
                  <!-- 文本消息 -->
                  <text v-if="message.type === 'text'" class="text-content">{{ message.content }}</text>
                  <!-- 图片消息 -->
                  <image
                    v-else-if="message.type === 'image'"
                    class="image-content"
                    :src="message.content"
                    mode="widthFix"
                    @click="previewImage(message.content)"
                  />
                  <!-- 快捷回复按钮 -->
                  <view v-else-if="message.type === 'quick-replies'" class="quick-replies">
                    <view
                      v-for="(reply, idx) in message.replies"
                      :key="idx"
                      class="quick-reply-btn"
                      @click="sendQuickReply(reply)"
                    >
                      <text class="quick-reply-text">{{ reply }}</text>
                    </view>
                  </view>
                </view>
                <text class="message-time">{{ formatMessageTime(message.time) }}</text>
              </view>
            </view>

            <!-- 用户消息 -->
            <view v-else class="user-message-content">
              <view class="message-content-wrapper">
                <view class="message-content">
                  <!-- 文本消息 -->
                  <text v-if="message.type === 'text'" class="text-content">{{ message.content }}</text>
                  <!-- 图片消息 -->
                  <image
                    v-else-if="message.type === 'image'"
                    class="image-content"
                    :src="message.content"
                    mode="widthFix"
                    @click="previewImage(message.content)"
                  />
                </view>
                <view class="message-status">
                  <text class="message-time">{{ formatMessageTime(message.time) }}</text>
                  <uni-icons
                    v-if="message.status === 'sending'"
                    type="spinner-cycle"
                    size="12"
                    color="#999"
                  />
                  <uni-icons
                    v-else-if="message.status === 'success'"
                    type="checkmarkempty"
                    size="12"
                    color="#4CAF50"
                  />
                  <uni-icons
                    v-else-if="message.status === 'failed'"
                    type="closeempty"
                    size="12"
                    color="#F44336"
                    @click="resendMessage(message)"
                  />
                </view>
              </view>
              <image class="avatar" :src="userAvatar" mode="aspectFill" />
            </view>
          </view>
        </view>
      </view>

      <!-- 智能客服提示 -->
      <view v-if="showSmartTips && smartTips.length > 0" class="smart-tips">
        <view class="tips-header">
          <uni-icons type="help" size="16" color="#FF6B00" />
          <text class="tips-title">猜你想问</text>
        </view>
        <view class="tips-list">
          <view
            v-for="(tip, index) in smartTips"
            :key="index"
            class="tip-item"
            @click="sendQuickReply(tip)"
          >
            <text class="tip-text">{{ tip }}</text>
            <uni-icons type="right" size="14" color="#999" />
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部输入区域 -->
    <view class="input-area">
      <!-- 功能按钮 -->
      <view class="action-bar">
        <view class="action-btn-wrapper" @click="showQuickActions = !showQuickActions">
          <uni-icons type="plus" size="24" color="#666" />
        </view>
        <view class="action-btn-wrapper" @click="showFAQ">
          <uni-icons type="help" size="24" color="#666" />
        </view>
      </view>

      <!-- 输入框 -->
      <view class="input-wrapper">
        <textarea
          v-model="inputText"
          class="message-input"
          placeholder="请输入您的问题..."
          :auto-height="true"
          :maxlength="500"
          @focus="onInputFocus"
          @blur="onInputBlur"
        />
        <view
          :class="['send-btn', inputText.trim() ? 'active' : '']"
          @click="sendMessage"
        >
          <text class="send-text">发送</text>
        </view>
      </view>

      <!-- 快捷操作面板 -->
      <view v-if="showQuickActions" class="quick-actions-panel">
        <view class="action-grid">
          <view class="action-item" @click="chooseImage">
            <view class="action-icon-wrapper">
              <uni-icons type="image" size="28" color="#FF6B00" />
            </view>
            <text class="action-label">图片</text>
          </view>
          <view class="action-item" @click="takePhoto">
            <view class="action-icon-wrapper">
              <uni-icons type="camera" size="28" color="#FF6B00" />
            </view>
            <text class="action-label">拍照</text>
          </view>
          <view class="action-item" @click="submitTicket">
            <view class="action-icon-wrapper">
              <uni-icons type="compose" size="28" color="#FF6B00" />
            </view>
            <text class="action-label">工单</text>
          </view>
          <view class="action-item" @click="callHotline">
            <view class="action-icon-wrapper">
              <uni-icons type="phone" size="28" color="#FF6B00" />
            </view>
            <text class="action-label">电话</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 常见问题弹窗 -->
    <uni-popup ref="faqPopup" type="bottom">
      <view class="faq-popup">
        <view class="faq-header">
          <text class="faq-title">常见问题</text>
          <uni-icons type="close" size="20" color="#666" @click="closeFAQ" />
        </view>
        <scroll-view class="faq-content" scroll-y>
          <view
            v-for="(category, index) in faqCategories"
            :key="index"
            class="faq-category"
          >
            <view class="category-title" @click="toggleCategory(index)">
              <text class="category-name">{{ category.name }}</text>
              <uni-icons
                :type="category.expanded ? 'up' : 'down'"
                size="16"
                color="#999"
              />
            </view>
            <view v-if="category.expanded" class="faq-list">
              <view
                v-for="(faq, idx) in category.faqs"
                :key="idx"
                class="faq-item"
                @click="sendFAQQuestion(faq.question)"
              >
                <text class="faq-question">{{ faq.question }}</text>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>
    </uni-popup>

    <!-- 联系方式弹窗 -->
    <uni-popup ref="contactPopup" type="center">
      <view class="contact-popup">
        <view class="contact-header">
          <text class="contact-title">联系我们</text>
          <uni-icons type="close" size="20" color="#666" @click="closeContactInfo" />
        </view>
        <view class="contact-content">
          <view class="contact-item">
            <uni-icons type="phone" size="20" color="#FF6B00" />
            <text class="contact-label">客服热线</text>
            <text class="contact-value">400-123-4567</text>
          </view>
          <view class="contact-item">
            <uni-icons type="email" size="20" color="#FF6B00" />
            <text class="contact-label">客服邮箱</text>
            <text class="contact-value">service@jiaeats.com</text>
          </view>
          <view class="contact-item">
            <uni-icons type="chatbubble" size="20" color="#FF6B00" />
            <text class="contact-label">工作时间</text>
            <text class="contact-value">9:00-22:00</text>
          </view>
        </view>
        <view class="contact-footer">
          <button class="contact-btn primary" @click="callHotline">拨打电话</button>
        </view>
      </view>
    </uni-popup>

    <!-- 工单提交弹窗 -->
    <uni-popup ref="ticketPopup" type="center">
      <view class="ticket-popup">
        <view class="ticket-header">
          <text class="ticket-title">提交工单</text>
          <uni-icons type="close" size="20" color="#666" @click="closeTicket" />
        </view>
        <view class="ticket-content">
          <view class="form-item">
            <text class="form-label">问题类型</text>
            <picker :value="ticketTypeIndex" :range="ticketTypes" @change="onTicketTypeChange">
              <view class="picker-value">
                <text>{{ ticketTypes[ticketTypeIndex] }}</text>
                <uni-icons type="down" size="14" color="#999" />
              </view>
            </picker>
          </view>
          <view class="form-item">
            <text class="form-label">问题描述</text>
            <textarea
              v-model="ticketDescription"
              class="ticket-textarea"
              placeholder="请详细描述您遇到的问题..."
              :maxlength="500"
            />
            <text class="char-count">{{ ticketDescription.length }}/500</text>
          </view>
          <view class="form-item">
            <text class="form-label">联系方式</text>
            <input
              v-model="ticketContact"
              class="ticket-input"
              placeholder="请输入手机号或邮箱"
            />
          </view>
        </view>
        <view class="ticket-footer">
          <button class="ticket-btn secondary" @click="closeTicket">取消</button>
          <button class="ticket-btn primary" @click="submitTicketConfirm">提交</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const userAvatar = computed(() => userStore.userInfo?.avatar || '/static/images/default-avatar.png')

// 消息数据
const messages = ref([
  {
    id: 1,
    sender: 'service',
    type: 'text',
    content: '您好！我是佳食宜选的智能客服，有什么可以帮您的吗？',
    time: new Date().getTime() - 300000,
    status: 'success'
  }
])

const loadingHistory = ref(false)
const hasMoreHistory = ref(true)
const scrollToView = ref('')

// 输入相关
const inputText = ref('')
const showQuickActions = ref(false)
const showSmartTips = ref(true)

// 智能提示
const smartTips = ref([
  '如何修改收货地址？',
  '订单如何退款？',
  '优惠券怎么使用？',
  '忘记密码怎么办？'
])

// 常见问题
const faqCategories = ref([
  {
    name: '订单问题',
    expanded: false,
    faqs: [
      { question: '如何取消订单？' },
      { question: '订单支付失败怎么办？' },
      { question: '如何申请退款？' },
      { question: '配送时间可以修改吗？' }
    ]
  },
  {
    name: '账户问题',
    expanded: false,
    faqs: [
      { question: '如何修改密码？' },
      { question: '忘记密码怎么办？' },
      { question: '如何修改个人信息？' },
      { question: '如何注销账户？' }
    ]
  },
  {
    name: '优惠活动',
    expanded: false,
    faqs: [
      { question: '如何领取优惠券？' },
      { question: '积分如何使用？' },
      { question: '如何参加活动？' }
    ]
  },
  {
    name: '其他问题',
    expanded: false,
    faqs: [
      { question: '如何联系商家？' },
      { question: '如何评价订单？' },
      { question: '如何收藏菜品？' }
    ]
  }
])

// 工单相关
const ticketTypeIndex = ref(0)
const ticketTypes = ['订单问题', '支付问题', '账户问题', '功能建议', '其他']
const ticketDescription = ref('')
const ticketContact = ref('')

// 弹窗引用
const faqPopup = ref(null)
const contactPopup = ref(null)
const ticketPopup = ref(null)

// 按时间分组消息
const messageGroups = computed(() => {
  const groups = []
  let currentTimeGroup = null

  const sortedMessages = [...messages.value].sort((a, b) => a.time - b.time)

  sortedMessages.forEach(msg => {
    const msgDate = new Date(msg.time)
    const timeKey = `${msgDate.getFullYear()}-${msgDate.getMonth()}-${msgDate.getDate()}`

    if (!currentTimeGroup || currentTimeGroup.key !== timeKey) {
      currentTimeGroup = {
        key: timeKey,
        timeText: formatTimeGroup(msg.time),
        messages: []
      }
      groups.push(currentTimeGroup)
    }

    currentTimeGroup.messages.push(msg)
  })

  return groups
})

// 格式化时间分组
function formatTimeGroup(timestamp) {
  const date = new Date(timestamp)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)

  if (date.toDateString() === today.toDateString()) {
    return '今天'
  } else if (date.toDateString() === yesterday.toDateString()) {
    return '昨天'
  } else {
    return `${date.getMonth() + 1}月${date.getDate()}日`
  }
}

// 格式化消息时间
function formatMessageTime(timestamp) {
  const date = new Date(timestamp)
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

// 加载历史消息
function loadHistoryMessages() {
  if (loadingHistory.value || !hasMoreHistory.value) return

  loadingHistory.value = true
  setTimeout(() => {
    // 模拟加载历史消息
    hasMoreHistory.value = false
    loadingHistory.value = false
  }, 1000)
}

// 发送消息
function sendMessage() {
  const text = inputText.value.trim()
  if (!text) return

  const newMessage = {
    id: Date.now(),
    sender: 'user',
    type: 'text',
    content: text,
    time: Date.now(),
    status: 'sending'
  }

  messages.value.push(newMessage)
  inputText.value = ''
  showQuickActions.value = false

  // 滚动到底部
  nextTick(() => {
    scrollToView.value = 'msg-' + newMessage.id
  })

  // 模拟发送成功
  setTimeout(() => {
    newMessage.status = 'success'

    // 智能客服自动回复
    handleAutoReply(text)
  }, 1000)
}

// 发送快捷回复
function sendQuickReply(text) {
  inputText.value = text
  sendMessage()
}

// 处理自动回复
function handleAutoReply(userMessage) {
  let replyContent = ''
  let quickReplies = null

  // 简单的关键词匹配
  if (userMessage.includes('地址') || userMessage.includes('收货')) {
    replyContent = '您可以在"我的-收货地址"中管理您的收货地址。如需帮助，请告诉我具体遇到的问题。'
    quickReplies = ['如何新增地址？', '如何修改地址？', '如何删除地址？']
  } else if (userMessage.includes('退款') || userMessage.includes('取消订单')) {
    replyContent = '订单退款需要在订单详情页申请。请注意：已接单的订单需要商家同意才能退款。'
    quickReplies = ['查看订单详情', '联系商家协商', '申请平台介入']
  } else if (userMessage.includes('优惠券')) {
    replyContent = '您可以在"我的-优惠券"中查看所有可用优惠券。下单时自动匹配最优惠的方案。'
    quickReplies = ['如何领取优惠券？', '为什么用不了？', '优惠券有效期']
  } else if (userMessage.includes('密码')) {
    replyContent = '修改密码请前往"我的-设置-账号安全"。如果是忘记密码，可以通过手机号重置。'
    quickReplies = ['忘记密码', '修改密码', '账号安全']
  } else {
    replyContent = '收到您的问题，我正在为您查询相关信息，请稍等...'
    quickReplies = ['转人工客服', '提交工单', '查看常见问题']
  }

  setTimeout(() => {
    const replyMessage = {
      id: Date.now(),
      sender: 'service',
      type: 'text',
      content: replyContent,
      time: Date.now(),
      status: 'success'
    }
    messages.value.push(replyMessage)

    // 添加快捷回复选项
    if (quickReplies) {
      setTimeout(() => {
        const quickReplyMessage = {
          id: Date.now() + 1,
          sender: 'service',
          type: 'quick-replies',
          replies: quickReplies,
          time: Date.now(),
          status: 'success'
        }
        messages.value.push(quickReplyMessage)

        nextTick(() => {
          scrollToView.value = 'msg-' + quickReplyMessage.id
        })
      }, 500)
    }

    nextTick(() => {
      scrollToView.value = 'msg-' + replyMessage.id
    })
  }, 1000)
}

// 重发消息
function resendMessage(message) {
  const index = messages.value.findIndex(m => m.id === message.id)
  if (index > -1) {
    message.status = 'sending'
    setTimeout(() => {
      message.status = 'success'
    }, 1000)
  }
}

// 预览图片
function previewImage(url) {
  uni.previewImage({
    urls: [url],
    current: url
  })
}

// 选择图片
function chooseImage() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album'],
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0]
      sendImageMessage(tempFilePath)
    },
    fail: () => {
      uni.showToast({ title: '选择图片失败', icon: 'none' })
    }
  })
}

// 拍照
function takePhoto() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['camera'],
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0]
      sendImageMessage(tempFilePath)
    },
    fail: () => {
      uni.showToast({ title: '拍照失败', icon: 'none' })
    }
  })
}

// 发送图片消息
function sendImageMessage(imagePath) {
  const newMessage = {
    id: Date.now(),
    sender: 'user',
    type: 'image',
    content: imagePath,
    time: Date.now(),
    status: 'sending'
  }

  messages.value.push(newMessage)
  showQuickActions.value = false

  nextTick(() => {
    scrollToView.value = 'msg-' + newMessage.id
  })

  // 模拟上传成功
  setTimeout(() => {
    newMessage.status = 'success'

    // 客服回复
    setTimeout(() => {
      const replyMessage = {
        id: Date.now(),
        sender: 'service',
        type: 'text',
        content: '收到您的图片，我们正在查看，请稍等...',
        time: Date.now(),
        status: 'success'
      }
      messages.value.push(replyMessage)

      nextTick(() => {
        scrollToView.value = 'msg-' + replyMessage.id
      })
    }, 1000)
  }, 2000)
}

// 输入框焦点事件
function onInputFocus() {
  showQuickActions.value = false
  nextTick(() => {
    scrollToView.value = ''
  })
}

function onInputBlur() {
  // 延迟滚动，避免输入框收起时消息被遮挡
  setTimeout(() => {
    scrollToView.value = ''
  }, 300)
}

// 显示常见问题
function showFAQ() {
  faqPopup.value?.open()
}

// 关闭常见问题
function closeFAQ() {
  faqPopup.value?.close()
}

// 切换分类展开状态
function toggleCategory(index) {
  faqCategories.value[index].expanded = !faqCategories.value[index].expanded
}

// 发送FAQ问题
function sendFAQQuestion(question) {
  closeFAQ()
  sendQuickReply(question)
}

// 显示联系方式
function showContactInfo() {
  contactPopup.value?.open()
}

// 关闭联系方式
function closeContactInfo() {
  contactPopup.value?.close()
}

// 拨打客服电话
function callHotline() {
  uni.makePhoneCall({
    phoneNumber: '400-123-4567',
    fail: () => {
      uni.showToast({ title: '拨号失败', icon: 'none' })
    }
  })
  closeContactInfo()
}

// 提交工单
function submitTicket() {
  showQuickActions.value = false
  ticketPopup.value?.open()
}

// 关闭工单
function closeTicket() {
  ticketPopup.value?.close()
  // 重置表单
  ticketTypeIndex.value = 0
  ticketDescription.value = ''
  ticketContact.value = ''
}

// 工单类型改变
function onTicketTypeChange(e) {
  ticketTypeIndex.value = e.detail.value
}

// 确认提交工单
function submitTicketConfirm() {
  if (!ticketDescription.value.trim()) {
    uni.showToast({ title: '请填写问题描述', icon: 'none' })
    return
  }

  if (!ticketContact.value.trim()) {
    uni.showToast({ title: '请填写联系方式', icon: 'none' })
    return
  }

  // 模拟提交
  uni.showLoading({ title: '提交中...' })
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({ title: '工单提交成功', icon: 'success' })
    closeTicket()

    // 发送确认消息
    const confirmMessage = {
      id: Date.now(),
      sender: 'service',
      type: 'text',
      content: '您的工单已提交成功，工单号：TK' + Date.now() + '。我们会在24小时内处理您的问題，请耐心等待。',
      time: Date.now(),
      status: 'success'
    }
    messages.value.push(confirmMessage)

    nextTick(() => {
      scrollToView.value = 'msg-' + confirmMessage.id
    })
  }, 1500)
}

onMounted(() => {
  // 初始化滚动到底部
  nextTick(() => {
    if (messages.value.length > 0) {
      const lastMessage = messages.value[messages.value.length - 1]
      scrollToView.value = 'msg-' + lastMessage.id
    }
  })
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.customer-service-page {
  @include flex-column;
  height: 100vh;
  background-color: $bg-color;
}

.navbar {
  @include navbar;
  background: linear-gradient(135deg, #FF6B00 0%, #FF8F00 100%);

  .nav-content {
    @include flex-row;
    align-items: center;
    justify-content: space-between;
    height: 100%;
    padding: 0 $spacing-md;

    .nav-title {
      font-size: 18px;
      font-weight: bold;
      color: #FFFFFF;
    }

    .nav-actions {
      .action-btn {
        font-size: 14px;
        color: #FFFFFF;
      }
    }
  }
}

.chat-container {
  flex: 1;
  padding: $spacing-md;
  overflow-y: auto;
}

.loading-history {
  @include flex-center;
  padding: $spacing-md;

  .loading-text {
    font-size: 12px;
    color: $text-color-secondary;
  }
}

.message-list {
  min-height: 100%;
}

.time-divider {
  @include flex-center;
  margin: $spacing-lg 0;

  .time-text {
    font-size: 12px;
    color: $text-color-secondary;
    background-color: $bg-color-grey;
    padding: 4px 12px;
    border-radius: 12px;
  }
}

.message-item {
  margin-bottom: $spacing-md;

  &.user-message {
    .user-message-content {
      @include flex-row;
      justify-content: flex-end;

      .message-content-wrapper {
        align-items: flex-end;
        margin-right: $spacing-sm;
        max-width: 70%;

        .message-content {
          background: linear-gradient(135deg, #FF6B00 0%, #FF8F00 100%);
          border-radius: 12px 0 12px 12px;

          .text-content {
            color: #FFFFFF;
          }
        }

        .message-time,
        .message-status {
          color: $text-color-secondary;
        }
      }

      .avatar {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        flex-shrink: 0;
      }
    }
  }

  &.service-message {
    .service-message-content {
      @include flex-row;

      .avatar {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        flex-shrink: 0;
        margin-right: $spacing-sm;
      }

      .message-content-wrapper {
        max-width: 70%;

        .message-content {
          background-color: #FFFFFF;
          border-radius: 0 12px 12px 12px;
        }
      }
    }
  }
}

.message-content {
  padding: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

  .text-content {
    font-size: 15px;
    line-height: 1.5;
    word-break: break-all;
  }

  .image-content {
    max-width: 200px;
    border-radius: 8px;
  }
}

.message-time,
.message-status {
  font-size: 11px;
  margin-top: 4px;
  color: $text-color-secondary;
  @include flex-row;
  align-items: center;
}

.message-status {
  @include flex-row;
  gap: 4px;
}

.quick-replies {
  @include flex-column;
  gap: 8px;

  .quick-reply-btn {
    background-color: $primary-color;
    border-radius: 8px;
    padding: 10px 16px;
    transition: all 0.3s;

    &:active {
      opacity: 0.8;
      transform: scale(0.98);
    }

    .quick-reply-text {
      font-size: 14px;
      color: #FFFFFF;
    }
  }
}

.smart-tips {
  margin-top: $spacing-lg;
  padding: $spacing-md;
  background-color: #FFF8F0;
  border-radius: 12px;
  border: 1px solid #FFE4CC;

  .tips-header {
    @include flex-row;
    align-items: center;
    gap: 8px;
    margin-bottom: $spacing-sm;

    .tips-title {
      font-size: 14px;
      font-weight: bold;
      color: $primary-color;
    }
  }

  .tips-list {
    @include flex-column;
    gap: 8px;

    .tip-item {
      @include flex-row;
      align-items: center;
      justify-content: space-between;
      padding: 10px 12px;
      background-color: #FFFFFF;
      border-radius: 8px;
      transition: all 0.3s;

      &:active {
        background-color: $bg-color-grey;
      }

      .tip-text {
        flex: 1;
        font-size: 13px;
        color: $text-color-primary;
      }
    }
  }
}

.input-area {
  background-color: #FFFFFF;
  border-top: 1px solid $border-color;
  padding: $spacing-sm $spacing-md;
  padding-bottom: calc(#{$spacing-sm} + env(safe-area-inset-bottom));
}

.action-bar {
  @include flex-row;
  gap: $spacing-md;
  margin-bottom: $spacing-sm;

  .action-btn-wrapper {
    width: 32px;
    height: 32px;
    @include flex-center;
    background-color: $bg-color-grey;
    border-radius: 50%;
    transition: all 0.3s;

    &:active {
      opacity: 0.7;
      transform: scale(0.95);
    }
  }
}

.input-wrapper {
  @include flex-row;
  align-items: flex-end;
  gap: $spacing-sm;

  .message-input {
    flex: 1;
    min-height: 40px;
    max-height: 120px;
    padding: 10px 12px;
    background-color: $bg-color-grey;
    border-radius: 20px;
    font-size: 15px;
    line-height: 1.5;
  }

  .send-btn {
    width: 60px;
    height: 36px;
    @include flex-center;
    background-color: $bg-color-grey;
    border-radius: 18px;
    transition: all 0.3s;

    &.active {
      background: linear-gradient(135deg, #FF6B00 0%, #FF8F00 100%);

      .send-text {
        color: #FFFFFF;
      }
    }

    .send-text {
      font-size: 14px;
      color: $text-color-secondary;
    }

    &:active {
      transform: scale(0.95);
    }
  }
}

.quick-actions-panel {
  margin-top: $spacing-sm;
  padding: $spacing-md;
  background-color: #FFFFFF;
  border-radius: 12px;

  .action-grid {
    @include grid-layout(4, 1fr);
    gap: $spacing-lg;

    .action-item {
      @include flex-column;
      align-items: center;
      gap: 8px;

      .action-icon-wrapper {
        width: 48px;
        height: 48px;
        @include flex-center;
        background: linear-gradient(135deg, #FFF8F0 0%, #FFE4CC 100%);
        border-radius: 12px;
        transition: all 0.3s;

        &:active {
          transform: scale(0.95);
        }
      }

      .action-label {
        font-size: 12px;
        color: $text-color-primary;
      }
    }
  }
}

.faq-popup {
  height: 70vh;
  background-color: #FFFFFF;
  border-radius: 20px 20px 0 0;
  @include flex-column;

  .faq-header {
    @include flex-row;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-lg $spacing-lg $spacing-md;
    border-bottom: 1px solid $border-color;

    .faq-title {
      font-size: 16px;
      font-weight: bold;
    }
  }

  .faq-content {
    flex: 1;
    padding: $spacing-md;
  }

  .faq-category {
    margin-bottom: $spacing-md;
    background-color: #FFFFFF;
    border-radius: 12px;
    overflow: hidden;

    .category-title {
      @include flex-row;
      align-items: center;
      justify-content: space-between;
      padding: 14px $spacing-md;
      background-color: $bg-color-grey;

      .category-name {
        font-size: 15px;
        font-weight: bold;
      }
    }

    .faq-list {
      @include flex-column;

      .faq-item {
        padding: 14px $spacing-md;
        border-bottom: 1px solid $border-color;

        &:last-child {
          border-bottom: none;
        }

        &:active {
          background-color: $bg-color-grey;
        }

        .faq-question {
          font-size: 14px;
          color: $text-color-primary;
        }
      }
    }
  }
}

.contact-popup {
  width: 80%;
  max-width: 320px;
  background-color: #FFFFFF;
  border-radius: 16px;
  @include flex-column;

  .contact-header {
    @include flex-row;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-lg;
    border-bottom: 1px solid $border-color;

    .contact-title {
      font-size: 16px;
      font-weight: bold;
    }
  }

  .contact-content {
    padding: $spacing-lg;
    @include flex-column;
    gap: $spacing-lg;

    .contact-item {
      @include flex-column;
      gap: 8px;

      .contact-label {
        font-size: 12px;
        color: $text-color-secondary;
      }

      .contact-value {
        font-size: 14px;
        color: $text-color-primary;
        font-weight: bold;
      }
    }
  }

  .contact-footer {
    padding: $spacing-lg;
    border-top: 1px solid $border-color;

    .contact-btn {
      width: 100%;
      height: 44px;
      @include flex-center;
      background: linear-gradient(135deg, #FF6B00 0%, #FF8F00 100%);
      border-radius: 22px;
      font-size: 15px;
      color: #FFFFFF;
      border: none;

      &.primary {
        background: linear-gradient(135deg, #FF6B00 0%, #FF8F00 100%);
      }

      &:active {
        opacity: 0.9;
      }
    }
  }
}

.ticket-popup {
  width: 85%;
  max-width: 360px;
  background-color: #FFFFFF;
  border-radius: 16px;
  @include flex-column;

  .ticket-header {
    @include flex-row;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-lg;
    border-bottom: 1px solid $border-color;

    .ticket-title {
      font-size: 16px;
      font-weight: bold;
    }
  }

  .ticket-content {
    padding: $spacing-lg;
    @include flex-column;
    gap: $spacing-lg;
    max-height: 50vh;
    overflow-y: auto;

    .form-item {
      @include flex-column;
      gap: 8px;

      .form-label {
        font-size: 14px;
        font-weight: bold;
        color: $text-color-primary;
      }

      .picker-value {
        @include flex-row;
        align-items: center;
        justify-content: space-between;
        padding: 12px;
        background-color: $bg-color-grey;
        border-radius: 8px;
        font-size: 14px;
      }

      .ticket-textarea {
        min-height: 100px;
        padding: 12px;
        background-color: $bg-color-grey;
        border-radius: 8px;
        font-size: 14px;
        line-height: 1.5;
      }

      .ticket-input {
        padding: 12px;
        background-color: $bg-color-grey;
        border-radius: 8px;
        font-size: 14px;
      }

      .char-count {
        align-self: flex-end;
        font-size: 11px;
        color: $text-color-secondary;
      }
    }
  }

  .ticket-footer {
    @include flex-row;
    gap: $spacing-md;
    padding: $spacing-lg;
    border-top: 1px solid $border-color;

    .ticket-btn {
      flex: 1;
      height: 44px;
      @include flex-center;
      border-radius: 22px;
      font-size: 15px;
      border: none;

      &.secondary {
        background-color: $bg-color-grey;
        color: $text-color-primary;
      }

      &.primary {
        background: linear-gradient(135deg, #FF6B00 0%, #FF8F00 100%);
        color: #FFFFFF;
      }

      &:active {
        opacity: 0.9;
      }
    }
  }
}
</style>
