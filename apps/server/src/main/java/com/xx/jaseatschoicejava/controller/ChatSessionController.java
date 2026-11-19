package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.ChatSession;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.security.CurrentIdentityResolver;
import com.xx.jaseatschoicejava.service.ChatMsgService;
import com.xx.jaseatschoicejava.service.ChatSessionService;
import com.xx.jaseatschoicejava.service.UserService;
import com.xx.jaseatschoicejava.util.ChatSessionIdGenerator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 聊天会话控制器
 */
@Slf4j
@Api(tags = "聊天会话管理")
@RestController
@RequestMapping("/v1/chat")
public class ChatSessionController {

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private ChatMsgService chatMsgService;

    @Autowired
    private UserService userService;

    /**
     * 归属判据统一取自共享组件（#597），不在控制器里另写一套。
     */
    @Autowired
    private CurrentIdentityResolver identityResolver;

    /**
     * 按 sessionId 取出属于调用方的会话行。
     *
     * <p>不能用 {@code getOne}：sessionId 由「排序后的双方ID」确定性哈希生成
     * （{@link ChatSessionIdGenerator}），同一 sessionId 会存在多行——会话参与方各一行
     * （种子数据 {@code S58234cb0310fdfc04b643ce4c4c8015c} 就对应两个 user_id）。表上唯一键是
     * {@code uk_user_session(user_id, session_id)}，也印证了同一 sessionId 允许跨 user_id 重复。
     * 因此这里按 sessionId 列出候选，再挑出归属调用方的那一行。
     *
     * <p>归属判据用 {@link CurrentIdentityResolver#isCallerIdentity}：{@code ai_chat_session.user_id}
     * 既可能是用户ID（种子 {@code Sseed*}），也可能是商家ID（种子 {@code Smerchant*}，商家端桌面
     * Messages.vue 与 e2e 10.14 都传商家ID）。只比用户ID 会把商家端会话误判成别人的。
     *
     * <p>请求体/查询参数里的 userId 只当"调用方想操作自己哪一份身份"的提示，且必须先通过
     * isCallerIdentity 校验才有意义，绝不作为归属依据。
     *
     * @param request      HTTP请求
     * @param sessionId    会话ID
     * @param identityHint 请求里带的 userId，可为 null
     * @return 归属调用方的会话行；会话不存在或不属于调用方时返回 null
     */
    private ChatSession findOwnedSession(HttpServletRequest request, String sessionId, String identityHint) {
        List<ChatSession> owned = chatSessionService.list(
                        new LambdaQueryWrapper<ChatSession>().eq(ChatSession::getSessionId, sessionId))
                .stream()
                .filter(session -> identityResolver.isCallerIdentity(request, session.getUserId()))
                .collect(Collectors.toList());

        if (owned.isEmpty()) {
            return null;
        }
        if (identityHint != null) {
            for (ChatSession session : owned) {
                if (identityHint.equals(session.getUserId())) {
                    return session;
                }
            }
        }
        return owned.get(0);
    }

    /**
     * 获取用户的所有会话列表
     */
    @ApiOperation("获取用户会话列表")
    @GetMapping("/users/{userId}/chat-sessions")
    public ResponseResult<?> getUserChatSessions(@PathVariable String userId, HttpServletRequest request) {
        // #597：会话归属方可能是用户ID 也可能是商家ID，用 isCallerIdentity 认调用方的任一身份；
        // 非管理员只能查自己的会话。
        if (!identityResolver.isCallerIdentity(request, userId)) {
            return ResponseResult.fail("403", "无权查看该用户会话");
        }

        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSession::getUserId, userId);
        queryWrapper.orderByDesc(ChatSession::getPinned); // 置顶的在前
        queryWrapper.orderByDesc(ChatSession::getLastMessageTime); // 按最后消息时间降序

        log.info("获取用户会话列表: userId={}", userId);
        List<ChatSession> sessions = chatSessionService.list(queryWrapper);
        log.info("获取用户会话列表: {}", sessions);

        Set<String> targetUserIds = sessions.stream()
                .filter(session -> "single".equals(session.getSessionType()))
                .map(ChatSession::getTargetId)
                .filter(targetId -> targetId != null && !targetId.isEmpty())
                .collect(Collectors.toSet());

