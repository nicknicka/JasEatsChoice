package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 反馈操作审计：关联修正、分诊、解决、回复都留痕。
 */
@Data
@TableName("fb_feedback_audit")
public class FeedbackAudit {

    @TableId(type = IdType.ASSIGN_ID)
    private String auditId;

    private String feedbackId;

    /** ASSOCIATION / TRIAGE / RESOLVE / REPLY。 */
    private String action;

    /** ADMIN / SYSTEM。 */
    private String actorType;

    private String actorId;

    private String detail;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
