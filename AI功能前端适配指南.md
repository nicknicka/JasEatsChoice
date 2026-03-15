# AI功能前端适配指南

**文档版本**: v2.0 - 结构化卡片系统
**创建日期**: 2026-03-15
**作者**: Claude AI Assistant
**适用项目**: 佳食宜选 - AI智能助手前端适配

---

## 📋 核心设计理念

### 传统方式 vs 结构化卡片方式

```
❌ 传统方式（当前）：
用户点击 → 发送文本"我的订单" → AI理解 → 返回纯文本 → 前端显示文本

✅ 结构化卡片方式（目标）：
用户点击 → 发送结构化请求 → 后端识别类型 → 返回JSON数据 → 前端渲染可交互卡片
```

### 关键区别

| 维度 | 传统方式 | 结构化卡片方式 |
|-----|---------|--------------|
| **消息格式** | 纯文本 | `{type, data}` 对象 |
| **后端处理** | AI理解语义 | 直接识别类型 |
| **返回数据** | 格式化文本字符串 | 结构化JSON |
| **前端渲染** | Markdown/纯文本 | 专用交互组件 |
| **用户体验** | 静态文本展示 | 可点击、可操作的卡片 |

---

## 🎯 系统架构

### 整体流程

```
┌─────────────────────────────────────────────────────────────────┐
│                        前端 (Vue 3)                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. 用户点击"我的订单"按钮                                       │
│     ↓                                                          │
│  2. 发送结构化消息：                                            │
│     {                                                           │
│       type: "structured_query",                                 │
│       queryType: "order_list",                                  │
│       params: { status: "all" }                                 │
│     }                                                           │
│     ↓                                                          │
│  3. 后端返回结构化数据：                                        │
│     {                                                           │
│       code: 200,                                                │
│       messageType: "order_list_card",                           │
│       data: {                                                   │
│         orders: [...],                                          │
│         total: 10,                                              │
│         actions: ["cancel", "detail"]                           │
│       }                                                         │
│     }                                                           │
│     ↓                                                          │
│  4. 前端识别 messageType，渲染 OrderListCard 组件               │
│     ↓                                                          │
│  5. 用户在卡片中点击"取消订单"                                   │
│     ↓                                                          │
│  6. 触发 action 事件，重新查询或显示确认对话框                   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📦 消息数据结构设计

### 前端发送的查询消息

```javascript
// 发送给后端的结构化查询
{
  messageType: 'structured_query',  // 消息类型：结构化查询
  queryType: 'order_list',          // 查询类型
  params: {                         // 查询参数（可选）
    status: 'all',                  // 订单状态筛选
    page: 1,                        // 分页
    pageSize: 10
  },
  timestamp: 1710480000000
}
```

### 后端返回的数据格式

```javascript
// 订单列表卡片数据
{
  code: 200,
  messageType: 'order_list_card',   // 消息类型：订单列表卡片
  summary: '找到 10 个订单',          // 简短摘要（AI生成的描述）
  data: {
    total: 10,                      // 总数
    pendingCount: 2,                // 待处理数量
    orders: [                       // 订单列表
      {
        orderId: 'O20260315001',
        status: 'delivering',
        statusText: '配送中',
        dishCount: 3,
        dishNames: ['宫保鸡丁', '鱼香肉丝', '西红柿鸡蛋'],
        totalAmount: 58.00,
        createTime: '2026-03-15 12:30:00',
        canCancel: false,           // 是否可取消
        canUrge: true,              // 是否可催单
        actions: [                  // 可用操作
          {
            type: 'detail',
            text: '查看详情',
            icon: 'View'
          },
          {
            type: 'urge',
            text: '催单',
            icon: 'Bell'
          }
        ]
      }
    ],
    pagination: {                   // 分页信息
      currentPage: 1,
      pageSize: 10,
      totalPage: 1
    }
  }
}

