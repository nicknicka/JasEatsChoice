package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 反馈消息：首次描述之外的用户追加、管理员回复与内部备注。
 */
@Data
@TableName("fb_feedback_message")
public class FeedbackMessage {

    @TableId(type = IdType.ASSIGN_ID)
    private String messageId;

    private String feedbackId;

    /** USER / ADMIN / SYSTEM。 */
    private String authorType;

    private String authorId;

    /** USER 用户可见 / INTERNAL 内部备注。 */
    private String visibility;

    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
