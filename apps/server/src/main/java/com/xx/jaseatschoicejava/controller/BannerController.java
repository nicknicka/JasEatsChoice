package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Banner;
import com.xx.jaseatschoicejava.service.BannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 轮播图控制器
 */
@RestController
@RequestMapping("/v1/banners")
public class BannerController {

    private static final Logger logger = LoggerFactory.getLogger(BannerController.class);

    @Autowired
    private BannerService bannerService;

    /**
     * 获取轮播图列表
     * @param position 位置(home/merchant/dish等)
     * @param status 状态(1-启用,0-禁用)
     */
    @GetMapping
    public ResponseResult<?> getBanners(@RequestParam(required = false) String position,
                                       @RequestParam(required = false) Integer status) {
        try {
            LambdaQueryWrapper<Banner> queryWrapper = new LambdaQueryWrapper<>();

            // 按位置筛选
            if (position != null && !position.isEmpty()) {
                queryWrapper.eq(Banner::getPosition, position);
            }

            // 按状态筛选（默认只返回启用的）
            if (status == null) {
                status = 1; // 默认只返回启用的轮播图
            }
            queryWrapper.eq(Banner::getStatus, status);

            // 按排序字段排序
            queryWrapper.orderByAsc(Banner::getSortOrder);

            List<Banner> banners = bannerService.list(queryWrapper);

            // 转换为前端需要的格式
            List<Map<String, Object>> result = banners.stream()
                .map(banner -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("bannerId", banner.getId());
                    map.put("id", banner.getId());
                    map.put("title", banner.getTitle());
                    map.put("imageUrl", banner.getImageUrl());
                    map.put("image", banner.getImageUrl());
                    map.put("type", banner.getType());
                    map.put("targetType", banner.getTargetType());
                    map.put("targetId", banner.getTargetId());
                    map.put("link", banner.getLink());
                    map.put("position", banner.getPosition());
                    map.put("sortOrder", banner.getSortOrder());
                    return map;
                })
                .collect(Collectors.toList());

            return ResponseResult.success(result);
        } catch (Exception e) {
            logger.error("获取轮播图列表失败: {}", e.getMessage(), e);
            return ResponseResult.fail("500", "获取轮播图列表失败");
        }
    }

    /**
     * 获取轮播图详情
     */
    @GetMapping("/{bannerId}")
    public ResponseResult<?> getBannerDetail(@PathVariable String bannerId) {
        try {
            Banner banner = bannerService.getById(bannerId);
            if (banner != null) {
                return ResponseResult.success(banner);
            }
            return ResponseResult.fail("404", "轮播图不存在");
        } catch (Exception e) {
            logger.error("获取轮播图详情失败: {}", e.getMessage(), e);
            return ResponseResult.fail("500", "获取轮播图详情失败");
        }
    }

    /**
     * 创建轮播图（管理员功能）
     */
    @PostMapping
    public ResponseResult<?> createBanner(@RequestBody Banner banner) {
        try {
            banner.setCreateTime(LocalDateTime.now());
            banner.setUpdateTime(LocalDateTime.now());
            boolean success = bannerService.save(banner);
            if (success) {
                return ResponseResult.success(banner, "创建成功");
            }
            return ResponseResult.fail("500", "创建失败");
        } catch (Exception e) {
            logger.error("创建轮播图失败: {}", e.getMessage(), e);
            return ResponseResult.fail("500", "创建失败");
        }
    }

    /**
     * 更新轮播图（管理员功能）
     */
    @PutMapping("/{bannerId}")
    public ResponseResult<?> updateBanner(@PathVariable String bannerId, @RequestBody Banner banner) {
        try {
            banner.setId(bannerId);
            banner.setUpdateTime(LocalDateTime.now());
            boolean success = bannerService.updateById(banner);
            if (success) {
                return ResponseResult.success("更新成功");
            }
            return ResponseResult.fail("500", "更新失败");
        } catch (Exception e) {
            logger.error("更新轮播图失败: {}", e.getMessage(), e);
            return ResponseResult.fail("500", "更新失败");
        }
    }

    /**
     * 删除轮播图（管理员功能）
     */
    @DeleteMapping("/{bannerId}")
    public ResponseResult<?> deleteBanner(@PathVariable String bannerId) {
        try {
            boolean success = bannerService.removeById(bannerId);
            if (success) {
                return ResponseResult.success("删除成功");
            }
            return ResponseResult.fail("500", "删除失败");
        } catch (Exception e) {
            logger.error("删除轮播图失败: {}", e.getMessage(), e);
            return ResponseResult.fail("500", "删除失败");
        }
    }

    /**
     * 更新轮播图状态（管理员功能）
     */
    @PutMapping("/{bannerId}/status")
    public ResponseResult<?> updateBannerStatus(@PathVariable String bannerId, @RequestBody Map<String, Integer> request) {
        try {
            Integer status = request.get("status");
            Banner banner = bannerService.getById(bannerId);
            if (banner != null) {
                banner.setStatus(status);
                banner.setUpdateTime(LocalDateTime.now());
                boolean success = bannerService.updateById(banner);
                if (success) {
                    return ResponseResult.success("状态更新成功");
                }
            }
            return ResponseResult.fail("500", "状态更新失败");
        } catch (Exception e) {
            logger.error("更新轮播图状态失败: {}", e.getMessage(), e);
            return ResponseResult.fail("500", "状态更新失败");
        }
    }
}
