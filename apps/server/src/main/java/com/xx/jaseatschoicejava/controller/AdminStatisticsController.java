package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.service.AdminStatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员-统计数据控制器
 */
@Api(tags = "管理员-统计数据")
@RestController
@RequestMapping("/admin/statistics")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final AdminStatisticsService adminStatisticsService;

    /**
     * 获取仪表板统计数据
     */
    @ApiOperation("获取仪表板统计数据")
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('admin:statistics:view')")
    public ResponseResult<Map<String, Object>> getDashboardStatistics(
            @ApiParam("统计天数") @RequestParam(defaultValue = "7") int days) {

        Map<String, Object> data = adminStatisticsService.getDashboardStatistics(days);
        return ResponseResult.success(data, "获取统计数据成功");
    }

    /**
     * 导出统计数据（待实现）
     */
    @ApiOperation("导出统计数据")
    @GetMapping("/export")
    @PreAuthorize("hasAnyAuthority('admin:statistics:export')")
    public ResponseResult<String> exportStatistics(
            @ApiParam("导出参数") @RequestParam(required = false) Map<String, Object> params) {

        // TODO: 实现导出功能
        return ResponseResult.fail("401", "数据导出功能开发中，请稍后");
    }
}
