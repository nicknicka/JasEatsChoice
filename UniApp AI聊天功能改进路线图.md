# UniApp AI聊天功能改进路线图

## 文档信息

- **项目名称**：JasEatsChoice UniApp AI聊天功能
- **当前版本**：v1.0
- **创建日期**：2026-03-30
- **最后更新**：2026-03-30
- **维护人员**：Claude Code

---

## 📋 目录

- [当前状态总结](#当前状态总结)
- [短期改进（1-2周）](#短期改进1-2周)
- [中期改进（2-4周）](#中期改进2-4周)
- [长期改进（1-2月）](#长期改进1-2月)
- [优先级排序](#优先级排序)
- [技术债务清单](#技术债务清单)
- [性能优化建议](#性能优化建议)
- [用户体验优化](#用户体验优化)
- [测试计划](#测试计划)

---

## 当前状态总结

### ✅ 已完成功能

| 模块 | 功能 | 状态 | 完成度 |
|------|------|------|--------|
| **接口对接** | SupervisorAgent统一接口 | ✅ 完成 | 100% |
| **数据解析** | 卡片数据解析工具 | ✅ 完成 | 100% |
| **卡片组件** | 菜品列表卡片 | ✅ 完成 | 100% |
| **卡片组件** | 订单列表卡片 | ✅ 完成 | 100% |
| **卡片组件** | 收藏列表卡片 | ✅ 完成 | 100% |
| **卡片组件** | 用户信息卡片 | ✅ 完成 | 100% |
| **UI渲染** | 卡片渲染逻辑 | ✅ 完成 | 100% |
| **占位符** | Emoji占位符 | ✅ 完成 | 100% |
| **数据持久化** | 消息保存（含卡片数据） | ✅ 完成 | 100% |

### 🎊 功能亮点

1. **统一架构**：UniApp和桌面端使用同一SupervisorAgent
2. **智能路由**：自动识别用户意图并路由到合适的卡片类型
3. **数据解析**：自动解析卡片数据标记，支持多种格式
4. **类型映射**：后端类型自动映射到前端支持的类型
5. **优雅降级**：卡片数据解析失败时不影响文本显示

---

## 短期改进（1-2周）

### 🔴 高优先级 - 本周完成

#### 1. 完善交互功能对接

**当前状态**：
- ✅ 卡片UI已完整实现
- ❌ 操作按钮只显示Toast，没有实际功能

**改进内容**：

##### 1.1 购物车功能
```javascript
// DishListCard.vue
const handleAction = (actionType, dish) => {
    switch (actionType) {
        case 'add_to_cart':
            // 调用购物车API
            cartApi.addDish({
                dishId: dish.dishId,
                quantity: 1
            }).then(() => {
                uni.showToast({
                    title: '已加入购物车',
                    icon: 'success'
                });
                // 更新购物车数量
                updateCartCount();
            });
            break;
    }
};
```

**验收标准**：
- ✅ 点击"加入购物车"按钮后调用API
- ✅ 成功后显示Toast提示
- ✅ 更新购物车角标数量
- ✅ 失败时显示错误提示

##### 1.2 收藏功能
```javascript
// FavoriteListCard.vue
const handleRemove = (item) => {
    favoriteApi.remove(item.id).then(() => {
        uni.showToast({
            title: '已取消收藏',
            icon: 'success'
        });
        // 从列表中移除
        removeFromList(item.id);
    });
};
```

**验收标准**：
- ✅ 点击"取消收藏"后调用API
- ✅ 成功后从列表中移除
- ✅ 失败时显示错误提示

##### 1.3 订单详情跳转
```javascript
// OrderListCard.vue
const handleViewDetail = (order) => {
    uni.navigateTo({
        url: `/pages-user/order/progress?orderId=${order.orderId}`
    });
};
```

**验收标准**：
- ✅ 点击"查看详情"跳转到订单详情页
- ✅ 订单ID正确传递
- ✅ 页面返回后保持状态

**工作量估计**：2-3天

---

#### 2. 添加卡片加载状态

**当前问题**：
- ❌ 卡片数据加载时没有loading提示
- ❌ 用户不知道系统正在工作

**改进方案**：

##### 2.1 添加骨架屏
```vue
<!-- DishListCard.vue -->
<template>
    <view class="dish-list-card">
        <!-- 骨架屏 -->
        <view v-if="loading" class="skeleton-wrapper">
            <view class="skeleton-item" v-for="i in 3" :key="i">
                <view class="skeleton-image"></view>
                <view class="skeleton-content">
                    <view class="skeleton-title"></view>
                    <view class="skeleton-text"></view>
                </view>
            </view>
        </view>

        <!-- 实际内容 -->
        <view v-else class="card-content">
            <!-- 卡片内容 -->
        </view>
    </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { dishApi } from '@/api';

const loading = ref(true);
const dishes = ref([]);

onMounted(async () => {
    if (props.data.dishes) {
        // 如果有数据直接使用
        dishes.value = props.data.dishes;
        loading.value = false;
    } else if (props.data.dishIds) {
        // 如果有ID列表，需要加载
        try {
            const result = await dishApi.getListByIds(props.data.dishIds);
            dishes.value = result.data;
        } catch (error) {
            console.error('加载菜品失败:', error);
        } finally {
            loading.value = false;
        }
    }
});
</script>

<style lang="scss" scoped>
.skeleton-wrapper {
    padding: $spacing-md;
}

.skeleton-item {
    display: flex;
    gap: $spacing-md;
    margin-bottom: $spacing-md;
    padding: $spacing-md;
    background: $bg-color-base;
    border-radius: $border-radius-base;
}

.skeleton-image {
    width: 160rpx;
    height: 160rpx;
    border-radius: $border-radius-base;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
}

.skeleton-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: $spacing-sm;
}

.skeleton-title {
    width: 60%;
    height: 32rpx;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
    border-radius: 4rpx;
}

.skeleton-text {
    width: 100%;
    height: 24rpx;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
    border-radius: 4rpx;
}

@keyframes shimmer {
    0% { background-position: 200% 0; }
    100% { background-position: -200% 0; }
}
</style>
```

**验收标准**：
- ✅ 数据加载时显示骨架屏
- ✅ 加载完成后显示实际内容
- ✅ 骨架屏动画流畅

**工作量估计**：1-2天

---

#### 3. 添加更多卡片类型

**当前缺失**：

##### 3.1 评价列表卡片 (ReviewListCard)
```vue
<template>
    <view class="review-list-card">
        <view class="card-header">
            <text class="icon">⭐</text>
            <text class="title">评价列表</text>
        </view>

        <view class="review-list">
            <view
                v-for="review in reviews"
                :key="review.id"
                class="review-item"
            >
                <view class="review-header">
                    <text class="reviewer">{{ review.userName }}</text>
                    <view class="rating">
                        <text v-for="i in 5" :key="i">
                            {{ i <= review.rating ? '⭐' : '☆' }}
                        </text>
                    </view>
                </view>
                <text class="review-content">{{ review.content }}</text>
                <text class="review-time">{{ formatTime(review.createTime) }}</text>
            </view>
        </view>
    </view>
</template>
```

##### 3.2 优惠券列表卡片 (CouponListCard)
```vue
<template>
    <view class="coupon-list-card">
        <view class="card-header">
            <text class="icon">🎫</text>
            <text class="title">优惠券列表</text>
        </view>

        <view class="coupon-list">
            <view
                v-for="coupon in coupons"
                :key="coupon.id"
                class="coupon-item"
                @tap="handleReceive(coupon)"
            >
                <view class="coupon-amount">
                    <text class="amount">¥{{ coupon.amount }}</text>
                    <text class="condition">{{ coupon.condition }}</text>
                </view>
                <view class="coupon-info">
                    <text class="coupon-name">{{ coupon.name }}</text>
                    <text class="coupon-time">{{ formatDate(coupon.endTime) }}</text>
                </view>
                <view class="coupon-action">
                    <text class="receive-btn">立即领取</text>
                </view>
            </view>
        </view>
    </view>
</template>
```

##### 3.3 错误提示卡片 (ErrorCard)
```vue
<template>
    <view class="error-card">
        <view class="error-icon">⚠️</view>
        <text class="error-title">{{ data.title }}</text>
        <text class="error-message">{{ data.message }}</text>
        <view class="error-actions">
            <button @click="handleRetry">重试</button>
            <button @click="handleCancel">取消</button>
        </view>
    </view>
</template>
```

**验收标准**：
- ✅ 新增3种卡片类型
- ✅ 样式与现有卡片一致
- ✅ 支持交互操作

**工作量估计**：3-4天

---

#### 4. 测试和调试

##### 4.1 功能测试清单

**基础功能**：
- [ ] AI聊天消息发送和接收
- [ ] 卡片数据正确解析
- [ ] 所有卡片类型正确显示
- [ ] 操作按钮功能正常
- [ ] 聊天历史保存和加载

**边界情况**：
- [ ] 无网络连接时的处理
- [ ] API超时时的处理
- [ ] 空数据时的显示
- [ ] 错误数据的降级处理

**兼容性测试**：
- [ ] iOS端测试
- [ ] Android端测试
- [ ] 小程序端测试
- [ ] H5端测试

##### 4.2 性能测试

**测试指标**：
- 消息加载时间 < 2秒
- 卡片渲染时间 < 1秒
- 滚动流畅度 > 60fps
- 内存占用 < 100MB

**测试工具**：
- Chrome DevTools（H5）
- Safari Web Inspector（iOS）
- Android Profiler（Android）

**工作量估计**：2-3天

---

## 中期改进（2-4周）

### 🟡 中优先级 - 2周内完成

#### 5. 用户体验优化

##### 5.1 添加快捷提问按钮

**当前问题**：
- ❌ 用户需要手动输入所有问题
- ❌ 不知道可以问什么

**改进方案**：

```vue
<!-- 快捷提问面板 -->
<view class="quick-questions-bar">
    <text class="bar-title">💬 常见问题</text>
    <scroll-view class="questions-scroll" scroll-x>
        <view
            v-for="(question, index) in quickQuestions"
            :key="index"
            class="question-chip"
            @tap="askQuickQuestion(question)"
        >
            <text class="question-text">{{ question }}</text>
        </view>
    </scroll-view>
</view>

<script setup>
const quickQuestions = ref([
    '推荐一些好吃的菜品',
    '我的订单有哪些',
    '查看我的收藏',
    '我的个人信息',
    '推荐适合减肥的食谱',
    '今日卡路里摄入建议'
]);

const askQuickQuestion = (question) => {
    inputText.value = question;
    sendMessage();
};
</script>

<style lang="scss" scoped>
.quick-questions-bar {
    padding: $spacing-md;
    background: $bg-color-white;
    border-bottom: 1rpx solid $border-color-light;
}

.questions-scroll {
    display: flex;
    white-space: nowrap;
}

.question-chip {
    display: inline-block;
    padding: $spacing-sm $spacing-md;
    margin-right: $spacing-sm;
    background: $primary-50;
    border-radius: $border-radius-round;
    border: 1rpx solid $primary-200;
}

.question-text {
    font-size: $font-size-sm;
    color: $primary-500;
    white-space: nowrap;
}
</style>
```

**验收标准**：
- ✅ 显示6个常用问题
- ✅ 点击直接发送
- ✅ 横向滚动查看所有问题

**工作量估计**：1天

---

##### 5.2 添加"新对话"按钮

```vue
<view class="new-chat-btn" @tap="handleNewChat">
    <text class="btn-icon">✨</text>
    <text class="btn-text">新对话</text>
</view>

<script setup>
const handleNewChat = () => {
    uni.showModal({
        title: '开始新对话',
        content: '这将清空当前聊天记录，是否继续？',
        success: (res) => {
            if (res.confirm) {
                clearHistory();
            }
        }
    });
};
</script>
```

**验收标准**：
- ✅ 点击按钮弹出确认对话框
- ✅ 确认后清空聊天记录
- ✅ 添加成功提示

**工作量估计**：0.5天

---

##### 5.3 消息输入优化

**当前问题**：
- ❌ 只支持文本输入
- ❌ 没有语音输入

**改进方案**：

```vue
<!-- 添加语音输入按钮 -->
<view class="input-actions">
    <view class="action-btn" @tap="startVoiceInput">
        <text class="btn-icon">🎤</text>
    </view>
</view>

<script setup>
const startVoiceInput = () => {
    // #ifdef APP-PLUS
    const speechRecognizer = uni.requireNativePlugin('SpeechRecognizer');
    speechRecognizer.start({
        lang: 'zh-cn',
        continuous: false,
        success: (res) => {
            inputText.value = res.result;
        }
    });
    // #endif

    // #ifndef APP-PLUS
    uni.showToast({
        title: '语音输入仅在APP中支持',
        icon: 'none'
    });
    // #endif
};
</script>
```

**工作量估计**：2天

---

##### 5.4 消息显示增强

**改进内容**：

1. **Markdown渲染**
```vue
<view class="markdown-content" v-html="renderMarkdown(msg.content)"></view>

<script setup>
import { marked } from 'marked';

const renderMarkdown = (content) => {
    return marked(content);
};
</script>
```

2. **代码高亮**
```vue
<view class="code-block">
    <text class="code-text">{{ code }}</text>
</view>
```

3. **消息复制功能**
```vue
<view class="message-actions" @longpress="showActions(msg)">
    <view class="action-item" @tap="copyMessage(msg)">
        <text>复制</text>
    </view>
    <view class="action-item" @tap="deleteMessage(msg)">
        <text>删除</text>
    </view>
</view>
```

**工作量估计**：3天

---

#### 6. 性能优化

##### 6.1 虚拟滚动

**当前问题**：
- ❌ 消息列表很长时可能卡顿
- ❌ 一次性渲染所有消息

**改进方案**：

```vue
<scroll-view
    class="chat-messages"
    scroll-y
    :scroll-into-view="scrollIntoView"
    @scrolltolower="loadMore"
>
    <view
        v-for="(msg, index) in visibleMessages"
        :key="msg.id"
        class="message-wrapper"
    >
        <!-- 消息内容 -->
    </view>
</scroll-view>

<script setup>
import { computed } from 'vue';

const visibleMessages = computed(() => {
    const startIndex = Math.max(0, messages.value.length - 50);
    const endIndex = messages.value.length;
    return messages.value.slice(startIndex, endIndex);
});
</script>
```

**工作量估计**：2天

---

##### 6.2 图片优化

```vue
<template>
    <image
        :src="dish.imageUrl"
        mode="aspectFill"
        lazy-load
        :webp="true"
        class="dish-img"
    />
</template>
```

**优化点**：
- ✅ 懒加载：只加载可见区域的图片
- ✅ WebP格式：优先使用WebP格式
- ✅ 图片压缩：自动压缩大图

**工作量估计**：1天

---

##### 6.3 数据缓存

```javascript
// utils/cache.js
export const cacheManager = {
    set(key, data, ttl = 3600000) {
        const expireTime = Date.now() + ttl;
        uni.setStorageSync(key, JSON.stringify({
            data,
            expireTime
        }));
    },

    get(key) {
        const cached = uni.getStorageSync(key);
        if (!cached) return null;

        const { data, expireTime } = JSON.parse(cached);
        if (Date.now() > expireTime) {
            uni.removeStorageSync(key);
            return null;
        }

        return data;
    }
};

// 使用缓存
const getCachedDishes = async (dishIds) => {
    const cacheKey = `dishes_${dishIds.join(',')}`;
    const cached = cacheManager.get(cacheKey);

    if (cached) {
        console.log('使用缓存数据');
        return cached;
    }

    const result = await dishApi.getListByIds(dishIds);
    cacheManager.set(cacheKey, result.data);
    return result.data;
};
```

**工作量估计**：2天

---

#### 7. 对话历史管理

##### 7.1 多会话支持

```javascript
// store/modules/chat.js
export const useChatStore = defineStore('chat', {
    state: () => ({
        sessions: [],
        currentSessionId: null
    }),

    actions: {
        createSession() {
            const sessionId = Date.now().toString();
            this.sessions.push({
                sessionId,
                title: '新对话',
                messages: [],
                createTime: Date.now()
            });
            this.currentSessionId = sessionId;
            return sessionId;
        },

        switchSession(sessionId) {
            this.currentSessionId = sessionId;
        },

        deleteSession(sessionId) {
            const index = this.sessions.findIndex(s => s.sessionId === sessionId);
            if (index > -1) {
                this.sessions.splice(index, 1);
            }
        }
    }
});
```

**工作量估计**：3天

---

##### 7.2 搜索功能

```vue
<view class="search-bar">
    <input
        v-model="searchKeyword"
        placeholder="搜索聊天记录"
        @confirm="handleSearch"
    />
</view>

<script setup>
const handleSearch = () => {
    const keyword = searchKeyword.value.trim();
    if (!keyword) return;

    const results = messages.value.filter(msg =>
        msg.content.includes(keyword)
    );

    // 高亮关键词
    highlightResults(results, keyword);
};
</script>
```

**工作量估计**：2天

---

## 长期改进（1-2月）

### 🔵 低优先级 - 按需完成

#### 8. 高级功能

##### 8.1 多模态支持

**图片识别功能**：
```javascript
const handleImageUpload = async (imagePath) => {
    uni.showLoading({ title: '识别中...' });

    try {
        const result = await aiApi.recognizeDish(imagePath);
        console.log('识别结果:', result);

        // 将识别结果添加到输入框
        inputText.value = `这张图片是什么菜品？`;
        sendMessage();
    } catch (error) {
        uni.showToast({
            title: '识别失败',
            icon: 'none'
        });
    } finally {
        uni.hideLoading();
    }
};
```

**语音输入功能**：
```javascript
const startVoiceInput = () => {
    const recorderManager = uni.getRecorderManager();

    recorderManager.onStop((res) => {
        const { tempFilePath } = res;
        // 语音转文字
        speechToText(tempFilePath);
    });

    recorderManager.start();
};
```

**工作量估计**：5天

---

##### 8.2 个性化推荐

**基于历史的推荐**：
```javascript
const getPersonalizedRecommendations = async () => {
    // 获取用户历史记录
    const history = await aiApi.getHistory(userId);

    // 分析用户偏好
    const preferences = analyzePreferences(history);

    // 生成推荐
    const recommendations = await aiApi.recommend({
        userId,
        preferences
    });

    return recommendations;
};
```

**智能快捷提问**：
```javascript
const getSmartQuickQuestions = async () => {
    // 根据时间、历史等动态生成
    const hour = new Date().getHours();

    if (hour >= 7 && hour < 9) {
        return ['推荐早餐', '今日营养建议'];
    } else if (hour >= 11 && hour < 13) {
        return ['推荐午餐', '附近餐厅推荐'];
    } else if (hour >= 18 && hour < 20) {
        return ['推荐晚餐', '今日摄入总结'];
    }
};
```

**工作量估计**：4天

---

##### 8.3 数据可视化

**卡路里摄入图表**：
```vue
<view class="calorie-chart">
    <canvas canvas-id="calorieChart"></canvas>
</view>

<script setup>
import uCharts from '@/utils/u-charts';

onMounted(() => {
    const chart = new uCharts({
        type: 'line',
        canvasId: 'calorieChart',
        data: calorieData
    });
});
</script>
```

**饮食分析报告**：
```javascript
const generateDietReport = async () => {
    const report = await aiApi.analyzeDiet({
        userId,
        dateRange: 'week'
    });

    // 生成可视化报告
    return {
        totalCalories: report.totalCalories,
        averageCalories: report.averageCalories,
        nutritionBalance: report.nutritionBalance
    };
};
```

**工作量估计**：6天

---

#### 9. 代码质量提升

##### 9.1 TypeScript迁移

**当前状态**：纯JavaScript
**目标**：TypeScript

**步骤**：
1. 配置TypeScript环境
2. 定义接口类型
3. 逐步迁移组件
4. 添加类型检查

```typescript
// types/chat.ts
export interface Message {
    id: number;
    sender: 'user' | 'ai';
    content: string;
    messageType?: string | null;
    cardData?: object | null;
    time: string;
    avatar: string;
    isUser: boolean;
}

export interface CardData {
    dishes?: Dish[];
    orders?: Order[];
    favorites?: Favorite[];
}

export interface Dish {
    dishId: string;
    dishName: string;
    imageUrl?: string;
    price?: number;
    rating?: number;
}
```

**工作量估计**：10天

---

##### 9.2 单元测试

**测试框架**：Jest + @testing-library/vue

```javascript
// tests/unit/cardParser.test.js
import { parseCardDataFromContent } from '@/utils/cardParser';

describe('cardParser', () => {
    test('解析菜品卡片数据', () => {
        const content = `推荐菜品

[CARD_DATA_START]
[{"type": "dish", "data": {"dishes": [{"dishId": "1", "dishName": "西红柿炒鸡蛋"}]}}]
[CARD_DATA_END]`;

        const result = parseCardDataFromContent(content);

        expect(result.messageType).toBe('dish_list_card');
        expect(result.cardData.dishes).toHaveLength(1);
        expect(result.cardData.dishes[0].dishName).toBe('西红柿炒鸡蛋');
    });

    test('解析失败时返回原内容', () => {
        const content = '没有卡片数据的普通文本';
        const result = parseCardDataFromContent(content);

        expect(result.messageType).toBeNull();
        expect(result.cardData).toBeNull();
        expect(result.content).toBe(content);
    });
});
```

**工作量估计**：5天

---

##### 9.3 E2E测试

**测试框架**：Playwright / Appium

```javascript
// tests/e2e/ai-chat.spec.js
test('完整聊天流程', async ({ page }) => {
    // 1. 进入AI聊天页面
    await page.goto('/pages/ai/index');

    // 2. 发送消息
    await page.fill('input[data-testid="chat-input"]', '推荐一些菜品');
    await page.tap('button[data-testid="send-btn"]');

    // 3. 等待AI回复
    await page.waitForSelector('.message-ai');

    // 4. 验证卡片显示
    await expect(page.locator('.dish-list-card')).toBeVisible();

    // 5. 点击操作按钮
    await page.tap('.action-btn');

    // 6. 验证成功提示
    await expect(page.locator('.uni-toast')).toContainText('已加入购物车');
});
```

**工作量估计**：5天

---

#### 10. 高级UI/UX

##### 10.1 打字机效果

```javascript
const startTypewriter = async (messageIndex, fullText, speed = 50) => {
    const message = messages.value[messageIndex];
    let displayText = '';

    for (let i = 0; i < fullText.length; i++) {
        displayText += fullText[i];
        message.displayContent = displayText;
        await nextTick();
        await new Promise(resolve => setTimeout(resolve, speed));
    }

    message.isTyping = false;
};
```

##### 10.2 消息动画

```vue
<transition-group name="message">
    <view
        v-for="msg in messages"
        :key="msg.id"
        class="message-wrapper"
    >
        <!-- 消息内容 -->
    </view>
</transition-group>

<style>
.message-enter-active {
    animation: slideIn 0.3s ease-out;
}

.message-leave-active {
    animation: slideOut 0.3s ease-in;
}
</style>
```

**工作量估计**：3天

---

## 优先级排序

### 🔴 立即完成（本周）

| 序号 | 改进项 | 工作量 | 价值 | 优先级 |
|------|--------|--------|------|--------|
| 1 | 完善交互功能对接 | 2-3天 | 高 | 🔴 P0 |
| 2 | 添加卡片加载状态 | 1-2天 | 高 | 🔴 P0 |
| 3 | 功能测试 | 2-3天 | 高 | 🔴 P0 |

**总计**：5-8天

---

### 🟡 2周内完成

| 序号 | 改进项 | 工作量 | 价值 | 优先级 |
|------|--------|--------|------|--------|
| 4 | 添加快捷提问按钮 | 1天 | 中 | 🟡 P1 |
| 5 | 添加"新对话"按钮 | 0.5天 | 中 | 🟡 P1 |
| 6 | 添加更多卡片类型 | 3-4天 | 中 | 🟡 P1 |
| 7 | 消息输入优化 | 2天 | 中 | 🟢 P2 |
| 8 | 消息显示增强 | 3天 | 中 | 🟢 P2 |

**总计**：9.5-10.5天

---

### 🟢 按需完成（1-2月）

| 序号 | 改进项 | 工作量 | 价值 | 优先级 |
|------|--------|--------|------|--------|
| 9 | 虚拟滚动 | 2天 | 中 | 🟢 P2 |
| 10 | 图片优化 | 1天 | 低 | 🟢 P3 |
| 11 | 数据缓存 | 2天 | 中 | 🟢 P2 |
| 12 | 多会话支持 | 3天 | 中 | 🟢 P2 |
| 13 | 搜索功能 | 2天 | 低 | 🟢 P3 |
| 14 | TypeScript迁移 | 10天 | 高 | 🟢 P3 |
| 15 | 单元测试 | 5天 | 高 | 🟢 P3 |

**总计**：25天

---

## 技术债务清单

### 当前存在的技术债务

| 序号 | 债务项 | 影响 | 建议解决时间 |
|------|--------|------|-------------|
| 1 | 缺少错误边界处理 | 中 | 立即 |
| 2 | 缺少单元测试覆盖 | 高 | 2周内 |
| 3 | 缺少类型检查 | 中 | 1月内 |
| 4 | 缺少性能监控 | 中 | 2周内 |
| 5 | 缺少日志系统 | 低 | 按需 |
| 6 | 缺少文档注释 | 低 | 持续 |

---

## 性能优化建议

### 1. 首屏加载优化

**目标**：首屏加载时间 < 2秒

**措施**：
- 路由懒加载
- 组件异步加载
- 图片懒加载
- 预加载关键资源

```javascript
// 路由懒加载
const routes = [
    {
        path: '/pages/ai/index',
        component: () => import('@/pages/ai/index.vue')
    }
];
```

---

### 2. 运行时性能优化

**目标**：滚动帧率 > 60fps

**措施**：
- 虚拟滚动（只渲染可见消息）
- 防抖/节流（滚动事件）
- requestAnimationFrame（动画优化）

```javascript
import { debounce } from 'lodash-es';

const handleScroll = debounce(() => {
    // 处理滚动
}, 100);
```

---

### 3. 内存优化

**目标**：内存占用 < 100MB

**措施**：
- 限制历史消息数量（最多保留100条）
- 图片缓存限制（最多50张）
- 及时销毁不用的对象

---

## 用户体验优化

### 1. 交互反馈优化

**当前问题**：操作后反馈不明显

**改进方案**：
- 所有操作添加Loading状态
- 成功/失败有明确提示
- 错误有友好提示和解决方案

---

### 2. 空状态优化

**当前问题**：空状态提示不够友好

**改进方案**：
- 使用插图或Emoji
- 提供操作引导
- 添加快捷操作按钮

---

### 3. 错误处理优化

**当前问题**：错误提示不够详细

**改进方案**：
- 区分错误类型（网络错误、服务器错误、业务错误）
- 提供重试按钮
- 给出解决建议

---

## 测试计划

### 单元测试

**覆盖目标**：核心工具函数

**测试内容**：
- [x] cardParser.js - 卡片数据解析
- [ ] utils/cache.js - 缓存管理
- [ ] utils/helper.js - 工具函数

---

### 集成测试

**覆盖目标**：API调用流程

**测试内容**：
- [ ] AI聊天流程
- [ ] 卡片渲染流程
- [ ] 数据持久化流程
- [ ] 错误处理流程

---

### E2E测试

**覆盖目标**：关键用户路径

**测试内容**：
- [ ] 发送消息 → 接收回复 → 显示卡片
- [ ] 点击卡片按钮 → 跳转详情页
- [ ] 查看历史记录
- [ ] 清空聊天记录

---

## 实施建议

### 第1周：功能完善
- 周一-周三：完善交互功能对接
- 周四-周五：添加加载状态和错误处理

### 第2周：功能增强
- 周一-周二：添加快捷提问和新对话按钮
- 周三-周五：添加更多卡片类型

### 第3-4周：优化提升
- 性能优化（虚拟滚动、图片优化）
- 用户体验优化（Markdown渲染、消息复制）
- 测试和bug修复

---

## 成功指标

### 功能指标
- ✅ 所有卡片类型正常显示
- ✅ 操作按钮功能正常
- ✅ 错误处理完善
- ✅ 加载状态友好

### 性能指标
- ✅ 首屏加载 < 2秒
- ✅ 消息响应 < 1秒
- ✅ 卡片渲染 < 0.5秒
- ✅ 滚动流畅度 > 60fps

### 质量指标
- ✅ 单元测试覆盖率 > 80%
- ✅ E2E测试通过率 100%
- ✅ Bug率 < 5%

---

## 总结

本改进路线图提供了从短期到长期的完整改进方向，建议按照优先级逐步实施：

**立即完成（本周）**：
1. 完善交互功能对接
2. 添加卡片加载状态
3. 进行功能测试

**近期完成（2周内）**：
1. 添加快捷提问按钮
2. 添加更多卡片类型
3. 优化用户输入体验

**长期规划（1-2月）**：
1. TypeScript迁移
2. 单元测试覆盖
3. 高级功能开发

---

**文档版本**：v1.0
**最后更新**：2026-03-30
**维护人**：Claude Code
**审核状态**：待审核

---

## 附录：快速开始指南

### 本周可以做的3个快速改进

#### 改进1：添加快捷提问（1小时）

```vue
<!-- 在输入框上方添加 -->
<view class="quick-questions">
    <view
        v-for="q in ['推荐菜品', '我的订单', '我的收藏']"
        :key="q"
        class="question-chip"
        @tap="askQuestion(q)"
    >
        {{ q }}
    </view>
</view>
```

#### 改进2：添加加载状态（2小时）

```vue
<!-- 在AI消息中添加loading提示 -->
<view v-if="msg.loading" class="loading">
    <text class="loading-text">AI正在思考...</text>
    <view class="loading-dots">
        <view class="dot"></view>
        <view class="dot"></view>
        <view class="dot"></view>
    </view>
</view>
```

#### 改进3：添加错误提示（1小时）

```javascript
try {
    await api.call();
} catch (error) {
    uni.showModal({
        title: '操作失败',
        content: error.message || '请稍后重试',
        showCancel: false
    });
}
```

---

**联系方式**：
- 项目负责人：许佳宜
- 技术支持：Claude Code
- 问题反馈：GitHub Issues

---

**祝改进顺利！** 🚀
