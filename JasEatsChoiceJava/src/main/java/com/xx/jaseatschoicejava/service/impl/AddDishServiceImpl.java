package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.dto.*;
import com.xx.jaseatschoicejava.entity.*;
import com.xx.jaseatschoicejava.enums.AddDishApprovalStatus;
import com.xx.jaseatschoicejava.enums.AddDishPermission;
import com.xx.jaseatschoicejava.mapper.AddDishRequestMapper;
import com.xx.jaseatschoicejava.mapper.AddDishSettingMapper;
import com.xx.jaseatschoicejava.service.AddDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 加菜服务实现
 */
@Slf4j
@Service
public class AddDishServiceImpl extends ServiceImpl<AddDishRequestMapper, AddDishRequest> implements AddDishService {

    @Autowired
    private AddDishSettingMapper settingMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createAddDishRequest(CreateAddDishDTO dto, Long requestUserId) {
        // 1. 检查权限
        if (!checkAddDishPermission(dto.getGroupOrderId(), requestUserId)) {
            throw new RuntimeException("无加菜权限");
        }

        // 2. 获取群订单信息（这里需要注入GroupOrderService，暂时简化处理）
        // GroupOrder groupOrder = groupOrderService.getById(dto.getGroupOrderId());

        // 3. 检查加菜设置（预算限制、数量限制）
        AddDishSetting setting = getSettingEntity(dto.getGroupOrderId());
        if (setting != null) {
            // 检查数量限制
            if (setting.getMaxDishCount() != null &&
                dto.getDishItems().size() > setting.getMaxDishCount()) {
                throw new RuntimeException("超过单次加菜数量限制");
            }

            // 计算总金额（需要获取菜品价格，这里简化处理）
            BigDecimal totalAmount = calculateTotalAmount(dto);
            if (setting.getBudgetLimit() != null &&
                totalAmount.compareTo(setting.getBudgetLimit()) > 0) {
                throw new RuntimeException("超过单次加菜预算限制");
            }
        }

        // 4. 创建加菜请求
        AddDishRequest request = new AddDishRequest();
        request.setId(UUID.randomUUID().toString().replace("-", ""));
        request.setGroupOrderId(dto.getGroupOrderId());
        request.setOriginalOrderId(dto.getOriginalOrderId());
        request.setRequestUserId(requestUserId);
        // request.setMerchantId(merchantId); // 从群订单获取
        request.setApprovalStatus(AddDishApprovalStatus.PENDING.getValue());
        request.setCreateTime(LocalDateTime.now());
        request.setUpdateTime(LocalDateTime.now());

        // 设置超时时间
        request.setTimeoutTime(LocalDateTime.now().plusMinutes(15));
        request.setFirstRemindTime(LocalDateTime.now().plusMinutes(10));

        // 保存菜品信息为JSON
        try {
            request.setDishInfo(objectMapper.writeValueAsString(dto.getDishItems()));
        } catch (JsonProcessingException e) {
            log.error("序列化菜品信息失败", e);
            throw new RuntimeException("保存菜品信息失败");
        }

        // 5. 保存到数据库
        save(request);

        // 6. 发送WebSocket通知（需要在后续集成）
        // notifyAddDishRequest(request);

        return request.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewResultDTO batchReview(BatchReviewDTO dto) {
        List<String> failedReasons = new ArrayList<>();
        int approvedCount = 0;
        int rejectedCount = 0;

        for (String requestId : dto.getRequestIds()) {
            try {
                AddDishRequest request = getById(requestId);
                if (request == null) {
                    failedReasons.add(requestId + ": 请求不存在");
                    continue;
                }

                // 检查状态
                if (!AddDishApprovalStatus.PENDING.getValue().equals(request.getApprovalStatus())) {
                    failedReasons.add(requestId + ": 该请求已审核");
                    continue;
                }

                // 更新状态
                if ("approve".equals(dto.getAction())) {
                    request.setApprovalStatus(AddDishApprovalStatus.APPROVED.getValue());
                    approvedCount++;

                    // 审核通过后创建订单（需要调用OrderService）
                    // Order addOrder = createAddOrder(request);
                } else if ("reject".equals(dto.getAction())) {
                    request.setApprovalStatus(AddDishApprovalStatus.REJECTED.getValue());
                    request.setRejectReason(dto.getRejectReason());
                    rejectedCount++;
                }

                request.setReviewerId(dto.getReviewerId());
                request.setReviewTime(LocalDateTime.now());
                request.setUpdateTime(LocalDateTime.now());
                updateById(request);

            } catch (Exception e) {
                log.error("审核请求失败, requestId: {}", requestId, e);
                failedReasons.add(requestId + ": " + e.getMessage());
            }
        }

        if (!failedReasons.isEmpty()) {
            return ReviewResultDTO.failure(failedReasons);
        }

        return ReviewResultDTO.success(approvedCount, rejectedCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean withdrawRequest(String requestId, Long userId) {
        AddDishRequest request = getById(requestId);
        if (request == null) {
            throw new RuntimeException("请求不存在");
        }

        // 只有请求人可以撤回
        if (!request.getRequestUserId().equals(userId)) {
            throw new RuntimeException("无权撤回此请求");
        }

        // 只能撤回待审核状态的请求
        if (!AddDishApprovalStatus.PENDING.getValue().equals(request.getApprovalStatus())) {
            throw new RuntimeException("该请求已审核，无法撤回");
        }

        request.setApprovalStatus(AddDishApprovalStatus.WITHDRAWN.getValue());
        request.setUpdateTime(LocalDateTime.now());

        return updateById(request);
    }

    @Override
    public List<AddDishRequestVO> getReviewList(Long groupOrderId) {
        LambdaQueryWrapper<AddDishRequest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddDishRequest::getGroupOrderId, groupOrderId)
                   .eq(AddDishRequest::getApprovalStatus, AddDishApprovalStatus.PENDING.getValue())
                   .orderByDesc(AddDishRequest::getCreateTime);

        List<AddDishRequest> requests = list(queryWrapper);
        return requests.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<AddDishRequestVO> getHistory(Long groupOrderId) {
        LambdaQueryWrapper<AddDishRequest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddDishRequest::getGroupOrderId, groupOrderId)
                   .in(AddDishRequest::getApprovalStatus,
                       AddDishApprovalStatus.APPROVED.getValue(),
                       AddDishApprovalStatus.REJECTED.getValue(),
                       AddDishApprovalStatus.WITHDRAWN.getValue(),
                       AddDishApprovalStatus.TIMEOUT_REJECTED.getValue())
                   .orderByDesc(AddDishRequest::getCreateTime);

        List<AddDishRequest> requests = list(queryWrapper);
        return requests.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public AllergyCheckResultDTO checkAllergyConflict(CreateAddDishDTO dto) {
        // TODO: 实现饮食禁忌检测逻辑
        // 1. 获取群成员列表
        // 2. 获取成员饮食禁忌
        // 3. 获取菜品食材信息
        // 4. 检测冲突
        return new AllergyCheckResultDTO(false, new ArrayList<>());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleTimeoutRequests() {
        LocalDateTime now = LocalDateTime.now();
        List<AddDishRequest> timeoutRequests = baseMapper.selectTimeoutRequests(now);

        for (AddDishRequest request : timeoutRequests) {
            try {
                request.setApprovalStatus(AddDishApprovalStatus.TIMEOUT_REJECTED.getValue());
                request.setUpdateTime(LocalDateTime.now());
                updateById(request);

                // 发送超时通知
                // notifyTimeout(request);
                log.info("加菜请求超时自动驳回, requestId: {}", request.getId());
            } catch (Exception e) {
                log.error("处理超时请求失败, requestId: {}", request.getId(), e);
            }
        }
    }

    @Override
    public void sendReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<AddDishRequest> needRemind = baseMapper.selectNeedRemindRequests(now);

        for (AddDishRequest request : needRemind) {
            try {
                request.setSecondRemindTime(now);
                updateById(request);

                // 发送提醒通知
                // notifyReminder(request);
                log.info("发送加菜超时提醒, requestId: {}", request.getId());
            } catch (Exception e) {
                log.error("发送提醒失败, requestId: {}", request.getId(), e);
            }
        }
    }

    @Override
    public boolean checkAddDishPermission(Long groupOrderId, Long userId) {
        // 1. 检查群订单是否允许加菜
        // 2. 获取加菜设置
        AddDishSetting setting = getSettingEntity(groupOrderId);
        if (setting == null) {
            // 默认全员可加菜
            return true;
        }

        // 3. 检查权限模式
        if (AddDishPermission.INITIATOR_ONLY.getValue().equals(setting.getAddDishPermission())) {
            // 仅发起者可加菜 - 需要检查是否为发起者
            // GroupOrder groupOrder = groupOrderService.getById(groupOrderId);
            // return groupOrder.getInitiatorId().equals(userId);
            return true; // 临时返回
        }

        return true;
    }

    @Override
    public AddDishSettingDTO getSetting(Long groupOrderId) {
        AddDishSetting setting = getSettingEntity(groupOrderId);
        AddDishSettingDTO dto = new AddDishSettingDTO();
        if (setting != null) {
            dto.setGroupOrderId(setting.getGroupOrderId());
            dto.setAddDishPermission(setting.getAddDishPermission());
            dto.setBudgetLimit(setting.getBudgetLimit());
            dto.setMaxDishCount(setting.getMaxDishCount());
        } else {
            // 返回默认设置
            dto.setGroupOrderId(groupOrderId);
            dto.setAddDishPermission(AddDishPermission.ALL_MEMBERS.getValue());
        }
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSetting(AddDishSettingDTO dto) {
        AddDishSetting setting = new AddDishSetting();
        setting.setGroupOrderId(dto.getGroupOrderId());
        setting.setAddDishPermission(dto.getAddDishPermission());
        setting.setBudgetLimit(dto.getBudgetLimit());
        setting.setMaxDishCount(dto.getMaxDishCount());

        LambdaQueryWrapper<AddDishSetting> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddDishSetting::getGroupOrderId, dto.getGroupOrderId());

        AddDishSetting existing = settingMapper.selectOne(queryWrapper);
        if (existing != null) {
            setting.setId(existing.getId());
            return settingMapper.updateById(setting) > 0;
        } else {
            return settingMapper.insert(setting) > 0;
        }
    }

    /**
     * 获取加菜设置实体
     */
    private AddDishSetting getSettingEntity(Long groupOrderId) {
        LambdaQueryWrapper<AddDishSetting> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddDishSetting::getGroupOrderId, groupOrderId);
        return settingMapper.selectOne(queryWrapper);
    }

    /**
     * 计算总金额（简化版本）
     */
    private BigDecimal calculateTotalAmount(CreateAddDishDTO dto) {
        // TODO: 根据菜品ID获取实际价格并计算
        return BigDecimal.ZERO;
    }

    /**
     * 转换为VO
     */
    private AddDishRequestVO convertToVO(AddDishRequest request) {
        AddDishRequestVO vo = new AddDishRequestVO();
        vo.setId(request.getId());
        vo.setGroupOrderId(request.getGroupOrderId());
        vo.setStatus(request.getApprovalStatus());
        vo.setStatusDesc(AddDishApprovalStatus.fromValue(request.getApprovalStatus()).getDesc());
        vo.setSubmitTime(request.getCreateTime());
        vo.setRejectReason(request.getRejectReason());

        // 计算剩余时间
        if (request.getTimeoutTime() != null &&
            AddDishApprovalStatus.PENDING.getValue().equals(request.getApprovalStatus())) {
            long remaining = java.time.Duration.between(LocalDateTime.now(), request.getTimeoutTime()).getSeconds();
            vo.setRemainingTime(Math.max(0, remaining));
        }

        // 解析菜品信息
        try {
            List<CreateAddDishDTO.DishItem> items = objectMapper.readValue(
                request.getDishInfo(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, CreateAddDishDTO.DishItem.class)
            );
            // TODO: 转换为DishInfo并填充菜品名称、价格等
        } catch (JsonProcessingException e) {
            log.error("解析菜品信息失败", e);
        }

        return vo;
    }
}
