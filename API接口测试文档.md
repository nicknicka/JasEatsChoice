# 佳食宜选 - API接口测试文档

**文档版本**：V1.0
**生成日期**：2025-01-30
**基础URL**：`http://localhost:8080`

---

## 目录

1. [菜品步骤管理API](#一菜品步骤管理api)
2. [备注冲突检测API](#二备注冲突检测api)
3. [想吃列表审核API](#三想吃列表审核api)
4. [通用说明](#四通用说明)

---

## 一、菜品步骤管理API

### 1.1 更新单个菜品步骤

**接口描述**：更新单个订单菜品的制作步骤

**请求路径**：`POST /v1/dish-steps/update`

**请求参数**：
```json
{
  "orderDishId": "订单菜品ID",
  "newStepStatus": 3,
  "operationType": "FORWARD",
  "remark": "备注信息",
  "estimatedMinutes": 15
}
```

**参数说明**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderDishId | String | 是 | 订单菜品ID |
| newStepStatus | Integer | 是 | 新步骤状态：0-待备菜, 1-备菜中, 2-预处理中, 3-烹饪中, 4-摆盘中, 5-待上菜, 6-已上菜, 10-快餐制作中, 11-快餐打包中, 12-快餐待出餐, 13-快餐已出餐 |
| operationType | String | 否 | 操作类型：FORWARD-前进, BACKWARD-回退, SKIP-跳过 |
| remark | String | 否 | 备注信息 |
| estimatedMinutes | Integer | 否 | 预计耗时（分钟） |

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": true
}
```

**Curl示例**：
```bash
curl -X POST "http://localhost:8080/v1/dish-steps/update" \
  -H "Content-Type: application/json" \
  -d '{
    "orderDishId": "1234567890",
    "newStepStatus": 3,
    "operationType": "FORWARD",
    "remark": "开始烹饪",
    "estimatedMinutes": 15
  }'
```

---

### 1.2 批量更新菜品步骤

**接口描述**：批量更新多个菜品到同一状态

**请求路径**：`POST /v1/dish-steps/batch-update`

**请求参数**：
```json
{
  "orderDishIds": ["id1", "id2", "id3"],
  "newStepStatus": 5,
  "operationType": "FORWARD",
  "remark": "统一标记",
  "estimatedMinutes": 10
}
```

**参数说明**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderDishIds | Array | 是 | 订单菜品ID列表 |
| newStepStatus | Integer | 是 | 目标步骤状态 |
| operationType | String | 否 | 操作类型 |
| remark | String | 否 | 备注信息 |
| estimatedMinutes | Integer | 否 | 预计耗时（分钟） |

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": "成功更新 3 个菜品步骤"
}
```

---

### 1.3 批量标记菜品步骤

**接口描述**：勾选多个菜品统一标记到某一步骤

**请求路径**：`POST /v1/dish-steps/batch-mark`

**请求参数**：
- **Body**: `["id1", "id2", "id3"]` (订单菜品ID列表)
- **Query**: `targetStepStatus=5` (目标步骤状态)

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": "成功标记 3 个菜品"
}
```

---

### 1.4 回退菜品步骤

**接口描述**：将菜品步骤回退到之前的步骤

**请求路径**：`POST /v1/dish-steps/rollback`

**请求参数**：
```json
{
  "orderDishId": "1234567890",
  "newStepStatus": 1,
  "operationType": "BACKWARD",
  "rollbackReason": "味道不够，重新备料",
  "remark": "回退操作"
}
```

**参数说明**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| rollbackReason | String | **是** | 回退原因（必填） |
| newStepStatus | Integer | 是 | 回退目标步骤 |

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": true
}
```

---

### 1.5 获取订单所有菜品的步骤详情

**接口路径**：`GET /v1/dish-steps/order/{orderId}`

**路径参数**：
- `orderId` - 订单ID

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": [
    {
      "orderDishId": "1234567890",
      "orderId": "order001",
      "dishId": "dish001",
      "dishName": "宫保鸡丁",
      "dishImage": "https://example.com/image.jpg",
      "quantity": 2,
      "stepStatus": 3,
      "stepStatusName": "烹饪中",
      "stepStartTime": "2025-01-30T12:00:00",
      "estimatedCompletionTime": "2025-01-30T12:15:00",
      "cookingMinutes": 15,
      "stepSort": 1,
      "isFastFood": false,
      "servingStatus": 0,
      "elapsedMinutes": 5,
      "remainingMinutes": 10,
      "progressPercent": 42,
      "stepHistory": [
        {
          "id": "history001",
          "oldStepStatus": 2,
          "oldStepStatusName": "预处理中",
          "newStepStatus": 3,
          "newStepStatusName": "烹饪中",
          "operationType": "FORWARD",
          "operatorName": "张师傅",
          "createTime": "2025-01-30T12:00:00",
          "estimatedMinutes": 15
        }
      ]
    }
  ]
}
```

---

### 1.6 根据步骤状态筛选订单菜品

**接口路径**：`GET /v1/dish-steps/filter`

**查询参数**：
- `orderId` - 订单ID
- `stepStatus` - 步骤状态

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": [/* 同上 */]
}
```

