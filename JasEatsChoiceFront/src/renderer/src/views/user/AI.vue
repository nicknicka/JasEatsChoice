<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { ChatRound, Camera, Document, Loading, Delete, Picture, Pointer } from '@element-plus/icons-vue'
import axios from 'axios'

// 从配置中导入API地址
import { API_CONFIG } from '../../config/index.js'

// 常用问题快捷入口
const quickQuestions = ref([
  '推荐适合减肥的食谱',
  '今日卡路里摄入建议',
  '如何搭配营养均衡的饮食',
  '推荐低卡路里零食',
  '适合运动后的食物'
])

// 表情选择器
const emojiPicker = ref(false)
const emojis = ['😊', '👍', '🍎', '🥗', '💪', '🔥', '❤️', '✨', '🎯', '👨‍🍳']

// Chat messages
const messages = ref([])

// User input for chat
const inputMessage = ref('')
const inputMaxLength = 500 // Maximum message length for chat

// Loading state for chat
const isLoading = ref(false)
const isTyping = ref(false) // 打字机效果状态

// 聊天历史记录持久化
const STORAGE_KEY = 'ai-chat-history'

// 保存聊天记录到localStorage
const saveMessages = () => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(messages.value))
  } catch (error) {
    console.error('保存聊天记录失败:', error)
  }
}

// 从localStorage加载聊天记录
const loadMessages = () => {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved) {
      const parsed = JSON.parse(saved)
      if (parsed && parsed.length > 0) {
        messages.value = parsed
      } else {
        // 如果没有历史记录，添加欢迎消息
        messages.value = [
          {
            id: 1,
            sender: 'ai',
            content: '您好！我是您的AI饮食助手。有什么可以帮您的吗？',
            time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
            avatar: '🤖'
          }
        ]
      }
    } else {
      // 首次使用，添加欢迎消息
      messages.value = [
        {
          id: 1,
          sender: 'ai',
          content: '您好！我是您的AI饮食助手。有什么可以帮您的吗？',
          time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
          avatar: '🤖'
        }
      ]
    }
  } catch (error) {
    console.error('加载聊天记录失败:', error)
    messages.value = [
      {
        id: 1,
        sender: 'ai',
        content: '您好！我是您的AI饮食助手。有什么可以帮您的吗？',
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        avatar: '🤖'
      }
    ]
  }
}

// 清空聊天记录
const clearChat = () => {
  ElMessageBox.confirm('确定要清空所有聊天记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    messages.value = [
      {
        id: 1,
        sender: 'ai',
        content: '您好！我是您的AI饮食助手。有什么可以帮您的吗？',
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        avatar: '🤖'
      }
    ]
    saveMessages()
    ElMessage.success('聊天记录已清空')
  }).catch(() => {})
}

// 监听messages变化，自动保存
watch(messages, () => {
  saveMessages()
}, { deep: true })

// 打字机效果：逐字显示AI回复
const typeWriterEffect = async (messageObj, text) => {
  isTyping.value = true
  messageObj.content = ''

  for (let i = 0; i < text.length; i++) {
    messageObj.content += text[i]
    await new Promise(resolve => setTimeout(resolve, 30))

    // 自动滚动到底部
    await nextTick()
    const chatContainer = document.querySelector('.chat-messages')
    if (chatContainer) {
      chatContainer.scrollTop = chatContainer.scrollHeight
    }
  }

  isTyping.value = false
}

// Tab selection - AI聊天已设置为默认
const activeTab = ref('chat')

// AI Dish Recognition
const recognitionResult = ref(null)
const recognitionLoading = ref(false)
const recognitionProgress = ref(0) // 识别进度
const selectedImage = ref(null)
const imageMaxSize = 10 * 1024 * 1024 // 10MB maximum image size
const isDragging = ref(false) // 拖拽状态

// 拖拽上传处理
const handleDragOver = (event) => {
  event.preventDefault()
  isDragging.value = true
}

const handleDragLeave = (event) => {
  event.preventDefault()
  isDragging.value = false
}

const handleDrop = (event) => {
  event.preventDefault()
  isDragging.value = false

  const file = event.dataTransfer.files[0]
  if (file) {
    // Validate file type
    if (!file.type.startsWith('image/')) {
      ElMessage.error('请选择图片文件')
      return
    }

    // Validate file size
    if (file.size > imageMaxSize) {
      ElMessage.error('图片大小不能超过10MB')
      return
    }

    selectedImage.value = URL.createObjectURL(file)
    recognitionResult.value = null // Clear previous result
    ElMessage.success('图片上传成功')
  }
}

