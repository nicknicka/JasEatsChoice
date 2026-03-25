<template>
  <div class="chat-content-wrapper">
    <!-- 聊天消息区域 -->
    <div class="chat-messages" ref="chatContainerRef">
      <ChatMessage
        v-for="message in visibleMessages"
        :key="message.id || message.key"
        v-bind="message"
        @card-click="handleCardClick"
        @view-menu="handleViewMenu"
        @order-now="handleOrderNow"
      />
    </div>

    <!-- 底部输入区域容器 -->
    <div class="bottom-input-container">
      <!-- 快捷提问区域 -->
      <QuickQuestions
        :show="showQuickQuestions"
        :questions="quickQuestions"
        @close="showQuickQuestions = false"
        @select="handleQuickQuestion"
      />

      <!-- 输入框区域 -->
      <MessageInput
        v-model="inputMessage"
        :show-emoji-picker="showEmojiPicker"
        :show-quick-questions="showQuickQuestions"
        :uploaded-images="uploadedImages"
        :emojis="commonEmojis"
        :is-streaming="isStreaming"
        :is-loading="isLoading"
        :personal-data-enabled="aiPersonalDataEnabled"
        @toggle-emoji="toggleEmoji"
        @clear-input="clearInput"
        @clear-chat="clearChat"
        @show-quick-questions="showQuickQuestions = true"
        @select-emoji="selectEmoji"
        @remove-image="removeUploadedImage"
        @send="sendMessage"
        @stop-streaming="stopStreaming"
        @toggle-personal-data="handlePersonalDataToggle"
        @upload-image="handleImageUpload"
      />
    </div>

    <!-- 菜品选择弹窗 -->
    <DishSelectorDialog
      v-model="showDishSelectorDialog"
      :card-data="selectedCardData"
      @confirm="handleConfirmOrder"
      @close="handleDishSelectorClose"
      @add-dish="handleAddDish"
      @ai-order="handleAIOrder"
    />

    <!-- 支付方式选择弹窗 -->
    <PaymentDialog
      v-model="showPaymentDialog"
      :order-id="paymentOrderId"
      :merchant-name="paymentMerchantName"
      :total-amount="paymentTotalAmount"
      @success="handlePaymentSuccess"
      @close="handlePaymentClose"
      @insufficient-balance="handleInsufficientBalance"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ChatMessage from './ChatMessage.vue'
import QuickQuestions from './QuickQuestions.vue'
import MessageInput from './MessageInput.vue'
import DishSelectorDialog from '../../../components/merchant/DishSelectorDialog.vue'
import PaymentDialog from '../../../components/merchant/PaymentDialog.vue'
import { useAIChat } from '../../../composables/useAIChat'
import { useUserPreference } from '../../../composables/useUserPreference'
import { useImageUpload } from '../../../composables/useImageUpload'
import { QUICK_QUESTIONS, COMMON_EMOJIS, ERROR_MESSAGES, logger } from '../../../config/chatConfig'

// 聊天功能
const {
  messages,
  isLoading,
  isStreaming,
  chatContainerRef,
  loadMessages,
  sendMessage: sendChatMessage,
  clearChat,
  stopStreaming
} = useAIChat()

// 计算是否显示loading指示器
const showLoadingIndicator = computed(() => {
  // 简化逻辑：只要在加载中就显示
  const result = isLoading.value
  logger.log('📊 showLoadingIndicator计算:', {
    isLoading: isLoading.value,
    result: result
  })
  return result
})

// 监控loading状态变化（用于调试）
watch([isLoading, messages], () => {
  if (isLoading.value) {
    const lastMessage = messages.value[messages.value.length - 1]
    logger.log('🔄 Loading状态:', {
      isLoading: isLoading.value,
      messageCount: messages.value.length,
      lastSender: lastMessage?.sender,
      lastContentLength: lastMessage?.content?.length || 0,
      lastContent: lastMessage?.content,
      showLoading: showLoadingIndicator.value
    })
  }
}, { deep: true })

