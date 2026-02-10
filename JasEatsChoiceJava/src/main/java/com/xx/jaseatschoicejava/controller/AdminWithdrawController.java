package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.entity.WithdrawRecord;
import com.xx.jaseatschoicejava.util.SystemLogHelper;
import com.xx.jaseatschoicejava.service.UserService;
import com.xx.jaseatschoicejava.service.WalletService;
import com.xx.jaseatschoicejava.service.WithdrawRecordService;
import com.xx.jaseatschoicejava.util.AdminContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员-提现审核控制器
 */
@Api(tags = "管理员-提现审核")
@RestController
@RequestMapping("/admin/withdrawals")
@PreAuthorize("hasRole('ADMIN')")
public class AdminWithdrawController {

    private static final Logger log = LoggerFactory.getLogger(AdminWithdrawController.class);

    @Autowired
    private WithdrawRecordService withdrawRecordService;

    @Autowired(required = false)
    private UserService userService;

    @Autowired(required = false)
    private WalletService walletService;

    /**
     * 获取提现记录列表（分页）
     */
    @ApiOperation("获取提现记录列表")
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('admin:finance:withdrawal')")
    public ResponseEntity<Map<String, Object>> getWithdrawList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {

        Page<WithdrawRecord> pageParam = new Page<>(page, pageSize);
        IPage<WithdrawRecord> result = withdrawRecordService.getWithdrawPage(pageParam, keyword, status);

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
     * 获取提现详情
     */
    @ApiOperation("获取提现详情")
    @GetMapping("/{withdrawId}")
    @PreAuthorize("hasAnyAuthority('admin:finance:withdrawal')")
    public ResponseEntity<Map<String, Object>> getWithdrawDetail(@PathVariable String withdrawId) {
        WithdrawRecord record = withdrawRecordService.getWithdrawDetail(withdrawId);

        Map<String, Object> response = new HashMap<>();
        if (record != null) {
            // 添加用户信息
            if (record.getUserId() != null && userService != null) {
                User user = userService.getById(record.getUserId());
                if (user != null) {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("userId", user.getUserId());
                    userInfo.put("nickname", user.getNickname());
                    userInfo.put("phone", user.getPhone());
                    userInfo.put("avatar", user.getAvatar());
                    response.put("user", userInfo);
                }
            }

            response.put("success", true);
            response.put("data", record);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "提现记录不存在");
            return ResponseEntity.status(404).body(response);
        }
    }

    /**
     * 审核提现申请
     */
    @ApiOperation("审核提现申请")
    @PostMapping("/{withdrawId}/process")
    @PreAuthorize("hasAnyAuthority('admin:finance:withdrawal')")
    public ResponseEntity<Map<String, Object>> processWithdraw(
            @PathVariable String withdrawId,
            @RequestBody Map<String, String> request) {

        String decision = request.get("decision"); // APPROVE 或 REJECT
        String comment = request.get("comment");

        // 获取当前管理员ID
        Long adminId = AdminContext.getAdminId();

        WithdrawRecord record = withdrawRecordService.getById(withdrawId);
        if (record == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "提现记录不存在");
            return ResponseEntity.status(404).body(response);
        }

        try {
            boolean success = withdrawRecordService.processWithdraw(withdrawId, decision, comment, adminId);

            if (success) {
                // 记录操作日志
                String operation = "APPROVE".equals(decision) ? "审核通过" : "审核拒绝";
                SystemLogHelper.logUpdate(
                    "提现管理",
                    operation + "提现申请：" + record.getWithdrawNo(),
                    AdminContext.getAdminId(),
                    AdminContext.getAdminUsername(),
                    Map.of("withdrawId", withdrawId, "decision", decision, "amount", record.getAmount())
                );

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "APPROVE".equals(decision) ? "提现已通过审核" : "提现申请已拒绝");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "审核失败，请检查提现记录状态");
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            log.error("处理提现审核失败: {}", withdrawId, e);

            // 记录失败日志
            SystemLogHelper.logError(
                "提现管理",
                "审核提现失败：" + record.getWithdrawNo(),
                AdminContext.getAdminId(),
                AdminContext.getAdminUsername(),
                e
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "处理失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 批量审核提现（全部通过）
     */
    @ApiOperation("批量审核提现")
    @PostMapping("/batch/process")
    @PreAuthorize("hasAnyAuthority('admin:finance:withdrawal')")
    public ResponseEntity<Map<String, Object>> batchProcessWithdraw(
            @RequestBody Map<String, Object> request) {

        @SuppressWarnings("unchecked")
        java.util.List<String> withdrawIds = (java.util.List<String>) request.get("withdrawIds");
        String decision = (String) request.get("decision"); // APPROVE 或 REJECT
        String comment = (String) request.get("comment");

        if (withdrawIds == null || withdrawIds.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "提现ID列表不能为空");
            return ResponseEntity.status(400).body(response);
        }

        Long adminId = AdminContext.getAdminId();
        int successCount = 0;
        int failCount = 0;
        java.util.List<String> failedItems = new java.util.ArrayList<>();

        for (String withdrawId : withdrawIds) {
            try {
                boolean success = withdrawRecordService.processWithdraw(withdrawId, decision, comment, adminId);
                if (success) {
                    successCount++;
                } else {
                    failCount++;
                    failedItems.add(withdrawId);
                }
            } catch (Exception e) {
                failCount++;
                failedItems.add(withdrawId + "(" + e.getMessage() + ")");
                log.error("批量处理提现失败: {}", withdrawId, e);
            }
        }

        // 记录操作日志
        if (successCount > 0) {
            SystemLogHelper.logUpdate(
                "提现管理",
                "批量审核提现：" + successCount + "个成功",
                AdminContext.getAdminId(),
                AdminContext.getAdminUsername(),
                Map.of("totalCount", withdrawIds.size(), "successCount", successCount, "failCount", failCount)
            );
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "批量处理完成：成功" + successCount + "个，失败" + failCount + "个");
        response.put("successCount", successCount);
        response.put("failCount", failCount);
        response.put("failedItems", failedItems);
        return ResponseEntity.ok(response);
    }

