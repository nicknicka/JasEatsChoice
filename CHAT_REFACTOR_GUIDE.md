# Chat.vue 重构指南

## 问题分析

原有的 [Chat.vue](JasEatsChoiceFront/src/renderer/src/views/user/Chat.vue) 文件存在以下问题：

1. **代码量过大**：超过 4000 行，违反单一职责原则
2. **语法错误**：第 705、1197 行存在 `merchant - name` 应为 `merchant.name` 的错误
3. **缺少常量管理**：魔法数字遍布代码（30000、1000、50 等）
4. **功能耦合严重**：聊天、WebSocket、群订单、好友管理等功能混在一起
5. **缺少性能优化**：搜索没有防抖、长列表没有虚拟滚动
6. **可维护性差**：缺少类型定义、注释和单元测试

## 重构架构

### 新文件结构

```
JasEatsChoiceFront/src/renderer/src/
├── constants/
│   └── chatConstants.js              # 常量配置（已创建）
├── composables/
│   ├── useWebSocketChat.js           # WebSocket 连接管理（已创建）
│   ├── useChatMessages.js            # 消息管理（已创建）
│   ├── useMessageActions.js          # 消息操作（已创建）
│   └── useConversations.js           # 会话管理（已创建）
├── components/
│   └── chat/
│       ├── ChatMessageList.vue       # 消息列表组件（已创建）
│       └── ConversationList.vue      # 会话列表组件（已创建）
└── views/
    └── user/
        └── Chat.vue                  # 主组件（需要重构）
```

### 重构后的优势

1. **代码量减少**：主组件从 4000+ 行减少到约 500 行
2. **职责分离**：每个 composable 负责单一功能
3. **可复用性**：composables 可在其他组件中复用
4. **可测试性**：每个模块可独立测试
5. **性能提升**：搜索防抖、常量缓存等优化

## 使用方法

### 1. 常量配置

```javascript
import { WEBSOCKET_CONFIG, MESSAGE_CONFIG } from '@/constants/chatConstants'

// 使用配置
const heartbeatInterval = WEBSOCKET_CONFIG.HEARTBEAT_INTERVAL
const searchDelay = MESSAGE_CONFIG.SEARCH_DEBOUNCE_DELAY
```

### 2. WebSocket 连接

```javascript
import { useWebSocketChat } from '@/composables/useWebSocketChat'

const {
  isConnected,
  initWebSocket,
  closeWebSocket,
  sendMessage
} = useWebSocketChat({
  userId,
  token,
  onMessage: (data) => {
    console.log('收到消息:', data)
  }
})
```

### 3. 消息管理

```javascript
import { useChatMessages } from '@/composables/useChatMessages'

const {
  chatMessages,
  loadChatMessages,
  loadMoreMessages,
  addMessage,
  formatMessageTime
} = useChatMessages({ userId, selectedConversation })
```

### 4. 消息操作

```javascript
import { useMessageActions } from '@/composables/useMessageActions'

const {
  searchKeyword,
  replyingTo,
  searchMessages,
  exportChatHistory,
  handleMessageCommand
} = useMessageActions({
  chatHistory,
  chatMessages,
  userId,
  formatMessageTime
})
```

### 5. 会话管理

```javascript
import { useConversations } from '@/composables/useConversations'

const {
  conversations,
  selectedConversation,
  sortedConversations,
  selectConversation,
  togglePin,
  deleteConversation
} = useConversations()
```

### 6. 子组件使用

```vue
<template>
  <!-- 会话列表 -->
  <ConversationList
    :conversations="sortedConversations"
    v-model="selectedConversation"
    @select="selectConversation"
    @toggle-pin="togglePin"
  />

  <!-- 消息列表 -->
  <ChatMessageList
    :messages="chatMessages"
    :current-user-id="userId"
    :can-recall="canRecallMessage"
    @load-more="loadMoreMessages"
    @command="handleMessageCommand"
  />
</template>

<script setup>
import ConversationList from '@/components/chat/ConversationList.vue'
import ChatMessageList from '@/components/chat/ChatMessageList.vue'
</script>
```

## 已修复的错误

### 语法错误

- **修复位置 1**：[Chat.vue:705](JasEatsChoiceFront/src/renderer/src/views/user/Chat.vue#L705)
  ```javascript
  // 修复前
  {{ merchant - name }}
  // 修复后
  {{ merchant.name }}
  ```

- **修复位置 2**：[Chat.vue:1197](JasEatsChoiceFront/src/renderer/src/views/user/Chat.vue#L1197)
  ```javascript
  // 修复前
  currentOrder.merchantName = merchant - name
  // 修复后
  currentOrder.merchantName = merchant.name
  ```

## 下一步建议

### 1. 继续拆分组件

将以下功能继续拆分为独立组件：

- `GroupOrderPanel.vue` - 群订单面板
- `MerchantSelector.vue` - 商家选择器
- `ProductSelector.vue` - 商品选择器
- `FriendSelector.vue` - 好友选择器
- `MessageInput.vue` - 消息输入框

### 2. 添加 TypeScript

为所有 composables 和组件添加类型定义：

```typescript
// types/chat.ts
export interface Message {
  id: string | number
  fromId: string
  toId: string
  content: string
  createTime: string
  status?: MessageStatus
}

export interface Conversation {
  id: string | number
  name: string
  type: ConversationType
  lastMessage?: string
  time: string
  unreadCount?: number
  pinned?: boolean
}
```

### 3. 性能优化

- 消息列表使用虚拟滚动
- 添加消息懒加载图片
- 优化大量数据渲染

### 4. 测试

- 为 composables 添加单元测试
- 为组件添加集成测试
- 添加 E2E 测试

### 5. 状态管理

考虑使用 Pinia 管理全局状态：

```javascript
// stores/chat.js
import { defineStore } from 'pinia'

export const useChatStore = defineStore('chat', {
  state: () => ({
    conversations: [],
    currentConversation: null,
    chatHistory: {}
  }),
  actions: {
    // ...
  }
})
```

## 迁移检查清单

- [x] 修复语法错误
- [x] 创建常量配置文件
- [x] 创建 WebSocket composable
- [x] 创建消息管理 composable
- [x] 创建消息操作 composable
- [x] 创建会话管理 composable
- [x] 创建消息列表组件
- [x] 创建会话列表组件
- [ ] 重构主组件
- [ ] 添加 TypeScript 支持
- [ ] 添加单元测试
- [ ] 添加性能优化（虚拟滚动）
- [ ] 拆分群订单组件
- [ ] 拆分商家/商品选择组件

## 相关文档

- [佳食宜选技术实现指导.md](佳食宜选技术实现指导.md)
- [产品需求说明书（PRD）.md](产品需求说明书（PRD）.md)
- [用户端首页效果图.md](用户端首页效果图.md)
- [AI聊天助手页面设计.md](AI聊天助手页面设计.md)
