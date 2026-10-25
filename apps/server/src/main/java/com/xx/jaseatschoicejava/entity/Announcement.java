package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商家公告实体类
 */
@Data
@TableName("announcement")
public class Announcement {
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "公告ID")
    private String id;

    @ApiModelProperty(value = "商家ID，NULL表示系统公告")
    private String merchantId;

    @ApiModelProperty(value = "公告标题")
    private String title;

    @ApiModelProperty(value = "公告内容")
    private String content;

    @ApiModelProperty(value = "公告类型：system-系统公告, activity-活动公告, urgent-紧急公告, update-更新说明")
    private String type;

    @ApiModelProperty(value = "优先级：normal-普通, important-重要, urgent-紧急")
    private String priority;

    @ApiModelProperty(value = "目标群体：all-全部用户, merchant-商家端, customer-用户端")
    private String targetAudience;

    @ApiModelProperty(value = "阅读量")
    private Long readCount;

    @ApiModelProperty(value = "阅读人数")
    private Long readUsers;

    @ApiModelProperty(value = "公告状态")
    private String status; // active: 启用, draft: 草稿, inactive: 禁用

    @ApiModelProperty(value = "开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
