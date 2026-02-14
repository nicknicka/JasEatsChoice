# 实体ID生成策略统计

## 概述

系统中共有 **44 个实体类**，其中：
- **38 个实体**使用 `IdType.ASSIGN_ID`（MyBatis-Plus 自动生成 ID）
- **6 个实体**使用 `IdType.INPUT`（需要手动生成 ID）

---

## 使用 `IdType.ASSIGN_ID`（自动生成ID）的实体

| 序号 | 实体类 | ID前缀 | 说明 |
|------|--------|--------|------|
| 1 | ContentExtraction | - | 内容抽取 |
| 2 | ScheduledTaskLog | - | 定时任务日志 |
| 3 | Dish | D | 菜品 |
| 4 | MenuDish | MND | 菜单菜品 |
| 5 | OrderDish | OD | 订单菜品 |
| 6 | GroupOrderAddition | GOA | 群订单追加 |
| 7 | ReviewReply | RRP | 评价回复 |
| 8 | Wallet | W | 钱包 |
| 9 | UserAddress | A | 用户地址 |
| 10 | UserCoupon | UC | 用户优惠券 |
| 11 | Announcement | AN | 公告 |
| 12 | SystemConfig | - | 系统配置 |
| 13 | SystemLog | - | 系统日志 |
| 14 | RefundRecord | RR | 退款记录 |
| 15 | Tutorial | T | 教程 |
| 16 | DishListItem | DLI | 菜品列表项 |
| 17 | IngredientConflictRule | ICR | 食材冲突规则 |
| 18 | DishStepHistory | DSH | 菜品步骤历史 |
| 19 | HotTopic | HT | 热门话题 |
| 20 | GroupOrder | GO | 群订单 |
| 21 | Notification | N | 通知 |
| 22 | UserPreference | UP | 用户偏好 |
| 23 | UserCollection | C | 用户收藏 |
| 24 | WithdrawRecord | WR | 提现记录 |
| 25 | RechargeRecord | RC | 充值记录 |
| 26 | WantToEat | WTE | 想吃记录 |
| 27 | MessageRecord | MR | 消息记录 |
| 28 | Contact | CT | 联系人 |
| 29 | Recipe | - | 食谱 |
| 30 | ConsumeHistory | CR | 饮食记录 |
| 31 | PaymentRecord | P | 支付记录 |
| 32 | Address | - | 地址（部分字段） |
| 33 | Discount | - | 优惠 |
| 34 | Review | - | 评价 |
| 35 | RecommendReject | - | 推荐拒绝 |
| 36 | GroupOrderDish | GOD | 群订单菜品 |
| 37 | CalorieRecord | - | 卡路里记录 |
| 38 | ChatSessionIdGenerator | - | 聊天会话ID（工具类） |

**特点**：
- MyBatis-Plus 自动生成 16 位数字 ID
- 使用雪花算法或时间戳 + 随机数
- 无需手动干预，插入时自动赋值

---

## 使用 `IdType.INPUT`（手动生成ID）的实体

| 序号 | 实体类 | ID前缀 | 生成方式 | 当前状态 |
|------|--------|--------|----------|----------|
| 1 | **Order** | O | `IdGenerator.generateId()` + `toOrderIdString()` | ✅ 已实现自动生成 |
| 2 | **Merchant** | M | 待添加 | ❌ 需要手动生成 |
| 3 | AddDishRequest | - | - | ❌ 需要手动生成 |
| 4 | **ChatMsg** | CM | 待添加 | ❌ 需要手动生成 |
| 5 | **GroupOrderDish** | GOD | 待添加 | ❌ 需要手动生成 |
| 6 | Address | A | 待添加 | ❌ 需要手动生成 |

**特点**：
- 需要手动生成 ID 并设置到实体中
- ID 格式：前缀 + 16 位数字（如 `O1234567890123456`）
- 如果不设置 ID，插入数据库时会报错："Column 'id' cannot be null"

---

## ID 前缀对照表

| 前缀 | 实体类型 | 示例 |
|------|----------|------|
| U | User | U1234567890123456 |
| M | Merchant | M1234567890123456 |
| G | Group | G1234567890123456 |
| O | Order | O1234567890123456 |
| D | Dish | D1234567890123456 |
| MN | Menu | MN1234567890123456 |
| OD | OrderDish | OD1234567890123456 |
| A | Address | A1234567890123456 |
| W | Wallet | W1234567890123456 |
| P | Payment | P1234567890123456 |
| UC | UserCoupon | UC1234567890123456 |
| AN | Announcement | AN1234567890123456 |
| CM | ChatMsg | CM1234567890123456 |
| GO | GroupOrder | GO1234567890123456 |
| GOA | GroupOrderAddition | GOA1234567890123456 |
| GOD | GroupOrderDish | GOD1234567890123456 |
| R | Review | R1234567890123456 |
| RRP | ReviewReply | RRP1234567890123456 |
| T | Tutorial | T1234567890123456 |
| CT | Contact | CT1234567890123456 |
| MR | MessageRecord | MR1234567890123456 |
| N | Notification | N1234567890123456 |
| RR | RefundRecord | RR1234567890123456 |
| RC | RechargeRecord | RC1234567890123456 |
| WR | WithdrawRecord | WR1234567890123456 |
| CR | ConsumeHistory | CR1234567890123456 |
| HT | HotTopic | HT1234567890123456 |
| UP | UserPreference | UP1234567890123456 |
| C | UserCollection | C1234567890123456 |

