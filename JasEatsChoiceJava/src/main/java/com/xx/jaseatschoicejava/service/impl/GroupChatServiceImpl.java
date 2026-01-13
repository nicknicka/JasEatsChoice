package com.xx.jaseatschoicejava.service.impl;

import com.xx.jaseatschoicejava.entity.ChatMsg;
import com.xx.jaseatschoicejava.enums.MsgType;
import com.xx.jaseatschoicejava.service.ChatMsgService;
import com.xx.jaseatschoicejava.service.GroupChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 群聊消息服务实现
 */
@Service
public class GroupChatServiceImpl implements GroupChatService {

    private static final Logger logger = LoggerFactory.getLogger(GroupChatServiceImpl.class);

    private final ChatMsgService chatMsgService;

    // 构造函数注入
    public GroupChatServiceImpl(ChatMsgService chatMsgService) {
        this.chatMsgService = chatMsgService;
    }

    @Override
    public boolean sendMessage(String groupId, String message) {
        try {
            // 创建群聊消息
            ChatMsg chatMsg = new ChatMsg();
            chatMsg.setFromId("0"); // 系统消息，fromId设为0
            chatMsg.setToId(groupId); // 接收者为群ID
            chatMsg.setMsgType(MsgType.ORDER_SYNC.getValue()); // 订单同步消息类型
            chatMsg.setContent(message);
            chatMsg.setReadStatus(false); // 初始为未读
            chatMsg.setCreateTime(LocalDateTime.now());

            // 保存消息到数据库
            chatMsgService.save(chatMsg);

            // 这里可以添加Netty实时推送逻辑，将消息发送给群里的在线用户
            // 由于NettyChatHandler是在netty线程池中的，需要另外的方式调用
            // 例如可以通过事件总线或其他方式通知Netty推送消息

            logger.info("成功发送消息到群 {}: {}", groupId, message);
            return true;
        } catch (Exception e) {
            logger.error("发送消息到群 {} 失败", groupId, e);
            return false;
        }
    }
}
