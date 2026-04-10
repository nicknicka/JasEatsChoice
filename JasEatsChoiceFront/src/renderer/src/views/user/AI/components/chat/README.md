# 聊天组件入口

这里是 AI 聊天的推荐导入入口。

## 统一导出

```js
import { AIChatPanel, ChatMessage, MessageInput, QuickQuestions } from '@/views/user/AI/components/chat'
```

## 说明

这些导出只是为了把当前分散在同一层的聊天组件收拢成一个稳定入口，方便后续继续拆分或替换。

优先通过这里引入，不要直接在新代码里散落地引用上层文件。
