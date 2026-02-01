package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.RefundRecord;
import com.xx.jaseatschoicejava.entity.RechargeRecord;
import com.xx.jaseatschoicejava.service.RefundRecordService;
import com.xx.jaseatschoicejava.service.RechargeRecordService;
import com.xx.jaseatschoicejava.util.AdminContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员-财务管理控制器（更新版）
 */
@Api(tags = "管理员-财务管理")
@RestController
@RequestMapping("/admin/finance")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFinanceController {

    @Autowired
    private RefundRecordService refundRecordService;

    @Autowired(required = false)
    private RechargeRecordService rechargeRecordService;

    /**
     * 获取充值记录列表（分页）
     */
    @ApiOperation("获取充值记录列表")
    @GetMapping("/recharges")
    @PreAuthorize("hasAnyAuthority('admin:finance:recharges')")
    public ResponseEntity<Map<String, Object>> getRechargeList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String status) {

        // TODO: 实现充值记录查询（如果RechargeRecordService可用）
        // 目前暂时返回模拟数据
        Map<String, Object> response = new HashMap<>();
        response.put("message", "充值记录查询功能待完善");
        response.put("success", false);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取充值记录详情
     */
    @ApiOperation("获取充值记录详情")
    @GetMapping("/recharges/{rechargeId}")
    @PreAuthorize("hasAnyAuthority('admin:finance:recharges')")
    public ResponseEntity<Map<String, Object>> getRechargeDetail(@PathVariable String rechargeId) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "充值记录详情查询功能待完善");
        response.put("success", false);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取退款列表（分页）
     */
    @ApiOperation("获取退款列表")
    @GetMapping("/refunds")
    @PreAuthorize("hasAnyAuthority('admin:finance:refunds')")
    public ResponseEntity<Map<String, Object>> getRefundList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {

        Page<RefundRecord> pageParam = new Page<>(page, pageSize);
        IPage<RefundRecord> result = refundRecordService.getRefundPage(pageParam, keyword, status);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("records", result.getRecords());
        response.put("total", result.getTotal());
        response.put("size", result.getSize());
        response.put("current", result.getCurrent());
        response.put("pages", result.getPages());

        return ResponseEntity.ok(response);
    }

    /**
     * 获取退款详情
     */
    @ApiOperation("获取退款详情")
    @GetMapping("/refunds/{refundId}")
    @PreAuthorize("hasAnyAuthority('admin:finance:refunds')")
    public ResponseEntity<Map<String, Object>> getRefundDetail(@PathVariable String refundId) {
        RefundRecord refundRecord = refundRecordService.getRefundDetail(refundId);

        Map<String, Object> response = new HashMap<>();
        if (refundRecord != null) {
            response.put("success", true);
            response.put("data", refundRecord);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "退款记录不存在");
            return ResponseEntity.status(404).body(response);
        }
    }

    /**
     * 处理退款申请
     */
    @ApiOperation("处理退款申请")
    @PostMapping("/refunds/{refundId}/process")
    @PreAuthorize("hasAnyAuthority('admin:finance:refunds')")
    public ResponseEntity<Map<String, Object>> processRefund(
            @PathVariable String refundId,
            @RequestBody Map<String, String> request) {

        String decision = request.get("decision"); // APPROVE 或 REJECT
        String comment = request.get("comment");

        // 获取当前管理员ID
        Long adminId = AdminContext.getAdminId();

        try {
            boolean success = refundRecordService.processRefund(refundId, decision, comment, adminId);

            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "APPROVE".equals(decision) ? "退款已批准" : "退款已拒绝");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "处理失败");
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "处理失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取退款统计
     */
    @ApiOperation("获取退款统计")
    @GetMapping("/refunds/statistics")
    @PreAuthorize("hasAnyAuthority('admin:finance:refunds')")
    public ResponseEntity<Map<String, Object>> getRefundStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 待处理退款数量
        long pendingCount = refundRecordService.count(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RefundRecord>()
                .eq("status", "PENDING")
        );

        // 今日退款总额
        String today = java.time.LocalDate.now().toString();
        java.math.BigDecimal todayRefund = refundRecordService.sumRefundAmountByStatus("SUCCESS");

        stats.put("pendingCount", pendingCount);
        stats.put("todayRefund", todayRefund != null ? todayRefund : java.math.BigDecimal.ZERO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", stats);

        return ResponseEntity.ok(response);
    }
}
