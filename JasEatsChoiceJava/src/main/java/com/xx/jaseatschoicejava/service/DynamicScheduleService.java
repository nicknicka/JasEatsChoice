package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.entity.ScheduledTask;
import com.xx.jaseatschoicejava.entity.ScheduledTaskLog;

import java.util.List;
import java.util.Map;

/**
 * 动态定时任务服务接口
 *
 * @author Claude
 * @since 2025-02-12
 */
public interface DynamicScheduleService {

    /**
     * 初始化所有启用的定时任务
     */
    void initTasks();

    /**
     * 添加并启动定时任务
     *
     * @param task 任务配置
     * @return 是否成功
     */
    boolean addAndStartTask(ScheduledTask task);

    /**
     * 更新任务配置并重启
     *
     * @param task 任务配置
     * @return 是否成功
     */
    boolean updateAndRestartTask(ScheduledTask task);

    /**
     * 暂停任务
     *
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean pauseTask(String taskId);

    /**
     * 恢复任务
     *
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean resumeTask(String taskId);

    /**
     * 立即执行任务（手动触发）
     *
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean executeNow(String taskId);

    /**
     * 停止并删除任务
     *
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean stopAndRemoveTask(String taskId);

    /**
     * 刷新任务（重新加载配置）
     *
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean refreshTask(String taskId);

    /**
     * 获取任务执行状态
     *
     * @param taskId 任务ID
     * @return 任务状态信息
     */
    Map<String, Object> getTaskStatus(String taskId);

    /**
     * 记录任务执行日志
     *
     * @param taskId 任务ID
     * @param log 日志信息
     * @return 是否成功
     */
    boolean logTaskExecution(String taskId, ScheduledTaskLog log);

    /**
     * 获取任务执行历史
     *
     * @param taskId 任务ID
     * @param limit 限制数量
     * @return 执行日志列表
     */
    List<ScheduledTaskLog> getTaskExecutionHistory(String taskId, int limit);

    /**
     * 获取所有运行中的任务
     *
     * @return 任务列表
     */
    List<ScheduledTask> getRunningTasks();

    /**
     * 清理过期的任务日志
     *
     * @param days 保留天数
     * @return 清理数量
     */
    int cleanOldLogs(int days);
}
