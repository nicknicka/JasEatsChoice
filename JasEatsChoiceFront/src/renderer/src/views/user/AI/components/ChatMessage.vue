<template>
  <div
    class="chat-message"
    :class="{
      'user-message': sender === 'user',
      'ai-message': sender === 'ai'
    }"
  >
    <div class="message-avatar">{{ avatar }}</div>
    <div class="message-content">
      <!-- Loading状态 -->
      <div v-if="isLoading" class="message-loading">
        <div class="typing-indicator">
          <span></span>
          <span></span>
          <span></span>
        </div>
        <div class="loading-text">AI正在思考中...</div>
      </div>

      <!-- 进度状态（AI消息专属） -->
      <div v-else-if="sender === 'ai' && (isThinking || progress)" class="message-progress">
        <div class="progress-indicator">
          <div class="typing-indicator">
            <span></span>
            <span></span>
            <span></span>
          </div>
          <div class="progress-text">{{ progress ? content : 'AI正在思考中...' }}</div>
        </div>
      </div>

      <!-- 渲染卡片或文本 -->
      <template v-else-if="content">
        <!-- 商家下单卡片 -->
        <CardMessage
          v-if="parsedMessage.hasCard"
          :message="parsedMessage"
          @card-click="handleCardClick"
          @view-menu="handleViewMenu"
          @order-now="handleOrderNow"
        />
        <!-- 订单列表卡片 -->
        <div v-else-if="messageType === 'order_list_card' && cardData" class="message-card">
          <OrderListCard :data="cardData" @action="handleCardAction" />
        </div>
        <!-- 普通文本消息 -->
        <div v-else class="message-text markdown-content" v-html="formattedContent"></div>
        <div class="message-time">{{ time }}</div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { marked } from 'marked'
import OrderListCard from './cards/OrderListCard.vue'
import CardMessage from '../../../components/chat/CardMessage.vue'
import { parseCardData } from '../../../utils/cardMessageParser'

const props = defineProps({
  sender: {
    type: String,
    required: true
  },
  content: {
    type: String,
    required: true
  },
  time: {
    type: String,
    default: ''
  },
  avatar: {
    type: String,
    default: '🤖'
  },
  isLoading: {
    type: Boolean,
    default: false
  },
  isThinking: {
    type: Boolean,
    default: false
  },
  progress: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['action', 'card-click', 'view-menu', 'order-now'])

// 调试：监听props变化
watch(() => props.isLoading, (newVal) => {
  console.log('🔄 ChatMessage isLoading changed:', newVal, 'sender:', props.sender)
}, { immediate: true })

// 消息类型
const messageType = ref('text')
const cardData = ref(null)
const parsedMessage = ref({
  hasCard: false,
  text: '',
  cardData: null
})

// 格式化内容（Markdown渲染）
const formattedContent = computed(() => {
  if (messageType.value !== 'text') {
    return ''
  }

  // 配置marked选项
  marked.setOptions({
    breaks: true, // 支持换行
    gfm: true     // 支持GitHub风格Markdown
  })

  try {
    return marked.parse(props.content)
  } catch (error) {
    console.error('Markdown解析失败:', error)
    return props.content
  }
})

// 监听内容变化，尝试解析结构化数据
watch(() => props.content, (newContent) => {
  parseMessageContent(newContent)
}, { immediate: true })

/**
 * 解析消息内容，检测是否包含结构化数据
 */
const parseMessageContent = (content) => {
  if (!content || props.sender === 'user') {
    messageType.value = 'text'
    cardData.value = null
    parsedMessage.value = {
      hasCard: false,
      text: content,
      cardData: null
    }
    return
  }

  // 优先解析新的卡片数据格式（[CARD_DATA_START]...[CARD_DATA_END]）
  const parsed = parseCardData(content)
  if (parsed.hasCard) {
    parsedMessage.value = parsed
    messageType.value = parsed.cardData.cardType || 'card'
    cardData.value = parsed.cardData
    return
  }

  // 尝试解析订单列表（兼容旧的文本格式）
  const orderData = parseOrderList(content)
  if (orderData) {
    messageType.value = 'order_list_card'
    cardData.value = orderData
    parsedMessage.value = {
      hasCard: false,
      text: content,
      cardData: null
    }
    return
  }

  // 默认为文本消息
  messageType.value = 'text'
  cardData.value = null
  parsedMessage.value = {
    hasCard: false,
    text: content,
    cardData: null
  }
}

/**
 * 处理卡片点击
 */
const handleCardClick = (merchant) => {
  emit('card-click', merchant)
}

/**
 * 处理查看菜单
 */
const handleViewMenu = (merchant) => {
  emit('view-menu', merchant)
}

/**
 * 处理立即下单
 */
const handleOrderNow = (orderData) => {
  emit('order-now', orderData)
}

/**
 * 解析订单列表
 * 从文本中提取订单信息并转换为卡片数据格式
 */
const parseOrderList = (content) => {
  // 检测是否包含订单列表标识
  if (!content.includes('📜') && !content.includes('订单列表')) {
    return null
  }

  try {
    // 简单的解析逻辑（实际应该让后端返回JSON）
    const orders = []
    const lines = content.split('\n')

    let currentOrder = null
    let totalCount = 0

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i].trim()

      // 解析订单总数
      if (line.includes('共') && line.includes('条')) {
        const match = line.match(/共(\d+)条/)
        if (match) {
          totalCount = parseInt(match[1])
        }
      }

      // 解析订单行
      if (line.match(/^\d+\./) || line.match(/^\*\*\d+\./)) {
        if (currentOrder) {
          orders.push(currentOrder)
        }

        currentOrder = {
          orderId: '',
          status: 0,
          statusText: '未知',
          dishCount: 0,
          totalAmount: '0.00',
          createTime: '',
          actions: []
        }

        // 提取订单ID
        const idMatch = line.match(/订单\s+([A-Z0-9]+)/i) || line.match(/#([A-Z0-9]+)/i)
        if (idMatch) {
          currentOrder.orderId = idMatch[1]
        }
      }

      // 解析状态
      if (line.includes('状态：') || line.includes('状态:')) {
        const statusText = line.split(/：|:/)[1]?.trim() || ''
        currentOrder.statusText = statusText

        // 映射状态值
        const statusMap = {
          '待支付': 0,
          '待接单': 1,
          '制作中': 2,
          '已完成': 3,
          '已取消': 4
        }
        currentOrder.status = statusMap[statusText] || 0
      }

      // 解析金额
      if (line.includes('金额：') || line.includes('金额:')) {
        const amountMatch = line.match(/¥?([\d.]+)/)
        if (amountMatch) {
          currentOrder.totalAmount = amountMatch[1]
        }
      }

      // 解析时间
      if (line.includes('时间：') || line.includes('时间:')) {
        const timeText = line.split(/：|:/)[1]?.trim() || ''
        currentOrder.createTime = timeText
      }
    }

    // 添加最后一个订单
    if (currentOrder) {
      orders.push(currentOrder)
    }

    // 如果成功解析到订单，返回卡片数据
    if (orders.length > 0) {
      return {
        summary: '为您查询到订单列表',
        total: totalCount || orders.length,
        pendingCount: orders.filter(o => o.status < 3).length,
        orders: orders.map(order => ({
          ...order,
          dishCount: 1, // 默认值，实际应该从订单详情获取
          actions: order.status < 3 ? [
            { type: 'detail', text: '查看详情', icon: 'View' }
          ] : []
        }))
      }
    }
  } catch (error) {
    console.error('解析订单列表失败:', error)
  }

  return null
}

