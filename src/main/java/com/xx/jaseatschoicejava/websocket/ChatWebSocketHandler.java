package com.xx.jaseatschoicejava.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.socket.*;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket处理器
 */
public class ChatWebSocketHandler implements WebSocketHandler {

    // 存储WebSocket会话，key为用户ID，value为WebSocketSession
    private static final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 获取用户ID
        String userId = (String) session.getAttributes().get("userId");
        // 存储会话
        sessionMap.put(userId, session);
        System.out.println("用户 " + userId + " 已连接");
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // 处理客户端消息
        if (message instanceof TextMessage) {
            String jsonMessage = ((TextMessage) message).getPayload();
            // 解析消息
            JsonNode msgNode = objectMapper.readTree(jsonMessage);

            // 获取消息类型
            String msgType = msgNode.get("msgType").asText();
            // 获取发送方
            String fromId = (String) session.getAttributes().get("userId");
            // 获取接收方
            String toId = msgNode.get("toId").asText();
            // 获取消息内容
            String content = msgNode.get("content").asText();

            // 构造响应消息
            ObjectNode responseMsg = objectMapper.createObjectNode();
            responseMsg.put("msgType", msgType);
            responseMsg.put("fromId", fromId);
            responseMsg.put("toId", toId);
            responseMsg.put("content", content);
            responseMsg.put("timestamp", Instant.now().toEpochMilli());
            responseMsg.put("ack", true);

            // 根据消息类型处理
            switch (msgType) {
                case "single":
                    // 单聊，发送给指定用户
                    sendMessageToUser(toId, new TextMessage(objectMapper.writeValueAsString(responseMsg)));
                    break;
                case "group":
                    // 群聊，发送给所有在线用户（简化处理，实际需根据群成员列表发送）
                    sendMessageToAll(new TextMessage(objectMapper.writeValueAsString(responseMsg)));
                    break;
                case "order_sync":
                    // 订单同步消息，发送给指定用户或群组
                    sendMessageToUser(toId, new TextMessage(objectMapper.writeValueAsString(responseMsg)));
                    break;
                case "order_status":
                    // 订单状态通知，发送给指定用户
                    sendMessageToUser(toId, new TextMessage(objectMapper.writeValueAsString(responseMsg)));
                    break;
                default:
                    // 未知消息类型，发送错误提示
                    responseMsg.put("content", "未知消息类型");
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(responseMsg)));
                    break;
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // 处理传输错误
        String userId = (String) session.getAttributes().get("userId");
        System.err.println("用户 " + userId + " 连接错误: " + exception.getMessage());
        // 移除会话
        sessionMap.remove(userId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        // 连接关闭后处理
        String userId = (String) session.getAttributes().get("userId");
        System.out.println("用户 " + userId + " 已断开连接");
        // 移除会话
        sessionMap.remove(userId);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 发送消息给指定用户
     */
    private void sendMessageToUser(String userId, TextMessage message) {
        WebSocketSession session = sessionMap.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(message);
            } catch (Exception e) {
                System.err.println("发送消息给用户 " + userId + " 失败: " + e.getMessage());
            }
        }
    }

    /**
     * 发送消息给所有在线用户
     */
    private void sendMessageToAll(TextMessage message) {
        for (WebSocketSession session : sessionMap.values()) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(message);
                } catch (Exception e) {
                    System.err.println("发送消息给用户 " + session.getAttributes().get("userId") + " 失败: " + e.getMessage());
                }
            }
        }
    }
}

