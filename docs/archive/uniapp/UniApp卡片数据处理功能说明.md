# UniApp卡片数据处理功能实现说明

## 修改日期
2026-03-30

## 修改背景

参照桌面端（Electron + Vue3）的AI聊天实现，为UniApp前端添加完整的SupervisorAgent卡片数据处理功能。

## 修改内容

### 1. 新增文件

#### ✅ `/src/utils/cardParser.js` - 卡片数据解析工具

参照桌面端 `AIChatFull.vue` 的 `parseCardDataFromContent` 函数实现。

**核心功能**：
- `parseCardDataFromContent(content)` - 从消息内容中提取卡片数据和类型
- `convertToSupportedCardType(cardType)` - 转换为支持的卡片类型
- `parseCardData(cardData)` - 解析卡片数据（JSON字符串或对象）
- `hasCardData(message)` - 检查消息是否包含卡片数据

**支持的卡片类型**：
- `dish_list_card` - 菜品列表卡片
- `order_list_card` - 订单列表卡片
- `favorite_list_card` - 收藏列表卡片
- `review_list_card` - 评价列表卡片
- `coupon_list_card` - 优惠券列表卡片
- `user_info_card` - 用户信息卡片

---

### 2. 修改的文件

#### ✅ `/src/pages/ai/index.vue`

**修改1：导入cardParser工具**
```javascript
// 新增导入（line 235）
import { parseCardDataFromContent, parseCardData, hasCardData } from "@/utils/cardParser";
```

**修改2：onComplete回调 - 添加卡片数据解析**
```javascript
// onComplete - 完成回调（line ~646）
async () => {
    console.log("✅ AI消息接收完成");
    isTyping.value = false;
    isStreaming.value = false;

    // 获取AI回复内容
    const aiContent = messages.value[aiMessageIndex].content;

    // 🔧 解析卡片数据（参照桌面端）
    console.log("🔍 [UniApp] 开始解析卡片数据");
    const { content: cleanContent, cardData, messageType } = parseCardDataFromContent(aiContent);

    // 更新消息对象
    messages.value[aiMessageIndex].content = cleanContent;
    messages.value[aiMessageIndex].messageType = messageType;
    messages.value[aiMessageIndex].cardData = cardData;

    console.log("✅ [UniApp] 卡片数据解析完成:", {
        messageType,
        hasCardData: !!cardData,
        contentLength: cleanContent.length
    });

    // 保存AI消息到后端（包含卡片数据）
    await saveMessageToBackend("ai", cleanContent, messageType, cardData);

    // 保存到本地存储作为备份
    saveChatHistoryToLocal();

    await scrollToBottom();
},
```

**修改3：saveMessageToBackend函数 - 支持卡片数据**
```javascript
// 函数签名修改（line 492）
const saveMessageToBackend = async (sender, content, messageType = null, cardData = null, retryCount = 0) => {
    // ...

    // 构建请求数据
    const requestData = {
        userId,
        sender,
        content,
    };

    // 如果有卡片数据，添加到请求中
    if (messageType) {
        requestData.messageType = messageType;
    }
    if (cardData) {
        requestData.cardData = cardData;
    }

    const response = await aiApi.saveMessage(requestData);
    // ...
}
```

---

### 3. API接口更新

#### ✅ `/src/api/urlEnum.js`

**修改前**：
```javascript
CHAT: '/v1/ai/chat',  // 旧接口
```

**修改后**：
```javascript
CHAT: '/agent/supervisor/chat',  // 统一使用SupervisorAgent
```

**说明**：UniApp和桌面端现在都使用SupervisorAgent接口，只是返回方式不同：
- UniApp：普通POST，一次性返回完整响应
- 桌面端：SSE流式，实时推送执行过程

---

### 4. 后端接口对接

#### ✅ 后端已有的SupervisorAgent接口

**`/agent/supervisor/chat`** - UniApp使用（普通POST）
- 请求：`{ message: string, userId: string }`
- 响应：`{ success: true, code: "200", data: "AI回复内容（含卡片数据）" }`
- 位置：`SupervisorAgentController.java` line 46

**`/agent/supervisor-sse/chat`** - 桌面端使用（SSE流式）
- 请求：`{ message: string, userId: string }`
- 响应：SSE流式事件
- 位置：`SupervisorSSEController.java` line 65

**卡片数据格式**：
```
[CARD_DATA_START]
{
  "type": "dish_list_card",
  "data": { dishes: [...] }
}
[CARD_DATA_END]
```

---

## 消息数据结构

### 消息对象包含的字段

```javascript
{
    id: number,
    sender: 'user' | 'ai',
    content: string,           // 纯文本内容（已移除卡片标记）
    messageType: string | null,  // 消息类型（如 'dish_list_card'）
    cardData: object | null,     // 卡片数据
    time: string,
    avatar: string,
    isUser: boolean
}
```

### 卡片数据示例

**菜品列表卡片**：
```javascript
{
    messageType: 'dish_list_card',
    cardData: {
        dishes: [
            {
                dishId: '1',
                dishName: '西红柿炒鸡蛋',
                imageUrl: 'https://...',
                description: '经典家常菜',
                price: 18.00,
                rating: 4.8,
                category: '家常菜',
                tags: ['推荐', '下饭菜'],
                actions: ['addToCart', 'addToFavorite']
            }
        ]
    }
}
```

