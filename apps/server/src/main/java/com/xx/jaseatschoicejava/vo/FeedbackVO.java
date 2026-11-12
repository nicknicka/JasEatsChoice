package com.xx.jaseatschoicejava.vo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.entity.Feedback;
import com.xx.jaseatschoicejava.enums.FeedbackResolutionCode;
import com.xx.jaseatschoicejava.enums.FeedbackStatus;
import com.xx.jaseatschoicejava.enums.FeedbackType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户可见的反馈视图。
 *
 * <p>只承载用户自己能看到的信息：不暴露内部备注、分诊审计与 GitHub 关联细节。
 */
@Data
public class FeedbackVO {

    private String feedbackId;
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

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static FeedbackVO from(Feedback feedback) {
        return from(feedback, List.of(), List.of());
    }

    public static FeedbackVO from(Feedback feedback, List<FeedbackMessageVO> messages,
                                  List<FeedbackAttachmentVO> attachments) {
        FeedbackVO vo = new FeedbackVO();
        vo.fill(feedback);
        vo.messages = messages == null ? List.of() : messages;
        vo.attachments = attachments == null ? List.of() : attachments;
        return vo;
    }

    /**
     * 把用户可见字段填进来。子类（管理端视图）复用它，避免两处逻辑漂移。
     */
    protected void fill(Feedback feedback) {
        this.feedbackId = feedback.getFeedbackId();
        this.type = feedback.getType();
        FeedbackType type = FeedbackType.fromCode(feedback.getType());
        this.typeLabel = type == null ? feedback.getType() : type.getLabel();
        this.content = feedback.getContent();
        this.status = feedback.getStatus();
        FeedbackStatus status = FeedbackStatus.fromCode(feedback.getStatus());
        this.statusLabel = status == null ? feedback.getStatus() : status.getLabel();
        FeedbackResolutionCode code = FeedbackResolutionCode.fromCode(feedback.getResolutionCode());
        this.resolutionCode = code == null ? feedback.getResolutionCode() : code.getCode();
        this.orderId = feedback.getOrderId();
        this.merchantId = feedback.getMerchantId();
        this.contact = feedback.getContact();
        this.deviceInfo = parseDeviceInfo(feedback.getDeviceInfo());
        this.environment = feedback.getEnvironment();
        this.createdAt = feedback.getCreatedAt();
        this.updatedAt = feedback.getUpdatedAt();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseDeviceInfo(String json) {
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
