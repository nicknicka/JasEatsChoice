# 商家端聊天优化组件使用文档

## 📦 组件列表

### 1. ConversationList - 会话列表组件

会话列表显示组件，支持搜索、筛选未读消息等功能。

**位置**: `src/renderer/src/components/merchant/chat/ConversationList.vue`

#### Props

| 参数                 | 类型    | 默认值 | 说明           |
| -------------------- | ------- | ------ | -------------- |
| conversations        | Array   | []     | 会话列表数据   |
| selectedConversation | Object  | null   | 当前选中的会话 |
| searchKeyword        | String  | ''     | 搜索关键词     |
| showUnreadOnly       | Boolean | false  | 是否仅显示未读 |

#### Events

| 事件名 | 参数                 | 说明               |
| ------ | -------------------- | ------------------ |
| select | conversation: Object | 选择会话时触发     |
| search | keyword: String      | 搜索时触发         |
| filter | filterData: Object   | 筛选条件变化时触发 |

#### 使用示例

```vue
<template>
  <ConversationList
    :conversations="filteredConversations"
    :selected-conversation="selectedConversation"
    :search-keyword="searchKeyword"
    :show-unread-only="showUnreadOnly"
    @select="selectConversation"
  />
</template>

<script setup>
import { ref } from 'vue'

const conversations = ref([
  {
    id: 1,
    type: 'private',
    name: '王小明',
    avatar: '👨',
    lastMessage: '你好',
    time: '12:30',
    unreadCount: 2
  }
])

const selectedConversation = ref(null)
const searchKeyword = ref('')
const showUnreadOnly = ref(false)

const selectConversation = (conversation) => {
  selectedConversation.value = conversation
}
</script>
```

---

### 2. ChatMessageList - 消息列表组件

消息列表显示组件，自动滚动到底部，支持单聊和群聊模式。

**位置**: `src/renderer/src/components/merchant/chat/ChatMessageList.vue`

#### Props

| 参数             | 类型          | 默认值    | 说明                           |
| ---------------- | ------------- | --------- | ------------------------------ |
| messages         | Array         | []        | 消息列表数据                   |
| conversationType | String        | 'private' | 会话类型：'private' 或 'group' |
| currentUserId    | String/Number | null      | 当前用户ID                     |

#### Events

| 事件名           | 参数 | 说明             |
| ---------------- | ---- | ---------------- |
| scroll-to-bottom | -    | 滚动到底部时触发 |

#### 暴露方法

| 方法名         | 参数                   | 返回值 | 说明               |
| -------------- | ---------------------- | ------ | ------------------ |
| scrollToBottom | smooth: Boolean = true | void   | 滚动到消息列表底部 |

#### 使用示例

```vue
<template>
  <ChatMessageList
    ref="messageListRef"
    :messages="chatMessages"
    :conversation-type="conversationType"
    :current-user-id="currentUserId"
  />
</template>

<script setup>
import { ref } from 'vue'
import ChatMessageList from '@/components/merchant/chat/ChatMessageList.vue'

const messageListRef = ref(null)
const chatMessages = ref([
  {
    id: 1,
    sender: 'merchant',
    content: '您好！',
    time: '2024-01-12 12:30',
    isRead: true
  }
])

// 手动滚动到底部
const scrollBottom = () => {
  messageListRef.value?.scrollToBottom()
}
</script>
```

---

### 3. MessageInput - 消息输入组件

消息输入组件，支持快捷键、文件上传、同步到群聊等功能。

**位置**: `src/renderer/src/components/merchant/chat/MessageInput.vue`

#### Props

| 参数           | 类型    | 默认值            | 说明                   |
| -------------- | ------- | ----------------- | ---------------------- |
| disabled       | Boolean | false             | 是否禁用               |
| sending        | Boolean | false             | 是否发送中             |
| placeholder    | String  | '输入消息内容...' | 占位文本               |
| showSyncToggle | Boolean | false             | 是否显示同步至群聊开关 |
| syncToGroup    | Boolean | false             | 是否同步至群聊         |

#### Events

| 事件名             | 参数            | 说明                     |
| ------------------ | --------------- | ------------------------ |
| send               | content: String | 发送消息时触发           |
| update:syncToGroup | value: Boolean  | 同步至群聊状态变化时触发 |
| upload-file        | file: File      | 上传文件时触发           |
| upload-image       | file: File      | 上传图片时触发           |

#### 暴露方法

| 方法名     | 参数 | 返回值 | 说明         |
| ---------- | ---- | ------ | ------------ |
| focus      | -    | void   | 聚焦输入框   |
| clearInput | -    | void   | 清空输入内容 |

#### 快捷键

- **Enter**: 发送消息
- **Shift + Enter**: 换行

