package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.ContentSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 内容源Mapper接口
 *
 * @author Claude
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
    @Select("SELECT * FROM t_content_source WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<ContentSource> selectByUserId(@Param("userId") String userId);

    /**
     * 查询待提取的内容源
     *
     * @return 内容源列表
     */
    @Select("SELECT * FROM t_content_source WHERE extraction_status = 'PENDING' ORDER BY create_time ASC")
    List<ContentSource> selectPendingSources();

    /**
     * 查询提取失败的内容源
     *
     * @param maxRetryCount 最大重试次数
     * @return 内容源列表
     */
    @Select("SELECT * FROM t_content_source WHERE extraction_status = 'FAILED' ORDER BY create_time ASC LIMIT #{limit}")
    List<ContentSource> selectFailedSources(@Param("limit") int limit);
}
