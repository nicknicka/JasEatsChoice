package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.GroupOrderAddition;
import com.xx.jaseatschoicejava.service.GroupOrderAdditionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 群订单加菜控制器
 */
@RestController
@RequestMapping("/v1/group-order-additions")
@RequiredArgsConstructor
public class GroupOrderAdditionController {

    private static final Logger logger = LoggerFactory.getLogger(GroupOrderAdditionController.class);

    private final GroupOrderAdditionService groupOrderAdditionService;

    /**
     * 发起加菜请求
     */
    @PostMapping("/request")
    public ResponseResult<?> requestAddDish(@RequestBody Map<String, Object> request) {
        try {
            String groupId = request.get("groupId").toString();
            String userId = request.get("userId").toString();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> dishes = (List<Map<String, Object>>) request.get("dishes");

            GroupOrderAddition addition = groupOrderAdditionService.requestAddDish(groupId, userId, dishes);

            logger.info("加菜请求创建成功 - additionId: {}, userId: {}", addition.getId(), userId);
            return ResponseResult.success(addition);
        } catch (Exception e) {
            logger.error("创建加菜请求失败", e);
            return ResponseResult.fail("500", "创建加菜请求失败：" + e.getMessage());
        }
    }

    /**
     * 审核加菜请求
     */
    @PostMapping("/{additionId}/review")
    public ResponseResult<?> reviewAddition(
            @PathVariable String additionId,
            @RequestBody Map<String, Object> request) {
        try {
            boolean approved = Boolean.parseBoolean(request.get("approved").toString());
            String rejectReason = request.get("rejectReason") != null
                    ? request.get("rejectReason").toString()
                    : null;

            boolean success = groupOrderAdditionService.reviewAddition(additionId, approved, rejectReason);

            if (success) {
                logger.info("加菜审核完成 - additionId: {}, approved: {}", additionId, approved);
                return ResponseResult.success(Map.of(
                        "additionId", additionId,
                        "approved", approved,
                        "status", approved ? "approved_pending_payment" : "rejected"
                ));
            }
            return ResponseResult.fail("500", "审核加菜请求失败");
        } catch (Exception e) {
            logger.error("审核加菜请求失败 - additionId: {}", additionId, e);
            return ResponseResult.fail("500", "审核加菜请求失败：" + e.getMessage());
        }
    }

    /**
     * 获取待审核的加菜列表
     */
    @GetMapping("/pending")
    public ResponseResult<?> getPendingAdditions(@RequestParam String groupOrderId) {
        try {
            List<GroupOrderAddition> additions = groupOrderAdditionService.getPendingAdditions(groupOrderId);
            return ResponseResult.success(additions);
        } catch (Exception e) {
            logger.error("获取待审核加菜列表失败 - groupOrderId: {}", groupOrderId, e);
            return ResponseResult.fail("500", "获取待审核加菜列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取审核通过待支付的加菜列表
     */
    @GetMapping("/approved-pending-payment")
    public ResponseResult<?> getApprovedPendingPayments(@RequestParam String groupOrderId) {
        try {
            List<GroupOrderAddition> additions = groupOrderAdditionService.getApprovedPendingPayments(groupOrderId);
            return ResponseResult.success(additions);
        } catch (Exception e) {
            logger.error("获取待支付加菜列表失败 - groupOrderId: {}", groupOrderId, e);
            return ResponseResult.fail("500", "获取待支付加菜列表失败：" + e.getMessage());
        }
    }

    /**
     * 统一支付加菜池
     */
    @PostMapping("/pay-pool")
    public ResponseResult<?> payAdditionPool(@RequestBody Map<String, Object> request) {
        String groupOrderId = null;
        try {
            groupOrderId = request.get("groupOrderId").toString();

            boolean success = groupOrderAdditionService.payAdditionPool(groupOrderId);

            if (success) {
                logger.info("加菜池支付成功 - groupOrderId: {}", groupOrderId);
                return ResponseResult.success(Map.of(
                        "groupOrderId", groupOrderId,
                        "status", "paid"
                ));
            }
            return ResponseResult.fail("500", "支付加菜池失败");
        } catch (Exception e) {
            logger.error("支付加菜池失败 - groupOrderId: {}", groupOrderId, e);
            return ResponseResult.fail("500", "支付加菜池失败：" + e.getMessage());
        }
    }

    /**
     * 获取加菜详情
     */
    @GetMapping("/{additionId}")
    public ResponseResult<?> getAdditionDetail(@PathVariable String additionId) {
        try {
            GroupOrderAddition addition = groupOrderAdditionService.getById(additionId);
            if (addition != null) {
                return ResponseResult.success(addition);
            }
            return ResponseResult.fail("404", "加菜记录不存在");
        } catch (Exception e) {
            logger.error("获取加菜详情失败 - additionId: {}", additionId, e);
            return ResponseResult.fail("500", "获取加菜详情失败：" + e.getMessage());
        }
    }
}
