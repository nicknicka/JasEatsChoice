package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.ChatSession;
import com.xx.jaseatschoicejava.entity.GroupOrder;
import com.xx.jaseatschoicejava.mapper.ChatSessionMapper;
import com.xx.jaseatschoicejava.service.ChatSessionService;
import com.xx.jaseatschoicejava.service.GroupOrderChatService;
import com.xx.jaseatschoicejava.service.GroupOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 群订单专属会话服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupOrderChatServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession>
        implements GroupOrderChatService {

    private final ChatSessionService chatSessionService;
    private final GroupOrderService groupOrderService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatSession createGroupOrderSession(String groupOrderId, String groupId, String merchantId) {
        log.info("创建群订单专属会话 - groupOrderId: {}, groupId: {}, merchantId: {}",
                groupOrderId, groupId, merchantId);

        // 获取群订单信息
        GroupOrder groupOrder = groupOrderService.getById(groupOrderId);
        if (groupOrder == null) {
            throw new RuntimeException("群订单不存在");
        }

        // 检查是否已存在专属会话
        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSession::getRelatedOrderId, groupOrderId);
        long count = chatSessionService.count(queryWrapper);

        if (count > 0) {
            log.info("群订单专属会话已存在 - groupOrderId: {}", groupOrderId);
            return chatSessionService.getOne(queryWrapper);
        }

        // 创建群订单专属会话
        ChatSession session = new ChatSession();
        session.setSessionId(generateSessionId(groupOrderId));
        session.setUserId(groupOrder.getInitiatorId()); // 会话所有者为发起者
        session.setSessionType("group_order"); // 群订单专属会话
        session.setSessionName("【群订单】" + groupId + "群-" + merchantId + "商家");
        session.setAvatar(""); // 可以设置群订单默认头像
        session.setGroupId(groupId);
        session.setRelatedOrderId(groupOrderId);
        session.setUnreadCount(0);
        session.setPinned(1); // 默认置顶
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());

        // 设置最后消息为订单创建消息
        String lastMessage = "群订单已创建，等待商家接单";
        session.setLastMessage(lastMessage);
        session.setLastMessageTime(LocalDateTime.now());

        chatSessionService.save(session);

        log.info("群订单专属会话创建成功 - sessionId: {}", session.getSessionId());
        return session;
    }

    @Override
    public String syncMessageToGroup(String merchantId, String groupOrderId, String message, String messageType) {
        log.info("同步消息到群订单会话 - merchantId: {}, groupOrderId: {}, type: {}",
                merchantId, groupOrderId, messageType);

        // 获取群订单专属会话
        ChatSession session = getGroupOrderSession(groupOrderId);
        if (session == null) {
            log.warn("群订单专属会话不存在 - groupOrderId: {}", groupOrderId);
            return null;
        }

        // 构建同步消息，添加前缀和标签
        String syncedMessage = buildSyncMessage(messageType, message);

        // 这里需要调用群聊服务发送消息
        // 实际发送由 GroupChatService 或 ChatController 处理
        // 此服务只负责构建消息格式
        // TODO: 调用群聊消息发送接口

        // 更新会话的最后消息
        updateLastMessage(groupOrderId, syncedMessage, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return syncedMessage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean archiveGroupOrderSession(String groupOrderId) {
        log.info("归档群订单专属会话 - groupOrderId: {}", groupOrderId);

        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSession::getRelatedOrderId, groupOrderId);
        queryWrapper.eq(ChatSession::getSessionType, "group_order");

        ChatSession session = chatSessionService.getOne(queryWrapper);
        if (session == null) {
            log.warn("群订单专属会话不存在，无需归档 - groupOrderId: {}", groupOrderId);
            return false;
        }

        // 更新会话名称，添加"已完成"标记
        session.setSessionName(session.getSessionName() + " [已完成]");
        session.setPinned(0); // 取消置顶
        session.setUpdateTime(LocalDateTime.now());

        return chatSessionService.updateById(session);
    }

    @Override
    public ChatSession getGroupOrderSession(String groupOrderId) {
        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSession::getRelatedOrderId, groupOrderId);
        queryWrapper.eq(ChatSession::getSessionType, "group_order");
        return chatSessionService.getOne(queryWrapper);
    }

    @Override
    public boolean hasGroupOrderSession(String groupOrderId) {
        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSession::getRelatedOrderId, groupOrderId);
        queryWrapper.eq(ChatSession::getSessionType, "group_order");
        long count = chatSessionService.count(queryWrapper);
        return count > 0;
    }

    @Override
    public List<Map<String, Object>> getPendingSyncMessages(String merchantId, String sessionType) {
        // TODO: 实现从商家专属会话获取未同步消息
        // 这需要查询消息表并过滤未同步的消息
        return List.of();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markMessageAsSynced(String messageId) {
        // TODO: 标记消息为已同步
        // 需要在消息表中添加 synced 字段
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLastMessage(String groupOrderId, String lastMessage, String lastMessageTime) {
        ChatSession session = getGroupOrderSession(groupOrderId);
        if (session == null) {
            return false;
        }

        session.setLastMessage(lastMessage);
        session.setLastMessageTime(LocalDateTime.parse(lastMessageTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        session.setUpdateTime(LocalDateTime.now());

        return chatSessionService.updateById(session);
    }

    /**
     * 生成会话ID
     */
    private String generateSessionId(String groupOrderId) {
        return "GO_" + groupOrderId + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 构建同步消息
     * 格式：【订单同步】[标签] 消息内容
     */
    private String buildSyncMessage(String messageType, String messageContent) {
        return String.format("【订单同步】[%s] %s", messageType, messageContent);
    }
}
