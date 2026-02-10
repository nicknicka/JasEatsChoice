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
    @PreAuthorize("hasAnyAuthority('admin:finance:recharge')")
    public ResponseEntity<Map<String, Object>> getRechargeList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String status) {

        if (rechargeRecordService == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "充值记录服务不可用");
            return ResponseEntity.status(503).body(response);
        }

        Page<RechargeRecord> pageParam = new Page<>(page, pageSize);
        IPage<RechargeRecord> result = rechargeRecordService.getRechargePage(pageParam, keyword, paymentMethod, status);

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
     * 获取充值记录详情
     */
    @ApiOperation("获取充值记录详情")
    @GetMapping("/recharges/{rechargeId}")
    @PreAuthorize("hasAnyAuthority('admin:finance:recharge')")
    public ResponseEntity<Map<String, Object>> getRechargeDetail(@PathVariable String rechargeId) {
        if (rechargeRecordService == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "充值记录服务不可用");
            return ResponseEntity.status(503).body(response);
        }

        RechargeRecord rechargeRecord = rechargeRecordService.getRechargeDetail(rechargeId);

        Map<String, Object> response = new HashMap<>();
        if (rechargeRecord != null) {
            response.put("success", true);
            response.put("data", rechargeRecord);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "充值记录不存在");
            return ResponseEntity.status(404).body(response);
        }
    }

    /**
     * 获取退款列表（分页）
     */
    @ApiOperation("获取退款列表")
    @GetMapping("/refunds")
    @PreAuthorize("hasAnyAuthority('admin:finance:refund')")
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
    @PreAuthorize("hasAnyAuthority('admin:finance:refund')")
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
    @PreAuthorize("hasAnyAuthority('admin:finance:refund')")
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
    @GetMapping("/refunds/stats")
    @PreAuthorize("hasAnyAuthority('admin:finance:refund')")
    public ResponseEntity<Map<String, Object>> getRefundStatistics() {
        // 待处理退款数量
        long pending = refundRecordService.count(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RefundRecord>()
                .eq("status", "PENDING")
        );

        // 今日已批准退款总额
        java.time.LocalDateTime todayStart = java.time.LocalDate.now().atStartOfDay();
        java.util.List<RefundRecord> todayApprovedRecords = refundRecordService.list(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RefundRecord>()
                .eq("status", "COMPLETED")
                .ge("process_time", todayStart)
        );

        java.math.BigDecimal todayApproved = todayApprovedRecords.stream()
            .map(RefundRecord::getRefundAmount)
            .filter(amount -> amount != null)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        // 今日已拒绝数量
        long todayRejected = refundRecordService.count(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RefundRecord>()
                .eq("status", "REJECTED")
                .ge("process_time", todayStart)
        );

        // 本月退款总额
        java.time.LocalDateTime monthStart = java.time.LocalDate.now().withDayOfMonth(1).atStartOfDay();
        java.util.List<RefundRecord> monthRecords = refundRecordService.list(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RefundRecord>()
                .eq("status", "COMPLETED")
                .ge("process_time", monthStart)
        );

        java.math.BigDecimal monthTotal = monthRecords.stream()
            .map(RefundRecord::getRefundAmount)
            .filter(amount -> amount != null)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        Map<String, Object> response = new HashMap<>();
        response.put("pending", pending);
        response.put("todayApproved", todayApproved != null ? todayApproved : java.math.BigDecimal.ZERO);
        response.put("todayRejected", todayRejected);
        response.put("monthTotal", monthTotal != null ? monthTotal : java.math.BigDecimal.ZERO);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取充值统计
     */
    @ApiOperation("获取充值统计")
    @GetMapping("/recharges/stats")
    @PreAuthorize("hasAnyAuthority('admin:finance:recharge')")
    public ResponseEntity<Map<String, Object>> getRechargeStatistics() {
        if (rechargeRecordService == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "充值记录服务不可用");
            return ResponseEntity.status(503).body(response);
        }

        // 今日充值统计
        java.time.LocalDateTime todayStart = java.time.LocalDate.now().atStartOfDay();
        java.util.List<RechargeRecord> todayRecords = rechargeRecordService.list(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RechargeRecord>()
                .eq("recharge_status", "success")
                .ge("create_time", todayStart)
        );

        java.math.BigDecimal todayAmount = todayRecords.stream()
            .map(RechargeRecord::getAmount)
            .filter(amount -> amount != null)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        long todayCount = todayRecords.size();

        // 本月充值统计
        java.time.LocalDateTime monthStart = java.time.LocalDate.now().withDayOfMonth(1).atStartOfDay();
        java.util.List<RechargeRecord> monthRecords = rechargeRecordService.list(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RechargeRecord>()
                .eq("recharge_status", "success")
                .ge("create_time", monthStart)
        );

        java.math.BigDecimal monthAmount = monthRecords.stream()
            .map(RechargeRecord::getAmount)
            .filter(amount -> amount != null)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        long monthCount = monthRecords.size();

        Map<String, Object> response = new HashMap<>();
        response.put("todayAmount", todayAmount != null ? todayAmount : java.math.BigDecimal.ZERO);
        response.put("todayCount", todayCount);
        response.put("monthAmount", monthAmount != null ? monthAmount : java.math.BigDecimal.ZERO);
        response.put("monthCount", monthCount);

        return ResponseEntity.ok(response);
    }
}
