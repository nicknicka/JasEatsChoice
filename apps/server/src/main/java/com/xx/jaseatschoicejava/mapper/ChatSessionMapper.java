package com.xx.jaseatschoicejava.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.ChatSession;

/**
 * 聊天会话Mapper接口
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
