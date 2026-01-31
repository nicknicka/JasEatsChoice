package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提取任务实体类
 *
 * @author Claude
 * @since 2025-01-31
 */
@Data
@TableName("t_extraction_task")
public class ExtractionTask {

    /**
     * 主键ID
     */
    private String id;

    /**
     * 内容源ID
     */
    private String sourceId;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 任务状态
     */
    private String taskStatus;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 结果数据（JSON格式）
     */
    private String resultData;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
