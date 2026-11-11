package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 反馈附件元数据。文件本体落在 Git 外的运行时上传目录。
 */
@Data
@TableName("fb_feedback_attachment")
public class FeedbackAttachment {

    @TableId(type = IdType.ASSIGN_ID)
    private String attachmentId;

    private String feedbackId;

    private String messageId;

    private String uploaderId;

    /** 相对访问路径（不含服务器地址）。 */
    private String url;

    private String originalName;

    private String contentType;

    private Long fileSize;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