// 计算可见的消息列表
const visibleMessages = computed(() => {
  const result = []

  // 添加普通消息
  for (const message of messages.value) {
    // 如果是AI消息且内容为空且正在加载，跳过（会显示loading）
    if (message.sender === 'ai' && (!message.content || message.content.trim() === '') && isLoading.value) {
      logger.log('⏭️ 跳过空AI消息，显示loading')
      continue
    }
    result.push(message)
  }

  // 如果正在加载，添加loading消息
  if (isLoading.value) {
    logger.log('➕ 添加loading消息到列表')
    result.push({
      key: 'loading-indicator',
      sender: 'ai',
      content: '',
      time: '',
      avatar: '🤖',
      isLoading: true
    })
  }

  logger.log('📋 可见消息列表:', result.length, '条')
  return result
})

// 用户偏好
const { aiPersonalDataEnabled, loadUserPreference, handlePersonalDataToggle } = useUserPreference()

// 图片上传
const {
  uploadedImages,
  handleImageUpload: uploadImage,
  removeUploadedImage
} = useImageUpload()

// 本地状态
const inputMessage = ref('')
const showQuickQuestions = ref(true)
const showEmojiPicker = ref(false)
const inputContainerRef = ref(null)
const quickQuestions = ref(QUICK_QUESTIONS)
const commonEmojis = ref(COMMON_EMOJIS)

// 菜品选择弹窗状态
const showDishSelectorDialog = ref(false)
const selectedCardData = ref(null)

// 支付弹窗状态
const showPaymentDialog = ref(false)
const paymentOrderId = ref('')
const paymentMerchantName = ref('')
const paymentTotalAmount = ref(0)

/**
 * 切换表情面板
 */
const toggleEmoji = () => {
  showEmojiPicker.value = !showEmojiPicker.value
}

/**
 * 选择表情
 */
const selectEmoji = (emoji) => {
  inputMessage.value += emoji
  showEmojiPicker.value = false
  nextTick(() => {
    // 聚焦回输入框
    const textarea = document.querySelector('.message-textarea textarea')
    if (textarea) textarea.focus()
  })
}

/**
 * 清空输入
 */
const clearInput = () => {
  inputMessage.value = ''
  // 注意：不清空图片，只清空文本
  ElMessage.success(ERROR_MESSAGES.CLEARED)
}

/**
 * 点击外部区域关闭表情面板
 */
const handleClickOutside = (event) => {
  // 简单的点击外部关闭逻辑
  const emojiPanel = document.querySelector('.emoji-panel')
  const emojiButton = document.querySelector('[class*="is-active"]')

  if (emojiPanel && !emojiPanel.contains(event.target) && !emojiButton?.contains(event.target)) {
    showEmojiPicker.value = false
  }
}

/**
 * 处理快捷提问
 */
const handleQuickQuestion = (question) => {
  inputMessage.value = question
  // 自动发送
  sendMessage()
}

/**
 * 处理图片上传
 */
const handleImageUpload = (file, event) => {
  uploadImage(file, null, (error) => {
    logger.error('图片上传失败:', error)
  })
  // 清空input，允许重复上传
  if (event && event.target) {
    event.target.value = ''
  }
}

/**
 * 发送消息
 */
const sendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message) {
    ElMessage.warning('请输入问题')
    return
  }

  // 清空输入
  inputMessage.value = ''

  // 发送消息
  await sendChatMessage(message)
}

/**
 * 处理卡片点击
 */
const handleCardClick = (merchant) => {
  logger.log('卡片点击:', merchant)
  // 可以显示商家详情或其他操作
}

/**
 * 处理查看菜单
 */
const handleViewMenu = (merchant) => {
  logger.log('查看菜单:', merchant)
  // 可以打开菜单详情页或弹窗
  ElMessage.info('正在加载菜单...')
  // TODO: 实现查看菜单功能
}

/**
 * 处理立即下单
 */
const handleOrderNow = (orderData) => {
  logger.log('立即下单:', orderData)
  selectedCardData.value = orderData
  showDishSelectorDialog.value = true
}

/**
 * 确认订单
 */
