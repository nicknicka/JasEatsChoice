# AI 聊天目录说明

这部分代码已经按职责拆成三层，当前主页面不再直接使用这里的旧聊天面板文件：

## 1. 页面入口

- `src/renderer/src/views/user/AI.vue`：当前主入口，路由直接指向这里。
- `src/renderer/src/views/user/AI/legacy/index.vue`：旧的页面容器，已归档。

## 2. 聊天组件

- `legacy/chat/index.js`：旧聊天组件的统一导出入口。
- `legacy/AIChatPanel.vue`：旧的聊天面板。
- `legacy/ChatMessage.vue`：旧的消息渲染组件。
- `legacy/MessageInput.vue`：旧的底部输入区。
- `legacy/QuickQuestions.vue`：旧的快捷提问面板。

## 3. 归档组件

- `legacy/README.md`：历史文件归档说明。
- `AIChatFull.vue`：当前仍在使用的主聊天实现。
- `legacy/AIChatSimple.vue`、`legacy/QuickActions.vue`：已归档。

## 推荐使用方式

新代码不要再从旧的 `legacy/chat/index.js` 引入。

```js
import AiChatFull from '@/views/user/AI/components/AIChatFull.vue'
```

如果你在整理旧页面，只保留 `AIChatFull`，不要再新增对旧聊天面板的依赖。
