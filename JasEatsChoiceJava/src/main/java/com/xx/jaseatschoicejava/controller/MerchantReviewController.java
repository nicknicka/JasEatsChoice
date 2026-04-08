package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.dto.*;
import com.xx.jaseatschoicejava.service.MerchantReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商家评价回复控制器
 * 提供AI评价回复生成功能
 */
@RestController
@RequestMapping("/v1/merchant/review")
public class MerchantReviewController {

    @Autowired
    private MerchantReviewService merchantReviewService;

    /**
     * 获取待回复评价列表
     */
    @GetMapping("/{merchantId}/pending")
    public ResponseResult<?> getPendingReviews(@PathVariable String merchantId) {
        try {
            List<PendingReviewDTO> reviews = merchantReviewService.getPendingReviews(merchantId);
            return ResponseResult.success(reviews);
        } catch (Exception e) {
            return ResponseResult.fail("500", "获取评价列表失败：" + e.getMessage());
        }
    }

    /**
     * AI生成回复建议
     */
    @PostMapping("/generate-reply")
    public ResponseResult<?> generateReply(@RequestBody ReviewReplyRequestDTO request) {
        try {
            List<String> suggestions = merchantReviewService.generateReplySuggestions(request);
            return ResponseResult.success(suggestions);
        } catch (Exception e) {
            return ResponseResult.fail("500", "生成回复失败：" + e.getMessage());
        }
    }

    /**
     * 提交评价回复
     */
    @PostMapping("/submit-reply")
    public ResponseResult<?> submitReply(@RequestBody SubmitReplyDTO request) {
        try {
            boolean success = merchantReviewService.submitReply(request);
            if (success) {
                return ResponseResult.success("回复成功");
            }
            return ResponseResult.fail("500", "回复失败");
        } catch (Exception e) {
            return ResponseResult.fail("500", "提交回复失败：" + e.getMessage());
        }
    }
}