---

### 1.7 初始化订单菜品步骤

**接口路径**：`POST /v1/dish-steps/initialize/{orderId}`

**路径参数**：
- `orderId` - 订单ID

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": true
}
```

---

## 二、备注冲突检测API

### 2.1 检测备注冲突

**接口描述**：检测用户备注与菜品成分的冲突

**请求路径**：`POST /v1/remark-conflict/check`

**请求参数**：
```json
{
  "dishId": "dish001",
  "remark": "免辣，不要葱",
  "tasteTags": ["mild_no_spicy", "no_onion"],
  "userAllergies": "[\"花生\", \"芒果\"]",
  "preferenceTags": []
}
```

**参数说明**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| dishId | String | 是 | 菜品ID |
| remark | String | 是 | 备注内容 |
| tasteTags | Array | 否 | 选中的口味标签代码 |
| userAllergies | String | 否 | 用户过敏食材（JSON字符串） |
| preferenceTags | Array | 否 | 用户偏好标签 |

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": {
    "hasConflict": true,
    "conflictLevel": "HIGH",
    "conflicts": [
      {
        "conflictType": "ALLERGY",
        "description": "该菜品包含您的过敏食材",
        "conflictItem": "花生",
        "severity": 3,
        "priority": 1,
        "color": "red"
      }
    ],
    "suggestions": [
      "警告：如有花生过敏，请勿选择此菜品或提前告知商家"
    ]
  }
}
```

**冲突级别说明**：
- `HIGH` - 高（过敏风险，红色）
- `MEDIUM_HIGH` - 中高（核心需求，黄色）
- `MEDIUM` - 中（口味调整，蓝色）
- `LOW` - 低（一般备注，灰色）

---

### 2.2 解析备注中的口味标签

**接口路径**：`POST /v1/remark-conflict/parse-tags`

**查询参数**：
- `remark` - 备注内容

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": ["mild_no_spicy", "no_onion"]
}
```

---

### 2.3 获取菜品推荐口味标签

**接口路径**：`GET /v1/remark-conflict/recommended-tags/{dishId}`

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": [
    "mild_no_spicy",
    "no_onion",
    "no_garlic",
    "no_coriander",
    "less_sugar",
    "less_salt"
  ]
}
```

---

### 2.4 格式化备注

**接口路径**：`POST /v1/remark-conflict/format`

**查询参数**：
- `originalRemark` - 原始备注
- `tasteTags` - 选中的口味标签列表（JSON数组）

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": "【免辣】【不要葱】 我的孩子要吃清淡点"
}
```

---

## 三、想吃列表审核API

### 3.1 创建想吃列表项

**接口路径**：`POST /v1/wish-list/create`

**请求参数**：
```json
{
  "merchantId": "merchant001",
  "dishName": "红烧狮子头",
  "dishImage": "https://example.com/image.jpg",
  "tasteRequirement": "微甜，不辣",
  "description": "希望肥瘦适中，软烂一些",
  "recipeId": "recipe001",
  "expectedAvailableTime": "2025-02-05T12:00:00"
}
```

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "提交成功",
  "data": "wish123456"
}
```

---

### 3.2 查看用户的想吃列表

