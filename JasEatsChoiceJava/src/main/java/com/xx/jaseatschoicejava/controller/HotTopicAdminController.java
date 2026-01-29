package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.HotTopic;
import com.xx.jaseatschoicejava.service.HotTopicService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 热点管理后台控制器
 */
@RestController
@RequestMapping("/v1/admin/hot-topic")
@Api(tags = "热点管理")
public class HotTopicAdminController {

    private static final Logger log = LoggerFactory.getLogger(HotTopicAdminController.class);

    @Autowired
    private HotTopicService hotTopicService;

    /**
     * 分页查询热点列表
     *
     * @param page  页码
     * @param size  每页大小
     * @param status 状态筛选（可选）
     * @return 分页结果
     */
    @GetMapping("/list")
    @ApiOperation("分页查询热点列表")
    public ResponseResult<Page<HotTopic>> list(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") int size,
            @ApiParam("状态筛选") @RequestParam(required = false) String status) {

        try {
            Page<HotTopic> pageParam = new Page<>(page, size);

            // TODO: 根据status筛选逻辑，这里暂时返回所有数据
            Page<HotTopic> result = hotTopicService.page(pageParam);

            log.info("查询热点列表成功，页码: {}, 大小: {}, 总数: {}", page, size, result.getTotal());
            return ResponseResult.success(result);

        } catch (Exception e) {
            log.error("查询热点列表失败", e);
            return ResponseResult.fail("500", "查询失败");
        }
    }

    /**
     * 获取热点详情
     *
     * @param id 热点ID
     * @return 热点详情
     */
    @GetMapping("/detail/{id}")
    @ApiOperation("获取热点详情")
    public ResponseResult<HotTopic> detail(@ApiParam("热点ID") @PathVariable String id) {
        try {
            HotTopic hotTopic = hotTopicService.getById(id);
            if (hotTopic == null) {
                return ResponseResult.fail("404", "热点不存在");
            }

            log.info("查询热点详情成功: {}", id);
            return ResponseResult.success(hotTopic);

        } catch (Exception e) {
            log.error("查询热点详情失败", e);
            return ResponseResult.fail("500", "查询失败");
        }
    }

    /**
     * 创建热点
     *
     * @param hotTopic 热点实体
     * @return 操作结果
     */
    @PostMapping("/create")
    @ApiOperation("创建热点")
    public ResponseResult<HotTopic> create(@RequestBody HotTopic hotTopic) {
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

            boolean success = hotTopicService.createHotTopic(hotTopic);
            if (success) {
                log.info("创建热点成功: {}", hotTopic.getContent());
                return ResponseResult.success(hotTopic, "创建成功");
            } else {
                return ResponseResult.fail("500", "创建失败");
            }

        } catch (Exception e) {
            log.error("创建热点失败", e);
            return ResponseResult.fail("500", "创建失败");
        }
    }

    /**
     * 更新热点
     *
     * @param id       热点ID
     * @param hotTopic 热点实体
     * @return 操作结果
     */
    @PutMapping("/update/{id}")
    @ApiOperation("更新热点")
    public ResponseResult<Void> update(
            @ApiParam("热点ID") @PathVariable String id,
            @RequestBody HotTopic hotTopic) {

        try {
            hotTopic.setId(id);
            boolean success = hotTopicService.updateHotTopic(hotTopic);

            if (success) {
                log.info("更新热点成功: {}", id);
                return ResponseResult.success(null, "更新成功");
            } else {
                return ResponseResult.fail("500", "更新失败");
            }

        } catch (Exception e) {
            log.error("更新热点失败", e);
            return ResponseResult.fail("500", "更新失败");
        }
    }

    /**
     * 删除热点
     *
     * @param id 热点ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除热点")
    public ResponseResult<Void> delete(@ApiParam("热点ID") @PathVariable String id) {
        try {
            boolean success = hotTopicService.deleteHotTopic(id);

            if (success) {
                log.info("删除热点成功: {}", id);
                return ResponseResult.success(null, "删除成功");
            } else {
                return ResponseResult.fail("404", "热点不存在");
            }

        } catch (Exception e) {
            log.error("删除热点失败", e);
            return ResponseResult.fail("500", "删除失败");
        }
    }

    /**
     * 审核热点
     *
     * @param id         热点ID
     * @param reviewerId 审核人ID
     * @param approved   是否通过
     * @param comment    审核意见
     * @return 操作结果
     */
    @PostMapping("/review/{id}")
    @ApiOperation("审核热点")
    public ResponseResult<Void> review(
            @ApiParam("热点ID") @PathVariable String id,
            @ApiParam("审核人ID") @RequestParam Long reviewerId,
            @ApiParam("是否通过") @RequestParam boolean approved,
            @ApiParam("审核意见") @RequestParam(required = false) String comment) {

        try {
            boolean success = hotTopicService.reviewHotTopic(id, reviewerId, approved, comment);

            if (success) {
                log.info("审核热点成功: {}, 通过: {}", id, approved);
                return ResponseResult.success(null, "审核成功");
            } else {
                return ResponseResult.fail("404", "热点不存在");
            }

        } catch (Exception e) {
            log.error("审核热点失败", e);
            return ResponseResult.fail("500", "审核失败");
        }
    }

    /**
     * 批量删除热点
     *
     * @param ids 热点ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch-delete")
    @ApiOperation("批量删除热点")
    public ResponseResult<Void> batchDelete(@RequestBody List<String> ids) {
        try {
            int successCount = 0;
            for (String id : ids) {
                if (hotTopicService.deleteHotTopic(id)) {
                    successCount++;
                }
            }

            log.info("批量删除热点完成，成功: {}/{}", successCount, ids.size());
            return ResponseResult.success(null, "成功删除 " + successCount + " 条");

        } catch (Exception e) {
            log.error("批量删除热点失败", e);
            return ResponseResult.fail("500", "批量删除失败");
        }
    }

    /**
     * 获取统计数据
     *
     * @return 统计数据
     */
    @GetMapping("/statistics")
    @ApiOperation("获取热点统计数据")
    public ResponseResult<Map<String, Object>> statistics() {
        try {
            List<HotTopic> allTopics = hotTopicService.list();

            Map<String, Object> stats = new HashMap<>();
            stats.put("total", allTopics.size());
            stats.put("active", allTopics.stream().filter(t -> HotTopic.Status.ACTIVE.getCode().equals(t.getStatus())).count());
            stats.put("pending", allTopics.stream().filter(t -> HotTopic.ReviewStatus.PENDING.getCode().equals(t.getReviewStatus())).count());
            stats.put("totalClicks", allTopics.stream().mapToInt(HotTopic::getClickCount).sum());
            stats.put("totalShares", allTopics.stream().mapToInt(HotTopic::getShareCount).sum());

            log.info("查询热点统计成功");
            return ResponseResult.success(stats);

        } catch (Exception e) {
            log.error("查询热点统计失败", e);
            return ResponseResult.fail("500", "查询统计失败");
        }
    }
}
