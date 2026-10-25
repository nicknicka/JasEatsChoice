# 业务组件使用指南

本目录存放跨页面复用的业务组件。

## 组件列表

### 1. DishCard - 菜品卡片

**使用场景**：首页、收藏页、商家详情页、搜索页、订单确认页

**示例**：
```vue
<template>
  <DishCard
    :dish="dishData"
    :showMerchant="true"
    :showAddBtn="true"
    @click="handleDishClick"
    @favorite="handleFavorite"
    @add="handleAddToCart"
  />
</template>

<script setup>
import DishCard from '@/components/business/DishCard.vue'

const dishData = {
  id: 1,
  name: '宫保鸡丁',
  image: 'https://...',
  price: 28,
  monthlySales: 999,
  tags: ['辣味', '川菜'],
  isFavorite: false,
  merchantName: '川味轩'
}
</script>
```

**Props**：
- `dish` (Object, 必填) - 菜品数据
- `showMerchant` (Boolean) - 是否显示商家信息，默认 false
- `showAddBtn` (Boolean) - 是否显示加购按钮，默认 false

**Events**：
- `click` - 点击卡片
- `favorite` - 点击收藏按钮
- `add` - 点击加购按钮

---

### 2. MerchantCard - 商家卡片

**使用场景**：首页、收藏页、搜索页、附近商家

**示例**：
```vue
<template>
  <MerchantCard
    :merchant="merchantData"
    @click="handleMerchantClick"
    @favorite="handleFavorite"
  />
</template>

<script setup>
import MerchantCard from '@/components/business/MerchantCard.vue'

const merchantData = {
  id: 1,
  name: '川味轩',
  image: 'https://...',
  rating: 4.8,
  reviewCount: 999,
  tags: ['川菜', '辣味'],
  description: '正宗川菜，地道风味',
  address: 'xx路xx号',
  distance: '1.2km',
  isFavorite: false
}
</script>
```

**Props**：
- `merchant` (Object, 必填) - 商家数据

**Events**：
- `click` - 点击卡片
- `favorite` - 点击收藏按钮

---

### 3. CouponCard - 优惠券卡片

**使用场景**：我的优惠券、订单确认页

**示例**：
```vue
<template>
  <CouponCard
    :coupon="couponData"
    @use="handleUseCoupon"
  />
</template>

<script setup>
import CouponCard from '@/components/business/CouponCard.vue'

const couponData = {
  id: 1,
  name: '新用户专享券',
  amount: 10,
  conditionText: '满50可用',
  timeText: '2024.01.01-2024.12.31',
  status: 'available' // available, used, expired, received
}
</script>
```

**Props**：
- `coupon` (Object, 必填) - 优惠券数据

**Events**：
- `use` - 使用优惠券

---

### 4. OrderCard - 订单卡片

**使用场景**：订单列表、订单详情

**示例**：
```vue
<template>
  <OrderCard
    :order="orderData"
    @click="handleOrderClick"
    @action="handleOrderAction"
  />
</template>

<script setup>
import OrderCard from '@/components/business/OrderCard.vue'

const orderData = {
  id: 1,
  merchantName: '川味轩',
  status: 'delivering', // pending, confirmed, preparing, delivering, completed, cancelled
  statusText: '配送中',
  timeText: '今天 12:30',
  items: [
    { name: '宫保鸡丁', image: 'https://...', quantity: 1 },
    { name: '麻婆豆腐', image: 'https://...', quantity: 2 }
  ],
  totalAmount: 58,
  actions: [
    { text: '联系骑手', type: 'contact_rider', class: 'secondary' },
    { text: '查看详情', type: 'detail', class: 'outline' }
  ]
}
</script>
```

**Props**：
- `order` (Object, 必填) - 订单数据

**Events**：
- `click` - 点击卡片
- `action` - 点击操作按钮

---

### 5. ReviewItem - 评价项

**使用场景**：评价列表、菜品详情

**示例**：
```vue
<template>
  <ReviewItem
    :review="reviewData"
    :showDish="true"
    @click="handleReviewClick"
  />
</template>

<script setup>
import ReviewItem from '@/components/business/ReviewItem.vue'

const reviewData = {
  id: 1,
  userName: '美食家',
  userAvatar: 'https://...',
  rating: 5,
  content: '味道很棒，分量足，会再次购买！',
  images: ['https://...', 'https://...'],
  timeText: '2024-03-15',
  tags: ['味道赞', '分量足', '配送快'],
  dishName: '宫保鸡丁',
  merchantReply: '感谢您的评价，期待您的下次光临！',
  replyTime: '2024-03-16'
}
</script>
```

**Props**：
- `review` (Object, 必填) - 评价数据
- `showDish` (Boolean) - 是否显示菜品信息，默认 false

**Events**：
- `click` - 点击评价

---

### 6. NutritionBar - 营养成分条

**使用场景**：食谱详情、卡路里管理、健康分析

**示例**：
```vue
<template>
  <NutritionBar
    label="蛋白质"
    icon="🥩"
    :current="65"
    :target="80"
    unit="g"
    color="#FF6B35"
    :showPercent="true"
  />
</template>

<script setup>
import NutritionBar from '@/components/business/NutritionBar.vue'
</script>
```

**Props**：
- `label` (String, 必填) - 标签文字
- `icon` (String) - 图标
- `current` (Number/String, 必填) - 当前值
- `target` (Number/String) - 目标值
- `unit` (String) - 单位
- `color` (String) - 进度条颜色，默认 '#FF6B35'
- `showValue` (Boolean) - 是否显示数值，默认 true
- `showPercent` (Boolean) - 是否显示百分比，默认 false
- `showShine` (Boolean) - 是否显示光泽动画，默认 true
- `direction` (String) - 方向：'horizontal'（横向）或 'vertical'（纵向），默认 'horizontal'

**特性**：
- 自动计算百分比
- 根据百分比自动变色（低/中/高/完成）
- 支持横向和纵向两种布局
- 流畅的动画效果

---

## 通用规则

### 命名规范
- 组件名使用大驼峰：`DishCard`
- 组件文件使用大驼峰：`DishCard.vue`
- 事件名使用小驼峰：`@handleClick`

### Props 设计原则
- 必填参数明确标注
- 提供合理的默认值
- 使用类型检查提高代码质量

### Events 设计原则
- 事件名语义化
- 传递相关数据作为参数
- 使用 `@click.stop` 阻止事件冒泡

### 样式规范
- 使用全局变量和 mixins
- 保持设计系统一致性
- 添加 active 状态反馈

---

## 在页面中使用

### 方式1：导入使用
```vue
<script setup>
import DishCard from '@/components/business/DishCard.vue'
</script>
```

### 方式2：全局自动注册（推荐）

在 `pages.json` 中配置 easycom：
```json
{
  "easycom": {
    "autoscan": true,
    "custom": {
      "^Biz(.*)": "@/components/business/Biz$1.vue"
    }
  }
}
```

然后在页面中直接使用：
```vue
<template>
  <BizDishCard :dish="dishData" />
</template>
```

---

## 添加新的业务组件

当需要在多个页面使用相同的UI模式时，按以下步骤添加：

1. 在本目录创建组件文件
2. 实现组件逻辑和样式
3. 在此 README 中添加文档
4. 在需要的页面中引入使用

**判断标准**：
- ✅ 在 2个或以上页面使用 → 创建业务组件
- ❌ 只在单个页面使用 → 放在页面目录下的 components 文件夹
