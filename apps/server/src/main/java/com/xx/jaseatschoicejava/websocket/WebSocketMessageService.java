package com.xx.jaseatschoicejava.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.xx.jaseatschoicejava.entity.ChatMsg;
import com.xx.jaseatschoicejava.entity.ChatSession;
import com.xx.jaseatschoicejava.service.ChatSessionService;
import com.xx.jaseatschoicejava.service.UserService;
import com.xx.jaseatschoicejava.entity.User;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;

/**
 * WebSocket消息推送服务
 * 用于在HTTP API处理完成后，主动推送消息给在线用户
 * 使用Netty Channel进行WebSocket推送
 */
@Service
public class WebSocketMessageService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketMessageService.class);

    // 存储用户的Netty Channel (userId -> 该用户的全部连接)
    // 同一用户可能同时打开多个页面/标签，每个连接必须独立保留：
    // 早先的 userId -> 单个 Channel 模型会让后建立的连接覆盖先建立的，
    // 导致先建立的连接从此收不到任何推送。
    private static final Map<String, Set<Channel>> USER_CHANNELS = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private UserService userService;

    @Autowired
    private com.xx.jaseatschoicejava.service.GroupMemberService groupMemberService;

    /**
     * 构造函数，配置ObjectMapper支持Java 8日期时间类型
     */
    public WebSocketMessageService() {
        this.objectMapper = new ObjectMapper();
        // ⭐ 注册 JavaTimeModule 以支持 LocalDateTime 等类型
        this.objectMapper.registerModule(new JavaTimeModule());
        // ⭐ 禁用将日期序列化为时间戳（使用ISO-8601格式）
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * 注册用户Channel
     */
    public static void registerChannel(String userId, Channel channel) {
        if (userId == null || channel == null) {
            return;
        }
        USER_CHANNELS.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(channel);
        logger.info("✅ [WebSocket] 用户 {} 的Channel已注册，当前连接数 {}", userId, USER_CHANNELS.get(userId).size());
    }

    /**
     * 移除用户Channel
     */
    public static void removeChannel(Channel channel) {
        if (channel == null) {
            return;
        }
        USER_CHANNELS.entrySet().removeIf(entry -> {
            entry.getValue().remove(channel);
            return entry.getValue().isEmpty();
        });
        logger.info("❌ [WebSocket] Channel已移除");
    }

    /**
     * 推送聊天消息给指定用户
     */
    public void pushChatMessageToUser(String userId, ChatMsg chatMsg) {
        if (!hasActiveConnection(userId)) {
            logger.info("⚠️ [WebSocket] 用户 {} 不在线或WebSocket未连接", userId);
            return;
        }

        try {
            // 构造完整的消息对象
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", "chat");
            messageData.put("content", buildChatMessageDTO(chatMsg));

            String messageJson = objectMapper.writeValueAsString(messageData);

            int delivered = pushToUser(userId, messageJson);

            logger.info("✅ [WebSocket] 推送消息给用户 {}, messageId: {}, sessionId: {}, 送达连接数 {}",
                userId, chatMsg.getMsgId(), chatMsg.getSessionId(), delivered);
        } catch (Exception e) {
            logger.error("❌ [WebSocket] 推送消息失败给用户 {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * 构建聊天消息DTO
     */
    private Map<String, Object> buildChatMessageDTO(ChatMsg chatMsg) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("msgId", chatMsg.getMsgId());
        dto.put("sessionId", chatMsg.getSessionId()); // ⭐ 包含sessionId
        dto.put("fromId", chatMsg.getFromId());
        dto.put("toId", chatMsg.getToId());
        dto.put("content", chatMsg.getContent());
        dto.put("msgType", chatMsg.getMsgType());
        dto.put("readStatus", chatMsg.getReadStatus());
        dto.put("createdAt", chatMsg.getCreatedAt());
        dto.put("fileUrl", chatMsg.getFileUrl());
        dto.put("fileName", chatMsg.getFileName());

        // ⭐ 查询并添加发送者信息
        try {
            User sender = userService.getById(chatMsg.getFromId());
            if (sender != null) {
                dto.put("senderName", sender.getNickname());
                dto.put("username", sender.getNickname());
                logger.info("📛 [WebSocket] 查询到发送者信息: userId={}, nickname={}",
                    chatMsg.getFromId(), sender.getNickname());
            } else {
                logger.warn("⚠️ [WebSocket] 未找到发送者用户信息: userId={}", chatMsg.getFromId());
            }
        } catch (Exception e) {
            logger.error("❌ [WebSocket] 查询发送者信息失败: userId={}, error={}",
                chatMsg.getFromId(), e.getMessage(), e);
        }

        // ⭐ 查询并添加会话信息
        try {
            ChatSession session = chatSessionService.getOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatSession>()
                    .eq(ChatSession::getUserId, chatMsg.getToId())
                    .eq(ChatSession::getSessionId, chatMsg.getSessionId())
            );

            if (session != null) {
                dto.put("sessionName", session.getSessionName());
                dto.put("sessionType", session.getSessionType());
            }
        } catch (Exception e) {
            logger.warn("查询会话信息失败: {}", e.getMessage());
        }

        return dto;
    }

    /**
     * 向群成员广播系统消息
     * @param groupId 群ID
     * @param messageType 消息类型（member_left, member_kicked, member_joined等）
     * @param data 消息数据
     */
    public void broadcastToGroup(String groupId, String messageType, Map<String, Object> data) {
        logger.info("📢 [WebSocket] 准备向群成员广播消息: groupId={}, messageType={}", groupId, messageType);

        try {
            // 获取群成员列表
            java.util.List<com.xx.jaseatschoicejava.entity.GroupMember> members =
                groupMemberService.getGroupMembers(groupId);

            logger.info("📢 [WebSocket] 群 {} 当前有 {} 个成员", groupId, members.size());

            int successCount = 0;
            for (com.xx.jaseatschoicejava.entity.GroupMember member : members) {
                String userId = member.getUserId();

                if (hasActiveConnection(userId)) {
                    try {
                        // 构造消息
                        Map<String, Object> messageData = new HashMap<>();
                        messageData.put("type", "group_event");
                        messageData.put("eventType", messageType);
                        messageData.put("groupId", groupId);
                        messageData.put("data", data);
                        messageData.put("timestamp", System.currentTimeMillis());

                        String messageJson = objectMapper.writeValueAsString(messageData);
                        successCount += pushToUser(userId, messageJson);

                        logger.info("✅ [WebSocket] 成功推送群事件消息给用户: userId={}, eventType={}", userId, messageType);
                    } catch (Exception e) {
                        logger.error("❌ [WebSocket] 推送消息失败给用户 {}: {}", userId, e.getMessage());
                    }
                } else {
                    logger.debug("⚠️ [WebSocket] 用户 {} 不在线，跳过推送", userId);
                }
            }

            logger.info("📊 [WebSocket] 群事件消息推送完成: groupId={}, 总成员数={}, 在线成员数={}, 成功推送数={}",
                groupId, members.size(), successCount, successCount);
        } catch (Exception e) {
            logger.error("❌ [WebSocket] 广播群消息失败: groupId={}, error={}", groupId, e.getMessage(), e);
        }
    }

    /**
     * 获取在线用户数
     */
    public int getOnlineUserCount() {
        return USER_CHANNELS.size();
    }

    /**
     * 枚举当前在线用户（返回快照，遍历期间连接变化不会影响结果）
     */
    public static Set<String> getOnlineUsers() {
        return new HashSet<>(USER_CHANNELS.keySet());
    }

    /**
     * 向指定用户推送已序列化的消息。
     *
     * @return 实际写入成功的连接数；0 表示用户不在线或连接已失效（正常情况，不是异常）
     */
    public static int pushToUser(String userId, String message) {
        Set<Channel> channels = userId == null ? null : USER_CHANNELS.get(userId);
        if (channels == null || channels.isEmpty() || message == null) {
            return 0;
        }

        int delivered = 0;
        for (Channel channel : channels) {
            if (writeToChannel(channel, message, userId)) {
                delivered++;
            }
        }
        return delivered;
    }

    /**
     * 向全体在线用户广播已序列化的消息。
     *
     * @return 实际写入成功的连接数
     */
    public static int broadcastToAll(String message) {
        if (message == null) {
            return 0;
        }

        int delivered = 0;
        for (Map.Entry<String, Set<Channel>> entry : USER_CHANNELS.entrySet()) {
            for (Channel channel : entry.getValue()) {
                if (writeToChannel(channel, message, entry.getKey())) {
                    delivered++;
                }
            }
        }
        return delivered;
    }

    /**
     * 把消息写入一条连接，返回是否真的写进去了。
     *
     * 只把"写入成功"计入送达：连接已失效、写入被拒或写入立即失败都返回 false。
     * 调用方据此得到的是实际送达数，而不是尝试次数。
     */
    private static boolean writeToChannel(Channel channel, String message, String userId) {
        if (channel == null || !channel.isActive()) {
            return false;
        }
        try {
            io.netty.channel.ChannelFuture future =
                    channel.writeAndFlush(new TextWebSocketFrame(message));
            if (future.isDone() && !future.isSuccess()) {
                logger.warn("⚠️ [WebSocket] 推送失败 userId={}: {}",
                        userId, future.cause() == null ? "写入未成功" : future.cause().getMessage());
                return false;
            }
            // 写入尚未完成（异步 flush 进行中）：视为已交给连接，
            // 后续失败由连接自身的异常处理与断开清理负责。
            return true;
        } catch (Exception e) {
            logger.warn("⚠️ [WebSocket] 推送失败 userId={}: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * 判断该用户是否有任意一条活跃连接
     */
    public static boolean hasActiveConnection(String userId) {
        Set<Channel> channels = userId == null ? null : USER_CHANNELS.get(userId);
        if (channels == null || channels.isEmpty()) {
            return false;
        }
        for (Channel channel : channels) {
            if (channel != null && channel.isActive()) {
                return true;
            }
        }
        return false;
    }
}
