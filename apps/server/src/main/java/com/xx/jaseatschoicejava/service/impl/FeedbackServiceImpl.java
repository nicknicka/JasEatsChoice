package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.dto.FeedbackCreateDTO;
import com.xx.jaseatschoicejava.entity.Feedback;
import com.xx.jaseatschoicejava.enums.FeedbackStatus;
import com.xx.jaseatschoicejava.enums.FeedbackType;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.mapper.FeedbackMapper;
import com.xx.jaseatschoicejava.service.FeedbackService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 用户反馈应用服务实现：提交、自己的历史查询、归属校验。
 */
@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    private static final int CONTENT_MAX_LENGTH = 2000;
    private static final int CONTACT_MAX_LENGTH = 128;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Feedback create(String userId, FeedbackCreateDTO dto) {
        if (userId == null || userId.isBlank()) {
            throw new BusinessException("401", "未登录");
        }
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
        String contact = dto.getContact() == null ? null : dto.getContact().trim();
        if (contact != null && contact.length() > CONTACT_MAX_LENGTH) {
            throw new BusinessException("400", "联系方式过长");
        }

        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setType(type.getCode());
        feedback.setContent(content);
        feedback.setStatus(FeedbackStatus.PENDING.getCode());
        feedback.setContact(contact == null || contact.isEmpty() ? null : contact);
        feedback.setOrderId(trimToNull(dto.getOrderId()));
        feedback.setMerchantId(trimToNull(dto.getMerchantId()));
        feedback.setDeviceInfo(writeDeviceInfo(dto.getDeviceInfo()));
        feedback.setEnvironment(deriveEnvironment(dto.getDeviceInfo()));

        save(feedback);
        return feedback;
    }

    @Override
    public IPage<Feedback> listOwn(String userId, long page, long size, String type, String status) {
        if (userId == null || userId.isBlank()) {
            throw new BusinessException("401", "未登录");
        }
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
        if (userId == null || userId.isBlank()) {
            throw new BusinessException("401", "未登录");
        }
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