        Map<String, User> targetUserMap = targetUserIds.isEmpty()
                ? new HashMap<>()
                : userService.listByIds(targetUserIds).stream()
                    .collect(Collectors.toMap(User::getUserId, user -> user, (existing, replacement) -> existing));

        // 转换为前端需要的格式
        List<Map<String, Object>> result = sessions.stream()
                .map(session -> {
                    User targetUser = "single".equals(session.getSessionType())
                            ? targetUserMap.get(session.getTargetId())
                            : null;
                    String sessionName = targetUser != null && targetUser.getNickname() != null && !targetUser.getNickname().trim().isEmpty()
                            ? targetUser.getNickname()
                            : session.getSessionName();
                    String avatar = targetUser != null && targetUser.getAvatar() != null && !targetUser.getAvatar().trim().isEmpty()
                            ? targetUser.getAvatar()
                            : session.getAvatar();

                    Map<String, Object> sessionMap = new HashMap<>();
                    sessionMap.put("id", session.getSessionId());
                    sessionMap.put("type", session.getSessionType());
                    sessionMap.put("name", sessionName);
                    sessionMap.put("avatar", avatar != null ? avatar : (session.getSessionType().equals("group") ? "👥" : "💬"));
                    sessionMap.put("lastMessage", session.getLastMessage() != null ? session.getLastMessage() : "暂无消息");
                    sessionMap.put("time", session.getLastMessageTime() != null ?
                            session.getLastMessageTime().toString().substring(11, 16) : "");
                    sessionMap.put("unreadCount", session.getUnreadCount() != null ? session.getUnreadCount() : 0);
                    sessionMap.put("pinned", session.getPinned() != null ? session.getPinned() == 1 : false);
                    sessionMap.put("memberCount", session.getMemberCount() != null ? session.getMemberCount() : 0);
                    // ⭐ 添加 groupId（仅群聊会话有值）
                    sessionMap.put("groupId", session.getGroupId());
                    // ⭐ 添加 targetId（仅单聊会话有值）
                    sessionMap.put("targetId", session.getTargetId());
                    return sessionMap;
                })
                .collect(Collectors.toList());

