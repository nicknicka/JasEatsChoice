package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.HotTopic;
import com.xx.jaseatschoicejava.service.HotTopicService;
import com.xx.jaseatschoicejava.service.SystemLogService;
import com.xx.jaseatschoicejava.util.AdminContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 热点管理后台控制器
 */
@RestController
@RequestMapping("/v1/admin/hot-topic")
@Api(tags = "热点管理")
@PreAuthorize("hasRole('ADMIN')")
public class HotTopicAdminController {

    private static final Logger log = LoggerFactory.getLogger(HotTopicAdminController.class);

    @Autowired
    private HotTopicService hotTopicService;

    @Autowired(required = false)
    private SystemLogService systemLogService;

    /**
     * 分页查询热点列表
     */
    @ApiOperation("分页查询热点列表")
    @GetMapping("")
    public ResponseEntity<IPage<HotTopic>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {

        try {
            Page<HotTopic> pageParam = new Page<>(page, size);

            // 构建查询条件
            QueryWrapper<HotTopic> queryWrapper = new QueryWrapper<>();

            // 状态筛选
            if (status != null && !status.isEmpty()) {
                queryWrapper.eq("status", status);
            }

            // 按优先级降序、创建时间倒序
            queryWrapper.orderByDesc("priority")
                .orderByDesc("create_time");

            IPage<HotTopic> result = hotTopicService.page(pageParam, queryWrapper);

            log.info("查询热点列表成功，页码: {}, 大小: {}, 总数: {}", page, size, result.getTotal());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("查询热点列表失败", e);
            return ResponseEntity.status(500).body(new Page<>());
        }
    }

