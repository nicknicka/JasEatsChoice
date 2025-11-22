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
 * 聊天消息实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_chat_msg")
@ApiModel(description = "聊天消息实体")
public class ChatMsg {

    @TableId
    @TableField("id")
    @ApiModelProperty(value = "消息ID")
    private Long id; // 消息ID

    @TableField("from_id")
    @ApiModelProperty(value = "发送者ID")
    private String fromId; // 发送者ID

    @TableField("to_id")
    @ApiModelProperty(value = "接收者ID")
    private String toId; // 接收者ID

    @TableField("msg_type")
    @ApiModelProperty(value = "消息类型（single/group/order_sync/order_status）")
    private String msgType; // 消息类型（single/group/order_sync/order_status）

    @TableField("content")
    @ApiModelProperty(value = "消息内容")
    private String content; // 消息内容

    @TableField("read_status")
    @ApiModelProperty(value = "已读状态：true-已读，false-未读")
    private Boolean readStatus; // 已读状态：true-已读，false-未读

    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime; // 创建时间
}
