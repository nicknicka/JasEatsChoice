package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.agent.monitoring.AgentPerformanceMonitor;
import com.xx.jaseatschoicejava.agent.monitoring.CallChainTraceService;
import com.xx.jaseatschoicejava.common.ResponseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Agent监控控制器
 * 提供监控数据查询和调用链追踪的API
 *
 * 注意：此Controller暂时禁用，等待监控系统配置完善后启用
 *
 * @author Claude
 * @since 2026-03-25
 */
// @RestController  // 暂时禁用
// @RequestMapping("/api/admin/agent-monitoring")
public class AgentMonitoringController {

    private static final Logger log = LoggerFactory.getLogger(AgentMonitoringController.class);

    @Resource
    private AgentPerformanceMonitor.PerformanceStats performanceStats;

    @Resource
    private CallChainTraceService callChainTraceService;

    /**
     * 获取性能统计信息
     */
    @GetMapping("/performance-stats")
    public ResponseResult<AgentPerformanceMonitor.PerformanceStats> getPerformanceStats() {
        log.info("查询Agent性能统计");
        return ResponseResult.success(performanceStats);
    }

    /**
     * 获取调用链报告
     */
    @GetMapping("/call-chain/{sessionId}")
    public ResponseResult<String> getCallChainReport(@PathVariable String sessionId) {
        log.info("查询调用链报告: {}", sessionId);
        String report = callChainTraceService.generateReport(sessionId);
        return ResponseResult.success(report);
    }

    /**
     * 清理过期调用链
     */
    @PostMapping("/call-chain/cleanup")
    public ResponseResult<Map<String, Object>> cleanupOldChains(
            @RequestParam(defaultValue = "60") int maxAgeMinutes) {
        log.info("清理过期调用链，最大保留时间: {}分钟", maxAgeMinutes);
        callChainTraceService.cleanupOldChains(maxAgeMinutes);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "清理完成");
        result.put("maxAgeMinutes", maxAgeMinutes);
        return ResponseResult.success(result);
    }

    /**
     * 获取监控概览
     */
    @GetMapping("/overview")
    public ResponseResult<Map<String, Object>> getOverview() {
        log.info("查询Agent监控概览");

        Map<String, Object> overview = new HashMap<>();
        overview.put("performanceStats", performanceStats);
        overview.put("monitoringEnabled", true);
        overview.put("timestamp", System.currentTimeMillis());

        return ResponseResult.success(overview);
    }
}
