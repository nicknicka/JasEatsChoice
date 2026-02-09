package com.xx.jaseatschoicejava.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xx.jaseatschoicejava.entity.Announcement;
import com.xx.jaseatschoicejava.service.AnnouncementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告定时任务
 * 负责自动处理过期公告
 */
@Component
public class AnnouncementScheduler {

    private static final Logger log = LoggerFactory.getLogger(AnnouncementScheduler.class);

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 自动下线过期公告
     * 每小时执行一次（0分0秒）
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void autoInactiveExpiredAnnouncements() {
        try {
            LocalDateTime now = LocalDateTime.now();

            // 查询所有需要下线的公告
            // 条件：状态为active 且 结束时间不为空 且 结束时间已过
            QueryWrapper<Announcement> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", "active")
                    .isNotNull("end_time")
                    .le("end_time", now);

            List<Announcement> expiredAnnouncements = announcementService.list(queryWrapper);

            if (!expiredAnnouncements.isEmpty()) {
                log.info("发现 {} 个过期公告需要自动下线", expiredAnnouncements.size());

                // 批量更新状态为inactive
                for (Announcement announcement : expiredAnnouncements) {
                    announcement.setStatus("inactive");
                }

                boolean success = announcementService.updateBatchById(expiredAnnouncements);

                if (success) {
                    log.info("成功下线 {} 个过期公告", expiredAnnouncements.size());
                } else {
                    log.error("批量下线过期公告失败");
                }
            } else {
                log.debug("没有需要下线的过期公告");
            }
        } catch (Exception e) {
            log.error("自动下线过期公告任务执行失败", e);
        }
    }

    /**
     * 自动上线到期的公告
     * 每小时执行一次（0分5秒）
     */
    @Scheduled(cron = "0 5 * * * ?")
    public void autoActivePendingAnnouncements() {
        try {
            LocalDateTime now = LocalDateTime.now();

            // 查询所有需要上线的公告
            // 条件：状态为active 且 开始时间不为空 且 开始时间已到
            QueryWrapper<Announcement> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", "active")
                    .isNotNull("start_time")
                    .le("start_time", now);

            List<Announcement> readyAnnouncements = announcementService.list(queryWrapper);

            if (!readyAnnouncements.isEmpty()) {
                log.info("发现 {} 个已到开始时间的公告", readyAnnouncements.size());

                // 这些公告已经是active状态，所以只需要记录日志
                // 实际上线逻辑在发布时就已经处理了
                log.debug("公告已自动生效，共 {} 个", readyAnnouncements.size());
            }
        } catch (Exception e) {
            log.error("自动上线公告任务执行失败", e);
        }
    }
}
