# Session ID 生成方案说明

## 📋 方案对比

### 方案1：MD5哈希（推荐使用）⭐

**格式**：`S` + 32位MD5哈希
**示例**：`Sa1b2c3d4e5f6...7890abcdef1234`

**优势**：
- ✅ 快速高效（性能最好）
- ✅ 固定33字符长度（含前缀S）
- ✅ 不可逆（保护用户隐私）
- ✅ 相同输入总是生成相同输出
- ✅ 便于数据库索引和查询

**使用场景**：
- 生产环境推荐
- 对性能有要求
- 需要固定长度

---

### 方案2：SHA-256哈希（最安全）

**格式**：`S` + 64位SHA-256哈希
**示例**：`Sa1b2c3d4e5f6...7890abcdef...1234567890abcdef`

**优势**：
- ✅ 最安全的哈希算法
- ✅ 不可逆（保护用户隐私）
- ✅ 固定65字符长度（含前缀S）
- ✅ 抗碰撞性最强

**劣势**：
- ⚠️ 存储空间较大
- ⚠️ 性能略低于MD5

**使用场景**：
- 对安全性要求极高
- 可以接受较长的ID
- 金融级应用

---

### 方案3：UUID命名空间（标准格式）

**格式**：`S` + UUID（无连字符）
**示例**：`Sa1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6`

**优势**：
- ✅ 标准UUID格式
- ✅ 可扩展性好
- ✅ 固定33字符长度（含前缀S）

**使用场景**：
- 需要符合UUID标准
- 与其他系统集成
- 需要全局唯一性

---

## 🎯 默认使用方案

**当前默认**：方案1（MD5哈希）

```java
// 自动使用MD5哈希方案
String sessionId = ChatSessionIdGenerator.generateSessionId(
    "single",  // 消息类型
    "user1",    // 发送者ID
    "user2"     // 接收者ID
);
// 结果：Sa1b2c3d4e5f67890abcdef1234567890（33字符）
```

---

## 💡 使用示例

### 示例1：生成单聊会话ID

```java
// 使用MD5哈希（推荐）
String sessionId1 = ChatSessionIdGenerator.generateSingleChatSessionId(
    "3384650106421960",
    "1000000000000000"
);
// 结果：S8f7d6e5c4b3a209876543210fedcba98（示例）

// 验证：同一对用户总是生成相同的ID
String sessionId2 = ChatSessionIdGenerator.generateSingleChatSessionId(
    "1000000000000000",
    "3384650106421960"
);
// 结果：S8f7d6e5c4b3a209876543210fedcba98（与sessionId1相同）
```

### 示例2：生成群聊会话ID

```java
String sessionId = ChatSessionIdGenerator.getGroupChatSessionId(
    "G1234567890123456"
);
// 结果：G1234567890123456（直接使用群ID）
```

### 示例3：使用SHA-256哈希

```java
String sessionId = ChatSessionIdGenerator.generateSingleChatSessionIdWithSHA256(
    "user1",
    "user2"
);
// 结果：Sa1b2c3d4e5f6...7890abcdef...1234567890abcdef（65字符）
```

### 示例4：使用UUID命名空间

```java
String sessionId = ChatSessionIdGenerator.generateSingleChatSessionIdWithUUID(
    "user1",
    "user2"
);
// 结果：Sa1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6（33字符）
```

---

## 🔄 数据迁移

### 旧格式 → 新格式

**旧格式示例**：
```
user1_user2 (简单拼接)
```

**新格式示例**（MD5哈希）：
```
Sa1b2c3d4e5f67890abcdef1234567890
```

### 迁移方法

**方法1：自动升级（推荐）**

```java
// 在ChatController的sendMessage方法中已经自动处理
// 新消息会自动使用新的哈希格式
String sessionId = ChatSessionIdGenerator.generateSessionId(
    msgType, fromId, toId
);
chatMsg.setSessionId(sessionId);
```

**方法2：批量升级旧数据**

