# API 对接文档 - 堂食/自取模式

## 📋 文档概述

本文档为前端开发人员提供堂食/自取模式的 API 对接说明，包括参数格式、字段说明和示例代码。

**重要变更**：系统已从**配送模式**切换为**堂食/自取模式**，请前端团队同步更新相关代码。

---

## 🔄 主要变更说明

### 变更前（配送模式）- ❌ 已废弃

```json
{
  "userId": "xxx",
  "merchantId": "xxx",
  "dishItems": [...],
  "deliveryAddress": "XX大学XX宿舍",
  "phoneNumber": "13800138000",
  "note": "少辣"
}
```

**费用计算**：
- 菜品费 + 配送费（5元） + 包装费

### 变更后（堂食/自取模式）- ✅ 当前使用

```json
{
  "userId": "xxx",
  "merchantId": "xxx",
  "dishItems": [...],
  "diningMode": "dine_in",  // 或 "takeout"
  "tableNumber": "A12",       // 堂食时必需
  "note": "少辣"
}
```

**费用计算**：
- 菜品费 + 包装费（仅自取，2元/项）
- **无配送费**

---

## 📦 核心接口：创建订单

### 接口地址

```
POST /api/ai/tool/createOrder
```

### 请求参数

| 参数名 | 类型 | 必需 | 说明 | 示例 |
|--------|------|------|------|------|
| userId | String | ✅ | 用户ID | "3384650106421960" |
| merchantId | String | ✅ | 商家ID | "M1234567890123456" |
| dishItems | Array | ✅ | 菜品列表（数组） | 见下方示例 |
| diningMode | String | ✅ | 就餐方式 | "dine_in" 或 "takeout" |
| tableNumber | String | ⚠️ | 座号（堂食时必需） | "A12" |
| note | String | ❌ | 备注信息 | "少辣" |

### dishItems 格式

```json
[
  {
    "dishId": "1",
    "quantity": 1,
    "price": 15.5
  },
  {
    "dishId": "2",
    "quantity": 2,
    "price": 2.0
  }
]
```

**重要**：
- ✅ 必须是数组格式
- ✅ 每个元素必须包含 `dishId`, `quantity`, `price`
- ❌ 不能使用 `"dishIds": "1,2"` 这种字符串格式

### 响应示例

**成功响应**：
```json
{
  "code": 200,
  "message": "订单创建成功",
  "data": {
    "orderId": "ORD20260325001",
    "totalAmount": 17.5,
    "status": 0,
    "estimatedTime": "15-20分钟"
  }
}
```

**错误响应**：
```json
{
  "code": 400,
  "message": "缺少必需参数，需要：userId、merchantId、diningMode（就餐方式：dine_in/takeout）"
}
```

---

## 🧮 费用计算接口

### 接口地址

```
POST /api/ai/tool/calculateOrderPrice
```

### 请求参数

| 参数名 | 类型 | 必需 | 说明 |
|--------|------|------|------|
| dishItemsJson | String | ✅ | 菜品列表（JSON字符串） |
| userId | String | ✅ | 用户ID |
| diningMode | String | ✅ | 就餐方式（"dine_in" 或 "takeout"） |

### 请求示例

```json
{
  "dishItemsJson": "[{\"dishId\":\"1\",\"quantity\":2,\"price\":15.5}]",
  "userId": "3384650106421960",
  "diningMode": "takeout"
}
```

### 费用规则

| 就餐方式 | 配送费 | 包装费 | 说明 |
|----------|--------|--------|------|
| 堂食 (dine_in) | 0元 | 0元 | 堂食无任何附加费用 |
| 自取 (takeout) | 0元 | 2元/项 | 每个菜品收取2元包装费 |

### 计算示例

**示例 1：堂食，2个菜品**
```
菜品费用：15.5 × 2 = 31.0元
配送费：0元
包装费：0元（堂食无包装费）
总计：31.0元
```

