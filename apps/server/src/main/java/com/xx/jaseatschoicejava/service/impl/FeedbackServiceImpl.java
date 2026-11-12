package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.config.FileUploadConfig;
import com.xx.jaseatschoicejava.dto.FeedbackAssociationUpdateDTO;
import com.xx.jaseatschoicejava.dto.FeedbackCreateDTO;
import com.xx.jaseatschoicejava.dto.FeedbackIssueDraftDTO;
import com.xx.jaseatschoicejava.dto.FeedbackTriageDTO;
import com.xx.jaseatschoicejava.entity.Admin;
import com.xx.jaseatschoicejava.entity.Feedback;
import com.xx.jaseatschoicejava.entity.FeedbackAttachment;
import com.xx.jaseatschoicejava.entity.FeedbackAudit;
import com.xx.jaseatschoicejava.entity.FeedbackIssueLink;
import com.xx.jaseatschoicejava.entity.FeedbackMessage;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.enums.FeedbackAuthorType;
import com.xx.jaseatschoicejava.enums.FeedbackMessageVisibility;
import com.xx.jaseatschoicejava.enums.FeedbackPriority;
import com.xx.jaseatschoicejava.enums.FeedbackResolutionCode;
import com.xx.jaseatschoicejava.enums.FeedbackStatus;
import com.xx.jaseatschoicejava.enums.FeedbackType;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.mapper.AdminMapper;
import com.xx.jaseatschoicejava.mapper.FeedbackAttachmentMapper;
import com.xx.jaseatschoicejava.mapper.FeedbackAuditMapper;
import com.xx.jaseatschoicejava.mapper.FeedbackMapper;
import com.xx.jaseatschoicejava.mapper.FeedbackMessageMapper;
import com.xx.jaseatschoicejava.service.FeedbackService;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.service.bridge.FeedbackIssueBridge;
import com.xx.jaseatschoicejava.vo.AdminFeedbackDetailVO;
import com.xx.jaseatschoicejava.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
    private AdminMapper adminMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private FeedbackIssueBridge feedbackIssueBridge;

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

    @Override
    public IPage<Feedback> adminList(long page, long size, String status, String type,
                                     String userId, String merchantId, String orderId, String priority) {
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<Feedback>()
                .eq(hasText(status), Feedback::getStatus, status)
                .eq(hasText(type), Feedback::getType, type)
                .eq(hasText(priority), Feedback::getPriority, priority)
                .eq(hasText(userId), Feedback::getUserId, userId)
                .eq(hasText(merchantId), Feedback::getMerchantId, merchantId)
                .eq(hasText(orderId), Feedback::getOrderId, orderId)
                .orderByDesc(Feedback::getCreatedAt);
        return page(new Page<>(safePage, safeSize), wrapper);
    }

    @Override
    public Map<String, Long> adminStatusCounts() {
        // 逐状态 count：不 SELECT status 列，避免全局 JsonTypeHandler 把状态串当 JSON 解析
        Map<String, Long> counts = new LinkedHashMap<>();
        for (FeedbackStatus status : FeedbackStatus.values()) {
            counts.put(status.getCode(), count(new LambdaQueryWrapper<Feedback>()
                    .eq(Feedback::getStatus, status.getCode())));
        }
        return counts;
    }

    @Override
    public Map<Long, String> resolveAssigneeNames(Collection<Long> assigneeIds) {
        List<Long> ids = assigneeIds == null ? List.of()
                : assigneeIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<Admin> admins = adminMapper.selectList(
                new LambdaQueryWrapper<Admin>().in(Admin::getAdminId, ids));
        Map<Long, String> names = new LinkedHashMap<>();
        for (Admin admin : admins) {
            names.put(admin.getAdminId(),
                    hasText(admin.getRealName()) ? admin.getRealName() : admin.getUsername());
        }
        return names;
    }

    @Override
    public AdminFeedbackDetailVO adminDetail(String feedbackId) {
        Feedback feedback = getById(feedbackId);
        if (feedback == null) {
            throw new BusinessException("404", "反馈不存在");
        }
        List<FeedbackMessage> messages = feedbackMessageMapper.selectList(new LambdaQueryWrapper<FeedbackMessage>()
                .eq(FeedbackMessage::getFeedbackId, feedbackId)
                .orderByAsc(FeedbackMessage::getCreatedAt));
        List<FeedbackAttachment> attachments = listAttachments(feedbackId);
        List<FeedbackAudit> audits = feedbackAuditMapper.selectList(new LambdaQueryWrapper<FeedbackAudit>()
                .eq(FeedbackAudit::getFeedbackId, feedbackId)
                .orderByAsc(FeedbackAudit::getCreatedAt));
        return AdminFeedbackDetailVO.from(feedback, messages, attachments, audits);
    }

    @Override
    public FeedbackMessage addAdminMessage(Long adminId, String feedbackId, String content, String visibility) {
        Feedback feedback = getById(feedbackId);
        if (feedback == null) {
            throw new BusinessException("404", "反馈不存在");
        }
        String body = content == null ? "" : content.trim();
        if (body.isEmpty()) {
            throw new BusinessException("400", "消息内容不能为空");
        }
        if (body.length() > CONTENT_MAX_LENGTH) {
            throw new BusinessException("400", "消息内容过长");
        }
        String visible = FeedbackMessageVisibility.INTERNAL.getCode()
                .equalsIgnoreCase(visibility == null ? "" : visibility.trim())
                ? FeedbackMessageVisibility.INTERNAL.getCode()
                : FeedbackMessageVisibility.USER.getCode();

        FeedbackMessage message = new FeedbackMessage();
        message.setFeedbackId(feedbackId);
        message.setAuthorType(FeedbackAuthorType.ADMIN.getCode());
        message.setAuthorId(adminId == null ? null : String.valueOf(adminId));
        message.setVisibility(visible);
        message.setContent(body);
        feedbackMessageMapper.insert(message);
        writeAudit(feedbackId, "REPLY", adminId, "visibility=" + visible);
        return message;
    }

    @Override
    public Feedback triage(Long adminId, String feedbackId, FeedbackTriageDTO dto) {
        Feedback feedback = getById(feedbackId);
        if (feedback == null) {
            throw new BusinessException("404", "反馈不存在");
        }
        String target = dto == null || dto.getStatus() == null ? "" : dto.getStatus().trim().toLowerCase();
        if (!TRIAGE_TARGETS.contains(target)) {
            throw new BusinessException("400", "只支持 needs_info / accepted / rejected");
        }
        String current = feedback.getStatus();
        if (!triageAllowed(current, target)) {
            throw new BusinessException("409", "非法状态流转：" + current + " -> " + target);
        }
        FeedbackPriority priority = null;
        if (hasText(dto.getPriority())) {
            priority = FeedbackPriority.fromCode(dto.getPriority());
            if (priority == null) {
                throw new BusinessException("400", "优先级只支持 normal / important / urgent");
            }
        }
        boolean accepted = FeedbackStatus.ACCEPTED.getCode().equals(target);
        var update = lambdaUpdate()
                .eq(Feedback::getFeedbackId, feedbackId)
                .set(Feedback::getStatus, target)
                .set(accepted, Feedback::getPlatformSystem,
                        accepted && Boolean.TRUE.equals(dto.getPlatformSystem()));
        if (priority != null) {
            update.set(Feedback::getPriority, priority.getCode());
        }
        if (dto.getAssigneeId() != null) {
            // 0 表示取消认领；其余按 sys_admin.admin_id 落库
            update.set(Feedback::getAssigneeId, dto.getAssigneeId() == 0L ? null : dto.getAssigneeId());
        }
        update.update();
        writeAudit(feedbackId, "TRIAGE", adminId, "status=" + current + "->" + target);
        return getById(feedbackId);
    }

    @Override
    public FeedbackIssueLink createIssue(Long adminId, String feedbackId, FeedbackIssueDraftDTO draft) {
        Feedback feedback = getById(feedbackId);
        if (feedback == null) {
            throw new BusinessException("404", "反馈不存在");
        }
        if (!FeedbackStatus.ACCEPTED.getCode().equals(feedback.getStatus())) {
            throw new BusinessException("409", "只有已接受的反馈才能创建 Issue");
        }
        if (!Boolean.TRUE.equals(feedback.getPlatformSystem())) {
            throw new BusinessException("409", "只有平台系统问题才能创建 Issue");
        }
        FeedbackIssueLink link = feedbackIssueBridge.createOrGet(feedback, draft);
        writeAudit(feedbackId, "BRIDGE", adminId, "issue=" + link.getRepo() + "#" + link.getIssueNumber());
        return link;
    }

    @Override
    public Feedback syncIssueState(String feedbackId, String state) {
        FeedbackIssueLink link = feedbackIssueBridge.syncState(feedbackId, state);
        Feedback feedback = getById(feedbackId);
        if (feedback == null) {
            throw new BusinessException("404", "反馈不存在");
        }
        if ("closed".equals(link.getState()) && FeedbackStatus.ACCEPTED.getCode().equals(feedback.getStatus())) {
            FeedbackMessage message = new FeedbackMessage();
            message.setFeedbackId(feedbackId);
            message.setAuthorType(FeedbackAuthorType.SYSTEM.getCode());
            message.setVisibility(FeedbackMessageVisibility.USER.getCode());
            message.setContent("该反馈对应的研发任务已完成处理。");
            feedbackMessageMapper.insert(message);
            lambdaUpdate()
                    .eq(Feedback::getFeedbackId, feedbackId)
                    .set(Feedback::getStatus, FeedbackStatus.RESOLVED.getCode())
                    .update();
            return getById(feedbackId);
        }
        return feedback;
    }

    @Override
    public Feedback resolve(Long adminId, String feedbackId, String resolutionCode) {
        Feedback feedback = getById(feedbackId);
        if (feedback == null) {
            throw new BusinessException("404", "反馈不存在");
        }
        FeedbackResolutionCode code = FeedbackResolutionCode.fromCode(resolutionCode);
        if (code == null) {
            throw new BusinessException("400", "处理结果编码不合法");
        }
        if (!FeedbackStatus.ACCEPTED.getCode().equals(feedback.getStatus())) {
            throw new BusinessException("409", "只有已接受的反馈才能解决");
        }
        lambdaUpdate()
                .eq(Feedback::getFeedbackId, feedbackId)
                .set(Feedback::getStatus, FeedbackStatus.RESOLVED.getCode())
                .set(Feedback::getResolutionCode, code.getCode())
                .update();
        writeAudit(feedbackId, "RESOLVE", adminId, "resolution=" + code.getCode());
        return getById(feedbackId);
    }

    private static final Set<String> TRIAGE_TARGETS =
            Set.of(FeedbackStatus.NEEDS_INFO.getCode(), FeedbackStatus.ACCEPTED.getCode(), FeedbackStatus.REJECTED.getCode());

    private static boolean triageAllowed(String current, String target) {
        FeedbackStatus from = FeedbackStatus.fromCode(current);
        if (from == null) {
            return false;
        }
        return switch (from) {
            case PENDING, TRIAGING -> FeedbackStatus.NEEDS_INFO.getCode().equals(target)
                    || FeedbackStatus.ACCEPTED.getCode().equals(target)
                    || FeedbackStatus.REJECTED.getCode().equals(target);
            case NEEDS_INFO -> FeedbackStatus.ACCEPTED.getCode().equals(target)
                    || FeedbackStatus.REJECTED.getCode().equals(target);
            default -> false;
        };
    }

    private void writeAudit(String feedbackId, String action, Long adminId, String detail) {
        FeedbackAudit audit = new FeedbackAudit();
        audit.setFeedbackId(feedbackId);
        audit.setAction(action);
        audit.setActorType("ADMIN");
        audit.setActorId(adminId == null ? null : String.valueOf(adminId));
        audit.setDetail(detail);
        feedbackAuditMapper.insert(audit);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
