package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.service.MerchantService;
import com.xx.jaseatschoicejava.util.SystemLogHelper;
import com.xx.jaseatschoicejava.util.AdminContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员-商家管理控制器（更新版）
 */
@Api(tags = "管理员-商家管理")
@RestController
@RequestMapping("/admin/merchants")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMerchantController {

    @Autowired
    private MerchantService merchantService;

    /**
     * 获取商家列表（分页）
     */
    @ApiOperation("获取商家列表")
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('admin:merchant:list')")
    public ResponseEntity<IPage<Merchant>> getMerchantList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {

        Page<Merchant> pageParam = new Page<>(page, pageSize);

        // 构建查询条件
        var queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Merchant>();

        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("audit_status", status);
        }

        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like("shop_name", keyword)
                .or()
                .like("contact_name", keyword)
                .or()
                .like("contact_phone", keyword)
            );
        }

        queryWrapper.orderByDesc("create_time");

        IPage<Merchant> result = merchantService.page(pageParam, queryWrapper);

        return ResponseEntity.ok(result);
    }

    /**
     * 获取商家详情
     */
    @ApiOperation("获取商家详情")
    @GetMapping("/{merchantId}")
    @PreAuthorize("hasAnyAuthority('admin:merchant:list')")
    public ResponseEntity<Map<String, Object>> getMerchantDetail(@PathVariable Long merchantId) {
        Merchant merchant = merchantService.getById(merchantId);

        Map<String, Object> response = new HashMap<>();
        if (merchant != null) {
            response.put("success", true);
            response.put("data", merchant);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "商家不存在");
            return ResponseEntity.status(404).body(response);
        }
    }

    /**
     * 审核商家
     */
    @ApiOperation("审核商家")
    @PutMapping("/{merchantId}/audit")
    @PreAuthorize("hasAnyAuthority('admin:merchant:audit')")
    public ResponseEntity<Map<String, Object>> auditMerchant(
            @PathVariable Long merchantId,
            @RequestBody Map<String, Object> request) {

        String status = (String) request.get("status"); // APPROVED, REJECTED
        String reason = (String) request.get("reason");

        Merchant merchant = merchantService.getById(merchantId);
        if (merchant == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "商家不存在");
            return ResponseEntity.status(404).body(response);
        }

        // 检查当前状态
        if (!"PENDING".equals(merchant.getAuditStatus())) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "该商家已审核，无法重复操作");
            return ResponseEntity.status(400).body(response);
        }

        // 更新商家状态
        merchant.setAuditStatus(status);
        merchant.setAuditReason(reason);
        merchant.setAuditTime(LocalDateTime.now());

        // 获取当前管理员ID
        Long adminId = AdminContext.getAdminId();
        merchant.setAuditBy(adminId != null ? adminId.toString() : "SYSTEM");

        boolean success = merchantService.updateById(merchant);

        // 记录操作日志
        if (success) {
            String operation = "APPROVED".equals(status) ? "审核通过" : "审核拒绝";
            SystemLogHelper.logUpdate(
                "商家管理",
                operation + "商家：" + merchant.getShopName(),
                AdminContext.getAdminId(),
                AdminContext.getAdminUsername(),
                Map.of("merchantId", merchantId, "status", status)
            );
        }

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "APPROVED".equals(status) ? "商家审核通过" : "商家已拒绝");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "审核失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 修改商家状态
     */
    @ApiOperation("修改商家状态")
    @PutMapping("/{merchantId}/status")
    @PreAuthorize("hasAnyAuthority('admin:merchant:status')")
    public ResponseEntity<Map<String, Object>> updateMerchantStatus(
            @PathVariable Long merchantId,
            @RequestBody Map<String, String> request) {

        String newStatus = request.get("status"); // ACTIVE, LOCKED, DELETED

        Merchant merchant = merchantService.getById(merchantId);
        if (merchant == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "商家不存在");
            return ResponseEntity.status(404).body(response);
        }

        // 更新状态（转换为Boolean）
        // ACTIVE=true, LOCKED=false, DELETED=特殊处理可以设置为false
        merchant.setStatus("ACTIVE".equalsIgnoreCase(newStatus));

        boolean success = merchantService.updateById(merchant);

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "状态修改成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "状态修改失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取待审核商家列表
     */
    @ApiOperation("获取待审核商家列表")
    @GetMapping("/pending")
    @PreAuthorize("hasAnyAuthority('admin:merchant:audit')")
    public ResponseEntity<IPage<Merchant>> getPendingMerchants(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<Merchant> pageParam = new Page<>(page, pageSize);

        var queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Merchant>()
            .eq("audit_status", "PENDING")
            .orderByDesc("create_time");

        IPage<Merchant> result = merchantService.page(pageParam, queryWrapper);

        return ResponseEntity.ok(result);
    }
}
