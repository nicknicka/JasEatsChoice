package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.security.CurrentIdentityResolver;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.MenuDishService;
import com.xx.jaseatschoicejava.service.MenuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 菜单批量操作端点（POST /v1/menus/menu/batch）测试。
 * 重点是：请求体里的 merchantId 不可信，必须由登录身份校验归属后才能动数据。
 */
class MenuControllerBatchTest {

    private MenuService menuService;
    private CurrentIdentityResolver identityResolver;
    private MenuController controller;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        menuService = mock(MenuService.class);
        identityResolver = mock(CurrentIdentityResolver.class);
        controller = new MenuController();
        ReflectionTestUtils.setField(controller, "menuService", menuService);
        ReflectionTestUtils.setField(controller, "menuDishService", mock(MenuDishService.class));
        ReflectionTestUtils.setField(controller, "dishService", mock(DishService.class));
        ReflectionTestUtils.setField(controller, "identityResolver", identityResolver);
        request = new MockHttpServletRequest();
    }

    private ResponseResult<?> invoke(String action, List<String> menuIds, String merchantId) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("menuIds", menuIds);
        params.put("action", action);
        if (merchantId != null) {
            params.put("merchantId", merchantId);
        }
        return controller.batchOperateMenus(params, request);
    }

    @Test
    void activate_delegatesToServiceWhenCallerManagesMerchant() {
        when(identityResolver.canManageMerchant(any(MockHttpServletRequest.class), eq("m-1"))).thenReturn(true);
        when(menuService.batchOperateMenus(List.of("menu-1", "menu-2"), "activate", "m-1")).thenReturn(true);

        ResponseResult<?> response = invoke("activate", List.of("menu-1", "menu-2"), "m-1");

        assertTrue(response.getSuccess());
        assertEquals("批量操作成功", response.getData());
        verify(menuService).batchOperateMenus(List.of("menu-1", "menu-2"), "activate", "m-1");
    }

    @Test
    void deactivate_delegatesToServiceWhenCallerManagesMerchant() {
        when(identityResolver.canManageMerchant(any(MockHttpServletRequest.class), eq("m-1"))).thenReturn(true);
        when(menuService.batchOperateMenus(anyList(), eq("deactivate"), eq("m-1"))).thenReturn(true);

        ResponseResult<?> response = invoke("deactivate", List.of("menu-1"), "m-1");

        assertTrue(response.getSuccess());
        verify(menuService).batchOperateMenus(List.of("menu-1"), "deactivate", "m-1");
    }

    @Test
    void rejectsOtherMerchantWithoutTouchingService() {
        when(identityResolver.canManageMerchant(any(MockHttpServletRequest.class), eq("m-2"))).thenReturn(false);

        ResponseResult<?> response = invoke("activate", List.of("menu-1"), "m-2");

        assertFalse(response.getSuccess());
        assertEquals("403", response.getCode());
        verify(menuService, never()).batchOperateMenus(anyList(), anyString(), anyString());
    }

    @Test
    void rejectsWhenMerchantIdMissing() {
        ResponseResult<?> response = invoke("activate", List.of("menu-1"), null);

        assertFalse(response.getSuccess());
        assertEquals("400", response.getCode());
        verify(menuService, never()).batchOperateMenus(anyList(), anyString(), any());
    }

    @Test
    void rejectsEmptyMenuIds() {
        ResponseResult<?> response = invoke("activate", List.of(), "m-1");

        assertFalse(response.getSuccess());
        assertEquals("400", response.getCode());
        verify(menuService, never()).batchOperateMenus(anyList(), anyString(), any());
    }

    @Test
    void serviceFailureIsReportedAsFailure() {
        when(identityResolver.canManageMerchant(any(MockHttpServletRequest.class), eq("m-1"))).thenReturn(true);
        when(menuService.batchOperateMenus(anyList(), eq("activate"), eq("m-1"))).thenReturn(false);

        ResponseResult<?> response = invoke("activate", List.of("menu-1"), "m-1");

        assertFalse(response.getSuccess());
        assertEquals("500", response.getCode());
    }
}
