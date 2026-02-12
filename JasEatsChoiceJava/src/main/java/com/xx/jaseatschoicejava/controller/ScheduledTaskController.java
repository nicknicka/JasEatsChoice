package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.ScheduledTask;
import com.xx.jaseatschoicejava.entity.ScheduledTaskLog;
import com.xx.jaseatschoicejava.service.DynamicScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 定时任务管理控制器
 *
 * @author Claude
 * @since 2025-02-12
 */
@Slf4j
@RestController
@RequestMapping("/v1/scheduled-tasks")
public class ScheduledTaskController {

    @Autowired
    private DynamicScheduleService dynamicScheduleService;

    /**
     * 获取所有定时任务列表
     */
    @GetMapping
    public ResponseResult<?> getTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String taskGroup) {

        LambdaQueryWrapper<ScheduledTask> queryWrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            queryWrapper.eq(ScheduledTask::getStatus, status);
        }
        if (taskGroup != null) {
            queryWrapper.eq(ScheduledTask::getTaskGroup, taskGroup);
        }
        queryWrapper.orderByDesc(ScheduledTask::getCreateTime);

        // TODO: 实现分页查询
        List<ScheduledTask> tasks = dynamicScheduleService.getRunningTasks();

        return ResponseResult.success(Map.of(
                "tasks", tasks,
                "total", tasks.size()
        ));
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{taskId}")
    public ResponseResult<?> getTaskDetail(@PathVariable String taskId) {
        Map<String, Object> status = dynamicScheduleService.getTaskStatus(taskId);
        return ResponseResult.success(status);
    }

    /**
     * 创建定时任务
     */
    @PostMapping
    public ResponseResult<?> createTask(@RequestBody ScheduledTask task) {
        // 设置默认值
        if (task.getStatus() == null) {
            task.setStatus("ENABLED");
        }
        if (task.getExecuteCount() == null) {
            task.setExecuteCount(0);
        }
        if (task.getFailCount() == null) {
            task.setFailCount(0);
        }
        if (task.getLogExecution() == null) {
            task.setLogExecution(true);
        }
        if (task.getPriority() == null) {
            task.setPriority(2);
        }

        boolean result = dynamicScheduleService.addAndStartTask(task);
        if (result) {
            return ResponseResult.success(task, "任务创建成功");
        }
        return ResponseResult.fail("500", "任务创建失败");
    }

    /**
     * 更新定时任务
     */
    @PutMapping("/{taskId}")
    public ResponseResult<?> updateTask(@PathVariable String taskId, @RequestBody ScheduledTask task) {
        task.setId(taskId);
        boolean result = dynamicScheduleService.updateAndRestartTask(task);
        if (result) {
            return ResponseResult.success(task, "任务更新成功");
        }
        return ResponseResult.fail("500", "任务更新失败");
    }

    /**
     * 删除定时任务
     */
    @DeleteMapping("/{taskId}")
    public ResponseResult<?> deleteTask(@PathVariable String taskId) {
        boolean result = dynamicScheduleService.stopAndRemoveTask(taskId);
        if (result) {
            return ResponseResult.success("任务删除成功");
        }
        return ResponseResult.fail("500", "任务删除失败");
    }

    /**
     * 暂停任务
     */
    @PutMapping("/{taskId}/pause")
    public ResponseResult<?> pauseTask(@PathVariable String taskId) {
        boolean result = dynamicScheduleService.pauseTask(taskId);
        if (result) {
            return ResponseResult.success("任务暂停成功");
        }
        return ResponseResult.fail("500", "任务暂停失败");
    }

    /**
     * 恢复任务
     */
    @PutMapping("/{taskId}/resume")
    public ResponseResult<?> resumeTask(@PathVariable String taskId) {
        boolean result = dynamicScheduleService.resumeTask(taskId);
        if (result) {
            return ResponseResult.success("任务恢复成功");
        }
        return ResponseResult.fail("500", "任务恢复失败");
    }

    /**
     * 立即执行任务
     */
    @PostMapping("/{taskId}/execute")
    public ResponseResult<?> executeNow(@PathVariable String taskId) {
        boolean result = dynamicScheduleService.executeNow(taskId);
        if (result) {
            return ResponseResult.success("任务已加入执行队列");
        }
        return ResponseResult.fail("500", "任务执行失败");
    }

    /**
     * 刷新任务配置
     */
    @PostMapping("/{taskId}/refresh")
    public ResponseResult<?> refreshTask(@PathVariable String taskId) {
        boolean result = dynamicScheduleService.refreshTask(taskId);
        if (result) {
            return ResponseResult.success("任务刷新成功");
        }
        return ResponseResult.fail("500", "任务刷新失败");
    }

    /**
     * 获取任务执行历史
     */
    @GetMapping("/{taskId}/logs")
    public ResponseResult<?> getTaskLogs(
            @PathVariable String taskId,
            @RequestParam(defaultValue = "50") int limit) {

        List<ScheduledTaskLog> logs = dynamicScheduleService.getTaskExecutionHistory(taskId, limit);
        return ResponseResult.success(logs);
    }

    /**
     * 获取运行中的任务列表
     */
    @GetMapping("/running")
    public ResponseResult<?> getRunningTasks() {
        List<ScheduledTask> tasks = dynamicScheduleService.getRunningTasks();
        return ResponseResult.success(tasks);
    }

    /**
     * 清理旧日志
     */
    @DeleteMapping("/logs/clean")
    public ResponseResult<?> cleanOldLogs(@RequestParam(defaultValue = "30") int days) {
        int count = dynamicScheduleService.cleanOldLogs(days);
        return ResponseResult.success(Map.of(
                "cleanedCount", count,
                "message", "清理了" + count + "条超过" + days + "天的日志"
        ));
    }
}
