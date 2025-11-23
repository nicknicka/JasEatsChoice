package com.xx.jaseatschoicejava.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.constants.Constant;
import com.xx.jaseatschoicejava.enums.MsgType;
import com.xx.jaseatschoicejava.entity.ChatMsg;
import com.xx.jaseatschoicejava.service.ChatMsgService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Netty聊天消息处理器
 *
 * @Author nickxiao
 * @Date 2025/11/22
 */
@ChannelHandler.Sharable
public class NettyChatHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger logger = LoggerFactory.getLogger(NettyChatHandler.class);

    // 存储所有在线Channel
    private static final ChannelGroup CHANNEL_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    // 存储用户ID与Channel的映射关系
    private static final ConcurrentHashMap<String, Channel> USER_CHANNEL_MAP = new ConcurrentHashMap<>();

    // JSON解析器
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 聊天消息服务（用于存储离线消息）
    private final ChatMsgService chatMsgService;

    // 构造函数注入
    public NettyChatHandler(ChatMsgService chatMsgService) {
        this.chatMsgService = chatMsgService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        CHANNEL_GROUP.add(ctx.channel());
        logger.info("Channel {} connected", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 移除Channel
        CHANNEL_GROUP.remove(ctx.channel());

        // 移除用户映射
        USER_CHANNEL_MAP.entrySet().removeIf(entry -> entry.getValue().equals(ctx.channel()));

        logger.info("Channel {} disconnected", ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
        logger.info("Received message: {} from channel: {}", message, ctx.channel().remoteAddress());

        try {
            // 解析JSON消息
            com.fasterxml.jackson.databind.JsonNode msgNode = objectMapper.readTree(message);

            // 获取消息类型
            String msgType = msgNode.get("msgType").asText();
            // 获取发送方
            String fromId = msgNode.get("fromId").asText();
            // 获取接收方
            String toId = msgNode.get("toId").asText();
            // 获取消息内容
            String content = msgNode.get("content").asText();

            // 构造响应消息
            com.fasterxml.jackson.databind.node.ObjectNode responseMsg = objectMapper.createObjectNode();
            responseMsg.put("msgType", msgType);
            responseMsg.put("fromId", fromId);
            responseMsg.put("toId", toId);
            responseMsg.put("content", content);
            responseMsg.put("timestamp", Instant.now().toEpochMilli());
            responseMsg.put("ack", true);

            // 将用户ID与Channel绑定
            USER_CHANNEL_MAP.putIfAbsent(fromId, ctx.channel());

            // 根据消息类型处理
            switch (msgType) {
                case "single":
                    // 单聊，发送给指定用户
                    sendMessageToUser(toId, objectMapper.writeValueAsString(responseMsg));
                    break;
                case "group":
                    // 群聊，发送给所有在线用户（简化处理，实际需根据群成员列表发送）
                    sendMessageToAll(objectMapper.writeValueAsString(responseMsg));
                    break;
                case "order_sync":
                    // 订单同步消息，发送给指定用户或群组
                    sendMessageToUser(toId, objectMapper.writeValueAsString(responseMsg));
                    break;
                case "order_status":
                    // 订单状态通知，发送给指定用户
                    sendMessageToUser(toId, objectMapper.writeValueAsString(responseMsg));
                    break;
                default:
                    // 未知消息类型，发送错误提示
                    responseMsg.put("content", "未知消息类型");
                    ctx.writeAndFlush(objectMapper.writeValueAsString(responseMsg));
                    break;
            }
        } catch (Exception e) {
            logger.error("Failed to process message: {}", message, e);
            ctx.writeAndFlush("{\"msgType\":\"error\",\"content\":\"消息格式错误\"}");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Channel {} exception: {}", ctx.channel().remoteAddress(), cause.getMessage(), cause);
        ctx.close();
    }

    /**
     * 发送消息给指定用户
     */
    private void sendMessageToUser(String userId, String message) {
        Channel channel = USER_CHANNEL_MAP.get(userId);
        if (channel != null && channel.isActive()) {
            try {
                channel.writeAndFlush(message);
                logger.info("Sent message: {} to user: {}", message, userId);
            } catch (Exception e) {
                logger.error("Failed to send message to user {}: {}", userId, e.getMessage());
            }
        } else {
            logger.info("User {} is offline, storing message in database", userId);
            // 将消息存入数据库，等待用户上线后推送
            try {
                // 解析JSON消息
                com.fasterxml.jackson.databind.JsonNode msgNode = objectMapper.readTree(message);

                // 提取消息内容
                String fromId = msgNode.get("fromId").asText();
                String toId = msgNode.get("toId").asText();
                String msgType = msgNode.get("msgType").asText();
                String content = msgNode.get("content").asText();

                // 保存到数据库
                ChatMsg chatMsg = new ChatMsg();
                chatMsg.setFromId(fromId);
                chatMsg.setToId(toId);
                chatMsg.setMsgType(msgType);
                chatMsg.setContent(content);
                chatMsg.setReadStatus(false); // 未读状态
                chatMsg.setCreateTime(LocalDateTime.now());

                chatMsgService.save(chatMsg);
                logger.info("Message stored in database for user: {}", userId);
            } catch (Exception e) {
                logger.error("Failed to store offline message: {}", e.getMessage());
            }
        }
    }

    /**
     * 发送消息给所有在线用户
     */
    private void sendMessageToAll(String message) {
        CHANNEL_GROUP.writeAndFlush(message);
        logger.info("Broadcast message: {}", message);
    }
}

