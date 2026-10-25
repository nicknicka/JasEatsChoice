package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统日志实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_system_log")
@ApiModel(description = "系统日志实体")
public class SystemLog {

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "日志ID")
    private String logId;

    @ApiModelProperty(value = "操作类型：LOGIN-登录, LOGOUT-登出, CREATE-创建, UPDATE-更新, DELETE-删除, QUERY-查询, EXPORT-导出, OTHER-其他")
    private String operationType;

    @ApiModelProperty(value = "操作模块：USER-用户管理, MERCHANT-商家管理, ORDER-订单管理, DISH-菜品管理, ADMIN-管理员管理, FINANCE-财务管理, STATISTICS-统计管理, SYSTEM-系统管理")
    private String module;

    @ApiModelProperty(value = "操作描述")
    private String description;

    @ApiModelProperty(value = "操作人ID")
    private Long operatorId;

    @ApiModelProperty(value = "操作人名称")
    private String operatorName;

    @ApiModelProperty(value = "操作人类型：ADMIN-管理员, USER-用户, MERCHANT-商家, SYSTEM-系统")
    private String operatorType;

    @ApiModelProperty(value = "请求方法")
    private String method;

    @ApiModelProperty(value = "请求参数")
    private String params;

    @ApiModelProperty(value = "返回结果")
    private String result;

    @ApiModelProperty(value = "执行时长(毫秒)")
    private Long executeTime;

    @ApiModelProperty(value = "IP地址")
    private String ip;

    @ApiModelProperty(value = "操作状态：SUCCESS-成功, FAILED-失败")
    private String status;

    @ApiModelProperty(value = "错误信息")
    private String errorMessage;

    @ApiModelProperty(value = "浏览器类型")
    private String browser;

    @ApiModelProperty(value = "操作系统")
    private String os;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