    /**
     * 获取热点详情
     */
    @ApiOperation("获取热点详情")
    @GetMapping("/detail/{id}")
    public ResponseEntity<Map<String, Object>> detail(@PathVariable String id) {
        try {
            HotTopic hotTopic = hotTopicService.getById(id);
            if (hotTopic == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "热点不存在");
                return ResponseEntity.status(404).body(response);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", hotTopic);

            log.info("查询热点详情成功: {}", id);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("查询热点详情失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 创建热点
     */
    @ApiOperation("创建热点")
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(@RequestBody HotTopic hotTopic) {
        try {
            // 设置默认值
            if (hotTopic.getPriority() == null) {
                hotTopic.setPriority(0);
            }
            if (hotTopic.getStatus() == null) {
                hotTopic.setStatus(HotTopic.Status.ACTIVE.getCode());
            }
            if (hotTopic.getClickCount() == null) {
                hotTopic.setClickCount(0);
            }
            if (hotTopic.getShareCount() == null) {
                hotTopic.setShareCount(0);
            }
            if (hotTopic.getRequireReview() == null) {
                hotTopic.setRequireReview(false);
            }
            if (hotTopic.getReviewStatus() == null) {
                hotTopic.setReviewStatus(HotTopic.ReviewStatus.APPROVED.getCode());
            }

            // 设置创建时间和开始时间
            if (hotTopic.getCreateTime() == null) {
                hotTopic.setCreateTime(LocalDateTime.now());
            }
            // 如果没有设置开始时间，默认为当前时间
            if (hotTopic.getStartDate() == null) {
                hotTopic.setStartDate(LocalDateTime.now());
            }
            // 结束时间可以为空，表示永久生效

            boolean success = hotTopicService.createHotTopic(hotTopic);

            if (success) {
                log.info("创建热点成功: {}", hotTopic.getContent());

                // 记录操作日志
                if (systemLogService != null) {
                    Long adminId = AdminContext.getAdminId();
                    String adminName = AdminContext.getAdminUsername();

                    systemLogService.logOperation(
                        "CREATE", "HOT_TOPIC", "创建热点：" + hotTopic.getContent(),
                        adminId, adminName, "ADMIN",
                        "HotTopicAdminController.create",
                        "content=" + hotTopic.getContent(),
                        null, 0L, null, "SUCCESS"
                    );
                }

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "创建成功");
                response.put("data", hotTopic);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "创建失败");
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            log.error("创建热点失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 更新热点
     */
    @ApiOperation("更新热点")
    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable String id,
            @RequestBody HotTopic hotTopic) {

        try {
            hotTopic.setId(id);
            boolean success = hotTopicService.updateHotTopic(hotTopic);

            if (success) {
                log.info("更新热点成功: {}", id);

                // 记录操作日志
                if (systemLogService != null) {
                    Long adminId = AdminContext.getAdminId();
                    String adminName = AdminContext.getAdminUsername();

                    systemLogService.logOperation(
                        "UPDATE", "HOT_TOPIC", "更新热点：" + id,
                        adminId, adminName, "ADMIN",
                        "HotTopicAdminController.update",
                        "id=" + id,
                        null, 0L, null, "SUCCESS"
                    );
                }

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "更新成功");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "更新失败");
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            log.error("更新热点失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 删除热点
     */
    @ApiOperation("删除热点")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String id) {
        try {
            boolean success = hotTopicService.deleteHotTopic(id);

            if (success) {
                log.info("删除热点成功: {}", id);

                // 记录操作日志
                if (systemLogService != null) {
                    Long adminId = AdminContext.getAdminId();
                    String adminName = AdminContext.getAdminUsername();

                    systemLogService.logOperation(
                        "DELETE", "HOT_TOPIC", "删除热点：" + id,
                        adminId, adminName, "ADMIN",
                        "HotTopicAdminController.delete",
                        "id=" + id,
                        null, 0L, null, "SUCCESS"
                    );
                }

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "删除成功");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "热点不存在");
                return ResponseEntity.status(404).body(response);
            }

        } catch (Exception e) {
            log.error("删除热点失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 审核热点
     */
    @ApiOperation("审核热点")
    @PostMapping("/review/{id}")
    public ResponseEntity<Map<String, Object>> review(
            @PathVariable String id,
            @RequestParam Long reviewerId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String comment) {

        try {
            boolean success = hotTopicService.reviewHotTopic(id, reviewerId, approved, comment);

            if (success) {
                log.info("审核热点成功: {}, 通过: {}", id, approved);

                // 记录操作日志
                if (systemLogService != null) {
                    String adminName = AdminContext.getAdminUsername();

                    systemLogService.logOperation(
                        "REVIEW", "HOT_TOPIC", "审核热点：" + id + (approved ? "通过" : "拒绝"),
                        reviewerId, adminName, "ADMIN",
                        "HotTopicAdminController.review",
                        "id=" + id + ", approved=" + approved + ", comment=" + comment,
                        null, 0L, null, "SUCCESS"
                    );
                }

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", approved ? "审核通过" : "已拒绝");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "热点不存在");
                return ResponseEntity.status(404).body(response);
            }

        } catch (Exception e) {
            log.error("审核热点失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "审核失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 批量删除热点
     */
    @ApiOperation("批量删除热点")
    @DeleteMapping("/batch-delete")
    public ResponseEntity<Map<String, Object>> batchDelete(@RequestBody List<String> ids) {
        try {
            int successCount = 0;
            for (String id : ids) {
                if (hotTopicService.deleteHotTopic(id)) {
                    successCount++;
                }
            }

            log.info("批量删除热点完成，成功: {}/{}", successCount, ids.size());

            // 记录操作日志
            if (systemLogService != null && successCount > 0) {
                Long adminId = AdminContext.getAdminId();
                String adminName = AdminContext.getAdminUsername();

                systemLogService.logOperation(
                    "DELETE", "HOT_TOPIC", "批量删除热点：" + successCount + "条",
                    adminId, adminName, "ADMIN",
                    "HotTopicAdminController.batchDelete",
                    "count=" + ids.size(),
                    null, 0L, null, "SUCCESS"
                );
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "成功删除 " + successCount + " 条");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("批量删除热点失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "批量删除失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取统计数据
     */
    @ApiOperation("获取热点统计数据")
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> statistics() {
        try {
            List<HotTopic> allTopics = hotTopicService.list();

            Map<String, Object> stats = new HashMap<>();
            stats.put("total", allTopics.size());
            stats.put("active", allTopics.stream().filter(t -> HotTopic.Status.ACTIVE.getCode().equals(t.getStatus())).count());
            stats.put("pending", allTopics.stream().filter(t -> HotTopic.ReviewStatus.PENDING.getCode().equals(t.getReviewStatus())).count());
            stats.put("totalClicks", allTopics.stream().mapToInt(HotTopic::getClickCount).sum());
            stats.put("totalShares", allTopics.stream().mapToInt(HotTopic::getShareCount).sum());

            log.info("查询热点统计成功");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("查询热点统计失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询统计失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
