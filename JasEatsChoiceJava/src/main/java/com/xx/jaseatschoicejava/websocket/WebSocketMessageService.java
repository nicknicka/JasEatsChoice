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

    // 存储用户的Netty Channel (userId -> Channel)
    private static final Map<String, Channel> USER_CHANNELS = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private UserService userService;

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
        USER_CHANNELS.put(userId, channel);
        logger.info("✅ [WebSocket] 用户 {} 的Channel已注册", userId);
    }

    /**
     * 移除用户Channel
     */
    public static void removeChannel(Channel channel) {
        USER_CHANNELS.entrySet().removeIf(entry -> entry.getValue().equals(channel));
        logger.info("❌ [WebSocket] Channel已移除");
    }

    /**
     * 推送聊天消息给指定用户
     */
    public void pushChatMessageToUser(String userId, ChatMsg chatMsg) {
        Channel channel = USER_CHANNELS.get(userId);
        if (channel == null || !channel.isActive()) {
            logger.info("⚠️ [WebSocket] 用户 {} 不在线或WebSocket未连接", userId);
            return;
        }

        try {
            // 构造完整的消息对象
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", "chat");
            messageData.put("content", buildChatMessageDTO(chatMsg));

            String messageJson = objectMapper.writeValueAsString(messageData);

            // 使用Netty Channel发送消息
            channel.writeAndFlush(new TextWebSocketFrame(messageJson));

            logger.info("✅ [WebSocket] 推送消息给用户 {}, messageId: {}, sessionId: {}",
                userId, chatMsg.getMsgId(), chatMsg.getSessionId());
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
        dto.put("createTime", chatMsg.getCreateTime());
        dto.put("fileUrl", chatMsg.getFileUrl());
        dto.put("fileName", chatMsg.getFileName());

        // ⭐ 查询并添加发送者信息
        try {
            User sender = userService.getById(chatMsg.getFromId());
            if (sender != null) {
                dto.put("senderName", sender.getNickname());
                dto.put("username", sender.getNickname());
            }
        } catch (Exception e) {
            logger.warn("查询发送者信息失败: {}", e.getMessage());
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
     * 获取在线用户数
     */
    public int getOnlineUserCount() {
        return USER_CHANNELS.size();
    }
}
