package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员操作日志实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_admin_operation_log")
@ApiModel(description = "管理员操作日志实体")
public class AdminOperationLog {

    @TableId(value = "log_id", type = IdType.AUTO)
    @ApiModelProperty(value = "日志ID")
    private Long logId;

    @TableField("admin_id")
    @ApiModelProperty(value = "管理员ID")
    private Long adminId;

    @TableField("username")
    @ApiModelProperty(value = "管理员用户名")
    private String username;

    @TableField("operation_type")
    @ApiModelProperty(value = "操作类型：LOGIN, LOGOUT, CREATE, UPDATE, DELETE, AUDIT等")
    private String operationType;

    @TableField("module_name")
    @ApiModelProperty(value = "模块名称")
    private String moduleName;

    @TableField("operation_desc")
    @ApiModelProperty(value = "操作描述")
    private String operationDesc;

    @TableField("request_method")
    @ApiModelProperty(value = "请求方法：GET, POST, PUT, DELETE")
    private String requestMethod;

    @TableField("request_url")
    @ApiModelProperty(value = "请求URL")
    private String requestUrl;

    @TableField("request_params")
    @ApiModelProperty(value = "请求参数")
    private String requestParams;

    @TableField("response_result")
    @ApiModelProperty(value = "响应结果")
    private String responseResult;

    @TableField("ip_address")
    @ApiModelProperty(value = "IP地址")
    private String ipAddress;

    @TableField("execute_time")
    @ApiModelProperty(value = "执行时长（毫秒）")
    private Integer executeTime;

    @TableField("status")
    @ApiModelProperty(value = "状态：SUCCESS-成功, FAIL-失败")
    private String status;

    @TableField("error_message")
    @ApiModelProperty(value = "错误信息")
    private String errorMessage;

    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