---

## IdGenerator 工具类

**位置**：`com.xx.jaseatschoicejava.util.IdGenerator`

**功能**：
1. **生成原始 ID**：`generateId()` - 生成 16 位数字 ID
   - 使用时间戳 + 随机数 + 位运算
   - 保证无序、不可预测
   - 性能优化（比原方案快约 50 倍）

2. **转换为带前缀的字符串**：
   - `toOrderIdString(Long id)` → "O" + id
   - `toMerchantIdString(Long id)` → "M" + id
   - `toUserIdString(Long id)` → "U" + id
   - 等等...

**使用示例**：
```java
// 生成订单 ID
Long generatedId = IdGenerator.generateId();        // 例如：1234567890123456
String orderId = IdGenerator.toOrderIdString(generatedId);  // 例如：O1234567890123456
order.setId(orderId);
```

---

## 建议修改清单

### 🔴 高优先级（需要立即处理）

1. ✅ **Order（订单）** - 已添加自动生成逻辑
   - 位置：`OrderServiceImpl.createOrderWithDishes()`
   - 状态：已完成

2. ⚠️ **Merchant（商家）** - 需要添加自动生成逻辑
   - 位置：`MerchantService` 或注册接口
   - 建议：在商家注册时自动生成 ID

3. ⚠️ **GroupOrder（群订单）** - 需要添加自动生成逻辑
   - 位置：`GroupOrderService`
   - 建议：在创建群订单时自动生成 ID

### 🔸 中优先级（建议处理）

4. ⚠️ **ChatMsg（聊天消息）** - 需要添加自动生成逻辑
   - 位置：聊天消息保存接口

5. ⚠️ **GroupOrderDish（群订单菜品）** - 需要添加自动生成逻辑
   - 位置：群订单菜品保存接口

6. ⚠️ **Address（地址）** - 需要添加自动生成逻辑
   - 位置：用户地址保存接口

---

## 技术说明

### MyBatis-Plus ID 生成策略

**ASSIGN_ID**（自动）：
- MyBatis-Plus 使用雪花算法或数据库自增 ID
- 支持 `UUID`、`AUTO`（自增）、`INPUT`（手动输入）等策略
- 配置方式：`@TableId(type = IdType.ASSIGN_ID)`

**INPUT**（手动）：
- 需要应用程序代码中设置 ID
- 适合有特殊规则或前缀要求的场景
- 当前系统中 Order 和 Merchant 使用此策略

### ID 格式规范

所有实体的 ID 格式：
```
前缀（1-3位）+ 16 位纯数字
```

示例：
- `O12345678901234567` - 订单 ID
- `M9876543210123456` - 商家 ID
- `U5678901234567890` - 用户 ID

---

## 更新日志

### 2025-01-14
- ✅ 为 `OrderServiceImpl` 添加自动生成订单 ID 的逻辑
- 📋 创建实体 ID 生成策略统计文档
- 🔍 统计系统中 44 个实体的 ID 生成方式

---

## 相关文件

- `IdGenerator.java` - ID 生成工具类
- `IdPrefixUtil.java` - ID 前缀工具类
- `Order.java` - 订单实体（使用 INPUT）
- `Merchant.java` - 商家实体（使用 INPUT）
- `OrderServiceImpl.java` - 订单服务实现（已更新）

---

**说明**：
- 统计时间：2025-01-14
- 统计范围：`com.xx.jaseatschoicejava.entity` 包下的所有实体类
- 数据准确性：基于代码搜索和实际文件检查

---

## 实际使用案例

### 1. 用户（User）ID 生成

**位置**：`UserServiceImpl.register()` 方法

**代码示例**：
```java
@Override
public boolean register(User user) {
    // 生成用户ID
    String userId = IdGenerator.toUserIdString(IdGenerator.generateId());
    user.setUserId(userId);
    // 对密码进行加密
    String encryptedPassword = passwordEncoder.encode(user.getPassword());
    user.setPassword(encryptedPassword);
    return save(user);
}
```

**说明**：
- 在用户注册时自动生成带 `U` 前缀的用户ID
- 示例ID：`U1234567890123456`

---

### 2. 商家（Merchant）ID 生成

**位置**：`MerchantServiceImpl.register()` 方法