#### 使用示例

```vue
<template>
  <MessageInput
    ref="inputRef"
    :disabled="!selectedConversation"
    :sending="sending"
    :show-sync-toggle="selectedConversation?.type === 'private'"
    :sync-to-group="syncToGroup"
    @update:sync-to-group="syncToGroup = $event"
    @send="sendMessage"
    @upload-file="handleUploadFile"
    @upload-image="handleUploadImage"
  />
</template>

<script setup>
import { ref } from 'vue'
import MessageInput from '@/components/merchant/chat/MessageInput.vue'

const inputRef = ref(null)
const sending = ref(false)
const syncToGroup = ref(false)
const selectedConversation = ref(null)

const sendMessage = (content) => {
  sending.value = true
  // 发送消息逻辑
  setTimeout(() => {
    sending.value = false
  }, 1000)
}

const handleUploadFile = (file) => {
  console.log('上传文件:', file)
}

const handleUploadImage = (file) => {
  console.log('上传图片:', file)
}
</script>
```

---

### 4. OrderInfoPanel - 订单信息面板组件

订单信息展示组件，支持摘要和详情两种显示模式，可以查看订单详情、更新订单状态、发送订单提醒。

**位置**: `src/renderer/src/components/merchant/chat/OrderInfoPanel.vue`

#### Props

| 参数  | 类型   | 默认值 | 说明         |
| ----- | ------ | ------ | ------------ |
| order | Object | null   | 订单数据对象 |

#### Events

| 事件名        | 参数                 | 说明               |
| ------------- | -------------------- | ------------------ |
| status-update | { orderId, status }  | 订单状态更新时触发 |
| send-reminder | { orderId, content } | 发送订单提醒时触发 |

#### 订单数据格式

```javascript
{
  orderId: 'JD20241121001',
  items: [
    { name: '麻辣香锅饭', quantity: 2, price: 35 }
  ],
  status: 3, // 1:待接单 2:备菜中 3:烹饪中 4:待上菜 5:已完成
  totalAmount: 98,
  address: '北京市朝阳区望京SOHO T1',
  phone: '138****8888'
}
```

#### 使用示例

```vue
<template>
  <OrderInfoPanel
    :order="selectedConversation?.relatedOrder"
    @status-update="handleStatusUpdate"
    @send-reminder="handleSendReminder"
  />
</template>

<script setup>
import { ref } from 'vue'
import OrderInfoPanel from '@/components/merchant/chat/OrderInfoPanel.vue'

const selectedConversation = ref({
  relatedOrder: {
    orderId: 'JD20241121001',
    items: [{ name: '麻辣香锅饭', quantity: 2, price: 35 }],
    status: 3,
    totalAmount: 98,
    address: '北京市朝阳区望京SOHO T1',
    phone: '138****8888'
  }
})

const handleStatusUpdate = ({ orderId, status }) => {
  console.log('更新订单状态:', orderId, status)
  // 调用API更新订单状态
}

const handleSendReminder = ({ orderId, content }) => {
  console.log('发送订单提醒:', orderId, content)
  // 发送订单提醒消息
}
</script>
```

---

## 🛠️ 工具函数

### chat/messageUtils - 消息工具函数

**位置**: `src/renderer/src/utils/chat/messageUtils.js`

#### 函数列表

```javascript
import {
  sanitizeMessage, // XSS防护
  filterSensitiveInfo, // 过滤敏感信息
  formatMessageTime, // 格式化消息时间
  formatConversationTime, // 格式化会话时间
  truncateMessage, // 截断消息
  cleanMessage, // 清理消息（XSS+敏感信息过滤）
  generateMessageId, // 生成唯一消息ID
  isImageMessage, // 判断是否为图片消息
  isFileMessage, // 判断是否为文件消息
  calculateUnreadCount, // 计算未读消息数量
  sortConversationsByUnread // 按未读排序会话
} from '@/utils/chat/messageUtils'
```

#### 使用示例

```javascript
import { cleanMessage, formatMessageTime } from '@/utils/chat/messageUtils'

// 清理消息内容（XSS防护 + 敏感信息过滤）
const content = cleanMessage(userInput)

// 格式化时间
const time = formatMessageTime('2024-01-12 12:30:00')
// 输出: "12:30" 或 "昨天 12:30" 或 "2024-01-12 12:30"
```

---

### chat/chatApi - 聊天API工具函数

**位置**: `src/renderer/src/utils/chat/chatApi.js`

#### 函数列表

```javascript
import {
  getCurrentUserId, // 获取当前用户ID
  handleAuthError, // 处理认证错误
  handleApiError, // 处理API错误
  getChatSessions, // 获取会话列表
  getChatMessages, // 获取聊天记录
  sendMessage, // 发送消息
  markMessagesAsRead, // 标记消息已读
  buildSessionId, // 构建会话ID
  formatMessageForSend, // 格式化消息用于发送
  createLocalMessage // 创建本地消息对象
} from '@/utils/chat/chatApi'
```

