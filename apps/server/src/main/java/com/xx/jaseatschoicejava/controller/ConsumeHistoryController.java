package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.ConsumeHistory;
import com.xx.jaseatschoicejava.service.ConsumeHistoryService;
import com.xx.jaseatschoicejava.common.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/consume-history")
@RequiredArgsConstructor
public class ConsumeHistoryController {

    private final ConsumeHistoryService consumeHistoryService;

    /**
     * 获取用户消费记录
     * @param userId 用户ID
     * @param type 类型（all/recharge/consume/withdraw）
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @param page 页码
     * @param pageSize 每页条数
     * @return 消费记录分页结果
     */
    @GetMapping
    public ResponseResult<?> getConsumeHistory(
            @RequestParam String userId,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        Page<ConsumeHistory> historyPage = consumeHistoryService.getConsumeHistoryByUserId(
                userId, type, startDate, endDate, page, pageSize
        );

        return ResponseResult.success(historyPage);
    }
}
