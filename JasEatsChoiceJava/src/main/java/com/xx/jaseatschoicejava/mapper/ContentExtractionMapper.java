package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.ContentExtraction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 内容提取Mapper接口
 *
 * @author Claude
 * @since 2025-01-31
 */
@Mapper
public interface ContentExtractionMapper extends BaseMapper<ContentExtraction> {

    /**
     * 根据内容源ID查询提取结果
     *
     * @param sourceId 内容源ID
     * @return 提取结果
     */
    @Select("SELECT * FROM t_content_extraction WHERE source_id = #{sourceId}")
    ContentExtraction selectBySourceId(@Param("sourceId") String sourceId);

    /**
     * 查询未发布的提取结果
     *
     * @return 提取结果列表
     */
    @Select("SELECT * FROM t_content_extraction WHERE is_published = 0 ORDER BY create_time DESC")
    List<ContentExtraction> selectUnpublished();

    /**
     * 查询未验证的提取结果
     *
     * @return 提取结果列表
     */
    @Select("SELECT * FROM t_content_extraction WHERE is_verified = 0 ORDER BY create_time DESC")
    List<ContentExtraction> selectUnverified();
}
