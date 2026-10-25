package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * AI聊天历史实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_ai_chat_history")
@ApiModel(description = "AI聊天历史实体")
public class AIChatHistory {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "用户ID")
    @TableField("user_id")
    private String userId;

    @ApiModelProperty(value = "发送者：user-用户，ai-AI助手")
    @TableField("sender")
    private String sender;

    @ApiModelProperty(value = "消息内容")
    @TableField("content")
    private String content;

    @ApiModelProperty(value = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "消息类型（用于卡片显示）")
    @TableField("message_type")
    private String messageType;

    @ApiModelProperty(value = "卡片数据（JSON格式）")
    @TableField("card_data")
    private String cardData;
}