**接口路径**：`GET /v1/wish-list/my`

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": [
    {
      "id": "wish123456",
      "dishName": "红烧狮子头",
      "dishImage": "https://example.com/image.jpg",
      "auditStatus": 0,
      "auditStatusName": "待审核",
      "createTime": "2025-01-30T10:00:00",
      "timeoutTime": "2025-01-31T10:00:00",
      "remainingHours": 20,
      "canAppeal": false,
      "canWithdraw": true
    }
  ]
}
```

---

### 3.3 商家查看待审核列表

**接口路径**：`GET /v1/wish-list/pending`

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": [
    {
      "id": "wish123456",
      "userId": "user001",
      "userNickname": "美食家小明",
      "dishName": "红烧狮子头",
      "dishImage": "https://example.com/image.jpg",
      "description": "希望肥瘦适中，软烂一些",
      "createTime": "2025-01-30T10:00:00",
      "timeoutTime": "2025-01-31T10:00:00",
      "remainingHours": 20
    }
  ]
}
```

---

### 3.4 商家审核想吃列表项

**接口路径**：`POST /v1/wish-list/audit`

**请求参数**：
```json
{
  "wishListItemId": "wish123456",
  "approved": false,
  "rejectionReasonCode": 1,
  "rejectionReason": "食材季节性短缺，河豚目前无法采购",
  "auditRemark": "建议等到春季再尝试",
  "actualAvailableTime": "2025-04-01"
}
```

**参数说明**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| approved | Boolean | 是 | true-通过, false-拒绝 |
| rejectionReasonCode | Integer | 条件 | 拒绝时必填，1-9或99 |
| rejectionReason | String | 条件 | 拒绝时必填 |
| auditRemark | String | 否 | 审核备注 |
| actualAvailableTime | String | 否 | 预计上架时间 |

**拒绝原因代码**：
- 1 - 食材季节性短缺
- 2 - 食材供应链问题
- 3 - 制作工艺过于复杂
- 4 - 成本过高
- 5 - 与餐厅定位不符
- 6 - 食品安全考虑
- 7 - 原料品质不稳定
- 8 - 制作时间过长
- 9 - 特殊设备限制
- 99 - 其他原因

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "审核成功",
  "data": true
}
```

---

### 3.5 用户申诉

**接口路径**：`POST /v1/wish-list/appeal`

**请求参数**：
```json
{
  "wishListItemId": "wish123456",
  "appealContent": "我可以接受用其他食材替代，请帮忙制作"
}
```

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "申诉成功",
  "data": true
}
```

---

### 3.6 商家回复申诉

**接口路径**：`POST /v1/wish-list/appeal-reply`

**请求参数**：
```json
{
  "wishListItemId": "wish123456",
  "approved": true,
  "appealReply": "好的，我们可以尝试用替代食材制作"
}
```

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "回复成功",
  "data": true
}
```

---

### 3.7 撤回想吃列表项

**接口路径**：`DELETE /v1/wish-list/{wishListItemId}`

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "撤回成功",
  "data": true
}
```

---

### 3.8 获取想吃列表项详情

**接口路径**：`GET /v1/wish-list/detail/{wishListItemId}`

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": {
    "id": "wish123456",
    "userId": "user001",
    "merchantId": "merchant001",
    "dishName": "红烧狮子头",
    "dishImage": "https://example.com/image.jpg",
    "tasteRequirement": "微甜，不辣",
    "description": "希望肥瘦适中，软烂一些",
    "auditStatus": 2,
    "auditStatusName": "已拒绝",
    "rejectionReasonCode": 1,
    "rejectionReasonTitle": "食材季节性短缺",
    "rejectionReason": "食材季节性短缺，河豚目前无法采购",
    "auditRemark": "建议等到春季再尝试",
    "auditorName": "李大厨",
    "auditTime": "2025-01-30T14:00:00",
    "isAppealed": false,
    "canAppeal": true,
    "canWithdraw": false
  }
}
```

---

## 四、通用说明

### 4.1 统一响应格式

所有接口均使用统一的响应格式：

**成功响应**：
```json
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": {}
}
```

**失败响应**：
```json
{
  "success": false,
  "code": "500",
  "message": "错误描述",
  "data": null
}
```

### 4.2 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 4.3 认证说明

大部分接口需要在请求头中携带用户认证信息：

```http
Headers:
  Authorization: Bearer {token}
  Content-Type: application/json
