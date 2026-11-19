package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * 单条评价详情端点（GET /v1/reviews/{reviewId}）测试
 */
@ExtendWith(MockitoExtension.class)
class ReviewControllerDetailTest {

    @Mock
    private ReviewService reviewService;

    @Mock
    private ReviewReplyService reviewReplyService;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderDishService orderDishService;

    @Mock
    private UserService userService;

    @Mock
    private DishService dishService;

    @Mock
    private com.xx.jaseatschoicejava.security.CurrentIdentityResolver identityResolver;

    @InjectMocks
    private ReviewController controller;

    @Test
    void getReviewDetail_shouldReturnSameShapeAsMerchantListItem() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 2, 3, 4);

        Review review = new Review();
        review.setId("r-1");
        review.setMerchantId("m-1");
        review.setOrderId("o-1");
        review.setUserId("u-1");
        review.setRating(5);
        review.setContent("很好吃");
        review.setImages("[\"a.png\"]");
        review.setStatus(0);
        review.setCreatedAt(createdAt);

        ReviewReply merchantReply = new ReviewReply();
        merchantReply.setId("rr-1");
        merchantReply.setReviewId("r-1");
        merchantReply.setContent("谢谢支持");
        merchantReply.setIsAdditional(0);
        merchantReply.setCreatedAt(createdAt.plusMinutes(5));

        User user = new User();
        user.setUserId("u-1");
        user.setNickname("小明");

        Order order = new Order();
        order.setId("o-1");

        OrderDish orderDish = new OrderDish();
        orderDish.setOrderId("o-1");
        orderDish.setDishId("d-1");

        Dish dish = new Dish();
        dish.setId("d-1");
        dish.setName("红烧肉");

        when(reviewService.getById("r-1")).thenReturn(review);
        when(reviewService.list(ArgumentMatchers.<Wrapper<Review>>any())).thenReturn(List.of(review));
        when(reviewReplyService.list(ArgumentMatchers.<Wrapper<ReviewReply>>any())).thenReturn(List.of(merchantReply));
        when(userService.listByIds(anyList())).thenReturn(List.of(user));
        when(orderService.listByIds(anyList())).thenReturn(List.of(order));
        when(orderDishService.list(ArgumentMatchers.<Wrapper<OrderDish>>any())).thenReturn(List.of(orderDish));
        when(dishService.listByIds(anyList())).thenReturn(List.of(dish));

        // 归属校验（#597）：详情页判据是"评价作者本人或该商家"，这里以作者身份放行
        when(identityResolver.canAccessOwnedResource(
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(true);

        ResponseResult<?> detailResponse =
                controller.getReviewDetail("r-1", new org.springframework.mock.web.MockHttpServletRequest());

        assertTrue(detailResponse.getSuccess());
        assertNotNull(detailResponse.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> detail = (Map<String, Object>) detailResponse.getData();

        assertEquals("r-1", detail.get("id"));
        assertEquals("o-1", detail.get("orderId"));
        assertEquals("u-1", detail.get("userId"));
        assertEquals(5, detail.get("rating"));
        assertEquals("很好吃", detail.get("content"));
        assertEquals("[\"a.png\"]", detail.get("images"));
        assertEquals(createdAt, detail.get("createdAt"));
        assertEquals("小明", detail.get("userName"));
        assertEquals("o-1", detail.get("orderNo"));
        assertEquals(List.of("红烧肉"), detail.get("dishes"));
        assertEquals("replied", detail.get("status"));
        assertEquals("2026-01-02 03:04", detail.get("time"));
        assertEquals(1, ((List<?>) detail.get("replies")).size());

        // 与商家评价列表中的单个条目字段口径完全一致
        ResponseResult<?> listResponse = controller.getMerchantReviews("m-1", null, null, null);

        assertTrue(listResponse.getSuccess());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) listResponse.getData();

        assertEquals(1, list.size());
        assertEquals(list.get(0).keySet(), detail.keySet());
    }

    @Test
    void getReviewDetail_shouldReturn404WhenReviewMissing() {
        when(reviewService.getById("missing")).thenReturn(null);

        ResponseResult<?> response =
                controller.getReviewDetail("missing", new org.springframework.mock.web.MockHttpServletRequest());

        assertFalse(response.getSuccess());
        assertEquals("404", response.getCode());
        assertEquals("评价不存在", response.getMessage());
    }

    @Test
    void getReviewDetail_shouldReturn404WhenCallerIsNeitherAuthorNorMerchant() {
        Review review = new Review();
        review.setId("r-other");
        review.setStatus(0);
        review.setUserId("u-other");
        review.setMerchantId("m-other");

        when(reviewService.getById("r-other")).thenReturn(review);
        // 既不是作者也不是该商家 → 与"不存在"同样回 404，避免探测主键
        when(identityResolver.canAccessOwnedResource(
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(false);

        ResponseResult<?> response =
                controller.getReviewDetail("r-other", new org.springframework.mock.web.MockHttpServletRequest());

        assertFalse(response.getSuccess());
        assertEquals("404", response.getCode());
        assertEquals("评价不存在", response.getMessage());
    }

    @Test
    void getReviewDetail_shouldReturn404WhenReviewDeleted() {
        Review deleted = new Review();
        deleted.setId("r-deleted");
        deleted.setStatus(1);

        when(reviewService.getById("r-deleted")).thenReturn(deleted);

        ResponseResult<?> response =
                controller.getReviewDetail("r-deleted", new org.springframework.mock.web.MockHttpServletRequest());

        assertFalse(response.getSuccess());
        assertEquals("404", response.getCode());
        assertEquals("评价不存在", response.getMessage());
    }
}
