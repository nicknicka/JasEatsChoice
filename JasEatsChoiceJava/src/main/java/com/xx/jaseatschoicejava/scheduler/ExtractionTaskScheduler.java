package com.xx.jaseatschoicejava.scheduler;

import com.xx.jaseatschoicejava.service.ContentExtractionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 内容提取任务调度器
 * 负责定期处理待提取的任务
 *
 * @author Claude
 * @since 2025-02-01
 */
@Component
public class ExtractionTaskScheduler {

    private static final Logger log = LoggerFactory.getLogger(ExtractionTaskScheduler.class);

    @Autowired
    private ContentExtractionService contentExtractionService;

    /**
     * 定时处理待提取任务
     * 每5秒执行一次，处理所有待处理和正在处理的任务
     *
     * cron表达式: "0/5 * * * *" 表示每5秒执行一次
     * 格式：秒 分 时 日 月 周 年（6个字段）
     */
    @Scheduled(cron = "0/5 * * * * *")
    public void processPendingExtractionTasks() {
        try {
            log.debug("开始处理待提取任务...");

            int processedCount = contentExtractionService.processPendingTasks();

            if (processedCount > 0) {
                log.info("成功处理 {} 个提取任务", processedCount);
            } else {
                log.debug("没有待处理的提取任务");
            }

        } catch (Exception e) {
            log.error("处理提取任务失败", e);
        }
    }

    /**
     * 清理已完成的旧任务（可选）
     * 每天凌晨3点执行一次，清理7天前已完成或失败的任务
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanOldTasks() {
        try {
            log.info("开始清理旧的提取任务...");
            // TODO: 实现清理逻辑，保留最近7天的任务记录
            log.debug("清理旧任务完成");
        } catch (Exception e) {
            log.error("清理旧任务失败", e);
        }
    }
}
