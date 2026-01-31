package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.ExtractionTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 提取任务Mapper接口
 *
 * @author Claude
 * @since 2025-01-31
 */
@Mapper
public interface ExtractionTaskMapper extends BaseMapper<ExtractionTask> {

    /**
     * 查询待处理任务
     *
     * @return 任务列表
     */
    @Select("SELECT * FROM t_extraction_task WHERE task_status = 'PENDING' ORDER BY priority DESC, create_time ASC")
    List<ExtractionTask> selectPendingTasks();

    /**
     * 根据内容源ID查询任务
     *
     * @param sourceId 内容源ID
     * @return 任务列表
     */
    @Select("SELECT * FROM t_extraction_task WHERE source_id = #{sourceId} ORDER BY create_time DESC")
    List<ExtractionTask> selectBySourceId(@Param("sourceId") String sourceId);
}
