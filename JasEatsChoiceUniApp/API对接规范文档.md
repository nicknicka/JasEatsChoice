# 佳食宜选 API对接规范文档

**版本**: v1.0.0
**更新时间**: 2026-03-20
**适用范围**: UniApp前端、桌面端前端

---

## 📋 目录

- [响应格式规范](#响应格式规范)
- [请求格式规范](#请求格式规范)
- [支付接口规范](#支付接口规范)
- [错误处理规范](#错误处理规范)
- [字段类型规范](#字段类型规范)

---

## 响应格式规范

### ResponseResult 标准格式

后端统一使用 `ResponseResult<T>` 格式返回数据：

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

### 前端响应处理

**request.js 响应拦截器**（已修复）：

```javascript
// src/utils/request.js
if (response.statusCode >= 200 && response.statusCode < 300) {
  const response = res.data

  // 检查业务状态
  if (response.success === true || response.code === '200' || response.code === 200) {
    // ✅ 返回完整response对象
    resolve(response)
  } else if (response.success === false) {
    // 业务失败
    reject({ message: response.message, code: response.code })
  }
}
```

**页面代码使用方式**：

```javascript
// ✅ 正确用法
const res = await api.getData()
if (res.code === 200 || res.code === '200') {
  const data = res.data  // 从data字段获取实际数据
  console.log('数据:', data)
} else {
  console.error('错误:', res.message)
}
```

---

## 请求格式规范

### HTTP Header

```http
Content-Type: application/json
Authorization: Bearer {token}
```

### 请求体格式

```json
{
  "field1": "value1",
  "field2": 123,
  "field3": true
}
```

### 分页参数

```javascript
{
  page: 1,        // 页码，从1开始
  size: 10,       // 每页数量
  sort: "createTime",  // 排序字段
  order: "desc"  // 排序方向：asc/desc
}
```

---

## 支付接口规范

### 支付流程

```
1. 创建订单 → POST /v1/orders
2. 创建支付 → POST /v1/payment/create
3. 获取支付参数 → POST /v1/payment/{wechat|alipay}
4. 调起支付 → uni.requestPayment()
5. 轮询支付状态 → GET /v1/payment/status/{paymentNo}
6. 处理支付结果 → 更新订单状态
```

### 1. 创建支付订单

**接口**: `POST /v1/payment/create`

**请求参数**:

```javascript
{
  orderId: string,        // 订单ID（必填）
  userId: string,         // 用户ID（必填）
  paymentMethod: string,  // 支付方式：wechat/alipay/wallet（必填）
  couponId: string        // 优惠券ID（可选）
}
```

**响应示例**:

```json
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": {
    "paymentNo": "PAY20260320123456789",  // 支付流水号
    "orderId": "ORD123456",
    "amount": 58.00,
    "paymentMethod": "wechat",
    "status": "pending"
  }
}
```

### 2. 微信支付

**接口**: `POST /v1/payment/wechat`

**请求参数**:

```javascript
{
  paymentNo: string  // 支付流水号（必填）
}
```

**响应示例**:

```json
{
  "success": true,
  "code": "200",
  "message": "微信支付订单创建成功",
  "data": {
    "paymentNo": "PAY20260320123456789",
    "status": "pending",
    "timeStamp": "1699999999",
    "nonceStr": "abc123def456",
    "package": "prepay_id=wx123456789",
    "signType": "MD5",
    "paySign": "C380BEC2BFD727A4B6845133519F3AD6"
  }
}
```

**前端调起支付**:

```javascript
const payParams = res.data
uni.requestPayment({
  provider: 'wxpay',
  timeStamp: payParams.timeStamp,
  nonceStr: payParams.nonceStr,
  package: payParams.package,
  signType: payParams.signType,
  paySign: payParams.paySign,
  success: () => {
    console.log('支付成功')
  },
  fail: (err) => {
    console.error('支付失败:', err)
  }
})
```

### 3. 支付宝支付

**接口**: `POST /v1/payment/alipay`

**请求参数**:

```javascript
{
  paymentNo: string  // 支付流水号（必填）
}
```

**响应示例**:

```json
{
  "success": true,
  "code": "200",
  "message": "支付宝支付订单创建成功",
  "data": {
    "paymentNo": "PAY20260320123456789",
    "status": "pending",
    "orderInfo": "app_id=2021000000000000&method=alipay.trade.wap.pay..."
  }
}
```

**前端调起支付**:

```javascript
const payParams = res.data
uni.requestPayment({
  provider: 'alipay',
  orderInfo: payParams.orderInfo,
  success: () => {
    console.log('支付成功')
  },
  fail: (err) => {
    console.error('支付失败:', err)
  }
})
```

### 4. 查询支付状态

**接口**: `GET /v1/payment/status/{paymentNo}`

**响应示例**:

```json
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": {
    "paymentNo": "PAY20260320123456789",
    "status": "success",  // pending/success/failed
    "amount": 58.00,
    "paymentMethod": "wechat",
    "orderStatus": 1
  }
}
```

**支付状态说明**:

| 状态 | 说明 |
|------|------|
| `pending` | 待支付 |
| `success` | 支付成功 |
| `failed` | 支付失败 |
| `refund` | 已退款 |

---

## 错误处理规范

### HTTP状态码

| 状态码 | 说明 | 处理方式 |
|--------|------|---------|
| 200 | 请求成功 | 检查`success`字段判断业务是否成功 |
| 400 | 请求参数错误 | 提示用户检查输入 |
| 401 | 未授权 | 清除token，跳转登录页 |
| 403 | 权限不足 | 提示权限不足 |
| 404 | 资源不存在 | 提示资源不存在 |
| 500+ | 服务器错误 | 提示稍后重试 |

### 业务错误码

| 错误码 | 说明 | 示例 |
|--------|------|------|
| 200 | 成功 | 请求成功 |
| 400 | 请求参数错误 | 缺少必填参数 |
| 401 | 未登录 | Token无效或过期 |
| 403 | 权限不足 | 无权访问资源 |
| 404 | 资源不存在 | 订单不存在 |
| 10001 | 用户名已存在 | 注册时用户名重复 |
| 10002 | 手机号已注册 | 注册时手机号重复 |
| 20001 | 商品不存在 | 查询的商品ID无效 |
| 30001 | 会话不存在 | 聊天会话不存在 |
| 40001 | 会话不存在 | 消息已撤回 |
| 50001 | 敏感词违规 | 内容包含敏感词 |

### 前端错误处理

```javascript
try {
  const res = await api.someMethod()
  if (res.code === 200) {
    // 处理成功逻辑
  } else {
    // 处理业务失败
    uni.showToast({
      title: res.message || '操作失败',
      icon: 'none'
    })
  }
} catch (error) {
  // 处理HTTP错误或其他异常
  console.error('请求失败:', error)
  uni.showToast({
    title: error.message || '网络错误，请稍后重试',
    icon: 'none'
  })
}
```

---

## 字段类型规范

### 基础类型

| TypeScript类型 | Java类型 | 说明 | 示例 |
|---------------|---------|------|------|
| `string` | `String` | 字符串 | `"userId"`, `"2026-03-20T10:00:00"` |
| `number` | `Integer` | 整数 | `100`, `1` |
| `number` | `BigDecimal` | 金额 | `58.50`（字符串传输） |
| `boolean` | `Boolean` | 布尔值 | `true`, `false` |
| `Array<T>` | `List<T>` | 数组 | `[{id: "1"}, {id: "2"}]` |
| `Object` | `Map/Object` | 对象 | `{id: "1", name: "xxx"}` |

### 特殊类型

#### 1. 金额类型

```typescript
// 前端使用number，但传输时转换为字符串
interface Amount {
  value: number  // 58.50
}

// 后端接收
{
  "amount": "58.00"  // 使用字符串避免精度问题
}
```

#### 2. 时间类型

```typescript
// ISO 8601格式
{
  "createTime": "2026-03-20T10:30:00",
  "updateTime": "2026-03-20T10:30:00"
}
```

#### 3. 枚举类型

**订单状态**：

```typescript
// 后端：数字枚举
enum OrderStatus {
  PENDING = 0,       // 待支付
  PAID = 1,          // 已支付
  PREPARING = 2,     // 制作中
  DELIVERING = 3,    // 配送中
  COMPLETED = 4,     // 已完成
  CANCELLED = 5      // 已取消
}

// 前端：可使用字符串映射
const orderStatusMap = {
  0: 'pending',
  1: 'paid',
  2: 'preparing',
  3: 'delivering',
  4: 'completed',
  5: 'cancelled'
}
```

**支付状态**：

```typescript
enum PaymentStatus {
  PENDING = 'pending',    // 待支付
  SUCCESS = 'success',    // 成功
  FAILED = 'failed',      // 失败
  REFUND = 'refund'       // 已退款
}
```

---

## 常见问题

### Q1: 响应格式不一致？

**问题**: 后端返回`{success, code, message, data}`，但前端有时只收到`data`

**解决**: 已修复`request.js`，现在统一返回完整response对象

```javascript
// ✅ 正确使用
const res = await api.getData()
const data = res.data  // 从data字段获取数据
```

### Q2: 金额精度问题？

**问题**: 前端使用number计算金额可能出现精度丢失

**解决**:
- 前端计算时使用整数（分为单位）
- 后端使用BigDecimal
- 传输时使用字符串

```javascript
// ✅ 正确做法
const amount = 5850  // 58.50元，以分为单位
const displayAmount = (amount / 100).toFixed(2)  // "58.50"
```

### Q3: Token过期处理？

**问题**: 401错误需要清除token并跳转登录

**解决**: `request.js`已自动处理

```javascript
// src/utils/request.js 已实现
if (res.statusCode === 401) {
  handleTokenExpired(options.url)
  reject({ message: '未授权，请重新登录', statusCode: 401 })
}
```

### Q4: 支付回调处理？

**问题**: 支付成功后需要轮询查询状态

**解决**: 使用`paymentApi.pollPaymentStatus`方法

```javascript
await paymentApi.pollPaymentStatus(paymentNo, {
  interval: 2000,      // 每2秒查询一次
  maxAttempts: 15,     // 最多查询15次（30秒）
  onSuccess: (data) => {
    console.log('支付成功:', data)
  },
  onFailed: (data) => {
    console.log('支付失败:', data)
  },
  onTimeout: () => {
    console.log('支付超时')
  }
})
```

---

## 版本历史

### v1.0.0 (2026-03-20)

- ✅ 统一响应格式为ResponseResult
- ✅ 修复request.js响应处理逻辑
- ✅ 完善支付接口规范
- ✅ 添加错误处理规范
- ✅ 统一字段类型规范

---

**维护人员**: 开发团队
**联系方式**: 提交Issue到项目仓库
**更新频率**: 随项目更新
