package com.xx.jaseatschoicejava.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.ContentSource;

/**
 * 内容源Mapper接口
 *

 * @since 2025-01-31
 */
@Mapper
public interface ContentSourceMapper extends BaseMapper<ContentSource> {

    /**
     * 查询用户的内容源列表
     *
     * @param userId 用户ID
     * @return 内容源列表
     */
    @Select("SELECT * FROM ext_content_source WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<ContentSource> selectByUserId(@Param("userId") String userId);

    /**
     * 查询待提取的内容源
     *
     * @return 内容源列表
     */
    @Select("SELECT * FROM ext_content_source WHERE extraction_status = 'PENDING' ORDER BY created_at ASC")
    List<ContentSource> selectPendingSources();

    /**
     * 查询提取失败的内容源
     *
     * @param maxRetryCount 最大重试次数
     * @return 内容源列表
     */
    @Select("SELECT * FROM ext_content_source WHERE extraction_status IN ('FAILED', 'PARSE_FAILED') ORDER BY created_at ASC LIMIT #{limit}")
    List<ContentSource> selectFailedSources(@Param("limit") int limit);
}
