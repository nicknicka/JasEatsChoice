package com.xx.jaseatschoicejava.scheduler;

import com.xx.jaseatschoicejava.service.WishListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 想吃列表超时处理定时任务
 *
 * @author Claude
 * @since 2025-02-12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WishListTimeoutScheduler {

    private final WishListService wishListService;

    /**
     * 每10分钟执行一次超时检查
     * 检查超过24小时未审核的想吃列表项，自动通过
     */
    @Scheduled(fixedRate = 600000) // 10分钟 = 600000毫秒
    public void checkTimeoutItems() {
        try {
            log.debug("开始检查想吃列表超时请求...");

            int count = wishListService.autoAuditTimeoutItems();

            if (count > 0) {
                log.info("想吃列表超时检查完成，自动通过了{}条超时记录", count);
            } else {
                log.debug("没有超时的想吃列表项");
            }

        } catch (Exception e) {
            log.error("检查想吃列表超时请求失败", e);
        }
    }
}
