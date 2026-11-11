package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.config.FileUploadConfig;
import com.xx.jaseatschoicejava.dto.FeedbackAssociationUpdateDTO;
import com.xx.jaseatschoicejava.dto.FeedbackCreateDTO;
import com.xx.jaseatschoicejava.entity.Feedback;
import com.xx.jaseatschoicejava.entity.FeedbackAttachment;
import com.xx.jaseatschoicejava.entity.FeedbackAudit;
import com.xx.jaseatschoicejava.entity.FeedbackMessage;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.enums.FeedbackAuthorType;
import com.xx.jaseatschoicejava.enums.FeedbackMessageVisibility;
import com.xx.jaseatschoicejava.enums.FeedbackStatus;
import com.xx.jaseatschoicejava.enums.FeedbackType;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.mapper.FeedbackAttachmentMapper;
import com.xx.jaseatschoicejava.mapper.FeedbackAuditMapper;
import com.xx.jaseatschoicejava.mapper.FeedbackMapper;
import com.xx.jaseatschoicejava.mapper.FeedbackMessageMapper;
import com.xx.jaseatschoicejava.service.FeedbackService;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户反馈应用服务实现：提交、消息、附件、归属校验、审计。
 */
@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    private static final int CONTENT_MAX_LENGTH = 2000;
    private static final int CONTACT_MAX_LENGTH = 128;
    private static final int ATTACHMENT_MAX_COUNT = 3;
    private static final long ATTACHMENT_MAX_SIZE = 5L * 1024 * 1024;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private FeedbackMessageMapper feedbackMessageMapper;

    @Autowired
    private FeedbackAttachmentMapper feedbackAttachmentMapper;

    @Autowired
    private FeedbackAuditMapper feedbackAuditMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private FileUploadConfig fileUploadConfig;

    @Override
    public Feedback create(String userId, FeedbackCreateDTO dto) {
        requireIdentity(userId);
        FeedbackType type = FeedbackType.fromCode(dto == null ? null : dto.getType());
        if (type == null) {
            throw new BusinessException("400", "反馈类型不合法");
        }
        String content = dto.getContent() == null ? "" : dto.getContent().trim();
        if (content.isEmpty()) {
            throw new BusinessException("400", "反馈内容不能为空");
        }
        if (content.length() > CONTENT_MAX_LENGTH) {
            throw new BusinessException("400", "反馈内容过长");
        }
        String contact = trimToNull(dto.getContact());
        if (contact != null && contact.length() > CONTACT_MAX_LENGTH) {
            throw new BusinessException("400", "联系方式过长");
        }

        String orderId = trimToNull(dto.getOrderId());
        String merchantId = trimToNull(dto.getMerchantId());
        // 关联订单必须属于当前用户；可从订单反推商家。
        if (orderId != null) {
            Order order = orderService.getById(orderId);
            if (order == null || !userId.equals(order.getUserId())) {
                throw new BusinessException("400", "订单不存在或不属于当前用户");
            }
            if (merchantId == null) {
                merchantId = order.getMerchantId();
            }
        }

        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setType(type.getCode());
        feedback.setContent(content);
        feedback.setStatus(FeedbackStatus.PENDING.getCode());
        feedback.setContact(contact);
        feedback.setOrderId(orderId);
        feedback.setMerchantId(merchantId);
        feedback.setDeviceInfo(writeDeviceInfo(dto.getDeviceInfo()));
        feedback.setEnvironment(deriveEnvironment(dto.getDeviceInfo()));

        save(feedback);
        return feedback;
    }

    @Override
    public IPage<Feedback> listOwn(String userId, long page, long size, String type, String status) {
        requireIdentity(userId);
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), 50);
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<Feedback>()
                .eq(Feedback::getUserId, userId)
                .eq(type != null && !type.isBlank(), Feedback::getType, type)
                .eq(status != null && !status.isBlank(), Feedback::getStatus, status)
                .orderByDesc(Feedback::getCreatedAt);
        return page(new Page<>(safePage, safeSize), wrapper);
    }

    @Override
    public Feedback getOwn(String userId, String feedbackId) {
        requireIdentity(userId);
        Feedback feedback = getById(feedbackId);
        if (feedback == null) {
            throw new BusinessException("404", "反馈不存在");
        }
        // 不存在与不属于当前用户返回同一结果，避免泄露反馈是否存在。
        if (!userId.equals(feedback.getUserId())) {
            throw new BusinessException("404", "反馈不存在");
        }
        return feedback;
    }

    @Override
    public List<FeedbackMessage> listVisibleMessages(String feedbackId) {
        return feedbackMessageMapper.selectList(new LambdaQueryWrapper<FeedbackMessage>()
                .eq(FeedbackMessage::getFeedbackId, feedbackId)
                .eq(FeedbackMessage::getVisibility, FeedbackMessageVisibility.USER.getCode())
                .orderByAsc(FeedbackMessage::getCreatedAt));
    }

    @Override
    public List<FeedbackAttachment> listAttachments(String feedbackId) {
        return feedbackAttachmentMapper.selectList(new LambdaQueryWrapper<FeedbackAttachment>()
                .eq(FeedbackAttachment::getFeedbackId, feedbackId)
                .orderByAsc(FeedbackAttachment::getCreatedAt));
    }

    @Override
    public FeedbackMessage appendUserMessage(String userId, String feedbackId, String content) {
        getOwn(userId, feedbackId);
        String body = content == null ? "" : content.trim();
        if (body.isEmpty()) {
            throw new BusinessException("400", "消息内容不能为空");
        }
        if (body.length() > CONTENT_MAX_LENGTH) {
            throw new BusinessException("400", "消息内容过长");
        }
        FeedbackMessage message = new FeedbackMessage();
        message.setFeedbackId(feedbackId);
        message.setAuthorType(FeedbackAuthorType.USER.getCode());
        message.setAuthorId(userId);
        message.setVisibility(FeedbackMessageVisibility.USER.getCode());
        message.setContent(body);
        feedbackMessageMapper.insert(message);
        return message;
    }

    @Override
    public FeedbackAttachment addAttachment(String userId, String feedbackId, MultipartFile file) {
        getOwn(userId, feedbackId);
        if (file == null || file.isEmpty()) {
            throw new BusinessException("400", "附件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("400", "只支持图片附件");
        }
        if (file.getSize() > ATTACHMENT_MAX_SIZE) {
            throw new BusinessException("400", "图片不能超过5MB");
        }
        Long existing = feedbackAttachmentMapper.selectCount(new LambdaQueryWrapper<FeedbackAttachment>()
                .eq(FeedbackAttachment::getFeedbackId, feedbackId));
        if (existing != null && existing >= ATTACHMENT_MAX_COUNT) {
            throw new BusinessException("400", "最多上传3张图片");
        }

        String relativePath;
        try {
            relativePath = FileUploadUtil.uploadImage(file, "feedback", userId);
        } catch (IOException e) {
            throw new BusinessException("500", "附件上传失败");
        }

        FeedbackAttachment attachment = new FeedbackAttachment();
        attachment.setFeedbackId(feedbackId);
        attachment.setUploaderId(userId);
        attachment.setUrl(fileUploadConfig.getUrlPrefix() + relativePath);
        attachment.setOriginalName(file.getOriginalFilename());
        attachment.setContentType(contentType);
        attachment.setFileSize(file.getSize());
        feedbackAttachmentMapper.insert(attachment);
        return attachment;
    }

    @Override
    public Feedback updateAssociation(Long adminId, String feedbackId, FeedbackAssociationUpdateDTO dto) {
        Feedback feedback = getById(feedbackId);
        if (feedback == null) {
            throw new BusinessException("404", "反馈不存在");
        }
        String orderId = trimToNull(dto.getOrderId());
        String merchantId = trimToNull(dto.getMerchantId());
        if (orderId != null) {
            Order order = orderService.getById(orderId);
            if (order == null) {
                throw new BusinessException("400", "订单不存在");
            }
            if (merchantId == null) {
                merchantId = order.getMerchantId();
            }
        }

        String detail = "orderId=" + feedback.getOrderId() + "->" + orderId
                + "; merchantId=" + feedback.getMerchantId() + "->" + merchantId;
        FeedbackAudit audit = new FeedbackAudit();
        audit.setFeedbackId(feedbackId);
        audit.setAction("ASSOCIATION");
        audit.setActorType("ADMIN");
        audit.setActorId(adminId == null ? null : String.valueOf(adminId));
        audit.setDetail(detail);
        feedbackAuditMapper.insert(audit);

        lambdaUpdate()
                .eq(Feedback::getFeedbackId, feedbackId)
                .set(Feedback::getOrderId, orderId)
                .set(Feedback::getMerchantId, merchantId)
                .update();
        return getById(feedbackId);
    }

    private static void requireIdentity(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new BusinessException("401", "未登录");
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String writeDeviceInfo(Map<String, Object> deviceInfo) {
        if (deviceInfo == null || deviceInfo.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(deviceInfo);
        } catch (Exception e) {
            throw new BusinessException("400", "设备元数据格式不合法");
        }
    }

    private static String deriveEnvironment(Map<String, Object> deviceInfo) {
        if (deviceInfo == null || deviceInfo.isEmpty()) {
            return null;
        }
        Map<String, String> parts = new LinkedHashMap<>();
        for (String key : new String[]{"platform", "system", "os", "appVersion", "version"}) {
            Object value = deviceInfo.get(key);
            if (value != null && !value.toString().isBlank()) {
                parts.put(key, value.toString());
            }
        }
        if (parts.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        parts.forEach((k, v) -> {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(k).append('=').append(v);
        });
        return sb.toString();
    }
}
