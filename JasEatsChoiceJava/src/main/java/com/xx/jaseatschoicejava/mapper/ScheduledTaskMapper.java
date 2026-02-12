package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.ScheduledTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 定时任务Mapper
 *
 * @author Claude
 * @since 2025-02-12
 */
@Mapper
public interface ScheduledTaskMapper extends BaseMapper<ScheduledTask> {
}
