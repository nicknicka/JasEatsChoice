# AI 聊天目录说明

这部分代码已经按职责拆成三层：

## 1. 页面入口

- `src/renderer/src/views/user/AI/index.vue`：当前主入口，路由直接指向这里。
- `src/renderer/src/views/user/AI.vue`：兼容入口，只负责转发到 `index.vue`。

## 2. 聊天组件

- `components/chat/index.js`：聊天组件的统一导出入口。
- `AIChatPanel.vue`：当前正在使用的聊天面板。
- `ChatMessage.vue`：消息渲染组件。
- `MessageInput.vue`：底部输入区。
- `QuickQuestions.vue`：快捷提问面板。

## 3. 归档组件

- `legacy/README.md`：历史文件归档说明。
- `AIChatFull.vue`、`AIChatSimple.vue`、`QuickActions.vue`：旧版实现，目前仍保留在当前目录，后续如继续整理可再整体迁移到 `legacy/`。

## 推荐使用方式

新代码优先从 `components/chat/index.js` 引入。

```js
import { AIChatPanel } from '@/views/user/AI/components/chat'
```

如果你在整理旧页面，只保留 `AIChatPanel`，不要再新增对 `AIChatFull` 或 `AIChatSimple` 的依赖。
