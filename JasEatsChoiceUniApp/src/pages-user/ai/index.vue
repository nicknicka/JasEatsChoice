<template>
  <view class="ai-chat-container">
    <!-- 聊天头部 -->
    <view class="chat-header">
      <view class="header-info">
        <text class="header-title">AI饮食助手</text>
        <text class="header-status">在线</text>
      </view>
      <view class="header-actions">
        <text class="action-btn" @click="clearHistory">清空</text>
      </view>
    </view>

    <!-- 快捷提问 -->
    <view class="quick-questions" v-if="messages.length <= 1">
      <view class="quick-title">🤔 您想了解什么？</view>
      <scroll-view class="quick-scroll" scroll-x>
        <view
          class="quick-item"
          v-for="(question, index) in quickQuestions"
          :key="index"
          @click="askQuickQuestion(question)"
        >
          <text class="quick-icon">{{ question.icon }}</text>
          <text class="quick-text">{{ question.text }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 聊天消息 -->
    <scroll-view
      class="chat-messages"
      scroll-y
      :scroll-into-view="scrollIntoView"
      :scroll-with-animation="true"
    >
      <!-- 欢迎消息 -->
      <view class="message-welcome" v-if="messages.length === 1">
        <text class="welcome-icon">👋</text>
        <text class="welcome-text">您好！我是您的AI饮食助手</text>
        <text class="welcome-tips">我可以帮您：</text>
        <view class="welcome-features">
          <text class="feature-item">• 推荐健康食谱</text>
          <text class="feature-item">• 分析营养成分</text>
          <text class="feature-item">• 制定饮食计划</text>
          <text class="feature-item">• 解答饮食疑问</text>
        </view>
      </view>

      <!-- 消息列表 -->
      <view
        class="message-wrapper"
        v-for="(msg, index) in displayMessages"
        :key="msg.id"
        :id="'msg-' + index"
      >
        <view class="message" :class="{ user: msg.isUser }">
          <!-- AI消息 -->
          <view class="message-avatar" v-if="!msg.isUser">
            <text class="avatar-icon">🤖</text>
          </view>

          <!-- 用户消息 -->
          <view class="message-content" :class="{ user: msg.isUser }">
            <!-- 文本消息 -->
            <text class="content-text" v-if="msg.type === 'text'">{{ msg.content }}</text>

            <!-- 建议卡片 -->
            <view class="content-card" v-if="msg.type === 'suggestion' && msg.suggestions">
              <view
                class="suggestion-item"
                v-for="(suggestion, sIndex) in msg.suggestions"
                :key="sIndex"
                @click="applySuggestion(suggestion)"
              >
                <text class="suggestion-icon">🍽️</text>
                <view class="suggestion-info">
                  <text class="suggestion-title">{{ suggestion.title }}</text>
                  <text class="suggestion-desc">{{ suggestion.desc }}</text>
                </view>
                <text class="suggestion-arrow">→</text>
              </view>
            </view>

            <!-- 时间戳 -->
            <text class="message-time">{{ msg.time }}</text>
          </view>

          <!-- 用户头像 -->
          <view class="message-avatar user" v-if="msg.isUser">
            <image class="avatar-image" :src="userInfo.avatar" mode="aspectFill" />
          </view>
        </view>
      </view>

      <!-- 加载动画 -->
      <view class="message-wrapper" v-if="isTyping">
        <view class="message">
          <view class="message-avatar">
            <text class="avatar-icon">🤖</text>
          </view>
          <view class="message-content typing">
            <view class="typing-indicator">
              <view class="typing-dot"></view>
              <view class="typing-dot"></view>
              <view class="typing-dot"></view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 输入区域 -->
    <view class="chat-input-area">
      <!-- 快捷功能 -->
      <view class="input-features">
        <scroll-view class="features-scroll" scroll-x>
          <view
            class="feature-btn"
            v-for="feature in inputFeatures"
            :key="feature.key"
            @click="useFeature(feature)"
          >
            <text class="feature-icon">{{ feature.icon }}</text>
            <text class="feature-label">{{ feature.label }}</text>
          </view>
        </scroll-view>
      </view>

      <!-- 输入框 -->
      <view class="input-wrapper">
        <input
          class="chat-input"
          type="text"
          v-model="inputText"
          placeholder="输入您的问题..."
          :maxlength="500"
          @confirm="sendMessage"
          confirm-type="send"
        />
        <button
          class="send-btn"
          :class="{ disabled: !inputText.trim() }"
          @click="sendMessage"
          :disabled="!inputText.trim()"
        >
          <text class="send-icon">发送</text>
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'

// 用户信息
const userInfo = ref({
  avatar: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=用户'
})

// 消息列表
const messages = ref([
  {
    id: Date.now(),
    type: 'text',
    content: '您好！我是AI饮食助手，有什么可以帮您的吗？',
    isUser: false,
    time: formatTime(new Date())
  }
])

// 输入文本
const inputText = ref('')

// 是否正在输入
const isTyping = ref(false)

// 滚动位置
const scrollIntoView = ref('')

// 快捷提问
const quickQuestions = ref([
  { icon: '🥗', text: '推荐健康食谱' },
  { icon: '📊', text: '分析营养成分' },
  { icon: '📅', text: '制定饮食计划' },
  { icon: '💪', text: '增肌食谱推荐' },
  { icon: '⚖️', text: '减脂饮食建议' },
  { icon: '🩺', text: '特殊人群饮食' }
])

// 输入功能
const inputFeatures = ref([
  { key: 'calorie', icon: '🔥', label: '卡路里' },
  { key: 'recipe', icon: '🍳', label: '食谱' },
  { key: 'plan', icon: '📅', label: '计划' },
  { key: 'health', icon: '💊', label: '健康' }
])

// 显示的消息（排除欢迎消息）
const displayMessages = computed(() => {
  return messages.value.slice(1)
})

/**
 * 格式化时间
 */
function formatTime(date) {
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

/**
 * 滚动到底部
 */
const scrollToBottom = async () => {
  await nextTick()
  if (displayMessages.value.length > 0) {
    scrollIntoView.value = 'msg-' + (displayMessages.value.length - 1)
  }
}

/**
 * 发送消息
 */
const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text) return

  // 添加用户消息
  const userMsg = {
    id: Date.now(),
    type: 'text',
    content: text,
    isUser: true,
    time: formatTime(new Date())
  }
  messages.value.push(userMsg)

  // 清空输入框
  inputText.value = ''

  // 滚动到底部
  await scrollToBottom()

  // 显示输入状态
  isTyping.value = true

  try {
    // TODO: 调用后端API
    // const res = await aiApi.chat({ message: text })

    // 模拟AI回复
    await new Promise(resolve => setTimeout(resolve, 1500))

    const aiMsg = {
      id: Date.now() + 1,
      type: 'text',
      content: generateAIResponse(text),
      isUser: false,
      time: formatTime(new Date())
    }

    // 如果包含建议，添加建议卡片
    if (shouldShowSuggestion(text)) {
      aiMsg.type = 'suggestion'
      aiMsg.suggestions = generateSuggestions(text)
    }

    messages.value.push(aiMsg)
    isTyping.value = false

    await scrollToBottom()

    // 保存到历史记录
    saveChatHistory()
  } catch (error) {
    console.error('发送消息失败:', error)
    isTyping.value = false
    uni.showToast({
      title: '发送失败，请重试',
      icon: 'none'
    })
  }
}

