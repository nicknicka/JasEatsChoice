# UniApp 代码重构实施计划

**项目名称**：佳食宜选 UniApp 端代码重构
**生成时间**：2026-03-20
**预计周期**：4周
**涉及文件**：20个页面，约23,000行代码

---

## 📋 目录

- [一、重构总体策略](#一重构总体策略)
- [二、模块一：聊天通讯模块](#二模块一聊天通讯模块)
- [三、模块二：订单交易模块](#三模块二订单交易模块)
- [四、模块三：商家管理模块](#四模块三商家管理模块)
- [五、模块四：菜品管理模块](#五模块四菜品管理模块)
- [六、模块五：AI智能模块](#六模块五ai智能模块)
- [七、模块六：其他功能模块](#七模块六其他功能模块)
- [八、实施进度表](#八实施进度表)
- [九、检查清单](#九检查清单)

---

## 一、重构总体策略

### 1.1 重构目标

- ✅ **代码可维护性**：单个文件控制在500行以内
- ✅ **组件复用性**：提取公共组件，复用率提升40%
- ✅ **逻辑清晰度**：使用 Composables 分离业务逻辑
- ✅ **性能优化**：按需加载，减少首屏渲染时间

### 1.2 技术方案

**组件拆分策略**：
```
src/
├── components/           # 公共组件库
│   ├── common/          # 通用组件
│   ├── business/        # 业务组件
│   └── layout/          # 布局组件
├── composables/         # 逻辑复用
│   ├── useChat.ts
│   ├── useOrder.ts
│   └── ...
└── pages/              # 页面（轻量化）
```

**代码组织规范**：
```vue
<template>
  <!-- 简洁的模板，使用子组件 -->
  <ComponentA />
  <ComponentB />
</template>

<script setup lang="ts">
// 使用 composables 管理逻辑
const { data, methods } = usePageLogic()
</script>

<style scoped>
/* 仅保留页面特定样式 */
</style>
```

### 1.3 重构原则

1. **渐进式重构**：逐个模块进行，不中断功能
2. **测试先行**：重构前确保有测试覆盖
3. **向后兼容**：保持API接口不变
4. **文档同步**：更新组件文档和使用说明

---

## 二、模块一：聊天通讯模块

### 📊 模块概览

| 统计项 | 数量 |
|--------|------|
| 涉及页面 | 3个 |
| 总代码行数 | 3,707行 |
| 需拆分组件 | 18个 |
| 需创建Composables | 6个 |
| 预计工时 | 5天 |

### 📄 页面清单

#### 2.1 群聊页面（最高优先级）

**文件路径**：`src/pages-common/chat/group-chat.vue`
**当前代码量**：1,381行
**目标代码量**：< 500行
**问题严重程度**：🔥🔥 极高（74个函数）

**当前结构分析**：
```
group-chat.vue (1381行)
├── <template>    235行  ⚠️ 可优化
├── <script>      719行  ❌ 严重过长
└── <style>       425行  ⚠️ 可优化
```

**详细拆分方案**：

| 步骤 | 拆分内容 | 文件名 | 预估行数 | 优先级 |
|------|----------|--------|----------|--------|
| 1 | 群聊头部信息栏 | `GroupChatHeader.vue` | 120行 | P0 |
| 2 | 消息列表容器 | `MessageList.vue` | 150行 | P0 |
| 3 | 单条消息气泡 | `MessageBubble.vue` | 180行 | P0 |
| 4 | 消息输入框 | `MessageInput.vue` | 200行 | P0 |
| 5 | 群成员列表 | `GroupMemberList.vue` | 150行 | P1 |
| 6 | 群成员卡片 | `MemberCard.vue` | 80行 | P1 |
| 7 | 消息引用组件 | `MessageQuote.vue` | 60行 | P2 |
| 8 | 快捷回复面板 | `QuickReply.vue` | 100行 | P2 |

**Composables拆分**：

| Composable | 职责 | 主要方法 |
|------------|------|----------|
| `useGroupChat.ts` | 群聊核心逻辑 | `sendMessage()`, `loadHistory()`, `handleScroll()` |
| `useMessage.ts` | 消息数据处理 | `parseMessage()`, `formatTime()`, `checkStatus()` |
| `useGroupMember.ts` | 成员管理 | `getMembers()`, `addMember()`, `removeMember()` |
| `useMessageInput.ts` | 输入框逻辑 | `handleInput()`, `sendImage()`, `sendQuote()` |

**实施步骤**：

```bash
# 第1步：创建组件目录
mkdir -p src/components/chat
mkdir -p src/composables/chat

# 第2步：提取组件（按顺序）
touch src/components/chat/GroupChatHeader.vue
touch src/components/chat/MessageList.vue
touch src/components/chat/MessageBubble.vue
touch src/components/chat/MessageInput.vue

# 第3步：提取逻辑
touch src/composables/chat/useGroupChat.ts
touch src/composables/chat/useMessage.ts

# 第4步：重构主页面
# 将原1381行缩减至约300行
```

**重构后主页面结构**：
```vue
<template>
  <view class="group-chat">
    <GroupChatHeader :group="group" @-back="handleBack" />
    <MessageList :messages="messages" @load-more="loadMore" />
    <MessageInput @send="sendMessage" />
  </view>
</template>

<script setup lang="ts">
import { useGroupChat } from '@/composables/chat/useGroupChat'

const {
  group,
  messages,
  handleBack,
  loadMore,
  sendMessage
} = useGroupChat()
</script>

<style scoped lang="scss">
.group-chat {
  height: 100vh;
  display: flex;
  flex-direction: column;
}
</style>
```

**预期收益**：
- ✅ 主页面从1381行降至300行（-78%）
- ✅ 组件可在其他聊天页面复用
- ✅ 逻辑清晰，易于测试和维护

---

#### 2.2 聊天室页面（最高优先级）

**文件路径**：`src/pages-common/chat/chat-room.vue`
**当前代码量**：1,208行
**目标代码量**：< 500行
**问题严重程度**：🔥🔥 极高（70个函数）

**当前结构分析**：
```
chat-room.vue (1208行)
├── <template>    199行  ✅ 合理
├── <script>      670行  ❌ 严重过长
└── <style>       337行  ⚠️ 可优化
```

**详细拆分方案**：

| 步骤 | 拆分内容 | 文件名 | 预估行数 | 优先级 |
|------|----------|--------|----------|--------|
| 1 | 聊天室头部 | `ChatRoomHeader.vue` | 100行 | P0 |
| 2 | 消息展示区 | `ChatMessageArea.vue` | 150行 | P0 |
| 3 | 消息气泡（复用群聊） | `MessageBubble.vue` | - | 已有 |
| 4 | 输入工具栏 | `ChatInputToolbar.vue` | 120行 | P0 |
| 5 | 表情选择器 | `EmojiPicker.vue` | 200行 | P1 |
| 6 | 图片预览器 | `ImagePreview.vue` | 150行 | P1 |
| 7 | 更多操作面板 | `MoreActionsPanel.vue` | 180行 | P2 |

**Composables拆分**：

| Composable | 职责 | 主要方法 |
|------------|------|----------|
| `useChatRoom.ts` | 聊天室核心 | `connect()`, `disconnect()`, `sendMessage()` |
| `useWebSocket.ts` | WebSocket管理 | `connect()`, `onMessage()`, `send()` |
| `useEmoji.ts` | 表情处理 | `getEmojis()`, `selectEmoji()` |
| `useImagePreview.ts` | 图片预览 | `preview()`, `close()` |

**实施步骤**：

```bash
# 第1步：创建组件（部分可复用群聊组件）
touch src/components/chat/ChatRoomHeader.vue
touch src/components/chat/ChatInputToolbar.vue
touch src/components/chat/EmojiPicker.vue

# 第2步：提取WebSocket逻辑（通用）
touch src/composables/chat/useWebSocket.ts

# 第3步：重构主页面
```

**预期收益**：
- ✅ 主页面从1208行降至350行（-71%）
- ✅ WebSocket逻辑可复用
- ✅ 表情选择器可用于其他输入场景

---

#### 2.3 群详情页面

**文件路径**：`src/pages-common/chat/group-detail.vue`
**当前代码量**：1,118行
**目标代码量**：< 400行
**问题严重程度**：🔥 高

**详细拆分方案**：

| 步骤 | 拆分内容 | 文件名 | 预估行数 | 优先级 |
|------|----------|--------|----------|--------|
| 1 | 群基本信息 | `GroupBasicInfo.vue` | 150行 | P0 |
| 2 | 成员列表（复用） | `GroupMemberList.vue` | - | 已有 |
| 3 | 群公告编辑 | `GroupNoticeEditor.vue` | 120行 | P0 |
| 4 | 群设置项 | `GroupSettings.vue` | 180行 | P1 |
| 5 | 成员管理 | `MemberManagement.vue` | 200行 | P1 |
| 6 | 转让群主 | `TransferOwner.vue` | 150行 | P2 |
| 7 | 解散群组 | `DisbandGroup.vue` | 100行 | P2 |

**Composables拆分**：

| Composable | 职责 | 主要方法 |
|------------|------|----------|
| `useGroupDetail.ts` | 群详情管理 | `fetchDetail()`, `updateInfo()`, `uploadNotice()` |
| `useGroupSettings.ts` | 群设置管理 | `updateSettings()`, `muteAll()`, `allowInvite()` |

**预期收益**：
- ✅ 主页面从1118行降至280行（-75%）
- ✅ 复用已创建的群成员列表组件

---

### 📦 模块一：聊天组件清单

**新建组件（18个）**：

```
src/components/chat/
├── GroupChatHeader.vue          # 群聊头部
├── ChatRoomHeader.vue           # 聊天室头部
├── MessageList.vue              # 消息列表
├── MessageBubble.vue            # 消息气泡（通用）
├── MessageInput.vue             # 消息输入框
├── ChatInputToolbar.vue         # 聊天工具栏
├── GroupMemberList.vue          # 群成员列表
├── MemberCard.vue               # 成员卡片
├── MessageQuote.vue             # 消息引用
├── QuickReply.vue               # 快捷回复
├── EmojiPicker.vue              # 表情选择器
├── ImagePreview.vue             # 图片预览
├── MoreActionsPanel.vue         # 更多操作
├── GroupBasicInfo.vue           # 群基本信息
├── GroupNoticeEditor.vue        # 群公告编辑
├── GroupSettings.vue            # 群设置
├── MemberManagement.vue         # 成员管理
└── TransferOwner.vue            # 转让群主
```

**新建Composables（6个）**：

```
src/composables/chat/
├── useGroupChat.ts              # 群聊逻辑
├── useChatRoom.ts               # 聊天室逻辑
├── useMessage.ts                # 消息处理
├── useGroupMember.ts            # 成员管理
├── useWebSocket.ts              # WebSocket管理
└── useGroupSettings.ts          # 群设置管理
```

---

## 三、模块二：订单交易模块

### 📊 模块概览

| 统计项 | 数量 |
|--------|------|
| 涉及页面 | 4个 |
| 总代码行数 | 4,464行 |
| 需拆分组件 | 16个 |
| 需创建Composables | 5个 |
| 预计工时 | 4天 |

### 📄 页面清单

#### 3.1 支付页面（高优先级）

**文件路径**：`src/pages-common/payment/index.vue`
**当前代码量**：1,318行
**目标代码量**：< 400行
**问题严重程度**：🔥 高（66个函数）

**详细拆分方案**：

| 步骤 | 拆分内容 | 文件名 | 预估行数 | 优先级 |
|------|----------|--------|----------|--------|
| 1 | 订单信息卡片 | `OrderInfoCard.vue` | 120行 | P0 |
| 2 | 支付方式选择器 | `PaymentMethodSelector.vue` | 200行 | P0 |
| 3 | 支付方式单项 | `PaymentMethodItem.vue` | 80行 | P0 |
| 4 | 支付详情 | `PaymentDetail.vue` | 100行 | P0 |
| 5 | 支付密码输入 | `PaymentPassword.vue` | 150行 | P0 |
| 6 | 优惠券选择 | `CouponSelector.vue` | 180行 | P1 |

**Composables拆分**：

| Composable | 职责 | 主要方法 |
|------------|------|----------|
| `usePayment.ts` | 支付核心逻辑 | `pay()`, `checkBalance()`, `verifyPassword()` |
| `usePaymentMethod.ts` | 支付方式管理 | `selectMethod()`, `getMethods()` |

**实施后主页面**：
```vue
<template>
  <view class="payment-page">
    <OrderInfoCard :order="order" />
    <PaymentMethodSelector v-model="selectedMethod" />
    <PaymentDetail :order="order" :method="selectedMethod" />
    <PaymentPassword @confirm="handlePay" />
  </view>
</template>

<script setup lang="ts">
import { usePayment } from '@/composables/payment/usePayment'

const { order, selectedMethod, handlePay } = usePayment()
</script>
```

---

#### 3.2 订单详情页面

**文件路径**：`src/pages-user/order/detail/index.vue`
**当前代码量**：1,234行
**目标代码量**：< 400行

**详细拆分方案**：

| 步骤 | 拆分内容 | 文件名 | 预估行数 | 优先级 |
|------|----------|--------|----------|--------|
| 1 | 订单状态展示 | `OrderStatus.vue` | 150行 | P0 |
| 2 | 订单基本信息 | `OrderBasicInfo.vue` | 120行 | P0 |
| 3 | 菜品列表 | `OrderDishList.vue` | 140行 | P0 |
| 4 | 菜品项卡片 | `OrderDishCard.vue` | 80行 | P1 |
| 5 | 订单时间线 | `OrderTimeline.vue` | 180行 | P1 |
| 6 | 订单操作栏 | `OrderActions.vue` | 150行 | P0 |

**Composables拆分**：

| Composable | 职责 | 主要方法 |
|------------|------|----------|
| `useOrderDetail.ts` | 订单详情管理 | `fetchDetail()`, `cancelOrder()`, `refund()` |
| `useOrderTimeline.ts` | 时间线逻辑 | `getTimeline()`, `formatStatus()` |

---

#### 3.3 订单进度页面

**文件路径**：`src/pages-user/order/progress/index.vue`
**当前代码量**：1,221行
**目标代码量**：< 350行

**详细拆分方案**：

| 步骤 | 拆分内容 | 文件名 | 预估行数 | 优先级 |
|------|----------|--------|----------|--------|
| 1 | 配送地图 | `DeliveryMap.vue` | 200行 | P0 |
| 2 | 进度时间线（复用） | `OrderTimeline.vue` | - | 已有 |
| 3 | 骑手信息卡片 | `RiderInfoCard.vue` | 120行 | P0 |
| 4 | 配送状态卡片 | `DeliveryStatusCard.vue` | 100行 | P1 |
| 5 | 联系骑手弹窗 | `ContactRiderDialog.vue` | 80行 | P2 |

**Composables拆分**：

| Composable | 职责 | 主要方法 |
|------------|------|----------|
| `useOrderProgress.ts` | 订单进度管理 | `fetchProgress()`, `trackRider()`, `contactRider()` |

---

#### 3.4 订单确认页面

**文件路径**：`src/pages-user/order/confirm/index.vue`
**当前代码量**：991行
**目标代码量**：< 350行

**详细拆分方案**：

| 步骤 | 拆分内容 | 文件名 | 预估行数 | 优先级 |
|------|----------|--------|----------|--------|
| 1 | 地址选择器 | `AddressSelector.vue` | 180行 | P0 |
| 2 | 地址卡片 | `AddressCard.vue` | 80行 | P1 |
| 3 | 订单摘要 | `OrderSummary.vue` | 150行 | P0 |
| 4 | 配送时间选择 | `DeliveryTimeSelector.vue` | 140行 | P0 |
| 5 | 备注输入 | `OrderRemark.vue` | 100行 | P1 |
| 6 | 优惠选择 | `DiscountSelector.vue` | 120行 | P2 |

**Composables拆分**：

| Composable | 职责 | 主要方法 |
|------------|------|----------|
| `useOrderConfirm.ts` | 订单确认逻辑 | `validateOrder()`, `calculatePrice()`, `submitOrder()` |

---

### 📦 模块二：订单组件清单

**新建组件（16个）**：

```
src/components/order/
├── OrderInfoCard.vue            # 订单信息卡片
├── PaymentMethodSelector.vue    # 支付方式选择器
├── PaymentMethodItem.vue        # 支付方式单项
├── PaymentDetail.vue            # 支付详情
├── PaymentPassword.vue          # 支付密码
├── CouponSelector.vue           # 优惠券选择
├── OrderStatus.vue              # 订单状态
├── OrderBasicInfo.vue           # 订单基本信息
├── OrderDishList.vue            # 菜品列表
├── OrderDishCard.vue            # 菜品卡片
├── OrderTimeline.vue            # 订单时间线
├── OrderActions.vue             # 订单操作
├── DeliveryMap.vue              # 配送地图
├── RiderInfoCard.vue            # 骑手信息
├── DeliveryStatusCard.vue       # 配送状态
└── ContactRiderDialog.vue       # 联系骑手弹窗
```

**新建Composables（5个）**：

```
src/composables/order/
├── usePayment.ts                # 支付逻辑
├── usePaymentMethod.ts          # 支付方式
├── useOrderDetail.ts            # 订单详情
├── useOrderProgress.ts          # 订单进度
└── useOrderConfirm.ts           # 订单确认
```

---

## 四、模块三：商家管理模块

### 📊 模块概览

| 统计项 | 数量 |
|--------|------|
| 涉及页面 | 1个 |
| 总代码行数 | 1,469行 |
| 需拆分组件 | 8个 |
| 需创建Composables | 3个 |
| 预计工时 | 3天 |

### 📄 页面清单

#### 4.1 商家详情页面（高优先级）

**文件路径**：`src/pages-user/merchant/detail/index.vue`
**当前代码量**：1,469行
**目标代码量**：< 400行

**详细拆分方案**：

| 步骤 | 拆分内容 | 文件名 | 预估行数 | 优先级 |
|------|----------|--------|----------|--------|
| 1 | 商家头部卡片 | `MerchantHeader.vue` | 180行 | P0 |
| 2 | 商家统计数据 | `MerchantStats.vue` | 100行 | P0 |
| 3 | 优惠券区域 | `CouponSection.vue` | 150行 | P0 |
| 4 | 优惠券卡片 | `CouponCard.vue` | 80行 | P1 |
| 5 | 分类标签栏 | `CategoryTabs.vue` | 120行 | P0 |
| 6 | 菜品列表 | `MerchantDishList.vue` | 160行 | P0 |
| 7 | 菜品卡片（可复用） | `DishCard.vue` | - | 通用 |
| 8 | 商家操作栏 | `MerchantActions.vue` | 100行 | P1 |

**Composables拆分**：

| Composable | 职责 | 主要方法 |
|------------|------|----------|
| `useMerchant.ts` | 商家数据管理 | `fetchMerchant()`, `toggleFavorite()`, `shareMerchant()` |
| `useDishFilter.ts` | 菜品筛选 | `filterByCategory()`, `sortByPrice()` |
| `useCoupon.ts` | 优惠券管理 | `fetchCoupons()`, `receiveCoupon()` |

**实施后主页面**：
```vue
<template>
  <view class="merchant-detail">
    <MerchantHeader :merchant="merchant" />
    <CouponSection :coupons="coupons" />
    <CategoryTabs v-model="activeCategory" />
    <MerchantDishList :dishes="filteredDishes" />
  </view>
</template>

<script setup lang="ts">
import { useMerchant } from '@/composables/merchant/useMerchant'
import { useDishFilter } from '@/composables/merchant/useDishFilter'

const { merchant, coupons } = useMerchant()
const { filteredDishes, activeCategory } = useDishFilter()
</script>
```

---

### 📦 模块三：商家组件清单

**新建组件（8个）**：

```
src/components/merchant/
├── MerchantHeader.vue           # 商家头部
├── MerchantStats.vue            # 商家统计
├── CouponSection.vue            # 优惠券区域
├── CouponCard.vue               # 优惠券卡片
├── CategoryTabs.vue             # 分类标签
├── MerchantDishList.vue         # 菜品列表
├── DishCard.vue                 # 菜品卡片（通用）
└── MerchantActions.vue          # 商家操作
```

**新建Composables（3个）**：

```
src/composables/merchant/
├── useMerchant.ts               # 商家管理
├── useDishFilter.ts             # 菜品筛选
└── useCoupon.ts                 # 优惠券管理
```

---

## 五、模块四：菜品管理模块

### 📊 模块概览

| 统计项 | 数量 |
|--------|------|
| 涉及页面 | 3个 |
| 总代码行数 | 3,354行 |
| 需拆分组件 | 12个 |
| 需创建Composables | 4个 |
| 预计工时 | 3天 |

### 📄 页面清单

#### 5.1 菜品详情页面

**文件路径**：`src/pages-user/dish/detail/index.vue`
**当前代码量**：1,257行
**目标代码量**：< 400行

**详细拆分方案**：

| 步骤 | 拆分内容 | 文件名 | 预估行数 | 优先级 |
|------|----------|--------|----------|--------|
| 1 | 菜品头部信息 | `DishHeader.vue` | 150行 | P0 |
| 2 | 营养信息展示 | `NutritionInfo.vue` | 180行 | P0 |
| 3 | 菜品标签 | `DishTags.vue` | 100行 | P1 |
| 4 | 菜品图片轮播 | `DishImages.vue` | 120行 | P0 |
| 5 | 规格选择器 | `SpecSelector.vue` | 140行 | P0 |

**Composables拆分**：

| Composable | 职责 | 主要方法 |
|------------|------|----------|
| `useDish.ts` | 菜品数据管理 | `fetchDish()`, `toggleFavorite()` |
| `useNutrition.ts` | 营养计算 | `calculateCalories()`, `getNutrients()` |

---

#### 5.2 商家菜品编辑页面

**文件路径**：`src/pages-merchant/dish/edit.vue`
**当前代码量**：1,136行
**目标代码量**：< 400行

**详细拆分方案**：

| 步骤 | 拆分内容 | 文件名 | 预估行数 | 优先级 |
|------|----------|--------|----------|--------|
| 1 | 基本信息编辑 | `DishBasicEdit.vue` | 150行 | P0 |
| 2 | 营养信息编辑 | `DishNutritionEdit.vue` | 180行 | P0 |
| 3 | 图片上传器 | `ImageUploader.vue` | 200行 | P0 |
| 4 | 标签选择器 | `TagSelector.vue` | 120行 | P1 |
| 5 | 规格配置 | `SpecConfig.vue` | 160行 | P1 |

---

#### 5.3 菜品步骤配置页面

**文件路径**：`src/pages-merchant/dish/step-config.vue`
**当前代码量**：925行
**目标代码量**：< 350行

**详细拆分方案**：

| 步骤 | 拆分内容 | 文件名 | 预估行数 | 优先级 |
|------|----------|--------|----------|--------|
| 1 | 步骤编辑器 | `StepEditor.vue` | 200行 | P0 |
| 2 | 步骤预览 | `StepPreview.vue` | 150行 | P0 |
| 3 | 定时器配置 | `TimerConfig.vue` | 120行 | P1 |
| 4 | 步骤排序 | `StepSorter.vue` | 100行 | P2 |

---

### 📦 模块四：菜品组件清单

**新建组件（12个）**：

```
src/components/dish/
├── DishHeader.vue               # 菜品头部
├── NutritionInfo.vue            # 营养信息
├── DishTags.vue                 # 菜品标签
├── DishImages.vue               # 菜品图片
├── SpecSelector.vue             # 规格选择
├── DishBasicEdit.vue            # 基本信息编辑
├── DishNutritionEdit.vue        # 营养信息编辑
├── ImageUploader.vue            # 图片上传器
├── TagSelector.vue              # 标签选择
├── SpecConfig.vue               # 规格配置
├── StepEditor.vue               # 步骤编辑
└── StepPreview.vue              # 步骤预览
```

**新建Composables（4个）**：

```
src/composables/dish/
├── useDish.ts                   # 菜品管理
├── useNutrition.ts              # 营养计算
├── useDishEdit.ts               # 菜品编辑
└── useStepConfig.ts             # 步骤配置
```

---

## 六、模块五：AI智能模块

### 📊 模块概览

| 统计项 | 数量 |
|--------|------|
| 涉及页面 | 2个 |
| 总代码行数 | 2,378行 |
| 需拆分组件 | 10个 |
| 需创建Composables | 4个 |
| 预计工时 | 3天 |

### 📄 页面清单

#### 6.1 AI高级分析页面

**文件路径**：`src/pages-user/ai/advanced.vue`
**当前代码量**：1,384行
**目标代码量**：< 400行

**详细拆分方案**：

| 步骤 | 拆分内容 | 文件名 | 预估行数 | 优先级 |
|------|----------|--------|----------|--------|
| 1 | 营养环形图 | `NutritionChart.vue` | 200行 | P0 |
| 2 | 营养进度条 | `NutritionProgress.vue` | 120行 | P0 |
| 3 | 智能推荐卡片 | `RecommendCard.vue` | 150行 | P0 |
| 4 | 健康报告 | `HealthReport.vue` | 180行 | P0 |
| 5 | 分析选项卡 | `AnalysisTabs.vue` | 100行 | P1 |

**Composables拆分**：

| Composable | 职责 | 主要方法 |
|------------|------|----------|
| `useNutritionAnalysis.ts` | 营养分析 | `analyze()`, `getChartData()` |
| `useRecommend.ts` | 智能推荐 | `getRecommendations()`, `updatePreferences()` |
| `useHealthReport.ts` | 健康报告 | `generateReport()`, `exportReport()` |

---

#### 6.2 AI内容提取页面

**文件路径**：`src/pages-user/ai/content-extract.vue`
**当前代码量**：994行
**目标代码量**：< 350行

**详细拆分方案**：

| 步骤 | 拆分内容 | 文件名 | 预估行数 | 优先级 |
|------|----------|--------|----------|--------|
| 1 | 图片上传组件 | `AIImageUpload.vue` | 180行 | P0 |
| 2 | 内容预览 | `ContentPreview.vue` | 150行 | P0 |
| 3 | 提取结果展示 | `ExtractResult.vue` | 200行 | P0 |
| 4 | 编辑确认 | `EditConfirm.vue` | 120行 | P1 |

---

### 📦 模块五：AI组件清单

**新建组件（10个）**：

```
src/components/ai/
├── NutritionChart.vue           # 营养环形图
├── NutritionProgress.vue        # 营养进度
├── RecommendCard.vue            # 推荐卡片
├── HealthReport.vue             # 健康报告
├── AnalysisTabs.vue             # 分析选项卡
├── AIImageUpload.vue            # 图片上传
├── ContentPreview.vue           # 内容预览
├── ExtractResult.vue            # 提取结果
└── EditConfirm.vue              # 编辑确认
```

**新建Composables（4个）**：

```
src/composables/ai/
├── useNutritionAnalysis.ts      # 营养分析
├── useRecommend.ts              # 智能推荐
├── useHealthReport.ts           # 健康报告
└── useContentExtract.ts         # 内容提取
```

---

## 七、模块六：其他功能模块

### 📊 模块概览

| 统计项 | 数量 |
|--------|------|
| 涉及页面 | 5个 |
| 总代码行数 | 4,780行 |
| 需拆分组件 | 15个 |
| 需创建Composables | 5个 |
| 预计工时 | 3天 |

### 📄 页面清单

#### 7.1 钱包页面

**文件路径**：`src/pages-user/wallet/index.vue`
**当前代码量**：1,044行
**目标代码量**：< 350行

**详细拆分方案**：

| 组件名 | 预估行数 | 优先级 |
|--------|----------|--------|
| WalletBalance.vue | 120行 | P0 |
| TransactionList.vue | 180行 | P0 |
| RechargeDialog.vue | 150行 | P0 |
| WithdrawDialog.vue | 140行 | P1 |

---

#### 7.2 群订单创建页面

**文件路径**：`src/pages/group-order/create.vue`
**当前代码量**：968行
**目标代码量**：< 350行

**详细拆分方案**：

| 组件名 | 预估行数 | 优先级 |
|--------|----------|--------|
| MerchantSelect.vue | 150行 | P0 |
| DishSelect.vue | 180行 | P0 |
| ParticipantInvite.vue | 160行 | P0 |

---

#### 7.3 商家菜单管理页面

**文件路径**：`src/pages-merchant/menu/index.vue`
**当前代码量**：927行
**目标代码量**：< 350行

**详细拆分方案**：

| 组件名 | 预估行数 | 优先级 |
|--------|----------|--------|
| MenuCategory.vue | 140行 | P0 |
| DishManagement.vue | 200行 | P0 |
| BatchOperations.vue | 120行 | P1 |

---

#### 7.4 评价列表页面

**文件路径**：`src/pages-user/review/list/index.vue`
**当前代码量**：940行
**目标代码量**：< 350行

**详细拆分方案**：

| 组件名 | 预估行数 | 优先级 |
|--------|----------|--------|
| ReviewFilter.vue | 140行 | P0 |
| ReviewItem.vue | 160行 | P0 |
| ReviewStats.vue | 120行 | P1 |

---

#### 7.5 食谱详情页面

**文件路径**：`src/pages-user/recipe/detail/index.vue`
**当前代码量**：933行
**目标代码量**：< 350行

**详细拆分方案**：

| 组件名 | 预估行数 | 优先级 |
|--------|----------|--------|
| RecipeHeader.vue | 130行 | P0 |
| Ingredients.vue | 140行 | P0 |
| CookingSteps.vue | 180行 | P0 |

---

### 📦 模块六：其他组件清单

**新建组件（15个）**：

```
src/components/wallet/
├── WalletBalance.vue
├── TransactionList.vue
├── RechargeDialog.vue
└── WithdrawDialog.vue

src/components/group-order/
├── MerchantSelect.vue
├── DishSelect.vue
└── ParticipantInvite.vue

src/components/menu/
├── MenuCategory.vue
├── DishManagement.vue
└── BatchOperations.vue

src/components/review/
├── ReviewFilter.vue
├── ReviewItem.vue
└── ReviewStats.vue

src/components/recipe/
├── RecipeHeader.vue
├── Ingredients.vue
└── CookingSteps.vue
```

---

## 八、实施进度表

### 📅 总体时间安排

```
第1周（3月20日-3月26日）
├── 聊天通讯模块重构
├── 创建聊天相关组件（18个）
└── 创建聊天相关Composables（6个）

第2周（3月27日-4月2日）
├── 订单交易模块重构
├── 商家管理模块重构
├── 创建订单组件（16个）
└── 创建商家组件（8个）

第3周（4月3日-4月9日）
├── 菜品管理模块重构
├── AI智能模块重构
├── 创建菜品组件（12个）
└── 创建AI组件（10个）

第4周（4月10日-4月16日）
├── 其他功能模块重构
├── 创建其他组件（15个）
├── 整体测试和优化
└── 文档更新
```

### 📊 每日任务分解

#### 第1周：聊天模块

| 日期 | 任务 | 预计工时 | 负责人 | 状态 |
|------|------|----------|--------|------|
| 3月20日 | 创建聊天组件目录结构、提取GroupChatHeader | 4h | - | ⬜ 待开始 |
| 3月21日 | 提取MessageList和MessageBubble组件 | 6h | - | ⬜ 待开始 |
| 3月22日 | 提取MessageInput和useGroupChat | 6h | - | ⬜ 待开始 |
| 3月23日 | 重构group-chat.vue主页面 | 4h | - | ⬜ 待开始 |
| 3月24日 | 提取ChatRoomHeader和ChatInputToolbar | 4h | - | ⬜ 待开始 |
| 3月25日 | 提取EmojiPicker和ImagePreview | 6h | - | ⬜ 待开始 |
| 3月26日 | 重构chat-room.vue、创建useWebSocket | 6h | - | ⬜ 待开始 |

#### 第2周：订单+商家模块

| 日期 | 任务 | 预计工时 | 负责人 | 状态 |
|------|------|----------|--------|------|
| 3月27日 | 创建订单组件、提取OrderInfoCard | 4h | - | ⬜ 待开始 |
| 3月28日 | 提取PaymentMethodSelector和PaymentDetail | 6h | - | ⬜ 待开始 |
| 3月29日 | 提取PaymentPassword和usePayment | 6h | - | ⬜ 待开始 |
| 3月30日 | 重构payment/index.vue | 4h | - | ⬜ 待开始 |
| 3月31日 | 提取OrderStatus和OrderTimeline | 4h | - | ⬜ 待开始 |
| 4月1日 | 提取MerchantHeader和CouponSection | 6h | - | ⬜ 待开始 |
| 4月2日 | 重构merchant/detail/index.vue | 6h | - | ⬜ 待开始 |

#### 第3周：菜品+AI模块

| 日期 | 任务 | 预计工时 | 负责人 | 状态 |
|------|------|----------|--------|------|
| 4月3日 | 创建菜品组件、提取DishHeader | 4h | - | ⬜ 待开始 |
| 4月4日 | 提取NutritionInfo和DishImages | 6h | - | ⬜ 待开始 |
| 4月5日 | 重构dish/detail/index.vue | 4h | - | ⬜ 待开始 |
| 4月6日 | 创建AI组件、提取NutritionChart | 4h | - | ⬜ 待开始 |
| 4月7日 | 提取RecommendCard和HealthReport | 6h | - | ⬜ 待开始 |
| 4月8日 | 创建AI相关Composables | 6h | - | ⬜ 待开始 |
| 4月9日 | 重构ai/advanced.vue | 6h | - | ⬜ 待开始 |

#### 第4周：其他模块+测试

| 日期 | 任务 | 预计工时 | 负责人 | 状态 |
|------|------|----------|--------|------|
| 4月10日 | 重构wallet/index.vue | 4h | - | ⬜ 待开始 |
| 4月11日 | 重构group-order/create.vue | 4h | - | ⬜ 待开始 |
| 4月12日 | 重构menu/index.vue | 4h | - | ⬜ 待开始 |
| 4月13日 | 重构review/list/index.vue | 4h | - | ⬜ 待开始 |
| 4月14日 | 重构recipe/detail/index.vue | 4h | - | ⬜ 待开始 |
| 4月15日 | 整体功能测试、性能优化 | 8h | - | ⬜ 待开始 |
| 4月16日 | 文档更新、代码审查 | 8h | - | ⬜ 待开始 |

---

## 九、检查清单

### ✅ 模块一：聊天通讯模块

- [ ] 创建 `src/components/chat/` 目录
- [ ] **group-chat.vue 重构**
  - [ ] 提取 GroupChatHeader.vue
  - [ ] 提取 MessageList.vue
  - [ ] 提取 MessageBubble.vue
  - [ ] 提取 MessageInput.vue
  - [ ] 提取 GroupMemberList.vue
  - [ ] 提取 MemberCard.vue
  - [ ] 提取 MessageQuote.vue
  - [ ] 提取 QuickReply.vue
  - [ ] 创建 useGroupChat.ts
  - [ ] 创建 useMessage.ts
  - [ ] 创建 useGroupMember.ts
  - [ ] 创建 useMessageInput.ts
  - [ ] 重构主页面（目标<500行）
  - [ ] 功能测试
- [ ] **chat-room.vue 重构**
  - [ ] 提取 ChatRoomHeader.vue
  - [ ] 提取 ChatMessageArea.vue
  - [ ] 提取 ChatInputToolbar.vue
  - [ ] 提取 EmojiPicker.vue
  - [ ] 提取 ImagePreview.vue
  - [ ] 提取 MoreActionsPanel.vue
  - [ ] 创建 useChatRoom.ts
  - [ ] 创建 useWebSocket.ts
  - [ ] 创建 useEmoji.ts
  - [ ] 创建 useImagePreview.ts
  - [ ] 重构主页面（目标<500行）
  - [ ] 功能测试
- [ ] **group-detail.vue 重构**
  - [ ] 提取 GroupBasicInfo.vue
  - [ ] 复用 GroupMemberList.vue
  - [ ] 提取 GroupNoticeEditor.vue
  - [ ] 提取 GroupSettings.vue
  - [ ] 提取 MemberManagement.vue
  - [ ] 提取 TransferOwner.vue
  - [ ] 提取 DisbandGroup.vue
  - [ ] 创建 useGroupDetail.ts
  - [ ] 创建 useGroupSettings.ts
  - [ ] 重构主页面（目标<400行）
  - [ ] 功能测试

### ✅ 模块二：订单交易模块

- [ ] 创建 `src/components/order/` 目录
- [ ] **payment/index.vue 重构**
  - [ ] 提取 OrderInfoCard.vue
  - [ ] 提取 PaymentMethodSelector.vue
  - [ ] 提取 PaymentMethodItem.vue
  - [ ] 提取 PaymentDetail.vue
  - [ ] 提取 PaymentPassword.vue
  - [ ] 提取 CouponSelector.vue
  - [ ] 创建 usePayment.ts
  - [ ] 创建 usePaymentMethod.ts
  - [ ] 重构主页面（目标<400行）
  - [ ] 功能测试
- [ ] **order/detail/index.vue 重构**
  - [ ] 提取 OrderStatus.vue
  - [ ] 提取 OrderBasicInfo.vue
  - [ ] 提取 OrderDishList.vue
  - [ ] 提取 OrderDishCard.vue
  - [ ] 提取 OrderTimeline.vue
  - [ ] 提取 OrderActions.vue
  - [ ] 创建 useOrderDetail.ts
  - [ ] 创建 useOrderTimeline.ts
  - [ ] 重构主页面（目标<400行）
  - [ ] 功能测试
- [ ] **order/progress/index.vue 重构**
  - [ ] 提取 DeliveryMap.vue
  - [ ] 复用 OrderTimeline.vue
  - [ ] 提取 RiderInfoCard.vue
  - [ ] 提取 DeliveryStatusCard.vue
  - [ ] 提取 ContactRiderDialog.vue
  - [ ] 创建 useOrderProgress.ts
  - [ ] 重构主页面（目标<350行）
  - [ ] 功能测试
- [ ] **order/confirm/index.vue 重构**
  - [ ] 提取 AddressSelector.vue
  - [ ] 提取 AddressCard.vue
  - [ ] 提取 OrderSummary.vue
  - [ ] 提取 DeliveryTimeSelector.vue
  - [ ] 提取 OrderRemark.vue
  - [ ] 提取 DiscountSelector.vue
  - [ ] 创建 useOrderConfirm.ts
  - [ ] 重构主页面（目标<350行）
  - [ ] 功能测试

### ✅ 模块三：商家管理模块

- [ ] 创建 `src/components/merchant/` 目录
- [ ] **merchant/detail/index.vue 重构**
  - [ ] 提取 MerchantHeader.vue
  - [ ] 提取 MerchantStats.vue
  - [ ] 提取 CouponSection.vue
  - [ ] 提取 CouponCard.vue
  - [ ] 提取 CategoryTabs.vue
  - [ ] 提取 MerchantDishList.vue
  - [ ] 提取 DishCard.vue（通用）
  - [ ] 提取 MerchantActions.vue
  - [ ] 创建 useMerchant.ts
  - [ ] 创建 useDishFilter.ts
  - [ ] 创建 useCoupon.ts
  - [ ] 重构主页面（目标<400行）
  - [ ] 功能测试

### ✅ 模块四：菜品管理模块

- [ ] 创建 `src/components/dish/` 目录
- [ ] **dish/detail/index.vue 重构**
  - [ ] 提取 DishHeader.vue
  - [ ] 提取 NutritionInfo.vue
  - [ ] 提取 DishTags.vue
  - [ ] 提取 DishImages.vue
  - [ ] 提取 SpecSelector.vue
  - [ ] 创建 useDish.ts
  - [ ] 创建 useNutrition.ts
  - [ ] 重构主页面（目标<400行）
  - [ ] 功能测试
- [ ] **dish/edit.vue 重构**
  - [ ] 提取 DishBasicEdit.vue
  - [ ] 提取 DishNutritionEdit.vue
  - [ ] 提取 ImageUploader.vue
  - [ ] 提取 TagSelector.vue
  - [ ] 提取 SpecConfig.vue
  - [ ] 创建 useDishEdit.ts
  - [ ] 重构主页面（目标<400行）
  - [ ] 功能测试
- [ ] **dish/step-config.vue 重构**
  - [ ] 提取 StepEditor.vue
  - [ ] 提取 StepPreview.vue
  - [ ] 提取 TimerConfig.vue
  - [ ] 提取 StepSorter.vue
  - [ ] 创建 useStepConfig.ts
  - [ ] 重构主页面（目标<350行）
  - [ ] 功能测试

### ✅ 模块五：AI智能模块

- [ ] 创建 `src/components/ai/` 目录
- [ ] **ai/advanced.vue 重构**
  - [ ] 提取 NutritionChart.vue
  - [ ] 提取 NutritionProgress.vue
  - [ ] 提取 RecommendCard.vue
  - [ ] 提取 HealthReport.vue
  - [ ] 提取 AnalysisTabs.vue
  - [ ] 创建 useNutritionAnalysis.ts
  - [ ] 创建 useRecommend.ts
  - [ ] 创建 useHealthReport.ts
  - [ ] 重构主页面（目标<400行）
  - [ ] 功能测试
- [ ] **ai/content-extract.vue 重构**
  - [ ] 提取 AIImageUpload.vue
  - [ ] 提取 ContentPreview.vue
  - [ ] 提取 ExtractResult.vue
  - [ ] 提取 EditConfirm.vue
  - [ ] 创建 useContentExtract.ts
  - [ ] 重构主页面（目标<350行）
  - [ ] 功能测试

### ✅ 模块六：其他功能模块

- [ ] 创建相关组件目录
- [ ] **wallet/index.vue 重构**
  - [ ] 提取 WalletBalance.vue
  - [ ] 提取 TransactionList.vue
  - [ ] 提取 RechargeDialog.vue
  - [ ] 提取 WithdrawDialog.vue
  - [ ] 重构主页面（目标<350行）
  - [ ] 功能测试
- [ ] **group-order/create.vue 重构**
  - [ ] 提取 MerchantSelect.vue
  - [ ] 提取 DishSelect.vue
  - [ ] 提取 ParticipantInvite.vue
  - [ ] 重构主页面（目标<350行）
  - [ ] 功能测试
- [ ] **menu/index.vue 重构**
  - [ ] 提取 MenuCategory.vue
  - [ ] 提取 DishManagement.vue
  - [ ] 提取 BatchOperations.vue
  - [ ] 重构主页面（目标<350行）
  - [ ] 功能测试
- [ ] **review/list/index.vue 重构**
  - [ ] 提取 ReviewFilter.vue
  - [ ] 提取 ReviewItem.vue
  - [ ] 提取 ReviewStats.vue
  - [ ] 重构主页面（目标<350行）
  - [ ] 功能测试
- [ ] **recipe/detail/index.vue 重构**
  - [ ] 提取 RecipeHeader.vue
  - [ ] 提取 Ingredients.vue
  - [ ] 提取 CookingSteps.vue
  - [ ] 重构主页面（目标<350行）
  - [ ] 功能测试

### ✅ 整体验收

- [ ] 所有页面代码行数检查（目标<500行）
- [ ] 组件复用率统计
- [ ] 功能完整性测试
- [ ] 性能测试（首屏加载、渲染速度）
- [ ] 代码审查
- [ ] 文档更新
- [ ] 发布上线

---

## 📝 附录

### A. 组件命名规范

```
# 页面组件
pages-xxx/xxx/index.vue

# 业务组件
components/[module]/[ComponentName].vue

# 通用组件
components/common/[ComponentName].vue

# Composables
composables/[module]/use[Feature].ts
```

### B. Git提交规范

```bash
# 重构提交
feat(refactor): 重构群聊页面，提取消息组件
- 提取 MessageList.vue
- 提取 MessageBubble.vue
- 提取 MessageInput.vue
- 创建 useGroupChat.ts
- 主页面从1381行降至300行

# 测试提交
test(chat): 添加群聊组件单元测试
- MessageList 组件测试
- MessageBubble 组件测试
```

### C. 代码审查检查项

- [ ] 组件职责单一
- [ ] Props/Emits 定义清晰
- [ ] 使用 TypeScript 类型定义
- [ ] 添加必要的注释
- [ ] 样式使用 scoped
- [ ] 无 console.log 调试代码
- [ ] 性能优化（避免不必要的渲染）

---

## 📞 联系方式

如有疑问，请联系：
- **项目负责人**：许佳宜
- **指导教师**：温清机

---

**文档版本**：v1.0
**最后更新**：2026-03-20