const handleConfirmOrder = async (orderData) => {
  logger.log('确认订单:', orderData)

  try {
    // 调用AI创建订单
    const message = `请帮我创建订单，商家ID：${orderData.merchant.merchantId}，就餐方式：${orderData.diningMode}`
    await sendChatMessage(message)

    // TODO: 实际应该调用后端API创建订单
    // const response = await axios.post('/api/order/create', {
    //   merchantId: orderData.merchant.merchantId,
    //   dishItems: orderData.selectedDishes,
    //   diningMode: orderData.diningMode
    // })

    // 显示支付弹窗
    showPaymentDialog.value = true
    paymentOrderId.value = 'ORD' + Date.now() // 临时订单ID
    paymentMerchantName.value = orderData.merchant.name
    paymentTotalAmount.value = orderData.totalAmount
  } catch (error) {
    logger.error('创建订单失败:', error)
    ElMessage.error('创建订单失败，请重试')
  }
}

/**
 * 菜品选择弹窗关闭
 */
const handleDishSelectorClose = () => {
  showDishSelectorDialog.value = false
  selectedCardData.value = null
}

/**
 * 添加其他菜品
 */
const handleAddDish = (merchantId) => {
  logger.log('添加其他菜品:', merchantId)
  ElMessage.info('正在加载菜单...')
  // TODO: 实现添加菜品功能
}

/**
 * AI下单
 */
const handleAIOrder = async (orderData) => {
  logger.log('AI下单:', orderData)

  try {
    const message = `请帮我下单，商家：${orderData.merchant.name}，已选择菜品：${orderData.selectedDishes.map(d => d.dishName).join('、')}`
    await sendChatMessage(message)

    // 关闭菜品选择弹窗
    handleDishSelectorClose()

    // 显示支付弹窗
    showPaymentDialog.value = true
    paymentOrderId.value = 'ORD' + Date.now()
    paymentMerchantName.value = orderData.merchant.name
    paymentTotalAmount.value = calculateOrderTotal(orderData.selectedDishes, orderData.diningMode)
  } catch (error) {
    logger.error('AI下单失败:', error)
    ElMessage.error('下单失败，请重试')
  }
}

/**
 * 计算订单总价
 */
const calculateOrderTotal = (dishes, diningMode) => {
  let dishTotal = 0
  let totalItems = 0

  dishes.forEach(dish => {
    dishTotal += (dish.price || 0) * (dish.quantity || 0)
    totalItems += dish.quantity || 0
  })

  const packagingFee = diningMode === 'takeout' ? totalItems * 2 : 0
  return dishTotal + packagingFee
}

/**
 * 支付成功
 */
const handlePaymentSuccess = (paymentData) => {
  logger.log('支付成功:', paymentData)
  ElMessage.success('支付成功！订单已提交')

  // 关闭支付弹窗
  showPaymentDialog.value = false

  // TODO: 跳转到订单详情页
  // router.push(`/orders/${paymentData.orderId}`)
}

/**
 * 支付弹窗关闭
 */
const handlePaymentClose = () => {
  showPaymentDialog.value = false
}

/**
 * 余额不足
 */
const handleInsufficientBalance = (data) => {
  logger.log('余额不足:', data)
  ElMessageBox.alert(
    `账户余额：¥${data.balance.toFixed(2)}，需要：¥${data.required.toFixed(2)}。请充值或选择其他支付方式。`,
    '余额不足',
    {
      type: 'warning'
    }
  )
}

// 生命周期
onMounted(async () => {
  // 加载聊天历史
  await loadMessages()
  // 加载用户偏好
  await loadUserPreference()
  // 添加点击外部监听
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped lang="less">
.chat-content-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  flex: 1;
  gap: 8px;
  overflow: hidden;
  min-height: 0;
  box-sizing: border-box;
}

.chat-messages {
  width: 100%;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  background-color: #fff;
  border-radius: 16px;
  padding: 24px;
  box-sizing: border-box;
  box-shadow: 0 2px 16px 0 rgba(0, 0, 0, 0.06);

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: #dee2e6;
    border-radius: 3px;

    &:hover {
      background: #adb5bd;
    }
  }
}

/* 底部输入容器 */
.bottom-input-container {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
</style>
