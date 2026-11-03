# AI 聊天目录说明

用户端 AI 页面入口是 `src/renderer/src/views/user/AI.vue`，主聊天实现是 `AIChatFull.vue`，其余为各功能页签组件。

## 组件

- `AIChatFull.vue`：主聊天实现，被 `views/user/AI.vue` 使用。
- `ContentExtractionTab.vue`：内容提取页签。
- `DishRecognition.vue`：菜品识别页签。
- `RecipeOptimization.vue`：食谱优化页签。
- `XhsSessionDialog.vue`：小红书会话弹窗，被 `ContentExtractionTab.vue` 使用。
- `cards/`：消息卡片组件（`UniCard` 系列与各类列表卡片）。
- `cards/elements/`：卡片内的原子元素。

## 推荐使用方式

```js
import AiChatFull from '@/views/user/AI/components/AIChatFull.vue'
```
