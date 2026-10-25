package com.xx.jaseatschoicejava.scheduler;

import com.xx.jaseatschoicejava.service.AddDishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 加菜超时处理定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AddDishTimeoutScheduler {

    private final AddDishService addDishService;

    /**
     * 每2分钟执行一次超时检查
     */
    @Scheduled(fixedRate = 120000) // 2分钟
    public void checkTimeoutRequests() {
        try {
            log.debug("开始检查加菜超时请求...");
            addDishService.handleTimeoutRequests();
        } catch (Exception e) {
            log.error("检查加菜超时请求失败", e);
        }
    }

    /**
     * 每2分钟执行一次提醒发送
     */
    @Scheduled(fixedRate = 120000) // 2分钟
    public void sendReminders() {
        try {
            log.debug("开始发送加菜提醒...");
            addDishService.sendReminders();
        } catch (Exception e) {
            log.error("发送加菜提醒失败", e);
        }
    }
}