// 重新识别
const reRecognize = () => {
  recognitionResult.value = null
  recognizeDish()
}

// AI Recipe Optimization
const originalRecipe = ref('')
const optimizedRecipe = ref(null)
const optimizationLoading = ref(false)
const recipeMinLength = 20 // Minimum recipe length
const recipeMaxLength = 10000 // Maximum recipe length

// Image upload handling
const handleImageUpload = (event) => {
  const file = event.target.files[0]
  if (file) {
    // Validate file type
    if (!file.type.startsWith('image/')) {
      ElMessage.error('请选择图片文件')
      event.target.value = '' // Clear the input to allow reselect
      return
    }

    // Validate file size
    if (file.size > imageMaxSize) {
      ElMessage.error('图片大小不能超过10MB')
      event.target.value = '' // Clear the input to allow reselect
      return
    }

    selectedImage.value = URL.createObjectURL(file)
    recognitionResult.value = null // Clear previous result
    ElMessage.success('图片上传成功')
  }
}

// New method to handle image upload click
const handleUploadClick = () => {
  const input = document.getElementById('image-upload')
  if (input) {
    input.click()
  }
}

// Simulate AI dish recognition
const recognizeDish = () => {
  if (!selectedImage.value) {
    return
  }

  recognitionLoading.value = true
  recognitionProgress.value = 0

  // 模拟进度条
  const progressInterval = setInterval(() => {
    if (recognitionProgress.value < 90) {
      recognitionProgress.value += 10
    }
  }, 150)

  // Mock AI recognition
  setTimeout(() => {
    clearInterval(progressInterval)
    recognitionProgress.value = 100
    recognitionResult.value = {
      name: '宫保鸡丁',
      ingredients: ['鸡肉', '花生米', '辣椒', '黄瓜', '胡萝卜'],
      calories: 450,
      protein: 28,
      fat: 18,
      carbs: 15,
      difficulty: '中等',
      preparationTime: '25分钟',
      tags: ['川菜', '经典', '蛋白质丰富'],
      nutritionScore: 85
    }
    recognitionLoading.value = false
    ElMessage.success('识别成功！')
  }, 2000)
}

// 添加表情到输入框
const addEmoji = (emoji) => {
  inputMessage.value += emoji
  emojiPicker.value = false
}

// Simulate AI recipe optimization
const optimizeRecipe = () => {
  // Validate recipe content
  const trimmedRecipe = originalRecipe.value.trim()
  if (!trimmedRecipe) {
    ElMessage.warning('请输入食谱')
    return
  }
  if (trimmedRecipe.length < recipeMinLength) {
    ElMessage.warning(`食谱长度不能少于${recipeMinLength}个字符`)
    return
  }
  if (trimmedRecipe.length > recipeMaxLength) {
    ElMessage.warning(`食谱长度不能超过${recipeMaxLength}个字符`)
    return
  }

  optimizationLoading.value = true

  // Call backend API for recipe optimization
  axios
    .post(API_CONFIG.baseURL + API_CONFIG.ai.recipe, { foodName: originalRecipe.value })
    .then((response) => {
      // Format the backend response into the expected structure
      const backendRecipes = response.data.data
      // For simplicity, take the first recipe as the optimized result
      if (backendRecipes && backendRecipes.length > 0) {
        const firstRecipe = backendRecipes[0]
        optimizedRecipe.value = {
          original: originalRecipe.value,
          optimized: `推荐食谱：${firstRecipe.name}
难度：${firstRecipe.difficulty}
卡路里：${firstRecipe.calorie}大卡
食材：${firstRecipe.ingredients}
步骤：${firstRecipe.steps}`,
          improvements: ['营养均衡', '口味优化', '步骤简化']
        }
      } else {
        // No recipes returned from backend
        optimizedRecipe.value = {
          original: originalRecipe.value,
          optimized: `优化失败：没有找到合适的优化食谱。`,
          improvements: []
        }
      }
    })
    .catch((error) => {
      console.error('食谱优化接口调用失败:', error)
      let errorMsg = `优化失败：无法获取AI优化建议。`

      // Add more specific error messages
      if (error.response) {
        // Server responded with error status code
        if (error.response.status === 404) {
          errorMsg = '食谱优化服务暂时不可用，请稍后重试。'
        } else if (error.response.status === 500) {
          errorMsg = '服务器内部错误，请稍后重试。'
        }
      } else if (error.request) {
        // No response received from server
        errorMsg = '网络连接超时，请检查网络设置。'
      }

      optimizedRecipe.value = {
        original: originalRecipe.value,
        optimized: errorMsg,
        improvements: []
      }
      ElMessage.error(errorMsg)
    })
    .finally(() => {
      optimizationLoading.value = false
    })
}

