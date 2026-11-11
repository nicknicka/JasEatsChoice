package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.dto.FeedbackAssociationUpdateDTO;
import com.xx.jaseatschoicejava.service.FeedbackService;
import com.xx.jaseatschoicejava.util.AdminContext;
import com.xx.jaseatschoicejava.vo.FeedbackVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员-反馈管理控制器。
 */
@Api(tags = "管理员-反馈管理")
@RestController
@RequestMapping("/admin/feedbacks")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    /**
     * 修正反馈的订单/商家关联，写入审计。
     */
    @ApiOperation("修正反馈关联")
    @PutMapping("/{feedbackId}/association")
    @PreAuthorize("hasAnyAuthority('admin:feedback:triage')")
    public ResponseResult<FeedbackVO> updateAssociation(
            @PathVariable String feedbackId,
            @RequestBody FeedbackAssociationUpdateDTO dto) {
        Long adminId = AdminContext.getAdminId();
        return ResponseResult.success(FeedbackVO.from(
                feedbackService.updateAssociation(adminId, feedbackId, dto)));
    }
}