// 收藏列表卡片数据
{
  code: 200,
  messageType: 'favorite_list_card',
  summary: '您收藏了 15 个菜品',
  data: {
    total: 15,
    favorites: [
      {
        dishId: 'D001',
        dishName: '宫保鸡丁',
        imageUrl: 'https://...',
        price: 28.00,
        rating: 4.8,
        salesCount: 1234,
        tags: ['川菜', '微辣'],
        collectionTime: '2026-03-10',
        actions: [
          {
            type: 'add_to_cart',
            text: '加入购物车',
            icon: 'ShoppingCart'
          },
          {
            type: 'remove_favorite',
            text: '取消收藏',
            icon: 'Delete'
          }
        ]
      }
    ]
  }
}

// 评价列表卡片数据
{
  code: 200,
  messageType: 'review_list_card',
  summary: '您发布了 8 条评价',
  data: {
    total: 8,
    avgRating: 4.5,
    reviews: [
      {
        reviewId: 'R001',
        orderId: 'O20260315001',
        dishName: '宫保鸡丁',
        dishImage: 'https://...',
        rating: 5,
        content: '味道很好，推荐！',
        images: ['url1', 'url2'],
        createTime: '2026-03-14',
        merchantReply: '感谢您的评价！',
        actions: [
          {
            type: 'view_detail',
            text: '查看详情',
            icon: 'View'
          },
          {
            type: 'delete',
            text: '删除评价',
            icon: 'Delete'
          }
        ]
      }
    ]
  }
}

// 用户信息卡片数据
{
  code: 200,
  messageType: 'user_info_card',
  summary: '这是您的个人信息档案',
  data: {
    // 基本信息
    basicInfo: {
      nickname: '美食达人',
      phone: '138****5678',
      email: 'user@example.com',
      location: '北京市',
      gender: '女',
      avatar: 'https://...',
      registerTime: '2025-01-15'
    },
    // 身体数据
    bodyData: {
      height: 165,
      weight: 55,
      bmi: 20.2,
      bmiStatus: 'normal',
      bmiText: '正常'
    },
    // 饮食偏好
    preferences: {
      dietGoal: '保持健康',
      allergies: ['花生', '芒果'],
      tags: ['川菜', '清淡', '低糖']
    },
    // 账户状态
    accountStatus: {
      hasPaymentPassword: true,
      isMerchant: false
    },
    // 可用操作
    actions: [
      {
        type: 'edit_profile',
        text: '编辑资料',
        icon: 'Edit'
      },
      {
        type: 'view_health',
        text: '健康分析',
        icon: 'TrendCharts'
      }
    ]
  }
}
```

---

## 🧩 前端实现

### 1. 消息类型枚举

```javascript
// src/constants/messageTypes.js
export const MessageTypes = {
  // 用户发送的消息类型
  TEXT: 'text',                          // 纯文本消息（AI对话）
  STRUCTURED_QUERY: 'structured_query',  // 结构化查询

  // 后端返回的卡片类型
  ORDER_LIST_CARD: 'order_list_card',           // 订单列表卡片
  FAVORITE_LIST_CARD: 'favorite_list_card',     // 收藏列表卡片
  REVIEW_LIST_CARD: 'review_list_card',         // 评价列表卡片
  COUPON_LIST_CARD: 'coupon_list_card',         // 优惠券列表卡片
  USER_INFO_CARD: 'user_info_card',             // 用户信息卡片
  DISH_LIST_CARD: 'dish_list_card',             // 菜品列表卡片
  NUTRITION_DATA_CARD: 'nutrition_data_card',   // 营养数据卡片
  HEALTH_ADVICE_CARD: 'health_advice_card',     // 健康建议卡片
}

