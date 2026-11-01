package com.xx.jaseatschoicejava.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.dto.AiConnectivityResult;
import com.xx.jaseatschoicejava.dto.AiModelCandidateDTO;
import com.xx.jaseatschoicejava.dto.AiModelOption;
import com.xx.jaseatschoicejava.dto.AiPreviousConfigSummary;
import com.xx.jaseatschoicejava.dto.AiRuntimeConfigSnapshot;
import com.xx.jaseatschoicejava.service.AiRuntimeConfigService;
import com.xx.jaseatschoicejava.util.SecurityUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 管理端 AI 配置接口。
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
    public ResponseResult<AiConnectivityResult> testConnectivity() {
        return ResponseResult.success(aiRuntimeConfigService.testCurrentConnectivity());
    }

    @ApiOperation("测试候选 AI 配置（不持久化）")
    @PostMapping("/test")
    @PreAuthorize("hasAnyAuthority('admin:setting:ai')")
    public ResponseResult<AiConnectivityResult> testCandidate(@RequestBody AiModelCandidateDTO candidate) {
        return ResponseResult.success(aiRuntimeConfigService.testCandidateConnectivity(candidate));
    }

    @ApiOperation("获取 provider 可用模型列表（不持久化）")
    @PostMapping("/models")
    @PreAuthorize("hasAnyAuthority('admin:setting:ai')")
    public ResponseResult<List<AiModelOption>> discoverModels(@RequestBody AiModelCandidateDTO candidate) {
        return ResponseResult.success(aiRuntimeConfigService.discoverModels(candidate));
    }

    @ApiOperation("测试通过后保存并激活候选 AI 配置")
    @PostMapping("/activate")
    @PreAuthorize("hasAnyAuthority('admin:setting:ai')")
    public ResponseResult<AiRuntimeConfigSnapshot> saveAndActivate(@RequestBody AiModelCandidateDTO candidate) {
        return ResponseResult.success(aiRuntimeConfigService.saveAndActivate(
            candidate, SecurityUtil.getCurrentPhone()));
    }

    @ApiOperation("获取上一版有效 AI 配置摘要")
    @GetMapping("/previous-summary")
    @PreAuthorize("hasAnyAuthority('admin:setting:ai')")
    public ResponseResult<AiPreviousConfigSummary> getPreviousConfigSummary() {
        return ResponseResult.success(aiRuntimeConfigService.getPreviousConfigSummary());
    }

    @ApiOperation("回滚到上一版有效 AI 配置")
    @PostMapping("/rollback")
    @PreAuthorize("hasAnyAuthority('admin:setting:ai')")
    public ResponseResult<AiRuntimeConfigSnapshot> rollbackToPrevious() {
        return ResponseResult.success(aiRuntimeConfigService.rollbackToPrevious(
            SecurityUtil.getCurrentPhone()));
    }
}
