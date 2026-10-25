package com.xx.jaseatschoicejava.scheduler;

import com.xx.jaseatschoicejava.service.GroupOrderAdditionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 群订单加菜超时处理定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GroupOrderAdditionScheduler {

    private final GroupOrderAdditionService groupOrderAdditionService;

    /**
     * 每2分钟执行一次超时检查
     * 检查超过15分钟未审核的加菜请求，自动驳回
     */
    @Scheduled(fixedRate = 120000) // 2分钟
    public void checkTimeoutAdditions() {
        try {
            log.debug("开始检查群订单加菜超时请求...");
            int count = groupOrderAdditionService.checkTimeoutAdditions();
            if (count > 0) {
                log.info("群订单加菜超时检查完成，处理了{}条超时记录", count);
            }
        } catch (Exception e) {
            log.error("检查群订单加菜超时请求失败", e);
        }
    }
}