// Handle keydown event for textarea
const handleKeyDown = (event) => {
  // Shift+Enter for newline, Enter to send
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

// Send message to AI
const sendMessage = () => {
  // Validate message content
  const trimmedMsg = inputMessage.value.trim()
  if (!trimmedMsg) {
    ElMessage.warning('请输入问题')
    return
  }
  if (trimmedMsg.length > inputMaxLength) {
    ElMessage.warning(`消息长度不能超过${inputMaxLength}个字符`)
    return
  }

  // Add user message
  const userMsg = {
    id: messages.value.length + 1,
    sender: 'user',
    content: trimmedMsg,
    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    avatar: '👤'
  }
  messages.value.push(userMsg)
  const userInput = trimmedMsg
  inputMessage.value = ''

  // Call backend AI API
  isLoading.value = true

  // 使用后端API获取AI回复
  axios
    .post(API_CONFIG.baseURL + API_CONFIG.ai.chat, { message: userInput })
    .then(async (response) => {
      // Check if response is valid
      if (response.data && response.data.data && response.data.data.content) {
        // 创建AI消息对象
        const aiResponse = {
          id: messages.value.length + 1,
          sender: 'ai',
          content: '', // 初始为空，由打字机效果填充
          time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
          avatar: '🤖'
        }
        messages.value.push(aiResponse)

        // 使用打字机效果显示回复
        await typeWriterEffect(aiResponse, response.data.data.content)
      } else {
        throw new Error('Invalid response format')
      }
    })
    .catch((error) => {
      console.error('AI聊天接口调用失败:', error)
      let errorMsg = '对不起，暂时无法获取AI回复，请稍后重试。'

      // Add more specific error messages
      if (error.response) {
        // Server responded with error status code
        if (error.response.status === 404) {
          errorMsg = 'AI聊天服务暂时不可用，请稍后重试。'
        } else if (error.response.status === 500) {
          errorMsg = '服务器内部错误，请稍后重试。'
        }
      } else if (error.request) {
        // No response received from server
        errorMsg = '网络连接超时，请检查网络设置。'
      }

      const aiResponse = {
        id: messages.value.length + 1,
        sender: 'ai',
        content: errorMsg,
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        avatar: '🤖'
      }
      messages.value.push(aiResponse)
    })
    .finally(() => {
      isLoading.value = false
    })
}

// Ensure AI聊天 is the default tab on component mount
onMounted(() => {
  activeTab.value = 'chat'
  loadMessages() // 加载聊天历史记录
})
</script>

<template>
  <div class="app-container">
    <div class="main-content">
      <!-- Right Content Area -->
      <el-main class="content-area">
        <div class="ai-chat-container">
          <div class="chat-header">
            <h2>AI饮食助手</h2>
            <div class="chat-info">
              <el-tag type="success">在线</el-tag>
              <el-button @click="clearChat" type="danger" size="small" plain>
                <el-icon><Delete /></el-icon>
                清空对话
              </el-button>
            </div>
          </div>

          <!-- Tab Menu -->
          <el-tabs v-model="activeTab" type="border-card" class="ai-tabs">
            <el-tab-pane label="AI聊天" name="chat" :icon="ChatRound">
              <!-- 常用问题快捷入口 -->
              <div class="quick-questions">
                <div class="quick-questions-title">💡 快捷提问：</div>
                <el-tag
                  v-for="q in quickQuestions"
                  :key="q"
                  @click="inputMessage = q"
                  class="question-tag"
                  type="info"
                  effect="plain"
                >
                  {{ q }}
                </el-tag>
              </div>

              <div class="chat-messages">
                <div
                  v-for="message in messages"
                  :key="message.id"
                  class="chat-message"
                  :class="{
                    'user-message': message.sender === 'user',
                    'ai-message': message.sender === 'ai'
                  }"
                >
                  <div class="message-avatar">{{ message.avatar }}</div>
                  <div class="message-content">
                    <div class="message-text">{{ message.content }}</div>
                    <div class="message-time">{{ message.time }}</div>
                  </div>
                </div>

                <div v-if="isLoading" class="chat-message ai-message loading">
                  <div class="message-avatar">🤖</div>
                  <div class="message-content">
                    <el-skeleton :rows="2" style="width: 200px"></el-skeleton>
                  </div>
                </div>
              </div>

              <div class="chat-input-area">
                <div class="input-wrapper">
                  <div class="input-toolbar">
                    <el-button @click="emojiPicker = !emojiPicker" size="small" text>
                      😊 表情
                    </el-button>
                    <!-- 表情选择器 -->
                    <div v-if="emojiPicker" class="emoji-picker">
                      <span
                        v-for="emoji in emojis"
                        :key="emoji"
                        @click="addEmoji(emoji)"
                        class="emoji-item"
                      >
                        {{ emoji }}
                      </span>
                    </div>
                  </div>

                  <el-input
                    v-model="inputMessage"
                    placeholder="请输入您的问题...（例如：推荐适合减肥的食谱）&#10;Enter 发送，Shift+Enter 换行"
                    clearable
                    resize="none"
                    :rows="2"
                    type="textarea"
                    @keydown="handleKeyDown"
                    maxlength="500"
                    show-word-limit
                  />
                  <div class="input-footer">
                    <span class="input-hint">💡 按 Enter 发送，Shift+Enter 换行</span>
                  </div>
                </div>
                <el-button
                  type="primary"
                  size="large"
                  class="send-btn"
                  @click="sendMessage"
                  :disabled="isLoading || isTyping"
                >
                  <el-icon><ChatRound /></el-icon>
                  发送
                </el-button>
              </div>
            </el-tab-pane>

            <el-tab-pane label="菜品识别" name="recognition" :icon="Camera">
              <div class="recognition-section">
                <div class="upload-area">
                  <input
                    type="file"
                    accept="image/*"
                    style="display: none"
                    id="image-upload"
                    @change="handleImageUpload"
                  />
                  <div
                    class="upload-zone"
                    :class="{ 'has-image': selectedImage, 'is-dragging': isDragging }"
                    @click="handleUploadClick"
                    @dragover="handleDragOver"
                    @dragleave="handleDragLeave"
                    @drop="handleDrop"
                  >
                    <div v-if="!selectedImage" class="upload-placeholder">
                      <el-icon :size="48"><Camera /></el-icon>
                      <p class="upload-text">点击或拖拽上传菜品图片</p>
                      <p class="upload-hint">支持 JPG、PNG 格式，最大 10MB</p>
                    </div>
                    <div v-else class="image-preview">
                      <img :src="selectedImage" alt="菜品图片" />
                      <div class="image-overlay">
                        <el-button type="danger" size="small" @click.stop="selectedImage = null">
                          <el-icon><Delete /></el-icon>
                          删除图片
                        </el-button>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 识别进度条 -->
                <div v-if="recognitionLoading" class="recognition-progress">
                  <el-progress :percentage="recognitionProgress" :stroke-width="12" />
                  <p class="progress-text">正在识别菜品，请稍候...</p>
                </div>

                <div class="recognition-buttons">
                  <el-button
                    type="primary"
                    size="large"
                    class="recognize-btn"
                    @click="recognizeDish"
                    :disabled="!selectedImage || recognitionLoading"
                  >
                    <el-icon v-if="recognitionLoading"><Loading /></el-icon>
                    {{ recognitionLoading ? '识别中...' : '🔍 开始识别菜品' }}
                  </el-button>

                  <el-button
                    v-if="recognitionResult"
                    type="success"
                    size="large"
                    class="re-recognize-btn"
                    @click="reRecognize"
                    :disabled="recognitionLoading"
                  >
                    🔄 重新识别
                  </el-button>
                </div>

                <div v-if="recognitionResult" class="recognition-result">
                  <div class="result-header">
                    <h4>✨ 识别结果</h4>
                  </div>
                  <div class="result-cards">
                    <div class="result-card main-card">
                      <div class="card-label">菜品名称</div>
                      <div class="card-value">{{ recognitionResult.name }}</div>
                    </div>
                    <div class="result-card calories-card">
                      <div class="card-label">🔥 卡路里</div>
                      <div class="card-value highlight">{{ recognitionResult.calories }} kcal</div>
                    </div>
                    <div class="result-card">
                      <div class="card-label">👨‍🍳 难度</div>
                      <div class="card-value">{{ recognitionResult.difficulty }}</div>
                    </div>
                    <div class="result-card">
                      <div class="card-label">⏱️ 烹饪时间</div>
                      <div class="card-value">{{ recognitionResult.preparationTime }}</div>
                    </div>

                    <!-- 营养成分图表 -->
                    <div class="result-card full-width nutrition-card">
                      <div class="card-label">📊 营养成分</div>
                      <div class="nutrition-chart">
                        <div class="nutrition-item">
                          <div class="nutrition-label">
                            <span class="nutrition-icon">💪</span>
                            <span>蛋白质</span>
                          </div>
                          <div class="nutrition-bar">
                            <div
                              class="nutrition-fill protein"
                              :style="{ width: recognitionResult.protein + '%' }"
                            ></div>
                          </div>
                          <div class="nutrition-value">{{ recognitionResult.protein }}g</div>
                        </div>
                        <div class="nutrition-item">
                          <div class="nutrition-label">
                            <span class="nutrition-icon">🧈</span>
                            <span>脂肪</span>
                          </div>
                          <div class="nutrition-bar">
                            <div
                              class="nutrition-fill fat"
                              :style="{ width: recognitionResult.fat + '%' }"
                            ></div>
                          </div>
                          <div class="nutrition-value">{{ recognitionResult.fat }}g</div>
                        </div>
                        <div class="nutrition-item">
                          <div class="nutrition-label">
                            <span class="nutrition-icon">🍞</span>
                            <span>碳水</span>
                          </div>
                          <div class="nutrition-bar">
                            <div
                              class="nutrition-fill carbs"
                              :style="{ width: recognitionResult.carbs + '%' }"
                            ></div>
                          </div>
                          <div class="nutrition-value">{{ recognitionResult.carbs }}g</div>
                        </div>
                      </div>
                    </div>

                    <div class="result-card full-width">
                      <div class="card-label">🥗 主要食材</div>
                      <div class="card-value">
                        <el-tag v-for="ingredient in recognitionResult.ingredients" :key="ingredient" class="ingredient-tag">
                          {{ ingredient }}
                        </el-tag>
                      </div>
                    </div>
                    <div class="result-card full-width">
                      <div class="card-label">🏷️ 标签</div>
                      <div class="card-value">
                        <el-tag
                          v-for="tag in recognitionResult.tags"
                          :key="tag"
                          type="success"
                          class="tag-item"
                        >
                          {{ tag }}
                        </el-tag>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="食谱优化" name="recipe" :icon="Document">
              <div class="recipe-section">
                <div class="recipe-input">
                  <el-input
                    v-model="originalRecipe"
                    placeholder="请输入您的食谱...&#10;例如：西红柿鸡蛋的做法：1. 准备西红柿2个，鸡蛋2个；2. 煎鸡蛋；3. 炒西红柿；4. 混合翻炒"
                    clearable
                    resize="vertical"
                    :rows="6"
                    type="textarea"
                    maxlength="10000"
                    show-word-limit
                  />
                </div>

                <el-button
                  type="primary"
                  size="large"
                  class="optimize-btn"
                  @click="optimizeRecipe"
                  :disabled="!originalRecipe || optimizationLoading"
                >
                  <el-icon v-if="optimizationLoading"><Loading /></el-icon>
                  {{ optimizationLoading ? '优化中...' : '✨ 开始优化食谱' }}
                </el-button>

                <div v-if="optimizedRecipe" class="recipe-result">
                  <div class="result-header">
                    <h4>✨ 优化结果</h4>
                  </div>

                  <div class="recipe-comparison">
                    <div class="recipe-card original-recipe-card">
                      <div class="card-header">
                        <span class="card-title">📝 原食谱</span>
                      </div>
                      <div class="card-content">
                        <pre>{{ optimizedRecipe.original }}</pre>
                      </div>
                    </div>

                    <div class="recipe-arrow">→</div>

                    <div class="recipe-card optimized-recipe-card">
                      <div class="card-header">
                        <span class="card-title">⭐ 优化后</span>
                      </div>
                      <div class="card-content">
                        <pre>{{ optimizedRecipe.optimized }}</pre>
                      </div>
                    </div>
                  </div>

                  <div class="improvements-section">
                    <div class="improvements-title">🎯 优化亮点</div>
                    <div class="improvements-tags">
                      <el-tag
                        v-for="improvement in optimizedRecipe.improvements"
                        :key="improvement"
                        size="large"
                        type="success"
                        effect="plain"
                      >
                        {{ improvement }}
                      </el-tag>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-main>
    </div>
  </div>
