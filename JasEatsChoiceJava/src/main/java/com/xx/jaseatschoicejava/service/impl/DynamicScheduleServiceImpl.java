package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.entity.ScheduledTask;
import com.xx.jaseatschoicejava.entity.ScheduledTaskLog;
import com.xx.jaseatschoicejava.mapper.ScheduledTaskLogMapper;
import com.xx.jaseatschoicejava.mapper.ScheduledTaskMapper;
import com.xx.jaseatschoicejava.service.DynamicScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 动态定时任务服务实现
 * 使用Spring TaskScheduler管理动态任务
 *
 * @author Claude
 * @since 2025-02-12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicScheduleServiceImpl implements DynamicScheduleService {

    private final ScheduledTaskMapper scheduledTaskMapper;
    private final ScheduledTaskLogMapper scheduledTaskLogMapper;
    private final TaskScheduler taskScheduler;

    // 存储任务ID到ScheduledFuture的映射
    private final Map<String, ScheduledFuture<?>> taskFutures = new ConcurrentHashMap<>();

    // 存储运行中的任务信息
    private final Map<String, Map<String, Object>> runningTasks = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("初始化动态定时任务系统...");
        initTasks();
    }

    @PreDestroy
    public void destroy() {
        log.info("关闭动态定时任务系统...");
        taskFutures.values().forEach(future -> future.cancel(false));
        taskFutures.clear();
        runningTasks.clear();
    }

    @Override
    public void initTasks() {
        log.info("初始化所有启用的定时任务...");

        // 查询所有启用的任务
        List<ScheduledTask> tasks = scheduledTaskMapper.selectList(
                new LambdaQueryWrapper<ScheduledTask>()
                        .eq(ScheduledTask::getStatus, "ENABLED")
        );

        log.info("找到 {} 个启用的定时任务", tasks.size());

        // 启动每个任务
        for (ScheduledTask task : tasks) {
            try {
                addAndStartTask(task);
                log.info("任务 [{}] 启动成功", task.getTaskName());
            } catch (Exception e) {
                log.error("任务 [{}] 启动失败: {}", task.getTaskName(), e.getMessage(), e);
            }
        }

        log.info("定时任务系统初始化完成");
    }

    @Override
    public boolean addAndStartTask(ScheduledTask task) {
        try {
            String taskId = task.getId();

            // 如果任务已存在，先停止
            if (taskFutures.containsKey(taskId)) {
                stopAndRemoveTask(taskId);
            }

            // 根据任务类型调度
            ScheduledFuture<?> future = null;

            switch (task.getTaskType()) {
                case "CRON":
                    // Cron表达式任务
                    CronTrigger cronTrigger = new CronTrigger(task.getCronExpression());
                    future = taskScheduler.schedule(
                            () -> executeTask(task),
                            cronTrigger
                    );
                    break;

                case "FIXED_RATE":
                    // 固定频率任务
                    future = taskScheduler.scheduleAtFixedRate(
                            () -> executeTask(task),
                            Instant.now(),
                            Duration.ofMillis(task.getRateInMillis())
                    );
                    break;

                case "FIXED_DELAY":
                    // 固定延迟任务
                    future = taskScheduler.scheduleWithFixedDelay(
                            () -> executeTask(task),
                            Instant.now(),
                            Duration.ofMillis(task.getRateInMillis())
                    );
                    break;

                case "ONCE":
                    // 一次性任务
                    future = taskScheduler.schedule(
                            () -> executeTask(task),
                            Instant.now()
                    );
                    break;

                default:
                    log.error("不支持的任务类型: {}", task.getTaskType());
                    return false;
            }

            if (future != null) {
                taskFutures.put(taskId, future);

                // 存储运行中的任务信息
                Map<String, Object> taskInfo = new HashMap<>();
                taskInfo.put("task", task);
                taskInfo.put("startTime", LocalDateTime.now());
                runningTasks.put(taskId, taskInfo);

                // 更新任务状态
                task.setStatus("RUNNING");
                scheduledTaskMapper.updateById(task);

                log.info("任务 [{}] 已添加并启动", task.getTaskName());
                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("添加并启动任务失败: {}", task.getTaskName(), e);
            return false;
        }
    }

    @Override
    public boolean updateAndRestartTask(ScheduledTask task) {
        // 先停止旧任务
        stopAndRemoveTask(task.getId());

        // 重新启动任务
        return addAndStartTask(task);
    }

    @Override
    public boolean pauseTask(String taskId) {
        ScheduledFuture<?> future = taskFutures.get(taskId);
        if (future != null) {
            future.cancel(false);
            taskFutures.remove(taskId);
            runningTasks.remove(taskId);

            // 更新任务状态
            ScheduledTask task = scheduledTaskMapper.selectById(taskId);
            if (task != null) {
                task.setStatus("PAUSED");
                scheduledTaskMapper.updateById(task);
            }

            log.info("任务 [{}] 已暂停", taskId);
            return true;
        }
        return false;
    }

    @Override
    public boolean resumeTask(String taskId) {
        ScheduledTask task = scheduledTaskMapper.selectById(taskId);
        if (task != null && "PAUSED".equals(task.getStatus())) {
            task.setStatus("ENABLED");
            scheduledTaskMapper.updateById(task);
            return addAndStartTask(task);
        }
        return false;
    }

    @Override
    public boolean executeNow(String taskId) {
        ScheduledTask task = scheduledTaskMapper.selectById(taskId);
        if (task != null) {
            // 异步执行
            taskScheduler.schedule(() -> executeTask(task), Instant.now());
            log.info("任务 [{}] 已立即执行", task.getTaskName());
            return true;
        }
        return false;
    }

    @Override
    public boolean stopAndRemoveTask(String taskId) {
        ScheduledFuture<?> future = taskFutures.remove(taskId);
        if (future != null) {
            future.cancel(false);
        }

        runningTasks.remove(taskId);

        // 更新任务状态
        ScheduledTask task = scheduledTaskMapper.selectById(taskId);
        if (task != null) {
            task.setStatus("DISABLED");
            scheduledTaskMapper.updateById(task);
        }

        log.info("任务 [{}] 已停止并移除", taskId);
        return true;
    }

    @Override
    public boolean refreshTask(String taskId) {
        ScheduledTask task = scheduledTaskMapper.selectById(taskId);
        if (task != null) {
            return updateAndRestartTask(task);
        }
        return false;
    }

    @Override
    public Map<String, Object> getTaskStatus(String taskId) {
        Map<String, Object> status = new HashMap<>();

        ScheduledTask task = scheduledTaskMapper.selectById(taskId);
        if (task != null) {
            status.put("task", task);
            status.put("isRunning", taskFutures.containsKey(taskId));
            status.put("taskInfo", runningTasks.get(taskId));
        }

        return status;
    }

    @Override
    public boolean logTaskExecution(String taskId, ScheduledTaskLog taskLog) {
        try {
            taskLog.setTaskId(taskId);
            taskLog.setCreateTime(LocalDateTime.now());
            scheduledTaskLogMapper.insert(taskLog);
            return true;
        } catch (Exception e) {
            this.log.error("记录任务执行日志失败: {}", taskId, e);
            return false;
        }
    }

    @Override
    public List<ScheduledTaskLog> getTaskExecutionHistory(String taskId, int limit) {
        return scheduledTaskLogMapper.selectList(
                new LambdaQueryWrapper<ScheduledTaskLog>()
                        .eq(ScheduledTaskLog::getTaskId, taskId)
                        .orderByDesc(ScheduledTaskLog::getStartTime)
                        .last("LIMIT " + limit)
        );
    }

    @Override
    public List<ScheduledTask> getRunningTasks() {
        List<ScheduledTask> tasks = new ArrayList<>();
        for (String taskId : taskFutures.keySet()) {
            ScheduledTask task = scheduledTaskMapper.selectById(taskId);
            if (task != null) {
                tasks.add(task);
            }
        }
        return tasks;
    }

    @Override
    public int cleanOldLogs(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        int deleted = scheduledTaskLogMapper.delete(
                new LambdaQueryWrapper<ScheduledTaskLog>()
                        .lt(ScheduledTaskLog::getStartTime, cutoffDate)
        );
        log.info("清理了 {} 条超过 {} 天的任务日志", deleted, days);
        return deleted;
    }

    /**
     * 执行任务
     */
    private void executeTask(ScheduledTask task) {
        String taskId = task.getId();
        LocalDateTime startTime = LocalDateTime.now();

        log.info("开始执行任务: {}", task.getTaskName());

        ScheduledTaskLog taskLog = new ScheduledTaskLog();
        taskLog.setTaskId(taskId);
        taskLog.setTaskName(task.getTaskName());
        taskLog.setTaskCode(task.getTaskCode());
        taskLog.setStartTime(startTime);
        taskLog.setExecuteStatus("RUNNING");

        try {
            // TODO: 根据taskClassName和taskMethodName反射调用
            // 这里简化处理，实际应该使用反射调用具体的方法
            log.debug("执行任务逻辑: {} - {}", task.getTaskClassName(), task.getTaskMethodName());

            // 模拟任务执行
            Thread.sleep(1000);

            // 更新任务信息
            task.setLastExecuteTime(startTime);
            task.setExecuteCount((task.getExecuteCount() != null ? task.getExecuteCount() : 0) + 1);
            task.setLastExecuteResult("SUCCESS");
            task.setRetriedCount(0);

            // 记录日志
            LocalDateTime endTime = LocalDateTime.now();
            taskLog.setEndTime(endTime);
            taskLog.setDuration(java.time.Duration.between(startTime, endTime).toMillis());
            taskLog.setExecuteStatus("SUCCESS");
            taskLog.setResultMessage("任务执行成功");

            log.info("任务 [{}] 执行成功，耗时: {} ms", task.getTaskName(),
                    taskLog.getDuration());

        } catch (Exception e) {
            log.error("任务 [{}] 执行失败", task.getTaskName(), e);

            // 更新任务信息
            task.setLastExecuteResult("FAILED");
            task.setLastErrorMessage(e.getMessage());
            task.setFailCount((task.getFailCount() != null ? task.getFailCount() : 0) + 1);

            // 记录日志
            LocalDateTime endTime = LocalDateTime.now();
            taskLog.setEndTime(endTime);
            taskLog.setDuration(java.time.Duration.between(startTime, endTime).toMillis());
            taskLog.setExecuteStatus("FAILED");
            taskLog.setErrorMessage(e.getMessage());
            taskLog.setExceptionStack(getStackTrace(e));

            // 重试逻辑
            if (task.getRetryCount() != null && task.getRetryCount() > 0) {
                int retried = task.getRetriedCount() != null ? task.getRetriedCount() : 0;
                if (retried < task.getRetryCount()) {
                    task.setRetriedCount(retried + 1);
                    log.info("任务 [{}] 将进行第 {} 次重试", task.getTaskName(), retried + 1);
                    // 延迟重试
                    taskScheduler.schedule(() -> executeTask(task),
                            Instant.now().plusSeconds(60));
                }
            }

        } finally {
            // 保存执行日志
            if (task.getLogExecution() != null && task.getLogExecution()) {
                logTaskExecution(taskId, taskLog);
            }

            // 更新任务信息
            scheduledTaskMapper.updateById(task);
        }
    }

    /**
     * 获取异常堆栈信息
     */
    private String getStackTrace(Throwable throwable) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