        return ResponseResult.success(result);
    }

    /**
     * 创建或更新会话
     */
    @ApiOperation("创建或更新会话")
    @PostMapping("/sessions")
    public ResponseResult<?> createOrUpdateSession(@RequestBody Map<String, Object> params,
                                                  HttpServletRequest request) {
        String userId = (String) params.get("userId");
        // #597：这里**故意不校验归属**。用户端建群时，创建者会替每个成员各建一条会话行
        // （apps/client/src/renderer/src/views/user/Chat.vue:1614），加了 isCallerIdentity
        // 会让群聊建不起来。正确修法是服务端在建群时扇出成员会话，属于接口契约变更，
        // 已记在 #597 里；读/改/删既有会话的越权在本文件其余方法里已收口。
        String sessionId = (String) params.get("sessionId");
        String sessionType = (String) params.get("sessionType");
        String sessionName = (String) params.get("sessionName");
        String avatar = (String) params.get("avatar");
        Integer memberCount = params.get("memberCount") != null ?
                (Integer) params.get("memberCount") : 0;

        String groupId = (String) params.get("groupId"); // ⭐ 直接从请求参数中获取groupId
        String targetId = (String) params.get("targetId"); // ⭐ 提前声明 targetId，避免作用域问题

        // ⭐ 统一生成sessionId的规则
        if ("group".equals(sessionType)) {
            // 群聊：使用 ChatSessionIdGenerator 生成 S 开头的会话ID
            // 如果没有传groupId，且sessionId是G开头的，从sessionId中提取
            if (groupId == null && sessionId != null && sessionId.startsWith("G")) {
                groupId = sessionId;
            }

            // 如果 sessionId 不是 S 开头，需要转换
            if (sessionId != null && !sessionId.startsWith("S")) {
                sessionId = ChatSessionIdGenerator.getGroupChatSessionId(sessionId);
            }
            // 如果已经是 S 开头，直接使用
        } else {
            // ⭐ 单聊：如果前端没有传sessionId，或者sessionId不合法，则自动生成
            // 使用双方的 userId 生成确定性的 sessionId（MD5哈希）
            if (sessionId == null || sessionId.isEmpty() || !sessionId.startsWith("S")) {
                // 从请求中获取对方的ID（优先使用 targetId，其次从 sessionId 解析）
                if (targetId == null && sessionId != null && !sessionId.startsWith("S")) {
                    // 如果没有 targetId，尝试使用 sessionId 作为 targetId
                    targetId = sessionId;
                }

                if (targetId != null && !targetId.isEmpty()) {
                    // 使用双方ID生成sessionId（确定性哈希）
                    sessionId = ChatSessionIdGenerator.generateSingleChatSessionId(userId, targetId);
                    log.info("自动生成单聊sessionId: userId={}, targetId={}, sessionId={}",
                            userId, targetId, sessionId);
                } else {
                    // 如果没有 targetId，使用传入的 sessionId（兼容旧逻辑）
                    log.warn("未提供targetId，使用传入的sessionId: {}", sessionId);
                }
            }
        }

        // 查找是否已存在该会话
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
            session.setSessionName(sessionName);
            session.setAvatar(avatar);
            session.setMemberCount(memberCount);
            session.setGroupId(groupId); // ⭐ 保存 groupId（仅群聊有值）
            session.setUnreadCount(0);
            session.setPinned(0);  // 0-未置顶
            session.setCreatedAt(LocalDateTime.now());
            session.setUpdatedAt(LocalDateTime.now());

            // ⭐ 设置 targetId（仅单聊）
            if ("single".equals(sessionType) && targetId != null && !targetId.isEmpty()) {
                session.setTargetId(targetId);
            }

            // 使用 saveOrUpdate 避免并发插入导致的唯一键冲突
            try {
                chatSessionService.saveOrUpdate(session);
            } catch (Exception e) {
                // 如果保存失败，可能是并发导致的唯一键冲突，重新查询并更新
                log.warn("保存会话失败，尝试重新查询并更新: userId={}, sessionId={}, error={}",
                        userId, sessionId, e.getMessage());
                ChatSession existingSession = chatSessionService.getOne(queryWrapper);
                if (existingSession != null) {
                    existingSession.setSessionName(sessionName);
                    existingSession.setAvatar(avatar);
                    existingSession.setMemberCount(memberCount);
                    existingSession.setUpdatedAt(LocalDateTime.now());
                    chatSessionService.updateById(existingSession);
                    return ResponseResult.success(existingSession);
                } else {
                    throw e;
                }
            }
        } else {
            // 更新会话信息
            session.setSessionName(sessionName);
            session.setAvatar(avatar);
            session.setMemberCount(memberCount);
            session.setUpdatedAt(LocalDateTime.now());
            chatSessionService.updateById(session);
        }

        return ResponseResult.success(session);
    }

    /**
     * 更新会话最后消息
     */
    @ApiOperation("更新会话最后消息")
    @PostMapping("/sessions/{sessionId}/last-message")
    public ResponseResult<?> updateLastMessage(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        String userId = (String) params.get("userId");
        String content = (String) params.get("content");
        LocalDateTime messageTime = LocalDateTime.now();

        // #597：先按 sessionId 取出属于调用方的会话行；不存在或不属于调用方一律 404
        // （用 404 而不是 403，避免用他人主键探测会话是否存在）。
        ChatSession session = findOwnedSession(request, sessionId, userId);
        if (session == null) {
            return ResponseResult.fail("404", "会话不存在");
        }

        // 更新发送者的会话
        LambdaUpdateWrapper<ChatSession> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChatSession::getUserId, session.getUserId());
        updateWrapper.eq(ChatSession::getSessionId, sessionId);
        updateWrapper.set(ChatSession::getLastMessage, content);
        updateWrapper.set(ChatSession::getLastMessageTime, messageTime);
        updateWrapper.set(ChatSession::getUpdatedAt, LocalDateTime.now());
        boolean updated = chatSessionService.update(updateWrapper);

        // 同上：内容与时间戳都未变化时会返回 0 行，属幂等重放，不当作失败
        if (!updated) {
            log.debug("更新最后消息未改变任何行（内容与秒级时间戳相同）：sessionId={}", sessionId);
        }

        return ResponseResult.success("更新成功");
    }

    /**
     * 增加会话未读消息数
     */
    @ApiOperation("增加未读消息数")
    @PostMapping("/sessions/{sessionId}/unread-increment")
    public ResponseResult<?> incrementUnreadCount(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        String userId = (String) params.get("userId");

        // #597：归属校验通过才允许改未读；不属于调用方的会话按不存在处理
        ChatSession session = findOwnedSession(request, sessionId, userId);
        if (session == null) {
            return ResponseResult.fail("404", "会话不存在");
        }

        session.setUnreadCount((session.getUnreadCount() != null ? session.getUnreadCount() : 0) + 1);
        session.setUpdatedAt(LocalDateTime.now());
        boolean updated = chatSessionService.updateById(session);

        if (!updated) {
            return ResponseResult.fail("404", "会话不存在");
        }

        return ResponseResult.success("更新成功");
    }

    /**
     * 清除会话未读消息数
     */
    @ApiOperation("清除未读消息数")
    @PostMapping("/sessions/{sessionId}/unread-clear")
    public ResponseResult<?> clearUnreadCount(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        String userId = (String) params.get("userId");

        // #597：归属校验通过才允许改未读；不属于调用方的会话按不存在处理
        ChatSession session = findOwnedSession(request, sessionId, userId);
        if (session == null) {
            return ResponseResult.fail("404", "会话不存在");
        }

        LambdaUpdateWrapper<ChatSession> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChatSession::getUserId, session.getUserId());
        updateWrapper.eq(ChatSession::getSessionId, sessionId);
        updateWrapper.set(ChatSession::getUnreadCount, 0);
        updateWrapper.set(ChatSession::getUpdatedAt, LocalDateTime.now());
        boolean updated = chatSessionService.update(updateWrapper);

        // 受影响行数为 0 不当作失败：MySQL 返回的是"实际改变的行数"，
        // 未读已是 0、且 updated_at 在同一秒内相同时会返回 0 行，属幂等重放。
        // 授权已由上面的归属校验保证，这里无需再用行数兜底。
        if (!updated) {
            log.debug("清除未读未改变任何行（幂等重放）：sessionId={}", sessionId);
        }

        return ResponseResult.success("清除成功");
    }

    /**
     * 切换会话置顶状态
     */
    @ApiOperation("切换置顶状态")
    @PostMapping("/sessions/{sessionId}/toggle-pin")
    public ResponseResult<?> togglePin(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        String userId = (String) params.get("userId");

        // #597：归属校验通过才允许改置顶；不属于调用方的会话按不存在处理
        ChatSession session = findOwnedSession(request, sessionId, userId);
        if (session == null) {
            return ResponseResult.fail("404", "会话不存在");
        }

        // 切换置顶状态：0变1，1变0
        int newPinned = (session.getPinned() == null || session.getPinned() == 0) ? 1 : 0;
        session.setPinned(newPinned);
        session.setUpdatedAt(LocalDateTime.now());
        boolean updated = chatSessionService.updateById(session);

        if (!updated) {
            return ResponseResult.fail("404", "会话不存在");
        }

        return ResponseResult.success(newPinned == 1);
    }

    /**
     * 删除会话
     */
    @ApiOperation("删除会话")
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseResult<?> deleteSession(
            @PathVariable String sessionId,
            @RequestParam String userId,
            HttpServletRequest request) {

        // #597：userId 只当"删自己哪一份身份"的提示，归属仍以 sessionId 对应行的实际归属为准
        ChatSession session = findOwnedSession(request, sessionId, userId);
        if (session == null) {
            return ResponseResult.fail("404", "会话不存在");
        }

        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSession::getUserId, session.getUserId());
        queryWrapper.eq(ChatSession::getSessionId, sessionId);

        boolean deleted = chatSessionService.remove(queryWrapper);

        // 判到写之间会话可能已被删除，受影响行数为 0 时同样按不存在处理
        if (deleted) {
            return ResponseResult.success("删除成功");
        }

        return ResponseResult.fail("404", "会话不存在");
    }
}
