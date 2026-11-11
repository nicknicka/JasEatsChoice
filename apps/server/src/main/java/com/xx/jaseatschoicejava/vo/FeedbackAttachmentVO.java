package com.xx.jaseatschoicejava.vo;

import com.xx.jaseatschoicejava.entity.FeedbackAttachment;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 反馈附件视图。
 */
@Data
public class FeedbackAttachmentVO {

    private String attachmentId;
    private String url;
    private String originalName;
    private String contentType;
    private Long fileSize;
    private LocalDateTime createdAt;

    public static FeedbackAttachmentVO from(FeedbackAttachment attachment) {
        FeedbackAttachmentVO vo = new FeedbackAttachmentVO();
        vo.attachmentId = attachment.getAttachmentId();
        vo.url = attachment.getUrl();
        vo.originalName = attachment.getOriginalName();
        vo.contentType = attachment.getContentType();
        vo.fileSize = attachment.getFileSize();
        vo.createdAt = attachment.getCreatedAt();
        return vo;
    }
}
