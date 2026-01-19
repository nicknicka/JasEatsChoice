# WebSocket 连接问题修复指南

## 问题描述
前端在尝试连接 WebSocket 时遇到 **401 未授权错误**，导致连接失败。

## 根本原因

### 1. **WebSocket 端点配置错误**
- **原配置**: `/ws`
- **正确配置**: `/ws/chat`
- **影响**: 前端连接的端点与后端不匹配，导致握手失败

### 2. **认证流程问题**
- 后端在 WebSocket 握手阶段通过 URL 参数验证 token
- 前端原代码在连接建立后才发送认证消息（多余且无效）

### 3. **Token 可能无效或过期**
- localStorage 中的 token 可能已过期
- 用户可能未正确登录

## 已修复的问题

### ✅ 修复 1: 更新 WebSocket 端点配置
**文件**: `/src/renderer/src/constants/wsConstants.js`

```javascript
// 修改前
ENDPOINT: '/ws'

// 修改后
ENDPOINT: '/ws/chat'
```

### ✅ 修复 2: 在 URL 中添加认证参数
**文件**: `/src/renderer/src/views/user/HomeContent.vue`

```javascript
// 修改前：URL 中没有认证参数
const wsUrl = `${WS_CONFIG.URL}${WS_CONFIG.ENDPOINT}`

// 修改后：URL 中包含 userId 和 token
const wsUrl = `${WS_CONFIG.URL}${WS_CONFIG.ENDPOINT}?userId=${userId}&token=${token}`
```

### ✅ 修复 3: 移除重复的认证消息
**文件**: `/src/renderer/src/views/user/HomeContent.vue`

```javascript
// 删除了握手后发送认证消息的代码
// 因为后端已经在握手阶段通过 URL 参数验证了 token
```

### ✅ 修复 4: 增强错误处理和日志
添加了详细的错误日志，包括：
- Token 检查
- 用户 ID 检查
- 连接状态日志
- 详细的错误码说明

## 验证修复结果

### 1. 检查后端服务是否运行
```bash
# 检查 Netty 服务器是否在 11277 端口运行
lsof -i :11277
```

### 2. 检查用户登录状态
打开浏览器控制台，执行：
```javascript
// 检查 token 是否存在
console.log('Token:', localStorage.getItem('auth_token'))

// 检查用户 ID
console.log('UserId:', localStorage.getItem('auth_userId'))

// 检查完整的认证信息
console.log('Auth Info:', {
  token: localStorage.getItem('auth_token'),
  userId: localStorage.getItem('auth_userId'),
  phone: localStorage.getItem('auth_phone'),
  merchantId: localStorage.getItem('auth_merchantId'),
  currentRole: localStorage.getItem('auth_currentRole')
})
```

### 3. 查看控制台日志
刷新页面后，应该看到以下日志序列：

```
🔌 Connecting to WebSocket server: ws://localhost:11277/ws/chat?userId=1&token=xxx
📝 当前用户: 1 Token存在: true
✅ WebSocket 连接已建立
🔐 认证成功，userId: 1
```

如果看到错误，日志会显示具体原因。

## 如果仍然无法连接

### 情况 1: Token 无效或过期
**解决方案**: 重新登录
1. 退出登录
2. 重新登录获取新 token
3. 刷新页面

### 情况 2: 后端服务未启动
**解决方案**: 启动后端 Netty 服务器
```bash
# 在项目根目录执行
mvn spring-boot:run
```

### 情况 3: Token 格式问题
**检查**:
- Token 应该是有效的 JWT 格式（三段，用 `.` 分隔）
- Token 不应该被 URL 编码

**临时解决方案** - 手动设置 Token：
```javascript
// 在浏览器控制台执行（需要先获取有效的 token）
localStorage.setItem('auth_token', 'your_valid_token_here')
localStorage.setItem('auth_userId', '1')
// 刷新页面
location.reload()
```

## 调试技巧

### 查看 WebSocket 握手详情
1. 打开浏览器开发者工具
2. 切换到 **Network** 标签
3. 筛选 **WS** (WebSocket)
4. 查看握手请求的详细信息：
   - 请求 URL 应该包含 `userId` 和 `token` 参数
   - 响应状态应该是 **101 Switching Protocols**

### 查看后端日志
后端会输出详细的认证日志：
```
WebSocket握手请求URI: /ws/chat?userId=1&token=xxx
WebSocket认证成功: userId=1
```

如果看到错误：
```
WebSocket握手失败：缺少token参数
WebSocket握手失败：无效的token
```

说明需要重新登录获取有效 token。

## 后端认证流程说明

后端的 `WebSocketAuthHandler` 在握手阶段执行以下验证：

1. **提取参数**: 从 URL 中提取 `userId` 和 `token`
2. **验证 Token**: 使用 `JwtUtil.extractUserId(token)` 验证 token 有效性
3. **匹配检查**: 如果提供了 userId，检查是否与 token 中的 userId 匹配
4. **存储信息**: 将 userId 存储到 Channel 属性中
5. **重置 URI**: 将 URI 重置为 `/ws/chat`（移除查询参数）

只有验证通过后，才会升级到 WebSocket 协议。

## 相关文件

### 前端
- `/src/renderer/src/constants/wsConstants.js` - WebSocket 配置
- `/src/renderer/src/views/user/HomeContent.vue` - 首页 WebSocket 连接
- `/src/renderer/src/composables/useWebSocketChat.js` - 聊天 WebSocket 连接
- `/src/renderer/src/store/authStore.ts` - 认证状态管理

### 后端
- `/src/main/java/com/xx/jaseatschoicejava/netty/WebSocketAuthHandler.java` - 认证处理器
- `/src/main/java/com/xx/jaseatschoicejava/netty/NettyServer.java` - Netty 服务器
- `/src/main/java/com/xx/jaseatschoicejava/util/JwtUtil.java` - JWT 工具类

## 预防措施

### 1. Token 过期处理
在请求拦截器中检测 401 错误，自动跳转登录页：
```javascript
// api.js 响应拦截器
if (error.response && error.response.status === 401) {
  // 清空认证信息
  authStore.clearAuth()
  // 跳转登录页
  router.push('/login')
}
```

### 2. 定期刷新 Token
实现 Token 自动刷新机制，避免频繁需要重新登录。

### 3. 连接状态监控
添加 WebSocket 连接状态指示器，让用户了解实时连接状态。

## 总结

主要问题是 **WebSocket 端点配置错误** 和 **认证参数未通过 URL 传递**。通过修复这两个问题，WebSocket 连接应该能够正常建立。

如果问题仍然存在，请检查：
1. ✅ 后端服务是否正常运行
2. ✅ 用户是否已登录（token 是否有效）
3. ✅ 浏览器控制台的详细错误日志
4. ✅ 后端日志中的认证信息

---

**修复日期**: 2025-01-19
**修复人**: Claude Code Assistant
