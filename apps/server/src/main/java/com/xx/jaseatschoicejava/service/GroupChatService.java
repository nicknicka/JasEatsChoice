package com.xx.jaseatschoicejava.service;

/**
 * 群聊消息服务
 */
public interface GroupChatService {
    /**
     * 发送消息到群聊
     * @param groupId 群ID
     * @param message 消息内容
     * @return 是否发送成功
     */
    boolean sendMessage(String groupId, String message);
}
