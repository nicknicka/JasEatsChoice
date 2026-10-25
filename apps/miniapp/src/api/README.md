# API 使用指南

## 快速开始

### 1. 导入 API

```javascript
// 方式一：从统一入口导入（推荐）
import { bannerApi, merchantApi, userApi } from '@/api'

// 方式二：从模块直接导入
import { bannerApi } from '@/api/modules/banner'
import { merchantApi } from '@/api/modules/merchant'
```

### 2. 调用 API

```javascript
// 获取轮播图列表
const res = await bannerApi.getList({ position: 'home' })
if (res.success) {
  banners.value = res.data
}

// 获取附近商家
const res = await merchantApi.getNearby({
  latitude: Number(location.latitude),
  longitude: Number(location.longitude),
  radius: 5000,
  limit: 10
})

// 用户登录
const res = await userApi.login({
  phone: '13800138000',
  password: '123456'
})
```

---

## URL 枚举使用

### 导入枚举

```javascript
import { BANNER_API, MERCHANT_API, buildUrl } from '@/api/urlEnum'
```

### 基础使用

```javascript
// 直接使用枚举
const url = BANNER_API.GET_LIST  // '/v1/banners'

// 构建带路径参数的 URL
const url = buildUrl(BANNER_API.GET_DETAIL, { bannerId: '123' })
// 结果: '/v1/banners/123'

// 多个路径参数
const url = buildUrl(USER_API.DELETE_FAVORITE, {
  userId: 'user123',
  targetType: 'dish',
  targetId: 'dish456'
})
// 结果: '/v1/users/user123/favorites/dish/dish456'
```

### 自定义 API 模块

```javascript
import { get, post } from '@/utils/request'
import { MY_CUSTOM_API, buildUrl } from '@/api/urlEnum'

export const customApi = {
  getList: (params) => get(MY_CUSTOM_API.GET_LIST, params),
  getDetail: (id) => get(buildUrl(MY_CUSTOM_API.GET_DETAIL, { id })),
  create: (data) => post(MY_CUSTOM_API.CREATE, data)
}
```

---

## 参数类型注意事项

### 1. 数值类型必须转换

```javascript
// ❌ 错误：传递字符串
const res = await merchantApi.getNearby({
  latitude: '31.2304',    // 字符串会导致后端参数类型错误
  longitude: '121.4737'
})

// ✅ 正确：转换为数值
const res = await merchantApi.getNearby({
  latitude: Number(31.2304),
  longitude: Number(121.4737)
})
```

### 2. 布尔类型

```javascript
// ✅ 使用布尔值
const res = await dishApi.setAvailability(dishId, true)
```

### 3. 分页参数

```javascript
// ✅ 使用数值类型
const res = await dishApi.getList({
  page: 1,
  size: 10
})
```

---

## 响应数据处理

### 标准响应格式

```javascript
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": {}
}
```

### 推荐处理方式

```javascript
try {
  const res = await bannerApi.getList({ position: 'home' })

  // 检查业务状态
  if (res.success === true || res.code === '200' || res.code === 200) {
    const data = res.data
    // 处理数据
  } else {
    // 业务失败
    console.error(res.message)
  }
} catch (error) {
  // 网络错误或异常
  console.error('请求失败:', error.message)
}
```

---

## 字段兼容处理

### ID 字段

```javascript
// 兼容多种 ID 命名
const bannerId = banner.bannerId || banner.id
const merchantId = merchant.merchantId || merchant.id
```

### 图片字段

```javascript
// 兼容多种图片字段
const image = banner.imageUrl || banner.image
const logo = merchant.avatar || merchant.logo || merchant.coverImage
```

### 评分字段

```javascript
// 兼容多种评分字段
const rating = merchant.rating || merchant.score || 0
```

---

## 错误处理

### 网络错误

request.js 已自动处理网络错误并显示 Toast 提示。

### 业务错误

```javascript
const res = await userApi.login({ phone, password })
if (!res.success) {
  // 业务失败，res.message 包含错误信息
  // request.js 已自动显示 Toast
}
```

### 401 未授权

request.js 自动清除 token 并跳转登录页。

---

## 已重构的 API 模块

- ✅ banner.js - 轮播图
- ✅ merchant.js - 商家
- ✅ user.js - 用户
- ✅ dish.js - 菜品
- ✅ order.js - 订单

### 待重构的模块

- cart.js - 购物车
- review.js - 评价
- coupon.js - 优惠券
- wallet.js - 钱包
- address.js - 地址
- ai.js - AI
- chat.js - 聊天
- conversation.js - 会话
- message.js - 消息
- notification.js - 通知
- history.js - 历史
- feedback.js - 反馈
- wishlist.js - 愿望清单
- groupOrder.js - 拼单
- recipe.js - 食谱

---

## 开发建议

### 1. 添加新 API 时使用枚举

```javascript
// 在 urlEnum.js 中添加
export const MY_NEW_API = {
  GET_LIST: '/v1/new-resource',
  GET_DETAIL: '/v1/new-resource/:id'
}

// 在模块中使用
import { MY_NEW_API, buildUrl } from '../urlEnum'

export const newApi = {
  getList: (params) => get(MY_NEW_API.GET_LIST, params),
  getDetail: (id) => get(buildUrl(MY_NEW_API.GET_DETAIL, { id }))
}
```

### 2. 保持字段对齐

参考 [前后端字段对齐检查报告.md](../../前后端字段对齐检查报告.md) 确保前后端字段一致。

### 3. 添加 JSDoc 注释

```javascript
/**
 * 获取商家详情
 * GET /v1/merchants/{merchantId}
 * @param {string} merchantId - 商家ID
 * @returns {Promise} 返回商家详情
 */
getDetail: (merchantId) => get(buildUrl(MERCHANT_API.GET_MERCHANT_DETAIL, { merchantId }))
```

---

## 常见问题

### Q: 为什么 getNearby 报错 "latitude 应是 Double 类型"？

A: 确保 latitude 和 longitude 是数值类型，使用 `Number()` 转换：
```javascript
latitude: Number(location.latitude)
```

### Q: bannerApi.getList 报错 "Cannot read property 'getList' of undefined"？

A: 确保正确导入：
```javascript
import { bannerApi } from '@/api'
```

### Q: 如何添加自定义请求头？

A: 修改 request.js，在 header 中添加：
```javascript
header: {
  'Content-Type': 'application/json',
  'X-Custom-Header': 'value'
}
```

---

**更新时间**：2026-03-28
**维护者**：Claude Code
