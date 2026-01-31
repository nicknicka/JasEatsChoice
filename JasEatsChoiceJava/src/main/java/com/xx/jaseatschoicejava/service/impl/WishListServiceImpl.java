package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xx.jaseatschoicejava.dto.AppealReplyDTO;
import com.xx.jaseatschoicejava.dto.WishListAppealDTO;
import com.xx.jaseatschoicejava.dto.WishListAuditDTO;
import com.xx.jaseatschoicejava.dto.WishListItemCreateDTO;
import com.xx.jaseatschoicejava.entity.WishListItem;
import com.xx.jaseatschoicejava.enums.RejectionReason;
import com.xx.jaseatschoicejava.enums.WishListAuditStatus;
import com.xx.jaseatschoicejava.mapper.WishListItemMapper;
import com.xx.jaseatschoicejava.service.WishListService;
import com.xx.jaseatschoicejava.vo.WishListItemDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 想吃列表服务实现类
 *
 * @author Claude
 * @since 2025-01-30
 */
@Slf4j
@Service
public class WishListServiceImpl implements WishListService {

    @Autowired
    private WishListItemMapper wishListItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createWishListItem(WishListItemCreateDTO dto, String userId) {
        WishListItem item = new WishListItem();
        item.setId(UUID.randomUUID().toString().replace("-", ""));
        item.setUserId(userId);
        item.setMerchantId(dto.getMerchantId());
        item.setDishName(dto.getDishName());
        item.setDishImage(dto.getDishImage());
        item.setTasteRequirement(dto.getTasteRequirement());
        item.setDescription(dto.getDescription());
        item.setRecipeId(dto.getRecipeId());
        item.setExpectedAvailableTime(dto.getExpectedAvailableTime());

        // 设置初始状态和超时时间（24小时后自动通过）
        item.setAuditStatus(WishListAuditStatus.PENDING.getCode());
        item.setTimeoutTime(LocalDateTime.now().plusHours(24));
        item.setCreateTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());