**示例 2：自取，2个菜品**
```
菜品费用：15.5 × 2 = 31.0元
配送费：0元
包装费：2 × 2 = 4.0元
总计：35.0元
```

---

## 📝 前端对接指南

### 1. 订单确认页面调整

#### 需要移除的字段

```vue
<!-- ❌ 删除配送地址相关 -->
<el-form-item label="配送地址">
  <el-input v-model="form.deliveryAddress" />
</el-form-item>

<el-form-item label="联系电话">
  <el-input v-model="form.phoneNumber" />
</el-form-item>

<!-- ❌ 删除配送费显示 -->
<div class="fee-item">
  <span>配送费</span>
  <span>¥5.00</span>
</div>
```

#### 需要新增的字段

```vue
<!-- ✅ 新增就餐方式选择 -->
<el-form-item label="就餐方式" required>
  <el-radio-group v-model="form.diningMode">
    <el-radio label="dine_in">堂食</el-radio>
    <el-radio label="takeout">自取</el-radio>
  </el-radio-group>
</el-form-item>

<!-- ✅ 新增座号输入（堂食时显示） -->
<el-form-item
  v-if="form.diningMode === 'dine_in'"
  label="座号"
  required
>
  <el-input v-model="form.tableNumber" placeholder="请输入座号，如：A12" />
</el-form-item>

<!-- ✅ 调整包装费显示（仅自取时显示） -->
<div v-if="form.diningMode === 'takeout'" class="fee-item">
  <span>包装费</span>
  <span>¥{{ packagingFee.toFixed(2) }}</span>
</div>
```

### 2. 表单数据结构

```javascript
const form = reactive({
  userId: '',
  merchantId: '',
  dishItems: [], // ✅ 必须是数组
  diningMode: 'dine_in', // ✅ 新增字段
  tableNumber: '', // ✅ 新增字段（堂食时必需）
  note: ''
  // ❌ 删除字段：deliveryAddress, phoneNumber
})
```

### 3. 提交订单函数

```javascript
const submitOrder = async () => {
  // 验证就餐方式
  if (!form.diningMode) {
    ElMessage.error('请选择就餐方式')
    return
  }

  // 验证座号（堂食时必需）
  if (form.diningMode === 'dine_in' && !form.tableNumber) {
    ElMessage.error('堂食时请填写座号')
    return
  }

  // 构建请求参数
  const params = {
    userId: form.userId,
    merchantId: form.merchantId,
    dishItems: form.dishItems.map(item => ({
      dishId: item.dishId,
      quantity: item.quantity,
      price: item.price
    })),
    diningMode: form.diningMode,
    ...(form.diningMode === 'dine_in' && { tableNumber: form.tableNumber }),
    ...(form.note && { note: form.note })
  }

  try {
    const res = await createOrder(params)
    ElMessage.success('订单创建成功')
    // 跳转到支付页面
  } catch (error) {
    ElMessage.error(error.message || '订单创建失败')
  }
}
```

### 4. 费用计算函数

```javascript
const calculateTotal = () => {
  // 菜品费用
  const dishTotal = form.dishItems.reduce((sum, item) => {
    return sum + (item.price * item.quantity)
  }, 0)

  // 配送费：固定0元
  const deliveryFee = 0

  // 包装费：仅自取时收取
  const packagingFee = form.diningMode === 'takeout'
    ? form.dishItems.length * 2
    : 0

  // 总计
  const total = dishTotal + deliveryFee + packagingFee

  return {
    dishTotal,
    deliveryFee,
    packagingFee,
    total
  }
}
```

---

## 🔄 字段映射表

### 旧字段 → 新字段

| 旧字段（已废弃） | 新字段 | 说明 |
|----------------|--------|------|
| deliveryAddress | diningMode | "dine_in"=堂食, "takeout"=自取 |
| phoneNumber | tableNumber | 座号（堂食时） |
| - | 就餐信息 | 订单详情页显示 |
| 配送费（5元） | 0元 | 所有模式均无配送费 |
| 包装费（每项2元） | 包装费（自取时每项2元） | 堂食无包装费 |

