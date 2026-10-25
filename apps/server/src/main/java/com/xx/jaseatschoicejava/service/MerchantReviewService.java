package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.dto.*;
import java.util.List;

/**
 * 商家评价回复服务接口
 */
public interface MerchantReviewService {

    /**
     * 获取待回复评价列表
     * @param merchantId 商家ID
     * @return 待回复评价列表
     */
    List<PendingReviewDTO> getPendingReviews(String merchantId);

    /**
     * AI生成评价回复建议
     * @param request 回复生成请求
     * @return 回复建议列表（3种风格）
     */
    List<String> generateReplySuggestions(ReviewReplyRequestDTO request);

    /**
     * 提交评价回复
     * @param request 提交请求
     * @return 是否成功
     */
    boolean submitReply(SubmitReplyDTO request);
}