</template>

<style scoped lang="less">
.app-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.top-nav-bar {
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  color: #ff6b6b;
}

.search-input {
  width: 400px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
}

.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.sidebar-menu {
  background-color: #f0f2f5;
  border-right: 1px solid #e6e8eb;
  padding: 20px 0;
  display: flex;
  flex-direction: column;

  .avatar-section {
    text-align: center;
    padding-bottom: 20px;
    border-bottom: 1px solid #e6e8eb;
    margin-bottom: 20px;
  }

  .menu-list {
    border: none;
    flex: 1;
  }

  .setting-menu {
    border-top: 1px solid #e6e8eb;
    margin-top: auto;
    width: 100%;
  }
}

.content-area {
  padding: 20px;
  background-color: #fafafa;
  overflow-y: auto;
}

.ai-chat-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  max-width: 900px;
  margin: 0 auto;

  .chat-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      font-size: 24px;
      font-weight: bold;
      margin: 0;
    }

    .chat-info {
      display: flex;
      gap: 10px;
    }
  }

  .ai-tabs {
    flex: 1;
    display: flex;
    flex-direction: column;

    :deep(.el-tabs__content) {
      flex: 1;
      overflow-y: auto;
      padding: 20px 0;
    }

    :deep(.el-tabs__content-item) {
      height: 100%;
    }
  }

  .chat-messages {
    /* 响应式聊天框高度 */
    flex: 1;
    min-height: 400px;
    max-height: calc(100vh - 500px);
    overflow-y: auto;
    background-color: #fff;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
    margin-bottom: 20px;

    .chat-message {
      display: flex;
      gap: 15px;
      margin-bottom: 20px;
      animation: messageFadeIn 0.3s ease-out;

      &.user-message {
        flex-direction: row-reverse;
        justify-content: flex-start;

        .message-content {
          align-items: flex-end;

          .message-text {
            background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
            color: #fff;
            border-radius: 18px 18px 0 18px;
            box-shadow: 0 2px 8px rgba(255, 107, 107, 0.2);
          }
        }
      }

      &.ai-message {
        flex-direction: row;
        justify-content: flex-start;

        .message-content {
          align-items: flex-start;

          .message-text {
            background: linear-gradient(135deg, #fff5f5 0%, #ffe8e8 100%);
            color: #d32f2f;
            border-radius: 18px 18px 18px 0;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
            border: 1px solid #ffcdd2;
          }
        }
      }

      &.loading {
        .message-text {
          background-color: #f5f7fa;
        }
      }

      .message-avatar {
        font-size: 40px;
        flex-shrink: 0;
        filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
      }

      .message-content {
        display: flex;
        flex-direction: column;
        gap: 5px;

        .message-text {
          max-width: 70%;
          padding: 12px 16px;
          border-radius: 18px;
          line-height: 1.6;
          transition: transform 0.2s ease;

          &:hover {
            transform: translateY(-2px);
          }
        }

        .message-time {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }

  .chat-input-area {
    /* 固定发送消息区域在页面底部 */
    position: sticky;
    bottom: 0;
    background-color: #fafafa;
    padding: 10px 0 20px 0;
    z-index: 100;
    display: flex;
    gap: 10px;
    margin-bottom: 20px;

    .input-wrapper {
      flex: 1;
      position: relative;

      .el-input {
        textarea {
          min-height: 80px;
          border-radius: 12px;
          transition: all 0.3s ease;

          &:focus {
            box-shadow: 0 0 0 2px rgba(255, 107, 107, 0.1);
          }
        }
      }

      .input-footer {
        position: absolute;
        bottom: 8px;
        right: 12px;

        .input-hint {
          font-size: 12px;
          color: #909399;
          background-color: rgba(255, 255, 255, 0.9);
          padding: 2px 8px;
          border-radius: 4px;
        }
      }
    }

    .send-btn {
      align-self: flex-end;
      background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
      border: none;
      height: 80px;
      padding: 0 30px;
      font-weight: bold;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(255, 107, 107, 0.4);
      }

      &:active {
        transform: translateY(0);
      }

      &:disabled {
        background: #c0c4cc;
        transform: none;
        box-shadow: none;
      }
    }
  }

  /* Dish Recognition Section */
  .recognition-section {
    padding: 20px;
    background-color: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);

    .upload-area {
      margin-bottom: 20px;

      .upload-zone {
        border: 3px dashed #ff6b6b;
        border-radius: 12px;
        padding: 40px;
        text-align: center;
        cursor: pointer;
        transition: all 0.3s ease;
        background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);

        &:hover {
          border-color: #ff5252;
          background: linear-gradient(135deg, #ffe8e8 0%, #fff 100%);
          transform: scale(1.02);
        }

        &.has-image {
          padding: 0;
          border-style: solid;
        }

        .upload-placeholder {
          .el-icon {
            color: #ff6b6b;
            margin-bottom: 15px;
          }

          .upload-text {
            font-size: 16px;
            font-weight: 500;
            color: #303133;
            margin: 10px 0;
          }

          .upload-hint {
            font-size: 13px;
            color: #909399;
          }
        }

        .image-preview {
          position: relative;
          width: 100%;
          height: 300px;
          overflow: hidden;
          border-radius: 8px;

          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }

          .image-overlay {
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0, 0, 0, 0.5);
            display: flex;
            align-items: center;
            justify-content: center;
            opacity: 0;
            transition: opacity 0.3s ease;

            &:hover {
              opacity: 1;
            }
          }
        }
      }
    }

    .recognize-btn {
      width: 100%;
      height: 50px;
      font-size: 16px;
      font-weight: bold;
      margin-bottom: 20px;
      background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
      border: none;
      transition: all 0.3s ease;

      &:hover:not(:disabled) {
        transform: translateY(-2px);
        box-shadow: 0 4px 16px rgba(255, 107, 107, 0.4);
      }

      &:disabled {
        background: #c0c4cc;
      }
    }

    .recognition-result {
      animation: resultFadeIn 0.5s ease-out;

      .result-header {
        text-align: center;
        margin-bottom: 24px;

        h4 {
          font-size: 20px;
          font-weight: bold;
          color: #303133;
          margin: 0;
        }
      }

      .result-cards {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: 15px;

        .result-card {
          background: linear-gradient(135deg, #fff 0%, #fff5f5 100%);
          border: 2px solid #ffcdd2;
          border-radius: 12px;
          padding: 20px;
          transition: all 0.3s ease;

          &:hover {
            transform: translateY(-4px);
            box-shadow: 0 4px 16px rgba(255, 107, 107, 0.2);
            border-color: #ff6b6b;
          }

          &.main-card {
            grid-column: 1 / -1;
            background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
            border: none;

            .card-label {
              color: rgba(255, 255, 255, 0.9);
            }

            .card-value {
              color: #fff;
              font-size: 24px;
              font-weight: bold;
            }
          }

          &.calories-card {
            .card-value.highlight {
              color: #ff6b6b;
              font-size: 28px;
              font-weight: bold;
            }
          }

          &.full-width {
            grid-column: 1 / -1;
          }

          .card-label {
            font-size: 13px;
            color: #909399;
            margin-bottom: 8px;
          }

          .card-value {
            font-size: 16px;
            font-weight: 500;
            color: #303133;

            .ingredient-tag,
            .tag-item {
              margin: 4px;
              padding: 6px 12px;
              font-weight: normal;
            }
          }
        }
      }
    }
  }

  /* Recipe Optimization Section */
  .recipe-section {
    padding: 20px;
    background-color: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);

    .recipe-input {
      margin-bottom: 20px;

      .el-input {
        textarea {
          border-radius: 12px;
          transition: all 0.3s ease;

          &:focus {
            box-shadow: 0 0 0 2px rgba(255, 107, 107, 0.1);
          }
        }
      }
    }

    .optimize-btn {
      width: 100%;
      height: 50px;
      font-size: 16px;
      font-weight: bold;
      margin-bottom: 20px;
      background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
      border: none;
      transition: all 0.3s ease;

      &:hover:not(:disabled) {
        transform: translateY(-2px);
        box-shadow: 0 4px 16px rgba(255, 107, 107, 0.4);
      }

      &:disabled {
        background: #c0c4cc;
      }
    }

    .recipe-result {
      animation: resultFadeIn 0.5s ease-out;

      .result-header {
        text-align: center;
        margin-bottom: 24px;

        h4 {
          font-size: 20px;
          font-weight: bold;
          color: #303133;
          margin: 0;
        }
      }

      .recipe-comparison {
        display: flex;
        align-items: stretch;
        gap: 20px;
        margin-bottom: 30px;

        @media (max-width: 768px) {
          flex-direction: column;
        }

        .recipe-card {
          flex: 1;
          background: #fff;
          border: 2px solid #ffcdd2;
          border-radius: 12px;
          overflow: hidden;
          transition: all 0.3s ease;

          &:hover {
            transform: translateY(-4px);
            box-shadow: 0 4px 16px rgba(255, 107, 107, 0.2);
          }

          .card-header {
            padding: 15px 20px;
            background: linear-gradient(135deg, #fff5f5 0%, #ffe8e8 100%);
            border-bottom: 2px solid #ffcdd2;

            .card-title {
              font-size: 16px;
              font-weight: bold;
              color: #303133;
            }
          }

          .card-content {
            padding: 20px;
            max-height: 400px;
            overflow-y: auto;

            pre {
              margin: 0;
              white-space: pre-wrap;
              word-wrap: break-word;
              font-family: inherit;
              line-height: 1.8;
              color: #606266;
            }
          }

          &.optimized-recipe-card {
            .card-header {
              background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
              border-bottom: none;

              .card-title {
                color: #fff;
              }
            }
          }
        }

        .recipe-arrow {
          display: flex;
          align-items: center;
          font-size: 36px;
          color: #ff6b6b;
          font-weight: bold;

          @media (max-width: 768px) {
            transform: rotate(90deg);
          }
        }
      }

      .improvements-section {
        background: linear-gradient(135deg, #fff 0%, #f0f9ff 100%);
        border: 2px solid #b3e0ff;
        border-radius: 12px;
        padding: 20px;

        .improvements-title {
          font-size: 16px;
          font-weight: bold;
          color: #303133;
          margin-bottom: 15px;
        }

        .improvements-tags {
          display: flex;
          flex-wrap: wrap;
          gap: 10px;

          .el-tag {
            padding: 10px 20px;
            font-size: 14px;
            font-weight: 500;
          }
        }
      }
    }
  }

  /* 动画定义 */
  @keyframes messageFadeIn {
    from {
      opacity: 0;
      transform: translateY(10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  @keyframes resultFadeIn {
    from {
      opacity: 0;
      transform: scale(0.95);
    }
    to {
      opacity: 1;
      transform: scale(1);
    }
  }

  /* 快捷提问样式 */
  .quick-questions {
    background: linear-gradient(135deg, #f0f9ff 0%, #fff 100%);
    border: 2px solid #b3e0ff;
    border-radius: 12px;
    padding: 15px 20px;
    margin-bottom: 20px;

    .quick-questions-title {
      font-size: 14px;
      font-weight: bold;
      color: #303133;
      margin-bottom: 12px;
    }

    .question-tag {
      margin: 5px;
      padding: 8px 16px;
      cursor: pointer;
      transition: all 0.3s ease;
      font-size: 13px;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
        background-color: #409eff;
        color: #fff;
        border-color: #409eff;
      }
    }
  }

  /* 表情选择器样式 */
  .input-toolbar {
    position: relative;
    margin-bottom: 10px;

    .emoji-picker {
      position: absolute;
      top: 100%;
      left: 0;
      background-color: #fff;
      border: 2px solid #e6e8eb;
      border-radius: 12px;
      padding: 10px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
      z-index: 1000;
      display: grid;
      grid-template-columns: repeat(5, 1fr);
      gap: 5px;
      animation: emojiFadeIn 0.2s ease-out;

      .emoji-item {
        font-size: 24px;
        cursor: pointer;
        padding: 5px;
        border-radius: 8px;
        transition: all 0.2s ease;
        text-align: center;

        &:hover {
          background-color: #f0f2f5;
          transform: scale(1.2);
        }
      }
    }
  }

  @keyframes emojiFadeIn {
    from {
      opacity: 0;
      transform: translateY(-10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  /* 菜品识别增强样式 */
  .recognition-section {
    .upload-zone {
      &.is-dragging {
        border-color: #409eff;
        background: linear-gradient(135deg, #e3f2fd 0%, #fff 100%);
        transform: scale(1.02);
      }
    }

    .recognition-progress {
      margin: 20px 0;
      padding: 20px;
      background-color: #f0f9ff;
      border-radius: 12px;

      .progress-text {
        text-align: center;
        margin-top: 10px;
        font-size: 14px;
        color: #409eff;
        font-weight: 500;
      }
    }

    .recognition-buttons {
      display: flex;
      gap: 10px;
      margin-bottom: 20px;

      .recognize-btn,
      .re-recognize-btn {
        flex: 1;
      }
    }

    .nutrition-card {
      .nutrition-chart {
        margin-top: 15px;

        .nutrition-item {
          display: flex;
          align-items: center;
          gap: 15px;
          margin-bottom: 15px;

          &:last-child {
            margin-bottom: 0;
          }

          .nutrition-label {
            flex: 0 0 80px;
            display: flex;
            align-items: center;
            gap: 5px;
            font-size: 13px;
            font-weight: 500;
            color: #606266;

            .nutrition-icon {
              font-size: 18px;
            }
          }

          .nutrition-bar {
            flex: 1;
            height: 24px;
            background-color: #f0f2f5;
            border-radius: 12px;
            overflow: hidden;
            position: relative;

            .nutrition-fill {
              height: 100%;
              border-radius: 12px;
              transition: width 0.6s ease-out;
              position: relative;

              &.protein {
                background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
              }

              &.fat {
                background: linear-gradient(90deg, #f093fb 0%, #f5576c 100%);
              }

              &.carbs {
                background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%);
              }
            }
          }

          .nutrition-value {
            flex: 0 0 50px;
            text-align: right;
            font-size: 14px;
            font-weight: bold;
            color: #303133;
          }
        }
      }
    }
  }
}
</style>