---

## 功能对比

| 功能 | UniApp（本次修改后） | 桌面端 | 状态 |
|------|---------------------|--------|------|
| **使用接口** | `/agent/supervisor/chat` | `/agent/supervisor-sse/chat` | ✅ 统一使用SupervisorAgent |
| **返回方式** | 普通POST（一次性） | SSE流式 | ✅ 分别适配 |
| **卡片数据解析** | ✅ 支持 | ✅ 支持 | ✅ 完全一致 |
| **支持的卡片类型** | 6种 | 8种 | ⚠️ UniApp较少 |
| **卡片渲染** | ⚠️ 待实现 | ✅ 已实现 | ⚠️ 需要后续开发 |
| **数据持久化** | ✅ 支持（含卡片数据） | ✅ 支持（含卡片数据） | ✅ 完全一致 |

---

## 待完成功能

### 🔲 高优先级

1. **卡片组件渲染**
   - 创建菜品列表卡片组件
   - 创建订单列表卡片组件
   - 创建收藏列表卡片组件
   - 创建用户信息卡片组件

2. **消息模板更新**
   - 修改 `index.vue` 的消息显示部分
   - 根据 `messageType` 渲染对应的卡片组件
   - 添加卡片点击事件处理

### 🔲 中优先级

3. **加载历史记录时解析卡片**
   - 修改 `loadChatHistory` 函数
   - 从后端加载的消息也需要解析卡片数据

4. **卡片交互功能**
   - 菜品卡片：加入购物车、收藏
   - 订单卡片：查看订单详情
   - 优惠券卡片：领取优惠券

---

## 测试方法

### 1. 测试卡片数据解析

**步骤**：
1. 启动后端服务
2. 打开UniApp AI聊天页面
3. 发送测试消息：
   ```
   推荐一些好吃的菜品
   我的订单有哪些
   我的收藏列表
   ```

**预期结果**：
- 控制台输出：
  ```
  🔍 [UniApp] 开始解析卡片数据
  ✅ [UniApp] 卡片数据解析完成: { messageType: 'dish_list_card', hasCardData: true, ... }
  ```
- 消息对象的 `messageType` 和 `cardData` 字段正确填充

### 2. 测试数据持久化

**步骤**：
1. 发送包含卡片的消息
2. 刷新页面
3. 检查历史记录加载

**预期结果**：
- 后端数据库中的 `ai_chat_history` 表包含：
  - `message_type` 字段：存储卡片类型
  - `card_data` 字段：存储卡片JSON数据

---

## 技术要点

### 1. 卡片数据标记格式

后端SupervisorAgent返回的卡片数据使用特殊标记包裹：
```
这是AI的文本回复内容。

[CARD_DATA_START]
{
  "type": "dish_list_card",
  "data": { dishes: [...] }
}
[CARD_DATA_END]
```

前端解析时：
1. 提取 `[CARD_DATA_START]` 和 `[CARD_DATA_END]` 之间的内容
2. 解析JSON获取卡片数据
3. 移除卡片标记，只保留纯文本内容

### 2. 类型映射

后端返回的卡片类型可能与前端不一致，需要进行类型映射：

| 后端类型 | 前端类型 | 说明 |
|---------|---------|------|
| `recommendation_dish` | `dish_list_card` | 菜品推荐 |
| `dish` | `dish_list_card` | 菜品列表 |
| `orderlist` | `order_list_card` | 订单列表 |
| `favoritelist` | `favorite_list_card` | 收藏列表 |

### 3. 字段映射

部分后端字段名与前端不一致，需要进行映射：

| 后端字段 | 前端字段 | 说明 |
|---------|---------|------|
| `id` | `dishId` | 菜品ID |
| `title` | `dishName` | 菜品名称 |
| `image` | `imageUrl` | 图片URL |
| `highlight` | `description` | 描述信息 |

---

## 注意事项

1. **向后兼容**：
   - 旧的消息（无卡片数据）仍可正常显示
   - `messageType` 和 `cardData` 为可选字段

2. **错误处理**：
   - 卡片数据解析失败时，降级为纯文本显示
   - JSON解析异常不会导致消息丢失

3. **性能优化**：
   - 卡片数据解析在前端同步执行
   - 避免阻塞UI线程

4. **数据一致性**：
   - 前端和后端保持相同的卡片数据格式
   - 定期同步卡片类型定义

---

## 相关文件

- **后端控制器**：
  - `SupervisorAgentController.java` - 普通POST接口
  - `SupervisorSSEController.java` - SSE流式接口
  - `AIChatHistoryController.java` - 历史记录接口

- **前端文件**：
  - `src/utils/cardParser.js` - 卡片解析工具
  - `src/pages/ai/index.vue` - AI聊天页面
  - `src/api/urlEnum.js` - API配置
  - `src/api/modules/ai.js` - AI接口封装

- **桌面端参考**：
  - `JasEatsChoiceFront/src/renderer/src/views/user/AI/components/AIChatFull.vue`

---

## 版本历史

- **v1.0** (2026-03-30) - 初始版本
  - 添加卡片数据解析工具
  - 修改AI聊天页面支持卡片数据
  - 更新API接口配置
  - 修改消息持久化逻辑

---

**修改完成日期**：2026-03-30
**修改人**：Claude Code
**审核状态**：待测试
