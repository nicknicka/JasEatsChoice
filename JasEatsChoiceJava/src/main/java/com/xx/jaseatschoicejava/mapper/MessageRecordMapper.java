package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.MessageRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 消息记录Mapper
 *
 * @author xx
 * @date 2025-01-26
 */
@Mapper
public interface MessageRecordMapper extends BaseMapper<MessageRecord> {

    /**
     * 根据用户ID获取消息记录列表
     *
     * @param userId 用户ID
     * @param pageSize 分页大小
     * @param pageNum 当前页码
     * @return 消息记录列表
     */
    List<MessageRecord> getMessageRecordsByUserId(@Param("userId") String userId,
                                                  @Param("pageSize") Integer pageSize,
                                                  @Param("pageNum") Integer pageNum);

    /**
     * 根据用户ID获取未读消息数量
     *
     * @param userId 用户ID
     * @return 未读消息数量
     */
    Integer getUnreadMessageCountByUserId(@Param("userId") String userId);
}
