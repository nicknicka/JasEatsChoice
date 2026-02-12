package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.entity.ChatSession;
import com.xx.jaseatschoicejava.entity.GroupOrder;

import java.util.List;
import java.util.Map;

/**
 * 群订单专属会话服务
 * 负责处理群订单与聊天会话的关联和同步
 */
public interface GroupOrderChatService {

    /**
     * 商家接单后自动创建群订单专属会话
     * @param groupOrderId 群订单ID
     * @param groupId 群组ID
     * @param merchantId 商家ID
     * @return 创建的会话
     */
    ChatSession createGroupOrderSession(String groupOrderId, String groupId, String merchantId);

    /**
     * 同步商家消息到群订单会话
     * @param merchantId 商家ID
     * @param groupOrderId 群订单ID
     * @param message 消息内容
     * @param messageType 消息类型（如：出餐进度、食材替换等）
     * @return 同步的消息内容（包含【订单同步】前缀）
     */
    String syncMessageToGroup(String merchantId, String groupOrderId, String message, String messageType);

    /**
     * 群订单完成后归档会话
     * @param groupOrderId 群订单ID
     * @return 是否归档成功
     */
    boolean archiveGroupOrderSession(String groupOrderId);

    /**
     * 获取群订单的专属会话
     * @param groupOrderId 群订单ID
     * @return 会话信息
     */
    ChatSession getGroupOrderSession(String groupOrderId);

    /**
     * 检查群订单是否有专属会话
     * @param groupOrderId 群订单ID
     * @return 是否存在专属会话
     */
    boolean hasGroupOrderSession(String groupOrderId);

    /**
     * 获取待同步的消息列表（从商家会话获取）
     * @param merchantId 商家ID
     * @param sessionType 会话类型筛选
     * @return 消息列表
     */
    List<Map<String, Object>> getPendingSyncMessages(String merchantId, String sessionType);

    /**
     * 标记消息为已同步
     * @param messageId 消息ID
     * @return 是否标记成功
     */
    boolean markMessageAsSynced(String messageId);

    /**
     * 更新群订单会话的最后消息
     * @param groupOrderId 群订单ID
     * @param lastMessage 最后消息内容
     * @param lastMessageTime 最后消息时间
     * @return 是否更新成功
     */
    boolean updateLastMessage(String groupOrderId, String lastMessage, String lastMessageTime);
}
