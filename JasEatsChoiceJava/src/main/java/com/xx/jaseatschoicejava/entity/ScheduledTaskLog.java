package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时任务执行日志
 *
 * @author Claude
 * @since 2025-02-12
 */
@Data
@TableName("t_scheduled_task_log")
@ApiModel(description = "定时任务执行日志")
public class ScheduledTaskLog {

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "日志ID")
    private String id;

    @ApiModelProperty(value = "任务ID")
    private String taskId;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "任务代码")
    private String taskCode;

    @ApiModelProperty(value = "执行开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "执行结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "执行时长（毫秒）")
    private Long duration;

    @ApiModelProperty(value = "执行状态：SUCCESS-成功, FAILED-失败, TIMEOUT-超时")
    private String executeStatus;

    @ApiModelProperty(value = "执行结果信息")
    private String resultMessage;

    @ApiModelProperty(value = "错误信息")
    private String errorMessage;

    @ApiModelProperty(value = "异常堆栈")
    private String exceptionStack;

    @ApiModelProperty(value = "执行参数（JSON格式）")
    private String executeParams;

    @ApiModelProperty(value = "执行返回结果（JSON格式）")
    private String executeResult;

    @ApiModelProperty(value = "服务器IP")
    private String serverIp;

    @ApiModelProperty(value = "服务器主机名")
    private String serverHostname;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
