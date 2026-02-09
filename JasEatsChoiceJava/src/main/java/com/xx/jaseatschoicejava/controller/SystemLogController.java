package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.SystemLog;
import com.xx.jaseatschoicejava.service.SystemLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统日志管理控制器 ✨ 新增
 */
@Api(tags = "管理员-系统日志")
@RestController
@RequestMapping("/admin/system/logs")
@PreAuthorize("hasRole('ADMIN')")
public class SystemLogController {

    @Autowired
    private SystemLogService systemLogService;

    /**
     * 分页查询系统日志
     */
    @ApiOperation("分页查询系统日志")
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('admin:setting:log:view')")
    public ResponseEntity<Map<String, Object>> getLogList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String operatorName,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        Page<SystemLog> pageParam = new Page<>(page, pageSize);
        IPage<SystemLog> result = systemLogService.getLogPage(
            pageParam, operatorName, module, operationType, status, startTime, endTime
        );

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
     * 获取操作统计
     */
    @ApiOperation("获取操作统计")
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyAuthority('admin:setting:log:view')")
    public ResponseEntity<Map<String, Object>> getLogStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 各操作类型统计
            Map<String, Object> operationStats = new HashMap<>();
            operationStats.put("LOGIN", systemLogService.countByOperationType("LOGIN"));
            operationStats.put("LOGOUT", systemLogService.countByOperationType("LOGOUT"));
            operationStats.put("CREATE", systemLogService.countByOperationType("CREATE"));
            operationStats.put("UPDATE", systemLogService.countByOperationType("UPDATE"));
            operationStats.put("DELETE", systemLogService.countByOperationType("DELETE"));
            operationStats.put("QUERY", systemLogService.countByOperationType("QUERY"));
            operationStats.put("EXPORT", systemLogService.countByOperationType("EXPORT"));

            stats.put("operationStats", operationStats);

            // 使用 QueryWrapper 获取总记录数（更安全的方式）
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SystemLog> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            Long totalLogs = systemLogService.count(wrapper);
            stats.put("totalLogs", totalLogs);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 记录详细错误日志
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SystemLogController.class);
            logger.error("获取系统日志统计失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取统计数据失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 清理过期日志
     */
    @ApiOperation("清理过期日志")
    @DeleteMapping("/clean")
    @PreAuthorize("hasAnyAuthority('admin:setting:log')")
    public ResponseEntity<Map<String, Object>> cleanExpiredLogs(
            @RequestParam(defaultValue = "90") Integer days) {

        boolean success = systemLogService.cleanExpiredLogs(days);

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "成功清理" + days + "天前的日志");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "清理失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 导出日志
     */
    @ApiOperation("导出日志")
    @GetMapping("/export")
    @PreAuthorize("hasAnyAuthority('admin:setting:log')")
    public ResponseEntity<Map<String, Object>> exportLogs(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        // TODO: 实现日志导出功能（CSV/Excel）
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "日志导出功能待实现");
        return ResponseEntity.ok(response);
    }
}
