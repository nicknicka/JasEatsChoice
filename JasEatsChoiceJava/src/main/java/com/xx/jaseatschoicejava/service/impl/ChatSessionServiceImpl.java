package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.ChatSession;
import com.xx.jaseatschoicejava.mapper.ChatSessionMapper;
import com.xx.jaseatschoicejava.service.ChatSessionService;
import org.springframework.stereotype.Service;

/**
 * 聊天会话服务实现
 */
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements ChatSessionService {
}
