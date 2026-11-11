package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.dto.AgentRuntimeConfigDTO;
import com.xx.jaseatschoicejava.dto.AgentRuntimeHealthVO;
import com.xx.jaseatschoicejava.service.AgentRuntimeConfigService;
import com.xx.jaseatschoicejava.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员-Agent 运行时配置。
 *
 * <p>整个控制器只对 admin:agent:runtime 开放；模型 key 只写不可读。
 */
@Api(tags = "管理员-Agent 运行时")
@RestController
@RequestMapping("/admin/agent/runtime")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAgentRuntimeController {

    @Autowired
    private AgentRuntimeConfigService agentRuntimeConfigService;

    @ApiOperation("获取运行时配置、健康与硬边界")
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('admin:agent:runtime')")
    public ResponseResult<AgentRuntimeHealthVO> getRuntime() {
        return ResponseResult.success(agentRuntimeConfigService.getHealth());
    }

    @ApiOperation("更新供应商/模型/endpoint/数据政策")
    @PutMapping("")
    @PreAuthorize("hasAnyAuthority('admin:agent:runtime')")
    public ResponseResult<AgentRuntimeHealthVO> update(@RequestBody AgentRuntimeConfigDTO dto) {
        return ResponseResult.success(agentRuntimeConfigService.update(dto, SecurityUtil.getCurrentPhone()));
    }

    @ApiOperation("轮换模型 key（只写不可读）")
    @PostMapping("/key")
    @PreAuthorize("hasAnyAuthority('admin:agent:runtime')")
    public ResponseResult<AgentRuntimeHealthVO> rotateKey(@RequestBody Map<String, String> body) {
        return ResponseResult.success(agentRuntimeConfigService.rotateKey(
                body == null ? null : body.get("key"), SecurityUtil.getCurrentPhone()));
    }

    @ApiOperation("测试模型连接")
    @PostMapping("/key/test")
    @PreAuthorize("hasAnyAuthority('admin:agent:runtime')")
    public ResponseResult<AgentRuntimeHealthVO.Health> testConnection() {
        return ResponseResult.success(agentRuntimeConfigService.testConnectivity());
    }

    @ApiOperation("独立吊销模型 key")
    @DeleteMapping("/key")
    @PreAuthorize("hasAnyAuthority('admin:agent:runtime')")
    public ResponseResult<AgentRuntimeHealthVO> revokeKey() {
        return ResponseResult.success(agentRuntimeConfigService.revokeKey());
    }
}