/**
 * 处理卡片操作
 */
const handleCardAction = (action) => {
  emit('action', action)
}
</script>

<style scoped>
.chat-message {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  animation: messageFadeIn 0.4s ease-out;
}

.user-message {
  flex-direction: row-reverse;
  justify-content: flex-start;
}

.ai-message {
  flex-direction: row;
  justify-content: flex-start;
}

.message-avatar {
  font-size: 42px;
  flex-shrink: 0;
  filter: drop-shadow(0 2px 6px rgba(0, 0, 0, 0.15));
  line-height: 1;
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-width: 75%;
}

.user-message .message-content {
  align-items: flex-end;
}

.ai-message .message-content {
  align-items: flex-start;
}

.message-card {
  width: 100%;
  max-width: 100%;
}

.message-text {
  padding: 14px 18px;
  border-radius: 20px;
  line-height: 1.7;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 0.929rem;
  white-space: pre-wrap;
  word-break: break-word;
}

.user-message .message-text {
  background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
  color: #fff;
  border-radius: 20px 20px 4px 20px;
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.25);
  font-weight: 500;
}

.ai-message .message-text {
  background: linear-gradient(135deg, #fff9fa 0%, #fff3f4 100%);
  color: #c8232c;
  border-radius: 4px 20px 20px 20px; /* 左上直角贴近头像，其余圆角 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #ffe0e3;
}

.message-text:hover {
  transform: translateY(-2px) scale(1.01);
}

.message-time {
  font-size: 0.857rem;
  color: #a8abb2;
  margin-top: 2px;
}

/* Markdown样式 */
.markdown-content {
  line-height: 1.8;
}

.markdown-content :deep(p) {
  margin: 0 0 8px 0;
}

.markdown-content :deep(p:last-child) {
  margin-bottom: 0;
}

.markdown-content :deep(code) {
  background: rgba(0, 0, 0, 0.05);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.9em;
}

.markdown-content :deep(pre) {
  background: rgba(0, 0, 0, 0.05);
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 8px 0;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin: 8px 0;
  padding-left: 24px;
}

.markdown-content :deep(li) {
  margin: 4px 0;
}

.markdown-content :deep(strong) {
  font-weight: 600;
}

@keyframes messageFadeIn {
  from {
    opacity: 0;
    transform: translateY(15px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* Loading动画 */
.message-loading {
  padding: 16px 20px;
  background: linear-gradient(135deg, #fff9fa 0%, #fff3f4 100%);
  border-radius: 20px 20px 20px 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #ffe0e3;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}

.typing-indicator span {
  width: 10px;
  height: 10px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
  border-radius: 50%;
  animation: typingBounce 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typingBounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.loading-text {
  font-size: 0.857rem;
  color: #909399;
  font-weight: 500;
}

.message-progress {
  padding: 12px 16px;
  background: linear-gradient(135deg, #fff9fa 0%, #fff3f4 100%);
  border-radius: 20px 20px 20px 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #ffe0e3;
}

.progress-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-indicator .typing-indicator {
  margin-bottom: 0;
  flex-shrink: 0;
}

.progress-text {
  font-size: 0.857rem;
  color: #606266;
  font-weight: 500;
}
</style>