```

### 4.4 分页参数

列表类接口支持分页查询：

```json
{
  "page": 1,
  "size": 20,
  "total": 100
}
```

---

## 五、测试工具推荐

### 5.1 Postman

1. 导入环境变量：
   ```
   baseUrl = http://localhost:8080
   token = your-auth-token
   ```

2. 创建Collection（请求集合）：
   - 菜品步骤管理API
   - 备注冲突检测API
   - 想吃列表审核API

3. 设置Pre-request Script：
   ```javascript
   // 自动添加认证token
   if (!pm.environment.baseUrl && pm.request.url.indexOf('http') === -1) {
     pm.request.url = pm.environment.baseUrl + pm.request.url;
   }
   ```

### 5.2 Curl测试

**更新菜品步骤**：
```bash
curl -X POST "http://localhost:8080/v1/dish-steps/update" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "orderDishId": "1234567890",
    "newStepStatus": 3,
    "operationType": "FORWARD"
  }'
```

**检测备注冲突**：
```bash
curl -X POST "http://localhost:8080/v1/remark-conflict/check" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "dishId": "dish001",
    "remark": "免辣，不要葱",
    "tasteTags": ["mild_no_spicy", "no_onion"]
  }'
```

**创建想吃列表项**：
```bash
curl -X POST "http://localhost:8080/v1/wish-list/create" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "dishName": "红烧狮子头",
    "description": "希望肥瘦适中",
    "expectedAvailableTime": "2025-02-05T12:00:00"
  }'
```

### 5.3 JavaScript/Axios测试

```javascript
import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Authorization': 'Bearer ' + token,
    'Content-Type': 'application/json'
  }
})

// 更新菜品步骤
async function updateDishStep() {
  try {
    const response = await api.post('/v1/dish-steps/update', {
      orderDishId: '1234567890',
      newStepStatus: 3,
      operationType: 'FORWARD'
    })
    console.log('更新成功:', response.data)
  } catch (error) {
    console.error('更新失败:', error)
  }
}

// 检测备注冲突
async function checkRemarkConflict() {
  try {
    const response = await api.post('/v1/remark-conflict/check', {
      dishId: 'dish001',
      remark: '免辣，不要葱',
      tasteTags: ['mild_no_spicy', 'no_onion']
    })
    console.log('冲突检测结果:', response.data)
  } catch (error) {
    console.error('检测失败:', error)
  }
}
```

---

## 六、测试检查清单

### 6.1 菜品步骤管理API

- [ ] 更新单个菜品步骤
- [ ] 批量更新菜品步骤
- [ ] 批量标记菜品步骤
- [ ] 回退菜品步骤
- [ ] 获取订单菜品步骤详情
- [ ] 根据步骤状态筛选
- [ ] 初始化订单菜品步骤

### 6.2 备注冲突检测API

- [ ] 检测备注冲突
- [ ] 解析备注中的口味标签
- [ ] 获取菜品推荐标签
- [ ] 格式化备注

### 6.3 想吃列表审核API

- [ ] 创建想吃列表项
- [ ] 查看用户的想吃列表
- [ ] 商家查看待审核列表
- [ ] 商家审核（通过/拒绝）
- [ ] 用户申诉
- [ ] 商家回复申诉
- [ ] 撤回想吃列表项
- [ ] 获取列表项详情

---

## 七、常见问题

### Q1: 如何获取菜品ID？

A: 可以通过以下方式获取菜品ID：
- 查询商家菜品列表：`GET /v1/dish/list?merchantId={merchantId}`
- 搜索菜品：`GET /v1/dish/search?keyword={keyword}`

### Q2: 如何获取订单ID？

A: 用户下单后会返回订单ID，也可以通过以下方式查询：
- 我的订单列表：`GET /v1/orders/my`
- 订单详情：`GET /v1/orders/{orderId}`

### Q3: 审核状态码对应关系？

A:
- 0: 待审核
- 1: 已通过
- 2: 已拒绝
- 3: 申诉中
- 4: 申诉成功
- 5: 申诉失败
- 6: 超时自动通过
- 7: 已撤回

### Q4: 超时自动通过机制如何工作？

A: 系统会定时检查（建议每小时执行一次）：
- 检查创建时间超过24小时且状态仍为"待审核"的记录
- 自动将状态更新为"超时自动通过"
- 记录审核备注为"超时自动通过（24小时未审核）"

---

## 八、联系支持

如有问题，请联系开发团队或查看项目文档：
- 项目概述：佳食宜选.md
- PRD文档：产品需求说明书（PRD）.md
- 技术文档：佳食宜选技术实现指导.md

---

**文档更新日期**：2025-01-30
**API版本**：V1.0
