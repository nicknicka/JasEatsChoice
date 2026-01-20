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
 * 聊天消息实体类（优化版）
 * 优化点：
 * 1. id改为msg_id，语义更清晰
 * 2. 移除reply_content、reply_from_id、reply_from_name冗余字段
 * 3. 只保留reply_to引用字段，通过关联查询获取回复内容
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_chat_msg")
@ApiModel(description = "聊天消息实体")
public class ChatMsg {

    @TableId(type = com.baomidou.mybatisplus.annotation.IdType.INPUT)
    @ApiModelProperty(value = "消息ID")
    private String msgId; // 消息ID（主键）

    @TableField("from_id")
    @ApiModelProperty(value = "发送者ID")
    private String fromId; // 发送者ID

    @TableField("to_id")
    @ApiModelProperty(value = "接收者ID（群聊时为NULL）")
    private String toId; // 接收者ID

    @TableField("session_id")
    @ApiModelProperty(value = "会话ID")
    private String sessionId; // 会话ID

    @TableField("msg_type")
    @ApiModelProperty(value = "消息类型：single/group/order_sync/order_status")
    private String msgType; // 消息类型

    @TableField("content")
    @ApiModelProperty(value = "消息内容")
    private String content; // 消息内容

    @TableField("reply_to")
    @ApiModelProperty(value = "回复的消息ID（引用原消息，不存储冗余内容）")
    private String replyTo; // 回复的消息ID

    @TableField("read_status")
    @ApiModelProperty(value = "已读状态：0-未读，1-已读")
    private Integer readStatus; // 已读状态（改为Integer类型，与数据库TINYINT对应）

    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime; // 创建时间

    // ============ 非数据库字段（用于前端展示）============

    @TableField(exist = false)
    @ApiModelProperty(value = "被回复消息内容（关联查询获得）")
    private String replyContent; // 被回复消息内容

    @TableField(exist = false)
    @ApiModelProperty(value = "被回复消息的发送者ID（关联查询获得）")
    private String replyFromId; // 被回复消息的发送者ID

    @TableField(exist = false)
    @ApiModelProperty(value = "被回复消息的发送者名称（关联查询获得）")
    private String replyFromName; // 被回复消息的发送者名称

    @TableField(exist = false)
    @ApiModelProperty(value = "发送者名称（关联查询获得）")
    private String fromName; // 发送者名称

    @TableField(exist = false)
    @ApiModelProperty(value = "发送者头像（关联查询获得）")
    private String fromAvatar; // 发送者头像
}
