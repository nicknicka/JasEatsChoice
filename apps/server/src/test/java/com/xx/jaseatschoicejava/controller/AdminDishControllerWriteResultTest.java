package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.MerchantService;
import com.xx.jaseatschoicejava.util.NotificationUtil;
import com.xx.jaseatschoicejava.util.UserIdentityUtil;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 管理员菜品状态与审核落库结果校验（#595）。
 *
 * <p>这两处原先都忽略 {@code updateById} 的返回值就回「成功」，
 * 这里证伪：落库失败必须返回失败，成功时审核人要取到真实 adminId。
 */
class AdminDishControllerWriteResultTest {

    private AdminDishController newController(DishService dishService) {
        AdminDishController controller = new AdminDishController();
        ReflectionTestUtils.setField(controller, "dishService", dishService);
        ReflectionTestUtils.setField(controller, "merchantService", mock(MerchantService.class));
        return controller;
    }

    private Dish dish() {
        Dish dish = new Dish();
        dish.setId("dish-1");
        dish.setName("宫保鸡丁");
        dish.setMerchantId("merchant-1");
        dish.setStatus(true);
        return dish;
    }

    @Test
    void updateDishStatus_whenPersistFails_returnsFailure() {
        DishService dishService = mock(DishService.class);
        when(dishService.getById("dish-1")).thenReturn(dish());
        when(dishService.updateById(any(Dish.class))).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = newController(dishService)
            .updateDishStatus("dish-1", Map.of("status", "INACTIVE"));

        assertEquals(500, response.getStatusCode().value());
        assertFalse((Boolean) response.getBody().get("success"));
    }

    @Test
    void updateDishStatus_whenPersistSucceeds_returnsSuccess() {
        DishService dishService = mock(DishService.class);
        when(dishService.getById("dish-1")).thenReturn(dish());
        when(dishService.updateById(any(Dish.class))).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = newController(dishService)
            .updateDishStatus("dish-1", Map.of("status", "INACTIVE"));

        assertEquals(200, response.getStatusCode().value());
        assertTrue((Boolean) response.getBody().get("success"));
    }

    @Test
    void submitAudit_whenPersistFails_returnsFailureWithoutNotifying() {
        DishService dishService = mock(DishService.class);
        when(dishService.getById("dish-1")).thenReturn(dish());
        when(dishService.updateById(any(Dish.class))).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(UserIdentityUtil.VERIFIED_USER_ID_ATTRIBUTE, "7");

        try (MockedStatic<NotificationUtil> notifications = mockStatic(NotificationUtil.class)) {
            ResponseEntity<Map<String, Object>> response = newController(dishService)
                .submitAudit("dish-1", Map.of("decision", "approve", "comment", "通过"), request);

            assertEquals(500, response.getStatusCode().value());
            assertFalse((Boolean) response.getBody().get("success"));
            // 落库失败就不该告诉商家"审核通过"
            notifications.verifyNoInteractions();
        }
    }

    @Test
    void submitAudit_persistsRealAdminIdFromToken() {
        DishService dishService = mock(DishService.class);
        when(dishService.getById("dish-1")).thenReturn(dish());
        when(dishService.updateById(any(Dish.class))).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        // JwtAuthenticationFilter 验签后写入的属性，管理端令牌里就是 adminId
        request.setAttribute(UserIdentityUtil.VERIFIED_USER_ID_ATTRIBUTE, "7");

        try (MockedStatic<NotificationUtil> notifications = mockStatic(NotificationUtil.class)) {
            ResponseEntity<Map<String, Object>> response = newController(dishService)
                .submitAudit("dish-1", Map.of("decision", "approve", "comment", "通过"), request);

            assertTrue((Boolean) response.getBody().get("success"));
            ArgumentCaptor<Dish> captor = ArgumentCaptor.forClass(Dish.class);
            verify(dishService).updateById(captor.capture());
            assertEquals(7L, captor.getValue().getAuditAdminId());
            assertEquals("APPROVED", captor.getValue().getAuditStatus());
            assertNotNull(captor.getValue().getAuditTime());
        }
    }

    @Test
    void submitAudit_withoutVerifiedIdentity_leavesAuditAdminNull() {
        DishService dishService = mock(DishService.class);
        when(dishService.getById("dish-1")).thenReturn(dish());
        when(dishService.updateById(any(Dish.class))).thenReturn(true);

        try (MockedStatic<NotificationUtil> notifications = mockStatic(NotificationUtil.class)) {
            newController(dishService)
                .submitAudit("dish-1", Map.of("decision", "reject", "comment", "图片不清"), new MockHttpServletRequest());

            ArgumentCaptor<Dish> captor = ArgumentCaptor.forClass(Dish.class);
            verify(dishService).updateById(captor.capture());
            // 拿不到可信身份时宁可留空，也不要编一个 adminId
            assertEquals(null, captor.getValue().getAuditAdminId());
            verify(dishService, never()).removeById(any(String.class));
        }
    }
}