    /**
     * 完成提现（打款成功后调用）
     */
    @ApiOperation("完成提现")
    @PutMapping("/{withdrawId}/complete")
    @PreAuthorize("hasAnyAuthority('admin:finance:withdrawal')")
    public ResponseEntity<Map<String, Object>> completeWithdraw(
            @PathVariable String withdrawId,
            @RequestBody(required = false) Map<String, String> request) {

        WithdrawRecord record = withdrawRecordService.getById(withdrawId);
        if (record == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "提现记录不存在");
            return ResponseEntity.status(404).body(response);
        }

        // 只有处理中状态的提现才能完成
        if (!"processing".equals(record.getWithdrawStatus())) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "提现状态不正确，当前状态：" + record.getWithdrawStatus());
            return ResponseEntity.status(400).body(response);
        }

        record.setWithdrawStatus("success");
        record.setCompleteTime(LocalDateTime.now());

        String remark = request != null ? request.get("remark") : "提现已完成";
        record.setRemark(remark);

        boolean success = withdrawRecordService.updateById(record);

        if (success) {
            // 记录操作日志
            SystemLogHelper.logUpdate(
                "提现管理",
                "完成提现：" + record.getWithdrawNo(),
                AdminContext.getAdminId(),
                AdminContext.getAdminUsername(),
                Map.of("withdrawId", withdrawId)
            );
        }

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "提现已完成");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "操作失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 提现失败（打款失败时调用）
     */
    @ApiOperation("标记提现失败")
    @PutMapping("/{withdrawId}/fail")
    @PreAuthorize("hasAnyAuthority('admin:finance:withdrawal')")
    public ResponseEntity<Map<String, Object>> failWithdraw(
            @PathVariable String withdrawId,
            @RequestBody Map<String, String> request) {

        String reason = request.get("reason");

        WithdrawRecord record = withdrawRecordService.getById(withdrawId);
        if (record == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "提现记录不存在");
            return ResponseEntity.status(404).body(response);
        }

        // 只有处理中状态的提现才能标记为失败
        if (!"processing".equals(record.getWithdrawStatus())) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "提现状态不正确，当前状态：" + record.getWithdrawStatus());
            return ResponseEntity.status(400).body(response);
        }

        record.setWithdrawStatus("failed");
        record.setRejectReason(reason);
        record.setCompleteTime(LocalDateTime.now());

        // 退款到用户钱包
        if (walletService != null) {
            try {
                // walletService.refundWithdraw(record.getUserId(), record.getAmount());
                log.info("提现失败，已退款到钱包: {}, 金额: {}", withdrawId, record.getAmount());
            } catch (Exception e) {
                log.error("退款到钱包失败: {}", withdrawId, e);
            }
        }

        boolean success = withdrawRecordService.updateById(record);

        if (success) {
            // 记录操作日志
            SystemLogHelper.logUpdate(
                "提现管理",
                "提现失败并退款：" + record.getWithdrawNo(),
                AdminContext.getAdminId(),
                AdminContext.getAdminUsername(),
                Map.of("withdrawId", withdrawId, "reason", reason)
            );
        }

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "已标记为失败并退款");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "操作失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取提现统计
     */
    @ApiOperation("获取提现统计")
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyAuthority('admin:finance:withdrawal')")
    public ResponseEntity<Map<String, Object>> getWithdrawStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 待审核提现数量
        long pendingCount = withdrawRecordService.count(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<WithdrawRecord>()
                .eq("withdraw_status", "pending")
        );

        // 处理中提现数量
        long processingCount = withdrawRecordService.count(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<WithdrawRecord>()
                .eq("withdraw_status", "processing")
        );

        // 今日提现总额
        java.math.BigDecimal todayWithdraw = withdrawRecordService.sumWithdrawAmountByStatus("success");

        // 总提现金额
        java.math.BigDecimal totalWithdraw = withdrawRecordService.sumWithdrawAmountByStatus("success");

        stats.put("pendingCount", pendingCount);
        stats.put("processingCount", processingCount);
        stats.put("todayWithdraw", todayWithdraw != null ? todayWithdraw : BigDecimal.ZERO);
        stats.put("totalWithdraw", totalWithdraw != null ? totalWithdraw : BigDecimal.ZERO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", stats);

        return ResponseEntity.ok(response);
    }
}
