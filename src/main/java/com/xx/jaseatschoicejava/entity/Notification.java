package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统通知实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_notification")
@ApiModel(description = "系统通知实体")
public class Notification {

    @TableId
    @TableField("id")
    @ApiModelProperty(value = "通知ID")
    private Long id; // 通知ID

    @TableField("user_id")
    @ApiModelProperty(value = "用户ID")
    private Long userId; // 用户ID

    @TableField("title")
    @ApiModelProperty(value = "通知标题")
    private String title; // 通知标题

    @TableField("content")
    @ApiModelProperty(value = "通知内容")
    private String content; // 通知内容

    @TableField("read_status")
    @ApiModelProperty(value = "已读状态：true-已读，false-未读")
    private Boolean readStatus; // 已读状态：true-已读，false-未读

    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime; // 创建时间
}
