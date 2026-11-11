package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.dto.FeedbackCreateDTO;
import com.xx.jaseatschoicejava.entity.Feedback;
import com.xx.jaseatschoicejava.service.FeedbackService;
import com.xx.jaseatschoicejava.util.UserIdentityUtil;
import com.xx.jaseatschoicejava.vo.FeedbackPageVO;
import com.xx.jaseatschoicejava.vo.FeedbackVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 用户端反馈接口。
 *
 * <p>身份只来自可信 JWT：请求体里的 userId 不参与归属判断。
 */
@Api(tags = "用户反馈")
@RestController
@RequestMapping("/v1/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @ApiOperation("提交反馈")
    @PostMapping("")
    public ResponseResult<FeedbackVO> create(@RequestBody FeedbackCreateDTO dto, HttpServletRequest request) {
        String userId = requireUserId(request);
        return ResponseResult.success(FeedbackVO.from(feedbackService.create(userId, dto)));
    }

    @ApiOperation("查看自己的反馈历史")
    @GetMapping("")
    public ResponseResult<FeedbackPageVO> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        String userId = requireUserId(request);
        IPage<Feedback> result = feedbackService.listOwn(userId, page, size, type, status);
        List<FeedbackVO> list = result.getRecords().stream().map(FeedbackVO::from).toList();
        return ResponseResult.success(new FeedbackPageVO(list, result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @ApiOperation("查看自己的反馈详情")
    @GetMapping("/{feedbackId}")
    public ResponseResult<FeedbackVO> detail(@PathVariable String feedbackId, HttpServletRequest request) {
        String userId = requireUserId(request);
        return ResponseResult.success(FeedbackVO.from(feedbackService.getOwn(userId, feedbackId)));
    }

    private String requireUserId(HttpServletRequest request) {
        String userId = UserIdentityUtil.extractVerifiedUserId(request);
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        return userId;
    }
}
