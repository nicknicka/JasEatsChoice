package com.xx.jaseatschoicejava.vo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.entity.Feedback;
import com.xx.jaseatschoicejava.entity.FeedbackAttachment;
import com.xx.jaseatschoicejava.entity.FeedbackAudit;
import com.xx.jaseatschoicejava.entity.FeedbackMessage;
import com.xx.jaseatschoicejava.enums.FeedbackResolutionCode;
import com.xx.jaseatschoicejava.enums.FeedbackStatus;
import com.xx.jaseatschoicejava.enums.FeedbackType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理员反馈详情：包含内部备注、全部消息、附件与审计痕迹。
 */
@Data
public class AdminFeedbackDetailVO {

    private String feedbackId;
    private String userId;
    private String type;
    private String typeLabel;
    private String content;
    private String status;
    private String statusLabel;
    private String resolutionCode;
    private String orderId;
    private String merchantId;
    private String contact;
    private Map<String, Object> deviceInfo;
    private String environment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<FeedbackMessageVO> messages;
    private List<FeedbackAttachmentVO> attachments;
    private List<AuditVO> audits;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Data
    public static class AuditVO {
        private String action;
        private String actorType;
        private String actorId;
        private String detail;
        private LocalDateTime createdAt;

        static AuditVO from(FeedbackAudit audit) {
            AuditVO vo = new AuditVO();
            vo.action = audit.getAction();
            vo.actorType = audit.getActorType();
            vo.actorId = audit.getActorId();
            vo.detail = audit.getDetail();
            vo.createdAt = audit.getCreatedAt();
            return vo;
        }
    }

    public static AdminFeedbackDetailVO from(Feedback feedback, List<FeedbackMessage> messages,
                                             List<FeedbackAttachment> attachments, List<FeedbackAudit> audits) {
        AdminFeedbackDetailVO vo = new AdminFeedbackDetailVO();
        vo.feedbackId = feedback.getFeedbackId();
        vo.userId = feedback.getUserId();
        vo.type = feedback.getType();
        FeedbackType type = FeedbackType.fromCode(feedback.getType());
        vo.typeLabel = type == null ? feedback.getType() : type.getLabel();
        vo.content = feedback.getContent();
        vo.status = feedback.getStatus();
        FeedbackStatus status = FeedbackStatus.fromCode(feedback.getStatus());
        vo.statusLabel = status == null ? feedback.getStatus() : status.getLabel();
        FeedbackResolutionCode code = FeedbackResolutionCode.fromCode(feedback.getResolutionCode());
        vo.resolutionCode = code == null ? feedback.getResolutionCode() : code.getCode();
        vo.orderId = feedback.getOrderId();
        vo.merchantId = feedback.getMerchantId();
        vo.contact = feedback.getContact();
        vo.deviceInfo = parse(feedback.getDeviceInfo());
        vo.environment = feedback.getEnvironment();
        vo.createdAt = feedback.getCreatedAt();
        vo.updatedAt = feedback.getUpdatedAt();
        vo.messages = messages == null ? List.of() : messages.stream().map(FeedbackMessageVO::from).toList();
        vo.attachments = attachments == null ? List.of() : attachments.stream().map(FeedbackAttachmentVO::from).toList();
        vo.audits = audits == null ? List.of() : audits.stream().map(AuditVO::from).toList();
        return vo;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parse(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, Map.class);
        } catch (Exception ignored) {
            return null;
        }
    }
}