        wishListItemMapper.insert(item);
        return item.getId();
    }

    @Override
    public List<WishListItemDetailVO> getUserWishList(String userId) {
        List<WishListItem> items = wishListItemMapper.selectByUserId(userId);
        return items.stream()
            .map(this::convertToDetailVO)
            .collect(Collectors.toList());
    }

    @Override
    public List<WishListItemDetailVO> getMerchantPendingList(String merchantId) {
        List<WishListItem> items = wishListItemMapper.selectPendingByMerchantId(merchantId);
        return items.stream()
            .map(this::convertToDetailVO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditWishListItem(WishListAuditDTO dto, String auditorId, String auditorName) {
        WishListItem item = wishListItemMapper.selectById(dto.getWishListItemId());
        if (item == null) {
            throw new RuntimeException("想吃列表项不存在");
        }

        if (!WishListAuditStatus.PENDING.getCode().equals(item.getAuditStatus())) {
            throw new RuntimeException("该列表项已审核，无法重复操作");
        }

        LambdaUpdateWrapper<WishListItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(WishListItem::getId, dto.getWishListItemId());

        if (dto.getApproved()) {
            // 审核通过
            updateWrapper.set(WishListItem::getAuditStatus, WishListAuditStatus.APPROVED.getCode())
                .set(WishListItem::getActualAvailableTime, dto.getActualAvailableTime())
                .set(WishListItem::getAuditorId, auditorId)
                .set(WishListItem::getAuditorName, auditorName)
                .set(WishListItem::getAuditTime, LocalDateTime.now());
        } else {
            // 审核拒绝
            if (dto.getRejectionReasonCode() == null) {
                throw new RuntimeException("拒绝时必须选择拒绝原因");
            }
            updateWrapper.set(WishListItem::getAuditStatus, WishListAuditStatus.REJECTED.getCode())
                .set(WishListItem::getRejectionReasonCode, dto.getRejectionReasonCode())
                .set(WishListItem::getRejectionReason, dto.getRejectionReason())
                .set(WishListItem::getAuditRemark, dto.getAuditRemark())
                .set(WishListItem::getAuditorId, auditorId)
                .set(WishListItem::getAuditorName, auditorName)
                .set(WishListItem::getAuditTime, LocalDateTime.now());
        }

        updateWrapper.set(WishListItem::getUpdateTime, LocalDateTime.now());

        int updated = wishListItemMapper.update(null, updateWrapper);
        return updated > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean appealWishListItem(WishListAppealDTO dto, String userId) {
        WishListItem item = wishListItemMapper.selectById(dto.getWishListItemId());
        if (item == null) {
            throw new RuntimeException("想吃列表项不存在");
        }

        if (!item.getUserId().equals(userId)) {
            throw new RuntimeException("只能申诉自己的列表项");
        }

        if (!WishListAuditStatus.REJECTED.getCode().equals(item.getAuditStatus())) {
            throw new RuntimeException("只有被拒绝的列表项才能申诉");
        }

        LambdaUpdateWrapper<WishListItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(WishListItem::getId, dto.getWishListItemId())
            .set(WishListItem::getAuditStatus, WishListAuditStatus.APPEALING.getCode())
            .set(WishListItem::getIsAppealed, true)
            .set(WishListItem::getAppealContent, dto.getAppealContent())
            .set(WishListItem::getAppealTime, LocalDateTime.now())
            .set(WishListItem::getUpdateTime, LocalDateTime.now());

        int updated = wishListItemMapper.update(null, updateWrapper);
        return updated > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean replyAppeal(AppealReplyDTO dto, String replierId) {
        WishListItem item = wishListItemMapper.selectById(dto.getWishListItemId());
        if (item == null) {
            throw new RuntimeException("想吃列表项不存在");
        }

        if (!WishListAuditStatus.APPEALING.getCode().equals(item.getAuditStatus())) {
            throw new RuntimeException("该列表项不在申诉状态");
        }

        WishListAuditStatus newStatus = dto.getApproved()
            ? WishListAuditStatus.APPEAL_APPROVED
            : WishListAuditStatus.APPEAL_REJECTED;

        LambdaUpdateWrapper<WishListItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(WishListItem::getId, dto.getWishListItemId())
            .set(WishListItem::getAuditStatus, newStatus.getCode())
            .set(WishListItem::getAppealReply, dto.getAppealReply())
            .set(WishListItem::getAppealReplyTime, LocalDateTime.now())
            .set(WishListItem::getAppealReplierId, replierId)
            .set(WishListItem::getUpdateTime, LocalDateTime.now());

        int updated = wishListItemMapper.update(null, updateWrapper);
        return updated > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean withdrawWishListItem(String wishListItemId, String userId) {
        LambdaUpdateWrapper<WishListItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(WishListItem::getId, wishListItemId)
            .eq(WishListItem::getUserId, userId)
            .set(WishListItem::getAuditStatus, WishListAuditStatus.WITHDRAWN.getCode())
            .set(WishListItem::getUpdateTime, LocalDateTime.now());

        int updated = wishListItemMapper.update(null, updateWrapper);
        return updated > 0;
    }

    @Override
    public WishListItemDetailVO getWishListItemDetail(String wishListItemId) {
        WishListItem item = wishListItemMapper.selectById(wishListItemId);
        if (item == null) {
            throw new RuntimeException("想吃列表项不存在");
        }
        return convertToDetailVO(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int autoAuditTimeoutItems() {
        List<WishListItem> timeoutItems = wishListItemMapper.selectTimeoutItems(LocalDateTime.now());

        int count = 0;
        for (WishListItem item : timeoutItems) {
            try {
                LambdaUpdateWrapper<WishListItem> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(WishListItem::getId, item.getId())
                    .set(WishListItem::getAuditStatus, WishListAuditStatus.AUTO_APPROVED.getCode())
                    .set(WishListItem::getAuditRemark, "超时自动通过（24小时未审核）")
                    .set(WishListItem::getUpdateTime, LocalDateTime.now());

                if (wishListItemMapper.update(null, updateWrapper) > 0) {
                    count++;
                }
            } catch (Exception e) {
                log.error("自动审核失败：wishListItemId={}", item.getId(), e);
            }
        }

        return count;
    }

    /**
     * 转换为详情VO
     */
    private WishListItemDetailVO convertToDetailVO(WishListItem item) {
        WishListItemDetailVO vo = new WishListItemDetailVO();
        vo.setId(item.getId());
        vo.setUserId(item.getUserId());
        vo.setMerchantId(item.getMerchantId());
        vo.setDishName(item.getDishName());
        vo.setDishImage(item.getDishImage());
        vo.setTasteRequirement(item.getTasteRequirement());
        vo.setDescription(item.getDescription());
        vo.setRecipeId(item.getRecipeId());
        vo.setAuditStatus(item.getAuditStatus());

        WishListAuditStatus status = WishListAuditStatus.getByCode(item.getAuditStatus());
        vo.setAuditStatusName(status.getDescription());

        vo.setRejectionReasonCode(item.getRejectionReasonCode());
        vo.setAuditRemark(item.getAuditRemark());
        vo.setAuditorName(item.getAuditorName());
        vo.setAuditTime(item.getAuditTime());
        vo.setIsAppealed(item.getIsAppealed());
        vo.setAppealContent(item.getAppealContent());
        vo.setAppealTime(item.getAppealTime());
        vo.setAppealReply(item.getAppealReply());
        vo.setAppealReplyTime(item.getAppealReplyTime());
        vo.setExpectedAvailableTime(item.getExpectedAvailableTime());
        vo.setActualAvailableTime(item.getActualAvailableTime());
        vo.setTimeoutTime(item.getTimeoutTime());
        vo.setCreateTime(item.getCreateTime());

        // 计算剩余时间
        if (item.getTimeoutTime() != null && item.getAuditStatus() == 0) {
            long hours = ChronoUnit.HOURS.between(LocalDateTime.now(), item.getTimeoutTime());
            vo.setRemainingHours(Math.max(0, hours));
        }

        // 设置拒绝原因标题
        if (item.getRejectionReasonCode() != null) {
            RejectionReason reason = RejectionReason.getByCode(item.getRejectionReasonCode());
            vo.setRejectionReasonTitle(reason.getTitle());
            vo.setRejectionReasonDescription(reason.getDescription());
        }

        // 设置可操作标识
        vo.setCanAppeal(status.canAppeal());
        vo.setCanWithdraw(WishListAuditStatus.PENDING.equals(status));

        return vo;
    }
}
