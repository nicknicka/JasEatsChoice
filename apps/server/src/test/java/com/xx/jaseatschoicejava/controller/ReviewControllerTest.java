package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Review;
import com.xx.jaseatschoicejava.entity.ReviewReply;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.OrderDishService;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.service.ReviewReplyService;
import com.xx.jaseatschoicejava.service.ReviewService;
import com.xx.jaseatschoicejava.service.UserService;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReviewControllerTest {

    @Test
    public void getReviewStatistics_shouldCountReviewsAndMerchantReplies() {
        ReviewService reviewService = mock(ReviewService.class);
        ReviewReplyService reviewReplyService = mock(ReviewReplyService.class);
        OrderService orderService = mock(OrderService.class);
        OrderDishService orderDishService = mock(OrderDishService.class);
        UserService userService = mock(UserService.class);
        DishService dishService = mock(DishService.class);

        ReviewController controller = new ReviewController(
                reviewService,
                reviewReplyService,
                orderService,
                orderDishService,
                userService,
                dishService
        );

        Review review1 = new Review();
        review1.setId("r1");
        review1.setMerchantId("m1");
        review1.setRating(5);

        Review review2 = new Review();
        review2.setId("r2");
        review2.setMerchantId("m1");
        review2.setRating(4);

        ReviewReply merchantReply = new ReviewReply();
        merchantReply.setReviewId("r1");
        merchantReply.setIsAdditional(0);

        ReviewReply additionalReply = new ReviewReply();
        additionalReply.setReviewId("r1");
        additionalReply.setIsAdditional(1);

        when(reviewService.list(org.mockito.ArgumentMatchers.<Wrapper<Review>>any()))
                .thenReturn(List.of(review1, review2));
        when(reviewReplyService.list(org.mockito.ArgumentMatchers.<Wrapper<ReviewReply>>any()))
                .thenReturn(List.of(merchantReply, additionalReply));

        ResponseResult<?> response = controller.getReviewStatistics("m1");

        assertTrue(response.getSuccess());
        assertNotNull(response.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> statistics = (Map<String, Object>) response.getData();
        @SuppressWarnings("unchecked")
        Map<Integer, Long> ratingCounts = (Map<Integer, Long>) statistics.get("ratingCounts");

        assertEquals(Integer.valueOf(2), statistics.get("total"));
        assertEquals(Double.valueOf(4.5), statistics.get("avgRating"));
        assertEquals(Long.valueOf(1L), statistics.get("repliedCount"));
        assertEquals(Long.valueOf(1L), statistics.get("unrepliedCount"));
        assertEquals(Long.valueOf(1L), ratingCounts.get(5));
        assertEquals(Long.valueOf(1L), ratingCounts.get(4));
        assertEquals(Long.valueOf(0L), ratingCounts.get(3));
    }
}
