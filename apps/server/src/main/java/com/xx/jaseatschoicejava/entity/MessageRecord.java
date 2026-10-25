package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息记录实体
 *
 * @author xx
 * @date 2025-01-26
 */
@Data
@TableName("t_message_record")
public class MessageRecord {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 发送者ID
     */
    private String senderId;

    /**
     * 接收者ID
     */
    private String receiverId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型 (text: 文本, image: 图片, file: 文件)
     */
    private String messageType = "text";

    /**
     * 发送时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime sendTime;

    /**
     * 阅读状态 (0: 未读, 1: 已读)
     */
    private Integer readStatus = 0;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
