package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.Announcement;
import com.xx.jaseatschoicejava.service.AnnouncementService;
import com.xx.jaseatschoicejava.service.SystemLogService;
import com.xx.jaseatschoicejava.util.AdminContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员-公告管理控制器
 */
@Api(tags = "管理员-公告管理")
@RestController
@RequestMapping("/admin/announcements")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnnouncementController {

    private static final Logger log = LoggerFactory.getLogger(AdminAnnouncementController.class);

    @Autowired
    private AnnouncementService announcementService;

    @Autowired(required = false)
    private SystemLogService systemLogService;

    /**
     * 获取公告列表（分页）
     */
    @ApiOperation("获取公告列表")
    @GetMapping("")
    public ResponseEntity<IPage<Announcement>> getAnnouncementList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {

        Page<Announcement> pageParam = new Page<>(page, pageSize);

        // 构建查询条件
        QueryWrapper<Announcement> queryWrapper = new QueryWrapper<>();

        // 只查询系统级公告（merchantId为null或"SYSTEM"）
        queryWrapper.and(wrapper -> wrapper
            .isNull("merchant_id")
            .or()
            .eq("merchant_id", "SYSTEM")
        );

        // 状态筛选
        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }

        // 关键词搜索（标题、内容）
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                .like("title", keyword)
                .or()
                .like("content", keyword)
            );
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc("create_time");

        IPage<Announcement> result = announcementService.page(pageParam, queryWrapper);

        return ResponseEntity.ok(result);
    }

    /**
     * 获取公告详情
     */
    @ApiOperation("获取公告详情")
    @GetMapping("/{announcementId}")
    public ResponseEntity<Map<String, Object>> getAnnouncementDetail(@PathVariable String announcementId) {
        Announcement announcement = announcementService.getById(announcementId);

        Map<String, Object> response = new HashMap<>();
        if (announcement != null) {
            response.put("success", true);
            response.put("data", announcement);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "公告不存在");
            return ResponseEntity.status(404).body(response);
        }
    }

    /**
     * 创建公告
     */
    @ApiOperation("创建公告")
    @PostMapping("")
    public ResponseEntity<Map<String, Object>> createAnnouncement(@RequestBody Map<String, Object> request) {
        String title = (String) request.get("title");
        String content = (String) request.get("content");
        String startTimeStr = (String) request.get("startTime");
        String endTimeStr = (String) request.get("endTime");

        if (!StringUtils.hasText(title)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "公告标题不能为空");
            return ResponseEntity.status(400).body(response);
        }

        if (!StringUtils.hasText(content)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "公告内容不能为空");
            return ResponseEntity.status(400).body(response);
        }

        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setStatus("active");
        announcement.setMerchantId("SYSTEM"); // 标记为系统级公告

        // 设置开始和结束时间
        if (StringUtils.hasText(startTimeStr)) {
            try {
                announcement.setStartTime(LocalDateTime.parse(startTimeStr));
            } catch (Exception e) {
                log.warn("开始时间格式错误: {}", startTimeStr);
            }
        }
        if (StringUtils.hasText(endTimeStr)) {
            try {
                announcement.setEndTime(LocalDateTime.parse(endTimeStr));
            } catch (Exception e) {
                log.warn("结束时间格式错误: {}", endTimeStr);
            }
        }

        boolean success = announcementService.save(announcement);

        // 记录操作日志
        if (success && systemLogService != null) {
            Long adminId = AdminContext.getAdminId();
            String adminName = AdminContext.getAdminUsername();

            systemLogService.logOperation(
                "CREATE", "ANNOUNCEMENT", "创建系统公告：" + title,
                adminId, adminName, "ADMIN",
                "AdminAnnouncementController.createAnnouncement",
                "title=" + title,
                null, 0L, null, "SUCCESS"
            );
        }

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "公告创建成功");
            response.put("data", announcement);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "公告创建失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 更新公告
     */
    @ApiOperation("更新公告")
    @PutMapping("/{announcementId}")
    public ResponseEntity<Map<String, Object>> updateAnnouncement(
            @PathVariable String announcementId,
            @RequestBody Map<String, Object> request) {

        Announcement announcement = announcementService.getById(announcementId);
        if (announcement == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "公告不存在");
            return ResponseEntity.status(404).body(response);
        }

        // 检查是否为系统级公告
        if (!"SYSTEM".equals(announcement.getMerchantId()) && announcement.getMerchantId() != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "只能修改系统级公告");
            return ResponseEntity.status(403).body(response);
        }

        String title = (String) request.get("title");
        String content = (String) request.get("content");
        String status = (String) request.get("status");
        String startTimeStr = (String) request.get("startTime");
        String endTimeStr = (String) request.get("endTime");

        if (StringUtils.hasText(title)) announcement.setTitle(title);
        if (StringUtils.hasText(content)) announcement.setContent(content);
        if (StringUtils.hasText(status)) announcement.setStatus(status);

        if (StringUtils.hasText(startTimeStr)) {
            try {
                announcement.setStartTime(LocalDateTime.parse(startTimeStr));
            } catch (Exception e) {
                log.warn("开始时间格式错误: {}", startTimeStr);
            }
        }
        if (StringUtils.hasText(endTimeStr)) {
            try {
                announcement.setEndTime(LocalDateTime.parse(endTimeStr));
            } catch (Exception e) {
                log.warn("结束时间格式错误: {}", endTimeStr);
            }
        }

        boolean success = announcementService.updateById(announcement);

        // 记录操作日志
        if (success && systemLogService != null) {
            Long adminId = AdminContext.getAdminId();
            String adminName = AdminContext.getAdminUsername();

            systemLogService.logOperation(
                "UPDATE", "ANNOUNCEMENT", "更新系统公告：" + title,
                adminId, adminName, "ADMIN",
                "AdminAnnouncementController.updateAnnouncement",
                "announcementId=" + announcementId,
                null, 0L, null, "SUCCESS"
            );
        }

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "公告更新成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "公告更新失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 删除公告
     */
    @ApiOperation("删除公告")
    @DeleteMapping("/{announcementId}")
    public ResponseEntity<Map<String, Object>> deleteAnnouncement(@PathVariable String announcementId) {
        Announcement announcement = announcementService.getById(announcementId);
        if (announcement == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "公告不存在");
            return ResponseEntity.status(404).body(response);
        }

        // 检查是否为系统级公告
        if (!"SYSTEM".equals(announcement.getMerchantId()) && announcement.getMerchantId() != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "只能删除系统级公告");
            return ResponseEntity.status(403).body(response);
        }

        boolean success = announcementService.removeById(announcementId);

        // 记录操作日志
        if (success && systemLogService != null) {
            Long adminId = AdminContext.getAdminId();
            String adminName = AdminContext.getAdminUsername();

            systemLogService.logOperation(
                "DELETE", "ANNOUNCEMENT", "删除系统公告：" + announcement.getTitle(),
                adminId, adminName, "ADMIN",
                "AdminAnnouncementController.deleteAnnouncement",
                "announcementId=" + announcementId,
                null, 0L, null, "SUCCESS"
            );
        }

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "公告删除成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "公告删除失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 批量删除公告
     */
    @ApiOperation("批量删除公告")
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchDeleteAnnouncements(@RequestBody java.util.List<String> announcementIds) {
        if (announcementIds == null || announcementIds.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "公告ID列表不能为空");
            return ResponseEntity.status(400).body(response);
        }

        int successCount = 0;
        int failCount = 0;

        for (String announcementId : announcementIds) {
            Announcement announcement = announcementService.getById(announcementId);
            if (announcement != null && ("SYSTEM".equals(announcement.getMerchantId()) || announcement.getMerchantId() == null)) {
                if (announcementService.removeById(announcementId)) {
                    successCount++;
                } else {
                    failCount++;
                }
            } else {
                failCount++;
            }
        }

        // 记录操作日志
        if (systemLogService != null && successCount > 0) {
            Long adminId = AdminContext.getAdminId();
            String adminName = AdminContext.getAdminUsername();

            systemLogService.logOperation(
                "DELETE", "ANNOUNCEMENT", "批量删除系统公告：" + successCount + "个成功，" + failCount + "个失败",
                adminId, adminName, "ADMIN",
                "AdminAnnouncementController.batchDeleteAnnouncements",
                "totalCount=" + announcementIds.size(),
                null, 0L, null, failCount == 0 ? "SUCCESS" : "PARTIAL"
            );
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "批量删除完成：成功" + successCount + "个，失败" + failCount + "个");
        response.put("successCount", successCount);
        response.put("failCount", failCount);
        return ResponseEntity.ok(response);
    }

    /**
     * 发布/下线公告（修改状态）
     */
    @ApiOperation("发布/下线公告")
    @PutMapping("/{announcementId}/status")
    public ResponseEntity<Map<String, Object>> updateAnnouncementStatus(
            @PathVariable String announcementId,
            @RequestBody Map<String, String> request) {

        Announcement announcement = announcementService.getById(announcementId);
        if (announcement == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "公告不存在");
            return ResponseEntity.status(404).body(response);
        }

        // 检查是否为系统级公告
        if (!"SYSTEM".equals(announcement.getMerchantId()) && announcement.getMerchantId() != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "只能修改系统级公告");
            return ResponseEntity.status(403).body(response);
        }

        String status = request.get("status"); // active 或 inactive
        announcement.setStatus(status);

        boolean success = announcementService.updateById(announcement);

        // 记录操作日志
        if (success && systemLogService != null) {
            Long adminId = AdminContext.getAdminId();
            String adminName = AdminContext.getAdminUsername();

            String operation = "active".equals(status) ? "发布" : "下线";
            systemLogService.logOperation(
                "UPDATE", "ANNOUNCEMENT", operation + "系统公告：" + announcement.getTitle(),
                adminId, adminName, "ADMIN",
                "AdminAnnouncementController.updateAnnouncementStatus",
                "announcementId=" + announcementId + ", status=" + status,
                null, 0L, null, "SUCCESS"
            );
        }

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "active".equals(status) ? "公告已发布" : "公告已下线");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "状态修改失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取公告统计
     */
    @ApiOperation("获取公告统计")
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getAnnouncementStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 总公告数（系统级）
        long totalCount = announcementService.count(
            new QueryWrapper<Announcement>()
                .and(wrapper -> wrapper
                    .isNull("merchant_id")
                    .or()
                    .eq("merchant_id", "SYSTEM")
                )
        );

        // 启用中的公告数
        long activeCount = announcementService.count(
            new QueryWrapper<Announcement>()
                .and(wrapper -> wrapper
                    .isNull("merchant_id")
                    .or()
                    .eq("merchant_id", "SYSTEM")
                )
                .eq("status", "active")
        );

        // 今日新增公告数
        LocalDateTime todayStart = java.time.LocalDate.now().atStartOfDay();
        long todayNewCount = announcementService.count(
            new QueryWrapper<Announcement>()
                .and(wrapper -> wrapper
                    .isNull("merchant_id")
                    .or()
                    .eq("merchant_id", "SYSTEM")
                )
                .ge("create_time", todayStart)
        );

        stats.put("totalCount", totalCount);
        stats.put("activeCount", activeCount);
        stats.put("inactiveCount", totalCount - activeCount);
        stats.put("todayNewCount", todayNewCount);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", stats);
        return ResponseEntity.ok(response);
    }
}
