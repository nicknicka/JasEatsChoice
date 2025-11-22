package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.ChatMsg;
import com.xx.jaseatschoicejava.mapper.ChatMsgMapper;
import com.xx.jaseatschoicejava.service.ChatMsgService;
import org.springframework.stereotype.Service;

/**
 * 聊天消息服务实现
 */
@Service
public class ChatMsgServiceImpl extends ServiceImpl<ChatMsgMapper, ChatMsg> implements ChatMsgService {
}