---

## 📊 订单详情页面调整

### address 字段显示逻辑

```vue
<template>
  <div class="order-info">
    <!-- 旧版显示（已废弃） -->
    <!-- <div>配送地址：{{ order.address }}</div> -->

    <!-- 新版显示 -->
    <div>
      就餐信息：{{ formatDiningInfo(order) }}
    </div>
  </div>
</template>

<script setup>
const formatDiningInfo = (order) => {
  // address 字段格式：
  // 堂食示例：堂食 - 座号：A12
  // 自取示例：自取

  if (order.address.includes('堂食')) {
    return order.address // 显示"堂食 - 座号：A12"
  } else if (order.address.includes('自取')) {
    return '🥡 自取' // 显示"自取"
  }
  return order.address
}
</script>
```

---

## 🧪 测试用例

### 测试用例 1：堂食订单

```javascript
// 请求参数
{
  userId: "3384650106421960",
  merchantId: "M1234567890123456",
  dishItems: [
    { dishId: "1", quantity: 1, price: 15.5 }
  ],
  diningMode: "dine_in",
  tableNumber: "A12",
  note: "少辣"
}

// 预期响应
{
  "code": 200,
  "message": "✅ 订单创建成功！\n\n📋 订单号：ORD20260325001\n...",
  "data": {
    "orderId": "ORD20260325001",
    "totalAmount": 15.5
  }
}
```

### 测试用例 2：自取订单

```javascript
// 请求参数
{
  userId: "3384650106421960",
  merchantId: "M1234567890123456",
  dishItems: [
    { dishId: "1", quantity: 2, price: 15.5 }
  ],
  diningMode: "takeout",
  note: "尽快准备好"
}

// 预期响应
{
  "code": 200,
  "message": "✅ 订单创建成功！...",
  "data": {
    "orderId": "ORD20260325001",
    "totalAmount": 35.0 // 31.0 + 4.0包装费
  }
}
```

---

## ⚠️ 常见错误

### 错误 1：使用旧的参数格式

**错误示例**：
```json
{
  "dishIds": "1,2",
  "quantity": {"1": 1, "2": 1}
}
```

**正确做法**：
```json
{
  "dishItems": [
    {"dishId": "1", "quantity": 1, "price": 15.5},
    {"dishId": "2", "quantity": 1, "price": 2.0}
  ]
}
```

### 错误 2：缺少必需参数

**错误示例**：
```json
{
  "userId": "xxx",
  "merchantId": "xxx",
  "dishItems": [...]
  // ❌ 缺少 diningMode
}
```

**正确做法**：
```json
{
  "userId": "xxx",
  "merchantId": "xxx",
  "dishItems": [...],
  "diningMode": "dine_in" // ✅ 必需参数
}
```

### 错误 3：堂食时缺少座号

**错误示例**：
```json
{
  "diningMode": "dine_in"
  // ❌ 缺少 tableNumber
}
```

**正确做法**：
```json
{
  "diningMode": "dine_in",
  "tableNumber": "A12" // ✅ 堂食时必需
}
```

---

## 📞 技术支持

如有疑问，请联系后端开发团队：

- **问题反馈**：提交 Issue 到项目仓库
- **技术文档**：查看项目根目录下的 `CLAUDE.md`
- **测试指南**：参考 `堂食自取模式测试指南.md`

---

## 🎉 更新日志

| 日期 | 版本 | 说明 |
|------|------|------|
| 2026-03-25 | v2.0 | 切换为堂食/自取模式，移除配送功能 |
| 2026-03-24 | v1.0 | 初始版本（配送模式） |

---

**请前端团队尽快完成适配，确保与后端 API 保持一致！**
