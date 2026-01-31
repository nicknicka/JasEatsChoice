package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.service.MerchantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员-商家管理控制器
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
        IPage<Merchant> result = merchantService.page(pageParam);

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
            response.put("merchant", merchant);
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

        // TODO: 实现商家审核逻辑
        // 1. 更新商家状态
        // 2. 发送通知给商家
        // 3. 记录操作日志

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "审核完成");
        return ResponseEntity.ok(response);
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

        String status = request.get("status");

        // TODO: 实现状态修改逻辑

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "状态修改成功");
        return ResponseEntity.ok(response);
    }
}