/**
 * 生成AI回复（模拟）
 */
const generateAIResponse = (text) => {
  const responses = {
    '推荐健康食谱': '根据您的需求，我为您推荐以下健康食谱：\n\n早餐：燕麦牛奶粥配鸡蛋（约420卡）\n午餐：清蒸鲈鱼配时蔬（约580卡）\n晚餐：鸡胸肉蔬菜沙拉（约380卡）\n\n这些食谱营养均衡，适合日常食用。',
    '分析营养成分': '请告诉我您想分析哪种食物的营养成分？我可以为您提供详细的分析报告。',
    '制定饮食计划': '为了制定个性化的饮食计划，我需要了解以下信息：\n\n1. 您的身高体重\n2. 运动习惯\n3. 饮食偏好\n4. 健康目标\n\n请提供这些信息，我会为您制定专属计划。',
    '增肌': '增肌期间建议：\n\n1. 蛋白质摄入：每公斤体重2-2.2g\n2. 碳水化合物：保证训练能量\n3. 脂肪：适量摄入，促进激素分泌\n4. 多餐少食：每天5-6餐\n\n推荐食物：鸡胸肉、牛肉、鸡蛋、牛奶、燕麦等。',
    '减脂': '减脂期间建议：\n\n1. 控制总热量：比日常摄入少300-500卡\n2. 高蛋白饮食：防止肌肉流失\n3. 低GI碳水：稳定血糖\n4. 多吃蔬菜：增加饱腹感\n\n注意：不要过度节食，要保证营养均衡。',
    'default': `收到您的问题："${text}"\n\n我正在为您分析，稍后会给出专业建议。\n\n您可以问我关于：\n• 营养成分分析\n• 食谱推荐\n• 饮食计划\n• 健康建议`
  }

  for (const [key, value] of Object.entries(responses)) {
    if (text.includes(key)) {
      return value
    }
  }

  return responses['default']
}