#### 使用示例

```javascript
import { getChatSessions, sendMessage, buildSessionId } from '@/utils/chat/chatApi'

// 获取会话列表
const sessions = await getChatSessions(userId)

// 发送消息
const result = await sendMessage({
  fromId: '1',
  toId: '2',
  content: '你好',
  msgType: 'private'
})

// 构建会话ID
const sessionId = buildSessionId('1', '2', 'private')
// 输出: "1_2"
```

---

## 📝 完整示例

### 使用公共组件重构聊天页面

```vue
<script setup>
import { ref, onMounted } from 'vue'
import ConversationList from '@/components/merchant/chat/ConversationList.vue'
import ChatMessageList from '@/components/merchant/chat/ChatMessageList.vue'
import MessageInput from '@/components/merchant/chat/MessageInput.vue'
import OrderInfoPanel from '@/components/merchant/chat/OrderInfoPanel.vue'
import {
  getChatSessions,
  getChatMessages,
  sendMessage as sendChatMessage
} from '@/utils/chat/chatApi'
import { sortConversationsByUnread, cleanMessage } from '@/utils/chat/messageUtils'

// 数据定义
const conversations = ref([])
const chatMessages = ref([])
const selectedConversation = ref(null)
const searchKeyword = ref('')
const sending = ref(false)
const currentUserId = ref('')

// 初始化
onMounted(async () => {
  // 获取会话列表
  const sessions = await getChatSessions(currentUserId.value)
  conversations.value = sortConversationsByUnread(sessions)

  // 默认选中第一个会话
  if (sessions.length > 0) {
    await selectConversation(sessions[0])
  }
})

// 选择会话
const selectConversation = async (conversation) => {
  selectedConversation.value = conversation

  // 清空未读
  if (conversation.unreadCount > 0) {
    conversation.unreadCount = 0
  }

  // 加载消息
  const messages = await getChatMessages(conversation.id, currentUserId.value)
  chatMessages.value = messages
}

// 发送消息
const sendMessage = async (content) => {
  if (!selectedConversation.value) return

  sending.value = true

  // 清理消息内容
  const cleanedContent = cleanMessage(content)

  // 发送消息
  const result = await sendChatMessage({
    fromId: currentUserId.value,
    toId: selectedConversation.value.id,
    content: cleanedContent,
    msgType: selectedConversation.value.type === 'group' ? 'group' : 'private'
  })

  if (result.success) {
    // 添加到聊天记录
    chatMessages.value.push({
      id: Date.now(),
      sender: 'merchant',
      content: cleanedContent,
      time: new Date().toISOString(),
      isRead: true
    })
  }

  sending.value = false
}
</script>

<template>
  <div class="chat-container">
    <!-- 会话列表 -->
    <ConversationList
      :conversations="conversations"
      :selected-conversation="selectedConversation"
      :search-keyword="searchKeyword"
      @select="selectConversation"
    />

    <!-- 聊天区域 -->
    <div v-if="selectedConversation" class="chat-area">
      <!-- 订单信息 -->
      <OrderInfoPanel
        v-if="selectedConversation.relatedOrder"
        :order="selectedConversation.relatedOrder"
      />

      <!-- 消息列表 -->
      <ChatMessageList :messages="chatMessages" :conversation-type="selectedConversation.type" />

      <!-- 消息输入 -->
      <MessageInput :sending="sending" @send="sendMessage" />
    </div>
  </div>
</template>
```

---

## ⚡ 性能优化建议

### 1. 虚拟滚动

当消息数量超过100条时，建议使用虚拟滚动：

```vue
<template>
  <el-table :data="chatMessages" height="400" virtual-scrolling>
    <!-- 消息项 -->
  </el-table>
</template>
```

### 2. 防抖搜索

搜索输入建议添加防抖：

```javascript
import { debounce } from 'lodash-es'

const handleSearch = debounce((keyword) => {
  searchKeyword.value = keyword
}, 300)
```

### 3. 图片懒加载

头像图片添加懒加载：

```vue
<img :src="avatar" loading="lazy" />
```

---

## 🔒 安全性建议

### 1. 始终使用 cleanMessage 清理用户输入

```javascript
import { cleanMessage } from '@/utils/chat/messageUtils'

const content = cleanMessage(userInput)
```

### 2. 验证文件类型

```javascript
const handleImageUpload = (file) => {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  // 上传逻辑
}
```

---

## 📞 技术支持

如有问题，请联系开发团队或查看项目文档。
