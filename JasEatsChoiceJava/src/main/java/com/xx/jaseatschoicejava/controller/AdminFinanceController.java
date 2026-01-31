package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 管理员-财务管理控制器
 */
@Api(tags = "管理员-财务管理")
@RestController
@RequestMapping("/admin/finance")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFinanceController {

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

        // 模拟数据
        List<Map<String, Object>> records = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("rechargeId", 1000L + i);
            record.put("username", "user" + (i + 1));
            record.put("amount", 100 + i * 10);
            record.put("paymentMethod", i % 2 == 0 ? "WECHAT" : "ALIPAY");
            record.put("status", "SUCCESS");
            record.put("transactionId", "TXN" + System.currentTimeMillis() + i);
            record.put("balanceBefore", 500 + i * 20);
            record.put("balanceAfter", 600 + i * 20);
            record.put("createTime", LocalDateTime.now().minusDays(i));
            records.add(record);
        }

        IPage<Map<String, Object>> result = new Page<>(page, pageSize, 100);
        result.setRecords(records);

        Map<String, Object> response = new HashMap<>();
        response.put("records", records);
        response.put("total", 100);
        response.put("size", pageSize);
        response.put("current", page);
        response.put("pages", 10);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取充值记录详情
     */
    @ApiOperation("获取充值记录详情")
    @GetMapping("/recharges/{rechargeId}")
    @PreAuthorize("hasAnyAuthority('admin:finance:recharges')")
    public ResponseEntity<Map<String, Object>> getRechargeDetail(@PathVariable Long rechargeId) {
        Map<String, Object> record = new HashMap<>();
        record.put("rechargeId", rechargeId);
        record.put("username", "user1");
        record.put("amount", 100);
        record.put("paymentMethod", "WECHAT");
        record.put("status", "SUCCESS");
        record.put("transactionId", "TXN" + System.currentTimeMillis());
        record.put("balanceBefore", 500);
        record.put("balanceAfter", 600);
        record.put("createTime", LocalDateTime.now());
        record.put("remark", "用户充值");

        return ResponseEntity.ok(record);
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

        List<Map<String, Object>> records = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("refundId", 2000L + i);
            record.put("orderId", 3000L + i);
            record.put("username", "user" + (i + 1));
            record.put("refundAmount", 50 + i * 5);
            record.put("reason", i % 3 == 0 ? "商品质量问题" : "不想要了");
            record.put("status", i == 0 ? "PENDING" : "COMPLETED");
            record.put("applyTime", LocalDateTime.now().minusDays(i));
            record.put("processTime", i == 0 ? null : LocalDateTime.now().minusDays(i).plusHours(2));
            record.put("description", "用户申请退款");
            record.put("processComment", i == 0 ? null : "同意退款");
            records.add(record);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("records", records);
        response.put("total", 50);
        response.put("size", pageSize);
        response.put("current", page);
        response.put("pages", 5);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取退款详情
     */
    @ApiOperation("获取退款详情")
    @GetMapping("/refunds/{refundId}")
    @PreAuthorize("hasAnyAuthority('admin:finance:refunds')")
    public ResponseEntity<Map<String, Object>> getRefundDetail(@PathVariable Long refundId) {
        Map<String, Object> record = new HashMap<>();
        record.put("refundId", refundId);
        record.put("orderId", 3000L);
        record.put("username", "user1");
        record.put("refundAmount", 50);
        record.put("reason", "商品质量问题");
        record.put("status", "PENDING");
        record.put("applyTime", LocalDateTime.now());
        record.put("processTime", null);
        record.put("description", "用户申请退款");
        record.put("processComment", null);

        return ResponseEntity.ok(record);
    }

    /**
     * 处理退款申请
     */
    @ApiOperation("处理退款申请")
    @PostMapping("/refunds/{refundId}/process")
    @PreAuthorize("hasAnyAuthority('admin:finance:refunds')")
    public ResponseEntity<Map<String, Object>> processRefund(
            @PathVariable Long refundId,
            @RequestBody Map<String, String> request) {

        String decision = request.get("decision");
        String comment = request.get("comment");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "处理提交成功");
        response.put("decision", decision);
        response.put("comment", comment);

        return ResponseEntity.ok(response);
    }
}
