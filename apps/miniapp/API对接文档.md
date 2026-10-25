# 佳食宜选 UniApp 小程序 - API对接文档

**更新时间**：2026-03-18
**版本**：v1.0.0

---

## 📋 目录

- [对接说明](#对接说明)
- [后端响应格式](#后端响应格式)
- [API路径规范](#api路径规范)
- [已对接模块](#已对接模块)
- [数据类型说明](#数据类型说明)
- [微信小程序配置](#微信小程序配置)
- [常见问题](#常见问题)

---

## 对接说明

### 1. 对接原则

- ✅ 所有API路径统一使用 `/v1/` 前缀
- ✅ 响应格式统一为 `ResponseResult` 格式
- ✅ 认证方式使用 `Bearer Token`
- ✅ 时间格式统一使用 ISO 8601 字符串
- ✅ 金额类型使用 `BigDecimal`（字符串传输）

### 2. 参考来源

小程序端API对接参考了桌面端（JasEatsChoiceFront）的配置，确保两端的请求格式、响应处理保持一致。

---

## 后端响应格式

### ResponseResult 标准格式

```typescript
{
  success: boolean,    // 业务是否成功
  code: string,        // 业务状态码（"200"表示成功）
  message: string,     // 提示信息
  data: T             // 返回数据
}
```

### 成功响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": {
    "id": "123",
    "name": "宫保鸡丁"
  }
}
```

### 失败响应示例

```json
{
  "success": false,
  "code": "400",
  "message": "请求参数错误",
  "data": null
}
```

### HTTP状态码说明

| 状态码 | 说明 | 处理方式 |
|--------|------|---------|
| 200 | 请求成功 | 检查 `success` 字段判断业务是否成功 |
| 400 | 请求参数错误 | 提示用户检查输入 |
| 401 | 未授权 | 清除token，跳转登录页 |
| 403 | 权限不足 | 提示权限不足 |
| 404 | 资源不存在 | 提示资源不存在 |
| 500+ | 服务器错误 | 提示稍后重试 |

---

## API路径规范

### 路径对比

| 模块 | 旧路径（错误） | 新路径（正确） |
|------|--------------|--------------|
| 用户 | `/api/user/login` | `/v1/user/login` |
| 订单 | `/api/order/list` | `/v1/orders` |
| 菜品 | `/api/dish/list` | `/v1/dishes` |
| 商家 | `/api/merchant/list` | `/v1/merchants` |

### RESTful 风格

```
GET    /v1/resources          # 获取列表
GET    /v1/resources/{id}     # 获取详情
POST   /v1/resources          # 创建资源
PUT    /v1/resources/{id}     # 更新资源
DELETE /v1/resources/{id}     # 删除资源
```

---

## 已对接模块

### 1. 用户模块 (`user.js`)

```javascript
import { userApi } from '@/api/modules/user'

// 用户登录
userApi.login({ phone: '13800138000', code: '1234' })

// 获取用户信息
userApi.getUserInfo(userId)

// 完善资料
userApi.completeProfile({
  userId,
  height: 175,
  weight: 70,
  goal: 'lose_weight'
})
```

**主要接口**：
- `POST /v1/user/login` - 用户登录
- `POST /v1/user/register` - 用户注册
- `POST /v1/user/send-code` - 发送验证码
- `GET /v1/users/{userId}` - 获取用户信息
- `PUT /v1/users/{userId}` - 更新用户信息
- `POST /v1/user/profile` - 完善身体数据

### 2. 订单模块 (`order.js`)

```javascript
import { orderApi } from '@/api/modules/order'

// 创建订单
orderApi.create({
  order: {
    userId,
    merchantId,
    totalAmount: 58.00,
    deliveryAddress: 'xxx',
    remark: '少辣'
  },
  dishes: [
    { dishId: '123', quantity: 2, price: 29.00 }
  ]
})

// 获取订单列表
orderApi.getByUser(userId)

// 获取订单详情
orderApi.getDetail(orderId)
```

**主要接口**：
- `POST /v1/orders` - 创建订单
- `GET /v1/orders/user/{userId}` - 获取用户订单
- `GET /v1/orders/merchant/{merchantId}` - 获取商家订单
- `GET /v1/orders/{orderId}` - 获取订单详情
- `GET /v1/orders/{orderId}/dishes` - 获取订单菜品
- `PUT /v1/orders/{orderId}/status` - 更新订单状态
- `POST /v1/orders/{orderId}/cancel` - 取消订单

### 3. 菜品模块 (`dish.js`)

```javascript
import { dishApi } from '@/api/modules/dish'

// 获取菜品列表
dishApi.getList({ merchantId, category: '川菜' })

// 获取菜品详情
dishApi.getDetail(dishId)

// 获取推荐菜品
dishApi.getRecommend({ userId, limit: 10 })
```

**主要接口**：
- `GET /v1/dishes` - 获取菜品列表
- `GET /v1/dishes/{dishId}` - 获取菜品详情
- `GET /v1/merchants/{merchantId}/dishes` - 获取商家菜品
- `GET /v1/dishes/recommended` - 获取推荐菜品
- `GET /v1/dishes/search` - 搜索菜品
- `POST /v1/dishes` - 创建菜品（商家）
- `PUT /v1/dishes/{dishId}` - 更新菜品（商家）

### 4. 商家模块 (`merchant.js`)

```javascript
import { merchantApi } from '@/api/modules/merchant'

// 获取商家列表
merchantApi.getList({ keyword: '快餐' })

// 获取商家详情
merchantApi.getDetail(merchantId)

// 获取附近商家
merchantApi.getNearby({
  latitude: 39.9,
  longitude: 116.4,
  radius: 3000
})
```

**主要接口**：
- `GET /v1/merchants` - 获取商家列表
- `GET /v1/merchants/{merchantId}` - 获取商家详情
- `GET /v1/merchants/nearby` - 获取附近商家
- `POST /v1/merchant/login` - 商家登录
- `GET /v1/merchants/{merchantId}/statistics` - 获取统计数据

---

## 数据类型说明

### 基础类型

| 类型 | 说明 | 示例 |
|------|------|------|
| String | 字符串 | `"userId"`, `"2026-03-18T10:00:00"` |
| Number | 数字 | `100`, `58.50` |
| Boolean | 布尔值 | `true`, `false` |
| Array | 数组 | `[{id: "1"}, {id: "2"}]` |
| Object | 对象 | `{id: "1", name: "xxx"}` |

### 特殊类型

#### 1. 金额类型
```json
{
  "totalAmount": "58.00"  // 使用字符串避免精度问题
}
```

#### 2. 时间类型
```json
{
  "createTime": "2026-03-18T10:30:00",  // ISO 8601格式
  "updateTime": "2026-03-18T10:30:00"
}
```

#### 3. 枚举类型
```json
{
  "orderStatus": "pending",    // 订单状态
  "paymentStatus": "paid",     // 支付状态
  "deliveryStatus": "delivering"  // 配送状态
}
```

### 订单状态枚举

| 状态值 | 说明 |
|--------|------|
| `pending` | 待确认 |
| `confirmed` | 已确认 |
| `preparing` | 制作中 |
| `ready` | 待配送 |
| `delivering` | 配送中 |
| `completed` | 已完成 |
| `cancelled` | 已取消 |
| `refunded` | 已退款 |

---

## 微信小程序配置

### 1. 服务器域名配置

在微信公众平台配置以下域名：

**request合法域名**：
```
https://api.yourdomain.com
```

**socket合法域名**：
```
wss://api.yourdomain.com
```

**uploadFile合法域名**：
```
https://api.yourdomain.com
```

**downloadFile合法域名**：
```
https://api.yourdomain.com
```

### 2. 权限配置

在 `manifest.json` 中配置所需权限：

```json
{
  "mp-weixin": {
    "permission": {
      "scope.userLocation": {
        "desc": "您的位置信息将用于推荐附近商家"
      }
    },
    "requiredPrivateInfos": [
      "getLocation",
      "chooseLocation",
      "chooseAddress"
    ]
  }
}
```

### 3. AppID 配置

1. 登录 [微信公众平台](https://mp.weixin.qq.com/)
2. 开发 -> 开发管理 -> 开发设置
3. 复制 AppID
4. 在 `manifest.json` 中配置：
   ```json
   {
     "mp-weixin": {
       "appid": "your_appid"
     }
   }
   ```

### 4. 环境切换

在 `src/config/index.js` 中切换环境：

```javascript
// 开发环境
const development = {
  baseURL: 'http://localhost:8080'
}

// 生产环境
const production = {
  baseURL: 'https://api.yourdomain.com'
}
```

---

## 常见问题

### 1. 跨域问题

**问题**：开发时请求后端接口报跨域错误

**解决**：
- 后端添加CORS配置
- 或使用代理（推荐）
- 小程序真机预览不受跨域限制

### 2. Token过期

**问题**：401错误，提示未授权

**解决**：
- `request.js` 已自动处理token过期
- 自动清除本地存储
- 自动跳转登录页

### 3. 数据格式不匹配

**问题**：后端返回的数据格式与前端期望不一致

**解决**：
- 确认后端使用 `ResponseResult` 格式
- 检查 `request.js` 响应拦截器
- 查看控制台日志定位问题

### 4. 图片上传失败

**问题**：上传图片报错

**解决**：
- 确认上传域名已配置
- 检查文件大小限制
- 确认token有效

### 5. 小程序审核

**问题**：小程序审核不通过

**检查项**：
- ✅ 服务器域名已备案
- ✅ HTTPS证书有效
- ✅ 隐私协议完整
- ✅ 用户协议完整
- ✅ 权限使用说明清晰

---

## 更新日志

### v1.0.0 (2026-03-18)

- ✅ 统一API路径为 `/v1/` 前缀
- ✅ 适配后端 `ResponseResult` 响应格式
- ✅ 更新所有API模块（user/order/dish/merchant）
- ✅ 添加环境配置（开发/生产）
- ✅ 完善错误处理机制
- ✅ 添加微信小程序配置
- ✅ 创建API对接文档

---

## 技术支持

如有问题，请联系：
- **后端API文档**：查看后端Swagger文档
- **桌面端参考**：`JasEatsChoiceFront` 项目
- **问题反馈**：提交Issue到项目仓库

---

**文档维护**：开发团队
**最后更新**：2026-03-18
