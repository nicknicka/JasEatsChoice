package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.dto.*;
import com.xx.jaseatschoicejava.service.AddDishService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 加菜功能控制器
 */
@RestController
@RequestMapping("/v1/add-dish")
public class AddDishController {

    private static final Logger logger = LoggerFactory.getLogger(AddDishController.class);

    @Autowired
    private AddDishService addDishService;

    /**
     * 创建加菜请求
     */
    @PostMapping("/request")
    public ResponseResult<?> createAddDishRequest(@RequestBody CreateAddDishDTO dto,
                                                   @RequestHeader("X-User-Id") String userId) {
        try {
            String requestId = addDishService.createAddDishRequest(dto, userId);
            return ResponseResult.success(requestId);
        } catch (Exception e) {
            logger.error("创建加菜请求失败", e);
            return ResponseResult.fail("500", "创建加菜请求失败：" + e.getMessage());
        }
    }

    /**
     * 获取加菜审核列表
     */
    @GetMapping("/review-list/{groupOrderId}")
    public ResponseResult<?> getReviewList(@PathVariable String groupOrderId) {
        try {
            List<AddDishRequestVO> reviewList = addDishService.getReviewList(groupOrderId);
            return ResponseResult.success(reviewList);
        } catch (Exception e) {
            logger.error("获取审核列表失败", e);
            return ResponseResult.fail("500", "获取审核列表失败：" + e.getMessage());
        }
    }

    /**
     * 批量审核加菜请求
     */
    @PutMapping("/review")
    public ResponseResult<?> batchReview(@RequestBody BatchReviewDTO dto) {
        try {
            ReviewResultDTO result = addDishService.batchReview(dto);
            return ResponseResult.success(result);
        } catch (Exception e) {
            logger.error("批量审核失败", e);
            return ResponseResult.fail("500", "批量审核失败：" + e.getMessage());
        }
    }

    /**
     * 撤回加菜请求
     */
    @DeleteMapping("/request/{requestId}")
    public ResponseResult<?> withdrawRequest(@PathVariable String requestId,
                                              @RequestHeader("X-User-Id") String userId) {
        try {
            boolean success = addDishService.withdrawRequest(requestId, userId);
            return ResponseResult.success(success);
        } catch (Exception e) {
            logger.error("撤回请求失败", e);
            return ResponseResult.fail("500", "撤回请求失败：" + e.getMessage());
        }
    }

    /**
     * 获取加菜历史
     */
    @GetMapping("/history/{groupOrderId}")
    public ResponseResult<?> getHistory(@PathVariable String groupOrderId) {
        try {
            List<AddDishRequestVO> history = addDishService.getHistory(groupOrderId);
            return ResponseResult.success(history);
        } catch (Exception e) {
            logger.error("获取加菜历史失败", e);
            return ResponseResult.fail("500", "获取加菜历史失败：" + e.getMessage());
        }
    }

    /**
     * 检查饮食禁忌冲突
     */
    @PostMapping("/check-allergy")
    public ResponseResult<?> checkAllergy(@RequestBody CreateAddDishDTO dto) {
        try {
            AllergyCheckResultDTO result = addDishService.checkAllergyConflict(dto);
            return ResponseResult.success(result);
        } catch (Exception e) {
            logger.error("检查饮食禁忌失败", e);
            return ResponseResult.fail("500", "检查饮食禁忌失败：" + e.getMessage());
        }
    }

    /**
     * 获取加菜设置
     */
    @GetMapping("/setting/{groupOrderId}")
    public ResponseResult<?> getSetting(@PathVariable String groupOrderId) {
        try {
            AddDishSettingDTO setting = addDishService.getSetting(groupOrderId);
            return ResponseResult.success(setting);
        } catch (Exception e) {
            logger.error("获取加菜设置失败", e);
            return ResponseResult.fail("500", "获取加菜设置失败：" + e.getMessage());
        }
    }

    /**
     * 更新加菜设置
     */
    @PutMapping("/setting")
    public ResponseResult<?> updateSetting(@RequestBody AddDishSettingDTO dto) {
        try {
            boolean success = addDishService.updateSetting(dto);
            return ResponseResult.success(success);
        } catch (Exception e) {
            logger.error("更新加菜设置失败", e);
            return ResponseResult.fail("500", "更新加菜设置失败：" + e.getMessage());
        }
    }
}