export const QueryTypes = {
  // 订单相关
  ORDER_LIST: 'order_list',
  ORDER_DETAIL: 'order_detail',
  CANCEL_ORDER: 'cancel_order',
  URGE_ORDER: 'urge_order',

  // 收藏相关
  FAVORITE_LIST: 'favorite_list',
  ADD_FAVORITE: 'add_favorite',
  REMOVE_FAVORITE: 'remove_favorite',

  // 评价相关
  REVIEW_LIST: 'review_list',
  USER_REVIEWS: 'user_reviews',

  // 优惠券相关
  COUPON_LIST: 'coupon_list',

  // 用户信息相关
  USER_INFO: 'user_info',
  USER_PREFERENCES: 'user_preferences',

  // 菜品相关
  DISH_LIST: 'dish_list',
  DISH_DETAIL: 'dish_detail',

  // 营养健康相关
  NUTRITION_DATA: 'nutrition_data',
  HEALTH_ADVICE: 'health_advice',
  BMI_CALCULATE: 'bmi_calculate'
}
```

### 2. 快捷操作按钮组件

```vue
<!-- src/components/QuickActions.vue -->
<template>
  <div class="quick-actions">
    <div class="actions-grid">
      <div
        v-for="action in quickActions"
        :key="action.id"
        class="action-card"
        @click="handleAction(action)"
      >
        <div class="action-icon" :style="{ backgroundColor: action.color }">
          <el-icon :size="24">
            <component :is="action.icon" />
          </el-icon>
        </div>
        <div class="action-content">
          <div class="action-title">{{ action.title }}</div>
          <div class="action-desc">{{ action.desc }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { DocumentCopy, Star, ChatLineRound, Ticket, User, TrendCharts } from '@element-plus/icons-vue'

const emit = defineEmits(['query'])

const quickActions = [
  {
    id: 'orders',
    title: '我的订单',
    desc: '查看订单状态',
    icon: DocumentCopy,
    color: '#409EFF',
    queryType: 'order_list'
  },
  {
    id: 'favorites',
    title: '我的收藏',
    desc: '收藏的菜品',
    icon: Star,
    color: '#F59E0B',
    queryType: 'favorite_list'
  },
  {
    id: 'reviews',
    title: '我的评价',
    desc: '评价记录',
    icon: ChatLineRound,
    color: '#10B981',
    queryType: 'review_list'
  },
  {
    id: 'coupons',
    title: '优惠券',
    desc: '可用优惠券',
    icon: Ticket,
    color: '#EF4444',
    queryType: 'coupon_list'
  },
  {
    id: 'userinfo',
    title: '个人信息',
    desc: '我的资料',
    icon: User,
    color: '#8B5CF6',
    queryType: 'user_info'
  },
  {
    id: 'nutrition',
    title: '健康分析',
    desc: '营养摄入',
    icon: TrendCharts,
    color: '#EC4899',
    queryType: 'nutrition_data'
  }
]

const handleAction = (action) => {
  emit('query', {
    type: 'structured_query',
    queryType: action.queryType,
    params: {}
  })
}
</script>

<style scoped lang="less">
.quick-actions {
  padding: 16px 0;

  .actions-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
    gap: 12px;
  }

  .action-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    background: #f9f9f9;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      background: #f0f0f0;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0,0,0,0.08);
    }

    .action-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
    }

    .action-content {
      flex: 1;

      .action-title {
        font-weight: 600;
        font-size: 14px;
        color: #333;
        margin-bottom: 4px;
      }

      .action-desc {
        font-size: 12px;
        color: #999;
      }
    }
  }
}
</style>
```

### 3. 消息发送逻辑

```javascript
// src/composables/useStructuredQuery.js
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

export function useStructuredQuery() {
  const loading = ref(false)

  // 发送结构化查询
  const sendStructuredQuery = async (query) => {
    loading.value = true

    try {
      // 发送结构化查询消息
      const response = await axios.post('/api/ai/chat', {
        messageType: 'structured_query',
        queryType: query.queryType,
        params: query.params || {},
        timestamp: Date.now()
      })

      // 返回结构化数据
      return {
        success: true,
        messageType: response.data.messageType,
        summary: response.data.summary,
        data: response.data.data
      }
    } catch (error) {
      ElMessage.error('查询失败：' + (error.message || '未知错误'))
      return {
        success: false,
        error: error.message
      }
    } finally {
      loading.value = false
    }
  }

  // 处理卡片操作
  const handleCardAction = async (actionType, payload) => {
    loading.value = true

    try {
      // 根据操作类型调用不同的API
      let response
      switch (actionType) {
        case 'cancel_order':
          response = await axios.post('/api/order/cancel', payload)
          break
        case 'urge_order':
          response = await axios.post('/api/order/urge', payload)
          break
        case 'remove_favorite':
          response = await axios.delete(`/api/favorite/${payload.dishId}`)
          break
        case 'add_to_cart':
          response = await axios.post('/api/cart/add', payload)
          break
        // ... 其他操作
        default:
          throw new Error('未知操作类型：' + actionType)
      }

      ElMessage.success('操作成功')
      return { success: true, data: response.data }
    } catch (error) {
      ElMessage.error('操作失败：' + (error.message || '未知错误'))
      return { success: false, error: error.message }
    } finally {
      loading.value = false
    }
  }

  return {
    loading,
    sendStructuredQuery,
    handleCardAction
  }
}
```

### 4. 卡片组件映射系统

```vue
<!-- src/components/message/CardMessage.vue -->
<template>
  <div class="card-message">
    <!-- 根据消息类型渲染不同的卡片组件 -->
    <component
      :is="cardComponent"
      :data="message.data"
      :summary="message.summary"
      @action="handleAction"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import {
  MessageTypes,
  getMessageCardComponent
} from '@/utils/messageCardMapper'

