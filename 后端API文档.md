# 后端API文档

生成时间：2026-04-15
接口总数：497
服务基址：`http://localhost:7777/api`

## 通用响应结构

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | 是否成功 | `true` |
| `code` | 字符串 | 业务状态码 | `"200"` |
| `message` | 字符串 | 提示信息 | `"成功"` |
| `data` | 对象/数组/基础类型 | 业务数据体 | `{}` |

## 通用异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

## 目录

- [Supervisor监督代理（SSE流式）](#Supervisor监督代理（SSE流式）)（2 个接口）
- [add-dish-controller](#add-dish-controller)（8 个接口）
- [address-controller](#address-controller)（6 个接口）
- [admin-announcement-controller](#admin-announcement-controller)（8 个接口）
- [admin-controller](#admin-controller)（8 个接口）
- [admin-dish-controller](#admin-dish-controller)（6 个接口）
- [admin-finance-controller](#admin-finance-controller)（7 个接口）
- [admin-merchant-controller](#admin-merchant-controller)（5 个接口）
- [admin-order-controller](#admin-order-controller)（5 个接口）
- [admin-permission-controller](#admin-permission-controller)（8 个接口）
- [admin-role-controller](#admin-role-controller)（8 个接口）
- [admin-statistics-controller](#admin-statistics-controller)（2 个接口）
- [admin-system-config-controller](#admin-system-config-controller)（9 个接口）
- [admin-user-controller](#admin-user-controller)（6 个接口）
- [admin-withdraw-controller](#admin-withdraw-controller)（8 个接口）
- [ai-chat-history-controller](#ai-chat-history-controller)（4 个接口）
- [ai-controller](#ai-controller)（7 个接口）
- [banner-controller](#banner-controller)（6 个接口）
- [cache-monitor-controller](#cache-monitor-controller)（5 个接口）
- [calorie-record-controller](#calorie-record-controller)（7 个接口）
- [captcha-controller](#captcha-controller)（1 个接口）
- [category-controller](#category-controller)（2 个接口）
- [chat-controller](#chat-controller)（6 个接口）
- [chat-session-controller](#chat-session-controller)（7 个接口）
- [collection-controller](#collection-controller)（5 个接口）
- [consume-history-controller](#consume-history-controller)（1 个接口）
- [contact-controller](#contact-controller)（9 个接口）
- [content-extraction-controller](#content-extraction-controller)（9 个接口）
- [coupon-controller](#coupon-controller)（5 个接口）
- [discount-controller](#discount-controller)（7 个接口）
- [dish-controller](#dish-controller)（10 个接口）
- [dish-step-controller](#dish-step-controller)（9 个接口）
- [favorite-controller](#favorite-controller)（10 个接口）
- [festival-controller](#festival-controller)（11 个接口）
- [file-access-controller](#file-access-controller)（2 个接口）
- [file-controller](#file-controller)（3 个接口）
- [group-controller](#group-controller)（11 个接口）
- [group-order-addition-controller](#group-order-addition-controller)（6 个接口）
- [group-order-chat-controller](#group-order-chat-controller)（7 个接口）
- [group-order-controller](#group-order-controller)（7 个接口）
- [home-controller](#home-controller)（3 个接口）
- [hot-topic-admin-controller](#hot-topic-admin-controller)（8 个接口）
- [ingredient-conflict-rule-controller](#ingredient-conflict-rule-controller)（9 个接口）
- [legacy-address-controller](#legacy-address-controller)（4 个接口）
- [location-controller](#location-controller)（6 个接口）
- [menu-controller](#menu-controller)（11 个接口）
- [merchant-controller](#merchant-controller)（17 个接口）
- [merchant-insight-controller](#merchant-insight-controller)（6 个接口）
- [merchant-review-controller](#merchant-review-controller)（3 个接口）
- [merchants-controller](#merchants-controller)（3 个接口）
- [message-controller](#message-controller)（2 个接口）
- [message-record-controller](#message-record-controller)（8 个接口）
- [mock-o-auth-controller](#mock-o-auth-controller)（4 个接口）
- [notification-controller](#notification-controller)（7 个接口）
- [o-auth-controller](#o-auth-controller)（6 个接口）
- [order-controller](#order-controller)（14 个接口）
- [payment-controller](#payment-controller)（8 个接口）
- [payment-password-controller](#payment-password-controller)（5 个接口）
- [recipe-controller](#recipe-controller)（10 个接口）
- [recommend-controller](#recommend-controller)（12 个接口）
- [reject-recommendation-controller](#reject-recommendation-controller)（5 个接口）
- [remark-conflict-controller](#remark-conflict-controller)（4 个接口）
- [review-controller](#review-controller)（7 个接口）
- [scheduled-task-controller](#scheduled-task-controller)（12 个接口）
- [supervisor-agent-controller](#supervisor-agent-controller)（3 个接口）
- [system-log-controller](#system-log-controller)（4 个接口）
- [tutorial-admin-controller](#tutorial-admin-controller)（7 个接口）
- [tutorial-controller](#tutorial-controller)（4 个接口）
- [tutorial-merchant-controller](#tutorial-merchant-controller)（5 个接口）
- [tutorial-statistics-controller](#tutorial-statistics-controller)（4 个接口）
- [tutorial-user-controller](#tutorial-user-controller)（5 个接口）
- [user-controller](#user-controller)（15 个接口）
- [user-statistics-controller](#user-statistics-controller)（5 个接口）
- [verification-controller](#verification-controller)（2 个接口）
- [wallet-controller](#wallet-controller)（5 个接口）
- [wallet-security-controller](#wallet-security-controller)（2 个接口）
- [weather-controller](#weather-controller)（1 个接口）
- [wish-list-controller](#wish-list-controller)（8 个接口）

## Supervisor监督代理（SSE流式）

### 1. SSE流式聊天

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | SSE流式聊天 |
| 请求地址 | `/api/agent/supervisor-sse/chat` |
| 请求方式 | `GET` |
| 接口描述 | 实时推送Agent执行过程和流式结果 |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `message` | 字符串 | 是 | 用户消息（位置：query） | `"示例内容"` |
| `userId` | 字符串 | 否 | 用户ID（开启个性化服务后传入）（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `timeout` | 整数 | - | `1` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "timeout": 1
}
```

### 2. POST方式SSE流式聊天

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | POST方式SSE流式聊天 |
| 请求地址 | `/api/agent/supervisor-sse/chat` |
| 请求方式 | `POST` |
| 接口描述 | 支持复杂请求体的流式聊天 |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `message` | 字符串 | 否 | - | `"示例内容"` |
| `userId` | 字符串 | 否 | - | `"10001"` |
| `sessionId` | 字符串 | 否 | - | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `timeout` | 整数 | - | `1` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "message": "示例内容",
  "userId": "10001",
  "sessionId": "10001"
}
```

#### 响应示例

```json
{
  "timeout": 1
}
```

## add-dish-controller

### 1. checkAllergy

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | checkAllergy |
| 请求地址 | `/api/v1/add-dish/check-allergy` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupOrderId` | 整数 | 否 | - | `1` |
| `originalOrderId` | 字符串 | 否 | - | `"10001"` |
| `dishItems` | 数组<DishItem> | 否 | - | `[{"dishId": 1, "quantity": 1, "customization": "示例值"}]` |
| `dishItems[].dishId` | 整数 | 否 | - | `1` |
| `dishItems[].quantity` | 整数 | 否 | - | `1` |
| `dishItems[].customization` | 字符串 | 否 | - | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "groupOrderId": 1,
  "originalOrderId": "10001",
  "dishItems": [
    {}
  ]
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. getHistory

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getHistory |
| 请求地址 | `/api/v1/add-dish/history/{groupOrderId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupOrderId` | 整数 | 是 | -（位置：path） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. createAddDishRequest

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | createAddDishRequest |
| 请求地址 | `/api/v1/add-dish/request` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `X-User-Id` | 整数 | 是 | -（位置：header） | `1` |
| `groupOrderId` | 整数 | 否 | - | `1` |
| `originalOrderId` | 字符串 | 否 | - | `"10001"` |
| `dishItems` | 数组<DishItem> | 否 | - | `[{"dishId": 1, "quantity": 1, "customization": "示例值"}]` |
| `dishItems[].dishId` | 整数 | 否 | - | `1` |
| `dishItems[].quantity` | 整数 | 否 | - | `1` |
| `dishItems[].customization` | 字符串 | 否 | - | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "groupOrderId": 1,
  "originalOrderId": "10001",
  "dishItems": [
    {}
  ]
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. withdrawRequest

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | withdrawRequest |
| 请求地址 | `/api/v1/add-dish/request/{requestId}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `requestId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `X-User-Id` | 整数 | 是 | -（位置：header） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. batchReview

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | batchReview |
| 请求地址 | `/api/v1/add-dish/review` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `requestIds` | 数组<字符串> | 否 | - | `["示例值"]` |
| `action` | 字符串 | 否 | - | `"示例值"` |
| `rejectReason` | 字符串 | 否 | - | `"示例内容"` |
| `reviewerId` | 整数 | 否 | - | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "requestIds": [
    "示例值"
  ],
  "action": "示例值",
  "rejectReason": "示例内容",
  "reviewerId": 1
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. getReviewList

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getReviewList |
| 请求地址 | `/api/v1/add-dish/review-list/{groupOrderId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupOrderId` | 整数 | 是 | -（位置：path） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 7. updateSetting

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateSetting |
| 请求地址 | `/api/v1/add-dish/setting` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupOrderId` | 整数 | 否 | - | `1` |
| `addDishPermission` | 整数 | 否 | - | `1` |
| `budgetLimit` | 数字 | 否 | - | `99.9` |
| `maxDishCount` | 整数 | 否 | - | `1` |
| `currentAddCount` | 整数 | 否 | - | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "groupOrderId": 1,
  "addDishPermission": 1,
  "budgetLimit": 99.9,
  "maxDishCount": 1,
  "currentAddCount": 1
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 8. getSetting

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getSetting |
| 请求地址 | `/api/v1/add-dish/setting/{groupOrderId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupOrderId` | 整数 | 是 | -（位置：path） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## address-controller

### 1. addAddress_1

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | addAddress_1 |
| 请求地址 | `/api/v1/addresses` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 否 | - | `"10001"` |
| `userId` | 字符串 | 否 | - | `"10001"` |
| `receiverName` | 字符串 | 否 | - | `"示例名称"` |
| `receiverPhone` | 字符串 | 否 | - | `"13800138000"` |
| `province` | 字符串 | 否 | - | `"示例值"` |
| `city` | 字符串 | 否 | - | `"示例值"` |
| `district` | 字符串 | 否 | - | `"示例值"` |
| `detail` | 字符串 | 否 | - | `"示例值"` |
| `isDefault` | 整数 | 否 | - | `1` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "userId": "10001",
  "receiverName": "示例名称",
  "receiverPhone": "13800138000",
  "province": "示例值"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. getDefaultAddress

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getDefaultAddress |
| 请求地址 | `/api/v1/addresses/default` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | Address | - | `{"id": "10001", "userId": "10001", "receiverName": "示例名称", "receiverPhone": "13800138000", "province": "示例值"}` |
| `data.id` | 字符串 | - | `"10001"` |
| `data.userId` | 字符串 | - | `"10001"` |
| `data.receiverName` | 字符串 | - | `"示例名称"` |
| `data.receiverPhone` | 字符串 | - | `"13800138000"` |
| `data.province` | 字符串 | - | `"示例值"` |
| `data.city` | 字符串 | - | `"示例值"` |
| `data.district` | 字符串 | - | `"示例值"` |
| `data.detail` | 字符串 | - | `"示例值"` |
| `data.isDefault` | 整数 | - | `1` |
| `data.createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data.updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {
    "id": "10001",
    "userId": "10001",
    "receiverName": "示例名称",
    "receiverPhone": "13800138000",
    "province": "示例值"
  }
}
```

### 3. getUserAddresses

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getUserAddresses |
| 请求地址 | `/api/v1/addresses/user` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 数组<Address> | - | `[{"id": "10001", "userId": "10001", "receiverName": "示例名称", "receiverPhone": "13800138000", "province": "示例值"}]` |
| `data[].id` | 字符串 | - | `"10001"` |
| `data[].userId` | 字符串 | - | `"10001"` |
| `data[].receiverName` | 字符串 | - | `"示例名称"` |
| `data[].receiverPhone` | 字符串 | - | `"13800138000"` |
| `data[].province` | 字符串 | - | `"示例值"` |
| `data[].city` | 字符串 | - | `"示例值"` |
| `data[].district` | 字符串 | - | `"示例值"` |
| `data[].detail` | 字符串 | - | `"示例值"` |
| `data[].isDefault` | 整数 | - | `1` |
| `data[].createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": [
    {}
  ]
}
```

### 4. deleteAddress_1

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | deleteAddress_1 |
| 请求地址 | `/api/v1/addresses/{id}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. updateAddress_1

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateAddress_1 |
| 请求地址 | `/api/v1/addresses/{id}` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `id` | 字符串 | 否 | - | `"10001"` |
| `userId` | 字符串 | 否 | - | `"10001"` |
| `receiverName` | 字符串 | 否 | - | `"示例名称"` |
| `receiverPhone` | 字符串 | 否 | - | `"13800138000"` |
| `province` | 字符串 | 否 | - | `"示例值"` |
| `city` | 字符串 | 否 | - | `"示例值"` |
| `district` | 字符串 | 否 | - | `"示例值"` |
| `detail` | 字符串 | 否 | - | `"示例值"` |
| `isDefault` | 整数 | 否 | - | `1` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "userId": "10001",
  "receiverName": "示例名称",
  "receiverPhone": "13800138000",
  "province": "示例值"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. setDefaultAddress

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | setDefaultAddress |
| 请求地址 | `/api/v1/addresses/{id}/default` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## admin-announcement-controller

### 1. getAnnouncementList

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getAnnouncementList |
| 请求地址 | `/api/admin/announcements` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `pageSize` | 整数 | 否 | -（位置：query） | `1` |
| `keyword` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `status` | 字符串 | 否 | -（位置：query） | `"1"` |
| `type` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `priority` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `targetAudience` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `startDate` | 字符串 | 否 | -（位置：query） | `"2026-04-15"` |
| `endDate` | 字符串 | 否 | -（位置：query） | `"2026-04-15"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `records` | 数组<Announcement> | - | `[{"id": "10001", "merchantId": "10001", "title": "示例值", "content": "示例内容", "type": "示例值"}]` |
| `records[].id` | 字符串 | - | `"10001"` |
| `records[].merchantId` | 字符串 | - | `"10001"` |
| `records[].title` | 字符串 | - | `"示例值"` |
| `records[].content` | 字符串 | - | `"示例内容"` |
| `records[].type` | 字符串 | - | `"示例值"` |
| `records[].priority` | 字符串 | - | `"示例值"` |
| `records[].targetAudience` | 字符串 | - | `"示例值"` |
| `records[].readCount` | 整数 | - | `1` |
| `records[].readUsers` | 整数 | - | `1` |
| `records[].status` | 字符串 | - | `"1"` |
| `records[].startTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].endTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `total` | 整数 | - | `1` |
| `current` | 整数 | - | `1` |
| `pages` | 整数 | - | `1` |
| `size` | 整数 | - | `1` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "records": [
    {}
  ],
  "total": 1,
  "current": 1,
  "pages": 1,
  "size": 1
}
```

### 2. createAnnouncement

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | createAnnouncement |
| 请求地址 | `/api/admin/announcements` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 3. batchDeleteAnnouncements

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | batchDeleteAnnouncements |
| 请求地址 | `/api/admin/announcements/batch` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
[
  "示例值"
]
```

#### 响应示例

```json
{}
```

### 4. getAnnouncementStatistics

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getAnnouncementStatistics |
| 请求地址 | `/api/admin/announcements/statistics` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 5. deleteAnnouncement_1

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | deleteAnnouncement_1 |
| 请求地址 | `/api/admin/announcements/{announcementId}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `announcementId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 6. getAnnouncementDetail

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getAnnouncementDetail |
| 请求地址 | `/api/admin/announcements/{announcementId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `announcementId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 7. updateAnnouncement_1

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateAnnouncement_1 |
| 请求地址 | `/api/admin/announcements/{announcementId}` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `announcementId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 8. updateAnnouncementStatus

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateAnnouncementStatus |
| 请求地址 | `/api/admin/announcements/{announcementId}/status` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `announcementId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

## admin-controller

### 1. createAdmin

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | createAdmin |
| 请求地址 | `/api/admin/create` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `adminId` | 整数 | 否 | - | `1` |
| `username` | 字符串 | 否 | - | `"示例名称"` |
| `password` | 字符串 | 否 | - | `"123456"` |
| `realName` | 字符串 | 否 | - | `"示例名称"` |
| `phone` | 字符串 | 否 | - | `"13800138000"` |
| `email` | 字符串 | 否 | - | `"demo@example.com"` |
| `avatar` | 字符串 | 否 | - | `"https://example.com/file.png"` |
| `status` | 字符串 | 否 | - | `"1"` |
| `roleId` | 整数 | 否 | - | `1` |
| `lastLoginTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `lastLoginIp` | 字符串 | 否 | - | `"示例值"` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `createBy` | 整数 | 否 | - | `1` |
| `updateBy` | 整数 | 否 | - | `1` |
| `remark` | 字符串 | 否 | - | `"示例内容"` |
| `roleName` | 字符串 | 否 | - | `"示例名称"` |
| `roleCode` | 字符串 | 否 | - | `"200"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "adminId": 1,
  "username": "示例名称",
  "password": "123456",
  "realName": "示例名称",
  "phone": "13800138000"
}
```

#### 响应示例

```json
{}
```

### 2. getCurrentAdmin

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getCurrentAdmin |
| 请求地址 | `/api/admin/current` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 3. getAdminList

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getAdminList |
| 请求地址 | `/api/admin/list` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `pageSize` | 整数 | 否 | -（位置：query） | `1` |
| `username` | 字符串 | 否 | -（位置：query） | `"示例名称"` |
| `status` | 字符串 | 否 | -（位置：query） | `"1"` |
| `roleId` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `records` | 数组<Admin> | - | `[{"adminId": 1, "username": "示例名称", "password": "123456", "realName": "示例名称", "phone": "13800138000"}]` |
| `records[].adminId` | 整数 | - | `1` |
| `records[].username` | 字符串 | - | `"示例名称"` |
| `records[].password` | 字符串 | - | `"123456"` |
| `records[].realName` | 字符串 | - | `"示例名称"` |
| `records[].phone` | 字符串 | - | `"13800138000"` |
| `records[].email` | 字符串 | - | `"demo@example.com"` |
| `records[].avatar` | 字符串 | - | `"https://example.com/file.png"` |
| `records[].status` | 字符串 | - | `"1"` |
| `records[].roleId` | 整数 | - | `1` |
| `records[].lastLoginTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].lastLoginIp` | 字符串 | - | `"示例值"` |
| `records[].createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].createBy` | 整数 | - | `1` |
| `records[].updateBy` | 整数 | - | `1` |
| `records[].remark` | 字符串 | - | `"示例内容"` |
| `records[].roleName` | 字符串 | - | `"示例名称"` |
| `records[].roleCode` | 字符串 | - | `"200"` |
| `total` | 整数 | - | `1` |
| `current` | 整数 | - | `1` |
| `pages` | 整数 | - | `1` |
| `size` | 整数 | - | `1` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "records": [
    {}
  ],
  "total": 1,
  "current": 1,
  "pages": 1,
  "size": 1
}
```

### 4. login_1

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | login_1 |
| 请求地址 | `/api/admin/login` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 5. updateProfile

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateProfile |
| 请求地址 | `/api/admin/profile` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 6. uploadAvatarBase64_1

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | uploadAvatarBase64_1 |
| 请求地址 | `/api/admin/profile/avatar/base64` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 7. resetPassword

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | resetPassword |
| 请求地址 | `/api/admin/{adminId}/password` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `adminId` | 整数 | 是 | -（位置：path） | `1` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 8. updateStatus

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateStatus |
| 请求地址 | `/api/admin/{adminId}/status` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `adminId` | 整数 | 是 | -（位置：path） | `1` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

## admin-dish-controller

### 1. getDishList

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getDishList |
| 请求地址 | `/api/admin/dishes` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `pageSize` | 整数 | 否 | -（位置：query） | `1` |
| `keyword` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `category` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `status` | 字符串 | 否 | -（位置：query） | `"1"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `records` | 数组<Dish> | - | `[{"merchantId": "10001", "merchantName": "示例名称", "category": "示例值", "price": 99.9, "calorie": 1}]` |
| `records[].merchantId` | 字符串 | - | `"10001"` |
| `records[].merchantName` | 字符串 | - | `"示例名称"` |
| `records[].category` | 字符串 | - | `"示例值"` |
| `records[].price` | 数字 | - | `99.9` |
| `records[].calorie` | 整数 | - | `1` |
| `records[].estimatedCookingMinutes` | 整数 | - | `1` |
| `records[].stepTemplate` | 字符串 | - | `"示例值"` |
| `records[].isFastFood` | 布尔 | - | `true` |
| `records[].ingredients` | 字符串 | - | `"示例值"` |
| `records[].description` | 字符串 | - | `"示例值"` |
| `records[].cookingSteps` | 字符串 | - | `"示例值"` |
| `records[].nutrition` | 字符串 | - | `"示例值"` |
| `records[].image` | 字符串 | - | `"https://example.com/file.png"` |
| `records[].score` | 数字 | - | `99.9` |
| `records[].avgRating` | 数字 | - | `99.9` |
| `records[].isOnline` | 布尔 | - | `true` |
| `records[].viewCount` | 整数 | - | `1` |
| `records[].orderCount` | 整数 | - | `1` |
| `records[].favoriteCount` | 整数 | - | `1` |
| `records[].tags` | JsonNode | - | `{}` |
| `records[].statusCode` | 字符串 | - | `"1"` |
| `records[].auditStatus` | 字符串 | - | `"1"` |
| `records[].auditComment` | 字符串 | - | `"示例值"` |
| `records[].auditTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].auditAdminId` | 整数 | - | `1` |
| `records[].submitTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].stock` | 整数 | - | `1` |
| `records[].createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].status` | 布尔 | - | `true` |
| `records[].cookingMinutes` | 整数 | - | `1` |
| `records[].dishId` | 字符串 | - | `"10001"` |
| `records[].dishName` | 字符串 | - | `"示例名称"` |
| `total` | 整数 | - | `1` |
| `current` | 整数 | - | `1` |
| `pages` | 整数 | - | `1` |
| `size` | 整数 | - | `1` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "records": [
    {}
  ],
  "total": 1,
  "current": 1,
  "pages": 1,
  "size": 1
}
```

### 2. getAuditList

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getAuditList |
| 请求地址 | `/api/admin/dishes/audit` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `pageSize` | 整数 | 否 | -（位置：query） | `1` |
| `keyword` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `auditStatus` | 字符串 | 否 | -（位置：query） | `"1"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `records` | 数组<Dish> | - | `[{"merchantId": "10001", "merchantName": "示例名称", "category": "示例值", "price": 99.9, "calorie": 1}]` |
| `records[].merchantId` | 字符串 | - | `"10001"` |
| `records[].merchantName` | 字符串 | - | `"示例名称"` |
| `records[].category` | 字符串 | - | `"示例值"` |
| `records[].price` | 数字 | - | `99.9` |
| `records[].calorie` | 整数 | - | `1` |
| `records[].estimatedCookingMinutes` | 整数 | - | `1` |
| `records[].stepTemplate` | 字符串 | - | `"示例值"` |
| `records[].isFastFood` | 布尔 | - | `true` |
| `records[].ingredients` | 字符串 | - | `"示例值"` |
| `records[].description` | 字符串 | - | `"示例值"` |
| `records[].cookingSteps` | 字符串 | - | `"示例值"` |
| `records[].nutrition` | 字符串 | - | `"示例值"` |
| `records[].image` | 字符串 | - | `"https://example.com/file.png"` |
| `records[].score` | 数字 | - | `99.9` |
| `records[].avgRating` | 数字 | - | `99.9` |
| `records[].isOnline` | 布尔 | - | `true` |
| `records[].viewCount` | 整数 | - | `1` |
| `records[].orderCount` | 整数 | - | `1` |
| `records[].favoriteCount` | 整数 | - | `1` |
| `records[].tags` | JsonNode | - | `{}` |
| `records[].statusCode` | 字符串 | - | `"1"` |
| `records[].auditStatus` | 字符串 | - | `"1"` |
| `records[].auditComment` | 字符串 | - | `"示例值"` |
| `records[].auditTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].auditAdminId` | 整数 | - | `1` |
| `records[].submitTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].stock` | 整数 | - | `1` |
| `records[].createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].status` | 布尔 | - | `true` |
| `records[].cookingMinutes` | 整数 | - | `1` |
| `records[].dishId` | 字符串 | - | `"10001"` |
| `records[].dishName` | 字符串 | - | `"示例名称"` |
| `total` | 整数 | - | `1` |
| `current` | 整数 | - | `1` |
| `pages` | 整数 | - | `1` |
| `size` | 整数 | - | `1` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "records": [
    {}
  ],
  "total": 1,
  "current": 1,
  "pages": 1,
  "size": 1
}
```

### 3. getAuditDetail

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getAuditDetail |
| 请求地址 | `/api/admin/dishes/audit/{dishId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `dishId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `merchantId` | 字符串 | - | `"10001"` |
| `merchantName` | 字符串 | - | `"示例名称"` |
| `category` | 字符串 | - | `"示例值"` |
| `price` | 数字 | - | `99.9` |
| `calorie` | 整数 | - | `1` |
| `estimatedCookingMinutes` | 整数 | - | `1` |
| `stepTemplate` | 字符串 | - | `"示例值"` |
| `isFastFood` | 布尔 | - | `true` |
| `ingredients` | 字符串 | - | `"示例值"` |
| `description` | 字符串 | - | `"示例值"` |
| `cookingSteps` | 字符串 | - | `"示例值"` |
| `nutrition` | 字符串 | - | `"示例值"` |
| `image` | 字符串 | - | `"https://example.com/file.png"` |
| `score` | 数字 | - | `99.9` |
| `avgRating` | 数字 | - | `99.9` |
| `isOnline` | 布尔 | - | `true` |
| `viewCount` | 整数 | - | `1` |
| `orderCount` | 整数 | - | `1` |
| `favoriteCount` | 整数 | - | `1` |
| `tags` | JsonNode | - | `{}` |
| `statusCode` | 字符串 | - | `"1"` |
| `auditStatus` | 字符串 | - | `"1"` |
| `auditComment` | 字符串 | - | `"示例值"` |
| `auditTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `auditAdminId` | 整数 | - | `1` |
| `submitTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `stock` | 整数 | - | `1` |
| `createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `status` | 布尔 | - | `true` |
| `cookingMinutes` | 整数 | - | `1` |
| `dishId` | 字符串 | - | `"10001"` |
| `dishName` | 字符串 | - | `"示例名称"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "merchantId": "10001",
  "merchantName": "示例名称",
  "category": "示例值",
  "price": 99.9,
  "calorie": 1
}
```

### 4. submitAudit

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | submitAudit |
| 请求地址 | `/api/admin/dishes/audit/{dishId}` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `dishId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 5. getDishDetail_1

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getDishDetail_1 |
| 请求地址 | `/api/admin/dishes/{dishId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `dishId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 6. updateDishStatus_1

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateDishStatus_1 |
| 请求地址 | `/api/admin/dishes/{dishId}/status` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `dishId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

## admin-finance-controller

### 1. getRechargeList

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getRechargeList |
| 请求地址 | `/api/admin/finance/recharges` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `pageSize` | 整数 | 否 | -（位置：query） | `1` |
| `keyword` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `paymentMethod` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `status` | 字符串 | 否 | -（位置：query） | `"1"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 2. getRechargeStatistics

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getRechargeStatistics |
| 请求地址 | `/api/admin/finance/recharges/stats` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 3. getRechargeDetail

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getRechargeDetail |
| 请求地址 | `/api/admin/finance/recharges/{rechargeId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `rechargeId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 4. getRefundList

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getRefundList |
| 请求地址 | `/api/admin/finance/refunds` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `pageSize` | 整数 | 否 | -（位置：query） | `1` |
| `keyword` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `status` | 字符串 | 否 | -（位置：query） | `"1"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 5. getRefundStatistics

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getRefundStatistics |
| 请求地址 | `/api/admin/finance/refunds/stats` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 6. getRefundDetail

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getRefundDetail |
| 请求地址 | `/api/admin/finance/refunds/{refundId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `refundId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 7. processRefund

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | processRefund |
| 请求地址 | `/api/admin/finance/refunds/{refundId}/process` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `refundId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

## admin-merchant-controller

### 1. getMerchantList

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getMerchantList |
| 请求地址 | `/api/admin/merchants` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `pageSize` | 整数 | 否 | -（位置：query） | `1` |
| `status` | 字符串 | 否 | -（位置：query） | `"1"` |
| `keyword` | 字符串 | 否 | -（位置：query） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `records` | 数组<Merchant> | - | `[{"name": "示例名称", "address": "示例值", "longitude": 99.9, "latitude": 99.9, "category": "示例值"}]` |
| `records[].name` | 字符串 | - | `"示例名称"` |
| `records[].address` | 字符串 | - | `"示例值"` |
| `records[].longitude` | 数字 | - | `99.9` |
| `records[].latitude` | 数字 | - | `99.9` |
| `records[].category` | 字符串 | - | `"示例值"` |
| `records[].phone` | 字符串 | - | `"13800138000"` |
| `records[].password` | 字符串 | - | `"123456"` |
| `records[].email` | 字符串 | - | `"demo@example.com"` |
| `records[].businessLicense` | 字符串 | - | `"示例值"` |
| `records[].businessScope` | JsonNode | - | `{}` |
| `records[].contactName` | 字符串 | - | `"示例名称"` |
| `records[].avatar` | 字符串 | - | `"https://example.com/file.png"` |
| `records[].rating` | 数字 | - | `99.9` |
| `records[].businessHours` | JsonNode | - | `{}` |
| `records[].averagePrice` | 数字 | - | `99.9` |
| `records[].status` | 布尔 | - | `true` |
| `records[].auditStatus` | 字符串 | - | `"1"` |
| `records[].auditReason` | 字符串 | - | `"示例内容"` |
| `records[].auditTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].auditBy` | 字符串 | - | `"示例值"` |
| `records[].createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].album` | JsonNode | - | `{}` |
| `records[].merchantId` | 字符串 | - | `"10001"` |
| `total` | 整数 | - | `1` |
| `current` | 整数 | - | `1` |
| `pages` | 整数 | - | `1` |
| `size` | 整数 | - | `1` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "records": [
    {}
  ],
  "total": 1,
  "current": 1,
  "pages": 1,
  "size": 1
}
```

### 2. getPendingMerchants

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getPendingMerchants |
| 请求地址 | `/api/admin/merchants/pending` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `pageSize` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `records` | 数组<Merchant> | - | `[{"name": "示例名称", "address": "示例值", "longitude": 99.9, "latitude": 99.9, "category": "示例值"}]` |
| `records[].name` | 字符串 | - | `"示例名称"` |
| `records[].address` | 字符串 | - | `"示例值"` |
| `records[].longitude` | 数字 | - | `99.9` |
| `records[].latitude` | 数字 | - | `99.9` |
| `records[].category` | 字符串 | - | `"示例值"` |
| `records[].phone` | 字符串 | - | `"13800138000"` |
| `records[].password` | 字符串 | - | `"123456"` |
| `records[].email` | 字符串 | - | `"demo@example.com"` |
| `records[].businessLicense` | 字符串 | - | `"示例值"` |
| `records[].businessScope` | JsonNode | - | `{}` |
| `records[].contactName` | 字符串 | - | `"示例名称"` |
| `records[].avatar` | 字符串 | - | `"https://example.com/file.png"` |
| `records[].rating` | 数字 | - | `99.9` |
| `records[].businessHours` | JsonNode | - | `{}` |
| `records[].averagePrice` | 数字 | - | `99.9` |
| `records[].status` | 布尔 | - | `true` |
| `records[].auditStatus` | 字符串 | - | `"1"` |
| `records[].auditReason` | 字符串 | - | `"示例内容"` |
| `records[].auditTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].auditBy` | 字符串 | - | `"示例值"` |
| `records[].createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].album` | JsonNode | - | `{}` |
| `records[].merchantId` | 字符串 | - | `"10001"` |
| `total` | 整数 | - | `1` |
| `current` | 整数 | - | `1` |
| `pages` | 整数 | - | `1` |
| `size` | 整数 | - | `1` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "records": [
    {}
  ],
  "total": 1,
  "current": 1,
  "pages": 1,
  "size": 1
}
```

### 3. getMerchantDetail_2

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getMerchantDetail_2 |
| 请求地址 | `/api/admin/merchants/{merchantId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 整数 | 是 | -（位置：path） | `1` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 4. auditMerchant

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | auditMerchant |
| 请求地址 | `/api/admin/merchants/{merchantId}/audit` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 整数 | 是 | -（位置：path） | `1` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 5. updateMerchantStatus_1

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateMerchantStatus_1 |
| 请求地址 | `/api/admin/merchants/{merchantId}/status` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 整数 | 是 | -（位置：path） | `1` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

## admin-order-controller

### 1. getOrderList

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getOrderList |
| 请求地址 | `/api/admin/orders` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `pageSize` | 整数 | 否 | -（位置：query） | `1` |
| `status` | 整数 | 否 | -（位置：query） | `1` |
| `keyword` | 字符串 | 否 | -（位置：query） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `records` | 数组<Order> | - | `[{"id": "10001", "userId": "10001", "merchantId": "10001", "merchantName": "示例名称", "totalAmount": 99.9}]` |
| `records[].id` | 字符串 | - | `"10001"` |
| `records[].userId` | 字符串 | - | `"10001"` |
| `records[].merchantId` | 字符串 | - | `"10001"` |
| `records[].merchantName` | 字符串 | - | `"示例名称"` |
| `records[].totalAmount` | 数字 | - | `99.9` |
| `records[].status` | 整数 | - | `1` |
| `records[].statusText` | 字符串 | - | `"1"` |
| `records[].paymentId` | 字符串 | - | `"10001"` |
| `records[].paidAmount` | 数字 | - | `99.9` |
| `records[].paymentTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].addressId` | 字符串 | - | `"10001"` |
| `records[].address` | 字符串 | - | `"示例值"` |
| `records[].remark` | 字符串 | - | `"示例内容"` |
| `records[].createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `total` | 整数 | - | `1` |
| `current` | 整数 | - | `1` |
| `pages` | 整数 | - | `1` |
| `size` | 整数 | - | `1` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "records": [
    {}
  ],
  "total": 1,
  "current": 1,
  "pages": 1,
  "size": 1
}
```

### 2. batchUpdateOrderStatus

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | batchUpdateOrderStatus |
| 请求地址 | `/api/admin/orders/batch/status` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 3. getOrderStatistics

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getOrderStatistics |
| 请求地址 | `/api/admin/orders/statistics` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 4. getOrderDetail_1

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getOrderDetail_1 |
| 请求地址 | `/api/admin/orders/{orderId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `orderId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 5. updateOrderStatus_1

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateOrderStatus_1 |
| 请求地址 | `/api/admin/orders/{orderId}/status` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `orderId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

## admin-permission-controller

### 1. getPermissionList

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getPermissionList |
| 请求地址 | `/api/admin/permissions` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `pageSize` | 整数 | 否 | -（位置：query） | `1` |
| `permissionName` | 字符串 | 否 | -（位置：query） | `"示例名称"` |
| `resourceType` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `status` | 字符串 | 否 | -（位置：query） | `"1"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `records` | 数组<Permission> | - | `[{"permissionId": 1, "permissionName": "示例名称", "permissionCode": "200", "resourceType": "示例值", "parentId": 1}]` |
| `records[].permissionId` | 整数 | - | `1` |
| `records[].permissionName` | 字符串 | - | `"示例名称"` |
| `records[].permissionCode` | 字符串 | - | `"200"` |
| `records[].resourceType` | 字符串 | - | `"示例值"` |
| `records[].parentId` | 整数 | - | `1` |
| `records[].path` | 字符串 | - | `"示例值"` |
| `records[].icon` | 字符串 | - | `"示例值"` |
| `records[].description` | 字符串 | - | `"示例值"` |
| `records[].sortOrder` | 整数 | - | `1` |
| `records[].status` | 字符串 | - | `"1"` |
| `records[].createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].children` | 数组<Permission> | - | `[{"permissionId": 1, "permissionName": "示例名称", "permissionCode": "200", "resourceType": "示例值", "parentId": 1}]` |
| `total` | 整数 | - | `1` |
| `current` | 整数 | - | `1` |
| `pages` | 整数 | - | `1` |
| `size` | 整数 | - | `1` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "records": [
    {}
  ],
  "total": 1,
  "current": 1,
  "pages": 1,
  "size": 1
}
```

### 2. createPermission

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | createPermission |
| 请求地址 | `/api/admin/permissions` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 3. getChildPermissions

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getChildPermissions |
| 请求地址 | `/api/admin/permissions/children/{parentId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `parentId` | 整数 | 是 | -（位置：path） | `1` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 4. getTopLevelPermissions

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getTopLevelPermissions |
| 请求地址 | `/api/admin/permissions/top` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 5. getPermissionTree

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getPermissionTree |
| 请求地址 | `/api/admin/permissions/tree` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 6. deletePermission

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | deletePermission |
| 请求地址 | `/api/admin/permissions/{permissionId}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `permissionId` | 整数 | 是 | -（位置：path） | `1` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 7. getPermissionDetail

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getPermissionDetail |
| 请求地址 | `/api/admin/permissions/{permissionId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `permissionId` | 整数 | 是 | -（位置：path） | `1` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 8. updatePermission

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updatePermission |
| 请求地址 | `/api/admin/permissions/{permissionId}` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `permissionId` | 整数 | 是 | -（位置：path） | `1` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

## admin-role-controller

### 1. getRoleList

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getRoleList |
| 请求地址 | `/api/admin/roles` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `pageSize` | 整数 | 否 | -（位置：query） | `1` |
| `roleName` | 字符串 | 否 | -（位置：query） | `"示例名称"` |
| `status` | 字符串 | 否 | -（位置：query） | `"1"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `records` | 数组<Role> | - | `[{"roleId": 1, "roleName": "示例名称", "roleCode": "200", "description": "示例值", "status": "1"}]` |
| `records[].roleId` | 整数 | - | `1` |
| `records[].roleName` | 字符串 | - | `"示例名称"` |
| `records[].roleCode` | 字符串 | - | `"200"` |
| `records[].description` | 字符串 | - | `"示例值"` |
| `records[].status` | 字符串 | - | `"1"` |
| `records[].createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].sortOrder` | 整数 | - | `1` |
| `records[].permissionCount` | 整数 | - | `1` |
| `total` | 整数 | - | `1` |
| `current` | 整数 | - | `1` |
| `pages` | 整数 | - | `1` |
| `size` | 整数 | - | `1` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "records": [
    {}
  ],
  "total": 1,
  "current": 1,
  "pages": 1,
  "size": 1
}
```

### 2. createRole

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | createRole |
| 请求地址 | `/api/admin/roles` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 3. getAllRoles

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getAllRoles |
| 请求地址 | `/api/admin/roles/all` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
[
  {
    "roleId": 1,
    "roleName": "示例名称",
    "roleCode": "200",
    "description": "示例值",
    "status": "1"
  }
]
```

### 4. deleteRole

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | deleteRole |
| 请求地址 | `/api/admin/roles/{roleId}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `roleId` | 整数 | 是 | -（位置：path） | `1` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 5. getRoleDetail

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getRoleDetail |
| 请求地址 | `/api/admin/roles/{roleId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `roleId` | 整数 | 是 | -（位置：path） | `1` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 6. updateRole

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateRole |
| 请求地址 | `/api/admin/roles/{roleId}` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `roleId` | 整数 | 是 | -（位置：path） | `1` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 7. getRolePermissions

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getRolePermissions |
| 请求地址 | `/api/admin/roles/{roleId}/permissions` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `roleId` | 整数 | 是 | -（位置：path） | `1` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 8. assignPermissions

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | assignPermissions |
| 请求地址 | `/api/admin/roles/{roleId}/permissions` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `roleId` | 整数 | 是 | -（位置：path） | `1` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

## admin-statistics-controller

### 1. getDashboardStatistics

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getDashboardStatistics |
| 请求地址 | `/api/admin/statistics/dashboard` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `days` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象<字符串,对象> | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. exportStatistics

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | exportStatistics |
| 请求地址 | `/api/admin/statistics/export` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 字符串 | - | `"示例值"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": "示例值"
}
```

## admin-system-config-controller

### 1. getConfigList

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getConfigList |
| 请求地址 | `/api/admin/settings/config` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `pageSize` | 整数 | 否 | -（位置：query） | `1` |
| `keyword` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `configGroup` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `status` | 字符串 | 否 | -（位置：query） | `"1"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `records` | 数组<SystemConfig> | - | `[{"id": "10001", "configKey": "示例值", "configValue": "示例值", "configName": "示例名称", "configGroup": "示例值"}]` |
| `records[].id` | 字符串 | - | `"10001"` |
| `records[].configKey` | 字符串 | - | `"示例值"` |
| `records[].configValue` | 字符串 | - | `"示例值"` |
| `records[].configName` | 字符串 | - | `"示例名称"` |
| `records[].configGroup` | 字符串 | - | `"示例值"` |
| `records[].configType` | 字符串 | - | `"示例值"` |
| `records[].description` | 字符串 | - | `"示例值"` |
| `records[].isSystem` | 布尔 | - | `true` |
| `records[].status` | 字符串 | - | `"1"` |
| `records[].createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `total` | 整数 | - | `1` |
| `current` | 整数 | - | `1` |
| `pages` | 整数 | - | `1` |
| `size` | 整数 | - | `1` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "records": [
    {}
  ],
  "total": 1,
  "current": 1,
  "pages": 1,
  "size": 1
}
```

### 2. createConfig

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | createConfig |
| 请求地址 | `/api/admin/settings/config` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 3. batchUpdateConfigs

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | batchUpdateConfigs |
| 请求地址 | `/api/admin/settings/config/batch` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 4. getConfigsByGroup

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getConfigsByGroup |
| 请求地址 | `/api/admin/settings/config/group/{configGroup}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `configGroup` | 字符串 | 是 | -（位置：path） | `"示例值"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 5. getConfigGroups

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getConfigGroups |
| 请求地址 | `/api/admin/settings/config/groups` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 6. refreshConfigCache

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | refreshConfigCache |
| 请求地址 | `/api/admin/settings/config/refresh` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 7. deleteConfig

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | deleteConfig |
| 请求地址 | `/api/admin/settings/config/{configId}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `configId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 8. getConfigDetail

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getConfigDetail |
| 请求地址 | `/api/admin/settings/config/{configId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `configId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 9. updateConfig

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateConfig |
| 请求地址 | `/api/admin/settings/config/{configId}` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `configId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

## admin-user-controller

### 1. getUserList

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getUserList |
| 请求地址 | `/api/admin/users` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `pageSize` | 整数 | 否 | -（位置：query） | `1` |
| `keyword` | 字符串 | 否 | -（位置：query） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `records` | 数组<User> | - | `[{"userId": "10001", "phone": "13800138000", "hasPaymentPassword": true, "nickname": "示例名称", "height": 99.9}]` |
| `records[].userId` | 字符串 | - | `"10001"` |
| `records[].phone` | 字符串 | - | `"13800138000"` |
| `records[].hasPaymentPassword` | 布尔 | - | `true` |
| `records[].nickname` | 字符串 | - | `"示例名称"` |
| `records[].height` | 数字 | - | `99.9` |
| `records[].weight` | 数字 | - | `99.9` |
| `records[].dietGoal` | 字符串 | - | `"示例值"` |
| `records[].allergies` | JsonNode | - | `{}` |
| `records[].preferTags` | JsonNode | - | `{}` |
| `records[].email` | 字符串 | - | `"demo@example.com"` |
| `records[].disableWeatherRecommend` | 布尔 | - | `true` |
| `records[].merchantId` | 字符串 | - | `"10001"` |
| `records[].createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].avatar` | 字符串 | - | `"https://example.com/file.png"` |
| `records[].location` | 字符串 | - | `"示例值"` |
| `records[].gender` | 字符串 | - | `"示例值"` |
| `records[].birthday` | 字符串 | - | `"示例值"` |
| `records[].bio` | 字符串 | - | `"示例值"` |
| `total` | 整数 | - | `1` |
| `current` | 整数 | - | `1` |
| `pages` | 整数 | - | `1` |
| `size` | 整数 | - | `1` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "records": [
    {}
  ],
  "total": 1,
  "current": 1,
  "pages": 1,
  "size": 1
}
```

### 2. deleteUser

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | deleteUser |
| 请求地址 | `/api/admin/users/{userId}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 3. getUserDetail

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getUserDetail |
| 请求地址 | `/api/admin/users/{userId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 4. updateUser_1

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateUser_1 |
| 请求地址 | `/api/admin/users/{userId}` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 5. getUserStatistics

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getUserStatistics |
| 请求地址 | `/api/admin/users/{userId}/statistics` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 6. updateUserStatus

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateUserStatus |
| 请求地址 | `/api/admin/users/{userId}/status` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

## admin-withdraw-controller

### 1. getWithdrawList

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getWithdrawList |
| 请求地址 | `/api/admin/finance/withdrawals` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `pageSize` | 整数 | 否 | -（位置：query） | `1` |
| `keyword` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `status` | 字符串 | 否 | -（位置：query） | `"1"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 2. batchProcessWithdraw

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | batchProcessWithdraw |
| 请求地址 | `/api/admin/finance/withdrawals/batch/process` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 3. getWithdrawStatistics

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getWithdrawStatistics |
| 请求地址 | `/api/admin/finance/withdrawals/statistics` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 4. getWithdrawTrend

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getWithdrawTrend |
| 请求地址 | `/api/admin/finance/withdrawals/trend` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `days` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 5. getWithdrawDetail

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getWithdrawDetail |
| 请求地址 | `/api/admin/finance/withdrawals/{withdrawId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `withdrawId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 6. completeWithdraw

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | completeWithdraw |
| 请求地址 | `/api/admin/finance/withdrawals/{withdrawId}/complete` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `withdrawId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 7. failWithdraw

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | failWithdraw |
| 请求地址 | `/api/admin/finance/withdrawals/{withdrawId}/fail` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `withdrawId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 8. processWithdraw

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | processWithdraw |
| 请求地址 | `/api/admin/finance/withdrawals/{withdrawId}/process` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `withdrawId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

## ai-chat-history-controller

### 1. clearChatHistory

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | clearChatHistory |
| 请求地址 | `/api/v1/ai/chat/clear` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 2. hasChatHistory

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | hasChatHistory |
| 请求地址 | `/api/v1/ai/chat/has-history` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 3. getChatHistory

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getChatHistory |
| 请求地址 | `/api/v1/ai/chat/history` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 4. saveMessage

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | saveMessage |
| 请求地址 | `/api/v1/ai/chat/save` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

## ai-controller

### 1. generateDishDescription

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | generateDishDescription |
| 请求地址 | `/api/v1/ai/dish-description` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `name` | 字符串 | 否 | - | `"示例名称"` |
| `ingredients` | 数组<字符串> | 否 | - | `["示例值"]` |
| `category` | 字符串 | 否 | - | `"示例值"` |
| `style` | 字符串 | 否 | - | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "name": "示例名称",
  "ingredients": [
    "示例值"
  ],
  "category": "示例值",
  "style": "示例值"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. dishRecognize

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | dishRecognize |
| 请求地址 | `/api/v1/ai/dish-recognize` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 否 | -（位置：query） | `"10001"` |
| `image` | 字符串 | 是 | - | `"https://example.com/file.png"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "image": "https://example.com/file.png"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. dishRecognizeByUrl

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | dishRecognizeByUrl |
| 请求地址 | `/api/v1/ai/dish-recognize-url` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. health

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | health |
| 请求地址 | `/api/v1/ai/health` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象<字符串,对象> | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. recipeOptimize

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | recipeOptimize |
| 请求地址 | `/api/v1/ai/recipe` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. recipeUpload

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | recipeUpload |
| 请求地址 | `/api/v1/ai/recipe-upload` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 7. recipeOptimizeStream

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | recipeOptimizeStream |
| 请求地址 | `/api/v1/ai/recipe/stream` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `timeout` | 整数 | - | `1` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "timeout": 1
}
```

## banner-controller

### 1. getBanners

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getBanners |
| 请求地址 | `/api/v1/banners` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `position` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `status` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. createBanner

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | createBanner |
| 请求地址 | `/api/v1/banners` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 否 | - | `"10001"` |
| `title` | 字符串 | 否 | - | `"示例值"` |
| `imageUrl` | 字符串 | 否 | - | `"https://example.com/file.png"` |
| `type` | 字符串 | 否 | - | `"示例值"` |
| `targetType` | 字符串 | 否 | - | `"示例值"` |
| `targetId` | 字符串 | 否 | - | `"10001"` |
| `link` | 字符串 | 否 | - | `"示例值"` |
| `position` | 字符串 | 否 | - | `"示例值"` |
| `sortOrder` | 整数 | 否 | - | `1` |
| `status` | 整数 | 否 | - | `1` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "title": "示例值",
  "imageUrl": "https://example.com/file.png",
  "type": "示例值",
  "targetType": "示例值"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. deleteBanner

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | deleteBanner |
| 请求地址 | `/api/v1/banners/{bannerId}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `bannerId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. getBannerDetail

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getBannerDetail |
| 请求地址 | `/api/v1/banners/{bannerId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `bannerId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. updateBanner

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateBanner |
| 请求地址 | `/api/v1/banners/{bannerId}` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `bannerId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `id` | 字符串 | 否 | - | `"10001"` |
| `title` | 字符串 | 否 | - | `"示例值"` |
| `imageUrl` | 字符串 | 否 | - | `"https://example.com/file.png"` |
| `type` | 字符串 | 否 | - | `"示例值"` |
| `targetType` | 字符串 | 否 | - | `"示例值"` |
| `targetId` | 字符串 | 否 | - | `"10001"` |
| `link` | 字符串 | 否 | - | `"示例值"` |
| `position` | 字符串 | 否 | - | `"示例值"` |
| `sortOrder` | 整数 | 否 | - | `1` |
| `status` | 整数 | 否 | - | `1` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "title": "示例值",
  "imageUrl": "https://example.com/file.png",
  "type": "示例值",
  "targetType": "示例值"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. updateBannerStatus

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateBannerStatus |
| 请求地址 | `/api/v1/banners/{bannerId}/status` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `bannerId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## cache-monitor-controller

### 1. getDetails

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getDetails |
| 请求地址 | `/api/admin/cache/details` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 2. getHealth

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getHealth |
| 请求地址 | `/api/admin/cache/health` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 3. getOverview_1

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getOverview_1 |
| 请求地址 | `/api/admin/cache/overview` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 4. getReport

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getReport |
| 请求地址 | `/api/admin/cache/report` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 5. resetStats

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | resetStats |
| 请求地址 | `/api/admin/cache/reset` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

## calorie-record-controller

### 1. createCalorieRecord

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | createCalorieRecord |
| 请求地址 | `/api/calorie-records` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 否 | - | `"10001"` |
| `userId` | 字符串 | 否 | - | `"10001"` |
| `dishId` | 字符串 | 否 | - | `"10001"` |
| `calorie` | 整数 | 否 | - | `1` |
| `protein` | 数字 | 否 | - | `99.9` |
| `fat` | 数字 | 否 | - | `99.9` |
| `carbohydrate` | 数字 | 否 | - | `99.9` |
| `mealTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `recordTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `foodName` | 字符串 | 否 | - | `"示例名称"` |
| `description` | 字符串 | 否 | - | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "userId": "10001",
  "dishId": "10001",
  "calorie": 1,
  "protein": 99.9
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. updateCalorieRecord

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateCalorieRecord |
| 请求地址 | `/api/calorie-records` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 否 | - | `"10001"` |
| `userId` | 字符串 | 否 | - | `"10001"` |
| `dishId` | 字符串 | 否 | - | `"10001"` |
| `calorie` | 整数 | 否 | - | `1` |
| `protein` | 数字 | 否 | - | `99.9` |
| `fat` | 数字 | 否 | - | `99.9` |
| `carbohydrate` | 数字 | 否 | - | `99.9` |
| `mealTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `recordTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `foodName` | 字符串 | 否 | - | `"示例名称"` |
| `description` | 字符串 | 否 | - | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "userId": "10001",
  "dishId": "10001",
  "calorie": 1,
  "protein": 99.9
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. getRecordsByUserId

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getRecordsByUserId |
| 请求地址 | `/api/calorie-records/user/{userId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. getRecordsByUserIdAndDate

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getRecordsByUserIdAndDate |
| 请求地址 | `/api/calorie-records/user/{userId}/date/{date}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `date` | 字符串 | 是 | -（位置：path） | `"2026-04-15"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. getTodayNutritionSummary

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getTodayNutritionSummary |
| 请求地址 | `/api/calorie-records/user/{userId}/today-summary` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. getWeeklyRecordsByUserId

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getWeeklyRecordsByUserId |
| 请求地址 | `/api/calorie-records/user/{userId}/week` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 7. deleteCalorieRecord

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | deleteCalorieRecord |
| 请求地址 | `/api/calorie-records/{recordId}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `recordId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## captcha-controller

### 1. generateCaptcha

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | generateCaptcha |
| 请求地址 | `/api/v1/captcha/checkCode` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## category-controller

### 1. getCommonCategories

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getCommonCategories |
| 请求地址 | `/api/v1/category/common` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 数组<字符串> | - | `["示例值"]` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": [
    "示例值"
  ]
}
```

### 2. getAllCategories

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getAllCategories |
| 请求地址 | `/api/v1/category/list` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 数组<字符串> | - | `["示例值"]` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": [
    "示例值"
  ]
}
```

## chat-controller

### 1. sendMessage_2

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | sendMessage_2 |
| 请求地址 | `/api/v1/chat/messages` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `msgId` | 字符串 | 否 | - | `"10001"` |
| `fromId` | 字符串 | 否 | - | `"10001"` |
| `toId` | 字符串 | 否 | - | `"10001"` |
| `sessionId` | 字符串 | 否 | - | `"10001"` |
| `msgType` | 字符串 | 否 | - | `"示例值"` |
| `sessionType` | 字符串 | 否 | - | `"示例值"` |
| `content` | 字符串 | 否 | - | `"示例内容"` |
| `replyTo` | 字符串 | 否 | - | `"示例值"` |
| `readStatus` | 布尔 | 否 | - | `true` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `fileUrl` | 字符串 | 否 | - | `"https://example.com/file.png"` |
| `fileName` | 字符串 | 否 | - | `"https://example.com/file.png"` |
| `fileSize` | 整数 | 否 | - | `1` |
| `fileType` | 字符串 | 否 | - | `"https://example.com/file.png"` |
| `replyContent` | 字符串 | 否 | - | `"示例内容"` |
| `replyFromId` | 字符串 | 否 | - | `"10001"` |
| `replyFromName` | 字符串 | 否 | - | `"示例名称"` |
| `fromName` | 字符串 | 否 | - | `"示例名称"` |
| `fromAvatar` | 字符串 | 否 | - | `"https://example.com/file.png"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "msgId": "10001",
  "fromId": "10001",
  "toId": "10001",
  "sessionId": "10001",
  "msgType": "示例值"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. markMessageAsRead_1

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | markMessageAsRead_1 |
| 请求地址 | `/api/v1/chat/messages/{messageId}/read` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `messageId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. recallMessage

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | recallMessage |
| 请求地址 | `/api/v1/chat/messages/{messageId}/recall` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `messageId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. uploadFile

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | uploadFile |
| 请求地址 | `/api/v1/chat/upload-file` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `file` | 字符串 | 是 | - | `"https://example.com/file.png"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "file": "https://example.com/file.png"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. uploadImage

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | uploadImage |
| 请求地址 | `/api/v1/chat/upload-image` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `file` | 字符串 | 是 | - | `"https://example.com/file.png"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "file": "https://example.com/file.png"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. getChatMessages

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getChatMessages |
| 请求地址 | `/api/v1/chat/{sessionId}/messages` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `sessionId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `userId` | 字符串 | 否 | -（位置：query） | `"10001"` |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `size` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## chat-session-controller

### 1. createOrUpdateSession

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | createOrUpdateSession |
| 请求地址 | `/api/v1/chat/sessions` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. deleteSession

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | deleteSession |
| 请求地址 | `/api/v1/chat/sessions/{sessionId}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `sessionId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. updateLastMessage

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateLastMessage |
| 请求地址 | `/api/v1/chat/sessions/{sessionId}/last-message` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `sessionId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. togglePin

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | togglePin |
| 请求地址 | `/api/v1/chat/sessions/{sessionId}/toggle-pin` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `sessionId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. clearUnreadCount

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | clearUnreadCount |
| 请求地址 | `/api/v1/chat/sessions/{sessionId}/unread-clear` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `sessionId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. incrementUnreadCount

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | incrementUnreadCount |
| 请求地址 | `/api/v1/chat/sessions/{sessionId}/unread-increment` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `sessionId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 7. getUserChatSessions

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getUserChatSessions |
| 请求地址 | `/api/v1/chat/users/{userId}/chat-sessions` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## collection-controller

### 1. removeCollection

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | removeCollection |
| 请求地址 | `/api/v1/collections` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `type` | 字符串 | 是 | -（位置：query） | `"示例值"` |
| `id` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. getCollectionsByUserId

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getCollectionsByUserId |
| 请求地址 | `/api/v1/collections` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. addCollection

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | addCollection |
| 请求地址 | `/api/v1/collections` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 否 | - | `"10001"` |
| `userId` | 字符串 | 否 | - | `"10001"` |
| `collectableType` | 字符串 | 否 | - | `"示例值"` |
| `collectableId` | 字符串 | 否 | - | `"10001"` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "userId": "10001",
  "collectableType": "示例值",
  "collectableId": "10001",
  "createTime": "2026-04-15 21:00:00"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. checkCollection

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | checkCollection |
| 请求地址 | `/api/v1/collections/check` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `type` | 字符串 | 是 | -（位置：query） | `"示例值"` |
| `id` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. getCollectionsByType

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getCollectionsByType |
| 请求地址 | `/api/v1/collections/type` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `type` | 字符串 | 是 | -（位置：query） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## consume-history-controller

### 1. getConsumeHistory

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getConsumeHistory |
| 请求地址 | `/api/v1/consume-history` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `type` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `startDate` | 字符串 | 否 | -（位置：query） | `"2026-04-15"` |
| `endDate` | 字符串 | 否 | -（位置：query） | `"2026-04-15"` |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `pageSize` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## contact-controller

### 1. deleteFriend

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | deleteFriend |
| 请求地址 | `/api/v1/contacts/friends` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `friendId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. getMyFriends

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getMyFriends |
| 请求地址 | `/api/v1/contacts/friends` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. acceptFriendRequest

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | acceptFriendRequest |
| 请求地址 | `/api/v1/contacts/friends/accept` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 否 | - | `"10001"` |
| `requesterId` | 字符串 | 否 | - | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "userId": "10001",
  "requesterId": "10001"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. rejectFriendRequest

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | rejectFriendRequest |
| 请求地址 | `/api/v1/contacts/friends/reject` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 否 | - | `"10001"` |
| `requesterId` | 字符串 | 否 | - | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "userId": "10001",
  "requesterId": "10001"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. sendFriendRequest

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | sendFriendRequest |
| 请求地址 | `/api/v1/contacts/friends/request` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 否 | - | `"10001"` |
| `userId` | 字符串 | 否 | - | `"10001"` |
| `targetId` | 字符串 | 否 | - | `"10001"` |
| `relationType` | 字符串 | 否 | - | `"示例值"` |
| `status` | 字符串 | 否 | - | `"1"` |
| `role` | 字符串 | 否 | - | `"示例值"` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "userId": "10001",
  "targetId": "10001",
  "relationType": "示例值",
  "status": "1"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. getFriendRequests

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getFriendRequests |
| 请求地址 | `/api/v1/contacts/friends/requests` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 7. joinGroup

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | joinGroup |
| 请求地址 | `/api/v1/contacts/groups/join` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 否 | - | `"10001"` |
| `userId` | 字符串 | 否 | - | `"10001"` |
| `targetId` | 字符串 | 否 | - | `"10001"` |
| `relationType` | 字符串 | 否 | - | `"示例值"` |
| `status` | 字符串 | 否 | - | `"1"` |
| `role` | 字符串 | 否 | - | `"示例值"` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "userId": "10001",
  "targetId": "10001",
  "relationType": "示例值",
  "status": "1"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 8. leaveGroup_1

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | leaveGroup_1 |
| 请求地址 | `/api/v1/contacts/groups/leave` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `groupId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 9. getGroupMembers_1

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getGroupMembers_1 |
| 请求地址 | `/api/v1/contacts/groups/{groupId}/members` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## content-extraction-controller

### 1. updateExtraction

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateExtraction |
| 请求地址 | `/api/v1/content-extraction/extraction` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `extractionId` | 字符串 | 否 | - | `"10001"` |
| `dishName` | 字符串 | 否 | - | `"示例名称"` |
| `dishImage` | 字符串 | 否 | - | `"https://example.com/file.png"` |
| `description` | 字符串 | 否 | - | `"示例值"` |
| `ingredients` | 数组<IngredientItem> | 否 | - | `[{"name": "示例名称", "amount": "示例值"}]` |
| `ingredients[].name` | 字符串 | 否 | - | `"示例名称"` |
| `ingredients[].amount` | 字符串 | 否 | - | `"示例值"` |
| `steps` | 数组<StepItem> | 否 | - | `[{"stepNumber": 1, "description": "示例值", "image": "https://example.com/file.png"}]` |
| `steps[].stepNumber` | 整数 | 否 | - | `1` |
| `steps[].description` | 字符串 | 否 | - | `"示例值"` |
| `steps[].image` | 字符串 | 否 | - | `"https://example.com/file.png"` |
| `stepsDescription` | 字符串 | 否 | - | `"示例值"` |
| `cookingTime` | 整数 | 否 | - | `1` |
| `difficulty` | 字符串 | 否 | - | `"示例值"` |
| `tags` | 数组<字符串> | 否 | - | `["示例值"]` |
| `calories` | 整数 | 否 | - | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 布尔 | - | `true` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "extractionId": "10001",
  "dishName": "示例名称",
  "dishImage": "https://example.com/file.png",
  "description": "示例值",
  "ingredients": [
    {}
  ]
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": true
}
```

### 2. getExtractionDetail

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getExtractionDetail |
| 请求地址 | `/api/v1/content-extraction/extraction/{extractionId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `extractionId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | ContentExtractionDetailVO | - | `{"id": "10001", "sourceId": "10001", "contentUrl": "https://example.com/file.png", "platform": "示例值", "platformName": "示例名称"}` |
| `data.id` | 字符串 | - | `"10001"` |
| `data.sourceId` | 字符串 | - | `"10001"` |
| `data.contentUrl` | 字符串 | - | `"https://example.com/file.png"` |
| `data.platform` | 字符串 | - | `"示例值"` |
| `data.platformName` | 字符串 | - | `"示例名称"` |
| `data.originalTitle` | 字符串 | - | `"示例值"` |
| `data.dishName` | 字符串 | - | `"示例名称"` |
| `data.dishImage` | 字符串 | - | `"https://example.com/file.png"` |
| `data.description` | 字符串 | - | `"示例值"` |
| `data.ingredients` | 数组<IngredientItem> | - | `[{"name": "示例名称", "amount": "示例值"}]` |
| `data.steps` | 数组<StepItem> | - | `[{"stepNumber": 1, "description": "示例值", "image": "https://example.com/file.png"}]` |
| `data.stepsDescription` | 字符串 | - | `"示例值"` |
| `data.cookingTime` | 整数 | - | `1` |
| `data.difficulty` | 字符串 | - | `"示例值"` |
| `data.difficultyName` | 字符串 | - | `"示例名称"` |
| `data.tags` | 数组<字符串> | - | `["示例值"]` |
| `data.calories` | 整数 | - | `1` |
| `data.isPublished` | 布尔 | - | `true` |
| `data.recipeId` | 字符串 | - | `"10001"` |
| `data.manualScore` | 整数 | - | `1` |
| `data.isVerified` | 布尔 | - | `true` |
| `data.extractionMethod` | 字符串 | - | `"示例值"` |
| `data.createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data.updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {
    "id": "10001",
    "sourceId": "10001",
    "contentUrl": "https://example.com/file.png",
    "platform": "示例值",
    "platformName": "示例名称"
  }
}
```

### 3. publishAsRecipe

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | publishAsRecipe |
| 请求地址 | `/api/v1/content-extraction/extraction/{extractionId}/publish` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `extractionId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 字符串 | - | `"示例值"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": "示例值"
}
```

### 4. verifyExtraction

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | verifyExtraction |
| 请求地址 | `/api/v1/content-extraction/extraction/{extractionId}/verify` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `extractionId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `verified` | 布尔 | 是 | -（位置：query） | `true` |
| `score` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 布尔 | - | `true` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": true
}
```

### 5. addContentSource

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | addContentSource |
| 请求地址 | `/api/v1/content-extraction/source` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `contentUrl` | 字符串 | 是 | - | `"https://example.com/file.png"` |
| `contentType` | 字符串 | 否 | - | `"示例内容"` |
| `platform` | 字符串 | 否 | - | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 字符串 | - | `"示例值"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "contentUrl": "https://example.com/file.png",
  "contentType": "示例内容",
  "platform": "示例值"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": "示例值"
}
```

### 6. deleteContentSource

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | deleteContentSource |
| 请求地址 | `/api/v1/content-extraction/source/{sourceId}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `sourceId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 布尔 | - | `true` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": true
}
```

### 7. getContentSourceDetail

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getContentSourceDetail |
| 请求地址 | `/api/v1/content-extraction/source/{sourceId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `sourceId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | ContentSourceVO | - | `{"id": "10001", "contentUrl": "https://example.com/file.png", "contentType": "示例内容", "contentTypeName": "示例名称", "platform": "示例值"}` |
| `data.id` | 字符串 | - | `"10001"` |
| `data.contentUrl` | 字符串 | - | `"https://example.com/file.png"` |
| `data.contentType` | 字符串 | - | `"示例内容"` |
| `data.contentTypeName` | 字符串 | - | `"示例名称"` |
| `data.platform` | 字符串 | - | `"示例值"` |
| `data.platformName` | 字符串 | - | `"示例名称"` |
| `data.title` | 字符串 | - | `"示例值"` |
| `data.author` | 字符串 | - | `"示例值"` |
| `data.coverImage` | 字符串 | - | `"https://example.com/file.png"` |
| `data.videoDuration` | 整数 | - | `1` |
| `data.videoDurationFormatted` | 字符串 | - | `"示例值"` |
| `data.description` | 字符串 | - | `"示例值"` |
| `data.isExtracted` | 布尔 | - | `true` |
| `data.extractionId` | 字符串 | - | `"10001"` |
| `data.extractionStatus` | 字符串 | - | `"1"` |
| `data.extractionStatusName` | 字符串 | - | `"1"` |
| `data.errorMessage` | 字符串 | - | `"示例内容"` |
| `data.extractedDishName` | 字符串 | - | `"示例名称"` |
| `data.extractedDishImage` | 字符串 | - | `"https://example.com/file.png"` |
| `data.isPublished` | 布尔 | - | `true` |
| `data.createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data.extractionTime` | 字符串 | - | `"2026-04-15 21:00:00"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {
    "id": "10001",
    "contentUrl": "https://example.com/file.png",
    "contentType": "示例内容",
    "contentTypeName": "示例名称",
    "platform": "示例值"
  }
}
```

### 8. reExtract

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | reExtract |
| 请求地址 | `/api/v1/content-extraction/source/{sourceId}/re-extract` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `sourceId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 布尔 | - | `true` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": true
}
```

### 9. getUserContentSources

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getUserContentSources |
| 请求地址 | `/api/v1/content-extraction/sources` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 数组<ContentSourceVO> | - | `[{"id": "10001", "contentUrl": "https://example.com/file.png", "contentType": "示例内容", "contentTypeName": "示例名称", "platform": "示例值"}]` |
| `data[].id` | 字符串 | - | `"10001"` |
| `data[].contentUrl` | 字符串 | - | `"https://example.com/file.png"` |
| `data[].contentType` | 字符串 | - | `"示例内容"` |
| `data[].contentTypeName` | 字符串 | - | `"示例名称"` |
| `data[].platform` | 字符串 | - | `"示例值"` |
| `data[].platformName` | 字符串 | - | `"示例名称"` |
| `data[].title` | 字符串 | - | `"示例值"` |
| `data[].author` | 字符串 | - | `"示例值"` |
| `data[].coverImage` | 字符串 | - | `"https://example.com/file.png"` |
| `data[].videoDuration` | 整数 | - | `1` |
| `data[].videoDurationFormatted` | 字符串 | - | `"示例值"` |
| `data[].description` | 字符串 | - | `"示例值"` |
| `data[].isExtracted` | 布尔 | - | `true` |
| `data[].extractionId` | 字符串 | - | `"10001"` |
| `data[].extractionStatus` | 字符串 | - | `"1"` |
| `data[].extractionStatusName` | 字符串 | - | `"1"` |
| `data[].errorMessage` | 字符串 | - | `"示例内容"` |
| `data[].extractedDishName` | 字符串 | - | `"示例名称"` |
| `data[].extractedDishImage` | 字符串 | - | `"https://example.com/file.png"` |
| `data[].isPublished` | 布尔 | - | `true` |
| `data[].createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].extractionTime` | 字符串 | - | `"2026-04-15 21:00:00"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": [
    {}
  ]
}
```

## coupon-controller

### 1. checkCouponAvailable

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | checkCouponAvailable |
| 请求地址 | `/api/v1/coupons/check` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `couponId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `orderAmount` | 数字 | 是 | -（位置：query） | `99.9` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象<字符串,对象> | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. issueTestCoupon

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | issueTestCoupon |
| 请求地址 | `/api/v1/coupons/issue-test` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | UserCoupon | - | `{"id": "10001", "userId": "10001", "name": "示例名称", "amount": 99.9, "minAmount": 99.9}` |
| `data.id` | 字符串 | - | `"10001"` |
| `data.userId` | 字符串 | - | `"10001"` |
| `data.name` | 字符串 | - | `"示例名称"` |
| `data.amount` | 数字 | - | `99.9` |
| `data.minAmount` | 数字 | - | `99.9` |
| `data.status` | 字符串 | - | `"1"` |
| `data.orderId` | 字符串 | - | `"10001"` |
| `data.expireTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data.createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data.updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data.useTime` | 字符串 | - | `"2026-04-15 21:00:00"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {
    "id": "10001",
    "userId": "10001",
    "name": "示例名称",
    "amount": 99.9,
    "minAmount": 99.9
  }
}
```

### 3. releaseCoupon

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | releaseCoupon |
| 请求地址 | `/api/v1/coupons/release` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `couponId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `orderId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象<字符串,字符串> | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. useCoupon

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | useCoupon |
| 请求地址 | `/api/v1/coupons/use` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `couponId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `orderId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象<字符串,字符串> | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. getUserCoupons

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getUserCoupons |
| 请求地址 | `/api/v1/coupons/user` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 数组<UserCoupon> | - | `[{"id": "10001", "userId": "10001", "name": "示例名称", "amount": 99.9, "minAmount": 99.9}]` |
| `data[].id` | 字符串 | - | `"10001"` |
| `data[].userId` | 字符串 | - | `"10001"` |
| `data[].name` | 字符串 | - | `"示例名称"` |
| `data[].amount` | 数字 | - | `99.9` |
| `data[].minAmount` | 数字 | - | `99.9` |
| `data[].status` | 字符串 | - | `"1"` |
| `data[].orderId` | 字符串 | - | `"10001"` |
| `data[].expireTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].useTime` | 字符串 | - | `"2026-04-15 21:00:00"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": [
    {}
  ]
}
```

## discount-controller

### 1. getMerchantDiscounts

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getMerchantDiscounts |
| 请求地址 | `/api/v1/merchant/{merchantId}/discounts` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. addDiscount

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | addDiscount |
| 请求地址 | `/api/v1/merchant/{merchantId}/discounts` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `id` | 字符串 | 否 | - | `"10001"` |
| `merchantId` | 字符串 | 否 | - | `"10001"` |
| `name` | 字符串 | 否 | - | `"示例名称"` |
| `type` | 字符串 | 否 | - | `"示例值"` |
| `discountValue` | 数字 | 否 | - | `99.9` |
| `minAmount` | 数字 | 否 | - | `99.9` |
| `limitPerUser` | 整数 | 否 | - | `1` |
| `description` | 字符串 | 否 | - | `"示例值"` |
| `usageNotes` | 字符串 | 否 | - | `"示例值"` |
| `status` | 字符串 | 否 | - | `"1"` |
| `validityType` | 字符串 | 否 | - | `"示例值"` |
| `validityPeriod` | 字符串 | 否 | - | `"示例值"` |
| `validDays` | 整数 | 否 | - | `1` |
| `usedCount` | 整数 | 否 | - | `1` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "merchantId": "10001",
  "name": "示例名称",
  "type": "示例值",
  "discountValue": 99.9
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. batchDeleteDiscounts

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | batchDeleteDiscounts |
| 请求地址 | `/api/v1/merchant/{merchantId}/discounts/batch` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `ids` | 字符串 | 是 | -（位置：query） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. batchUpdateDiscountStatus

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | batchUpdateDiscountStatus |
| 请求地址 | `/api/v1/merchant/{merchantId}/discounts/batch` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. deleteDiscount

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | deleteDiscount |
| 请求地址 | `/api/v1/merchant/{merchantId}/discounts/{discountId}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `discountId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. updateDiscount

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateDiscount |
| 请求地址 | `/api/v1/merchant/{merchantId}/discounts/{discountId}` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `discountId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `id` | 字符串 | 否 | - | `"10001"` |
| `merchantId` | 字符串 | 否 | - | `"10001"` |
| `name` | 字符串 | 否 | - | `"示例名称"` |
| `type` | 字符串 | 否 | - | `"示例值"` |
| `discountValue` | 数字 | 否 | - | `99.9` |
| `minAmount` | 数字 | 否 | - | `99.9` |
| `limitPerUser` | 整数 | 否 | - | `1` |
| `description` | 字符串 | 否 | - | `"示例值"` |
| `usageNotes` | 字符串 | 否 | - | `"示例值"` |
| `status` | 字符串 | 否 | - | `"1"` |
| `validityType` | 字符串 | 否 | - | `"示例值"` |
| `validityPeriod` | 字符串 | 否 | - | `"示例值"` |
| `validDays` | 整数 | 否 | - | `1` |
| `usedCount` | 整数 | 否 | - | `1` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "merchantId": "10001",
  "name": "示例名称",
  "type": "示例值",
  "discountValue": 99.9
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 7. toggleDiscountStatus

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | toggleDiscountStatus |
| 请求地址 | `/api/v1/merchant/{merchantId}/discounts/{discountId}/status` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `discountId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## dish-controller

### 1. getDishes

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getDishes |
| 请求地址 | `/api/v1/dishes` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `category` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `keyword` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `merchantId` | 字符串 | 否 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. createDish

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | createDish |
| 请求地址 | `/api/v1/dishes` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 否 | - | `"10001"` |
| `merchantName` | 字符串 | 否 | - | `"示例名称"` |
| `category` | 字符串 | 否 | - | `"示例值"` |
| `price` | 数字 | 否 | - | `99.9` |
| `calorie` | 整数 | 否 | - | `1` |
| `estimatedCookingMinutes` | 整数 | 否 | - | `1` |
| `stepTemplate` | 字符串 | 否 | - | `"示例值"` |
| `isFastFood` | 布尔 | 否 | - | `true` |
| `ingredients` | 字符串 | 否 | - | `"示例值"` |
| `description` | 字符串 | 否 | - | `"示例值"` |
| `cookingSteps` | 字符串 | 否 | - | `"示例值"` |
| `nutrition` | 字符串 | 否 | - | `"示例值"` |
| `image` | 字符串 | 否 | - | `"https://example.com/file.png"` |
| `score` | 数字 | 否 | - | `99.9` |
| `avgRating` | 数字 | 否 | - | `99.9` |
| `isOnline` | 布尔 | 否 | - | `true` |
| `viewCount` | 整数 | 否 | - | `1` |
| `orderCount` | 整数 | 否 | - | `1` |
| `favoriteCount` | 整数 | 否 | - | `1` |
| `tags` | JsonNode | 否 | - | `{}` |
| `statusCode` | 字符串 | 否 | - | `"1"` |
| `auditStatus` | 字符串 | 否 | - | `"1"` |
| `auditComment` | 字符串 | 否 | - | `"示例值"` |
| `auditTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `auditAdminId` | 整数 | 否 | - | `1` |
| `submitTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `stock` | 整数 | 否 | - | `1` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `status` | 布尔 | 否 | - | `true` |
| `cookingMinutes` | 整数 | 否 | - | `1` |
| `dishId` | 字符串 | 否 | - | `"10001"` |
| `dishName` | 字符串 | 否 | - | `"示例名称"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "merchantId": "10001",
  "merchantName": "示例名称",
  "category": "示例值",
  "price": 99.9,
  "calorie": 1
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. batchDeleteDishes

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | batchDeleteDishes |
| 请求地址 | `/api/v1/dishes/batch` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. batchUpdateDishStatus

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | batchUpdateDishStatus |
| 请求地址 | `/api/v1/dishes/batch/status` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. getDishesByMerchant

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getDishesByMerchant |
| 请求地址 | `/api/v1/dishes/merchant/{merchantId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. getRecommendedDishes

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getRecommendedDishes |
| 请求地址 | `/api/v1/dishes/recommended` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 否 | -（位置：query） | `"10001"` |
| `limit` | 整数 | 否 | -（位置：query） | `1` |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `size` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 7. getReplacementDishes

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getReplacementDishes |
| 请求地址 | `/api/v1/dishes/replacement` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `type` | 字符串 | 是 | -（位置：query） | `"示例值"` |
| `exclude` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `limit` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 8. getDishDetail

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getDishDetail |
| 请求地址 | `/api/v1/dishes/{dishId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `dishId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 9. updateDish

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateDish |
| 请求地址 | `/api/v1/dishes/{dishId}` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `dishId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `merchantId` | 字符串 | 否 | - | `"10001"` |
| `merchantName` | 字符串 | 否 | - | `"示例名称"` |
| `category` | 字符串 | 否 | - | `"示例值"` |
| `price` | 数字 | 否 | - | `99.9` |
| `calorie` | 整数 | 否 | - | `1` |
| `estimatedCookingMinutes` | 整数 | 否 | - | `1` |
| `stepTemplate` | 字符串 | 否 | - | `"示例值"` |
| `isFastFood` | 布尔 | 否 | - | `true` |
| `ingredients` | 字符串 | 否 | - | `"示例值"` |
| `description` | 字符串 | 否 | - | `"示例值"` |
| `cookingSteps` | 字符串 | 否 | - | `"示例值"` |
| `nutrition` | 字符串 | 否 | - | `"示例值"` |
| `image` | 字符串 | 否 | - | `"https://example.com/file.png"` |
| `score` | 数字 | 否 | - | `99.9` |
| `avgRating` | 数字 | 否 | - | `99.9` |
| `isOnline` | 布尔 | 否 | - | `true` |
| `viewCount` | 整数 | 否 | - | `1` |
| `orderCount` | 整数 | 否 | - | `1` |
| `favoriteCount` | 整数 | 否 | - | `1` |
| `tags` | JsonNode | 否 | - | `{}` |
| `statusCode` | 字符串 | 否 | - | `"1"` |
| `auditStatus` | 字符串 | 否 | - | `"1"` |
| `auditComment` | 字符串 | 否 | - | `"示例值"` |
| `auditTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `auditAdminId` | 整数 | 否 | - | `1` |
| `submitTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `stock` | 整数 | 否 | - | `1` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `status` | 布尔 | 否 | - | `true` |
| `cookingMinutes` | 整数 | 否 | - | `1` |
| `dishId` | 字符串 | 否 | - | `"10001"` |
| `dishName` | 字符串 | 否 | - | `"示例名称"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "merchantId": "10001",
  "merchantName": "示例名称",
  "category": "示例值",
  "price": 99.9,
  "calorie": 1
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 10. updateDishStatus

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateDishStatus |
| 请求地址 | `/api/v1/dishes/{dishId}/status` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `dishId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## dish-step-controller

### 1. batchMarkDishSteps

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | batchMarkDishSteps |
| 请求地址 | `/api/v1/dish-steps/batch-mark` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `targetStepStatus` | 整数 | 是 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 字符串 | - | `"示例值"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
[
  "示例值"
]
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": "示例值"
}
```

### 2. batchUpdateDishSteps

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | batchUpdateDishSteps |
| 请求地址 | `/api/v1/dish-steps/batch-update` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `orderDishIds` | 数组<字符串> | 否 | - | `["示例值"]` |
| `orderDishId` | 字符串 | 否 | - | `"10001"` |
| `newStepStatus` | 整数 | 是 | - | `1` |
| `operationType` | 字符串 | 否 | - | `"示例值"` |
| `rollbackReason` | 字符串 | 否 | - | `"示例内容"` |
| `remark` | 字符串 | 否 | - | `"示例内容"` |
| `estimatedMinutes` | 整数 | 否 | - | `1` |
| `stepSortItems` | 数组<StepSortItem> | 否 | - | `[{"orderDishId": "10001", "sort": 1}]` |
| `stepSortItems[].orderDishId` | 字符串 | 是 | - | `"10001"` |
| `stepSortItems[].sort` | 整数 | 是 | - | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 字符串 | - | `"示例值"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "orderDishIds": [
    "示例值"
  ],
  "orderDishId": "10001",
  "newStepStatus": 1,
  "operationType": "示例值",
  "rollbackReason": "示例内容"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": "示例值"
}
```

### 3. getDishStepDetail

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getDishStepDetail |
| 请求地址 | `/api/v1/dish-steps/detail/{orderDishId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `orderDishId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | DishStepDetailVO | - | `{"orderDishId": "10001", "orderId": "10001", "dishId": "10001", "dishName": "示例名称", "dishImage": "https://example.com/file.png"}` |
| `data.orderDishId` | 字符串 | - | `"10001"` |
| `data.orderId` | 字符串 | - | `"10001"` |
| `data.dishId` | 字符串 | - | `"10001"` |
| `data.dishName` | 字符串 | - | `"示例名称"` |
| `data.dishImage` | 字符串 | - | `"https://example.com/file.png"` |
| `data.quantity` | 整数 | - | `1` |
| `data.stepStatus` | 整数 | - | `1` |
| `data.stepStatusName` | 字符串 | - | `"1"` |
| `data.stepStartTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data.estimatedCompletionTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data.cookingMinutes` | 整数 | - | `1` |
| `data.stepSort` | 整数 | - | `1` |
| `data.isFastFood` | 布尔 | - | `true` |
| `data.servingStatus` | 整数 | - | `1` |
| `data.elapsedMinutes` | 整数 | - | `1` |
| `data.remainingMinutes` | 整数 | - | `1` |
| `data.progressPercent` | 整数 | - | `1` |
| `data.stepHistory` | 数组<StepHistoryItem> | - | `[{"id": "10001", "oldStepStatus": 1, "oldStepStatusName": "1", "newStepStatus": 1, "newStepStatusName": "1"}]` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {
    "orderDishId": "10001",
    "orderId": "10001",
    "dishId": "10001",
    "dishName": "示例名称",
    "dishImage": "https://example.com/file.png"
  }
}
```

### 4. filterByStepStatus

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | filterByStepStatus |
| 请求地址 | `/api/v1/dish-steps/filter` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `orderId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `stepStatus` | 整数 | 是 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 数组<DishStepDetailVO> | - | `[{"orderDishId": "10001", "orderId": "10001", "dishId": "10001", "dishName": "示例名称", "dishImage": "https://example.com/file.png"}]` |
| `data[].orderDishId` | 字符串 | - | `"10001"` |
| `data[].orderId` | 字符串 | - | `"10001"` |
| `data[].dishId` | 字符串 | - | `"10001"` |
| `data[].dishName` | 字符串 | - | `"示例名称"` |
| `data[].dishImage` | 字符串 | - | `"https://example.com/file.png"` |
| `data[].quantity` | 整数 | - | `1` |
| `data[].stepStatus` | 整数 | - | `1` |
| `data[].stepStatusName` | 字符串 | - | `"1"` |
| `data[].stepStartTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].estimatedCompletionTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].cookingMinutes` | 整数 | - | `1` |
| `data[].stepSort` | 整数 | - | `1` |
| `data[].isFastFood` | 布尔 | - | `true` |
| `data[].servingStatus` | 整数 | - | `1` |
| `data[].elapsedMinutes` | 整数 | - | `1` |
| `data[].remainingMinutes` | 整数 | - | `1` |
| `data[].progressPercent` | 整数 | - | `1` |
| `data[].stepHistory` | 数组<StepHistoryItem> | - | `[{"id": "10001", "oldStepStatus": 1, "oldStepStatusName": "1", "newStepStatus": 1, "newStepStatusName": "1"}]` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": [
    {}
  ]
}
```

### 5. initializeOrderDishSteps

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | initializeOrderDishSteps |
| 请求地址 | `/api/v1/dish-steps/initialize/{orderId}` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `orderId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 布尔 | - | `true` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": true
}
```

### 6. getOrderDishSteps

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getOrderDishSteps |
| 请求地址 | `/api/v1/dish-steps/order/{orderId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `orderId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 数组<DishStepDetailVO> | - | `[{"orderDishId": "10001", "orderId": "10001", "dishId": "10001", "dishName": "示例名称", "dishImage": "https://example.com/file.png"}]` |
| `data[].orderDishId` | 字符串 | - | `"10001"` |
| `data[].orderId` | 字符串 | - | `"10001"` |
| `data[].dishId` | 字符串 | - | `"10001"` |
| `data[].dishName` | 字符串 | - | `"示例名称"` |
| `data[].dishImage` | 字符串 | - | `"https://example.com/file.png"` |
| `data[].quantity` | 整数 | - | `1` |
| `data[].stepStatus` | 整数 | - | `1` |
| `data[].stepStatusName` | 字符串 | - | `"1"` |
| `data[].stepStartTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].estimatedCompletionTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].cookingMinutes` | 整数 | - | `1` |
| `data[].stepSort` | 整数 | - | `1` |
| `data[].isFastFood` | 布尔 | - | `true` |
| `data[].servingStatus` | 整数 | - | `1` |
| `data[].elapsedMinutes` | 整数 | - | `1` |
| `data[].remainingMinutes` | 整数 | - | `1` |
| `data[].progressPercent` | 整数 | - | `1` |
| `data[].stepHistory` | 数组<StepHistoryItem> | - | `[{"id": "10001", "oldStepStatus": 1, "oldStepStatusName": "1", "newStepStatus": 1, "newStepStatusName": "1"}]` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": [
    {}
  ]
}
```

### 7. rollbackDishStep

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | rollbackDishStep |
| 请求地址 | `/api/v1/dish-steps/rollback` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `orderDishIds` | 数组<字符串> | 否 | - | `["示例值"]` |
| `orderDishId` | 字符串 | 否 | - | `"10001"` |
| `newStepStatus` | 整数 | 是 | - | `1` |
| `operationType` | 字符串 | 否 | - | `"示例值"` |
| `rollbackReason` | 字符串 | 否 | - | `"示例内容"` |
| `remark` | 字符串 | 否 | - | `"示例内容"` |
| `estimatedMinutes` | 整数 | 否 | - | `1` |
| `stepSortItems` | 数组<StepSortItem> | 否 | - | `[{"orderDishId": "10001", "sort": 1}]` |
| `stepSortItems[].orderDishId` | 字符串 | 是 | - | `"10001"` |
| `stepSortItems[].sort` | 整数 | 是 | - | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 布尔 | - | `true` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "orderDishIds": [
    "示例值"
  ],
  "orderDishId": "10001",
  "newStepStatus": 1,
  "operationType": "示例值",
  "rollbackReason": "示例内容"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": true
}
```

### 8. updateDishStep

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateDishStep |
| 请求地址 | `/api/v1/dish-steps/update` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `orderDishIds` | 数组<字符串> | 否 | - | `["示例值"]` |
| `orderDishId` | 字符串 | 否 | - | `"10001"` |
| `newStepStatus` | 整数 | 是 | - | `1` |
| `operationType` | 字符串 | 否 | - | `"示例值"` |
| `rollbackReason` | 字符串 | 否 | - | `"示例内容"` |
| `remark` | 字符串 | 否 | - | `"示例内容"` |
| `estimatedMinutes` | 整数 | 否 | - | `1` |
| `stepSortItems` | 数组<StepSortItem> | 否 | - | `[{"orderDishId": "10001", "sort": 1}]` |
| `stepSortItems[].orderDishId` | 字符串 | 是 | - | `"10001"` |
| `stepSortItems[].sort` | 整数 | 是 | - | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 布尔 | - | `true` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "orderDishIds": [
    "示例值"
  ],
  "orderDishId": "10001",
  "newStepStatus": 1,
  "operationType": "示例值",
  "rollbackReason": "示例内容"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": true
}
```

### 9. updateStepSort

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateStepSort |
| 请求地址 | `/api/v1/dish-steps/update-sort` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 布尔 | - | `true` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
[
  {
    "orderDishId": "10001",
    "sort": 1
  }
]
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": true
}
```

## favorite-controller

### 1. getFavorites

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getFavorites |
| 请求地址 | `/api/v1/favorites` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `type` | 字符串 | 否 | -（位置：query） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. getDishFavorites

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getDishFavorites |
| 请求地址 | `/api/v1/favorites/dishes` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. addDishFavorite

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | addDishFavorite |
| 请求地址 | `/api/v1/favorites/dishes` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 否 | - | `"10001"` |
| `userId` | 字符串 | 否 | - | `"10001"` |
| `collectableType` | 字符串 | 否 | - | `"示例值"` |
| `collectableId` | 字符串 | 否 | - | `"10001"` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "userId": "10001",
  "collectableType": "示例值",
  "collectableId": "10001",
  "createTime": "2026-04-15 21:00:00"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. removeDishFavorite

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | removeDishFavorite |
| 请求地址 | `/api/v1/favorites/dishes/{dishId}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `dishId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. checkDishFavorite

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | checkDishFavorite |
| 请求地址 | `/api/v1/favorites/dishes/{dishId}/check` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `dishId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. addMerchantFavorite

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | addMerchantFavorite |
| 请求地址 | `/api/v1/favorites/merchants` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 否 | - | `"10001"` |
| `userId` | 字符串 | 否 | - | `"10001"` |
| `collectableType` | 字符串 | 否 | - | `"示例值"` |
| `collectableId` | 字符串 | 否 | - | `"10001"` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "userId": "10001",
  "collectableType": "示例值",
  "collectableId": "10001",
  "createTime": "2026-04-15 21:00:00"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 7. removeMerchantFavorite

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | removeMerchantFavorite |
| 请求地址 | `/api/v1/favorites/merchants/{merchantId}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 8. getRecipeFavorites

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getRecipeFavorites |
| 请求地址 | `/api/v1/favorites/recipes` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 9. addRecipeFavorite

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | addRecipeFavorite |
| 请求地址 | `/api/v1/favorites/recipes` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 否 | - | `"10001"` |
| `userId` | 字符串 | 否 | - | `"10001"` |
| `collectableType` | 字符串 | 否 | - | `"示例值"` |
| `collectableId` | 字符串 | 否 | - | `"10001"` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "userId": "10001",
  "collectableType": "示例值",
  "collectableId": "10001",
  "createTime": "2026-04-15 21:00:00"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 10. removeRecipeFavorite

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | removeRecipeFavorite |
| 请求地址 | `/api/v1/favorites/recipes/{recipeId}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `recipeId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## festival-controller

### 1. getActiveFestivals

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getActiveFestivals |
| 请求地址 | `/api/v1/festival/active` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 数组<FestivalRecommendVO> | - | `[{"festivalId": "10001", "festivalName": "示例名称", "festivalType": "示例值", "description": "示例值", "icon": "示例值"}]` |
| `data[].festivalId` | 字符串 | - | `"10001"` |
| `data[].festivalName` | 字符串 | - | `"示例名称"` |
| `data[].festivalType` | 字符串 | - | `"示例值"` |
| `data[].description` | 字符串 | - | `"示例值"` |
| `data[].icon` | 字符串 | - | `"示例值"` |
| `data[].themeColor` | 字符串 | - | `"示例值"` |
| `data[].backgroundImage` | 字符串 | - | `"https://example.com/file.png"` |
| `data[].isCurrent` | 布尔 | - | `true` |
| `data[].daysUntilFestival` | 整数 | - | `1` |
| `data[].recommendDishes` | 数组<DishRecommendItemVO> | - | `[{"dishId": "10001", "dishName": "示例名称", "dishImage": "https://example.com/file.png", "dishPrice": 99.9, "recommendType": "示例值"}]` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": [
    {}
  ]
}
```

### 2. createCustomEvent

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | createCustomEvent |
| 请求地址 | `/api/v1/festival/custom-event` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `eventName` | 字符串 | 是 | - | `"示例名称"` |
| `eventType` | 字符串 | 是 | - | `"示例值"` |
| `eventDate` | 字符串 | 是 | - | `"2026-04-15"` |
| `year` | 整数 | 否 | - | `1` |
| `reminderDays` | 整数 | 否 | - | `1` |
| `description` | 字符串 | 否 | - | `"示例值"` |
| `preferredDishIds` | 数组<字符串> | 否 | - | `["示例值"]` |
| `guestCount` | 整数 | 否 | - | `1` |
| `budgetPerPerson` | 数字 | 否 | - | `99.9` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 字符串 | - | `"示例值"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "eventName": "示例名称",
  "eventType": "示例值",
  "eventDate": "2026-04-15",
  "year": 1,
  "reminderDays": 1
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": "示例值"
}
```

### 3. getCustomEvents

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getCustomEvents |
| 请求地址 | `/api/v1/festival/custom-events` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 数组<UserEventVO> | - | `[{"id": "10001", "eventName": "示例名称", "eventType": "示例值", "eventTypeIcon": "示例值", "eventDate": "2026-04-15"}]` |
| `data[].id` | 字符串 | - | `"10001"` |
| `data[].eventName` | 字符串 | - | `"示例名称"` |
| `data[].eventType` | 字符串 | - | `"示例值"` |
| `data[].eventTypeIcon` | 字符串 | - | `"示例值"` |
| `data[].eventDate` | 字符串 | - | `"2026-04-15"` |
| `data[].year` | 整数 | - | `1` |
| `data[].reminderDays` | 整数 | - | `1` |
| `data[].description` | 字符串 | - | `"示例值"` |
| `data[].preferredDishes` | 数组<字符串> | - | `["示例值"]` |
| `data[].guestCount` | 整数 | - | `1` |
| `data[].budgetPerPerson` | 数字 | - | `99.9` |
| `data[].isActive` | 布尔 | - | `true` |
| `data[].daysUntilEvent` | 整数 | - | `1` |
| `data[].createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": [
    {}
  ]
}
```

### 4. getUpcomingEvents

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getUpcomingEvents |
| 请求地址 | `/api/v1/festival/custom-events/upcoming` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `days` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 数组<UserEventVO> | - | `[{"id": "10001", "eventName": "示例名称", "eventType": "示例值", "eventTypeIcon": "示例值", "eventDate": "2026-04-15"}]` |
| `data[].id` | 字符串 | - | `"10001"` |
| `data[].eventName` | 字符串 | - | `"示例名称"` |
| `data[].eventType` | 字符串 | - | `"示例值"` |
| `data[].eventTypeIcon` | 字符串 | - | `"示例值"` |
| `data[].eventDate` | 字符串 | - | `"2026-04-15"` |
| `data[].year` | 整数 | - | `1` |
| `data[].reminderDays` | 整数 | - | `1` |
| `data[].description` | 字符串 | - | `"示例值"` |
| `data[].preferredDishes` | 数组<字符串> | - | `["示例值"]` |
| `data[].guestCount` | 整数 | - | `1` |
| `data[].budgetPerPerson` | 数字 | - | `99.9` |
| `data[].isActive` | 布尔 | - | `true` |
| `data[].daysUntilEvent` | 整数 | - | `1` |
| `data[].createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": [
    {}
  ]
}
```

### 5. submitFeedback_1

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | submitFeedback_1 |
| 请求地址 | `/api/v1/festival/feedback` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `recommendHistoryId` | 字符串 | 是 | - | `"10001"` |
| `isClicked` | 布尔 | 否 | - | `true` |
| `isOrdered` | 布尔 | 否 | - | `true` |
| `feedbackScore` | 整数 | 否 | - | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 布尔 | - | `true` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "recommendHistoryId": "10001",
  "isClicked": true,
  "isOrdered": true,
  "feedbackScore": 1
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": true
}
```

### 6. getHomepageRecommends

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getHomepageRecommends |
| 请求地址 | `/api/v1/festival/homepage` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `limit` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 数组<FestivalRecommendVO> | - | `[{"festivalId": "10001", "festivalName": "示例名称", "festivalType": "示例值", "description": "示例值", "icon": "示例值"}]` |
| `data[].festivalId` | 字符串 | - | `"10001"` |
| `data[].festivalName` | 字符串 | - | `"示例名称"` |
| `data[].festivalType` | 字符串 | - | `"示例值"` |
| `data[].description` | 字符串 | - | `"示例值"` |
| `data[].icon` | 字符串 | - | `"示例值"` |
| `data[].themeColor` | 字符串 | - | `"示例值"` |
| `data[].backgroundImage` | 字符串 | - | `"https://example.com/file.png"` |
| `data[].isCurrent` | 布尔 | - | `true` |
| `data[].daysUntilFestival` | 整数 | - | `1` |
| `data[].recommendDishes` | 数组<DishRecommendItemVO> | - | `[{"dishId": "10001", "dishName": "示例名称", "dishImage": "https://example.com/file.png", "dishPrice": 99.9, "recommendType": "示例值"}]` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": [
    {}
  ]
}
```

### 7. addFestivalDishRecommend

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | addFestivalDishRecommend |
| 请求地址 | `/api/v1/festival/recommend` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `festivalId` | 字符串 | 是 | - | `"10001"` |
| `dishIds` | 数组<字符串> | 是 | - | `["示例值"]` |
| `recommendType` | 字符串 | 是 | - | `"示例值"` |
| `recommendReason` | 字符串 | 否 | - | `"示例内容"` |
| `position` | 整数 | 否 | - | `1` |
| `priority` | 整数 | 否 | - | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 布尔 | - | `true` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "festivalId": "10001",
  "dishIds": [
    "示例值"
  ],
  "recommendType": "示例值",
  "recommendReason": "示例内容",
  "position": 1
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": true
}
```

### 8. getCurrentRecommendations

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getCurrentRecommendations |
| 请求地址 | `/api/v1/festival/recommendations/current` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 数组<FestivalRecommendVO> | - | `[{"festivalId": "10001", "festivalName": "示例名称", "festivalType": "示例值", "description": "示例值", "icon": "示例值"}]` |
| `data[].festivalId` | 字符串 | - | `"10001"` |
| `data[].festivalName` | 字符串 | - | `"示例名称"` |
| `data[].festivalType` | 字符串 | - | `"示例值"` |
| `data[].description` | 字符串 | - | `"示例值"` |
| `data[].icon` | 字符串 | - | `"示例值"` |
| `data[].themeColor` | 字符串 | - | `"示例值"` |
| `data[].backgroundImage` | 字符串 | - | `"https://example.com/file.png"` |
| `data[].isCurrent` | 布尔 | - | `true` |
| `data[].daysUntilFestival` | 整数 | - | `1` |
| `data[].recommendDishes` | 数组<DishRecommendItemVO> | - | `[{"dishId": "10001", "dishName": "示例名称", "dishImage": "https://example.com/file.png", "dishPrice": 99.9, "recommendType": "示例值"}]` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": [
    {}
  ]
}
```

### 9. getRecommendationsByFestival

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getRecommendationsByFestival |
| 请求地址 | `/api/v1/festival/recommendations/festival/{festivalId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `festivalId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 数组<FestivalRecommendVO> | - | `[{"festivalId": "10001", "festivalName": "示例名称", "festivalType": "示例值", "description": "示例值", "icon": "示例值"}]` |
| `data[].festivalId` | 字符串 | - | `"10001"` |
| `data[].festivalName` | 字符串 | - | `"示例名称"` |
| `data[].festivalType` | 字符串 | - | `"示例值"` |
| `data[].description` | 字符串 | - | `"示例值"` |
| `data[].icon` | 字符串 | - | `"示例值"` |
| `data[].themeColor` | 字符串 | - | `"示例值"` |
| `data[].backgroundImage` | 字符串 | - | `"https://example.com/file.png"` |
| `data[].isCurrent` | 布尔 | - | `true` |
| `data[].daysUntilFestival` | 整数 | - | `1` |
| `data[].recommendDishes` | 数组<DishRecommendItemVO> | - | `[{"dishId": "10001", "dishName": "示例名称", "dishImage": "https://example.com/file.png", "dishPrice": 99.9, "recommendType": "示例值"}]` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": [
    {}
  ]
}
```

### 10. getFestivalsByType

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getFestivalsByType |
| 请求地址 | `/api/v1/festival/type/{festivalType}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `festivalType` | 字符串 | 是 | -（位置：path） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 数组<FestivalRecommendVO> | - | `[{"festivalId": "10001", "festivalName": "示例名称", "festivalType": "示例值", "description": "示例值", "icon": "示例值"}]` |
| `data[].festivalId` | 字符串 | - | `"10001"` |
| `data[].festivalName` | 字符串 | - | `"示例名称"` |
| `data[].festivalType` | 字符串 | - | `"示例值"` |
| `data[].description` | 字符串 | - | `"示例值"` |
| `data[].icon` | 字符串 | - | `"示例值"` |
| `data[].themeColor` | 字符串 | - | `"示例值"` |
| `data[].backgroundImage` | 字符串 | - | `"https://example.com/file.png"` |
| `data[].isCurrent` | 布尔 | - | `true` |
| `data[].daysUntilFestival` | 整数 | - | `1` |
| `data[].recommendDishes` | 数组<DishRecommendItemVO> | - | `[{"dishId": "10001", "dishName": "示例名称", "dishImage": "https://example.com/file.png", "dishPrice": 99.9, "recommendType": "示例值"}]` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": [
    {}
  ]
}
```

### 11. getUpcomingFestivals

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getUpcomingFestivals |
| 请求地址 | `/api/v1/festival/upcoming` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `days` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 数组<FestivalRecommendVO> | - | `[{"festivalId": "10001", "festivalName": "示例名称", "festivalType": "示例值", "description": "示例值", "icon": "示例值"}]` |
| `data[].festivalId` | 字符串 | - | `"10001"` |
| `data[].festivalName` | 字符串 | - | `"示例名称"` |
| `data[].festivalType` | 字符串 | - | `"示例值"` |
| `data[].description` | 字符串 | - | `"示例值"` |
| `data[].icon` | 字符串 | - | `"示例值"` |
| `data[].themeColor` | 字符串 | - | `"示例值"` |
| `data[].backgroundImage` | 字符串 | - | `"https://example.com/file.png"` |
| `data[].isCurrent` | 布尔 | - | `true` |
| `data[].daysUntilFestival` | 整数 | - | `1` |
| `data[].recommendDishes` | 数组<DishRecommendItemVO> | - | `[{"dishId": "10001", "dishName": "示例名称", "dishImage": "https://example.com/file.png", "dishPrice": 99.9, "recommendType": "示例值"}]` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": [
    {}
  ]
}
```

## file-access-controller

### 1. accessFile

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | accessFile |
| 请求地址 | `/api/files/{filename}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `filename` | 字符串 | 是 | -（位置：path） | `"https://example.com/file.png"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
"示例值"
```

### 2. accessFileCompat

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | accessFileCompat |
| 请求地址 | `/api/{filename}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `filename` | 字符串 | 是 | -（位置：path） | `"https://example.com/file.png"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
"示例值"
```

## file-controller

### 1. getFile

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getFile |
| 请求地址 | `/api/files/**` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
"示例值"
```

### 2. getChatImage

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getChatImage |
| 请求地址 | `/api/files/chat/{filename}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `filename` | 字符串 | 是 | -（位置：path） | `"https://example.com/file.png"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
"示例值"
```

### 3. getImage

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getImage |
| 请求地址 | `/api/files/image/{filename}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `filename` | 字符串 | 是 | -（位置：path） | `"https://example.com/file.png"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
"示例值"
```

## group-controller

### 1. createGroup

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | createGroup |
| 请求地址 | `/api/v1/groups` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 否 | - | `"10001"` |
| `creatorId` | 字符串 | 否 | - | `"10001"` |
| `groupName` | 字符串 | 否 | - | `"示例名称"` |
| `description` | 字符串 | 否 | - | `"示例值"` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "creatorId": "10001",
  "groupName": "示例名称",
  "description": "示例值",
  "createTime": "2026-04-15 21:00:00"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. getMyGroups

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getMyGroups |
| 请求地址 | `/api/v1/groups/my` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. getGroupById

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getGroupById |
| 请求地址 | `/api/v1/groups/{groupIdOrSessionId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupIdOrSessionId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. deleteGroup

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | deleteGroup |
| 请求地址 | `/api/v1/groups/{groupId}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. updateGroup

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateGroup |
| 请求地址 | `/api/v1/groups/{groupId}` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `id` | 字符串 | 否 | - | `"10001"` |
| `creatorId` | 字符串 | 否 | - | `"10001"` |
| `groupName` | 字符串 | 否 | - | `"示例名称"` |
| `description` | 字符串 | 否 | - | `"示例值"` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "creatorId": "10001",
  "groupName": "示例名称",
  "description": "示例值",
  "createTime": "2026-04-15 21:00:00"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. leaveGroup

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | leaveGroup |
| 请求地址 | `/api/v1/groups/{groupId}/leave` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 7. getGroupMembers

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getGroupMembers |
| 请求地址 | `/api/v1/groups/{groupId}/members` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 8. addMember

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | addMember |
| 请求地址 | `/api/v1/groups/{groupId}/members` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `role` | 字符串 | 否 | -（位置：query） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 9. removeMember

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | removeMember |
| 请求地址 | `/api/v1/groups/{groupId}/members/{targetUserId}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `targetUserId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `operatorId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 10. isGroupMember

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | isGroupMember |
| 请求地址 | `/api/v1/groups/{groupId}/members/{userId}/check` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 11. getUserRole

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getUserRole |
| 请求地址 | `/api/v1/groups/{groupId}/members/{userId}/role` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## group-order-addition-controller

### 1. getApprovedPendingPayments

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getApprovedPendingPayments |
| 请求地址 | `/api/v1/group-order-additions/approved-pending-payment` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupOrderId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. payAdditionPool

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | payAdditionPool |
| 请求地址 | `/api/v1/group-order-additions/pay-pool` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. getPendingAdditions

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getPendingAdditions |
| 请求地址 | `/api/v1/group-order-additions/pending` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupOrderId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. requestAddDish

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | requestAddDish |
| 请求地址 | `/api/v1/group-order-additions/request` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. getAdditionDetail

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getAdditionDetail |
| 请求地址 | `/api/v1/group-order-additions/{additionId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `additionId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. reviewAddition

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | reviewAddition |
| 请求地址 | `/api/v1/group-order-additions/{additionId}/review` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `additionId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## group-order-chat-controller

### 1. markMessageAsSynced

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | markMessageAsSynced |
| 请求地址 | `/api/v1/group-order-chat/messages/{messageId}/synced` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `messageId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. getPendingSyncMessages

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getPendingSyncMessages |
| 请求地址 | `/api/v1/group-order-chat/pending-sync/{merchantId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `sessionType` | 字符串 | 否 | -（位置：query） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. getGroupOrderSession

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getGroupOrderSession |
| 请求地址 | `/api/v1/group-order-chat/sessions/{groupOrderId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupOrderId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. createGroupOrderSession

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | createGroupOrderSession |
| 请求地址 | `/api/v1/group-order-chat/sessions/{groupOrderId}` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupOrderId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `groupId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `merchantId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. archiveGroupOrderSession

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | archiveGroupOrderSession |
| 请求地址 | `/api/v1/group-order-chat/sessions/{groupOrderId}/archive` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupOrderId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. hasGroupOrderSession

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | hasGroupOrderSession |
| 请求地址 | `/api/v1/group-order-chat/sessions/{groupOrderId}/exists` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupOrderId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 7. syncMessageToGroup

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | syncMessageToGroup |
| 请求地址 | `/api/v1/group-order-chat/sync` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## group-order-controller

### 1. createGroupOrder

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | createGroupOrder |
| 请求地址 | `/api/v1/group-orders/group-orders` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. cancelGroupOrder

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | cancelGroupOrder |
| 请求地址 | `/api/v1/group-orders/group-orders/{groupOrderId}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupOrderId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. getGroupOrderDetail

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getGroupOrderDetail |
| 请求地址 | `/api/v1/group-orders/group-orders/{groupOrderId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupOrderId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. updateGroupOrderStatus

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateGroupOrderStatus |
| 请求地址 | `/api/v1/group-orders/group-orders/{groupOrderId}/status` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupOrderId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. syncGroupOrderMessage

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | syncGroupOrderMessage |
| 请求地址 | `/api/v1/group-orders/group-orders/{groupOrderId}/sync-message` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupOrderId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. getOrCreateDraftOrder

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getOrCreateDraftOrder |
| 请求地址 | `/api/v1/group-orders/groups/{groupId}/draft-order` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `initiatorId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 7. getGroupOrders

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getGroupOrders |
| 请求地址 | `/api/v1/group-orders/groups/{groupId}/orders` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `groupId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `status` | 整数 | 否 | -（位置：query） | `1` |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `size` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## home-controller

### 1. getHotTopic

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getHotTopic |
| 请求地址 | `/api/v1/home/hot-topic` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | HotTopicResponse | - | `{"content": "示例内容", "sourceType": "示例值", "sourceId": "10001", "redirectUrl": "https://example.com/file.png", "clickable": true}` |
| `data.content` | 字符串 | - | `"示例内容"` |
| `data.sourceType` | 字符串 | - | `"示例值"` |
| `data.sourceId` | 字符串 | - | `"10001"` |
| `data.redirectUrl` | 字符串 | - | `"https://example.com/file.png"` |
| `data.clickable` | 布尔 | - | `true` |
| `data.createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {
    "content": "示例内容",
    "sourceType": "示例值",
    "sourceId": "10001",
    "redirectUrl": "https://example.com/file.png",
    "clickable": true
  }
}
```

### 2. recordClick

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | recordClick |
| 请求地址 | `/api/v1/home/hot-topic/click` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. recordShare

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | recordShare |
| 请求地址 | `/api/v1/home/hot-topic/share` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## hot-topic-admin-controller

### 1. list

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | list |
| 请求地址 | `/api/v1/admin/hot-topic` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `size` | 整数 | 否 | -（位置：query） | `1` |
| `status` | 字符串 | 否 | -（位置：query） | `"1"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `records` | 数组<HotTopic> | - | `[{"id": "10001", "content": "示例内容", "priority": 1, "sourceType": "示例值", "sourceId": "10001"}]` |
| `records[].id` | 字符串 | - | `"10001"` |
| `records[].content` | 字符串 | - | `"示例内容"` |
| `records[].priority` | 整数 | - | `1` |
| `records[].sourceType` | 字符串 | - | `"示例值"` |
| `records[].sourceId` | 字符串 | - | `"10001"` |
| `records[].startDate` | 字符串 | - | `"2026-04-15"` |
| `records[].endDate` | 字符串 | - | `"2026-04-15"` |
| `records[].status` | 字符串 | - | `"1"` |
| `records[].clickCount` | 整数 | - | `1` |
| `records[].shareCount` | 整数 | - | `1` |
| `records[].requireReview` | 布尔 | - | `true` |
| `records[].reviewStatus` | 字符串 | - | `"1"` |
| `records[].reviewerId` | 整数 | - | `1` |
| `records[].reviewTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].reviewComment` | 字符串 | - | `"示例值"` |
| `records[].remark` | 字符串 | - | `"示例内容"` |
| `records[].createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].createBy` | 整数 | - | `1` |
| `records[].updateBy` | 整数 | - | `1` |
| `records[].active` | 布尔 | - | `true` |
| `total` | 整数 | - | `1` |
| `current` | 整数 | - | `1` |
| `pages` | 整数 | - | `1` |
| `size` | 整数 | - | `1` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "records": [
    {}
  ],
  "total": 1,
  "current": 1,
  "pages": 1,
  "size": 1
}
```

### 2. batchDelete

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | batchDelete |
| 请求地址 | `/api/v1/admin/hot-topic/batch-delete` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
[
  "示例值"
]
```

#### 响应示例

```json
{}
```

### 3. create

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | create |
| 请求地址 | `/api/v1/admin/hot-topic/create` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 否 | - | `"10001"` |
| `content` | 字符串 | 否 | - | `"示例内容"` |
| `priority` | 整数 | 否 | - | `1` |
| `sourceType` | 字符串 | 否 | - | `"示例值"` |
| `sourceId` | 字符串 | 否 | - | `"10001"` |
| `startDate` | 字符串 | 否 | - | `"2026-04-15"` |
| `endDate` | 字符串 | 否 | - | `"2026-04-15"` |
| `status` | 字符串 | 否 | - | `"1"` |
| `clickCount` | 整数 | 否 | - | `1` |
| `shareCount` | 整数 | 否 | - | `1` |
| `requireReview` | 布尔 | 否 | - | `true` |
| `reviewStatus` | 字符串 | 否 | - | `"1"` |
| `reviewerId` | 整数 | 否 | - | `1` |
| `reviewTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `reviewComment` | 字符串 | 否 | - | `"示例值"` |
| `remark` | 字符串 | 否 | - | `"示例内容"` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `createBy` | 整数 | 否 | - | `1` |
| `updateBy` | 整数 | 否 | - | `1` |
| `active` | 布尔 | 否 | - | `true` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "content": "示例内容",
  "priority": 1,
  "sourceType": "示例值",
  "sourceId": "10001"
}
```

#### 响应示例

```json
{}
```

### 4. delete

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | delete |
| 请求地址 | `/api/v1/admin/hot-topic/delete/{id}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 5. detail

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | detail |
| 请求地址 | `/api/v1/admin/hot-topic/detail/{id}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 6. review

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | review |
| 请求地址 | `/api/v1/admin/hot-topic/review/{id}` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `reviewerId` | 整数 | 是 | -（位置：query） | `1` |
| `approved` | 布尔 | 是 | -（位置：query） | `true` |
| `comment` | 字符串 | 否 | -（位置：query） | `"示例值"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 7. statistics

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | statistics |
| 请求地址 | `/api/v1/admin/hot-topic/statistics` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 8. update

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | update |
| 请求地址 | `/api/v1/admin/hot-topic/update/{id}` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `id` | 字符串 | 否 | - | `"10001"` |
| `content` | 字符串 | 否 | - | `"示例内容"` |
| `priority` | 整数 | 否 | - | `1` |
| `sourceType` | 字符串 | 否 | - | `"示例值"` |
| `sourceId` | 字符串 | 否 | - | `"10001"` |
| `startDate` | 字符串 | 否 | - | `"2026-04-15"` |
| `endDate` | 字符串 | 否 | - | `"2026-04-15"` |
| `status` | 字符串 | 否 | - | `"1"` |
| `clickCount` | 整数 | 否 | - | `1` |
| `shareCount` | 整数 | 否 | - | `1` |
| `requireReview` | 布尔 | 否 | - | `true` |
| `reviewStatus` | 字符串 | 否 | - | `"1"` |
| `reviewerId` | 整数 | 否 | - | `1` |
| `reviewTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `reviewComment` | 字符串 | 否 | - | `"示例值"` |
| `remark` | 字符串 | 否 | - | `"示例内容"` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `createBy` | 整数 | 否 | - | `1` |
| `updateBy` | 整数 | 否 | - | `1` |
| `active` | 布尔 | 否 | - | `true` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "content": "示例内容",
  "priority": 1,
  "sourceType": "示例值",
  "sourceId": "10001"
}
```

#### 响应示例

```json
{}
```

## ingredient-conflict-rule-controller

### 1. getEnabledRules

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getEnabledRules |
| 请求地址 | `/api/v1/ingredient-conflict-rules` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. createRule

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | createRule |
| 请求地址 | `/api/v1/ingredient-conflict-rules` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 否 | - | `"10001"` |
| `ruleName` | 字符串 | 否 | - | `"示例名称"` |
| `conflictType` | 字符串 | 否 | - | `"示例值"` |
| `mainIngredients` | 字符串 | 否 | - | `"示例值"` |
| `conflictTags` | 字符串 | 否 | - | `"示例值"` |
| `severity` | 整数 | 否 | - | `1` |
| `priority` | 整数 | 否 | - | `1` |
| `description` | 字符串 | 否 | - | `"示例值"` |
| `suggestion` | 字符串 | 否 | - | `"示例值"` |
| `enabled` | 布尔 | 否 | - | `true` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "ruleName": "示例名称",
  "conflictType": "示例值",
  "mainIngredients": "示例值",
  "conflictTags": "示例值"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. batchDeleteRules

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | batchDeleteRules |
| 请求地址 | `/api/v1/ingredient-conflict-rules/batch` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. checkDishConflicts

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | checkDishConflicts |
| 请求地址 | `/api/v1/ingredient-conflict-rules/check-dish` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. checkConflicts

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | checkConflicts |
| 请求地址 | `/api/v1/ingredient-conflict-rules/check-ingredients` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. getRulesBySeverity

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getRulesBySeverity |
| 请求地址 | `/api/v1/ingredient-conflict-rules/severity/{severity}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `severity` | 整数 | 是 | -（位置：path） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 7. getRulesByType

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getRulesByType |
| 请求地址 | `/api/v1/ingredient-conflict-rules/type/{conflictType}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `conflictType` | 字符串 | 是 | -（位置：path） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 8. deleteRule

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | deleteRule |
| 请求地址 | `/api/v1/ingredient-conflict-rules/{id}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 9. updateRule

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateRule |
| 请求地址 | `/api/v1/ingredient-conflict-rules/{id}` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `id` | 字符串 | 否 | - | `"10001"` |
| `ruleName` | 字符串 | 否 | - | `"示例名称"` |
| `conflictType` | 字符串 | 否 | - | `"示例值"` |
| `mainIngredients` | 字符串 | 否 | - | `"示例值"` |
| `conflictTags` | 字符串 | 否 | - | `"示例值"` |
| `severity` | 整数 | 否 | - | `1` |
| `priority` | 整数 | 否 | - | `1` |
| `description` | 字符串 | 否 | - | `"示例值"` |
| `suggestion` | 字符串 | 否 | - | `"示例值"` |
| `enabled` | 布尔 | 否 | - | `true` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "ruleName": "示例名称",
  "conflictType": "示例值",
  "mainIngredients": "示例值",
  "conflictTags": "示例值"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## legacy-address-controller

### 1. getAddresses

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getAddresses |
| 请求地址 | `/api/v1/users/{userId}/addresses` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. addAddress

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | addAddress |
| 请求地址 | `/api/v1/users/{userId}/addresses` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `id` | 字符串 | 否 | - | `"10001"` |
| `userId` | 字符串 | 否 | - | `"10001"` |
| `receiverName` | 字符串 | 否 | - | `"示例名称"` |
| `receiverPhone` | 字符串 | 否 | - | `"13800138000"` |
| `province` | 字符串 | 否 | - | `"示例值"` |
| `city` | 字符串 | 否 | - | `"示例值"` |
| `district` | 字符串 | 否 | - | `"示例值"` |
| `detail` | 字符串 | 否 | - | `"示例值"` |
| `isDefault` | 整数 | 否 | - | `1` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "userId": "10001",
  "receiverName": "示例名称",
  "receiverPhone": "13800138000",
  "province": "示例值"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. deleteAddress

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | deleteAddress |
| 请求地址 | `/api/v1/users/{userId}/addresses/{addressId}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `addressId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. updateAddress

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateAddress |
| 请求地址 | `/api/v1/users/{userId}/addresses/{addressId}` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `addressId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `id` | 字符串 | 否 | - | `"10001"` |
| `userId` | 字符串 | 否 | - | `"10001"` |
| `receiverName` | 字符串 | 否 | - | `"示例名称"` |
| `receiverPhone` | 字符串 | 否 | - | `"13800138000"` |
| `province` | 字符串 | 否 | - | `"示例值"` |
| `city` | 字符串 | 否 | - | `"示例值"` |
| `district` | 字符串 | 否 | - | `"示例值"` |
| `detail` | 字符串 | 否 | - | `"示例值"` |
| `isDefault` | 整数 | 否 | - | `1` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "userId": "10001",
  "receiverName": "示例名称",
  "receiverPhone": "13800138000",
  "province": "示例值"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## location-controller

### 1. getCurrentLocation

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getCurrentLocation |
| 请求地址 | `/api/v1/location` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `latitude` | 数字 | 否 | -（位置：query） | `99.9` |
| `longitude` | 数字 | 否 | -（位置：query） | `99.9` |
| `ip` | 字符串 | 否 | -（位置：query） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. getCascaderLocationData

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getCascaderLocationData |
| 请求地址 | `/api/v1/location/cascader` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. geocode

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | geocode |
| 请求地址 | `/api/v1/location/geocode` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `address` | 字符串 | 是 | -（位置：query） | `"示例值"` |
| `city` | 字符串 | 否 | -（位置：query） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. getPublicIp

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getPublicIp |
| 请求地址 | `/api/v1/location/public-ip` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. reverseGeocode

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | reverseGeocode |
| 请求地址 | `/api/v1/location/reverse-geocode` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `lng` | 字符串 | 是 | -（位置：query） | `"示例值"` |
| `lat` | 字符串 | 是 | -（位置：query） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. searchAddress

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | searchAddress |
| 请求地址 | `/api/v1/location/search` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `address` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `keywords` | 字符串 | 否 | -（位置：query） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## menu-controller

### 1. getMenusByDishId

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getMenusByDishId |
| 请求地址 | `/api/v1/menus/dishes/{dishId}/menus` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `dishId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. batchUpdateDishStatusInMenus

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | batchUpdateDishStatusInMenus |
| 请求地址 | `/api/v1/menus/dishes/{dishId}/status` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `dishId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. batchOperateMenus

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | batchOperateMenus |
| 请求地址 | `/api/v1/menus/menu/batch` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. updateDishStatusInMenu

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateDishStatusInMenu |
| 请求地址 | `/api/v1/menus/menu/{menuId}/dishes/{dishId}/status` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `menuId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `dishId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. setMenuSchedule

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | setMenuSchedule |
| 请求地址 | `/api/v1/menus/menu/{menuId}/schedule` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `menuId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. getMenusByMerchantId

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getMenusByMerchantId |
| 请求地址 | `/api/v1/menus/merchants/{merchantId}/menu` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 7. addMenu

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | addMenu |
| 请求地址 | `/api/v1/menus/merchants/{merchantId}/menu` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `id` | 字符串 | 否 | - | `"10001"` |
| `merchantId` | 字符串 | 否 | - | `"10001"` |
| `description` | 字符串 | 否 | - | `"示例值"` |
| `status` | 字符串 | 否 | - | `"1"` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `dishCount` | 整数 | 否 | - | `1` |
| `menuName` | 字符串 | 否 | - | `"示例名称"` |
| `category` | 字符串 | 否 | - | `"示例值"` |
| `autoOnline` | 字符串 | 否 | - | `"示例值"` |
| `autoOffline` | 字符串 | 否 | - | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "merchantId": "10001",
  "description": "示例值",
  "status": "1",
  "createTime": "2026-04-15 21:00:00"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 8. getMenuDetailByMerchant

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getMenuDetailByMerchant |
| 请求地址 | `/api/v1/menus/merchants/{merchantId}/menu/{menuId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `menuId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 9. updateMenu

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateMenu |
| 请求地址 | `/api/v1/menus/merchants/{merchantId}/menu/{menuId}` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `menuId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 10. getMenuDishes

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getMenuDishes |
| 请求地址 | `/api/v1/menus/merchants/{merchantId}/menu/{menuId}/dishes` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `menuId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 11. getMenuDetail

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getMenuDetail |
| 请求地址 | `/api/v1/menus/{menuId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `menuId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## merchant-controller

### 1. getMerchants_1

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getMerchants_1 |
| 请求地址 | `/api/v1/merchant` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `category` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `keyword` | 字符串 | 否 | -（位置：query） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. register_1

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | register_1 |
| 请求地址 | `/api/v1/merchant/register` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `Authorization` | 字符串 | 否 | -（位置：header） | `"示例值"` |
| `name` | 字符串 | 否 | - | `"示例名称"` |
| `businessLicense` | 字符串 | 否 | - | `"示例值"` |
| `businessScope` | 数组<字符串> | 否 | - | `["示例值"]` |
| `contactName` | 字符串 | 否 | - | `"示例名称"` |
| `phone` | 字符串 | 否 | - | `"13800138000"` |
| `email` | 字符串 | 否 | - | `"demo@example.com"` |
| `captcha` | 字符串 | 否 | - | `"示例值"` |
| `captchaKey` | 字符串 | 否 | - | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "name": "示例名称",
  "businessLicense": "示例值",
  "businessScope": [
    "示例值"
  ],
  "contactName": "示例名称",
  "phone": "13800138000"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. getMerchantDetail_1

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getMerchantDetail_1 |
| 请求地址 | `/api/v1/merchant/{merchantId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. updateMerchant

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateMerchant |
| 请求地址 | `/api/v1/merchant/{merchantId}` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. deleteMerchantAlbum

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | deleteMerchantAlbum |
| 请求地址 | `/api/v1/merchant/{merchantId}/album` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `imageUrl` | 字符串 | 是 | -（位置：query） | `"https://example.com/file.png"` |
| `albumType` | 字符串 | 是 | -（位置：query） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. getMerchantAlbum

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getMerchantAlbum |
| 请求地址 | `/api/v1/merchant/{merchantId}/album` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 7. uploadMerchantAlbum

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | uploadMerchantAlbum |
| 请求地址 | `/api/v1/merchant/{merchantId}/album` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `images` | 数组<字符串> | 是 | -（位置：query） | `["https://example.com/file.png"]` |
| `albumType` | 字符串 | 是 | -（位置：query） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 8. getAnnouncements

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getAnnouncements |
| 请求地址 | `/api/v1/merchant/{merchantId}/announcements` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 9. addAnnouncement

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | addAnnouncement |
| 请求地址 | `/api/v1/merchant/{merchantId}/announcements` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `id` | 字符串 | 否 | - | `"10001"` |
| `merchantId` | 字符串 | 否 | - | `"10001"` |
| `title` | 字符串 | 否 | - | `"示例值"` |
| `content` | 字符串 | 否 | - | `"示例内容"` |
| `type` | 字符串 | 否 | - | `"示例值"` |
| `priority` | 字符串 | 否 | - | `"示例值"` |
| `targetAudience` | 字符串 | 否 | - | `"示例值"` |
| `readCount` | 整数 | 否 | - | `1` |
| `readUsers` | 整数 | 否 | - | `1` |
| `status` | 字符串 | 否 | - | `"1"` |
| `startTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `endTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "merchantId": "10001",
  "title": "示例值",
  "content": "示例内容",
  "type": "示例值"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 10. deleteAnnouncement

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | deleteAnnouncement |
| 请求地址 | `/api/v1/merchant/{merchantId}/announcements/{announcementId}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `announcementId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 11. updateAnnouncement

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateAnnouncement |
| 请求地址 | `/api/v1/merchant/{merchantId}/announcements/{announcementId}` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `announcementId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `id` | 字符串 | 否 | - | `"10001"` |
| `merchantId` | 字符串 | 否 | - | `"10001"` |
| `title` | 字符串 | 否 | - | `"示例值"` |
| `content` | 字符串 | 否 | - | `"示例内容"` |
| `type` | 字符串 | 否 | - | `"示例值"` |
| `priority` | 字符串 | 否 | - | `"示例值"` |
| `targetAudience` | 字符串 | 否 | - | `"示例值"` |
| `readCount` | 整数 | 否 | - | `1` |
| `readUsers` | 整数 | 否 | - | `1` |
| `status` | 字符串 | 否 | - | `"1"` |
| `startTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `endTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "merchantId": "10001",
  "title": "示例值",
  "content": "示例内容",
  "type": "示例值"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 12. toggleAnnouncementStatus

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | toggleAnnouncementStatus |
| 请求地址 | `/api/v1/merchant/{merchantId}/announcements/{announcementId}/status` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `announcementId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 13. uploadMerchantAvatar

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | uploadMerchantAvatar |
| 请求地址 | `/api/v1/merchant/{merchantId}/avatar` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `avatar` | 字符串 | 是 | - | `"https://example.com/file.png"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "avatar": "https://example.com/file.png"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 14. getBusinessOverview

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getBusinessOverview |
| 请求地址 | `/api/v1/merchant/{merchantId}/business-overview` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 15. acceptOrder

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | acceptOrder |
| 请求地址 | `/api/v1/merchant/{merchantId}/orders/{orderId}/accept` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `orderId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 16. getStatistics

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getStatistics |
| 请求地址 | `/api/v1/merchant/{merchantId}/statistics` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `timeRange` | 字符串 | 是 | -（位置：query） | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 17. updateMerchantStatus

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateMerchantStatus |
| 请求地址 | `/api/v1/merchant/{merchantId}/status` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## merchant-insight-controller

### 1. generateAiSuggestions

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | generateAiSuggestions |
| 请求地址 | `/api/v1/merchant/insight/{merchantId}/ai-suggestions` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. getFullInsight

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getFullInsight |
| 请求地址 | `/api/v1/merchant/insight/{merchantId}/full` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `timeRange` | 字符串 | 否 | -（位置：query） | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. getMetrics

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getMetrics |
| 请求地址 | `/api/v1/merchant/insight/{merchantId}/metrics` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `timeRange` | 字符串 | 否 | -（位置：query） | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. getRatingDistribution

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getRatingDistribution |
| 请求地址 | `/api/v1/merchant/insight/{merchantId}/rating-distribution` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. getTopDishes

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getTopDishes |
| 请求地址 | `/api/v1/merchant/insight/{merchantId}/top-dishes` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `timeRange` | 字符串 | 否 | -（位置：query） | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. getSalesTrend

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getSalesTrend |
| 请求地址 | `/api/v1/merchant/insight/{merchantId}/trend` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `timeRange` | 字符串 | 否 | -（位置：query） | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## merchant-review-controller

### 1. generateReply

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | generateReply |
| 请求地址 | `/api/v1/merchant/review/generate-reply` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `reviewId` | 字符串 | 否 | - | `"10001"` |
| `reviewContent` | 字符串 | 否 | - | `"示例内容"` |
| `rating` | 整数 | 否 | - | `1` |
| `userName` | 字符串 | 否 | - | `"示例名称"` |
| `styleCount` | 整数 | 否 | - | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "reviewId": "10001",
  "reviewContent": "示例内容",
  "rating": 1,
  "userName": "示例名称",
  "styleCount": 1
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. submitReply

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | submitReply |
| 请求地址 | `/api/v1/merchant/review/submit-reply` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `reviewId` | 字符串 | 否 | - | `"10001"` |
| `merchantId` | 字符串 | 否 | - | `"10001"` |
| `content` | 字符串 | 否 | - | `"示例内容"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "reviewId": "10001",
  "merchantId": "10001",
  "content": "示例内容"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. getPendingReviews

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getPendingReviews |
| 请求地址 | `/api/v1/merchant/review/{merchantId}/pending` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## merchants-controller

### 1. getMerchants

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getMerchants |
| 请求地址 | `/api/v1/merchants` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `category` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `keyword` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `sort` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `size` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. getNearbyMerchants

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getNearbyMerchants |
| 请求地址 | `/api/v1/merchants/nearby` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `latitude` | 数字 | 否 | -（位置：query） | `99.9` |
| `longitude` | 数字 | 否 | -（位置：query） | `99.9` |
| `radius` | 数字 | 否 | -（位置：query） | `99.9` |
| `limit` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. getMerchantDetail

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getMerchantDetail |
| 请求地址 | `/api/v1/merchants/{merchantId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## message-controller

### 1. getMessageList

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getMessageList |
| 请求地址 | `/api/v1/legacy/message/list` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `pageSize` | 整数 | 否 | -（位置：query） | `1` |
| `pageNum` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. sendMessage_1

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | sendMessage_1 |
| 请求地址 | `/api/v1/legacy/message/send` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## message-record-controller

### 1. getMessageRecords

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getMessageRecords |
| 请求地址 | `/api/v1/message/records` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `pageSize` | 整数 | 否 | -（位置：query） | `1` |
| `pageNum` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. markAllMessagesAsRead

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | markAllMessagesAsRead |
| 请求地址 | `/api/v1/message/records/all-read` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. batchDeleteMessages

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | batchDeleteMessages |
| 请求地址 | `/api/v1/message/records/batch` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
[
  "示例值"
]
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. getUnreadMessageCountCompat

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getUnreadMessageCountCompat |
| 请求地址 | `/api/v1/message/records/unread-count` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. deleteMessage

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | deleteMessage |
| 请求地址 | `/api/v1/message/records/{messageId}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `messageId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. markMessageAsRead

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | markMessageAsRead |
| 请求地址 | `/api/v1/message/records/{messageId}/read` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `messageId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 7. sendMessage

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | sendMessage |
| 请求地址 | `/api/v1/message/send` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 否 | - | `"10001"` |
| `senderId` | 字符串 | 否 | - | `"10001"` |
| `receiverId` | 字符串 | 否 | - | `"10001"` |
| `content` | 字符串 | 否 | - | `"示例内容"` |
| `messageType` | 字符串 | 否 | - | `"示例内容"` |
| `sendTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `readStatus` | 整数 | 否 | - | `1` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "senderId": "10001",
  "receiverId": "10001",
  "content": "示例内容",
  "messageType": "示例内容"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 8. getUnreadMessageCount

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getUnreadMessageCount |
| 请求地址 | `/api/v1/message/unread-count` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## mock-o-auth-controller

### 1. authorizePage

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | authorizePage |
| 请求地址 | `/api/mock/oauth/{provider}/authorize` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `provider` | 字符串 | 是 | -（位置：path） | `"示例值"` |
| `state` | 字符串 | 是 | -（位置：query） | `"示例值"` |
| `redirect_uri` | 字符串 | 是 | -（位置：query） | `"示例值"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
"示例值"
```

### 2. authorizeSubmit

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | authorizeSubmit |
| 请求地址 | `/api/mock/oauth/{provider}/authorize` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `provider` | 字符串 | 是 | -（位置：path） | `"示例值"` |
| `state` | 字符串 | 是 | -（位置：query） | `"示例值"` |
| `redirect_uri` | 字符串 | 是 | -（位置：query） | `"示例值"` |
| `username` | 字符串 | 是 | -（位置：query） | `"示例名称"` |
| `password` | 字符串 | 否 | -（位置：query） | `"123456"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
"示例值"
```

### 3. token

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | token |
| 请求地址 | `/api/mock/oauth/{provider}/token` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `provider` | 字符串 | 是 | -（位置：path） | `"示例值"` |
| `code` | 字符串 | 是 | -（位置：query） | `"200"` |
| `appid` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `secret` | 字符串 | 是 | -（位置：query） | `"示例值"` |
| `grant_type` | 字符串 | 是 | -（位置：query） | `"示例值"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 4. userinfo

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | userinfo |
| 请求地址 | `/api/mock/oauth/{provider}/userinfo` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `provider` | 字符串 | 是 | -（位置：path） | `"示例值"` |
| `access_token` | 字符串 | 是 | -（位置：query） | `"token_xxx"` |
| `openid` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

## notification-controller

### 1. markAllAsRead

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | markAllAsRead |
| 请求地址 | `/api/notifications/all-read` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. batchDeleteNotifications

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | batchDeleteNotifications |
| 请求地址 | `/api/notifications/batch` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
[
  "示例值"
]
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. getUnreadCount

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getUnreadCount |
| 请求地址 | `/api/notifications/unread-count` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. getNotificationsByUserId

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getNotificationsByUserId |
| 请求地址 | `/api/notifications/user/{userId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. deleteNotification

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | deleteNotification |
| 请求地址 | `/api/notifications/{notificationId}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `notificationId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. getNotificationDetail

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getNotificationDetail |
| 请求地址 | `/api/notifications/{notificationId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `notificationId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 7. markAsRead

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | markAsRead |
| 请求地址 | `/api/notifications/{notificationId}/read` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `notificationId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## o-auth-controller

### 1. getBoundAccounts

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getBoundAccounts |
| 请求地址 | `/api/v1/oauth/accounts` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 数组<BoundAccountInfo> | - | `[{"provider": "示例值", "nickname": "示例名称", "avatarUrl": "https://example.com/file.png", "bindTime": "2026-04-15 21:00:00"}]` |
| `data[].provider` | 字符串 | - | `"示例值"` |
| `data[].nickname` | 字符串 | - | `"示例名称"` |
| `data[].avatarUrl` | 字符串 | - | `"https://example.com/file.png"` |
| `data[].bindTime` | 字符串 | - | `"2026-04-15 21:00:00"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": [
    {}
  ]
}
```

### 2. getAuthorizeUrl

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getAuthorizeUrl |
| 请求地址 | `/api/v1/oauth/authorize-url` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `provider` | 字符串 | 否 | - | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | AuthorizeUrlResponse | - | `{"authUrl": "https://example.com/file.png", "state": "示例值", "codeVerifier": "200"}` |
| `data.authUrl` | 字符串 | - | `"https://example.com/file.png"` |
| `data.state` | 字符串 | - | `"示例值"` |
| `data.codeVerifier` | 字符串 | - | `"200"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "provider": "示例值"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {
    "authUrl": "https://example.com/file.png",
    "state": "示例值",
    "codeVerifier": "200"
  }
}
```

### 3. bindOAuthAccount

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | bindOAuthAccount |
| 请求地址 | `/api/v1/oauth/bind` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `provider` | 字符串 | 否 | - | `"示例值"` |
| `code` | 字符串 | 否 | - | `"200"` |
| `state` | 字符串 | 否 | - | `"示例值"` |
| `codeVerifier` | 字符串 | 否 | - | `"200"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "provider": "示例值",
  "code": "200",
  "state": "示例值",
  "codeVerifier": "200"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. bindPhone

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | bindPhone |
| 请求地址 | `/api/v1/oauth/bind-phone` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `tempToken` | 字符串 | 否 | - | `"token_xxx"` |
| `phone` | 字符串 | 否 | - | `"13800138000"` |
| `smsCode` | 字符串 | 否 | - | `"200"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | LoginResult | - | `{"success": true, "needBindPhone": true, "tempToken": "token_xxx", "token": "token_xxx", "user": {}}` |
| `data.success` | 布尔 | - | `true` |
| `data.needBindPhone` | 布尔 | - | `true` |
| `data.tempToken` | 字符串 | - | `"token_xxx"` |
| `data.token` | 字符串 | - | `"token_xxx"` |
| `data.user` | 对象 | - | `{}` |
| `data.oauthNickname` | 字符串 | - | `"示例名称"` |
| `data.oauthAvatar` | 字符串 | - | `"https://example.com/file.png"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "tempToken": "token_xxx",
  "phone": "13800138000",
  "smsCode": "200"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {
    "success": true,
    "needBindPhone": true,
    "tempToken": "token_xxx",
    "token": "token_xxx",
    "user": {}
  }
}
```

### 5. handleCallback

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | handleCallback |
| 请求地址 | `/api/v1/oauth/callback` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `provider` | 字符串 | 否 | - | `"示例值"` |
| `code` | 字符串 | 否 | - | `"200"` |
| `state` | 字符串 | 否 | - | `"示例值"` |
| `codeVerifier` | 字符串 | 否 | - | `"200"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | LoginResult | - | `{"success": true, "needBindPhone": true, "tempToken": "token_xxx", "token": "token_xxx", "user": {}}` |
| `data.success` | 布尔 | - | `true` |
| `data.needBindPhone` | 布尔 | - | `true` |
| `data.tempToken` | 字符串 | - | `"token_xxx"` |
| `data.token` | 字符串 | - | `"token_xxx"` |
| `data.user` | 对象 | - | `{}` |
| `data.oauthNickname` | 字符串 | - | `"示例名称"` |
| `data.oauthAvatar` | 字符串 | - | `"https://example.com/file.png"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "provider": "示例值",
  "code": "200",
  "state": "示例值",
  "codeVerifier": "200"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {
    "success": true,
    "needBindPhone": true,
    "tempToken": "token_xxx",
    "token": "token_xxx",
    "user": {}
  }
}
```

### 6. unbindOAuthAccount

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | unbindOAuthAccount |
| 请求地址 | `/api/v1/oauth/unbind/{provider}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `provider` | 字符串 | 是 | -（位置：path） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## order-controller

### 1. createOrder

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | createOrder |
| 请求地址 | `/api/v1/orders` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `order` | Order | 否 | - | `{"id": "10001", "userId": "10001", "merchantId": "10001", "merchantName": "示例名称", "totalAmount": 99.9}` |
| `order.id` | 字符串 | 否 | - | `"10001"` |
| `order.userId` | 字符串 | 否 | - | `"10001"` |
| `order.merchantId` | 字符串 | 否 | - | `"10001"` |
| `order.merchantName` | 字符串 | 否 | - | `"示例名称"` |
| `order.totalAmount` | 数字 | 否 | - | `99.9` |
| `order.status` | 整数 | 否 | - | `1` |
| `order.statusText` | 字符串 | 否 | - | `"1"` |
| `order.paymentId` | 字符串 | 否 | - | `"10001"` |
| `order.paidAmount` | 数字 | 否 | - | `99.9` |
| `order.paymentTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `order.addressId` | 字符串 | 否 | - | `"10001"` |
| `order.address` | 字符串 | 否 | - | `"示例值"` |
| `order.remark` | 字符串 | 否 | - | `"示例内容"` |
| `order.createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `order.updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `dishes` | 数组<OrderDish> | 否 | - | `[{"id": "10001", "orderId": "10001", "dishId": "10001", "quantity": 1, "price": 99.9}]` |
| `dishes[].id` | 字符串 | 否 | - | `"10001"` |
| `dishes[].orderId` | 字符串 | 否 | - | `"10001"` |
| `dishes[].dishId` | 字符串 | 否 | - | `"10001"` |
| `dishes[].quantity` | 整数 | 否 | - | `1` |
| `dishes[].price` | 数字 | 否 | - | `99.9` |
| `dishes[].customization` | 字符串 | 否 | - | `"示例值"` |
| `dishes[].stepStatus` | 整数 | 否 | - | `1` |
| `dishes[].stepStartTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `dishes[].estimatedCompletionTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `dishes[].cookingMinutes` | 整数 | 否 | - | `1` |
| `dishes[].stepSort` | 整数 | 否 | - | `1` |
| `dishes[].isFastFood` | 布尔 | 否 | - | `true` |
| `dishes[].servingStatus` | 整数 | 否 | - | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "order": {
    "id": "10001",
    "userId": "10001",
    "merchantId": "10001",
    "merchantName": "示例名称",
    "totalAmount": 99.9
  },
  "dishes": [
    {}
  ]
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. getOrderCount

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getOrderCount |
| 请求地址 | `/api/v1/orders/count` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. getOrdersByMerchantId

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getOrdersByMerchantId |
| 请求地址 | `/api/v1/orders/merchant/{merchantId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `today` | 布尔 | 否 | -（位置：query） | `true` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. getOrdersByUserId

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getOrdersByUserId |
| 请求地址 | `/api/v1/orders/user/{userId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. getUserOrderStatistics

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getUserOrderStatistics |
| 请求地址 | `/api/v1/orders/user/{userId}/statistics` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. getOrderDetail

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getOrderDetail |
| 请求地址 | `/api/v1/orders/{orderId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `orderId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 7. cancelOrder

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | cancelOrder |
| 请求地址 | `/api/v1/orders/{orderId}/cancel` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `orderId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `reason` | 字符串 | 否 | -（位置：query） | `"示例内容"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 8. getOrderDishes

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getOrderDishes |
| 请求地址 | `/api/v1/orders/{orderId}/dishes` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `orderId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 9. payOrder

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | payOrder |
| 请求地址 | `/api/v1/orders/{orderId}/pay` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `orderId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `paymentMethod` | 字符串 | 否 | -（位置：query） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 10. getOrderPayment

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getOrderPayment |
| 请求地址 | `/api/v1/orders/{orderId}/payment` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `orderId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 11. reorder

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | reorder |
| 请求地址 | `/api/v1/orders/{orderId}/reorder` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `orderId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 12. rollbackStatus

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | rollbackStatus |
| 请求地址 | `/api/v1/orders/{orderId}/rollback` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `orderId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 13. getRollbackOptions

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getRollbackOptions |
| 请求地址 | `/api/v1/orders/{orderId}/rollback-options` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `orderId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 14. updateOrderStatus

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateOrderStatus |
| 请求地址 | `/api/v1/orders/{orderId}/status` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `orderId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `status` | 整数 | 是 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## payment-controller

### 1. alipay

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | alipay |
| 请求地址 | `/api/v1/payment/alipay` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象<字符串,对象> | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. balancePay

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | balancePay |
| 请求地址 | `/api/v1/payment/balance` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象<字符串,对象> | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. getAvailableCoupons

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getAvailableCoupons |
| 请求地址 | `/api/v1/payment/coupons` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `orderAmount` | 数字 | 否 | -（位置：query） | `99.9` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 数组<UserCoupon> | - | `[{"id": "10001", "userId": "10001", "name": "示例名称", "amount": 99.9, "minAmount": 99.9}]` |
| `data[].id` | 字符串 | - | `"10001"` |
| `data[].userId` | 字符串 | - | `"10001"` |
| `data[].name` | 字符串 | - | `"示例名称"` |
| `data[].amount` | 数字 | - | `99.9` |
| `data[].minAmount` | 数字 | - | `99.9` |
| `data[].status` | 字符串 | - | `"1"` |
| `data[].orderId` | 字符串 | - | `"10001"` |
| `data[].expireTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].useTime` | 字符串 | - | `"2026-04-15 21:00:00"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": [
    {}
  ]
}
```

### 4. createPayment

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | createPayment |
| 请求地址 | `/api/v1/payment/create` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象<字符串,对象> | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. getOrderPaymentInfo

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getOrderPaymentInfo |
| 请求地址 | `/api/v1/payment/order/{orderId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `orderId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象<字符串,对象> | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. getOrderByPaymentStatus

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getOrderByPaymentStatus |
| 请求地址 | `/api/v1/payment/order/{orderId}/status` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `orderId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象<字符串,对象> | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 7. getPaymentStatus

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getPaymentStatus |
| 请求地址 | `/api/v1/payment/status/{paymentNo}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `paymentNo` | 字符串 | 是 | -（位置：path） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象<字符串,对象> | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 8. wechatPay

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | wechatPay |
| 请求地址 | `/api/v1/payment/wechat` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象<字符串,对象> | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## payment-password-controller

### 1. changePaymentPassword

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | changePaymentPassword |
| 请求地址 | `/api/v1/payment-password/change` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `oldPassword` | 字符串 | 是 | -（位置：query） | `"123456"` |
| `newPassword` | 字符串 | 是 | -（位置：query） | `"123456"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. checkPaymentPassword

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | checkPaymentPassword |
| 请求地址 | `/api/v1/payment-password/check/{userId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. resetPaymentPassword

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | resetPaymentPassword |
| 请求地址 | `/api/v1/payment-password/reset` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `newPassword` | 字符串 | 是 | -（位置：query） | `"123456"` |
| `verificationCode` | 字符串 | 是 | -（位置：query） | `"200"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. setupPaymentPassword

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | setupPaymentPassword |
| 请求地址 | `/api/v1/payment-password/setup` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `password` | 字符串 | 是 | -（位置：query） | `"123456"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. verifyPaymentPassword

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | verifyPaymentPassword |
| 请求地址 | `/api/v1/payment-password/verify` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `password` | 字符串 | 是 | -（位置：query） | `"123456"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## recipe-controller

### 1. addRecipe

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | addRecipe |
| 请求地址 | `/api/v1/recipe` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 否 | - | `"10001"` |
| `userId` | 字符串 | 否 | - | `"10001"` |
| `name` | 字符串 | 否 | - | `"示例名称"` |
| `type` | 字符串 | 否 | - | `"示例值"` |
| `items` | 字符串 | 否 | - | `"示例值"` |
| `calories` | 整数 | 否 | - | `1` |
| `protein` | 整数 | 否 | - | `1` |
| `carbs` | 整数 | 否 | - | `1` |
| `fat` | 整数 | 否 | - | `1` |
| `customNutrition` | 字符串 | 否 | - | `"示例值"` |
| `detail` | 字符串 | 否 | - | `"示例值"` |
| `cookTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `favorite` | 布尔 | 否 | - | `true` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "userId": "10001",
  "name": "示例名称",
  "type": "示例值",
  "items": "示例值"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. getAllRecipes

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getAllRecipes |
| 请求地址 | `/api/v1/recipe/all` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. batchToggleFavorite

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | batchToggleFavorite |
| 请求地址 | `/api/v1/recipe/batch-toggle-favorite` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. getFavoriteRecipes

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getFavoriteRecipes |
| 请求地址 | `/api/v1/recipe/favorite` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 否 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. getRecommendedRecipes

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getRecommendedRecipes |
| 请求地址 | `/api/v1/recipe/recommend` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. getTodayRecipes

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getTodayRecipes |
| 请求地址 | `/api/v1/recipe/today` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 7. toggleFavorite

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | toggleFavorite |
| 请求地址 | `/api/v1/recipe/toggle-favorite/{id}` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 整数 | 是 | -（位置：path） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 8. deleteRecipe

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | deleteRecipe |
| 请求地址 | `/api/v1/recipe/{id}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 整数 | 是 | -（位置：path） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 9. getRecipeDetail

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getRecipeDetail |
| 请求地址 | `/api/v1/recipe/{id}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 整数 | 是 | -（位置：path） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 10. updateRecipe

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateRecipe |
| 请求地址 | `/api/v1/recipe/{id}` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 整数 | 是 | -（位置：path） | `1` |
| `id` | 字符串 | 否 | - | `"10001"` |
| `userId` | 字符串 | 否 | - | `"10001"` |
| `name` | 字符串 | 否 | - | `"示例名称"` |
| `type` | 字符串 | 否 | - | `"示例值"` |
| `items` | 字符串 | 否 | - | `"示例值"` |
| `calories` | 整数 | 否 | - | `1` |
| `protein` | 整数 | 否 | - | `1` |
| `carbs` | 整数 | 否 | - | `1` |
| `fat` | 整数 | 否 | - | `1` |
| `customNutrition` | 字符串 | 否 | - | `"示例值"` |
| `detail` | 字符串 | 否 | - | `"示例值"` |
| `cookTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `favorite` | 布尔 | 否 | - | `true` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "userId": "10001",
  "name": "示例名称",
  "type": "示例值",
  "items": "示例值"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## recommend-controller

### 1. recordBehavior

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | recordBehavior |
| 请求地址 | `/api/v1/recommendations/behavior` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 否 | - | `"10001"` |
| `behaviorType` | 字符串 | 否 | - | `"示例值"` |
| `itemType` | 字符串 | 否 | - | `"示例值"` |
| `itemId` | 字符串 | 否 | - | `"10001"` |
| `context` | 对象<字符串,对象> | 否 | - | `{}` |
| `duration` | 整数 | 否 | - | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "userId": "10001",
  "behaviorType": "示例值",
  "itemType": "示例值",
  "itemId": "10001",
  "context": {}
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. getUserBehaviors

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getUserBehaviors |
| 请求地址 | `/api/v1/recommendations/behavior/{userId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `limit` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. recordFeedback

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | recordFeedback |
| 请求地址 | `/api/v1/recommendations/feedback` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. getUserProfile

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getUserProfile |
| 请求地址 | `/api/v1/recommendations/profile/{userId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. generateShoppingList

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | generateShoppingList |
| 请求地址 | `/api/v1/recommendations/recipe/{userId}/shopping-list` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `date` | 字符串 | 否 | -（位置：query） | `"2026-04-15"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. setRecommendPreference

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | setRecommendPreference |
| 请求地址 | `/api/v1/recommendations/users/{userId}/prefer` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 7. getRecommendDishes

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getRecommendDishes |
| 请求地址 | `/api/v1/recommendations/{userId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `scene` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `limit` | 整数 | 否 | -（位置：query） | `1` |
| `timePeriod` | 字符串 | 否 | -（位置：query） | `"2026-04-15 21:00:00"` |
| `weather` | 字符串 | 否 | -（位置：query） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 8. filterRecommendDishes

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | filterRecommendDishes |
| 请求地址 | `/api/v1/recommendations/{userId}/filter` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 9. getRecommendationReason

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getRecommendationReason |
| 请求地址 | `/api/v1/recommendations/{userId}/reason/{dishId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `dishId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 10. refreshRecommendations

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | refreshRecommendations |
| 请求地址 | `/api/v1/recommendations/{userId}/refresh` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 11. recordRejectBehavior

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | recordRejectBehavior |
| 请求地址 | `/api/v1/recommendations/{userId}/reject` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 12. replaceRecommendDishes

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | replaceRecommendDishes |
| 请求地址 | `/api/v1/recommendations/{userId}/replace` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## reject-recommendation-controller

### 1. clearRejectRecord

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | clearRejectRecord |
| 请求地址 | `/api/v1/recommendations/rejects` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `dishId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. addRejectRecord

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | addRejectRecord |
| 请求地址 | `/api/v1/recommendations/rejects` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `dishId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `reason` | 字符串 | 否 | -（位置：query） | `"示例内容"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. countRejects

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | countRejects |
| 请求地址 | `/api/v1/recommendations/rejects/count` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `dishId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. getFrequentlyRejectedDishIds

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getFrequentlyRejectedDishIds |
| 请求地址 | `/api/v1/recommendations/rejects/frequent` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `threshold` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. getRejectedDishIds

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getRejectedDishIds |
| 请求地址 | `/api/v1/recommendations/rejects/list` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## remark-conflict-controller

### 1. checkConflict

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | checkConflict |
| 请求地址 | `/api/v1/remark-conflict/check` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `dishId` | 字符串 | 是 | - | `"10001"` |
| `remark` | 字符串 | 否 | - | `"示例内容"` |
| `tasteTags` | 数组<字符串> | 否 | - | `["示例值"]` |
| `userAllergies` | 字符串 | 否 | - | `"示例值"` |
| `preferenceTags` | 数组<字符串> | 否 | - | `["示例值"]` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | RemarkConflictCheckVO | - | `{"hasConflict": true, "conflictLevel": "示例值", "conflicts": [{}], "suggestions": ["示例值"]}` |
| `data.hasConflict` | 布尔 | - | `true` |
| `data.conflictLevel` | 字符串 | - | `"示例值"` |
| `data.conflicts` | 数组<ConflictItem> | - | `[{"conflictType": "示例值", "description": "示例值", "conflictItem": "示例值", "severity": 1, "priority": 1}]` |
| `data.suggestions` | 数组<字符串> | - | `["示例值"]` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "dishId": "10001",
  "remark": "示例内容",
  "tasteTags": [
    "示例值"
  ],
  "userAllergies": "示例值",
  "preferenceTags": [
    "示例值"
  ]
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {
    "hasConflict": true,
    "conflictLevel": "示例值",
    "conflicts": [],
    "suggestions": []
  }
}
```

### 2. formatRemark

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | formatRemark |
| 请求地址 | `/api/v1/remark-conflict/format` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `originalRemark` | 字符串 | 是 | -（位置：query） | `"示例内容"` |
| `tasteTags` | 数组<字符串> | 是 | -（位置：query） | `["示例值"]` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 字符串 | - | `"示例值"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": "示例值"
}
```

### 3. parseTasteTags

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | parseTasteTags |
| 请求地址 | `/api/v1/remark-conflict/parse-tags` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `remark` | 字符串 | 是 | -（位置：query） | `"示例内容"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 数组<字符串> | - | `["示例值"]` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": [
    "示例值"
  ]
}
```

### 4. getRecommendedTasteTags

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getRecommendedTasteTags |
| 请求地址 | `/api/v1/remark-conflict/recommended-tags/{dishId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `dishId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 数组<字符串> | - | `["示例值"]` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": [
    "示例值"
  ]
}
```

## review-controller

### 1. submitReview

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | submitReview |
| 请求地址 | `/api/v1/reviews` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. getDishReviews

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getDishReviews |
| 请求地址 | `/api/v1/reviews/dish/{dishId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `dishId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `size` | 整数 | 否 | -（位置：query） | `1` |
| `sort` | 字符串 | 否 | -（位置：query） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. getMerchantReviews

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getMerchantReviews |
| 请求地址 | `/api/v1/reviews/merchant/{merchantId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `status` | 字符串 | 否 | -（位置：query） | `"1"` |
| `rating` | 整数 | 否 | -（位置：query） | `1` |
| `keyword` | 字符串 | 否 | -（位置：query） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. getReviewStatistics

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getReviewStatistics |
| 请求地址 | `/api/v1/reviews/merchant/{merchantId}/statistics` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. getReviewByOrderId

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getReviewByOrderId |
| 请求地址 | `/api/v1/reviews/order/{orderId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `orderId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. addAdditionalReview

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | addAdditionalReview |
| 请求地址 | `/api/v1/reviews/{reviewId}/additional` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `reviewId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 7. replyReview

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | replyReview |
| 请求地址 | `/api/v1/reviews/{reviewId}/reply` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `reviewId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## scheduled-task-controller

### 1. getTasks

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getTasks |
| 请求地址 | `/api/v1/scheduled-tasks` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `pageSize` | 整数 | 否 | -（位置：query） | `1` |
| `status` | 字符串 | 否 | -（位置：query） | `"1"` |
| `taskGroup` | 字符串 | 否 | -（位置：query） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. createTask

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | createTask |
| 请求地址 | `/api/v1/scheduled-tasks` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 否 | - | `"10001"` |
| `taskName` | 字符串 | 否 | - | `"示例名称"` |
| `taskCode` | 字符串 | 否 | - | `"200"` |
| `description` | 字符串 | 否 | - | `"示例值"` |
| `cronExpression` | 字符串 | 否 | - | `"示例值"` |
| `taskType` | 字符串 | 否 | - | `"示例值"` |
| `rateInMillis` | 整数 | 否 | - | `1` |
| `status` | 字符串 | 否 | - | `"1"` |
| `taskClassName` | 字符串 | 否 | - | `"示例名称"` |
| `taskMethodName` | 字符串 | 否 | - | `"示例名称"` |
| `taskParams` | 字符串 | 否 | - | `"示例值"` |
| `lastExecuteTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `nextExecuteTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `executeCount` | 整数 | 否 | - | `1` |
| `failCount` | 整数 | 否 | - | `1` |
| `lastExecuteResult` | 字符串 | 否 | - | `"示例值"` |
| `lastErrorMessage` | 字符串 | 否 | - | `"示例内容"` |
| `logExecution` | 布尔 | 否 | - | `true` |
| `timeoutSeconds` | 整数 | 否 | - | `1` |
| `retryCount` | 整数 | 否 | - | `1` |
| `retriedCount` | 整数 | 否 | - | `1` |
| `taskGroup` | 字符串 | 否 | - | `"示例值"` |
| `priority` | 整数 | 否 | - | `1` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `createdBy` | 字符串 | 否 | - | `"示例值"` |
| `updatedBy` | 字符串 | 否 | - | `"2026-04-15"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "taskName": "示例名称",
  "taskCode": "200",
  "description": "示例值",
  "cronExpression": "示例值"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. cleanOldLogs

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | cleanOldLogs |
| 请求地址 | `/api/v1/scheduled-tasks/logs/clean` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `days` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. getRunningTasks

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getRunningTasks |
| 请求地址 | `/api/v1/scheduled-tasks/running` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. deleteTask

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | deleteTask |
| 请求地址 | `/api/v1/scheduled-tasks/{taskId}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `taskId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. getTaskDetail

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getTaskDetail |
| 请求地址 | `/api/v1/scheduled-tasks/{taskId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `taskId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 7. updateTask

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateTask |
| 请求地址 | `/api/v1/scheduled-tasks/{taskId}` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `taskId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `id` | 字符串 | 否 | - | `"10001"` |
| `taskName` | 字符串 | 否 | - | `"示例名称"` |
| `taskCode` | 字符串 | 否 | - | `"200"` |
| `description` | 字符串 | 否 | - | `"示例值"` |
| `cronExpression` | 字符串 | 否 | - | `"示例值"` |
| `taskType` | 字符串 | 否 | - | `"示例值"` |
| `rateInMillis` | 整数 | 否 | - | `1` |
| `status` | 字符串 | 否 | - | `"1"` |
| `taskClassName` | 字符串 | 否 | - | `"示例名称"` |
| `taskMethodName` | 字符串 | 否 | - | `"示例名称"` |
| `taskParams` | 字符串 | 否 | - | `"示例值"` |
| `lastExecuteTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `nextExecuteTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `executeCount` | 整数 | 否 | - | `1` |
| `failCount` | 整数 | 否 | - | `1` |
| `lastExecuteResult` | 字符串 | 否 | - | `"示例值"` |
| `lastErrorMessage` | 字符串 | 否 | - | `"示例内容"` |
| `logExecution` | 布尔 | 否 | - | `true` |
| `timeoutSeconds` | 整数 | 否 | - | `1` |
| `retryCount` | 整数 | 否 | - | `1` |
| `retriedCount` | 整数 | 否 | - | `1` |
| `taskGroup` | 字符串 | 否 | - | `"示例值"` |
| `priority` | 整数 | 否 | - | `1` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `createdBy` | 字符串 | 否 | - | `"示例值"` |
| `updatedBy` | 字符串 | 否 | - | `"2026-04-15"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "taskName": "示例名称",
  "taskCode": "200",
  "description": "示例值",
  "cronExpression": "示例值"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 8. executeNow

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | executeNow |
| 请求地址 | `/api/v1/scheduled-tasks/{taskId}/execute` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `taskId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 9. getTaskLogs

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getTaskLogs |
| 请求地址 | `/api/v1/scheduled-tasks/{taskId}/logs` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `taskId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `limit` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 10. pauseTask

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | pauseTask |
| 请求地址 | `/api/v1/scheduled-tasks/{taskId}/pause` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `taskId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 11. refreshTask

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | refreshTask |
| 请求地址 | `/api/v1/scheduled-tasks/{taskId}/refresh` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `taskId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 12. resumeTask

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | resumeTask |
| 请求地址 | `/api/v1/scheduled-tasks/{taskId}/resume` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `taskId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## supervisor-agent-controller

### 1. quickChat

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | quickChat |
| 请求地址 | `/api/agent/supervisor/chat` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `message` | 字符串 | 是 | -（位置：query） | `"示例内容"` |
| `userId` | 字符串 | 否 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 字符串 | - | `"示例值"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": "示例值"
}
```

### 2. chat

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | chat |
| 请求地址 | `/api/agent/supervisor/chat` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `message` | 字符串 | 否 | - | `"示例内容"` |
| `userId` | 字符串 | 否 | - | `"10001"` |
| `sessionId` | 字符串 | 否 | - | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 字符串 | - | `"示例值"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "message": "示例内容",
  "userId": "10001",
  "sessionId": "10001"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": "示例值"
}
```

### 3. chatWithContext

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | chatWithContext |
| 请求地址 | `/api/agent/supervisor/chatWithContext` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `message` | 字符串 | 否 | - | `"示例内容"` |
| `userId` | 字符串 | 否 | - | `"10001"` |
| `sessionId` | 字符串 | 否 | - | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 字符串 | - | `"示例值"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "message": "示例内容",
  "userId": "10001",
  "sessionId": "10001"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": "示例值"
}
```

## system-log-controller

### 1. getLogList

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getLogList |
| 请求地址 | `/api/admin/system/logs` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `pageSize` | 整数 | 否 | -（位置：query） | `1` |
| `operatorName` | 字符串 | 否 | -（位置：query） | `"示例名称"` |
| `module` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `operationType` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `status` | 字符串 | 否 | -（位置：query） | `"1"` |
| `startTime` | 字符串 | 否 | -（位置：query） | `"2026-04-15 21:00:00"` |
| `endTime` | 字符串 | 否 | -（位置：query） | `"2026-04-15 21:00:00"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 2. cleanExpiredLogs

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | cleanExpiredLogs |
| 请求地址 | `/api/admin/system/logs/clean` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `days` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 3. exportLogs

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | exportLogs |
| 请求地址 | `/api/admin/system/logs/export` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `module` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `operationType` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `startTime` | 字符串 | 否 | -（位置：query） | `"2026-04-15 21:00:00"` |
| `endTime` | 字符串 | 否 | -（位置：query） | `"2026-04-15 21:00:00"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 4. getLogStatistics

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getLogStatistics |
| 请求地址 | `/api/admin/system/logs/statistics` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

## tutorial-admin-controller

### 1. createByAdmin

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | createByAdmin |
| 请求地址 | `/api/v1/tutorial/admin/create` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 否 | - | `"10001"` |
| `title` | 字符串 | 否 | - | `"示例值"` |
| `type` | 字符串 | 否 | - | `"示例值"` |
| `duration` | 字符串 | 否 | - | `"示例值"` |
| `views` | 字符串 | 否 | - | `"示例值"` |
| `sourceType` | 字符串 | 否 | - | `"示例值"` |
| `sourceId` | 字符串 | 否 | - | `"10001"` |
| `authorType` | 字符串 | 否 | - | `"示例值"` |
| `authorId` | 字符串 | 否 | - | `"10001"` |
| `author` | 字符串 | 否 | - | `"示例值"` |
| `status` | 字符串 | 否 | - | `"1"` |
| `reviewStatus` | 字符串 | 否 | - | `"1"` |
| `reviewerId` | 字符串 | 否 | - | `"10001"` |
| `reviewTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `reviewComment` | 字符串 | 否 | - | `"示例值"` |
| `featured` | 布尔 | 否 | - | `true` |
| `linkedMerchantId` | 字符串 | 否 | - | `"10001"` |
| `linkedDishId` | 字符串 | 否 | - | `"10001"` |
| `aiModelVersion` | 字符串 | 否 | - | `"示例值"` |
| `content` | 字符串 | 否 | - | `"示例内容"` |
| `coverImage` | 字符串 | 否 | - | `"https://example.com/file.png"` |
| `videoUrl` | 字符串 | 否 | - | `"https://example.com/file.png"` |
| `tags` | 字符串 | 否 | - | `"示例值"` |
| `difficulty` | 字符串 | 否 | - | `"示例值"` |
| `calories` | 整数 | 否 | - | `1` |
| `prepTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `servings` | 整数 | 否 | - | `1` |
| `rating` | 数字 | 否 | - | `99.9` |
| `ratingCount` | 整数 | 否 | - | `1` |
| `favoriteCount` | 整数 | 否 | - | `1` |
| `viewCount` | 整数 | 否 | - | `1` |
| `shareCount` | 整数 | 否 | - | `1` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `official` | 布尔 | 否 | - | `true` |
| `editable` | 布尔 | 否 | - | `true` |
| `published` | 布尔 | 否 | - | `true` |
| `adminSource` | 布尔 | 否 | - | `true` |
| `merchantSource` | 布尔 | 否 | - | `true` |
| `aigenerated` | 布尔 | 否 | - | `true` |
| `userSource` | 布尔 | 否 | - | `true` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `id` | 字符串 | - | `"10001"` |
| `title` | 字符串 | - | `"示例值"` |
| `type` | 字符串 | - | `"示例值"` |
| `duration` | 字符串 | - | `"示例值"` |
| `views` | 字符串 | - | `"示例值"` |
| `sourceType` | 字符串 | - | `"示例值"` |
| `sourceId` | 字符串 | - | `"10001"` |
| `authorType` | 字符串 | - | `"示例值"` |
| `authorId` | 字符串 | - | `"10001"` |
| `author` | 字符串 | - | `"示例值"` |
| `status` | 字符串 | - | `"1"` |
| `reviewStatus` | 字符串 | - | `"1"` |
| `reviewerId` | 字符串 | - | `"10001"` |
| `reviewTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `reviewComment` | 字符串 | - | `"示例值"` |
| `featured` | 布尔 | - | `true` |
| `linkedMerchantId` | 字符串 | - | `"10001"` |
| `linkedDishId` | 字符串 | - | `"10001"` |
| `aiModelVersion` | 字符串 | - | `"示例值"` |
| `content` | 字符串 | - | `"示例内容"` |
| `coverImage` | 字符串 | - | `"https://example.com/file.png"` |
| `videoUrl` | 字符串 | - | `"https://example.com/file.png"` |
| `tags` | 字符串 | - | `"示例值"` |
| `difficulty` | 字符串 | - | `"示例值"` |
| `calories` | 整数 | - | `1` |
| `prepTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `servings` | 整数 | - | `1` |
| `rating` | 数字 | - | `99.9` |
| `ratingCount` | 整数 | - | `1` |
| `favoriteCount` | 整数 | - | `1` |
| `viewCount` | 整数 | - | `1` |
| `shareCount` | 整数 | - | `1` |
| `createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `official` | 布尔 | - | `true` |
| `editable` | 布尔 | - | `true` |
| `published` | 布尔 | - | `true` |
| `adminSource` | 布尔 | - | `true` |
| `merchantSource` | 布尔 | - | `true` |
| `aigenerated` | 布尔 | - | `true` |
| `userSource` | 布尔 | - | `true` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "title": "示例值",
  "type": "示例值",
  "duration": "示例值",
  "views": "示例值"
}
```

#### 响应示例

```json
{
  "id": "10001",
  "title": "示例值",
  "type": "示例值",
  "duration": "示例值",
  "views": "示例值"
}
```

### 2. getAllTutorials_1

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getAllTutorials_1 |
| 请求地址 | `/api/v1/tutorial/admin/list` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
[
  {
    "id": "10001",
    "title": "示例值",
    "type": "示例值",
    "duration": "示例值",
    "views": "示例值"
  }
]
```

### 3. getPendingTutorials

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getPendingTutorials |
| 请求地址 | `/api/v1/tutorial/admin/pending` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `size` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `records` | 数组<Tutorial> | - | `[{"id": "10001", "title": "示例值", "type": "示例值", "duration": "示例值", "views": "示例值"}]` |
| `records[].id` | 字符串 | - | `"10001"` |
| `records[].title` | 字符串 | - | `"示例值"` |
| `records[].type` | 字符串 | - | `"示例值"` |
| `records[].duration` | 字符串 | - | `"示例值"` |
| `records[].views` | 字符串 | - | `"示例值"` |
| `records[].sourceType` | 字符串 | - | `"示例值"` |
| `records[].sourceId` | 字符串 | - | `"10001"` |
| `records[].authorType` | 字符串 | - | `"示例值"` |
| `records[].authorId` | 字符串 | - | `"10001"` |
| `records[].author` | 字符串 | - | `"示例值"` |
| `records[].status` | 字符串 | - | `"1"` |
| `records[].reviewStatus` | 字符串 | - | `"1"` |
| `records[].reviewerId` | 字符串 | - | `"10001"` |
| `records[].reviewTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].reviewComment` | 字符串 | - | `"示例值"` |
| `records[].featured` | 布尔 | - | `true` |
| `records[].linkedMerchantId` | 字符串 | - | `"10001"` |
| `records[].linkedDishId` | 字符串 | - | `"10001"` |
| `records[].aiModelVersion` | 字符串 | - | `"示例值"` |
| `records[].content` | 字符串 | - | `"示例内容"` |
| `records[].coverImage` | 字符串 | - | `"https://example.com/file.png"` |
| `records[].videoUrl` | 字符串 | - | `"https://example.com/file.png"` |
| `records[].tags` | 字符串 | - | `"示例值"` |
| `records[].difficulty` | 字符串 | - | `"示例值"` |
| `records[].calories` | 整数 | - | `1` |
| `records[].prepTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].servings` | 整数 | - | `1` |
| `records[].rating` | 数字 | - | `99.9` |
| `records[].ratingCount` | 整数 | - | `1` |
| `records[].favoriteCount` | 整数 | - | `1` |
| `records[].viewCount` | 整数 | - | `1` |
| `records[].shareCount` | 整数 | - | `1` |
| `records[].createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].official` | 布尔 | - | `true` |
| `records[].editable` | 布尔 | - | `true` |
| `records[].published` | 布尔 | - | `true` |
| `records[].adminSource` | 布尔 | - | `true` |
| `records[].merchantSource` | 布尔 | - | `true` |
| `records[].aigenerated` | 布尔 | - | `true` |
| `records[].userSource` | 布尔 | - | `true` |
| `total` | 整数 | - | `1` |
| `size` | 整数 | - | `1` |
| `current` | 整数 | - | `1` |
| `orders` | 数组<OrderItem> | - | `[{"column": "示例值", "asc": true}]` |
| `orders[].column` | 字符串 | - | `"示例值"` |
| `orders[].asc` | 布尔 | - | `true` |
| `optimizeCountSql` | PageTutorial | - | `{"records": [{}], "total": 1, "size": 1, "current": 1, "orders": [{}]}` |
| `searchCount` | PageTutorial | - | `{"records": [{}], "total": 1, "size": 1, "current": 1, "orders": [{}]}` |
| `optimizeJoinOfCountSql` | 布尔 | - | `true` |
| `maxLimit` | 整数 | - | `1` |
| `countId` | 字符串 | - | `"10001"` |
| `pages` | 整数 | - | `1` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "records": [
    {}
  ],
  "total": 1,
  "size": 1,
  "current": 1,
  "orders": [
    {}
  ]
}
```

### 4. deleteTutorial_1

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | deleteTutorial_1 |
| 请求地址 | `/api/v1/tutorial/admin/{id}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 5. approveTutorial

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | approveTutorial |
| 请求地址 | `/api/v1/tutorial/admin/{id}/approve` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 6. toggleFeatured

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | toggleFeatured |
| 请求地址 | `/api/v1/tutorial/admin/{id}/featured` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 7. rejectTutorial

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | rejectTutorial |
| 请求地址 | `/api/v1/tutorial/admin/{id}/reject` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

## tutorial-controller

### 1. getFeaturedTutorials

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getFeaturedTutorials |
| 请求地址 | `/api/v1/tutorial/featured` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
[
  {
    "id": "10001",
    "title": "示例值",
    "type": "示例值",
    "duration": "示例值",
    "views": "示例值"
  }
]
```

### 2. getAllTutorials

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getAllTutorials |
| 请求地址 | `/api/v1/tutorial/list` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
[
  {
    "id": "10001",
    "title": "示例值",
    "type": "示例值",
    "duration": "示例值",
    "views": "示例值"
  }
]
```

### 3. getTutorialsByPage

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getTutorialsByPage |
| 请求地址 | `/api/v1/tutorial/page` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `size` | 整数 | 否 | -（位置：query） | `1` |
| `sourceType` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `status` | 字符串 | 否 | -（位置：query） | `"1"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `records` | 数组<Tutorial> | - | `[{"id": "10001", "title": "示例值", "type": "示例值", "duration": "示例值", "views": "示例值"}]` |
| `records[].id` | 字符串 | - | `"10001"` |
| `records[].title` | 字符串 | - | `"示例值"` |
| `records[].type` | 字符串 | - | `"示例值"` |
| `records[].duration` | 字符串 | - | `"示例值"` |
| `records[].views` | 字符串 | - | `"示例值"` |
| `records[].sourceType` | 字符串 | - | `"示例值"` |
| `records[].sourceId` | 字符串 | - | `"10001"` |
| `records[].authorType` | 字符串 | - | `"示例值"` |
| `records[].authorId` | 字符串 | - | `"10001"` |
| `records[].author` | 字符串 | - | `"示例值"` |
| `records[].status` | 字符串 | - | `"1"` |
| `records[].reviewStatus` | 字符串 | - | `"1"` |
| `records[].reviewerId` | 字符串 | - | `"10001"` |
| `records[].reviewTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].reviewComment` | 字符串 | - | `"示例值"` |
| `records[].featured` | 布尔 | - | `true` |
| `records[].linkedMerchantId` | 字符串 | - | `"10001"` |
| `records[].linkedDishId` | 字符串 | - | `"10001"` |
| `records[].aiModelVersion` | 字符串 | - | `"示例值"` |
| `records[].content` | 字符串 | - | `"示例内容"` |
| `records[].coverImage` | 字符串 | - | `"https://example.com/file.png"` |
| `records[].videoUrl` | 字符串 | - | `"https://example.com/file.png"` |
| `records[].tags` | 字符串 | - | `"示例值"` |
| `records[].difficulty` | 字符串 | - | `"示例值"` |
| `records[].calories` | 整数 | - | `1` |
| `records[].prepTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].servings` | 整数 | - | `1` |
| `records[].rating` | 数字 | - | `99.9` |
| `records[].ratingCount` | 整数 | - | `1` |
| `records[].favoriteCount` | 整数 | - | `1` |
| `records[].viewCount` | 整数 | - | `1` |
| `records[].shareCount` | 整数 | - | `1` |
| `records[].createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].official` | 布尔 | - | `true` |
| `records[].editable` | 布尔 | - | `true` |
| `records[].published` | 布尔 | - | `true` |
| `records[].adminSource` | 布尔 | - | `true` |
| `records[].merchantSource` | 布尔 | - | `true` |
| `records[].aigenerated` | 布尔 | - | `true` |
| `records[].userSource` | 布尔 | - | `true` |
| `total` | 整数 | - | `1` |
| `size` | 整数 | - | `1` |
| `current` | 整数 | - | `1` |
| `orders` | 数组<OrderItem> | - | `[{"column": "示例值", "asc": true}]` |
| `orders[].column` | 字符串 | - | `"示例值"` |
| `orders[].asc` | 布尔 | - | `true` |
| `optimizeCountSql` | PageTutorial | - | `{"records": [{}], "total": 1, "size": 1, "current": 1, "orders": [{}]}` |
| `searchCount` | PageTutorial | - | `{"records": [{}], "total": 1, "size": 1, "current": 1, "orders": [{}]}` |
| `optimizeJoinOfCountSql` | 布尔 | - | `true` |
| `maxLimit` | 整数 | - | `1` |
| `countId` | 字符串 | - | `"10001"` |
| `pages` | 整数 | - | `1` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "records": [
    {}
  ],
  "total": 1,
  "size": 1,
  "current": 1,
  "orders": [
    {}
  ]
}
```

### 4. getTutorialDetail

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getTutorialDetail |
| 请求地址 | `/api/v1/tutorial/{id}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `id` | 字符串 | - | `"10001"` |
| `title` | 字符串 | - | `"示例值"` |
| `type` | 字符串 | - | `"示例值"` |
| `duration` | 字符串 | - | `"示例值"` |
| `views` | 字符串 | - | `"示例值"` |
| `sourceType` | 字符串 | - | `"示例值"` |
| `sourceId` | 字符串 | - | `"10001"` |
| `authorType` | 字符串 | - | `"示例值"` |
| `authorId` | 字符串 | - | `"10001"` |
| `author` | 字符串 | - | `"示例值"` |
| `status` | 字符串 | - | `"1"` |
| `reviewStatus` | 字符串 | - | `"1"` |
| `reviewerId` | 字符串 | - | `"10001"` |
| `reviewTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `reviewComment` | 字符串 | - | `"示例值"` |
| `featured` | 布尔 | - | `true` |
| `linkedMerchantId` | 字符串 | - | `"10001"` |
| `linkedDishId` | 字符串 | - | `"10001"` |
| `aiModelVersion` | 字符串 | - | `"示例值"` |
| `content` | 字符串 | - | `"示例内容"` |
| `coverImage` | 字符串 | - | `"https://example.com/file.png"` |
| `videoUrl` | 字符串 | - | `"https://example.com/file.png"` |
| `tags` | 字符串 | - | `"示例值"` |
| `difficulty` | 字符串 | - | `"示例值"` |
| `calories` | 整数 | - | `1` |
| `prepTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `servings` | 整数 | - | `1` |
| `rating` | 数字 | - | `99.9` |
| `ratingCount` | 整数 | - | `1` |
| `favoriteCount` | 整数 | - | `1` |
| `viewCount` | 整数 | - | `1` |
| `shareCount` | 整数 | - | `1` |
| `createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `official` | 布尔 | - | `true` |
| `editable` | 布尔 | - | `true` |
| `published` | 布尔 | - | `true` |
| `adminSource` | 布尔 | - | `true` |
| `merchantSource` | 布尔 | - | `true` |
| `aigenerated` | 布尔 | - | `true` |
| `userSource` | 布尔 | - | `true` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "id": "10001",
  "title": "示例值",
  "type": "示例值",
  "duration": "示例值",
  "views": "示例值"
}
```

## tutorial-merchant-controller

### 1. createByMerchant

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | createByMerchant |
| 请求地址 | `/api/v1/tutorial/merchant/create` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 否 | - | `"10001"` |
| `title` | 字符串 | 否 | - | `"示例值"` |
| `type` | 字符串 | 否 | - | `"示例值"` |
| `duration` | 字符串 | 否 | - | `"示例值"` |
| `views` | 字符串 | 否 | - | `"示例值"` |
| `sourceType` | 字符串 | 否 | - | `"示例值"` |
| `sourceId` | 字符串 | 否 | - | `"10001"` |
| `authorType` | 字符串 | 否 | - | `"示例值"` |
| `authorId` | 字符串 | 否 | - | `"10001"` |
| `author` | 字符串 | 否 | - | `"示例值"` |
| `status` | 字符串 | 否 | - | `"1"` |
| `reviewStatus` | 字符串 | 否 | - | `"1"` |
| `reviewerId` | 字符串 | 否 | - | `"10001"` |
| `reviewTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `reviewComment` | 字符串 | 否 | - | `"示例值"` |
| `featured` | 布尔 | 否 | - | `true` |
| `linkedMerchantId` | 字符串 | 否 | - | `"10001"` |
| `linkedDishId` | 字符串 | 否 | - | `"10001"` |
| `aiModelVersion` | 字符串 | 否 | - | `"示例值"` |
| `content` | 字符串 | 否 | - | `"示例内容"` |
| `coverImage` | 字符串 | 否 | - | `"https://example.com/file.png"` |
| `videoUrl` | 字符串 | 否 | - | `"https://example.com/file.png"` |
| `tags` | 字符串 | 否 | - | `"示例值"` |
| `difficulty` | 字符串 | 否 | - | `"示例值"` |
| `calories` | 整数 | 否 | - | `1` |
| `prepTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `servings` | 整数 | 否 | - | `1` |
| `rating` | 数字 | 否 | - | `99.9` |
| `ratingCount` | 整数 | 否 | - | `1` |
| `favoriteCount` | 整数 | 否 | - | `1` |
| `viewCount` | 整数 | 否 | - | `1` |
| `shareCount` | 整数 | 否 | - | `1` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `official` | 布尔 | 否 | - | `true` |
| `editable` | 布尔 | 否 | - | `true` |
| `published` | 布尔 | 否 | - | `true` |
| `adminSource` | 布尔 | 否 | - | `true` |
| `merchantSource` | 布尔 | 否 | - | `true` |
| `aigenerated` | 布尔 | 否 | - | `true` |
| `userSource` | 布尔 | 否 | - | `true` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `id` | 字符串 | - | `"10001"` |
| `title` | 字符串 | - | `"示例值"` |
| `type` | 字符串 | - | `"示例值"` |
| `duration` | 字符串 | - | `"示例值"` |
| `views` | 字符串 | - | `"示例值"` |
| `sourceType` | 字符串 | - | `"示例值"` |
| `sourceId` | 字符串 | - | `"10001"` |
| `authorType` | 字符串 | - | `"示例值"` |
| `authorId` | 字符串 | - | `"10001"` |
| `author` | 字符串 | - | `"示例值"` |
| `status` | 字符串 | - | `"1"` |
| `reviewStatus` | 字符串 | - | `"1"` |
| `reviewerId` | 字符串 | - | `"10001"` |
| `reviewTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `reviewComment` | 字符串 | - | `"示例值"` |
| `featured` | 布尔 | - | `true` |
| `linkedMerchantId` | 字符串 | - | `"10001"` |
| `linkedDishId` | 字符串 | - | `"10001"` |
| `aiModelVersion` | 字符串 | - | `"示例值"` |
| `content` | 字符串 | - | `"示例内容"` |
| `coverImage` | 字符串 | - | `"https://example.com/file.png"` |
| `videoUrl` | 字符串 | - | `"https://example.com/file.png"` |
| `tags` | 字符串 | - | `"示例值"` |
| `difficulty` | 字符串 | - | `"示例值"` |
| `calories` | 整数 | - | `1` |
| `prepTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `servings` | 整数 | - | `1` |
| `rating` | 数字 | - | `99.9` |
| `ratingCount` | 整数 | - | `1` |
| `favoriteCount` | 整数 | - | `1` |
| `viewCount` | 整数 | - | `1` |
| `shareCount` | 整数 | - | `1` |
| `createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `official` | 布尔 | - | `true` |
| `editable` | 布尔 | - | `true` |
| `published` | 布尔 | - | `true` |
| `adminSource` | 布尔 | - | `true` |
| `merchantSource` | 布尔 | - | `true` |
| `aigenerated` | 布尔 | - | `true` |
| `userSource` | 布尔 | - | `true` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "title": "示例值",
  "type": "示例值",
  "duration": "示例值",
  "views": "示例值"
}
```

#### 响应示例

```json
{
  "id": "10001",
  "title": "示例值",
  "type": "示例值",
  "duration": "示例值",
  "views": "示例值"
}
```

### 2. getMerchantTutorials

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getMerchantTutorials |
| 请求地址 | `/api/v1/tutorial/merchant/my` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `size` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `records` | 数组<Tutorial> | - | `[{"id": "10001", "title": "示例值", "type": "示例值", "duration": "示例值", "views": "示例值"}]` |
| `records[].id` | 字符串 | - | `"10001"` |
| `records[].title` | 字符串 | - | `"示例值"` |
| `records[].type` | 字符串 | - | `"示例值"` |
| `records[].duration` | 字符串 | - | `"示例值"` |
| `records[].views` | 字符串 | - | `"示例值"` |
| `records[].sourceType` | 字符串 | - | `"示例值"` |
| `records[].sourceId` | 字符串 | - | `"10001"` |
| `records[].authorType` | 字符串 | - | `"示例值"` |
| `records[].authorId` | 字符串 | - | `"10001"` |
| `records[].author` | 字符串 | - | `"示例值"` |
| `records[].status` | 字符串 | - | `"1"` |
| `records[].reviewStatus` | 字符串 | - | `"1"` |
| `records[].reviewerId` | 字符串 | - | `"10001"` |
| `records[].reviewTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].reviewComment` | 字符串 | - | `"示例值"` |
| `records[].featured` | 布尔 | - | `true` |
| `records[].linkedMerchantId` | 字符串 | - | `"10001"` |
| `records[].linkedDishId` | 字符串 | - | `"10001"` |
| `records[].aiModelVersion` | 字符串 | - | `"示例值"` |
| `records[].content` | 字符串 | - | `"示例内容"` |
| `records[].coverImage` | 字符串 | - | `"https://example.com/file.png"` |
| `records[].videoUrl` | 字符串 | - | `"https://example.com/file.png"` |
| `records[].tags` | 字符串 | - | `"示例值"` |
| `records[].difficulty` | 字符串 | - | `"示例值"` |
| `records[].calories` | 整数 | - | `1` |
| `records[].prepTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].servings` | 整数 | - | `1` |
| `records[].rating` | 数字 | - | `99.9` |
| `records[].ratingCount` | 整数 | - | `1` |
| `records[].favoriteCount` | 整数 | - | `1` |
| `records[].viewCount` | 整数 | - | `1` |
| `records[].shareCount` | 整数 | - | `1` |
| `records[].createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `records[].official` | 布尔 | - | `true` |
| `records[].editable` | 布尔 | - | `true` |
| `records[].published` | 布尔 | - | `true` |
| `records[].adminSource` | 布尔 | - | `true` |
| `records[].merchantSource` | 布尔 | - | `true` |
| `records[].aigenerated` | 布尔 | - | `true` |
| `records[].userSource` | 布尔 | - | `true` |
| `total` | 整数 | - | `1` |
| `size` | 整数 | - | `1` |
| `current` | 整数 | - | `1` |
| `orders` | 数组<OrderItem> | - | `[{"column": "示例值", "asc": true}]` |
| `orders[].column` | 字符串 | - | `"示例值"` |
| `orders[].asc` | 布尔 | - | `true` |
| `optimizeCountSql` | PageTutorial | - | `{"records": [{}], "total": 1, "size": 1, "current": 1, "orders": [{}]}` |
| `searchCount` | PageTutorial | - | `{"records": [{}], "total": 1, "size": 1, "current": 1, "orders": [{}]}` |
| `optimizeJoinOfCountSql` | 布尔 | - | `true` |
| `maxLimit` | 整数 | - | `1` |
| `countId` | 字符串 | - | `"10001"` |
| `pages` | 整数 | - | `1` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "records": [
    {}
  ],
  "total": 1,
  "size": 1,
  "current": 1,
  "orders": [
    {}
  ]
}
```

### 3. deleteTutorial

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | deleteTutorial |
| 请求地址 | `/api/v1/tutorial/merchant/{id}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 4. updateByMerchant

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateByMerchant |
| 请求地址 | `/api/v1/tutorial/merchant/{id}` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `id` | 字符串 | 否 | - | `"10001"` |
| `title` | 字符串 | 否 | - | `"示例值"` |
| `type` | 字符串 | 否 | - | `"示例值"` |
| `duration` | 字符串 | 否 | - | `"示例值"` |
| `views` | 字符串 | 否 | - | `"示例值"` |
| `sourceType` | 字符串 | 否 | - | `"示例值"` |
| `sourceId` | 字符串 | 否 | - | `"10001"` |
| `authorType` | 字符串 | 否 | - | `"示例值"` |
| `authorId` | 字符串 | 否 | - | `"10001"` |
| `author` | 字符串 | 否 | - | `"示例值"` |
| `status` | 字符串 | 否 | - | `"1"` |
| `reviewStatus` | 字符串 | 否 | - | `"1"` |
| `reviewerId` | 字符串 | 否 | - | `"10001"` |
| `reviewTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `reviewComment` | 字符串 | 否 | - | `"示例值"` |
| `featured` | 布尔 | 否 | - | `true` |
| `linkedMerchantId` | 字符串 | 否 | - | `"10001"` |
| `linkedDishId` | 字符串 | 否 | - | `"10001"` |
| `aiModelVersion` | 字符串 | 否 | - | `"示例值"` |
| `content` | 字符串 | 否 | - | `"示例内容"` |
| `coverImage` | 字符串 | 否 | - | `"https://example.com/file.png"` |
| `videoUrl` | 字符串 | 否 | - | `"https://example.com/file.png"` |
| `tags` | 字符串 | 否 | - | `"示例值"` |
| `difficulty` | 字符串 | 否 | - | `"示例值"` |
| `calories` | 整数 | 否 | - | `1` |
| `prepTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `servings` | 整数 | 否 | - | `1` |
| `rating` | 数字 | 否 | - | `99.9` |
| `ratingCount` | 整数 | 否 | - | `1` |
| `favoriteCount` | 整数 | 否 | - | `1` |
| `viewCount` | 整数 | 否 | - | `1` |
| `shareCount` | 整数 | 否 | - | `1` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `official` | 布尔 | 否 | - | `true` |
| `editable` | 布尔 | 否 | - | `true` |
| `published` | 布尔 | 否 | - | `true` |
| `adminSource` | 布尔 | 否 | - | `true` |
| `merchantSource` | 布尔 | 否 | - | `true` |
| `aigenerated` | 布尔 | 否 | - | `true` |
| `userSource` | 布尔 | 否 | - | `true` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "title": "示例值",
  "type": "示例值",
  "duration": "示例值",
  "views": "示例值"
}
```

#### 响应示例

```json
{}
```

### 5. submitForReview

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | submitForReview |
| 请求地址 | `/api/v1/tutorial/merchant/{id}/submit` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

## tutorial-statistics-controller

### 1. getOverview

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getOverview |
| 请求地址 | `/api/v1/tutorial/stats/overview` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 2. favoriteTutorial

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | favoriteTutorial |
| 请求地址 | `/api/v1/tutorial/stats/{id}/favorite` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 3. rateTutorial

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | rateTutorial |
| 请求地址 | `/api/v1/tutorial/stats/{id}/rating` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 4. incrementViewCount

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | incrementViewCount |
| 请求地址 | `/api/v1/tutorial/stats/{id}/view` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

## tutorial-user-controller

### 1. createUserTutorial

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | createUserTutorial |
| 请求地址 | `/api/v1/tutorial/user/create` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 否 | - | `"10001"` |
| `title` | 字符串 | 否 | - | `"示例值"` |
| `type` | 字符串 | 否 | - | `"示例值"` |
| `duration` | 字符串 | 否 | - | `"示例值"` |
| `views` | 字符串 | 否 | - | `"示例值"` |
| `sourceType` | 字符串 | 否 | - | `"示例值"` |
| `sourceId` | 字符串 | 否 | - | `"10001"` |
| `authorType` | 字符串 | 否 | - | `"示例值"` |
| `authorId` | 字符串 | 否 | - | `"10001"` |
| `author` | 字符串 | 否 | - | `"示例值"` |
| `status` | 字符串 | 否 | - | `"1"` |
| `reviewStatus` | 字符串 | 否 | - | `"1"` |
| `reviewerId` | 字符串 | 否 | - | `"10001"` |
| `reviewTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `reviewComment` | 字符串 | 否 | - | `"示例值"` |
| `featured` | 布尔 | 否 | - | `true` |
| `linkedMerchantId` | 字符串 | 否 | - | `"10001"` |
| `linkedDishId` | 字符串 | 否 | - | `"10001"` |
| `aiModelVersion` | 字符串 | 否 | - | `"示例值"` |
| `content` | 字符串 | 否 | - | `"示例内容"` |
| `coverImage` | 字符串 | 否 | - | `"https://example.com/file.png"` |
| `videoUrl` | 字符串 | 否 | - | `"https://example.com/file.png"` |
| `tags` | 字符串 | 否 | - | `"示例值"` |
| `difficulty` | 字符串 | 否 | - | `"示例值"` |
| `calories` | 整数 | 否 | - | `1` |
| `prepTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `servings` | 整数 | 否 | - | `1` |
| `rating` | 数字 | 否 | - | `99.9` |
| `ratingCount` | 整数 | 否 | - | `1` |
| `favoriteCount` | 整数 | 否 | - | `1` |
| `viewCount` | 整数 | 否 | - | `1` |
| `shareCount` | 整数 | 否 | - | `1` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `official` | 布尔 | 否 | - | `true` |
| `editable` | 布尔 | 否 | - | `true` |
| `published` | 布尔 | 否 | - | `true` |
| `adminSource` | 布尔 | 否 | - | `true` |
| `merchantSource` | 布尔 | 否 | - | `true` |
| `aigenerated` | 布尔 | 否 | - | `true` |
| `userSource` | 布尔 | 否 | - | `true` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `id` | 字符串 | - | `"10001"` |
| `title` | 字符串 | - | `"示例值"` |
| `type` | 字符串 | - | `"示例值"` |
| `duration` | 字符串 | - | `"示例值"` |
| `views` | 字符串 | - | `"示例值"` |
| `sourceType` | 字符串 | - | `"示例值"` |
| `sourceId` | 字符串 | - | `"10001"` |
| `authorType` | 字符串 | - | `"示例值"` |
| `authorId` | 字符串 | - | `"10001"` |
| `author` | 字符串 | - | `"示例值"` |
| `status` | 字符串 | - | `"1"` |
| `reviewStatus` | 字符串 | - | `"1"` |
| `reviewerId` | 字符串 | - | `"10001"` |
| `reviewTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `reviewComment` | 字符串 | - | `"示例值"` |
| `featured` | 布尔 | - | `true` |
| `linkedMerchantId` | 字符串 | - | `"10001"` |
| `linkedDishId` | 字符串 | - | `"10001"` |
| `aiModelVersion` | 字符串 | - | `"示例值"` |
| `content` | 字符串 | - | `"示例内容"` |
| `coverImage` | 字符串 | - | `"https://example.com/file.png"` |
| `videoUrl` | 字符串 | - | `"https://example.com/file.png"` |
| `tags` | 字符串 | - | `"示例值"` |
| `difficulty` | 字符串 | - | `"示例值"` |
| `calories` | 整数 | - | `1` |
| `prepTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `servings` | 整数 | - | `1` |
| `rating` | 数字 | - | `99.9` |
| `ratingCount` | 整数 | - | `1` |
| `favoriteCount` | 整数 | - | `1` |
| `viewCount` | 整数 | - | `1` |
| `shareCount` | 整数 | - | `1` |
| `createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `official` | 布尔 | - | `true` |
| `editable` | 布尔 | - | `true` |
| `published` | 布尔 | - | `true` |
| `adminSource` | 布尔 | - | `true` |
| `merchantSource` | 布尔 | - | `true` |
| `aigenerated` | 布尔 | - | `true` |
| `userSource` | 布尔 | - | `true` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "title": "示例值",
  "type": "示例值",
  "duration": "示例值",
  "views": "示例值"
}
```

#### 响应示例

```json
{
  "id": "10001",
  "title": "示例值",
  "type": "示例值",
  "duration": "示例值",
  "views": "示例值"
}
```

### 2. getMyTutorials

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getMyTutorials |
| 请求地址 | `/api/v1/tutorial/user/my` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `page` | 整数 | 否 | -（位置：query） | `1` |
| `size` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 3. deleteUserTutorial

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | deleteUserTutorial |
| 请求地址 | `/api/v1/tutorial/user/{id}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

### 4. updateUserTutorial

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateUserTutorial |
| 请求地址 | `/api/v1/tutorial/user/{id}` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `id` | 字符串 | 否 | - | `"10001"` |
| `title` | 字符串 | 否 | - | `"示例值"` |
| `type` | 字符串 | 否 | - | `"示例值"` |
| `duration` | 字符串 | 否 | - | `"示例值"` |
| `views` | 字符串 | 否 | - | `"示例值"` |
| `sourceType` | 字符串 | 否 | - | `"示例值"` |
| `sourceId` | 字符串 | 否 | - | `"10001"` |
| `authorType` | 字符串 | 否 | - | `"示例值"` |
| `authorId` | 字符串 | 否 | - | `"10001"` |
| `author` | 字符串 | 否 | - | `"示例值"` |
| `status` | 字符串 | 否 | - | `"1"` |
| `reviewStatus` | 字符串 | 否 | - | `"1"` |
| `reviewerId` | 字符串 | 否 | - | `"10001"` |
| `reviewTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `reviewComment` | 字符串 | 否 | - | `"示例值"` |
| `featured` | 布尔 | 否 | - | `true` |
| `linkedMerchantId` | 字符串 | 否 | - | `"10001"` |
| `linkedDishId` | 字符串 | 否 | - | `"10001"` |
| `aiModelVersion` | 字符串 | 否 | - | `"示例值"` |
| `content` | 字符串 | 否 | - | `"示例内容"` |
| `coverImage` | 字符串 | 否 | - | `"https://example.com/file.png"` |
| `videoUrl` | 字符串 | 否 | - | `"https://example.com/file.png"` |
| `tags` | 字符串 | 否 | - | `"示例值"` |
| `difficulty` | 字符串 | 否 | - | `"示例值"` |
| `calories` | 整数 | 否 | - | `1` |
| `prepTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `servings` | 整数 | 否 | - | `1` |
| `rating` | 数字 | 否 | - | `99.9` |
| `ratingCount` | 整数 | 否 | - | `1` |
| `favoriteCount` | 整数 | 否 | - | `1` |
| `viewCount` | 整数 | 否 | - | `1` |
| `shareCount` | 整数 | 否 | - | `1` |
| `createTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |
| `official` | 布尔 | 否 | - | `true` |
| `editable` | 布尔 | 否 | - | `true` |
| `published` | 布尔 | 否 | - | `true` |
| `adminSource` | 布尔 | 否 | - | `true` |
| `merchantSource` | 布尔 | 否 | - | `true` |
| `aigenerated` | 布尔 | 否 | - | `true` |
| `userSource` | 布尔 | 否 | - | `true` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "title": "示例值",
  "type": "示例值",
  "duration": "示例值",
  "views": "示例值"
}
```

#### 响应示例

```json
{}
```

### 5. submitUserTutorial

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | submitUserTutorial |
| 请求地址 | `/api/v1/tutorial/user/{id}/submit` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

默认返回通用响应结构。

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{}
```

## user-controller

### 1. submitFeedback

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | submitFeedback |
| 请求地址 | `/api/v1/users/feedback` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. login

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | login |
| 请求地址 | `/api/v1/users/login` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `phone` | 字符串 | 否 | - | `"13800138000"` |
| `password` | 字符串 | 否 | - | `"123456"` |
| `captcha` | 字符串 | 否 | - | `"示例值"` |
| `checkCodeKey` | 字符串 | 否 | - | `"200"` |
| `code` | 字符串 | 否 | - | `"200"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "phone": "13800138000",
  "password": "123456",
  "captcha": "示例值",
  "checkCodeKey": "200",
  "code": "200"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. register

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | register |
| 请求地址 | `/api/v1/users/register` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `phone` | 字符串 | 否 | - | `"13800138000"` |
| `password` | 字符串 | 否 | - | `"123456"` |
| `captcha` | 字符串 | 否 | - | `"示例值"` |
| `checkCodeKey` | 字符串 | 否 | - | `"200"` |
| `nickname` | 字符串 | 否 | - | `"示例名称"` |
| `email` | 字符串 | 否 | - | `"demo@example.com"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "phone": "13800138000",
  "password": "123456",
  "captcha": "示例值",
  "checkCodeKey": "200",
  "nickname": "示例名称"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. resetPassword_1

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | resetPassword_1 |
| 请求地址 | `/api/v1/users/reset-password` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. searchUsers

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | searchUsers |
| 请求地址 | `/api/v1/users/search` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `keyword` | 字符串 | 是 | -（位置：query） | `"示例值"` |
| `searchType` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `userId` | 字符串 | 否 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 6. sendEmailCode

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | sendEmailCode |
| 请求地址 | `/api/v1/users/send-email-code` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 7. sendSmsCode

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | sendSmsCode |
| 请求地址 | `/api/v1/users/send-sms-code` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 8. getUserInfo

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getUserInfo |
| 请求地址 | `/api/v1/users/{userId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 9. updateUser

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateUser |
| 请求地址 | `/api/v1/users/{userId}` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 10. uploadAvatar

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | uploadAvatar |
| 请求地址 | `/api/v1/users/{userId}/avatar` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `file` | 字符串 | 是 | - | `"https://example.com/file.png"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "file": "https://example.com/file.png"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 11. uploadAvatarBase64

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | uploadAvatarBase64 |
| 请求地址 | `/api/v1/users/{userId}/avatar/base64` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 12. updateUserInfo

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateUserInfo |
| 请求地址 | `/api/v1/users/{userId}/info` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 13. updatePassword

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updatePassword |
| 请求地址 | `/api/v1/users/{userId}/password` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 14. getPreferences

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getPreferences |
| 请求地址 | `/api/v1/users/{userId}/preferences` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 15. updatePreferences

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updatePreferences |
| 请求地址 | `/api/v1/users/{userId}/preferences` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `id` | 字符串 | 否 | - | `"10001"` |
| `userId` | 字符串 | 否 | - | `"10001"` |
| `tagWeights` | 字符串 | 否 | - | `"示例值"` |
| `disableWeatherRecommend` | 布尔 | 否 | - | `true` |
| `dietGoal` | 字符串 | 否 | - | `"示例值"` |
| `allergies` | 字符串 | 否 | - | `"示例值"` |
| `enableAiPersonalData` | 布尔 | 否 | - | `true` |
| `enableOrderNotification` | 布尔 | 否 | - | `true` |
| `enableActivityNotification` | 布尔 | 否 | - | `true` |
| `enableMerchantReplyNotification` | 布尔 | 否 | - | `true` |
| `enableGroupChatNotification` | 布尔 | 否 | - | `true` |
| `updateTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "id": "10001",
  "userId": "10001",
  "tagWeights": "示例值",
  "disableWeatherRecommend": true,
  "dietGoal": "示例值"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## user-statistics-controller

### 1. getCaloriesStatistics

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getCaloriesStatistics |
| 请求地址 | `/api/v1/user-statistics/{userId}/calories` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `days` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. getConsumeStatistics

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getConsumeStatistics |
| 请求地址 | `/api/v1/user-statistics/{userId}/consume` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `days` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. getDietRecordsStatistics

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getDietRecordsStatistics |
| 请求地址 | `/api/v1/user-statistics/{userId}/diet-records` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |
| `days` | 整数 | 否 | -（位置：query） | `1` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. getFavoritesStatistics

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getFavoritesStatistics |
| 请求地址 | `/api/v1/user-statistics/{userId}/favorites` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. getOverviewStatistics

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getOverviewStatistics |
| 请求地址 | `/api/v1/user-statistics/{userId}/overview` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## verification-controller

### 1. sendVerificationCode

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | sendVerificationCode |
| 请求地址 | `/api/v1/verification/send` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `phone` | 字符串 | 是 | -（位置：query） | `"13800138000"` |
| `type` | 字符串 | 否 | -（位置：query） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象<字符串,对象> | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. verifyCode

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | verifyCode |
| 请求地址 | `/api/v1/verification/verify` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `phone` | 字符串 | 是 | -（位置：query） | `"13800138000"` |
| `code` | 字符串 | 是 | -（位置：query） | `"200"` |
| `type` | 字符串 | 否 | -（位置：query） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象<字符串,对象> | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## wallet-controller

### 1. getBalance

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getBalance |
| 请求地址 | `/api/v1/wallet/balance/{userId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. checkBalance

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | checkBalance |
| 请求地址 | `/api/v1/wallet/check` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `amount` | 数字 | 是 | -（位置：query） | `99.9` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 3. getWalletInfo

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getWalletInfo |
| 请求地址 | `/api/v1/wallet/info/{userId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 4. recharge

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | recharge |
| 请求地址 | `/api/v1/wallet/recharge` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `amount` | 数字 | 是 | -（位置：query） | `99.9` |
| `rechargeNo` | 字符串 | 是 | -（位置：query） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 5. withdraw

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | withdraw |
| 请求地址 | `/api/v1/wallet/withdraw` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `amount` | 数字 | 是 | -（位置：query） | `99.9` |
| `withdrawNo` | 字符串 | 是 | -（位置：query） | `"示例值"` |
| `withdrawMethod` | 字符串 | 否 | -（位置：query） | `"示例值"` |
| `accountInfo` | 字符串 | 否 | -（位置：query） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## wallet-security-controller

### 1. updateWalletLockStatus

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | updateWalletLockStatus |
| 请求地址 | `/api/v1/wallet/lock-status` |
| 请求方式 | `PUT` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |
| `locked` | 布尔 | 是 | -（位置：query） | `true` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象<字符串,对象> | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

### 2. getWalletSecuritySettings

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getWalletSecuritySettings |
| 请求地址 | `/api/v1/wallet/security-settings` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `userId` | 字符串 | 是 | -（位置：query） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象<字符串,对象> | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## weather-controller

### 1. getWeatherInfo

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getWeatherInfo |
| 请求地址 | `/api/v1/weather` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `city` | 字符串 | 是 | -（位置：query） | `"示例值"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 对象 | - | `{}` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {}
}
```

## wish-list-controller

### 1. appealWishListItem

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | appealWishListItem |
| 请求地址 | `/api/v1/wish-list/appeal` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `wishListItemId` | 字符串 | 是 | - | `"10001"` |
| `appealContent` | 字符串 | 是 | - | `"示例内容"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 布尔 | - | `true` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "wishListItemId": "10001",
  "appealContent": "示例内容"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": true
}
```

### 2. auditWishListItem

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | auditWishListItem |
| 请求地址 | `/api/v1/wish-list/audit` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `wishListItemId` | 字符串 | 是 | - | `"10001"` |
| `approved` | 布尔 | 是 | - | `true` |
| `rejectionReasonCode` | 整数 | 否 | - | `1` |
| `rejectionReason` | 字符串 | 否 | - | `"示例内容"` |
| `auditRemark` | 字符串 | 否 | - | `"示例内容"` |
| `actualAvailableTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 布尔 | - | `true` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "wishListItemId": "10001",
  "approved": true,
  "rejectionReasonCode": 1,
  "rejectionReason": "示例内容",
  "auditRemark": "示例内容"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": true
}
```

### 3. autoAuditTimeoutItems

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | autoAuditTimeoutItems |
| 请求地址 | `/api/v1/wish-list/auto-audit-timeout` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 整数 | - | `1` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": 1
}
```

### 4. createWishListItem

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | createWishListItem |
| 请求地址 | `/api/v1/wish-list/item` |
| 请求方式 | `POST` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `merchantId` | 字符串 | 否 | - | `"10001"` |
| `dishName` | 字符串 | 是 | - | `"示例名称"` |
| `dishImage` | 字符串 | 否 | - | `"https://example.com/file.png"` |
| `tasteRequirement` | 字符串 | 否 | - | `"示例值"` |
| `description` | 字符串 | 否 | - | `"示例值"` |
| `recipeId` | 字符串 | 否 | - | `"10001"` |
| `expectedAvailableTime` | 字符串 | 否 | - | `"2026-04-15 21:00:00"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 字符串 | - | `"示例值"` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{
  "merchantId": "10001",
  "dishName": "示例名称",
  "dishImage": "https://example.com/file.png",
  "tasteRequirement": "示例值",
  "description": "示例值"
}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": "示例值"
}
```

### 5. withdrawWishListItem

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | withdrawWishListItem |
| 请求地址 | `/api/v1/wish-list/item/{itemId}` |
| 请求方式 | `DELETE` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `itemId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 布尔 | - | `true` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": true
}
```

### 6. getWishListItemDetail

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getWishListItemDetail |
| 请求地址 | `/api/v1/wish-list/item/{itemId}` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| `itemId` | 字符串 | 是 | -（位置：path） | `"10001"` |

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | WishListItemDetailVO | - | `{"id": "10001", "userId": "10001", "userNickname": "示例名称", "userAvatar": "https://example.com/file.png", "merchantId": "10001"}` |
| `data.id` | 字符串 | - | `"10001"` |
| `data.userId` | 字符串 | - | `"10001"` |
| `data.userNickname` | 字符串 | - | `"示例名称"` |
| `data.userAvatar` | 字符串 | - | `"https://example.com/file.png"` |
| `data.merchantId` | 字符串 | - | `"10001"` |
| `data.merchantName` | 字符串 | - | `"示例名称"` |
| `data.dishName` | 字符串 | - | `"示例名称"` |
| `data.dishImage` | 字符串 | - | `"https://example.com/file.png"` |
| `data.tasteRequirement` | 字符串 | - | `"示例值"` |
| `data.description` | 字符串 | - | `"示例值"` |
| `data.recipeId` | 字符串 | - | `"10001"` |
| `data.recipeName` | 字符串 | - | `"示例名称"` |
| `data.auditStatus` | 整数 | - | `1` |
| `data.auditStatusName` | 字符串 | - | `"1"` |
| `data.rejectionReasonCode` | 整数 | - | `1` |
| `data.rejectionReasonTitle` | 字符串 | - | `"示例内容"` |
| `data.rejectionReasonDescription` | 字符串 | - | `"示例内容"` |
| `data.auditRemark` | 字符串 | - | `"示例内容"` |
| `data.auditorName` | 字符串 | - | `"示例名称"` |
| `data.auditTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data.isAppealed` | 布尔 | - | `true` |
| `data.appealContent` | 字符串 | - | `"示例内容"` |
| `data.appealTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data.appealReply` | 字符串 | - | `"示例值"` |
| `data.appealReplyTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data.expectedAvailableTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data.actualAvailableTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data.timeoutTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data.remainingHours` | 整数 | - | `1` |
| `data.createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data.canAppeal` | 布尔 | - | `true` |
| `data.canWithdraw` | 布尔 | - | `true` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": {
    "id": "10001",
    "userId": "10001",
    "userNickname": "示例名称",
    "userAvatar": "https://example.com/file.png",
    "merchantId": "10001"
  }
}
```

### 7. getUserWishList

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getUserWishList |
| 请求地址 | `/api/v1/wish-list/items` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 数组<WishListItemDetailVO> | - | `[{"id": "10001", "userId": "10001", "userNickname": "示例名称", "userAvatar": "https://example.com/file.png", "merchantId": "10001"}]` |
| `data[].id` | 字符串 | - | `"10001"` |
| `data[].userId` | 字符串 | - | `"10001"` |
| `data[].userNickname` | 字符串 | - | `"示例名称"` |
| `data[].userAvatar` | 字符串 | - | `"https://example.com/file.png"` |
| `data[].merchantId` | 字符串 | - | `"10001"` |
| `data[].merchantName` | 字符串 | - | `"示例名称"` |
| `data[].dishName` | 字符串 | - | `"示例名称"` |
| `data[].dishImage` | 字符串 | - | `"https://example.com/file.png"` |
| `data[].tasteRequirement` | 字符串 | - | `"示例值"` |
| `data[].description` | 字符串 | - | `"示例值"` |
| `data[].recipeId` | 字符串 | - | `"10001"` |
| `data[].recipeName` | 字符串 | - | `"示例名称"` |
| `data[].auditStatus` | 整数 | - | `1` |
| `data[].auditStatusName` | 字符串 | - | `"1"` |
| `data[].rejectionReasonCode` | 整数 | - | `1` |
| `data[].rejectionReasonTitle` | 字符串 | - | `"示例内容"` |
| `data[].rejectionReasonDescription` | 字符串 | - | `"示例内容"` |
| `data[].auditRemark` | 字符串 | - | `"示例内容"` |
| `data[].auditorName` | 字符串 | - | `"示例名称"` |
| `data[].auditTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].isAppealed` | 布尔 | - | `true` |
| `data[].appealContent` | 字符串 | - | `"示例内容"` |
| `data[].appealTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].appealReply` | 字符串 | - | `"示例值"` |
| `data[].appealReplyTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].expectedAvailableTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].actualAvailableTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].timeoutTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].remainingHours` | 整数 | - | `1` |
| `data[].createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].canAppeal` | 布尔 | - | `true` |
| `data[].canWithdraw` | 布尔 | - | `true` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": [
    {}
  ]
}
```

### 8. getMerchantPendingList

#### 接口基本信息

| 项目 | 内容 |
| --- | --- |
| 接口名称 | getMerchantPendingList |
| 请求地址 | `/api/v1/wish-list/merchant/pending` |
| 请求方式 | `GET` |
| 接口描述 | - |

#### 请求参数

无请求参数。

#### 响应参数

| 参数名 | 类型 | 说明 | 示例值 |
| --- | --- | --- | --- |
| `success` | 布尔 | - | `true` |
| `code` | 字符串 | - | `"200"` |
| `message` | 字符串 | - | `"示例内容"` |
| `data` | 数组<WishListItemDetailVO> | - | `[{"id": "10001", "userId": "10001", "userNickname": "示例名称", "userAvatar": "https://example.com/file.png", "merchantId": "10001"}]` |
| `data[].id` | 字符串 | - | `"10001"` |
| `data[].userId` | 字符串 | - | `"10001"` |
| `data[].userNickname` | 字符串 | - | `"示例名称"` |
| `data[].userAvatar` | 字符串 | - | `"https://example.com/file.png"` |
| `data[].merchantId` | 字符串 | - | `"10001"` |
| `data[].merchantName` | 字符串 | - | `"示例名称"` |
| `data[].dishName` | 字符串 | - | `"示例名称"` |
| `data[].dishImage` | 字符串 | - | `"https://example.com/file.png"` |
| `data[].tasteRequirement` | 字符串 | - | `"示例值"` |
| `data[].description` | 字符串 | - | `"示例值"` |
| `data[].recipeId` | 字符串 | - | `"10001"` |
| `data[].recipeName` | 字符串 | - | `"示例名称"` |
| `data[].auditStatus` | 整数 | - | `1` |
| `data[].auditStatusName` | 字符串 | - | `"1"` |
| `data[].rejectionReasonCode` | 整数 | - | `1` |
| `data[].rejectionReasonTitle` | 字符串 | - | `"示例内容"` |
| `data[].rejectionReasonDescription` | 字符串 | - | `"示例内容"` |
| `data[].auditRemark` | 字符串 | - | `"示例内容"` |
| `data[].auditorName` | 字符串 | - | `"示例名称"` |
| `data[].auditTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].isAppealed` | 布尔 | - | `true` |
| `data[].appealContent` | 字符串 | - | `"示例内容"` |
| `data[].appealTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].appealReply` | 字符串 | - | `"示例值"` |
| `data[].appealReplyTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].expectedAvailableTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].actualAvailableTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].timeoutTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].remainingHours` | 整数 | - | `1` |
| `data[].createTime` | 字符串 | - | `"2026-04-15 21:00:00"` |
| `data[].canAppeal` | 布尔 | - | `true` |
| `data[].canWithdraw` | 布尔 | - | `true` |

#### 异常响应

| 错误码 | 错误信息 | 异常场景 |
| --- | --- | --- |
| `400` | 请求参数错误 | 缺少必填参数、字段类型错误、业务校验失败 |
| `401` | 未授权或登录失效 | 未携带令牌、令牌失效 |
| `403` | 无权限访问 | 角色权限不足 |
| `404` | 资源不存在 | 路径参数对应记录不存在 |
| `405` | 请求方法不支持 | 请求方式与控制器定义不一致 |
| `500` | 系统异常，请联系管理员 | 未捕获异常、服务内部错误 |

#### 请求示例

```json
{}
```

#### 响应示例

```json
{
  "success": true,
  "code": "200",
  "message": "示例内容",
  "data": [
    {}
  ]
}
```
