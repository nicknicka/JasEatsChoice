package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.ChatMsg;
import com.xx.jaseatschoicejava.entity.ChatSession;
import com.xx.jaseatschoicejava.service.ChatMsgService;
import com.xx.jaseatschoicejava.service.ChatSessionService;
import com.xx.jaseatschoicejava.service.UserService;
import com.xx.jaseatschoicejava.service.GroupService;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.entity.Group;
import com.xx.jaseatschoicejava.util.ChatSessionIdGenerator;
import com.xx.jaseatschoicejava.util.FileUploadUtil;
import com.xx.jaseatschoicejava.util.IdGenerator;
import com.xx.jaseatschoicejava.websocket.WebSocketMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 聊天消息控制器
 */
@Api(tags = "聊天消息模块")
@RestController
@RequestMapping("/v1/chat")
public class ChatController {

    @Autowired
    private ChatMsgService chatMsgService;

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private WebSocketMessageService webSocketMessageService;

    @Autowired
    private com.xx.jaseatschoicejava.service.ContactService contactService;

    @Value("${file.upload.url-prefix}")
    private String fileUrlPrefix;

    @Value("${file.upload.server-url}")
    private String serverUrl;

    /**
     * 上传图片
     */
    @ApiOperation("上传聊天图片")
    @PostMapping("/upload-image")
    public ResponseResult<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件
            if (file.isEmpty()) {
                return ResponseResult.fail("400", "文件不能为空");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseResult.fail("400", "只支持图片文件");
            }

