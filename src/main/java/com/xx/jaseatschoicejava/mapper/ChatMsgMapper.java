package com.xx.jaseatschoicejava.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.ChatMsg;

/**
 * 聊天消息Mapper接口
 */


@Mapper
public interface ChatMsgMapper extends BaseMapper<ChatMsg> {
}