/**
 * 判断是否显示建议卡片
 */
const shouldShowSuggestion = (text) => {
  const keywords = ['推荐', '食谱', '怎么做', '怎样']
  return keywords.some(keyword => text.includes(keyword))
}

/**
 * 生成建议
 */
const generateSuggestions = (text) => {
  return [
    {
      title: '燕麦牛奶粥',
      desc: '营养早餐，15分钟即可完成'
    },
    {
      title: '清蒸鲈鱼',
      desc: '高蛋白低脂，适合减脂期'
    },
    {
      title: '蔬菜沙拉',
      desc: '低卡健康，富含维生素'
    }
  ]
}

/**
 * 快捷提问
 */
const askQuickQuestion = (question) => {
  inputText.value = question.text
  sendMessage()
}

/**
 * 使用功能
 */
const useFeature = (feature) => {
  const featureTexts = {
    calorie: '帮我分析一下今天的卡路里摄入',
    recipe: '推荐一些适合晚餐的健康食谱',
    plan: '帮我制定一个一周饮食计划',
    health: '增肌期间应该怎么安排饮食？'
  }
  inputText.value = featureTexts[feature.key]
  sendMessage()
}

/**
 * 应用建议
 */
const applySuggestion = (suggestion) => {
  uni.navigateTo({
    url: '/pages/recipe/today'
  })
}

/**
 * 清空历史
 */
const clearHistory = () => {
  uni.showModal({
    title: '清空聊天记录',
    content: '确定要清空所有聊天记录吗？',
    confirmColor: '#FF6B35',
    success: (res) => {
      if (res.confirm) {
        messages.value = [
          {
            id: Date.now(),
            type: 'text',
            content: '您好！我是AI饮食助手，有什么可以帮您的吗？',
            isUser: false,
            time: formatTime(new Date())
          }
        ]

        // 清空本地存储
        uni.removeStorageSync('chatHistory')

        uni.showToast({
          title: '已清空聊天记录',
          icon: 'success'
        })
      }
    }
  })
}

/**
 * 保存聊天历史
 */
const saveChatHistory = () => {
  try {
    uni.setStorageSync('chatHistory', JSON.stringify(messages.value))
  } catch (error) {
    console.error('保存聊天历史失败:', error)
  }
}

/**
 * 加载聊天历史
 */
const loadChatHistory = () => {
  try {
    const history = uni.getStorageSync('chatHistory')
    if (history) {
      messages.value = JSON.parse(history)
    }
  } catch (error) {
    console.error('加载聊天历史失败:', error)
  }
}

