package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * 聊天会话实体类（优化版）
 * 优化点：
 * 1. 使用自增ID作为主键（MyBatis-Plus要求）
 * 2. session_id和user_id作为业务唯一键，添加唯一索引
 * 3. 保留user_id用于区分不同用户的会话视图（置顶、未读数等）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_chat_session")
@ApiModel(description = "聊天会话实体")
public class ChatSession {

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @TableField("session_id")
    @ApiModelProperty(value = "会话标识（业务唯一键的一部分）")
    private String sessionId; // 会话标识（私聊为双方用户ID组合，群聊为群ID）

    @TableField("user_id")
    @ApiModelProperty(value = "用户ID（业务唯一键的一部分）")
    private String userId; // 用户ID

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
    private Integer pinned; // 是否置顶（0-未置顶，1-置顶）

    @TableField("member_count")
    @ApiModelProperty(value = "成员数量（群聊）")
    private Integer memberCount; // 成员数量

    @TableField("group_id")
    @ApiModelProperty(value = "群组ID（仅群聊会话有效）")
    private String groupId; // 群组ID（仅群聊会话有值）

    @TableField("target_id")
    @ApiModelProperty(value = "目标用户ID（仅单聊会话有效，表示对方的userId）")
    private String targetId; // 目标用户ID（仅单聊会话有值）

    @TableField("related_order_id")
    @ApiModelProperty(value = "关联的群订单ID（群订单专属会话有效）")
    private String relatedOrderId; // 关联的群订单ID

    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime; // 创建时间

    @TableField("update_time")
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime; // 更新时间
}
