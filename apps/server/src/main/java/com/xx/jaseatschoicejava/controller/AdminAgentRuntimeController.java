package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.dto.AgentRuntimeHealthVO;
import com.xx.jaseatschoicejava.service.AgentRuntimeConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员-Agent 运行时状态（只读）。
 *
 * <p>agent 的实际运行与配置在 DSH（另一个运行时），JAS 配不了它；本接口只读展示主机侧健康与硬边界。
 */
@Api(tags = "管理员-Agent 运行时")
@RestController
@RequestMapping("/admin/agent/runtime")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAgentRuntimeController {

    @Autowired
    private AgentRuntimeConfigService agentRuntimeConfigService;

    @ApiOperation("获取运行状态与硬边界")
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('admin:agent:runtime')")
    public ResponseResult<AgentRuntimeHealthVO> getRuntime() {
        return ResponseResult.success(agentRuntimeConfigService.getHealth());
    }
}