// 组件挂载
onMounted(() => {
  loadChatHistory()
  scrollToBottom()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.ai-chat-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  display: flex;
  flex-direction: column;
}

/* 聊天头部 */
.chat-header {
  background: linear-gradient(135deg, #FF6B35, #FF8F61);
  padding: $spacing-md $spacing-lg;
  @include flex-between;
  box-shadow: $box-shadow-md;
}

.header-info {
  @include flex-center-column;
  align-items: flex-start;
  gap: $spacing-xs;
}

.header-title {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: #fff;
}

.header-status {
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.8);
}

.header-actions {
  @include flex-center;
}

.action-btn {
  padding: $spacing-sm $spacing-md;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: $border-radius-round;
  font-size: $font-size-sm;
  color: #fff;

  &:active {
    opacity: 0.6;
  }
}

/* 快捷提问 */
.quick-questions {
  background-color: $bg-color-white;
  padding: $spacing-md;
  border-bottom: 1rpx solid $border-color-lighter;
}

.quick-title {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
  margin-bottom: $spacing-md;
}

.quick-scroll {
  white-space: nowrap;
}

.quick-item {
  display: inline-flex;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-sm $spacing-md;
  margin-right: $spacing-sm;
  background-color: rgba(255, 107, 53, 0.1);
  border-radius: $border-radius-round;
  flex-shrink: 0;

  &:active {
    transform: scale(0.95);
  }
}

.quick-icon {
  font-size: $font-size-lg;
}

.quick-text {
  font-size: $font-size-sm;
  color: $primary-color;
}

/* 聊天消息 */
.chat-messages {
  flex: 1;
  padding: $spacing-md;
  overflow-y: auto;
}

.message-welcome {
  @include flex-center-column;
  align-items: center;
  padding: 80rpx $spacing-lg;
  text-align: center;
}

.welcome-icon {
  font-size: 120rpx;
  margin-bottom: $spacing-lg;
}

.welcome-text {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  margin-bottom: $spacing-md;
}

.welcome-tips {
  font-size: $font-size-base;
  color: $text-color-secondary;
  margin-bottom: $spacing-lg;
}

.welcome-features {
  @include flex-center-column;
  gap: $spacing-sm;
  align-items: flex-start;
}

.feature-item {
  font-size: $font-size-sm;
  color: $text-color-regular;
  line-height: $line-height-lg;
}

.message-wrapper {
  margin-bottom: $spacing-lg;
}

.message {
  @include flex-center;
  gap: $spacing-sm;

  &.user {
    flex-direction: row-reverse;
  }
}

.message-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  @include flex-center;
  background: linear-gradient(135deg, #64B5F6, #2196F3);
  flex-shrink: 0;

  &.user {
    background: linear-gradient(135deg, #FF6B35, #FF8F61);
  }
}

.avatar-icon {
  font-size: 36rpx;
}

.avatar-image {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.message-content {
  max-width: 520rpx;
  padding: $spacing-md;
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-sm;
  position: relative;

  &.user {
    background: linear-gradient(135deg, $primary-color, #FF8F61);
    color: #fff;
  }

  &.typing {
    padding: $spacing-md $spacing-lg;
  }
}

.content-text {
  font-size: $font-size-base;
  line-height: $line-height-lg;
  white-space: pre-wrap;
  word-break: break-all;
}

.message-time {
  display: block;
  font-size: $font-size-xs;
  color: rgba(0, 0, 0, 0.3);
  margin-top: $spacing-xs;

  .user & {
    color: rgba(255, 255, 255, 0.6);
  }
}

/* 建议卡片 */
.content-card {
  margin-top: $spacing-sm;
}

.suggestion-item {
  @include flex-center;
  padding: $spacing-sm;
  margin-bottom: $spacing-sm;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;

  &:last-child {
    margin-bottom: 0;
  }

  &:active {
    background-color: rgba(255, 107, 53, 0.1);
  }
}

.suggestion-icon {
  font-size: $font-size-xl;
  margin-right: $spacing-sm;
}

.suggestion-info {
  flex: 1;
  @include flex-center-column;
  align-items: flex-start;
  gap: $spacing-xs;
}

.suggestion-title {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-medium;
}

.suggestion-desc {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.suggestion-arrow {
  font-size: $font-size-lg;
  color: $text-color-placeholder;
}

/* 输入动画 */
.typing-indicator {
  @include flex-center;
  gap: 8rpx;
}

.typing-dot {
  width: 12rpx;
  height: 12rpx;
  background-color: $text-color-secondary;
  border-radius: 50%;
  animation: typing 1.4s infinite;

  &:nth-child(2) {
    animation-delay: 0.2s;
  }

  &:nth-child(3) {
    animation-delay: 0.4s;
  }
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.7;
  }
  30% {
    transform: translateY(-10rpx);
    opacity: 1;
  }
}

/* 输入区域 */
.chat-input-area {
  background-color: $bg-color-white;
  padding: $spacing-md;
  border-top: 1rpx solid $border-color-lighter;
}

.input-features {
  margin-bottom: $spacing-md;
}

.features-scroll {
  white-space: nowrap;
}

.feature-btn {
  display: inline-flex;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-sm $spacing-md;
  margin-right: $spacing-sm;
  background-color: $bg-color-base;
  border-radius: $border-radius-round;
  flex-shrink: 0;

  &:active {
    background-color: rgba(255, 107, 53, 0.1);
  }
}

.feature-icon {
  font-size: $font-size-lg;
}

.feature-label {
  font-size: $font-size-sm;
  color: $text-color-regular;
}

.input-wrapper {
  @include flex-center;
  gap: $spacing-sm;
}

.chat-input {
  flex: 1;
  height: 80rpx;
  padding: 0 $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-round;
  font-size: $font-size-base;
  color: $text-color-primary;
}

.send-btn {
  width: 160rpx;
  height: 80rpx;
  @include flex-center;
  background: linear-gradient(135deg, $primary-color, #FF8F61);
  color: #fff;
  font-size: $font-size-base;
  border-radius: $border-radius-round;
  border: none;

  &.disabled {
    opacity: 0.4;
  }

  &:active:not(.disabled) {
    opacity: 0.8;
  }
}
</style>
