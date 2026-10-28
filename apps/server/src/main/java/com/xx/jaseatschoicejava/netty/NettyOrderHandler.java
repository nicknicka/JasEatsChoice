package com.xx.jaseatschoicejava.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.entity.ChatMsg;
import com.xx.jaseatschoicejava.service.ChatMsgService;
import com.xx.jaseatschoicejava.util.JwtUtil;
import com.xx.jaseatschoicejava.websocket.WebSocketMessageService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 订单WebSocket处理器
 * 处理订单相关的WebSocket消息
 *
 * @Author nickxiao
 * @Date 2025/01/19
 */
@ChannelHandler.Sharable
@Component
public class NettyOrderHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(NettyOrderHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatMsgService chatMsgService;
    private final JwtUtil jwtUtil;

    // 在线连接统一登记到 WebSocketMessageService，此处不再维护第二份映射。

    public NettyOrderHandler(ChatMsgService chatMsgService, JwtUtil jwtUtil) {
        this.chatMsgService = chatMsgService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        try {
            String text = msg.text();
            logger.info("收到订单WebSocket消息: {}", text);

            // ⭐ 先解析为 JsonNode 获取消息类型，避免反序列化失败
            com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(text);
            String msgType = jsonNode.has("msgType") ? jsonNode.get("msgType").asText() : "";

            // 处理不同类型的消息
            switch (msgType) {
                case "auth":
                    // 认证消息不包含 token 字段，直接处理
                    handleAuth(ctx);
                    break;
                case "heartbeat":
                    handleHeartbeat(ctx);
                    break;
                case "orderUpdate":
                    // 订单更新消息需要完整的 ChatMsg 对象
                    ChatMsg chatMsg = objectMapper.treeToValue(jsonNode, ChatMsg.class);
                    handleOrderUpdate(chatMsg);
                    break;
                default:
                    logger.warn("未知的消息类型: {}", msgType);
            }
        } catch (Exception e) {
            logger.error("处理订单WebSocket消息失败", e);
            sendError(ctx, "消息处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理认证消息
     * 注意：实际认证已在WebSocket握手时完成，这里只是确认客户端身份
     */
    private void handleAuth(ChannelHandlerContext ctx) {
        try {
            // 从Channel属性中获取已认证的用户ID
            String userId = ctx.channel().attr(WebSocketAuthHandler.USER_ID_KEY).get();

            if (userId == null || userId.isEmpty()) {
                sendError(ctx, "用户未认证");
                return;
            }

            // 登记到统一在线连接表（同一用户的多个连接各自独立保留）
            WebSocketMessageService.registerChannel(userId, ctx.channel());

            logger.info("订单WebSocket认证确认，用户ID: {}", userId);
            sendMessage(ctx, createMessage("system", "auth", "认证成功", ""));
        } catch (Exception e) {
            logger.error("订单WebSocket认证确认失败", e);
            sendError(ctx, "认证失败: " + e.getMessage());
        }
    }

    /**
     * 处理心跳消息
     */
    private void handleHeartbeat(ChannelHandlerContext ctx) {
        sendMessage(ctx, createMessage("system", "heartbeat", "pong", ""));
    }

    /**
     * 处理订单更新消息
     */
    private void handleOrderUpdate(ChatMsg chatMsg) {
        // 这里可以添加订单更新的业务逻辑
        logger.info("收到订单更新消息: {}", chatMsg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            logger.info("订单WebSocket握手完成");

            // 握手阶段已完成 token 校验，此处即可登记连接，
            // 不必等客户端再补发一条认证消息，连接的可用时机因此确定。
            String userId = ctx.channel().attr(WebSocketAuthHandler.USER_ID_KEY).get();
            if (userId != null && !userId.isEmpty()) {
                WebSocketMessageService.registerChannel(userId, ctx.channel());
                logger.info("订单WebSocket连接已登记，用户ID: {}", userId);
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String userId = ctx.channel().attr(WebSocketAuthHandler.USER_ID_KEY).get();
        if (userId != null) {
            WebSocketMessageService.removeChannel(ctx.channel());
            logger.info("订单WebSocket用户断开连接，用户ID: {}", userId);
        }
    }

    /**
     * 发送消息给客户端
     */
    public void sendMessage(ChannelHandlerContext ctx, String message) {
        if (ctx != null && ctx.channel().isActive()) {
            ctx.writeAndFlush(new TextWebSocketFrame(message));
        }
    }

    /**
     * 发送错误消息
     */
    private void sendError(ChannelHandlerContext ctx, String error) {
        sendMessage(ctx, createMessage("system", "error", error, ""));
    }

    /**
     * 创建消息
     */
    private String createMessage(String fromId, String msgType, String content, String toId) {
        try {
            ChatMsg msg = new ChatMsg();
            msg.setFromId(fromId);
            msg.setMsgType(msgType);
            msg.setContent(content);
            msg.setToId(toId);
            return objectMapper.writeValueAsString(msg);
        } catch (Exception e) {
            logger.error("创建消息失败", e);
            return "";
        }
    }
}
