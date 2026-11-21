package com.xx.jaseatschoicejava.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.entity.AddDishRequest;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.dto.AddDishRequestVO;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * 加菜审核列表的菜品信息补齐（#595）。
 *
 * <p>原先 {@code convertToVO} 解析出 dishItems 后直接丢弃，列表里没有菜名和价格。
 * 这里验证名称、单价、数量都按 dishId 回查补上，菜品已删除时至少保留 dishId。
 */
class AddDishConvertToVOTest {

    private AddDishRequest request(String dishInfoJson) {
        AddDishRequest request = new AddDishRequest();
        request.setId("req-1");
        request.setGroupOrderId("group-1");
        request.setDishInfo(dishInfoJson);
        request.setTotalAmount(new BigDecimal("58.00"));
        request.setApprovalStatus(com.xx.jaseatschoicejava.enums.AddDishApprovalStatus.PENDING.getValue());
        request.setCreatedAt(java.time.LocalDateTime.now());
        return request;
    }

    private AddDishServiceImpl newService(DishService dishService, AddDishRequest pending) {
        AddDishServiceImpl service = spy(new AddDishServiceImpl());
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(service, "dishService", dishService);
        doReturn(List.of(pending)).when(service)
            .list(org.mockito.ArgumentMatchers.any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
        return service;
    }

    @Test
    void reviewList_fillsDishNameAndPrice() {
        AddDishRequest pending = request(
            "[{\"dishId\":\"dish-1\",\"quantity\":2,\"customization\":\"少辣\"},"
                + "{\"dishId\":\"dish-2\",\"quantity\":1}]");

        DishService dishService = mock(DishService.class);
        Dish first = new Dish();
        first.setId("dish-1");
        first.setName("宫保鸡丁");
        first.setPrice(new BigDecimal("28.00"));
        Dish second = new Dish();
        second.setId("dish-2");
        second.setName("米饭");
        second.setPrice(new BigDecimal("2.00"));
        when(dishService.getById("dish-1")).thenReturn(first);
        when(dishService.getById("dish-2")).thenReturn(second);

        List<AddDishRequestVO> result = newService(dishService, pending).getReviewList("group-1");

        assertEquals(1, result.size());
        List<AddDishRequestVO.DishInfo> dishes = result.get(0).getDishInfo();
        assertNotNull(dishes, "dishInfo 不应为空");
        assertEquals(2, dishes.size());
        assertEquals("宫保鸡丁", dishes.get(0).getDishName());
        assertEquals(new BigDecimal("28.00"), dishes.get(0).getPrice());
        assertEquals(2, dishes.get(0).getQuantity());
        assertEquals("少辣", dishes.get(0).getCustomization());
        assertEquals("米饭", dishes.get(1).getDishName());
    }

    @Test
    void reviewList_deletedDishKeepsIdWithoutInventingData() {
        AddDishRequest pending = request("[{\"dishId\":\"gone\",\"quantity\":1}]");

        DishService dishService = mock(DishService.class);
        when(dishService.getById("gone")).thenReturn(null);

        List<AddDishRequestVO.DishInfo> dishes =
            newService(dishService, pending).getReviewList("group-1").get(0).getDishInfo();

        assertEquals("gone", dishes.get(0).getDishId());
        // 查不到就不编菜名和价格
        assertNull(dishes.get(0).getDishName());
        assertNull(dishes.get(0).getPrice());
    }
}