```java
// 创建迁移服务
@Service
public class SessionIdUpgradeService {

    @Autowired
    private ChatMsgService chatMsgService;

    @Transactional
    public void upgradeExistingSessionIds() {
        // 1. 查询所有旧格式的消息
        LambdaQueryWrapper<ChatMsg> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMsg::getMsgType, "single")
               .likeRight(ChatMsg::getSessionId, "1")  // 旧格式
               .notLikeRight(ChatMsg::getSessionId, "S");  // 不以S开头

        List<ChatMsg> messages = chatMsgService.list(wrapper);

        System.out.println("找到 " + messages.size() + " 条需要升级的消息");

        // 2. 逐条升级
        int count = 0;
        for (ChatMsg msg : messages) {
            String oldSessionId = msg.getSessionId();
            String newSessionId = ChatSessionIdGenerator.upgradeSessionId(oldSessionId);

            if (!oldSessionId.equals(newSessionId)) {
                msg.setSessionId(newSessionId);
                chatMsgService.updateById(msg);
                count++;

                if (count % 100 == 0) {
                    System.out.println("已升级 " + count + " 条消息...");
                }
            }
        }

        System.out.println("升级完成！共升级 " + count + " 条消息");
    }
}
```

**方法3：手动升级（单条）**

```java
// 升级单个session_id
String oldId = "1000000000000000_3384650106421960";
String newId = ChatSessionIdGenerator.upgradeSessionId(oldId);

// 更新数据库
UPDATE t_chat_msg SET session_id = 'S[new_hash]' WHERE session_id = oldId;
```

---

## 🔒 安全性说明

### 为什么使用哈希？

1. **保护用户隐私**
   - 旧格式：`user1_user2` 可以直接看出用户ID
   - 新格式：`Sa1b2c3...` 无法反推出用户ID

2. **防止暴力破解**
   - 添加了盐值（SALT）：`JasEatsChoice_Chat_2026`
   - 即使知道用户ID，也无法直接计算session_id

3. **固定长度**
   - 便于数据库设计和索引
   - 统一的格式便于处理

### 安全等级

| 方案 | 安全等级 | 说明 |
|------|---------|------|
| 旧格式（简单拼接） | ⭐ | 完全暴露用户ID |
| MD5哈希 | ⭐⭐⭐⭐ | 不可逆，带盐值 |
| SHA-256哈希 | ⭐⭐⭐⭐⭐ | 最安全，抗碰撞 |
| UUID | ⭐⭐⭐⭐ | 标准格式，可预测 |

---

## 📊 性能对比

| 操作 | 旧格式 | MD5 | SHA-256 | UUID |
|------|--------|-----|---------|------|
| 生成速度 | ⚡⚡⚡⚡⚡ | ⚡⚡⚡⚡ | ⚡⚡⚡ | ⚡⚡⚡⚡ |
| 存储空间 | 小 | 小 | 大 | 中 |
| 查询性能 | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| 索引效率 | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |

---

## 🚀 推荐配置

### 生产环境（推荐）

```java
// 使用MD5哈希方案（性能和安全的最佳平衡）
String sessionId = ChatSessionIdGenerator.generateSessionId(
    msgType, fromId, toId
);
```

### 高安全场景

```java
// 使用SHA-256哈希方案
String sessionId = ChatSessionIdGenerator.generateSingleChatSessionIdWithSHA256(
    fromId, toId
);
```

### 跨系统集成

```java
// 使用UUID命名空间方案
String sessionId = ChatSessionIdGenerator.generateSingleChatSessionIdWithUUID(
    fromId, toId
);
```

---

## 📝 注意事项

1. **向后兼容**
   - 旧格式的session_id仍然可以正常工作
   - 新消息自动使用新格式
   - 建议逐步迁移旧数据

2. **盐值管理**
   - 当前盐值：`JasEatsChoice_Chat_2026`
   - 可以修改盐值以增强安全性
   - 修改盐值后需要重新生成所有session_id

3. **前缀规则**
   - `S` 前缀：单聊会话
   - `G` 前缀：群聊会话（直接使用群ID）

---

## 🎯 快速开始

1. **代码已完成** ✅
   - `ChatSessionIdGenerator.java` 已更新
   - `ChatController.java` 已配置自动使用新方法

2. **新消息自动使用新格式** ✅
   - 发送消息时自动生成MD5哈希格式的session_id

3. **旧数据需要迁移** ⚠️
   - 使用 `SessionIdUpgradeService` 批量迁移
   - 或保持旧格式继续使用（向后兼容）

---

**下一步**：重新编译并重启服务，新消息将自动使用新的哈希格式！
