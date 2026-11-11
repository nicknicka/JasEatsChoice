package com.xx.jaseatschoicejava.vo;

import com.xx.jaseatschoicejava.entity.FeedbackMessage;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 反馈消息视图。仅承载调用方有权看到的消息。
 */
@Data
public class FeedbackMessageVO {

    private String messageId;
    private String authorType;
    private String visibility;
    private String content;
    private LocalDateTime createdAt;

    public static FeedbackMessageVO from(FeedbackMessage message) {
        FeedbackMessageVO vo = new FeedbackMessageVO();
        vo.messageId = message.getMessageId();
        vo.authorType = message.getAuthorType();
        vo.visibility = message.getVisibility();
        vo.content = message.getContent();
        vo.createdAt = message.getCreatedAt();
        return vo;
    }
}
