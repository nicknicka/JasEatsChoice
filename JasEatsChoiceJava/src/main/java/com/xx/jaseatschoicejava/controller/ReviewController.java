package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.OrderDish;
import com.xx.jaseatschoicejava.entity.Review;
import com.xx.jaseatschoicejava.entity.ReviewReply;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.OrderDishService;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.service.ReviewReplyService;
import com.xx.jaseatschoicejava.service.ReviewService;
import com.xx.jaseatschoicejava.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评价管理控制器（商家端）
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
                           .orderByAsc(ReviewReply::getCreateTime);
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

                // 回复信息
                List<ReviewReply> replies = replyMap.get(review.getId());
                if (replies != null && !replies.isEmpty()) {
                    item.put("status", "replied");
                    // 第一条回复作为主回复
                    ReviewReply firstReply = replies.get(0);
                    item.put("reply", firstReply.getContent());

                    // 后续回复作为追评
                    if (replies.size() > 1) {
                        List<Map<String, Object>> additionalReplies = new ArrayList<>();
                        for (int i = 1; i < replies.size(); i++) {
                            ReviewReply reply = replies.get(i);
                            Map<String, Object> replyItem = new HashMap<>();
                            replyItem.put("content", reply.getContent());
                            replyItem.put("time", reply.getCreateTime());
                            additionalReplies.add(replyItem);
                        }
                        item.put("replies", additionalReplies);
                    } else {
                        item.put("replies", new ArrayList<>());
                    }
                } else {
                    item.put("status", "unreplied");
                    item.put("reply", "");
                    item.put("replies", new ArrayList<>());
                }

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
            reply.setReviewId(reviewId);
            reply.setMerchantId(merchantId);
            reply.setContent(content);
            reply.setCreateTime(LocalDateTime.now());
            reply.setUpdateTime(LocalDateTime.now());

            boolean success = reviewReplyService.save(reply);

            if (success) {
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

            // 查询已回复和未回复数量
            List<String> reviewIds = reviews.stream()
                    .map(Review::getId)
                    .collect(Collectors.toList());

            long repliedCount = 0;
            long unrepliedCount = reviews.size();

            if (!reviewIds.isEmpty()) {
                LambdaQueryWrapper<ReviewReply> replyWrapper = new LambdaQueryWrapper<>();
                replyWrapper.in(ReviewReply::getReviewId, reviewIds)
                           .select(ReviewReply::getReviewId)
                           .groupBy(ReviewReply::getReviewId);
                repliedCount = reviewReplyService.count(replyWrapper);
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
