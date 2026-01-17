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
 * 聊天会话实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_chat_session")
@ApiModel(description = "聊天会话实体")
public class ChatSession {

    @TableId(type = com.baomidou.mybatisplus.annotation.IdType.ASSIGN_ID)
    @ApiModelProperty(value = "会话ID")
    private String id; // 会话ID

    @TableField("user_id")
    @ApiModelProperty(value = "用户ID")
    private String userId; // 用户ID

    @TableField("session_id")
    @ApiModelProperty(value = "会话标识(私聊为对方用户ID,群聊为群ID)")
    private String sessionId; // 会话标识

    @TableField("session_type")
    @ApiModelProperty(value = "会话类型: single-私聊, group-群聊")
    private String sessionType; // 会话类型

    @TableField("session_name")
    @ApiModelProperty(value = "会话名称")
    private String sessionName; // 会话名称

    @TableField("avatar")
    @ApiModelProperty(value = "会话头像")
    private String avatar; // 会话头像

    @TableField("last_message")
    @ApiModelProperty(value = "最后一条消息内容")
    private String lastMessage; // 最后一条消息

    @TableField("last_message_time")
    @ApiModelProperty(value = "最后一条消息时间")
    private LocalDateTime lastMessageTime; // 最后消息时间

    @TableField("unread_count")
    @ApiModelProperty(value = "未读消息数")
    private Integer unreadCount; // 未读消息数

    @TableField("pinned")
    @ApiModelProperty(value = "是否置顶")
    private Boolean pinned; // 是否置顶

    @TableField("member_count")
    @ApiModelProperty(value = "成员数量(群聊)")
    private Integer memberCount; // 成员数量

    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime; // 创建时间

    @TableField("update_time")
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime; // 更新时间
}
