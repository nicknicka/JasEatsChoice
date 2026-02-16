package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.OrderDish;
import com.xx.jaseatschoicejava.entity.Review;
import com.xx.jaseatschoicejava.entity.ReviewReply;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.enums.NotificationTypeEnum;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.OrderDishService;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.service.ReviewReplyService;
import com.xx.jaseatschoicejava.service.ReviewService;
import com.xx.jaseatschoicejava.service.UserService;
import com.xx.jaseatschoicejava.util.NotificationUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评价管理控制器（用户端 + 商家端）
 */
@Slf4j
@Api(tags = "评价管理")
@RestController
@RequestMapping("/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewReplyService reviewReplyService;
    private final OrderService orderService;
    private final OrderDishService orderDishService;
    private final UserService userService;
    private final DishService dishService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== 用户端API ====================

    /**
     * 用户提交评价（使用事务确保评价记录和订单状态更新的原子性）
     */
    @ApiOperation("用户提交评价")
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult<?> submitReview(@RequestBody Map<String, Object> request) {
        try {
            String orderId = (String) request.get("orderId");
            String merchantId = (String) request.get("merchantId");
            Integer rating = (Integer) request.get("rating");
            String content = (String) request.get("content");
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) request.get("tags");
            @SuppressWarnings("unchecked")
            List<String> images = (List<String>) request.get("images");

            if (orderId == null || orderId.trim().isEmpty()) {
                return ResponseResult.fail("400", "订单ID不能为空");
            }
            if (merchantId == null || merchantId.trim().isEmpty()) {
                return ResponseResult.fail("400", "商家ID不能为空");
            }
            if (rating == null || rating < 1 || rating > 5) {
                return ResponseResult.fail("400", "评分必须在1-5之间");
            }
            if (content == null || content.trim().isEmpty()) {
                return ResponseResult.fail("400", "评价内容不能为空");
            }

            log.info("用户提交评价，orderId={}, merchantId={}, rating={}", orderId, merchantId, rating);

            // 检查订单是否存在
            Order order = orderService.getById(orderId);
            if (order == null) {
                return ResponseResult.fail("404", "订单不存在");
            }

            // 检查订单状态是否为待评价（7）
            if (order.getStatus() != 7) {
                return ResponseResult.fail("400", "订单状态不正确，无法评价");
            }

            // 检查是否已经评价过
            LambdaQueryWrapper<Review> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(Review::getOrderId, orderId);
            Review existingReview = reviewService.getOne(checkWrapper);
            if (existingReview != null) {
                return ResponseResult.fail("400", "该订单已评价");
            }

            // 创建评价
            Review review = new Review();
            review.setId(generateReviewId());
            review.setUserId(order.getUserId());
            review.setOrderId(orderId);
            review.setMerchantId(merchantId);
            review.setRating(rating);
            review.setContent(content);

            // 将图片列表转换为JSON字符串
            String imagesJson = "[]";
            if (images != null && !images.isEmpty()) {
                try {
                    imagesJson = objectMapper.writeValueAsString(images);
                } catch (Exception e) {
                    log.warn("图片列表转换JSON失败", e);
                }
            }
            review.setImages(imagesJson);

            review.setStatus(0); // 正常状态
            review.setCreateTime(LocalDateTime.now());
            review.setUpdateTime(LocalDateTime.now());

            boolean success = reviewService.save(review);

            if (success) {
                // 更新订单状态为已评价（8）
                order.setStatus(8);
                orderService.updateById(order);

                // 通知用户评价提交成功
                NotificationUtil.createReviewNotification(
                    order.getUserId(),
                    NotificationTypeEnum.REVIEW_SUBMITTED,
                    order.getMerchantId(),
                    orderId
                );

                // 通知商家有新评价
                NotificationUtil.createReviewNotification(
                    order.getMerchantId(),
                    NotificationTypeEnum.REVIEW_SUBMITTED,
                    order.getMerchantId(),
                    orderId
                );

                log.info("用户提交评价成功，reviewId={}, 订单状态已更新为已评价", review.getId());
                return ResponseResult.success(review);
            } else {
                return ResponseResult.fail("500", "提交评价失败");
            }

        } catch (Exception e) {
            log.error("提交评价失败", e);
            return ResponseResult.fail("500", "提交评价失败：" + e.getMessage());
        }
    }

    /**
     * 获取订单的评价详情
     */
    @ApiOperation("获取订单的评价详情")
    @GetMapping("/order/{orderId}")
    public ResponseResult<?> getReviewByOrderId(@PathVariable String orderId) {
        try {
            log.info("获取订单评价详情，orderId={}", orderId);

            LambdaQueryWrapper<Review> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Review::getOrderId, orderId)
                       .eq(Review::getStatus, 0);

            Review review = reviewService.getOne(queryWrapper);

            if (review == null) {
                return ResponseResult.fail("404", "评价不存在");
            }

            // 查询回复信息
            LambdaQueryWrapper<ReviewReply> replyWrapper = new LambdaQueryWrapper<>();
            replyWrapper.eq(ReviewReply::getReviewId, review.getId())
                       .orderByAsc(ReviewReply::getCreateTime);
            List<ReviewReply> replies = reviewReplyService.list(replyWrapper);

            Map<String, Object> result = new HashMap<>();
            result.put("id", review.getId());
            result.put("userId", review.getUserId());
            result.put("orderId", review.getOrderId());
            result.put("merchantId", review.getMerchantId());
            result.put("rating", review.getRating());
            result.put("content", review.getContent());
            result.put("createTime", review.getCreateTime());
            result.put("updateTime", review.getUpdateTime());

            // 将JSON字符串转换为List返回
            try {
                String imagesJson = review.getImages();
                if (imagesJson != null && !imagesJson.isEmpty()) {
                    List<String> imagesList = objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
                    result.put("images", imagesList);
                } else {
                    result.put("images", new ArrayList<>());
                }
            } catch (Exception e) {
                log.warn("解析图片列表失败", e);
                result.put("images", new ArrayList<>());
            }

            // 回复信息
            List<Map<String, Object>> replyList = new ArrayList<>();
            for (ReviewReply reply : replies) {
                Map<String, Object> replyItem = new HashMap<>();
                replyItem.put("id", reply.getId());
                replyItem.put("content", reply.getContent());
                replyItem.put("createTime", reply.getCreateTime());
                replyItem.put("isAdditional", reply.getIsAdditional() != null ? reply.getIsAdditional() : 0);
                replyList.add(replyItem);
            }
            result.put("replies", replyList);

            log.info("返回评价详情，reviewId={}", review.getId());
            return ResponseResult.success(result);

        } catch (Exception e) {
            log.error("获取订单评价详情失败", e);
            return ResponseResult.fail("500", "获取评价详情失败：" + e.getMessage());
        }
    }

    /**
     * 用户追加评价
     */
    @ApiOperation("用户追加评价")
    @PostMapping("/{reviewId}/additional")
    public ResponseResult<?> addAdditionalReview(
            @PathVariable String reviewId,
            @RequestBody Map<String, Object> request
    ) {
        try {
            String content = (String) request.get("content");
            @SuppressWarnings("unchecked")
            List<String> images = (List<String>) request.get("images");

            if (content == null || content.trim().isEmpty()) {
                return ResponseResult.fail("400", "追评内容不能为空");
            }

            log.info("用户追加评价，reviewId={}", reviewId);

            // 检查评价是否存在
            Review review = reviewService.getById(reviewId);
            if (review == null) {
                return ResponseResult.fail("404", "评价不存在");
            }

            // 创建追评回复（使用ReviewReply表存储追评）
            ReviewReply reply = new ReviewReply();
            reply.setId(generateReplyId());
            reply.setReviewId(reviewId);
            reply.setMerchantId(review.getMerchantId());
            reply.setContent(content);
            reply.setIsAdditional(1); // 标记为追评
            reply.setCreateTime(LocalDateTime.now());
            reply.setUpdateTime(LocalDateTime.now());

            boolean success = reviewReplyService.save(reply);

            if (success) {
                // 通知用户追加评价成功
                NotificationUtil.createReviewNotification(
                    review.getUserId(),
                    NotificationTypeEnum.REVIEW_ADDITIONAL,
                    review.getMerchantId(),
                    review.getOrderId()
                );

                log.info("用户追加评价成功，replyId={}", reply.getId());
                return ResponseResult.success(reply);
            } else {
                return ResponseResult.fail("500", "追加评价失败");
            }

        } catch (Exception e) {
            log.error("追加评价失败", e);
            return ResponseResult.fail("500", "追加评价失败：" + e.getMessage());
        }
    }

    /**
     * 生成评价ID
     */
    private String generateReviewId() {
        return "R" + System.currentTimeMillis();
    }

    /**
     * 生成回复ID
     */
    private String generateReplyId() {
        return "RR" + System.currentTimeMillis();
    }

    // ==================== 商家端API ====================

    /**
     * 获取商家评价列表
     */
    @ApiOperation("获取商家评价列表")
    @GetMapping("/merchant/{merchantId}")
    public ResponseResult<?> getMerchantReviews(
            @PathVariable String merchantId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String keyword
    ) {
        try {
            log.info("获取商家评价列表，merchantId={}, status={}, rating={}, keyword={}",
                    merchantId, status, rating, keyword);

            // 查询评价列表
            LambdaQueryWrapper<Review> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Review::getMerchantId, merchantId)
                       .eq(Review::getStatus, 0) // 只查询正常状态的评价
                       .orderByDesc(Review::getCreateTime);

            // 评分筛选
            if (rating != null) {
                queryWrapper.eq(Review::getRating, rating);
            }

            List<Review> reviews = reviewService.list(queryWrapper);

            // 查询回复信息
            List<String> reviewIds = reviews.stream()
                    .map(Review::getId)
                    .collect(Collectors.toList());

            Map<String, List<ReviewReply>> replyMap = new HashMap<>();
            if (!reviewIds.isEmpty()) {
                LambdaQueryWrapper<ReviewReply> replyWrapper = new LambdaQueryWrapper<>();
                replyWrapper.in(ReviewReply::getReviewId, reviewIds)
                           .orderByDesc(ReviewReply::getCreateTime); // 改为倒序，最新的在前
                List<ReviewReply> replies = reviewReplyService.list(replyWrapper);

                replyMap = replies.stream()
                        .collect(Collectors.groupingBy(ReviewReply::getReviewId));
            }

            // 查询用户信息
            List<String> userIds = reviews.stream()
                    .map(Review::getUserId)
                    .distinct()
                    .collect(Collectors.toList());

            Map<String, User> userMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                List<User> users = userService.listByIds(userIds);
                userMap = users.stream()
                        .collect(Collectors.toMap(User::getUserId, u -> u));
            }

            // 查询订单信息
            List<String> orderIds = reviews.stream()
                    .map(Review::getOrderId)
                    .distinct()
                    .collect(Collectors.toList());

            Map<String, Order> orderMap = new HashMap<>();
            Map<String, List<String>> orderDishesMap = new HashMap<>();
            if (!orderIds.isEmpty()) {
                List<Order> orders = orderService.listByIds(orderIds);
                orderMap = orders.stream()
                        .collect(Collectors.toMap(Order::getId, o -> o));

                // 查询订单菜品
                LambdaQueryWrapper<OrderDish> dishWrapper = new LambdaQueryWrapper<>();
                dishWrapper.in(OrderDish::getOrderId, orderIds);
                List<OrderDish> orderDishes = orderDishService.list(dishWrapper);

                // 查询菜品信息以获取菜品名称
                List<String> dishIds = orderDishes.stream()
                        .map(OrderDish::getDishId)
                        .distinct()
                        .collect(Collectors.toList());

                Map<String, String> dishNameMap = new HashMap<>();
                if (!dishIds.isEmpty()) {
                    List<Dish> dishes = dishService.listByIds(dishIds);
                    dishNameMap = dishes.stream()
                            .collect(Collectors.toMap(Dish::getId, Dish::getName));
                }

                // 按订单ID分组，将菜品名称收集
                final Map<String, String> finalDishNameMap = dishNameMap;
                orderDishesMap = orderDishes.stream()
                        .collect(Collectors.groupingBy(
                                OrderDish::getOrderId,
                                Collectors.mapping(
                                        od -> finalDishNameMap.getOrDefault(od.getDishId(), "未知菜品"),
                                        Collectors.toList()
                                )
                        ));
            }

            // 组装返回数据
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (Review review : reviews) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", review.getId());
                item.put("orderId", review.getOrderId());
                item.put("userId", review.getUserId());
                item.put("rating", review.getRating());
                item.put("content", review.getContent());
                item.put("images", review.getImages());
                item.put("createTime", review.getCreateTime());

                // 用户信息
                User user = userMap.get(review.getUserId());
                if (user != null) {
                    item.put("userName", user.getNickname() != null ? user.getNickname() : "用户" + user.getUserId());
                } else {
                    item.put("userName", "未知用户");
                }

                // 订单信息
                Order order = orderMap.get(review.getOrderId());
                if (order != null) {
                    item.put("orderNo", order.getId()); // 使用订单ID作为订单号
                }

                // 菜品信息
                List<String> dishNames = orderDishesMap.get(review.getOrderId());
                if (dishNames != null) {
                    item.put("dishes", dishNames);
                } else {
                    item.put("dishes", new ArrayList<>());
                }

                // 回复信息 - 统一返回所有回复（商家+用户），按时间倒序
                List<ReviewReply> replies = replyMap.get(review.getId());
                List<Map<String, Object>> allReplies = new ArrayList<>();
                boolean hasMerchantReply = false; // 是否有商家回复

                if (replies != null && !replies.isEmpty()) {
                    for (ReviewReply reply : replies) {
                        Map<String, Object> replyItem = new HashMap<>();
                        Integer isAdditional = reply.getIsAdditional();

                        replyItem.put("id", reply.getId());
                        replyItem.put("content", reply.getContent());
                        replyItem.put("time", reply.getCreateTime().toString().replace("T", " ").substring(0, 16));
                        replyItem.put("createTime", reply.getCreateTime()); // 添加原始时间用于排序
                        // isAdditional: 0/null=商家回复, 1=用户追评
                        replyItem.put("isAdditional", isAdditional == null ? 0 : isAdditional);
                        replyItem.put("isMerchant", isAdditional == null || isAdditional == 0);

                        // 检查是否有商家回复
                        if (isAdditional == null || isAdditional == 0) {
                            hasMerchantReply = true;
                        }

                        allReplies.add(replyItem);
                    }

                    // 确保按时间倒序排序（最新的在前）
                    allReplies.sort((a, b) -> {
                        LocalDateTime timeA = (LocalDateTime) a.get("createTime");
                        LocalDateTime timeB = (LocalDateTime) b.get("createTime");
                        return timeB.compareTo(timeA); // 降序：最新的在前
                    });
                }

                // 设置回复状态：只有商家回复才算"已回复"
                item.put("status", hasMerchantReply ? "replied" : "unreplied");

                // 设置所有回复列表（已按时间倒序）
                item.put("replies", allReplies);

                // 格式化时间
                item.put("time", review.getCreateTime().toString().replace("T", " ").substring(0, 16));

                resultList.add(item);
            }

            // 关键词搜索
            if (keyword != null && !keyword.trim().isEmpty()) {
                String lowerKeyword = keyword.toLowerCase();
                resultList = resultList.stream()
                        .filter(item -> {
                            String orderNo = (String) item.get("orderNo");
                            String userName = (String) item.get("userName");
                            @SuppressWarnings("unchecked")
                            List<String> dishes = (List<String>) item.get("dishes");

                            boolean match = (orderNo != null && orderNo.toLowerCase().contains(lowerKeyword)) ||
                                    (userName != null && userName.toLowerCase().contains(lowerKeyword));

                            if (!match && dishes != null) {
                                match = dishes.stream().anyMatch(dish -> dish.toLowerCase().contains(lowerKeyword));
                            }

                            return match;
                        })
                        .collect(Collectors.toList());
            }

            // 状态筛选
            if (status != null && !status.equals("all")) {
                resultList = resultList.stream()
                        .filter(item -> status.equals(item.get("status")))
                        .collect(Collectors.toList());
            }

            log.info("返回评价列表，数量={}", resultList.size());
            return ResponseResult.success(resultList);

        } catch (Exception e) {
            log.error("获取商家评价列表失败", e);
            return ResponseResult.fail("500", "获取评价列表失败：" + e.getMessage());
        }
    }

    /**
     * 回复评价
     */
    @ApiOperation("回复评价")
    @PostMapping("/{reviewId}/reply")
    public ResponseResult<?> replyReview(
            @PathVariable String reviewId,
            @RequestBody Map<String, String> request
    ) {
        try {
            String content = request.get("content");
            String merchantId = request.get("merchantId");

            if (content == null || content.trim().isEmpty()) {
                return ResponseResult.fail("400", "回复内容不能为空");
            }

            log.info("回复评价，reviewId={}, merchantId={}", reviewId, merchantId);

            // 检查评价是否存在
            Review review = reviewService.getById(reviewId);
            if (review == null) {
                return ResponseResult.fail("404", "评价不存在");
            }

            // 创建回复
            ReviewReply reply = new ReviewReply();
            reply.setId(generateReplyId());
            reply.setReviewId(reviewId);
            reply.setMerchantId(merchantId);
            reply.setContent(content);
            reply.setCreateTime(LocalDateTime.now());
            reply.setUpdateTime(LocalDateTime.now());

            boolean success = reviewReplyService.save(reply);

            if (success) {
                // 通知用户商家已回复评价
                NotificationUtil.createReviewNotification(
                    review.getUserId(),
                    NotificationTypeEnum.REVIEW_REPLY,
                    merchantId,
                    review.getOrderId()
                );

                log.info("回复评价成功，replyId={}", reply.getId());
                return ResponseResult.success("回复成功");
            } else {
                return ResponseResult.fail("500", "回复失败");
            }

        } catch (Exception e) {
            log.error("回复评价失败", e);
            return ResponseResult.fail("500", "回复失败：" + e.getMessage());
        }
    }

    /**
     * 获取评价统计
     */
    @ApiOperation("获取评价统计")
    @GetMapping("/merchant/{merchantId}/statistics")
    public ResponseResult<?> getReviewStatistics(@PathVariable String merchantId) {
        try {
            log.info("获取评价统计，merchantId={}", merchantId);

            // 查询所有评价
            LambdaQueryWrapper<Review> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Review::getMerchantId, merchantId)
                       .eq(Review::getStatus, 0);
            List<Review> reviews = reviewService.list(queryWrapper);

            // 统计各星级数量
            Map<Integer, Long> ratingCounts = new HashMap<>();
            for (int i = 1; i <= 5; i++) {
                ratingCounts.put(i, 0L);
            }

            for (Review review : reviews) {
                Integer rating = review.getRating();
                ratingCounts.put(rating, ratingCounts.getOrDefault(rating, 0L) + 1);
            }

            // 计算平均评分
            double avgRating = 0;
            if (!reviews.isEmpty()) {
                double totalRating = reviews.stream()
                        .mapToInt(Review::getRating)
                        .sum();
                avgRating = totalRating / reviews.size();
            }

            // 查询已回复和未回复数量（只统计商家回复，不包括用户追评）
            List<String> reviewIds = reviews.stream()
                    .map(Review::getId)
                    .collect(Collectors.toList());

            long repliedCount = 0;
            long unrepliedCount = reviews.size();

            if (!reviewIds.isEmpty()) {
                // 使用原生SQL统计商家回复数（is_additional = 0 或 null）
                String reviewIdsStr = reviewIds.stream()
                        .map(id -> "'" + id + "'")
                        .collect(Collectors.joining(","));

                String sql = "SELECT DISTINCT review_id FROM t_review_reply " +
                            "WHERE review_id IN (" + reviewIdsStr + ") " +
                            "AND (is_additional = 0 OR is_additional IS NULL)";

                List<Map<String, Object>> merchantReplies = reviewReplyService.listMaps(
                        new QueryWrapper<ReviewReply>().apply(sql)
                );

                repliedCount = merchantReplies.size();
                unrepliedCount = reviews.size() - repliedCount;
            }

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("total", reviews.size());
            statistics.put("avgRating", Math.round(avgRating * 10) / 10.0);
            statistics.put("ratingCounts", ratingCounts);
            statistics.put("repliedCount", repliedCount);
            statistics.put("unrepliedCount", unrepliedCount);

            log.info("评价统计：{}", statistics);
            return ResponseResult.success(statistics);

        } catch (Exception e) {
            log.error("获取评价统计失败", e);
            return ResponseResult.fail("500", "获取评价统计失败：" + e.getMessage());
        }
    }
}
