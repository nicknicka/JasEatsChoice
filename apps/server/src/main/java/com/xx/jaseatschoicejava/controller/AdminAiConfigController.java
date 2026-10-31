package com.xx.jaseatschoicejava.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.dto.AiRuntimeConfigSnapshot;
import com.xx.jaseatschoicejava.service.AiRuntimeConfigService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 管理端 AI 配置只读切片。
 */
@Api(tags = "管理员-AI配置")
@RestController
@RequestMapping("/admin/settings/ai")
public class AdminAiConfigController {

    private final AiRuntimeConfigService aiRuntimeConfigService;

    public AdminAiConfigController(AiRuntimeConfigService aiRuntimeConfigService) {
        this.aiRuntimeConfigService = aiRuntimeConfigService;
    }

    @ApiOperation("获取当前 AI 运行时配置快照")
    @GetMapping("/current")
    @PreAuthorize("hasAnyAuthority('admin:setting:ai')")
    public ResponseResult<AiRuntimeConfigSnapshot> getCurrentConfig() {
        return ResponseResult.success(aiRuntimeConfigService.getCurrentSnapshot());
    }

    @ApiOperation("测试当前 AI 配置连通性")
    @PostMapping("/connectivity-test")
    @PreAuthorize("hasAnyAuthority('admin:setting:ai')")
    public ResponseResult<com.xx.jaseatschoicejava.dto.AiConnectivityResult> testConnectivity() {
        return ResponseResult.success(aiRuntimeConfigService.testCurrentConnectivity());
    }
}
