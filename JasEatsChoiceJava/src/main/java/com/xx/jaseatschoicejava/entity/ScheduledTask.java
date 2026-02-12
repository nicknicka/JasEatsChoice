package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时任务实体
 *
 * @author Claude
 * @since 2025-02-12
 */
@Data
@TableName("t_scheduled_task")
@ApiModel(description = "定时任务")
public class ScheduledTask {

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "任务ID")
    private String id;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "任务代码（用于标识和执行）")
    private String taskCode;

    @ApiModelProperty(value = "任务描述")
    private String description;

    @ApiModelProperty(value = "Cron表达式")
    private String cronExpression;

    @ApiModelProperty(value = "任务类型：ONCE-一次性任务, CRON-Cron表达式任务, FIXED_RATE-固定频率任务, FIXED_DELAY-固定延迟任务")
    private String taskType;

    @ApiModelProperty(value = "执行频率（毫秒），用于固定频率/延迟任务")
    private Long rateInMillis;

    @ApiModelProperty(value = "任务状态：ENABLED-启用, DISABLED-禁用, PAUSED-暂停")
    private String status;

    @ApiModelProperty(value = "任务类名（实现类）")
    private String taskClassName;

    @ApiModelProperty(value = "任务方法名")
    private String taskMethodName;

    @ApiModelProperty(value = "任务参数（JSON格式）")
    private String taskParams;

    @ApiModelProperty(value = "上次执行时间")
    private LocalDateTime lastExecuteTime;

    @ApiModelProperty(value = "下次执行时间")
    private LocalDateTime nextExecuteTime;

    @ApiModelProperty(value = "执行次数")
    private Integer executeCount;

    @ApiModelProperty(value = "失败次数")
    private Integer failCount;

    @ApiModelProperty(value = "最后一次执行结果：SUCCESS-成功, FAILED-失败, RUNNING-运行中")
    private String lastExecuteResult;

    @ApiModelProperty(value = "最后一次执行错误信息")
    private String lastErrorMessage;

    @ApiModelProperty(value = "是否记录执行日志")
    private Boolean logExecution;

    @ApiModelProperty(value = "超时时间（秒），0表示不限制")
    private Integer timeoutSeconds;

    @ApiModelProperty(value = "重试次数")
    private Integer retryCount;

    @ApiModelProperty(value = "已重试次数")
    private Integer retriedCount;

    @ApiModelProperty(value = "任务分组")
    private String taskGroup;

    @ApiModelProperty(value = "优先级：1-高, 2-中, 3-低")
    private Integer priority;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "创建人")
    private String createdBy;

    @ApiModelProperty(value = "更新人")
    private String updatedBy;
}