            // 验证文件大小（5MB）
            long maxSize = 5 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                return ResponseResult.fail("400", "图片大小不能超过5MB");
            }

            // 上传文件，返回相对URL路径（如：chat/abc123.jpg）
            String relativeUrl = FileUploadUtil.uploadImage(file, "chat");

            // 构建返回数据
            Map<String, Object> result = new HashMap<>();
            // fileUrl保存相对路径，用于存储到数据库
            result.put("fileUrl", relativeUrl);
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", file.getSize());
            result.put("fileType", contentType);
            // 构建完整的URL（包含服务器地址和前缀）
            // 例如：http://localhost:8080 + /api/uploads/ + chat/abc123.jpg
            String fullUrl = serverUrl + fileUrlPrefix + relativeUrl;
            result.put("fullUrl", fullUrl);

            return ResponseResult.success(result);
        } catch (Exception e) {
            return ResponseResult.fail("500", "图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件
     */
    @ApiOperation("上传聊天文件")
    @PostMapping("/upload-file")
    public ResponseResult<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件
            if (file.isEmpty()) {
                return ResponseResult.fail("400", "文件不能为空");
            }

            // 验证文件大小（10MB）
            long maxSize = 10 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                return ResponseResult.fail("400", "文件大小不能超过10MB");
            }

            // 上传文件，返回相对URL路径（如：chat/abc123.pdf）
            String relativeUrl = FileUploadUtil.uploadFile(file, "chat");

            // 构建返回数据
            Map<String, Object> result = new HashMap<>();
            // fileUrl保存相对路径，用于存储到数据库
            result.put("fileUrl", relativeUrl);
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", file.getSize());
            result.put("fileType", file.getContentType());
            // 构建完整的URL（包含服务器地址和前缀）
            String fullUrl = serverUrl + fileUrlPrefix + relativeUrl;
            result.put("fullUrl", fullUrl);

            return ResponseResult.success(result);
        } catch (Exception e) {
            return ResponseResult.fail("500", "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取聊天记录（分页）
     * @param sessionId 会话ID，可以是：
     *                  1. 单聊：两个用户ID用"_"拼接，如"user1_user2"
     *                  2. 群聊：群组ID，如"group123"
     */
    @ApiOperation("获取聊天记录")
    @GetMapping("/{sessionId}/messages")
    public ResponseResult<?> getChatMessages(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        // ========== 日志：开始加载聊天消息 ==========
        System.out.println("📡 [Chat] 开始加载聊天消息");
        System.out.println("  - sessionId: " + sessionId);
        System.out.println("  - page: " + page);
        System.out.println("  - size: " + size);
        System.out.println("  - 会话类型: " + (sessionId.contains("_") ? "单聊" : "群聊"));

        Page<ChatMsg> chatMsgPage = new Page<>(page, size);
        LambdaQueryWrapper<ChatMsg> queryWrapper = new LambdaQueryWrapper<>();

        // 判断是单聊还是群聊
        queryWrapper.eq(ChatMsg::getSessionId, sessionId);

        // 按时间倒序排序(最新的在前)
        queryWrapper.orderByDesc(ChatMsg::getCreateTime);

        // 查询结果
        long startTime = System.currentTimeMillis();
        Page<ChatMsg> result = chatMsgService.page(chatMsgPage, queryWrapper);
        long queryTime = System.currentTimeMillis() - startTime;

        // 将消息按时间正序排列(旧的在前,方便前端显示)
        java.util.Collections.reverse(result.getRecords());

        // ========== 日志：查询结果 ==========
//        System.out.println("✅ [Chat] 查询完成");
//        System.out.println("  - 查询耗时: " + queryTime + "ms");
//        System.out.println("  - 总消息数: " + result.getTotal());
//        System.out.println("  - 当前页消息数: " + result.getRecords().size());
//        System.out.println("  - 总页数: " + result.getPages());
//        System.out.println("  - 当前页: " + result.getCurrent());

//        // 打印前3条消息的摘要
//        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
//            System.out.println("  - 消息摘要(前3条):");
//            int printCount = Math.min(3, result.getRecords().size());
//            for (int i = 0; i < printCount; i++) {
//                ChatMsg msg = result.getRecords().get(i);
//                System.out.println("    [" + (i + 1) + "] msgId=" + msg.getMsgId() +
//                        ", fromId=" + msg.getFromId() +
//                        ", toId=" + msg.getToId() +
//                        ", type=" + msg.getMsgType() +
//                        ", content=" + (msg.getContent() != null && msg.getContent().length() > 20
//                                ? msg.getContent().substring(0, 20) + "..." : msg.getContent()));
//            }
//        }

        // 返回符合前端期望的格式
        Map<String, Object> responseData = new java.util.HashMap<>();
        responseData.put("records", result.getRecords());
        responseData.put("total", result.getTotal());
        responseData.put("current", result.getCurrent());
        responseData.put("pages", result.getPages());
        responseData.put("size", result.getSize());

        System.out.println("📤 [Chat] 返回数据给前端");
        return ResponseResult.success(responseData);
    }

    /**
     * 发送消息
     */
    @ApiOperation("发送消息")
    @PostMapping("/messages")
    public ResponseResult<?> sendMessage(@RequestBody ChatMsg chatMsg) {
        // 设置默认值
        chatMsg.setReadStatus(false);  // 0-未读
        chatMsg.setCreateTime(LocalDateTime.now());

        // 🔍 调试日志：检查接收到的消息数据
        System.out.println("📨 [Chat] 接收到消息");
        System.out.println("  - fromId: " + chatMsg.getFromId());
        System.out.println("  - toId: " + chatMsg.getToId());
        System.out.println("  - sessionType: " + chatMsg.getSessionType());
        System.out.println("  - msgType: " + chatMsg.getMsgType());
        System.out.println("  - content: " + chatMsg.getContent());

        // ⭐ 生成消息ID（使用IdGenerator）
        if (chatMsg.getMsgId() == null || chatMsg.getMsgId().isEmpty()) {
            String messageId = IdGenerator.toChatMsgIdString(IdGenerator.generateId());
            chatMsg.setMsgId(messageId);
        }

        // ⭐ 生成并设置 session_id（统一使用双方ID生成哈希）
        String sessionId;
        if ("group".equals(chatMsg.getSessionType())) {
            // 群聊：使用 ChatSessionIdGenerator 生成 S 开头的会话ID
            System.out.println("  - 群聊消息，使用 toId 生成 sessionId: " + chatMsg.getToId());
            sessionId = ChatSessionIdGenerator.getGroupChatSessionId(chatMsg.getToId());
            System.out.println("  - 生成的 sessionId: " + sessionId);
        } else {
            // 单聊：使用双方 userId 生成哈希 sessionId（与会话创建逻辑保持一致）
            System.out.println("  - 单聊消息，使用 fromId 和 toId 生成 sessionId");
            sessionId = ChatSessionIdGenerator.generateSingleChatSessionId(
                chatMsg.getFromId(),
                chatMsg.getToId()
            );
            System.out.println("  - fromId: " + chatMsg.getFromId() + ", toId: " + chatMsg.getToId());
            System.out.println("  - 生成的 sessionId: " + sessionId);
        }
        chatMsg.setSessionId(sessionId);

        // 保存消息
        boolean success = chatMsgService.save(chatMsg);
        if (success) {
            // ========== 创建或更新会话记录 ==========

            // 1. 为发送者创建或更新会话
            createOrUpdateSessionForUser(
                chatMsg.getFromId(),
                chatMsg.getToId(),
                chatMsg.getSessionType(),
                chatMsg.getContent(),
                chatMsg.getCreateTime()
            );

            // 2. 为接收者创建或更新会话（如果是群聊，所有成员都会收到消息）
            if ("group".equals(chatMsg.getSessionType())) {
                // 群聊：更新群组会话
                updateSessionForReceiver(
                    chatMsg.getToId(),  // 群ID
                    chatMsg.getFromId(),  // 发送者ID
                    chatMsg.getSessionType(),
                    chatMsg.getContent(),
                    chatMsg.getCreateTime(),
                    true  // 是群聊
                );
            } else {
                // 单聊：为接收者创建会话
                createOrUpdateSessionForUser(
                    chatMsg.getToId(),
                    chatMsg.getFromId(),
                    chatMsg.getSessionType(),
                    chatMsg.getContent(),
                    chatMsg.getCreateTime()
                );
            }

            // ========== ⭐ WebSocket实时推送消息给接收方 ==========
            try {
                if ("group".equals(chatMsg.getSessionType())) {
                    // ⭐ 群聊：查询所有群成员，逐个推送给在线成员
                    String groupId = chatMsg.getToId();
                    System.out.println("📡 [WebSocket] 开始推送群聊消息，群组ID: " + groupId);

                    // 查询群成员列表
                    java.util.List<com.xx.jaseatschoicejava.entity.Contact> groupMembers =
                        contactService.lambdaQuery()
                            .eq(com.xx.jaseatschoicejava.entity.Contact::getTargetId, groupId)
                            .eq(com.xx.jaseatschoicejava.entity.Contact::getRelationType, "group")
                            .eq(com.xx.jaseatschoicejava.entity.Contact::getStatus, "normal")
                            .list();

                    System.out.println("📋 [WebSocket] 群成员数量: " + groupMembers.size());

                    // 推送给每个在线的群成员（不包括发送者）
                    int successCount = 0;
                    for (com.xx.jaseatschoicejava.entity.Contact member : groupMembers) {
                        String memberUserId = member.getUserId();

                        // 不推送给发送者自己
                        if (memberUserId.equals(chatMsg.getFromId())) {
                            System.out.println("  ⊗ 跳过发送者: " + memberUserId);
                            continue;
                        }

                        try {
                            webSocketMessageService.pushChatMessageToUser(memberUserId, chatMsg);
                            successCount++;
                            System.out.println("  ✅ 推送给成员: " + memberUserId);
                        } catch (Exception e) {
                            System.err.println("  ❌ 推送给成员 " + memberUserId + " 失败: " + e.getMessage());
                        }
                    }

                    System.out.println("📡 [WebSocket] 群聊消息推送完成，成功: " + successCount + "/" + groupMembers.size());
                } else {
                    // 单聊：推送给接收方
                    webSocketMessageService.pushChatMessageToUser(chatMsg.getToId(), chatMsg);
                    System.out.println("📡 [WebSocket] 消息已推送给用户: " + chatMsg.getToId());
                }
            } catch (Exception e) {
                System.err.println("❌ [WebSocket] 推送消息失败: " + e.getMessage());
                e.printStackTrace();
                // 推送失败不影响消息保存，只记录错误
            }

            return ResponseResult.success(chatMsg);
        } else {
            return ResponseResult.fail("500", "消息发送失败");
        }
    }

    /**
     * 为用户创建或更新会话
     */
    private void createOrUpdateSessionForUser(
            String userId,
            String otherId,
            String sessionType,
            String content,
            LocalDateTime messageTime) {

        // ⭐ 生成sessionId（统一使用双方ID生成哈希）
        String sessionId;
        if ("group".equals(sessionType)) {
            // 群聊：使用 ChatSessionIdGenerator 生成 S 开头的会话ID
            sessionId = ChatSessionIdGenerator.getGroupChatSessionId(otherId);
        } else {
            // 单聊：使用双方 userId 生成哈希 sessionId（与会话创建逻辑保持一致）
            sessionId = ChatSessionIdGenerator.generateSingleChatSessionId(userId, otherId);
        }

        // 查找现有会话
        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSession::getUserId, userId);
        queryWrapper.eq(ChatSession::getSessionId, sessionId);

        ChatSession session = chatSessionService.getOne(queryWrapper);

        if (session == null) {
            // 创建新会话
            session = new ChatSession();
            session.setUserId(userId);
            session.setSessionId(sessionId);
            session.setSessionType(sessionType);
            session.setSessionName(getSessionName(sessionType, otherId));
            session.setAvatar("group".equals(sessionType) ? "👥" : "💬");
            session.setMemberCount("group".equals(sessionType) ? 0 : null);
            session.setUnreadCount(0);
            session.setPinned(0);  // 0-未置顶
            session.setCreateTime(LocalDateTime.now());

            // ⭐ 设置 targetId（仅单聊）
            if ("single".equals(sessionType)) {
                session.setTargetId(otherId);
            }
        }

        // 更新会话信息
        session.setLastMessage(content);
        session.setLastMessageTime(messageTime);
        session.setUpdateTime(LocalDateTime.now());

        // 保存会话
        chatSessionService.saveOrUpdate(session);
    }

    /**
     * 为接收者更新会话（增加未读数）
     */
    private void updateSessionForReceiver(
            String receiverId,
            String senderId,
            String sessionType,
            String content,
            LocalDateTime messageTime,
            boolean isGroup) {

        // ⭐ 生成sessionId（统一使用双方ID生成哈希）
        String sessionId;
        if (isGroup) {
            // 群聊：使用 ChatSessionIdGenerator 生成 S 开头的会话ID
            sessionId = ChatSessionIdGenerator.getGroupChatSessionId(receiverId);
        } else {
            // 单聊：使用双方 userId 生成哈希 sessionId（与会话创建逻辑保持一致）
            sessionId = ChatSessionIdGenerator.generateSingleChatSessionId(receiverId, senderId);
        }

        // 查找现有会话
        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSession::getUserId, receiverId);
        queryWrapper.eq(ChatSession::getSessionId, sessionId);

        ChatSession session = chatSessionService.getOne(queryWrapper);

        if (session == null) {
            // 创建新会话
            session = new ChatSession();
            session.setUserId(receiverId);
            session.setSessionId(sessionId);
            session.setSessionType(sessionType);
            session.setSessionName(getSessionName(sessionType, isGroup ? receiverId : senderId));
            session.setAvatar("group".equals(sessionType) ? "👥" : "💬");
            session.setMemberCount(isGroup ? 0 : null);
            session.setUnreadCount(1);  // 新会话，初始未读数为1
            session.setPinned(0);  // 0-未置顶
            session.setCreateTime(LocalDateTime.now());
            session.setLastMessage(content);
            session.setLastMessageTime(messageTime);
            session.setUpdateTime(LocalDateTime.now());

            // ⭐ 设置 targetId（仅单聊）
            if (!isGroup) {
                session.setTargetId(senderId);  // 接收者的会话中，targetId 是发送者
            }

            chatSessionService.save(session);
        } else {
            // 更新现有会话，增加未读数
            session.setLastMessage(content);
            session.setLastMessageTime(messageTime);
            session.setUnreadCount((session.getUnreadCount() != null ? session.getUnreadCount() : 0) + 1);
            session.setUpdateTime(LocalDateTime.now());
            chatSessionService.updateById(session);
        }
    }

    /**
     * 构建会话ID（单聊）
     */
    private String buildSessionId(String userId1, String userId2) {
        // 按字典序排列，确保唯一性
        java.util.List<String> ids = new java.util.ArrayList<>();
        ids.add(userId1);
        ids.add(userId2);
        java.util.Collections.sort(ids);
        return ids.get(0) + "_" + ids.get(1);
    }

    /**
     * 获取会话名称（查询真实名称）
     */
    private String getSessionName(String sessionType, String otherId) {
        if ("group".equals(sessionType)) {
            // 群聊：查询群信息获取群名
            try {
                Group group = groupService.getById(otherId);
                if (group != null && group.getGroupName() != null && !group.getGroupName().isEmpty()) {
                    return group.getGroupName();
                }
            } catch (Exception e) {
                System.err.println("查询群信息失败: " + e.getMessage());
            }
            // 查询失败时返回默认名称
            return "\u7fa4\u804a " + otherId;
        } else {
            // 单聊：查询用户信息获取昵称
            try {
                User user = userService.getById(otherId);
                if (user != null && user.getNickname() != null && !user.getNickname().isEmpty()) {
                    return user.getNickname();
                }
            } catch (Exception e) {
                System.err.println("查询用户信息失败: " + e.getMessage());
            }
            // 查询失败时返回默认名称
            return "\u7528\u6237 " + otherId;
        }
    }

    /**
     * 标记消息已读
     */
    @ApiOperation("标记消息已读")
    @PutMapping("/messages/{messageId}/read")
    public ResponseResult<?> markMessageAsRead(@PathVariable String messageId) {
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setMsgId(messageId);
        chatMsg.setReadStatus(true);  // 1-已读

        boolean success = chatMsgService.updateById(chatMsg);
        if (success) {
            return ResponseResult.success("消息标记已读成功");
        } else {
            return ResponseResult.fail("500", "消息标记已读失败");
        }
    }

    /**
     * 撤回消息
     */
    @ApiOperation("撤回消息")
    @PostMapping("/messages/{messageId}/recall")
    public ResponseResult<?> recallMessage(
            @PathVariable String messageId,
            @RequestBody Map<String, String> request) {

        // 获取消息
        ChatMsg chatMsg = chatMsgService.getById(messageId);
        if (chatMsg == null) {
            return ResponseResult.fail("404", "消息不存在");
        }

        // 验证用户身份（只有发送者才能撤回）
        String userId = request.get("userId");
        if (!chatMsg.getFromId().equals(userId)) {
            return ResponseResult.fail("403", "无权撤回此消息");
        }

        // 检查消息是否已过期（2分钟内可以撤回）
        LocalDateTime createTime = chatMsg.getCreateTime();
        LocalDateTime now = LocalDateTime.now();
        long minutesDiff = java.time.Duration.between(createTime, now).toMinutes();
        if (minutesDiff > 2) {
            return ResponseResult.fail("400", "消息已超过2分钟，无法撤回");
        }

        // 检查消息内容是否已被撤回
        if ("消息已撤回".equals(chatMsg.getContent())) {
            return ResponseResult.fail("400", "消息已被撤回");
        }

        // 更新消息内容为"消息已撤回"
        ChatMsg updateMsg = new ChatMsg();
        updateMsg.setMsgId(messageId);
        updateMsg.setContent("消息已撤回");

        boolean success = chatMsgService.updateById(updateMsg);
        if (success) {
            return ResponseResult.success("消息撤回成功");
        } else {
            return ResponseResult.fail("500", "消息撤回失败");
        }
    }
}

