package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.dto.AdminFeedbackMessageDTO;
import com.xx.jaseatschoicejava.dto.FeedbackAssociationUpdateDTO;
import com.xx.jaseatschoicejava.dto.FeedbackIssueDraftDTO;
import com.xx.jaseatschoicejava.dto.FeedbackResolveDTO;
import com.xx.jaseatschoicejava.dto.FeedbackTriageDTO;
import com.xx.jaseatschoicejava.entity.Feedback;
import com.xx.jaseatschoicejava.entity.FeedbackIssueLink;
import com.xx.jaseatschoicejava.service.FeedbackService;
import com.xx.jaseatschoicejava.util.AdminContext;
import com.xx.jaseatschoicejava.vo.AdminFeedbackDetailVO;
import com.xx.jaseatschoicejava.vo.AdminFeedbackVO;
import com.xx.jaseatschoicejava.vo.FeedbackMessageVO;
import com.xx.jaseatschoicejava.vo.FeedbackPageVO;
import com.xx.jaseatschoicejava.vo.FeedbackVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员-反馈管理控制器：列表、详情、分诊、回复、解决与关联修正。
 */
@Api(tags = "管理员-反馈管理")
@RestController
@RequestMapping("/admin/feedbacks")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @ApiOperation("反馈列表")
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('admin:feedback:list')")
    public ResponseResult<FeedbackPageVO<AdminFeedbackVO>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String merchantId,
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String priority) {
        IPage<Feedback> result = feedbackService.adminList(
                page, size, status, type, userId, merchantId, orderId, priority);
        // 管理端列表额外带分诊优先级与负责人；负责人名字在这里一次性解析，避免前端多拉一次名单
        Map<Long, String> assigneeNames = feedbackService.resolveAssigneeNames(
                result.getRecords().stream().map(Feedback::getAssigneeId).toList());
        List<AdminFeedbackVO> list = result.getRecords().stream()
                // 注意：assigneeNames 可能是 Map.of()（不可变），而 get(null) 会抛 NPE，
                // 未认领的反馈 assigneeId 就是 null，必须先判空。
                .map(item -> AdminFeedbackVO.of(item, item.getAssigneeId() == null
                        ? null
                        : assigneeNames.get(item.getAssigneeId())))
                .toList();
        return ResponseResult.success(
                new FeedbackPageVO<>(list, result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @ApiOperation("各状态反馈条数（分诊队列摘要）")
    @GetMapping("/stats")
    @PreAuthorize("hasAnyAuthority('admin:feedback:list')")
    public ResponseResult<Map<String, Long>> stats() {
        return ResponseResult.success(feedbackService.adminStatusCounts());
    }

    @ApiOperation("反馈详情")
    @GetMapping("/{feedbackId}")
    @PreAuthorize("hasAnyAuthority('admin:feedback:detail')")
    public ResponseResult<AdminFeedbackDetailVO> detail(@PathVariable String feedbackId) {
        return ResponseResult.success(feedbackService.adminDetail(feedbackId));
    }

    @ApiOperation("回复用户或写内部备注")
    @PostMapping("/{feedbackId}/messages")
    @PreAuthorize("hasAnyAuthority('admin:feedback:reply')")
    public ResponseResult<FeedbackMessageVO> reply(
            @PathVariable String feedbackId,
            @RequestBody AdminFeedbackMessageDTO dto) {
        Long adminId = AdminContext.getAdminId();
        return ResponseResult.success(FeedbackMessageVO.from(feedbackService.addAdminMessage(
                adminId, feedbackId, dto == null ? null : dto.getContent(),
                dto == null ? null : dto.getVisibility())));
    }

    @ApiOperation("分诊")
    @PostMapping("/{feedbackId}/triage")
    @PreAuthorize("hasAnyAuthority('admin:feedback:triage')")
    public ResponseResult<FeedbackVO> triage(
            @PathVariable String feedbackId,
            @RequestBody FeedbackTriageDTO dto) {
        Long adminId = AdminContext.getAdminId();
        return ResponseResult.success(FeedbackVO.from(
                feedbackService.triage(adminId, feedbackId, dto)));
    }

    @ApiOperation("创建 GitHub Issue 关联")
    @PostMapping("/{feedbackId}/issue")
    @PreAuthorize("hasAnyAuthority('admin:feedback:triage')")
    public ResponseResult<FeedbackIssueLink> createIssue(
            @PathVariable String feedbackId,
            @RequestBody FeedbackIssueDraftDTO draft) {
        Long adminId = AdminContext.getAdminId();
        return ResponseResult.success(feedbackService.createIssue(adminId, feedbackId, draft));
    }

    @ApiOperation("解决反馈")
    @PostMapping("/{feedbackId}/resolve")
    @PreAuthorize("hasAnyAuthority('admin:feedback:resolve')")
    public ResponseResult<FeedbackVO> resolve(
            @PathVariable String feedbackId,
            @RequestBody FeedbackResolveDTO dto) {
        Long adminId = AdminContext.getAdminId();
        return ResponseResult.success(FeedbackVO.from(
                feedbackService.resolve(adminId, feedbackId, dto == null ? null : dto.getResolutionCode())));
    }

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
