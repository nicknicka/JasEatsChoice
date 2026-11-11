package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 反馈与 GitHub Issue 的一对一关联，是幂等创建的事实来源。
 */
@Data
@TableName("fb_feedback_issue_link")
public class FeedbackIssueLink {

    @TableId(type = IdType.ASSIGN_ID)
    private String linkId;

    private String feedbackId;

    private String repo;

    private String issueNumber;

    private String issueUrl;

    /** open / closed。 */
    private String state;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
