package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.AIChatHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * AI聊天历史Mapper接口
 */
@Mapper
public interface AIChatHistoryMapper extends BaseMapper<AIChatHistory> {

    /**
     * 根据用户ID删除所有聊天记录
     * @param userId 用户ID
     * @return 删除的记录数
     */
    int deleteByUserId(@Param("userId") String userId);
}