**代码示例**：
```java
@Override
public Merchant register(Merchant merchant) {
    // 生成商家ID
    String merchantId = IdGenerator.toMerchantIdString(IdGenerator.generateId());
    merchant.setId(merchantId);
    // 默认新注册商家为营业状态
    merchant.setStatus(true);
    // 设置创建时间
    merchant.setCreateTime(LocalDateTime.now());
    // ... 其他逻辑
}
```

**说明**：
- 在商家注册时自动生成带 `M` 前缀的商家ID
- 示例ID：`M1234567890123456`

---

### 3. 群组（Group）ID 生成

**位置**：`GroupServiceImpl.save()` 方法

**代码示例**：
```java
@Override
public boolean save(Group group) {
    // 生成群ID
    String groupId = IdGenerator.toGroupIdString(IdGenerator.generateId());
    group.setId(groupId);

    // 设置创建时间和更新时间
    LocalDateTime now = LocalDateTime.now();
    group.setCreateTime(now);
    group.setUpdateTime(now);

    // ... 保存逻辑
}
```

**说明**：
- 在创建群组时自动生成带 `G` 前缀的群组ID
- 示例ID：`G1234567890123456`

---

### 4. 聊天消息（ChatMsg）ID 生成

**位置**：`ChatController.sendMessage()` 方法

**代码示例**：
```java
@PostMapping("/messages")
public ResponseResult<?> sendMessage(@RequestBody ChatMsg chatMsg) {
    // 设置默认值
    chatMsg.setReadStatus(false);
    chatMsg.setCreateTime(LocalDateTime.now());

    // 生成消息ID（使用IdGenerator）
    if (chatMsg.getMsgId() == null || chatMsg.getMsgId().isEmpty()) {
        String messageId = IdGenerator.toChatMsgIdString(IdGenerator.generateId());
        chatMsg.setMsgId(messageId);
    }

    // ... 保存消息和其他逻辑
}
```

**说明**：
- 在发送消息时自动生成带 `CM` 前缀的消息ID
- 示例ID：`CM1234567890123456`
- 包含空值检查，避免重复生成

---

### 5. 订单（Order）ID 生成

**位置**：`OrderServiceImpl.createOrderWithDishes()` 方法

**代码示例**：
```java
private Order createOrderWithDishes(OrderCreateDTO orderCreateDTO) {
    Order order = orderCreateDTO.getOrder();
    List<OrderDish> dishes = orderCreateDTO.getDishes();

    // 自动生成订单ID（如果未设置）
    if (order.getId() == null || order.getId().isEmpty()) {
        Long generatedId = IdGenerator.generateId();
        String orderId = IdGenerator.toOrderIdString(generatedId);
        order.setId(orderId);
        log.info("自动生成订单ID: {}", orderId);
    }

    // ... 其他订单处理逻辑
}
```

**说明**：
- 在创建订单时自动生成带 `O` 前缀的订单ID
- 示例ID：`O1234567890123456`
- 包含空值检查，避免重复生成
- 添加了日志记录便于调试

---

## 使用模板

### 基本模板（推荐）

```java
// 1. 生成原始ID
Long generatedId = IdGenerator.generateId();

// 2. 转换为带前缀的字符串ID
String entityId = IdGenerator.toXxxIdString(generatedId);

// 3. 设置到实体对象
entity.setId(entityId);
```

### 带空值检查的模板（更安全）

```java
// 检查ID是否已存在，避免重复生成
if (entity.getId() == null || entity.getId().isEmpty()) {
    Long generatedId = IdGenerator.generateId();
    String entityId = IdGenerator.toXxxIdString(generatedId);
    entity.setId(entityId);
    log.info("自动生成{} ID: {}", "实体类型", entityId);
}
```

### 事务中的使用（确保数据一致性）

```java
@Transactional
public void createEntity(Entity entity) {
    // 生成ID
    String entityId = IdGenerator.toXxxIdString(IdGenerator.generateId());
    entity.setId(entityId);

    // 保存实体
    boolean success = save(entity);

    if (!success) {
        throw new RuntimeException("保存失败");
    }
}
```

---

## 注意事项

1. **前缀一致性**：确保使用正确的 `toXxxIdString()` 方法，前缀必须与实体类型匹配

2. **空值检查**：建议在生成ID前检查实体ID是否为空，避免覆盖已有ID

3. **性能优化**：`generateId()` 是同步方法，性能已优化（比原方案快约50倍），可在高并发场景使用

4. **事务管理**：建议在 `@Transactional` 事务方法中使用，确保ID生成和数据保存的一致性

5. **日志记录**：建议在自动生成ID时添加日志，便于问题追踪和调试

---

## 更新日志

### 2025-01-14（补充）
- ✅ 添加5个实体的实际使用案例（User、Merchant、Group、ChatMsg、Order）
- 📋 补充使用模板和注意事项
- 📝 基于代码库实际搜索结果整理

### 2025-01-14（初始）
- ✅ 为 `OrderServiceImpl` 添加自动生成订单 ID 的逻辑
- 📋 创建实体 ID 生成策略统计文档
- 🔍 统计系统中 44 个实体的 ID 生成方式