const props = defineProps({
  message: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['action'])

// 根据消息类型获取对应的卡片组件
const cardComponent = computed(() => {
  return getMessageCardComponent(props.message.messageType)
})

// 处理卡片中的操作
const handleAction = (actionType, payload) => {
  emit('action', actionType, payload)
}
</script>
```

```javascript
// src/utils/messageCardMapper.js
import OrderListCard from '@/components/cards/OrderListCard.vue'
import FavoriteListCard from '@/components/cards/FavoriteListCard.vue'
import ReviewListCard from '@/components/cards/ReviewListCard.vue'
import CouponListCard from '@/components/cards/CouponListCard.vue'
import UserInfoCard from '@/components/cards/UserInfoCard.vue'
import DishListCard from '@/components/cards/DishListCard.vue'
import NutritionDataCard from '@/components/cards/NutritionDataCard.vue'

const cardComponentMap = {
  [MessageTypes.ORDER_LIST_CARD]: OrderListCard,
  [MessageTypes.FAVORITE_LIST_CARD]: FavoriteListCard,
  [MessageTypes.REVIEW_LIST_CARD]: ReviewListCard,
  [MessageTypes.COUPON_LIST_CARD]: CouponListCard,
  [MessageTypes.USER_INFO_CARD]: UserInfoCard,
  [MessageTypes.DISH_LIST_CARD]: DishListCard,
  [MessageTypes.NUTRITION_DATA_CARD]: NutritionDataCard
}

export function getMessageCardComponent(messageType) {
  return cardComponentMap[messageType] || null
}
```

### 5. 订单列表卡片组件

```vue
<!-- src/components/cards/OrderListCard.vue -->
<template>
  <div class="order-list-card">
    <!-- 摘要信息 -->
    <div class="card-summary">
      <el-icon><DocumentCopy /></el-icon>
      <span>{{ summary }}</span>
    </div>

    <!-- 统计标签 -->
    <div class="card-stats">
      <el-tag v-if="data.pendingCount > 0" type="warning" size="small">
        待处理: {{ data.pendingCount }}
      </el-tag>
      <el-tag type="info" size="small">
        共 {{ data.total }} 个订单
      </el-tag>
    </div>

    <!-- 订单列表 -->
    <div class="order-list">
      <div
        v-for="order in data.orders"
        :key="order.orderId"
        class="order-item"
      >
        <!-- 订单头部 -->
        <div class="order-header">
          <div class="order-id">订单 #{{ order.orderId }}</div>
          <el-tag :type="getStatusType(order.status)" size="small">
            {{ order.statusText }}
          </el-tag>
        </div>

        <!-- 订单内容 -->
        <div class="order-content">
          <div class="order-dishes">
            <el-tag
              v-for="(name, index) in order.dishNames.slice(0, 3)"
              :key="index"
              size="small"
              type="info"
            >
              {{ name }}
            </el-tag>
            <span v-if="order.dishNames.length > 3" class="more-dishes">
              等{{ order.dishCount }}道菜
            </span>
          </div>

          <div class="order-meta">
            <span class="order-amount">￥{{ order.totalAmount.toFixed(2) }}</span>
            <span class="order-time">{{ formatTime(order.createTime) }}</span>
          </div>
        </div>

        <!-- 订单操作 -->
        <div class="order-actions">
          <el-button
            v-for="action in order.actions"
            :key="action.type"
            :icon="action.icon"
            size="small"
            :type="getButtonType(action.type)"
            link
            @click="handleAction(action.type, order)"
          >
            {{ action.text }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 分页（如果有多页） -->
    <div v-if="data.pagination && data.pagination.totalPage > 1" class="card-pagination">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="data.pagination.pageSize"
        :total="data.total"
        layout="prev, pager, next"
        small
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { DocumentCopy } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const props = defineProps({
  data: {
    type: Object,
    required: true
  },
  summary: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['action'])

const currentPage = ref(1)

const getStatusType = (status) => {
  const typeMap = {
    'pending_payment': 'warning',
    'pending_confirmation': 'info',
    'preparing': 'primary',
    'delivering': 'success',
    'completed': 'info',
    'cancelled': 'danger'
  }
  return typeMap[status] || 'info'
}

const getButtonType = (actionType) => {
  const buttonTypeMap = {
    'cancel': 'danger',
    'urge': 'warning',
    'detail': 'primary',
    'reorder': 'success'
  }
  return buttonTypeMap[actionType] || 'default'
}

const formatTime = (time) => {
  // 简单的日期格式化
  return time.split(' ')[0] // 只显示日期部分
}

const handleAction = async (actionType, order) => {
  // 如果是取消操作，需要确认
  if (actionType === 'cancel') {
    try {
      await ElMessageBox.confirm(
        `确定要取消订单 #${order.orderId} 吗？`,
        '确认取消',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    } catch {
      return // 用户取消
    }
  }

  // 触发操作事件
  emit('action', actionType, {
    orderId: order.orderId,
    ...order
  })
}

const handlePageChange = (page) => {
  // 触发分页查询
  emit('action', 'page_change', { page })
}
</script>

<style scoped lang="less">
.order-list-card {
  .card-summary {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    font-weight: 600;
    color: #333;
  }

  .card-stats {
    display: flex;
    gap: 8px;
    margin-bottom: 16px;
  }

  .order-list {
    .order-item {
      background: #f9f9f9;
      border-radius: 12px;
      padding: 12px;
      margin-bottom: 12px;
      transition: all 0.3s;

      &:hover {
        background: #f5f5f5;
        box-shadow: 0 2px 8px rgba(0,0,0,0.06);
      }

      .order-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .order-id {
          font-weight: 600;
          color: #333;
        }
      }

      .order-content {
        margin-bottom: 8px;

        .order-dishes {
          display: flex;
          gap: 4px;
          align-items: center;
          margin-bottom: 8px;
          flex-wrap: wrap;

          .more-dishes {
            font-size: 12px;
            color: #999;
          }
        }

        .order-meta {
          display: flex;
          justify-content: space-between;
          color: #666;
          font-size: 14px;

          .order-amount {
            color: #ff6b6b;
            font-weight: 600;
          }

          .order-time {
            font-size: 12px;
            color: #999;
          }
        }
      }

      .order-actions {
        padding-top: 8px;
        border-top: 1px solid #eee;
        display: flex;
        gap: 12px;
      }
    }
  }

  .card-pagination {
    display: flex;
    justify-content: center;
    margin-top: 16px;
  }
}
</style>
```

### 6. 用户信息卡片组件

```vue
<!-- src/components/cards/UserInfoCard.vue -->
<template>
  <div class="user-info-card">
    <!-- 摘要 -->
    <div class="card-summary">
      <el-icon><User /></el-icon>
      <span>{{ summary }}</span>
    </div>

    <!-- 用户信息布局 -->
    <div class="user-info-layout">
      <!-- 左侧：头像和基本信息 -->
      <div class="user-basic">
        <el-avatar :size="80" :src="data.basicInfo.avatar">
          {{ data.basicInfo.nickname?.charAt(0) }}
        </el-avatar>
        <div class="user-name">{{ data.basicInfo.nickname }}</div>
        <div class="user-phone">{{ data.basicInfo.phone }}</div>
      </div>

      <!-- 右侧：详细信息 -->
      <div class="user-details">
        <!-- 基本信息 -->
        <div class="info-section">
          <div class="section-title">📋 基本信息</div>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">邮箱：</span>
              <span class="value">{{ data.basicInfo.email || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">地区：</span>
              <span class="value">{{ data.basicInfo.location || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">性别：</span>
              <span class="value">{{ data.basicInfo.gender || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">注册时间：</span>
              <span class="value">{{ data.basicInfo.registerTime }}</span>
            </div>
          </div>
        </div>

        <!-- 身体数据 -->
        <div class="info-section">
          <div class="section-title">💪 身体数据</div>
          <div class="body-data-grid">
            <div class="body-data-item">
              <div class="data-value">{{ data.bodyData.height }} cm</div>
              <div class="data-label">身高</div>
            </div>
            <div class="body-data-item">
              <div class="data-value">{{ data.bodyData.weight }} kg</div>
              <div class="data-label">体重</div>
            </div>
            <div class="body-data-item">
              <div class="data-value" :class="getBmiClass(data.bodyData.bmiStatus)">
                {{ data.bodyData.bmi }}
              </div>
              <div class="data-label">BMI ({{ data.bodyData.bmiText }})</div>
            </div>
          </div>
        </div>

        <!-- 饮食偏好 -->
        <div class="info-section">
          <div class="section-title">🍽️ 饮食偏好</div>
          <div class="preferences">
            <div class="preference-item">
              <span class="label">饮食目标：</span>
              <el-tag type="success" size="small">
                {{ data.preferences.dietGoal || '未设置' }}
              </el-tag>
            </div>
            <div v-if="data.preferences.allergies?.length" class="preference-item">
              <span class="label">过敏食材：</span>
              <el-tag
                v-for="allergy in data.preferences.allergies"
                :key="allergy"
                type="danger"
                size="small"
              >
                {{ allergy }}
              </el-tag>
            </div>
            <div v-if="data.preferences.tags?.length" class="preference-item">
              <span class="label">偏好标签：</span>
              <el-tag
                v-for="tag in data.preferences.tags"
                :key="tag"
                type="warning"
                size="small"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="card-actions">
      <el-button
        v-for="action in data.actions"
        :key="action.type"
        :icon="action.icon"
        type="primary"
        size="small"
        @click="handleAction(action.type)"
      >
        {{ action.text }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { User } from '@element-plus/icons-vue'

const props = defineProps({
  data: {
    type: Object,
    required: true
  },
  summary: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['action'])

const getBmiClass = (status) => {
  const classMap = {
    'underweight': 'bmi-under',
    'normal': 'bmi-normal',
    'overweight': 'bmi-over',
    'obese': 'bmi-obese'
  }
  return classMap[status] || ''
}

const handleAction = (actionType) => {
  emit('action', actionType, {})
}
</script>

<style scoped lang="less">
.user-info-card {
  .card-summary {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 16px;
    font-weight: 600;
    color: #333;
  }

  .user-info-layout {
    display: flex;
    gap: 20px;
    margin-bottom: 16px;

    .user-basic {
      text-align: center;
      padding: 16px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 12px;
      color: #fff;
      min-width: 140px;

      .user-name {
        font-size: 18px;
        font-weight: 600;
        margin: 12px 0 4px;
      }

      .user-phone {
        font-size: 12px;
        opacity: 0.9;
      }
    }

    .user-details {
      flex: 1;

      .info-section {
        margin-bottom: 16px;

        .section-title {
          font-weight: 600;
          margin-bottom: 8px;
          color: #333;
        }

        .info-grid {
          display: grid;
          grid-template-columns: repeat(2, 1fr);
          gap: 8px;

          .info-item {
            font-size: 14px;
            color: #666;

            .label {
              color: #999;
            }
          }
        }

        .body-data-grid {
          display: flex;
          gap: 16px;

          .body-data-item {
            text-align: center;
            padding: 12px 16px;
            background: #f9f9f9;
            border-radius: 8px;

            .data-value {
              font-size: 20px;
              font-weight: 700;
              color: #333;
              margin-bottom: 4px;

              &.bmi-normal { color: #67c23a; }
              &.bmi-under { color: #909399; }
              &.bmi-over { color: #e6a23c; }
              &.bmi-obese { color: #f56c6c; }
            }

            .data-label {
              font-size: 12px;
              color: #999;
            }
          }
        }

        .preferences {
          .preference-item {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 8px;
            font-size: 14px;

            .label {
              color: #666;
              min-width: 80px;
            }
          }
        }
      }
    }
  }

  .card-actions {
    display: flex;
    gap: 8px;
    padding-top: 16px;
    border-top: 1px solid #eee;
  }
}
</style>
```

---

## 🔧 后端适配

### 1. 修改AI聊天接口

```java
/**
 * AI聊天接口 - 支持结构化查询
 */
@PostMapping("/api/ai/chat")
public ResponseEntity<?> chat(
    @RequestBody Map<String, Object> request,
    @RequestHeader("Authorization") String token
) {
    String userId = jwtUtil.extractUserId(token.substring(7));

    // 判断是否为结构化查询
    String messageType = (String) request.get("messageType");
    if ("structured_query".equals(messageType)) {
        return handleStructuredQuery(request, userId);
    }

    // 普通文本消息（原有逻辑）
    String message = (String) request.get("message");
    return handleTextMessage(message, userId);
}

/**
 * 处理结构化查询
 */
private ResponseEntity<?> handleStructuredQuery(
    Map<String, Object> request,
    String userId
) {
    String queryType = (String) request.get("queryType");
    Map<String, Object> params = (Map<String, Object>) request.get("params");

    // 根据查询类型调用不同的Service
    Map<String, Object> result = new HashMap<>();

    switch (queryType) {
        case "order_list":
            result = getOrderListCard(userId, params);
            break;
        case "favorite_list":
            result = getFavoriteListCard(userId, params);
            break;
        case "review_list":
            result = getReviewListCard(userId, params);
            break;
        case "coupon_list":
            result = getCouponListCard(userId, params);
            break;
        case "user_info":
            result = getUserInfoCard(userId, params);
            break;
        // ... 其他查询类型
        default:
            throw new IllegalArgumentException("未知的查询类型：" + queryType);
    }

    return ResponseEntity.ok(result);
}

/**
 * 获取订单列表卡片数据
 */
private Map<String, Object> getOrderListCard(
    String userId,
    Map<String, Object> params
) {
    // 查询订单列表
    List<Order> orders = orderService.getUserOrders(userId);

    // 构建卡片数据
    List<Map<String, Object>> orderData = orders.stream()
        .map(order -> {
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("orderId", order.getOrderId());
            orderMap.put("status", order.getStatus());
            orderMap.put("statusText", getStatusText(order.getStatus()));
            orderMap.put("dishCount", order.getDishItems().size());
            orderMap.put("dishNames", extractDishNames(order));
            orderMap.put("totalAmount", order.getTotalAmount());
            orderMap.put("createTime", order.getCreateTime().toString());
            orderMap.put("canCancel", canCancelOrder(order));
            orderMap.put("canUrge", canUrgeOrder(order));

            // 添加操作按钮
            List<Map<String, String>> actions = new ArrayList<>();
            actions.add(Map.of("type", "detail", "text", "查看详情", "icon", "View"));
            if (canCancelOrder(order)) {
                actions.add(Map.of("type", "cancel", "text", "取消订单", "icon", "Delete"));
            }
            if (canUrgeOrder(order)) {
                actions.add(Map.of("type", "urge", "text", "催单", "icon", "Bell"));
            }
            orderMap.put("actions", actions);

            return orderMap;
        })
        .collect(Collectors.toList());

    // 构建返回数据
    Map<String, Object> data = new HashMap<>();
    data.put("messageType", "order_list_card");
    data.put("summary", "找到 " + orders.size() + " 个订单");
    data.put("data", Map.of(
        "total", orders.size(),
        "pendingCount", orders.stream().filter(o -> "pending".equals(o.getStatus())).count(),
        "orders", orderData
    ));

    return data;
}

/**
 * 获取用户信息卡片数据
 */
private Map<String, Object> getUserInfoCard(
    String userId,
    Map<String, Object> params
) {
    User user = userService.getById(userId);

    // 构建基本信息
    Map<String, Object> basicInfo = new HashMap<>();
    basicInfo.put("nickname", user.getNickname());
    basicInfo.put("phone", maskPhone(user.getPhone()));
    basicInfo.put("email", user.getEmail());
    basicInfo.put("location", user.getLocation());
    basicInfo.put("gender", user.getGender());
    basicInfo.put("avatar", user.getAvatar());
    basicInfo.put("registerTime", user.getCreateTime().toString().split(" ")[0]);

    // 构建身体数据
    Map<String, Object> bodyData = new HashMap<>();
    bodyData.put("height", user.getHeight());
    bodyData.put("weight", user.getWeight());

    if (user.getHeight() != null && user.getWeight() != null) {
        double bmi = calculateBMI(user.getHeight(), user.getWeight());
        bodyData.put("bmi", String.format("%.1f", bmi));
        bodyData.put("bmiStatus", getBMIStatus(bmi));
        bodyData.put("bmiText", getBMIStatusText(bmi));
    }

    // 构建饮食偏好
    Map<String, Object> preferences = new HashMap<>();
    preferences.put("dietGoal", user.getDietGoal());

    if (user.getAllergies() != null) {
        List<String> allergies = extractAllergies(user.getAllergies());
        preferences.put("allergies", allergies);
    }

    if (user.getPreferTags() != null) {
        List<String> tags = extractTags(user.getPreferTags());
        preferences.put("tags", tags);
    }

    // 账户状态
    Map<String, Object> accountStatus = new HashMap<>();
    accountStatus.put("hasPaymentPassword", user.getHasPaymentPassword());
    accountStatus.put("isMerchant", user.getMerchantId() != null);

    // 操作按钮
    List<Map<String, String>> actions = new ArrayList<>();
    actions.add(Map.of("type", "edit_profile", "text", "编辑资料", "icon", "Edit"));
    actions.add(Map.of("type", "view_health", "text", "健康分析", "icon", "TrendCharts"));

    // 构建返回数据
    Map<String, Object> data = new HashMap<>();
    data.put("messageType", "user_info_card");
    data.put("summary", "这是您的个人信息档案");
    data.put("data", Map.of(
        "basicInfo", basicInfo,
        "bodyData", bodyData,
        "preferences", preferences,
        "accountStatus", accountStatus,
        "actions", actions
    ));

    return data;
}
```

---

## 📋 实施步骤

### 阶段一：基础架构搭建（2-3天）

**目标**：建立消息类型系统和卡片组件映射

- [x] 创建消息类型枚举
- [x] 创建查询类型枚举
- [ ] 实现卡片组件映射系统
- [ ] 修改消息渲染逻辑，支持卡片组件
- [ ] 创建快捷操作按钮组件

### 阶段二：后端适配（3-4天）

**目标**：修改后端接口，支持结构化查询

- [ ] 修改AI聊天接口，识别结构化查询
- [ ] 实现各个查询类型的数据组装方法
- [ ] 确保数据格式符合前端卡片组件要求
- [ ] 添加操作处理接口（取消订单、催单等）

### 阶段三：卡片组件开发（5-7天）

**目标**：实现所有卡片组件

- [ ] OrderListCard 组件（1天）
- [ ] FavoriteListCard 组件（1天）
- [ ] ReviewListCard 组件（1天）
- [ ] CouponListCard 组件（1天）
- [ ] UserInfoCard 组件（1天）
- [ ] DishListCard 组件（1天）
- [ ] NutritionDataCard 组件（0.5天）
- [ ] 测试和优化（0.5天）

### 阶段四：集成测试（2-3天）

**目标**：端到端测试

- [ ] 功能测试（所有查询类型）
- [ ] 操作测试（取消订单、收藏等）
- [ ] 兼容性测试
- [ ] 性能测试
- [ ] Bug修复

---

## 🎯 关键优势

### 1. 用户体验提升

- ✅ 可视化数据展示，信息更直观
- ✅ 一键操作，无需输入命令
- ✅ 实时交互，操作反馈及时

### 2. 开发效率提高

- ✅ 统一的数据格式
- ✅ 组件化开发，可复用
- ✅ 前后端职责清晰

### 3. 系统扩展性强

- ✅ 新增卡片类型只需添加组件
- ✅ 查询类型易于扩展
- ✅ 支持渐进式开发

---

**文档结束**

如有疑问，请参考项目技术文档或联系开发团队。
